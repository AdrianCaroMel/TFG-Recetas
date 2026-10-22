package com.adriancaro.tfgrecetas.logica;

import com.adriancaro.tfgrecetas.modelo.Ingrediente;
import com.adriancaro.tfgrecetas.modelo.LineaCompra;
import com.adriancaro.tfgrecetas.modelo.PerfilUsuario;
import com.adriancaro.tfgrecetas.modelo.Producto;
import com.adriancaro.tfgrecetas.modelo.Receta;
import com.adriancaro.tfgrecetas.modelo.Supermercado;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Va acumulando lo que hace falta comprar en el mes.
 *
 * Idea clave: en el super no se compran 80 g de arroz, se compra un paquete de 1 kg.
 * Por eso el coste se calcula en envases enteros, y si un paquete ya comprado
 * todavia tiene producto, la siguiente receta que lo use sale "gratis".
 */
public class CestaCompra {

    private final PerfilUsuario perfil;
    private final Map<Integer, Double> cantidades = new HashMap<Integer, Double>();
    private final Map<Integer, Producto> productos = new HashMap<Integer, Producto>();
    private double costeTotal = 0;

    public CestaCompra(PerfilUsuario perfil) {
        this.perfil = perfil;
    }

    /** Lo que subiria el gasto del mes si se anade esta receta. */
    public double costeAnadir(Receta receta) {
        double coste = 0;
        for (Ingrediente ing : receta.getIngredientes()) {
            Producto p = ing.getProducto();
            double actual = cantidadDe(p);
            double nueva = actual + ing.getCantidad() * perfil.getPersonas();
            int envasesExtra = envases(p, nueva) - envases(p, actual);
            coste += envasesExtra * precioEnvase(p);
        }
        return coste;
    }

    public void anadir(Receta receta) {
        costeTotal += costeAnadir(receta);
        for (Ingrediente ing : receta.getIngredientes()) {
            Producto p = ing.getProducto();
            productos.put(p.getId(), p);
            cantidades.put(p.getId(), cantidadDe(p) + ing.getCantidad() * perfil.getPersonas());
        }
    }

    public double getCosteTotal() {
        return costeTotal;
    }

    /** Lista de la compra ordenada por supermercado y categoria. */
    public List<LineaCompra> generarLista() {
        List<LineaCompra> lista = new ArrayList<LineaCompra>();
        for (Producto p : productos.values()) {
            if (perfil.isTengoBasicos() && p.esBasico()) {
                continue; // ya lo tiene en casa
            }
            double cantidad = cantidadDe(p);
            int numEnvases = envases(p, cantidad);
            Supermercado s = mejorSupermercado(p);
            lista.add(new LineaCompra(p, cantidad, numEnvases, s, numEnvases * p.precioEn(s)));
        }

        Collections.sort(lista, new Comparator<LineaCompra>() {
            @Override
            public int compare(LineaCompra a, LineaCompra b) {
                int c = a.getSupermercado().compareTo(b.getSupermercado());
                if (c == 0) {
                    c = a.getProducto().getCategoria().compareTo(b.getProducto().getCategoria());
                }
                if (c == 0) {
                    c = a.getProducto().getNombre().compareTo(b.getProducto().getNombre());
                }
                return c;
            }
        });
        return lista;
    }

    /**
     * Coste aproximado de UNA racion de la receta, pagando solo lo que se gasta de cada
     * envase (sin redondear a paquetes enteros). Sirve para mostrar el precio por plato.
     */
    public double costeRacion(Receta receta) {
        double coste = 0;
        for (Ingrediente ing : receta.getIngredientes()) {
            Producto p = ing.getProducto();
            coste += ing.getCantidad() * precioEnvase(p) / p.getEnvase();
        }
        return coste;
    }

    /** Supermercado (de los elegidos por el usuario) donde el producto es mas barato. */
    public Supermercado mejorSupermercado(Producto p) {
        Supermercado mejor = null;
        for (Supermercado s : perfil.getSupermercados()) {
            if (p.seVendeEn(s) && (mejor == null || p.precioEn(s) < p.precioEn(mejor))) {
                mejor = s;
            }
        }
        return mejor;
    }

    private double precioEnvase(Producto p) {
        if (perfil.isTengoBasicos() && p.esBasico()) {
            return 0;
        }
        Supermercado s = mejorSupermercado(p);
        if (s == null) {
            return Double.MAX_VALUE / 1000; // no deberia pasar: el filtro ya quita estas recetas
        }
        return p.precioEn(s);
    }

    private int envases(Producto p, double cantidad) {
        if (cantidad <= 0) {
            return 0;
        }
        // el -1e-9 evita que 1000.0000001 g cuente como 2 paquetes por redondeos
        return (int) Math.ceil(cantidad / p.getEnvase() - 1e-9);
    }

    private double cantidadDe(Producto p) {
        Double c = cantidades.get(p.getId());
        if (c == null) {
            return 0;
        }
        return c;
    }
}
