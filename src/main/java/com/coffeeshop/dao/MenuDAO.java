package com.coffeeshop.dao;

import com.coffeeshop.config.DatabaseConfig;
import com.coffeeshop.model.Menu;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Module: DAO - MenuDAO
 * Data Access Object for Menu operations.
 */
public class MenuDAO {

    public List<Menu> findAll() {
        List<Menu> list = new ArrayList<>();
        String sql = """
            SELECT m.*, c.nama as kategori_nama
            FROM menu m LEFT JOIN categories c ON m.kategori_id = c.id
            ORDER BY c.nama, m.nama
        """;
        try (Statement st = DatabaseConfig.getConnection().createStatement()) {
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<Menu> findByKategori(int kategoriId) {
        List<Menu> list = new ArrayList<>();
        String sql = """
            SELECT m.*, c.nama as kategori_nama FROM menu m
            LEFT JOIN categories c ON m.kategori_id = c.id
            WHERE m.kategori_id = ? AND m.tersedia = 1
        """;
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setInt(1, kategoriId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<Menu> findAvailable() {
        List<Menu> list = new ArrayList<>();
        String sql = """
            SELECT m.*, c.nama as kategori_nama FROM menu m
            LEFT JOIN categories c ON m.kategori_id = c.id
            WHERE m.tersedia = 1 ORDER BY c.id, m.nama
        """;
        try (Statement st = DatabaseConfig.getConnection().createStatement()) {
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<Menu> search(String keyword) {
        List<Menu> list = new ArrayList<>();
        String sql = """
            SELECT m.*, c.nama as kategori_nama FROM menu m
            LEFT JOIN categories c ON m.kategori_id = c.id
            WHERE (m.nama LIKE ? OR m.kode LIKE ?) AND m.tersedia = 1
        """;
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setString(1, "%" + keyword + "%");
            ps.setString(2, "%" + keyword + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public boolean save(Menu m) {
        String sql = "INSERT INTO menu (kode, nama, kategori_id, harga, stok, satuan, tersedia, deskripsi) VALUES (?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setString(1, m.getKode());
            ps.setString(2, m.getNama());
            ps.setInt(3, m.getKategoriId());
            ps.setDouble(4, m.getHarga());
            ps.setInt(5, m.getStok());
            ps.setString(6, m.getSatuan());
            ps.setInt(7, m.isTersedia() ? 1 : 0);
            ps.setString(8, m.getDeskripsi());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean update(Menu m) {
        String sql = "UPDATE menu SET nama=?, kategori_id=?, harga=?, stok=?, satuan=?, tersedia=?, deskripsi=? WHERE id=?";
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setString(1, m.getNama());
            ps.setInt(2, m.getKategoriId());
            ps.setDouble(3, m.getHarga());
            ps.setInt(4, m.getStok());
            ps.setString(5, m.getSatuan());
            ps.setInt(6, m.isTersedia() ? 1 : 0);
            ps.setString(7, m.getDeskripsi());
            ps.setInt(8, m.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean updateStok(int menuId, int pengurangan) {
        String sql = "UPDATE menu SET stok = stok - ? WHERE id = ? AND stok >= ?";
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setInt(1, pengurangan);
            ps.setInt(2, menuId);
            ps.setInt(3, pengurangan);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM menu WHERE id=?";
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    private Menu mapRow(ResultSet rs) throws SQLException {
        Menu m = new Menu();
        m.setId(rs.getInt("id"));
        m.setKode(rs.getString("kode"));
        m.setNama(rs.getString("nama"));
        m.setKategoriId(rs.getInt("kategori_id"));
        m.setKategoriNama(rs.getString("kategori_nama"));
        m.setHarga(rs.getDouble("harga"));
        m.setStok(rs.getInt("stok"));
        m.setSatuan(rs.getString("satuan"));
        m.setTersedia(rs.getInt("tersedia") == 1);
        m.setDeskripsi(rs.getString("deskripsi"));
        return m;
    }
}
