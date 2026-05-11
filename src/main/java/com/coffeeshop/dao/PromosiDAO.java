package com.coffeeshop.dao;

import com.coffeeshop.config.DatabaseConfig;
import com.coffeeshop.model.Promosi;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Module: DAO - PromosiDAO
 * Data Access Object for Promosi (promotion/discount) operations.
 */
public class PromosiDAO {

    public List<Promosi> findAll() {
        List<Promosi> list = new ArrayList<>();
        String sql = "SELECT * FROM promosi ORDER BY nama";
        try (Statement st = DatabaseConfig.getConnection().createStatement()) {
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<Promosi> findAktif() {
        List<Promosi> list = new ArrayList<>();
        String today = LocalDate.now().toString();
        String sql = """
            SELECT * FROM promosi
            WHERE aktif = 1 AND tgl_mulai <= ? AND tgl_selesai >= ?
            ORDER BY nama
        """;
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setString(1, today);
            ps.setString(2, today);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public Promosi findByKode(String kode) {
        String sql = "SELECT * FROM promosi WHERE kode = ?";
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setString(1, kode.toUpperCase());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public boolean save(Promosi p) {
        String sql = """
            INSERT INTO promosi (kode, nama, tipe, nilai, min_transaksi, aktif, tgl_mulai, tgl_selesai, deskripsi)
            VALUES (?,?,?,?,?,?,?,?,?)
        """;
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setString(1, p.getKode().toUpperCase());
            ps.setString(2, p.getNama());
            ps.setString(3, p.getTipe());
            ps.setDouble(4, p.getNilai());
            ps.setDouble(5, p.getMinTransaksi());
            ps.setInt(6, p.isAktif() ? 1 : 0);
            ps.setString(7, p.getTglMulai() != null ? p.getTglMulai().toString() : null);
            ps.setString(8, p.getTglSelesai() != null ? p.getTglSelesai().toString() : null);
            ps.setString(9, p.getDeskripsi());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean update(Promosi p) {
        String sql = "UPDATE promosi SET nama=?, tipe=?, nilai=?, min_transaksi=?, aktif=?, tgl_mulai=?, tgl_selesai=?, deskripsi=? WHERE id=?";
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setString(1, p.getNama());
            ps.setString(2, p.getTipe());
            ps.setDouble(3, p.getNilai());
            ps.setDouble(4, p.getMinTransaksi());
            ps.setInt(5, p.isAktif() ? 1 : 0);
            ps.setString(6, p.getTglMulai() != null ? p.getTglMulai().toString() : null);
            ps.setString(7, p.getTglSelesai() != null ? p.getTglSelesai().toString() : null);
            ps.setString(8, p.getDeskripsi());
            ps.setInt(9, p.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM promosi WHERE id=?";
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    private Promosi mapRow(ResultSet rs) throws SQLException {
        Promosi p = new Promosi();
        p.setId(rs.getInt("id"));
        p.setKode(rs.getString("kode"));
        p.setNama(rs.getString("nama"));
        p.setTipe(rs.getString("tipe"));
        p.setNilai(rs.getDouble("nilai"));
        p.setMinTransaksi(rs.getDouble("min_transaksi"));
        p.setAktif(rs.getInt("aktif") == 1);
        String tglMulai = rs.getString("tgl_mulai");
        String tglSelesai = rs.getString("tgl_selesai");
        if (tglMulai != null) p.setTglMulai(LocalDate.parse(tglMulai));
        if (tglSelesai != null) p.setTglSelesai(LocalDate.parse(tglSelesai));
        p.setDeskripsi(rs.getString("deskripsi"));
        return p;
    }
}
