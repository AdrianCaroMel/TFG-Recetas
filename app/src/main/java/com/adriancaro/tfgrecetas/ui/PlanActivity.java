package com.adriancaro.tfgrecetas.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.adriancaro.tfgrecetas.R;
import com.adriancaro.tfgrecetas.datos.PerfilStorage;
import com.adriancaro.tfgrecetas.datos.PlanStorage;
import com.adriancaro.tfgrecetas.datos.RepositorioDatos;
import com.adriancaro.tfgrecetas.logica.PlanificadorMensual;
import com.adriancaro.tfgrecetas.modelo.DiaPlan;
import com.adriancaro.tfgrecetas.modelo.Momento;
import com.adriancaro.tfgrecetas.modelo.PerfilUsuario;
import com.adriancaro.tfgrecetas.modelo.PlanMensual;
import com.adriancaro.tfgrecetas.modelo.Receta;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

/**
 * Muestra el plan del mes: resumen del gasto y la lista de dias.
 * El plan se guarda en el movil (PlanStorage), asi no cambia al cerrar la app
 * y se pueden cambiar platos sueltos. "Otro plan" genera uno nuevo.
 */
public class PlanActivity extends BaseActivity implements PlanAdapter.Acciones {

    private PlanAdapter adapter;
    private RecyclerView rvPlan;
    private PlanMensual plan;
    private PerfilUsuario perfil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan);
        prepararPantalla(true);

        rvPlan = findViewById(R.id.rvPlan);
        rvPlan.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PlanAdapter(this, this);
        rvPlan.setAdapter(adapter);

        findViewById(R.id.btnListaCompra).setOnClickListener(v ->
                startActivity(new Intent(this, ListaCompraActivity.class)));

        mostrarPlan();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_plan, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.accionOtroPlan) {
            otroPlan();
            return true;
        }
        if (item.getItemId() == R.id.accionCambiarDatos) {
            startActivity(new Intent(this, SupermercadosActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void mostrarPlan() {
        plan = PlanStorage.obtener(this);
        perfil = PlanStorage.perfilDelPlan(this);
        adapter.mostrar(plan, perfil);
    }

    /** Otro plan: se cambia la semilla del azar y se vuelve a calcular. */
    private void otroPlan() {
        // Se usa el perfil completo (no la copia limitada al super del plan) para no perder supermercados
        PerfilUsuario completo = PerfilStorage.cargar(this);
        completo.setSemilla(completo.getSemilla() + 1);
        PerfilStorage.guardar(this, completo);
        PlanStorage.borrar(this);
        mostrarPlan();
        rvPlan.scrollToPosition(0);
        rvPlan.scheduleLayoutAnimation();
        Snackbar.make(rvPlan, R.string.otro_plan_generado, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void abrirReceta(Receta receta) {
        Intent intent = new Intent(this, RecetaActivity.class);
        intent.putExtra(RecetaActivity.EXTRA_ID_RECETA, receta.getId());
        startActivity(intent);
    }

    /** Ofrece las recetas que caben en el presupuesto y cambia la elegida. */
    @Override
    public void cambiarPlato(int indiceDia, Momento momento) {
        List<Receta> recetas = RepositorioDatos.get(this).getRecetas();
        final List<PlanificadorMensual.Alternativa> alternativas =
                PlanificadorMensual.alternativas(perfil, plan, indiceDia, momento, recetas);
        if (alternativas.isEmpty()) {
            Snackbar.make(rvPlan, R.string.sin_alternativas, Snackbar.LENGTH_LONG).show();
            return;
        }

        String[] opciones = new String[alternativas.size()];
        for (int i = 0; i < alternativas.size(); i++) {
            PlanificadorMensual.Alternativa alt = alternativas.get(i);
            String precio = Math.abs(alt.getDiferencia()) < 0.005
                    ? getString(R.string.mismo_precio) : Formato.diferencia(alt.getDiferencia());
            opciones[i] = alt.getReceta().getNombre() + "  (" + precio + ")";
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.elegir_otro_plato, plan.getDias().get(indiceDia).getNumero()))
                .setItems(opciones, (dialogo, elegido) -> {
                    List<DiaPlan> dias = new ArrayList<DiaPlan>();
                    for (DiaPlan d : plan.getDias()) {
                        dias.add(d.copiar());
                    }
                    dias.get(indiceDia).setReceta(momento, alternativas.get(elegido).getReceta());
                    plan = PlanificadorMensual.desdeDias(perfil, dias, recetas);
                    PlanStorage.guardar(this, plan);
                    adapter.mostrar(plan, perfil);
                    Snackbar.make(rvPlan, R.string.plato_cambiado, Snackbar.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.cancelar, null)
                .show();
    }
}
