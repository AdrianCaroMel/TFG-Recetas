package com.adriancaro.tfgrecetas.modelo;

/**
 * Restricciones alimenticias que puede marcar el usuario:
 * alergias / intolerancias (miran los alergenos del producto)
 * y dietas (miran el origen del producto).
 */
public enum Restriccion {

    SIN_GLUTEN("Sin gluten (celiaquía)"),
    SIN_LACTOSA("Sin lactosa"),
    SIN_HUEVO("Sin huevo"),
    SIN_FRUTOS_SECOS("Sin frutos secos"),
    SIN_PESCADO("Sin pescado"),
    SIN_MARISCO("Sin marisco"),
    SIN_SOJA("Sin soja"),
    VEGETARIANO("Vegetariana"),
    VEGANO("Vegana");

    private final String nombreVisible;

    Restriccion(String nombreVisible) {
        this.nombreVisible = nombreVisible;
    }

    public String getNombreVisible() {
        return nombreVisible;
    }

    /** True para las dietas (vegetariana, vegana); false para alergias e intolerancias. */
    public boolean esDieta() {
        return this == VEGETARIANO || this == VEGANO;
    }

    /**
     * Devuelve true si el producto se puede comer con esta restriccion.
     */
    public boolean permite(Producto p) {
        String origen = p.getOrigen();
        switch (this) {
            case SIN_GLUTEN:
                return !p.tieneAlergeno("GLUTEN");
            case SIN_LACTOSA:
                return !p.tieneAlergeno("LACTOSA");
            case SIN_HUEVO:
                return !p.tieneAlergeno("HUEVO");
            case SIN_FRUTOS_SECOS:
                return !p.tieneAlergeno("FRUTOS_SECOS");
            case SIN_PESCADO:
                return !p.tieneAlergeno("PESCADO");
            case SIN_MARISCO:
                return !p.tieneAlergeno("MARISCO");
            case SIN_SOJA:
                return !p.tieneAlergeno("SOJA");
            case VEGETARIANO:
                // fuera carne, pescado y marisco
                return !origen.equals("CARNE") && !origen.equals("PESCADO") && !origen.equals("MARISCO");
            case VEGANO:
                // solo productos de origen vegetal
                return origen.equals("VEGETAL");
            default:
                return true;
        }
    }
}
