package bg.sofia.uni.fmi.mjt.httpserver.entities;

public enum CommonHeaders {
    SERVER("server"),
    DATE("date"),
    CONTENT_LENGTH("content-length"),
    CONTENT_TYPE("content-type"),
    ALLOW("allow");

    CommonHeaders(String contentType) {
        this.contentType = contentType;
    }

    private final String contentType;

    public String value() {
        return contentType;
    }
}
