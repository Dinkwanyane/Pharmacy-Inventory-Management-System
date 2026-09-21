/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pharmacy.inventory.management.system;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 * @author Dinkwanyane
 */
public class DBConnection {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/pims_db?useSSL=false&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "maetis2201@";
    
    private static Connection connection;

    private DBConnection() {
    }
    
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            } catch (ClassNotFoundException e) {
                throw new SQLException("MySQL JDBC Driver not found on classpath.", e);
            }
        }
        return connection;
    }

    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
        }
    }
}
