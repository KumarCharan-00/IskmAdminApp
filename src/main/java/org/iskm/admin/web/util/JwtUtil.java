package org.iskm.admin.web.util;

import java.util.Date;

import org.springframework.stereotype.Component;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;

@Component
public class JwtUtil {

    private static final String SECRET_KEY = "your_secret_key";
    private static final long EXPIRATION_TIME = 1000 * 60 * 60; // 1 hour

    private final Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY);

    public String generateToken(String username) {
        return JWT.create()
                .withSubject(username)
                .withIssuedAt(new Date(System.currentTimeMillis()))
                .withIssuer("IskmAdminApp")
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .sign(algorithm);
    }

    public DecodedJWT validateToken(String token, String username) {
        return decodedJWT(token);
    }

    public DecodedJWT decodedJWT(String token) {
        return getVerifier().verify(token);
    }

    private JWTVerifier getVerifier() {
        return JWT.require(algorithm).withIssuer("IskmAdminApp").build();
    }
}