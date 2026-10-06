package com.mycompany.quanlysieuthi.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public class JwtUtil {

    private static final String DEFAULT_SECRET = "quanlysieuthi_jwt_secret_key_2026_super_secure_32bytes_min!";
    private static final long ACCESS_TOKEN_VALIDITY_MS = 30L * 60 * 1000; // 30 minutes
    private static final long REFRESH_TOKEN_VALIDITY_MS = 7L * 24 * 60 * 60 * 1000; // 7 days

    private static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(DEFAULT_SECRET.getBytes(StandardCharsets.UTF_8));

    private JwtUtil() {
    }

    public static String generateAccessToken(String employeeId, String username, String fullName, String position) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + ACCESS_TOKEN_VALIDITY_MS);

        return Jwts.builder()
                .subject(username)
                .claim("employeeId", employeeId)
                .claim("fullName", fullName)
                .claim("position", position)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(SECRET_KEY)
                .compact();
    }

    public static String generateRefreshToken(String employeeId, String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + REFRESH_TOKEN_VALIDITY_MS);

        return Jwts.builder()
                .subject(username)
                .claim("employeeId", employeeId)
                .claim("type", "REFRESH")
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(SECRET_KEY)
                .compact();
    }

    public static Claims parseToken(String token) throws JwtException {
        return Jwts.parser()
                .verifyWith(SECRET_KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public static boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
