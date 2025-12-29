public class PengirimanStandard extends Pengiriman implements LayananKargo {
    double jarak;
    double tarifPerKm = 500;
    double tarifPerKg = 1000;

    public PengirimanStandard(String noResi, String pengirim, String tujuan, double berat, double jarak){
        super(noResi, tujuan, pengirim, berat);
        this.jarak = jarak;
    }
    public double hitungTarif(){
        return(this.berat *tarifPerKg) + (this.jarak *tarifPerKm);
    }
}
