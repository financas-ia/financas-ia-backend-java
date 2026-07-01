package com.example.financas.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class PreAuthTokenFilter extends OncePerRequestFilter {

    private final PreAuthTokenService preAuthTokenService;

    public PreAuthTokenFilter(PreAuthTokenService preAuthTokenService) {
        this.preAuthTokenService = preAuthTokenService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        var token = this.recoverToken(request);
        if (token != null) {
            var email = preAuthTokenService.validatePreAuthToken(token);
            if (email != null) {
                request.setAttribute("preAuthEmail", email);
            }
        }
        filterChain.doFilter(request, response);
    }

    public String recoverToken(HttpServletRequest request) {
        var authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return null;
        return authHeader.replace("Bearer", "");
    }

}
