package com.github.ssalfelder.ocrformmate.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;

@Configuration
public class PasswordEncoderConfig {

    @Bean
    public Argon2PasswordEncoder passwordEncoder() {

        return new Argon2PasswordEncoder(16,
                                        32,
                                          2,
                                        65536,
                                           5);
    }
}
