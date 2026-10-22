package com.adriancaro.tfgrecetas.modelo;

/**
 * Supermercados con los que trabaja la app.
 * El orden importa: coincide con el orden de las columnas de precio en productos.csv
 */
public enum Supermercado {

    MERCADONA("Mercadona"),
    DIA("DIA"),
    LIDL("Lidl"),
    ALDI("Aldi"),
    ALCAMPO("Alcampo"),
    CARREFOUR("Carrefour");

    private final String nombreVisible;

    Supermercado(String nombreVisible) {
        this.nombreVisible = nombreVisible;
    }

    public String getNombreVisible() {
        return nombreVisible;
    }
}
