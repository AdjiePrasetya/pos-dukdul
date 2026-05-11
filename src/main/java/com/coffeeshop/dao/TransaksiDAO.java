package com.coffeeshop.dao;

import com.coffeeshop.config.DatabaseConfig;
import com.coffeeshop.model.DetailTransaksi;
import com.coffeeshop.model.Transaksi;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Module: DAO - TransaksiDAO
 * Data Access Object for Transaksi (sales transaction) operations.
 */
public class TransaksiDAO {

    private final MenuDAO menuDAO = new MenuDAO();

    public boolean simpan(Transaksi t) {
        Connection conn = DatabaseConfig.getConnection();
        try {
            conn.setAutoCommit(false);

            // Insert transaksi
            String sql = """
                INSERT INTO transaksi (no_transaksi, kasir_id, promosi_id, subtotal, diskon, pajak, total, bayar, kembalian, metode_bayar, status)
                VALUES (?,?,?,?,?,?,?,?,?,?,?)
            """;
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, t.getNoTransaksi());
            ps.setInt(2, t.getKasirId());
            ps.setObject(3, t.getPromosi() != null ? t.getPromosi().getId() : null);
            ps.setDouble(4, t.getSubtotal());
            ps.setDouble(5, t.getDiskon());
            ps.setDouble(6, t.getPajak());
            ps.setDouble(7, t.getTotal());
            ps.setDouble(8, t.getBayar());
            ps.setDouble(9, t.getKembalian());
            ps.setString(10, t.getMetodeBayar());
            ps.setString(11, t.getStatus());
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            int transaksiId = keys.next() ? keys.getInt(1) : -1;

            // Insert detail transaksi
            String sqlDetail = """
                INSERT INTO detail_transaksi (transaksi_id, menu_id, nama_menu, harga, qty, subtotal, catatan)
                VALUES (?,?,?,?,?,?,?)
            """;
            PreparedStatement psDetail = conn.prepareStatement(sqlDetail);
            for (DetailTransaksi d : t.getDetails()) {
                psDetail.setInt(1, transaksiId);
                psDetail.setInt(2, d.getMenuId());
                psDetail.setString(3, d.getNamaMenu());
                psDetail.setDouble(4, d.getHarga());
                psDetail.setInt(5, d.getQty());
                psDetail.setDouble(6, d.getSubtotal());
                psDetail.setString(7, d.getCatatan());
                psDetail.addBatch();

                // Reduce stock
                menuDAO.updateStok(d.getMenuId(), d.getQty());
            }
            psDetail.executeBatch();
            conn.commit();
            conn.setAutoCommit(true);
            return true;
        } catch (SQLException e) {
            try { conn.rollback(); conn.setAutoCommit(true); } catch (SQLException ignored) {}
            e.printStackTrace();
            return false;
        }
    }

    public List<Transaksi> findAll() {
        List<Transaksi> list = new ArrayList<>();
        String sql = """
            SELECT t.*, u.nama as kasir_nama FROM transaksi t
            LEFT JOIN users u ON t.kasir_id = u.id
            ORDER BY t.created_at DESC
        """;
        try (Statement st = DatabaseConfig.getConnection().createStatement()) {
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<Transaksi> findByDate(String tanggal) {
        List<Transaksi> list = new ArrayList<>();
        String sql = """
            SELECT t.*, u.nama as kasir_nama FROM transaksi t
            LEFT JOIN users u ON t.kasir_id = u.id
            WHERE DATE(t.created_at) = ?
            ORDER BY t.created_at DESC
        """;
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setString(1, tanggal);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<DetailTransaksi> findDetailByTransaksiId(int transaksiId) {
        List<DetailTransaksi> list = new ArrayList<>();
        String sql = "SELECT * FROM detail_transaksi WHERE transaksi_id = ?";
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setInt(1, transaksiId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                DetailTransaksi d = new DetailTransaksi();
                d.setId(rs.getInt("id"));
                d.setTransaksiId(rs.getInt("transaksi_id"));
                d.setMenuId(rs.getInt("menu_id"));
                d.setNamaMenu(rs.getString("nama_menu"));
                d.setHarga(rs.getDouble("harga"));
                d.setQty(rs.getInt("qty"));
                d.setCatatan(rs.getString("catatan"));
                list.add(d);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // Laporan: Total pendapatan per hari
    public List<Object[]> laporanHarian(String bulan) {
        List<Object[]> list = new ArrayList<>();
        String sql = """
            SELECT DATE(created_at) as tanggal, COUNT(*) as jumlah, SUM(total) as pendapatan
            FROM transaksi WHERE strftime('%Y-%m', created_at) = ?
            GROUP BY DATE(created_at) ORDER BY tanggal
        """;
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setString(1, bulan);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Object[]{rs.getString("tanggal"), rs.getInt("jumlah"), rs.getDouble("pendapatan")});
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // Menu terlaris
    public List<Object[]> menuTerlaris(int limit) {
        List<Object[]> list = new ArrayList<>();
        String sql = """
            SELECT dt.nama_menu, SUM(dt.qty) as total_qty, SUM(dt.subtotal) as total_pendapatan
            FROM detail_transaksi dt
            GROUP BY dt.nama_menu ORDER BY total_qty DESC LIMIT ?
        """;
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setInt(1, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Object[]{rs.getString("nama_menu"), rs.getInt("total_qty"), rs.getDouble("total_pendapatan")});
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // Summary hari ini
    public Object[] summaryHariIni() {
        String sql = """
            SELECT COUNT(*) as jumlah, COALESCE(SUM(total),0) as pendapatan, COALESCE(SUM(diskon),0) as total_diskon
            FROM transaksi WHERE DATE(created_at) = DATE('now')
        """;
        try (Statement st = DatabaseConfig.getConnection().createStatement()) {
            ResultSet rs = st.executeQuery(sql);
            if (rs.next()) {
                return new Object[]{rs.getInt("jumlah"), rs.getDouble("pendapatan"), rs.getDouble("total_diskon")};
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return new Object[]{0, 0.0, 0.0};
    }

    private Transaksi mapRow(ResultSet rs) throws SQLException {
        Transaksi t = new Transaksi();
        t.setId(rs.getInt("id"));
        t.setNoTransaksi(rs.getString("no_transaksi"));
        t.setKasirId(rs.getInt("kasir_id"));
        t.setKasirNama(rs.getString("kasir_nama"));
        t.setBayar(rs.getDouble("bayar"));
        t.setMetodeBayar(rs.getString("metode_bayar"));
        t.setStatus(rs.getString("status"));
        String createdAt = rs.getString("created_at");
        if (createdAt != null) {
            try {
                t.setCreatedAt(LocalDateTime.parse(createdAt.replace(" ", "T"),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
            } catch (Exception ignored) {}
        }
        return t;
    }

    public String generateNoTransaksi() {
        String prefix = "TRX" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String sql = "SELECT COUNT(*) FROM transaksi WHERE no_transaksi LIKE ?";
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setString(1, prefix + "%");
            ResultSet rs = ps.executeQuery();
            int count = rs.getInt(1) + 1;
            return prefix + String.format("%03d", count);
        } catch (SQLException e) { e.printStackTrace(); }
        return prefix + "001";
    }
}
