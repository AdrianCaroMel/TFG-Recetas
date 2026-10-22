package com.adriancaro.tfgrecetas.modelo;

import java.util.ArrayList;
import java.util.List;

/**
 * Una receta con sus ingredientes (para una persona) y sus pasos.
 */
public class Receta {

    private final int id;
    private final String nombre;
    private final String tipo;        // COMIDA, CENA o AMBAS
    private final int minutos;
    private final List<String> pasos;
    private final List<Ingrediente> ingredientes = new ArrayList<Ingrediente>();

    public Receta(int id, String nombre, String tipo, int minutos, List<String> pasos) {
        this.id = id;
        this.nombre = nombre;
        this.tipo = tipo;
        this.minutos = minutos;
        this.pasos = pasos;
    }

    public void anadirIngrediente(Ingrediente ingrediente) {
        ingredientes.add(ingrediente);
    }

    /** Si la receta se puede poner en ese momento del dia. */
    public boolean sirvePara(Momento momento) {
        return tipo.equals("AMBAS") || tipo.equals(momento.name());
    }

    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getTipo() { return tipo; }
    public int getMinutos() { return minutos; }
    public List<String> getPasos() { return pasos; }
    public List<Ingrediente> getIngredientes() { return ingredientes; }
}
