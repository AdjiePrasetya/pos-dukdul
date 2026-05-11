package com.coffeeshop.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Module: Model - Transaksi
 * Represents a sales transaction (order) in the POS system.
 */
public class Transaksi {
    private int id;
    private String noTransaksi;
    private int kasirId;
    private String kasirNama;
    private Promosi promosi;
    private List<DetailTransaksi> details;
    private double subtotal;
    private double diskon;
    private double pajak;
    private double total;
    private double bayar;
    private double kembalian;
    private String metodeBayar; // tunai, qris, transfer
    private String status;
    private LocalDateTime createdAt;

    public Transaksi() {
        this.details = new ArrayList<>();
        this.metodeBayar = "tunai";
        this.status = "selesai";
    }

    public void tambahItem(DetailTransaksi detail) {
        // Check if item already exists
        for (DetailTransaksi d : details) {
            if (d.getMenuId() == detail.getMenuId()) {
                d.setQty(d.getQty() + detail.getQty());
                d.hitungSubtotal();
                hitungTotal();
                return;
            }
        }
        details.add(detail);
        hitungTotal();
    }

    public void hapusItem(int menuId) {
        details.removeIf(d -> d.getMenuId() == menuId);
        hitungTotal();
    }

    public void hitungTotal() {
        subtotal = details.stream().mapToDouble(DetailTransaksi::getSubtotal).sum();
        diskon = (promosi != null) ? promosi.hitungDiskon(subtotal) : 0;
        pajak = 0; // Coffee shop Pontianak - no tax for simplicity
        total = subtotal - diskon + pajak;
    }

    public void setPromosiAndRecalculate(Promosi p) {
        this.promosi = p;
        hitungTotal();
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNoTransaksi() { return noTransaksi; }
    public void setNoTransaksi(String noTransaksi) { this.noTransaksi = noTransaksi; }
    public int getKasirId() { return kasirId; }
    public void setKasirId(int kasirId) { this.kasirId = kasirId; }
    public String getKasirNama() { return kasirNama; }
    public void setKasirNama(String kasirNama) { this.kasirNama = kasirNama; }
    public Promosi getPromosi() { return promosi; }
    public List<DetailTransaksi> getDetails() { return details; }
    public void setDetails(List<DetailTransaksi> details) { this.details = details; }
    public double getSubtotal() { return subtotal; }
    public double getDiskon() { return diskon; }
    public double getPajak() { return pajak; }
    public double getTotal() { return total; }
    public double getBayar() { return bayar; }
    public void setBayar(double bayar) {
        this.bayar = bayar;
        this.kembalian = bayar - total;
    }
    public double getKembalian() { return kembalian; }
    public String getMetodeBayar() { return metodeBayar; }
    public void setMetodeBayar(String metodeBayar) { this.metodeBayar = metodeBayar; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public boolean isEmpty() { return details.isEmpty(); }
    public int getTotalItem() { return details.stream().mapToInt(DetailTransaksi::getQty).sum(); }
}
