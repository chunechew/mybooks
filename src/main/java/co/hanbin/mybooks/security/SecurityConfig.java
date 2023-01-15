package co.hanbin.mybooks.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
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
        http.cors().and().csrf().disable();
				// .authorizeRequests()
                // .antMatchers("/swagger-resources/**").permitAll()
				// .antMatchers("/apigw").permitAll()
				// .anyRequest().denyAll().and()
				// .exceptionHandling().authenticationEntryPoint(jwtAuthenticationEntryPoint).and()
				// .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

		// http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
