/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Modelo;

/**
 *
 * @author Garme
 */
public class Inventario {

    private String codigo;
    private String nombre;
    private int cantidadStock;
    private double precioUnitario;
    private int stockMinimo;

    public Inventario(String codigo, String nombre, double precioUnitario, int cantidadStock, int stockMinimo) {
        if (codigo == null || codigo.trim().isEmpty() || nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El código y el nombre son obligatorios.");
        }
        if (!Double.isFinite(precioUnitario) || precioUnitario < 0) {
            throw new IllegalArgumentException("El precio debe ser un número no negativo.");
        }
        this.codigo = codigo.trim();
        this.nombre = nombre.trim();
        setCantidadStock(cantidadStock);
        this.precioUnitario = precioUnitario;
        if (stockMinimo < 0) {
            throw new IllegalArgumentException("El stock mínimo no puede ser negativo.");
        }
        this.stockMinimo = stockMinimo;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public int getCantidadStock() {
        return cantidadStock;
    }

    public void setCantidadStock(int cantidadStock) {
        if (cantidadStock < 0) {
            throw new IllegalArgumentException("La cantidad en stock no puede ser negativa.");
        }
        this.cantidadStock = cantidadStock;
    }

    public void registrarEntrada(int cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad de entrada debe ser mayor que cero.");
        }
        if (cantidad > Integer.MAX_VALUE - cantidadStock) {
            throw new IllegalArgumentException("La cantidad excede el máximo permitido.");
        }
        cantidadStock += cantidad;
    }

    public double getPrecioUnitario() {
        return precioUnitario;
    }

    public int getStockMinimo() {
        return stockMinimo;
    }

    @Override
    public String toString() {
        return nombre + " (Stock: " + cantidadStock + ")";
    }
}
