package com.senac.cafeteria.controller;

import com.senac.cafeteria.dtos.AuthRequest;
import com.senac.cafeteria.dtos.AuthResponse;
import com.senac.cafeteria.security.JwtUtil;
import com.senac.cafeteria.services.MyUserDetailsService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.http.ResponseEntity;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

import org.springframework.web.bind.annotation.*;

import java.util.Map;

/*
 * Controller responsável pelos endpoints de autenticação da API.
 * Expõe endpoints para obter token (login) e para criar sessão do Spring a partir de um token JWT.
 */
@RestController
@RequestMapping("/api/auth")
public class ApiAuthController {

    // Manager usado para autenticação tradicional (username/password)
    private final AuthenticationManager authenticationManager;
    // Utilitário JWT para gerar e validar tokens
    private final JwtUtil jwtUtil;
    // Serviço que carrega detalhes do usuário (implementa UserDetailsService)
    private final MyUserDetailsService userDetailsService;

    // Construtor com injeção de dependências
    public ApiAuthController(AuthenticationManager authenticationManager,
                             JwtUtil jwtUtil,
                             MyUserDetailsService userDetailsService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Endpoint de login:
     * - Recebe AuthRequest (username, password).
     * - Usa AuthenticationManager para autenticar as credenciais.
     * - Se autenticado, gera e retorna um JWT (AuthResponse).
     * - Em caso de credenciais inválidas retorna 401 com mensagem de erro.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest body) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(body.getUsername(), body.getPassword()));
            String token = jwtUtil.generateToken(body.getUsername());
            return ResponseEntity.ok(new AuthResponse(token));
        } catch (BadCredentialsException ex) {
            // Retorna 401 quando as credenciais não batem
            return ResponseEntity.status(401).body(Map.of("error", "Credenciais inválidas"));
        }
    }

    /**
     * Cria uma sessão HTTP do Spring a partir de um token JWT válido.
     * Útil quando uma interface cliente (ex: SPA) obtém um token e quer abrir uma sessão com JSESSIONID.
     *
     * Fluxo:
     * - Recebe JSON com { "token": "..." }.
     * - Valida o token com JwtUtil.
     * - Extrai username e carrega UserDetails.
     * - Cria uma Authentication e popula o SecurityContextHolder.
     * - Persiste o SecurityContext na sessão HTTP (gera cookie JSESSIONID).
     */
    @PostMapping("/session")
    public ResponseEntity<?> createSession(@RequestBody Map<String, String> body, HttpServletRequest request) {
        String token = body.get("token");
        if (token == null || !jwtUtil.validateToken(token)) {
            // Token ausente ou inválido -> 401
            return ResponseEntity.status(401).body(Map.of("error", "token inválido"));
        }

        String username = jwtUtil.extractUsername(token);
        UserDetails userDetails;
        try {
            // Carrega os detalhes do usuário (username -> UserDetails)
            userDetails = userDetailsService.loadUserByUsername(username);
        } catch (Exception e) {
            // Usuário não encontrado -> 401
            return ResponseEntity.status(401).body(Map.of("error", "usuário não encontrado"));
        }

        // Cria Authentication com as authorities do usuário e popula o SecurityContext
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        // Persiste o SecurityContext na sessão HTTP para que o Spring mantenha a autenticação
        HttpSession session = request.getSession(true); // cria sessão se não existir
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                SecurityContextHolder.getContext());

        // Retorna OK indicando que a sessão foi criada com sucesso
        return ResponseEntity.ok(Map.of("ok", true));
    }
}
