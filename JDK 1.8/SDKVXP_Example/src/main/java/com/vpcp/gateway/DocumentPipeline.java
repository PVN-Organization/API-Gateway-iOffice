package com.vpcp.gateway;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.vpcp.services.KnobstickServiceImp;
import com.vpcp.services.VnptProperties;
import com.vpcp.services.model.GetChangeStatusResult;
import com.vpcp.services.model.GetEdocResult;
import com.vpcp.services.model.GetReceivedEdocResult;
import com.vpcp.services.model.SendEdocResult;

/**
 * Document Pipeline
 * 
 * Xử lý logic pipeline cho Document/Edoc APIs
 */
public class DocumentPipeline {
    
    private final KnobstickServiceImp knobstickService;
    private final VnptProperties vnptProperties;
    private final String uploadDir;
    private final String systemId;
    
    public DocumentPipeline(String endpoint, String systemId, String secret, 
                          String storePathDir, int maxConnection, int retry) {
        // Khởi tạo VXP Properties
        this.vnptProperties = new VnptProperties(
            endpoint, systemId, secret, storePathDir, maxConnection, retry
        );
        this.systemId = systemId;
        
        // Khởi tạo Knobstick Service
        this.knobstickService = new KnobstickServiceImp(vnptProperties);
        
        // Setup upload directory
        this.uploadDir = storePathDir + "/uploads";
        createUploadDir();
    }
    
    private void createUploadDir() {
        try {
            Files.createDirectories(Paths.get(uploadDir));
            System.out.println("✅ Upload directory: " + uploadDir);
        } catch (IOException e) {
            System.err.println("❌ Failed to create upload directory: " + e.getMessage());
        }
    }
    
    /**
     * Build meta: merge ENV defaults với API input
     * Priority: API input > ENV config
     */
    private EdxmlMeta buildMetaWithEnvDefaults(String metaJson) {
        EdxmlMeta meta = null;
        
        // Parse meta từ API nếu có
        if (metaJson != null && !metaJson.trim().isEmpty()) {
            try {
                meta = new com.google.gson.Gson().fromJson(metaJson, EdxmlMeta.class);
            } catch (Exception e) {
                System.err.println("⚠️  Failed to parse meta JSON: " + e.getMessage());
            }
        }
        
        // Nếu chưa có meta, tạo mới
        if (meta == null) {
            meta = new EdxmlMeta();
        }
        
        // Merge với ENV config (ENV làm default, API override)
        GatewayConfig.OrganizationInfo envOrg = GatewayConfig.getOrganizationInfo();
        
        if (envOrg.isConfigured()) {
            // Default FROM từ ENV nếu API không có
            if (meta.from == null) {
                meta.from = envOrg.toPartyMeta();
                System.out.println("📌 Using FROM from ENV: " + envOrg.organId);
            } else {
                // Merge: API có gì thì giữ, không có thì lấy từ ENV
                if (meta.from.organId == null || meta.from.organId.trim().isEmpty()) {
                    meta.from.organId = envOrg.organId;
                }
                if (meta.from.organizationInCharge == null || meta.from.organizationInCharge.trim().isEmpty()) {
                    meta.from.organizationInCharge = envOrg.organizationInCharge;
                }
                if (meta.from.organName == null || meta.from.organName.trim().isEmpty()) {
                    meta.from.organName = envOrg.organName;
                }
                if (meta.from.organAdd == null || meta.from.organAdd.trim().isEmpty()) {
                    meta.from.organAdd = envOrg.organAdd;
                }
                if (meta.from.email == null || meta.from.email.trim().isEmpty()) {
                    meta.from.email = envOrg.email;
                }
                if (meta.from.telephone == null || meta.from.telephone.trim().isEmpty()) {
                    meta.from.telephone = envOrg.telephone;
                }
                if (meta.from.fax == null || meta.from.fax.trim().isEmpty()) {
                    meta.from.fax = envOrg.fax;
                }
                if (meta.from.website == null || meta.from.website.trim().isEmpty()) {
                    meta.from.website = envOrg.website;
                }
            }
        }
        
        return meta;
    }
    
    /**
     * Pipeline: Gửi văn bản (Upload document)
     */
    public SendDocumentResponse sendDocument(SendDocumentRequest request) {
        System.out.println("⚙️ Pipeline: sendDocument");
        
        // Step 1: Validate input
        System.out.println("  Step 1: Validating input...");
        if (request == null) {
            return new SendDocumentResponse(false, "Request is required", null);
        }
        
        // fromCode no longer required (will be taken from meta for EDXML; header uses systemId)
        
        if (request.getFileContent() == null || request.getFileContent().length == 0) {
            return new SendDocumentResponse(false, "File content is required", null);
        }
        
        if (request.getFileName() == null || request.getFileName().isEmpty()) {
            return new SendDocumentResponse(false, "File name is required", null);
        }
        
        // Step 2: Save uploaded file
        System.out.println("  Step 2: Saving uploaded file...");
        String filePath = null;
        try {
            filePath = saveUploadedFile(request.getFileName(), request.getFileContent());
            System.out.println("  ✅ File saved: " + filePath);
        } catch (IOException e) {
            System.err.println("  ❌ Failed to save file: " + e.getMessage());
            return new SendDocumentResponse(false, "Failed to save file: " + e.getMessage(), null);
        }
        
        // Step 3: Validate file
        System.out.println("  Step 3: Validating file format...");
        EdxmlValidator.ValidationResult validation = EdxmlValidator.validateFile(filePath);
        System.out.println("  Validation: " + validation.getMessage());
        
        // Step 4: Wrap file vào EDXML (Gateway tự động đóng gói!)
        String finalFilePath = filePath;
        if (request.isForceWrap() || !validation.isValid()) {
            System.out.println("  Step 4: File không phải XML, đóng gói vào EDXML...");
            System.out.println("  From: " + request.getFromCode() + " → To: " + request.getToCode());
            try {
                // Build meta: merge ENV config với API input
                EdxmlMeta meta = buildMetaWithEnvDefaults(request.getMetaJson());
                
                finalFilePath = EdxmlBuilder.wrapFileToEdxml(filePath, request.getFromCode(), request.getToCode(), meta);
                System.out.println("  ✅ File đã được wrap thành EDXML");
            } catch (IOException e) {
                System.err.println("  ❌ Failed to wrap EDXML: " + e.getMessage());
                return new SendDocumentResponse(false, "Failed to wrap file to EDXML: " + e.getMessage(), null);
            }
        } else {
            System.out.println("  Step 4: File đã là XML, dùng trực tiếp");
        }
        
        // Step 5: Build JSON header
        System.out.println("  Step 5: Building request header...");
        // Header 'from' luôn dùng systemId (theo yêu cầu VXP), EDXML From/To lấy từ meta
        String jsonHeader = buildSendEdocHeader(
            this.systemId,
            request.getServiceType(),
            request.getMessageType()
        );
        
        // Step 6: Call VXP SDK
        System.out.println("  Step 6: Calling VXP SDK...");
        System.out.println("  File path: " + finalFilePath);
        SendEdocResult sdkResult = null;
        try {
            sdkResult = knobstickService.sendEdoc(jsonHeader, finalFilePath);
        } catch (Exception e) {
            System.err.println("  ❌ SDK call failed: " + e.getMessage());
            
            String errorMsg = "VXP SDK error: " + e.getMessage();
            if (!validation.isValid()) {
                errorMsg += "\n\nFile validation warning: " + validation.getMessage();
            }
            
            return new SendDocumentResponse(false, errorMsg, null);
        }
        
        // Step 6: Validate response
        System.out.println("  Step 6: Validating response...");
        if (sdkResult == null) {
            String errorMsg = "VXP Server không phản hồi";
            if (!validation.isValid()) {
                errorMsg += "\n\nNguyên nhân có thể: " + validation.getMessage();
            }
            return new SendDocumentResponse(false, errorMsg, null);
        }
        
        if ("OK".equals(sdkResult.getStatus())) {
            System.out.println("  ✅ Document sent successfully");
            System.out.println("  📄 Document ID: " + sdkResult.getDocID());
            
            // Note: No mock storage. Rely on VXP server persistence only.
            
            String message = "Document sent successfully";
            if (!validation.isValid()) {
                message += " (File đã được Gateway wrap thành EDXML)";
            }
            
            return new SendDocumentResponse(
                true,
                message,
                new DocumentInfo(
                    sdkResult.getDocID(),
                    request.getFileName(),
                    request.getFromCode(),
                    System.currentTimeMillis()
                )
            );
        } else {
            System.out.println("  ❌ Send failed: " + sdkResult.getErrorDesc());
            
            String errorMsg = "VXP Server error: " + sdkResult.getErrorDesc();
            if (!validation.isValid()) {
                errorMsg += "\n\nFile validation: " + validation.getMessage();
            }
            
            return new SendDocumentResponse(false, errorMsg, null);
        }
    }

    /**
     * Pipeline: Gửi NHIỀU file gộp vào 1 EDXML (Aggregate)
     */
    public SendDocumentResponse sendDocumentsAggregated(String fromCode, String toCodeComma,
                                                        java.util.List<FilePart> files,
                                                        String metaJson) {
        try {
            System.out.println("⚙️ Pipeline: sendDocumentsAggregated");
            if (files == null || files.isEmpty()) {
                return new SendDocumentResponse(false, "files are required", null);
            }

            // Build meta: merge ENV defaults với API input
            EdxmlMeta meta = buildMetaWithEnvDefaults(metaJson);

            // Derive fromCode from meta if not provided
            if (fromCode == null || fromCode.trim().isEmpty()) {
                if (meta != null && meta.from != null && meta.from.organId != null) {
                    fromCode = meta.from.organId;
                    System.out.println("📌 Derived fromCode from meta: " + fromCode);
                } else {
                    return new SendDocumentResponse(false, "fromCode required (provide directly or via meta.from.organId)", null);
                }
            }

            // Derive toCode from meta if not provided
            if (toCodeComma == null || toCodeComma.trim().isEmpty()) {
                if (meta != null && meta.to != null && !meta.to.isEmpty()) {
                    java.util.List<String> organIds = new java.util.ArrayList<>();
                    for (EdxmlMeta.PartyMeta party : meta.to) {
                        if (party.organId != null) organIds.add(party.organId);
                    }
                    toCodeComma = String.join(",", organIds);
                    System.out.println("📌 Derived toCode from meta: " + toCodeComma);
                } else {
                    return new SendDocumentResponse(false, "toCode required (provide directly or via meta.to[].organId)", null);
                }
            }

            // Build aggregated EDXML using SDK builder
            java.util.List<String> toCodes = new java.util.ArrayList<>();
            for (String c : toCodeComma.split(",")) { if (c != null && !c.trim().isEmpty()) toCodes.add(c.trim()); }
            String edxmlPath = EdxmlAggregateBuilder.buildAggregatedEdxml(this.uploadDir, fromCode, toCodes, files, meta);

            // Header JSON (use systemId for SDK header)
            String jsonHeader = buildSendEdocHeader(systemId, "eDoc", "edoc");

            // Send via SDK
            SendEdocResult sdkResult = knobstickService.sendEdoc(jsonHeader, edxmlPath);
            if (sdkResult == null || sdkResult.getStatus() == null || !"OK".equals(sdkResult.getStatus())) {
                String err = sdkResult != null ? sdkResult.getErrorDesc() : "null response";
                return new SendDocumentResponse(false, "VXP SDK error: " + err, null);
            }

            // Success
            DocumentInfo doc = new DocumentInfo(sdkResult.getDocID(), new java.io.File(edxmlPath).getName(), fromCode, System.currentTimeMillis());
            return new SendDocumentResponse(true, "Aggregated EDXML sent successfully", doc);
        } catch (Exception e) {
            return new SendDocumentResponse(false, "Aggregate error: " + e.getMessage(), null);
        }
    }
    
    /**
     * Pipeline: Lấy danh sách văn bản đến
     */
    public ReceivedDocumentsResponse getReceivedDocuments(String fromDate, String toDate, String status, Integer limit) {
        System.out.println("⚙️ Pipeline: getReceivedDocuments");
        System.out.println("  Params: fromDate=" + fromDate + ", toDate=" + toDate + ", status=" + status + ", limit=" + limit);
        
        // Step 1: Build request with optional filters (and recommended params)
        System.out.println("  Step 1: Building request...");
        StringBuilder jsonBuilder = new StringBuilder("{\"servicetype\":\"eDoc\",\"messagetype\":\"edoc\"");
        // Always include 'from' = systemId (receiver org)
        jsonBuilder.append(",\"from\":\"").append(this.systemId).append("\"");
        
        if (fromDate != null) {
            jsonBuilder.append(",\"fromDate\":\"").append(fromDate).append("\"");
        }
        if (toDate != null) {
            jsonBuilder.append(",\"toDate\":\"").append(toDate).append("\"");
        }
        if (status != null) {
            jsonBuilder.append(",\"status\":\"").append(status).append("\"");
        }
        if (limit != null && limit > 0) {
            // Map limit to pageSize; default pageIndex = 1
            jsonBuilder.append(",\"pageSize\":").append(limit);
            jsonBuilder.append(",\"pageIndex\":1");
        }
        
        jsonBuilder.append("}");
        String jsonHeader = jsonBuilder.toString();
        System.out.println("  Request JSON: " + jsonHeader);
        
        // Step 2: Call SDK
        System.out.println("  Step 2: Calling VXP SDK...");
        GetReceivedEdocResult sdkResult = knobstickService.getReceivedEdocList(jsonHeader);
        
        // Step 3: Validate
        System.out.println("  Step 3: Validating response...");
        if (sdkResult == null) {
            throw new RuntimeException("SDK returned null response");
        }
        
        if (!"OK".equals(sdkResult.getStatus())) {
            throw new RuntimeException("SDK error: " + sdkResult.getErrorDesc());
        }
        
        // Step 4: Transform data
        System.out.println("  Step 4: Transforming data...");
        java.util.List<DocumentSummary> documents = new java.util.ArrayList<>();
        
        // Always use VXP server data; do not fallback to any mock storage
        boolean hasVxpDocuments = (sdkResult.getKnobsticks() != null && sdkResult.getKnobsticks().size() > 0);
        if (!hasVxpDocuments) {
            System.out.println("  ℹ️ VXP Server returned 0 documents");
        } else {
            System.out.println("  Using VXP Server data...");
        }
        
        if (sdkResult.getKnobsticks() != null) {
            for (int i = 0; i < sdkResult.getKnobsticks().size(); i++) {
                com.vpcp.services.model.Knobstick knob = sdkResult.getKnobsticks().get(i);
                
                // Get available fields (some may not exist in SDK model)
                String fromCode = null;
                String toCode = null;
                String messageType = null;
                String serviceType = null;
                
                try { fromCode = (String) knob.getClass().getMethod("getFromCode").invoke(knob); } catch (Exception e) {}
                try { toCode = (String) knob.getClass().getMethod("getToCode").invoke(knob); } catch (Exception e) {}
                try { messageType = (String) knob.getClass().getMethod("getMessageType").invoke(knob); } catch (Exception e) {}
                try { serviceType = (String) knob.getClass().getMethod("getServiceType").invoke(knob); } catch (Exception e) {}
                
                documents.add(new DocumentSummary(
                    knob.getId(),
                    fromCode,
                    toCode,
                    messageType,
                    serviceType
                ));
            }
        }
        
        // Step 5: Apply limit if specified
        java.util.List<DocumentSummary> finalDocuments = documents;
        if (limit != null && limit > 0 && documents.size() > limit) {
            finalDocuments = documents.subList(0, limit);
            System.out.println("  ⚙️ Applied limit: " + limit + " (from " + documents.size() + " total)");
        }
        
        System.out.println("  ✅ Retrieved " + finalDocuments.size() + " documents");
        
        return new ReceivedDocumentsResponse(documents.size(), finalDocuments);
    }
    
    /**
     * Pipeline: Lấy chi tiết văn bản
     */
    public GetDocumentResponse getDocumentDetail(String docId) {
        System.out.println("⚙️ Pipeline: getDocumentDetail - " + docId);
        
        // Step 1: Validate
        System.out.println("  Step 1: Validating input...");
        if (docId == null || docId.isEmpty()) {
            return new GetDocumentResponse(false, "Document ID is required", null, null);
        }
        
        // Step 2: Build request
        System.out.println("  Step 2: Building request...");
        String jsonHeader = "{\"filePath\":\"xml\",\"docId\":\"" + docId + "\"}";
        
        // Step 3: Call SDK
        System.out.println("  Step 3: Calling VXP SDK...");
        GetEdocResult sdkResult = knobstickService.getEdoc(jsonHeader);
        
        // Step 4: Validate
        System.out.println("  Step 4: Validating response...");
        if (sdkResult == null) {
            return new GetDocumentResponse(false, "SDK returned null response", null, null);
        }
        
        if ("OK".equals(sdkResult.getStatus())) {
            System.out.println("  ✅ Document retrieved successfully");
            return new GetDocumentResponse(true, "Document retrieved successfully", sdkResult.getData(), sdkResult.getFilePath());
        } else {
            System.out.println("  ❌ Get failed: " + sdkResult.getErrorDesc());
            return new GetDocumentResponse(false, sdkResult.getErrorDesc(), null, null);
        }
    }

    /**
     * Pipeline: Lấy chi tiết văn bản và PARSE EDXML
     */
    public GetDocumentParsedResponse getDocumentDetailParsed(String docId) {
        GetDocumentResponse raw = getDocumentDetail(docId);
        if (!raw.isSuccess() || raw.getData() == null) {
            return new GetDocumentParsedResponse(false, raw.getMessage(), null, null);
        }
        ParsedEdxml parsed = EdxmlParser.parse(raw.getData());
        if (parsed == null) {
            return new GetDocumentParsedResponse(false, "Failed to parse EDXML", null, raw.getData());
        }
        return new GetDocumentParsedResponse(true, "Parsed successfully", parsed, raw.getData());
    }
    
    /**
     * Pipeline: Cập nhật trạng thái văn bản
     */
    public UpdateStatusResponse updateDocumentStatus(String docId, String status) {
        System.out.println("⚙️ Pipeline: updateDocumentStatus");
        
        // Step 1: Validate
        System.out.println("  Step 1: Validating input...");
        if (docId == null || docId.isEmpty()) {
            return new UpdateStatusResponse(false, "Document ID is required");
        }
        
        if (status == null || status.isEmpty()) {
            return new UpdateStatusResponse(false, "Status is required");
        }
        
        // Validate status value
        String[] validStatuses = {"inbox", "rejection", "acceptance", "assignment", 
                                 "processing", "done", "fail", "cancellation"};
        boolean validStatus = false;
        for (String valid : validStatuses) {
            if (valid.equals(status)) {
                validStatus = true;
                break;
            }
        }
        
        if (!validStatus) {
            return new UpdateStatusResponse(false, "Invalid status value");
        }
        
        // Step 2: Build request
        System.out.println("  Step 2: Building request...");
        String jsonHeader = "{\"status\":\"" + status + "\",\"docId\":\"" + docId + "\"}";
        
        // Step 3: Call SDK
        System.out.println("  Step 3: Calling VXP SDK...");
        GetChangeStatusResult sdkResult = knobstickService.updateStatus(jsonHeader);
        
        // Step 4: Validate
        System.out.println("  Step 4: Validating response...");
        if (sdkResult == null) {
            return new UpdateStatusResponse(false, "SDK returned null response");
        }
        
        if ("OK".equals(sdkResult.getStatus())) {
            System.out.println("  ✅ Status updated successfully");
            return new UpdateStatusResponse(true, "Status updated successfully");
        } else {
            System.out.println("  ❌ Update failed: " + sdkResult.getErrorDesc());
            return new UpdateStatusResponse(false, sdkResult.getErrorDesc());
        }
    }
    
    // Helper methods
    
    private String saveUploadedFile(String fileName, byte[] content) throws IOException {
        String filePath = uploadDir + "/" + System.currentTimeMillis() + "_" + fileName;
        Files.write(Paths.get(filePath), content);
        return filePath;
    }
    
    private String buildSendEdocHeader(String from, String serviceType, String messageType) {
        if (serviceType == null) serviceType = "eDoc";
        if (messageType == null) messageType = "edoc";
        
        return String.format(
            "{\"from\":\"%s\",\"servicetype\":\"%s\",\"messagetype\":\"%s\"}",
            from, serviceType, messageType
        );
    }
}

