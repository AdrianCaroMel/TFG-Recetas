package com.adriancaro.tfgrecetas.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.adriancaro.tfgrecetas.R;
import com.adriancaro.tfgrecetas.datos.PerfilStorage;
import com.adriancaro.tfgrecetas.datos.RepositorioDatos;
import com.adriancaro.tfgrecetas.logica.PlanificadorMensual;
import com.adriancaro.tfgrecetas.modelo.PerfilUsuario;
import com.adriancaro.tfgrecetas.modelo.PlanMensual;

/**
 * Muestra el plan del mes: resumen del gasto y la lista de dias.
 * El plan se calcula al entrar (es rapido) a partir del perfil guardado.
 * Como el azar usa una semilla guardada, siempre sale el mismo plan
 * hasta que el usuario pulsa "Otro plan".
 */
public class PlanActivity extends AppCompatActivity {

    private TextView tvGasto;
    private TextView tvSobrante;
    private TextView tvAviso;
    private ProgressBar barraGasto;
    private ListView lvDias;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan);

        tvGasto = findViewById(R.id.tvGasto);
        tvSobrante = findViewById(R.id.tvSobrante);
        tvAviso = findViewById(R.id.tvAviso);
        barraGasto = findViewById(R.id.barraGasto);
        lvDias = findViewById(R.id.lvDias);
        Button btnListaCompra = findViewById(R.id.btnListaCompra);
        Button btnOtroPlan = findViewById(R.id.btnOtroPlan);

        btnListaCompra.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(PlanActivity.this, ListaCompraActivity.class));
            }
        });

        // Otro plan: se cambia la semilla del azar y se vuelve a calcular
        btnOtroPlan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PerfilUsuario perfil = PerfilStorage.cargar(PlanActivity.this);
                perfil.setSemilla(perfil.getSemilla() + 1);
                PerfilStorage.guardar(PlanActivity.this, perfil);
                mostrarPlan();
            }
        });

        mostrarPlan();
    }

    private void mostrarPlan() {
        PerfilUsuario perfil = PerfilStorage.cargar(this);
        PlanMensual plan = PlanificadorMensual.generar(perfil, RepositorioDatos.get(this).getRecetas());

        // Resumen del gasto
        tvGasto.setText("Gasto del mes: " + Formato.euros(plan.getCosteTotal())
                + " de " + Formato.euros(plan.getPresupuesto()));
        tvSobrante.setText("Te sobran " + Formato.euros(plan.getSobrante())
                + " para desayunos, fruta y caprichos · " + perfil.getPersonas()
                + (perfil.getPersonas() == 1 ? " persona" : " personas"));
        int porcentaje = 0;
        if (plan.getPresupuesto() > 0) {
            porcentaje = (int) Math.round(plan.getCosteTotal() * 100 / plan.getPresupuesto());
        }
        barraGasto.setProgress(Math.min(100, porcentaje));

        // Aviso si el presupuesto no llega para todo el mes
        if (plan.isCompleto()) {
            tvAviso.setVisibility(View.GONE);
        } else {
            tvAviso.setVisibility(View.VISIBLE);
            tvAviso.setText(plan.getAviso());
        }

        lvDias.setAdapter(new DiaAdapter(this, plan.getDias()));
    }
}
