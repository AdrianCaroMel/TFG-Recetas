package com.adriancaro.tfgrecetas.modelo;

import java.util.List;

/**
 * Un producto del supermercado (lo que se compra), con su precio en cada cadena.
 */
public class Producto {

    private final int id;
    private final String nombre;
    private final String categoria;
    private final String unidad;      // g, ml o ud
    private final double envase;      // cantidad que trae un envase
    private final double[] precios;   // precio por envase en cada Supermercado (-1 = no lo vende)
    private final List<String> alergenos;
    private final String origen;      // VEGETAL, LACTEO, HUEVO, CARNE, PESCADO, MARISCO
    /** kcal, proteinas, hidratos y grasas por 100 g / 100 ml, o por unidad si la unidad es "ud". */
    private double[] nutrientes = new double[4];

    public Producto(int id, String nombre, String categoria, String unidad, double envase,
                    double[] precios, List<String> alergenos, String origen) {
        this.id = id;
        this.nombre = nombre;
        this.categoria = categoria;
        this.unidad = unidad;
        this.envase = envase;
        this.precios = precios;
        this.alergenos = alergenos;
        this.origen = origen;
    }

    public double precioEn(Supermercado s) {
        return precios[s.ordinal()];
    }

    public boolean seVendeEn(Supermercado s) {
        return precios[s.ordinal()] >= 0;
    }

    public boolean tieneAlergeno(String alergeno) {
        return alergenos.contains(alergeno);
    }

    /** Aceite, sal, especias... cosas que mucha gente ya tiene en casa. */
    public boolean esBasico() {
        return categoria.equals("Básicos");
    }

    /**
     * Valores nutricionales de una cantidad del producto (en su unidad):
     * {kcal, proteinas g, hidratos g, grasas g}.
     */
    public double[] nutrientesDe(double cantidad) {
        double factor = unidad.equals("ud") ? cantidad : cantidad / 100.0;
        return new double[]{nutrientes[0] * factor, nutrientes[1] * factor,
                nutrientes[2] * factor, nutrientes[3] * factor};
    }

    public void setNutrientes(double kcal, double proteinas, double hidratos, double grasas) {
        nutrientes = new double[]{kcal, proteinas, hidratos, grasas};
    }

    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getCategoria() { return categoria; }
    public String getUnidad() { return unidad; }
    public double getEnvase() { return envase; }
    public List<String> getAlergenos() { return alergenos; }
    public String getOrigen() { return origen; }
}
