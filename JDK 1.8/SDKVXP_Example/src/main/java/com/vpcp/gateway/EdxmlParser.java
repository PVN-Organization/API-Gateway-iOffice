package com.vpcp.gateway;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;

public class EdxmlParser {

    public static ParsedEdxml parse(String xml) {
        if (xml == null || xml.trim().isEmpty()) return null;
        try {
            SAXBuilder builder = new SAXBuilder();
            Document doc = builder.build(new StringReader(xml));

            Namespace ns = Namespace.getNamespace("http://www.mic.gov.vn/TBT/QCVN_102_2016");

            Element root = doc.getRootElement(); // edXML
            Element env = root.getChild("edXMLEnvelope", Namespace.getNamespace("edXML", ns.getURI()));
            if (env == null) env = root.getChild("edXMLEnvelope", ns);
            if (env == null) return null;

            Element header = env.getChild("edXMLHeader", Namespace.getNamespace("edXML", ns.getURI()));
            if (header == null) header = env.getChild("edXMLHeader", ns);
            if (header == null) return null;

            Element msg = header.getChild("MessageHeader", Namespace.getNamespace("edXML", ns.getURI()));
            if (msg == null) msg = header.getChild("MessageHeader", ns);
            if (msg == null) return null;

            ParsedEdxml out = new ParsedEdxml();

            // From
            Element fromEl = msg.getChild("From", Namespace.getNamespace("edXML", ns.getURI()));
            if (fromEl == null) fromEl = msg.getChild("From", ns);
            if (fromEl != null) {
                ParsedEdxml.PartyInfo p = new ParsedEdxml.PartyInfo();
                p.organId = getText(fromEl, "OrganId", ns);
                p.organizationInCharge = getText(fromEl, "OrganizationInCharge", ns);
                p.organName = getText(fromEl, "OrganName", ns);
                out.from = p;
            }

            // To (multiple)
            List<ParsedEdxml.PartyInfo> tos = new ArrayList<>();
            for (Element toEl : msg.getChildren("To", Namespace.getNamespace("edXML", ns.getURI()))) {
                ParsedEdxml.PartyInfo p = new ParsedEdxml.PartyInfo();
                p.organId = getText(toEl, "OrganId", ns);
                p.organizationInCharge = getText(toEl, "OrganizationInCharge", ns);
                p.organName = getText(toEl, "OrganName", ns);
                tos.add(p);
            }
            if (tos.isEmpty()) {
                for (Element toEl : msg.getChildren("To", ns)) {
                    ParsedEdxml.PartyInfo p = new ParsedEdxml.PartyInfo();
                    p.organId = getText(toEl, "OrganId", ns);
                    p.organizationInCharge = getText(toEl, "OrganizationInCharge", ns);
                    p.organName = getText(toEl, "OrganName", ns);
                    tos.add(p);
                }
            }
            out.to = tos;

            // Document/Code/Promulgation/Subject
            out.documentId = getText(msg, "DocumentId", ns);

            Element codeEl = msg.getChild("Code", Namespace.getNamespace("edXML", ns.getURI()));
            if (codeEl == null) codeEl = msg.getChild("Code", ns);
            if (codeEl != null) {
                out.codeNumber = getText(codeEl, "CodeNumber", ns);
                out.codeNotation = getText(codeEl, "CodeNotation", ns);
            }

            Element promEl = msg.getChild("PromulgationInfo", Namespace.getNamespace("edXML", ns.getURI()));
            if (promEl == null) promEl = msg.getChild("PromulgationInfo", ns);
            if (promEl != null) {
                out.place = getText(promEl, "Place", ns);
                out.promulgationDate = getText(promEl, "PromulgationDate", ns);
            }

            out.subject = getText(msg, "Subject", ns);
            out.content = getText(msg, "Content", ns);

            // DocumentType
            Element docTypeEl = msg.getChild("DocumentType", Namespace.getNamespace("edXML", ns.getURI()));
            if (docTypeEl == null) docTypeEl = msg.getChild("DocumentType", ns);
            if (docTypeEl != null) {
                out.type = getText(docTypeEl, "Type", ns);
                out.typeDetail = getText(docTypeEl, "TypeDetail", ns);
                out.typeName = getText(docTypeEl, "TypeName", ns);
            }

            // Attachments from edXMLManifest
            List<ParsedEdxml.AttachmentInfo> atts = new ArrayList<>();
            Element body = env.getChild("edXMLBody", Namespace.getNamespace("edXML", ns.getURI()));
            if (body == null) body = env.getChild("edXMLBody", ns);
            if (body != null) {
                Element manifest = body.getChild("edXMLManifest", Namespace.getNamespace("edXML", ns.getURI()));
                if (manifest == null) manifest = body.getChild("edXMLManifest", ns);
                if (manifest != null) {
                    for (Element ref : manifest.getChildren("Reference", Namespace.getNamespace("edXML", ns.getURI()))) {
                        ParsedEdxml.AttachmentInfo ai = new ParsedEdxml.AttachmentInfo();
                        ai.contentId = getText(ref, "ContentId", ns);
                        ai.contentType = getText(ref, "ContentType", ns);
                        ai.attachmentName = getText(ref, "AttachmentName", ns);
                        atts.add(ai);
                    }
                    if (atts.isEmpty()) {
                        for (Element ref : manifest.getChildren("Reference", ns)) {
                            ParsedEdxml.AttachmentInfo ai = new ParsedEdxml.AttachmentInfo();
                            ai.contentId = getText(ref, "ContentId", ns);
                            ai.contentType = getText(ref, "ContentType", ns);
                            ai.attachmentName = getText(ref, "AttachmentName", ns);
                            atts.add(ai);
                        }
                    }
                }
            }
            out.attachments = atts;

            // Parse AttachmentEncoded để lấy nội dung file (thường KHÔNG có namespace)
            List<Element> attachmentEncodedList = root.getChildren("AttachmentEncoded");
            if (attachmentEncodedList.isEmpty()) {
                attachmentEncodedList = root.getChildren("AttachmentEncoded", ns);
            }
            
            if (!attachmentEncodedList.isEmpty()) {
                for (Element attEnc : attachmentEncodedList) {
                    List<Element> attachments = attEnc.getChildren("Attachment");
                    if (attachments.isEmpty()) {
                        attachments = attEnc.getChildren("Attachment", ns);
                    }
                    
                    for (Element att : attachments) {
                        String contentId = getTextDirect(att, "ContentId");
                        
                        // Try both with and without namespace
                        String base64Content = getText(att, "ContentTransferEncoded", ns);
                        if (base64Content == null) {
                            base64Content = getTextDirect(att, "ContentTransferEncoded");
                        }
                        
                        if (base64Content != null && !base64Content.trim().isEmpty()) {
                            try {
                                // Decode base64
                                byte[] decoded = Base64.getDecoder().decode(base64Content.trim());
                                
                                // Unzip nếu là zip
                                String decodedContent = unzipContent(decoded);
                                
                                // Match với attachment từ manifest để gán content
                                for (ParsedEdxml.AttachmentInfo ai : atts) {
                                    if (contentId != null && contentId.equals(ai.contentId)) {
                                        ai.decodedContent = decodedContent;
                                        break;
                                    }
                                }
                            } catch (Exception e) {
                                System.err.println("[EDXML-PARSER] Failed to decode attachment " + contentId + ": " + e.getMessage());
                            }
                        }
                    }
                }
            }

            return out;
        } catch (Exception e) {
            System.err.println("[EDXML-PARSER] Parse error: " + e.getMessage());
            return null;
        }
    }
    
    private static String unzipContent(byte[] zippedData) {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(zippedData);
            ZipInputStream zis = new ZipInputStream(bais);
            ZipEntry entry = zis.getNextEntry();
            
            if (entry != null) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    baos.write(buffer, 0, len);
                }
                zis.closeEntry();
                zis.close();
                return new String(baos.toByteArray(), "UTF-8");
            }
        } catch (Exception e) {
            // Nếu không phải zip, trả về raw text
            try {
                return new String(zippedData, "UTF-8");
            } catch (Exception ex) {
                return null;
            }
        }
        return null;
    }
    
    private static String getTextDirect(Element parent, String name) {
        if (parent == null) return null;
        Element child = parent.getChild(name);
        if (child == null) {
            // Try các namespace khác
            for (org.jdom2.Namespace ns : parent.getNamespacesInScope()) {
                child = parent.getChild(name, ns);
                if (child != null) break;
            }
        }
        return child != null ? child.getTextTrim() : null;
    }

    private static String getText(Element parent, String name, Namespace ns) {
        if (parent == null) return null;
        Element child = parent.getChild(name, Namespace.getNamespace("edXML", ns.getURI()));
        if (child == null) child = parent.getChild(name, ns);
        return child != null ? child.getTextTrim() : null;
    }
}


