package com.example.springbootaidemo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.springbootaidemo.model.Especialidad;
import com.example.springbootaidemo.model.Medico;
import com.example.springbootaidemo.model.Usuario;

public interface MedicoRepository extends JpaRepository<Medico, Long> {
    List<Medico> findByEspecialidad(Especialidad especialidad);

    Optional<Medico> findByUsuario(Usuario usuario);

    List<Medico> findByNombreContainingIgnoreCase(String nombre);

    List<Medico> findByEspecialidadId(Long especialidadId);

}