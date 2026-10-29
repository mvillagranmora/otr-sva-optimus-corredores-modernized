package cl.bcs.tesoreria.familia_b;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import cl.bcs.tesoreria.familia_b.support.Caso;
import cl.bcs.tesoreria.familia_b.support.ConfiguracionDelCaso;
import cl.bcs.tesoreria.familia_b.support.DiccionarioDeCaso;
import cl.bcs.tesoreria.familia_b.support.Equivalencia;
import cl.bcs.tesoreria.familia_b.support.ZonaUtc;
import cl.bcs.tesoreria.javacall.ConsultaBCPService;
import cl.bcs.tesoreria.javacall.bcp.LogMensajeTransaccionBcp;

/**
 * Characterization of legacy {@code cl.bcs.tesoreria.ConsultaBCP} (entry point {@code call(conn, json)}) as {@code ConsultaBCPService#execute(JsonObject)}:
 * BCP asks whether a depositor (DNI/RUC) exists and is enabled; the request and the answer are logged in LOG.MensajeTransaccionBCP.
 * 
 * <p>Goldens: {@code src/test/resources/tesoreria-b/cases/TES-B-89..95}. The reception timestamp is the fixed database clock of the harness (2025-04-01 10:15:30).
 */
@ExtendWith(ZonaUtc.class)
class ConsultaBCPServiceTest {

    static final List<String> CASOS = List.of(
            "TES-B-89_consulta-dni-y-ruc-devuelve-nombre-y-tipo-de-documento-bcp",
            "TES-B-90_consulta-con-errores-de-cliente-informa-codigos-bcp",
            "TES-B-91_consulta-sin-solicitud-no-encuentra-el-log",
            "TES-B-92_consulta-sin-clientes-devuelve-resultado-vacio",
            "TES-B-93_consulta-con-fecha-de-sistema-no-configurada-lanza-excepcion",
            "TES-B-94_consulta-mismo-cliente-dos-veces",
            "TES-B-95_consulta-solicitud-repetida-toma-el-ultimo-registro");

    private static final Caso.Contador EJECUTADOS = new Caso.Contador();

    private static void verificar(String nombre) {
        Equivalencia.verificar(EJECUTADOS, nombre, caso -> new ConsultaBCPService(caso.jdbc(), new DiccionarioDeCaso(), new ConfiguracionDelCaso(caso), new LogMensajeTransaccionBcp(caso.jdbc())).execute(caso.entrada().deepCopy()));
    }

    @AfterAll
    static void resumen() {
        EJECUTADOS.imprimir("ConsultaBCP");
        assertThat(EJECUTADOS.total()).as("equivalence cases executed (zero = not proven)").isGreaterThan(0);
    }

    // ConsultaBCP.java:497-504: DNI -> BCP type code 1, RUC -> 6, natural person name composed from the three name parts, legal person by RazonSocial
    @Test
    void tesB89_consultaDniYRucDevuelveNombreYTipoDeDocumentoBcp() {
        verificar(CASOS.get(0));
    }

    // ConsultaBCP.java:451-456: every client validation reports its BCP error code (CP0010 / CP0141 ...)
    @Test
    void tesB90_consultaConErroresDeClienteInformaCodigosBcp() {
        verificar(CASOS.get(1));
    }

    // ConsultaBCP.java:343-348: a request without IdSolicitudBcp cannot be found back in the log -> ERROR_EN_REGISTRO_MENSAJE_ENTRADA_BCP
    @Test
    void tesB91_consultaSinSolicitudNoEncuentraElLog() {
        verificar(CASOS.get(2));
    }

    // ConsultaBCP.java:358-366,487-509: no depositors in the request: empty result
    @Test
    void tesB92_consultaSinClientesDevuelveResultadoVacio() {
        verificar(CASOS.get(3));
    }

    // ConsultaBCP.java:297: PLATAFORMA/PLA.FechaSistema missing: configuration exception
    @Test
    void tesB93_consultaConFechaDeSistemaNoConfiguradaLanzaExcepcion() {
        verificar(CASOS.get(4));
    }

    // ConsultaBCP.java:381-400,413-441: the same depositor twice (with and without leading zeros): both answered
    @Test
    void tesB94_consultaMismoClienteDosVeces() {
        verificar(CASOS.get(5));
    }

    // ConsultaBCP.java:332-341,350: two log rows with the same RqUID: the latest id is returned as NumeroOperacionOptimus
    @Test
    void tesB95_consultaSolicitudRepetidaTomaElUltimoRegistro() {
        verificar(CASOS.get(6));
    }

}
