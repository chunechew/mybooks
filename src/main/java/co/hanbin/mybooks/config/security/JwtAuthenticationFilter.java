package co.hanbin.mybooks.config.security;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import co.hanbin.mybooks.member.component.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
	@Value("${jwt.access-token-secret}")
	private String SECRET_KEY;

	@Autowired
	private SecurityConfig securityConfig;

	@Autowired
	private JwtUtil jwtUtil;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		String uri = request.getRequestURI();
		if(uri.contains(securityConfig.API_DIRECTORY) && !uri.contains(securityConfig.API_DIRECTORY_EXCEPTION)) {
			log.debug("doFilterInternal - Checking SECRET_KEY: " + SECRET_KEY);
			log.debug("doFilterInternal - try");
			
			try {
				Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

				if(authentication == null) {
					log.debug("The authentication is null.");
					throw new Exception();
				}

				boolean isAuthenticated = authentication.isAuthenticated();

				if(!isAuthenticated) {
					log.debug("You are not authenticated.");
					throw new Exception();
				}

				String tokenHeader = request.getHeader("Authorization");
				String jwtToken = null;
				
				if(StringUtils.hasText(tokenHeader) && tokenHeader.startsWith("Bearer ")) {
					log.debug("doFilterInternal - The request header has the 'Authorization' field that starts with 'Bearer '.");
					jwtToken = tokenHeader.replace("Bearer ", "");
				} else {
					log.debug("doFilterInternal - The request header does not have the 'Authorization' field or it does not start with 'Bearer '.");
				}

				log.debug("doFilterInternal - jwtToken: " + jwtToken);

				Jws<Claims> jws = jwtUtil.getJws(jwtToken, SECRET_KEY);
				Boolean isJwtValid = jwtUtil.isValid(jws);
			
				if(isJwtValid != null && isJwtValid == true) {
					log.debug("doFilterInternal - The JWT is vaild.");
					Authentication auth = jwtUtil.getAuth((String)(jws.getBody().get("username")));
					if(auth != null) {
						SecurityContextHolder.getContext().setAuthentication(auth);
					} else {
						throw new JwtException("No such account!");
					}
				} else {
					if(isJwtValid != null && isJwtValid == false) {
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
}