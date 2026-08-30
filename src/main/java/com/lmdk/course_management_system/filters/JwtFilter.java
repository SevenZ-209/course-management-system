package com.lmdk.course_management_system.filters;

import com.lmdk.course_management_system.pojo.User;
import com.lmdk.course_management_system.services.UserService;
import com.lmdk.course_management_system.utils.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final UserService userService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if(header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7).trim();

        if(token.isBlank()) {
            unauthorized(response);
            return;
        }

        try {
            String username = jwtUtils.validateTokenAndGetUsername(token);

            if(username == null || username.isBlank()) {
                unauthorized(response);
                return;
            }

            if(SecurityContextHolder.getContext().getAuthentication() == null) {
                User user = userService.getUserByUsername(username);

                if(user == null || user.getStatus() != User.UserStatus.ACTIVE) {
                    unauthorized(response);
                    return;
                }

                SimpleGrantedAuthority authority =
                        new SimpleGrantedAuthority("ROLE_" + user.getRole().name());

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                user.getUsername(),
                                null,
                                List.of(authority)
                        );

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

            filterChain.doFilter(request, response);
        } catch(Exception ex) {
            SecurityContextHolder.clearContext();
            unauthorized(response);
        }
    }

    private void unauthorized(
            HttpServletResponse response
    ) throws IOException {
        response.setStatus(
                HttpServletResponse.SC_UNAUTHORIZED
        );
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        response.getWriter().write(
                "{\"message\":\"Token không hợp lệ hoặc đã hết hạn!\"}"
        );
    }
}