package com.example.springbootaidemo.service;

import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.springbootaidemo.model.Usuario;
import com.example.springbootaidemo.repository.UserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String dni) throws UsernameNotFoundException {
        Usuario user = userRepository.findByDni(dni)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con DNI: " + dni));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getDni())
                .password(user.getPassword())
                .disabled(!user.isEnabled())
                .authorities(user.getRoles().stream()
                        .map(role -> new SimpleGrantedAuthority(role.getName()))
                        .collect(Collectors.toList()))
                .build();
    }
}