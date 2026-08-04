package com.boot.pay.auth;

import cn.hutool.core.util.IdUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Token 工具 — accessToken(JWT) + refreshToken(UUID)
 */
@Component
public class JwtTokenUtil {

    private final SecretKey secretKey;
    private final long accessExpiration;
    private final long refreshExpiration;

    public JwtTokenUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-expiration}") long accessExpiration,
            @Value("${jwt.refresh-expiration}") long refreshExpiration) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessExpiration = accessExpiration;
        this.refreshExpiration = refreshExpiration;
    }

    // ========== accessToken ==========

    public String generateAccessToken(Long userId, String username, String nickname) {
        Date now = new Date();
        return Jwts.builder()
                .setId(IdUtil.fastSimpleUUID())
                .setSubject(String.valueOf(userId))
                .claim("username", username)
                .claim("nickname", nickname)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + accessExpiration))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims parseAccessToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean isAccessTokenExpired(String token) {
        try {
            return parseAccessToken(token).getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    public Long getUserIdFromToken(String token) {
        return Long.valueOf(parseAccessToken(token).getSubject());
    }

    // ========== refreshToken ==========

    public String generateRefreshToken() {
        return IdUtil.fastSimpleUUID();
    }

    public long getRefreshExpiration() {
        return refreshExpiration;
    }
}
