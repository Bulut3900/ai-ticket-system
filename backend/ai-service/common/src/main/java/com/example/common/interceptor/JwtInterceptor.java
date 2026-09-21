package com.example.common.interceptor;

import com.example.common.BusinessException;                     // ← common
import com.example.common.util.JwtUtil;                          // ← common
import com.example.common.util.UserContext;                  // ← 暂时还引用 ai-service 的（后面会处理）
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 放行 OPTIONS 请求（跨域预检）
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 从请求头中拿 Token
        String token = request.getHeader("Authorization");

        if (token == null || token.isEmpty()) {
            throw new BusinessException(401, "未登录，请先登录");
        }

        // 如果 Token 以 "Bearer " 开头，去掉前缀
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        // 校验 Token
        if (!jwtUtil.validateToken(token)) {
            throw new BusinessException(401, "登录已过期，请重新登录");
        }

        // 解析用户信息，放进上下文
        Claims claims = jwtUtil.parseToken(token);
        Long userId = Long.valueOf(claims.getSubject());
        String username = claims.get("username", String.class);
        String role = claims.get("role", String.class);

        UserContext.set(userId, username, role);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 请求结束后清理 ThreadLocal，避免内存泄漏
        UserContext.clear();
    }
}