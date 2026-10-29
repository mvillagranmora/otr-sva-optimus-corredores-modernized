package cl.bcs.socionegocio.eje;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.AgregarLog.InAgregarLog;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ModificarEjecutivoPortafolio implements JavaCall {
    private static final Logger logger = LogManager.getLogger(ModificarEjecutivoPortafolio.class);

    private final String SQL_SELECT_COUNT = loadResourceToString("cl/bcs/socionegocio/eje/sql/ModificarEjecutivoPortafolio_Select_Count.sql");
    private final String SQL_SELECT_PERSONA = loadResourceToString("cl/bcs/socionegocio/eje/sql/ModificarEjecutivoPortafolio_Select_Persona.sql");
    private final String SQL_SELECT_PERSONA_ANTERIOR = loadResourceToString("cl/bcs/socionegocio/eje/sql/ModificarEjecutivoPortafolio_Select_PersonaAnterior.sql");
    private final String SQL_SELECT_EMPLEADO_ANTERIOR = loadResourceToString("cl/bcs/socionegocio/eje/sql/ModificarEjecutivoPortafolio_Select_EmpleadoAnterior.sql");
    private final String SQL_UPDATE_CLIENTE = loadResourceToString("cl/bcs/socionegocio/eje/sql/ModificarEjecutivoPortafolio_Update_Cliente.sql");
    private final String SQL_UPDATE_PORTAFOLIO = loadResourceToString("cl/bcs/socionegocio/eje/sql/ModificarEjecutivoPortafolio_Update_Portafolio.sql");

    public static class Entrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String p_UsaReglasNegocio;
        public Long p_IdCliente;
        public Long p_IdPortafolio;
        public Long p_IdEmpleadoEjecutivo;
        public String p_EsPrincipal; // S/N
        public String p_ClientePrivado; // S/N
    }

    public static class ErrorDetalle { public String Codigo; public String Descripcion; }
    public static class AdvertenciaDetalle { public String Codigo; public String RequiereAutorizacion; public String Descripcion; }
    public static class Contador { public Long Contador; }
    public static class Persona { public Long IdPersona; }
    public static class PersonaAnterior { public Long IdPersonaEjecutivo; }
    public static class EmpleadoAnterior { public Long IdEmpleadoEjecutivo; }
    public static class UpdCliente { public Long IdPersonaEjecutivo; public Long IdCliente; }
    public static class UpdPortafolio { public Long IdEmpleadoEjecutivo; public String EsEjecutivoExclusivo; public Long IdPortafolio; public Long IdCliente; }
    
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

        // Validar existencia de ejecutivo
        HashMap<String,Object> p1 = new HashMap<>();
        p1.put("IdEmpleadoEjecutivo", in.p_IdEmpleadoEjecutivo);
        List<Contador> c = sql.query(conn, SQL_SELECT_COUNT, p1, Contador.class);
        long count = (c.isEmpty()||c.get(0).Contador==null)?0L:c.get(0).Contador;
        if (count==0) { ErrorDetalle e=new ErrorDetalle(); e.Codigo="EJECUTIVO_NO_EXISTE"; e.Descripcion=""; out.P_OUT_ERRORESDETALLE.add(e); out.P_OUT_ERRORESCONTADOR++; return out; }

        Long v_IdPersonaEjecutivo = null;
        Long v_IdPersonaEjecutivoAnterior = null;
        
        // Si es principal, actualizar ejecutivo del cliente
        if ("S".equals(in.p_EsPrincipal)) {
            // Obtener IdPersona del ejecutivo nuevo
            HashMap<String,Object> sp = new HashMap<>();
            sp.put("IdEmpleado", in.p_IdEmpleadoEjecutivo);
            List<Persona> personas = sql.query(conn, SQL_SELECT_PERSONA, sp, Persona.class);
            v_IdPersonaEjecutivo = personas.isEmpty() || personas.get(0).IdPersona == null ? null : personas.get(0).IdPersona;

            // Obtener IdPersonaEjecutivo anterior del cliente
            HashMap<String,Object> spa = new HashMap<>();
            spa.put("IdCliente", in.p_IdCliente);
            List<PersonaAnterior> personasAnt = sql.query(conn, SQL_SELECT_PERSONA_ANTERIOR, spa, PersonaAnterior.class);
            v_IdPersonaEjecutivoAnterior = personasAnt.isEmpty() || personasAnt.get(0).IdPersonaEjecutivo == null ? null : personasAnt.get(0).IdPersonaEjecutivo;

            // Actualizar cliente
            UpdCliente uc = new UpdCliente();
            uc.IdPersonaEjecutivo = v_IdPersonaEjecutivo;
            uc.IdCliente = in.p_IdCliente;
            sql.batch(conn, SQL_UPDATE_CLIENTE, java.util.Arrays.asList(uc), UpdCliente.class);
        }

        // Obtener IdEmpleadoEjecutivo anterior del portafolio
        HashMap<String,Object> pea = new HashMap<>();
        pea.put("IdPortafolio", in.p_IdPortafolio);
        pea.put("IdCliente", in.p_IdCliente);
        List<EmpleadoAnterior> empAnt = sql.query(conn, SQL_SELECT_EMPLEADO_ANTERIOR, pea, EmpleadoAnterior.class);
        Long v_IdEmpleadoEjecutivoPortafolioAnterior = empAnt.isEmpty() || empAnt.get(0).IdEmpleadoEjecutivo == null ? null : empAnt.get(0).IdEmpleadoEjecutivo;

        // Actualizar portafolio
        UpdPortafolio up = new UpdPortafolio();
        up.IdEmpleadoEjecutivo = in.p_IdEmpleadoEjecutivo;
        up.EsEjecutivoExclusivo = in.p_ClientePrivado != null ? in.p_ClientePrivado : "N";
        up.IdPortafolio = in.p_IdPortafolio;
        up.IdCliente = in.p_IdCliente;
        sql.batch(conn, SQL_UPDATE_PORTAFOLIO, java.util.Arrays.asList(up), UpdPortafolio.class);

        // Agregar log
        String v_Fecha = FechaDB.ObtenerFechaHoraSistemaUTC(conn, in.p_IdEmpresa);
        AgregarLog agregarLogService = new AgregarLog();
        InAgregarLog entradaAgregarLog = new InAgregarLog();
        AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
        log.IdCabeceraLog = in.p_IdCliente;
        log.IdTipoLog = "MODIFICACION";
        log.IdModulo = in.p_IdModulo != null ? in.p_IdModulo : "SOC";
        log.IdUsuario = in.p_IdUsuario;
        log.IdEmpresa = in.p_IdEmpresa;
        log.FechaSistemaLog = v_Fecha.substring(0, 10);
        log.IdUsuarioAtendido = in.p_IdUsuario;
        log.IdTipoRegistro = "EJECUTIVO";
        log.IdRegistro = String.valueOf(in.p_IdCliente);
        log.NombreProceso = "Modificar Ejecutivo Portafolio";
        entradaAgregarLog.p_Log.add(log);

        // Detalle log - Cliente
        AgregarLog.InAgregarLog.DetalleLog d1 = new AgregarLog.InAgregarLog.DetalleLog();
        d1.SubRegistro = "Cliente";
        d1.CampoRegistro = "IdCliente";
        d1.ValorAntiguo = "";
        d1.ValorNuevo = String.valueOf(in.p_IdCliente);
        entradaAgregarLog.p_LogDetalle.add(d1);

        // Detalle log - EjecutivoPortafolio
        AgregarLog.InAgregarLog.DetalleLog d2 = new AgregarLog.InAgregarLog.DetalleLog();
        d2.SubRegistro = "EjecutivoPortafolio";
        d2.CampoRegistro = "IdEmpleadoEjecutivo";
        d2.ValorAntiguo = v_IdEmpleadoEjecutivoPortafolioAnterior != null ? String.valueOf(v_IdEmpleadoEjecutivoPortafolioAnterior) : "";
        d2.ValorNuevo = String.valueOf(in.p_IdEmpleadoEjecutivo);
        entradaAgregarLog.p_LogDetalle.add(d2);

        // Detalle log - EjecutivoCliente (si es principal)
        if ("S".equals(in.p_EsPrincipal)) {
            AgregarLog.InAgregarLog.DetalleLog d3 = new AgregarLog.InAgregarLog.DetalleLog();
            d3.SubRegistro = "EjecutivoCliente";
            d3.CampoRegistro = "IdPersonaEjecutivo";
            d3.ValorAntiguo = v_IdPersonaEjecutivoAnterior != null ? String.valueOf(v_IdPersonaEjecutivoAnterior) : "";
            d3.ValorNuevo = v_IdPersonaEjecutivo != null ? String.valueOf(v_IdPersonaEjecutivo) : "";
            entradaAgregarLog.p_LogDetalle.add(d3);
        }

        // Detalle log - Portafolio
        AgregarLog.InAgregarLog.DetalleLog d4 = new AgregarLog.InAgregarLog.DetalleLog();
        d4.SubRegistro = "Portafolio";
        d4.CampoRegistro = "IdPortafolio";
        d4.ValorAntiguo = "";
        d4.ValorNuevo = String.valueOf(in.p_IdPortafolio);
        entradaAgregarLog.p_LogDetalle.add(d4);

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
                    + "    \"p_IdCliente\": 1000,\r\n"
                    + "    \"p_IdPortafolio\": 2000,\r\n"
                    + "    \"p_IdEmpleadoEjecutivo\": 300,\r\n"
                    + "    \"p_EsPrincipal\": \"S\",\r\n"
                    + "    \"p_ClientePrivado\": \"N\"\r\n"
                    + "}";

            Gson gson = new Gson();
            Entrada entrada = gson.fromJson(json, Entrada.class);

            ModificarEjecutivoPortafolio servicio = new ModificarEjecutivoPortafolio();
            System.out.println("ENTRADA: " + json);
            Salida salida = servicio.logica(conn, entrada);
            System.out.println(gson.toJson(salida));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
