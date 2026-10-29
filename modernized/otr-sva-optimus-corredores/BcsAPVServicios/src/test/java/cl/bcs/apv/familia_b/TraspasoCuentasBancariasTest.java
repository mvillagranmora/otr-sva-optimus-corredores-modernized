package cl.bcs.apv.familia_b;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import cl.bcs.apv.familia_b.support.Caso;
import cl.bcs.apv.familia_b.support.Equivalencia;
import cl.bcs.apv.familia_b.support.ZonaUtc;

/**
 * Characterization of legacy {@code cl.bcs.APV.TraspasoCuentasBancarias} (entry point {@code call(conn, json)}) through the whole modern path:
 * legacy JSON, adapter, REAL use case {@code TraspasarEntreCuentasBancarias} on H2, adapter, legacy JSON. The expected values are RECORDED by executing the unmodified
 * legacy class (legacy-oracle/record.sh); nothing is computed here. Goldens: {@code src/test/resources/apv-b/cases/APV-B-NN_*}.
 * Every method name starts with its behaviour id (BUSINESS_RULES.md has no rule of this module); the comment cites the legacy file:line it pins.
 */
@ExtendWith(ZonaUtc.class)
class TraspasoCuentasBancariasTest {

    static final List<String> CASOS = List.of(
            "APV-B-141_dis-sin-liquidacion-crea-egreso-e-ingreso-apv",
            "APV-B-142_dis-traspaso-usa-conceptos-de-traspaso",
            "APV-B-143_dis-con-liquidacion-automatica-liquida-ambos-comprobantes",
            "APV-B-144_dev-modulo-apv-usa-devrzg-y-no-liquida",
            "APV-B-145_dev-otro-modulo-usa-egrdev-y-liquida",
            "APV-B-146_dbf-usa-conceptos-de-bonificacion",
            "APV-B-147_rez-sin-apertura-usa-monto-informado",
            "APV-B-148_comprobante-con-error-no-liquida-ninguno-y-suma-mensajes",
            "APV-B-149_ingreso-egreso-con-error-continua-con-el-comprobante",
            "APV-B-150_sin-transiciones-configuradas-responde-vacio",
            "APV-B-151_apertura-de-registros-sin-configurar-lanza-excepcion",
            "APV-B-152_liquidacion-automatica-sin-configurar-excepcion-tras-crear",
            "APV-B-153_opcion-nula-lanza-NullPointerException",
            "APV-B-154_fecha-nula-lanza-NullPointerException-antes-de-tesoreria",
            "APV-B-155_comprobante-con-mas-resultados-que-movimientos-excepcion",
            "APV-B-156_fecha-de-sistema-sin-configurar-lanza-excepcion",
            "APV-B-157_origen-recaudacion-reemplaza-las-transiciones",
            "APV-B-158_origen-recaudacion-traspa-usa-la-cuenta-del-traspaso",
            "APV-B-159_origen-recaudacion-sin-registros-lanza-IndexOutOfBounds",
            "APV-B-160_rez-con-apertura-arma-las-transiciones-desde-la-recaudacion");

    private static final Caso.Contador EJECUTADOS = new Caso.Contador();

    @AfterAll
    static void resumen() {
        EJECUTADOS.imprimir("TraspasoCuentasBancarias");
        assertThat(EJECUTADOS.total()).as("equivalence cases executed (zero = not proven)").isGreaterThan(0);
    }

    // TraspasoCuentasBancarias.java:162-173,262-382: DIS: two transitions (ORIGEN then DESTINO): EGRAPV/MOVEGR/EGRTES and INGAPV/MOVING/INGTES with the amount informed, the DESTINO person is the recaudacion person (0 here)
    @Test
    void apvB141_disSinLiquidacionCreaEgresoEIngresoApv() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(0));
    }

    // TraspasoCuentasBancarias.java:165,172: DIS with p_EsTraspaso true uses the concepts ITRAPV / ETRAPV
    @Test
    void apvB142_disTraspasoUsaConceptosDeTraspaso() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(1));
    }

    // TraspasoCuentasBancarias.java:456-472: REC.LiquidacionAutomatica = S (module APV): LiquidarComprobante is called with the comprobantes created
    @Test
    void apvB143_disConLiquidacionAutomaticaLiquidaAmbosComprobantes() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(2));
    }

    // TraspasoCuentasBancarias.java:169,457: DEV from module APV: egress concept DEVRZG, and it is never liquidated even with automatic liquidation
    @Test
    void apvB144_devModuloApvUsaDevrzgYNoLiquida() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(3));
    }

    // TraspasoCuentasBancarias.java:170,457: DEV from another module: EGRDEV, liquidated
    @Test
    void apvB145_devOtroModuloUsaEgrdevYLiquida() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(4));
    }

    // TraspasoCuentasBancarias.java:164,171: DBF: INGBON / EGRBON
    @Test
    void apvB146_dbfUsaConceptosDeBonificacion() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(5));
    }

    // TraspasoCuentasBancarias.java:162,168,185-190: REZ with APV.AbreRegistrosTesoreria = N: INGREZ / EGRREZ and every row takes the informed amount
    @Test
    void apvB147_rezSinAperturaUsaMontoInformado() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(6));
    }

    // TraspasoCuentasBancarias.java:437-454,474: CrearComprobante answers an ERROR: its messages are added (counter 1), no comprobante is prepared for liquidation but LiquidarComprobante is still called (S)
    @Test
    void apvB148_comprobanteConErrorNoLiquidaNingunoYSumaMensajes() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(7));
    }

    // TraspasoCuentasBancarias.java:407-408: CrearIngresoEgreso messages are added to the output and the process continues with the comprobante
    @Test
    void apvB149_ingresoEgresoConErrorContinuaConElComprobante() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(8));
    }

    // TraspasoCuentasBancarias.java:183,384-386: no transition for the operation: the service answers with empty lists and calls no collaborator
    @Test
    void apvB150_sinTransicionesConfiguradasRespondeVacio() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(9));
    }

    // TraspasoCuentasBancarias.java:157: APV.AbreRegistrosTesoreria missing: Exception before any query
    @Test
    void apvB151_aperturaDeRegistrosSinConfigurarLanzaExcepcion() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(10));
    }

    // TraspasoCuentasBancarias.java:456: REC.LiquidacionAutomatica (module APV) is read AFTER creating movimientos and comprobantes: the Exception escapes and those collaborator calls are already done
    @Test
    void apvB152_liquidacionAutomaticaSinConfigurarExcepcionTrasCrear() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(11));
    }

    // TraspasoCuentasBancarias.java:162: p_Opcion null: NullPointerException
    @Test
    void apvB153_opcionNulaLanzaNullPointerException() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(12));
    }

    // TraspasoCuentasBancarias.java:377: p_Fecha null: NullPointerException while building the first payment form, before Tesoreria is called
    @Test
    void apvB154_fechaNulaLanzaNullPointerExceptionAntesDeTesoreria() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(13));
    }

    // TraspasoCuentasBancarias.java:445-449: the IdComprobante result index is used on the movement list: 3 results for 2 movements, IndexOutOfBoundsException
    @Test
    void apvB155_comprobanteConMasResultadosQueMovimientosExcepcion() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(14));
    }

    // TraspasoCuentasBancarias.java:156: PLATAFORMA/PLA.FechaSistema missing: Exception
    @Test
    void apvB156_fechaDeSistemaSinConfigurarLanzaExcepcion() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(15));
    }

    // TraspasoCuentasBancarias.java:202-260: with p_OrigenRecaudacion the ORIGEN row comes from the recaudacion query (amount = convenido + APV) and the DESTINO transition is appended; the DESTINO person is the recaudacion client
    @Test
    void apvB157_origenRecaudacionReemplazaLasTransiciones() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(16));
    }

    // TraspasoCuentasBancarias.java:329-331: recaudacion origin TRASPA and an INGTES row: the corredora bank account is the account of the recaudacion row (IdCuentaBancoTraspaso)
    @Test
    void apvB158_origenRecaudacionTraspaUsaLaCuentaDelTraspaso() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(17));
    }

    // TraspasoCuentasBancarias.java:216-217: recaudacion ids without a REC.Cliente row: PersonaRecaudacion.get(0) fails (IndexOutOfBoundsException)
    @Test
    void apvB159_origenRecaudacionSinRegistrosLanzaIndexOutOfBounds() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(18));
    }

    // TraspasoCuentasBancarias.java:224-242,484-509: REZ with apertura S: the transitions are rebuilt from the recaudacion rows and filled by llenadoTransicionArmada (IdTipoCuentaBancariaSocio takes the bank account type, a legacy quirk)
    @Test
    void apvB160_rezConAperturaArmaLasTransicionesDesdeLaRecaudacion() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(19));
    }
}
