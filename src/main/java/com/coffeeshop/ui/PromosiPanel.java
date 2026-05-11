package com.coffeeshop.ui;

import com.coffeeshop.dao.PromosiDAO;
import com.coffeeshop.model.Promosi;
import com.coffeeshop.util.SessionManager;
import com.coffeeshop.util.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Module: UI - PromosiPanel
 * Promotion management: CRUD for promo codes / discounts.
 * This is a key differentiating feature of Duduk Dulu POS.
 */
public class PromosiPanel extends JPanel {

    private final PromosiDAO promosiDAO = new PromosiDAO();
    private DefaultTableModel tableModel;
    private JTable table;

    public PromosiPanel() {
        setLayout(new BorderLayout());
        setBackground(UITheme.BG_MAIN);
        buildUI();
    }

    private void buildUI() {
        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.BG_MAIN);
        header.setBorder(BorderFactory.createEmptyBorder(20, 28, 12, 28));

        JPanel headerLeft = new JPanel();
        headerLeft.setLayout(new BoxLayout(headerLeft, BoxLayout.Y_AXIS));
        headerLeft.setOpaque(false);
        JLabel lblTitle = UITheme.titleLabel("🎁 Promosi & Diskon");
        JLabel lblSub = UITheme.bodyLabel("Fitur unggulan: manajemen kode promo untuk meningkatkan penjualan");
        lblSub.setForeground(UITheme.TEXT_LIGHT);
        headerLeft.add(lblTitle);
        headerLeft.add(lblSub);
        header.add(headerLeft, BorderLayout.WEST);

        JPanel headerRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        headerRight.setOpaque(false);
        JButton btnRefresh = UITheme.ghostButton("🔄 Refresh");
        btnRefresh.addActionListener(e -> loadData());
        headerRight.add(btnRefresh);
        if (SessionManager.getInstance().isAdmin()) {
            JButton btnAdd = UITheme.primaryButton("+ Buat Promo Baru");
            btnAdd.addActionListener(e -> showFormDialog(null));
            headerRight.add(btnAdd);
        }
        header.add(headerRight, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Info card
        JPanel infoCard = new JPanel(new GridLayout(1, 3, 12, 0));
        infoCard.setBackground(UITheme.BG_MAIN);
        infoCard.setBorder(BorderFactory.createEmptyBorder(0, 28, 12, 28));
        infoCard.add(infoChip("💡 Tipe: Persen (%)", "Diskon berdasarkan persentase dari total", UITheme.INFO));
        infoCard.add(infoChip("💰 Tipe: Nominal (Rp)", "Potongan langsung sejumlah rupiah", UITheme.SUCCESS));
        infoCard.add(infoChip("📅 Berbasis Periode", "Promo hanya aktif pada rentang tanggal tertentu", UITheme.ACCENT));
        add(infoCard, BorderLayout.CENTER);

        // Table
        String[] cols = {"ID", "Kode", "Nama Promo", "Tipe", "Nilai", "Min. Transaksi", "Berlaku Hingga", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        UITheme.styleTable(table);
        table.getColumnModel().getColumn(0).setMaxWidth(40);
        table.getColumnModel().getColumn(1).setPreferredWidth(90);
        table.getColumnModel().getColumn(2).setPreferredWidth(180);
        table.getColumnModel().getColumn(3).setPreferredWidth(70);
        table.getColumnModel().getColumn(4).setPreferredWidth(90);
        table.getColumnModel().getColumn(5).setPreferredWidth(110);
        table.getColumnModel().getColumn(6).setPreferredWidth(110);
        table.getColumnModel().getColumn(7).setPreferredWidth(70);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder(0, 28, 0, 28));

        JPanel tableAndActions = new JPanel(new BorderLayout());
        tableAndActions.setBackground(UITheme.BG_MAIN);
        tableAndActions.add(scroll, BorderLayout.CENTER);

        // Action bar
        if (SessionManager.getInstance().isAdmin()) {
            JPanel actionBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 12));
            actionBar.setBackground(UITheme.BG_MAIN);
            actionBar.setBorder(BorderFactory.createEmptyBorder(0, 20, 8, 20));
            JButton btnEdit   = UITheme.ghostButton("✏ Edit Promo");
            JButton btnToggle = UITheme.accentButton("🔄 Aktif/Nonaktif");
            JButton btnDelete = UITheme.dangerButton("🗑 Hapus");
            btnEdit.addActionListener(e -> editSelected());
            btnToggle.addActionListener(e -> toggleSelected());
            btnDelete.addActionListener(e -> deleteSelected());
            actionBar.add(btnEdit);
            actionBar.add(btnToggle);
            actionBar.add(btnDelete);
            tableAndActions.add(actionBar, BorderLayout.SOUTH);
        }

        // Replace center with split
        remove(infoCard);
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, infoCard, tableAndActions);
        split.setDividerLocation(100);
        split.setDividerSize(4);
        split.setBorder(null);
        add(split, BorderLayout.CENTER);

        loadData();
    }

    private JPanel infoChip(String title, String desc, Color color) {
        JPanel chip = new JPanel();
        chip.setLayout(new BoxLayout(chip, BoxLayout.Y_AXIS));
        chip.setBackground(UITheme.BG_CARD);
        chip.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 4, 0, 0, color),
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
            )
        ));
        JLabel t = new JLabel(title);
        t.setFont(new Font("Segoe UI", Font.BOLD, 12));
        t.setForeground(color);
        JLabel d = new JLabel("<html>" + desc + "</html>");
        d.setFont(UITheme.FONT_SMALL);
        d.setForeground(UITheme.TEXT_LIGHT);
        chip.add(t);
        chip.add(d);
        return chip;
    }

    private void loadData() {
        tableModel.setRowCount(0);
        List<Promosi> list = promosiDAO.findAll();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        for (Promosi p : list) {
            String nilai = "persen".equals(p.getTipe()) ?
                p.getNilai() + "%" : UITheme.formatRupiah(p.getNilai());
            String hingga = p.getTglSelesai() != null ? p.getTglSelesai().format(fmt) : "-";
            String status = p.isValid() ? "✅ Aktif" : (p.isAktif() ? "⏰ Belum/Lewat" : "❌ Nonaktif");
            tableModel.addRow(new Object[]{
                p.getId(), p.getKode(), p.getNama(), p.getTipe(),
                nilai, UITheme.formatRupiah(p.getMinTransaksi()), hingga, status
            });
        }
    }

    private Promosi getSelectedPromosi() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Pilih promo terlebih dahulu!"); return null; }
        int id = (int) tableModel.getValueAt(row, 0);
        return promosiDAO.findAll().stream().filter(p -> p.getId() == id).findFirst().orElse(null);
    }

    private void editSelected() {
        Promosi p = getSelectedPromosi();
        if (p != null) showFormDialog(p);
    }

    private void toggleSelected() {
        Promosi p = getSelectedPromosi();
        if (p == null) return;
        p.setAktif(!p.isAktif());
        if (promosiDAO.update(p)) {
            JOptionPane.showMessageDialog(this,
                "Promo " + (p.isAktif() ? "diaktifkan." : "dinonaktifkan."));
            loadData();
        }
    }

    private void deleteSelected() {
        Promosi p = getSelectedPromosi();
        if (p == null) return;
        int ok = JOptionPane.showConfirmDialog(this,
            "Hapus promo '" + p.getNama() + "'?", "Konfirmasi Hapus",
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok == JOptionPane.YES_OPTION && promosiDAO.delete(p.getId())) {
            JOptionPane.showMessageDialog(this, "Promo berhasil dihapus.");
            loadData();
        }
    }

    private void showFormDialog(Promosi existing) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
            existing == null ? "Buat Promo Baru" : "Edit Promo", true);
        dialog.setSize(440, 520);
        dialog.setLocationRelativeTo(this);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UITheme.BG_MAIN);
        form.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 4, 5, 4);
        gbc.weightx = 1;

        JTextField tfKode     = UITheme.styledField(16);
        JTextField tfNama     = UITheme.styledField(16);
        JComboBox<String> cbTipe = new JComboBox<>(new String[]{"persen", "nominal"});
        cbTipe.setFont(UITheme.FONT_BODY);
        JTextField tfNilai    = UITheme.styledField(16);
        JTextField tfMinTrx   = UITheme.styledField(16);
        JTextField tfTglMulai = UITheme.styledField(16);
        JTextField tfTglAkhir = UITheme.styledField(16);
        JCheckBox cbAktif     = new JCheckBox("Aktif"); cbAktif.setFont(UITheme.FONT_BODY); cbAktif.setBackground(UITheme.BG_MAIN); cbAktif.setSelected(true);
        JTextArea taDeskripsi = new JTextArea(3, 16); taDeskripsi.setFont(UITheme.FONT_BODY); taDeskripsi.setLineWrap(true);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        if (existing != null) {
            tfKode.setText(existing.getKode());
            tfNama.setText(existing.getNama());
            cbTipe.setSelectedItem(existing.getTipe());
            tfNilai.setText(String.valueOf(existing.getNilai()));
            tfMinTrx.setText(String.valueOf((int)existing.getMinTransaksi()));
            if (existing.getTglMulai() != null) tfTglMulai.setText(existing.getTglMulai().format(fmt));
            if (existing.getTglSelesai() != null) tfTglAkhir.setText(existing.getTglSelesai().format(fmt));
            cbAktif.setSelected(existing.isAktif());
            if (existing.getDeskripsi() != null) taDeskripsi.setText(existing.getDeskripsi());
        } else {
            tfTglMulai.setText(LocalDate.now().format(fmt));
            tfTglAkhir.setText(LocalDate.now().plusMonths(1).format(fmt));
        }

        int r = 0;
        addRow(form, gbc, r++, "Kode Promo*", tfKode);
        addRow(form, gbc, r++, "Nama Promo*", tfNama);
        addRow(form, gbc, r++, "Tipe Diskon*", cbTipe);
        addRow(form, gbc, r++, "Nilai*", tfNilai);
        addRow(form, gbc, r++, "Min. Transaksi (Rp)", tfMinTrx);
        addRow(form, gbc, r++, "Tgl Mulai (yyyy-MM-dd)", tfTglMulai);
        addRow(form, gbc, r++, "Tgl Akhir (yyyy-MM-dd)", tfTglAkhir);
        gbc.gridy = r++; gbc.gridx = 0; form.add(cbAktif, gbc);
        addRow(form, gbc, r, "Deskripsi", new JScrollPane(taDeskripsi));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnRow.setBackground(UITheme.BG_MAIN);
        JButton btnSave = UITheme.primaryButton("Simpan");
        JButton btnCancel = UITheme.ghostButton("Batal");
        btnCancel.addActionListener(e -> dialog.dispose());
        btnSave.addActionListener(e -> {
            try {
                Promosi p = existing != null ? existing : new Promosi();
                p.setKode(tfKode.getText().trim().toUpperCase());
                p.setNama(tfNama.getText().trim());
                p.setTipe((String) cbTipe.getSelectedItem());
                p.setNilai(Double.parseDouble(tfNilai.getText().trim()));
                p.setMinTransaksi(tfMinTrx.getText().trim().isEmpty() ? 0 :
                    Double.parseDouble(tfMinTrx.getText().trim()));
                if (!tfTglMulai.getText().trim().isEmpty())
                    p.setTglMulai(LocalDate.parse(tfTglMulai.getText().trim(), fmt));
                if (!tfTglAkhir.getText().trim().isEmpty())
                    p.setTglSelesai(LocalDate.parse(tfTglAkhir.getText().trim(), fmt));
                p.setAktif(cbAktif.isSelected());
                p.setDeskripsi(taDeskripsi.getText().trim());

                if (p.getKode().isEmpty() || p.getNama().isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Kode dan nama promo wajib diisi!");
                    return;
                }
                boolean ok = existing == null ? promosiDAO.save(p) : promosiDAO.update(p);
                if (ok) {
                    JOptionPane.showMessageDialog(dialog, "Promo berhasil disimpan!");
                    dialog.dispose();
                    loadData();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Data tidak valid: " + ex.getMessage());
            }
        });
        btnRow.add(btnCancel); btnRow.add(btnSave);

        dialog.setLayout(new BorderLayout());
        dialog.add(new JScrollPane(form), BorderLayout.CENTER);
        dialog.add(btnRow, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void addRow(JPanel form, GridBagConstraints gbc, int row, String label, JComponent field) {
        gbc.gridy = row; gbc.gridx = 0; gbc.weightx = 0.35; form.add(UITheme.bodyLabel(label), gbc);
        gbc.gridx = 1; gbc.weightx = 0.65; form.add(field, gbc);
    }
}
