package ma.province.safi.passwordmanager;

import com.sun.net.httpserver.HttpServer;
import ma.province.safi.passwordmanager.config.DatabaseConnection;
import ma.province.safi.passwordmanager.controller.*;
import ma.province.safi.passwordmanager.dao.*;
import ma.province.safi.passwordmanager.security.CryptoService;
import ma.province.safi.passwordmanager.security.SecurityInterceptor;
import ma.province.safi.passwordmanager.service.*;
import ma.province.safi.passwordmanager.session.SessionManager;
import ma.province.safi.passwordmanager.util.StaticFileHandler;

import javax.crypto.SecretKey;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class Main {

    public static void main(String[] args) throws Exception {

        System.out.println();
        System.out.println("╔══════════════════════════════════════════════════╗");
        System.out.println("║  Gestion des Mots de Passe - Province de Safi  ║");
        System.out.println("╠══════════════════════════════════════════════════╣");
        System.out.println("║  Serveur démarré: http://localhost:8080         ║");
        System.out.println("║  Login admin:       admin / admin123            ║");
        System.out.println("╚══════════════════════════════════════════════════╝");

        // === 1. Vérification connexion BD ===
        System.out.println("[INFO] Test de connexion à SQL Server...");
        if (!DatabaseConnection.testConnection()) {
            System.err.println("[ERREUR] Impossible de se connecter à SQL Server. Vérifiez que le serveur est démarré.");
            System.err.println("[ERREUR] URL: jdbc:sqlserver://localhost:1433;databaseName=GestionMotsDePasse_safi");
            System.exit(1);
        }
        System.out.println("[INFO] Connexion SQL Server réussie.");

        // === 2. Injection de dépendances ===
        // DAO
        UtilisateurDAO utilisateurDAO = new UtilisateurDAO();
        ServeurDAO serveurDAO = new ServeurDAO();
        SwitchDAO switchDAO = new SwitchDAO();
        SystemeInterneDAO systemeInterneDAO = new SystemeInterneDAO();
        SystemeExterneDAO systemeExterneDAO = new SystemeExterneDAO();
        NotificationDAO notificationDAO = new NotificationDAO();
        AuditDAO auditDAO = new AuditDAO();
        DivisionDAO divisionDAO = new DivisionDAO();

        // Sécurité
        SessionManager sessionManager = new SessionManager();
        SecurityInterceptor security = new SecurityInterceptor(sessionManager);

        // Générer ou charger la clé AES (en production: depuis variable d'environnement)
        SecretKey aesKey = CryptoService.genererCle();
        CryptoService cryptoService = new CryptoService(aesKey);

        // Services
        AuthService authService = new AuthService(utilisateurDAO, auditDAO, sessionManager);
        NotificationService notificationService = new NotificationService(notificationDAO);

        ServeurService serveurService = new ServeurService(serveurDAO, auditDAO, notificationDAO, cryptoService);
        SwitchService switchService = new SwitchService(switchDAO, auditDAO, notificationDAO, cryptoService);
        SystemeInterneService systemeInterneService = new SystemeInterneService(systemeInterneDAO, auditDAO, notificationDAO, cryptoService);
        SystemeExterneService systemeExterneService = new SystemeExterneService(systemeExterneDAO, auditDAO, notificationDAO, cryptoService);

        ExportService exportService = new ExportService(serveurService, switchService, systemeInterneService, systemeExterneService, auditDAO);
        RechercheService rechercheService = new RechercheService(serveurDAO, switchDAO, systemeInterneDAO, systemeExterneDAO);

        // === 3. Créer l'utilisateur admin par défaut si vide ===
        authService.creerPremierAdministrateur();

        // === 4. Démarrer le serveur HTTP ===
        HttpServer server;
        try {
            server = HttpServer.create(new InetSocketAddress(8080), 0);
        } catch (java.net.BindException e) {
            return;
        }

        // Fichiers statiques (servis depuis le répertoire web/)
        StaticFileHandler staticHandler = new StaticFileHandler("web");
        server.createContext("/", staticHandler);
        server.createContext("/index.html", staticHandler);
        server.createContext("/login.html", staticHandler);
        server.createContext("/serveurs.html", staticHandler);
        server.createContext("/switches.html", staticHandler);
        server.createContext("/systemes-internes.html", staticHandler);
        server.createContext("/systemes-externes.html", staticHandler);
        server.createContext("/divisions.html", staticHandler);
        server.createContext("/notifications.html", staticHandler);
        server.createContext("/audit.html", staticHandler);
        server.createContext("/css", staticHandler);
        server.createContext("/js", staticHandler);
        server.createContext("/img", staticHandler);

        // API REST
        server.createContext("/api/login", new LoginHandler(authService));
        server.createContext("/api/logout", new LogoutHandler(authService));
        server.createContext("/api/session", new SessionHandler(security));
        server.createContext("/api/serveurs", new ServeurHandler(serveurService, security, exportService));
        server.createContext("/api/switches", new SwitchHandler(switchService, security, exportService));
        server.createContext("/api/systemes-internes", new SystemeInterneHandler(systemeInterneService, security, exportService));
        server.createContext("/api/systemes-externes", new SystemeExterneHandler(systemeExterneService, security, exportService));
        server.createContext("/api/divisions", new DivisionHandler(divisionDAO, security));
        server.createContext("/api/notifications", new NotificationHandler(notificationService, security));
        server.createContext("/api/notifications/sse", new SseHandler());
        server.createContext("/api/audit", new AuditHandler(auditDAO, security));
        server.createContext("/api/recherche", new RechercheHandler(rechercheService, security));

        server.setExecutor(Executors.newFixedThreadPool(10));
        server.start();
    }

}
