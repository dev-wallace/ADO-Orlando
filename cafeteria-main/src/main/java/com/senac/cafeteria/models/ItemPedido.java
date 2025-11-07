package com.senac.cafeteria.models;

import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * Classe que representa a RELAÇÃO N:N entre Pedido e Produto.
 *
 *  Um Pedido pode conter vários Produtos.
 *  Um Produto pode estar em vários Pedidos.
 *
 * Essa relação N:N é implementada de forma explícita por meio
 * desta entidade intermediária: ITEM_PEDIDO.
 *
 * Cada ItemPedido liga um Produto a um Pedido e armazena
 * informações adicionais, como quantidade e preço unitário.
 */
@Entity
@Table(name = "item_pedido")
public class ItemPedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Muitos itens pertencem a um mesmo Pedido.
     * (Lado N da relação com Pedido)
     */
    @ManyToOne
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    /**
     * Muitos itens podem referenciar o mesmo Produto.
     * (Lado N da relação com Produto)
     */
    @ManyToOne
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    private Integer quantidade;
    private BigDecimal precoUnitario;

    // Construtores
    public ItemPedido() {}

    public ItemPedido(Produto produto, Integer quantidade) {
        this.produto = produto;
        this.quantidade = quantidade;
        this.precoUnitario = produto.getPreco();
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Pedido getPedido() { return pedido; }
    public void setPedido(Pedido pedido) { this.pedido = pedido; }

    public Produto getProduto() { return produto; }
    public void setProduto(Produto produto) { this.produto = produto; }

    public Integer getQuantidade() { return quantidade; }
    public void setQuantidade(Integer quantidade) { this.quantidade = quantidade; }

    public BigDecimal getPrecoUnitario() { return precoUnitario; }
    public void setPrecoUnitario(BigDecimal precoUnitario) { this.precoUnitario = precoUnitario; }

    public BigDecimal getSubtotal() {
        return precoUnitario.multiply(BigDecimal.valueOf(quantidade));
    }
}
