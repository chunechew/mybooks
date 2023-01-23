package co.hanbin.mybooks.member.service;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;

import co.hanbin.mybooks.member.entity.JsonResponse;
import co.hanbin.mybooks.member.entity.Member;

public interface MemberService extends UserDetailsService {
    public void signUpUser(Member member);

    public JsonResponse login(Member member);

    public Member loginProc(String username, String password);

    public JsonResponse logout(HttpServletRequest request, HttpServletResponse response);

    public Map<String, Object> refreshToken(String refreshToken);

    public Authentication getAuth(String username);
}
