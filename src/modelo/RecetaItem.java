package modelo;

public class RecetaItem {
    private int codigoInsumo;
    private String nombreInsumo; // solo para mostrar en tabla, no se persiste aquí
    private double cantidadRequerida;

    public RecetaItem() {}

    public RecetaItem(int codigoInsumo, String nombreInsumo, double cantidadRequerida) {
        this.codigoInsumo = codigoInsumo;
        this.nombreInsumo = nombreInsumo;
        this.cantidadRequerida = cantidadRequerida;
    }

    public int getCodigoInsumo() { return codigoInsumo; }
    public void setCodigoInsumo(int codigoInsumo) { this.codigoInsumo = codigoInsumo; }

    public String getNombreInsumo() { return nombreInsumo; }
    public void setNombreInsumo(String nombreInsumo) { this.nombreInsumo = nombreInsumo; }

    public double getCantidadRequerida() { return cantidadRequerida; }
    public void setCantidadRequerida(double cantidadRequerida) { this.cantidadRequerida = cantidadRequerida; }
}