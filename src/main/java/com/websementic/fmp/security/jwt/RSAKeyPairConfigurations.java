package com.websementic.fmp.security.jwt;

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.TimeBasedEpochGenerator;
import com.fasterxml.uuid.impl.UUIDUtil;
import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Configuration
public class RSAKeyPairConfigurations {

    private final JwtConfigurationProperties jwtConfigurationProperties;
    private final Path keyPairsDir;
    private final TimeBasedEpochGenerator generator = Generators.timeBasedEpochGenerator();
    @Getter
    private final ConcurrentHashMap<String, KeyPair> keyPairsWithId = new ConcurrentHashMap<>();

    private String usedKeyPairKey;

    public RSAKeyPairConfigurations(JwtConfigurationProperties jwtConfigurationProperties) throws IOException {
        this.jwtConfigurationProperties = jwtConfigurationProperties;
        keyPairsDir = Files.createDirectories(Path.of(jwtConfigurationProperties.getKeyPairsPath()));
    }

    //TODO : Implement a way to rotate key pairs for a specific duration and also do the cleanup of old key pairs
    @PostConstruct
    public void init() throws IOException, NoSuchAlgorithmException {
        try (Stream<Path> keyPairsDirsStream = Files.list(Path.of(keyPairsDir.toString()));) {

            Set<Path> keyPairsDirs = keyPairsDirsStream.collect(Collectors.toSet());

            this.usedKeyPairKey = getLatestKeyPairId(keyPairsDirs)
                    .orElseGet(() -> generateKeyPairAndSaveAndGetId(keyPairsDir));

            loadAllAvailableKeys(keyPairsDirs);
        }
    }

    private String generateKeyPairAndSaveAndGetId(Path savingDir) {
        try {
            KeyPair keyPair = generateRSAKeyPair();
            String id = generator.generate().toString();
            saveKeyPair(savingDir, id, keyPair);
            keyPairsWithId.put(id, keyPair);
            return id;
        } catch (NoSuchAlgorithmException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Optional<String> getLatestKeyPairId(Set<Path> keyPairPaths) throws IOException, NoSuchAlgorithmException {
        if (keyPairPaths.isEmpty()) return Optional.empty();

        keyPairPaths = keyPairPaths.stream().filter(Files::isDirectory).filter(path -> {
            try {
                UUIDUtil.uuid(path.getFileName().toString());
                return true;
            } catch (Exception ignored) {
                return false;
            }
        }).collect(Collectors.toSet());

        Path newerKeyPair = null;
        for (Path keyPairDir : keyPairPaths) {
            if (newerKeyPair == null) {
                newerKeyPair = keyPairDir;
            } else {
                String currentUuid = keyPairDir.getFileName().toString();
                String newerUuid = newerKeyPair.getFileName().toString();
                if (LocalDateTime.ofInstant(Instant.ofEpochMilli(UUIDUtil.extractTimestamp(UUIDUtil.uuid(currentUuid))), ZoneOffset.systemDefault())
                        .isAfter(LocalDateTime.ofInstant(Instant.ofEpochMilli(UUIDUtil.extractTimestamp(UUIDUtil.uuid(newerUuid))), ZoneOffset.systemDefault()))) {
                    newerKeyPair = keyPairDir;
                }
            }
            try {
                validateKeyPairDir(newerKeyPair);
            } catch (Exception e) {
                log.error("Failed to validate key pair directory", e);
                newerKeyPair = null;
            }
        }
        if (newerKeyPair != null) {
            return Optional.of(newerKeyPair.getFileName().toString());
        }
        return Optional.empty();
    }

    private void loadAllAvailableKeys(Set<Path> keyPairsDirs) {
        for (Path path : keyPairsDirs) {
            try {
                RSAPublicKey rsaPublicKey = RSAKeysUtils.loadPublicKeyFromFile(Path.of(path.toString(), "public.pem").toString());
                RSAPrivateKey rsaPrivateKey = RSAKeysUtils.loadPrivateKeyFromFile(Path.of(path.toString(), "private.pem").toString());
                keyPairsWithId.put(path.getFileName().toString(), new KeyPair(rsaPublicKey, rsaPrivateKey));
            } catch (Exception ignored) {
            }

        }
    }

    private void validateKeyPairDir(Path path) {
        Assert.isTrue(Files.isDirectory(path), "path is not a directory");
        Assert.isTrue(Files.exists(Path.of(path.toString(), "public.pem")), "public.pem file does not exist");
        Assert.isTrue(Files.exists(Path.of(path.toString(), "private.pem")), "private.pem file does not exist");
    }

    private KeyPair generateRSAKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        return kpg.generateKeyPair();
    }

    private void saveKeyPair(@NotNull Path dir, String timeBasedUUID, KeyPair keyPair) throws IOException {
        Path newDir = Files.createDirectory(Path.of(dir.toString(), timeBasedUUID));
        RSAKeysUtils.writeRSAPublicKeyToPem(Path.of(newDir.toString(), "public.pem").toString(), (RSAPublicKey) keyPair.getPublic());
        RSAKeysUtils.writeRSAPrivateKeyAsPem(Path.of(newDir.toString(), "private.pem").toString(), (RSAPrivateKey) keyPair.getPrivate());
    }

    public Optional<KeyPair> getKeyPair(String id) {
        return Optional.of(keyPairsWithId.get(id));
    }

    public String getTokenSigningKeyPairId() {
        return usedKeyPairKey;
    }

    public KeyPair getTokenSigningKeyPair() {
        return Optional.of(keyPairsWithId.get(usedKeyPairKey))
                .orElseGet(() -> keyPairsWithId.get(generateKeyPairAndSaveAndGetId(keyPairsDir)));
    }

}
