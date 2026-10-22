package com.adriancaro.tfgrecetas.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.adriancaro.tfgrecetas.R;
import com.adriancaro.tfgrecetas.datos.FavoritosStorage;
import com.adriancaro.tfgrecetas.datos.PerfilStorage;
import com.adriancaro.tfgrecetas.datos.RepositorioDatos;
import com.adriancaro.tfgrecetas.logica.CestaCompra;
import com.adriancaro.tfgrecetas.logica.FiltroRecetas;
import com.adriancaro.tfgrecetas.logica.Nutricion;
import com.adriancaro.tfgrecetas.modelo.Ingrediente;
import com.adriancaro.tfgrecetas.modelo.PerfilUsuario;
import com.adriancaro.tfgrecetas.modelo.Receta;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Recetario: todas las recetas que el usuario puede hacer (segun sus restricciones
 * y supermercados), con buscador, filtros rapidos y favoritas.
 */
public class RecetarioActivity extends BaseActivity {

    private final List<Receta> aptas = new ArrayList<Receta>();
    private final List<Receta> visibles = new ArrayList<Receta>();
    private CestaCompra precios;
    private RecyclerView rvRecetas;
    private TextView tvContador;
    private TextView tvVacio;
    private ChipGroup grupoFiltros;
    private TextInputEditText etBuscar;
    private Adaptador adaptador;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recetario);
        prepararPantalla(true);

        PerfilUsuario perfil = PerfilStorage.cargar(this);
        precios = new CestaCompra(perfil);
        aptas.addAll(FiltroRecetas.recetasValidas(RepositorioDatos.get(this).getRecetas(), perfil));

        rvRecetas = findViewById(R.id.rvRecetas);
        tvContador = findViewById(R.id.tvContador);
        tvVacio = findViewById(R.id.tvVacio);
        grupoFiltros = findViewById(R.id.grupoFiltros);
        etBuscar = findViewById(R.id.etBuscar);

        adaptador = new Adaptador();
        rvRecetas.setLayoutManager(new LinearLayoutManager(this));
        rvRecetas.setAdapter(adaptador);

        grupoFiltros.setOnCheckedStateChangeListener((grupo, marcados) -> filtrar(true));
        etBuscar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                filtrar(false);
            }
        });
        filtrar(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Al volver de una receta puede haber cambiado su estado de favorita
        if (adaptador != null) {
            filtrar(false);
        }
    }

    /** Aplica buscador + filtro elegido. Con animar=true la lista vuelve a entrar en cascada. */
    private void filtrar(boolean animar) {
        String texto = String.valueOf(etBuscar.getText()).trim().toLowerCase(Locale.ROOT);
        int filtro = grupoFiltros.getCheckedChipId();
        visibles.clear();
        for (Receta r : aptas) {
            if (!texto.isEmpty() && !coincide(r, texto)) {
                continue;
            }
            if (filtro == R.id.chipRapidas && r.getMinutos() > 20) {
                continue;
            }
            if (filtro == R.id.chipLigeras && Nutricion.deRacion(r).getKcal() > 400) {
                continue;
            }
            if (filtro == R.id.chipFavoritas && !FavoritosStorage.esFavorita(this, r.getId())) {
                continue;
            }
            visibles.add(r);
        }
        adaptador.notifyDataSetChanged();
        if (animar) {
            rvRecetas.scheduleLayoutAnimation();
        }
        tvContador.setText(getString(R.string.recetas_aptas, visibles.size()));
        tvVacio.setVisibility(visibles.isEmpty() ? View.VISIBLE : View.GONE);
    }

    /** Busca en el nombre y en los ingredientes. */
    private boolean coincide(Receta r, String texto) {
        if (r.getNombre().toLowerCase(Locale.ROOT).contains(texto)) {
            return true;
        }
        for (Ingrediente ing : r.getIngredientes()) {
            if (ing.getProducto().getNombre().toLowerCase(Locale.ROOT).contains(texto)) {
                return true;
            }
        }
        return false;
    }

    private String momento(Receta r) {
        if (r.getTipo().equals("AMBAS")) {
            return getString(R.string.comida) + " y " + getString(R.string.cena).toLowerCase(Locale.ROOT);
        }
        return r.getTipo().equals("COMIDA") ? getString(R.string.comida) : getString(R.string.cena);
    }

    private class Adaptador extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View vista = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_receta, parent, false);
            return new RecyclerView.ViewHolder(vista) { };
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            Receta r = visibles.get(position);
            View v = holder.itemView;
            ((TextView) v.findViewById(R.id.tvEmoji)).setText(Emoji.de(r));
            ((TextView) v.findViewById(R.id.tvMomentoReceta)).setText(momento(r));
            ((TextView) v.findViewById(R.id.tvNombre)).setText(r.getNombre());
            ((TextView) v.findViewById(R.id.tvDatos)).setText("⏱ " + getString(R.string.minutos, r.getMinutos())
                    + "  ·  🔥 " + getString(R.string.kcal, (int) Math.round(Nutricion.deRacion(r).getKcal()))
                    + "  ·  " + getString(R.string.por_racion, Formato.euros(precios.costeRacion(r))));

            ImageButton btnFavorita = v.findViewById(R.id.btnFavorita);
            pintarCorazon(btnFavorita, FavoritosStorage.esFavorita(RecetarioActivity.this, r.getId()));
            btnFavorita.setOnClickListener(boton -> {
                boolean favorita = FavoritosStorage.alternar(RecetarioActivity.this, r.getId());
                pintarCorazon(btnFavorita, favorita);
                // Pequeno "latido" del corazon
                boton.setScaleX(0.6f);
                boton.setScaleY(0.6f);
                boton.animate().scaleX(1f).scaleY(1f).setDuration(350)
                        .setInterpolator(new OvershootInterpolator(4f)).start();
                Snackbar.make(rvRecetas, favorita ? R.string.favorita_anadida : R.string.favorita_quitada,
                        Snackbar.LENGTH_SHORT).show();
            });

            v.setOnClickListener(tarjeta -> {
                Intent intent = new Intent(RecetarioActivity.this, RecetaActivity.class);
                intent.putExtra(RecetaActivity.EXTRA_ID_RECETA, r.getId());
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return visibles.size();
        }

        private void pintarCorazon(ImageButton boton, boolean favorita) {
            boton.setImageResource(favorita ? R.drawable.ic_corazon : R.drawable.ic_corazon_borde);
        }
    }
}
