package com.senac.cafeteria.config;

import jakarta.annotation.PostConstruct;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.senac.cafeteria.models.Produto;
import com.senac.cafeteria.models.Usuario;
import com.senac.cafeteria.models.enums.Role;
import com.senac.cafeteria.repositories.ProdutoRepository;
import com.senac.cafeteria.repositories.UsuarioRepository;

import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {
    
    private final UsuarioRepository usuarioRepository;
    private final ProdutoRepository produtoRepository;
    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    public void init() {
        criarUsuarios();
        criarProdutosIniciais();
    }

    private void criarUsuarios() {
        // Criar usuário funcionário se não existir
        if (usuarioRepository.findByEmail("funcionario@cafe.com").isEmpty()) {
            Usuario funcionario = new Usuario();
            funcionario.setNome("Funcionário Admin");
            funcionario.setEmail("funcionario@cafe.com");
            funcionario.setSenha(passwordEncoder.encode("123456"));
            funcionario.setRole(Role.FUNCIONARIO);
            usuarioRepository.save(funcionario);
            System.out.println("Usuário funcionário criado: funcionario@cafe.com / 123456");
        }

        // Criar usuário cliente de teste
        if (usuarioRepository.findByEmail("cliente@teste.com").isEmpty()) {
            Usuario cliente = new Usuario();
            cliente.setNome("Cliente Teste");
            cliente.setEmail("cliente@teste.com");
            cliente.setSenha(passwordEncoder.encode("123456"));
            cliente.setRole(Role.CLIENTE);
            usuarioRepository.save(cliente);
            System.out.println("Usuário cliente criado: cliente@teste.com / 123456");
        }
    }

    private void criarProdutosIniciais() {
        if (produtoRepository.count() == 0) {
            List<Produto> produtos = Arrays.asList(
                criarProduto("Café Expresso", "Café forte e aromático, preparado na hora", new BigDecimal("5.90")),
                criarProduto("Cappuccino", "Café com leite vaporizado e espuma cremosa", new BigDecimal("8.50")),
                criarProduto("Latte", "Café com leite vaporizado e uma suave camada de espuma", new BigDecimal("9.00")),
                criarProduto("Mocha", "Café com chocolate, leite vaporizado e chantilly", new BigDecimal("12.00")),
            
                criarProduto("Suco de Laranja Natural", "Suco fresco de laranja, feito na hora", new BigDecimal("8.00")),
                criarProduto("Sanduíche Natural", "Pão integral com peito de peru e queijo branco", new BigDecimal("15.00")),
                criarProduto("Bolo de Chocolate", "Fatia de bolo de chocolate com cobertura", new BigDecimal("9.90")),
                criarProduto("Croissant", "Croissant folhado e crocante, perfeito para acompanhar", new BigDecimal("6.00"))
            );

            produtoRepository.saveAll(produtos);
            System.out.println("10 produtos iniciais criados com sucesso!");
        } else {
            System.out.println("Produtos já existem no banco. Nenhum produto inicial criado.");
        }
    }

    private Produto criarProduto(String nome, String descricao, BigDecimal preco) {
        Produto produto = new Produto();
        produto.setNome(nome);
        produto.setDescricao(descricao);
        produto.setPreco(preco);
        // As imagens ficarão nulas inicialmente - você pode adicionar depois pelo admin
        return produto;
    }
}