package com.example.springbootaidemo.controller;

import java.time.LocalDateTime;

public class ReservaParcial {
    public String nombreCliente;
    public Integer cantidadPersonas;
    public LocalDateTime fechaHora;

    public boolean isCompleta() {
        return nombreCliente != null && cantidadPersonas != null && fechaHora != null;
    }
}