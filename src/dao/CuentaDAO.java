package dao;

import modelo.Cuenta;
import modelo.DetalleCuenta;
import util.ConexionBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CuentaDAO {

    // --- Abrir cuenta: crea la cuenta y marca la mesa como OCUPADA ---
    public int abrirCuenta(int numeroMesa, String dpiMesero) {
        Connection con = ConexionBD.obtenerConexion();
        String sqlCuenta = "INSERT INTO cuenta (numero_mesa, dpi_mesero, fecha, hora_apertura, estado, total, propina) " +
                            "VALUES (?, ?, CURDATE(), NOW(), 'ABIERTA', 0, 0)";
        String sqlMesa = "UPDATE mesa SET estado = 'OCUPADA' WHERE numero_mesa = ?";
        try {
            con.setAutoCommit(false);
            int idGenerado = -1;
            try (PreparedStatement ps = con.prepareStatement(sqlCuenta, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, numeroMesa);
                ps.setString(2, dpiMesero);
                ps.executeUpdate();
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) idGenerado = rs.getInt(1);
            }
            try (PreparedStatement ps = con.prepareStatement(sqlMesa)) {
                ps.setInt(1, numeroMesa);
                ps.executeUpdate();
            }
            con.commit();
            return idGenerado;
        } catch (SQLException e) {
            System.err.println("Error al abrir cuenta: " + e.getMessage());
            try { con.rollback(); } catch (SQLException ex) { /* ignorar */ }
            return -1;
        } finally {
            try { con.setAutoCommit(true); } catch (SQLException ex) { /* ignorar */ }
        }
    }

    // --- Verifica que haya stock suficiente de TODOS los insumos de la receta ---
    public boolean verificarInventarioSuficiente(int codigoProducto, int cantidadPedida) {
        String sql = "SELECT i.nombre, i.stock_actual, r.cantidad_requerida " +
                     "FROM receta r JOIN insumo i ON r.codigo_insumo = i.codigo_insumo " +
                     "WHERE r.codigo_producto = ?";
        try (PreparedStatement ps = ConexionBD.obtenerConexion().prepareStatement(sql)) {
            ps.setInt(1, codigoProducto);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                double necesario = rs.getDouble("cantidad_requerida") * cantidadPedida;
                if (rs.getDouble("stock_actual") < necesario) {
                    return false; // no alcanza el insumo
                }
            }
            return true;
        } catch (SQLException e) {
            System.err.println("Error al verificar inventario: " + e.getMessage());
            return false;
        }
    }

    // --- Agrega un producto a la cuenta, descuenta inventario y actualiza el total ---
    public boolean agregarProducto(int idCuenta, int codigoProducto, int cantidad, double precioUnitario) {
        if (!verificarInventarioSuficiente(codigoProducto, cantidad)) {
            return false; // el llamador debe mostrar la alerta de inventario insuficiente
        }

        Connection con = ConexionBD.obtenerConexion();
        double subtotal = precioUnitario * cantidad;

        String sqlDetalle = "INSERT INTO detalle_cuenta (id_cuenta, codigo_producto, cantidad, subtotal) VALUES (?, ?, ?, ?)";
        String sqlTotal = "UPDATE cuenta SET total = total + ? WHERE id_cuenta = ?";
        String sqlReceta = "SELECT codigo_insumo, cantidad_requerida FROM receta WHERE codigo_producto = ?";
        String sqlDescontar = "UPDATE insumo SET stock_actual = stock_actual - ? WHERE codigo_insumo = ?";

        try {
            con.setAutoCommit(false);

            try (PreparedStatement ps = con.prepareStatement(sqlDetalle)) {
                ps.setInt(1, idCuenta);
                ps.setInt(2, codigoProducto);
                ps.setInt(3, cantidad);
                ps.setDouble(4, subtotal);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = con.prepareStatement(sqlTotal)) {
                ps.setDouble(1, subtotal);
                ps.setInt(2, idCuenta);
                ps.executeUpdate();
            }

            try (PreparedStatement psReceta = con.prepareStatement(sqlReceta)) {
                psReceta.setInt(1, codigoProducto);
                ResultSet rs = psReceta.executeQuery();
                try (PreparedStatement psDescontar = con.prepareStatement(sqlDescontar)) {
                    while (rs.next()) {
                        double cantidadDescontar = rs.getDouble("cantidad_requerida") * cantidad;
                        psDescontar.setDouble(1, cantidadDescontar);
                        psDescontar.setInt(2, rs.getInt("codigo_insumo"));
                        psDescontar.addBatch();
                    }
                    psDescontar.executeBatch();
                }
            }

            con.commit();
            return true;
        } catch (SQLException e) {
            System.err.println("Error al agregar producto a cuenta: " + e.getMessage());
            try { con.rollback(); } catch (SQLException ex) { /* ignorar */ }
            return false;
        } finally {
            try { con.setAutoCommit(true); } catch (SQLException ex) { /* ignorar */ }
        }
    }

    // --- Cobra la cuenta: la marca PAGADA, registra propina, y libera la mesa ---
    public boolean cobrarCuenta(int idCuenta, int numeroMesa, double propina) {
        Connection con = ConexionBD.obtenerConexion();
        String sqlCuenta = "UPDATE cuenta SET estado = 'PAGADA', hora_cierre = NOW(), propina = ? WHERE id_cuenta = ?";
        String sqlMesa = "UPDATE mesa SET estado = 'LIBRE' WHERE numero_mesa = ?";
        try {
            con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement(sqlCuenta)) {
                ps.setDouble(1, propina);
                ps.setInt(2, idCuenta);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = con.prepareStatement(sqlMesa)) {
                ps.setInt(1, numeroMesa);
                ps.executeUpdate();
            }
            con.commit();
            return true;
        } catch (SQLException e) {
            System.err.println("Error al cobrar cuenta: " + e.getMessage());
            try { con.rollback(); } catch (SQLException ex) { /* ignorar */ }
            return false;
        } finally {
            try { con.setAutoCommit(true); } catch (SQLException ex) { /* ignorar */ }
        }
    }

    public List<Cuenta> listarCuentasAbiertas() {
        List<Cuenta> lista = new ArrayList<>();
        String sql = "SELECT c.*, e.nombre_completo FROM cuenta c " +
                     "JOIN empleado e ON c.dpi_mesero = e.dpi " +
                     "WHERE c.estado = 'ABIERTA' ORDER BY c.hora_apertura";
        try (Statement st = ConexionBD.obtenerConexion().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar cuentas abiertas: " + e.getMessage());
        }
        return lista;
    }

    public List<DetalleCuenta> listarDetalle(int idCuenta) {
        List<DetalleCuenta> lista = new ArrayList<>();
        String sql = "SELECT d.*, p.nombre FROM detalle_cuenta d " +
                     "JOIN producto p ON d.codigo_producto = p.codigo_producto " +
                     "WHERE d.id_cuenta = ?";
        try (PreparedStatement ps = ConexionBD.obtenerConexion().prepareStatement(sql)) {
            ps.setInt(1, idCuenta);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(new DetalleCuenta(
                    rs.getInt("codigo_producto"), rs.getString("nombre"),
                    rs.getInt("cantidad"), rs.getDouble("subtotal")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar detalle: " + e.getMessage());
        }
        return lista;
    }

    private Cuenta mapear(ResultSet rs) throws SQLException {
        Cuenta c = new Cuenta();
        c.setIdCuenta(rs.getInt("id_cuenta"));
        c.setNumeroMesa(rs.getInt("numero_mesa"));
        c.setDpiMesero(rs.getString("dpi_mesero"));
        c.setNombreMesero(rs.getString("nombre_completo"));
        c.setHoraApertura(rs.getTimestamp("hora_apertura").toLocalDateTime());
        c.setEstado(rs.getString("estado"));
        c.setTotal(rs.getDouble("total"));
        c.setPropina(rs.getDouble("propina"));
        return c;
    }
}