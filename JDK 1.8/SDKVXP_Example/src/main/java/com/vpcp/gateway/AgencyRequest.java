package com.vpcp.gateway;

/**
 * Agency Request Model
 * 
 * Request model cho register/update agency
 */
public class AgencyRequest {
    private String id;
    private String pcode;
    private String code;
    private String name;
    private String mail;
    private String mobile;
    private String fax;
    
    public AgencyRequest() {
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getPcode() {
        return pcode;
    }
    
    public void setPcode(String pcode) {
        this.pcode = pcode;
    }
    
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getMail() {
        return mail;
    }
    
    public void setMail(String mail) {
        this.mail = mail;
    }
    
    public String getMobile() {
        return mobile;
    }
    
    public void setMobile(String mobile) {
        this.mobile = mobile;
    }
    
    public String getFax() {
        return fax;
    }
    
    public void setFax(String fax) {
        this.fax = fax;
    }
}

