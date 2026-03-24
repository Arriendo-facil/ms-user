package co.com.bancolombia.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security.jwt")
public record JwtProperties(
        long accessTokenExpirationMinutes,
        long refreshTokenExpirationDays
) {}
