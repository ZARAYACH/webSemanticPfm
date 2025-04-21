package com.websementic.fmp.security.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.websementic.fmp.configuration.CustomRedisCacheConfigurations;
import com.websementic.fmp.exeption.*;
import com.websementic.fmp.security.service.SessionService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.websementic.fmp.security.jwt.JwtService.*;
import static java.util.Arrays.stream;

@Service
@Slf4j
class RefreshTokenService implements TokenService {

    private final CacheManager cacheManager;
    private final Cache sessionCache;
    private final Cache refreshTokensCache;
    private final JwtConfigurationProperties jwtConfigurationProperties;
    private final SessionService sessionService;
    private final RSAKeyPairConfigurations rsaKeyPairConfigurations;

    public RefreshTokenService(CacheManager cacheManager, JwtConfigurationProperties jwtConfigurationProperties,
                               SessionService sessionService, RSAKeyPairConfigurations rsaKeyPairConfigurations1) {
        this.jwtConfigurationProperties = jwtConfigurationProperties;
        this.rsaKeyPairConfigurations = rsaKeyPairConfigurations1;
        this.cacheManager = cacheManager;
        this.sessionCache = cacheManager.getCache(CustomRedisCacheConfigurations.SESSION_IDS_CACHE_NAME);
        this.refreshTokensCache = cacheManager.getCache(CustomRedisCacheConfigurations.REFRESH_JTI_CACHE_NAME);
        this.sessionService = sessionService;
    }

    @Override
    public DecodedJWT validateToken(String token) throws TokenValidationException {
        try {
            DecodedJWT decodedJWT = JWT.require(getAlgorithm(JWT.decode(token).getKeyId())).build().verify(token);
            validateTokenClaims(decodedJWT);
            return decodedJWT;
        } catch (Exception e) {
            throw new TokenValidationException(e);
        }

    }

    private void validateTokenClaims(DecodedJWT decodedJWT) throws BadArgumentException {
        try {
            Assert.hasText(decodedJWT.getId(), "JTI can't be empty");
            Assert.hasText(decodedJWT.getSubject(), "Subject cannot be empty");
            Assert.isTrue(Objects.equals(decodedJWT.getClaim(TOKEN_TYPE_CLAIM_NAME).asString(), TOKEN_TYPE_REFRESH_NAME), "Invalid Token type ");
            Assert.hasText(decodedJWT.getClaim(SESSION_ID_CLAIM_NAME).asString(), "sessionId cannot be empty");
            Assert.isTrue(validJTI(decodedJWT.getId()), "Token is Black listed.");
            Assert.isTrue(validSession(decodedJWT.getClaim(SESSION_ID_CLAIM_NAME).asString()), "Session expired");
        } catch (Exception e) {
            throw new BadArgumentException(e);
        }
    }

    // To preserver the stateless nature of jwt's , the cache should be reachable if not we would divert to using the db to check for session validation
    private boolean validJTI(String id) {
        if (refreshTokensCache != null) {
            try {
                return refreshTokensCache.get(id) == null;
                //TODO: implement a backup way to check if the token is black listed if the cache is unreachable
            } catch (Exception ignored) {
            }
        }
        return true;
    }

    private boolean validSession(String sessionId) {
        if (sessionCache != null) {
            try {
                return sessionCache.get(sessionId) == null;
            } catch (RedisConnectionFailureException ignored) {
            }
        }
        try {
            return sessionService.findById(sessionId).getExpiredAt().isAfter(LocalDateTime.now());
        } catch (NotFoundException e) {
            return false;
        }
    }

    @Override
    public String buildToken(UserDetails userDetails, String sessionId) throws AuthenticationServiceUnavailableException, BadArgumentException {
        try {
            Assert.notNull(userDetails, "UserDetails must not be null");
            Assert.hasText(sessionId, "SessionId must not be null or empty");
        } catch (Exception e) {
            throw new BadArgumentException(e);
        }
        Instant now = Instant.now();
        try {
            return JWT.create()
                    .withJWTId(UUID.randomUUID().toString())
                    .withKeyId(rsaKeyPairConfigurations.getTokenSigningKeyPairId())
                    .withSubject(userDetails.getUsername())
                    .withClaim(ROLES_CLAIM_NAME, userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()))
                    .withClaim(TOKEN_TYPE_CLAIM_NAME, TOKEN_TYPE_REFRESH_NAME)
                    .withClaim(SESSION_ID_CLAIM_NAME, sessionId)
                    .withIssuedAt(now)
                    .withExpiresAt(now.plusSeconds(jwtConfigurationProperties.getRefreshTokenValidityInSeconds()))
                    .sign(getSigningAlgorithm());
        } catch (Exception e) {
            throw new AuthenticationServiceUnavailableException("Auth service unavailable", e);
        }
    }

    @Override
    public Long getExpirationTimeInSeconds() {
        return jwtConfigurationProperties.getRefreshTokenValidityInSeconds();
    }

    @Override
    public Cookie buildTokenCookie(String token, boolean isSecure) {
        Cookie refreshTokenCookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, token);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(isSecure);
        refreshTokenCookie.setPath("/api/v1/tokens");
        refreshTokenCookie.setMaxAge(Math.toIntExact(getExpirationTimeInSeconds()));
        return refreshTokenCookie;
    }

    @Override
    public String extractToken(HttpServletRequest request) {
        Cookie refreshTokenCookie = null;
        if (request.getCookies() != null && request.getCookies().length > 0) {
            refreshTokenCookie = stream(request.getCookies())
                    .filter(cookie -> cookie.getName().equals(REFRESH_TOKEN_COOKIE_NAME))
                    .findFirst().orElse(null);
        }
        if (refreshTokenCookie != null && StringUtils.isNotBlank(refreshTokenCookie.getValue())) {
            return refreshTokenCookie.getValue();
        }
        throw new UnauthenticatedException("Could not extract refresh token from request");
    }

    @Override
    public void blackListToken(String jti) {
        try {
            if (refreshTokensCache != null) {
                refreshTokensCache.put(jti, LocalDateTime.now().toEpochSecond(ZoneOffset.systemDefault().getRules().getOffset(Instant.now())));
            }
        } catch (RedisConnectionFailureException ignored) {
        }
        //TODO: implement a blacklist in the database as a alternative when redis is unreachable
    }

    private Algorithm getAlgorithm(String id) {
        KeyPair keyPair = rsaKeyPairConfigurations.getKeyPair(id).orElseThrow(() -> new AuthenticationInvalidTokenException("Signing Algorithm not found"));
        return Algorithm.RSA256((RSAPublicKey) keyPair.getPublic(), (RSAPrivateKey) keyPair.getPrivate());
    }

    private Algorithm getSigningAlgorithm() {
        KeyPair keyPair = rsaKeyPairConfigurations.getTokenSigningKeyPair();
        return Algorithm.RSA256((RSAPublicKey) keyPair.getPublic(), (RSAPrivateKey) keyPair.getPrivate());
    }
}

