package cl.bcs.custodia.evc;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.custodia.evc.CrearEventoCapitalCliente.EventoCL;
import cl.bcs.custodia.evc.CrearEventoCapitalCliente.EventoCapitalCliente_IN;
import cl.bcs.custodia.evc.CrearEventoCapitalCliente.EventoCapitalCliente_OUT;
import cl.bcs.custodia.evc.ProcesarEventoCapital.SalidaClientesConDerecho;
import cl.bcs.custodia.pojo.EventoCapital;
import cl.bcs.custodia.pojo.EventoCapitalCliente;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ProcesarEventoCapitalSorteoLetras implements JavaCall{
	private final Log log = new Log("CUS.ProcesarEventoCapitalSorteoLetras");
	
	private final String SQL_SELECT_INSTRUMENTO 				= loadResourceToString("cl/bcs/custodia/sql/ProcesarEventoCapitalSorteoLetras_SelectInstrumento.sql");
	private final String SQL_UPDATE_EVENTO_CAPITAL 				= loadResourceToString("cl/bcs/custodia/sql/ProcesarEventoCapitalSorteoLetras_UpdateEventoCapital.sql");
	private final String SQL_INSERT_EVENTO_CAPITAL_CLIENTE		= loadResourceToString("cl/bcs/custodia/sql/ProcesarEventoCapitalSorteoLetras_InsertEventoCapitalCliente.sql"); // para insertar solo la contraparte
	
	
	public static class ProcesarEventoCapitalSorteoLetrasEntrada {
		public Long							p_IdEmpresa; 
		public Long							p_IdUsuario;
		public String						p_IdModulo;
		public String						p_Origen; //corresponde desde donde es ejecutado el evento MAN: UI, AUT: Job Pauta
		public String						p_UsaReglasNegocio;
		
		public List<EventoCapital>			p_EventoCapital; //eventos capital a procesar
				
		public ProcesarEventoCapitalSorteoLetrasEntrada (){
			this.p_EventoCapital 		= new ArrayList<EventoCapital>();
		}
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	public static class ProcesarEventoCapitalSorteoLetrasSalida {
		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;
		public List<Long>			P_OUT_SECUENCIA;
		
		public ProcesarEventoCapitalSorteoLetrasSalida() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_SECUENCIA = new ArrayList<Long>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class Instrumento {
		public Long 	IdInstrumento;
		public String 	Nemotecnico;
		public String	IdClasificacionTipoInstrumento;
		public String	IdTipoInstrumento;
		public BigDecimal 	CorteMinimo;
		public String 		IdEmisor;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class EventoCapitalUpd {
		public Long 	p_IdEmpresa;
		public Long 	p_IdEventoCapital;
		public String 	p_IdTipoEventoCapital;
		public Long 	p_Secuencia;
		public String	p_IdEstado;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	/*
	public static class TotalPorEvento {
		public Long 			IdEventoCapital;
		public BigDecimal		CantidadTotalDerecho;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}*/
	
	
	//Ajusta la cantidad prorrateada segun el corte minimo(ej. 141 > 150)
	public BigDecimal ajustarCantidadProrrateada(BigDecimal cantidadProrrateada, BigDecimal corteMinimo, BigDecimal limite) throws Exception{
		int countSuperior = 0, countInferior = 0;
		
		BigDecimal cantidadProrrateadaSuperior = cantidadProrrateada.setScale(0, BigDecimal.ROUND_DOWN);
		BigDecimal cantidadProrrateadaInferior = cantidadProrrateada.setScale(0, BigDecimal.ROUND_DOWN);
		BigDecimal corteMinimoAju = corteMinimo.setScale(0, BigDecimal.ROUND_DOWN);
				
		while (cantidadProrrateadaSuperior.remainder(corteMinimoAju).compareTo(BigDecimal.ZERO) != 0)
		{
			cantidadProrrateadaSuperior = cantidadProrrateadaSuperior.add(BigDecimal.ONE);
			countSuperior++;
		}
		
		while (cantidadProrrateadaInferior.remainder(corteMinimoAju).compareTo(BigDecimal.ZERO) != 0)
		{
			cantidadProrrateadaInferior = cantidadProrrateadaInferior.subtract(BigDecimal.ONE);
			countInferior++;
		}
		
		if (countInferior < countSuperior && cantidadProrrateadaInferior.compareTo(corteMinimo) >= 0 || cantidadProrrateadaSuperior.compareTo(limite) > 0)
			return cantidadProrrateadaInferior;
		else
			return cantidadProrrateadaSuperior;
		
	} 
	public ProcesarEventoCapitalSorteoLetrasSalida logica(Connection con, ProcesarEventoCapitalSorteoLetrasEntrada in) throws Exception {
		log.write("INICIO::CUS.ProcesarEventoCapitalSorteoLetras");
		ProcesarEventoCapitalSorteoLetrasSalida out = new ProcesarEventoCapitalSorteoLetrasSalida();
		HashMap<String,String> mapMsg = new HashMap<>();
		SQL sql = new SQL();
		Long Id = 0L;
		
		List<EventoCapital> p_EventoCapital = new ArrayList<>();
		String v_Estado = "PRO";//que estado deberia ser
		// Consulta empresa
		ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
		InformacionEmpresa empresa = consultarEmpresaService.logica(con, in.p_IdEmpresa);
		
		//consulto instrumentos
		HashMap<String,Object> params = new HashMap<String,Object>();
		List<Long> idsInstrumento = new ArrayList<>();
		for (EventoCapital ev: in.p_EventoCapital){
			idsInstrumento.add(ev.IdInstrumento);
		}
		params.put("p_IdInstrumento", idsInstrumento);
		List<Instrumento> inst = sql.query(con, SQL_SELECT_INSTRUMENTO, params, Instrumento.class);
		Map<Long, Object> instMap = Utils.List2Hash(inst, "IdInstrumento");
		Map<String, Object> instNemoMap = Utils.List2Hash(inst, "Nemotecnico");
				
		for (EventoCapital ev: in.p_EventoCapital){
			Instrumento ins = (Instrumento)instNemoMap.get(ev.Nemo);
			if (ins != null && ins.IdTipoInstrumento.equalsIgnoreCase("LTH")){
				p_EventoCapital.add(ev);
			}
		}
		//******************************************************************************************************************
		//												VALIDACIONES INICIALES
		//******************************************************************************************************************			
		{
			RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();					
			RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(con, in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo);
			out.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
			

			Set<Long> instrumentosEncontrados = inst.stream().map(i -> i.IdInstrumento).collect(Collectors.toSet());
			
			// Validar si algún IdInstrumento del evento no está en el set
			boolean faltaInstrumento = in.p_EventoCapital.stream().anyMatch(ev -> !instrumentosEncontrados.contains(ev.IdInstrumento));
			
			if (faltaInstrumento){
				mapMsg.clear();
				//mapMsg.put("Nemotecnico", ev.Nemo);
				out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"UI_INSTRUMENTO_NO_EXISTE",mapMsg,con,in.p_IdEmpresa));
				Id++;
			}
			if (p_EventoCapital.size() == 0){
				mapMsg.clear();
				//mapMsg.put("Nemotecnico", ev.Nemo);
				out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"UI_INSTRUMENTO_NO_VALIDO",mapMsg,con,in.p_IdEmpresa));
				Id++;
			}
			if (MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES) > 0){
				return out;
			}
		}
		//******************************************************************************************************************
		//													LOGICA
		//******************************************************************************************************************	
		{
			List<EventoCapitalUpd> updateEventos = new ArrayList<>();
			List<EventoCapitalCliente> registrosContraparte = new ArrayList<>();
			ProcesarEventoCapital eventoCapital = new ProcesarEventoCapital();
			CrearEventoCapitalCliente eventoCapitalCliente = new CrearEventoCapitalCliente(); 
			EventoCapitalCliente_OUT eventoCapitalClienteSalida = new EventoCapitalCliente_OUT();
			EventoCapitalCliente_IN evCliEntrada = new EventoCapitalCliente_IN();
			evCliEntrada.p_IdEmpresa = in.p_IdEmpresa;
			evCliEntrada.p_IdUsuario = in.p_IdUsuario;
			evCliEntrada.p_IdModulo = in.p_IdModulo;
			evCliEntrada.p_UsaReglasNegocio = in.p_UsaReglasNegocio;
			
			for (EventoCapital ev: p_EventoCapital){
				//List<Long> socioNegocio = new ArrayList<>();
				SalidaClientesConDerecho CCD = eventoCapital.getClientesConDerecho(con, ev, in.p_IdModulo, empresa);
				out.P_OUT_MENSAJES.addAll(CCD.P_OUT_MENSAJES);
				if (CCD.P_OUT_CLIENTES.size() == 0 ){
					mapMsg.clear();
					mapMsg.put("Nemotecnico", ev.Nemo);
					out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_INFORMACION,"NO_EXISTEN_CARTERAS_PARA_NEMOTECNICO",mapMsg,con,in.p_IdEmpresa));
					Id++;
					break;
				}
				if (MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES) > 0){
					return out;
				}
				//calculo del prorrateo
				BigDecimal CantidadTotalDerecho = BigDecimal.ZERO;
				String operacionesVigentes = "N";
				for (EventoCapitalCliente cli: CCD.P_OUT_CLIENTES){
					if (cli.TipoCartera.equalsIgnoreCase("CUS") || cli.TipoCartera.equalsIgnoreCase("PAC")){
						CantidadTotalDerecho = CantidadTotalDerecho.add(cli.CantidadDerecho);
						operacionesVigentes = cli.TipoCartera.equalsIgnoreCase("GAR") || cli.TipoCartera.equalsIgnoreCase("PAC") ? "S" : operacionesVigentes; 
						//socioNegocio.add(cli.IdSocioNegocio);
					}
				}
				
				CCD.P_OUT_CLIENTES.stream().filter(data -> "GAR".equals(data.TipoCartera)).forEach(cli -> {
					evCliEntrada.p_EventoCapital.add(crearEventoCapitalCliente(ev, cli, BigDecimal.ZERO, BigDecimal.ZERO, "S"));
				});
				
				BigDecimal cantidadTotal = BigDecimal.ZERO;
				BigDecimal total = BigDecimal.ZERO;
				//busco los cliente
				BigDecimal totalSorteo = ev.SorteadoSorteoLetras;
				CCD.P_OUT_CLIENTES.sort((c1,c2)->c2.CantidadDerecho.compareTo(c1.CantidadDerecho));
				for (EventoCapitalCliente cli: CCD.P_OUT_CLIENTES){
					if ((cli.TipoCartera.equalsIgnoreCase("CUS") || cli.TipoCartera.equalsIgnoreCase("PAC") ) && totalSorteo.compareTo(BigDecimal.ZERO) > 0){
						BigDecimal cantidadProrrateada = BigDecimal.ZERO;
						BigDecimal totalProrrateada = BigDecimal.ZERO;
						Instrumento ins = (Instrumento) instMap.get(ev.IdInstrumento);
						cantidadProrrateada = (cli.CantidadDerecho.divide(CantidadTotalDerecho,4,RoundingMode.DOWN).multiply(ev.SorteadoSorteoLetras).divide(ins.CorteMinimo)).multiply(ins.CorteMinimo);
						cantidadProrrateada = ajustarCantidadProrrateada(cantidadProrrateada, ins.CorteMinimo, totalSorteo);  // Ajusta cantidad prorrateada segun corte minimo(la cantidad debe ser multiplo de corte minimo)  
						cantidadTotal = cantidadTotal.add(cantidadProrrateada);
						//ROUND(((FT."CANTIDADPRORRATEADA" / CorteMinimo."CorteMinimo") * (SorteoLetras."APagarSorteoLetras" / (SorteoLetras."SorteadoSorteoLetras" / CorteMinimo."CorteMinimo")) * (SorteoLetras."APagarPesosSorteoLetras" / SorteoLetras."APagarSorteoLetras")),0),	--	Total
						totalProrrateada = (cantidadProrrateada.divide(ins.CorteMinimo,4,RoundingMode.DOWN)).multiply((ev.APagarSorteoLetras.divide((ev.SorteadoSorteoLetras.divide(ins.CorteMinimo,4,RoundingMode.DOWN)),4,RoundingMode.DOWN)).multiply((ev.APagarPesosSorteoLetras.divide(ev.APagarSorteoLetras,4,RoundingMode.DOWN))));
						totalSorteo = totalSorteo.subtract(cantidadProrrateada);
						total = total.add(totalProrrateada);
						
						if(cantidadProrrateada.equals(BigDecimal.ZERO))
						{
							continue;
						}						
						
						evCliEntrada.p_EventoCapital.add(crearEventoCapitalCliente(ev, cli, cantidadProrrateada, totalProrrateada,operacionesVigentes));
						
					}
				}
				List<Long> secuenciaEventoCapital= Utils.getSecuencia(con, "BSCL.Custodia.BD.Secuencias::EventoCapitalCliente", new Long(CCD.P_OUT_CONTRAPARTES.size()));
				for (EventoCapitalCliente cli: CCD.P_OUT_CONTRAPARTES){
					if("NOM".equals(cli.TipoCartera)) continue;
					
					cli.CantidadDerecho			= BigDecimal.ZERO;//cantidadTotal.multiply(new BigDecimal(-1));
					cli.CantidadNueva			= "GAR".equals(cli.TipoCartera) ? BigDecimal.ZERO : cantidadTotal.multiply(new BigDecimal(-1));
					cli.Total					= "GAR".equals(cli.TipoCartera) ? BigDecimal.ZERO : total.multiply(new BigDecimal(-1));
					cli.IdEventoCapitalCliente	= secuenciaEventoCapital.get(CCD.P_OUT_CONTRAPARTES.indexOf(cli));
					cli.IdEventoCapital			= ev.IdEventoCapital;
					cli.IdTipoEventoCapital 				= ev.IdTipoEventoCapital;
					cli.IdClasificacionTipoInstrumento	= ev.IdClasificacionTipoInstrumento;
					cli.IdTipoInstrumento					= ev.IdTipoInstrumento;  
					cli.IdInstrumento						= ev.IdInstrumento;
					cli.Nemo								= ev.Nemo;
				    cli.FechaPago							= ev.FechaPago;
				    cli.NumeroDividendo					= ev.NumeroDividendo;
				    cli.Secuencia							= ev.Secuencia;
				    cli.IdMoneda							= ev.IdMoneda;
				    cli.MontoCapital						= BigDecimal.ZERO;
				    cli.MontoInteres						= BigDecimal.ZERO;
				    cli.TipoCartera						= "CUS";
				    cli.IdEstado							= "VIG";//VIG o PRO
				    
					registrosContraparte.add(cli);
				}
				
				EventoCapitalUpd evCapUpd = new EventoCapitalUpd();
				evCapUpd.p_IdEmpresa = in.p_IdEmpresa;
				evCapUpd.p_IdEventoCapital = ev.IdEventoCapital;
				evCapUpd.p_IdTipoEventoCapital = ev.IdTipoEventoCapital;
				evCapUpd.p_Secuencia = ev.Secuencia;
				evCapUpd.p_IdEstado = v_Estado;
				updateEventos.add(evCapUpd);
			}
			
			if (evCliEntrada.p_EventoCapital.size() > 0){
				
				List<EventoCL> listEvento = new ArrayList<EventoCL>();
				for(EventoCL evo :evCliEntrada.p_EventoCapital) {
					if(listEvento.isEmpty()) {
						listEvento.add(evo);	
					}else 
						for(EventoCL _evo : listEvento) {
							if(evo.p_IdPortafolio.equals(_evo.p_IdPortafolio) && evo.p_Nemo.equals(_evo.p_Nemo) && evo.p_TipoCartera.equals(_evo.p_TipoCartera)) {
								_evo.p_CantidadDerecho = _evo.p_CantidadDerecho.add(evo.p_CantidadDerecho);
								_evo.p_CantidadNueva = _evo.p_CantidadNueva.add(evo.p_CantidadDerecho);
								_evo.p_Total = _evo.p_Total.add(evo.p_Total);
							}
							else{
								listEvento.add(evo);	
							}
							break;
						}
				}
				
				evCliEntrada.p_EventoCapital = listEvento;
				eventoCapitalClienteSalida = eventoCapitalCliente.logica(con, evCliEntrada);
				out.P_OUT_MENSAJES.addAll(eventoCapitalClienteSalida.P_OUT_MENSAJES);
				out.P_OUT_SECUENCIA.addAll(eventoCapitalClienteSalida.P_OUT_SECUENCIA);
				
				sql.batch(con, SQL_INSERT_EVENTO_CAPITAL_CLIENTE, registrosContraparte , EventoCapitalCliente.class);
				log.write("ProcesarEventoCapitalSorteoLetras - Fin Insert Eventos Capital Cliente");
				
				if (MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES) > 0){
					return out;
				}
				sql.batch(con, SQL_UPDATE_EVENTO_CAPITAL, updateEventos, EventoCapitalUpd.class);
			}
			else {
				mapMsg.clear();
				//mapMsg.put("Nemotecnico", ev.Nemo);
				out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_INFORMACION,"NO_EXISTEN_CLIENTES",mapMsg,con,in.p_IdEmpresa));
				Id++;
			}
		}
			
		log.write("FIN::CUS.ProcesarEventoCapitalSorteoLetras");
		return out;
		
	}
	
	private EventoCL crearEventoCapitalCliente(EventoCapital ev, EventoCapitalCliente cli, BigDecimal cantidadProrrateada, BigDecimal totalProrrateada, String operacionesVigentes) {
		EventoCL evC = new EventoCL();
		evC.p_IdTipoEventoCapital 				= ev.IdTipoEventoCapital;
	    evC.p_IdClasificacionTipoInstrumento	= ev.IdClasificacionTipoInstrumento;
	    evC.p_IdTipoInstrumento					= ev.IdTipoInstrumento;  
	    evC.p_IdInstrumento						= ev.IdInstrumento;
	    evC.p_Nemo								= ev.Nemo;
	    evC.p_FechaPago							= Fecha.stringToSqlDate(ev.FechaPago);
	    evC.p_NumeroDividendo					= ev.NumeroDividendo;
	    evC.p_Secuencia							= ev.Secuencia;
	    evC.p_ValorUnitario						= ev.Valor;		
	    evC.p_Factor							= BigDecimal.ONE;//ev.Factor;
	    evC.p_Descripcion						= ev.Descripcion;
	    evC.p_IdCliente							= cli.IdCliente;
	    evC.p_IdPortafolio						= cli.IdPortafolio;
	    evC.p_IdMoneda							= ev.IdMonedaLiquidacion;
	    evC.p_CantidadDerecho					= cli.CantidadDerecho;
	    evC.p_CantidadNueva						= cantidadProrrateada;
	    evC.p_Total								= totalProrrateada.setScale(0, RoundingMode.HALF_UP);
	    evC.p_MontoCapital						= BigDecimal.ZERO;
	    evC.p_MontoInteres						= BigDecimal.ZERO;
	    evC.p_TipoCartera						= cli.TipoCartera;
	    evC.p_IdBoveda							= cli.IdBoveda;
	    evC.p_IdOrigen							= cli.Origen;
	    evC.p_IdBolsa							= cli.IdBolsa;
	    evC.p_IdEstado							= "VIG";//VIG o PRO
	    evC.p_TipoSocioNegocio					= cli.TipoSocioNegocio;
	    evC.p_IdTipoOperacion					= "SORLET";
		evC.p_OperacionesVigentes				= operacionesVigentes;
		return evC;
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		Gson gson = Utils.getGsonBuilder();
		ProcesarEventoCapitalSorteoLetrasEntrada jin = gson.fromJson(in.toString(), ProcesarEventoCapitalSorteoLetrasEntrada.class);
		ProcesarEventoCapitalSorteoLetrasSalida out = logica(conn, jin);
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
			conn = PlataformaUtils.getDESAConnection();
			//datos entrada364858
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":25,\"p_IdModulo\":\"CUS\",\"p_Origen\":\"UI\",\"p_UsaReglasNegocio\":\"S\",\"p_IdEventoCapital\":[366283]}";
			ProcesarEventoCapitalSorteoLetrasEntrada objIn = gson.fromJson(json,ProcesarEventoCapitalSorteoLetrasEntrada.class);
			//mientras pruebo hare el rollback
			ProcesarEventoCapitalSorteoLetras demo = new ProcesarEventoCapitalSorteoLetras();
			
			ProcesarEventoCapitalSorteoLetrasSalida ObjOut=demo.logica(conn, objIn);
				
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
