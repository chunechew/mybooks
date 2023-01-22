package co.hanbin.mybooks.config.security;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CustomAuthenticationManager implements AuthenticationManager {
    @Override
    public Authentication authenticate(Authentication auth) {
        log.info("auth.getName(): " + auth.getName() + ", auth.getCredentials(): " + auth.getCredentials());
        if (auth.getName().equals(auth.getCredentials())) {
            return new UsernamePasswordAuthenticationToken(auth.getName(),
                auth.getCredentials(), auth.getAuthorities());
        }
        throw new BadCredentialsException("Bad Credentials");
    }
}