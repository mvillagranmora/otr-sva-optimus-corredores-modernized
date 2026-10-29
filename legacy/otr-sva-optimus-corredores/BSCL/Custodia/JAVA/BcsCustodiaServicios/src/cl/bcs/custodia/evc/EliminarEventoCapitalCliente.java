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
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class EliminarEventoCapitalCliente implements JavaCall{
	private final Log log = new Log("CUS.EliminarEventoCapitalCliente");
	private final String SQL_SELECT_EVENTO_CAPITAL_CLIENTE= "SELECT \"IdEventoCapitalCliente\" AS \"p_IdEventoCapitalCliente\", \"IdEventoCapital\" AS \"p_IdEventoCapital\" FROM  \"BSCL.Custodia.BD::CUS.EventoCapitalCliente\" WHERE \"IdEventoCapitalCliente\" IN (:p_IdEventoCapitalCliente) AND \"IdEstado\" = 'CER'";
	private final String SQL_SELECT_DATOS_EVENTO_CAPITAL_CLIENTE = loadResourceToString("cl/bcs/custodia/sql/EliminarEventoCapitalCliente_SelectEventoCapitalCliente.sql");
	private final String SQL_DELETE_EVENTO_CAPITAL= loadResourceToString("cl/bcs/custodia/sql/EliminarEventoCapitalCliente_DeleteEventoCapital.sql");

	
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
		public Long p_IdEventoCapital;
		public Long p_IdEventoCapitalCliente;
		public String p_Nemo;
		public String p_IdMoneda;
		public String p_IdTipoEventoCapital;
		public Long p_Secuencia;
		public String p_NombreCliente;
		public Long p_IdCliente;
		
	    public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public EventoCapital_OUT logica (Connection conn, EventoCapital_IN in) throws Exception {
		SQL _sql = new SQL();
		Long Id = 0L;
		HashMap<String,String> mapMsg = new HashMap<>();
		log.write("INICIO::CUS.EliminarEventoCapitalCliente");
		EventoCapital_OUT out = new EventoCapital_OUT();
		HashMap<String, Object> params = new HashMap<>();
		List<Long> idEventoCapitalCliente = new ArrayList<>();
		List<Evento> eventoClienteExistente = new ArrayList<>();
		Map<String, Object> eventoClienteExistenteMap = new HashMap<>();
		List<Evento> datosEventoCliente = new ArrayList<>();
		Map<String, Object> datosEventoClienteMap = new HashMap<>();
		AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
		entradaAgregarLog.p_IdTrabajo = 1L;
		
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		String fechaHoraSistema = FechaDB.ObtenerFechaHoraSistemaLocal(conn,in.p_IdEmpresa);
		for (Evento ev: in.p_EventoCapital){
			idEventoCapitalCliente.add(ev.p_IdEventoCapitalCliente);
			
		}
		
		if (idEventoCapitalCliente.size() > 0){
			params = new HashMap<>();
			params.put("p_IdEventoCapitalCliente", idEventoCapitalCliente);
			eventoClienteExistente = _sql.query(conn, SQL_SELECT_EVENTO_CAPITAL_CLIENTE, params, Evento.class);
			eventoClienteExistenteMap = Utils.List2Hash(eventoClienteExistente, "p_IdEventoCapitalCliente");
		}
		//::Validaciones
		{
			if (idEventoCapitalCliente.size() <= 0){
				// No Viene Evento
				mapMsg.clear();
				out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"SIN_DATOS_A_PROCESAR",mapMsg,conn,in.p_IdEmpresa));
				Id++;
			}
			for (Evento eve : in.p_EventoCapital) {
				Evento evExi = (Evento) eventoClienteExistenteMap.get(eve.p_IdEventoCapitalCliente);
				if (evExi != null){
					mapMsg.clear();
					out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"EVENTO_CAPITAL_CLIENTE_NO_DISPONIBLE",mapMsg,conn,in.p_IdEmpresa));
					Id++;
					break;
				}
			}
			if (MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES) > 0){
				return out;
			}
		}
		{
			List<Evento> datosDelete = new ArrayList<>();
			List<AgregarLog.InAgregarLog.Log> logList = new ArrayList<>();
			datosEventoCliente = _sql.query(conn, SQL_SELECT_DATOS_EVENTO_CAPITAL_CLIENTE, params, Evento.class);
			datosEventoClienteMap = Utils.List2Hash(datosEventoCliente, "p_IdEventoCapitalCliente");
			for (Evento evCap: in.p_EventoCapital){
				Evento inEv = new Evento();
				inEv.p_IdEventoCapitalCliente = evCap.p_IdEventoCapitalCliente;
				
			    datosDelete.add(inEv);
			    
			    mapMsg.clear();
			    mapMsg.put("p_IdEventoCapitalCliente", evCap.p_IdEventoCapitalCliente.toString());
				out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_OK,"UI_EVENTO_CAPITAL_ELIMINADO",mapMsg,conn,in.p_IdEmpresa));
				Id++;
				
				Evento evento = (Evento) datosEventoClienteMap.get(inEv.p_IdEventoCapitalCliente);
				//parametros log
				AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
				log.IdTrabajo		= entradaAgregarLog.p_IdTrabajo; 
				log.IdCabeceraLog	= evCap.p_IdEventoCapitalCliente; 
				log.IdTipoLog		= "ELIMINACION"; 
				log.IdModulo		= in.p_IdModulo; 
				log.IdUsuario		= in.p_IdUsuario; 
				log.IdEmpresa		= in.p_IdEmpresa; 
				log.FechaSistemaLog	= fechaSistema; 
				log.IdUsuarioAtendido	= in.p_IdUsuario; 
				log.IdCliente			= evento.p_IdCliente;
				log.EstadoCliente		= ""; 
				log.NombreCliente		= evento.p_NombreCliente; 
				log.IdTipoDocumentoIdentidad	= ""; 
				log.IdDocumentoIdentidad	= ""; 
				log.IdPortafolio			= 0L; 
				log.EstadoPortafolio		= ""; 
				log.IdTipoRegistro			= "ELIMINAREVENTOCAPITAL"; 
				log.IdRegistro				= evCap.p_IdEventoCapitalCliente.toString(); 
				log.EstadoRegistro			= ""; 
				log.IdTipoOperacion			= ""; 
				log.Folio					= 0L; 
				log.Cantidad				= BigDecimal.ZERO; 
				log.PrecioTasa				= BigDecimal.ZERO; 
				log.Monto					= BigDecimal.ZERO; 
				log.IdMonedaOrigen			= evento.p_IdMoneda; 
				log.IdMonedaLiquidacion		= ""; 
				log.TipoCambio				= ""; 
				log.Nemo					= evento.p_Nemo; 
				log.IdProceso				= 0L; 
				log.EstadoProceso			= ""; 
				log.NombreProceso			= "cl.bcs.custodia.evc.EliminarEventoCapitalCliente";
				logList.add(log);
				
				entradaAgregarLog.p_LogDetalle.add(CrearEventoCapital.agregarDetalleLog(log.IdCabeceraLog,  "ELIMINAREVENTOCAPITALCLIENTE", "IdTipoEventoCapital", evento.p_IdTipoEventoCapital, ""));
				entradaAgregarLog.p_LogDetalle.add(CrearEventoCapital.agregarDetalleLog(log.IdCabeceraLog,  "ELIMINAREVENTOCAPITALCLIENTE", "Secuencia", evento.p_Secuencia.toString(), ""));
			}
						
			entradaAgregarLog.p_Log = logList;
			_sql.batch(conn, SQL_DELETE_EVENTO_CAPITAL, datosDelete, Evento.class);
		}
		//AGREGAR LOG
		{
			AgregarLog agregarLogService = new AgregarLog();
			agregarLogService.logica(conn, entradaAgregarLog);
		}
				
				
		log.write("FIN::CUS.EliminarEventoCapitalCliente");
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
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":0,\"p_IdModulo\":\"CUS\",\"p_UsaReglasNegocio\":\"S\",\"p_EventoCapital\":[{\"p_IdEventoCapital\":6825675,\"p_IdEventoCapitalCliente\":6908436}, {\"p_IdEventoCapital\":6825675,\"p_IdEventoCapitalCliente\":6908438} ]}";
			//{\"p_IdEventoCapital\":363629},
			
			EventoCapital_IN objIn = gson.fromJson(json,EventoCapital_IN.class);
			//mientras pruebo hare el rollback
			EliminarEventoCapitalCliente demo = new EliminarEventoCapitalCliente();
			
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
