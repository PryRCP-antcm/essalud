package com.example.springbootaidemo.chatbot;

public class Mensaje {
    private String texto;
    private String tipo; // "USER" o "BOT"

    public Mensaje(String texto, String tipo) {
        this.texto = texto;
        this.tipo = tipo;
    }

    public String getTexto() {
        return texto;
    }

    public String getTipo() {
        return tipo;
    }

}
