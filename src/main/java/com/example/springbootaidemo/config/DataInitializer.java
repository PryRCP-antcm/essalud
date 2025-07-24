package com.example.springbootaidemo.config;

import com.example.springbootaidemo.model.Role;
import com.example.springbootaidemo.model.Usuario;
import com.example.springbootaidemo.model.Especialidad;
import com.example.springbootaidemo.repository.RoleRepository;
import com.example.springbootaidemo.repository.UserRepository;
import com.example.springbootaidemo.repository.EspecialidadRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Collections;
import java.util.List;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(RoleRepository roleRepository,
                                      UserRepository userRepository,
                                      EspecialidadRepository especialidadRepository,
                                      BCryptPasswordEncoder encoder) {
        return args -> {
            // Crear roles
            Role adminRole = createRoleIfNotExists(roleRepository, "ROLE_ADMIN");
            createRoleIfNotExists(roleRepository, "ROLE_PACIENTE");
            createRoleIfNotExists(roleRepository, "ROLE_MEDICO");

            // Crear usuario administrador
            String defaultAdminDni = "admin";
            if (userRepository.findByDni(defaultAdminDni).isEmpty()) {
                Usuario admin = new Usuario();
                admin.setDni(defaultAdminDni);
                admin.setPassword(encoder.encode("admin123"));
                admin.setRoles(Collections.singleton(adminRole));
                userRepository.save(admin);
                System.out.println("✅ Usuario administrador creado con DNI: " + defaultAdminDni);
            } else {
                System.out.println("ℹ️ El usuario administrador ya existe.");
            }

            // Crear especialidades
            List<String> especialidades = List.of(
                "Cardiología", "Pediatría", "Neurología",
                "Dermatología", "Oftalmología", "Medicina General"
            );

            for (String nombre : especialidades) {
                createEspecialidadIfNotExists(especialidadRepository, nombre);
            }
        };
    }

    private Role createRoleIfNotExists(RoleRepository repo, String roleName) {
        Role role = repo.findByName(roleName);
        if (role == null) {
            role = new Role();
            role.setName(roleName);
            role = repo.save(role);
            System.out.println("✅ Rol creado: " + roleName);
        } else {
            System.out.println("ℹ️ El rol ya existe: " + roleName);
        }
        return role;
    }

    private void createEspecialidadIfNotExists(EspecialidadRepository repo, String nombre) {
        if (repo.findByNombreIgnoreCase(nombre).isEmpty()) {
            Especialidad esp = new Especialidad();
            esp.setNombre(nombre);
            repo.save(esp);
            System.out.println("✅ Especialidad creada: " + nombre);
        } else {
            System.out.println("ℹ️ La especialidad ya existe: " + nombre);
        }
    }
}
