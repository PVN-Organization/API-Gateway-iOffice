package com.vpcp.gateway;

import java.nio.file.Files;
import java.nio.file.Paths;

public class EdxmlParseCli {
    public static void main(String[] args) throws Exception {
        String path = args.length > 0 ? args[0] : "JDK 1.8/SDKVXP_Example/uploads/1f881896-6e3a-4a5f-87f1-c380878e148e.edxml";
        String xml = new String(Files.readAllBytes(Paths.get(path)), "UTF-8");
        ParsedEdxml parsed = EdxmlParser.parse(xml);
        if (parsed == null) {
            System.out.println("Parse failed");
            System.exit(1);
        }
        // Basic
        System.out.println("=== BASIC INFO ===");
        System.out.println("DocumentId: " + parsed.documentId);
        System.out.println("CodeNumber: " + parsed.codeNumber);
        System.out.println("CodeNotation: " + parsed.codeNotation);
        System.out.println("Place: " + parsed.place);
        System.out.println("PromulgationDate: " + parsed.promulgationDate);
        System.out.println("Subject: " + parsed.subject);
        System.out.println("Content: " + parsed.content);
        
        // DocumentType
        System.out.println("\n=== DOCUMENT TYPE ===");
        System.out.println("Type: " + parsed.type);
        System.out.println("TypeName: " + parsed.typeName);
        System.out.println("TypeDetail: " + parsed.typeDetail);
        
        // From
        if (parsed.from != null) {
            System.out.println("\n=== FROM ===");
            System.out.println("OrganId: " + parsed.from.organId);
            System.out.println("OrganizationInCharge: " + parsed.from.organizationInCharge);
            System.out.println("OrganName: " + parsed.from.organName);
            System.out.println("OrganAdd: " + parsed.from.organAdd);
            System.out.println("Email: " + parsed.from.email);
            System.out.println("Telephone: " + parsed.from.telephone);
            System.out.println("Fax: " + parsed.from.fax);
            System.out.println("Website: " + parsed.from.website);
        }
        
        // To
        if (parsed.to != null && !parsed.to.isEmpty()) {
            System.out.println("\n=== TO (" + parsed.to.size() + ") ===");
            int idx = 1;
            for (ParsedEdxml.PartyInfo p : parsed.to) {
                System.out.println("-- To #" + idx++);
                System.out.println("   OrganId: " + p.organId);
                System.out.println("   OrganizationInCharge: " + p.organizationInCharge);
                System.out.println("   OrganName: " + p.organName);
            }
        }
        
        // ResponseFor
        if (parsed.responseFor != null) {
            System.out.println("\n=== RESPONSE FOR ===");
            System.out.println("OrganId: " + parsed.responseFor.organId);
            System.out.println("Code: " + parsed.responseFor.code);
            System.out.println("DocumentId: " + parsed.responseFor.documentId);
            System.out.println("PromulgationDate: " + parsed.responseFor.promulgationDate);
        }
        
        // StaffInfo
        if (parsed.staffInfo != null) {
            System.out.println("\n=== STAFF INFO ===");
            System.out.println("Department: " + parsed.staffInfo.department);
            System.out.println("Staff: " + parsed.staffInfo.staff);
            System.out.println("Email: " + parsed.staffInfo.email);
            System.out.println("Mobile: " + parsed.staffInfo.mobile);
        }
        
        // Status
        if (parsed.statusCode != null || parsed.description != null || parsed.timestamp != null) {
            System.out.println("\n=== STATUS ===");
            System.out.println("StatusCode: " + parsed.statusCode);
            System.out.println("Description: " + parsed.description);
            System.out.println("Timestamp: " + parsed.timestamp);
        }
        
        // SignerInfo
        if (parsed.signerInfo != null) {
            System.out.println("\n=== SIGNER ===");
            System.out.println("FullName: " + parsed.signerInfo.fullName);
            System.out.println("Position: " + parsed.signerInfo.position);
        }
        
        // OtherInfo
        if (parsed.otherInfo != null) {
            System.out.println("\n=== OTHER INFO ===");
            System.out.println("Priority: " + parsed.otherInfo.priority);
            System.out.println("SphereOfPromulgation: " + parsed.otherInfo.sphereOfPromulgation);
            System.out.println("TyperNotation: " + parsed.otherInfo.typerNotation);
            System.out.println("PromulgationAmount: " + parsed.otherInfo.promulgationAmount);
            System.out.println("PageAmount: " + parsed.otherInfo.pageAmount);
            System.out.println("Direction: " + parsed.otherInfo.direction);
            if (parsed.otherInfo.appendixes != null && !parsed.otherInfo.appendixes.isEmpty()) {
                System.out.println("Appendixes: " + parsed.otherInfo.appendixes);
            }
        }
        
        // Steering
        if (parsed.steeringType != null) {
            System.out.println("\n=== STEERING ===");
            System.out.println("SteeringType: " + parsed.steeringType);
        }
        
        // ToPlaces
        if (parsed.toPlaces != null && !parsed.toPlaces.isEmpty()) {
            System.out.println("ToPlaces: " + parsed.toPlaces);
        }
        
        // Attachments
        System.out.println("\n=== ATTACHMENTS ===");
        System.out.println("Count: " + (parsed.attachments != null ? parsed.attachments.size() : 0));
        if (parsed.attachments != null) {
            int i = 1;
            for (ParsedEdxml.AttachmentInfo ai : parsed.attachments) {
                System.out.println("-- Attachment #" + i++);
                System.out.println("   Name        : " + ai.attachmentName);
                System.out.println("   ContentType : " + ai.contentType);
                System.out.println("   ContentId   : " + ai.contentId);
                String content = ai.decodedContent;
                if (content == null) content = "<null>";
                String preview = content.length() > 120 ? content.substring(0, 120) + "..." : content;
                System.out.println("   Decoded     : " + preview.replace('\n', ' '));
            }
        }
    }
}
