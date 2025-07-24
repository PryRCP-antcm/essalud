package com.example.springbootaidemo.controller;

import com.example.springbootaidemo.chatbot.ChatSession;
import com.example.springbootaidemo.model.*;
import com.example.springbootaidemo.repository.*;
import com.example.springbootaidemo.service.CitaMedicaService;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;

import java.security.Principal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/chatbot")
public class ChatbotCitaController {

    private final OpenAiChatModel chatModel;

    @Autowired
    private EspecialidadRepository especialidadRepository;
    @Autowired
    private HorarioRepository horarioRepository;
    @Autowired
    private PacienteRepository pacienteRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CitaMedicaService citaMedicaService;

    public ChatbotCitaController(OpenAiChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @PreAuthorize("hasRole('ROLE_PACIENTE')")
    @GetMapping
    public String vistaChat(HttpSession httpSession, Model model) {
        ChatSession session = (ChatSession) httpSession.getAttribute("chatSession");
        if (session == null) {
            session = new ChatSession();
            httpSession.setAttribute("chatSession", session);
        }
        model.addAttribute("mensajes", session.getHistorial());
        return "chatbot";
    }

    @PreAuthorize("hasRole('ROLE_PACIENTE')")
    @PostMapping("/ask")
    public String procesarMensaje(@RequestParam String message, Principal principal,
                                  HttpSession httpSession, Model model) {

        String dni = principal.getName();
        ChatSession session = (ChatSession) httpSession.getAttribute("chatSession");
        if (session == null) {
            session = new ChatSession();
        }

        String respuesta;

        // 🔴 Comando especial: cancelar
        if (message.equalsIgnoreCase("cancelar")) {
            session.reiniciar();
            respuesta = "❌ Has cancelado el proceso. Si deseas agendar otra cita, escribe 'Quiero una cita'.";
            session.addMensaje(respuesta, "BOT");
            httpSession.setAttribute("chatSession", session);
            model.addAttribute("mensajes", session.getHistorial());
            return "chatbot";
        }

        session.addMensaje(message, "USER");

        switch (session.getEstado()) {
            case "INICIO" -> {
                if (message.toLowerCase().contains("cita")) {
                    List<Especialidad> especialidades = especialidadRepository.findAll();
                    String lista = especialidades.stream().map(Especialidad::getNombre)
                            .collect(Collectors.joining(", "));
                    respuesta = "Estas son las especialidades disponibles: " + lista + ". ¿Con cuál deseas continuar?";
                    session.setEstado("ESPERANDO_ESPECIALIDAD");
                } else {
                    respuesta = "👋 Hola, soy tu asistente de citas. Puedes decirme 'Quiero una cita' para comenzar.";
                }
            }

            case "ESPERANDO_ESPECIALIDAD" -> {
                String especialidadNombre = obtenerEspecialidadDesdeOpenAI(message);
                Optional<Especialidad> especialidadOpt = especialidadRepository.findByNombreIgnoreCase(especialidadNombre);
                if (especialidadOpt.isPresent()) {
                    session.setEspecialidad(especialidadOpt.get());
                    session.setEstado("FECHA");
                    respuesta = "📅 ¿Para qué fecha deseas la cita? (formato: AAAA-MM-DD)";
                } else {
                    respuesta = "❌ No encontré esa especialidad. Intenta nuevamente.";
                }
            }

            case "FECHA" -> {
                try {
                    LocalDate fecha = LocalDate.parse(message.trim());
                    session.setFecha(fecha);

                    Long espId = session.getEspecialidad().getId();
                    List<Horario> horarios = horarioRepository.findDisponiblesPorEspecialidadYFecha(espId, fecha);

                    if (!horarios.isEmpty()) {
                        StringBuilder opciones = new StringBuilder("🕒 Horarios disponibles:\n");
                        for (int i = 0; i < horarios.size(); i++) {
                            Horario h = horarios.get(i);
                            opciones.append("%d. %s (%s - %s) con Dr. %s\n".formatted(
                                    i + 1, h.getFecha(), h.getHoraInicio(), h.getHoraFin(), h.getMedico().getNombre()));
                        }
                        opciones.append("👉 Escribe el número del horario que prefieres.");
                        session.setEstado("HORARIO");
                        respuesta = opciones.toString();
                    } else {
                        respuesta = "⚠️ No hay horarios disponibles para esa fecha.";
                    }
                } catch (Exception e) {
                    respuesta = "❌ Formato de fecha incorrecto. Usa AAAA-MM-DD.";
                }
            }

            case "HORARIO" -> {
                try {
                    int seleccion = Integer.parseInt(message.trim()) - 1;
                    Long espId = session.getEspecialidad().getId();
                    LocalDate fecha = session.getFecha();

                    List<Horario> horarios = horarioRepository.findDisponiblesPorEspecialidadYFecha(espId, fecha)
                            .stream().sorted(Comparator.comparing(Horario::getHoraInicio)).collect(Collectors.toList());

                    if (seleccion >= 0 && seleccion < horarios.size()) {
                        Horario seleccionado = horarios.get(seleccion);
                        session.setHorarioSeleccionado(seleccionado);
                        session.setEstado("MOTIVO");
                        respuesta = "✍️ ¿Cuál es el motivo de tu cita?";
                    } else {
                        respuesta = "❌ Selección fuera de rango.";
                    }
                } catch (Exception e) {
                    respuesta = "❌ Ingresa un número válido.";
                }
            }

            case "MOTIVO" -> {
                session.setMotivo(message);
                Paciente paciente = obtenerPacientePorDni(dni);
                if (paciente == null) {
                    respuesta = "❌ Error: paciente no encontrado.";
                    break;
                }

                Horario horario = session.getHorarioSeleccionado();
                Especialidad especialidad = session.getEspecialidad();
                CitaMedica cita = new CitaMedica();
                cita.setPaciente(paciente);
                cita.setEspecialidad(especialidad);
                cita.setHorario(horario);
                cita.setMedico(horario.getMedico());
                cita.setFecha(horario.getFecha());
                cita.setHora(horario.getHoraInicio());
                cita.setMotivo(session.getMotivo());

                citaMedicaService.guardar(cita);

                // Actualizar cupo
                horario.setCupo(horario.getCupo() + 1);
                if (horario.getCupo() >= Horario.MAX_CUPOS) {
                    horario.setDisponible(false);
                }
                horarioRepository.save(horario);

                respuesta = "✅ Cita registrada para el %s a las %s con %s.\nMotivo: %s"
                        .formatted(horario.getFecha(), horario.getHoraInicio(), horario.getMedico().getNombre(),
                                session.getMotivo());

                // Resetear conversación después de agendar
                httpSession.removeAttribute("chatSession");
                session = new ChatSession();
                httpSession.setAttribute("chatSession", session);
            }

            default -> {
                respuesta = "❓ No entendí. Empezando de nuevo...";
                session.setEstado("INICIO");
            }
        }

        session.addMensaje(respuesta, "BOT");
        httpSession.setAttribute("chatSession", session);
        model.addAttribute("mensajes", session.getHistorial());
        return "chatbot";
    }

    private Paciente obtenerPacientePorDni(String dni) {
        return userRepository.findByDni(dni)
                .flatMap(pacienteRepository::findByUsuario)
                .orElse(null);
    }

    private String obtenerEspecialidadDesdeOpenAI(String message) {
        String prompt = """
                Extrae solo el nombre de la especialidad médica del siguiente mensaje:
                "%s"
                Devuélvelo sin explicaciones.
                """.formatted(message);
        return chatModel.call(prompt).trim();
    }
}
