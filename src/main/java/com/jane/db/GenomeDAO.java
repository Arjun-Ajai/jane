package com.jane.db;

import com.jane.core.NeuralNetwork;
import com.jane.core.activations.Sigmoid; // Assuming sigmoid for now, can make it dynamic
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class GenomeDAO {

    public void insertGenome(int generationId, NeuralNetwork brain, double fitness, boolean isBest) {
        String sql = "INSERT INTO genomes (generation_id, weights_json, fitness, is_best) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
            pstmt.setInt(1, generationId);
            pstmt.setString(2, brain.serializeWeights());
            pstmt.setDouble(3, fitness);
            pstmt.setInt(4, isBest ? 1 : 0);
            
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            throw new PersistenceException("Failed to insert genome", e);
        }
    }

    public NeuralNetwork findBest(int sessionId) {
        // Find the best genome from the latest generation of the given session, or overall best.
        // The prompt asks for "load best brain" for a session. We can just pick the genome with the highest fitness for this session.
        String sql = "SELECT g.weights_json FROM genomes g " +
                     "JOIN generations gen ON g.generation_id = gen.id " +
                     "WHERE gen.session_id = ? " +
                     "ORDER BY g.fitness DESC LIMIT 1";
                     
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
            pstmt.setInt(1, sessionId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String weights = rs.getString("weights_json");
                    // We need to know network topology. Assuming Flappy Bird topology for now
                    // In a more generic setup, topology would be saved too.
                    NeuralNetwork nn = new NeuralNetwork(4, 4, 1, new Sigmoid());
                    nn.deserializeWeights(weights);
                    return nn;
                }
            }
        } catch (SQLException e) {
            throw new PersistenceException("Failed to find best genome", e);
        }
        return null;
    }
}
