package com.adriancaro.tfgrecetas.logica;

import static org.junit.Assert.assertEquals;

import com.adriancaro.tfgrecetas.modelo.Ingrediente;
import com.adriancaro.tfgrecetas.modelo.Producto;
import com.adriancaro.tfgrecetas.modelo.Receta;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ComposicionTest {

    @Test
    public void gruposPorCategoria() {
        assertEquals(Composicion.CEREAL, Composicion.grupoDe("Cereales y pasta"));
        assertEquals(Composicion.VERDURA, Composicion.grupoDe("Congelados"));
        assertEquals(Composicion.LEGUMBRE, Composicion.grupoDe("Proteína vegetal"));
        assertEquals(Composicion.PESCADO, Composicion.grupoDe("Marisco"));
        assertEquals(Composicion.DESPENSA, Composicion.grupoDe("Conservas"));
    }

    @Test
    public void fraccionesSumanUnoYLaSalNoSeDibuja() {
        Receta r = new Receta(1, "Prueba", "COMIDA", 10, Arrays.asList("Paso"));
        r.anadirIngrediente(new Ingrediente(producto("Arroz", "Cereales y pasta", "g"), 75));
        r.anadirIngrediente(new Ingrediente(producto("Huevo", "Huevos", "ud"), 0.5));
        r.anadirIngrediente(new Ingrediente(producto("Sal", "Básicos", "g"), 2));
        List<Composicion.Parte> partes = Composicion.de(r);
        assertEquals(2, partes.size());
        assertEquals(0.75, partes.get(0).getFraccion(), 1e-9);
        assertEquals(Composicion.HUEVO, partes.get(1).getGrupo());
        assertEquals(1.0, partes.get(0).getFraccion() + partes.get(1).getFraccion(), 1e-9);
    }

    private Producto producto(String nombre, String categoria, String unidad) {
        return new Producto(1, nombre, categoria, unidad, 1000, new double[6], new ArrayList<String>(), "VEGETAL");
    }
}
