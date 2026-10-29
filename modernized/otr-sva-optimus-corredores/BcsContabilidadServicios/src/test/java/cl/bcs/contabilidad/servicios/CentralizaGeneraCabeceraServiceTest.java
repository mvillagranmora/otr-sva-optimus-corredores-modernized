package cl.bcs.contabilidad.servicios;

import cl.bcs.contabilidad.service.CentralizaGeneraCabeceraService;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static cl.bcs.contabilidad.servicios.Out.*;
import static cl.bcs.contabilidad.servicios.RecordingJdbc.row;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Characterization of legacy cl.bcs.contabilidad.CentralizaGeneraCabecera (CentralizaGeneraCabecera.java).
 * FechaProceso/HoraProceso come from the DB clock (FechaDB, L94-95): only their format/presence is pinned.
 */
class CentralizaGeneraCabeceraServiceTest {

    static final String SEL = "CentralizaGenerico_SelectTipoCentralizacion.sql";
    static final String INS = "CentralizaGenerico_InsertCentralizacion.sql";

    final RecordingJdbc jdbc = new RecordingJdbc()
            .sequence("BSCL.Contabilidad.BD.Secuencias::IdCentralizacion", 9001, "Id")
            .rows(SEL, row("IdTipoCentralizacion", 331L, "Nombre", "Facturas", "TipoEnvio", "A"),
                    row("IdTipoCentralizacion", 332L, "Nombre", "Tesoreria", "TipoEnvio", "M"));

    JsonObject run(String tipos, boolean conUsuario) {
        CentralizaGeneraCabeceraService svc = LegacySql.injectBlank(new CentralizaGeneraCabeceraService(jdbc),
                "SQL_SELECT_TIPOS=" + SEL, "SQL_INSERT=" + INS);
        return svc.execute(json("{\"p_IdEmpresa\":1,\"p_Ejercicio\":2024,\"p_FechaContable\":\"2024-02-20\","
                + (conUsuario ? "\"p_CodigoUsuario\":97," : "") + "\"p_TipoCentralizacion\":" + tipos + "}"));
    }

    static final String DOS = "[{\"IdTipoCentralizacion\":331},{\"IdTipoCentralizacion\":332}]";

    @Test
    @DisplayName("CONT-G-440 empty p_TipoCentralizacion -> {0, []} and no SQL at all")
    void contG440_vacio() {
        // legacy CentralizaGeneraCabecera.java:87-89
        JsonObject out = run("[]", true);
        assertThat(errores(out)).isZero();
        assertThat(mensajes(out)).isEmpty();
        assertThat(jdbc.calls()).isEmpty();
    }

    @Test
    @DisplayName("CONT-G-441 tipos read with :IdTipoCentralizacion = input ids")
    void contG441_query() {
        // legacy CentralizaGeneraCabecera.java:91-92
        run(DOS, true);
        assertThat(jdbc.single(SEL).params()).containsEntry("IdTipoCentralizacion", List.of(331L, 332L));
    }

    @Test
    @DisplayName("CONT-G-442 one Centralizacion row per tipo, Estado E, TipoGeneracion = TipoEnvio, CodigoUsuario as string")
    void contG442_inserts() {
        // legacy CentralizaGeneraCabecera.java:97-117, 125
        run(DOS, true);
        List<Map<String, Object>> ins = jdbc.executedParamSets(INS);
        assertThat(ins).extracting(p -> p.get("IdCentralizacion") + "/" + p.get("IdTipoCentralizacion") + "/" + p.get("TipoGeneracion"))
                .containsExactly("9001/331/A", "9002/332/M");
        assertThat(ins).allSatisfy(p -> {
            assertThat(p).containsEntry("IdEmpresa", 1L).containsEntry("FechaContable", "2024-02-20").containsEntry("Glosa", "")
                    .containsEntry("CodigoUsuario", "97").containsEntry("Estado", "E");
            assertThat((String) p.get("FechaProceso")).matches("\\d{4}-\\d{2}-\\d{2}");
            assertThat(p.get("HoraProceso")).isNotNull();
        });
    }

    @Test
    @DisplayName("CONT-G-443 one ERROR CENTRALIZACION_GENERADA_CON_ERROR per tipo (id = IdTipoCentralizacion) yet counter stays 0")
    void contG443_mensajes() {
        // legacy CentralizaGeneraCabecera.java:118-121 (P_OUT_ERRORESCONTADOR never incremented)
        JsonObject out = run(DOS, true);
        assertThat(errores(out)).isZero();
        List<JsonObject> m = mensajes(out);
        assertThat(m).hasSize(2);
        assertMensaje(m.get(0), 331L, "ERROR", "CENTRALIZACION_GENERADA_CON_ERROR");
        assertMensaje(m.get(1), 332L, "ERROR", "CENTRALIZACION_GENERADA_CON_ERROR");
    }

    @Test
    @DisplayName("CONT-G-444 p_CodigoUsuario absent -> CodigoUsuario ''")
    void contG444_sinUsuario() {
        // legacy CentralizaGeneraCabecera.java:114
        run(DOS, false);
        assertThat(jdbc.executedParamSets(INS)).allSatisfy(p -> assertThat(p).containsEntry("CodigoUsuario", ""));
    }

    @Test
    @DisplayName("CONT-G-445 tipo not in catalogue -> legacy NullPointerException propagates (no row inserted)")
    void contG445_tipoDesconocido() {
        // legacy CentralizaGeneraCabecera.java:103, 115 (tipoCentralizacion.TipoEnvio on null)
        assertThatThrownBy(() -> run("[{\"IdTipoCentralizacion\":999}]", true)).isInstanceOf(RuntimeException.class);
        assertThat(jdbc.executions(INS)).isZero();
    }
}
