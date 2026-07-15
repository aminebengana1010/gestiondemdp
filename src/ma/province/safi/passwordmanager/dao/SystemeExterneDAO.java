package ma.province.safi.passwordmanager.dao;

import ma.province.safi.passwordmanager.config.DatabaseConnection;
import ma.province.safi.passwordmanager.model.SystemeExterne;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SystemeExterneDAO {

    public void ajouter(SystemeExterne se, String secretChiffre, String iv) throws SQLException {
        String sql = """
            INSERT INTO dbo.SystemeExterne (NomSysteme, UrlSysteme, IdDivisionExterne, IdSystemeInterne, LoginSysteme, MotPasseChiffre, VecteurInitialisation, DateDernierChangement)
            VALUES (?, ?, ?, ?, ?, ?, ?, SYSUTCDATETIME())
            """;
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, se.getNom());
            ps.setString(2, se.getUrl());
            ps.setInt(3, se.getIdDivisionExterne());
            if (se.getIdSystemeInterneLie() == null) ps.setNull(4, Types.INTEGER);
            else ps.setInt(4, se.getIdSystemeInterneLie());
            ps.setString(5, se.getLogin());
            ps.setString(6, secretChiffre);
            ps.setString(7, iv);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) se.setId(rs.getInt(1));
            }
        }
    }

    public void modifier(SystemeExterne se) throws SQLException {
        String sql = "UPDATE dbo.SystemeExterne SET NomSysteme=?, UrlSysteme=?, IdDivisionExterne=?, IdSystemeInterne=?, LoginSysteme=? WHERE IdSystemeExterne=?";
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, se.getNom());
            ps.setString(2, se.getUrl());
            ps.setInt(3, se.getIdDivisionExterne());
            if (se.getIdSystemeInterneLie() == null) ps.setNull(4, Types.INTEGER);
            else ps.setInt(4, se.getIdSystemeInterneLie());
            ps.setString(5, se.getLogin());
            ps.setInt(6, se.getId());
            ps.executeUpdate();
        }
    }

    public void mettreAJourSecret(int id, String secretChiffre, String iv) throws SQLException {
        String sql = "UPDATE dbo.SystemeExterne SET MotPasseChiffre=?, VecteurInitialisation=?, DateDernierChangement=SYSUTCDATETIME() WHERE IdSystemeExterne=?";
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, secretChiffre);
            ps.setString(2, iv);
            ps.setInt(3, id);
            ps.executeUpdate();
        }
    }

    public void supprimer(int id) throws SQLException {
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement("DELETE FROM dbo.SystemeExterne WHERE IdSystemeExterne=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public SystemeExterne trouverParId(int id) throws SQLException {
        String sql = "SELECT IdSystemeExterne, NomSysteme, UrlSysteme, IdDivisionExterne, IdSystemeInterne, LoginSysteme, MotPasseChiffre, VecteurInitialisation, DateDernierChangement FROM dbo.SystemeExterne WHERE IdSystemeExterne=?";
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return mapSystemeExterne(rs);
            }
        }
    }

    public List<SystemeExterne> rechercher(String texte) throws SQLException {
        List<SystemeExterne> list = new ArrayList<>();
        String sql = "SELECT IdSystemeExterne, NomSysteme, UrlSysteme, IdDivisionExterne, IdSystemeInterne, LoginSysteme, MotPasseChiffre, VecteurInitialisation, DateDernierChangement FROM dbo.SystemeExterne WHERE NomSysteme LIKE ? OR UrlSysteme LIKE ? OR LoginSysteme LIKE ? ORDER BY NomSysteme";
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            String p = "%" + texte + "%";
            ps.setString(1, p); ps.setString(2, p); ps.setString(3, p);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapSystemeExterneLite(rs));
            }
        }
        return list;
    }

    public List<SystemeExterne> lister() throws SQLException {
        List<SystemeExterne> list = new ArrayList<>();
        String sql = "SELECT IdSystemeExterne, NomSysteme, UrlSysteme, IdDivisionExterne, IdSystemeInterne, LoginSysteme, MotPasseChiffre, VecteurInitialisation, DateDernierChangement FROM dbo.SystemeExterne ORDER BY NomSysteme";
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapSystemeExterneLite(rs));
            }
        }
        return list;
    }

    private SystemeExterne mapSystemeExterne(ResultSet rs) throws SQLException {
        SystemeExterne se = new SystemeExterne();
        se.setId(rs.getInt("IdSystemeExterne"));
        se.setNom(rs.getString("NomSysteme"));
        se.setUrl(rs.getString("UrlSysteme"));
        se.setIdDivisionExterne(rs.getInt("IdDivisionExterne"));
        int idSi = rs.getInt("IdSystemeInterne");
        se.setIdSystemeInterneLie(rs.wasNull() ? null : idSi);
        se.setLogin(rs.getString("LoginSysteme"));
        se.setMotPasseChiffre(rs.getString("MotPasseChiffre"));
        se.setVecteurInitialisation(rs.getString("VecteurInitialisation"));
        Timestamp ts = rs.getTimestamp("DateDernierChangement");
        if (ts != null) se.setDateDernierChangement(ts.toLocalDateTime());
        return se;
    }

    private SystemeExterne mapSystemeExterneLite(ResultSet rs) throws SQLException {
        SystemeExterne se = new SystemeExterne();
        se.setId(rs.getInt("IdSystemeExterne"));
        se.setNom(rs.getString("NomSysteme"));
        se.setUrl(rs.getString("UrlSysteme"));
        se.setIdDivisionExterne(rs.getInt("IdDivisionExterne"));
        int idSi = rs.getInt("IdSystemeInterne");
        se.setIdSystemeInterneLie(rs.wasNull() ? null : idSi);
        se.setLogin(rs.getString("LoginSysteme"));
        se.setMotPasseChiffre(rs.getString("MotPasseChiffre"));
        se.setVecteurInitialisation(rs.getString("VecteurInitialisation"));
        Timestamp ts = rs.getTimestamp("DateDernierChangement");
        if (ts != null) se.setDateDernierChangement(ts.toLocalDateTime());
        return se;
    }
}
