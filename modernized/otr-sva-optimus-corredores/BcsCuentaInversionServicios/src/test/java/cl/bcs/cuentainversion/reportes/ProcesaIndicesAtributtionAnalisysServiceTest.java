package cl.bcs.cuentainversion.reportes;

import cl.bcs.cuentainversion.reportes.support.CasoReporte;
import cl.bcs.cuentainversion.reportes.support.CasoReporte.Resultado;
import cl.bcs.cuentainversion.service.ProcesaIndicesAtributtionAnalisysService;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static cl.bcs.cuentainversion.reportes.support.CasoReporte.codigos;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Characterization of legacy {@code cl.bcs.cuentaInversion.ProcesaIndicesAtributtionAnalisys} (legacy/.../cuentaInversion/ProcesaIndicesAtributtionAnalisys.java) against the modern
 * {@code ProcesaIndicesAtributtionAnalisysService}.
 *
 * <p>Every case under src/test/resources/reportes/cases/ was RECORDED by executing the unmodified legacy class (see
 * reportes/README.md); the modern service runs on the same H2 data and its JavaCall output and changed tables must equal the
 * recording. Explicit assertions before the comparison restate each rule with the literal values the legacy produced.
 * Ids CTA-B-NN are local to family B (BUSINESS_RULES.md has no rule for this module except RULE-076).
 
 */
class ProcesaIndicesAtributtionAnalisysServiceTest {

    static final List<String> CASOS = List.of(
            "CTA-B-165_indices-crea-un-indice-nuevo",
            "CTA-B-166_indices-crear-un-indice-que-es-parametro-del-sistema-es-error",
            "CTA-B-167_indices-crear-un-indice-duplicado-falla-con-mensaje-de-identificacion",
            "CTA-B-168_indices-crear-con-lista-vacia-responde-error-generico",
            "CTA-B-169_indices-modificar-conserva-los-campos-nulos",
            "CTA-B-170_indices-modificar-un-indice-que-no-existe-no-falla",
            "CTA-B-171_indices-eliminar-borra-valores-y-definicion",
            "CTA-B-172_indices-eliminar-un-indice-usado-en-el-arbol-es-error",
            "CTA-B-173_indices-tarea-desconocida-no-hace-nada",
            "CTA-B-174_indices-sin-tarea-lanza-excepcion");

    private static final AtomicInteger EJECUTADOS = new AtomicInteger();
    private static final Map<String, CasoReporte> CASOS_CARGADOS = new ConcurrentHashMap<>();

    /** Loads the recorded case, runs the modern service on its H2 data and counts the execution. */
    private static Resultado ejecutar(String nombre) {
        CasoReporte caso = CasoReporte.cargar(nombre);
        CASOS_CARGADOS.put(nombre, caso);
        Resultado r = caso.ejecutar(new ProcesaIndicesAtributtionAnalisysService(caso.jdbc())::execute);
        EJECUTADOS.incrementAndGet();
        return r;
    }

    private static void verificar(String nombre, Resultado r) {
        CASOS_CARGADOS.get(nombre).assertEquivalente(r);
    }

    private static JsonObject salida(Resultado r) {
        assertThat(r.excepcion()).as("modern implementation threw").isNull();
        return r.salida();
    }

    private static long contador(JsonObject s, String campo) {
        return s.get(campo).getAsLong();
    }

    @AfterAll
    static void ningunCasoOmitido() {
        System.out.println("equivalence cases executed (ProcesaIndicesAtributtionAnalisys): " + EJECUTADOS.get() + "/" + CASOS.size());
        assertThat(EJECUTADOS.get()).as("equivalence cases executed (a comparison that did not run is not proof)").isPositive();
    }

    @Test
    @DisplayName("CTA-B-165: task C inserts a new index")
    void ctaB165_indicesCreaUnIndiceNuevo() {
        // ProcesaIndicesAtributtionAnalisys.java:118-150 (TRANSACCION_INSERT)
        // Task C with a new index id: a row is inserted in MER.DefinicionIndiceFinanciero and no message is returned.
        Resultado r = ejecutar("CTA-B-165_indices-crea-un-indice-nuevo");
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(r.tablas().keySet()).containsExactlyInAnyOrder("BSCL.Plataforma.BD::MER.DefinicionIndiceFinanciero");
        assertThat(r.tablas().getAsJsonArray("BSCL.Plataforma.BD::MER.DefinicionIndiceFinanciero")).as("rows of DefinicionIndiceFinanciero").hasSize(4);
        verificar("CTA-B-165_indices-crea-un-indice-nuevo", r);
    }

    @Test
    @DisplayName("CTA-B-166: creating an index that is a system parameter is an error")
    void ctaB166_indicesCrearUnIndiceQueEsParametroDelSistemaEsError() {
        // ProcesaIndicesAtributtionAnalisys.java:127 (UI_EXISTE_INDICE_SISTEMA)
        // The id exists in MER.DefinicionParametroFinanciero (a system parameter) -> UI_PROCESO_EJECUTADO_ERROR with the
        // UI_EXISTE_INDICE_SISTEMA text; nothing inserted. The error counter is set by call().
        Resultado r = ejecutar("CTA-B-166_indices-crear-un-indice-que-es-parametro-del-sistema-es-error");
        JsonObject s = salida(r);
        assertThat(codigos(s)).containsExactly("UI_PROCESO_EJECUTADO_ERROR");
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(1L);
        assertThat(r.tablas().keySet()).as("legacy changed no table").isEmpty();
        verificar("CTA-B-166_indices-crear-un-indice-que-es-parametro-del-sistema-es-error", r);
    }

    @Test
    @DisplayName("CTA-B-167: duplicate index id fails with the identification message")
    void ctaB167_indicesCrearUnIndiceDuplicadoFallaConMensajeDeIdentificacion() {
        // ProcesaIndicesAtributtionAnalisys.java:139 (catch Exception -> UI_PROCESO_EJECUTADO_ERROR_INDENTIFICAR_INDICE)
        // Inserting an existing index id fails in the DB (primary key assumption in schema-reportes.sql): the exception is
        // caught and answered with UI_PROCESO_EJECUTADO_ERROR + the UI_PROCESO_EJECUTADO_ERROR_INDENTIFICAR_INDICE text.
        Resultado r = ejecutar("CTA-B-167_indices-crear-un-indice-duplicado-falla-con-mensaje-de-identificacion");
        JsonObject s = salida(r);
        assertThat(codigos(s)).containsExactly("UI_PROCESO_EJECUTADO_ERROR");
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(1L);
        assertThat(r.tablas().keySet()).as("legacy changed no table").isEmpty();
        verificar("CTA-B-167_indices-crear-un-indice-duplicado-falla-con-mensaje-de-identificacion", r);
    }

    @Test
    @DisplayName("CTA-B-168: task C with an empty list answers the generic error")
    void ctaB168_indicesCrearConListaVaciaRespondeErrorGenerico() {
        // ProcesaIndicesAtributtionAnalisys.java:139 (registros.get(0) fails inside the inner try)
        // No registros: registros.get(0) throws inside the inner try and is answered with UI_PROCESO_EJECUTADO_ERROR
        // (identification text).
        Resultado r = ejecutar("CTA-B-168_indices-crear-con-lista-vacia-responde-error-generico");
        JsonObject s = salida(r);
        assertThat(codigos(s)).containsExactly("UI_PROCESO_EJECUTADO_ERROR");
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(1L);
        assertThat(r.tablas().keySet()).as("legacy changed no table").isEmpty();
        verificar("CTA-B-168_indices-crear-con-lista-vacia-responde-error-generico", r);
    }

    @Test
    @DisplayName("CTA-B-169: task M keeps null fields (COALESCE)")
    void ctaB169_indicesModificarConservaLosCamposNulos() {
        // ProcesaIndicesAtributtionAnalisys.java:150-155 (TRANSACCION_UPDATE)
        // Task M: UPDATE with COALESCE(parameter, current): only the supplied name changes; period, description and currencies
        // keep their values.
        Resultado r = ejecutar("CTA-B-169_indices-modificar-conserva-los-campos-nulos");
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(r.tablas().keySet()).containsExactlyInAnyOrder("BSCL.Plataforma.BD::MER.DefinicionIndiceFinanciero");
        assertThat(r.tablas().getAsJsonArray("BSCL.Plataforma.BD::MER.DefinicionIndiceFinanciero")).as("rows of DefinicionIndiceFinanciero").hasSize(3);
        verificar("CTA-B-169_indices-modificar-conserva-los-campos-nulos", r);
    }

    @Test
    @DisplayName("CTA-B-170: task M of an unknown id does not fail")
    void ctaB170_indicesModificarUnIndiceQueNoExisteNoFalla() {
        // ProcesaIndicesAtributtionAnalisys.java:150-155
        // UPDATE of an unknown id affects no row and no message is produced.
        Resultado r = ejecutar("CTA-B-170_indices-modificar-un-indice-que-no-existe-no-falla");
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(r.tablas().keySet()).as("legacy changed no table").isEmpty();
        verificar("CTA-B-170_indices-modificar-un-indice-que-no-existe-no-falla", r);
    }

    @Test
    @DisplayName("CTA-B-171: task E deletes values and definition")
    void ctaB171_indicesEliminarBorraValoresYDefinicion() {
        // ProcesaIndicesAtributtionAnalisys.java:156-175 (TRANSACCION_DELETE)
        // Task E on an index that no classification node uses: its values (MER.IndiceFinanciero) and its definition are deleted.
        Resultado r = ejecutar("CTA-B-171_indices-eliminar-borra-valores-y-definicion");
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(r.tablas().keySet()).containsExactlyInAnyOrder("BSCL.Plataforma.BD::MER.DefinicionIndiceFinanciero", "BSCL.Plataforma.BD::MER.IndiceFinanciero");
        assertThat(r.tablas().getAsJsonArray("BSCL.Plataforma.BD::MER.DefinicionIndiceFinanciero")).as("rows of DefinicionIndiceFinanciero").hasSize(2);
        assertThat(r.tablas().getAsJsonArray("BSCL.Plataforma.BD::MER.IndiceFinanciero")).as("rows of IndiceFinanciero").hasSize(3);
        verificar("CTA-B-171_indices-eliminar-borra-valores-y-definicion", r);
    }

    @Test
    @DisplayName("CTA-B-172: task E of an index used in the tree is an error")
    void ctaB172_indicesEliminarUnIndiceUsadoEnElArbolEsError() {
        // ProcesaIndicesAtributtionAnalisys.java:165 (UI_EXISTE_INDICE_RELACIONADO)
        // The index is referenced by INS.ArbolClasificacionActivos -> UI_PROCESO_EJECUTADO_ERROR with the
        // UI_EXISTE_INDICE_RELACIONADO text; nothing deleted.
        Resultado r = ejecutar("CTA-B-172_indices-eliminar-un-indice-usado-en-el-arbol-es-error");
        JsonObject s = salida(r);
        assertThat(codigos(s)).containsExactly("UI_PROCESO_EJECUTADO_ERROR");
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(1L);
        assertThat(r.tablas().keySet()).as("legacy changed no table").isEmpty();
        verificar("CTA-B-172_indices-eliminar-un-indice-usado-en-el-arbol-es-error", r);
    }

    @Test
    @DisplayName("CTA-B-173: unknown task does nothing")
    void ctaB173_indicesTareaDesconocidaNoHaceNada() {
        // ProcesaIndicesAtributtionAnalisys.java:118-175 (no else branch)
        // Any task other than C, M, E is ignored without error.
        Resultado r = ejecutar("CTA-B-173_indices-tarea-desconocida-no-hace-nada");
        JsonObject s = salida(r);
        assertThat(codigos(s)).isEmpty();
        assertThat(contador(s, "P_OUT_ERRORESCONTADOR")).isEqualTo(0L);
        assertThat(r.tablas().keySet()).as("legacy changed no table").isEmpty();
        verificar("CTA-B-173_indices-tarea-desconocida-no-hace-nada", r);
    }

    @Test
    @DisplayName("CTA-B-174: missing task throws NullPointerException (defect)")
    void ctaB174_indicesSinTareaLanzaExcepcion() {
        // ProcesaIndicesAtributtionAnalisys.java:118, :185 (p_Tarea.equals in try and in catch)
        // LEGACY DEFECT: p_Tarea absent -> NullPointerException (the catch block dereferences it again).
        Resultado r = ejecutar("CTA-B-174_indices-sin-tarea-lanza-excepcion");
        assertThat(r.excepcion()).as("legacy threw java.lang.NullPointerException").isEqualTo("java.lang.NullPointerException");
        verificar("CTA-B-174_indices-sin-tarea-lanza-excepcion", r);
    }
}
