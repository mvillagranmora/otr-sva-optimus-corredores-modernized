package cl.bcs.fondomutuo.manejador;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import cl.bcs.fondomutuo.manejador.ManejadorSolicitudAporteRescateService.Lista;
import cl.bcs.fondomutuo.rescatereinversion.RescateReinversionDestino;

/**
 * Architecture review F3 W1 (HIGH, H1): the validator of {@code fondomutuo.solicitud.listas-servidas} accepts the eight names of the UI reads, still rejects any other unknown
 * name (a typo stops the start-up) and does not count a read as "another list of the Manejador" in the RESCATE_REINVERSION + closed custody rule.
 */
class ListasLecturaValidadorTest {

    private static final Set<String> OCHO = Set.of("CONSULTAR_FECHA_SOLICITUD", "CONSULTAR_VALOR_DOLAR", "CONSULTAR_FONDO_REINVERSION", "CONSULTAR_INSTRUMENTOS",
            "CONSULTAR_SOLICITUDES", "CONSULTAR_SOLICITUDES_WS", "CONSULTAR_DETALLE_SOLICITUD", "CONSULTAR_CUSTODIA_PORTAFOLIO");

    private static Map<Lista, TipoSolicitudPort> destinos(boolean conRescateReinversion) {
        Map<Lista, TipoSolicitudPort> d = new EnumMap<>(Lista.class);
        for (Lista l : Lista.values()) if (l != Lista.NORMAL) d.put(l, new TipoNoServido(l.name()));
        if (conRescateReinversion) {
            d.put(Lista.RESCATE_REINVERSION, new RescateReinversionDestino(in -> {
                throw new AssertionError("not called at start-up");
            }));
        }
        return d;
    }

    @Test
    void lasOchoLecturasSonLasDeLasConstantesLISTA() {
        assertThat(ManejadorConfiguracion.LISTAS_LECTURA).containsExactlyInAnyOrderElementsOf(OCHO);
    }

    @Test
    void cadaLecturaAbreSola_yJuntoANormal_ConCustodiaCerrada() {
        for (String lectura : OCHO) {
            assertThatCode(() -> ManejadorConfiguracion.validarListasServidas(Set.of(lectura), destinos(false), false)).as(lectura).doesNotThrowAnyException();
            assertThatCode(() -> ManejadorConfiguracion.validarListasServidas(Set.of("NORMAL", lectura), destinos(false), false)).as("NORMAL + " + lectura)
                    .doesNotThrowAnyException();
        }
        assertThatCode(() -> ManejadorConfiguracion.validarListasServidas(OCHO, destinos(false), false)).doesNotThrowAnyException();
    }

    @Test
    void unNombreDesconocido_inclusoParecidoAUnaLectura_detieneElArranque() {
        for (String typo : new String[] {"CONSULTAR_FECHA_SOLICITUDES", "CONSULTAR_VALOR_DOLARES", "CONSULTAR", "consultar_fecha_solicitud", "NORMAL2"}) {
            assertThatThrownBy(() -> ManejadorConfiguracion.validarListasServidas(Set.of("NORMAL", typo), destinos(false), true)).as(typo)
                    .isInstanceOf(IllegalStateException.class).hasMessageContaining("unknown list").hasMessageContaining(typo);
        }
        assertThatThrownBy(() -> ManejadorConfiguracion.validarListasServidas(Set.of("CONSULTAR_FECHA_SOLICITUD", "TYPO"), destinos(false), true))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("TYPO");
    }

    @Test
    void unaListaDelManejadorSinHandler_sigueFallando_aunqueVengaConLecturas() {
        assertThatThrownBy(() -> ManejadorConfiguracion.validarListasServidas(Set.of("TRASPASO", "CONSULTAR_FECHA_SOLICITUD"), destinos(false), true))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("TRASPASO");
    }

    @Test
    void rescateReinversionMasLecturas_conCustodiaCerrada_noEsFalsoPositivo() {
        for (String lectura : OCHO) {
            assertThatCode(() -> ManejadorConfiguracion.validarListasServidas(Set.of("RESCATE_REINVERSION", lectura), destinos(true), false)).as(lectura)
                    .doesNotThrowAnyException();
        }
        assertThatCode(() -> ManejadorConfiguracion.validarListasServidas(Set.of("RESCATE_REINVERSION", "CONSULTAR_FECHA_SOLICITUD", "CONSULTAR_DETALLE_SOLICITUD"),
                destinos(true), false)).doesNotThrowAnyException();
    }

    @Test
    void rescateReinversionMasOtraListaDelManejador_conCustodiaCerrada_sigueFallando_tambienConLecturas() {
        assertThatThrownBy(() -> ManejadorConfiguracion.validarListasServidas(Set.of("RESCATE_REINVERSION", "NORMAL", "CONSULTAR_FECHA_SOLICITUD"), destinos(true), false))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("custodia");
        assertThatCode(() -> ManejadorConfiguracion.validarListasServidas(Set.of("RESCATE_REINVERSION", "NORMAL", "CONSULTAR_FECHA_SOLICITUD"), destinos(true), true))
                .doesNotThrowAnyException();
    }
}
