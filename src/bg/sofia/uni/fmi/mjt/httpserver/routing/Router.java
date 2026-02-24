package bg.sofia.uni.fmi.mjt.httpserver.routing;

import bg.sofia.uni.fmi.mjt.httpserver.entities.HttpVerbs;
import bg.sofia.uni.fmi.mjt.httpserver.handlers.HttpHandler;

import java.util.Optional;

/**
 * The primary Router interface.
 */
public interface Router {

    /**
     * Registers a route with a specific HTTP method and path pattern.
     */
    void registerRoute(HttpVerbs method, String pathPattern, HttpHandler handler);

    /**
     * Attempts to find a matching handler for an incoming request.
     */
    Optional<RouteMatch> resolve(HttpVerbs method, String path);

    default void get(String path, HttpHandler handler) {
        registerRoute(HttpVerbs.GET, path, handler);
    }

    default void post(String path, HttpHandler handler) {
        registerRoute(HttpVerbs.POST, path, handler);
    }

    default void put(String path, HttpHandler handler) {
        registerRoute(HttpVerbs.PUT, path, handler);
    }

    default void delete(String path, HttpHandler handler) {
        registerRoute(HttpVerbs.DELETE, path, handler);
    }
}
