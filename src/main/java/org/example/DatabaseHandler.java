package org.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseHandler {
    private static final String URL = "jdbc:sqlite:schedule.db";

    public static void initializeDatabase() {
        try (Connection conn = DriverManager.getConnection(URL)) {
            if (conn != null) {
                String createTable = "CREATE TABLE IF NOT EXISTS schedules (" +
                        "date TEXT PRIMARY KEY," +
                        "data TEXT NOT NULL" +
                        ");";
                conn.createStatement().execute(createTable);
                System.out.println("Table 'schedules' is ready.");
            }
        } catch (SQLException e) {
            System.out.println("Error during database initialization: " + e.getMessage());
        }
    }

    public static void saveSchedule(String date, String data) {
        String insertSQL = "INSERT OR REPLACE INTO schedules(date, data) VALUES(?, ?)";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
            pstmt.setString(1, date);
            pstmt.setString(2, data);
            pstmt.executeUpdate();
            System.out.println("Schedule for " + date + " saved.");
        } catch (SQLException e) {
            System.out.println("Error saving schedule: " + e.getMessage());
        }
    }

    public static String getSchedule(String date) {
        String querySQL = "SELECT data FROM schedules WHERE date = ?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(querySQL)) {
            pstmt.setString(1, date);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("data");
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving schedule: " + e.getMessage());
        }
        return null;
    }
}
