package com.adriancaro.tfgrecetas.modelo;

/**
 * Producto + cantidad que necesita una receta para UNA persona.
 */
public class Ingrediente {

    private final Producto producto;
    private final double cantidad;

    public Ingrediente(Producto producto, double cantidad) {
        this.producto = producto;
        this.cantidad = cantidad;
    }

    public Producto getProducto() { return producto; }
    public double getCantidad() { return cantidad; }
}
