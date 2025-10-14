package com.vpcp.gateway;

import java.util.List;

/**
 * Agency List Response Model
 * 
 * Response cho danh sách agencies
 */
public class AgencyListResponse {
    private int total;
    private List<AgencyDetail> agencies;
    
    public AgencyListResponse() {
    }
    
    public AgencyListResponse(int total, List<AgencyDetail> agencies) {
        this.total = total;
        this.agencies = agencies;
    }
    
    public int getTotal() {
        return total;
    }
    
    public void setTotal(int total) {
        this.total = total;
    }
    
    public List<AgencyDetail> getAgencies() {
        return agencies;
    }
    
    public void setAgencies(List<AgencyDetail> agencies) {
        this.agencies = agencies;
    }
}

