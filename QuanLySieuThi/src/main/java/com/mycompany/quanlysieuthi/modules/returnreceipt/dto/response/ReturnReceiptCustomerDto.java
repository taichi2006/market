package com.mycompany.quanlysieuthi.modules.returnreceipt.dto.response;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.annotations.JsonAdapter;

import java.lang.reflect.Type;

/**
 * Unified Customer information DTO reused across all return receipt responses
 * (POST creation and GET detail).
 */
@JsonAdapter(ReturnReceiptCustomerDto.Serializer.class)
public class ReturnReceiptCustomerDto {

    private String customerId;
    private String fullName;
    private String phoneNumber;
    private Integer remainingPoints;

    public ReturnReceiptCustomerDto() {
    }

    public ReturnReceiptCustomerDto(String customerId, String fullName, Integer remainingPoints) {
        this.customerId = customerId;
        this.fullName = fullName;
        this.remainingPoints = remainingPoints;
    }

    public ReturnReceiptCustomerDto(String customerId, String fullName, String phoneNumber) {
        this.customerId = customerId;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
    }

    public ReturnReceiptCustomerDto(String customerId, String fullName, String phoneNumber, Integer remainingPoints) {
        this.customerId = customerId;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.remainingPoints = remainingPoints;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Integer getRemainingPoints() {
        return remainingPoints;
    }

    public void setRemainingPoints(Integer remainingPoints) {
        this.remainingPoints = remainingPoints;
    }

    public static class Serializer implements JsonSerializer<ReturnReceiptCustomerDto> {
        @Override
        public JsonElement serialize(ReturnReceiptCustomerDto src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject obj = new JsonObject();
            if (src.getCustomerId() != null) {
                obj.addProperty("customerId", src.getCustomerId());
            }
            if (src.getFullName() != null) {
                obj.addProperty("fullName", src.getFullName());
            }
            if (src.getPhoneNumber() != null) {
                obj.addProperty("phoneNumber", src.getPhoneNumber());
            }
            if (src.getRemainingPoints() != null) {
                obj.addProperty("remainingPoints", src.getRemainingPoints());
            }
            return obj;
        }
    }
}
