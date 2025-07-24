package com.example.springbootaidemo.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.springbootaidemo.model.Horario;
import com.example.springbootaidemo.model.Medico;
import com.example.springbootaidemo.repository.HorarioRepository;
import com.example.springbootaidemo.repository.MedicoRepository;

@Service
public class HorarioService {

    @Autowired
    private HorarioRepository horarioRepository;

    @Autowired
    private MedicoRepository medicoRepository;

    // ✅ Método esperado por el Controller
    public void crearHorariosPorRango(Long medicoId, LocalDate fechaInicio, LocalDate fechaFin,
            LocalTime horaInicio, LocalTime horaFin) {

        Medico medico = medicoRepository.findById(medicoId).orElse(null);
        if (medico == null || fechaInicio == null || fechaFin == null || horaInicio == null || horaFin == null) {
            return;
        }

        List<Horario> horarios = new ArrayList<>();

        for (LocalDate fecha = fechaInicio; !fecha.isAfter(fechaFin); fecha = fecha.plusDays(1)) {
            Horario horario = new Horario();
            horario.setMedico(medico);
            horario.setFecha(fecha);
            horario.setHoraInicio(horaInicio);
            horario.setHoraFin(horaFin);
            horario.setDisponible(true); // Por defecto, disponible
            horarios.add(horario);
            horario.setCupo(0); // al crear nuevo horario

        }

        horarioRepository.saveAll(horarios);
    }

    public void eliminarHorario(Long id) {
        horarioRepository.deleteById(id);
    }

    public List<Horario> obtenerTodos() {
        return horarioRepository.findAll();
    }

    public List<Horario> obtenerPorMedicoYFecha(Long medicoId, LocalDate fecha) {
        Medico medico = medicoRepository.findById(medicoId).orElse(null);
        return medico != null ? horarioRepository.findByMedicoAndFechaAndDisponibleTrue(medico, fecha) : List.of();
    }

    public List<Horario> buscarPorFiltros(Long especialidadId, Long medicoId, LocalDate fecha) {
        if (especialidadId != null && medicoId == null && fecha == null) {
            return horarioRepository.findByMedico_Especialidad_Id(especialidadId);
        } else if (especialidadId == null && medicoId != null && fecha == null) {
            return horarioRepository.findByMedicoId(medicoId);
        } else if (especialidadId == null && medicoId == null && fecha != null) {
            return horarioRepository.findByFecha(fecha);
        } else if (especialidadId != null && medicoId != null && fecha == null) {
            return horarioRepository.findByMedicoIdAndEspecialidadId(medicoId, especialidadId);
        } else if (medicoId != null && fecha != null && especialidadId == null) {
            return horarioRepository.findByMedicoIdAndFecha(medicoId, fecha);
        } else if (especialidadId != null && fecha != null && medicoId == null) {
            return horarioRepository.findByMedico_Especialidad_IdAndFecha(especialidadId, fecha);
        } else {
            return horarioRepository.findAll(); // Sin filtros
        }
    }

}