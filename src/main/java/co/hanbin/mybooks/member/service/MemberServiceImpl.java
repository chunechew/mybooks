package co.hanbin.mybooks.member.service;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import co.hanbin.mybooks.member.entity.Member;
import co.hanbin.mybooks.member.enumerate.MemberRole;
import co.hanbin.mybooks.member.repository.MemberRepository;
import co.hanbin.mybooks.member.repository.PrincipalDetails;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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
            final String ENCODED_PASSWORD = saltUtil.encodePassword(PASSWORD_SALT, password);
            log.debug("MemberServiceImpl > login(username, password) > ENCODED_PASSWORD: " + ENCODED_PASSWORD);
            log.debug("MemberServiceImpl > login(username, password) > SAVED_PASSWORD: " + member.getPassword());
            if(!member.getPassword().contentEquals(ENCODED_PASSWORD)) {
                throw new Exception ("비밀번호가 틀립니다.");
            }
            Collection<GrantedAuthority> authorities = new ArrayList<>();
		    authorities.add(new SimpleGrantedAuthority(member.getRole().name()));
            Authentication auth = new UsernamePasswordAuthenticationToken(username, ENCODED_PASSWORD, authorities);
            SecurityContextHolder.getContext().setAuthentication(auth); // 세션에 저장
        } catch (Exception e) {
            e.printStackTrace();
            return null;
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
