package com.adriancaro.tfgrecetas.datos;

import android.content.Context;

import com.adriancaro.tfgrecetas.modelo.Producto;
import com.adriancaro.tfgrecetas.modelo.Receta;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Carga una sola vez los CSV de la carpeta assets y los guarda en memoria.
 * (Singleton: todas las pantallas usan la misma instancia.)
 */
public class RepositorioDatos {

    private static RepositorioDatos instancia;

    private final List<Producto> productos;
    private final List<Receta> recetas;

    private RepositorioDatos(Context context) throws IOException {
        productos = CargadorDatos.cargarProductos(abrir(context, "productos.csv"));
        // Los precios reales comprobados tienen prioridad sobre los estimados
        CargadorDatos.aplicarPreciosReales(abrir(context, "precios_reales.csv"), productos);
        recetas = CargadorDatos.cargarRecetas(abrir(context, "recetas.csv"),
                abrir(context, "ingredientes.csv"), productos);
    }

    public static synchronized RepositorioDatos get(Context context) {
        if (instancia == null) {
            try {
                instancia = new RepositorioDatos(context.getApplicationContext());
            } catch (IOException e) {
                throw new RuntimeException("No se han podido leer los datos de la app", e);
            }
        }
        return instancia;
    }

    public List<Producto> getProductos() {
        return productos;
    }

    public List<Receta> getRecetas() {
        return recetas;
    }

    public Receta buscarReceta(int id) {
        for (Receta r : recetas) {
            if (r.getId() == id) {
                return r;
            }
        }
        return null;
    }

    private static BufferedReader abrir(Context context, String fichero) throws IOException {
        return new BufferedReader(new InputStreamReader(context.getAssets().open(fichero), StandardCharsets.UTF_8));
    }
}
