package org.iskm.admin.web.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.iskm.admin.web.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Predicate;

public class JWTAuthenticationSecurityFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    private final RequestMatcher excludedPathMatchers;

    public JWTAuthenticationSecurityFilter(RequestMatcher excludedPathMatchers) {
        this.excludedPathMatchers = excludedPathMatchers;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        if (excludedPathMatchers.matches(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        var token = Arrays.stream(request.getCookies())
                .filter(Objects::nonNull)
                .filter(cookie -> "login.at".equals(cookie.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElse(null);

        if (token != null) {
            if (jwtUtil.isTokenExpired(token)) {
                var newToken = jwtUtil.refreshToken(token);
                response.addCookie(jwtUtil.generateHttpOnlyCookie(newToken));
                token = newToken;
            }
            var decodedToken = jwtUtil.validateToken(token);
            if (Objects.nonNull(decodedToken)) {
                var authToken = new UsernamePasswordAuthenticationToken(decodedToken, null, null);
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
                response.setStatus(HttpServletResponse.SC_OK);
            } else {
                SecurityContextHolder.clearContext();
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            }
        } else {
            SecurityContextHolder.clearContext();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

        filterChain.doFilter(request, response);
    }
}
