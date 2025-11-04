package com.senac.cafeteria.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private String descricao;
    private BigDecimal preco;

    @Lob
    private byte[] imagem;

    @Transient
    private String imagemBase64;

    /**
     * Relação N:N explícita com Pedido,
     * representada pela entidade intermediária ItemPedido.
     * 
     * Um Produto pode estar em vários Pedidos
     * e um Pedido pode conter vários Produtos.
     */
    @JsonIgnore
    @OneToMany(mappedBy = "produto", fetch = FetchType.LAZY)
    private List<ItemPedido> itensPedido = new ArrayList<>();
}
