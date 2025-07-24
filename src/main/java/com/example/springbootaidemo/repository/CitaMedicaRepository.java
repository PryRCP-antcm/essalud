package com.example.springbootaidemo.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.springbootaidemo.model.CitaMedica;
import com.example.springbootaidemo.model.Medico;
import com.example.springbootaidemo.model.Paciente;

public interface CitaMedicaRepository extends JpaRepository<CitaMedica, Long> {
    List<CitaMedica> findByPaciente(Paciente paciente);

    List<CitaMedica> findByMedicoAndFecha(Medico medico, LocalDate fecha);

    List<CitaMedica> findByMedico(Medico medico);

    List<CitaMedica> findByMedicoAndEstado(Medico medico, String estado);

    @Query("SELECT DISTINCT c.paciente FROM CitaMedica c WHERE c.medico = :medico AND c.estado = 'Atendido'")
    List<Paciente> findPacientesAtendidosPorMedico(@Param("medico") Medico medico);

    List<CitaMedica> findByMedicoAndPacienteOrderByFechaDesc(Medico medico, Paciente paciente);

    List<CitaMedica> findByMedicoAndEstadoOrderByFechaAtencionDesc(Medico medico, String estado);

    List<CitaMedica> findByMedicoAndEstadoOrderByFechaAtencionDescHoraAtencionDesc(Medico medico, String estado);

    List<CitaMedica> findByPacienteAndEstadoOrderByFechaAtencionDescHoraAtencionDesc(Paciente paciente, String estado);

}
