package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionBD {

    private static final String URL = "jdbc:mysql://localhost:3306/javabeans_cafe";
    private static final String USUARIO = "root";
    private static final String CONTRASENA = "champet2020";

    private static Connection conexion = null;

    public static Connection obtenerConexion() {
        try {
            if (conexion == null || conexion.isClosed()) {
                conexion = DriverManager.getConnection(URL, USUARIO, CONTRASENA);
                System.out.println("Conexión establecida con javabeans_cafe");
            }
        } catch (SQLException e) {
            System.err.println("Error al conectar con la base de datos: " + e.getMessage());
        }
        return conexion;
    }

    public static void cerrarConexion() {
        try {
            if (conexion != null && !conexion.isClosed()) {
                conexion.close();
                System.out.println("Conexión cerrada");
            }
        } catch (SQLException e) {
            System.err.println("Error al cerrar la conexión: " + e.getMessage());
        }
    }

    // Clase de prueba rápida
    public static void main(String[] args) {
        Connection con = obtenerConexion();
        if (con != null) {
            System.out.println("¡Conexión exitosa!");
        } else {
            System.out.println("No se pudo conectar.");
        }
        cerrarConexion();
    }
}