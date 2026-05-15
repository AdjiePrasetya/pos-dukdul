package com.coffeeshop.ui;

import com.coffeeshop.dao.UserDAO;
import com.coffeeshop.model.User;
import com.coffeeshop.util.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Module: UI - UserPanel
 * Admin-only: manage kasir/user accounts.
 */
public class UserPanel extends JPanel {

    private final UserDAO userDAO = new UserDAO();
    private DefaultTableModel tableModel;
    private JTable table;

    public UserPanel() {
        setLayout(new BorderLayout());
        setBackground(UITheme.BG_MAIN);
        buildUI();
    }

    private void buildUI() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.BG_MAIN);
        header.setBorder(BorderFactory.createEmptyBorder(20, 28, 12, 28));
        header.add(UITheme.titleLabel("👤 Manajemen User"), BorderLayout.WEST);

        JPanel headerRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        headerRight.setOpaque(false);
        JButton btnAdd = UITheme.primaryButton("+ Tambah User");
        JButton btnRefresh = UITheme.ghostButton("🔄");
        btnAdd.addActionListener(e -> showFormDialog(null));
        btnRefresh.addActionListener(e -> loadData());
        headerRight.add(btnRefresh);
        headerRight.add(btnAdd);
        header.add(headerRight, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        String[] cols = {"ID", "Username", "Nama Lengkap", "Role"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        UITheme.styleTable(table);
        table.getColumnModel().getColumn(0).setMaxWidth(40);
        table.getColumnModel().getColumn(1).setPreferredWidth(120);
        table.getColumnModel().getColumn(2).setPreferredWidth(180);
        table.getColumnModel().getColumn(3).setPreferredWidth(80);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder(0, 28, 0, 28));
        add(scroll, BorderLayout.CENTER);

        JPanel actionBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 12));
        actionBar.setBackground(UITheme.BG_MAIN);
        actionBar.setBorder(BorderFactory.createEmptyBorder(0, 20, 8, 20));
        JButton btnEdit   = UITheme.ghostButton("✏ Edit");
        JButton btnPass   = UITheme.accentButton("🔑 Ganti Password");
        JButton btnDelete = UITheme.dangerButton("🗑 Hapus");
        btnEdit.addActionListener(e -> editSelected());
        btnPass.addActionListener(e -> gantiPasswordSelected());
        btnDelete.addActionListener(e -> deleteSelected());
        actionBar.add(btnEdit); actionBar.add(btnPass); actionBar.add(btnDelete);
        add(actionBar, BorderLayout.SOUTH);

        loadData();
    }

    private void loadData() {
        tableModel.setRowCount(0);
        for (User u : userDAO.findAll()) {
            tableModel.addRow(new Object[]{u.getId(), u.getUsername(), u.getNama(), u.getRole()});
        }
    }

    private User getSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Pilih user dulu!"); return null; }
        int id = (int) tableModel.getValueAt(row, 0);
        return userDAO.findAll().stream().filter(u -> u.getId() == id).findFirst().orElse(null);
    }

    private void editSelected() {
        User u = getSelected();
        if (u != null) showFormDialog(u);
    }

    private void gantiPasswordSelected() {
        User u = getSelected();
        if (u == null) return;
        JPasswordField pf = new JPasswordField(16);
        int ok = JOptionPane.showConfirmDialog(this, pf,
            "Password baru untuk " + u.getNama() + ":", JOptionPane.OK_CANCEL_OPTION);
        if (ok == JOptionPane.OK_OPTION) {
            String newPass = new String(pf.getPassword()).trim();
            if (newPass.isEmpty()) { JOptionPane.showMessageDialog(this, "Password tidak boleh kosong!"); return; }
            if (userDAO.updatePassword(u.getId(), newPass)) {
                JOptionPane.showMessageDialog(this, "Password berhasil diganti.");
            }
        }
    }

    private void deleteSelected() {
        User u = getSelected();
        if (u == null) return;
        if ("admin".equals(u.getUsername())) {
            JOptionPane.showMessageDialog(this, "Akun admin utama tidak bisa dihapus!");
            return;
        }
        int ok = JOptionPane.showConfirmDialog(this,
            "Hapus user '" + u.getNama() + "'?", "Konfirmasi Hapus",
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok == JOptionPane.YES_OPTION && userDAO.delete(u.getId())) {
            JOptionPane.showMessageDialog(this, "User berhasil dihapus.");
            loadData();
        }
    }

    private void showFormDialog(User existing) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
            existing == null ? "Tambah User Baru" : "Edit User", true);
        dialog.setSize(380, 300);
        dialog.setLocationRelativeTo(this);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UITheme.BG_MAIN);
        form.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 4, 6, 4);
        gbc.weightx = 1;

        JTextField tfUsername = UITheme.styledField(16);
        JTextField tfNama     = UITheme.styledField(16);
        JComboBox<String> cbRole = new JComboBox<>(new String[]{"kasir", "manager", "admin"});
        cbRole.setFont(UITheme.FONT_BODY);
        JPasswordField pfPass = existing == null ? UITheme.styledPasswordField(16) : null;

        if (existing != null) {
            tfUsername.setText(existing.getUsername());
            tfUsername.setEditable(false);
            tfNama.setText(existing.getNama());
            cbRole.setSelectedItem(existing.getRole());
        }

        int r = 0;
        addRow(form, gbc, r++, "Username*", tfUsername);
        addRow(form, gbc, r++, "Nama Lengkap*", tfNama);
        addRow(form, gbc, r++, "Role*", cbRole);
        if (existing == null) addRow(form, gbc, r, "Password*", pfPass);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnRow.setBackground(UITheme.BG_MAIN);
        JButton btnSave   = UITheme.primaryButton("Simpan");
        JButton btnCancel = UITheme.ghostButton("Batal");
        btnCancel.addActionListener(e -> dialog.dispose());
        btnSave.addActionListener(e -> {
            String username = tfUsername.getText().trim();
            String nama = tfNama.getText().trim();
            String role = (String) cbRole.getSelectedItem();
            if (username.isEmpty() || nama.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Username dan nama wajib diisi!");
                return;
            }
            if (existing == null) {
                String pass = new String(pfPass.getPassword()).trim();
                if (pass.isEmpty()) { JOptionPane.showMessageDialog(dialog, "Password wajib diisi!"); return; }
                User u = new User(); u.setUsername(username); u.setNama(nama); u.setRole(role); u.setPassword(pass);
                if (userDAO.save(u)) {
                    JOptionPane.showMessageDialog(dialog, "User berhasil ditambahkan!");
                    dialog.dispose(); loadData();
                }
            } else {
                existing.setNama(nama); existing.setRole(role);
                if (userDAO.update(existing)) {
                    JOptionPane.showMessageDialog(dialog, "User berhasil diperbarui!");
                    dialog.dispose(); loadData();
                }
            }
        });
        btnRow.add(btnCancel); btnRow.add(btnSave);

        dialog.setLayout(new BorderLayout());
        dialog.add(form, BorderLayout.CENTER);
        dialog.add(btnRow, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void addRow(JPanel f, GridBagConstraints gbc, int row, String lbl, JComponent field) {
        gbc.gridy = row; gbc.gridx = 0; gbc.weightx = 0.35; f.add(UITheme.bodyLabel(lbl), gbc);
        gbc.gridx = 1; gbc.weightx = 0.65; f.add(field, gbc);
    }
}
