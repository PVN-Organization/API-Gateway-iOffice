package com.vpcp.gateway;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

/**
 * EdXML Builder
 * 
 * Tự động wrap file bất kỳ vào EDXML format
 */
public class EdxmlBuilder {
    
    /**
     * Wrap file vào EDXML format
     * 
     * @param originalFile File gốc (TXT, PDF, DOCX, v.v.)
     * @param fromCode Mã đơn vị gửi
     * @param toCode Mã đơn vị nhận (optional)
     * @return Path to EDXML file
     */
    public static String wrapFileToEdxml(String originalFile, String fromCode, String toCode) throws IOException {
        return wrapFileToEdxml(originalFile, fromCode, toCode, null);
    }

    public static String wrapFileToEdxml(String originalFile, String fromCode, String toCode, EdxmlMeta meta) throws IOException {
        File file = new File(originalFile);
        
        // Read file content and encode base64
        byte[] fileBytes = java.nio.file.Files.readAllBytes(file.toPath());
        String base64Content = Base64.getEncoder().encodeToString(fileBytes);
        
        // Get file info
        String fileName = file.getName();
        String fileExtension = getFileExtension(fileName);
        long fileSize = file.length();
        
        // Build EDXML
        String edxml = buildEdxml(
            fromCode,
            toCode != null ? toCode : "vxp.saas.03",
            fileName,
            fileExtension,
            fileSize,
            base64Content,
            meta
        );
        
        // Save EDXML file
        String edxmlPath = originalFile + ".edxml";
        java.nio.file.Files.write(
            java.nio.file.Paths.get(edxmlPath),
            edxml.getBytes("UTF-8")
        );
        
        System.out.println("  ✅ EDXML created: " + edxmlPath);
        System.out.println("  📦 Wrapped file: " + fileName + " (" + fileSize + " bytes)");
        
        return edxmlPath;
    }
    
    /**
     * Build EDXML structure theo chuẩn VXP QCVN_102_2016
     */
    private static String buildEdxml(String fromCode, String toCode, 
                                     String fileName, String fileExt, 
                                     long fileSize, String base64Content,
                                     EdxmlMeta meta) {
        // Resolve effective from/to from meta if not provided
        String effectiveFrom = fromCode;
        if ((effectiveFrom == null || effectiveFrom.trim().isEmpty()) && meta != null && meta.from != null && meta.from.organId != null) {
            effectiveFrom = meta.from.organId;
        }
        String effectiveTo = toCode;
        if ((effectiveTo == null || effectiveTo.trim().isEmpty()) && meta != null && meta.to != null && !meta.to.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < meta.to.size(); i++) {
                EdxmlMeta.PartyMeta pm = meta.to.get(i);
                if (pm != null && pm.organId != null && !pm.organId.trim().isEmpty()) {
                    if (sb.length() > 0) sb.append(",");
                    sb.append(pm.organId.trim());
                }
            }
            if (sb.length() > 0) effectiveTo = sb.toString();
        }
        String timestamp = new SimpleDateFormat("yyyy/MM/dd").format(new Date());
        // Unique IDs for document and content
        String docCode = UUID.randomUUID().toString();
        String contentId = "cid:" + UUID.randomUUID().toString();
        String contentType = guessContentType(fileExt);
        
        StringBuilder edxml = new StringBuilder();
        edxml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        edxml.append("<edXML xmlns=\"http://www.mic.gov.vn/TBT/QCVN_102_2016\">\n");
        edxml.append("  <edXML:edXMLEnvelope xmlns:edXML=\"http://www.mic.gov.vn/TBT/QCVN_102_2016\">\n");
        edxml.append("    <edXML:edXMLHeader>\n");
        edxml.append("      <edXML:MessageHeader>\n");
        
        // From
        edxml.append("        <edXML:From>\n");
        edxml.append("          <edXML:OrganId>").append(effectiveFrom).append("</edXML:OrganId>\n");
        EdxmlMeta.PartyMeta fromMeta = meta != null ? meta.from : null;
        // Log chosen From fields (no fallback to code for name/incharge)
        String fromInCharge = (fromMeta != null && fromMeta.organizationInCharge != null) ? fromMeta.organizationInCharge : "";
        String fromName = (fromMeta != null && fromMeta.organName != null) ? fromMeta.organName : "";
        System.out.println("[EDXML] From.organId=" + effectiveFrom
            + ", organizationInCharge=" + fromInCharge
            + ", organName=" + fromName
        );
        edxml.append("          <edXML:OrganizationInCharge>").append(fromInCharge).append("</edXML:OrganizationInCharge>\n");
        edxml.append("          <edXML:OrganName>").append(fromName).append("</edXML:OrganName>\n");
        edxml.append("          <edXML:OrganAdd>").append(fromMeta != null && fromMeta.organAdd != null ? fromMeta.organAdd : defaultAddress(effectiveFrom)).append("</edXML:OrganAdd>\n");
        edxml.append("          <edXML:Email>").append(fromMeta != null && fromMeta.email != null ? fromMeta.email : defaultEmail(effectiveFrom)).append("</edXML:Email>\n");
        edxml.append("          <edXML:Telephone>").append(fromMeta != null && fromMeta.telephone != null ? fromMeta.telephone : defaultTelephone(effectiveFrom)).append("</edXML:Telephone>\n");
        edxml.append("          <edXML:Fax>").append(fromMeta != null && fromMeta.fax != null ? fromMeta.fax : defaultFax(effectiveFrom)).append("</edXML:Fax>\n");
        edxml.append("          <edXML:Website>").append(fromMeta != null && fromMeta.website != null ? fromMeta.website : defaultWebsite(effectiveFrom)).append("</edXML:Website>\n");
        edxml.append("        </edXML:From>\n");
        
        // To (support multi-recipients: comma-separated codes)
        if (toCode == null || toCode.trim().isEmpty()) {
            toCode = "";
        }
        String[] recipients = effectiveTo != null ? effectiveTo.split(",") : new String[0];
        for (String rc : recipients) {
            String rcCode = rc.trim();
            if (rcCode.isEmpty()) continue;
            // Try find override in meta.to list
            EdxmlMeta.PartyMeta tMeta = null;
            if (meta != null && meta.to != null) {
                for (EdxmlMeta.PartyMeta pm : meta.to) {
                    if (pm != null && pm.organId != null && pm.organId.equalsIgnoreCase(rcCode)) {
                        tMeta = pm; break;
                    }
                }
            }
            // Log chosen To fields (no fallback to code for name/incharge)
            String toInCharge = (tMeta != null && tMeta.organizationInCharge != null) ? tMeta.organizationInCharge : "";
            String toName = (tMeta != null && tMeta.organName != null) ? tMeta.organName : "";
            System.out.println("[EDXML] To.organId=" + rcCode
                + ", organizationInCharge=" + toInCharge
                + ", organName=" + toName
            );
            edxml.append("        <edXML:To>\n");
            edxml.append("          <edXML:OrganId>").append(rcCode).append("</edXML:OrganId>\n");
            edxml.append("          <edXML:OrganizationInCharge>").append(toInCharge).append("</edXML:OrganizationInCharge>\n");
            edxml.append("          <edXML:OrganName>").append(toName).append("</edXML:OrganName>\n");
            edxml.append("          <edXML:OrganAdd>").append(tMeta != null && tMeta.organAdd != null ? tMeta.organAdd : defaultAddress(rcCode)).append("</edXML:OrganAdd>\n");
            edxml.append("          <edXML:Email>").append(tMeta != null && tMeta.email != null ? tMeta.email : defaultEmail(rcCode)).append("</edXML:Email>\n");
            edxml.append("          <edXML:Telephone>").append(tMeta != null && tMeta.telephone != null ? tMeta.telephone : defaultTelephone(rcCode)).append("</edXML:Telephone>\n");
            edxml.append("          <edXML:Fax>").append(tMeta != null && tMeta.fax != null ? tMeta.fax : defaultFax(rcCode)).append("</edXML:Fax>\n");
            edxml.append("          <edXML:Website>").append(tMeta != null && tMeta.website != null ? tMeta.website : defaultWebsite(rcCode)).append("</edXML:Website>\n");
            edxml.append("        </edXML:To>\n");
        }
        
        // Document Info
        edxml.append("        <edXML:DocumentId>").append(docCode).append("</edXML:DocumentId>\n");
        edxml.append("        <edXML:Code>\n");
        edxml.append("          <edXML:CodeNumber>").append(new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())).append("</edXML:CodeNumber>\n");
        edxml.append("          <edXML:CodeNotation>").append(meta != null && meta.codeNotation != null ? meta.codeNotation : "GW-AUTO").append("</edXML:CodeNotation>\n");
        edxml.append("        </edXML:Code>\n");
        edxml.append("        <edXML:PromulgationInfo>\n");
        edxml.append("          <edXML:Place>").append(meta != null && meta.place != null ? meta.place : "Hà Nội").append("</edXML:Place>\n");
        edxml.append("          <edXML:PromulgationDate>").append(timestamp).append("</edXML:PromulgationDate>\n");
        edxml.append("        </edXML:PromulgationInfo>\n");
        edxml.append("        <edXML:DocumentType>\n");
        edxml.append("          <edXML:Type>1</edXML:Type>\n");
        edxml.append("          <edXML:TypeName>Công văn</edXML:TypeName>\n");
        edxml.append("        </edXML:DocumentType>\n");
        edxml.append("        <edXML:Subject>").append(meta != null && meta.subject != null ? meta.subject : fileName).append("</edXML:Subject>\n");
        edxml.append("        <edXML:Content>").append(meta != null && meta.content != null ? meta.content : "File uploaded via Gateway").append("</edXML:Content>\n");
        edxml.append("        <edXML:SignerInfo>\n");
        edxml.append("          <edXML:FullName>").append(meta != null && meta.signerFullName != null ? meta.signerFullName : "Gateway Auto").append("</edXML:FullName>\n");
        edxml.append("        </edXML:SignerInfo>\n");
        // OtherInfo (optional defaults)
        edxml.append("        <edXML:OtherInfo>\n");
        edxml.append("          <edXML:Priority>0</edXML:Priority>\n");
        edxml.append("          <edXML:SphereOfPromulgation>Lưu hành nội bộ</edXML:SphereOfPromulgation>\n");
        edxml.append("          <edXML:TyperNotation>TVC</edXML:TyperNotation>\n");
        edxml.append("          <edXML:PromulgationAmount>1</edXML:PromulgationAmount>\n");
        edxml.append("          <edXML:PageAmount>1</edXML:PageAmount>\n");
        edxml.append("          <edXML:Direction>false</edXML:Direction>\n");
        edxml.append("          <edXML:Appendixes>\n");
        edxml.append("            <edXML:Appendix>Phụ lục</edXML:Appendix>\n");
        edxml.append("          </edXML:Appendixes>\n");
        edxml.append("        </edXML:OtherInfo>\n");
        // SteeringType
        edxml.append("        <edXML:SteeringType>1</edXML:SteeringType>\n");
        // ToPlaces (optional)
        edxml.append("        <edXML:ToPlaces>\n");
        edxml.append("          <edXML:Place>Các bộ</edXML:Place>\n");
        edxml.append("        </edXML:ToPlaces>\n");
        edxml.append("      </edXML:MessageHeader>\n");
        // TraceHeaderList (single entry)
        edxml.append("      <edXML:TraceHeaderList>\n");
        edxml.append("        <edXML:TraceHeader>\n");
        edxml.append("          <edXML:OrganId>").append(fromCode).append("</edXML:OrganId>\n");
        edxml.append("          <edXML:Business>\n");
        edxml.append("            <edXML:BusinessName>Gửi văn bản</edXML:BusinessName>\n");
        edxml.append("            <edXML:BusinessCode>sendEdoc</edXML:BusinessCode>\n");
        edxml.append("          </edXML:Business>\n");
        edxml.append("          <edXML:StaffInfo>\n");
        edxml.append("            <edXML:StaffName>Gateway Auto</edXML:StaffName>\n");
        edxml.append("            <edXML:StaffCode>GW</edXML:StaffCode>\n");
        edxml.append("          </edXML:StaffInfo>\n");
        edxml.append("          <edXML:Timestamp>")
             .append(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date())).append("</edXML:Timestamp>\n");
        edxml.append("        </edXML:TraceHeader>\n");
        edxml.append("      </edXML:TraceHeaderList>\n");
        edxml.append("    </edXML:edXMLHeader>\n");
        
        // Body + Manifest
        edxml.append("    <edXML:edXMLBody>\n");
        edxml.append("      <edXML:edXMLManifest version=\"1.0\">\n");
        edxml.append("        <edXML:Reference>\n");
        edxml.append("          <edXML:ContentId>").append(contentId).append("</edXML:ContentId>\n");
        edxml.append("          <edXML:ContentType>").append(contentType).append("</edXML:ContentType>\n");
        edxml.append("          <edXML:AttachmentName>").append(fileName).append("</edXML:AttachmentName>\n");
        edxml.append("        </edXML:Reference>\n");
        edxml.append("      </edXML:edXMLManifest>\n");
        edxml.append("    </edXML:edXMLBody>\n");
        edxml.append("  </edXML:edXMLEnvelope>\n");
        // AttachmentEncoded with base64 content
        edxml.append("  <AttachmentEncoded>\n");
        edxml.append("    <Attachment>\n");
        edxml.append("      <ContentId>").append(contentId).append("</ContentId>\n");
        edxml.append("      <AttachmentName>").append(fileName).append("</AttachmentName>\n");
        edxml.append("      <ContentType>").append(contentType).append("</ContentType>\n");
        edxml.append("      <edXML:ContentTransferEncoded xmlns:edXML=\"http://www.mic.gov.vn/TBT/QCVN_102_2016\">").append(base64Content).append("</edXML:ContentTransferEncoded>\n");
        edxml.append("    </Attachment>\n");
        edxml.append("  </AttachmentEncoded>\n");
        edxml.append("</edXML>\n");
        
        return edxml.toString();
    }
    
    /**
     * Get file extension
     */
    private static String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot > 0 && lastDot < fileName.length() - 1) {
            return fileName.substring(lastDot + 1).toUpperCase();
        }
        return "UNKNOWN";
    }

    private static String guessContentType(String fileExtUpper) {
        String ext = fileExtUpper != null ? fileExtUpper.toUpperCase() : "";
        if ("PDF".equals(ext)) return "application/pdf";
        if ("XML".equals(ext)) return "application/xml";
        if ("JSON".equals(ext)) return "application/json";
        if ("DOC".equals(ext) || "DOCX".equals(ext)) return "application/msword";
        if ("TXT".equals(ext)) return "text/plain";
        return "application/octet-stream";
    }

    private static String defaultOrgName(String code) {
        // Simple fallback: use code as name; can be mapped to agency registry later
        if ("vxp.saas.03".equalsIgnoreCase(code)) return "Đơn vị test vxp 3";
        return code;
    }

    private static String defaultEmail(String code) {
        // Simple fallback email format
        if ("vxp.saas.03".equalsIgnoreCase(code)) return "contact-vxp3@example.vn";
        return code.replaceAll("[^a-zA-Z0-9]", "").toLowerCase() + "@example.vn";
    }

    private static String defaultAddress(String code) {
        if ("vxp.saas.03".equalsIgnoreCase(code)) return "Số 1, Hà Nội";
        return "Hà Nội";
    }

    private static String defaultTelephone(String code) {
        if ("vxp.saas.03".equalsIgnoreCase(code)) return "+84 24 1234 5678";
        return "(024)";
    }

    private static String defaultFax(String code) {
        if ("vxp.saas.03".equalsIgnoreCase(code)) return "+84 24 8765 4321";
        return "(024)";
    }

    private static String defaultWebsite(String code) {
        if ("vxp.saas.03".equalsIgnoreCase(code)) return "https://vxp3.example.vn";
        return "http://example.vn";
    }
}

