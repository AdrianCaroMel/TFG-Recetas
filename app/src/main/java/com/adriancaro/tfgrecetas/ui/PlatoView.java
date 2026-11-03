package com.adriancaro.tfgrecetas.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.adriancaro.tfgrecetas.R;
import com.adriancaro.tfgrecetas.logica.Composicion;
import com.adriancaro.tfgrecetas.modelo.Receta;

import java.util.ArrayList;
import java.util.List;

/**
 * Dibuja una receta como un plato visto desde arriba: cada ingrediente es un trozo
 * del color de su grupo de alimentos y su tamaño depende de la cantidad que lleva.
 * Sustituye a los emojis, que hacían la app más infantil.
 */
public class PlatoView extends View {

    private final Paint pincel = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF anillo = new RectF();
    private List<Composicion.Parte> partes = new ArrayList<Composicion.Parte>();

    public PlatoView(Context context) {
        super(context);
    }

    public PlatoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /** Cambia la receta y vuelve a dibujar. */
    public void setReceta(Receta receta) {
        partes = Composicion.de(receta);
        setContentDescription(receta.getNombre());
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float lado = Math.min(getWidth(), getHeight());
        float cx = getWidth() / 2f, cy = getHeight() / 2f;
        float radio = lado / 2f;

        // Plato: círculo de la superficie con un borde fino y un segundo aro interior
        pincel.setStyle(Paint.Style.FILL);
        pincel.setColor(ContextCompat.getColor(getContext(), R.color.superficie));
        canvas.drawCircle(cx, cy, radio - 1, pincel);
        pincel.setStyle(Paint.Style.STROKE);
        pincel.setStrokeWidth(Math.max(1f, lado / 100f));
        pincel.setColor(ContextCompat.getColor(getContext(), R.color.borde));
        canvas.drawCircle(cx, cy, radio - 1, pincel);
        canvas.drawCircle(cx, cy, radio * 0.85f, pincel);

        // Comida: un arco grueso por ingrediente, con una pequeña separación entre trozos
        float grosor = radio * 0.44f;
        float medio = radio * 0.27f + grosor / 2f;
        anillo.set(cx - medio, cy - medio, cx + medio, cy + medio);
        pincel.setStrokeWidth(grosor);
        pincel.setStrokeCap(Paint.Cap.BUTT);
        float angulo = -90f;
        for (Composicion.Parte parte : partes) {
            float barrido = (float) (parte.getFraccion() * 360f);
            float hueco = Math.min(2.5f, barrido / 4f);
            pincel.setColor(ContextCompat.getColor(getContext(), colorDe(parte.getGrupo())));
            canvas.drawArc(anillo, angulo + hueco, barrido - 2 * hueco, false, pincel);
            angulo += barrido;
        }
    }

    /** Color de cada grupo de alimentos (definidos en colors.xml, claro y oscuro). */
    private static int colorDe(String grupo) {
        switch (grupo) {
            case Composicion.CEREAL:
                return R.color.c_cereal;
            case Composicion.VERDURA:
                return R.color.c_verdura;
            case Composicion.LEGUMBRE:
                return R.color.c_legumbre;
            case Composicion.CARNE:
                return R.color.c_carne;
            case Composicion.PESCADO:
                return R.color.c_pescado;
            case Composicion.LACTEO:
                return R.color.c_lacteo;
            case Composicion.HUEVO:
                return R.color.c_huevo;
            case Composicion.CONDIMENTO:
                return R.color.c_condimento;
            default:
                return R.color.c_despensa;
        }
    }
}
