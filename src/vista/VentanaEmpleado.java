package vista;

import dao.EmpleadoDAO;
import modelo.Empleado;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;


public class VentanaEmpleado extends JInternalFrame {

    private EmpleadoDAO dao = new EmpleadoDAO();
    private JTable tabla;
    private DefaultTableModel modeloTabla;

    private JTextField txtDpi, txtNombre, txtSalario, txtCorreo, txtFecha;
    private JComboBox<String> cmbRol, cmbJornada;

    public VentanaEmpleado() {
        super("Gestión de Personal", true, true, true, true);
        setSize(750, 500);
        setLayout(new BorderLayout());

        add(crearPanelFormulario(), BorderLayout.NORTH);
        add(crearPanelTabla(), BorderLayout.CENTER);
        add(crearPanelBotones(), BorderLayout.SOUTH);

        cargarTabla();
    }

    private JPanel crearPanelFormulario() {
        JPanel panel = new JPanel(new GridLayout(3, 4, 5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Datos del empleado"));

        panel.add(new JLabel("DPI:"));
        txtDpi = new JTextField();
        panel.add(txtDpi);

        panel.add(new JLabel("Nombre completo:"));
        txtNombre = new JTextField();
        panel.add(txtNombre);

        panel.add(new JLabel("Rol:"));
        cmbRol = new JComboBox<>(new String[]{"MESERO", "COCINA", "BARISTA", "ADMINISTRADOR"});
        panel.add(cmbRol);

        panel.add(new JLabel("Jornada:"));
        cmbJornada = new JComboBox<>(new String[]{"MATUTINA", "VESPERTINA", "NOCTURNA"});
        panel.add(cmbJornada);

        panel.add(new JLabel("Salario:"));
        txtSalario = new JTextField();
        panel.add(txtSalario);

        panel.add(new JLabel("Correo:"));
        txtCorreo = new JTextField();
        panel.add(txtCorreo);

        panel.add(new JLabel("Fecha contratación (yyyy-mm-dd):"));
        txtFecha = new JTextField(LocalDate.now().toString());
        panel.add(txtFecha);

        return panel;
    }

    private JScrollPane crearPanelTabla() {
        modeloTabla = new DefaultTableModel(
            new String[]{"DPI", "Nombre", "Rol", "Jornada", "Salario", "Correo", "Habilitado"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        tabla = new JTable(modeloTabla);
        tabla.getSelectionModel().addListSelectionListener(e -> cargarSeleccionEnFormulario());
        return new JScrollPane(tabla);
    }

    private JPanel crearPanelBotones() {
        JPanel panel = new JPanel();

        JButton btnGuardar = new JButton("Guardar");
        btnGuardar.addActionListener(e -> guardarEmpleado());

        JButton btnActualizar = new JButton("Actualizar");
        btnActualizar.addActionListener(e -> actualizarEmpleado());

        JButton btnDeshabilitar = new JButton("Deshabilitar");
        btnDeshabilitar.addActionListener(e -> deshabilitarEmpleado());

        JButton btnLimpiar = new JButton("Limpiar");
        btnLimpiar.addActionListener(e -> limpiarFormulario());

        panel.add(btnGuardar);
        panel.add(btnActualizar);
        panel.add(btnDeshabilitar);
        panel.add(btnLimpiar);
        return panel;
    }

    private void guardarEmpleado() {
        try {
            if (txtDpi.getText().isBlank() || txtNombre.getText().isBlank() || txtCorreo.getText().isBlank()) {
                JOptionPane.showMessageDialog(this, "DPI, nombre y correo son obligatorios.");
                return;
            }
            if (dao.existeCorreo(txtCorreo.getText())) {
                JOptionPane.showMessageDialog(this, "Ese correo ya está registrado con otro empleado.");
                return;
            }

            Empleado emp = new Empleado(
                txtDpi.getText().trim(),
                txtNombre.getText().trim(),
                (String) cmbRol.getSelectedItem(),
                (String) cmbJornada.getSelectedItem(),
                Double.parseDouble(txtSalario.getText().trim()),
                LocalDate.parse(txtFecha.getText().trim()),
                txtCorreo.getText().trim(),
                true
            );

            if (dao.insertar(emp)) {
                JOptionPane.showMessageDialog(this, "Empleado registrado correctamente.");
                cargarTabla();
                limpiarFormulario();
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo registrar (¿DPI duplicado?).");
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "El salario debe ser un número válido.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void actualizarEmpleado() {
        if (txtDpi.getText().isBlank()) {
            JOptionPane.showMessageDialog(this, "Selecciona un empleado de la tabla primero.");
            return;
        }
        try {
            Empleado emp = new Empleado(
                txtDpi.getText().trim(),
                txtNombre.getText().trim(),
                (String) cmbRol.getSelectedItem(),
                (String) cmbJornada.getSelectedItem(),
                Double.parseDouble(txtSalario.getText().trim()),
                LocalDate.parse(txtFecha.getText().trim()),
                txtCorreo.getText().trim(),
                true
            );
            dao.actualizar(emp);
            JOptionPane.showMessageDialog(this, "Empleado actualizado.");
            cargarTabla();
            limpiarFormulario();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "El salario debe ser un número válido.");
        }
    }

    private void deshabilitarEmpleado() {
        if (txtDpi.getText().isBlank()) {
            JOptionPane.showMessageDialog(this, "Selecciona un empleado de la tabla primero.");
            return;
        }
        int confirmar = JOptionPane.showConfirmDialog(this, "¿Deshabilitar a este empleado?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirmar == JOptionPane.YES_OPTION) {
            dao.deshabilitar(txtDpi.getText().trim());
            cargarTabla();
            limpiarFormulario();
        }
    }

    private void cargarTabla() {
        modeloTabla.setRowCount(0);
        List<Empleado> lista = dao.listarTodos();
        for (Empleado e : lista) {
            modeloTabla.addRow(new Object[]{
                e.getDpi(), e.getNombreCompleto(), e.getRol(), e.getJornada(),
                e.getSalario(), e.getCorreo(), e.isHabilitado() ? "Sí" : "No"
            });
        }
    }

    private void cargarSeleccionEnFormulario() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) return;
        txtDpi.setText(modeloTabla.getValueAt(fila, 0).toString());
        txtNombre.setText(modeloTabla.getValueAt(fila, 1).toString());
        cmbRol.setSelectedItem(modeloTabla.getValueAt(fila, 2).toString());
        cmbJornada.setSelectedItem(modeloTabla.getValueAt(fila, 3).toString());
        txtSalario.setText(modeloTabla.getValueAt(fila, 4).toString());
        txtCorreo.setText(modeloTabla.getValueAt(fila, 5).toString());
    }

    private void limpiarFormulario() {
        txtDpi.setText("");
        txtNombre.setText("");
        txtSalario.setText("");
        txtCorreo.setText("");
        txtFecha.setText(LocalDate.now().toString());
        tabla.clearSelection();
    }
}