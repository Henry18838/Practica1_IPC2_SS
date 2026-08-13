package dao;

import modelo.Insumo;
import util.ConexionBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InsumoDAO {

    public boolean insertar(Insumo ins) {
        String sql = "INSERT INTO insumo (nombre, unidad_medida, stock_actual, stock_minimo, costo) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = ConexionBD.obtenerConexion().prepareStatement(sql)) {
            ps.setString(1, ins.getNombre());
            ps.setString(2, ins.getUnidadMedida());
            ps.setDouble(3, ins.getStockActual());
            ps.setDouble(4, ins.getStockMinimo());
            ps.setDouble(5, ins.getCosto());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error al insertar insumo: " + e.getMessage());
            return false;
        }
    }

    public boolean actualizar(Insumo ins) {
        String sql = "UPDATE insumo SET nombre=?, unidad_medida=?, stock_minimo=?, costo=? WHERE codigo_insumo=?";
        try (PreparedStatement ps = ConexionBD.obtenerConexion().prepareStatement(sql)) {
            ps.setString(1, ins.getNombre());
            ps.setString(2, ins.getUnidadMedida());
            ps.setDouble(3, ins.getStockMinimo());
            ps.setDouble(4, ins.getCosto());
            ps.setInt(5, ins.getCodigoInsumo());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error al actualizar insumo: " + e.getMessage());
            return false;
        }
    }

    // Registra una compra: aumenta el stock y guarda el historial en compra_insumo
    public boolean registrarCompra(int codigoInsumo, double cantidad, double costoTotal) {
        String sqlCompra = "INSERT INTO compra_insumo (codigo_insumo, cantidad, costo_total, fecha_compra) VALUES (?, ?, ?, CURDATE())";
        String sqlStock = "UPDATE insumo SET stock_actual = stock_actual + ? WHERE codigo_insumo = ?";
        Connection con = ConexionBD.obtenerConexion();
        try {
            con.setAutoCommit(false);
            try (PreparedStatement psCompra = con.prepareStatement(sqlCompra)) {
                psCompra.setInt(1, codigoInsumo);
                psCompra.setDouble(2, cantidad);
                psCompra.setDouble(3, costoTotal);
                psCompra.executeUpdate();
            }
            try (PreparedStatement psStock = con.prepareStatement(sqlStock)) {
                psStock.setDouble(1, cantidad);
                psStock.setInt(2, codigoInsumo);
                psStock.executeUpdate();
            }
            con.commit();
            return true;
        } catch (SQLException e) {
            System.err.println("Error al registrar compra: " + e.getMessage());
            try { con.rollback(); } catch (SQLException ex) { /* ignorar */ }
            return false;
        } finally {
            try { con.setAutoCommit(true); } catch (SQLException ex) { /* ignorar */ }
        }
    }

    public List<Insumo> listarTodos() {
        List<Insumo> lista = new ArrayList<>();
        String sql = "SELECT * FROM insumo ORDER BY nombre";
        try (Statement st = ConexionBD.obtenerConexion().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar insumos: " + e.getMessage());
        }
        return lista;
    }

    private Insumo mapear(ResultSet rs) throws SQLException {
        return new Insumo(
            rs.getInt("codigo_insumo"),
            rs.getString("nombre"),
            rs.getString("unidad_medida"),
            rs.getDouble("stock_actual"),
            rs.getDouble("stock_minimo"),
            rs.getDouble("costo")
        );
    }
}