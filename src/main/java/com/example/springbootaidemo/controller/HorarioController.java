package com.example.springbootaidemo.controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.springbootaidemo.model.Especialidad;
import com.example.springbootaidemo.model.Horario;
import com.example.springbootaidemo.model.Medico;
import com.example.springbootaidemo.repository.EspecialidadRepository;
import com.example.springbootaidemo.repository.MedicoRepository;
import com.example.springbootaidemo.service.HorarioService;

@Controller
public class HorarioController {

    @Autowired
    private HorarioService horarioService;

    @Autowired
    private EspecialidadRepository especialidadRepository;

    @Autowired
    private MedicoRepository medicoRepository;

    // Vista principal de gestion de horarios
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/gestionhorarios")
    public String mostrarFormulario(@RequestParam(required = false) Long especialidadId,
            @RequestParam(required = false) Long medicoId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            Model model) {

        List<Horario> horarios = horarioService.buscarPorFiltros(especialidadId, medicoId, fecha);

        model.addAttribute("especialidades", especialidadRepository.findAll());
        model.addAttribute("medicos", medicoRepository.findAll());
        model.addAttribute("horarios", horarios);

        return "gestionhorarios";
    }

    // Obtener medicos por especialidad (para AJAX)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/api/medicos/porEspecialidad")
    @ResponseBody
    public List<Medico> obtenerMedicosPorEspecialidad(@RequestParam Long especialidadId) {
        Especialidad esp = especialidadRepository.findById(especialidadId).orElse(null);
        return esp != null ? medicoRepository.findByEspecialidad(esp) : List.of();
    }

    // Registrar horarios en rango de fechas para un medico
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/horarios/crear")
    public String crearHorario(@RequestParam Long medicoId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime horaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime horaFin) {

        horarioService.crearHorariosPorRango(medicoId, fechaInicio, fechaFin, horaInicio, horaFin);
        return "redirect:/gestionhorarios";
    }

    // Eliminar horario por ID
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/horarios/eliminar/{id}")
    public String eliminarHorario(@PathVariable Long id) {
        horarioService.eliminarHorario(id);
        return "redirect:/gestionhorarios";
    }

    // API: Obtener todos los horarios (opcional)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/api/horarios")
    @ResponseBody
    public List<Horario> obtenerHorarios() {
        return horarioService.obtenerTodos();
    }

    // API: Obtener horarios por medico y fecha (opcional)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/api/horarios/medico")
    @ResponseBody
    public List<Horario> horariosPorMedicoYFecha(@RequestParam Long medicoId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return horarioService.obtenerPorMedicoYFecha(medicoId, fecha);
    }

}
