package bg.sofia.uni.fmi.mjt.httpserver.routing;

import bg.sofia.uni.fmi.mjt.httpserver.entities.HttpVerbs;
import bg.sofia.uni.fmi.mjt.httpserver.handlers.HttpHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class TrieRouter implements Router {
    private final RouteNode root = new RouteNode("/", new ArrayList<>());

    @Override
    public void registerRoute(HttpVerbs method, String pathPattern, HttpHandler handler) {
        validateInput(method, pathPattern, handler);
        String[] segments = parsePath(pathPattern);
        registerRecursive(root, segments, 0, handler, method);
    }

    @Override
    public Optional<RouteMatch> resolve(HttpVerbs method, String path) {
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("Cannot resolve route handler based on null or blank path");
        }

        if (method == null) {
            throw new IllegalArgumentException("Cannot resolve route handler based on null HTTP method");
        }

        String[] segments = parsePath(path);
        Map<String, String> params = new HashMap<>();
        return resolveRecursive(root, segments, 0, params);
    }

    private void registerRecursive(RouteNode currentNode, String[] segments, int index, HttpHandler handler,
                                   HttpVerbs method) {
        if (index == segments.length) {
            if (currentNode.handlers.containsKey(method)) {
                throw new IllegalStateException("Cannot multiple endpoint handlers for the same path");
            }
            currentNode.handlers.put(method, handler);
            return;
        }

        String segment = segments[index];
        RouteNode child = findChild(currentNode, segment);

        if (child == null) {
            child = new RouteNode(segment, new ArrayList<>());
            currentNode.children.add(child);
        }

        registerRecursive(child, segments, index + 1, handler, method);
    }

    private Optional<RouteMatch> resolveRecursive(RouteNode currentNode, String[] segments, int index,
                                                  Map<String, String> params) {
        if (index == segments.length) {
            if (!currentNode.handlers.isEmpty()) {
                return Optional.of(new RouteMatch(currentNode.handlers, params));
            }
            return Optional.empty();
        }

        String segment = segments[index];

        for (RouteNode child : currentNode.children) {
            if (child.path.equals(segment)) {
                return resolveRecursive(child, segments, index + 1, params);
            }
        }

        for (RouteNode child : currentNode.children) {
            if (child.path.startsWith(":")) {
                String paramName = child.path.substring(1);
                params.put(paramName, segment);
                Optional<RouteMatch> match = resolveRecursive(child, segments, index + 1, params);
                if (match.isPresent()) {
                    return match;
                }
                params.remove(paramName);
            }
        }

        return Optional.empty();
    }

    private RouteNode findChild(RouteNode parent, String segment) {
        for (RouteNode child : parent.children) {
            if (child.path.equals(segment)) {
                return child;
            }
        }
        return null;
    }

    private String[] parsePath(String path) {
        return Arrays.stream(path.split("/"))
            .filter(s -> !s.isEmpty())
            .toArray(String[]::new);
    }

    private void validateInput(HttpVerbs method, String path, HttpHandler handler) {
        if (method == null) throw new IllegalArgumentException("Method cannot be null");

        if (path == null) throw new IllegalArgumentException("Path cannot be null");
        if (!path.startsWith("/")) path = "/" + path;

        if (handler == null) throw new IllegalArgumentException("Handler cannot be null");
    }

    private static class RouteNode {
        Map<HttpVerbs, HttpHandler> handlers = new EnumMap<>(HttpVerbs.class);
        String path;
        List<RouteNode> children;

        public RouteNode(String path, List<RouteNode> children) {
            this.path = path;
            this.children = children;
        }
    }
}
