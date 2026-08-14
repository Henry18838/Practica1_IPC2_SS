package vista;

import dao.ProductoDAO;
import dao.InsumoDAO;
import modelo.Producto;
import modelo.RecetaItem;
import modelo.Insumo;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class VentanaProducto extends JInternalFrame {

    private ProductoDAO dao = new ProductoDAO();
    private InsumoDAO insumoDao = new InsumoDAO();

    private JTable tablaProductos;
    private DefaultTableModel modeloProductos;

    private JTextField txtCodigo, txtNombre, txtPrecio;
    private JComboBox<String> cmbCategoria;
    private JLabel lblFoto;
    private byte[] fotoActual;

    private JTable tablaReceta;
    private DefaultTableModel modeloReceta;
    private JComboBox<Insumo> cmbInsumo;
    private JTextField txtCantidadReceta;
    private List<RecetaItem> recetaEnEdicion = new ArrayList<>();

    public VentanaProducto() {
        super("Gestión de Menú", true, true, true, true);
        setSize(950, 650);
        setLayout(new BorderLayout());

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, crearPanelFormulario(), crearPanelTabla());
        split.setDividerLocation(320);
        add(split, BorderLayout.CENTER);
        add(crearPanelBotonesGlobales(), BorderLayout.SOUTH);

        cargarTablaProductos();
    }

    private JPanel crearPanelFormulario() {
        JPanel panelGeneral = new JPanel(new BorderLayout());

        // --- Datos del producto ---
        JPanel panelDatos = new JPanel(new GridLayout(2, 4, 5, 5));
        panelDatos.setBorder(BorderFactory.createTitledBorder("Datos del producto"));

        panelDatos.add(new JLabel("Código (vacío = nuevo):"));
        txtCodigo = new JTextField();
        panelDatos.add(txtCodigo);

        panelDatos.add(new JLabel("Nombre:"));
        txtNombre = new JTextField();
        panelDatos.add(txtNombre);

        panelDatos.add(new JLabel("Categoría:"));
        cmbCategoria = new JComboBox<>(new String[]{"BEBIDA_CALIENTE", "BEBIDA_FRIA", "POSTRE", "COMIDA"});
        panelDatos.add(cmbCategoria);

        panelDatos.add(new JLabel("Precio de venta:"));
        txtPrecio = new JTextField();
        panelDatos.add(txtPrecio);

        // --- Foto ---
        JPanel panelFoto = new JPanel();
        lblFoto = new JLabel("Sin foto");
        lblFoto.setPreferredSize(new Dimension(100, 100));
        lblFoto.setBorder(BorderFactory.createEtchedBorder());
        JButton btnCargarFoto = new JButton("Cargar Foto...");
        btnCargarFoto.addActionListener(e -> cargarFoto());
        panelFoto.add(lblFoto);
        panelFoto.add(btnCargarFoto);

        JPanel panelSuperior = new JPanel(new BorderLayout());
        panelSuperior.add(panelDatos, BorderLayout.CENTER);
        panelSuperior.add(panelFoto, BorderLayout.EAST);

        // --- Receta ---
        JPanel panelReceta = new JPanel(new BorderLayout());
        panelReceta.setBorder(BorderFactory.createTitledBorder("Receta (insumos que usa este producto)"));

        JPanel panelAgregarReceta = new JPanel();
        cmbInsumo = new JComboBox<>();
        cargarComboInsumos();
        txtCantidadReceta = new JTextField(6);
        JButton btnAgregarReceta = new JButton("Agregar a receta");
        btnAgregarReceta.addActionListener(e -> agregarItemReceta());
        panelAgregarReceta.add(new JLabel("Insumo:"));
        panelAgregarReceta.add(cmbInsumo);
        panelAgregarReceta.add(new JLabel("Cantidad:"));
        panelAgregarReceta.add(txtCantidadReceta);
        panelAgregarReceta.add(btnAgregarReceta);

        modeloReceta = new DefaultTableModel(new String[]{"Insumo", "Cantidad"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        tablaReceta = new JTable(modeloReceta);
        JButton btnQuitarReceta = new JButton("Quitar seleccionado");
        btnQuitarReceta.addActionListener(e -> quitarItemReceta());

        panelReceta.add(panelAgregarReceta, BorderLayout.NORTH);
        panelReceta.add(new JScrollPane(tablaReceta), BorderLayout.CENTER);
        panelReceta.add(btnQuitarReceta, BorderLayout.SOUTH);

        panelGeneral.add(panelSuperior, BorderLayout.NORTH);
        panelGeneral.add(panelReceta, BorderLayout.CENTER);
        return panelGeneral;
    }

    private JScrollPane crearPanelTabla() {
        modeloProductos = new DefaultTableModel(new String[]{"Código", "Nombre", "Categoría", "Precio"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        tablaProductos = new JTable(modeloProductos);
        tablaProductos.getSelectionModel().addListSelectionListener(e -> cargarSeleccion());
        JScrollPane scroll = new JScrollPane(tablaProductos);
        scroll.setBorder(BorderFactory.createTitledBorder("Productos del menú"));
        return scroll;
    }

    private JPanel crearPanelBotonesGlobales() {
        JPanel panel = new JPanel();

        JButton btnGuardar = new JButton("Guardar Nuevo Producto");
        btnGuardar.addActionListener(e -> guardarProducto());

        JButton btnActualizar = new JButton("Actualizar Producto");
        btnActualizar.addActionListener(e -> actualizarProducto());

        JButton btnGuardarReceta = new JButton("Guardar Receta");
        btnGuardarReceta.addActionListener(e -> guardarReceta());

        JButton btnLimpiar = new JButton("Limpiar");
        btnLimpiar.addActionListener(e -> limpiarFormulario());

        JButton btnExportar = new JButton("Exportar Menú a HTML");
        btnExportar.addActionListener(e -> exportarMenuHtml());

        panel.add(btnGuardar);
        panel.add(btnActualizar);
        panel.add(btnGuardarReceta);
        panel.add(btnLimpiar);
        panel.add(btnExportar);
        return panel;
    }

    private void cargarComboInsumos() {
        cmbInsumo.removeAllItems();
        for (Insumo i : insumoDao.listarTodos()) {
            cmbInsumo.addItem(i);
        }
        cmbInsumo.setRenderer(new DefaultListCellRendererInsumo());
    }

    // Renderer simple para mostrar el nombre del insumo en vez de su toString por defecto
    private static class DefaultListCellRendererInsumo extends javax.swing.DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Insumo) {
                setText(((Insumo) value).getNombre());
            }
            return this;
        }
    }

    private void cargarFoto() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Imágenes", "jpg", "jpeg", "png"));
        int resultado = chooser.showOpenDialog(this);
        if (resultado == JFileChooser.APPROVE_OPTION) {
            try {
                File archivo = chooser.getSelectedFile();
                fotoActual = Files.readAllBytes(archivo.toPath());
                mostrarFotoEnLabel(fotoActual);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al cargar la imagen: " + ex.getMessage());
            }
        }
    }

    private void mostrarFotoEnLabel(byte[] datos) {
        if (datos == null) {
            lblFoto.setIcon(null);
            lblFoto.setText("Sin foto");
            return;
        }
        ImageIcon icono = new ImageIcon(datos);
        Image escalada = icono.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
        lblFoto.setIcon(new ImageIcon(escalada));
        lblFoto.setText("");
    }

    private void agregarItemReceta() {
        Insumo seleccionado = (Insumo) cmbInsumo.getSelectedItem();
        if (seleccionado == null || txtCantidadReceta.getText().isBlank()) {
            JOptionPane.showMessageDialog(this, "Selecciona un insumo y una cantidad.");
            return;
        }
        try {
            double cantidad = Double.parseDouble(txtCantidadReceta.getText().trim());
            RecetaItem item = new RecetaItem(seleccionado.getCodigoInsumo(), seleccionado.getNombre(), cantidad);
            recetaEnEdicion.add(item);
            modeloReceta.addRow(new Object[]{item.getNombreInsumo(), item.getCantidadRequerida()});
            txtCantidadReceta.setText("");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "La cantidad debe ser un número válido.");
        }
    }

    private void quitarItemReceta() {
        int fila = tablaReceta.getSelectedRow();
        if (fila == -1) return;
        recetaEnEdicion.remove(fila);
        modeloReceta.removeRow(fila);
    }

    private void guardarProducto() {
        try {
            if (txtNombre.getText().isBlank() || txtPrecio.getText().isBlank()) {
                JOptionPane.showMessageDialog(this, "Nombre y precio son obligatorios.");
                return;
            }
            Producto p = new Producto(0, txtNombre.getText().trim(), (String) cmbCategoria.getSelectedItem(),
                    Double.parseDouble(txtPrecio.getText().trim()), fotoActual);
            int codigoGenerado = dao.insertar(p);
            if (codigoGenerado != -1) {
                JOptionPane.showMessageDialog(this, "Producto registrado con código " + codigoGenerado + ". Ahora puedes agregarle la receta.");
                txtCodigo.setText(String.valueOf(codigoGenerado));
                if (!recetaEnEdicion.isEmpty()) {
                    dao.guardarReceta(codigoGenerado, recetaEnEdicion);
                }
                cargarTablaProductos();
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo registrar el producto.");
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "El precio debe ser un número válido.");
        }
    }

    private void actualizarProducto() {
        if (txtCodigo.getText().isBlank()) {
            JOptionPane.showMessageDialog(this, "Selecciona un producto de la tabla primero.");
            return;
        }
        try {
            Producto p = new Producto(Integer.parseInt(txtCodigo.getText().trim()), txtNombre.getText().trim(),
                    (String) cmbCategoria.getSelectedItem(), Double.parseDouble(txtPrecio.getText().trim()), fotoActual);
            dao.actualizar(p);
            JOptionPane.showMessageDialog(this, "Producto actualizado.");
            cargarTablaProductos();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "El precio debe ser un número válido.");
        }
    }

    private void guardarReceta() {
        if (txtCodigo.getText().isBlank()) {
            JOptionPane.showMessageDialog(this, "Primero guarda o selecciona un producto.");
            return;
        }
        int codigoProducto = Integer.parseInt(txtCodigo.getText().trim());
        if (dao.guardarReceta(codigoProducto, recetaEnEdicion)) {
            JOptionPane.showMessageDialog(this, "Receta guardada.");
        }
    }

    private void cargarTablaProductos() {
        modeloProductos.setRowCount(0);
        for (Producto p : dao.listarTodos()) {
            modeloProductos.addRow(new Object[]{p.getCodigoProducto(), p.getNombre(), p.getCategoria(), p.getPrecioVenta()});
        }
    }

    private void cargarSeleccion() {
        int fila = tablaProductos.getSelectedRow();
        if (fila == -1) return;
        int codigo = (int) modeloProductos.getValueAt(fila, 0);
        txtCodigo.setText(String.valueOf(codigo));
        txtNombre.setText(modeloProductos.getValueAt(fila, 1).toString());
        cmbCategoria.setSelectedItem(modeloProductos.getValueAt(fila, 2).toString());
        txtPrecio.setText(modeloProductos.getValueAt(fila, 3).toString());

        // Cargar receta existente
        recetaEnEdicion = dao.listarRecetaPorProducto(codigo);
        modeloReceta.setRowCount(0);
        for (RecetaItem item : recetaEnEdicion) {
            modeloReceta.addRow(new Object[]{item.getNombreInsumo(), item.getCantidadRequerida()});
        }

        // Cargar foto existente
        for (Producto p : dao.listarTodos()) {
            if (p.getCodigoProducto() == codigo) {
                fotoActual = p.getFoto();
                mostrarFotoEnLabel(fotoActual);
                break;
            }
        }
    }

    private void limpiarFormulario() {
        txtCodigo.setText("");
        txtNombre.setText("");
        txtPrecio.setText("");
        fotoActual = null;
        mostrarFotoEnLabel(null);
        recetaEnEdicion = new ArrayList<>();
        modeloReceta.setRowCount(0);
        tablaProductos.clearSelection();
    }

    private void exportarMenuHtml() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("menu.html"));
        int resultado = chooser.showSaveDialog(this);
        if (resultado != JFileChooser.APPROVE_OPTION) return;

        StringBuilder html = new StringBuilder();
        html.append("<html><head><meta charset='UTF-8'><title>Menú JavaBeans Café</title>");
        html.append("<style>body{font-family:Arial,sans-serif;background:#f5f0e8;padding:20px;}");
        html.append(".producto{display:inline-block;width:220px;margin:10px;padding:10px;background:white;border-radius:10px;box-shadow:0 2px 5px rgba(0,0,0,0.15);text-align:center;}");
        html.append(".producto img{width:180px;height:180px;object-fit:cover;border-radius:8px;}");
        html.append("h1{text-align:center;color:#4b2e2b;} h2{color:#6f4e37;}</style></head><body>");
        html.append("<h1>☕ Menú JavaBeans Café</h1>");

        String[] categorias = {"BEBIDA_CALIENTE", "BEBIDA_FRIA", "POSTRE", "COMIDA"};
        for (String categoria : categorias) {
            html.append("<h2>").append(categoria.replace("_", " ")).append("</h2>");
            for (Producto p : dao.listarTodos()) {
                if (!p.getCategoria().equals(categoria)) continue;
                html.append("<div class='producto'>");
                if (p.getFoto() != null) {
                    String base64 = java.util.Base64.getEncoder().encodeToString(p.getFoto());
                    html.append("<img src='data:image/jpeg;base64,").append(base64).append("'/>");
                }
                html.append("<h3>").append(p.getNombre()).append("</h3>");
                html.append("<p>Q").append(String.format("%.2f", p.getPrecioVenta())).append("</p>");
                html.append("</div>");
            }
        }
        html.append("</body></html>");

        try {
            Files.writeString(chooser.getSelectedFile().toPath(), html.toString());
            JOptionPane.showMessageDialog(this, "Menú exportado correctamente.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al exportar: " + ex.getMessage());
        }
    }
}