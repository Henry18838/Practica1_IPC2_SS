package dao;

import modelo.Empleado;
import util.ConexionBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmpleadoDAO {

    public boolean insertar(Empleado emp) {
        String sql = "INSERT INTO empleado (dpi, nombre_completo, rol, jornada, salario, fecha_contratacion, correo, habilitado) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = ConexionBD.obtenerConexion().prepareStatement(sql)) {
            ps.setString(1, emp.getDpi());
            ps.setString(2, emp.getNombreCompleto());
            ps.setString(3, emp.getRol());
            ps.setString(4, emp.getJornada());
            ps.setDouble(5, emp.getSalario());
            ps.setDate(6, Date.valueOf(emp.getFechaContratacion()));
            ps.setString(7, emp.getCorreo());
            ps.setBoolean(8, true);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error al insertar empleado: " + e.getMessage());
            return false;
        }
    }

    public boolean actualizar(Empleado emp) {
        String sql = "UPDATE empleado SET nombre_completo=?, rol=?, jornada=?, salario=?, correo=? WHERE dpi=?";
        try (PreparedStatement ps = ConexionBD.obtenerConexion().prepareStatement(sql)) {
            ps.setString(1, emp.getNombreCompleto());
            ps.setString(2, emp.getRol());
            ps.setString(3, emp.getJornada());
            ps.setDouble(4, emp.getSalario());
            ps.setString(5, emp.getCorreo());
            ps.setString(6, emp.getDpi());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error al actualizar empleado: " + e.getMessage());
            return false;
        }
    }

    public boolean deshabilitar(String dpi) {
        String sql = "UPDATE empleado SET habilitado = FALSE WHERE dpi = ?";
        try (PreparedStatement ps = ConexionBD.obtenerConexion().prepareStatement(sql)) {
            ps.setString(1, dpi);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error al deshabilitar empleado: " + e.getMessage());
            return false;
        }
    }

    public List<Empleado> listarTodos() {
        List<Empleado> lista = new ArrayList<>();
        String sql = "SELECT * FROM empleado ORDER BY nombre_completo";
        try (Statement st = ConexionBD.obtenerConexion().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(mapearEmpleado(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar empleados: " + e.getMessage());
        }
        return lista;
    }

    public boolean existeCorreo(String correo) {
        String sql = "SELECT COUNT(*) FROM empleado WHERE correo = ?";
        try (PreparedStatement ps = ConexionBD.obtenerConexion().prepareStatement(sql)) {
            ps.setString(1, correo);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {
            System.err.println("Error al verificar correo: " + e.getMessage());
        }
        return false;
    }

    private Empleado mapearEmpleado(ResultSet rs) throws SQLException {
        Empleado emp = new Empleado();
        emp.setDpi(rs.getString("dpi"));
        emp.setNombreCompleto(rs.getString("nombre_completo"));
        emp.setRol(rs.getString("rol"));
        emp.setJornada(rs.getString("jornada"));
        emp.setSalario(rs.getDouble("salario"));
        emp.setFechaContratacion(rs.getDate("fecha_contratacion").toLocalDate());
        emp.setCorreo(rs.getString("correo"));
        emp.setHabilitado(rs.getBoolean("habilitado"));
        return emp;
    }
}