package com.coffeeshop.ui;

import com.coffeeshop.dao.MenuDAO;
import com.coffeeshop.dao.PromosiDAO;
import com.coffeeshop.dao.TransaksiDAO;
import com.coffeeshop.model.DetailTransaksi;
import com.coffeeshop.model.Menu;
import com.coffeeshop.model.Promosi;
import com.coffeeshop.model.Transaksi;
import com.coffeeshop.util.PrintUtil;
import com.coffeeshop.util.SessionManager;
import com.coffeeshop.util.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Module: UI - KasirPanel
 * Main cashier / transaction screen.
 * Features: browse menu, add to cart, apply promo, process payment.
 */
public class KasirPanel extends JPanel {

    private final MenuDAO menuDAO       = new MenuDAO();
    private final PromosiDAO promosiDAO = new PromosiDAO();
    private final TransaksiDAO trxDAO   = new TransaksiDAO();

    private Transaksi currentTrx;
    private DefaultTableModel cartModel;
    private JTextField tfSearch, tfPromoKode;
    private JLabel lblSubtotal, lblDiskon, lblTotal, lblPromoInfo;
    private JComboBox<String> cbKategori;
    private JPanel menuGrid;
    private JComboBox<String> cbMetodeBayar;

    public KasirPanel() {
        setLayout(new BorderLayout());
        setBackground(UITheme.BG_MAIN);
        newTransaction();
        buildUI();
    }

    private void newTransaction() {
        currentTrx = new Transaksi();
        currentTrx.setKasirId(SessionManager.getInstance().getCurrentUser().getId());
        currentTrx.setKasirNama(SessionManager.getInstance().getCurrentUser().getNama());
        currentTrx.setNoTransaksi(trxDAO.generateNoTransaksi());
    }

    private void buildUI() {
        // ── Top header ───────────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.BG_MAIN);
        header.setBorder(BorderFactory.createEmptyBorder(20, 28, 8, 28));

        JLabel lblTitle = UITheme.titleLabel("🛒 Kasir / Transaksi");
        JLabel lblNo = UITheme.bodyLabel("No: " + currentTrx.getNoTransaksi());
        lblNo.setForeground(UITheme.TEXT_LIGHT);

        header.add(lblTitle, BorderLayout.WEST);
        header.add(lblNo, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // ── Split pane: LEFT = menu, RIGHT = cart ─────────────────────
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(620);
        splitPane.setDividerSize(6);
        splitPane.setBorder(BorderFactory.createEmptyBorder(0, 16, 16, 16));
        splitPane.setResizeWeight(0.6);

        splitPane.setLeftComponent(buildMenuPanel());
        splitPane.setRightComponent(buildCartPanel());
        add(splitPane, BorderLayout.CENTER);
    }

    // ── Left: Menu Browser ────────────────────────────────────────────
    private JPanel buildMenuPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(UITheme.BG_MAIN);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 6));

        // Search + filter bar
        JPanel filterBar = new JPanel(new BorderLayout(8, 0));
        filterBar.setOpaque(false);

        tfSearch = UITheme.styledField(20);
        tfSearch.setToolTipText("Cari nama menu atau kode...");
        tfSearch.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent e) { loadMenu(); }
        });

        String[] cats = {"Semua", "Kopi", "Non-Kopi", "Makanan", "Paket"};
        cbKategori = new JComboBox<>(cats);
        cbKategori.setFont(UITheme.FONT_BODY);
        cbKategori.setPreferredSize(new Dimension(120, 36));
        cbKategori.addActionListener(e -> loadMenu());

        filterBar.add(tfSearch, BorderLayout.CENTER);
        filterBar.add(cbKategori, BorderLayout.EAST);

        panel.add(filterBar, BorderLayout.NORTH);

        // Menu grid
        menuGrid = new JPanel(new GridLayout(0, 3, 10, 10));
        menuGrid.setBackground(UITheme.BG_MAIN);

        JScrollPane scroll = new JScrollPane(menuGrid);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(UITheme.BG_MAIN);
        scroll.getVerticalScrollBar().setUnitIncrement(20);
        panel.add(scroll, BorderLayout.CENTER);

        loadMenu();
        return panel;
    }

    private void loadMenu() {
        menuGrid.removeAll();
        List<Menu> menus;
        String keyword = tfSearch.getText().trim();
        String cat = (String) cbKategori.getSelectedItem();

        if (!keyword.isEmpty()) {
            menus = menuDAO.search(keyword);
        } else if (cat != null && !cat.equals("Semua")) {
            int catId = switch (cat) {
                case "Kopi" -> 1;
                case "Non-Kopi" -> 2;
                case "Makanan" -> 3;
                case "Paket" -> 4;
                default -> 0;
            };
            menus = menuDAO.findByKategori(catId);
        } else {
            menus = menuDAO.findAvailable();
        }

        for (Menu m : menus) {
            menuGrid.add(buildMenuCard(m));
        }

        if (menus.isEmpty()) {
            JLabel lbl = UITheme.bodyLabel("Tidak ada menu ditemukan.");
            lbl.setForeground(UITheme.TEXT_LIGHT);
            menuGrid.add(lbl);
        }

        menuGrid.revalidate();
        menuGrid.repaint();
    }

    private JPanel buildMenuCard(Menu menu) {
        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setBackground(UITheme.BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Category badge color
        Color badgeColor = switch (menu.getKategoriId()) {
            case 1 -> UITheme.PRIMARY;
            case 2 -> UITheme.INFO;
            case 3 -> UITheme.SUCCESS;
            default -> UITheme.ACCENT;
        };

        JLabel lblKat = new JLabel(menu.getKategoriNama() != null ? menu.getKategoriNama() : "");
        lblKat.setFont(new Font("Segoe UI", Font.BOLD, 9));
        lblKat.setForeground(Color.WHITE);
        lblKat.setBackground(badgeColor);
        lblKat.setOpaque(true);
        lblKat.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));

        JLabel lblName = new JLabel("<html><b>" + menu.getNama() + "</b></html>");
        lblName.setFont(UITheme.FONT_BODY);
        lblName.setForeground(UITheme.TEXT_PRIMARY);

        JLabel lblHarga = new JLabel(UITheme.formatRupiah(menu.getHarga()));
        lblHarga.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblHarga.setForeground(UITheme.PRIMARY);

        JLabel lblStok = new JLabel("Stok: " + menu.getStok());
        lblStok.setFont(UITheme.FONT_SMALL);
        lblStok.setForeground(menu.getStok() < 5 ? UITheme.DANGER : UITheme.TEXT_LIGHT);

        JPanel infoPanel = new JPanel(new GridLayout(3, 1, 0, 2));
        infoPanel.setOpaque(false);
        infoPanel.add(lblName);
        infoPanel.add(lblHarga);
        infoPanel.add(lblStok);

        card.add(lblKat, BorderLayout.NORTH);
        card.add(infoPanel, BorderLayout.CENTER);

        // Hover & click
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                card.setBackground(new Color(0xFFF3E0));
                card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(UITheme.ACCENT, 2),
                    BorderFactory.createEmptyBorder(9, 9, 9, 9)
                ));
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                card.setBackground(UITheme.BG_CARD);
                card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1),
                    BorderFactory.createEmptyBorder(10, 10, 10, 10)
                ));
            }
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (menu.getStok() <= 0) {
                    JOptionPane.showMessageDialog(KasirPanel.this,
                        "Stok " + menu.getNama() + " habis!", "Stok Habis",
                        JOptionPane.WARNING_MESSAGE);
                    return;
                }
                addToCart(menu);
            }
        });

        return card;
    }

    // ── Right: Cart / Order Summary ───────────────────────────────────
    private JPanel buildCartPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(UITheme.BG_CARD);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));

        JLabel lblCart = UITheme.headingLabel("📋 Pesanan");
        panel.add(lblCart, BorderLayout.NORTH);

        // Cart table
        String[] cols = {"Menu", "Harga", "Qty", "Subtotal"};
        cartModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable cartTable = new JTable(cartModel);
        UITheme.styleTable(cartTable);
        cartTable.getColumnModel().getColumn(0).setPreferredWidth(140);
        cartTable.getColumnModel().getColumn(1).setPreferredWidth(80);
        cartTable.getColumnModel().getColumn(2).setPreferredWidth(40);
        cartTable.getColumnModel().getColumn(3).setPreferredWidth(90);

        // Double-click to remove
        cartTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = cartTable.getSelectedRow();
                    if (row >= 0) {
                        String namaMenu = (String) cartModel.getValueAt(row, 0);
                        int confirm = JOptionPane.showConfirmDialog(KasirPanel.this,
                            "Hapus " + namaMenu + " dari pesanan?", "Hapus Item",
                            JOptionPane.YES_NO_OPTION);
                        if (confirm == JOptionPane.YES_OPTION) {
                            // Find menuId from details by name
                            currentTrx.getDetails().stream()
                                .filter(d -> d.getNamaMenu().equals(namaMenu))
                                .findFirst()
                                .ifPresent(d -> currentTrx.hapusItem(d.getMenuId()));
                            refreshCart();
                        }
                    }
                }
            }
        });

        JScrollPane scroll = new JScrollPane(cartTable);
        scroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));
        panel.add(scroll, BorderLayout.CENTER);

        // Bottom: promo + summary + payment
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setBackground(UITheme.BG_CARD);

        // Promo section
        bottomPanel.add(Box.createVerticalStrut(8));
        JLabel lblPromo = UITheme.headingLabel("🎁 Kode Promo");
        lblPromo.setAlignmentX(LEFT_ALIGNMENT);
        bottomPanel.add(lblPromo);
        bottomPanel.add(Box.createVerticalStrut(4));

        JPanel promoBar = new JPanel(new BorderLayout(6, 0));
        promoBar.setOpaque(false);
        promoBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        promoBar.setAlignmentX(LEFT_ALIGNMENT);

        tfPromoKode = UITheme.styledField(12);
        tfPromoKode.setToolTipText("Masukkan kode promo...");
        JButton btnPromo = UITheme.accentButton("Terapkan");
        btnPromo.addActionListener(e -> applyPromo());
        JButton btnHapusPromo = UITheme.ghostButton("Hapus");
        btnHapusPromo.addActionListener(e -> removePromo());

        promoBar.add(tfPromoKode, BorderLayout.CENTER);
        promoBar.add(btnPromo, BorderLayout.EAST);
        bottomPanel.add(promoBar);
        bottomPanel.add(Box.createVerticalStrut(2));

        lblPromoInfo = new JLabel(" ");
        lblPromoInfo.setFont(UITheme.FONT_SMALL);
        lblPromoInfo.setForeground(UITheme.SUCCESS);
        lblPromoInfo.setAlignmentX(LEFT_ALIGNMENT);
        bottomPanel.add(lblPromoInfo);
        bottomPanel.add(Box.createVerticalStrut(6));
        bottomPanel.add(new JSeparator());
        bottomPanel.add(Box.createVerticalStrut(8));

        // Summary
        lblSubtotal = summaryRow(bottomPanel, "Subtotal");
        lblDiskon   = summaryRowAccent(bottomPanel, "Diskon");
        lblTotal    = summaryRowBig(bottomPanel, "TOTAL");
        bottomPanel.add(Box.createVerticalStrut(8));
        bottomPanel.add(new JSeparator());
        bottomPanel.add(Box.createVerticalStrut(8));

        // Metode bayar
        JPanel metodePanel = new JPanel(new BorderLayout(8, 0));
        metodePanel.setOpaque(false);
        metodePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        metodePanel.setAlignmentX(LEFT_ALIGNMENT);
        JLabel lblMetode = UITheme.bodyLabel("Metode Bayar:");
        cbMetodeBayar = new JComboBox<>(new String[]{"tunai", "qris", "transfer"});
        cbMetodeBayar.setFont(UITheme.FONT_BODY);
        metodePanel.add(lblMetode, BorderLayout.WEST);
        metodePanel.add(cbMetodeBayar, BorderLayout.CENTER);
        bottomPanel.add(metodePanel);
        bottomPanel.add(Box.createVerticalStrut(8));

        // Action buttons
        JPanel btnRow = new JPanel(new GridLayout(1, 2, 8, 0));
        btnRow.setOpaque(false);
        btnRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btnRow.setAlignmentX(LEFT_ALIGNMENT);

        JButton btnBatal = UITheme.dangerButton("🗑 Batal");
        JButton btnBayar = UITheme.successButton("✅ Bayar");
        btnBayar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnBatal.addActionListener(e -> batalTransaksi());
        btnBayar.addActionListener(e -> prosesTransaksi());

        btnRow.add(btnBatal);
        btnRow.add(btnBayar);
        bottomPanel.add(btnRow);

        panel.add(bottomPanel, BorderLayout.SOUTH);
        return panel;
    }

    private JLabel summaryRow(JPanel parent, String label) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
        row.setAlignmentX(LEFT_ALIGNMENT);
        row.add(UITheme.bodyLabel(label), BorderLayout.WEST);
        JLabel val = UITheme.bodyLabel("Rp 0");
        row.add(val, BorderLayout.EAST);
        parent.add(row);
        return val;
    }

    private JLabel summaryRowAccent(JPanel parent, String label) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
        row.setAlignmentX(LEFT_ALIGNMENT);
        JLabel l = UITheme.bodyLabel(label);
        l.setForeground(UITheme.SUCCESS);
        row.add(l, BorderLayout.WEST);
        JLabel val = new JLabel("- Rp 0");
        val.setFont(UITheme.FONT_BODY);
        val.setForeground(UITheme.SUCCESS);
        row.add(val, BorderLayout.EAST);
        parent.add(row);
        return val;
    }

    private JLabel summaryRowBig(JPanel parent, String label) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        row.setAlignmentX(LEFT_ALIGNMENT);
        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.BOLD, 15));
        l.setForeground(UITheme.TEXT_PRIMARY);
        row.add(l, BorderLayout.WEST);
        JLabel val = new JLabel("Rp 0");
        val.setFont(new Font("Segoe UI", Font.BOLD, 15));
        val.setForeground(UITheme.PRIMARY);
        row.add(val, BorderLayout.EAST);
        parent.add(row);
        return val;
    }

    // ── Business Logic ────────────────────────────────────────────────
    private void addToCart(Menu menu) {
        DetailTransaksi detail = new DetailTransaksi(menu, 1);
        currentTrx.tambahItem(detail);
        refreshCart();
    }

    private void refreshCart() {
        cartModel.setRowCount(0);
        for (DetailTransaksi d : currentTrx.getDetails()) {
            cartModel.addRow(new Object[]{
                d.getNamaMenu(),
                UITheme.formatRupiah(d.getHarga()),
                d.getQty(),
                UITheme.formatRupiah(d.getSubtotal())
            });
        }
        currentTrx.hitungTotal();
        lblSubtotal.setText(UITheme.formatRupiah(currentTrx.getSubtotal()));
        lblDiskon.setText("- " + UITheme.formatRupiah(currentTrx.getDiskon()));
        lblTotal.setText(UITheme.formatRupiah(currentTrx.getTotal()));
    }

    private void applyPromo() {
        String kode = tfPromoKode.getText().trim().toUpperCase();
        if (kode.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Masukkan kode promo terlebih dahulu.");
            return;
        }
        if (currentTrx.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Keranjang masih kosong!");
            return;
        }
        Promosi promo = promosiDAO.findByKode(kode);
        if (promo == null) {
            lblPromoInfo.setText("❌ Kode promo tidak ditemukan.");
            lblPromoInfo.setForeground(UITheme.DANGER);
            return;
        }
        if (!promo.isValid()) {
            lblPromoInfo.setText("❌ Promo tidak aktif atau sudah kadaluarsa.");
            lblPromoInfo.setForeground(UITheme.DANGER);
            return;
        }
        if (currentTrx.getSubtotal() < promo.getMinTransaksi()) {
            lblPromoInfo.setText("❌ Min. transaksi " + UITheme.formatRupiah(promo.getMinTransaksi()));
            lblPromoInfo.setForeground(UITheme.DANGER);
            return;
        }
        currentTrx.setPromosiAndRecalculate(promo);
        lblPromoInfo.setText("✅ Promo " + promo.getNama() + " diterapkan!");
        lblPromoInfo.setForeground(UITheme.SUCCESS);
        refreshCart();
    }

    private void removePromo() {
        currentTrx.setPromosiAndRecalculate(null);
        tfPromoKode.setText("");
        lblPromoInfo.setText(" ");
        refreshCart();
    }

    private void prosesTransaksi() {
        if (currentTrx.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Keranjang masih kosong!", "Peringatan",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Payment dialog
        String metodeBayar = (String) cbMetodeBayar.getSelectedItem();
        currentTrx.setMetodeBayar(metodeBayar);

        if ("tunai".equals(metodeBayar)) {
            String inputBayar = JOptionPane.showInputDialog(this,
                "Total: " + UITheme.formatRupiah(currentTrx.getTotal()) +
                "\n\nJumlah Uang Diterima (Rp):",
                "Pembayaran Tunai", JOptionPane.PLAIN_MESSAGE);
            if (inputBayar == null) return;
            try {
                double bayar = Double.parseDouble(inputBayar.replace(",", "").replace(".", "").trim());
                if (bayar < currentTrx.getTotal()) {
                    JOptionPane.showMessageDialog(this, "Uang tidak cukup!", "Error",
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
                currentTrx.setBayar(bayar);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Jumlah tidak valid!");
                return;
            }
        } else {
            currentTrx.setBayar(currentTrx.getTotal());
        }

        boolean saved = trxDAO.simpan(currentTrx);
        if (saved) {
            // Show receipt
            String struk = PrintUtil.generateStruk(currentTrx);
            JTextArea ta = new JTextArea(struk);
            ta.setFont(UITheme.FONT_MONO);
            ta.setEditable(false);
            JScrollPane sp = new JScrollPane(ta);
            sp.setPreferredSize(new Dimension(360, 420));
            JOptionPane.showMessageDialog(this, sp,
                "✅ Transaksi Berhasil!", JOptionPane.PLAIN_MESSAGE);

            // Reset
            newTransaction();
            refreshCart();
            removePromo();
            loadMenu(); // refresh stok
        } else {
            JOptionPane.showMessageDialog(this, "Gagal menyimpan transaksi!", "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void batalTransaksi() {
        if (currentTrx.isEmpty()) return;
        int confirm = JOptionPane.showConfirmDialog(this,
            "Batalkan semua pesanan?", "Konfirmasi Batal",
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            newTransaction();
            refreshCart();
            removePromo();
        }
    }
}
