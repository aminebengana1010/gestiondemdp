package ma.province.safi.passwordmanager.util;

import ma.province.safi.passwordmanager.config.DatabaseConnection;

import java.sql.*;

public class AfficherTables {

    public static void main(String[] args) {
        try (Connection cn = DatabaseConnection.getConnection()) {
            DatabaseMetaData meta = cn.getMetaData();

            // Récupérer les tables utilisateur (pas les systemes)
            ResultSet tables = meta.getTables("GestionMotsDePasse_safi", "dbo", null, new String[]{"TABLE"});

            System.out.println("╔══════════════════════════════════════════════════╗");
            System.out.println("║  Tables - GestionMotsDePasse_safi              ║");
            System.out.println("╚══════════════════════════════════════════════════╝");
            System.out.println();

            while (tables.next()) {
                String tableName = tables.getString("TABLE_NAME");
                afficherTable(meta, tableName);
                System.out.println();
            }

            tables.close();

        } catch (SQLException e) {
            System.err.println("Erreur: " + e.getMessage());
        }
    }

    private static void afficherTable(DatabaseMetaData meta, String tableName) throws SQLException {
        System.out.println("┌──────────────────────────────────────────────────────────┐");
        System.out.printf("│  %-54s │%n", tableName);
        System.out.println("├──────────────────────────────────────────────────────────┤");
        System.out.printf("│  %-3s %-25s %-15s %-7s│%n", "#", "COLONNE", "TYPE", "NULL?");
        System.out.println("├──────────────────────────────────────────────────────────┤");

        ResultSet cols = meta.getColumns("GestionMotsDePasse_safi", "dbo", tableName, null);
        int i = 1;
        while (cols.next()) {
            String name = cols.getString("COLUMN_NAME");
            String type = cols.getString("TYPE_NAME");
            int size = cols.getInt("COLUMN_SIZE");
            int nullable = cols.getInt("NULLABLE");
            String isNullable = (nullable == DatabaseMetaData.columnNullable) ? "OUI" : "NON";

            String typeStr = type + "(" + size + ")";
            System.out.printf("│  %-3d %-25s %-15s %-7s│%n", i, name, typeStr, isNullable);
            i++;
        }
        cols.close();

        // Clés primaires
        ResultSet pk = meta.getPrimaryKeys("GestionMotsDePasse_safi", "dbo", tableName);
        if (pk.next()) {
            System.out.println("├──────────────────────────────────────────────────────────┤");
            System.out.printf("│  PK: %-51s │%n", pk.getString("COLUMN_NAME"));
        }
        pk.close();

        // Clés étrangères
        ResultSet fk = meta.getImportedKeys("GestionMotsDePasse_safi", "dbo", tableName);
        boolean hasFk = false;
        while (fk.next()) {
            if (!hasFk) {
                System.out.println("├──────────────────────────────────────────────────────────┤");
                hasFk = true;
            }
            String fkCol = fk.getString("FKCOLUMN_NAME");
            String pkTable = fk.getString("PKTABLE_NAME");
            String pkCol = fk.getString("PKCOLUMN_NAME");
            System.out.printf("│  FK: %-12s → %-18s %-15s│%n", fkCol, pkTable + "." + pkCol, "");
        }
        fk.close();

        System.out.println("└──────────────────────────────────────────────────────────┘");
    }
}
