package com.adriancaro.tfgrecetas.ui;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.adriancaro.tfgrecetas.R;
import com.adriancaro.tfgrecetas.datos.PerfilStorage;
import com.adriancaro.tfgrecetas.datos.RepositorioDatos;
import com.adriancaro.tfgrecetas.modelo.Ingrediente;
import com.adriancaro.tfgrecetas.modelo.Receta;

/**
 * Detalle de una receta con las cantidades ya multiplicadas por las personas.
 */
public class RecetaActivity extends AppCompatActivity {

    public static final String EXTRA_ID_RECETA = "id_receta";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receta);

        int id = getIntent().getIntExtra(EXTRA_ID_RECETA, -1);
        Receta receta = RepositorioDatos.get(this).buscarReceta(id);
        if (receta == null) {
            finish();
            return;
        }
        int personas = PerfilStorage.cargar(this).getPersonas();

        TextView tvNombre = findViewById(R.id.tvNombreReceta);
        TextView tvInfo = findViewById(R.id.tvInfoReceta);
        TextView tvIngredientes = findViewById(R.id.tvIngredientes);
        TextView tvPasos = findViewById(R.id.tvPasos);

        tvNombre.setText(receta.getNombre());
        tvInfo.setText(receta.getMinutos() + " minutos · para " + personas
                + (personas == 1 ? " persona" : " personas"));

        // Ingredientes: cantidad por persona x numero de personas
        StringBuilder ingredientes = new StringBuilder();
        for (Ingrediente ing : receta.getIngredientes()) {
            double cantidad = ing.getCantidad() * personas;
            ingredientes.append("• ")
                    .append(ing.getProducto().getNombre())
                    .append(": ")
                    .append(Formato.cantidad(cantidad, ing.getProducto().getUnidad()))
                    .append("\n");
        }
        tvIngredientes.setText(ingredientes.toString().trim());

        // Pasos numerados
        StringBuilder pasos = new StringBuilder();
        for (int i = 0; i < receta.getPasos().size(); i++) {
            pasos.append(i + 1).append(". ").append(receta.getPasos().get(i)).append("\n");
        }
        tvPasos.setText(pasos.toString().trim());
    }
}
