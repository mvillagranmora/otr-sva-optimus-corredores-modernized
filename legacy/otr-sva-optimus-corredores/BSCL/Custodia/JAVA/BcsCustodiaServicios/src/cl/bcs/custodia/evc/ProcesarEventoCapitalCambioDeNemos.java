package cl.bcs.custodia.evc;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.custodia.pojo.EventoCapital;
import cl.bcs.custodia.pojo.EventoCapitalCliente;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ProcesarEventoCapitalCambioDeNemos implements JavaCall{
	private static final Logger logger = LogManager.getLogger(ProcesarEventoCapitalCambioDeNemos.class);
	private final String SQL_SELECT_INSTRUMENTO = 	loadResourceToString("cl/bcs/custodia/sql/ProcesarEventoCapitalCambioDeNemos_SelectInstrumento.sql");
	private final String SQL_SELECT_CLIENTE_CARTERA = loadResourceToString("cl/bcs/custodia/sql/ProcesarEventoCapitalCambioDeNemos_SelectCartera.sql");
	private final String SQL_SELECT_CLIENTE_CARTERA_NEGATIVA = loadResourceToString("cl/bcs/custodia/sql/ProcesarEventoCapitalCambioDeNemos_SelectCarteraNegativa.sql");
	private final String SQL_SELECT_CLIENTE_SUBYACENTE = loadResourceToString("cl/bcs/custodia/sql/ProcesarEventoCapitalCambioDeNemos_SelectSubyacente.sql");
	private final String SQL_SELECT_CLIENTE_FACTURAS = loadResourceToString("cl/bcs/custodia/sql/ProcesarEventoCapitalCambioDeNemos_SelectFactura.sql");
	private final String SQL_SELECT_CLIENTE_GARANTIA = loadResourceToString("cl/bcs/custodia/sql/ProcesarEventoCapitalCambioDeNemos_SelectGarantia.sql");
	private final String SQL_SELECT_CLIENTE_OPERACION_TRANSITO = loadResourceToString("cl/bcs/custodia/sql/ProcesarEventoCapitalCambioDeNemos_SelectOperacionTransito.sql");
	private final String SQL_SELECT_CLIENTE_MOVIMIENTO_TRANSITO = loadResourceToString("cl/bcs/custodia/sql/ProcesarEventoCapitalCambioDeNemos_SelectMovimientoTransito.sql");
	private final String SQL_INSERT_EVENTO_CAPITAL_CLIENTE = loadResourceToString("cl/bcs/custodia/sql/ProcesarEventoCapitalCambioDeNemos_InsertEventoCapitalCliente.sql");
	private final String SQL_UPDATE_EVENTO_CAPITAL = loadResourceToString("cl/bcs/custodia/sql/ProcesarEventoCapitalCambioDeNemos_UpdateEventoCapital.sql");
	
	private final String SQL_UPDATE_CARTERA = loadResourceToString("cl/bcs/custodia/sql/CambioNemos_UpdateCartera.sql");
	private final String SQL_UPDATE_GARANTIA = loadResourceToString("cl/bcs/custodia/sql/CambioNemos_UpdateGarantia.sql");
	private final String SQL_UPDATE_MOVIMIENTO_COMPROBANTE_SIMULTANEA = loadResourceToString("cl/bcs/custodia/sql/CambioNemos_UpdateMovimientoComprobanteSimultanea.sql");
	private final String SQL_UPDATE_SUBYACENTE = loadResourceToString("cl/bcs/custodia/sql/CambioNemos_UpdateSubyacente.sql");
	
	public static class ProcesarEventoCapitalCambioDeNemosEntrada {
		public Long							p_IdEmpresa; 
		public Long							p_IdUsuario;
		public String						p_IdModulo;
		public String						p_Origen;
		public List<EventoCapital>			p_EventoCapital;
		
		public ProcesarEventoCapitalCambioDeNemosEntrada() {
			this.p_EventoCapital = new ArrayList<EventoCapital>();
		}
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	public static class ProcesarEventoCapitalCambioDeNemosEntrada2 {
		public Long							p_IdEmpresa; 
		public Long							p_IdUsuario;
		public String						p_IdModulo;
		public String						p_Origen;
		public List<InstrumentoNemo>		p_Instrumento;
		
		public ProcesarEventoCapitalCambioDeNemosEntrada2() {
			this.p_Instrumento = new ArrayList<InstrumentoNemo>();
		}
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	public static class ProcesarEventoCapitalCambioDeNemosSalida {
		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;
		public List<Long>			P_OUT_SECUENCIA;
		
		public ProcesarEventoCapitalCambioDeNemosSalida() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_SECUENCIA = new ArrayList<Long>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class ProcesarEventoCapitalCambioDeNemosSalida2 {
		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;
		public List<InstrumentoNemo>	P_OUT_INSTRUMENTO;
		
		public ProcesarEventoCapitalCambioDeNemosSalida2() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_INSTRUMENTO = new ArrayList<InstrumentoNemo>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class InstrumentoNemo {
		//Enviar nemotecnico solo en la entrada
		public Long IdInstrumento;
		public Long IdInstrumentoNuevo;
		public String Nemotecnico;
		public String NemotecnicoNuevo;
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	}
	
	public static class Instrumento {
		public Long IdInstrumento;
		public String Nemotecnico;
		public String RazonSocial;
		public Date FechaEmision;
		public Date FechaVencimiento;
		public String IdEmisor;
		public String IdTipoReajuste;
		public String IdMoneda;
		public String IdTipoInstrumento;
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	}
	
	public static class InstrumentoNuevo {
		public Long p_IdInstrumento;
		public Long p_IdInstrumentoNuevo;
		public String p_NemoNuevo;
		public String p_Emisor;
		public String p_ISIN;
		
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
	
	public ProcesarEventoCapitalCambioDeNemosSalida2 logicaCambioNemo(Connection con, ProcesarEventoCapitalCambioDeNemosEntrada2 in) throws Exception {
		logger.info("INICIO::CUS.ProcesarEventoCapitalCambioDeNemos");
		ProcesarEventoCapitalCambioDeNemosSalida2 out = new ProcesarEventoCapitalCambioDeNemosSalida2();
		HashMap<String,String> mapMsg = new HashMap<>();
		HashMap<String,Object> params = new HashMap<String,Object>();
		SQL sql = new SQL();
		Long Id = 0L;
		List<AgregarLog.InAgregarLog.Log> logList = new ArrayList<>();
		AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
		entradaAgregarLog.p_IdTrabajo = 1L;
		// obtener fecha del sistema
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");

		List<String> nemos = new ArrayList<>();
		List<String> nemosNuevos = new ArrayList<>();
		for (InstrumentoNemo nemo: in.p_Instrumento){
			nemos.add(nemo.Nemotecnico);
			nemos.add(nemo.NemotecnicoNuevo);
			nemosNuevos.add(nemo.NemotecnicoNuevo);
		}
		//----Buscamos los instrumentos nuevos
		params = new HashMap<String,Object>(); 
		params.put("p_Nemo", nemos);
		params.put("p_IdInstrumento", null); //solo se ocupa en logica
		List<Instrumento> instrumentos = sql.query(con, SQL_SELECT_INSTRUMENTO, params, Instrumento.class);
		HashMap<String, Object> instrumentosHash = Utils.List2Hash(instrumentos, "Nemotecnico");
		//******************************************************************************************************************
		//												VALIDACIONES INICIALES
		//******************************************************************************************************************
		{
			if (nemos.size() != instrumentos.size()){
				mapMsg.clear();
				out.P_OUT_MENSAJES.add(new MensajeSalida(Id, MensajeSalida.MENSAJE_ERROR, "UI_INSTRUMENTO_NO_EXISTE", mapMsg, con, in.p_IdEmpresa));
				Id++;
			}
			out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
			if (MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES) > 0){
				return out;
			}
		}
		//Logica
		{
			List<InstrumentoNuevo> updateInstrumentos = new ArrayList<>();
			for (InstrumentoNemo i: in.p_Instrumento){
				InstrumentoNuevo ins = new InstrumentoNuevo();
				Instrumento insViejo = (Instrumento)instrumentosHash.get(i.Nemotecnico);
				Instrumento insNuevo = (Instrumento)instrumentosHash.get(i.NemotecnicoNuevo);
				if (insNuevo != null){
					ins.p_IdInstrumento 		= insViejo.IdInstrumento;
					ins.p_IdInstrumentoNuevo 	= insNuevo.IdInstrumento;
					ins.p_NemoNuevo				= insNuevo.Nemotecnico;
					ins.p_Emisor				= insNuevo.IdEmisor;
					updateInstrumentos.add(ins);
					
					//parametros log
					AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
					log.IdTrabajo		= entradaAgregarLog.p_IdTrabajo; 
					log.IdCabeceraLog	= insViejo.IdInstrumento;
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
					log.IdTipoRegistro			= "PROCESAREVENTOCAPITAL"; 
					log.IdRegistro				= insViejo.IdInstrumento.toString(); 
					log.EstadoRegistro			= ""; 
					log.IdTipoOperacion			= ""; 
					log.Folio					= 0L; 
					log.Cantidad				= BigDecimal.ZERO; 
					log.PrecioTasa				= BigDecimal.ZERO; 
					log.Monto					= BigDecimal.ZERO; 
					log.IdMonedaOrigen			= ""; 
					log.IdMonedaLiquidacion		= ""; 
					log.TipoCambio				= ""; 
					log.Nemo					= insViejo.Nemotecnico; 
					log.IdProceso				= 0L; 
					log.EstadoProceso			= ""; 
					log.NombreProceso			= "cl.bcs.custodia.evc.ProcesarEventoCapitalCambioDeNemosCorteCupon";
					logList.add(log);
				}
			}
			sql.batch(con, SQL_UPDATE_MOVIMIENTO_COMPROBANTE_SIMULTANEA, updateInstrumentos, InstrumentoNuevo.class);
			sql.batch(con, SQL_UPDATE_CARTERA, updateInstrumentos, InstrumentoNuevo.class);
			sql.batch(con, SQL_UPDATE_SUBYACENTE, updateInstrumentos, InstrumentoNuevo.class);
			sql.batch(con, SQL_UPDATE_GARANTIA, updateInstrumentos, InstrumentoNuevo.class);
		}
		logger.info("FIN::CUS.ProcesarEventoCapitalCambioDeNemos");
		return out;
	}
	
	public ProcesarEventoCapitalCambioDeNemosSalida logica(Connection con, ProcesarEventoCapitalCambioDeNemosEntrada in) throws Exception {
		logger.info("INICIO::CUS.ProcesarEventoCapitalCambioDeNemos");
		ProcesarEventoCapitalCambioDeNemosSalida out = new ProcesarEventoCapitalCambioDeNemosSalida();
		HashMap<String,String> mapMsg = new HashMap<>();
		HashMap<String,Object> params = new HashMap<String,Object>();
		SQL sql = new SQL();
		Long Id = 0L;
		// obtener fecha del sistema
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		String fechaHoraSistema   = FechaDB.ObtenerFechaHoraSistemaUTC(con, in.p_IdEmpresa);
		// obtener valor del parámetro
		
		AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
		entradaAgregarLog.p_IdTrabajo = 1L;
		
		List<String> nemos = new ArrayList<>();
		List<Long> idsInstrumentos = new ArrayList<>();
		List<String> nemosNuevos = new ArrayList<>();
		List<Long> idsInstrumentosNuevos = new ArrayList<>();
		List<EventoCapitalCliente> eventoCapital = new ArrayList<>();
		for (EventoCapital ev: in.p_EventoCapital){
			nemos.add(ev.Nemo);
			if (ev.IdInstrumento != null && !ev.IdInstrumento.equals(0L)) idsInstrumentos.add(ev.IdInstrumento);
			nemosNuevos.add(ev.NemoNuevo);
			if (ev.IdInstrumentoNuevo != null && !ev.IdInstrumentoNuevo.equals(0L)) idsInstrumentosNuevos.add(ev.IdInstrumentoNuevo);
		}
		//----Buscamos los instrumentos nuevos
		params = new HashMap<String,Object>(); 
		params.put("p_Nemo", nemosNuevos);
		params.put("p_IdInstrumento", (idsInstrumentosNuevos.size() > 0) ? idsInstrumentosNuevos : null);
		List<Instrumento> instrumentosNuevos = sql.query(con, SQL_SELECT_INSTRUMENTO, params, Instrumento.class);
		//******************************************************************************************************************
		//												VALIDACIONES INICIALES
		//******************************************************************************************************************			
		{
			if (in.p_EventoCapital.size() == 0){
				mapMsg.clear();
				out.P_OUT_MENSAJES.add(new MensajeSalida(Id, MensajeSalida.MENSAJE_ERROR, "UI_EVENTO_CAPITAL_NO_EXISTE", mapMsg, con, in.p_IdEmpresa));
				Id++;
			}
			if (instrumentosNuevos.size() == 0 || instrumentosNuevos.size() != in.p_EventoCapital.size()){
				mapMsg.clear();
				out.P_OUT_MENSAJES.add(new MensajeSalida(Id, MensajeSalida.MENSAJE_ERROR, "UI_INSTRUMENTO_NO_EXISTE", mapMsg, con, in.p_IdEmpresa));
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
			
			//--------------------------------------------------------------------------------------------------------
			//---												CARTERAS
			//--- Inserta registros en tabla "BSCL.Custodia.BD::CUS.EventoCapitalCliente" a partir de resultado de 
			//--- "BSCL.Custodia.BD::CUS.Cartera"
			//--------------------------------------------------------------------------------------------------------
			params = new HashMap<String,Object>(); 
			params.put("p_IdEmpresa", in.p_IdEmpresa);
			params.put("p_Nemo", nemos);
			params.put("p_IdInstrumento", (idsInstrumentos.size() > 0) ? idsInstrumentos : null);
			List<EventoCapitalCliente> eventoCapitalCartera = sql.query(con, SQL_SELECT_CLIENTE_CARTERA, params, EventoCapitalCliente.class);
			//--------------------------------------------------------------------------------------------------------
			//---												CARTERAS NEGATIVAS
			//--- Inserta registros en tabla "BSCL.Custodia.BD::CUS.EventoCapitalCliente" a partir de resultado de 
			//--- "BSCL.Custodia.BD::CUS.CarteraNegativa"
			//--------------------------------------------------------------------------------------------------------
			List<EventoCapitalCliente> eventoCapitalCarteraNegativa = sql.query(con, SQL_SELECT_CLIENTE_CARTERA_NEGATIVA, params, EventoCapitalCliente.class);
			//--------------------------------------------------------------------------------------------------------
			//---											SUBYACENTE
			//--- Inserta registros en tabla "BSCL.Custodia.BD::CUS.EventoCapitalCliente" a partir de resultado de 
			//--- "BSCL.Custodia.BD::CUS.Subyacente"
			//--------------------------------------------------------------------------------------------------------
			List<EventoCapitalCliente> eventoCapitalSubyacente = sql.query(con, SQL_SELECT_CLIENTE_SUBYACENTE, params, EventoCapitalCliente.class);
			//--------------------------------------------------------------------------------------------------------
			//---											FACTURAS
			//--- Inserta registros en tabla "BSCL.Custodia.BD::CUS.EventoCapitalCliente" a partir de resultado de 
			//--- "BSCL.Facturacion.BD::FAC.MovimientoComprobante"
			//--------------------------------------------------------------------------------------------------------
			List<EventoCapitalCliente> eventoCapitalFactura = sql.query(con, SQL_SELECT_CLIENTE_FACTURAS, params, EventoCapitalCliente.class);
			//--------------------------------------------------------------------------------------------------------
			//---											GARANTIAS
			//--- Inserta registros en tabla "BSCL.Custodia.BD::CUS.EventoCapitalCliente" a partir de resultado de 
			//---	"BSCL.Custodia.BD::GTA.Garantia"
			//--------------------------------------------------------------------------------------------------------
			List<EventoCapitalCliente> eventoCapitalGarantia = sql.query(con, SQL_SELECT_CLIENTE_GARANTIA, params, EventoCapitalCliente.class);
			//--------------------------------------------------------------------------------------------------------
			//--- 											OPERACIONES EN TRÁNSITO 
			//--- Inserta registros en tabla "BSCL.Custodia.BD::CUS.EventoCapitalCliente"  de operaciones
			//--- que se encuentran en tránsito, por ejemplo, ventas lo liquidadas.
			//--------------------------------------------------------------------------------------------------------
			List<EventoCapitalCliente> eventoCapitalOperacionTransito = sql.query(con, SQL_SELECT_CLIENTE_OPERACION_TRANSITO, params, EventoCapitalCliente.class);
			//--------------------------------------------------------------------------------------------------------
			//--- 											MOVIMIENTO EN TRÁNSITO 
			//--- Inserta registros en tabla "BSCL.Custodia.BD::CUS.EventoCapitalCliente"  de movimientos
			//--- que se encuentran en tránsito, por ejemplo, Nominales.
			//--------------------------------------------------------------------------------------------------------
			List<EventoCapitalCliente> eventoCapitalMovimientoTransito = sql.query(con, SQL_SELECT_CLIENTE_MOVIMIENTO_TRANSITO, params, EventoCapitalCliente.class);
			//consolido los eventos
			eventoCapital.addAll(eventoCapitalCartera);
			eventoCapital.addAll(eventoCapitalCarteraNegativa);
			eventoCapital.addAll(eventoCapitalSubyacente);
			eventoCapital.addAll(eventoCapitalFactura);
			eventoCapital.addAll(eventoCapitalGarantia);
			eventoCapital.addAll(eventoCapitalOperacionTransito);
			eventoCapital.addAll(eventoCapitalMovimientoTransito);
			//------------Inserto Evento capital cliente
			List<Long> secuenciaEventoCapitalCliente= Utils.getSecuencia(con, "BSCL.Custodia.BD.Secuencias::EventoCapitalCliente", new Long(eventoCapital.size()));

			List<AgregarLog.InAgregarLog.Log> logList = new ArrayList<>();
			for (EventoCapital ev: in.p_EventoCapital){
				for (EventoCapitalCliente evCap: eventoCapital){
					if (ev.Nemo.equals(evCap.Nemo)){
						evCap.IdEventoCapitalCliente 	= secuenciaEventoCapitalCliente.get(eventoCapital.indexOf(evCap));
						evCap.IdEventoCapital			= ev.IdEventoCapital;
						evCap.IdTipoEventoCapital		= ev.IdTipoEventoCapital;
						evCap.Secuencia					= ev.Secuencia;
						evCap.NumeroDividendo			= ev.NumeroDividendo;
						evCap.IdClasificacionTipoInstrumento = ev.IdClasificacionTipoInstrumento;
						evCap.IdTipoInstrumento		= ev.IdTipoInstrumento;
						evCap.IdInstrumento			= ev.IdInstrumento;
						evCap.Nemo					= ev.Nemo;
						evCap.IdInstrumentoNuevo	= ev.IdInstrumentoNuevo;
						evCap.NemoNuevo				= ev.NemoNuevo;
						evCap.FechaCierre			= ev.FechaLimite;
						evCap.FechaPago				= ev.FechaPago;
						evCap.CantidadDerecho		= evCap.CantidadDerecho;
						evCap.IdMoneda				= ev.IdMoneda;
						evCap.FechaHoraIngresoRegistro = Fecha.toDateTime(fechaHoraSistema);
						evCap.FechaHoraUltimaActualizacion = Fecha.toDateTime(fechaHoraSistema);
						evCap.IdUsuario				= ev.IdUsuario;
						evCap.IdEstado				= ev.IdEstado;
						evCap.IdBoveda				= evCap.IdBoveda == null? "": evCap.IdBoveda;
						evCap.Origen				= in.p_Origen;
					}
					mapMsg.clear();
				    mapMsg.put("p_NumeroDividendo", ev.NumeroDividendo.toString());
					out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_OK,"UI_EVENTO_CAPITAL_CREADO",mapMsg,con,in.p_IdEmpresa));
					out.P_OUT_SECUENCIA.add(ev.IdEventoCapital);
					Id++;
				}
				//parametros log
				AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
				log.IdTrabajo		= entradaAgregarLog.p_IdTrabajo; 
				log.IdCabeceraLog	= ev.IdEventoCapital; 
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
				log.IdTipoRegistro			= "PROCESAREVENTOCAPITAL"; 
				log.IdRegistro				= ev.IdEventoCapital.toString(); 
				log.EstadoRegistro			= ""; 
				log.IdTipoOperacion			= ""; 
				log.Folio					= 0L; 
				log.Cantidad				= BigDecimal.ZERO; 
				log.PrecioTasa				= BigDecimal.ZERO; 
				log.Monto					= BigDecimal.ZERO; 
				log.IdMonedaOrigen			= ""; 
				log.IdMonedaLiquidacion		= ""; 
				log.TipoCambio				= ""; 
				log.Nemo					= ev.Nemo; 
				log.IdProceso				= 0L; 
				log.EstadoProceso			= ""; 
				log.NombreProceso			= "cl.bcs.custodia.evc.ProcesarEventoCapitalCambioDeNemos";
				logList.add(log);
			}
			List<InstrumentoNuevo> updateInstrumentos = new ArrayList<>();
			List<EventoCapitalUpd> updateEventos = new ArrayList<>();
			for (EventoCapital ev: in.p_EventoCapital){
				InstrumentoNuevo ins = new InstrumentoNuevo();
				ins.p_IdInstrumento 		= ev.IdInstrumento;
				ins.p_IdInstrumentoNuevo 	= ev.IdInstrumentoNuevo;
				ins.p_NemoNuevo				= ev.NemoNuevo;
				ins.p_Emisor				= ev.IdEmisorNuevo;
				updateInstrumentos.add(ins);
				
				EventoCapitalUpd evCapUpd = new EventoCapitalUpd();
				evCapUpd.p_IdEmpresa = in.p_IdEmpresa;
				evCapUpd.p_IdEventoCapital = ev.IdEventoCapital;
				evCapUpd.p_IdTipoEventoCapital = ev.IdTipoEventoCapital;
				evCapUpd.p_Secuencia = ev.Secuencia;
				evCapUpd.p_IdEstado = "PRO";
				updateEventos.add(evCapUpd);
				
			}
			entradaAgregarLog.p_Log = logList;
			sql.batch(con, SQL_UPDATE_EVENTO_CAPITAL, updateEventos, EventoCapitalUpd.class);
			sql.batch(con, SQL_INSERT_EVENTO_CAPITAL_CLIENTE, eventoCapital, EventoCapitalCliente.class);
			
			//AGREGAR LOG
			{
				AgregarLog agregarLogService = new AgregarLog();
				agregarLogService.logica(con, entradaAgregarLog);
			}

		}
		
		logger.info("FIN::CUS.ProcesarEventoCapitalCambioDeNemos");
		return out;
		
		
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		Gson gson = Utils.getGsonBuilder();
		ProcesarEventoCapitalCambioDeNemosEntrada jin = gson.fromJson(in.toString(), ProcesarEventoCapitalCambioDeNemosEntrada.class);
		ProcesarEventoCapitalCambioDeNemosSalida out = logica(conn, jin);
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
			//datos entrada
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"CUS\",\"p_EventoCapital\":[{\"IdEventoCapital\":366799,\"IdEmpresa\":1,\"IdTipoEventoCapital\":\"CN\",\"Secuencia\":9988,\"Descripcion\":\"TEST 1: CUPRUM X ZOFRI\",\"EsInscrito\":\"S\",\"IdClasificacionTipoInstrumento\":\"RV\",\"IdTipoInstrumento\":\"ACN\",\"IdInstrumento\":592,\"Nemo\":\"CUPRUM\",\"Factor\":0E-8,\"Valor\":0E-8,\"IdMoneda\":\"CLP\",\"IdInstrumentoNuevo\":2220,\"NemoNuevo\":\"ZOFRI\",\"FactorNuevo\":1.00000000,\"FechaActualizacion\":\"2020-08-21 00:00:00.000\",\"FechaLimite\":\"2020-08-21 00:00:00.000\",\"FechaPago\":\"2020-08-27 00:00:00.000\",\"NumeroDividendo\":1,\"IdTipoDividendo\":\"P\",\"PrecioAnterior\":0E-8,\"FechaHoraIngresoRegistro\":\"2020-08-21 00:00:00.000\",\"FechaHoraUltimaActualizacion\":\"2020-08-27 00:00:00.000\",\"TipoIngreso\":\"C\",\"IdUsuario\":21,\"IdEstado\":\"VIG\",\"CuentaDepositanteSorteoLetras\":0,\"MonedaSorteoLetras\":0,\"GlosaMonedaSorteoLetras\":\"\",\"EmisorPagadorSorteoLetras\":0,\"EmisorSorteoLetras\":0,\"InstrumentoSorteoLetras\":0,\"GlosaInstrumentoSorteoLetras\":\"\",\"BloqueoSorteoLetras\":0,\"SorteadoSorteoLetras\":0.0000,\"APagarSorteoLetras\":0.0000,\"APagarPesosSorteoLetras\":0,\"Origen\":\"MAN\",\"IdEmisor\":\"CUPRUM\",\"IdMonedaLiquidacion\":\"CLP\",\"Paridad\":1.00000000,\"IdEmisorNuevo\":\"ZOFRI\",\"EstadoNotificacion\":\"N\",\"IdInstrumentoAnterior\":0,\"Indice\":0,\"Impuesto\":0E-8,\"CantidadCuotas\":0E-8,\"Monto\":0E-8}]}";
			
			ProcesarEventoCapitalCambioDeNemosEntrada objIn = gson.fromJson(json,ProcesarEventoCapitalCambioDeNemosEntrada.class);
			//mientras pruebo hare el rollback
			ProcesarEventoCapitalCambioDeNemos demo = new ProcesarEventoCapitalCambioDeNemos();
			
			ProcesarEventoCapitalCambioDeNemosSalida ObjOut=demo.logica(conn, objIn);
				
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
