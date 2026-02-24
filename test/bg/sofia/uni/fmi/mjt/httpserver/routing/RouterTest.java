package bg.sofia.uni.fmi.mjt.httpserver.routing;

import bg.sofia.uni.fmi.mjt.httpserver.entities.HttpResponse;
import bg.sofia.uni.fmi.mjt.httpserver.entities.HttpVerbs;
import bg.sofia.uni.fmi.mjt.httpserver.handlers.HttpHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RouterTest {
    private final HttpHandler mockHandler = _ -> HttpResponse.ok();

    @Mock
    private Router router;

    @Test
    void testGetDefaultMethod() {
        doCallRealMethod().when(router).get("/test", mockHandler);
        router.get("/test", mockHandler);
        verify(router).registerRoute(HttpVerbs.GET, "/test", mockHandler);
    }

    @Test
    void testPostDefaultMethod() {
        doCallRealMethod().when(router).post("/test", mockHandler);
        router.post("/test", mockHandler);
        verify(router).registerRoute(HttpVerbs.POST, "/test", mockHandler);
    }

    @Test
    void testPutDefaultMethod() {
        doCallRealMethod().when(router).put("/test", mockHandler);
        router.put("/test", mockHandler);
        verify(router).registerRoute(HttpVerbs.PUT, "/test", mockHandler);
    }

    @Test
    void testDeleteDefaultMethod() {
        doCallRealMethod().when(router).delete("/test", mockHandler);
        router.delete("/test", mockHandler);
        verify(router).registerRoute(HttpVerbs.DELETE, "/test", mockHandler);
    }
}
