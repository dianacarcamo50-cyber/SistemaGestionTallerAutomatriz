/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Modelo;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
/**
 *
 * @author Garme
 */

public class OrdenTrabajo {

    public enum EstadoOrden {
        PENDIENTE, EN_PROCESO, FINALIZADA, CANCELADA
    }

    private String numeroOrden;
    private Date fecha;
    private Cliente cliente;
    private Vehiculo vehiculo;
    private String diagnostico;
    private String mecanico;
    private List<Servicio> servicios;
    private List<Inventario> repuestos;
    private EstadoOrden estado;
    private boolean inventarioDescontado;

    public OrdenTrabajo(String numeroOrden, Cliente cliente, Vehiculo vehiculo, String diagnostico, String mecanico) {
        this.numeroOrden = numeroOrden;
        this.fecha = new Date();
        this.cliente = cliente;
        this.vehiculo = vehiculo;
        this.diagnostico = diagnostico;
        this.mecanico = mecanico;
        this.servicios = new ArrayList<>();
        this.repuestos = new ArrayList<>();
        this.estado = EstadoOrden.PENDIENTE;
        this.inventarioDescontado = false;
    }

    public void agregarServicio(Servicio s) {
        servicios.add(s);
    }

    public void agregarRepuesto(Inventario r) {
        repuestos.add(r);
    }

    public double calcularCostoTotal() {
        double totalServicios = servicios.stream().mapToDouble(Servicio::getPrecioBase).sum();
        double totalRepuestos = repuestos.stream().mapToDouble(Inventario::getPrecioUnitario).sum();
        return totalServicios + totalRepuestos;
    }

    public String getNumeroOrden() {
        return numeroOrden;
    }

    public Date getFecha() {
        return fecha;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public Vehiculo getVehiculo() {
        return vehiculo;
    }

    public String getDiagnostico() {
        return diagnostico;
    }

    public String getMecanico() {
        return mecanico;
    }

    public List<Servicio> getServicios() {
        return servicios;
    }

    public List<Inventario> getRepuestos() {
        return repuestos;
    }

    public EstadoOrden getEstado() {
        return estado;
    }

    public void setEstado(EstadoOrden estado) {
        this.estado = estado;
    }

    public boolean isInventarioDescontado() {
        return inventarioDescontado;
    }

    public void setInventarioDescontado(boolean inventarioDescontado) {
        this.inventarioDescontado = inventarioDescontado;
    }
}
