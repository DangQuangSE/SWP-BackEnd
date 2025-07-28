package com.S_Health.GenderHealthCare.config;

import com.S_Health.GenderHealthCare.entity.User;
import com.S_Health.GenderHealthCare.exception.exceptions.AppException;
import com.S_Health.GenderHealthCare.service.authentication.JWTService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.security.auth.message.AuthException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
            // Authentication APIs
            "POST:/api/auth/**",
            "PUT:/api/auth/**",

            // Public read-only APIs
            "GET:/api/services/**",          // Service catalog
            "GET:/api/specializations/**",   // Specializations
            "GET:/api/rooms/**",             // Room information
            "GET:/api/tags/**",              // Blog tags
            "GET:/api/blog/**",              // Blog reading (except protected ones)
            "GET:/api/comment/blog/**",                // Comment reading

            // Config APIs (public access)
            "GET:/api/config/**",            // Get config values
            "POST:/api/config/**",           // Create config
            "PUT:/api/config/**",            // Update config
            "DELETE:/api/config/**",         // Delete config

            // Chat APIs for customers (no login required)
            "POST:/api/chat/start",          // Customer start chat
            "POST:/api/chat/send",           // Customer send message
            "GET:/api/chat/sessions/*/messages",    // Customer get chat history
            "POST:/api/chat/sessions/*/verify",     // Customer verify/recover session
            "POST:/api/chat/sessions/*/mark-read",  // Mark messages as read (customer can use)
            "GET:/api/chat/sessions/*/unread-count", // Get unread count (customer can use)

            // Payment & External APIs
            "POST:/api/payment/vnpay/**",


            // Swagger UI endpoints
            "GET:/swagger-ui/**",
            "GET:/v3/api-docs/**",
            "GET:/swagger-resources/**",
            "GET:/webjars/**",

            "POST:/api/me/profile",
            "POST:/api/blog/{id}/like"
            );

    private final List<String> PROTECTED_GET_API = List.of(
            "/api/cycle-track/logs",
            "/api/appointment/by-status",
            "/api/appointment/my-schedule",
            "/api/appointment/*",                // Appointment details by ID
            "/api/appointment/*/patient-history", // Patient history
            "/api/appointment/*/status-history",  // Status history
            "/api/me/**",                        // User profile APIs
            "/api/medical-profile/**",           // Medical profile APIs (bao gồm patient history)
            "/api/medical-result/**",            // Medical results
            "/api/payment/history/**",           // Payment history
            "/api/blog/my-blogs/**",
            "/api/blog/admin/**",
            "/api/chat/sessions",                // Staff get chat sessions (GET method)
            "/api/zoom/**",                      // Zoom APIs
            "/api/notifications",
            "/api/certifications/my-certifications"
    );

    private final List<String> PROTECTED_PATCH_API = List.of(
            "/api/appointment/*/status",         // Update appointment status
            "/api/appointment/detail/*/status",   // Update appointment detail status
            "/api/cycle-track/logs"    ,// ví dụ route cần bảo vệ
//            "/api/user/private/**"       // thêm wildcard nếu muốn
            "/api/appointment/by-status",
            "/api/zoom/**",
            "/api/notifications",
            "/api/notifications/{notificationId}",
            "/api/notifications/{id}/read"
    );

    private final List<String> PROTECTED_POST_API = List.of(
            "/api/chat/join/*",                  // Staff join chat session
            "/api/chat/sessions/*/end",           // Staff end chat session
            "/api/zoom/**",
            "/api/me",
            "/api/certifications"
    );

    public boolean isPulicApi(String uri, String method) {
        AntPathMatcher matcher = new AntPathMatcher();

        // Xử lý GET requests
        if (method.equalsIgnoreCase("GET")) {
            boolean isProtected = PROTECTED_GET_API.stream()
                    .anyMatch(pattern -> matcher.match(pattern, uri));
            return !isProtected; // Nếu không bị bảo vệ → cho phép
        }

        // Xử lý PATCH requests
        if (method.equalsIgnoreCase("PATCH")) {
            boolean isProtected = PROTECTED_PATCH_API.stream()
                    .anyMatch(pattern -> matcher.match(pattern, uri));
            return !isProtected; // Nếu không bị bảo vệ → cho phép (nhưng hầu hết PATCH đều protected)
        }

        // Xử lý POST requests
        if (method.equalsIgnoreCase("POST")) {
            // Kiểm tra xem có phải protected POST API không
            boolean isProtected = PROTECTED_POST_API.stream()
                    .anyMatch(pattern -> matcher.match(pattern, uri));

            if (isProtected) {
                return false; // Protected POST API → cần token
            }

            // Kiểm tra xem có phải public POST API không
            return PUBLIC_API.stream().anyMatch(pattern -> {
                String[] parts = pattern.split(":", 2);
                if (parts.length != 2) return false;
                String allowedMethod = parts[0];
                String allowedUri = parts[1];
                return method.equalsIgnoreCase(allowedMethod) && matcher.match(allowedUri, uri);
            });
        }

        // Xử lý các method khác (PUT, DELETE)
        // Tất cả đều cần token trừ những API trong PUBLIC_API
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

        if (isPulicApi(uri, method)) {
            filterChain.doFilter(request, response);
        } else {
            //xát thực
            String token = getToken(request);
            if (token == null) {
                resolver.resolveException(request, response, null, new AppException("Empty token!") {
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
            } catch (IllegalArgumentException illegalArgumentException) {
                // token null hoặc empty
                resolver.resolveException(request, response, null, new AuthException("Token không được để trống!"));
                return;
            } catch (Exception e) {
                // Các lỗi khác khi parse token
                resolver.resolveException(request, response, null, new AuthException("Lỗi xử lý token: " + e.getMessage()));
                return;
            }
            // => token dung
            UsernamePasswordAuthenticationToken
                    authenToken =
                    new UsernamePasswordAuthenticationToken(user, token, user.getAuthorities());
            authenToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authenToken);
            // token ok, cho vao`
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
