package bg.sofia.uni.fmi.mjt.httpserver.logging;

import java.net.Socket;
import java.time.LocalDateTime;

public abstract class Logger {
    public static void log(Socket socket, String message) {
        String now = LocalDateTime.now().toString();
        System.out.println("[" + now + "] [ " + socket.getRemoteSocketAddress() + " ] " + message);
    }
}
