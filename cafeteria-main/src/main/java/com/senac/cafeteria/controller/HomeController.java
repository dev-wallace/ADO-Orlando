package com.senac.cafeteria.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.senac.cafeteria.models.Usuario;
import com.senac.cafeteria.models.enums.Role; // ← IMPORT CORRETO para Role

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "auth/login"; // ← Agora aponta para templates/auth/login.html
    }

    @GetMapping("/login")
    public String login() {
        return "auth/login"; // ← Mesmo caminho
    }

    @GetMapping("/redirecionarPorRole")
    public String redirecionarPorRole(@AuthenticationPrincipal Usuario usuario) {
        if (usuario != null) {
            if (usuario.getRole() == Role.FUNCIONARIO) {
                return "redirect:/admin/dashboard"; // ← REDIRECIONA PARA DASHBOARD
            } else if (usuario.getRole() == Role.CLIENTE) {
                return "redirect:/menu";
            }
        }
        return "redirect:/login";
    }
}