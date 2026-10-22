package com.adriancaro.tfgrecetas.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.adriancaro.tfgrecetas.R;
import com.adriancaro.tfgrecetas.datos.PerfilStorage;
import com.adriancaro.tfgrecetas.modelo.PerfilUsuario;
import com.adriancaro.tfgrecetas.modelo.Restriccion;
import com.google.android.material.button.MaterialButton;

import java.util.Locale;

/**
 * Pantalla de inicio. La primera vez explica los pasos; despues muestra
 * los datos del usuario y accesos a cada apartado de la app.
 */
public class MainActivity extends BaseActivity {

    private MaterialButton btnPrincipal;
    private MaterialButton btnCambiarDatos;
    private View cardPerfil;
    private View cardPasos;
    private View accesos;
    private TextView tvPerfil;
    private boolean logoAnimado = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        prepararPantalla(false);

        btnPrincipal = findViewById(R.id.btnPrincipal);
        btnCambiarDatos = findViewById(R.id.btnCambiarDatos);
        cardPerfil = findViewById(R.id.cardPerfil);
        cardPasos = findViewById(R.id.cardPasos);
        accesos = findViewById(R.id.accesos);
        tvPerfil = findViewById(R.id.tvPerfil);

        rellenarPaso(R.id.paso1, 1, R.string.bienvenida_paso1);
        rellenarPaso(R.id.paso2, 2, R.string.bienvenida_paso2);
        rellenarPaso(R.id.paso3, 3, R.string.bienvenida_paso3);

        // El primer acceso (el plan) va destacado con el degradado calido
        configurarAcceso(R.id.accesoPlan, "📅", R.string.acceso_plan, R.string.acceso_plan_sub,
                PlanActivity.class, true);
        configurarAcceso(R.id.accesoRecetario, "📖", R.string.acceso_recetario, R.string.acceso_recetario_sub,
                RecetarioActivity.class, false);
        configurarAcceso(R.id.accesoCompra, "🛒", R.string.acceso_compra, R.string.acceso_compra_sub,
                ListaCompraActivity.class, false);
        configurarAcceso(R.id.accesoConsejos, "👩‍🍳", R.string.acceso_consejos, R.string.acceso_consejos_sub,
                ConsejosActivity.class, false);

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
        btnPrincipal.setVisibility(completo ? View.GONE : View.VISIBLE);
        btnCambiarDatos.setVisibility(completo ? View.VISIBLE : View.GONE);
        cardPerfil.setVisibility(completo ? View.VISIBLE : View.GONE);
        accesos.setVisibility(completo ? View.VISIBLE : View.GONE);
        cardPasos.setVisibility(completo ? View.GONE : View.VISIBLE);
        if (completo) {
            tvPerfil.setText(resumenPerfil(PerfilStorage.cargar(this)));
        }

        // El logo entra con un pequeno rebote la primera vez
        if (!logoAnimado) {
            logoAnimado = true;
            View logo = findViewById(R.id.logo);
            logo.setScaleX(0.4f);
            logo.setScaleY(0.4f);
            logo.animate().scaleX(1f).scaleY(1f).setDuration(600)
                    .setInterpolator(new OvershootInterpolator(2.5f)).start();
        }
    }

    private void configurarAcceso(int idAcceso, String emoji, int titulo, int subtitulo,
                                  Class<?> pantalla, boolean destacado) {
        View acceso = findViewById(idAcceso);
        TextView tvTitulo = acceso.findViewById(R.id.tvTituloAcceso);
        TextView tvSub = acceso.findViewById(R.id.tvSubAcceso);
        ((TextView) acceso.findViewById(R.id.tvEmojiAcceso)).setText(emoji);
        tvTitulo.setText(titulo);
        tvSub.setText(subtitulo);
        if (destacado) {
            acceso.findViewById(R.id.fondoAcceso).setBackgroundResource(R.drawable.degradado_calido);
            int blanco = ContextCompat.getColor(this, R.color.sobre_degradado);
            tvTitulo.setTextColor(blanco);
            tvSub.setTextColor(blanco);
        }
        acceso.setOnClickListener(v -> startActivity(new Intent(this, pantalla)));
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
                texto.append(i == 0 ? r.getNombreVisible() : r.getNombreVisible().toLowerCase(Locale.ROOT));
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
