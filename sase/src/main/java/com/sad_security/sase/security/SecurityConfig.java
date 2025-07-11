package com.sad_security.sase.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


import com.sad_security.sase.service.CustomStudentDetailsService;


@Configuration
@EnableWebSecurity
public class SecurityConfig{




/*  BISOGNA USARE PER FORZA QUESTA FUNZIONE DI ENCRYPTION IN QUANTO SPRING SECURITY CONFRONTA L'HASH DELLA PASSWORD
 CONSERVATA IN DATABASE E QUELLO DELLA PASSWORD INSERITA DALL'UTENTE */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // Usa BCryptPasswordEncoder
    }
 


	@Bean
	public SecurityFilterChain filterChain (HttpSecurity http) throws Exception{
		http
		.authorizeHttpRequests(requests -> requests.requestMatchers( "/dashboard")
		.authenticated()
		.anyRequest().permitAll()
		)
		.formLogin( form -> form.loginPage("/login")
		.defaultSuccessUrl("/dashboard", true)
		.failureUrl("/login?error")
		.permitAll()
		).logout( logout -> logout.permitAll()
		);
		


	return http.build();
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception{

		return authenticationConfiguration.getAuthenticationManager();
		

	}
}