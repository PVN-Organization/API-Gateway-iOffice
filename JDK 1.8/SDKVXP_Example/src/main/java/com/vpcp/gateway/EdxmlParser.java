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
                p.organAdd = getText(fromEl, "OrganAdd", ns);
                p.email = getText(fromEl, "Email", ns);
                p.telephone = getText(fromEl, "Telephone", ns);
                p.fax = getText(fromEl, "Fax", ns);
                p.website = getText(fromEl, "Website", ns);
                out.from = p;
            }

            // To (multiple)
            List<ParsedEdxml.PartyInfo> tos = new ArrayList<>();
            for (Element toEl : msg.getChildren("To", Namespace.getNamespace("edXML", ns.getURI()))) {
                ParsedEdxml.PartyInfo p = new ParsedEdxml.PartyInfo();
                p.organId = getText(toEl, "OrganId", ns);
                p.organizationInCharge = getText(toEl, "OrganizationInCharge", ns);
                p.organName = getText(toEl, "OrganName", ns);
                p.organAdd = getText(toEl, "OrganAdd", ns);
                p.email = getText(toEl, "Email", ns);
                p.telephone = getText(toEl, "Telephone", ns);
                p.fax = getText(toEl, "Fax", ns);
                p.website = getText(toEl, "Website", ns);
                tos.add(p);
            }
            if (tos.isEmpty()) {
                for (Element toEl : msg.getChildren("To", ns)) {
                    ParsedEdxml.PartyInfo p = new ParsedEdxml.PartyInfo();
                    p.organId = getText(toEl, "OrganId", ns);
                    p.organizationInCharge = getText(toEl, "OrganizationInCharge", ns);
                    p.organName = getText(toEl, "OrganName", ns);
                    p.organAdd = getText(toEl, "OrganAdd", ns);
                    p.email = getText(toEl, "Email", ns);
                    p.telephone = getText(toEl, "Telephone", ns);
                    p.fax = getText(toEl, "Fax", ns);
                    p.website = getText(toEl, "Website", ns);
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

            // ResponseFor (for status EDXML)
            Element respEl = msg.getChild("ResponseFor", Namespace.getNamespace("edXML", ns.getURI()));
            if (respEl == null) respEl = msg.getChild("ResponseFor", ns);
            if (respEl != null) {
                ParsedEdxml.ResponseForInfo rf = new ParsedEdxml.ResponseForInfo();
                rf.organId = getText(respEl, "OrganId", ns);
                rf.code = getText(respEl, "Code", ns);
                rf.documentId = getText(respEl, "DocumentId", ns);
                rf.promulgationDate = getText(respEl, "PromulgationDate", ns);
                out.responseFor = rf;
            }

            // StaffInfo
            Element staffEl = msg.getChild("StaffInfo", Namespace.getNamespace("edXML", ns.getURI()));
            if (staffEl == null) staffEl = msg.getChild("StaffInfo", ns);
            if (staffEl != null) {
                ParsedEdxml.StaffInfo si = new ParsedEdxml.StaffInfo();
                si.department = getText(staffEl, "Department", ns);
                si.staff = getText(staffEl, "Staff", ns);
                si.email = getText(staffEl, "Email", ns);
                si.mobile = getText(staffEl, "Mobile", ns);
                out.staffInfo = si;
            }

            // StatusCode, Description, Timestamp (for status EDXML)
            out.statusCode = getText(msg, "StatusCode", ns);
            out.description = getText(msg, "Description", ns);
            out.timestamp = getText(msg, "Timestamp", ns);

            // SignerInfo
            Element signerEl = msg.getChild("SignerInfo", Namespace.getNamespace("edXML", ns.getURI()));
            if (signerEl == null) signerEl = msg.getChild("SignerInfo", ns);
            if (signerEl != null) {
                ParsedEdxml.SignerInfo signer = new ParsedEdxml.SignerInfo();
                signer.fullName = getText(signerEl, "FullName", ns);
                signer.position = getText(signerEl, "Position", ns);
                out.signerInfo = signer;
            }

            // OtherInfo
            Element otherEl = msg.getChild("OtherInfo", Namespace.getNamespace("edXML", ns.getURI()));
            if (otherEl == null) otherEl = msg.getChild("OtherInfo", ns);
            if (otherEl != null) {
                ParsedEdxml.OtherInfo oi = new ParsedEdxml.OtherInfo();
                oi.priority = getText(otherEl, "Priority", ns);
                oi.sphereOfPromulgation = getText(otherEl, "SphereOfPromulgation", ns);
                oi.typerNotation = getText(otherEl, "TyperNotation", ns);
                oi.promulgationAmount = getText(otherEl, "PromulgationAmount", ns);
                oi.pageAmount = getText(otherEl, "PageAmount", ns);
                oi.direction = getText(otherEl, "Direction", ns);
                // Appendixes
                Element appendixesEl = otherEl.getChild("Appendixes", Namespace.getNamespace("edXML", ns.getURI()));
                if (appendixesEl == null) appendixesEl = otherEl.getChild("Appendixes", ns);
                if (appendixesEl != null) {
                    List<String> appendixes = new ArrayList<>();
                    for (Element appEl : appendixesEl.getChildren("Appendix", Namespace.getNamespace("edXML", ns.getURI()))) {
                        String app = appEl.getTextTrim();
                        if (app != null && !app.isEmpty()) appendixes.add(app);
                    }
                    if (appendixes.isEmpty()) {
                        for (Element appEl : appendixesEl.getChildren("Appendix", ns)) {
                            String app = appEl.getTextTrim();
                            if (app != null && !app.isEmpty()) appendixes.add(app);
                        }
                    }
                    oi.appendixes = appendixes;
                }
                out.otherInfo = oi;
            }

            // SteeringType
            out.steeringType = getText(msg, "SteeringType", ns);

            // ToPlaces
            Element toPlacesEl = msg.getChild("ToPlaces", Namespace.getNamespace("edXML", ns.getURI()));
            if (toPlacesEl == null) toPlacesEl = msg.getChild("ToPlaces", ns);
            if (toPlacesEl != null) {
                List<String> toPlaces = new ArrayList<>();
                for (Element plEl : toPlacesEl.getChildren("Place", Namespace.getNamespace("edXML", ns.getURI()))) {
                    String pl = plEl.getTextTrim();
                    if (pl != null && !pl.isEmpty()) toPlaces.add(pl);
                }
                if (toPlaces.isEmpty()) {
                    for (Element plEl : toPlacesEl.getChildren("Place", ns)) {
                        String pl = plEl.getTextTrim();
                        if (pl != null && !pl.isEmpty()) toPlaces.add(pl);
                    }
                }
                out.toPlaces = toPlaces;
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
                                
                                // Unzip nếu là zip -> lấy bytes sau unzip (hoặc raw nếu không zip)
                                byte[] contentBytes = unzipContentBytes(decoded);
                                if (contentBytes == null) contentBytes = decoded;
                                
                                // Match với attachment từ manifest để gán content
                                for (ParsedEdxml.AttachmentInfo ai : atts) {
                                    if (contentId != null && contentId.equals(ai.contentId)) {
                                        // Phân loại theo contentType để mapping đúng trường
                                        if (ai.contentType != null) {
                                            String ct = ai.contentType.toLowerCase();
                                            if (ct.startsWith("text/") || ct.contains("xml") || ct.contains("json") || ct.startsWith("application/xml") || ct.startsWith("text+xml")) {
                                                // Textual content: decode UTF-8
                                                try {
                                                    ai.decodedContent = new String(contentBytes, "UTF-8");
                                                } catch (Exception ex) {
                                                    ai.decodedContent = null;
                                                }
                                            } else {
                                                // Binary content: giữ base64 cho client xử lý
                                                ai.decodedContentBase64 = Base64.getEncoder().encodeToString(contentBytes);
                                            }
                                        } else {
                                            // Không có contentType: fallback base64 để an toàn
                                            ai.decodedContentBase64 = Base64.getEncoder().encodeToString(contentBytes);
                                        }
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
    
    private static byte[] unzipContentBytes(byte[] zippedData) {
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
                return baos.toByteArray();
            }
        } catch (Exception e) {
            // Nếu không phải zip, trả về raw bytes
            return zippedData;
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


