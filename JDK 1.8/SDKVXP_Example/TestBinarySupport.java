import com.vpcp.gateway.EdxmlParser;
import com.vpcp.gateway.ParsedEdxml;
import java.nio.file.Files;
import java.nio.file.Paths;

public class TestBinarySupport {
    public static void main(String[] args) {
        try {
            // Đọc mock EDXML
            String xmlContent = new String(Files.readAllBytes(Paths.get("../../mock_edxml.xml")));
            
            // Parse với EdxmlParser
            ParsedEdxml parsed = EdxmlParser.parse(xmlContent);
            
            System.out.println("📋 Parsed Document:");
            System.out.println("From: " + parsed.from.organId);
            System.out.println("To: " + parsed.to.get(0).organId);
            System.out.println("Timestamp: " + parsed.timestamp);
            System.out.println("Attachments: " + parsed.attachments.size());
            
            for (ParsedEdxml.AttachmentInfo att : parsed.attachments) {
                System.out.println("\n📎 Attachment: " + att.attachmentName);
                System.out.println("Content Type: " + att.contentType);
                System.out.println("Content ID: " + att.contentId);
                
                if (att.decodedContent != null) {
                    System.out.println("✅ Text Content: " + att.decodedContent);
                } else {
                    System.out.println("❌ Text Content: null");
                }
                
                if (att.decodedContentBase64 != null) {
                    System.out.println("✅ Binary Content (Base64): " + att.decodedContentBase64.substring(0, Math.min(50, att.decodedContentBase64.length())) + "...");
                    System.out.println("Binary Length: " + att.decodedContentBase64.length() + " chars");
                } else {
                    System.out.println("❌ Binary Content: null");
                }
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
