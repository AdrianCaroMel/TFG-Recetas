package com.adriancaro.tfgrecetas.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.adriancaro.tfgrecetas.R;
import com.adriancaro.tfgrecetas.logica.CestaCompra;
import com.adriancaro.tfgrecetas.modelo.DiaPlan;
import com.adriancaro.tfgrecetas.modelo.Momento;
import com.adriancaro.tfgrecetas.modelo.PerfilUsuario;
import com.adriancaro.tfgrecetas.modelo.PlanMensual;
import com.adriancaro.tfgrecetas.modelo.Receta;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Lista del plan con tres tipos de fila:
 *  - el resumen del gasto (arriba del todo),
 *  - un separador "Semana N" cada 7 dias,
 *  - una tarjeta por dia con su comida y su cena.
 */
public class PlanAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    /** Lo que pasa al pulsar en una receta o en el boton de cambiarla. */
    public interface Acciones {
        void abrirReceta(Receta receta);

        void cambiarPlato(int indiceDia, Momento momento);
    }

    private static final int TIPO_RESUMEN = 0;
    private static final int TIPO_SEMANA = 1;
    private static final int TIPO_DIA = 2;

    private final Context context;
    private final Acciones acciones;
    private PlanMensual plan;
    private PerfilUsuario perfil;
    private CestaCompra precios;
    /** Cada fila es: null (resumen), un Integer (numero de semana) o un int[] {indiceDia}. */
    private final List<Object> filas = new ArrayList<Object>();

    public PlanAdapter(Context context, Acciones acciones) {
        this.context = context;
        this.acciones = acciones;
    }

    public void mostrar(PlanMensual plan, PerfilUsuario perfil) {
        this.plan = plan;
        this.perfil = perfil;
        this.precios = new CestaCompra(perfil);
        filas.clear();
        filas.add(null);
        for (int i = 0; i < plan.getDias().size(); i++) {
            if (i % 7 == 0) {
                filas.add(i / 7 + 1);
            }
            filas.add(new int[]{i});
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        Object fila = filas.get(position);
        if (fila == null) {
            return TIPO_RESUMEN;
        }
        return fila instanceof Integer ? TIPO_SEMANA : TIPO_DIA;
    }

    @Override
    public int getItemCount() {
        return filas.size();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int tipo) {
        LayoutInflater inflater = LayoutInflater.from(context);
        int layout = tipo == TIPO_RESUMEN ? R.layout.item_resumen
                : tipo == TIPO_SEMANA ? R.layout.item_semana : R.layout.item_dia;
        return new RecyclerView.ViewHolder(inflater.inflate(layout, parent, false)) { };
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        View vista = holder.itemView;
        switch (getItemViewType(position)) {
            case TIPO_RESUMEN:
                pintarResumen(vista);
                break;
            case TIPO_SEMANA:
                ((TextView) vista).setText(context.getString(R.string.semana, (Integer) filas.get(position)));
                break;
            default:
                pintarDia(vista, ((int[]) filas.get(position))[0]);
        }
    }

    private void pintarResumen(View vista) {
        TextView tvGasto = vista.findViewById(R.id.tvGasto);
        TextView tvPresupuesto = vista.findViewById(R.id.tvPresupuesto);
        LinearProgressIndicator barra = vista.findViewById(R.id.barraGasto);
        TextView tvSobrante = vista.findViewById(R.id.tvSobrante);
        TextView tvDetalles = vista.findViewById(R.id.tvDetalles);
        View cardAviso = vista.findViewById(R.id.cardAviso);
        TextView tvAviso = vista.findViewById(R.id.tvAviso);

        tvGasto.setText(Formato.euros(plan.getCosteTotal()));
        tvPresupuesto.setText(context.getString(R.string.de_presupuesto, Formato.euros(plan.getPresupuesto())));
        int porcentaje = 0;
        if (plan.getPresupuesto() > 0) {
            porcentaje = (int) Math.round(plan.getCosteTotal() * 100 / plan.getPresupuesto());
        }
        barra.setProgress(Math.min(100, porcentaje));

        boolean sobra = plan.getSobrante() > 0.005;
        tvSobrante.setVisibility(sobra ? View.VISIBLE : View.GONE);
        tvSobrante.setText(context.getString(R.string.te_sobran, Formato.euros(plan.getSobrante())));
        tvDetalles.setText(context.getString(R.string.resumen_plan, plan.getDias().size(),
                recetasDistintas(), Formato.personas(context, perfil.getPersonas())));

        boolean hayAviso = !plan.isCompleto() && !plan.getAviso().isEmpty();
        cardAviso.setVisibility(hayAviso ? View.VISIBLE : View.GONE);
        tvAviso.setText(plan.getAviso());
    }

    private void pintarDia(View vista, int indiceDia) {
        DiaPlan dia = plan.getDias().get(indiceDia);
        LinearLayout contenedor = vista.findViewById(R.id.contenedorMomentos);
        TextView tvDia = vista.findViewById(R.id.tvDia);
        tvDia.setText(context.getString(R.string.dia, dia.getNumero()));

        // Se quitan las filas de un uso anterior de esta tarjeta (la primera vista es el titulo)
        while (contenedor.getChildCount() > 1) {
            contenedor.removeViewAt(1);
        }
        for (Momento m : Momento.values()) {
            Receta receta = dia.getReceta(m);
            if (receta != null) {
                contenedor.addView(crearFilaMomento(contenedor, indiceDia, m, receta));
            }
        }
    }

    private View crearFilaMomento(ViewGroup padre, int indiceDia, Momento momento, Receta receta) {
        View fila = LayoutInflater.from(context).inflate(R.layout.item_momento, padre, false);
        ((TextView) fila.findViewById(R.id.tvMomento)).setText(momento.getNombreVisible());
        ((TextView) fila.findViewById(R.id.tvNombreReceta)).setText(receta.getNombre());
        String info = "⏱ " + context.getString(R.string.minutos, receta.getMinutos()) + "  ·  "
                + context.getString(R.string.por_racion, Formato.euros(precios.costeRacion(receta)));
        ((TextView) fila.findViewById(R.id.tvInfoReceta)).setText(info);

        fila.setOnClickListener(v -> acciones.abrirReceta(receta));
        fila.findViewById(R.id.btnCambiar).setOnClickListener(v -> acciones.cambiarPlato(indiceDia, momento));
        return fila;
    }

    private int recetasDistintas() {
        Set<Integer> ids = new HashSet<Integer>();
        for (DiaPlan dia : plan.getDias()) {
            for (Momento m : Momento.values()) {
                if (dia.getReceta(m) != null) {
                    ids.add(dia.getReceta(m).getId());
                }
            }
        }
        return ids.size();
    }
}
