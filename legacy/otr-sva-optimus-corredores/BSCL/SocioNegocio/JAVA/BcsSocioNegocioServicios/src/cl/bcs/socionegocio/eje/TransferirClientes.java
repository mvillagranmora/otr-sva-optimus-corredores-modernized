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
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class TransferirClientes implements JavaCall {
    private static final Logger logger = LogManager.getLogger(TransferirClientes.class);

    private final String SQL_SELECT_COUNT = loadResourceToString("cl/bcs/socionegocio/eje/sql/TransferirClientes_Select_Count.sql");
    private final String SQL_SELECT_PERSONA = loadResourceToString("cl/bcs/socionegocio/eje/sql/TransferirClientes_Select_Persona.sql");
    private final String SQL_UPDATE_CLIENTE = loadResourceToString("cl/bcs/socionegocio/eje/sql/TransferirClientes_Update_Cliente.sql");
    private final String SQL_UPDATE_PORTAFOLIO = loadResourceToString("cl/bcs/socionegocio/eje/sql/TransferirClientes_Update_Portafolio.sql");

    public static class Entrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String p_UsaReglasNegocio;
        public Long p_IdEmpleadoEjecutivoAntiguo;
        public Long p_IdEmpleadoEjecutivoNuevo;
    }

    public static class ErrorDetalle { public String Codigo; public String Descripcion; }
    public static class AdvertenciaDetalle { public String Codigo; public String RequiereAutorizacion; public String Descripcion; }
    public static class Persona { public Long IdPersona; }
    public static class Contador { public Long Contador; }
    public static class UpdCli { public Long IdPersonaEjecutivoNuevo; public Long IdEmpleadoEjecutivoAntiguo; }
    public static class UpdPor { public Long IdEmpleadoEjecutivoNuevo; public Long IdEmpleadoEjecutivoAntiguo; }
    
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

        // Validar existencia de ejecutivos antiguo y nuevo
        HashMap<String,Object> p1 = new HashMap<>();
        p1.put("IdEmpleado", in.p_IdEmpleadoEjecutivoAntiguo);
        List<Contador> c1 = sql.query(conn, SQL_SELECT_COUNT, p1, Contador.class);
        long v1 = (c1.isEmpty()||c1.get(0).Contador==null)?0L:c1.get(0).Contador;
        if (v1==0) { ErrorDetalle e=new ErrorDetalle(); e.Codigo="EJECUTIVO_ANTIGUO_NO_EXISTE"; e.Descripcion=""; out.P_OUT_ERRORESDETALLE.add(e); out.P_OUT_ERRORESCONTADOR++; return out; }

        HashMap<String,Object> p2 = new HashMap<>();
        p2.put("IdEmpleado", in.p_IdEmpleadoEjecutivoNuevo);
        List<Contador> c2 = sql.query(conn, SQL_SELECT_COUNT, p2, Contador.class);
        long v2 = (c2.isEmpty()||c2.get(0).Contador==null)?0L:c2.get(0).Contador;
        if (v2==0) { ErrorDetalle e=new ErrorDetalle(); e.Codigo="EJECUTIVO_NUEVO_NO_EXISTE"; e.Descripcion=""; out.P_OUT_ERRORESDETALLE.add(e); out.P_OUT_ERRORESCONTADOR++; return out; }

        // Obtener persona del ejecutivo nuevo
        HashMap<String,Object> sp = new HashMap<>();
        sp.put("IdEmpleado", in.p_IdEmpleadoEjecutivoNuevo);
        List<Persona> personas = sql.query(conn, SQL_SELECT_PERSONA, sp, Persona.class);
        if (personas.isEmpty() || personas.get(0).IdPersona == null) {
            ErrorDetalle e = new ErrorDetalle(); e.Codigo = "PERSONA_NUEVO_NO_ENCONTRADA"; e.Descripcion = ""; out.P_OUT_ERRORESDETALLE.add(e); out.P_OUT_ERRORESCONTADOR++; return out;
        }
        Long v_IdPersonaEjecutivoNuevo = personas.get(0).IdPersona;

        // Ejecutar updates (una vez cada uno)
        // Usar clases públicas estáticas para parámetros de batch
        TransferirClientes.UpdCli u1 = new TransferirClientes.UpdCli();
        u1.IdPersonaEjecutivoNuevo = v_IdPersonaEjecutivoNuevo;
        u1.IdEmpleadoEjecutivoAntiguo = in.p_IdEmpleadoEjecutivoAntiguo;
        sql.batch(conn, SQL_UPDATE_CLIENTE, java.util.Arrays.asList(u1), UpdCli.class);

        TransferirClientes.UpdPor u2 = new TransferirClientes.UpdPor();
        u2.IdEmpleadoEjecutivoNuevo = in.p_IdEmpleadoEjecutivoNuevo;
        u2.IdEmpleadoEjecutivoAntiguo = in.p_IdEmpleadoEjecutivoAntiguo;
        sql.batch(conn, SQL_UPDATE_PORTAFOLIO, java.util.Arrays.asList(u2), UpdPor.class);

        // Log transferencia
        String v_Fecha = FechaDB.ObtenerFechaHoraSistemaUTC(conn, in.p_IdEmpresa);
        AgregarLog agregarLogService = new AgregarLog();
        InAgregarLog entradaAgregarLog = new InAgregarLog();
        AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
        log.IdCabeceraLog = in.p_IdEmpleadoEjecutivoAntiguo;
        log.IdTipoLog = "MODIFICACION";
        log.IdModulo = in.p_IdModulo != null ? in.p_IdModulo : "SOC";
        log.IdUsuario = in.p_IdUsuario;
        log.IdEmpresa = in.p_IdEmpresa;
        log.FechaSistemaLog = v_Fecha.substring(0, 10);
        log.IdUsuarioAtendido = in.p_IdUsuario;
        log.IdTipoRegistro = "EJECUTIVO";
        log.IdRegistro = String.valueOf(in.p_IdEmpleadoEjecutivoAntiguo);
        log.NombreProceso = "Transferir Clientes";
        entradaAgregarLog.p_Log.add(log);

        AgregarLog.InAgregarLog.DetalleLog d = new AgregarLog.InAgregarLog.DetalleLog();
        d.SubRegistro = "Transferencia";
        d.CampoRegistro = "IdEmpleadoEjecutivoDestino";
        d.ValorAntiguo = String.valueOf(in.p_IdEmpleadoEjecutivoAntiguo);
        d.ValorNuevo = String.valueOf(in.p_IdEmpleadoEjecutivoNuevo);
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
                    + "    \"p_IdEmpleadoEjecutivoAntiguo\": 300,\r\n"
                    + "    \"p_IdEmpleadoEjecutivoNuevo\": 301\r\n"
                    + "}";

            Gson gson = new Gson();
            Entrada entrada = gson.fromJson(json, Entrada.class);

            TransferirClientes servicio = new TransferirClientes();
            System.out.println("ENTRADA: " + json);
            Salida salida = servicio.logica(conn, entrada);
            System.out.println(gson.toJson(salida));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

