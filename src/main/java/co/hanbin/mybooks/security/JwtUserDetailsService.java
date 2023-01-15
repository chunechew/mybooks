package co.hanbin.mybooks.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import co.hanbin.mybooks.user.entity.User;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JwtUserDetailsService implements UserDetailsService {
	private final JwtUserServiceForFilter jwtUserServiceForFilter;
	
	@Override
	public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
		User user = jwtUserServiceForFilter.getUserByUserId(userId);
		return jwtUserServiceForFilter.getUserDetailsByUser(user);
	}
}