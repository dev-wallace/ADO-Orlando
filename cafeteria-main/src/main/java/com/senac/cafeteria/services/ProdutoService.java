package com.senac.cafeteria.services;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.senac.cafeteria.models.Produto;
import com.senac.cafeteria.repositories.ProdutoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProdutoService {
    private final ProdutoRepository produtoRepository;

    public Produto salvarProduto(Produto produto, MultipartFile imagem) throws IOException {
    if(imagem != null && !imagem.isEmpty()) {
        produto.setImagem(imagem.getBytes());
    }
    return produtoRepository.save(produto);
}
    
    public void excluirProduto(Long id) {
        produtoRepository.deleteById(id);
    }

    // Método para listar todos os produtos
    public List<Produto> listarTodos() {
        return produtoRepository.findAll();
    }

    // Método para buscar produto por ID
    public Produto buscarPorId(Long id) {
        Optional<Produto> produto = produtoRepository.findById(id);
        return produto.orElseThrow(() -> new RuntimeException("Produto não encontrado com ID: " + id));
    }

    // Método para atualizar produto
    public Produto atualizarProduto(Long id, Produto produtoAtualizado, MultipartFile imagem) throws IOException {
        Produto produtoExistente = buscarPorId(id);
        
        produtoExistente.setNome(produtoAtualizado.getNome());
        produtoExistente.setDescricao(produtoAtualizado.getDescricao());
        produtoExistente.setPreco(produtoAtualizado.getPreco());
        
        if (imagem != null && !imagem.isEmpty()) {
            produtoExistente.setImagem(imagem.getBytes());
        }
        
        return produtoRepository.save(produtoExistente);
    }

    // Método para buscar produtos por nome (opcional)
    public List<Produto> buscarPorNome(String nome) {
        return produtoRepository.findByNomeContainingIgnoreCase(nome);
    }

    // Método para verificar se produto existe
    public boolean existeProduto(Long id) {
        return produtoRepository.existsById(id);
    }

    // Método para contar total de produtos
    public long contarProdutos() {
        return produtoRepository.count();
    }
}