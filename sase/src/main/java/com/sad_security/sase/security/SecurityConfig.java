package com.sad_security.sase.security;

import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        // AuthenticationProvider per professori
        @SuppressWarnings("deprecation")
        @Bean
        public DaoAuthenticationProvider professoreAuthenticationProvider(
                        @Qualifier("professoreDetailsService") UserDetailsService professoreDetailsService) {
                DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
                provider.setUserDetailsService(professoreDetailsService);
                provider.setPasswordEncoder(passwordEncoder());
                return provider;
        }

        // AuthenticationProvider per studenti
        @SuppressWarnings("deprecation")
        @Bean
        public DaoAuthenticationProvider studenteAuthenticationProvider(
                        @Qualifier("studenteDetailsService") UserDetailsService studenteDetailsService) {
                DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
                provider.setUserDetailsService(studenteDetailsService);
                provider.setPasswordEncoder(passwordEncoder());
                return provider;
        }

        // AuthenticationManager che gestisce entrambi
        @Bean
        public AuthenticationManager authenticationManager(
                        DaoAuthenticationProvider professoreAuthenticationProvider,
                        DaoAuthenticationProvider studenteAuthenticationProvider) {
                return new ProviderManager(List.of(professoreAuthenticationProvider,
                                studenteAuthenticationProvider));
        }

        // Sicurezza per professore
        @Bean
        public SecurityFilterChain professoreFilterChain(HttpSecurity http) throws Exception {
                http
                                .securityMatcher("/professore/**", "/classe/crea", "/account/professore/**")
                                .authorizeHttpRequests(auth -> auth
                                                .anyRequest().hasRole("PROFESSORE"))
                                .formLogin(form -> form
                                                .loginPage("/professore/login")
                                                .defaultSuccessUrl("/professore/profDashboard", true)
                                                .failureUrl("/professore/login?error")
                                                .permitAll())
                                .exceptionHandling(exception -> exception.accessDeniedPage("/accessDenied"))
                                .logout(logout -> logout.permitAll());

                return http.build();
        }

        // Sicurezza per studente
        @Bean
        public SecurityFilterChain studenteFilterChain(HttpSecurity http) throws Exception {
                http
                                .securityMatcher("/account/studente/**", "/login", "/studente/dashboard",
                                                "/classe/getClassiIscritte",
                                                "/classe/iscriviti")
                                .authorizeHttpRequests(auth -> auth
                                                .anyRequest()
                                                .hasRole("STUDENTE")

                                )
                                .formLogin(form -> form
                                                .loginPage("/login")
                                                .loginProcessingUrl("/login")
                                                .defaultSuccessUrl("/studente/dashboard", true)
                                                .failureUrl("/login?error")
                                                .permitAll())
                                .exceptionHandling(exception -> exception.accessDeniedPage("/accessDenied"))
                                .logout(logout -> logout.permitAll());

                return http.build();
        }
}
