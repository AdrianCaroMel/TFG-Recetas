package com.adriancaro.tfgrecetas.datos;

import android.content.Context;
import android.content.SharedPreferences;

import com.adriancaro.tfgrecetas.logica.PlanificadorMensual;
import com.adriancaro.tfgrecetas.modelo.DiaPlan;
import com.adriancaro.tfgrecetas.modelo.Momento;
import com.adriancaro.tfgrecetas.modelo.PerfilUsuario;
import com.adriancaro.tfgrecetas.modelo.PlanMensual;
import com.adriancaro.tfgrecetas.modelo.Receta;
import com.adriancaro.tfgrecetas.modelo.Supermercado;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Guarda el plan actual en el movil para que no cambie al cerrar la app
 * y para poder cambiar platos sueltos.
 *
 * Solo se guardan los ids de las recetas de cada dia ("idComida,idCena;idComida,idCena;..."
 * con 0 si ese momento esta vacio). El coste y la lista de la compra se recalculan al cargar.
 * Tambien guarda que productos de la lista de la compra se han marcado como comprados.
 */
public class PlanStorage {

    private static final String FICHERO = "plan_actual";
    private static final String CLAVE_DIAS = "dias";
    private static final String CLAVE_COMPRADOS = "comprados";
    private static final String CLAVE_SUPER = "supermercado";

    /** Devuelve el plan guardado o, si no hay ninguno, genera uno nuevo y lo guarda. */
    public static PlanMensual obtener(Context context) {
        PerfilUsuario perfil = PerfilStorage.cargar(context);
        List<Receta> recetas = RepositorioDatos.get(context).getRecetas();

        List<DiaPlan> dias = leerDias(context);
        if (dias != null && !dias.isEmpty()) {
            Supermercado s = supermercadoGuardado(context, perfil);
            PlanMensual plan = PlanificadorMensual.desdeDias(s == null ? perfil : perfil.soloEn(s), dias, recetas);
            plan.setSupermercado(s);
            return plan;
        }
        PlanMensual plan = PlanificadorMensual.generar(perfil, recetas);
        guardar(context, plan);
        return plan;
    }

    public static void guardar(Context context, PlanMensual plan) {
        StringBuilder texto = new StringBuilder();
        for (DiaPlan dia : plan.getDias()) {
            texto.append(id(dia.getComida())).append(',').append(id(dia.getCena())).append(';');
        }
        prefs(context).edit()
                .putString(CLAVE_DIAS, texto.toString())
                .putString(CLAVE_SUPER, plan.getSupermercado() == null ? "" : plan.getSupermercado().name())
                .apply();
    }

    /**
     * Perfil con el que se ha hecho el plan guardado: si toda la compra va a un solo
     * supermercado, el perfil queda limitado a ese super (para cambiar platos, precios...).
     */
    public static PerfilUsuario perfilDelPlan(Context context) {
        PerfilUsuario perfil = PerfilStorage.cargar(context);
        Supermercado s = supermercadoGuardado(context, perfil);
        return s == null ? perfil : perfil.soloEn(s);
    }

    private static Supermercado supermercadoGuardado(Context context, PerfilUsuario perfil) {
        String nombre = prefs(context).getString(CLAVE_SUPER, "");
        for (Supermercado s : perfil.getSupermercados()) {
            if (s.name().equals(nombre)) {
                return s;
            }
        }
        return null;
    }

    /** Olvida el plan (y lo marcado como comprado). Se llama al cambiar los datos o pedir otro plan. */
    public static void borrar(Context context) {
        prefs(context).edit().clear().apply();
    }

    public static Set<Integer> comprados(Context context) {
        Set<Integer> ids = new HashSet<Integer>();
        for (String id : prefs(context).getStringSet(CLAVE_COMPRADOS, new HashSet<String>())) {
            ids.add(Integer.parseInt(id));
        }
        return ids;
    }

    public static void marcarComprado(Context context, int idProducto, boolean comprado) {
        // Se copia el conjunto: SharedPreferences no permite modificar el que devuelve
        Set<String> ids = new HashSet<String>(prefs(context).getStringSet(CLAVE_COMPRADOS, new HashSet<String>()));
        if (comprado) {
            ids.add(String.valueOf(idProducto));
        } else {
            ids.remove(String.valueOf(idProducto));
        }
        prefs(context).edit().putStringSet(CLAVE_COMPRADOS, ids).apply();
    }

    /** Lee los dias guardados. Devuelve null si no hay plan o si alguna receta ya no existe. */
    private static List<DiaPlan> leerDias(Context context) {
        String texto = prefs(context).getString(CLAVE_DIAS, "");
        if (texto.isEmpty()) {
            return null;
        }
        RepositorioDatos repo = RepositorioDatos.get(context);
        List<DiaPlan> dias = new ArrayList<DiaPlan>();
        try {
            for (String trozo : texto.split(";")) {
                if (trozo.isEmpty()) {
                    continue;
                }
                String[] ids = trozo.split(",");
                DiaPlan dia = new DiaPlan(dias.size() + 1);
                if (!ponerReceta(dia, Momento.COMIDA, Integer.parseInt(ids[0]), repo)
                        || !ponerReceta(dia, Momento.CENA, Integer.parseInt(ids[1]), repo)) {
                    return null;
                }
                dias.add(dia);
            }
        } catch (RuntimeException e) {
            return null; // datos guardados con otro formato: se genera un plan nuevo
        }
        return dias;
    }

    private static boolean ponerReceta(DiaPlan dia, Momento momento, int id, RepositorioDatos repo) {
        if (id == 0) {
            return true;
        }
        Receta receta = repo.buscarReceta(id);
        if (receta == null) {
            return false;
        }
        dia.setReceta(momento, receta);
        return true;
    }

    private static int id(Receta receta) {
        return receta == null ? 0 : receta.getId();
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(FICHERO, Context.MODE_PRIVATE);
    }
}
