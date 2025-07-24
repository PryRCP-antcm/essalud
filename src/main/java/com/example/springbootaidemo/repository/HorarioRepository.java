package com.example.springbootaidemo.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.springbootaidemo.model.Horario;
import com.example.springbootaidemo.model.Medico;

public interface HorarioRepository extends JpaRepository<Horario, Long> {

    // Buscar por especialidad (a través del médico)
    List<Horario> findByMedico_Especialidad_Id(Long especialidadId);

    // Buscar por médico
    List<Horario> findByMedicoId(Long medicoId);

    // Buscar por fecha
    List<Horario> findByFecha(LocalDate fecha);

    // Buscar por médico y fecha
    List<Horario> findByMedicoIdAndFecha(Long medicoId, LocalDate fecha);

    // ✅ CORRIGE AQUÍ (eliminamos el incorrecto):
    // Elimina esto porque Horario no tiene campo especialidadId directamente:
    // List<Horario> findByEspecialidadIdAndFecha(Long especialidadId, LocalDate fecha);

    // ✅ Si quieres buscar por especialidad y fecha (por relación):
    List<Horario> findByMedico_Especialidad_IdAndFecha(Long especialidadId, LocalDate fecha);

    // Buscar horarios disponibles por médico y fecha
    List<Horario> findByMedicoAndFechaAndDisponibleTrue(Medico medico, LocalDate fecha);

    // Buscar horarios disponibles
    List<Horario> findByDisponibleTrue();

    // ✅ Consulta personalizada para horarios disponibles por especialidad y fecha
    @Query("SELECT h FROM Horario h WHERE h.disponible = true AND h.medico.especialidad.id = :especialidadId AND h.fecha = :fecha")
    List<Horario> findDisponiblesPorEspecialidadYFecha(@Param("especialidadId") Long especialidadId,
                                                       @Param("fecha") LocalDate fecha);

    // ✅ Esta query es válida pero opcional si ya tienes findByMedico_Especialidad_Id
    @Query("SELECT h FROM Horario h WHERE h.medico.id = :medicoId AND h.medico.especialidad.id = :especialidadId")
    List<Horario> findByMedicoIdAndEspecialidadId(@Param("medicoId") Long medicoId,
                                                  @Param("especialidadId") Long especialidadId);
}
