import java.util.Scanner;
import java.util.ArrayList;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.sql.*;

public class SistemTAM {
    // Poin g: Collection Framework sebagai antrean harian digital
    private ArrayList<Pengiriman> listAntreanHariIni = new ArrayList<>();
    private Scanner input = new Scanner(System.in);
    
    // Poin h: Konfigurasi JDBC untuk penyimpanan permanen
    private final String URL = "jdbc:mysql://localhost:3306/db_tamcargo";
    private final String USER = "root";
    private final String PASS = "";

    public void jalankanSistem() {
        int pilihan = 0;
        do {
            System.out.println("\n===== DASHBOARD OPERASIONAL TAM CARGO =====");
            System.out.println("1. Catat Barang Masuk Gudang (Create)");
            System.out.println("2. Proses Pengiriman/Buat Resi (Update Status)");
            System.out.println("3. Lihat Antrean Hari Ini (Read Collection)");
            System.out.println("4. Lihat Riwayat Pengiriman");
            System.out.println("5. Lihat Stok Gudang Tersedia (Read Database)");
            System.out.println("6. Hapus Data Pengiriman (Delete)");
            System.out.println("7. Keluar");
            System.out.print("Pilih Menu: ");
            
            // Poin f: Exception Handling untuk validasi input
            try {
                pilihan = input.nextInt();
                prosesMenu(pilihan);
            } catch (Exception e) {
                System.out.println("Error: Input tidak valid! Harap masukkan angka.");
                input.nextLine(); 
            }
        } while (pilihan != 7);
    }

    private void prosesMenu(int p) {
        switch (p) {
            case 1 -> catatBarangGudang();
            case 2 -> buatResiPengiriman();
            case 3 -> tampilkanAntrean();
            case 4 -> tampilkanRiwayatDatabase();
            case 5 -> tampilkanStokTersedia();
            case 6 -> hapusPengiriman();
            case 7 -> System.out.println("Menutup sistem... Salam TAM Cargo!");
            default -> System.out.println("Pilihan tidak tersedia.");
        }
    }

    //Pencatatan Barang Masuk Gudang
    private void catatBarangGudang() {
        try {
            System.out.print("Nama Barang/Kargo: ");
            String nama = input.next();
            System.out.print("Berat (kg): ");
            double berat = input.nextDouble();

            String sql = "INSERT INTO gudang_barang (nama_barang, berat, status) VALUES (?, ?, 'Tersedia')";
            try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, nama);
                ps.setDouble(2, berat);
                ps.executeUpdate();
                System.out.println(" Berhasil: Barang tercatat di Gudang (Status: Tersedia).");
            }
        } catch (SQLException e) {
            System.out.println("Gagal simpan ke database: " + e.getMessage());
        }
    }

    //Efisiensi Biaya & Penomoran Resi Unik
    private void buatResiPengiriman() {
        tampilkanStokTersedia();
        System.out.print("\nMasukkan ID Barang dari Gudang yang akan dikirim: ");
        int idBarang = input.nextInt();
        
        System.out.print("Nama Pengirim: ");
        String pengirim = input.next();
        System.out.print("Kota Tujuan: ");
        String tujuan = input.next();
        System.out.print("Jenis Layanan (1. Standard / 2. Express): ");
        int jenis = input.nextInt();
        System.out.println("Jarak(KM): ");
        double jarak = input.nextDouble();

        //Manipulasi Method Date dan String
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmm");
        String noResi = "TAM-" + sdf.format(new Date()) + "-" + tujuan.toUpperCase().substring(0,3);
        
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {
            conn.setAutoCommit(false); // Transaksi JDBC agar data sinkron

            double berat = 0;
            String namaBarang = "";
            
            // Ambil data barang dari gudang
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT nama_barang, berat FROM gudang_barang WHERE id_barang = " + idBarang);
            // Poin d: Percabangan
            if (rs.next()) {
                namaBarang = rs.getString("nama_barang");
                berat = rs.getDouble("berat");
            } else {
                System.out.println(" ID Barang tidak ditemukan!");
                return;
            }

            // Poin c: Inheritance (Memilih Sub-class)
            Pengiriman p;
            if (jenis == 2) {
                p = new PengirimanExpress(noResi, pengirim, tujuan, berat, jarak);
            } else {
                p = new PengirimanStandard(noResi, pengirim, tujuan, berat, jarak); // Default jarak 100km
            }

            // 1. INSERT ke Tabel Pengiriman (Permanen)
            String sqlResi = "INSERT INTO pengiriman VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement ps1 = conn.prepareStatement(sqlResi);
            ps1.setString(1, p.noResi);
            ps1.setString(2, p.pengirim);
            ps1.setString(3, p.tujuan);
            ps1.setDouble(4, p.berat);
            ps1.setDouble(5, p.hitungTarif()); // Poin d: Math Logic
            ps1.setDate(6, new java.sql.Date(System.currentTimeMillis()));
            ps1.executeUpdate();

            // 2. UPDATE Status Barang di Gudang (Status: Dikirim)
            String sqlUpdate = "UPDATE gudang_barang SET status = 'Dikirim', no_resi = ? WHERE id_barang = ?";
            PreparedStatement ps2 = conn.prepareStatement(sqlUpdate);
            ps2.setString(1, p.noResi);
            ps2.setInt(2, idBarang);
            ps2.executeUpdate();

            conn.commit(); // Eksekusi Transaksi
            listAntreanHariIni.add(p); // Poin g: Simpan ke Collection Memori
            
            System.out.println("\n RESI DICETAK: " + noResi);
            System.out.println("Biaya Otomatis: Rp " + p.hitungTarif());
            
        } catch (SQLException e) {
            System.out.println("Gagal proses transaksi: " + e.getMessage());
        }
    }

    private void tampilkanAntrean() {
        System.out.println("\n--- ANTREAN PENGIRIMAN HARI INI (DARI ARRAYLIST) ---");
        if (listAntreanHariIni.isEmpty()) System.out.println("Antrean masih kosong.");
        //Poin d: Perulangan
        for (Pengiriman p : listAntreanHariIni) {
            System.out.println(p.noResi + " | " + p.pengirim + " | Tujuan: " + p.tujuan + " | Rp " + p.hitungTarif());
        }
    }

    private void tampilkanStokTersedia() {
        System.out.println("\n--- STOK BARANG TERSEDIA DI GUDANG (STATUS: TERSEDIA/BELUM DIKIRIM) ---");
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM gudang_barang WHERE status = 'Tersedia'")) {
            while (rs.next()) {
                System.out.println("ID: " + rs.getInt("id_barang") + " | " + rs.getString("nama_barang") + " | " + rs.getDouble("berat") + " kg");
            }
        } catch (SQLException e) { 
            System.out.println("Database Error: " + e.getMessage()); 
        }
    }

    private void hapusPengiriman() {
        System.out.print("Masukkan No Resi yang akan dihapus: ");
        String resi = input.next();
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {
            // Poin h: Delete data dari database
            PreparedStatement ps = conn.prepareStatement("DELETE FROM pengiriman WHERE no_resi = ?");
            ps.setString(1, resi);
            if (ps.executeUpdate() > 0) {
                listAntreanHariIni.removeIf(p -> p.noResi.equals(resi)); // Hapus dari memori
                System.out.println("Data resi " + resi + " berhasil dihapus dari sistem.");
            } else {
                System.out.println(" Data tidak ditemukan.");
            }
        } catch (SQLException e) { 
            System.out.println("Gagal menghapus: " + e.getMessage()); 
        }
    }

    private void tampilkanRiwayatDatabase() {
    System.out.println("\n--- SEMUA RIWAYAT PENGIRIMAN (DARI DATABASE) ---");
    String sql = "SELECT * FROM pengiriman ORDER BY tanggal_kirim DESC";
    
    try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(sql)) {
        
        System.out.printf("%-25s | %-15s | %-15s | %-10s%n", "No Resi", "Pengirim", "Tarif", "Tanggal");
        System.out.println("-----------------------------------------------------------------------");
        
        while (rs.next()) {
            System.out.printf("%-25s | %-15s | Rp %-12.0f | %-10s%n", 
                rs.getString("no_resi"), 
                rs.getString("pengirim"), 
                rs.getDouble("tarif"), 
                rs.getDate("tanggal_kirim"));
        }
    } catch (SQLException e) {
        System.out.println("Gagal memuat data dari database: " + e.getMessage());
    }
}
}