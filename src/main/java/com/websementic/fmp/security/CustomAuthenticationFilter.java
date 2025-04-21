package com.websementic.fmp.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.websementic.fmp.exeption.BadArgumentException;
import com.websementic.fmp.security.jwt.JwtService;
import com.websementic.fmp.security.service.SessionService;
import com.websementic.fmp.user.model.Session;
import com.websementic.fmp.user.model.User;
import com.websementic.fmp.user.repository.SessionRepository;
import com.websementic.fmp.user.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Component
public class CustomAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final SessionRepository sessionRepository;
    private final UserService userService;
    private final SessionService sessionService;

    public CustomAuthenticationFilter(AuthenticationManager authenticationManager, JwtService jwtService, SessionRepository sessionRepository, UserService userService, SessionService sessionService) {
        super(authenticationManager);
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.sessionRepository = sessionRepository;
        this.sessionService = sessionService;
        this.userService = userService;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.isEmpty(authHeader) || !authHeader.startsWith("Basic ")) {
            throw new AuthenticationCredentialsNotFoundException("Couldn't extract credentials From the request");
        }

        final String token = authHeader.substring(6);
        String[] emailPassword = new String(Base64.getDecoder().decode(token)).split(":", 2);

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(emailPassword[0], emailPassword[1]);
        return authenticationManager.authenticate(authenticationToken);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) throws IOException {

        User user = (User) authentication.getPrincipal();

        Session session = sessionService.create(user, request.getHeader(HttpHeaders.USER_AGENT));

        String accessToken;
        String refreshToken;

        try {
            accessToken = jwtService.generateAccessToken(user, session.getId());
            refreshToken = jwtService.generateRefreshToken(user, session.getId());
        } catch (BadArgumentException e) {
            throw new AuthenticationServiceException(e.getMessage());
        }

        Map<String, String> tokens = new HashMap<>();

        tokens.put("access_token", accessToken);
        response.setContentType(APPLICATION_JSON_VALUE);

        response.addCookie(jwtService.createAccessTokenCookie(accessToken, request.isSecure()));
        response.addCookie(jwtService.createRefreshTokenCookie(refreshToken, request.isSecure()));

        new ObjectMapper().writeValue(response.getOutputStream(), tokens);
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        //TODO: handle unsuccessful Authentication attempts
        throw failed;
    }


}
