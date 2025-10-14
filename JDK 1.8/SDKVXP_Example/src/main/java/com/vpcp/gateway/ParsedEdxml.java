package com.vpcp.gateway;

import java.util.List;

public class ParsedEdxml {
    public static class PartyInfo {
        public String organId;
        public String organizationInCharge;
        public String organName;
    }

    public static class AttachmentInfo {
        public String attachmentName;
        public String contentType;
        public String contentId;
        public String decodedContent; // Nội dung đã decode từ base64 + unzip
    }

    public String documentId;
    public String codeNumber;
    public String codeNotation;
    public String place;
    public String promulgationDate;
    public String subject;
    public PartyInfo from;
    public List<PartyInfo> to;
    public List<AttachmentInfo> attachments;
}


