package com.adriancaro.tfgrecetas.datos;

import android.content.Context;
import android.content.SharedPreferences;

import com.adriancaro.tfgrecetas.modelo.PerfilUsuario;
import com.adriancaro.tfgrecetas.modelo.Restriccion;
import com.adriancaro.tfgrecetas.modelo.Supermercado;

import java.util.ArrayList;
import java.util.List;

/**
 * Guarda y recupera el perfil del usuario con SharedPreferences.
 * Las listas (supermercados, restricciones) se guardan como texto separado por comas.
 */
public class PerfilStorage {

    private static final String FICHERO = "perfil_usuario";

    public static void guardar(Context context, PerfilUsuario perfil) {
        SharedPreferences.Editor editor = context.getSharedPreferences(FICHERO, Context.MODE_PRIVATE).edit();

        StringBuilder supers = new StringBuilder();
        for (Supermercado s : perfil.getSupermercados()) {
            supers.append(s.name()).append(",");
        }
        StringBuilder restricciones = new StringBuilder();
        for (Restriccion r : perfil.getRestricciones()) {
            restricciones.append(r.name()).append(",");
        }

        editor.putString("supermercados", supers.toString());
        editor.putString("restricciones", restricciones.toString());
        editor.putFloat("presupuesto", (float) perfil.getPresupuesto());
        editor.putInt("personas", perfil.getPersonas());
        editor.putBoolean("comida", perfil.isPlanificarComida());
        editor.putBoolean("cena", perfil.isPlanificarCena());
        editor.putBoolean("basicos", perfil.isTengoBasicos());
        editor.putLong("semilla", perfil.getSemilla());
        editor.apply();
    }

    public static PerfilUsuario cargar(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(FICHERO, Context.MODE_PRIVATE);
        PerfilUsuario perfil = new PerfilUsuario();

        List<Supermercado> supers = new ArrayList<Supermercado>();
        for (String nombre : prefs.getString("supermercados", "").split(",")) {
            if (!nombre.isEmpty()) {
                supers.add(Supermercado.valueOf(nombre));
            }
        }
        List<Restriccion> restricciones = new ArrayList<Restriccion>();
        for (String nombre : prefs.getString("restricciones", "").split(",")) {
            if (!nombre.isEmpty()) {
                restricciones.add(Restriccion.valueOf(nombre));
            }
        }

        perfil.setSupermercados(supers);
        perfil.setRestricciones(restricciones);
        perfil.setPresupuesto(prefs.getFloat("presupuesto", 0));
        perfil.setPersonas(prefs.getInt("personas", 1));
        perfil.setPlanificarComida(prefs.getBoolean("comida", true));
        perfil.setPlanificarCena(prefs.getBoolean("cena", true));
        perfil.setTengoBasicos(prefs.getBoolean("basicos", false));
        perfil.setSemilla(prefs.getLong("semilla", 1));
        return perfil;
    }

    /** True cuando el usuario ya ha terminado los tres pasos al menos una vez. */
    public static boolean estaCompleto(Context context) {
        return context.getSharedPreferences(FICHERO, Context.MODE_PRIVATE).getBoolean("completo", false);
    }

    public static void marcarCompleto(Context context) {
        context.getSharedPreferences(FICHERO, Context.MODE_PRIVATE).edit().putBoolean("completo", true).apply();
    }
}
