package com.adriancaro.tfgrecetas.logica;

import com.adriancaro.tfgrecetas.modelo.Ingrediente;
import com.adriancaro.tfgrecetas.modelo.PerfilUsuario;
import com.adriancaro.tfgrecetas.modelo.Producto;
import com.adriancaro.tfgrecetas.modelo.Receta;
import com.adriancaro.tfgrecetas.modelo.Restriccion;
import com.adriancaro.tfgrecetas.modelo.Supermercado;

import java.util.ArrayList;
import java.util.List;

/**
 * Se queda solo con las recetas que el usuario puede hacer:
 * todos sus ingredientes cumplen sus restricciones y se venden
 * en alguno de los supermercados que ha elegido.
 */
public class FiltroRecetas {

    public static List<Receta> recetasValidas(List<Receta> recetas, PerfilUsuario perfil) {
        List<Receta> validas = new ArrayList<Receta>();
        for (Receta r : recetas) {
            boolean valida = true;
            for (Ingrediente ing : r.getIngredientes()) {
                if (!productoPermitido(ing.getProducto(), perfil)) {
                    valida = false;
                    break;
                }
            }
            if (valida && cumpleLimitesNutricionales(r, perfil)) {
                validas.add(r);
            }
        }
        return validas;
    }

    /**
     * Algunas dietas medicas no prohiben productos sino cantidades por racion:
     * diabetes limita los hidratos y la dieta blanda / baja en grasa limitan la grasa
     * (asi quedan fuera, por ejemplo, los fritos).
     */
    public static boolean cumpleLimitesNutricionales(Receta receta, PerfilUsuario perfil) {
        Nutricion n = null;
        for (Restriccion r : perfil.getRestricciones()) {
            if (r == Restriccion.CONTROL_AZUCAR || r == Restriccion.DIETA_BLANDA || r == Restriccion.BAJA_EN_GRASA) {
                if (n == null) {
                    n = Nutricion.deRacion(receta);
                }
                if (r == Restriccion.CONTROL_AZUCAR && n.getHidratos() > Restriccion.MAX_HIDRATOS_DIABETES) {
                    return false;
                }
                if (r != Restriccion.CONTROL_AZUCAR && n.getGrasas() > Restriccion.MAX_GRASAS) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean productoPermitido(Producto p, PerfilUsuario perfil) {
        // Restricciones alimenticias
        for (Restriccion restriccion : perfil.getRestricciones()) {
            if (!restriccion.permite(p)) {
                return false;
            }
        }
        // Basta con que lo venda uno de sus supermercados
        for (Supermercado s : perfil.getSupermercados()) {
            if (p.seVendeEn(s)) {
                return true;
            }
        }
        return false;
    }
}
