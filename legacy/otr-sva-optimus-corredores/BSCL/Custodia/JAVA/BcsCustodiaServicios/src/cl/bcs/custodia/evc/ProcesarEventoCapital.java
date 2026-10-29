package cl.bcs.custodia.evc;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.map.MultiKeyMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.plataforma.pla.utils.Feriados;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.custodia.ConsultarFlujoCustodia;
import cl.bcs.custodia.ConsultarFlujoCustodia.DatosFlujoCustodia;
import cl.bcs.custodia.ConsultarFlujoCustodia.FlujoCustodiaDetallado;
import cl.bcs.custodia.ConsultarFlujoCustodia.MatrizUso;
import cl.bcs.custodia.evc.ProcesarEventoCapitalCambioDeNemos.ProcesarEventoCapitalCambioDeNemosEntrada;
import cl.bcs.custodia.evc.ProcesarEventoCapitalCambioDeNemos.ProcesarEventoCapitalCambioDeNemosSalida;
import cl.bcs.custodia.evc.ProcesarEventoCapitalCanje.ProcesarEventoCapitalCanjeEntrada;
import cl.bcs.custodia.evc.ProcesarEventoCapitalCanje.ProcesarEventoCapitalCanjeSalida;
import cl.bcs.custodia.evc.ProcesarEventoCapitalDividendoEnAcciones.DatosCliente;
import cl.bcs.custodia.evc.ProcesarEventoCapitalDividendoEnAcciones.DatosCuentaCustodio;
import cl.bcs.custodia.evc.ProcesarEventoCapitalDividendoEnAcciones.DatosEjecutivo;
import cl.bcs.custodia.evc.ProcesarEventoCapitalDividendoEnAcciones.DatosSocioDeNegocio;
import cl.bcs.custodia.evc.ProcesarEventoCapitalDividendoEnAcciones.ProcesarEventoCapitalDividendoEnAccionesEntrada;
import cl.bcs.custodia.evc.ProcesarEventoCapitalDividendoEnAcciones.ProcesarEventoCapitalDividendoEnAccionesSalida;
import cl.bcs.custodia.evc.ProcesarEventoCapitalDividendoEnDinero.ProcesarEventoCapitalDividendoEnDineroEntrada;
import cl.bcs.custodia.evc.ProcesarEventoCapitalDividendoEnDinero.ProcesarEventoCapitalDividendoEnDineroSalida;
import cl.bcs.custodia.evc.ProcesarEventoCapitalDivisionSociedad.ProcesarEventoCapitalDivisionSociedadEntrada;
import cl.bcs.custodia.evc.ProcesarEventoCapitalDivisionSociedad.ProcesarEventoCapitalDivisionSociedadSalida;
import cl.bcs.custodia.evc.ProcesarEventoCapitalEmisionLiberada.ProcesarEventoCapitalEmisionLiberadaEntrada;
import cl.bcs.custodia.evc.ProcesarEventoCapitalEmisionLiberada.ProcesarEventoCapitalEmisionLiberadaSalida;
import cl.bcs.custodia.evc.ProcesarEventoCapitalEmisionPago.ProcesarEventoCapitalEmisionPagoEntrada;
import cl.bcs.custodia.evc.ProcesarEventoCapitalEmisionPago.ProcesarEventoCapitalEmisionPagoSalida;
import cl.bcs.custodia.evc.ProcesarEventoCapitalSorteoLetras.ProcesarEventoCapitalSorteoLetrasEntrada;
import cl.bcs.custodia.evc.ProcesarEventoCapitalSorteoLetras.ProcesarEventoCapitalSorteoLetrasSalida;

import cl.bcs.custodia.ConsultarEventoCapitalCliente;
import cl.bcs.custodia.ConsultarEventoCapitalCliente.ArrSalidaGrilla;
import cl.bcs.custodia.evc.ProcesarEventoCapitalRepartoCapital;
import cl.bcs.custodia.evc.ProcesarEventoCapitalRepartoCapital.ProcesarEventoCapitalRepartoCapitalEntrada;
import cl.bcs.custodia.evc.ProcesarEventoCapitalRepartoCapital.ProcesarEventoCapitalRepartoCapitalSalida;


import cl.bcs.custodia.pojo.EventoCapital;
import cl.bcs.custodia.pojo.EventoCapitalCliente;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.ObtenerDiaAnterior;
import cl.bcs.plataforma.pla.ObtenerDiasHabilesAnteriores;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.ObtenerDiaAnterior.InObtenerDiaAnterior;
import cl.bcs.plataforma.pla.ObtenerDiaAnterior.OutObtenerDiaAnterior;
import cl.bcs.plataforma.pla.ObtenerDiasHabilesAnteriores.InObtenerDiasHabilesAnteriores;
import cl.bcs.plataforma.pla.ObtenerDiasHabilesAnteriores.OutObtenerDiasHabilesAnteriores;
import cl.bcs.plataforma.pla.ObtenerDiasHabilesAnteriores.OutObtenerDiasHabilesAnteriores.FechasOut;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.MonedaFormato;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ProcesarEventoCapital implements JavaCall{
	private static final Logger logger = LogManager.getLogger(ProcesarEventoCapital.class);
	private final String SQL_SELECT_CONSULTAR_EVENTO_CAPITAL			= loadResourceToString("cl/bcs/custodia/sql/ProcesarEventoCapital_SelectEventoCapital.sql");
	private final String SQL_SELECT_CONSULTAR_DATOS_CLIENTE				= loadResourceToString("cl/bcs/custodia/sql/ProcesarEventoCapital_SelectDatosCliente.sql");
	private final String SQL_SELECT_CONSULTAR_DATOS_SOCIO_NEGOCIO		= loadResourceToString("cl/bcs/custodia/sql/ProcesarEventoCapital_SelectDatosSocioDeNegocio.sql");
	private final String SQL_SELECT_CONSULTAR_DATOS_EJECUTIVO			= loadResourceToString("cl/bcs/custodia/sql/ProcesarEventoCapital_SelectDatosEjecutivo.sql");
	private final String SQL_SELECT_CONSULTAR_DATOS_CUENTA_CUSTODIO 	= loadResourceToString("cl/bcs/custodia/sql/ProcesarEventoCapital_SelectDatosCuentaCustodio.sql");
	private final String SQL_SELECT_CONSULTAR_BOVEDA					= loadResourceToString("cl/bcs/custodia/sql/ProcesarEventoCapital_SelectBoveda.sql");
	
	private final String SQL_SELECT_CONSULTAR_CONTRAPARTE				= loadResourceToString("cl/bcs/custodia/sql/ProcesarEventoCapital_SelectContraparte.sql");
	private final String SQL_SELECT_CONSULTAR_SIMULTANEA_COMPRA			= loadResourceToString("cl/bcs/custodia/sql/ProcesarEventoCapital_SelectSimultaneaCompra.sql");
	private final String SQL_SELECT_CONSULTAR_SIMULTANEA_VENTA			= loadResourceToString("cl/bcs/custodia/sql/ProcesarEventoCapital_SelectSimultaneaVenta.sql");
	private final String SQL_SELECT_OPERACIONES_FECHA_LIMITE 			= loadResourceToString("cl/bcs/custodia/sql/ProcesarEventoCapital_SelectOperacionesFechaLimite.sql");
	private final String SQL_SELECT_CARTERAS_NEGATIVAS					= loadResourceToString("cl/bcs/custodia/sql/ProcesarEventoCapital_SelectCarterasNegativas.sql");
	private final String SQL_SELECT_PARAMETRO					        = loadResourceToString("cl/bcs/custodia/sql/ProcesarEventoCapital_SelectParametro.sql");
	private final String SQL_SELECT_EMPRESTITO_VIGENTE                = loadResourceToString("cl/bcs/custodia/sql/ProcesarEventoCapital_SelectEmprestitoVigente.sql");
	private final String SQL_SELECT_EMISOR					            = loadResourceToString("cl/bcs/custodia/sql/ProcesarEventoCapital_SelectEmisor.sql");
	private final String SQL_SELECT_CONSULTAR_CORREDOR_CONTRAPARTE		= loadResourceToString("cl/bcs/custodia/sql/ProcesarEventoCapital_SelectCorredorContraparte.sql");
	private final String SQL_SELECT_CONSULTAR_CONTRAPARTE_NOM			= loadResourceToString("cl/bcs/custodia/sql/ProcesarEventoCapital_SelectCorredorContraparteNominal.sql");
	private final String SQL_SELECT_CONSULTAR_EXENTOS_IMP_DV			= loadResourceToString("cl/bcs/custodia/sql/ProcesarEventoCapital_SelectClientesExentosImpuestoDvExtranjero.sql");
	private final String SQL_SELECT_PAIS_EMISOR							= loadResourceToString("cl/bcs/custodia/sql/ProcesarEventoCapital_SelectPaisEmisor.sql");
	private final String SQL_SELECT_PACTOS_VIGENTES			            = loadResourceToString("cl/bcs/custodia/sql/ProcesarEventoCapital_SelectPactosVigentes.sql");		
	private final String SQL_SELECT_MATRIZ_USO				            = "SELECT \"IdCliente\", \"IdPortafolio\", \"UsoTipoOperacion\", \"IdClasificacionTipoInstrumento\", \"IdTipoInstrumento\", \"IdInstrumento\" FROM \"BSCL.Custodia.BD::CUS.FlujoCustodia_MatrizUso\"";		
	
	private final String SQL_INSERT_EVENTO_CAPITAL_CLIENTE_CARTERA		= loadResourceToString("cl/bcs/custodia/sql/ProcesarEventoCapital_InsertEventoCapitalClienteCartera.sql");
	
	//IdInstrumento
	public static class ConsultaBovedas{
		public String	IdBoveda;
		public Long		IdSocioNegocio;					
		public String	TipoSocioNegocio;					
		public Long		IdPortafolio;
		public String	IdTipoDocumentoIdentidad;
		public String	DocumentoIdentidad;				
		public String	NombreCliente;
		public String	IdEstado;
		
		public String toString() {
			return Utils.printJson(this, false);
		}		
	};
	
	public static class ContraparteNominales {
		public Long IdRegistro;
		public Long IdCorredorContraparte;
	}
	
	public static class ConsultaContraparteGarantia{
		public Long		IdContraparte;
		public Long		IdSocioNegocio;					
		public String	TipoSocioNegocio;					
		public Long		IdPortafolio;
		public String	IdTipoDocumentoIdentidad;
		public String	DocumentoIdentidad;				
		public String	NombreCliente;
		public String 	IdEstado;
		
		public String toString() {
			return Utils.printJson(this, false);
		}		
	};
	
	public static class ConsultaCorredorContraparte{
		public Long		IdCorredorContraparte;
		public Long		IdSocioNegocio;					
		public String	TipoSocioNegocio;					
		public Long		IdPortafolio;
		public String	IdTipoDocumentoIdentidad;
		public String	DocumentoIdentidad;
		public String	NombreCorredor;
		public String 	IdEstado;
		
		public String toString() {
			return Utils.printJson(this, false);
		}		
	};
	public static class ConsultaContraparte {
		public Long		IdInstrumento;
		public String	TipoCustodia;
		public String	IndicadorOD;
		public Long		IdSocioNegocio;					
		public String	TipoSocioNegocio;					
		public Long		IdPortafolio;
		public String	IdTipoDocumentoIdentidad;
		public String	DocumentoIdentidad;				
		public String	NombreCliente;
		public String 	IdEstado;
		public String	DocumentoIdentidadEmisor;
		public String	NombreClienteEmisor;
		public Long     IdCorredorContraparte;
		public Long    FolioRegistro;
		public BigDecimal Cantidad;
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	public static class ConsultaPactos{
		public String		IdEstado;               
		public BigDecimal	Cantidad;             
		public BigDecimal	INGCantidad;       
		public BigDecimal	EGRCantidad;
		public String		Nemotecnico;
		public Long			IdPortafolio;
		public Long			FolioCartera;          
		public Long			FolioComprobante;
		public Long			EGRFolioComprobante;    
		public Long			IdPortafolioRelacionado;
		
		public String toString() {
			return Utils.printJson(this, false);
		}		
	};
	
	public static class ConsultaImpuestoDvExtranjero{
		public Long 	IdCliente;
		public String 	AplicaImpuesto;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class PaisEmisor{
		public String 	IdEmisor;
		public Long		IdPaisEmisor;
		public Long 	IdPaisSistema;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ProcesarEventoCapitalEntrada {
		public Long							p_IdEmpresa; 
		public Long							p_IdUsuario;
		public String						p_IdModulo;
		public String						p_Origen; //corresponde desde donde es ejecutado el evento MAN: UI, AUT: Job Pauta, AGENDA: Agenda
		public String						p_UsaReglasNegocio;
		public String						p_Prametro;
		public List<String>					p_FechaLimite; // fechas corte
		public List<Long>					p_IdEventoCapital;//eventos capital a procesar
		public List<String>					p_IdTipoEventoCapital; //tipos eventos capital a procesar
				
		//public String 		p_FechaPago; // fecha de pago del evento, por si se ejecuta con una fecha anterior .. igual validar
		//public List<Evento> 				p_EventoCapital;
		public ProcesarEventoCapitalEntrada (){
			this.p_FechaLimite 			= new ArrayList<String>();
			this.p_IdEventoCapital 		= new ArrayList<Long>();
			this.p_IdTipoEventoCapital 	= new ArrayList<String>();
		}
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	public static class ProcesarEventoCapitalSalida {
		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;
		public List<Long>			P_OUT_SECUENCIA;
		
		public ProcesarEventoCapitalSalida() {
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
	
	public class EventoCapitalClienteCartera{
		public String IdEventoCapitalClienteCartera;
		public Long IdEventoCapitalCliente;
		public Long IdCartera;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public class LlaveCliente{
		public Long IdPortafolio;
		public String TipoCartera;
		public Long IdContraparteGarantia;
		public BigDecimal Total;
		public LlaveCliente(Long idPortafolio, String tipoCartera, Long idContraparteGarantia, BigDecimal total) {
			this.IdPortafolio = idPortafolio;
			this.TipoCartera = tipoCartera;
			this.IdContraparteGarantia = idContraparteGarantia;
			this.Total = total;
		}
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public ProcesarEventoCapitalSalida logica(Connection con, ProcesarEventoCapitalEntrada in) throws Exception {
		logger.info("INICIO::CUS.ProcesarEventoCapital");
		ProcesarEventoCapitalSalida out = new ProcesarEventoCapitalSalida();
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
		
		
		

		List<EventoCapital> eventoCapitalRC = new ArrayList<>();     // REPARTO DE CAPITAL
		
		
		
		//Map<String, Object> eventoCapitalMap_IN = new HashMap<>();
		//Map<String, Object> TipoEventoCapitalMap_IN = new HashMap<>();
		// obtener fecha del sistema
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");		
		
		if (in.p_FechaLimite.size() == 0){
			 in.p_FechaLimite.add(fechaSistema);
		}
		//Consulta evento a capital a procesar.
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("p_IdEmpresa", in.p_IdEmpresa);
		
		if("AGENDA".equals(in.p_Origen)) {
			Long paisSistema = ConfiguracionSistema.getParametroConfiguracionLong(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.PaisSistema");
			
			Long EventoCapitalDiasBursatilRetroactivos = 30L;
			try {
				EventoCapitalDiasBursatilRetroactivos = ConfiguracionSistema.getParametroConfiguracionLong(con, in.p_IdEmpresa,"CUSTODIA", "EventoCapitalDiasBursatilRetroactivos");
			} catch(Exception e) {
				logger.warn("ADVERTENCIA::Parametro EventoCapitalDiasBursatilRetroactivos no existe, se toma valor por defecto de " + EventoCapitalDiasBursatilRetroactivos + " dias.");
			}
			
			// Obtener el dia habil para el proceso de la agenda
			ObtenerDiasHabilesAnteriores fecha = new ObtenerDiasHabilesAnteriores();
			InObtenerDiasHabilesAnteriores entradaDia = new InObtenerDiasHabilesAnteriores();
			entradaDia.idPais = paisSistema;
			entradaDia.diaHabil = "S";
			entradaDia.soloCaeDiaHabil = "S";
			InObtenerDiasHabilesAnteriores.FechasIn inObtenerDiaAntHabil = new InObtenerDiasHabilesAnteriores.FechasIn();
			inObtenerDiaAntHabil.fechaInicial = fechaSistema;
			inObtenerDiaAntHabil.numeroDias = 1L;
			List<InObtenerDiasHabilesAnteriores.FechasIn> fechaHabil = new ArrayList<>();
			fechaHabil.add(inObtenerDiaAntHabil);
			entradaDia.fechas = fechaHabil;
			
			OutObtenerDiasHabilesAnteriores FechaLimiteMax = fecha.logica(con, entradaDia);
			String outFechaLimiteMax = FechaLimiteMax.fechas.get(0).fechaCalculada;

			// Obtener dias habiles anteriores a la fecha limite max para obtener la fecha limite minima
			ObtenerDiaAnterior obtenerDiaAnt = new ObtenerDiaAnterior();
			InObtenerDiaAnterior inObtenerDiaAnt = new InObtenerDiaAnterior();
			InObtenerDiaAnterior.FechasIn fechasIn = new InObtenerDiaAnterior.FechasIn();
			fechasIn.fechaInicial = outFechaLimiteMax;
			fechasIn.numeroDias = EventoCapitalDiasBursatilRetroactivos;
			inObtenerDiaAnt.fechas.add(fechasIn);
			inObtenerDiaAnt.idPais = paisSistema;
			inObtenerDiaAnt.diaHabil = "N";
			inObtenerDiaAnt.soloCaeDiaHabil = "S";

			OutObtenerDiaAnterior fechaLimiteMin = obtenerDiaAnt.logica(con, inObtenerDiaAnt);
			String outFechaLimiteMin = fechaLimiteMin.fechas.get(0).fechaCalculada;
			
			params.put("p_FechaLimiteMin", outFechaLimiteMin);
			params.put("p_FechaLimiteMax", outFechaLimiteMax);
		} else {
			params.put("p_FechaLimiteMin", in.p_FechaLimite.stream().min(Comparator.naturalOrder()).orElse(null));
			params.put("p_FechaLimiteMax", in.p_FechaLimite.stream().max(Comparator.naturalOrder()).orElse(null));
		}
		
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
				
				
				else if ("RC".equalsIgnoreCase(ev.IdTipoEventoCapital) && in.p_IdTipoEventoCapital.contains(ev.IdTipoEventoCapital)){
					eventoCapitalRC.add(ev);
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
				
					

				case "RC":     //REPARTO DE CAPITAL
					eventoCapitalRC.add(ev);
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
		if (eventoCapitalDA.size() > 0){//DIVIDENDO EN ACCIONES
			ProcesarEventoCapitalDividendoEnAcciones proc = new ProcesarEventoCapitalDividendoEnAcciones();
			ProcesarEventoCapitalDividendoEnAccionesEntrada entrada = new ProcesarEventoCapitalDividendoEnAccionesEntrada();
			entrada.p_IdEmpresa = in.p_IdEmpresa; 
			entrada.p_IdUsuario = in.p_IdUsuario;
			entrada.p_IdModulo = "CUS";
			entrada.p_EventoCapital = eventoCapitalDA;
			ProcesarEventoCapitalDividendoEnAccionesSalida sal = proc.logica(con, entrada);
			out.P_OUT_MENSAJES = sal.P_OUT_MENSAJES;
		}
		if (eventoCapitalEL.size() > 0){//EMISION LIBERADA
			ProcesarEventoCapitalEmisionLiberada proc = new ProcesarEventoCapitalEmisionLiberada();
			ProcesarEventoCapitalEmisionLiberadaEntrada entrada = new ProcesarEventoCapitalEmisionLiberadaEntrada();
			entrada.p_IdEmpresa = in.p_IdEmpresa; 
			entrada.p_IdUsuario = in.p_IdUsuario;
			entrada.p_IdModulo = "CUS";
			entrada.p_EventoCapital = eventoCapitalEL;
			ProcesarEventoCapitalEmisionLiberadaSalida sal = proc.logica(con, entrada);
			out.P_OUT_MENSAJES = sal.P_OUT_MENSAJES;			
		}
		if (eventoCapitalEP.size() > 0){//Emision Pagada
			ProcesarEventoCapitalEmisionPago proc = new ProcesarEventoCapitalEmisionPago();
			ProcesarEventoCapitalEmisionPagoEntrada entrada = new ProcesarEventoCapitalEmisionPagoEntrada();
			entrada.p_IdEmpresa = in.p_IdEmpresa; 
			entrada.p_IdUsuario = in.p_IdUsuario;
			entrada.p_IdModulo = "CUS";
			entrada.p_EventoCapital = eventoCapitalEP;
			ProcesarEventoCapitalEmisionPagoSalida sal = proc.logica(con, entrada);
			out.P_OUT_MENSAJES = sal.P_OUT_MENSAJES;			
		}
		if (eventoCapitalDV.size() > 0){//DIVIDENDO EN DINERO
			ProcesarEventoCapitalDividendoEnDinero proc = new ProcesarEventoCapitalDividendoEnDinero();
			ProcesarEventoCapitalDividendoEnDineroEntrada entrada = new ProcesarEventoCapitalDividendoEnDineroEntrada();
			entrada.p_IdEmpresa = in.p_IdEmpresa; 
			entrada.p_IdUsuario = in.p_IdUsuario;
			entrada.p_IdModulo = "CUS";
			entrada.p_EventoCapital = eventoCapitalDV;
			ProcesarEventoCapitalDividendoEnDineroSalida sal = proc.logica(con, entrada);
			out.P_OUT_MENSAJES = sal.P_OUT_MENSAJES;
		}
		
		

		if (eventoCapitalRC.size() > 0){//REPARTO CAPITAL
			ProcesarEventoCapitalRepartoCapital proc = new ProcesarEventoCapitalRepartoCapital();
			ProcesarEventoCapitalRepartoCapitalEntrada entrada = new ProcesarEventoCapitalRepartoCapitalEntrada();
			entrada.p_IdEmpresa = in.p_IdEmpresa; 
			entrada.p_IdUsuario = in.p_IdUsuario;
			entrada.p_IdModulo = "CUS";
			entrada.p_EventoCapital = eventoCapitalRC;
			ProcesarEventoCapitalRepartoCapitalSalida sal = proc.logica(con, entrada);
			out.P_OUT_MENSAJES = sal.P_OUT_MENSAJES;
		}
		
		
		
		
		if (eventoCapitalCN.size() > 0){ // CAMBIO DE NEMO
			ProcesarEventoCapitalCambioDeNemos proc = new ProcesarEventoCapitalCambioDeNemos();
			ProcesarEventoCapitalCambioDeNemosEntrada entrada = new ProcesarEventoCapitalCambioDeNemosEntrada();
			entrada.p_IdEmpresa = in.p_IdEmpresa; 
			entrada.p_IdUsuario = in.p_IdUsuario;
			entrada.p_IdModulo = "CUS";
			entrada.p_EventoCapital = eventoCapitalCN;
			ProcesarEventoCapitalCambioDeNemosSalida sal = proc.logica(con, entrada);
			out.P_OUT_MENSAJES = sal.P_OUT_MENSAJES;
			out.P_OUT_SECUENCIA = sal.P_OUT_SECUENCIA;
		}
		if (eventoCapitalSL.size() > 0){ // Sorteo de letras
			ProcesarEventoCapitalSorteoLetras proc = new ProcesarEventoCapitalSorteoLetras();
			ProcesarEventoCapitalSorteoLetrasEntrada entrada = new ProcesarEventoCapitalSorteoLetrasEntrada();
			entrada.p_IdEmpresa = in.p_IdEmpresa; 
			entrada.p_IdUsuario = in.p_IdUsuario;
			entrada.p_IdModulo = "CUS";
			entrada.p_UsaReglasNegocio = "S";
			entrada.p_EventoCapital = eventoCapitalSL;
			ProcesarEventoCapitalSorteoLetrasSalida sal = proc.logica(con, entrada);
			out.P_OUT_MENSAJES = sal.P_OUT_MENSAJES;
			out.P_OUT_SECUENCIA = sal.P_OUT_SECUENCIA;
		}
		if (eventoCapitalCA.size() > 0){ // CANJE
			ProcesarEventoCapitalCanje proc = new ProcesarEventoCapitalCanje();
			ProcesarEventoCapitalCanjeEntrada entrada = new ProcesarEventoCapitalCanjeEntrada();
			entrada.p_IdEmpresa = in.p_IdEmpresa; 
			entrada.p_IdUsuario = in.p_IdUsuario;
			entrada.p_IdModulo = "CUS";
			entrada.p_EventoCapital = eventoCapitalCA;
			ProcesarEventoCapitalCanjeSalida sal = proc.logica(con, entrada);
			out.P_OUT_MENSAJES = sal.P_OUT_MENSAJES;
		}
		if(eventoCapitalDF.size() > 0) {
			ProcesarEventoCapitalDVFFMM proc = new ProcesarEventoCapitalDVFFMM();
			ProcesarEventoCapitalDVFFMM.Entrada entrada = new ProcesarEventoCapitalDVFFMM.Entrada();
			entrada.p_IdEmpresa = in.p_IdEmpresa;
			entrada.p_IdUsuario = in.p_IdUsuario;
			entrada.p_IdModulo = "CUS";
			entrada.p_IdEventoCapital = eventoCapitalDF;
			ProcesarEventoCapitalDVFFMM.Salida sal = proc.logica(con, entrada);
			out.P_OUT_MENSAJES = sal.P_OUT_MENSAJES;
			out.P_OUT_SECUENCIA = sal.P_OUT_SECUENCIA;
		}
		if (eventoCapitalDS.size() > 0) {
			ProcesarEventoCapitalDivisionSociedad proc = new ProcesarEventoCapitalDivisionSociedad();
			ProcesarEventoCapitalDivisionSociedadEntrada entrada = new ProcesarEventoCapitalDivisionSociedadEntrada();
			entrada.p_IdEmpresa = in.p_IdEmpresa; 
			entrada.p_IdUsuario = in.p_IdUsuario;
			entrada.p_IdModulo = "CUS";
			entrada.p_EventoCapital = eventoCapitalDS;
			ProcesarEventoCapitalDivisionSociedadSalida sal = proc.logica(con, entrada);
			out.P_OUT_MENSAJES = sal.P_OUT_MENSAJES;
		}
		

		
		logger.info("FIN::CUS.ProcesarEventoCapital");
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
	

	
	private String obtieneValorTipoCartera (String vItemOrigen, String vItemCalculo) throws Exception{
		String regValores = new String();
		
		switch (vItemOrigen) {
		case "CUSTODIA":
			switch (vItemCalculo){
			case "CARTERA_TRANSITO_COMPRA":
				regValores = "TRC";
				break;
				
			case "CARTERA_TRANSITO_VENTA":
				regValores = "TRV";
				break;
				
			case "INFORMACION_CARTERA":
				regValores = "CUS";
				break;
			case "INFORMACION_TRANSITO_INGRESO":
			case "INFORMACION_TRANSITO_RETIRO":
			case "MOV_TRANSITO_RETIRO_VENTA_DESCUBIERTA":
				regValores = "NOM"; // Nominal en transito
				break;				
			}
			break;
		case "CUSTODIA_NEGATIVA":
			regValores = "NEG";	
			break;
		case "GARANTIA":
			regValores = "GAR";
			break;
		case "SIMULTANEA":
			switch (vItemCalculo) {
			case "SIMULTANEA_COMPRA":
				regValores = "SIC";
				break;
			case "SIMULTANEA_VENTA":
				regValores = "SIV";
				break;
			default:
				regValores = "SIM";
				break;
			}
			break;
		case "PRESTAMO":
			switch (vItemCalculo) {
			case "PRESTAMO":
				regValores = "PRE";
				break;
			case "EMPRESTITO":
				regValores = "EMP";
				break;
			default:
				regValores = "PRE";
				break;
			}
			break;
		case "PACTOS":
			regValores = "PAC";
			break;
		default:
			break;
		};
		return regValores;
	};
	
	private String obtieneValorBolsa (String vBolsaSistema, String vIdBolsa) throws Exception{
		String regValores = new String();
	
		switch (vIdBolsa) {
		case "":
			regValores	= vBolsaSistema;
			break;
		default:
			regValores	= vIdBolsa;
			break;
		};
		return regValores;
	};
	
	//TODO: Agrega Clientes
	private EventoCapitalCliente AgregaRegistroCliente(
							EventoCapital 									datosEvento,
							FlujoCustodiaDetallado 							regSalidaCusPor,
							DatosCliente 									infoClienteSocio, 
							DatosSocioDeNegocio 							datosSocN,
							DatosEjecutivo 									datosEjec,
							String 											valorTipoCartera,
							DatosCuentaCustodio								valorBoveda,
							String 											vBolsaSistema,
							String 											vlocalizacionSistema,
							InformacionEmpresa 								empresa,
							Connection										con) throws Exception {

		EventoCapitalCliente registroEvento = new EventoCapitalCliente();
		
		registroEvento.IdEventoCapital					= datosEvento.IdEventoCapital;
		registroEvento.IdTipoEventoCapital				= datosEvento.IdTipoEventoCapital;
		registroEvento.Secuencia						= datosEvento.Secuencia;
		registroEvento.IdSocioNegocio					= datosSocN!= null ? datosSocN.IdSocioDeNegocio:0L;
		registroEvento.Paridad							= datosEvento.Paridad;
		if (datosSocN!= null && datosSocN.IdCliente > 0){
		registroEvento.TipoSocioNegocio		= "CLI";
		}else if (datosSocN!= null && datosSocN.IdProveedor > 0){
		registroEvento.TipoSocioNegocio		= "PRO";
		}else if (datosSocN!= null && datosSocN.IdEmpleado > 0){
		registroEvento.TipoSocioNegocio		= "EMP";
		}else if (datosSocN!= null && datosSocN.IdContraparte > 0){
		registroEvento.TipoSocioNegocio		= "CNT";
		};
		registroEvento.DocumentoIdentidad				= infoClienteSocio!= null ? infoClienteSocio.DocumentoIdentidad:"";
		registroEvento.NombreCliente					= infoClienteSocio!= null ? infoClienteSocio.NombreCompleto:"";
		registroEvento.IdTipoDocumentoIdentidad			= infoClienteSocio!= null ? infoClienteSocio.TipoDocumentoIdentidad:"";
		registroEvento.IdCliente						= regSalidaCusPor.IdCliente;
		registroEvento.IdPortafolio						= regSalidaCusPor.IdPortafolio;
		registroEvento.IdClasificacionTipoInstrumento	= datosEvento.IdClasificacionTipoInstrumento;
		registroEvento.IdTipoInstrumento				= datosEvento.IdTipoInstrumento;
		registroEvento.IdInstrumento					= datosEvento.IdInstrumento;
		registroEvento.Nemo								= datosEvento.Nemo;
		registroEvento.IdInstrumentoNuevo				= 0L;
		registroEvento.NemoNuevo						= "";
		registroEvento.FechaCierre						= datosEvento.FechaLimite;
		registroEvento.FechaPago						= datosEvento.FechaPago;
		
		// registroEvento.CantidadDerecho - los prestatarios deben quedar negativos, ya que estos son las contrapartes de los emprestitos. 
		// siultaneas de compra (compra PH venta TP) deben quedar negativos.
		registroEvento.CantidadDerecho					= (valorTipoCartera.equals("PRE") || valorTipoCartera.equals("EMP") || valorTipoCartera.equals("SIV")  || valorTipoCartera.equals("SIC")) ? regSalidaCusPor.CantidadProceso.multiply(new BigDecimal(-1)) : regSalidaCusPor.CantidadProceso;
		registroEvento.Precio							= datosEvento.IdTipoEventoCapital.equals("EP") ? datosEvento.Valor : regSalidaCusPor.PrecioMercado;
		
		if (infoClienteSocio!= null){
			if (infoClienteSocio.IdTipoPersona != null && infoClienteSocio.IdPaisResidencia != null && infoClienteSocio.IdTipoPersona.equalsIgnoreCase("J") && infoClienteSocio.IdPaisResidencia.equals(empresa.IdPais)){
				registroEvento.MontoRetencion	= BigDecimal.ZERO;
			}else {
				registroEvento.MontoRetencion	= (((((((regSalidaCusPor.Cantidad != null ? regSalidaCusPor.Cantidad : BigDecimal.ZERO))).multiply(datosEvento.Paridad)).multiply(datosEvento.Factor)).multiply(datosEvento.Impuesto)).divide(new BigDecimal(100),20,RoundingMode.HALF_UP));
			};
			}else{
				registroEvento.MontoRetencion	= BigDecimal.ZERO;
		}
		registroEvento.TipoCartera 						= valorTipoCartera;
		registroEvento.IdConceptoGarantia				= valorTipoCartera == "GAR" ? regSalidaCusPor.IdConcepto : "";
		registroEvento.IdTipoOperacionGarantia			= ""; //recorreFlujoDetallado.IdTipoOperacion;
		
		// registroEvento.CantidadNueva - los prestatarios deben quedar negativos, ya que estos son las contrapartes de los emprestitos. 
		// siultaneas de compra (compra PH venta TP) deben quedar negativos.
		
		registroEvento.CantidadNueva					= (!datosEvento.IdTipoEventoCapital.equals("DV") && !datosEvento.IdTipoEventoCapital.equals("RC")) ? ("NOM".equals(valorTipoCartera)) ? regSalidaCusPor.CantidadProceso.multiply(datosEvento.Factor) : (valorTipoCartera.equals("PRE")) ? regSalidaCusPor.Cantidad.multiply(datosEvento.Factor).multiply(BigDecimal.valueOf(-1)) : regSalidaCusPor.Cantidad.multiply(datosEvento.Factor) : BigDecimal.ZERO;
		registroEvento.MontoCapital						= BigDecimal.ZERO;
		registroEvento.MontoInteres						= BigDecimal.ZERO;
		registroEvento.IdMoneda							= datosEvento.IdMonedaLiquidacion;
		registroEvento.SecuenciaCupon					= datosEvento.Secuencia;
		registroEvento.Factor                           = datosEvento.Factor;
		
		// registroEvento.Total - los prestatarios deben quedar negativos, ya que estos son las contrapartes de los emprestitos. 
		// siultaneas de compra (compra PH venta TP) deben quedar negativos.
		
		
		

		registroEvento.Total 							= 
		(datosEvento.IdTipoEventoCapital.equals("DV") || datosEvento.IdTipoEventoCapital.equals("RC"))
		?  (valorTipoCartera.equals("PRE") || valorTipoCartera.equals("EMP")  || valorTipoCartera.equals("SIV")  || valorTipoCartera.equals("SIC") || valorTipoCartera.equals("PAC")) 
			? (regSalidaCusPor.CantidadProceso.multiply(datosEvento.Paridad).multiply(datosEvento.Factor).multiply(new BigDecimal(-1))) 
			: (regSalidaCusPor.CantidadProceso.multiply(datosEvento.Paridad).multiply(datosEvento.Factor))  
		: BigDecimal.ZERO;
			
		

		
		
		
		registroEvento.IdBoveda							= (valorBoveda.IdBoveda != null && !valorBoveda.IdBoveda.equals("")) ? valorBoveda.IdBoveda : "DCV"; 
		registroEvento.NumeroCuentaBoveda				= valorBoveda.Codigo != null ? valorBoveda.Codigo : "";
		registroEvento.IdTipoCuentaCustodio				= valorBoveda.IdTipoCuentaCustodia != null ? valorBoveda.IdTipoCuentaCustodia : "";
		registroEvento.PagaClienteCorredor				= valorBoveda.LiquidaDividendos == "S" ? "CL" : "CO";
		registroEvento.IdBolsa							= vBolsaSistema;
		registroEvento.IdMoneda							= datosEvento.IdMonedaLiquidacion;
		registroEvento.IdUsuario						= datosEvento.IdUsuario;
		registroEvento.NumeroDividendo					= datosEvento.NumeroDividendo;
		registroEvento.IdEstado							= "PRO";
		registroEvento.IdEmpleadoEjecutivoPortafolio	= datosEjec != null ? datosEjec.IdEmpleadoEjecutivoPortafolio != null ? datosEjec.IdEmpleadoEjecutivoPortafolio : 0L : 0L;
		registroEvento.CodigoUsuarioAgente				= datosEjec != null ? datosEjec.CodigoEmpleado != null ? datosEjec.CodigoEmpleado : "" : "";
		registroEvento.NombreAgente						= datosEjec != null ? datosEjec.NombreCompletoEjecutivo != null ? datosEjec.NombreCompletoEjecutivo : "" : "";
		registroEvento.CodigoSucursal					= datosEjec != null ? datosEjec.CodigoSucursal != null ? datosEjec.CodigoSucursal : "" : "";
		registroEvento.AfectoaClearing					= "N";
		registroEvento.IdCorredorContraparte			= regSalidaCusPor.IdCorredorContraparte != null ? regSalidaCusPor.IdCorredorContraparte : 0L;
		registroEvento.IdContraparteGaratia				= regSalidaCusPor.IdContraparte;
		
		return registroEvento;
	};
	
	

	public List<EventoCapitalCliente> agruparEventoCapitalCliente(ArrayList<EventoCapitalCliente> lstEventoCapitalCliente) throws Exception{	

		List<EventoCapitalCliente> lstEventoCapitalClienteAgrupado = new ArrayList<>();
		
		MultiKeyMap hashEventoCapitalClienteAgrupado = new MultiKeyMap();

		for(EventoCapitalCliente ecc : lstEventoCapitalCliente)
		{	
			EventoCapitalCliente objEveCapCliAgrupago = (EventoCapitalCliente) hashEventoCapitalClienteAgrupado.get(ecc.IdSocioNegocio, ecc.IdCliente, ecc.IdPortafolio, ecc.TipoCartera);
				
			if (objEveCapCliAgrupago == null)
			{	// si no existe lo agregamos
				lstEventoCapitalClienteAgrupado.add(ecc);
				hashEventoCapitalClienteAgrupado.put(ecc.IdSocioNegocio, ecc.IdCliente, ecc.IdPortafolio, ecc.TipoCartera, ecc);
			}
			else
			{ 	// si ya existe lo actualizamos
				objEveCapCliAgrupago.CantidadDerecho = objEveCapCliAgrupago.CantidadDerecho.add(ecc.CantidadDerecho);
				objEveCapCliAgrupago.CantidadNueva 	 = objEveCapCliAgrupago.CantidadNueva.add(ecc.CantidadNueva);
				objEveCapCliAgrupago.Total 			 = objEveCapCliAgrupago.Total.add(ecc.Total);
				objEveCapCliAgrupago.MontoImpuestoExtranjero = objEveCapCliAgrupago.MontoImpuestoExtranjero.add(ecc.MontoImpuestoExtranjero);
			}
		}
	
		// ACTUALIZAMOS LISTA CON LAS CANTIDADES QUE CORRESPONDEN
		for(EventoCapitalCliente ecca : lstEventoCapitalClienteAgrupado)
		{	
			EventoCapitalCliente nuevoValor = (EventoCapitalCliente) hashEventoCapitalClienteAgrupado.get(ecca.IdSocioNegocio, ecca.IdCliente, ecca.IdPortafolio, ecca.TipoCartera);
			ecca.CantidadDerecho 	= nuevoValor.CantidadDerecho;
			ecca.CantidadNueva 		= nuevoValor.CantidadNueva;
			ecca.Total 				= nuevoValor.Total;
		}
 

		return lstEventoCapitalClienteAgrupado.parallelStream().filter(ecc-> ecc.CantidadDerecho.compareTo(BigDecimal.ZERO)!=0).collect(Collectors.toList());
		

	}
	//TODO: Obtener Clientes con derecho y sus contrapartes
	public SalidaClientesConDerecho getClientesConDerecho(Connection con, EventoCapital recorreEvento, String IdModulo,InformacionEmpresa empresa) throws Exception{
		SalidaClientesConDerecho out = new SalidaClientesConDerecho();
		final List<String> tipoEventoCapitalPrestamo = new ArrayList<>(Arrays.asList("DV", "DA", "RC", "EL", "EP"));
		SQL sql=new SQL();
		ArrayList<EventoCapitalCliente> datosInsertEvCapCli = new ArrayList<>();
		ArrayList<EventoCapitalCliente> datosInsertEvCapCliContraparte = new ArrayList<>();
		ArrayList<EventoCapitalCliente> datosInsertEvCapCliContraparteTr = new ArrayList<>();
		HashMap<Long,String> mapClienteImpuestoDvExt = new HashMap<Long, String>();
		String agruparEventoCapitalCliente    	= "N";
		try
		{
			agruparEventoCapitalCliente    	= ConfiguracionSistema.getParametroConfiguracionString(con, recorreEvento.IdEmpresa, "CUSTODIA", "AgruparEventoCapitalCliente");
		}
		catch(Exception ex)
		{
			agruparEventoCapitalCliente    	= "N";
		}
		

		String monedaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, recorreEvento.IdEmpresa, "PLATAFORMA", "PLA.MonedaSistema");
		
		

		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, recorreEvento.IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		String paisSistema = ConfiguracionSistema.getParametroConfiguracionString(con, recorreEvento.IdEmpresa, "PLATAFORMA", "PLA.PaisSistema");
		String fechaFlujoCustodia = recorreEvento.FechaLimite.toString();
		String pNombreEmisor= ConfiguracionSistema.getParametroConfiguracionString(con, recorreEvento.IdEmpresa, "CUSTODIA", "CUS.NombreEmisor");
		String pEntidadEmisorDestinoGtia= "SL".equals(recorreEvento.IdTipoEventoCapital) ? "N": ConfiguracionSistema.getParametroConfiguracionString(con, recorreEvento.IdEmpresa, "CUSTODIA", "CUS.EntidadEmisorDestinoGtia");
		
		//Si la fecha limite es menor a la fecha de sistema hay que revisar si es es una fecha Habil ya que el FLUCUS va a ir la cartera historica y no existe historica para dias inhabiles
		if(recorreEvento.FechaLimite.toString().compareTo(fechaSistema) != 0) 
		{
			
			boolean fechaLimiteEsDiaHabil = Feriados.esDiaHabil(con, recorreEvento.FechaLimite.toString(), Long.parseLong(paisSistema));
			
			if (!fechaLimiteEsDiaHabil)
			{   //Si NO es dia habil, busca hacia atras una fecha habil para consultar flujo custodia(Cartera historica)
				ObtenerDiaAnterior servicioObtenerDiaAnterior = new ObtenerDiaAnterior();
				InObtenerDiaAnterior entradaObtenerDiaAnterior = new InObtenerDiaAnterior();
				
				List<InObtenerDiaAnterior.FechasIn> lstFechas = new ArrayList<InObtenerDiaAnterior.FechasIn>();
				InObtenerDiaAnterior.FechasIn objFecha = new InObtenerDiaAnterior.FechasIn();		
				objFecha.fechaInicial = recorreEvento.FechaLimite.toString(); //format.format(addDays(myDate, Calendar.DATE, -1).getTime());
				objFecha.numeroDias   = 1L;
				lstFechas.add(objFecha);
				
				
				entradaObtenerDiaAnterior.idPais 	= Long.parseLong(paisSistema);
				entradaObtenerDiaAnterior.diaHabil 	= "S";
				entradaObtenerDiaAnterior.fechas 	= lstFechas;
				

				OutObtenerDiaAnterior salidaObtenerDiaAnterior = servicioObtenerDiaAnterior.logica(con, entradaObtenerDiaAnterior);

				if (salidaObtenerDiaAnterior.erroresContador.compareTo(0L) == 0)
				{
					fechaFlujoCustodia = salidaObtenerDiaAnterior.fechas.get(0).fechaCalculada;
				}
				else
				{
					fechaFlujoCustodia = recorreEvento.FechaLimite.toString();
				}
			}

		}

		Long Id = 0L;
		HashMap<String,String> mapMsg = new HashMap<>();
		{
			String localizacionSistema	= ConfiguracionSistema.getParametroConfiguracionString(con, recorreEvento.IdEmpresa, "PLATAFORMA", "PLA.LocalizacionSistema");
			String BolsaSistema			= ConfiguracionSistema.getParametroConfiguracionString(con, recorreEvento.IdEmpresa, "PLATAFORMA", "PLA.BolsaDefectoSistema");
			//Llamado a Flujo de Custodia
			List<ConsultarFlujoCustodia.FlujoUso> dato_p_uso = new ArrayList<ConsultarFlujoCustodia.FlujoUso>();
			ConsultarFlujoCustodia.FlujoUso datosFlujoUso = new ConsultarFlujoCustodia.FlujoUso();
			datosFlujoUso.UsoTipoOperacion				= "FLEVCP";
			datosFlujoUso.IdCliente						= null;
			datosFlujoUso.IdPortafolio					= null; //132695L; // null; //31224L;
			datosFlujoUso.BusquedaEspecifica			= "FLUJO_CLIENTES_DERECHO_CAPITAL";
			datosFlujoUso.IdTipoInstrumentoEspecifico	= null;
			dato_p_uso.add(datosFlujoUso);
			
			List<ConsultarFlujoCustodia.FlujoFiltrosBusqueda> dato_p_FiltrosBusqueda = new ArrayList<ConsultarFlujoCustodia.FlujoFiltrosBusqueda>();
			ConsultarFlujoCustodia.FlujoFiltrosBusqueda datosFlujoFiltrosBusqueda = new ConsultarFlujoCustodia.FlujoFiltrosBusqueda();
			datosFlujoFiltrosBusqueda.IdClasificacionTipoInstrumento	= recorreEvento.IdClasificacionTipoInstrumento;
			datosFlujoFiltrosBusqueda.IdTipoInstrumento					= recorreEvento.IdTipoInstrumento;
			datosFlujoFiltrosBusqueda.IdInstrumento						= recorreEvento.IdInstrumento;
			datosFlujoFiltrosBusqueda.IdEmisor							= null;
			datosFlujoFiltrosBusqueda.FechaVencimiento					= null;
			datosFlujoFiltrosBusqueda.IdTipoReajuste					= null;
			datosFlujoFiltrosBusqueda.EsFungible						= null;
			datosFlujoFiltrosBusqueda.IdBoveda							= null;
			dato_p_FiltrosBusqueda.add(datosFlujoFiltrosBusqueda);
			
			List<ConsultarFlujoCustodia.FlujoEsAgrupadoPor> dato_p_EsAgrupadoPor = new ArrayList<ConsultarFlujoCustodia.FlujoEsAgrupadoPor>();
			ConsultarFlujoCustodia.FlujoEsAgrupadoPor datosFlujoEsAgrupadoPor = new ConsultarFlujoCustodia.FlujoEsAgrupadoPor();
			datosFlujoEsAgrupadoPor.ClasificacionTipoInstrumento	= "S";
			datosFlujoEsAgrupadoPor.TipoInstrumento					= "S";
			datosFlujoEsAgrupadoPor.Instrumento						= "S";
			datosFlujoEsAgrupadoPor.Emisor							= "N";
			datosFlujoEsAgrupadoPor.Nemotecnico						= "N";
			datosFlujoEsAgrupadoPor.NemoSVS							= "N";
			datosFlujoEsAgrupadoPor.FechaEmisionInstrumento			= "N";
			datosFlujoEsAgrupadoPor.FechaVencimientoInstrumento		= "N";
			datosFlujoEsAgrupadoPor.FechaLiquidacionInstrumento		= "N";
			datosFlujoEsAgrupadoPor.TipoReajuste					= "N";
			datosFlujoEsAgrupadoPor.EsFungible						= "N";
			datosFlujoEsAgrupadoPor.CodigoIFRS						= "N";
			datosFlujoEsAgrupadoPor.Boveda							= "N";
			datosFlujoEsAgrupadoPor.Bolsa							= "N";
			datosFlujoEsAgrupadoPor.TipoOperacion					= "N";
			datosFlujoEsAgrupadoPor.Mesa							= "N";
			datosFlujoEsAgrupadoPor.FolioRegistro					= "N";
			datosFlujoEsAgrupadoPor.ItemOrigen						= "N";
			dato_p_EsAgrupadoPor.add(datosFlujoEsAgrupadoPor);
			
			ConsultarFlujoCustodia llamadoFlujoCustodia = new ConsultarFlujoCustodia();
			ConsultarFlujoCustodia.FlujoCustodiaEntrada paramInSP = new ConsultarFlujoCustodia.FlujoCustodiaEntrada(); 
			paramInSP.p_IdEmpresa			= recorreEvento.IdEmpresa;
			paramInSP.p_IdUsuario			= recorreEvento.IdUsuario;
			paramInSP.p_IdModulo			= IdModulo;
			
			paramInSP.p_FechaSaldo			= fechaFlujoCustodia;
			
			paramInSP.p_IdPais				= empresa.IdPais;
			paramInSP.p_DiasProyeccion		= 0;
			paramInSP.p_DiaHabil			= "S";
			paramInSP.p_ValorizarFlujo		= "S";
			paramInSP.p_Uso					= dato_p_uso;
			paramInSP.p_FiltrosBusqueda		= dato_p_FiltrosBusqueda;
			paramInSP.p_EsAgrupadoPor		= dato_p_EsAgrupadoPor;
			
			ConsultarFlujoCustodia.FlujoCustodiaSalida salidaFlujo = llamadoFlujoCustodia.logica(con, paramInSP);
			out.P_OUT_MENSAJES.addAll(salidaFlujo.P_OUT_MENSAJES);
			logger.debug("Fin ConsultarFlujoCustodia");
			
			
		
			
		
			
			//operaciones en fecha limite
			HashMap<String, Object> params = new HashMap<String, Object>();

			//List<MatrizUso> _matrizUso = sql.query(con, SQL_SELECT_MATRIZ_USO, params, MatrizUso.class);
			
			List<Long> IdCliente = new ArrayList<Long>();
			List<Long> IdPortafolio = new ArrayList<Long>();
			
			//if (_matrizUso.size()>0)
			//{	
				params.clear();
				params.put("IdEmpresa", recorreEvento.IdEmpresa);
				params.put("FechaMaxima", recorreEvento.FechaLimite);
				params.put("IdClasificacionTipoInstrumento", recorreEvento.IdClasificacionTipoInstrumento);
				params.put("IdTipoInstrumento", recorreEvento.IdTipoInstrumento);
				params.put("IdInstrumento", recorreEvento.IdInstrumento);	
				params.put("UsoTipoOperacion", "FLEVCP");
				
				List<DatosFlujoCustodia> operacionesFechaLimite = sql.query(con, SQL_SELECT_OPERACIONES_FECHA_LIMITE, params, DatosFlujoCustodia.class);				
				List<DatosFlujoCustodia> carterasNegativas = sql.query(con, SQL_SELECT_CARTERAS_NEGATIVAS, params, DatosFlujoCustodia.class);	
				
				Set<Long> foliosRegistro = salidaFlujo.p_out_FlujoDetallado.stream().filter(data -> "PRESTA".equals(data.IdTipoOperacion)).map(data -> data.FolioRegistro).collect(Collectors.toSet());
				operacionesFechaLimite = operacionesFechaLimite.stream().filter(data -> !foliosRegistro.contains(data.FolioRegistro)).collect(Collectors.toList());

				//agregamos carteras en transito al flujo
				for(DatosFlujoCustodia obj : operacionesFechaLimite) {
					IdCliente.add(obj.IdCliente);
					IdPortafolio.add(obj.IdPortafolio);
					
					FlujoCustodiaDetallado flujoDetallado = llamadoFlujoCustodia.copyPojoFlujoCustodiaDetallado(obj);
					if(flujoDetallado == null) {
						out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_ERROR", null, con, empresa.IdEmpresa));
						return out;
					}
					salidaFlujo.p_out_FlujoDetallado.add(flujoDetallado);
				}
				
				//agregamos carteras negativas al flujo
				for(DatosFlujoCustodia obj : carterasNegativas) {
					IdCliente.add(obj.IdCliente);
					IdPortafolio.add(obj.IdPortafolio);
					
					FlujoCustodiaDetallado flujoDetallado = llamadoFlujoCustodia.copyPojoFlujoCustodiaDetallado(obj);
					if(flujoDetallado == null) {
						out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_ERROR", null, con, empresa.IdEmpresa));
						return out;
					}
					salidaFlujo.p_out_FlujoDetallado.add(flujoDetallado);
				}
			//}
			
			if (salidaFlujo.p_out_FlujoDetallado.size() == 0)
			{
				mapMsg.put("p_NumeroDividendo", recorreEvento.NumeroDividendo.toString());
				out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"UI_EVENTO_CAPITAL_SIN_CLIENTES_CON_DERECHO",mapMsg, con,recorreEvento.IdEmpresa));
				return out;
			}
			if (salidaFlujo.p_out_FlujoDetallado.size()>0 ){
				HashMap<String,Object> paramDatosCliente = new HashMap<String,Object>(); 			// parametros para consulta de cliente
				HashMap<String,Object> paramDatosSocioDeNegocio = new HashMap<String,Object>(); 	// parametros para consulta de socio de negocio
				HashMap<String,Object> paramDatosEjecutivo = new HashMap<String,Object>(); 			// parametros para consulta de ejecutivo
				HashMap<String,Object> paramDatosCuentasCustodios = new HashMap<String,Object>(); 	// parametros para consulta cuentas custodio
//				HashMap<String,Object> paramDatosVendedorTransito = new HashMap<String,Object>(); 	// parametros para encontrar al vendedor de las operaciones en transito.
				
				paramDatosCuentasCustodios.put("localizacionSistema", localizacionSistema);
				
//				List<Long> IdRegistro = new ArrayList<Long>();
				
				for (ConsultarFlujoCustodia.FlujoCustodiaDetallado recorreFlujo: salidaFlujo.p_out_FlujoDetallado){
					if(IdCliente.indexOf(recorreFlujo.IdCliente) < 0)
						IdCliente.add( recorreFlujo.IdCliente);
					if(IdPortafolio.indexOf(recorreFlujo.IdPortafolio) < 0)
						IdPortafolio.add( recorreFlujo.IdPortafolio);
//					if (recorreFlujo.ItemOrigen.equals("CUSTODIA") && recorreFlujo.ItemCalculo.equals("CARTERA_TRANSITO_COMPRA")){
//						IdRegistro.add(recorreFlujo.IdRegistro);
//					}
				}
				
				
				paramDatosCliente.put("IdCliente", IdCliente);
				paramDatosCliente.put("IdEmpresa", empresa.IdEmpresa);
				paramDatosSocioDeNegocio.put("IdCliente", IdCliente);
				paramDatosEjecutivo.put("IdPortafolio", IdPortafolio);
				paramDatosCuentasCustodios.put("IdPortafolio", IdPortafolio);
//				paramDatosVendedorTransito.put("IdRegistro", IdRegistro);
				
				List<DatosCliente> datosCliente = sql.query(con, SQL_SELECT_CONSULTAR_DATOS_CLIENTE, paramDatosCliente, DatosCliente.class);
				HashMap<Long,Object> infoDatosCliente = Utils.List2Hash(datosCliente, "IdCliente");
				if (datosCliente.size() == 0){
					//out.P_OUT_ERRORESCONTADOR += 1;
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_NO_SE_ENCUENTRAN_DATOS_CLIENTE", null, con, empresa.IdEmpresa));
					return out;
				}
				logger.debug("Fin SQL_SELECT_CONSULTAR_DATOS_CLIENTE");			
				
				List<DatosSocioDeNegocio> datosSocioDeNegocio = sql.query(con, SQL_SELECT_CONSULTAR_DATOS_SOCIO_NEGOCIO, paramDatosSocioDeNegocio, DatosSocioDeNegocio.class);
				HashMap<Long,Object> infoDatosSocio = Utils.List2Hash(datosSocioDeNegocio, "IdCliente");
				logger.debug("Fin SQL_SELECT_CONSULTAR_DATOS_SOCIO_NEGOCIO");
				
				List<DatosEjecutivo> datosEjecutivo = sql.query(con, SQL_SELECT_CONSULTAR_DATOS_EJECUTIVO, paramDatosEjecutivo, DatosEjecutivo.class);
				HashMap<Long,Object> infoDatosEjecutivo = Utils.List2Hash(datosEjecutivo, "IdPortafolio");
				logger.debug("Fin SQL_SELECT_CONSULTAR_DATOS_EJECUTIVO");
				
				List<DatosCuentaCustodio> datosCuentaCustodio = sql.query(con, SQL_SELECT_CONSULTAR_DATOS_CUENTA_CUSTODIO, paramDatosCuentasCustodios, DatosCuentaCustodio.class);
//				HashMap<Long,Object> infoDatosCtaCustodio = Utils.List2Hash(datosCuentaCustodio, "IdPortafolio");
				logger.debug("Fin SQL_SELECT_CONSULTAR_DATOS_CUENTA_CUSTODIO");
					           
				//variables y datos para contraparte//////////////////////////////////////////////////////////////////////////
				HashMap<String,Object> paramsConsBoveda = new HashMap<>();
				HashMap<String, Object> paramsConsContraparteGarantia = new HashMap<>(); 
				HashMap<String, Object> paramsConsCorredorContraparte = new HashMap<>();
				HashMap<String,Object> paramsConsContraparte = new HashMap<>();
				HashMap<String,Object> paramsConsPactos = new HashMap<>();

				HashMap<String,Object> paramsConsEmprestito = new HashMap<>(1);
				
				String separarEventoCapitalClienteNominal = "N";
				separarEventoCapitalClienteNominal = ConfiguracionSistema.getParametroConfiguracionString(con, recorreEvento.IdEmpresa, "CUSTODIA", "SepararEventoCapitalClienteNominal");
				
				List<FlujoCustodiaDetallado> nominalesSinContraparte = salidaFlujo.p_out_FlujoDetallado.stream()
																				.filter(x -> {
																					try {
																						return (x.IdCorredorContraparte == null || x.IdCorredorContraparte == 0) 
																								&& "NOM".equals(obtieneValorTipoCartera(x.ItemOrigen, x.ItemCalculo ));
																					} catch (Exception e) {
																						return false;
																					}
																				})
																				.collect(Collectors.toList());
				
				if (nominalesSinContraparte.size() > 0) {
					
					List<Long> idsMovimientosTransitos = nominalesSinContraparte.stream()
																				.map(x -> x.IdRegistro)
																				.collect(Collectors.toList());
					
					HashMap<String, Object> paramsContraparteNominales = new HashMap<>();
					paramsContraparteNominales.put("idsMovimientosTransito", idsMovimientosTransitos);
					
					List<ContraparteNominales> contrapartesNominales = sql.query(con, SQL_SELECT_CONSULTAR_CONTRAPARTE_NOM, paramsContraparteNominales, ContraparteNominales.class);
					
					HashMap<Long, Object> contraparteMovHash = Utils.List2Hash(contrapartesNominales, "IdRegistro");
					
					for (FlujoCustodiaDetallado flujo : nominalesSinContraparte) {
						
						Long idRegistroFlujo = flujo.IdRegistro;

					    ContraparteNominales contraparte = (ContraparteNominales) contraparteMovHash.get(idRegistroFlujo);

					    if (contraparte != null) {
					        flujo.IdCorredorContraparte = contraparte.IdCorredorContraparte;
					    }
					}

				}
				
				
				List<String> IdBoveda = new ArrayList<String>();
				List<Long> IdContraparte = new ArrayList<Long>();
				List<Long> IdCorredorContraparte = new ArrayList<Long>();
				List<String> FechaLimite = new ArrayList<String>();
				List<String> IdTipoInstrumento = new ArrayList<String>();
				List<Long> IdInstrumento = new ArrayList<Long>();
				for (ConsultarFlujoCustodia.FlujoCustodiaDetallado ConsBov: salidaFlujo.p_out_FlujoDetallado){
					if (ConsBov.IdBoveda != null && !IdBoveda.contains(ConsBov.IdBoveda)) {
						IdBoveda.add( ConsBov.IdBoveda);
					}
					
					if (ConsBov.IdContraparte != null && !IdContraparte.contains(ConsBov.IdContraparte)) {
						IdContraparte.add(ConsBov.IdContraparte);
					}
					if (ConsBov.IdCorredorContraparte != null && !IdCorredorContraparte.contains(ConsBov.IdCorredorContraparte)) {
						IdCorredorContraparte.add(ConsBov.IdCorredorContraparte);
					}
					if (FechaLimite.size() == 0){
						FechaLimite.add(recorreEvento.FechaLimite.toString());
					}

					if (!IdTipoInstrumento.contains(ConsBov.IdTipoInstrumento)){
						IdTipoInstrumento.add(ConsBov.IdTipoInstrumento);
					}
					
					if (!IdInstrumento.contains(ConsBov.IdInstrumento)){
						IdInstrumento.add(ConsBov.IdInstrumento);
					}

				}; 
				paramsConsBoveda.put("IdBoveda", IdBoveda);
				paramsConsBoveda.put("IdEmpresa", empresa.IdEmpresa);
				
				paramsConsContraparteGarantia.put("IdContraparte", IdContraparte);
				paramsConsContraparteGarantia.put("IdEmpresa", empresa.IdEmpresa);
				paramsConsContraparteGarantia.put("p_IdInstrumento", IdInstrumento);
				paramsConsContraparteGarantia.put("p_EntidadEmisorDestinoGtia", pEntidadEmisorDestinoGtia);
				paramsConsCorredorContraparte.put("IdCorredorContraparte", IdCorredorContraparte);
				paramsConsCorredorContraparte.put("IdEmpresa", empresa.IdEmpresa);
									
				paramsConsContraparte.put("IdEmpresa", empresa.IdEmpresa);
				paramsConsContraparte.put("FechaLimite", FechaLimite);
				paramsConsContraparte.put("IdTipoInstrumento", IdTipoInstrumento);
				paramsConsContraparte.put("IdInstrumento", IdInstrumento);
				paramsConsContraparte.put("IdEmpresa", empresa.IdEmpresa);
				
				paramsConsPactos.put("IdInstrumento", IdInstrumento);
				paramsConsPactos.put("IdPortafolio",IdPortafolio);
				paramsConsEmprestito.put("FoliosRegistro", salidaFlujo.p_out_FlujoDetallado.stream().filter(data -> "PRESTA".equals(data.IdTipoOperacion))
						.map(data -> data.FolioRegistro).collect(Collectors.toList()));
				
				List<ConsultaBovedas> 		infoBoveda 				= sql.query(con, SQL_SELECT_CONSULTAR_BOVEDA, paramsConsBoveda, ConsultaBovedas.class);
				logger.debug("Fin SQL_SELECT_CONSULTAR_BOVEDA");
				List<ConsultaContraparteGarantia> 		infoContraparteGarantia		= sql.query(con, SQL_SELECT_CONSULTAR_CONTRAPARTE, paramsConsContraparteGarantia, ConsultaContraparteGarantia.class);
				logger.debug("Fin SQL_SELECT_CONSULTAR_CONTRAPARTE");
				List<ConsultaCorredorContraparte> 		infoCorredorContraparte		= sql.query(con, SQL_SELECT_CONSULTAR_CORREDOR_CONTRAPARTE, paramsConsCorredorContraparte, ConsultaCorredorContraparte.class);
				logger.debug("SQL_SELECT_CONSULTAR_CORREDOR_CONTRAPARTE");
				List<ConsultaContraparte> 	infoSimultaneaCompra	= sql.query(con, SQL_SELECT_CONSULTAR_SIMULTANEA_COMPRA, paramsConsContraparte, ConsultaContraparte.class);
				logger.debug("Fin SQL_SELECT_CONSULTAR_SIMULTANEA_COMPRA");
				List<ConsultaContraparte> 	infoSimultaneaVenta		= sql.query(con, SQL_SELECT_CONSULTAR_SIMULTANEA_VENTA, paramsConsContraparte, ConsultaContraparte.class);
				logger.debug("Fin SQL_SELECT_CONSULTAR_SIMULTANEA_VENTA");
				logger.debug("SQL_SELECT_CONSULTAR_EMPRESTITO");
				List<ConsultaContraparte> 	infoEmprestito		= sql.query(con, SQL_SELECT_EMPRESTITO_VIGENTE, paramsConsEmprestito, ConsultaContraparte.class);
				logger.debug("FIN SQL_SELECT_CONSULTAR_EMPRESTITO");
				
				Long IdPaisEmisor = 0L;
				HashMap<String,Object> paramsConsultaPaisEmisor = new HashMap<String,Object>();
				paramsConsultaPaisEmisor.put("IdEmisor", recorreEvento.IdEmisor);
				List<PaisEmisor> paisEmisorInstrumento = sql.query(con, SQL_SELECT_PAIS_EMISOR, paramsConsultaPaisEmisor, PaisEmisor.class);
				logger.debug("Fin SQL_SELECT_PAIS_EMISOR");
				if (paisEmisorInstrumento.size() > 0) {
					PaisEmisor resultado = paisEmisorInstrumento.get(0);
					IdPaisEmisor = resultado.IdPaisEmisor == null? resultado.IdPaisSistema : resultado.IdPaisEmisor;
				}

				ArrayList<String> instTipoCartera = new ArrayList<>();					
				ArrayList<String> instTipoCarteraTr = new ArrayList<>();
				
				List<Long> listaIdClientes = new ArrayList<Long>();
				for (FlujoCustodiaDetallado fce : salidaFlujo.p_out_FlujoDetallado) {
					listaIdClientes.add(fce.IdCliente);
				}
				HashMap<String,Object> paramIdsClientes = new HashMap<String,Object>();
				paramIdsClientes.put("ids", listaIdClientes);
				paramIdsClientes.put("IdPaisEmisor", IdPaisEmisor);
				List<ConsultaImpuestoDvExtranjero> consultaImpuestoDvExtranjero = sql.query(con, SQL_SELECT_CONSULTAR_EXENTOS_IMP_DV, paramIdsClientes, ConsultaImpuestoDvExtranjero.class);
				logger.debug("Fin SQL_SELECT_CONSULTAR_EXENTOS_IMP_DV");
				for (ConsultaImpuestoDvExtranjero cliente: consultaImpuestoDvExtranjero) {
					mapClienteImpuestoDvExt.put(cliente.IdCliente, cliente.AplicaImpuesto);
				}

 				boolean contienePAC = salidaFlujo.p_out_FlujoDetallado.stream().anyMatch(f -> "VENPAC".equals(f.IdTipoOperacion));
 				boolean TipoInstrumentoEsLTH = IdTipoInstrumento.stream().anyMatch(t->"LTH".equals(t));
 				
				if (contienePAC && TipoInstrumentoEsLTH) {
				    List<ConsultaPactos> infoPactosVigentes = sql.query(con, SQL_SELECT_PACTOS_VIGENTES, paramsConsPactos, ConsultaPactos.class);
				    logger.debug("Fin SQL_SELECT_CONSULTAR_SIMULTANEA_VENTA");
				    Map<Long, BigDecimal> cantidadesPorPortafolio = infoPactosVigentes.stream()
				        .filter(f -> f.IdPortafolioRelacionado != null && f.IdPortafolioRelacionado != 0L)
				        .collect(Collectors.toMap(
				            f -> f.IdPortafolioRelacionado,
				            f -> f.Cantidad,
				            BigDecimal::add
				        ));
				    
				    salidaFlujo.p_out_FlujoDetallado=salidaFlujo.p_out_FlujoDetallado.stream().filter(s->!"VENPAC".equalsIgnoreCase(s.IdTipoOperacion)).collect(Collectors.toList());
				    salidaFlujo.p_out_FlujoDetallado.forEach(flujo -> {
				        if (cantidadesPorPortafolio.containsKey(flujo.IdPortafolio)) {
				            flujo.Cantidad = flujo.Cantidad.add(cantidadesPorPortafolio.get(flujo.IdPortafolio));
				            flujo.CantidadProceso = flujo.CantidadProceso.add(cantidadesPorPortafolio.get(flujo.IdPortafolio));
				        }
				    });
				}

				//TODO: CLIENTE
				MultiKeyMap datosInsertEvCapCliHash = new MultiKeyMap();
				for (FlujoCustodiaDetallado regSalidaCusPor : salidaFlujo.p_out_FlujoDetallado){
					DatosCliente datosCli 			= (DatosCliente) infoDatosCliente.get(regSalidaCusPor.IdCliente);
					DatosSocioDeNegocio datosSocN 	= (DatosSocioDeNegocio) infoDatosSocio.get(regSalidaCusPor.IdCliente);
					DatosEjecutivo datosEjec 		= (DatosEjecutivo) infoDatosEjecutivo.get(regSalidaCusPor.IdPortafolio);
					if(regSalidaCusPor.IdContraparte == null) regSalidaCusPor.IdContraparte = 0L;
					if(regSalidaCusPor.IdCorredorContraparte == null) regSalidaCusPor.IdCorredorContraparte = 0L;
				    String valorTipoCartera = obtieneValorTipoCartera(regSalidaCusPor.ItemOrigen, regSalidaCusPor.ItemCalculo);
				    String valorBolsa 		= obtieneValorBolsa(BolsaSistema, regSalidaCusPor.IdBolsa);
				    DatosCuentaCustodio valorBoveda_ = new DatosCuentaCustodio();
				    
				    if (datosCuentaCustodio.size() == 0){
				    	valorBoveda_.IdBoveda		=  !regSalidaCusPor.IdBoveda.equals("") ? !regSalidaCusPor.IdBoveda.equals("COR") ? regSalidaCusPor.IdBoveda : "DCV" : "DCV";
				    }else{
						for (DatosCuentaCustodio regCuentaCustodio : datosCuentaCustodio){
							if (( !"SL".equals(recorreEvento.IdTipoEventoCapital) && regCuentaCustodio.IdBoveda.equals(regSalidaCusPor.IdBoveda) ) || ( "SL".equals(recorreEvento.IdTipoEventoCapital) && regCuentaCustodio.IdBoveda.equals(regSalidaCusPor.IdBoveda) && regCuentaCustodio.IdPortafolio.equals(regSalidaCusPor.IdPortafolio) )){
								valorBoveda_.IdTipoCuentaCustodia		= regCuentaCustodio.IdTipoCuentaCustodia; 
								valorBoveda_.IdBoveda					= regCuentaCustodio.IdBoveda != "" ? regCuentaCustodio.IdBoveda : "DCV";
								valorBoveda_.Codigo						= regCuentaCustodio.Codigo;						
							}else{
								continue;
							}
						};				    	
				    }
					
					if (!(regSalidaCusPor.IdCliente.compareTo((long) 0)==0)){
						EventoCapitalCliente regEvCliente = new EventoCapitalCliente();			
						String llaveIdContr = (valorTipoCartera == "GAR" ? regSalidaCusPor.IdContraparte.toString(): valorTipoCartera == "NOM" || valorTipoCartera == "TRV" || valorTipoCartera == "TRC" || valorTipoCartera == "SIC" || valorTipoCartera == "SIV" ? regSalidaCusPor.IdCorredorContraparte.toString(): "0");						
						
						
						String boveda = valorBoveda_.IdBoveda != null && !valorBoveda_.IdBoveda.equals("") ?
								valorBoveda_.IdBoveda : 
									((regEvCliente.IdBoveda == null || regEvCliente.IdBoveda.equals("")) ? 
											"DCV" : regEvCliente.IdBoveda) ;
						
						String llaveEvCapCli = (
								regSalidaCusPor.IdCliente.toString() + 
								regSalidaCusPor.IdPortafolio.toString() + 
								valorTipoCartera + 
								llaveIdContr + 
								boveda +
								valorBolsa);	
						
						
						EventoCapitalCliente evCap = (EventoCapitalCliente) datosInsertEvCapCliHash.get(llaveEvCapCli);
						
						if (evCap == null){	// si no existe lo agregamos
							regEvCliente = AgregaRegistroCliente (	recorreEvento,	regSalidaCusPor,		datosCli,			datosSocN,
																		datosEjec,		valorTipoCartera,		valorBoveda_,			BolsaSistema, 		localizacionSistema, empresa	,con);
							
							if (regEvCliente.IdSocioNegocio != null ){ // SOLO SE AGREGAN CLIENTES CON "IdSocioNegocio"
								datosInsertEvCapCli.add(regEvCliente);
								datosInsertEvCapCliHash.put(	regEvCliente.IdPortafolio, 
										regEvCliente.TipoCartera,
																regEvCliente.TipoCartera == "GAR" ? regEvCliente.IdContraparteGaratia : regEvCliente.TipoCartera == "NOM" || valorTipoCartera == "TRV" || valorTipoCartera == "TRC" || valorTipoCartera == "SIC" || valorTipoCartera == "SIV" ? regSalidaCusPor.IdCorredorContraparte : 0,
																regEvCliente.IdBoveda, 
																regEvCliente.IdBolsa,
																regEvCliente);
							}
						}else{ // si ya existe lo actualizamos
							EventoCapitalCliente nuevoValor = (EventoCapitalCliente) datosInsertEvCapCliHash.get(	regSalidaCusPor.IdPortafolio, 
																													valorTipoCartera, 
																													valorTipoCartera == "GAR" ? regSalidaCusPor.IdContraparte : valorTipoCartera == "NOM" || valorTipoCartera == "TRV" || valorTipoCartera == "TRC" || valorTipoCartera == "SIC" || valorTipoCartera == "SIV" ? regSalidaCusPor.IdCorredorContraparte : 0,
																													valorBoveda_.IdBoveda,
																													valorBolsa
																													);
							
							

							nuevoValor.CantidadDerecho 	= (nuevoValor.TipoCartera.equals("PRE") || nuevoValor.TipoCartera.equals("EMP") || valorTipoCartera.equals("SIV") || valorTipoCartera.equals("SIC")) ?		
																(nuevoValor.CantidadDerecho.add(regSalidaCusPor.CantidadProceso.multiply(new BigDecimal(-1)))) 
																:nuevoValor.CantidadDerecho.add(regSalidaCusPor.CantidadProceso);
																
							nuevoValor.CantidadNueva 	= nuevoValor.IdTipoEventoCapital.equals("DA") ||  nuevoValor.IdTipoEventoCapital.equals("EP") || nuevoValor.IdTipoEventoCapital.equals("EL")? 
																(nuevoValor.TipoCartera.equals("PRE") || nuevoValor.TipoCartera.equals("EMP"))  ? 
																		nuevoValor.CantidadNueva.add(new BigDecimal (regSalidaCusPor.Cantidad.multiply(recorreEvento.Factor).multiply(new BigDecimal(-1)).doubleValue())) 
																		:nuevoValor.CantidadNueva.add(new BigDecimal (regSalidaCusPor.Cantidad.multiply(recorreEvento.Factor).doubleValue()))
																:BigDecimal.ZERO;
							
							nuevoValor.Total 			= (nuevoValor.IdTipoEventoCapital.equals("DV") || nuevoValor.IdTipoEventoCapital.equals("RC")) ? 
									(nuevoValor.TipoCartera.equals("PRE") || nuevoValor.TipoCartera.equals("EMP")  || nuevoValor.TipoCartera.equals("SIV")  || nuevoValor.TipoCartera.equals("SIC")) 
										?(nuevoValor.Total.add(regSalidaCusPor.CantidadProceso.multiply(nuevoValor.Paridad).multiply(recorreEvento.Factor).multiply(new BigDecimal(-1)))) 
										:(nuevoValor.Total.add(regSalidaCusPor.CantidadProceso.multiply(nuevoValor.Paridad).multiply(recorreEvento.Factor))) 
									:BigDecimal.ZERO;
																		
						}
						
						if (regEvCliente.IdTipoEventoCapital.equalsIgnoreCase("DV") && mapClienteImpuestoDvExt.get(regEvCliente.IdCliente) != null && mapClienteImpuestoDvExt.get(regEvCliente.IdCliente).equalsIgnoreCase("S")) {
							BigDecimal montoImpuestoRvExtranjero = regEvCliente.Total.multiply(recorreEvento.ImpuestoDivExtranjero.divide(new BigDecimal(100), MathContext.DECIMAL128) ,MathContext.DECIMAL128);
							regEvCliente.Total = regEvCliente.Total.subtract(montoImpuestoRvExtranjero,MathContext.DECIMAL128);
							regEvCliente.ImpuestoDivExtranjero = recorreEvento.ImpuestoDivExtranjero;
							regEvCliente.MontoImpuestoExtranjero = montoImpuestoRvExtranjero;
							regEvCliente.MontoImpuestoExtranjero = MonedaFormato.formatear(con, regEvCliente.MontoImpuestoExtranjero , MonedaFormato.MONTO, recorreEvento.IdMonedaLiquidacion).valor;
						}
						
						//logica contraparte
						//registroEvento
						if ((regEvCliente.TipoCartera.equals("EMP") || regEvCliente.TipoCartera.equals("PRE") || regEvCliente.TipoCartera.equals("PAC")) && !tipoEventoCapitalPrestamo.contains(recorreEvento.IdTipoEventoCapital)){  // Para Prestamos y emprestitos no debe generar la contraparte, ya que se netean entre ellos.
							continue;
						}
						if (regEvCliente.TipoCartera.equals("TRC")){ //transacciones de compra
							for (ConsultaCorredorContraparte recorreCorredorContraparte : infoCorredorContraparte){										
								if (regEvCliente.IdCorredorContraparte != null && regEvCliente.IdCorredorContraparte.equals(recorreCorredorContraparte.IdCorredorContraparte)){
									
									if (!instTipoCarteraTr.contains(regEvCliente.TipoCartera.concat(recorreCorredorContraparte.IdSocioNegocio.toString()).concat(regEvCliente.IdCorredorContraparte.toString())))
									{	
										EventoCapitalCliente registroEvento = new EventoCapitalCliente();
										Utils.copyPojo(regEvCliente, registroEvento);
										registroEvento.TipoSocioNegocio			= "CNT";
										
										registroEvento.NombreCliente			= recorreCorredorContraparte.NombreCorredor != null ? recorreCorredorContraparte.NombreCorredor : "";
										registroEvento.DocumentoIdentidad		= recorreCorredorContraparte.DocumentoIdentidad != null ? recorreCorredorContraparte.DocumentoIdentidad : "";
										registroEvento.IdSocioNegocio			= recorreCorredorContraparte.IdSocioNegocio != null ? recorreCorredorContraparte.IdSocioNegocio : 0;
										registroEvento.IdPortafolio				= recorreCorredorContraparte.IdPortafolio != null ? recorreCorredorContraparte.IdPortafolio : 0;
										registroEvento.IdTipoDocumentoIdentidad	= recorreCorredorContraparte.IdTipoDocumentoIdentidad != null ? recorreCorredorContraparte.IdTipoDocumentoIdentidad : "R";
										registroEvento.IdEstado					= recorreCorredorContraparte.IdEstado != null ? recorreCorredorContraparte.IdEstado : "PRO";
										
										registroEvento.IdConceptoGarantia		= "";
										registroEvento.IdTipoOperacionGarantia	= "";
										registroEvento.IdBoveda					= regEvCliente.IdBoveda;
										registroEvento.NumeroCuentaBoveda		= "";
										registroEvento.IdTipoCuentaCustodio		= "";
										registroEvento.CantidadDerecho			= regEvCliente.CantidadDerecho.multiply(new BigDecimal(-1));
										registroEvento.CantidadNueva			= regEvCliente.CantidadNueva.multiply(new BigDecimal(-1));
										registroEvento.MontoCapital				= regEvCliente.MontoCapital;
										registroEvento.MontoInteres				= regEvCliente.MontoInteres;
										registroEvento.Total					= regEvCliente.Total.multiply(new BigDecimal(-1));
										registroEvento.MontoImpuestoExtranjero = regEvCliente.MontoImpuestoExtranjero.multiply(new BigDecimal(-1));
										
										
										//Los dividendos en transito de corredores contrapartes(solo CLP) afectan al clearing financiero (afectan el monto final a Pagar o Cobrar del clearing)
										registroEvento.AfectoaClearing 			= (registroEvento.IdMoneda.equalsIgnoreCase(monedaSistema) ? "S" : "N"); 
									
										
										
										datosInsertEvCapCliContraparteTr.add(registroEvento);
									
										instTipoCarteraTr.add(regEvCliente.TipoCartera.concat(recorreCorredorContraparte.IdSocioNegocio.toString()).concat(regEvCliente.IdCorredorContraparte.toString()));
									}
									else 
									{	
										int posicion = instTipoCarteraTr.indexOf(regEvCliente.TipoCartera.concat(recorreCorredorContraparte.IdSocioNegocio.toString()).concat(regEvCliente.IdCorredorContraparte.toString()));
										
										datosInsertEvCapCliContraparteTr.get(posicion).CantidadDerecho	= datosInsertEvCapCliContraparteTr.get(posicion).CantidadDerecho.add(regEvCliente.CantidadDerecho.multiply(new BigDecimal(-1)));
										datosInsertEvCapCliContraparteTr.get(posicion).CantidadNueva	= datosInsertEvCapCliContraparteTr.get(posicion).CantidadNueva.add(regEvCliente.CantidadNueva.multiply(new BigDecimal(-1)));
										datosInsertEvCapCliContraparteTr.get(posicion).MontoCapital		= datosInsertEvCapCliContraparteTr.get(posicion).MontoCapital.add(regEvCliente.MontoCapital);
										datosInsertEvCapCliContraparteTr.get(posicion).MontoInteres		= datosInsertEvCapCliContraparteTr.get(posicion).MontoInteres.add(regEvCliente.MontoInteres);
										datosInsertEvCapCliContraparteTr.get(posicion).Total			= datosInsertEvCapCliContraparteTr.get(posicion).Total.add(regEvCliente.Total.multiply(new BigDecimal(-1)));
										datosInsertEvCapCliContraparteTr.get(posicion).MontoImpuestoExtranjero = datosInsertEvCapCliContraparteTr.get(posicion).MontoImpuestoExtranjero.add(regEvCliente.MontoImpuestoExtranjero.multiply(new BigDecimal(-1)));
									}
								}
							}
						}else if(regEvCliente.TipoCartera.equals("TRV")) {
							for (ConsultaCorredorContraparte recorreCorredorContraparte : infoCorredorContraparte){										
								if (regEvCliente.IdCorredorContraparte != null && regEvCliente.IdCorredorContraparte.equals(recorreCorredorContraparte.IdCorredorContraparte)){
									
									if (!instTipoCarteraTr.contains(regEvCliente.TipoCartera.concat(recorreCorredorContraparte.IdSocioNegocio.toString()).concat(regEvCliente.IdCorredorContraparte.toString())))
									{	
										EventoCapitalCliente registroEvento = new EventoCapitalCliente();
										Utils.copyPojo(regEvCliente, registroEvento);
										registroEvento.TipoSocioNegocio			= "CNT";
										
										registroEvento.NombreCliente			= recorreCorredorContraparte.NombreCorredor != null ? recorreCorredorContraparte.NombreCorredor : "";
										registroEvento.DocumentoIdentidad		= recorreCorredorContraparte.DocumentoIdentidad != null ? recorreCorredorContraparte.DocumentoIdentidad : "";
										registroEvento.IdSocioNegocio			= recorreCorredorContraparte.IdSocioNegocio != null ? recorreCorredorContraparte.IdSocioNegocio : 0;
										registroEvento.IdPortafolio				= recorreCorredorContraparte.IdPortafolio != null ? recorreCorredorContraparte.IdPortafolio : 0;
										registroEvento.IdTipoDocumentoIdentidad	= recorreCorredorContraparte.IdTipoDocumentoIdentidad != null ? recorreCorredorContraparte.IdTipoDocumentoIdentidad : "R";
										registroEvento.IdEstado					= recorreCorredorContraparte.IdEstado != null ? recorreCorredorContraparte.IdEstado : "PRO";
										
										registroEvento.IdConceptoGarantia		= "";
										registroEvento.IdTipoOperacionGarantia	= "";
										registroEvento.IdBoveda					= regEvCliente.IdBoveda;
										registroEvento.NumeroCuentaBoveda		= "";
										registroEvento.IdTipoCuentaCustodio		= "";
										registroEvento.CantidadDerecho			= regEvCliente.CantidadDerecho;
										registroEvento.CantidadNueva			= regEvCliente.CantidadNueva;
										registroEvento.MontoCapital				= regEvCliente.MontoCapital;
										registroEvento.MontoInteres				= regEvCliente.MontoInteres;
										registroEvento.Total					= regEvCliente.Total;
										registroEvento.MontoImpuestoExtranjero = regEvCliente.MontoImpuestoExtranjero.multiply(new BigDecimal(-1));
										
										//Los dividendos en transito de corredores contrapartes(solo CLP) afectan al clearing financiero (afectan el monto final a Pagar o Cobrar del clearing)
										registroEvento.AfectoaClearing 			= (registroEvento.IdMoneda.equalsIgnoreCase(monedaSistema) ? "S" : "N"); 
										
										 
										
										
										datosInsertEvCapCliContraparteTr.add(registroEvento);
									
										instTipoCarteraTr.add(regEvCliente.TipoCartera.concat(recorreCorredorContraparte.IdSocioNegocio.toString()).concat(regEvCliente.IdCorredorContraparte.toString()));
									}
									else 
									{	
										int posicion = instTipoCarteraTr.indexOf(regEvCliente.TipoCartera.concat(recorreCorredorContraparte.IdSocioNegocio.toString()).concat(regEvCliente.IdCorredorContraparte.toString()));
										
										datosInsertEvCapCliContraparteTr.get(posicion).CantidadDerecho	= datosInsertEvCapCliContraparteTr.get(posicion).CantidadDerecho.add(regEvCliente.CantidadDerecho);
										datosInsertEvCapCliContraparteTr.get(posicion).CantidadNueva	= datosInsertEvCapCliContraparteTr.get(posicion).CantidadNueva.add(regEvCliente.CantidadNueva);
										datosInsertEvCapCliContraparteTr.get(posicion).MontoCapital		= datosInsertEvCapCliContraparteTr.get(posicion).MontoCapital.add(regEvCliente.MontoCapital);
										datosInsertEvCapCliContraparteTr.get(posicion).MontoInteres		= datosInsertEvCapCliContraparteTr.get(posicion).MontoInteres.add(regEvCliente.MontoInteres);
										datosInsertEvCapCliContraparteTr.get(posicion).Total			= datosInsertEvCapCliContraparteTr.get(posicion).Total.add(regEvCliente.Total);
										datosInsertEvCapCliContraparteTr.get(posicion).MontoImpuestoExtranjero = datosInsertEvCapCliContraparteTr.get(posicion).MontoImpuestoExtranjero.add(regEvCliente.MontoImpuestoExtranjero.multiply(new BigDecimal(-1)));
									}
								}
							}
							
							if (!instTipoCartera.contains(regEvCliente.TipoCartera.concat(regEvCliente.IdContraparteGaratia != null ? regEvCliente.IdContraparteGaratia.toString() : ""))){
								EventoCapitalCliente registroEventoDCV = new EventoCapitalCliente();
								Utils.copyPojo(regEvCliente, registroEventoDCV);
								registroEventoDCV.IdSocioNegocio			= 0L;
								registroEventoDCV.TipoSocioNegocio			= "CNT";
								registroEventoDCV.IdPortafolio				= 0L;
								registroEventoDCV.IdTipoDocumentoIdentidad	= "R";
								registroEventoDCV.DocumentoIdentidad		= "";
								for (ConsultaBovedas recorreBoveda : infoBoveda){
									if (registroEventoDCV.IdBoveda != null && registroEventoDCV.IdBoveda.equals(recorreBoveda.IdBoveda)){
										registroEventoDCV.NombreCliente			= recorreBoveda.NombreCliente != null ? recorreBoveda.NombreCliente : "";
										registroEventoDCV.DocumentoIdentidad		= recorreBoveda.DocumentoIdentidad != null ? recorreBoveda.DocumentoIdentidad : "";
										registroEventoDCV.IdSocioNegocio			= recorreBoveda.IdSocioNegocio != null ? recorreBoveda.IdSocioNegocio : 0;
										registroEventoDCV.IdPortafolio				= recorreBoveda.IdPortafolio != null ? recorreBoveda.IdPortafolio : 0;
										registroEventoDCV.IdTipoDocumentoIdentidad	= recorreBoveda.IdTipoDocumentoIdentidad != null ? recorreBoveda.IdTipoDocumentoIdentidad : "R";
									}
								}
								registroEventoDCV.IdCliente				= 0L;
								registroEventoDCV.IdConceptoGarantia		= "";
								registroEventoDCV.IdTipoOperacionGarantia	= "";
								registroEventoDCV.IdBoveda					= regEvCliente.IdBoveda != null ? regEvCliente.IdBoveda : "CIA";
								registroEventoDCV.NumeroCuentaBoveda		= "";
								registroEventoDCV.IdTipoCuentaCustodio		= "";
								registroEventoDCV.CantidadDerecho			= regEvCliente.TipoCartera.equals("SIC") ? regEvCliente.CantidadDerecho : regEvCliente.CantidadDerecho.multiply(new BigDecimal(-1));
								registroEventoDCV.CantidadNueva			    = regEvCliente.TipoCartera.equals("SIC") ? regEvCliente.CantidadNueva : regEvCliente.CantidadNueva.multiply(new BigDecimal(-1));
								registroEventoDCV.MontoCapital				= regEvCliente.MontoCapital;
								registroEventoDCV.MontoInteres				= regEvCliente.MontoInteres;
								registroEventoDCV.Total					= regEvCliente.Total.multiply(new BigDecimal(-1));
								registroEventoDCV.MontoImpuestoExtranjero = regEvCliente.MontoImpuestoExtranjero.multiply(new BigDecimal(-1));
								
								datosInsertEvCapCliContraparte.add(registroEventoDCV);
								instTipoCartera.add(regEvCliente.TipoCartera.concat(regEvCliente.IdContraparteGaratia != null ? regEvCliente.IdContraparteGaratia.toString() : ""));
							}
							else {
								int posicion = instTipoCartera.indexOf(regEvCliente.TipoCartera.concat(regEvCliente.IdContraparteGaratia != null ? regEvCliente.IdContraparteGaratia.toString() : ""));
								datosInsertEvCapCliContraparte.get(posicion).CantidadDerecho	= datosInsertEvCapCliContraparte.get(posicion).CantidadDerecho.add(datosInsertEvCapCliContraparte.get(posicion).TipoCartera.equals("SIC") ? regEvCliente.CantidadDerecho : regEvCliente.CantidadDerecho.multiply(new BigDecimal(-1)));
								datosInsertEvCapCliContraparte.get(posicion).CantidadNueva		= datosInsertEvCapCliContraparte.get(posicion).CantidadNueva.add(datosInsertEvCapCliContraparte.get(posicion).TipoCartera.equals("SIC") ? regEvCliente.CantidadNueva : regEvCliente.CantidadNueva.multiply(new BigDecimal(-1)));
								datosInsertEvCapCliContraparte.get(posicion).MontoCapital		= datosInsertEvCapCliContraparte.get(posicion).MontoCapital.add(regEvCliente.MontoCapital);
								datosInsertEvCapCliContraparte.get(posicion).MontoInteres		= datosInsertEvCapCliContraparte.get(posicion).MontoInteres.add(regEvCliente.MontoInteres);
								datosInsertEvCapCliContraparte.get(posicion).Total				= datosInsertEvCapCliContraparte.get(posicion).Total.add(datosInsertEvCapCliContraparte.get(posicion).TipoCartera.equals("SIC") ? regEvCliente.Total : regEvCliente.Total.multiply(new BigDecimal(-1)));
								datosInsertEvCapCliContraparte.get(posicion).MontoImpuestoExtranjero = datosInsertEvCapCliContraparte.get(posicion).MontoImpuestoExtranjero.add(regEvCliente.MontoImpuestoExtranjero.multiply(new BigDecimal(-1)));
							}
						}else{
							
							if (!instTipoCartera.contains(regEvCliente.TipoCartera.concat(regEvCliente.IdContraparteGaratia != null ? regEvCliente.IdContraparteGaratia.toString() : "").concat(regEvCliente.IdCorredorContraparte != null ? regEvCliente.IdCorredorContraparte.toString() : ""))){
							
								EventoCapitalCliente registroEvento = new EventoCapitalCliente();
								Utils.copyPojo(regEvCliente, registroEvento);
								registroEvento.IdSocioNegocio			= 0L;
								registroEvento.TipoSocioNegocio			= "CNT";
								registroEvento.IdPortafolio				= 0L;
								registroEvento.IdTipoDocumentoIdentidad	= "R";
								registroEvento.DocumentoIdentidad		= "";
							////cambio custodia boveda aes
								if (regEvCliente.TipoCartera.equals("CUS") || regEvCliente.TipoCartera.equals("NEG")){
									
									for (ConsultaBovedas recorreBoveda : infoBoveda){
										String Parametro = "N";
										String DocumentoIdentidadEmisor = " ";
										String NombreClienteEmisor = " ";
										params.clear();
										List<ProcesarEventoCapitalEntrada> Parametros = sql.query(con, SQL_SELECT_PARAMETRO, params, ProcesarEventoCapitalEntrada.class);
										params.put("p_IdInstrumento", IdInstrumento);
										List<ConsultaContraparte> Emisor = sql.query(con, SQL_SELECT_EMISOR, params, ConsultaContraparte.class);
										
										for(ProcesarEventoCapitalEntrada Parametr : Parametros){
											 Parametro = Parametr.p_Prametro; 
										}
										for(ConsultaContraparte Emiso : Emisor){
											DocumentoIdentidadEmisor = Emiso.DocumentoIdentidadEmisor; 
											NombreClienteEmisor = Emiso.NombreClienteEmisor; 
										}
										
										if (Parametro.equalsIgnoreCase("S")){
											//Se pide esta configuracion si esta prendido el parametro de tanner 
											
											if (registroEvento.IdBoveda != null && registroEvento.IdBoveda.equals(recorreBoveda.IdBoveda)){
												if (recorreBoveda.IdBoveda.equalsIgnoreCase("DCV")){ 
													
													registroEvento.NombreCliente			= NombreClienteEmisor;
													registroEvento.DocumentoIdentidad		= DocumentoIdentidadEmisor; 
													registroEvento.IdSocioNegocio			= recorreBoveda.IdSocioNegocio != null ? recorreBoveda.IdSocioNegocio : 0;
													registroEvento.IdPortafolio				= recorreBoveda.IdPortafolio != null ? recorreBoveda.IdPortafolio : 0;
													registroEvento.IdTipoDocumentoIdentidad	= recorreBoveda.IdTipoDocumentoIdentidad != null ? recorreBoveda.IdTipoDocumentoIdentidad : "R";
												}else{
													registroEvento.NombreCliente			= recorreBoveda.NombreCliente != null ? recorreBoveda.NombreCliente : "";
													registroEvento.DocumentoIdentidad		= recorreBoveda.DocumentoIdentidad != null ? recorreBoveda.DocumentoIdentidad : "";
													registroEvento.IdSocioNegocio			= recorreBoveda.IdSocioNegocio != null ? recorreBoveda.IdSocioNegocio : 0;
													registroEvento.IdPortafolio				= recorreBoveda.IdPortafolio != null ? recorreBoveda.IdPortafolio : 0;
													registroEvento.IdTipoDocumentoIdentidad	= recorreBoveda.IdTipoDocumentoIdentidad != null ? recorreBoveda.IdTipoDocumentoIdentidad : "R";
												}
											}
										}else{
											registroEvento.NombreCliente			= recorreBoveda.NombreCliente != null ? recorreBoveda.NombreCliente : "";
											registroEvento.DocumentoIdentidad		= recorreBoveda.DocumentoIdentidad != null ? recorreBoveda.DocumentoIdentidad : "";
											registroEvento.IdSocioNegocio			= recorreBoveda.IdSocioNegocio != null ? recorreBoveda.IdSocioNegocio : 0;
											registroEvento.IdPortafolio				= recorreBoveda.IdPortafolio != null ? recorreBoveda.IdPortafolio : 0;
											registroEvento.IdTipoDocumentoIdentidad	= recorreBoveda.IdTipoDocumentoIdentidad != null ? recorreBoveda.IdTipoDocumentoIdentidad : "R";
										}
									}							
									
								}else if (regEvCliente.TipoCartera.equals("NOM")){
									for (ConsultaCorredorContraparte recorreCorredorContraparte : infoCorredorContraparte){										
										if (registroEvento.IdCorredorContraparte != null && registroEvento.IdCorredorContraparte.equals(recorreCorredorContraparte.IdCorredorContraparte)){
											registroEvento.NombreCliente			= recorreCorredorContraparte.NombreCorredor != null ? recorreCorredorContraparte.NombreCorredor : "";
											registroEvento.DocumentoIdentidad		= recorreCorredorContraparte.DocumentoIdentidad != null ? recorreCorredorContraparte.DocumentoIdentidad : "";
											registroEvento.IdSocioNegocio			= recorreCorredorContraparte.IdSocioNegocio != null ? recorreCorredorContraparte.IdSocioNegocio : 0;
											registroEvento.IdPortafolio				= recorreCorredorContraparte.IdPortafolio != null ? recorreCorredorContraparte.IdPortafolio : 0;
											registroEvento.IdTipoDocumentoIdentidad	= recorreCorredorContraparte.IdTipoDocumentoIdentidad != null ? recorreCorredorContraparte.IdTipoDocumentoIdentidad : "R";
										}
									}
								
								}else if (regEvCliente.TipoCartera.equals("GAR")){
									if (empresa.CodigoPais.equals("PE")){ // Peru
										for (ConsultaBovedas recorreBoveda : infoBoveda){
											if (registroEvento.IdBoveda != null && registroEvento.IdBoveda.equals(recorreBoveda.IdBoveda)){
												registroEvento.NombreCliente			= recorreBoveda.NombreCliente != null ? recorreBoveda.NombreCliente : "";
												registroEvento.DocumentoIdentidad		= recorreBoveda.DocumentoIdentidad != null ? recorreBoveda.DocumentoIdentidad : "";
												registroEvento.IdSocioNegocio			= recorreBoveda.IdSocioNegocio != null ? recorreBoveda.IdSocioNegocio : 0; 
												registroEvento.IdPortafolio				= recorreBoveda.IdPortafolio != null ? recorreBoveda.IdPortafolio : 0;
												registroEvento.IdTipoDocumentoIdentidad	= recorreBoveda.IdTipoDocumentoIdentidad != null ? recorreBoveda.IdTipoDocumentoIdentidad : "R";
											}
										}
									}else { // Chile
										for (ConsultaContraparteGarantia recorreContraparte : infoContraparteGarantia){
											if (registroEvento.IdContraparteGaratia != null && registroEvento.IdContraparteGaratia.equals(recorreContraparte.IdContraparte)){
												registroEvento.NombreCliente			= recorreContraparte.NombreCliente != null ? recorreContraparte.NombreCliente : "";
												registroEvento.DocumentoIdentidad		= recorreContraparte.DocumentoIdentidad != null ? recorreContraparte.DocumentoIdentidad : "";
												registroEvento.IdSocioNegocio			= recorreContraparte.IdSocioNegocio != null ? recorreContraparte.IdSocioNegocio : 0;												
												registroEvento.IdTipoDocumentoIdentidad	= recorreContraparte.IdTipoDocumentoIdentidad != null ? recorreContraparte.IdTipoDocumentoIdentidad : "R";
												registroEvento.IdEstado					= recorreContraparte.IdEstado != null ? recorreContraparte.IdEstado : "PRO";
											}
										}
									}
								}else if (regEvCliente.TipoCartera.equals("SIC")){ 
									for (ConsultaContraparte recorreSimulC : infoSimultaneaCompra){
										if (registroEvento.IdCorredorContraparte != null && registroEvento.IdCorredorContraparte.equals(recorreSimulC.IdCorredorContraparte)){
											registroEvento.NombreCliente			= recorreSimulC.NombreCliente != null ? recorreSimulC.NombreCliente : "";
											registroEvento.DocumentoIdentidad		= recorreSimulC.DocumentoIdentidad != null ? recorreSimulC.DocumentoIdentidad : "";
											registroEvento.IdSocioNegocio			= recorreSimulC.IdSocioNegocio != null ? recorreSimulC.IdSocioNegocio : 0;
											registroEvento.IdPortafolio				= recorreSimulC.IdPortafolio != null ? recorreSimulC.IdPortafolio : 0;
											registroEvento.IdTipoDocumentoIdentidad	= recorreSimulC.IdTipoDocumentoIdentidad != null ? recorreSimulC.IdTipoDocumentoIdentidad : "R";
											registroEvento.IdEstado					= recorreSimulC.IdEstado != null ? recorreSimulC.IdEstado : "PRO";
										}
									}	
								}else if (regEvCliente.TipoCartera.equals("SIV")){ 
									for (ConsultaContraparte recorreSimulV : infoSimultaneaVenta){
										if (registroEvento.IdCorredorContraparte != null && registroEvento.IdCorredorContraparte.equals(recorreSimulV.IdCorredorContraparte)){	
											registroEvento.NombreCliente			= recorreSimulV.NombreCliente != null ? recorreSimulV.NombreCliente : "";
											registroEvento.DocumentoIdentidad		= recorreSimulV.DocumentoIdentidad != null ? recorreSimulV.DocumentoIdentidad : "";
											registroEvento.IdSocioNegocio			= recorreSimulV.IdSocioNegocio != null ? recorreSimulV.IdSocioNegocio : 0;
											registroEvento.IdPortafolio				= recorreSimulV.IdPortafolio != null ? recorreSimulV.IdPortafolio : 0;
											registroEvento.IdTipoDocumentoIdentidad	= recorreSimulV.IdTipoDocumentoIdentidad != null ? recorreSimulV.IdTipoDocumentoIdentidad : "R";
											registroEvento.IdEstado					= recorreSimulV.IdEstado != null ? recorreSimulV.IdEstado : "PRO";
										}
									}
								} else if (regEvCliente.TipoCartera.equals("PRE")) {
									Long folioCartera = regSalidaCusPor.FolioRegistro;
									Long idCorredorContraparte = regEvCliente.IdCorredorContraparte;
									
									List<ConsultaContraparte> infoContraparte = infoEmprestito.stream().filter(data -> data.FolioRegistro.equals(folioCartera)).collect(Collectors.toList());
									ConsultaCorredorContraparte corredorContraparte =  infoCorredorContraparte.stream().filter(data -> data.IdCorredorContraparte.equals(idCorredorContraparte)).findFirst().orElse(null);
									
									if(infoContraparte.size() == 0 && corredorContraparte == null) {
										continue;
									} else if(infoContraparte.size() > 0){
										for(ConsultaContraparte cc : infoContraparte) {
											EventoCapitalCliente registroEventoPRE = obtenerContrapartePrestamista(regEvCliente);
											registroEventoPRE.TipoSocioNegocio			= "CNT";
											registroEventoPRE.NombreCliente			    = cc.NombreCliente != null ? cc.NombreCliente : "";
											registroEventoPRE.DocumentoIdentidad		= cc.DocumentoIdentidad != null ? cc.DocumentoIdentidad : "";
											registroEventoPRE.IdSocioNegocio			= cc.IdSocioNegocio != null ? cc.IdSocioNegocio : 0;
											registroEventoPRE.IdPortafolio				= cc.IdPortafolio != null ? cc.IdPortafolio : 0;
											registroEventoPRE.IdTipoDocumentoIdentidad	= cc.IdTipoDocumentoIdentidad != null ? cc.IdTipoDocumentoIdentidad : "R";
											registroEventoPRE.IdEstado					= "PRO";
											registroEventoPRE.IdCorredorContraparte     = cc.IdCorredorContraparte == null ? 0L : cc.IdCorredorContraparte;
											regEvCliente.IdCorredorContraparte          = cc.IdCorredorContraparte == null ? 0L : cc.IdCorredorContraparte;
											
											registroEventoPRE.IdCliente				    = 0L;
											registroEventoPRE.IdConceptoGarantia		= "GAR".equals(regEvCliente.TipoCartera) ? registroEvento.IdConceptoGarantia : "";
											registroEventoPRE.IdTipoOperacionGarantia	= "";
											registroEventoPRE.IdBoveda					= registroEvento.IdBoveda != null ? registroEvento.IdBoveda : "CIA";
											registroEventoPRE.NumeroCuentaBoveda		= "";
											registroEventoPRE.IdTipoCuentaCustodio		= "";
											registroEventoPRE.CantidadDerecho			= cc.Cantidad;
											registroEventoPRE.CantidadNueva			    = BigDecimal.ZERO;
											registroEventoPRE.MontoCapital				= BigDecimal.ZERO;
											registroEventoPRE.MontoInteres				= BigDecimal.ZERO;
											registroEventoPRE.Total					    = BigDecimal.ZERO;
											registroEventoPRE.TipoCartera               = "EMP";
											registroEventoPRE.MontoImpuestoExtranjero   = registroEvento.MontoImpuestoExtranjero.multiply(new BigDecimal(-1));
											if((!regEvCliente.IdTipoEventoCapital.equals("DV") && !regEvCliente.IdTipoEventoCapital.equals("RC"))) {
												registroEventoPRE.CantidadNueva = cc.Cantidad.multiply(regEvCliente.Factor);
											} else if((regEvCliente.IdTipoEventoCapital.equals("DV") || regEvCliente.IdTipoEventoCapital.equals("RC"))) {
												registroEventoPRE.Total = cc.Cantidad.multiply(regEvCliente.Paridad).multiply(regEvCliente.Factor);
											}
											datosInsertEvCapCliContraparte.add(registroEventoPRE);
											
											if (regEvCliente.TipoCartera.equals("NOM") && separarEventoCapitalClienteNominal.equals("S")) {
												instTipoCartera.add(regEvCliente.TipoCartera.concat(regEvCliente.IdContraparteGaratia != null ? regEvCliente.IdContraparteGaratia.toString() : "").concat(regEvCliente.IdCorredorContraparte != null ? regEvCliente.IdCorredorContraparte.toString() : "").concat(regEvCliente.CantidadDerecho.doubleValue() > 0 ? "P": "N"));
											}
											else
											{
												instTipoCartera.add(regEvCliente.TipoCartera.concat(regEvCliente.IdContraparteGaratia != null ? regEvCliente.IdContraparteGaratia.toString() : "").concat(regEvCliente.IdCorredorContraparte != null || regEvCliente.IdCorredorContraparte == 0L ? registroEventoPRE.IdSocioNegocio.toString() : ""));
											}
										}
										continue;
									} else if (corredorContraparte != null) {
										registroEvento.TipoSocioNegocio			= "CNT";
										registroEvento.NombreCliente			= corredorContraparte.NombreCorredor != null ? corredorContraparte.NombreCorredor : "";
										registroEvento.DocumentoIdentidad		= corredorContraparte.DocumentoIdentidad != null ? corredorContraparte.DocumentoIdentidad : "";
										registroEvento.IdSocioNegocio			= corredorContraparte.IdSocioNegocio != null ? corredorContraparte.IdSocioNegocio : 0;
										registroEvento.IdPortafolio				= corredorContraparte.IdPortafolio != null ? corredorContraparte.IdPortafolio : 0;
										registroEvento.IdTipoDocumentoIdentidad	= corredorContraparte.IdTipoDocumentoIdentidad != null ? corredorContraparte.IdTipoDocumentoIdentidad : "R";
										registroEvento.IdEstado					= corredorContraparte.IdEstado != null ? corredorContraparte.IdEstado : "PRO";
										registroEvento.IdCorredorContraparte    = corredorContraparte.IdCorredorContraparte != null ? corredorContraparte.IdCorredorContraparte : 0;
										regEvCliente.IdCorredorContraparte      = corredorContraparte.IdCorredorContraparte != null ? corredorContraparte.IdCorredorContraparte : 0;
									}
									registroEvento.TipoCartera                  = "EMP";
								}
								registroEvento.IdCliente				= 0L;
								registroEvento.IdConceptoGarantia		= "GAR".equals(regEvCliente.TipoCartera) ? registroEvento.IdConceptoGarantia : "";
								registroEvento.IdTipoOperacionGarantia	= "";
								registroEvento.IdBoveda					= registroEvento.IdBoveda != null ? registroEvento.IdBoveda : "CIA";
								registroEvento.NumeroCuentaBoveda		= "";
								registroEvento.IdTipoCuentaCustodio		= "";
								registroEvento.CantidadDerecho			= registroEvento.CantidadDerecho.multiply(new BigDecimal(-1));
								registroEvento.CantidadNueva			= registroEvento.CantidadNueva.multiply(new BigDecimal(-1));
								registroEvento.MontoCapital				= registroEvento.MontoCapital;
								registroEvento.MontoInteres				= registroEvento.MontoInteres;
								registroEvento.Total					= registroEvento.Total.multiply(new BigDecimal(-1));
								registroEvento.MontoImpuestoExtranjero = registroEvento.MontoImpuestoExtranjero.multiply(new BigDecimal(-1));
								
								datosInsertEvCapCliContraparte.add(registroEvento);

								if (regEvCliente.TipoCartera.equals("NOM") && separarEventoCapitalClienteNominal.equals("S")) {
									instTipoCartera.add(regEvCliente.TipoCartera.concat(regEvCliente.IdContraparteGaratia != null ? regEvCliente.IdContraparteGaratia.toString() : "").concat(regEvCliente.IdCorredorContraparte != null ? regEvCliente.IdCorredorContraparte.toString() : "").concat(regEvCliente.CantidadDerecho.doubleValue() > 0 ? "P": "N"));
								}
								else
								{
									instTipoCartera.add(regEvCliente.TipoCartera.concat(regEvCliente.IdContraparteGaratia != null ? regEvCliente.IdContraparteGaratia.toString() : "").concat(regEvCliente.IdCorredorContraparte != null ? regEvCliente.IdCorredorContraparte.toString() : ""));
								}
								
							}else{
								int posicion = instTipoCartera.indexOf(regEvCliente.TipoCartera.concat(regEvCliente.IdContraparteGaratia != null ? regEvCliente.IdContraparteGaratia.toString() : "").concat(regEvCliente.IdCorredorContraparte != null ? regEvCliente.IdCorredorContraparte.toString() : ""));
								datosInsertEvCapCliContraparte.get(posicion).CantidadDerecho	= datosInsertEvCapCliContraparte.get(posicion).CantidadDerecho.add(regEvCliente.CantidadDerecho.multiply(new BigDecimal(-1)));
								datosInsertEvCapCliContraparte.get(posicion).CantidadNueva		= datosInsertEvCapCliContraparte.get(posicion).CantidadNueva.add(regEvCliente.CantidadNueva.multiply(new BigDecimal(-1)));
								datosInsertEvCapCliContraparte.get(posicion).MontoCapital		= datosInsertEvCapCliContraparte.get(posicion).MontoCapital.add(regEvCliente.MontoCapital);
								datosInsertEvCapCliContraparte.get(posicion).MontoInteres		= datosInsertEvCapCliContraparte.get(posicion).MontoInteres.add(regEvCliente.MontoInteres);
								datosInsertEvCapCliContraparte.get(posicion).Total				= datosInsertEvCapCliContraparte.get(posicion).Total.add(regEvCliente.Total.multiply(new BigDecimal(-1)));
								datosInsertEvCapCliContraparte.get(posicion).MontoImpuestoExtranjero = datosInsertEvCapCliContraparte.get(posicion).MontoImpuestoExtranjero.add(regEvCliente.MontoImpuestoExtranjero.multiply(new BigDecimal(-1)));
							}
						}
						//
						
					}
				}
				if (datosInsertEvCapCli.size() == 0){
					
					mapMsg.clear();
					mapMsg.put("p_NumeroDividendo", recorreEvento.NumeroDividendo.toString());
					out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ADVERTENCIA,"UI_EVENTO_CAPITAL_SIN_CLIENTES_CON_DERECHO",mapMsg, con,recorreEvento.IdEmpresa));
					Id++;
					return out;
				}

				//GUARDA LAS CARTERAS RELACIONADAS A CADA EVENTO CAPITAL CLIENTE
				//EventoCapitalClienteCartera
				List<EventoCapitalClienteCartera> insertEventoCapitalClienteCartera = new ArrayList<EventoCapitalClienteCartera>();
				String hash = "";
				HashMap<String, Boolean> EventoCapitalClienteCarteraFiltro = new HashMap<String, Boolean>();
				
				List<Long> secuenciaEventoCapitalCliente= Utils.getSecuencia(con, "BSCL.Custodia.BD.Secuencias::EventoCapitalCliente", new Long(datosInsertEvCapCli.size()));
				for (EventoCapitalCliente recorreEvCapCli: datosInsertEvCapCli){
					recorreEvCapCli.IdEventoCapitalCliente = secuenciaEventoCapitalCliente.get(datosInsertEvCapCli.indexOf(recorreEvCapCli));
					for(ConsultarFlujoCustodia.FlujoCustodiaDetallado recorreFlujo: salidaFlujo.p_out_FlujoDetallado) {
						if(recorreEvCapCli.IdCliente.equals(recorreFlujo.IdCliente) && recorreEvCapCli.IdPortafolio.equals(recorreFlujo.IdPortafolio) 
								&& recorreEvCapCli.IdTipoInstrumento.equals(recorreFlujo.IdTipoInstrumento) && recorreEvCapCli.IdInstrumento.equals(recorreFlujo.IdInstrumento)
								&& ((recorreEvCapCli.TipoCartera.equalsIgnoreCase(recorreFlujo.ItemOrigen.substring(0,3)) && "FLUJO_CLIENTES_DERECHO_CAPITAL".equalsIgnoreCase(recorreFlujo.BusquedaEspecifica))
								|| ("NOM".equalsIgnoreCase(recorreEvCapCli.TipoCartera) && !"FLUJO_CLIENTES_DERECHO_CAPITAL".equalsIgnoreCase(recorreFlujo.BusquedaEspecifica)))) {
							EventoCapitalClienteCartera eventoCapitalClienteCartera = new EventoCapitalClienteCartera();
							eventoCapitalClienteCartera.IdEventoCapitalClienteCartera = recorreEvCapCli.IdEventoCapitalCliente.toString() + '_' + recorreFlujo.IdRegistro.toString();
							eventoCapitalClienteCartera.IdEventoCapitalCliente = recorreEvCapCli.IdEventoCapitalCliente;
							eventoCapitalClienteCartera.IdCartera = recorreFlujo.IdRegistro;
							
							hash = recorreEvCapCli.IdEventoCapitalCliente.toString() + '_' + recorreFlujo.IdRegistro.toString();
							
							if (!EventoCapitalClienteCarteraFiltro.containsKey(hash)) {
								EventoCapitalClienteCarteraFiltro.put(hash, true);
								insertEventoCapitalClienteCartera.add(eventoCapitalClienteCartera);
							}
							 
							
						}
					}
				}
				
				sql.batch(con, SQL_INSERT_EVENTO_CAPITAL_CLIENTE_CARTERA, insertEventoCapitalClienteCartera, EventoCapitalClienteCartera.class);
			}
		}
		//sacamos los TRV de los clientes, los usamos solo para procesar las contrapartes correspondientes
		datosInsertEvCapCli = (ArrayList<EventoCapitalCliente>) datosInsertEvCapCli.stream().filter(p -> !p.TipoCartera.equals("TRV")).collect(Collectors.toList());
		//Agrupa ECC para genera solo un registro por cliente-portafolio
		if (agruparEventoCapitalCliente.equalsIgnoreCase("S"))
		{
			out.P_OUT_CLIENTES.addAll(agruparEventoCapitalCliente(datosInsertEvCapCli));
			for (EventoCapitalCliente recorreEvCapCliCNT: datosInsertEvCapCliContraparte) {
				out.P_OUT_CONTRAPARTES.add(recorreEvCapCliCNT);
			}			
			out.P_OUT_CONTRAPARTES.addAll(agruparEventoCapitalCliente(datosInsertEvCapCliContraparteTr));
		}
		else
		{	// No agrupa ECC
			out.P_OUT_CLIENTES.addAll(datosInsertEvCapCli);
			out.P_OUT_CONTRAPARTES.addAll(datosInsertEvCapCliContraparte);
			out.P_OUT_CONTRAPARTES.addAll(datosInsertEvCapCliContraparteTr);
		}
		
		if (recorreEvento.IdTipoEventoCapital.equalsIgnoreCase("DV") && datosInsertEvCapCli.size() > 0) {					
			out = AgregarGastoTransferencia(con, out, recorreEvento);
		}
	
		BigDecimal newValue ;
		for(EventoCapitalCliente _datosEvCapCli : out.P_OUT_CLIENTES){
			newValue = BigDecimal.ZERO;
			newValue =  _datosEvCapCli.CantidadDerecho.setScale(0, RoundingMode.DOWN);
			_datosEvCapCli.CantidadDerecho 			= _datosEvCapCli.CantidadDerecho.setScale(0, RoundingMode.DOWN);
			_datosEvCapCli.CantidadNueva 			= _datosEvCapCli.CantidadNueva.setScale(0, RoundingMode.DOWN);
			_datosEvCapCli.Total 				= MonedaFormato.formatear(con, _datosEvCapCli.Total , MonedaFormato.MONTO, recorreEvento.IdMonedaLiquidacion).valor;
		}
	
		for(EventoCapitalCliente _datosEvCapCon : out.P_OUT_CONTRAPARTES){
			
			newValue = BigDecimal.ZERO;
			newValue =  _datosEvCapCon.CantidadDerecho.setScale(0, RoundingMode.DOWN);
			_datosEvCapCon.CantidadDerecho 			=  _datosEvCapCon.CantidadDerecho.setScale(0, RoundingMode.DOWN); // Lo trunca y lo deja sin decimales
			_datosEvCapCon.CantidadNueva 			= _datosEvCapCon.CantidadNueva.setScale(0, RoundingMode.DOWN);  // Lo trunca y lo deja sin decimales
			System.out.println("POST _datosEvCapCon.CantidadDerecho "+_datosEvCapCon.CantidadDerecho);
			System.out.println(" ");
			_datosEvCapCon.Total 				= MonedaFormato.formatear(con, _datosEvCapCon.Total , MonedaFormato.MONTO, recorreEvento.IdMonedaLiquidacion).valor;
		}

		// GCBSXNEC-1 Suscripcion complementaria 2da vuelta:
		if (("S").equalsIgnoreCase(recorreEvento.SuscripcionComplementaria) && !("").equalsIgnoreCase(recorreEvento.SecuenciaRelacionada)) {
			
			ConsultarEventoCapitalCliente CECC = new ConsultarEventoCapitalCliente();
			ConsultarEventoCapitalCliente.Parametros_Entrada entradaConsultarECCLI = new ConsultarEventoCapitalCliente.Parametros_Entrada();
			ConsultarEventoCapitalCliente.ArrSalidaGrilla salidaConsultarECCLI = new ConsultarEventoCapitalCliente.ArrSalidaGrilla();
			ArrayList<EventoCapitalCliente> datosInsertEvCapCliSusComp = new ArrayList<>();
			ArrayList<EventoCapitalCliente> datosInsertEvCapCliContraparteSusComp = new ArrayList<>();
			BigDecimal CantidadDerechoTotal = BigDecimal.ZERO;
			
			entradaConsultarECCLI.p_IdEmpresa 	= recorreEvento.IdEmpresa;
			entradaConsultarECCLI.p_IdUsuario 	= recorreEvento.IdUsuario;
			entradaConsultarECCLI.p_IdModulo 	= IdModulo;
			entradaConsultarECCLI.p_IdEmpleadoConsulta = 0;
			entradaConsultarECCLI.p_IdCliente 	= 0;
			entradaConsultarECCLI.p_TipoEvento	= "SU";
			entradaConsultarECCLI.p_Instrumento = recorreEvento.IdInstrumento.toString();
			entradaConsultarECCLI.p_Estado		= "0";
			entradaConsultarECCLI.p_TipoSocioNegocio = "0";
			entradaConsultarECCLI.p_NumeroDividendo = 1;
			entradaConsultarECCLI.p_FechaPagoDesde = "1900-01-01T06:00:00.000Z";
			entradaConsultarECCLI.p_FechaPagoHasta = fechaSistema;
			entradaConsultarECCLI.p_Secuencia	= Long.parseLong(recorreEvento.SecuenciaRelacionada);
			entradaConsultarECCLI.p_SuscripcionComplementaria = recorreEvento.SuscripcionComplementaria;
			
			salidaConsultarECCLI = CECC.logica(con, entradaConsultarECCLI);

			HashMap<String, EventoCapitalCliente> mapClientes = new HashMap<>(64);
			for(ConsultarEventoCapitalCliente.Parametros_Salida operacion : salidaConsultarECCLI.P_OUT_CUSTODIA_EVENTO_CAPITAL) {
				String hashKey = operacion.IdSocioNegocio + "_" + operacion.IdCliente + "_" + operacion.IdPortafolio;
				EventoCapitalCliente eventoCapitalCliente = mapClientes.get(hashKey);
				if (eventoCapitalCliente == null) {
					eventoCapitalCliente = new EventoCapitalCliente();
					eventoCapitalCliente.IdSocioNegocio = operacion.IdSocioNegocio;
					eventoCapitalCliente.IdCliente = operacion.IdCliente;
					eventoCapitalCliente.IdPortafolio = operacion.IdPortafolio;
					eventoCapitalCliente.TipoCartera = operacion.TipoCartera;
					eventoCapitalCliente.CantidadDerecho = operacion.CantidadNueva.setScale(0, RoundingMode.DOWN);
					eventoCapitalCliente.CantidadNueva = operacion.CantidadNueva.multiply(recorreEvento.Factor).setScale(0, RoundingMode.DOWN);

				} else {
					eventoCapitalCliente.CantidadDerecho = eventoCapitalCliente.CantidadDerecho.add(operacion.CantidadNueva);
					eventoCapitalCliente.CantidadNueva = eventoCapitalCliente.CantidadNueva.add(operacion.CantidadNueva.multiply(recorreEvento.Factor));					
				}
				mapClientes.put(hashKey, eventoCapitalCliente);
			}
			
			List<EventoCapitalCliente> clientesAgrupados = agruparEventoCapitalCliente((ArrayList<EventoCapitalCliente>) out.P_OUT_CLIENTES);
			for(EventoCapitalCliente evCaCli : clientesAgrupados) {
				String hashKey = evCaCli.IdSocioNegocio + "_" + evCaCli.IdCliente + "_" + evCaCli.IdPortafolio;
				EventoCapitalCliente eventoCapitalCliente = mapClientes.get(hashKey);
				if (eventoCapitalCliente != null) {
					evCaCli.CantidadDerecho = eventoCapitalCliente.CantidadDerecho.setScale(0, RoundingMode.DOWN);
					evCaCli.CantidadNueva = eventoCapitalCliente.CantidadNueva.setScale(0, RoundingMode.DOWN);
					evCaCli.SecuenciaRelacionada = "" + recorreEvento.SecuenciaRelacionada;
					evCaCli.SuscripcionComplementaria = recorreEvento.SuscripcionComplementaria;
					if(("").equalsIgnoreCase(evCaCli.IdConceptoGarantia) && evCaCli.IdContraparteGaratia == 0 && ("CUS").contentEquals(evCaCli.TipoCartera)) {							
						datosInsertEvCapCliSusComp.add(evCaCli);
						CantidadDerechoTotal = CantidadDerechoTotal.subtract(evCaCli.CantidadDerecho);
					}
				}				
			}

			out.P_OUT_CLIENTES = datosInsertEvCapCliSusComp; // REEMPLAZA TODOS LOS CLIENTES EN LA SALIDA PARA INCLUIR UNICAMENTE AQUELLOS QUE TENGAN SUSCRIPCION COMPLEMENTARIA.
			
			HashMap<String, Object> params = new HashMap<String, Object>();
			HashMap<String, Object> paramsConsBoveda = new HashMap<>();
			
			params.clear();
			List<ProcesarEventoCapitalEntrada> Parametros = sql.query(con, SQL_SELECT_PARAMETRO, params, ProcesarEventoCapitalEntrada.class);
			params.put("p_IdInstrumento", recorreEvento.IdInstrumento.toString());
			List<ConsultaContraparte> Emisor = sql.query(con, SQL_SELECT_EMISOR, params, ConsultaContraparte.class);
			paramsConsBoveda.put("IdBoveda", "DCV");
			paramsConsBoveda.put("IdEmpresa", empresa.IdEmpresa);
			List<ConsultaBovedas> infoBoveda = sql.query(con, SQL_SELECT_CONSULTAR_BOVEDA, paramsConsBoveda, ConsultaBovedas.class);
			
			String parametro = Parametros.get(0).p_Prametro;
			
			for(EventoCapitalCliente evCaContr : out.P_OUT_CONTRAPARTES) {				
				if(("S").equalsIgnoreCase(parametro)) {
					if(evCaContr.NombreCliente.equalsIgnoreCase(Emisor.get(0).NombreClienteEmisor) && 
						evCaContr.DocumentoIdentidad.equalsIgnoreCase(Emisor.get(0).DocumentoIdentidadEmisor) &&
						("CUS").equalsIgnoreCase(evCaContr.TipoCartera))
					{
						evCaContr.CantidadDerecho = CantidadDerechoTotal;
						evCaContr.CantidadNueva	= CantidadDerechoTotal.multiply(recorreEvento.Factor).setScale(0, RoundingMode.DOWN);
						evCaContr.SecuenciaRelacionada = "" + recorreEvento.SecuenciaRelacionada;
						evCaContr.SuscripcionComplementaria = recorreEvento.SuscripcionComplementaria;
						datosInsertEvCapCliContraparteSusComp.add(evCaContr);
					}
				} else {
					if(evCaContr.NombreCliente.equalsIgnoreCase(infoBoveda.get(0).NombreCliente) && 
						evCaContr.DocumentoIdentidad.equalsIgnoreCase(infoBoveda.get(0).DocumentoIdentidad) &&
						("CUS").equalsIgnoreCase(evCaContr.TipoCartera))
					{
						evCaContr.CantidadDerecho = CantidadDerechoTotal;
						evCaContr.CantidadNueva	= CantidadDerechoTotal.multiply(recorreEvento.Factor).setScale(0, RoundingMode.DOWN);
						evCaContr.SecuenciaRelacionada = "" + recorreEvento.SecuenciaRelacionada;
						evCaContr.SuscripcionComplementaria = recorreEvento.SuscripcionComplementaria;
						datosInsertEvCapCliContraparteSusComp.add(evCaContr);
					}
				}				
			}
			out.P_OUT_CONTRAPARTES = datosInsertEvCapCliContraparteSusComp; // LIMPIA LOS CLIENTES CONTRAPARTE Y DEJA UNICAMENTE EL QUE CORRESPONDA AL EMISOR O DCV SEGUN CORRESPONDA.
		
		}
		
		return out;
	}
	private SalidaClientesConDerecho AgregarGastoTransferencia(Connection con, SalidaClientesConDerecho out, EventoCapital evento) throws Exception{
		int cantidadClientes = 0;
		List<EventoCapitalCliente> eccCliente = out.P_OUT_CLIENTES;
		List<EventoCapitalCliente> eccContraparte = out.P_OUT_CONTRAPARTES;
		HashMap<Long, LlaveCliente> mapClienteEvCapCli = new HashMap<Long, LlaveCliente>();
		for(EventoCapitalCliente ecc: eccCliente) {
			BigDecimal total = ecc.Total;
			LlaveCliente llaveGasto = new LlaveCliente(ecc.IdPortafolio, ecc.TipoCartera, ecc.IdContraparteGaratia, total);
			if (mapClienteEvCapCli.get(ecc.IdCliente) == null || llaveGasto.TipoCartera.equalsIgnoreCase("CUS")) {
				mapClienteEvCapCli.put(ecc.IdCliente, llaveGasto);
			}
		}
		cantidadClientes = mapClienteEvCapCli.size();
		if(cantidadClientes == 0) return out;
		BigDecimal gastoDividido = evento.GastoTransferencia.divide(new BigDecimal(cantidadClientes),MathContext.DECIMAL128);
		gastoDividido = MonedaFormato.formatear(con, gastoDividido , MonedaFormato.MONTO, evento.IdMonedaLiquidacion).valor;
		
		HashMap<String, BigDecimal> mapContraparteEvCapCli = new HashMap<String, BigDecimal>();
		for(EventoCapitalCliente ecc: eccCliente) {
			LlaveCliente llave = mapClienteEvCapCli.get(ecc.IdCliente);
			String llaveContraparte = ecc.TipoCartera + ecc.IdContraparteGaratia;
			BigDecimal gastoTotalContraparte = mapContraparteEvCapCli.get(llaveContraparte);
			if(gastoTotalContraparte == null) 
				gastoTotalContraparte = new BigDecimal(0);
			if (llave.IdPortafolio == ecc.IdPortafolio && llave.TipoCartera.equalsIgnoreCase(ecc.TipoCartera) && llave.IdContraparteGarantia == ecc.IdContraparteGaratia) {
				ecc.Total = ecc.Total.subtract(gastoDividido);
				ecc.GastoTransferencia = gastoDividido;
				BigDecimal nuevoTotalContraparte = gastoTotalContraparte.add(gastoDividido.multiply(new BigDecimal(-1)));
				mapContraparteEvCapCli.put(llaveContraparte, nuevoTotalContraparte);
			}
		}
		for(EventoCapitalCliente ecc: eccContraparte) {
			String llaveContraparte = ecc.TipoCartera + ecc.IdContraparteGaratia;
			BigDecimal gastoTotalContraparte = mapContraparteEvCapCli.get(llaveContraparte);
			if (gastoTotalContraparte != null) {
				ecc.Total = ecc.Total.subtract(gastoTotalContraparte);
				ecc.GastoTransferencia = gastoTotalContraparte;
			}
		}
		return out;
	}
	
	public static void agregarLogProcesarEventoCapital(Connection con, Long idEventoCapital, String p_IdModulo, Long p_IdUsuario, Long p_IdEmpresa, String idEstadoEventoCapital, String idTipoEventoCapital, int registrosProcesados, String nemo) {
		try {
			logger.debug("INICIO::ProcesarEventoCapital.agregarLogProcesarEventoCapital");
			long inicio = System.currentTimeMillis();
			String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
			AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
			AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
			
			log.IdTrabajo		= entradaAgregarLog.p_IdTrabajo; 
			log.IdCabeceraLog	= idEventoCapital; 
			log.IdTipoLog		= "PROCESO"; 
			log.IdModulo		= p_IdModulo; 
			log.IdUsuario		= p_IdUsuario; 
			log.IdEmpresa		= p_IdEmpresa; 
			log.FechaSistemaLog	= fechaSistema; 
			log.IdUsuarioAtendido	= p_IdUsuario; 
			log.IdCliente			= 0L; 
			log.EstadoCliente		= ""; 
			log.NombreCliente		= ""; 
			log.IdTipoDocumentoIdentidad	= ""; 
			log.IdDocumentoIdentidad	= ""; 
			log.IdPortafolio			= 0L; 
			log.EstadoPortafolio		= ""; 
			log.IdTipoRegistro			= "PROCESAREVENTOCAPITAL"; 
			log.IdRegistro				= idEventoCapital.toString(); 
			log.EstadoRegistro			= idEstadoEventoCapital; 
			log.IdTipoOperacion			= idTipoEventoCapital; 
			log.Folio					= 0L; 
			log.Cantidad				= new BigDecimal(registrosProcesados); 
			log.PrecioTasa				= BigDecimal.ZERO; 
			log.Monto					= BigDecimal.ZERO; 
			log.IdMonedaOrigen			= ""; 
			log.IdMonedaLiquidacion		= ""; 
			log.TipoCambio				= ""; 
			log.Nemo					= nemo; 
			log.IdProceso				= 0L; 
			log.EstadoProceso			= ""; 
			log.NombreProceso			= "cl.bcs.custodia.evc.ProcesarEventoCapital.java";
			
			entradaAgregarLog.p_Log.add(log);
			AgregarLog agregarLogService = new AgregarLog();
			agregarLogService.logica(con, entradaAgregarLog);
			
			long fin = System.currentTimeMillis();
			logger.debug("FIN::ProcesarEventoCapital.agregarLogProcesarEventoCapital:" , fin - inicio);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private EventoCapitalCliente obtenerContrapartePrestamista(EventoCapitalCliente eventoCliente) {
		EventoCapitalCliente registroEventoPestamista     = new EventoCapitalCliente();
		registroEventoPestamista.IdEventoCapitalCliente   = eventoCliente.IdEventoCapitalCliente;
		registroEventoPestamista.IdEventoCapital          = eventoCliente.IdEventoCapital;
		registroEventoPestamista.IdTipoEventoCapital      = eventoCliente.IdTipoEventoCapital;
		registroEventoPestamista.Secuencia                = eventoCliente.Secuencia;
		registroEventoPestamista.IdSocioNegocio           = eventoCliente.IdSocioNegocio;
		registroEventoPestamista.IdPersona                = eventoCliente.IdPersona;
		registroEventoPestamista.TipoSocioNegocio         = eventoCliente.TipoSocioNegocio;
		registroEventoPestamista.IdCliente                = eventoCliente.IdCliente;
		registroEventoPestamista.IdPortafolio             = eventoCliente.IdPortafolio;
		registroEventoPestamista.IdTipoDocumentoIdentidad = eventoCliente.IdTipoDocumentoIdentidad;
		registroEventoPestamista.DocumentoIdentidad       = eventoCliente.DocumentoIdentidad;
		registroEventoPestamista.NombreCliente            = eventoCliente.NombreCliente;
		registroEventoPestamista.IdClasificacionTipoInstrumento = eventoCliente.IdClasificacionTipoInstrumento;
		
		registroEventoPestamista.IdTipoInstrumento        = eventoCliente.IdTipoInstrumento;
		registroEventoPestamista.IdInstrumento            = eventoCliente.IdInstrumento;
		registroEventoPestamista.Nemo                     = eventoCliente.Nemo;
		registroEventoPestamista.IdInstrumentoNuevo       = eventoCliente.IdInstrumentoNuevo;
		registroEventoPestamista.NemoNuevo                = eventoCliente.NemoNuevo;
		registroEventoPestamista.FechaCierre              = eventoCliente.FechaCierre;
		registroEventoPestamista.FechaPago                = eventoCliente.FechaPago;
		registroEventoPestamista.CantidadDerecho          = eventoCliente.CantidadDerecho;
		registroEventoPestamista.TipoCartera              = eventoCliente.TipoCartera;
		registroEventoPestamista.CantidadNueva            = eventoCliente.CantidadNueva;
		registroEventoPestamista.MontoCapital             = eventoCliente.MontoCapital;
		registroEventoPestamista.MontoInteres             = eventoCliente.MontoInteres;
		registroEventoPestamista.Total                    = eventoCliente.Total;
		registroEventoPestamista.IdMoneda                 = eventoCliente.IdMoneda;
		registroEventoPestamista.SecuenciaCupon           = eventoCliente.SecuenciaCupon;
		registroEventoPestamista.CodigoUsuarioAgente      = eventoCliente.CodigoUsuarioAgente;
		registroEventoPestamista.NombreAgente             = eventoCliente.NombreAgente;
		registroEventoPestamista.CodigoSucursal           = eventoCliente.CodigoSucursal;
		registroEventoPestamista.FechaHoraIngresoRegistro = eventoCliente.FechaHoraIngresoRegistro;
		registroEventoPestamista.FechaHoraUltimaActualizacion = eventoCliente.FechaHoraUltimaActualizacion;
		
		registroEventoPestamista.IdUsuario                = eventoCliente.IdUsuario;
		registroEventoPestamista.IdEstado                 = eventoCliente.IdEstado;
		registroEventoPestamista.IdBoveda                 = eventoCliente.IdBoveda;
		registroEventoPestamista.IdBolsa                  = eventoCliente.IdBolsa;
		registroEventoPestamista.IdConceptoGarantia       = eventoCliente.IdConceptoGarantia;
		registroEventoPestamista.IdTipoOperacionGarantia  = eventoCliente.IdTipoOperacionGarantia;
		registroEventoPestamista.MontoReajuste            = eventoCliente.MontoReajuste;
		registroEventoPestamista.Precio                   = eventoCliente.Precio;
		registroEventoPestamista.PrecioNuevo              = eventoCliente.PrecioNuevo;
		registroEventoPestamista.NumeroDividendo          = eventoCliente.NumeroDividendo;
		registroEventoPestamista.Origen                   = eventoCliente.Origen;
		registroEventoPestamista.Paridad                  = eventoCliente.Paridad;
		registroEventoPestamista.IdCartera                = eventoCliente.IdCartera;
		registroEventoPestamista.FolioCartera             = eventoCliente.FolioCartera;
		registroEventoPestamista.IdEmpleadoEjecutivoPortafolio = eventoCliente.IdEmpleadoEjecutivoPortafolio;
		
		registroEventoPestamista.EstadoNotificacion       = eventoCliente.EstadoNotificacion;
		registroEventoPestamista.IdEventoCapitalClienteFormaDePago = eventoCliente.IdEventoCapitalClienteFormaDePago;
		registroEventoPestamista.TipoCuentaBoveda         = eventoCliente.TipoCuentaBoveda;
		registroEventoPestamista.NumeroCuentaBoveda       = eventoCliente.NumeroCuentaBoveda;
		registroEventoPestamista.PagaClienteCorredor      = eventoCliente.PagaClienteCorredor;
		registroEventoPestamista.IdTipoReajuste           = eventoCliente.IdTipoReajuste;
		registroEventoPestamista.INGMontoInteresCorteCupon = eventoCliente.INGMontoInteresCorteCupon;
		registroEventoPestamista.Amortizacion             = eventoCliente.Amortizacion;
		registroEventoPestamista.IdInstrumentoAnterior    = eventoCliente.IdInstrumentoAnterior;
		registroEventoPestamista.AfectoaClearing          = eventoCliente.AfectoaClearing;
		registroEventoPestamista.ProcesadoClearing        = eventoCliente.ProcesadoClearing;
		registroEventoPestamista.IdCorredorContraparte    = eventoCliente.IdCorredorContraparte;
		registroEventoPestamista.IdComprobanteTesoreria   = eventoCliente.IdComprobanteTesoreria;
		registroEventoPestamista.TipoOperacion            = eventoCliente.TipoOperacion;
		registroEventoPestamista.CodigoCorredorContraparte = eventoCliente.CodigoCorredorContraparte;
		registroEventoPestamista.MontoBeneficioArchivo    = eventoCliente.MontoBeneficioArchivo;
		registroEventoPestamista.DiferenciaMontoBeneficio = eventoCliente.DiferenciaMontoBeneficio;
		registroEventoPestamista.CantidadDerechoArchivo   = eventoCliente.CantidadDerechoArchivo;
		registroEventoPestamista.AccionesDisponiblesArchivo = eventoCliente.AccionesDisponiblesArchivo;
		registroEventoPestamista.DiferenciaCantidadDerecho = eventoCliente.DiferenciaCantidadDerecho;
		registroEventoPestamista.DiferenciaAccionesCantidadNueva = eventoCliente.DiferenciaAccionesCantidadNueva;
		registroEventoPestamista.MontoInteresArchivo      = eventoCliente.MontoInteresArchivo;
		registroEventoPestamista.MontoCapitalArchivo      = eventoCliente.MontoCapitalArchivo;
		registroEventoPestamista.DiferenciaMontoInteres   = eventoCliente.DiferenciaMontoInteres;
		registroEventoPestamista.DiferenciaMontoCapital   = eventoCliente.DiferenciaMontoCapital;
		registroEventoPestamista.TipoCarteraNormativa     = eventoCliente.TipoCarteraNormativa;
		registroEventoPestamista.IdTipoCuentaCustodio     = eventoCliente.IdTipoCuentaCustodio;
		registroEventoPestamista.IdMovimientoComprobanteFac = eventoCliente.IdMovimientoComprobanteFac;
		registroEventoPestamista.IdCuentaCustodioPortafolio = eventoCliente.IdCuentaCustodioPortafolio;
		registroEventoPestamista.MontoRetencion           = eventoCliente.MontoRetencion;
		registroEventoPestamista.DiferenciaMontoRetencion = eventoCliente.DiferenciaMontoRetencion;
		registroEventoPestamista.MontoRetencionArchivo = eventoCliente.MontoRetencionArchivo;
		registroEventoPestamista.IdConcepto               = eventoCliente.IdConcepto;
		registroEventoPestamista.IdContraparteGaratia     = eventoCliente.IdContraparteGaratia;
		registroEventoPestamista.ISFUT                    = eventoCliente.ISFUT;
		registroEventoPestamista.RetencionISFUT           = eventoCliente.RetencionISFUT;
		registroEventoPestamista.MontoRetencionISFUT      = eventoCliente.MontoRetencionISFUT;
		registroEventoPestamista.FactorOtraMoneda         = eventoCliente.FactorOtraMoneda;
		registroEventoPestamista.SecuenciaRelacionada     = eventoCliente.SecuenciaRelacionada;
		registroEventoPestamista.SuscripcionComplementaria = eventoCliente.SuscripcionComplementaria;
		registroEventoPestamista.ImpuestoDivExtranjero    = eventoCliente.ImpuestoDivExtranjero;
		registroEventoPestamista.MontoImpuestoExtranjero  = eventoCliente.MontoImpuestoExtranjero;
		registroEventoPestamista.GastoTransferencia       = eventoCliente.GastoTransferencia;
		registroEventoPestamista.Factor                   = eventoCliente.Factor;
		return registroEventoPestamista;
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		Gson gson = Utils.getGsonBuilder();
		ProcesarEventoCapitalEntrada jin = gson.fromJson(in.toString(), ProcesarEventoCapitalEntrada.class);
		ProcesarEventoCapitalSalida out = logica(conn, jin);
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
			conn = PlataformaUtils.getPropertiesConnection("qa02", "connections.properties");

			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":97,\"p_IdModulo\":\"CUS\",\"p_Origen\":\"UI\",\"p_UsaReglasNegocio\":\"S\",\"p_IdEventoCapital\":[54154],\"p_FechaLimite\":[\"2025-10-22\"],\"p_IdTipoEventoCapital\":[]}";
			ProcesarEventoCapitalEntrada objIn = gson.fromJson(json,ProcesarEventoCapitalEntrada.class);
			ProcesarEventoCapital demo = new ProcesarEventoCapital();
			ProcesarEventoCapitalSalida ObjOut=demo.logica(conn, objIn);
			System.out.println(ObjOut.toString());
			
			//conn.commit();
			conn.rollback();
		}catch (Exception e){
			e.printStackTrace();
		}finally {
			conn.close();
		}
			System.out.println("== END ==");

	};
}
