package bg.sofia.uni.fmi.mjt.httpserver.handlers;

import bg.sofia.uni.fmi.mjt.httpserver.entities.HttpRequest;
import bg.sofia.uni.fmi.mjt.httpserver.entities.HttpResponse;

@FunctionalInterface
public interface HttpHandler {
    HttpResponse handle(HttpRequest request);
}
