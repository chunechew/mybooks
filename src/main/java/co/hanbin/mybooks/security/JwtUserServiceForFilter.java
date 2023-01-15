package co.hanbin.mybooks.security;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import co.hanbin.mybooks.user.entity.User;
import co.hanbin.mybooks.user.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JwtUserServiceForFilter {
    @Autowired
	private final UserRepository userRepository;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private final String ROLE_PREFIX = "ROLE_";

	@Value("${jwt.access-token-expire}")
    private long ACCESS_TOKEN_VALIDITY;

	@Value("${jwt.refresh-token-expire}")
    private long REFRESH_TOKEN_VALIDITY;

	@Value("${jwt.access-token-secret}")
	private String ACCESS_TOKEN_SECRET;

	@Value("${jwt.refresh-token-secret}")
	private String REFRESH_TOKEN_SECRET;
	
    public User getUserByUserId(String userId) throws UsernameNotFoundException {
		User user = userRepository.findByUserId(userId);

		if(user != null) {
			user.setAuths(getUserAuths(user));
			user.setRoles(getUserRoles(user));
		}

		return user;
	}

    public List<String> getUserAuths(User user) {
		List<String> auths = new ArrayList<>();
		Integer userCls = user.getUserCls();

		if(userCls == 0 || userCls == 1) {
			String authUser = "USER";
			auths.add(authUser);
		}

		if(userCls == 0) {
			String authAdmin = "ADMIN";
			auths.add(authAdmin);
		}

		return auths;
	}

	public Collection<GrantedAuthority> getUserRoles(User user) {
		Collection<GrantedAuthority> roles = new ArrayList<>();
		Integer userCls = user.getUserCls();

		if(userCls == 0 || userCls == 1) {
			SimpleGrantedAuthority roleUser = new SimpleGrantedAuthority(ROLE_PREFIX + "USER");
			roles.add(roleUser);
		}

		if(userCls == 0) {
			SimpleGrantedAuthority roleAdmin = new SimpleGrantedAuthority(ROLE_PREFIX + "ADMIN");
			roles.add(roleAdmin);
		}

		return roles;
	}

    public UserDetails getUserDetailsByUser(User user) {
		return new org.springframework.security.core.userdetails.User(user.getUserId(), user.getUserPw(), user.getRoles());
	}

	public User createTokens(User user) throws ParseException {
        final UserDetails userDetails = getUserDetailsByUser(user);

        final Map<String, String> accessTokenMap = generateToken(user, userDetails, "access");
        final Map<String, String> refreshTokenMap = generateToken(user, userDetails, "refresh");

        final String accessToken = accessTokenMap.get("token");
        final String accessTokenExpire = accessTokenMap.get("expire");

        final String refreshToken = refreshTokenMap.get("token");
        final String refreshTokenExpire = refreshTokenMap.get("expire");

		logger.info("accessTokenExpire: " + accessTokenExpire);

        user.setAccessToken(accessToken);
        user.setAccessTokenExpire(accessTokenExpire);
        user.setRefreshToken(refreshToken);
        user.setRefreshTokenExpire(refreshTokenExpire);

        return user;
    }

	//generate token for user
	public Map<String, String> generateToken(User user, UserDetails userDetails, String mode) {
		Map<String, Object> claims = new HashMap<>();
		claims.put("iss", "bookshelf.hanbin.co");
		claims.put("userCls", user.getUserCls());

		return doGenerateToken(claims, userDetails.getUsername(), mode);
	}

	//while creating the token -
	//1. Define  claims of the token, like Issuer, Expiration, Subject, and the ID
	//2. Sign the JWT using the HS512 algorithm and secret key.
	//3. According to JWS Compact Serialization(https://tools.ietf.org/html/draft-ietf-jose-json-web-signature-41#section-3.1)
	//   compaction of the JWT to a URL-safe string 
	private Map<String, String> doGenerateToken(Map<String, Object> claims, String subject, String mode) {
		long expire = 0L;
		String secret = "";

		if(mode.equals("access")) {
			expire = ACCESS_TOKEN_VALIDITY;
			secret = ACCESS_TOKEN_SECRET;
		} else {
			expire = REFRESH_TOKEN_VALIDITY;
			secret = REFRESH_TOKEN_SECRET;
		}

		logger.info("expire: " + expire + ", secret:" + secret);

		Date expiration = new Date(System.currentTimeMillis() + expire);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssZ");
		String expireAt = sdf.format(expiration);

		String token = Jwts.builder().setClaims(claims).setSubject(subject).setIssuedAt(new Date(System.currentTimeMillis()))
						.setExpiration(expiration)
						.signWith(Keys.hmacShaKeyFor(secret.getBytes()), SignatureAlgorithm.HS512).compact();
		
		Map<String, String> map = new HashMap<>();

		map.put("token", token);
		map.put("expire", expireAt);

		return map;
	}
}
