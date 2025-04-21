package com.websementic.fmp.security.jwt;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.security.interfaces.RSAPublicKey;

@Service
@RequiredArgsConstructor
public class JWKSetService {

    private final RSAKeyPairConfigurations rsaKeyPairConfigurations;

    @Cacheable(value = "jwk_sets")
    public JWKSet jwkSet() {
        return new JWKSet(rsaKeyPairConfigurations.getKeyPairsWithId().entrySet().stream()
                .map(keyPairWithId -> (JWK) new RSAKey.Builder((RSAPublicKey) keyPairWithId.getValue().getPublic())
                        .keyUse(KeyUse.SIGNATURE)
                        .algorithm(JWSAlgorithm.RS256)
                        .keyID(keyPairWithId.getKey()).build()).toList());
    }
}
