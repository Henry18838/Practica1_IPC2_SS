package vista;

import javax.swing.*;

public class VentanaPrincipal extends JFrame {

    private JDesktopPane escritorio;

    public VentanaPrincipal() {
        setTitle("JavaBeans Café - Sistema de Administración");
        setSize(1100, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        escritorio = new JDesktopPane();
        setContentPane(escritorio);

        crearBarraMenu();
    }

    private void crearBarraMenu() {
        JMenuBar menuBar = new JMenuBar();

        JMenu menuPersonal = new JMenu("Personal");
        agregarItem(menuPersonal, "Gestionar Empleados", e -> abrirVentanaEmpleado());
        agregarItem(menuPersonal, "Nóminas", e -> abrirPendiente("Gestión de Nóminas"));

        JMenu menuInventario = new JMenu("Inventario");
        agregarItem(menuInventario, "Gestionar Insumos", e -> abrirVentanaInsumo());

        JMenu menuMenu = new JMenu("Menú");
        agregarItem(menuMenu, "Gestionar Productos", e -> abrirPendiente("Gestión de Menú"));

        JMenu menuMesas = new JMenu("Mesas");
        agregarItem(menuMesas, "Control de Mesas", e -> abrirPendiente("Control de Mesas"));

        JMenu menuCuentas = new JMenu("Cuentas");
        agregarItem(menuCuentas, "Gestionar Cuentas", e -> abrirPendiente("Gestión de Cuentas"));

        JMenu menuReportes = new JMenu("Reportes");
        agregarItem(menuReportes, "Flujo de Caja", e -> abrirPendiente("Reporte de Flujo de Caja"));
        agregarItem(menuReportes, "Productos Más Vendidos", e -> abrirPendiente("Reporte de Productos"));
        agregarItem(menuReportes, "Insumos con Bajo Stock", e -> abrirPendiente("Reporte de Bajo Stock"));

        menuBar.add(menuPersonal);
        menuBar.add(menuInventario);
        menuBar.add(menuMenu);
        menuBar.add(menuMesas);
        menuBar.add(menuCuentas);
        menuBar.add(menuReportes);

        setJMenuBar(menuBar);
    }

    private void agregarItem(JMenu menu, String texto, java.awt.event.ActionListener listener) {
        JMenuItem item = new JMenuItem(texto);
        item.addActionListener(listener);
        menu.add(item);
    }

    private void abrirVentanaEmpleado() {
        VentanaEmpleado ve = new VentanaEmpleado();
        escritorio.add(ve);
        ve.setVisible(true);
    }

    // Método temporal para los módulos que aún no hemos construido
    private void abrirPendiente(String modulo) {
        JOptionPane.showMessageDialog(this, "Módulo pendiente de implementar: " + modulo);
    }

    private void abrirVentanaInsumo() {
        VentanaInsumo vi = new VentanaInsumo();
        escritorio.add(vi);
        vi.setVisible(true);
    }

    public JDesktopPane getEscritorio() {
        return escritorio;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            VentanaPrincipal ventana = new VentanaPrincipal();
            ventana.setVisible(true);
        });
    }
}