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

    // Relação funcional (1:N) com ItemPedido
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemPedido> itens = new ArrayList<>();

    /**
     * Relação ManyToMany simbólica (para requisito acadêmico)
     * Não interfere na lógica funcional de ItemPedido.
     */
    @ManyToMany
    @JoinTable(
        name = "pedido_produto_relacionado",
        joinColumns = @JoinColumn(name = "pedido_id"),
        inverseJoinColumns = @JoinColumn(name = "produto_id")
    )
    private List<Produto> produtosRelacionados = new ArrayList<>();

    // Construtores
    public Pedido() {
        this.dataCriacao = LocalDateTime.now();
        this.total = BigDecimal.ZERO;
        this.status = StatusPedido.PENDENTE;
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

    public List<Produto> getProdutosRelacionados() { return produtosRelacionados; }
    public void setProdutosRelacionados(List<Produto> produtosRelacionados) { this.produtosRelacionados = produtosRelacionados; }

    // Métodos auxiliares reais
    public void adicionarItem(ItemPedido item) {
        item.setPedido(this);
        this.itens.add(item);
        calcularTotal();
    }

    public void removerItem(ItemPedido item) {
        this.itens.remove(item);
        calcularTotal();
    }

    public void calcularTotal() {
        this.total = itens.stream()
            .map(ItemPedido::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
