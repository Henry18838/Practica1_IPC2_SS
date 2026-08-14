package modelo;

public class Producto {
    private int codigoProducto;
    private String nombre;
    private String categoria; // BEBIDA_CALIENTE, BEBIDA_FRIA, POSTRE, COMIDA
    private double precioVenta;
    private byte[] foto;

    public Producto() {}

    public Producto(int codigoProducto, String nombre, String categoria, double precioVenta, byte[] foto) {
        this.codigoProducto = codigoProducto;
        this.nombre = nombre;
        this.categoria = categoria;
        this.precioVenta = precioVenta;
        this.foto = foto;
    }

    public int getCodigoProducto() { return codigoProducto; }
    public void setCodigoProducto(int codigoProducto) { this.codigoProducto = codigoProducto; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public double getPrecioVenta() { return precioVenta; }
    public void setPrecioVenta(double precioVenta) { this.precioVenta = precioVenta; }

    public byte[] getFoto() { return foto; }
    public void setFoto(byte[] foto) { this.foto = foto; }
}