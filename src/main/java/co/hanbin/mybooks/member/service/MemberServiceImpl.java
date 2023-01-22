package co.hanbin.mybooks.member.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import co.hanbin.mybooks.member.entity.Member;
import co.hanbin.mybooks.member.enumerate.MemberRole;
import co.hanbin.mybooks.member.repository.MemberRepository;
import co.hanbin.mybooks.member.repository.PrincipalDetails;

@Service
public class MemberServiceImpl implements MemberService {
    @Value("${password-salt}")
    private String PASSWORD_SALT;

    @Autowired
    private MemberRepository memberRepository;

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
    public Member login(String username, String password) {
        Member member = memberRepository.findByUsername(username);
        
        try {
            if(member==null) throw new Exception ("멤버가 조회되지 않음");
            password = saltUtil.encodePassword(PASSWORD_SALT, password);
            if(!member.getPassword().equals(password))
                throw new Exception ("비밀번호가 틀립니다.");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return member;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Member member = memberRepository.findByUsername(username);
        if(member != null) return new PrincipalDetails(member);
        return null;
    }
}
