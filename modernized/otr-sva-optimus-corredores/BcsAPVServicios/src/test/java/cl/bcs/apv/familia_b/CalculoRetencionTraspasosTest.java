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
 * Characterization of legacy {@code cl.bcs.APV.CalculoRetencionTraspasos} (entry point {@code call(conn, json)}) through the whole modern path:
 * legacy JSON, adapter, REAL use case {@code CalcularRetencionTraspasos} on H2, adapter, legacy JSON. The expected values are RECORDED by executing the unmodified
 * legacy class (legacy-oracle/record.sh); nothing is computed here. Goldens: {@code src/test/resources/apv-b/cases/APV-B-NN_*}.
 * Every method name starts with its behaviour id (BUSINESS_RULES.md has no rule of this module); the comment cites the legacy file:line it pins.
 */
@ExtendWith(ZonaUtc.class)
class CalculoRetencionTraspasosTest {

    static final List<String> CASOS = List.of(
            "APV-B-37_retencion-orden-rv-facturada-descuenta-quince-por-ciento",
            "APV-B-38_retencion-fondo-mutuo-ejecutado-usa-monto-de-la-solicitud",
            "APV-B-39_retencion-sin-configuracion-vigente-no-retiene",
            "APV-B-40_retencion-razon-de-retiro-distinta-no-retiene",
            "APV-B-41_retencion-regimen-igual-al-id-de-matriz-retiene",
            "APV-B-42_retencion-regimen-igual-al-regimen-de-matriz-no-retiene",
            "APV-B-43_retencion-porcentaje-cero-se-ignora",
            "APV-B-44_retencion-dos-configuraciones-se-aplican-en-cascada",
            "APV-B-45_retencion-suma-dos-traspasos-y-dos-ordenes",
            "APV-B-46_retencion-traspaso-sin-ordenes-devuelve-ceros",
            "APV-B-47_retencion-orden-rv-no-facturada-se-ignora",
            "APV-B-48_retencion-orden-en-ambas-vistas-se-cuenta-dos-veces",
            "APV-B-49_retencion-portafolio-inactivo-se-excluye",
            "APV-B-50_retencion-sin-parametros-devuelve-ceros",
            "APV-B-51_retencion-error-en-el-calculo-informa-mensaje-sin-tipo",
            "APV-B-52_retencion-traspaso-nulo-no-encuentra-ordenes");

    private static final Caso.Contador EJECUTADOS = new Caso.Contador();

    @AfterAll
    static void resumen() {
        EJECUTADOS.imprimir("CalculoRetencionTraspasos");
        assertThat(EJECUTADOS.total()).as("equivalence cases executed (zero = not proven)").isGreaterThan(0);
    }

    // CalculoRetencionTraspasos.java:150-175,190: RV order of the invoiced assignment, 15% retention: discount 850000, retention 150000, total 1000000
    @Test
    void apvB37_retencionOrdenRvFacturadaDescuentaQuincePorCiento() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(0));
    }

    // CalculoRetencionTraspasos.java:150: MontoOrden 0 in the FFMM branch of ConsultaOrdenesTraspaso.sql: the FFMM amount is used
    @Test
    void apvB38_retencionFondoMutuoEjecutadoUsaMontoDeLaSolicitud() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(1));
    }

    // CalculoRetencionTraspasos.java:158-171: only configurations in state VIG apply (ConsultarRetencion.sql)
    @Test
    void apvB39_retencionSinConfiguracionVigenteNoRetiene() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(2));
    }

    // CalculoRetencionTraspasos.java:160: RazonRetiro must equal CodigoRetiro
    @Test
    void apvB40_retencionRazonDeRetiroDistintaNoRetiene() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(3));
    }

    // CalculoRetencionTraspasos.java:163-164: a configuration IdRegimenTributario is compared with the portafolio's IdMatrizAgrupadorAPV (not with the matrix regimen): equal ids retain
    @Test
    void apvB41_retencionRegimenIgualAlIdDeMatrizRetiene() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(4));
    }

    // CalculoRetencionTraspasos.java:163-164: a configuration equal to the matrix REGIMEN (30) but not to the matrix id (10) does not retain
    @Test
    void apvB42_retencionRegimenIgualAlRegimenDeMatrizNoRetiene() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(5));
    }

    // CalculoRetencionTraspasos.java:161: a zero percentage is skipped
    @Test
    void apvB43_retencionPorcentajeCeroSeIgnora() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(6));
    }

    // CalculoRetencionTraspasos.java:158-171: two matching configurations apply one after the other on the reduced amount and the reported retention is only the LAST one (total 900000 instead of 1000000)
    @Test
    void apvB44_retencionDosConfiguracionesSeAplicanEnCascada() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(7));
    }

    // CalculoRetencionTraspasos.java:141-175: amounts of several traspasos and of RV and FFMM orders are added up
    @Test
    void apvB45_retencionSumaDosTraspasosYDosOrdenes() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(8));
    }

    // CalculoRetencionTraspasos.java:127-129,190: a traspaso without orders gives zeros
    @Test
    void apvB46_retencionTraspasoSinOrdenesDevuelveCeros() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(9));
    }

    // ConsultaOrdenesTraspaso.sql: an RV order counts only when its movimiento comprobante is in facturation state FAC
    @Test
    void apvB47_retencionOrdenRvNoFacturadaSeIgnora() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(10));
    }

    // ConsultaOrdenesTraspaso.sql (UNION ALL): the same id matched by the RV and by the FFMM branch is counted twice
    @Test
    void apvB48_retencionOrdenEnAmbasVistasSeCuentaDosVeces() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(11));
    }

    // ConsultaOrdenesTraspaso.sql: orders of an inactive (INA) portafolio are excluded
    @Test
    void apvB49_retencionPortafolioInactivoSeExcluye() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(12));
    }

    // CalculoRetencionTraspasos.java:127-129,190: no traspasos: zeros
    @Test
    void apvB50_retencionSinParametrosDevuelveCeros() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(13));
    }

    // CalculoRetencionTraspasos.java:178-188: an error while computing (null percentage) is caught: message with code ERROR and NO type, so call() counts 0 errors; the amounts are the zeros of the start
    @Test
    void apvB51_retencionErrorEnElCalculoInformaMensajeSinTipo() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(14));
    }

    // CalculoRetencionTraspasos.java:141-146: IdTraspaso null binds NULL: no orders, zeros
    @Test
    void apvB52_retencionTraspasoNuloNoEncuentraOrdenes() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(15));
    }
}
