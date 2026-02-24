package bg.sofia.uni.fmi.mjt.httpserver.entities;

public enum ContentType {
    TEXT("text/plain"),
    FILE("application/octet-stream"),
    JSON("application/json");

    private final String mime;

    ContentType(String mime) {
        this.mime = mime;
    }

    public String mime() {
        return mime;
    }
}
