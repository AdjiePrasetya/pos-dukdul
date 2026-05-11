package com.coffeeshop.ui;

import com.coffeeshop.dao.MenuDAO;
import com.coffeeshop.model.Menu;
import com.coffeeshop.util.SessionManager;
import com.coffeeshop.util.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Module: UI - MenuPanel
 * Menu management: view, add, edit, delete menu items and stock.
 */
public class MenuPanel extends JPanel {

    private final MenuDAO menuDAO = new MenuDAO();
    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField tfSearch;

    private static final String[] KATEGORI_NAMES = {"Kopi", "Non-Kopi", "Makanan", "Paket"};
    private static final int[] KATEGORI_IDS = {1, 2, 3, 4};

    public MenuPanel() {
        setLayout(new BorderLayout());
        setBackground(UITheme.BG_MAIN);
        buildUI();
    }

    private void buildUI() {
        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.BG_MAIN);
        header.setBorder(BorderFactory.createEmptyBorder(20, 28, 12, 28));
        header.add(UITheme.titleLabel("☕ Menu & Stok"), BorderLayout.WEST);

        JPanel headerRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        headerRight.setOpaque(false);
        tfSearch = UITheme.styledField(16);
        tfSearch.setToolTipText("Cari menu...");
        tfSearch.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent e) { loadData(); }
        });
        JButton btnAdd = UITheme.primaryButton("+ Tambah Menu");
        JButton btnRefresh = UITheme.ghostButton("🔄 Refresh");
        btnAdd.addActionListener(e -> showFormDialog(null));
        btnRefresh.addActionListener(e -> loadData());
        headerRight.add(new JLabel("Cari:"));
        headerRight.add(tfSearch);
        headerRight.add(btnRefresh);
        if (SessionManager.getInstance().isAdmin()) headerRight.add(btnAdd);
        header.add(headerRight, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Table
        String[] cols = {"ID", "Kode", "Nama Menu", "Kategori", "Harga", "Stok", "Tersedia"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
            public Class<?> getColumnClass(int c) { return c == 6 ? Boolean.class : Object.class; }
        };
        table = new JTable(tableModel);
        UITheme.styleTable(table);
        table.setAutoCreateRowSorter(true);
        table.getColumnModel().getColumn(0).setMaxWidth(40);
        table.getColumnModel().getColumn(1).setPreferredWidth(70);
        table.getColumnModel().getColumn(2).setPreferredWidth(160);
        table.getColumnModel().getColumn(3).setPreferredWidth(90);
        table.getColumnModel().getColumn(4).setPreferredWidth(100);
        table.getColumnModel().getColumn(5).setPreferredWidth(60);
        table.getColumnModel().getColumn(6).setMaxWidth(70);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder(0, 28, 0, 28));
        add(scroll, BorderLayout.CENTER);

        // Action bar
        if (SessionManager.getInstance().isAdmin()) {
            JPanel actionBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 12));
            actionBar.setBackground(UITheme.BG_MAIN);
            actionBar.setBorder(BorderFactory.createEmptyBorder(0, 20, 8, 20));

            JButton btnEdit       = UITheme.ghostButton("✏ Edit Menu");
            JButton btnUpdateStok = UITheme.accentButton("📦 Update Stok");
            JButton btnDelete     = UITheme.dangerButton("🗑 Hapus");

            btnEdit.addActionListener(e -> editSelected());
            btnUpdateStok.addActionListener(e -> updateStokSelected());
            btnDelete.addActionListener(e -> deleteSelected());

            actionBar.add(btnEdit);
            actionBar.add(btnUpdateStok);
            actionBar.add(btnDelete);
            add(actionBar, BorderLayout.SOUTH);
        }

        loadData();
    }

    private void loadData() {
        tableModel.setRowCount(0);
        String kw = tfSearch.getText().trim();
        List<Menu> menus = kw.isEmpty() ? menuDAO.findAll() : menuDAO.search(kw);
        for (Menu m : menus) {
            tableModel.addRow(new Object[]{
                m.getId(), m.getKode(), m.getNama(), m.getKategoriNama(),
                UITheme.formatRupiah(m.getHarga()), m.getStok(), m.isTersedia()
            });
        }
    }

    private Menu getSelectedMenu() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Pilih menu terlebih dahulu!");
            return null;
        }
        int id = (int) tableModel.getValueAt(table.convertRowIndexToModel(row), 0);
        return menuDAO.findAll().stream().filter(m -> m.getId() == id).findFirst().orElse(null);
    }

    private void editSelected() {
        Menu m = getSelectedMenu();
        if (m != null) showFormDialog(m);
    }

    private void deleteSelected() {
        Menu m = getSelectedMenu();
        if (m == null) return;
        int ok = JOptionPane.showConfirmDialog(this,
            "Hapus menu '" + m.getNama() + "'?", "Konfirmasi Hapus",
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok == JOptionPane.YES_OPTION) {
            if (menuDAO.delete(m.getId())) {
                JOptionPane.showMessageDialog(this, "Menu berhasil dihapus.");
                loadData();
            }
        }
    }

    private void updateStokSelected() {
        Menu m = getSelectedMenu();
        if (m == null) return;
        String input = JOptionPane.showInputDialog(this,
            "Stok saat ini: " + m.getStok() + "\nMasukkan stok baru:", "Update Stok",
            JOptionPane.PLAIN_MESSAGE);
        if (input == null) return;
        try {
            int stokBaru = Integer.parseInt(input.trim());
            m.setStok(stokBaru);
            if (menuDAO.update(m)) {
                JOptionPane.showMessageDialog(this, "Stok berhasil diperbarui.");
                loadData();
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Masukkan angka yang valid!");
        }
    }

    private void showFormDialog(Menu existing) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
            existing == null ? "Tambah Menu Baru" : "Edit Menu", true);
        dialog.setSize(420, 480);
        dialog.setLocationRelativeTo(this);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UITheme.BG_MAIN);
        form.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.weightx = 1;

        JTextField tfKode  = UITheme.styledField(16);
        JTextField tfNama  = UITheme.styledField(16);
        JTextField tfHarga = UITheme.styledField(16);
        JTextField tfStok  = UITheme.styledField(16);
        JTextField tfSatuan = UITheme.styledField(16);
        JComboBox<String> cbKat = new JComboBox<>(KATEGORI_NAMES);
        cbKat.setFont(UITheme.FONT_BODY);
        JCheckBox cbTersedia = new JCheckBox("Tersedia untuk dijual");
        cbTersedia.setFont(UITheme.FONT_BODY);
        cbTersedia.setBackground(UITheme.BG_MAIN);
        cbTersedia.setSelected(true);
        JTextArea taDesc = new JTextArea(3, 16);
        taDesc.setFont(UITheme.FONT_BODY);
        taDesc.setLineWrap(true);

        if (existing != null) {
            tfKode.setText(existing.getKode());
            tfNama.setText(existing.getNama());
            tfHarga.setText(String.valueOf((int) existing.getHarga()));
            tfStok.setText(String.valueOf(existing.getStok()));
            tfSatuan.setText(existing.getSatuan());
            for (int i = 0; i < KATEGORI_IDS.length; i++) {
                if (KATEGORI_IDS[i] == existing.getKategoriId()) cbKat.setSelectedIndex(i);
            }
            cbTersedia.setSelected(existing.isTersedia());
            if (existing.getDeskripsi() != null) taDesc.setText(existing.getDeskripsi());
        }

        addFormRow(form, gbc, 0, "Kode Menu*", tfKode);
        addFormRow(form, gbc, 1, "Nama Menu*", tfNama);
        addFormRow(form, gbc, 2, "Kategori*", cbKat);
        addFormRow(form, gbc, 3, "Harga (Rp)*", tfHarga);
        addFormRow(form, gbc, 4, "Stok Awal*", tfStok);
        addFormRow(form, gbc, 5, "Satuan", tfSatuan);
        gbc.gridy = 6; gbc.gridx = 0; form.add(cbTersedia, gbc);
        addFormRow(form, gbc, 7, "Deskripsi", new JScrollPane(taDesc));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnRow.setBackground(UITheme.BG_MAIN);
        JButton btnSave   = UITheme.primaryButton("Simpan");
        JButton btnCancel = UITheme.ghostButton("Batal");
        btnCancel.addActionListener(e -> dialog.dispose());
        btnSave.addActionListener(e -> {
            try {
                Menu m = existing != null ? existing : new Menu();
                m.setKode(tfKode.getText().trim());
                m.setNama(tfNama.getText().trim());
                m.setKategoriId(KATEGORI_IDS[cbKat.getSelectedIndex()]);
                m.setHarga(Double.parseDouble(tfHarga.getText().trim()));
                m.setStok(Integer.parseInt(tfStok.getText().trim()));
                m.setSatuan(tfSatuan.getText().trim().isEmpty() ? "cup" : tfSatuan.getText().trim());
                m.setTersedia(cbTersedia.isSelected());
                m.setDeskripsi(taDesc.getText().trim());

                if (m.getNama().isEmpty() || m.getKode().isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Kode dan nama wajib diisi!");
                    return;
                }
                boolean ok = existing == null ? menuDAO.save(m) : menuDAO.update(m);
                if (ok) {
                    JOptionPane.showMessageDialog(dialog, "Menu berhasil disimpan!");
                    dialog.dispose();
                    loadData();
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Harga dan stok harus berupa angka!");
            }
        });
        btnRow.add(btnCancel);
        btnRow.add(btnSave);

        dialog.setLayout(new BorderLayout());
        dialog.add(new JScrollPane(form), BorderLayout.CENTER);
        dialog.add(btnRow, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void addFormRow(JPanel form, GridBagConstraints gbc, int row, String label, JComponent field) {
        gbc.gridy = row; gbc.gridx = 0; gbc.weightx = 0.3;
        form.add(UITheme.bodyLabel(label), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        form.add(field, gbc);
    }
}
