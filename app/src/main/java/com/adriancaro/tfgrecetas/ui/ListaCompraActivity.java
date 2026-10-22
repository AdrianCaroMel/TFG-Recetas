package com.adriancaro.tfgrecetas.ui;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.adriancaro.tfgrecetas.R;
import com.adriancaro.tfgrecetas.datos.PerfilStorage;
import com.adriancaro.tfgrecetas.datos.PlanStorage;
import com.adriancaro.tfgrecetas.logica.PlanificadorMensual;
import com.adriancaro.tfgrecetas.modelo.LineaCompra;
import com.adriancaro.tfgrecetas.modelo.PerfilUsuario;
import com.adriancaro.tfgrecetas.modelo.PlanMensual;
import com.adriancaro.tfgrecetas.modelo.Supermercado;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.Map;
import java.util.Set;

/**
 * Lista de la compra del mes agrupada por supermercado.
 * Se pueden marcar los productos ya comprados (se guardan) y compartir la lista.
 */
public class ListaCompraActivity extends BaseActivity {

    private PlanMensual plan;
    private Set<Integer> comprados;
    private TextView tvComprados;
    private LinearProgressIndicator barraComprados;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_compra);
        prepararPantalla(true);

        plan = PlanStorage.obtener(this);
        PerfilUsuario perfil = PerfilStorage.cargar(this);
        comprados = PlanStorage.comprados(this);

        ((TextView) findViewById(R.id.tvTotalCompra)).setText(Formato.euros(plan.getCosteTotal()));
        findViewById(R.id.tvBasicos).setVisibility(perfil.isTengoBasicos() ? View.VISIBLE : View.GONE);
        tvComprados = findViewById(R.id.tvComprados);
        barraComprados = findViewById(R.id.barraComprados);
        barraComprados.setMax(Math.max(1, plan.getListaCompra().size()));

        LinearLayout contenedor = findViewById(R.id.contenedorCompra);
        mostrarComparacion(contenedor, perfil);

        // La lista viene ordenada por supermercado: una tarjeta cada vez que cambia
        LinearLayout productos = null;
        Supermercado actual = null;
        for (LineaCompra linea : plan.getListaCompra()) {
            if (linea.getSupermercado() != actual) {
                actual = linea.getSupermercado();
                View tarjeta = getLayoutInflater().inflate(R.layout.item_supermercado, contenedor, false);
                ((TextView) tarjeta.findViewById(R.id.tvSupermercado)).setText(actual.getNombreVisible());
                ((TextView) tarjeta.findViewById(R.id.tvSubtotal)).setText(Formato.euros(subtotal(actual)));
                // Antes del aviso de precios, que es la ultima vista
                contenedor.addView(tarjeta, contenedor.getChildCount() - 1);
                productos = tarjeta.findViewById(R.id.contenedorProductos);
            }
            productos.addView(crearFila(productos, linea));
        }
        actualizarContador();
    }

    /**
     * Si el usuario tiene varios supermercados, muestra cuanto costaria este mismo plan
     * en cada uno (el primero es el elegido, el mas barato).
     */
    private void mostrarComparacion(LinearLayout contenedor, PerfilUsuario perfil) {
        if (perfil.getSupermercados().size() < 2) {
            return;
        }
        Map<Supermercado, Double> costes = PlanificadorMensual.compararSupermercados(perfil, plan.getDias());
        View tarjeta = getLayoutInflater().inflate(R.layout.item_comparacion, contenedor, false);
        LinearLayout filas = tarjeta.findViewById(R.id.contenedorComparacion);
        double masBarato = costes.isEmpty() ? 0 : costes.values().iterator().next();
        for (Supermercado s : perfil.getSupermercados()) {
            if (!costes.containsKey(s)) {
                filas.addView(filaComparacion(filas, s.getNombreVisible(), getString(R.string.comparacion_no_vende), false));
            }
        }
        int i = 0;
        for (Map.Entry<Supermercado, Double> e : costes.entrySet()) {
            String precio = i == 0 ? Formato.euros(e.getValue())
                    : getString(R.string.comparacion_mas, Formato.euros(e.getValue() - masBarato));
            filas.addView(filaComparacion(filas, e.getKey().getNombreVisible(), precio, i == 0), i);
            i++;
        }
        contenedor.addView(tarjeta, 1);
    }

    private View filaComparacion(LinearLayout padre, String nombre, String valor, boolean ganador) {
        View fila = getLayoutInflater().inflate(R.layout.item_ingrediente, padre, false);
        TextView tvNombre = fila.findViewById(R.id.tvIngrediente);
        TextView tvValor = fila.findViewById(R.id.tvCantidad);
        tvNombre.setText(ganador ? "✓ " + nombre : nombre);
        tvValor.setText(valor);
        if (ganador) {
            tvNombre.setTypeface(null, android.graphics.Typeface.BOLD);
            tvValor.setTextColor(ContextCompat.getColor(this, R.color.verde));
        }
        return fila;
    }

    private View crearFila(LinearLayout padre, LineaCompra linea) {
        View fila = getLayoutInflater().inflate(R.layout.item_compra, padre, false);
        MaterialCheckBox cb = fila.findViewById(R.id.cbComprado);
        TextView tvProducto = fila.findViewById(R.id.tvProducto);
        TextView tvDetalle = fila.findViewById(R.id.tvDetalleProducto);
        TextView tvPrecio = fila.findViewById(R.id.tvPrecio);

        String unidad = linea.getProducto().getUnidad();
        tvProducto.setText(linea.getProducto().getNombre());
        tvDetalle.setText(linea.getEnvases() + " × " + Formato.cantidad(linea.getProducto().getEnvase(), unidad)
                + " · " + getString(R.string.necesitas, Formato.cantidad(linea.getCantidadNecesaria(), unidad)));
        tvPrecio.setText(Formato.euros(linea.getPrecioTotal()));

        final int id = linea.getProducto().getId();
        pintarComprado(fila, cb, tvProducto, comprados.contains(id));
        fila.setOnClickListener(v -> {
            boolean marcado = !comprados.contains(id);
            if (marcado) {
                comprados.add(id);
            } else {
                comprados.remove(id);
            }
            PlanStorage.marcarComprado(this, id, marcado);
            pintarComprado(fila, cb, tvProducto, marcado);
            actualizarContador();
        });
        return fila;
    }

    /** Lo comprado se ve tachado y mas apagado. */
    private void pintarComprado(View fila, MaterialCheckBox cb, TextView tvProducto, boolean comprado) {
        cb.setChecked(comprado);
        if (comprado) {
            tvProducto.setPaintFlags(tvProducto.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            tvProducto.setPaintFlags(tvProducto.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
        }
        fila.setAlpha(comprado ? 0.55f : 1f);
    }

    private void actualizarContador() {
        int hechos = 0;
        for (LineaCompra linea : plan.getListaCompra()) {
            if (comprados.contains(linea.getProducto().getId())) {
                hechos++;
            }
        }
        tvComprados.setText(getString(R.string.productos_comprados, hechos, plan.getListaCompra().size()));
        barraComprados.setProgress(hechos);
    }

    private double subtotal(Supermercado s) {
        double suma = 0;
        for (LineaCompra linea : plan.getListaCompra()) {
            if (linea.getSupermercado() == s) {
                suma += linea.getPrecioTotal();
            }
        }
        return suma;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_lista_compra, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.accionCompartir) {
            compartir();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /** Envia la lista como texto (WhatsApp, correo, notas...). Solo lo que falta por comprar. */
    private void compartir() {
        StringBuilder texto = new StringBuilder("🛒 ").append(getString(R.string.titulo_lista_compra))
                .append(" (").append(Formato.euros(plan.getCosteTotal())).append(")\n");
        Supermercado actual = null;
        for (LineaCompra linea : plan.getListaCompra()) {
            if (comprados.contains(linea.getProducto().getId())) {
                continue;
            }
            if (linea.getSupermercado() != actual) {
                actual = linea.getSupermercado();
                texto.append("\n").append(actual.getNombreVisible().toUpperCase(java.util.Locale.ROOT)).append("\n");
            }
            texto.append("• ").append(linea.getEnvases()).append(" × ").append(linea.getProducto().getNombre())
                    .append(" (").append(Formato.cantidad(linea.getProducto().getEnvase(), linea.getProducto().getUnidad()))
                    .append(") – ").append(Formato.euros(linea.getPrecioTotal())).append("\n");
        }

        Intent enviar = new Intent(Intent.ACTION_SEND);
        enviar.setType("text/plain");
        enviar.putExtra(Intent.EXTRA_TEXT, texto.toString().trim());
        startActivity(Intent.createChooser(enviar, getString(R.string.compartir_con)));
    }
}
