package com.videoplatform.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;

public class JwtUtils {

    private final String jwtSecret;
    private final long jwtExpirationMillis;

    public JwtUtils(String jwtSecret, long jwtExpirationMillis) {
        this.jwtSecret = jwtSecret;
        this.jwtExpirationMillis = jwtExpirationMillis;
    }

    /**
     * Генерирует JWT-токен с "streamId" в payload, сроком жизни jwtExpirationMillis.
     */
    public String generateStreamToken(Long streamId) {
        long now = System.currentTimeMillis();
        Date issuedAt = new Date(now);
        Date expiresAt = new Date(now + jwtExpirationMillis);

        return Jwts.builder()
                .claim("streamId", streamId.toString())
                .setIssuedAt(issuedAt)
                .setExpiration(expiresAt)
                .signWith(SignatureAlgorithm.HS256, jwtSecret)
                .compact();
    }

    /**
     * Валидирует токен и проверяет, что payload.streamId == expectedStreamId.
     */
    public boolean validateStreamToken(String token, Long expectedStreamId) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(jwtSecret)
                    .parseClaimsJws(token)
                    .getBody();
            String sid = claims.get("streamId", String.class);
            return sid != null && sid.equals(expectedStreamId.toString());
        } catch (Exception e) {
            return false;
        }
    }
}