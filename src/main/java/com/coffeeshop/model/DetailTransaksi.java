package com.coffeeshop.model;

/**
 * Module: Model - DetailTransaksi
 * Represents a line item within a transaction.
 */
public class DetailTransaksi {
    private int id;
    private int transaksiId;
    private int menuId;
    private String namaMenu;
    private double harga;
    private int qty;
    private double subtotal;
    private String catatan;

    public DetailTransaksi() {}

    public DetailTransaksi(Menu menu, int qty) {
        this.menuId = menu.getId();
        this.namaMenu = menu.getNama();
        this.harga = menu.getHarga();
        this.qty = qty;
        hitungSubtotal();
    }

    public void hitungSubtotal() {
        this.subtotal = harga * qty;
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getTransaksiId() { return transaksiId; }
    public void setTransaksiId(int transaksiId) { this.transaksiId = transaksiId; }
    public int getMenuId() { return menuId; }
    public void setMenuId(int menuId) { this.menuId = menuId; }
    public String getNamaMenu() { return namaMenu; }
    public void setNamaMenu(String namaMenu) { this.namaMenu = namaMenu; }
    public double getHarga() { return harga; }
    public void setHarga(double harga) { this.harga = harga; }
    public int getQty() { return qty; }
    public void setQty(int qty) { this.qty = qty; hitungSubtotal(); }
    public double getSubtotal() { return subtotal; }
    public String getCatatan() { return catatan; }
    public void setCatatan(String catatan) { this.catatan = catatan; }
}
