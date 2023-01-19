package co.hanbin.mybooks.member.controller;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.hanbin.mybooks.db.service.RedisUtil;
import co.hanbin.mybooks.member.component.JwtUtil;
import co.hanbin.mybooks.member.entity.Member;
import co.hanbin.mybooks.member.entity.Response;
import co.hanbin.mybooks.member.entity.request.RequestLoginUser;
import co.hanbin.mybooks.member.service.CookieUtil;
import co.hanbin.mybooks.member.service.MemberService;

@RestController
@RequestMapping("/member")
public class MemberController {

    @Autowired
    private MemberService memberService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CookieUtil cookieUtil;

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
    public Response login(@RequestBody RequestLoginUser user,
                          HttpServletRequest req,
                          HttpServletResponse res) {
        try {
            final Member member = memberService.loginUser(user.getUsername(), user.getPassword());
            final String token = jwtUtil.generateToken(member);
            final String refreshJwt = jwtUtil.generateRefreshToken(member);
            Cookie accessToken = cookieUtil.createCookie(JwtUtil.ACCESS_TOKEN_NAME, token);
            Cookie refreshToken = cookieUtil.createCookie(JwtUtil.REFRESH_TOKEN_NAME, refreshJwt);
            redisUtil.setDataExpire(refreshJwt, member.getUsername(), JwtUtil.REFRESH_TOKEN_EXPIRE);
            res.addCookie(accessToken);
            res.addCookie(refreshToken);
            return new Response("success", "로그인에 성공했습니다.", token);
        } catch (Exception e) {
            return new Response("error", "로그인에 실패했습니다.", e.getMessage());
        }
    }
}
