package ma.province.safi.passwordmanager.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public final class DatabaseInitializer {

    private static final String SQL_PATH = "sql/gestion_mots_de_passe.sql";

    private DatabaseInitializer() {}

    public static void initialiser() throws IOException, SQLException {
        try (Connection cn = DatabaseConnection.getConnection()) {
            // Vérifier si la base est déjà initialisée
            if (tableExiste(cn, "Role")) {
                System.out.println("[DB] Tables déjà présentes — initialisation ignorée.");
                return;
            }
        }

        if (!Files.exists(Paths.get(SQL_PATH))) {
            System.err.println("[DB] Fichier SQL introuvable: " + SQL_PATH);
            System.err.println("[DB] Créez-le ou exécutez-le manuellement dans SSMS.");
            return;
        }

        String sql = Files.readString(Paths.get(SQL_PATH));

        // Supprimer les blocs IF DB_ID / CREATE DATABASE / USE
        sql = sql.replaceAll("(?is)IF\\s+DB_ID\\s*\\([^)]+\\)\\s*IS\\s+NULL[^;]*;", "");
        sql = sql.replaceAll("(?is)CREATE\\s+DATABASE\\s+[^;]+;", "");
        sql = sql.replaceAll("(?is)USE\\s+\\w+\\s*;", "");

        // Découper par GO
        String[] instructions = sql.split("\\bGO\\b");

        try (Connection cn = DatabaseConnection.getConnection();
             Statement stmt = cn.createStatement()) {

            int cpt = 0;
            for (String instruction : instructions) {
                String trimmed = instruction.trim();
                if (trimmed.isEmpty()) continue;

                try {
                    stmt.execute(trimmed);
                    cpt++;
                } catch (SQLException e) {
                    String msg = e.getMessage().toUpperCase();
                    if (msg.contains("ALREADY EXISTS") || msg.contains("OBJECT '")
                            || msg.contains("EXISTE DÉJÀ") || msg.contains("IL EXISTE DÉJÀ")
                            || msg.contains("VIOLATION OF UNIQUE KEY") || msg.contains("VIOLATION DE")) {
                        // Déjà existant — normal
                    } else {
                        System.err.println("[DB] " + e.getMessage());
                        System.err.println("[DB] → " + trimmed.substring(0, Math.min(100, trimmed.length())));
                    }
                }
            }
            System.out.println("[DB] Initialisation terminée (" + cpt + " instructions exécutées).");
        }
    }

    public static void mettreAJourContrainteDivisionType() throws SQLException {
        try (Connection cn = DatabaseConnection.getConnection();
             Statement st = cn.createStatement()) {
            // Delete old data incompatible with new constraint
            st.execute("DELETE FROM dbo.SystemeExterne WHERE IdDivisionExterne IN (SELECT IdDivisionExterne FROM dbo.DivisionExterne WHERE TypeDivision NOT IN (N'AAL', N'Commune', N'Pashalik', N'District'))");
            st.execute("DELETE FROM dbo.DivisionExterne WHERE TypeDivision NOT IN (N'AAL', N'Commune', N'Pashalik', N'District')");
            // Drop old constraint if exists
            try { st.execute("ALTER TABLE dbo.DivisionExterne DROP CONSTRAINT CK_DivisionExterne_Type"); }
            catch (SQLException ignored) {}
            // Drop old unique constraint if exists
            try { st.execute("ALTER TABLE dbo.DivisionExterne DROP CONSTRAINT UQ_DivisionExterne_NomType"); }
            catch (SQLException ignored) {}
            // Add new columns if missing
            try { st.execute("ALTER TABLE dbo.DivisionExterne ADD SousType NVARCHAR(50) NULL"); }
            catch (SQLException ignored) {}
            try { st.execute("ALTER TABLE dbo.DivisionExterne ADD CaidatNom NVARCHAR(255) NULL"); }
            catch (SQLException ignored) {}
            // Add new constraint
            st.execute("ALTER TABLE dbo.DivisionExterne ADD CONSTRAINT CK_DivisionExterne_Type CHECK (TypeDivision IN (N'AAL', N'Commune', N'Pashalik', N'District'))");
            // Add new unique on NomDivision
            try { st.execute("ALTER TABLE dbo.DivisionExterne ADD CONSTRAINT UQ_DivisionExterne_Nom UNIQUE (NomDivision)"); }
            catch (SQLException ignored) {}
            System.out.println("[DB] Contrainte CK_DivisionExterne_Type et colonnes mises à jour.");
        }
    }

    public static void mettreAJourColonneService() throws SQLException {
        try (Connection cn = DatabaseConnection.getConnection()) {
            // Vérifier si la colonne Service existe déjà
            String sql = "SELECT COUNT(*) FROM sys.columns WHERE object_id = OBJECT_ID('dbo.DivisionInterne') AND name = 'Service'";
            try (Statement st = cn.createStatement();
                 ResultSet rs = st.executeQuery(sql)) {
                if (rs.next() && rs.getInt(1) == 0) {
                    try (Statement st2 = cn.createStatement()) {
                        st2.execute("ALTER TABLE dbo.DivisionInterne ADD Service NVARCHAR(100) NULL");
                        System.out.println("[DB] Colonne Service ajoutée à DivisionInterne.");
                    }
                }
            }
            // Remplacer UQ_DivisionInterne_Nom par index filtré (ignore NULLs)
            String checkUq = "SELECT COUNT(*) FROM sys.indexes WHERE object_id = OBJECT_ID('dbo.DivisionInterne') AND name = 'UQ_DivisionInterne_Nom'";
            try (Statement st = cn.createStatement();
                 ResultSet rs = st.executeQuery(checkUq)) {
                if (rs.next() && rs.getInt(1) > 0) {
                    try (Statement st2 = cn.createStatement()) {
                        st2.execute("ALTER TABLE dbo.DivisionInterne DROP CONSTRAINT UQ_DivisionInterne_Nom");
                        st2.execute("ALTER TABLE dbo.DivisionInterne ALTER COLUMN Service NVARCHAR(255) NULL");
                        st2.execute("CREATE UNIQUE NONCLUSTERED INDEX UQ_DivisionInterne_NomService ON dbo.DivisionInterne (NomDivision, Service) WHERE Service IS NOT NULL");
                        System.out.println("[DB] Contrainte UQ remplacée par index filtré, colonne Service passa à NVARCHAR(255).");
                    }
                }
            }
            // Vérifier si l'ancien index filtré existe déjà (migration déjà faite)
            String checkIdx = "SELECT COUNT(*) FROM sys.indexes WHERE object_id = OBJECT_ID('dbo.DivisionInterne') AND name = 'UQ_DivisionInterne_NomService'";
            try (Statement st = cn.createStatement();
                 ResultSet rs = st.executeQuery(checkIdx)) {
                if (!rs.next() || rs.getInt(1) == 0) {
                    // Si la contrainte UNIQUE existe encore, la remplacer
                    String checkUq2 = "SELECT COUNT(*) FROM sys.objects WHERE parent_object_id = OBJECT_ID('dbo.DivisionInterne') AND name = 'UQ_DivisionInterne_NomService'";
                    try (Statement st2 = cn.createStatement();
                         ResultSet rs2 = st2.executeQuery(checkUq2)) {
                        if (rs2.next() && rs2.getInt(1) > 0) {
                            st.execute("ALTER TABLE dbo.DivisionInterne DROP CONSTRAINT UQ_DivisionInterne_NomService");
                            System.out.println("[DB] Ancienne contrainte UQ_DivisionInterne_NomService supprimée.");
                        }
                    }
                    st.execute("ALTER TABLE dbo.DivisionInterne ALTER COLUMN Service NVARCHAR(255) NULL");
                    st.execute("CREATE UNIQUE NONCLUSTERED INDEX UQ_DivisionInterne_NomService ON dbo.DivisionInterne (NomDivision, Service) WHERE Service IS NOT NULL");
                    System.out.println("[DB] Index filtré UQ_DivisionInterne_NomService créé.");
                }
            }
        }
    }

    private static boolean tableExiste(Connection cn, String nomTable) throws SQLException {
        try (Statement st = cn.createStatement();
             ResultSet rs = st.executeQuery(
                 "SELECT COUNT(*) FROM sys.tables WHERE name = '" + nomTable + "'")) {
            return rs.next() && rs.getInt(1) > 0;
        }
    }
}
