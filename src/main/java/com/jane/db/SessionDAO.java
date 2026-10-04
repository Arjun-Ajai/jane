package com.jane.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;

public class SessionDAO {

    public int createSession(String mode, String label) {
        String sql = "INSERT INTO sessions (mode, label, created_at) VALUES (?, ?, ?)";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
             
            pstmt.setString(1, mode);
            pstmt.setString(2, label);
            pstmt.setString(3, LocalDateTime.now().toString());
            
            pstmt.executeUpdate();
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new PersistenceException("Creating session failed, no ID obtained.", null);
                }
            }
        } catch (SQLException e) {
            throw new PersistenceException("Failed to create session", e);
        }
    }
}
