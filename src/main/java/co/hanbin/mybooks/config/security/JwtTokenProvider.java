package co.hanbin.mybooks.config.security;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import co.hanbin.mybooks.member.service.MemberService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JwtTokenProvider {
	@Autowired
	private MemberService memberService;

    @Value("${jwt.access-token-expire}")
    private long accessTokenExpire;
	
	@Value("${jwt.access-token-secret}")
	private String accessTokenSecret;
	
	private Key getSecretKey(String secretKey) {
		byte[] KeyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
		return Keys.hmacShaKeyFor(KeyBytes);
	}

	private String getUsername(String jwtToken) {
		return Jwts.parserBuilder()
			.setSigningKey(getSecretKey(accessTokenSecret))
			.build()
			.parseClaimsJws(jwtToken)
			.getBody()
			.getSubject();
	}
	
	public boolean validateToken(String jwtToken) {
		try {
			log.info("validate..");
			Jws<Claims>  claims = Jwts.parserBuilder()
										.setSigningKey(getSecretKey(accessTokenSecret))
										.build()
										.parseClaimsJws(jwtToken);
			log.info("{}",claims.getBody().getExpiration());
			return !claims.getBody().getExpiration().before(new Date());
		}catch(Exception e) {
			return false;
		}
	}
	
	public Authentication getAuthentication(String jwtToken) {
		UserDetails userDetails = memberService.loadUserByUsername(getUsername(jwtToken));
		log.info("PASSWORD : {}",userDetails.getPassword());
		return new UsernamePasswordAuthenticationToken(userDetails, userDetails.getPassword(), userDetails.getAuthorities());
	}
	
	
	public String generateToken(String username) {
		return Jwts.builder()
					.setSubject(username)
					.setIssuedAt(new Date())
					.setExpiration(new Date(new Date().getTime() + accessTokenExpire))
					.signWith(getSecretKey(accessTokenSecret), SignatureAlgorithm.HS256)
					.compact();
	}
}