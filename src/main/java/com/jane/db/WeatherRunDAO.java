package com.jane.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;

public class WeatherRunDAO {

    public int insertRun(int sessionId, int epoch, double loss) {
        String sql = "INSERT INTO weather_runs (session_id, epoch, loss, created_at) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
             
            pstmt.setInt(1, sessionId);
            pstmt.setInt(2, epoch);
            pstmt.setDouble(3, loss);
            pstmt.setString(4, LocalDateTime.now().toString());
            
            pstmt.executeUpdate();
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new PersistenceException("Creating run failed, no ID obtained.", null);
                }
            }
        } catch (SQLException e) {
            throw new PersistenceException("Failed to insert weather run", e);
        }
    }
    
    public void insertPrediction(int runId, String date, double actualTemp, double predictedTemp) {
        String sql = "INSERT INTO weather_predictions (run_id, date, actual_temp, predicted_temp) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
            pstmt.setInt(1, runId);
            pstmt.setString(2, date);
            pstmt.setDouble(3, actualTemp);
            pstmt.setDouble(4, predictedTemp);
            
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            throw new PersistenceException("Failed to insert weather prediction", e);
        }
    }
}
