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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.util.StringUtils;

import co.hanbin.mybooks.member.repository.PrincipalDetails;
import co.hanbin.mybooks.member.service.MemberService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

public class JwtAuthorizationFilter extends BasicAuthenticationFilter{

	@Value("${jwt.access-token-expire}")
	private String SECRET_KEY;
	
	private final MemberService memberService;
	
	public JwtAuthorizationFilter(AuthenticationManager authenticationManager
								, MemberService memberService) {
		
		super(authenticationManager);
		this.memberService = memberService;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		
		try {
			String tokenHeader = request.getHeader("Authorization");
			String jwtToken = null;
			
			if(StringUtils.hasText(tokenHeader) && tokenHeader.startsWith("Bearer")) {
				jwtToken = tokenHeader.replace("Bearer ", "");
			}
		
			if(jwtToken != null && isValid(jwtToken)) {
				SecurityContextHolder.getContext().setAuthentication(getAuth(jwtToken));
			}
			
		}catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		chain.doFilter(request, response);
	}

	private Authentication getAuth(String jwtToken) {
		PrincipalDetails user = (PrincipalDetails)memberService.loadUserByUsername(getEmail(jwtToken));
		return new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword(), user.getAuthorities());
	}
	
	private String getEmail(String jwtToken) {
		return Jwts.parserBuilder()
					.setSigningKey(getSecretKey())
					.build()
					.parseClaimsJws(jwtToken).getBody()
					.getSubject();
	}
	
	private boolean isValid(String jwtToken) {
		boolean ret = true; 
		
		Jws<Claims> jws = null;
		
		try {
			jws = Jwts.parserBuilder()
				.setSigningKey(getSecretKey())
				.build()
				.parseClaimsJws(jwtToken);
		
			if( jws == null ||
				jws.getBody().getSubject() == null ||
				jws.getBody().getExpiration().before(new Date())) {
				ret = false;
			}
			
		}catch (Exception e) {
			ret = false;
		}
		return ret;
	}
	
	private Key getSecretKey() {
		byte[] keyBytes = SECRET_KEY.getBytes();
		return Keys.hmacShaKeyFor(keyBytes);
	}
}