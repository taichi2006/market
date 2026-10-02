package com.mycompany.quanlysieuthi.position;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.mycompany.quanlysieuthi.util.ResponseUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

/**
 * RESTful API Controller for Position resource:
 * - GET  /api/positions       /  /positions
 * - GET  /api/positions/{id}  /  /positions/{id}
 * - POST /api/positions       /  /positions
 */
@WebServlet(name = "PositionController", urlPatterns = {"/api/positions", "/api/positions/*"})
public class PositionController extends HttpServlet {

    private final PositionService positionService = new PositionService();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String positionId = extractIdFromPath(req);

            if (positionId == null) {
                // GET /api/positions : Danh sách toàn bộ chức vụ
                List<PositionResponse> positions = positionService.getAllPositions();
                ResponseUtil.sendJson(resp, HttpServletResponse.SC_OK, positions);
            } else {
                // GET /api/positions/{id} : Chi tiết chức vụ theo ID
                PositionResponse position = positionService.getPositionById(positionId);
                if (position == null) {
                    ResponseUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND,
                            "Position not found with ID: " + positionId);
                } else {
                    ResponseUtil.sendJson(resp, HttpServletResponse.SC_OK, position);
                }
            }
        } catch (Exception e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Failed to retrieve positions: " + e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            PositionRequest requestDto = gson.fromJson(req.getReader(), PositionRequest.class);
            if (requestDto == null) {
                ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Request body is empty");
                return;
            }

            PositionResponse responseDto = positionService.createPosition(requestDto);
            ResponseUtil.sendJson(resp, HttpServletResponse.SC_CREATED, responseDto);
        } catch (JsonSyntaxException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST,
                    "Malformed JSON payload: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            ResponseUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Failed to create position: " + e.getMessage());
        }
    }

    private String extractIdFromPath(HttpServletRequest req) {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.trim().isEmpty() || "/".equals(pathInfo.trim())) {
            return null;
        }
        String cleanPath = pathInfo.trim();
        if (cleanPath.startsWith("/")) {
            cleanPath = cleanPath.substring(1);
        }
        if (cleanPath.endsWith("/")) {
            cleanPath = cleanPath.substring(0, cleanPath.length() - 1);
        }
        return cleanPath.isEmpty() ? null : cleanPath;
    }
}
