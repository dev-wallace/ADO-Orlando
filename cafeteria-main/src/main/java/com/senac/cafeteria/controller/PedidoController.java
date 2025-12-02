package com.senac.cafeteria.controller;

import com.senac.cafeteria.models.Usuario;
import com.senac.cafeteria.services.PedidoService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/*
 * Controller para endpoints relacionados aos pedidos do cliente.
 * Exibe a lista de pedidos do usuário autenticado e detalhes de um pedido.
 */
@Controller
@RequestMapping("/pedidos")
@RequiredArgsConstructor
public class PedidoController {

    // Serviço que fornece operações sobre pedidos (listar, buscar por id)
    private final PedidoService pedidoService;

    // Lista os pedidos do usuário autenticado e adiciona ao model para a view
    @GetMapping
    public String meusPedidos(@AuthenticationPrincipal Usuario usuario, Model model) {
        var pedidos = pedidoService.listarPedidosPorUsuario(usuario);
        model.addAttribute("pedidos", pedidos);
        return "cliente/pedidos";
    }

    // Exibe detalhes de um pedido verificando autorização (dono do pedido ou funcionário)
    @GetMapping("/{id}")
    public String detalhesPedido(@AuthenticationPrincipal Usuario usuario,
                                 @PathVariable Long id,
                                 Model model) {
        var pedido = pedidoService.buscarPorId(id);

        // Verificar se o pedido pertence ao usuário ou se o usuário é funcionário
        if (!pedido.getUsuario().getId().equals(usuario.getId()) &&
                !usuario.getRole().name().equals("FUNCIONARIO")) {
            throw new RuntimeException("Acesso negado");
        }

        model.addAttribute("pedido", pedido);
        return "cliente/detalhes-pedido";
    }
}
