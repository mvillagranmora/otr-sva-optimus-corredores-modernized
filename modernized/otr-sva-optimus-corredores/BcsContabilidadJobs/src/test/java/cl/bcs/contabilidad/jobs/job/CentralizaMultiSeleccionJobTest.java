package cl.bcs.contabilidad.jobs.job;

import cl.bcs.contabilidad.jobs.runtime.JobResultado;
import cl.bcs.contabilidad.jobs.runtime.JobResultado.Desenlace;
import cl.bcs.contabilidad.jobs.support.H2Jobs;
import cl.bcs.contabilidad.jobs.support.H2Jobs.Estados.Estado;
import cl.bcs.contabilidad.service.CentralizaMultiSeleccionService;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Job shell of legacy {@code BcsContabilidadJobs/src/cl/bcs/contabilidad/jobs/CentralizaMultiSeleccionJob.java}
 * (transaction -> logica(con, debug=false), :27-101). The centralization itself is
 * {@code CentralizaMultiSeleccionService.execute(JsonObject)} in BcsContabilidadServicios, mocked here at that boundary.
 *
 * <p>Legacy flow: read IdEmpresa, IdUsuario, IdModulo, FechaContable, Ejercicio, IdsTipoCentralizacion and
 * {@code fechaConsulta} (lower-case f, :59-65); split IdsTipoCentralizacion on ';' and parse each id (:68-78); build
 * EntradaCentralizaMultiSeleccion (p_SimulaOperacion defaults to "N" in its constructor, CentralizaMultiSeleccion.java:63);
 * call the service and IGNORE its output (:88-89); commit and return true (:91-92). Any exception: rollback, rethrow (:95-98).
 */
class CentralizaMultiSeleccionJobTest {

    static final String CLASE = "cl.bcs.contabilidad.jobs.CentralizaMultiSeleccionJob";

    private final H2Jobs db = H2Jobs.nueva();
    private final CentralizaMultiSeleccionService servicio = mock(CentralizaMultiSeleccionService.class);

    private JobResultado ejecutar(Map<String, String> params) {
        return db.runner(new CentralizaMultiSeleccionJob(servicio)).ejecutar(CLASE, 900L, params);
    }

    private static Map<String, String> params() {
        Map<String, String> p = new HashMap<>();
        p.put("IdEmpresa", "1");
        p.put("IdUsuario", "21");
        p.put("IdModulo", "CON");
        p.put("FechaContable", "2026-04-30");
        p.put("Ejercicio", "2026");
        p.put("IdsTipoCentralizacion", "200;201;210");
        p.put("fechaConsulta", "2026-04-29");
        return p;
    }

    private JsonObject entradaEnviada() {
        ArgumentCaptor<JsonObject> c = ArgumentCaptor.forClass(JsonObject.class);
        verify(servicio).execute(c.capture());
        return c.getValue();
    }

    private static boolean nulo(JsonObject o, String k) {
        return !o.has(k) || o.get(k).isJsonNull();
    }

    private static List<Long> tipos(JsonObject in) {
        List<Long> out = new ArrayList<>();
        for (JsonElement e : in.getAsJsonArray("p_in_TiposCentralizacion")) {
            JsonObject t = e.getAsJsonObject();
            assertThat(t.keySet()).as("each tipo carries only IdTipoCentralizacion").containsExactly("IdTipoCentralizacion");
            out.add(t.get("IdTipoCentralizacion").getAsLong());
        }
        return out;
    }

    @Test
    @DisplayName("CONT-J-10: the seven job params map to the service input, with p_SimulaOperacion 'N' and one InTiposCentralizacion per id")
    void contJ10_mapeoEntrada() {
        // CentralizaMultiSeleccionJob.java:59-65, :80-86; CentralizaMultiSeleccion.java:62-65 (default "N")
        when(servicio.execute(any())).thenReturn(JsonParser.parseString("{\"P_OUT_ERRORESCONTADOR\":0}").getAsJsonObject());

        JobResultado r = ejecutar(params());

        assertThat(r.desenlace()).isEqualTo(Desenlace.COMMIT);
        JsonObject in = entradaEnviada();
        assertThat(in.get("p_IdEmpresa").getAsLong()).isEqualTo(1L);
        assertThat(in.get("p_IdUsuario").getAsLong()).isEqualTo(21L);
        assertThat(in.get("p_IdModulo").getAsString()).isEqualTo("CON");
        assertThat(in.get("p_FechaContable").getAsString()).isEqualTo("2026-04-30");
        assertThat(in.get("p_FechaConsulta").getAsString()).isEqualTo("2026-04-29");
        assertThat(in.get("p_Ejercicio").getAsLong()).isEqualTo(2026L);
        assertThat(in.get("p_SimulaOperacion").getAsString()).isEqualTo("N");
        assertThat(tipos(in)).containsExactly(200L, 201L, 210L);
    }

    @Test
    @DisplayName("CONT-J-11: the consultation date is read from 'fechaConsulta' (lower-case f); 'FechaConsulta' is ignored")
    void contJ11_fechaConsultaMinuscula() {
        // CentralizaMultiSeleccionJob.java:65 getParam("fechaConsulta")
        when(servicio.execute(any())).thenReturn(new JsonObject());
        Map<String, String> p = params();
        p.remove("fechaConsulta");
        p.put("FechaConsulta", "2026-04-29");

        assertThat(ejecutar(p).desenlace()).isEqualTo(Desenlace.COMMIT);
        assertThat(nulo(entradaEnviada(), "p_FechaConsulta")).isTrue();
    }

    @ParameterizedTest(name = "CONT-J-12: IdsTipoCentralizacion \"{0}\" -> {1}")
    @CsvSource(delimiter = '|', value = {
            "15|15",
            "200;201;|200,201",      // String.split drops trailing empty strings
            "200;200|200,200",       // duplicates are passed through; the service de-duplicates
            "210;200|210,200"})      // order kept
    void contJ12_splitDeTipos(String ids, String esperado) {
        // CentralizaMultiSeleccionJob.java:68-78
        when(servicio.execute(any())).thenReturn(new JsonObject());
        Map<String, String> p = params();
        p.put("IdsTipoCentralizacion", ids);

        assertThat(ejecutar(p).desenlace()).isEqualTo(Desenlace.COMMIT);
        List<Long> exp = new ArrayList<>();
        for (String s : esperado.split(",")) exp.add(Long.parseLong(s));
        assertThat(tipos(entradaEnviada())).isEqualTo(exp);
    }

    @ParameterizedTest(name = "CONT-J-13: IdsTipoCentralizacion \"{0}\" -> NumberFormatException, ERROR, service not called")
    @ValueSource(strings = {"", "200;;201", ";200", " 200", "200,201", "abc"})
    void contJ13_tiposInvalidos(String ids) {
        // CentralizaMultiSeleccionJob.java:76 Long.parseLong(tipo) — no trim, empty tokens fail
        Map<String, String> p = params();
        p.put("IdsTipoCentralizacion", ids);

        JobResultado r = ejecutar(p);

        assertThat(r.desenlace()).isEqualTo(Desenlace.ERROR);
        assertThat(r.error()).isInstanceOf(NumberFormatException.class);
        verify(servicio, never()).execute(any());
        assertThat(db.estados().llamadas()).singleElement().extracting(Estado::error).isEqualTo(true);
    }

    @ParameterizedTest(name = "CONT-J-14: {0} = {1} -> NumberFormatException, ERROR, service not called")
    @CsvSource(delimiter = '|', nullValues = "NULL", value = {
            "IdEmpresa|NULL", "IdEmpresa|uno", "IdUsuario|NULL", "IdUsuario|2 1", "Ejercicio|NULL", "Ejercicio|2026.0"})
    void contJ14_numericosObligatorios(String param, String valor) {
        // CentralizaMultiSeleccionJob.java:59,60,63 Long.parseLong(getParam(..)); getParam of a missing key is null
        Map<String, String> p = params();
        if (valor == null) p.remove(param); else p.put(param, valor);

        JobResultado r = ejecutar(p);

        assertThat(r.desenlace()).isEqualTo(Desenlace.ERROR);
        assertThat(r.error()).isInstanceOf(NumberFormatException.class);
        verify(servicio, never()).execute(any());
    }

    @Test
    @DisplayName("CONT-J-14b: missing IdsTipoCentralizacion -> ERROR (legacy NPE on split), service not called")
    void contJ14b_sinIdsTipoCentralizacion() {
        // CentralizaMultiSeleccionJob.java:68 idsTipoCentralizacion.split(";") on null
        Map<String, String> p = params();
        p.remove("IdsTipoCentralizacion");

        JobResultado r = ejecutar(p);

        assertThat(r.desenlace()).isEqualTo(Desenlace.ERROR);
        verify(servicio, never()).execute(any());
        assertThat(db.estados().llamadas()).singleElement().extracting(Estado::error).isEqualTo(true);
    }

    @Test
    @DisplayName("CONT-J-15: missing IdModulo / FechaContable / fechaConsulta are passed as null, the job still runs and commits")
    void contJ15_textosOpcionalesNulos() {
        // CentralizaMultiSeleccionJob.java:61,62,65 — plain getParam, no validation in the job
        when(servicio.execute(any())).thenReturn(new JsonObject());
        Map<String, String> p = params();
        p.remove("IdModulo");
        p.remove("FechaContable");
        p.remove("fechaConsulta");

        assertThat(ejecutar(p).desenlace()).isEqualTo(Desenlace.COMMIT);
        JsonObject in = entradaEnviada();
        assertThat(nulo(in, "p_IdModulo")).isTrue();
        assertThat(nulo(in, "p_FechaContable")).isTrue();
        assertThat(nulo(in, "p_FechaConsulta")).isTrue();
    }

    @Test
    @DisplayName("CONT-J-16: P_OUT_ERRORESCONTADOR = 3 -> still COMMIT (the output is ignored), the service's writes persist, status(id,false,\"\")")
    void contJ16_commitConErrores() {
        // CentralizaMultiSeleccionJob.java:88-92 — LEGACY DEFECT kept: the salida (and its error counter) is never read.
        when(servicio.execute(any())).thenAnswer(inv -> {
            db.marcar("centralizado-parcial");
            return JsonParser.parseString("{\"P_OUT_ERRORESCONTADOR\":3,\"P_OUT_MENSAJES\":[{\"Tipo\":\"ERROR\"}]}").getAsJsonObject();
        });

        JobResultado r = ejecutar(params());

        assertThat(r.desenlace()).isEqualTo(Desenlace.COMMIT);
        assertThat(db.marcas()).containsExactly("centralizado-parcial");
        assertThat(db.estados().llamadas()).containsExactly(new Estado(900L, false, ""));
    }

    @Test
    @DisplayName("CONT-J-17: the service throws after writing -> ROLLBACK of its writes, ERROR, status(id, true, message)")
    void contJ17_excepcionRollback() {
        // CentralizaMultiSeleccionJob.java:95-98 + BcsJob.java:140-159
        when(servicio.execute(any())).thenAnswer(inv -> {
            db.marcar("no-debe-quedar");
            throw new IllegalStateException("tipo 200 sin configuracion");
        });

        JobResultado r = ejecutar(params());

        assertThat(r.desenlace()).isEqualTo(Desenlace.ERROR);
        assertThat(db.marcas()).isEmpty();
        assertThat(db.estados().llamadas()).containsExactly(new Estado(900L, true, "tipo 200 sin configuracion"));
    }

    @Test
    @DisplayName("CONT-J-18: a null service output is also ignored -> COMMIT; the service is called exactly once")
    void contJ18_salidaNulaIgnorada() {
        // CentralizaMultiSeleccionJob.java:88-92
        when(servicio.execute(any())).thenReturn(null);

        assertThat(ejecutar(params()).desenlace()).isEqualTo(Desenlace.COMMIT);
        verify(servicio, times(1)).execute(any());
    }

    @Test
    @DisplayName("CONT-J-19: the job is registered under the legacy class name cl.bcs.contabilidad.jobs.CentralizaMultiSeleccionJob")
    void contJ19_clase() {
        assertThat(new CentralizaMultiSeleccionJob(servicio).clase()).isEqualTo(CLASE);
    }
}
