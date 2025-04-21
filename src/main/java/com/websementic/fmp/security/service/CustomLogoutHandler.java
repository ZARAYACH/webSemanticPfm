package com.websementic.fmp.security.service;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.websementic.fmp.security.jwt.JwtService;
import com.websementic.fmp.user.model.Session;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomLogoutHandler implements LogoutHandler {

    private final JwtService jwtService;
    private final SessionService sessionService;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        try {
            DecodedJWT decodedJWT = jwtService.validateAccessToken(jwtService.extractAccessToken(request));
            Session session = sessionService.findById(decodedJWT.getClaim(JwtService.SESSION_ID_CLAIM_NAME).asString());
            sessionService.terminate(session);

            response.addCookie(jwtService.createAccessTokenCookie(null, request.isSecure()));
            response.addCookie(jwtService.createRefreshTokenCookie(null, request.isSecure()));


        } catch (Exception e) {
            response.addCookie(jwtService.createAccessTokenCookie(null, request.isSecure()));
            response.addCookie(jwtService.createRefreshTokenCookie(null, request.isSecure()));
        }
        response.setStatus(HttpServletResponse.SC_OK);
    }
}

