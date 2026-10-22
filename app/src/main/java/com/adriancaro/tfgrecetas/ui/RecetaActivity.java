package com.adriancaro.tfgrecetas.ui;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.OvershootInterpolator;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.adriancaro.tfgrecetas.R;
import com.adriancaro.tfgrecetas.datos.FavoritosStorage;
import com.adriancaro.tfgrecetas.datos.PlanStorage;
import com.adriancaro.tfgrecetas.datos.RepositorioDatos;
import com.adriancaro.tfgrecetas.logica.CestaCompra;
import com.adriancaro.tfgrecetas.logica.Nutricion;
import com.adriancaro.tfgrecetas.modelo.Ingrediente;
import com.adriancaro.tfgrecetas.modelo.PerfilUsuario;
import com.adriancaro.tfgrecetas.modelo.Producto;
import com.adriancaro.tfgrecetas.modelo.Receta;
import com.adriancaro.tfgrecetas.modelo.Supermercado;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.Snackbar;

/**
 * Detalle de una receta con las cantidades ya multiplicadas por las personas.
 */
public class RecetaActivity extends BaseActivity {

    public static final String EXTRA_ID_RECETA = "id_receta";

    private int idReceta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receta);
        prepararPantalla(true);

        int id = getIntent().getIntExtra(EXTRA_ID_RECETA, -1);
        Receta receta = RepositorioDatos.get(this).buscarReceta(id);
        if (receta == null) {
            finish();
            return;
        }
        PerfilUsuario perfil = PlanStorage.perfilDelPlan(this);
        int personas = perfil.getPersonas();

        idReceta = receta.getId();
        ((TextView) findViewById(R.id.tvNombreReceta)).setText(receta.getNombre());
        TextView tvEmoji = findViewById(R.id.tvEmojiGrande);
        tvEmoji.setText(Emoji.de(receta));
        // El emoji entra girando un poco y con rebote
        tvEmoji.setScaleX(0.3f);
        tvEmoji.setScaleY(0.3f);
        tvEmoji.setRotation(-25f);
        tvEmoji.animate().scaleX(1f).scaleY(1f).rotation(0f).setStartDelay(150).setDuration(650)
                .setInterpolator(new OvershootInterpolator(2.5f)).start();

        // Datos rapidos: tiempo, personas y precio por racion
        ChipGroup grupoInfo = findViewById(R.id.grupoInfo);
        anadirInfo(grupoInfo, "⏱ " + getString(R.string.minutos, receta.getMinutos()));
        anadirInfo(grupoInfo, "👥 " + Formato.personas(this, personas));
        anadirInfo(grupoInfo, "💶 " + getString(R.string.por_racion,
                Formato.euros(new CestaCompra(perfil).costeRacion(receta))));
        anadirInfo(grupoInfo, "📋 " + getResources().getQuantityString(R.plurals.pasos,
                receta.getPasos().size(), receta.getPasos().size()));

        // Nutricion de una racion (las cantidades de la receta son por persona)
        Nutricion nutricion = Nutricion.deRacion(receta);
        ((TextView) findViewById(R.id.tvKcal)).setText(getString(R.string.kcal, Math.round(nutricion.getKcal())));
        ((TextView) findViewById(R.id.tvMacros)).setText(getString(R.string.macros,
                Math.round(nutricion.getProteinas()), Math.round(nutricion.getHidratos()),
                Math.round(nutricion.getGrasas())));
        ((TextView) findViewById(R.id.tvRacion)).setText(getString(R.string.racion_detalle,
                Math.round(nutricion.getPesoGramos()), nutricion.porcentajeDiario()));

        // Ingredientes: cantidad por persona x numero de personas
        // Debajo de cada uno, el supermercado (de los del plan) donde sale más barato.
        // Al tocar la fila se marca, para ir viendo lo que ya está preparado.
        CestaCompra cesta = new CestaCompra(perfil);
        LinearLayout contenedorIngredientes = findViewById(R.id.contenedorIngredientes);
        for (Ingrediente ing : receta.getIngredientes()) {
            Producto producto = ing.getProducto();
            View fila = getLayoutInflater().inflate(R.layout.item_ingrediente, contenedorIngredientes, false);
            TextView tvNombre = fila.findViewById(R.id.tvIngrediente);
            tvNombre.setText(producto.getNombre());
            ((TextView) fila.findViewById(R.id.tvCantidad)).setText(
                    Formato.cantidad(ing.getCantidad() * personas, producto.getUnidad()));
            TextView tvDonde = fila.findViewById(R.id.tvDonde);
            Supermercado donde = cesta.mejorSupermercado(producto);
            if (perfil.isTengoBasicos() && producto.esBasico()) {
                tvDonde.setText(R.string.ya_en_casa);
            } else if (donde != null) {
                tvDonde.setText(getString(R.string.donde_comprar, donde.getNombreVisible(),
                        Formato.euros(producto.precioEn(donde))));
            } else {
                tvDonde.setVisibility(View.GONE);
            }
            MaterialCheckBox cb = fila.findViewById(R.id.cbIngrediente);
            fila.setOnClickListener(v -> {
                boolean marcado = !cb.isChecked();
                cb.setChecked(marcado);
                tvNombre.setPaintFlags(marcado
                        ? tvNombre.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG
                        : tvNombre.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
                fila.animate().alpha(marcado ? 0.5f : 1f).setDuration(200).start();
            });
            contenedorIngredientes.addView(fila);
        }

        findViewById(R.id.btnCocina).setOnClickListener(v -> {
            Intent intent = new Intent(this, CocinaActivity.class);
            intent.putExtra(EXTRA_ID_RECETA, idReceta);
            startActivity(intent);
        });

        // Pasos numerados
        LinearLayout contenedorPasos = findViewById(R.id.contenedorPasos);
        for (int i = 0; i < receta.getPasos().size(); i++) {
            View fila = getLayoutInflater().inflate(R.layout.item_paso, contenedorPasos, false);
            ((TextView) fila.findViewById(R.id.tvNumero)).setText(String.valueOf(i + 1));
            ((TextView) fila.findViewById(R.id.tvTextoPaso)).setText(receta.getPasos().get(i));
            contenedorPasos.addView(fila);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_receta, menu);
        pintarFavorita(menu.findItem(R.id.accionFavorita));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.accionFavorita) {
            boolean favorita = FavoritosStorage.alternar(this, idReceta);
            pintarFavorita(item);
            Snackbar.make(findViewById(R.id.raiz),
                    favorita ? R.string.favorita_anadida : R.string.favorita_quitada, Snackbar.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /** Corazon relleno o vacio, en blanco para que se vea sobre la barra verde. */
    private void pintarFavorita(MenuItem item) {
        boolean favorita = FavoritosStorage.esFavorita(this, idReceta);
        item.setIcon(favorita ? R.drawable.ic_corazon : R.drawable.ic_corazon_borde);
        item.setIconTintList(ColorStateList.valueOf(Color.WHITE));
    }

    private void anadirInfo(ChipGroup grupo, String texto) {
        Chip chip = new Chip(this);
        chip.setText(texto);
        chip.setClickable(false);
        chip.setCheckable(false);
        grupo.addView(chip);
    }
}
