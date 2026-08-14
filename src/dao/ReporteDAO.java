package dao;

import util.ConexionBD;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReporteDAO {

    // ---------- REPORTE 1: FLUJO DE CAJA ----------
    public static class ResultadoFlujoCaja {
        public double totalIngresos;
        public double totalEgresosNomina;
        public double totalEgresosCompras;
        public double balance;
    }

    public ResultadoFlujoCaja flujoCaja(LocalDate desde, LocalDate hasta) {
        ResultadoFlujoCaja r = new ResultadoFlujoCaja();

        String sqlIngresos = "SELECT COALESCE(SUM(total + propina), 0) FROM cuenta WHERE estado = 'PAGADA'" + condicionFecha("fecha", desde, hasta);
        String sqlNomina = "SELECT COALESCE(SUM(monto), 0) FROM nomina WHERE estado = 'PAGADO'" + condicionFecha("fecha_emision", desde, hasta);
        String sqlCompras = "SELECT COALESCE(SUM(costo_total), 0) FROM compra_insumo WHERE 1=1" + condicionFecha("fecha_compra", desde, hasta);

        r.totalIngresos = ejecutarEscalar(sqlIngresos, desde, hasta);
        r.totalEgresosNomina = ejecutarEscalar(sqlNomina, desde, hasta);
        r.totalEgresosCompras = ejecutarEscalar(sqlCompras, desde, hasta);
        r.balance = r.totalIngresos - r.totalEgresosNomina - r.totalEgresosCompras;
        return r;
    }

    // ---------- REPORTE 2: PRODUCTOS MÁS VENDIDOS ----------
    public static class ProductoVendido {
        public String nombre;
        public int cantidadVendida;
    }

    public List<ProductoVendido> productosMasVendidos(LocalDate desde, LocalDate hasta) {
        List<ProductoVendido> lista = new ArrayList<>();
        String sql = "SELECT p.nombre, SUM(d.cantidad) AS total_vendido " +
                     "FROM detalle_cuenta d " +
                     "JOIN producto p ON d.codigo_producto = p.codigo_producto " +
                     "JOIN cuenta c ON d.id_cuenta = c.id_cuenta " +
                     "WHERE c.estado = 'PAGADA'" + condicionFecha("c.fecha", desde, hasta) +
                     " GROUP BY p.codigo_producto, p.nombre ORDER BY total_vendido DESC";
        try (PreparedStatement ps = prepararConFechas(sql, desde, hasta)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ProductoVendido pv = new ProductoVendido();
                pv.nombre = rs.getString("nombre");
                pv.cantidadVendida = rs.getInt("total_vendido");
                lista.add(pv);
            }
        } catch (SQLException e) {
            System.err.println("Error en reporte de productos más vendidos: " + e.getMessage());
        }
        return lista;
    }

    // ---------- REPORTE 3: INSUMOS CON BAJO STOCK ----------
    public static class InsumoBajoStock {
        public String nombre;
        public double stockActual;
        public double stockMinimo;
    }

    public List<InsumoBajoStock> insumosBajoStock() {
        List<InsumoBajoStock> lista = new ArrayList<>();
        String sql = "SELECT nombre, stock_actual, stock_minimo FROM insumo WHERE stock_actual <= stock_minimo ORDER BY nombre";
        try (Statement st = ConexionBD.obtenerConexion().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                InsumoBajoStock i = new InsumoBajoStock();
                i.nombre = rs.getString("nombre");
                i.stockActual = rs.getDouble("stock_actual");
                i.stockMinimo = rs.getDouble("stock_minimo");
                lista.add(i);
            }
        } catch (SQLException e) {
            System.err.println("Error en reporte de bajo stock: " + e.getMessage());
        }
        return lista;
    }

    // ---------- Utilidades para el filtro de fechas ----------
    private String condicionFecha(String columna, LocalDate desde, LocalDate hasta) {
        StringBuilder sb = new StringBuilder();
        if (desde != null) sb.append(" AND ").append(columna).append(" >= ?");
        if (hasta != null) sb.append(" AND ").append(columna).append(" <= ?");
        return sb.toString();
    }

    private PreparedStatement prepararConFechas(String sql, LocalDate desde, LocalDate hasta) throws SQLException {
        PreparedStatement ps = ConexionBD.obtenerConexion().prepareStatement(sql);
        int index = 1;
        if (desde != null) ps.setDate(index++, Date.valueOf(desde));
        if (hasta != null) ps.setDate(index++, Date.valueOf(hasta));
        return ps;
    }

    private double ejecutarEscalar(String sql, LocalDate desde, LocalDate hasta) {
        try (PreparedStatement ps = prepararConFechas(sql, desde, hasta)) {
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            System.err.println("Error ejecutando reporte: " + e.getMessage());
        }
        return 0;
    }
}