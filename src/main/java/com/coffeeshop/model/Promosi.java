package com.coffeeshop.model;

import java.time.LocalDate;

/**
 * Module: Model - Promosi
 * Represents a promotion/discount that can be applied to a transaction.
 * This is the differentiating feature of Duduk Dulu POS system.
 */
public class Promosi {
    private int id;
    private String kode;
    private String nama;
    private String tipe; // "persen" or "nominal"
    private double nilai;
    private double minTransaksi;
    private boolean aktif;
    private LocalDate tglMulai;
    private LocalDate tglSelesai;
    private String deskripsi;

    public Promosi() {}

    public Promosi(int id, String kode, String nama, String tipe, double nilai, double minTransaksi) {
        this.id = id;
        this.kode = kode;
        this.nama = nama;
        this.tipe = tipe;
        this.nilai = nilai;
        this.minTransaksi = minTransaksi;
        this.aktif = true;
    }

    /**
     * Calculate discount amount based on subtotal
     */
    public double hitungDiskon(double subtotal) {
        if (subtotal < minTransaksi) return 0;
        if ("persen".equals(tipe)) {
            return subtotal * (nilai / 100.0);
        } else {
            return nilai;
        }
    }

    public boolean isValid() {
        if (!aktif) return false;
        LocalDate today = LocalDate.now();
        if (tglMulai != null && today.isBefore(tglMulai)) return false;
        if (tglSelesai != null && today.isAfter(tglSelesai)) return false;
        return true;
    }

    public String getLabel() {
        if ("persen".equals(tipe)) {
            return kode + " - " + nama + " (" + (int)nilai + "%)";
        } else {
            return kode + " - " + nama + " (Rp " + String.format("%,.0f", nilai) + ")";
        }
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getKode() { return kode; }
    public void setKode(String kode) { this.kode = kode; }
    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }
    public String getTipe() { return tipe; }
    public void setTipe(String tipe) { this.tipe = tipe; }
    public double getNilai() { return nilai; }
    public void setNilai(double nilai) { this.nilai = nilai; }
    public double getMinTransaksi() { return minTransaksi; }
    public void setMinTransaksi(double minTransaksi) { this.minTransaksi = minTransaksi; }
    public boolean isAktif() { return aktif; }
    public void setAktif(boolean aktif) { this.aktif = aktif; }
    public LocalDate getTglMulai() { return tglMulai; }
    public void setTglMulai(LocalDate tglMulai) { this.tglMulai = tglMulai; }
    public LocalDate getTglSelesai() { return tglSelesai; }
    public void setTglSelesai(LocalDate tglSelesai) { this.tglSelesai = tglSelesai; }
    public String getDeskripsi() { return deskripsi; }
    public void setDeskripsi(String deskripsi) { this.deskripsi = deskripsi; }

    @Override
    public String toString() { return getLabel(); }
}
