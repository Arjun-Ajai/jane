package com.jane.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;

public class GenerationDAO {

    public int insertGeneration(int sessionId, int genNumber, double bestFitness, double avgFitness) {
        String sql = "INSERT INTO generations (session_id, gen_number, best_fitness, avg_fitness, created_at) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
             
            pstmt.setInt(1, sessionId);
            pstmt.setInt(2, genNumber);
            pstmt.setDouble(3, bestFitness);
            pstmt.setDouble(4, avgFitness);
            pstmt.setString(5, LocalDateTime.now().toString());
            
            pstmt.executeUpdate();
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new PersistenceException("Creating generation failed, no ID obtained.", null);
                }
            }
        } catch (SQLException e) {
            throw new PersistenceException("Failed to insert generation", e);
        }
    }
}
