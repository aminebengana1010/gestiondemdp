package ma.province.safi.passwordmanager.util;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class StaticFileHandler implements HttpHandler {

    private final String baseDirectory;
    private static final Map<String, String> MIME_TYPES = Map.ofEntries(
        Map.entry("html", "text/html"),
        Map.entry("css", "text/css"),
        Map.entry("js", "application/javascript"),
        Map.entry("json", "application/json"),
        Map.entry("png", "image/png"),
        Map.entry("jpg", "image/jpeg"),
        Map.entry("jpeg", "image/jpeg"),
        Map.entry("gif", "image/gif"),
        Map.entry("svg", "image/svg+xml"),
        Map.entry("ico", "image/x-icon"),
        Map.entry("woff", "font/woff"),
        Map.entry("woff2", "font/woff2")
    );

    public StaticFileHandler(String baseDirectory) {
        this.baseDirectory = baseDirectory;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String path = exchange.getRequestURI().getPath();

            // Redirection de la racine vers index.html
            if (path.equals("/")) {
                path = "/index.html";
            }

            // Déterminer le chemin du fichier
            String filePath = baseDirectory + path;

            // Sécurité: éviter le directory traversal
            Path normalizedPath = Paths.get(filePath).normalize();
            Path basePath = Paths.get(baseDirectory).normalize();
            if (!normalizedPath.startsWith(basePath)) {
                ResponseUtil.json(exchange, 403, "{\"erreur\":\"Accès interdit\"}");
                return;
            }

            File file = normalizedPath.toFile();
            if (!file.exists() || file.isDirectory()) {
                // Si c'est l'API qui n'a pas matché de route, renvoyer 404
                if (path.startsWith("/api/")) {
                    ResponseUtil.json(exchange, 404, "{\"erreur\":\"API non trouvée\"}");
                } else {
                    // Servir index.html pour le SPA-like routing
                    File indexFile = Paths.get(baseDirectory, "index.html").toFile();
                    if (indexFile.exists()) {
                        byte[] bytes = Files.readAllBytes(indexFile.toPath());
                        ResponseUtil.envoyerFichierStatique(exchange, bytes, "text/html");
                    } else {
                        ResponseUtil.json(exchange, 404, "{\"erreur\":\"Fichier introuvable\"}");
                    }
                }
                return;
            }

            // Déterminer le MIME type
            String fileName = file.getName();
            String extension = fileName.contains(".") ? fileName.substring(fileName.lastIndexOf('.') + 1) : "";
            String mimeType = MIME_TYPES.getOrDefault(extension.toLowerCase(), "application/octet-stream");

            byte[] bytes = Files.readAllBytes(file.toPath());
            ResponseUtil.envoyerFichierStatique(exchange, bytes, mimeType);

        } catch (Exception e) {
            e.printStackTrace();
            ResponseUtil.json(exchange, 500, "{\"erreur\":\"Erreur serveur\"}");
        }
    }
}
