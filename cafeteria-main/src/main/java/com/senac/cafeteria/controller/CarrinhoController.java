package com.senac.cafeteria.controller;

import com.senac.cafeteria.models.Usuario;
import com.senac.cafeteria.services.CarrinhoService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Base64;

@Controller
@RequestMapping("/carrinho")
@RequiredArgsConstructor
public class CarrinhoController {

    private final CarrinhoService carrinhoService;

    @GetMapping
    public String verCarrinho(@AuthenticationPrincipal Usuario usuario, Model model) {
        var itensCarrinho = carrinhoService.getCarrinho(usuario.getId());
        
        // Converter imagens para Base64
        itensCarrinho.keySet().forEach(produto -> {
            if (produto.getImagem() != null) {
                String base64Image = Base64.getEncoder().encodeToString(produto.getImagem());
                produto.setImagemBase64(base64Image);
            }
        });

        model.addAttribute("itensCarrinho", itensCarrinho);
        model.addAttribute("total", carrinhoService.calcularTotal(usuario.getId()));
        model.addAttribute("quantidadeItens", carrinhoService.getQuantidadeItens(usuario.getId()));
        
        return "carrinho/carrinho";
    }

    @PostMapping("/adicionar/{produtoId}")
    public String adicionarAoCarrinho(@AuthenticationPrincipal Usuario usuario,
                                     @PathVariable Long produtoId,
                                     @RequestParam(defaultValue = "1") Integer quantidade,
                                     RedirectAttributes redirectAttributes) {
        try {
            carrinhoService.adicionarAoCarrinho(usuario.getId(), produtoId, quantidade);
            redirectAttributes.addFlashAttribute("sucesso", "Produto adicionado ao carrinho!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao adicionar produto: " + e.getMessage());
        }
        return "redirect:/menu";
    }

    @PostMapping("/remover/{produtoId}")
    public String removerDoCarrinho(@AuthenticationPrincipal Usuario usuario,
                                   @PathVariable Long produtoId,
                                   RedirectAttributes redirectAttributes) {
        try {
            carrinhoService.removerDoCarrinho(usuario.getId(), produtoId);
            redirectAttributes.addFlashAttribute("sucesso", "Produto removido do carrinho!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao remover produto: " + e.getMessage());
        }
        return "redirect:/carrinho";
    }

    @PostMapping("/atualizar/{produtoId}")
    public String atualizarQuantidade(@AuthenticationPrincipal Usuario usuario,
                                     @PathVariable Long produtoId,
                                     @RequestParam Integer quantidade,
                                     RedirectAttributes redirectAttributes) {
        try {
            carrinhoService.atualizarQuantidade(usuario.getId(), produtoId, quantidade);
            redirectAttributes.addFlashAttribute("sucesso", "Quantidade atualizada!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao atualizar quantidade: " + e.getMessage());
        }
        return "redirect:/carrinho";
    }

    @PostMapping("/finalizar")
    public String finalizarPedido(@AuthenticationPrincipal Usuario usuario,
                                 RedirectAttributes redirectAttributes) {
        try {
            var pedido = carrinhoService.finalizarPedido(usuario);
            redirectAttributes.addFlashAttribute("sucesso", 
                "Pedido #" + pedido.getId() + " realizado com sucesso!");
            return "redirect:/pedidos";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao finalizar pedido: " + e.getMessage());
            return "redirect:/carrinho";
        }
    }

    @PostMapping("/limpar")
    public String limparCarrinho(@AuthenticationPrincipal Usuario usuario,
                                RedirectAttributes redirectAttributes) {
        try {
            carrinhoService.limparCarrinho(usuario.getId());
            redirectAttributes.addFlashAttribute("sucesso", "Carrinho limpo!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao limpar carrinho: " + e.getMessage());
        }
        return "redirect:/carrinho";
    }
}