package com.adriancaro.tfgrecetas.ui;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.adriancaro.tfgrecetas.R;
import com.adriancaro.tfgrecetas.modelo.DiaPlan;
import com.adriancaro.tfgrecetas.modelo.Receta;

import java.util.List;

/**
 * Adaptador de la lista del plan: pinta cada dia con su comida y su cena.
 * Al pulsar una receta se abre su detalle.
 */
public class DiaAdapter extends ArrayAdapter<DiaPlan> {

    public DiaAdapter(Context context, List<DiaPlan> dias) {
        super(context, 0, dias);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View fila = convertView;
        if (fila == null) {
            fila = LayoutInflater.from(getContext()).inflate(R.layout.item_dia, parent, false);
        }

        DiaPlan dia = getItem(position);
        TextView tvDia = fila.findViewById(R.id.tvDia);
        TextView tvComida = fila.findViewById(R.id.tvComida);
        TextView tvCena = fila.findViewById(R.id.tvCena);

        tvDia.setText("Día " + dia.getNumero());
        pintarReceta(tvComida, "Comida", dia.getComida());
        pintarReceta(tvCena, "Cena", dia.getCena());
        return fila;
    }

    private void pintarReceta(TextView tv, String momento, final Receta receta) {
        if (receta == null) {
            tv.setVisibility(View.GONE);
            return;
        }
        tv.setVisibility(View.VISIBLE);
        tv.setText(momento + ": " + receta.getNombre() + "  (" + receta.getMinutos() + " min)");
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), RecetaActivity.class);
                intent.putExtra(RecetaActivity.EXTRA_ID_RECETA, receta.getId());
                getContext().startActivity(intent);
            }
        });
    }
}
