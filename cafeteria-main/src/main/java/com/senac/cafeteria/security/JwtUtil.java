package com.senac.cafeteria.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

/*
 * Componente utilitário para geração, validação e extração de informações de JWT.
 * Centraliza a lógica relacionada aos tokens JWT usados pela aplicação.
 */
@Component
public class JwtUtil {

    // Chave HMAC usada para assinar/validar tokens
    private final Key key;
    // Tempo de expiração em milissegundos configurado via application.properties
    private final long expirationMillis;

    /*
     * Construtor injeta a secret e a expiração a partir das propriedades da aplicação.
     * A secret é convertida em Key compatível com a biblioteca jjwt.
     */
    public JwtUtil(@Value("${jwt.secret}") String secret,
                   @Value("${jwt.expiration}") long expirationMillis) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes()); // cria a Key a partir do secret
        this.expirationMillis = expirationMillis; // guarda o tempo de expiração
    }

    /*
     * Gera um token JWT com o username como subject, issuedAt e expiration.
     * O token é assinado com a Key e o algoritmo HS256.
     */
    public String generateToken(String username) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMillis);

        return Jwts.builder()
                .setSubject(username)         // identifica o usuário no subject
                .setIssuedAt(now)             // data de emissão
                .setExpiration(expiry)        // data de expiração
                .signWith(key, SignatureAlgorithm.HS256) // assinatura do token
                .compact();                   // retorna o token compactado
    }

    /*
     * Valida o token: tenta fazer o parse com a chave e captura exceções de validação.
     * Retorna true quando o token é válido e não expirou.
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            // Qualquer problema ao parsear/validar o token indica token inválido
            return false;
        }
    }

    /*
     * Extrai o username (subject) do token JWT já validado.
     */
    public String extractUsername(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody();
        return claims.getSubject();
    }
}
