/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Modelo;
import Excepciones.PagoInsuficienteException;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 *
 * @author Garme
 */

public class Factura {

    private String numeroFactura;
    private OrdenTrabajo orden;
    private double impuestoPorcentaje;
    private BigDecimal subtotalServicios;
    private BigDecimal subtotalRepuestos;
    private BigDecimal total;
    private boolean pagada;

    public Factura(String numeroFactura, OrdenTrabajo orden, double impuestoPorcentaje) {
        if (numeroFactura == null || numeroFactura.trim().isEmpty() || orden == null
                || orden.getEstado() != OrdenTrabajo.EstadoOrden.FINALIZADA) {
            throw new IllegalArgumentException("Se requiere un número de factura y una orden finalizada.");
        }
        if (!Double.isFinite(impuestoPorcentaje) || impuestoPorcentaje < 0) {
            throw new IllegalArgumentException("El impuesto debe ser un porcentaje no negativo.");
        }
        this.numeroFactura = numeroFactura;
        this.orden = orden;
        this.impuestoPorcentaje = impuestoPorcentaje;
        calcularTotales();
    }

    private void calcularTotales() {
        subtotalServicios = orden.getServicios().stream()
                .map(s -> BigDecimal.valueOf(s.getPrecioBase()))
                .reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);
        subtotalRepuestos = orden.getRepuestos().stream()
                .map(r -> BigDecimal.valueOf(r.getPrecioUnitario()))
                .reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);
        BigDecimal subtotal = subtotalServicios.add(subtotalRepuestos);
        BigDecimal impuesto = subtotal.multiply(BigDecimal.valueOf(impuestoPorcentaje))
                .divide(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP);
        total = subtotal.add(impuesto);
    }

    public double procesarPagoEfectivo(double montoEntregado) throws PagoInsuficienteException {
        if (pagada) {
            throw new IllegalStateException("La factura ya fue pagada.");
        }
        if (!Double.isFinite(montoEntregado) || montoEntregado < 0) {
            throw new IllegalArgumentException("Ingrese un monto válido.");
        }
        BigDecimal monto = BigDecimal.valueOf(montoEntregado);
        if (monto.stripTrailingZeros().scale() > 2) {
            throw new IllegalArgumentException("El monto debe tener como máximo dos decimales.");
        }
        if (monto.compareTo(total) < 0) {
            throw new PagoInsuficienteException("El monto entregado (L. " + monto
                    + ") es insuficiente. Total a pagar: L. " + total);
        }
        pagada = true;
        return monto.subtract(total).doubleValue();
    }

    public double getImpuestoPorcentaje() { return impuestoPorcentaje; }

    public boolean isPagada() { return pagada; }

    public String getNumeroFactura() {
        return numeroFactura;
    }

    public OrdenTrabajo getOrden() {
        return orden;
    }

    public double getSubtotalServicios() {
        return subtotalServicios.doubleValue();
    }

    public double getSubtotalRepuestos() {
        return subtotalRepuestos.doubleValue();
    }

    public double getTotal() {
        return total.doubleValue();
    }
}
