package cl.bcs.replica.respaldo;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonObject;

import cl.bcs.replica.kit.Compuertas;
import cl.bcs.replica.kit.EntradaDiaSiguiente;
import cl.bcs.replica.kit.ServicioNoServidoException;
import cl.bcs.replica.kit.Sql;
import cl.bcs.replica.port.out.ConfiguracionSistemaPort;
import cl.bcs.replica.port.out.ConsultarEmpresaPort;
import cl.bcs.replica.port.out.FeriadosPort;
import cl.bcs.replica.port.out.ObtenerDiaSiguientePort;
import cl.bcs.replica.port.out.RespaldoOptimusPort;
import cl.bcs.replica.port.out.RespaldoSebraPort;

public class ReplicaUtilsServicio {
    private final Compuertas compuertas;
    private final ConfiguracionSistemaPort configuracion;
    private final ConsultarEmpresaPort empresas;
    private final ObtenerDiaSiguientePort diasSiguientes;
    private final FeriadosPort feriados;
    private final RespaldoSebraPort sebra;
    private final RespaldoOptimusPort optimus;

    public ReplicaUtilsServicio(Compuertas compuertas, ConfiguracionSistemaPort configuracion, ConsultarEmpresaPort empresas, ObtenerDiaSiguientePort diasSiguientes, FeriadosPort feriados,
            RespaldoSebraPort sebra, RespaldoOptimusPort optimus) {
        this.compuertas = compuertas;
        this.configuracion = configuracion;
        this.empresas = empresas;
        this.diasSiguientes = diasSiguientes;
        this.feriados = feriados;
        this.sebra = sebra;
        this.optimus = optimus;
    }

    public JsonObject call(Connection conn, JsonObject in) throws Exception {
        return null;
    }

    public void procesarRespaldoSebra(Connection conn, String fechaSistemaEjecucion, Long idEmpresa) throws Exception {
        String respaldar = configuracion.parametroString(conn, idEmpresa, "PLATAFORMA", "PLA.RespaldoReplicaTablasSebra");
        if (respaldar.equalsIgnoreCase("S")) {
            compuertas.exigirRespaldoSebra();
            sebra.respaldarPeriodo(conn, fechaSistemaEjecucion.substring(0, 7));
        }
    }

    public void procesarRespaldoDiarioCarterasSebra(Connection conn, String fechaSistemaEjecucion, Long idEmpresa) throws Exception {
        String respaldar = "N";
        try {
            respaldar = configuracion.parametroString(conn, idEmpresa, "PLATAFORMA", "PLA.RespaldoDiarioReplicaCarteraSebra");
        } catch (ServicioNoServidoException e) {
            throw e;
        } catch (Exception e) {
            // legacy: the error is only logged and the backup is not made
        }
        if (respaldar.equalsIgnoreCase("S")) {
            compuertas.exigirRespaldoSebra();
            String codPer = fechaSistemaEjecucion.substring(0, 7);
            String codDia = "90" + fechaSistemaEjecucion.substring(8, 10) + "-01";
            sebra.respaldarDiario(conn, codPer, codDia);
        }
    }

    public void guardarEnvioPostPauta(Connection conn, String tabla, String fechaSistema, String fechaEnvio) throws Exception {
        List<Map<String, Object>> datos = new ArrayList<>();
        datos.add(Sql.fila("Fecha", fechaEnvio, "FechaSistema", fechaSistema, "Tabla", tabla));
        Sql.batch(conn, Sql.recurso("EnviarTablaReplica_InsertEnvioPostPauta.sql"), datos);
    }

    private void guardarCuatro(Connection conn, String fechaSistema, String fecha) throws Exception {
        guardarEnvioPostPauta(conn, "Custodia_CustodiaFechaOperacionHis", fechaSistema, fecha);
        guardarEnvioPostPauta(conn, "Custodia_CustodiaFechaLiquidacionHis", fechaSistema, fecha);
        guardarEnvioPostPauta(conn, "CuentaInversion_SaldoFechaEfectivaHis", fechaSistema, fecha);
        guardarEnvioPostPauta(conn, "CuentaInversion_SaldoFechaMovimientoHis", fechaSistema, fecha);
    }

    public void procesarRespaldoOptimus(Connection conn, String fechaSistemaEjecucion, Long idEmpresa) throws Exception {
        compuertas.exigirRespaldoOptimus();
        optimus.respaldarDiaActual(conn, fechaSistemaEjecucion);
        guardarCuatro(conn, fechaSistemaEjecucion, fechaSistemaEjecucion);
        Long idPais = empresas.idPais(conn, idEmpresa);
        String destino = fechaSistemaEjecucion;
        while (true) {
            // D-REP-02: el legacy pasa el id de empresa como pais de la fecha
            JsonObject salida = diasSiguientes.logica(conn, EntradaDiaSiguiente.unDia(idPais, destino, idEmpresa));
            if (salida.get("P_OUT_ERRORESCONTADOR").getAsLong() != 0L) break;
            destino = salida.getAsJsonArray("p_out_fechas").get(0).getAsJsonObject().get("fechaCalculada").getAsString();
            if (feriados.esDiaHabil(conn, destino, idPais)) break;
            optimus.copiarDiaInhabil(conn, fechaSistemaEjecucion, destino);
            guardarCuatro(conn, fechaSistemaEjecucion, destino);
        }
    }
}
