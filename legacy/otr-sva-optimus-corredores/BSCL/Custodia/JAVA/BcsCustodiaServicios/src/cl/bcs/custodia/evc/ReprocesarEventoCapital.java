package cl.bcs.custodia.evc;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.custodia.evc.ProcesarEventoCapital.ProcesarEventoCapitalEntrada;
import cl.bcs.custodia.evc.ProcesarEventoCapital.ProcesarEventoCapitalSalida;
import cl.bcs.custodia.pojo.EventoCapital;
import cl.bcs.custodia.pojo.EventoCapitalCliente;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;


import cl.bcs.custodia.evc.ProcesarEventoCapitalCorteCupon;
import cl.bcs.custodia.evc.ProcesarEventoCapitalCorteCupon.EntradaProcesarEventoCapitalCorteCupon;
import cl.bcs.custodia.evc.ProcesarEventoCapitalCorteCupon.SalidaProcesarEventoCapitalCorteCupon;



public class ReprocesarEventoCapital implements JavaCall{
	private final Log log = new Log("CUS.ReprocesarEventoCapital");
	private final String SQL_SELECT_CONSULTAR_EVENTO_CAPITAL			= loadResourceToString("cl/bcs/custodia/sql/ReprocesarEventoCapital_SelectEventoCapital.sql");
	private final String SQL_DELETE_EVENTO_CAPITAL_CLIENTE			= loadResourceToString("cl/bcs/custodia/sql/ReprocesarEventoCapital_DeleteEventoCapitalCliente.sql");
	private final String SQL_UPDATE_EVENTO_CAPITAL			= loadResourceToString("cl/bcs/custodia/sql/ReprocesarEventoCapital_UpdateEventoCapitalCliente.sql");
	
	public static class ReprocesarEventoCapitalEntrada {
		public Long							p_IdEmpresa; 
		public Long							p_IdUsuario;
		public String						p_IdModulo;
		public String						p_Origen; //corresponde desde donde es ejecutado el evento MAN: UI, AUT: Job Pauta
		public String						p_UsaReglasNegocio;
		public List<Long>					p_IdEventoCapital;//eventos capital a procesar
				
		public ReprocesarEventoCapitalEntrada (){
			this.p_IdEventoCapital 		= new ArrayList<Long>();
		}
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	public static class ReprocesarEventoCapitalSalida {
		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;
		public List<Long>			P_OUT_SECUENCIA;
		
		public ReprocesarEventoCapitalSalida() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_SECUENCIA = new ArrayList<Long>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public class SalidaClientesConDerecho{
		public List<EventoCapitalCliente> 	P_OUT_CLIENTES;
		public List<EventoCapitalCliente> 	P_OUT_CONTRAPARTES;
		public List<MensajeSalida>			P_OUT_MENSAJES;
		public Long							P_OUT_ERRORESCONTADOR;
		
		public SalidaClientesConDerecho() {
			this.P_OUT_CLIENTES 		= new ArrayList<EventoCapitalCliente>();
			this.P_OUT_CONTRAPARTES		= new ArrayList<EventoCapitalCliente>();
			this.P_OUT_MENSAJES 		= new ArrayList<MensajeSalida>();
			this.P_OUT_ERRORESCONTADOR 	= 0L;
		}
			
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	public static class datosEvento {
		public Long p_IdEventoCapital;
		    
	    public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public ReprocesarEventoCapitalSalida logica(Connection con, ReprocesarEventoCapitalEntrada in) throws Exception {
		log.write("INICIO::CUS.ReprocesarEventoCapital");
		ReprocesarEventoCapitalSalida out = new ReprocesarEventoCapitalSalida();
		HashMap<String,String> mapMsg = new HashMap<>();
		SQL _sql = new SQL();
		Long Id = 0L;
		List<String> fechaLim = new ArrayList<>();
		List<Long> evCapital = new ArrayList<>();
		

		List<EventoCapital> evCapitalCC = new ArrayList<>();
		
				
		// obtener fecha del sistema
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		String tFechaHoraSistema   = FechaDB.ObtenerFechaHoraSistemaUTC(con, in.p_IdEmpresa);
		
		//Consulta evento a capital a procesar.
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("p_IdEmpresa", in.p_IdEmpresa);
		params.put("p_IdEventoCapital", in.p_IdEventoCapital);
		params.put("p_FechaPago", fechaSistema);
		List<EventoCapital> eventoCapital = _sql.query(con, SQL_SELECT_CONSULTAR_EVENTO_CAPITAL, params, EventoCapital.class);
				
		//******************************************************************************************************************
		//												VALIDACIONES INICIALES
		//******************************************************************************************************************			
		{
			RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();					
			RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(con, in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo);
			out.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
			
			if (eventoCapital.size() == 0) {
				mapMsg.clear();
				out.P_OUT_MENSAJES.add(new MensajeSalida(Id, MensajeSalida.MENSAJE_ERROR, "UI_EVENTO_CAPITAL_NO_EXISTE", mapMsg, con, in.p_IdEmpresa));
				Id++;
			}
			if (MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES) > 0){
				return out;
			}
		}
		
		//******************************************************************************************************************
		//													LOGICA
		//******************************************************************************************************************	
		
		///********Aplico Filtros restantes a los eventos*******
		List<datosEvento> evento = new ArrayList<>();
		for (EventoCapital ev: eventoCapital){
			datosEvento dat = new datosEvento();
			dat.p_IdEventoCapital = ev.IdEventoCapital;
			evento.add(dat);
			fechaLim.add(Fecha.stringToSqlDate(ev.FechaLimite));
			
			if (ev.IdTipoEventoCapital.equalsIgnoreCase("CC"))
			{	//Evento capital Corte cupon	
				evCapitalCC.add(ev);				
			}
			else
			{
				evCapital.add(ev.IdEventoCapital);
			}
		}
		//Elimino los evento Capital Cliente
		_sql.batch(con, SQL_DELETE_EVENTO_CAPITAL_CLIENTE, evento, datosEvento.class);
		
		//Actualizo evento capital a VIG para que lo procese nuevamente
		_sql.batch(con, SQL_UPDATE_EVENTO_CAPITAL, evento, datosEvento.class);
		//llamada a los servicios
		if (evCapital.size() > 0 && fechaLim.size() > 0){//DIVIDENDO EN ACCIONES
			ProcesarEventoCapital proc = new ProcesarEventoCapital();
			ProcesarEventoCapitalEntrada evc = new ProcesarEventoCapitalEntrada();
			evc.p_IdEmpresa = in.p_IdEmpresa; 
			evc.p_IdUsuario = in.p_IdUsuario;
			evc.p_IdModulo = in.p_IdModulo;
			evc.p_Origen = in.p_Origen; //corresponde desde donde es ejecutado el evento MAN: UI, AUT: Job Pauta
			evc.p_UsaReglasNegocio = in.p_UsaReglasNegocio;
			evc.p_FechaLimite = fechaLim; // fechas corte
			evc.p_IdEventoCapital = evCapital;//eventos capital a procesar
			ProcesarEventoCapitalSalida sal = proc.logica(con, evc);
			out.P_OUT_MENSAJES = sal.P_OUT_MENSAJES;
		}
		
		

		//PROCESAR EVENTO CAPITAL CORTE CUPON
		if (evCapitalCC.size() > 0 )
		{	
			
			List<Long> lstIdInstrumento = new ArrayList<Long>();
			
			
	        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

			java.sql.Date fechaCorteCuponDesde = new java.sql.Date(format.parse("9999-09-09").getTime());
			java.sql.Date fechaCorteCuponHasta = new java.sql.Date(format.parse("0001-01-01").getTime());			
			
			for (EventoCapital ecCC : evCapitalCC)
			{
				if (ecCC.FechaLimite.compareTo(fechaCorteCuponDesde) < 0)
				{   //Calcula fecha menor
					fechaCorteCuponDesde = ecCC.FechaLimite;
				}
				
				if (ecCC.FechaLimite.compareTo(fechaCorteCuponHasta) > 0)
				{   //Calcula fecha mayor
					fechaCorteCuponHasta = ecCC.FechaPago;
				}
				
				if (!lstIdInstrumento.contains(ecCC.IdInstrumento))
				{
					lstIdInstrumento.add(ecCC.IdInstrumento);
				}
			}
				
			ProcesarEventoCapitalCorteCupon servicioProcesarCC = new ProcesarEventoCapitalCorteCupon();
			EntradaProcesarEventoCapitalCorteCupon entradaProcesarCC = new EntradaProcesarEventoCapitalCorteCupon();
			
			entradaProcesarCC.p_IdEmpresa 	= in.p_IdEmpresa; 
			entradaProcesarCC.p_IdUsuario 	= in.p_IdUsuario;
			entradaProcesarCC.p_IdModulo 	= in.p_IdModulo;
			entradaProcesarCC.p_FechaCorteCuponDesde = fechaCorteCuponDesde.toString();
			entradaProcesarCC.p_FechaCorteCuponHasta = fechaCorteCuponHasta.toString();			
			entradaProcesarCC.p_IdInstrumento = lstIdInstrumento;
			
			SalidaProcesarEventoCapitalCorteCupon salidaProcesarCC = servicioProcesarCC.logica(con, entradaProcesarCC);
			
			out.P_OUT_MENSAJES = salidaProcesarCC.P_OUT_MENSAJES;
		}
		

		

		
		
		
		log.write("FIN::CUS.ProcesarEventoCapital");
		return out;
		
	}
		
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		Gson gson = Utils.getGsonBuilder();
		ReprocesarEventoCapitalEntrada jin = gson.fromJson(in.toString(), ReprocesarEventoCapitalEntrada.class);
		ReprocesarEventoCapitalSalida out = logica(conn, jin);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public static void main(String[] args)  throws Exception,SQLException {
		// TODO Auto-generated method stub
		Connection conn = null;
		Gson gson = new Gson();
		
		try {
			System.out.println("== BEGIN ==");
			conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties");
			//datos entrada364858
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":0,\"p_IdModulo\":\"CUS\",\"p_Origen\":\"UI\",\"p_UsaReglasNegocio\":\"S\",\"p_IdEventoCapital\":[6825797]}";
			
			ReprocesarEventoCapitalEntrada objIn = gson.fromJson(json,ReprocesarEventoCapitalEntrada.class);
			//mientras pruebo hare el rollback
			ReprocesarEventoCapital demo = new ReprocesarEventoCapital();
			ReprocesarEventoCapitalSalida ObjOut=demo.logica(conn, objIn);
			System.out.println(ObjOut.toString());
			conn.rollback();
			//conn.rollback();
			
		}catch (Exception e){
			e.printStackTrace();
		}finally {
			conn.close();
		}
			System.out.println("== END ==");

	};
}
