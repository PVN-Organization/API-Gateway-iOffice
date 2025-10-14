package com.vpcp.gateway;

/**
 * Gateway Configuration từ Environment Variables
 */
public class GatewayConfig {
    
    // Thông tin đơn vị (từ env vars)
    public static class OrganizationInfo {
        public final String organId;
        public final String organizationInCharge;
        public final String organName;
        public final String organAdd;
        public final String email;
        public final String telephone;
        public final String fax;
        public final String website;
        
        public OrganizationInfo() {
            this.organId = getEnv("ORG_ID", null);
            this.organizationInCharge = getEnv("ORG_IN_CHARGE", null);
            this.organName = getEnv("ORG_NAME", null);
            this.organAdd = getEnv("ORG_ADDRESS", null);
            this.email = getEnv("ORG_EMAIL", null);
            this.telephone = getEnv("ORG_TELEPHONE", null);
            this.fax = getEnv("ORG_FAX", null);
            this.website = getEnv("ORG_WEBSITE", null);
        }
        
        public boolean isConfigured() {
            return organId != null && !organId.trim().isEmpty();
        }
        
        public EdxmlMeta.PartyMeta toPartyMeta() {
            EdxmlMeta.PartyMeta party = new EdxmlMeta.PartyMeta();
            party.organId = this.organId;
            party.organizationInCharge = this.organizationInCharge;
            party.organName = this.organName;
            party.organAdd = this.organAdd;
            party.email = this.email;
            party.telephone = this.telephone;
            party.fax = this.fax;
            party.website = this.website;
            return party;
        }
    }
    
    private static final OrganizationInfo orgInfo = new OrganizationInfo();
    
    public static OrganizationInfo getOrganizationInfo() {
        return orgInfo;
    }
    
    private static String getEnv(String key, String defaultValue) {
        String value = System.getenv(key);
        return (value != null && !value.trim().isEmpty()) ? value : defaultValue;
    }
    
    // Log config khi khởi động
    public static void logConfig() {
        System.out.println("📋 Gateway Configuration:");
        if (orgInfo.isConfigured()) {
            System.out.println("  ✅ Organization Info (from ENV):");
            System.out.println("    - ID: " + orgInfo.organId);
            System.out.println("    - In Charge: " + orgInfo.organizationInCharge);
            System.out.println("    - Name: " + orgInfo.organName);
            System.out.println("    - Address: " + orgInfo.organAdd);
            System.out.println("    - Email: " + orgInfo.email);
            System.out.println("    - Telephone: " + orgInfo.telephone);
        } else {
            System.out.println("  ⚠️  No organization info in ENV - will use meta from API requests");
        }
    }
}

