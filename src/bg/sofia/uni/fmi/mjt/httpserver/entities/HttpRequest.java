package bg.sofia.uni.fmi.mjt.httpserver.entities;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public record HttpRequest(String remoteAddress, int port, LocalDateTime timestamp, String method, String uri,
                          String version, Map<String, String> headers, String body, Map<String, String> params) {
    private static final Set<String> supportedHttpVersions = Set.of("HTTP/1.1");

    private static final Set<String> supportedMethods =
        Arrays.stream(HttpVerbs.values()).map(Enum::name).collect(Collectors.toSet());

    private static boolean isHttpMethod(String verb) {
        return supportedMethods.contains(verb.toUpperCase());
    }

    public HttpRequest(String remoteAddress, int port, LocalDateTime timestamp, String method, String uri,
                       String version, Map<String, String> headers, String body) {
        this(remoteAddress, port, timestamp, method, uri, version, headers, body, new HashMap<>());
    }

    public HttpRequest {
        if (method == null || method.isBlank()) {
            throw new IllegalArgumentException("HTTP Method cannot be null or blank");
        }

        if (uri == null || uri.isBlank()) {
            throw new IllegalArgumentException("URI cannot be null or blank");
        }

        if (version == null || version.isBlank()) {
            throw new IllegalArgumentException("HTTP Version cannot be null or blank");
        }

        if (!isHttpMethod(method)) {
            throw new IllegalArgumentException("Cannot create request with unsupported HTTP Method: " + method);
        }

        if (!supportedHttpVersions.contains(version)) {
            throw new IllegalArgumentException("Cannot create request with unsupported HTTP Version: " + version);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        HttpRequest that = (HttpRequest) o;
        return port == that.port && remoteAddress.equals(that.remoteAddress) && timestamp.equals(that.timestamp);
    }

    @Override
    public int hashCode() {
        int result = remoteAddress.hashCode();
        result = 31 * result + port;
        result = 31 * result + timestamp.hashCode();
        return result;
    }
}
