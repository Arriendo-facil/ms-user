package co.com.bancolombia.config.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security.jwt")
public record JwtProperties(
        long accessTokenExpirationMinutes,
        long refreshTokenExpirationDays
) {}
