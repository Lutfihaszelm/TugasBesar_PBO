public class PengirimanExpress extends Pengiriman implements LayananKargo{
    double jarak;
    double biayaPrioritas = 100;
    double tarifPerKm = 500;
    double tarifPerKg = 1000;
    public PengirimanExpress(String noResi, String pengirim, String tujuan, double berat, double jarak) {
        super(noResi, tujuan, pengirim, berat);
        this.jarak = jarak;
    }

    @Override
    public double hitungTarif() {
        return (this.berat* tarifPerKg) + (this.jarak * tarifPerKm) + (this.jarak * biayaPrioritas);
    }
}
