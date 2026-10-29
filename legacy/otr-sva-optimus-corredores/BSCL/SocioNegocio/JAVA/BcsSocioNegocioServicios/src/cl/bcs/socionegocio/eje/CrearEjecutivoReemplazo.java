package cl.bcs.socionegocio.eje;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.AgregarLog.InAgregarLog;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;


public class CrearEjecutivoReemplazo implements cl.bcs.utils.JavaCall {
    private static final Logger logger = LogManager.getLogger(CrearEjecutivoReemplazo.class);

    private final String SQL_SELECT_COUNT = loadResourceToString("cl/bcs/socionegocio/eje/sql/CrearEjecutivoReemplazo_Select_Count.sql");
    private final String SQL_UPDATE_REEMPLAZO = loadResourceToString("cl/bcs/socionegocio/eje/sql/CrearEjecutivoReemplazo_Update_Reemplazo.sql");

    // ...existing code...

    public static class Entrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String p_UsaReglasNegocio;

        public Long p_IdEmpleadoEjecutivo;
        public Long p_IdEmpleadoReemplazo;
        public String p_FechaInicioReemplazo; // YYYY-MM-DD
        public String p_FechaFinReemplazo;    // YYYY-MM-DD
    }

    public static class ErrorDetalle { public String Codigo; public String Descripcion; }
    public static class AdvertenciaDetalle { public String Codigo; public String RequiereAutorizacion; public String Descripcion; }
    public static class Salida {
        public Integer P_OUT_ADVERTENCIASCONTADOR;
        public List<AdvertenciaDetalle> P_OUT_ADVERTENCIASDETALLE;
        public Integer P_OUT_ERRORESCONTADOR;
        public List<ErrorDetalle> P_OUT_ERRORESDETALLE;

        public Salida() {
            this.P_OUT_ADVERTENCIASCONTADOR = 0;
            this.P_OUT_ADVERTENCIASDETALLE = new ArrayList<>();
            this.P_OUT_ERRORESCONTADOR = 0;
            this.P_OUT_ERRORESDETALLE = new ArrayList<>();
        }
    }
    public static class Contador { public Long Contador; }

    public Salida logica(Connection conn, Entrada in) throws Exception {
        Salida out = new Salida();
        SQL sql = new SQL();

        // Validación: existen ejecutivo y reemplazo
        HashMap<String,Object> p = new HashMap<>();
        p.put("IdEmpleadoEjecutivo", in.p_IdEmpleadoEjecutivo);
        List<Contador> ce = sql.query(conn, SQL_SELECT_COUNT, p, Contador.class);
        long c1 = (ce.isEmpty()||ce.get(0).Contador==null)?0L:ce.get(0).Contador;
        if (c1==0) { ErrorDetalle e=new ErrorDetalle(); e.Codigo="EJECUTIVO_NO_EXISTE"; e.Descripcion=""; out.P_OUT_ERRORESDETALLE.add(e); out.P_OUT_ERRORESCONTADOR++; return out; }

        HashMap<String,Object> p2 = new HashMap<>();
        p2.put("IdEmpleadoEjecutivo", in.p_IdEmpleadoReemplazo);
        List<Contador> cr = sql.query(conn, SQL_SELECT_COUNT, p2, Contador.class);
        long c2 = (cr.isEmpty()||cr.get(0).Contador==null)?0L:cr.get(0).Contador;
        if (c2==0) { ErrorDetalle e=new ErrorDetalle(); e.Codigo="EJECUTIVO_NO_EXISTE"; e.Descripcion=""; out.P_OUT_ERRORESDETALLE.add(e); out.P_OUT_ERRORESCONTADOR++; return out; }

        // Update campos de reemplazo (usar clase pública estática)
        CrearEjecutivoReemplazo.Upd u = new CrearEjecutivoReemplazo.Upd();
        u.IdEmpleadoReemplazo = in.p_IdEmpleadoReemplazo;
        u.FechaInicioReemplazo = in.p_FechaInicioReemplazo;
        u.FechaFinReemplazo = in.p_FechaFinReemplazo;
        u.IdEmpleadoEjecutivo = in.p_IdEmpleadoEjecutivo;
        sql.batch(conn, SQL_UPDATE_REEMPLAZO, java.util.Arrays.asList(u), Upd.class);

        // Log según SP
        String v_Fecha = FechaDB.ObtenerFechaHoraSistemaUTC(conn, in.p_IdEmpresa);
        AgregarLog agregarLogService = new AgregarLog();
        InAgregarLog entradaAgregarLog = new InAgregarLog();
        AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
        log.IdCabeceraLog = in.p_IdEmpleadoEjecutivo;
        log.IdTipoLog = "INGRESO";
        log.IdModulo = in.p_IdModulo != null ? in.p_IdModulo : "SOC";
        log.IdUsuario = in.p_IdUsuario;
        log.IdEmpresa = in.p_IdEmpresa;
        log.FechaSistemaLog = v_Fecha.substring(0, 10);
        log.IdUsuarioAtendido = in.p_IdUsuario;
        log.IdTipoRegistro = "EJECUTIVO";
        log.IdRegistro = String.valueOf(in.p_IdEmpleadoEjecutivo);
        log.NombreProceso = "Crear Ejecutivo Reemplazo";
        entradaAgregarLog.p_Log.add(log);

        AgregarLog.InAgregarLog.DetalleLog d1 = new AgregarLog.InAgregarLog.DetalleLog();
        d1.SubRegistro = "EjecutivoReemplazo";
        d1.CampoRegistro = "IdEmpleadoReemplaza";
        d1.ValorAntiguo = "";
        d1.ValorNuevo = String.valueOf(in.p_IdEmpleadoReemplazo);
        entradaAgregarLog.p_LogDetalle.add(d1);

        AgregarLog.InAgregarLog.DetalleLog d2 = new AgregarLog.InAgregarLog.DetalleLog();
        d2.SubRegistro = "FechaInicioReemplazo";
        d2.CampoRegistro = "FechaInicioReemplazo";
        d2.ValorAntiguo = "";
        d2.ValorNuevo = in.p_FechaInicioReemplazo;
        entradaAgregarLog.p_LogDetalle.add(d2);

        AgregarLog.InAgregarLog.DetalleLog d3 = new AgregarLog.InAgregarLog.DetalleLog();
        d3.SubRegistro = "FechaFinReemplazo";
        d3.CampoRegistro = "FechaFinReemplazo";
        d3.ValorAntiguo = "";
        d3.ValorNuevo = in.p_FechaFinReemplazo;
        entradaAgregarLog.p_LogDetalle.add(d3);

        agregarLogService.logica(conn, entradaAgregarLog);

        return out;
    }

    // Clase de parámetros para UPDATE reemplazo
    public static class Upd { public Long IdEmpleadoReemplazo; public String FechaInicioReemplazo; public String FechaFinReemplazo; public Long IdEmpleadoEjecutivo; }

    @Override
    public JsonObject call(Connection conn, JsonObject jsonEntrada) throws Exception {
        Gson gson = Utils.getGsonBuilder();
        Entrada entrada = gson.fromJson(jsonEntrada.toString(), Entrada.class);
        Salida salida = logica(conn, entrada);
        return gson.fromJson(gson.toJson(salida), JsonObject.class);
    }

    public static void main(String[] args) throws Exception {
        try {
            Connection conn = PlataformaUtils.getPropertiesConnection("desa01","connections.properties");
            
            String json = "{\r\n"
                    + "    \"p_IdEmpresa\": 1,\r\n"
                    + "    \"p_IdUsuario\": 100,\r\n"
                    + "    \"p_IdModulo\": \"SOC\",\r\n"
                    + "    \"p_UsaReglasNegocio\": \"N\",\r\n"
                    + "    \"p_IdEmpleadoEjecutivo\": 300,\r\n"
                    + "    \"p_IdEmpleadoReemplazo\": 301,\r\n"
                    + "    \"p_FechaInicioReemplazo\": \"2025-01-01\",\r\n"
                    + "    \"p_FechaFinReemplazo\": \"2025-12-31\"\r\n"
                    + "}";

            Gson gson = new Gson();
            Entrada entrada = gson.fromJson(json, Entrada.class);

            CrearEjecutivoReemplazo servicio = new CrearEjecutivoReemplazo();
            System.out.println("ENTRADA");
            System.out.println(entrada);
            
            Salida salida = servicio.logica(conn, entrada);
            System.out.println(salida);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


