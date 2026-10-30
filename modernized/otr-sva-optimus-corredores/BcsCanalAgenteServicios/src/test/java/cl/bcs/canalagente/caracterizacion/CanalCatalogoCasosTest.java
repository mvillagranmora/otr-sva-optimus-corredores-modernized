package cl.bcs.canalagente.caracterizacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/** The case matrix itself: sizes, families, the rules and defects every case cites (reglas.json). Nothing here needs the production classes. */
class CanalCatalogoCasosTest {

    private static JsonObject reglas() {
        return JsonParser.parseString(CasoCanal.texto("reglas.json")).getAsJsonObject();
    }

    @Test
    @DisplayName("La matriz tiene los 88 casos de las 3 familias, todos con golden")
    void tamano_y_familias() {
        Map<String, Integer> porFamilia = new TreeMap<>();
        for (String id : CasoCanal.casos()) porFamilia.merge(id.split("-")[1], 1, Integer::sum);
        assertEquals(Map.of("CAL", 19, "LOG", 56, "PRC", 13), porFamilia);
        assertEquals(88, CasoCanal.casos().size());
    }

    @Test
    @DisplayName("Cada regla o defecto que cita un caso esta en reglas.json y cada uno de reglas.json lo cita algun caso (salvo D-CAN-14, D-CAN-16 y D-CAN-17, solo texto o documentacion)")
    void referencias_cerradas() {
        JsonObject r = reglas();
        Set<String> conocidos = new TreeSet<>();
        r.getAsJsonObject("reglas").keySet().forEach(conocidos::add);
        r.getAsJsonObject("defectos").keySet().forEach(conocidos::add);
        Set<String> citados = new TreeSet<>();
        for (String id : CasoCanal.casos()) citados.addAll(CasoCanal.refs(id));
        List<String> desconocidos = new ArrayList<>(citados);
        desconocidos.removeAll(conocidos);
        assertTrue(desconocidos.isEmpty(), "cited ids that are not in reglas.json: " + desconocidos);
        Set<String> sinCaso = new TreeSet<>(conocidos);
        sinCaso.removeAll(citados);
        assertEquals(Set.of("D-CAN-14", "D-CAN-16", "D-CAN-17"), sinCaso);
        Set<String> declarados = new TreeSet<>();
        r.getAsJsonArray("sinCaso").forEach(e -> declarados.add(e.getAsString()));
        assertEquals(declarados, sinCaso);
    }

    @Test
    @DisplayName("Todo caso cita al menos una regla o defecto")
    void todo_caso_cita_una_regla() {
        for (String id : CasoCanal.casos()) assertTrue(!CasoCanal.refs(id).isEmpty(), id);
    }

    @Test
    @DisplayName("Los casos en los que el legacy lanza excepcion son una parte propia de la matriz (rutas de error cubiertas)")
    void rutas_de_error() {
        long conExcepcion = CasoCanal.casos().stream().filter(id -> CasoCanal.golden(id).has("excepcion")).count();
        assertTrue(conExcepcion >= 20, "cases in which the legacy throws: " + conExcepcion);
        long conSalida = CasoCanal.casos().stream().filter(id -> CasoCanal.golden(id).has("salida")).count();
        assertTrue(conSalida >= 60, "cases with output: " + conSalida);
        assertTrue(CasoCanal.golden(CasoCanal.id("CAN-LOG-", "portafolio-inexistente-lanza-nullpointer")).get("excepcion").getAsString().startsWith("NullPointerException"));
    }
}
