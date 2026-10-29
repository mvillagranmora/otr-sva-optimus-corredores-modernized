package cl.bcs.tesoreria.familia_a;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Set;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;

import cl.bcs.tesoreria.familia_a.support.Canon;
import cl.bcs.tesoreria.familia_a.support.Caso;
import cl.bcs.tesoreria.familia_a.support.Verifica;

/** Proves the harness itself can fail: it must reject a wrong output, a missing fixture and a wrong collaborator trace. Not an equivalence test. */
class HarnessSelfCheckTest {

    private static String primerCaso() {
        return Caso.todos().stream().filter(n -> n.startsWith("TES-A-01_")).findFirst().orElseThrow();
    }

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
        assertThatThrownBy(() -> Caso.cargar("TES-A-9999_no-existe")).isInstanceOf(Throwable.class);
    }

    @Test
    void unaSalidaDistintaDelGoldenHaceFallarLaComparacion() {
        Caso caso = Caso.cargar(primerCaso());
        JsonObject distinta = new JsonObject();
        distinta.addProperty("salida_inventada", "x");
        assertThatThrownBy(() -> Verifica.comparar(caso, new Verifica.Ejecucion(distinta, null))).isInstanceOf(AssertionError.class);
    }

    @Test
    void unaExcepcionDondeElLegacyNoLanzoHaceFallarLaComparacion() {
        Caso caso = Caso.cargar(primerCaso());
        assertThatThrownBy(() -> Verifica.comparar(caso, new Verifica.Ejecucion(null, new IllegalStateException("x")))).isInstanceOf(AssertionError.class);
    }

    @Test
    void unaTrazaDeColaboradoresInventadaHaceFallarLaComparacion() {
        Caso caso = Caso.cargar(primerCaso());
        JsonObject llamada = new JsonObject();
        llamada.addProperty("servicio", "ServicioInventado");
        assertThatThrownBy(() -> Verifica.colaboradores(caso, java.util.List.of(llamada))).isInstanceOf(AssertionError.class);
    }
}
