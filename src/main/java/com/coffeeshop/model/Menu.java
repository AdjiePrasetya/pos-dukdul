package com.coffeeshop.model;

/**
 * Module: Model - Menu
 * Represents a product/menu item in the coffee shop.
 */
public class Menu {
    private int id;
    private String kode;
    private String nama;
    private int kategoriId;
    private String kategoriNama;
    private double harga;
    private int stok;
    private String satuan;
    private boolean tersedia;
    private String deskripsi;

    public Menu() {}

    public Menu(int id, String kode, String nama, int kategoriId, double harga, int stok) {
        this.id = id;
        this.kode = kode;
        this.nama = nama;
        this.kategoriId = kategoriId;
        this.harga = harga;
        this.stok = stok;
        this.tersedia = true;
        this.satuan = "cup";
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getKode() { return kode; }
    public void setKode(String kode) { this.kode = kode; }
    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }
    public int getKategoriId() { return kategoriId; }
    public void setKategoriId(int kategoriId) { this.kategoriId = kategoriId; }
    public String getKategoriNama() { return kategoriNama; }
    public void setKategoriNama(String kategoriNama) { this.kategoriNama = kategoriNama; }
    public double getHarga() { return harga; }
    public void setHarga(double harga) { this.harga = harga; }
    public int getStok() { return stok; }
    public void setStok(int stok) { this.stok = stok; }
    public String getSatuan() { return satuan; }
    public void setSatuan(String satuan) { this.satuan = satuan; }
    public boolean isTersedia() { return tersedia; }
    public void setTersedia(boolean tersedia) { this.tersedia = tersedia; }
    public String getDeskripsi() { return deskripsi; }
    public void setDeskripsi(String deskripsi) { this.deskripsi = deskripsi; }

    @Override
    public String toString() { return nama; }
}
