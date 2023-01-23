package co.hanbin.mybooks.config.security;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import co.hanbin.mybooks.member.enumerate.MemberRole;

@Configuration
public class SecurityConfig {
	public final String API_DIRECTORY = "/api/"; // 로그인된 사용자만 허용할 경로
	public final String API_DIRECTORY_EXCEPTION = "/api/member/"; // API_DIRECTORY에 해당돼도 예외적으로 모든 사용자에게 허용할 경로

	@Autowired
	private Environment environment;

	@Autowired
	private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

	@Bean
    public AuthenticationManager authenticationManager() throws Exception {
        return new CustomAuthenticationManager();
    }

	@Bean
	public JwtAuthenticationFilter getJwtAuthenticationFilter() throws Exception {
		return new JwtAuthenticationFilter();
	}

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		String[] profilesArray = environment.getActiveProfiles();
		List<String> profiles = Arrays.asList(profilesArray);

		// 개발 모드에서만 허용(Swagger UI, H2 Console)
		if(profiles.contains("dev")) {
			http.authorizeRequests()
					.antMatchers("/swagger-resources/**", "/swagger-ui/**", "/v2/api-docs", "/h2-console/**").permitAll().and()
				.headers().frameOptions().disable(); // H2 콘솔 화면에서 프레임을 사용하기 위해 허용
		}

		http.cors().and().csrf().disable()
				.formLogin().disable().logout().invalidateHttpSession(true).clearAuthentication(true).permitAll().and()
				.authorizeRequests()
					.antMatchers("/favicon.ico", "/error", API_DIRECTORY_EXCEPTION + "**").permitAll()
					.antMatchers(API_DIRECTORY + "**").hasAnyAuthority(MemberRole.ROLE_USER.name(), MemberRole.ROLE_ADMIN.name())
					.anyRequest().denyAll().and()
				.exceptionHandling().authenticationEntryPoint(jwtAuthenticationEntryPoint).and()
				.addFilterBefore(getJwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
		
        return http.build();
    }
}
