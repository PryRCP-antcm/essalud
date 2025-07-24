package com.example.springbootaidemo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.springbootaidemo.model.Administrador;
import com.example.springbootaidemo.model.Usuario;

public interface AdministradorRepository extends JpaRepository<Administrador, Long> {
    Optional<Administrador> findByUsuario(Usuario usuario);

}