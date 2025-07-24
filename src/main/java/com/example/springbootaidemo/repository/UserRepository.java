package com.example.springbootaidemo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.springbootaidemo.model.Usuario;

@Repository
public interface UserRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByDni(String dni);
}