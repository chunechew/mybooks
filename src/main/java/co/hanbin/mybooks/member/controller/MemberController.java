package co.hanbin.mybooks.member.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.hanbin.mybooks.db.service.RedisUtil;
import co.hanbin.mybooks.member.component.JwtUtil;
import co.hanbin.mybooks.member.entity.Member;
import co.hanbin.mybooks.member.entity.Response;
import co.hanbin.mybooks.member.service.MemberService;

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
    public Response login(@RequestBody Member member,
                          HttpServletRequest req,
                          HttpServletResponse res) {
        try {
            String username = member.getUsername();
            String password = member.getPassword();
            String role = member.getRole().name();
            member = memberService.login(username, password);
            password = member.getPassword();
            final String accessToken = jwtUtil.doGenerateToken(username, role, ACCESS_TOKEN_SECRET, ACCESS_TOKEN_EXPIRE);
            final String refreshToken = jwtUtil.doGenerateToken(username, role, REFRESH_TOKEN_SECRET, REFRESH_TOKEN_EXPIRE);

            redisUtil.setDataExpire(refreshToken, member.getUsername(), REFRESH_TOKEN_EXPIRE);
            
            Map<String, Object> data = new HashMap<>();
            data.put("accessToken", accessToken);
            data.put("refreshToken", refreshToken);

            return new Response("success", "로그인에 성공했습니다.", data);
        } catch (Exception e) {
            return new Response("error", "로그인에 실패했습니다.", e.getMessage());
        }
    }
}
