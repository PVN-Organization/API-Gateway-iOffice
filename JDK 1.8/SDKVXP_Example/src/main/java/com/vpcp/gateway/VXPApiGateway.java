package com.vpcp.gateway;

import static spark.Spark.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * VXP API Gateway - REST API Pipeline
 * 
 * API trung gian để gọi VXP SDK và trả về dữ liệu theo format chuẩn
 * 
 * Usage:
 *   java -cp "target/classes:lib/*" com.vpcp.gateway.VXPApiGateway
 * 
 * Endpoints:
 *   GET  /api/health              - Health check
 *   GET  /api/agencies            - Lấy danh sách đơn vị
 *   GET  /api/agencies/:id        - Lấy thông tin 1 đơn vị
 *   POST /api/agencies            - Đăng ký/cập nhật đơn vị
 */
public class VXPApiGateway {
    
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static AgencyPipeline agencyPipeline;
    
    public static void main(String[] args) {
        // Cấu hình port (mặc định 8080)
        int port = getPort(args);
        port(port);
        
        // Khởi tạo pipeline
        initializePipeline();
        
        // CORS
        enableCORS();
        
        // Routes
        setupRoutes();
        
        System.out.println("=====================================");
        System.out.println("🚀 VXP API Gateway Started!");
        System.out.println("=====================================");
        System.out.println("📡 Server running on: http://localhost:" + port);
        System.out.println("📖 API Documentation:");
        System.out.println("  GET  /api/health              - Health check");
        System.out.println("  GET  /api/agencies            - Get all agencies");
        System.out.println("  GET  /api/agencies/:id        - Get agency by ID");
        System.out.println("  POST /api/agencies            - Register/Update agency");
        System.out.println("=====================================");
    }
    
    private static void initializePipeline() {
        // Khởi tạo VXP connection
        String endpoint = "http://vxpdungchung.vnpt.vn";
        String systemId = "vxp.saas.03";
        String secret = "A7TKG/r2gOKTEpe1MhWx6zw8O4JfzN0KkCRS6HZY81Ve";
        String storePathDir = "/tmp/vxp";
        int maxConnection = 10;
        int retry = 3;
        
        agencyPipeline = new AgencyPipeline(endpoint, systemId, secret, storePathDir, maxConnection, retry);
        System.out.println("✅ Pipeline initialized successfully");
    }
    
    private static void setupRoutes() {
        // Health Check
        get("/api/health", (req, res) -> {
            res.type("application/json");
            return gson.toJson(new ApiResponse(
                true,
                "VXP API Gateway is running",
                new HealthStatus("healthy", System.currentTimeMillis())
            ));
        });
        
        // Get All Agencies
        get("/api/agencies", (req, res) -> {
            res.type("application/json");
            
            try {
                System.out.println("📥 Request: GET /api/agencies");
                
                AgencyListResponse result = agencyPipeline.getAllAgencies();
                
                System.out.println("✅ Response: " + result.getTotal() + " agencies");
                
                return gson.toJson(new ApiResponse(
                    true,
                    "Successfully retrieved agencies",
                    result
                ));
                
            } catch (Exception e) {
                System.err.println("❌ Error: " + e.getMessage());
                res.status(500);
                return gson.toJson(new ApiResponse(
                    false,
                    "Error: " + e.getMessage(),
                    null
                ));
            }
        });
        
        // Get Agency by ID
        get("/api/agencies/:id", (req, res) -> {
            res.type("application/json");
            
            try {
                String id = req.params(":id");
                System.out.println("📥 Request: GET /api/agencies/" + id);
                
                AgencyDetail agency = agencyPipeline.getAgencyById(id);
                
                if (agency != null) {
                    System.out.println("✅ Response: Found agency " + agency.getName());
                    return gson.toJson(new ApiResponse(
                        true,
                        "Agency found",
                        agency
                    ));
                } else {
                    res.status(404);
                    return gson.toJson(new ApiResponse(
                        false,
                        "Agency not found",
                        null
                    ));
                }
                
            } catch (Exception e) {
                System.err.println("❌ Error: " + e.getMessage());
                res.status(500);
                return gson.toJson(new ApiResponse(
                    false,
                    "Error: " + e.getMessage(),
                    null
                ));
            }
        });
        
        // Register/Update Agency
        post("/api/agencies", (req, res) -> {
            res.type("application/json");
            
            try {
                System.out.println("📥 Request: POST /api/agencies");
                
                // Parse request body
                AgencyRequest agencyReq = gson.fromJson(req.body(), AgencyRequest.class);
                
                RegisterResult result = agencyPipeline.registerAgency(agencyReq);
                
                if (result.isSuccess()) {
                    System.out.println("✅ Response: Agency registered successfully");
                    return gson.toJson(new ApiResponse(
                        true,
                        "Agency registered successfully",
                        result
                    ));
                } else {
                    res.status(400);
                    return gson.toJson(new ApiResponse(
                        false,
                        result.getMessage(),
                        null
                    ));
                }
                
            } catch (Exception e) {
                System.err.println("❌ Error: " + e.getMessage());
                res.status(500);
                return gson.toJson(new ApiResponse(
                    false,
                    "Error: " + e.getMessage(),
                    null
                ));
            }
        });
        
        // 404 Handler
        notFound((req, res) -> {
            res.type("application/json");
            return gson.toJson(new ApiResponse(
                false,
                "Endpoint not found: " + req.pathInfo(),
                null
            ));
        });
        
        // Exception Handler
        exception(Exception.class, (e, req, res) -> {
            res.type("application/json");
            res.status(500);
            res.body(gson.toJson(new ApiResponse(
                false,
                "Internal server error: " + e.getMessage(),
                null
            )));
        });
    }
    
    private static void enableCORS() {
        options("/*", (req, res) -> {
            String accessControlRequestHeaders = req.headers("Access-Control-Request-Headers");
            if (accessControlRequestHeaders != null) {
                res.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
            }
            
            String accessControlRequestMethod = req.headers("Access-Control-Request-Method");
            if (accessControlRequestMethod != null) {
                res.header("Access-Control-Allow-Methods", accessControlRequestMethod);
            }
            
            return "OK";
        });
        
        before((req, res) -> {
            res.header("Access-Control-Allow-Origin", "*");
            res.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            res.header("Access-Control-Allow-Headers", "Content-Type, Authorization");
        });
    }
    
    private static int getPort(String[] args) {
        if (args.length > 0) {
            try {
                return Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port number, using default 8080");
            }
        }
        return 8080;
    }
    
    // Response Models
    static class ApiResponse {
        private boolean success;
        private String message;
        private Object data;
        private long timestamp;
        
        public ApiResponse(boolean success, String message, Object data) {
            this.success = success;
            this.message = message;
            this.data = data;
            this.timestamp = System.currentTimeMillis();
        }
    }
    
    static class HealthStatus {
        private String status;
        private long timestamp;
        
        public HealthStatus(String status, long timestamp) {
            this.status = status;
            this.timestamp = timestamp;
        }
    }
}

