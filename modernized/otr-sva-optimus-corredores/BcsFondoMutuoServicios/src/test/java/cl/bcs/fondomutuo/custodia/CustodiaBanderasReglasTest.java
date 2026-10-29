package cl.bcs.fondomutuo.custodia;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;

import cl.bcs.fondomutuo.adapter.out.jdbc.ConexionUnidadDeTrabajo;
import cl.bcs.fondomutuo.kit.Compuertas;
import cl.bcs.fondomutuo.kit.ServicioNoServidoException;
import cl.bcs.fondomutuo.port.out.http.CustodiaPort;

/** Review of 2026-10-28, H3: the rules of the three custody switches without a Spring context. */
class CustodiaBanderasReglasTest {

    static final CustodiaPort HTTP = e -> new JsonObject();
    final FlujoCustodiaEnProceso enProceso = new FlujoCustodiaEnProceso(new ConexionUnidadDeTrabajo(null), null);

    @Test
    @DisplayName("the 6-argument Compuertas (harnesses) keeps Normal following G-CUS and the commissions on HTTP")
    void elConstructorDeSeisArgumentosConservaElComportamiento() {
        Compuertas abierta = new Compuertas("NORMAL", true, false, false, false, true);
        assertThat(abierta.custodiaNormalHabilitada()).isTrue();
        assertThat(abierta.comisionesEnProceso()).isFalse();
        assertThatCode(abierta::gCustodiaNormal).doesNotThrowAnyException();
        Compuertas cerrada = new Compuertas("NORMAL", true, false, false, false, false);
        assertThat(cerrada.custodiaNormalHabilitada()).isFalse();
        assertThatThrownBy(cerrada::gCustodiaNormal).isInstanceOf(ServicioNoServidoException.class);
        Compuertas separada = new Compuertas("NORMAL", true, false, false, false, true, false, false);
        assertThatCode(separada::gCustodia).doesNotThrowAnyException();
        assertThatThrownBy(separada::gCustodiaNormal).isInstanceOf(ServicioNoServidoException.class);
    }

    @Test
    @DisplayName("the start-up rule: combinations")
    void laReglaDeArranque() {
        assertThatCode(() -> CustodiaEnProcesoArranque.iniciar(false, false, false, HTTP)).doesNotThrowAnyException();
        assertThatCode(() -> CustodiaEnProcesoArranque.iniciar(true, false, false, enProceso)).doesNotThrowAnyException();
        assertThatCode(() -> CustodiaEnProcesoArranque.iniciar(true, true, false, enProceso)).doesNotThrowAnyException();
        assertThatCode(() -> CustodiaEnProcesoArranque.iniciar(true, false, true, enProceso)).doesNotThrowAnyException();
        assertThatCode(() -> CustodiaEnProcesoArranque.iniciar(true, true, true, enProceso)).doesNotThrowAnyException();
        assertThatThrownBy(() -> CustodiaEnProcesoArranque.iniciar(false, true, false, HTTP)).isInstanceOf(IllegalStateException.class).hasMessageContaining("normal-habilitada");
        assertThatThrownBy(() -> CustodiaEnProcesoArranque.iniciar(false, false, true, HTTP)).isInstanceOf(IllegalStateException.class).hasMessageContaining("comisiones-en-proceso");
        assertThatThrownBy(() -> CustodiaEnProcesoArranque.iniciar(true, true, true, HTTP)).isInstanceOf(IllegalStateException.class).hasMessageContaining("not the in-process adapter");
    }

    @Test
    @DisplayName("which port CalcularComisiones gets")
    void elPuertoDeLasComisiones() {
        List<CustodiaPort> ambos = List.of(enProceso, HTTP);
        assertThat(CustodiaParaComisiones.elegir(enProceso, ambos, false)).as("in-process @Primary but the switch is off: HTTP").isSameAs(HTTP);
        assertThat(CustodiaParaComisiones.elegir(enProceso, ambos, true)).isSameAs(enProceso);
        assertThat(CustodiaParaComisiones.elegir(HTTP, List.of(HTTP), false)).as("default: the only port, unchanged").isSameAs(HTTP);
        assertThat(CustodiaParaComisiones.elegir(HTTP, List.of(HTTP), true)).isSameAs(HTTP);
    }
}
