package co.hanbin.mybooks.config.security;

import java.io.IOException;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import co.hanbin.mybooks.member.component.JwtUtil;
import co.hanbin.mybooks.member.service.MemberService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
	@Value("${jwt.access-token-secret}")
	private String ACCESS_TOKEN_SECRET;

	@Value("${jwt.refresh-token-secret}")
    public String REFRESH_TOKEN_SECRET;

	@Autowired
	private SecurityConfig securityConfig;

	@Autowired
    private MemberService memberService;

	@Autowired
	private JwtUtil jwtUtil;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		String uri = request.getRequestURI();
		if(uri.contains(securityConfig.API_DIRECTORY) && !uri.contains(securityConfig.API_DIRECTORY_EXCEPTION)) {
			log.debug("doFilterInternal - Checking ACCESS_TOKEN_SECRET: " + ACCESS_TOKEN_SECRET);
			log.debug("doFilterInternal - try");
			
			try {
				// Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

				// if(authentication == null) {
				// 	log.debug("The authentication is null.");
				// 	throw new Exception();
				// }

				// boolean isAuthenticated = authentication.isAuthenticated();

				// if(!isAuthenticated) {
				// 	log.debug("You are not authenticated.");
				// 	throw new Exception();
				// }

				String tokenHeader = request.getHeader("Authorization");
				String jwtToken = null;
				
				if(StringUtils.hasText(tokenHeader) && tokenHeader.startsWith("Bearer ")) {
					log.debug("doFilterInternal - The request header has the 'Authorization' field that starts with 'Bearer '.");
					jwtToken = tokenHeader.replace("Bearer ", "");
				} else {
					log.debug("doFilterInternal - The request header does not have the 'Authorization' field or it does not start with 'Bearer '.");
				}

				log.debug("doFilterInternal - jwtToken: " + jwtToken);

				Map<String, Object> isValidAndJws = jwtUtil.isValid(jwtToken, ACCESS_TOKEN_SECRET, "AT:");

				@SuppressWarnings("unchecked")
				Jws<Claims> jws = (Jws<Claims>)isValidAndJws.get("jws");
				Boolean isJwtValid = (Boolean)isValidAndJws.get("isValid");

				if(isJwtValid != null && isJwtValid == true) {
					log.debug("doFilterInternal - The JWT is vaild.");
					Authentication auth = memberService.getAuth((String)(jws.getBody().get("username")));
					if(auth != null) {
						SecurityContextHolder.getContext().setAuthentication(auth);
					} else {
						throw new JwtException("No such account!");
					}
				} else {
					if(isJwtValid != null && isJwtValid == false) {
						throw new IllegalArgumentException();
					} else {
						String refreshToken = request.getHeader("Refresh-token");

						if(refreshToken != null) {
							Map<String, Object> isRtValidAndJws = jwtUtil.isValid(refreshToken, REFRESH_TOKEN_SECRET, "RT:");
							
							@SuppressWarnings("unchecked")
							Jws<Claims> rtJws = (Jws<Claims>)isRtValidAndJws.get("jws");
							Boolean isRtValid = (Boolean)isRtValidAndJws.get("isValid");

							if(isRtValid != null && isRtValid == true) {
								Map<String, Object> result = memberService.refreshToken(refreshToken);
								String status = (String)(result.get("status"));

								if(status.contentEquals("success")) {
									Authentication newAuth = memberService.getAuth((String)rtJws.getBody().get("username"));

									if(newAuth != null) {
										SecurityContextHolder.getContext().setAuthentication(newAuth);

										HttpSession session = request.getSession();

										@SuppressWarnings("unchecked")
										Map<String, Object> data = (Map<String, Object>)result.get("data");

										session.setAttribute("newTokens", data); // 새 토큰을 세션에 기억(JsonResponse에서 참조)
									} else {
										new SecurityContextLogoutHandler().logout(request, response, null);
										throw new JwtException("No such account!");
									}
								}
							}
						}

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