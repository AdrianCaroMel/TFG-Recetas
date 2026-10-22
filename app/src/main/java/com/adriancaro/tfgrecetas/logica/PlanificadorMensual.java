package com.adriancaro.tfgrecetas.logica;

import com.adriancaro.tfgrecetas.modelo.DiaPlan;
import com.adriancaro.tfgrecetas.modelo.Momento;
import com.adriancaro.tfgrecetas.modelo.PerfilUsuario;
import com.adriancaro.tfgrecetas.modelo.PlanMensual;
import com.adriancaro.tfgrecetas.modelo.Receta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

/**
 * Corazon de la app: reparte recetas por los dias del mes sin pasarse del presupuesto.
 *
 * Algoritmo voraz (greedy) en dos pasadas:
 *  1. Modo ahorro: se elige siempre la receta que menos sube el gasto. Da el coste
 *     minimo aproximado del mes. Si ni asi llega el presupuesto, se avisa al usuario.
 *  2. Modo normal: si sobra dinero se usa para dar variedad. Cada hueco (dia + momento)
 *     se rellena con la receta de menor "puntuacion":
 *        coste extra + penalizacion por repetirla (mas si fue hace poco) + un poco de azar
 *     y siempre dejando reservado lo necesario para los huecos que quedan.
 */
public class PlanificadorMensual {

    public static PlanMensual generar(PerfilUsuario perfil, List<Receta> todasLasRecetas) {
        List<Momento> momentos = perfil.getMomentos();
        List<Receta> validas = FiltroRecetas.recetasValidas(todasLasRecetas, perfil);

        // Candidatas para cada momento del dia
        Map<Momento, List<Receta>> candidatas = new HashMap<Momento, List<Receta>>();
        for (Momento m : momentos) {
            List<Receta> lista = new ArrayList<Receta>();
            for (Receta r : validas) {
                if (r.sirvePara(m)) {
                    lista.add(r);
                }
            }
            if (lista.isEmpty()) {
                PlanMensual vacio = new PlanMensual();
                vacio.setPresupuesto(perfil.getPresupuesto());
                vacio.setCompleto(false);
                vacio.setAviso("No hay recetas de " + m.getNombreVisible().toLowerCase(Locale.ROOT)
                        + " compatibles con tus restricciones y supermercados. Prueba a elegir mas supermercados.");
                return vacio;
            }
            candidatas.put(m, lista);
        }

        if (momentos.isEmpty()) {
            PlanMensual vacio = new PlanMensual();
            vacio.setPresupuesto(perfil.getPresupuesto());
            vacio.setCompleto(false);
            vacio.setAviso("Elige al menos comida o cena para poder hacer el plan.");
            return vacio;
        }

        // Pasada 1: lo mas barato posible, sin limite de dinero
        PlanMensual barato = construir(perfil, candidatas, Double.MAX_VALUE, 0, true);
        double costeMinimo = barato.getCosteTotal();

        if (costeMinimo > perfil.getPresupuesto()) {
            // No llega ni en modo ahorro: plan hasta donde de el dinero + aviso
            PlanMensual parcial = construir(perfil, candidatas, perfil.getPresupuesto(), 0, true);
            if (parcial.isCompleto()) {
                return parcial; // con el limite puesto ha encontrado otra combinacion que si entra
            }
            parcial.setPresupuestoMinimo(costeMinimo);
            parcial.setAviso(String.format(Locale.ROOT,
                    "Con %.2f euros no llega para todo el mes. Necesitarias unos %.2f euros.",
                    perfil.getPresupuesto(), costeMinimo));
            return parcial;
        }

        // Pasada 2: plan variado respetando el presupuesto
        PlanMensual plan = construir(perfil, candidatas, perfil.getPresupuesto(), costeMinimo, false);
        if (!plan.isCompleto()) {
            // Muy raro, pero si el azar nos deja sin dinero devolvemos el plan barato
            barato.setPresupuesto(perfil.getPresupuesto());
            return barato;
        }
        return plan;
    }

    /**
     * Rellena los huecos del mes uno a uno.
     *
     * @param presupuesto limite de gasto
     * @param costeMinimo coste del plan barato, sirve para reservar dinero para los huecos que faltan
     * @param modoAhorro  true = solo importa el precio (sin azar ni variedad extra)
     */
    private static PlanMensual construir(PerfilUsuario perfil, Map<Momento, List<Receta>> candidatas,
                                         double presupuesto, double costeMinimo, boolean modoAhorro) {
        List<Momento> momentos = perfil.getMomentos();
        Random azar = new Random(perfil.getSemilla());
        CestaCompra cesta = new CestaCompra(perfil);
        PlanMensual plan = new PlanMensual();
        plan.setPresupuesto(perfil.getPresupuesto());

        Map<Integer, Integer> usos = new HashMap<Integer, Integer>();
        Map<Integer, Integer> ultimoDia = new HashMap<Integer, Integer>(); // ultimo dia que se uso cada receta
        Map<Momento, Receta> anterior = new HashMap<Momento, Receta>();
        int totalHuecos = perfil.getDias() * momentos.size();
        int huecosRestantes = totalHuecos;
        double reservaPorHueco = costeMinimo / totalHuecos;

        for (int dia = 1; dia <= perfil.getDias(); dia++) {
            DiaPlan diaPlan = new DiaPlan(dia);
            Receta elegidaHoy = null;

            for (Momento m : momentos) {
                List<Receta> lista = candidatas.get(m);
                // Maximo de veces que se puede repetir una receta en el mes
                int maxUsos = Math.max(4, (int) Math.ceil((double) perfil.getDias() / lista.size()) + 1);
                // Dinero que "toca" por hueco: si sobra mucho, mas peso tiene la variedad
                double margen = 0;
                if (!modoAhorro) {
                    margen = (presupuesto - cesta.getCosteTotal()) / huecosRestantes;
                }
                double reserva = reservaPorHueco * (huecosRestantes - 1);

                // Primero con todas las normas; si no sale nada, sin las de variedad
                Receta mejor = elegir(lista, cesta, usos, ultimoDia, dia, anterior.get(m), elegidaHoy, maxUsos,
                        presupuesto - reserva, margen, azar, modoAhorro, true);
                if (mejor == null) {
                    mejor = elegir(lista, cesta, usos, ultimoDia, dia, null, null, Integer.MAX_VALUE,
                            presupuesto - reserva, margen, azar, modoAhorro, false);
                }
                if (mejor == null) {
                    // No queda dinero para este hueco
                    plan.setCompleto(false);
                    if (diaPlan.getComida() != null || diaPlan.getCena() != null) {
                        plan.getDias().add(diaPlan);
                    }
                    return terminar(plan, cesta);
                }

                cesta.anadir(mejor);
                Integer veces = usos.get(mejor.getId());
                usos.put(mejor.getId(), veces == null ? 1 : veces + 1);
                ultimoDia.put(mejor.getId(), dia);
                anterior.put(m, mejor);
                elegidaHoy = mejor;
                diaPlan.setReceta(m, mejor);
                huecosRestantes--;
            }
            plan.getDias().add(diaPlan);
        }
        return terminar(plan, cesta);
    }

    private static Receta elegir(List<Receta> lista, CestaCompra cesta, Map<Integer, Integer> usos,
                                 Map<Integer, Integer> ultimoDia, int dia, Receta anterior,
                                 Receta elegidaHoy, int maxUsos, double limite,
                                 double margen, Random azar, boolean modoAhorro, boolean conVariedad) {
        Receta mejor = null;
        double mejorPuntuacion = Double.MAX_VALUE;

        for (Receta r : lista) {
            Integer veces = usos.get(r.getId());
            int numUsos = veces == null ? 0 : veces;

            if (conVariedad && lista.size() > 2) {
                // no repetir lo mismo que ayer en este momento ni lo mismo que ya se come hoy
                if (r == anterior || r == elegidaHoy) {
                    continue;
                }
            }
            if (numUsos >= maxUsos) {
                continue;
            }

            double costeExtra = cesta.costeAnadir(r);
            if (cesta.getCosteTotal() + costeExtra > limite + 1e-9) {
                continue; // se pasaria del presupuesto
            }

            // Cuanto mas reciente fue la ultima vez, mas penaliza (ayer > hace una semana)
            double recencia = 0;
            Integer ultimo = ultimoDia.get(r.getId());
            if (ultimo != null) {
                recencia = 1.0 / (dia - ultimo + 1);
            }

            double puntuacion = costeExtra;
            if (modoAhorro) {
                puntuacion += numUsos * 0.01 + recencia * 0.05; // solo para desempatar
            } else {
                puntuacion += numUsos * margen * 0.6 + recencia * margen * 3 + azar.nextDouble() * margen;
            }

            if (puntuacion < mejorPuntuacion) {
                mejorPuntuacion = puntuacion;
                mejor = r;
            }
        }
        return mejor;
    }

    private static PlanMensual terminar(PlanMensual plan, CestaCompra cesta) {
        plan.setCosteTotal(cesta.getCosteTotal());
        plan.setListaCompra(cesta.generarLista());
        return plan;
    }
}
