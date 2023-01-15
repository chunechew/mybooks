package co.hanbin.mybooks.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

public class CustomAuthenticationManager implements AuthenticationManager {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public Authentication authenticate(Authentication auth) {
        logger.info("auth.getName(): " + auth.getName() + ", auth.getCredentials(): " + auth.getCredentials());
        // if (auth.getName().equals(auth.getCredentials())) {
            return new UsernamePasswordAuthenticationToken(auth.getName(),
                auth.getCredentials(), auth.getAuthorities());
        // }
        // throw new BadCredentialsException("Bad Credentials");
    }
}