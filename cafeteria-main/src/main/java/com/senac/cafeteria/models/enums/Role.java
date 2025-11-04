package com.senac.cafeteria.models.enums;

public enum Role {
    CLIENTE("ROLE_CLIENTE"),
    FUNCIONARIO("ROLE_FUNCIONARIO");

    private final String authority;

    Role(String authority) {
        this.authority = authority;
    }

    public String getAuthority() {
        return authority; // ← Este método é ESSENCIAL
    }
}