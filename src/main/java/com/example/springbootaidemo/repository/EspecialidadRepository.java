package com.example.springbootaidemo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.springbootaidemo.model.Especialidad;

public interface EspecialidadRepository extends JpaRepository<Especialidad, Long> {
    Optional<Especialidad> findByNombreIgnoreCase(String nombre); // ✅ debe devolver Optional
}