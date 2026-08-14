package vista;

import dao.CuentaDAO;
import dao.MesaDAO;
import dao.EmpleadoDAO;
import dao.ProductoDAO;
import modelo.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class VentanaCuenta extends JInternalFrame {

    private CuentaDAO cuentaDao = new CuentaDAO();
    private MesaDAO mesaDao = new MesaDAO();
    private EmpleadoDAO empleadoDao = new EmpleadoDAO();
    private ProductoDAO productoDao = new ProductoDAO();

    private JComboBox<Mesa> cmbMesa;
    private JComboBox<Empleado> cmbMesero;

    private JTable tablaCuentasAbiertas;
    private DefaultTableModel modeloCuentasAbiertas;

    private JTable tablaDetalle;
    private DefaultTableModel modeloDetalle;
    private JComboBox<Producto> cmbProducto;
    private JTextField txtCantidad;
    private JLabel lblTotal;

    private Cuenta cuentaSeleccionada;

    public VentanaCuenta() {
        super("Gestión de Cuentas", true, true, true, true);
        setSize(950, 650);
        setLayout(new BorderLayout());

        add(crearPanelAbrirCuenta(), BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, crearPanelCuentasAbiertas(), crearPanelDetalle());
        split.setDividerLocation(200);
        add(split, BorderLayout.CENTER);

        refrescarTodo();
    }

    private JPanel crearPanelAbrirCuenta() {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createTitledBorder("Abrir nueva cuenta"));

        cmbMesa = new JComboBox<>();
        cmbMesa.setRenderer(new RendererGenerico());

        cmbMesero = new JComboBox<>();
        cmbMesero.setRenderer(new RendererGenerico());

        JButton btnAbrir = new JButton("Abrir Cuenta");
        btnAbrir.addActionListener(e -> abrirCuenta());

        panel.add(new JLabel("Mesa libre:"));
        panel.add(cmbMesa);
        panel.add(new JLabel("Mesero:"));
        panel.add(cmbMesero);
        panel.add(btnAbrir);
        return panel;
    }

    private JScrollPane crearPanelCuentasAbiertas() {
        modeloCuentasAbiertas = new DefaultTableModel(new String[]{"ID", "Mesa", "Mesero", "Hora Apertura", "Total"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        tablaCuentasAbiertas = new JTable(modeloCuentasAbiertas);
        tablaCuentasAbiertas.getSelectionModel().addListSelectionListener(e -> seleccionarCuenta());
        JScrollPane scroll = new JScrollPane(tablaCuentasAbiertas);
        scroll.setBorder(BorderFactory.createTitledBorder("Cuentas abiertas"));
        return scroll;
    }

    private JPanel crearPanelDetalle() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Detalle de la cuenta seleccionada"));

        JPanel panelAgregar = new JPanel();
        cmbProducto = new JComboBox<>();
        cmbProducto.setRenderer(new RendererGenerico());
        txtCantidad = new JTextField(5);
        JButton btnAgregar = new JButton("Agregar Producto");
        btnAgregar.addActionListener(e -> agregarProducto());
        panelAgregar.add(new JLabel("Producto:"));
        panelAgregar.add(cmbProducto);
        panelAgregar.add(new JLabel("Cantidad:"));
        panelAgregar.add(txtCantidad);
        panelAgregar.add(btnAgregar);

        modeloDetalle = new DefaultTableModel(new String[]{"Producto", "Cantidad", "Subtotal"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        tablaDetalle = new JTable(modeloDetalle);

        JPanel panelInferior = new JPanel();
        lblTotal = new JLabel("Total: Q0.00");
        JButton btnCobrar = new JButton("Cobrar Cuenta");
        btnCobrar.addActionListener(e -> cobrarCuenta());
        panelInferior.add(lblTotal);
        panelInferior.add(btnCobrar);

        panel.add(panelAgregar, BorderLayout.NORTH);
        panel.add(new JScrollPane(tablaDetalle), BorderLayout.CENTER);
        panel.add(panelInferior, BorderLayout.SOUTH);
        return panel;
    }

    private void abrirCuenta() {
        Mesa mesa = (Mesa) cmbMesa.getSelectedItem();
        Empleado mesero = (Empleado) cmbMesero.getSelectedItem();
        if (mesa == null || mesero == null) {
            JOptionPane.showMessageDialog(this, "Selecciona una mesa y un mesero.");
            return;
        }
        int id = cuentaDao.abrirCuenta(mesa.getNumeroMesa(), mesero.getDpi());
        if (id != -1) {
            JOptionPane.showMessageDialog(this, "Cuenta abierta con ID " + id);
            refrescarTodo();
        } else {
            JOptionPane.showMessageDialog(this, "No se pudo abrir la cuenta.");
        }
    }

    private void seleccionarCuenta() {
        int fila = tablaCuentasAbiertas.getSelectedRow();
        if (fila == -1) return;
        int idCuenta = (int) modeloCuentasAbiertas.getValueAt(fila, 0);
        for (Cuenta c : cuentaDao.listarCuentasAbiertas()) {
            if (c.getIdCuenta() == idCuenta) {
                cuentaSeleccionada = c;
                break;
            }
        }
        cargarDetalle();
    }

    private void cargarDetalle() {
        modeloDetalle.setRowCount(0);
        if (cuentaSeleccionada == null) return;
        List<DetalleCuenta> detalles = cuentaDao.listarDetalle(cuentaSeleccionada.getIdCuenta());
        for (DetalleCuenta d : detalles) {
            modeloDetalle.addRow(new Object[]{d.getNombreProducto(), d.getCantidad(), d.getSubtotal()});
        }
        lblTotal.setText("Total: Q" + String.format("%.2f", cuentaSeleccionada.getTotal()));
    }

    private void agregarProducto() {
        if (cuentaSeleccionada == null) {
            JOptionPane.showMessageDialog(this, "Selecciona una cuenta abierta primero.");
            return;
        }
        Producto producto = (Producto) cmbProducto.getSelectedItem();
        if (producto == null || txtCantidad.getText().isBlank()) {
            JOptionPane.showMessageDialog(this, "Selecciona un producto y una cantidad.");
            return;
        }
        try {
            int cantidad = Integer.parseInt(txtCantidad.getText().trim());
            boolean exito = cuentaDao.agregarProducto(cuentaSeleccionada.getIdCuenta(), producto.getCodigoProducto(),
                    cantidad, producto.getPrecioVenta());
            if (exito) {
                txtCantidad.setText("");
                // Recargar la cuenta seleccionada para traer el total actualizado
                for (Cuenta c : cuentaDao.listarCuentasAbiertas()) {
                    if (c.getIdCuenta() == cuentaSeleccionada.getIdCuenta()) {
                        cuentaSeleccionada = c;
                        break;
                    }
                }
                cargarDetalle();
                refrescarCuentasAbiertas();
            } else {
                JOptionPane.showMessageDialog(this, "⚠ Inventario insuficiente para preparar este producto.");
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "La cantidad debe ser un número entero.");
        }
    }

    private void cobrarCuenta() {
        if (cuentaSeleccionada == null) {
            JOptionPane.showMessageDialog(this, "Selecciona una cuenta abierta primero.");
            return;
        }
        String propinaStr = JOptionPane.showInputDialog(this, "Propina (0 si no aplica):", "0");
        if (propinaStr == null) return;
        try {
            double propina = Double.parseDouble(propinaStr.trim());
            boolean exito = cuentaDao.cobrarCuenta(cuentaSeleccionada.getIdCuenta(), cuentaSeleccionada.getNumeroMesa(), propina);
            if (exito) {
                JOptionPane.showMessageDialog(this, "Cuenta cobrada. Total: Q" + String.format("%.2f", cuentaSeleccionada.getTotal())
                        + " + propina Q" + String.format("%.2f", propina));
                cuentaSeleccionada = null;
                refrescarTodo();
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "La propina debe ser un número válido.");
        }
    }

    private void refrescarTodo() {
        // Mesas libres
        cmbMesa.removeAllItems();
        for (Mesa m : mesaDao.listarTodas()) {
            if ("LIBRE".equals(m.getEstado())) cmbMesa.addItem(m);
        }
        // Meseros habilitados
        cmbMesero.removeAllItems();
        for (Empleado e : empleadoDao.listarTodos()) {
            if ("MESERO".equals(e.getRol()) && e.isHabilitado()) cmbMesero.addItem(e);
        }
        // Productos del menú
        cmbProducto.removeAllItems();
        for (Producto p : productoDao.listarTodos()) {
            cmbProducto.addItem(p);
        }
        refrescarCuentasAbiertas();
        modeloDetalle.setRowCount(0);
        lblTotal.setText("Total: Q0.00");
    }

    private void refrescarCuentasAbiertas() {
        modeloCuentasAbiertas.setRowCount(0);
        for (Cuenta c : cuentaDao.listarCuentasAbiertas()) {
            modeloCuentasAbiertas.addRow(new Object[]{
                c.getIdCuenta(), c.getNumeroMesa(), c.getNombreMesero(), c.getHoraApertura(), c.getTotal()
            });
        }
    }

    // Renderer genérico para mostrar el nombre correcto de Mesa, Empleado o Producto en los combos
    private static class RendererGenerico extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Mesa m) {
                setText("Mesa " + m.getNumeroMesa() + " (cap. " + m.getCapacidad() + ")");
            } else if (value instanceof Empleado e) {
                setText(e.getNombreCompleto());
            } else if (value instanceof Producto p) {
                setText(p.getNombre() + " - Q" + String.format("%.2f", p.getPrecioVenta()));
            }
            return this;
        }
    }
}