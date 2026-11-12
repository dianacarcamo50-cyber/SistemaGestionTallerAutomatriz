/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Hilos;
import Modelo.OrdenTrabajo;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;
import java.util.List;
/**
 *
 * @author Garme
 */


public class ProcesoServicioHilo extends SwingWorker<Void, Integer> {
    private final OrdenTrabajo orden;
    private final JProgressBar progressBar;
    private final Runnable onCompleteCallback;
    private final Runnable descontarInventario;

    public ProcesoServicioHilo(OrdenTrabajo orden, JProgressBar progressBar, Runnable descontarInventario, Runnable onCompleteCallback) {
        this.orden = orden;
        this.progressBar = progressBar;
        this.onCompleteCallback = onCompleteCallback;
        this.descontarInventario = descontarInventario;
    }

    @Override
    protected Void doInBackground() throws Exception {

        for (int i = 0; i <= 100; i += 20) {
            Thread.sleep(1000); 
            publish(i);
        }
        return null;
    }

    @Override
    protected void process(List<Integer> chunks) {
        int ultimoProgreso = chunks.get(chunks.size() - 1);
        if (progressBar != null) {
            progressBar.setValue(ultimoProgreso);
        }
    }

    @Override
    protected void done() {
        try {
            get();
            descontarInventario.run();
            orden.setEstado(OrdenTrabajo.EstadoOrden.FINALIZADA);
            JOptionPane.showMessageDialog(null, "Procesamiento de la Orden #" + orden.getNumeroOrden() + " finalizado exitosamente.");
            if (onCompleteCallback != null) onCompleteCallback.run();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            if (progressBar != null) { progressBar.setValue(0); progressBar.setString("No finalizado"); }
            orden.setEstado(OrdenTrabajo.EstadoOrden.PENDIENTE);
            JOptionPane.showMessageDialog(null, "Procesamiento interrumpido.");
        } catch (java.util.concurrent.ExecutionException | IllegalStateException ex) {
            if (progressBar != null) { progressBar.setValue(0); progressBar.setString("No finalizado"); }
            orden.setEstado(OrdenTrabajo.EstadoOrden.PENDIENTE);
            Throwable causa = ex.getCause() == null ? ex : ex.getCause();
            JOptionPane.showMessageDialog(null, "No se pudo finalizar la orden: " + causa.getMessage());
        }
    }
}