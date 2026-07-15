package ma.province.safi.passwordmanager.dao;

import ma.province.safi.passwordmanager.config.DatabaseConnection;
import ma.province.safi.passwordmanager.model.Serveur;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServeurDAO {

    public void ajouter(Serveur s, String secretChiffre, String iv) throws SQLException {
        String sql = """
            INSERT INTO dbo.Serveur (NomServeur, AdresseIP, LoginServeur, MotPasseChiffre, VecteurInitialisation, DateDernierChangement)
            VALUES (?, ?, ?, ?, ?, SYSUTCDATETIME())
            """;
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, s.getNom());
            ps.setString(2, s.getAdresseIP());
            ps.setString(3, s.getLogin());
            ps.setString(4, secretChiffre);
            ps.setString(5, iv);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) s.setId(rs.getInt(1));
            }
        }
    }

    public void modifier(Serveur s) throws SQLException {
        String sql = """
            UPDATE dbo.Serveur SET NomServeur=?, AdresseIP=?, LoginServeur=?
            WHERE IdServeur=?
            """;
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, s.getNom());
            ps.setString(2, s.getAdresseIP());
            ps.setString(3, s.getLogin());
            ps.setInt(4, s.getId());
            ps.executeUpdate();
        }
    }

    public void mettreAJourSecret(int id, String secretChiffre, String iv) throws SQLException {
        String sql = "UPDATE dbo.Serveur SET MotPasseChiffre=?, VecteurInitialisation=?, DateDernierChangement=SYSUTCDATETIME() WHERE IdServeur=?";
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
             PreparedStatement ps = cn.prepareStatement("DELETE FROM dbo.Serveur WHERE IdServeur=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public Serveur trouverParId(int id) throws SQLException {
        String sql = "SELECT IdServeur, NomServeur, AdresseIP, LoginServeur, MotPasseChiffre, VecteurInitialisation, DateDernierChangement FROM dbo.Serveur WHERE IdServeur=?";
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return mapServeur(rs);
            }
        }
    }

    public List<Serveur> lister() throws SQLException {
        List<Serveur> list = new ArrayList<>();
        String sql = "SELECT IdServeur, NomServeur, AdresseIP, LoginServeur, MotPasseChiffre, VecteurInitialisation, DateDernierChangement FROM dbo.Serveur ORDER BY NomServeur";
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Serveur s = new Serveur();
                s.setId(rs.getInt("IdServeur"));
                s.setNom(rs.getString("NomServeur"));
                s.setAdresseIP(rs.getString("AdresseIP"));
                s.setLogin(rs.getString("LoginServeur"));
                s.setMotPasseChiffre(rs.getString("MotPasseChiffre"));
                s.setVecteurInitialisation(rs.getString("VecteurInitialisation"));
                s.setDateDernierChangement(rs.getTimestamp("DateDernierChangement") != null ? rs.getTimestamp("DateDernierChangement").toLocalDateTime() : null);
                list.add(s);
            }
        }
        return list;
    }

    public List<Serveur> rechercher(String texte) throws SQLException {
        List<Serveur> list = new ArrayList<>();
        String sql = "SELECT IdServeur, NomServeur, AdresseIP, LoginServeur, MotPasseChiffre, VecteurInitialisation FROM dbo.Serveur WHERE NomServeur LIKE ? OR AdresseIP LIKE ? OR LoginServeur LIKE ? ORDER BY NomServeur";
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            String p = "%" + texte + "%";
            ps.setString(1, p); ps.setString(2, p); ps.setString(3, p);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Serveur s = new Serveur();
                    s.setId(rs.getInt("IdServeur"));
                    s.setNom(rs.getString("NomServeur"));
                    s.setAdresseIP(rs.getString("AdresseIP"));
                    s.setLogin(rs.getString("LoginServeur"));
                    s.setMotPasseChiffre(rs.getString("MotPasseChiffre"));
                    s.setVecteurInitialisation(rs.getString("VecteurInitialisation"));
                    list.add(s);
                }
            }
        }
        return list;
    }

    Serveur mapServeur(ResultSet rs) throws SQLException {
        Serveur s = new Serveur();
        s.setId(rs.getInt("IdServeur"));
        s.setNom(rs.getString("NomServeur"));
        s.setAdresseIP(rs.getString("AdresseIP"));
        s.setLogin(rs.getString("LoginServeur"));
        s.setMotPasseChiffre(rs.getString("MotPasseChiffre"));
        s.setVecteurInitialisation(rs.getString("VecteurInitialisation"));
        Timestamp ts = rs.getTimestamp("DateDernierChangement");
        if (ts != null) s.setDateDernierChangement(ts.toLocalDateTime());
        return s;
    }
}
