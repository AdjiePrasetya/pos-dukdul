package com.coffeeshop.ui;

import com.coffeeshop.dao.TransaksiDAO;
import com.coffeeshop.model.DetailTransaksi;
import com.coffeeshop.model.Transaksi;
import com.coffeeshop.util.PrintUtil;
import com.coffeeshop.util.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Module: UI - LaporanPanel
 * Sales reports: daily summary, transaction history, and top menu items.
 */
public class LaporanPanel extends JPanel {

    private final TransaksiDAO trxDAO = new TransaksiDAO();
    private DefaultTableModel trxModel, detailModel;
    private JTable trxTable;
    private JLabel lblSummaryTrx, lblSummaryPendapatan, lblSummaryDiskon;
    private JTextField tfTanggal;

    public LaporanPanel() {
        setLayout(new BorderLayout());
        setBackground(UITheme.BG_MAIN);
        buildUI();
    }

    private void buildUI() {
        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.BG_MAIN);
        header.setBorder(BorderFactory.createEmptyBorder(20, 28, 12, 28));
        header.add(UITheme.titleLabel("📊 Laporan Penjualan"), BorderLayout.WEST);

        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        filterRow.setOpaque(false);
        tfTanggal = UITheme.styledField(12);
        tfTanggal.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        tfTanggal.setToolTipText("Format: yyyy-MM-dd");
        JButton btnFilter  = UITheme.primaryButton("🔍 Tampilkan");
        JButton btnToday   = UITheme.ghostButton("Hari Ini");
        btnFilter.addActionListener(e -> refresh());
        btnToday.addActionListener(e -> {
            tfTanggal.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            refresh();
        });
        filterRow.add(new JLabel("Tanggal:"));
        filterRow.add(tfTanggal);
        filterRow.add(btnToday);
        filterRow.add(btnFilter);
        header.add(filterRow, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Summary cards
        JPanel summaryRow = new JPanel(new GridLayout(1, 3, 16, 0));
        summaryRow.setOpaque(false);
        summaryRow.setBorder(BorderFactory.createEmptyBorder(0, 28, 12, 28));
        lblSummaryTrx        = addSummaryCard(summaryRow, "0",   "Total Transaksi",         UITheme.INFO);
        lblSummaryPendapatan = addSummaryCard(summaryRow, "Rp 0","Total Pendapatan",         UITheme.SUCCESS);
        lblSummaryDiskon     = addSummaryCard(summaryRow, "Rp 0","Total Diskon Diberikan",   UITheme.ACCENT);
        add(summaryRow, BorderLayout.CENTER);

        // Transaction table + detail panel
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setDividerLocation(500);
        split.setResizeWeight(0.55);
        split.setBorder(BorderFactory.createEmptyBorder(0, 16, 16, 16));

        // Transaction list
        JPanel leftPanel = new JPanel(new BorderLayout(0, 4));
        leftPanel.setBackground(UITheme.BG_MAIN);
        leftPanel.add(UITheme.headingLabel("  Daftar Transaksi"), BorderLayout.NORTH);

        String[] trxCols = {"No. Transaksi", "Kasir", "Subtotal", "Diskon", "Total", "Bayar", "Metode", "Waktu"};
        trxModel = new DefaultTableModel(trxCols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        trxTable = new JTable(trxModel);
        UITheme.styleTable(trxTable);
        trxTable.getColumnModel().getColumn(0).setPreferredWidth(130);
        trxTable.getColumnModel().getColumn(1).setPreferredWidth(90);
        trxTable.getColumnModel().getColumn(7).setPreferredWidth(110);

        trxTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) loadDetail();
        });

        JButton btnCetak = UITheme.ghostButton("🖨 Cetak Struk");
        btnCetak.addActionListener(e -> cetakSelected());

        JPanel leftBottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftBottom.setOpaque(false);
        leftBottom.add(btnCetak);

        leftPanel.add(new JScrollPane(trxTable), BorderLayout.CENTER);
        leftPanel.add(leftBottom, BorderLayout.SOUTH);

        // Detail panel
        JPanel rightPanel = new JPanel(new BorderLayout(0, 4));
        rightPanel.setBackground(UITheme.BG_MAIN);
        rightPanel.add(UITheme.headingLabel("  Detail Pesanan"), BorderLayout.NORTH);

        String[] detailCols = {"Menu", "Harga Satuan", "Qty", "Subtotal", "Catatan"};
        detailModel = new DefaultTableModel(detailCols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable detailTable = new JTable(detailModel);
        UITheme.styleTable(detailTable);
        rightPanel.add(new JScrollPane(detailTable), BorderLayout.CENTER);

        split.setLeftComponent(leftPanel);
        split.setRightComponent(rightPanel);

        // Remove old center and add tabbed pane
        remove(summaryRow);

        JPanel mainContent = new JPanel(new BorderLayout(0, 8));
        mainContent.setBackground(UITheme.BG_MAIN);
        mainContent.add(summaryRow, BorderLayout.NORTH);
        mainContent.add(split, BorderLayout.CENTER);
        add(mainContent, BorderLayout.CENTER);

        refresh();
    }

    private JLabel addSummaryCard(JPanel parent, String val, String desc, Color color) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(UITheme.BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 4, 0, 0, color),
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR),
                BorderFactory.createEmptyBorder(14, 14, 14, 14)
            )
        ));
        JLabel valLbl = new JLabel(val);
        valLbl.setFont(new Font("Segoe UI", Font.BOLD, 20));
        valLbl.setForeground(color);
        JLabel descLbl = new JLabel(desc);
        descLbl.setFont(UITheme.FONT_SMALL);
        descLbl.setForeground(UITheme.TEXT_LIGHT);
        card.add(valLbl);
        card.add(Box.createVerticalStrut(4));
        card.add(descLbl);
        parent.add(card);
        return valLbl;
    }

    public void refresh() {
        String tanggal = tfTanggal.getText().trim();
        List<Transaksi> list = trxDAO.findByDate(tanggal);

        trxModel.setRowCount(0);
        double totalPendapatan = 0, totalDiskon = 0;
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm:ss");

        for (Transaksi t : list) {
            // Load financial data from DB (stored fields)
            String waktu = t.getCreatedAt() != null ? t.getCreatedAt().format(fmt) : "-";
            trxModel.addRow(new Object[]{
                t.getNoTransaksi(), t.getKasirNama(),
                "Rp " + String.format("%,.0f", t.getSubtotal()),
                "- Rp " + String.format("%,.0f", t.getDiskon()),
                "Rp " + String.format("%,.0f", t.getTotal()),
                "Rp " + String.format("%,.0f", t.getBayar()),
                t.getMetodeBayar().toUpperCase(),
                waktu
            });
            totalPendapatan += t.getTotal();
            totalDiskon += t.getDiskon();
        }

        lblSummaryTrx.setText(String.valueOf(list.size()));
        lblSummaryPendapatan.setText(UITheme.formatRupiah(totalPendapatan));
        lblSummaryDiskon.setText(UITheme.formatRupiah(totalDiskon));
        detailModel.setRowCount(0);
    }

    private void loadDetail() {
        detailModel.setRowCount(0);
        int row = trxTable.getSelectedRow();
        if (row < 0) return;
        String noTrx = (String) trxModel.getValueAt(row, 0);

        // Find transaksi id by noTrx
        trxDAO.findByDate(tfTanggal.getText().trim()).stream()
            .filter(t -> noTrx.equals(t.getNoTransaksi()))
            .findFirst()
            .ifPresent(t -> {
                List<DetailTransaksi> details = trxDAO.findDetailByTransaksiId(t.getId());
                for (DetailTransaksi d : details) {
                    detailModel.addRow(new Object[]{
                        d.getNamaMenu(),
                        UITheme.formatRupiah(d.getHarga()),
                        d.getQty(),
                        UITheme.formatRupiah(d.getSubtotal()),
                        d.getCatatan() != null ? d.getCatatan() : ""
                    });
                }
            });
    }

    private void cetakSelected() {
        int row = trxTable.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Pilih transaksi dulu!"); return; }
        String noTrx = (String) trxModel.getValueAt(row, 0);

        trxDAO.findByDate(tfTanggal.getText().trim()).stream()
            .filter(t -> noTrx.equals(t.getNoTransaksi()))
            .findFirst()
            .ifPresent(t -> {
                t.setDetails(trxDAO.findDetailByTransaksiId(t.getId()));
                String struk = PrintUtil.generateStruk(t);
                JTextArea ta = new JTextArea(struk);
                ta.setFont(UITheme.FONT_MONO);
                ta.setEditable(false);
                JScrollPane sp = new JScrollPane(ta);
                sp.setPreferredSize(new Dimension(360, 420));
                JOptionPane.showMessageDialog(this, sp, "Struk Transaksi", JOptionPane.PLAIN_MESSAGE);
            });
    }
}
