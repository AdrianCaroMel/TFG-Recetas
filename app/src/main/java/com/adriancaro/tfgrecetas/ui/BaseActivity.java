package com.adriancaro.tfgrecetas.ui;

import android.content.res.Configuration;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.adriancaro.tfgrecetas.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.progressindicator.LinearProgressIndicator;

/**
 * Base de todas las pantallas.
 *
 * Desde Android 15 las apps se dibujan por detras de la barra de estado y la de navegacion
 * (edge-to-edge). Si no se tiene en cuenta, la barra superior tapa el principio de la
 * pantalla (eso pasaba en "Presupuesto": no se veia el campo del dinero).
 * Aqui se reserva ese espacio en todas las pantallas a la vez.
 */
public abstract class BaseActivity extends AppCompatActivity {

    /**
     * Llamar despues de setContentView.
     *
     * @param conFlechaAtras true para mostrar la flecha de volver en la barra superior
     */
    protected void prepararPantalla(boolean conFlechaAtras) {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(conFlechaAtras);
            }
        }

        final View raiz = findViewById(R.id.raiz);
        final View appbar = findViewById(R.id.appbar);
        ViewCompat.setOnApplyWindowInsetsListener(raiz, (v, insets) -> {
            Insets barras = insets.getInsets(WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.displayCutout());
            Insets teclado = insets.getInsets(WindowInsetsCompat.Type.ime());
            int abajo = Math.max(barras.bottom, teclado.bottom);
            if (appbar != null) {
                // La barra verde se estira por detras de la barra de estado
                appbar.setPadding(0, barras.top, 0, 0);
                v.setPadding(barras.left, 0, barras.right, abajo);
            } else {
                v.setPadding(barras.left, barras.top, barras.right, abajo);
            }
            return WindowInsetsCompat.CONSUMED;
        });

        // Iconos de la barra de estado: claros sobre la barra verde, oscuros sobre fondo claro
        boolean modoOscuro = (getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        WindowInsetsControllerCompat controlador =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        controlador.setAppearanceLightStatusBars(appbar == null ? !modoOscuro : modoOscuro);
        controlador.setAppearanceLightNavigationBars(!modoOscuro);
    }

    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }

    /** Muestra "Paso X de 3" y la barra de progreso de los pasos iniciales. */
    protected void mostrarPaso(int paso) {
        TextView tvPaso = findViewById(R.id.tvPaso);
        LinearProgressIndicator progreso = findViewById(R.id.progresoPasos);
        tvPaso.setText(getString(R.string.paso_de, paso, 3));
        progreso.setProgress(paso);
    }

    /** Anade al contenedor un titulo y un grupo de chips vacio, y devuelve el grupo. */
    protected ChipGroup anadirSeccion(LinearLayout contenedor, String titulo) {
        View seccion = getLayoutInflater().inflate(R.layout.seccion_opciones, contenedor, false);
        ((TextView) seccion.findViewById(R.id.tvTituloSeccion)).setText(titulo);
        contenedor.addView(seccion);
        return seccion.findViewById(R.id.grupoChips);
    }

    /** Anade una opcion seleccionable al grupo. */
    protected Chip anadirChip(ChipGroup grupo, String texto, boolean marcado) {
        Chip chip = (Chip) getLayoutInflater().inflate(R.layout.item_chip, grupo, false);
        chip.setText(texto);
        chip.setChecked(marcado);
        // Pequeno "pop" al tocarlo para que se note que ha cambiado
        chip.setOnClickListener(v -> {
            v.setScaleX(0.88f);
            v.setScaleY(0.88f);
            v.animate().scaleX(1f).scaleY(1f).setDuration(260)
                    .setInterpolator(new android.view.animation.OvershootInterpolator(3f)).start();
        });
        grupo.addView(chip);
        return chip;
    }
}
