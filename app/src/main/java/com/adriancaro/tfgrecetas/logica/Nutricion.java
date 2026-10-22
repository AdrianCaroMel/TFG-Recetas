package com.adriancaro.tfgrecetas.logica;

import com.adriancaro.tfgrecetas.modelo.Ingrediente;
import com.adriancaro.tfgrecetas.modelo.Receta;

/**
 * Informacion nutricional aproximada de UNA racion de una receta
 * (las cantidades de las recetas ya estan pensadas para una persona).
 */
public class Nutricion {

    /** Referencia de un adulto medio que se usa en el etiquetado de alimentos en la UE. */
    public static final double KCAL_DIARIAS_REFERENCIA = 2000;

    private final double kcal;
    private final double proteinas;
    private final double hidratos;
    private final double grasas;
    private final double pesoGramos;

    private Nutricion(double kcal, double proteinas, double hidratos, double grasas, double pesoGramos) {
        this.kcal = kcal;
        this.proteinas = proteinas;
        this.hidratos = hidratos;
        this.grasas = grasas;
        this.pesoGramos = pesoGramos;
    }

    public static Nutricion deRacion(Receta receta) {
        double kcal = 0, proteinas = 0, hidratos = 0, grasas = 0, peso = 0;
        for (Ingrediente ing : receta.getIngredientes()) {
            double[] n = ing.getProducto().nutrientesDe(ing.getCantidad());
            kcal += n[0];
            proteinas += n[1];
            hidratos += n[2];
            grasas += n[3];
            // Peso aproximado de lo que se sirve (los ml de aceite o caldo cuentan como gramos;
            // las unidades no se cuentan porque su peso varia mucho)
            if (!ing.getProducto().getUnidad().equals("ud")) {
                peso += ing.getCantidad();
            }
        }
        return new Nutricion(kcal, proteinas, hidratos, grasas, peso);
    }

    /** Porcentaje de las kcal de referencia de un dia que aporta la racion. */
    public int porcentajeDiario() {
        return (int) Math.round(kcal * 100 / KCAL_DIARIAS_REFERENCIA);
    }

    public double getKcal() { return kcal; }
    public double getProteinas() { return proteinas; }
    public double getHidratos() { return hidratos; }
    public double getGrasas() { return grasas; }
    public double getPesoGramos() { return pesoGramos; }
}
