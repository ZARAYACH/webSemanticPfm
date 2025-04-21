package com.websementic.fmp.security.jwt;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.websementic.fmp.exeption.BadArgumentException;
import com.websementic.fmp.exeption.TokenValidationException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.userdetails.UserDetails;


interface TokenService {

    DecodedJWT validateToken(String token) throws TokenValidationException;

    String buildToken(UserDetails userDetails, String sessionId) throws BadArgumentException;

    Long getExpirationTimeInSeconds();

    Cookie buildTokenCookie(String token, boolean isSecure);

    String extractToken(HttpServletRequest request);

    void blackListToken(String jti);

}
