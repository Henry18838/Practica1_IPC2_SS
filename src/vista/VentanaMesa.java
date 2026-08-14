package vista;

import dao.MesaDAO;
import modelo.Mesa;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class VentanaMesa extends JInternalFrame {

    private MesaDAO dao = new MesaDAO();
    private JPanel panelVisual;
    private JTextField txtNumero, txtCapacidad;

    public VentanaMesa() {
        super("Control de Mesas", true, true, true, true);
        setSize(650, 500);
        setLayout(new BorderLayout());

        add(crearPanelFormulario(), BorderLayout.NORTH);

        panelVisual = new JPanel(new GridLayout(0, 4, 10, 10));
        panelVisual.setBorder(BorderFactory.createTitledBorder("Estado actual de las mesas"));
        add(new JScrollPane(panelVisual), BorderLayout.CENTER);

        refrescarVisual();
    }

    private JPanel crearPanelFormulario() {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createTitledBorder("Registrar nueva mesa"));

        txtNumero = new JTextField(5);
        txtCapacidad = new JTextField(5);

        JButton btnGuardar = new JButton("Registrar Mesa");
        btnGuardar.addActionListener(e -> registrarMesa());

        JButton btnRefrescar = new JButton("Refrescar");
        btnRefrescar.addActionListener(e -> refrescarVisual());

        panel.add(new JLabel("Número de mesa:"));
        panel.add(txtNumero);
        panel.add(new JLabel("Capacidad:"));
        panel.add(txtCapacidad);
        panel.add(btnGuardar);
        panel.add(btnRefrescar);
        return panel;
    }

    private void registrarMesa() {
        try {
            Mesa m = new Mesa(Integer.parseInt(txtNumero.getText().trim()),
                               Integer.parseInt(txtCapacidad.getText().trim()), "LIBRE");
            if (dao.insertar(m)) {
                JOptionPane.showMessageDialog(this, "Mesa registrada.");
                txtNumero.setText("");
                txtCapacidad.setText("");
                refrescarVisual();
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo registrar (¿número de mesa duplicado?).");
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Número de mesa y capacidad deben ser enteros.");
        }
    }

    public void refrescarVisual() {
        panelVisual.removeAll();
        List<Mesa> mesas = dao.listarTodas();
        for (Mesa m : mesas) {
            JButton btnMesa = new JButton("<html><center>Mesa " + m.getNumeroMesa()
                    + "<br>Cap: " + m.getCapacidad() + "<br>" + m.getEstado() + "</center></html>");
            btnMesa.setPreferredSize(new Dimension(120, 80));
            if ("LIBRE".equals(m.getEstado())) {
                btnMesa.setBackground(new Color(144, 238, 144)); // verde claro
            } else {
                btnMesa.setBackground(new Color(240, 128, 128)); // rojo claro
            }
           btnMesa.setForeground(new Color(30, 30, 30)); // texto oscuro fijo, siempre legible sobre verde/rojo claro
            btnMesa.setOpaque(true);
            btnMesa.setBorderPainted(false);
            panelVisual.add(btnMesa);
        }
        panelVisual.revalidate();
        panelVisual.repaint();
    }
}