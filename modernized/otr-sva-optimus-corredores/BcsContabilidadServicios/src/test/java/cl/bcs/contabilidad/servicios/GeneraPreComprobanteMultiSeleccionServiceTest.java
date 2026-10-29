package cl.bcs.contabilidad.servicios;

import cl.bcs.contabilidad.service.GeneraPreComprobanteMultiSeleccionService;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static cl.bcs.contabilidad.servicios.Out.*;
import static cl.bcs.contabilidad.servicios.RecordingJdbc.row;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Characterization of legacy cl.bcs.contabilidad.GeneraPreComprobanteMultiSeleccion
 * (GeneraPreComprobanteMultiSeleccion.java, 1001 lines).
 *
 * <p>Amounts come from the EVENTS (Evento.Monto) attached to each matching movement, signed by the
 * definition's event operator; Movimiento.Monto is never summed (L702-758).
 * Not pinned (hidden platform collaborators): ReversaPreComprobante (L828-838), EnvioContabilidad to SAP
 * with p_Destino "B" / p_UsaReglasNegocio "S" (L848-858). CONT-G-105 assumes EnvioContabilidad reports
 * no problem for the generated precomprobante. FechaProceso/HoraProceso come from the DB clock.
 */
class GeneraPreComprobanteMultiSeleccionServiceTest {

    static final String P = "GeneraPreComprobanteMultiSeleccion_";
    static final String CEN = P + "SelectCentralizacion.sql", TIPO = P + "SelectTipoContabilidad.sql",
            ULT = P + "SelectPrecomprobanteUltimo.sql", COND = P + "SelectCondicionesAsiento.sql",
            MOVD = P + "SelectMovimientosDatos.sql", MOV = P + "SelectMovimientos.sql", EVT = P + "SelectEventos.sql",
            GLOSA = P + "SelectDefinicionTipoGlosa.sql", CONDP = P + "SelectCondicionesAsientoPrecomprobante.sql",
            TCTC = P + "SelectTipoContabilidadTipoCentralizacion.sql", ASI = P + "SelectAsiento.sql",
            EVTASI = P + "SelectEventosAsiento.sql", INS_PRE = P + "InsertPrecomprobantes.sql",
            INS_ASI = P + "InsertPrecomprobantesAsientos.sql", INS_EVT = P + "InsertEventoPrecomprobantesAsientos.sql";

    final RecordingJdbc jdbc = new RecordingJdbc()
            .sequence("BSCL.Contabilidad.BD.Secuencias::IdPreComprobante", 7000, "Id")
            .sequence("BSCL.Contabilidad.BD.Secuencias::IdPreComprobanteAsiento", 8000, "Id");

    /** Tipo 1882 with one centralizacion (tipo 50), one movement (Numero 100) with dato Bolsa=BCS and one MONTO event. */
    void happyPath(String opD, String opH, String montoEvento) {
        jdbc.rows(CEN, row("IdTipoCentralizacion", 50L, "IdCentralizacion", 386L, "IdTipoContabilidad", 1882L, "Nombre", "Facturas"))
            .rows(TIPO, row("IdTipoContabilidad", 1882L, "Nombre", "FACTURAS", "Nemo", "FAC", "Tipo", "T", "Reversa", "N", "Agrupa", "N",
                    "Condicionado", "S", "TipoGeneracion", "A", "IdEmpresa", 1L, "Ejercicio", 2020L, "Formato", "F", "Estado", "V"))
            .rows(TCTC, row("IdTipoContabilidad", 1882L, "IdTipoCentralizacion", 50L))
            .rows(ASI, asiento(11L, "110101", "D", "OK"), asiento(12L, "210101", "H", "OK"))
            .rows(EVTASI, row("IdTipoEvento", "MONTO", "IdDefinicionAsiento", 11L, "Operador", opD, "Estado", "V"),
                    row("IdTipoEvento", "MONTO", "IdDefinicionAsiento", 12L, "Operador", opH, "Estado", "V"))
            .rows(MOVD, row("IdMovimiento", 1L, "TipoDato", "V", "IdTipoDatoEvento", "Bolsa", "ValorVarchar", "BCS", "ValorDecimal", null, "Estado", "V", "TipoEvento", "MONTO"))
            .rows(MOV, row("IdMovimiento", 1L, "IdTipoCentralizacion", 50L, "IdTipoMovimiento", "FAC", "IdEmpresa", 1L, "Numero", 100L, "Linea", 1L,
                    "Fecha", "2020-07-30", "DocumentoIdentidad", "11111111-1", "Cuenta", 1L, "Ejercicio", 2020L, "Periodo", "07",
                    "IdCentralizacion", 386L, "Monto", new BigDecimal("999999.00"), "Estado", "V", "IdMoneda", "CLP"))
            .rows(EVT, evento(9001L, "MONTO", montoEvento));
    }

    static Map<String, Object> asiento(long id, String cuenta, String dh, String validacion) {
        return row("IdDefinicionAsiento", id, "IdTipoContabilidad", 1882L, "IdCuentaContable", cuenta, "Ejercicio", 2020L, "Orden", id,
                "DebeHaber", dh, "TipoEvento", "S", "Condicionado", "S", "OmiteCondicionCmbte", "N", "Estado", "V", "Formato", "F",
                "IdMetadataCuentaContable", null, "Validacion", validacion);
    }

    static Map<String, Object> evento(long id, String tipo, String monto) {
        return row("IdEvento", id, "IdEmpresa", 1L, "IdTipoEvento", tipo, "IdCentralizacion", 386L, "IdMovimiento", 1L,
                "FechaContable", "2020-07-30", "Monto", new BigDecimal(monto), "CodigoMoneda", "CLP", "CodigoUsuario", "4",
                "FechaProceso", "2020-07-30", "HoraProceso", "10:00:00", "Estado", "V");
    }

    JsonObject run(String tipos) {
        GeneraPreComprobanteMultiSeleccionService svc = LegacySql.injectBlank(new GeneraPreComprobanteMultiSeleccionService(jdbc),
                "SQL_SELECT_CENTRALIZACION=" + CEN, "SQL_SELECT_TIPO_CONTABILIDAD=" + TIPO, "SQL_SELECT_PRECOMP_ULTIMO=" + ULT,
                "SQL_SELECT_CONDICIONES_ASIENTO=" + COND, "SQL_SELECT_MOVIMIENTOS_DATOS=" + MOVD, "SQL_SELECT_MOVIMIENTOS=" + MOV,
                "SQL_SELECT_EVENTOS=" + EVT, "SQL_SELECT_TIPO_GLOSA=" + GLOSA, "SQL_SELECT_CONDICIONES_PRECOMP=" + CONDP,
                "SQL_SELECT_TC_CENTRALIZACION=" + TCTC, "SQL_SELECT_ASIENTO=" + ASI, "SQL_EVENTOS_ASIENTO=" + EVTASI,
                "SQL_INSERT_PRECOMPROBANTES=" + INS_PRE, "SQL_INSERT_PRECOMP_ASIENTOS=" + INS_ASI, "SQL_INSERT_EVENTO_PRECOMP=" + INS_EVT);
        return svc.execute(json("{\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_IdModulo\":\"CON\",\"p_TipoContabilidad\":" + tipos
                + ",\"p_FechaContable\":\"2020-07-30\",\"p_Ejercicio\":2020}"));
    }

    JsonObject run() { return run("[{\"IdTipoContabilidad\":1882}]"); }

    void nothingInserted() {
        assertThat(jdbc.executions(INS_PRE) + jdbc.executions(INS_ASI) + jdbc.executions(INS_EVT)).isZero();
    }

    @Test
    @DisplayName("CONT-G-100 every lookup binds :IdTipoContabilidad = DISTINCT input ids (+ :FechaContable/:IdEmpresa where used)")
    void contG100_queryParams() {
        // legacy GeneraPreComprobanteMultiSeleccion.java:430-510
        happyPath("+", "+", "1000.005");
        run("[{\"IdTipoContabilidad\":1882},{\"IdTipoContabilidad\":1882}]");
        List<Long> ids = List.of(1882L);
        for (String f : List.of(CEN, MOVD, MOV, EVT))
            assertThat(jdbc.single(f).params()).as(f).containsEntry("IdTipoContabilidad", ids)
                    .containsEntry("FechaContable", "2020-07-30").containsEntry("IdEmpresa", 1L);
        for (String f : List.of(TIPO, ULT))
            assertThat(jdbc.single(f).params()).as(f).containsEntry("IdTipoContabilidad", ids).containsEntry("IdEmpresa", 1L);
        for (String f : List.of(TCTC, COND, ASI, EVTASI, CONDP, GLOSA))
            assertThat(jdbc.single(f).params()).as(f).containsEntry("IdTipoContabilidad", ids);
    }

    @Test
    @DisplayName("CONT-G-101 unknown tipo -> ADVERTENCIA NO_EXISTE_TIPO_CONTABILIDAD id=IdTipoContabilidad, counter 0, nothing inserted")
    void contG101_tipoNoExiste() {
        // legacy GeneraPreComprobanteMultiSeleccion.java:522-526
        JsonObject out = run();
        assertThat(errores(out)).isZero();
        assertThat(mensajes(out)).hasSize(1);
        assertMensaje(mensajes(out).get(0), 1882L, "ADVERTENCIA", "NO_EXISTE_TIPO_CONTABILIDAD");
        nothingInserted();
    }

    @Test
    @DisplayName("CONT-G-102 asientos whose account is not homologated (Validacion ERR) -> one NO_EXISTE_CUENTA_CONTABLE_ASIENTO each")
    void contG102_cuentasInvalidas() {
        // legacy GeneraPreComprobanteMultiSeleccion.java:517-535
        happyPath("+", "+", "1000");
        jdbc.rows(ASI, asiento(11L, "110101", "D", "ERR"), asiento(12L, "210101", "H", "ERR"), asiento(13L, "310101", "H", "OK"));
        JsonObject out = run();
        assertThat(codigos(out)).containsExactly("NO_EXISTE_CUENTA_CONTABLE_ASIENTO", "NO_EXISTE_CUENTA_CONTABLE_ASIENTO");
        assertMensaje(mensajes(out).get(0), 1882L, "ADVERTENCIA", "NO_EXISTE_CUENTA_CONTABLE_ASIENTO");
        nothingInserted();
    }

    @Test
    @DisplayName("CONT-G-103 no centralizacion for the date -> ADVERTENCIA NO_EXISTEN_CENTRALIZACIONES_ASOCIADAS")
    void contG103_sinCentralizacion() {
        // legacy GeneraPreComprobanteMultiSeleccion.java:536-540
        happyPath("+", "+", "1000");
        jdbc.rows(CEN, List.of());
        JsonObject out = run();
        assertThat(codigos(out)).containsExactly("NO_EXISTEN_CENTRALIZACIONES_ASOCIADAS");
        assertMensaje(mensajes(out).get(0), 1882L, "ADVERTENCIA", "NO_EXISTEN_CENTRALIZACIONES_ASOCIADAS");
        nothingInserted();
    }

    @ParameterizedTest(name = "ultimo FechaContable {0}, Reversa {1} -> blocked {2}")
    @CsvSource({"2020-07-30,N,true", "2020-07-29,N,false"})
    @DisplayName("CONT-G-104 existing precomprobante on the same date with Reversa N -> YA_EXISTE_PRECOMPROBANTE")
    void contG104_yaExiste(String fecha, String reversa, boolean blocked) {
        // legacy GeneraPreComprobanteMultiSeleccion.java:541-546
        happyPath("+", "+", "1000");
        jdbc.rows(ULT, row("IdPreComprobante", 6000L, "IdEmpresa", 1L, "IdTipoContabilidad", 1882L, "FechaContable", fecha, "Estado", "V", "IdReversa", 0L));
        JsonObject out = run();
        assertThat(codigos(out).contains("YA_EXISTE_PRECOMPROBANTE")).isEqualTo(blocked);
        assertThat(jdbc.executions(INS_PRE) > 0).isEqualTo(!blocked);
    }

    @Test
    @DisplayName("CONT-G-105 precomprobante row: sequence id, Glosa 'Nombre/Nemo', Folio 0, IdReversa 0, Estado V; PRE_COMPROBANTE_GENERADO")
    void contG105_precomprobante() {
        // legacy GeneraPreComprobanteMultiSeleccion.java:603-616, 786, 799, 842, 868-876
        happyPath("+", "+", "1000.005");
        JsonObject out = run();
        assertThat(jdbc.executedParamSets(INS_PRE)).singleElement().satisfies(p -> {
            assertThat(p).containsEntry("IdPreComprobante", 7000L).containsEntry("IdEmpresa", 1L).containsEntry("CodigoUsuario", "4")
                    .containsEntry("IdTipoContabilidad", 1882L).containsEntry("Ejercicio", 2020L).containsEntry("FechaContable", "2020-07-30")
                    .containsEntry("Glosa", "FACTURAS/FAC").containsEntry("FolioContabilidad", 0L).containsEntry("IdReversa", 0L)
                    .containsEntry("Estado", "V");
            assertThat((String) p.get("FechaProceso")).matches("\\d{4}-\\d{2}-\\d{2}");
            assertThat(p.get("HoraProceso")).isNotNull();
        });
        assertThat(mensajes(out)).anySatisfy(m -> assertMensaje(m, 7000L, "INFORMACION", "PRE_COMPROBANTE_GENERADO"));
    }

    @Test
    @DisplayName("CONT-G-106 one asiento per definition: D gets MontoDebe, H gets MontoHaber from the event amount; Linea 1..n; balanced")
    void contG106_asientos() {
        // legacy GeneraPreComprobanteMultiSeleccion.java:702-772, 806-813
        happyPath("+", "+", "1000.005");
        run();
        assertThat(jdbc.executedParamSets(INS_ASI)).hasSize(2);
        Map<String, Object> d = jdbc.executedAt(INS_ASI, 0), h = jdbc.executedAt(INS_ASI, 1);
        assertThat(d).containsEntry("IdPreComprobanteAsiento", 8000L).containsEntry("IdPreComprobante", 7000L).containsEntry("IdCuentaContable", "110101")
                .containsEntry("Linea", 1L).containsEntry("IdEmpresa", 1L).containsEntry("Ejercicio", 2020L).containsEntry("FechaContable", "2020-07-30")
                .containsEntry("Glosa", "FACTURAS/FAC").containsEntry("Estado", "V");
        assertThat((BigDecimal) d.get("MontoDebe")).isEqualByComparingTo("1000.005");
        assertThat((BigDecimal) d.get("MontoHaber")).isEqualByComparingTo("0");
        assertThat(h).containsEntry("IdPreComprobanteAsiento", 8001L).containsEntry("IdCuentaContable", "210101").containsEntry("Linea", 2L);
        assertThat((BigDecimal) h.get("MontoDebe")).isEqualByComparingTo("0");
        assertThat((BigDecimal) h.get("MontoHaber")).isEqualByComparingTo("1000.005");
        assertThat(jdbc.executedAt(INS_PRE, 0)).containsEntry("Estado", "V");
    }

    @Test
    @DisplayName("CONT-G-107 one EventoPreComprobanteAsiento per (asiento, event): operator, Linea 0, Estado V, DebeHaber side")
    void contG107_eventos() {
        // legacy GeneraPreComprobanteMultiSeleccion.java:725-742, 814-817, 844
        happyPath("+", "+", "1000.005");
        run();
        assertThat(jdbc.executedParamSets(INS_EVT))
                .extracting(p -> p.get("IdPreComprobanteAsiento") + "/" + p.get("IdEvento") + "/" + p.get("Operador") + "/" + p.get("Linea") + "/" + p.get("Estado") + "/" + p.get("DebeHaber") + "/" + p.get("GlosaDetalle"))
                .containsExactly("8000/9001/+/0/V/D/", "8001/9001/+/0/V/H/");
    }

    @Test
    @DisplayName("CONT-G-108 operator '-' on an H definition turns the amount into a debit -> Debe != Haber -> precomprobante Estado D")
    void contG108_descuadre() {
        // legacy GeneraPreComprobanteMultiSeleccion.java:926-929, 749-757, 972-978, 821
        happyPath("+", "-", "1000.005");
        run();
        Map<String, Object> h = jdbc.executedAt(INS_ASI, 1);
        assertThat((BigDecimal) h.get("MontoDebe")).isEqualByComparingTo("1000.005");
        assertThat((BigDecimal) h.get("MontoHaber")).isEqualByComparingTo("0");
        assertThat(jdbc.executedAt(INS_EVT, 1)).containsEntry("DebeHaber", "D").containsEntry("Operador", "-");
        assertThat(jdbc.executedAt(INS_PRE, 0)).containsEntry("Estado", "D");
    }

    @ParameterizedTest(name = "debe {0} vs haber {1} -> {2}")
    @CsvSource({"1000.005,1000.0049,D", "1000.004,1000.001,V", "-500,500,V"})
    @DisplayName("CONT-G-109 balance compared after setScale(2, HALF_UP); event amounts taken in absolute value")
    void contG109_redondeo(String debe, String haber, String estado) {
        // legacy GeneraPreComprobanteMultiSeleccion.java:821 (HALF_UP to 2 decimals), 926-929 (abs)
        happyPath("+", "+", "1");
        jdbc.rows(EVTASI, row("IdTipoEvento", "MONTO_D", "IdDefinicionAsiento", 11L, "Operador", "+", "Estado", "V"),
                row("IdTipoEvento", "MONTO_H", "IdDefinicionAsiento", 12L, "Operador", "+", "Estado", "V"))
            .rows(EVT, evento(9001L, "MONTO_D", debe), evento(9002L, "MONTO_H", haber));
        run();
        assertThat(jdbc.executedAt(INS_PRE, 0)).containsEntry("Estado", estado);
        assertThat((BigDecimal) jdbc.executedAt(INS_ASI, 0).get("MontoDebe")).isEqualByComparingTo(new BigDecimal(debe).abs());
    }

    @Test
    @DisplayName("CONT-G-110 matching movements without events -> NO_EXISTEN_MOVIMIENTOS_CENTRALIZACIONES_ASOCIADAS, nothing inserted")
    void contG110_sinEventos() {
        // legacy GeneraPreComprobanteMultiSeleccion.java:765-782
        happyPath("+", "+", "1000");
        jdbc.rows(EVT, List.of());
        JsonObject out = run();
        assertThat(codigos(out)).containsExactly("NO_EXISTEN_MOVIMIENTOS_CENTRALIZACIONES_ASOCIADAS");
        assertMensaje(mensajes(out).get(0), 1882L, "ADVERTENCIA", "NO_EXISTEN_MOVIMIENTOS_CENTRALIZACIONES_ASOCIADAS");
        nothingInserted();
    }

    @ParameterizedTest(name = "conditions {0} on Bolsa=BCS -> included {1}")
    @CsvSource(delimiter = ';', value = {"=:BCS;true", "=:BEC;false", "=:BEC,=:BCS;true", "<>:BEC;true", "<>:BEC,<>:BCS;false"})
    @DisplayName("CONT-G-111 comprobante-level conditions: '=' alternatives are OR-ed, other operators AND-ed, per tipo dato")
    void contG111_condiciones(String conds, boolean incluido) {
        // legacy GeneraPreComprobanteMultiSeleccion.java:657-660, 682, 886-923, 931-937
        happyPath("+", "+", "1000");
        List<Map<String, Object>> rows = new java.util.ArrayList<>();
        long id = 1;
        for (String c : conds.split(",")) {
            String[] p = c.split(":");
            rows.add(row("IdCondicion", id++, "IdDefinicionAsiento", 0L, "IdTipoContabilidad", 1882L, "IdTipoDatoEvento", "Bolsa",
                    "ValorVarchar", p[1], "ValorDecimal", null, "Condicion", p[0], "Conector", "AND", "TipoDato", "V", "TipoCondicion", "C", "Estado", "V"));
        }
        jdbc.rows(COND, rows);
        JsonObject out = run();
        assertThat(jdbc.executions(INS_PRE) > 0).isEqualTo(incluido);
        assertThat(codigos(out).contains("NO_EXISTEN_MOVIMIENTOS_CENTRALIZACIONES_ASOCIADAS")).isEqualTo(!incluido);
    }

    @Test
    @DisplayName("CONT-G-112 default glosa (IdDefinicionAsiento 0): DAT -> ' campo valor', MOV -> ' campo field' -> GlosaDetalle")
    void contG112_glosaDetalle() {
        // legacy GeneraPreComprobanteMultiSeleccion.java:628-648, 723, 939-970
        happyPath("+", "+", "1000");
        jdbc.rows(GLOSA, row("IdTipoContabilidad", 1882L, "IdDefinicionAsiento", 0L, "Origen", "DAT", "Orden", 1L, "GlosaCampo", "Bolsa",
                        "NombreCampoMovimiento", null, "IdTipoDatoEvento", "Bolsa", "Estado", "V"),
                row("IdTipoContabilidad", 1882L, "IdDefinicionAsiento", 0L, "Origen", "MOV", "Orden", 2L, "GlosaCampo", "Num",
                        "NombreCampoMovimiento", "Numero", "IdTipoDatoEvento", null, "Estado", "V"));
        run();
        assertThat(jdbc.executedParamSets(INS_EVT)).extracting(p -> p.get("GlosaDetalle")).containsOnly(" Bolsa BCS Num 100");
    }

    @Test
    @DisplayName("CONT-G-113 several tipos: warnings for the invalid one, precomprobante for the valid one")
    void contG113_mixto() {
        // legacy GeneraPreComprobanteMultiSeleccion.java:513-550, 587-783
        happyPath("+", "+", "1000");
        JsonObject out = run("[{\"IdTipoContabilidad\":1882},{\"IdTipoContabilidad\":1999}]");
        assertThat(mensajes(out)).anySatisfy(m -> assertMensaje(m, 1999L, "ADVERTENCIA", "NO_EXISTE_TIPO_CONTABILIDAD"));
        assertThat(jdbc.executedParamSets(INS_PRE)).extracting(p -> p.get("IdTipoContabilidad")).containsExactly(1882L);
    }
}
