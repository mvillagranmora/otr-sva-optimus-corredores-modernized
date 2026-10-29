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

public class ModificarEstadoEjecutivo implements cl.bcs.utils.JavaCall {
    private static final Logger logger = LogManager.getLogger(ModificarEstadoEjecutivo.class);

    private final String SQL_SELECT_COUNT = loadResourceToString("cl/bcs/socionegocio/eje/sql/ModificarEstadoEjecutivo_Select_Count.sql");
    private final String SQL_SELECT_ESTADO = loadResourceToString("cl/bcs/socionegocio/eje/sql/ModificarEstadoEjecutivo_Select_Estado.sql");
    private final String SQL_UPDATE_ESTADO = loadResourceToString("cl/bcs/socionegocio/eje/sql/ModificarEstadoEjecutivo_Update_Estado.sql");

    public static class Entrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String p_UsaReglasNegocio;

        public Long p_IdEmpleadoEjecutivo;
        public String p_IdEstado;
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
    public static class EstadoRow { public String IdEstado; }
    public static class Upd { public String IdEstado; public Long IdEmpleadoEjecutivo; }

    public Salida logica(Connection conn, Entrada in) throws Exception {
        Salida out = new Salida();
        SQL sql = new SQL();

        // Validación: existe el ejecutivo
        HashMap<String,Object> p1 = new HashMap<>();
        p1.put("IdEmpleadoEjecutivo", in.p_IdEmpleadoEjecutivo);
        List<Contador> c = sql.query(conn, SQL_SELECT_COUNT, p1, Contador.class);
        long count = (c.isEmpty() || c.get(0).Contador == null) ? 0L : c.get(0).Contador;
        if (count == 0) {
            ErrorDetalle e = new ErrorDetalle();
            e.Codigo = "EJECUTIVO_NO_EXISTE";
            e.Descripcion = ""; // Mensaje se delega a capa superior según diccionario si aplica
            out.P_OUT_ERRORESDETALLE.add(e);
            out.P_OUT_ERRORESCONTADOR++;
            return out;
        }

        // Valor antiguo para log
        List<EstadoRow> ant = sql.query(conn, SQL_SELECT_ESTADO, p1, EstadoRow.class);
        String idEstadoAnt = ant.isEmpty() ? "" : (ant.get(0).IdEstado == null ? "" : ant.get(0).IdEstado);

        // Update estado (usar clase pública estática para evitar problemas de reflexión)
        ModificarEstadoEjecutivo.Upd u = new ModificarEstadoEjecutivo.Upd();
        u.IdEstado = in.p_IdEstado;
        u.IdEmpleadoEjecutivo = in.p_IdEmpleadoEjecutivo;
        sql.batch(conn, SQL_UPDATE_ESTADO, java.util.Arrays.asList(u), Upd.class);

        // Log según SP
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
        log.NombreProceso = "Modificar Estado Ejecutivo";
        entradaAgregarLog.p_Log.add(log);

        AgregarLog.InAgregarLog.DetalleLog d = new AgregarLog.InAgregarLog.DetalleLog();
        d.SubRegistro = "Estado";
        d.CampoRegistro = "IdEstado";
        d.ValorAntiguo = idEstadoAnt;
        d.ValorNuevo = in.p_IdEstado;
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
                    + "    \"p_IdEmpleadoEjecutivo\": 300,\r\n"
                    + "    \"p_IdEstado\": \"ACT\"\r\n"
                    + "}";

            Gson gson = new Gson();
            Entrada entrada = gson.fromJson(json, Entrada.class);

            ModificarEstadoEjecutivo servicio = new ModificarEstadoEjecutivo();
            System.out.println("ENTRADA");
            System.out.println(entrada);
            
            Salida salida = servicio.logica(conn, entrada);
            System.out.println(salida);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

