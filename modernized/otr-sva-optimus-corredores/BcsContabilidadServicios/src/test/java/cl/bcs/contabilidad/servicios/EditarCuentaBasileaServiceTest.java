package cl.bcs.contabilidad.servicios;

import cl.bcs.contabilidad.service.EditarCuentaBasileaService;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static cl.bcs.contabilidad.servicios.Out.*;
import static cl.bcs.contabilidad.servicios.RecordingJdbc.row;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Characterization of legacy cl.bcs.contabilidad.EditarCuentaBasilea (EditarCuentaBasilea.java).
 * PLA.FechaSistema (L100) comes from the platform configuration; only its presence is pinned.
 * Not pinned: the AgregarLog audit entry (L122-130).
 */
class EditarCuentaBasileaServiceTest {

    static final String SEL = "EditarCuentaBasilea_SelectCuentaContableCliente.sql";
    static final String SEL_ACT = "EditarCuentaBasilea_SelectCuentaContableClienteActual.sql";
    static final String UPD = "EditarCuentaBasilea_UpdateCuentaBasilea.sql";

    final RecordingJdbc jdbc = new RecordingJdbc()
            .rows(SEL_ACT, row("CuentaContable", "110100", "NombreCuentaContable", "Caja antigua"));

    JsonObject run(String accion) {
        EditarCuentaBasileaService svc = LegacySql.injectBlank(new EditarCuentaBasileaService(jdbc),
                "SQL_SELECT_CUENTA=" + SEL, "SQL_SELECT_CUENTA_ACTUAL=" + SEL_ACT, "SQL_UPDATE_CUENTA=" + UPD);
        return svc.execute(json("{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"CON\",\"p_Accion\":\"" + accion + "\","
                + "\"p_Concepto\":\"ACTIVO_X\",\"p_TipoCuentaContable\":\"DEBE\",\"p_CuentaContable\":\"110101\",\"p_NombreCuentaContable\":\"Caja\"}"));
    }

    @Test
    @DisplayName("CONT-G-400 p_Accion other than GUARDAR -> P_OUT_ERRORESCONTADOR 1 with NO message and no SQL")
    void contG400_accionInvalida() {
        // legacy EditarCuentaBasilea.java:86-90
        JsonObject out = run("CONSULTAR");
        assertThat(errores(out)).isEqualTo(1L);
        assertThat(mensajes(out)).isEmpty();
        assertThat(jdbc.calls()).isEmpty();
    }

    @Test
    @DisplayName("CONT-G-401 looks up :CuentaContabilidad and the current row by :IdConcepto/:TipoCuentaContable")
    void contG401_queries() {
        // legacy EditarCuentaBasilea.java:98-107
        jdbc.rows(SEL, row("CuentaContable", "110101", "NombreCuentaContable", "Caja"));
        run("GUARDAR");
        assertThat(jdbc.single(SEL).params()).containsEntry("CuentaContabilidad", "110101");
        assertThat(jdbc.single(SEL_ACT).params()).containsEntry("IdConcepto", "ACTIVO_X").containsEntry("TipoCuentaContable", "DEBE");
    }

    @Test
    @DisplayName("CONT-G-402 account not homologated -> ERROR NO_EXISTE_CUENTA_CONTABLE")
    void contG402_noExiste() {
        // legacy EditarCuentaBasilea.java:138-143
        JsonObject out = run("GUARDAR");
        assertThat(errores(out)).isEqualTo(1L);
        assertMensaje(mensajes(out).get(0), 0L, "ERROR", "NO_EXISTE_CUENTA_CONTABLE");
        assertThat(jdbc.executions(UPD)).isZero();
    }

    @Test
    @DisplayName("CONT-G-403 more than one match -> ERROR UI_ERROR_EDITAR_CUENTA_CONTABLE_BASILEA")
    void contG403_multiple() {
        // legacy EditarCuentaBasilea.java:145-150
        jdbc.rows(SEL, row("CuentaContable", "110101", "NombreCuentaContable", "Caja"), row("CuentaContable", "110101", "NombreCuentaContable", "Caja"));
        JsonObject out = run("GUARDAR");
        assertMensaje(mensajes(out).get(0), 0L, "ERROR", "UI_ERROR_EDITAR_CUENTA_CONTABLE_BASILEA");
        assertThat(jdbc.executions(UPD)).isZero();
    }

    @Test
    @DisplayName("CONT-G-404 'NO HOMOLOGADO' name -> ERROR NO_EXISTE_CUENTA_CONTABLE_HOMOLOGADA")
    void contG404_noHomologado() {
        // legacy EditarCuentaBasilea.java:154-160
        jdbc.rows(SEL, row("CuentaContable", "110101", "NombreCuentaContable", "NO HOMOLOGADO"));
        assertMensaje(mensajes(run("GUARDAR")).get(0), 0L, "ERROR", "NO_EXISTE_CUENTA_CONTABLE_HOMOLOGADA");
    }

    @Test
    @DisplayName("CONT-G-405 name differs from p_NombreCuentaContable -> ERROR CUENTA_CONTABLE_ERROR_NOMBRE")
    void contG405_nombreDistinto() {
        // legacy EditarCuentaBasilea.java:162-166
        jdbc.rows(SEL, row("CuentaContable", "110101", "NombreCuentaContable", "Caja chica"));
        JsonObject out = run("GUARDAR");
        assertThat(errores(out)).isEqualTo(1L);
        assertMensaje(mensajes(out).get(0), 0L, "ERROR", "CUENTA_CONTABLE_ERROR_NOMBRE");
    }

    @Test
    @DisplayName("CONT-G-406 valid -> UPDATE with cuenta, nombre, IdUsuario, Concepto, TipoCuentaContable, FechaSistema; CuentaContable echoed")
    void contG406_guarda() {
        // legacy EditarCuentaBasilea.java:109-121
        jdbc.rows(SEL, row("CuentaContable", "110101", "NombreCuentaContable", "Caja"));
        JsonObject out = run("GUARDAR");
        assertThat(errores(out)).isZero();
        assertThat(mensajes(out)).isEmpty();
        assertThat(jdbc.executedParamSets(UPD)).singleElement().satisfies(p -> {
            assertThat(p).containsEntry("CuentaContable", "110101").containsEntry("NombreCuentaContable", "Caja")
                    .containsEntry("IdUsuario", 21L).containsEntry("Concepto", "ACTIVO_X").containsEntry("TipoCuentaContable", "DEBE");
            assertThat(p.get("FechaSistema")).as("PLA.FechaSistema bound").isNotNull().isNotSameAs(RecordingJdbc.MISSING);
        });
        JsonObject cc = out.getAsJsonObject("CuentaContable");
        assertThat(str(cc, "CuentaContable")).isEqualTo("110101");
        assertThat(str(cc, "NombreCuentaContable")).isEqualTo("Caja");
        assertThat(str(cc, "Concepto")).isEqualTo("ACTIVO_X");
        assertThat(lng(cc, "IdUsuario")).isEqualTo(21L);
        assertThat(str(cc, "FechaSistema")).isNotBlank();
    }
}
