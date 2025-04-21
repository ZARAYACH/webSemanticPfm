package com.websementic.fmp.configuration;

import com.websementic.fmp.security.jwt.JwtConfigurationProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
@Slf4j
@EnableCaching
@RequiredArgsConstructor
public class CustomRedisCacheConfigurations implements CachingConfigurer {

    public static final String SESSION_IDS_CACHE_NAME = "sessionId";
    public static final String REFRESH_JTI_CACHE_NAME = "refreshJti";
    public static final String ACCESS_JTI_CACHE_NAME = "accessJti";

    private final CustomCacheErrorHandler customCacheErrorHandler;
    private final JwtConfigurationProperties jwtConfigurationProperties;
    private final RedisConnectionFactory redisConnectionFactory;

    @Override
    public CacheErrorHandler errorHandler() {
        return customCacheErrorHandler;
    }

    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer() {
        return (builder) -> builder
                .withCacheConfiguration(SESSION_IDS_CACHE_NAME,
                        RedisCacheConfiguration.defaultCacheConfig()
                                .prefixCacheNameWith("BLACKLIST:")
                                .disableCachingNullValues()
                                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericToStringSerializer<>(Object.class)))
                                .entryTtl(Duration.ofSeconds(jwtConfigurationProperties.getRefreshTokenValidityInSeconds())))
                .withCacheConfiguration(REFRESH_JTI_CACHE_NAME,
                        RedisCacheConfiguration.defaultCacheConfig()
                                .prefixCacheNameWith("BLACKLIST:")
                                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericToStringSerializer<>(Object.class)))
                                .disableCachingNullValues()
                                .entryTtl(Duration.ofSeconds(jwtConfigurationProperties.getRefreshTokenValidityInSeconds())))
                .withCacheConfiguration(ACCESS_JTI_CACHE_NAME,
                        RedisCacheConfiguration.defaultCacheConfig()
                                .disableCachingNullValues()
                                .prefixCacheNameWith("BLACKLIST:")
                                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericToStringSerializer<>(Object.class)))
                                .entryTtl(Duration.ofSeconds(jwtConfigurationProperties.getRefreshTokenValidityInSeconds())));
    }

}