package org.iskm.admin.web.util;

import java.time.Instant;
import java.util.Date;

import static org.iskm.admin.web.util.Constants.EXPIRATION_TIME_IN_MS;
import org.springframework.stereotype.Component;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;

import jakarta.servlet.http.Cookie;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JwtUtil {

    private static final String SECRET_KEY = "your_secret_key";

    private final Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY);

    public String generateToken(String username) {
        log.info("Generating token for user: {}", username);
        return JWT.create()
                .withSubject(username)
                .withIssuedAt(Instant.now())
                .withIssuer("IskmAdminApp")
                .withExpiresAt(Instant.now().plusMillis(EXPIRATION_TIME_IN_MS))
                .sign(algorithm);
    }

    public DecodedJWT validateToken(String token) {
        return decodedJWT(token);
    }

    public DecodedJWT decodedJWT(String token) {
        return getVerifier().verify(token);
    }

    public boolean isTokenExpired(String token) {
        DecodedJWT decodedJWT = decodedJWT(token);
        return decodedJWT.getExpiresAt().before(Date.from(Instant.now()));
    }

    private JWTVerifier getVerifier() {
        return JWT.require(algorithm).withIssuer("IskmAdminApp").build();
    }

    public String refreshToken(String token) {
        DecodedJWT decodedJWT = decodedJWT(token);
        String username = decodedJWT.getSubject();
        log.info("Refreshing token for user: {}", username);
        return generateToken(username);
    }

    public Cookie generateHttpOnlyCookie(String token) {
        log.info("Generating cookie for user: {}", token.substring(0, 5));
        var cookie = new Cookie("login.at", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(Constants.EXPIRATION_TIME_IN_MS / 1000); // 1 day
        log.info("Generated cookie for user: {}", cookie.getValue().substring(0, 5));
        return cookie;
    }
}
