package com.adriancaro.tfgrecetas.datos;

import com.adriancaro.tfgrecetas.modelo.Ingrediente;
import com.adriancaro.tfgrecetas.modelo.Producto;
import com.adriancaro.tfgrecetas.modelo.Receta;
import com.adriancaro.tfgrecetas.modelo.Supermercado;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lee los CSV de productos, recetas e ingredientes.
 * No usa nada de Android, asi se puede probar en el ordenador con un test normal.
 * Formato: separador ';', lineas que empiezan por '#' son comentarios,
 * la primera linea que no es comentario es la cabecera.
 */
public class CargadorDatos {

    public static List<Producto> cargarProductos(BufferedReader lector) throws IOException {
        List<Producto> productos = new ArrayList<Producto>();
        List<String[]> filas = leerFilas(lector);
        int numSupers = Supermercado.values().length;

        for (String[] c : filas) {
            // id;nombre;categoria;unidad;envase;6 precios;alergenos;origen;kcal;proteinas;hidratos;grasas
            int id = Integer.parseInt(c[0].trim());
            String nombre = c[1].trim();
            String categoria = c[2].trim();
            String unidad = c[3].trim();
            double envase = Double.parseDouble(c[4].trim());

            // precios: celda vacia = ese supermercado no lo vende
            double[] precios = new double[numSupers];
            for (int i = 0; i < numSupers; i++) {
                String celda = c[5 + i].trim();
                if (celda.isEmpty()) {
                    precios[i] = -1;
                } else {
                    precios[i] = Double.parseDouble(celda);
                }
            }

            String textoAlergenos = c[5 + numSupers].trim();
            List<String> alergenos = new ArrayList<String>();
            if (!textoAlergenos.isEmpty()) {
                alergenos.addAll(Arrays.asList(textoAlergenos.split("\\|")));
            }
            String origen = c[6 + numSupers].trim();

            Producto producto = new Producto(id, nombre, categoria, unidad, envase, precios, alergenos, origen);
            // Columnas opcionales de nutricion: kcal;proteinas;hidratos;grasas
            int n = 7 + numSupers;
            if (c.length >= n + 4) {
                producto.setNutrientes(Double.parseDouble(c[n].trim()), Double.parseDouble(c[n + 1].trim()),
                        Double.parseDouble(c[n + 2].trim()), Double.parseDouble(c[n + 3].trim()));
            }
            productos.add(producto);
        }
        return productos;
    }

    public static List<Receta> cargarRecetas(BufferedReader lectorRecetas, BufferedReader lectorIngredientes,
                                             List<Producto> productos) throws IOException {
        // Mapa id -> producto para buscar rapido
        Map<Integer, Producto> mapaProductos = new HashMap<Integer, Producto>();
        for (Producto p : productos) {
            mapaProductos.put(p.getId(), p);
        }

        // 1. Recetas: id;nombre;tipo;minutos;pasos
        List<Receta> recetas = new ArrayList<Receta>();
        Map<Integer, Receta> mapaRecetas = new HashMap<Integer, Receta>();
        for (String[] c : leerFilas(lectorRecetas)) {
            int id = Integer.parseInt(c[0].trim());
            List<String> pasos = Arrays.asList(c[4].trim().split("\\|"));
            Receta r = new Receta(id, c[1].trim(), c[2].trim(), Integer.parseInt(c[3].trim()), pasos);
            recetas.add(r);
            mapaRecetas.put(id, r);
        }

        // 2. Ingredientes: idReceta;idProducto;cantidad
        for (String[] c : leerFilas(lectorIngredientes)) {
            Receta r = mapaRecetas.get(Integer.parseInt(c[0].trim()));
            Producto p = mapaProductos.get(Integer.parseInt(c[1].trim()));
            if (r == null || p == null) {
                throw new IOException("Ingrediente con receta o producto que no existe: " + c[0] + ";" + c[1]);
            }
            r.anadirIngrediente(new Ingrediente(p, Double.parseDouble(c[2].trim())));
        }
        return recetas;
    }

    /** Devuelve las filas de datos ya separadas, sin comentarios ni cabecera. */
    private static List<String[]> leerFilas(BufferedReader lector) throws IOException {
        List<String[]> filas = new ArrayList<String[]>();
        boolean cabeceraLeida = false;
        String linea;
        while ((linea = lector.readLine()) != null) {
            if (linea.trim().isEmpty() || linea.startsWith("#")) {
                continue;
            }
            if (!cabeceraLeida) {
                cabeceraLeida = true;
                continue;
            }
            filas.add(linea.split(";", -1));
        }
        return filas;
    }
}
