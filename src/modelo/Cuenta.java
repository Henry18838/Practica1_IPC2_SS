package modelo;

import java.time.LocalDateTime;

public class Cuenta {
    private int idCuenta;
    private int numeroMesa;
    private String dpiMesero;
    private String nombreMesero; // solo para mostrar
    private LocalDateTime horaApertura;
    private LocalDateTime horaCierre;
    private String estado; // ABIERTA, PAGADA
    private double total;
    private double propina;

    public Cuenta() {}

    public int getIdCuenta() { return idCuenta; }
    public void setIdCuenta(int idCuenta) { this.idCuenta = idCuenta; }

    public int getNumeroMesa() { return numeroMesa; }
    public void setNumeroMesa(int numeroMesa) { this.numeroMesa = numeroMesa; }

    public String getDpiMesero() { return dpiMesero; }
    public void setDpiMesero(String dpiMesero) { this.dpiMesero = dpiMesero; }

    public String getNombreMesero() { return nombreMesero; }
    public void setNombreMesero(String nombreMesero) { this.nombreMesero = nombreMesero; }

    public LocalDateTime getHoraApertura() { return horaApertura; }
    public void setHoraApertura(LocalDateTime horaApertura) { this.horaApertura = horaApertura; }

    public LocalDateTime getHoraCierre() { return horaCierre; }
    public void setHoraCierre(LocalDateTime horaCierre) { this.horaCierre = horaCierre; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }

    public double getPropina() { return propina; }
    public void setPropina(double propina) { this.propina = propina; }
}