package cl.bcs.custodia.evc;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.custodia.evc.CrearEventoCapitalCliente.EventoExiste;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ModificarEventoCapitalCliente implements JavaCall{
	private final Log log = new Log("CUS.ModificarEventoCapitalCliente");
	
	private final String SQL_SELECT_EVENTO_CAPITAL 			= "SELECT \"IdEventoCapital\", \"Factor\", \"IdEstado\",\"TasaEfectiva\",\"IdMoneda\" FROM \"BSCL.Custodia.BD::CUS.EventoCapital\" WHERE \"IdEmpresa\" = :p_IdEmpresa AND \"IdEventoCapital\" IN (:p_IdEventoCapital)";
	private final String SQL_SELECT_EVENTO_CAPITAL_CLIENTE = loadResourceToString("cl/bcs/custodia/sql/ModificarEventoCapitalCliente_SelectEventoCapitalCliente.sql");
	private final String SQL_SELECT_CONFIGURACION  = loadResourceToString("cl/bcs/custodia/sql/ConsultarConfiguracion.sql");
	private final String SQL_UPDATE_EVENTO_CAPITAL = loadResourceToString("cl/bcs/custodia/sql/ModificarEventoCapitalCliente_UpdateEventoCapital.sql");
	private final String SQL_VALIDAR_IMPUESTO_GASTO_DV = loadResourceToString("cl/bcs/custodia/sql/ModificarEventoCapitalCliente_ValidarImpuestoGastoDV.sql");
	private final String SQL_SELECT_CONTRAPARTE = loadResourceToString("cl/bcs/custodia/sql/ModificarEventoCapitalCliente_SelectContraparte.sql");
	private final String SQL_UPDATE_CONTRAPARTE = loadResourceToString("cl/bcs/custodia/sql/ModificarEventoCapitalCliente_UpdateContraparte.sql");
	
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
		public String 		p_IdTipoEventoCapital;
		public Long 		p_IdEventoCapitalCliente;//				BIGINT     		default 0,
		public BigDecimal	p_CantidadDerecho;//				DECIMAL(26,8)	default 0,
		public BigDecimal 	p_CantidadNueva;//						BIGINT     		default 0,
		public BigDecimal 	p_MontoCapital;//					BIGINT     		default 0,
		public BigDecimal 	p_MontoInteres;//					DECIMAL(26,8)	default 0,	
		public String 		p_TipoCartera;//					NVARCHAR(3) 	default '',
		public String 		p_IdBoveda;//					NVARCHAR(3) 	default '',
		public BigDecimal 	p_Total;//						DECIMAL(26,8)	default 0,	
		public BigDecimal	p_FactorOtraMoneda;

		public BigDecimal 	p_CantidadDerechoArchivo;//				BIGINT	DEFAULT 0,
		public BigDecimal 	p_DiferenciaCantidadDerechoArchivo;//BIGINT	DEFAULT 0,
		public BigDecimal 	p_CantidadNuevaArchivo;//		BIGINT			DEFAULT 0,
		public BigDecimal 	p_DiferenciaCantidadNuevaArchivo;//		BIGINT	DEFAULT 0,
		public BigDecimal 	p_MontoBeneficioArchivo;//			BIGINT	DEFAULT 0,
		public BigDecimal 	p_DiferenciaMontoBeneficioArchivo;//	BIGINT	DEFAULT 0,
		public BigDecimal 	p_MontoInteresArchivo;//				BIGINT	DEFAULT 0,
		public BigDecimal 	p_DiferenciaMontoInteresArchivo;//		BIGINT	DEFAULT 0,
		public BigDecimal 	p_MontoCapitalArchivo;//			BIGINT	DEFAULT 0,
		public BigDecimal 	p_DiferenciaMontoCapitalArchivo;//
		public String		p_FechaHoraUltimaActualizacion;
		public String 	p_TipoSocioNegocio;     

		public BigDecimal   p_Precio;
		
		public String p_ISFUT;
		public String p_RetencionISFUT;
		public BigDecimal p_MontoRetencionISFUT;
		
		public BigDecimal p_MontoGastoTransferencia;
		public BigDecimal p_MontoImpuestoExtranjero;
		
		public String 	p_IdMoneda; 

		//--------------------
		//public BigDecimal	p_Valor;
		//public Long			p_IdUsuario;
		//public String		p_Origen;
		
		
	    public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class EventoExiste {
		public Long			IdEventoCapitalCliente; 		
		public Long			IdEventoCapital;
		public Long			NumeroDividendo;
		public BigDecimal	Factor;
		public String		Estado;
		public BigDecimal	CantidadDerecho;
		public BigDecimal	CantidadNueva;
		public BigDecimal	Total;
		public BigDecimal	MontoCapital;
		public BigDecimal	MontoInteres;
		public String		TipoSocioNegocio;
		public BigDecimal	TasaEfectiva;
		public String		IdMoneda;
		public String		IdBoveda;
		public String		TipoCartera;
		public String		Nemo;
		public String		NombreCliente;
		public Long		    IdCliente;
		public BigDecimal	MontoImpuestoExtranjero;
		public BigDecimal	MontoGastoTransferencia;
			    
	    public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class Configuracion {
		public Long 		IdEmpresa;
		public String 		CodigoParametro;
		public String 		Descripcion;
		public String 		Valor;
		
	    public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ValidacionImpuestoGastoDV {
		public Long			IdEventoCapitalCliente;
		public String		AplicaGasto;
		public String		AplicaImpuesto;
	    public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class MontosYTotal {
		public Long			IdEventoCapitalClienteCli;
		public Long 		IdEventoCapitalClienteCnt;
		public BigDecimal	Total;
		public BigDecimal	MontoImpuestoExtranjero;
		public BigDecimal	MontoGastoTransferencia;
	    public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public EventoCapital_OUT logica (Connection conn, EventoCapital_IN in) throws Exception {
		SQL _sql = new SQL();
		Long Id = 0L;
		HashMap<String,String> mapMsg = new HashMap<String,String>();
		log.write("INICIO::CUS.ModificarEventoCapitalCliente");
		EventoCapital_OUT out = new EventoCapital_OUT();
		HashMap<String, Object> params = new HashMap<String, Object>();
		List<Object> paramsList = new ArrayList<Object>();
		List<Configuracion> configuracion = new ArrayList<Configuracion>();
		HashMap<String, Object> configuracionMap = new HashMap<String, Object>();
		List<Long> idEventoCapitalCliente = new ArrayList<Long>();
		List<Long> idEventoCapital = new ArrayList<Long>();
		List<EventoExiste> eventoClienteExistente = new ArrayList<EventoExiste>();
		HashMap<Long, Object> eventoClienteExistenteMap = new HashMap<Long, Object>();
		List<EventoExiste> eventoExistente = new ArrayList<EventoExiste>();
		HashMap<Long, Object> eventoExistenteMap = new HashMap<Long, Object>();
		AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
		entradaAgregarLog.p_IdTrabajo = 1L;
		
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		String fechaHoraSistema = FechaDB.ObtenerFechaHoraSistemaLocal(conn,in.p_IdEmpresa);
		//consulto parametro configuracion custodia
		params = new HashMap<>();
		paramsList = new ArrayList<>();
		paramsList.add("CUS.Rango");
		params.put("p_IdEmpresa", in.p_IdEmpresa);
		params.put("p_CodigoParametro", paramsList);
		configuracion = _sql.query(conn, SQL_SELECT_CONFIGURACION, params, Configuracion.class);
		configuracionMap = Utils.List2Hash(configuracion, "CodigoParametro");
		//-----
		
		for (Evento ev: in.p_EventoCapital){
			idEventoCapitalCliente.add(ev.p_IdEventoCapitalCliente);
		}
		
		if (idEventoCapitalCliente.size() > 0){
			params = new HashMap<>();
			params.put("p_IdEventoCapitalCliente", idEventoCapitalCliente);
			eventoClienteExistente = _sql.query(conn, SQL_SELECT_EVENTO_CAPITAL_CLIENTE, params, EventoExiste.class);
			eventoClienteExistenteMap = Utils.List2Hash(eventoClienteExistente, "IdEventoCapitalCliente");
		}
		for (EventoExiste ev: eventoClienteExistente){
			idEventoCapital.add(ev.IdEventoCapital);
		}
		if (idEventoCapital.size() > 0){
			params = new HashMap<>();
			params.put("p_IdEmpresa", in.p_IdEmpresa);
			params.put("p_IdEventoCapital", idEventoCapital);
			eventoExistente = _sql.query(conn, SQL_SELECT_EVENTO_CAPITAL, params, EventoExiste.class);
			eventoExistenteMap = Utils.List2Hash(eventoExistente, "IdEventoCapital");
		}
		List<Long> listaIdEvCapCli = new ArrayList<Long>();
		for(Evento e: in.p_EventoCapital){
			if(!listaIdEvCapCli.contains(e.p_IdEventoCapitalCliente)) 
				listaIdEvCapCli.add(e.p_IdEventoCapitalCliente);
		}
		HashMap<String,Object> valGastoImpuestoParams = new HashMap<String,Object>();
		valGastoImpuestoParams.put("IdsEventoCapitalCliente", listaIdEvCapCli);
		List<ValidacionImpuestoGastoDV> validacionImpuestoGasto = _sql.query(conn, SQL_VALIDAR_IMPUESTO_GASTO_DV, valGastoImpuestoParams, ValidacionImpuestoGastoDV.class);
		HashMap<Long, ValidacionImpuestoGastoDV> valGastoImpuestoMap = new HashMap<Long, ValidacionImpuestoGastoDV>();
		for(ValidacionImpuestoGastoDV val :validacionImpuestoGasto){
			valGastoImpuestoMap.put(val.IdEventoCapitalCliente, val);
		}
		//::Validaciones
		{
			if (idEventoCapitalCliente.size() == 0){
				// No Viene Evento
				mapMsg.clear();
				out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"SIN_DATOS_A_PROCESAR",mapMsg,conn,in.p_IdEmpresa));
				Id++;
			}
			for (Evento ev : in.p_EventoCapital){
				EventoExiste evCliEx = (EventoExiste) eventoClienteExistenteMap.get(ev.p_IdEventoCapitalCliente);
				EventoExiste evEx = (EventoExiste) eventoExistenteMap.get(evCliEx.IdEventoCapital);
				
				if(ev.p_MontoImpuestoExtranjero.compareTo(new BigDecimal(0)) > 0 && !ev.p_IdTipoEventoCapital.equalsIgnoreCase("DV")) {
					mapMsg.clear();
					out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR, "UI_EV_CAPITAL_CLIENTE_SIN_IMPUESTO",mapMsg,conn,in.p_IdEmpresa));
					Id++;
				}
				
				if(ev.p_MontoGastoTransferencia.compareTo(new BigDecimal(0)) > 0 && !ev.p_IdTipoEventoCapital.equalsIgnoreCase("DV")) {
					mapMsg.clear();
					out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR, "UI_EV_CAPITAL_CLIENTE_SIN_GASTO",mapMsg,conn,in.p_IdEmpresa));
					Id++;
				}
				
				if(ev.p_MontoImpuestoExtranjero.compareTo(new BigDecimal(0)) > 0 && valGastoImpuestoMap.get(ev.p_IdEventoCapitalCliente) != null 
				  && valGastoImpuestoMap.get(ev.p_IdEventoCapitalCliente).AplicaImpuesto.equalsIgnoreCase("N")) {
					mapMsg.clear();
					out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR, "UI_EV_CAPITAL_CLIENTE_EXENTO_IMPUESTO",mapMsg,conn,in.p_IdEmpresa));
					Id++;
				}
				
				if (evCliEx == null){
					mapMsg.clear();
					out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"UI_EVENTO_CAPITAL_CLIENTE_NO_EXISTE",mapMsg,conn,in.p_IdEmpresa));
					Id++;
					break;
				}
				if (evEx == null){
					mapMsg.clear();
					out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"UI_EVENTO_CAPITAL_Y_O_SECUENCIA_NO_EXISTE",mapMsg,conn,in.p_IdEmpresa));
					Id++;
					break;
				}
				
				if (configuracionMap.get("CUS.Rango") != null && !"DV".equalsIgnoreCase(ev.p_IdTipoEventoCapital))
				{
					BigDecimal val = evEx.Factor.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO:  ev.p_Total.divide(evEx.Factor,20,RoundingMode.HALF_UP).abs();
					BigDecimal rango = new BigDecimal(((Configuracion)configuracionMap.get("CUS.Rango")).Valor);
					
					if (rango.compareTo(BigDecimal.ZERO) != 0)
					{
						if (evCliEx.Total.equals(ev.p_Total) && !evCliEx.CantidadDerecho.equals(ev.p_CantidadDerecho) && !evCliEx.CantidadDerecho.equals(val)){
							mapMsg.clear();
							out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"UI_MONTO_NO_PERMITIDO",mapMsg,conn,in.p_IdEmpresa));
							Id++;
							break;
						}
						if (ev.p_Total.abs().subtract(evCliEx.Total.abs()).compareTo(rango) > 0){
							mapMsg.clear();
							out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"UI_MONTO_SOBREPASA_RANGO",mapMsg,conn,in.p_IdEmpresa));
							Id++;
							break;
						}						
					}
					
				}
			}
			
			if (MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES) > 0){
				return out;
			}
		}
		{
			HashMap<Long, Boolean> modificaTotal = ConsultarModificacionTotal(eventoClienteExistente, in.p_EventoCapital);
			HashMap<Long, MontosYTotal> diferenciasMontosYTotal = ObtenerDiferenciaMontos(eventoClienteExistente, in.p_EventoCapital, modificaTotal);
			HashMap<String,Object> selectCntParams = new HashMap<String,Object>();
			selectCntParams.put("IdsEventoCapitalCliente", listaIdEvCapCli);
			List<MontosYTotal> montosContraparte = _sql.query(conn, SQL_SELECT_CONTRAPARTE, selectCntParams, MontosYTotal.class);
			List<MontosYTotal> montosContraparteCalculados = CalcularMontosContraparte(diferenciasMontosYTotal, montosContraparte);

			List<Evento> datosUpdate = new ArrayList<>();
			List<AgregarLog.InAgregarLog.Log> logList = new ArrayList<>();
			for (Evento evCap: in.p_EventoCapital){
				EventoExiste evCliEx = (EventoExiste) eventoClienteExistenteMap.get(evCap.p_IdEventoCapitalCliente);
				EventoExiste evEx = (EventoExiste) eventoExistenteMap.get(evCliEx.IdEventoCapital);
				Evento inEv = new Evento();
				inEv.p_IdEventoCapitalCliente = evCap.p_IdEventoCapitalCliente;
				inEv.p_CantidadDerecho 	= evCap.p_CantidadDerecho;
				inEv.p_CantidadNueva 	= evCap.p_CantidadNueva;
				inEv.p_MontoCapital 	= evCap.p_MontoCapital;

				if(evCap.p_IdTipoEventoCapital.equalsIgnoreCase("DV")) {
					if(evCap.p_IdMoneda != null && !evCap.p_IdMoneda.equalsIgnoreCase(eventoExistente.get(0).IdMoneda) && in.p_EventoCapital.get(0).p_CantidadDerecho.compareTo(BigDecimal.ZERO) != 0) {
						inEv.p_FactorOtraMoneda = in.p_EventoCapital.get(0).p_Total.divide(in.p_EventoCapital.get(0).p_CantidadDerecho, MathContext.DECIMAL128);
					}else inEv.p_FactorOtraMoneda = evCap.p_FactorOtraMoneda;
				} else inEv.p_FactorOtraMoneda = evCap.p_FactorOtraMoneda;
				
				inEv.p_Precio 			= evCap.p_Precio;
				inEv.p_MontoInteres		= evCap.p_MontoInteres;	
				inEv.p_TipoCartera		= evCap.p_TipoCartera;
				inEv.p_IdBoveda			= evCap.p_IdBoveda;
				inEv.p_Total			= evCap.p_Total;	
				if (evCap.p_IdTipoEventoCapital.equalsIgnoreCase("DV") && diferenciasMontosYTotal.get(evCap.p_IdEventoCapitalCliente) != null && modificaTotal.get(evCap.p_IdEventoCapitalCliente)==false) {
					inEv.p_Total = inEv.p_Total.subtract(diferenciasMontosYTotal.get(evCap.p_IdEventoCapitalCliente).Total);
				}
				inEv.p_CantidadDerechoArchivo 			= evCap.p_CantidadDerechoArchivo;
				inEv.p_DiferenciaCantidadDerechoArchivo = evCap.p_DiferenciaCantidadDerechoArchivo;
				inEv.p_CantidadNuevaArchivo 			= evCap.p_CantidadNuevaArchivo;
				inEv.p_DiferenciaCantidadNuevaArchivo 	= evCap.p_DiferenciaCantidadNuevaArchivo;
				inEv.p_MontoBeneficioArchivo 			= evCap.p_MontoBeneficioArchivo;
				inEv.p_DiferenciaMontoBeneficioArchivo 	= evCap.p_DiferenciaMontoBeneficioArchivo;
				inEv.p_MontoInteresArchivo 				= evCap.p_MontoInteresArchivo;
				inEv.p_DiferenciaMontoInteresArchivo 	= evCap.p_DiferenciaMontoInteresArchivo;
				inEv.p_MontoCapitalArchivo 				= evCap.p_MontoCapitalArchivo;
				inEv.p_DiferenciaMontoCapitalArchivo	= evCap.p_DiferenciaMontoCapitalArchivo;
				inEv.p_FechaHoraUltimaActualizacion		= fechaHoraSistema;
				inEv.p_TipoSocioNegocio					= evCap.p_TipoSocioNegocio;
				inEv.p_ISFUT							= evCap.p_ISFUT;
				inEv.p_RetencionISFUT					= evCap.p_RetencionISFUT;
				inEv.p_MontoRetencionISFUT = BigDecimal.ZERO;
				inEv.p_IdMoneda							= evCap.p_IdMoneda;
				if( (evCap.p_ISFUT.equalsIgnoreCase("S") || evCap.p_RetencionISFUT.equalsIgnoreCase("S") ) && 
						(evEx.TasaEfectiva.compareTo(BigDecimal.ZERO) == 1))
				{
					inEv.p_MontoRetencionISFUT = (evEx.TasaEfectiva.multiply(evCap.p_Total, MathContext.DECIMAL128).divide(new BigDecimal(100), MathContext.DECIMAL128));
				}
				inEv.p_MontoGastoTransferencia 			= evCap.p_MontoGastoTransferencia;
				inEv.p_MontoImpuestoExtranjero 			= evCap.p_MontoImpuestoExtranjero;
			    datosUpdate.add(inEv);
			    
			    mapMsg.clear();
			    mapMsg.put("p_NumeroDividendo", evCliEx.NumeroDividendo.toString());
				out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_OK,"UI_EVENTO_CAPITAL_MODIFICADO",mapMsg,conn,in.p_IdEmpresa));
				Id++;
				
				//parametros log
				AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
				log.IdTrabajo		= entradaAgregarLog.p_IdTrabajo; 
				log.IdCabeceraLog	= evCap.p_IdEventoCapitalCliente; 
				log.IdTipoLog		= "MODIFICACION"; 
				log.IdModulo		= in.p_IdModulo; 
				log.IdUsuario		= in.p_IdUsuario; 
				log.IdEmpresa		= in.p_IdEmpresa; 
				log.FechaSistemaLog	= fechaSistema; 
				log.IdUsuarioAtendido	= in.p_IdUsuario; 
				log.IdCliente			= evCliEx.IdCliente; 
				log.EstadoCliente		= ""; 
				log.NombreCliente		= evCliEx.NombreCliente; 
				log.IdTipoDocumentoIdentidad	= ""; 
				log.IdDocumentoIdentidad	= ""; 
				log.IdPortafolio			= 0L; 
				log.EstadoPortafolio		= ""; 
				log.IdTipoRegistro			= "MODIFICAREVENTOCAPITALCLIENTE"; 
				log.IdRegistro				= evCap.p_IdEventoCapitalCliente.toString(); 
				log.EstadoRegistro			= ""; 
				log.IdTipoOperacion			= ""; 
				log.Folio					= 0L; 
				log.Cantidad				= BigDecimal.ZERO; 
				log.PrecioTasa				= BigDecimal.ZERO; 
				log.Monto					= BigDecimal.ZERO; 
				log.IdMonedaOrigen			= evCliEx.IdMoneda; 
				log.IdMonedaLiquidacion		= ""; 
				log.TipoCambio				= ""; 
				log.Nemo					= evCliEx.Nemo; 
				log.IdProceso				= 0L; 
				log.EstadoProceso			= ""; 
				log.NombreProceso			= "cl.bcs.custodia.evc.ModificarEventoCapitalCliente";
				logList.add(log);
				
				if(!evCliEx.IdBoveda.equals(inEv.p_IdBoveda)) 		 entradaAgregarLog.p_LogDetalle.add(CrearEventoCapital.agregarDetalleLog(log.IdCabeceraLog, "MODIFICAREVENTOCAPITALCLIENTE", "IdBoveda", evCliEx.IdBoveda, inEv.p_IdBoveda));
				if(!evCliEx.TipoCartera.equals(inEv.p_TipoCartera)) entradaAgregarLog.p_LogDetalle.add(CrearEventoCapital.agregarDetalleLog(log.IdCabeceraLog, "MODIFICAREVENTOCAPITALCLIENTE", "TipoCartera", evCliEx.TipoCartera, inEv.p_TipoCartera));
				if(evCliEx.CantidadDerecho.compareTo(inEv.p_CantidadDerecho) != 0) entradaAgregarLog.p_LogDetalle.add(CrearEventoCapital.agregarDetalleLog(log.IdCabeceraLog, "MODIFICAREVENTOCAPITALCLIENTE", "CantidadDerecho", evCliEx.CantidadDerecho.toString(), inEv.p_CantidadDerecho.toString()));
				if(evCliEx.CantidadNueva.compareTo(inEv.p_CantidadNueva) != 0) entradaAgregarLog.p_LogDetalle.add(CrearEventoCapital.agregarDetalleLog(log.IdCabeceraLog, "MODIFICAREVENTOCAPITALCLIENTE", "CantidadNueva", evCliEx.CantidadNueva.toString(), inEv.p_CantidadNueva.toString()));
				if(evCliEx.Total.compareTo(inEv.p_Total) != 0) entradaAgregarLog.p_LogDetalle.add(CrearEventoCapital.agregarDetalleLog(log.IdCabeceraLog, "MODIFICAREVENTOCAPITALCLIENTE", "Total", evCliEx.Total.toString(), inEv.p_Total.toString()));
				if(evCliEx.MontoCapital.compareTo(inEv.p_MontoCapital) != 0) entradaAgregarLog.p_LogDetalle.add(CrearEventoCapital.agregarDetalleLog(log.IdCabeceraLog, "MODIFICAREVENTOCAPITALCLIENTE", "MontoCapital", evCliEx.MontoCapital.toString(), inEv.p_MontoCapital.toString()));
				if(evCliEx.MontoInteres.compareTo(inEv.p_MontoInteres) != 0) entradaAgregarLog.p_LogDetalle.add(CrearEventoCapital.agregarDetalleLog(log.IdCabeceraLog, "MODIFICAREVENTOCAPITALCLIENTE", "MontoInteres", evCliEx.MontoInteres.toString(), inEv.p_MontoInteres.toString()));
			}

			entradaAgregarLog.p_Log = logList;
			_sql.batch(conn, SQL_UPDATE_EVENTO_CAPITAL, datosUpdate, Evento.class);
			_sql.batch(conn, SQL_UPDATE_CONTRAPARTE, montosContraparteCalculados, MontosYTotal.class);
		}
		//AGREGAR LOG
		{
			AgregarLog agregarLogService = new AgregarLog();
			agregarLogService.logica(conn, entradaAgregarLog);
		}
				
				
		log.write("FIN::CUS.ModificarEventoCapitalCliente");
		return out;
	};
	
	private HashMap<Long, Boolean> ConsultarModificacionTotal(List<EventoExiste> eventoClienteExistente, List<Evento> p_EventoCapital) {
		HashMap<Long, Boolean> modificaTotal = new HashMap<Long, Boolean>();
		HashMap<Long, BigDecimal> comparacionTotal = new HashMap<Long, BigDecimal>();
		for(EventoExiste ev: eventoClienteExistente) {
			modificaTotal.put(ev.IdEventoCapitalCliente, false);
			comparacionTotal.put(ev.IdEventoCapitalCliente, ev.Total);
		}
		for(Evento ec: p_EventoCapital) {
			BigDecimal totalAnterior = comparacionTotal.get(ec.p_IdEventoCapitalCliente) == null? null: comparacionTotal.get(ec.p_IdEventoCapitalCliente);
			if(totalAnterior != null && totalAnterior.compareTo(ec.p_Total) != 0) {
				modificaTotal.put(ec.p_IdEventoCapitalCliente, true);
			}
		}
		return modificaTotal;
	}

	private List<MontosYTotal> CalcularMontosContraparte(HashMap<Long, MontosYTotal> diferenciasMontosYTotal, List<MontosYTotal> montosContraparte) {
		List<MontosYTotal> montosContraparteCalculados = new ArrayList<MontosYTotal>();
		for(MontosYTotal mt: montosContraparte) {
			MontosYTotal montos = new MontosYTotal();
			montos.IdEventoCapitalClienteCli = mt.IdEventoCapitalClienteCli;
			montos.IdEventoCapitalClienteCnt = mt.IdEventoCapitalClienteCnt;
			montos.Total = mt.Total;
			montos.MontoImpuestoExtranjero = mt.MontoImpuestoExtranjero;
			montos.MontoGastoTransferencia = mt.MontoGastoTransferencia;
			if (diferenciasMontosYTotal.get(mt.IdEventoCapitalClienteCli)!=null) {
				BigDecimal diferenciaTotal = diferenciasMontosYTotal.get(mt.IdEventoCapitalClienteCli).Total.multiply(new BigDecimal(-1));
				BigDecimal diferenciaImpuesto = diferenciasMontosYTotal.get(mt.IdEventoCapitalClienteCli).MontoImpuestoExtranjero.multiply(new BigDecimal(-1));
				BigDecimal diferenciaGasto = diferenciasMontosYTotal.get(mt.IdEventoCapitalClienteCli).MontoGastoTransferencia.multiply(new BigDecimal(-1));
				montos.Total = montos.Total.subtract(diferenciaTotal);
				montos.MontoImpuestoExtranjero = montos.MontoImpuestoExtranjero.add(diferenciaImpuesto);
				montos.MontoGastoTransferencia = montos.MontoGastoTransferencia.add(diferenciaGasto);
			}
			montosContraparteCalculados.add(montos);
		}
		return montosContraparteCalculados;
	}

	private HashMap<Long, MontosYTotal> ObtenerDiferenciaMontos(List<EventoExiste> eventoClienteExistente, List<Evento> p_EventoCapital, HashMap<Long, Boolean> modificaTotal) {
		HashMap<Long, MontosYTotal> diferencias = new HashMap<Long, MontosYTotal>();
		for (EventoExiste ee: eventoClienteExistente) {
			MontosYTotal montos = new MontosYTotal();
			montos.IdEventoCapitalClienteCli = ee.IdEventoCapitalCliente;
			montos.IdEventoCapitalClienteCnt = null;
			montos.Total = ee.Total;
			montos.MontoImpuestoExtranjero = ee.MontoImpuestoExtranjero;
			montos.MontoGastoTransferencia = ee.MontoGastoTransferencia;
			diferencias.put(ee.IdEventoCapitalCliente, montos);
		}
		for (Evento ev: p_EventoCapital) {
			BigDecimal diferenciaGasto = new BigDecimal(0);
			BigDecimal diferenciaImpuesto = new BigDecimal(0);
			BigDecimal diferenciaTotal = new BigDecimal(0);
			if(diferencias.get(ev.p_IdEventoCapitalCliente) != null && diferencias.get(ev.p_IdEventoCapitalCliente).MontoGastoTransferencia != null) {
				diferenciaGasto = ev.p_MontoGastoTransferencia.subtract(diferencias.get(ev.p_IdEventoCapitalCliente).MontoGastoTransferencia);
			}
			if(diferencias.get(ev.p_IdEventoCapitalCliente) != null && diferencias.get(ev.p_IdEventoCapitalCliente).MontoImpuestoExtranjero != null) {
				diferenciaImpuesto = ev.p_MontoImpuestoExtranjero.subtract(diferencias.get(ev.p_IdEventoCapitalCliente).MontoImpuestoExtranjero);
			}
			if(modificaTotal.get(ev.p_IdEventoCapitalCliente)!=null && modificaTotal.get(ev.p_IdEventoCapitalCliente)==true) {
				diferenciaTotal = ev.p_Total.subtract(diferencias.get(ev.p_IdEventoCapitalCliente).Total);
			}else {
				diferenciaTotal = diferenciaGasto.add(diferenciaImpuesto);
			}
			MontosYTotal diferenciasMontos = new MontosYTotal();
			diferenciasMontos.IdEventoCapitalClienteCli = ev.p_IdEventoCapitalCliente;
			diferenciasMontos.IdEventoCapitalClienteCnt = null;
			diferenciasMontos.Total = diferenciaTotal;
			diferenciasMontos.MontoImpuestoExtranjero = diferenciaImpuesto;
			diferenciasMontos.MontoGastoTransferencia = diferenciaGasto;
			diferencias.put(ev.p_IdEventoCapitalCliente, diferenciasMontos);
		}
		return diferencias;
	}

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
			conn = PlataformaUtils.getPropertiesConnection("desa02", "connections.properties");
			//datos entrada
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":97,\"p_IdModulo\":\"CUS\",\"p_UsaReglasNegocio\":\"S\",\"p_EventoCapital\":[{\"p_IdTipoEventoCapital\":\"SU\",\"p_IdEventoCapitalCliente\":7895028,\"p_CantidadDerecho\":1,\"p_CantidadNueva\":10,\"p_MontoCapital\":310,\"p_Precio\":0,\"p_MontoInteres\":0,\"p_TipoCartera\":\"CUS\",\"p_IdBoveda\":\"BCS\",\"p_Total\":25,\"p_CantidadDerechoArchivo\":0,\"p_DiferenciaCantidadDerechoArchivo\":0,\"p_CantidadNuevaArchivo\":0,\"p_DiferenciaCantidadNuevaArchivo\":0,\"p_MontoBeneficioArchivo\":0,\"p_DiferenciaMontoBeneficioArchivo\":0,\"p_MontoInteresArchivo\":0,\"p_DiferenciaMontoInteresArchivo\":0,\"p_MontoCapitalArchivo\":300,\"p_DiferenciaMontoCapitalArchivo\":0,\"p_FechaHoraUltimaActualizacion\":\"2024-06-19 19:02:04\",\"p_TipoSocioNegocio\":\"CLI\",\"p_ISFUT\":\"N\",\"p_RetencionISFUT\":\"N\",\"p_MontoRetencionISFUT\":0}]}";
			
			EventoCapital_IN objIn = gson.fromJson(json,EventoCapital_IN.class);
			//mientras pruebo hare el rollback
			ModificarEventoCapitalCliente demo = new ModificarEventoCapitalCliente();
			
			EventoCapital_OUT ObjOut=demo.logica(conn, objIn);
				
			System.out.println(ObjOut.toString());
			
			conn.rollback();
			//conn.commit();
		}catch (Exception e){
			e.printStackTrace();
		}finally {
			conn.close();
		}
			System.out.println("== END ==");

	};
}
