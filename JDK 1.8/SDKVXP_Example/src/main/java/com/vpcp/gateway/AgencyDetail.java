package com.vpcp.gateway;

/**
 * Agency Detail Model
 * 
 * Model cho thông tin chi tiết đơn vị
 */
public class AgencyDetail {
    private String id;
    private String code;
    private String name;
    private String pcode;
    private String mail;
    private String mobile;
    private String fax;
    
    public AgencyDetail() {
    }
    
    public AgencyDetail(String id, String code, String name, String pcode, 
                       String mail, String mobile, String fax) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.pcode = pcode;
        this.mail = mail;
        this.mobile = mobile;
        this.fax = fax;
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
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
    
    public String getPcode() {
        return pcode;
    }
    
    public void setPcode(String pcode) {
        this.pcode = pcode;
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

