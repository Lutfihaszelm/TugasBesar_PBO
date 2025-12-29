public abstract class Pengiriman {
    String noResi;
    String tujuan;
    String pengirim;
    double berat;

    public Pengiriman(String noResi, String tujuan, String pengirim, double berat){
        this.noResi = noResi;
        this.tujuan = tujuan;
        this.pengirim = pengirim;
        this.berat = berat;
    }
    public abstract double hitungTarif();
}
