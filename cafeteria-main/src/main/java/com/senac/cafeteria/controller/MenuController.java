package com.senac.cafeteria.controller;

import com.senac.cafeteria.services.ProdutoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Base64;

@Controller
public class MenuController {

    @Autowired
    private ProdutoService produtoService;

    @GetMapping("/menu")
    public String menu(Model model) {
        var produtos = produtoService.listarTodos();
        
        // Converter imagens para Base64
        for (var produto : produtos) {
            if (produto.getImagem() != null) {
                String base64Image = Base64.getEncoder().encodeToString(produto.getImagem());
                produto.setImagemBase64(base64Image);
            }
        }
        
        model.addAttribute("produtos", produtos);
        return "public/menu";
    }
}