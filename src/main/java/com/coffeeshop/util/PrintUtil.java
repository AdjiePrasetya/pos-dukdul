package com.coffeeshop.util;

import com.coffeeshop.model.DetailTransaksi;
import com.coffeeshop.model.Transaksi;

import java.time.format.DateTimeFormatter;

/**
 * Module: Util - PrintUtil
 * Generates receipt text for printing or display.
 */
public class PrintUtil {

    public static String generateStruk(Transaksi t) {
        StringBuilder sb = new StringBuilder();
        String line = "================================";
        String thin = "--------------------------------";

        sb.append(line).append("\n");
        sb.append("     COFFE SHOP DUDUK DULU\n");
        sb.append("   Jl. Ahmad Yani, Pontianak\n");
        sb.append("    Telp: 0561-XXXXXXXX\n");
        sb.append(line).append("\n");

        sb.append("No   : ").append(t.getNoTransaksi()).append("\n");
        if (t.getCreatedAt() != null) {
            sb.append("Tgl  : ").append(t.getCreatedAt()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).append("\n");
        }
        sb.append("Kasir: ").append(t.getKasirNama()).append("\n");
        sb.append("Bayar: ").append(t.getMetodeBayar().toUpperCase()).append("\n");
        sb.append(thin).append("\n");

        // Items
        for (DetailTransaksi d : t.getDetails()) {
            sb.append(d.getNamaMenu()).append("\n");
            sb.append(String.format("  %dx%s = %s\n",
                d.getQty(),
                UITheme.formatRupiah(d.getHarga()),
                UITheme.formatRupiah(d.getSubtotal())
            ));
            if (d.getCatatan() != null && !d.getCatatan().isEmpty()) {
                sb.append("  *").append(d.getCatatan()).append("\n");
            }
        }

        sb.append(thin).append("\n");
        sb.append(String.format("%-16s %s\n", "Subtotal", UITheme.formatRupiah(t.getSubtotal())));

        if (t.getDiskon() > 0) {
            String promoNama = t.getPromosi() != null ? t.getPromosi().getNama() : "Diskon";
            sb.append(String.format("%-16s -%s\n", promoNama, UITheme.formatRupiah(t.getDiskon())));
        }

        sb.append(line).append("\n");
        sb.append(String.format("%-16s %s\n", "TOTAL", UITheme.formatRupiah(t.getTotal())));
        sb.append(String.format("%-16s %s\n", "Bayar", UITheme.formatRupiah(t.getBayar())));
        sb.append(String.format("%-16s %s\n", "Kembalian", UITheme.formatRupiah(t.getKembalian())));
        sb.append(line).append("\n");
        sb.append("   Terima kasih sudah mampir!\n");
        sb.append("     Selamat menikmati :)\n");
        sb.append(line).append("\n");

        return sb.toString();
    }
}
