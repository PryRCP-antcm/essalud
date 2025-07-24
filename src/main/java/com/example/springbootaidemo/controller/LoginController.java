package com.example.springbootaidemo.controller;

import java.security.Principal;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.springbootaidemo.model.Administrador;
import com.example.springbootaidemo.model.Medico;
import com.example.springbootaidemo.model.Paciente;
import com.example.springbootaidemo.model.Usuario;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;

import com.example.springbootaidemo.repository.AdministradorRepository;
import com.example.springbootaidemo.repository.MedicoRepository;
import com.example.springbootaidemo.repository.PacienteRepository;
import com.example.springbootaidemo.repository.UserRepository;

@Controller
public class LoginController implements ErrorController {
    @Autowired
    private MedicoRepository medicoRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private AdministradorRepository administradorRepository;

    @Autowired
    private UserRepository usuarioRepository;

    @GetMapping("/error")
    public String handleError() {
        return "redirect:/citas"; // Redirige a la página /citas cuando ocurre un error
    }

    @PreAuthorize("hasRole('ROLE_PACIENTE')")
    @GetMapping("/perfil")
    public String verPerfil(Model model, Principal principal) {
        String dni = principal.getName();

        Usuario usuario = usuarioRepository.findByDni(dni).orElse(null);
        if (usuario == null) {
            return "redirect:/login?error=true";
        }

        model.addAttribute("usuario", usuario); // ➡️ atributos como dni, roles, enabled

        // Busca en las entidades relacionadas
        Medico medico = medicoRepository.findByUsuario(usuario).orElse(null);
        if (medico != null) {
            model.addAttribute("user", medico); // ➡️ atributos como nombre, correo, etc.
            return "perfil";
        }

        Paciente paciente = pacienteRepository.findByUsuario(usuario).orElse(null);
        if (paciente != null) {
            model.addAttribute("user", paciente);
            return "perfil";
        }

        Administrador admin = administradorRepository.findByUsuario(usuario).orElse(null);
        if (admin != null) {
            model.addAttribute("user", admin);
            return "perfil";
        }

        return "redirect:/login?error=true";
    }

}
