package co.hanbin.mybooks.member.service;

import co.hanbin.mybooks.member.entity.Member;

public interface MemberService {
    void signUpUser(Member member);

    Member loginUser(String id, String password);
}
