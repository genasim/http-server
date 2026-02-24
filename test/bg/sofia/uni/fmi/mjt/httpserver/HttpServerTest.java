package bg.sofia.uni.fmi.mjt.httpserver;

import bg.sofia.uni.fmi.mjt.httpserver.handlers.HttpHandler;
import bg.sofia.uni.fmi.mjt.httpserver.routing.Router;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class HttpServerTest {
    private static final int VALID_PORT = 8080;
    private HttpServer server;

    @Mock
    private Router router;

    @BeforeEach
    void setUp() {
        server = new HttpServer(VALID_PORT, router);
    }

    @Test
    void testConstructorWithValidArguments() {
        assertDoesNotThrow(() -> new HttpServer(VALID_PORT, router),
            "Constructor should not throw with valid arguments");
    }

    @Test
    void testConstructorWellKnownPortThrows() {
        assertThrows(IllegalArgumentException.class, () -> new HttpServer(1024, router),
            "Constructor should throw for port 1024");
    }

    @Test
    void testConstructorOverflowPortThrows() {
        assertThrows(IllegalArgumentException.class, () -> new HttpServer(65536, router),
            "Constructor should throw for port 65536");
    }

    @Test
    void testConstructorWithNullRouterThrows() {
        assertThrows(IllegalArgumentException.class, () -> new HttpServer(VALID_PORT, null),
            "Constructor should throw for null router");
    }

    @Test
    void testGetDelegatesToRouter() {
        HttpHandler handler = mock(HttpHandler.class);
        server.get("/test", handler);
        verify(router).get("/test", handler);
    }

    @Test
    void testPostDelegatesToRouter() {
        HttpHandler handler = mock(HttpHandler.class);
        server.post("/test", handler);
        verify(router).post("/test", handler);
    }

    @Test
    void testPutDelegatesToRouter() {
        HttpHandler handler = mock(HttpHandler.class);
        server.put("/test", handler);
        verify(router).put("/test", handler);
    }

    @Test
    void testDeleteDelegatesToRouter() {
        HttpHandler handler = mock(HttpHandler.class);
        server.delete("/test", handler);
        verify(router).delete("/test", handler);
    }
}
