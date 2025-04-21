package com.websementic.fmp.security.jwt;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.websementic.fmp.exeption.AuthenticationInvalidTokenException;
import com.websementic.fmp.exeption.BadArgumentException;
import com.websementic.fmp.security.service.SessionService;
import com.websementic.fmp.user.model.Session;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {

    private final AccessTokenService accessTokenService;
    private final RefreshTokenService refreshTokenService;
    private final SessionService sessionService;

    @Override
    public DecodedJWT validateAccessToken(String token) {
        try {
            return accessTokenService.validateToken(token);
        } catch (Exception e) {
            throw new AuthenticationInvalidTokenException("Invalid access token", e);
        }
    }


    @Override
    public DecodedJWT validateRefreshToken(String token) {
        try {
            return refreshTokenService.validateToken(token);
        } catch (Exception e) {
            throw new AuthenticationInvalidTokenException("Invalid refresh token", e);
        }
    }

    @Override
    public String generateAccessToken(UserDetails userDetails, String sessionId) throws BadArgumentException {
        return accessTokenService.buildToken(userDetails, sessionId);
    }

    @Override
    public String generateRefreshToken(UserDetails userDetails, String sessionId) throws BadArgumentException {
        return refreshTokenService.buildToken(userDetails, sessionId);
    }


    @Override
    public Cookie createAccessTokenCookie(String token, boolean isSecure) {
        return accessTokenService.buildTokenCookie(token, isSecure);
    }

    @Override
    public Cookie createRefreshTokenCookie(String token, boolean isSecure) {
        return refreshTokenService.buildTokenCookie(token, isSecure);
    }

    @Override
    public String extractAccessToken(HttpServletRequest request) {
        return accessTokenService.extractToken(request);
    }

    @Override
    public String extractRefreshToken(HttpServletRequest request) {
        return refreshTokenService.extractToken(request);
    }

    @Override
    public void blackListRefreshToken(String jti) {
        refreshTokenService.blackListToken(jti);
    }

    @Override
    public void blackListAccessToken(String jti) {
        accessTokenService.blackListToken(jti);
    }

    @Override
    public void extendSessionExpirationWindowAsync(@NotBlank String sessionId) {
        CompletableFuture.runAsync(() -> {
            try {
                Session session = sessionService.findById(sessionId);
                sessionService.extendSessionExpiration(session, refreshTokenService.getExpirationTimeInSeconds());
            } catch (Exception e) {
                log.error("Couldn't Extend session", e);
            }
        });

    }
}