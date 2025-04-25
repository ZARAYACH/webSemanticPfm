package com.websementic.fmp.security.jwt;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@EnableConfigurationProperties(JwtConfigurationProperties.class)
@ConfigurationProperties(prefix = "app.security.jwt")
@Configuration
public class JwtConfigurationProperties {
    private String KeyPairsPath = "/home/webSemanticProject/keys" ;
    private Long accessTokenValidityInSeconds = 900L;
    private Long refreshTokenValidityInSeconds = 2628000L;

}
