package com.adriancaro.tfgrecetas.modelo;

/**
 * Momentos del dia que planifica la app.
 */
public enum Momento {
    COMIDA("Comida"),
    CENA("Cena");

    private final String nombreVisible;

    Momento(String nombreVisible) {
        this.nombreVisible = nombreVisible;
    }

    public String getNombreVisible() {
        return nombreVisible;
    }
}
