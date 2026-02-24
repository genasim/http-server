package bg.sofia.uni.fmi.mjt.httpserver.entities;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HttpRequestTest {
    private static final String VALID_METHOD = "GET";
    private static final String VALID_URI = "/test";
    private static final String VALID_VERSION = "HTTP/1.1";
    private static final String REMOTE_ADDRESS = "127.0.0.1";
    private static final int PORT = 8080;

    @Test
    void testHttpRequestCreation() {
        LocalDateTime timestamp = LocalDateTime.now();
        HttpRequest request = new HttpRequest(
            REMOTE_ADDRESS, PORT, timestamp, VALID_METHOD, VALID_URI, VALID_VERSION, Collections.emptyMap(), ""
        );

        assertEquals(REMOTE_ADDRESS, request.remoteAddress(), "Remote address should match");
        assertEquals(PORT, request.port(), "Port should match");
        assertEquals(timestamp, request.timestamp(), "Timestamp should match");
        assertEquals(VALID_METHOD, request.method(), "Method should match");
        assertEquals(VALID_URI, request.uri(), "URI should match");
        assertEquals(VALID_VERSION, request.version(), "Version should match");
        assertEquals(Collections.emptyMap(), request.headers(), "Headers should be empty");
        assertEquals("", request.body(), "Body should be empty");
        assertEquals(Collections.emptyMap(), request.params(), "Params should be empty");
    }

    @Test
    void testNullMethodThrows() {
        assertThrows(IllegalArgumentException.class, () -> new HttpRequest(
            REMOTE_ADDRESS, PORT, LocalDateTime.now(), null, VALID_URI, VALID_VERSION, Collections.emptyMap(), ""
        ), "Null method should throw IllegalArgumentException");
    }

    @Test
    void testBlankMethodThrows() {
        assertThrows(IllegalArgumentException.class, () -> new HttpRequest(
            REMOTE_ADDRESS, PORT, LocalDateTime.now(), " ", VALID_URI, VALID_VERSION, Collections.emptyMap(), ""
        ), "Blank method should throw IllegalArgumentException");
    }

    @Test
    void testNullUriThrows() {
        assertThrows(IllegalArgumentException.class, () -> new HttpRequest(
            REMOTE_ADDRESS, PORT, LocalDateTime.now(), VALID_METHOD, null, VALID_VERSION, Collections.emptyMap(), ""
        ), "Null URI should throw IllegalArgumentException");
    }

    @Test
    void testBlankUriThrows() {
        assertThrows(IllegalArgumentException.class, () -> new HttpRequest(
            REMOTE_ADDRESS, PORT, LocalDateTime.now(), VALID_METHOD, " ", VALID_VERSION, Collections.emptyMap(), ""
        ), "Blank URI should throw IllegalArgumentException");
    }

    @Test
    void testNullVersionThrows() {
        assertThrows(IllegalArgumentException.class, () -> new HttpRequest(
            REMOTE_ADDRESS, PORT, LocalDateTime.now(), VALID_METHOD, VALID_URI, null, Collections.emptyMap(), ""
        ), "Null version should throw IllegalArgumentException");
    }

    @Test
    void testBlankVersionThrows() {
        assertThrows(IllegalArgumentException.class, () -> new HttpRequest(
            REMOTE_ADDRESS, PORT, LocalDateTime.now(), VALID_METHOD, VALID_URI, " ", Collections.emptyMap(), ""
        ), "Blank version should throw IllegalArgumentException");
    }

    @Test
    void testUnsupportedHttpMethodThrows() {
        assertThrows(IllegalArgumentException.class, () -> new HttpRequest(
            REMOTE_ADDRESS, PORT, LocalDateTime.now(), "INVALID", VALID_URI, VALID_VERSION, Collections.emptyMap(), ""
        ), "Unsupported HTTP method should throw IllegalArgumentException");
    }

    @Test
    void testUnsupportedHttpVersionThrows() {
        assertThrows(IllegalArgumentException.class, () -> new HttpRequest(
            REMOTE_ADDRESS, PORT, LocalDateTime.now(), VALID_METHOD, VALID_URI, "HTTP/1.0", Collections.emptyMap(), ""
        ), "Unsupported HTTP version should throw IllegalArgumentException");
    }
}
