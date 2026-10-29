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

public class EliminarEjecutivoReemplazo implements cl.bcs.utils.JavaCall {
    private static final Logger logger = LogManager.getLogger(EliminarEjecutivoReemplazo.class);

    private final String SQL_SELECT_COUNT = loadResourceToString("cl/bcs/socionegocio/eje/sql/EliminarEjecutivoReemplazo_Select_Count.sql");
    private final String SQL_UPDATE_CLEAR = loadResourceToString("cl/bcs/socionegocio/eje/sql/EliminarEjecutivoReemplazo_Update_Clear.sql");

    public static class Entrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String p_UsaReglasNegocio;
        public Long p_IdEmpleadoEjecutivo;
    }

    public static class ErrorDetalle { public String Codigo; public String Descripcion; }
    public static class AdvertenciaDetalle { public String Codigo; public String RequiereAutorizacion; public String Descripcion; }
    public static class Contador { public Long Contador; }
    public static class Upd { public Long IdEmpleadoEjecutivo; }
    
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

    public Salida logica(Connection conn, Entrada in) throws Exception {
        Salida out = new Salida();
        SQL sql = new SQL();

        HashMap<String,Object> p = new HashMap<>();
        p.put("IdEmpleadoEjecutivo", in.p_IdEmpleadoEjecutivo);
        List<Contador> c = sql.query(conn, SQL_SELECT_COUNT, p, Contador.class);
        long v = (c.isEmpty()||c.get(0).Contador==null)?0L:c.get(0).Contador;
        if (v==0) { ErrorDetalle e=new ErrorDetalle(); e.Codigo="EJECUTIVO_NO_EXISTE"; e.Descripcion=""; out.P_OUT_ERRORESDETALLE.add(e); out.P_OUT_ERRORESCONTADOR++; return out; }

        // Clase pública estática para parámetros de batch
        EliminarEjecutivoReemplazo.Upd u = new EliminarEjecutivoReemplazo.Upd();
        u.IdEmpleadoEjecutivo = in.p_IdEmpleadoEjecutivo;
        sql.batch(conn, SQL_UPDATE_CLEAR, java.util.Arrays.asList(u), Upd.class);

        String v_Fecha = FechaDB.ObtenerFechaHoraSistemaUTC(conn, in.p_IdEmpresa);
        AgregarLog agregarLogService = new AgregarLog();
        InAgregarLog entradaAgregarLog = new InAgregarLog();
        AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
        log.IdCabeceraLog = in.p_IdEmpleadoEjecutivo;
        log.IdTipoLog = "MODIFICACION";
        log.IdModulo = in.p_IdModulo != null ? in.p_IdModulo : "SOC";
        log.IdUsuario = in.p_IdUsuario;
        log.IdEmpresa = in.p_IdEmpresa;
        log.FechaSistemaLog = v_Fecha.substring(0, 10);
        log.IdUsuarioAtendido = in.p_IdUsuario;
        log.IdTipoRegistro = "EJECUTIVO";
        log.IdRegistro = String.valueOf(in.p_IdEmpleadoEjecutivo);
        log.NombreProceso = "Eliminar Ejecutivo Reemplazo";
        entradaAgregarLog.p_Log.add(log);

        AgregarLog.InAgregarLog.DetalleLog d = new AgregarLog.InAgregarLog.DetalleLog();
        d.SubRegistro = "EjecutivoReemplazo";
        d.CampoRegistro = "IdEmpleadoReemplaza";
        d.ValorAntiguo = "";
        d.ValorNuevo = "";
        entradaAgregarLog.p_LogDetalle.add(d);

        agregarLogService.logica(conn, entradaAgregarLog);

        return out;
    }

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
                    + "    \"p_IdEmpleadoEjecutivo\": 300\r\n"
                    + "}";

            Gson gson = new Gson();
            Entrada entrada = gson.fromJson(json, Entrada.class);

            EliminarEjecutivoReemplazo servicio = new EliminarEjecutivoReemplazo();
            System.out.println("ENTRADA");
            System.out.println(entrada);
            
            Salida salida = servicio.logica(conn, entrada);
            System.out.println(salida);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
   

