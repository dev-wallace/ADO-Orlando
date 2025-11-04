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

@Service
@RequiredArgsConstructor
@Transactional
public class CarrinhoService {

    private final PedidoRepository pedidoRepository;
    private final ProdutoService produtoService;

    // Carrinho em memória (simplificado - em produção use Redis ou sessão)
    private final Map<Long, Map<Long, Integer>> carrinhos = new HashMap<>();

    public void adicionarAoCarrinho(Long usuarioId, Long produtoId, Integer quantidade) {
        carrinhos.putIfAbsent(usuarioId, new HashMap<>());
        Map<Long, Integer> carrinhoUsuario = carrinhos.get(usuarioId);
        
        carrinhoUsuario.merge(produtoId, quantidade, Integer::sum);
    }

    public void removerDoCarrinho(Long usuarioId, Long produtoId) {
        if (carrinhos.containsKey(usuarioId)) {
            carrinhos.get(usuarioId).remove(produtoId);
        }
    }

    public void atualizarQuantidade(Long usuarioId, Long produtoId, Integer quantidade) {
        if (carrinhos.containsKey(usuarioId) && quantidade > 0) {
            carrinhos.get(usuarioId).put(produtoId, quantidade);
        }
    }

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

    public BigDecimal calcularTotal(Long usuarioId) {
        Map<Produto, Integer> carrinho = getCarrinho(usuarioId);
        return carrinho.entrySet().stream()
            .map(entry -> entry.getKey().getPreco().multiply(BigDecimal.valueOf(entry.getValue())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void limparCarrinho(Long usuarioId) {
        carrinhos.remove(usuarioId);
    }

    public Integer getQuantidadeItens(Long usuarioId) {
        if (carrinhos.containsKey(usuarioId)) {
            return carrinhos.get(usuarioId).values().stream()
                .mapToInt(Integer::intValue)
                .sum();
        }
        return 0;
    }

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