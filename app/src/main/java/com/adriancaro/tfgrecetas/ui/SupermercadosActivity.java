package com.adriancaro.tfgrecetas.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.adriancaro.tfgrecetas.R;
import com.adriancaro.tfgrecetas.datos.PerfilStorage;
import com.adriancaro.tfgrecetas.modelo.PerfilUsuario;
import com.adriancaro.tfgrecetas.modelo.Supermercado;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

/**
 * Paso 1: el usuario marca los supermercados en los que puede comprar.
 */
public class SupermercadosActivity extends BaseActivity {

    private final List<Chip> chips = new ArrayList<Chip>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opciones);
        prepararPantalla(true);
        mostrarPaso(1);

        TextView tvPregunta = findViewById(R.id.tvPregunta);
        TextView tvAyuda = findViewById(R.id.tvAyuda);
        LinearLayout contenedor = findViewById(R.id.contenedorOpciones);
        Button btnSiguiente = findViewById(R.id.btnSiguiente);

        tvPregunta.setText(R.string.pregunta_supermercados);
        tvAyuda.setText(R.string.ayuda_supermercados);

        // Un chip por supermercado, marcando los que ya eligio otra vez
        final PerfilUsuario perfil = PerfilStorage.cargar(this);
        ChipGroup grupo = anadirSeccion(contenedor, getString(R.string.titulo_supermercados));
        for (Supermercado s : Supermercado.values()) {
            chips.add(anadirChip(grupo, s.getNombreVisible(), perfil.getSupermercados().contains(s)));
        }

        btnSiguiente.setOnClickListener(v -> {
            List<Supermercado> elegidos = new ArrayList<Supermercado>();
            for (int i = 0; i < chips.size(); i++) {
                if (chips.get(i).isChecked()) {
                    elegidos.add(Supermercado.values()[i]);
                }
            }
            if (elegidos.isEmpty()) {
                Snackbar.make(v, R.string.error_supermercado, Snackbar.LENGTH_SHORT).show();
                return;
            }
            perfil.setSupermercados(elegidos);
            PerfilStorage.guardar(this, perfil);
            startActivity(new Intent(this, RestriccionesActivity.class));
        });
    }
}
