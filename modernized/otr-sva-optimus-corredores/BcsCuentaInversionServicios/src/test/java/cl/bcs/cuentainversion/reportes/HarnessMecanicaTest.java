package cl.bcs.cuentainversion.reportes;

import cl.bcs.cuentainversion.reportes.support.CasoReporte;
import cl.bcs.cuentainversion.reportes.support.CasoReporte.Resultado;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Proves the comparison harness itself can fail: a stub that replays the RECORDED legacy output passes on every case where the
 * legacy changed no table, and the same stub with one altered field is rejected. Without this, a green suite could hide a
 * comparison that compares nothing.
 */
class HarnessMecanicaTest {

    private static boolean sinCambios(JsonObject esp) {
        return !esp.has("excepcion") && esp.get("salida").isJsonObject() && esp.getAsJsonObject("tablas").size() == 0;
    }

    @Test
    void replayingTheRecordedOutputPassesAndAlteringItFails() {
        int reproducidos = 0;
        for (String nombre : CasoReporte.todos()) {
            CasoReporte caso = CasoReporte.cargar(nombre);
            JsonObject esp = caso.esperado();
            if (!sinCambios(esp)) {
                continue;
            }
            JsonObject grabado = esp.getAsJsonObject("salida");
            String out = caso.directorioSalida().toString().replace('\\', '/');
            JsonObject replay = com.google.gson.JsonParser.parseString(grabado.toString().replace("${OUT}", out)).getAsJsonObject();
            Resultado ok = caso.ejecutar(in -> replay);
            caso.assertEquivalente(ok);

            JsonObject alterada = replay.deepCopy();
            alterada.addProperty("CAMPO_ALTERADO", "x");
            Resultado mal = caso.ejecutar(in -> alterada);
            assertThatThrownBy(() -> caso.assertEquivalente(mal)).as(nombre + ": an altered output must be rejected").isInstanceOf(AssertionError.class);
            reproducidos++;
        }
        System.out.println("harness replay cases executed: " + reproducidos);
        assertThat(reproducidos).as("harness replay cases executed").isGreaterThan(10);
    }

    @Test
    void anExceptionCaseRejectsANormalReturnAndAcceptsTheSameExceptionClass() {
        List<String> conExcepcion = CasoReporte.todos().stream().filter(n -> CasoReporte.cargar(n).esperado().has("excepcion")).toList();
        assertThat(conExcepcion).as("recorded exception cases").isNotEmpty();
        for (String nombre : conExcepcion) {
            CasoReporte caso = CasoReporte.cargar(nombre);
            Resultado normal = caso.ejecutar(in -> new JsonObject());
            assertThatThrownBy(() -> caso.assertEquivalente(normal)).as(nombre).isInstanceOf(AssertionError.class);
        }
    }
}
