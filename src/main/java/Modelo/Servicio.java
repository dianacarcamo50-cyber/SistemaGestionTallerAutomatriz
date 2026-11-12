/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Modelo;

/**
 *
 * @author Garme
 */

public class Servicio {

    private String codigo;
    private String descripcion;
    private double precioBase;
    private double tiempoEstimadoHoras;

    public Servicio(String codigo, String descripcion, double precioBase, double tiempoEstimadoHoras) {
        this.codigo = codigo;
        this.descripcion = descripcion;
        if (!Double.isFinite(precioBase) || precioBase <= 0) {
            throw new IllegalArgumentException("El precio base debe ser positivo.");
        }
        this.precioBase = precioBase;
        this.tiempoEstimadoHoras = tiempoEstimadoHoras;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public double getPrecioBase() {
        return precioBase;
    }

    public double getTiempoEstimadoHoras() {
        return tiempoEstimadoHoras;
    }

    @Override
    public String toString() {
        return descripcion + " (L. " + precioBase + ")";
    }
}
