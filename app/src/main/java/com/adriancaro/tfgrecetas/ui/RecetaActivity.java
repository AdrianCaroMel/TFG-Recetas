package com.adriancaro.tfgrecetas.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.adriancaro.tfgrecetas.R;
import com.adriancaro.tfgrecetas.datos.PerfilStorage;
import com.adriancaro.tfgrecetas.datos.RepositorioDatos;
import com.adriancaro.tfgrecetas.logica.CestaCompra;
import com.adriancaro.tfgrecetas.modelo.Ingrediente;
import com.adriancaro.tfgrecetas.modelo.PerfilUsuario;
import com.adriancaro.tfgrecetas.modelo.Receta;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

/**
 * Detalle de una receta con las cantidades ya multiplicadas por las personas.
 */
public class RecetaActivity extends BaseActivity {

    public static final String EXTRA_ID_RECETA = "id_receta";

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
        PerfilUsuario perfil = PerfilStorage.cargar(this);
        int personas = perfil.getPersonas();

        ((TextView) findViewById(R.id.tvNombreReceta)).setText(receta.getNombre());

        // Datos rapidos: tiempo, personas y precio por racion
        ChipGroup grupoInfo = findViewById(R.id.grupoInfo);
        anadirInfo(grupoInfo, "⏱ " + getString(R.string.minutos, receta.getMinutos()));
        anadirInfo(grupoInfo, "👥 " + Formato.personas(this, personas));
        anadirInfo(grupoInfo, "💶 " + getString(R.string.por_racion,
                Formato.euros(new CestaCompra(perfil).costeRacion(receta))));

        // Ingredientes: cantidad por persona x numero de personas
        LinearLayout contenedorIngredientes = findViewById(R.id.contenedorIngredientes);
        for (Ingrediente ing : receta.getIngredientes()) {
            View fila = getLayoutInflater().inflate(R.layout.item_ingrediente, contenedorIngredientes, false);
            ((TextView) fila.findViewById(R.id.tvIngrediente)).setText(ing.getProducto().getNombre());
            ((TextView) fila.findViewById(R.id.tvCantidad)).setText(
                    Formato.cantidad(ing.getCantidad() * personas, ing.getProducto().getUnidad()));
            contenedorIngredientes.addView(fila);
        }

        // Pasos numerados
        LinearLayout contenedorPasos = findViewById(R.id.contenedorPasos);
        for (int i = 0; i < receta.getPasos().size(); i++) {
            View fila = getLayoutInflater().inflate(R.layout.item_paso, contenedorPasos, false);
            ((TextView) fila.findViewById(R.id.tvNumero)).setText(String.valueOf(i + 1));
            ((TextView) fila.findViewById(R.id.tvTextoPaso)).setText(receta.getPasos().get(i));
            contenedorPasos.addView(fila);
        }
    }

    private void anadirInfo(ChipGroup grupo, String texto) {
        Chip chip = new Chip(this);
        chip.setText(texto);
        chip.setClickable(false);
        chip.setCheckable(false);
        grupo.addView(chip);
    }
}
