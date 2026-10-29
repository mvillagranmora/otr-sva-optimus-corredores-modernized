package cl.bcs.mesa.SPT;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.evt.AgregarEvento;
import cl.bcs.plataforma.pla.utils.ConsultarImagen;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.MonedaFormato;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.ConsultarImagen.ConsultarImagenEntrada;
import cl.bcs.plataforma.pla.utils.ConsultarImagen.ConsultarImagenSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class NotificarEstadoSpot implements JavaCall{
	private static final Logger logger = LogManager.getLogger(NotificarEstadoSpot.class);
			
	private final String SQL_INFO_NOTIFICACION = loadResourceToString("cl/bcs/mesa/SPT/sql/NotificarEstadoSpot_SelectInfoNotificacion.sql");
	
	
	public static class EntradaNotificarEstadoSpot{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public List<OperacionSpot> p_IdOperacionesSpot;
		
		public EntradaNotificarEstadoSpot(){
			this.p_IdOperacionesSpot = new ArrayList<OperacionSpot>();
		}
		
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class OperacionSpot{
		public Long IdOperacionSpot;
		
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaNotificarEstadoSpot{
		public Long P_OUT_ERRORESCONTADOR;
		
		public SalidaNotificarEstadoSpot(){
			this.P_OUT_ERRORESCONTADOR = 0L;
		}
		
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class InfoNotificaciones {
		//basicos
		public Long 	  IdOperacionSpot;
		public Long 	  IdCliente;
		public Long 	  IDCLIENTEPERSONA;
		public Long 	  IdPortafolioCliente;
		public Long 	  IdEmpleadoDigitador;
		public Long 	  IDEMPLEADOPERSONA;
		public Long		  IDCORREOEMPLEADO;
		public Long	      IDCORREOCLIENTE;
		//tags notificaciones
		public Long 	  FOLIO;
		public String 	  AGENTE;
		public String 	  FECHA;
		public String 	  HORA;
		public String     DOCUMENTOIDENTIDAD;
		public String	  CLIENTE;	
		public String 	  CORREDORA;
		public String 	  ATENCIONCLIENTE;
		public String	  PORTAFOLIO;
		public String	  ESTADO;
		public String	  TIPOOPERACION;
		public BigDecimal PARIDAD;
		public BigDecimal MONTORECIBIR;
		public BigDecimal TIPOCAMBIO;
		public BigDecimal MONTOENTREGAR;
		public String	  MONEDA_SECUNDARIA;
		public String	  MONEDA_PRINCIPAL;
		
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {				
		Gson gson = getGsonBuilder();				
		EntradaNotificarEstadoSpot in=gson.fromJson(jin.toString() ,EntradaNotificarEstadoSpot.class);
		SalidaNotificarEstadoSpot out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaNotificarEstadoSpot logica(Connection con, EntradaNotificarEstadoSpot in) throws Exception{
		Long init = System.currentTimeMillis();
		logger.info("Inicio servicio NotificarEstadoSpot");
		
		SalidaNotificarEstadoSpot out = new SalidaNotificarEstadoSpot();
		SQL sql = new SQL();
		
		String tFechaHoraSistema = FechaDB.ObtenerFechaHoraSistemaUTC(con, in.p_IdEmpresa);
	    Timestamp vFechaHoraSistema = Fecha.toTimeStamp(tFechaHoraSistema);
	    
	    List<Long> idOperacionesSpot = new ArrayList<Long>();
	    for(OperacionSpot op : in.p_IdOperacionesSpot){
	    	idOperacionesSpot.add(op.IdOperacionSpot);
	    }
	    
	    HashMap<String, Object> params = new HashMap<String, Object>();
	    params.put("IdEmpresa", in.p_IdEmpresa);
		params.put("SegundosZonaHoraria", in.p_IdEmpresa);
		params.put("IdOperacion", idOperacionesSpot);
		logger.debug("INICIO::SQL_INFO_NOTIFICACION");
		List<InfoNotificaciones> listaInfo = sql.query(con, SQL_INFO_NOTIFICACION, params, InfoNotificaciones.class);
		logger.debug("FIN::SQL_INFO_NOTIFICACION");
		
        String rutaLogoCorredora = "";
        ConsultarImagenEntrada imagenCorredoraIn = new ConsultarImagenEntrada();
        imagenCorredoraIn.p_IdEmpresa = in.p_IdEmpresa;
        imagenCorredoraIn.p_imagenNombres.add("LOGO_CORREDOR");
        ConsultarImagenSalida imagenCorredoraOut = new ConsultarImagen().logica(con, imagenCorredoraIn);
        if(imagenCorredoraOut.OUT_IMAGENES.size() > 0) {
        	rutaLogoCorredora = imagenCorredoraOut.OUT_IMAGENES.get(0).RutaPublica;        	
        }
		
		AgregarEvento.ServiceEntrada notifIn = new AgregarEvento.ServiceEntrada();
		notifIn.P_IDEMPRESA = in.p_IdEmpresa;
        notifIn.P_IDUSUARIO = in.p_IdUsuario;
        notifIn.P_IDMODULO = in.p_IdModulo;
        notifIn.P_EVENTOS = new ArrayList<>();
        notifIn.P_VALORES = new ArrayList<>();
        notifIn.P_ARCHIVOSADJUNTOS = new ArrayList<>();
        notifIn.P_IDDIRECCIONES = new ArrayList<>();
        for (InfoNotificaciones ord:listaInfo) {
        	// Evento de notificacion
            AgregarEvento.ObjEVENTOS evt = new AgregarEvento.ObjEVENTOS();
            evt.CodigoTipoEvento = "CAMBIO_ESTADO_OPERACION_SPOT";
            evt.IdSecuencia = ord.IdOperacionSpot;
            evt.FechaCreacionSistema =  Fecha.toStringDate(vFechaHoraSistema);            
            evt.IdPersonaRemitente = ord.IDEMPLEADOPERSONA;
            evt.IdPersonaDestinatario = ord.IDCLIENTEPERSONA;            
            evt.IdPortafolio = ord.IdPortafolioCliente;
            notifIn.P_EVENTOS.add(evt);
            
            AgregarEvento.ObjIDDIRECCIONES ndir = new AgregarEvento.ObjIDDIRECCIONES();
            ndir.IdSecuencia = ord.IdOperacionSpot;
            ndir.TipoDestinatario = "TO";
            ndir.IdDireccion = new Long(ord.IDCORREOCLIENTE);
            notifIn.P_IDDIRECCIONES.add(ndir);
            
            // Valores generales
            notifIn.addValor(ord.IdOperacionSpot, "FOLIO", ord.FOLIO+"")
            	   .addValor(ord.IdOperacionSpot, "IdCliente", ord.IdCliente+"")
                   .addValor(ord.IdOperacionSpot, "IDCLIENTEPERSONA", ord.IDCLIENTEPERSONA+"")  
                   .addValor(ord.IdOperacionSpot, "CLIENTE", ord.CLIENTE)  
                   .addValor(ord.IdOperacionSpot, "IdPortafolioCliente", ord.IdPortafolioCliente+"") 
                   .addValor(ord.IdOperacionSpot, "IdEmpleadoDigitador", ord.IdEmpleadoDigitador+"")
                   .addValor(ord.IdOperacionSpot, "AGENTE", ord.AGENTE)
                   .addValor(ord.IdOperacionSpot, "IDEMPLEADOPERSONA", ord.IDEMPLEADOPERSONA+"")
                   .addValor(ord.IdOperacionSpot, "FECHA", ord.FECHA)
                   .addValor(ord.IdOperacionSpot, "HORA", ord.HORA)
                   .addValor(ord.IdOperacionSpot, "DOCUMENTOIDENTIDAD", ord.DOCUMENTOIDENTIDAD)
                   .addValor(ord.IdOperacionSpot, "CORREDORA", ord.CORREDORA)
                   .addValor(ord.IdOperacionSpot, "ATENCIONCLIENTE", ord.ATENCIONCLIENTE)
                   .addValor(ord.IdOperacionSpot, "PORTAFOLIO", ord.PORTAFOLIO)
                   .addValor(ord.IdOperacionSpot, "ESTADO", ord.ESTADO)
                   .addValor(ord.IdOperacionSpot, "TIPOOPERACION", ord.TIPOOPERACION)
                   .addValor(ord.IdOperacionSpot, "PARIDAD", MonedaFormato.formatear(con, ord.PARIDAD, MonedaFormato.MONTO, ord.MONEDA_SECUNDARIA).valorConFormato)
                   .addValor(ord.IdOperacionSpot, "MONTORECIBIR", MonedaFormato.formatear(con, ord.MONTORECIBIR, MonedaFormato.MONTO, ord.MONEDA_PRINCIPAL).valorConFormato)
                   .addValor(ord.IdOperacionSpot, "TIPOCAMBIO", MonedaFormato.formatear(con, ord.TIPOCAMBIO , MonedaFormato.MONTO, ord.MONEDA_SECUNDARIA).valorConFormato)
                   .addValor(ord.IdOperacionSpot, "MONTOENTREGAR", MonedaFormato.formatear(con, ord.MONTOENTREGAR, MonedaFormato.MONTO, ord.MONEDA_SECUNDARIA).valorConFormato)
                   .addValor(ord.IdOperacionSpot, "LOGO_CORREDORA", rutaLogoCorredora);
        }
        
        if (notifIn.P_EVENTOS.size() > 0) {
            AgregarEvento notif = new AgregarEvento();
            AgregarEvento.ServiceSalida notifOut = notif.logica(con, notifIn);
            
            /*for (AgregarEvento.ObjIDEVENTO objIdEvento : notifOut.P_OUT_IDEVENTO) {
            	logger.debug("ID EVENTO :: " + objIdEvento.IdEvento);
            }*/
        }
        
		logger.info("Termino del servicio NotificarEstadoSpot, tiempo:" + (System.currentTimeMillis() - init));
		return out;
	}
	
	public static void main(String[] args){
		
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			NotificarEstadoSpot sp = new NotificarEstadoSpot();
			EntradaNotificarEstadoSpot in = new EntradaNotificarEstadoSpot();
			in.p_IdEmpresa = 1L;
			in.p_IdModulo = "SPT";
			in.p_IdUsuario = 21L;
			OperacionSpot orden = new OperacionSpot();
			orden.IdOperacionSpot = 6896L;
			in.p_IdOperacionesSpot.add(orden);
			sp.logica(con, in);
			con.commit();
		} catch (Exception e) {
			e.printStackTrace();			
		}	
		System.out.println("== END ==");
	}

}
