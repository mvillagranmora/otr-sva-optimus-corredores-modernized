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
 * Characterization of legacy {@code cl.bcs.APV.CrearTraspasoTesoreria} (entry point {@code call(conn, json)}) through the whole modern path:
 * legacy JSON, adapter, REAL use case {@code CrearTraspasoTesoreria} on H2, adapter, legacy JSON. The expected values are RECORDED by executing the unmodified
 * legacy class (legacy-oracle/record.sh); nothing is computed here. Goldens: {@code src/test/resources/apv-b/cases/APV-B-NN_*}.
 * Every method name starts with its behaviour id (BUSINESS_RULES.md has no rule of this module); the comment cites the legacy file:line it pins.
 */
@ExtendWith(ZonaUtc.class)
class CrearTraspasoTesoreriaTest {

    static final List<String> CASOS = List.of(
            "APV-B-161_tes-rv-con-saldo-crea-movimientos-y-comprobante",
            "APV-B-162_tes-rv-con-retencion-agrega-movimientos-de-retencion",
            "APV-B-163_tes-retencion-sin-regimen-aplica-a-toda-orden",
            "APV-B-164_tes-retencion-regimen-igual-ids-pequenos-retiene",
            "APV-B-165_tes-retencion-regimen-igual-ids-mayores-a-127-no-retiene",
            "APV-B-166_tes-fondo-mutuo-usa-el-monto-de-la-solicitud",
            "APV-B-167_tes-saldo-insuficiente-no-genera-movimientos",
            "APV-B-168_tes-rescate-de-regimen-dc-se-omite",
            "APV-B-169_tes-portafolio-distinto-al-de-la-cuenta",
            "APV-B-170_tes-cargo-abono-con-errores-no-copia-mensajes",
            "APV-B-171_tes-ingreso-egreso-con-error-corta-sin-comprobante",
            "APV-B-172_tes-comprobante-con-error-copia-mensajes-y-corta",
            "APV-B-173_tes-dos-traspasos-acumulan-movimientos",
            "APV-B-174_tes-cliente-sin-portafolio-apv-advierte-y-corta",
            "APV-B-175_tes-sin-transicion-dev-lanza-IndexOutOfBounds",
            "APV-B-176_tes-sin-portafolio-empresa-lanza-IndexOutOfBounds",
            "APV-B-177_tes-traspaso-sin-contrato-lanza-IndexOutOfBounds",
            "APV-B-178_tes-procedimiento-sin-saldos-lanza-NullPointer",
            "APV-B-179_tes-procedimiento-lanza-excepcion-sql",
            "APV-B-180_tes-sin-parametros-llama-a-los-tres-servicios-vacios",
            "APV-B-250_tes-saldo-igual-al-monto-genera-movimientos",
            "APV-B-360_tes-mixta-rv-y-ffmm-sin-retencion",
            "APV-B-361_tes-mixta-con-una-retencion-sin-regimen-retiene-ambas",
            "APV-B-362_tes-mixta-dos-retenciones-se-acumulan-sobre-el-monto",
            "APV-B-363_tes-mixta-retencion-por-regimen-solo-alcanza-a-la-rv",
            "APV-B-364_tes-mixta-retencion-de-otro-codigo-de-retiro-no-aplica",
            "APV-B-365_tes-mixta-retencion-con-porcentaje-cero-se-ignora",
            "APV-B-366_tes-mixta-saldo-insuficiente-solo-para-la-ffmm",
            "APV-B-367_tes-mixta-rescate-dc-omitido-y-ffmm-apv-pasa",
            "APV-B-368_tes-mixta-dos-traspasos-una-orden-de-cada-tipo");

    private static final Caso.Contador EJECUTADOS = new Caso.Contador();

    @AfterAll
    static void resumen() {
        EJECUTADOS.imprimir("CrearTraspasoTesoreria");
        assertThat(EJECUTADOS.total()).as("equivalence cases executed (zero = not proven)").isGreaterThan(0);
    }

    // CrearTraspasoTesoreria.java:347-368,379-597: an RV order with enough balance: CARAPV (client portafolio) + ABOAPV (corredora default portafolio), one TREAPV egress with its payment forms, one PAREGR comprobante; only the cargo/abono messages come back (p_IngresoEgreso/p_FormaDePago of the output are never filled)
    @Test
    void apvB161_tesRvConSaldoCreaMovimientosYComprobante() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(0));
    }

    // CrearTraspasoTesoreria.java:388-406,600-626: a configured retention (15%) reduces the amounts (850000) and adds two EGRRET cargo/abono pairs numbered after the main pair (the abono is listed first)
    @Test
    void apvB162_tesRvConRetencionAgregaMovimientosDeRetencion() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(1));
    }

    // CrearTraspasoTesoreria.java:396: a retention configuration without IdRegimenTributario (null) applies to every order
    @Test
    void apvB163_tesRetencionSinRegimenAplicaATodaOrden() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(2));
    }

    // CrearTraspasoTesoreria.java:396-397: IdRegimenTributario == IdRegimenTributarioMatriz is a Long reference comparison: equal small ids (10, inside the Long cache) match
    @Test
    void apvB164_tesRetencionRegimenIgualIdsPequenosRetiene() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(3));
    }

    // CrearTraspasoTesoreria.java:396-397: the same comparison with ids above 127 (200): two distinct Long objects, == is false and nothing is retained
    @Test
    void apvB165_tesRetencionRegimenIgualIdsMayoresA127NoRetiene() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(4));
    }

    // CrearTraspasoTesoreria.java:363: the FFMM order amount is used when the RV amount is null/0
    @Test
    void apvB166_tesFondoMutuoUsaElMontoDeLaSolicitud() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(5));
    }

    // CrearTraspasoTesoreria.java:368: SaldoDisponible below the order amount: no cargo/abono nor tesoreria rows, but CrearCargoAbono, CrearIngresoEgreso and CrearComprobante are still called (empty movements)
    @Test
    void apvB167_tesSaldoInsuficienteNoGeneraMovimientos() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(6));
    }

    // CrearTraspasoTesoreria.java:350-358: RazonRetiro RES in a regimen containing DC is skipped
    @Test
    void apvB168_tesRescateDeRegimenDcSeOmite() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(7));
    }

    // CrearTraspasoTesoreria.java:368: an order whose portafolio differs from the cuenta de inversion row generates nothing
    @Test
    void apvB169_tesPortafolioDistintoAlDeLaCuenta() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(8));
    }

    // CrearTraspasoTesoreria.java:690-701: CrearCargoAbono answers errors: its messages are NOT copied to the output; the process continues
    @Test
    void apvB170_tesCargoAbonoConErroresNoCopiaMensajes() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(9));
    }

    // CrearTraspasoTesoreria.java:717-724: CrearIngresoEgreso answers an ERROR: its messages are returned and the service ends before the comprobante
    @Test
    void apvB171_tesIngresoEgresoConErrorCortaSinComprobante() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(10));
    }

    // CrearTraspasoTesoreria.java:768-778: CrearComprobante answers an ERROR: the messages are copied with id = contadorIds and the service ends
    @Test
    void apvB172_tesComprobanteConErrorCopiaMensajesYCorta() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(11));
    }

    // CrearTraspasoTesoreria.java:261-673: two traspasos: counters of cargo/abono and ingreso/egreso run on across them and both contracts are looked up
    @Test
    void apvB173_tesDosTraspasosAcumulanMovimientos() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(12));
    }

    // CrearTraspasoTesoreria.java:263-277: the client has no APV portafolio: ADVERTENCIA-like message of type 'Warning' (not counted as error) and the service ends before any collaborator
    @Test
    void apvB174_tesClienteSinPortafolioApvAdvierteYCorta() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(13));
    }

    // CrearTraspasoTesoreria.java:240-241: no DEV row in APV.OrigenDestinoTransicion: _CtaBco.get(0) fails (IndexOutOfBoundsException)
    @Test
    void apvB175_tesSinTransicionDevLanzaIndexOutOfBounds() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(14));
    }

    // CrearTraspasoTesoreria.java:252,258: the company has no default portafolio row: _DatosEmpresa.get(0) fails (IndexOutOfBoundsException)
    @Test
    void apvB176_tesSinPortafolioEmpresaLanzaIndexOutOfBounds() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(15));
    }

    // CrearTraspasoTesoreria.java:663-665: traspaso without a row in TRA.Traspaso: _IdContratoSAAPV.get(0) fails after the movements were prepared
    @Test
    void apvB177_tesTraspasoSinContratoLanzaIndexOutOfBounds() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(16));
    }

    // CrearTraspasoTesoreria.java:333-347: the stored procedure answers no p_out_SaldoPorFechaMovimiento table: the for-each over null throws NullPointerException
    @Test
    void apvB178_tesProcedimientoSinSaldosLanzaNullPointer() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(17));
    }

    // CrearTraspasoTesoreria.java:333: the stored procedure fails (SQLException): propagates, no collaborator is called
    @Test
    void apvB179_tesProcedimientoLanzaExcepcionSql() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(18));
    }

    // CrearTraspasoTesoreria.java:674-701,750-778: no traspasos: the three collaborators are still called with empty lists
    @Test
    void apvB180_tesSinParametrosLlamaALosTresServiciosVacios() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(19));
    }

    // CrearTraspasoTesoreria.java:368: SaldoDisponible EXACTLY equal to the order amount passes the >= test: the movements are generated (boundary that a '>' mutation breaks)
    @Test
    void apvB250_tesSaldoIgualAlMontoGeneraMovimientos() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(20));
    }

    // CrearTraspasoTesoreria.java:347-368,379-597: one RV order (portafolio 5001) and one FFMM order (5002) of the same traspaso generate two cargo/abono pairs and two egresos without retention
    @Test
    void apvB360_tesMixtaRvYFfmmSinRetencion() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(21));
    }

    // CrearTraspasoTesoreria.java:388-406: a retention without regimen (15%) applies to the RV and to the FFMM order: both amounts are reduced and each gets its EGRRET pair
    @Test
    void apvB361_tesMixtaConUnaRetencionSinRegimenRetieneAmbas() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(22));
    }

    // CrearTraspasoTesoreria.java:388-406: two retentions (15% then 10%) are applied one after the other to the SAME amount (765000), but only the LAST retention amount (85000) is reported in the EGRRET pair
    @Test
    void apvB362_tesMixtaDosRetencionesSeAcumulanSobreElMonto() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(23));
    }

    // CrearTraspasoTesoreria.java:396-397: retention by regimen: the one of regimen 10 (Long cache) reaches the RV order, the one of regimen 200 (distinct Long objects) never reaches the FFMM order
    @Test
    void apvB363_tesMixtaRetencionPorRegimenSoloAlcanzaALaRv() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(24));
    }

    // CrearTraspasoTesoreria.java:392-393: a retention whose CodigoRetiro (RES) differs from the order's (TRA) is ignored
    @Test
    void apvB364_tesMixtaRetencionDeOtroCodigoDeRetiroNoAplica() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(25));
    }

    // CrearTraspasoTesoreria.java:393: a retention with percentage 0 is ignored
    @Test
    void apvB365_tesMixtaRetencionConPorcentajeCeroSeIgnora() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(26));
    }

    // CrearTraspasoTesoreria.java:368: the RV order has no enough balance and is skipped while the FFMM order of the same traspaso is generated (numbers of cargo/abono restart at 1)
    @Test
    void apvB366_tesMixtaSaldoInsuficienteSoloParaLaFfmm() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(27));
    }

    // CrearTraspasoTesoreria.java:347-358,368: in a RES traspaso the RV order of the DC regimen (portafolio 5002) is skipped while the FFMM order of the APV-A regimen (5001) passes
    @Test
    void apvB367_tesMixtaRescateDcOmitidoYFfmmApvPasa() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(28));
    }

    // CrearTraspasoTesoreria.java:263-368: two traspasos, one RV (5001) and one FFMM (5002) order: the procedure is called per traspaso, retention applies to both and the movement numbers run across the two
    @Test
    void apvB368_tesMixtaDosTraspasosUnaOrdenDeCadaTipo() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(29));
    }
}
