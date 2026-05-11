package com.coffeeshop.util;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

/**
 * Module: Util - UITheme
 * Centralized theme/color constants and UI factory methods
 * for consistent styling across all frames.
 */
public class UITheme {

    // ── Color Palette (warm coffee shop aesthetic) ──────────────────────
    public static final Color PRIMARY       = new Color(0x6F4E37);   // Coffee brown
    public static final Color PRIMARY_DARK  = new Color(0x4A3728);   // Dark espresso
    public static final Color PRIMARY_LIGHT = new Color(0xA07850);   // Latte
    public static final Color ACCENT        = new Color(0xF5A623);   // Caramel / gold
    public static final Color ACCENT_DARK   = new Color(0xD4891B);   // Dark caramel
    public static final Color SUCCESS       = new Color(0x27AE60);   // Green
    public static final Color DANGER        = new Color(0xE74C3C);   // Red
    public static final Color WARNING       = new Color(0xF39C12);   // Orange
    public static final Color INFO          = new Color(0x2980B9);   // Blue
    public static final Color BG_MAIN       = new Color(0xFAF7F2);   // Warm white
    public static final Color BG_SIDEBAR    = new Color(0x2C1A10);   // Very dark coffee
    public static final Color BG_CARD       = Color.WHITE;
    public static final Color TEXT_PRIMARY  = new Color(0x2C1A10);
    public static final Color TEXT_LIGHT    = new Color(0x9E9E9E);
    public static final Color BORDER_COLOR  = new Color(0xE0D6CC);

    // ── Fonts ────────────────────────────────────────────────────────────
    public static final Font FONT_TITLE   = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font FONT_HEADING = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_BODY    = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL   = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_MONO    = new Font("Consolas", Font.PLAIN, 12);

    // ── Button Factory ───────────────────────────────────────────────────
    public static JButton primaryButton(String text) {
        JButton btn = new JButton(text);
        styleButton(btn, PRIMARY, Color.WHITE);
        return btn;
    }

    public static JButton accentButton(String text) {
        JButton btn = new JButton(text);
        styleButton(btn, ACCENT, Color.WHITE);
        return btn;
    }

    public static JButton dangerButton(String text) {
        JButton btn = new JButton(text);
        styleButton(btn, DANGER, Color.WHITE);
        return btn;
    }

    public static JButton successButton(String text) {
        JButton btn = new JButton(text);
        styleButton(btn, SUCCESS, Color.WHITE);
        return btn;
    }

    public static JButton ghostButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BODY);
        btn.setForeground(PRIMARY);
        btn.setBackground(Color.WHITE);
        btn.setBorder(BorderFactory.createLineBorder(PRIMARY, 1));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width + 20, 36));
        return btn;
    }

    private static void styleButton(JButton btn, Color bg, Color fg) {
        btn.setFont(FONT_BODY);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width + 20, 36));
        // Hover effect
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(bg.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(bg);
            }
        });
    }

    // ── Field Factory ────────────────────────────────────────────────────
    public static JTextField styledField(int columns) {
        JTextField tf = new JTextField(columns);
        tf.setFont(FONT_BODY);
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        return tf;
    }

    public static JPasswordField styledPasswordField(int columns) {
        JPasswordField pf = new JPasswordField(columns);
        pf.setFont(FONT_BODY);
        pf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        return pf;
    }

    // ── Label Factory ────────────────────────────────────────────────────
    public static JLabel titleLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_TITLE);
        lbl.setForeground(TEXT_PRIMARY);
        return lbl;
    }

    public static JLabel headingLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_HEADING);
        lbl.setForeground(TEXT_PRIMARY);
        return lbl;
    }

    public static JLabel bodyLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_BODY);
        lbl.setForeground(TEXT_PRIMARY);
        return lbl;
    }

    // ── Panel Factory ────────────────────────────────────────────────────
    public static JPanel cardPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(BG_CARD);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));
        return panel;
    }

    public static Border cardBorder() {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(12, 12, 12, 12)
        );
    }

    // ── Table styling ────────────────────────────────────────────────────
    public static void styleTable(JTable table) {
        table.setFont(FONT_BODY);
        table.setRowHeight(32);
        table.getTableHeader().setFont(FONT_HEADING);
        table.getTableHeader().setBackground(PRIMARY);
        table.getTableHeader().setForeground(Color.WHITE);
        table.setGridColor(BORDER_COLOR);
        table.setSelectionBackground(new Color(0xA07850).brighter());
        table.setSelectionForeground(Color.WHITE);
        table.setShowGrid(true);
        table.setFillsViewportHeight(true);
    }

    // ── Format currency ──────────────────────────────────────────────────
    public static String formatRupiah(double amount) {
        return "Rp " + String.format("%,.0f", amount);
    }
}
