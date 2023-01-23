package co.hanbin.mybooks.member.component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import co.hanbin.mybooks.member.repository.PrincipalDetails;
import co.hanbin.mybooks.member.service.MemberService;
import co.hanbin.mybooks.member.service.SaltUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JwtUtil {
    @Value("${password-salt}")
    private String PASSWORD_SALT;

    @Autowired
    private SaltUtil saltUtil;

    @Autowired
	private MemberService memberService;

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

    public Authentication getAuth(String username) {
		log.debug("getAuth - process");
		Authentication auth = null;
		PrincipalDetails user = (PrincipalDetails)memberService.loadUserByUsername(username);

		log.debug("getAuth > user: " + user.getUsername() + ", " + user.getPassword() + ", " + user.getAuthorities());
		
		if(user != null && user.getUsername() != null) {
			// final String ENCODED_PASSWORD = saltUtil.encodePassword(PASSWORD_SALT, user.getPassword());
			auth = new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword(), user.getAuthorities());
		}
		
		return auth;
	}

	public Jws<Claims> getJws(String jwtToken, String secretKey) {
		Jws<Claims> jws = Jwts.parserBuilder()
							.setSigningKey(getSecretKey(secretKey))
							.build()
							.parseClaimsJws(jwtToken);
		
		return jws;
	}
	
	public Boolean isValid(Jws<Claims> jws) {
		Boolean ret = true;
		
		if(jws == null ||
			jws.getBody().get("username") == null ||
			jws.getBody().get("role") == null) {
			ret = false;
		} else if(jws.getBody().getExpiration().before(new Date())) {
			ret = null;
		}

		return ret;
	}
	
	public Key getSecretKey(String secretKey) {
		byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
		return Keys.hmacShaKeyFor(keyBytes);
	}
}
