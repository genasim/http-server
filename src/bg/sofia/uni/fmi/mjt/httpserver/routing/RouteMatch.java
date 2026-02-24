package bg.sofia.uni.fmi.mjt.httpserver.routing;

import bg.sofia.uni.fmi.mjt.httpserver.entities.HttpVerbs;
import bg.sofia.uni.fmi.mjt.httpserver.handlers.HttpHandler;

import java.util.Map;

/**
 * The result of a successful routing match.
 * Contains the handler and the parameters extracted from the path.
 */
public record RouteMatch(Map<HttpVerbs, HttpHandler> handlers, Map<String, String> pathParameters) {
}
