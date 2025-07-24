package com.example.springbootaidemo.controller;

import com.example.springbootaidemo.model.Especialidad;
import com.example.springbootaidemo.model.Medico;
import com.example.springbootaidemo.model.Role;
import com.example.springbootaidemo.model.Usuario;
import com.example.springbootaidemo.repository.EspecialidadRepository;
import com.example.springbootaidemo.repository.MedicoRepository;
import com.example.springbootaidemo.repository.RoleRepository;
import com.example.springbootaidemo.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/medicos")
public class MedicoController {

    @Autowired
    private MedicoRepository medicoRepository;

    @Autowired
    private EspecialidadRepository especialidadRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("")
    public String listarMedicos(@RequestParam(required = false) String nombre,
            @RequestParam(required = false) Long especialidadId,
            Model model) {
        List<Medico> medicos;
        if (nombre != null && !nombre.isEmpty()) {
            medicos = medicoRepository.findByNombreContainingIgnoreCase(nombre);
        } else if (especialidadId != null) {
            medicos = medicoRepository.findByEspecialidadId(especialidadId);
        } else {
            medicos = medicoRepository.findAll();
        }

        model.addAttribute("medicos", medicos);
        model.addAttribute("especialidades", especialidadRepository.findAll());
        return "medicos";
    }

    @GetMapping("/registrar")
    public String mostrarFormularioRegistro(Model model) {
        model.addAttribute("especialidades", especialidadRepository.findAll());
        return "registro-medico";
    }

    @PostMapping("/registrar")
    public String registrarMedico(@RequestParam String dni,
            @RequestParam String password,
            @RequestParam String nombre,
            @RequestParam String correo,
            @RequestParam String telefono,
            @RequestParam Long especialidadId) {

        Usuario usuario = new Usuario();
        usuario.setDni(dni);
        usuario.setPassword(passwordEncoder.encode(password));
        usuario.setEnabled(true);
        Role rolMedico = roleRepository.findByName("ROLE_MEDICO");
        usuario.getRoles().add(rolMedico);
        userRepository.save(usuario);

        Medico medico = new Medico();
        medico.setNombre(nombre);
        medico.setCorreo(correo);
        medico.setTelefono(telefono);
        medico.setUsuario(usuario);
        Especialidad especialidad = especialidadRepository.findById(especialidadId).orElse(null);
        medico.setEspecialidad(especialidad);
        medicoRepository.save(medico);

        return "redirect:/medicos";
    }
}
