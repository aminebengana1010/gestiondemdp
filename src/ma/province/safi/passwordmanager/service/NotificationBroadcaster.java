package ma.province.safi.passwordmanager.service;

import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class NotificationBroadcaster {

    private static final NotificationBroadcaster INSTANCE = new NotificationBroadcaster();
    private final List<SseClient> clients = new CopyOnWriteArrayList<>();

    private NotificationBroadcaster() {}

    public static NotificationBroadcaster getInstance() {
        return INSTANCE;
    }

    public SseClient ajouterClient() {
        SseClient client = new SseClient();
        clients.add(client);
        return client;
    }

    public void retirerClient(SseClient client) {
        clients.remove(client);
    }

    public void diffuser(String event, String data) {
        String message = formatSSE(event, data);
        for (SseClient client : clients) {
            client.envoyer(message);
        }
    }

    public void diffuserNotification(String message, String type) {
        String escaped = message.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
        diffuser("notification", "{\"message\":\"" + escaped + "\",\"type\":\"" + type + "\"}");
    }

    private String formatSSE(String event, String data) {
        return "event: " + event + "\ndata: " + data + "\n\n";
    }

    public static class SseClient {
        private HttpExchange exchange;
        private volatile boolean closed = false;

        public void attacher(HttpExchange exchange) {
            this.exchange = exchange;
            exchange.getResponseHeaders().add("Content-Type", "text/event-stream; charset=UTF-8");
            exchange.getResponseHeaders().add("Cache-Control", "no-cache");
            exchange.getResponseHeaders().add("Connection", "keep-alive");
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");

            try {
                exchange.sendResponseHeaders(200, 0);
                // Envoyer un premier événement pour confirmer la connexion
                OutputStream os = exchange.getResponseBody();
                os.write("event: connected\ndata: {}\n\n".getBytes(StandardCharsets.UTF_8));
                os.flush();
            } catch (IOException e) {
                closed = true;
            }
        }

        public void envoyer(String message) {
            if (closed) return;
            try {
                OutputStream os = exchange.getResponseBody();
                synchronized (this) {
                    os.write(message.getBytes(StandardCharsets.UTF_8));
                    os.flush();
                }
            } catch (IOException e) {
                closed = true;
            }
        }

        public boolean isClosed() { return closed; }
    }
}
