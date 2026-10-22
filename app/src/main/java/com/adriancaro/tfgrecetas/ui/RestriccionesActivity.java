package com.adriancaro.tfgrecetas.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.adriancaro.tfgrecetas.R;
import com.adriancaro.tfgrecetas.datos.PerfilStorage;
import com.adriancaro.tfgrecetas.modelo.PerfilUsuario;
import com.adriancaro.tfgrecetas.modelo.Restriccion;

import java.util.ArrayList;
import java.util.List;

/**
 * Paso 2: alergias, intolerancias y dietas. Puede no marcar ninguna.
 */
public class RestriccionesActivity extends AppCompatActivity {

    private final List<CheckBox> casillas = new ArrayList<CheckBox>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opciones);

        TextView tvPregunta = findViewById(R.id.tvPregunta);
        TextView tvAyuda = findViewById(R.id.tvAyuda);
        LinearLayout contenedor = findViewById(R.id.contenedorOpciones);
        Button btnSiguiente = findViewById(R.id.btnSiguiente);

        tvPregunta.setText(R.string.pregunta_restricciones);
        tvAyuda.setText(R.string.ayuda_restricciones);

        final PerfilUsuario perfil = PerfilStorage.cargar(this);
        for (Restriccion r : Restriccion.values()) {
            CheckBox cb = new CheckBox(this);
            cb.setText(r.getNombreVisible());
            cb.setTextSize(18);
            cb.setChecked(perfil.getRestricciones().contains(r));
            contenedor.addView(cb);
            casillas.add(cb);
        }

        btnSiguiente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Restriccion> elegidas = new ArrayList<Restriccion>();
                for (int i = 0; i < casillas.size(); i++) {
                    if (casillas.get(i).isChecked()) {
                        elegidas.add(Restriccion.values()[i]);
                    }
                }
                perfil.setRestricciones(elegidas);
                PerfilStorage.guardar(RestriccionesActivity.this, perfil);
                startActivity(new Intent(RestriccionesActivity.this, PresupuestoActivity.class));
            }
        });
    }
}
