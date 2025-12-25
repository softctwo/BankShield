package com.bankshield.common.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.annotation.PostConstruct;
import java.util.*;

@Component
@Slf4j
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @PostConstruct
    public void validateSecret() {
        if (secret == null || secret.length() < 32) {
            throw new IllegalStateException(
                "JWT secret必须配置且长度不少于32个字符。请通过 'jwt.secret' 配置项设置JWT密钥。"
            );
        }
    }
    
    @Value("${jwt.expiration:86400}")
    private Long expiration;
    
    @Value("${jwt.refresh-expiration:604800}")
    private Long refreshExpiration;
    
    /**
     * 从token中获取用户名
     */
    public String getUsernameFromToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims.getSubject();
        } catch (Exception e) {
            log.error("解析token失败: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 从token中获取用户ID
     */
    public Long getUserIdFromToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return Long.parseLong(claims.get("userId").toString());
        } catch (Exception e) {
            log.error("从token获取用户ID失败: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 从token中获取权限
     */
    public List<String> getAuthoritiesFromToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            Object authorities = claims.get("authorities");
            if (authorities instanceof List) {
                return (List<String>) authorities;
            }
            return new ArrayList<>();
        } catch (Exception e) {
            log.error("从token获取权限失败: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * 生成token
     */
    public String generateToken(Long userId, String username, List<String> authorities) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("authorities", authorities);
        return createToken(claims, username, expiration);
    }
    
    /**
     * 生成refresh token
     */
    public String generateRefreshToken(Long userId, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        return createToken(claims, username, refreshExpiration);
    }
    
    /**
     * 创建token
     */
    private String createToken(Map<String, Object> claims, String subject, Long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration * 1000);
        
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key)
                .compact();
    }
    
    /**
     * 验证token是否有效
     */
    public boolean validateToken(String token) {
        try {
            getClaimsFromToken(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.error("Token已过期: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("不支持的Token: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("Token格式错误: {}", e.getMessage());
        } catch (SignatureException e) {
            log.error("Token签名错误: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("Token参数错误: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Token验证失败: {}", e.getMessage());
        }
        return false;
    }
    
    /**
     * 判断token是否过期
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            Date expiration = claims.getExpiration();
            return expiration.before(new Date());
        } catch (Exception e) {
            log.error("判断token是否过期失败: {}", e.getMessage());
            return true;
        }
    }
    
    /**
     * 刷新token
     */
    public String refreshToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            Date now = new Date();
            Date expiryDate = new Date(now.getTime() + expiration * 1000);
            
            SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
            
            return Jwts.builder()
                    .setClaims(claims)
                    .setIssuedAt(now)
                    .setExpiration(expiryDate)
                    .signWith(key)
                    .compact();
        } catch (Exception e) {
            log.error("刷新token失败: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 从token获取Claims
     */
    private Claims getClaimsFromToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
        return Jwts.parser()
                .setSigningKey(key)
                .parseClaimsJws(token)
                .getBody();
    }
}