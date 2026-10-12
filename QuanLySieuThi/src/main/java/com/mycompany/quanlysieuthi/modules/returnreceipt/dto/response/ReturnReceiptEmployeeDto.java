package com.mycompany.quanlysieuthi.modules.returnreceipt.dto.response;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.annotations.JsonAdapter;

import java.lang.reflect.Type;

/**
 * Unified Employee information DTO reused across all return receipt responses
 * (POST, GET list, and GET detail).
 */
@JsonAdapter(ReturnReceiptEmployeeDto.Serializer.class)
public class ReturnReceiptEmployeeDto {

    private String employeeId;
    private String fullName;
    private String username;

    public ReturnReceiptEmployeeDto() {
    }

    public ReturnReceiptEmployeeDto(String employeeId, String fullName) {
        this.employeeId = employeeId;
        this.fullName = fullName;
    }

    public ReturnReceiptEmployeeDto(String employeeId, String fullName, String username) {
        this.employeeId = employeeId;
        this.fullName = fullName;
        this.username = username;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public static class Serializer implements JsonSerializer<ReturnReceiptEmployeeDto> {
        @Override
        public JsonElement serialize(ReturnReceiptEmployeeDto src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject obj = new JsonObject();
            if (src.getEmployeeId() != null) {
                obj.addProperty("employeeId", src.getEmployeeId());
            }
            if (src.getFullName() != null) {
                obj.addProperty("fullName", src.getFullName());
            }
            if (src.getUsername() != null) {
                obj.addProperty("username", src.getUsername());
            }
            return obj;
        }
    }
}
