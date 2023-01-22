package co.hanbin.mybooks.member.service;

import org.springframework.security.core.userdetails.UserDetailsService;

import co.hanbin.mybooks.member.entity.Member;

public interface MemberService extends UserDetailsService {
    void signUpUser(Member member);

    Member login(String username, String password);
}
