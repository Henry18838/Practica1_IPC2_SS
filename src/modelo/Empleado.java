package modelo;

import java.time.LocalDate;

public class Empleado {
    private String dpi;
    private String nombreCompleto;
    private String rol;        // MESERO, COCINA, BARISTA, ADMINISTRADOR
    private String jornada;    // MATUTINA, VESPERTINA, NOCTURNA
    private double salario;
    private LocalDate fechaContratacion;
    private String correo;
    private boolean habilitado;

    public Empleado() {}

    public Empleado(String dpi, String nombreCompleto, String rol, String jornada,
                     double salario, LocalDate fechaContratacion, String correo, boolean habilitado) {
        this.dpi = dpi;
        this.nombreCompleto = nombreCompleto;
        this.rol = rol;
        this.jornada = jornada;
        this.salario = salario;
        this.fechaContratacion = fechaContratacion;
        this.correo = correo;
        this.habilitado = habilitado;
    }

    public String getDpi() { return dpi; }
    public void setDpi(String dpi) { this.dpi = dpi; }

    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public String getJornada() { return jornada; }
    public void setJornada(String jornada) { this.jornada = jornada; }

    public double getSalario() { return salario; }
    public void setSalario(double salario) { this.salario = salario; }

    public LocalDate getFechaContratacion() { return fechaContratacion; }
    public void setFechaContratacion(LocalDate fechaContratacion) { this.fechaContratacion = fechaContratacion; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public boolean isHabilitado() { return habilitado; }
    public void setHabilitado(boolean habilitado) { this.habilitado = habilitado; }
}