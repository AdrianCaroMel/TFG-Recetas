package com.adriancaro.tfgrecetas.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.adriancaro.tfgrecetas.R;
import com.adriancaro.tfgrecetas.datos.PerfilStorage;

/**
 * Pantalla de inicio. Si el usuario ya tiene sus datos, le deja ir directo a su plan.
 */
public class MainActivity extends AppCompatActivity {

    private Button btnPrincipal;
    private Button btnCambiarDatos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnPrincipal = findViewById(R.id.btnPrincipal);
        btnCambiarDatos = findViewById(R.id.btnCambiarDatos);

        // Boton principal: empezar o ver el plan
        btnPrincipal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (PerfilStorage.estaCompleto(MainActivity.this)) {
                    startActivity(new Intent(MainActivity.this, PlanActivity.class));
                } else {
                    startActivity(new Intent(MainActivity.this, SupermercadosActivity.class));
                }
            }
        });

        // Volver a rellenar los pasos
        btnCambiarDatos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SupermercadosActivity.class));
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Se comprueba aqui y no en onCreate para que se actualice al volver atras
        if (PerfilStorage.estaCompleto(this)) {
            btnPrincipal.setText(R.string.ver_mi_plan);
            btnCambiarDatos.setVisibility(View.VISIBLE);
        } else {
            btnPrincipal.setText(R.string.empezar);
            btnCambiarDatos.setVisibility(View.GONE);
        }
    }
}
