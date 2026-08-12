package com.main.AqarCustomer.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    // Injected from application.properties (jwt.secret) — the raw string used to build the signing key.
    @Value("${jwt.secret}")
    private String secret;

    // Injected from application.properties (jwt.expiration.ms) — how long a token stays valid, in milliseconds.
    @Value("${jwt.expiration.ms}")
    private Long expirationMs;

    // Builds the cryptographic key used to sign and verify tokens, from the raw secret string (HMAC-SHA algorithm family).
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    // Creates a new signed JWT for the given email: sets the subject (who the token belongs to),
    // the issue time, an expiry time, and signs it with the secret key so it can't be tampered with.
    public String generateToken(String email) {
        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    // Reads a token and pulls out the email (subject) stored inside it, without needing the caller to parse claims manually.
    public String extractEmail(String token) {
        return parseClaims(token).getSubject();
    }

    // Verifies the token's signature against the signing key, then decodes and returns its claims (payload).
    // Throws a JwtException if the token is invalid, expired, or tampered with.
    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // Attempts to parse the token; returns true if it's valid (signature checks out, not expired),
    // false if parsing throws an exception — used as a quick check before trusting a token.
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}