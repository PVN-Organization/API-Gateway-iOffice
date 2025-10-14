package com.vpcp.gateway;

import java.util.List;

/**
 * Received Documents Response Model
 */
public class ReceivedDocumentsResponse {
    private int total;
    private List<DocumentSummary> documents;
    
    public ReceivedDocumentsResponse() {
    }
    
    public ReceivedDocumentsResponse(int total, List<DocumentSummary> documents) {
        this.total = total;
        this.documents = documents;
    }
    
    public int getTotal() {
        return total;
    }
    
    public void setTotal(int total) {
        this.total = total;
    }
    
    public List<DocumentSummary> getDocuments() {
        return documents;
    }
    
    public void setDocuments(List<DocumentSummary> documents) {
        this.documents = documents;
    }
}

