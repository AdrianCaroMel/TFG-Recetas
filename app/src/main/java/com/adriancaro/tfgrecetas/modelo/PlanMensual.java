package com.adriancaro.tfgrecetas.modelo;

import java.util.ArrayList;
import java.util.List;

/**
 * Resultado del planificador: los dias con sus recetas, la lista de la compra y el coste.
 */
public class PlanMensual {

    private final List<DiaPlan> dias = new ArrayList<DiaPlan>();
    private List<LineaCompra> listaCompra = new ArrayList<LineaCompra>();
    private double costeTotal;
    private double presupuesto;
    private boolean completo = true;
    private String aviso = "";
    private double presupuestoMinimo = -1; // solo se rellena si el presupuesto no llega
    private Supermercado supermercado;     // si toda la compra se hace en un solo super (si no, null)

    public double getSobrante() {
        return presupuesto - costeTotal;
    }

    public List<DiaPlan> getDias() { return dias; }
    public List<LineaCompra> getListaCompra() { return listaCompra; }
    public void setListaCompra(List<LineaCompra> listaCompra) { this.listaCompra = listaCompra; }
    public double getCosteTotal() { return costeTotal; }
    public void setCosteTotal(double costeTotal) { this.costeTotal = costeTotal; }
    public double getPresupuesto() { return presupuesto; }
    public void setPresupuesto(double presupuesto) { this.presupuesto = presupuesto; }
    public boolean isCompleto() { return completo; }
    public void setCompleto(boolean completo) { this.completo = completo; }
    public String getAviso() { return aviso; }
    public void setAviso(String aviso) { this.aviso = aviso; }
    public Supermercado getSupermercado() { return supermercado; }
    public void setSupermercado(Supermercado supermercado) { this.supermercado = supermercado; }
    public double getPresupuestoMinimo() { return presupuestoMinimo; }
    public void setPresupuestoMinimo(double presupuestoMinimo) { this.presupuestoMinimo = presupuestoMinimo; }
}
