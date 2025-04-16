package com.github.ssalfelder.ocrformmate.security;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @PostConstruct
    public void init() {
        System.out.println(">>> SecurityConfig wurde geladen!");
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http

                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/user/register",
                                "/user/validate",
                                "/clerk/register",
                                "/clerk/validate",
                                "/ping"
                        ).permitAll()
                        .anyRequest().authenticated()
                )

                // 3. Standard-Login-Oberfläche deaktivieren
                .formLogin(form -> form.disable())

                // 4. HTTP Basic Auth ebenfalls deaktivieren
                .httpBasic(httpBasic -> httpBasic.disable());

        return http.build();
    }


    @Bean
    public UserDetailsService noopUserDetailsService() {
        return username -> null; // Kein Benutzer vorhanden = keine Authentifizierung nötig
    }
}
