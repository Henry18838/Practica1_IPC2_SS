package dao;

import modelo.Mesa;
import util.ConexionBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MesaDAO {

    public boolean insertar(Mesa m) {
        String sql = "INSERT INTO mesa (numero_mesa, capacidad, estado) VALUES (?, ?, 'LIBRE')";
        try (PreparedStatement ps = ConexionBD.obtenerConexion().prepareStatement(sql)) {
            ps.setInt(1, m.getNumeroMesa());
            ps.setInt(2, m.getCapacidad());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error al insertar mesa: " + e.getMessage());
            return false;
        }
    }

    public boolean actualizarCapacidad(int numeroMesa, int capacidad) {
        String sql = "UPDATE mesa SET capacidad = ? WHERE numero_mesa = ?";
        try (PreparedStatement ps = ConexionBD.obtenerConexion().prepareStatement(sql)) {
            ps.setInt(1, capacidad);
            ps.setInt(2, numeroMesa);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error al actualizar mesa: " + e.getMessage());
            return false;
        }
    }

    public boolean cambiarEstado(int numeroMesa, String nuevoEstado) {
        String sql = "UPDATE mesa SET estado = ? WHERE numero_mesa = ?";
        try (PreparedStatement ps = ConexionBD.obtenerConexion().prepareStatement(sql)) {
            ps.setString(1, nuevoEstado);
            ps.setInt(2, numeroMesa);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error al cambiar estado de mesa: " + e.getMessage());
            return false;
        }
    }

    public List<Mesa> listarTodas() {
        List<Mesa> lista = new ArrayList<>();
        String sql = "SELECT * FROM mesa ORDER BY numero_mesa";
        try (Statement st = ConexionBD.obtenerConexion().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new Mesa(rs.getInt("numero_mesa"), rs.getInt("capacidad"), rs.getString("estado")));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar mesas: " + e.getMessage());
        }
        return lista;
    }
}
