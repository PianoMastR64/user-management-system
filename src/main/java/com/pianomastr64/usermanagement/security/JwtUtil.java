package com.pianomastr64.usermanagement.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Arrays;
import java.util.Date;

@Component
public class JwtUtil {
    private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 24; // 1 day in milliseconds
    
    private final SecretKey key;
    private final JwtParser parser;
    
    public JwtUtil(@Value("${jwt.secret}") String secret, Environment env) {
        if(secret == null || secret.length() < 32) {
            throw new IllegalStateException("JWT secret must be set and at least 32 characters.");
        }
        
        
        if("very_long_secret_key_for_development".equals(secret)) {
            boolean isProd = Arrays.asList(env.getActiveProfiles()).contains("prod");
            if(isProd) {
                throw new IllegalStateException("JWT secret must be set in production.");
            }
            
            System.out.println("Warning: Using default development JWT secret key. " +
                "Please set a secure key in production.");
        }
        
        
        
        key = Keys.hmacShaKeyFor(secret.getBytes());
        parser = Jwts.parser()
            .verifyWith(key)
            .build();
    }
    
    public String generateToken(Long userId) {
        return Jwts.builder()
            .subject(userId.toString())
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
            .signWith(key)
            .compact();
    }
    
    public Long extractId(String token) {
        String id = parser
            .parseSignedClaims(token)
            .getPayload()
            .getSubject();
        return Long.valueOf(id);
    }
    
    public boolean validateToken(String token) {
        try {
            parser.parseSignedClaims(token);
            return true;
        } catch(JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
