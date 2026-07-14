package ma.province.safi.passwordmanager.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DatabaseConnection {

    private static final String URL =
        "jdbc:sqlserver://localhost:1433;" +
        "databaseName=GestionMotsDePasse_safi;" +
        "encrypt=true;" +
        "trustServerCertificate=true;";

    private static final String USER = "sa";
    private static final String PASSWORD = "sa";

    private DatabaseConnection() {}

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
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
