package ma.province.safi.passwordmanager.dao;

import ma.province.safi.passwordmanager.config.DatabaseConnection;
import ma.province.safi.passwordmanager.model.LogAudit;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AuditDAO {

    public void enregistrer(String action, String entite, Integer idCible, String cible, int idUtilisateur, String details)
            throws SQLException {
        String sql = "INSERT INTO dbo.LogAudit (Action, Entite, IdCible, Cible, Details, AdresseIP, IdUtilisateur) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, action);
            ps.setString(2, entite);
            if (idCible == null) ps.setNull(3, Types.INTEGER);
            else ps.setInt(3, idCible);
            ps.setString(4, cible != null ? cible : "");
            ps.setString(5, details);
            ps.setString(6, "127.0.0.1");
            ps.setInt(7, idUtilisateur);
            ps.executeUpdate();
        }
    }

    public void enregistrerOld(String action, String entite, Integer idCible, int idUtilisateur, String details) throws SQLException {
        enregistrer(action, entite, idCible, "", idUtilisateur, details);
    }

    public List<LogAudit> listerDerniers(int limit) throws SQLException {
        List<LogAudit> list = new ArrayList<>();
        String sql = "SELECT IdLog, Action, Entite, IdCible, Cible, Details, AdresseIP, IdUtilisateur, DateAction FROM dbo.LogAudit ORDER BY DateAction DESC OFFSET 0 ROWS FETCH NEXT ? ROWS ONLY";
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapLogAudit(rs));
                }
            }
        }
        return list;
    }

    private LogAudit mapLogAudit(ResultSet rs) throws SQLException {
        LogAudit a = new LogAudit();
        a.setIdLog(rs.getLong("IdLog"));
        a.setAction(rs.getString("Action"));
        a.setEntite(rs.getString("Entite"));
        a.setIdCible((Integer) rs.getObject("IdCible"));
        a.setCible(rs.getString("Cible"));
        a.setDetails(rs.getString("Details"));
        a.setAdresseIP(rs.getString("AdresseIP"));
        a.setIdUtilisateur(rs.getInt("IdUtilisateur"));
        Timestamp ts = rs.getTimestamp("DateAction");
        if (ts != null) a.setDateAction(ts.toLocalDateTime());
        return a;
    }
}
