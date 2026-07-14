package ma.province.safi.passwordmanager.dao;

import ma.province.safi.passwordmanager.config.DatabaseConnection;
import ma.province.safi.passwordmanager.model.Notification;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificationDAO {

    public void creer(int idUtilisateur, String message, String type) throws SQLException {
        String sql = "INSERT INTO dbo.Notification (IdUtilisateur, Message, TypeNotification, Lu) VALUES (?, ?, ?, 0)";
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idUtilisateur);
            ps.setString(2, message);
            ps.setString(3, type);
            ps.executeUpdate();
        }
    }

    public List<Notification> listerNonLues(int idUtilisateur) throws SQLException {
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT IdNotification, IdUtilisateur, Message, TypeNotification, Lu, DateNotification FROM dbo.Notification WHERE IdUtilisateur = ? AND Lu = 0 ORDER BY DateNotification DESC";
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idUtilisateur);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapNotification(rs));
                }
            }
        }
        return list;
    }

    public List<Notification> listerToutes(int idUtilisateur) throws SQLException {
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT IdNotification, IdUtilisateur, Message, TypeNotification, Lu, DateNotification FROM dbo.Notification WHERE IdUtilisateur = ? ORDER BY DateNotification DESC";
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idUtilisateur);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapNotification(rs));
                }
            }
        }
        return list;
    }

    public void marquerLue(long idNotification) throws SQLException {
        String sql = "UPDATE dbo.Notification SET Lu = 1, DateLecture = SYSUTCDATETIME() WHERE IdNotification = ?";
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, idNotification);
            ps.executeUpdate();
        }
    }

    public int compterNonLues(int idUtilisateur) throws SQLException {
        String sql = "SELECT COUNT(*) FROM dbo.Notification WHERE IdUtilisateur = ? AND Lu = 0";
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idUtilisateur);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private Notification mapNotification(ResultSet rs) throws SQLException {
        Notification n = new Notification();
        n.setIdNotification(rs.getLong("IdNotification"));
        n.setIdUtilisateur(rs.getInt("IdUtilisateur"));
        n.setMessage(rs.getString("Message"));
        n.setTypeNotification(rs.getString("TypeNotification"));
        n.setLu(rs.getBoolean("Lu"));
        Timestamp ts = rs.getTimestamp("DateNotification");
        if (ts != null) n.setDateNotification(ts.toLocalDateTime());
        return n;
    }
}
