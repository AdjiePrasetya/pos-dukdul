package com.coffeeshop.ui;

import com.coffeeshop.dao.UserDAO;
import com.coffeeshop.model.User;
import com.coffeeshop.util.SessionManager;
import com.coffeeshop.util.UITheme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Module: UI - LoginFrame
 * Login screen for the POS application.
 */
public class LoginFrame extends JFrame {

    private final UserDAO userDAO = new UserDAO();
    private JTextField tfUsername;
    private JPasswordField pfPassword;
    private JLabel lblError;

    public LoginFrame() {
        setTitle("Duduk Dulu POS - Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(420, 560);
        setLocationRelativeTo(null);
        setResizable(false);
        buildUI();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.BG_MAIN);

        // ── Header / Brand area ──────────────────────────────────────────
        JPanel header = new JPanel(new GridBagLayout());
        header.setBackground(UITheme.PRIMARY_DARK);
        header.setPreferredSize(new Dimension(420, 200));

        JPanel brandBox = new JPanel();
        brandBox.setLayout(new BoxLayout(brandBox, BoxLayout.Y_AXIS));
        brandBox.setOpaque(false);

        // Coffee cup emoji as icon substitute
        JLabel lblIcon = new JLabel("☕", SwingConstants.CENTER);
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 52));
        lblIcon.setForeground(UITheme.ACCENT);
        lblIcon.setAlignmentX(CENTER_ALIGNMENT);

        JLabel lblBrand = new JLabel("Duduk Dulu", SwingConstants.CENTER);
        lblBrand.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblBrand.setForeground(Color.WHITE);
        lblBrand.setAlignmentX(CENTER_ALIGNMENT);

        JLabel lblSub = new JLabel("Coffee Shop POS System", SwingConstants.CENTER);
        lblSub.setFont(UITheme.FONT_SMALL);
        lblSub.setForeground(new Color(200, 180, 160));
        lblSub.setAlignmentX(CENTER_ALIGNMENT);

        brandBox.add(lblIcon);
        brandBox.add(Box.createVerticalStrut(6));
        brandBox.add(lblBrand);
        brandBox.add(Box.createVerticalStrut(4));
        brandBox.add(lblSub);

        header.add(brandBox);
        root.add(header, BorderLayout.NORTH);

        // ── Login Form ───────────────────────────────────────────────────
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(UITheme.BG_MAIN);
        formPanel.setBorder(BorderFactory.createEmptyBorder(32, 48, 32, 48));

        JLabel lblTitle = new JLabel("Masuk ke Sistem");
        lblTitle.setFont(UITheme.FONT_HEADING);
        lblTitle.setForeground(UITheme.TEXT_PRIMARY);
        lblTitle.setAlignmentX(LEFT_ALIGNMENT);

        JLabel lblUsernameHint = UITheme.bodyLabel("Username");
        lblUsernameHint.setAlignmentX(LEFT_ALIGNMENT);
        lblUsernameHint.setBorder(BorderFactory.createEmptyBorder(16, 0, 4, 0));

        tfUsername = UITheme.styledField(20);
        tfUsername.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        tfUsername.setAlignmentX(LEFT_ALIGNMENT);

        JLabel lblPassHint = UITheme.bodyLabel("Password");
        lblPassHint.setAlignmentX(LEFT_ALIGNMENT);
        lblPassHint.setBorder(BorderFactory.createEmptyBorder(12, 0, 4, 0));

        pfPassword = UITheme.styledPasswordField(20);
        pfPassword.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        pfPassword.setAlignmentX(LEFT_ALIGNMENT);

        lblError = new JLabel(" ");
        lblError.setFont(UITheme.FONT_SMALL);
        lblError.setForeground(UITheme.DANGER);
        lblError.setAlignmentX(LEFT_ALIGNMENT);
        lblError.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));

        JButton btnLogin = UITheme.primaryButton("  Masuk  ");
        btnLogin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btnLogin.setAlignmentX(LEFT_ALIGNMENT);
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLogin.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));

        formPanel.add(lblTitle);
        formPanel.add(lblUsernameHint);
        formPanel.add(tfUsername);
        formPanel.add(lblPassHint);
        formPanel.add(pfPassword);
        formPanel.add(lblError);
        formPanel.add(Box.createVerticalStrut(16));
        formPanel.add(btnLogin);

        root.add(formPanel, BorderLayout.CENTER);

        // ── Footer ───────────────────────────────────────────────────────
        JPanel footer = new JPanel();
        footer.setBackground(new Color(0xEEE5DA));
        footer.setBorder(BorderFactory.createEmptyBorder(12, 0, 12, 0));
        JLabel footerLbl = new JLabel("© 2025 Duduk Dulu Coffee Shop - Pontianak");
        footerLbl.setFont(UITheme.FONT_SMALL);
        footerLbl.setForeground(UITheme.TEXT_LIGHT);
        footer.add(footerLbl);
        root.add(footer, BorderLayout.SOUTH);

        // ── Actions ──────────────────────────────────────────────────────
        btnLogin.addActionListener(e -> doLogin());
        KeyAdapter enterKey = new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) doLogin();
            }
        };
        tfUsername.addKeyListener(enterKey);
        pfPassword.addKeyListener(enterKey);

        setContentPane(root);
    }

    private void doLogin() {
        String username = tfUsername.getText().trim();
        String password = new String(pfPassword.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            lblError.setText("Username dan password tidak boleh kosong!");
            return;
        }

        User user = userDAO.login(username, password);
        if (user != null) {
            SessionManager.getInstance().login(user);
            dispose();
            new MainFrame().setVisible(true);
        } else {
            lblError.setText("Username atau password salah!");
            pfPassword.setText("");
            tfPassword();
        }
    }

    private void tfPassword() {
        pfPassword.requestFocusInWindow();
    }
}
