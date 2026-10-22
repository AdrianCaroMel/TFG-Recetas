package com.adriancaro.tfgrecetas.ui;

import com.adriancaro.tfgrecetas.modelo.Receta;

import java.util.Locale;

/**
 * Elige un emoji para cada receta segun su nombre (solo decorativo).
 */
public class Emoji {

    private static final String[][] REGLAS = {
            {"hamburguesa", "🍔"}, {"burrito", "🌯"}, {"tortilla", "🍳"}, {"revuelto", "🍳"},
            {"ensalada", "🥗"}, {"crema", "🥣"}, {"sopa", "🍲"}, {"puré", "🥣"},
            {"gambas", "🍤"}, {"merluza", "🐟"}, {"atún", "🐟"}, {"pollo", "🍗"},
            {"lomo", "🥩"}, {"albóndigas", "🥩"}, {"berenjena", "🍆"}, {"espaguetis", "🍝"},
            {"macarrones", "🍝"}, {"pasta", "🍝"}, {"arroz", "🍚"}, {"lentejas", "🍲"},
            {"alubias", "🫘"}, {"garbanzos", "🫘"}, {"brócoli", "🥦"}, {"champiñones", "🍄"},
            {"tofu", "🥢"}, {"quinoa", "🥗"}, {"cuscús", "🥘"}, {"pisto", "🥘"},
    };

    public static String de(Receta receta) {
        String nombre = receta.getNombre().toLowerCase(Locale.ROOT);
        for (String[] regla : REGLAS) {
            if (nombre.contains(regla[0])) {
                return regla[1];
            }
        }
        return "🍽️";
    }
}
