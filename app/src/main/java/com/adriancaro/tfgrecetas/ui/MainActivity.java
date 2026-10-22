package com.adriancaro.tfgrecetas.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.adriancaro.tfgrecetas.R;
import com.adriancaro.tfgrecetas.datos.PerfilStorage;
import com.adriancaro.tfgrecetas.modelo.PerfilUsuario;
import com.adriancaro.tfgrecetas.modelo.Restriccion;
import com.google.android.material.button.MaterialButton;

/**
 * Pantalla de inicio. Si el usuario ya tiene sus datos, le deja ir directo a su plan.
 */
public class MainActivity extends BaseActivity {

    private MaterialButton btnPrincipal;
    private MaterialButton btnCambiarDatos;
    private View cardPerfil;
    private View cardPasos;
    private TextView tvPerfil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        prepararPantalla(false);

        btnPrincipal = findViewById(R.id.btnPrincipal);
        btnCambiarDatos = findViewById(R.id.btnCambiarDatos);
        cardPerfil = findViewById(R.id.cardPerfil);
        cardPasos = findViewById(R.id.cardPasos);
        tvPerfil = findViewById(R.id.tvPerfil);

        rellenarPaso(R.id.paso1, 1, R.string.bienvenida_paso1);
        rellenarPaso(R.id.paso2, 2, R.string.bienvenida_paso2);
        rellenarPaso(R.id.paso3, 3, R.string.bienvenida_paso3);

        // Boton principal: empezar o ver el plan
        btnPrincipal.setOnClickListener(v -> {
            if (PerfilStorage.estaCompleto(this)) {
                startActivity(new Intent(this, PlanActivity.class));
            } else {
                startActivity(new Intent(this, SupermercadosActivity.class));
            }
        });

        // Volver a rellenar los pasos
        btnCambiarDatos.setOnClickListener(v -> startActivity(new Intent(this, SupermercadosActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Se comprueba aqui y no en onCreate para que se actualice al volver atras
        boolean completo = PerfilStorage.estaCompleto(this);
        btnPrincipal.setText(completo ? R.string.ver_mi_plan : R.string.empezar);
        btnCambiarDatos.setVisibility(completo ? View.VISIBLE : View.GONE);
        cardPerfil.setVisibility(completo ? View.VISIBLE : View.GONE);
        cardPasos.setVisibility(completo ? View.GONE : View.VISIBLE);
        if (completo) {
            tvPerfil.setText(resumenPerfil(PerfilStorage.cargar(this)));
        }
    }

    private String resumenPerfil(PerfilUsuario perfil) {
        StringBuilder texto = new StringBuilder();
        texto.append("💶 ").append(Formato.euros(perfil.getPresupuesto())).append(" al mes · ")
                .append(Formato.personas(this, perfil.getPersonas()));
        texto.append("\n🛒 ").append(Formato.lista(perfil.getSupermercados()));
        if (!perfil.getRestricciones().isEmpty()) {
            texto.append("\n🥗 ");
            for (int i = 0; i < perfil.getRestricciones().size(); i++) {
                Restriccion r = perfil.getRestricciones().get(i);
                texto.append(i == 0 ? r.getNombreVisible() : r.getNombreVisible().toLowerCase(java.util.Locale.ROOT));
                if (i < perfil.getRestricciones().size() - 1) {
                    texto.append(", ");
                }
            }
        }
        return texto.toString();
    }

    private void rellenarPaso(int idPaso, int numero, int idTexto) {
        View paso = findViewById(idPaso);
        ((TextView) paso.findViewById(R.id.tvNumero)).setText(String.valueOf(numero));
        ((TextView) paso.findViewById(R.id.tvTextoPaso)).setText(idTexto);
    }
}
