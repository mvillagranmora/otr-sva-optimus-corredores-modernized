
package cl.bcs.normativa.circulares;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Timestamp;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.AgregarLog.InAgregarLog;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;

//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;


public class ReeprocesarEventoCircular1985 implements JavaCall {
	static final Logger logger = LogManager.getLogger(ReeprocesarEventoCircular1985.class);

	//private static final Logger logger = LogManager.getLogger(NotificarNCG365FFMM.class);
	
	private final String SQL_SELECT_DETALLE_EVENTO		= loadResourceToString("cl/bcs/normativa/circulares/sql/ReeprocesarEventoCircular1985_SelectEventoDetalle.sql");
	private final String SQL_SELECT_DIRECCION_ELETRONICA_ACTUAL		= loadResourceToString("cl/bcs/normativa/circulares/sql/ReeprocesarEventoCircular1985_SelectDireccionElectronicaCliente.sql");
	private final String SQL_UPDATE_EVENTO		= loadResourceToString("cl/bcs/normativa/circulares/sql/ReeprocesarEventoCircular1985_UpdateEstadoEvento.sql");
	private final String SQL_UPDATE_DIRECCION_ELECTRONICA		= loadResourceToString("cl/bcs/normativa/circulares/sql/ReeprocesarEventoCircular1985_UpdateDireccionElectronicaEvento.sql");



	public static class ReeprocesarEventoCircular1985_Entrada {
		public Long			p_IdEmpresa;
		public Long			p_IdUsuario;
		public String 		p_IdModulo;
		public List<DetalleEvento> p_Eventos;

		public  ReeprocesarEventoCircular1985_Entrada() {
			p_Eventos = new ArrayList<DetalleEvento>();
		}
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class DetalleEvento {
		public Long p_IdEvento = 0L;
		public Long p_IdCliente = 0L;
	}

	public static class Evento {
		public Long IdEvento;
		
	}
	public static class Email {
		public Long IdEmail;
		public Long IdEvento;
	}
	
	public static class ObjDetalleEvento {
		public Long IdEvento;
		public String Procesado;
		public Long IdDireccionElectronica;
		public Long IdCliente;
	}
	
	public static class ObjEmailCliente {
		public Long IdDireccionElectronicaActual;
		public Long IdCliente;
	}
	
	
	public static class ReeprocesarEventoCircular1985_Salida {
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR;
		public ReeprocesarEventoCircular1985_Salida() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_ERRORESCONTADOR = 0L;
		}
		
	}

	public JsonObject logicaAsync(Connection con,ReeprocesarEventoCircular1985_Entrada in) throws Exception {
		Gson gson = getGsonBuilder();	
		ReeprocesarEventoCircular1985_Entrada jin = gson.fromJson(in.toString(), ReeprocesarEventoCircular1985_Entrada.class);
		ReeprocesarEventoCircular1985_Salida out = logica(con, jin);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	
	// ---------------------------------------------------
	// LOGICA
	// ---------------------------------------------------
	
	public ReeprocesarEventoCircular1985_Salida logica(Connection con, ReeprocesarEventoCircular1985_Entrada in) throws Exception {
		SQL sql = new SQL();
		
		logger.info("INICIO SERVICIO");
		
		ReeprocesarEventoCircular1985_Salida out = new ReeprocesarEventoCircular1985_Salida();
		
        
		try {
			HashMap<String, String> valores = new HashMap<String, String>();
			out.P_OUT_ERRORESCONTADOR = 0L;
			List<Evento> paramsUpdateEventos = getEventos(con, in.p_Eventos);
			if (paramsUpdateEventos.size() != 0) {
				List<Email> paramsUpdateEmails = getEmails(con, in.p_Eventos);
				if (paramsUpdateEmails.size() != 0) {
					logger.info("INICIO - SQL_UPDATE_DIRECCION_ELECTRONICA");
					sql.batch(con, SQL_UPDATE_DIRECCION_ELECTRONICA, paramsUpdateEmails, Email.class);
					logger.info("FIN - SQL_UPDATE_DIRECCION_ELECTRONICA");
					logger.info("INICIO - SQL_UPDATE_EVENTO");
					sql.batch(con, SQL_UPDATE_EVENTO, paramsUpdateEventos, Evento.class);
					logger.info("FIN - SQL_UPDATE_EVENTO");
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_OK, "UI_NOTIFICACION_REENVIADA",
							valores, con, in.p_IdEmpresa));
					return out;
				} else {
					logger.info("INICIO - SQL_UPDATE_EVENTO");
					sql.batch(con, SQL_UPDATE_EVENTO, paramsUpdateEventos, Evento.class);
					logger.info("FIN - SQL_UPDATE_EVENTO");
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_OK, "UI_NOTIFICACION_REENVIADA",
							valores, con, in.p_IdEmpresa));
					
					//LOG DE AUDITORIA
					AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
					String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, 1L, "PLATAFORMA", "PLA.FechaSistema");
					Long idCabeceraLog = 159L;
					if (!in.p_Eventos.isEmpty() && in.p_Eventos != null) {
			            for (DetalleEvento datosLog: in.p_Eventos) {
			            	
							AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
							log.IdCabeceraLog = idCabeceraLog;
							log.IdTipoLog = "INFORMATIVO";
							log.IdModulo = "NOR";
							log.IdUsuario = in.p_IdUsuario;
							log.IdEmpresa = in.p_IdEmpresa;
							log.FechaSistemaLog = fechaSistema != null ? fechaSistema : "";
							log.IdUsuarioAtendido = 0L;
							log.IdCliente = datosLog.p_IdCliente != null ? datosLog.p_IdCliente : 0L;
							log.EstadoCliente = "";
							log.NombreCliente = "";
							log.IdTipoDocumentoIdentidad = "";
							log.IdDocumentoIdentidad = "";
							log.IdPortafolio = 0L;
							log.EstadoPortafolio = "";
							log.IdTipoRegistro = "CIRCULAR1985_REENVIO_NOTIFICACION";
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
							log.NombreProceso = "ReeprocesarEventoCircular1985";						
							entradaAgregarLog.p_Log.add(log);
							
							//detalle log 
							AgregarLog.InAgregarLog.DetalleLog detLog = new AgregarLog.InAgregarLog.DetalleLog();
							detLog.IdCabeceraLog = idCabeceraLog;
							detLog.IdTrabajo = 0L;
							detLog.SubRegistro = "CIRCULAR1985_REENVIO_NOTIFICACION";
							detLog.CampoRegistro = "Descripcion:  ";
							detLog.ValorAntiguo = "";
							detLog.ValorNuevo = "Se reenviaron notificaciones de evento circular 1985";
							entradaAgregarLog.p_LogDetalle.add(detLog);
							
							idCabeceraLog = idCabeceraLog + 1L;
						}
			            
			            AgregarLog agregarLogService = new AgregarLog();
						agregarLogService.logica(con, entradaAgregarLog);
					}
					return out;
				}
			} else {
				out.P_OUT_ERRORESCONTADOR++;
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_EVENTO_NO_EXISTE",
						valores, con, in.p_IdEmpresa));
				return out;
			}
			
			
	        
		}
		catch(Exception e) {
			logger.error(e);
			
			e.printStackTrace();
			throw e;
		}
        
     	
     	
   
        
	}
	
	public List<Email> getEmails(Connection con, List<DetalleEvento> evts) throws Exception {
		SQL sql = new SQL();
		HashMap<String, Object> params = new HashMap<>(1);
		HashMap<String, Object> paramsEmail = new HashMap<String, Object>(1);
		HashMap<Long, ObjEmailCliente> emailsCliente = new HashMap<>();
		List<ObjDetalleEvento> eventos = new ArrayList<>();
		List<Email> updateEmails = new ArrayList<>();
		List<Long> idEventos = new ArrayList<Long>(); 
		List<Long> idClientes = new ArrayList<Long>(); 
		for (DetalleEvento evento : evts) {
			idEventos.add(evento.p_IdEvento);
		}
		params.put("p_IdEvento", idEventos);
		if (params.size() > 0) {
			logger.info("INICIO - SQL_SELECT_DETALLE_EVENTO");
			eventos = sql.query(con, SQL_SELECT_DETALLE_EVENTO, params, ObjDetalleEvento.class);
			logger.info("FIN - SQL_SELECT_DETALLE_EVENTO");
		}
		
		for (DetalleEvento cliente : evts) {
			idClientes.add(cliente.p_IdCliente);
			
		}
		paramsEmail.put("p_IdCliente", idClientes);
		
		if (paramsEmail.size() > 0) {
			logger.info("INICIO - SQL_SELECT_DIRECCION_ELETRONICA_ACTUAL");
			List<ObjEmailCliente> emails = sql.query(con, SQL_SELECT_DIRECCION_ELETRONICA_ACTUAL, paramsEmail,ObjEmailCliente.class);
			HashMap<Object, Object> __emailsCliente = Utils.List2Hash(emails, "IdCliente");
			for (Object key : __emailsCliente.keySet()) {
				emailsCliente.put((Long) key, (ObjEmailCliente) __emailsCliente.get(key));
			}
			logger.info("FIN - SQL_SELECT_DIRECCION_ELETRONICA_ACTUAL");

			
		}
		if (eventos.size() > 0) {
			for (ObjDetalleEvento evt : eventos) {
				
				if (emailsCliente.size() != 0) {
					if (evt.IdDireccionElectronica
							.compareTo(emailsCliente.get(evt.IdCliente).IdDireccionElectronicaActual) != 0) {
						Email email = new Email();
						email.IdEmail = emailsCliente.get(evt.IdCliente).IdDireccionElectronicaActual;
						email.IdEvento = evt.IdEvento;
						updateEmails.add(email);
					}
				}

			}
		}
		return updateEmails;

	}

	public List<Evento> getEventos(Connection con, List<DetalleEvento> evts) throws Exception {
	    SQL sql = new SQL();
	    HashMap<String, Object> params = new HashMap<String, Object>(1);
	    HashMap<Long, ObjDetalleEvento> eventosDetalle = new HashMap<>();
	    List<Evento> updateEventos = new ArrayList<>();
	    List<Long> idEventos = new ArrayList<Long>();

	    for (DetalleEvento evento : evts) {
	        idEventos.add(evento.p_IdEvento);
	    }

	    params.put("p_IdEvento", idEventos);

	    if (!idEventos.isEmpty()) {
	        logger.info("INICIO - SQL_SELECT_DETALLE_EVENTO");

	        List<ObjDetalleEvento> eventos = sql.query(con, SQL_SELECT_DETALLE_EVENTO, params, ObjDetalleEvento.class);

	        for (ObjDetalleEvento detalleEvento : eventos) {
	            eventosDetalle.put(detalleEvento.IdEvento, detalleEvento);
	        }

	        logger.info("FIN - SQL_SELECT_DETALLE_EVENTO");
	    }

	    for (DetalleEvento evento : evts) {
	        ObjDetalleEvento detalleEvento = eventosDetalle.get(evento.p_IdEvento);

	        if (detalleEvento != null && "S".equals(detalleEvento.Procesado)) {
	            Evento evt = new Evento();
	            evt.IdEvento = detalleEvento.IdEvento;
	            updateEventos.add(evt);
	        }
	    }

	    return updateEventos;
	}
	// ---------------------------------------------------
	// CALL
	// ---------------------------------------------------
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {				
		Gson gson = getGsonBuilder();				
		ReeprocesarEventoCircular1985_Entrada in=gson.fromJson(jin.toString() ,ReeprocesarEventoCircular1985_Entrada.class);
		ReeprocesarEventoCircular1985_Salida out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}	

	
	
	// ---------------------------------------------------
	// METODO PARA PRUEBAS LOCALES
	// ---------------------------------------------------
	public static void main(String[] args){
		
		System.out.println("== TEST ==");
		
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			ReeprocesarEventoCircular1985 sp = new ReeprocesarEventoCircular1985();
			ReeprocesarEventoCircular1985_Entrada in = new ReeprocesarEventoCircular1985_Entrada();
			String master01 = "{\r\n"
					+ "    \"p_IdEmpresa\": 1,\r\n"
					+ "    \"p_IdUsuario\": 0,\r\n"
					+ "    \"p_IdModulo\": \"NOR\",\r\n"
					+ "    \"p_Eventos\": [\r\n"
					+ "        {\r\n"
					+ "            \"p_IdEvento\": 75118680,\r\n"
					+ "            \"p_IdCliente\": 61625\r\n"
					+ "        },\r\n"
					+ "        {\r\n"
					+ "            \"p_IdEvento\": 75118682,\r\n"
					+ "            \"p_IdCliente\": 61625\r\n"
					+ "        },\r\n"
					+ "        {\r\n"
					+ "            \"p_IdEvento\": 75118684,\r\n"
					+ "            \"p_IdCliente\": 61625\r\n"
					+ "        }\r\n"
					+ "    ]\r\n"
					+ "}";
			ReeprocesarEventoCircular1985_Entrada Objin = gson.fromJson(master01, ReeprocesarEventoCircular1985_Entrada.class);
			// "2020-08-03"; // Si va en 0001-01-01 toma la fecha de sistema

			ReeprocesarEventoCircular1985_Salida OUT = new ReeprocesarEventoCircular1985_Salida();
			OUT = sp.logica(con, Objin);
			System.out.println(OUT);
			con.commit();
			//con.rollback();
		} catch (Exception e) {
			e.printStackTrace();			
		}	
		System.out.println("== END ==");
	}

}