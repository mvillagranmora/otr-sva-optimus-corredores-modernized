package cl.bcs.replica.envio;

import java.sql.Connection;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonObject;

import cl.bcs.replica.domain.Workstack;
import cl.bcs.replica.kit.Compuertas;
import cl.bcs.replica.kit.EntradaDiaSiguiente;
import cl.bcs.replica.kit.Sql;
import cl.bcs.replica.port.out.ConfiguracionSistemaPort;
import cl.bcs.replica.port.out.ConsultarEmpresaPort;
import cl.bcs.replica.port.out.FeriadosPort;
import cl.bcs.replica.port.out.ObtenerDiaSiguientePort;
import cl.bcs.replica.port.out.TriggersReplicaPort;

public class EnviarTablaReplicaServicio {
    private static final String CIERRE = ";serialDelete:false;truncarDestino:false;serialLaunch:false";
    private static final String HIST_CI = "tables:CuentaInversion_SaldoFechaEfectivaHist,CuentaInversion_SaldoFechaMovimientoHist;BCS_query:QueryReplicaDelta;Fecha:";
    private static final String HIST_CUS = "tables:Custodia_CustodiaFechaOperacionHist,Custodia_CustodiaFechaLiquidacionHist;BCS_query:QueryReplicaDelta;Fecha:";
    private static final String PATRIMONIO = "tables:CuentaInversion_PatrimonioCajaFechaLiquidacion,CuentaInversion_PatrimonioCajaFechaOperacion,Custodia_PatrimonioConsolidadoFechaLiquidacion,"
            + "Custodia_PatrimonioConsolidadoFechaOperacion,Custodia_PatrimonioCustodiaFechaLiquidacion,Custodia_PatrimonioCustodiaFechaOperacion;BCS_query:QueryReplicaDelta;Fecha:";

    private final Compuertas compuertas;
    private final ConsultarEmpresaPort empresas;
    private final ObtenerDiaSiguientePort diasSiguientes;
    private final FeriadosPort feriados;
    private final ConfiguracionSistemaPort configuracion;
    private final TriggersReplicaPort triggers;
    private final Clock reloj;

    public EnviarTablaReplicaServicio(Compuertas compuertas, ConsultarEmpresaPort empresas, ObtenerDiaSiguientePort diasSiguientes, FeriadosPort feriados, ConfiguracionSistemaPort configuracion,
            TriggersReplicaPort triggers, Clock reloj) {
        this.compuertas = compuertas;
        this.empresas = empresas;
        this.diasSiguientes = diasSiguientes;
        this.feriados = feriados;
        this.configuracion = configuracion;
        this.triggers = triggers;
        this.reloj = reloj;
    }

    public JsonObject call(Connection conn, JsonObject in) throws Exception {
        return null;
    }

    public void logica(Connection conn, Long idEmpresa, String proceso, Map<String, Object> parametros) throws Exception {
        String fecha = (String) parametros.get("FechaSistemaEjecucion");
        List<Workstack> lista = new ArrayList<>();
        List<Map<String, Object>> resumen = new ArrayList<>();

        if (proceso.equalsIgnoreCase("INICIO_PAUTA")) {
            compuertas.exigirTriggers();
            try {
                triggers.desactivar(conn);
            } catch (Exception e) {
                // legacy: the error is only logged
            }
            lista.add(Workstack.nuevo("CUENTA INVERSION DIA ACTUAL", HIST_CI + fecha + CIERRE));
            limpiarEInsertarHistorico(conn, fecha);
            lista.addAll(respaldarDataDiaInhabil(conn, idEmpresa, fecha, "CUENTA INVERSION DIA INHABIL", HIST_CI));
        } else if (proceso.equalsIgnoreCase("VALORIZAR_FFMM")) {
            lista.add(Workstack.nuevo("CUSTODIA ACTUAL", "tables:Custodia_CustodiaFechaLiquidacionHist,Custodia_CustodiaFechaOperacionHist;BCS_query:QueryReplicaDelta;Fecha:" + fecha + CIERRE));
            lista.add(Workstack.nuevo("CARTERAFM", "tables:Custodia_CarteraFM;BCS_query:QueryReplica;Fecha:" + fecha + ";serialDelete:false;truncarDestino:true;serialLaunch:false"));
            lista.addAll(respaldarDataDiaInhabil(conn, idEmpresa, fecha, "CUSTODIA ACTUAL DIA INHABIL", HIST_CUS));
        } else if (proceso.equalsIgnoreCase("PAUTA_PATRIMONIO")) {
            lista.add(Workstack.nuevo("PATRIMONIO DIA ACTUAL", PATRIMONIO + fecha + CIERRE));
        } else if (proceso.equalsIgnoreCase("COPIA_PATRIMONIO_PARA_INHABIL")) {
            Long idPais = empresas.idPais(conn, idEmpresa);
            String destino = fecha;
            while (true) {
                JsonObject salida = diasSiguientes.logica(conn, EntradaDiaSiguiente.unDia(idPais, destino, idPais));
                if (salida.get("P_OUT_ERRORESCONTADOR").getAsLong() != 0L) return;
                destino = salida.getAsJsonArray("p_out_fechas").get(0).getAsJsonObject().get("fechaCalculada").getAsString();
                if (feriados.esDiaHabil(conn, destino, idPais)) break;
                lista.add(Workstack.nuevo("PATRIMONIO DIA " + destino, PATRIMONIO + destino + CIERRE));
            }
        } else if (proceso.equalsIgnoreCase("PAUTA_RESPALDAR_CUSTODIA")) {
            lista.add(Workstack.nuevo("CARTERAFMHIS ACTUAL", "tables:Custodia_CarteraFMHis;BCS_query:QueryReplicaDelta;Fecha:" + fecha + CIERRE));
            lista.add(Workstack.nuevo("CARTERARVHIS ACTUAL", "tables:Custodia_CarteraRVHis;BCS_query:QueryReplicaDelta;Fecha:" + fecha + CIERRE));
            lista.add(Workstack.nuevo("GARANTIAHIS ACTUAL", "tables:Custodia_GarantiaHis;BCS_query:QueryReplicaDelta;Fecha:" + fecha + CIERRE));
        } else if (proceso.equalsIgnoreCase("CAMBIO_FECHA_SISTEMA")) {
            lista.add(Workstack.nuevo("CUENTA INVERSION DIA SIGUIENTE", "tables:CuentaInversion_SaldoFechaEfectiva,CuentaInversion_SaldoFechaMovimiento;BCS_query:QueryReplicaDelta;Fecha:" + fecha + CIERRE));
        } else if (proceso.equalsIgnoreCase("VALORIZAR_CARTERA_RV_PRECIO_MERCADO")) {
            lista.add(Workstack.nuevo("CARTERARV", "tables:Custodia_CarteraRV;BCS_query:QueryReplica;Fecha:" + fecha + " ;serialDelete:false;truncarDestino:true;serialLaunch:false"));
        } else if (proceso.equalsIgnoreCase("FIN_PAUTA")) {
            compuertas.exigirTriggers();
            lista.add(Workstack.nuevo("CUSTODIA DIA SIGUIENTE", "tables:Custodia_CustodiaFechaLiquidacion,Custodia_CustodiaFechaOperacion;BCS_query:QueryReplicaDelta;Fecha:" + fecha + CIERRE));
            Long desfase = configuracion.parametroLong(conn, idEmpresa, "PLATAFORMA", "PLA.SegundosZonaHoraria");
            Map<String, Object> r = new HashMap<>();
            r.put("Tipo", "PAUTA");
            r.put("Estado", "OK");
            r.put("FechaSistema", fechaSql(fecha));
            r.put("Nombre", "FIN_PAUTA");
            r.put("Detalle", "FIN_PAUTA");
            r.put("UltimaActualizacion", fechaHoraSql(LocalDateTime.now(reloj).plusSeconds(desfase).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
            resumen.add(r);
            try {
                triggers.reactivar(conn);
                Sql.batch(conn, Sql.recurso("EnviarTablaReplica_UpsertResumen.sql"), resumen);
            } catch (Exception e) {
                // legacy: the error is only logged
            }
            limpiarEInsertarHistorico(conn, fecha);
        }

        if (lista.size() > 0) {
            List<Map<String, Object>> filas = new ArrayList<>();
            for (Workstack w : lista) filas.add(Sql.fila("Key", w.Key, "Table", w.Table, "Action", w.Action, "Status", w.Status, "Parameters", w.Parameters));
            Sql.batch(conn, Sql.recurso("EnviarTablaReplica_InsertWorkstack.sql"), filas);
        }
    }

    private static void limpiarEInsertarHistorico(Connection conn, String fecha) throws Exception {
        Sql.exec(conn, Sql.recurso("EnviarTablaReplica_DeleteCuentaInversionFechaEfectivaHistorica.sql").replace(":FechaEjecucion", fecha));
        Sql.exec(conn, Sql.recurso("EnviarTablaReplica_DeleteCuentaInversionFechaMovimientoHistorica.sql").replace(":FechaEjecucion", fecha));
        Sql.exec(conn, Sql.recurso("EnviarTablaReplica_DeleteCuentaInversionFechaEfectivaCurrent.sql").replace(":FechaSaldo", fecha));
        Sql.exec(conn, Sql.recurso("EnviarTablaReplica_DeleteCuentaInversionFechaMovimientoCurrent.sql").replace(":FechaSaldo", fecha));
        Sql.exec(conn, Sql.recurso("EnviarTablaReplica_InsertCuentaInversionFechaEfectivaHistorica.sql"));
        Sql.exec(conn, Sql.recurso("EnviarTablaReplica_InsertCuentaInversionFechaMovimientoHistorica.sql"));
        Sql.exec(conn, Sql.recurso("EnviarTablaReplica_DeleteCustodiaFechaLiquidacionHistorica.sql").replace(":FechaEjecucion", fecha));
        Sql.exec(conn, Sql.recurso("EnviarTablaReplica_DeleteCustodiaFechaOperacionHistorica.sql").replace(":FechaEjecucion", fecha));
        Sql.exec(conn, Sql.recurso("EnviarTablaReplica_DeleteCustodiaFechaLiquidacionCurrent.sql").replace(":FechaCustodia", fecha));
        Sql.exec(conn, Sql.recurso("EnviarTablaReplica_DeleteCustodiaFechaOperacionCurrent.sql").replace(":FechaCustodia", fecha));
        Sql.exec(conn, Sql.recurso("EnviarTablaReplica_InsertCustodiaFechaLiquidacionHistorica.sql"));
        Sql.exec(conn, Sql.recurso("EnviarTablaReplica_InsertCustodiaFechaOperacionHistorica.sql"));
    }

    static Date fechaSql(String s) {
        return Date.valueOf(LocalDate.parse(s, DateTimeFormatter.ofPattern(s.length() == 19 ? "yyyy-MM-dd HH:mm:ss" : "yyyy-MM-dd")));
    }

    static Timestamp fechaHoraSql(String s) {
        return Timestamp.valueOf(LocalDateTime.parse(s.length() == 19 ? s : s + " 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }

    public List<Workstack> respaldarDataDiaInhabil(Connection con, Long idEmpresa, String fechaOrigen, String tabla, String parametros) throws Exception {
        List<Workstack> lista = new ArrayList<>();
        Long idPais = empresas.idPais(con, idEmpresa);
        String destino = fechaOrigen;
        while (true) {
            JsonObject salida = diasSiguientes.logica(con, EntradaDiaSiguiente.unDia(idPais, destino, idPais));
            if (salida.get("P_OUT_ERRORESCONTADOR").getAsLong() != 0L) break;
            destino = salida.getAsJsonArray("p_out_fechas").get(0).getAsJsonObject().get("fechaCalculada").getAsString();
            if (feriados.esDiaHabil(con, destino, idPais)) break;
            lista.add(Workstack.nuevo(tabla, parametros + destino + CIERRE));
            List<Map<String, Object>> fechas = new ArrayList<>();
            fechas.add(Sql.fila("FechaOrigen", fechaOrigen, "FechaDestino", destino));
            if ("CUSTODIA ACTUAL DIA INHABIL".equals(tabla)) {
                Sql.exec(con, Sql.recurso("EnviarTablaReplica_DeleteCustodiaFechaLiquidacionCurrent.sql").replace(":FechaCustodia", destino));
                Sql.exec(con, Sql.recurso("EnviarTablaReplica_DeleteCustodiaFechaOperacionCurrent.sql").replace(":FechaCustodia", destino));
                Sql.batch(con, Sql.recurso("EnviarTablaReplica_InsertCustodiaFechaLiquidacionDiaInhabil.sql"), fechas);
                Sql.batch(con, Sql.recurso("EnviarTablaReplica_InsertCustodiaFechaOperacionDiaInhabil.sql"), fechas);
            } else if ("CUENTA INVERSION DIA INHABIL".equals(tabla)) {
                Sql.exec(con, Sql.recurso("EnviarTablaReplica_DeleteCuentaInversionFechaEfectivaCurrent.sql").replace(":FechaSaldo", destino));
                Sql.exec(con, Sql.recurso("EnviarTablaReplica_DeleteCuentaInversionFechaMovimientoCurrent.sql").replace(":FechaSaldo", destino));
                Sql.batch(con, Sql.recurso("EnviarTablaReplica_InsertCuentaInversionFechaEfectivaDiaInhabil.sql"), fechas);
                Sql.batch(con, Sql.recurso("EnviarTablaReplica_InsertCuentaInversionFechaMovimientoDiaInhabil.sql"), fechas);
            }
        }
        return lista;
    }
}
