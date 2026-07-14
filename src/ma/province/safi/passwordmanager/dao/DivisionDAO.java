package ma.province.safi.passwordmanager.dao;

import ma.province.safi.passwordmanager.config.DatabaseConnection;
import ma.province.safi.passwordmanager.model.DivisionExterne;
import ma.province.safi.passwordmanager.model.DivisionInterne;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DivisionDAO {

    public List<DivisionInterne> listerInternes() throws SQLException {
        List<DivisionInterne> list = new ArrayList<>();
        String sql = "SELECT IdDivisionInterne, NomDivision FROM dbo.DivisionInterne ORDER BY NomDivision";
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                DivisionInterne d = new DivisionInterne();
                d.setIdDivisionInterne(rs.getInt("IdDivisionInterne"));
                d.setNomDivision(rs.getString("NomDivision"));
                list.add(d);
            }
        }
        return list;
    }

    public List<DivisionExterne> listerExternes() throws SQLException {
        List<DivisionExterne> list = new ArrayList<>();
        String sql = "SELECT IdDivisionExterne, NomDivision, TypeDivision FROM dbo.DivisionExterne ORDER BY NomDivision";
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                DivisionExterne d = new DivisionExterne();
                d.setIdDivisionExterne(rs.getInt("IdDivisionExterne"));
                d.setNomDivision(rs.getString("NomDivision"));
                d.setTypeDivision(rs.getString("TypeDivision"));
                list.add(d);
            }
        }
        return list;
    }

    public void ajouterInterne(String nom) throws SQLException {
        String sql = "INSERT INTO dbo.DivisionInterne (NomDivision) VALUES (?)";
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, nom);
            ps.executeUpdate();
        }
    }

    public void ajouterExterne(String nom, String type) throws SQLException {
        String sql = "INSERT INTO dbo.DivisionExterne (NomDivision, TypeDivision) VALUES (?, ?)";
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, nom);
            ps.setString(2, type);
            ps.executeUpdate();
        }
    }

    public void modifierInterne(int id, String nom) throws SQLException {
        String sql = "UPDATE dbo.DivisionInterne SET NomDivision = ? WHERE IdDivisionInterne = ?";
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, nom);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public void modifierExterne(int id, String nom, String type) throws SQLException {
        String sql = "UPDATE dbo.DivisionExterne SET NomDivision = ?, TypeDivision = ? WHERE IdDivisionExterne = ?";
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, nom);
            ps.setString(2, type);
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
