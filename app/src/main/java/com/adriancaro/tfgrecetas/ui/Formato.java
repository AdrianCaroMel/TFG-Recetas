package com.adriancaro.tfgrecetas.ui;

import android.content.Context;

import com.adriancaro.tfgrecetas.R;
import com.adriancaro.tfgrecetas.modelo.Supermercado;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

/**
 * Pequenas utilidades para mostrar euros y cantidades en formato espanol.
 */
public class Formato {

    private static final Locale ES = Locale.forLanguageTag("es-ES");

    public static String euros(double cantidad) {
        return String.format(ES, "%.2f €", cantidad);
    }

    /** Diferencia con signo: "+0,85 €" o "−0,30 €". */
    public static String diferencia(double cantidad) {
        if (cantidad >= 0) {
            return "+" + euros(cantidad);
        }
        return "−" + euros(-cantidad);
    }

    /** 1500 g -> "1,5 kg", 250 ml -> "250 ml", 2 ud -> "2 ud." */
    public static String cantidad(double valor, String unidad) {
        DecimalFormat df = new DecimalFormat("#.##", DecimalFormatSymbols.getInstance(ES));
        if (unidad.equals("g") && valor >= 1000) {
            return df.format(valor / 1000) + " kg";
        }
        if (unidad.equals("ml") && valor >= 1000) {
            return df.format(valor / 1000) + " l";
        }
        if (unidad.equals("ud")) {
            return df.format(valor) + " ud.";
        }
        return df.format(valor) + " " + unidad;
    }

    /** "1 persona" / "3 personas" */
    public static String personas(Context context, int n) {
        return context.getResources().getQuantityString(R.plurals.personas, n, n);
    }

    /** "Mercadona, Lidl y Dia" */
    public static String lista(List<Supermercado> supermercados) {
        StringBuilder texto = new StringBuilder();
        for (int i = 0; i < supermercados.size(); i++) {
            if (i > 0) {
                texto.append(i == supermercados.size() - 1 ? " y " : ", ");
            }
            texto.append(supermercados.get(i).getNombreVisible());
        }
        return texto.toString();
    }
}
