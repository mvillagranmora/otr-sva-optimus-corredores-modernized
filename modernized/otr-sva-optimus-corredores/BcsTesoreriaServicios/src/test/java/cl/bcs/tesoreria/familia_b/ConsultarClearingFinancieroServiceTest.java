package cl.bcs.tesoreria.familia_b;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import cl.bcs.tesoreria.familia_b.support.Caso;
import cl.bcs.tesoreria.familia_b.support.ConfiguracionDelCaso;
import cl.bcs.tesoreria.familia_b.support.Equivalencia;
import cl.bcs.tesoreria.familia_b.support.ZonaUtc;
import cl.bcs.tesoreria.javacall.liq.ConsultarClearingFinancieroService;

/**
 * Characterization of legacy {@code cl.bcs.tesoreria.liq.ConsultarClearingFinanciero} (entry point {@code call(conn, json)}) as
 * {@code ConsultarClearingFinancieroService#execute(JsonObject)}: groups the day's market transactions, dividends, derivative liquidations
 * and simultaneous-operation advances by clearing house (RV, PM, PH, SM, DV, HS) and counterparty.
 * 
 * <p>Goldens: {@code src/test/resources/tesoreria-b/cases/TES-B-23..46}. The resume list comes from a legacy HashMap (iteration order is an
 * implementation detail), so P_OUT_CLEARINGFINANCIERORESUMEN is compared as a set. Configuration (TESORERIA/UsaCamaraHS, PLATAFORMA/PLA.FechaSistema) has no seam in
 * the modern service yet (see README).
 */
@ExtendWith(ZonaUtc.class)
class ConsultarClearingFinancieroServiceTest {

    static final List<String> CASOS = List.of(
            "TES-B-23_todas-las-camaras-sin-cambios-cclv",
            "TES-B-24_todas-las-camaras-con-cambios-cclv",
            "TES-B-25_solo-camara-rv-con-dividendos",
            "TES-B-26_solo-camara-rv-con-cambios-cclv",
            "TES-B-27_solo-camara-pm-sin-cambios-cclv-incluye-rf",
            "TES-B-28_solo-camara-pm-con-cambios-cclv-incluye-if",
            "TES-B-29_solo-camara-pm-sin-parametros-de-inclusion",
            "TES-B-30_solo-camara-ph-incluye-rf-e-if",
            "TES-B-31_solo-camara-ph-incluye-solo-if",
            "TES-B-32_solo-camara-sm-simultaneas-y-anticipos-del-dia",
            "TES-B-33_camara-sm-fecha-anterior-usa-anticipos-historicos",
            "TES-B-34_camara-sm-fecha-posterior-usa-anticipos-vigentes",
            "TES-B-35_camara-dv-con-liquidaciones-de-derivados",
            "TES-B-36_sin-camaras-solicitadas-no-hay-detalle",
            "TES-B-37_camara-desconocida-se-ignora",
            "TES-B-38_corredor-no-existe-UI_CORREDOR_NO_EXISTE",
            "TES-B-39_usa-camara-hs-con-rf-if-t2-y-ph-sin-rv-ni-dv",
            "TES-B-40_usa-camara-hs-con-todas-las-camaras-y-cambios-cclv",
            "TES-B-41_usa-camara-hs-sin-rv-con-dv",
            "TES-B-42_usa-camara-hs-con-rv-sin-dv",
            "TES-B-43_usa-camara-hs-solo-ph-con-t0-rf-if",
            "TES-B-44_usa-camara-hs-solo-pm-con-cambios-cclv",
            "TES-B-45_operacion-sin-tipo-conocido-lanza-NullPointerException",
            "TES-B-46_fecha-sistema-no-configurada-con-camara-sm");

    private static final Caso.Contador EJECUTADOS = new Caso.Contador();

    private static final String[] SIN_ORDEN = {"P_OUT_CLEARINGFINANCIERORESUMEN"};

    private static void verificar(String nombre) {
        Equivalencia.verificar(EJECUTADOS, nombre, caso -> new ConsultarClearingFinancieroService(caso.jdbc(), new ConfiguracionDelCaso(caso)).execute(caso.entrada().deepCopy()), SIN_ORDEN);
    }

    @AfterAll
    static void resumen() {
        EJECUTADOS.imprimir("ConsultarClearingFinanciero");
        assertThat(EJECUTADOS.total()).as("equivalence cases executed (zero = not proven)").isGreaterThan(0);
    }

    // ConsultarClearingFinanciero.java:448-466,650-726,733-754,818-841,871-920: every chamber, CAMBIOSCCLV absent: RF/IF T1 and RV T2 -> 'RV + PM', T0 -> 'PH + SM', DV, dividends under 'RV + PM' with code 2 (legacy quirk), simultaneous advances
    @Test
    void tesB23_todasLasCamarasSinCambiosCclv() {
        verificar(CASOS.get(0));
    }

    // ConsultarClearingFinanciero.java:674-682,692-700,739-742: CAMBIOSCCLV=S: PM -> 'PM'(1) and RV -> 'RV'(3)
    @Test
    void tesB24_todasLasCamarasConCambiosCclv() {
        verificar(CASOS.get(1));
    }

    // ConsultarClearingFinanciero.java:692-704,733-754: RV only: RV T2 transactions plus the day's dividends
    @Test
    void tesB25_soloCamaraRvIncluyeDividendos() {
        verificar(CASOS.get(2));
    }

    // ConsultarClearingFinanciero.java:692-700,739-742: RV only with CAMBIOSCCLV=S
    @Test
    void tesB26_soloCamaraRvConCambiosCclv() {
        verificar(CASOS.get(3));
    }

    // ConsultarClearingFinanciero.java:674-689: PM only, RF included, no CAMBIOSCCLV -> group 'RV + PM'
    @Test
    void tesB27_soloCamaraPmSinCambiosCclvIncluyeRf() {
        verificar(CASOS.get(4));
    }

    // ConsultarClearingFinanciero.java:674-689: PM only, IF included, CAMBIOSCCLV=S -> group 'PM'
    @Test
    void tesB28_soloCamaraPmConCambiosCclvIncluyeIf() {
        verificar(CASOS.get(5));
    }

    // ConsultarClearingFinanciero.java:684-688: PM without CLEARINGINCLUYE* parameters includes nothing
    @Test
    void tesB29_soloCamaraPmSinParametrosDeInclusionNoIncluyeNada() {
        verificar(CASOS.get(6));
    }

    // ConsultarClearingFinanciero.java:650-671: PH only, RF and IF included: T0 transactions in 'PH + SM'
    @Test
    void tesB30_soloCamaraPhIncluyeRfEIf() {
        verificar(CASOS.get(7));
    }

    // ConsultarClearingFinanciero.java:662-671: PH only, only IF included
    @Test
    void tesB31_soloCamaraPhIncluyeSoloIf() {
        verificar(CASOS.get(8));
    }

    // ConsultarClearingFinanciero.java:707-717,871-920: SM only: SIM T0 transactions plus the current advances (system date)
    @Test
    void tesB32_soloCamaraSmSimultaneasYAnticiposDelDia() {
        verificar(CASOS.get(9));
    }

    // ConsultarClearingFinanciero.java:879-883: SM with a liquidation date before the system date reads the historical advances (FechaRespaldo)
    @Test
    void tesB33_camaraSmFechaAnteriorUsaAnticiposHistoricos() {
        verificar(CASOS.get(10));
    }

    // ConsultarClearingFinanciero.java:879-880: SM with a liquidation date after the system date reads the current advances
    @Test
    void tesB34_camaraSmFechaPosteriorUsaAnticiposVigentes() {
        verificar(CASOS.get(11));
    }

    // ConsultarClearingFinanciero.java:719-726,818-841,978: DV: derivative transactions plus the cartola liquidations (cancelled / other concepts / non DV excluded)
    @Test
    void tesB35_camaraDvConLiquidacionesDeDerivados() {
        verificar(CASOS.get(12));
    }

    // ConsultarClearingFinanciero.java:448-466: no chamber requested: empty detail
    @Test
    void tesB36_sinCamarasSolicitadasNoHayDetalle() {
        verificar(CASOS.get(13));
    }

    // ConsultarClearingFinanciero.java:449-465: a chamber code that is not RV/PM/PH/SM/DV does not set any validator
    @Test
    void tesB37_camaraDesconocidaSeIgnora() {
        verificar(CASOS.get(14));
    }

    // ConsultarClearingFinanciero.java:443-445: company without brokerage data -> error message UI_CORREDOR_NO_EXISTE
    @Test
    void tesB38_corredorInexistenteInformaUiCorredorNoExiste() {
        verificar(CASOS.get(15));
    }

    // ConsultarClearingFinanciero.java:530-538,563-577,597-608: UsaCamaraHS=S: RF/IF T2 in PH without RV/DV chambers goes to group HS
    @Test
    void tesB39_usaCamaraHsRfIfT2EnPhSinRvNiDv() {
        verificar(CASOS.get(16));
    }

    // ConsultarClearingFinanciero.java:540-548,611-645: UsaCamaraHS=S with all chambers and CAMBIOSCCLV=S
    @Test
    void tesB40_usaCamaraHsConTodasLasCamarasYCambiosCclv() {
        verificar(CASOS.get(17));
    }

    // ConsultarClearingFinanciero.java:530-538,638-645: UsaCamaraHS=S without RV, with DV
    @Test
    void tesB41_usaCamaraHsSinRvConDv() {
        verificar(CASOS.get(18));
    }

    // ConsultarClearingFinanciero.java:550-558,611-623: UsaCamaraHS=S with RV and without DV
    @Test
    void tesB42_usaCamaraHsConRvSinDv() {
        verificar(CASOS.get(19));
    }

    // ConsultarClearingFinanciero.java:563-577: UsaCamaraHS=S, PH only: T0 RF/IF -> HS
    @Test
    void tesB43_usaCamaraHsSoloPhConT0RfIf() {
        verificar(CASOS.get(20));
    }

    // ConsultarClearingFinanciero.java:580-594: UsaCamaraHS=S, PM only with CAMBIOSCCLV=S: T1 RF/IF -> HS
    @Test
    void tesB44_usaCamaraHsSoloPmConCambiosCclv() {
        verificar(CASOS.get(21));
    }

    // ConsultarClearingFinanciero.java:400-401: an operation type with no INS.TipoOperacion row leaves AccionTipoOperacion null: NullPointerException (legacy defect)
    @Test
    void tesB45_operacionSinTipoConocidoLanzaNullPointerException() {
        verificar(CASOS.get(22));
    }

    // ConsultarClearingFinanciero.java:434-440,879: PLA.FechaSistema missing: the date falls back to 0001-01-01 and the current advances are read
    @Test
    void tesB46_fechaSistemaNoConfiguradaUsaAnticiposVigentes() {
        verificar(CASOS.get(23));
    }

}
