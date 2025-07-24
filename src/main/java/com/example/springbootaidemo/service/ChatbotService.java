package com.example.springbootaidemo.service;

import org.springframework.stereotype.Service;

import com.example.springbootaidemo.chatbot.ChatSession;

@Service
public class ChatbotService {

    public String procesarMensaje(ChatSession session, String message) {
        return "Respuesta del chatbot";
    }
}