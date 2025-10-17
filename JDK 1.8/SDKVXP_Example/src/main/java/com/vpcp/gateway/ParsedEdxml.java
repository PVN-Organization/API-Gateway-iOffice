package com.vpcp.gateway;

import java.util.List;

public class ParsedEdxml {
    public static class PartyInfo {
        public String organId;
        public String organizationInCharge;
        public String organName;
        public String organAdd;
        public String email;
        public String telephone;
        public String fax;
        public String website;
    }

    public static class AttachmentInfo {
        public String attachmentName;
        public String contentType;
        public String contentId;
        public String decodedContent; // Nội dung đã decode từ base64 + unzip
    }

    public static class ResponseForInfo {
        public String organId;
        public String code;
        public String documentId;
        public String promulgationDate;
    }

    public static class StaffInfo {
        public String department;
        public String staff;
        public String email;
        public String mobile;
    }

    public static class SignerInfo {
        public String fullName;
        public String position;
    }

    public static class OtherInfo {
        public String priority;
        public String sphereOfPromulgation;
        public String typerNotation;
        public String promulgationAmount;
        public String pageAmount;
        public String direction;
        public List<String> appendixes;
    }

    // Basic fields
    public String documentId;
    public String codeNumber;
    public String codeNotation;
    public String place;
    public String promulgationDate;
    public String subject;
    public String content;
    
    // DocumentType
    public String typeName;
    public String type;
    public String typeDetail;
    
    // Parties
    public PartyInfo from;
    public List<PartyInfo> to;
    
    // Attachments
    public List<AttachmentInfo> attachments;
    
    // Response & Status (for status EDXML)
    public ResponseForInfo responseFor;
    public StaffInfo staffInfo;
    public String statusCode;
    public String description;
    public String timestamp;
    
    // Signer
    public SignerInfo signerInfo;
    
    // Other
    public OtherInfo otherInfo;
    public String steeringType;
    public List<String> toPlaces;
}


