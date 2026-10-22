package com.adriancaro.tfgrecetas.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.adriancaro.tfgrecetas.R;
import com.adriancaro.tfgrecetas.datos.PerfilStorage;
import com.adriancaro.tfgrecetas.datos.PlanStorage;
import com.adriancaro.tfgrecetas.datos.RepositorioDatos;
import com.adriancaro.tfgrecetas.logica.PlanificadorMensual;
import com.adriancaro.tfgrecetas.modelo.PerfilUsuario;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * Paso 3: presupuesto mensual, personas y que comidas planificar.
 * Mientras el usuario rellena los datos se le muestra el gasto minimo estimado.
 */
public class PresupuestoActivity extends BaseActivity {

    private static final int MAX_PERSONAS = 10;

    private PerfilUsuario perfil;
    private TextInputLayout tilPresupuesto;
    private TextInputEditText etPresupuesto;
    private TextView tvPersonas;
    private MaterialButton btnMenos;
    private MaterialButton btnMas;
    private Chip chipComida;
    private Chip chipCena;
    private MaterialSwitch swBasicos;
    private MaterialCardView cardEstimacion;
    private TextView tvEstimacion;

    private int personas;
    /** Ultimo minimo calculado (se recalcula solo cuando cambian personas, comidas o basicos). */
    private double costeMinimo = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_presupuesto);
        prepararPantalla(true);
        mostrarPaso(3);

        tilPresupuesto = findViewById(R.id.tilPresupuesto);
        etPresupuesto = findViewById(R.id.etPresupuesto);
        tvPersonas = findViewById(R.id.tvPersonas);
        btnMenos = findViewById(R.id.btnMenos);
        btnMas = findViewById(R.id.btnMas);
        chipComida = findViewById(R.id.chipComida);
        chipCena = findViewById(R.id.chipCena);
        swBasicos = findViewById(R.id.swBasicos);
        cardEstimacion = findViewById(R.id.cardEstimacion);
        tvEstimacion = findViewById(R.id.tvEstimacion);

        // Rellenar con lo que ya tenia guardado
        perfil = PerfilStorage.cargar(this);
        if (perfil.getPresupuesto() > 0) {
            etPresupuesto.setText(String.valueOf((int) perfil.getPresupuesto()));
        }
        personas = Math.max(1, Math.min(MAX_PERSONAS, perfil.getPersonas()));
        chipComida.setChecked(perfil.isPlanificarComida());
        chipCena.setChecked(perfil.isPlanificarCena());
        swBasicos.setChecked(perfil.isTengoBasicos());

        btnMenos.setOnClickListener(v -> cambiarPersonas(-1));
        btnMas.setOnClickListener(v -> cambiarPersonas(+1));
        chipComida.setOnCheckedChangeListener((b, marcado) -> recalcularMinimo());
        chipCena.setOnCheckedChangeListener((b, marcado) -> recalcularMinimo());
        swBasicos.setOnCheckedChangeListener((b, marcado) -> recalcularMinimo());
        etPresupuesto.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                tilPresupuesto.setError(null);
                mostrarEstimacion();
            }
        });

        findViewById(R.id.btnGenerar).setOnClickListener(v -> generar());
        cambiarPersonas(0);
    }

    private void cambiarPersonas(int cambio) {
        personas = Math.max(1, Math.min(MAX_PERSONAS, personas + cambio));
        tvPersonas.setText(String.valueOf(personas));
        btnMenos.setEnabled(personas > 1);
        btnMas.setEnabled(personas < MAX_PERSONAS);
        recalcularMinimo();
    }

    private void leerFormulario() {
        perfil.setPersonas(personas);
        perfil.setPlanificarComida(chipComida.isChecked());
        perfil.setPlanificarCena(chipCena.isChecked());
        perfil.setTengoBasicos(swBasicos.isChecked());
    }

    /** El calculo es rapido (milisegundos), se puede hacer directamente al cambiar algo. */
    private void recalcularMinimo() {
        leerFormulario();
        costeMinimo = PlanificadorMensual.costeMinimo(perfil, RepositorioDatos.get(this).getRecetas());
        mostrarEstimacion();
    }

    private void mostrarEstimacion() {
        double presupuesto = leerNumero(String.valueOf(etPresupuesto.getText()));
        boolean aviso;
        String texto;
        if (!chipComida.isChecked() && !chipCena.isChecked()) {
            aviso = true;
            texto = getString(R.string.error_momentos);
        } else if (costeMinimo < 0) {
            aviso = true;
            texto = getString(R.string.estimacion_sin_recetas);
        } else if (presupuesto <= 0) {
            aviso = false;
            texto = getString(R.string.estimacion_sin_presupuesto, Formato.euros(costeMinimo));
        } else if (presupuesto >= costeMinimo) {
            aviso = false;
            texto = getString(R.string.estimacion_llega, Formato.euros(costeMinimo));
        } else {
            aviso = true;
            texto = getString(R.string.estimacion_no_llega, Formato.euros(costeMinimo));
        }
        tvEstimacion.setText(texto);
        cardEstimacion.setCardBackgroundColor(ContextCompat.getColor(this,
                aviso ? R.color.rojo_claro : R.color.verde_claro));
        tvEstimacion.setTextColor(ContextCompat.getColor(this,
                aviso ? R.color.rojo_aviso : R.color.verde_oscuro));
    }

    private void generar() {
        double presupuesto = leerNumero(String.valueOf(etPresupuesto.getText()));
        if (presupuesto <= 0) {
            tilPresupuesto.setError(getString(R.string.error_presupuesto));
            etPresupuesto.requestFocus();
            return;
        }
        if (!chipComida.isChecked() && !chipCena.isChecked()) {
            Snackbar.make(tvEstimacion, R.string.error_momentos, Snackbar.LENGTH_SHORT).show();
            return;
        }

        // Guardar, marcar que ya ha completado los pasos y olvidar el plan anterior
        leerFormulario();
        perfil.setPresupuesto(presupuesto);
        PerfilStorage.guardar(this, perfil);
        PerfilStorage.marcarCompleto(this);
        PlanStorage.borrar(this);

        // Ir al plan quitando los pasos de la pila: queda Inicio -> Plan
        Intent inicio = new Intent(this, MainActivity.class);
        inicio.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        Intent plan = new Intent(this, PlanActivity.class);
        startActivities(new Intent[]{inicio, plan});
    }

    /** Lee un numero aceptando coma o punto decimal. Devuelve -1 si no es valido. */
    private double leerNumero(String texto) {
        try {
            return Double.parseDouble(texto.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
