package com.bankshield.common.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 */
public class JwtUtil {
    
    /**
     * 密钥
     */
    private static final String SECRET_KEY = "bankshield-jwt-secret-key-for-token-generation-2024";
    
    /**
     * 过期时间（24小时）
     */
    private static final long EXPIRATION = 86400000L;
    
    /**
     * 生成SecretKey
     */
    private static SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
    }
    
    /**
     * 生成Token
     */
    public static String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", username);
        return createToken(claims, username);
    }
    
    /**
     * 生成Token（带额外信息）
     */
    public static String generateToken(String username, Map<String, Object> claims) {
        if (claims == null) {
            claims = new HashMap<>();
        }
        claims.put("username", username);
        return createToken(claims, username);
    }
    
    /**
     * 创建Token
     */
    private static String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + EXPIRATION);
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(getSecretKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    
    /**
     * 解析Token
     */
    public static Claims parseToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSecretKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            throw new RuntimeException("Token解析失败", e);
        }
    }
    
    /**
     * 从Token中获取用户名
     */
    public static String getUsernameFromToken(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.getSubject();
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 验证Token是否有效
     */
    public static boolean validateToken(String token) {
        try {
            Claims claims = parseToken(token);
            Date expiration = claims.getExpiration();
            return expiration.after(new Date());
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 验证Token是否有效（指定用户名）
     */
    public static boolean validateToken(String token, String username) {
        try {
            String tokenUsername = getUsernameFromToken(token);
            return username.equals(tokenUsername) && validateToken(token);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 判断Token是否过期
     */
    public static boolean isTokenExpired(String token) {
        try {
            Claims claims = parseToken(token);
            Date expiration = claims.getExpiration();
            return expiration.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }
    
    /**
     * 刷新Token
     */
    public static String refreshToken(String token) {
        try {
            Claims claims = parseToken(token);
            String username = claims.getSubject();
            return generateToken(username);
        } catch (Exception e) {
            throw new RuntimeException("Token刷新失败", e);
        }
    }
}
