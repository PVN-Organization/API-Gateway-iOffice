package com.vpcp.gateway;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.vnpt.xml.base.Content;
import com.vnpt.xml.base.attachment.Attachment;
import com.vnpt.xml.base.header.Header;
import com.vnpt.xml.base.header.Organization;
import com.vnpt.xml.base.header.SignerInfo;
import com.vnpt.xml.ed.Ed;
import com.vnpt.xml.ed.builder.EdXmlBuilder;
import com.vnpt.xml.ed.header.Code;
import com.vnpt.xml.ed.header.DocumentType;
import com.vnpt.xml.ed.header.MessageHeader;
import com.vnpt.xml.ed.header.PromulgationInfo;

/**
 * Build EDXML with multiple attachments using SDK EdXmlBuilder
 */
public class EdxmlAggregateBuilder {

    public static String buildAggregatedEdxml(String outputDir,
                                              String fromCode,
                                              List<String> toCodes,
                                              List<FilePart> files,
                                              EdxmlMeta meta) throws Exception {
        // Prepare temp working dir
        File outDir = new File(outputDir);
        if (!outDir.exists()) outDir.mkdirs();

        // Create ED object
        Ed ed = new Ed();

        // Code info
        String codeNumber = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String codeNotation = meta != null && meta.codeNotation != null ? meta.codeNotation : "GW-AUTO";
        Code code = new Code(codeNumber, codeNotation);

        // From org (minimal fields; SDK requires Organization objects)
        String fromOrganName = meta != null && meta.from != null && meta.from.organName != null ? meta.from.organName : fromCode;
        String fromOrganizationInCharge = meta != null && meta.from != null && meta.from.organizationInCharge != null ? meta.from.organizationInCharge : fromOrganName;
        System.out.println("[EDXML-AGG] From.organId=" + fromCode
            + ", organizationInCharge=" + fromOrganizationInCharge
            + ", organName=" + fromOrganName);
        Organization from = new Organization(fromCode, fromOrganName, fromOrganizationInCharge, null, null, null, null, null);

        // To list
        List<Organization> toList = new ArrayList<>();
        if (toCodes != null) {
            for (String c : toCodes) {
                if (c == null || c.trim().isEmpty()) continue;
                String name = c;
                String inCharge = null;
                if (meta != null && meta.to != null) {
                    for (EdxmlMeta.PartyMeta pm : meta.to) {
                        if (pm != null && pm.organId != null && pm.organId.equalsIgnoreCase(c.trim())) {
                            name = pm.organName != null ? pm.organName : c;
                            inCharge = pm.organizationInCharge != null ? pm.organizationInCharge : null;
                            break;
                        }
                    }
                }
                String orgInCharge = inCharge != null ? inCharge : name;
                System.out.println("[EDXML-AGG] To.organId=" + c.trim()
                    + ", organizationInCharge=" + orgInCharge
                    + ", organName=" + name);
                toList.add(new Organization(c.trim(), name, orgInCharge, null, null, null, null, null));
            }
        }

        // Promulgation and type
        PromulgationInfo promulgationInfo = new PromulgationInfo(
            meta != null && meta.place != null ? meta.place : "Hà Nội",
            new java.util.Date()
        );
        DocumentType docType = new DocumentType(1, "Công văn");

        // Signer
        SignerInfo signerInfo = new SignerInfo(null, null,
            meta != null && meta.signerFullName != null ? meta.signerFullName : "Gateway Auto");

        // Header
        MessageHeader messageHeader = new MessageHeader(
            from,
            toList,
            code,
            promulgationInfo,
            docType,
            meta != null && meta.subject != null ? meta.subject : codeNumber,
            meta != null && meta.content != null ? meta.content : "File uploaded via Gateway",
            null,
            signerInfo,
            new Date(),
            null,
            null
        );
        Header header = new Header(messageHeader, null, null);
        ed.setHeader(header);

        // Add attachments: write each content to temp file, then attach
        int idx = 1;
        for (FilePart fp : files) {
            if (fp == null || fp.getContent() == null) continue;
            String name = fp.getFileName() != null ? fp.getFileName() : ("file_" + idx);
            File tmp = new File(outDir, name);
            try (FileOutputStream fos = new FileOutputStream(tmp)) {
                fos.write(fp.getContent());
            }
            // Use unique ContentId with UUID to avoid conflicts
            String contentId = "cid:" + java.util.UUID.randomUUID().toString();
            System.out.println("[EDXML-AGG] Adding attachment " + idx + ": " + name + " with ContentId: " + contentId);
            ed.addAttachment(new Attachment(contentId, name, name, tmp));
            idx++;
        }

        // Build using SDK
        Content built = EdXmlBuilder.build(ed, outputDir);
        return built.getContent().getPath();
    }
}
