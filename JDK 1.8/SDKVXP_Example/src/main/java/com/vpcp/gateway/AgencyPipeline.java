package com.vpcp.gateway;

import java.util.ArrayList;
import java.util.List;

import com.vpcp.services.AgencyServiceImp;
import com.vpcp.services.VnptProperties;
import com.vpcp.services.model.GetAgenciesResult;
import com.vpcp.services.model.RegisterAgencyResult;

/**
 * Agency Pipeline
 * 
 * Xử lý logic pipeline cho Agency APIs
 * Gọi VXP SDK và transform data
 */
public class AgencyPipeline {
    
    private final AgencyServiceImp agencyService;
    private final VnptProperties vnptProperties;
    
    public AgencyPipeline(String endpoint, String systemId, String secret, 
                         String storePathDir, int maxConnection, int retry) {
        // Khởi tạo VXP Properties
        this.vnptProperties = new VnptProperties(
            endpoint, systemId, secret, storePathDir, maxConnection, retry
        );
        
        // Khởi tạo Agency Service
        this.agencyService = new AgencyServiceImp(vnptProperties);
    }
    
    /**
     * Pipeline: Lấy danh sách tất cả agencies
     */
    public AgencyListResponse getAllAgencies() {
        System.out.println("⚙️ Pipeline: getAllAgencies");
        
        // Step 1: Gọi SDK
        System.out.println("  Step 1: Calling VXP SDK...");
        GetAgenciesResult sdkResult = agencyService.getAgenciesList("{}");
        
        // Step 2: Validate response
        System.out.println("  Step 2: Validating response...");
        if (sdkResult == null) {
            System.err.println("  ❌ SDK returned null");
            throw new RuntimeException("SDK returned null response");
        }
        
        System.out.println("  Status: " + sdkResult.getStatus());
        System.out.println("  ErrorDesc: " + sdkResult.getErrorDesc());
        
        if (!"OK".equals(sdkResult.getStatus())) {
            String errorMsg = sdkResult.getErrorDesc() != null ? sdkResult.getErrorDesc() : "Unknown error";
            System.err.println("  ❌ SDK error: " + errorMsg);
            throw new RuntimeException("SDK error: " + errorMsg);
        }
        
        // Step 3: Transform data
        System.out.println("  Step 3: Transforming data...");
        List<AgencyDetail> agencies = new ArrayList<>();
        
        if (sdkResult.getAgencies() != null) {
            for (int i = 0; i < sdkResult.getAgencies().size(); i++) {
                com.vpcp.services.model.Agency agency = sdkResult.getAgencies().get(i);
                
                // Only get available fields from Agency model
                // Some fields may not be available in SDK model
                agencies.add(new AgencyDetail(
                    agency.getId(),
                    agency.getCode(),
                    agency.getName(),
                    null,  // pcode - not available in SDK model
                    agency.getMail(),
                    null,  // mobile - not available in SDK model  
                    null   // fax - not available in SDK model
                ));
            }
        }
        
        // Step 4: Return response
        System.out.println("  Step 4: Building response...");
        return new AgencyListResponse(
            agencies.size(),
            agencies
        );
    }
    
    /**
     * Pipeline: Lấy thông tin 1 agency theo ID
     */
    public AgencyDetail getAgencyById(String id) {
        System.out.println("⚙️ Pipeline: getAgencyById - " + id);
        
        // Step 1: Get all agencies (cache có thể thêm sau)
        AgencyListResponse allAgencies = getAllAgencies();
        
        // Step 2: Find by ID
        System.out.println("  Step 2: Searching for agency ID: " + id);
        for (AgencyDetail agency : allAgencies.getAgencies()) {
            if (id.equals(agency.getId()) || id.equals(agency.getCode())) {
                System.out.println("  ✅ Found: " + agency.getName());
                return agency;
            }
        }
        
        System.out.println("  ❌ Not found");
        return null;
    }
    
    /**
     * Pipeline: Đăng ký/cập nhật agency
     */
    public RegisterResult registerAgency(AgencyRequest request) {
        System.out.println("⚙️ Pipeline: registerAgency");
        
        // Step 1: Validate input
        System.out.println("  Step 1: Validating input...");
        if (request == null) {
            return new RegisterResult(false, "Request body is required");
        }
        
        if (request.getCode() == null || request.getCode().isEmpty()) {
            return new RegisterResult(false, "Agency code is required");
        }
        
        if (request.getName() == null || request.getName().isEmpty()) {
            return new RegisterResult(false, "Agency name is required");
        }
        
        // Step 2: Build JSON data
        System.out.println("  Step 2: Building request data...");
        String data = String.format(
            "{\"id\":\"%s\",\"pcode\":\"%s\",\"code\":\"%s\",\"name\":\"%s\",\"mail\":\"%s\",\"mobile\":\"%s\",\"fax\":\"%s\"}",
            request.getId() != null ? request.getId() : "",
            request.getPcode() != null ? request.getPcode() : "",
            request.getCode(),
            request.getName(),
            request.getMail() != null ? request.getMail() : "",
            request.getMobile() != null ? request.getMobile() : "",
            request.getFax() != null ? request.getFax() : ""
        );
        
        // Step 3: Call SDK
        System.out.println("  Step 3: Calling VXP SDK...");
        RegisterAgencyResult sdkResult = agencyService.registerAgency("{}", data);
        
        // Step 4: Validate response
        System.out.println("  Step 4: Validating response...");
        if (sdkResult == null) {
            return new RegisterResult(false, "SDK returned null response");
        }
        
        if ("OK".equals(sdkResult.getStatus())) {
            System.out.println("  ✅ Register success");
            return new RegisterResult(true, "Agency registered successfully");
        } else {
            System.out.println("  ❌ Register failed: " + sdkResult.getErrorDesc());
            return new RegisterResult(false, sdkResult.getErrorDesc());
        }
    }
}

