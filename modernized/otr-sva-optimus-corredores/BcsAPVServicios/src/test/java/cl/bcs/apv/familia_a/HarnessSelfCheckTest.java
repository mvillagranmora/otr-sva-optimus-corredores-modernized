package cl.bcs.apv.familia_a;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.apv.familia_a.support.Canon;
import cl.bcs.apv.familia_a.support.Caso;
import cl.bcs.apv.familia_a.support.Dump;
import cl.bcs.apv.familia_a.support.Verifica;

/**
 * Proves the harness itself can fail: it must reject a wrong output, a missing fixture, a wrong collaborator trace and a masked value that differs,
 * and it must ACCEPT the golden itself. Not an equivalence test (its pairs are never written by the equivalence dump).
 */
class HarnessSelfCheckTest {

    /** A case with no table dump and no collaborator calls: its golden is just the output. */
    private static final String CASO_SIMPLE = "APV-A-053_cliente-ausente-no-devuelve-nada";

    @Test
    void canonIgnoraElOrdenDeLasClavesYCompararNumerosPorValor() {
        assertThat(Canon.texto(Canon.parse("{\"a\":1,\"b\":2.0}"))).isEqualTo(Canon.texto(Canon.parse("{\"b\":2,\"a\":1.0}")));
    }

    @Test
    void canonDetectaUnValorDistinto() {
        assertThat(Canon.texto(Canon.parse("{\"a\":1}"))).isNotEqualTo(Canon.texto(Canon.parse("{\"a\":2}")));
    }

    @Test
    void canonRespetaElOrdenDeUnArrayExceptoSiSeDeclaraSinOrden() {
        String a = "{\"x\":[{\"k\":1},{\"k\":2}]}";
        String b = "{\"x\":[{\"k\":2},{\"k\":1}]}";
        assertThat(Canon.texto(Canon.parse(a))).isNotEqualTo(Canon.texto(Canon.parse(b)));
        assertThat(Canon.texto(Canon.parse(a), "x")).isEqualTo(Canon.texto(Canon.parse(b), "x"));
        assertThat(Set.of("x")).isNotEmpty();
    }

    @Test
    void unCasoSinFixtureHaceFallarLaPrueba() {
        assertThatThrownBy(() -> Caso.cargar("APV-A-9999_no-existe")).isInstanceOf(Throwable.class);
    }

    @Test
    void elGoldenMismoSeAceptaComoSalida() {
        Caso caso = Caso.cargar(CASO_SIMPLE);
        JsonElement golden = caso.esperado().get("salida");
        Verifica.comparar(caso, new Verifica.Ejecucion(golden, null), List.of());
    }

    @Test
    void unaSalidaDistintaDelGoldenHaceFallarLaComparacion() {
        Caso caso = Caso.cargar(CASO_SIMPLE);
        JsonObject distinta = new JsonObject();
        distinta.addProperty("salida_inventada", "x");
        assertThatThrownBy(() -> Verifica.comparar(caso, new Verifica.Ejecucion(distinta, null))).isInstanceOf(AssertionError.class);
    }

    @Test
    void unValorDistintoDentroDeLaSalidaHaceFallarLaComparacion() {
        Caso caso = Caso.cargar(CASO_SIMPLE);
        JsonObject copia = caso.esperado().getAsJsonObject("salida").deepCopy();
        copia.getAsJsonArray("P_OUT_MENSAJES").add(JsonParser.parseString("{\"id\":9,\"tipo\":\"ERROR\",\"codigo\":\"X\",\"descripcion\":\"x\"}"));
        assertThatThrownBy(() -> Verifica.comparar(caso, new Verifica.Ejecucion(copia, null))).isInstanceOf(AssertionError.class);
    }

    @Test
    void unaExcepcionDondeElLegacyNoLanzoHaceFallarLaComparacion() {
        Caso caso = Caso.cargar(CASO_SIMPLE);
        assertThatThrownBy(() -> Verifica.comparar(caso, new Verifica.Ejecucion(null, new IllegalStateException("x")))).isInstanceOf(AssertionError.class);
    }

    @Test
    void unaTrazaDeColaboradoresInventadaHaceFallarLaComparacion() {
        Caso caso = Caso.cargar(CASO_SIMPLE);
        JsonObject llamada = new JsonObject();
        llamada.addProperty("servicio", "ServicioInventado");
        assertThatThrownBy(() -> Verifica.colaboradores(caso, List.of(llamada))).isInstanceOf(AssertionError.class);
    }

    @Test
    void laMascaraDeFechasEscondeElDiaPeroNoLaClave() {
        JsonElement e = JsonParser.parseString("{\"x\":[{\"FechaCreacion\":\"2026-10-20 10:11:12.345\",\"Otra\":\"2026-10-20\"}]}");
        JsonElement m = Dump.enmascararClaves(e, Set.of("FechaCreacion"));
        assertThat(m.toString()).contains("\"FechaCreacion\":\"<TS>\"").contains("\"Otra\":\"2026-10-20\"");
    }

    @Test
    void ningunGoldenGuardaLaFechaDelDiaDeLaGrabacion() throws Exception {
        // a golden that carries the run day makes the suite fail the day after it was recorded: the masks must have removed it
        String hoy = java.time.LocalDate.now().toString();
        for (String nombre : Caso.todos()) {
            Path exp = Caso.recurso(Caso.RAIZ + "/cases/" + nombre).resolve("expected.json");
            assertThat(Files.readString(exp)).as(nombre + ": golden must not contain the day it is run (" + hoy + ")").doesNotContain(hoy);
        }
    }

    @Test
    void elTextoUtilDeLaNpeDelJdkSeNormalizaEnAmbosLados() {
        String legacy = "{\"d\":\"Cannot invoke \\\"String.equals(Object)\\\" because \\\"<local33>.CodigoTipoSolicitud\\\" is null\"}";
        String moderno = "{\"d\":\"Cannot invoke \\\"String.equals(Object)\\\" because \\\"contrato.CodigoTipoSolicitud\\\" is null\"}";
        assertThat(Canon.texto(Canon.parse(legacy))).contains(Dump.NPE_TOKEN).doesNotContain("local33");
        assertThat(Canon.texto(Canon.parse(moderno))).isEqualTo(Canon.texto(Canon.parse(legacy)));
        assertThat(Dump.sinMensajeNpe("NullPointerException: Cannot read field \"x\" because \"<local4>\" is null")).isEqualTo("NullPointerException: <NPE-MSG>");
    }

    @Test
    void laMascaraDeLaNpeNoEscondeOtrasDiferenciasDeMensaje() {
        String a = "{\"d\":\"Cannot invoke \\\"String.equals(Object)\\\" because \\\"<local3>.X\\\" is null\"}";
        String otraNpe = "{\"d\":\"Cannot invoke \\\"String.equals(Object)\\\" because \\\"<local3>.X\\\" is null y mas\"}";
        String noNpe = "{\"d\":\"Error al cargar el contrato\"}";
        assertThat(Canon.texto(Canon.parse(a))).isNotEqualTo(Canon.texto(Canon.parse(noNpe)));
        assertThat(Canon.texto(Canon.parse("{\"d\":\"Error A\"}"))).isNotEqualTo(Canon.texto(Canon.parse("{\"d\":\"Error B\"}")));
        assertThat(Canon.texto(Canon.parse(a))).isNotEqualTo(Canon.texto(Canon.parse(otraNpe)));
    }
}
