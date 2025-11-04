package com.senac.cafeteria.models.enums;

public enum TipoEntrega {
    RETIRADA_LOJA("Retirada na loja"),
    ENTREGA("Entrega em domicílio");

    private final String descricao;

    TipoEntrega(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}