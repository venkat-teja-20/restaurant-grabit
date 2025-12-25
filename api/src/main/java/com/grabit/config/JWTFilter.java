package com.grabit.config;

import com.grabit.Utilities.JwtUtil;
import com.grabit.Utilities.Utility;
import com.grabit.bean.PermissionDTO;
import com.grabit.bean.RoleDTO;
import com.grabit.enums.CommonErrors;
import com.grabit.exception.APIError;
import com.grabit.exception.CustomException;
import com.grabit.exception.JwtAuthenticationException;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@Log4j2
public class JWTFilter extends OncePerRequestFilter {

    @Autowired
    private RestTemplate restTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            String authHeader = request.getHeader("Authorization") == null ? request.getHeader("authorization") : request.getHeader("Authorization");
            String token = null;
            String email = null;
            List<Integer> permissions = null;
            String role = null;
            if (!Utility.isNullOrEmpty(authHeader) && authHeader.startsWith("JWT ")) {
                token = authHeader.substring(4);
                Claims claims = JwtUtil.extractClaims(token);
                email = JwtUtil.getEmail(claims);
                permissions = JwtUtil.getPermissions(claims);
                role = JwtUtil.extractRole(claims);
            }

            if (permissions == null || permissions.isEmpty()) {
                throw new CustomException(Utility.buildErrorObject("PERMISSIONS_MISSING", "User do not have any valid permissions", 500, "jwtFilter"));
            }

            // Get Permissions Data From Auth Service
            if (role != null && email != null && SecurityContextHolder.getContext() == null) {
                RoleDTO roleDTO = restTemplate.getForObject("http://localhost:7090/role/permissions?role=" + role, RoleDTO.class);
                List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                if (Utility.isNullOrEmpty(roleDTO) || Utility.isNullOrEmpty(roleDTO.getRole()) || Utility.isNullOrEmpty(roleDTO.getPermissions())) {
                    throw new CustomException(Utility.buildErrorObject("INVALID_RESPONSE", "response received from auth service while fetching role details is null or not valid", 500, "jwtFilter"));
                }
                authorities.add(new SimpleGrantedAuthority(roleDTO.getRole().toValue()));
                for (PermissionDTO permissionDTO : roleDTO.getPermissions()) {
                    authorities.add(new SimpleGrantedAuthority(permissionDTO.getPermission().toValue()));
                }
                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(email, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
            }
            filterChain.doFilter(request, response);
        } catch (JwtAuthenticationException e){
            APIError apiError=e.getAuthenticationError();
            if(Utility.isNullOrEmpty(apiError))
                apiError=new APIError(CommonErrors.AUTHENTICATION_REQUIRED.toString(),CommonErrors.AUTHENTICATION_REQUIRED.getMessage());
            log.info("Authentication Unsuccessful : "+Utility.toJson(e.getAuthenticationError()));
            request.setAttribute("responseWriteFlag",true);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write(Utility.toJson(e.getAuthenticationError()));
        }
    }
}
