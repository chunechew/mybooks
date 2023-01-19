package co.hanbin.mybooks.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

import co.hanbin.mybooks.member.enumerate.MemberRole;

@Configuration
// @EnableWebSecurity
// @EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
	@Autowired
	private Environment environment;

    // @Autowired
	// private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

	// @Autowired
	// private JwtRequestFilter jwtRequestFilter;

	// private UserDetailsService jwtUserDetailsService;

	// @Bean
    // public UserDetailsService userDetailsService() {
	// 	return jwtUserDetailsService;
	// }

	// @Bean
    // public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
    //     return new CustomAuthenticationManager();
    // }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		String[] profilesArray = this.environment.getActiveProfiles();
		List<String> profiles = Arrays.asList(profilesArray);

		if(profiles.contains("dev")) {
			http.authorizeRequests().antMatchers("/h2-console/**").permitAll().and()
				// .csrf().ignoringAntMatchers("/h2-console/**").disable().httpBasic().and()
				.headers().frameOptions().disable();
		}

        http.cors().and().csrf().disable()
				.authorizeRequests()
                .antMatchers("/swagger-resources/**").permitAll()
				.antMatchers("/api/member/**").permitAll()
				.antMatchers("/api/**").hasAnyRole(MemberRole.ROLE_USER.name(), MemberRole.ROLE_ADMIN.name());
				// .antMatchers("/h2-console/**").permitAll()
				// .anyRequest().denyAll().and()
				// .csrf().ignoringAntMatchers("/h2-console/**").disable().httpBasic().and()
				// .headers().frameOptions().disable().and() // h2-console 전용. frame/iframe 허용
				// .exceptionHandling().authenticationEntryPoint(jwtAuthenticationEntryPoint).and()
				// .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

		http.authorizeRequests().anyRequest().denyAll().and()
			.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
			.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

	// Security 무시하기 
    public void configure(WebSecurity web)throws Exception{
        web.ignoring().antMatchers("/h2-console/**");
    }
}
