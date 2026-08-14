package vista;

import dao.ReporteDAO;
import dao.ReporteDAO.ResultadoFlujoCaja;
import dao.ReporteDAO.ProductoVendido;
import dao.ReporteDAO.InsumoBajoStock;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.List;

public class VentanaReporte extends JInternalFrame {

    private ReporteDAO dao = new ReporteDAO();

    private JTextField txtDesde, txtHasta;

    // Flujo de caja
    private JLabel lblIngresos, lblEgresosNomina, lblEgresosCompras, lblBalance;

    // Productos más vendidos
    private DefaultTableModel modeloProductos;

    // Bajo stock
    private DefaultTableModel modeloBajoStock;

    public VentanaReporte() {
        super("Reportes", true, true, true, true);
        setSize(750, 550);
        setLayout(new BorderLayout());

        add(crearPanelFiltro(), BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Flujo de Caja", crearPanelFlujoCaja());
        tabs.addTab("Productos Más Vendidos", crearPanelProductos());
        tabs.addTab("Insumos Bajo Stock", crearPanelBajoStock());
        add(tabs, BorderLayout.CENTER);
    }

    private JPanel crearPanelFiltro() {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createTitledBorder("Filtro de fechas (vacío = incluir todo)"));

        txtDesde = new JTextField(10);
        txtHasta = new JTextField(10);

        JButton btnGenerar = new JButton("Generar Reportes");
        btnGenerar.addActionListener(e -> generarTodos());

        panel.add(new JLabel("Desde (yyyy-mm-dd):"));
        panel.add(txtDesde);
        panel.add(new JLabel("Hasta (yyyy-mm-dd):"));
        panel.add(txtHasta);
        panel.add(btnGenerar);
        return panel;
    }

    private JPanel crearPanelFlujoCaja() {
        JPanel panel = new JPanel(new GridLayout(5, 1, 5, 10));
        lblIngresos = new JLabel("Total ingresos: Q0.00");
        lblEgresosNomina = new JLabel("Total egresos (nóminas pagadas): Q0.00");
        lblEgresosCompras = new JLabel("Total egresos (compras de insumos): Q0.00");
        lblBalance = new JLabel("BALANCE: Q0.00");
        lblBalance.setFont(lblBalance.getFont().deriveFont(Font.BOLD, 16f));

        JButton btnExportar = new JButton("Exportar Flujo de Caja a HTML");
        btnExportar.addActionListener(e -> exportarFlujoCaja());

        panel.add(lblIngresos);
        panel.add(lblEgresosNomina);
        panel.add(lblEgresosCompras);
        panel.add(lblBalance);
        panel.add(btnExportar);
        return panel;
    }

    private JPanel crearPanelProductos() {
        JPanel panel = new JPanel(new BorderLayout());
        modeloProductos = new DefaultTableModel(new String[]{"Producto", "Cantidad Vendida"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable tabla = new JTable(modeloProductos);
        JButton btnExportar = new JButton("Exportar a HTML");
        btnExportar.addActionListener(e -> exportarProductos());

        panel.add(new JScrollPane(tabla), BorderLayout.CENTER);
        panel.add(btnExportar, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel crearPanelBajoStock() {
        JPanel panel = new JPanel(new BorderLayout());
        modeloBajoStock = new DefaultTableModel(new String[]{"Insumo", "Stock Actual", "Stock Mínimo"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable tabla = new JTable(modeloBajoStock);
        JButton btnExportar = new JButton("Exportar a HTML");
        btnExportar.addActionListener(e -> exportarBajoStock());

        panel.add(new JScrollPane(tabla), BorderLayout.CENTER);
        panel.add(btnExportar, BorderLayout.SOUTH);
        return panel;
    }

    private LocalDate parsearFecha(String texto) {
        if (texto == null || texto.isBlank()) return null;
        try {
            return LocalDate.parse(texto.trim());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Formato de fecha inválido, usa yyyy-mm-dd. Se ignorará ese filtro.");
            return null;
        }
    }

    private void generarTodos() {
        LocalDate desde = parsearFecha(txtDesde.getText());
        LocalDate hasta = parsearFecha(txtHasta.getText());

        // Flujo de caja
        ResultadoFlujoCaja r = dao.flujoCaja(desde, hasta);
        lblIngresos.setText("Total ingresos: Q" + String.format("%.2f", r.totalIngresos));
        lblEgresosNomina.setText("Total egresos (nóminas pagadas): Q" + String.format("%.2f", r.totalEgresosNomina));
        lblEgresosCompras.setText("Total egresos (compras de insumos): Q" + String.format("%.2f", r.totalEgresosCompras));
        lblBalance.setText((r.balance >= 0 ? "GANANCIA: Q" : "PÉRDIDA: Q") + String.format("%.2f", Math.abs(r.balance)));
        lblBalance.setForeground(r.balance >= 0 ? new Color(0, 128, 0) : Color.RED);

        // Productos más vendidos
        modeloProductos.setRowCount(0);
        for (ProductoVendido pv : dao.productosMasVendidos(desde, hasta)) {
            modeloProductos.addRow(new Object[]{pv.nombre, pv.cantidadVendida});
        }

        // Bajo stock (no usa filtro de fecha, es una foto del momento actual)
        modeloBajoStock.setRowCount(0);
        for (InsumoBajoStock i : dao.insumosBajoStock()) {
            modeloBajoStock.addRow(new Object[]{i.nombre, i.stockActual, i.stockMinimo});
        }
    }

    private void guardarHtml(String contenido, String nombreSugerido) {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new java.io.File(nombreSugerido));
        int resultado = chooser.showSaveDialog(this);
        if (resultado != JFileChooser.APPROVE_OPTION) return;
        try {
            Files.writeString(chooser.getSelectedFile().toPath(), contenido);
            JOptionPane.showMessageDialog(this, "Reporte exportado correctamente.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al exportar: " + ex.getMessage());
        }
    }

    private String estiloBase(String titulo) {
        return "<html><head><meta charset='UTF-8'><title>" + titulo + "</title>" +
               "<style>body{font-family:Arial,sans-serif;background:#f5f0e8;padding:20px;}" +
               "table{border-collapse:collapse;width:100%;background:white;}" +
               "th,td{border:1px solid #ccc;padding:8px;text-align:left;}" +
               "th{background:#6f4e37;color:white;}" +
               "h1{color:#4b2e2b;}</style></head><body><h1>" + titulo + "</h1>";
    }

    private void exportarFlujoCaja() {
        StringBuilder html = new StringBuilder(estiloBase("Flujo de Caja - JavaBeans Café"));
        html.append("<p>").append(lblIngresos.getText()).append("</p>");
        html.append("<p>").append(lblEgresosNomina.getText()).append("</p>");
        html.append("<p>").append(lblEgresosCompras.getText()).append("</p>");
        html.append("<h2>").append(lblBalance.getText()).append("</h2>");
        html.append("</body></html>");
        guardarHtml(html.toString(), "flujo_caja.html");
    }

    private void exportarProductos() {
        StringBuilder html = new StringBuilder(estiloBase("Productos Más Vendidos - JavaBeans Café"));
        html.append("<table><tr><th>Producto</th><th>Cantidad Vendida</th></tr>");
        for (int i = 0; i < modeloProductos.getRowCount(); i++) {
            html.append("<tr><td>").append(modeloProductos.getValueAt(i, 0)).append("</td><td>")
                .append(modeloProductos.getValueAt(i, 1)).append("</td></tr>");
        }
        html.append("</table></body></html>");
        guardarHtml(html.toString(), "productos_mas_vendidos.html");
    }

    private void exportarBajoStock() {
        StringBuilder html = new StringBuilder(estiloBase("Insumos con Bajo Stock - JavaBeans Café"));
        html.append("<table><tr><th>Insumo</th><th>Stock Actual</th><th>Stock Mínimo</th></tr>");
        for (int i = 0; i < modeloBajoStock.getRowCount(); i++) {
            html.append("<tr><td>").append(modeloBajoStock.getValueAt(i, 0)).append("</td><td>")
                .append(modeloBajoStock.getValueAt(i, 1)).append("</td><td>")
                .append(modeloBajoStock.getValueAt(i, 2)).append("</td></tr>");
        }
        html.append("</table></body></html>");
        guardarHtml(html.toString(), "insumos_bajo_stock.html");
    }
}