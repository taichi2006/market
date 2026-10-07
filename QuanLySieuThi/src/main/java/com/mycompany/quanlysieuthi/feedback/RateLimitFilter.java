package com.mycompany.quanlysieuthi.feedback;

import com.mycompany.quanlysieuthi.util.ResponseUtil;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Filter to limit requests per IP for feedback submission (anti-spam).
 * Maximum 5 requests per minute per IP.
 */
public class RateLimitFilter implements Filter {

    public static final int MAX_REQUESTS = 5;
    public static final long WINDOW_MILLIS = 60 * 1000L; // 1 minute

    private static final Map<String, Deque<Long>> IP_REQUEST_LOG = new ConcurrentHashMap<>();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        // Rate limit only applies to POST /api/feedback
        if ("POST".equalsIgnoreCase(request.getMethod())) {
            String clientIp = extractClientIp(request);
            if (!isAllowed(clientIp)) {
                ResponseUtil.sendError(response, 429, "Gửi quá nhiều đánh giá liên tục từ cùng một IP/thiết bị.");
                return;
            }
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }

    public static synchronized boolean isAllowed(String ip) {
        long now = System.currentTimeMillis();
        Deque<Long> timestamps = IP_REQUEST_LOG.computeIfAbsent(ip, k -> new ArrayDeque<>());

        // Evict expired timestamps
        while (!timestamps.isEmpty() && now - timestamps.peekFirst() > WINDOW_MILLIS) {
            timestamps.pollFirst();
        }

        if (timestamps.size() >= MAX_REQUESTS) {
            return false;
        }

        timestamps.addLast(now);
        return true;
    }

    public static void reset() {
        IP_REQUEST_LOG.clear();
    }

    public static String extractClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.trim().isEmpty() || "unknown".equalsIgnoreCase(ip.trim())) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.trim().isEmpty() || "unknown".equalsIgnoreCase(ip.trim())) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.trim().isEmpty() || "unknown".equalsIgnoreCase(ip.trim())) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return (ip != null) ? ip.trim() : "unknown";
    }

    @Override
    public void destroy() {
        IP_REQUEST_LOG.clear();
    }
}
