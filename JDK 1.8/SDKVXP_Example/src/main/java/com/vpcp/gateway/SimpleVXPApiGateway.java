package com.vpcp.gateway;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

/**
 * Simple VXP API Gateway  
 * Using JDK built-in HttpServer (no external dependencies needed)
 * 
 * Usage:
 *   java -cp "target/classes:lib/*" com.vpcp.gateway.SimpleVXPApiGateway [port] [systemId] [secret]
 * 
 * Examples:
 *   java -cp "target/classes:lib/*" com.vpcp.gateway.SimpleVXPApiGateway 8080
 *   java -cp "target/classes:lib/*" com.vpcp.gateway.SimpleVXPApiGateway 8080 vxp.saas.02 secret_key
 */
public class SimpleVXPApiGateway {
    
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static AgencyPipeline agencyPipeline;
    private static DocumentPipeline documentPipeline;
    
    public static void main(String[] args) throws IOException {
        int port = getPort(args);
        String systemId = getSystemId(args);
        String secret = getSecret(args);
        
        // Initialize pipeline
        initializePipeline(systemId, secret);
        
        // Create HTTP server (bind to 0.0.0.0 for external access)
        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", port), 0);
        
        // Register handlers
        server.createContext("/api/", new MainHandler());
        
        // Start server
        server.setExecutor(null);
        server.start();
        
        System.out.println("=====================================");
        System.out.println("🚀 VXP API Gateway Started!");
        System.out.println("=====================================");
        System.out.println("📡 Server: http://0.0.0.0:" + port);
        System.out.println("");
        System.out.println("📖 Endpoints:");
        System.out.println("");
        System.out.println("  🏥 Health:");
        System.out.println("    GET  /api/health                    - Health check");
        System.out.println("");
        System.out.println("  🏢 Agencies:");
        System.out.println("    GET  /api/agencies                  - Get all agencies");
        System.out.println("    GET  /api/agencies?id=XXX           - Get agency by ID");
        System.out.println("    POST /api/agencies                  - Register agency");
        System.out.println("");
        System.out.println("  📄 Documents:");
        System.out.println("    GET  /api/documents/received        - Get received documents");
        System.out.println("    GET  /api/documents/{id}            - Get document detail");
        System.out.println("    POST /api/documents/send            - Send single document");
        System.out.println("    POST /api/documents/send/batch      - Send multiple documents ⭐");
        System.out.println("    PUT  /api/documents/status          - Update document status");
        System.out.println("=====================================");
        System.out.println("Press Ctrl+C to stop");
        System.out.println("");
    }
    
    private static void initializePipeline(String systemId, String secret) {
        String endpoint = "http://vxpdungchung.vnpt.vn";
        String storePathDir = "/tmp/vxp_" + systemId;
        int maxConnection = 10;
        int retry = 3;
        
        agencyPipeline = new AgencyPipeline(endpoint, systemId, secret, storePathDir, maxConnection, retry);
        documentPipeline = new DocumentPipeline(endpoint, systemId, secret, storePathDir, maxConnection, retry);
        System.out.println("✅ Pipelines initialized with SystemID: " + systemId);
        
        // Log Gateway ENV config
        GatewayConfig.logConfig();
    }
    
    private static int getPort(String[] args) {
        if (args.length > 0) {
            try {
                return Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port, using 8080");
            }
        }
        return 8080;
    }
    
    private static String getSystemId(String[] args) {
        if (args.length > 1) {
            return args[1];
        }
        // Default to vxp.saas.03 (receiver)
        return "vxp.saas.03";
    }
    
    private static String getSecret(String[] args) {
        if (args.length > 2) {
            return args[2];
        }
        // Default to vxp.saas.03 secret (receiver)
        return "A7TKG/r2gOKTEpe1MhWx6zw8O4JfzN0KkCRS6HZY81Ve";
    }
    
    // Main Router Handler
    static class MainHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();
            String query = exchange.getRequestURI().getQuery();
            
            System.out.println("📥 " + method + " " + path + (query != null ? "?" + query : ""));
            
            try {
                // Health
                if (path.equals("/api/health") && ("GET".equals(method) || "HEAD".equals(method))) {
                    handleHealth(exchange);
                }
                // Agencies
                else if (path.equals("/api/agencies") && "GET".equals(method)) {
                    if (query != null && query.startsWith("id=")) {
                        handleGetAgencyById(exchange, query.substring(3));
                    } else {
                        handleGetAllAgencies(exchange);
                    }
                }
                else if (path.equals("/api/agencies") && "POST".equals(method)) {
                    handleRegisterAgency(exchange);
                }
                // Documents - Received
                else if (path.equals("/api/documents/received") && "GET".equals(method)) {
                    handleGetReceivedDocuments(exchange);
                }
                // Documents - Send Single
                else if (path.equals("/api/documents/send") && "POST".equals(method)) {
                    handleSendDocument(exchange);
                }
                // Documents - Send Multiple (Batch)
                else if (path.equals("/api/documents/send/batch") && "POST".equals(method)) {
                    handleSendDocumentBatch(exchange);
                }
                // Documents - Status
                else if (path.equals("/api/documents/status") && "PUT".equals(method)) {
                    handleUpdateDocumentStatus(exchange);
                }
                // Documents - Detail by ID
                else if (path.startsWith("/api/documents/") && "GET".equals(method)) {
                    String docId = path.substring("/api/documents/".length());
                    handleGetDocumentDetail(exchange, docId);
                }
                // 404
                else {
                    sendError(exchange, 404, "Endpoint not found: " + path);
                }
            } catch (Exception e) {
                System.err.println("❌ Error: " + e.getMessage());
                e.printStackTrace();
                sendError(exchange, 500, "Internal server error: " + e.getMessage());
            }
        }
        
        // Health
        private void handleHealth(HttpExchange exchange) throws IOException {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "VXP API Gateway is running");
            response.put("timestamp", System.currentTimeMillis());
            sendResponse(exchange, 200, response);
        }
        
        // Get All Agencies
        private void handleGetAllAgencies(HttpExchange exchange) throws IOException {
            AgencyListResponse result = agencyPipeline.getAllAgencies();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Successfully retrieved agencies");
            response.put("data", result);
            sendResponse(exchange, 200, response);
        }
        
        // Get Agency By ID
        private void handleGetAgencyById(HttpExchange exchange, String id) throws IOException {
            AgencyDetail agency = agencyPipeline.getAgencyById(id);
            if (agency != null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Agency found");
                response.put("data", agency);
                sendResponse(exchange, 200, response);
            } else {
                sendError(exchange, 404, "Agency not found");
            }
        }
        
        // Register Agency
        private void handleRegisterAgency(HttpExchange exchange) throws IOException {
            String body = readRequestBody(exchange);
            AgencyRequest request = gson.fromJson(body, AgencyRequest.class);
            RegisterResult result = agencyPipeline.registerAgency(request);
            Map<String, Object> response = new HashMap<>();
            response.put("success", result.isSuccess());
            response.put("message", result.getMessage());
            sendResponse(exchange, result.isSuccess() ? 200 : 400, response);
        }
        
        // Get Received Documents
        private void handleGetReceivedDocuments(HttpExchange exchange) throws IOException {
            // Parse query parameters
            String query = exchange.getRequestURI().getQuery();
            String fromDate = null;
            String toDate = null;
            String status = null;
            Integer limit = null;
            
            if (query != null) {
                String[] params = query.split("&");
                for (String param : params) {
                    String[] pair = param.split("=");
                    if (pair.length == 2) {
                        String key = pair[0];
                        String value = pair[1];
                        
                        if ("fromDate".equals(key)) {
                            fromDate = value;
                        } else if ("toDate".equals(key)) {
                            toDate = value;
                        } else if ("status".equals(key)) {
                            status = value;
                        } else if ("limit".equals(key)) {
                            try {
                                limit = Integer.parseInt(value);
                            } catch (NumberFormatException e) {
                                // ignore
                            }
                        }
                    }
                }
            }
            
            ReceivedDocumentsResponse result = documentPipeline.getReceivedDocuments(fromDate, toDate, status, limit);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Successfully retrieved documents");
            response.put("data", result);
            sendResponse(exchange, 200, response);
        }
        
        // Get Document Detail
        private void handleGetDocumentDetail(HttpExchange exchange, String docId) throws IOException {
            boolean parsed = false;
            if (docId.endsWith("/parsed")) {
                parsed = true;
                docId = docId.substring(0, docId.length() - "/parsed".length());
            }
            if (parsed) {
                GetDocumentParsedResponse result = documentPipeline.getDocumentDetailParsed(docId);
                Map<String, Object> response = new HashMap<>();
                response.put("success", result.isSuccess());
                response.put("message", result.getMessage());
                if (result.isSuccess()) {
                    Map<String, Object> data = new HashMap<>();
                    data.put("docId", docId);
                    data.put("parsed", result.getParsed());
                    data.put("raw", result.getRaw());
                    response.put("data", data);
                }
                sendResponse(exchange, result.isSuccess() ? 200 : 404, response);
                return;
            }
            GetDocumentResponse result = documentPipeline.getDocumentDetail(docId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", result.isSuccess());
            response.put("message", result.getMessage());
            if (result.isSuccess()) {
                Map<String, Object> data = new HashMap<>();
                data.put("docId", docId);
                data.put("data", result.getData());
                data.put("filePath", result.getFilePath());
                response.put("data", data);
            }
            sendResponse(exchange, result.isSuccess() ? 200 : 404, response);
        }
        
        // Send Document
        private void handleSendDocument(HttpExchange exchange) throws IOException {
            String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
            
            if (contentType != null && contentType.startsWith("multipart/form-data")) {
                handleMultipartSend(exchange, contentType);
            } else {
                sendError(exchange, 400, "Content-Type must be multipart/form-data");
            }
        }
        
        // Send Multiple Documents (Batch)
        private void handleSendDocumentBatch(HttpExchange exchange) throws IOException {
            String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
            
            if (contentType != null && contentType.startsWith("multipart/form-data")) {
                handleMultipartBatchSend(exchange, contentType);
            } else {
                sendError(exchange, 400, "Content-Type must be multipart/form-data");
            }
        }
        
        // Update Document Status
        private void handleUpdateDocumentStatus(HttpExchange exchange) throws IOException {
            String body = readRequestBody(exchange);
            Map<String, String> request = gson.fromJson(body, Map.class);
            String docId = request.get("docId");
            String status = request.get("status");
            UpdateStatusResponse result = documentPipeline.updateDocumentStatus(docId, status);
            Map<String, Object> response = new HashMap<>();
            response.put("success", result.isSuccess());
            response.put("message", result.getMessage());
            sendResponse(exchange, result.isSuccess() ? 200 : 400, response);
        }
        
        private void handleMultipartBatchSend(HttpExchange exchange, String contentType) throws IOException {
            try {
                // Parse boundary
                String boundary = extractBoundary(contentType);
                if (boundary == null) {
                    sendError(exchange, 400, "Invalid multipart boundary");
                    return;
                }
                
                // Read multipart data
                byte[] body = readRequestBodyBytes(exchange);
                java.util.List<MultipartData> dataList = parseMultipartBatch(body, boundary);
                
                if (dataList.isEmpty()) {
                    sendError(exchange, 400, "No files provided");
                    return;
                }
                
                // Aggregate mode?
                boolean aggregate = false;
                if (!dataList.isEmpty() && dataList.get(0).aggregate != null) {
                    String flag = dataList.get(0).aggregate.trim().toLowerCase();
                    aggregate = ("true".equals(flag) || "1".equals(flag) || "yes".equals(flag));
                }

                // Aggregate: build one EDXML containing ALL files
                if (aggregate) {
                    java.util.List<FilePart> fileParts = new java.util.ArrayList<>();
                    String metaJson = null;
                    String fromCode = null;
                    String toCode = null;
                    
                    // Collect metaJson, fromCode, toCode from any part
                    for (MultipartData d : dataList) {
                        if (d.fileContent != null) fileParts.add(new FilePart(d.fileName, d.fileContent));
                        if (d.metaJson != null && metaJson == null) metaJson = d.metaJson;
                        if (d.fromCode != null && fromCode == null) fromCode = d.fromCode;
                        if (d.toCode != null && toCode == null) toCode = d.toCode;
                    }
                    
                    // Let DocumentPipeline derive fromCode/toCode from metaJson if not provided
                    SendDocumentResponse agg = documentPipeline.sendDocumentsAggregated(
                        fromCode,
                        toCode,
                        fileParts,
                        metaJson
                    );

                    java.util.Map<String, Object> response = new java.util.HashMap<>();
                    response.put("success", agg.isSuccess());
                    response.put("message", agg.getMessage());
                    response.put("data", agg.getDocument());
                    sendResponse(exchange, agg.isSuccess() ? 200 : 400, response);
                    return;
                }

                // Process each file
                java.util.List<Map<String, Object>> results = new java.util.ArrayList<>();
                int successCount = 0;
                int failCount = 0;
                
                for (int i = 0; i < dataList.size(); i++) {
                    MultipartData data = dataList.get(i);
                    Map<String, Object> fileResult = new HashMap<>();
                    
                    try {
                        // Validate
                        if (data.fileContent == null) {
                            fileResult.put("success", false);
                            fileResult.put("fileName", data.fileName != null ? data.fileName : "unknown");
                            fileResult.put("error", "Missing file content");
                            failCount++;
                            results.add(fileResult);
                            continue;
                        }
                        
                        // Create request
                        SendDocumentRequest request = new SendDocumentRequest(
                            data.fromCode,
                            data.toCode,
                            data.fileName != null ? data.fileName : "document_" + i + ".edxml",
                            data.fileContent
                        );
                        request.setServiceType(data.serviceType != null ? data.serviceType : "eDoc");
                        request.setMessageType(data.messageType != null ? data.messageType : "edoc");
                        if (data.metaJson != null) request.setMetaJson(data.metaJson);
                        request.setForceWrap(true); // Force rebuild with meta
                        
                        // Send document
                        SendDocumentResponse sendResult = documentPipeline.sendDocument(request);
                        
                        fileResult.put("success", sendResult.isSuccess());
                        fileResult.put("fileName", request.getFileName());
                        fileResult.put("message", sendResult.getMessage());
                        if (sendResult.isSuccess() && sendResult.getDocument() != null) {
                            fileResult.put("docId", sendResult.getDocument().getDocId());
                            successCount++;
                        } else {
                            failCount++;
                        }
                    } catch (Exception e) {
                        fileResult.put("success", false);
                        fileResult.put("fileName", data.fileName != null ? data.fileName : "unknown");
                        fileResult.put("error", e.getMessage());
                        failCount++;
                    }
                    
                    results.add(fileResult);
                }
                
                // Build response
                Map<String, Object> response = new HashMap<>();
                response.put("success", successCount > 0);
                response.put("message", "Processed " + dataList.size() + " files: " + successCount + " success, " + failCount + " failed");
                response.put("totalFiles", dataList.size());
                response.put("successCount", successCount);
                response.put("failCount", failCount);
                response.put("results", results);
                
                sendResponse(exchange, successCount > 0 ? 200 : 400, response);
                
            } catch (Exception e) {
                System.err.println("❌ Batch send error: " + e.getMessage());
                e.printStackTrace();
                sendError(exchange, 500, "Failed to process batch: " + e.getMessage());
            }
        }
        
        private void handleMultipartSend(HttpExchange exchange, String contentType) throws IOException {
            try {
                // Parse boundary
                String boundary = extractBoundary(contentType);
                if (boundary == null) {
                    sendError(exchange, 400, "Invalid multipart boundary");
                    return;
                }
                
                // Read multipart data
                byte[] body = readRequestBodyBytes(exchange);
                MultipartData data = parseMultipart(body, boundary);
                
                if (data.fileContent == null) {
                    sendError(exchange, 400, "file is required");
                    return;
                }
                
                // Create request (gateway tối giản: không dùng fromCode/toCode, lấy từ meta)
                SendDocumentRequest request = new SendDocumentRequest(
                    null,
                    null,
                    data.fileName != null ? data.fileName : "document.edxml",
                    data.fileContent
                );
                request.setServiceType(data.serviceType != null ? data.serviceType : "eDoc");
                request.setMessageType(data.messageType != null ? data.messageType : "edoc");
                if (data.metaJson != null) request.setMetaJson(data.metaJson);
                // Gateway tự động: luôn rebuild EDXML và áp dụng meta khi có
                request.setForceWrap(true);
                
                // Send document
                SendDocumentResponse result = documentPipeline.sendDocument(request);
                
                Map<String, Object> response = new HashMap<>();
                response.put("success", result.isSuccess());
                response.put("message", result.getMessage());
                response.put("data", result.getDocument());
                
                sendResponse(exchange, result.isSuccess() ? 200 : 400, response);
                
            } catch (Exception e) {
                System.err.println("❌ Multipart parse error: " + e.getMessage());
                sendError(exchange, 400, "Failed to parse multipart data: " + e.getMessage());
            }
        }
        
        
        // Parse multipart form data
        private MultipartData parseMultipart(byte[] body, String boundary) {
            MultipartData data = new MultipartData();
            String bodyStr = new String(body, StandardCharsets.UTF_8);
            String[] parts = bodyStr.split("--" + boundary);
            
            for (String part : parts) {
                if (part.contains("Content-Disposition")) {
                    // Extract field name
                    String name = extractFieldName(part);
                    
                    if ("fromCode".equals(name) || "from".equals(name)) {
                        data.fromCode = extractFieldValue(part);
                    } else if ("toCode".equals(name) || "to".equals(name)) {
                        data.toCode = extractFieldValue(part);
                    } else if ("serviceType".equals(name) || "servicetype".equals(name)) {
                        data.serviceType = extractFieldValue(part);
                    } else if ("messageType".equals(name) || "messagetype".equals(name)) {
                        data.messageType = extractFieldValue(part);
                    } else if ("meta".equals(name) || "metadata".equals(name)) {
                        data.metaJson = extractFieldValue(part);
                    } else if ("file".equals(name)) {
                        data.fileName = extractFileName(part);
                        data.fileContent = extractFileContent(body, part);
                    }
                }
            }
            
            return data;
        }
        
        // Parse multipart form data with MULTIPLE files
        private java.util.List<MultipartData> parseMultipartBatch(byte[] body, String boundary) {
            java.util.List<MultipartData> dataList = new java.util.ArrayList<>();
            String bodyStr = new String(body, StandardCharsets.UTF_8);
            String[] parts = bodyStr.split("--" + boundary);
            
            // Extract common fields first
            String fromCode = null;
            String toCode = null;
            String serviceType = null;
            String messageType = null;
            String metaJson = null;
            String aggregateFlag = null;
            
            // First pass: extract common fields
            for (String part : parts) {
                if (part.contains("Content-Disposition")) {
                    String name = extractFieldName(part);
                    
                    if ("fromCode".equals(name) || "from".equals(name)) {
                        fromCode = extractFieldValue(part);
                    } else if ("toCode".equals(name) || "to".equals(name)) {
                        toCode = extractFieldValue(part);
                    } else if ("serviceType".equals(name) || "servicetype".equals(name)) {
                        serviceType = extractFieldValue(part);
                    } else if ("messageType".equals(name) || "messagetype".equals(name)) {
                        messageType = extractFieldValue(part);
                    } else if ("meta".equals(name) || "metadata".equals(name)) {
                        metaJson = extractFieldValue(part);
                    } else if ("aggregate".equals(name)) {
                        aggregateFlag = extractFieldValue(part);
                    }
                }
            }
            
            // Second pass: extract all files
            for (String part : parts) {
                if (part.contains("Content-Disposition") && part.contains("filename")) {
                    String name = extractFieldName(part);
                    
                    if ("file".equals(name) || "files".equals(name) || name != null && name.startsWith("file")) {
                        MultipartData data = new MultipartData();
                        data.fromCode = fromCode;
                        data.toCode = toCode;
                        data.serviceType = serviceType;
                        data.messageType = messageType;
                        data.metaJson = metaJson;
                        data.aggregate = aggregateFlag;
                        data.fileName = extractFileName(part);
                        data.fileContent = extractFileContent(body, part);
                        
                        if (data.fileContent != null && data.fileContent.length > 0) {
                            dataList.add(data);
                        }
                    }
                }
            }
            
            System.out.println("  📦 Parsed " + dataList.size() + " files from multipart");
            return dataList;
        }
        
        private String extractBoundary(String contentType) {
            String[] parts = contentType.split(";");
            for (String part : parts) {
                part = part.trim();
                if (part.startsWith("boundary=")) {
                    return part.substring(9);
                }
            }
            return null;
        }
        
        private String extractFieldName(String part) {
            int nameIndex = part.indexOf("name=\"");
            if (nameIndex != -1) {
                int endIndex = part.indexOf("\"", nameIndex + 6);
                if (endIndex != -1) {
                    return part.substring(nameIndex + 6, endIndex);
                }
            }
            return null;
        }
        
        private String extractFileName(String part) {
            int nameIndex = part.indexOf("filename=\"");
            if (nameIndex != -1) {
                int endIndex = part.indexOf("\"", nameIndex + 10);
                if (endIndex != -1) {
                    return part.substring(nameIndex + 10, endIndex);
                }
            }
            return "document.edxml";
        }
        
        private String extractFieldValue(String part) {
            // Find content after double CRLF (after headers)
            int contentStart = part.indexOf("\r\n\r\n");
            if (contentStart != -1) {
                String value = part.substring(contentStart + 4).trim();
                // Remove trailing boundary if exists
                int boundaryEnd = value.indexOf("\r\n");
                if (boundaryEnd != -1) {
                    value = value.substring(0, boundaryEnd);
                }
                return value;
            }
            return null;
        }
        
        private byte[] extractFileContent(byte[] fullBody, String part) {
            // Simplified - extract binary content from multipart
            // In production, use proper multipart library
            try {
                int contentStart = part.indexOf("\r\n\r\n");
                if (contentStart != -1) {
                    String value = part.substring(contentStart + 4);
                    return value.trim().getBytes(StandardCharsets.UTF_8);
                }
            } catch (Exception e) {
                System.err.println("Error extracting file content: " + e.getMessage());
            }
            return new byte[0];
        }
    }
    
    // Multipart data holder
    static class MultipartData {
        String fromCode;
        String toCode;
        String serviceType;
        String messageType;
        String fileName;
        byte[] fileContent;
        String metaJson; // optional JSON metadata
        String aggregate; // optional: true/1/yes (for batch mode)
    }
    
    // Helper methods
    private static void sendResponse(HttpExchange exchange, int statusCode, Object data) throws IOException {
        String json = gson.toJson(data);
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        
        // HEAD request: send headers only, no body
        String method = exchange.getRequestMethod();
        if ("HEAD".equals(method)) {
            exchange.sendResponseHeaders(statusCode, -1);
        } else {
            exchange.sendResponseHeaders(statusCode, bytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(bytes);
            os.close();
        }
    }
    
    private static void sendError(HttpExchange exchange, int statusCode, String message) throws IOException {
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("message", message);
        error.put("timestamp", System.currentTimeMillis());
        
        sendResponse(exchange, statusCode, error);
    }
    
    private static String readRequestBody(HttpExchange exchange) throws IOException {
        InputStream is = exchange.getRequestBody();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        StringBuilder body = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            body.append(line);
        }
        return body.toString();
    }
    
    private static byte[] readRequestBodyBytes(HttpExchange exchange) throws IOException {
        InputStream is = exchange.getRequestBody();
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[16384];
        while ((nRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        buffer.flush();
        return buffer.toByteArray();
    }
}

