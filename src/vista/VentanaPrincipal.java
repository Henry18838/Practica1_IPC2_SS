package vista;

import javax.swing.*;
import java.awt.Color;
import java.awt.Font;

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
        new dao.NominaDAO().generarNominasAutomaticas();
    }

    private void crearBarraMenu() {
        JMenuBar menuBar = new JMenuBar();

        JMenu menuPersonal = new JMenu("Personal");
        agregarItem(menuPersonal, "Gestionar Empleados", e -> abrirVentanaEmpleado());
        agregarItem(menuPersonal, "Nóminas", e -> abrirVentanaNomina());

        JMenu menuInventario = new JMenu("Inventario");
        agregarItem(menuInventario, "Gestionar Insumos", e -> abrirVentanaInsumo());

        JMenu menuMenu = new JMenu("Menú");
        agregarItem(menuMenu, "Gestionar Productos", e -> abrirVentanaProducto());

        JMenu menuMesas = new JMenu("Mesas");
        agregarItem(menuMesas, "Control de Mesas", e -> abrirVentanaMesa());

        JMenu menuCuentas = new JMenu("Cuentas");
        agregarItem(menuCuentas, "Gestionar Cuentas", e -> abrirVentanaCuenta());

        JMenu menuReportes = new JMenu("Reportes");
        agregarItem(menuReportes, "Flujo de Caja", e -> abrirVentanaReporte());
        agregarItem(menuReportes, "Productos Más Vendidos", e -> abrirVentanaReporte());
        agregarItem(menuReportes, "Insumos con Bajo Stock", e -> abrirVentanaReporte());

        menuBar.add(menuPersonal);
        menuBar.add(menuInventario);
        menuBar.add(menuMenu);
        menuBar.add(menuMesas);
        menuBar.add(menuCuentas);
        menuBar.add(menuReportes);

        menuBar.setBorder(BorderFactory.createEmptyBorder(8, 5, 8, 5)); // arriba, izquierda, abajo, derecha
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

    private void abrirVentanaProducto() {
        VentanaProducto vp = new VentanaProducto();
        escritorio.add(vp);
        vp.setVisible(true);
    }

    private void abrirVentanaMesa() {
        VentanaMesa vm = new VentanaMesa();
        escritorio.add(vm);
        vm.setVisible(true);
    }

    private void abrirVentanaCuenta() {
        VentanaCuenta vc = new VentanaCuenta();
        escritorio.add(vc);
        vc.setVisible(true);
    }

    private void abrirVentanaNomina() {
        VentanaNomina vn = new VentanaNomina();
        escritorio.add(vn);
        vn.setVisible(true);
    }

    private void abrirVentanaReporte() {
        VentanaReporte vr = new VentanaReporte();
        escritorio.add(vr);
        vr.setVisible(true);
    }

    public JDesktopPane getEscritorio() {
        return escritorio;
    }

    public static void main(String[] args) {
        try {
            UIManager.put("nimbusBase", new Color(42, 34, 29)); // café muy oscuro
            UIManager.put("nimbusBlueGrey", new Color(75, 62, 53)); // café medio para bordes
            UIManager.put("control", new Color(33, 27, 23)); // fondo general casi negro cálido
            UIManager.put("nimbusLightBackground", new Color(50, 41, 35)); // fondo de tablas/campos de texto (oscuro
                                                                           // también)
            UIManager.put("text", new Color(232, 217, 200)); // texto crema, buen contraste
            UIManager.put("nimbusSelectionBackground", new Color(196, 120, 48)); // ámbar/terracota para selección
            UIManager.put("nimbusFocus", new Color(196, 120, 48));
            UIManager.put("nimbusOrange", new Color(196, 120, 48));
            UIManager.put("nimbusSelectedText", new Color(255, 255, 255)); // texto blanco cuando algo está seleccionado
            UIManager.put("info", new Color(50, 41, 35)); // tooltips oscuros también
            UIManager.put("MenuBar:Menu[Enabled].textForeground", new Color(232, 217, 200));
            UIManager.put("MenuBar:Menu[MouseOver].textForeground", new Color(255, 255, 255));
            UIManager.put("MenuBar:Menu[Selected].textForeground", new Color(255, 255, 255));
            UIManager.put("Menu[Enabled].textForeground", new Color(232, 217, 200));
            UIManager.put("MenuItem[Enabled].textForeground", new Color(232, 217, 200));
            UIManager.put("MenuItem[MouseOver].textForeground", new Color(255, 255, 255));
            UIManager.put("MenuItem[MouseOver].background", new Color(196, 120, 48));
            UIManager.put("PopupMenu[Enabled].background", new Color(50, 41, 35));
            UIManager.put("PopupMenu[Enabled].border",
                    javax.swing.BorderFactory.createLineBorder(new Color(75, 62, 53)));
            UIManager.put("MenuBar.font", new Font("Segoe UI", Font.BOLD, 16));
            UIManager.put("Menu.font", new Font("Segoe UI", Font.BOLD, 16));
            UIManager.put("MenuItem.font", new Font("Segoe UI", Font.PLAIN, 14));

            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println("No se pudo aplicar Nimbus, se usará el look and feel por defecto.");
        }

        SwingUtilities.invokeLater(() -> {
            VentanaPrincipal ventana = new VentanaPrincipal();
            ventana.setVisible(true);
        });
    }
}