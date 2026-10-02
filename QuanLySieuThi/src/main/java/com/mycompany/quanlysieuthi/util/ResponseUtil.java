package com.mycompany.quanlysieuthi.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Utility class for sending standardized JSON HTTP responses.
 */
public class ResponseUtil {

    private static final Gson GSON = new GsonBuilder()
            .serializeNulls()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .create();

    private ResponseUtil() {
    }

    public static void sendJson(HttpServletResponse response, int statusCode, Object data) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(statusCode);

        PrintWriter writer = response.getWriter();
        writer.write(GSON.toJson(data));
        writer.flush();
    }

    public static void sendError(HttpServletResponse response, int statusCode, String message) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(statusCode);

        Map<String, Object> errorPayload = new LinkedHashMap<>();
        errorPayload.put("success", false);
        errorPayload.put("statusCode", statusCode);
        errorPayload.put("message", message);

        PrintWriter writer = response.getWriter();
        writer.write(GSON.toJson(errorPayload));
        writer.flush();
    }
}
