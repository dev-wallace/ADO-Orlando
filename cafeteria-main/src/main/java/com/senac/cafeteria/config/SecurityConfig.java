package com.senac.cafeteria.config;

import com.senac.cafeteria.security.JwtAuthenticationFilter;
import com.senac.cafeteria.services.MyUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.authentication.AuthenticationProvider;

/*
 * Classe de configuração de segurança da aplicação.
 * Aqui são definidos os beans e as duas SecurityFilterChain (API e Web).
 */
@Configuration
public class SecurityConfig {

    // Filtro JWT injetado para verificar tokens nas requisições
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    // Serviço que carrega detalhes do usuário (implementação do UserDetailsService)
    private final MyUserDetailsService userDetailsService;

    // Construtor com injeção de dependências
    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, MyUserDetailsService userDetailsService) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter; // inicializa o filtro JWT
        this.userDetailsService = userDetailsService; // inicializa o serviço de usuários
    }

    /*
     * Bean que fornece um AuthenticationProvider baseado em DAO (usuário + senha).
     * O DaoAuthenticationProvider usa o MyUserDetailsService e um PasswordEncoder.
     */
    @Bean
    public AuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(); // provedor padrão que busca usuários via UserDetailsService
        provider.setUserDetailsService(userDetailsService); // configura o serviço de detalhes do usuário
        provider.setPasswordEncoder(passwordEncoder()); // configura o encoder de senhas (BCrypt)
        return provider; // retorna o provedor configurado como bean
    }

    // Rotas do Swagger/OpenAPI que devem permanecer públicas (acesso sem autenticação)
    private static final String[] SWAGGER_MATCHERS = new String[] {
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/swagger-ui/index.html",
            "/swagger-resources/**",
            "/webjars/**"
    };

    /*
     * SecurityFilterChain para a API que utiliza JWT — prioridade alta (Order 1).
     * Esta chain trata rotas que começam com /api/** e utiliza autenticação stateless.
     */
    @Bean
    @Order(1)
    public SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {
        http
                // Aplica esta chain apenas para caminhos que combinam com /api/**
                .securityMatcher("/api/**")
                // Desabilita CSRF para a API (quando se usa JWT geralmente se desabilita)
                .csrf().disable()
                // Autorizações específicas para endpoints da API
                .authorizeHttpRequests(auth -> auth
                        // Permite acesso livre a endpoints de auth (login/registro)
                        .requestMatchers("/api/auth/**").permitAll()
                        // Permite acesso livre aos endpoints do Swagger/OpenAPI
                        .requestMatchers(SWAGGER_MATCHERS).permitAll()
                        // Demais requisições da API exigem autenticação
                        .anyRequest().authenticated()
                )
                // Configura gerenciamento de sessão para stateless (sem sessão no servidor)
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Adiciona o AuthenticationProvider configurado anteriormente (DAO + encoder)
                .authenticationProvider(daoAuthenticationProvider())
                // Adiciona o filtro JWT antes do filtro padrão de autenticação por username/senha
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        // Constrói e retorna a SecurityFilterChain para a API
        return http.build();
    }

    /*
     * SecurityFilterChain para a parte web da aplicação (form login / Thymeleaf).
     * Menor prioridade (Order 2) — usada para rotas não começando com /api/.
     */
    @Bean
    @Order(2)
    public SecurityFilterChain webFilterChain(HttpSecurity http) throws Exception {
        http
                // Regras de autorização para recursos estáticos, páginas públicas e áreas restritas
                .authorizeHttpRequests(authz -> authz
                        // Permite recursos estáticos (css, js, imagens, webjars, favicon)
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/images_homapage/**", "/webjars/**", "/favicon.ico").permitAll()
                        // Permite acesso ao Swagger UI
                        .requestMatchers(SWAGGER_MATCHERS).permitAll()
                        // Páginas públicas (home, menu, cadastro, login, about)
                        .requestMatchers("/", "/menu", "/cadastro", "/login", "/about").permitAll()
                        // Rotas do carrinho e perfil restritas a clientes (ROLE_CLIENTE)
                        .requestMatchers("/carrinho/**", "/perfil").hasAuthority("ROLE_CLIENTE")
                        // Rotas administrativas restritas a funcionários (ROLE_FUNCIONARIO)
                        .requestMatchers("/admin/**").hasAuthority("ROLE_FUNCIONARIO")
                        // Demais requisições exigem autenticação
                        .anyRequest().authenticated()
                )
                // Configuração do formulário de login
                .formLogin(form -> form
                        .loginPage("/login") // página customizada de login
                        .loginProcessingUrl("/login") // endpoint que processa o POST do login
                        .defaultSuccessUrl("/redirecionarPorRole", true) // redireciona após sucesso baseado na role
                        .failureUrl("/login?error=true") // página em caso de falha no login
                        .permitAll() // permite acesso ao formulário de login sem autenticação
                )
                // Configuração do logout
                .logout(logout -> logout
                        .logoutUrl("/logout") // endpoint de logout
                        .logoutSuccessUrl("/?logout=true") // redireciona após logout
                        .invalidateHttpSession(true) // invalida a sessão HTTP
                        .deleteCookies("JSESSIONID") // remove cookie de sessão
                        .permitAll() // permite acesso ao logout sem autenticação
                )
                // Gerenciamento de sessão para a aplicação web (proteção contra fixation)
                .sessionManagement(session -> session
                        .sessionFixation().migrateSession() // migra sessão para evitar session fixation
                )
                // Usa o mesmo AuthenticationProvider configurado (DAO + encoder)
                .authenticationProvider(daoAuthenticationProvider())
                // Desabilita CSRF para simplificar (adequar conforme necessidade)
                .csrf(csrf -> csrf.disable());

        // Comentário: se desejar aceitar também Authorization: Bearer <token> nas requisições web,
        // pode-se adicionar o jwtAuthenticationFilter na cadeia com addFilterBefore.

        // Constrói e retorna a SecurityFilterChain para a web
        return http.build();
    }

    /*
     * Bean que fornece o PasswordEncoder usado para armazenar e verificar senhas.
     * BCrypt é seguro e recomendado por padrão.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // instancia o encoder BCrypt
    }

    /*
     * Bean que expõe o AuthenticationManager a partir da AuthenticationConfiguration.
     * Útil para realizar autenticação manual (por exemplo, ao gerar token no endpoint /api/auth).
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager(); // retorna o AuthenticationManager configurado pelo Spring
    }
}
