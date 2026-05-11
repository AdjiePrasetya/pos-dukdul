package com.coffeeshop.main;

import com.coffeeshop.config.DatabaseConfig;
import com.coffeeshop.ui.LoginFrame;

import javax.swing.*;

/**
 * Main entry point for Sistem Kasir Pintar - Coffe Shop Duduk Dulu
 * Architecture: Modular - each module is imported independently
 */
public class Main {
    public static void main(String[] args) {
        // Initialize database
        DatabaseConfig.initialize();

        // Launch UI on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
            new LoginFrame().setVisible(true);
        });
    }
}
