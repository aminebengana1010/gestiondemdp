import java.sql.*;

public class AfficherSchema {

    static final String URL = "jdbc:sqlserver://localhost:1433;databaseName=GestionMotsDePasse_safi;encrypt=false";
    static final String USER = "sa";
    static final String PASSWORD = "sa";

    public static void main(String[] args) {
        try (Connection cn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            DatabaseMetaData meta = cn.getMetaData();
            ResultSet tables = meta.getTables(null, "dbo", null, new String[]{"TABLE"});

            while (tables.next()) {
                String name = tables.getString("TABLE_NAME");
                afficherTable(meta, name);
            }
            tables.close();
        } catch (SQLException e) {
            System.err.println("Erreur: " + e.getMessage());
        }
    }

    static void afficherTable(DatabaseMetaData meta, String name) throws SQLException {
        String sep = "+" + "-".repeat(60) + "+";
        System.out.println(sep);
        System.out.printf("| %-58s |%n", name);
        System.out.println(sep);
        System.out.printf("| %-3s %-22s %-18s %-8s|%n", "#", "COLONNE", "TYPE", "NULL?");
        System.out.println(sep);

        ResultSet cols = meta.getColumns(null, "dbo", name, null);
        int i = 1;
        while (cols.next()) {
            String col = cols.getString("COLUMN_NAME");
            String type = cols.getString("TYPE_NAME") + "(" + cols.getInt("COLUMN_SIZE") + ")";
            String nul = cols.getInt("NULLABLE") == DatabaseMetaData.columnNullable ? "OUI" : "NON";
            System.out.printf("| %-3d %-22s %-18s %-8s|%n", i, col, type, nul);
            i++;
        }
        cols.close();

        ResultSet pk = meta.getPrimaryKeys(null, "dbo", name);
        if (pk.next()) {
            System.out.println(sep);
            System.out.printf("| PK: %-55s |%n", pk.getString("COLUMN_NAME"));
        }
        pk.close();

        ResultSet fk = meta.getImportedKeys(null, "dbo", name);
        boolean headerFk = false;
        while (fk.next()) {
            if (!headerFk) { System.out.println(sep); headerFk = true; }
            String fcol = fk.getString("FKCOLUMN_NAME");
            String ref = fk.getString("PKTABLE_NAME") + "." + fk.getString("PKCOLUMN_NAME");
            System.out.printf("| FK: %-14s -> %-38s |%n", fcol, ref);
        }
        fk.close();
        System.out.println(sep);
        System.out.println();
    }
}
