package ma.province.safi.passwordmanager.dao;

import ma.province.safi.passwordmanager.config.DatabaseConnection;
import ma.province.safi.passwordmanager.model.Utilisateur;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UtilisateurDAO {

    public Utilisateur trouverParLogin(String login) throws SQLException {
        String sql = """
            SELECT u.IdUtilisateur, u.Nom, u.Email, u.Login, u.MotPasseHash, u.SelMotPasse,
                   u.IdRole, u.EstActif, u.DateCreation
            FROM dbo.Utilisateur u
            WHERE u.Login = ?
            """;
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, login);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return mapUtilisateur(rs);
            }
        }
    }

    public Utilisateur trouverParId(int id) throws SQLException {
        String sql = """
            SELECT u.IdUtilisateur, u.Nom, u.Email, u.Login, u.MotPasseHash, u.SelMotPasse,
                   u.IdRole, u.EstActif, u.DateCreation
            FROM dbo.Utilisateur u
            WHERE u.IdUtilisateur = ?
            """;
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return mapUtilisateur(rs);
            }
        }
    }

    public void ajouter(Utilisateur u) throws SQLException {
        String sql = "INSERT INTO dbo.Utilisateur (Nom, Email, Login, MotPasseHash, SelMotPasse, IdRole, EstActif) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, u.getNom());
            ps.setString(2, u.getEmail());
            ps.setString(3, u.getLogin());
            ps.setString(4, u.getMotPasseHash());
            ps.setString(5, u.getSelMotPasse());
            ps.setInt(6, u.getIdRole());
            ps.setBoolean(7, u.isActif());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) u.setIdUtilisateur(rs.getInt(1));
            }
        }
    }

    public List<Utilisateur> lister() throws SQLException {
        List<Utilisateur> list = new ArrayList<>();
        String sql = "SELECT IdUtilisateur, Nom, Email, Login, IdRole, EstActif, DateCreation FROM dbo.Utilisateur ORDER BY Nom";
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Utilisateur u = new Utilisateur();
                u.setIdUtilisateur(rs.getInt("IdUtilisateur"));
                u.setNom(rs.getString("Nom"));
                u.setEmail(rs.getString("Email"));
                u.setLogin(rs.getString("Login"));
                u.setIdRole(rs.getInt("IdRole"));
                u.setEstActif(rs.getBoolean("EstActif"));
                Timestamp ts = rs.getTimestamp("DateCreation");
                if (ts != null) u.setDateCreation(ts.toLocalDateTime());
                list.add(u);
            }
        }
        return list;
    }

    private Utilisateur mapUtilisateur(ResultSet rs) throws SQLException {
        Utilisateur u = new Utilisateur();
        u.setIdUtilisateur(rs.getInt("IdUtilisateur"));
        u.setNom(rs.getString("Nom"));
        u.setEmail(rs.getString("Email"));
        u.setLogin(rs.getString("Login"));
        u.setMotPasseHash(rs.getString("MotPasseHash"));
        u.setSelMotPasse(rs.getString("SelMotPasse"));
        u.setIdRole(rs.getInt("IdRole"));
        u.setEstActif(rs.getBoolean("EstActif"));
        Timestamp ts = rs.getTimestamp("DateCreation");
        if (ts != null) u.setDateCreation(ts.toLocalDateTime());
        return u;
    }
}
