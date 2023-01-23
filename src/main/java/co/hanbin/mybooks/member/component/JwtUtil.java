package co.hanbin.mybooks.member.component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import co.hanbin.mybooks.db.service.RedisUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {
    @Autowired
    private RedisUtil redisUtil;

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

    public String doGenerateToken(String username, String role, String secretKey, long expireTime) {
        Map<String, String> claim = new HashMap<>();
        claim.put("username", username);
        claim.put("role", role);

        String jwt = Jwts.builder()
                .setClaims(claim)
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

    public Jws<Claims> getJws(String jwtToken, String secretKey) {
		Jws<Claims> jws = Jwts.parserBuilder()
							.setSigningKey(getSecretKey(secretKey))
							.build()
							.parseClaimsJws(jwtToken);
		
		return jws;
	}
	
	public Map<String, Object> isValid(String jwtToken, String secretKey, String prefix) {
        Jws<Claims> jws = getJws(jwtToken, secretKey);
        Boolean isValid = true;
        
        if(jws == null ||
			jws.getBody().get("username") == null ||
			jws.getBody().get("role") == null) {
            isValid = false;
		} else if(jws.getBody().getExpiration().before(new Date())) {
			isValid = null;
		} else {
            String cachedToken = (String)(redisUtil.getData(prefix + jws.getBody().get("username")));
            if(!cachedToken.contentEquals(jwtToken)) {
                isValid = null;
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("isValid", isValid);
        result.put("jws", jws);

		return result;
	}
	
	public Key getSecretKey(String secretKey) {
		byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
		return Keys.hmacShaKeyFor(keyBytes);
	}
}
