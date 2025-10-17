package com.vpcp.gateway;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Manual EDXML Builder - Build EDXML XML manually to support multiple attachments
 * Based on QCVN 102:2016 standard
 */
public class ManualEdxmlBuilder {

    public static String buildAggregatedEdxml(String outputDir,
                                              String fromCode,
                                              List<String> toCodes,
                                              List<FilePart> files,
                                              EdxmlMeta meta) throws Exception {
        File outDir = new File(outputDir);
        if (!outDir.exists()) outDir.mkdirs();

        // Generate unique document ID
        String docId = UUID.randomUUID().toString();
        String codeNumber = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String codeNotation = meta != null && meta.codeNotation != null ? meta.codeNotation : "GW-AUTO";
        String place = meta != null && meta.place != null ? meta.place : "Hà Nội";
        String promulgationDate = new SimpleDateFormat("yyyy/MM/dd").format(new Date());
        String subject = meta != null && meta.subject != null ? meta.subject : codeNumber;
        String content = meta != null && meta.content != null ? meta.content : "File uploaded via Gateway";
        String signerName = meta != null && meta.signerFullName != null ? meta.signerFullName : "Gateway Auto";

        // From info
        String fromOrganName = meta != null && meta.from != null && meta.from.organName != null ? meta.from.organName : fromCode;
        String fromOrganizationInCharge = meta != null && meta.from != null && meta.from.organizationInCharge != null ? meta.from.organizationInCharge : fromOrganName;

        System.out.println("[MANUAL-EDXML] Building EDXML with " + files.size() + " attachments");
        System.out.println("[MANUAL-EDXML] From: " + fromCode + " (" + fromOrganName + ")");

        // Start building XML
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<edXML xmlns=\"http://www.mic.gov.vn/TBT/QCVN_102_2016\">\n");
        xml.append("  <edXML:edXMLEnvelope xmlns:edXML=\"http://www.mic.gov.vn/TBT/QCVN_102_2016\">\n");
        xml.append("    <edXML:edXMLHeader>\n");
        xml.append("      <edXML:MessageHeader>\n");

        // From
        xml.append("        <edXML:From>\n");
        xml.append("          <edXML:OrganId>").append(escapeXml(fromCode)).append("</edXML:OrganId>\n");
        xml.append("          <edXML:OrganizationInCharge>").append(escapeXml(fromOrganizationInCharge)).append("</edXML:OrganizationInCharge>\n");
        xml.append("          <edXML:OrganName>").append(escapeXml(fromOrganName)).append("</edXML:OrganName>\n");
        xml.append("        </edXML:From>\n");

        // To (multiple)
        for (String toCode : toCodes) {
            String toOrganName = toCode;
            String toOrganizationInCharge = toOrganName;
            if (meta != null && meta.to != null) {
                for (EdxmlMeta.PartyMeta pm : meta.to) {
                    if (pm != null && pm.organId != null && pm.organId.equalsIgnoreCase(toCode.trim())) {
                        toOrganName = pm.organName != null ? pm.organName : toCode;
                        toOrganizationInCharge = pm.organizationInCharge != null ? pm.organizationInCharge : toOrganName;
                        break;
                    }
                }
            }
            xml.append("        <edXML:To>\n");
            xml.append("          <edXML:OrganId>").append(escapeXml(toCode)).append("</edXML:OrganId>\n");
            xml.append("          <edXML:OrganizationInCharge>").append(escapeXml(toOrganizationInCharge)).append("</edXML:OrganizationInCharge>\n");
            xml.append("          <edXML:OrganName>").append(escapeXml(toOrganName)).append("</edXML:OrganName>\n");
            xml.append("        </edXML:To>\n");
        }

        // Document ID, Code, etc.
        xml.append("        <edXML:DocumentId>").append(escapeXml(docId)).append("</edXML:DocumentId>\n");
        xml.append("        <edXML:Code>\n");
        xml.append("          <edXML:CodeNumber>").append(escapeXml(codeNumber)).append("</edXML:CodeNumber>\n");
        xml.append("          <edXML:CodeNotation>").append(escapeXml(codeNotation)).append("</edXML:CodeNotation>\n");
        xml.append("        </edXML:Code>\n");
        xml.append("        <edXML:PromulgationInfo>\n");
        xml.append("          <edXML:Place>").append(escapeXml(place)).append("</edXML:Place>\n");
        xml.append("          <edXML:PromulgationDate>").append(escapeXml(promulgationDate)).append("</edXML:PromulgationDate>\n");
        xml.append("        </edXML:PromulgationInfo>\n");
        xml.append("        <edXML:DocumentType>\n");
        xml.append("          <edXML:Type>1</edXML:Type>\n");
        xml.append("          <edXML:TypeDetail>0</edXML:TypeDetail>\n");
        xml.append("          <edXML:TypeName>Công văn</edXML:TypeName>\n");
        xml.append("        </edXML:DocumentType>\n");
        xml.append("        <edXML:Subject>").append(escapeXml(subject)).append("</edXML:Subject>\n");
        xml.append("        <edXML:Content>").append(escapeXml(content)).append("</edXML:Content>\n");
        xml.append("        <edXML:SignerInfo>\n");
        xml.append("          <edXML:FullName>").append(escapeXml(signerName)).append("</edXML:FullName>\n");
        xml.append("        </edXML:SignerInfo>\n");
        xml.append("        <edXML:OtherInfo>\n");
        xml.append("          <edXML:Priority>0</edXML:Priority>\n");
        xml.append("          <edXML:SphereOfPromulgation>Lưu hành nội bộ</edXML:SphereOfPromulgation>\n");
        xml.append("          <edXML:TyperNotation>TVC</edXML:TyperNotation>\n");
        xml.append("          <edXML:PromulgationAmount>1</edXML:PromulgationAmount>\n");
        xml.append("          <edXML:PageAmount>1</edXML:PageAmount>\n");
        xml.append("          <edXML:Direction>false</edXML:Direction>\n");
        xml.append("        </edXML:OtherInfo>\n");
        xml.append("        <edXML:SteeringType>0</edXML:SteeringType>\n");
        xml.append("      </edXML:MessageHeader>\n");
        xml.append("      <edXML:TraceHeaderList>\n");
        xml.append("        <edXML:Bussiness>\n");
        xml.append("          <edXML:BussinessDocType>0</edXML:BussinessDocType>\n");
        xml.append("          <edXML:Paper>0</edXML:Paper>\n");
        xml.append("        </edXML:Bussiness>\n");
        xml.append("        <edXML:TraceHeader>\n");
        xml.append("          <edXML:OrganId>").append(escapeXml(fromCode)).append("</edXML:OrganId>\n");
        xml.append("          <edXML:Timestamp>").append(escapeXml(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()))).append("</edXML:Timestamp>\n");
        xml.append("        </edXML:TraceHeader>\n");
        xml.append("      </edXML:TraceHeaderList>\n");
        xml.append("    </edXML:edXMLHeader>\n");

        // Body - Manifest with multiple References
        xml.append("    <edXML:edXMLBody>\n");
        xml.append("      <edXML:edXMLManifest version=\"1.0\">\n");

        // Prepare attachment data
        class AttachmentData {
            String contentId;
            String name;
            String contentType;
            String base64Content;
        }
        java.util.List<AttachmentData> attachmentDataList = new java.util.ArrayList<>();

        int idx = 1;
        for (FilePart fp : files) {
            if (fp == null || fp.getContent() == null) continue;
            
            AttachmentData ad = new AttachmentData();
            ad.contentId = "cid:" + UUID.randomUUID().toString();
            ad.name = fp.getFileName() != null ? fp.getFileName() : ("file_" + idx + ".bin");
            ad.contentType = guessContentType(ad.name);
            
            // Zip and encode content
            ad.base64Content = zipAndEncode(fp.getContent(), ad.name);
            
            attachmentDataList.add(ad);
            System.out.println("[MANUAL-EDXML] Attachment " + idx + ": " + ad.name + " (" + ad.contentId + ")");
            
            // Add Reference
            xml.append("        <edXML:Reference>\n");
            xml.append("          <edXML:ContentId>").append(escapeXml(ad.contentId)).append("</edXML:ContentId>\n");
            xml.append("          <edXML:ContentType>").append(escapeXml(ad.contentType)).append("</edXML:ContentType>\n");
            xml.append("          <edXML:AttachmentName>").append(escapeXml(ad.name)).append("</edXML:AttachmentName>\n");
            xml.append("        </edXML:Reference>\n");
            
            idx++;
        }

        xml.append("      </edXML:edXMLManifest>\n");
        xml.append("    </edXML:edXMLBody>\n");
        xml.append("  </edXML:edXMLEnvelope>\n");

        // AttachmentEncoded section with multiple Attachments
        xml.append("  <AttachmentEncoded>\n");
        for (AttachmentData ad : attachmentDataList) {
            xml.append("    <Attachment>\n");
            xml.append("      <ContentId>").append(escapeXml(ad.contentId)).append("</ContentId>\n");
            xml.append("      <AttachmentName>").append(escapeXml(ad.name)).append("</AttachmentName>\n");
            xml.append("      <ContentType>").append(escapeXml(ad.contentType)).append("</ContentType>\n");
            xml.append("      <edXML:ContentTransferEncoded xmlns:edXML=\"http://www.mic.gov.vn/TBT/QCVN_102_2016\">");
            xml.append(ad.base64Content);
            xml.append("</edXML:ContentTransferEncoded>\n");
            xml.append("    </Attachment>\n");
        }
        xml.append("  </AttachmentEncoded>\n");
        xml.append("</edXML>\n");

        // Write to file
        String edxmlPath = outputDir + "/" + docId + ".edxml";
        Files.write(new File(edxmlPath).toPath(), xml.toString().getBytes("UTF-8"));
        
        System.out.println("[MANUAL-EDXML] Created EDXML with " + attachmentDataList.size() + " attachments: " + edxmlPath);
        return edxmlPath;
    }

    private static String zipAndEncode(byte[] content, String filename) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(baos);
        
        // Create unique internal filename with UUID
        String internalName = UUID.randomUUID().toString() + getExtension(filename);
        ZipEntry entry = new ZipEntry(internalName);
        zos.putNextEntry(entry);
        zos.write(content);
        zos.closeEntry();
        zos.close();
        
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }

    private static String getExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        return lastDot > 0 ? filename.substring(lastDot) : "";
    }

    private static String guessContentType(String filename) {
        String lower = filename.toLowerCase();
        // Documents
        if (lower.endsWith(".pdf")) return "application/pdf";
        if (lower.endsWith(".doc")) return "application/msword";
        if (lower.endsWith(".docx")) return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        if (lower.endsWith(".xls")) return "application/vnd.ms-excel";
        if (lower.endsWith(".xlsx")) return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        if (lower.endsWith(".ppt")) return "application/vnd.ms-powerpoint";
        if (lower.endsWith(".pptx")) return "application/vnd.openxmlformats-officedocument.presentationml.presentation";
        // Text
        if (lower.endsWith(".txt")) return "text/plain";
        if (lower.endsWith(".xml")) return "text/xml";
        if (lower.endsWith(".html") || lower.endsWith(".htm")) return "text/html";
        if (lower.endsWith(".csv")) return "text/csv";
        // Images
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".gif")) return "image/gif";
        if (lower.endsWith(".bmp")) return "image/bmp";
        if (lower.endsWith(".tiff") || lower.endsWith(".tif")) return "image/tiff";
        // Archives
        if (lower.endsWith(".zip")) return "application/zip";
        if (lower.endsWith(".rar")) return "application/x-rar-compressed";
        if (lower.endsWith(".7z")) return "application/x-7z-compressed";
        // Other
        if (lower.endsWith(".json")) return "application/json";
        if (lower.endsWith(".edxml")) return "application/xml";
        return "application/octet-stream";
    }

    private static String escapeXml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&apos;");
    }
}

