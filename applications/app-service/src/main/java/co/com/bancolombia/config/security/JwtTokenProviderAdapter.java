package co.com.bancolombia.config.security;

import co.com.bancolombia.model.user.User;
import co.com.bancolombia.model.user.gateways.TokenProvider;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.security.interfaces.RSAPrivateKey;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtTokenProviderAdapter implements TokenProvider {

    private final RSAPrivateKey rsaPrivateKey;
    private final JwtProperties jwtProperties;

    @Override
    public Mono<String> generateAccessToken(User user) {
        return Mono.fromCallable(() -> {
            Instant now = Instant.now();
            Instant expiry = now.plusSeconds(jwtProperties.accessTokenExpirationMinutes() * 60);

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
}
