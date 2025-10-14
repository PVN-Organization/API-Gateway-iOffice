package com.vpcp.gateway;

/**
 * Document Summary Model
 */
public class DocumentSummary {
    private String id;
    private String fromCode;
    private String toCode;
    private String messageType;
    private String serviceType;
    
    public DocumentSummary() {
    }
    
    public DocumentSummary(String id, String fromCode, String toCode, 
                          String messageType, String serviceType) {
        this.id = id;
        this.fromCode = fromCode;
        this.toCode = toCode;
        this.messageType = messageType;
        this.serviceType = serviceType;
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getFromCode() {
        return fromCode;
    }
    
    public void setFromCode(String fromCode) {
        this.fromCode = fromCode;
    }
    
    public String getToCode() {
        return toCode;
    }
    
    public void setToCode(String toCode) {
        this.toCode = toCode;
    }
    
    public String getMessageType() {
        return messageType;
    }
    
    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }
    
    public String getServiceType() {
        return serviceType;
    }
    
    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }
}

