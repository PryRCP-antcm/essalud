package com.example.springbootaidemo.controller;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.springbootaidemo.model.CitaMedica;
import com.example.springbootaidemo.model.Especialidad;
import com.example.springbootaidemo.model.Horario;
import com.example.springbootaidemo.model.Medico;
import com.example.springbootaidemo.model.Paciente;
import com.example.springbootaidemo.model.Usuario;
import com.example.springbootaidemo.repository.CitaMedicaRepository;
import com.example.springbootaidemo.repository.EspecialidadRepository;
import com.example.springbootaidemo.repository.HorarioRepository;
import com.example.springbootaidemo.repository.MedicoRepository;
import com.example.springbootaidemo.repository.PacienteRepository;
import com.example.springbootaidemo.repository.UserRepository;
import com.example.springbootaidemo.service.CitaMedicaService;

@Controller
public class CitaMedicaController {

    @Autowired
    private CitaMedicaService citaMedicaService;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private MedicoRepository medicoRepository;

    @Autowired
    private EspecialidadRepository especialidadRepository;

    @Autowired
    private HorarioRepository horarioRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CitaMedicaRepository citaMedicaRepository;

    // ✅ Vista principal para agendar citas
    @PreAuthorize("hasRole('ROLE_PACIENTE')")
    @GetMapping("/agendarcita")
    public String verCitas(Model model, Principal principal) {
        String dni = principal.getName();
        Usuario usuario = userRepository.findByDni(dni).orElse(null);
        if (usuario == null)
            return "redirect:/login?error=true";

        Paciente paciente = pacienteRepository.findByUsuario(usuario).orElse(null);
        if (paciente == null)
            return "redirect:/login?error=true";

        model.addAttribute("pacienteActual", paciente);
        model.addAttribute("especialidades", especialidadRepository.findAll());
        model.addAttribute("horarios", horarioRepository.findByDisponibleTrue());
        return "agendarcita";
    }

    // ✅ Registrar una nueva cita desde el formulario
    @PreAuthorize("hasRole('ROLE_PACIENTE')")
    @PostMapping("/citas")
    public String guardarCita(@RequestParam Long pacienteId,
            @RequestParam Long especialidadId,
            @RequestParam Long horarioId,
            @RequestParam String motivo) {

        Paciente paciente = pacienteRepository.findById(pacienteId).orElse(null);
        Especialidad especialidad = especialidadRepository.findById(especialidadId).orElse(null);
        Horario horario = horarioRepository.findById(horarioId).orElse(null);

        if (paciente != null && especialidad != null && horario != null && horario.isDisponible()) {

            System.out.println("Horario ID: " + horario.getId());
            System.out.println("Cupo actual antes de registrar: " + horario.getCupo());
            System.out.println("Disponible: " + horario.isDisponible());

            if (horario.getCupo() < Horario.MAX_CUPOS) {
                Medico medico = horario.getMedico();

                CitaMedica cita = new CitaMedica();
                cita.setPaciente(paciente);
                cita.setEspecialidad(especialidad);
                cita.setHorario(horario);
                cita.setMedico(medico);
                cita.setFecha(horario.getFecha());
                cita.setHora(horario.getHoraInicio());
                cita.setMotivo(motivo);

                citaMedicaService.guardar(cita);

                // Incrementar el cupo
                horario.setCupo(horario.getCupo() + 1);

                // Marcar como no disponible si ya llegó al límite
                if (horario.getCupo() >= Horario.MAX_CUPOS) {
                    horario.setDisponible(false);
                }

                horarioRepository.save(horario);
                System.out.println("Cita registrada. Cupo nuevo: " + horario.getCupo());
            } else {
                System.out.println("No se puede registrar, cupo lleno.");
            }
        }

        return "redirect:/citas";
    }

    @PreAuthorize("hasRole('ROLE_PACIENTE')")
    @GetMapping("/citas")
    public String listarCitasPaciente(Model model, Principal principal) {
        String dni = principal.getName();
        Usuario usuario = userRepository.findByDni(dni).orElse(null);
        Paciente paciente = pacienteRepository.findByUsuario(usuario).orElse(null);

        // Solo citas pendientes
        List<CitaMedica> citas = citaMedicaService.buscarPorPaciente(paciente)
                .stream()
                .filter(c -> c.getEstado().equalsIgnoreCase("Pendiente"))
                .toList();

        model.addAttribute("citas", citas);
        return "citas";
    }

    @PostMapping("/citas/cancelar/{id}")
    @PreAuthorize("hasRole('ROLE_PACIENTE')")
    public String cancelarCita(@PathVariable Long id) {
        citaMedicaService.eliminar(id); // o marcarla como cancelada si prefieres
        return "redirect:/citas";
    }

    @GetMapping("/citas/detalle/{id}")
    @PreAuthorize("hasRole('ROLE_PACIENTE')")
    public String verDetalleCita(@PathVariable Long id, Model model) {
        CitaMedica cita = citaMedicaService.buscarPorId(id);
        model.addAttribute("cita", cita);
        return "detalle-cita";
    }

    @GetMapping("/historial")
    @PreAuthorize("hasRole('ROLE_PACIENTE')")
    public String verHistorial(Model model, Principal principal) {
        String dni = principal.getName();
        Usuario usuario = userRepository.findByDni(dni).orElse(null);
        Paciente paciente = pacienteRepository.findByUsuario(usuario).orElse(null);

        List<CitaMedica> citas = citaMedicaRepository.findByPacienteAndEstadoOrderByFechaAtencionDescHoraAtencionDesc(paciente, "Atendido");
        model.addAttribute("citas", citas);
        return "historial";
    }

    // ✅ API: obtener todas las citas
    @GetMapping("/api/citas")
    @ResponseBody
    public List<CitaMedica> obtenerCitasApi() {
        return citaMedicaService.listarTodas();
    }

    // ✅ API: crear cita desde JSON
    @PostMapping("/api/citas")
    @ResponseBody
    public CitaMedica crearCitaApi(@RequestBody CitaMedica cita) {
        return citaMedicaService.guardar(cita);
    }

    // ✅ API: eliminar cita por ID
    @DeleteMapping("/api/citas/{id}")
    @ResponseBody
    public void eliminarCita(@PathVariable Long id) {
        citaMedicaService.eliminar(id);
    }

    // ✅ API: actualizar cita
    @PutMapping("/api/citas/{id}")
    @ResponseBody
    public ResponseEntity<CitaMedica> actualizarCita(@PathVariable Long id, @RequestBody CitaMedica nuevaCita) {
        CitaMedica actualizada = citaMedicaService.actualizar(id, nuevaCita);
        return actualizada != null ? ResponseEntity.ok(actualizada) : ResponseEntity.notFound().build();
    }

    // ✅ API: buscar por paciente
    @GetMapping("/api/citas/paciente/{id}")
    @ResponseBody
    public List<CitaMedica> buscarPorPaciente(@PathVariable Long id) {
        Paciente paciente = pacienteRepository.findById(id).orElse(null);
        return paciente != null ? citaMedicaService.buscarPorPaciente(paciente) : List.of();
    }

    // ✅ API: buscar por médico y fecha
    @GetMapping("/api/citas/medico/{medicoId}")
    @ResponseBody
    public List<CitaMedica> buscarPorMedicoYFecha(@PathVariable Long medicoId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        Medico medico = medicoRepository.findById(medicoId).orElse(null);
        return medico != null ? citaMedicaService.buscarPorMedicoYFecha(medico, fecha) : List.of();
    }

    // ✅ API: obtener horarios disponibles por especialidad y fecha (AJAX)
    @GetMapping("/api/horarios/disponibles")
    @ResponseBody
    public List<Horario> obtenerHorariosDisponibles(
            @RequestParam Long especialidadId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {

        return horarioRepository.findDisponiblesPorEspecialidadYFecha(especialidadId, fecha);
    }
}