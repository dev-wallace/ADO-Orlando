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

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final MyUserDetailsService userDetailsService;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, MyUserDetailsService userDetailsService) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public AuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    // Rotas do swagger / openapi que precisam ficar públicas
    private static final String[] SWAGGER_MATCHERS = new String[] {
        "/v3/api-docs/**",
        "/swagger-ui/**",
        "/swagger-ui.html",
        "/swagger-ui/index.html",
        "/swagger-resources/**",
        "/webjars/**"
    };

    // Chain para API (JWT) — alta prioridade
    @Bean
    @Order(1)
    public SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/api/**")
            .csrf().disable()
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers(SWAGGER_MATCHERS).permitAll()     // libera swagger também para chamadas ao /v3/api-docs
                .anyRequest().authenticated()
            )
            .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authenticationProvider(daoAuthenticationProvider())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // Chain para web (form login / Thymeleaf)
    @Bean
    @Order(2)
    public SecurityFilterChain webFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authz -> authz
<<<<<<< HEAD
                .requestMatchers("/css/**", "/js/**", "/images/**", "/images_homapage/**", "/webjars/**", "/favicon.ico").permitAll()
=======
                .requestMatchers("/css/*", "/js/", "/images/", "/images_homapage/", "/webjars/*", "/favicon.ico").permitAll()
>>>>>>> 3c1f5f0f962c0975e55a991c26f36f654955c71a
                .requestMatchers(SWAGGER_MATCHERS).permitAll()     // libera swagger UI
                .requestMatchers("/", "/menu", "/cadastro", "/login", "/about").permitAll()
                .requestMatchers("/carrinho/**", "/perfil").hasAuthority("ROLE_CLIENTE")
                .requestMatchers("/admin/**").hasAuthority("ROLE_FUNCIONARIO")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/redirecionarPorRole", true)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .sessionManagement(session -> session
                .sessionFixation().migrateSession()
            )
            .authenticationProvider(daoAuthenticationProvider())
            .csrf(csrf -> csrf.disable());

        // Se quiser que a aplicação web também aceite Authorization: Bearer <token> em requisições normais,
        // descomente a linha abaixo:
        // http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
