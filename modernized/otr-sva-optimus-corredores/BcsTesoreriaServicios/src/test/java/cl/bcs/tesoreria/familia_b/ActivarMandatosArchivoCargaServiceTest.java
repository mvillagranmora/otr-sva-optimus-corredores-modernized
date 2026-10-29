package cl.bcs.tesoreria.familia_b;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import cl.bcs.tesoreria.familia_b.support.Caso;
import cl.bcs.tesoreria.familia_b.support.Equivalencia;
import cl.bcs.tesoreria.familia_b.support.ZonaUtc;
import cl.bcs.tesoreria.javacall.ActivarMandatosArchivoCargaService;

/**
 * Characterization of legacy {@code cl.bcs.tesoreria.ActivarMandatosArchivoCargo} (typed entry {@code logica(Connection, Long DiasDescuento)}) as
 * {@code ActivarMandatosArchivoCargaService#execute(JsonObject)} (scalar parameter travelling as {@code {"valor": n}}): deactivates the active
 * PAC mandates whose contract discount day differs from the given day.
 * 
 * <p>Goldens: {@code src/test/resources/tesoreria-b/cases/TES-B-199..203} (output + mandate states). Legacy defect pinned in TES-B-203: {@code call()} is a
 * TODO stub that returns null.
 */
@ExtendWith(ZonaUtc.class)
class ActivarMandatosArchivoCargaServiceTest {

    static final List<String> CASOS = List.of(
            "TES-B-199_dia-distinto-desactiva-mandatos-csc-y-saapv-vigentes",
            "TES-B-200_dia-igual-a-todos-no-actualiza-ninguno",
            "TES-B-201_sin-mandatos-activos-informa-cero-actualizados",
            "TES-B-202_dia-descuento-nulo-no-selecciona-nada",
            "TES-B-203_punto-de-entrada-call-no-hace-nada-y-devuelve-nulo");

    private static final Caso.Contador EJECUTADOS = new Caso.Contador();

    private static void verificar(String nombre) {
        Equivalencia.verificar(EJECUTADOS, nombre, caso -> {
            var servicio = new ActivarMandatosArchivoCargaService(caso.jdbc());
            if ("call".equals(caso.caso().get("metodo").getAsString())) {
                return servicio.execute(caso.entrada().deepCopy());
            }
            var valor = caso.entrada().get("valor");
            return cl.bcs.tesoreria.javacall.kit.JsonLegacy.arbol(servicio.logica(valor.isJsonNull() ? null : valor.getAsLong()));
        });
    }

    @AfterAll
    static void resumen() {
        EJECUTADOS.imprimir("ActivarMandatosArchivoCargo");
        assertThat(EJECUTADOS.total()).as("equivalence cases executed (zero = not proven)").isGreaterThan(0);
    }

    // ActivarMandatosArchivoCargo.java:65-74: mandates with an active CSC/SAAPV contract of another discount day become INA (annulled/revoked contracts and non-PAC payment types never match)
    @Test
    void tesB199_diaDistintoDesactivaMandatosCscYSaapvVigentes() {
        verificar(CASOS.get(0));
    }

    // ActivarMandatosArchivoCargo.java:67-71: nothing differs: no update
    @Test
    void tesB200_diaIgualATodosNoActualizaNinguno() {
        verificar(CASOS.get(1));
    }

    // ActivarMandatosArchivoCargo.java:74: no active mandates: message '0 Mandatos actualizados Correctamente.'
    @Test
    void tesB201_sinMandatosActivosInformaCeroActualizados() {
        verificar(CASOS.get(2));
    }

    // ActivarMandatosArchivoCargo.java:65-67: null day: the != comparison is never true
    @Test
    void tesB202_diaDescuentoNuloNoSeleccionaNada() {
        verificar(CASOS.get(3));
    }

    // ActivarMandatosArchivoCargo.java:124-128: legacy defect: call() returns null without running logica
    @Test
    void tesB203_callNoHaceNadaYDevuelveNulo() {
        verificar(CASOS.get(4));
    }

}
