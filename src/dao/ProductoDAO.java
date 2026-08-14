package dao;

import modelo.Producto;
import modelo.RecetaItem;
import util.ConexionBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductoDAO {

    public int insertar(Producto p) {
        String sql = "INSERT INTO producto (nombre, categoria, precio_venta, foto) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = ConexionBD.obtenerConexion().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getNombre());
            ps.setString(2, p.getCategoria());
            ps.setDouble(3, p.getPrecioVenta());
            if (p.getFoto() != null) {
                ps.setBytes(4, p.getFoto());
            } else {
                ps.setNull(4, Types.BLOB);
            }
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Error al insertar producto: " + e.getMessage());
        }
        return -1;
    }

    public boolean actualizar(Producto p) {
        String sql = "UPDATE producto SET nombre=?, categoria=?, precio_venta=?" +
                     (p.getFoto() != null ? ", foto=?" : "") + " WHERE codigo_producto=?";
        try (PreparedStatement ps = ConexionBD.obtenerConexion().prepareStatement(sql)) {
            ps.setString(1, p.getNombre());
            ps.setString(2, p.getCategoria());
            ps.setDouble(3, p.getPrecioVenta());
            if (p.getFoto() != null) {
                ps.setBytes(4, p.getFoto());
                ps.setInt(5, p.getCodigoProducto());
            } else {
                ps.setInt(4, p.getCodigoProducto());
            }
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error al actualizar producto: " + e.getMessage());
            return false;
        }
    }

    public List<Producto> listarTodos() {
        List<Producto> lista = new ArrayList<>();
        String sql = "SELECT * FROM producto ORDER BY categoria, nombre";
        try (Statement st = ConexionBD.obtenerConexion().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new Producto(
                    rs.getInt("codigo_producto"),
                    rs.getString("nombre"),
                    rs.getString("categoria"),
                    rs.getDouble("precio_venta"),
                    rs.getBytes("foto")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar productos: " + e.getMessage());
        }
        return lista;
    }

    // Reemplaza toda la receta de un producto (borra las anteriores y guarda las nuevas)
    public boolean guardarReceta(int codigoProducto, List<RecetaItem> items) {
        Connection con = ConexionBD.obtenerConexion();
        String sqlBorrar = "DELETE FROM receta WHERE codigo_producto = ?";
        String sqlInsertar = "INSERT INTO receta (codigo_producto, codigo_insumo, cantidad_requerida) VALUES (?, ?, ?)";
        try {
            con.setAutoCommit(false);
            try (PreparedStatement psBorrar = con.prepareStatement(sqlBorrar)) {
                psBorrar.setInt(1, codigoProducto);
                psBorrar.executeUpdate();
            }
            try (PreparedStatement psInsertar = con.prepareStatement(sqlInsertar)) {
                for (RecetaItem item : items) {
                    psInsertar.setInt(1, codigoProducto);
                    psInsertar.setInt(2, item.getCodigoInsumo());
                    psInsertar.setDouble(3, item.getCantidadRequerida());
                    psInsertar.addBatch();
                }
                psInsertar.executeBatch();
            }
            con.commit();
            return true;
        } catch (SQLException e) {
            System.err.println("Error al guardar receta: " + e.getMessage());
            try { con.rollback(); } catch (SQLException ex) { /* ignorar */ }
            return false;
        } finally {
            try { con.setAutoCommit(true); } catch (SQLException ex) { /* ignorar */ }
        }
    }

    public List<RecetaItem> listarRecetaPorProducto(int codigoProducto) {
        List<RecetaItem> lista = new ArrayList<>();
        String sql = "SELECT r.codigo_insumo, i.nombre, r.cantidad_requerida " +
                     "FROM receta r JOIN insumo i ON r.codigo_insumo = i.codigo_insumo " +
                     "WHERE r.codigo_producto = ?";
        try (PreparedStatement ps = ConexionBD.obtenerConexion().prepareStatement(sql)) {
            ps.setInt(1, codigoProducto);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(new RecetaItem(rs.getInt("codigo_insumo"), rs.getString("nombre"), rs.getDouble("cantidad_requerida")));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar receta: " + e.getMessage());
        }
        return lista;
    }
}