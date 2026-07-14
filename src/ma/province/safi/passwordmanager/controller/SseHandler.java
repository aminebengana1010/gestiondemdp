package ma.province.safi.passwordmanager.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ma.province.safi.passwordmanager.service.NotificationBroadcaster;
import ma.province.safi.passwordmanager.service.NotificationBroadcaster.SseClient;

import java.io.IOException;

public class SseHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");

            SseClient client = NotificationBroadcaster.getInstance().ajouterClient();
            client.attacher(exchange);

            // Boucle pour garder la connexion ouverte,
            // le client SSE se déconnecte quand le navigateur ferme
            while (!client.isClosed()) {
                try {
                    Thread.sleep(30000);
                    // Envoi keep-alive
                    client.envoyer(": keepalive\n\n");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        } catch (Exception e) {
            // Connexion fermée par le client — normal
        }
    }
}
