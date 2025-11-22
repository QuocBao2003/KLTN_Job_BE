package com.example.demo.config;

import com.example.demo.util.SecurityUtil;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;


import org.springframework.context.annotation.Configuration;

import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Arrays;

@Configuration
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfiguration {
    @Value("${demo.jwt.base64-secret}")
    private String jwtKey;
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


        @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, CustomAuthenticationEntryPoint customAuthenticationEntryPoint) throws Exception {
        String[] whileList = {"/","/api/v1/auth/login","/api/v1/auth/refresh","/storage/**","/api/v1/auth/register","/v3/api-docs/**","/swagger-ui/**",
                "/api/v1/auth/google", "/api/v1/auth/oauth2/callback/**", "/api/v1/auth/google/token",
                "/api/v1/email/**","/api/v1/files","/api/v1/ask","/api/v1/auth/registerHR","/api/v1/save-jobs/{userId}","/api/v1/save-jobs/{jobId}"};
        http
                .csrf(c->c.disable())
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(
                authz -> authz
                        .requestMatchers(whileList).permitAll()
                        .requestMatchers(HttpMethod.GET,"/api/v1/companies/**").permitAll()
                        .requestMatchers(HttpMethod.GET,"/api/v1/jobs/**").permitAll()
                        .requestMatchers(HttpMethod.GET,"/api/v1/jobs_professions/**").permitAll()

                        .requestMatchers(HttpMethod.GET,"/api/v1/skills/**").permitAll()
                        .requestMatchers(HttpMethod.GET,"/api/v1/submit-cv").authenticated()
                        .requestMatchers(HttpMethod.GET,"/api/v1/cvs/**").authenticated()
                        .requestMatchers("/api/v1/chat/**").authenticated()
                        .anyRequest().authenticated()
                )

                .oauth2ResourceServer((oauth2)->oauth2.jwt(Customizer.withDefaults())
                        .authenticationEntryPoint(customAuthenticationEntryPoint))
                .formLogin(f ->f.disable())

//                .exceptionHandling(
//                        exception ->exception
//                                .authenticationEntryPoint(new BearerTokenAuthenticationEntryPoint())//401
//                                .accessDeniedHandler(new BearerTokenAccessDeniedHandler())) //403
                .sessionManagement(session ->session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        return http.build();
    }
    private SecretKey getSecrectKey() {
        byte[] keyBytes = java.util.Base64.getDecoder().decode(jwtKey);
        // HS512 tương ứng với HmacSHA512 trong JCA

        return new SecretKeySpec(keyBytes, "HmacSHA512");
    }
//    mã hóa key
    @Bean
    public JwtEncoder  jwtEncoder() {
        return new NimbusJwtEncoder(new ImmutableSecret<>(getSecrectKey()));
    }
    @Bean
    public JwtDecoder jwtDecoder() {
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withSecretKey(
                getSecrectKey()).macAlgorithm(SecurityUtil.JWT_ALGORITHM).build();
        return token -> {
            try {
                var decoded = jwtDecoder.decode(token);

                return decoded;
            } catch (Exception e) {
                System.out.println("JWT error" + e.getMessage());
                throw e;
            }
         };
        }

//nạp data vào JWT
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter(){
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter= new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthorityPrefix("");
        grantedAuthoritiesConverter.setAuthoritiesClaimName("permission");

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }
    }
