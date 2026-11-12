/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package Vista;

/**
 *
 * @author Garme
 */
public class ServiciosMecanicos extends javax.swing.JFrame {

    /**
     * Creates new form ServiciosMecanicos
     */
    private final Controlador.TallerControlador controlador;
    private final javax.swing.table.DefaultTableModel tablaModelo = new javax.swing.table.DefaultTableModel(
            new Object[]{"Código", "Descripción", "Precio", "Horas"}, 0) {
        @Override public boolean isCellEditable(int fila, int columna) { return false; }
    };

    public ServiciosMecanicos() {
        this(Controlador.ContextoAplicacion.getControlador());
    }

    public ServiciosMecanicos(Controlador.TallerControlador controlador) {
        this.controlador = java.util.Objects.requireNonNull(controlador);
        initComponents();
        crearFormulario();
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    private void crearFormulario() {
        setTitle("Servicios mecánicos");
        tabla.setModel(tablaModelo);
        guardar.addActionListener(e -> guardarServicio());
        buscar.addActionListener(e -> buscarServicio());
        limpiar.addActionListener(e -> limpiarCampos());
        serviciosRequeridos.addActionListener(e -> agregarServiciosRequeridos());
        javax.swing.JMenuBar barra = new javax.swing.JMenuBar();
        javax.swing.JMenu acciones = new javax.swing.JMenu("Acciones");
        javax.swing.JMenuItem deshacer = new javax.swing.JMenuItem("Deshacer último registro de servicio");
        deshacer.addActionListener(e -> {
            if (controlador.deshacerRegistroServicio()) {
                actualizarTabla();
                limpiarCampos();
                javax.swing.JOptionPane.showMessageDialog(this, "Se deshizo el último registro de servicio.");
            } else {
                javax.swing.JOptionPane.showMessageDialog(this,
                        "No hay un registro que se pueda deshacer, o el servicio ya está asociado a una orden.");
            }
        });
        acciones.add(deshacer);
        barra.add(acciones);
        setJMenuBar(barra);
        actualizarTabla();
    }
    private void agregarServiciosRequeridos() {
        String[] nombres = {"Cambio de aceite", "Revisión de frenos", "Diagnóstico electrónico",
                "Alineamiento", "Mantenimiento general"};
        for (String nombre : nombres) {
            boolean existe = false;
            for (Modelo.Servicio servicio : controlador.getRepoServicios().obtenerTodos()) {
                if (nombre.equalsIgnoreCase(servicio.getDescripcion().trim())) {
                    existe = true;
                    break;
                }
            }
            if (existe) continue;
            javax.swing.JTextField campoCodigo = new javax.swing.JTextField();
            javax.swing.JTextField campoPrecio = new javax.swing.JTextField();
            javax.swing.JTextField campoTiempo = new javax.swing.JTextField();
            Object[] campos = {"Código:", campoCodigo, "Precio base:", campoPrecio,
                    "Tiempo estimado (horas):", campoTiempo};
            while (true) {
                int opcion = javax.swing.JOptionPane.showConfirmDialog(this, campos,
                        nombre, javax.swing.JOptionPane.OK_CANCEL_OPTION);
                if (opcion != javax.swing.JOptionPane.OK_OPTION) {
                    actualizarTabla();
                    return;
                }
                String cod = campoCodigo.getText().trim();
                try {
                    double valor = Double.parseDouble(campoPrecio.getText().trim());
                    double horas = Double.parseDouble(campoTiempo.getText().trim());
                    if (cod.isEmpty() || controlador.getRepoServicios().existe(cod)
                            || !Double.isFinite(valor) || valor <= 0
                            || !Double.isFinite(horas) || horas <= 0) {
                        javax.swing.JOptionPane.showMessageDialog(this,
                                "Ingrese un código único y valores positivos para precio y tiempo.");
                        continue;
                    }
                    controlador.getRepoServicios().guardar(cod, new Modelo.Servicio(cod, nombre, valor, horas));
                    actualizarTabla();
                    break;
                } catch (NumberFormatException ex) {
                    javax.swing.JOptionPane.showMessageDialog(this, "Ingrese números válidos para precio y tiempo.");
                }
            }
        }
        javax.swing.JOptionPane.showMessageDialog(this, "Los servicios requeridos están registrados.");
    }

    private void guardarServicio() {
        String cod = codigo.getText().trim();
        String desc = descripcion.getText().trim();
        if (cod.isEmpty() || desc.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this, "Complete el código y la descripción.");
            return;
        }
        if (controlador.getRepoServicios().existe(cod)) {
            javax.swing.JOptionPane.showMessageDialog(this, "Ya existe un servicio con ese código.");
            return;
        }
        try {
            double valor = Double.parseDouble(precio.getText().trim());
            double horas = Double.parseDouble(tiempo.getText().trim());
            if (!Double.isFinite(valor) || valor <= 0 || !Double.isFinite(horas) || horas <= 0) {
                javax.swing.JOptionPane.showMessageDialog(this, "El precio y el tiempo deben ser números positivos.");
                return;
            }
            controlador.getRepoServicios().guardar(cod, new Modelo.Servicio(cod, desc, valor, horas));
            actualizarTabla();
            limpiarCampos();
            javax.swing.JOptionPane.showMessageDialog(this, "Servicio registrado correctamente.");
        } catch (NumberFormatException ex) {
            javax.swing.JOptionPane.showMessageDialog(this, "Ingrese un precio y un tiempo válidos.");
        }
    }

    private void buscarServicio() {
        Modelo.Servicio servicio = controlador.getRepoServicios().buscar(codigo.getText().trim());
        if (servicio == null) {
            javax.swing.JOptionPane.showMessageDialog(this, "No se encontró un servicio con ese código.");
            return;
        }
        descripcion.setText(servicio.getDescripcion());
        precio.setText(String.valueOf(servicio.getPrecioBase()));
        tiempo.setText(String.valueOf(servicio.getTiempoEstimadoHoras()));
    }

    private void actualizarTabla() {
        tablaModelo.setRowCount(0);
        for (Modelo.Servicio servicio : controlador.getRepoServicios().obtenerTodos()) {
            tablaModelo.addRow(new Object[]{servicio.getCodigo(), servicio.getDescripcion(),
                    servicio.getPrecioBase(), servicio.getTiempoEstimadoHoras()});
        }
    }

    private void limpiarCampos() {
        codigo.setText(""); descripcion.setText(""); precio.setText(""); tiempo.setText("");
        codigo.requestFocusInWindow();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        codigoLabel = new javax.swing.JLabel();
        descripcionLabel = new javax.swing.JLabel();
        precioLabel = new javax.swing.JLabel();
        tiempoLabel = new javax.swing.JLabel();
        codigo = new javax.swing.JTextField();
        descripcion = new javax.swing.JTextField();
        precio = new javax.swing.JTextField();
        tiempo = new javax.swing.JTextField();
        guardar = new javax.swing.JButton();
        buscar = new javax.swing.JButton();
        limpiar = new javax.swing.JButton();
        serviciosRequeridos = new javax.swing.JButton();
        scroll = new javax.swing.JScrollPane();
        tabla = new javax.swing.JTable();
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        codigoLabel.setText("Código:");
        guardar.setText("Guardar");
        descripcionLabel.setText("Descripción:");
        buscar.setText("Buscar");
        precioLabel.setText("Precio base:");
        limpiar.setText("Limpiar");
        tiempoLabel.setText("Tiempo estimado (horas):");
        serviciosRequeridos.setText("Agregar servicios requeridos");
        scroll.setViewportView(tabla);
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
        layout.setHorizontalGroup(layout.createParallelGroup().addGroup(layout.createSequentialGroup().addGroup(layout.createParallelGroup()
            .addComponent(codigoLabel)
            .addComponent(descripcionLabel)
            .addComponent(precioLabel)
            .addComponent(tiempoLabel)
        ).addGroup(layout.createParallelGroup()
            .addComponent(codigo)
            .addComponent(descripcion)
            .addComponent(precio)
            .addComponent(tiempo)
        )).addGroup(layout.createSequentialGroup().addComponent(guardar).addComponent(buscar).addComponent(limpiar).addComponent(serviciosRequeridos)).addComponent(scroll));
        layout.setVerticalGroup(layout.createSequentialGroup()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(codigoLabel).addComponent(codigo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(descripcionLabel).addComponent(descripcion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(precioLabel).addComponent(precio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(tiempoLabel).addComponent(tiempo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(guardar).addComponent(buscar).addComponent(limpiar).addComponent(serviciosRequeridos)).addComponent(scroll, javax.swing.GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE));
        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(ServiciosMecanicos.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ServiciosMecanicos.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ServiciosMecanicos.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ServiciosMecanicos.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new ServiciosMecanicos().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel codigoLabel;
    private javax.swing.JLabel descripcionLabel;
    private javax.swing.JLabel precioLabel;
    private javax.swing.JLabel tiempoLabel;
    private javax.swing.JTextField codigo;
    private javax.swing.JTextField descripcion;
    private javax.swing.JTextField precio;
    private javax.swing.JTextField tiempo;
    private javax.swing.JButton guardar;
    private javax.swing.JButton buscar;
    private javax.swing.JButton limpiar;
    private javax.swing.JButton serviciosRequeridos;
    private javax.swing.JScrollPane scroll;
    private javax.swing.JTable tabla;
    // End of variables declaration//GEN-END:variables
}
