package org.iskm.admin.web.config;

import jakarta.servlet.http.Cookie;
import org.iskm.admin.web.security.JWTAuthenticationSecurityFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.transaction.PlatformTransactionManager;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import java.util.List;

@Configuration
public class SecurityConfig {

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // Required for encoding passwords
    }

    @Bean
    public RequestMatcher excludedPathMatchers() {
        List<String> excludedPaths = List.of(
                "/login",
                "/authenticate/user",
                "/public/**",
                "/css/**",
                "/js/**",
                "/images/**"
        );

        PathMatcher pathMatcher = new AntPathMatcher();

        return request -> {
            String path = request.getServletPath();
            return excludedPaths.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
        };
    }


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        var jwtFilter = new JWTAuthenticationSecurityFilter(excludedPathMatchers());

        return http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(req ->
                        req.requestMatchers(excludedPathMatchers())
                                .permitAll()
                                .anyRequest().permitAll()//.authenticated()
                )//.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .logout(logout ->
                        logout.logoutUrl("/logout")
                                .logoutSuccessHandler(
                                        (request, response, authentication) -> {
                                            var cookie = new Cookie("login.at", null);
                                            cookie.setHttpOnly(true);
                                            cookie.setPath("/");
                                            cookie.setMaxAge(0);
                                            response.addCookie(cookie);
                                        }
                                ).logoutSuccessUrl("/login")
                                .permitAll()
                ).sessionManagement(
                        session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .build();
    }
}