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
            if (valida) {
                validas.add(r);
            }
        }
        return validas;
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
