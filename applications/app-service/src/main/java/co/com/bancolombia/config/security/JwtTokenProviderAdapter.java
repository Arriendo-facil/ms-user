package co.com.bancolombia.config.security;

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
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtTokenProviderAdapter implements TokenProvider {

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
                    .issuer("ms-user")
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

                if (claims.getExpirationTime().before(new Date())) {
                    throw new UnauthorizedException("TOKEN_EXPIRED", "Token expirado");
                }

                return TokenClaims.builder()
                        .userId(claims.getStringClaim("userId"))
                        .build();
            } catch (UnauthorizedException e) {
                throw e;
            } catch (Exception e) {
                throw new UnauthorizedException("INVALID_TOKEN", "Token inválido");
            }
        });
    }
}
