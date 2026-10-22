package com.adriancaro.tfgrecetas.modelo;

/**
 * Un dia del plan: que se come y que se cena (null si ese momento no se planifica).
 */
public class DiaPlan {

    private final int numero;
    private Receta comida;
    private Receta cena;

    public DiaPlan(int numero) {
        this.numero = numero;
    }

    public void setReceta(Momento momento, Receta receta) {
        if (momento == Momento.COMIDA) {
            comida = receta;
        } else {
            cena = receta;
        }
    }

    public Receta getReceta(Momento momento) {
        if (momento == Momento.COMIDA) {
            return comida;
        }
        return cena;
    }

    /** Copia del dia (las recetas se comparten, no se copian). */
    public DiaPlan copiar() {
        DiaPlan copia = new DiaPlan(numero);
        copia.comida = comida;
        copia.cena = cena;
        return copia;
    }

    public int getNumero() { return numero; }
    public Receta getComida() { return comida; }
    public Receta getCena() { return cena; }
}
