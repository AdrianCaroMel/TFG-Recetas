package com.adriancaro.tfgrecetas.logica;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Busca tiempos dentro del texto de un paso ("cuece 10-12 minutos", "30 segundos")
 * para ofrecer un temporizador en el modo cocina.
 */
public final class Temporizadores {

    private static final Pattern TIEMPO = Pattern.compile(
            "(\\d+)\\s*(?:(?:-|–|a)\\s*(\\d+)\\s*)?(segundos?|seg\\b|minutos?|min\\b|horas?)",
            Pattern.CASE_INSENSITIVE);

    private Temporizadores() {
    }

    /**
     * Segundos del tiempo mas largo que aparece en el paso, o 0 si no hay ninguno.
     * En un rango ("6-8 minutos") se usa el minimo: es mejor mirar antes que pasarse.
     */
    public static int segundosDe(String paso) {
        if (paso == null) {
            return 0;
        }
        int mayor = 0;
        Matcher m = TIEMPO.matcher(paso);
        while (m.find()) {
            int valor = Integer.parseInt(m.group(1));
            String unidad = m.group(3).toLowerCase();
            int segundos;
            if (unidad.startsWith("seg")) {
                segundos = valor;
            } else if (unidad.startsWith("hora")) {
                segundos = valor * 3600;
            } else {
                segundos = valor * 60;
            }
            mayor = Math.max(mayor, segundos);
        }
        return mayor;
    }

    /** "1:05" o "12:00" para mostrar la cuenta atras. */
    public static String formato(long segundos) {
        return String.format(java.util.Locale.ROOT, "%d:%02d", segundos / 60, segundos % 60);
    }
}
