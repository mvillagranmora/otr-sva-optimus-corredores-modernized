package cl.bcs.apv.adapter.javacall;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.apv.adapter.javacall.ApvJavaCallRegistro.OperacionRegistrada;
import cl.bcs.apv.adapter.javacall.mapeo.ModificarBonificacionMapeo;
import cl.bcs.apv.api.casouso.CasosUso;
import cl.bcs.apv.api.casouso.ModificarBonificacion;
import cl.bcs.apv.api.comun.Mensaje;
import cl.bcs.apv.api.dto.modificarbonificacion.ModificarBonificacionEntrada;
import cl.bcs.apv.api.dto.modificarbonificacion.ModificarBonificacionResultado;

/** Smoke of the registry and of every generated mapper (the real equivalence is proven by the characterization goldens in BcsAPVServicios). */
class ApvJavaCallRegistroTest {

    private final ApvJavaCallRegistro registro = new ApvJavaCallRegistro();

    @Test
    void hayUnaEntradaPorCadaClaseJavaCallLegacy() throws IOException {
        Set<String> esperados = new TreeSet<>();
        try (var in = getClass().getResourceAsStream("/legacy-fqns.txt")) {
            for (String l : new String(in.readAllBytes(), StandardCharsets.UTF_8).split("\\R")) {
                if (!l.isBlank()) {
                    esperados.add(l.trim());
                }
            }
        }
        Set<String> registrados = new TreeSet<>();
        registro.todas().forEach(o -> assertTrue(registrados.add(o.claseLegacy()), "duplicada " + o.claseLegacy()));
        assertEquals(66, esperados.size());
        assertEquals(esperados, registrados);
        assertEquals(64, registro.todas().stream().filter(OperacionRegistrada::invocable).count());
        assertEquals(CasosUso.TODOS.size(), registro.todas().stream().map(OperacionRegistrada::interfaz).filter(i -> i != null).distinct().count());
        Set<String> rutas = new HashSet<>();
        registro.todas().stream().filter(OperacionRegistrada::invocable)
                .forEach(o -> assertTrue(rutas.add(o.verbo() + " " + o.ruta()), "ruta repetida " + o.verbo() + " " + o.ruta()));
        assertTrue(registro.buscar("GrabarContratoSAAPV").isPresent());
        assertTrue(registro.buscar("cl.bcs.APV.GrabarContratoSAAPV").isPresent());
        assertFalse(registro.buscar("cl.bcs.APV.NoExiste").isPresent());
        assertFalse(registro.buscar("ValidarBancoPrevired").orElseThrow().invocable());
        assertFalse(registro.buscar("ValidarMandatosPAC").orElseThrow().invocable());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    void todoMapeadorHaceViajeDeIdaYVueltaConLaEntradaYElResultadoPorDefecto() {
        for (OperacionRegistrada op : registro.todas()) {
            if (!op.invocable()) {
                continue;
            }
            MapeoOperacion m = op.mapeo();
            Object e0 = m.entradaDesdeJson(new JsonObject());
            JsonObject j1 = m.entradaAJson(e0);
            Object e1 = m.entradaDesdeJson(j1);
            assertEquals(e0, e1, op.claseLegacy() + " entrada");
            assertEquals(j1, m.entradaAJson(e1), op.claseLegacy() + " entrada json");
            Object r0 = m.resultadoDesdeJson(new JsonObject());
            JsonObject k1 = m.resultadoAJson(r0);
            Object r1 = m.resultadoDesdeJson(k1);
            assertEquals(r0, r1, op.claseLegacy() + " resultado");
            assertEquals(k1, m.resultadoAJson(r1), op.claseLegacy() + " resultado json");
        }
    }

    @Test
    void reglasDeMapeoClaveAusenteNulaYPorDefecto() {
        var m = ModificarBonificacionMapeo.INSTANCIA;
        // key missing: legacy constructor default (empty list), scalars null
        ModificarBonificacionEntrada vacia = m.entradaDesdeJson(new JsonObject());
        assertEquals(List.of(), vacia.parametros());
        assertNull(vacia.idEmpresa());
        // explicit null: null (Gson assigns null over the constructor default)
        ModificarBonificacionEntrada nula = m.entradaDesdeJson(JsonParser.parseString("{\"p_Parametros\":null}").getAsJsonObject());
        assertNull(nula.parametros());
        // content, camelCase typed names, BigDecimal kept, unknown keys ignored
        ModificarBonificacionEntrada e = m.entradaDesdeJson(JsonParser.parseString(
                "{\"p_IdEmpresa\":1,\"p_IdUsuario\":17,\"p_IdModulo\":\"APV\",\"p_Parametros\":[{\"IdBonificacion\":210,\"MontoABonificar\":100.50}],\"otro\":1}")
                .getAsJsonObject());
        assertEquals(1L, e.idEmpresa());
        assertEquals("APV", e.idModulo());
        assertEquals(1, e.parametros().size());
        assertEquals(210L, e.parametros().get(0).idBonificacion());
        assertEquals(new java.math.BigDecimal("100.50"), e.parametros().get(0).montoABonificar());
        // plain Gson output: null members omitted (the error counter is absent), lists always present
        JsonObject salida = m.resultadoAJson(ModificarBonificacionResultado.porDefecto());
        assertEquals("{\"P_OUT_MENSAJES\":[]}", salida.toString());
    }

    @Test
    void elPipelineEnProcesoVaDeJsonLegacyAJsonLegacy() throws Exception {
        ModificarBonificacion caso = entrada -> new ModificarBonificacionResultado(
                List.of(new Mensaje(1L, "APV", "OK", "Bonificacion Modificada")), 0L);
        ApvJavaCallPipeline p = new ApvJavaCallPipelineEnProceso(registro, Map.of(ModificarBonificacion.class, caso));
        JsonObject salida = p.invocar("cl.bcs.APV.ModificarBonificacion", new JsonObject());
        assertEquals("{\"P_OUT_MENSAJES\":[{\"id\":1,\"tipo\":\"APV\",\"codigo\":\"OK\",\"descripcion\":\"Bonificacion Modificada\"}],\"P_OUT_ERRORESCONTADOR\":0}",
                salida.toString());
        assertNull(p.invocar("cl.bcs.APV.ValidarBancoPrevired", new JsonObject()));
        assertThrows(IllegalArgumentException.class, () -> p.invocar("cl.bcs.APV.NoExiste", new JsonObject()));
        assertThrows(IllegalStateException.class, () -> p.invocar("cl.bcs.APV.CrearContratoSAAPV", new JsonObject()));
    }
}
