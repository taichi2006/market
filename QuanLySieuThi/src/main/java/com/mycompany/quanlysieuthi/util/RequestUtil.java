package com.mycompany.quanlysieuthi.util;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import jakarta.servlet.http.HttpServletRequest;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * Standard utility for parsing query parameters, path variables, client IP, and request body.
 */
public final class RequestUtil {

    private static final Gson GSON = new Gson();

    private RequestUtil() {
    }

    /**
     * Parses integer page parameter (default 1 if missing or invalid).
     */
    public static int parsePage(HttpServletRequest req) {
        return getPage(req, 1);
    }

    public static int getPage(HttpServletRequest req, int defaultVal) {
        String pageParam = req.getParameter("page");
        if (pageParam == null || pageParam.trim().isEmpty()) {
            return defaultVal;
        }
        try {
            int page = Integer.parseInt(pageParam.trim());
            return page > 0 ? page : defaultVal;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Tham số page phải là số nguyên dương");
        }
    }

    /**
     * Parses integer limit parameter (default 10 if missing or invalid).
     */
    public static int parseLimit(HttpServletRequest req) {
        return getLimit(req, 10);
    }

    public static int getLimit(HttpServletRequest req, int defaultVal) {
        String limitParam = req.getParameter("limit");
        if (limitParam == null || limitParam.trim().isEmpty()) {
            return defaultVal;
        }
        try {
            int limit = Integer.parseInt(limitParam.trim());
            return limit >= 0 ? limit : defaultVal;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Tham số limit phải là số nguyên");
        }
    }

    /**
     * Extracts path parameter ID from request URL (e.g. /api/employee/{id} -> {id}).
     */
    public static String extractPathId(HttpServletRequest req) {
        String pathInfo = req.getPathInfo();
        if (pathInfo != null && !pathInfo.trim().isEmpty() && !pathInfo.trim().equals("/")) {
            String clean = pathInfo.trim();
            if (clean.startsWith("/")) {
                clean = clean.substring(1);
            }
            if (clean.endsWith("/")) {
                clean = clean.substring(0, clean.length() - 1);
            }
            String[] segments = clean.split("/");
            if (segments.length > 0 && !segments[0].isEmpty()) {
                return segments[0];
            }
        }

        String uri = req.getRequestURI();
        if (uri != null) {
            String[] parts = uri.split("/");
            if (parts.length > 0) {
                String last = parts[parts.length - 1];
                if (!last.isEmpty()) {
                    return last;
                }
            }
        }
        return null;
    }

    public static <T> T parseJson(HttpServletRequest req, Class<T> clazz) throws IOException {
        return parseJsonBody(req, clazz);
    }

    /**
     * Reads request body and parses into DTO using Gson.
     */
    public static <T> T parseJsonBody(HttpServletRequest req, Class<T> clazz) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        String body = sb.toString().trim();
        if (body.isEmpty()) {
            return null;
        }
        try {
            return GSON.fromJson(body, clazz);
        } catch (JsonSyntaxException e) {
            throw new IllegalArgumentException("Định dạng JSON không hợp lệ: " + e.getMessage(), e);
        }
    }

    /**
     * Extracts client real IP taking into account reverse proxies (X-Forwarded-For).
     */
    public static String extractClientIp(HttpServletRequest req) {
        String ip = req.getHeader("X-Forwarded-For");
        if (ip == null || ip.trim().isEmpty() || "unknown".equalsIgnoreCase(ip.trim())) {
            ip = req.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.trim().isEmpty() || "unknown".equalsIgnoreCase(ip.trim())) {
            ip = req.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.trim().isEmpty() || "unknown".equalsIgnoreCase(ip.trim())) {
            ip = req.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return (ip != null) ? ip.trim() : "127.0.0.1";
    }
}
