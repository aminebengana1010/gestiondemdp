package ma.province.safi.passwordmanager.dao;

import ma.province.safi.passwordmanager.config.DatabaseConnection;
import ma.province.safi.passwordmanager.model.SwitchReseau;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SwitchDAO {

    public void ajouter(SwitchReseau sw, String secretChiffre, String iv) throws SQLException {
        String sql = """
            INSERT INTO dbo.SwitchReseau (NomSwitch, AdresseMAC, Emplacement, LoginSwitch, MotPasseChiffre, VecteurInitialisation, DateDernierChangement)
            VALUES (?, ?, ?, ?, ?, ?, SYSUTCDATETIME())
            """;
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, sw.getNom());
            ps.setString(2, sw.getAdresseMAC());
            ps.setString(3, sw.getEmplacement());
            ps.setString(4, sw.getLogin());
            ps.setString(5, secretChiffre);
            ps.setString(6, iv);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) sw.setId(rs.getInt(1));
            }
        }
    }

    public void modifier(SwitchReseau sw) throws SQLException {
        String sql = "UPDATE dbo.SwitchReseau SET NomSwitch=?, AdresseMAC=?, Emplacement=?, LoginSwitch=? WHERE IdSwitch=?";
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, sw.getNom());
            ps.setString(2, sw.getAdresseMAC());
            ps.setString(3, sw.getEmplacement());
            ps.setString(4, sw.getLogin());
            ps.setInt(5, sw.getId());
            ps.executeUpdate();
        }
    }

    public void mettreAJourSecret(int id, String secretChiffre, String iv) throws SQLException {
        String sql = "UPDATE dbo.SwitchReseau SET MotPasseChiffre=?, VecteurInitialisation=?, DateDernierChangement=SYSUTCDATETIME() WHERE IdSwitch=?";
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
             PreparedStatement ps = cn.prepareStatement("DELETE FROM dbo.SwitchReseau WHERE IdSwitch=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public SwitchReseau trouverParId(int id) throws SQLException {
        String sql = "SELECT IdSwitch, NomSwitch, AdresseMAC, Emplacement, LoginSwitch, MotPasseChiffre, VecteurInitialisation, DateDernierChangement FROM dbo.SwitchReseau WHERE IdSwitch=?";
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                SwitchReseau sw = new SwitchReseau();
                sw.setId(rs.getInt("IdSwitch"));
                sw.setNom(rs.getString("NomSwitch"));
                sw.setAdresseMAC(rs.getString("AdresseMAC"));
                sw.setEmplacement(rs.getString("Emplacement"));
                sw.setLogin(rs.getString("LoginSwitch"));
                sw.setMotPasseChiffre(rs.getString("MotPasseChiffre"));
                sw.setVecteurInitialisation(rs.getString("VecteurInitialisation"));
                Timestamp ts = rs.getTimestamp("DateDernierChangement");
                if (ts != null) sw.setDateDernierChangement(ts.toLocalDateTime());
                return sw;
            }
        }
    }

    public List<SwitchReseau> lister() throws SQLException {
        List<SwitchReseau> list = new ArrayList<>();
        String sql = "SELECT IdSwitch, NomSwitch, AdresseMAC, Emplacement, LoginSwitch, MotPasseChiffre, VecteurInitialisation, DateDernierChangement FROM dbo.SwitchReseau ORDER BY NomSwitch";
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                SwitchReseau sw = new SwitchReseau();
                sw.setId(rs.getInt("IdSwitch"));
                sw.setNom(rs.getString("NomSwitch"));
                sw.setAdresseMAC(rs.getString("AdresseMAC"));
                sw.setEmplacement(rs.getString("Emplacement"));
                sw.setLogin(rs.getString("LoginSwitch"));
                sw.setMotPasseChiffre(rs.getString("MotPasseChiffre"));
                sw.setVecteurInitialisation(rs.getString("VecteurInitialisation"));
                sw.setDateDernierChangement(rs.getTimestamp("DateDernierChangement") != null ? rs.getTimestamp("DateDernierChangement").toLocalDateTime() : null);
                list.add(sw);
            }
        }
        return list;
    }

    public List<SwitchReseau> rechercher(String texte) throws SQLException {
        List<SwitchReseau> list = new ArrayList<>();
        String sql = "SELECT IdSwitch, NomSwitch, AdresseMAC, Emplacement, LoginSwitch, MotPasseChiffre, VecteurInitialisation FROM dbo.SwitchReseau WHERE NomSwitch LIKE ? OR AdresseMAC LIKE ? OR Emplacement LIKE ? OR LoginSwitch LIKE ? ORDER BY NomSwitch";
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            String p = "%" + texte + "%";
            for (int i = 1; i <= 4; i++) ps.setString(i, p);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    SwitchReseau sw = new SwitchReseau();
                    sw.setId(rs.getInt("IdSwitch"));
                    sw.setNom(rs.getString("NomSwitch"));
                    sw.setAdresseMAC(rs.getString("AdresseMAC"));
                    sw.setEmplacement(rs.getString("Emplacement"));
                    sw.setLogin(rs.getString("LoginSwitch"));
                    sw.setMotPasseChiffre(rs.getString("MotPasseChiffre"));
                    sw.setVecteurInitialisation(rs.getString("VecteurInitialisation"));
                    list.add(sw);
                }
            }
        }
        return list;
    }
}
