package com.adriancaro.tfgrecetas.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.adriancaro.tfgrecetas.R;
import com.adriancaro.tfgrecetas.datos.PerfilStorage;
import com.adriancaro.tfgrecetas.modelo.PerfilUsuario;
import com.adriancaro.tfgrecetas.modelo.Restriccion;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Paso 2: alergias, intolerancias y dietas. Puede no marcar ninguna.
 */
public class RestriccionesActivity extends BaseActivity {

    private final Map<Restriccion, Chip> chips = new HashMap<Restriccion, Chip>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opciones);
        prepararPantalla(true);
        mostrarPaso(2);

        TextView tvPregunta = findViewById(R.id.tvPregunta);
        TextView tvAyuda = findViewById(R.id.tvAyuda);
        LinearLayout contenedor = findViewById(R.id.contenedorOpciones);
        Button btnSiguiente = findViewById(R.id.btnSiguiente);

        tvPregunta.setText(R.string.pregunta_restricciones);
        tvAyuda.setText(R.string.ayuda_restricciones);

        // Tres grupos: alergias / intolerancias, dietas y dietas por salud
        final PerfilUsuario perfil = PerfilStorage.cargar(this);
        ChipGroup alergias = anadirSeccion(contenedor, getString(R.string.seccion_alergias));
        ChipGroup dietas = anadirSeccion(contenedor, getString(R.string.seccion_dieta));
        ChipGroup medicas = anadirSeccion(contenedor, getString(R.string.seccion_dieta_medica));
        TextView aviso = new TextView(this);
        aviso.setText(R.string.aviso_dieta_medica);
        aviso.setTextAppearance(R.style.Texto_Secundario);
        aviso.setPadding(0, 12, 0, 0);
        contenedor.addView(aviso);
        for (Restriccion r : Restriccion.values()) {
            ChipGroup grupo = r.esDietaMedica() ? medicas : r.esDieta() ? dietas : alergias;
            chips.put(r, anadirChip(grupo, r.getNombreVisible(), perfil.getRestricciones().contains(r)));
        }

        // Vegana ya incluye vegetariana: si marca vegana, se marca tambien vegetariana
        chips.get(Restriccion.VEGANO).setOnCheckedChangeListener((boton, marcado) -> {
            if (marcado) {
                chips.get(Restriccion.VEGETARIANO).setChecked(true);
            }
        });

        btnSiguiente.setOnClickListener(v -> {
            List<Restriccion> elegidas = new ArrayList<Restriccion>();
            for (Restriccion r : Restriccion.values()) {
                if (chips.get(r).isChecked()) {
                    elegidas.add(r);
                }
            }
            perfil.setRestricciones(elegidas);
            PerfilStorage.guardar(this, perfil);
            startActivity(new Intent(this, PresupuestoActivity.class));
        });
    }
}
