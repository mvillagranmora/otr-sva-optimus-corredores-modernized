package cl.bcs.contabilidad.servicios;

import cl.bcs.contabilidad.service.CuadraturaCierreDiarioService;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static cl.bcs.contabilidad.servicios.Out.*;
import static cl.bcs.contabilidad.servicios.RecordingJdbc.row;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Characterization of legacy cl.bcs.contabilidad.CuadraturaCierreDiario (CuadraturaCierreDiario.java).
 * Not pinned: the AgregarLog audit entry (L316, L419-465). For centralisation rows (IdTipoCentralizacion != 0)
 * the legacy simulates CentralizaMultiSeleccion (L231-243); CONT-G-128 assumes that simulation returns no
 * resultados (no configuration for the tipo), so every current movement is a descuadre.
 */
class CuadraturaCierreDiarioServiceTest {

    static final String SEL_CUA = "CuadraturaCierreDiario_SelectCuadraturaDiariaOperaciones.sql";
    static final String SEL_MOV = "CuadraturaCierreDiario_SelectContabilidad.sql";
    static final String SEL_CMP = "CuadraturaCierreDiario_SelectComprobantes.sql";
    static final String DEL = "CuadraturaCierreDiario_DeleteCuadraturaDiariaOperaciones.sql";
    static final String INS = "CuadraturaCierreDiario_InsertCuadraturaDiariaOperaciones.sql";
    static final String SEL_JSON = "CuadraturaCierreDiario_SelectJSONCuadraturaDiariaOperaciones.sql";

    final RecordingJdbc jdbc = new RecordingJdbc();

    static Map<String, Object> resumen(String id, long idTipoCentralizacion) {
        return row("IdCuadraturaDiariaOperaciones", id, "IdEmpresa", 1L, "IdTipoCentralizacion", idTipoCentralizacion,
                "OperacionCuadraturaDiariaOperaciones", "Operacion " + id, "FechaEjecucion", null, "TotalOperaciones", 0L,
                "OperacionesConProblemas", 0L, "TipoOperacion", "CON", "Resultado", null, "Orden", 1L, "DetalleCuadraturaJSON", null);
    }

    static Map<String, Object> comprobante(long id, String estado) {
        return row("IdPreComprobante", id, "NombreEmpresa", "BCS", "Ejercicio", 2024L, "FechaContable", "2024-03-15",
                "Estado", estado, "IdTipoContabilidad", 1882L);
    }

    JsonObject run(String tipoLlamado, String procesos) {
        CuadraturaCierreDiarioService svc = LegacySql.injectBlank(new CuadraturaCierreDiarioService(jdbc),
                "SQL_SELECT_CUADRATURA=" + SEL_CUA, "SQL_SELECT_MOVIMIENTOS=" + SEL_MOV, "SQL_SELECT_COMPROBANTES=" + SEL_CMP,
                "SQL_DELETE_CUADRATURA=" + DEL, "SQL_INSERT_CUADRATURA=" + INS, "SQL_SELECT_DETALLE=" + SEL_JSON);
        return svc.execute(json("{\"p_IdUsuario\":21,\"p_IdEmpresa\":1,\"p_IdModulo\":\"CON\",\"p_FechaInicio\":\"2024-03-15\","
                + "\"p_IdProceso\":" + procesos + (tipoLlamado == null ? "" : ",\"p_TipoLlamado\":\"" + tipoLlamado + "\"") + "}"));
    }

    static final String CUACOMCON = "[{\"IdCuadraturaDiariaOperaciones\":\"CUACOMCON\"}]";

    @Test
    @DisplayName("CONT-G-120 Proceso: summary rows read with :IdEmpresa and :IdCuadraturaDiariaOperaciones list")
    void contG120_selectResumen() {
        // legacy CuadraturaCierreDiario.java:194-204
        jdbc.rows(SEL_CUA, resumen("CUACOMCON", 0));
        run("Proceso", CUACOMCON);
        assertThat(jdbc.single(SEL_CUA).params()).containsEntry("IdEmpresa", 1L).containsEntry("IdCuadraturaDiariaOperaciones", List.of("CUACOMCON"));
    }

    @Test
    @DisplayName("CONT-G-121 CUACOMCON: comprobantes read with :FechaCentralizacion = p_FechaInicio and :IdEmpresa")
    void contG121_selectComprobantes() {
        // legacy CuadraturaCierreDiario.java:250-256
        jdbc.rows(SEL_CUA, resumen("CUACOMCON", 0));
        run("Proceso", CUACOMCON);
        assertThat(jdbc.single(SEL_CMP).params()).containsEntry("FechaCentralizacion", "2024-03-15").containsEntry("IdEmpresa", 1L);
    }

    @Test
    @DisplayName("CONT-G-122 CUACOMCON: Estado V (any case) = 'No Contabilizado' descuadre; Total, problemas, UI_DESCUADRADO, JSON detail")
    void contG122_cuacomconDescuadrado() {
        // legacy CuadraturaCierreDiario.java:252-273, 303-312, 345-354
        jdbc.rows(SEL_CUA, resumen("CUACOMCON", 0))
            .rows(SEL_CMP, comprobante(1, "C"), comprobante(2, "V"), comprobante(3, "v"));
        run("Proceso", CUACOMCON);
        assertThat(jdbc.executedParamSets(INS)).hasSize(1);
        Map<String, Object> p = jdbc.executedAt(INS, 0);
        assertThat(p).containsEntry("IdCuadraturaDiariaOperaciones", "CUACOMCON").containsEntry("IdEmpresa", 1L)
                .containsEntry("IdTipoCentralizacion", 0L).containsEntry("TotalOperaciones", 3L).containsEntry("OperacionesConProblemas", 2L)
                .containsEntry("Resultado", "UI_DESCUADRADO").containsEntry("IdUsuario", 21L).containsEntry("Orden", 1L)
                .containsEntry("TipoOperacion", "CON").containsEntry("OperacionCuadraturaDiariaOperaciones", "Operacion CUACOMCON");
        assertThat(String.valueOf(p.get("FechaEjecucion"))).isEqualTo("2024-03-15");
        JsonArray detalle = parseArray((String) p.get("DetalleCuadraturaJSON"));
        assertThat(detalle).hasSize(2);
        List<Long> ids = new ArrayList<>();
        detalle.forEach(e -> {
            ids.add(e.getAsJsonObject().get("IdPreComprobante").getAsLong());
            assertThat(e.getAsJsonObject().get("Descripcion").getAsString()).isEqualTo("No Contabilizado");
        });
        assertThat(ids).containsExactly(2L, 3L);
    }

    @ParameterizedTest(name = "{0} comprobantes estado {1} -> {2}")
    @CsvSource({"2,C,UI_CUADRADO", "0,C,UI_SIN_OPERACIONES"})
    @DisplayName("CONT-G-123 CUACOMCON resultado: none pending -> UI_CUADRADO; no comprobantes -> UI_SIN_OPERACIONES")
    void contG123_resultados(int n, String estado, String resultado) {
        // legacy CuadraturaCierreDiario.java:259-266
        jdbc.rows(SEL_CUA, resumen("CUACOMCON", 0));
        List<Map<String, Object>> cmps = new ArrayList<>();
        for (int i = 0; i < n; i++) cmps.add(comprobante(i + 1, estado));
        jdbc.rows(SEL_CMP, cmps);
        run("Proceso", CUACOMCON);
        assertThat(jdbc.executedAt(INS, 0)).containsEntry("Resultado", resultado)
                .containsEntry("TotalOperaciones", (long) n).containsEntry("OperacionesConProblemas", 0L);
    }

    @Test
    @DisplayName("CONT-G-124 previous summary rows deleted (by id) before the new rows are inserted")
    void contG124_deleteThenInsert() {
        // legacy CuadraturaCierreDiario.java:304-313
        jdbc.rows(SEL_CUA, resumen("CUACOMCON", 0));
        run("Proceso", CUACOMCON);
        assertThat(jdbc.executedParamSets(DEL)).singleElement().satisfies(p -> assertThat(p).containsEntry("IdCuadraturaDiariaOperaciones", "CUACOMCON"));
        List<String> order = jdbc.executedFilesInOrder();
        assertThat(order.indexOf(DEL)).isGreaterThanOrEqualTo(0).isLessThan(order.indexOf(INS));
    }

    @Test
    @DisplayName("CONT-G-125 Proceso output: OK UI_PROCESO_EJECUTADO_EXITO id 0, counter 0, P_OUT_DETALLE_CUADRATURA present as null")
    void contG125_salidaProceso() {
        // legacy CuadraturaCierreDiario.java:176-182 (serializeNulls gson), 315
        jdbc.rows(SEL_CUA, resumen("CUACOMCON", 0));
        JsonObject out = run("Proceso", CUACOMCON);
        assertThat(errores(out)).isZero();
        assertThat(mensajes(out)).hasSize(1);
        assertMensaje(mensajes(out).get(0), 0L, "OK", "UI_PROCESO_EJECUTADO_EXITO");
        assertThat(out.has("P_OUT_DETALLE_CUADRATURA")).isTrue();
        assertThat(out.get("P_OUT_DETALLE_CUADRATURA").isJsonNull()).isTrue();
    }

    @Test
    @DisplayName("CONT-G-126 Consulta: P_OUT_DETALLE_CUADRATURA = first row's DetalleCuadraturaJSON (as a string), no writes")
    void contG126_consulta() {
        // legacy CuadraturaCierreDiario.java:318-332
        jdbc.rows(SEL_JSON, row("DetalleCuadraturaJSON", "[{\"IdPreComprobante\":2}]"), row("DetalleCuadraturaJSON", "[]"));
        JsonObject out = run("Consulta", CUACOMCON);
        assertThat(jdbc.single(SEL_JSON).params()).containsEntry("IdEmpresa", 1L).containsEntry("IdCuadraturaDiariaOperaciones", List.of("CUACOMCON"));
        assertThat(out.get("P_OUT_DETALLE_CUADRATURA").isJsonPrimitive()).isTrue();
        assertThat(out.get("P_OUT_DETALLE_CUADRATURA").getAsString()).isEqualTo("[{\"IdPreComprobante\":2}]");
        assertThat(errores(out)).isZero();
        assertThat(mensajes(out)).isEmpty();
        assertThat(jdbc.executions(INS) + jdbc.executions(DEL)).isZero();
    }

    @Test
    @DisplayName("CONT-G-127 unknown p_TipoLlamado -> no SQL, empty messages; missing p_TipoLlamado -> exception propagates")
    void contG127_tipoLlamado() {
        // legacy CuadraturaCierreDiario.java:194, 318 (neither branch), 334-339 (NPE on null caught, logged, rethrown)
        JsonObject out = run("MANUAL", CUACOMCON);
        assertThat(jdbc.calls()).isEmpty();
        assertThat(mensajes(out)).isEmpty();
        assertThat(errores(out)).isZero();
        assertThatThrownBy(() -> run(null, CUACOMCON)).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("CONT-G-128 centralisation row: movements read by :IdCentralizaciones; unmatched current movements -> 'Movimiento ya no es Contabilizable'")
    void contG128_centralizacion() {
        // legacy CuadraturaCierreDiario.java:206-229, 277-299, 402-416
        jdbc.rows(SEL_CUA, resumen("CEN_SPOT", 5))
            .rows(SEL_MOV, row("IdTipoCentralizacion", 5L, "IdMovimiento", 10L, "Numero", 100L, "Monto", new java.math.BigDecimal("10.00")),
                    row("IdTipoCentralizacion", 5L, "IdMovimiento", 11L, "Numero", 101L, "Monto", new java.math.BigDecimal("20.00")));
        run("Proceso", "[{\"IdCuadraturaDiariaOperaciones\":\"CEN_SPOT\"}]");
        assertThat(jdbc.single(SEL_MOV).params()).containsEntry("IdEmpresa", 1L).containsEntry("FechaCentralizacion", "2024-03-15")
                .containsEntry("IdCentralizaciones", List.of(5L));
        Map<String, Object> p = jdbc.executedAt(INS, 0);
        assertThat(p).containsEntry("TotalOperaciones", 2L).containsEntry("OperacionesConProblemas", 2L).containsEntry("Resultado", "UI_DESCUADRADO");
        JsonArray detalle = parseArray((String) p.get("DetalleCuadraturaJSON"));
        assertThat(detalle).hasSize(2);
        detalle.forEach(e -> assertThat(e.getAsJsonObject().get("Descripcion").getAsString()).isEqualTo("Movimiento ya no es Contabilizable"));
    }

    @Test
    @DisplayName("CONT-G-129 database failure in Proceso propagates (after recording UI_PROCESO_TERMINADO_CON_ERRORES)")
    void contG129_failurePropagates() {
        // legacy CuadraturaCierreDiario.java:334-339
        jdbc.failOn(SEL_CUA);
        assertThatThrownBy(() -> run("Proceso", CUACOMCON)).isInstanceOf(RuntimeException.class);
    }
}
