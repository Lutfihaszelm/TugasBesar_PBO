import java.util.ArrayList;

public class Gudang {
    private ArrayList<Barang> stokBarang = new ArrayList<>();

    public void tambahKeGudang(Barang b) {
        stokBarang.add(b);
    }

    public void tampilkanStok() {
        System.out.println("--- DAFTAR BARANG DI GUDANG TAM CARGO ---");
        for (Barang b : stokBarang) {
            System.out.println("[" + b.kodeBarang + "] " + b.namaBarang + " (" + b.beratBarang + " kg)");
        }
    }
}
