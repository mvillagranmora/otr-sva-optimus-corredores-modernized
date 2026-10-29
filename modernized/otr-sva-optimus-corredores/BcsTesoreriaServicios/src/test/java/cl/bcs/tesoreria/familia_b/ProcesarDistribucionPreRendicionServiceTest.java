package cl.bcs.tesoreria.familia_b;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import cl.bcs.tesoreria.familia_b.support.Caso;
import cl.bcs.tesoreria.familia_b.support.Equivalencia;
import cl.bcs.tesoreria.familia_b.support.ZonaUtc;
import cl.bcs.tesoreria.familia_b.support.ColaboradoresDeCaso;
import cl.bcs.tesoreria.familia_b.support.ServicioDistribucionDeCaso;

/**
 * Characterization of legacy {@code ProcesarDistribucion.ValidaPreRendicion} (public method called by CrearCargaMasivaPacRen) as
 * {@code ProcesarDistribucionService#validaPreRendicion(JsonObject)}: validates the rendition file against the mandates and writes PAC.LogMandatoPAC.
 * 
 * <p>Goldens: {@code src/test/resources/tesoreria-b/cases/TES-B-309..318} (output + PAC.LogMandatoPAC with FechaProceso masked). Collaborators (CalcularParidad,
 * RevisarRequisitosBasicos) are recorded fakes and their call trace is compared.
 */
@ExtendWith(ZonaUtc.class)
class ProcesarDistribucionPreRendicionServiceTest {

    static final List<String> CASOS = List.of(
            "TES-B-309_pre-rendicion-valida-inserta-el-log-del-mandato",
            "TES-B-310_pre-rendicion-con-log-existente-lo-actualiza",
            "TES-B-311_pre-rendicion-mandato-ya-procesado-lo-informa",
            "TES-B-312_pre-rendicion-fila-huerfana-del-archivo-lanza-NPE-por-clave-nula",
            "TES-B-313_pre-rendicion-estado-cargo-banco-rechazado-informa-por-detalle",
            "TES-B-314_pre-rendicion-requisitos-basicos-con-error",
            "TES-B-315_pre-rendicion-sin-mandatos-el-archivo-no-coincide",
            "TES-B-316_pre-rendicion-mandato-inactivo-informa-por-detalle-de-archivo",
            "TES-B-317_pre-rendicion-saapv-usa-el-dia-de-descuento-del-contrato",
            "TES-B-318_pre-rendicion-monto-del-archivo-distinto-informa-sin-guardar-id-valido");

    private static final Caso.Contador EJECUTADOS = new Caso.Contador();

    private static final String[] SIN_ORDEN = {"P_OUT_MENSAJES", "P_OUT_SOLICITUDES_PROCESADAS", "P_OUT_SOLICITUDES_NO_PROCESADAS", "P_OUT_SOLICITUDES_A_PROCESAR"};

    private static void verificar(String nombre) {
        ColaboradoresDeCaso[] col = new ColaboradoresDeCaso[1];
        Equivalencia.verificar(EJECUTADOS, nombre, caso -> {
            col[0] = new ColaboradoresDeCaso(caso);
            return ServicioDistribucionDeCaso.de(caso, col[0]).validaPreRendicion(caso.entrada().deepCopy());
        }, () -> col[0].traza(), SIN_ORDEN);
    }

    @AfterAll
    static void resumen() {
        EJECUTADOS.imprimir("ProcesarDistribucion.ValidaPreRendicion");
        assertThat(EJECUTADOS.total()).as("equivalence cases executed (zero = not proven)").isGreaterThan(0);
    }

    // ProcesarDistribucion.java:2161-2290: ValidaPreRendicion: a valid mandate without log gets a log row (PreRendicion S)
    @Test
    void tesB309_preRendicionValidaInsertaElLogDelMandato() {
        verificar(CASOS.get(0));
    }

    // ProcesarDistribucion.java:2277-2279,2323-2350: existing pending log: updated (FechaProceso refreshed, glosa rewritten)
    @Test
    void tesB310_preRendicionConLogExistenteLoActualiza() {
        verificar(CASOS.get(1));
    }

    // ProcesarDistribucion.java:2255,2352-2367: a processed (PRO) log of the month makes the mandate 'Mandato se encuentra procesado'
    @Test
    void tesB311_preRendicionMandatoYaProcesadoLoInforma() {
        verificar(CASOS.get(2));
    }

    // ProcesarDistribucion.java:2427,2209: orphan file row: the detail query has no mandate filter, IdentServicio null -> groupingBy NullPointerException (legacy defect)
    @Test
    void tesB312_preRendicionFilaHuerfanaDelArchivoLanzaNPEPorClaveNula() {
        verificar(CASOS.get(3));
    }

    // ProcesarDistribucion.java:2444-2472: bank charge state != 3: error per file detail, log row with IdMandatoPAC -1
    @Test
    void tesB313_preRendicionEstadoCargoBancoRechazadoInformaPorDetalle() {
        verificar(CASOS.get(4));
    }

    // ProcesarDistribucion.java:2195-2200: basic requirements with errors
    @Test
    void tesB314_preRendicionRequisitosBasicosConError() {
        verificar(CASOS.get(5));
    }

    // ProcesarDistribucion.java:2219-2223: no mandates: 'EL ARCHIVO DE CARGA NO COINCIDE INFORMACION EXISTENTE'
    @Test
    void tesB315_preRendicionSinMandatosElArchivoNoCoincide() {
        verificar(CASOS.get(6));
    }

    // ProcesarDistribucion.java:2473-2500: inactive mandate: rejected through its file detail
    @Test
    void tesB316_preRendicionMandatoInactivoInformaPorDetalleDeArchivo() {
        verificar(CASOS.get(7));
    }

    // ProcesarDistribucion.java:2236-2240: discount day read from the SAAPV contract
    @Test
    void tesB317_preRendicionSaapvUsaElDiaDeDescuentoDelContrato() {
        verificar(CASOS.get(8));
    }

    // ProcesarDistribucion.java:1951-1965: amount mismatch: rejected without a valid mandate id
    @Test
    void tesB318_preRendicionMontoDelArchivoDistintoInformaSinGuardarIdValido() {
        verificar(CASOS.get(9));
    }

}
