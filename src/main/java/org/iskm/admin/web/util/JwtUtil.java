package org.iskm.admin.web.util;

import java.time.Instant;
import java.util.Date;

import jakarta.servlet.http.Cookie;
import org.springframework.stereotype.Component;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;

import static org.iskm.admin.web.util.Constants.EXPIRATION_TIME_IN_MS;

@Component
public class JwtUtil {

    private static final String SECRET_KEY = "your_secret_key";

    private final Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY);

    public String generateToken(String username) {
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
        return generateToken(username);
    }

    public Cookie generateHttpOnlyCookie(String token) {
        var cookie = new Cookie("login.at", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(Constants.EXPIRATION_TIME_IN_MS / 1000); // 1 day
        return cookie;
    }
}