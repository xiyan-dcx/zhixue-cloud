package com.zhixue.common.utils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtils {
    @Value("${zhixue.jwt.secret}") private String secret;
    @Value("${zhixue.jwt.expire}") private long expire;

    private SecretKey getKey() { return Keys.hmacShaKeyFor(
            secret.getBytes(StandardCharsets.UTF_8)); }

    public String createToken(String username, Integer version) {
        Date now = new Date();
        return Jwts.builder().subject(username).claim("version", version == null ? 0 : version).issuedAt(now)
                .expiration(new Date(now.getTime() + expire * 1000))
                .signWith(getKey()).compact();
    }

    public String parseUsername(String token) {
        try {
            return Jwts.parser().verifyWith(getKey()).build()
                    .parseSignedClaims(token).getPayload().getSubject();
        } catch (Exception e) { return null; }
    }

    public boolean matchesVersion(String token, Integer version) {
        try {
            Integer tokenVersion = Jwts.parser().verifyWith(getKey()).build().parseSignedClaims(token)
                    .getPayload().get("version", Integer.class);
            return tokenVersion != null && tokenVersion.equals(version == null ? 0 : version);
        } catch (Exception e) { return false; }
    }
}
