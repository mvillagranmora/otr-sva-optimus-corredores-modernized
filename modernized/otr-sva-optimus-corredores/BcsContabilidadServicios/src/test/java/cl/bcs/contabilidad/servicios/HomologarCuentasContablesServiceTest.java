package cl.bcs.contabilidad.servicios;

import cl.bcs.contabilidad.service.HomologarCuentasContablesService;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;
import java.util.Map;

import static cl.bcs.contabilidad.servicios.Out.*;
import static cl.bcs.contabilidad.servicios.RecordingJdbc.row;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Characterization of legacy cl.bcs.contabilidad.HomologarCuentasContables (HomologarCuentasContables.java).
 * The SAP B1 connection (legacy: interfazSAP.properties L78-99) is a second fake datasource here; no SAP
 * host, user or password is used or copied. Not pinned: the InsertarLogServicio audit row (L177-196).
 */
class HomologarCuentasContablesServiceTest {

    static final String SAP_SEL = "HomologarCuentasContables_SelectCuentasContables.sql";
    static final String DEL = "HomologarCuentasContables_DeleteCuentasContables.sql";
    static final String INS = "HomologarCuentasContables_InsertCuentasContables.sql";
    static final String LONG_NAME = "X".repeat(80);

    final RecordingJdbc local = new RecordingJdbc();
    final RecordingJdbc sap = new RecordingJdbc();

    JsonObject run() {
        HomologarCuentasContablesService svc = LegacySql.injectBlank(new HomologarCuentasContablesService(local),
                "SQL_SELECT_SAP=" + SAP_SEL, "SQL_DELETE_LOCAL=" + DEL, "SQL_INSERT_LOCAL=" + INS);
        svc.setSapJdbc(sap);
        return svc.execute(json("{\"p_IdEmpresa\":1,\"p_IdModulo\":\"CON\",\"p_IdUsuario\":21,\"p_FechaContable\":\"2024-03-15\"}"));
    }

    void threeAccounts() {
        sap.defaultSelectRows(
                row("AcctCode", "1101", "AcctName", "Caja", "Segment_0", "110101", "LocManTran", "N", "GroupMask", 1, "FatherNum", null),
                row("AcctCode", "2101", "AcctName", LONG_NAME, "Segment_0", "  ", "LocManTran", "N", "GroupMask", 3, "FatherNum", "1101"),
                row("AcctCode", "4101", "AcctName", null, "Segment_0", null, "LocManTran", "N", "GroupMask", 9, "FatherNum", null));
    }

    @Test
    @DisplayName("CONT-G-180 SAP query is the legacy OACT select with :EMPRESA replaced by the company schema")
    void contG180_sapQuery() {
        // legacy HomologarCuentasContables.java:101 + sql/HomologarCuentasContables_SelectCuentasContables.sql
        threeAccounts();
        run();
        assertThat(sap.calls()).hasSize(1);
        String executed = LegacySql.norm(sap.calls().get(0).sql());
        assertThat(executed).matches("SELECT \"AcctCode\",\"AcctName\",\"Segment_0\",\"LocManTran\",\"GroupMask\",\"FatherNum\" FROM [A-Za-z0-9_\"]+\\.OACT");
        assertThat(executed).doesNotContain(":EMPRESA");
    }

    @Test
    @DisplayName("CONT-G-181 OACT -> HomologaCuenta mapping (Segment_0 fallback, 70-char truncation, defaults, Imputable)")
    void contG181_mapping() {
        // legacy HomologarCuentasContables.java:105-173
        threeAccounts();
        run();
        List<Map<String, Object>> ins = local.executedParamSets(INS);
        assertThat(ins).hasSize(3);
        assertThat(ins.get(0)).containsEntry("CuentaOptimus", "110101").containsEntry("CuentaContabilidad", "110101")
                .containsEntry("CodigoContabilidad", "1101").containsEntry("NombreCuenta", "Caja").containsEntry("DetalleCuenta", "Caja")
                .containsEntry("CuentaProveedor", "").containsEntry("CuentaEEFF", "").containsEntry("CentroCosto", "")
                .containsEntry("TipoCuenta", "A").containsEntry("Imputable", "N");
        assertThat(ins.get(1)).containsEntry("CuentaOptimus", "2101").containsEntry("CuentaContabilidad", "2101")
                .containsEntry("NombreCuenta", "X".repeat(70)).containsEntry("DetalleCuenta", "X".repeat(70))
                .containsEntry("TipoCuenta", "P").containsEntry("Imputable", "S");
        assertThat(ins.get(2)).containsEntry("CuentaOptimus", "4101").containsEntry("NombreCuenta", "CUENTA SIN NOMBRE")
                .containsEntry("DetalleCuenta", "CUENTA SIN DETALLE").containsEntry("TipoCuenta", "").containsEntry("Imputable", "S");
    }

    @ParameterizedTest(name = "GroupMask {0} -> ''{1}''")
    @CsvSource(value = {"1,A", "5,A", "6,A", "8,A", "2,P", "3,P", "4,P", "7,P", "0,''", "9,''"})
    @DisplayName("CONT-G-182 GroupMask -> TipoCuenta")
    void contG182_groupMask(int mask, String tipo) {
        // legacy HomologarCuentasContables.java:145-161
        sap.defaultSelectRows(row("AcctCode", "1", "AcctName", "N", "Segment_0", "1", "LocManTran", "N", "GroupMask", mask, "FatherNum", null));
        run();
        assertThat(local.executedParamSets(INS)).singleElement().satisfies(p -> assertThat(p).containsEntry("TipoCuenta", tipo));
    }

    @Test
    @DisplayName("CONT-G-183 local table is DELETEd (all rows) before the INSERTs, on the local datasource")
    void contG183_deleteThenInsert() {
        // legacy HomologarCuentasContables.java:198-200
        threeAccounts();
        run();
        List<String> order = local.executedFilesInOrder();
        assertThat(order).contains(DEL, INS);
        assertThat(order.indexOf(DEL)).isLessThan(order.indexOf(INS));
        assertThat(sap.callsTo(DEL)).isEmpty();
    }

    @Test
    @DisplayName("CONT-G-184 zero SAP accounts -> DELETE still executes once (table wiped), no INSERT")
    void contG184_emptySapStillDeletes() {
        // legacy HomologarCuentasContables.java:198 + BcsUtils SQL.java:599-601 (no props && empty list -> addBatch once)
        sap.defaultSelectRows();
        run();
        assertThat(local.executions(DEL)).isEqualTo(1);
        assertThat(local.executions(INS)).isZero();
    }

    @Test
    @DisplayName("CONT-G-185 success output: counter 0 and INFORMACION CUENTAS_CONTABLES_ACTUALIZADAS 'Cuentas contables actualizadas'")
    void contG185_output() {
        // legacy HomologarCuentasContables.java:66, 202
        threeAccounts();
        JsonObject out = run();
        assertThat(errores(out)).isZero();
        assertThat(mensajes(out)).hasSize(1);
        assertMensaje(mensajes(out).get(0), 0L, "INFORMACION", "CUENTAS_CONTABLES_ACTUALIZADAS");
        assertThat(str(mensajes(out).get(0), "descripcion")).isEqualTo("Cuentas contables actualizadas");
    }
}
