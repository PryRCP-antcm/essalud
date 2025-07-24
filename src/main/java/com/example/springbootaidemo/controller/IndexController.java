package com.example.springbootaidemo.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.springbootaidemo.model.Paciente;
import com.example.springbootaidemo.model.Role;
import com.example.springbootaidemo.model.Usuario;
import com.example.springbootaidemo.repository.PacienteRepository;
import com.example.springbootaidemo.repository.RoleRepository;
import com.example.springbootaidemo.repository.UserRepository;

@Controller
public class IndexController {

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/login")
    public String ShowLogin() {
        return "login";
    }

    @PreAuthorize("hasRole('ROLE_PACIENTE')")
    @GetMapping("/menu")
    public String ShowMenu() {
        return "menu";
    }

    @GetMapping("/riesgo")
    @PreAuthorize("hasRole('ROLE_PACIENTE')")
    public String mostrarFormulario() {
        return "riesgo";
    }

    @PreAuthorize("hasRole('ROLE_PACIENTE')")
    @PostMapping("/riesgo")
    public String procesarFormulario(
            @RequestParam("embarazos") String embarazos,
            @RequestParam("glucosa") String glucosa,
            @RequestParam("presion") String presion,
            @RequestParam("piel") String piel,
            @RequestParam("insulina") String insulina,
            @RequestParam("imc") String imc,
            @RequestParam("funcion") String funcion,
            @RequestParam("edad") String edad,
            Model model) {

        try {
            List<String> comando = new ArrayList<>();
            comando.add("python");
            comando.add("src/main/resources/static/css/python/prediccion_diabetes.py");
            comando.addAll(Arrays.asList(embarazos, glucosa, presion, piel, insulina, imc, funcion, edad));

            ProcessBuilder pb = new ProcessBuilder(comando);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder salida = new StringBuilder();
            String linea;
            while ((linea = reader.readLine()) != null) {
                salida.append(linea).append("\n");
            }

            process.waitFor();
            model.addAttribute("salidaPython", salida.toString().trim());

        } catch (IOException | InterruptedException e) {
            model.addAttribute("salidaPython", "Error: " + e.getMessage());
        }

        return "riesgo";
    }

    @GetMapping("/registro-paciente")
    public String mostrarFormularioRegistro() {
        return "registro-paciente";
    }

    @PostMapping("/registrar-paciente")
    public String registrarPaciente(@RequestParam String dni,
            @RequestParam String password,
            @RequestParam String nombre,
            @RequestParam String apellido,
            @RequestParam String correo,
            @RequestParam String telefono) {

        // Crear usuario
        Usuario usuario = new Usuario();
        usuario.setDni(dni);
        usuario.setPassword(passwordEncoder.encode(password));
        usuario.setEnabled(true);

        Role rolPaciente = roleRepository.findByName("ROLE_PACIENTE");
        usuario.getRoles().add(rolPaciente);

        userRepository.save(usuario);

        // Crear paciente
        Paciente paciente = new Paciente();
        paciente.setNombre(nombre);
        paciente.setApellido(apellido);
        paciente.setCorreo(correo);
        paciente.setTelefono(telefono);
        paciente.setUsuario(usuario);

        pacienteRepository.save(paciente);

        return "redirect:/login"; // puedes redirigir a una vista de lista o de login
    }

}
