package com.vpcp.gateway;

import java.util.ArrayList;
import java.util.List;

/**
 * EDXML Metadata overrides (optional)
 */
public class EdxmlMeta {
    public static class PartyMeta {
        public String organId;                 // code (e.g., vxp.saas.03)
        public String organizationInCharge;    // OrganizationInCharge
        public String organName;               // OrganName
        public String organAdd;                // Address
        public String email;
        public String telephone;
        public String fax;
        public String website;
    }

    // Header overrides
    public PartyMeta from;            // From party details
    public List<PartyMeta> to;        // Multiple recipients

    public String subject;            // Subject
    public String content;            // Content/summary
    public String codeNotation;       // CodeNotation
    public String place;              // PromulgationInfo.Place
    public String signerFullName;     // SignerInfo.FullName

    // Utility: ensure to list not null
    public List<PartyMeta> getToOrEmpty() {
        return to != null ? to : new ArrayList<PartyMeta>();
    }
}
