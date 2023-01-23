package co.hanbin.mybooks.member.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.hanbin.mybooks.db.service.RedisUtil;
import co.hanbin.mybooks.member.component.JwtUtil;
import co.hanbin.mybooks.member.entity.Member;
import co.hanbin.mybooks.member.entity.Response;
import co.hanbin.mybooks.member.service.MemberService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/member")
public class MemberController {
    @Value("${jwt.access-token-secret}")
    public String ACCESS_TOKEN_SECRET;

    @Value("${jwt.access-token-expire}")
    public long ACCESS_TOKEN_EXPIRE;

    @Value("${jwt.refresh-token-secret}")
    public String REFRESH_TOKEN_SECRET;

    @Value("${jwt.refresh-token-expire}")
    public long REFRESH_TOKEN_EXPIRE;

    @Autowired
    private MemberService memberService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RedisUtil redisUtil;

    @PostMapping("/signup")
    public Response signUpUser(@RequestBody Member member){
        Response response = null;

        try {
            memberService.signUpUser(member);
            response = new Response("success", "회원가입을 성공적으로 완료했습니다.", null);
        } catch(Exception e){
            e.printStackTrace();
            response = new Response("failed", "회원가입을 하는 도중 오류가 발생했습니다.", e.toString());
        }

        return response;
    }

    @PostMapping("/login")
    public Response login(@RequestBody Member member, HttpServletRequest req, HttpServletResponse res) {
        try {
            String username = member.getUsername();
            String password = member.getPassword();
            String role = member.getRole().name();
            member = memberService.login(username, password);

            if(member == null) {
                throw new Exception();
            }

            password = member.getPassword();
            final String accessToken = jwtUtil.doGenerateToken(username, role, ACCESS_TOKEN_SECRET, ACCESS_TOKEN_EXPIRE);
            final String refreshToken = jwtUtil.doGenerateToken(username, role, REFRESH_TOKEN_SECRET, REFRESH_TOKEN_EXPIRE);

            redisUtil.setDataExpire(username, refreshToken, REFRESH_TOKEN_EXPIRE);
            
            Map<String, Object> data = new HashMap<>();
            data.put("accessToken", accessToken);
            data.put("refreshToken", refreshToken);

            return new Response("success", "로그인에 성공했습니다.", data);
        } catch (Exception e) {
            return new Response("error", "로그인에 실패했습니다.", e.getMessage());
        }
    }

    @PostMapping("/logout")
    public Response logout(HttpServletRequest req, HttpServletResponse res) {
        Object authorization = req.getHeader("Authorization");
        String accessToken = null;
        Authentication auth = null;

        if(authorization != null) { // 헤더에 Authorization이 있으면
            accessToken = (String)authorization;

            final String BEARER_ = "Bearer ";

            if(accessToken.startsWith(BEARER_)) { // "Bearer "로 시작하면
                accessToken = accessToken.replace(BEARER_, "");

                Jws<Claims> jws = jwtUtil.getJws(accessToken, ACCESS_TOKEN_SECRET);
                Boolean isAccessTokenValid = jwtUtil.isValid(jws);

                if(isAccessTokenValid != null && isAccessTokenValid == true) { // 토큰이 유효하면
                    String username = (String)(jws.getBody().get("username"));
                    String value = (String)(redisUtil.getData(username)); // Redis에서 데이터 조회

                    if(!ObjectUtils.isEmpty(value)) { // Redis에 데이터가 있으면
                        redisUtil.deleteData(username); // 삭제 처리
                    }
                }
            }
        }

        new SecurityContextLogoutHandler().logout(req, res, auth); // Spring Security에서도 로그아웃 처리

        return new Response("success", "로그아웃되었습니다.", null);
    }

    @PostMapping("/refreshToken")
    public Response refreshToken(@RequestBody Member member, HttpServletRequest req, HttpServletResponse res) {
        try {
            String refreshToken = member.getRefreshToken();
            Jws<Claims> jws = jwtUtil.getJws(refreshToken, REFRESH_TOKEN_SECRET);
            Boolean isJwtValid = jwtUtil.isValid(jws);

            if(isJwtValid != null && isJwtValid == true) {
                log.debug("refreshToken - The JWT is vaild.");
                String username = (String)(jws.getBody().get("username"));

                UserDetails user = memberService.loadUserByUsername(username);

                if(user == null) {
                    throw new Exception();
                }

                String role = user.getAuthorities().iterator().next().toString();

                final String accessToken = jwtUtil.doGenerateToken(username, role, ACCESS_TOKEN_SECRET, ACCESS_TOKEN_EXPIRE);
                refreshToken = jwtUtil.doGenerateToken(username, role, REFRESH_TOKEN_SECRET, REFRESH_TOKEN_EXPIRE);

                redisUtil.setDataExpire(username, refreshToken, REFRESH_TOKEN_EXPIRE);
                
                Map<String, Object> data = new HashMap<>();
                data.put("accessToken", accessToken);
                data.put("refreshToken", refreshToken);

                return new Response("success", "JWT 재발급에 성공했습니다.", data);
            } else {
                if(isJwtValid != null && isJwtValid == false) {
                    throw new IllegalArgumentException();
                } else {
                    throw new ExpiredJwtException(null, null, null);
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
