package com.vpcp.gateway;

/**
 * Get Document Response Model
 */
public class GetDocumentResponse {
    private boolean success;
    private String message;
    private String data;
    private String filePath;
    
    public GetDocumentResponse() {
    }
    
    public GetDocumentResponse(boolean success, String message, String data, String filePath) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.filePath = filePath;
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
    
    public String getData() {
        return data;
    }
    
    public void setData(String data) {
        this.data = data;
    }
    
    public String getFilePath() {
        return filePath;
    }
    
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}

