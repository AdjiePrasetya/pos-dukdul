# Kasir Pintar - Duduk Dulu Coffee Shop

Sistem *Point of Sale* (POS) berbasis desktop yang dibangun menggunakan Java dan SQLite. Aplikasi ini dirancang khusus untuk memudahkan proses transaksi dan manajemen operasional harian pada "Duduk Dulu Coffee Shop".

## 🚀 Fitur Utama

- **Login System**: Autentikasi pengguna dengan hak akses berdasarkan *role* (Admin & Kasir).
- **Dashboard**: Ringkasan singkat mengenai aktivitas operasional toko.
- **Sistem Kasir (Transaksi)**: Modul untuk memproses pesanan pelanggan, perhitungan subtotal, pajak, potongan diskon/promosi, hingga kalkulasi kembalian.
- **Manajemen Menu**: Pengelolaan data produk dan minuman (Tambah, Edit, Hapus, Manajemen Stok, Kategori, dan Harga).
- **Manajemen Promosi**: Pengaturan kode voucher/diskon (tipe persentase maupun nominal) lengkap dengan syarat minimal transaksi.
- **Laporan Transaksi**: Rekapitulasi riwayat transaksi penjualan dan detail pesanan.
- **Manajemen Pengguna (User)**: Pengelolaan akun staf (kasir) dan admin.

## 🛠 Teknologi yang Digunakan

- **Bahasa Pemrograman**: Java (Minimal JDK 17)
- **UI Framework**: Java Swing (Desktop GUI)
- **Database**: SQLite (Database lokal berbentuk file tunggal, sangat ringan dan tidak memerlukan instalasi server database khusus)
- **Build Tool**: Apache Maven

## 📂 Struktur Proyek

Berikut adalah gambaran struktur folder pada proyek ini:

```text
pos_project/
├── pom.xml                 # File konfigurasi Maven
├── run.bat                 # Script untuk menjalankan aplikasi di Windows
├── dudukdulu_pos.db        # File database SQLite (Ter-generate otomatis)
└── src/
    └── main/
        └── java/
            └── com/
                └── coffeeshop/
                    ├── config/    # Konfigurasi database & tabel
                    ├── dao/       # Data Access Object (Kueri DB)
                    ├── main/      # Entry point aplikasi (Main.java)
                    ├── model/     # Representasi objek/entitas data
                    ├── ui/        # Antarmuka Java Swing (Panel & Frame)
                    └── util/      # Kelas utilitas (Print, Session, Theme)
```

Secara lebih rinci, pemisahan logika kodenya adalah sebagai berikut:
- `com.coffeeshop.main`: *Entry point* (titik awal) saat aplikasi dijalankan (`Main.java`).
- `com.coffeeshop.model`: Kelas representasi objek data/entitas (seperti `Menu`, `Transaksi`, `User`).
- `com.coffeeshop.dao`: *Data Access Object* yang menangani kueri dan interaksi CRUD langsung dengan *database* SQLite.
- `com.coffeeshop.ui`: Komponen antarmuka pengguna (GUI), memuat jendela (*frame*) dan panel (*panel*) berbasis Java Swing.
- `com.coffeeshop.config`: Konfigurasi *database*, mencakup pembuatan file `dudukdulu_pos.db` secara otomatis, pembuatan tabel, dan penyisipan data awal (*seeding*).
- `com.coffeeshop.util`: Kelas-kelas utilitas tambahan seperti manajemen sesi (*SessionManager*), utilitas cetak (*PrintUtil*), dan tema *styling* antarmuka (*UITheme*).

## 📋 Persyaratan Sistem (Prerequisites)

Pastikan di perangkat Anda sudah terinstal:
1. **Java Development Kit (JDK) 17** atau yang lebih baru.
2. **Apache Maven** (Untuk proses kompilasi dan *packaging*).

## ⚙️ Cara Menjalankan Aplikasi

Saat dijalankan untuk pertama kalinya, aplikasi akan otomatis mengunduh *dependency* (yaitu driver `sqlite-jdbc`), mengompilasi program menjadi file *fat JAR*, lalu secara otomatis menginisiasi file database `dudukdulu_pos.db`.

### 1. Menggunakan Script (Khusus OS Windows)
Di *root folder* proyek, telah disediakan sebuah *batch script*.
- Cukup klik dua kali pada file `run.bat`
- Atau buka *Command Prompt* di folder ini dan ketik:
  ```cmd
  run.bat
  ```

### 2. Secara Manual (Linux, macOS, atau Windows)
Buka terminal dan arahkan ke direktori proyek ini, kemudian jalankan perintah berikut:

**Langkah 1: Build dan Package**
```bash
mvn clean package
```
*(Proses ini akan menghasilkan file `dudukdulu-pos.jar` di dalam folder `target/`)*

**Langkah 2: Menjalankan Aplikasi**
```bash
java -jar target/dudukdulu-pos.jar
```

## 🔐 Akun Akses Default (Seed Data)

Pada saat *database* dibuat di awal, sistem otomatis menyisipkan data contoh (menu kopi, promo, dll) beserta dua akun *default* yang bisa Anda gunakan untuk *login*:

**Akses Administrator (Full Access):**
- **Username**: `admin`
- **Password**: `admin123`

**Akses Kasir (Limited Access):**
- **Username**: `kasir1`
- **Password**: `kasir123`
