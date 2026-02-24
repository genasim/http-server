package bg.sofia.uni.fmi.mjt.httpserver;

import bg.sofia.uni.fmi.mjt.httpserver.entities.HttpResponse;
import bg.sofia.uni.fmi.mjt.httpserver.entities.HttpVerbs;
import bg.sofia.uni.fmi.mjt.httpserver.routing.RouteMatch;
import bg.sofia.uni.fmi.mjt.httpserver.routing.Router;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClientRequestHandlerTest {
    @Mock
    private Socket socket;

    @Mock
    private Router router;

    @InjectMocks
    private ClientRequestHandler handler;

    @Test
    void testSuccessfulRequest() throws IOException {
        String request = "GET /test HTTP/1.1\r\n\r\n";
        String expectedResponse = "HTTP/1.1 200 OK\r\n";
        mockSocket(request);
        when(router.resolve(HttpVerbs.GET, "/test")).thenReturn(Optional.of(
            new RouteMatch(Map.of(HttpVerbs.GET, _ -> HttpResponse.ok()), Collections.emptyMap())
        ));

        handler.run();

        assertEquals(expectedResponse, getSocketOutput().split("\r\n")[0] + "\r\n",
            "Should return 200 OK for a successful request");
    }

    @Test
    void testInvalidRequestLine() throws IOException {
        String request = "GET /test\r\n\r\n";
        String expectedResponse = "HTTP/1.1 400 Bad Request\r\n";

        when(socket.getInputStream()).thenReturn(new ByteArrayInputStream(request.getBytes()));
        when(socket.getOutputStream()).thenReturn(new ByteArrayOutputStream());

        handler.run();

        assertEquals(expectedResponse, getSocketOutput().split("\r\n")[0] + "\r\n",
            "Should return 400 Bad Request for an invalid request line");
    }

    @Test
    void testRouteNotFound() throws IOException {
        String request = "GET /nonexistent HTTP/1.1\r\n\r\n";
        String expectedResponse = "HTTP/1.1 404 Not Found\r\n";

        mockSocket(request);
        when(router.resolve(HttpVerbs.GET, "/nonexistent")).thenReturn(Optional.empty());

        handler.run();

        assertEquals(expectedResponse, getSocketOutput().split("\r\n")[0] + "\r\n",
            "Should return 404 Not Found for a nonexistent route");
    }

    @Test
    void testMethodNotAllowed() throws IOException {
        String request = "POST /test HTTP/1.1\r\n\r\n";
        String expectedResponse = "HTTP/1.1 405 Method Not Allowed\r\n";

        mockSocket(request);
        when(router.resolve(HttpVerbs.POST, "/test")).thenReturn(Optional.of(
            new RouteMatch(Map.of(HttpVerbs.GET, _ -> HttpResponse.ok()), Collections.emptyMap())
        ));

        handler.run();

        assertEquals(expectedResponse, getSocketOutput().split("\r\n")[0] + "\r\n",
            "Should return 405 Method Not Allowed for an unsupported method");
    }

    @Test
    void testValidJsonBody() throws IOException {
        String request =
            "POST /test HTTP/1.1\r\nContent-Type: application/json\r\nContent-Length: 10\r\n\r\n{\"foo\":5}";
        String expectedResponse = "HTTP/1.1 200 OK\r\n";

        mockSocket(request);
        when(router.resolve(HttpVerbs.POST, "/test")).thenReturn(Optional.of(
            new RouteMatch(Map.of(HttpVerbs.POST, _ -> HttpResponse.ok()), Collections.emptyMap())
        ));

        handler.run();

        assertEquals(expectedResponse, getSocketOutput().split("\r\n")[0] + "\r\n",
            "Should parse the JSON body without errors");
    }

    @Test
    void testInvalidJsonBodyReturnsBadRequest() throws IOException {
        String request = "POST /test HTTP/1.1\r\nContent-Type: application/json\r\nContent-Length: 10\r\n\r\n{invalid}";
        String expectedResponse = "HTTP/1.1 400 Bad Request\r\n";
        mockSocket(request);

        handler.run();

        assertEquals(expectedResponse, getSocketOutput().split("\r\n")[0] + "\r\n",
            "Should return 400 Bad Request for an invalid JSON body");
    }

    @Test
    void testHandlerUnhandledExceptionReturnsInternalServerError() throws IOException {
        String request = "GET /error HTTP/1.1\r\n\r\n";
        String expectedResponse = "HTTP/1.1 500 Internal Server Error\r\n";

        mockSocket(request);
        when(router.resolve(HttpVerbs.GET, "/error")).thenReturn(Optional.of(
            new RouteMatch(Map.of(HttpVerbs.GET, _ -> {
                throw new RuntimeException("Test error");
            }), Collections.emptyMap())
        ));

        handler.run();

        assertEquals(expectedResponse, getSocketOutput().split("\r\n")[0] + "\r\n",
            "Should return 500 Internal Server Error when a handler throws an exception");
    }

    @Test
    void testNullSocketThrows() {
        assertThrows(IllegalArgumentException.class, () -> new ClientRequestHandler(null, router),
            "Constructor should throw for null socket");
    }

    @Test
    void testNullRouterThrows() {
        assertThrows(IllegalArgumentException.class, () -> new ClientRequestHandler(socket, null),
            "Constructor should throw for null router");
    }

    private void mockSocket(String request) throws IOException {
        when(socket.getInputStream()).thenReturn(new ByteArrayInputStream(request.getBytes()));
        when(socket.getOutputStream()).thenReturn(new ByteArrayOutputStream());
        when(socket.getInetAddress()).thenReturn(InetAddress.getLocalHost());
    }

    private String getSocketOutput() throws IOException {
        return ((ByteArrayOutputStream) socket.getOutputStream()).toString();
    }
}
