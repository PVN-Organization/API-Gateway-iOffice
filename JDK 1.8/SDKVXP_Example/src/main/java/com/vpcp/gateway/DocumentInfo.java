package com.vpcp.gateway;

/**
 * Document Info Model
 */
public class DocumentInfo {
    private String docId;
    private String fileName;
    private String fromCode;
    private long timestamp;
    
    public DocumentInfo() {
    }
    
    public DocumentInfo(String docId, String fileName, String fromCode, long timestamp) {
        this.docId = docId;
        this.fileName = fileName;
        this.fromCode = fromCode;
        this.timestamp = timestamp;
    }
    
    // Getters and Setters
    public String getDocId() {
        return docId;
    }
    
    public void setDocId(String docId) {
        this.docId = docId;
    }
    
    public String getFileName() {
        return fileName;
    }
    
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    public String getFromCode() {
        return fromCode;
    }
    
    public void setFromCode(String fromCode) {
        this.fromCode = fromCode;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}

