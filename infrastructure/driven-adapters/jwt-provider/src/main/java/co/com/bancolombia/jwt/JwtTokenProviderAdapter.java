package co.com.bancolombia.jwt;

import co.com.bancolombia.model.auth.TokenClaims;
import co.com.bancolombia.model.exception.UnauthorizedException;
import co.com.bancolombia.model.user.User;
import co.com.bancolombia.model.user.gateways.TokenProvider;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProviderAdapter implements TokenProvider {

    private static final String EXPECTED_ISSUER = "ms-user";

    private final RSAPrivateKey rsaPrivateKey;
    private final RSAPublicKey rsaPublicKey;
    private final JwtProperties jwtProperties;

    @Override
    public Mono<String> generateAccessToken(User user) {
        return Mono.fromCallable(() -> {
            Instant now = Instant.now();
            Instant expiry = now.plusSeconds(jwtProperties.accessTokenExpirationMinutes() * 60L);

            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .subject(user.getEmail())
                    .issuer(EXPECTED_ISSUER)
                    .issueTime(Date.from(now))
                    .expirationTime(Date.from(expiry))
                    .jwtID(UUID.randomUUID().toString())
                    .claim("userId", user.getId())
                    .build();

            SignedJWT jwt = new SignedJWT(
                    new JWSHeader.Builder(JWSAlgorithm.RS256).keyID("ms-user-key").build(),
                    claims
            );
            jwt.sign(new RSASSASigner(rsaPrivateKey));
            return jwt.serialize();
        });
    }

    @Override
    public Mono<TokenClaims> validateToken(String token) {
        return Mono.fromCallable(() -> {
            try {
                SignedJWT jwt = SignedJWT.parse(token);

                if (!jwt.verify(new RSASSAVerifier(rsaPublicKey))) {
                    throw new UnauthorizedException("INVALID_TOKEN", "Token inválido");
                }

                JWTClaimsSet claims = jwt.getJWTClaimsSet();

                if (!EXPECTED_ISSUER.equals(claims.getIssuer())) {
                    throw new UnauthorizedException("INVALID_TOKEN", "Token inválido");
                }

                Date expiration = claims.getExpirationTime();
                if (expiration == null || expiration.before(new Date())) {
                    throw new UnauthorizedException("TOKEN_EXPIRED", "Token expirado");
                }

                return TokenClaims.builder()
                        .userId(claims.getStringClaim("userId"))
                        .build();
            } catch (UnauthorizedException e) {
                throw e;
            } catch (Exception e) {
                log.warn("Token validation failed unexpectedly: {}", e.getMessage());
                throw new UnauthorizedException("INVALID_TOKEN", "Token inválido");
            }
        });
    }
}
