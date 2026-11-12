/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package Vista;

/**
 *
 * @author Garme
 */
public class Inventario extends javax.swing.JFrame {

    /**
     * Creates new form Inventario
     */
    private final Controlador.TallerControlador controlador;
    private final javax.swing.table.DefaultTableModel modeloRegistros = new javax.swing.table.DefaultTableModel(
            new Object[]{"Código", "Nombre", "Stock", "Precio unitario", "Stock mínimo"}, 0) {
        @Override public boolean isCellEditable(int fila, int columna) { return false; }
    };



    public Inventario() {
        this(Controlador.ContextoAplicacion.getControlador());
    }

    public Inventario(Controlador.TallerControlador controlador) {
        this.controlador = java.util.Objects.requireNonNull(controlador);
        initComponents();
        configurarTablaRegistros();
        configurarAcciones();
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    private void configurarTablaRegistros() {
        javax.swing.JTable tabla = new javax.swing.JTable(modeloRegistros);
        tabla.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting() || tabla.getSelectedRow() < 0) return;
            int fila = tabla.convertRowIndexToModel(tabla.getSelectedRow());
            Modelo.Inventario repuesto = controlador.getRepoRepuestos().buscar(
                    String.valueOf(modeloRegistros.getValueAt(fila, 0)));
            if (repuesto == null) return;
            codigoT.setText(repuesto.getCodigo());
            nombreT.setText(repuesto.getNombre());
            cantidadT.setText(String.valueOf(repuesto.getCantidadStock()));
            precioT.setText(String.valueOf(repuesto.getPrecioUnitario()));
            minimoT.setText(String.valueOf(repuesto.getStockMinimo()));
            entradaT.setText("");
        });
        javax.swing.JScrollPane scroll = new javax.swing.JScrollPane(tabla);
        scroll.setPreferredSize(new java.awt.Dimension(650, 180));
        java.awt.Container formulario = getContentPane();
        javax.swing.JPanel contenido = new javax.swing.JPanel(new java.awt.BorderLayout(0, 8));
        contenido.add(formulario, java.awt.BorderLayout.NORTH);
        contenido.add(scroll, java.awt.BorderLayout.CENTER);
        setContentPane(contenido);
        actualizarTablaRegistros();
        addWindowFocusListener(new java.awt.event.WindowAdapter() {
            @Override public void windowGainedFocus(java.awt.event.WindowEvent e) { actualizarTablaRegistros(); }
        });
        pack();
    }

    private void actualizarTablaRegistros() {
        modeloRegistros.setRowCount(0);
        for (Modelo.Inventario repuesto : controlador.getRepoRepuestos().obtenerTodos()) {
            modeloRegistros.addRow(new Object[]{repuesto.getCodigo(), repuesto.getNombre(),
                    repuesto.getCantidadStock(), repuesto.getPrecioUnitario(), repuesto.getStockMinimo()});
        }
    }

    private void configurarAcciones() {
        setTitle("Inventario de repuestos");
        guardar.addActionListener(e -> guardarRepuesto());
        consultar.addActionListener(e -> consultarRepuesto());
        entrada.addActionListener(e -> registrarEntrada());
        alertas.addActionListener(e -> controlador.verificarAlertasInventario());
        limpiar.addActionListener(e -> limpiarCampos());
    }
    private void guardarRepuesto() {
        String codigo = codigoT.getText().trim();
        if (controlador.getRepoRepuestos().existe(codigo)) {
            javax.swing.JOptionPane.showMessageDialog(this, "Ya existe un repuesto con ese código.");
            return;
        }
        try {
            int cantidad = Integer.parseInt(cantidadT.getText().trim());
            int minimo = Integer.parseInt(minimoT.getText().trim());
            double precio = Double.parseDouble(precioT.getText().trim());
            Modelo.Inventario repuesto = new Modelo.Inventario(codigo, nombreT.getText().trim(), precio, cantidad, minimo);
            controlador.getRepoRepuestos().guardar(codigo, repuesto);
            actualizarTablaRegistros();
            javax.swing.JOptionPane.showMessageDialog(this, "Repuesto registrado correctamente.");
            limpiarCampos();
            controlador.verificarAlertasInventario();
        } catch (NumberFormatException ex) {
            javax.swing.JOptionPane.showMessageDialog(this, "Ingrese cantidades enteras y un precio válido.");
        } catch (IllegalArgumentException ex) {
            javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    private Modelo.Inventario buscarRepuesto() {
        Modelo.Inventario repuesto = controlador.getRepoRepuestos().buscar(codigoT.getText().trim());
        if (repuesto == null) javax.swing.JOptionPane.showMessageDialog(this, "No se encontró el código indicado.");
        return repuesto;
    }

    private void consultarRepuesto() {
        Modelo.Inventario repuesto = buscarRepuesto();
        if (repuesto == null) return;
        nombreT.setText(repuesto.getNombre());
        cantidadT.setText(String.valueOf(repuesto.getCantidadStock()));
        precioT.setText(String.valueOf(repuesto.getPrecioUnitario()));
        minimoT.setText(String.valueOf(repuesto.getStockMinimo()));
    }

    private void registrarEntrada() {
        Modelo.Inventario repuesto = buscarRepuesto();
        if (repuesto == null) return;
        try {
            repuesto.registrarEntrada(Integer.parseInt(entradaT.getText().trim()));
            actualizarTablaRegistros();
            consultarRepuesto();
            entradaT.setText("");
            javax.swing.JOptionPane.showMessageDialog(this, "Entrada registrada correctamente.");
        } catch (NumberFormatException ex) {
            javax.swing.JOptionPane.showMessageDialog(this, "Ingrese una cantidad entera válida.");
        } catch (IllegalArgumentException ex) {
            javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    private void limpiarCampos() {
        codigoT.setText(""); nombreT.setText(""); cantidadT.setText("");
        precioT.setText(""); minimoT.setText(""); entradaT.setText("");
        codigoT.requestFocusInWindow();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        jLabel1 = new javax.swing.JLabel();
        nombre = new javax.swing.JLabel();
        cantidad = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        minimoLabel = new javax.swing.JLabel();
        entradaLabel = new javax.swing.JLabel();
        codigoT = new javax.swing.JTextField();
        nombreT = new javax.swing.JTextField();
        cantidadT = new javax.swing.JTextField();
        precioT = new javax.swing.JTextField();
        minimoT = new javax.swing.JTextField();
        entradaT = new javax.swing.JTextField();
        guardar = new javax.swing.JButton();
        consultar = new javax.swing.JButton();
        entrada = new javax.swing.JButton();
        alertas = new javax.swing.JButton();
        limpiar = new javax.swing.JButton();
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        jLabel1.setText("Código");
        nombre.setText("Nombre");
        cantidad.setText("Cantidad");
        jLabel3.setText("Precio unitario");
        minimoLabel.setText("Stock mínimo");
        entradaLabel.setText("Entrada");
        guardar.setText("Guardar");
        consultar.setText("Consultar");
        entrada.setText("Registrar entrada");
        alertas.setText("Ver alertas");
        limpiar.setText("Limpiar");
        cantidadT.addActionListener(evt -> cantidadTActionPerformed(evt));
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
        layout.setHorizontalGroup(layout.createParallelGroup()
            .addGroup(layout.createSequentialGroup().addGroup(layout.createParallelGroup()
                .addComponent(jLabel1)
                .addComponent(nombre)
                .addComponent(cantidad)
                .addComponent(jLabel3)
                .addComponent(minimoLabel)
                .addComponent(entradaLabel)
            ).addGroup(layout.createParallelGroup()
                .addComponent(codigoT)
                .addComponent(nombreT)
                .addComponent(cantidadT)
                .addComponent(precioT)
                .addComponent(minimoT)
                .addComponent(entradaT)
            ))
            .addGroup(layout.createSequentialGroup().addComponent(guardar).addComponent(consultar).addComponent(entrada))
            .addGroup(layout.createSequentialGroup().addComponent(alertas).addComponent(limpiar)));
        layout.setVerticalGroup(layout.createSequentialGroup()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(jLabel1).addComponent(codigoT, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(nombre).addComponent(nombreT, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(cantidad).addComponent(cantidadT, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(jLabel3).addComponent(precioT, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(minimoLabel).addComponent(minimoT, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(entradaLabel).addComponent(entradaT, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(guardar).addComponent(consultar).addComponent(entrada))
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(alertas).addComponent(limpiar)));
        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cantidadTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cantidadTActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cantidadTActionPerformed

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
            java.util.logging.Logger.getLogger(Inventario.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Inventario.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Inventario.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Inventario.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Inventario().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel cantidad;
    private javax.swing.JTextField cantidadT;
    private javax.swing.JTextField codigoT;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel nombre;
    private javax.swing.JTextField nombreT;
    private javax.swing.JTextField precioT;

    private javax.swing.JLabel minimoLabel;
    private javax.swing.JLabel entradaLabel;
    private javax.swing.JTextField minimoT;
    private javax.swing.JTextField entradaT;
    private javax.swing.JButton guardar;
    private javax.swing.JButton consultar;
    private javax.swing.JButton entrada;
    private javax.swing.JButton alertas;
    private javax.swing.JButton limpiar;
    // End of variables declaration//GEN-END:variables
}
