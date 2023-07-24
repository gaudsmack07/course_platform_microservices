package com.costacloud.courseservice;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    protected SecurityFilterChain filter(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests()
                .antMatchers("/actuator/**").permitAll()
                .anyRequest().authenticated()
                .and().oauth2ResourceServer().jwt();
        http.cors();
        return http.build();
    }
}

