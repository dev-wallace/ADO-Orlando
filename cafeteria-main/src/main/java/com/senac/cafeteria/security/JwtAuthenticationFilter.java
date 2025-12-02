package com.senac.cafeteria.security;

import com.senac.cafeteria.services.MyUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// Componente Spring que representa um filtro aplicado a cada requisição (uma vez por requisição)
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    // Utilitário para operações com JWT (validação, extração de username, etc.)
    private final JwtUtil jwtUtil;
    // Serviço que carrega os detalhes do usuário (implementação de UserDetailsService)
    private final MyUserDetailsService userDetailsService;

    // Construtor com injeção de dependências do utilitário JWT e do serviço de usuários
    public JwtAuthenticationFilter(JwtUtil jwtUtil, MyUserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    /*
     * Método principal do filtro que é executado para cada requisição HTTP.
     * - Extrai o header Authorization
     * - Se houver um token Bearer válido, valida e obtém o username do token
     * - Carrega UserDetails e cria uma Authentication para popular o SecurityContext
     * - Continua a cadeia de filtros chamando filterChain.doFilter(...)
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Lê o header Authorization da requisição
        String authHeader = request.getHeader("Authorization");
        String token = null;
        String username = null;

        // Verifica se o header começa com "Bearer " e extrai o token
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7); // remove "Bearer " do começo
            // Valida o token e extrai o username se válido
            if (jwtUtil.validateToken(token)) {
                username = jwtUtil.extractUsername(token);
            }
        }

        // Se obtivemos um username e ainda não há autenticação no contexto de segurança
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // Carrega os detalhes do usuário a partir do serviço
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            // Cria um token de autenticação com as authorities do usuário
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            // Define a autenticação no contexto de segurança do Spring
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

        // Continua a cadeia de filtros (essencial para que a requisição prossiga)
        filterChain.doFilter(request, response);
    }

    /*
     * Método que permite pular o filtro para caminhos específicos.
     * Aqui está configurado para não filtrar endpoints de autenticação da API.
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Obtém o caminho da requisição (ex: /api/auth/login)
        String path = request.getServletPath();
        // Retorna true para não aplicar o filtro quando o caminho começar com /api/auth
        return path.startsWith("/api/auth");
    }
}
