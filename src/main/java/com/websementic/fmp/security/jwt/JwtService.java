package com.websementic.fmp.security.jwt;


import com.auth0.jwt.interfaces.DecodedJWT;
import com.websementic.fmp.exeption.BadArgumentException;
import com.websementic.fmp.exeption.NotFoundException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.userdetails.UserDetails;

public interface JwtService {

    public static final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";
    public static final String ACCESS_TOKEN_COOKIE_NAME = "access_token";
    public static final String ROLES_CLAIM_NAME = "ROLES";
    public static final String TOKEN_TYPE_CLAIM_NAME = "TOKEN_TYPE";
    public static final String SESSION_ID_CLAIM_NAME = "SESSION_ID";
    public static final String TOKEN_TYPE_REFRESH_NAME = "REFRESH";
    public static final String TOKEN_TYPE_ACCESS_NAME = "ACCESS";


    DecodedJWT validateAccessToken(String token);

    DecodedJWT validateRefreshToken(String token);

    String generateAccessToken(UserDetails userDetails, String sessionId) throws BadArgumentException;

    String generateRefreshToken(UserDetails userDetails, String sessionId) throws BadArgumentException;

    //in production is secure should always be true
    Cookie createAccessTokenCookie(String token, boolean isSecure);

    Cookie createRefreshTokenCookie(String token, boolean isSecure);

    String extractAccessToken(HttpServletRequest request);

    String extractRefreshToken(HttpServletRequest request);

    void blackListRefreshToken(String jti);

    void blackListAccessToken(String jti);

    void extendSessionExpirationWindowAsync(String sessionId) throws NotFoundException;


}
