package com.vpcp.gateway;

/**
 * Send Document Response Model
 */
public class SendDocumentResponse {
    private boolean success;
    private String message;
    private DocumentInfo document;
    
    public SendDocumentResponse() {
    }
    
    public SendDocumentResponse(boolean success, String message, DocumentInfo document) {
        this.success = success;
        this.message = message;
        this.document = document;
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
    
    public DocumentInfo getDocument() {
        return document;
    }
    
    public void setDocument(DocumentInfo document) {
        this.document = document;
    }
}

