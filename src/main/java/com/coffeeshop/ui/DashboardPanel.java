package com.coffeeshop.ui;

import com.coffeeshop.dao.TransaksiDAO;
import com.coffeeshop.util.SessionManager;
import com.coffeeshop.util.UITheme;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Module: UI - DashboardPanel
 * Home/dashboard showing today's summary and quick stats.
 */
public class DashboardPanel extends JPanel {

    private final TransaksiDAO transaksiDAO = new TransaksiDAO();
    private JLabel lblTransaksi, lblPendapatan, lblDiskon;
    private JPanel terlarisList;

    public DashboardPanel() {
        setLayout(new BorderLayout());
        setBackground(UITheme.BG_MAIN);
        buildUI();
    }

    private void buildUI() {
        // ── Header ──────────────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.BG_MAIN);
        header.setBorder(BorderFactory.createEmptyBorder(24, 28, 8, 28));

        JLabel lblTitle = UITheme.titleLabel("Dashboard");
        JLabel lblDate = UITheme.bodyLabel(
            LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy")));
        lblDate.setForeground(UITheme.TEXT_LIGHT);
        JLabel lblWelcome = UITheme.bodyLabel("Selamat datang, " +
            SessionManager.getInstance().getCurrentUser().getNama() + " 👋");

        JPanel headerLeft = new JPanel();
        headerLeft.setLayout(new BoxLayout(headerLeft, BoxLayout.Y_AXIS));
        headerLeft.setOpaque(false);
        headerLeft.add(lblTitle);
        headerLeft.add(Box.createVerticalStrut(4));
        headerLeft.add(lblWelcome);

        header.add(headerLeft, BorderLayout.WEST);
        header.add(lblDate, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // ── Main Content ─────────────────────────────────────────────────
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(UITheme.BG_MAIN);
        content.setBorder(BorderFactory.createEmptyBorder(8, 28, 28, 28));

        // Stats cards row
        JPanel statsRow = new JPanel(new GridLayout(1, 3, 16, 0));
        statsRow.setOpaque(false);
        statsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        statsRow.setAlignmentX(LEFT_ALIGNMENT);

        lblTransaksi  = createStatLabel("0", "Transaksi Hari Ini", UITheme.INFO);
        lblPendapatan = createStatLabel("Rp 0", "Pendapatan Hari Ini", UITheme.SUCCESS);
        lblDiskon     = createStatLabel("Rp 0", "Total Diskon Diberikan", UITheme.ACCENT);

        statsRow.add(lblTransaksi.getParent());
        statsRow.add(lblPendapatan.getParent());
        statsRow.add(lblDiskon.getParent());
        content.add(statsRow);
        content.add(Box.createVerticalStrut(20));

        // Menu terlaris section
        JLabel lblTerlaris = UITheme.headingLabel("🏆 Menu Terlaris");
        lblTerlaris.setAlignmentX(LEFT_ALIGNMENT);
        content.add(lblTerlaris);
        content.add(Box.createVerticalStrut(10));

        terlarisList = new JPanel();
        terlarisList.setLayout(new BoxLayout(terlarisList, BoxLayout.Y_AXIS));
        terlarisList.setBackground(UITheme.BG_CARD);
        terlarisList.setBorder(UITheme.cardBorder());
        terlarisList.setAlignmentX(LEFT_ALIGNMENT);
        terlarisList.setMaximumSize(new Dimension(Integer.MAX_VALUE, 280));
        content.add(terlarisList);

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(UITheme.BG_MAIN);
        add(scroll, BorderLayout.CENTER);

        refresh();
    }

    private JLabel createStatLabel(String value, String desc, Color color) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(UITheme.BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 4, 0, 0, color),
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)
            )
        ));

        JLabel valLbl = new JLabel(value);
        valLbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valLbl.setForeground(color);

        JLabel descLbl = new JLabel(desc);
        descLbl.setFont(UITheme.FONT_SMALL);
        descLbl.setForeground(UITheme.TEXT_LIGHT);

        card.add(valLbl);
        card.add(Box.createVerticalStrut(4));
        card.add(descLbl);
        return valLbl;
    }

    public void refresh() {
        TransaksiDAO dao = new TransaksiDAO();
        Object[] summary = dao.summaryHariIni();
        lblTransaksi.setText(String.valueOf(summary[0]));
        lblPendapatan.setText(UITheme.formatRupiah((double) summary[1]));
        lblDiskon.setText(UITheme.formatRupiah((double) summary[2]));

        // Refresh terlaris
        terlarisList.removeAll();
        List<Object[]> terlaris = dao.menuTerlaris(5);
        if (terlaris.isEmpty()) {
            JLabel lbl = UITheme.bodyLabel("Belum ada data transaksi hari ini.");
            lbl.setForeground(UITheme.TEXT_LIGHT);
            terlarisList.add(lbl);
        } else {
            int rank = 1;
            for (Object[] row : terlaris) {
                JPanel item = new JPanel(new BorderLayout());
                item.setOpaque(false);
                item.setBorder(BorderFactory.createEmptyBorder(6, 0, 6, 0));
                item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

                String rankIcon = switch (rank) {
                    case 1 -> "🥇";
                    case 2 -> "🥈";
                    case 3 -> "🥉";
                    default -> rank + ".";
                };
                JLabel lblName = UITheme.bodyLabel(rankIcon + "  " + row[0]);
                JLabel lblQty  = UITheme.bodyLabel(row[1] + " terjual | " + UITheme.formatRupiah((double)row[2]));
                lblQty.setForeground(UITheme.TEXT_LIGHT);
                item.add(lblName, BorderLayout.WEST);
                item.add(lblQty, BorderLayout.EAST);
                terlarisList.add(item);
                terlarisList.add(new JSeparator());
                rank++;
            }
        }
        terlarisList.revalidate();
        terlarisList.repaint();
    }
}
