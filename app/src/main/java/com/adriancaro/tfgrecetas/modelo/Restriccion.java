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
    VEGANO("Vegana"),
    // Dietas por enfermedad o recomendacion medica
    DIETA_BLANDA("Dieta blanda (estómago delicado)"),
    BAJA_EN_SAL("Baja en sal (tensión alta)"),
    CONTROL_AZUCAR("Control de azúcar (diabetes)"),
    BAJA_EN_GRASA("Baja en grasa (colesterol)");

    /** Maximo de hidratos de carbono por racion en la dieta de control de azucar (g). */
    public static final double MAX_HIDRATOS_DIABETES = 65;
    /** Maximo de grasa por racion en las dietas blanda y baja en grasa (g). */
    public static final double MAX_GRASAS = 20;

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

    /** True para las dietas por enfermedad (blanda, baja en sal...). */
    public boolean esDietaMedica() {
        return this == DIETA_BLANDA || this == BAJA_EN_SAL || this == CONTROL_AZUCAR || this == BAJA_EN_GRASA;
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
            case DIETA_BLANDA:
                return !p.tieneEtiqueta("IRRITANTE") && !p.tieneEtiqueta("INDIGESTO") && !p.tieneEtiqueta("GRASO");
            case BAJA_EN_SAL:
                return !p.tieneEtiqueta("SALADO");
            case BAJA_EN_GRASA:
                return !p.tieneEtiqueta("GRASO");
            default:
                return true;
        }
    }
}
