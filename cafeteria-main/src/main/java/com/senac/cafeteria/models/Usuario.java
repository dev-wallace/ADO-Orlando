package com.senac.cafeteria.models;

import jakarta.persistence.*;
import java.util.Collection;
import java.util.List;
import lombok.Data;
import com.senac.cafeteria.models.enums.Role;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Entity
@Data
public class Usuario implements UserDetails { // ← IMPLEMENTAR UserDetails
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String nome;
    private String email;
    private String senha;
    private String endereco;
    
    @Enumerated(EnumType.STRING)
    private Role role;
    
    @OneToMany(mappedBy = "usuario")
    private List<Pedido> pedidos;

    // ← MÉTODOS OBRIGATÓRIOS DO UserDetails

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.getAuthority()));
    }

    @Override
    public String getPassword() {
        return senha; // ← Retorna a senha do usuário
    }

    @Override
    public String getUsername() {
        return email; // ← Retorna o email como username
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // ← Conta nunca expira
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // ← Conta nunca é bloqueada
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // ← Credenciais nunca expiram
    }

    @Override
    public boolean isEnabled() {
        return true; // ← Usuário sempre está ativo
    }
}