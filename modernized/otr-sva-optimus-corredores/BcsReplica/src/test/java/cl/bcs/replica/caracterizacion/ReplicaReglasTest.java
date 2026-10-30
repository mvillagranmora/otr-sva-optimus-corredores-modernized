package cl.bcs.replica.caracterizacion;

import static cl.bcs.replica.caracterizacion.VolcadoAyuda.HLI;
import static cl.bcs.replica.caracterizacion.VolcadoAyuda.HOP;
import static cl.bcs.replica.caracterizacion.VolcadoAyuda.POST;
import static cl.bcs.replica.caracterizacion.VolcadoAyuda.RESUMEN;
import static cl.bcs.replica.caracterizacion.VolcadoAyuda.SEF;
import static cl.bcs.replica.caracterizacion.VolcadoAyuda.SFM;
import static cl.bcs.replica.caracterizacion.VolcadoAyuda.WS;
import static cl.bcs.replica.caracterizacion.VolcadoAyuda.colaboradores;
import static cl.bcs.replica.caracterizacion.VolcadoAyuda.columna;
import static cl.bcs.replica.caracterizacion.VolcadoAyuda.contar;
import static cl.bcs.replica.caracterizacion.VolcadoAyuda.excepcion;
import static cl.bcs.replica.caracterizacion.VolcadoAyuda.filas;
import static cl.bcs.replica.caracterizacion.VolcadoAyuda.parametrosWs;
import static cl.bcs.replica.caracterizacion.VolcadoAyuda.run;
import static cl.bcs.replica.caracterizacion.VolcadoAyuda.sentencias;
import static cl.bcs.replica.caracterizacion.VolcadoAyuda.sql;
import static cl.bcs.replica.caracterizacion.VolcadoAyuda.tablasWs;
import static cl.bcs.replica.caracterizacion.VolcadoAyuda.tipos;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * The rules (B-REP-nn) and the defects (D-REP-nn) of BcsReplica one by one, with the expected value written out in the test: what each rule says is readable here, and the equivalence test
 * (goldens recorded from the legacy) is the proof that the whole behaviour matches. The ids are those of reglas.json. The legacy is replicated bug for bug: every D-REP test pins a defect
 * the legacy has today.
 */
class ReplicaReglasTest {

    private static final String CIERRE = ";serialDelete:false;truncarDestino:false;serialLaunch:false";
    private static final String HIST_CI = "tables:CuentaInversion_SaldoFechaEfectivaHist,CuentaInversion_SaldoFechaMovimientoHist;BCS_query:QueryReplicaDelta;Fecha:";
    private static final String HIST_CUS = "tables:Custodia_CustodiaFechaOperacionHist,Custodia_CustodiaFechaLiquidacionHist;BCS_query:QueryReplicaDelta;Fecha:";
    private static final String PATRIMONIO = "tables:CuentaInversion_PatrimonioCajaFechaLiquidacion,CuentaInversion_PatrimonioCajaFechaOperacion,Custodia_PatrimonioConsolidadoFechaLiquidacion,"
            + "Custodia_PatrimonioConsolidadoFechaOperacion,Custodia_PatrimonioCustodiaFechaLiquidacion,Custodia_PatrimonioCustodiaFechaOperacion;BCS_query:QueryReplicaDelta;Fecha:";
    private static final List<String> SEBRA = List.of("TBCPDTPC", "TBCPMSPC", "TBCTCIRF", "TBCTCURF", "TBCTCIRV", "TBCTCURV", "TBCTCIFM", "TBCTCUFM", "TBCTCTCL", "TBCTTRTP", "TBCTCIIF", "TBCTGTIA",
            "TBCTTRPT");

    // ---------------------------------------------------------------------------------------------------------------------------------- IniciarReplica

    @Test
    @DisplayName("B-REP-01 / B-REP-02: IniciarReplicaFull encola la pauta con la consulta QueryReplica y la fecha de sistema de la empresa")
    void B_REP_01_02_full() throws Exception {
        JsonObject d = run("REP-INI-", "full-logica-encola-la-pauta");
        JsonArray f = filas(d, WS);
        assertEquals(1, f.size());
        JsonObject w = f.get(0).getAsJsonObject();
        assertEquals(41, w.get("IdStack").getAsLong());
        assertEquals("-1", w.get("Key").getAsString());
        assertEquals("INICIAR_REPLICA_PAUTA", w.get("Table").getAsString());
        assertEquals("F", w.get("Action").getAsString());
        assertEquals(0, w.get("Status").getAsInt());
        assertEquals("2026-03-02 14:30:00", w.get("Stamp").getAsString());
        assertEquals("BCS_query:QueryReplica;Fecha:2026-03-04", w.get("Parameters").getAsString());
        assertEquals("{\"P_OUT_MENSAJES\":[{\"id\":0,\"tipo\":\"OK\",\"codigo\":\"INICIAR_REPLICA_PAUTA\",\"descripcion\":\"Se inicia Replica desde Pauta\"}],\"P_OUT_ERRORESCONTADOR\":0}",
                d.get("salida").toString());
    }

    @Test
    @DisplayName("B-REP-02: IniciarReplicaDelta es igual a Full salvo la consulta QueryReplicaDelta")
    void B_REP_02_delta() throws Exception {
        JsonObject d = run("REP-INI-", "delta-logica-encola-la-pauta");
        JsonObject w = filas(d, WS).get(0).getAsJsonObject();
        assertEquals("BCS_query:QueryReplicaDelta;Fecha:2026-03-04", w.get("Parameters").getAsString());
        assertEquals("INICIAR_REPLICA_PAUTA", w.get("Table").getAsString());
        assertEquals("F", w.get("Action").getAsString());
        assertEquals("-1", w.get("Key").getAsString());
    }

    @Test
    @DisplayName("B-REP-02: de la secuencia se usa el primer valor y se pide una sola vez, con el nombre de la secuencia WorkStack")
    void B_REP_02_secuencia() throws Exception {
        JsonObject d = run("REP-INI-", "full-logica-secuencia-con-varios-valores");
        assertEquals(41, filas(d, WS).get(0).getAsJsonObject().get("IdStack").getAsLong());
        JsonArray s = colaboradores(d, "Utils", "getSecuencia");
        assertEquals(1, s.size());
        assertEquals("[\"BSCL.Plataforma.BD.Secuencias::WorkStack\",1]", s.get(0).getAsJsonObject().get("argumentos").toString());
    }

    @Test
    @DisplayName("B-REP-01: la fecha de sistema se pide con el esquema \"BSCL\" y la empresa de la entrada")
    void B_REP_01_fecha_de_la_empresa() throws Exception {
        JsonObject d = run("REP-INI-", "full-logica-la-empresa-llega");
        JsonArray c = colaboradores(d, "Utils", "getFechaSistema");
        assertEquals("[\"BSCL\",7]", c.get(0).getAsJsonObject().get("argumentos").toString());
        assertEquals("BCS_query:QueryReplica;Fecha:2026-05-05", columna(filas(d, WS), "Parameters").get(0));
    }

    @Test
    @DisplayName("B-REP-24: call devuelve el JSON de la salida con el contador de errores recontado (0 mensajes de error)")
    void B_REP_24_call() throws Exception {
        JsonObject d = run("REP-INI-", "delta-call-devuelve-el-mensaje-ok");
        assertEquals("{\"P_OUT_MENSAJES\":[{\"id\":0,\"tipo\":\"OK\",\"codigo\":\"INICIAR_REPLICA_PAUTA\",\"descripcion\":\"Se inicia Replica desde Pauta\"}],\"P_OUT_ERRORESCONTADOR\":0}",
                d.get("salida").toString());
        assertEquals("BCS_query:QueryReplicaDelta;Fecha:2026-03-04", columna(filas(d, WS), "Parameters").get(0));
    }

    @Test
    @DisplayName("B-REP-24: call lee la empresa y el usuario del JSON, aceptando numeros escritos como texto")
    void B_REP_24_call_texto_numerico() throws Exception {
        JsonObject d = run("REP-INI-", "full-call-empresa-como-texto-numerico");
        assertNull(excepcion(d));
        assertEquals(1, filas(d, WS).size());
        assertEquals("[\"BSCL\",1]", colaboradores(d, "Utils", "getFechaSistema").get(0).getAsJsonObject().get("argumentos").toString());
    }

    @Test
    @DisplayName("D-REP-17: la fila encolada lleva Key \"-1\" y el Stamp de la hora de la JVM; el usuario no cuenta")
    void D_REP_17_usuario_ignorado() throws Exception {
        JsonObject a = run("REP-INI-", "full-logica-encola-la-pauta");
        JsonObject b = run("REP-INI-", "full-logica-usuario-nulo-no-importa");
        assertEquals(filas(a, WS).toString(), filas(b, WS).toString());
        assertEquals("2026-12-31 23:59:59", filas(run("REP-INI-", "full-logica-secuencia-grande"), WS).get(0).getAsJsonObject().get("Stamp").getAsString());
    }

    @Test
    @DisplayName("D-REP-18: empresa nula falla con NullPointerException despues de pedir la secuencia; la empresa no numerica falla en el JSON")
    void D_REP_18_sin_validacion() throws Exception {
        JsonObject d = run("REP-INI-", "delta-logica-empresa-nula");
        assertEquals("NullPointerException", excepcion(d));
        assertEquals(1, colaboradores(d, "Utils", "getSecuencia").size());
        assertEquals(0, colaboradores(d, "Utils", "getFechaSistema").size());
        assertEquals(0, sentencias(d));
        assertEquals("JsonSyntaxException", excepcion(run("REP-INI-", "full-call-empresa-no-numerica")));
        assertEquals("NullPointerException", excepcion(run("REP-INI-", "full-call-json-vacio")));
        assertEquals("IndexOutOfBoundsException", excepcion(run("REP-INI-", "full-logica-secuencia-vacia")));
    }

    // ---------------------------------------------------------------------------------------------------------------------------------- EnviarTablaReplica

    @Test
    @DisplayName("B-REP-05: el proceso se compara sin distinguir mayusculas; uno desconocido, con espacios o truncado no escribe nada (D-REP-16)")
    void B_REP_05_proceso() throws Exception {
        assertEquals(List.of("CUENTA INVERSION DIA ACTUAL"), tablasWs(run("REP-ENV-", "proceso-en-minusculas")));
        assertEquals(List.of("PATRIMONIO DIA ACTUAL"), tablasWs(run("REP-ENV-", "pauta-patrimonio-minusculas-y-mezcla")));
        for (String f : new String[] { "proceso-con-espacio-no-coincide", "no-hace-nada-proceso-desconocido", "no-hace-nada-proceso-vacio", "no-hace-nada-fin-pauta-con-espacio-final",
                "no-hace-nada-prefijo-no-basta", "no-hace-nada-nombre-truncado", "no-hace-nada-proceso-del-main-del-legacy" }) {
            JsonObject d = run("REP-ENV-", f);
            assertEquals(0, sentencias(d), f);
            assertEquals(0, d.getAsJsonArray("colaboradores").size(), f);
            assertNull(excepcion(d), f);
            assertEquals(0, filas(d, WS).size(), f);
        }
    }

    @Test
    @DisplayName("D-REP-16: un proceso nulo o ausente falla con NullPointerException")
    void D_REP_16_proceso_nulo() throws Exception {
        assertEquals("NullPointerException", excepcion(run("REP-ENV-", "inicio-pauta-proceso-nulo")));
        assertEquals("NullPointerException", excepcion(run("REP-ENV-", "inicio-pauta-sin-proceso")));
    }

    @Test
    @DisplayName("B-REP-06: INICIO_PAUTA apaga los triggers, encola el dia actual, limpia e inserta el historico (cuenta inversion y custodia) y encola al final")
    void B_REP_06_inicio_pauta() throws Exception {
        JsonObject d = run("REP-ENV-", "inicio-pauta-sin-inhabiles");
        assertEquals(List.of("DROP", "DROP", "DELETE", "DELETE", "DELETE", "DELETE", "INSERT", "INSERT", "DELETE", "DELETE", "DELETE", "DELETE", "INSERT", "INSERT", "batch:INSERT"), tipos(d));
        assertTrue(sql(d).get(0).startsWith("DROP TRIGGER \"BSCL.Plataforma.BD::PLA.Replica.TriggerReplicaUpdateTablaCartera\""));
        assertTrue(sql(d).get(1).startsWith("DROP TRIGGER \"BSCL.Plataforma.BD::PLA.Replica.TriggerReplicaUpdateTablaCuentaInversion_SaldoFechaMovimiento\""));
        assertEquals(List.of("CUENTA INVERSION DIA ACTUAL"), tablasWs(d));
        assertEquals(List.of(HIST_CI + "2026-03-04" + CIERRE), parametrosWs(d));
        assertEquals(1, colaboradores(d, "ConsultarEmpresa", "logica").size());
        assertEquals(1, colaboradores(d, "ObtenerDiaSiguiente", "logica").size());
        assertEquals(1, colaboradores(d, "Feriados", "esDiaHabil").size());
    }

    @Test
    @DisplayName("B-REP-07: VALORIZAR_FFMM encola CUSTODIA ACTUAL y CARTERAFM (truncarDestino:true) y respalda los dias inhabiles de custodia")
    void B_REP_07_valorizar_ffmm() throws Exception {
        JsonObject d = run("REP-ENV-", "valorizar-ffmm-sin-inhabiles");
        assertEquals(List.of("CUSTODIA ACTUAL", "CARTERAFM"), tablasWs(d));
        assertEquals(List.of("tables:Custodia_CustodiaFechaLiquidacionHist,Custodia_CustodiaFechaOperacionHist;BCS_query:QueryReplicaDelta;Fecha:2026-03-04" + CIERRE,
                "tables:Custodia_CarteraFM;BCS_query:QueryReplica;Fecha:2026-03-04;serialDelete:false;truncarDestino:true;serialLaunch:false"), parametrosWs(d));
        assertEquals(List.of("batch:INSERT"), tipos(d));
    }

    @Test
    @DisplayName("B-REP-08: PAUTA_PATRIMONIO encola un solo Workstack con las seis tablas de patrimonio")
    void B_REP_08_pauta_patrimonio() throws Exception {
        JsonObject d = run("REP-ENV-", "pauta-patrimonio-encola-sin-tocar-datos");
        assertEquals(List.of("PATRIMONIO DIA ACTUAL"), tablasWs(d));
        assertEquals(List.of(PATRIMONIO + "2026-03-04" + CIERRE), parametrosWs(d));
        assertEquals(List.of("batch:INSERT"), tipos(d));
        assertEquals(0, d.getAsJsonArray("colaboradores").size());
    }

    @Test
    @DisplayName("B-REP-09: COPIA_PATRIMONIO_PARA_INHABIL encola un Workstack por cada dia inhabil siguiente y nada si el siguiente es habil")
    void B_REP_09_copia_patrimonio() throws Exception {
        JsonObject d = run("REP-ENV-", "copia-patrimonio-fin-de-semana-dos-workstack");
        assertEquals(List.of("PATRIMONIO DIA 2026-03-07", "PATRIMONIO DIA 2026-03-08"), tablasWs(d));
        assertEquals(List.of(PATRIMONIO + "2026-03-07" + CIERRE, PATRIMONIO + "2026-03-08" + CIERRE), parametrosWs(d));
        assertEquals(3, colaboradores(d, "Feriados", "esDiaHabil").size());
        JsonObject habil = run("REP-ENV-", "copia-patrimonio-siguiente-habil-no-encola");
        assertEquals(0, sentencias(habil));
        assertEquals(0, filas(habil, WS).size());
    }

    @Test
    @DisplayName("B-REP-10: PAUTA_RESPALDAR_CUSTODIA encola CARTERAFMHIS, CARTERARVHIS y GARANTIAHIS")
    void B_REP_10_respaldar_custodia() throws Exception {
        JsonObject d = run("REP-ENV-", "pauta-respaldar-custodia-encola-sin-tocar-datos");
        assertEquals(List.of("CARTERAFMHIS ACTUAL", "CARTERARVHIS ACTUAL", "GARANTIAHIS ACTUAL"), tablasWs(d));
        assertEquals(List.of("tables:Custodia_CarteraFMHis;BCS_query:QueryReplicaDelta;Fecha:2026-03-04" + CIERRE, "tables:Custodia_CarteraRVHis;BCS_query:QueryReplicaDelta;Fecha:2026-03-04" + CIERRE,
                "tables:Custodia_GarantiaHis;BCS_query:QueryReplicaDelta;Fecha:2026-03-04" + CIERRE), parametrosWs(d));
    }

    @Test
    @DisplayName("B-REP-11: CAMBIO_FECHA_SISTEMA encola CUENTA INVERSION DIA SIGUIENTE")
    void B_REP_11_cambio_fecha() throws Exception {
        JsonObject d = run("REP-ENV-", "cambio-fecha-sistema-encola-sin-tocar-datos");
        assertEquals(List.of("CUENTA INVERSION DIA SIGUIENTE"), tablasWs(d));
        assertEquals(List.of("tables:CuentaInversion_SaldoFechaEfectiva,CuentaInversion_SaldoFechaMovimiento;BCS_query:QueryReplicaDelta;Fecha:2026-03-04" + CIERRE), parametrosWs(d));
    }

    @Test
    @DisplayName("B-REP-12 / D-REP-05: VALORIZAR_CARTERA_RV encola CARTERARV con truncarDestino:true y un espacio sobrante despues de la fecha")
    void B_REP_12_D_REP_05_cartera_rv() throws Exception {
        JsonObject d = run("REP-ENV-", "valorizar-cartera-rv-precio-mercado-encola-sin-tocar-datos");
        assertEquals(List.of("CARTERARV"), tablasWs(d));
        assertEquals(List.of("tables:Custodia_CarteraRV;BCS_query:QueryReplica;Fecha:2026-03-04 ;serialDelete:false;truncarDestino:true;serialLaunch:false"), parametrosWs(d));
    }

    @Test
    @DisplayName("B-REP-13: FIN_PAUTA encola, lee el desfase horario, reactiva los triggers, registra el resumen y limpia e inserta el historico")
    void B_REP_13_fin_pauta() throws Exception {
        JsonObject d = run("REP-ENV-", "fin-pauta-completo-offset-cero");
        assertEquals(List.of("CREATE", "CREATE", "batch:UPSERT", "DELETE", "DELETE", "DELETE", "DELETE", "INSERT", "INSERT", "DELETE", "DELETE", "DELETE", "DELETE", "INSERT", "INSERT", "batch:INSERT"), tipos(d));
        assertEquals(List.of("CUSTODIA DIA SIGUIENTE"), tablasWs(d));
        assertEquals(List.of("tables:Custodia_CustodiaFechaLiquidacion,Custodia_CustodiaFechaOperacion;BCS_query:QueryReplicaDelta;Fecha:2026-03-06" + CIERRE), parametrosWs(d));
        assertEquals("[1,\"PLATAFORMA\",\"PLA.SegundosZonaHoraria\"]", colaboradores(d, "ConfiguracionSistema", "getParametroConfiguracionLong").get(0).getAsJsonObject().get("argumentos").toString());
        JsonObject r = filas(d, RESUMEN).get(0).getAsJsonObject();
        assertEquals("PAUTA", r.get("Tipo").getAsString());
        assertEquals("FIN_PAUTA", r.get("Nombre").getAsString());
        assertEquals("OK", r.get("Estado").getAsString());
        assertEquals("FIN_PAUTA", r.get("Detalle").getAsString());
        assertEquals("2026-03-06", r.get("FechaSistema").getAsString());
        assertEquals(0, colaboradores(d, "ObtenerDiaSiguiente", "logica").size());
    }

    @Test
    @DisplayName("D-REP-09: la UltimaActualizacion del resumen es la hora del reloj mas los segundos del parametro (positivos, negativos, cruzando el dia o el ano); la clave la incluye")
    void D_REP_09_ultima_actualizacion() throws Exception {
        assertEquals("2026-03-02 14:30:00", columna(filas(run("REP-ENV-", "fin-pauta-completo-offset-cero"), RESUMEN), "UltimaActualizacion").get(0));
        assertEquals("2026-03-02 15:30:00", columna(filas(run("REP-ENV-", "fin-pauta-offset-positivo"), RESUMEN), "UltimaActualizacion").get(0));
        assertEquals("2026-03-02 10:30:00", columna(filas(run("REP-ENV-", "fin-pauta-offset-negativo"), RESUMEN), "UltimaActualizacion").get(0));
        assertEquals("2026-03-02 23:40:00", columna(filas(run("REP-ENV-", "fin-pauta-offset-cruza-la-medianoche"), RESUMEN), "UltimaActualizacion").get(0));
        assertEquals("2027-01-01 00:30:00", columna(filas(run("REP-ENV-", "fin-pauta-reloj-fin-de-ano"), RESUMEN), "UltimaActualizacion").get(0));
        JsonArray mismo = filas(run("REP-ENV-", "fin-pauta-mismo-segundo-actualiza"), RESUMEN);
        assertEquals(2, mismo.size());
        assertEquals("2026-03-05", mismo.get(0).getAsJsonObject().get("FechaSistema").getAsString());
        assertEquals("2026-03-06", mismo.get(1).getAsJsonObject().get("FechaSistema").getAsString());
        assertEquals("OK", mismo.get(1).getAsJsonObject().get("Estado").getAsString());
        assertEquals("FIN_PAUTA", mismo.get(1).getAsJsonObject().get("Detalle").getAsString());
        assertEquals(List.of("2026-03-02 14:29:59", "2026-03-02 14:30:00"), columna(filas(run("REP-ENV-", "fin-pauta-otro-segundo-agrega-fila"), RESUMEN), "UltimaActualizacion"));
    }

    @Test
    @DisplayName("D-REP-19: FIN_PAUTA acepta la fecha con hora; con otro formato falla con DateTimeParseException y con nulo con NullPointerException, antes de escribir y de encolar")
    void D_REP_19_fecha_del_resumen() throws Exception {
        JsonObject hora = run("REP-ENV-", "fin-pauta-fecha-con-hora-para-el-resumen");
        assertNull(excepcion(hora));
        assertEquals("2026-03-06", columna(filas(hora, RESUMEN), "FechaSistema").get(0));
        for (String f : new String[] { "fin-pauta-fecha-mal-formada", "fin-pauta-fecha-nula" }) {
            JsonObject d = run("REP-ENV-", f);
            assertEquals(0, sentencias(d), f);
            assertEquals(0, filas(d, WS).size(), f);
            assertEquals(1, colaboradores(d, "ConfiguracionSistema", "getParametroConfiguracionLong").size(), f);
        }
        assertEquals("DateTimeParseException", excepcion(run("REP-ENV-", "fin-pauta-fecha-mal-formada")));
        assertEquals("NullPointerException", excepcion(run("REP-ENV-", "fin-pauta-fecha-nula")));
    }

    @Test
    @DisplayName("B-REP-13: un parametro de desfase ausente o no numerico detiene FIN_PAUTA antes de escribir")
    void B_REP_13_desfase_invalido() throws Exception {
        assertEquals("Exception", excepcion(run("REP-ENV-", "fin-pauta-parametro-ausente")));
        for (String f : new String[] { "fin-pauta-parametro-no-numerico", "fin-pauta-parametro-vacio", "fin-pauta-parametro-nulo" }) {
            JsonObject d = run("REP-ENV-", f);
            assertEquals("NumberFormatException", excepcion(d), f);
            assertEquals(0, sentencias(d), f);
        }
    }

    // ---------------------------------------------------------------------------------------------------------------------------------- dias inhabiles

    @Test
    @DisplayName("B-REP-14 / B-REP-16: por cada dia inhabil se borra el destino, se copia el origen y se encola el Workstack con la fecha destino")
    void B_REP_14_16_dias_inhabiles() throws Exception {
        JsonObject d = run("REP-ENV-", "inicio-pauta-fin-de-semana-copia-dos-dias");
        assertEquals(List.of("CUENTA INVERSION DIA ACTUAL", "CUENTA INVERSION DIA INHABIL", "CUENTA INVERSION DIA INHABIL"), tablasWs(d));
        assertEquals(List.of(HIST_CI + "2026-03-06" + CIERRE, HIST_CI + "2026-03-07" + CIERRE, HIST_CI + "2026-03-08" + CIERRE), parametrosWs(d));
        List<String> t = tipos(d);
        assertEquals(List.of("DELETE", "DELETE", "batch:INSERT", "batch:INSERT", "DELETE", "DELETE", "batch:INSERT", "batch:INSERT", "batch:INSERT"), t.subList(t.size() - 9, t.size()));
        assertEquals(3, colaboradores(d, "ObtenerDiaSiguiente", "logica").size());
        JsonObject p = colaboradores(d, "ObtenerDiaSiguiente", "logica").get(0).getAsJsonObject().getAsJsonArray("argumentos").get(0).getAsJsonObject();
        assertEquals("{\"p_idPais\":56,\"p_diaHabil\":\"N\",\"p_soloCaeDiaHabil\":\"N\",\"p_consideraHabilAnterior\":\"N\",\"p_fechas\":[{\"fechaInicial\":\"2026-03-06\",\"numeroDias\":1,\"pais\":56,\"IdInterno\":0}]}", p.toString());
    }

    @Test
    @DisplayName("B-REP-14: en custodia se borran liquidacion y operacion del destino y luego se copian liquidacion y operacion")
    void B_REP_14_custodia() throws Exception {
        JsonObject d = run("REP-ENV-", "valorizar-ffmm-borra-el-destino-antes-de-copiar");
        List<String> s = sql(d);
        assertTrue(s.get(0).startsWith("DELETE FROM \"BSCL.Custodia.BD::HIS.CustodiaFechaLiquidacion\" WHERE \"FechaCustodia\" = '2026-03-07'"));
        assertTrue(s.get(1).startsWith("DELETE FROM \"BSCL.Custodia.BD::HIS.CustodiaFechaOperacion\" WHERE \"FechaCustodia\" = '2026-03-07'"));
        assertTrue(s.get(2).startsWith("INSERT INTO \"BSCL.Custodia.BD::HIS.CustodiaFechaLiquidacion\""));
        assertTrue(s.get(3).startsWith("INSERT INTO \"BSCL.Custodia.BD::HIS.CustodiaFechaOperacion\""));
        assertEquals(List.of("2026-03-06", "2026-03-06", "2026-03-07", "2026-03-07"), columna(filas(d, HLI), "FechaCustodia"));
        assertEquals(List.of("10", "11", "10", "11"), columna(filas(d, HLI), "IdCliente"));
    }

    @Test
    @DisplayName("B-REP-22: la copia recalcula el Checksum (-IdCuentaInversion con saldo cero, la suma ponderada, NULL si falta una columna)")
    void B_REP_22_checksum() throws Exception {
        JsonObject d = run("REP-ENV-", "inicio-pauta-fin-de-semana-copia-dos-dias");
        List<String> chk = columna(filas(d, SEF), "Checksum");
        List<String> fechas = columna(filas(d, SEF), "FechaSaldo");
        assertEquals(9, chk.size());
        for (int i = 0; i < chk.size(); i += 3) {
            assertEquals("1518", chk.get(i));
            assertEquals("-2", chk.get(i + 1));
            assertNull(chk.get(i + 2));
        }
        assertEquals(List.of("2026-03-06", "2026-03-06", "2026-03-06", "2026-03-07", "2026-03-07", "2026-03-07", "2026-03-08", "2026-03-08", "2026-03-08"), fechas);
    }

    @Test
    @DisplayName("B-REP-14: la copia solo trae las filas de la fecha origen")
    void B_REP_14_solo_la_fecha_origen() throws Exception {
        JsonObject d = run("REP-ENV-", "inicio-pauta-copia-no-trae-lo-de-otras-fechas");
        assertEquals(List.of("2026-03-05", "2026-03-05", "2026-03-06", "2026-03-06", "2026-03-06", "2026-03-07", "2026-03-07", "2026-03-07", "2026-03-10"), columna(filas(d, SEF), "FechaSaldo"));
    }

    @Test
    @DisplayName("D-REP-04: si el dia siguiente da error el ciclo termina sin excepcion y se encolan los Workstack ya acumulados")
    void D_REP_04_error_dia_siguiente() throws Exception {
        JsonObject d = run("REP-ENV-", "inicio-pauta-error-en-el-segundo-dia-conserva-el-primero");
        assertNull(excepcion(d));
        assertEquals(List.of("CUENTA INVERSION DIA ACTUAL", "CUENTA INVERSION DIA INHABIL"), tablasWs(d));
        assertEquals(List.of("2026-03-06", "2026-03-06", "2026-03-06", "2026-03-07", "2026-03-07", "2026-03-07"), columna(filas(d, SEF), "FechaSaldo"));
        JsonObject primero = run("REP-ENV-", "inicio-pauta-dia-siguiente-con-error-corta-y-encola-lo-previo");
        assertEquals(List.of("CUENTA INVERSION DIA ACTUAL"), tablasWs(primero));
        assertEquals(0, colaboradores(primero, "Feriados", "esDiaHabil").size());
        assertEquals(List.of("CUENTA INVERSION DIA ACTUAL"), tablasWs(run("REP-ENV-", "inicio-pauta-contador-dos-tambien-corta")));
    }

    @Test
    @DisplayName("D-REP-03: COPIA_PATRIMONIO_PARA_INHABIL sale sin encolar nada si el dia siguiente da error, aunque ya haya acumulado dias")
    void D_REP_03_copia_patrimonio_pierde_lo_acumulado() throws Exception {
        JsonObject d = run("REP-ENV-", "copia-patrimonio-error-en-el-segundo-dia-pierde-el-primero");
        assertNull(excepcion(d));
        assertEquals(0, sentencias(d));
        assertEquals(2, colaboradores(d, "ObtenerDiaSiguiente", "logica").size());
        assertEquals(0, sentencias(run("REP-ENV-", "copia-patrimonio-error-en-el-primer-dia-no-encola-nada")));
    }

    @Test
    @DisplayName("D-REP-20: una tabla distinta de las dos conocidas (comparacion exacta) encola los Workstack y no copia datos")
    void D_REP_20_tabla_desconocida() throws Exception {
        for (String f : new String[] { "tabla-desconocida-encola-sin-copiar", "tabla-en-minusculas-no-coincide", "tabla-nula-es-una-tabla-desconocida" }) {
            JsonObject d = run("REP-INH-", f);
            assertEquals(0, sentencias(d), f);
            assertNull(excepcion(d), f);
            assertTrue(d.getAsJsonArray("salida").size() >= 1, f);
        }
        JsonObject d = run("REP-INH-", "tabla-desconocida-encola-sin-copiar");
        assertEquals("[{\"Key\":\"\",\"Table\":\"OTRA TABLA\",\"Action\":\"M\",\"Status\":0,\"Parameters\":\"x;Fecha:2026-03-07;serialDelete:false;truncarDestino:false;serialLaunch:false\"},"
                + "{\"Key\":\"\",\"Table\":\"OTRA TABLA\",\"Action\":\"M\",\"Status\":0,\"Parameters\":\"x;Fecha:2026-03-08;serialDelete:false;truncarDestino:false;serialLaunch:false\"}]", d.get("salida").toString());
    }

    @Test
    @DisplayName("B-REP-14 / B-REP-15: respaldarDataDiaInhabil devuelve la lista y no inserta los Workstack (los inserta quien la llama); con siguiente habil devuelve una lista vacia")
    void B_REP_15_lista_devuelta() throws Exception {
        JsonObject d = run("REP-INH-", "cuenta-inversion-fin-de-semana");
        assertEquals(2, d.getAsJsonArray("salida").size());
        assertEquals(0, contar(d, "INSERT INTO \"BSCL.Plataforma"));
        assertEquals("[]", run("REP-INH-", "siguiente-habil-devuelve-lista-vacia").get("salida").toString());
        assertEquals("[]", run("REP-INH-", "error-en-el-primer-dia-devuelve-lista-vacia").get("salida").toString());
    }

    @Test
    @DisplayName("D-REP-21: los Workstack se insertan despues de las copias de los dias inhabiles: si falla la insercion final las copias quedan")
    void D_REP_21_orden_de_las_inserciones() throws Exception {
        JsonObject d = run("REP-ENV-", "inicio-pauta-falla-insercion-del-workstack");
        assertEquals("SQLException", excepcion(d));
        assertEquals(0, filas(d, WS).size());
        JsonObject copia = run("REP-ENV-", "inicio-pauta-falla-copia-dia-inhabil-deja-la-mitad");
        assertEquals("SQLException", excepcion(copia));
        assertEquals(0, filas(copia, WS).size());
        assertEquals(List.of("2026-03-06", "2026-03-06", "2026-03-06", "2026-03-07", "2026-03-07", "2026-03-07"), columna(filas(copia, SEF), "FechaSaldo"));
        assertEquals(List.of("2026-03-06", "2026-03-06"), columna(filas(copia, SFM), "FechaSaldo"));
    }

    @Test
    @DisplayName("B-REP-21: la retencion borra las fechas <= fecha de sistema - 10 dias (limite inclusivo) y las de la fecha de sistema")
    void B_REP_21_retencion() throws Exception {
        JsonObject d = run("REP-ENV-", "inicio-pauta-retencion-diez-dias");
        assertEquals(List.of("2026-02-25", "2026-03-05"), columna(filas(d, SEF), "FechaSaldo"));
        assertEquals(List.of("2026-02-25", "2026-03-05"), columna(filas(d, SFM), "FechaSaldo"));
        assertEquals(List.of("2026-02-25", "2026-03-05"), columna(filas(d, HLI), "FechaCustodia"));
        assertEquals(List.of("2026-02-25", "2026-03-05"), columna(filas(d, HOP), "FechaCustodia"));
        assertTrue(sql(d).get(2).contains("ADD_DAYS('2026-03-06',-10)"));
        assertTrue(sql(d).get(4).endsWith("\"FechaSaldo\" = '2026-03-06'"));
    }

    @Test
    @DisplayName("B-REP-06: el historico que se inserta toma lo que entregan las vistas analiticas, despues de borrar la fecha de sistema (D-REP-08: empresa 1 y fecha 0001-01-01 fijas en el SQL)")
    void B_REP_06_D_REP_08_historico() throws Exception {
        JsonObject d = run("REP-ENV-", "inicio-pauta-borra-la-fecha-del-sistema-antes-del-historico");
        assertEquals(List.of("1000", "0", "-250"), columna(filas(d, SEF), "SaldoDisponible").subList(0, 3));
        String ins = d.getAsJsonArray("sentencias").get(6).getAsJsonObject().get("sql").getAsString();
        assertTrue(ins.contains("ConsultarSaldoCuentaInversionFechaEfectivaAnalitica"));
        assertTrue(ins.contains("PLACEHOLDER.\"$$p_idempresa$$\" => 1, PLACEHOLDER.\"$$p_fechaconsulta$$\" => '0001-01-01'"), ins);
    }

    @Test
    @DisplayName("D-REP-06: si falla el primer DROP TRIGGER se traga el error y no se intenta el segundo; INICIO_PAUTA sigue")
    void D_REP_06_drop_trigger() throws Exception {
        JsonObject d = run("REP-ENV-", "inicio-pauta-primer-drop-falla");
        assertNull(excepcion(d));
        assertEquals(1, contar(d, "DROP TRIGGER"));
        assertEquals("FALLO_INYECTADO", d.getAsJsonArray("sentencias").get(0).getAsJsonObject().get("ejecucion").getAsString());
        assertEquals(1, filas(d, WS).size());
        JsonObject segundo = run("REP-ENV-", "inicio-pauta-segundo-drop-falla");
        assertEquals(2, contar(segundo, "DROP TRIGGER"));
        assertNull(excepcion(segundo));
    }

    @Test
    @DisplayName("D-REP-07: en FIN_PAUTA un error al crear los triggers o al registrar el resumen se traga y omite el resto de ese bloque; el historico y el Workstack siguen")
    void D_REP_07_create_trigger() throws Exception {
        JsonObject d = run("REP-ENV-", "fin-pauta-create-trigger-custodia-falla");
        assertNull(excepcion(d));
        assertEquals(1, contar(d, "CREATE TRIGGER"));
        assertEquals(0, contar(d, "UPSERT"));
        assertEquals(0, filas(d, RESUMEN).size());
        assertEquals(1, filas(d, WS).size());
        assertTrue(contar(d, "DELETE") >= 8);
        JsonObject segundo = run("REP-ENV-", "fin-pauta-create-trigger-cuenta-inversion-falla");
        assertEquals(2, contar(segundo, "CREATE TRIGGER"));
        assertEquals(0, filas(segundo, RESUMEN).size());
        JsonObject upsert = run("REP-ENV-", "fin-pauta-upsert-resumen-falla");
        assertNull(excepcion(upsert));
        assertEquals(0, filas(upsert, RESUMEN).size());
        assertEquals(1, filas(upsert, WS).size());
    }

    @Test
    @DisplayName("D-REP-11: la fecha de sistema no se valida: nula, sin clave o sin parametros falla; no texto falla; con hora o basura pasa a los SQL")
    void D_REP_11_fecha_sin_validar() throws Exception {
        JsonObject nula = run("REP-ENV-", "inicio-pauta-fecha-nula-falla-despues-de-apagar-triggers");
        assertEquals("NullPointerException", excepcion(nula));
        assertEquals(2, contar(nula, "DROP TRIGGER"));
        assertEquals(2, sentencias(nula));
        assertEquals("NullPointerException", excepcion(run("REP-ENV-", "inicio-pauta-sin-clave-de-fecha")));
        assertEquals("NullPointerException", excepcion(run("REP-ENV-", "inicio-pauta-parametros-nulos")));
        assertEquals("ClassCastException", excepcion(run("REP-ENV-", "inicio-pauta-fecha-numerica")));
        assertEquals(List.of("tables:Custodia_CarteraRV;BCS_query:QueryReplica;Fecha:null ;serialDelete:false;truncarDestino:true;serialLaunch:false"),
                parametrosWs(run("REP-ENV-", "valorizar-cartera-rv-precio-mercado-fecha-nula")));
        assertNull(excepcion(run("REP-ENV-", "inicio-pauta-fecha-con-hora-pasa-tal-cual")));
        assertEquals(List.of("tables:CuentaInversion_SaldoFechaEfectivaHist,CuentaInversion_SaldoFechaMovimientoHist;BCS_query:QueryReplicaDelta;Fecha:2026-03-04 10:00:00" + CIERRE),
                parametrosWs(run("REP-ENV-", "inicio-pauta-fecha-con-hora-pasa-tal-cual")));
    }

    @Test
    @DisplayName("D-REP-15: Parameters es VARCHAR(500): una fecha muy larga hace fallar la insercion final")
    void D_REP_15_parameters_largo() throws Exception {
        JsonObject d = run("REP-ENV-", "pauta-patrimonio-fecha-larga-excede-500");
        assertEquals("SQLException", excepcion(d));
        assertEquals(0, filas(d, WS).size());
    }

    @Test
    @DisplayName("D-REP-13: ningun proceso es atomico: ante un error quedan en la sesion los borrados y copias ya hechos")
    void D_REP_13_no_atomico() throws Exception {
        JsonObject d = run("REP-ENV-", "inicio-pauta-falla-insercion-historica-propaga");
        assertEquals("SQLException", excepcion(d));
        assertEquals(0, filas(d, WS).size());
        assertEquals(3, filas(d, SEF).size());
        assertEquals(8, sentencias(d));
        JsonObject s = run("REP-SEB-", "falla-insercion-de-tbctcirv");
        assertEquals("SQLException", excepcion(s));
        assertEquals(List.of("3"), columna(filas(s, "BSCL.Replica.BD::TBCTCIRV"), "SEC_CAR_INVER"));
        assertEquals(List.of("3", "11", "12"), columna(filas(s, "BSCL.Replica.BD::TBCPDTPC"), "SEC_COMPROM"));
        assertEquals(List.of("2026-02", "2026-03", "2026-03"), columna(filas(s, "BSCL.Replica.BD::TBCTCURV"), "COD_PER"));
    }

    @Test
    @DisplayName("D-REP-01: call de EnviarTablaReplica y de ReplicaUtils devuelve null y no hace nada")
    void D_REP_01_call_devuelve_null() throws Exception {
        for (String f : new String[] { "enviar-tabla-replica-call-devuelve-null", "enviar-tabla-replica-call-con-entrada-vacia", "replica-utils-call-devuelve-null" }) {
            JsonObject d = run("REP-CAL-", f);
            assertTrue(d.get("salidaNula").getAsBoolean(), f);
            assertEquals(0, sentencias(d), f);
            assertEquals(0, d.getAsJsonArray("colaboradores").size(), f);
        }
    }

    @Test
    @DisplayName("B-REP-15: los Workstack se insertan en un solo lote con CURRENT_TIMESTAMP de la base; las columnas son Key, Table, Action, Status, Stamp, Parameters")
    void B_REP_15_lote() throws Exception {
        JsonObject d = run("REP-ENV-", "inicio-pauta-fin-de-semana-copia-dos-dias");
        JsonObject ultimo = d.getAsJsonArray("sentencias").get(d.getAsJsonArray("sentencias").size() - 1).getAsJsonObject();
        assertEquals("batch", ultimo.get("tipo").getAsString());
        assertEquals("INSERT INTO \"BSCL.Plataforma.BD::PLA.Replica.WorkStack\" (\"Key\",\"Table\",\"Action\",\"Status\",\"Stamp\",\"Parameters\") VALUES(?,?,?,?,CURRENT_TIMESTAMP,?)", ultimo.get("sql").getAsString());
        assertEquals(3, ultimo.getAsJsonArray("filas").size());
        assertEquals("[\"\",\"CUENTA INVERSION DIA ACTUAL\",\"M\",0,\"" + HIST_CI + "2026-03-06" + CIERRE + "\"]", ultimo.getAsJsonArray("filas").get(0).toString());
        assertEquals("2026-03-02 14:30:00", columna(filas(d, WS), "Stamp").get(0));
    }

    // ---------------------------------------------------------------------------------------------------------------------------------- ReplicaUtils

    @Test
    @DisplayName("B-REP-03: solo \"S\" o \"s\" activa el respaldo Sebra; N, vacio, X, SI, \" S\" o \"1\" no hacen nada")
    void B_REP_03_compuerta_del_parametro() throws Exception {
        for (String f : new String[] { "param-s-reemplaza-el-periodo-en-las-13", "param-s-minuscula-tambien-activa" }) {
            JsonObject d = run("REP-SEB-", f);
            assertEquals(26, sentencias(d), f);
            assertEquals(13, contar(d, "delete from"), f);
            assertEquals(13, contar(d, "INSERT INTO"), f);
        }
        for (String f : new String[] { "param-n-no-hace-nada", "param-n-minuscula-no-hace-nada", "param-vacio-no-hace-nada", "param-x-no-hace-nada", "param-si-no-hace-nada", "param-s-con-espacio-no-hace-nada",
                "param-s-con-espacio-final-no-hace-nada", "param-true-no-hace-nada", "param-uno-no-hace-nada" }) {
            JsonObject d = run("REP-SEB-", f);
            assertEquals(0, sentencias(d), f);
            assertNull(excepcion(d), f);
            assertEquals(1, d.getAsJsonArray("colaboradores").size(), f);
        }
        assertEquals("[1,\"PLATAFORMA\",\"PLA.RespaldoReplicaTablasSebra\"]", run("REP-SEB-", "param-s-reemplaza-el-periodo-en-las-13").getAsJsonArray("colaboradores").get(0).getAsJsonObject().get("argumentos").toString());
        assertEquals("NullPointerException", excepcion(run("REP-SEB-", "param-nulo-falla-al-comparar")));
    }

    @Test
    @DisplayName("B-REP-04 / B-REP-17: el respaldo Sebra reemplaza el periodo aaaa-mm en las 13 tablas, en un orden fijo (delete e insert por tabla)")
    void B_REP_17_orden_y_periodo() throws Exception {
        JsonObject d = run("REP-SEB-", "param-s-reemplaza-el-periodo-en-las-13");
        List<String> s = sql(d);
        for (int i = 0; i < 13; i++) {
            assertEquals("delete from \"BSCL.Replica.BD::" + SEBRA.get(i) + "\" where COD_PER = '2026-03';", s.get(2 * i), "delete " + i);
            assertTrue(s.get(2 * i + 1).startsWith("INSERT INTO \"BSCL.Replica.BD::" + SEBRA.get(i) + "\""), "insert " + i);
            assertTrue(s.get(2 * i + 1).endsWith("WHERE COD_PER = '2026-03';"), "insert " + i);
        }
        assertEquals(List.of("2026-02", "2026-03", "2026-03"), columna(filas(d, "BSCL.Replica.BD::TBCPDTPC"), "COD_PER"));
        assertEquals(List.of("3", "11", "12"), columna(filas(d, "BSCL.Replica.BD::TBCPDTPC"), "SEC_COMPROM"));
        assertEquals(List.of("3", "11", "12"), columna(filas(d, "BSCL.Replica.BD::TBCTTRPT"), "SEC_MOVTO"));
        assertTrue(sql(run("REP-SEB-", "fecha-con-hora-usa-los-primeros-siete")).get(0).endsWith("'2026-03';"));
        assertTrue(sql(run("REP-SEB-", "otro-periodo-no-toca-marzo")).get(0).endsWith("'2026-02';"));
        assertEquals("StringIndexOutOfBoundsException", excepcion(run("REP-SEB-", "fecha-corta-falla-al-recortar")));
        assertEquals("NullPointerException", excepcion(run("REP-SEB-", "fecha-nula-con-s-falla")));
        assertNull(excepcion(run("REP-SEB-", "fecha-nula-con-n-no-falla")));
        assertFalse(columna(filas(run("REP-SEB-", "periodo-sin-filas-en-origen-vacia-el-destino"), "BSCL.Replica.BD::TBCTCIRV"), "COD_PER").contains("2026-05"));
    }

    @Test
    @DisplayName("B-REP-23 / D-REP-12: el parametro Sebra ausente se propaga (la excepcion del colaborador, sin escribir); en el respaldo diario se traga y se asume N")
    void B_REP_23_D_REP_12_parametro_ausente() throws Exception {
        JsonObject s = run("REP-SEB-", "param-ausente-se-propaga");
        assertEquals("Exception", excepcion(s));
        assertEquals("Exception: Parametro PLA.RespaldoReplicaTablasSebra en modulo PLATAFORMA no encontrado", s.get("excepcion").getAsString());
        assertEquals("Exception", excepcion(run("REP-SEB-", "param-ausente-con-fecha-nula")));
        JsonObject d = run("REP-DIA-", "param-ausente-se-traga");
        assertNull(excepcion(d));
        assertEquals(0, sentencias(d));
    }

    @Test
    @DisplayName("B-REP-18 / D-REP-14: el respaldo diario reemplaza 9<dd>-01 en las 4 carteras con el periodo mensual; el mismo dia de otro mes pisa el respaldo anterior")
    void B_REP_18_D_REP_14_respaldo_diario() throws Exception {
        JsonObject d = run("REP-DIA-", "param-s-copia-el-periodo-al-dia");
        List<String> s = sql(d);
        assertEquals(8, s.size());
        for (int i = 0; i < 4; i++) {
            String t = List.of("TBCTCIFM", "TBCTCIRV", "TBCTCIIF", "TBCTCIRF").get(i);
            assertEquals("delete from \"BSCL.Replica.BD::" + t + "\" where COD_PER = '9006-01';", s.get(2 * i));
            assertTrue(s.get(2 * i + 1).startsWith("INSERT INTO \"BSCL.Replica.BD::" + t + "\""));
            assertTrue(s.get(2 * i + 1).endsWith("WHERE COD_PER = '2026-03';"));
        }
        assertEquals(List.of("2026-02", "2026-03", "2026-03", "9006-01", "9006-01"), columna(filas(d, "BSCL.Replica.BD::TBCTCIFM"), "COD_PER"));
        assertEquals(List.of("3", "1", "2", "1", "2"), columna(filas(d, "BSCL.Replica.BD::TBCTCIFM"), "SEC_CAR_INVER"));
        assertTrue(sql(run("REP-DIA-", "dia-uno-genera-9001-01")).get(0).contains("'9001-01'"));
        assertTrue(sql(run("REP-DIA-", "dia-treinta-y-uno-genera-9031-01")).get(0).contains("'9031-01'"));
        JsonObject pisa = run("REP-DIA-", "mismo-dia-de-otro-mes-pisa-el-respaldo-anterior");
        assertEquals(List.of("2026-02", "2026-03", "2026-03", "2026-04", "9006-01"), columna(filas(pisa, "BSCL.Replica.BD::TBCTCIFM"), "COD_PER"));
        assertEquals(List.of("3", "1", "2", "7", "7"), columna(filas(pisa, "BSCL.Replica.BD::TBCTCIFM"), "SEC_CAR_INVER"));
        JsonObject vacio = run("REP-DIA-", "periodo-sin-filas-borra-el-respaldo-del-dia");
        assertEquals(List.of("2026-02", "2026-03", "2026-03"), columna(filas(vacio, "BSCL.Replica.BD::TBCTCIFM"), "COD_PER"));
    }

    @Test
    @DisplayName("B-REP-18: el respaldo diario compara el parametro como Sebra (S o s); y falla con fecha corta o nula solo si esta activo")
    void B_REP_18_parametro_y_fechas() throws Exception {
        assertEquals(8, sentencias(run("REP-DIA-", "param-s-minuscula-tambien-activa")));
        for (String f : new String[] { "param-n-no-hace-nada", "param-vacio-no-hace-nada", "param-x-no-hace-nada", "param-si-no-hace-nada" }) assertEquals(0, sentencias(run("REP-DIA-", f)), f);
        assertEquals("StringIndexOutOfBoundsException", excepcion(run("REP-DIA-", "fecha-corta-falla-al-recortar-el-dia")));
        assertEquals("NullPointerException", excepcion(run("REP-DIA-", "fecha-nula-con-s-falla")));
        assertNull(excepcion(run("REP-DIA-", "fecha-nula-con-n-no-falla")));
        assertEquals("NullPointerException", excepcion(run("REP-DIA-", "param-nulo-falla")));
        assertTrue(sql(run("REP-DIA-", "fecha-con-hora-usa-el-dia")).get(0).contains("'9006-01'"));
    }

    @Test
    @DisplayName("B-REP-19 / B-REP-20: el respaldo Optimus guarda 4 envios por fecha (la de sistema y cada dia inhabil) y copia los 4 conjuntos a cada dia inhabil")
    void B_REP_19_20_optimus() throws Exception {
        JsonObject d = run("REP-OPT-", "fin-de-semana-copia-dos-dias-y-guarda-doce-envios");
        assertEquals(12, filas(d, POST).size());
        assertEquals(List.of("2026-03-06", "2026-03-07", "2026-03-08"), columna(filas(d, POST), "Fecha").subList(0, 3));
        assertEquals(List.of("2026-03-06"), List.copyOf(new java.util.TreeSet<>(columna(filas(d, POST), "FechaSistema"))));
        List<String> t = tipos(d);
        assertEquals(List.of("DELETE", "INSERT", "DELETE", "INSERT", "DELETE", "INSERT", "DELETE", "INSERT"), t.subList(0, 8));
        assertEquals(List.of("batch:UPSERT", "batch:UPSERT", "batch:UPSERT", "batch:UPSERT"), t.subList(8, 12));
        assertEquals(List.of("2026-03-06", "2026-03-06", "2026-03-06", "2026-03-07", "2026-03-07", "2026-03-07", "2026-03-08", "2026-03-08", "2026-03-08"), columna(filas(d, SEF), "FechaSaldo"));
        JsonObject sin = run("REP-OPT-", "sin-dias-inhabiles-guarda-cuatro-envios");
        assertEquals(4, filas(sin, POST).size());
        assertEquals(1, colaboradores(sin, "Feriados", "esDiaHabil").size());
        JsonObject repetido = run("REP-OPT-", "envio-ya-registrado-no-se-duplica");
        assertEquals(8, filas(repetido, POST).size());
    }

    @Test
    @DisplayName("D-REP-02: el respaldo Optimus pasa el id de empresa como pais de cada fecha y el pais de la empresa como p_idPais")
    void D_REP_02_pais_es_el_id_de_empresa() throws Exception {
        JsonObject d = run("REP-OPT-", "el-pais-que-recibe-el-colaborador-es-el-id-de-empresa");
        JsonObject in = colaboradores(d, "ObtenerDiaSiguiente", "logica").get(0).getAsJsonObject().getAsJsonArray("argumentos").get(0).getAsJsonObject();
        assertEquals(152, in.get("p_idPais").getAsLong());
        assertEquals(2, in.getAsJsonArray("p_fechas").get(0).getAsJsonObject().get("pais").getAsLong());
        assertEquals("[\"2026-03-07\",152]", colaboradores(d, "Feriados", "esDiaHabil").get(0).getAsJsonObject().get("argumentos").toString());
        JsonObject enviar = run("REP-ENV-", "inicio-pauta-otra-empresa-otro-pais");
        assertEquals(152, colaboradores(enviar, "ObtenerDiaSiguiente", "logica").get(0).getAsJsonObject().getAsJsonArray("argumentos").get(0).getAsJsonObject().getAsJsonArray("p_fechas").get(0).getAsJsonObject().get("pais").getAsLong());
    }

    @Test
    @DisplayName("B-REP-20: guardarEnvioPostPauta hace UPSERT de (FechaSistema, Tabla, Fecha): la misma terna no duplica, otra fecha de envio agrega (D-REP-10), una clave nula o larga falla")
    void B_REP_20_D_REP_10_envio_post_pauta() throws Exception {
        assertEquals(List.of("Custodia_CarteraFM"), columna(filas(run("REP-POS-", "guarda-una-terna"), POST), "Tabla"));
        assertEquals(1, filas(run("REP-POS-", "misma-terna-no-duplica"), POST).size());
        assertEquals(List.of("2026-03-07", "2026-03-09"), columna(filas(run("REP-POS-", "otra-fecha-de-envio-agrega-fila"), POST), "Fecha"));
        assertEquals(List.of("Otra", "Custodia_CarteraFM", "Otra"), columna(filas(run("REP-POS-", "conserva-las-otras-filas"), POST), "Tabla"));
        for (String f : new String[] { "tabla-nula-viola-la-clave", "fecha-sistema-nula-viola-la-clave", "fecha-envio-nula-viola-la-clave", "tabla-de-101-caracteres-no-cabe", "fecha-de-11-caracteres-no-cabe",
                "fecha-con-hora-no-cabe", "falla-el-upsert-propaga" }) {
            JsonObject d = run("REP-POS-", f);
            assertEquals("SQLException", excepcion(d), f);
            assertTrue(columna(filas(d, POST), "Tabla").stream().noneMatch(t -> t.length() > 100), f);
        }
        assertNull(excepcion(run("REP-POS-", "tabla-de-100-caracteres-cabe")));
        assertEquals("UPSERT \"BSCL.Replica.BD::EnvioPostPauta\" (\"FechaSistema\", \"Tabla\", \"Fecha\") VALUES(?,?,?) WITH PRIMARY KEY;", sql(run("REP-POS-", "guarda-una-terna")).get(0));
        assertEquals("[[\"2026-03-06\",\"Custodia_CarteraFM\",\"2026-03-09\"]]", run("REP-POS-", "guarda-una-terna").getAsJsonArray("sentencias").get(0).getAsJsonObject().getAsJsonArray("filas").toString().replace("\"fechaHora\"", "x"));
        assertFalse(sql(run("REP-POS-", "comillas-en-la-tabla-van-como-parametro")).get(0).contains("a'b"));
    }
}
