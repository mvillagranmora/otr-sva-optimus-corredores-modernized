package cl.bcs.apv.adapter.javacall;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/** The runtime helpers keep the legacy Gson semantics (coercions, nulls, dates, flavours). */
class MapeoJsonTest {

    private static JsonObject json(String s) {
        return JsonParser.parseString(s).getAsJsonObject();
    }

    @Test
    void escalaresUsanLasCoercionesDeGson() {
        JsonObject o = json("{\"a\":\"12\",\"b\":7,\"c\":1.50,\"d\":null}");
        assertEquals(12L, MapeoJson.escalar(Sabor.PLANO, o, "a", Long.class, 0L));
        assertEquals("7", MapeoJson.escalar(Sabor.PLANO, o, "b", String.class, "x"));
        assertEquals(new BigDecimal("1.50"), MapeoJson.escalar(Sabor.PLANO, o, "c", BigDecimal.class, null));
        assertNull(MapeoJson.escalar(Sabor.PLANO, o, "d", Long.class, 5L));
        assertEquals(5L, MapeoJson.escalar(Sabor.PLANO, o, "ausente", Long.class, 5L));
        assertEquals(5L, MapeoJson.primitivo(Sabor.PLANO, o, "d", Long.class, 5L));
        assertThrows(JsonSyntaxException.class, () -> MapeoJson.escalar(Sabor.PLANO, json("{\"a\":1.5}"), "a", Long.class, 0L));
    }

    @Test
    void listasPorDefectoNulaYConElementosNulos() {
        JsonObject o = json("{\"l\":[1,null,3],\"n\":null}");
        assertEquals(java.util.Arrays.asList(1L, null, 3L), MapeoJson.listaEscalar(Sabor.PLANO, o, "l", Long.class, List.of()));
        assertNull(MapeoJson.listaEscalar(Sabor.PLANO, o, "n", Long.class, List.of()));
        assertEquals(List.of(), MapeoJson.listaEscalar(Sabor.PLANO, o, "x", Long.class, List.of()));
    }

    @Test
    void sabores_nulosSeOmitenOSeEscriben() {
        JsonObject plano = new JsonObject();
        MapeoJson.poner(Sabor.PLANO, plano, "k", null);
        assertEquals("{}", plano.toString());
        JsonObject constructor = new JsonObject();
        MapeoJson.poner(Sabor.CONSTRUCTOR, constructor, "k", null);
        assertEquals("{\"k\":null}", constructor.toString());
    }

    @Test
    void fechaSqlDeLegacyViajaConElFormatoDeGson() {
        JsonObject j = new JsonObject();
        MapeoJson.ponerFechaSql(Sabor.PLANO, j, "f", LocalDate.of(2026, 10, 20));
        assertEquals("{\"f\":\"Oct 20, 2026\"}", j.toString());
        assertEquals(LocalDate.of(2026, 10, 20), MapeoJson.fechaSql(Sabor.PLANO, j, "f", null));
    }
}
