package com.adriancaro.tfgrecetas.modelo;

/**
 * Una linea de la lista de la compra: cuantos envases de un producto
 * hay que comprar en el mes y en que supermercado sale mas barato.
 */
public class LineaCompra {

    private final Producto producto;
    private final double cantidadNecesaria;
    private final int envases;
    private final Supermercado supermercado;
    private final double precioTotal;

    public LineaCompra(Producto producto, double cantidadNecesaria, int envases,
                       Supermercado supermercado, double precioTotal) {
        this.producto = producto;
        this.cantidadNecesaria = cantidadNecesaria;
        this.envases = envases;
        this.supermercado = supermercado;
        this.precioTotal = precioTotal;
    }

    public Producto getProducto() { return producto; }
    public double getCantidadNecesaria() { return cantidadNecesaria; }
    public int getEnvases() { return envases; }
    public Supermercado getSupermercado() { return supermercado; }
    public double getPrecioTotal() { return precioTotal; }
}
