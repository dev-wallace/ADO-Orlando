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

@RestController
@RequestMapping("/api/auth")
public class ApiAuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final MyUserDetailsService userDetailsService;

    public ApiAuthController(AuthenticationManager authenticationManager,
                             JwtUtil jwtUtil,
                             MyUserDetailsService userDetailsService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Mantive seu endpoint de login — devolve o token JWT.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest body) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(body.getUsername(), body.getPassword()));
            String token = jwtUtil.generateToken(body.getUsername());
            return ResponseEntity.ok(new AuthResponse(token));
        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(401).body(Map.of("error", "Credenciais inválidas"));
        }
    }

    /**
     * Cria a sessão do Spring a partir de um token JWT válido.
     * Espera JSON: { "token": "..." }
     *
     * - Valida o token.
     * - Extrai username, carrega UserDetails e popula SecurityContext.
     * - Grava SecurityContext na sessão HTTP (gera Set-Cookie: JSESSIONID).
     */
    @PostMapping("/session")
    public ResponseEntity<?> createSession(@RequestBody Map<String, String> body, HttpServletRequest request) {
        String token = body.get("token");
        if (token == null || !jwtUtil.validateToken(token)) {
            return ResponseEntity.status(401).body(Map.of("error", "token inválido"));
        }

        String username = jwtUtil.extractUsername(token);
        UserDetails userDetails;
        try {
            userDetails = userDetailsService.loadUserByUsername(username);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "usuário não encontrado"));
        }

        // Cria Authentication e popula SecurityContext
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        // Persiste SecurityContext na sessão HTTP para criação de JSESSIONID
        HttpSession session = request.getSession(true);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                SecurityContextHolder.getContext());

        return ResponseEntity.ok(Map.of("ok", true));
    }
}
