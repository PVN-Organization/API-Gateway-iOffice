import com.vpcp.gateway.EdxmlParser;
import com.vpcp.gateway.ParsedEdxml;

public class TestEdxmlParser {
    public static void main(String[] args) {
        String testEdxml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<edXML xmlns=\"http://www.mic.gov.vn/TBT/QCVN_102_2016\">\n" +
            "  <edXML:edXMLEnvelope xmlns:edXML=\"http://www.mic.gov.vn/TBT/QCVN_102_2016\">\n" +
            "    <edXML:edXMLHeader>\n" +
            "      <edXML:MessageHeader>\n" +
            "        <edXML:From><edXML:OrganId>test.from</edXML:OrganId></edXML:From>\n" +
            "        <edXML:To><edXML:OrganId>test.to</edXML:OrganId></edXML:To>\n" +
            "        <edXML:DocumentId>doc-123</edXML:DocumentId>\n" +
            "        <edXML:Subject>Test Subject</edXML:Subject>\n" +
            "      </edXML:MessageHeader>\n" +
            "    </edXML:edXMLHeader>\n" +
            "    <edXML:edXMLBody>\n" +
            "      <edXML:edXMLManifest version=\"1.0\">\n" +
            "        <edXML:Reference>\n" +
            "          <edXML:ContentId>cid:test-123</edXML:ContentId>\n" +
            "          <edXML:ContentType>text/plain</edXML:ContentType>\n" +
            "          <edXML:AttachmentName>test.txt</edXML:AttachmentName>\n" +
            "        </edXML:Reference>\n" +
            "      </edXML:edXMLManifest>\n" +
            "    </edXML:edXMLBody>\n" +
            "  </edXML:edXMLEnvelope>\n" +
            "  <AttachmentEncoded>\n" +
            "    <Attachment>\n" +
            "      <ContentId>cid:test-123</ContentId>\n" +
            "      <AttachmentName>test.txt</AttachmentName>\n" +
            "      <ContentType>application/zip</ContentType>\n" +
            "      <edXML:ContentTransferEncoded xmlns:edXML=\"http://www.mic.gov.vn/TBT/QCVN_102_2016\">UEsDBBQACAgIAAyITlsAAAAAAAAAAAAAAAAoAAAANWNjYmYzMmQtNzk0NC00MjY3LWE5MGQtN2U3MjExOTljMWViLnR4dHPOSU3MUyhJLS4BAFBLBwjVr56TDAAAAAoAAABQSwECFAAUAAgICAAMiE5b1a+ekwwAAAAKAAAAKAAAAAAAAAAAAAAAAAAAAAAANWNjYmYzMmQtNzk0NC00MjY3LWE5MGQtN2U3MjExOTljMWViLnR4dFBLBQYAAAAAAQABAFYAAABiAAAAAAA=</edXML:ContentTransferEncoded>\n" +
            "    </Attachment>\n" +
            "  </AttachmentEncoded>\n" +
            "</edXML>";
        
        ParsedEdxml parsed = EdxmlParser.parse(testEdxml);
        
        if (parsed != null) {
            System.out.println("✅ Parse SUCCESS");
            System.out.println("DocumentId: " + parsed.documentId);
            System.out.println("Subject: " + parsed.subject);
            System.out.println("From: " + (parsed.from != null ? parsed.from.organId : "null"));
            
            if (parsed.attachments != null && !parsed.attachments.isEmpty()) {
                System.out.println("\nAttachments: " + parsed.attachments.size());
                for (ParsedEdxml.AttachmentInfo att : parsed.attachments) {
                    System.out.println("  - Name: " + att.attachmentName);
                    System.out.println("    Type: " + att.contentType);
                    System.out.println("    Decoded Content: " + (att.decodedContent != null ? att.decodedContent : "NULL"));
                }
            } else {
                System.out.println("❌ No attachments found");
            }
        } else {
            System.out.println("❌ Parse FAILED");
        }
    }
}

