package cl.bcs.replica.caracterizacion;

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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/** The case matrix itself: sizes, families, the rules and defects every case cites (reglas.json) and the collaborators each case scripts. Nothing here needs the production classes. */
class ReplicaCatalogoCasosTest {

    private static JsonObject reglas() {
        return JsonParser.parseString(CasoReplica.texto("reglas.json")).getAsJsonObject();
    }

    @Test
    @DisplayName("La matriz tiene los 264 casos de las 8 familias, todos con golden")
    void tamano_y_familias() {
        Map<String, Integer> porFamilia = new TreeMap<>();
        for (String id : CasoReplica.casos()) porFamilia.merge(id.split("-")[1], 1, Integer::sum);
        assertEquals(Map.of("CAL", 3, "DIA", 24, "ENV", 106, "INH", 15, "INI", 50, "OPT", 21, "POS", 14, "SEB", 31), porFamilia);
        assertEquals(264, CasoReplica.casos().size());
    }

    @Test
    @DisplayName("Cada regla o defecto que cita un caso esta en reglas.json y cada uno de reglas.json lo cita algun caso (salvo D-REP-22, solo documentado)")
    void referencias_cerradas() {
        JsonObject r = reglas();
        Set<String> conocidos = new TreeSet<>();
        r.getAsJsonObject("reglas").keySet().forEach(conocidos::add);
        r.getAsJsonObject("defectos").keySet().forEach(conocidos::add);
        Set<String> citados = new TreeSet<>();
        for (String id : CasoReplica.casos()) citados.addAll(CasoReplica.refs(id));
        List<String> desconocidos = new ArrayList<>(citados);
        desconocidos.removeAll(conocidos);
        assertTrue(desconocidos.isEmpty(), "cited ids that are not in reglas.json: " + desconocidos);
        Set<String> sinCaso = new TreeSet<>(conocidos);
        sinCaso.removeAll(citados);
        assertEquals(Set.of("D-REP-22"), sinCaso);
    }

    @Test
    @DisplayName("Los casos tienen el nombre de las reglas en el nombre de la prueba: cada caso cita al menos una regla")
    void todo_caso_cita_una_regla() {
        for (String id : CasoReplica.casos()) assertTrue(!CasoReplica.refs(id).isEmpty(), id);
    }

    @Test
    @DisplayName("Los casos del legacy que lanzan excepcion son una parte propia de la matriz (rutas de error cubiertas)")
    void rutas_de_error() {
        long conExcepcion = CasoReplica.casos().stream().filter(id -> CasoReplica.golden(id).has("excepcion")).count();
        assertTrue(conExcepcion >= 90, "cases in which the legacy throws: " + conExcepcion);
        long sinIdem = CasoReplica.casos().stream().filter(id -> CasoReplica.golden(id).get("sentencias").getAsJsonArray().size() == 0).count();
        assertTrue(sinIdem > 20, "cases with no SQL at all: " + sinIdem);
        JsonElement e = CasoReplica.golden(CasoReplica.id("REP-CAL-", "enviar-tabla-replica-call-devuelve-null")).get("salidaNula");
        assertTrue(e.getAsBoolean());
    }
}
