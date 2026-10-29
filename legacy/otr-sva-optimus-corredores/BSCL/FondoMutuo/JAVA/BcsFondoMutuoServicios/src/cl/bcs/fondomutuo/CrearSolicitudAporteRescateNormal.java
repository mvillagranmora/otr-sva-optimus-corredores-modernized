package cl.bcs.fondomutuo;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.fondomutuo.CalcularComisiones.EntradaCalcularComisiones;
import cl.bcs.fondomutuo.CalcularComisiones.ResultadoCalcularComision;
import cl.bcs.fondomutuo.CalcularComisiones.SalidaCalcularComisiones;
import cl.bcs.fondomutuo.CalcularFechaSolicitudAporteRescate.EntradaCalcularFechaSolicitudAporteRescate;
import cl.bcs.fondomutuo.CalcularFechaSolicitudAporteRescate.ResultadoFecha;
import cl.bcs.fondomutuo.CalcularFechaSolicitudAporteRescate.SalidaCalcularFechaSolicitudAporteRescate;
import cl.bcs.fondomutuo.CalcularMontoNumeroCuota.EntradaCalcularMontoNumeroCuota;
import cl.bcs.fondomutuo.CalcularMontoNumeroCuota.SalidaCalcularMontoNumeroCuota;
import cl.bcs.fondomutuo.ConsultarFlujoCustodiaFondoMutuo.EntradaConsultarFlujoCustodiaFondoMutuo;
import cl.bcs.fondomutuo.ConsultarFlujoCustodiaFondoMutuo.SalidaConsultarFlujoCustodiaFondoMutuo;
import cl.bcs.fondomutuo.CrearSolicitudAGF.EntradaCrearSolicitudAGF;
import cl.bcs.fondomutuo.CrearSolicitudAGF.SalidaCrearSolicitudAGF;
import cl.bcs.fondomutuo.ObtenerPerfilFondoMutuo.EntradaObtenerPerfilFondoMutuo;
import cl.bcs.fondomutuo.ObtenerPerfilFondoMutuo.SalidaObtenerPerfilFondoMutuo;
import cl.bcs.fondomutuo.ProcesarIngresoEgresoTesoreria.EntradaProcesarIngresoEgresoTesoreria;
import cl.bcs.fondomutuo.ProcesarIngresoEgresoTesoreria.SalidaProcesarIngresoEgresoTesoreria;
import cl.bcs.fondomutuo.ServicioCumpleRequisitosCrearSolicitudAporteRescate.EntradaServicioCumpleRequisitosCrearSolicitudAporteRescate;
import cl.bcs.fondomutuo.ServicioCumpleRequisitosCrearSolicitudAporteRescate.SalidaServicioCumpleRequisitosCrearSolicitudAporteRescate;
import cl.bcs.fondomutuo.ValidaMontoInversion.EntradaValidaMontoInversion;
import cl.bcs.fondomutuo.ValidaMontoInversion.SalidaValidaMontoInversion;
import cl.bcs.fondomutuo.ValidaPatrimonioEmisor.EntradaValidaPatrimonioEmisor;
import cl.bcs.fondomutuo.ValidaPatrimonioEmisor.SalidaValidaPatrimonioEmisor;
import cl.bcs.fondomutuo.ProcesarIngresoOperacionSpot.EntradaProcesarIngresoOperacionSpot;
import cl.bcs.fondomutuo.ProcesarIngresoOperacionSpot.SalidaProcesarIngresoOperacionSpot;
import cl.bcs.fondomutuo.pojo.ClientePortafolioSolicitudes;
import cl.bcs.fondomutuo.pojo.ConfiguracionPropositoPortafolio;
import cl.bcs.fondomutuo.pojo.ConstantesFondoMutuo;
import cl.bcs.fondomutuo.pojo.IndicadoresPortafolio;
import cl.bcs.fondomutuo.pojo.Moneda;
import cl.bcs.fondomutuo.pojo.PortafolioAPV;
import cl.bcs.fondomutuo.pojo.RegimenSerieDeFondo;
import cl.bcs.fondomutuo.pojo.ResultadoSolicitud;
import cl.bcs.fondomutuo.pojo.SerieFondoSolicitudes;
import cl.bcs.fondomutuo.pojo.SolicitudAporteRescate;
import cl.bcs.fondomutuo.pojo.TipoMovimientoFondoMutuo;
import cl.bcs.fondomutuo.utils.FondoMutuoUtils;
import cl.bcs.fondomutuo.pojo.FormaPagoSpot;
import cl.bcs.plataforma.bat.InscribirProcesoBatch;
import cl.bcs.plataforma.bat.InscribirProcesoBatch.InTipoPrograma;
import cl.bcs.plataforma.bat.InscribirProcesoBatch.InscribirProcesoBatchEntrada;
import cl.bcs.plataforma.bat.InscribirProcesoBatch.Parametro;
import cl.bcs.plataforma.mer.CalcularParidad;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadSalida;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoSalida;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.NotificarEstadoOrdenesOWP;
import cl.bcs.plataforma.pla.NotificarEstadoOrdenesOWP.NotificarEstadoOrdenes_Entrada;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.SaldoFlujoCaja;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.MonedaFormato;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.tesoreria.CrearIngresoEgreso.IngresoEgresoGenerado_Salida;
import cl.bcs.tesoreria.CrearIngresoEgreso.ResultadoProceso_Salida;
import cl.bcs.mesa.SPT.CrearOperacionSpot.OperacionObjOut;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CrearSolicitudAporteRescateNormal implements JavaCall {
	
	//private final Log log = new Log("FM.CrearSolicitudAporteRescateNormal");
	static final Logger logger = LogManager.getLogger(CrearSolicitudAporteRescateNormal.class);
	private final String SQL_SELECT_SERIE_FONDO = loadResourceToString("cl/bcs/fondomutuo/sql/ComunesSolicitudes_SelectSerieFondo.sql");
	private final String SQL_SELECT_PORTAFOLIO = loadResourceToString("cl/bcs/fondomutuo/sql/ComunesSolicitudes_SelectPortafolio.sql");
	private final String SQL_SELECT_PORTAFOLIO_APV = loadResourceToString("cl/bcs/fondomutuo/sql/ComunesSolicitudes_SelectPortafolioAPV.sql");
	private final String SQL_SELECT_TIPO_MOVIMIENTO = loadResourceToString("cl/bcs/fondomutuo/sql/Comunes_SelectTipoMovimientoFondoMutuo.sql");
	private final String SQL_SELECT_REGIMEN = loadResourceToString("cl/bcs/fondomutuo/sql/ComunesSolicitudes_SelectRegimenSerieDeFondo.sql");
	private final String SQL_SELECT_INDICADORES_PORTAFOLIO = loadResourceToString("cl/bcs/fondomutuo/sql/ComunesSolicitudes_SelectIndicadoresPortafolio.sql");
	private final String SQL_SELECT_MONEDA = loadResourceToString("cl/bcs/fondomutuo/sql/ComunesSolicitudes_SelectMoneda.sql");
	private final String SQL_INSERT_SOLICITUD = loadResourceToString("cl/bcs/fondomutuo/sql/ComunesSolicitudes_InsertSolicitud.sql");
	private final String SQL_SELECT_SOLICITUDES = loadResourceToString("cl/bcs/fondomutuo/sql/CertificarSolicitudAporteRescate_SelectSolicitudes.sql");
	private final String SQL_SELECT_CONFIGURACION_REGIMEN_PORTAFOLIO = loadResourceToString("cl/bcs/fondomutuo/sql/ComunesSolicitudes_SelectConfiguracionPropositoPortafolio.sql");
	private final String SQL_SELECT_COUNT_CUSTODIA = loadResourceToString("cl/bcs/fondomutuo/sql/CrearSolicitudAporteRescateNormal_SelectCountCustodia.sql");
	private final String SQL_SELECT_CONSULTA_TRAPASO_APV = loadResourceToString("cl/bcs/fondomutuo/sql/CrearSolicitudAporteRescateNormal_SelectConsultaTraspasoAPV.sql");
	private final String SQL_CALL_CREAR_BLOQUEO_SOLICITUD = loadResourceToString("cl/bcs/fondomutuo/sql/CrearSolicitudAporteRescateNormal_CallCrearBloqueoSolicitudAporteRescate.sql");
	private final String SQL_SELECT_BLOQUEO_SOLICITUD = loadResourceToString("cl/bcs/fondomutuo/sql/CrearSolicitudAporteRescateNormal_SelectBloqueoSolicitud.sql");
	private final String SQL_CALL_ELIMINAR_BLOQUEO_SOLICITUD = loadResourceToString("cl/bcs/fondomutuo/sql/CrearSolicitudAporteRescateNormal_CallEliminarBloqueoSolicitudAporteRescate.sql");
	private final String SQL_CREATE_TABLE_BLOQUEO_SOLICITUD = loadResourceToString("cl/bcs/fondomutuo/sql/CrearSolicitudAporteRescateNormal_CreateTableBloqueoSolicitudAporteRescate.sql");
	private final String SQL_INSERT_TABLE_BLOQUEO_SOLICITUD = loadResourceToString("cl/bcs/fondomutuo/sql/CrearSolicitudAporteRescateNormal_InsertTableBloqueoSolicitudAporteRescate.sql");
	private final String SQL_DROP_TABLE_BLOQUEO_SOLICITUD = loadResourceToString("cl/bcs/fondomutuo/sql/CrearSolicitudAporteRescateNormal_DropTableBloqueoSolicitudAporteRescate.sql");
	private final String SQL_SELECT_PARAMETRO_OPERA_CON_SALDO_NEGATIVO = loadResourceToString("cl/bcs/fondomutuo/sql/CrearSolicitudAporteRescateNormal_SelectParametroOperaConSaldoNegativo.sql");
	
	private final ConstantesFondoMutuo _c = new ConstantesFondoMutuo();
	
	public static class EntradaCrearSolicitudAporteRescateNormal{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_SimularOperacion;
		public String p_UsaReglasNegocio;
		public String p_IdTipoCanalIngreso;
		public Long p_NumeroDividendo;
		public List<InSolicitudesAporteRescate> p_SolicitudesAporteRescate;
		public List<InFormaPagoSpot> p_FormaPagoSpot;
		public String p_IdOrigenProceso;  // REC: Recaudacion    PAC: Pago automatico  BONIF: Proceso Gestion Bonificacion  CAR: Carga Masiva Fondos Mutuos
		public String p_PermiteOperarProspectos;
		
		public EntradaCrearSolicitudAporteRescateNormal() {
			p_IdTipoCanalIngreso = "OPT";
			p_SolicitudesAporteRescate = new ArrayList<InSolicitudesAporteRescate>();
			p_FormaPagoSpot = new ArrayList<InFormaPagoSpot>();
			p_NumeroDividendo = 0L;
			p_IdOrigenProceso = "OPT";
			p_PermiteOperarProspectos = "N";
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class InSolicitudesAporteRescate{
    	public Long IdInterno;
    	public Long IdCliente;
    	public Long IdPortafolio;
    	public String TipoSolicitud;
    	public String IdSerieFondo;
    	public String CalculoPorMontoCuota;
    	public String EsPorMontoLiquido;
    	public BigDecimal Monto;
    	public BigDecimal NumeroCuotas;
    	public String IdMoneda;
    	public String IdMedioDePago;
    	public Long IdPreferenciaPagoPortafolio;
    	public String Visado;
    	public String SolicitudBatch;
    	public String OrigenOperacion;
    	public String FechaOperacion;
    	public String IdRegimenTributario;
    	public String Glosa;
    	public String IndicadorMLT;
    	public String IdSerieFondoDestino;
    	public String IdEmisorDestino;
    	public Long IdAutorizadoOrigen;
    	public BigDecimal Porcentaje;
    	public Long FolioContrato;
    	public String IdMonedaPago;
    	public BigDecimal MontoPatrimonio;
    	
    	public String IngresoAutomatico;
    	public String ForzarSpot; //Parametro para forzar spot independiente si tiene saldo en dolar, para distribucion y CSC
    	public String TipoContratoOperacion;
    	public String FolioContratoOperacion;
    	public String EsTraspaso;
    	public String Canal;
    	public Long IdSolicitudAporteRescateRelacionado;//Para solicitudes BF
    	public Date FechaValorCuota;
    	public String IgnorarMontoMinimo;	// S / N
    	public String SimulaCambioPortafolioEjecutadoOk;
    	public String IngresoDesde;
    	public InSolicitudesAporteRescate(){
    		this.IdMonedaPago = "";
    		this.IdEmisorDestino = "";
    		this.MontoPatrimonio = BigDecimal.ZERO;
    		this.IngresoAutomatico = "N";
    		this.ForzarSpot = "N";
    		this.TipoContratoOperacion = "";
    		this.FolioContratoOperacion = "";
    		this.EsTraspaso = "N";
    		this.Canal = "";
    		this.IdSolicitudAporteRescateRelacionado = 0L;
    		this.IgnorarMontoMinimo = "N";
    		this.SimulaCambioPortafolioEjecutadoOk = "N";
    		this.IngresoDesde = "";
    	}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		public Long IdOrigenRecaCliente = 0L;
	}
	
	public static class InFormaPagoSpot{
		
    	public Long IdInternoPagoOperacion;
    	public Long IdComprobantePagoOperacion;
    	public String IdTipoMovimiento;
    	public String IdFormaDePago;
    	public String IdMoneda;
    	public String IdMonedaLiquidacion;
    	public Long IdOperacion;
    	public String RecibePagaPortafolio;
    	public BigDecimal MontoFormaDePago;
    	public String AfectarTesoreria;
    
	
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaTraspasoTotal{
		
    	public String Id;
    	public Long Cantidad;
	
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class TraspasoAPV{
		
    	public Long TraspasoAPV;
    	public Long IdTraspaso;
    
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaCrearSolicitudAporteRescateNormal{
		public Long P_OUT_ERRORESCONTADOR;
		public List <MensajeSalida> P_OUT_MENSAJES;
		public List <ResultadoSolicitud> P_OUT_RESULTADO;
		
		public SalidaCrearSolicitudAporteRescateNormal(){
			this.P_OUT_ERRORESCONTADOR=0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_RESULTADO = new ArrayList<ResultadoSolicitud>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Portafolio{
		public Long IdPortafolio;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class BloqueoSolicitudAporteRescate{
		public String 		IdBloqueoSolicitudAporteRescate;
		public Long 		IdPortafolio;
		public String   	IdSerieFondo;
		public String 		IdRegimenTributario;
		public String   	TipoSolicitud;
		public BigDecimal	Monto;
		public BigDecimal	NumeroCuotas;
		public String 		FechaSistema;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ParametroOperaCSN {
		public String ValorIndicador = "N";
		public Long IdEntidad = 0L;
		public String CodigoTipoIndicador = "";
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		EntradaCrearSolicitudAporteRescateNormal jin = gson.fromJson(in.toString(), EntradaCrearSolicitudAporteRescateNormal.class);
		SalidaCrearSolicitudAporteRescateNormal out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	@Trace
	public SalidaCrearSolicitudAporteRescateNormal logica(Connection con, EntradaCrearSolicitudAporteRescateNormal entrada) throws Exception{
		logger.info("INICIO::CrearSolicitudAporteRescateNormal");
		SalidaCrearSolicitudAporteRescateNormal out = new SalidaCrearSolicitudAporteRescateNormal();
		SQL sql=new SQL();
		
		RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
		RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(con, entrada.p_IdEmpresa, entrada.p_IdUsuario, entrada.p_IdModulo);
		if (MensajeSalida.getNumeroErrores(salidaBasicos.p_out_Mensajes) > 0) {
			out.P_OUT_ERRORESCONTADOR = 1L;
			out.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
			return out;
		}
		
		if(entrada.p_SolicitudesAporteRescate.size() == 0){
			out.P_OUT_ERRORESCONTADOR = 1L;
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"NO_SE_INGRESARON_SOLICITUDES_APORTE_RESCATE",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
			return out;
		}
		
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		List<BloqueoSolicitudAporteRescate> solicitudesBloqueadas = new ArrayList<BloqueoSolicitudAporteRescate>();
		
		if(!_c.SI.equalsIgnoreCase(entrada.p_SimularOperacion) && aplicaBloqueoSolicitud(entrada)) {
			//Bloquear solicitudes duplicadas
			for (InSolicitudesAporteRescate mov: entrada.p_SolicitudesAporteRescate) {
				BloqueoSolicitudAporteRescate bloqueo = new BloqueoSolicitudAporteRescate();
				bloqueo.IdBloqueoSolicitudAporteRescate = String.valueOf(mov.IdPortafolio) + '_' + mov.IdSerieFondo + '_' + mov.IdRegimenTributario + '_' + mov.TipoSolicitud + '_' +  String.valueOf(mov.Monto) + '_' + String.valueOf(mov.NumeroCuotas) + '_' + fechaSistema;
				bloqueo.IdPortafolio = mov.IdPortafolio;
				bloqueo.IdSerieFondo = mov.IdSerieFondo;
				bloqueo.IdRegimenTributario = mov.IdRegimenTributario;
				bloqueo.TipoSolicitud = mov.TipoSolicitud;
				bloqueo.Monto = mov.Monto;
				bloqueo.NumeroCuotas = mov.NumeroCuotas;
				bloqueo.FechaSistema = fechaSistema;
				solicitudesBloqueadas.add(bloqueo);
			}

			try {
				List<String> idsBloqueoSolicitudAporteRescate = solicitudesBloqueadas.stream().map(sol -> sol.IdBloqueoSolicitudAporteRescate).collect(Collectors.toList());
				HashMap<String,Object> paramsQuery = new HashMap<String,Object>();
				paramsQuery.put("p_IdBloqueoSolicitudAporteRescate", idsBloqueoSolicitudAporteRescate);
				
				logger.debug("INICIO::SQL_SELECT_BLOQUEO_SOLICITUD");
				List<BloqueoSolicitudAporteRescate> solicitudesBloqueadasBD = sql.query(con, SQL_SELECT_BLOQUEO_SOLICITUD, paramsQuery, BloqueoSolicitudAporteRescate.class);
				logger.debug("FIN::SQL_SELECT_BLOQUEO_SOLICITUD");
				
				List<String> idsSolicitudesBloqueadasBD = solicitudesBloqueadasBD.stream().map(sol -> sol.IdBloqueoSolicitudAporteRescate).collect(Collectors.toList());
				
				if(solicitudesBloqueadasBD.size() > 0 && solicitudesBloqueadas.size() == solicitudesBloqueadasBD.size() && idsBloqueoSolicitudAporteRescate.equals(idsSolicitudesBloqueadasBD)) {
					out.P_OUT_ERRORESCONTADOR++;
					out.P_OUT_MENSAJES.add(new MensajeSalida(1L, MensajeSalida.MENSAJE_ERROR, "UI_ERROR_BLOQUEO_SOLICITUD", new HashMap<String,String>(), con, entrada.p_IdEmpresa));
					return out;
				} else {
					HashMap<String,Object> paramsSp = new HashMap<String,Object>();
					paramsSp.put("p_BloqueoSolicitudAporteRescate", solicitudesBloqueadas);
					SQL.exec(con, SQL_CREATE_TABLE_BLOQUEO_SOLICITUD);
					sql.batch(con, SQL_INSERT_TABLE_BLOQUEO_SOLICITUD, solicitudesBloqueadas, BloqueoSolicitudAporteRescate.class);
					logger.debug("INICIO::SQL_CALL_CREAR_BLOQUEO_SOLICITUD");
					sql.call(con, SQL_CALL_CREAR_BLOQUEO_SOLICITUD, paramsSp, new HashMap<String,Class>());
					logger.debug("FIN::SQL_CALL_CREAR_BLOQUEO_SOLICITUD"); 
				}
			} catch(SQLException sqle) {
				logger.error(sqle);
				out.P_OUT_ERRORESCONTADOR++;
				out.P_OUT_MENSAJES.add(new MensajeSalida(1L, MensajeSalida.MENSAJE_ERROR, "UI_ERROR_BLOQUEO_SOLICITUD", new HashMap<String,String>(), con, entrada.p_IdEmpresa));
				return out;
			}
		}
		
		try {
			ArrayList<Long> correlativos = new ArrayList<Long>();
			for (InSolicitudesAporteRescate mov: entrada.p_SolicitudesAporteRescate) {	
				if(mov.IdInterno.longValue() == 0L){
					out.P_OUT_ERRORESCONTADOR = 1L;
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"ID_INTERNO_NO_CERO",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
					eliminarBloqueoSolicitudAporteRescate(con, entrada.p_SimularOperacion, entrada.p_IdEmpresa, solicitudesBloqueadas, out.P_OUT_MENSAJES, out.P_OUT_ERRORESCONTADOR, sql);
					return out;
				}
				if (correlativos.contains(mov.IdInterno)) {
					out.P_OUT_ERRORESCONTADOR = 1L;
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"CORRELATIVO_INCORRECTO",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
					eliminarBloqueoSolicitudAporteRescate(con, entrada.p_SimularOperacion, entrada.p_IdEmpresa, solicitudesBloqueadas, out.P_OUT_MENSAJES, out.P_OUT_ERRORESCONTADOR, sql);
					return out;
				} else {
					correlativos.add(mov.IdInterno);
				}
			}
			
			ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
			InformacionEmpresa empresa = consultarEmpresaService.logica(con, entrada.p_IdEmpresa);
			Long idPaisEmpresa = empresa.IdPais;
			String calcularComisionCertificacion = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "FONDOMUTUO", "CALCULAR_COMISION_CERTIFICACION");
			String respetaValutasCargaMasiva = _c.NO;
			try {
				respetaValutasCargaMasiva = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "FONDOMUTUO", "RespetaValutasCargaMasiva");
			}catch(Exception e) {
				logger.warn("ADVERTENCIA::CrearSolicitudAporteRescateNormal::No se encuentra parametro RespetaValutasCargaMasiva");
			}
			
			int countTransaccionesBatch = 0;
			
			List<String> series = new ArrayList<String>();
			List<Long> clientes = new ArrayList<Long>();
			List<String> regimen = new ArrayList<String>();
			List<Long> portafolios = new ArrayList<Long>();
			List<String> monedas = new ArrayList<String>();
			List<Long> foliosContrato = new ArrayList<Long>();
			ArrayList<Portafolio> listaIdPortafolios = new ArrayList<Portafolio>();
			List<String> idTipoMovimientos = new ArrayList<String>();		
			idTipoMovimientos.add(_c.AporteFondoMutuo);
			idTipoMovimientos.add(_c.RescateFondoMutuo);
			idTipoMovimientos.add(_c.RescateTraspaso);
			idTipoMovimientos.add(_c.AporteTraspaso);
			boolean existeTraspasoTotal = false;
			for(InSolicitudesAporteRescate mov : entrada.p_SolicitudesAporteRescate){
				if(mov.IdMonedaPago == null || mov.IdMonedaPago.isEmpty()) {
					mov.IdMonedaPago = mov.IdMoneda;
				}
				if(_c.SI.equalsIgnoreCase(mov.SolicitudBatch)){
					countTransaccionesBatch++;
				}
				if(!series.contains(mov.IdSerieFondo)) series.add(mov.IdSerieFondo);
				if(!clientes.contains(mov.IdCliente)) clientes.add(mov.IdCliente);
				if(!portafolios.contains(mov.IdPortafolio)) {
					portafolios.add(mov.IdPortafolio);
			        Portafolio port = new Portafolio();
			        port.IdPortafolio = mov.IdPortafolio;
					listaIdPortafolios.add(port);
				}
				if(!regimen.contains(mov.IdRegimenTributario)) regimen.add(mov.IdRegimenTributario);
				if(!foliosContrato.contains(mov.FolioContrato)) foliosContrato.add(mov.FolioContrato);
				if(_c.CalculoPorTotal.equals(mov.CalculoPorMontoCuota)) existeTraspasoTotal = true;
				
			}		
			
			ObtenerPerfilFondoMutuo obtenerPerfilFondoMutuo = new ObtenerPerfilFondoMutuo();
			EntradaObtenerPerfilFondoMutuo entradaObtenerPerfilFondoMutuo = new EntradaObtenerPerfilFondoMutuo();
			entradaObtenerPerfilFondoMutuo.p_IdUsuario = entrada.p_IdUsuario;
			logger.info("obtenerPerfilFondoMutuo");
			SalidaObtenerPerfilFondoMutuo salidaObtenerPerfilFondoMutuo = obtenerPerfilFondoMutuo.logica(con, entradaObtenerPerfilFondoMutuo);
			logger.info("obtenerPerfilFondoMutuo");
			if(salidaObtenerPerfilFondoMutuo.PerfilFondoMutuo == null && countTransaccionesBatch != entrada.p_SolicitudesAporteRescate.size()){
				out.P_OUT_ERRORESCONTADOR = 1L;
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"PERFIL_INCORRECTO",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
				eliminarBloqueoSolicitudAporteRescate(con, entrada.p_SimularOperacion, entrada.p_IdEmpresa, solicitudesBloqueadas, out.P_OUT_MENSAJES, out.P_OUT_ERRORESCONTADOR, sql);
				return out;
			}
			
			
			logger.info("llamadas varias");
			HashMap<String,Object> params = new HashMap<String,Object>();
			params.put("IdEmpresa", entrada.p_IdEmpresa);
			params.put("IdSerieFondo", series);
			logger.debug("INICIO::SQL_SELECT_SERIE_FONDO");
			List<SerieFondoSolicitudes> seriesSolicitudes = sql.query(con, SQL_SELECT_SERIE_FONDO, params, SerieFondoSolicitudes.class);
			logger.debug("FIN::SQL_SELECT_SERIE_FONDO");
			params.clear();
			params.put("IdPortafolio", portafolios);
			logger.debug("INICIO::SQL_SELECT_PORTAFOLIO");
			List<ClientePortafolioSolicitudes> clientesSolicitudes = sql.query(con, SQL_SELECT_PORTAFOLIO, params, ClientePortafolioSolicitudes.class);
			logger.debug("FIN::SQL_SELECT_PORTAFOLIO");
			params.clear();
			params.put("IdTipoMovimientoFondoMutuo", idTipoMovimientos);
			logger.debug("INICIO::SQL_SELECT_TIPO_MOVIMIENTO");
			List<TipoMovimientoFondoMutuo> tipoMovimientos = sql.query(con, SQL_SELECT_TIPO_MOVIMIENTO, params, TipoMovimientoFondoMutuo.class);
			logger.debug("FIN::SQL_SELECT_TIPO_MOVIMIENTO");
			params.clear();
			params.put("IdEmpresa", entrada.p_IdEmpresa);
			params.put("IdSerieFondo", series);
			params.put("IdRegimenTributario", regimen);
			logger.debug("INICIO::SQL_SELECT_REGIMEN");
			List<RegimenSerieDeFondo> regimenesSeries = sql.query(con, SQL_SELECT_REGIMEN, params, RegimenSerieDeFondo.class);
			logger.debug("FIN::SQL_SELECT_REGIMEN");
			params.clear();
			params.put("IdPortafolio", portafolios);
			params.put("CodigoTipoIndicador", _c.UsaCuentaInversion);
			logger.debug("INICIO::SQL_SELECT_INDICADORES_PORTAFOLIO");
			List<IndicadoresPortafolio> indicadoresPortafolio = sql.query(con, SQL_SELECT_INDICADORES_PORTAFOLIO, params, IndicadoresPortafolio.class);
			logger.debug("FIN::SQL_SELECT_INDICADORES_PORTAFOLIO");
			params.clear();
			params.put("IdPortafolio", portafolios);
			logger.debug("INICIO::SQL_SELECT_PORTAFOLIO_APV");
			List<PortafolioAPV> portafolioAPV = sql.query(con, SQL_SELECT_PORTAFOLIO_APV, params, PortafolioAPV.class);
			logger.debug("FIN::SQL_SELECT_PORTAFOLIO_APV");
			params.clear();
			params.put("IdRegimenFondoMutuo", regimen);
			logger.debug("INICIO::SQL_SELECT_CONFIGURACION_REGIMEN_PORTAFOLIO");
			List<ConfiguracionPropositoPortafolio> configuracionesRegimenPortafolio = sql.query(con, SQL_SELECT_CONFIGURACION_REGIMEN_PORTAFOLIO, params, ConfiguracionPropositoPortafolio.class);
			logger.debug("FIN::SQL_SELECT_CONFIGURACION_REGIMEN_PORTAFOLIO");
			params.clear();
			params.put("FolioContrato", foliosContrato);
			logger.debug("INICIO::SQL_SELECT_CONSULTA_TRAPASO_APV");
			List<TraspasoAPV> traspasosAPV = sql.query(con, SQL_SELECT_CONSULTA_TRAPASO_APV, params,TraspasoAPV.class);
			logger.debug("FIN::SQL_SELECT_CONSULTA_TRAPASO_APV");
			params.clear();
			
			List<SalidaTraspasoTotal> custodiasDC = new ArrayList<SalidaTraspasoTotal>();
			if(existeTraspasoTotal) {
				params.put("IdSerieFondo", series);
				params.put("IdPortafolio", portafolios);
				logger.debug("INICIO::SQL_SELECT_COUNT_CUSTODIA");
				custodiasDC = sql.query(con, SQL_SELECT_COUNT_CUSTODIA, params, SalidaTraspasoTotal.class);
				logger.debug("FIN::SQL_SELECT_COUNT_CUSTODIA");
				params.clear();
			}
			
			logger.info("llamadas varias");
			for(SerieFondoSolicitudes serie : seriesSolicitudes){
				if(!monedas.contains(serie.IdMoneda)) monedas.add(serie.IdMoneda);
			}
			params.put("IdMoneda", monedas);
			logger.info("Llamada a consultar monedas");
			logger.debug("INICIO::SQL_SELECT_MONEDA");
			List<Moneda> monedasFondo = sql.query(con, SQL_SELECT_MONEDA, params, Moneda.class);
			logger.debug("FIN::SQL_SELECT_MONEDA");
			logger.info("Finaliza consultar monedas");
			params.clear();
			
			HashMap<String,Object> hseries = Utils.List2Hash(seriesSolicitudes, "IdSerieFondo");
			HashMap<Long,Object> hclientes = Utils.List2Hash(clientesSolicitudes, "IdPortafolio");
			HashMap<String,Object> htipoMovimiento =  Utils.List2Hash(tipoMovimientos, "IdTipoMovimientoFondoMutuo");
			HashMap<String,Object> hseriesRegimen =  Utils.List2Hash(regimenesSeries, "IdRegimenFondoMutuoSerieFondo");
			HashMap<Long,Object> hindicadoresPortafolio =  Utils.List2Hash(indicadoresPortafolio, "IdPortafolio");
			HashMap<String,Object> hmonedasFondo =  Utils.List2Hash(monedasFondo, "IdMoneda");
			HashMap<Long,Object> hportafolioAPV =  Utils.List2Hash(portafolioAPV, "IdPortafolio");
			HashMap<String,Object> hconfRegimenPortafolio =  Utils.List2Hash(configuracionesRegimenPortafolio, "Id");
			HashMap<String,Object> hcustodiasDC =  Utils.List2Hash(custodiasDC, "Id");
			Map<Long,TraspasoAPV> hTraspasoAPV = traspasosAPV.parallelStream().collect(Collectors.toMap(traspaso->traspaso.IdTraspaso, traspaso->traspaso));
			
			CalcularFechaSolicitudAporteRescate calculoFechas = new CalcularFechaSolicitudAporteRescate();
			EntradaCalcularFechaSolicitudAporteRescate entradaFechas = new EntradaCalcularFechaSolicitudAporteRescate();
			entradaFechas.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaFechas.p_IdPais = idPaisEmpresa;
			
			String rescateGeneraSpotDolar = obtenerRescateGeneraMovimientoSpot(con, entrada.p_IdEmpresa);
			
			for(InSolicitudesAporteRescate solicitud : entrada.p_SolicitudesAporteRescate){
				if(solicitud.FechaOperacion == null && "APV".equalsIgnoreCase(entrada.p_IdModulo)) {
					solicitud.FechaOperacion = fechaSistema;
				}
				SerieFondoSolicitudes serie = (SerieFondoSolicitudes) hseries.get(solicitud.IdSerieFondo);
				CalcularFechaSolicitudAporteRescate.InFechas fecha = new CalcularFechaSolicitudAporteRescate.InFechas();
				if(serie != null){
					fecha.IdInterno = solicitud.IdInterno;
					fecha.IdSerieFondo = solicitud.IdSerieFondo;
					if(_c.SI.equalsIgnoreCase(solicitud.SolicitudBatch) && _c.NO.equals(respetaValutasCargaMasiva)){
						fecha.EstadoFondo = _c.Abierto;
						fecha.EstadoLiquidacion = _c.Abierto;
						fecha.NumeroDiasRetencion = 0L;
						fecha.NumeroValutaValorCuota = 0L;
						fecha.NumeroValutaCertificacion = 0L;
						fecha.NumeroValutaCurse = 0L;
						fecha.SolicitudBatch = _c.SI;
					}else{
						if(_c.TipoSolicitudAporte.equalsIgnoreCase(solicitud.TipoSolicitud)){
							fecha.EstadoFondo = _c.PerfilComercial.equalsIgnoreCase(salidaObtenerPerfilFondoMutuo.PerfilFondoMutuo) ? serie.EstadoAporteComercial : serie.EstadoAporteBackoffice;
							fecha.NumeroDiasRetencion = serie.DiasRetencionesAporte;
							fecha.NumeroValutaValorCuota = serie.DiasValutaValorCuotaAporte;
							fecha.NumeroValutaCertificacion = serie.DiasValutaCertificacionAporte;
							fecha.NumeroValutaCurse = serie.DiasValutaCurseAporte;
						}else if (_c.TipoSolicitudRescate.equalsIgnoreCase(solicitud.TipoSolicitud)){
							fecha.EstadoFondo = _c.PerfilComercial.equalsIgnoreCase(salidaObtenerPerfilFondoMutuo.PerfilFondoMutuo) ? serie.EstadoRescateComercial : serie.EstadoRescateBackoffice;
							fecha.NumeroDiasRetencion = serie.DiasRetencionesRescate;
							fecha.NumeroValutaValorCuota = serie.DiasValutaValorCuotaRescate;
							fecha.NumeroValutaCertificacion = serie.DiasValutaCertificacionRescate;
							fecha.NumeroValutaCurse = serie.DiasValutaCurseRescate;
						}else{
							fecha.EstadoFondo = _c.Cerrado;
							fecha.NumeroDiasRetencion = 0L;
							fecha.NumeroValutaValorCuota = 0L;
							fecha.NumeroValutaCertificacion = 0L;
							fecha.NumeroValutaCurse = 0L;
						}
						fecha.EstadoLiquidacion = _c.PerfilComercial.equalsIgnoreCase(salidaObtenerPerfilFondoMutuo.PerfilFondoMutuo) ? serie.EstadoLiquidezComercial : serie.EstadoLiquidezBackoffice;
						
						//SIEMPRE EL FONDO ESTA ABIERTO PARA BATCH.
						if(_c.SI.equals(solicitud.SolicitudBatch)) {
							fecha.EstadoFondo = _c.Abierto;
							fecha.EstadoLiquidacion = _c.Abierto;
						}
						fecha.SolicitudBatch = _c.NO;
					}
					
					fecha.FechaEmision = _c.FechaDefecto.equalsIgnoreCase(solicitud.FechaOperacion) ? Fecha.stringToSqlDate(serie.FechaFondo) : solicitud.FechaOperacion;
					if (_c.FechaDefecto.equalsIgnoreCase(solicitud.FechaOperacion)) {
						if (_c.TipoSolicitudAporte.equalsIgnoreCase(solicitud.TipoSolicitud)) {
							String EstadoFondoAporte = _c.PerfilComercial.equalsIgnoreCase(salidaObtenerPerfilFondoMutuo.PerfilFondoMutuo) ? serie.EstadoAporteComercial : serie.EstadoAporteBackoffice;
							fecha.FechaEmision = _c.Abierto.equalsIgnoreCase(EstadoFondoAporte) ? Fecha.stringToSqlDate(serie.FechaFondo) : fechaSistema;
						}
						if (_c.TipoSolicitudRescate.equalsIgnoreCase(solicitud.TipoSolicitud)) {
							String EstadoFondoRescate = _c.PerfilComercial.equalsIgnoreCase(salidaObtenerPerfilFondoMutuo.PerfilFondoMutuo) ? serie.EstadoRescateComercial : serie.EstadoRescateBackoffice;
							fecha.FechaEmision = _c.Abierto.equalsIgnoreCase(EstadoFondoRescate) ? Fecha.stringToSqlDate(serie.FechaFondo) : fechaSistema;
						}
					}
	
					fecha.TipoSolicitud = solicitud.TipoSolicitud;
					fecha.FondoExtranjero = serie.FondoExtranjero;
					fecha.IdPaisFondo = serie.IdPaisOrigen;
					fecha.IdPaisEmisor = serie.IdPaisEmisor;
					Moneda moneda = (Moneda) hmonedasFondo.get(serie.IdMoneda);
					fecha.IdPaisMoneda = moneda != null ? moneda.IdPais : 0L;
					if(_c.SI.equalsIgnoreCase(rescateGeneraSpotDolar) && _c.Dolar.equalsIgnoreCase(solicitud.IdMoneda) && _c.Pesos.equalsIgnoreCase(solicitud.IdMonedaPago) && _c.TipoSolicitudRescate.equalsIgnoreCase(solicitud.TipoSolicitud)) {
						fecha.RescateGeneraSpotCertificacion = _c.SI;
					}
					fecha.FondoNacionalDolar = serie.FondoNacionalDolar;
					fecha.IdMercadoFondoMutuo = serie.IdMercadoFondoMutuo;
					entradaFechas.p_SolicitudAporteRescate.add(fecha);
				}
			}
			logger.info("llamadas fechas");
			SalidaCalcularFechaSolicitudAporteRescate salidaFechas = calculoFechas.logica(con, entradaFechas);
			logger.info("llamadas fechas");
			HashMap<Long,Object> hfechas =  Utils.List2Hash(salidaFechas.P_FECHASAPORTERESCATE, "IdInterno");
			
			CalcularMontoNumeroCuota calculo = new CalcularMontoNumeroCuota();
			EntradaCalcularMontoNumeroCuota entradaCalculo = new EntradaCalcularMontoNumeroCuota();
			entradaCalculo.p_IdEmpresa = entrada.p_IdEmpresa;
			for(InSolicitudesAporteRescate solicitud : entrada.p_SolicitudesAporteRescate){
				CalcularMontoNumeroCuota.Serie serie = new CalcularMontoNumeroCuota.Serie();
				SerieFondoSolicitudes serieFondo = (SerieFondoSolicitudes) hseries.get(solicitud.IdSerieFondo);
				ResultadoFecha fecha = (ResultadoFecha) hfechas.get(solicitud.IdInterno);
				serie.IdInterno = solicitud.IdInterno;
				if (solicitud.FechaValorCuota != null) {
					serie.Fecha = Fecha.stringToSqlDate(solicitud.FechaValorCuota);
				} else if (fecha != null) {
					serie.Fecha = Fecha.stringToSqlDate(fecha.FechaAceptacion);
				} else {
					serie.Fecha = fechaSistema;
				}
				serie.IdSerieFondo = solicitud.IdSerieFondo;
			 	serie.CalculoPorMontoCuota = solicitud.CalculoPorMontoCuota;
			 	serie.Monto = solicitud.Monto;
			 	serie.NumeroCuotas = solicitud.NumeroCuotas;
			 	serie.IdMonedaFondo = serieFondo != null ? serieFondo.IdMoneda : "";
			 	serie.IdMonedaInversion = solicitud.IdMoneda;
			 	serie.TipoSolicitud =  solicitud.TipoSolicitud;
			 	serie.TipoFactorAjuste = _c.UF;
			 	serie.IdEmpresa = entrada.p_IdEmpresa;
			 	entradaCalculo.p_Series.add(serie);
			}
			
			SalidaCalcularMontoNumeroCuota salidaCalculo = calculo.logica(con, entradaCalculo);
			logger.info("secuencia");
			List<Long> secuencias = Utils.getSecuencia(con, "BSCL.FondoMutuo.BD.Secuencias::SolicitudAporteRescate", Long.valueOf(entrada.p_SolicitudesAporteRescate.size()));
			logger.info("secuencia");
			HashMap<Long,Object> valoresCuota = Utils.List2Hash(salidaCalculo.P_OUT_RESULTADO, "IdInterno");
			
			CalcularParidad calcularParidad = new CalcularParidad();
			CalcularParidadEntrada calcularParidadEntrada = new CalcularParidadEntrada();
			calcularParidadEntrada.p_IdEmpresa = entrada.p_IdEmpresa;
			calcularParidadEntrada.p_IdTrabajo = 1L;
			for(InSolicitudesAporteRescate solicitud : entrada.p_SolicitudesAporteRescate) {
				ResultadoFecha fecha = (ResultadoFecha) hfechas.get(solicitud.IdInterno);
				CalcularMontoNumeroCuota.Resultado vc = (CalcularMontoNumeroCuota.Resultado) valoresCuota.get(solicitud.IdInterno);
				CalculoEntrada calculoEntrada = new CalculoEntrada();
				calculoEntrada.IdTrabajo = 1L;
				calculoEntrada.IdInterno = solicitud.IdInterno;
				if (solicitud.FechaValorCuota != null) {
					calculoEntrada.Fecha = Fecha.stringToSqlDate(solicitud.FechaValorCuota);
				} else if (fecha != null) {
					calculoEntrada.Fecha = Fecha.stringToSqlDate(fecha.FechaAceptacion);
				} else {
					calculoEntrada.Fecha = fechaSistema;
				}
				calculoEntrada.IdMonedaOrigen = solicitud.IdMoneda;
				calculoEntrada.IdMonedaDestino = solicitud.IdMonedaPago;
				calculoEntrada.ValorCambioLocalModificado = BigDecimal.ZERO;
				calculoEntrada.MontoOrigen = vc != null ? vc.MontoDestino : solicitud.Monto;
				calculoEntrada.IdUso = "";
				calcularParidadEntrada.p_CalculosEntrada.add(calculoEntrada);
			}
			
			CalcularParidadSalida calculoMontoMonedaPago = calcularParidad.logica(con, calcularParidadEntrada);
			HashMap<Long,Object> hashMontoMonedaPago = Utils.List2Hash(calculoMontoMonedaPago.p_out_CalculosSalida, "IdInterno");
			
			CalcularComisiones calculoComisiones = new CalcularComisiones();
			EntradaCalcularComisiones entradaCalcularComisiones = new EntradaCalcularComisiones();
			entradaCalcularComisiones.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaCalcularComisiones.p_IdUsuario = entrada.p_IdUsuario;
			entradaCalcularComisiones.p_IdUsuario = entrada.p_IdUsuario;
			entradaCalcularComisiones.p_IdModulo = entrada.p_IdModulo;
			entradaCalcularComisiones.p_SimularOperacion = entrada.p_SimularOperacion;
			entradaCalcularComisiones.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
			
			for(InSolicitudesAporteRescate solicitud : entrada.p_SolicitudesAporteRescate){
				TraspasoAPV traspaso = hTraspasoAPV.get(solicitud.FolioContrato);
				CalcularComisiones.InCustodia inCustodia =  new CalcularComisiones.InCustodia();
				SerieFondoSolicitudes serieFondo = (SerieFondoSolicitudes) hseries.get(solicitud.IdSerieFondo);
				CalcularMontoNumeroCuota.Resultado vc = (CalcularMontoNumeroCuota.Resultado) valoresCuota.get(solicitud.IdInterno);
				ResultadoFecha fechas = (ResultadoFecha) hfechas.get(solicitud.IdInterno);
				inCustodia.IdInterno = solicitud.IdInterno;
				inCustodia.IdSerieFondo = solicitud.IdSerieFondo;
				inCustodia.IdInstrumento = serieFondo != null ? serieFondo.IdInstrumento : 0L;
				inCustodia.IdCliente = solicitud.IdCliente;
				inCustodia.IdPortafolio = solicitud.IdPortafolio;
				inCustodia.Cantidad = vc != null ? vc.NumeroCuotas : BigDecimal.ZERO;
				inCustodia.IdMonedaFondo = serieFondo != null ? serieFondo.IdMoneda : "";
				inCustodia.IdMonedaOperacion = solicitud.IdMoneda;
				inCustodia.TipoSolicitud = solicitud.TipoSolicitud;
				inCustodia.IdTipoMovimientoFondoMutuo = _c.TipoSolicitudAporte.equals(solicitud.TipoSolicitud) ? _c.AporteFondoMutuo : _c.RescateFondoMutuo;
				inCustodia.FechaValorCuota = solicitud.FechaValorCuota != null ? Fecha.toStringDate(solicitud.FechaValorCuota) : Fecha.toStringDate(fechas.FechaValorCuota);
				inCustodia.FechaCurse = Fecha.toStringDate(fechas.FechaAceptacion);
				inCustodia.EsTraspaso = traspaso != null ? _c.SI : solicitud.EsTraspaso;
				entradaCalcularComisiones.p_DatosCustodia.add(inCustodia);
			}
			
			SalidaCalcularComisiones salidaCalculoComisiones = new SalidaCalcularComisiones();
			logger.info("comisiones");
			if(_c.SI.equalsIgnoreCase(calcularComisionCertificacion)){
				salidaCalculoComisiones = calculoComisiones.logica(con, entradaCalcularComisiones);
			}
			logger.info("comisiones");
			HashMap<Long,Object> hcomisiones = Utils.List2Hash(salidaCalculoComisiones.P_OUT_RESULTADO, "IdInterno");
			
			List<SolicitudAporteRescate> solicitudesProcesar = new ArrayList<SolicitudAporteRescate>();
			
			ServicioCumpleRequisitosCrearSolicitudAporteRescate cumpleRequisitos = new ServicioCumpleRequisitosCrearSolicitudAporteRescate();
			EntradaServicioCumpleRequisitosCrearSolicitudAporteRescate entradaCumpleRequisitos = new EntradaServicioCumpleRequisitosCrearSolicitudAporteRescate();
			entradaCumpleRequisitos.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaCumpleRequisitos.p_IdUsuario = entrada.p_IdUsuario;
			entradaCumpleRequisitos.p_PermiteOperarProspectos = entrada.p_PermiteOperarProspectos;
			
			ValidaPatrimonioEmisor validaPatrimonioEmisor = new ValidaPatrimonioEmisor();
			EntradaValidaPatrimonioEmisor entradaValidaPatrimonioEmisor = new EntradaValidaPatrimonioEmisor();
			entradaValidaPatrimonioEmisor.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaValidaPatrimonioEmisor.p_IdUsuario = entrada.p_IdUsuario;
			entradaValidaPatrimonioEmisor.p_Fecha = fechaSistema;
			int i = 0;
			List<ResultadoSolicitud> resultadoTemp = new ArrayList<ResultadoSolicitud>();
			
			HashMap<String,BigDecimal> hashMontosPortafolioEmisor = new HashMap<String,BigDecimal>();
			
			List<Long> folioContratoSolicitudes = Utils.getSecuencia(con, "BSCL.FondoMutuo.BD.Secuencias::FolioContrato", (long) entrada.p_SolicitudesAporteRescate.size());
			
			int segundosZonaHoraria = FechaDB.ObtenerSegundosZonaHoraria(con, entrada.p_IdEmpresa);
	        Fecha.setLocalOffSet(segundosZonaHoraria);
	        Timestamp fechaHoraUTC = Fecha.fechaSistemaHoraLocal_fechaHoraUTC(fechaSistema,FechaDB.ObtenerHoraSistemaLocal(con,entrada.p_IdEmpresa));
	
			for(InSolicitudesAporteRescate solicitud : entrada.p_SolicitudesAporteRescate){
				String tm = _c.TipoSolicitudAporte.equalsIgnoreCase(solicitud.TipoSolicitud) ? (_c.SI.equalsIgnoreCase(solicitud.EsTraspaso) ? _c.AporteTraspaso : _c.AporteFondoMutuo) : (	_c.TipoSolicitudRescate.equalsIgnoreCase(solicitud.TipoSolicitud) ? (_c.SI.equalsIgnoreCase(solicitud.EsTraspaso) || _c.RazonRetiroTraspaso.equalsIgnoreCase(solicitud.EsTraspaso)  ? _c.RescateTraspaso : _c.RescateFondoMutuo): "");
				ClientePortafolioSolicitudes cliente = (ClientePortafolioSolicitudes) hclientes.get(solicitud.IdPortafolio);
				TipoMovimientoFondoMutuo tipoMovimiento = (TipoMovimientoFondoMutuo) htipoMovimiento.get(tm);
				SerieFondoSolicitudes serie = (SerieFondoSolicitudes) hseries.get(solicitud.IdSerieFondo);
				CalcularMontoNumeroCuota.Resultado vc = (CalcularMontoNumeroCuota.Resultado) valoresCuota.get(solicitud.IdInterno);
				CalculoSalida montoMonedaPago = (CalculoSalida) hashMontoMonedaPago.get(solicitud.IdInterno);
				RegimenSerieDeFondo r = (RegimenSerieDeFondo) hseriesRegimen.get(solicitud.IdSerieFondo + "_" +solicitud.IdRegimenTributario);
				ConfiguracionPropositoPortafolio confPropositoPortafolio = null;
				if(cliente != null) confPropositoPortafolio = (ConfiguracionPropositoPortafolio) hconfRegimenPortafolio.get(solicitud.IdRegimenTributario + "_" + cliente.Proposito);
				
				ResultadoFecha fecha = (ResultadoFecha) hfechas.get(solicitud.IdInterno);
				IndicadoresPortafolio indicadorPortafolio = (IndicadoresPortafolio) hindicadoresPortafolio.get(solicitud.IdPortafolio);
				ResultadoCalcularComision comision = (ResultadoCalcularComision) hcomisiones.get(solicitud.IdInterno);
				SolicitudAporteRescate sol = new SolicitudAporteRescate();
				ServicioCumpleRequisitosCrearSolicitudAporteRescate.InSolicitudes solVal = new ServicioCumpleRequisitosCrearSolicitudAporteRescate.InSolicitudes();
				ServicioCumpleRequisitosCrearSolicitudAporteRescate.InPortafolio portVal = new ServicioCumpleRequisitosCrearSolicitudAporteRescate.InPortafolio();
				
				String procedenciaCanal = "";
				
				if ("PAC".equalsIgnoreCase(entrada.p_IdOrigenProceso)) {
					procedenciaCanal = "PAC";
				}else {
					procedenciaCanal = solicitud.Canal != null && !solicitud.Canal.isEmpty() ? solicitud.Canal : entrada.p_IdTipoCanalIngreso != null ? entrada.p_IdTipoCanalIngreso : "";
				}
				
				//SE COMPLETA EL MOVIMIENTO
				sol.IdInterno = solicitud.IdInterno;
				sol.IdSolicitudAporteRescate = secuencias.get(i);
			 	sol.IdCliente = solicitud.IdCliente;
			 	sol.IdPortafolio = solicitud.IdPortafolio;
			 	sol.TipoSolicitud = solicitud.TipoSolicitud;
			 	sol.IdSerieFondo = serie != null ? serie.IdSerieFondo : "";
			 	sol.IdTipoMovimientoFondoMutuo = tm;
			 	sol.IdDividendo = entrada.p_NumeroDividendo;
				sol.IdSerieFondoDestino = "";
				sol.IdEmisorDestino = solicitud.IdEmisorDestino;
				sol.IdClienteDestino = 0L;
				sol.IdPortafolioDestino = 0L;
				sol.IndicadorMLT = solicitud.IndicadorMLT;
				sol.Overnight = _c.NO;
				sol.IdSolicitudAporteRescateRelacionado = solicitud.IdSolicitudAporteRescateRelacionado != null ? solicitud.IdSolicitudAporteRescateRelacionado : 0L;
				sol.TipoCarteraNormativa = _c.AporteFondoMutuo.equalsIgnoreCase(sol.IdTipoMovimientoFondoMutuo) && r != null ? r.TipoCarteraNormativa : "";
				sol.IdTipoCartera = "";
				sol.CalculoPorMontoCuota = solicitud.CalculoPorMontoCuota;
				sol.FechaEmision = Fecha.toDate(fechaSistema);
				sol.FechaOriginalInversion = Fecha.toDate("0001-01-01");
				//sol.FechaIngresoRegistro = Fecha.toTimeStamp(FechaDB.ObtenerFechaHoraSistemaUTC(con, entrada.p_IdEmpresa));
				sol.FechaIngresoRegistro = fechaHoraUTC;
				sol.IdMoneda = solicitud.IdMoneda;
				sol.Estado =  _c.EstadoIngresado;
				sol.IdMedioDePago = solicitud.IdMedioDePago;
				sol.Glosa = solicitud.Glosa;
				sol.IdEmpresa = entrada.p_IdEmpresa;
				sol.IdUsuario = entrada.p_IdUsuario;
				sol.IdModulo = entrada.p_IdModulo;
				sol.IdEmisorOrigen = serie != null ? serie.IdEmisor : "";
				sol.SolicitudBatch = solicitud.SolicitudBatch;
				sol.OrigenOperacion = solicitud.OrigenOperacion;
				sol.IdAutorizadoOrigen = solicitud.IdAutorizadoOrigen;
				sol.IdEmpleadoEjecutivo = cliente != null && cliente.IdEmpleadoEjecutivo != null ? cliente.IdEmpleadoEjecutivo : 0L;
				sol.IdRegimenTributario = solicitud.IdRegimenTributario;
				sol.IngresoAutomatico = solicitud.IngresoAutomatico;
				sol.EsPorMontoLiquido = solicitud.EsPorMontoLiquido;
				sol.Porcentaje = solicitud.Porcentaje;
				sol.IdTipoCanalIngreso = procedenciaCanal;
				sol.FolioContrato = solicitud.FolioContrato > 0 ? solicitud.FolioContrato : folioContratoSolicitudes.get(entrada.p_SolicitudesAporteRescate.indexOf(solicitud));
				sol.IdMonedaPago = solicitud.IdMonedaPago;
				sol.IdPersona = cliente != null ? cliente.IdPersona : 0L;
				sol.IdTipoInstrumento = serie != null ? serie.IdTipoInstrumento : "";
				sol.IdInstrumento = serie != null ? serie.IdInstrumento : 0L;
				sol.Visado = _c.TipoSolicitudRescate.equalsIgnoreCase(sol.TipoSolicitud) || (_c.TipoSolicitudAporte.equalsIgnoreCase(sol.TipoSolicitud) && _c.CuentaInversion.equalsIgnoreCase(sol.IdMedioDePago)) ? _c.NO : solicitud.Visado; 
				sol.IdPreferenciaPagoPortafolio = solicitud.IdPreferenciaPagoPortafolio;
				sol.EstadoProceso = _c.EstadoPendiente;
				sol.Monto = BigDecimal.ZERO;
				sol.NumeroCuotas = BigDecimal.ZERO;
				sol.ValorCuota = BigDecimal.ZERO;
				sol.CostoUF = BigDecimal.ZERO;
			    sol.CostoMonedaLocal = BigDecimal.ZERO; 
				sol.ValorUFFechaInversion = BigDecimal.ZERO; 
				sol.ParidadFechaInversion = BigDecimal.ZERO;
				sol.IdMonedaSistema = "";
				sol.MontoMonedaSistema = BigDecimal.ZERO;
				sol.MontoPagoLocal = BigDecimal.ZERO; 
				sol.MontoPago = BigDecimal.ZERO;
				sol.MontoComision = BigDecimal.ZERO;
				sol.MontoComisionLocal = BigDecimal.ZERO;
				sol.MontoComisionMonedaSistema = BigDecimal.ZERO;
				sol.GeneraSpot = sol.IdMoneda.equalsIgnoreCase(sol.IdMonedaPago) ? _c.NO : _c.SI;
				sol.ValidaSpot = _c.NO;
				sol.ForzarSpot = solicitud.ForzarSpot;
				sol.TipoContratoOperacion = solicitud.TipoContratoOperacion;
				sol.FolioContratoOperacion = solicitud.FolioContratoOperacion;
				sol.IdOrigenRecaCliente = solicitud.IdOrigenRecaCliente;
				sol.SimulaCambioPortafolioEjecutadoOk = solicitud.SimulaCambioPortafolioEjecutadoOk;
				sol.IngresoDesde = solicitud.IngresoDesde;
				if(vc != null){
					if (vc.ValorCuota != null) {
						sol.Monto = vc.Monto;
						sol.NumeroCuotas = vc.NumeroCuotas;
						sol.ValorCuota = vc.ValorCuota;
						sol.CostoUF = vc.CostoEnFactorAjuste;
						sol.CostoMonedaLocal = vc.MontoDestino; 
						sol.ValorUFFechaInversion = vc.ValorFactorAjuste; 
						sol.ParidadFechaInversion = vc.ValorParidad;
						sol.IdMonedaSistema = vc.IdMonedaSistema;
						sol.MontoMonedaSistema = vc.MontoMonedaSistema;
						BigDecimal comisionPER = BigDecimal.ZERO;
						if(comision == null || (comision != null && comision.TipoComision != null && comision.TipoComision.equalsIgnoreCase(_c.TipoComisionTrimestral) && _c.TipoSolicitudAporte.equals(sol.TipoSolicitud)) ) {
							sol.MontoComision = BigDecimal.ZERO;
							sol.MontoComisionLocal = BigDecimal.ZERO;
							sol.MontoComisionMonedaSistema = BigDecimal.ZERO;
							sol.ValorPivoteLocalConversionLocal = BigDecimal.ZERO;
							sol.ValorPivoteSistemaConversionSistema = BigDecimal.ZERO;
						} else {
							if(comision.TipoComision != null && comision.TipoComision.equalsIgnoreCase(_c.TipoComisionTrimestral)) {
								sol.MontoComisionLocal = comision.ComisionLocal;
								sol.MontoComisionMonedaSistema = comision.ComisionMonedaSistema;
								sol.ValorPivoteLocalConversionLocal = comision.ValorPivoteLocalConversionLocal;
								sol.ValorPivoteSistemaConversionSistema = comision.ValorPivoteSistemaConversionSistema;
								sol.Monto = vc.Monto.subtract(comision.Comision);
								sol.NumeroCuotas = vc.NumeroCuotas.subtract(comision.CantidadComision);
								sol.MontoInversion = sol.Monto;
								sol.NumeroCuotasInversion = sol.NumeroCuotas;
								sol.CostoMonedaLocal = _c.TipoComisionTrimestral.equals(comision.TipoComision) ? sol.CostoMonedaLocal.subtract(comision.Comision, MathContext.DECIMAL128) : sol.CostoMonedaLocal;
								comisionPER = comision.Comision;
							} else {
								sol.MontoComision = comision.Comision;
								sol.MontoComisionLocal = comision.ComisionLocal;
								sol.MontoComisionMonedaSistema = comision.ComisionMonedaSistema;
								sol.ValorPivoteLocalConversionLocal = comision.ValorPivoteLocalConversionLocal;
								sol.ValorPivoteSistemaConversionSistema = comision.ValorPivoteSistemaConversionSistema;
							}
						}
					
						sol.MontoPagoLocal = vc.MontoDestino != null ? vc.MontoDestino.subtract(sol.MontoComisionLocal, MathContext.DECIMAL128) : BigDecimal.ZERO;
						
						if( comisionPER.compareTo(BigDecimal.ZERO) == 1 ) {
							sol.MontoPago = sol.MontoPago.subtract(comisionPER, MathContext.DECIMAL128);
						} else {
							sol.MontoPago = vc.Monto != null ? vc.Monto.subtract(sol.MontoComision, MathContext.DECIMAL128) : BigDecimal.ZERO;
						}
						
						sol.CostoMonedaLocalInversion = vc.MontoDestino;
						sol.ValorCuotaInversion = vc.ValorCuota;
						sol.ParidadFechaCertificacion = vc.ValorParidad;
					}
				}
				if(montoMonedaPago != null) {
					sol.MontoMonedaPago = MonedaFormato.formatear(con, montoMonedaPago.MontoDestino, MonedaFormato.MONTO, sol.IdMonedaPago).valor;
				}
				if(fecha != null){
					sol.FechaAceptacion = fecha.FechaAceptacion;
				    sol.FechaValorCuota = solicitud.FechaValorCuota != null ? solicitud.FechaValorCuota : fecha.FechaValorCuota;
				    sol.FechaCertificacion = fecha.FechaCertificacion;
				    sol.FechaLiquidacion = fecha.FechaLiquidacion;
				}
				if(r != null){
					switch(r.GrupoRegimen){
						case "APV":
							sol.Tributacion = _c.TributacionAPV;
							break;
						case "ART108":
							sol.Tributacion = _c.TributacionART108;
							break;
						case "ART107":
							sol.Tributacion = _c.TributacionART107;
							break;
						case "57BIS":
							sol.Tributacion = _c.Tributacion57BIS;
							break;
						default:
							sol.Tributacion = _c.TributacionDefecto;
							break;
					}
				}
				
				solicitudesProcesar.add(sol);
				
				//VALIDACIONES
				solVal.IdInterno = sol.IdInterno;
				solVal.IdCliente = sol.IdCliente;
				solVal.IdPortafolio = sol.IdPortafolio;
				solVal.IdSerieFondo = sol.IdSerieFondo;
				solVal.IdTipoMovimientoFondoMutuo = tipoMovimiento != null ? tipoMovimiento.IdTipoMovimientoFondoMutuo : "";
				solVal.Monto = sol.Monto;
				solVal.NumeroCuotas = sol.NumeroCuotas;
				solVal.IdEstadoSerie = "";
				solVal.EsDiferido = _c.SI;
				solVal.EsMontoMaximo = _c.SI;
				solVal.EsMontoMinimo = _c.SI;
				solVal.PermiteCambioGrupo = _c.SI;
				solVal.EsPortafolioValido = _c.SI;
				solVal.PermiteRescateMandato = _c.SI;
				solVal.EsCuentaInversionValido = _c.SI;
				solVal.EsFechaValidaAporte = _c.SI;
				solVal.EsFechaValidaRescate = _c.SI;
				solVal.ValorCuota = sol.ValorCuota;
				solVal.Overnight = _c.NO;
				solVal.EsMoneyMarket = _c.SI;
				solVal.EsFamiliaEquivalente = _c.SI;
				solVal.EsPortafolioRegimenValido = _c.SI;
				solVal.EsRegimenValido = _c.SI;
				solVal.EsCuentaInversionValido = _c.SI;
				solVal.PermiteAporte = _c.SI;
				solVal.EsRegimenDestinoValido = _c.SI;
				solVal.Glosa = solicitud.Glosa;
				solVal.ProcedenciaCanal = procedenciaCanal;
				if(serie != null){
					solVal.IdEstadoSerie = serie.IdEstado;
					solVal.RestringeAporte = serie.RestringeAporte;
					solVal.RestringeRescate = serie.RestringeRescate;
					if(_c.TipoSolicitudAporte.equalsIgnoreCase(solicitud.TipoSolicitud)){
						solVal.EstadoFondo = _c.PerfilComercial.equalsIgnoreCase(salidaObtenerPerfilFondoMutuo.PerfilFondoMutuo) ? serie.EstadoAporteComercial : serie.EstadoAporteBackoffice;
						solVal.EstadoBloqueo = serie.BloqueoAporte;
						solVal.EsMontoMaximo = serie.MontoMaximo.compareTo(BigDecimal.ZERO) != 0 && serie.MontoMaximo.compareTo(sol.Monto) == -1 ? _c.NO : _c.SI;
						if(r != null && _c.SI.equals(r.ValidaMontoMinimoInstrumento)) {
							solVal.EsMontoMinimo = serie.MontoMinimo.compareTo(BigDecimal.ZERO) != 0 && serie.MontoMinimo.compareTo(sol.Monto) == 1 ? _c.NO : _c.SI;
						}
						//PATRIMONIOS ALTOS
						BigDecimal montoPortafolioPorEmisor = hashMontosPortafolioEmisor.get(sol.IdCliente + "_" + serie.IdEmisor);
						if(montoPortafolioPorEmisor == null) montoPortafolioPorEmisor = BigDecimal.ZERO;
						montoPortafolioPorEmisor = montoPortafolioPorEmisor.add(sol.MontoMonedaSistema, MathContext.DECIMAL128);
						hashMontosPortafolioEmisor.put(sol.IdCliente + "_" + serie.IdEmisor, montoPortafolioPorEmisor);
						if(_c.SI.equals(serie.PatrimonioAltos)) {
							ValidaPatrimonioEmisor.Solicitud valPatrimonio = new ValidaPatrimonioEmisor.Solicitud();
							valPatrimonio.IdInterno = sol.IdInterno;
							valPatrimonio.IdCliente = sol.IdCliente;
							valPatrimonio.IdSerieFondo = serie.IdSerieFondo;
							valPatrimonio.IdEmisor = serie.IdEmisor;
							valPatrimonio.MontoPatrimonio = solicitud.Monto; //monto para validar el patrimonio emisor
							valPatrimonio.ValorCuota = sol.ValorCuota;
							entradaValidaPatrimonioEmisor.p_Solicitudes.add(valPatrimonio);
						}
					}else if (_c.TipoSolicitudRescate.equalsIgnoreCase(solicitud.TipoSolicitud)){
						solVal.EstadoFondo = _c.PerfilComercial.equalsIgnoreCase(salidaObtenerPerfilFondoMutuo.PerfilFondoMutuo) ? serie.EstadoRescateComercial : serie.EstadoRescateBackoffice;
						solVal.EstadoBloqueo = serie.BloqueoRescate;
						solVal.EsMontoMaximo = _c.SI;
						solVal.EsMontoMinimo = _c.SI;
					}
					solVal.EstadoLiquidez = _c.PerfilComercial.equalsIgnoreCase(salidaObtenerPerfilFondoMutuo.PerfilFondoMutuo) ? serie.EstadoLiquidezComercial : serie.EstadoLiquidezBackoffice;
					solVal.EsFondoValido = _c.SI;
					if(!sol.IdMoneda.equalsIgnoreCase(serie.IdMoneda) && _c.NO.equalsIgnoreCase(serie.Diferido)){
						solVal.EsDiferido = _c.NO;
					}
					
				}
				if(r == null 
						&& ((_c.TipoSolicitudRescate.equals(solicitud.TipoSolicitud) 
								&& !_c.CalculoPorTotal.equals(solicitud.CalculoPorMontoCuota)) 
								|| _c.TipoSolicitudAporte.equalsIgnoreCase(sol.TipoSolicitud))){
					solVal.EsRegimenValido = _c.NO;
				}else {
					solVal.PermiteAporte = _c.TipoSolicitudAporte.equalsIgnoreCase(sol.TipoSolicitud) ? r.PermiteAporte : _c.SI;
				}
				
				SalidaTraspasoTotal custodiaDC = (SalidaTraspasoTotal) hcustodiasDC.get(sol.IdPortafolio + "_" +sol.IdSerieFondo);
	
				if (r != null){
					if(("S".equalsIgnoreCase(r.PermiteRescate)) || ("S".equalsIgnoreCase(r.PermiteTraspaso) && _c.RazonRetiroTraspaso.equalsIgnoreCase(solicitud.EsTraspaso))) {
						solVal.PermiteRescate = _c.SI;
					}else {
						solVal.PermiteRescate = _c.TipoSolicitudRescate.equalsIgnoreCase(sol.TipoSolicitud) ? r.PermiteRescate : _c.SI;;
					}
				}else if(custodiaDC != null && custodiaDC.Id != null && custodiaDC.Cantidad > 0) {
					solVal.PermiteRescate = _c.NO;
				}
				
				if(confPropositoPortafolio == null && (_c.TipoSolicitudAporte.equalsIgnoreCase(sol.TipoSolicitud) || (_c.TipoSolicitudRescate.equals(solicitud.TipoSolicitud) && !_c.CalculoPorTotal.equals(solicitud.CalculoPorMontoCuota)))) solVal.EsPortafolioRegimenValido = _c.NO;
				if(_c.CuentaInversion.equalsIgnoreCase(sol.IdMedioDePago) && _c.NO.equalsIgnoreCase(indicadorPortafolio.ValorIndicador)){
					solVal.EsCuentaInversionValido = _c.NO;
				}
				
				if(cliente != null){
					portVal.IdInterno = sol.IdInterno;
					portVal.IdPortafolio = sol.IdPortafolio;
					portVal.IdPortafolioReal = cliente.IdPortafolio;
					portVal.IdEstadoPortafolio = cliente.IdEstadoPortafolio;
					portVal.IdCliente = sol.IdCliente;
					portVal.IdClienteReal = cliente.IdCliente;
					portVal.IdEstadoCliente = cliente.IdEstadoCliente;
					portVal.DocumentoIdentidad = cliente.DocumentoIdentidad;
					portVal.IdEmpleadoPortafolio = cliente.IdEmpleadoEjecutivo;
					portVal.IdClienteDestino = sol.IdClienteDestino;
					portVal.IdClienteRealDestino = 0L;
					portVal.IdPortafolioDestino = sol.IdPortafolio;
					portVal.IdPortafolioRealDestino = 0L;
					portVal.IdEstadoClienteDestino = "";
					portVal.IdEstadoPortafolioDestino = "";
				}
				
				solVal.IdTipoOperacion=_c.TipoSolicitudAporte.equalsIgnoreCase(sol.TipoSolicitud) ? _c.TipoOperacionAporteFondoMutuo : _c.TipoOperacionRescateFondoMutuo;
				solVal.IdTipoProducto=_c.TipoOperacion;
				
	
				//Para portafolios con regimen BF (Bonificacion Fiscal) no se debe validar el monto Minimo para ingreso ni certificacion
				boolean esPortafolioBonificacionFiscal = cliente.CodigoPortafolio.equalsIgnoreCase(_c.BonificacionFiscal);
				boolean solicitudIgnoraMontoMinimo = _c.NO.equals(solVal.EsMontoMinimo) && _c.SI.equals(solicitud.IgnorarMontoMinimo);
				if (esPortafolioBonificacionFiscal || solicitudIgnoraMontoMinimo)
				{
					solVal.EsMontoMinimo = _c.SI;
				}
						
				entradaCumpleRequisitos.p_Portafolios.add(portVal);
				entradaCumpleRequisitos.p_SolicitudesAporteRescate.add(solVal);
				
				//SE PREPARA SALIDA
				ResultadoSolicitud solicitudSalida = new ResultadoSolicitud();
				solicitudSalida = Utils.copyPojo(sol, solicitudSalida);
				if(serie != null){
					solicitudSalida.IdFondo = serie.IdFondo;
					solicitudSalida.Nemotecnico = serie.Nemotecnico;
					solicitudSalida.NombreFondo = serie.NombreFondo;
					solicitudSalida.NombreCorto = serie.NombreCortoTipoFondo;
					solicitudSalida.NombreSerieFondo = serie.NombreSerieFondo;
					solicitudSalida.CodigoSerie = serie.CodigoSerie;
					solicitudSalida.IdEmisorOrigen = serie.IdEmisor;
				}
				if(cliente != null){
					solicitudSalida.DocumentoIdentidad = cliente.DocumentoIdentidad;
					solicitudSalida.CodigoPortafolio = cliente.CodigoPortafolio;
					solicitudSalida.NombreCliente = cliente.NombreCompleto;
					solicitudSalida.IdPortafolio = cliente.IdPortafolio;
				}
				solicitudSalida.MontoTransferencia = sol.Monto;
				solicitudSalida.NumeroCuotaTransferencia = sol.NumeroCuotas;
				solicitudSalida.FechaAceptacion = Fecha.stringToSqlDate(sol.FechaAceptacion);
				solicitudSalida.FechaOriginalInversion = Fecha.stringToSqlDate(sol.FechaOriginalInversion);
				solicitudSalida.FechaIngresoRegistro = Fecha.toStringTimeStamp(sol.FechaIngresoRegistro);
				solicitudSalida.FechaLiquidacion = Fecha.stringToSqlDate(sol.FechaLiquidacion);
				solicitudSalida.FechaCertificacion = Fecha.stringToSqlDate(sol.FechaCertificacion);
				solicitudSalida.FechaValorCuota = Fecha.stringToSqlDate(sol.FechaValorCuota);
				resultadoTemp.add(solicitudSalida);
				
				i++;
				
			};//fin completar movimiento
			
			HashMap<Long,Object> hSolicitudSalida = Utils.List2Hash(resultadoTemp, "IdInterno");
			logger.info("requisitos");
			SalidaServicioCumpleRequisitosCrearSolicitudAporteRescate salidaCumpleRequisitos = cumpleRequisitos.logica(con, entradaCumpleRequisitos);
			Map<Long,List<MensajeSalida>> mensajesSalidaAgrupadosPorSolicitud = salidaCumpleRequisitos.P_OUT_MENSAJES.parallelStream().collect(Collectors.groupingBy(mje->Math.abs(mje.id)));
			for(SolicitudAporteRescate sol : solicitudesProcesar){
				List<MensajeSalida> mensajesSalidaSolicitud = mensajesSalidaAgrupadosPorSolicitud.get(sol.IdInterno);
				if(mensajesSalidaSolicitud != null){
					out.P_OUT_MENSAJES.addAll(mensajesSalidaSolicitud);
					Long errores = mensajesSalidaSolicitud.parallelStream().filter(mje->MensajeSalida.MENSAJE_ERROR.equalsIgnoreCase(mje.tipo)).count();
					out.P_OUT_ERRORESCONTADOR += errores;
					if(!errores.equals(0L)){
						sol.EstadoProceso = _c.EstadoRechazado;
					}
				}
			}
			
			logger.info("requisitos");
			long registrosPorProcesar = solicitudesProcesar.parallelStream().filter(sol->_c.EstadoPendiente.equalsIgnoreCase(sol.EstadoProceso)).count();
			
			if(registrosPorProcesar == 0){
				eliminarBloqueoSolicitudAporteRescate(con, entrada.p_SimularOperacion, entrada.p_IdEmpresa, solicitudesBloqueadas, out.P_OUT_MENSAJES, out.P_OUT_ERRORESCONTADOR, sql);
				return out;
			}
			
			
			//VALIDACION INVERSIONES
			EntradaValidaMontoInversion entradaValidaMontoInversion  = new EntradaValidaMontoInversion();
			entradaValidaMontoInversion.p_IdEmpresa=entrada.p_IdEmpresa;
			entradaValidaMontoInversion.p_IdUsuario=entrada.p_IdUsuario;
			entradaValidaMontoInversion.p_Solicitudes= new ArrayList<ValidaMontoInversion.Solicitud>();
			
			entradaValidaMontoInversion.p_Solicitudes = solicitudesProcesar.parallelStream().filter(solicitud->_c.TipoSolicitudAporte.equals(solicitud.TipoSolicitud)).map(solicitud->{
				SerieFondoSolicitudes serieFondoSolicitud = (SerieFondoSolicitudes)hseries.get(solicitud.IdSerieFondo);
				RegimenSerieDeFondo regimenSerieFondo = (RegimenSerieDeFondo) hseriesRegimen.get(solicitud.IdSerieFondo + "_" +solicitud.IdRegimenTributario);
				ValidaMontoInversion.Solicitud solicitudAValidar = new ValidaMontoInversion.Solicitud();
				solicitudAValidar.IdInterno = solicitud.IdInterno;
				solicitudAValidar.FolioContrato = solicitud.FolioContrato;
				solicitudAValidar.AportesAltos = serieFondoSolicitud.AportesAltos;
				solicitudAValidar.Monto = solicitud.MontoMonedaSistema;
				solicitudAValidar.ValidaMontoMinimo = regimenSerieFondo != null ? regimenSerieFondo.ValidaMontoMinimoInstrumento : _c.SI;
				return solicitudAValidar;
			}).collect(Collectors.toList());
			
			if(entradaValidaMontoInversion.p_Solicitudes.size() > 0) {
				ValidaMontoInversion validaMontoInversion = new ValidaMontoInversion();
				SalidaValidaMontoInversion salidaValidaMontoInversion = validaMontoInversion.logica(con, entradaValidaMontoInversion);
				
				for(MensajeSalida mje : salidaValidaMontoInversion.P_OUT_MENSAJES){
					out.P_OUT_MENSAJES.add(mje);
					if(MensajeSalida.MENSAJE_ERROR.equalsIgnoreCase(mje.tipo)){
						out.P_OUT_ERRORESCONTADOR++;
						for(SolicitudAporteRescate sol : solicitudesProcesar){
							if(sol.IdInterno.equals(mje.id)){
								sol.EstadoProceso = _c.EstadoRechazado;
								break;
							}
						}
					}
				}
			}
			
			Date fechaMaximaCertificacion = Fecha.toDate("0001-01-01");
			for(SolicitudAporteRescate sol : solicitudesProcesar){
				if(_c.EstadoPendiente.equalsIgnoreCase(sol.EstadoProceso) && sol.FechaCertificacion.compareTo(fechaMaximaCertificacion) == 1){
					fechaMaximaCertificacion = sol.FechaCertificacion;
				}
			}
			//PATROMINO EMISOR
			if(esValidacionPatrimonio(entrada, entradaValidaPatrimonioEmisor)) {
				SalidaValidaPatrimonioEmisor salidaValidaPatrimonioEmisor = validaPatrimonioEmisor.logica(con, entradaValidaPatrimonioEmisor);
				for(MensajeSalida mje : salidaValidaPatrimonioEmisor.P_OUT_MENSAJES){
					if(MensajeSalida.MENSAJE_ERROR.equalsIgnoreCase(mje.tipo)){
						out.P_OUT_ERRORESCONTADOR++;
						out.P_OUT_MENSAJES.add(mje);
						for(SolicitudAporteRescate sol : solicitudesProcesar){
							if(sol.IdInterno.equals(mje.id)){
								sol.EstadoProceso = _c.EstadoRechazado;
								break;
							}
						}
					}
				}
			}
			
			//LLAMAMOS FLUJO CAJA
			List<Long> portFlujo = new ArrayList<Long>();
			List<String> monedaFlujo = new ArrayList<String>();
			HashMap<String,BigDecimal> portTotalesMontos = new HashMap<String,BigDecimal>();
			SaldoFlujoCaja saldoFlujoCaja = new SaldoFlujoCaja();
			SaldoFlujoCaja.ServiceEntrada entradaSaldoFlujoCaja = new SaldoFlujoCaja.ServiceEntrada();
			entradaSaldoFlujoCaja.P_IDEMPRESA 		= entrada.p_IdEmpresa;
			entradaSaldoFlujoCaja.P_FECHADESDE 		= fechaSistema;
			entradaSaldoFlujoCaja.P_FECHAHASTA 		= Fecha.stringToSqlDate(fechaMaximaCertificacion);
			entradaSaldoFlujoCaja.P_USO = _c.UsoFlujoCajaIngreso;
			entradaSaldoFlujoCaja.P_DESPLEGARENUI 	= _c.NO;
			entradaSaldoFlujoCaja.P_AGRUPARPORORIGEN = _c.NO;
			entradaSaldoFlujoCaja.P_PORTAFOLIOACONSULTAR = new ArrayList<SaldoFlujoCaja.ObjPORTAFOLIOACONSULTAR>();
			if(esOrigenProcesosManuales(entrada)) {
			for(SolicitudAporteRescate sol : solicitudesProcesar){
				if("PAC".equalsIgnoreCase(entrada.p_IdOrigenProceso) && "EUR".equalsIgnoreCase(sol.IdMoneda)) {
					HashMap<String,String> valores = new HashMap<String,String>();
					valores.put("IdSerieFondo", 		sol.IdSerieFondo);
					
					out.P_OUT_ERRORESCONTADOR++;
					out.P_OUT_MENSAJES.add(new MensajeSalida(sol.IdInterno,MensajeSalida.MENSAJE_ERROR,"MONEDA_INVALIDA_PAC",valores,con,entrada.p_IdEmpresa));
					continue;
				}else if(  _c.EstadoPendiente.equalsIgnoreCase(sol.EstadoProceso) && _c.TipoSolicitudAporte.equalsIgnoreCase(sol.TipoSolicitud) && (_c.CuentaInversion.equalsIgnoreCase(sol.IdMedioDePago))){
						String 		id 			= sol.IdPortafolio.toString() + "_" + sol.IdMonedaPago;
						BigDecimal 	montoAcum 	= portTotalesMontos.get(id) != null ? portTotalesMontos.get(id) : BigDecimal.ZERO;
						portTotalesMontos.put(id, montoAcum.add(sol.MontoMonedaPago, MathContext.DECIMAL128));
						
						if(!portFlujo.contains(sol.IdPortafolio) || !monedaFlujo.contains(sol.IdMonedaPago)){
							SaldoFlujoCaja.ObjPORTAFOLIOACONSULTAR saldo = new SaldoFlujoCaja.ObjPORTAFOLIOACONSULTAR();
							saldo.IdPortafolio = sol.IdPortafolio;
							saldo.IdMoneda = sol.IdMonedaPago;
							entradaSaldoFlujoCaja.P_PORTAFOLIOACONSULTAR.add(saldo);
							if(!portFlujo.contains(sol.IdPortafolio)) portFlujo.add(sol.IdPortafolio);
							if(!monedaFlujo.contains(sol.IdMonedaPago)) monedaFlujo.add(sol.IdMonedaPago);
						}
					}
				}
			}
			logger.info("flujo caja");
			SaldoFlujoCaja.ServiceSalida salidaFlujoCaja = new SaldoFlujoCaja.ServiceSalida();
			salidaFlujoCaja.P_OUT_FLUJOCAJA = new ArrayList<SaldoFlujoCaja.ObjFLUJOCAJA>();
			if(entradaSaldoFlujoCaja.P_PORTAFOLIOACONSULTAR.size() > 0){
				salidaFlujoCaja = saldoFlujoCaja.logica(con, entradaSaldoFlujoCaja);
			}
			logger.info("flujo caja");
			
			//LLAMAMOS FLUJO CUSTODIA
			logger.info("flujo custodia");
			ConsultarFlujoCustodiaFondoMutuo flujo = new ConsultarFlujoCustodiaFondoMutuo();
			EntradaConsultarFlujoCustodiaFondoMutuo entradaFlujo = new EntradaConsultarFlujoCustodiaFondoMutuo();
			entradaFlujo.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaFlujo.p_IdUsuario = entrada.p_IdUsuario;
			entradaFlujo.p_IdModulo = entrada.p_IdModulo;
			entradaFlujo.p_IdPais = idPaisEmpresa;
			entradaFlujo.p_FechaSaldo = fechaSistema;
			entradaFlujo.p_DiasProyeccion = 0;
			List<Long> fPortafolio = new ArrayList<Long>();
	        List<String> fRegimen = new ArrayList<String>();
	        
			for(SolicitudAporteRescate sol : solicitudesProcesar){
				SerieFondoSolicitudes serie = (SerieFondoSolicitudes) hseries.get(sol.IdSerieFondo);
				
				if(_c.EstadoPendiente.equalsIgnoreCase(sol.EstadoProceso) 
						&& (_c.TipoSolicitudRescate.equalsIgnoreCase(sol.TipoSolicitud) || (_c.TipoSolicitudAporte.equalsIgnoreCase(sol.TipoSolicitud) && serie.PatrimonioMinimo.compareTo(BigDecimal.ZERO) == 1))){
					if(!fPortafolio.contains(sol.IdPortafolio)){
						fPortafolio.add(sol.IdPortafolio);
						ConsultarFlujoCustodiaFondoMutuo.InCarteras cartera = new ConsultarFlujoCustodiaFondoMutuo.InCarteras();
						cartera.IdCliente = sol.IdCliente;
						cartera.IdPortafolio = sol.IdPortafolio;
						entradaFlujo.p_Carteras.add(cartera);
					}
				
	                if (!fRegimen.contains(serie.IdInstrumento + (sol.IdRegimenTributario.isEmpty() ? null : sol.IdRegimenTributario))) {                 
	                    fRegimen.add(serie.IdInstrumento + (sol.IdRegimenTributario.isEmpty() ? null : sol.IdRegimenTributario));
	            
						ConsultarFlujoCustodiaFondoMutuo.InInstrumento instrumento = new ConsultarFlujoCustodiaFondoMutuo.InInstrumento();
						instrumento.IdInstrumento = serie.IdInstrumento;
						instrumento.IdRegimenFondoMutuo = sol.IdRegimenTributario.isEmpty() ? null : sol.IdRegimenTributario;
						entradaFlujo.p_Instrumentos.add(instrumento);
					}
				}
			}
			
			SalidaConsultarFlujoCustodiaFondoMutuo salidaFlujo = new SalidaConsultarFlujoCustodiaFondoMutuo();
			
			if(entradaFlujo.p_Carteras.size() > 0){
				salidaFlujo = flujo.logica(con, entradaFlujo);
			}
			
			logger.info("flujo custodia");
			//TESORERIA
			ProcesarIngresoEgresoTesoreria procesarTesoreria = new ProcesarIngresoEgresoTesoreria();
			EntradaProcesarIngresoEgresoTesoreria entradaprocesarTesoreria = new EntradaProcesarIngresoEgresoTesoreria();
			
			//AGF
			CrearSolicitudAGF crearSolicitudAGF = new CrearSolicitudAGF();
			EntradaCrearSolicitudAGF entradaCrearAGF = new EntradaCrearSolicitudAGF();
			
			//SPOT
			ProcesarIngresoOperacionSpot procesarSpot = new  ProcesarIngresoOperacionSpot();
			EntradaProcesarIngresoOperacionSpot entradaprocesarSpot= new EntradaProcesarIngresoOperacionSpot();
			
			//SE VALIDA CUSTODIA Y FLUJO CAJA, SI HAY SE INSERTA
			logger.debug("Solicitudes a procesar:" + solicitudesProcesar.size());
			
			logger.trace("PACAPORTES:" + entrada.p_SolicitudesAporteRescate);
			logger.trace("PACFORMASDEPAGO:" + entrada.p_FormaPagoSpot);
			
			ArrayList<Long> portafoliosABuscar = new ArrayList<>();
			
			solicitudesProcesar.forEach(soli -> {
				Long portafolio = soli.IdPortafolio;
				portafoliosABuscar.add(portafolio);
			});
			
			HashMap<String,Object> hashListadoPortafolios = new HashMap<String,Object>(1);
			hashListadoPortafolios.put("IdPortafolio", portafoliosABuscar);	
			
			List<ParametroOperaCSN> parametrosOPCSN = sql.query(con, SQL_SELECT_PARAMETRO_OPERA_CON_SALDO_NEGATIVO, hashListadoPortafolios, ParametroOperaCSN.class);
			
			for(SolicitudAporteRescate sol : solicitudesProcesar){
				if(_c.EstadoRechazado.equalsIgnoreCase(sol.EstadoProceso)) continue;
				SerieFondoSolicitudes serie = (SerieFondoSolicitudes) hseries.get(sol.IdSerieFondo);
				BigDecimal custodia = BigDecimal.ZERO;
				BigDecimal monto =  BigDecimal.ZERO;
				for(ConsultarFlujoCustodiaFondoMutuo.FlujoFondoMutuoTotalizado flujoCustodia : salidaFlujo.P_OUT_FLUJOFONDOMUTUOTOTALIZADO){
					if(FondoMutuoUtils.esFlujoCustodiaParaSolicitudProceso(flujoCustodia, sol)){
						custodia = flujoCustodia.CantidadDisponible;
						monto = flujoCustodia.CantidadDisponible.multiply(sol.ValorCuota, MathContext.DECIMAL128);
						break;
					}
				}
				
				//VALIDAMOS CUSTODIA, SI NO HAY ERROR SALIMOS
	 			if(_c.TipoSolicitudRescate.equalsIgnoreCase(sol.TipoSolicitud) && sol.NumeroCuotas.compareTo(custodia) == 1){
					sol.EstadoProceso = _c.EstadoRechazado;
					out.P_OUT_ERRORESCONTADOR++;
					HashMap<String,String> parametrosMensaje = new HashMap<String,String>();
					parametrosMensaje.put("Nemotecnico", sol.IdSerieFondo);
					parametrosMensaje.put("CantidadRetiro", sol.NumeroCuotas.toPlainString());
					parametrosMensaje.put("CantidadCustodia",custodia.toPlainString());
					out.P_OUT_MENSAJES.add(new MensajeSalida(sol.IdInterno,MensajeSalida.MENSAJE_ERROR,"UI_ERROR_FM_CLIENTE_SIN_CUSTODIA",parametrosMensaje,con,entrada.p_IdEmpresa));
					continue;
				}
	 			
				if(_c.TipoSolicitudAporte.equalsIgnoreCase(sol.TipoSolicitud) && serie.PatrimonioMinimo.compareTo(sol.Monto.add(monto, MathContext.DECIMAL128)) == 1 && entrada.p_IdOrigenProceso != "PAC" && !_c.OrigenBonificacion.equalsIgnoreCase(entrada.p_IdOrigenProceso)  && sol.TipoContratoOperacion != _c.SAAPV){
					sol.EstadoProceso = _c.EstadoRechazado;
					out.P_OUT_ERRORESCONTADOR++;
					out.P_OUT_MENSAJES.add(new MensajeSalida(sol.IdInterno,MensajeSalida.MENSAJE_ERROR,"UI_PATRIMONIO_MINIMO_INSUFICIENTE",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
					continue;
				}
				
				//VALIDAMOS CUENTA INVERSION, SI NO HAY ERROR SALIMOS
				String id = sol.IdPortafolio.toString() + "_" + sol.IdMonedaPago;
				String fecha = Fecha.stringToSqlDate(sol.FechaAceptacion);
				 
				if(_c.TipoSolicitudAporte.equalsIgnoreCase(sol.TipoSolicitud) 
						&& _c.CuentaInversion.equalsIgnoreCase(sol.IdMedioDePago) && esOrigenProcesosManuales(entrada)){
					
					BigDecimal saldo = BigDecimal.ZERO;
					BigDecimal total = portTotalesMontos.get(id) != null ? portTotalesMontos.get(id) : BigDecimal.ZERO;
					
					for(SaldoFlujoCaja.ObjFLUJOCAJA f : salidaFlujoCaja.P_OUT_FLUJOCAJA){		
						if(f.IdPortafolio.longValue() == sol.IdPortafolio && f.IdMoneda.equalsIgnoreCase(sol.IdMonedaPago) && f.FechaSaldo.equalsIgnoreCase(fecha)){
							saldo = f.Saldo;
						
						}
					}
					
					// CELLINCECT-763 Opera con saldo negativo FFMM:
					ParametroOperaCSN paramOPCSN = new ParametroOperaCSN();
					
					paramOPCSN = parametrosOPCSN.stream().filter(parametro -> parametro.IdEntidad.equals(sol.IdPortafolio)).findFirst().orElse(new ParametroOperaCSN());
					
					if(("N").equalsIgnoreCase(paramOPCSN.ValorIndicador) || !("CORREDORES").equalsIgnoreCase(sol.IngresoDesde)) {
						if(total.compareTo(saldo) == 1 && (_c.NO.equals(sol.SimulaCambioPortafolioEjecutadoOk) ) ){
							ClientePortafolioSolicitudes clientePortafolio = (ClientePortafolioSolicitudes) hclientes.get(sol.IdPortafolio);
							HashMap<String,String> mapMsg = new HashMap<>();
							mapMsg.put("CodigoPortafolio",clientePortafolio.CodigoPortafolio);
							mapMsg.put("DocumentoIdentidad",clientePortafolio.DocumentoIdentidad);
							
							sol.EstadoProceso = _c.EstadoRechazado;
							out.P_OUT_ERRORESCONTADOR++;
							out.P_OUT_MENSAJES.add(new MensajeSalida(sol.IdInterno,MensajeSalida.MENSAJE_ERROR,"UI_SALDO_INSUFICIENTE_FLUJO_CAJA",mapMsg,con,entrada.p_IdEmpresa));
							continue;
						}
						
					}
				}
				
				//APORTES: VALIDAMOS GENERACION DE MOVIMIENTOS (TESORERIA/SPOT)    
				if(_c.TipoSolicitudAporte.equalsIgnoreCase(sol.TipoSolicitud)){
					
					String omitirTesoreria=_c.NO;
					
					if(_c.CuentaInversion.equalsIgnoreCase(sol.IdMedioDePago)){
						omitirTesoreria=_c.SI;
					};
					
					//MUEVE TESORERIA
					if(_c.NO.equalsIgnoreCase(omitirTesoreria)){entradaprocesarTesoreria.p_Solicitudes.add(sol);};
				   
					
				   //MUEVE SPOT
					if(_c.SI.equalsIgnoreCase(sol.GeneraSpot)){
						sol.Estado =  _c.EstadoPendiente;
						entradaprocesarSpot.p_DatosOperacion.add(sol);
					    Integer contPag=0;
						for(InFormaPagoSpot pagos  : entrada.p_FormaPagoSpot){
							FormaPagoSpot pag = new FormaPagoSpot();
							
							if (pagos.IdOperacion == null) {
								logger.debug("PAGONULL.PAGO" + pagos);
								logger.debug("PAGONULL.SOL" + sol);
							}
							
							if (sol.IdInterno == null) {
								logger.debug("SOLNULL." + sol);
							}
							
							if(pagos.IdOperacion.equals(sol.IdInterno)){
								pag.AfectarTesoreria=pagos.AfectarTesoreria;
								pag.IdComprobantePagoOperacion=pagos.IdComprobantePagoOperacion;
								pag.IdFormaDePago=pagos.IdFormaDePago;
								pag.IdInternoPagoOperacion=pagos.IdInternoPagoOperacion;
								pag.IdMoneda=pagos.IdMoneda;
								pag.IdMonedaLiquidacion=pagos.IdMonedaLiquidacion;
								pag.IdOperacion=pagos.IdOperacion;
								pag.IdTipoMovimiento=pagos.IdTipoMovimiento;
								pag.MontoFormaDePago=pagos.MontoFormaDePago;
								pag.RecibePagaPortafolio=pagos.RecibePagaPortafolio;
								if(_c.TipoSolicitudAporte.equalsIgnoreCase(sol.TipoSolicitud))
									pag.FechaLiquidacion=sol.FechaAceptacion;
								else
									pag.FechaLiquidacion=sol.FechaLiquidacion;
								entradaprocesarSpot.p_DatosFormaPago.add(pag);
								contPag++;
							};
						};
					   
					
						
				   };
				   
					
				};//fin valida Aportes
				
			}//fin valida custodia y caja
			logger.debug("fin procesar solicitudes a procesar");
			
			List<SolicitudAporteRescate> solicitudesInsertar = new ArrayList<SolicitudAporteRescate>();
			if(!_c.SI.equalsIgnoreCase(entrada.p_SimularOperacion)){
				if(entradaprocesarTesoreria.p_Solicitudes.size() > 0){
					logger.debug("Solicitudes Tesoreria insertar:" + entradaprocesarTesoreria.p_Solicitudes.size());
					entradaprocesarTesoreria.p_IdEmpresa = entrada.p_IdEmpresa;
					entradaprocesarTesoreria.p_IdUsuario = entrada.p_IdUsuario;
					entradaprocesarTesoreria.p_IdModulo = entrada.p_IdModulo;
					entradaprocesarTesoreria.p_SimularOperacion = entrada.p_SimularOperacion;
					entradaprocesarTesoreria.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;		
					entradaprocesarTesoreria.p_EsIngresoAutomatico = _c.NO;
					logger.info("teso");
					SalidaProcesarIngresoEgresoTesoreria salidaTesoreria = procesarTesoreria.logica(con, entradaprocesarTesoreria);
					logger.info("teso");
					for(MensajeSalida rs : salidaTesoreria.P_OUT_MENSAJES){
						if(MensajeSalida.MENSAJE_ERROR.equalsIgnoreCase(rs.tipo)){
							out.P_OUT_ERRORESCONTADOR += 1L;
							out.P_OUT_MENSAJES.add(new MensajeSalida(0L, rs.tipo, rs.codigo, _c.ModuloTesoreria + " : " + rs.descripcion));
							//Si tiene errores, rechaza Aportes
							for(SolicitudAporteRescate sol : solicitudesProcesar){
									sol.EstadoProceso = _c.EstadoRechazado;
									break;
							}
							
						}
					}
					
					for(ResultadoProceso_Salida rs : salidaTesoreria.P_OUT_RESULTADOPROCESO){
						if(_c.EstadoRechazadoTesoreria.equalsIgnoreCase(rs.Resultado)){
							for(SolicitudAporteRescate sol : solicitudesProcesar){
								
								if(rs.Id.longValue() == sol.IdInterno.longValue() && rs.Campo.equalsIgnoreCase(_c.NumeroReferenciaTesoreria)){
									sol.EstadoProceso = _c.EstadoRechazado;
									break;
								}
								
							}
						}
					}
					for(IngresoEgresoGenerado_Salida rs : salidaTesoreria.P_OUT_INGRESOEGRESOGENERADO){
						for(SolicitudAporteRescate sol : solicitudesProcesar){
							
							if(rs.NumeroReferencia.longValue() == sol.IdInterno.longValue()){
								sol.IdIngresoEgreso =rs.IdIngresoEgreso;
								break;
							}
							
						}
						
					}
				}
	
				if(entradaprocesarSpot.p_DatosOperacion.size() > 0){
					entradaprocesarSpot.p_IdEmpresa = entrada.p_IdEmpresa;
					entradaprocesarSpot.p_IdUsuario = entrada.p_IdUsuario;
					entradaprocesarSpot.p_IdModulo = entrada.p_IdModulo;
					entradaprocesarSpot.p_CalculaMargen = _c.SpotCalculaMargen;
					entradaprocesarSpot.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
					entradaprocesarSpot.p_PermiteOperarProspectos = entrada.p_PermiteOperarProspectos;
					logger.info("spot");
					SalidaProcesarIngresoOperacionSpot salidaSpot = procesarSpot.logica(con, entradaprocesarSpot);
					logger.info("spot");
					
					for(OperacionObjOut rs : salidaSpot.P_OUT_IDOPERACIONSPOT){
						
						for(SolicitudAporteRescate sol : solicitudesProcesar){
							if((Long.parseLong(rs.IdInterno) == sol.IdInterno) ){
								if(_c.SpotEstadoRechazado.equalsIgnoreCase(rs.Estado)){			
									sol.EstadoProceso = _c.EstadoRechazado;
									break;
								}else{
									sol.IdOperacionSpot = rs.IdOperacionSpot;
									break;
								}
							}
						}
					}
					
					for(MensajeSalida rs : salidaSpot.P_OUT_MENSAJES){
						if(_c.SpotError.equalsIgnoreCase(rs.tipo)){
							out.P_OUT_ERRORESCONTADOR += 1L;
							out.P_OUT_MENSAJES.add(new MensajeSalida(rs.id, rs.tipo, rs.codigo,  _c.ModuloSpot + " : " + rs.descripcion));
							//Si tiene errores, rechaza Aportes
							for(SolicitudAporteRescate sol : solicitudesProcesar){
									sol.EstadoProceso = _c.EstadoRechazado;
									break;
							}
							
						}
					}
					
				}
				
	
				
				InscribirProcesoBatch procesoBatch = new InscribirProcesoBatch();
				InscribirProcesoBatchEntrada entradaProcesoBatch = new InscribirProcesoBatchEntrada();
				List<Parametro> solicitudesBonificacionFiscalCrear = new ArrayList<Parametro>();
				//VALIDAMOS SOLICITUDES CORRECTAS A INSERTAR
				for(SolicitudAporteRescate sol : solicitudesProcesar){
					if(_c.EstadoPendiente.equalsIgnoreCase(sol.EstadoProceso)){
						if(_c.NO.equalsIgnoreCase(sol.SolicitudBatch)){
							//AGF
							CrearSolicitudAGF.Solicitudes solAGF = new CrearSolicitudAGF.Solicitudes();
							solAGF.IdSolicitudAporteRescate = sol.IdSolicitudAporteRescate;
							entradaCrearAGF.p_Solicitudes.add(solAGF);
						}
						solicitudesInsertar.add(sol);
						out.P_OUT_RESULTADO.add((ResultadoSolicitud) hSolicitudSalida.get(sol.IdInterno));
						PortafolioAPV papv = (PortafolioAPV) hportafolioAPV.get(sol.IdPortafolio);
						TraspasoAPV traspaso = hTraspasoAPV.get(sol.FolioContrato);
						if(_c.TipoSolicitudRescate.equals(sol.TipoSolicitud) && papv != null && traspaso == null){
							solicitudesBonificacionFiscalCrear.add(new Parametro(0L,"IdSolicitudAporteRescate",sol.IdSolicitudAporteRescate.toString(), null));
						}
					}
				}
				//INSERTAMOS LAS SIN ERRORES
				if(solicitudesInsertar.size() > 0){
					sql.batch(con, SQL_INSERT_SOLICITUD, solicitudesInsertar, SolicitudAporteRescate.class);
					//AGF
					if(entradaCrearAGF.p_Solicitudes.size() > 0){
						entradaCrearAGF.p_IdEmpresa = entrada.p_IdEmpresa;
						entradaCrearAGF.p_IdUsuario = entrada.p_IdUsuario;
						entradaCrearAGF.p_IdModulo = entrada.p_IdModulo;
						entradaCrearAGF.p_EsManual = _c.NO;
						logger.info("agf");
						SalidaCrearSolicitudAGF salidaAGF = crearSolicitudAGF.logica(con, entradaCrearAGF);
						logger.info("agf");
						out.P_OUT_ERRORESCONTADOR += salidaAGF.P_OUT_ERRORESCONTADOR;
						out.P_OUT_MENSAJES.addAll(salidaAGF.P_OUT_MENSAJES);
					}
					
					
					if (solicitudesBonificacionFiscalCrear.size() > 0 && !entrada.p_IdOrigenProceso.equalsIgnoreCase("PAC")) {
						entradaProcesoBatch.p_Parametro = new ArrayList<Parametro>();
						InTipoPrograma inTipoPrograma = new InTipoPrograma();
						entradaProcesoBatch.p_TipoPrograma = new ArrayList<InTipoPrograma>();
						inTipoPrograma.IdInterno = 0L;
						inTipoPrograma.IdTipoPrograma = "INGRESAR_ORDEN_FFMM_BONIFICACION_FISCAL";
						inTipoPrograma.SegundosDesfase = 0L;
						entradaProcesoBatch.p_TipoPrograma.add(inTipoPrograma);
						entradaProcesoBatch.p_Parametro.add(new Parametro(0L,"IdEmpresa",entrada.p_IdEmpresa.toString(), null));
						entradaProcesoBatch.p_Parametro.add(new Parametro(0L,"IdUsuario",entrada.p_IdUsuario.toString(), null));				
						
						for(Parametro solicitudBonificacionFiscalCrear : solicitudesBonificacionFiscalCrear) {
							entradaProcesoBatch.p_Parametro.add(new Parametro(0L,solicitudBonificacionFiscalCrear.NombreParametro, solicitudBonificacionFiscalCrear.Valor, null));
						}									
						
						entradaProcesoBatch.p_IdEmpresa = entrada.p_IdEmpresa;
						entradaProcesoBatch.p_IdUsuario = entrada.p_IdUsuario;
						entradaProcesoBatch.p_IdModulo = entrada.p_IdModulo;	
						procesoBatch.logica(con, entradaProcesoBatch);
				    }
					//AGREGAR LOG
					{
						AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
						for (SolicitudAporteRescate sol : solicitudesInsertar) {
							ClientePortafolioSolicitudes cliente = (ClientePortafolioSolicitudes) hclientes.get(sol.IdPortafolio);
							AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
							log.IdCabeceraLog = sol.IdSolicitudAporteRescate;
							log.IdTipoLog = "INGRESO";
							log.IdModulo = _c.ModuloFondoMutuo;
							log.IdUsuario = entrada.p_IdUsuario;
							log.IdEmpresa = entrada.p_IdEmpresa;
							log.FechaSistemaLog = fechaSistema;
							log.IdUsuarioAtendido = entrada.p_IdUsuario;
							log.IdCliente = sol.IdCliente;
							log.EstadoCliente = cliente.IdEstadoCliente;
							log.NombreCliente = cliente.NombreCompleto;
							log.IdTipoDocumentoIdentidad = cliente.TipoDocumentoIdentidad;
							log.IdDocumentoIdentidad = cliente.DocumentoIdentidad;
							log.IdPortafolio = sol.IdPortafolio;
							log.EstadoPortafolio = cliente.IdEstadoPortafolio;
							log.IdTipoRegistro = "SOLICITUD_APORTE_RESCATE";
							log.EstadoRegistro = sol.Estado;
							log.IdTipoOperacion = sol.IdTipoMovimientoFondoMutuo;
							log.Folio = sol.IdSolicitudAporteRescate;
							log.Cantidad = sol.NumeroCuotas;
							log.PrecioTasa = sol.ValorCuota;
							log.Monto = sol.Monto;
							log.IdMonedaOrigen = sol.IdMoneda;
							log.IdMonedaLiquidacion = sol.IdMoneda;
							log.TipoCambio = sol.ParidadFechaInversion.doubleValue()+"";
							log.Nemo = sol.IdSerieFondo;
							log.IdProceso = 0L;
							log.EstadoProceso = "";
							log.NombreProceso = "";						
							entradaAgregarLog.p_Log.add(log);
						}
						AgregarLog agregarLogService = new AgregarLog();
						agregarLogService.logica(con, entradaAgregarLog);
					}
				}
			}else{
				for(SolicitudAporteRescate sol : solicitudesProcesar){
					if(_c.EstadoPendiente.equalsIgnoreCase(sol.EstadoProceso)){
						out.P_OUT_RESULTADO.add((ResultadoSolicitud) hSolicitudSalida.get(sol.IdInterno));
					}
				}
			}
			
			//COMISION NN
			CrearSolicitudRescateComisionNN crearSolicitudRescateComisionNN = new CrearSolicitudRescateComisionNN();
			CrearSolicitudRescateComisionNN.EntradaCrearSolicitudRescateComisionNN entradaCrearSolicitudRescateComisionNN = new CrearSolicitudRescateComisionNN.EntradaCrearSolicitudRescateComisionNN();
			
			Long iNN = 1L;
			
			entradaCrearSolicitudRescateComisionNN.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaCrearSolicitudRescateComisionNN.p_IdModulo = entrada.p_IdModulo;
			entradaCrearSolicitudRescateComisionNN.p_IdTipoCanalIngreso = "";
			entradaCrearSolicitudRescateComisionNN.p_IdUsuario = entrada.p_IdUsuario;
			entradaCrearSolicitudRescateComisionNN.p_SimularOperacion = "N";
			entradaCrearSolicitudRescateComisionNN.p_UsaReglasNegocio = "S";
			
			for (SolicitudAporteRescate sal : solicitudesProcesar) {
				ResultadoCalcularComision comision = (ResultadoCalcularComision) hcomisiones.get(sal.IdInterno);
				if(comision != null && comision.Comision.compareTo(BigDecimal.ZERO) > 0 && comision.TipoComision != null && comision.TipoComision.equalsIgnoreCase(_c.TipoComisionTrimestral) && comision.CantidadComision.compareTo(BigDecimal.ZERO) > 0 && _c.TipoSolicitudRescate.equals(sal.TipoSolicitud)) {
					CrearSolicitudRescateComisionNN.InSolicitudesRescate inSolicitudesRescate = new CrearSolicitudRescateComisionNN.InSolicitudesRescate();
					inSolicitudesRescate.FolioContrato = 0L;
					inSolicitudesRescate.IdCliente = sal.IdCliente;
					inSolicitudesRescate.IdInterno = iNN++;
					inSolicitudesRescate.IdMoneda = sal.IdMoneda;
					inSolicitudesRescate.IdPortafolio = sal.IdPortafolio;
					inSolicitudesRescate.IdRegimenTributario = sal.IdRegimenTributario;
					inSolicitudesRescate.IdSerieFondo = sal.IdSerieFondo;
					inSolicitudesRescate.IngresoAutomatico = "N";
					inSolicitudesRescate.Monto = new BigDecimal(1); //sal.MontoOriginal.multiply(sal.ComisionNN).divide(sal.CuotasCliente);
					inSolicitudesRescate.NumeroCuotas = comision.CantidadComision;
					inSolicitudesRescate.FechaValorCuota = sal.FechaValorCuota;
					inSolicitudesRescate.FolioCartera = 0L;
					inSolicitudesRescate.IdSolicitudAporteRescateRelacionado = sal.IdSolicitudAporteRescate;
					inSolicitudesRescate.IdMonedaPago = sal.IdMonedaPago;
					entradaCrearSolicitudRescateComisionNN.p_SolicitudesRescate.add(inSolicitudesRescate);
				}
			}
			
	 		if(!entradaCrearSolicitudRescateComisionNN.p_SolicitudesRescate.isEmpty()) {
	 			CrearSolicitudRescateComisionNN.SalidaCrearSolicitudRescateComisionNN salida = crearSolicitudRescateComisionNN.logica(con, entradaCrearSolicitudRescateComisionNN);
	 			
	 			if(salida.P_OUT_ERRORESCONTADOR > 0L) {
	 				out.P_OUT_MENSAJES.addAll(salida.P_OUT_MENSAJES);
	 				out.P_OUT_ERRORESCONTADOR = out.P_OUT_MENSAJES.stream().filter(mensaje -> MensajeSalida.MENSAJE_ERROR.equals(mensaje.tipo)).count();
	 				eliminarBloqueoSolicitudAporteRescate(con, entrada.p_SimularOperacion, entrada.p_IdEmpresa, solicitudesBloqueadas, out.P_OUT_MENSAJES, out.P_OUT_ERRORESCONTADOR, sql);
	 				return out;
	 			}
	 			
	 			if(!salida.P_OUT_RESULTADO.isEmpty()) {
	 				out.P_OUT_RESULTADO.addAll(salida.P_OUT_RESULTADO);
	 			}
	 		}
			
			
			//Notificacion OpenWorkPlace
			logger.info("owp");
			String notificaOWP = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.NotificarEstadoOrdenOWP");
			if("S".equalsIgnoreCase(notificaOWP) && "OWP".equalsIgnoreCase(entrada.p_IdTipoCanalIngreso)){
				List<Long> idSolicitudes = new ArrayList<Long>();
				
				for(SolicitudAporteRescate in : solicitudesInsertar){
					if(!idSolicitudes.contains(in.IdSolicitudAporteRescate)) 
						idSolicitudes.add(in.IdSolicitudAporteRescate);
				}
				
				HashMap<String,Object> parametros = new HashMap<String,Object>();
				parametros.put("IdEmpresa", entrada.p_IdEmpresa);
				parametros.put("IdSolicitudAporteRescate", idSolicitudes);
				logger.debug("INICIO::SQL_SELECT_SOLICITUDES");
				List<SolicitudAporteRescate> solicitudesModificadas = sql.query(con, SQL_SELECT_SOLICITUDES, parametros, SolicitudAporteRescate.class);
				logger.debug("FIN::SQL_SELECT_SOLICITUDES");
				NotificarEstadoOrdenesOWP servicioNotOwp = new NotificarEstadoOrdenesOWP();	
				List<NotificarEstadoOrdenes_Entrada> listSolicitudesNotOWP = new ArrayList<NotificarEstadoOrdenes_Entrada>();
				
				for (SolicitudAporteRescate solicitud : solicitudesModificadas) {
					NotificarEstadoOrdenes_Entrada entradaSolicitud = new NotificarEstadoOrdenes_Entrada();
					entradaSolicitud.IdEmpresa = entrada.p_IdEmpresa;
					entradaSolicitud.IdUsuario = entrada.p_IdUsuario;
					entradaSolicitud.Solsec = solicitud.IdSolicitudAporteRescate;
					entradaSolicitud.NTitulos = solicitud.NumeroCuotas;
					entradaSolicitud.Precio = solicitud.ValorCuota;
					entradaSolicitud.Fecha = solicitud.FechaAceptacion.toString();
					entradaSolicitud.Estado = solicitud.Estado; 
					entradaSolicitud.Comision = solicitud.MontoComision;
					entradaSolicitud.Gastos = new BigDecimal(0);
					entradaSolicitud.Mercado = "F"; 
					entradaSolicitud.Divisa = solicitud.IdMoneda;
					entradaSolicitud.TipoCambio = solicitud.CostoMonedaLocal;
					entradaSolicitud.SolsecAsoc = solicitud.IdSolicitudAporteRescateRelacionado.toString();
					entradaSolicitud.Nemo = solicitud.IdSerieFondo;
					entradaSolicitud.IdClasificacionTipoInstrumento = "FM";
					listSolicitudesNotOWP.add(entradaSolicitud);
				}
				
				if(listSolicitudesNotOWP.size() > 0){
					servicioNotOwp.go(con, listSolicitudesNotOWP);
				}
			}
			logger.info("owp");
			eliminarBloqueoSolicitudAporteRescate(con, entrada.p_SimularOperacion, entrada.p_IdEmpresa, solicitudesBloqueadas, out.P_OUT_MENSAJES, out.P_OUT_ERRORESCONTADOR, sql);
			logger.info("FIN::CrearSolicitudAporteRescateNormal");
			return out;
		} catch (Exception e) {
			eliminarBloqueoSolicitudAporteRescate(con, entrada.p_SimularOperacion, entrada.p_IdEmpresa, solicitudesBloqueadas, out.P_OUT_MENSAJES, out.P_OUT_ERRORESCONTADOR, sql);
			e.printStackTrace();
			throw e;
		}		
	}
	
	private Boolean esValidacionPatrimonio(EntradaCrearSolicitudAporteRescateNormal entradaCrearAporteRescate, EntradaValidaPatrimonioEmisor entradaValidaPatrimonioEmisor) {
		logger.info("INICIO::CrearSolicitudAporteRescateNormal.esValidacionPatrimonio");
		Boolean esValidacionPatrimonio = entradaValidaPatrimonioEmisor.p_Solicitudes.size() > 0 && esOrigenProcesosManuales(entradaCrearAporteRescate);
		logger.info("FIN::CrearSolicitudAporteRescateNormal.esValidacionPatrimonio");
		return esValidacionPatrimonio;
	}
	
	private Boolean esOrigenProcesosManuales(EntradaCrearSolicitudAporteRescateNormal entradaCrearAporteRescate) {
		logger.trace("INICIO::CrearSolicitudAporteRescateNormal.esOrigenProcesosManuales");
		Boolean esOrigenProcesosAutomaticos = !_c.OrigenPAC.equals(entradaCrearAporteRescate.p_IdOrigenProceso) && !_c.OrigenRecaudacion.equals(entradaCrearAporteRescate.p_IdOrigenProceso)  && !_c.OrigenBonificacion.equals(entradaCrearAporteRescate.p_IdOrigenProceso);
		logger.trace("FIN::CrearSolicitudAporteRescateNormal.esOrigenProcesosManuales");
		return esOrigenProcesosAutomaticos;
	}
	
	private Boolean aplicaBloqueoSolicitud(EntradaCrearSolicitudAporteRescateNormal entradaCrearAporteRescate) {
		logger.trace("INICIO::CrearSolicitudAporteRescateNormal.aplicaBloqueoSolicitud");
		String origenProceso = entradaCrearAporteRescate.p_IdOrigenProceso;
		Boolean aplicaBloqueoSolicitud = esOrigenProcesosManuales(entradaCrearAporteRescate) && !_c.OrigenCargaMasivaFM.equalsIgnoreCase(origenProceso) && !_c.CSC.equalsIgnoreCase(origenProceso);
		logger.trace("FIN::CrearSolicitudAporteRescateNormal.aplicaBloqueoSolicitud");
		return aplicaBloqueoSolicitud;
	}
	
	private String obtenerRescateGeneraMovimientoSpot(Connection con, Long idEmpresa) throws Exception {
		logger.trace("INICIO::CertificarSolicitudAporteRescate.obtenerRescateGeneraMovimientoSpot");
		String rescateGeneraMovimientoSpot = ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "FONDOMUTUO", "RESCATE_GENERA_VENTA_SPOT_DOLAR");
		logger.trace("FIN::CertificarSolicitudAporteRescate.obtenerRescateGeneraMovimientoSpot");
		return rescateGeneraMovimientoSpot;
	}
	
	private void eliminarBloqueoSolicitudAporteRescate(Connection con, String simularOperacion, Long idEmpresa, List<BloqueoSolicitudAporteRescate> solicitudesBloqueadas, List<MensajeSalida> P_OUT_MENSAJES, Long P_OUT_ERRORESCONTADOR, SQL sql) throws Exception {
		if (!_c.SI.equalsIgnoreCase(simularOperacion) && solicitudesBloqueadas.size() > 0) {
			MensajeSalida objMensaje = new MensajeSalida();
			try {
				HashMap <String, Object> params = new HashMap<String, Object>();
				params.put("p_BloqueoSolicitudAporteRescate", solicitudesBloqueadas);
				logger.debug("INICIO::SQL_CALL_ELIMINAR_BLOQUEO_SOLICITUD");
				sql.call(con, SQL_CALL_ELIMINAR_BLOQUEO_SOLICITUD, params, new HashMap<String, Class>());
				logger.debug("FIN::SQL_CALL_ELIMINAR_BLOQUEO_SOLICITUD");
			} catch(SQLException sqle) {
				logger.error(sqle);
				objMensaje = new MensajeSalida(1L, MensajeSalida.MENSAJE_ERROR, "UI_ERROR_DELETE_BLOQUEO_SOLICITUD",new HashMap<String, String>(),con, idEmpresa);
				P_OUT_MENSAJES.add(objMensaje);
			} finally {
				SQL.exec(con, SQL_DROP_TABLE_BLOQUEO_SOLICITUD);
			}
		}
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
		Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();			
		
		String Json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":1,\"p_IdModulo\":\"FMM\",\"p_simularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"S\",\"p_SolicitudesAporteRescate\":[{\"IdInterno\":1,\"IdCliente\":61625,\"IdPortafolio\":206349,\"TipoSolicitud\":\"RES\",\"IdSerieFondo\":\"00068ACLP\",\"CalculoPorMontoCuota\":\"T\",\"EsPorMontoLiquido\":\"N\",\"Monto\":573586,\"NumeroCuotas\":408.6032,\"IdMoneda\":\"CLP\",\"IdMedioDePago\":\"CIN\",\"IdPreferenciaPagoPortafolio\":0,\"Visado\":\"N\",\"SolicitudBatch\":\"N\",\"OrigenOperacion\":\"M\",\"FechaOperacion\":\"2024-07-04\",\"IdRegimenTributario\":\"18Q\",\"Glosa\":\"\",\"IndicadorMLT\":\"N\",\"IdSerieFondoDestino\":\"\",\"IdEmisorDestino\":\"\",\"IdAutorizadoOrigen\":0,\"Porcentaje\":0,\"FolioContrato\":0,\"IdMonedaPago\":\"\"}]}";		
		EntradaCrearSolicitudAporteRescateNormal Objin = gson.fromJson(Json, EntradaCrearSolicitudAporteRescateNormal.class);
		CrearSolicitudAporteRescateNormal  demo=new CrearSolicitudAporteRescateNormal();				
		try {
			SalidaCrearSolicitudAporteRescateNormal ObjOut=demo.logica(con, Objin);
			con.rollback();
			System.out.println(ObjOut.toString());
		}
		catch(Exception er) {
			er.printStackTrace();	
			con.rollback();
		}
						
    	}
		System.out.println("== END ==");
	}

}