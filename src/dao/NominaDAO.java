package dao;

import modelo.Nomina;
import util.ConexionBD;

import java.sql.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

public class NominaDAO {

    // Se llama al iniciar la app. Revisa si hoy corresponde generar nóminas y las crea si no existen ya.
    public void generarNominasAutomaticas() {
        LocalDate hoy = LocalDate.now();
        YearMonth mesActual = YearMonth.from(hoy);

        LocalDate fechaGeneracionQuincena = mesActual.atDay(15).minusDays(5); // día 10
        LocalDate fechaGeneracionFinMes = mesActual.atEndOfMonth().minusDays(5);

        if (hoy.equals(fechaGeneracionQuincena)) {
            generarNominaPeriodo("QUINCENA", hoy);
        }
        if (hoy.equals(fechaGeneracionFinMes)) {
            generarNominaPeriodo("FIN_DE_MES", hoy);
        }
    }

    // Botón manual: fuerza la generación del período indicado, sin importar la fecha de hoy
    public void generarNominaManual(String tipoPago) {
        generarNominaPeriodo(tipoPago, LocalDate.now());
    }

    private void generarNominaPeriodo(String tipoPago, LocalDate fechaEmision) {
        String sqlEmpleados = "SELECT * FROM empleado WHERE habilitado = TRUE";
        String sqlYaExiste = "SELECT COUNT(*) FROM nomina WHERE dpi_empleado = ? AND tipo_pago = ? " +
                              "AND MONTH(fecha_emision) = MONTH(?) AND YEAR(fecha_emision) = YEAR(?)";
        String sqlInsertar = "INSERT INTO nomina (dpi_empleado, fecha_emision, tipo_pago, monto, estado) VALUES (?, ?, ?, ?, 'PENDIENTE')";

        Connection con = ConexionBD.obtenerConexion();
        try (Statement st = con.createStatement();
             ResultSet rsEmpleados = st.executeQuery(sqlEmpleados)) {

            while (rsEmpleados.next()) {
                String dpi = rsEmpleados.getString("dpi");
                double salario = rsEmpleados.getDouble("salario");
                String rol = rsEmpleados.getString("rol");

                // Evitar duplicados del mismo período
                try (PreparedStatement psExiste = con.prepareStatement(sqlYaExiste)) {
                    psExiste.setString(1, dpi);
                    psExiste.setString(2, tipoPago);
                    psExiste.setDate(3, Date.valueOf(fechaEmision));
                    psExiste.setDate(4, Date.valueOf(fechaEmision));
                    ResultSet rsExiste = psExiste.executeQuery();
                    if (rsExiste.next() && rsExiste.getInt(1) > 0) continue; // ya existe, saltar
                }

                double monto = tipoPago.equals("QUINCENA") ? salario * 0.30 : salario * 0.70;

                // Propinas del mes se suman solo al pago de fin de mes, y solo aplica a MESERO
                if (tipoPago.equals("FIN_DE_MES") && rol.equals("MESERO")) {
                    monto += obtenerPropinasDelMes(dpi, fechaEmision);
                }

                try (PreparedStatement psInsertar = con.prepareStatement(sqlInsertar)) {
                    psInsertar.setString(1, dpi);
                    psInsertar.setDate(2, Date.valueOf(fechaEmision));
                    psInsertar.setString(3, tipoPago);
                    psInsertar.setDouble(4, monto);
                    psInsertar.executeUpdate();
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al generar nóminas: " + e.getMessage());
        }
    }

    private double obtenerPropinasDelMes(String dpiMesero, LocalDate fecha) {
        String sql = "SELECT COALESCE(SUM(propina), 0) FROM cuenta " +
                     "WHERE dpi_mesero = ? AND estado = 'PAGADA' " +
                     "AND MONTH(fecha) = MONTH(?) AND YEAR(fecha) = YEAR(?)";
        try (PreparedStatement ps = ConexionBD.obtenerConexion().prepareStatement(sql)) {
            ps.setString(1, dpiMesero);
            ps.setDate(2, Date.valueOf(fecha));
            ps.setDate(3, Date.valueOf(fecha));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            System.err.println("Error al calcular propinas: " + e.getMessage());
        }
        return 0;
    }

    public boolean marcarPagado(int codigoNomina) {
        String sql = "UPDATE nomina SET estado = 'PAGADO' WHERE codigo_nomina = ?";
        try (PreparedStatement ps = ConexionBD.obtenerConexion().prepareStatement(sql)) {
            ps.setInt(1, codigoNomina);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error al marcar pagado: " + e.getMessage());
            return false;
        }
    }

    public List<Nomina> listarTodas() {
        List<Nomina> lista = new ArrayList<>();
        String sql = "SELECT n.*, e.nombre_completo FROM nomina n " +
                     "JOIN empleado e ON n.dpi_empleado = e.dpi ORDER BY n.fecha_emision DESC";
        try (Statement st = ConexionBD.obtenerConexion().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Nomina n = new Nomina();
                n.setCodigoNomina(rs.getInt("codigo_nomina"));
                n.setDpiEmpleado(rs.getString("dpi_empleado"));
                n.setNombreEmpleado(rs.getString("nombre_completo"));
                n.setFechaEmision(rs.getDate("fecha_emision").toLocalDate());
                n.setTipoPago(rs.getString("tipo_pago"));
                n.setMonto(rs.getDouble("monto"));
                n.setEstado(rs.getString("estado"));
                lista.add(n);
            }
        } catch (SQLException e) {
            System.err.println("Error al listar nóminas: " + e.getMessage());
        }
        return lista;
    }
}