package com.adriancaro.tfgrecetas.logica;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.adriancaro.tfgrecetas.datos.CargadorDatos;
import com.adriancaro.tfgrecetas.modelo.DiaPlan;
import com.adriancaro.tfgrecetas.modelo.Ingrediente;
import com.adriancaro.tfgrecetas.modelo.LineaCompra;
import com.adriancaro.tfgrecetas.modelo.Momento;
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
    private List<Producto> productos;

    @Before
    public void cargarDatos() throws Exception {
        List<Producto> productos = CargadorDatos.cargarProductos(abrir("productos.csv"));
        CargadorDatos.aplicarPreciosReales(abrir("precios_reales.csv"), productos);
        this.productos = productos;
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

    @Test
    public void sinMarcarBasicosSiAparecenEnLaLista() {
        PerfilUsuario perfil = perfil(150, 1, Supermercado.LIDL);
        PlanMensual plan = PlanificadorMensual.generar(perfil, recetas);
        boolean hayBasicos = false;
        for (LineaCompra linea : plan.getListaCompra()) {
            hayBasicos |= linea.getProducto().esBasico();
        }
        assertTrue(hayBasicos);
    }

    @Test
    public void elCosteMinimoNoSuperaAlDeUnPlanNormal() {
        PerfilUsuario perfil = perfil(300, 2, Supermercado.MERCADONA, Supermercado.DIA);
        double minimo = PlanificadorMensual.costeMinimo(perfil, recetas);
        PlanMensual plan = PlanificadorMensual.generar(perfil, recetas);
        assertTrue(minimo > 0);
        assertTrue(minimo <= plan.getCosteTotal() + 0.001);
    }

    @Test
    public void rehacerElPlanDesdeSusDiasDaElMismoCoste() {
        PerfilUsuario perfil = perfil(150, 2, Supermercado.MERCADONA, Supermercado.LIDL);
        PlanMensual plan = PlanificadorMensual.generar(perfil, recetas);
        PlanMensual rehecho = PlanificadorMensual.desdeDias(perfil, plan.getDias(), recetas);
        assertEquals(plan.getCosteTotal(), rehecho.getCosteTotal(), 0.001);
        assertTrue(rehecho.isCompleto());
    }

    @Test
    public void cambiarUnPlatoNoSePasaDelPresupuesto() {
        PerfilUsuario perfil = perfil(110, 1, Supermercado.MERCADONA, Supermercado.LIDL);
        PlanMensual plan = PlanificadorMensual.generar(perfil, recetas);
        List<PlanificadorMensual.Alternativa> alternativas =
                PlanificadorMensual.alternativas(perfil, plan, 4, Momento.CENA, recetas);
        assertFalse(alternativas.isEmpty());

        for (PlanificadorMensual.Alternativa alt : alternativas) {
            assertTrue(alt.getReceta().sirvePara(Momento.CENA));
            assertTrue(alt.getReceta() != plan.getDias().get(4).getCena());
            // Aplicar el cambio y comprobar que el coste coincide y no se pasa
            List<DiaPlan> dias = new ArrayList<DiaPlan>();
            for (DiaPlan d : plan.getDias()) {
                dias.add(d.copiar());
            }
            dias.get(4).setReceta(Momento.CENA, alt.getReceta());
            PlanMensual cambiado = PlanificadorMensual.desdeDias(perfil, dias, recetas);
            assertEquals(plan.getCosteTotal() + alt.getDiferencia(), cambiado.getCosteTotal(), 0.001);
            assertTrue(cambiado.getCosteTotal() <= perfil.getPresupuesto() + 0.001);
        }
    }

    @Test
    public void elCostePorRacionEsPositivoYMenorQueUnEnvaseEntero() {
        PerfilUsuario perfil = perfil(150, 1, Supermercado.MERCADONA);
        CestaCompra cesta = new CestaCompra(perfil);
        for (Receta r : recetas) {
            double racion = cesta.costeRacion(r);
            assertTrue(r.getNombre(), racion > 0 && racion < 10);
        }
    }

    @Test
    public void todasLasRecetasTienenCaloriasRazonables() {
        for (Receta r : recetas) {
            Nutricion n = Nutricion.deRacion(r);
            assertTrue(r.getNombre() + ": " + n.getKcal() + " kcal", n.getKcal() > 150 && n.getKcal() < 1200);
            assertTrue(r.getNombre(), n.getProteinas() > 0 && n.getPesoGramos() > 100);
        }
    }

    @Test
    public void lasCaloriasSeCalculanPorUnidadYPor100Gramos() {
        Producto huevo = null;
        Producto arroz = null;
        for (Receta r : recetas) {
            for (Ingrediente ing : r.getIngredientes()) {
                if (ing.getProducto().getNombre().equals("Huevos")) huevo = ing.getProducto();
                if (ing.getProducto().getNombre().equals("Arroz redondo")) arroz = ing.getProducto();
            }
        }
        assertEquals(172, huevo.nutrientesDe(2)[0], 0.01);   // 2 huevos
        assertEquals(283.2, arroz.nutrientesDe(80)[0], 0.01); // 80 g de arroz
    }

    @Test
    public void lasDietasMedicasDanUnMesCompletoYSeRespetan() {
        Restriccion[] dietas = {Restriccion.DIETA_BLANDA, Restriccion.BAJA_EN_SAL,
                Restriccion.CONTROL_AZUCAR, Restriccion.BAJA_EN_GRASA};
        for (Restriccion dieta : dietas) {
            PerfilUsuario perfil = perfil(250, 1, Supermercado.values());
            perfil.getRestricciones().add(dieta);
            PlanMensual plan = PlanificadorMensual.generar(perfil, recetas);
            assertTrue(dieta + ": " + plan.getAviso(), plan.isCompleto());
            for (Receta r : recetasDelPlan(plan)) {
                Nutricion n = Nutricion.deRacion(r);
                for (Ingrediente ing : r.getIngredientes()) {
                    assertTrue(dieta + " / " + r.getNombre(), dieta.permite(ing.getProducto()));
                }
                if (dieta == Restriccion.CONTROL_AZUCAR) {
                    assertTrue(r.getNombre(), n.getHidratos() <= Restriccion.MAX_HIDRATOS_DIABETES);
                }
                if (dieta == Restriccion.DIETA_BLANDA || dieta == Restriccion.BAJA_EN_GRASA) {
                    assertTrue(r.getNombre(), n.getGrasas() <= Restriccion.MAX_GRASAS);
                }
            }
        }
    }

    @Test
    public void conUnSoloSupermercadoTodaLaCompraEsEnElMasBarato() {
        PerfilUsuario perfil = perfil(200, 2, Supermercado.MERCADONA, Supermercado.DIA, Supermercado.ALCAMPO);
        perfil.setUnSoloSupermercado(true);
        PlanMensual plan = PlanificadorMensual.generar(perfil, recetas);
        assertTrue(plan.isCompleto());
        Supermercado elegido = plan.getSupermercado();
        // La compra de este plan no es mas cara en el elegido que en los otros (margen del 3 %)
        java.util.Map<Supermercado, Double> costes = PlanificadorMensual.compararSupermercados(perfil, plan.getDias());
        for (double coste : costes.values()) {
            assertTrue(elegido + " " + costes, costes.get(elegido) <= coste * 1.03);
        }
        for (LineaCompra linea : plan.getListaCompra()) {
            assertEquals(elegido, linea.getSupermercado());
        }
    }

    @Test
    public void laDietaBlandaNoLlevaPicanteNiLegumbres() {
        PerfilUsuario perfil = perfil(250, 1, Supermercado.values());
        perfil.getRestricciones().add(Restriccion.DIETA_BLANDA);
        for (Receta r : FiltroRecetas.recetasValidas(recetas, perfil)) {
            for (Ingrediente ing : r.getIngredientes()) {
                String nombre = ing.getProducto().getNombre();
                assertFalse(r.getNombre(), nombre.startsWith("Pimentón") || nombre.startsWith("Chorizo")
                        || nombre.startsWith("Lentejas") || nombre.startsWith("Garbanzos"));
            }
        }
    }

    @Test
    public void laComparacionEmpiezaPorElSuperElegidoYCuadraConElCoste() {
        PerfilUsuario perfil = perfil(200, 1, Supermercado.MERCADONA, Supermercado.CARREFOUR, Supermercado.ALCAMPO);
        perfil.setUnSoloSupermercado(true);
        PlanMensual plan = PlanificadorMensual.generar(perfil, recetas);
        java.util.Map<Supermercado, Double> comparacion =
                PlanificadorMensual.compararSupermercados(perfil, plan.getDias());
        assertEquals(plan.getCosteTotal(), comparacion.get(plan.getSupermercado()), 0.001);
        double anterior = 0;
        for (double coste : comparacion.values()) {
            assertTrue(coste >= anterior - 0.001); // ordenado de mas barato a mas caro
            anterior = coste;
        }
    }

    @Test
    public void ningunSuperGanaEnTodosLosProductos() {
        // Antes Lidl era el mas barato en los 42 productos y elegir supermercados no servia de nada
        java.util.Set<Supermercado> ganadores = new java.util.HashSet<Supermercado>();
        java.util.Set<Producto> vistos = new java.util.HashSet<Producto>();
        PerfilUsuario todos = perfil(200, 1, Supermercado.values());
        CestaCompra cesta = new CestaCompra(todos);
        for (Receta r : recetas) {
            for (Ingrediente ing : r.getIngredientes()) {
                if (vistos.add(ing.getProducto())) {
                    ganadores.add(cesta.mejorSupermercado(ing.getProducto()));
                }
            }
        }
        assertTrue("Ganadores: " + ganadores, ganadores.size() >= 2);
    }

    @Test
    public void losPreciosRealesSustituyenALosEstimadosSegunElEnvase() {
        Producto pechuga = producto("Pechuga de pollo");   // envase de 1000 g
        Producto cuscus = producto("Cuscús");               // envase de 500 g
        Producto huevos = producto("Huevos");               // envase de 12 ud
        assertEquals(8.89, pechuga.precioEn(Supermercado.LIDL), 0.001);
        assertEquals(0.99, cuscus.precioEn(Supermercado.ALCAMPO), 0.001); // 1,98 EUR/kg x 0,5 kg
        assertEquals(2.64, huevos.precioEn(Supermercado.CARREFOUR), 0.01); // 0,22 EUR/ud x 12
        assertTrue(pechuga.esPrecioVerificado(Supermercado.LIDL));
        assertFalse(pechuga.esPrecioVerificado(Supermercado.ALDI));
    }

    private Producto producto(String nombre) {
        for (Producto p : productos) {
            if (p.getNombre().equals(nombre)) {
                return p;
            }
        }
        throw new AssertionError("No existe " + nombre);
    }

    // ---------- utilidades ----------

    private PerfilUsuario perfil(double presupuesto, int personas, Supermercado... supers) {
        PerfilUsuario perfil = new PerfilUsuario();
        List<Supermercado> lista = new ArrayList<Supermercado>();
        for (Supermercado s : supers) {
            lista.add(s);
        }
        perfil.setSupermercados(lista);
        perfil.setUnSoloSupermercado(false); // los tests clasicos reparten la compra
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
