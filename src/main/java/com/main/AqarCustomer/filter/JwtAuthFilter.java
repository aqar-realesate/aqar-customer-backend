package com.main.AqarCustomer.filter;

import com.main.AqarCustomer.service.CustomUserDetailsService;
import com.main.AqarCustomer.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService customUserDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        String token = null;
        String email = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {

            token = authHeader.substring(7);
            email = jwtUtil.extractEmail(token);
        }
        if(email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // TODO fetch user by email
            UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);
            // TODO Validate Token
            if(jwtUtil.validateToken(email, userDetails, token)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails, null);
                // TODO Set all request details to the object that will be saved in SecurityContextHolder after verifying the jwt
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                // TODO set to spring context
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        // TODO - call the next filter in the filter chain
        filterChain.doFilter(request, response);

    }
}
