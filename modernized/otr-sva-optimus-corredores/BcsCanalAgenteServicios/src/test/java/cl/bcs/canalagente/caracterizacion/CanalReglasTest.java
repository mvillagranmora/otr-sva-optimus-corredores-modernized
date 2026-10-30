package cl.bcs.canalagente.caracterizacion;

import static cl.bcs.canalagente.caracterizacion.VolcadoAyuda.PROC;
import static cl.bcs.canalagente.caracterizacion.VolcadoAyuda.entradaCartola;
import static cl.bcs.canalagente.caracterizacion.VolcadoAyuda.excepcion;
import static cl.bcs.canalagente.caracterizacion.VolcadoAyuda.filas;
import static cl.bcs.canalagente.caracterizacion.VolcadoAyuda.filasIniciales;
import static cl.bcs.canalagente.caracterizacion.VolcadoAyuda.movimiento;
import static cl.bcs.canalagente.caracterizacion.VolcadoAyuda.movimientos;
import static cl.bcs.canalagente.caracterizacion.VolcadoAyuda.run;
import static cl.bcs.canalagente.caracterizacion.VolcadoAyuda.salida;
import static cl.bcs.canalagente.caracterizacion.VolcadoAyuda.sentencia;
import static cl.bcs.canalagente.caracterizacion.VolcadoAyuda.sentencias;
import static cl.bcs.canalagente.caracterizacion.VolcadoAyuda.sql;
import static cl.bcs.canalagente.caracterizacion.VolcadoAyuda.t;
import static cl.bcs.canalagente.caracterizacion.VolcadoAyuda.tipos;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * The rules (B-CA-nn) and the defects (D-CAN-nn) of BcsCanalAgenteServicios one by one, with the expected value written out in the test: what each rule says is readable here, and the equivalence
 * test (goldens recorded from the legacy) is the proof that the whole behaviour matches. The ids are those of reglas.json. The legacy is replicated bug for bug: every D-CAN test pins a defect the
 * legacy has today. (No RULE-nnn of BUSINESS_RULES.md describes this module: none of the 201 mentions ConsultarHistorialMovimientos, the work table or the cartola of the channel.)
 */
class CanalReglasTest {

    private static final String SELECT_CLIENTE = "SELECT TOP 1 \"IdCliente\" from \"BSCL.Portafolio.BD::POR.Portafolio\" where \"IdPortafolio\" = ?;";
    private static final String INSERT = "INSERT INTO \"BSCL.CanalAgente.BD::ConsultarHistorialMovimientosCuentaInversion_Proceso\" (\"IdCargoAbonoMovimiento\",\"IdPortafolio\", \"IdComprobante\") VALUES (?, ?, ?);";
    private static final String DELETE = "DELETE FROM \"BSCL.CanalAgente.BD::ConsultarHistorialMovimientosCuentaInversion_Proceso\";";
    private static final String SELECT_COMPROBANTE_INICIO = "SELECT chm.\"IdCargoAbonoMovimiento\", TO_VARCHAR(fac.\"FolioComprobante\") AS \"FolioComprobante\", por.\"CodigoPortafolio\", f";
    private static final String SELECT_COMPROBANTE_SHA = "af3722d1fe03d7ea276d813885a2f3df5b319ab8bef09d0bded27ead7a58198d";
    private static final int SELECT_COMPROBANTE_LARGO = 775;
    private static final Set<String> CAMPOS_MOVIMIENTO = new TreeSet<>(List.of("IdCargoAbonoMovimiento", "NombreCliente", "IdCliente", "IdPortafolio", "FechaMovimiento", "FechaLiquidacion", "IdMoneda", "IdConcepto",
            "NombreConcepto", "Cargo", "Abono", "FolioComprobante", "IdEstado", "NombreEstado", "NombreTipoComprobante", "NombreTipoProducto", "NombreTipoOperacion", "NombreTipoMovimiento", "SaldoSimulado",
            "CodigoPortafolio", "IdComprobante", "IdRespuestaDTE", "RespuestaPdfDTE", "DocumentoIdentidad", "IdMatrizAgrupador", "EsADC"));

    private static JsonObject json(String texto) {
        return JsonParser.parseString(texto).getAsJsonObject();
    }

    private static BigDecimal num(JsonElement e) {
        return new BigDecimal(e.getAsString());
    }

    private static void assertNum(String esperado, JsonElement e) {
        assertEquals(0, new BigDecimal(esperado).compareTo(num(e)), esperado + " vs " + e);
    }

    private static List<String> folios(JsonObject d) {
        java.util.ArrayList<String> l = new java.util.ArrayList<>();
        for (JsonElement m : movimientos(d)) l.add(t(m.getAsJsonObject(), "FolioComprobante"));
        return l;
    }

    // ---------------------------------------------------------------------------------------------------------------------------------- B-CA

    @Test
    @DisplayName("B-CA-01: el IdCliente se busca con SELECT TOP 1 sobre POR.Portafolio por IdPortafolio y se usa el primero")
    void B_CA_01_busca_el_cliente() throws Exception {
        JsonObject d = run("CAN-LOG-", "el-cliente-encontrado-se-envia-a-la-cartola");
        assertEquals("query", sentencia(d, 0).get("tipo").getAsString());
        assertEquals(SELECT_CLIENTE, sql(d, 0));
        assertEquals("[[200]]", sentencia(d, 0).getAsJsonArray("filas").toString());
        assertEquals(20, entradaCartola(d, 0).get("p_IdCliente").getAsLong());
        assertEquals(200, entradaCartola(d, 0).get("p_IdPortafolio").getAsLong());
    }

    @Test
    @DisplayName("B-CA-02: delega a la cartola con reglas de negocio S, saldo por fecha efectiva N y el resto copiado de la entrada")
    void B_CA_02_delegacion() throws Exception {
        JsonObject d = run("CAN-LOG-", "la-entrada-completa-se-delega");
        assertEquals(1, d.getAsJsonArray("colaboradores").size());
        assertEquals("ConsultarCartolaDeMovimiento", d.getAsJsonArray("colaboradores").get(0).getAsJsonObject().get("colaborador").getAsString());
        assertEquals(json("{\"p_IdEmpresa\":1,\"p_IdUsuario\":1646,\"p_IdModulo\":\"CTA\",\"p_UsaReglasNegocio\":\"S\",\"p_IdCliente\":10,\"p_IdPortafolio\":100,\"p_IdMoneda\":\"CLP\","
                + "\"p_FechaDesde\":\"2026-02-01\",\"p_FechaHasta\":\"2026-02-28\",\"p_Validaciones\":\"S\",\"p_SaldoPorFechaEfectiva\":\"N\",\"p_IncluyeSaldo\":\"N\",\"p_IncluyeAnulados\":\"S\"}"),
                entradaCartola(d, 0));
        JsonObject e = entradaCartola(d, 0);
        assertFalse(e.has("p_IdEstado") || e.has("p_IdCuentaInversion") || e.has("p_IdMatrizAgrupador") || e.has("p_EsADC"));
    }

    @Test
    @DisplayName("B-CA-03: el enriquecimiento hace INSERT por lote, SELECT con LEFT JOIN y DELETE sin WHERE, en ese orden, sobre la tabla de trabajo")
    void B_CA_03_enriquecimiento() throws Exception {
        JsonObject d = run("CAN-LOG-", "tres-movimientos-conservan-el-orden");
        assertEquals(List.of("query", "batch", "query", "exec"), tipos(d));
        assertEquals(SELECT_CLIENTE, sql(d, 0));
        assertEquals(INSERT, sql(d, 1));
        assertEquals("[[30,100,5002],[10,100,5001],[20,100,5004]]", sentencia(d, 1).getAsJsonArray("filas").toString());
        assertTrue(sql(d, 2).startsWith(SELECT_COMPROBANTE_INICIO), sql(d, 2));
        assertEquals(SELECT_COMPROBANTE_SHA, sentencia(d, 2).get("sqlSha256").getAsString());
        assertEquals(SELECT_COMPROBANTE_LARGO, sentencia(d, 2).get("sqlLargo").getAsInt());
        assertEquals("[[]]", sentencia(d, 2).getAsJsonArray("filas").toString());
        assertEquals(DELETE, sql(d, 3));
    }

    @Test
    @DisplayName("B-CA-04: sin comprobante salen nulos el folio, el DTE y el pdf, pero CodigoPortafolio y DocumentoIdentidad vienen del portafolio de la fila")
    void B_CA_04_sin_comprobante() throws Exception {
        for (String f : new String[] { "movimiento-sin-comprobante-deja-nulos", "comprobante-inexistente-deja-nulos" }) {
            JsonObject m = movimiento(run("CAN-LOG-", f), 0);
            assertNull(t(m, "FolioComprobante"));
            assertNull(t(m, "IdRespuestaDTE"));
            assertNull(t(m, "RespuestaPdfDTE"));
            assertEquals("PF-100", t(m, "CodigoPortafolio"));
            assertEquals("11.111.111-1", t(m, "DocumentoIdentidad"));
        }
        JsonObject desconocido = movimiento(run("CAN-LOG-", "portafolio-de-la-cartola-desconocido"), 0);
        assertEquals("7001", t(desconocido, "FolioComprobante"));
        assertNull(t(desconocido, "CodigoPortafolio"));
        assertNull(t(desconocido, "DocumentoIdentidad"));
        assertNull(t(movimiento(run("CAN-LOG-", "cliente-sin-documento"), 0), "DocumentoIdentidad"));
        assertNull(t(movimiento(run("CAN-LOG-", "portafolio-sin-fila-de-cliente"), 0), "DocumentoIdentidad"));
        JsonObject porFila = run("CAN-LOG-", "cada-fila-toma-el-portafolio-de-su-propio-movimiento");
        assertEquals(List.of("PF-100", "PF-200", "PF-300"), List.of(t(movimiento(porFila, 0), "CodigoPortafolio"), t(movimiento(porFila, 1), "CodigoPortafolio"), t(movimiento(porFila, 2), "CodigoPortafolio")));
        assertEquals(List.of("11.111.111-1", "22.222.222-2"), List.of(t(movimiento(porFila, 0), "DocumentoIdentidad"), t(movimiento(porFila, 1), "DocumentoIdentidad")));
        assertNull(t(movimiento(porFila, 2), "DocumentoIdentidad"));
    }

    @Test
    @DisplayName("B-CA-05: p_IdMatrizAgrupador y p_EsADC son opcionales: ausentes viajan nulos, presentes viajan a la cartola y cada movimiento devuelve los de su fila")
    void B_CA_05_opcionales() throws Exception {
        JsonObject ausentes = run("CAN-LOG-", "matriz-y-adc-opcionales-ausentes");
        assertFalse(entradaCartola(ausentes, 0).has("p_IdMatrizAgrupador"));
        assertFalse(entradaCartola(ausentes, 0).has("p_EsADC"));
        JsonObject presentes = run("CAN-LOG-", "matriz-y-adc-presentes-viajan");
        assertEquals(7, entradaCartola(presentes, 0).get("p_IdMatrizAgrupador").getAsLong());
        assertEquals("S", entradaCartola(presentes, 0).get("p_EsADC").getAsString());
        assertEquals(7, movimiento(presentes, 0).get("IdMatrizAgrupador").getAsLong());
        assertEquals("S", t(movimiento(presentes, 0), "EsADC"));
        JsonObject filas = run("CAN-LOG-", "matriz-y-adc-de-la-fila-de-cartola-vuelven");
        assertEquals(7, movimiento(filas, 0).get("IdMatrizAgrupador").getAsLong());
        assertTrue(movimiento(filas, 1).get("IdMatrizAgrupador").isJsonNull());
        assertTrue(movimiento(filas, 1).get("EsADC").isJsonNull());
        JsonObject cero = run("CAN-LOG-", "matriz-cero-y-adc-n");
        assertEquals(0, entradaCartola(cero, 0).get("p_IdMatrizAgrupador").getAsLong());
        assertEquals("N", entradaCartola(cero, 0).get("p_EsADC").getAsString());
    }

    @Test
    @DisplayName("B-CA-06: FechaMovimiento sale de FechaSistema, FechaLiquidacion de FechaLiquidacionOperacion y los montos pasan sin calculo")
    void B_CA_06_mapeo() throws Exception {
        JsonObject m = movimiento(run("CAN-LOG-", "mapeo-de-fechas-y-montos"), 0);
        assertEquals("2026-02-03 00:00:00.000", t(m, "FechaMovimiento"));
        assertEquals("2026-02-05 00:00:00.000", t(m, "FechaLiquidacion"));
        assertNum("1234.5", m.get("Cargo"));
        assertNum("0", m.get("Abono"));
        assertNum("-50.25", m.get("SaldoSimulado"));
        assertEquals(CAMPOS_MOVIMIENTO, new TreeSet<>(m.keySet()));
        JsonObject finos = movimiento(run("CAN-LOG-", "montos-con-ocho-decimales"), 0);
        assertNum("0.12345678", finos.get("Cargo"));
        assertNum("12345.12345678", finos.get("Abono"));
        assertNum("0.00000001", finos.get("SaldoSimulado"));
        JsonObject vacio = movimiento(run("CAN-LOG-", "campos-sin-valor-de-la-cartola"), 0);
        assertEquals(CAMPOS_MOVIMIENTO, new TreeSet<>(vacio.keySet()));
        assertNull(t(vacio, "NombreCliente"));
        assertNull(t(vacio, "FechaMovimiento"));
        assertNull(t(vacio, "Cargo"));
        assertEquals("7001", t(vacio, "FolioComprobante"));
    }

    @Test
    @DisplayName("B-CA-07: el resultado conserva el orden de la cartola")
    void B_CA_07_orden() throws Exception {
        JsonObject d = run("CAN-LOG-", "tres-movimientos-conservan-el-orden");
        assertEquals(List.of(30L, 10L, 20L), List.of(movimiento(d, 0).get("IdCargoAbonoMovimiento").getAsLong(), movimiento(d, 1).get("IdCargoAbonoMovimiento").getAsLong(),
                movimiento(d, 2).get("IdCargoAbonoMovimiento").getAsLong()));
        JsonObject grande = run("CAN-LOG-", "veinticinco-movimientos");
        assertEquals(25, movimientos(grande).size());
        assertEquals(1000, movimiento(grande, 0).get("IdCargoAbonoMovimiento").getAsLong());
        assertEquals(976, movimiento(grande, 24).get("IdCargoAbonoMovimiento").getAsLong());
        assertEquals(25, sentencia(grande, 1).getAsJsonArray("filas").size());
    }

    @Test
    @DisplayName("B-CA-08: al terminar bien el contador de errores es 0 y las listas de errores y advertencias van vacias")
    void B_CA_08_cierre() throws Exception {
        JsonObject s = salida(run("CAN-LOG-", "cartola-sin-movimientos-lote-vacio"));
        assertEquals(0, s.get("P_OUT_ERRORESCONTADOR").getAsLong());
        assertEquals(0, s.getAsJsonArray("P_OUT_ERRORESDETALLE").size());
        assertEquals(0, s.getAsJsonArray("P_OUT_ADVERTENCIASDETALLE").size());
        assertEquals(0, s.getAsJsonArray("P_OUT_RESULTADO").size());
    }

    @Test
    @DisplayName("B-CA-09: la tabla de trabajo queda vacia al terminar y la ultima sentencia es el DELETE sin WHERE; con cartola vacia el lote va vacio")
    void B_CA_09_tabla_vacia() throws Exception {
        JsonObject d = run("CAN-LOG-", "un-movimiento-con-comprobante-completo");
        assertEquals(0, filasIniciales(d, PROC).size());
        assertEquals(0, filas(d, PROC).size());
        assertEquals(DELETE, sql(d, sentencias(d) - 1));
        assertEquals("[1]", sentencia(d, sentencias(d) - 1).getAsJsonArray("filasAfectadas").toString());
        JsonObject vacia = run("CAN-LOG-", "cartola-sin-movimientos-lote-vacio");
        assertEquals("[]", sentencia(vacia, 1).getAsJsonArray("filas").toString());
        assertEquals(List.of("query", "batch", "query", "exec"), tipos(vacia));
    }

    @Test
    @DisplayName("B-CA-10: el folio sale como texto del folio de FAC.Comprobante (0 es \"0\") y el que trae la cartola se ignora")
    void B_CA_10_folio() throws Exception {
        assertEquals(List.of("7001"), folios(run("CAN-LOG-", "un-movimiento-con-comprobante-completo")));
        assertEquals(List.of("0"), folios(run("CAN-LOG-", "folio-cero-se-convierte-en-texto-0")));
        assertEquals(java.util.Arrays.asList("7001", null), folios(run("CAN-LOG-", "el-folio-de-la-cartola-se-ignora")));
        assertTrue(movimiento(run("CAN-LOG-", "un-movimiento-con-comprobante-completo"), 0).get("FolioComprobante").getAsJsonPrimitive().isString());
        assertEquals("7003", t(movimiento(run("CAN-LOG-", "comprobante-con-dte-y-pdf-propios"), 0), "FolioComprobante"));
        assertEquals("5", t(movimiento(run("CAN-LOG-", "comprobante-con-dte-y-pdf-propios"), 0), "IdRespuestaDTE"));
        JsonObject dte = run("CAN-LOG-", "el-dte-de-la-cartola-se-ignora");
        assertEquals("77", t(movimiento(dte, 0), "IdRespuestaDTE"));
        assertNull(t(movimiento(dte, 1), "IdRespuestaDTE"));
    }

    @Test
    @DisplayName("B-CA-11: por JSON la salida lleva los 7 campos de primer nivel, los 26 del movimiento, nulos explicitos y fechas yyyy-MM-dd HH:mm:ss.SSS")
    void B_CA_11_json() throws Exception {
        JsonObject d = run("CAN-CAL-", "json-completo-devuelve-el-contrato");
        assertEquals(new TreeSet<>(List.of("P_SALDOEFECTIVOINICIAL", "P_SALDOEFECTIVOFINAL", "P_OUT_RESULTADO", "P_OUT_ADVERTENCIASCONTADOR", "P_OUT_ADVERTENCIASDETALLE", "P_OUT_ERRORESCONTADOR",
                "P_OUT_ERRORESDETALLE")), new TreeSet<>(salida(d).keySet()));
        assertEquals(CAMPOS_MOVIMIENTO, new TreeSet<>(movimiento(d, 0).keySet()));
        assertTrue(salida(d).has("P_SALDOEFECTIVOINICIAL") && salida(d).get("P_SALDOEFECTIVOINICIAL").isJsonNull());
        assertTrue(movimiento(d, 1).has("FolioComprobante") && movimiento(d, 1).get("FolioComprobante").isJsonNull());
        assertEquals("2026-02-10 00:00:00.000", t(movimiento(d, 0), "FechaMovimiento"));
        JsonObject horas = movimiento(run("CAN-CAL-", "fechas-con-hora-en-la-cartola"), 0);
        assertEquals("2026-02-03 10:15:00.000", t(horas, "FechaMovimiento"));
        assertEquals("2026-02-05 23:59:59.123", t(horas, "FechaLiquidacion"));
        JsonObject nulas = movimiento(run("CAN-CAL-", "fecha-nula-en-la-cartola"), 0);
        assertTrue(nulas.has("FechaMovimiento") && nulas.get("FechaMovimiento").isJsonNull());
        JsonObject unicode = movimiento(run("CAN-CAL-", "caracteres-especiales-y-unicode"), 0);
        assertEquals("Ñandú \"Pérez\" & Cía <ltda>", t(unicode, "NombreCliente"));
        assertEquals("Vigente\ttab", t(unicode, "NombreEstado"));
    }

    @Test
    @DisplayName("B-CA-12: el comprobante se une al movimiento por IdCargoAbonoMovimiento: dos movimientos con el mismo comprobante reciben el mismo folio; cada id recibe el suyo")
    void B_CA_12_une_por_id() throws Exception {
        assertEquals(List.of("7001", "7001"), folios(run("CAN-LOG-", "dos-movimientos-comparten-el-mismo-comprobante")));
        assertEquals(List.of("7002", "7001", "0"), folios(run("CAN-LOG-", "tres-movimientos-conservan-el-orden")));
        JsonObject nulo = run("CAN-LOG-", "movimiento-con-id-nulo-se-inserta-y-sale");
        assertTrue(movimiento(nulo, 0).get("IdCargoAbonoMovimiento").isJsonNull());
        assertEquals(java.util.Arrays.asList("7001", "7002"), folios(nulo));
        assertEquals("[[null,100,5001],[2,100,5002]]", sentencia(nulo, 1).getAsJsonArray("filas").toString());
    }

    @Test
    @DisplayName("B-CA-13: el modulo no hace commit, rollback ni cierra la conexion que recibe (ninguna sentencia de transaccion en ningun caso)")
    void B_CA_13_sin_transaccion() throws Exception {
        for (String id : CasoCanal.casos()) {
            for (JsonElement s : CasoCanal.corrida(id).getAsJsonArray("sentencias")) {
                assertFalse(s.getAsJsonObject().get("tipo").getAsString().equals("transaccion"), id + ": " + s);
            }
        }
        JsonObject d = run("CAN-LOG-", "falla-el-select-de-comprobantes");
        assertEquals(2, filas(d, PROC).size(), "the rows stay in the session: the caller decides");
    }

    // ---------------------------------------------------------------------------------------------------------------------------------- D-CAN

    @Test
    @DisplayName("D-CAN-01: portafolio sin cliente lanza NullPointerException (el contador de errores es un Long nulo) y no llega a la cartola; el mensaje \"Cliente no encontrado.\" no sale nunca")
    void D_CAN_01_cliente_no_encontrado() throws Exception {
        for (String f : new String[] { "portafolio-inexistente-lanza-nullpointer", "portafolio-nulo-lanza-nullpointer", "portafolio-cero-lanza-nullpointer", "base-sin-ningun-portafolio" }) {
            JsonObject d = run("CAN-LOG-", f);
            assertEquals("NullPointerException", excepcion(d), f);
            assertFalse(d.has("salida"), f);
            assertEquals(List.of("query"), tipos(d), f);
            assertEquals(0, d.getAsJsonArray("colaboradores").size(), f);
            assertEquals(0, filas(d, PROC).size(), f);
        }
        assertEquals("[[null]]", sentencia(run("CAN-LOG-", "portafolio-nulo-lanza-nullpointer"), 0).getAsJsonArray("filas").toString());
        assertEquals("NullPointerException", excepcion(run("CAN-CAL-", "cliente-no-encontrado-lanza-nullpointer")));
    }

    @Test
    @DisplayName("D-CAN-02: P_OUT_ADVERTENCIASCONTADOR sale siempre nulo (la clave esta) con la lista de advertencias vacia")
    void D_CAN_02_advertencias_nulas() throws Exception {
        for (String f : new String[] { "un-movimiento-con-comprobante-completo", "mensajes-de-advertencia-no-se-propagan", "saldos-y-advertencias-quedan-nulos", "cartola-sin-movimientos-lote-vacio" }) {
            JsonObject s = salida(run("CAN-LOG-", f));
            assertTrue(s.has("P_OUT_ADVERTENCIASCONTADOR") && s.get("P_OUT_ADVERTENCIASCONTADOR").isJsonNull(), f);
            assertEquals(0, s.getAsJsonArray("P_OUT_ADVERTENCIASDETALLE").size(), f);
        }
    }

    @Test
    @DisplayName("D-CAN-03: los saldos inicial y final de efectivo no se asignan nunca: salen nulos (el procedimiento de BD los pone en 0)")
    void D_CAN_03_saldos_nulos() throws Exception {
        for (String f : new String[] { "un-movimiento-con-comprobante-completo", "saldos-y-advertencias-quedan-nulos", "cartola-sin-movimientos-lote-vacio" }) {
            JsonObject s = salida(run("CAN-LOG-", f));
            assertTrue(s.has("P_SALDOEFECTIVOINICIAL") && s.get("P_SALDOEFECTIVOINICIAL").isJsonNull(), f);
            assertTrue(s.has("P_SALDOEFECTIVOFINAL") && s.get("P_SALDOEFECTIVOFINAL").isJsonNull(), f);
        }
        assertNum("5000", movimiento(run("CAN-LOG-", "saldos-y-advertencias-quedan-nulos"), 0).get("SaldoSimulado"));
    }

    @Test
    @DisplayName("D-CAN-04: los errores y mensajes de la cartola no se propagan: el servicio responde con contador 0 y listas vacias")
    void D_CAN_04_errores_de_la_cartola_no_se_propagan() throws Exception {
        for (String f : new String[] { "errores-de-la-cartola-con-lista-vacia", "errores-de-la-cartola-con-movimientos", "mensajes-de-error-de-requisitos-basicos", "mensajes-de-advertencia-no-se-propagan" }) {
            JsonObject d = run("CAN-LOG-", f);
            assertEquals(0, salida(d).get("P_OUT_ERRORESCONTADOR").getAsLong(), f);
            assertEquals(0, salida(d).getAsJsonArray("P_OUT_ERRORESDETALLE").size(), f);
            assertEquals(0, salida(d).getAsJsonArray("P_OUT_ADVERTENCIASDETALLE").size(), f);
            assertFalse(d.has("excepcion"), f);
        }
        assertEquals(0, movimientos(run("CAN-LOG-", "errores-de-la-cartola-con-lista-vacia")).size());
        assertEquals(1, movimientos(run("CAN-LOG-", "errores-de-la-cartola-con-movimientos")).size());
        assertEquals(0, salida(run("CAN-CAL-", "cartola-con-errores-y-sin-movimientos-por-json")).get("P_OUT_ERRORESCONTADOR").getAsLong());
    }

    @Test
    @DisplayName("D-CAN-05: un fallo a mitad relanza sin limpiar: las filas insertadas quedan en la tabla de trabajo y el DELETE no se envia")
    void D_CAN_05_fallo_a_mitad() throws Exception {
        JsonObject select = run("CAN-LOG-", "falla-el-select-de-comprobantes");
        assertEquals("SQLException", excepcion(select));
        assertEquals(List.of("query", "batch", "query"), tipos(select));
        assertEquals("FALLO_INYECTADO", sentencia(select, 2).get("ejecucion").getAsString());
        assertEquals(2, filas(select, PROC).size());
        JsonObject insercion = run("CAN-LOG-", "falla-la-insercion-en-la-tabla-de-trabajo");
        assertEquals("SQLException", excepcion(insercion));
        assertEquals(List.of("query", "batch"), tipos(insercion));
        assertEquals(0, filas(insercion, PROC).size());
        JsonObject borrado = run("CAN-LOG-", "falla-el-delete-final");
        assertEquals("SQLException: no se puede borrar", d(borrado));
        assertFalse(borrado.has("salida"));
        assertEquals(1, filas(borrado, PROC).size());
        assertEquals("SQLException", excepcion(run("CAN-LOG-", "falla-el-select-del-cliente")));
        assertEquals(List.of("query"), tipos(run("CAN-LOG-", "falla-el-select-del-cliente")));
        assertEquals("Exception: Error en la cartola", d(run("CAN-LOG-", "cartola-lanza-error-se-propaga-sin-tocar")));
        assertEquals(List.of("query"), tipos(run("CAN-LOG-", "cartola-lanza-error-se-propaga-sin-tocar")));
    }

    private static String d(JsonObject d) {
        return d.get("excepcion").getAsString().split("\n")[0].split(" Query:")[0];
    }

    @Test
    @DisplayName("D-CAN-06: con un IdCargoAbonoMovimiento repetido todos reciben el comprobante de la ultima fila que devuelve el SELECT")
    void D_CAN_06_id_repetido() throws Exception {
        JsonObject d = run("CAN-LOG-", "id-de-movimiento-repetido-ambos-reciben");
        assertEquals(List.of("7002", "7002"), folios(d));
        assertEquals("5001", t(movimiento(d, 0), "IdComprobante"));
        assertEquals("5002", t(movimiento(d, 1), "IdComprobante"));
        assertEquals("[[1,100,5001],[1,100,5002]]", sentencia(d, 1).getAsJsonArray("filas").toString());
        assertEquals(List.of("7001", "7001"), folios(run("CAN-LOG-", "id-repetido-con-el-mismo-comprobante")));
    }

    @Test
    @DisplayName("D-CAN-07: no hay movimiento sin fila en la tabla de trabajo: aun sin comprobante la fila existe y aporta el portafolio (el reemplazo vacio es codigo muerto)")
    void D_CAN_07_codigo_muerto() throws Exception {
        JsonObject d = run("CAN-LOG-", "movimiento-sin-comprobante-deja-nulos");
        assertEquals("PF-100", t(movimiento(d, 0), "CodigoPortafolio"));
        assertNull(t(movimiento(d, 0), "FolioComprobante"));
        assertEquals("[[1,100,null]]", sentencia(d, 1).getAsJsonArray("filas").toString());
    }

    @Test
    @DisplayName("D-CAN-08: salida defectuosa de la cartola: lista nula, elemento nulo y salida nula fallan antes de tocar la tabla")
    void D_CAN_08_salida_defectuosa() throws Exception {
        JsonObject lista = run("CAN-LOG-", "cartola-con-lista-nula");
        assertEquals("SQLException: Null parameters. If parameters aren't need, pass an empty", lista.get("excepcion").getAsString());
        assertEquals(List.of("query"), tipos(lista));
        JsonObject elemento = run("CAN-LOG-", "cartola-con-un-elemento-nulo");
        assertTrue(elemento.get("excepcion").getAsString().startsWith("SQLException: Map Object failed"));
        assertEquals(List.of("query"), tipos(elemento));
        assertEquals(0, filas(elemento, PROC).size());
        JsonObject nula = run("CAN-LOG-", "cartola-que-devuelve-nulo");
        assertEquals("NullPointerException", excepcion(nula));
        assertEquals(List.of("query"), tipos(nula));
        assertEquals(1, nula.getAsJsonArray("colaboradores").size());
    }

    @Test
    @DisplayName("D-CAN-09: sin validacion de entrada: empresa, usuario, modulo, moneda y fechas nulas o de formato libre pasan tal cual a la cartola")
    void D_CAN_09_sin_validacion() throws Exception {
        JsonObject nulas = entradaCartola(run("CAN-LOG-", "moneda-nula-y-fechas-nulas"), 0);
        assertFalse(nulas.has("p_IdMoneda") || nulas.has("p_FechaDesde") || nulas.has("p_FechaHasta"));
        assertEquals(1, nulas.get("p_IdEmpresa").getAsLong());
        JsonObject sinEmpresa = entradaCartola(run("CAN-LOG-", "empresa-usuario-y-modulo-nulos"), 0);
        assertFalse(sinEmpresa.has("p_IdEmpresa") || sinEmpresa.has("p_IdUsuario") || sinEmpresa.has("p_IdModulo"));
        assertEquals("USD", sinEmpresa.get("p_IdMoneda").getAsString());
        JsonObject libre = entradaCartola(run("CAN-LOG-", "fechas-invertidas-y-formato-libre"), 0);
        assertEquals("28/02/2026", libre.get("p_FechaDesde").getAsString());
        assertEquals("2026-01-01 99:99", libre.get("p_FechaHasta").getAsString());
        JsonObject usd = entradaCartola(run("CAN-LOG-", "moneda-usd-y-modulo-distinto"), 0);
        assertEquals("USD", usd.get("p_IdMoneda").getAsString());
        assertEquals("PLA", usd.get("p_IdModulo").getAsString());
    }

    @Test
    @DisplayName("D-CAN-10: Gson acepta numeros como texto y enteros con .0, rechaza lo no numerico con JsonSyntaxException, ignora campos desconocidos y distingue mayusculas")
    void D_CAN_10_gson_de_entrada() throws Exception {
        JsonObject texto = run("CAN-CAL-", "numeros-como-texto-se-aceptan");
        JsonObject e = entradaCartola(texto, 0);
        assertEquals(1646, e.get("p_IdUsuario").getAsLong());
        assertEquals(1, e.get("p_IdEmpresa").getAsLong());
        assertEquals(100, e.get("p_IdPortafolio").getAsLong());
        assertEquals(0, e.get("p_IdMatrizAgrupador").getAsLong());
        assertTrue(e.get("p_IdUsuario").getAsJsonPrimitive().isNumber());
        assertEquals("[[100]]", sentencia(texto, 0).getAsJsonArray("filas").toString());
        assertEquals(100, entradaCartola(run("CAN-CAL-", "enteros-con-punto-cero"), 0).get("p_IdPortafolio").getAsLong());
        for (String f : new String[] { "entero-con-decimales-reales", "portafolio-no-numerico", "matriz-texto-vacio" }) {
            JsonObject d = run("CAN-CAL-", f);
            assertEquals("JsonSyntaxException", excepcion(d), f);
            assertEquals(0, sentencias(d), f);
            assertEquals(0, d.getAsJsonArray("colaboradores").size(), f);
        }
        assertFalse(run("CAN-CAL-", "campos-desconocidos-se-ignoran").has("excepcion"));
        JsonObject minusculas = run("CAN-CAL-", "nombre-de-campo-en-minusculas");
        assertEquals("NullPointerException", excepcion(minusculas));
        assertEquals("[[null]]", sentencia(minusculas, 0).getAsJsonArray("filas").toString());
        assertEquals("NullPointerException", excepcion(run("CAN-CAL-", "json-vacio-falla-por-portafolio-nulo")));
        JsonObject nulosExplicitos = entradaCartola(run("CAN-CAL-", "nulos-explicitos-en-los-opcionales"), 0);
        assertFalse(nulosExplicitos.has("p_IdMatrizAgrupador"));
    }

    @Test
    @DisplayName("D-CAN-11: p_IdEstado y p_IdCuentaInversion van nulos y la cartola recibe los valores por omision S, N y S: no se puede filtrar por estado ni excluir anulados")
    void D_CAN_11_valores_fijos() throws Exception {
        JsonObject e = entradaCartola(run("CAN-LOG-", "la-cartola-no-recibe-estado-ni-cuenta"), 0);
        assertFalse(e.has("p_IdEstado") || e.has("p_IdCuentaInversion"));
        assertEquals("S", e.get("p_Validaciones").getAsString());
        assertEquals("N", e.get("p_IncluyeSaldo").getAsString());
        assertEquals("S", e.get("p_IncluyeAnulados").getAsString());
        assertEquals("N", e.get("p_SaldoPorFechaEfectiva").getAsString());
        assertEquals("S", e.get("p_UsaReglasNegocio").getAsString());
        assertEquals(0, movimientos(run("CAN-LOG-", "la-cartola-no-recibe-estado-ni-cuenta")).size());
    }

    @Test
    @DisplayName("D-CAN-12: las filas ajenas de la sesion se mezclan en el SELECT (el que repite id pierde frente a la fila propia, que se inserto despues) y el DELETE las borra todas")
    void D_CAN_12_filas_ajenas() throws Exception {
        JsonObject d = run("CAN-LOG-", "filas-ajenas-en-la-tabla-de-trabajo");
        assertEquals(1, filasIniciales(d, PROC).size());
        assertEquals(0, filas(d, PROC).size());
        assertEquals("[2]", sentencia(d, 3).getAsJsonArray("filasAfectadas").toString());
        assertEquals(1, movimientos(d).size());
        JsonObject mismo = run("CAN-LOG-", "fila-ajena-con-el-mismo-id");
        assertEquals(List.of("7001"), folios(mismo));
        JsonObject fallaInsercion = run("CAN-LOG-", "falla-la-insercion-con-filas-ajenas");
        assertEquals(1, filas(fallaInsercion, PROC).size());
        assertEquals("9001", t(filas(fallaInsercion, PROC).get(0).getAsJsonObject(), "IdCargoAbonoMovimiento"));
        assertEquals(2, filas(run("CAN-LOG-", "falla-el-select-con-filas-ajenas"), PROC).size());
    }

    // ---------------------------------------------------------------------------------------------------------------------------------- BD/Servicios

    @Test
    @DisplayName("D-CAN-13: procedimiento de historial de cuenta inversion: una fila por fila de la cartola (id repetido incluido), folio numerico, sin cliente hace RETURN, saldos 0, no asigna p_out_ErroresContador")
    void D_CAN_13_procedimiento_historial() throws Throwable {
        JsonObject repetido = run("CAN-PRC-", "historial-id-repetido");
        JsonArray f = repetido.getAsJsonArray("salida");
        assertEquals(2, f.size());
        assertNum("7001", f.get(0).getAsJsonObject().get("FolioComprobante"));
        assertNum("7002", f.get(1).getAsJsonObject().get("FolioComprobante"));
        JsonObject numerico = run("CAN-PRC-", "historial-el-folio-sale-numerico");
        assertTrue(numerico.getAsJsonArray("salida").get(0).getAsJsonObject().get("FolioComprobante").getAsJsonPrimitive().isNumber());
        assertNum("0", numerico.getAsJsonArray("salida").get(1).getAsJsonObject().get("FolioComprobante"));
        JsonObject sin = run("CAN-PRC-", "historial-sin-comprobante").getAsJsonArray("salida").get(0).getAsJsonObject();
        assertTrue(sin.get("FolioComprobante").isJsonNull());
        assertEquals("PF-100", t(sin, "CodigoPortafolio"));
        JsonObject fuera = run("CAN-PRC-", "historial-portafolio-fuera-de-la-vista").getAsJsonArray("salida").get(0).getAsJsonObject();
        assertNull(t(fuera, "CodigoPortafolio"));
        assertNull(t(fuera, "DocumentoIdentidad"));
        JsonObject renombra = run("CAN-PRC-", "historial-renombra-las-fechas").getAsJsonArray("salida").get(0).getAsJsonObject();
        assertEquals("2026-02-03", t(renombra, "FechaMovimiento"));
        assertEquals("2026-02-05", t(renombra, "FechaLiquidacion"));
        assertEquals(0, run("CAN-PRC-", "historial-cartola-vacia").getAsJsonArray("salida").size());
        String texto = ApiCanalAgente.textoProcedimiento("ConsultarHistorialMovimientosCuentaInversion");
        assertTrue(texto.contains("IF (IS_EMPTY(:v_Cliente)) THEN\n\t\tRETURN;"), "no client: RETURN without an error");
        assertTrue(texto.contains("p_SaldoEfectivoInicial = 0;") && texto.contains("p_SaldoEfectivoFinal = 0;"));
        assertTrue(texto.contains("\"BSCL.Portafolio.BD.Vistas::ConsultarPortafolio\" por ON por.\"IdPortafolio\" = car.\"IdPortafolio\""));
        assertFalse(java.util.regex.Pattern.compile("(?<!OUT )p_out_ErroresContador\\s*=(?!>)").matcher(texto).find(), "p_out_ErroresContador is never assigned");
        assertFalse(java.util.regex.Pattern.compile("p_out_AdvertenciasContador\\s*=(?!>)").matcher(texto).find());
    }

    @Test
    @DisplayName("D-CAN-14: procedimiento de historial de custodia: cabecera copiada, p_IdCliente comentado con :p_IdEmpresa y sin asignar contadores (texto fijado por SHA-256; no ejecutable en H2)")
    void D_CAN_14_procedimiento_custodia() throws Throwable {
        String texto = ApiCanalAgente.textoProcedimiento("ConsultarHistorialMovimientosCustodia");
        assertTrue(texto.contains("--  Proposito: Consulta Historial de Movimientos Cuenta Inversion"));
        assertTrue(texto.contains("--p_IdCliente"));
        assertTrue(texto.contains(":p_IdEmpresa,\n"), "the commented p_IdCliente argument carries p_IdEmpresa");
        assertTrue(texto.contains("CALL \"BSCL.Custodia.BD.Servicios::ConsultarCartolaMovimientos\""));
        assertFalse(java.util.regex.Pattern.compile("p_out_(Errores|Advertencias)Contador\\s*=(?!>)").matcher(texto).find());
    }

    @Test
    @DisplayName("D-CAN-15: ConsultaAvanzadaClientes: el INNER JOIN con CLI.Cliente descarta los clientes sin fila, renombra el bloqueo del portafolio y agrega IdPersona")
    void D_CAN_15_consulta_avanzada() throws Throwable {
        JsonArray f = run("CAN-PRC-", "clientes-sin-fila-de-cliente-se-descartan").getAsJsonArray("salida");
        assertEquals(1, f.size());
        assertEquals(10, f.get(0).getAsJsonObject().get("IdCliente").getAsLong());
        assertEquals(1, f.get(0).getAsJsonObject().get("IdPersona").getAsLong());
        JsonObject b = run("CAN-PRC-", "clientes-renombra-el-bloqueo").getAsJsonArray("salida").get(0).getAsJsonObject();
        assertEquals("TMP", t(b, "IdTipoBloqueo"));
        assertEquals("Temporal", t(b, "NombreTipoBloqueo"));
        assertFalse(b.has("IdTipoBloqueoPortafolio"));
        assertEquals(3, run("CAN-PRC-", "clientes-varios-portafolios").getAsJsonArray("salida").size());
        assertEquals(0, run("CAN-PRC-", "clientes-sin-filas-devuelve-vacio").getAsJsonArray("salida").size());
        assertEquals(2, run("CAN-PRC-", "clientes-con-fila-de-cliente-agrega-idpersona").getAsJsonArray("salida").size());
        String texto = ApiCanalAgente.textoProcedimiento("ConsultaAvanzadaClientes");
        assertTrue(texto.contains("INNER JOIN \"BSCL.SocioNegocio.BD::CLI.Cliente\" CL ON CL.\"IdCliente\" = C.\"IdCliente\""));
        assertTrue(java.util.regex.Pattern.compile("p_out_Clientes\\s*=> v_out_Clientes").matcher(texto).find());
        assertFalse(texto.contains("DECLARE v_out_Clientes"), "the table variable v_out_Clientes is never declared");
    }

    @Test
    @DisplayName("D-CAN-16 / D-CAN-17: los scripts de prueba y el procedimiento ConsultarFlujoInstrumento no se portan (sin caso: documentados en reglas.json)")
    void D_CAN_16_17_no_se_portan() {
        JsonObject r = json(CasoCanal.texto("reglas.json"));
        assertTrue(r.getAsJsonObject("defectos").has("D-CAN-16") && r.getAsJsonObject("defectos").has("D-CAN-17"));
        Set<String> sinCaso = new TreeSet<>();
        r.getAsJsonArray("sinCaso").forEach(e -> sinCaso.add(e.getAsString()));
        assertTrue(sinCaso.containsAll(Set.of("D-CAN-16", "D-CAN-17")));
    }
}
