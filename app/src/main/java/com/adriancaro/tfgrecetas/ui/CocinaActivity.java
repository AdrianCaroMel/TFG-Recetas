package com.adriancaro.tfgrecetas.ui;

import android.media.Ringtone;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;

import com.adriancaro.tfgrecetas.R;
import com.adriancaro.tfgrecetas.datos.RepositorioDatos;
import com.adriancaro.tfgrecetas.logica.Temporizadores;
import com.adriancaro.tfgrecetas.modelo.Receta;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

/**
 * Modo cocina: muestra los pasos de uno en uno, con letra grande y un temporizador
 * cuando el paso dice un tiempo. La pantalla no se apaga mientras se cocina.
 */
public class CocinaActivity extends BaseActivity {

    private static final String ESTADO_PASO = "paso";

    private List<String> pasos;
    private int paso;
    private boolean terminado;

    private View tarjeta;
    private TextView tvPaso;
    private TextView tvNumero;
    private TextView tvTexto;
    private TextView tvTiempo;
    private View zonaTemporizador;
    private CircularProgressIndicator circulo;
    private LinearProgressIndicator progreso;
    private MaterialButton btnTemporizador;
    private MaterialButton btnAnterior;
    private MaterialButton btnSiguiente;

    private CountDownTimer temporizador;
    private long totalMs;
    private long restanteMs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cocina);
        prepararPantalla(true);

        Receta receta = RepositorioDatos.get(this).buscarReceta(
                getIntent().getIntExtra(RecetaActivity.EXTRA_ID_RECETA, -1));
        if (receta == null || receta.getPasos().isEmpty()) {
            finish();
            return;
        }
        setTitle(receta.getNombre());
        pasos = receta.getPasos();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        tarjeta = findViewById(R.id.tarjetaPaso);
        tvPaso = findViewById(R.id.tvPasoCocina);
        tvNumero = findViewById(R.id.tvNumeroGrande);
        tvTexto = findViewById(R.id.tvTextoCocina);
        tvTiempo = findViewById(R.id.tvTiempo);
        zonaTemporizador = findViewById(R.id.zonaTemporizador);
        circulo = findViewById(R.id.circuloTiempo);
        progreso = findViewById(R.id.progresoCocina);
        btnTemporizador = findViewById(R.id.btnTemporizador);
        btnAnterior = findViewById(R.id.btnAnterior);
        btnSiguiente = findViewById(R.id.btnSiguiente);
        progreso.setMax(pasos.size());

        btnAnterior.setOnClickListener(v -> {
            if (terminado) {
                terminado = false;
                mostrar(pasos.size() - 1, -1);
            } else if (paso > 0) {
                mostrar(paso - 1, -1);
            }
        });
        btnSiguiente.setOnClickListener(v -> {
            if (terminado) {
                finish();
            } else if (paso < pasos.size() - 1) {
                mostrar(paso + 1, 1);
            } else {
                mostrarFinal();
            }
        });
        btnTemporizador.setOnClickListener(v -> alternarTemporizador());

        int inicial = savedInstanceState == null ? 0 : savedInstanceState.getInt(ESTADO_PASO, 0);
        mostrar(Math.min(inicial, pasos.size() - 1), 0);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(ESTADO_PASO, paso);
    }

    @Override
    protected void onDestroy() {
        pararTemporizador();
        super.onDestroy();
    }

    /** Cambia de paso con un deslizamiento hacia el lado en que se avanza. */
    private void mostrar(int nuevo, int direccion) {
        pararTemporizador();
        paso = nuevo;
        if (direccion != 0) {
            tarjeta.setTranslationX(direccion * tarjeta.getWidth() * 0.4f);
            tarjeta.setAlpha(0f);
            tarjeta.animate().translationX(0f).alpha(1f).setDuration(280).start();
        }
        tvPaso.setText(getString(R.string.cocina_paso, paso + 1, pasos.size()));
        progreso.setProgressCompat(paso + 1, true);
        tvNumero.setText(String.valueOf(paso + 1));
        tvTexto.setText(pasos.get(paso));
        btnAnterior.setEnabled(paso > 0);
        btnSiguiente.setText(paso == pasos.size() - 1 ? R.string.cocina_terminar : R.string.siguiente);

        int segundos = Temporizadores.segundosDe(pasos.get(paso));
        zonaTemporizador.setVisibility(segundos > 0 ? View.VISIBLE : View.GONE);
        totalMs = segundos * 1000L;
        restanteMs = totalMs;
        pintarTiempo();
        btnTemporizador.setText(getString(R.string.cocina_iniciar, Temporizadores.formato(segundos)));
    }

    private void mostrarFinal() {
        pararTemporizador();
        terminado = true;
        zonaTemporizador.setVisibility(View.GONE);
        tvPaso.setText(R.string.cocina_listo);
        tvNumero.setText("🎉");
        tvTexto.setText(R.string.cocina_final);
        btnAnterior.setEnabled(true);
        btnSiguiente.setText(R.string.cocina_volver);
        tvNumero.setScaleX(0.2f);
        tvNumero.setScaleY(0.2f);
        tvNumero.animate().scaleX(1f).scaleY(1f).rotationBy(360f).setDuration(700)
                .setInterpolator(new OvershootInterpolator(2f)).start();
    }

    private void alternarTemporizador() {
        if (temporizador != null) {
            pararTemporizador();
            btnTemporizador.setText(R.string.cocina_seguir);
            return;
        }
        if (restanteMs <= 0) {
            restanteMs = totalMs;
        }
        btnTemporizador.setText(R.string.cocina_pausar);
        temporizador = new CountDownTimer(restanteMs, 100) {
            @Override
            public void onTick(long ms) {
                restanteMs = ms;
                pintarTiempo();
            }

            @Override
            public void onFinish() {
                temporizador = null;
                restanteMs = 0;
                pintarTiempo();
                avisarFinTiempo();
            }
        }.start();
    }

    private void pararTemporizador() {
        if (temporizador != null) {
            temporizador.cancel();
            temporizador = null;
        }
    }

    private void pintarTiempo() {
        tvTiempo.setText(Temporizadores.formato((restanteMs + 999) / 1000));
        circulo.setProgressCompat(totalMs == 0 ? 0 : (int) (1000 * restanteMs / totalMs), false);
    }

    /** Vibra, suena y hace latir la tarjeta para que se note aunque no se mire el móvil. */
    private void avisarFinTiempo() {
        btnTemporizador.setText(getString(R.string.cocina_iniciar, Temporizadores.formato(totalMs / 1000)));
        Vibrator vibrador = getSystemService(Vibrator.class);
        if (vibrador != null && vibrador.hasVibrator()) {
            vibrador.vibrate(VibrationEffect.createWaveform(new long[]{0, 400, 200, 400, 200, 600}, -1));
        }
        Ringtone sonido = RingtoneManager.getRingtone(this,
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        if (sonido != null) {
            sonido.play();
        }
        tarjeta.animate().scaleX(1.05f).scaleY(1.05f).setDuration(180).withEndAction(() ->
                tarjeta.animate().scaleX(1f).scaleY(1f).setDuration(260)
                        .setInterpolator(new OvershootInterpolator(3f)).start()).start();
        Snackbar.make(findViewById(R.id.raiz), R.string.cocina_fin_tiempo, Snackbar.LENGTH_LONG).show();
    }
}
