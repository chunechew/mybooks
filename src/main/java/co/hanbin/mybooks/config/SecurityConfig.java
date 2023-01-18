package co.hanbin.mybooks.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
// @EnableWebSecurity
// @EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
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
        http.cors().and().csrf().disable()
				.authorizeRequests()
                .antMatchers("/swagger-resources/**").permitAll()
				.antMatchers("/api/**").permitAll()
				.antMatchers("/h2-console/**").permitAll()
				.anyRequest().denyAll().and()
				.csrf().ignoringAntMatchers("/h2-console/**").disable().httpBasic().and()
				.headers().frameOptions().disable().and() // h2-console 전용. frame/iframe 허용
				// .exceptionHandling().authenticationEntryPoint(jwtAuthenticationEntryPoint).and()
				.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

		// http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

	// Security 무시하기 
    public void configure(WebSecurity web)throws Exception{
        web.ignoring().antMatchers("/h2-console/**");
    }
}
