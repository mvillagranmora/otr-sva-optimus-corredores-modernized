package cl.bcs.tesoreria.familia_b;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import cl.bcs.tesoreria.familia_b.support.Caso;
import cl.bcs.tesoreria.familia_b.support.ColaboradoresDeCaso;
import cl.bcs.tesoreria.familia_b.support.ConfiguracionDelCaso;
import cl.bcs.tesoreria.familia_b.support.DiccionarioDeCaso;
import cl.bcs.tesoreria.javacall.liq.ConsultarClearingFinancieroService;
import cl.bcs.tesoreria.familia_b.support.Equivalencia;
import cl.bcs.tesoreria.familia_b.support.ZonaUtc;
import cl.bcs.tesoreria.javacall.liq.ProcesarClearingFinancieroService;

/**
 * Characterization of legacy {@code cl.bcs.tesoreria.liq.ProcesarClearingFinanciero} (entry point {@code call(conn, json)}) as
 * {@code ProcesarClearingFinancieroService#execute(JsonObject)}: runs ConsultarClearingFinanciero (real legacy source in the oracle), turns the
 * net balance of every chamber group into an ingreso/egreso (collaborator CrearIngresoEgreso, a recorded fake), and stamps the generated
 * comprobante on the transactions, dividends and derivative liquidations.
 * 
 * <p>Goldens: {@code src/test/resources/tesoreria-b/cases/TES-B-47..63} (output + the three updated tables + the collaborator call, whose
 * NumeroReferencia values are masked by order of appearance because the legacy draws them from java.util.Random). The collaborator trace is
 * verified in {@code src/test-pending/tesoreria-b/ColaboradoresPendingTest} (needs the Colaboradores port).
 */
@ExtendWith(ZonaUtc.class)
class ProcesarClearingFinancieroServiceTest {

    static final List<String> CASOS = List.of(
            "TES-B-47_dos-camaras-genera-ingresos-egresos-y-marca-movimientos",
            "TES-B-48_todas-las-camaras-cambios-cclv-dividendos-y-derivados",
            "TES-B-49_sin-camaras-solicitadas-no-hay-movimientos-a-procesar",
            "TES-B-50_pais-sin-camara-activa-EMPRESA_LIQUIDADORA_NO_EXISTE",
            "TES-B-51_camara-sin-persona-LIQUIDADORA_SIN_PERSONA",
            "TES-B-52_sin-preferencias-de-pago-NO_CONFIGURADAS",
            "TES-B-53_sin-camara-ni-preferencias-acumula-dos-errores",
            "TES-B-54_falta-preferencia-del-concepto-UI_SIN_PREFERENCIA_PAGO",
            "TES-B-55_ingreso-egreso-con-error-devuelve-mensajes-y-no-marca",
            "TES-B-56_ingreso-egreso-lanza-excepcion-hace-rollback-y-relanza",
            "TES-B-57_localizacion-PE-no-migrada-lanza-NullPointerException",
            "TES-B-58_saldo-de-camara-cero-no-genera-ingreso-y-marca-con-nulo",
            "TES-B-59_sin-preferencias-de-portafolio-usa-cuentas-en-cero",
            "TES-B-60_localizacion-no-configurada-lanza-excepcion",
            "TES-B-61_solo-dividendos-y-derivados-camara-dv",
            "TES-B-62_todas-las-transacciones-ya-procesadas-no-hay-movimientos",
            "TES-B-63_camara-hs-agrupa-bajo-concepto-CLFIHS");

    private static final Caso.Contador EJECUTADOS = new Caso.Contador();

    private static void verificar(String nombre) {
        ColaboradoresDeCaso[] col = new ColaboradoresDeCaso[1];
        Equivalencia.verificar(EJECUTADOS, nombre, caso -> {
            col[0] = new ColaboradoresDeCaso(caso);
            ConfiguracionDelCaso configuracion = new ConfiguracionDelCaso(caso);
            return new ProcesarClearingFinancieroService(caso.jdbc(), new DiccionarioDeCaso(), configuracion,
                    new ConsultarClearingFinancieroService(caso.jdbc(), configuracion), col[0]).execute(caso.entrada().deepCopy());
        }, () -> col[0].traza());
    }

    @AfterAll
    static void resumen() {
        EJECUTADOS.imprimir("ProcesarClearingFinanciero");
        assertThat(EJECUTADOS.total()).as("equivalence cases executed (zero = not proven)").isGreaterThan(0);
    }

    // ProcesarClearingFinanciero.java:124-198,261-286,324-452: RV + PH: one ingreso/egreso per group balance; every transaction is stamped with the LAST group's comprobante (legacy defect)
    @Test
    void tesB47_dosCamarasGeneraIngresosEgresosYMarcaMovimientos() {
        verificar(CASOS.get(0));
    }

    // ProcesarClearingFinanciero.java:435-487: all chambers + CAMBIOSCCLV: dividends (CUS.EventoCapitalCliente) and derivative liquidations (CTA.CargoAbonoMovimiento) are stamped as well
    @Test
    void tesB48_todasLasCamarasIncluyeDividendosYDerivados() {
        verificar(CASOS.get(1));
    }

    // ProcesarClearingFinanciero.java:178-182,219-227: no chamber requested -> NO_EXISTEN_MOVIMIENTOS_A_PROCESAR
    @Test
    void tesB49_sinCamarasSolicitadasNoHayMovimientosAProcesar() {
        verificar(CASOS.get(2));
    }

    // ProcesarClearingFinanciero.java:162-165: country without an active chamber -> EMPRESA_LIQUIDADORA_NO_EXISTE
    @Test
    void tesB50_paisSinCamaraActivaEmpresaLiquidadoraNoExiste() {
        verificar(CASOS.get(3));
    }

    // ProcesarClearingFinanciero.java:140,166-169: chamber without a related person -> EMPRESA_LIQUIDADORA_SIN_PERSONA_RELACIONADA
    @Test
    void tesB51_camaraSinPersonaEmpresaLiquidadoraSinPersonaRelacionada() {
        verificar(CASOS.get(4));
    }

    // ProcesarClearingFinanciero.java:170-173: no payment preferences -> PREFERENCIAS_DE_PAGO_CLEARING_NO_CONFIGURADAS
    @Test
    void tesB52_sinPreferenciasDePagoNoConfiguradas() {
        verificar(CASOS.get(5));
    }

    // ProcesarClearingFinanciero.java:162-176: both preconditions failing accumulate two errors before returning
    @Test
    void tesB53_sinCamaraNiPreferenciasAcumulaDosErrores() {
        verificar(CASOS.get(6));
    }

    // ProcesarClearingFinanciero.java:379-384,414-423: no payment preference for the concept/movement/currency -> UI_SIN_PREFERENCIA_PAGO_CLEARING with the 'Clave'
    @Test
    void tesB54_faltaPreferenciaDelConceptoUiSinPreferenciaPagoClearing() {
        verificar(CASOS.get(7));
    }

    // ProcesarClearingFinanciero.java:427-432: CrearIngresoEgreso reporting an error: messages returned, nothing stamped
    @Test
    void tesB55_ingresoEgresoConErrorDevuelveMensajesYNoMarca() {
        verificar(CASOS.get(8));
    }

    // ProcesarClearingFinanciero.java:490-498: an exception of the collaborator: rollback and rethrow
    @Test
    void tesB56_ingresoEgresoLanzaExcepcionHaceRollbackYRelanza() {
        verificar(CASOS.get(9));
    }

    // ProcesarClearingFinanciero.java:199-218: PLA.LocalizacionSistema=PE: the Peruvian path was never migrated, conClearingSalida stays null -> NullPointerException (legacy defect)
    @Test
    void tesB57_localizacionPeNoMigradaLanzaNullPointerException() {
        verificar(CASOS.get(10));
    }

    // ProcesarClearingFinanciero.java:286,440-452: net zero group is filtered out: the stamping then writes NULL to IdComprobanteTesoreria (legacy defect: the WHERE '= 0' still matches)
    @Test
    void tesB58_saldoDeCamaraCeroNoGeneraIngresoYMarcaConNulo() {
        verificar(CASOS.get(11));
    }

    // ProcesarClearingFinanciero.java:375-378,398-403: no portfolio clearing preference: the counterparty bank account fields travel as 0 / empty
    @Test
    void tesB59_sinPreferenciasDePortafolioUsaCuentasEnCero() {
        verificar(CASOS.get(12));
    }

    // ProcesarClearingFinanciero.java:134: PLA.LocalizacionSistema missing: the configuration exception propagates
    @Test
    void tesB60_localizacionNoConfiguradaLanzaExcepcion() {
        verificar(CASOS.get(13));
    }

    // ProcesarClearingFinanciero.java:456-487: DV + RV chambers: dividends and derivative liquidations present
    @Test
    void tesB61_dividendosYDerivadosConCamaraDv() {
        verificar(CASOS.get(14));
    }

    // ProcesarClearingFinanciero.java:219-223: every resume line already has a comprobante: NO_EXISTEN_MOVIMIENTOS_A_PROCESAR
    @Test
    void tesB62_todasLasTransaccionesYaProcesadasNoHayMovimientos() {
        verificar(CASOS.get(15));
    }

    // ProcesarClearingFinanciero.java:80-110,338: UsaCamaraHS=S: group 5 maps to concept CLFIHS
    @Test
    void tesB63_camaraHsAgrupaBajoConceptoClfihs() {
        verificar(CASOS.get(16));
    }

}
