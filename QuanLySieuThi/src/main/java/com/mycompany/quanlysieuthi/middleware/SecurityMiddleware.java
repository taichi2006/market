package com.mycompany.quanlysieuthi.middleware;

import com.mycompany.quanlysieuthi.util.JwtUtil;
import com.mycompany.quanlysieuthi.util.ResponseUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
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
 * Centralized Security Middleware for the application.
 * Responsibilities:
 * 1. Allow preflight CORS OPTIONS requests.
 * 2. Bypass public routes (e.g. /api/auth/*).
 * 3. Verify Bearer Access Token (JWT) for protected routes (HTTP 401 on failure).
 * 4. Perform Role-Based Access Control (RBAC) per API endpoint (HTTP 403 on forbidden).
 */
public class SecurityMiddleware implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        // 1. Skip preflight OPTIONS request
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        // 2. Allow public endpoints without token
        if (isPublicRoute(request)) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        // 3. Verify Bearer Access Token
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            ResponseUtil.sendError(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "Chưa đăng nhập hoặc token không hợp lệ / hết hạn.");
            return;
        }

        String token = authHeader.substring(7).trim();
        Claims claims;
        try {
            claims = JwtUtil.parseToken(token);
        } catch (JwtException | IllegalArgumentException e) {
            ResponseUtil.sendError(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "Chưa đăng nhập hoặc token không hợp lệ / hết hạn.");
            return;
        }

        request.setAttribute("currentUser", claims);

        // 4. Role-based authorization
        String position = claims.get("position", String.class);
        if (position == null || position.trim().isEmpty()) {
            ResponseUtil.sendError(response, HttpServletResponse.SC_FORBIDDEN,
                    "Không xác định được quyền hạn của người dùng.");
            return;
        }
        position = position.trim();

        if (!isAuthorized(request, response, position, claims)) {
            return;
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }

    /**
     * Checks if the request targets a public API endpoint.
     */
    private boolean isPublicRoute(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri != null && uri.contains("/api/auth");
    }

    /**
     * Validates role permissions for protected routes.
     */
    private boolean isAuthorized(HttpServletRequest request, HttpServletResponse response,
                                String position, Claims claims) throws IOException {
        String uri = request.getRequestURI();
        if (uri == null) {
            return true;
        }

        // 1. Employee Management: STORE_OWNER or self-profile view (GET /api/employee/:id)
        if (uri.contains("/api/employee") || uri.contains("/api/employees")) {
            if ("STORE_OWNER".equalsIgnoreCase(position)) {
                return true;
            }
            if ("GET".equalsIgnoreCase(request.getMethod())) {
                String targetId = extractTargetId(request);
                String currentEmployeeId = claims.get("employeeId", String.class);
                if (targetId != null && currentEmployeeId != null && targetId.equalsIgnoreCase(currentEmployeeId.trim())) {
                    return true;
                }
                if (targetId != null) {
                    ResponseUtil.sendError(response, HttpServletResponse.SC_FORBIDDEN,
                            "Người gọi không phải là STORE_OWNER (và không phải chính chủ sở hữu tài khoản).");
                    return false;
                }
            }
            ResponseUtil.sendError(response, HttpServletResponse.SC_FORBIDDEN,
                    "Người gọi không phải là STORE_OWNER.");
            return false;
        }

        // 2. Customer Management: STORE_OWNER or CASHIER
        if (uri.contains("/api/customer")) {
            if ("STORE_OWNER".equalsIgnoreCase(position) || "CASHIER".equalsIgnoreCase(position)) {
                return true;
            }
            ResponseUtil.sendError(response, HttpServletResponse.SC_FORBIDDEN,
                    "Người dùng không có quyền truy cập thông tin khách hàng.");
            return false;
        }

        // 3. Purchase Receipt Management: STORE_OWNER or INVENTORY_MANAGER
        if (uri.contains("/api/purchase-receipt")) {
            if ("STORE_OWNER".equalsIgnoreCase(position) || "INVENTORY_MANAGER".equalsIgnoreCase(position)) {
                return true;
            }
            ResponseUtil.sendError(response, HttpServletResponse.SC_FORBIDDEN,
                    "Từ chối truy cập: Bạn không có quyền quản lý nhập hàng hoặc chủ cửa hàng.");
            return false;
        }

        // 4. Inventory Audit / Stock Report Management: STORE_OWNER or INVENTORY_MANAGER
        if (uri.contains("/api/report")) {
            if ("STORE_OWNER".equalsIgnoreCase(position) || "INVENTORY_MANAGER".equalsIgnoreCase(position)) {
                return true;
            }
            ResponseUtil.sendError(response, HttpServletResponse.SC_FORBIDDEN,
                    "Từ chối truy cập: Bạn không có quyền quản lý kho hoặc chủ cửa hàng.");
            return false;
        }

        // 5. Product Management: STORE_OWNER, CASHIER, INVENTORY_MANAGER for GET
        if (uri.contains("/api/product")) {
            if ("GET".equalsIgnoreCase(request.getMethod())) {
                if (uri.contains("/low-stock")) {
                    if ("STORE_OWNER".equalsIgnoreCase(position) || "INVENTORY_MANAGER".equalsIgnoreCase(position)) {
                        return true;
                    }
                    ResponseUtil.sendError(response, HttpServletResponse.SC_FORBIDDEN,
                            "Từ chối truy cập: Bạn không có quyền xem cảnh báo tồn kho.");
                    return false;
                }
                if ("STORE_OWNER".equalsIgnoreCase(position) || "CASHIER".equalsIgnoreCase(position) || "INVENTORY_MANAGER".equalsIgnoreCase(position)) {
                    return true;
                }
            } else if ("PATCH".equalsIgnoreCase(request.getMethod())) {
                if ("STORE_OWNER".equalsIgnoreCase(position) || "INVENTORY_MANAGER".equalsIgnoreCase(position)) {
                    return true;
                }
                ResponseUtil.sendError(response, HttpServletResponse.SC_FORBIDDEN,
                        "Từ chối truy cập: Người gọi không có quyền cập nhật sản phẩm.");
                return false;
            }
            ResponseUtil.sendError(response, HttpServletResponse.SC_FORBIDDEN,
                    "Từ chối truy cập: Bạn không có quyền truy cập sản phẩm.");
            return false;
        }

        return true;
    }

    /**
     * Extracts path parameter ID from request (e.g. /api/employee/{id}).
     */
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
