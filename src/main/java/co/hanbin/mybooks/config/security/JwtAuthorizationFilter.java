package co.hanbin.mybooks.config.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import co.hanbin.mybooks.member.repository.PrincipalDetails;
import co.hanbin.mybooks.member.service.MemberService;
import co.hanbin.mybooks.member.service.SaltUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JwtAuthorizationFilter extends /*BasicAuthenticationFilter*/OncePerRequestFilter {

	@Value("${jwt.access-token-secret}")
	private String SECRET_KEY;

	@Value("${password-salt}")
    private String PASSWORD_SALT;

	@Autowired
    private SaltUtil saltUtil;

	@Autowired
	private SecurityConfig securityConfig;
	
	@Autowired
	private MemberService memberService;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		String uri = request.getRequestURI();
		if(uri.contains(securityConfig.API_DIRECTORY) && !uri.contains(securityConfig.API_DIRECTORY_EXCEPTION)) {
			log.debug("doFilterInternal - Checking SECRET_KEY: " + SECRET_KEY);
			log.debug("doFilterInternal - try");
			
			try {
				String tokenHeader = request.getHeader("Authorization");
				String jwtToken = null;
				
				if(StringUtils.hasText(tokenHeader) && tokenHeader.startsWith("Bearer ")) {
					log.debug("doFilterInternal - The request header has the 'Authorization' field that starts with 'Bearer '.");
					jwtToken = tokenHeader.replace("Bearer ", "");
				} else {
					log.debug("doFilterInternal - The request header does not have the 'Authorization' field or it does not start with 'Bearer '.");
				}

				log.debug("doFilterInternal - jwtToken: " + jwtToken);

				Jws<Claims> jws = getJws(jwtToken, SECRET_KEY);
				Boolean isJwtValid = isValid(jws);
			
				if(jwtToken != null && isJwtValid == true) {
					log.debug("doFilterInternal - The JWT is vaild.");
					Authentication auth = getAuth((String)(jws.getBody().get("username")));
					if(auth != null) {
						SecurityContextHolder.getContext().setAuthentication(auth);
					} else {
						throw new JwtException("No such account!");
					}
				} else {
					if(isJwtValid == false) {
						throw new IllegalArgumentException();
					} else {
						throw new ExpiredJwtException(null, null, null);
					}
				}
			} catch(IllegalArgumentException e) {
				log.error("doFilterInternal - IllegalArgumentException");
				e.printStackTrace();
				request.setAttribute("exception", ErrorCodes.INVALID_TOKEN);
			} catch(ExpiredJwtException e) {
				log.error("doFilterInternal - ExpiredJwtException");
				e.printStackTrace();
				request.setAttribute("exception", ErrorCodes.EXPIRED_TOKEN);
			} catch(JwtException e) {
				log.error("doFilterInternal - No such account!");
				e.printStackTrace();
				request.setAttribute("exception", ErrorCodes.NO_SUCH_ACCOUNT);
			} catch(Exception e) {
				log.error("doFilterInternal - Exception");
				e.printStackTrace();
				request.setAttribute("exception", ErrorCodes.NON_LOGIN);
			}
		}
		
		chain.doFilter(request, response);
	}

	private Authentication getAuth(String username) {
		log.debug("getAuth - process");
		Authentication auth = null;
		PrincipalDetails user = (PrincipalDetails)memberService.loadUserByUsername(username);

		log.debug("getAuth > user: " + user.getUsername() + ", " + user.getPassword() + ", " + user.getAuthorities());
		
		if(user != null && user.getUsername() != null) {
			final String ENCODED_PASSWORD = saltUtil.encodePassword(PASSWORD_SALT, user.getPassword());
			auth = new UsernamePasswordAuthenticationToken(user.getUsername(), ENCODED_PASSWORD, user.getAuthorities());
		}
		
		return auth;
	}

	private Jws<Claims> getJws(String jwtToken, String secretKey) {
		Jws<Claims> jws = Jwts.parserBuilder()
							.setSigningKey(getSecretKey(secretKey))
							.build()
							.parseClaimsJws(jwtToken);
		
		return jws;
}
	
	private Boolean isValid(Jws<Claims> jws) {
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
	
	private Key getSecretKey(String secretKey) {
		byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
		return Keys.hmacShaKeyFor(keyBytes);
	}
}