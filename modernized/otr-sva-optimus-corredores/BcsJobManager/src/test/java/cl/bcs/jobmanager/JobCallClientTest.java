package cl.bcs.jobmanager;

import cl.bcs.jobmanager.config.JobManagerProperties;
import cl.bcs.jobmanager.service.JobCallClient;
import cl.bcs.jobmanager.service.JobCallDesenlace;
import cl.bcs.jobmanager.support.MockModulo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.net.ServerSocket;

import static org.assertj.core.api.Assertions.assertThat;

/** POST contract and response mapping of JobCallClient against a local HTTP server. */
class JobCallClientTest {

    private JobCallClient cliente(int readMs) {
        JobManagerProperties p = new JobManagerProperties();
        p.setInternalToken("tok-123");
        p.setConnectTimeoutMs(500);
        p.setReadTimeoutMs(readMs);
        return new JobCallClient(p);
    }

    @ParameterizedTest
    @CsvSource({"200,HECHO", "409,YA_RECLAMADO", "404,INCONSISTENTE", "422,INCONSISTENTE", "503,CLASE_NO_EJECUTADA",
            "500,ERROR_DEL_JOB", "401,RESPUESTA_INESPERADA", "400,RESPUESTA_INESPERADA"})
    void mapeaCadaRespuesta(int status, JobCallDesenlace esperado) throws Exception {
        try (MockModulo m = new MockModulo().responde(status)) {
            assertThat(cliente(5_000).llamar(m.url(), 42L, "cl.bcs.X")).isEqualTo(esperado);
        }
    }

    @Test
    void enviaPostConTokenIdProgramaYClase() throws Exception {
        try (MockModulo m = new MockModulo()) {
            cliente(5_000).llamar(m.url(), 42L, "cl.bcs.tesoreria.jobs.UnJob");
            assertThat(m.peticiones()).hasSize(1);
            MockModulo.Peticion p = m.peticiones().get(0);
            assertThat(p.metodo()).isEqualTo("POST");
            assertThat(p.ruta()).isEqualTo("/internal/job-call");
            assertThat(p.token()).isEqualTo("tok-123");
            assertThat(p.cuerpo()).contains("\"idPrograma\":42").contains("\"clase\":\"cl.bcs.tesoreria.jobs.UnJob\"");
        }
    }

    @Test
    void timeoutDeLecturaNoEsFalloDelJob() throws Exception {
        try (MockModulo m = new MockModulo().demora(1_500)) {
            assertThat(cliente(300).llamar(m.url(), 1L, "c")).isEqualTo(JobCallDesenlace.SIN_RESPUESTA);
        }
    }

    @Test
    void conexionRechazadaNoSeEnvia() throws Exception {
        int puertoCerrado;
        try (ServerSocket s = new ServerSocket(0)) {
            puertoCerrado = s.getLocalPort();
        }
        assertThat(cliente(5_000).llamar("http://127.0.0.1:" + puertoCerrado, 1L, "c")).isEqualTo(JobCallDesenlace.NO_ENVIADO);
    }
}
