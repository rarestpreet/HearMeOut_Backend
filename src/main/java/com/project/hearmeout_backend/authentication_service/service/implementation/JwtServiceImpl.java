package com.project.hearmeout_backend.authentication_service.service.implementation;

import com.project.hearmeout_backend.authentication_service.model.CustomUserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtServiceImpl {

  private final SecretKey secretKey;

  public JwtServiceImpl(@Value("${jwt.secret}") String secretKey) {
    this.secretKey = Keys.hmacShaKeyFor(secretKey.getBytes());
  }

  public String generateJwtToken(String username) {
    return Jwts.builder()
        .subject(username)
        .issuedAt(new Date())
        .expiration(new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(30)))
        .signWith(secretKey)
        .compact();
  }

  public Claims parseToken(String token) {
    return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
  }

  public String extractUsername(String token) {
    return parseToken(token).getSubject();
  }

  public Date extractExpiration(String token) {
    return parseToken(token).getExpiration();
  }

  public boolean isTokenValid(String token, CustomUserDetails userDetails) {
    return (extractUsername(token).equals(userDetails.getUsername())
        && !extractExpiration(token).before(new Date()));
  }

  public String generateRefreshToken() {
    SecureRandom random = new SecureRandom();

    byte[] bytes = new byte[64];
    random.nextBytes(bytes);

    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }
}
