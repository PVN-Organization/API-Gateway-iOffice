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

    // Basic document info
    public String subject;            // Subject
    public String content;            // Content/summary
    public String codeNumber;         // CodeNumber (auto-generated if null)
    public String codeNotation;       // CodeNotation
    public String place;              // PromulgationInfo.Place
    public String promulgationDate;   // PromulgationInfo.PromulgationDate (auto-generated if null)

    // DocumentType
    public String typeName;           // DocumentType.TypeName
    public String type;               // DocumentType.Type
    public String typeDetail;         // DocumentType.TypeDetail

    // Signer
    public String signerFullName;     // SignerInfo.FullName
    public String signerPosition;     // SignerInfo.Position

    // OtherInfo
    public String priority;           // OtherInfo.Priority
    public String sphereOfPromulgation; // OtherInfo.SphereOfPromulgation
    public String typerNotation;      // OtherInfo.TyperNotation
    public String promulgationAmount; // OtherInfo.PromulgationAmount
    public String pageAmount;         // OtherInfo.PageAmount
    public String direction;          // OtherInfo.Direction
    public List<String> appendixes;   // OtherInfo.Appendixes

    // Steering & Places
    public String steeringType;       // SteeringType
    public List<String> toPlaces;     // ToPlaces

    // Utility: ensure lists not null
    public List<PartyMeta> getToOrEmpty() {
        return to != null ? to : new ArrayList<PartyMeta>();
    }
    
    public List<String> getAppendixesOrEmpty() {
        return appendixes != null ? appendixes : new ArrayList<String>();
    }
    
    public List<String> getToPlacesOrEmpty() {
        return toPlaces != null ? toPlaces : new ArrayList<String>();
    }
}
