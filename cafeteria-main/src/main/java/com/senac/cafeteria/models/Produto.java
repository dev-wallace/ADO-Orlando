package com.senac.cafeteria.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "produto")
@Data
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private String descricao;
    private BigDecimal preco;

    /**
     * Armazenamento binário da imagem no banco.
     * Mantido para não quebrar o sistema de imagens.
     */
    @Lob
    private byte[] imagem;

    /**
     * Campo transitório usado para expor a imagem em Base64 na API.
     * Não é persistido no banco.
     */
    @Transient
    private String imagemBase64;

    /**
     * Relação funcional (1:N) com ItemPedido — mantém a lógica do pedido.
     */
    @JsonIgnore
    @OneToMany(mappedBy = "produto", fetch = FetchType.LAZY)
    private List<ItemPedido> itensPedido = new ArrayList<>();

    /**
     * Relação ManyToMany simbólica com Pedido (requisito acadêmico).
     * Mapeado por "produtosRelacionados" no lado Pedido.
     * Não utilizada na lógica de cálculo do pedido.
     */
    @JsonIgnore
    @ManyToMany(mappedBy = "produtosRelacionados", fetch = FetchType.LAZY)
    private List<Pedido> pedidosRelacionados = new ArrayList<>();
}
