package com.example.springbootaidemo.controller;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import com.example.springbootaidemo.model.CitaMedica;
import com.example.springbootaidemo.model.Medico;
import com.example.springbootaidemo.model.Paciente;
import com.example.springbootaidemo.model.Usuario;
import com.example.springbootaidemo.repository.CitaMedicaRepository;
import com.example.springbootaidemo.repository.MedicoRepository;
import com.example.springbootaidemo.repository.PacienteRepository;
import com.example.springbootaidemo.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/vermedico")
public class VerMedicoController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MedicoRepository medicoRepository;

    @Autowired
    private CitaMedicaRepository citaMedicaRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @PreAuthorize("hasRole('ROLE_MEDICO')")
    @GetMapping("/reservasMedico")
    public String verReservasMedico(Model model, Principal principal) {
        // 1. Obtener el DNI del usuario autenticado
        String dni = principal.getName();

        // 2. Buscar el Usuario por DNI
        Usuario usuario = userRepository.findByDni(dni)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // 3. Obtener el médico asociado a ese usuario
        Medico medico = medicoRepository.findByUsuario(usuario)
                .orElseThrow(() -> new RuntimeException("Médico no encontrado"));

        // 4. Buscar todas las citas asignadas a ese médico
        List<CitaMedica> citas = citaMedicaRepository.findByMedicoAndEstado(medico, "Pendiente");

        // 5. Pasar las citas a la vista
        model.addAttribute("citas", citas);

        return "reservas-medico";
    }

    @GetMapping("/detalle-reserva/{id}")
    @PreAuthorize("hasRole('ROLE_MEDICO')")
    public String verDetalleReserva(@PathVariable Long id, Model model) {
        CitaMedica cita = citaMedicaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada"));

        model.addAttribute("cita", cita);
        return "detalle-reserva";
    }

    @PostMapping("/cambiar-estado/{id}")
    @PreAuthorize("hasRole('ROLE_MEDICO')")
    public String cambiarEstadoCita(@PathVariable Long id) {
        CitaMedica cita = citaMedicaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada"));

        cita.setEstado("Atendido");
        citaMedicaRepository.save(cita);

        return "redirect:/vermedico/detalle-reserva/" + id;
    }

    @PostMapping("/marcarAtendido/{id}")
    @PreAuthorize("hasRole('ROLE_MEDICO')")
    public String marcarComoAtendido(@PathVariable Long id) {
        CitaMedica cita = citaMedicaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada"));

        cita.setEstado("Atendido");
        LocalTime horaActual = LocalTime.now().withSecond(0).withNano(0);
        cita.setHoraAtencion(horaActual);
        cita.setFechaAtencion(LocalDate.now());

        citaMedicaRepository.save(cita);

        return "redirect:/vermedico/detalle-reserva/" + id;
    }

    @GetMapping("/historial")
    @PreAuthorize("hasRole('ROLE_MEDICO')")
    public String verHistorialCitas(Model model, Principal principal) {
        String dni = principal.getName();
        Usuario usuario = userRepository.findByDni(dni)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        Medico medico = medicoRepository.findByUsuario(usuario)
                .orElseThrow(() -> new RuntimeException("Médico no encontrado"));
        List<CitaMedica> citas = citaMedicaRepository
                .findByMedicoAndEstadoOrderByFechaAtencionDescHoraAtencionDesc(medico, "Atendido");

        model.addAttribute("citas", citas);
        return "historial-reservas";
    }

    @GetMapping("/mis-pacientes")
    @PreAuthorize("hasRole('ROLE_MEDICO')")
    public String verPacientesAtendidos(Model model, Principal principal) {
        String dni = principal.getName();
        Usuario usuario = userRepository.findByDni(dni)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        Medico medico = medicoRepository.findByUsuario(usuario)
                .orElseThrow(() -> new RuntimeException("Médico no encontrado"));

        List<Paciente> pacientes = citaMedicaRepository.findPacientesAtendidosPorMedico(medico);
        model.addAttribute("pacientes", pacientes);
        return "mis-pacientes";
    }

    @GetMapping("/paciente/{pacienteId}/citas")
    @PreAuthorize("hasRole('ROLE_MEDICO')")
    public String verCitasDePaciente(@PathVariable Long pacienteId, Model model, Principal principal) {
        String dni = principal.getName();
        Usuario usuario = userRepository.findByDni(dni)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        Medico medico = medicoRepository.findByUsuario(usuario)
                .orElseThrow(() -> new RuntimeException("Médico no encontrado"));
        Paciente paciente = pacienteRepository.findById(pacienteId)
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));

        List<CitaMedica> citas = citaMedicaRepository
                .findByMedicoAndEstadoOrderByFechaAtencionDescHoraAtencionDesc(medico, "Atendido");
        model.addAttribute("paciente", paciente);
        model.addAttribute("citas", citas);
        return "citas-paciente-medico";
    }

}