package com.technolab.istime.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {

    private static final String URL = "jdbc:sqlite:istime.db";
    private static Connection connection = null;

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("org.sqlite.JDBC");
                connection = DriverManager.getConnection(URL);
                createTables();
            } catch (ClassNotFoundException e) {
                throw new SQLException("Driver SQLite introuvable.", e);
            }
        }
        return connection;
    }

    private static void createTables() throws SQLException {
        try (Statement stmt = connection.createStatement()) {

            stmt.execute("CREATE TABLE IF NOT EXISTS cours (id INTEGER PRIMARY KEY AUTOINCREMENT, filiere TEXT NOT NULL, professeur TEXT NOT NULL, matiere TEXT NOT NULL, salle TEXT NOT NULL, jour TEXT NOT NULL, creneau TEXT NOT NULL);");
            stmt.execute("CREATE TABLE IF NOT EXISTS professeurs (id INTEGER PRIMARY KEY AUTOINCREMENT, nom TEXT NOT NULL UNIQUE);");
            stmt.execute("CREATE TABLE IF NOT EXISTS matieres (id INTEGER PRIMARY KEY AUTOINCREMENT, nom TEXT NOT NULL UNIQUE);");
            stmt.execute("CREATE TABLE IF NOT EXISTS users (username TEXT PRIMARY KEY, password TEXT NOT NULL, role TEXT NOT NULL);");
            stmt.execute("CREATE TABLE IF NOT EXISTS filieres (id INTEGER PRIMARY KEY AUTOINCREMENT, nom TEXT NOT NULL UNIQUE);");

            stmt.execute("INSERT OR IGNORE INTO users (username, password, role) VALUES ('david', 'D@v3ntps', 'ADMIN'), ('user', '1234', 'GUEST')");
            
            String[] filieres = {
                "DUT 1 - Informatique de Gestion", "DUT 2 - Informatique de Gestion", 
                "DUT 1 - Finance Comptabilité", "DUT 2 - Finance Comptabilité", 
                "DUT 1 - GRH", "DUT 2 - GRH", "DUT 1 - GLT", "DUT 2 - GLT", 
                "DUT 1 - Marketing", "DUT 2 - Marketing",
                "BT1", "BT2", 
                "Licence - Génie Logiciel", "Licence - DataScience", "Licence - Système et Réseaux",
                "Licence - GRH", "Licence - GEA", "Licence - Logistique", "Licence - Marketing",
                "Licence - Génie Civil", "Licence - Géologie / Mines", "Licence - Agro-alimentaire"
            };
            
            for (String f : filieres) {
                stmt.execute("INSERT OR IGNORE INTO filieres (nom) VALUES ('" + f.replace("'", "''") + "')");
            }
            
        }
    }
}
