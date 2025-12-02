package com.senac.cafeteria.services;

import com.senac.cafeteria.models.*;
import com.senac.cafeteria.models.enums.StatusPedido;
import com.senac.cafeteria.repositories.PedidoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/*
 * Serviço que representa um carrinho simples em memória e provê operações:
 * adicionar, remover, atualizar quantidades, calcular total e finalizar pedido.
 * Em produção recomenda-se armazenar carrinho em Redis ou na sessão do usuário.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class CarrinhoService {

    // Repositório para persistir o pedido ao finalizar
    private final PedidoRepository pedidoRepository;
    // Serviço para carregar informações dos produtos (preço, imagem, etc.)
    private final ProdutoService produtoService;

    // Estrutura em memória: mapa usuárioId -> (produtoId -> quantidade)
    private final Map<Long, Map<Long, Integer>> carrinhos = new HashMap<>();

    // Adiciona um produto ao carrinho do usuário (soma quantidade se já existir)
    public void adicionarAoCarrinho(Long usuarioId, Long produtoId, Integer quantidade) {
        carrinhos.putIfAbsent(usuarioId, new HashMap<>());
        Map<Long, Integer> carrinhoUsuario = carrinhos.get(usuarioId);

        carrinhoUsuario.merge(produtoId, quantidade, Integer::sum);
    }

    // Remove um produto específico do carrinho do usuário
    public void removerDoCarrinho(Long usuarioId, Long produtoId) {
        if (carrinhos.containsKey(usuarioId)) {
            carrinhos.get(usuarioId).remove(produtoId);
        }
    }

    // Atualiza a quantidade de um item no carrinho (somente se quantidade > 0)
    public void atualizarQuantidade(Long usuarioId, Long produtoId, Integer quantidade) {
        if (carrinhos.containsKey(usuarioId) && quantidade > 0) {
            carrinhos.get(usuarioId).put(produtoId, quantidade);
        }
    }

    // Retorna o carrinho do usuário mapeado para objetos Produto -> quantidade
    public Map<Produto, Integer> getCarrinho(Long usuarioId) {
        Map<Produto, Integer> carrinhoComProdutos = new HashMap<>();

        if (carrinhos.containsKey(usuarioId)) {
            Map<Long, Integer> carrinhoUsuario = carrinhos.get(usuarioId);

            for (Map.Entry<Long, Integer> entry : carrinhoUsuario.entrySet()) {
                Produto produto = produtoService.buscarPorId(entry.getKey());
                carrinhoComProdutos.put(produto, entry.getValue());
            }
        }

        return carrinhoComProdutos;
    }

    // Calcula o total do carrinho multiplicando preço pela quantidade
    public BigDecimal calcularTotal(Long usuarioId) {
        Map<Produto, Integer> carrinho = getCarrinho(usuarioId);
        return carrinho.entrySet().stream()
                .map(entry -> entry.getKey().getPreco().multiply(BigDecimal.valueOf(entry.getValue())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Limpa o carrinho do usuário
    public void limparCarrinho(Long usuarioId) {
        carrinhos.remove(usuarioId);
    }

    // Retorna a quantidade total de itens no carrinho (soma das quantidades)
    public Integer getQuantidadeItens(Long usuarioId) {
        if (carrinhos.containsKey(usuarioId)) {
            return carrinhos.get(usuarioId).values().stream()
                    .mapToInt(Integer::intValue)
                    .sum();
        }
        return 0;
    }

    /*
     * Finaliza o pedido: converte itens do carrinho em Pedido e ItemPedido, salva
     * no repositório e limpa o carrinho em memória.
     */
    @Transactional
    public Pedido finalizarPedido(Usuario usuario) {
        Map<Produto, Integer> itensCarrinho = getCarrinho(usuario.getId());

        if (itensCarrinho.isEmpty()) {
            throw new RuntimeException("Carrinho vazio");
        }

        Pedido pedido = new Pedido();
        pedido.setUsuario(usuario);
        pedido.setStatus(StatusPedido.PENDENTE);

        for (Map.Entry<Produto, Integer> entry : itensCarrinho.entrySet()) {
            ItemPedido item = new ItemPedido(entry.getKey(), entry.getValue());
            pedido.adicionarItem(item);
        }

        pedido.calcularTotal();
        Pedido pedidoSalvo = pedidoRepository.save(pedido);
        limparCarrinho(usuario.getId());

        return pedidoSalvo;
    }
}
