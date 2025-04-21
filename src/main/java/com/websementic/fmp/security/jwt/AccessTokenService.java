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
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.cache.RedisCacheManager;
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
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Service
@Slf4j
class AccessTokenService implements TokenService {

    private final JwtConfigurationProperties jwtConfigurationProperties;
    private final RedisCacheManager redisCacheManager;
    private final Cache sessionsCache;
    private final Cache accessTokensCache;
    private final SessionService sessionService;
    private final RSAKeyPairConfigurations rsaKeyPairConfigurations;

    public AccessTokenService(JwtConfigurationProperties jwtConfigurationProperties, RedisCacheManager redisCacheManager,
                              SessionService sessionService, RSAKeyPairConfigurations rsaKeyPairConfigurations1) {
        this.jwtConfigurationProperties = jwtConfigurationProperties;
        this.redisCacheManager = redisCacheManager;
        this.sessionService = sessionService;
        this.sessionsCache = redisCacheManager.getCache(CustomRedisCacheConfigurations.SESSION_IDS_CACHE_NAME);
        this.accessTokensCache = redisCacheManager.getCache(CustomRedisCacheConfigurations.ACCESS_JTI_CACHE_NAME);
        this.rsaKeyPairConfigurations = rsaKeyPairConfigurations1;
    }

    @Override
    public DecodedJWT validateToken(String token) throws TokenValidationException {
        try {
            DecodedJWT decodedJWT = JWT.require(getAlgorithm(JWT.decode(token).getKeyId())).build().verify(token);
            validateTokenClaims(decodedJWT);
            return decodedJWT;
        } catch (Exception verificationEx) {
            throw new TokenValidationException(verificationEx);
        }
    }

    private void validateTokenClaims(DecodedJWT decodedJWT) throws BadArgumentException {
        try {
            Assert.hasText(decodedJWT.getId(), "JTI can't be empty");
            Assert.hasText(decodedJWT.getSubject(), "Subject cannot be empty");
            Assert.hasText(decodedJWT.getClaim(SESSION_ID_CLAIM_NAME).asString(), "sessionId cannot be empty");
            Assert.isTrue(Objects.equals(decodedJWT.getClaim(TOKEN_TYPE_CLAIM_NAME).asString(), TOKEN_TYPE_ACCESS_NAME), "Invalid Token type ");
            Assert.isTrue(validJTI(decodedJWT.getId()), "Token is black listed.");
            Assert.isTrue(validSession(decodedJWT.getClaim(SESSION_ID_CLAIM_NAME).asString()), "Session expired");
        } catch (Exception e) {
            throw new BadArgumentException(e);
        }
    }

    private boolean validJTI(String id) {
        if (accessTokensCache != null) {
            try {
                return accessTokensCache.get(id) == null;
            } catch (Exception ignore) {
            }
        }
        return true;
    }

    private boolean validSession(String sessionId) {
        if (sessionsCache != null) {
            try {
                return sessionsCache.get(sessionId) == null;
            } catch (RedisConnectionFailureException ignore) {
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
                    .withClaim(SESSION_ID_CLAIM_NAME, sessionId)
                    .withClaim(TOKEN_TYPE_CLAIM_NAME, TOKEN_TYPE_ACCESS_NAME)
                    .withIssuedAt(now)
                    .withExpiresAt(now.plusSeconds(jwtConfigurationProperties.getAccessTokenValidityInSeconds()))
                    .sign(getSigningAlgorithm());
        } catch (Exception e) {
            throw new AuthenticationServiceUnavailableException("Auth service unavailable", e);
        }
    }

    @Override
    public Long getExpirationTimeInSeconds() {
        return jwtConfigurationProperties.getAccessTokenValidityInSeconds();
    }

    @Override
    public Cookie buildTokenCookie(String token, boolean isSecure) {
        Cookie accessTokenCookie = new Cookie(ACCESS_TOKEN_COOKIE_NAME, token);
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setSecure(isSecure);
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(Math.toIntExact(getExpirationTimeInSeconds()));
        return accessTokenCookie;
    }

    @Override
    public String extractToken(HttpServletRequest request) {
        String authorizationHeader = request.getHeader(AUTHORIZATION);
        Cookie authorizationCookie = null;
        if (request.getCookies() != null && request.getCookies().length > 0) {
            authorizationCookie = stream(request.getCookies())
                    .filter(cookie -> cookie.getName().equals(ACCESS_TOKEN_COOKIE_NAME))
                    .findFirst().orElse(null);
        }
        if (authorizationCookie != null && StringUtils.isNotBlank(authorizationCookie.getValue())) {
            return authorizationCookie.getValue();
        } else if (StringUtils.isNotBlank(authorizationHeader) && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring("Bearer ".length());
        }
        throw new UnauthenticatedException("Could not extract access token from request");
    }

    @Override
    public void blackListToken(String jti) {
        try {
            if (accessTokensCache != null) {
                accessTokensCache.put(jti, LocalDateTime.now().toEpochSecond(ZoneOffset.systemDefault().getRules().getOffset(Instant.now())));
            }
        } catch (RedisConnectionFailureException ignored) {
        }
        //TODO: implement a blacklist in the database as a alternative when redis is unreachable
    }

    private Algorithm getAlgorithm(String id) {
        KeyPair keyPair = rsaKeyPairConfigurations.getKeyPair(id).orElseThrow(() -> new AuthenticationInvalidTokenException("Signing Key pair not found"));
        return Algorithm.RSA256(
                (RSAPublicKey) keyPair.getPublic(),
                (RSAPrivateKey) keyPair.getPrivate());
    }

    private Algorithm getSigningAlgorithm() {
        KeyPair keyPair = rsaKeyPairConfigurations.getTokenSigningKeyPair();
        return Algorithm.RSA256((RSAPublicKey) keyPair.getPublic(), (RSAPrivateKey) keyPair.getPrivate());
    }
}

