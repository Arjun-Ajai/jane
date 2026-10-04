package com.jane.db;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private static final String URL = "jdbc:sqlite:jane.db";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static void bootstrap() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             InputStream is = DatabaseManager.class.getResourceAsStream("/schema.sql")) {
             
            if (is == null) {
                System.err.println("Could not find schema.sql in resources.");
                return;
            }
            
            String schema = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            String[] commands = schema.split(";");
            
            for (String command : commands) {
                if (!command.trim().isEmpty()) {
                    stmt.execute(command);
                }
            }
        } catch (Exception e) {
            throw new PersistenceException("Failed to bootstrap database schema", e);
        }
    }
}
