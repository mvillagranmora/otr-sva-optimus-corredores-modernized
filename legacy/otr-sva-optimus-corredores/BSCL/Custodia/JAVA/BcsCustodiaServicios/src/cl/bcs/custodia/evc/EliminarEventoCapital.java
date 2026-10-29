package cl.bcs.custodia.evc;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.AgregarLog.InAgregarLog;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class EliminarEventoCapital implements JavaCall{
	private final Log log = new Log("CUS.EliminarEventoCapital");
	
	private final String SQL_SELECT_EVENTO_CAPITAL_CLIENTE= "SELECT \"IdEventoCapital\" FROM  \"BSCL.Custodia.BD::CUS.EventoCapitalCliente\" WHERE \"IdEventoCapital\" IN (:p_IdEventoCapital)";
	private final String SQL_SELECT_EVENTO_CAPITAL= loadResourceToString("cl/bcs/custodia/sql/EliminarEventoCapital_SelectEventoCapital.sql");
	private final String SQL_UPDATE_EVENTO_CAPITAL= loadResourceToString("cl/bcs/custodia/sql/EliminarEventoCapital_UpdateEventoCapital.sql");
	private final String SQL_UPDATE_EVENTO_CAPITAL_CLIENTE= loadResourceToString("cl/bcs/custodia/sql/EliminarEventoCapital_UpdateEventoCapitalCliente.sql");
	
	public static class EventoCapital_IN {
		public Long p_IdEmpresa;
	    public Long	p_IdUsuario;
	    public String p_IdModulo;
	    public String p_UsaReglasNegocio;
	    public List<Evento> p_EventoCapital;
	    	    
	    public EventoCapital_IN() {
			this.p_EventoCapital = new ArrayList<Evento>();
		}

		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class EventoCapital_OUT {
		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;
		public List<Long>			P_OUT_SECUENCIA;
		
		public EventoCapital_OUT() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_SECUENCIA = new ArrayList<Long>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class Evento {
		public Long 	p_IdEventoCapital;
		public String  	p_IdTipoEventoCapital;
		//----
		public Long		p_IdEmpresa;
		public String 	p_FechaHoraUltimaActualizacion;     
		public String 	p_IdEstado;
		
	    public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class DatosEvento {
		public Long 	IdEventoCapital;
		public String  	IdTipoEventoCapital;
		public String 	NombreEventoCapital;
		public String 	Nemo;     
		public Long     Secuencia;
		public String   IdEstado;
		
	    public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class EventoExiste {
		public Long 		IdEventoCapital;
		
			    
	    public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class Instrumento {
		public Long 		IdInstrumento;
		public String		Nemotecnico;
		public String		IdTipoInstrumento;
		public String		IdClasificacionTipoInstrumento;
		public String		IdEstado;
		public String		CallConfirmado;
		public String		CuponeraVariable;
		public Date			FechaPagoCupon;
		
	    public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public EventoCapital_OUT logica (Connection conn, EventoCapital_IN in) throws Exception {
		SQL _sql = new SQL();
		Long Id = 0L;
		HashMap<String,String> mapMsg = new HashMap<>();
		log.write("INICIO::CUS.EliminarEventoCapital");
		EventoCapital_OUT out = new EventoCapital_OUT();
		HashMap<String, Object> params = new HashMap<>();
		List<Long> idEventoCapital = new ArrayList<>();
		List<EventoExiste> eventoClienteExistente = new ArrayList<>();
		//Map<String, Object> eventoClienteExistenteMap = new HashMap<>();
		AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
		entradaAgregarLog.p_IdTrabajo = 1L;
		
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		String fechaHoraSistema = FechaDB.ObtenerFechaHoraSistemaLocal(conn,in.p_IdEmpresa);
		for (Evento ev: in.p_EventoCapital){
			idEventoCapital.add(ev.p_IdEventoCapital);
			
		}
		
		if (idEventoCapital.size() > 0){
			params = new HashMap<>();
			params.put("p_IdEventoCapital", idEventoCapital);
			eventoClienteExistente = _sql.query(conn, SQL_SELECT_EVENTO_CAPITAL_CLIENTE, params, EventoExiste.class);
			//eventoClienteExistenteMap = Utils.List2Hash(eventoClienteExistente, "IdEventoCapital");
		}
		//::Validaciones
		{
			if (idEventoCapital.size() <= 0){
				// No Viene Evento
				mapMsg.clear();
				out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"SIN_DATOS_A_PROCESAR",mapMsg,conn,in.p_IdEmpresa));
				Id++;
			}
			for (Evento eve : in.p_EventoCapital) {
				for (EventoExiste eveCli : eventoClienteExistente) {
					if (eve.p_IdTipoEventoCapital.equalsIgnoreCase("CC"))  continue;  //Algunos EC (Corte cupón) deben permitir eliminar los clientes con derecho
					if (eveCli.IdEventoCapital.equals(eve.p_IdEventoCapital))
					{
						mapMsg.clear();
						out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"EVENTO_CAPITAL_EXISTEN_CLIENTES_ASOCIADOS",mapMsg,conn,in.p_IdEmpresa));
						Id++;
						break;
					}
				}
			}
			if (MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES) > 0){
				return out;
			}
		}
		{
			List<Evento> datosUpdateEC  = new ArrayList<>();
			List<Evento> datosUpdateECC = new ArrayList<>();
			
			List<AgregarLog.InAgregarLog.Log> logList = new ArrayList<>();
			
			List<DatosEvento> datosEC = new ArrayList<>();
			params.clear();
			params.put("p_IdEmpresa", in.p_IdEmpresa);
			params.put("p_IdEventoCapital",idEventoCapital);
			datosEC = _sql.query(conn, SQL_SELECT_EVENTO_CAPITAL, params, DatosEvento.class);
			HashMap<String, Object> hDatosEC = Utils.List2Hash(datosEC, "IdEventoCapital");
			
			for (Evento evCap: in.p_EventoCapital){
				Evento inEv = new Evento();
				inEv.p_IdEventoCapital = evCap.p_IdEventoCapital;
				inEv.p_IdEmpresa		= in.p_IdEmpresa;
				inEv.p_FechaHoraUltimaActualizacion	= fechaHoraSistema;
			    inEv.p_IdEstado		= "ANU";
			    
			    datosUpdateEC.add(inEv);
			    
 			    //Algunos EC (Corte cupón) deben permitir eliminar los clientes con derecho
			    if (evCap.p_IdTipoEventoCapital.equalsIgnoreCase("CC"))
			    {
			    	datosUpdateECC.add(inEv);
			    }
			    
			    mapMsg.clear();
			    mapMsg.put("p_IdEventoCapital", evCap.p_IdEventoCapital.toString());
				out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_OK,"UI_EVENTO_CAPITAL_ANULADO",mapMsg,conn,in.p_IdEmpresa));
				Id++;
				
				DatosEvento evento = (DatosEvento) hDatosEC.get(inEv.p_IdEventoCapital);
				//parametros log
				AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
				log.IdTrabajo		= entradaAgregarLog.p_IdTrabajo; 
				log.IdCabeceraLog	= evCap.p_IdEventoCapital; 
				log.IdTipoLog		= "ELIMINACION"; 
				log.IdModulo		= in.p_IdModulo; 
				log.IdUsuario		= in.p_IdUsuario; 
				log.IdEmpresa		= in.p_IdEmpresa; 
				log.FechaSistemaLog	= fechaSistema; 
				log.IdUsuarioAtendido	= in.p_IdUsuario; 
				log.IdCliente			= 0L; 
				log.EstadoCliente		= ""; 
				log.NombreCliente		= ""; 
				log.IdTipoDocumentoIdentidad	= ""; 
				log.IdDocumentoIdentidad	= ""; 
				log.IdPortafolio			= 0L; 
				log.EstadoPortafolio		= ""; 
				log.IdTipoRegistro			= "ELIMINAREVENTOCAPITAL"; 
				log.IdRegistro				= evCap.p_IdEventoCapital.toString(); 
				log.EstadoRegistro			= ""; 
				log.IdTipoOperacion			= ""; 
				log.Folio					= 0L; 
				log.Cantidad				= BigDecimal.ZERO; 
				log.PrecioTasa				= BigDecimal.ZERO; 
				log.Monto					= BigDecimal.ZERO; 
				log.IdMonedaOrigen			= ""; 
				log.IdMonedaLiquidacion		= ""; 
				log.TipoCambio				= ""; 
				log.Nemo					= evento.Nemo; 
				log.IdProceso				= 0L; 
				log.EstadoProceso			= ""; 
				log.NombreProceso			= "cl.bcs.custodia.evc.EliminarEventoCapital";
				logList.add(log);
				
				entradaAgregarLog.p_LogDetalle.add(CrearEventoCapital.agregarDetalleLog(log.IdCabeceraLog,  "ELIMINAREVENTOCAPITAL", "IdTipoEventoCapital", evento.NombreEventoCapital, ""));
				entradaAgregarLog.p_LogDetalle.add(CrearEventoCapital.agregarDetalleLog(log.IdCabeceraLog,  "ELIMINAREVENTOCAPITAL", "Secuencia", evento.Secuencia.toString(), ""));
			}
						
			entradaAgregarLog.p_Log = logList;		
			_sql.batch(conn, SQL_UPDATE_EVENTO_CAPITAL, datosUpdateEC, Evento.class);
			
			
			//Algunos EC (Corte cupón) deben permitir eliminar los clientes con derecho
			if (datosUpdateECC.size() > 0)    
			{
				_sql.batch(conn, SQL_UPDATE_EVENTO_CAPITAL_CLIENTE, datosUpdateECC, Evento.class);
			}
			
		}
		//AGREGAR LOG
		{
			AgregarLog agregarLogService = new AgregarLog();
			agregarLogService.logica(conn, entradaAgregarLog);
		}
				
				
		log.write("FIN::CUS.EliminarEventoCapital");
		return out;
	};
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		Gson gson = Utils.getGsonBuilder();
		EventoCapital_IN jin = gson.fromJson(in.toString(), EventoCapital_IN.class);
		EventoCapital_OUT out = logica(conn, jin);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	};
	
	public static void main(String[] args)  throws Exception,SQLException {
		// TODO Auto-generated method stub
		Connection conn = null;
		Gson gson = new Gson();
		
		try {
			System.out.println("== BEGIN ==");
			conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties");
			//datos entrada
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":0,\"p_IdModulo\":\"CUS\",\"p_UsaReglasNegocio\":\"S\",\"p_EventoCapital\":[{\"p_IdEventoCapital\":6825676,\"p_IdTipoEventoCapital\":\"DV\",\"p_IdEmpresa\":1,\"p_FechaHoraUltimaActualizacion\":\"/Date(1720528486000)/\",\"p_IdEstado\":\"VIG\"},{\"p_IdEventoCapital\":6825673,\"p_IdTipoEventoCapital\":\"DV\",\"p_IdEmpresa\":1,\"p_FechaHoraUltimaActualizacion\":\"/Date(1720524587000)/\",\"p_IdEstado\":\"VIG\"}]}";
			//{\"p_IdEventoCapital\":363629},
			
			EventoCapital_IN objIn = gson.fromJson(json,EventoCapital_IN.class);
			//mientras pruebo hare el rollback
			EliminarEventoCapital demo = new EliminarEventoCapital();
			
			EventoCapital_OUT ObjOut=demo.logica(conn, objIn);
				
			System.out.println(ObjOut.toString());
			
			conn.rollback();
			
		}catch (Exception e){
			e.printStackTrace();
		}finally {
			conn.close();
		}
			System.out.println("== END ==");

	};
}
