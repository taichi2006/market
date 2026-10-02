package com.mycompany.quanlysieuthi.position;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service handling business logic and bidirectional mapping for Position.
 */
public class PositionService {

    private final PositionDao positionDao;

    public PositionService() {
        this.positionDao = new PositionDao();
    }

    public PositionService(PositionDao positionDao) {
        this.positionDao = positionDao;
    }

    public List<PositionResponse> getAllPositions() {
        return positionDao.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public PositionResponse getPositionById(String id) {
        return positionDao.findById(id)
                .map(this::toResponse)
                .orElse(null);
    }

    public PositionResponse createPosition(PositionRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Position request payload cannot be null");
        }
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Position name is required and cannot be empty");
        }

        Position position = toEntity(request);
        Position savedPosition = positionDao.save(position);
        return toResponse(savedPosition);
    }

    public Position toEntity(PositionRequest request) {
        if (request == null) {
            return null;
        }
        String positionId = (request.getId() != null && !request.getId().trim().isEmpty())
                ? request.getId().trim()
                : UUID.randomUUID().toString();

        return new Position(positionId, request.getName().trim());
    }

    public PositionResponse toResponse(Position entity) {
        if (entity == null) {
            return null;
        }
        return new PositionResponse(entity.getId(), entity.getName());
    }
}
