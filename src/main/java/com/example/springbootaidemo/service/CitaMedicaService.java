package com.example.springbootaidemo.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.springbootaidemo.model.CitaMedica;
import com.example.springbootaidemo.model.Horario;
import com.example.springbootaidemo.model.Medico;
import com.example.springbootaidemo.model.Paciente;
import com.example.springbootaidemo.repository.CitaMedicaRepository;
import com.example.springbootaidemo.repository.HorarioRepository;

@Service
public class CitaMedicaService {

    @Autowired
    private CitaMedicaRepository citaMedicaRepository;

    @Autowired
    private HorarioRepository horarioRepository;

    // Listar todas las citas
    public List<CitaMedica> listarTodas() {
        return citaMedicaRepository.findAll();
    }

    // Guardar una nueva cita y marcar el horario como no disponible
    public CitaMedica guardar(CitaMedica cita) {
        return citaMedicaRepository.save(cita);
    }

    // Buscar una cita por ID
    public CitaMedica buscarPorId(Long id) {
        return citaMedicaRepository.findById(id).orElse(null);
    }

    // Eliminar una cita y liberar el horario
    public boolean eliminar(Long id) {
        CitaMedica cita = buscarPorId(id);
        if (cita != null) {
            Horario horario = cita.getHorario();
            if (horario != null) {
                horario.setDisponible(true);
                horarioRepository.save(horario);
            }
            citaMedicaRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // Actualizar cita (por simplicidad no cambia el horario aquí)
    public CitaMedica actualizar(Long id, CitaMedica actualizada) {
        return citaMedicaRepository.findById(id).map(cita -> {
            cita.setMotivo(actualizada.getMotivo());
            cita.setMedico(actualizada.getMedico());
            cita.setEspecialidad(actualizada.getEspecialidad());
            cita.setPaciente(actualizada.getPaciente());
            return citaMedicaRepository.save(cita);
        }).orElse(null);
    }

    public List<CitaMedica> buscarPorPaciente(Paciente paciente) {
        return citaMedicaRepository.findByPaciente(paciente);
    }

    public List<CitaMedica> buscarPorMedicoYFecha(Medico medico, LocalDate fecha) {
        return citaMedicaRepository.findByMedicoAndFecha(medico, fecha);
    }

}