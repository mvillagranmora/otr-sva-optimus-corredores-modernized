package cl.bcs.contabilidad.servicios;

import cl.bcs.contabilidad.service.CentralizaMultiSeleccionService;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static cl.bcs.contabilidad.servicios.Out.*;
import static cl.bcs.contabilidad.servicios.RecordingJdbc.row;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Characterization of legacy cl.bcs.contabilidad.CentralizaMultiSeleccion (CentralizaMultiSeleccion.java).
 * All cases use p_SimulaOperacion "S" so the PLA.LocalizacionSistema = CL branch (L240-295) is not entered,
 * and configuration Tipo values other than P/J so no stored procedure / Java centralizer runs.
 * Not pinned: stored-procedure ("P", L304-327) and Java ("J", L330-360) execution, CentralizaGenerico (L411-421).
 */
class CentralizaMultiSeleccionServiceTest {

    static final String INFORMES = "Centraliza_SelectInformesValorizados.sql";
    static final String CONFIG = "CentralizaMultiSeleccion_SelectConfiguraciones.sql";
    static final String CALL = "CentralizaMultiSeleccion_CallCentralizacion.sql";
    static final String MENSAJES = "CentralizaMultiSeleccion_SelectMensajes.sql";
    static final String VIGENTES = "CentralizaMultiSeleccion_SelectCentralizacionesVigentes.sql";
    static final String DELETE = "CentralizaMultiSeleccion_DeleteDatosMovimientos.sql";
    static final String RUTAS = "CentralizaMultiSeleccion_SelectRutaInformes.sql";

    final RecordingJdbc jdbc = new RecordingJdbc()
            .rows(CONFIG, row("IdTipoCentralizacion", 371L, "Nombre", "Spot", "Servicio", "X", "Tipo", "X", "IdTipoCentralizacionOrigen", null, "RequiereFechaConsulta", "N"),
                    row("IdTipoCentralizacion", 372L, "Nombre", "Pactos", "Servicio", null, "Tipo", null, "IdTipoCentralizacionOrigen", null, "RequiereFechaConsulta", "N"));

    JsonObject run(String tipos) {
        CentralizaMultiSeleccionService svc = LegacySql.injectBlank(new CentralizaMultiSeleccionService(jdbc),
                "SQL_SELECT_INFORMES=" + INFORMES, "SQL_SELECT_CONFIG=" + CONFIG, "SQL_SELECT_CALL_CENT=" + CALL,
                "SQL_SELECT_MENSAJES=" + MENSAJES, "SQL_SELECT_VIGENTES=" + VIGENTES, "SQL_DELETE_MOVIMIENTOS=" + DELETE,
                "SQL_SELECT_RUTAS=" + RUTAS);
        return svc.execute(json("{\"p_IdEmpresa\":1,\"p_IdUsuario\":97,\"p_IdModulo\":\"CON\",\"p_FechaContable\":\"2025-08-06\","
                + "\"p_Ejercicio\":2025,\"p_SimulaOperacion\":\"S\",\"p_in_TiposCentralizacion\":" + tipos + "}"));
    }

    @Test
    @DisplayName("CONT-G-460 configurations read with :IdTipoCentralizacion = distinct input ids")
    void contG460_config() {
        // legacy CentralizaMultiSeleccion.java:195-204
        run("[{\"IdTipoCentralizacion\":371},{\"IdTipoCentralizacion\":371},{\"IdTipoCentralizacion\":372}]");
        assertThat(jdbc.single(CONFIG).params()).containsEntry("IdTipoCentralizacion", List.of(371L, 372L));
    }

    @Test
    @DisplayName("CONT-G-461 no configuration row -> ADVERTENCIA UI_CENTRALIZACION_NO_EXISTE id 0; Tipo null -> UI_CENTRALIZACION_CONFIGURACION_NO_EXISTE")
    void contG461_advertencias() {
        // legacy CentralizaMultiSeleccion.java:211-223
        JsonObject out = run("[{\"IdTipoCentralizacion\":999},{\"IdTipoCentralizacion\":372}]");
        assertThat(errores(out)).isZero();
        List<JsonObject> m = mensajes(out);
        assertThat(m).hasSize(2);
        assertMensaje(m.get(0), 0L, "ADVERTENCIA", "UI_CENTRALIZACION_NO_EXISTE");
        assertMensaje(m.get(1), 0L, "ADVERTENCIA", "UI_CENTRALIZACION_CONFIGURACION_NO_EXISTE");
    }

    @Test
    @DisplayName("CONT-G-462 simulation -> vigentes not checked; without a P-type config the datos-movimiento DELETE never runs")
    void contG462_noDelete() {
        // legacy CentralizaMultiSeleccion.java:240 (simula N only), 304-324 (DELETE only after a stored procedure)
        run("[{\"IdTipoCentralizacion\":371}]");
        assertThat(jdbc.callsTo(VIGENTES)).isEmpty();
        assertThat(jdbc.executions(DELETE)).isZero();
        assertThat(jdbc.callsTo(CALL)).isEmpty();
    }

    @Test
    @DisplayName("CONT-G-463 procedure messages: read by processed ids; RECH -> ERROR (+1), ADVE/RAUT -> AUTORIZACION, else INFORMACION; id = IdInterno")
    void contG463_mensajes() {
        // legacy CentralizaMultiSeleccion.java:380-408
        jdbc.rows(MENSAJES,
                row("IdInterno", 1L, "IdSesion", 371L, "Codigo", "MSG_OK", "CodigoDato1", "", "ValorDato1", null, "CodigoDato2", "", "ValorDato2", null, "Resultado", "OK"),
                row("IdInterno", 2L, "IdSesion", 371L, "Codigo", "MSG_ADV", "CodigoDato1", "", "ValorDato1", null, "CodigoDato2", "", "ValorDato2", null, "Resultado", "ADVE"),
                row("IdInterno", 3L, "IdSesion", 371L, "Codigo", "MSG_RAUT", "CodigoDato1", "", "ValorDato1", null, "CodigoDato2", "", "ValorDato2", null, "Resultado", "RAUT"),
                row("IdInterno", 4L, "IdSesion", 371L, "Codigo", "MSG_RECH", "CodigoDato1", "", "ValorDato1", null, "CodigoDato2", "", "ValorDato2", null, "Resultado", "RECH"));
        JsonObject out = run("[{\"IdTipoCentralizacion\":371},{\"IdTipoCentralizacion\":372}]");
        assertThat(jdbc.single(MENSAJES).params()).containsEntry("IdTipoCentralizacion", List.of(371L));
        assertThat(errores(out)).isEqualTo(1L);
        List<JsonObject> m = mensajes(out).stream().filter(x -> str(x, "codigo").startsWith("MSG_")).toList();
        assertThat(m).hasSize(4);
        assertMensaje(m.get(0), 1L, "INFORMACION", "MSG_OK");
        assertMensaje(m.get(1), 2L, "AUTORIZACION", "MSG_ADV");
        assertMensaje(m.get(2), 3L, "AUTORIZACION", "MSG_RAUT");
        assertMensaje(m.get(3), 4L, "ERROR", "MSG_RECH");
    }

    @Test
    @DisplayName("CONT-G-464 output keys P_OUT_ERRORESCONTADOR, P_OUT_MENSAJES, P_OUT_RESULTADO ([] when nothing centralised)")
    void contG464_output() {
        // legacy CentralizaMultiSeleccion.java:88-97
        JsonObject out = run("[{\"IdTipoCentralizacion\":371}]");
        assertThat(errores(out)).isZero();
        assertThat(mensajes(out)).isEmpty();
        assertThat(array(out, "P_OUT_RESULTADO")).isEmpty();
    }
}
