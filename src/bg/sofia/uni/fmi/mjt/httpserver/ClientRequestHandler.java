package bg.sofia.uni.fmi.mjt.httpserver;

import bg.sofia.uni.fmi.mjt.httpserver.entities.CommonHeaders;
import bg.sofia.uni.fmi.mjt.httpserver.entities.ContentType;
import bg.sofia.uni.fmi.mjt.httpserver.entities.HttpRequest;
import bg.sofia.uni.fmi.mjt.httpserver.entities.HttpResponse;
import bg.sofia.uni.fmi.mjt.httpserver.entities.HttpVerbs;
import bg.sofia.uni.fmi.mjt.httpserver.routing.RouteMatch;
import bg.sofia.uni.fmi.mjt.httpserver.routing.Router;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static bg.sofia.uni.fmi.mjt.httpserver.logging.Logger.log;

class ClientRequestHandler implements Runnable {
    private final Socket socket;
    private final Router router;

    public ClientRequestHandler(Socket socket, Router router) {
        if (socket == null) {
            throw new IllegalArgumentException("Socket cannot be null");
        }

        if (router == null) {
            throw new IllegalArgumentException("Router cannot be null");
        }

        this.socket = socket;
        this.router = router;
    }

    @Override
    public void run() {
        Thread.currentThread().setName("VT-HTTP-client-" + socket.getRemoteSocketAddress());

        try (OutputStream out = socket.getOutputStream();
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             socket) {

            String requestLine = in.readLine();
            if (requestLine == null) return;

            String[] tokens = requestLine.split(" ");
            if (tokens.length < 3) {
                log(socket, "Invalid request line: " + requestLine);
                writeResponse(out, HttpResponse.badRequest());
                return;
            }

            HttpRequest request = buildRawRequest(in, tokens, socket);
            final HttpVerbs verb = HttpVerbs.valueOf(request.method());
            if (request.headers().containsKey(CommonHeaders.CONTENT_LENGTH.value()) &&
                request.headers().get(CommonHeaders.CONTENT_TYPE.value()).equals(
                    ContentType.JSON.mime()) && !isValidJson(request.body())) {
                log(socket, "Incorrect JSON syntax");
                writeResponse(out, HttpResponse.badRequest());
                return;
            }

            final Optional<RouteMatch> routeMatch = router.resolve(verb, request.uri());
            if (routeMatch.isEmpty()) {
                log(socket, "No handler found for request");
                writeResponse(out, HttpResponse.notFound());
                return;
            }
            request.params().putAll(routeMatch.get().pathParameters());

            if (!routeMatch.get().handlers().containsKey(verb)) {
                log(socket, "Unsupported operation for this endpoint: " + verb + " " + request.uri());
                writeResponse(out, HttpResponse.methodNotAllowed());
                return;
            }

            try {
                HttpResponse response = routeMatch.get().handlers().get(verb).handle(request);
                log(socket, requestLine);
                writeResponse(out, response);
            } catch (RuntimeException e) {
                log(socket, "Error handling request: " + e.getMessage());
                e.printStackTrace();
                writeResponse(out, HttpResponse.error(e.getMessage()));
            }
        } catch (IOException e) {
            System.err.println("Error handling client: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void writeResponse(OutputStream out, HttpResponse response) throws IOException {
        final String CRLF = "\r\n";
        final String supportedHTTPVersion = "HTTP/1.0 ";
        final StringBuilder sb = new StringBuilder();

        sb.append(supportedHTTPVersion).append(response.statusCode()).append(' ').append(response.statusMessage())
            .append(CRLF);
        out.write(sb.toString().getBytes());
        sb.setLength(0);

        for (Map.Entry<String, String> header : response.headers().entrySet()) {
            sb.append(header.getKey()).append(": ").append(header.getValue()).append(CRLF);
            out.write(sb.toString().getBytes());
            sb.setLength(0);
        }
        out.write(CRLF.getBytes());

        if (response.headers().containsKey(CommonHeaders.CONTENT_LENGTH.value())) {
            out.write(response.body());
        }
    }

    private static HttpRequest buildRawRequest(BufferedReader in, String[] tokens, Socket socket)
        throws IOException {
        final String headerSeparator = ": ";
        final int headerTokenLimit = 2;

        String method = tokens[0];
        String uri = tokens[1];
        String version = tokens[2];

        Map<String, String> headers = new HashMap<>();
        String line;
        while ((line = in.readLine()) != null && !line.isEmpty()) {
            String[] headerParts = line.split(headerSeparator, headerTokenLimit);
            if (headerParts.length == headerTokenLimit) {
                headers.put(headerParts[0].toLowerCase(), headerParts[1]);
            }
        }

        String body = null;
        if (headers.containsKey(CommonHeaders.CONTENT_LENGTH.value())) {
            int contentLength = Integer.parseInt(headers.get(CommonHeaders.CONTENT_LENGTH.value()));
            body = parseRequestBody(in, contentLength);
        }

        String remoteAddress = socket.getInetAddress().getHostAddress();
        return new HttpRequest(remoteAddress, socket.getPort(), LocalDateTime.now(), method, uri, version, headers,
            body);
    }

    private static String parseRequestBody(Reader in, int contentLength) throws IOException {
        char[] buffer = new char[contentLength];
        int totalRead = 0;
        while (totalRead < contentLength) {
            int read = in.read(buffer, totalRead, contentLength - totalRead);
            if (read == -1) break;
            totalRead += read;
        }

        return String.valueOf(buffer);
    }


    private static boolean isValidJson(String json) {
        try {
            JsonParser.parseString(json);
        } catch (JsonSyntaxException e) {
            return false;
        }
        return true;
    }
}
