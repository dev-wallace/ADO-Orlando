package com.senac.cafeteria.models;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.senac.cafeteria.models.enums.StatusPedido;

@Entity
@Table(name = "pedido")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    private LocalDateTime dataCriacao;
    private BigDecimal total;

    @Enumerated(EnumType.STRING)
    private StatusPedido status;

    /**
     * Relação N:N explícita com Produto,
     * feita através da tabela intermediária ItemPedido.
     * 
     * Um Pedido pode conter vários Produtos (via ItemPedido)
     * e cada Produto pode estar em vários Pedidos.
     */
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemPedido> itens = new ArrayList<>();

    public Pedido() {
        this.dataCriacao = LocalDateTime.now();
        this.total = BigDecimal.ZERO;
        this.status = StatusPedido.PENDENTE;
    }

    // Métodos auxiliares para manipular os itens do pedido
    public void adicionarItem(ItemPedido item) {
        item.setPedido(this);
        this.itens.add(item);
        if (item.getProduto() != null) {
            item.getProduto().getItensPedido().add(item);
        }
        calcularTotal();
    }

    public void removerItem(ItemPedido item) {
        this.itens.remove(item);
        if (item.getProduto() != null) {
            item.getProduto().getItensPedido().remove(item);
        }
        calcularTotal();
    }

    public void calcularTotal() {
        this.total = itens.stream()
            .map(ItemPedido::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public StatusPedido getStatus() { return status; }
    public void setStatus(StatusPedido status) { this.status = status; }
    public List<ItemPedido> getItens() { return itens; }
    public void setItens(List<ItemPedido> itens) { this.itens = itens; }
}
