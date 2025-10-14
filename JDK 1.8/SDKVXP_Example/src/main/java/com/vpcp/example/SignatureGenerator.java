package com.vpcp.example;

import java.text.SimpleDateFormat;
import java.util.Date;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Signature Generator for VXP API
 * Usage: java -cp "target/classes:lib/*" com.vpcp.example.SignatureGenerator [systemId] [secretKey] [requestBody]
 */
public class SignatureGenerator {
    
    public static void main(String[] args) {
        String systemId = "vxp.saas.03";
        String secretKey = "A7TKG/r2gOKTEpe1MhWx6zw8O4JfzN0KkCRS6HZY81Ve";
        String requestBody = "";
        
        // Parse arguments
        if (args.length >= 2) {
            systemId = args[0];
            secretKey = args[1];
        }
        if (args.length >= 3) {
            requestBody = args[2];
        }
        
        try {
            // Generate timestamp
            String timestamp = generateTimestamp();
            String contentType = "application/json";
            
            // Create message to sign
            String message = systemId + timestamp + contentType + requestBody;
            
            // Generate signature
            String signature = generateSignature(message, secretKey);
            
            // Output in JSON format for easy parsing
            System.out.println("{");
            System.out.println("  \"timestamp\": \"" + timestamp + "\",");
            System.out.println("  \"signature\": \"" + signature + "\",");
            System.out.println("  \"systemId\": \"" + systemId + "\",");
            System.out.println("  \"contentType\": \"" + contentType + "\",");
            System.out.println("  \"message\": \"" + message + "\"");
            System.out.println("}");
            
        } catch (Exception e) {
            System.err.println("Error generating signature: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Generate timestamp in required format: DDMMYYYYTHHmmss+SSSS
     */
    private static String generateTimestamp() {
        Date now = new Date();
        SimpleDateFormat sdfDate = new SimpleDateFormat("ddMMyyyy");
        SimpleDateFormat sdfTime = new SimpleDateFormat("HHmmss");
        
        String dateStr = sdfDate.format(now);
        String timeStr = sdfTime.format(now);
        
        // Get milliseconds (4 digits)
        long millis = now.getTime() % 10000;
        String millisStr = String.format("%04d", millis);
        
        return dateStr + "T" + timeStr + "+" + millisStr;
    }
    
    /**
     * Generate HMAC-SHA256 signature
     */
    private static String generateSignature(String message, String secretKey) throws Exception {
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec(secretKey.getBytes("UTF-8"), "HmacSHA256");
        sha256_HMAC.init(secret_key);
        
        byte[] hash = sha256_HMAC.doFinal(message.getBytes("UTF-8"));
        
        // Convert to hex string
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        
        return hexString.toString();
    }
}

