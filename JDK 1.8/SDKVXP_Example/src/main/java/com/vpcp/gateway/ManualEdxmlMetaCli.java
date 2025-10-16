package com.vpcp.gateway;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ManualEdxmlMetaCli {
    public static void main(String[] args) throws Exception {
        String metaPath = args.length > 0 ? args[0] : "meta_local.json";
        String metaJson = new String(Files.readAllBytes(Paths.get(metaPath)), "UTF-8");
        com.google.gson.Gson gson = new com.google.gson.Gson();
        EdxmlMeta meta = gson.fromJson(metaJson, EdxmlMeta.class);

        String outputDir = "JDK 1.8/SDKVXP_Example/uploads";
        java.nio.file.Files.createDirectories(java.nio.file.Paths.get(outputDir));

        List<FilePart> files = new ArrayList<>();
        files.add(new FilePart("metaA.txt", "META A".getBytes("UTF-8")));
        files.add(new FilePart("metaB.txt", "META B".getBytes("UTF-8")));

        String fromCode = meta != null && meta.from != null ? meta.from.organId : "vxp.saas.03";
        java.util.List<String> toCodes = new java.util.ArrayList<>();
        if (meta != null && meta.to != null) {
            for (EdxmlMeta.PartyMeta p : meta.to) {
                if (p != null && p.organId != null) toCodes.add(p.organId);
            }
        }
        if (toCodes.isEmpty()) toCodes.add("vxp.saas.02");

        String edxmlPath = ManualEdxmlBuilder.buildAggregatedEdxml(outputDir, fromCode, toCodes, files, meta);
        System.out.println("EDXML generated at: " + edxmlPath);

        String xml = new String(Files.readAllBytes(Paths.get(edxmlPath)), "UTF-8");
        ParsedEdxml parsed = EdxmlParser.parse(xml);
        System.out.println("Attachments: " + (parsed.attachments != null ? parsed.attachments.size() : 0));
        if (parsed.attachments != null) {
            int i = 1;
            for (ParsedEdxml.AttachmentInfo ai : parsed.attachments) {
                System.out.println("-- #" + (i++) + ": " + ai.attachmentName + " | " + ai.contentType + " | " + ai.contentId);
                System.out.println("   Decoded: " + (ai.decodedContent != null ? ai.decodedContent : "<null>"));
            }
        }
    }
}
