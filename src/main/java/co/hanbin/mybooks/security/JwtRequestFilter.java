package co.hanbin.mybooks.security;

import java.io.IOException;
import java.text.ParseException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import co.hanbin.mybooks.user.entity.User;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtRequestFilter extends OncePerRequestFilter {

	@Autowired
	private JwtUserServiceForFilter userService;

	@Autowired
	private JwtTokenUtil jwtTokenUtil;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {

		final String accessTokenHeader = request.getHeader("Authorization");
		
		String userId = null;
		String accessToken = null;
		UserDetails userDetails = null;
		// JWT Token is in the form "Bearer token". Remove Bearer word and get
		// only the Token
		if (accessTokenHeader != null && accessTokenHeader.startsWith("Bearer ")) {
			accessToken = accessTokenHeader.substring(7);

			try {
				userId = jwtTokenUtil.getUsernameFromToken(accessToken, "access");
			} catch (IllegalArgumentException e) {
				logger.debug("Unable to get JWT Access Token");
				logger.error(e.getStackTrace());
			} catch (ExpiredJwtException e) {
				logger.debug("JWT Access Token has expired");
				
				final String refreshToken = request.getHeader("Refresh-Token");

				if(refreshToken != null) {
					try {
						User users = jwtTokenUtil.refreshTokens(refreshToken);

						response.addHeader("New-Access-Token", users.getAccessToken());
						response.addHeader("New-Refresh-Token", users.getRefreshToken());
						response.addHeader("New-Access-Token-Expire", users.getAccessTokenExpire());
						response.addHeader("New-Refresh-Token-Expire", users.getRefreshTokenExpire());
					} catch (ParseException e1) {
						// TODO Auto-generated catch block
						logger.error(e.getStackTrace());
						logger.error(e1.getStackTrace());
					}
				} else {
					logger.error(e.getStackTrace());
				}
			} catch (Exception e) {
				logger.error(e.getStackTrace());
			}
		} else {
			logger.debug("JWT Token does not begin with Bearer String");
		}

		// Once we get the token validate it.
		if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
			User user = userService.getUserByUserId(userId);
			userDetails = userService.getUserDetailsByUser(user);

			// if token is valid configure Spring Security to manually set
			// authentication
			if (jwtTokenUtil.validateToken(accessToken, "access", userDetails)) {

				UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
					userDetails, null, userDetails.getAuthorities());
				usernamePasswordAuthenticationToken
					.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
				// After setting the Authentication in the context, we specify
				// that the current user is authenticated. So it passes the
				// Spring Security Configurations successfully.
				SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
			}
		}
		chain.doFilter(request, response);
	}
}
