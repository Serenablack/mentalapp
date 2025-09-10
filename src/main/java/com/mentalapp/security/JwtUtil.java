package com.mentalapp.security;

import com.mentalapp.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
@Slf4j
public class JwtUtil {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.access-token.expiration:900000}")
    private long accessTokenExpiration;

    @Value("${jwt.issuer:mental-health-app}")
    private String issuer;

    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // Extract email from token (subject is now email)
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Extract email from token
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Extract userId from custom claims
    public Long extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", Long.class));
    }

    // Extract username from custom claims
    public String extractUsernameFromClaims(String token) {
        return extractClaim(token, claims -> claims.get("username", String.class));
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                       .setSigningKey(getSigningKey())
                       .build()
                       .parseClaimsJws(token)
                       .getBody();
        } catch (Exception e) {
            log.error("Error extracting claims from JWT token: {}", e.getMessage());
            throw e;
        }
    }

    public Boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (Exception e) {
            log.warn("Error checking token expiration: {}", e.getMessage());
            return true;
        }
    }

    // Generate token with User entity - use email as subject for consistency
    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", user.getEmail());
        claims.put("username", user.getUsername());
        claims.put("userId", user.getId());
        // Use email as subject since it's unique and what we'll lookup by
        return createToken(claims, user.getEmail());
    }

    // Generate token with UserDetails - for backward compatibility
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        // If UserDetails is actually our User entity, cast and get full info
        if (userDetails instanceof User) {
            User user = (User) userDetails;
            claims.put("email", user.getEmail());
            claims.put("username", user.getUsername());
            claims.put("userId", user.getId());
            return createToken(claims, user.getEmail());
        }
        // Fallback for generic UserDetails
        return createToken(claims, userDetails.getUsername());
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                   .setClaims(claims)
                   .setSubject(subject)
                   .setIssuer(issuer)
                   .setIssuedAt(new Date(System.currentTimeMillis()))
                   .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
                   .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                   .compact();
    }

    // Updated validation method - now compares email (subject) with username
    public Boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String emailFromToken = extractEmail(token);

            // If UserDetails is our User entity, compare with email
            if (userDetails instanceof User) {
                User user = (User) userDetails;
                return (emailFromToken.equals(user.getEmail()) && !isTokenExpired(token));
            }

            // Fallback comparison with username
            return (emailFromToken.equals(userDetails.getUsername()) && !isTokenExpired(token));
        } catch (Exception e) {
            log.warn("Error validating token for user {}: {}", userDetails.getUsername(), e.getMessage());
            return false;
        }
    }
}