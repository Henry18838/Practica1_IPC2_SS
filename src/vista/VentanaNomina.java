package vista;

import dao.NominaDAO;
import modelo.Nomina;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class VentanaNomina extends JInternalFrame {

    private NominaDAO dao = new NominaDAO();
    private JTable tabla;
    private DefaultTableModel modelo;

    public VentanaNomina() {
        super("Gestión de Nóminas", true, true, true, true);
        setSize(800, 500);
        setLayout(new BorderLayout());

        add(crearPanelBotones(), BorderLayout.NORTH);
        add(crearPanelTabla(), BorderLayout.CENTER);

        cargarTabla();
    }

    private JPanel crearPanelBotones() {
        JPanel panel = new JPanel();

        JButton btnGenerarQuincena = new JButton("Generar Nómina QUINCENA (manual)");
        btnGenerarQuincena.addActionListener(e -> generar("QUINCENA"));

        JButton btnGenerarFinMes = new JButton("Generar Nómina FIN DE MES (manual)");
        btnGenerarFinMes.addActionListener(e -> generar("FIN_DE_MES"));

        JButton btnPagar = new JButton("Marcar Seleccionado como PAGADO");
        btnPagar.addActionListener(e -> marcarPagado());

        panel.add(btnGenerarQuincena);
        panel.add(btnGenerarFinMes);
        panel.add(btnPagar);
        return panel;
    }

    private JScrollPane crearPanelTabla() {
        modelo = new DefaultTableModel(new String[]{"Código", "Empleado", "Fecha Emisión", "Tipo", "Monto", "Estado"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        tabla = new JTable(modelo);
        return new JScrollPane(tabla);
    }

    private void generar(String tipoPago) {
        dao.generarNominaManual(tipoPago);
        JOptionPane.showMessageDialog(this, "Nóminas de " + tipoPago + " generadas (solo para empleados que aún no tenían una este período).");
        cargarTabla();
    }

    private void marcarPagado() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona una nómina de la tabla primero.");
            return;
        }
        int codigo = (int) modelo.getValueAt(fila, 0);
        dao.marcarPagado(codigo);
        cargarTabla();
    }

    private void cargarTabla() {
        modelo.setRowCount(0);
        List<Nomina> lista = dao.listarTodas();
        for (Nomina n : lista) {
            modelo.addRow(new Object[]{
                n.getCodigoNomina(), n.getNombreEmpleado(), n.getFechaEmision(),
                n.getTipoPago(), n.getMonto(), n.getEstado()
            });
        }
    }
}