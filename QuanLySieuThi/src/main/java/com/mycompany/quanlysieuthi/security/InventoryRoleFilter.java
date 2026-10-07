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
 * Filter to restrict access to STORE_OWNER and INVENTORY_MANAGER roles only.
 */
public class InventoryRoleFilter implements Filter {

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
        if (position == null || (!"STORE_OWNER".equalsIgnoreCase(position.trim())
                && !"INVENTORY_MANAGER".equalsIgnoreCase(position.trim()))) {
            ResponseUtil.sendError(response, HttpServletResponse.SC_FORBIDDEN,
                    "Từ chối truy cập: Bạn không có quyền quản lý kho hoặc chủ cửa hàng.");
            return;
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {
    }
}
