package co.hanbin.mybooks.member.component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import co.hanbin.mybooks.member.entity.Member;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {
    @Value("${jwt.access-token-secret}")
    public static String ACCESS_TOKEN_SECRET;

    @Value("${jwt.access-token-expire}")
    public static long ACCESS_TOKEN_EXPIRE;

    @Value("${jwt.refresh-token-secret}")
    public static String REFRESH_TOKEN_SECRET;

    @Value("${jwt.refresh-token-expire}")
    public static long REFRESH_TOKEN_EXPIRE;

    final static public String ACCESS_TOKEN_NAME = "accessToken";
    final static public String REFRESH_TOKEN_NAME = "refreshToken";

    private Key getSigningKey(String secretKey) {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public Claims extractAllClaims(String token, String secretKey) throws ExpiredJwtException {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey(secretKey))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String getUsername(String token, String secretKey) {
        return extractAllClaims(token, secretKey).get("username", String.class);
    }

    public Boolean isTokenExpired(String token, String secretKey) {
        final Date expiration = extractAllClaims(token, secretKey).getExpiration();
        return expiration.before(new Date());
    }

    public String generateToken(Member member) {
        return doGenerateToken(member.getUsername(), ACCESS_TOKEN_SECRET, ACCESS_TOKEN_EXPIRE);
    }

    public String generateRefreshToken(Member member) {
        return doGenerateToken(member.getUsername(), REFRESH_TOKEN_SECRET, REFRESH_TOKEN_EXPIRE);
    }

    public String doGenerateToken(String username, String secretKey, long expireTime) {

        Claims claims = Jwts.claims();
        claims.put("username", username);

        String jwt = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expireTime))
                .signWith(getSigningKey(secretKey), SignatureAlgorithm.HS256)
                .compact();

        return jwt;
    }

    public Boolean validateToken(String token, String secretKey, UserDetails userDetails) {
        final String username = getUsername(token, secretKey);

        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token, secretKey));
    }
}
