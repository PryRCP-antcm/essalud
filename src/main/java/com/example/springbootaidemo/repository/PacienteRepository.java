package com.example.springbootaidemo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.springbootaidemo.model.Paciente;
import com.example.springbootaidemo.model.Usuario;

public interface PacienteRepository extends JpaRepository<Paciente, Long> {
    Optional<Paciente> findByUsuario(Usuario usuario);

}
