package bg.sofia.uni.fmi.mjt.httpserver.entities;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public record HttpResponse(int statusCode, String statusMessage, Map<String, String> headers, byte[] body) {
    public static HttpResponse ok() {
        return new HttpResponse(StatusCode.OK.code(), StatusCode.OK.message(), new HashMap<>(createBaseHeaders()),
            null);
    }

    public static HttpResponse ok(String body) {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        return new HttpResponse(StatusCode.OK.code(), StatusCode.OK.message(), new HashMap<>(createBaseHeaders()),
            bytes)
            .headers(Map.entry(CommonHeaders.CONTENT_TYPE.value(), ContentType.TEXT.mime()),
                Map.entry(CommonHeaders.CONTENT_LENGTH.value(), Integer.toString(bytes.length)));
    }

    public static HttpResponse noContent() {
        return new HttpResponse(StatusCode.NO_CONTENT.code(), StatusCode.NO_CONTENT.message(),
            new HashMap<>(createBaseHeaders()), new byte[0]);
    }

    public static HttpResponse badRequest() {
        return new HttpResponse(StatusCode.BAD_REQUEST.code(), StatusCode.BAD_REQUEST.message(),
            new HashMap<>(createBaseHeaders()), new byte[0]);
    }

    public static HttpResponse notFound() {
        return new HttpResponse(StatusCode.NOT_FOUND.code(), StatusCode.NOT_FOUND.message(),
            new HashMap<>(createBaseHeaders()), new byte[0]);
    }

    public static HttpResponse methodNotAllowed() {
        return new HttpResponse(StatusCode.METHOD_NOT_ALLOWED.code(), StatusCode.METHOD_NOT_ALLOWED.message(),
            new HashMap<>(createBaseHeaders()), new byte[0]);
    }

    public static HttpResponse error(String message) {
        byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
        return new HttpResponse(StatusCode.INTERNAL_SERVER_ERROR.code(), StatusCode.INTERNAL_SERVER_ERROR.message(),
            new HashMap<>(createBaseHeaders()), bytes)
            .headers(Map.entry(CommonHeaders.CONTENT_TYPE.value(), ContentType.TEXT.mime()),
                Map.entry(CommonHeaders.CONTENT_LENGTH.value(), Integer.toString(bytes.length)));
    }

    public HttpResponse text(String body) {
        if (body == null) {
            throw new IllegalArgumentException("Cannot create response with null text body");
        }

        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        headers.put(CommonHeaders.CONTENT_TYPE.value(), ContentType.TEXT.mime());
        headers.put(CommonHeaders.CONTENT_LENGTH.value(), Integer.toString(bytes.length));
        return new HttpResponse(statusCode, statusMessage, new HashMap<>(headers), bytes);
    }

    public HttpResponse file(byte[] body) {
        if (body == null) {
            throw new IllegalArgumentException("Cannot create response with null file body");
        }

        headers.put(CommonHeaders.CONTENT_TYPE.value(), ContentType.FILE.mime());
        headers.put(CommonHeaders.CONTENT_LENGTH.value(), Integer.toString(body.length));
        return new HttpResponse(statusCode, statusMessage, new HashMap<>(headers), body);
    }

    public HttpResponse json(String body) {
        if (body == null) {
            throw new IllegalArgumentException("Cannot create response with null json body");
        }

        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        headers.put(CommonHeaders.CONTENT_TYPE.value(), ContentType.JSON.mime());
        headers.put(CommonHeaders.CONTENT_LENGTH.value(), Integer.toString(bytes.length));
        return new HttpResponse(statusCode, statusMessage, new HashMap<>(headers), bytes);
    }

    public HttpResponse status(int status, String message) {
        if (status <= 0 || status >= 600) {
            throw new IllegalArgumentException("Invalid HTTP status code: " + status);
        }
        return new HttpResponse(status, message, new HashMap<>(headers), body);
    }

    @SafeVarargs
    public final HttpResponse headers(Map.Entry<String, String>... headers) {
        if (headers == null) {
            throw new IllegalArgumentException("Cannot add null headers to response");
        }

        for (var header : headers) {
            this.headers.put(header.getKey(), header.getValue());
        }
        return new HttpResponse(statusCode, statusMessage, new HashMap<>(this.headers), body);
    }

    private static Map<String, String> createBaseHeaders() {
        var headers = new HashMap<String, String>();
        headers.put("Server", "fmi-torrent-server");

        String now = LocalDateTime.now().toString();
        headers.put("Date", now);

        return headers;
    }
}
