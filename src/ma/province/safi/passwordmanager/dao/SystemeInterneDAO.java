package ma.province.safi.passwordmanager.dao;

import ma.province.safi.passwordmanager.config.DatabaseConnection;
import ma.province.safi.passwordmanager.model.SystemeInterne;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SystemeInterneDAO {

    public void ajouter(SystemeInterne si, String secretChiffre, String iv) throws SQLException {
        String sql = """
            INSERT INTO dbo.SystemeInterne (NomSysteme, UrlSysteme, IdDivisionInterne, LoginAdmin, MotPasseAdminChiffre, VecteurInitialisation, DateDernierChangement)
            VALUES (?, ?, ?, ?, ?, ?, SYSUTCDATETIME())
            """;
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, si.getNom());
            ps.setString(2, si.getUrl());
            ps.setInt(3, si.getIdDivisionInterne());
            ps.setString(4, si.getLogin());
            ps.setString(5, secretChiffre);
            ps.setString(6, iv);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) si.setId(rs.getInt(1));
            }
        }
    }

    public void modifier(SystemeInterne si) throws SQLException {
        String sql = "UPDATE dbo.SystemeInterne SET NomSysteme=?, UrlSysteme=?, IdDivisionInterne=?, LoginAdmin=? WHERE IdSystemeInterne=?";
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, si.getNom());
            ps.setString(2, si.getUrl());
            ps.setInt(3, si.getIdDivisionInterne());
            ps.setString(4, si.getLogin());
            ps.setInt(5, si.getId());
            ps.executeUpdate();
        }
    }

    public void mettreAJourSecret(int id, String secretChiffre, String iv) throws SQLException {
        String sql = "UPDATE dbo.SystemeInterne SET MotPasseAdminChiffre=?, VecteurInitialisation=?, DateDernierChangement=SYSUTCDATETIME() WHERE IdSystemeInterne=?";
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
             PreparedStatement ps = cn.prepareStatement("DELETE FROM dbo.SystemeInterne WHERE IdSystemeInterne=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public SystemeInterne trouverParId(int id) throws SQLException {
        String sql = "SELECT IdSystemeInterne, NomSysteme, UrlSysteme, IdDivisionInterne, LoginAdmin, MotPasseAdminChiffre, VecteurInitialisation, DateDernierChangement FROM dbo.SystemeInterne WHERE IdSystemeInterne=?";
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                SystemeInterne si = new SystemeInterne();
                si.setId(rs.getInt("IdSystemeInterne"));
                si.setNom(rs.getString("NomSysteme"));
                si.setUrl(rs.getString("UrlSysteme"));
                si.setIdDivisionInterne(rs.getInt("IdDivisionInterne"));
                si.setLogin(rs.getString("LoginAdmin"));
                si.setMotPasseChiffre(rs.getString("MotPasseAdminChiffre"));
                si.setVecteurInitialisation(rs.getString("VecteurInitialisation"));
                Timestamp ts = rs.getTimestamp("DateDernierChangement");
                if (ts != null) si.setDateDernierChangement(ts.toLocalDateTime());
                return si;
            }
        }
    }

    public List<SystemeInterne> lister() throws SQLException {
        List<SystemeInterne> list = new ArrayList<>();
        String sql = "SELECT IdSystemeInterne, NomSysteme, UrlSysteme, IdDivisionInterne, LoginAdmin, MotPasseAdminChiffre, VecteurInitialisation, DateDernierChangement FROM dbo.SystemeInterne ORDER BY NomSysteme";
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                SystemeInterne si = new SystemeInterne();
                si.setId(rs.getInt("IdSystemeInterne"));
                si.setNom(rs.getString("NomSysteme"));
                si.setUrl(rs.getString("UrlSysteme"));
                si.setIdDivisionInterne(rs.getInt("IdDivisionInterne"));
                si.setLogin(rs.getString("LoginAdmin"));
                si.setMotPasseChiffre(rs.getString("MotPasseAdminChiffre"));
                si.setVecteurInitialisation(rs.getString("VecteurInitialisation"));
                si.setDateDernierChangement(rs.getTimestamp("DateDernierChangement") != null ? rs.getTimestamp("DateDernierChangement").toLocalDateTime() : null);
                list.add(si);
            }
        }
        return list;
    }

    public List<SystemeInterne> rechercher(String texte) throws SQLException {
        List<SystemeInterne> list = new ArrayList<>();
        String sql = "SELECT IdSystemeInterne, NomSysteme, UrlSysteme, IdDivisionInterne, LoginAdmin, MotPasseAdminChiffre, VecteurInitialisation FROM dbo.SystemeInterne WHERE NomSysteme LIKE ? OR UrlSysteme LIKE ? OR LoginAdmin LIKE ? ORDER BY NomSysteme";
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            String p = "%" + texte + "%";
            for (int i = 1; i <= 3; i++) ps.setString(i, p);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    SystemeInterne si = new SystemeInterne();
                    si.setId(rs.getInt("IdSystemeInterne"));
                    si.setNom(rs.getString("NomSysteme"));
                    si.setUrl(rs.getString("UrlSysteme"));
                    si.setIdDivisionInterne(rs.getInt("IdDivisionInterne"));
                    si.setLogin(rs.getString("LoginAdmin"));
                    si.setMotPasseChiffre(rs.getString("MotPasseAdminChiffre"));
                    si.setVecteurInitialisation(rs.getString("VecteurInitialisation"));
                    list.add(si);
                }
            }
        }
        return list;
    }
}
