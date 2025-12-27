package com.grabit.config;

import com.grabit.handler.CustomAuthHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final String[] PERMITTED_PATHS=new String[]{

    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, CustomAuthHandler customAuthHandler, JWTFilter jwtFilter) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(request->
                        request
                                .requestMatchers(PERMITTED_PATHS).permitAll()
                                .anyRequest().authenticated()
                        )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(ex->ex
                        .authenticationEntryPoint(customAuthHandler)
                        .accessDeniedHandler(customAuthHandler)
                )
                ;
        return http.build();
    }
}
