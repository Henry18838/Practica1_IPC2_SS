package modelo;

public class DetalleCuenta {
    private int idDetalle;
    private int idCuenta;
    private int codigoProducto;
    private String nombreProducto; // solo para mostrar
    private int cantidad;
    private double subtotal;

    public DetalleCuenta() {}

    public DetalleCuenta(int codigoProducto, String nombreProducto, int cantidad, double subtotal) {
        this.codigoProducto = codigoProducto;
        this.nombreProducto = nombreProducto;
        this.cantidad = cantidad;
        this.subtotal = subtotal;
    }

    public int getIdDetalle() { return idDetalle; }
    public void setIdDetalle(int idDetalle) { this.idDetalle = idDetalle; }

    public int getIdCuenta() { return idCuenta; }
    public void setIdCuenta(int idCuenta) { this.idCuenta = idCuenta; }

    public int getCodigoProducto() { return codigoProducto; }
    public void setCodigoProducto(int codigoProducto) { this.codigoProducto = codigoProducto; }

    public String getNombreProducto() { return nombreProducto; }
    public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }
}