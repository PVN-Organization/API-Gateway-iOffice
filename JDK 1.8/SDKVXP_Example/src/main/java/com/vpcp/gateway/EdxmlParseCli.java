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
        System.out.println("DocumentId: " + parsed.documentId);
        System.out.println("Attachments: " + (parsed.attachments != null ? parsed.attachments.size() : 0));
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
