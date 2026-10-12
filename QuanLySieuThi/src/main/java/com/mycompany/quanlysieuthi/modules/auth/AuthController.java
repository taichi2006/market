package com.mycompany.quanlysieuthi.modules.auth;

import com.google.gson.JsonSyntaxException;
import com.mycompany.quanlysieuthi.common.util.RequestUtil;
import com.mycompany.quanlysieuthi.util.CookieUtil;
import com.mycompany.quanlysieuthi.util.ResponseUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(name = "AuthController", urlPatterns = { "/api/auth/*" })
public class AuthController extends HttpServlet {

    private final AuthService authService = new AuthService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = req.getPathInfo();
        if (path == null || path.isEmpty()) {
            String servletPath = req.getServletPath();
            if (servletPath != null && servletPath.contains("/")) {
                path = servletPath.substring(servletPath.lastIndexOf("/"));
            }
        }

        if ("/login".equalsIgnoreCase(path)) {
            handleLogin(req, resp);
        } else if ("/logout".equalsIgnoreCase(path)) {
            handleLogout(req, resp);
        } else if ("/refresh".equalsIgnoreCase(path)) {
            handleRefresh(req, resp);
        } else {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found: " + path);
        }
    }

    private void handleLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            AuthRequest.Login loginRequest = RequestUtil.parseJson(req, AuthRequest.Login.class);
            if (loginRequest == null) {
                ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "thiếu input");
                return;
            }

            AuthResponse.LoginResult result = authService.login(loginRequest);

            // Gửi refreshtoken về trong cookie (HttpOnly)
            CookieUtil.addRefreshTokenCookie(req, resp, result.getRefreshToken());

            // Access Token trả về trong Response Body
            ResponseUtil.sendSuccess(resp, HttpServletResponse.SC_OK, "Đăng nhập thành công", result.getResponseData());

        } catch (JsonSyntaxException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "thiếu input");
        } catch (IllegalArgumentException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (SecurityException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
        } catch (Exception e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Lỗi hệ thống: " + e.getMessage());
        }
    }

    private void handleLogout(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String refreshToken = CookieUtil.getCookieValue(req, CookieUtil.REFRESH_TOKEN_COOKIE_NAME);
            authService.logout(refreshToken);

            // Xóa refreshtoken khỏi cookie
            CookieUtil.deleteRefreshTokenCookie(req, resp);

            ResponseUtil.sendSuccess(resp, HttpServletResponse.SC_OK, "Đăng xuất thành công");
        } catch (Exception e) {
            CookieUtil.deleteRefreshTokenCookie(req, resp);
            ResponseUtil.sendSuccess(resp, HttpServletResponse.SC_OK, "Đăng xuất thành công");
        }
    }

    private void handleRefresh(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String refreshToken = CookieUtil.getCookieValue(req, CookieUtil.REFRESH_TOKEN_COOKIE_NAME);

            if (refreshToken == null || refreshToken.trim().isEmpty()) {
                ResponseUtil.sendError(resp, HttpServletResponse.SC_UNAUTHORIZED, "không tìm thấy token trong cookie");
                return;
            }

            String newAccessToken = authService.refresh(refreshToken);
            AuthResponse.RefreshResponseData responseData = new AuthResponse.RefreshResponseData(newAccessToken);

            ResponseUtil.sendSuccess(resp, HttpServletResponse.SC_OK, responseData);

        } catch (IllegalArgumentException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
        } catch (SecurityException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_FORBIDDEN, e.getMessage());
        } catch (Exception e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Lỗi hệ thống: " + e.getMessage());
        }
    }
}
