package ma.province.safi.passwordmanager.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ma.province.safi.passwordmanager.config.DatabaseConnection;
import ma.province.safi.passwordmanager.security.SecurityInterceptor;
import ma.province.safi.passwordmanager.session.Session;
import ma.province.safi.passwordmanager.util.ResponseUtil;

import java.sql.*;

public class SchemaHandler implements HttpHandler {

    private final SecurityInterceptor security;

    public SchemaHandler(SecurityInterceptor security) {
        this.security = security;
    }

    @Override
    public void handle(HttpExchange exchange) {
        try {
            Session session = security.verifierAuthentification(exchange);
            if (session == null) return;

            if (!"GET".equals(exchange.getRequestMethod())) {
                ResponseUtil.json(exchange, 405, "{\"erreur\":\"Méthode non autorisée\"}");
                return;
            }

            StringBuilder sb = new StringBuilder();
            sb.append("[");

            try (Connection cn = DatabaseConnection.getConnection()) {
                DatabaseMetaData meta = cn.getMetaData();
                ResultSet tables = meta.getTables("GestionMotsDePasse_safi", "dbo", null, new String[]{"TABLE"});

                boolean firstTable = true;
                while (tables.next()) {
                    String tableName = tables.getString("TABLE_NAME");
                    if (!firstTable) sb.append(",");
                    firstTable = false;

                    sb.append("{\"nom\":").append(jsonStr(tableName)).append(",");
                    sb.append("\"colonnes\":[");

                    ResultSet cols = meta.getColumns("GestionMotsDePasse_safi", "dbo", tableName, null);
                    boolean firstCol = true;
                    while (cols.next()) {
                        if (!firstCol) sb.append(",");
                        firstCol = false;
                        sb.append("{");
                        sb.append("\"nom\":").append(jsonStr(cols.getString("COLUMN_NAME"))).append(",");
                        sb.append("\"type\":").append(jsonStr(cols.getString("TYPE_NAME") + "(" + cols.getInt("COLUMN_SIZE") + ")")).append(",");
                        sb.append("\"nullable\":").append(cols.getInt("NULLABLE") == DatabaseMetaData.columnNullable);
                        sb.append("}");
                    }
                    cols.close();
                    sb.append("]");

                    // PK
                    ResultSet pk = meta.getPrimaryKeys("GestionMotsDePasse_safi", "dbo", tableName);
                    if (pk.next()) {
                        sb.append(",\"pk\":").append(jsonStr(pk.getString("COLUMN_NAME")));
                    }
                    pk.close();

                    // FK
                    ResultSet fk = meta.getImportedKeys("GestionMotsDePasse_safi", "dbo", tableName);
                    sb.append(",\"fk\":[");
                    boolean firstFk = true;
                    while (fk.next()) {
                        if (!firstFk) sb.append(",");
                        firstFk = false;
                        sb.append("{");
                        sb.append("\"colonne\":").append(jsonStr(fk.getString("FKCOLUMN_NAME"))).append(",");
                        sb.append("\"refere\":").append(jsonStr(fk.getString("PKTABLE_NAME") + "." + fk.getString("PKCOLUMN_NAME")));
                        sb.append("}");
                    }
                    fk.close();
                    sb.append("]");
                    sb.append("}");
                }
                tables.close();
            }

            sb.append("]");
            ResponseUtil.json(exchange, 200, sb.toString());

        } catch (Exception e) {
            try {
                ResponseUtil.json(exchange, 500, "{\"erreur\":" + jsonStr(e.getMessage()) + "}");
            } catch (Exception ignored) {}
        }
    }

    private static String jsonStr(String s) {
        if (s == null) return "null";
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }
}
