package com.technolab.istime.dao;

import com.technolab.istime.model.Cours;
import com.technolab.istime.utils.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class CoursDAO {

    public boolean verifierConflitSalle(String salle, String jour, String creneau) throws SQLException {
        String sql = "SELECT COUNT(*) FROM cours WHERE salle = ? AND jour = ? AND creneau = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, salle);
            pstmt.setString(2, jour);
            pstmt.setString(3, creneau);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        }
        return false;
    }

    public boolean verifierConflitProf(String prof, String jour, String creneau) throws SQLException {
        String sql = "SELECT COUNT(*) FROM cours WHERE professeur = ? AND jour = ? AND creneau = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, prof);
            pstmt.setString(2, jour);
            pstmt.setString(3, creneau);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        }
        return false;
    }

    public void ajouterCours(Cours cours) throws SQLException {
        String sql = "INSERT INTO cours(filiere, professeur, matiere, salle, jour, creneau) VALUES(?,?,?,?,?,?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, cours.getFiliere());
            pstmt.setString(2, cours.getProfesseur());
            pstmt.setString(3, cours.getMatiere());
            pstmt.setString(4, cours.getSalle());
            pstmt.setString(5, cours.getJour());
            pstmt.setString(6, cours.getCreneau());
            pstmt.executeUpdate();
        }
    }

    public void supprimerCours(int id) throws SQLException {
        String sql = "DELETE FROM cours WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    public void modifierCours(Cours cours) throws SQLException {
        String sql = "UPDATE cours SET filiere=?, professeur=?, matiere=?, salle=?, jour=?, creneau=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, cours.getFiliere());
            pstmt.setString(2, cours.getProfesseur());
            pstmt.setString(3, cours.getMatiere());
            pstmt.setString(4, cours.getSalle());
            pstmt.setString(5, cours.getJour());
            pstmt.setString(6, cours.getCreneau());
            pstmt.setInt(7, cours.getId());
            pstmt.executeUpdate();
        }
    }

    public List<Cours> listerCoursParJour(String jour) throws SQLException {
        List<Cours> liste = new ArrayList<>();
        String sql = "SELECT * FROM cours WHERE jour = ? ORDER BY salle, creneau";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, jour);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) liste.add(mapResultToCours(rs));
        }
        return liste;
    }
    
    public List<Cours> listerCoursParFiliere(String filiere) throws SQLException {
        List<Cours> liste = new ArrayList<>();
        String sql = "SELECT * FROM cours WHERE filiere = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, filiere);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) liste.add(mapResultToCours(rs));
        }
        return liste;
    }

    public List<Cours> rechercherGlobal(String type, String valeur) throws SQLException {
        List<Cours> liste = new ArrayList<>();
        String col = "professeur".equals(type) ? "professeur" : "salle";
        
        String sql = "SELECT * FROM cours WHERE " + col + " LIKE ? " +
                     "ORDER BY CASE jour " +
                     "WHEN 'Lundi' THEN 1 WHEN 'Mardi' THEN 2 WHEN 'Mercredi' THEN 3 " +
                     "WHEN 'Jeudi' THEN 4 WHEN 'Vendredi' THEN 5 WHEN 'Samedi' THEN 6 END, creneau";
                     
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + valeur + "%");
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) liste.add(mapResultToCours(rs));
        }
        return liste;
    }
    
    public List<String> getListe(String table) throws SQLException {
        List<String> liste = new ArrayList<>();
        String sql = "SELECT nom FROM " + table + " ORDER BY nom";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while(rs.next()) liste.add(rs.getString("nom"));
        }
        return liste;
    }

    public void ajouterRef(String table, String nom) throws SQLException {
        String sql = "INSERT INTO " + table + " (nom) VALUES (?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nom);
            pstmt.executeUpdate();
        }
    }

    public void supprimerRef(String table, String nom) throws SQLException {
        String sql = "DELETE FROM " + table + " WHERE nom = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nom);
            pstmt.executeUpdate();
        }
    }

    public Map<String, Integer> getVolumeHoraire() throws SQLException {
        Map<String, Integer> stats = new HashMap<>();
        String sql = "SELECT professeur, COUNT(*) as nb_cours FROM cours GROUP BY professeur";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                stats.put(rs.getString("professeur"), rs.getInt("nb_cours") * 2);
            }
        }
        return stats;
    }

    private Cours mapResultToCours(ResultSet rs) throws SQLException {
        return new Cours(
            rs.getInt("id"), rs.getString("filiere"), rs.getString("professeur"),
            rs.getString("matiere"), rs.getString("salle"), rs.getString("jour"),
            rs.getString("creneau")
        );
    }

    // AUTHENTIFICATION
    public String verifierLogin(String user, String pass) throws SQLException {
        String sql = "SELECT role FROM users WHERE username = ? AND password = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user);
            pstmt.setString(2, pass);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("role"); // Retourne "ADMIN" ou "GUEST"
            }
        }
        return null; // Échec
    }

    public List<String> getSallesOccupees(String jour, String creneau) throws SQLException {
        List<String> salles = new ArrayList<>();
        String sql = "SELECT salle FROM cours WHERE jour = ? AND creneau = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, jour);
            pstmt.setString(2, creneau);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                salles.add(rs.getString("salle"));
            }
        }
        return salles;
    }

    public void viderPlanning() throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM cours");
            stmt.executeUpdate("DELETE FROM sqlite_sequence WHERE name='cours'");
        }
    }

    public String getProfDansSalle(String salle, String jour, String creneau) throws SQLException {
        String sql = "SELECT professeur FROM cours WHERE salle = ? AND jour = ? AND creneau = ? LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, salle);
            pstmt.setString(2, jour);
            pstmt.setString(3, creneau);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("professeur");
            }
        }
        return null; // Salle vide
    }

    public String getFilieresEnCommun(String salle, String jour, String creneau) throws SQLException {
        StringBuilder sb = new StringBuilder();
        String sql = "SELECT filiere FROM cours WHERE salle = ? AND jour = ? AND creneau = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, salle);
            pstmt.setString(2, jour);
            pstmt.setString(3, creneau);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                if (sb.length() > 0) sb.append(" + ");
                sb.append(rs.getString("filiere").replace("DUT ", "").replace("Licence ", "L.")); // On raccourcit les noms pour l'affichage
            }
        }
        return sb.toString();
    }
}
