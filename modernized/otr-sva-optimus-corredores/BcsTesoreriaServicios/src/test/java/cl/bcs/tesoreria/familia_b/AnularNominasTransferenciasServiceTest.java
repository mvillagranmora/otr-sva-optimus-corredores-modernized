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
import cl.bcs.tesoreria.javacall.AnularNominasTransferenciasService;

/**
 * Characterization of legacy {@code cl.bcs.tesoreria.nor.AnularNominasTransferencias} (entry point {@code call}) as {@code AnularNominasTransferenciasService#execute(JsonObject)}:
 * annuls transfer payrolls, their comprobantes and releases the original movements.
 * 
 * <p>Goldens: {@code src/test/resources/tesoreria-b/cases/TES-B-221..232}. Collaborators RevisarRequisitosBasicos and AnularCargoAbono are recorded fakes
 * (their input is part of each golden; compared in {@code src/test-pending/tesoreria-b/}). Legacy defects: the requirements query result is never used; every exception is swallowed.
 */
@ExtendWith(ZonaUtc.class)
class AnularNominasTransferenciasServiceTest {

    static final List<String> CASOS = List.of(
            "TES-B-221_anula-nomina-y-libera-movimientos-con-reglas",
            "TES-B-222_anula-nomina-sin-reglas-de-negocio",
            "TES-B-223_sin-registros-informa-NO_VIENEN_MOVIMIENTOS",
            "TES-B-224_requisitos-basicos-con-error-igual-anula-y-cuenta-error",
            "TES-B-225_anular-cargo-abono-con-errores-suma-al-contador",
            "TES-B-226_nomina-sin-cargos-abono-no-llama-anular",
            "TES-B-227_nomina-inexistente-no-actualiza-nada",
            "TES-B-228_usa-reglas-nulo-lanza-NPE-tragado-y-no-actualiza",
            "TES-B-229_dos-nominas-a-la-vez",
            "TES-B-230_entidad-detalle-no-coincide-con-id-de-nomina",
            "TES-B-231_anular-cargo-abono-lanza-excepcion-tragada-sin-cerrar-temporal",
            "TES-B-232_nomina-recepcionada-se-anula-igual");

    private static final Caso.Contador EJECUTADOS = new Caso.Contador();

    private static void verificar(String nombre) {
        ColaboradoresDeCaso[] col = new ColaboradoresDeCaso[1];
        Equivalencia.verificar(EJECUTADOS, nombre, caso -> {
            col[0] = new ColaboradoresDeCaso(caso);
            return new AnularNominasTransferenciasService(caso.jdbc(), col[0]).execute(caso.entrada().deepCopy());
        }, () -> col[0].traza());
    }

    @AfterAll
    static void resumen() {
        EJECUTADOS.imprimir("AnularNominasTransferencias");
        assertThat(EJECUTADOS.total()).as("equivalence cases executed (zero = not proven)").isGreaterThan(0);
    }

    // nor/AnularNominasTransferencias.java:116-186 + sql/AnularNominasTransferencias_*.sql: nomina, detail, partida/detail comprobantes and forma de pago become ANU; original movements and their forma de pago (not ANU/MOD) go back to ING; TES cargo/abono of the comprobantes are sent to AnularCargoAbono
    @Test
    void tesB221_anulaNominaYLiberaMovimientosConReglas() {
        verificar(CASOS.get(0));
    }

    // nor/AnularNominasTransferencias.java:135: p_UsaReglasNegocio N skips RevisarRequisitosBasicos
    @Test
    void tesB222_anulaNominaSinReglasDeNegocio() {
        verificar(CASOS.get(1));
    }

    // nor/AnularNominasTransferencias.java:122-126: empty list: error NO_VIENEN_MOVIMIENTOS added to the output
    @Test
    void tesB223_sinRegistrosInformaNOVIENENMOVIMIENTOS() {
        verificar(CASOS.get(2));
    }

    // nor/AnularNominasTransferencias.java:139-143: basic-requirement errors are only counted: the annulment continues
    @Test
    void tesB224_requisitosBasicosConErrorIgualAnulaYCuentaError() {
        verificar(CASOS.get(3));
    }

    // nor/AnularNominasTransferencias.java:180-181: AnularCargoAbono errors are added to the counter
    @Test
    void tesB225_anularCargoAbonoConErroresSumaAlContador() {
        verificar(CASOS.get(4));
    }

    // nor/AnularNominasTransferencias.java:166-167: no TES cargo/abono: AnularCargoAbono is not called
    @Test
    void tesB226_nominaSinCargosAbonoNoLlamaAnular() {
        verificar(CASOS.get(5));
    }

    // nor/AnularNominasTransferencias.java:147,155-164: unknown nomina: the SQL updates nothing; the requirements query result is ignored
    @Test
    void tesB227_nominaInexistenteNoActualizaNada() {
        verificar(CASOS.get(6));
    }

    // nor/AnularNominasTransferencias.java:135,191-194: null UsaReglasNegocio: NullPointerException swallowed by the catch; nothing is updated and the temporary table is left
    @Test
    void tesB228_usaReglasNuloLanzaNPETragadoYNoActualiza() {
        verificar(CASOS.get(7));
    }

    // : dos nominas a la vez
    @Test
    void tesB229_dosNominasALaVez() {
        verificar(CASOS.get(8));
    }

    // nor/AnularNominasTransferencias.java:155-164: Entidad is only read by the requirements query: the SQL matches RP.Id against the nomina id
    @Test
    void tesB230_entidadDetalleNoCoincideConIdDeNomina() {
        verificar(CASOS.get(9));
    }

    // nor/AnularNominasTransferencias.java:191-194: AnularCargoAbono throws: swallowed, DROP of the temporary table skipped, committed changes stay
    @Test
    void tesB231_anularCargoAbonoLanzaExcepcionTragadaSinCerrarTemporal() {
        verificar(CASOS.get(10));
    }

    // nor/AnularNominasTransferencias.java:147: a received (RCP) nomina is annulled anyway: the requirements result is never read (legacy defect)
    @Test
    void tesB232_nominaRecepcionadaSeAnulaIgual() {
        verificar(CASOS.get(11));
    }

}
