package cl.bcs.cuentaInversion;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class EliminarCobroComisionRegistros implements JavaCall {
    private static final Logger logger = LogManager.getLogger(EliminarCobroComisionRegistros.class);

    private final String UPDATE_COBRO_COMISION_CUSTODIA = loadResourceToString("cl/bcs/cuentaInversion/sql/CobroComision_EliminarRegistrosBlotterCus.sql");
    private final String UPDATE_COBRO_COMISION_CAJA = loadResourceToString("cl/bcs/cuentaInversion/sql/CobroComision_EliminarRegistrosBlotterCaja.sql");
    private final String UPDATE_COBRO_COMISION_EDITADO = loadResourceToString("cl/bcs/cuentaInversion/sql/CobroComision_EliminarRegistroEditado.sql");
    private final String CONSULTA_EDITADO = loadResourceToString("cl/bcs/cuentaInversion/sql/CobroComision_ConsultarEditados.sql");

    
    public static class EntradaEliminarOpcionCobroComision extends BcsPojo {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String p_UsaReglasNegocio;
        public String p_IdPortafolios;
        public String p_FechaDesde;
        public String p_FechaHasta;
        public Long p_Periodo;
        
    }
    
    public static class EditadoOUT extends BcsPojo {
        public Long IdRegistro; 
        public String EstadoComision;
        public Long IdPortafolio;
    }
    
    
    
    public static class UpdateRegistroCobro extends BcsPojo {
        public Long IdPortafolio;
        public String FechaDesde;
        public String FechaHasta;
    }
    
    public static class UpdateEditado extends BcsPojo {
        public Long IdPortafolio;
        public Long Periodo;
    }
    
    public static class respuestaOUT extends BcsPojo {
        public Long IdPortafolio;
        public String Nemotecnico;
        public BigDecimal MontoComision;
        public String IdEstadoComision;
    }
    
    

    public static class SalidaEliminarOpcionCobroComision extends BcsPojo {
        public List<MensajeSalida> P_OUT_MENSAJES;
        public Long P_OUT_ERRORESCONTADOR;
        public Long P_OUT_ADVERTENCIASCONTADOR;
        
        public SalidaEliminarOpcionCobroComision () {
            this.P_OUT_MENSAJES = new ArrayList<>();
            this.P_OUT_ERRORESCONTADOR = 0L;
            this.P_OUT_ADVERTENCIASCONTADOR = 0L;
        }
    }
    
    
    
    private void agregarLog(Connection con,Long IdPortafolio, Long idEmpresa, Long idUsuario) throws Exception {
        logger.info("INICIO::EliminarCobroComisionOpcionesCobro.agregarLog");
        long inicio = System.currentTimeMillis();
        
        String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, 1L,"PLATAFORMA", "PLA.FechaSistema");
        
        AgregarLog agregarLogService = new AgregarLog();
        AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
        entradaAgregarLog.p_IdTrabajo = 1L;
        
            AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();

            log.IdTrabajo = entradaAgregarLog.p_IdTrabajo;
            log.IdCabeceraLog = entradaAgregarLog.p_IdTrabajo;
            log.IdTipoLog = "ELIMINACION";
            log.IdModulo = "CTA";
            log.IdUsuario = idUsuario;
            log.IdEmpresa = idEmpresa;
            log.FechaSistemaLog = fechaSistema;
            log.IdUsuarioAtendido = idUsuario;
            log.IdCliente = 0L;
            log.EstadoCliente = "";
            log.NombreCliente = "";
            log.IdTipoDocumentoIdentidad = "";
            log.IdDocumentoIdentidad = "";
            log.IdPortafolio = IdPortafolio;
            log.EstadoPortafolio = "";
            log.IdTipoRegistro = "ELIMINACION_REGISTRO_COBRO_COMISION";
            log.EstadoRegistro = "";
            log.IdTipoOperacion = "";
            log.Folio = 0L;
            log.Cantidad = BigDecimal.ZERO;
            log.PrecioTasa = BigDecimal.ZERO;
            log.Monto = BigDecimal.ZERO;
            log.IdMonedaOrigen = "";
            log.IdMonedaLiquidacion = "";
            log.TipoCambio = "";
            log.Nemo = "";
            log.IdProceso = 0L;
            log.EstadoProceso = "";
            log.NombreProceso = "Eliminación del portafolio "+ IdPortafolio +" en Blotter de Comisiones ";                      
            entradaAgregarLog.p_Log.add(log);
        
        
        agregarLogService.logica(con, entradaAgregarLog);
        
        long fin = System.currentTimeMillis();
        logger.info("FIN::EliminarCobroComisionOpcionesCobro.agregarLog", fin - inicio);
    }
    
    public SalidaEliminarOpcionCobroComision logica(Connection conn, EntradaEliminarOpcionCobroComision in) throws SQLException, Exception {
        logger.info("INICIO::EliminarCobroComision");
        long inicio = System.currentTimeMillis();
        SalidaEliminarOpcionCobroComision Salida = new SalidaEliminarOpcionCobroComision();
        SQL _sql = new SQL();
        
        List<Long> registrosComision =  Arrays.asList(in.p_IdPortafolios.split(";")).stream().filter(sec -> NumberUtils.isNumber(sec)).map(sec -> Long.parseLong(sec)).collect(Collectors.toList());
        
        if(registrosComision.isEmpty()) {
            Salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_ERROR_ENTRADA_SIN_DATA",new HashMap<String, String>(),conn,in.p_IdEmpresa));
            Salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(Salida.P_OUT_MENSAJES);
            return Salida;
        }

        // ACTUALIZA EL DETALLE POR PORTAFOLIO
        List<UpdateRegistroCobro> updateRegistroCobro = new ArrayList<>();
        HashMap<String, Object> edit = new HashMap<String, Object>(2);
        edit.put("Periodo", in.p_Periodo );
        edit.put("IdPortafolio", registrosComision);
        List<EditadoOUT> registro = _sql.query(conn, CONSULTA_EDITADO, edit, EditadoOUT.class);
        
        if(!registro.isEmpty()) {
	        List<UpdateEditado> updateEditado = registro.stream().map(reg -> {
	        	UpdateEditado registroEditado = new UpdateEditado();
	        	 registroEditado.Periodo = in.p_Periodo;
	             registroEditado.IdPortafolio = reg.IdPortafolio;
	        	return registroEditado;
	        } ).collect(Collectors.toList());
	        
	        _sql.batch(conn, UPDATE_COBRO_COMISION_EDITADO, updateEditado, UpdateEditado.class);
        }
        
        for (long portafolio : registrosComision) {
            
            UpdateRegistroCobro registroPortafolio = new UpdateRegistroCobro();
            
            registroPortafolio.IdPortafolio = portafolio;
            registroPortafolio.FechaDesde = in.p_FechaDesde;
            registroPortafolio.FechaHasta = in.p_FechaHasta;
            updateRegistroCobro.add(registroPortafolio);
            
            // SE VALIDA SI EXISTE UN REGISTRO EDITADO
            agregarLog(conn, portafolio , in.p_IdEmpresa, in.p_IdUsuario);
  
        }
        _sql.batch(conn, UPDATE_COBRO_COMISION_CUSTODIA, updateRegistroCobro, UpdateRegistroCobro.class);
        
        _sql.batch(conn, UPDATE_COBRO_COMISION_CAJA, updateRegistroCobro, UpdateRegistroCobro.class);
        
        
        Salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_OK,"UI_REGISTROS_ANULADOS_CORRECTAMENTE",new HashMap<String, String>(),conn,in.p_IdEmpresa));
        
        
        

        long fin = System.currentTimeMillis();
        logger.info("FIN::EliminarCobroComision.", fin - inicio);
        
        return Salida; 
    }
    
    @Override
    public JsonObject call(Connection con, JsonObject in) throws Exception {
        Gson gson = getGsonBuilder();
        EntradaEliminarOpcionCobroComision jin=gson.fromJson(in.toString() , EntradaEliminarOpcionCobroComision.class);
        SalidaEliminarOpcionCobroComision out = logica(con,jin);
        JsonParser parser = new JsonParser();
        JsonObject dataout  = parser.parse(gson.toJson(out)).getAsJsonObject();
        
        return dataout;
    }
    
    public static void main(String[] args) throws Exception {
        System.out.println("== TEST ==");
        try (Connection con = PlataformaUtils.getPropertiesConnection("desa01")) {
            Gson gson = Utils.getGsonBuilder();         
            String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":0,\"p_IdModulo\":\"CTA\",\"p_UsaReglasNegocio\":\"N\",\"p_FechaDesde\":\"2024-01-01\",\"p_FechaHasta\":\"2024-03-31\",\"p_Periodo\":1,\"p_IdPortafolios\":85937}";
            String json2 = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":0,\"p_IdModulo\":\"CTA\",\"p_UsaReglasNegocio\":\"N\",\"p_FechaDesde\":\"2024-01-01\",\"p_FechaHasta\":\"2024-03-31\",\"p_Periodo\":1,\"p_IdPortafolios\":\"482406;85937;\"}";
            EntradaEliminarOpcionCobroComision Objin = gson.fromJson(json2, EntradaEliminarOpcionCobroComision.class);
            EliminarCobroComisionRegistros demo = new EliminarCobroComisionRegistros();
            try {
                SalidaEliminarOpcionCobroComision salida = demo.logica(con, Objin);
                System.out.println(salida.toString());
                con.commit();
//                con.rollback();
            }
            catch(Exception er) {
                er.printStackTrace();   
                con.rollback();
            }
        }
        System.out.println("== END ==");
    }

}