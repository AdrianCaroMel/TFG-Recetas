package com.adriancaro.tfgrecetas.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.adriancaro.tfgrecetas.R;
import com.adriancaro.tfgrecetas.datos.PerfilStorage;
import com.adriancaro.tfgrecetas.modelo.PerfilUsuario;

/**
 * Paso 3: presupuesto mensual, personas y que comidas planificar.
 */
public class PresupuestoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_presupuesto);

        final EditText etPresupuesto = findViewById(R.id.etPresupuesto);
        final EditText etPersonas = findViewById(R.id.etPersonas);
        final CheckBox cbComida = findViewById(R.id.cbComida);
        final CheckBox cbCena = findViewById(R.id.cbCena);
        final CheckBox cbBasicos = findViewById(R.id.cbBasicos);
        Button btnGenerar = findViewById(R.id.btnGenerar);

        // Rellenar con lo que ya tenia guardado
        final PerfilUsuario perfil = PerfilStorage.cargar(this);
        if (perfil.getPresupuesto() > 0) {
            etPresupuesto.setText(String.valueOf((int) perfil.getPresupuesto()));
        }
        etPersonas.setText(String.valueOf(perfil.getPersonas()));
        cbComida.setChecked(perfil.isPlanificarComida());
        cbCena.setChecked(perfil.isPlanificarCena());
        cbBasicos.setChecked(perfil.isTengoBasicos());

        btnGenerar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Validaciones
                double presupuesto = leerNumero(etPresupuesto.getText().toString());
                if (presupuesto <= 0) {
                    etPresupuesto.setError(getString(R.string.error_presupuesto));
                    return;
                }
                int personas = (int) leerNumero(etPersonas.getText().toString());
                if (personas < 1 || personas > 10) {
                    etPersonas.setError(getString(R.string.error_personas));
                    return;
                }
                if (!cbComida.isChecked() && !cbCena.isChecked()) {
                    Toast.makeText(PresupuestoActivity.this, R.string.error_momentos, Toast.LENGTH_SHORT).show();
                    return;
                }

                // Guardar y marcar que ya ha completado los pasos
                perfil.setPresupuesto(presupuesto);
                perfil.setPersonas(personas);
                perfil.setPlanificarComida(cbComida.isChecked());
                perfil.setPlanificarCena(cbCena.isChecked());
                perfil.setTengoBasicos(cbBasicos.isChecked());
                PerfilStorage.guardar(PresupuestoActivity.this, perfil);
                PerfilStorage.marcarCompleto(PresupuestoActivity.this);

                // Ir al plan quitando los pasos de la pila: queda Inicio -> Plan
                Intent inicio = new Intent(PresupuestoActivity.this, MainActivity.class);
                inicio.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                Intent plan = new Intent(PresupuestoActivity.this, PlanActivity.class);
                startActivities(new Intent[]{inicio, plan});
            }
        });
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
