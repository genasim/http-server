package bg.sofia.uni.fmi.mjt.httpserver;

import bg.sofia.uni.fmi.mjt.httpserver.entities.HttpResponse;
import bg.sofia.uni.fmi.mjt.httpserver.handlers.HttpHandler;
import bg.sofia.uni.fmi.mjt.httpserver.routing.Router;
import bg.sofia.uni.fmi.mjt.httpserver.routing.TrieRouter;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

public class HttpServer {
    private final int port;
    private final Router router;

    public HttpServer(int port, Router router) {
        if (port <= 1024 || port >= 65536) {
            throw new IllegalArgumentException(
                "Port must be greater than 1024 (outside the well-known range) and less than 65536");
        }

        if (router == null) {
            throw new IllegalArgumentException("Router cannot be null");
        }

        this.router = router;
        this.port = port;
    }

    public void get(String path, HttpHandler handler) {
        router.get(path, handler);
    }

    public void post(String path, HttpHandler handler) {
        router.post(path, handler);
    }

    public void delete(String path, HttpHandler handler) {
        router.delete(path, handler);
    }

    public void put(String path, HttpHandler handler) {
        router.put(path, handler);
    }

    public void start() {
        int logicalCores = Runtime.getRuntime().availableProcessors();
        try (var requestExecutor = Executors.newWorkStealingPool(logicalCores - 1);
             var ioExecutor = Executors.newVirtualThreadPerTaskExecutor();
             ServerSocket serverSocket = new ServerSocket(port)) {
            InetAddress serverAddress = InetAddress.getLocalHost();
            IO.println(String.format("""
                ===============================================
                |   Server listening on %s:%d
                ===============================================
                """, serverAddress.getHostAddress(), port));

            while (true) {
                final Socket clientSocket = serverSocket.accept();
                IO.println("Accepted connection request from client " + clientSocket.getInetAddress() + ":" +
                    clientSocket.getPort());

                CompletableFuture.supplyAsync(() -> new ClientRequestHandler(clientSocket, router), requestExecutor)
                    .thenApplyAsync(req -> {
                        ioExecutor.execute(req);
                        return null;
                    }, ioExecutor);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Example how the server might be used
    static void main() {
        var router = new TrieRouter();
        router.get("/", (res) -> {
            // The endpoint handler runs in a separate virtual thread
            try {
                Thread.sleep(5000);
            } catch (Exception _) {
            }
            return HttpResponse.ok("Message received");
        });
        HttpServer server = new HttpServer(5050, router);

        server.get("/test", _ -> HttpResponse.ok("Hello World!"));

        server.start();
    }
}
