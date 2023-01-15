package co.hanbin.mybooks.security;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import co.hanbin.mybooks.error.ApiException;
import co.hanbin.mybooks.error.ExceptionEnum;
import co.hanbin.mybooks.user.entity.User;
import co.hanbin.mybooks.user.repository.UserRepository;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SupplementaryJwtUserService {
    @Autowired
	private static UserRepository userRepository;

	@Autowired
	private static JwtTokenUtil jwtTokenUtil;

	// private final long ACCESS_TOKEN_VALIDITY;
	// private final long REFRESH_TOKEN_VALIDITY;
	// private final String ACCESS_TOKEN_SECRET;
	// private final String REFRESH_TOKEN_SECRET;

	// private final JwtUserServiceForFilter jwtUserServiceForFilter;
	private final AuthenticationManager authenticationManager;
	// private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private static final String SSHA512_PREFIX = "{SSHA-512}";
	private static final String sdfPattern = "yyyyMMddHHmmssZ";
	private static final SimpleDateFormat sdf = new SimpleDateFormat(sdfPattern);

	public static User save(User user) {
		Date nowDt = new Date();
		String now = sdf.format(nowDt);
		
		User newUser = new User();
		newUser.setUserId(user.getUserId());
		newUser.setJoinMthdNo(user.getJoinMthdNo());
		newUser.setUserPw(jwtTokenUtil.passwordEncoder().encode(user.getUserPw()).replace(SSHA512_PREFIX, ""));
		newUser.setUserJoinDt(now);
		newUser.setUserModDt(now);
		newUser.setUserCls(user.getUserCls());
		newUser.setUserNm(user.getUserNm());
		newUser.setUserImg(user.getUserImg());

		return userRepository.save(newUser);
	}

	public boolean logout(HttpServletRequest request) {
		final String accessTokenHeader = request.getHeader("Authorization");

        // String userId = null;
		String accessToken = null;

        if (accessTokenHeader != null && accessTokenHeader.startsWith("Bearer ")) {
			accessToken = accessTokenHeader.substring(7);

            try {
				final String ACCESS = "access";
                boolean isExpired = jwtTokenUtil.isTokenExpired(accessToken, ACCESS);

                if(isExpired) {
                    throw new ApiException(ExceptionEnum.INVALID_ACCESS_TOKEN);
                }

                // userId = jwtTokenUtil.getUsernameFromToken(accessToken, ACCESS);
            } catch (IllegalArgumentException e) {
                System.out.println("Unable to get JWT Access Token");
            } catch (ExpiredJwtException e) {
                System.out.println("JWT Access Token has expired");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
			throw new ApiException(ExceptionEnum.INVALID_ACCESS_TOKEN);
		}

		return true;
	}
    
    public void authenticate(String userId, String userPw) throws Exception {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(userId, userPw));
	}
}
