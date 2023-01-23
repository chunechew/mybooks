package co.hanbin.mybooks.member.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import co.hanbin.mybooks.db.service.RedisUtil;
import co.hanbin.mybooks.member.component.JwtUtil;
import co.hanbin.mybooks.member.entity.JsonResponse;
import co.hanbin.mybooks.member.entity.Member;
import co.hanbin.mybooks.member.enumerate.MemberRole;
import co.hanbin.mybooks.member.repository.MemberRepository;
import co.hanbin.mybooks.member.repository.PrincipalDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class MemberServiceImpl implements MemberService {
    @Value("${jwt.access-token-secret}")
    public String ACCESS_TOKEN_SECRET;

    @Value("${jwt.access-token-expire}")
    public long ACCESS_TOKEN_EXPIRE;

    @Value("${jwt.refresh-token-secret}")
    public String REFRESH_TOKEN_SECRET;

    @Value("${jwt.refresh-token-expire}")
    public long REFRESH_TOKEN_EXPIRE;

    @Value("${password-salt}")
    private String PASSWORD_SALT;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private SaltUtil saltUtil;

    @Override
    public void signUpUser(Member member) {
        String password = member.getPassword();
        member.setPassword(saltUtil.encodePassword(PASSWORD_SALT, password));
        member.setRole(MemberRole.ROLE_USER);
        memberRepository.save(member);
    }

    @Override
    public JsonResponse login(Member member) {
        try {
            String username = member.getUsername();
            String password = member.getPassword();
            String role = member.getRole().name();
            member = loginProc(username, password);

            if(member == null) {
                throw new Exception();
            }

            password = member.getPassword();

            long current = new Date().getTime();
            final String accessToken = jwtUtil.doGenerateToken(username, role, ACCESS_TOKEN_SECRET, current + ACCESS_TOKEN_EXPIRE);
            final String refreshToken = jwtUtil.doGenerateToken(username, role, REFRESH_TOKEN_SECRET, current + REFRESH_TOKEN_EXPIRE);

            redisUtil.setDataExpire("AT:" + username, accessToken, current + ACCESS_TOKEN_EXPIRE);
            redisUtil.setDataExpire("RT:" + username, refreshToken, current + REFRESH_TOKEN_EXPIRE);
            
            ServletRequestAttributes attributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
            HttpServletRequest request = null;
            
            if(attributes != null) {
                request = attributes.getRequest();
                HttpSession session = request.getSession();

                Map<String, Object> newTokens = new HashMap<>();
                newTokens.put("accessToken", accessToken);
                newTokens.put("accessTokenExpire", current + ACCESS_TOKEN_EXPIRE);
                newTokens.put("refreshToken", refreshToken);
                newTokens.put("refreshTokenExpire", current + REFRESH_TOKEN_EXPIRE);

                session.setAttribute("newTokens", newTokens); // 새 토큰을 Spring Framework 세션에 임시 기억(JsonResponse에서 참조)
            }

            return new JsonResponse("success", "로그인에 성공했습니다.", null);
        } catch (Exception e) {
            return new JsonResponse("error", "로그인에 실패했습니다.", e.getMessage());
        }
    }

    @Override
    public Member loginProc(String username, String password) {
        Member member = memberRepository.findByUsername(username);
        
        try {
            if(member==null) throw new Exception ("멤버가 조회되지 않음");
            final String ENCODED_PASSWORD = saltUtil.encodePassword(PASSWORD_SALT, password);
            log.debug("MemberServiceImpl > login(username, password) > ENCODED_PASSWORD: " + ENCODED_PASSWORD);
            log.debug("MemberServiceImpl > login(username, password) > SAVED_PASSWORD: " + member.getPassword());
            if(!member.getPassword().contentEquals(ENCODED_PASSWORD)) {
                throw new Exception ("비밀번호가 틀립니다.");
            }
            Collection<GrantedAuthority> authorities = new ArrayList<>();
		    authorities.add(new SimpleGrantedAuthority(member.getRole().name()));
            Authentication auth = new UsernamePasswordAuthenticationToken(username, ENCODED_PASSWORD, authorities);
            SecurityContextHolder.getContext().setAuthentication(auth); // 세션에 저장
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return member;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Member member = memberRepository.findByUsername(username);
        if(member != null) return new PrincipalDetails(member);
        return null;
    }

    @Override
    public JsonResponse logout(HttpServletRequest request, HttpServletResponse response) {
        Object authorization = request.getHeader("Authorization");
        String accessToken = null;
        Authentication auth = null;

        if(authorization != null) { // 헤더에 Authorization이 있으면
            accessToken = (String)authorization;

            final String BEARER_ = "Bearer ";

            if(accessToken.startsWith(BEARER_)) { // "Bearer "로 시작하면
                accessToken = accessToken.replace(BEARER_, "");

                Map<String, Object> isAccessTokenValidAndJws = jwtUtil.isValid(accessToken, ACCESS_TOKEN_SECRET, "AT:");
                
                @SuppressWarnings("unchecked")
                Jws<Claims> jws = (Jws<Claims>)isAccessTokenValidAndJws.get("jws");
                Boolean isAccessTokenValid = (Boolean)isAccessTokenValidAndJws.get("isValid");

                if(isAccessTokenValid != null && isAccessTokenValid == true) { // 토큰이 유효하면
                    String username = (String)(jws.getBody().get("username"));
                    String value = (String)(redisUtil.getData(username)); // Redis에서 데이터 조회

                    if(!ObjectUtils.isEmpty(value)) { // Redis에 데이터가 있으면
                        redisUtil.deleteData("AT:" + username); // 삭제 처리
                        redisUtil.deleteData("RT:" + username);
                    }
                }
            }
        }

        new SecurityContextLogoutHandler().logout(request, response, auth); // Spring Security에서도 로그아웃 처리

        return new JsonResponse("success", "로그아웃되었습니다.", null);
    }

    @Override
    public Map<String, Object> refreshToken(String refreshToken) {
        Map<String, Object> result = new HashMap<>();

        try {
            Map<String, Object> isJwtValidAndJws = jwtUtil.isValid(refreshToken, REFRESH_TOKEN_SECRET, "RT:");
            
            @SuppressWarnings("unchecked")
            Jws<Claims> jws = (Jws<Claims>)isJwtValidAndJws.get("jws");
            Boolean isJwtValid = (Boolean)isJwtValidAndJws.get("isValid");

            if(isJwtValid != null && isJwtValid == true) {
                log.debug("refreshToken - The JWT is vaild.");
                String username = (String)(jws.getBody().get("username"));

                UserDetails user = loadUserByUsername(username);

                if(user == null) {
                    throw new Exception();
                }

                String role = user.getAuthorities().iterator().next().toString();


                long current = new Date().getTime();
                String accessToken = jwtUtil.doGenerateToken(username, role, ACCESS_TOKEN_SECRET, current + ACCESS_TOKEN_EXPIRE);
                refreshToken = jwtUtil.doGenerateToken(username, role, REFRESH_TOKEN_SECRET, current + REFRESH_TOKEN_EXPIRE);

                redisUtil.setDataExpire("AT:" + username, accessToken, current + ACCESS_TOKEN_EXPIRE);
                redisUtil.setDataExpire("RT:" + username, refreshToken, current + REFRESH_TOKEN_EXPIRE);
                
                Map<String, Object> newTokens = new HashMap<>();
                newTokens.put("accessToken", accessToken);
                newTokens.put("accessTokenExpire", current + ACCESS_TOKEN_EXPIRE);
                newTokens.put("refreshToken", refreshToken);
                newTokens.put("refreshTokenExpire", current + REFRESH_TOKEN_EXPIRE);

                result.put("status", "success");
                result.put("newTokens", newTokens);

                return result;
            } else {
                throw new Exception();
            }
        } catch(Exception e) {
            e.printStackTrace();

            result.put("status", "success");
            result.put("data", null);

            return result;
        }
    }

    @Override
    public Authentication getAuth(String username) {
		log.debug("getAuth - process");
		Authentication auth = null;
		PrincipalDetails user = (PrincipalDetails)loadUserByUsername(username);

		log.debug("getAuth > user: " + user.getUsername() + ", " + user.getPassword() + ", " + user.getAuthorities());
		
		if(user != null && user.getUsername() != null) {
			auth = new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword(), user.getAuthorities());
		}
		
		return auth;
	}
}
