package com.websementic.fmp.security;

import com.websementic.fmp.security.jwt.JwtService;
import com.websementic.fmp.security.service.CustomLogoutHandler;
import com.websementic.fmp.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;

@Configuration
@RequiredArgsConstructor
public class ApplicationSecurity {

    private final CustomAuthenticationRequestFilter jwtRequestFilter;
    private final UserDetailsService userDetailsService;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final CustomLogoutHandler customLogoutHandler;

    @Bean
    public SecurityFilterChain configure(final HttpSecurity http) throws Exception {
        return http.cors(httpSecurityCorsConfigurer -> httpSecurityCorsConfigurer.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests((authorize) -> authorize
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/users/me",
                                "/api/v1/books/**",
                                "/api/v1/borrowings/me")
                        .hasAnyRole(User.Role.USER.toString(),
                                User.Role.ADMIN.toString())
                        .requestMatchers(HttpMethod.POST, "/api/v1/borrowings/borrow",
                                "/api/v1/borrowings/*/return")
                        .hasAnyRole(User.Role.USER.toString(),
                                User.Role.ADMIN.toString())
                        .requestMatchers(HttpMethod.PUT, "/api/v1/users/*")
                        .hasAnyRole(User.Role.ADMIN.toString(),
                                User.Role.USER.toString())
                        .requestMatchers(HttpMethod.POST, "/login", "/logout").permitAll()
                        .requestMatchers(HttpMethod.POST, "/signup", "/api/v1/tokens").permitAll()
                        .requestMatchers(HttpMethod.GET, "/.well-known/jwks.json").permitAll()
                        .requestMatchers("/api/v1/api-docs/**",
                                "/swagger-ui.html",
                                "/swagger-ui/*").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        .anyRequest()
                        .hasRole(User.Role.ADMIN.toString()))

                .sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptionableConfigure ->
                        exceptionableConfigure.accessDeniedHandler(customAccessDeniedHandler)
                                .authenticationEntryPoint(customAuthenticationEntryPoint))
                .addFilterBefore(jwtRequestFilter, CustomAuthenticationFilter.class)
                .logout(httpSecurityLogoutConfigurer -> httpSecurityLogoutConfigurer
                        .logoutUrl("/logout")
                        .addLogoutHandler(customLogoutHandler)
                        .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler(HttpStatus.OK)).permitAll()
                        .deleteCookies(JwtService.ACCESS_TOKEN_COOKIE_NAME,
                                JwtService.REFRESH_TOKEN_COOKIE_NAME))
                .build();
    }

    private CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Collections.singletonList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD"));
        configuration.setAllowedHeaders(Collections.singletonList("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            final AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
