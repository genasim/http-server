package bg.sofia.uni.fmi.mjt.httpserver.entities;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HttpResponseTest {
    @Test
    void testOk() {
        HttpResponse response = HttpResponse.ok();
        assertEquals(StatusCode.OK.code(), response.statusCode(), "Status code should be 200");
        assertEquals(StatusCode.OK.message(), response.statusMessage(), "Status message should be OK");
        assertNull(response.body(), "Body should be null for OK response");
        assertTrue(response.headers().containsKey("Server"), "Response should contain Server header");
        assertTrue(response.headers().containsKey("Date"), "Response should contain Date header");
    }

    @Test
    void testOkWithBody() {
        String body = "test body";
        HttpResponse response = HttpResponse.ok(body);
        assertEquals(StatusCode.OK.code(), response.statusCode(), "Status code should be 200");
        assertEquals(StatusCode.OK.message(), response.statusMessage(), "Status message should be OK");
        assertArrayEquals(body.getBytes(StandardCharsets.UTF_8), response.body(),
            "Body should match the provided string");
        assertEquals(ContentType.TEXT.mime(), response.headers().get(CommonHeaders.CONTENT_TYPE.value()),
            "Content-Type should be text/plain");
        assertEquals(Integer.toString(body.length()), response.headers().get(CommonHeaders.CONTENT_LENGTH.value()),
            "Content-Length should match the body length");
        assertTrue(response.headers().containsKey("Server"), "Response should contain Server header");
        assertTrue(response.headers().containsKey("Date"), "Response should contain Date header");
    }

    @Test
    void testNoContent() {
        HttpResponse response = HttpResponse.noContent();
        assertEquals(StatusCode.NO_CONTENT.code(), response.statusCode(), "Status code should be 204");
        assertEquals(StatusCode.NO_CONTENT.message(), response.statusMessage(), "Status message should be No Content");
        assertEquals(0, response.body().length, "Body should be empty for No Content response");
        assertTrue(response.headers().containsKey("Server"), "Response should contain Server header");
        assertTrue(response.headers().containsKey("Date"), "Response should contain Date header");
    }

    @Test
    void testBadRequest() {
        HttpResponse response = HttpResponse.badRequest();
        assertEquals(StatusCode.BAD_REQUEST.code(), response.statusCode(), "Status code should be 400");
        assertEquals(StatusCode.BAD_REQUEST.message(), response.statusMessage(),
            "Status message should be Bad Request");
        assertEquals(0, response.body().length, "Body should be empty for Bad Request response");
        assertTrue(response.headers().containsKey("Server"), "Response should contain Server header");
        assertTrue(response.headers().containsKey("Date"), "Response should contain Date header");
    }

    @Test
    void testNotFound() {
        HttpResponse response = HttpResponse.notFound();
        assertEquals(StatusCode.NOT_FOUND.code(), response.statusCode(), "Status code should be 404");
        assertEquals(StatusCode.NOT_FOUND.message(), response.statusMessage(), "Status message should be Not Found");
        assertEquals(0, response.body().length, "Body should be empty for Not Found response");
        assertTrue(response.headers().containsKey("Server"), "Response should contain Server header");
        assertTrue(response.headers().containsKey("Date"), "Response should contain Date header");
    }

    @Test
    void testMethodNotAllowed() {
        HttpResponse response = HttpResponse.methodNotAllowed();
        assertEquals(StatusCode.METHOD_NOT_ALLOWED.code(), response.statusCode(), "Status code should be 405");
        assertEquals(StatusCode.METHOD_NOT_ALLOWED.message(), response.statusMessage(),
            "Status message should be Method Not Allowed");
        assertEquals(0, response.body().length, "Body should be empty for Method Not Allowed response");
        assertTrue(response.headers().containsKey("Server"), "Response should contain Server header");
        assertTrue(response.headers().containsKey("Date"), "Response should contain Date header");
    }

    @Test
    void testError() {
        String errorMessage = "error message";
        HttpResponse response = HttpResponse.error(errorMessage);
        assertEquals(StatusCode.INTERNAL_SERVER_ERROR.code(), response.statusCode(), "Status code should be 500");
        assertEquals(StatusCode.INTERNAL_SERVER_ERROR.message(), response.statusMessage(),
            "Status message should be Internal Server Error");
        assertArrayEquals(errorMessage.getBytes(StandardCharsets.UTF_8), response.body(),
            "Body should match the provided error message");
        assertEquals(ContentType.TEXT.mime(), response.headers().get(CommonHeaders.CONTENT_TYPE.value()),
            "Content-Type should be text/plain");
        assertEquals(Integer.toString(errorMessage.length()),
            response.headers().get(CommonHeaders.CONTENT_LENGTH.value()),
            "Content-Length should match the error message length");
        assertTrue(response.headers().containsKey("Server"), "Response should contain Server header");
        assertTrue(response.headers().containsKey("Date"), "Response should contain Date header");
    }

    @Test
    void testText() {
        HttpResponse initialResponse = HttpResponse.ok();
        String body = "text body";
        HttpResponse newResponse = initialResponse.text(body);

        assertNotSame(initialResponse, newResponse, "text() should return a new instance");
        assertEquals(initialResponse.statusCode(), newResponse.statusCode(), "Status code should not change");
        assertEquals(initialResponse.statusMessage(), newResponse.statusMessage(), "Status message should not change");
        assertArrayEquals(body.getBytes(StandardCharsets.UTF_8), newResponse.body(), "Body should be set correctly");
        assertEquals(ContentType.TEXT.mime(), newResponse.headers().get(CommonHeaders.CONTENT_TYPE.value()),
            "Content-Type should be set to text/plain");
        assertEquals(Integer.toString(body.length()), newResponse.headers().get(CommonHeaders.CONTENT_LENGTH.value()),
            "Content-Length should be set correctly");
    }

    @Test
    void testTextWithNullBodyThrows() {
        HttpResponse response = HttpResponse.ok();
        assertThrows(IllegalArgumentException.class, () -> response.text(null),
            "text() with null body should throw IllegalArgumentException");
    }

    @Test
    void testFile() {
        HttpResponse initialResponse = HttpResponse.ok();
        byte[] body = "file content".getBytes();
        HttpResponse newResponse = initialResponse.file(body);

        assertNotSame(initialResponse, newResponse, "file() should return a new instance");
        assertEquals(initialResponse.statusCode(), newResponse.statusCode(), "Status code should not change");
        assertEquals(initialResponse.statusMessage(), newResponse.statusMessage(), "Status message should not change");
        assertArrayEquals(body, newResponse.body(), "Body should be set correctly");
        assertEquals(ContentType.FILE.mime(), newResponse.headers().get(CommonHeaders.CONTENT_TYPE.value()),
            "Content-Type should be set to application/octet-stream");
        assertEquals(Integer.toString(body.length), newResponse.headers().get(CommonHeaders.CONTENT_LENGTH.value()),
            "Content-Length should be set correctly");
    }

    @Test
    void testFileWithNullBodyThrows() {
        HttpResponse response = HttpResponse.ok();
        assertThrows(IllegalArgumentException.class, () -> response.file(null),
            "file() with null body should throw IllegalArgumentException");
    }

    @Test
    void testJson() {
        HttpResponse initialResponse = HttpResponse.ok();
        String body = "{\"key\":\"value\"}";
        HttpResponse newResponse = initialResponse.json(body);

        assertNotSame(initialResponse, newResponse, "json() should return a new instance");
        assertEquals(initialResponse.statusCode(), newResponse.statusCode(), "Status code should not change");
        assertEquals(initialResponse.statusMessage(), newResponse.statusMessage(), "Status message should not change");
        assertArrayEquals(body.getBytes(StandardCharsets.UTF_8), newResponse.body(), "Body should be set correctly");
        assertEquals(ContentType.JSON.mime(), newResponse.headers().get(CommonHeaders.CONTENT_TYPE.value()),
            "Content-Type should be set to application/json");
        assertEquals(Integer.toString(body.length()), newResponse.headers().get(CommonHeaders.CONTENT_LENGTH.value()),
            "Content-Length should be set correctly");
    }

    @Test
    void testJsonWithNullBodyThrows() {
        HttpResponse response = HttpResponse.ok();
        assertThrows(IllegalArgumentException.class, () -> response.json(null),
            "json() with null body should throw IllegalArgumentException");
    }

    @Test
    void testStatus() {
        HttpResponse initialResponse = HttpResponse.ok();
        int newStatus = 418;
        String newMessage = "I'm a teapot";
        HttpResponse newResponse = initialResponse.status(newStatus, newMessage);

        assertNotSame(initialResponse, newResponse, "status() should return a new instance");
        assertEquals(newStatus, newResponse.statusCode(), "Status code should be updated");
        assertEquals(newMessage, newResponse.statusMessage(), "Status message should be updated");
        assertEquals(initialResponse.body(), newResponse.body(), "Body should not change");
        assertEquals(initialResponse.headers().size(), newResponse.headers().size(), "Headers should not change");
    }

    @Test
    void testStatusWithInvalidCodeThrows() {
        HttpResponse response = HttpResponse.ok();
        assertThrows(IllegalArgumentException.class, () -> response.status(0, "Invalid"),
            "status() with 0 should throw IllegalArgumentException");
        assertThrows(IllegalArgumentException.class, () -> response.status(600, "Invalid"),
            "status() with 600 should throw IllegalArgumentException");
    }

    @Test
    void testHeaders() {
        HttpResponse initialResponse = HttpResponse.ok();
        HttpResponse newResponse = initialResponse.headers(Map.entry("X-Test", "true"));

        assertNotSame(initialResponse, newResponse, "headers() should return a new instance");
        assertTrue(newResponse.headers().containsKey("X-Test"), "New header should be present");
        assertEquals("true", newResponse.headers().get("X-Test"), "New header should have the correct value");
        assertEquals(initialResponse.statusCode(), newResponse.statusCode(), "Status code should not change");
        assertEquals(initialResponse.statusMessage(), newResponse.statusMessage(), "Status message should not change");
        assertEquals(initialResponse.body(), newResponse.body(), "Body should not change");
    }

    @Test
    void testHeadersWithNullThrows() {
        HttpResponse response = HttpResponse.ok();
        assertThrows(IllegalArgumentException.class, () -> response.headers(null),
            "headers() with null should throw IllegalArgumentException");
    }
}
