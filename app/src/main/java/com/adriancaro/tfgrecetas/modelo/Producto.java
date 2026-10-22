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

    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getCategoria() { return categoria; }
    public String getUnidad() { return unidad; }
    public double getEnvase() { return envase; }
    public List<String> getAlergenos() { return alergenos; }
    public String getOrigen() { return origen; }
}
