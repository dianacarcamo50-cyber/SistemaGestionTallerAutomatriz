/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controlador;
import Excepciones.StockInsuficienteException;
import Hilos.ProcesoServicioHilo;
import Modelo.*;
import Repositorio.RepositorioGenerico;
import javax.swing.JProgressBar;
import javax.swing.JOptionPane;
import java.util.List;
import java.util.ArrayList;
/**
 *
 * @author Garme
 */

public class TallerControlador {
    private final RepositorioGenerico<String, Cliente> repoClientes = new RepositorioGenerico<>();
    private final RepositorioGenerico<String, Vehiculo> repoVehiculos = new RepositorioGenerico<>();
    private final RepositorioGenerico<String, Servicio> repoServicios = new RepositorioGenerico<>();
    private final RepositorioGenerico<String, Inventario> repoRepuestos = new RepositorioGenerico<>();
    private final RepositorioGenerico<String, Factura> repoFacturas = new RepositorioGenerico<>();
    private final RepositorioGenerico<String, OrdenTrabajo> repoOrdenes = new RepositorioGenerico<>();

    public RepositorioGenerico<String, Cliente> getRepoClientes() { return repoClientes; }
    public RepositorioGenerico<String, Vehiculo> getRepoVehiculos() { return repoVehiculos; }
    public RepositorioGenerico<String, Servicio> getRepoServicios() { return repoServicios; }
    public RepositorioGenerico<String, Inventario> getRepoRepuestos() { return repoRepuestos; }
    public RepositorioGenerico<String, Factura> getRepoFacturas() { return repoFacturas; }
    public RepositorioGenerico<String, OrdenTrabajo> getRepoOrdenes() { return repoOrdenes; }

    /** Deshace el último registro de servicio si ninguna orden lo utiliza. */
    public boolean deshacerRegistroServicio() {
        return repoServicios.deshacerInsercion(servicio -> repoOrdenes.obtenerTodos().stream()
                .noneMatch(orden -> orden.getServicios().contains(servicio)));
    }
    
    public Cliente buscarClientePorCodigo(String codigo) throws Excepciones.ClienteNoEncontradoException {
        String clave = codigo == null ? "" : codigo.trim();
        Cliente cliente = repoClientes.buscar(clave);
        if (cliente == null) {
            throw new Excepciones.ClienteNoEncontradoException(
                    clave.isEmpty() ? "Ingrese el código de un cliente registrado." : "No se encontró el cliente con código: " + clave);
        }
        return cliente;
    }

    public void registrarVehiculo(Vehiculo vehiculo) throws IllegalArgumentException {
        if (repoVehiculos.existe(vehiculo.getPlaca())) {
            throw new IllegalArgumentException("Ya existe un vehículo registrado con la placa: " + vehiculo.getPlaca());
        }
        repoVehiculos.guardar(vehiculo.getPlaca(), vehiculo);
    }

    public Vehiculo buscarVehiculoPorPlaca(String placa) {
        return repoVehiculos.buscar(placa);
    }

    public List<Vehiculo> listarVehiculosPorCliente(String codigoCliente) {
        List<Vehiculo> vehiculosDelCliente = new ArrayList<>();
        for (Vehiculo v : repoVehiculos.obtenerTodos()) {
            if (v.getPropietario() != null && v.getPropietario().getCodigo().equalsIgnoreCase(codigoCliente)) {
                vehiculosDelCliente.add(v);
            }
        }
        return vehiculosDelCliente;
    }

    public void verificarAlertasInventario() {
        StringBuilder alerta = new StringBuilder("¡ATENCIÓN! Repuestos con stock bajo:\n");
        boolean hayAlertas = false;

        for (Inventario repuesto : repoRepuestos.obtenerTodos()) {
            if (repuesto.getCantidadStock() <= repuesto.getStockMinimo()) {
                alerta.append("- ").append(repuesto.getNombre())
                      .append(" (Stock actual: ").append(repuesto.getCantidadStock())
                      .append(", Mínimo: ").append(repuesto.getStockMinimo()).append(")\n");
                hayAlertas = true;
            }
        }

        if (hayAlertas) {
            JOptionPane.showMessageDialog(null, alerta.toString(), "Alerta de Inventario", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    public void agregarRepuestoAOrden(OrdenTrabajo orden, String codigoRepuesto, int cantidad) throws Exception {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero.");
        }
        if (orden.isInventarioDescontado()) {
            throw new IllegalStateException("No se pueden agregar repuestos a una orden ya descontada.");
        }
        Inventario repuestoOriginal = repoRepuestos.buscar(codigoRepuesto);
        
        if (repuestoOriginal == null) {
            throw new Exception("El repuesto con código " + codigoRepuesto + " no fue encontrado en el inventario.");
        }
        
        long yaAgregados = orden.getRepuestos().stream().filter(r -> r == repuestoOriginal).count();
        if (yaAgregados + cantidad > repuestoOriginal.getCantidadStock()) {
            throw new StockInsuficienteException("Stock insuficiente para " + repuestoOriginal.getNombre() + 
                ". Stock disponible: " + repuestoOriginal.getCantidadStock() + ", Solicitado: " + cantidad);
        }
        
        for (int i = 0; i < cantidad; i++) {
            orden.agregarRepuesto(repuestoOriginal);
        }
    }

    public void descontarInventarioOrden(OrdenTrabajo orden) throws StockInsuficienteException {
        if (orden.isInventarioDescontado()) return;

        java.util.Map<Inventario, Integer> cantidades = new java.util.HashMap<>();
        for (Inventario r : orden.getRepuestos()) {
            cantidades.merge(r, 1, Integer::sum);
        }
        for (java.util.Map.Entry<Inventario, Integer> entrada : cantidades.entrySet()) {
            if (entrada.getKey().getCantidadStock() < entrada.getValue()) {
                throw new StockInsuficienteException("Stock insuficiente para el repuesto: " + entrada.getKey().getNombre());
            }
        }
        for (java.util.Map.Entry<Inventario, Integer> entrada : cantidades.entrySet()) {
            Inventario r = entrada.getKey();
            r.setCantidadStock(r.getCantidadStock() - entrada.getValue());
        }
        orden.setInventarioDescontado(true);
    }

    public ProcesoServicioHilo procesarOrdenMultihilo(OrdenTrabajo orden, JProgressBar progressBar, Runnable onComplete) {
        if (orden.getEstado() != OrdenTrabajo.EstadoOrden.PENDIENTE || orden.isInventarioDescontado()) {
            throw new IllegalStateException("La orden ya fue procesada o no está pendiente.");
        }
        orden.setEstado(OrdenTrabajo.EstadoOrden.EN_PROCESO);
        ProcesoServicioHilo hilo = new ProcesoServicioHilo(orden, progressBar, () -> {
            try {
                descontarInventarioOrden(orden);
            } catch (StockInsuficienteException ex) {
                throw new IllegalStateException(ex.getMessage(), ex);
            }
        }, onComplete);
        hilo.execute();
        return hilo;
    }
}
