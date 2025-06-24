package com.S_Health.GenderHealthCare.config;

import com.S_Health.GenderHealthCare.entity.User;
import com.S_Health.GenderHealthCare.exception.exceptions.AuthenticationException;
import com.S_Health.GenderHealthCare.service.authentication.JWTService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.security.auth.message.AuthException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.tomcat.util.net.jsse.JSSEUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.util.AntPathMatcher;

import java.io.IOException;
import java.util.List;

@Component
public class Filter extends OncePerRequestFilter {
    @Autowired
    @Qualifier("handlerExceptionResolver")
    private HandlerExceptionResolver resolver;

    @Autowired
    private JWTService jwtService;

    private final List<String> PUBLIC_API = List.of(
            "POST:/api/auth/**",
            "PUT:/api/auth/**",
            "PUT:/api/auth/**",
            "POST:/api/service/**",
            "PUT:/api/service/**",
            "DELETE:/api/service/**",
            "PATCH:/api/service/**",
            "POST:/api/payment/vnpay/**"
    );

    private final List<String> PROTECTED_GET_API = List.of(
            "/api/cycle-track/logs"    // ví dụ route cần bảo vệ
//            "/api/user/private/**"       // thêm wildcard nếu muốn
    );

    public boolean isPulicApi(String uri, String method) {
        // URL http:localhost:8080/api/student
        // URI: api/student

//        if (method.equals("GET")) return true;
        AntPathMatcher matcher = new AntPathMatcher();
        if (method.equalsIgnoreCase("GET")) {
            boolean isProtected = PROTECTED_GET_API.stream()
                    .anyMatch(pattern -> matcher.match(pattern, uri));
            return !isProtected; // Nếu không bị bảo vệ → cho phép
        }
        return PUBLIC_API.stream().anyMatch(pattern -> {
            String[] parts = pattern.split(":", 2);
            if (parts.length != 2) return false;
            String allowedMethod = parts[0];
            String allowedUri = parts[1];
            return method.equalsIgnoreCase(allowedMethod) && matcher.match(allowedUri, uri);
        });
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String uri = request.getRequestURI();
        String method = request.getMethod();
        if(isPulicApi(uri, method)) {
            filterChain.doFilter(request, response);
        }else{
            //xát thực
            String token = getToken(request);
            if(token == null) {
                resolver.resolveException(request, response, null, new AuthenticationException("Empty token!") {
                });
            }

            User user;
            try {
                // từ token tìm ra thằng đó là ai
                user = jwtService.extractAccount(token);
            } catch (ExpiredJwtException expiredJwtException) {
                // token het han
                resolver.resolveException(request, response, null, new AuthException("Token đã hết hạn!"));
                return;
            } catch (MalformedJwtException malformedJwtException) {
                resolver.resolveException(request, response, null, new AuthException("Token không hợp lệ!"));
                return;
            }
            // => token dung
            UsernamePasswordAuthenticationToken
                    authenToken =
                    new UsernamePasswordAuthenticationToken(user, token, user.getAuthorities());
            authenToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authenToken);
            filterChain.doFilter(request, response);
        }
    }

    public String getToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        return authHeader.substring(7);
    }
}
