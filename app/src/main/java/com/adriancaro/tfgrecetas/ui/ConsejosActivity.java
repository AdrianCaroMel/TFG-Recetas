package com.adriancaro.tfgrecetas.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.adriancaro.tfgrecetas.R;

/**
 * Consejos basicos de cocina para quien cocina por primera vez.
 * Los textos estan en res/values/strings.xml (consejos_titulos y consejos_textos).
 */
public class ConsejosActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consejos);
        prepararPantalla(true);

        String[] titulos = getResources().getStringArray(R.array.consejos_titulos);
        String[] textos = getResources().getStringArray(R.array.consejos_textos);
        LinearLayout contenedor = findViewById(R.id.contenedorConsejos);
        for (int i = 0; i < titulos.length; i++) {
            View tarjeta = getLayoutInflater().inflate(R.layout.item_consejo, contenedor, false);
            ((TextView) tarjeta.findViewById(R.id.tvTituloConsejo)).setText(titulos[i]);
            ((TextView) tarjeta.findViewById(R.id.tvTextoConsejo)).setText(textos[i]);
            contenedor.addView(tarjeta);
        }
    }
}
