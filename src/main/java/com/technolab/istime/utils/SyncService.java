package com.technolab.istime.utils;

import com.technolab.istime.dao.CoursDAO;
import com.technolab.istime.model.Cours;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class SyncService {

    private static final String DB_URL = "jdbc:postgresql://your-Supabase-URL.supabase.com:6543/postgres?sslmode=require";
    private static final String USER = "postgres.Your-Supabase-Username";
    private static final String PASS = "YourSupabase-PassWord";

    public void publierPlanning(String filiere) throws Exception {
        CoursDAO localDao = new CoursDAO();
        List<Cours> coursLocaux = localDao.listerCoursParFiliere(filiere);

        if (coursLocaux.isEmpty()) {
            throw new Exception("Aucun cours à publier pour cette filière.");
        }

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            conn.setAutoCommit(false);

            try {
                String deleteSql = "DELETE FROM cours WHERE filiere = ?";
                try (PreparedStatement pstDel = conn.prepareStatement(deleteSql)) {
                    pstDel.setString(1, filiere);
                    pstDel.executeUpdate();
                }

                String insertSql = "INSERT INTO cours (filiere, professeur, matiere, salle, jour, creneau) VALUES (?, ?, ?, ?, ?, ?)";
                try (PreparedStatement pstIns = conn.prepareStatement(insertSql)) {
                    for (Cours c : coursLocaux) {
                        pstIns.setString(1, c.getFiliere());
                        pstIns.setString(2, c.getProfesseur());
                        pstIns.setString(3, c.getMatiere());
                        pstIns.setString(4, c.getSalle());
                        pstIns.setString(5, c.getJour());
                        pstIns.setString(6, c.getCreneau());
                        pstIns.addBatch();
                    }
                    pstIns.executeBatch();
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw new Exception("Erreur SQL Cloud : " + e.getMessage());
            }
        } catch (SQLException e) {
    e.printStackTrace();
    throw new Exception("Erreur technique : " + e.getMessage());
}
    }
}
