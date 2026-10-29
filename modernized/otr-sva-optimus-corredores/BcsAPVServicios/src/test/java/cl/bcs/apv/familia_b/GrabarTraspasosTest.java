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
 * Characterization of legacy {@code cl.bcs.APV.GrabarTraspasos} (entry point {@code call(conn, json)}) through the whole modern path:
 * legacy JSON, adapter, REAL use case {@code GrabarTraspasos} on H2, adapter, legacy JSON. The expected values are RECORDED by executing the unmodified
 * legacy class (legacy-oracle/record.sh); nothing is computed here. Goldens: {@code src/test/resources/apv-b/cases/APV-B-NN_*}.
 * Every method name starts with its behaviour id (BUSINESS_RULES.md has no rule of this module); the comment cites the legacy file:line it pins.
 */
@ExtendWith(ZonaUtc.class)
class GrabarTraspasosTest {

    static final List<String> CASOS = List.of(
            "APV-B-181_gt-portafolio-existente-aprueba-y-pasa-a-ING",
            "APV-B-182_gt-sin-aprobacion-automatica-pasa-a-PDA-y-crea-evento",
            "APV-B-183_gt-evento-con-advertencias-y-errores-informa-ambos",
            "APV-B-184_gt-sin-portafolio-lo-crea-con-el-procedimiento",
            "APV-B-185_gt-procedimiento-con-errores-no-crea-el-detalle",
            "APV-B-186_gt-cliente-bloqueado-informa-una-vez-y-omite-filas",
            "APV-B-187_gt-portafolio-bloqueado-informa-y-omite-la-fila",
            "APV-B-188_gt-procedimiento-devuelve-portafolio-inactivo-y-lo-reactiva",
            "APV-B-189_gt-contrato-cero-no-relaciona-y-informa-error",
            "APV-B-190_gt-dos-regimenes-uno-existente-y-uno-nuevo",
            "APV-B-191_gt-regimen-bf-usa-origen-de-ahorro-BF",
            "APV-B-192_gt-regimen-corto-lanza-StringIndexOutOfBounds",
            "APV-B-193_gt-sin-traspasos-no-hace-nada",
            "APV-B-194_gt-aprobacion-automatica-sin-configurar-excepcion",
            "APV-B-195_gt-secuencia-en-cero-no-crea-detalle",
            "APV-B-196_gt-error-previo-impide-detalles-de-otras-filas");

    private static final Caso.Contador EJECUTADOS = new Caso.Contador();

    @AfterAll
    static void resumen() {
        EJECUTADOS.imprimir("GrabarTraspasos");
        assertThat(EJECUTADOS.total()).as("equivalence cases executed (zero = not proven)").isGreaterThan(0);
    }

    // GrabarTraspasos.java:240-298,370-425,429-448,535-559: the real ValidaContratoSAAPV finds the portafolio; detalle (VIG, N), PortafolioContratoSAAPV (origin APV, UNI, current period) and traspaso state ING (automatic approval S); every message carries the last detalle id
    @Test
    void apvB181_gtPortafolioExistenteApruebaYPasaAING() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(0));
    }

    // GrabarTraspasos.java:458-534: APV.ApruebaTraspasoAutomatico = N: the traspaso goes to PDA and CrearEventoAutorizacionMasivo is called with an INGTRA event
    @Test
    void apvB182_gtSinAprobacionAutomaticaPasaAPDAYCreaEvento() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(1));
    }

    // GrabarTraspasos.java:517-533: the authorization event answers warnings and errors: they are appended as ADVERTENCIA / ERROR messages (counter 1)
    @Test
    void apvB183_gtEventoConAdvertenciasYErroresInformaAmbos() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(2));
    }

    // GrabarTraspasos.java:300-368: no portafolio for the regimen: the stored procedure CrearPortafolio creates it (RefId 6001), message CREACION_PORTAFOLIO_OK and the detalle uses it
    @Test
    void apvB184_gtSinPortafolioLoCreaConElProcedimiento() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(3));
    }

    // GrabarTraspasos.java:340-358,429: the stored procedure answers errors: ERROR and ADVERTENCIA messages with the detalle id; an ERROR prevents the detalle/contrato rows and the state change
    @Test
    void apvB185_gtProcedimientoConErroresNoCreaElDetalle() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(4));
    }

    // GrabarTraspasos.java:195-231: a BLOQUEADO client is reported once (ClienteAnterior) and all its rows are skipped
    @Test
    void apvB186_gtClienteBloqueadoInformaUnaVezYOmiteFilas() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(5));
    }

    // GrabarTraspasos.java:280-298: the found portafolio is BLO: PORTAFOLIO_BLOQUEADO (dictionary text) and the row is skipped
    @Test
    void apvB187_gtPortafolioBloqueadoInformaYOmiteLaFila() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(6));
    }

    // GrabarTraspasos.java:360-380: when the created portafolio id is an existing INA portafolio it is reactivated (ModificarEstadoPortafolioTraspaso: ACT/AUT)
    @Test
    void apvB188_gtProcedimientoDevuelvePortafolioInactivoYLoReactiva() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(7));
    }

    // GrabarTraspasos.java:402-424,440-456: IdContratoSAAPV 0 builds no PortafolioContratoSAAPV row: the batch has no rows and answers ERROR_CREAR_PORTAFOLIO_CONTRATO
    @Test
    void apvB189_gtContratoCeroNoRelacionaYInformaError() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(8));
    }

    // GrabarTraspasos.java:209-426: two regimens: one portafolio found, one created by the stored procedure; two detalles and two relations; messages carry the last id
    @Test
    void apvB190_gtDosRegimenesUnoExistenteYUnoNuevo() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(9));
    }

    // GrabarTraspasos.java:405-407: RegimenTributario BF gives OrigenAhorro BF
    @Test
    void apvB191_gtRegimenBfUsaOrigenDeAhorroBF() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(10));
    }

    // GrabarTraspasos.java:408: a regimen shorter than 3 characters: substring(0,3) throws StringIndexOutOfBoundsException
    @Test
    void apvB192_gtRegimenCortoLanzaStringIndexOutOfBounds() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(11));
    }

    // GrabarTraspasos.java:184,194,429: no traspasos: no message, getSecuencia asked for 0 ids
    @Test
    void apvB193_gtSinTraspasosNoHaceNada() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(12));
    }

    // GrabarTraspasos.java:185: APV/APV.ApruebaTraspasoAutomatico missing: Exception right after the sequence request
    @Test
    void apvB194_gtAprobacionAutomaticaSinConfigurarExcepcion() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(13));
    }

    // GrabarTraspasos.java:370: a detalle id of 0 from the sequence is not > 0: no detalle row is prepared
    @Test
    void apvB195_gtSecuenciaEnCeroNoCreaDetalle() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(14));
    }

    // GrabarTraspasos.java:429: an ERROR message of an earlier row blocks the detalle/contrato rows of every row
    @Test
    void apvB196_gtErrorPrevioImpideDetallesDeOtrasFilas() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(15));
    }
}
