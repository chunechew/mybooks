package co.hanbin.mybooks.member.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import co.hanbin.mybooks.member.entity.Member;
import co.hanbin.mybooks.member.repository.MemberRepository;

@Service
public class MemberServiceImpl implements MemberService {
    @Value("${jwt.access-token-secret}")
    private String ACCESS_TOKEN_SECRET;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private SaltUtil saltUtil;

    @Override
    public void signUpUser(Member member) {
        String password = member.getPassword();
        String salt = saltUtil.genSalt();
        member.setSalt(ACCESS_TOKEN_SECRET);
        member.setPassword(saltUtil.encodePassword(salt,password));
        memberRepository.save(member);
    }

    @Override
    public Member loginUser(String id, String password) {
        Member member = memberRepository.findByUsername(id);
        
        try {
            if(member==null) throw new Exception ("멤버가 조회되지 않음");
            String salt = member.getSalt();
            password = saltUtil.encodePassword(salt,password);
            if(!member.getPassword().equals(password))
                throw new Exception ("비밀번호가 틀립니다.");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return member;
    }
}
