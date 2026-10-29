package cl.bcs.apv.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import cl.bcs.apv.api.casouso.CasosUso;

/** Smoke: every use case interface has the contract {@code Resultado ejecutar(Entrada)} over records that build their legacy defaults. */
class ApvApiSmokeTest {

    @Test
    void hay64CasosDeUsoConFirmaTipada() throws Exception {
        assertEquals(64, CasosUso.TODOS.size());
        Set<String> nombres = new HashSet<>();
        for (Class<?> caso : CasosUso.TODOS) {
            assertTrue(caso.isInterface(), caso.getName());
            assertTrue(nombres.add(caso.getSimpleName()), "duplicado " + caso);
            Method ejecutar = null;
            for (Method m : caso.getDeclaredMethods()) {
                if (m.getName().equals("ejecutar") && !m.isBridge() && !m.isSynthetic()) {
                    ejecutar = m;
                }
            }
            assertNotNull(ejecutar, caso.getName());
            assertEquals(1, ejecutar.getParameterCount(), caso.getName());
            Class<?> entrada = ejecutar.getParameterTypes()[0];
            Class<?> resultado = ejecutar.getReturnType();
            assertTrue(entrada.isRecord() && resultado.isRecord(), caso.getName());
            assertTrue(entrada.getSimpleName().endsWith("Entrada") && resultado.getSimpleName().endsWith("Resultado"), caso.getName());
            assertEquals(entrada, entrada.getMethod("porDefecto").invoke(null).getClass());
            assertEquals(resultado, resultado.getMethod("porDefecto").invoke(null).getClass());
        }
    }
}
