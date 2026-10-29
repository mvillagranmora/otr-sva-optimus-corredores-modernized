package cl.bcs.contabilidad.jobs.job;

import cl.bcs.contabilidad.jobs.runtime.JobResultado;
import cl.bcs.contabilidad.jobs.runtime.JobResultado.Desenlace;
import cl.bcs.contabilidad.jobs.support.H2Jobs;
import cl.bcs.contabilidad.jobs.support.H2Jobs.Estados.Estado;
import cl.bcs.contabilidad.service.GenerarInformeClienteRelacionadoService;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Job shell of legacy {@code BcsContabilidadJobs/src/cl/bcs/contabilidad/jobs/GenerarInformeClienteRelacionadoBatch.java}
 * (transaction -> logica(conn, debug=false), :27-64). The report itself is
 * {@code GenerarInformeClienteRelacionadoService.execute(JsonObject)} of BcsContabilidadServicios, mocked at that boundary.
 *
 * <p>Legacy: every param is optional with a default: IdEmpresa 1, IdUsuario 97, IdModulo "CON", FechaInicio
 * "2025-03-20", FechaFin "2025-03-30" (:37-48, numeric ones through Long.valueOf); the service runs with debug=false (:53);
 * P_OUT_ERRORESCONTADOR > 0 -> {@code throw new Throwable()} (no message, :55-58); otherwise
 * {@code updateRutaSalida(conn, out.RutaArchivo, getJobId())} (:61) and true.
 */
class GenerarInformeClienteRelacionadoBatchTest {

    static final String CLASE = "cl.bcs.contabilidad.jobs.GenerarInformeClienteRelacionadoBatch";
    static final String RUTA = "Informes/ClienteRelacionado/InformeClienteRelacionado_20260315_101530.xlsx";

    private final H2Jobs db = H2Jobs.nueva();
    private final GenerarInformeClienteRelacionadoService servicio = mock(GenerarInformeClienteRelacionadoService.class);

    @BeforeEach
    void preparar() {
        db.programa(610L, "ANTERIOR").programa(611L, "OTRO");
    }

    private JobResultado ejecutar(Map<String, String> params) {
        return db.runner(new GenerarInformeClienteRelacionadoBatch(servicio)).ejecutar(CLASE, 610L, params);
    }

    private void responder(String json) {
        when(servicio.execute(any())).thenReturn(JsonParser.parseString(json).getAsJsonObject());
    }

    private JsonObject entrada() {
        ArgumentCaptor<JsonObject> c = ArgumentCaptor.forClass(JsonObject.class);
        verify(servicio).execute(c.capture());
        return c.getValue();
    }

    @Test
    @DisplayName("CONT-J-80: no params -> defaults p_IdEmpresa 1, p_IdUsuario 97, p_IdModulo CON, p_FechaInicio 2025-03-20, p_FechaFin 2025-03-30")
    void contJ80_valoresPorDefecto() {
        // GenerarInformeClienteRelacionadoBatch.java:37-48
        responder("{\"P_OUT_ERRORESCONTADOR\":0,\"RutaArchivo\":\"" + RUTA + "\"}");

        assertThat(ejecutar(Map.of()).desenlace()).isEqualTo(Desenlace.COMMIT);

        JsonObject in = entrada();
        assertThat(in.get("p_IdEmpresa").getAsLong()).isEqualTo(1L);
        assertThat(in.get("p_IdUsuario").getAsLong()).isEqualTo(97L);
        assertThat(in.get("p_IdModulo").getAsString()).isEqualTo("CON");
        assertThat(in.get("p_FechaInicio").getAsString()).isEqualTo("2025-03-20");
        assertThat(in.get("p_FechaFin").getAsString()).isEqualTo("2025-03-30");
    }

    @Test
    @DisplayName("CONT-J-81: given params override every default")
    void contJ81_parametrosDados() {
        responder("{\"P_OUT_ERRORESCONTADOR\":0,\"RutaArchivo\":\"" + RUTA + "\"}");
        Map<String, String> p = Map.of("IdEmpresa", "2", "IdUsuario", "15", "IdModulo", "TES",
                "FechaInicio", "2026-01-01", "FechaFin", "2026-01-31");

        ejecutar(p);

        JsonObject in = entrada();
        assertThat(in.get("p_IdEmpresa").getAsLong()).isEqualTo(2L);
        assertThat(in.get("p_IdUsuario").getAsLong()).isEqualTo(15L);
        assertThat(in.get("p_IdModulo").getAsString()).isEqualTo("TES");
        assertThat(in.get("p_FechaInicio").getAsString()).isEqualTo("2026-01-01");
        assertThat(in.get("p_FechaFin").getAsString()).isEqualTo("2026-01-31");
    }

    @Test
    @DisplayName("CONT-J-82: an EMPTY text param is not defaulted: FechaInicio \"\" and IdModulo \"\" are passed as \"\"")
    void contJ82_vacioNoUsaDefault() {
        // Optional.ofNullable("") is present (BcsJob.java:76)
        responder("{\"P_OUT_ERRORESCONTADOR\":0,\"RutaArchivo\":\"" + RUTA + "\"}");

        ejecutar(Map.of("FechaInicio", "", "IdModulo", ""));

        JsonObject in = entrada();
        assertThat(in.get("p_FechaInicio").getAsString()).isEmpty();
        assertThat(in.get("p_IdModulo").getAsString()).isEmpty();
        assertThat(in.get("p_FechaFin").getAsString()).isEqualTo("2025-03-30");
    }

    @ParameterizedTest(name = "CONT-J-83: {0} = \"{1}\" -> NumberFormatException, ERROR, service never called")
    @CsvSource({"IdEmpresa, uno", "IdEmpresa, ''", "IdUsuario, 9 7", "IdUsuario, ''"})
    void contJ83_numericosInvalidos(String param, String valor) {
        // GenerarInformeClienteRelacionadoBatch.java:38, :41 Long.valueOf
        Map<String, String> p = new HashMap<>();
        p.put(param, valor);

        JobResultado r = ejecutar(p);

        assertThat(r.desenlace()).isEqualTo(Desenlace.ERROR);
        assertThat(r.error()).isInstanceOf(NumberFormatException.class);
        verifyNoInteractions(servicio);
        assertThat(db.rutaSalida(610L)).isEqualTo("ANTERIOR");
    }

    @Test
    @DisplayName("CONT-J-84: P_OUT_ERRORESCONTADOR = 1 -> ERROR with a NULL status message, RutaSalida not updated, the service's writes rolled back")
    void contJ84_erroresMayorQueCero() {
        // GenerarInformeClienteRelacionadoBatch.java:55-58 throw new Throwable() -> BcsJob.java:154-159 status(jobId, true, null).
        // Legacy Throwable is not an Exception, so BcsJob's catch(Exception) did not roll back explicitly; the connection was
        // closed uncommitted (HANA discards it). Here: explicit rollback.
        when(servicio.execute(any())).thenAnswer(inv -> {
            db.marcar("parcial");
            return JsonParser.parseString("{\"P_OUT_ERRORESCONTADOR\":1,\"P_OUT_MENSAJES\":[{\"Codigo\":\"UI_ERROR_GENERAR_INFORME\"}]}").getAsJsonObject();
        });

        JobResultado r = ejecutar(Map.of());

        assertThat(r.desenlace()).isEqualTo(Desenlace.ERROR);
        assertThat(db.estados().llamadas()).containsExactly(new Estado(610L, true, null));
        assertThat(db.rutaSalida(610L)).isEqualTo("ANTERIOR");
        assertThat(db.marcas()).isEmpty();
    }

    @Test
    @DisplayName("CONT-J-85: 0 errors -> RutaSalida of the job's IdPrograma = out.RutaArchivo; other rows untouched; COMMIT, status(id,false,\"\")")
    void contJ85_rutaSalida() {
        // GenerarInformeClienteRelacionadoBatch.java:61 updateRutaSalida(conn, out.RutaArchivo, this.getJobId())
        responder("{\"P_OUT_ERRORESCONTADOR\":0,\"P_OUT_MENSAJES\":[],\"RutaArchivo\":\"" + RUTA + "\"}");

        JobResultado r = ejecutar(Map.of());

        assertThat(r.desenlace()).isEqualTo(Desenlace.COMMIT);
        assertThat(db.rutaSalida(610L)).isEqualTo(RUTA);
        assertThat(db.rutaSalida(611L)).isEqualTo("OTRO");
        assertThat(db.estados().llamadas()).containsExactly(new Estado(610L, false, ""));
    }

    @Test
    @DisplayName("CONT-J-86: 0 errors and no RutaArchivo -> RutaSalida is overwritten with NULL")
    void contJ86_rutaArchivoAusente() {
        responder("{\"P_OUT_ERRORESCONTADOR\":0}");

        assertThat(ejecutar(Map.of()).desenlace()).isEqualTo(Desenlace.COMMIT);
        assertThat(db.rutaSalida(610L)).isNull();
    }

    @Test
    @DisplayName("CONT-J-87: P_OUT_ERRORESCONTADOR missing -> ERROR (legacy unboxing NPE of a null Long), RutaSalida untouched")
    void contJ87_contadorAusente() {
        // GenerarInformeClienteRelacionado.java:116 Long P_OUT_ERRORESCONTADOR; Batch :55 `> 0` unboxes it
        responder("{\"RutaArchivo\":\"" + RUTA + "\"}");

        assertThat(ejecutar(Map.of()).desenlace()).isEqualTo(Desenlace.ERROR);
        assertThat(db.rutaSalida(610L)).isEqualTo("ANTERIOR");
    }

    @Test
    @DisplayName("CONT-J-88: the service throws -> ERROR with its message")
    void contJ88_servicioFalla() {
        when(servicio.execute(any())).thenThrow(new IllegalStateException("RutaInformeClienteRel no configurada"));

        JobResultado r = ejecutar(Map.of());

        assertThat(r.desenlace()).isEqualTo(Desenlace.ERROR);
        assertThat(db.estados().llamadas()).containsExactly(new Estado(610L, true, "RutaInformeClienteRel no configurada"));
    }

    @Test
    @DisplayName("CONT-J-89: registered under the legacy class name cl.bcs.contabilidad.jobs.GenerarInformeClienteRelacionadoBatch")
    void contJ89_clase() {
        assertThat(new GenerarInformeClienteRelacionadoBatch(servicio).clase()).isEqualTo(CLASE);
    }
}
