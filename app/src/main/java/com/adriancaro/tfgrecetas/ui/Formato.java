package com.adriancaro.tfgrecetas.ui;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Pequenas utilidades para mostrar euros y cantidades en formato espanol.
 */
public class Formato {

    private static final Locale ES = new Locale("es", "ES");

    public static String euros(double cantidad) {
        return String.format(ES, "%.2f €", cantidad);
    }

    /** 1500 g -> "1,5 kg", 250 ml -> "250 ml", 2 ud -> "2 ud" */
    public static String cantidad(double valor, String unidad) {
        DecimalFormat df = new DecimalFormat("#.##", DecimalFormatSymbols.getInstance(ES));
        if (unidad.equals("g") && valor >= 1000) {
            return df.format(valor / 1000) + " kg";
        }
        if (unidad.equals("ml") && valor >= 1000) {
            return df.format(valor / 1000) + " l";
        }
        return df.format(valor) + " " + unidad;
    }
}
