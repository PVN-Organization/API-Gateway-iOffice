package com.vpcp.gateway;

public class FilePart {
    private final String fileName;
    private final byte[] content;

    public FilePart(String fileName, byte[] content) {
        this.fileName = fileName;
        this.content = content;
    }

    public String getFileName() {
        return fileName;
    }

    public byte[] getContent() {
        return content;
    }
}
