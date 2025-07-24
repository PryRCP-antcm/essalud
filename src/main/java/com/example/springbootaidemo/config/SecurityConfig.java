package com.example.springbootaidemo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.example.springbootaidemo.service.CustomUserDetailsService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private CustomSuccessHandler customSuccessHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/login").permitAll() // acceso público a login
                        .requestMatchers("/chatbot", "/chatbot/ask", "/agendarcita", "/menu", "/citas", "/perfil",
                                "/historial", "/soporte")
                        .hasRole("PACIENTE")// solo autenticados como ROLE_ADMIN
                        .requestMatchers("/gestionhorarios", "/medicos", "/citas", "/registro-medico")
                        .hasRole("ADMIN") // solo autenticados como ROLE_ADMIN
                        .requestMatchers("/vermedico/**").hasRole("MEDICO") // solo autenticados como ROLE_MEDICO
                        .requestMatchers("/error").permitAll()
                        .anyRequest().permitAll() // Permitir acceso a otras rutas
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .successHandler(customSuccessHandler)
                        .failureUrl("/login?error=true") // Redirige en caso de error
                        .permitAll())
                .logout(logout -> logout
                        .permitAll());
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
    }
}