package bg.sofia.uni.fmi.mjt.httpserver.routing;

import bg.sofia.uni.fmi.mjt.httpserver.entities.HttpResponse;
import bg.sofia.uni.fmi.mjt.httpserver.entities.HttpVerbs;
import bg.sofia.uni.fmi.mjt.httpserver.handlers.HttpHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrieRouterTest {

    private Router router;
    private final HttpHandler mockHandler = _ -> HttpResponse.ok();

    @BeforeEach
    void setUp() {
        router = new TrieRouter();
    }

    @Test
    void testRegisterAndResolveSimpleRoute() {
        router.registerRoute(HttpVerbs.GET, "/", mockHandler);
        Optional<RouteMatch> match = router.resolve(HttpVerbs.GET, "/");

        assertTrue(match.isPresent(), "Should find a route for GET /");
        assertNotNull(match.get().handlers().get(HttpVerbs.GET), "Handler for GET should not be null");
    }

    @Test
    void testRegisterAndResolveNestedRoute() {
        router.registerRoute(HttpVerbs.GET, "/users/profile", mockHandler);
        Optional<RouteMatch> match = router.resolve(HttpVerbs.GET, "/users/profile");

        assertTrue(match.isPresent(), "Should find a route for GET /users/profile");
        assertSame(mockHandler, match.get().handlers().get(HttpVerbs.GET), "Should return the correct handler");
    }

    @Test
    void testResolveRouteWithParameters() {
        router.registerRoute(HttpVerbs.GET, "/users/:id", mockHandler);
        Optional<RouteMatch> match = router.resolve(HttpVerbs.GET, "/users/123");

        assertTrue(match.isPresent(), "Should find a route for GET /users/123");
        assertEquals("123", match.get().pathParameters().get("id"), "Parameter 'id' should be '123'");
    }

    @Test
    void testResolveRouteWithParametersHaveCorrectNames() {
        router.registerRoute(HttpVerbs.GET, "/users/:id", mockHandler);
        Optional<RouteMatch> match = router.resolve(HttpVerbs.GET, "/users/123");

        assertTrue(match.get().pathParameters().containsKey("id"), "Parameter 'id' should be present");
    }


    @Test
    void testResolveRouteWithMultipleParameters() {
        router.registerRoute(HttpVerbs.GET, "/posts/:postId/comments/:commentId", mockHandler);
        Optional<RouteMatch> match = router.resolve(HttpVerbs.GET, "/posts/abc/comments/456");

        assertEquals("abc", match.get().pathParameters().get("postId"), "Parameter 'postId' should be 'abc'");
        assertEquals("456", match.get().pathParameters().get("commentId"), "Parameter 'commentId' should be '456'");
    }

    @Test
    void testResolveNonExistentRoute() {
        router.registerRoute(HttpVerbs.GET, "/users", mockHandler);
        Optional<RouteMatch> match = router.resolve(HttpVerbs.GET, "/nonexistent");
        assertFalse(match.isPresent(), "Should not find a route for a non-existent path");
    }

    @Test
    void testResolvePartiallyMatchingRoute() {
        router.registerRoute(HttpVerbs.GET, "/users/profile", mockHandler);
        Optional<RouteMatch> match = router.resolve(HttpVerbs.GET, "/users");
        assertFalse(match.isPresent(), "Should not find a route for a partially matching path");
    }

    @Test
    void testRegisterSamePathDifferentMethods() {
        HttpHandler postHandler = request -> HttpResponse.noContent();
        router.registerRoute(HttpVerbs.GET, "/resource", mockHandler);
        router.registerRoute(HttpVerbs.POST, "/resource", postHandler);

        Optional<RouteMatch> getMatch = router.resolve(HttpVerbs.GET, "/resource");
        assertTrue(getMatch.isPresent(), "Should find route for GET");
        assertEquals(mockHandler, getMatch.get().handlers().get(HttpVerbs.GET), "Should return the GET handler");

        Optional<RouteMatch> postMatch = router.resolve(HttpVerbs.POST, "/resource");
        assertTrue(postMatch.isPresent(), "Should find route for POST");
        assertEquals(postHandler, postMatch.get().handlers().get(HttpVerbs.POST), "Should return the POST handler");
    }

    @Test
    void testStaticPathShouldHavePrecedence() {
        HttpHandler specificHandler = request -> HttpResponse.ok("new");
        router.registerRoute(HttpVerbs.GET, "/users/:id", mockHandler);
        router.registerRoute(HttpVerbs.GET, "/users/new", specificHandler);

        Optional<RouteMatch> match = router.resolve(HttpVerbs.GET, "/users/new");

        assertTrue(match.isPresent(), "Should resolve the static path /users/new");
        assertSame(specificHandler, match.get().handlers().get(HttpVerbs.GET),
            "Should prioritize the static route handler");
        assertTrue(match.get().pathParameters().isEmpty(), "Should not have any parameters for a static route");
    }

    @Test
    void testRegisterDuplicateRouteThrows() {
        router.registerRoute(HttpVerbs.GET, "/duplicate", mockHandler);
        assertThrows(IllegalStateException.class, () -> router.registerRoute(HttpVerbs.GET, "/duplicate", mockHandler),
            "Registering a duplicate route should throw IllegalStateException");
    }

    @Test
    void testRegisterWithNullMethodThrows() {
        assertThrows(IllegalArgumentException.class, () -> router.registerRoute(null, "/path", mockHandler),
            "Registering with a null method should throw IllegalArgumentException");
    }

    @Test
    void testRegisterWithNullPathThrows() {
        assertThrows(IllegalArgumentException.class, () -> router.registerRoute(HttpVerbs.GET, null, mockHandler),
            "Registering with a null path should throw IllegalArgumentException");
    }

    @Test
    void testRegisterWithNullHandlerThrows() {
        assertThrows(IllegalArgumentException.class, () -> router.registerRoute(HttpVerbs.GET, "/path", null),
            "Registering with a null handler should throw IllegalArgumentException");
    }

    @Test
    void testResolveWithNullMethodThrows() {
        assertThrows(IllegalArgumentException.class, () -> router.resolve(null, "/path"),
            "Resolving with a null method should throw IllegalArgumentException");
    }

    @Test
    void testResolveWithNullPathThrows() {
        assertThrows(IllegalArgumentException.class, () -> router.resolve(HttpVerbs.GET, null),
            "Resolving with a null path should throw IllegalArgumentException");
    }

    @Test
    void testResolveWithBlankPathThrows() {
        assertThrows(IllegalArgumentException.class, () -> router.resolve(HttpVerbs.GET, "  "),
            "Resolving with a blank path should throw IllegalArgumentException");
    }

    @Test
    void testResolvePathIgnoresTrailingSlash() {
        router.registerRoute(HttpVerbs.GET, "/users", mockHandler);
        Optional<RouteMatch> match = router.resolve(HttpVerbs.GET, "/users/");

        assertTrue(match.isPresent(), "Should resolve path with a trailing slash");
        assertSame(mockHandler, match.get().handlers().get(HttpVerbs.GET), "Should return the correct handler");
    }

    @Test
    void testResolvePathWithoutLeadingSlash() {
        router.registerRoute(HttpVerbs.GET, "users", mockHandler);
        Optional<RouteMatch> match = router.resolve(HttpVerbs.GET, "/users");

        assertTrue(match.isPresent(), "Should resolve path even if registered without a leading slash");
    }
}
