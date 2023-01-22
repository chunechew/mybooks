package co.hanbin.mybooks.config.security;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import co.hanbin.mybooks.member.enumerate.MemberRole;

@Configuration
public class SecurityConfig {
	public final String API_DIRECTORY = "/api/";
	public final String API_DIRECTORY_EXCEPTION = "/api/member/";

	@Autowired
	private Environment environment;
	
	// @Autowired
	// private MemberService memberService;

	@Autowired
	private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

	@Bean
    public AuthenticationManager authenticationManager() throws Exception {
        return new CustomAuthenticationManager();
    }

	// @Bean
	// public JwtAuthenticationFilter getJwtAuthenticationFilter() throws Exception {
	// 	JwtAuthenticationFilter filter = new JwtAuthenticationFilter(authenticationManager(), memberService);
	// 	return filter;
	// }

	@Bean
	public JwtAuthorizationFilter getJwtAuthorizationFilter() throws Exception {
		return new JwtAuthorizationFilter(/*authenticationManager(), memberService*/);
	}

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		String[] profilesArray = environment.getActiveProfiles();
		List<String> profiles = Arrays.asList(profilesArray);

		if(profiles.contains("dev")) {
			http.authorizeRequests()
					.antMatchers("/swagger-resources/**", "/swagger-ui/**", "/v2/api-docs", "/h2-console/**").permitAll().and()
				.headers().frameOptions().disable(); // H2 콘솔 화면에서 프레임을 사용하기 위해 허용
		}

		http.cors().and().csrf().disable()
				.formLogin().disable().logout().invalidateHttpSession(true).clearAuthentication(true).permitAll().and()
				.authorizeRequests()
					.antMatchers("/favicon.ico", "/error", "/api/member/**").permitAll()
					.antMatchers(API_DIRECTORY + "**").authenticated()//.hasAnyRole(MemberRole.ROLE_USER.name(), MemberRole.ROLE_ADMIN.name())
					.anyRequest().denyAll().and()
				.exceptionHandling().authenticationEntryPoint(jwtAuthenticationEntryPoint).and()
				// .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.ALWAYS).and()
				// .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
				// .addFilter(getJwtAuthenticationFilter())
				// .addFilterBefore(getJwtAuthorizationFilter(), UsernamePasswordAuthenticationFilter.class);
				.addFilterBefore(getJwtAuthorizationFilter(), UsernamePasswordAuthenticationFilter.class);
		
        return http.build();
    }
}
