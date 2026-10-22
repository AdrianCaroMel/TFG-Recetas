package com.adriancaro.tfgrecetas.logica;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TemporizadoresTest {

    @Test
    public void minutosSueltos() {
        assertEquals(600, Temporizadores.segundosDe("Cuece la pasta 10 minutos removiendo de vez en cuando."));
        assertEquals(60, Temporizadores.segundosDe("Sofríe 1 minuto."));
    }

    @Test
    public void enUnRangoSeUsaElMinimo() {
        assertEquals(360, Temporizadores.segundosDe("Hornea 6-8 minutos a 200 °C."));
        assertEquals(540, Temporizadores.segundosDe("Deja 9 a 10 minutos a fuego medio."));
    }

    @Test
    public void seQuedaConElTiempoMasLargo() {
        assertEquals(300, Temporizadores.segundosDe("Remueve 30 segundos y luego cuece 5 minutos."));
    }

    @Test
    public void sinTiempoDevuelveCero() {
        assertEquals(0, Temporizadores.segundosDe("Sirve con un chorrito de aceite."));
        assertEquals(0, Temporizadores.segundosDe("Usa 2 huevos y 200 g de harina."));
    }

    @Test
    public void formatoDeCuentaAtras() {
        assertEquals("1:05", Temporizadores.formato(65));
        assertEquals("12:00", Temporizadores.formato(720));
    }
}
