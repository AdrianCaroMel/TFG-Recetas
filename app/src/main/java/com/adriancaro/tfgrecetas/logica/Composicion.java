package com.adriancaro.tfgrecetas.logica;

import com.adriancaro.tfgrecetas.modelo.Ingrediente;
import com.adriancaro.tfgrecetas.modelo.Receta;

import java.util.ArrayList;
import java.util.List;

/**
 * De qué está hecho un plato: cada ingrediente con su grupo de alimentos y la parte del plato que ocupa.
 * Sirve para dibujar el plato (PlatoView) sin usar emojis. No depende de Android.
 */
public final class Composicion {

    public static final String CEREAL = "CEREAL";
    public static final String VERDURA = "VERDURA";
    public static final String LEGUMBRE = "LEGUMBRE";
    public static final String CARNE = "CARNE";
    public static final String PESCADO = "PESCADO";
    public static final String LACTEO = "LACTEO";
    public static final String HUEVO = "HUEVO";
    public static final String CONDIMENTO = "CONDIMENTO";
    public static final String DESPENSA = "DESPENSA";

    /** Peso que se le da a un ingrediente que se cuenta en unidades (un huevo, una tortilla de trigo...). */
    private static final double GRAMOS_POR_UNIDAD = 50;
    /** Por debajo de esto (sal, especias) no se dibuja: no se vería. */
    private static final double MINIMO_GRAMOS = 3;

    /** Un trozo del plato. */
    public static class Parte {
        private final String grupo;
        private final double fraccion;

        Parte(String grupo, double fraccion) {
            this.grupo = grupo;
            this.fraccion = fraccion;
        }

        public String getGrupo() { return grupo; }
        public double getFraccion() { return fraccion; }
    }

    private Composicion() {
    }

    /** Partes del plato en el orden de la receta; las fracciones suman 1. */
    public static List<Parte> de(Receta receta) {
        List<String> grupos = new ArrayList<String>();
        List<Double> pesos = new ArrayList<Double>();
        double total = 0;
        for (Ingrediente ing : receta.getIngredientes()) {
            double peso = ing.getProducto().getUnidad().equals("ud")
                    ? ing.getCantidad() * GRAMOS_POR_UNIDAD : ing.getCantidad();
            if (peso >= MINIMO_GRAMOS) {
                grupos.add(grupoDe(ing.getProducto().getCategoria()));
                pesos.add(peso);
                total += peso;
            }
        }
        List<Parte> partes = new ArrayList<Parte>();
        for (int i = 0; i < grupos.size(); i++) {
            partes.add(new Parte(grupos.get(i), pesos.get(i) / total));
        }
        return partes;
    }

    /** Grupo de alimentos a partir de la categoría del producto en productos.csv. */
    public static String grupoDe(String categoria) {
        switch (categoria) {
            case "Cereales y pasta":
                return CEREAL;
            case "Verduras":
            case "Congelados":
                return VERDURA;
            case "Legumbres":
            case "Proteína vegetal":
                return LEGUMBRE;
            case "Carne":
                return CARNE;
            case "Pescado":
            case "Marisco":
                return PESCADO;
            case "Lácteos":
                return LACTEO;
            case "Huevos":
                return HUEVO;
            case "Básicos":
            case "Salsas":
                return CONDIMENTO;
            default:
                return DESPENSA;
        }
    }
}
