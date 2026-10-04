package com.jane;

import com.jane.db.DatabaseManager;
import com.jane.gui.MainFrame;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Bootstrap DB schema on startup
        try {
            DatabaseManager.bootstrap();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to bootstrap database. Persistence features may not work.");
        }
        
        // Launch GUI
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                // fallback to default
            }
            
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
