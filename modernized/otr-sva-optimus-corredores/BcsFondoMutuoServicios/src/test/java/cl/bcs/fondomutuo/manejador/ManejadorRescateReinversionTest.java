package cl.bcs.fondomutuo.manejador;

import static cl.bcs.fondomutuo.manejador.Banco.entrada;
import static cl.bcs.fondomutuo.manejador.Banco.json;
import static cl.bcs.fondomutuo.manejador.Banco.sol;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import cl.bcs.fondomutuo.kit.ServicioNoServidoException;
import cl.bcs.fondomutuo.manejador.ManejadorContrato.Entrada;
import cl.bcs.fondomutuo.manejador.ManejadorSolicitudAporteRescateService.Lista;
import cl.bcs.fondomutuo.rescatereinversion.RescateReinversionDestino;

/**
 * F2 W1 front 2: the list RESCATE_REINVERSION of the Manejador (plan 6). Start-up validation accepts it only with the real destination; the mixed-request
 * test of F1 (fmMan11) extended: with NORMAL and RESCATE_REINVERSION served, a request that ALSO has a non-served list is a 503 before ANY call; with both
 * served, Normal and then RescateReinversion run in the fixed order.
 */
class ManejadorRescateReinversionTest {

    @Test
    void arranque_RESCATE_REINVERSION_soloConSuHandlerReal() {
        Map<Lista, TipoSolicitudPort> conNoServido = new EnumMap<>(Lista.class);
        for (Lista l : Lista.values()) if (l != Lista.NORMAL) conNoServido.put(l, new TipoNoServido(l.name()));
        assertThatThrownBy(() -> ManejadorConfiguracion.validarListasServidas(Set.of("NORMAL", "RESCATE_REINVERSION"), conNoServido))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("RESCATE_REINVERSION");
        Map<Lista, TipoSolicitudPort> conReal = new EnumMap<>(conNoServido);
        conReal.put(Lista.RESCATE_REINVERSION, new RescateReinversionDestino(in -> {
            throw new AssertionError("not called at start-up");
        }));
        assertThatCode(() -> ManejadorConfiguracion.validarListasServidas(Set.of("NORMAL", "RESCATE_REINVERSION"), conReal)).doesNotThrowAnyException();
        assertThatThrownBy(() -> ManejadorConfiguracion.validarListasServidas(Set.of("NORMAL", "RESCATE_REINVERSION", "TRASPASO"), conReal))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("TRASPASO");
    }

    @Test
    void arranque_RESCATE_REINVERSION_con_otras_listas_exige_custodia_abierta() {
        Map<Lista, TipoSolicitudPort> destinos = new EnumMap<>(Lista.class);
        for (Lista l : Lista.values()) if (l != Lista.NORMAL) destinos.put(l, new TipoNoServido(l.name()));
        destinos.put(Lista.RESCATE_REINVERSION, new RescateReinversionDestino(in -> {
            throw new AssertionError("not called at start-up");
        }));
        // review HIGH: with G-CUS closed a request with NORMAL and RESCATE_REINVERSION would commit Normal and then fail at the custody gate
        assertThatThrownBy(() -> ManejadorConfiguracion.validarListasServidas(Set.of("NORMAL", "RESCATE_REINVERSION"), destinos, false))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("custodia");
        assertThatCode(() -> ManejadorConfiguracion.validarListasServidas(Set.of("NORMAL", "RESCATE_REINVERSION"), destinos, true)).doesNotThrowAnyException();
        // alone it is harmless (always a clean 503 without effects) and so is Normal alone
        assertThatCode(() -> ManejadorConfiguracion.validarListasServidas(Set.of("RESCATE_REINVERSION"), destinos, false)).doesNotThrowAnyException();
        assertThatCode(() -> ManejadorConfiguracion.validarListasServidas(Set.of("NORMAL"), destinos, false)).doesNotThrowAnyException();
    }

    private static Entrada mixta(boolean conTraspaso) {
        Entrada e = entrada();
        e.p_SolicitudesAporteRescateNormal = List.of(sol(1));
        e.p_SolicitudRescateReinversionRescate = List.of(json(2));
        e.p_SolicitudRescateReinversionAporte = List.of(json(2));
        if (conTraspaso) e.p_SolicitudRescateTraspaso = List.of(json(3));
        return e;
    }

    @Test
    void mixta_conRescateReinversionServida_peroOtraListaNo_503AntesDeLlamarANadie() {
        Banco b = new Banco();
        assertThatThrownBy(() -> b.servicio(Set.of("NORMAL", "RESCATE_REINVERSION")).logica(mixta(true))).isInstanceOf(ServicioNoServidoException.class)
                .extracting(x -> ((ServicioNoServidoException) x).compuerta()).isEqualTo("G0");
        assertThat(b.log).as("503 before the first call: neither Normal nor RescateReinversion ran").isEmpty();
    }

    @Test
    void mixta_conRescateReinversionNoServida_503SinLlamadas_yServidaAmbasCorren() throws Exception {
        Banco b = new Banco();
        assertThatThrownBy(() -> b.servicio(Set.of("NORMAL")).logica(mixta(false))).isInstanceOf(ServicioNoServidoException.class);
        assertThat(b.log).isEmpty();
        Banco s = new Banco();
        s.servicio(Set.of("NORMAL", "RESCATE_REINVERSION")).logica(mixta(false));
        // the scripted RescateReinversion of the bench returns no result in its simulation, so no real pass follows (D-MAN-7); the order is the fixed one
        assertThat(s.log).containsExactly("NORMAL:S[1]", "NORMAL:N[1]", "RESCATE_REINVERSION:S:A[2]:R[2]");
    }
}
