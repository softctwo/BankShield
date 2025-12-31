package com.bankshield.auth.dto;

public class RegisterResponse {
    private Long userId;
    private String message;
    private boolean success;
    
    public RegisterResponse() {}
    
    public RegisterResponse(Long userId, String message, boolean success) {
        this.userId = userId;
        this.message = message;
        this.success = success;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
}