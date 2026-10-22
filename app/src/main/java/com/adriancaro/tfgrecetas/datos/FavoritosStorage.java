package com.adriancaro.tfgrecetas.datos;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

/**
 * Recetas que el usuario ha marcado como favoritas (se guardan sus ids).
 */
public class FavoritosStorage {

    private static final String FICHERO = "favoritos";
    private static final String CLAVE = "ids";

    public static boolean esFavorita(Context context, int idReceta) {
        return ids(context).contains(String.valueOf(idReceta));
    }

    /** Cambia el estado de favorita y devuelve el nuevo estado. */
    public static boolean alternar(Context context, int idReceta) {
        Set<String> ids = new HashSet<String>(ids(context));
        boolean ahora = !ids.remove(String.valueOf(idReceta));
        if (ahora) {
            ids.add(String.valueOf(idReceta));
        }
        prefs(context).edit().putStringSet(CLAVE, ids).apply();
        return ahora;
    }

    private static Set<String> ids(Context context) {
        return prefs(context).getStringSet(CLAVE, new HashSet<String>());
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(FICHERO, Context.MODE_PRIVATE);
    }
}
