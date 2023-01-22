package co.hanbin.mybooks.config.security;

import java.io.IOException;
import java.security.Key;
import java.util.Date;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import co.hanbin.mybooks.member.entity.Member;
import co.hanbin.mybooks.member.repository.PrincipalDetails;
import co.hanbin.mybooks.member.service.MemberService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter{
	
	private MemberService memberService;
	@Value("${jwt.access-token-expire}")
    private long VALID_TIME;
	@Value("${jwt.access-token-secret}")
	private String SECRET_KEY;

    public JwtAuthenticationFilter(AuthenticationManager authenticationManager, MemberService memberService) {
        super(authenticationManager);
		this.memberService = memberService;
		
		setFilterProcessesUrl("/login");
	}

	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
		
		log.info("--JWT AUTHENTICATION FILTER--");
		
		try {
			
			Member creds = new ObjectMapper().readValue(request.getInputStream(), Member.class);
			
			return getAuthenticationManager().authenticate(
					new UsernamePasswordAuthenticationToken(
							creds.getUsername(), 
							creds.getPassword(),
							null
							));
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		} 
		
	}

	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
			Authentication authResult) throws IOException, ServletException {
		
		String username = ((PrincipalDetails)authResult.getPrincipal()).getUsername();
		UserDetails userDetails = memberService.loadUserByUsername(username);
		
		String jwtToken = Jwts.builder()
								.setSubject(userDetails.getUsername())
								.setExpiration(new Date(System.currentTimeMillis() + VALID_TIME))
								.signWith(getSecretKey(), SignatureAlgorithm.HS256)
								.compact();
		
		response.addHeader("token", jwtToken);
		response.addHeader("username", userDetails.getUsername());
	}

	private Key getSecretKey() {
		byte[] KeyBytes = SECRET_KEY.getBytes();
		return Keys.hmacShaKeyFor(KeyBytes);
	}
	
}