package co.handk.common.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Date;

public class JwtUtil {

    private static final String SECRET = "handk-system-secret-key-handk-system-secret-key";

    private static final SecretKey KEY = Keys.hmacShaKeyFor(SECRET.getBytes());

    // 生成token
    public static String generateToken(Long userId, String username) {

        return Jwts.builder()
                .claim("userId", userId)
                .claim("username", username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 7 * 24 * 3600 * 1000))
                .signWith(KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    // 解析token
    public static Claims parseToken(String token) {

        return Jwts.parserBuilder()
                .setSigningKey(KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}