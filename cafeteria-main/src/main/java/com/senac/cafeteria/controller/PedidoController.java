package com.senac.cafeteria.controller;

import com.senac.cafeteria.models.Usuario;
import com.senac.cafeteria.services.PedidoService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/pedidos")
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService pedidoService;

    @GetMapping
    public String meusPedidos(@AuthenticationPrincipal Usuario usuario, Model model) {
        var pedidos = pedidoService.listarPedidosPorUsuario(usuario);
        model.addAttribute("pedidos", pedidos);
        return "cliente/pedidos";
    }

    @GetMapping("/{id}")
    public String detalhesPedido(@AuthenticationPrincipal Usuario usuario,
                                @PathVariable Long id,
                                Model model) {
        var pedido = pedidoService.buscarPorId(id);
        
        // Verificar se o pedido pertence ao usuário ou se é admin
        if (!pedido.getUsuario().getId().equals(usuario.getId()) && 
            !usuario.getRole().name().equals("FUNCIONARIO")) {
            throw new RuntimeException("Acesso negado");
        }

        model.addAttribute("pedido", pedido);
        return "cliente/detalhes-pedido";
    }
}