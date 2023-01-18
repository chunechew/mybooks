package co.hanbin.mybooks.member.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.hanbin.mybooks.member.entity.Member;
import co.hanbin.mybooks.member.entity.Response;
import co.hanbin.mybooks.member.service.MemberService;

@RestController
@RequestMapping("/user")
public class MemberController {

    @Autowired
    private MemberService memberService;


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
}
