package com.mycompany.quanlysieuthi.customer;

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
 * Filter to restrict access to STORE_OWNER and CASHIER roles only.
 */
public class StaffRoleFilter implements Filter {

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
                && !"CASHIER".equalsIgnoreCase(position.trim()))) {
            ResponseUtil.sendError(response, HttpServletResponse.SC_FORBIDDEN,
                    "Người dùng không có quyền truy cập thông tin khách hàng.");
            return;
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {
    }
}
