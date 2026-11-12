/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package Vista;


import javax.swing.JOptionPane;

/**
 *
 * @author Garme
 */
public class CrearOrdenes extends javax.swing.JFrame {
    private final Controlador.TallerControlador controlador;
    private Modelo.OrdenTrabajo ordenActual;
    private final Runnable actualizarReporte;

    public CrearOrdenes() {
        this(Controlador.ContextoAplicacion.getControlador());
    }

    public CrearOrdenes(Controlador.TallerControlador controlador) {
        this(controlador, () -> {});
    }

    public CrearOrdenes(Controlador.TallerControlador controlador, Runnable actualizarReporte) {
        this.controlador = java.util.Objects.requireNonNull(controlador);
        this.actualizarReporte = java.util.Objects.requireNonNull(actualizarReporte);
        initComponents();
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        fechaT.setEditable(false);
        CostoTotalT.setEditable(false);
        jComboBox1.setEnabled(false);
        progreso.setStringPainted(true);
        progreso.setString("Sin procesar");
        guardar.addActionListener(evt -> guardarOrden());
        buscar.addActionListener(evt -> buscarOrden());
        eliminar.addActionListener(evt -> eliminarOrden());
        finalizar.addActionListener(evt -> finalizarOrden());
        cancelar.addActionListener(evt -> cancelarOrden());
        limpiarFormulario();
    }

    private void guardarOrden() {
        String numero = JOptionPane.showInputDialog(this, "Número único de orden:");
        if (numero == null) return;
        numero = numero.trim();
        if (numero.isEmpty() || controlador.getRepoOrdenes().existe(numero)) {
            JOptionPane.showMessageDialog(this, "Ingrese un número de orden único."); return;
        }
        Modelo.Cliente cliente;
        try {
            cliente = controlador.buscarClientePorCodigo(clienteT.getText());
        } catch (Excepciones.ClienteNoEncontradoException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Cliente no encontrado", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Modelo.Vehiculo vehiculo = controlador.buscarVehiculoPorPlaca(vehiculoT.getText().trim().toUpperCase(java.util.Locale.ROOT));
        if (vehiculo == null || vehiculo.getPropietario() == null
                || !cliente.getCodigo().equals(vehiculo.getPropietario().getCodigo())) {
            JOptionPane.showMessageDialog(this, "Ingrese un código de cliente y una placa registrada a su nombre."); return;
        }
        String diagnostico = diagnosticoT.getText().trim(), mecanico = mecanicoT.getText().trim();
        if (diagnostico.isEmpty() || mecanico.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingrese el diagnóstico y el mecánico."); return;
        }
        Modelo.OrdenTrabajo nueva = new Modelo.OrdenTrabajo(numero, cliente, vehiculo, diagnostico, mecanico);
        java.util.List<Modelo.Servicio> servicios = controlador.getRepoServicios().obtenerTodos();
        if (servicios.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Registre al menos un servicio antes de crear una orden.");
            return;
        }
        javax.swing.JList<Modelo.Servicio> listaServicios = new javax.swing.JList<>(
                servicios.toArray(new Modelo.Servicio[0]));
        listaServicios.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        javax.swing.JScrollPane panelServicios = new javax.swing.JScrollPane(listaServicios);
        panelServicios.setPreferredSize(new java.awt.Dimension(450, 160));
        int opcion = JOptionPane.showConfirmDialog(this,
                new Object[]{"Seleccione uno o varios servicios (Ctrl + clic para varios):", panelServicios},
                "Servicios de la orden", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (opcion != JOptionPane.OK_OPTION) return;
        if (listaServicios.isSelectionEmpty()) {
            JOptionPane.showMessageDialog(this, "Seleccione al menos un servicio.");
            return;
        }
        for (Modelo.Servicio servicio : listaServicios.getSelectedValuesList()) nueva.agregarServicio(servicio);

        java.util.List<Modelo.Inventario> repuestos = controlador.getRepoRepuestos().obtenerTodos();
        if (!repuestos.isEmpty()) {
            javax.swing.table.DefaultTableModel modelo = new javax.swing.table.DefaultTableModel(
                    new Object[]{"Código", "Repuesto", "Disponible", "Cantidad"}, 0) {
                @Override public boolean isCellEditable(int fila, int columna) { return columna == 3; }
            };
            for (Modelo.Inventario repuesto : repuestos) {
                modelo.addRow(new Object[]{repuesto.getCodigo(), repuesto.getNombre(), repuesto.getCantidadStock(), 0});
            }
            javax.swing.JTable tabla = new javax.swing.JTable(modelo);
            javax.swing.JScrollPane panelRepuestos = new javax.swing.JScrollPane(tabla);
            panelRepuestos.setPreferredSize(new java.awt.Dimension(520, 180));
            opcion = JOptionPane.showConfirmDialog(this,
                    new Object[]{"Indique la cantidad de cada repuesto; use 0 si no lo necesita:", panelRepuestos},
                    "Repuestos de la orden", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (opcion != JOptionPane.OK_OPTION) return;
            if (tabla.isEditing() && !tabla.getCellEditor().stopCellEditing()) {
                JOptionPane.showMessageDialog(this, "Termine de editar la cantidad antes de continuar.");
                return;
            }
            for (int fila = 0; fila < modelo.getRowCount(); fila++) {
                String codigo = modelo.getValueAt(fila, 0).toString();
                try {
                    int cantidad = Integer.parseInt(String.valueOf(modelo.getValueAt(fila, 3)).trim());
                    if (cantidad < 0) throw new IllegalArgumentException("La cantidad no puede ser negativa.");
                    if (cantidad > 0) controlador.agregarRepuestoAOrden(nueva, codigo, cantidad);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Ingrese una cantidad entera válida para " + codigo + ".");
                    return;
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "No se pudo agregar el repuesto " + codigo + ": " + ex.getMessage());
                    return;
                }
            }
        }
        if (controlador.getRepoOrdenes().existe(numero)) {
            JOptionPane.showMessageDialog(this, "El número de orden ya fue registrado. Utilice otro.");
            return;
        }
        controlador.getRepoOrdenes().guardar(numero, nueva);
        actualizarReporte.run();
        mostrarOrden(nueva);
        JOptionPane.showMessageDialog(this, "Orden " + numero + " registrada.");
    }

    private void buscarOrden() {
        String numero = JOptionPane.showInputDialog(this, "Número de orden:");
        if (numero == null) return;
        Modelo.OrdenTrabajo orden = controlador.getRepoOrdenes().buscar(numero.trim());
        if (orden == null) { JOptionPane.showMessageDialog(this, "Orden no encontrada."); return; }
        mostrarOrden(orden);
    }

    private void mostrarOrden(Modelo.OrdenTrabajo orden) {
        ordenActual = orden;
        if (orden.getEstado() != Modelo.OrdenTrabajo.EstadoOrden.EN_PROCESO) {
            progreso.setValue(orden.getEstado() == Modelo.OrdenTrabajo.EstadoOrden.FINALIZADA ? 100 : 0);
            progreso.setString(orden.getEstado() == Modelo.OrdenTrabajo.EstadoOrden.CANCELADA ? "Cancelada" : null);
        }
        fechaT.setText(new java.text.SimpleDateFormat("dd/MM/yyyy").format(orden.getFecha()));
        clienteT.setText(orden.getCliente().getCodigo());
        vehiculoT.setText(orden.getVehiculo().getPlaca());
        diagnosticoT.setText(orden.getDiagnostico());
        mecanicoT.setText(orden.getMecanico());
        CostoTotalT.setText(String.format(java.util.Locale.ROOT, "%.2f", orden.calcularCostoTotal()));
        jComboBox1.setSelectedIndex(orden.getEstado().ordinal());
        setTitle("Orden " + orden.getNumeroOrden());
    }

    private boolean ordenSeleccionadaVigente() {
        if (ordenActual == null) {
            JOptionPane.showMessageDialog(this, "Busque una orden primero.");
            return false;
        }
        if (controlador.getRepoOrdenes().buscar(ordenActual.getNumeroOrden()) != ordenActual) {
            JOptionPane.showMessageDialog(this, "La orden ya no está disponible. Busque otra orden.");
            limpiarFormulario();
            return false;
        }
        return true;
    }
    private void eliminarOrden() {
        if (!ordenSeleccionadaVigente()) return;
        if (ordenActual == null) { JOptionPane.showMessageDialog(this, "Busque una orden primero."); return; }
        if (ordenActual.getEstado() != Modelo.OrdenTrabajo.EstadoOrden.PENDIENTE) {
            JOptionPane.showMessageDialog(this, "Solo se pueden eliminar órdenes pendientes."); return;
        }
        if (JOptionPane.showConfirmDialog(this, "¿Eliminar la orden " + ordenActual.getNumeroOrden() + "?",
                "Confirmar", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            controlador.getRepoOrdenes().eliminar(ordenActual.getNumeroOrden());
            actualizarReporte.run();
            limpiarFormulario();
        }
    }

    private void cancelarOrden() {
        if (!ordenSeleccionadaVigente()) return;
        if (ordenActual == null) {
            JOptionPane.showMessageDialog(this, "Busque una orden primero.");
            return;
        }
        if (ordenActual.getEstado() != Modelo.OrdenTrabajo.EstadoOrden.PENDIENTE) {
            JOptionPane.showMessageDialog(this, "Solo se pueden cancelar órdenes pendientes.");
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "¿Cancelar la orden " + ordenActual.getNumeroOrden() + "?",
                "Confirmar cancelación", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            ordenActual.setEstado(Modelo.OrdenTrabajo.EstadoOrden.CANCELADA);
            actualizarReporte.run();
            mostrarOrden(ordenActual);
            JOptionPane.showMessageDialog(this, "Orden cancelada.");
        }
    }

    private void finalizarOrden() {
        if (!ordenSeleccionadaVigente()) return;
        if (ordenActual == null) { JOptionPane.showMessageDialog(this, "Busque una orden primero."); return; }
        Modelo.OrdenTrabajo orden = ordenActual;
        try {
            progreso.setValue(0);
            progreso.setString(null);
            Hilos.ProcesoServicioHilo proceso = controlador.procesarOrdenMultihilo(orden, progreso, () -> {
                if (ordenActual == orden) mostrarOrden(orden);
                actualizarReporte.run();
                controlador.verificarAlertasInventario();
            });
            guardar.setEnabled(false); buscar.setEnabled(false); eliminar.setEnabled(false);
            finalizar.setEnabled(false); cancelar.setEnabled(false);
            proceso.addPropertyChangeListener(e -> {
                if ("state".equals(e.getPropertyName())
                        && e.getNewValue() == javax.swing.SwingWorker.StateValue.DONE) {
                    guardar.setEnabled(true); buscar.setEnabled(true); eliminar.setEnabled(true);
                    finalizar.setEnabled(true); cancelar.setEnabled(true);
                    if (ordenActual == orden) mostrarOrden(orden);
                    actualizarReporte.run();
                }
            });
            mostrarOrden(orden);
            actualizarReporte.run();
        } catch (IllegalStateException ex) { JOptionPane.showMessageDialog(this, ex.getMessage()); }
    }

    private void limpiarFormulario() {
        ordenActual = null; setTitle("Crear órdenes");
        fechaT.setText(new java.text.SimpleDateFormat("dd/MM/yyyy").format(new java.util.Date()));
        clienteT.setText(""); vehiculoT.setText(""); diagnosticoT.setText(""); mecanicoT.setText("");
        CostoTotalT.setText("0.00"); jComboBox1.setSelectedIndex(0);
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        fechaT = new javax.swing.JTextField();
        fecha = new javax.swing.JLabel();
        cliente = new javax.swing.JLabel();
        diagnostico = new javax.swing.JLabel();
        vehiculo = new javax.swing.JLabel();
        clienteT = new javax.swing.JTextField();
        vehiculoT = new javax.swing.JTextField();
        diagnosticoT = new javax.swing.JTextField();
        mecanico = new javax.swing.JLabel();
        mecanicoT = new javax.swing.JTextField();
        estadoOrden = new javax.swing.JLabel();
        costo = new javax.swing.JLabel();
        CostoTotalT = new javax.swing.JTextField();
        eliminar = new javax.swing.JButton();
        guardar = new javax.swing.JButton();
        finalizar = new javax.swing.JButton();
        buscar = new javax.swing.JButton();
        jComboBox1 = new javax.swing.JComboBox<>();
        cancelar = new javax.swing.JButton();
        progreso = new javax.swing.JProgressBar();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        fecha.setText("Fecha");

        cliente.setText("Codigo de Cliente");

        diagnostico.setText("Diagnostico");

        vehiculo.setText("Vehiculo");

        vehiculoT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                vehiculoTActionPerformed(evt);
            }
        });

        mecanico.setText("Mecanico");

        estadoOrden.setText("Estado de la orden");

        costo.setText("Costo Total ");

        eliminar.setText("Eliminar Orden");

        guardar.setText("Guardar");

        finalizar.setText("Finalizar Orden");

        buscar.setText("Buscar");

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Pendiente", "En proceso", "Finalizada", "Cancelada" }));

        cancelar.setText("Cancelar orden");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(cliente, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(fecha, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(36, 36, 36)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(diagnosticoT, javax.swing.GroupLayout.DEFAULT_SIZE, 108, Short.MAX_VALUE)
                            .addComponent(vehiculoT)
                            .addComponent(clienteT)
                            .addComponent(fechaT)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(51, 51, 51)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(estadoOrden, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(vehiculo, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(diagnostico, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addComponent(mecanico, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addComponent(costo, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(mecanicoT)
                            .addComponent(CostoTotalT)
                            .addComponent(buscar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(eliminar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jComboBox1, 0, 111, Short.MAX_VALUE))))
                .addGap(105, 105, 105))
            .addGroup(layout.createSequentialGroup()
                .addGap(51, 51, 51)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(8, 8, 8)
                                .addComponent(finalizar))
                            .addComponent(guardar, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(cancelar))
                    .addComponent(progreso, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(fecha)
                    .addComponent(fechaT, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cliente)
                    .addComponent(clienteT, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(vehiculo)
                    .addComponent(vehiculoT, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(diagnostico)
                    .addComponent(diagnosticoT, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(mecanico)
                    .addComponent(mecanicoT, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(estadoOrden)
                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(costo)
                    .addComponent(CostoTotalT, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(eliminar)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(finalizar)
                    .addComponent(buscar))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cancelar)
                    .addComponent(guardar))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(progreso, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(47, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void vehiculoTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_vehiculoTActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_vehiculoTActionPerformed

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
            java.util.logging.Logger.getLogger(CrearOrdenes.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(CrearOrdenes.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(CrearOrdenes.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(CrearOrdenes.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new CrearOrdenes().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField CostoTotalT;
    private javax.swing.JButton buscar;
    private javax.swing.JButton cancelar;
    private javax.swing.JLabel cliente;
    private javax.swing.JTextField clienteT;
    private javax.swing.JLabel costo;
    private javax.swing.JLabel diagnostico;
    private javax.swing.JTextField diagnosticoT;
    private javax.swing.JButton eliminar;
    private javax.swing.JLabel estadoOrden;
    private javax.swing.JLabel fecha;
    private javax.swing.JTextField fechaT;
    private javax.swing.JButton finalizar;
    private javax.swing.JButton guardar;
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JLabel mecanico;
    private javax.swing.JTextField mecanicoT;
    private javax.swing.JProgressBar progreso;
    private javax.swing.JLabel vehiculo;
    private javax.swing.JTextField vehiculoT;
    // End of variables declaration//GEN-END:variables
}
