package cl.bcs.fondomutuo.adapter.in.javacall;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.sql.Connection;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;

/** FM-KIT-60..64: the allow-list is EXACTLY the 2 legacy FQN of the plan (DEV-F1); anything else is not served (503). */
class JavaCallDispatcherTest {

    private static JavaCallHandler handler(String fqn) {
        return new JavaCallHandler() {
            @Override
            public String fqn() {
                return fqn;
            }

            @Override
            public JsonObject call(Connection con, JsonObject jin) {
                return new JsonObject();
            }
        };
    }

    @Test
    void fmKit60_listaBlanca_sonExactamenteLosFqnDelPlan() {
        assertThat(JavaCallDispatcher.LISTA_BLANCA).containsExactlyInAnyOrder(
                "cl.bcs.fondomutuo.ManejadorSolicitudAporteRescate", "cl.bcs.fondomutuo.CrearSolicitudAporteRescateNormal",
                "cl.bcs.fondomutuo.CrearSolicitudRescateReinversion",   // F2 W1 front 2 (registry id 1436, plan 6.3)
                "cl.bcs.fondomutuo.ConsultarFechaSolicitudAporteRescate",   // F3 W1 front 1 (registry id 3022, read only)
                "cl.bcs.fondomutuo.ConsultarFondoMutuoReinversion", "cl.bcs.fondomutuo.ConsultarInstrumentosFondosMutuos",
                "cl.bcs.fondomutuo.ConsultarValorDolarFondoMutuo",   // F3 W1 VDO, FMR, IFM (read only)
                "cl.bcs.fondomutuo.ConsultarSolicitudesFFMM", "cl.bcs.fondomutuo.ConsultarSolicitudesFFMMWS",
                "cl.bcs.fondomutuo.ConsultarDetalleSolicitud",
                "cl.bcs.fondomutuo.ConsultarCustodiaPortafolioFondoMutuo",   // F3 W1 SFM, WS, DET, CPF (read only)
                "cl.bcs.fondomutuo.CargarValorCuota");   // F3 W2 CVC (registry id 3035, WRITES; not a read class)
    }

    @Test
    void fmKit61_sinHandlers_nadaSeSirve_niLosDosDeLaLista() {
        JavaCallDispatcher d = new JavaCallDispatcher(List.of());
        assertThat(d.servidas()).isEmpty();
        assertThat(d.buscar(JavaCallDispatcher.MANEJADOR)).isEmpty();
        assertThat(d.buscar(JavaCallDispatcher.CREAR_SOLICITUD_NORMAL)).isEmpty();
    }

    @Test
    void fmKit62_conHandlersDeLaLista_seSirvenSoloEsos() {
        JavaCallDispatcher d = new JavaCallDispatcher(List.of(handler(JavaCallDispatcher.MANEJADOR), handler(JavaCallDispatcher.CREAR_SOLICITUD_NORMAL)));
        assertThat(d.servidas()).containsExactlyInAnyOrder(JavaCallDispatcher.MANEJADOR, JavaCallDispatcher.CREAR_SOLICITUD_NORMAL);
        for (String otro : new String[] {"cl.bcs.fondomutuo.CrearSolicitudRescateReinversion", "cl.bcs.fondomutuo.AperturaAutomaticoFondos",
                "cl.bcs.fondomutuo.CalcularMontoNumeroCuota", "cl.bcs.plataforma.mer.CalcularParidad", "", "cl.bcs.fondomutuo.manejadorsolicitudaporterescate", null}) {
            assertThat(d.buscar(otro)).as(String.valueOf(otro)).isEmpty();
        }
    }

    @Test
    void fmKit63_unHandlerFueraDeLaLista_detieneElArranque_laListaNoCrecePorAccidente() {
        assertThatThrownBy(() -> new JavaCallDispatcher(List.of(handler("cl.bcs.fondomutuo.AperturaAutomaticoFondos"))))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("outside the allow-list");
    }

    @Test
    void fmKit64_dosHandlersParaElMismoFqn_detienenElArranque() {
        assertThatThrownBy(() -> new JavaCallDispatcher(List.of(handler(JavaCallDispatcher.MANEJADOR), handler(JavaCallDispatcher.MANEJADOR))))
                .isInstanceOf(IllegalStateException.class);
    }
}
