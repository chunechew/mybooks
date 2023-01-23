package co.hanbin.mybooks.member.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.hanbin.mybooks.member.entity.JsonResponse;
import co.hanbin.mybooks.member.entity.Member;
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

    @PostMapping("/signup")
    public JsonResponse signUpUser(@RequestBody Member member){
        JsonResponse response = null;

        try {
            memberService.signUpUser(member);
            response = new JsonResponse("success", "회원가입을 성공적으로 완료했습니다.", null);
        } catch(Exception e){
            e.printStackTrace();
            response = new JsonResponse("failed", "회원가입을 하는 도중 오류가 발생했습니다.", e.toString());
        }

        return response;
    }

    @PostMapping("/login")
    public JsonResponse login(@RequestBody Member member, HttpServletRequest request, HttpServletResponse response) {
        return memberService.login(member);
    }

    @PostMapping("/logout")
    public JsonResponse logout(HttpServletRequest request, HttpServletResponse response) {
        return memberService.logout(request, response);
    }

    @PostMapping("/refreshToken")
    public JsonResponse refreshToken(@RequestBody Member member, HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> result = memberService.refreshToken(member.getRefreshToken());
        String status = (String)(result.get("status"));
        
        if(status.contentEquals("success")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> newTokens = (Map<String, Object>)(result.get("newTokens"));

            HttpSession session = request.getSession();
            session.setAttribute("newTokens", newTokens); // 새 토큰을 세션에 기억(JsonResponse에서 참조)

            return new JsonResponse("success", "JWT 재발급에 성공했습니다.", null);
        } else {
            return new JsonResponse("error", "JWT 재발급에 실패했습니다.", null);
        }
    }
}
