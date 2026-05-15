package com.coffeeshop.config;

import java.sql.*;

/**
 * Module: DatabaseConfig
 * Handles SQLite database initialization and connection management.
 */
public class DatabaseConfig {

    private static final String DB_URL = "jdbc:sqlite:dudukdulu_pos.db";
    private static Connection connection;

    public static void initialize() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(DB_URL);
            createTables();
            seedData();
            System.out.println("[DB] Database initialized: dudukdulu_pos.db");
        } catch (Exception e) {
            System.err.println("[DB ERROR] " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(DB_URL);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }

    private static void createTables() throws SQLException {
        Statement stmt = connection.createStatement();

        // Users / Kasir table
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT UNIQUE NOT NULL,
                password TEXT NOT NULL,
                role TEXT NOT NULL DEFAULT 'kasir',
                nama TEXT NOT NULL,
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP
            )
        """);

        // Categories table
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS categories (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nama TEXT NOT NULL,
                deskripsi TEXT
            )
        """);

        // Menu / Products table
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS menu (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                kode TEXT UNIQUE NOT NULL,
                nama TEXT NOT NULL,
                kategori_id INTEGER,
                harga REAL NOT NULL,
                stok INTEGER DEFAULT 0,
                satuan TEXT DEFAULT 'cup',
                tersedia INTEGER DEFAULT 1,
                deskripsi TEXT,
                FOREIGN KEY (kategori_id) REFERENCES categories(id)
            )
        """);

        // Promotions table
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS promosi (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                kode TEXT UNIQUE NOT NULL,
                nama TEXT NOT NULL,
                tipe TEXT NOT NULL,
                nilai REAL NOT NULL,
                min_transaksi REAL DEFAULT 0,
                aktif INTEGER DEFAULT 1,
                tgl_mulai DATE,
                tgl_selesai DATE,
                deskripsi TEXT
            )
        """);

        // Transactions table
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS transaksi (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                no_transaksi TEXT UNIQUE NOT NULL,
                kasir_id INTEGER NOT NULL,
                promosi_id INTEGER,
                subtotal REAL NOT NULL,
                diskon REAL DEFAULT 0,
                pajak REAL DEFAULT 0,
                total REAL NOT NULL,
                bayar REAL NOT NULL,
                kembalian REAL NOT NULL,
                metode_bayar TEXT DEFAULT 'tunai',
                status TEXT DEFAULT 'selesai',
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (kasir_id) REFERENCES users(id),
                FOREIGN KEY (promosi_id) REFERENCES promosi(id)
            )
        """);

        // Transaction details table
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS detail_transaksi (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                transaksi_id INTEGER NOT NULL,
                menu_id INTEGER NOT NULL,
                nama_menu TEXT NOT NULL,
                harga REAL NOT NULL,
                qty INTEGER NOT NULL,
                subtotal REAL NOT NULL,
                catatan TEXT,
                FOREIGN KEY (transaksi_id) REFERENCES transaksi(id),
                FOREIGN KEY (menu_id) REFERENCES menu(id)
            )
        """);

        stmt.close();
        System.out.println("[DB] Tables created/verified.");
    }

    private static void seedData() throws SQLException {
        // Seed admin user if not exists
        PreparedStatement checkUser = connection.prepareStatement(
            "SELECT COUNT(*) FROM users WHERE username = ?");
        checkUser.setString(1, "admin");
        ResultSet rs = checkUser.executeQuery();
        if (rs.getInt(1) == 0) {
            PreparedStatement insertUser = connection.prepareStatement(
                "INSERT INTO users (username, password, role, nama) VALUES (?, ?, ?, ?)");
            insertUser.setString(1, "admin");
            insertUser.setString(2, "admin123");
            insertUser.setString(3, "admin");
            insertUser.setString(4, "Administrator");
            insertUser.execute();

            insertUser.setString(1, "kasir1");
            insertUser.setString(2, "kasir123");
            insertUser.setString(3, "kasir");
            insertUser.setString(4, "Budi Santoso");
            insertUser.execute();

            insertUser.setString(1, "manager");
            insertUser.setString(2, "manager123");
            insertUser.setString(3, "manager");
            insertUser.setString(4, "Manager Toko");
            insertUser.execute();

            insertUser.close();
        }
        checkUser.close();

        // Seed categories
        PreparedStatement checkCat = connection.prepareStatement(
            "SELECT COUNT(*) FROM categories");
        rs = checkCat.executeQuery();
        if (rs.getInt(1) == 0) {
            Statement stmt = connection.createStatement();
            stmt.execute("INSERT INTO categories (nama, deskripsi) VALUES ('Kopi', 'Minuman berbasis kopi')");
            stmt.execute("INSERT INTO categories (nama, deskripsi) VALUES ('Non-Kopi', 'Minuman non-kopi')");
            stmt.execute("INSERT INTO categories (nama, deskripsi) VALUES ('Makanan', 'Makanan & snack')");
            stmt.execute("INSERT INTO categories (nama, deskripsi) VALUES ('Paket', 'Paket bundling')");
            stmt.close();
        }
        checkCat.close();

        // Seed menu
        PreparedStatement checkMenu = connection.prepareStatement(
            "SELECT COUNT(*) FROM menu");
        rs = checkMenu.executeQuery();
        if (rs.getInt(1) == 0) {
            String[][] menus = {
                {"MN001", "Americano", "1", "18000", "50"},
                {"MN002", "Latte", "1", "22000", "50"},
                {"MN003", "Cappuccino", "1", "22000", "50"},
                {"MN004", "Espresso", "1", "15000", "50"},
                {"MN005", "Cold Brew", "1", "25000", "30"},
                {"MN006", "Matcha Latte", "2", "25000", "40"},
                {"MN007", "Teh Tarik", "2", "15000", "40"},
                {"MN008", "Cokelat Panas", "2", "20000", "40"},
                {"MN009", "Jus Jeruk", "2", "18000", "30"},
                {"MN010", "Croissant", "3", "18000", "20"},
                {"MN011", "Roti Bakar", "3", "15000", "25"},
                {"MN012", "Kue Lapis", "3", "12000", "30"},
                {"MN013", "Paket Ngopi Santai", "4", "35000", "20"},
                {"MN014", "Paket Duduk Dulu", "4", "45000", "20"},
            };
            PreparedStatement ins = connection.prepareStatement(
                "INSERT INTO menu (kode, nama, kategori_id, harga, stok) VALUES (?,?,?,?,?)");
            for (String[] m : menus) {
                ins.setString(1, m[0]);
                ins.setString(2, m[1]);
                ins.setInt(3, Integer.parseInt(m[2]));
                ins.setDouble(4, Double.parseDouble(m[3]));
                ins.setInt(5, Integer.parseInt(m[4]));
                ins.execute();
            }
            ins.close();
        }
        checkMenu.close();

        // Seed promosi
        PreparedStatement checkPromo = connection.prepareStatement(
            "SELECT COUNT(*) FROM promosi");
        rs = checkPromo.executeQuery();
        if (rs.getInt(1) == 0) {
            Statement stmt = connection.createStatement();
            stmt.execute("""
                INSERT INTO promosi (kode, nama, tipe, nilai, min_transaksi, tgl_mulai, tgl_selesai, deskripsi)
                VALUES ('DUDUK10', 'Promo Duduk Dulu 10%', 'persen', 10, 50000, '2025-01-01', '2025-12-31', 'Diskon 10% min transaksi 50rb')
            """);
            stmt.execute("""
                INSERT INTO promosi (kode, nama, tipe, nilai, min_transaksi, tgl_mulai, tgl_selesai, deskripsi)
                VALUES ('HEMAT5K', 'Hemat 5 Ribu', 'nominal', 5000, 30000, '2025-01-01', '2025-12-31', 'Potongan langsung 5rb')
            """);
            stmt.execute("""
                INSERT INTO promosi (kode, nama, tipe, nilai, min_transaksi, tgl_mulai, tgl_selesai, deskripsi)
                VALUES ('WEEKEND20', 'Weekend Spesial 20%', 'persen', 20, 80000, '2025-01-01', '2025-12-31', 'Diskon 20% di akhir pekan')
            """);
            stmt.close();
        }
        checkPromo.close();
    }
}
