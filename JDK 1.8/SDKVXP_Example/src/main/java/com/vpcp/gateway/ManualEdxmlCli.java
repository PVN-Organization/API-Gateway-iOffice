package com.vpcp.gateway;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ManualEdxmlCli {
    public static void main(String[] args) throws Exception {
        String outputDir = "JDK 1.8/SDKVXP_Example/uploads";
        java.nio.file.Files.createDirectories(java.nio.file.Paths.get(outputDir));

        // Prepare sample files
        String a = "LOCAL AGG A";
        String b = "LOCAL AGG B";
        String c = "LOCAL AGG C";

        List<FilePart> files = new ArrayList<>();
        files.add(new FilePart("localA.txt", a.getBytes("UTF-8")));
        files.add(new FilePart("localB.txt", b.getBytes("UTF-8")));
        files.add(new FilePart("localC.txt", c.getBytes("UTF-8")));

        // Meta
        EdxmlMeta meta = new EdxmlMeta();
        meta.from = new EdxmlMeta.PartyMeta();
        meta.from.organId = "vxp.saas.03";
        meta.from.organName = "Đơn vị test vxp 3";
        meta.from.organizationInCharge = "Đơn vị test vxp 3";
        meta.subject = "Local Aggregate Test";
        meta.content = "ManualEdxmlBuilder local test";

        List<String> toCodes = Arrays.asList("vxp.saas.02");
        String edxmlPath = ManualEdxmlBuilder.buildAggregatedEdxml(outputDir, "vxp.saas.03", toCodes, files, meta);

        System.out.println("EDXML generated at: " + edxmlPath);

        // Print small summary counts
        String xml = new String(Files.readAllBytes(Paths.get(edxmlPath)), "UTF-8");
        int refs = count(xml, "<edXML:Reference>");
        int atts = count(xml, "<Attachment>");
        System.out.println("Reference count: " + refs);
        System.out.println("Attachment count: " + atts);
    }

    private static int count(String text, String token) {
        int idx = 0, cnt = 0;
        while ((idx = text.indexOf(token, idx)) >= 0) { cnt++; idx += token.length(); }
        return cnt;
    }
}
