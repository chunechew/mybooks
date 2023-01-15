package co.hanbin.mybooks.security;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import co.hanbin.mybooks.error.ApiException;
import co.hanbin.mybooks.error.ExceptionEnum;
import co.hanbin.mybooks.user.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;

@Component
// @Service
@RequiredArgsConstructor
public class JwtTokenUtil {
	
	private final JwtUserServiceForFilter jwtUserServiceForFilter;
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Value("${jwt.access-token-secret}")
	private final String ACCESS_TOKEN_SECRET;

	@Value("${jwt.refresh-token-secret}")
	private final String REFRESH_TOKEN_SECRET;

	//retrieve username from jwt token
	public String getUsernameFromToken(String token, String mode) {
		return getClaimFromToken(token, mode, Claims::getSubject);
	}

	//retrieve expiration date from jwt token
	public Date getExpirationDateFromToken(String token, String mode) {
		return getClaimFromToken(token, mode, Claims::getExpiration);
	}

	public <T> T getClaimFromToken(String token, String mode, Function<Claims, T> claimsResolver) throws ExpiredJwtException {
		final Claims claims = getAllClaimsFromToken(token, mode);
		return claimsResolver.apply(claims);
	}
    //for retrieveing any information from token we will need the secret key
	private Claims getAllClaimsFromToken(String token, String mode) {
		String secret = "";

		if(mode.equals("access")) {
			secret = ACCESS_TOKEN_SECRET;
		} else {
			secret = REFRESH_TOKEN_SECRET;
		}

        try {
			return Jwts.parserBuilder().setSigningKey(secret.getBytes()).build().parseClaimsJws(token).getBody();
		// } catch (ExpiredJwtException e) {
        //     return e.getClaims();
		} catch(Exception e) {
			e.printStackTrace();
			throw new ApiException(ExceptionEnum.JWT_ERROR);
		}
	}

	//check if the token has expired
	public Boolean isTokenExpired(String token, String mode) {
		try {
			final Date expiration = getExpirationDateFromToken(token, mode);
			return expiration.before(new Date());
		} catch(Exception e) {
			e.printStackTrace();

			if(mode.equals("access")) {
				throw new ApiException(ExceptionEnum.ACCESS_TOKEN_EXPIRED);
			} else {
				throw new ApiException(ExceptionEnum.REFRESH_TOKEN_EXPIRED);
			}
		} 
	}

	//validate token
	public Boolean validateToken(String token, String mode, UserDetails userDetails) {
		final String username = getUsernameFromToken(token, mode);
		return (username.equals(userDetails.getUsername()) && !isTokenExpired(token, mode));
	}

	public Boolean validateToken(String token, String mode, User user) {
		final String username = getUsernameFromToken(token, mode);
		return (username.equals(user.getUserId()) && !isTokenExpired(token, mode));
	}

	public Long getExpiration(String token, String mode) {
		String secret = "";

		if(mode.equals("access")) {
			secret = ACCESS_TOKEN_SECRET;
		} else {
			secret = REFRESH_TOKEN_SECRET;
		}

		logger.info("secret: " + secret);

        // accessToken 남은 유효시간
        Date expiration = Jwts.parserBuilder().setSigningKey(Keys.hmacShaKeyFor(secret.getBytes())).build().parseClaimsJws(token).getBody().getExpiration();
        // 현재 시간
        Long now = new Date().getTime();
        return (expiration.getTime() - now);
    }

	public User refreshTokens(String refreshToken) throws ParseException {
        final String mode = "refresh";
        boolean isExpired = isTokenExpired(refreshToken, mode);

        if(isExpired) {
            throw new ApiException(ExceptionEnum.INVALID_REFRESH_TOKEN);
        }

        String userId = getUsernameFromToken(refreshToken, mode);

        logger.info("userId: " + userId + ", refreshToken: " + refreshToken);

        User user = jwtUserServiceForFilter.getUserByUserId(userId);
        user = jwtUserServiceForFilter.createTokens(user);

		return user;
	}

	public PasswordEncoder passwordEncoder() {
		Map<String, PasswordEncoder> encoders = new HashMap<>();
        encoders.put("SSHA-512", new PasswordEncoderWithSalt(ACCESS_TOKEN_SECRET));
        return new DelegatingPasswordEncoder("SSHA-512", encoders);
	}
}