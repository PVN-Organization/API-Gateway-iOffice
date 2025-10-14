package com.vpcp.gateway;

/**
 * Send Document Request Model
 */
public class SendDocumentRequest {
    private String fromCode;
    private String toCode;
    private String serviceType;
    private String messageType;
    private String fileName;
    private byte[] fileContent;
    private String metaJson; // optional JSON metadata overrides (EDXML)
    private boolean forceWrap; // force rebuild EDXML even if input is XML
    
    public SendDocumentRequest() {
    }
    
    public SendDocumentRequest(String fromCode, String toCode, String fileName, byte[] fileContent) {
        this.fromCode = fromCode;
        this.toCode = toCode;
        this.fileName = fileName;
        this.fileContent = fileContent;
        this.serviceType = "eDoc";
        this.messageType = "edoc";
    }
    
    // Getters and Setters
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
    
    public String getServiceType() {
        return serviceType;
    }
    
    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }
    
    public String getMessageType() {
        return messageType;
    }
    
    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }
    
    public String getFileName() {
        return fileName;
    }
    
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    public byte[] getFileContent() {
        return fileContent;
    }
    
    public void setFileContent(byte[] fileContent) {
        this.fileContent = fileContent;
    }

    public String getMetaJson() {
        return metaJson;
    }

    public void setMetaJson(String metaJson) {
        this.metaJson = metaJson;
    }

    public boolean isForceWrap() {
        return forceWrap;
    }

    public void setForceWrap(boolean forceWrap) {
        this.forceWrap = forceWrap;
    }
}

