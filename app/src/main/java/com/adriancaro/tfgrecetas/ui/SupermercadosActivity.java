package com.adriancaro.tfgrecetas.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.adriancaro.tfgrecetas.R;
import com.adriancaro.tfgrecetas.datos.PerfilStorage;
import com.adriancaro.tfgrecetas.modelo.PerfilUsuario;
import com.adriancaro.tfgrecetas.modelo.Supermercado;

import java.util.ArrayList;
import java.util.List;

/**
 * Paso 1: el usuario marca los supermercados en los que puede comprar.
 */
public class SupermercadosActivity extends AppCompatActivity {

    private final List<CheckBox> casillas = new ArrayList<CheckBox>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opciones);

        TextView tvPregunta = findViewById(R.id.tvPregunta);
        TextView tvAyuda = findViewById(R.id.tvAyuda);
        LinearLayout contenedor = findViewById(R.id.contenedorOpciones);
        Button btnSiguiente = findViewById(R.id.btnSiguiente);

        tvPregunta.setText(R.string.pregunta_supermercados);
        tvAyuda.setText(R.string.ayuda_supermercados);

        // Una casilla por supermercado, marcando los que ya eligio otra vez
        final PerfilUsuario perfil = PerfilStorage.cargar(this);
        for (Supermercado s : Supermercado.values()) {
            CheckBox cb = new CheckBox(this);
            cb.setText(s.getNombreVisible());
            cb.setTextSize(18);
            cb.setChecked(perfil.getSupermercados().contains(s));
            contenedor.addView(cb);
            casillas.add(cb);
        }

        btnSiguiente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Supermercado> elegidos = new ArrayList<Supermercado>();
                for (int i = 0; i < casillas.size(); i++) {
                    if (casillas.get(i).isChecked()) {
                        elegidos.add(Supermercado.values()[i]);
                    }
                }
                if (elegidos.isEmpty()) {
                    Toast.makeText(SupermercadosActivity.this, R.string.error_supermercado, Toast.LENGTH_SHORT).show();
                    return;
                }
                perfil.setSupermercados(elegidos);
                PerfilStorage.guardar(SupermercadosActivity.this, perfil);
                startActivity(new Intent(SupermercadosActivity.this, RestriccionesActivity.class));
            }
        });
    }
}
