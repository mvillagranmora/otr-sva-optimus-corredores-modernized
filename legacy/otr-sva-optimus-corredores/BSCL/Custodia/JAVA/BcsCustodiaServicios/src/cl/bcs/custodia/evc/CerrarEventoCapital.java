package cl.bcs.custodia.evc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.custodia.evc.CerrarEventoCapitalSorteoLetras.CerrarEventoCapitalSorteoLetrasEntrada;
import cl.bcs.custodia.evc.CerrarEventoCapitalSorteoLetras.CerrarEventoCapitalSorteoLetrasSalida;
import cl.bcs.custodia.pojo.EventoCapital;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CerrarEventoCapital implements JavaCall{
	private final Log log = new Log("CUS.CerrarEventoCapital");
	private final String SQL_SELECT_CONSULTAR_EVENTO_CAPITAL			= loadResourceToString("cl/bcs/custodia/sql/CerrarEventoCapital_SelectEventoCapital.sql");
	
	public static class CerrarEventoCapitalEntrada {
		public Long							p_IdEmpresa; 
		public Long							p_IdUsuario;
		public String						p_IdModulo;
		//public String						p_Origen; //corresponde desde donde es ejecutado el evento MAN: UI, AUT: Job Pauta
		public String						p_UsaReglasNegocio;
		
		public List<String>					p_FechaPago; // fechas pago del evento
		public List<Long>					p_IdEventoCapital;//eventos capital a procesar
		public List<String>					p_IdTipoEventoCapital; //tipos eventos capital a procesar
		
		public CerrarEventoCapitalEntrada (){
			this.p_FechaPago 			= new ArrayList<String>();
			this.p_IdEventoCapital 		= new ArrayList<Long>();
			this.p_IdTipoEventoCapital 	= new ArrayList<String>();
		}
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	public static class CerrarEventoCapitalSalida {
		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;
		public List<Long>			P_OUT_SECUENCIA;
		
		public CerrarEventoCapitalSalida() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_SECUENCIA = new ArrayList<Long>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public CerrarEventoCapitalSalida logica(Connection con, CerrarEventoCapitalEntrada in) throws Exception {
		log.write("INICIO::CUS.CerrarEventoCapital");
		CerrarEventoCapitalSalida out = new CerrarEventoCapitalSalida();
		HashMap<String,String> mapMsg = new HashMap<>();
		SQL sql = new SQL();
		Long Id = 0L;
		List<EventoCapital> eventoCapitalEP = new ArrayList<>();
		List<EventoCapital> eventoCapitalDA = new ArrayList<>();
		List<EventoCapital> eventoCapitalDV = new ArrayList<>();
		List<EventoCapital> eventoCapitalCN = new ArrayList<>();
		List<EventoCapital> eventoCapitalCC = new ArrayList<>();
		List<EventoCapital> eventoCapitalSL = new ArrayList<>();
		List<EventoCapital> eventoCapitalDS = new ArrayList<>();
		List<EventoCapital> eventoCapitalCA = new ArrayList<>();
		List<EventoCapital> eventoCapitalSU = new ArrayList<>();
		List<EventoCapital> eventoCapitalPB = new ArrayList<>();
		List<EventoCapital> eventoCapitalDF = new ArrayList<>();
		List<EventoCapital> eventoCapitalIds = new ArrayList<>();		
		List<EventoCapital> eventoCapitalEL = new ArrayList<>();     // EMISION LIBERADA
		
		//Map<String, Object> eventoCapitalMap_IN = new HashMap<>();
		//Map<String, Object> TipoEventoCapitalMap_IN = new HashMap<>();
		// obtener fecha del sistema
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		
		if (in.p_FechaPago.size() == 0){
			 in.p_FechaPago.add(fechaSistema);
		}
		//Consulta evento a capital a procesar.
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("p_IdEmpresa", in.p_IdEmpresa);
		params.put("p_FechaPago", in.p_FechaPago);
		List<EventoCapital> eventoCapital = sql.query(con, SQL_SELECT_CONSULTAR_EVENTO_CAPITAL, params, EventoCapital.class);
		
		
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
		for (EventoCapital ev: eventoCapital){
			EventoCapital evIng = in.p_IdEventoCapital.contains(ev.IdEventoCapital)?ev:null;
			
			if (evIng == null){
				if ("EP".equalsIgnoreCase(ev.IdTipoEventoCapital) && in.p_IdTipoEventoCapital.contains(ev.IdTipoEventoCapital)){
					eventoCapitalEP.add(ev);
				}
				else if ("DA".equalsIgnoreCase(ev.IdTipoEventoCapital) && in.p_IdTipoEventoCapital.contains(ev.IdTipoEventoCapital)){
					eventoCapitalDA.add(ev);
				}
				else if ("DV".equalsIgnoreCase(ev.IdTipoEventoCapital) && in.p_IdTipoEventoCapital.contains(ev.IdTipoEventoCapital)){
					eventoCapitalDV.add(ev);
				}
				else if ("CN".equalsIgnoreCase(ev.IdTipoEventoCapital) && in.p_IdTipoEventoCapital.contains(ev.IdTipoEventoCapital)){
					eventoCapitalCN.add(ev);
				}
				else if ("CC".equalsIgnoreCase(ev.IdTipoEventoCapital) && in.p_IdTipoEventoCapital.contains(ev.IdTipoEventoCapital)){
					eventoCapitalCC.add(ev);
				}
				else if ("SL".equalsIgnoreCase(ev.IdTipoEventoCapital) && in.p_IdTipoEventoCapital.contains(ev.IdTipoEventoCapital)){
					eventoCapitalSL.add(ev);
				}
				else if ("DS".equalsIgnoreCase(ev.IdTipoEventoCapital) && in.p_IdTipoEventoCapital.contains(ev.IdTipoEventoCapital)){
					eventoCapitalDS.add(ev);
				}
				else if ("CA".equalsIgnoreCase(ev.IdTipoEventoCapital) && in.p_IdTipoEventoCapital.contains(ev.IdTipoEventoCapital)){
					eventoCapitalCA.add(ev);
				}
				else if ("SU".equalsIgnoreCase(ev.IdTipoEventoCapital) && in.p_IdTipoEventoCapital.contains(ev.IdTipoEventoCapital)){
					eventoCapitalSU.add(ev);
				}
				else if ("PB".equalsIgnoreCase(ev.IdTipoEventoCapital) && in.p_IdTipoEventoCapital.contains(ev.IdTipoEventoCapital)){
					eventoCapitalPB.add(ev);
				}
				else if ("DF".equalsIgnoreCase(ev.IdTipoEventoCapital) && in.p_IdTipoEventoCapital.contains(ev.IdTipoEventoCapital)){
					eventoCapitalDF.add(ev);
				}
				else if ("EL".equalsIgnoreCase(ev.IdTipoEventoCapital) && in.p_IdTipoEventoCapital.contains(ev.IdTipoEventoCapital)){
					eventoCapitalEL.add(ev);  // EMISION LIBERADA
				}
			}
			else {
				eventoCapitalIds.add(evIng);
			}
		}
		//Actulizo con los registro asociados a los Id ingresados en el proceso
		for (EventoCapital ev: eventoCapitalIds){
			switch (ev.IdTipoEventoCapital) {
				case "EP":     //EMISION PAGADA
					eventoCapitalEP.add(ev);
					break;
				case "DA":     //DIVIDENDO EN ACCIONES
					eventoCapitalDA.add(ev);
					break;
				case "CN":    //CAMBIO NEMO
					eventoCapitalCN.add(ev);
					break;
				case "DV":     //DIVIDENDO EN DINERO
					eventoCapitalDV.add(ev);
					break;     
				case "CC":     //CORTE CUPON
					eventoCapitalCC.add(ev);
					break;
				case "SL":     //SORTEO DE LETRAS
					eventoCapitalSL.add(ev);
					break;
				case "DS":     //DIVISION DE SOCIEDAD
					eventoCapitalDS.add(ev);
					break;
				case "CA":     //CANJE
					eventoCapitalCA.add(ev);
					break;
				case "SU":     //EMISION SUSCRIPCION
					eventoCapitalSU.add(ev);
					break;
				case "PB":     //PREPAGO DE BONO
					eventoCapitalPB.add(ev);
					break;
				case "DF":     //DIVIDENDO EN DINERO FFMM:
					eventoCapitalDF.add(ev);
					break;					
				case "EL":     //EMISION LIBERADA
					eventoCapitalEL.add(ev);
					break;
				default:
					break;
			}
		}
		
		//llamada a los servicios
		/*if (eventoCapitalDA.size() > 0){//DIVIDENDO EN ACCIONES
			CerrarEventoCapitalDA proc = new CerrarEventoCapitalDA();
			EntradaCerrarEventoCapitalDA entrada = new EntradaCerrarEventoCapitalDA();
			entrada.p_IdEmpresa = in.p_IdEmpresa; 
			entrada.p_IdUsuario = in.p_IdUsuario;
			entrada.p_IdModulo = "CUS";
			entrada.p_EventoCapital = eventoCapitalDA;
			SalidaCerrarEventoCapitalDA sal = proc.logica(con, entrada);
			out.P_OUT_MENSAJES = sal.P_OUT_MENSAJES;
		}
		if (eventoCapitalEL.size() > 0){//EMISION LIBERADA
			CerrarEventoCapitalEmisionLiberada proc = new CerrarEventoCapitalEmisionLiberada();
			CerrarEventoCapitalEmisionLiberadaEntrada entrada = new CerrarEventoCapitalEmisionLiberadaEntrada();
			entrada.p_IdEmpresa = in.p_IdEmpresa; 
			entrada.p_IdUsuario = in.p_IdUsuario;
			entrada.p_IdModulo = "CUS";
			entrada.p_EventoCapital = eventoCapitalEL;
			CerrarEventoCapitalEmisionLiberadaSalida sal = proc.logica(con, entrada);
			out.P_OUT_MENSAJES = sal.P_OUT_MENSAJES;			
		}
		if (eventoCapitalEP.size() > 0){//Emision Pagada
			CerrarEventoCapitalEmisionPago proc = new CerrarEventoCapitalEmisionPago();
			CerrarEventoCapitalEmisionPagoEntrada entrada = new CerrarEventoCapitalEmisionPagoEntrada();
			entrada.p_IdEmpresa = in.p_IdEmpresa; 
			entrada.p_IdUsuario = in.p_IdUsuario;
			entrada.p_IdModulo = "CUS";
			entrada.p_EventoCapital = eventoCapitalEP;
			CerrarEventoCapitalEmisionPagoSalida sal = proc.logica(con, entrada);
			out.P_OUT_MENSAJES = sal.P_OUT_MENSAJES;			
		}
		if (eventoCapitalDV.size() > 0){//DIVIDENDO EN DINERO
			CerrarEventoCapitalDividendoEnDinero proc = new CerrarEventoCapitalDividendoEnDinero();
			CerrarEventoCapitalDividendoEnDineroEntrada entrada = new CerrarEventoCapitalDividendoEnDineroEntrada();
			entrada.p_IdEmpresa = in.p_IdEmpresa; 
			entrada.p_IdUsuario = in.p_IdUsuario;
			entrada.p_IdModulo = "CUS";
			entrada.p_EventoCapital = eventoCapitalDV;
			CerrarEventoCapitalDividendoEnDineroSalida sal = proc.logica(con, entrada);
			out.P_OUT_MENSAJES = sal.P_OUT_MENSAJES;
		}
		if (eventoCapitalCN.size() > 0){ // CAMBIO DE NEMO
			CerrarEventoCapitalCambioDeNemos proc = new CerrarEventoCapitalCambioDeNemos();
			CerrarEventoCapitalCambioDeNemosEntrada entrada = new CerrarEventoCapitalCambioDeNemosEntrada();
			entrada.p_IdEmpresa = in.p_IdEmpresa; 
			entrada.p_IdUsuario = in.p_IdUsuario;
			entrada.p_IdModulo = "CUS";
			entrada.p_EventoCapital = eventoCapitalCN;
			CerrarEventoCapitalCambioDeNemosSalida sal = proc.logica(con, entrada);
			out.P_OUT_MENSAJES = sal.P_OUT_MENSAJES;
			out.P_OUT_SECUENCIA = sal.P_OUT_SECUENCIA;
		}*/
		if (eventoCapitalSL.size() > 0){ // Sorteo de letras
			CerrarEventoCapitalSorteoLetras proc = new CerrarEventoCapitalSorteoLetras();
			CerrarEventoCapitalSorteoLetrasEntrada entrada = new CerrarEventoCapitalSorteoLetrasEntrada();
			entrada.p_IdEmpresa = in.p_IdEmpresa; 
			entrada.p_IdUsuario = in.p_IdUsuario;
			entrada.p_IdModulo = "CUS";
			entrada.p_UsaReglasNegocio = "S";
			entrada.p_EventoCapital = eventoCapitalSL;
			CerrarEventoCapitalSorteoLetrasSalida sal = proc.logica(con, entrada);
			out.P_OUT_MENSAJES = sal.P_OUT_MENSAJES;
			out.P_OUT_SECUENCIA = sal.P_OUT_SECUENCIA;
		}
		/*
		if (eventoCapitalCA.size() > 0){ // CANJE
			CerrarEventoCapitalCanje proc = new CerrarEventoCapitalCanje();
			CerrarEventoCapitalCanjeEntrada entrada = new CerrarEventoCapitalCanjeEntrada();
			entrada.p_IdEmpresa = in.p_IdEmpresa; 
			entrada.p_IdUsuario = in.p_IdUsuario;
			entrada.p_IdModulo = "CUS";
			entrada.p_EventoCapital = eventoCapitalCA;
			CerrarEventoCapitalCanjeSalida sal = proc.logica(con, entrada);
			out.P_OUT_MENSAJES = sal.P_OUT_MENSAJES;
		}*/
		/*
		if (eventoCapitalEP.size() > 0)
		if (eventoCapitalCC.size() > 0)
		if (eventoCapitalSL.size() > 0)
		if (eventoCapitalDS.size() > 0)
		if (eventoCapitalCA.size() > 0)
		if (eventoCapitalSU.size() > 0)
		if (eventoCapitalPB.size() > 0)
		if (eventoCapitalDF.size() > 0)
		
		if (out.P_OUT_SECUENCIA.size() == 0){
			mapMsg.clear();
			out.P_OUT_MENSAJES.add(new MensajeSalida(Id, MensajeSalida.MENSAJE_INFORMACION, "UI_EVENTO_CAPITAL_NO_EXISTE", mapMsg, con, in.p_IdEmpresa));
			Id++;
		}*/
		log.write("FIN::CUS.CerrarEventoCapital");
		return out;
		
	}
	/*
	 * switch (ev.IdTipoEventoCapital) {
			case "EP":     //EMISION PAGADA   
				break;
			case "DA":     //DIVIDENDO EN ACCIONES
				break;
			case "CN":     //CAMBIO NEMO VR
				break;
			case "DV":     //DIVIDENDO EN DINERO
				break;     
			case "CC":     //CORTE CUPON
				break;
			case "SL":     //SORTEO DE LETRAS
				break;
			case "DS":     //DIVISION DE SOCIEDAD
				break;
			case "CA":     //CANJE
				break;
			case "SU":     //EMISION SUSCRIPCION
				break;
			case "PB":     //PREPAGO DE BONO
				break;
			case "DF":     //DIVIDENDO EN DINERO FFMM:
				break;
			default:
				break;
			}
	 */
	
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		Gson gson = Utils.getGsonBuilder();
		CerrarEventoCapitalEntrada jin = gson.fromJson(in.toString(), CerrarEventoCapitalEntrada.class);
		CerrarEventoCapitalSalida out = logica(conn, jin);
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
			conn = PlataformaUtils.getUATConnection();
			//datos entrada364858
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":50,\"p_IdModulo\":\"CUS\",\"p_UsaReglasNegocio\":\"S\",\"p_FechaPago\":[\"2018-09-28\"],\"p_IdEventoCapital\":[936],\"p_IdTipoEventoCapital\":[]}";
			CerrarEventoCapitalEntrada objIn = gson.fromJson(json,CerrarEventoCapitalEntrada.class);
			//mientras pruebo hare el rollback
			CerrarEventoCapital demo = new CerrarEventoCapital();
			CerrarEventoCapitalSalida ObjOut=demo.logica(conn, objIn);
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
