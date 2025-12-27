package gui;

import javax.swing.*;
import utils.DatabaseInitializer;

/**
 * Main application entry point for GreenVault.
 * Initializes the Look and Feel and launches the login frame.
 */
public class GreenVaultApp {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            UIManager.put("control", UIConstants.BACKGROUND_GRAY);
        } catch (Exception e) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {}
        }

        // Initialize database (creates tables and adds ID column if needed)
        try {
            DatabaseInitializer.initializeDatabase();
        } catch (Exception e) {
            System.err.println("Warning: Database initialization had issues: " + e.getMessage());
            e.printStackTrace();
            // Continue anyway - database might already be initialized
        }

        SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
    }
}

