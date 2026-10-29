package cl.bcs.cuentainversion.reportes.support;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Comparison helpers of the pending (not yet compiled) golden tests: collaborator call log and generated files.
 * A missing recorded section FAILS the assertion; it is never skipped.
 */
public final class PendienteSupport {

    private PendienteSupport() {
    }

    /** The modern service must call its collaborators exactly like the legacy class did (service names, order and requests). */
    public static void assertLlamadas(FakeColaboradores fake, CasoReporte caso) {
        JsonElement esperado = caso.esperado().get("llamadasColaboradores");
        assertThat(esperado).as(caso.nombre() + ": expected.json has no llamadasColaboradores (re-record)").isNotNull();
        JsonArray actual = new JsonArray();
        List<JsonObject> l = fake.llamadas();
        l.forEach(actual::add);
        JsonCanon.assertEquivalente(caso.nombre() + " llamadasColaboradores", esperado, actual, caso.sinOrden());
    }

    /** The files written under the run's output directory must equal the recorded ones (names and contents, wall clock masked). */
    public static void assertArchivos(CasoReporte caso) {
        JsonObject actual;
        try {
            actual = ArchivoDump.directorio(caso.directorioSalida());
        } catch (Exception e) {
            throw new AssertionError("cannot read the generated files of " + caso.nombre(), e);
        }
        JsonElement esperado = caso.esperado().has("archivos") ? caso.esperado().get("archivos") : new JsonObject();
        JsonCanon.assertEquivalente(caso.nombre() + " archivos generados", esperado, actual);
    }

    public static Set<String> nombresArchivos(CasoReporte caso) {
        try {
            return ArchivoDump.directorio(caso.directorioSalida()).keySet();
        } catch (Exception e) {
            throw new AssertionError("cannot read the generated files of " + caso.nombre(), e);
        }
    }
}
