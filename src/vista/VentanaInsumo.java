package vista;

import dao.InsumoDAO;
import modelo.Insumo;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class VentanaInsumo extends JInternalFrame {

    private InsumoDAO dao = new InsumoDAO();
    private JTable tabla;
    private DefaultTableModel modeloTabla;

    private JTextField txtCodigo, txtNombre, txtUnidad, txtStockActual, txtStockMinimo, txtCosto;

    public VentanaInsumo() {
        super("Gestión de Inventario", true, true, true, true);
        setSize(800, 500);
        setLayout(new BorderLayout());

        add(crearPanelFormulario(), BorderLayout.NORTH);
        add(crearPanelTabla(), BorderLayout.CENTER);
        add(crearPanelBotones(), BorderLayout.SOUTH);

        cargarTabla();
    }

    private JPanel crearPanelFormulario() {
        JPanel panel = new JPanel(new GridLayout(3, 4, 5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Datos del insumo"));

        panel.add(new JLabel("Código (vacío = nuevo):"));
        txtCodigo = new JTextField();
        panel.add(txtCodigo);

        panel.add(new JLabel("Nombre:"));
        txtNombre = new JTextField();
        panel.add(txtNombre);

        panel.add(new JLabel("Unidad de medida:"));
        txtUnidad = new JTextField();
        panel.add(txtUnidad);

        panel.add(new JLabel("Stock actual (solo al crear):"));
        txtStockActual = new JTextField();
        panel.add(txtStockActual);

        panel.add(new JLabel("Stock mínimo:"));
        txtStockMinimo = new JTextField();
        panel.add(txtStockMinimo);

        panel.add(new JLabel("Costo:"));
        txtCosto = new JTextField();
        panel.add(txtCosto);

        return panel;
    }

    private JScrollPane crearPanelTabla() {
        modeloTabla = new DefaultTableModel(
            new String[]{"Código", "Nombre", "Unidad", "Stock Actual", "Stock Mínimo", "Costo", "Alerta"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        tabla = new JTable(modeloTabla);
        tabla.getSelectionModel().addListSelectionListener(e -> cargarSeleccionEnFormulario());
        return new JScrollPane(tabla);
    }

    private JPanel crearPanelBotones() {
        JPanel panel = new JPanel();

        JButton btnGuardar = new JButton("Guardar Nuevo");
        btnGuardar.addActionListener(e -> guardarInsumo());

        JButton btnActualizar = new JButton("Actualizar");
        btnActualizar.addActionListener(e -> actualizarInsumo());

        JButton btnComprar = new JButton("Registrar Compra");
        btnComprar.addActionListener(e -> registrarCompra());

        JButton btnLimpiar = new JButton("Limpiar");
        btnLimpiar.addActionListener(e -> limpiarFormulario());

        panel.add(btnGuardar);
        panel.add(btnActualizar);
        panel.add(btnComprar);
        panel.add(btnLimpiar);
        return panel;
    }

    private void guardarInsumo() {
        try {
            Insumo ins = new Insumo(
                0,
                txtNombre.getText().trim(),
                txtUnidad.getText().trim(),
                Double.parseDouble(txtStockActual.getText().trim()),
                Double.parseDouble(txtStockMinimo.getText().trim()),
                Double.parseDouble(txtCosto.getText().trim())
            );
            if (dao.insertar(ins)) {
                JOptionPane.showMessageDialog(this, "Insumo registrado.");
                cargarTabla();
                limpiarFormulario();
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Stock y costo deben ser números válidos.");
        }
    }

    private void actualizarInsumo() {
        if (txtCodigo.getText().isBlank()) {
            JOptionPane.showMessageDialog(this, "Selecciona un insumo de la tabla primero.");
            return;
        }
        try {
            Insumo ins = new Insumo(
                Integer.parseInt(txtCodigo.getText().trim()),
                txtNombre.getText().trim(),
                txtUnidad.getText().trim(),
                0,
                Double.parseDouble(txtStockMinimo.getText().trim()),
                Double.parseDouble(txtCosto.getText().trim())
            );
            dao.actualizar(ins);
            JOptionPane.showMessageDialog(this, "Insumo actualizado.");
            cargarTabla();
            limpiarFormulario();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Stock mínimo y costo deben ser números válidos.");
        }
    }

    private void registrarCompra() {
        if (txtCodigo.getText().isBlank()) {
            JOptionPane.showMessageDialog(this, "Selecciona un insumo de la tabla primero.");
            return;
        }
        String cantidadStr = JOptionPane.showInputDialog(this, "Cantidad comprada:");
        String costoStr = JOptionPane.showInputDialog(this, "Costo total de la compra:");
        if (cantidadStr == null || costoStr == null) return;

        try {
            int codigo = Integer.parseInt(txtCodigo.getText().trim());
            double cantidad = Double.parseDouble(cantidadStr.trim());
            double costoTotal = Double.parseDouble(costoStr.trim());

            if (dao.registrarCompra(codigo, cantidad, costoTotal)) {
                JOptionPane.showMessageDialog(this, "Compra registrada y stock actualizado.");
                cargarTabla();
                limpiarFormulario();
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo registrar la compra.");
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Cantidad y costo deben ser números válidos.");
        }
    }

    private void cargarTabla() {
        modeloTabla.setRowCount(0);
        List<Insumo> lista = dao.listarTodos();
        for (Insumo i : lista) {
            modeloTabla.addRow(new Object[]{
                i.getCodigoInsumo(), i.getNombre(), i.getUnidadMedida(),
                i.getStockActual(), i.getStockMinimo(), i.getCosto(),
                i.isStockBajo() ? "⚠ STOCK BAJO" : "OK"
            });
        }
    }

    private void cargarSeleccionEnFormulario() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) return;
        txtCodigo.setText(modeloTabla.getValueAt(fila, 0).toString());
        txtNombre.setText(modeloTabla.getValueAt(fila, 1).toString());
        txtUnidad.setText(modeloTabla.getValueAt(fila, 2).toString());
        txtStockMinimo.setText(modeloTabla.getValueAt(fila, 4).toString());
        txtCosto.setText(modeloTabla.getValueAt(fila, 5).toString());
    }

    private void limpiarFormulario() {
        txtCodigo.setText("");
        txtNombre.setText("");
        txtUnidad.setText("");
        txtStockActual.setText("");
        txtStockMinimo.setText("");
        txtCosto.setText("");
        tabla.clearSelection();
    }
}