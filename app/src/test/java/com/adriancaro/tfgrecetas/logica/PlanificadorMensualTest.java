package com.adriancaro.tfgrecetas.logica;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.adriancaro.tfgrecetas.datos.CargadorDatos;
import com.adriancaro.tfgrecetas.modelo.DiaPlan;
import com.adriancaro.tfgrecetas.modelo.Ingrediente;
import com.adriancaro.tfgrecetas.modelo.LineaCompra;
import com.adriancaro.tfgrecetas.modelo.PerfilUsuario;
import com.adriancaro.tfgrecetas.modelo.PlanMensual;
import com.adriancaro.tfgrecetas.modelo.Producto;
import com.adriancaro.tfgrecetas.modelo.Receta;
import com.adriancaro.tfgrecetas.modelo.Restriccion;
import com.adriancaro.tfgrecetas.modelo.Supermercado;

import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Tests de la logica (se ejecutan en el ordenador, sin emulador).
 * Leen los mismos CSV que usa la app desde src/main/assets.
 */
public class PlanificadorMensualTest {

    private static final String ASSETS = "src/main/assets/";
    private List<Receta> recetas;

    @Before
    public void cargarDatos() throws Exception {
        List<Producto> productos = CargadorDatos.cargarProductos(abrir("productos.csv"));
        recetas = CargadorDatos.cargarRecetas(abrir("recetas.csv"), abrir("ingredientes.csv"), productos);
    }

    @Test
    public void cargaTodosLosDatos() {
        assertTrue(recetas.size() >= 30);
        for (Receta r : recetas) {
            assertFalse("Receta sin ingredientes: " + r.getNombre(), r.getIngredientes().isEmpty());
        }
    }

    @Test
    public void nuncaSePasaDelPresupuesto() {
        double[] presupuestos = {60, 90, 120, 200, 400};
        for (double presupuesto : presupuestos) {
            for (int personas = 1; personas <= 4; personas++) {
                PerfilUsuario perfil = perfil(presupuesto, personas, Supermercado.MERCADONA, Supermercado.LIDL);
                PlanMensual plan = PlanificadorMensual.generar(perfil, recetas);
                assertTrue("Se pasa con " + presupuesto + " euros y " + personas + " personas",
                        plan.getCosteTotal() <= presupuesto + 0.001);
            }
        }
    }

    @Test
    public void conPresupuestoHolgadoElPlanEstaCompleto() {
        PerfilUsuario perfil = perfil(200, 1, Supermercado.DIA);
        PlanMensual plan = PlanificadorMensual.generar(perfil, recetas);
        assertTrue(plan.isCompleto());
        assertEquals(30, plan.getDias().size());
        for (DiaPlan dia : plan.getDias()) {
            assertTrue(dia.getComida() != null && dia.getCena() != null);
        }
    }

    @Test
    public void conPresupuestoCortoAvisaYDiceCuantoHaria() {
        PerfilUsuario perfil = perfil(40, 4, Supermercado.ALDI);
        PlanMensual plan = PlanificadorMensual.generar(perfil, recetas);
        assertFalse(plan.isCompleto());
        assertTrue(plan.getPresupuestoMinimo() > 40);
        assertFalse(plan.getAviso().isEmpty());
    }

    @Test
    public void unVeganoSoloRecibeRecetasVeganas() {
        PerfilUsuario perfil = perfil(150, 1, Supermercado.values());
        perfil.getRestricciones().add(Restriccion.VEGANO);
        PlanMensual plan = PlanificadorMensual.generar(perfil, recetas);
        for (Receta r : recetasDelPlan(plan)) {
            for (Ingrediente ing : r.getIngredientes()) {
                assertEquals(r.getNombre(), "VEGETAL", ing.getProducto().getOrigen());
            }
        }
    }

    @Test
    public void unCeliacoNoRecibeGluten() {
        PerfilUsuario perfil = perfil(150, 1, Supermercado.values());
        perfil.getRestricciones().add(Restriccion.SIN_GLUTEN);
        PlanMensual plan = PlanificadorMensual.generar(perfil, recetas);
        for (Receta r : recetasDelPlan(plan)) {
            for (Ingrediente ing : r.getIngredientes()) {
                assertFalse(r.getNombre(), ing.getProducto().tieneAlergeno("GLUTEN"));
            }
        }
    }

    @Test
    public void laListaDeLaCompraCuadraConElCosteYUsaSusSupers() {
        PerfilUsuario perfil = perfil(150, 2, Supermercado.MERCADONA, Supermercado.ALCAMPO);
        PlanMensual plan = PlanificadorMensual.generar(perfil, recetas);
        double suma = 0;
        for (LineaCompra linea : plan.getListaCompra()) {
            suma += linea.getPrecioTotal();
            assertTrue(perfil.getSupermercados().contains(linea.getSupermercado()));
            // los envases comprados cubren lo que hace falta
            assertTrue(linea.getEnvases() * linea.getProducto().getEnvase() >= linea.getCantidadNecesaria() - 0.001);
        }
        assertEquals(plan.getCosteTotal(), suma, 0.001);
    }

    @Test
    public void siYaTieneBasicosNoSeLosCobramos() {
        PerfilUsuario perfil = perfil(150, 1, Supermercado.LIDL);
        perfil.setTengoBasicos(true);
        PlanMensual plan = PlanificadorMensual.generar(perfil, recetas);
        for (LineaCompra linea : plan.getListaCompra()) {
            assertFalse(linea.getProducto().esBasico());
        }
    }

    // ---------- utilidades ----------

    private PerfilUsuario perfil(double presupuesto, int personas, Supermercado... supers) {
        PerfilUsuario perfil = new PerfilUsuario();
        List<Supermercado> lista = new ArrayList<Supermercado>();
        for (Supermercado s : supers) {
            lista.add(s);
        }
        perfil.setSupermercados(lista);
        perfil.setPresupuesto(presupuesto);
        perfil.setPersonas(personas);
        return perfil;
    }

    private List<Receta> recetasDelPlan(PlanMensual plan) {
        List<Receta> lista = new ArrayList<Receta>();
        for (DiaPlan dia : plan.getDias()) {
            if (dia.getComida() != null) {
                lista.add(dia.getComida());
            }
            if (dia.getCena() != null) {
                lista.add(dia.getCena());
            }
        }
        return lista;
    }

    private BufferedReader abrir(String fichero) throws Exception {
        return new BufferedReader(new InputStreamReader(new FileInputStream(ASSETS + fichero), "UTF-8"));
    }
}
