package com.adriancaro.tfgrecetas.logica;

import com.adriancaro.tfgrecetas.modelo.DiaPlan;
import com.adriancaro.tfgrecetas.modelo.Ingrediente;
import com.adriancaro.tfgrecetas.modelo.Momento;
import com.adriancaro.tfgrecetas.modelo.PerfilUsuario;
import com.adriancaro.tfgrecetas.modelo.PlanMensual;
import com.adriancaro.tfgrecetas.modelo.Receta;
import com.adriancaro.tfgrecetas.modelo.Supermercado;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
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

    private static final Locale ES = Locale.forLanguageTag("es-ES");

    /**
     * Genera el plan del mes.
     *
     * Si el usuario quiere hacer toda la compra en un solo supermercado (opcion por defecto,
     * porque nadie va a cuatro tiendas para ahorrar unos centimos), se calcula el gasto minimo
     * del mes en cada uno de sus supermercados y se planifica en el mas barato.
     * Si prefiere repartir la compra, cada producto se compra donde sale mas barato.
     */
    public static PlanMensual generar(PerfilUsuario perfil, List<Receta> todasLasRecetas) {
        if (!perfil.isUnSoloSupermercado() || perfil.getSupermercados().size() <= 1) {
            PlanMensual plan = generarEn(perfil, todasLasRecetas);
            if (perfil.getSupermercados().size() == 1) {
                plan.setSupermercado(perfil.getSupermercados().get(0));
            }
            return plan;
        }
        Supermercado elegido = supermercadoMasBarato(perfil, todasLasRecetas);
        if (elegido == null) {
            // Ningun super tiene por si solo recetas para todo: se reparte la compra
            return generarEn(perfil, todasLasRecetas);
        }
        PlanMensual plan = generarEn(perfil.soloEn(elegido), todasLasRecetas);
        plan.setSupermercado(elegido);
        return plan;
    }

    /**
     * De los supermercados del usuario, en el que sale mas barata la compra del mes.
     *
     * Se hace un plan de referencia (lo mas barato posible repartiendo la compra) y se
     * calcula cuanto costaria esa misma compra en cada supermercado. Asi la eleccion coincide
     * con la comparacion que ve el usuario en la lista de la compra.
     * Si ningun supermercado vende todos los productos de ese plan, se elige el de menor
     * gasto minimo por si solo. Devuelve null si ninguno sirve.
     */
    public static Supermercado supermercadoMasBarato(PerfilUsuario perfil, List<Receta> todasLasRecetas) {
        PerfilUsuario repartido = perfil.soloEn(perfil.getSupermercados().get(0));
        repartido.setSupermercados(perfil.getSupermercados());
        repartido.setUnSoloSupermercado(false);
        repartido.setPresupuesto(Double.MAX_VALUE / 4); // solo interesa la compra, no el limite
        Map<Momento, List<Receta>> candidatas = candidatas(repartido, todasLasRecetas);
        boolean hayRecetas = !repartido.getMomentos().isEmpty();
        for (List<Receta> lista : candidatas.values()) {
            hayRecetas &= !lista.isEmpty();
        }
        if (hayRecetas) {
            PlanMensual referencia = construir(repartido, candidatas, Double.MAX_VALUE, 0, true);
            Map<Supermercado, Double> costes = compararSupermercados(perfil, referencia.getDias());
            if (!costes.isEmpty()) {
                return costes.keySet().iterator().next();
            }
        }
        Supermercado mejor = null;
        double mejorCoste = Double.MAX_VALUE;
        for (Supermercado s : perfil.getSupermercados()) {
            double coste = costeMinimo(perfil.soloEn(s), todasLasRecetas);
            if (coste >= 0 && coste < mejorCoste - 1e-9) {
                mejorCoste = coste;
                mejor = s;
            }
        }
        return mejor;
    }

    /**
     * Cuanto costaria la compra de ESTE plan en cada uno de los supermercados del usuario
     * (solo los que venden todos los productos que hacen falta). Ordenado de mas barato a mas caro.
     */
    public static Map<Supermercado, Double> compararSupermercados(PerfilUsuario perfil, List<DiaPlan> dias) {
        List<Map.Entry<Supermercado, Double>> costes = new ArrayList<Map.Entry<Supermercado, Double>>();
        for (Supermercado s : perfil.getSupermercados()) {
            PerfilUsuario soloS = perfil.soloEn(s);
            CestaCompra cesta = new CestaCompra(soloS);
            boolean vendeTodo = true;
            for (DiaPlan dia : dias) {
                for (Momento m : Momento.values()) {
                    Receta r = dia.getReceta(m);
                    if (r == null) {
                        continue;
                    }
                    for (Ingrediente ing : r.getIngredientes()) {
                        vendeTodo &= ing.getProducto().seVendeEn(s);
                    }
                    cesta.anadir(r);
                }
            }
            if (vendeTodo) {
                costes.add(new AbstractMap.SimpleEntry<Supermercado, Double>(s, cesta.getCosteTotal()));
            }
        }
        Collections.sort(costes, new Comparator<Map.Entry<Supermercado, Double>>() {
            @Override
            public int compare(Map.Entry<Supermercado, Double> a, Map.Entry<Supermercado, Double> b) {
                return Double.compare(a.getValue(), b.getValue());
            }
        });
        Map<Supermercado, Double> resultado = new LinkedHashMap<Supermercado, Double>();
        for (Map.Entry<Supermercado, Double> e : costes) {
            resultado.put(e.getKey(), e.getValue());
        }
        return resultado;
    }

    /** Genera el plan comprando en los supermercados del perfil (cada producto en el mas barato). */
    private static PlanMensual generarEn(PerfilUsuario perfil, List<Receta> todasLasRecetas) {
        if (perfil.getMomentos().isEmpty()) {
            return planVacio(perfil, "Elige al menos comida o cena para poder hacer el plan.");
        }
        Map<Momento, List<Receta>> candidatas = candidatas(perfil, todasLasRecetas);
        for (Momento m : perfil.getMomentos()) {
            if (candidatas.get(m).isEmpty()) {
                return planVacio(perfil, "No hay recetas de " + m.getNombreVisible().toLowerCase(ES)
                        + " compatibles con tus restricciones y supermercados. Prueba a elegir más supermercados.");
            }
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
            parcial.setAviso(avisoPresupuesto(perfil.getPresupuesto(), costeMinimo));
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
     * Gasto aproximado del mes eligiendo siempre lo mas barato (manteniendo algo de variedad).
     * Devuelve -1 si no hay recetas para alguno de los momentos elegidos.
     */
    public static double costeMinimo(PerfilUsuario perfil, List<Receta> todasLasRecetas) {
        if (perfil.getMomentos().isEmpty()) {
            return -1;
        }
        Map<Momento, List<Receta>> candidatas = candidatas(perfil, todasLasRecetas);
        for (List<Receta> lista : candidatas.values()) {
            if (lista.isEmpty()) {
                return -1;
            }
        }
        return construir(perfil, candidatas, Double.MAX_VALUE, 0, true).getCosteTotal();
    }

    /**
     * Rehace un plan a partir de los dias ya decididos (por ejemplo, un plan guardado
     * en el movil o uno en el que el usuario ha cambiado un plato): recalcula la lista
     * de la compra, el coste y si se pasa del presupuesto.
     */
    public static PlanMensual desdeDias(PerfilUsuario perfil, List<DiaPlan> dias, List<Receta> todasLasRecetas) {
        CestaCompra cesta = new CestaCompra(perfil);
        PlanMensual plan = new PlanMensual();
        plan.setPresupuesto(perfil.getPresupuesto());
        for (DiaPlan dia : dias) {
            for (Momento m : Momento.values()) {
                if (dia.getReceta(m) != null) {
                    cesta.anadir(dia.getReceta(m));
                }
            }
            plan.getDias().add(dia);
        }
        terminar(plan, cesta);

        boolean completo = dias.size() >= perfil.getDias()
                && plan.getCosteTotal() <= perfil.getPresupuesto() + 1e-9;
        plan.setCompleto(completo);
        if (!completo) {
            double minimo = costeMinimo(perfil, todasLasRecetas);
            plan.setPresupuestoMinimo(minimo);
            plan.setAviso(avisoPresupuesto(perfil.getPresupuesto(), minimo));
        }
        return plan;
    }

    /**
     * Recetas que pueden sustituir a la del dia y momento indicados sin que el plan
     * se pase del presupuesto, ordenadas de mas barata a mas cara.
     */
    public static List<Alternativa> alternativas(PerfilUsuario perfil, PlanMensual plan, int indiceDia,
                                                 Momento momento, List<Receta> todasLasRecetas) {
        DiaPlan dia = plan.getDias().get(indiceDia);
        Receta actual = dia.getReceta(momento);
        Receta otraDelDia = dia.getReceta(momento == Momento.COMIDA ? Momento.CENA : Momento.COMIDA);
        // Si el plan ya se pasaba, al menos que el cambio no lo empeore
        double limite = Math.max(perfil.getPresupuesto(), plan.getCosteTotal());

        List<Alternativa> resultado = new ArrayList<Alternativa>();
        for (Receta r : candidatas(perfil, todasLasRecetas).get(momento)) {
            if (r == actual || r == otraDelDia) {
                continue;
            }
            CestaCompra cesta = new CestaCompra(perfil);
            for (int i = 0; i < plan.getDias().size(); i++) {
                for (Momento m : Momento.values()) {
                    Receta receta = (i == indiceDia && m == momento) ? r : plan.getDias().get(i).getReceta(m);
                    if (receta != null) {
                        cesta.anadir(receta);
                    }
                }
            }
            if (cesta.getCosteTotal() <= limite + 1e-9) {
                resultado.add(new Alternativa(r, cesta.getCosteTotal() - plan.getCosteTotal()));
            }
        }
        Collections.sort(resultado, new Comparator<Alternativa>() {
            @Override
            public int compare(Alternativa a, Alternativa b) {
                return Double.compare(a.getDiferencia(), b.getDiferencia());
            }
        });
        return resultado;
    }

    /** Una receta que puede ir en un hueco del plan y cuanto cambia el gasto del mes. */
    public static class Alternativa {
        private final Receta receta;
        private final double diferencia;

        public Alternativa(Receta receta, double diferencia) {
            this.receta = receta;
            this.diferencia = diferencia;
        }

        public Receta getReceta() { return receta; }
        /** Euros que sube (positivo) o baja (negativo) el gasto del mes con este cambio. */
        public double getDiferencia() { return diferencia; }
    }

    /** Recetas validas para cada momento que planifica el usuario. */
    private static Map<Momento, List<Receta>> candidatas(PerfilUsuario perfil, List<Receta> todasLasRecetas) {
        List<Receta> validas = FiltroRecetas.recetasValidas(todasLasRecetas, perfil);
        Map<Momento, List<Receta>> candidatas = new HashMap<Momento, List<Receta>>();
        for (Momento m : perfil.getMomentos()) {
            List<Receta> lista = new ArrayList<Receta>();
            for (Receta r : validas) {
                if (r.sirvePara(m)) {
                    lista.add(r);
                }
            }
            candidatas.put(m, lista);
        }
        return candidatas;
    }

    private static PlanMensual planVacio(PerfilUsuario perfil, String aviso) {
        PlanMensual vacio = new PlanMensual();
        vacio.setPresupuesto(perfil.getPresupuesto());
        vacio.setCompleto(false);
        vacio.setAviso(aviso);
        return vacio;
    }

    private static String avisoPresupuesto(double presupuesto, double minimo) {
        if (minimo < 0) {
            return "No hay recetas compatibles con tus restricciones y supermercados.";
        }
        return String.format(ES, "Con %.2f € no llega para todo el mes. Necesitarías unos %.2f €.",
                presupuesto, minimo);
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
