package cl.bcs.tesoreria.familia_b;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import cl.bcs.tesoreria.familia_b.support.Caso;
import cl.bcs.tesoreria.familia_b.support.ColaboradoresDeCaso;
import cl.bcs.tesoreria.familia_b.support.Equivalencia;
import cl.bcs.tesoreria.familia_b.support.ZonaUtc;
import cl.bcs.tesoreria.javacall.QuitarDetalleNominasTransferenciasService;

/**
 * Characterization of legacy {@code cl.bcs.tesoreria.nor.QuitarDetalleNominasTransferencias} (entry point {@code call}) as {@code QuitarDetalleNominasTransferenciasService#execute(JsonObject)}:
 * removes comprobantes from a transfer payroll and recalculates its amounts.
 * 
 * <p>Goldens: {@code src/test/resources/tesoreria-b/cases/TES-B-233..242}. Collaborator AnularCargoAbono is a recorded fake.
 */
@ExtendWith(ZonaUtc.class)
class QuitarDetalleNominasTransferenciasServiceTest {

    static final List<String> CASOS = List.of(
            "TES-B-233_quita-un-detalle-recalcula-monto-de-nomina-y-partida",
            "TES-B-234_quita-todos-los-detalles-deja-monto-nulo",
            "TES-B-235_sin-registros-informa-NO_VIENEN_MOVIMIENTOS",
            "TES-B-236_sin-cargos-abono-no-llama-anular",
            "TES-B-237_anular-cargo-abono-con-errores-suma-al-contador",
            "TES-B-238_anular-cargo-abono-lanza-excepcion-tragada",
            "TES-B-239_dos-nominas-solo-recalcula-la-del-primer-registro",
            "TES-B-240_nomina-nula-en-el-primer-registro-no-recalcula-nada",
            "TES-B-241_comprobante-fuera-de-la-nomina-igual-queda-ING",
            "TES-B-242_reglas-de-negocio-no-se-usan");

    private static final Caso.Contador EJECUTADOS = new Caso.Contador();

    private static void verificar(String nombre) {
        ColaboradoresDeCaso[] col = new ColaboradoresDeCaso[1];
        Equivalencia.verificar(EJECUTADOS, nombre, caso -> {
            col[0] = new ColaboradoresDeCaso(caso);
            return new QuitarDetalleNominasTransferenciasService(caso.jdbc(), col[0]).execute(caso.entrada().deepCopy());
        }, () -> col[0].traza());
    }

    @AfterAll
    static void resumen() {
        EJECUTADOS.imprimir("QuitarDetalleNominasTransferencias");
        assertThat(EJECUTADOS.total()).as("equivalence cases executed (zero = not proven)").isGreaterThan(0);
    }

    // nor/QuitarDetalleNominasTransferencias.java:120-149 + sql/QuitarDetalleNominasTransferencias_*.sql: the detail leaves the nomina (row deleted), forma de pago/comprobante/detail back to ING, amount of the nomina recalculated and copied to the partida comprobante
    @Test
    void tesB233_quitaUnDetalleRecalculaMontoDeNominaYPartida() {
        verificar(CASOS.get(0));
    }

    // nor/QuitarDetalleNominasTransferencias.java:146: no detail left: SUM is NULL so the nomina and partida amounts become NULL (legacy)
    @Test
    void tesB234_quitaTodosLosDetallesDejaMontoNulo() {
        verificar(CASOS.get(1));
    }

    // nor/QuitarDetalleNominasTransferencias.java:101-105: empty list: error NO_VIENEN_MOVIMIENTOS
    @Test
    void tesB235_sinRegistrosInformaNOVIENENMOVIMIENTOS() {
        verificar(CASOS.get(2));
    }

    // nor/QuitarDetalleNominasTransferencias.java:120-121: no TES cargo/abono: AnularCargoAbono is not called
    @Test
    void tesB236_sinCargosAbonoNoLlamaAnular() {
        verificar(CASOS.get(3));
    }

    // nor/QuitarDetalleNominasTransferencias.java:134-135: AnularCargoAbono errors added to the counter
    @Test
    void tesB237_anularCargoAbonoConErroresSumaAlContador() {
        verificar(CASOS.get(4));
    }

    // nor/QuitarDetalleNominasTransferencias.java:158-161: AnularCargoAbono throws: swallowed, nothing else is updated
    @Test
    void tesB238_anularCargoAbonoLanzaExcepcionTragada() {
        verificar(CASOS.get(5));
    }

    // nor/QuitarDetalleNominasTransferencias.java:144: only the nomina of the FIRST register is recalculated (legacy defect)
    @Test
    void tesB239_dosNominasSoloRecalculaLaDelPrimerRegistro() {
        verificar(CASOS.get(6));
    }

    // nor/QuitarDetalleNominasTransferencias.java:144-146: first register without nomina: the recalculation updates nothing
    @Test
    void tesB240_nominaNulaEnElPrimerRegistroNoRecalculaNada() {
        verificar(CASOS.get(7));
    }

    // nor/QuitarDetalleNominasTransferencias.java:140-141: the comprobante/detail states go back to ING even if the comprobante is not in the nomina
    @Test
    void tesB241_comprobanteFueraDeLaNominaIgualQuedaING() {
        verificar(CASOS.get(8));
    }

    // nor/QuitarDetalleNominasTransferencias.java:128: p_UsaReglasNegocio is never read
    @Test
    void tesB242_reglasDeNegocioNoSeUsan() {
        verificar(CASOS.get(9));
    }

}
