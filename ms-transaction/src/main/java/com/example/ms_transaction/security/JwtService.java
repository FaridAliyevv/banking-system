package com.example.ms_transaction.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey secretKey;

    public JwtService(@Value("${jwt.secret}") String secret) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String generateServiceToken() {
        return Jwts.builder()
                .subject("0")
                .claim("role", "SERVICE")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 5 * 60 * 1000))
                .signWith(secretKey)
                .compact();
    }
}