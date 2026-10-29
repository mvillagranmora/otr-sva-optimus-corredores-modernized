
package cl.bcs.socionegocio.eje;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.AgregarLog.InAgregarLog;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
// Clase pública del servicio implementa JavaCall (sin adaptadores)

public class CrearEjecutivo implements cl.bcs.utils.JavaCall {
    private static final Logger logger = LogManager.getLogger(CrearEjecutivo.class);

    private final String SQL_SELECT_COUNT = loadResourceToString("cl/bcs/socionegocio/eje/sql/CrearEjecutivo_Select_Count.sql");
    private final String SQL_INSERT_EJECUTIVO = loadResourceToString("cl/bcs/socionegocio/eje/sql/CrearEjecutivo_Insert_Ejecutivo.sql");

    public static class EntradaCrearEjecutivo {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String p_UsaReglasNegocio;

        public Long p_IdEmpleadoSupervisor;
        public Long p_IdEmpleadoEjecutivo;
        public String p_IdEstado;
    }

    public static class AdvertenciaDetalle {
        public String Codigo;
        public String RequiereAutorizacion;
        public String Descripcion;
    }

    public static class ErrorDetalle {
        public String Codigo;
        public String Descripcion;
    }

    public static class SalidaCrearEjecutivo {
        public Integer P_OUT_ADVERTENCIASCONTADOR;
        public List<AdvertenciaDetalle> P_OUT_ADVERTENCIASDETALLE;
        public Integer P_OUT_ERRORESCONTADOR;
        public List<ErrorDetalle> P_OUT_ERRORESDETALLE;

        public SalidaCrearEjecutivo() {
            this.P_OUT_ADVERTENCIASCONTADOR = 0;
            this.P_OUT_ADVERTENCIASDETALLE = new ArrayList<>();
            this.P_OUT_ERRORESCONTADOR = 0;
            this.P_OUT_ERRORESDETALLE = new ArrayList<>();
        }
    }

    public static class ContadorResult {
        public Long Contador;
    }

    // POJO para parámetros nombrados en INSERT (debe ser accesible públicamente)
    public static class InsertParams {
        public Long IdEmpleadoEjecutivo;
        public Long IdEmpleadoSupervisor;
        public String Fecha;
        public Long IdUsuario;
        public String IdEstado;
    }

    // POJO core: no implementa JavaCall
    public SalidaCrearEjecutivo logica(Connection conn, EntradaCrearEjecutivo in) throws Exception {
        SalidaCrearEjecutivo salida = new SalidaCrearEjecutivo();
        SQL sql = new SQL();

        // Validación: ya existe relación supervisor-ejecutivo
        HashMap<String, Object> params = new HashMap<>();
        params.put("IdEmpleadoEjecutivo", in.p_IdEmpleadoEjecutivo);
        params.put("IdEmpleadoSupervisor", in.p_IdEmpleadoSupervisor);
        List<ContadorResult> res = sql.query(conn, SQL_SELECT_COUNT, params, ContadorResult.class);
        long count = (res.isEmpty() || res.get(0).Contador == null) ? 0L : res.get(0).Contador;
        if (count > 0) {
            ErrorDetalle err = new ErrorDetalle();
            err.Codigo = "EJECUTIVO_EXISTE";
            HashMap<String, String> dicVals = new HashMap<>();
            String msg = Diccionario.getMensaje(conn, in.p_IdEmpresa, "EJECUTIVO_EXISTE", dicVals);
            err.Descripcion = msg;
            salida.P_OUT_ERRORESDETALLE.add(err);
            salida.P_OUT_ERRORESCONTADOR++;
            return salida;
        }

        // Obtener fecha de sistema (v_Fecha)
        String v_Fecha = FechaDB.ObtenerFechaHoraSistemaUTC(conn, in.p_IdEmpresa);

        // Insertar en EJE.Ejecutivo
        InsertParams insertParams = new InsertParams();
        insertParams.IdEmpleadoEjecutivo = in.p_IdEmpleadoEjecutivo;
        insertParams.IdEmpleadoSupervisor = in.p_IdEmpleadoSupervisor;
        insertParams.Fecha = v_Fecha;
        insertParams.IdUsuario = in.p_IdUsuario;
        insertParams.IdEstado = in.p_IdEstado;
        sql.batch(conn, SQL_INSERT_EJECUTIVO, java.util.Arrays.asList(insertParams), InsertParams.class);

        // Armar y enviar log (equivalente a CALL AgregarLog)
        AgregarLog agregarLogService = new AgregarLog();
        InAgregarLog entradaAgregarLog = new InAgregarLog();
        entradaAgregarLog.p_IdEmpresa = in.p_IdEmpresa;
        entradaAgregarLog.p_IdUsuario = in.p_IdUsuario;
        entradaAgregarLog.p_IdModulo = in.p_IdModulo;
        Long idTrabajo = (long) (Math.random() * 10000);

        AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
        log.IdTrabajo = idTrabajo;
        log.IdCabeceraLog = in.p_IdEmpleadoEjecutivo;
        log.IdTipoLog = "INGRESO";
        log.IdModulo = in.p_IdModulo != null ? in.p_IdModulo : "SOC";
        log.IdUsuario = in.p_IdUsuario;
        log.IdEmpresa = in.p_IdEmpresa;
        log.FechaSistemaLog = v_Fecha.substring(0, 10); // YYYY-MM-DD
        log.IdUsuarioAtendido = in.p_IdUsuario;
        log.IdTipoRegistro = "EJECUTIVO";
        log.IdRegistro = String.valueOf(in.p_IdEmpleadoEjecutivo);
        log.NombreProceso = "Crear Ejecutivo";

        entradaAgregarLog.p_Log.add(log);

        // Detalle log: IdEmpleadoSupervisor
        AgregarLog.InAgregarLog.DetalleLog d1 = new AgregarLog.InAgregarLog.DetalleLog();
        d1.IdTrabajo = idTrabajo;
        d1.IdCabeceraLog = log.IdCabeceraLog;
        d1.SubRegistro = "";
        d1.CampoRegistro = "IdEmpleadoSupervisor";
        d1.ValorAntiguo = "";
        d1.ValorNuevo = String.valueOf(in.p_IdEmpleadoSupervisor);
        entradaAgregarLog.p_LogDetalle.add(d1);

        // Detalle log: IdEmpleadoEjecutivo
        AgregarLog.InAgregarLog.DetalleLog d2 = new AgregarLog.InAgregarLog.DetalleLog();
        d2.IdTrabajo = idTrabajo;
        d2.IdCabeceraLog = log.IdCabeceraLog;
        d2.SubRegistro = "";
        d2.CampoRegistro = "IdEmpleadoEjecutivo";
        d2.ValorAntiguo = "";
        d2.ValorNuevo = String.valueOf(in.p_IdEmpleadoEjecutivo);
        entradaAgregarLog.p_LogDetalle.add(d2);

        agregarLogService.logica(conn, entradaAgregarLog);
        return salida;
    }

    @Override
    public JsonObject call(Connection conn, JsonObject jsonEntrada) throws Exception {
        Gson gson = Utils.getGsonBuilder();
        EntradaCrearEjecutivo entrada = gson.fromJson(jsonEntrada.toString(), EntradaCrearEjecutivo.class);
        SalidaCrearEjecutivo salida = logica(conn, entrada);
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
                    + "    \"p_IdEmpleadoSupervisor\": 10,\r\n"
                    + "    \"p_IdEmpleadoEjecutivo\": 300,\r\n"
                    + "    \"p_IdEstado\": \"ACT\"\r\n"
                    + "}";

            Gson gson = new Gson();
            EntradaCrearEjecutivo entrada = gson.fromJson(json, EntradaCrearEjecutivo.class);

            CrearEjecutivo servicio = new CrearEjecutivo();
            System.out.println("ENTRADA");
            System.out.println(entrada);
            
            SalidaCrearEjecutivo salida = servicio.logica(conn, entrada);
            System.out.println(salida);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
 
