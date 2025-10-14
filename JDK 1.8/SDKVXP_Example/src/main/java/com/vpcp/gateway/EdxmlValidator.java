package com.vpcp.gateway;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

/**
 * EdXML Validator
 * 
 * Validate file trước khi gửi lên VXP
 */
public class EdxmlValidator {
    
    /**
     * Validate file có phải XML hợp lệ không
     */
    public static ValidationResult validateFile(String filePath) {
        File file = new File(filePath);
        
        // Check file exists
        if (!file.exists()) {
            return new ValidationResult(false, "File không tồn tại");
        }
        
        // Check file size (max 50MB)
        long sizeInMB = file.length() / (1024 * 1024);
        if (sizeInMB > 50) {
            return new ValidationResult(false, "File quá lớn (max 50MB), size: " + sizeInMB + "MB");
        }
        
        // Check if XML
        if (!isXmlFile(filePath)) {
            return new ValidationResult(
                false, 
                "File không phải XML format. Gateway chấp nhận upload nhưng VXP Server yêu cầu file .edxml hợp lệ"
            );
        }
        
        // Validate XML structure
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(file);
            
            // Check root element
            String rootName = doc.getDocumentElement().getNodeName();
            System.out.println("  ✅ XML valid, root element: " + rootName);
            
            return new ValidationResult(true, "File XML hợp lệ");
            
        } catch (Exception e) {
            return new ValidationResult(
                false,
                "File XML không hợp lệ: " + e.getMessage() + 
                ". Gateway vẫn upload nhưng VXP Server sẽ reject"
            );
        }
    }
    
    /**
     * Check if file is XML by reading first bytes
     */
    private static boolean isXmlFile(String filePath) {
        try {
            java.io.FileInputStream fis = new java.io.FileInputStream(filePath);
            byte[] buffer = new byte[100];
            int read = fis.read(buffer);
            fis.close();
            
            if (read > 0) {
                String header = new String(buffer, 0, read, "UTF-8").trim();
                return header.startsWith("<?xml") || header.startsWith("<");
            }
        } catch (Exception e) {
            System.err.println("Error checking XML: " + e.getMessage());
        }
        return false;
    }
    
    /**
     * Validation Result
     */
    public static class ValidationResult {
        private boolean valid;
        private String message;
        
        public ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public String getMessage() {
            return message;
        }
    }
}

