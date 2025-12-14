// SingletonConnection.java
package tn.spring.autoecole.dao;

import java.sql.*;

public class SingletonConnection {
    private static Connection connection;

    private SingletonConnection(){
        try {
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/auto_ecole","root","");
            System.out.println("✓ Connexion à la base de données réussie!");
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("✗ Erreur de connexion: " + e.getMessage());
            throw new RuntimeException("Impossible de se connecter à la base de données", e);
        }
    }

    public static Connection getInstance(){
        if (connection == null)
            new SingletonConnection();
        return connection;
    }
}