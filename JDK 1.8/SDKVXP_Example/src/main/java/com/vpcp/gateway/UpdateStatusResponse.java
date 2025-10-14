package com.vpcp.gateway;

/**
 * Update Status Response Model
 */
public class UpdateStatusResponse {
    private boolean success;
    private String message;
    
    public UpdateStatusResponse() {
    }
    
    public UpdateStatusResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
}

