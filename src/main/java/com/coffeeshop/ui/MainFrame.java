package com.coffeeshop.ui;

import com.coffeeshop.util.SessionManager;
import com.coffeeshop.util.UITheme;

import javax.swing.*;
import java.awt.*;

/**
 * Module: UI - MainFrame
 * Main application frame with sidebar navigation.
 * Imports and displays all module panels (Kasir, Menu, Promosi, Laporan, etc.)
 */
public class MainFrame extends JFrame {

    private JPanel contentPanel;
    private CardLayout cardLayout;
    private JLabel lblUserInfo;

    // ── Module Panels (imported here only) ──────────────────────────────
    private KasirPanel kasirPanel;
    private MenuPanel menuPanel;
    private PromosiPanel promosiPanel;
    private LaporanPanel laporanPanel;
    private UserPanel userPanel;
    private DashboardPanel dashboardPanel;

    public MainFrame() {
        setTitle("Kasir Pintar - Duduk Dulu Coffee Shop");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 760);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(1000, 680));
        buildUI();
    }

    private void buildUI() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(UITheme.BG_MAIN);

        // ── Sidebar ──────────────────────────────────────────────────────
        JPanel sidebar = buildSidebar();
        add(sidebar, BorderLayout.WEST);

        // ── Content Area ─────────────────────────────────────────────────
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(UITheme.BG_MAIN);

        // Instantiate all module panels
        dashboardPanel = new DashboardPanel();
        kasirPanel     = new KasirPanel();
        menuPanel      = new MenuPanel();
        promosiPanel   = new PromosiPanel();
        laporanPanel   = new LaporanPanel();
        userPanel      = new UserPanel();

        contentPanel.add(dashboardPanel, "dashboard");
        contentPanel.add(kasirPanel,     "kasir");
        contentPanel.add(menuPanel,      "menu");
        contentPanel.add(promosiPanel,   "promosi");
        contentPanel.add(laporanPanel,   "laporan");
        contentPanel.add(userPanel,      "user");

        add(contentPanel, BorderLayout.CENTER);

        // Show dashboard by default
        showPanel("dashboard");
    }

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(UITheme.BG_SIDEBAR);
        sidebar.setPreferredSize(new Dimension(210, 0));

        // Brand
        JPanel brandArea = new JPanel();
        brandArea.setLayout(new BoxLayout(brandArea, BoxLayout.Y_AXIS));
        brandArea.setBackground(UITheme.PRIMARY_DARK);
        brandArea.setBorder(BorderFactory.createEmptyBorder(20, 16, 20, 16));
        brandArea.setAlignmentX(LEFT_ALIGNMENT);
        brandArea.setMaximumSize(new Dimension(210, 100));

        JLabel lblLogo = new JLabel("☕ Duduk Dulu");
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblLogo.setForeground(UITheme.ACCENT);
        lblLogo.setAlignmentX(LEFT_ALIGNMENT);

        JLabel lblPOS = new JLabel("Kasir Pintar POS");
        lblPOS.setFont(UITheme.FONT_SMALL);
        lblPOS.setForeground(new Color(180, 160, 140));
        lblPOS.setAlignmentX(LEFT_ALIGNMENT);

        brandArea.add(lblLogo);
        brandArea.add(Box.createVerticalStrut(4));
        brandArea.add(lblPOS);
        sidebar.add(brandArea);

        // Divider
        sidebar.add(Box.createVerticalStrut(8));

        // Nav items
        sidebar.add(navItem("🏠", "Dashboard", "dashboard"));

        if (!SessionManager.getInstance().isManager()) {
            sidebar.add(navItem("🛒", "Kasir / Transaksi", "kasir"));
            sidebar.add(navItem("☕", "Menu & Stok", "menu"));
            sidebar.add(navItem("🎁", "Promosi & Diskon", "promosi"));
            sidebar.add(navItem("📊", "Laporan", "laporan"));
        }

        // Admin only menu
        if (SessionManager.getInstance().isAdmin()) {
            sidebar.add(navSeparator("ADMIN"));
            sidebar.add(navItem("👤", "Manajemen User", "user"));
        }

        sidebar.add(Box.createVerticalGlue());

        // User info area
        JPanel userArea = new JPanel();
        userArea.setLayout(new BoxLayout(userArea, BoxLayout.Y_AXIS));
        userArea.setBackground(UITheme.PRIMARY_DARK);
        userArea.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        userArea.setMaximumSize(new Dimension(210, 100));
        userArea.setAlignmentX(LEFT_ALIGNMENT);

        lblUserInfo = new JLabel(SessionManager.getInstance().getCurrentUser().getNama());
        lblUserInfo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblUserInfo.setForeground(Color.WHITE);
        lblUserInfo.setAlignmentX(LEFT_ALIGNMENT);

        JLabel lblRole = new JLabel(SessionManager.getInstance().getCurrentUser().getRole().toUpperCase());
        lblRole.setFont(UITheme.FONT_SMALL);
        lblRole.setForeground(UITheme.ACCENT);
        lblRole.setAlignmentX(LEFT_ALIGNMENT);

        JButton btnLogout = new JButton("Keluar");
        btnLogout.setFont(UITheme.FONT_SMALL);
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setBackground(UITheme.DANGER);
        btnLogout.setBorderPainted(false);
        btnLogout.setFocusPainted(false);
        btnLogout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLogout.setAlignmentX(LEFT_ALIGNMENT);
        btnLogout.addActionListener(e -> doLogout());

        userArea.add(lblUserInfo);
        userArea.add(lblRole);
        userArea.add(Box.createVerticalStrut(8));
        userArea.add(btnLogout);

        sidebar.add(userArea);
        return sidebar;
    }

    private JButton navItem(String icon, String label, String panel) {
        JButton btn = new JButton(icon + "  " + label);
        btn.setFont(UITheme.FONT_BODY);
        btn.setForeground(new Color(200, 180, 160));
        btn.setBackground(UITheme.BG_SIDEBAR);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        btn.setMaximumSize(new Dimension(210, 44));
        btn.setAlignmentX(LEFT_ALIGNMENT);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> showPanel(panel));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent ev) {
                btn.setBackground(new Color(0x4A3728));
                btn.setForeground(UITheme.ACCENT);
            }
            public void mouseExited(java.awt.event.MouseEvent ev) {
                btn.setBackground(UITheme.BG_SIDEBAR);
                btn.setForeground(new Color(200, 180, 160));
            }
        });
        return btn;
    }

    private JLabel navSeparator(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lbl.setForeground(new Color(100, 80, 60));
        lbl.setBorder(BorderFactory.createEmptyBorder(12, 16, 4, 16));
        lbl.setMaximumSize(new Dimension(210, 30));
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        return lbl;
    }

    public void showPanel(String name) {
        cardLayout.show(contentPanel, name);
        if ("dashboard".equals(name)) dashboardPanel.refresh();
        if ("laporan".equals(name)) laporanPanel.refresh();
    }

    private void doLogout() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Yakin ingin keluar dari sistem?", "Konfirmasi Keluar",
            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            SessionManager.getInstance().logout();
            dispose();
            new LoginFrame().setVisible(true);
        }
    }
}
