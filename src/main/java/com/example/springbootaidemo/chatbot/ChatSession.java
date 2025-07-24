package com.example.springbootaidemo.chatbot;

import com.example.springbootaidemo.model.Especialidad;
import com.example.springbootaidemo.model.Horario;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ChatSession {
    private String estado = "INICIO";
    private Especialidad especialidad;
    private LocalDate fecha;
    private Horario horarioSeleccionado;
    private String motivo;
    private List<Mensaje> historial = new ArrayList<>();

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Especialidad getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(Especialidad especialidad) {
        this.especialidad = especialidad;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public Horario getHorarioSeleccionado() {
        return horarioSeleccionado;
    }

    public void setHorarioSeleccionado(Horario horarioSeleccionado) {
        this.horarioSeleccionado = horarioSeleccionado;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public List<Mensaje> getHistorial() {
        return historial;
    }

    public void addMensaje(String texto, String tipo) {
        this.historial.add(new Mensaje(texto, tipo));
    }

    public void reiniciar() {
        this.estado = "INICIO";
        this.especialidad = null;
        this.fecha = null;
        this.horarioSeleccionado = null;
        this.motivo = null;
        this.historial.clear();
    }

}
