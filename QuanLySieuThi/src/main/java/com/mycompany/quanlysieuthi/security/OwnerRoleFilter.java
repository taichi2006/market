package com.mycompany.quanlysieuthi.security;

import com.mycompany.quanlysieuthi.util.ResponseUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Filter to restrict access to STORE_OWNER role only.
 */
public class OwnerRoleFilter implements Filter {

    public static final String REQUIRED_ROLE = "STORE_OWNER";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        // Skip preflight OPTIONS request
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        Claims claims = (Claims) request.getAttribute("currentUser");
        if (claims == null) {
            ResponseUtil.sendError(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "Chưa đăng nhập hoặc token không hợp lệ / hết hạn.");
            return;
        }

        String position = claims.get("position", String.class);
        boolean isOwner = position != null && REQUIRED_ROLE.equalsIgnoreCase(position.trim());

        if (isOwner) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        // Allow an employee to view their own profile (GET /api/employee/:id)
        if ("GET".equalsIgnoreCase(request.getMethod())) {
            String targetId = extractTargetId(request);
            String currentEmployeeId = claims.get("employeeId", String.class);
            if (targetId != null && currentEmployeeId != null && targetId.equalsIgnoreCase(currentEmployeeId.trim())) {
                filterChain.doFilter(servletRequest, servletResponse);
                return;
            }
            if (targetId != null) {
                ResponseUtil.sendError(response, HttpServletResponse.SC_FORBIDDEN,
                        "Người gọi không phải là STORE_OWNER (và không phải chính chủ sở hữu tài khoản).");
                return;
            }
        }

        ResponseUtil.sendError(response, HttpServletResponse.SC_FORBIDDEN,
                "Người gọi không phải là STORE_OWNER.");
    }

    public static String extractTargetId(HttpServletRequest request) {
        String pathInfo = request.getPathInfo();
        if (pathInfo != null && !pathInfo.trim().isEmpty() && !pathInfo.trim().equals("/")) {
            String clean = pathInfo.trim();
            if (clean.startsWith("/")) {
                clean = clean.substring(1);
            }
            if (clean.endsWith("/")) {
                clean = clean.substring(0, clean.length() - 1);
            }
            if (!clean.isEmpty()) {
                return clean;
            }
        }

        String uri = request.getRequestURI();
        if (uri != null) {
            String[] parts = uri.split("/");
            if (parts.length > 0) {
                String last = parts[parts.length - 1];
                if (!last.isEmpty() && !last.equalsIgnoreCase("employee") && !last.equalsIgnoreCase("employees")) {
                    return last;
                }
            }
        }
        return null;
    }

    @Override
    public void destroy() {
    }
}
