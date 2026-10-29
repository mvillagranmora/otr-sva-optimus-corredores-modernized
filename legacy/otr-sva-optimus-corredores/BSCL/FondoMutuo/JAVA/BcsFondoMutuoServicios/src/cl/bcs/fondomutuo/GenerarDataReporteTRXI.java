package cl.bcs.fondomutuo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.fondomutuo.pojo.ConfiguracionEmisorOmnibus;
import cl.bcs.fondomutuo.pojo.ConfiguracionOmnibus;
import cl.bcs.fondomutuo.pojo.ConstantesFondoMutuo;
import cl.bcs.fondomutuo.pojo.Omnibus;
import cl.bcs.fondomutuo.pojo.OmnibusDetalle;
import cl.bcs.fondomutuo.pojo.SerieFondoSolicitudes;
import cl.bcs.plataforma.pla.InsertarLogServicio;
import cl.bcs.plataforma.pla.InsertarLogServicio.RegistroLog;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.MonedaFormato;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class GenerarDataReporteTRXI implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(GenerarDataReporteTRXI.class);
	
	private final String SQL_SELECT_SERIE_FONDO = loadResourceToString("cl/bcs/fondomutuo/sql/ComunesSolicitudes_SelectSerieFondo.sql");
	private final String SQL_SELECT_APORTE_RESCATE_CABECERA_NACIONAL = loadResourceToString("cl/bcs/fondomutuo/sql/GenerarDataReporteTRXI_SelectAporteRescateCabeceraNacional.sql");
	private final String SQL_SELECT_APORTE_RESCATE_CABECERA_INTERNACIONAL = loadResourceToString("cl/bcs/fondomutuo/sql/GenerarDataReporteTRXI_SelectAporteRescateCabeceraInternacional.sql");
	private final String SQL_SELECT_APORTE_RESCATE_CABECERA_INTERNACIONAL_SEPARADO = loadResourceToString("cl/bcs/fondomutuo/sql/GenerarDataReporteTRXI_SelectAporteRescateCabeceraInternacionalSeparado.sql");
	private final String SQL_SELECT_SOLICITUDES = loadResourceToString("cl/bcs/fondomutuo/sql/GenerarDataReporteTRXI_SelectSolicitudes.sql");
	private final String SQL_SELECT_APORTE_RESCATE = loadResourceToString("cl/bcs/fondomutuo/sql/GenerarDataReporteTRXI_SelectAporteRescate.sql");
	private final String SQL_SELECT_CONFIGURACION_EMISOR_OMNIBUS = loadResourceToString("cl/bcs/fondomutuo/sql/GenerarDataReporteTRXI_SelectConfiguracionEmisorOmnibus.sql");
	private final String SQL_SELECT_CONFIGURACION_OMNIBUS = loadResourceToString("cl/bcs/fondomutuo/sql/GenerarDataReporteTRXI_SelectConfiguracionOmnibus.sql");
	private final String SQL_INSERT_CABECERA = loadResourceToString("cl/bcs/plataforma/ftp/sql/FTP_InsertTableCabecera.sql");
	private final String SQL_INSERT_DETALLE = loadResourceToString("cl/bcs/fondomutuo/sql/FTP_InsertTableDetalle.sql");
	private final String SQL_INSERT_OMNIBUS = loadResourceToString("cl/bcs/fondomutuo/sql/GenerarDataReporteTRXI_InsertOmnibus.sql");
	private final String SQL_INSERT_OMNIBUS_DETALLE = loadResourceToString("cl/bcs/fondomutuo/sql/GenerarDataReporteTRXI_InsertOmnibusDetalle.sql");
	private final String SQL_UPDATE_SOLICITUD_APORTE_RESCATE = loadResourceToString("cl/bcs/fondomutuo/sql/GenerarDataReporteTRXI_UpdateSolicitudAporteRescate.sql");
	private final String SQL_UPDATE_FOLIO_OPERACION_EMISOR = loadResourceToString("cl/bcs/fondomutuo/sql/GenerarDataReporteTRXI_UpdateFolioOperacionEmisor.sql");
	private final String SQL_SELECT_BLOQUEO_SOLICITUDES = loadResourceToString("cl/bcs/fondomutuo/sql/GenerarDataReporteTRXI_SelectBloqueoSolicitudes.sql");
	
	private final ConstantesFondoMutuo _c = new ConstantesFondoMutuo();
	
	
	
	/***********************************************************************************************
	 *                                        CLASES
	 ***********************************************************************************************/
	
	public static class EntradaReporteTRXI{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		List <Long> p_IdSolicitudAporteRescate;
		
		public EntradaReporteTRXI() {
			this.p_IdSolicitudAporteRescate = new ArrayList<Long>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaReporteTRXI{
		
		public Long P_OUT_ERRORESCONTADOR;
		public List <MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaReporteTRXI(){
			this.P_OUT_ERRORESCONTADOR=0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>(); 
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class CabeceraArchivoTRXI{
		public String TipoCuenta;
		public String TipoCuentaArchivo;
		public String TipoMovimiento;
		public String IdEmisor;
		public String FondoExtranjero;
		public Long NumeroRegistros;
		public BigDecimal Monto;
		public Long Lote;
		public Long TipoTransaccion;
		public String IdMoneda;
		
		public CabeceraArchivoTRXI(){
			this.NumeroRegistros = 0L;
			this.Monto = BigDecimal.ZERO;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	public static class FormatoArchivo{
		public String NombreArchivo;
		public String TipoTransacion; // Solamente I 
		public String Cuenta;
		public String FolioInversion;
		public String FechaArchivo;
		public String HoraArchivo;
		public List <ObjAporteRescate> TrxIAporteRescate;
		
		public String toString() {
			return Utils.printJson(this, false);
		}		
	}
	
	public static class Cabecera {
		public String   NombreArchivo;
		public Long     IdCabecera;
		public String   IdCodigoTipo;
		public String   IdEstado;
		public java.sql.Timestamp FechaCreacion;
		public Long     IdUsuario;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Detalle {
		public Long   IdDetalle;
		public Long   IdCabecera;
		public String Linea;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class ObjAporteRescate{
		public String 		IdIng;
		public String 		Rut;
		public String 		TipoMovimiento;
		public String   	IdInstrumento;
		public String 		NombreFFMM;
		public BigDecimal   Valor;
		public Long   		TipoTransaccion;
		public Long			CantidadOperaciones;
		public Date 		FechaTransaccion;
		public String 		MonedaTransaccion;
		public String	 	TipoCuenta;
		public Long  		Lote; 
		public BigDecimal   ValorCuota;
		public BigDecimal   NumeroDeCuotas;
		public Date 		FechaLiquidez;
		public Long   		IdAdministradora;
		public Long 		Flag;
		public String 		Descripcion;
		public BigDecimal   TipoCambio;
		public BigDecimal   Monto;
		public Date 		FechaCuota;
		public String 		Serie;
		public String		IdEmisor;
		public String		IdSerieFondo;
		public String 		FondoExtranjero;
		public String		ISIN;
		public String		ISINDestino;
		public String toString() {
			return Utils.printJson(this, false);
		}		
	}
		
	public static class Solicitudes{
		public Long IdSolicitudAporteRescate;
		public String IdSerieFondo;
		public String TipoCuenta;
		public String TipoMovimiento;
		public Long TipoTransaccion;
		public Long IdOmnibusDetalle;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class LlaveSolicitudNacional{
		public String TipoCuenta;
		public String TipoMovimiento;
		public String IdEmisor;
		public Long TipoTransaccion;
		public String IdMoneda;
		@Override
		public int hashCode() {
			return 31 * this.TipoCuenta.hashCode() + this.TipoMovimiento.hashCode() + this.IdEmisor.hashCode() + this.TipoTransaccion.hashCode() + this.IdMoneda.hashCode();
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
			    return false;
			if (getClass() != obj.getClass())
			    return false;
			LlaveSolicitudNacional llave = (LlaveSolicitudNacional) obj;
			return this.TipoCuenta.equals(llave.TipoCuenta) && this.TipoMovimiento.equals(llave.TipoMovimiento) && this.IdEmisor.equals(llave.IdEmisor) 
			  && this.TipoTransaccion.equals(llave.TipoTransaccion);
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class LlaveSolicitudInternacional{
		public String IdEmisor;
		public String TipoMovimiento;
		public String IdMoneda;
		
		@Override
		public int hashCode() {
			return 31 * this.TipoMovimiento.hashCode() + this.IdEmisor.hashCode() + this.IdMoneda.hashCode();
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
			    return false;
			if (getClass() != obj.getClass())
			    return false;
			LlaveSolicitudInternacional llave = (LlaveSolicitudInternacional) obj;
			return this.TipoMovimiento.equals(llave.TipoMovimiento) && this.IdEmisor.equals(llave.IdEmisor) && this.IdMoneda.equals(llave.IdMoneda);
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	/***********************************************************************************************
	 *                                       LOGICA
	 ***********************************************************************************************/
	
	public SalidaReporteTRXI logica(Connection con, EntradaReporteTRXI entrada) throws Exception {
		logger.info("INICIO::GenerarDataReporteTRXI");
		
		SalidaReporteTRXI salida = new SalidaReporteTRXI();
		InsertarLogServicio insertarLogService = new InsertarLogServicio();
		
		salida.P_OUT_ERRORESCONTADOR=0L;
		
		String fechaSistema = obtenerFechasistema(con,entrada.p_IdEmpresa);
		String separaOmnibusInternacionalPorTipoMovimiento = obtenerSepararOmnibusInternacionalTipoMovimiento(con,entrada.p_IdEmpresa);
		
		DecimalFormatSymbols simbolos = new DecimalFormatSymbols();
		simbolos.setDecimalSeparator('.');
		DecimalFormat decimalFormat = new DecimalFormat("####.##",simbolos);
		decimalFormat.setMaximumFractionDigits(2);
		decimalFormat.setMinimumFractionDigits(2);
		
		if(entrada.p_IdSolicitudAporteRescate.size()>0){
			//Consulta Aportes Rescates 
			bloqueoSolicitudes(con, entrada.p_IdSolicitudAporteRescate);
			Map<String,ConfiguracionOmnibus> configuracionOmnibus = obtenerConfiguracionOmnibus(con);
			Map<String,ConfiguracionEmisorOmnibus> configuracionesEmisores = obtenerConfiguracionesEmisores(con);
			List<CabeceraArchivoTRXI> listCabeceraArchivoTRXNacional = obtenerCabecerasNacionales(con,entrada.p_IdSolicitudAporteRescate);
			List<CabeceraArchivoTRXI> listCabeceraArchivoTRXInternacional = obtenerCabecerasInternacionales(con,entrada.p_IdSolicitudAporteRescate,separaOmnibusInternacionalPorTipoMovimiento);
			List<ObjAporteRescate> listaAporteRescate = obtenerAportesRescatesAgrupados(con,entrada.p_IdSolicitudAporteRescate);
			List<Solicitudes> listaSolicitudes = obtenerSolicitudes(con,entrada.p_IdSolicitudAporteRescate);
			
			Map<String,SerieFondoSolicitudes> hSeries = obtenerSeriesFondo(con,listaSolicitudes,entrada.p_IdEmpresa);
			
			if(listCabeceraArchivoTRXNacional.size() == 0 && listCabeceraArchivoTRXInternacional.size() == 0){
				salida.P_OUT_ERRORESCONTADOR = 1L;
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_NO_EXISTEN_SOLICITUDES_CONDICIONES_OMNIBUS",new HashMap<String, String>(),con,entrada.p_IdEmpresa));
				return salida;
			}
			
			List<Long> secuenciasCabecera = Utils.getSecuencia(con, "BSCL.Plataforma.BD.Secuencias::Cabecera", Long.valueOf(listCabeceraArchivoTRXNacional.size() + Long.valueOf(listCabeceraArchivoTRXInternacional.size())));
			List<Long> secuenciasDetalle = Utils.getSecuencia(con, "BSCL.Plataforma.BD.Secuencias::Detalle", Long.valueOf(listaAporteRescate.size()) + Long.valueOf(listCabeceraArchivoTRXNacional.size() + Long.valueOf(listCabeceraArchivoTRXInternacional.size())));
			List<Long> secuenciasLote = Utils.getSecuencia(con, "BSCL.FondoMutuo.BD.Secuencias::IdLoteCorredora", Long.valueOf(listCabeceraArchivoTRXNacional.size()) + Long.valueOf(listCabeceraArchivoTRXInternacional.size()));
			List<Long> secuenciasMovimientoCorredora = Utils.getSecuencia(con, "BSCL.FondoMutuo.BD.Secuencias::IdMovimientoCorredora", Long.valueOf(listaAporteRescate.size()));
			
			List<Omnibus> omnibuses = new ArrayList<Omnibus>();
			List<Cabecera> cabeceraList = new ArrayList<Cabecera>();
			List<Detalle> detalleList = new ArrayList<Detalle>();
			List<OmnibusDetalle> omnibusDetalleList = new ArrayList<OmnibusDetalle>();
			
			int indexCabecera = 0;
			int indexDetalle = 0;
			int indexMovimiento = 0;
			
			List<RegistroLog> lstDatosLog = new ArrayList<RegistroLog>();
			
			DateFormat formatDate = new SimpleDateFormat("yyyy-MM-dd");
			
			Map<LlaveSolicitudNacional,List<ObjAporteRescate>> solicitudesNacionalesAgrupadas = obtenerMapSolicitudesNacionalesAgrupadasPorLlave(listaAporteRescate);
			Map<LlaveSolicitudInternacional,List<ObjAporteRescate>> solicitudesInternacionalesAgrupadas = obtenerMapSolicitudesInternacionalesAgrupadasPorLlave(listaAporteRescate,separaOmnibusInternacionalPorTipoMovimiento);
			
			for(CabeceraArchivoTRXI cabecera : listCabeceraArchivoTRXNacional) {
				List<SerieFondoSolicitudes> seriesCabecera = new ArrayList<SerieFondoSolicitudes>();
				LlaveSolicitudNacional llave = crearLlaveSolicitudNacional(cabecera);
				List<ObjAporteRescate> solicitudesNacionalesLlave = solicitudesNacionalesAgrupadas.get(llave);
				
				cabecera.Lote = secuenciasLote.get(indexCabecera);
				cabecera.NumeroRegistros = solicitudesNacionalesLlave != null ? Long.valueOf(solicitudesNacionalesLlave.size()) : 0L;
				
				Omnibus omnibus = crearOmnibus(cabecera.Lote, fechaSistema);
				omnibuses.add(omnibus);
				
				Long idCabecera = secuenciasCabecera.get(indexCabecera);
				Cabecera objCabecera = crearRegistroCabecera(cabecera, idCabecera, entrada.p_IdUsuario);
				cabeceraList.add(objCabecera);
				indexCabecera++;
				
				Detalle objDetalleCabecera = crearDetalleCabecera(secuenciasDetalle,indexDetalle,objCabecera.IdCabecera);
				detalleList.add(objDetalleCabecera);
				indexDetalle++;
				
				String Glosa = "Omnibus nacional ingresado. Folio " + omnibus.IdOmnibus + ". Cabecera " + objCabecera.IdCabecera + ".";
				lstDatosLog.add(insertarLogService.armaRegistroLog("FFM",entrada.p_IdUsuario,"INFORMACION",Glosa,objCabecera.IdCabecera.toString(),objCabecera.NombreArchivo));
				
				if(solicitudesNacionalesLlave != null) {
					for(ObjAporteRescate solicitud : solicitudesNacionalesLlave) {
						Long idOmnibusDetalle = secuenciasMovimientoCorredora.get(indexMovimiento);
						ConfiguracionEmisorOmnibus configuracionEmisor = obtenerConfiguracionEmisor(configuracionesEmisores,solicitud.IdEmisor);
						configuracionEmisor.FolioOperacion += 1;
						Long idMovimiento = configuracionEmisor.FolioOperacion;
						Long idDetalle = secuenciasDetalle.get(indexDetalle);
						SerieFondoSolicitudes serie = hSeries.get(solicitud.IdSerieFondo);
						Long decimales = 0L;
						if(serie != null) {
							decimales = serie.DecimalesCuota;
							seriesCabecera.add(serie);
						}
						
						Detalle ObjDetalle = crearDetalleNacional(con,solicitud,idDetalle,cabecera,decimales,objCabecera.IdCabecera,idMovimiento,fechaSistema, formatDate);
						detalleList.add(ObjDetalle);
		
						OmnibusDetalle omnibusDetalle = crearOmnibusDetalle(cabecera.Lote,idOmnibusDetalle,solicitud,formatDate);
						omnibusDetalleList.add(omnibusDetalle);
						
						enlazarSolicitudNacionalOmnibusDetalle(listaSolicitudes,solicitud,idOmnibusDetalle);
						
						indexMovimiento++;
						indexDetalle++;
					}
				}
				Long decimalesCabecera = obtenerDecimalesCabecera(seriesCabecera);
				cabecera.Monto = obtenerMontoCabecera(con,cabecera,decimalesCabecera);
				objDetalleCabecera.Linea = obtenerLineaCabeceraNacional(cabecera);
			}
			
			for(CabeceraArchivoTRXI cabecera : listCabeceraArchivoTRXInternacional) {
				LlaveSolicitudInternacional llave = crearLlaveSolicitudInternacional(cabecera,separaOmnibusInternacionalPorTipoMovimiento);
				List<ObjAporteRescate> solicitudesInternacionalesLlave = solicitudesInternacionalesAgrupadas.get(llave);
				
				cabecera.Lote = secuenciasLote.get(indexCabecera);
				cabecera.NumeroRegistros = solicitudesInternacionalesLlave != null ? Long.valueOf(solicitudesInternacionalesLlave.size()) : 0L;
				
				Omnibus omnibus = crearOmnibus(cabecera.Lote, fechaSistema);
				omnibuses.add(omnibus);
				
				Long idCabecera = secuenciasCabecera.get(indexCabecera);
				Cabecera objCabecera = crearRegistroCabecera(cabecera, idCabecera, entrada.p_IdUsuario);
				cabeceraList.add(objCabecera);
				indexCabecera++;
				
				Detalle objDetalleCabecera = crearDetalleCabecera(secuenciasDetalle,indexDetalle,objCabecera.IdCabecera);
				objDetalleCabecera.Linea = obtenerLineaCabeceraInternacional(configuracionOmnibus);
				detalleList.add(objDetalleCabecera);
				indexDetalle++;
				
				String Glosa = "Omnibus internacional ingresado. Folio " + omnibus.IdOmnibus + ". Cabecera " + objCabecera.IdCabecera + ".";
				lstDatosLog.add(insertarLogService.armaRegistroLog("FFM",entrada.p_IdUsuario,"INFORMACION",Glosa,objCabecera.IdCabecera.toString(),objCabecera.NombreArchivo));
				
				if(solicitudesInternacionalesLlave != null) {
					for(ObjAporteRescate solicitud : solicitudesInternacionalesLlave) {
						Long idMovimiento = secuenciasMovimientoCorredora.get(indexMovimiento);
						Long idDetalle = secuenciasDetalle.get(indexDetalle);
						ConfiguracionEmisorOmnibus configuracionEmisor = obtenerConfiguracionEmisor(configuracionesEmisores,solicitud.IdEmisor);
						configuracionEmisor.FolioOperacion += 1;
						Detalle ObjDetalle = crearDetalleInternacional(idDetalle,objCabecera.IdCabecera,solicitud,configuracionEmisor,decimalFormat);
						detalleList.add(ObjDetalle);
						
						OmnibusDetalle omnibusDetalle = crearOmnibusDetalle(cabecera.Lote,idMovimiento,solicitud,formatDate);
						omnibusDetalleList.add(omnibusDetalle);
						
						enlazarSolicitudInternacionalOmnibusDetalle(listaSolicitudes,solicitud,idMovimiento,separaOmnibusInternacionalPorTipoMovimiento);
						
						indexMovimiento++;
						indexDetalle++;
					}
				}
			}
			
			List<ConfiguracionEmisorOmnibus> listaConfiguracionEmisores = new ArrayList<ConfiguracionEmisorOmnibus>(configuracionesEmisores.values());
			insertarActualizarDatos(con,omnibuses,cabeceraList,detalleList,omnibusDetalleList,listaSolicitudes,listaConfiguracionEmisores);
			
			if(lstDatosLog.size() >= 1) {
				insertarLogService.logica(con,lstDatosLog);
			}	
		}else{
			salida.P_OUT_ERRORESCONTADOR = 1L;
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_NO_SE_INGRESARON_OPERACIONES",new HashMap<String, String>(),con,entrada.p_IdEmpresa));
		}
		
		logger.info("FIN::GenerarDataReporteTRXI");
		return salida;
	}
	
	private Map<String,ConfiguracionOmnibus> obtenerConfiguracionOmnibus(Connection con) throws Exception {
		logger.info("INICIO::GenerarDataReporteTRXI.obtenerLineaCabeceraInternacional");
		SQL sql = new SQL();
		List<ConfiguracionOmnibus> listaConfiguracionOmnibus = sql.query(con, SQL_SELECT_CONFIGURACION_OMNIBUS, new HashMap<String, Object>(), ConfiguracionOmnibus.class);
		Map<String,ConfiguracionOmnibus> configuracionOmnibus = listaConfiguracionOmnibus.parallelStream().collect(Collectors.toMap(configuracion->configuracion.IdConfiguracion, configuracion->configuracion));
		logger.info("FIN::GenerarDataReporteTRXI.obtenerLineaCabeceraInternacional");
		return configuracionOmnibus;
	}
	
	private Map<String,ConfiguracionEmisorOmnibus> obtenerConfiguracionesEmisores(Connection con) throws Exception{
		logger.info("INICIO::GenerarDataReporteTRXI.obtenerConfiguracionesEmisores");
		SQL sql = new SQL();
		List<ConfiguracionEmisorOmnibus> listaConfiguracionEmisores = sql.query(con, SQL_SELECT_CONFIGURACION_EMISOR_OMNIBUS, new HashMap<String, Object>(), ConfiguracionEmisorOmnibus.class);
		Map<String,ConfiguracionEmisorOmnibus> configuracionEmisores = listaConfiguracionEmisores.parallelStream().collect(Collectors.toMap(configuracion->configuracion.IdEmisor, configuracion->configuracion));
		logger.info("FIN::GenerarDataReporteTRXI.obtenerConfiguracionesEmisores");
		return configuracionEmisores;
	}
	
	private ConfiguracionEmisorOmnibus obtenerConfiguracionEmisor(Map<String,ConfiguracionEmisorOmnibus> configuracionesEmisores, String idEmisor) {
		logger.info("INICIO::GenerarDataReporteTRXI.obtenerConfiguracionEmisor");
		ConfiguracionEmisorOmnibus configuracionEmisor = configuracionesEmisores.get(idEmisor);
		if(configuracionEmisor == null) {
			configuracionEmisor = new ConfiguracionEmisorOmnibus();
			configuracionEmisor.IdEmisor = idEmisor;
		}
		logger.info("FIN::GenerarDataReporteTRXI.obtenerConfiguracionEmisor");
		return configuracionEmisor;
	}
	
	private String generaNombreArchivo(CabeceraArchivoTRXI obj){
		logger.info("INICIO::GenerarDataReporteTRXI.generaNombreArchivo");
		
		String nombreArchivo = "";
		if(_c.SI.equals(obj.FondoExtranjero)) {
			DateFormat fDate = new SimpleDateFormat("yyyyMMdd");
			String strFecha  = fDate.format(new Date(System.currentTimeMillis()));
			nombreArchivo = "INF_MTA_CHI_ORDER_P." + strFecha + "_" + String.valueOf(obj.Lote) + ".csv";
		}else {
			DateFormat fDate = new SimpleDateFormat("yyyyMMdd_HHmmss");
			String strFecha  = fDate.format(new Date(System.currentTimeMillis()));
			nombreArchivo = "TRX_I_"+obj.TipoCuentaArchivo+"_"+StringUtils.leftPad(String.valueOf(obj.Lote),9,"0")+"_"+strFecha+".txt";
		}
		logger.info("FIN::GenerarDataReporteTRXI.generaNombreArchivo");
		return nombreArchivo;
	}
	
	private String generaLineaNacional(Connection con, ObjAporteRescate objetoAporteRescate, CabeceraArchivoTRXI cabecera, String fechaTransaccion, Long idMovimiento, String fechaSistema, Long decimales) throws Exception {
		logger.info("INICIO::GenerarDataReporteTRXI.generaLineaNacional");
		
		if("R".equals(objetoAporteRescate.TipoMovimiento) && objetoAporteRescate.TipoTransaccion.equals(2L)) {
			objetoAporteRescate.Valor = objetoAporteRescate.NumeroDeCuotas.setScale(decimales.intValue(), RoundingMode.HALF_DOWN);
		}else {
			objetoAporteRescate.Valor = MonedaFormato.formatear(con, objetoAporteRescate.Valor, MonedaFormato.MONTO, objetoAporteRescate.MonedaTransaccion).valor;
		}
		
		String[] valorSeparado = objetoAporteRescate.Valor.toPlainString().split("\\.");
		String salida = fechaSistema.replaceAll("\\-","") + StringUtils.leftPad(Long.toString(idMovimiento),5,"0") + ";" + //ID_ING
				StringUtils.leftPad(objetoAporteRescate.Rut.toUpperCase(),10,"0") + ";" + //RUT
				StringUtils.rightPad(objetoAporteRescate.TipoMovimiento,1," ") + ";" + //TIPO MOVIMIENTO
				StringUtils.leftPad(objetoAporteRescate.IdInstrumento.toUpperCase(),8,"0") + ";" + //ID INSTRUMENTO
				StringUtils.rightPad(StringUtils.stripAccents(objetoAporteRescate.NombreFFMM.toUpperCase()),50," ") + ";" + //NOMBRE FFMM
				StringUtils.leftPad(valorSeparado[0],12,"0") + //VALOR
				StringUtils.rightPad(valorSeparado.length == 2 ? valorSeparado[1] : "0",6,"0") + ";" + //DECIMALES VALOR
				StringUtils.leftPad(Long.toString(objetoAporteRescate.TipoTransaccion), 1, " ") + ";" + //TIPO TRANSACCION
				StringUtils.leftPad(fechaTransaccion, 8, "0") + ";" + //FECHA TRANSACCION
				StringUtils.leftPad(objetoAporteRescate.MonedaTransaccion, 3, " ") + ";" + //MONEDA TRANSACCION
				StringUtils.rightPad(objetoAporteRescate.TipoCuenta, 2, "0") + ";" + //TIPO CUENTA
				StringUtils.leftPad(String.valueOf(cabecera.Lote), 9, "0") + ";" + //LOTE
				StringUtils.leftPad("0", 9, "0") +  //VALOR CUOTA
				StringUtils.rightPad("0", 9, "0") + ";" + //DECIMALES VALOR CUOTA
				StringUtils.leftPad("0", 12, "0") + //NUMERO CUOTAS PESOS EQUIVALENTES
				StringUtils.rightPad("0", 6, "0") + ";" + //DECIMALES NUMERO CUOTAS PESOS EQUIVALENTES
				StringUtils.leftPad("", 8, "0") + ";" + //FECHA LIQUIDEZ
				StringUtils.leftPad(Long.toString(objetoAporteRescate.IdAdministradora), 12, "0") + ";" + //ID_Administradora
				StringUtils.leftPad(Long.toString(objetoAporteRescate.Flag), 1, " ") + ";" + //FLAG
				StringUtils.rightPad(objetoAporteRescate.Descripcion, 100, " ") + ";" + //DESCRIPCION
				StringUtils.leftPad("0",18,"0") + ";" +  //TIPO CAMBIO
				StringUtils.leftPad("0",18,"0") + ";" + //MONTO CLP
				StringUtils.leftPad("", 8, "0") + ";" + //FECHA CUOTA
				StringUtils.rightPad(objetoAporteRescate.Serie, 10, " ") + ";";//SERIE
		
		logger.info("FIN::GenerarDataReporteTRXI.generaLineaNacional");
		return salida;
	}
	
	private String generaLineaInternacional(ObjAporteRescate objetoAporteRescate, ConfiguracionEmisorOmnibus configuracionEmisor,DecimalFormat decimalFormat) {
		logger.info("INICIO::GenerarDataReporteTRXI.generaLineaInternacional");
		
		String linea = "";
		linea += agregarComa(configuracionEmisor.NumeroCuenta); //Account number
		if ("S".equals(objetoAporteRescate.TipoMovimiento)) {
			linea += agregarComa("SWIC");
		} else if("A".equals(objetoAporteRescate.TipoMovimiento)) {
			linea += agregarComa("SUBS");
		} else {
			linea += agregarComa("REDM");
		} //Order type
		linea += agregarComa(objetoAporteRescate.ISIN); //ISIN/Common code
		linea += agregarComa(String.valueOf(configuracionEmisor.FolioOperacion)); //Participant order reference
		linea += agregarComa(""); //Cash settlement date
		if("S".equals(objetoAporteRescate.TipoMovimiento)){
			linea += Long.valueOf(1).equals(objetoAporteRescate.TipoTransaccion) ? agregarComa("") : agregarComa(obtenerNumeroSwitchInternacionalFormateado(objetoAporteRescate.NumeroDeCuotas,decimalFormat)); //Number of shares
		}else {
			linea += Long.valueOf(1).equals(objetoAporteRescate.TipoTransaccion) ? agregarComa("") : agregarComa(obtenerNumeroInternacionalFormateado(objetoAporteRescate.NumeroDeCuotas,decimalFormat)); //Number of shares
		}
		
		if("S".equals(objetoAporteRescate.TipoMovimiento)) {
			linea += Long.valueOf(1).equals(objetoAporteRescate.TipoTransaccion) ? agregarComa(obtenerNumeroSwitchInternacionalFormateado(objetoAporteRescate.Monto,decimalFormat)) : agregarComa(""); //Instructed gross cash amount
		}else {
			linea += Long.valueOf(1).equals(objetoAporteRescate.TipoTransaccion) ? agregarComa(obtenerNumeroInternacionalFormateado(objetoAporteRescate.Monto,decimalFormat)) : agregarComa(""); //Instructed gross cash amount
		}
		linea += agregarComa(objetoAporteRescate.MonedaTransaccion);//Payment currency
		linea += agregarComa("");//By order of client reference
		linea += agregarComa("");//In favor of client reference
		linea += agregarComa("");//Certification completed
		linea += agregarComa("CASH");//Dividend policy
		linea += agregarComa(configuracionEmisor.IdentificadorCuenta);//Account Id
		if("S".equals(objetoAporteRescate.TipoMovimiento)) {
			linea += agregarComa("");//Commission recipient
		}else{
			linea += agregarComa(configuracionEmisor.GlosaComision);//Commission recipient
		}
		linea += agregarComa("");//Sales charge type 1
		linea += agregarComa("");//Sales charge rate 1
		linea += agregarComa("");//Sales charge type 2
		linea += agregarComa("");//Sales charge rate 2
		linea += agregarComa("");//Sales charge type 3
		linea += agregarComa("");//Sales charge rate 3
		linea += agregarComa("");//Sales charge type 4
		linea += agregarComa("");//Sales charge rate 4
		linea += agregarComa("");//Commission type
		linea += agregarComa("");//Commission rate
		linea += agregarComa(objetoAporteRescate.ISINDestino);
		linea += agregarComa(""); //Dividend policy TO
		linea += agregarComa("");//SWITCH TO Sales charge type 1
		linea += agregarComa("");//SWITCH TO Sales charge rate 1
		linea += agregarComa("");//SWITCH TO Sales charge type 2
		linea += agregarComa("");//SWITCH TO Sales charge rate 2
		linea += agregarComa("");//SWITCH TO Sales charge type 3
		linea += agregarComa("");//SWITCH TO Sales charge rate 3
		linea += agregarComa("");//SWITCH TO Commission type
		linea += agregarComa("");//SWITCH TO Commission rate
		linea += agregarComa("");//Narrative to FundSettle
		linea += agregarComa("");//Narrative to the Transfer Agent
		linea += "";//Commission waiver
		logger.info("FIN::GenerarDataReporteTRXI.generaLineaInternacional");
		return linea;
	}
	
	private String agregarComa(String campo) {
		return campo + ",";
	}
	
	private Cabecera crearRegistroCabecera(CabeceraArchivoTRXI cabecera, Long idCabecera, Long idUsuario) {
		logger.info("INICIO::GenerarDataReporteTRXI.crearRegistroCabebera");
		Cabecera objCabecera = new Cabecera();
		objCabecera.NombreArchivo = generaNombreArchivo(cabecera);
		objCabecera.IdCabecera = idCabecera;
		objCabecera.IdCodigoTipo = "TRXI_" + cabecera.IdEmisor;
		objCabecera.IdEstado = "PEN";
		objCabecera.FechaCreacion = new Timestamp(System.currentTimeMillis());
		objCabecera.IdUsuario = idUsuario;
		logger.info("FIN::GenerarDataReporteTRXI.crearRegistroCabebera");
		return objCabecera;
	}
	
	private Detalle crearDetalleCabecera(List<Long> secuenciasDetalle, int indexDetalle, Long idCabecera) {
		logger.info("INICIO::GenerarDataReporteTRXI.crearDetalleCabecera");
		Detalle objDetalleCabecera = new Detalle();
		objDetalleCabecera.IdDetalle= secuenciasDetalle.get(indexDetalle);
		objDetalleCabecera.IdCabecera=idCabecera;
		logger.info("FIN::GenerarDataReporteTRXI.crearDetalleCabecera");
		return objDetalleCabecera;
	}
	
	private Map<LlaveSolicitudNacional,List<ObjAporteRescate>> obtenerMapSolicitudesNacionalesAgrupadasPorLlave(List<ObjAporteRescate> solicitudes){
		logger.info("INICIO::GenerarDataReporteTRXI.obtenerHashMapSolicitudesNacionales");
		Map<LlaveSolicitudNacional,List<ObjAporteRescate>> mapSolicitudesNacionalesAgrupadasPorLlave = solicitudes.parallelStream()
				.filter(solicitud->_c.NO.equals(solicitud.FondoExtranjero))
				.collect(Collectors.groupingBy(solicitud->{
					return crearLlaveSolicitudNacional(solicitud);
				}));
		logger.info("FIN::GenerarDataReporteTRXI.obtenerHashMapSolicitudesNacionales");
		return mapSolicitudesNacionalesAgrupadasPorLlave;
	}
	
	private Map<LlaveSolicitudInternacional,List<ObjAporteRescate>> obtenerMapSolicitudesInternacionalesAgrupadasPorLlave(List<ObjAporteRescate> solicitudes, String separaPorTipoMovimiento){
		logger.info("INICIO::GenerarDataReporteTRXI.obtenerHashMapSolicitudesNacionales");
		Map<LlaveSolicitudInternacional,List<ObjAporteRescate>> mapSolicitudesNacionalesAgrupadasPorLlave = solicitudes.parallelStream()
				.filter(solicitud->_c.SI.equals(solicitud.FondoExtranjero))
				.collect(Collectors.groupingBy(solicitud->{
					return crearLlaveSolicitudInternacional(solicitud,separaPorTipoMovimiento);
				}));
		logger.info("FIN::GenerarDataReporteTRXI.obtenerHashMapSolicitudesNacionales");
		return mapSolicitudesNacionalesAgrupadasPorLlave;
	}
	
	private LlaveSolicitudNacional crearLlaveSolicitudNacional(ObjAporteRescate solicitud) {
		logger.info("INICIO::GenerarDataReporteTRXI.crearLlaveSolicitudesNacionales");
		LlaveSolicitudNacional llave = new LlaveSolicitudNacional();
		llave.TipoTransaccion = solicitud.TipoTransaccion;
		llave.TipoMovimiento = solicitud.TipoMovimiento;
		llave.TipoCuenta = solicitud.TipoCuenta;
		llave.IdEmisor = solicitud.IdEmisor;
		llave.IdMoneda = solicitud.MonedaTransaccion;
		logger.info("FIN::GenerarDataReporteTRXI.crearLlaveSolicitudesNacionales");
		return llave;
	}
	
	private LlaveSolicitudNacional crearLlaveSolicitudNacional(CabeceraArchivoTRXI cabecera) {
		logger.info("INICIO::GenerarDataReporteTRXI.crearLlaveSolicitudesNacionales");
		LlaveSolicitudNacional llave = new LlaveSolicitudNacional();
		llave.TipoTransaccion = cabecera.TipoTransaccion;
		llave.TipoMovimiento = cabecera.TipoMovimiento;
		llave.TipoCuenta = cabecera.TipoCuenta;
		llave.IdEmisor = cabecera.IdEmisor;
		llave.IdMoneda = cabecera.IdMoneda;
		logger.info("FIN::GenerarDataReporteTRXI.crearLlaveSolicitudesNacionales");
		return llave;
	}
	
	private LlaveSolicitudInternacional crearLlaveSolicitudInternacional(ObjAporteRescate solicitud, String separaPorTipoMovimiento) {
		logger.info("INICIO::GenerarDataReporteTRXI.crearLlaveSolicitudInternacional");
		LlaveSolicitudInternacional llave = new LlaveSolicitudInternacional();
		llave.TipoMovimiento = _c.SI.equals(separaPorTipoMovimiento) ? solicitud.TipoMovimiento : "";
		llave.IdEmisor = solicitud.IdEmisor;
		llave.IdMoneda = solicitud.MonedaTransaccion;
		logger.info("FIN::GenerarDataReporteTRXI.crearLlaveSolicitudInternacional");
		return llave;
	}
	
	private LlaveSolicitudInternacional crearLlaveSolicitudInternacional(CabeceraArchivoTRXI cabecera, String separaPorTipoMovimiento) {
		logger.info("INICIO::GenerarDataReporteTRXI.crearLlaveSolicitudInternacional");
		LlaveSolicitudInternacional llave = new LlaveSolicitudInternacional();
		llave.TipoMovimiento = _c.SI.equals(separaPorTipoMovimiento) ? cabecera.TipoMovimiento : "";
		llave.IdEmisor = cabecera.IdEmisor;
		llave.IdMoneda = cabecera.IdMoneda;
		logger.info("FIN::GenerarDataReporteTRXI.crearLlaveSolicitudInternacional");
		return llave;
	}
	
	private Detalle crearDetalleNacional(Connection con, ObjAporteRescate solicitud, Long idDetalle, CabeceraArchivoTRXI cabecera, Long decimales, Long idCabecera, Long idMovimiento, String fechaSistema, DateFormat formatDate) throws Exception {
		logger.info("INICIO::GenerarDataReporteTRXI.obtenerDetalleNacional");
		String FTrx= (formatDate.format(solicitud.FechaTransaccion)).replaceAll("-","");
		Detalle objDetalle = new Detalle();
		objDetalle.IdDetalle = idDetalle;
		objDetalle.IdCabecera= idCabecera;
		objDetalle.Linea= generaLineaNacional(con,solicitud,cabecera,FTrx,idMovimiento,fechaSistema,decimales);
		
		logger.info("FIN::GenerarDataReporteTRXI.obtenerDetalleNacional");
		return objDetalle;
	}
	
	private Detalle crearDetalleInternacional(Long idDetalle, Long idCabecera, ObjAporteRescate solicitud, ConfiguracionEmisorOmnibus configuracionEmisor,DecimalFormat decimalFormat) {
		logger.info("INICIO::GenerarDataReporteTRXI.crearDetalleInternacional");
		Detalle objDetalle = new Detalle();
		objDetalle.IdDetalle = idDetalle;
		objDetalle.IdCabecera= idCabecera;
		objDetalle.Linea= generaLineaInternacional(solicitud,configuracionEmisor,decimalFormat);
		logger.info("FIN::GenerarDataReporteTRXI.crearDetalleInternacional");
		return objDetalle;
	}
	
	private OmnibusDetalle crearOmnibusDetalle(Long lote, Long idOmnibusDetalle, ObjAporteRescate solicitud, DateFormat formatDate) {
		logger.info("INICIO::GenerarDataReporteTRXI.obtenerOmnibusDetalle");
		OmnibusDetalle omnibusDetalle = new OmnibusDetalle();
		omnibusDetalle.IdOmnibus = lote;
		omnibusDetalle.IdOmnibusDetalle = idOmnibusDetalle;
		omnibusDetalle.IdSerieFondo = solicitud.IdSerieFondo;
		omnibusDetalle.Monto = solicitud.Monto;
		omnibusDetalle.CantidadCuotas = solicitud.NumeroDeCuotas;
		omnibusDetalle.FechaCertificacion = (formatDate.format(solicitud.FechaCuota));
		omnibusDetalle.CantidadOperaciones = solicitud.CantidadOperaciones;
		logger.info("FIN::GenerarDataReporteTRXI.obtenerOmnibusDetalle");
		return omnibusDetalle;
	}
	
	private void enlazarSolicitudNacionalOmnibusDetalle(List<Solicitudes> solicitudes, ObjAporteRescate detalle, Long idOmnibusDetalle) {
		logger.info("INICIO::GenerarDataReporteTRXI.enlazarSolicitudNacionalOmnibusDetalle");
		solicitudes.parallelStream()
		.filter(solicitud->{
			return solicitud.IdSerieFondo.equalsIgnoreCase(detalle.IdSerieFondo) && solicitud.TipoCuenta.equalsIgnoreCase(detalle.TipoCuenta) && solicitud.TipoMovimiento.equalsIgnoreCase(detalle.TipoMovimiento) && solicitud.TipoTransaccion.equals(detalle.TipoTransaccion);
		})
		.forEach(solicitud->{
			solicitud.IdOmnibusDetalle = idOmnibusDetalle;
		});
		logger.info("FIN::GenerarDataReporteTRXI.enlazarSolicitudNacionalOmnibusDetalle");
	}
	
	private void enlazarSolicitudInternacionalOmnibusDetalle(List<Solicitudes> solicitudes, ObjAporteRescate detalle, Long idDetalle, String separaPorTipoMovimiento) {
		logger.info("INICIO::GenerarDataReporteTRXI.enlazarSolicitudInternacionalOmnibusDetalle");
		solicitudes.parallelStream()
		.filter(solicitud->{
			if(_c.SI.equals(separaPorTipoMovimiento)) {
				return solicitud.IdSerieFondo.equalsIgnoreCase(detalle.IdSerieFondo) && solicitud.TipoMovimiento.equalsIgnoreCase(detalle.TipoMovimiento);
			}else {
				return solicitud.IdSerieFondo.equalsIgnoreCase(detalle.IdSerieFondo);
			}
			
		})
		.forEach(solicitud->{
			solicitud.IdOmnibusDetalle = idDetalle;
		});
		logger.info("FIN::GenerarDataReporteTRXI.enlazarSolicitudInternacionalOmnibusDetalle");
	}
	
	private Omnibus crearOmnibus(Long lote, String fechaSistema) {
		logger.info("INICIO::GenerarDataReporteTRXI.crearOmnibus");
		Omnibus omnibus = new Omnibus();
		omnibus.IdOmnibus = lote;
		omnibus.FechaOmnibus = fechaSistema;
		logger.info("FIN::GenerarDataReporteTRXI.crearOmnibus");
		return omnibus;
		
	}
	
	private Long obtenerDecimalesCabecera(List<SerieFondoSolicitudes> seriesCabecera) {
		logger.info("INICIO::GenerarDataReporteTRXI.obtenerDecimalesCabecera");
		SerieFondoSolicitudes serieConMaximoDecimales = seriesCabecera.stream().max(Comparator.comparing(c -> c.DecimalesCuota)).get();
		Long maximoDecimales = serieConMaximoDecimales == null ? 0L : serieConMaximoDecimales.DecimalesCuota;
		logger.info("INICIO::GenerarDataReporteTRXI.obtenerDecimalesCabecera");
		return maximoDecimales;
	}
	
	private BigDecimal obtenerMontoCabecera(Connection con, CabeceraArchivoTRXI cabecera, Long decimales) throws Exception {
		logger.info("INICIO::GenerarDataReporteTRXI.obtenerMontoCabecera");
		BigDecimal monto = BigDecimal.ZERO;
		if("R".equals(cabecera.TipoMovimiento) && cabecera.TipoTransaccion.equals(2L)) {
			monto = cabecera.Monto.setScale(decimales.intValue(), RoundingMode.HALF_DOWN);
		}else {
			monto = MonedaFormato.formatear(con, cabecera.Monto, MonedaFormato.MONTO, cabecera.IdMoneda).valor;
		}
		logger.info("FIN::GenerarDataReporteTRXI.obtenerMontoCabecera");
		return monto;
	}
	
	private String obtenerLineaCabeceraNacional(CabeceraArchivoTRXI cabecera) {
		logger.info("INICIO::GenerarDataReporteTRXI.obtenerLineaCabeceraNacional");
		String[] montoSeparado = cabecera.Monto.toPlainString().split("\\.");
		String linea = StringUtils.leftPad(Long.toString(cabecera.NumeroRegistros), 9, "0") + ";" +
				   StringUtils.leftPad(montoSeparado[0],12,"0") +
				   StringUtils.rightPad(montoSeparado.length == 2 ? montoSeparado[1] : "0",6,"0") + ";" + 
				   StringUtils.leftPad("",30," ") + ";";
		logger.info("INICIO::GenerarDataReporteTRXI.obtenerLineaCabeceraNacional");
		return linea;
	}
	
	private String obtenerLineaCabeceraInternacional(Map<String, ConfiguracionOmnibus> configuracionOmnibus) {
		logger.info("INICIO::GenerarDataReporteTRXI.obtenerLineaCabeceraInternacional");
		ConfiguracionOmnibus configuracionCabeceraOmnibusInternacional = configuracionOmnibus.get(_c.ConfiguracionCabeceraOmnibusInternacional);
		String linea = configuracionCabeceraOmnibusInternacional == null ? "" : configuracionCabeceraOmnibusInternacional.Configuracion;
		logger.info("INICIO::GenerarDataReporteTRXI.obtenerLineaCabeceraInternacional");
		return linea;
	}
	
	private String obtenerFechasistema(Connection con, Long idEmpresa) throws Exception {
		logger.info("INICIO::GenerarDataReporteTRXI.obtenerFechasistema");
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con,idEmpresa,"PLATAFORMA","PLA.FechaSistema");
		logger.info("FIN::GenerarDataReporteTRXI.obtenerFechasistema");
		return fechaSistema;
	}
	
	private String obtenerSepararOmnibusInternacionalTipoMovimiento(Connection con, Long idEmpresa) {
		logger.info("INICIO::GenerarDataReporteTRXI.obtenerSepararOmnibusInternacionalTipoMovimiento");
		String separarPorTipoMovimiento = "N";
		try {
			separarPorTipoMovimiento = ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "FONDOMUTUO", "OMNIBUS_INTERNACIONAL_SEPARA_POR_TIPO_MOVIMIENTO");
		}catch(Exception e) {
			logger.warn("ADVERTENCIA::GenerarDataReporteTRXI.obtenerIdCuentaOmnibus::no existe parametro OMNIBUS_INTERNACIONAL_SEPARA_POR_TIPO_MOVIMIENTO en Fondos Mutuos");
		}
		logger.info("FIN::GenerarDataReporteTRXI.obtenerSepararOmnibusInternacionalTipoMovimiento");
		return separarPorTipoMovimiento;
	}
	
	private List<CabeceraArchivoTRXI> obtenerCabecerasNacionales(Connection con,List <Long> solicitudes) throws SQLException{
		logger.info("INICIO::GenerarDataReporteTRXI.obtenerCabecerasNacionales");
		SQL sql = new SQL();
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("Solicitudes", solicitudes);
		List<CabeceraArchivoTRXI> listCabeceraArchivoTRXNacional = sql.query(con, SQL_SELECT_APORTE_RESCATE_CABECERA_NACIONAL, params, CabeceraArchivoTRXI.class);
		logger.info("FIN::GenerarDataReporteTRXI.obtenerCabecerasNacionales");
		return listCabeceraArchivoTRXNacional;
	}
	
	private List<CabeceraArchivoTRXI> obtenerCabecerasInternacionales(Connection con,List <Long> solicitudes, String separarTipoMovimiento) throws SQLException{
		logger.info("INICIO::GenerarDataReporteTRXI.obtenerCabecerasInternacionales");
		SQL sql = new SQL();
		List<CabeceraArchivoTRXI> listCabeceraArchivoTRXInternacional = new ArrayList<CabeceraArchivoTRXI>();
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("Solicitudes", solicitudes);
		if(_c.SI.equals(separarTipoMovimiento)) {
			listCabeceraArchivoTRXInternacional = sql.query(con, SQL_SELECT_APORTE_RESCATE_CABECERA_INTERNACIONAL_SEPARADO, params, CabeceraArchivoTRXI.class);
		}else {
			listCabeceraArchivoTRXInternacional = sql.query(con, SQL_SELECT_APORTE_RESCATE_CABECERA_INTERNACIONAL, params, CabeceraArchivoTRXI.class);
		}
		logger.info("FIN::GenerarDataReporteTRXI.obtenerCabecerasInternacionales");
		return listCabeceraArchivoTRXInternacional;
	}
	
	private List<ObjAporteRescate> obtenerAportesRescatesAgrupados(Connection con,List <Long> solicitudes) throws SQLException{
		logger.info("INICIO::GenerarDataReporteTRXI.obtenerAportesRescatesAgrupados");
		SQL sql = new SQL();
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("Solicitudes", solicitudes);
		List<ObjAporteRescate> listaAporteRescate = sql.query(con,SQL_SELECT_APORTE_RESCATE,params,ObjAporteRescate.class);
		logger.info("FIN::GenerarDataReporteTRXI.obtenerAportesRescatesAgrupados");
		return listaAporteRescate;
	}
	
	private List<Solicitudes> obtenerSolicitudes(Connection con,List <Long> solicitudes) throws SQLException{
		logger.info("INICIO::GenerarDataReporteTRXI.obtenerSolicitudes");
		SQL sql = new SQL();
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("Solicitudes", solicitudes);
		List<Solicitudes> listaSolicitudes = sql.query(con, SQL_SELECT_SOLICITUDES, params, Solicitudes.class);
		logger.info("FIN::GenerarDataReporteTRXI.obtenerSolicitudes");
		return listaSolicitudes;
	}
	
	private Map<String,SerieFondoSolicitudes> obtenerSeriesFondo(Connection con, List<Solicitudes> listaSolicitudes, Long idEmpresa) throws Exception{
		logger.info("INICIO::GenerarDataReporteTRXI.obtenerSeriesFondo");
		SQL sql = new SQL();
		HashMap<String,Object> params = new HashMap<String,Object>();
		List<String> series = listaSolicitudes.parallelStream().map(serie -> serie.IdSerieFondo).collect(Collectors.toList());
		params.clear();
		params.put("IdEmpresa", idEmpresa);
		params.put("IdSerieFondo", series);
		List<SerieFondoSolicitudes> seriesSolicitudes = sql.query(con, SQL_SELECT_SERIE_FONDO, params, SerieFondoSolicitudes.class);
		Map<String,SerieFondoSolicitudes> hSeries = seriesSolicitudes.parallelStream().collect(Collectors.toMap(serie->serie.IdSerieFondo, serie->serie));
		logger.info("FIN::GenerarDataReporteTRXI.obtenerSeriesFondo");
		return hSeries;
	}
	
	private void insertarActualizarDatos(Connection con,List<Omnibus> omnibuses,List<Cabecera> cabeceraList,List<Detalle> detalleList,List<OmnibusDetalle> omnibusDetalleList,List<Solicitudes> listaSolicitudes,List<ConfiguracionEmisorOmnibus> listaConfiguracionEmisores) throws Exception {
		logger.info("INICIO::GenerarDataReporteTRXI.insertarActualizarDatos");
		SQL sql = new SQL();
		sql.batch(con, SQL_INSERT_OMNIBUS, omnibuses, Omnibus.class);
		sql.batch(con, SQL_INSERT_CABECERA, cabeceraList, Cabecera.class);
		sql.batch(con, SQL_INSERT_DETALLE, detalleList, Detalle.class);
		sql.batch(con, SQL_INSERT_OMNIBUS_DETALLE, omnibusDetalleList, OmnibusDetalle.class);
		sql.batch(con, SQL_UPDATE_SOLICITUD_APORTE_RESCATE, listaSolicitudes, Solicitudes.class);
		sql.batch(con, SQL_UPDATE_FOLIO_OPERACION_EMISOR, listaConfiguracionEmisores, ConfiguracionEmisorOmnibus.class);
		logger.info("FIN::GenerarDataReporteTRXI.insertarActualizarDatos");
	}
	
	private String obtenerNumeroInternacionalFormateado(BigDecimal numero,DecimalFormat decimalFormat) {
		logger.info("INICIO::GenerarDataReporteTRXI.obtenerNumeroInternacionalFormateado");
		BigDecimal numeroAFormatear = numero.setScale(2, RoundingMode.HALF_UP);
		String numeroFormateado = decimalFormat.format(numeroAFormatear);
		logger.info("FIN::GenerarDataReporteTRXI.obtenerNumeroInternacionalFormateado");
		return numeroFormateado;
	}
	
	private String obtenerNumeroSwitchInternacionalFormateado(BigDecimal numero,DecimalFormat decimalFormat) {
		logger.info("INICIO::GenerarDataReporteTRXI.obtenerNumeroSwitchInternacionalFormateado");
		String numeroFormateado = obtenerNumeroInternacionalFormateado(numero,decimalFormat);
		numeroFormateado = StringUtils.rightPad(numeroFormateado,25," ");
		logger.info("FIN::GenerarDataReporteTRXI.obtenerNumeroSwitchInternacionalFormateado");
		return numeroFormateado;
	}
	
	private void bloqueoSolicitudes(Connection con, List <Long> solicitudes) throws SQLException {
		logger.debug("INICIO::GenerarDataReporteTRXI.bloqueoSolicitudes");
		SQL sql = new SQL();
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("Solicitudes", solicitudes);
		sql.query(con, SQL_SELECT_BLOQUEO_SOLICITUDES, params, Solicitudes.class);
		logger.debug("FIN::GenerarDataReporteTRXI.bloqueoSolicitudes");
		return;
	}
	
	/***********************************************************************************************
	 *                                        CALL
	 ***********************************************************************************************/
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = new Gson();		
		EntradaReporteTRXI in=gson.fromJson(jin.toString() ,EntradaReporteTRXI.class);
		SalidaReporteTRXI out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	/***********************************************************************************************
	 *                                       MAIN
	 ***********************************************************************************************/
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();			
			String jsonRF = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":23,\"p_IdModulo\":\"FMM\",\"p_IdSolicitudAporteRescate\":[11407374,11407375,11407376,11407377,11407378,11407379,11407380,11407381,11407382,11407383,11407384,11407385,11407386]}";
					
			EntradaReporteTRXI Objin = gson.fromJson(jsonRF, EntradaReporteTRXI.class);
			
			GenerarDataReporteTRXI  demo=new GenerarDataReporteTRXI();				
 			
 			try {
 				SalidaReporteTRXI ObjOut=demo.logica(con, Objin);
 				//con.commit();
 				System.out.println(ObjOut.toString());
 			}
 			catch(Exception er) {
				er.printStackTrace();	
				con.rollback();
			}
 			con.rollback();
			
			
	    }
		System.out.println("== END ==");
	}
	
}