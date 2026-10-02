package com.mycompany.quanlysieuthi.position;

/**
 * DTO for receiving create/update position requests from clients.
 */
public class PositionRequest {

    private String id;
    private String name;

    public PositionRequest() {
    }

    public PositionRequest(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
