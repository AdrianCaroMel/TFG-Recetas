package com.adriancaro.tfgrecetas.modelo;

import java.util.ArrayList;
import java.util.List;

/**
 * Todo lo que el usuario nos cuenta de su situacion.
 */
public class PerfilUsuario {

    private List<Supermercado> supermercados = new ArrayList<Supermercado>();
    private List<Restriccion> restricciones = new ArrayList<Restriccion>();
    private double presupuesto;           // euros al mes para comida
    private int personas = 1;             // personas que comen en casa
    private boolean planificarComida = true;
    private boolean planificarCena = true;
    private boolean tengoBasicos = false; // ya tiene aceite, sal y especias en casa
    private int dias = 30;                // dias que dura el plan
    private long semilla = 1;             // cambia al pulsar "Regenerar" para dar otro plan

    /** Lista de momentos (comida / cena) que hay que rellenar cada dia. */
    public List<Momento> getMomentos() {
        List<Momento> momentos = new ArrayList<Momento>();
        if (planificarComida) {
            momentos.add(Momento.COMIDA);
        }
        if (planificarCena) {
            momentos.add(Momento.CENA);
        }
        return momentos;
    }

    public List<Supermercado> getSupermercados() { return supermercados; }
    public void setSupermercados(List<Supermercado> supermercados) { this.supermercados = supermercados; }
    public List<Restriccion> getRestricciones() { return restricciones; }
    public void setRestricciones(List<Restriccion> restricciones) { this.restricciones = restricciones; }
    public double getPresupuesto() { return presupuesto; }
    public void setPresupuesto(double presupuesto) { this.presupuesto = presupuesto; }
    public int getPersonas() { return personas; }
    public void setPersonas(int personas) { this.personas = personas; }
    public boolean isPlanificarComida() { return planificarComida; }
    public void setPlanificarComida(boolean planificarComida) { this.planificarComida = planificarComida; }
    public boolean isPlanificarCena() { return planificarCena; }
    public void setPlanificarCena(boolean planificarCena) { this.planificarCena = planificarCena; }
    public boolean isTengoBasicos() { return tengoBasicos; }
    public void setTengoBasicos(boolean tengoBasicos) { this.tengoBasicos = tengoBasicos; }
    public int getDias() { return dias; }
    public void setDias(int dias) { this.dias = dias; }
    public long getSemilla() { return semilla; }
    public void setSemilla(long semilla) { this.semilla = semilla; }
}
