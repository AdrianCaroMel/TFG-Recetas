package com.adriancaro.tfgrecetas.ui;

import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.adriancaro.tfgrecetas.R;
import com.adriancaro.tfgrecetas.datos.PerfilStorage;
import com.adriancaro.tfgrecetas.datos.RepositorioDatos;
import com.adriancaro.tfgrecetas.logica.PlanificadorMensual;
import com.adriancaro.tfgrecetas.modelo.LineaCompra;
import com.adriancaro.tfgrecetas.modelo.PerfilUsuario;
import com.adriancaro.tfgrecetas.modelo.PlanMensual;
import com.adriancaro.tfgrecetas.modelo.Supermercado;

/**
 * Lista de la compra del mes agrupada por supermercado.
 * Se vuelve a calcular el plan (misma semilla = mismo plan que en la pantalla anterior).
 */
public class ListaCompraActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_compra);

        TextView tvTotal = findViewById(R.id.tvTotalCompra);
        LinearLayout contenedor = findViewById(R.id.contenedorCompra);

        PerfilUsuario perfil = PerfilStorage.cargar(this);
        PlanMensual plan = PlanificadorMensual.generar(perfil, RepositorioDatos.get(this).getRecetas());

        String total = "Total: " + Formato.euros(plan.getCosteTotal());
        if (perfil.isTengoBasicos()) {
            total += "\n(sin aceite, sal ni pimentón: ya los tienes)";
        }
        tvTotal.setText(total);

        // La lista viene ordenada por supermercado: se pone una cabecera cada vez que cambia
        Supermercado actual = null;
        for (LineaCompra linea : plan.getListaCompra()) {
            if (linea.getSupermercado() != actual) {
                actual = linea.getSupermercado();
                contenedor.addView(crearCabecera(actual.getNombreVisible() + " · "
                        + Formato.euros(subtotal(plan, actual))));
            }
            String texto = linea.getEnvases() + " x " + linea.getProducto().getNombre()
                    + " (" + Formato.cantidad(linea.getProducto().getEnvase(), linea.getProducto().getUnidad()) + ")"
                    + "  →  " + Formato.euros(linea.getPrecioTotal());
            TextView tv = new TextView(this);
            tv.setText(texto);
            tv.setTextSize(16);
            tv.setPadding(0, 6, 0, 6);
            contenedor.addView(tv);
        }
    }

    private TextView crearCabecera(String texto) {
        TextView tv = new TextView(this);
        tv.setText(texto);
        tv.setTextSize(18);
        tv.setTypeface(null, Typeface.BOLD);
        tv.setTextColor(ContextCompat.getColor(this, R.color.verde));
        tv.setPadding(0, 32, 0, 8);
        return tv;
    }

    private double subtotal(PlanMensual plan, Supermercado s) {
        double suma = 0;
        for (LineaCompra linea : plan.getListaCompra()) {
            if (linea.getSupermercado() == s) {
                suma += linea.getPrecioTotal();
            }
        }
        return suma;
    }
}
