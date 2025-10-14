package com.vpcp.gateway;

public class GetDocumentParsedResponse {
    private boolean success;
    private String message;
    private ParsedEdxml parsed;
    private String raw;

    public GetDocumentParsedResponse() {}

    public GetDocumentParsedResponse(boolean success, String message, ParsedEdxml parsed, String raw) {
        this.success = success;
        this.message = message;
        this.parsed = parsed;
        this.raw = raw;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public ParsedEdxml getParsed() { return parsed; }
    public void setParsed(ParsedEdxml parsed) { this.parsed = parsed; }
    public String getRaw() { return raw; }
    public void setRaw(String raw) { this.raw = raw; }
}


