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

		// /api/ 이고 /api/member/ 가 아닐 때만 필터링 적용
		String uri = request.getRequestURI();
		if(uri.contains(securityConfig.API_DIRECTORY) && !uri.contains(securityConfig.API_DIRECTORY_EXCEPTION)) {
			log.debug("doFilterInternal - Checking ACCESS_TOKEN_SECRET: " + ACCESS_TOKEN_SECRET);
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

				Map<String, Object> isValidAndJws = jwtUtil.isValid(jwtToken, ACCESS_TOKEN_SECRET, "AT:");

				@SuppressWarnings("unchecked")
				Jws<Claims> jws = (Jws<Claims>)isValidAndJws.get("jws");
				Boolean isJwtValid = (Boolean)isValidAndJws.get("isValid");

				if(isJwtValid != null && isJwtValid == true) { // 유효한 토큰
					log.debug("doFilterInternal - The JWT is vaild.");
					Authentication auth = memberService.getAuth((String)(jws.getBody().get("username")));
					if(auth != null) {
						SecurityContextHolder.getContext().setAuthentication(auth); // Spring Security의 세션에 로그인 처리(권한 체크 때문에 필요함)
					} else {
						new SecurityContextLogoutHandler().logout(request, response, null);
						throw new JwtException("No such account!");
					}
				} else if(isJwtValid != null && isJwtValid == false) { // 계정 없음
					throw new JwtException("No such account!");
				} else { // Access token이 만료됨
					String refreshToken = request.getHeader("Refresh-token");

					if(refreshToken != null) { // Refresh token으로 JWT 재발급
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
									SecurityContextHolder.getContext().setAuthentication(newAuth); // Spring Security의 세션에 로그인 처리(권한 체크 때문에 필요함)

									HttpSession session = request.getSession();

									@SuppressWarnings("unchecked")
									Map<String, Object> newTokens = (Map<String, Object>)result.get("newTokens");

									session.setAttribute("newTokens", newTokens); // 새 토큰을 Spring Framework 세션에 임시 기억(JsonResponse에서 참조)
								} else { // newAuth 가 null인 특수한 상황
									new SecurityContextLogoutHandler().logout(request, response, null);
									throw new JwtException("No such account!");
								}
							} else {
								new SecurityContextLogoutHandler().logout(request, response, null);
								throw new ExpiredJwtException(null, null, null);
							}
						}
					} else {
						new SecurityContextLogoutHandler().logout(request, response, null);
						throw new ExpiredJwtException(null, null, null);
					}	
				}
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