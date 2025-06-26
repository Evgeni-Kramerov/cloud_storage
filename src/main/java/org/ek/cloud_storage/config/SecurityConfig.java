package org.ek.cloud_storage.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.ek.cloud_storage.security.CloudUserDetailsService;
import org.ek.cloud_storage.security.JsonUsernamePasswordAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final CloudUserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   AuthenticationManager authenticationManager) throws Exception {

        JsonUsernamePasswordAuthenticationFilter jsonAuthFilter =
                new JsonUsernamePasswordAuthenticationFilter("/api/v1/auth/sign-in");

        // Use the shared object to get the authentication manager
        jsonAuthFilter.setAuthenticationManager(authenticationManager);

        return http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/sign-up").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/sign-in").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/sign-out").authenticated()
                        .anyRequest().authenticated()
                )
                .logout(logout -> logout
                        .logoutUrl("/api/v1/auth/sign-out")
                        .logoutSuccessHandler((request, response, authentication) -> {
                            if (authentication ==  null || !authentication.isAuthenticated() ) {
                                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                response.setContentType("application/json");
                                response.getWriter().write(new ObjectMapper().writeValueAsString("Unauthorized"));
                            }
                            else {
                                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                            }
                        })
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")

                )
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authenticationProvider(daoAuthenticationProvider()) // This is important
                .addFilterAt(jsonAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    private AuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new Argon2PasswordEncoder(16,32,1,7,3);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
