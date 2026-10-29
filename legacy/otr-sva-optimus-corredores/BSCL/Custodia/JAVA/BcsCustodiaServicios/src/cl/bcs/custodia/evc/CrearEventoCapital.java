package cl.bcs.custodia.evc;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.custodia.evc.BuscarMonedaPreferenciaPagoEventoCapitalCliente.MonedaPagoDividendos;
import cl.bcs.custodia.evc.BuscarMonedaPreferenciaPagoEventoCapitalCliente.VariacionCapitalLog;
import cl.bcs.custodia.pojo.EventoCapital;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.AgregarLog.InAgregarLog;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearEventoCapital implements JavaCall{
	private static final Logger logger 			= LogManager.getLogger(CrearEventoCapital.class);
	
	private final String SQL_SELECT_EVENTO_CAPITAL= loadResourceToString("cl/bcs/custodia/sql/CrearEventoCapital_SelectEventoCapital.sql");
	private final String SQL_SELECT_ID_INSTRUMENTO= loadResourceToString("cl/bcs/custodia/sql/CrearEventoCapital_SelectIdInstrumento.sql");
	private final String SQL_SELECT_INSTRUMENTO= loadResourceToString("cl/bcs/custodia/sql/CrearEventoCapital_SelectInstrumento.sql");
	private final String SQL_SELECT_INSTRUMENTO_BONO= loadResourceToString("cl/bcs/custodia/sql/CrearEventoCapital_SelectInstrumentoBono.sql");
	private final String SQL_SELECT_INSTRUMENTO_CUPON= loadResourceToString("cl/bcs/custodia/sql/CrearEventoCapital_SelectInstrumentoCupon.sql");
	private final String SQL_INSERT_EVENTO_CAPITAL= loadResourceToString("cl/bcs/custodia/sql/CrearEventoCapital_InsertEventoCapital.sql");
	private final String SQL_SELECT_FACTOR_OTRAS_MONEDAS_PAGO_DIVIDENDOS	= loadResourceToString("cl/bcs/custodia/sql/CrearEventoCapital_SelectFactorOtrasMonedaPagoDividendos.sql");
	private final String SQL_SELECT_IMPUESTO_DV_EXTRANJERO = loadResourceToString("cl/bcs/custodia/sql/CrearEventoCapital_ConsultaImpuestoDvExtranjero.sql");
	
	public static class EventoCapital_IN {
		public Long p_IdEmpresa;
	    public Long	p_IdUsuario;
	    public String p_IdModulo;
	    public String p_UsaReglasNegocio;
	    public List<Evento> p_EventoCapital;
	    public List<Archivo> p_DataArchivo;
	        
	    public EventoCapital_IN() {
			this.p_EventoCapital = new ArrayList<Evento>();
			this.p_DataArchivo = new ArrayList<Archivo>();
		}

		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class EventoCapital_OUT {
		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;
		public List<Long>			P_OUT_SECUENCIA;
		public List<EventoCapital_Con_Error> P_OUT_EVENTOS_ERRORES;
		
		public EventoCapital_OUT() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_SECUENCIA = new ArrayList<Long>();
			this.P_OUT_EVENTOS_ERRORES = new ArrayList<EventoCapital_Con_Error>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
		
	}
	
	public static class EventoCapital_Con_Error {
		public Long p_Secuencia;
		public String p_Nemo;
	    public String p_IdTipoEventoCapital;
	    public String p_FechaLimite;
	    public String p_FechaPago;
	    public String p_Mensaje;
	}
	
	public static class Evento {
		public Long 	p_IdEventoCapital;
		public String 	p_IdTipoEventoCapital;
		public Long 	p_Secuencia;
		public String 	p_Descripcion;
		public String 	p_EsInscrito;
		public String 	p_IdClasificacionTipoInstrumento;
		public String 	p_IdTipoInstrumento;  
		public Long 	p_IdInstrumento;
		public String  	p_Nemo;
		public BigDecimal	p_Factor;
		public BigDecimal	p_Impuesto;
		public String 		p_IdMoneda;
		public String 			p_IdBoveda;
		public String		p_IdMonedaLiquidacion;
		public BigDecimal	p_Paridad;
		public Long 		p_IdInstrumentoNuevo;
		public String 		p_NemoNuevo;
		public BigDecimal	p_FactorNuevo;
		public String 		p_FechaActualizacion;     
		public String 		p_FechaLimite;     
		public String 		p_FechaPago;     
		public Long 		p_NumeroDividendo;
		public String 		p_IdTipoDividendo;
		public BigDecimal	p_PrecioAnterior;
		public String 		p_FechaHoraIngresoRegistro;     
		public String 		p_FechaHoraUltimaActualizacion;     
		public String 		p_TipoIngreso;
		public String 		p_IdEstado;
		public String 		p_IdEmisor;
		public String 		p_IdEmisorNuevo;
		public BigDecimal	p_Monto;
		public BigDecimal	p_Cuotas;
		public String		p_Origen = "MAN";
		public String		p_EsISFUT;
		public BigDecimal	p_TasaEfectiva;
		//--------------------
		public Long		p_IdEmpresa;
		public BigDecimal	p_Valor;
		public Long			p_IdUsuario;		
		public Long 		p_Indice;
		//--------SORTEO LETRAS
		public BigDecimal	p_CuentaDepositanteSorteoLetras;
	    public BigDecimal	p_MonedaSorteoLetras;
		public String		p_GlosaMonedaSorteoLetras;
		public BigDecimal	p_EmisorPagadorSorteoLetras;
		public BigDecimal	p_EmisorSorteoLetras;
		public Long			p_InstrumentoSorteoLetras;
		public String		p_GlosaInstrumentoSorteoLetras;
		public BigDecimal	p_BloqueoSorteoLetras;
		public BigDecimal	p_SorteadoSorteoLetras;
		public BigDecimal	p_APagarSorteoLetras;
		public BigDecimal	p_APagarPesosSorteoLetras;
		//-------- GCBSXNEC-1: EMISION PAGADA 2DA VUELTA
		public String		p_SecuenciaRelacionada;
		public String		p_SuscripcionComplementaria;
		public BigDecimal   p_ImpuestoDivExtranjero;
		public BigDecimal	p_GastoTransferencia;
		public Boolean p_EsEP;	
			    
	    public Evento() {
			this.p_IdEventoCapital = 0L;
			this.p_IdTipoEventoCapital = "";
			this.p_Secuencia = 0L;
			this.p_Descripcion = "";
			this.p_EsInscrito = "";
			this.p_IdClasificacionTipoInstrumento = "";
			this.p_IdTipoInstrumento = "";
			this.p_IdInstrumento = 0L;
			this.p_Nemo = "";
			this.p_Factor = BigDecimal.ZERO;
			this.p_Impuesto = BigDecimal.ZERO;
			this.p_IdMoneda = "";
			this.p_IdBoveda = "";
			this.p_IdMonedaLiquidacion = "";
			this.p_Paridad = BigDecimal.ZERO;
			this.p_IdInstrumentoNuevo = 0L;
			this.p_NemoNuevo = "";
			this.p_FactorNuevo = BigDecimal.ZERO;
			this.p_FechaActualizacion = "";
			this.p_FechaLimite = "";
			this.p_FechaPago = "";
			this.p_NumeroDividendo = 0L;
			this.p_IdTipoDividendo = "";
			this.p_PrecioAnterior = BigDecimal.ZERO;
			this.p_FechaHoraIngresoRegistro = "";
			this.p_FechaHoraUltimaActualizacion = "";
			this.p_TipoIngreso = "";
			this.p_IdEstado = "";
			this.p_IdEmisor = "";
			this.p_IdEmisorNuevo = "";
			this.p_Monto = BigDecimal.ZERO;
			this.p_Cuotas = BigDecimal.ZERO;
			this.p_Origen = "MAN";
			this.p_EsISFUT = "";
			this.p_TasaEfectiva = BigDecimal.ZERO;
			this.p_IdEmpresa = 0L;
			this.p_Valor = BigDecimal.ZERO;
			this.p_IdUsuario = 0L;
			this.p_Indice = 0L;
			this.p_CuentaDepositanteSorteoLetras = BigDecimal.ZERO;
			this.p_MonedaSorteoLetras = BigDecimal.ZERO;
			this.p_GlosaMonedaSorteoLetras = "";
			this.p_EmisorPagadorSorteoLetras = BigDecimal.ZERO;
			this.p_EmisorSorteoLetras = BigDecimal.ZERO;
			this.p_InstrumentoSorteoLetras = 0L;
			this.p_GlosaInstrumentoSorteoLetras = "";
			this.p_BloqueoSorteoLetras = BigDecimal.ZERO;
			this.p_SorteadoSorteoLetras = BigDecimal.ZERO;
			this.p_APagarSorteoLetras = BigDecimal.ZERO;
			this.p_APagarPesosSorteoLetras = BigDecimal.ZERO;
			this.p_SecuenciaRelacionada = "";
			this.p_SuscripcionComplementaria = "N";
			this.p_ImpuestoDivExtranjero = BigDecimal.ZERO;
			this.p_GastoTransferencia = BigDecimal.ZERO;
			this.p_EsEP = false;
		}


		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class EventoExiste {
		public Long 		NumeroDividendo;
		public Long 		Secuencia;
			    
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
		public String		IdEmisor;
		public String 		LlaveNemotecnico;
		
	    public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Archivo {
		public BigDecimal	DEPOSITANTE;//		DECIMAL(10,0),
		public BigDecimal	MONEDA;//			DECIMAL(2,0),
		public String		GLOSAMONEDA;//		NVARCHAR(10),
		public BigDecimal	EMISORPAGADOR;//		DECIMAL(5,0),
		public BigDecimal	EMISOR;//			DECIMAL(5,0),
		public Long		INSTRUMENTO;//		NVARCHAR(2),
		public String 		GLOSAINSTRUMENTO;//	NVARCHAR(10),
		public String		EVENTO;//			NVARCHAR(8),
		public String		NEMO;//				NVARCHAR(12),
		public BigDecimal	BLOQUEO;//			DECIMAL(6,0),
		public BigDecimal	SORTEADO;//			DECIMAL(12,4),
		public BigDecimal	APAGAR;//			DECIMAL(12,4),
		public BigDecimal	APAGARPESOS;//		DECIMAL(12,0),
		public String		MENSAJEVALIDACION;// NVARCHAR(200)
		public String		IDTIPOEVENTOCAPITAL;
		
		
		public Archivo() {
			DEPOSITANTE = BigDecimal.ZERO;
			MONEDA = BigDecimal.ZERO;
			GLOSAMONEDA = "";
			EMISORPAGADOR = BigDecimal.ZERO;
			EMISOR = BigDecimal.ZERO;
			INSTRUMENTO = 0L;
			GLOSAINSTRUMENTO = "";
			EVENTO = "";
			NEMO = "";
			BLOQUEO = BigDecimal.ZERO;
			SORTEADO = BigDecimal.ZERO;
			APAGAR = BigDecimal.ZERO;
			APAGARPESOS = BigDecimal.ZERO;
			MENSAJEVALIDACION = "";
			IDTIPOEVENTOCAPITAL = "SL";
		}


		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ConsultaImpuestoDvExtranjero{
		public String AplicaImpuesto;
		public String AplicaGastoTransferencia;
		public String IdEmisor;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public EventoCapital_OUT logica (Connection conn, EventoCapital_IN in) throws Exception {
		logger.info("INICIO::CUS.CrearEventoCapital");
		EventoCapital_OUT out = new EventoCapital_OUT();
		{
			if (in.p_DataArchivo.size() > 0){
				out = CrearEventoCapitalArchivo(conn, in);
			}
			else {
				out = CrearEventoCapitalManual(conn, in);
			}
			
		}				
				
		logger.info("FIN::CUS.CrearEventoCapital");
		return out;
	};

	private EventoCapital_OUT CrearEventoCapitalManual (Connection conn, EventoCapital_IN in)  throws Exception {
		SQL _sql = new SQL();
		Long Id = 0L;
		//Long countInsertReserva = 0L;
		HashMap<String,String> mapMsg = new HashMap<>();
		logger.info("INICIO::CUS.CrearEventoCapitalManual");
		EventoCapital_OUT out = new EventoCapital_OUT();
		HashMap<String, Object> params = new HashMap<>();
		List<Long> numeroDividendo = new ArrayList<>();
		List<String> nemo= new ArrayList<>();
		List<Long> IdInstrumentoNuevo= new ArrayList<>();
		List<EventoExiste> eventoExistente = new ArrayList<>();
		List<Instrumento> idInstrumento = new ArrayList<>();
		List<Instrumento> instrumento = new ArrayList<>();
		List<Instrumento> instrumentoBono = new ArrayList<>();
		List<Instrumento> instrumentoCupon = new ArrayList<>();
		List<String> tipoEventosCapital = new ArrayList<>();
		List<String> idClasificacionTipoInstrumento = new ArrayList<>();
		Map<String, Object> idInstrumentoMap = new HashMap<>();
		HashMap<Long, EventoCapital_Con_Error> eventosErrores = new HashMap<>();
		HashMap<String,String> valoresDiccionario = new HashMap<String,String>();
		List<Long> listadoSecuencias = new ArrayList<>();
		List<VariacionCapitalLog> obtenerMonedasConFactores = new ArrayList<VariacionCapitalLog>();
		AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
		entradaAgregarLog.p_IdTrabajo = 1L;
		
		String monedaSistema    	= ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.MonedaSistema");
		String fechaSistema 		= ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		List<String> listaEmisores = new ArrayList<String>();
		for (Evento ev: in.p_EventoCapital){
			//secuenciasEvento.add(ev.p_Secuencia);
			numeroDividendo.add(ev.p_NumeroDividendo);
			nemo.add(ev.p_Nemo);
			IdInstrumentoNuevo.add(ev.p_IdInstrumentoNuevo);
			listadoSecuencias.add(ev.p_Secuencia);
			listaEmisores.add(ev.p_IdEmisor);
			if (ev.p_IdClasificacionTipoInstrumento != null && !ev.p_IdClasificacionTipoInstrumento.isEmpty()) idClasificacionTipoInstrumento.add(ev.p_IdClasificacionTipoInstrumento);

		}
		for (Evento ev: in.p_EventoCapital){
			tipoEventosCapital.add(ev.p_IdTipoEventoCapital);
		}
		
		if (numeroDividendo.size() > 0){
			params = new HashMap<>();
			params.put("p_IdEmpresa", in.p_IdEmpresa);
			params.put("p_IdTipoEventoCapital",tipoEventosCapital);
			params.put("p_NumeroDividendo", numeroDividendo);
			eventoExistente = _sql.query(conn, SQL_SELECT_EVENTO_CAPITAL, params, EventoExiste.class);
		}
		if (nemo.size() > 0){
			params = new HashMap<>();
			params.put("p_Nemo", nemo);
			params.put("FiltroIdClasificacionTipoInstrumento", (idClasificacionTipoInstrumento.size() > 0) ? 1 : 0);
			params.put("p_IdClasificacionTipoInstrumento", (idClasificacionTipoInstrumento.size() > 0) ? idClasificacionTipoInstrumento : "");
			idInstrumento = _sql.query(conn, SQL_SELECT_ID_INSTRUMENTO, params, Instrumento.class);
			idInstrumentoMap = Utils.List2Hash(idInstrumento, "LlaveNemotecnico");
		}
		if (IdInstrumentoNuevo.size() > 0){
			params = new HashMap<>();
			params.put("p_IdInstrumentoNuevo", IdInstrumentoNuevo);
			instrumento = _sql.query(conn, SQL_SELECT_INSTRUMENTO, params, Instrumento.class);
		}
		if (IdInstrumentoNuevo.size() > 0){
			params = new HashMap<>();
			params.put("p_IdInstrumento", IdInstrumentoNuevo);
			instrumentoBono = _sql.query(conn, SQL_SELECT_INSTRUMENTO_BONO, params, Instrumento.class);
		}
		if (IdInstrumentoNuevo.size() > 0){
			params = new HashMap<>();
			params.put("p_IdInstrumento", IdInstrumentoNuevo);
			instrumentoCupon = _sql.query(conn, SQL_SELECT_INSTRUMENTO_CUPON, params, Instrumento.class);
		}
		if (listadoSecuencias.size() > 0){
			params = new HashMap<>();
			params.put("Secuencia", listadoSecuencias);
			obtenerMonedasConFactores = _sql.query(conn, SQL_SELECT_FACTOR_OTRAS_MONEDAS_PAGO_DIVIDENDOS, params, VariacionCapitalLog.class);
		}
		HashMap<String,Object> mapEntradaConsultaImpDvExt = new HashMap<String,Object>();
		mapEntradaConsultaImpDvExt.put("ids", listaEmisores);
		List<ConsultaImpuestoDvExtranjero> consultaImpuestoDvExt = _sql.query(conn, SQL_SELECT_IMPUESTO_DV_EXTRANJERO, mapEntradaConsultaImpDvExt, ConsultaImpuestoDvExtranjero.class);
		HashMap<String,ConsultaImpuestoDvExtranjero> mapConsultaImpuestoDvExt = new HashMap<String,ConsultaImpuestoDvExtranjero>();
		for(ConsultaImpuestoDvExtranjero consulta: consultaImpuestoDvExt){
			mapConsultaImpuestoDvExt.put(consulta.IdEmisor, consulta);
		}
		//::Validaciones
		{
			if (numeroDividendo.size() <= 0){
				// No Viene Evento
				mapMsg.clear();
				out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"SIN_DATOS_A_PROCESAR",mapMsg,conn,in.p_IdEmpresa));
				Id++;
			}
			for (Evento evCap: in.p_EventoCapital){
				EventoCapital_Con_Error eventoError = new EventoCapital_Con_Error();
				for (EventoExiste evExi: eventoExistente){
					if (evCap.p_NumeroDividendo.equals(evExi.NumeroDividendo) && evCap.p_Secuencia.equals(evExi.Secuencia)){
							//evExi.CantidadSecuencia >= 1){
						mapMsg.clear();
						mapMsg.put("p_NumeroDividendo", evCap.p_NumeroDividendo.toString());
						mapMsg.put("p_Secuencia",evCap.p_Secuencia.toString()); 
						out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"UI_EVENTO_CAPITAL_Y_O_SECUENCIA_YA_EXISTE",mapMsg,conn,in.p_IdEmpresa));
						Id++;
						eventoError.p_Secuencia = evCap.p_Secuencia;
						eventoError.p_Nemo = evCap.p_Nemo;
						eventoError.p_IdTipoEventoCapital = evCap.p_IdTipoEventoCapital;
						eventoError.p_FechaLimite = evCap.p_FechaLimite;
						eventoError.p_FechaPago = evCap.p_FechaPago;
						eventoError.p_Mensaje = Diccionario.getMensaje(conn, in.p_IdEmpresa, "UI_EVENTO_CAPITAL_Y_O_SECUENCIA_YA_EXISTE", mapMsg);
						eventosErrores.put(evCap.p_Secuencia, eventoError);
						out.P_OUT_EVENTOS_ERRORES.add(eventoError);
					}
				}
				
				if ("SU".equalsIgnoreCase(evCap.p_IdTipoEventoCapital) && !evCap.p_EsEP) {
					mapMsg.clear();
					mapMsg.put("IdTipoEventoCapital","SU");
					out.P_OUT_MENSAJES.add(new MensajeSalida(Id, MensajeSalida.MENSAJE_ERROR, "UI_ERR_TIPO_EVENTO_NO_PERMITIDO", mapMsg, conn, in.p_IdEmpresa));
					Id++;
					eventoError.p_Secuencia = evCap.p_Secuencia;
					eventoError.p_Nemo = evCap.p_Nemo;
					eventoError.p_IdTipoEventoCapital = evCap.p_IdTipoEventoCapital;
					eventoError.p_FechaLimite = evCap.p_FechaLimite;
					eventoError.p_FechaPago = evCap.p_FechaPago;
					eventoError.p_Mensaje = Diccionario.getMensaje(conn, in.p_IdEmpresa, "UI_ERR_TIPO_EVENTO_NO_PERMITIDO", mapMsg);
					eventosErrores.put(evCap.p_Secuencia, eventoError);
					out.P_OUT_EVENTOS_ERRORES.add(eventoError);
				}
				
				if (evCap.p_IdTipoEventoCapital.equalsIgnoreCase("DV") && (evCap.p_IdMoneda.equalsIgnoreCase("PEN") || evCap.p_IdMoneda.equalsIgnoreCase("COP")) && !evCap.p_IdMonedaLiquidacion.equalsIgnoreCase("USD")){
					mapMsg.clear();
					mapMsg.put("Moneda", evCap.p_IdMoneda.toString());
					out.P_OUT_MENSAJES.add(new MensajeSalida(Id, MensajeSalida.MENSAJE_ERROR, "UI_ERROR_MONEDA_LIQ_EVENTO_DV", mapMsg, conn, in.p_IdEmpresa));
					Id++;
					eventoError.p_Secuencia = evCap.p_Secuencia;
					eventoError.p_Nemo = evCap.p_Nemo;
					eventoError.p_IdTipoEventoCapital = evCap.p_IdTipoEventoCapital;
					eventoError.p_FechaLimite = evCap.p_FechaLimite;
					eventoError.p_FechaPago = evCap.p_FechaPago;
					eventoError.p_Mensaje = Diccionario.getMensaje(conn, in.p_IdEmpresa, "UI_ERROR_MONEDA_LIQ_EVENTO_DV", mapMsg);
					eventosErrores.put(evCap.p_Secuencia, eventoError);
					out.P_OUT_EVENTOS_ERRORES.add(eventoError);
				}
				if (evCap.p_ImpuestoDivExtranjero.compareTo(BigDecimal.ZERO) < 0 || evCap.p_ImpuestoDivExtranjero.compareTo(new BigDecimal(100)) > 0){
					mapMsg.clear();
					out.P_OUT_MENSAJES.add(new MensajeSalida(Id, MensajeSalida.MENSAJE_ERROR, "UI_ERR_VALOR_IMP_DV_EXTRANJERO", mapMsg, conn, in.p_IdEmpresa));
					Id++;
					eventoError.p_Secuencia = evCap.p_Secuencia;
					eventoError.p_Nemo = evCap.p_Nemo;
					eventoError.p_IdTipoEventoCapital = evCap.p_IdTipoEventoCapital;
					eventoError.p_FechaLimite = evCap.p_FechaLimite;
					eventoError.p_FechaPago = evCap.p_FechaPago;
					eventoError.p_Mensaje = Diccionario.getMensaje(conn, in.p_IdEmpresa, "UI_ERR_VALOR_IMP_DV_EXTRANJERO", mapMsg);
					eventosErrores.put(evCap.p_Secuencia, eventoError);
					out.P_OUT_EVENTOS_ERRORES.add(eventoError);
				}
				if (!evCap.p_IdTipoEventoCapital.equalsIgnoreCase("DV") && evCap.p_ImpuestoDivExtranjero.compareTo(BigDecimal.ZERO) != 0) {
					mapMsg.clear();
					mapMsg.put("Evento", evCap.p_IdTipoEventoCapital.toString());
					out.P_OUT_MENSAJES.add(new MensajeSalida(Id, MensajeSalida.MENSAJE_ADVERTENCIA, "UI_ADV_EVENTO_IMP_DV_EXTRANJERO", mapMsg, conn, in.p_IdEmpresa));
					Id++;
					evCap.p_ImpuestoDivExtranjero = BigDecimal.ZERO;
				}
				String aplicaImpuestoDvExt = mapConsultaImpuestoDvExt.get(evCap.p_IdEmisor) != null ? mapConsultaImpuestoDvExt.get(evCap.p_IdEmisor).AplicaImpuesto : null;
				if (aplicaImpuestoDvExt != null && aplicaImpuestoDvExt.equalsIgnoreCase("N") && evCap.p_ImpuestoDivExtranjero.compareTo(BigDecimal.ZERO) != 0) {
					mapMsg.clear();
					mapMsg.put("Nemo", evCap.p_Nemo.toString());					
					out.P_OUT_MENSAJES.add(new MensajeSalida(Id, MensajeSalida.MENSAJE_ADVERTENCIA, "UI_ADV_NEMO_IMP_DV_EXTRANJERO", mapMsg, conn, in.p_IdEmpresa));
					Id++;
					evCap.p_ImpuestoDivExtranjero = BigDecimal.ZERO;
				}
				if (evCap.p_GastoTransferencia.compareTo(BigDecimal.ZERO) < 0){
					mapMsg.clear();
					out.P_OUT_MENSAJES.add(new MensajeSalida(Id, MensajeSalida.MENSAJE_ERROR, "UI_ERR_VALOR_GASTO_TRANSFERENCIA", mapMsg, conn, in.p_IdEmpresa));
					Id++;
					eventoError.p_Secuencia = evCap.p_Secuencia;
					eventoError.p_Nemo = evCap.p_Nemo;
					eventoError.p_IdTipoEventoCapital = evCap.p_IdTipoEventoCapital;
					eventoError.p_FechaLimite = evCap.p_FechaLimite;
					eventoError.p_FechaPago = evCap.p_FechaPago;
					eventoError.p_Mensaje = Diccionario.getMensaje(conn, in.p_IdEmpresa, "UI_ERR_VALOR_GASTO_TRANSFERENCIA", mapMsg);
					eventosErrores.put(evCap.p_Secuencia, eventoError);
					out.P_OUT_EVENTOS_ERRORES.add(eventoError);
				}
				if (!evCap.p_IdTipoEventoCapital.equalsIgnoreCase("DV") && evCap.p_GastoTransferencia.compareTo(BigDecimal.ZERO) != 0) {
					mapMsg.clear();
					mapMsg.put("Evento", evCap.p_IdTipoEventoCapital.toString());
					out.P_OUT_MENSAJES.add(new MensajeSalida(Id, MensajeSalida.MENSAJE_ADVERTENCIA, "UI_ADV_EVENTO_GASTO_TRANSFERENCIA", mapMsg, conn, in.p_IdEmpresa));
					Id++;
					evCap.p_GastoTransferencia = BigDecimal.ZERO;
				}
				String aplicaGastoTransferencia = mapConsultaImpuestoDvExt.get(evCap.p_IdEmisor) != null ? mapConsultaImpuestoDvExt.get(evCap.p_IdEmisor).AplicaGastoTransferencia : null;
				if (aplicaGastoTransferencia != null && aplicaGastoTransferencia.equalsIgnoreCase("N") && evCap.p_GastoTransferencia.compareTo(BigDecimal.ZERO) != 0) {
					mapMsg.clear();
					mapMsg.put("Nemo", evCap.p_Nemo.toString());					
					out.P_OUT_MENSAJES.add(new MensajeSalida(Id, MensajeSalida.MENSAJE_ADVERTENCIA, "UI_ADV_NEMO_GASTO_TRANSFERENCIA", mapMsg, conn, in.p_IdEmpresa));
					Id++;
					evCap.p_GastoTransferencia = BigDecimal.ZERO;
				}			
				if ((evCap.p_IdTipoEventoCapital.equalsIgnoreCase("DS") || evCap.p_IdTipoEventoCapital.equalsIgnoreCase("CN")) && evCap.p_Nemo.equalsIgnoreCase(evCap.p_NemoNuevo)){
					mapMsg.clear();
					mapMsg.put("Nemo", evCap.p_Nemo.toString());
					mapMsg.put("NemoNuevo",evCap.p_NemoNuevo.toString());
					out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"DIVISION_SOCIEDAD_INSTRUMENTO_IGUALES",mapMsg,conn,in.p_IdEmpresa));
					Id++;
					eventoError.p_Secuencia = evCap.p_Secuencia;
					eventoError.p_Nemo = evCap.p_Nemo;
					eventoError.p_IdTipoEventoCapital = evCap.p_IdTipoEventoCapital;
					eventoError.p_FechaLimite = evCap.p_FechaLimite;
					eventoError.p_FechaPago = evCap.p_FechaPago;
					eventoError.p_Mensaje = Diccionario.getMensaje(conn, in.p_IdEmpresa, "DIVISION_SOCIEDAD_INSTRUMENTO_IGUALES", mapMsg);
					eventosErrores.put(evCap.p_Secuencia, eventoError);
					out.P_OUT_EVENTOS_ERRORES.add(eventoError);
				}
				
				if (evCap.p_IdTipoEventoCapital.equalsIgnoreCase("CN")){
					for (Instrumento ins: instrumento){
						if (evCap.p_IdInstrumento.equals(ins.IdInstrumento) && !evCap.p_IdTipoInstrumento.equalsIgnoreCase(ins.IdTipoInstrumento)){
							mapMsg.clear();
							mapMsg.put("Nemo1", evCap.p_Nemo.toString());
							mapMsg.put("Nemo2",evCap.p_NemoNuevo.toString());
							out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"CAMBIONEMO_TIPOINSTRUMENTO_NO_COINCIDE_NUEVO_INSTRUMENTO",mapMsg,conn,in.p_IdEmpresa));
							Id++;
							eventoError.p_Secuencia = evCap.p_Secuencia;
							eventoError.p_Nemo = evCap.p_Nemo;
							eventoError.p_IdTipoEventoCapital = evCap.p_IdTipoEventoCapital;
							eventoError.p_FechaLimite = evCap.p_FechaLimite;
							eventoError.p_FechaPago = evCap.p_FechaPago;
							eventoError.p_Mensaje = Diccionario.getMensaje(conn, in.p_IdEmpresa, "CAMBIONEMO_TIPOINSTRUMENTO_NO_COINCIDE_NUEVO_INSTRUMENTO", mapMsg);
							eventosErrores.put(evCap.p_Secuencia, eventoError);
							out.P_OUT_EVENTOS_ERRORES.add(eventoError);
						}
						if (evCap.p_IdInstrumento.equals(ins.IdInstrumento) && !evCap.p_IdClasificacionTipoInstrumento.equalsIgnoreCase(ins.IdClasificacionTipoInstrumento)){
							mapMsg.clear();
							mapMsg.put("Nemo1", evCap.p_Nemo.toString());
							mapMsg.put("Nemo2",evCap.p_NemoNuevo.toString());
							out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"CAMBIONEMO_CLASIFICACIONTIPOINSTRUMENTO_NO_COINCIDE_NUEVO_INSTRUMENTO",mapMsg,conn,in.p_IdEmpresa));
							Id++;
							eventoError.p_Secuencia = evCap.p_Secuencia;
							eventoError.p_Nemo = evCap.p_Nemo;
							eventoError.p_IdTipoEventoCapital = evCap.p_IdTipoEventoCapital;
							eventoError.p_FechaLimite = evCap.p_FechaLimite;
							eventoError.p_FechaPago = evCap.p_FechaPago;
							eventoError.p_Mensaje = Diccionario.getMensaje(conn, in.p_IdEmpresa, "CAMBIONEMO_CLASIFICACIONTIPOINSTRUMENTO_NO_COINCIDE_NUEVO_INSTRUMENTO", mapMsg);
							eventosErrores.put(evCap.p_Secuencia, eventoError);
							out.P_OUT_EVENTOS_ERRORES.add(eventoError);
						}
					}
				}
				if (evCap.p_IdTipoEventoCapital.equalsIgnoreCase("PB")){
					for (Instrumento ins: instrumentoBono){
						if (evCap.p_IdInstrumento.equals(ins.IdInstrumento)){
							if (ins.CallConfirmado == null || ins.CallConfirmado.equals("") || ins.CallConfirmado.equalsIgnoreCase("N")){
								mapMsg.clear();
								out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"BONO_SIN_CALL_CONFIRMADO",mapMsg,conn,in.p_IdEmpresa));
								Id++;
								eventoError.p_Secuencia = evCap.p_Secuencia;
								eventoError.p_Nemo = evCap.p_Nemo;
								eventoError.p_IdTipoEventoCapital = evCap.p_IdTipoEventoCapital;
								eventoError.p_FechaLimite = evCap.p_FechaLimite;
								eventoError.p_FechaPago = evCap.p_FechaPago;
								eventoError.p_Mensaje = Diccionario.getMensaje(conn, in.p_IdEmpresa, "BONO_SIN_CALL_CONFIRMADO", valoresDiccionario);
								eventosErrores.put(evCap.p_Secuencia, eventoError);
								out.P_OUT_EVENTOS_ERRORES.add(eventoError);
								
								
							}
							if (ins.CuponeraVariable == null || ins.CuponeraVariable.equals("") || ins.CuponeraVariable.equalsIgnoreCase("N")){
								mapMsg.clear();
								out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"BONO_SIN_CUPONERA",mapMsg,conn,in.p_IdEmpresa));
								Id++;
								eventoError.p_Secuencia = evCap.p_Secuencia;
								eventoError.p_Nemo = evCap.p_Nemo;
								eventoError.p_IdTipoEventoCapital = evCap.p_IdTipoEventoCapital;
								eventoError.p_FechaLimite = evCap.p_FechaLimite;
								eventoError.p_FechaPago = evCap.p_FechaPago;
								eventoError.p_Mensaje = Diccionario.getMensaje(conn, in.p_IdEmpresa, "BONO_SIN_CUPONERA", valoresDiccionario);
								eventosErrores.put(evCap.p_Secuencia, eventoError);
								out.P_OUT_EVENTOS_ERRORES.add(eventoError);
							}
						}
					}
					for (Instrumento ins: instrumentoCupon){
						if (evCap.p_IdInstrumento.equals(ins.IdInstrumento)){
							if (ins.FechaPagoCupon.equals(Fecha.toDate(evCap.p_FechaPago))){
								mapMsg.clear();
								out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"FECHA_PAGO_NO_COINCIDE_CON_FECHA_CORTE",mapMsg,conn,in.p_IdEmpresa));
								Id++;
								eventoError.p_Secuencia = evCap.p_Secuencia;
								eventoError.p_Nemo = evCap.p_Nemo;
								eventoError.p_IdTipoEventoCapital = evCap.p_IdTipoEventoCapital;
								eventoError.p_FechaLimite = evCap.p_FechaLimite;
								eventoError.p_FechaPago = evCap.p_FechaPago;
								eventoError.p_Mensaje = Diccionario.getMensaje(conn, in.p_IdEmpresa, "FECHA_PAGO_NO_COINCIDE_CON_FECHA_CORTE", valoresDiccionario);
								eventosErrores.put(evCap.p_Secuencia, eventoError);
								out.P_OUT_EVENTOS_ERRORES.add(eventoError);
							}
						}
					}
				}
				if ( "RUE".equalsIgnoreCase(evCap.p_Origen) && ( evCap.p_IdTipoEventoCapital.equalsIgnoreCase("DV") || evCap.p_IdTipoEventoCapital.equalsIgnoreCase("RC")) ) {
					
					VariacionCapitalLog nuevaMonedaCalculoFactor = obtenerMonedasConFactores
																	.stream()
																	.filter(item -> item.Secuencia.compareTo( BigDecimal.valueOf(evCap.p_Secuencia) ) == 0 )
																	.findFirst()
																	.orElse(new VariacionCapitalLog());
					
					if( "CLP".equalsIgnoreCase(nuevaMonedaCalculoFactor.IdMoneda) ) {
						out.P_OUT_MENSAJES.add(new MensajeSalida(Id, MensajeSalida.MENSAJE_ADVERTENCIA, "UI_NO_EXISTE_FACTOR_TABLA_VARIACIONCAPIALLOG", new HashMap<String,String>(), conn, in.p_IdEmpresa));
					}
				}
				if(evCap.p_FechaPago.equals("")) {
					mapMsg.clear();
					out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"FECHA_PAGO_VACIA",mapMsg,conn,in.p_IdEmpresa));
					Id++;
				}
			}
		
			
		}
		{
			//obtengo secuencias
			List<Long> secuenciaEventoCapital= Utils.getSecuencia(conn, "BSCL.Custodia.BD.Secuencias::EventoCapital", new Long(in.p_EventoCapital.size()));
			List<Evento> datosInsert = new ArrayList<>();
			//List<AgregarLog.InAgregarLog.DetalleLog> detalleLogList = new ArrayList<>();
			List<AgregarLog.InAgregarLog.Log> logList = new ArrayList<>();
			for (Evento evCap: in.p_EventoCapital){
				if(eventosErrores.containsKey(evCap.p_Secuencia)){
					continue;
				} else {
					Evento inEv = new Evento();
					inEv.p_IdEventoCapital	= secuenciaEventoCapital.get(in.p_EventoCapital.indexOf(evCap));//  --  "IdEventoCapital",
					inEv.p_IdEmpresa		= in.p_IdEmpresa;//--  "IdEmpresa",
					inEv.p_IdTipoEventoCapital = evCap.p_IdTipoEventoCapital;//,                          --  "IdTipoEventoCapital",
					// SE COMENTA ESTO YA QUE LA SECUENCIA A ALMACENAR DEBE SER LA SECUENCIA INGRESADA POR PANTALLA.
					inEv.p_Secuencia		= evCap.p_Secuencia;//                              --  "Secuencia",
					inEv.p_Descripcion		= evCap.p_Descripcion;//                                  --  "Descripcion",
					inEv.p_EsInscrito		= evCap.p_EsInscrito;//                                   --  "EsInscrito",
					inEv.p_IdClasificacionTipoInstrumento = evCap.p_IdClasificacionTipoInstrumento;//               --  "IdClasificacionTipoInstrumento",
					inEv.p_IdTipoInstrumento	= evCap.p_IdTipoInstrumento;//                            --  "IdTipoInstrumento",
					if(evCap.p_IdTipoEventoCapital.equalsIgnoreCase("DF")) {
						inEv.p_IdInstrumento = !"".equalsIgnoreCase(evCap.p_Nemo) ? ((Instrumento) idInstrumentoMap.get(evCap.p_Nemo+"_"+evCap.p_IdTipoInstrumento)).IdInstrumento : 0L;
					}else {
						inEv.p_IdInstrumento = evCap.p_IdInstrumento;
					}
					inEv.p_Nemo					= evCap.p_Nemo;//                                         --  "Nemo",
					inEv.p_Factor				= evCap.p_Factor;//                                       --  "Factor",
				    inEv.p_Impuesto				= evCap.p_Impuesto;//                                     --  "impuesto"
				    inEv.p_Valor				= evCap.p_IdTipoEventoCapital.equalsIgnoreCase("EP")?evCap.p_FactorNuevo:BigDecimal.ZERO;//,                                        --  "Valor",
				    inEv.p_IdMoneda				= evCap.p_IdMoneda.equals("") ? monedaSistema : evCap.p_IdMoneda; //,                                     --  "IdMoneda",
				    inEv.p_IdBoveda				=  evCap.p_IdBoveda ; //,                                     --  "IdBoveda",
				    inEv.p_IdMonedaLiquidacion	= evCap.p_IdMonedaLiquidacion.equals("") ? monedaSistema : evCap.p_IdMonedaLiquidacion;//,
				    inEv.p_Paridad				= evCap.p_Paridad;//,
				    inEv.p_IdInstrumentoNuevo	= evCap.p_IdInstrumentoNuevo;//,                           --  "IdInstrumentoNuevo",
				    inEv.p_NemoNuevo			= evCap.p_NemoNuevo;//,                                    --  "NemoNuevo",
				    inEv.p_FactorNuevo			= evCap.p_FactorNuevo;//,                                  --  "FactorNuevo",
				    inEv.p_FechaActualizacion	= evCap.p_FechaActualizacion;//   --  "FechaActualizacion",   
				    inEv.p_FechaLimite			= evCap.p_FechaLimite;//,           --  "FechaLimite",
				    inEv.p_FechaPago			= evCap.p_FechaPago;//,             --  "FechaPago",
				    inEv.p_NumeroDividendo		= evCap.p_NumeroDividendo;//                              --  "NumeroDividendo",
				    inEv.p_IdTipoDividendo		= "P";//                                            --  "IdTipoDividendo",
				    inEv.p_PrecioAnterior		= BigDecimal.ZERO;//,                                              --  "PrecioAnterior",
				    inEv.p_FechaHoraIngresoRegistro		= fechaSistema;//                                 --  "FechaHoraIngresoRegistro",
				    inEv.p_FechaHoraUltimaActualizacion	= fechaSistema;//             --  "FechaHoraUltimaActualizacion",
				    inEv.p_TipoIngreso			= "C";//                                  --  "TipoIngreso",
				    inEv.p_IdUsuario			= in.p_IdUsuario;//                                              --  "IdUsuario",
				    inEv.p_IdEstado				= "VIG";//                                          --  "IdEstado"
				    inEv.p_Origen				= evCap.p_Origen;  //                                        --  "Origen"
				    inEv.p_IdEmisor				= evCap.p_IdEmisor;//,
				    inEv.p_IdEmisorNuevo		= evCap.p_IdEmisorNuevo;//,
				    inEv.p_Cuotas				= evCap.p_Cuotas;
				    inEv.p_Monto				= evCap.p_Monto;
				    inEv.p_Indice 				= evCap.p_Indice;
				    inEv.p_EsISFUT				= evCap.p_EsISFUT;
				    inEv.p_TasaEfectiva			= evCap.p_TasaEfectiva;
				    inEv.p_SecuenciaRelacionada = evCap.p_SecuenciaRelacionada;
				    inEv.p_SuscripcionComplementaria = evCap.p_SuscripcionComplementaria;
				    inEv.p_ImpuestoDivExtranjero	 = evCap.p_ImpuestoDivExtranjero;
				    inEv.p_GastoTransferencia	 	 = evCap.p_GastoTransferencia;
				    datosInsert.add(inEv);
				    
				    mapMsg.clear();
				    mapMsg.put("p_Nemo", evCap.p_Nemo.toString());
				    mapMsg.put("p_Descripcion", evCap.p_Descripcion.toString());
					out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_OK,"UI_EVENTO_CAPITAL_CREADO_MANUAL",mapMsg,conn,in.p_IdEmpresa));
					out.P_OUT_SECUENCIA.add(inEv.p_IdEventoCapital);
					Id++;
					
					//parametros log
					AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
					log.IdTrabajo		= entradaAgregarLog.p_IdTrabajo; 
					log.IdCabeceraLog	= inEv.p_IdEventoCapital; 
					log.IdTipoLog		= "INGRESO"; 
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
					log.IdTipoRegistro			= "INGRESAREVENTOCAPITAL"; 
					log.IdRegistro				= inEv.p_IdEventoCapital.toString(); 
					log.EstadoRegistro			= ""; 
					log.IdTipoOperacion			= ""; 
					log.Folio					= 0L; 
					log.Cantidad				= BigDecimal.ZERO; 
					log.PrecioTasa				= BigDecimal.ZERO; 
					log.Monto					= BigDecimal.ZERO; 
					log.IdMonedaOrigen			= inEv.p_IdMoneda; 
					log.IdMonedaLiquidacion		= inEv.p_IdMonedaLiquidacion; 
					log.TipoCambio				= ""; 
					log.Nemo					= inEv.p_Nemo; 
					log.IdProceso				= 0L; 
					log.EstadoProceso			= ""; 
					log.NombreProceso			= "cl.bcs.custodia.evc.CrearEventoCapital";
					logList.add(log);

					entradaAgregarLog.p_LogDetalle.add(agregarDetalleLog(log.IdCabeceraLog, "INGRESAREVENTOCAPITAL", "IdTipoEventoCapital", "", inEv.p_IdTipoEventoCapital));
					entradaAgregarLog.p_LogDetalle.add(agregarDetalleLog(log.IdCabeceraLog, "INGRESAREVENTOCAPITAL", "Factor", "", inEv.p_Factor.toString()));
					entradaAgregarLog.p_LogDetalle.add(agregarDetalleLog(log.IdCabeceraLog, "INGRESAREVENTOCAPITAL", "FechaLimite", "", inEv.p_FechaLimite));
					entradaAgregarLog.p_LogDetalle.add(agregarDetalleLog(log.IdCabeceraLog, "INGRESAREVENTOCAPITAL", "FechaPago", "", inEv.p_FechaPago));
					entradaAgregarLog.p_LogDetalle.add(agregarDetalleLog(log.IdCabeceraLog, "INGRESAREVENTOCAPITAL", "NumeroDividendo", "", inEv.p_NumeroDividendo.toString()));
					entradaAgregarLog.p_LogDetalle.add(agregarDetalleLog(log.IdCabeceraLog, "INGRESAREVENTOCAPITAL", "Descripcion", "", inEv.p_Descripcion));
					entradaAgregarLog.p_LogDetalle.add(agregarDetalleLog(log.IdCabeceraLog, "INGRESAREVENTOCAPITAL", "IdEmisor", "", inEv.p_IdEmisor));
					entradaAgregarLog.p_LogDetalle.add(agregarDetalleLog(log.IdCabeceraLog, "INGRESAREVENTOCAPITAL", "IdBoveda", "", inEv.p_IdBoveda));
					entradaAgregarLog.p_LogDetalle.add(agregarDetalleLog(log.IdCabeceraLog, "INGRESAREVENTOCAPITAL", "Secuencia", "", inEv.p_Secuencia.toString()));
					entradaAgregarLog.p_LogDetalle.add(agregarDetalleLog(log.IdCabeceraLog, "INGRESAREVENTOCAPITAL", "EsInscrito", "", inEv.p_EsInscrito));
					entradaAgregarLog.p_LogDetalle.add(agregarDetalleLog(log.IdCabeceraLog, "INGRESAREVENTOCAPITAL", "EsISFUT", "", inEv.p_EsISFUT));
					entradaAgregarLog.p_LogDetalle.add(agregarDetalleLog(log.IdCabeceraLog, "INGRESAREVENTOCAPITAL", "TasaEfectiva", "", inEv.p_TasaEfectiva.toString()));
					entradaAgregarLog.p_LogDetalle.add(agregarDetalleLog(log.IdCabeceraLog, "INGRESAREVENTOCAPITAL", "Paridad", "", inEv.p_Paridad.toString()));
					entradaAgregarLog.p_LogDetalle.add(agregarDetalleLog(log.IdCabeceraLog, "INGRESAREVENTOCAPITAL", "Valor", "", inEv.p_Valor.toString()));
					entradaAgregarLog.p_LogDetalle.add(agregarDetalleLog(log.IdCabeceraLog, "INGRESAREVENTOCAPITAL", "ImpuestoDivExtranjero", "", inEv.p_ImpuestoDivExtranjero.toString()));					
					entradaAgregarLog.p_LogDetalle.add(agregarDetalleLog(log.IdCabeceraLog, "INGRESAREVENTOCAPITAL", "GastoTransferencia", "", inEv.p_GastoTransferencia.toString()));
				}
								
			}
			
			entradaAgregarLog.p_Log = logList;
			_sql.batch(conn, SQL_INSERT_EVENTO_CAPITAL, datosInsert, Evento.class);
		}
		//AGREGAR LOG
		{
			AgregarLog agregarLogService = new AgregarLog();
			agregarLogService.logica(conn, entradaAgregarLog);
		}
				
				
		logger.info("FIN::CUS.CrearEventoCapitalManual");
		return out;
	
	}
	private EventoCapital_OUT CrearEventoCapitalArchivo (Connection conn, EventoCapital_IN in)  throws Exception {
		SQL _sql = new SQL();
		Long Id = 0L;
		//Long countInsertReserva = 0L;
		HashMap<String,String> mapMsg = new HashMap<>();
		logger.info("INICIO::CUS.CrearEventoCapitalArchivo");
		EventoCapital_OUT out = new EventoCapital_OUT();
		HashMap<String, Object> params = new HashMap<>();
		List<String> nemo= new ArrayList<>();
		List<EventoExiste> eventoExistente = new ArrayList<>();
		List<Instrumento> idInstrumento = new ArrayList<>();
		Map<String, Object> idInstrumentoMap = new HashMap<>();
		AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
		entradaAgregarLog.p_IdTrabajo = 1L;
		
		String monedaSistema    	= ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.MonedaSistema");
		String fechaSistema 		= ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		for (Archivo ev: in.p_DataArchivo){
			nemo.add(ev.NEMO.trim());
		}
		/*
		if (numeroDividendo.size() > 0){
			params = new HashMap<>();
			params.put("p_IdEmpresa", in.p_IdEmpresa);
			//params.put("p_Secuencia", secuenciasEvento);
			params.put("p_NumeroDividendo", numeroDividendo);
			eventoExistente = _sql.query(conn, SQL_SELECT_EVENTO_CAPITAL, params, EventoExiste.class);
		}*/
		if (nemo.size() > 0){
			params = new HashMap<>();
			params.put("p_Nemo", nemo);
			params.put("FiltroIdClasificacionTipoInstrumento",  1);
			params.put("p_IdClasificacionTipoInstrumento", "RF"); //el flujo de archivo (sorteo de letras) pertenece al mercado rf 
			idInstrumento = _sql.query(conn, SQL_SELECT_ID_INSTRUMENTO, params, Instrumento.class);
			idInstrumentoMap = Utils.List2Hash(idInstrumento, "Nemotecnico");
		}
		
		//::Validaciones
		{
			if (nemo.size() <= 0){
				// No Viene Evento
				mapMsg.clear();
				out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"SIN_DATOS_A_PROCESAR",mapMsg,conn,in.p_IdEmpresa));
				Id++;
			}
			for (Archivo evCap: in.p_DataArchivo){
				String tipoIns = idInstrumentoMap!=null && idInstrumentoMap.size() > 0?((Instrumento)idInstrumentoMap.get(evCap.NEMO.trim())).IdTipoInstrumento:null;
				if (tipoIns == null || !tipoIns.equalsIgnoreCase("LTH")){
					mapMsg.clear();
					mapMsg.put("Nemo", evCap.NEMO);
					out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ADVERTENCIA,"INSTRUMENTO_NO_VALIDO",mapMsg,conn,in.p_IdEmpresa));
					Id++;
				}
				if (evCap.MONEDA.compareTo(BigDecimal.ZERO) < 0 || evCap.MONEDA.compareTo(BigDecimal.ZERO) == 0){
					mapMsg.clear();
					out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"MONEDA_NO_PUEDE_SER_INFERIOR_O_IGUAL_A_CERO",mapMsg,conn,in.p_IdEmpresa));
					Id++;
				}
				if (evCap.NEMO.trim().equals("")){
					mapMsg.clear();
					out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"NEMO_NO_PUEDE_VENIR_EN_BLANCO",mapMsg,conn,in.p_IdEmpresa));
					Id++;
				}
				if (evCap.SORTEADO.compareTo(BigDecimal.ZERO) <= 0){
					mapMsg.clear();
					out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"SORTEADO_NO_PUEDE_SER_INFERIOR_O_IGUAL_A_CERO",mapMsg,conn,in.p_IdEmpresa));
					Id++;
				}
				if (evCap.APAGAR.compareTo(BigDecimal.ZERO)	<=	0){
					mapMsg.clear();
					out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"A_PAGAR_NO_PUEDE_SER_INFERIOR_O_IGUAL_A_CERO",mapMsg,conn,in.p_IdEmpresa));
					Id++;
				}
				if (evCap.APAGARPESOS.compareTo(BigDecimal.ZERO) <=	0){
					mapMsg.clear();
					out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"A_PAGAR_EN_PESOS_NO_PUEDE_SER_INFERIOR_O_IGUAL_A_CERO",mapMsg,conn,in.p_IdEmpresa));
					Id++;
				}
				if (evCap.EVENTO.equals("")){
					mapMsg.clear();
					out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"FECHA_PAGO_VACIA",mapMsg,conn,in.p_IdEmpresa));
					Id++;
				}
			}
			if (MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES) > 0){
				mapMsg.clear();
				out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ADVERTENCIA,"ARCHIVO_SORTEO_LETRAS_CON_ERRORES_REVISAR",mapMsg,conn,in.p_IdEmpresa));
				Id++;
				return out;
			}
		}
		{
			//obtengo secuencias
			List<Long> secuenciaEventoCapital= Utils.getSecuencia(conn, "BSCL.Custodia.BD.Secuencias::EventoCapital", new Long(in.p_DataArchivo.size()));
			List<Long> eventoCapitalSecuencia= Utils.getSecuencia(conn, "BSCL.Custodia.BD.Secuencias::EventoCapitalSecuencia", new Long(in.p_DataArchivo.size()));
			List<Evento> datosInsert = new ArrayList<>();
			List<AgregarLog.InAgregarLog.Log> logList = new ArrayList<>();
			for (Archivo evCap: in.p_DataArchivo){
				String tipoIns = idInstrumentoMap!=null && idInstrumentoMap.size() > 0?((Instrumento)idInstrumentoMap.get(evCap.NEMO.trim())).IdTipoInstrumento:null;
				if (tipoIns == null || !tipoIns.equalsIgnoreCase("LTH")){
					continue;
				}
				Evento inEv = new Evento();
				inEv.p_IdEventoCapital	= secuenciaEventoCapital.get(in.p_DataArchivo.indexOf(evCap));//  --  "IdEventoCapital",
				inEv.p_IdEmpresa		= in.p_IdEmpresa;//--  "IdEmpresa",
				inEv.p_IdTipoEventoCapital = evCap.IDTIPOEVENTOCAPITAL;//,                          --  "IdTipoEventoCapital",
				// SE COMENTA ESTO YA QUE LA SECUENCIA A ALMACENAR DEBE SER LA SECUENCIA INGRESADA POR PANTALLA.
				inEv.p_Secuencia		= eventoCapitalSecuencia.get(in.p_DataArchivo.indexOf(evCap));//                              --  "Secuencia",
				inEv.p_Descripcion		= "Sorteo de Letras DCV " + evCap.NEMO;//                                  --  "Descripcion",
				inEv.p_EsInscrito		= "N";//                                   --  "EsInscrito",
				inEv.p_IdClasificacionTipoInstrumento = ((Instrumento)idInstrumentoMap.get(evCap.NEMO.trim())).IdClasificacionTipoInstrumento;//               --  "IdClasificacionTipoInstrumento",
				inEv.p_IdTipoInstrumento	= ((Instrumento)idInstrumentoMap.get(evCap.NEMO.trim())).IdTipoInstrumento;//                            --  "IdTipoInstrumento",
				inEv.p_IdInstrumento		= ((Instrumento)idInstrumentoMap.get(evCap.NEMO.trim())).IdInstrumento;//, --   "IdInstrumento",
				inEv.p_Nemo					= ((Instrumento)idInstrumentoMap.get(evCap.NEMO.trim())).Nemotecnico;//                                         --  "Nemo",
				inEv.p_Factor				= BigDecimal.ONE;//                                       --  "Factor",
			    inEv.p_Impuesto				= BigDecimal.ZERO;//                                     --  "impuesto"
			    inEv.p_Valor				= BigDecimal.ZERO;//,                                        --  "Valor",
			    inEv.p_IdMoneda				= evCap.GLOSAMONEDA.trim(); //,                                     --  "IdMoneda",
			    inEv.p_IdBoveda	= "";
			    inEv.p_IdMonedaLiquidacion	= monedaSistema;//,
			    inEv.p_Paridad				= BigDecimal.ONE;//,
			    inEv.p_IdInstrumentoNuevo	= 0L;//,                           --  "IdInstrumentoNuevo",
			    inEv.p_NemoNuevo			= "";//,                                    --  "NemoNuevo",
			    inEv.p_FactorNuevo			= BigDecimal.ONE;//,                                  --  "FactorNuevo",
			    inEv.p_FechaActualizacion	= fechaSistema;//   --  "FechaActualizacion",   
			    inEv.p_FechaLimite			= evCap.EVENTO;//,           --  "FechaLimite",
			    inEv.p_FechaPago			= evCap.EVENTO;//,             --  "FechaPago",
			    inEv.p_NumeroDividendo		= inEv.p_IdEventoCapital;//0L;//                              --  "NumeroDividendo",
			    inEv.p_IdTipoDividendo		= "";//                                            --  "IdTipoDividendo",
			    inEv.p_PrecioAnterior		= BigDecimal.ZERO;//,                                              --  "PrecioAnterior",
			    inEv.p_FechaHoraIngresoRegistro		= fechaSistema;//                                 --  "FechaHoraIngresoRegistro",
			    inEv.p_FechaHoraUltimaActualizacion	= fechaSistema;//             --  "FechaHoraUltimaActualizacion",
			    inEv.p_TipoIngreso			= "S";//                                  --  "TipoIngreso",
			    inEv.p_IdUsuario			= in.p_IdUsuario;//                                              --  "IdUsuario",
			    inEv.p_IdEstado				= "VIG";//                                          --  "IdEstado"
			    inEv.p_Origen				= "MAN";  //                                        --  "Origen"
			    inEv.p_IdEmisor				= ((Instrumento)idInstrumentoMap.get(evCap.NEMO.trim())).IdEmisor;//,
			    inEv.p_IdEmisorNuevo		= "";//,
			    inEv.p_CuentaDepositanteSorteoLetras = evCap.DEPOSITANTE; //"CuentaDepositanteSorteoLetras"	,
			    inEv.p_MonedaSorteoLetras = evCap.MONEDA;				//								-- "MonedaSorteoLetras"				,
			    inEv.p_GlosaMonedaSorteoLetras = evCap.GLOSAMONEDA;		//									-- "GlosaMonedaSorteoLetras"		,
				inEv.p_EmisorPagadorSorteoLetras = evCap.EMISORPAGADOR;	//									-- "EmisorPagadorSorteoLetras"		,
				inEv.p_EmisorSorteoLetras = evCap.EMISOR;//,												-- "EmisorSorteoLetras"				,
				inEv.p_InstrumentoSorteoLetras = evCap.INSTRUMENTO;//,											-- "InstrumentoSorteoLetras"		,
				inEv.p_GlosaInstrumentoSorteoLetras = evCap.GLOSAINSTRUMENTO;//,										-- "GlosaInstrumentoSorteoLetras"	,
				inEv.p_BloqueoSorteoLetras = evCap.BLOQUEO;//,												-- "BloqueoSorteoLetras"			,
				inEv.p_SorteadoSorteoLetras = evCap.SORTEADO;//,												-- "SorteadoSorteoLetras"			,
				inEv.p_APagarSorteoLetras = evCap.APAGAR;//,												-- "APagarSorteoLetras"				,
				inEv.p_APagarPesosSorteoLetras = evCap.APAGARPESOS;//,											-- "APagarPesosSorteoLetras"		,
				inEv.p_EsISFUT				= "";
				inEv.p_TasaEfectiva			= BigDecimal.ONE;
			    
				datosInsert.add(inEv);
			    
			    mapMsg.clear();
			    mapMsg.put("p_NumeroDividendo", inEv.p_NumeroDividendo.toString());
				out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_OK,"UI_EVENTO_CAPITAL_CREADO",mapMsg,conn,in.p_IdEmpresa));
				out.P_OUT_SECUENCIA.add(inEv.p_IdEventoCapital);
				Id++;
				
				//parametros log
				AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
				log.IdTrabajo		= entradaAgregarLog.p_IdTrabajo; 
				log.IdCabeceraLog	= inEv.p_IdEventoCapital; 
				log.IdTipoLog		= "INGRESO"; 
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
				log.IdTipoRegistro			= "INGRESAREVENTOCAPITAL"; 
				log.IdRegistro				= inEv.p_IdEventoCapital.toString(); 
				log.EstadoRegistro			= ""; 
				log.IdTipoOperacion			= ""; 
				log.Folio					= 0L; 
				log.Cantidad				= BigDecimal.ZERO; 
				log.PrecioTasa				= BigDecimal.ZERO; 
				log.Monto					= BigDecimal.ZERO; 
				log.IdMonedaOrigen			= inEv.p_IdMoneda; 
				log.IdMonedaLiquidacion		= inEv.p_IdMonedaLiquidacion; 
				log.TipoCambio				= ""; 
				log.Nemo					= inEv.p_Nemo; 
				log.IdProceso				= 0L; 
				log.EstadoProceso			= ""; 
				log.NombreProceso			= "cl.bcs.custodia.evc.CrearEventoCapital";
				logList.add(log);
				
				entradaAgregarLog.p_LogDetalle.add(agregarDetalleLog(log.IdCabeceraLog, "INGRESAREVENTOCAPITAL", "IdTipoEventoCapital", "", inEv.p_IdTipoEventoCapital));
				entradaAgregarLog.p_LogDetalle.add(agregarDetalleLog(log.IdCabeceraLog, "INGRESAREVENTOCAPITAL", "Factor", "", inEv.p_Factor.toString()));
				entradaAgregarLog.p_LogDetalle.add(agregarDetalleLog(log.IdCabeceraLog, "INGRESAREVENTOCAPITAL", "FechaLimite", "", inEv.p_FechaLimite));
				entradaAgregarLog.p_LogDetalle.add(agregarDetalleLog(log.IdCabeceraLog, "INGRESAREVENTOCAPITAL", "FechaPago", "", inEv.p_FechaPago));
				entradaAgregarLog.p_LogDetalle.add(agregarDetalleLog(log.IdCabeceraLog, "INGRESAREVENTOCAPITAL", "NumeroDividendo", "", inEv.p_NumeroDividendo.toString()));
				entradaAgregarLog.p_LogDetalle.add(agregarDetalleLog(log.IdCabeceraLog, "INGRESAREVENTOCAPITAL", "Descripcion", "", inEv.p_Descripcion));
				entradaAgregarLog.p_LogDetalle.add(agregarDetalleLog(log.IdCabeceraLog, "INGRESAREVENTOCAPITAL", "IdEmisor", "", inEv.p_IdEmisor));
				entradaAgregarLog.p_LogDetalle.add(agregarDetalleLog(log.IdCabeceraLog, "INGRESAREVENTOCAPITAL", "IdBoveda", "", inEv.p_IdBoveda));
				entradaAgregarLog.p_LogDetalle.add(agregarDetalleLog(log.IdCabeceraLog, "INGRESAREVENTOCAPITAL", "Secuencia", "", inEv.p_Secuencia.toString()));
				entradaAgregarLog.p_LogDetalle.add(agregarDetalleLog(log.IdCabeceraLog, "INGRESAREVENTOCAPITAL", "EsInscrito", "", inEv.p_EsInscrito));
				entradaAgregarLog.p_LogDetalle.add(agregarDetalleLog(log.IdCabeceraLog, "INGRESAREVENTOCAPITAL", "EsISFUT", "", inEv.p_EsISFUT));
				entradaAgregarLog.p_LogDetalle.add(agregarDetalleLog(log.IdCabeceraLog, "INGRESAREVENTOCAPITAL", "TasaEfectiva", "", inEv.p_TasaEfectiva.toString()));
				entradaAgregarLog.p_LogDetalle.add(agregarDetalleLog(log.IdCabeceraLog, "INGRESAREVENTOCAPITAL", "Paridad", "", inEv.p_Paridad.toString()));
				entradaAgregarLog.p_LogDetalle.add(agregarDetalleLog(log.IdCabeceraLog, "INGRESAREVENTOCAPITAL", "Valor", "", inEv.p_Valor.toString()));			
			}
			
			entradaAgregarLog.p_Log = logList;
			_sql.batch(conn, SQL_INSERT_EVENTO_CAPITAL, datosInsert, Evento.class);
		}
		//AGREGAR LOG
		{
			AgregarLog agregarLogService = new AgregarLog();
			agregarLogService.logica(conn, entradaAgregarLog);
		}
				
				
		logger.info("FIN::CUS.CrearEventoCapitalArchivo");
		return out;
	
	}
	
	public static InAgregarLog.DetalleLog agregarDetalleLog(Long idCabeceraLog, String subRegistro, String campoRegistro, String valorAntiguo, String valorNuevo) {
		InAgregarLog.DetalleLog DetalleLogEvento = new InAgregarLog.DetalleLog();
		DetalleLogEvento.IdCabeceraLog = idCabeceraLog; 
		DetalleLogEvento.IdTrabajo = 0L;
		DetalleLogEvento.SubRegistro = subRegistro; 
		DetalleLogEvento.CampoRegistro = campoRegistro; 
		DetalleLogEvento.ValorAntiguo = valorAntiguo; 
		DetalleLogEvento.ValorNuevo = valorNuevo;
		return DetalleLogEvento;
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
			conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties");
			//datos entrada
			
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":0,\"p_IdModulo\":\"CUS\",\"p_DataArchivo\":[{\"DEPOSITANTE\":12176,\"MONEDA\":61,\"GLOSAMONEDA\":\"UF        \",\"EMISORPAGADOR\":1012,\"EMISOR\":1012,\"INSTRUMENTO\":21,\"GLOSAINSTRUMENTO\":\"LH        \",\"EVENTO\":\"20250108\",\"NEMO\":\"EST4170107  \",\"BLOQUEO\":0,\"SORTEADO\":30,\"APAGAR\":4.227,\"APAGARPESOS\":162408,\"MENSAJEVALIDACION\":\"OK\"},{\"DEPOSITANTE\":12176,\"MONEDA\":61,\"GLOSAMONEDA\":\"UF        \",\"EMISORPAGADOR\":1053,\"EMISOR\":1053,\"INSTRUMENTO\":21,\"GLOSAINSTRUMENTO\":\"LH        \",\"EVENTO\":\"20250108\",\"NEMO\":\"RPLG5E0107  \",\"BLOQUEO\":0,\"SORTEADO\":10,\"APAGAR\":1.4396,\"APAGARPESOS\":55312,\"MENSAJEVALIDACION\":\"OK\"}]}";
			//String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":9,\"p_IdModulo\":\"CUS\",\"p_DataArchivo\":[{\"DEPOSITANTE\":51511515,\"MONEDA\":61,\"GLOSAMONEDA\":\"UF        \",\"EMISORPAGADOR\":1001,\"EMISOR\":1029,\"INSTRUMENTO\":21,\"GLOSAINSTRUMENTO\":\"LH        \",\"EVENTO\":\"20161012\",\"NEMO\":\"EST2140110  \",\"BLOQUEO\":0,\"SORTEADO\":320,\"APAGAR\":214.2721,\"APAGARPESOS\":4299625,\"MENSAJEVALIDACION\":\"OK\"},{\"DEPOSITANTE\":51511515,\"MONEDA\":61,\"GLOSAMONEDA\":\"UF        \",\"EMISORPAGADOR\":1001,\"EMISOR\":1033,\"INSTRUMENTO\":21,\"GLOSAINSTRUMENTO\":\"LH        \",\"EVENTO\":\"20161012\",\"NEMO\":\"BBV-B30108  \",\"BLOQUEO\":0,\"SORTEADO\":40,\"APAGAR\":25.584,\"APAGARPESOS\":513374,\"MENSAJEVALIDACION\":\"OK\"},{\"DEPOSITANTE\":51511515,\"MONEDA\":61,\"GLOSAMONEDA\":\"UF        \",\"EMISORPAGADOR\":1037,\"EMISOR\":1035,\"INSTRUMENTO\":21,\"GLOSAINSTRUMENTO\":\"LH        \",\"EVENTO\":\"20161012\",\"NEMO\":\"STGCE10197  \",\"BLOQUEO\":0,\"SORTEADO\":190,\"APAGAR\":106.97,\"APAGARPESOS\":2146481,\"MENSAJEVALIDACION\":\"OK\"},{\"DEPOSITANTE\":51511515,\"MONEDA\":61,\"GLOSAMONEDA\":\"UF        \",\"EMISORPAGADOR\":1037,\"EMISOR\":1035,\"INSTRUMENTO\":21,\"GLOSAINSTRUMENTO\":\"LH        \",\"EVENTO\":\"20161012\",\"NEMO\":\"STGEC10599  \",\"BLOQUEO\":0,\"SORTEADO\":200,\"APAGAR\":9.166,\"APAGARPESOS\":183927,\"MENSAJEVALIDACION\":\"OK\"},{\"DEPOSITANTE\":51511515,\"MONEDA\":61,\"GLOSAMONEDA\":\"UF        \",\"EMISORPAGADOR\":1037,\"EMISOR\":1037,\"INSTRUMENTO\":21,\"GLOSAINSTRUMENTO\":\"LH        \",\"EVENTO\":\"20161012\",\"NEMO\":\"STD62H0501  \",\"BLOQUEO\":0,\"SORTEADO\":130,\"APAGAR\":100.594,\"APAGARPESOS\":2018539,\"MENSAJEVALIDACION\":\"OK\"},{\"DEPOSITANTE\":51511515,\"MONEDA\":61,\"GLOSAMONEDA\":\"UF        \",\"EMISORPAGADOR\":1507,\"EMISOR\":1507,\"INSTRUMENTO\":21,\"GLOSAINSTRUMENTO\":\"LH        \",\"EVENTO\":\"20161012\",\"NEMO\":\"DES7121199  \",\"BLOQUEO\":0,\"SORTEADO\":10,\"APAGAR\":5.582,\"APAGARPESOS\":112010,\"MENSAJEVALIDACION\":\"OK\"},{\"DEPOSITANTE\":51511515,\"MONEDA\":61,\"GLOSAMONEDA\":\"UF        \",\"EMISORPAGADOR\":1507,\"EMISOR\":1507,\"INSTRUMENTO\":21,\"GLOSAINSTRUMENTO\":\"LH        \",\"EVENTO\":\"20161012\",\"NEMO\":\"DES7200191  \",\"BLOQUEO\":0,\"SORTEADO\":1000,\"APAGAR\":834.4,\"APAGARPESOS\":16743237,\"MENSAJEVALIDACION\":\"OK\"}]}";
			EventoCapital_IN objIn = gson.fromJson(json,EventoCapital_IN.class);
			//mientras pruebo hare el rollback
			CrearEventoCapital demo = new CrearEventoCapital();
			
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
