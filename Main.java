import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        // Poin a: Membuat objek dari class SistemTAM (Instansiasi)
        SistemTAM operasional = new SistemTAM();
        Scanner mainScanner = new Scanner(System.in);
        
        System.out.println("==============================================");
        System.out.println("       SISTEM LOGISTIK TAM CARGO (PT TAM)     ");
        System.out.println("   Solusi Pengiriman Sumatera, Jawa, & Bali   ");
        System.out.println("==============================================");
        
        // Poin d: Perulangan Utama agar aplikasi terus berjalan
        boolean jalan = true;
        while (jalan) {
            // Memanggil menu yang ada di SistemTAM
            operasional.jalankanSistem();
            
            // Konfirmasi penutupan aplikasi setelah keluar dari jalankanSistem()
            System.out.println("\nApakah Anda benar-benar ingin keluar? (y/n)");
            String konfirmasi = mainScanner.next();
            
            if (konfirmasi.equalsIgnoreCase("y")) {
                jalan = false;
                System.out.println("Terima kasih atas dedikasi Anda di TAM Cargo.");
            }
        }
        mainScanner.close();
    }
}