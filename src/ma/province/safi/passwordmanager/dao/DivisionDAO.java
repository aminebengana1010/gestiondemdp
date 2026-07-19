package ma.province.safi.passwordmanager.dao;

import ma.province.safi.passwordmanager.config.DatabaseConnection;
import ma.province.safi.passwordmanager.model.TypeDivisionExterne;
import ma.province.safi.passwordmanager.model.DivisionExterne;
import ma.province.safi.passwordmanager.model.DivisionInterne;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DivisionDAO {

    public List<DivisionInterne> listerInternes() throws SQLException {
        List<DivisionInterne> list = new ArrayList<>();
        String sql = "SELECT IdDivisionInterne, NomDivision, Service FROM dbo.DivisionInterne ORDER BY NomDivision";
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                DivisionInterne d = new DivisionInterne();
                d.setIdDivisionInterne(rs.getInt("IdDivisionInterne"));
                d.setNomDivision(rs.getString("NomDivision"));
                d.setService(rs.getString("Service"));
                list.add(d);
            }
        }
        return list;
    }

    public List<DivisionExterne> listerExternes() throws SQLException {
        List<DivisionExterne> list = new ArrayList<>();
        String sql = "SELECT IdDivisionExterne, NomDivision, TypeDivision, SousType, CaidatNom FROM dbo.DivisionExterne ORDER BY NomDivision";
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                DivisionExterne d = new DivisionExterne();
                d.setIdDivisionExterne(rs.getInt("IdDivisionExterne"));
                d.setNomDivision(rs.getString("NomDivision"));
                d.setType(TypeDivisionExterne.fromString(rs.getString("TypeDivision")));
                d.setSousType(rs.getString("SousType"));
                d.setCaidatNom(rs.getString("CaidatNom"));
                list.add(d);
            }
        }
        return list;
    }

    public void ajouterExterne(String nom, String type, String sousType, String caidatNom) throws SQLException {
        String sql = "INSERT INTO dbo.DivisionExterne (NomDivision, TypeDivision, SousType, CaidatNom) VALUES (?, ?, ?, ?)";
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, nom);
            ps.setString(2, type);
            if (sousType != null && !sousType.isBlank()) ps.setString(3, sousType);
            else ps.setNull(3, Types.NVARCHAR);
            if (caidatNom != null && !caidatNom.isBlank()) ps.setString(4, caidatNom);
            else ps.setNull(4, Types.NVARCHAR);
            ps.executeUpdate();
        }
    }

    public void modifierExterne(int id, String nom, String type, String sousType, String caidatNom) throws SQLException {
        String sql = "UPDATE dbo.DivisionExterne SET NomDivision = ?, TypeDivision = ?, SousType = ?, CaidatNom = ? WHERE IdDivisionExterne = ?";
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, nom);
            ps.setString(2, type);
            if (sousType != null && !sousType.isBlank()) ps.setString(3, sousType);
            else ps.setNull(3, Types.NVARCHAR);
            if (caidatNom != null && !caidatNom.isBlank()) ps.setString(4, caidatNom);
            else ps.setNull(4, Types.NVARCHAR);
            ps.setInt(5, id);
            ps.executeUpdate();
        }
    }

    public void ajouterInterne(String nom, String service) throws SQLException {
        String sql = "INSERT INTO dbo.DivisionInterne (NomDivision, Service) VALUES (?, ?)";
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, nom);
            ps.setString(2, service);
            ps.executeUpdate();
        }
    }

    public void modifierInterne(int id, String nom, String service) throws SQLException {
        String sql = "UPDATE dbo.DivisionInterne SET NomDivision = ?, Service = ? WHERE IdDivisionInterne = ?";
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, nom);
            ps.setString(2, service);
            ps.setInt(3, id);
            ps.executeUpdate();
        }
    }

    public void supprimerInterne(int id) throws SQLException {
        String sql = "DELETE FROM dbo.DivisionInterne WHERE IdDivisionInterne = ?";
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public void supprimerExterne(int id) throws SQLException {
        String sql = "DELETE FROM dbo.DivisionExterne WHERE IdDivisionExterne = ?";
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
