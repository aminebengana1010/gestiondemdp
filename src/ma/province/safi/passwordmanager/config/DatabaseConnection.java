package ma.province.safi.passwordmanager.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DatabaseConnection {

    private static final String HOTE_DEFAUT = "localhost";
    private static final String PORT_DEFAUT = "1433";
    private static final String BASE_DEFAUT = "GestionMotsDePasse_safi";
    private static final String UTILISATEUR_DEFAUT = "sa";
    private static final String MDP_DEFAUT = "sa";

    private static final String URL;

    static {
        String hote = env("DB_HOST", HOTE_DEFAUT);
        String port = env("DB_PORT", PORT_DEFAUT);
        String base = env("DB_NAME", BASE_DEFAUT);
        URL = "jdbc:sqlserver://" + hote + ":" + port + ";" +
              "databaseName=" + base + ";" +
              "encrypt=true;" +
              "trustServerCertificate=true;";
    }

    private static final String USER = env("DB_USER", UTILISATEUR_DEFAUT);
    private static final String PASSWORD = env("DB_PASSWORD", MDP_DEFAUT);

    private static String env(String nom, String defaut) {
        String valeur = System.getenv(nom);
        return (valeur == null || valeur.isBlank()) ? defaut : valeur.trim();
    }

    private DatabaseConnection() {}

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static String getUrl() {
        return URL;
    }

    public static boolean testConnection() {
        try (Connection cn = getConnection()) {
            return cn.isValid(5);
        } catch (SQLException e) {
            System.err.println("[DB] Connexion échouée: " + e.getMessage());
            return false;
        }
    }
}
