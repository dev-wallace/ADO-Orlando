package com.senac.cafeteria.controller;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.senac.cafeteria.models.Usuario;
import com.senac.cafeteria.models.enums.Role;
import com.senac.cafeteria.repositories.UsuarioRepository;

@Controller
public class CadastroController {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public CadastroController(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/cadastro")
    public String processCadastro(@RequestParam String nome,
                                 @RequestParam String email,
                                 @RequestParam String senha,
                                 @RequestParam String endereco,
                                 RedirectAttributes redirectAttributes) {
        
        // Verificar se o email já existe
        if (usuarioRepository.findByEmail(email).isPresent()) {
            redirectAttributes.addFlashAttribute("erroCadastro", "Este email já está cadastrado");
            return "redirect:/";
        }
        
        // Criar novo usuário
        Usuario usuario = new Usuario();
        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.setSenha(passwordEncoder.encode(senha));
        usuario.setEndereco(endereco);
        usuario.setRole(Role.CLIENTE);
        
        usuarioRepository.save(usuario);
        
        redirectAttributes.addFlashAttribute("sucessoCadastro", "Cadastro realizado com sucesso! Faça login para continuar.");
        return "redirect:/";
    }
}                               