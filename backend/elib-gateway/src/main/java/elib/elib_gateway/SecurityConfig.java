package elib.elib_gateway;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

	private final JWTFilter jwtFilter;

	public SecurityConfig(JWTFilter jwtFilter) {
		this.jwtFilter = jwtFilter;
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		
		http.csrf(csrf -> csrf.disable())
				.authorizeHttpRequests(auth -> auth
				.requestMatchers("/api/user/auth/**").permitAll()
				.requestMatchers("/actuator/health").permitAll()
				.anyRequest().authenticated()
                )
				.addFilterBefore(jwtFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class)
				.httpBasic(Customizer.withDefaults());

		return http.build();
	}
}