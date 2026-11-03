package com.adriancaro.tfgrecetas.ui;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.adriancaro.tfgrecetas.R;
import com.adriancaro.tfgrecetas.datos.PerfilStorage;
import com.adriancaro.tfgrecetas.datos.RepositorioDatos;
import com.adriancaro.tfgrecetas.modelo.PerfilUsuario;
import com.adriancaro.tfgrecetas.modelo.Receta;
import com.adriancaro.tfgrecetas.modelo.Restriccion;
import com.google.android.material.button.MaterialButton;

import java.util.List;
import java.util.Locale;
import java.util.Random;

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
    private ValueAnimator animacionFondo;
    private ObjectAnimator giroPlato;

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

        // Plato de adorno de la portada: una receta al azar que gira muy despacio
        List<Receta> recetas = RepositorioDatos.get(this).getRecetas();
        PlatoView platoPortada = findViewById(R.id.platoPortada);
        platoPortada.setReceta(recetas.get(new Random().nextInt(recetas.size())));
        giroPlato = ObjectAnimator.ofFloat(platoPortada, View.ROTATION, 0f, 360f);
        giroPlato.setDuration(60000);
        giroPlato.setRepeatCount(ValueAnimator.INFINITE);
        giroPlato.setInterpolator(new LinearInterpolator());

        rellenarPaso(R.id.paso1, 1, R.string.bienvenida_paso1);
        rellenarPaso(R.id.paso2, 2, R.string.bienvenida_paso2);
        rellenarPaso(R.id.paso3, 3, R.string.bienvenida_paso3);

        // El primer acceso (el plan) va destacado con el degradado calido
        configurarAcceso(R.id.accesoPlan, R.drawable.ic_calendario, R.string.acceso_plan, R.string.acceso_plan_sub,
                PlanActivity.class, true);
        configurarAcceso(R.id.accesoRecetario, R.drawable.ic_libro, R.string.acceso_recetario, R.string.acceso_recetario_sub,
                RecetarioActivity.class, false);
        configurarAcceso(R.id.accesoCompra, R.drawable.ic_carrito, R.string.acceso_compra, R.string.acceso_compra_sub,
                ListaCompraActivity.class, false);
        configurarAcceso(R.id.accesoConsejos, R.drawable.ic_bombilla, R.string.acceso_consejos, R.string.acceso_consejos_sub,
                ConsejosActivity.class, false);

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

        prepararFondoAnimado();
    }

    /**
     * La cabecera cambia muy despacio entre dos degradados verdes (8 s ida y vuelta).
     * Da sensacion de "vida" sin distraer.
     */
    private void prepararFondoAnimado() {
        final GradientDrawable fondo = (GradientDrawable) findViewById(R.id.appbar).getBackground().mutate();
        final int a1 = ContextCompat.getColor(this, R.color.degradado_inicio);
        final int a2 = ContextCompat.getColor(this, R.color.degradado_alt_inicio);
        final int b1 = ContextCompat.getColor(this, R.color.degradado_fin);
        final int b2 = ContextCompat.getColor(this, R.color.degradado_alt_fin);
        final ArgbEvaluator mezcla = new ArgbEvaluator();
        animacionFondo = ValueAnimator.ofFloat(0f, 1f);
        animacionFondo.setDuration(8000);
        animacionFondo.setRepeatMode(ValueAnimator.REVERSE);
        animacionFondo.setRepeatCount(ValueAnimator.INFINITE);
        animacionFondo.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator a) {
                float f = (Float) a.getAnimatedValue();
                fondo.setColors(new int[]{(Integer) mezcla.evaluate(f, a1, a2), (Integer) mezcla.evaluate(f, b1, b2)});
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        animacionFondo.pause();
        giroPlato.pause();
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

        if (giroPlato.isPaused()) {
            giroPlato.resume();
        } else if (!giroPlato.isStarted()) {
            giroPlato.start();
        }
        if (animacionFondo.isPaused()) {
            animacionFondo.resume();
        } else if (!animacionFondo.isStarted()) {
            animacionFondo.start();
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

    private void configurarAcceso(int idAcceso, int icono, int titulo, int subtitulo,
                                  Class<?> pantalla, boolean destacado) {
        View acceso = findViewById(idAcceso);
        TextView tvTitulo = acceso.findViewById(R.id.tvTituloAcceso);
        TextView tvSub = acceso.findViewById(R.id.tvSubAcceso);
        ImageView ivIcono = acceso.findViewById(R.id.ivIconoAcceso);
        ivIcono.setImageResource(icono);
        ivIcono.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.verde)));
        tvTitulo.setText(titulo);
        tvSub.setText(subtitulo);
        if (destacado) {
            acceso.findViewById(R.id.fondoAcceso).setBackgroundResource(R.drawable.degradado_calido);
            int blanco = ContextCompat.getColor(this, R.color.sobre_degradado);
            tvTitulo.setTextColor(blanco);
            tvSub.setTextColor(blanco);
            // En el acceso destacado, icono azafrán sobre un cuadrado translúcido
            ivIcono.setBackgroundResource(R.drawable.fondo_icono_destacado);
            ivIcono.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.azafran)));
        }
        acceso.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, pantalla));
            }
        });
    }

    private String resumenPerfil(PerfilUsuario perfil) {
        StringBuilder texto = new StringBuilder();
        texto.append(Formato.euros(perfil.getPresupuesto())).append(" al mes · ")
                .append(Formato.personas(this, perfil.getPersonas()));
        texto.append("\n").append(Formato.lista(perfil.getSupermercados()));
        if (!perfil.getRestricciones().isEmpty()) {
            texto.append("\n");
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
