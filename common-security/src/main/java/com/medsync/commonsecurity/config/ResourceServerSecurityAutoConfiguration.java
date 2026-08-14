package com.medsync.commonsecurity.config;

import com.medsync.commonsecurity.jwt.JwtValidator;
import com.medsync.commonsecurity.jwt.ResourceServerJwtFilter;
import com.medsync.commonsecurity.web.RestAuthenticationEntryPoint;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.stream.Stream;

/**
 * Drop-in security for every service that only NEEDS TO VALIDATE tokens
 * (i.e. every service except auth-service).
 * <p>
 * Just adding the {@code common-security} dependency + setting
 * {@code medsync.security.secret-key} is enough to get:
 * - stateless JWT filter (no DB lookups)
 * - {@code @PreAuthorize}/{@code @EnableMethodSecurity} support
 * - actuator/swagger permitted by default
 * - a consistent 401 handler
 * <p>
 * auth-service does NOT use this class — it keeps its own SecurityConfig
 * because it also issues tokens, hashes passwords and hits the users table.
 */
@AutoConfiguration
@EnableMethodSecurity
@ConditionalOnMissingBean(SecurityFilterChain.class)
@EnableConfigurationProperties(JwtValidationProperties.class)
public class ResourceServerSecurityAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public JwtValidator jwtValidator(JwtValidationProperties properties) {
        return new JwtValidator(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public ResourceServerJwtFilter resourceServerJwtFilter(JwtValidator jwtValidator) {
        return new ResourceServerJwtFilter(jwtValidator);
    }

    @Bean
    @ConditionalOnMissingBean
    public RestAuthenticationEntryPoint restAuthenticationEntryPoint() {
        return new RestAuthenticationEntryPoint();
    }

    @Bean
    @ConditionalOnMissingBean(SecurityFilterChain.class)
    public SecurityFilterChain resourceServerFilterChain(HttpSecurity http, ResourceServerJwtFilter jwtFilter, RestAuthenticationEntryPoint entryPoint, JwtValidationProperties properties) {

        String[] publicPaths = Stream.of(
                        Stream.of(DefaultPublicEndpoints.MONITORING),
                        Stream.of(DefaultPublicEndpoints.SWAGGER),
                        properties.getPermitAll().stream())
                .flatMap(s -> s)
                .toArray(String[]::new);

        http
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(exception -> exception.authenticationEntryPoint(entryPoint))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(publicPaths).permitAll()
                        .anyRequest().authenticated()
                )
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable);

        return http.build();
    }
}
