package cl.bcs.mesa.MRF;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.portafolio.ws.ConsultarPortafolioPorDocumentoIdentidadCodigoPortafolio;
import cl.bcs.portafolio.ws.ConsultarPortafolioPorDocumentoIdentidadCodigoPortafolio.EntradaConsultarPortafolioPorDocumentoIdentidadCodigoPortafolio;
import cl.bcs.portafolio.ws.ConsultarPortafolioPorDocumentoIdentidadCodigoPortafolio.EntradaDocumentoIdentidadCodigoPortafolio;
import cl.bcs.portafolio.ws.ConsultarPortafolioPorDocumentoIdentidadCodigoPortafolio.SalidaConsultarPortafolioPorDocumentoIdentidadCodigoPortafolio;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CargaMasivaArchivoOperacionTermino implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(CargaMasivaArchivoOperacionTermino.class);
	
	private final String SQL_SELECT_FOLIO_AGRUPADOR = loadResourceToString("cl/bcs/mesa/MRF/sql/CargaMasivaArchivoOperacionTermino_SelectFolioAgrupador.sql");
	private final String SQL_SELECT_INSTRUMENTOS = loadResourceToString("cl/bcs/mesa/MRF/sql/CargaMasivaArchivoOperacionTermino_SelectInstrumentos.sql");
	private final String SQL_SELECT_CARTERA = loadResourceToString("cl/bcs/mesa/MRF/sql/CargaMasivaArchivoOperacionTermino_SelectCartera.sql");
	private final String SQL_SELECT_MESA = loadResourceToString("cl/bcs/mesa/MRF/sql/CargaMasivaArchivoOperacionTermino_SelectMesa.sql");
	private final String SQL_SELECT_INDICADOR = loadResourceToString("cl/bcs/mesa/MRF/sql/CargaMasivaArchivoOperacionTermino_SelectIndicador.sql");
	private final String SQL_INSERT_ARCHIVO = loadResourceToString("cl/bcs/mesa/MRF/sql/CargaMasivaArchivoOperacionTermino_InsertArchivo.sql");
	private final String SQL_INSERT_CABECERA = loadResourceToString("cl/bcs/mesa/MRF/sql/CargaMasivaArchivoOperacionTermino_InsertCabecera.sql");
	private final String SQL_INSERT_DETALLE = loadResourceToString("cl/bcs/mesa/MRF/sql/CargaMasivaArchivoOperacionTermino_InsertDetalle.sql");
	
	public static class EntradaCargaMasivaArchivoOperacionTermino extends BcsPojo{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public String p_SimularOperacion;
		public String p_NombreArchivo;
		public Long p_IdMesa;
		public List<OperacionTermino> p_Operaciones;
		
		public EntradaCargaMasivaArchivoOperacionTermino() {
			this.p_Operaciones = new ArrayList<OperacionTermino>();
		}
		
	}
	
	public static class OperacionTermino extends BcsPojo{
		public String FolioAgrupadorCartera;
		public String DocumentoIdentidad;
		public String CodigoPortafolio;
		public String TipoOperacion;
		public String CondicionLiquidacion;
		public String TipoReajuste;
		public BigDecimal ValorInicial;
		public String Mercado;
		public Long SecuenciaCartera;
		public String Nemotecnico;
		public String Emisor;
		public String Boveda;
		public String ReajusteCartera;
		public String MonedaLiquidacion;
		public String FechaVencimiento;
		public BigDecimal Cantidad;
		public BigDecimal ValorRescate;
		public String Fungible;
		public BigDecimal Monto;
	}
	
	public static class OperacionTerminoCabecera extends OperacionTermino {
		public Long IdCargaMasivaDetalle;
		public Long IdCargaMasiva;
		public Long IdPortafolio;
		public List<OperacionTerminoDetalle> Operaciones;
		
		public OperacionTerminoCabecera() {
			this.Operaciones = new ArrayList<OperacionTerminoDetalle>();
		}
	}
	
	public static class OperacionTerminoDetalle extends OperacionTermino {
		public Long IdCargaMasiva;
		public Long IdCargaMasivaDetalleCabecera;
		public Long IdPortafolio;
		public BigDecimal Tasa;
		
		public OperacionTerminoDetalle() {
			this.Tasa = BigDecimal.ZERO;
		}
	}
		
	public static class Archivo extends BcsPojo{
		public Long IdCargaMasiva;
		public Long IdEmpresa;
		public Long IdUsuario;
		public Long IdMesa;
		public Long CantidadRegistros;
		public String NombreArchivo;
		public String FechaCarga;
	}
	
	public static class SalidaCargaMasivaArchivoOperacionTermino extends BcsPojo {
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		public List<Error> P_OUT_ERRORES;
		
		public SalidaCargaMasivaArchivoOperacionTermino() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_ERRORES = new ArrayList<Error>();
		}
	}
	
	public static class Error extends BcsPojo {
		public String id;
		public String tipo;
		public String descripcion;
		
		public Error(String id, String tipo, String descripcion) {
			this.id = id;
			this.tipo = tipo;
			this.descripcion = descripcion;
		}
	}
	
	public static class OperacionTerminoDetalleExistente extends BcsPojo {
		public String FolioAgrupadorCartera; 
	}
	
	public static class Instrumento extends BcsPojo {
		public String Nemotecnico;
		public String IdClasificacionTipoInstrumento;
		public BigDecimal CorteMinimo;
		public String IdMoneda;
		public String IdTipoReajuste;
		public String IdMonedaEmision;
		public java.sql.Date FechaVencimiento;
	}
	
	public static class Cartera extends BcsPojo {
		public Long FolioCartera;
		public String Nemotecnico;
		public String IdClasificacionTipoInstrumento;
		public BigDecimal CorteMinimo;
		public String IdMoneda;
		public String IdTipoReajuste;
		public String IdMonedaEmision;
		public java.sql.Date FechaVencimiento;
		public Long IdPortafolio;
	}
	
	public static class Indicador extends BcsPojo {
		public String AfectaCustodia;
		public Long IdPortafolio;
	}
	
	public static class Mesa extends BcsPojo {
		public Long IdCliente;
		public Long IdPortafolio;
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaCargaMasivaArchivoOperacionTermino jin = gson.fromJson(in.toString(), EntradaCargaMasivaArchivoOperacionTermino.class);
		SalidaCargaMasivaArchivoOperacionTermino out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaCargaMasivaArchivoOperacionTermino logica(Connection con, EntradaCargaMasivaArchivoOperacionTermino entrada) throws Exception{
		logger.info("INICIO::CargaMasivaArchivoOperacionTermino");
		SalidaCargaMasivaArchivoOperacionTermino out = new SalidaCargaMasivaArchivoOperacionTermino();
		
		Map<String, ConsultarPortafolioPorDocumentoIdentidadCodigoPortafolio.Portafolio> portafolios = obtenerPortafolios(con, entrada);
		
		out = validarCampos(con, entrada, portafolios);
		if(out.P_OUT_ERRORESCONTADOR > 0) {
			return out;
		}
		
		Map<String, Boolean> operacionesExistentesAgrupadasPorFolio = obtenerOperacionesExistentesAgrupadasPorFolio(con, entrada.p_Operaciones);
		List<OperacionTermino> operacionesPorProcesar = new ArrayList<OperacionTermino>();
		
		if(operacionesExistentesAgrupadasPorFolio.size() > 0) {
			out.P_OUT_MENSAJES = obtenerMensajeDeOperacionInvalidas(con, entrada, operacionesExistentesAgrupadasPorFolio);
		}
		
		operacionesPorProcesar = obtenerOperacionesPorProcesar(entrada.p_Operaciones, operacionesExistentesAgrupadasPorFolio);
		
		if(operacionesPorProcesar.size() > 0) {
			operacionesPorProcesar = repasarMonedas(con, operacionesPorProcesar);
			Map<String, List<OperacionTermino>> operacionesAgrupadas = obtenerOperacionesAgrupadas(operacionesPorProcesar);
			Archivo archivo = obtenerArchivo(con, entrada, operacionesAgrupadas);
			List<OperacionTerminoCabecera> cabeceras = obtenerCabeceras(con, archivo.IdCargaMasiva, operacionesAgrupadas, portafolios);
			insertarArchivo(con, archivo);
			insertarCabeceras(con, cabeceras);
			insertarDetalles(con, cabeceras);
		}
		
		logger.info("FIN::CargaMasivaArchivoOperacionTermino");
		return out;
	}
	
	private Map<String, ConsultarPortafolioPorDocumentoIdentidadCodigoPortafolio.Portafolio> obtenerPortafolios(Connection con, EntradaCargaMasivaArchivoOperacionTermino entrada) throws Exception{
		logger.trace("INICIO::CargaMasivaArchivoOperacionTermino.obtenerPortafolios");
		
		EntradaConsultarPortafolioPorDocumentoIdentidadCodigoPortafolio entradaConsultarPortafolioPorDocumentoIdentidadCodigoPortafolio = new EntradaConsultarPortafolioPorDocumentoIdentidadCodigoPortafolio();
		entradaConsultarPortafolioPorDocumentoIdentidadCodigoPortafolio.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaConsultarPortafolioPorDocumentoIdentidadCodigoPortafolio.p_IdUsuario = entrada.p_IdUsuario;
		entradaConsultarPortafolioPorDocumentoIdentidadCodigoPortafolio.p_IdModulo = entrada.p_IdModulo;
		entradaConsultarPortafolioPorDocumentoIdentidadCodigoPortafolio.p_UsaReglasNegocio = "S";
		
		entradaConsultarPortafolioPorDocumentoIdentidadCodigoPortafolio.p_DocumentosIdentidadCodigosPortafolio = entrada.p_Operaciones
				.stream()
				.map(operacion->{
					EntradaDocumentoIdentidadCodigoPortafolio entradaDocumentoIdentidadCodigoPortafolio = new EntradaDocumentoIdentidadCodigoPortafolio();
					entradaDocumentoIdentidadCodigoPortafolio.DocumentoIdentidad = operacion.DocumentoIdentidad;
					entradaDocumentoIdentidadCodigoPortafolio.CodigoPortafolio = operacion.CodigoPortafolio;
					return entradaDocumentoIdentidadCodigoPortafolio;
				})
				.collect(Collectors.toList());
		
		ConsultarPortafolioPorDocumentoIdentidadCodigoPortafolio consultarPortafolioPorDocumentoIdentidadCodigoPortafolio = new ConsultarPortafolioPorDocumentoIdentidadCodigoPortafolio();
		SalidaConsultarPortafolioPorDocumentoIdentidadCodigoPortafolio salidaConsultarPortafolioPorDocumentoIdentidadCodigoPortafolio = consultarPortafolioPorDocumentoIdentidadCodigoPortafolio.logica(con, entradaConsultarPortafolioPorDocumentoIdentidadCodigoPortafolio);
		
		Map<String, ConsultarPortafolioPorDocumentoIdentidadCodigoPortafolio.Portafolio> portafolios = salidaConsultarPortafolioPorDocumentoIdentidadCodigoPortafolio.P_OUT_PORTAFOLIOS.stream().collect(Collectors.toMap(portafolio->portafolio.DocumentoIdentidad + "_" + portafolio.CodigoPortafolio, portafolio->portafolio));
		
		logger.trace("FIN::CargaMasivaArchivoOperacionTermino.obtenerPortafolios");
		return portafolios;
	}
	
	private SalidaCargaMasivaArchivoOperacionTermino validarCampos(Connection con, EntradaCargaMasivaArchivoOperacionTermino entrada, Map<String, ConsultarPortafolioPorDocumentoIdentidadCodigoPortafolio.Portafolio> portafolios) throws Exception {
		logger.trace("INICIO::CargaMasivaArchivoOperacionTermino.validarCampos");
		SalidaCargaMasivaArchivoOperacionTermino out = new SalidaCargaMasivaArchivoOperacionTermino();
		
		Mesa mesa = obtenerDatosMesa(con, entrada.p_IdMesa);
		Map<String, Instrumento> instrumentosAgrupadosPorNemotecnicoClasificacion = obtenerInstrumentosAgrupadosPorNemotecnicoClasificacion(con, entrada.p_Operaciones);
		Map<Long, Cartera> carteraAgrupadosPorFolioCartera = obtenerCarteraAgrupadosPorFolioCartera(con, entrada.p_Operaciones);
		Map<Long, Indicador> indicadoresPorPortafolio = obtenerIndicadoresPorPortafolio(con, portafolios);
		java.sql.Date fechaSistema = Fecha.toDate(ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema"));
		
		if(mesa == null) {
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,"OPERACION_TERMINO_CARGA_NO_MESA", new HashMap<String,String>(1), con, entrada.p_IdEmpresa));
			return out;
		}
		
		for(OperacionTermino operacion : entrada.p_Operaciones) {
			Instrumento instrumento = instrumentosAgrupadosPorNemotecnicoClasificacion.get(operacion.Nemotecnico + "_" + operacion.Mercado);
			Cartera cartera = carteraAgrupadosPorFolioCartera.get(operacion.SecuenciaCartera);
			ConsultarPortafolioPorDocumentoIdentidadCodigoPortafolio.Portafolio portafolio = portafolios.get(operacion.DocumentoIdentidad + "_" + operacion.CodigoPortafolio);
			
			if(portafolio == null) {
				HashMap<String,String> parametrosMensaje = new HashMap<String,String>(2);
				parametrosMensaje.put("IdCargaMasivaDetalle", operacion.FolioAgrupadorCartera);
				MensajeSalida mensajeError = new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,"OPERACION_TERMINO_CARGA_NO_EXISTE_CLIENTE", parametrosMensaje, con, entrada.p_IdEmpresa);
				out.P_OUT_MENSAJES.add(mensajeError);
				out.P_OUT_ERRORES.add(new Error("0", MensajeSalida.MENSAJE_ERROR, mensajeError.descripcion));
			}
			
			if(portafolio != null) {
				Indicador indicadorPortafolio = indicadoresPorPortafolio.get(portafolio.IdPortafolio);
				if(!"ACT".equals(portafolio.IdEstadoCliente)) {
					HashMap<String,String> parametrosMensaje = new HashMap<String,String>(2);
					parametrosMensaje.put("IdCargaMasivaDetalle", operacion.FolioAgrupadorCartera);
					MensajeSalida mensajeError = new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,"OPERACION_TERMINO_CLIENTE_NO_ACTIVO", parametrosMensaje, con, entrada.p_IdEmpresa);
					out.P_OUT_MENSAJES.add(mensajeError);
					out.P_OUT_ERRORES.add(new Error(operacion.FolioAgrupadorCartera, MensajeSalida.MENSAJE_ERROR, mensajeError.descripcion));
				}
				if(!"ACT".equals(portafolio.IdEstadoPortafolio)) {
					HashMap<String,String> parametrosMensaje = new HashMap<String,String>(2);
					parametrosMensaje.put("IdCargaMasivaDetalle", operacion.FolioAgrupadorCartera);
					MensajeSalida mensajeError = new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,"OPERACION_TERMINO_PORTAFOLIO_NO_ACTIVO", parametrosMensaje, con, entrada.p_IdEmpresa);
					out.P_OUT_MENSAJES.add(mensajeError);
					out.P_OUT_ERRORES.add(new Error(operacion.FolioAgrupadorCartera, MensajeSalida.MENSAJE_ERROR, mensajeError.descripcion));
				}
				if("CT".equals(operacion.TipoOperacion) && operacion.SecuenciaCartera > 0 && "N".equals(indicadorPortafolio.AfectaCustodia)) {
					HashMap<String,String> parametrosMensaje = new HashMap<String,String>(2);
					parametrosMensaje.put("IdCargaMasivaDetalle", operacion.FolioAgrupadorCartera);
					MensajeSalida mensajeError = new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,"OPERACION_TERMINO_CLIENTE_OPERA_NOMINAL", parametrosMensaje, con, entrada.p_IdEmpresa);
					out.P_OUT_MENSAJES.add(mensajeError);
					out.P_OUT_ERRORES.add(new Error(operacion.FolioAgrupadorCartera, MensajeSalida.MENSAJE_ERROR, mensajeError.descripcion));
				}
				
			}
			
			if(operacion.SecuenciaCartera == 0 && instrumento == null) {
				HashMap<String,String> parametrosMensaje = new HashMap<String,String>(2);
				parametrosMensaje.put("IdCargaMasivaDetalle", operacion.FolioAgrupadorCartera);
				MensajeSalida mensajeError = new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,"OPERACION_TERMINO_INSTRUMENTO_INVALIDO", parametrosMensaje, con, entrada.p_IdEmpresa);
				out.P_OUT_MENSAJES.add(mensajeError);
				out.P_OUT_ERRORES.add(new Error(operacion.FolioAgrupadorCartera, MensajeSalida.MENSAJE_ERROR, mensajeError.descripcion));
			}
			
			if(operacion.SecuenciaCartera != 0 && cartera == null) {
				HashMap<String,String> parametrosMensaje = new HashMap<String,String>(3);
				parametrosMensaje.put("IdCargaMasivaDetalle", operacion.FolioAgrupadorCartera);
				parametrosMensaje.put("FolioCartera", String.valueOf(operacion.SecuenciaCartera));
				MensajeSalida mensajeError = new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,"OPERACION_TERMINO_CARGA_CARTERA_NO_ENCONTRADA", parametrosMensaje, con, entrada.p_IdEmpresa);
				out.P_OUT_MENSAJES.add(mensajeError);
				out.P_OUT_ERRORES.add(new Error(operacion.FolioAgrupadorCartera, MensajeSalida.MENSAJE_ERROR, mensajeError.descripcion));
			}
			
			if(cartera != null) {
				if("CT".equals(operacion.TipoOperacion) && !cartera.IdPortafolio.equals(portafolio.IdPortafolio)) {
					HashMap<String,String> parametrosMensaje = new HashMap<String,String>(3);
					parametrosMensaje.put("IdCargaMasivaDetalle", operacion.FolioAgrupadorCartera);
					parametrosMensaje.put("FolioCartera", String.valueOf(operacion.SecuenciaCartera));
					MensajeSalida mensajeError = new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,"OPERACION_TERMINO_CARGA_CARTERA_NO_ENCONTRADA", parametrosMensaje, con, entrada.p_IdEmpresa);
					out.P_OUT_MENSAJES.add(mensajeError);
					out.P_OUT_ERRORES.add(new Error(operacion.FolioAgrupadorCartera, MensajeSalida.MENSAJE_ERROR, mensajeError.descripcion));
				}
				if("VT".equals(operacion.TipoOperacion) && !cartera.IdPortafolio.equals(mesa.IdPortafolio)) {
					HashMap<String,String> parametrosMensaje = new HashMap<String,String>(3);
					parametrosMensaje.put("IdCargaMasivaDetalle", operacion.FolioAgrupadorCartera);
					parametrosMensaje.put("FolioCartera", String.valueOf(operacion.SecuenciaCartera));
					MensajeSalida mensajeError = new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,"OPERACION_TERMINO_CARGA_CARTERA_NO_ENCONTRADA", parametrosMensaje, con, entrada.p_IdEmpresa);
					out.P_OUT_MENSAJES.add(mensajeError);
					out.P_OUT_ERRORES.add(new Error(operacion.FolioAgrupadorCartera, MensajeSalida.MENSAJE_ERROR, mensajeError.descripcion));
				}
			}
			
			if("RF".equals(operacion.Mercado)) {
				if(instrumento != null && instrumento.CorteMinimo.compareTo(BigDecimal.ZERO) == 1 && operacion.Cantidad.remainder(instrumento.CorteMinimo, MathContext.DECIMAL128).compareTo(BigDecimal.ZERO) == 1) {
					HashMap<String,String> parametrosMensaje = new HashMap<String,String>(2);
					parametrosMensaje.put("IdCargaMasivaDetalle", operacion.FolioAgrupadorCartera);
					MensajeSalida mensajeError = new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,"OPERACION_TERMINO_CORTE_MINIMO_INVALIDO", parametrosMensaje, con, entrada.p_IdEmpresa);
					out.P_OUT_MENSAJES.add(mensajeError);
					out.P_OUT_ERRORES.add(new Error(operacion.FolioAgrupadorCartera, MensajeSalida.MENSAJE_ERROR, mensajeError.descripcion));
				}
				
				if(instrumento != null && fechaSistema.compareTo(instrumento.FechaVencimiento) == 1) {
					HashMap<String,String> parametrosMensaje = new HashMap<String,String>(2);
					parametrosMensaje.put("IdCargaMasivaDetalle", operacion.FolioAgrupadorCartera);
					MensajeSalida mensajeError = new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,"OPERACION_FECHA_VENCIMIENTO_MENOR_FECHA_SISTEMA", parametrosMensaje, con, entrada.p_IdEmpresa);
					out.P_OUT_MENSAJES.add(mensajeError);
					out.P_OUT_ERRORES.add(new Error(operacion.FolioAgrupadorCartera, MensajeSalida.MENSAJE_ERROR, mensajeError.descripcion));
				}
				
				if(cartera != null && cartera.CorteMinimo.compareTo(BigDecimal.ZERO) == 1 && operacion.Cantidad.remainder(cartera.CorteMinimo, MathContext.DECIMAL128).compareTo(BigDecimal.ZERO) == 1) {
					HashMap<String,String> parametrosMensaje = new HashMap<String,String>(2);
					parametrosMensaje.put("IdCargaMasivaDetalle", operacion.FolioAgrupadorCartera);
					MensajeSalida mensajeError = new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,"OPERACION_TERMINO_CORTE_MINIMO_INVALIDO", parametrosMensaje, con, entrada.p_IdEmpresa);
					out.P_OUT_MENSAJES.add(mensajeError);
					out.P_OUT_ERRORES.add(new Error(operacion.FolioAgrupadorCartera, MensajeSalida.MENSAJE_ERROR, mensajeError.descripcion));
				}
				
				if("S".equals(operacion.Fungible)) {
					HashMap<String,String> parametrosMensaje = new HashMap<String,String>(2);
					parametrosMensaje.put("IdCargaMasivaDetalle", operacion.FolioAgrupadorCartera);
					MensajeSalida mensajeError = new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,"OPERACION_TERMINO_CARGA_FUNGIBLE_INVALIDA", parametrosMensaje, con, entrada.p_IdEmpresa);
					out.P_OUT_MENSAJES.add(mensajeError);
					out.P_OUT_ERRORES.add(new Error(operacion.FolioAgrupadorCartera, MensajeSalida.MENSAJE_ERROR, mensajeError.descripcion));
				}
			}
			
			if("IF".equals(operacion.Mercado)) {
				if(operacion.SecuenciaCartera == 0 && operacion.Emisor.isEmpty()) {
					HashMap<String,String> parametrosMensaje = new HashMap<String,String>(2);
					parametrosMensaje.put("IdCargaMasivaDetalle", operacion.FolioAgrupadorCartera);
					MensajeSalida mensajeError = new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,"OPERACION_TERMINO_CARGA_EMISOR_INVALIDO", parametrosMensaje, con, entrada.p_IdEmpresa);
					out.P_OUT_MENSAJES.add(mensajeError);
					out.P_OUT_ERRORES.add(new Error(operacion.FolioAgrupadorCartera, MensajeSalida.MENSAJE_ERROR, mensajeError.descripcion));
				}
				if(operacion.SecuenciaCartera == 0 && operacion.TipoReajuste.isEmpty()) {
					HashMap<String,String> parametrosMensaje = new HashMap<String,String>(2);
					parametrosMensaje.put("IdCargaMasivaDetalle", operacion.FolioAgrupadorCartera);
					MensajeSalida mensajeError = new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,"OPERACION_TERMINO_CARGA_TIPO_REAJUSTE_INVALIDO", parametrosMensaje, con, entrada.p_IdEmpresa);
					out.P_OUT_MENSAJES.add(mensajeError);
					out.P_OUT_ERRORES.add(new Error(operacion.FolioAgrupadorCartera, MensajeSalida.MENSAJE_ERROR, mensajeError.descripcion));
				}
				if(operacion.SecuenciaCartera == 0 && operacion.MonedaLiquidacion.isEmpty()) {
					HashMap<String,String> parametrosMensaje = new HashMap<String,String>(2);
					parametrosMensaje.put("IdCargaMasivaDetalle", operacion.FolioAgrupadorCartera);
					MensajeSalida mensajeError = new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,"OPERACION_TERMINO_CARGA_MONEDA_LIQUIDACION_INVALIDA" ,parametrosMensaje, con, entrada.p_IdEmpresa);
					out.P_OUT_MENSAJES.add(mensajeError);
					out.P_OUT_ERRORES.add(new Error(operacion.FolioAgrupadorCartera, MensajeSalida.MENSAJE_ERROR, mensajeError.descripcion));
				}
				if(operacion.SecuenciaCartera == 0 && (operacion.FechaVencimiento.isEmpty() || "0001-01-01".equals(operacion.FechaVencimiento))) {
					HashMap<String,String> parametrosMensaje = new HashMap<String,String>(2);
					parametrosMensaje.put("IdCargaMasivaDetalle", operacion.FolioAgrupadorCartera);
					MensajeSalida mensajeError = new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,"OPERACION_TERMINO_CARGA_FECHA_VENCIMIENTO_INVALIDA", parametrosMensaje, con, entrada.p_IdEmpresa);
					out.P_OUT_MENSAJES.add(mensajeError);
					out.P_OUT_ERRORES.add(new Error(operacion.FolioAgrupadorCartera, MensajeSalida.MENSAJE_ERROR, mensajeError.descripcion));
				}
				if(operacion.ValorRescate.compareTo(BigDecimal.ZERO) != 1) {
					HashMap<String,String> parametrosMensaje = new HashMap<String,String>(2);
					parametrosMensaje.put("IdCargaMasivaDetalle", operacion.FolioAgrupadorCartera);
					MensajeSalida mensajeError = new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,"OPERACION_TERMINO_CARGA_VALOR_RESCATE_INVALIDA", parametrosMensaje, con, entrada.p_IdEmpresa);
					out.P_OUT_MENSAJES.add(mensajeError);
					out.P_OUT_ERRORES.add(new Error(operacion.FolioAgrupadorCartera, MensajeSalida.MENSAJE_ERROR, mensajeError.descripcion));
				}
				if(operacion.SecuenciaCartera == 0 && operacion.Fungible.isEmpty()) {
					HashMap<String,String> parametrosMensaje = new HashMap<String,String>(2);
					parametrosMensaje.put("IdCargaMasivaDetalle", operacion.FolioAgrupadorCartera);
					MensajeSalida mensajeError = new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,"OPERACION_TERMINO_CARGA_FUNGIBLE_INVALIDA", parametrosMensaje, con, entrada.p_IdEmpresa);
					out.P_OUT_MENSAJES.add(mensajeError);
					out.P_OUT_ERRORES.add(new Error(operacion.FolioAgrupadorCartera, MensajeSalida.MENSAJE_ERROR, mensajeError.descripcion));
				}
				if(operacion.SecuenciaCartera == 0 && !(operacion.FechaVencimiento.isEmpty() || "0001-01-01".equals(operacion.FechaVencimiento)) && fechaSistema.compareTo(Fecha.toDate(operacion.FechaVencimiento)) == 1) {
					HashMap<String,String> parametrosMensaje = new HashMap<String,String>(2);
					parametrosMensaje.put("IdCargaMasivaDetalle", operacion.FolioAgrupadorCartera);
					MensajeSalida mensajeError = new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,"OPERACION_FECHA_VENCIMIENTO_MENOR_FECHA_SISTEMA", parametrosMensaje, con, entrada.p_IdEmpresa);
					out.P_OUT_MENSAJES.add(mensajeError);
					out.P_OUT_ERRORES.add(new Error(operacion.FolioAgrupadorCartera, MensajeSalida.MENSAJE_ERROR, mensajeError.descripcion));
				}
			}
		}
		
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		
		logger.trace("FIN::CargaMasivaArchivoOperacionTermino.validarCampos");
		return out;
	}
	
	private Mesa obtenerDatosMesa(Connection con, Long idMesa) throws Exception {
		logger.trace("INICIO::CargaMasivaArchivoOperacionTermino.obtenerDatosMesa");
		HashMap<String, Object> parametros = new HashMap<String, Object>(2);
		parametros.put("IdMesa", idMesa);
		
		SQL sql = new SQL();
		List<Mesa> mesas = sql.query(con, SQL_SELECT_MESA, parametros, Mesa.class);
		Mesa mesa = mesas.stream().findFirst().orElse(new Mesa());
		logger.trace("FIN::CargaMasivaArchivoOperacionTermino.obtenerDatosMesa");
		return mesa;
	}
	
	private Map<String, Instrumento> obtenerInstrumentosAgrupadosPorNemotecnicoClasificacion(Connection con, List<OperacionTermino> operaciones) throws Exception {
		logger.trace("INICIO::CargaMasivaArchivoOperacionTermino.obtenerInstrumentosAgrupadosPorNemotecnicoClasificacion");
		
		HashMap<String, Object> parametros = new HashMap<String, Object>(2);
		
		List<String> nemotecnicos = operaciones
				.stream()
				.filter(detalle->detalle.SecuenciaCartera == 0)
				.map(detalle->detalle.Nemotecnico)
				.distinct()
				.collect(Collectors.toList());
		parametros.put("Nemotecnico", nemotecnicos);
		
		SQL sql = new SQL();
		List<Instrumento> instrumentos = sql.query(con, SQL_SELECT_INSTRUMENTOS, parametros, Instrumento.class);
		Map<String, Instrumento> instrumentosAgrupadosPorNemotecnicoClasificacion = instrumentos.stream().collect(Collectors.toMap(nemotecnico->nemotecnico.Nemotecnico + "_" + nemotecnico.IdClasificacionTipoInstrumento, nemotecnico->nemotecnico));
		
		logger.trace("FIN::CargaMasivaArchivoOperacionTermino.obtenerInstrumentosAgrupadosPorNemotecnicoClasificacion");
		return instrumentosAgrupadosPorNemotecnicoClasificacion;
	}
	
	private Map<Long, Cartera> obtenerCarteraAgrupadosPorFolioCartera(Connection con, List<OperacionTermino> operaciones) throws Exception {
		logger.trace("INICIO::CargaMasivaArchivoOperacionTermino.obtenerCarteraAgrupadosPorFolioCartera");
		
		HashMap<String, Object> parametros = new HashMap<String, Object>(2);
		
		List<Long> secuenciaCarteras = operaciones
				.stream()
				.filter(detalle->detalle.SecuenciaCartera != 0)
				.map(detalle->detalle.SecuenciaCartera)
				.distinct()
				.collect(Collectors.toList());
		parametros.put("SecuenciaCartera", secuenciaCarteras);
		
		SQL sql = new SQL();
		List<Cartera> carteras = sql.query(con, SQL_SELECT_CARTERA, parametros, Cartera.class);
		Map<Long, Cartera> carteraAgrupadosPorFolioCartera = carteras.stream().collect(Collectors.toMap(cartera->cartera.FolioCartera, cartera->cartera));
		
		logger.trace("FIN::CargaMasivaArchivoOperacionTermino.obtenerCarteraAgrupadosPorFolioCartera");
		return carteraAgrupadosPorFolioCartera;
	}
	
	private Map<Long, Indicador> obtenerIndicadoresPorPortafolio(Connection con, Map<String, ConsultarPortafolioPorDocumentoIdentidadCodigoPortafolio.Portafolio> portafolios) throws Exception {
		logger.trace("INICIO::CargaMasivaArchivoOperacionTermino.obtenerIndicadoresPorPortafolio");
		
		HashMap<String, Object> parametros = new HashMap<String, Object>(2);
		
		List<Long> idPortafolios = portafolios.entrySet()
				.stream()
				.map(portafolio->portafolio.getValue().IdPortafolio)
				.distinct()
				.collect(Collectors.toList());
		parametros.put("IdPortafolio", idPortafolios);
		
		SQL sql = new SQL();
		List<Indicador> indicadores = sql.query(con, SQL_SELECT_INDICADOR, parametros, Indicador.class);
		Map<Long, Indicador> indicadoresPorPortafolio = indicadores.stream().collect(Collectors.toMap(indicador->indicador.IdPortafolio, indicador->indicador));
		
		logger.trace("FIN::CargaMasivaArchivoOperacionTermino.obtenerIndicadoresPorPortafolio");
		return indicadoresPorPortafolio;
	}
	
	private Map<String, Boolean> obtenerOperacionesExistentesAgrupadasPorFolio(Connection con, List<OperacionTermino> operaciones) throws Exception {
		logger.trace("INICIO::CargaMasivaArchivoOperacionTermino.obtenerOperacionesTerminoDetalleExistentes");
		
		List<String> folioOperaciones = operaciones
				.stream()
				.map(operacion->operacion.FolioAgrupadorCartera)
				.distinct()
				.collect(Collectors.toList());
		HashMap<String, Object> parametros = new HashMap<String, Object>();
		parametros.put("FolioAgrupadorCartera", folioOperaciones);
		
		SQL sql = new SQL();
		List<OperacionTerminoDetalleExistente> operacionesExistentes = sql.query(con, SQL_SELECT_FOLIO_AGRUPADOR, parametros, OperacionTerminoDetalleExistente.class);
		Map<String, Boolean> operacionesExistentesAgrupadasPorFolio = operacionesExistentes.stream().collect(Collectors.toMap(operacion->operacion.FolioAgrupadorCartera, operacion->true));
		
		logger.trace("FIN::CargaMasivaArchivoOperacionTermino.obtenerOperacionesTerminoDetalleExistentes");
		return operacionesExistentesAgrupadasPorFolio;
	}
	
	private List<MensajeSalida> obtenerMensajeDeOperacionInvalidas(Connection con, EntradaCargaMasivaArchivoOperacionTermino entrada, Map<String, Boolean> operacionesExistentesAgrupadasPorFolio) throws Exception {
		logger.trace("INICIO::CargaMasivaArchivoOperacionTermino.obtenerMensajeDeOperacionInvalidas");
		
		List<MensajeSalida> mensajesSalida = new ArrayList<MensajeSalida>();
		for(OperacionTermino operacion : entrada.p_Operaciones) {
			Boolean existeFolio = operacionesExistentesAgrupadasPorFolio.get(operacion.FolioAgrupadorCartera);
			if(existeFolio != null) {
				HashMap<String,String> parametrosMensaje = new HashMap<String,String>(2);
				parametrosMensaje.put("FolioAgrupadorCartera", operacion.FolioAgrupadorCartera);
				mensajesSalida.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ADVERTENCIA,"OPERACION_TERMINO_CARGA_FOLIO_EXISTE", parametrosMensaje, con, entrada.p_IdEmpresa));
			}
		}
		
		logger.trace("FIN::CargaMasivaArchivoOperacionTermino.obtenerMensajeDeOperacionInvalidas");
		return mensajesSalida;
	}
	
	private List<OperacionTermino> obtenerOperacionesPorProcesar(List<OperacionTermino> operaciones, Map<String, Boolean> operacionesExistentesAgrupadasPorFolio) throws Exception {
		logger.trace("INICIO::CargaMasivaArchivoOperacionTermino.obtenerOperacionesPorProcesar");
		
		List<OperacionTermino> operacionesPorProcesar = new ArrayList<OperacionTermino>();
		for(OperacionTermino operacion : operaciones) {
			Boolean existeFolio = operacionesExistentesAgrupadasPorFolio.get(operacion.FolioAgrupadorCartera);
			if(existeFolio == null) {
				operacionesPorProcesar.add(operacion);
			}
		}
		
		logger.trace("FIN::CargaMasivaArchivoOperacionTermino.obtenerOperacionesPorProcesar");
		return operacionesPorProcesar;
	}
	
	private List<OperacionTermino> repasarMonedas(Connection con, List<OperacionTermino> operaciones) throws Exception {
		logger.trace("INICIO::CargaMasivaArchivoOperacionTermino.repasarMonedas");
		
		Map<String, Instrumento> instrumentosAgrupadosPorNemotecnicoClasificacion = obtenerInstrumentosAgrupadosPorNemotecnicoClasificacion(con, operaciones);
		Map<Long, Cartera> carteraAgrupadosPorFolioCartera = obtenerCarteraAgrupadosPorFolioCartera(con, operaciones);
		
		for(OperacionTermino operacion : operaciones) {
			if(operacion.MonedaLiquidacion.isEmpty()) {
				Instrumento instrumento = instrumentosAgrupadosPorNemotecnicoClasificacion.get(operacion.Nemotecnico + "_" + operacion.Mercado);
				Cartera cartera = carteraAgrupadosPorFolioCartera.get(operacion.SecuenciaCartera);
				if(operacion.SecuenciaCartera == 0 && instrumento != null) {
					operacion.MonedaLiquidacion = instrumento.IdMoneda;
				}
				if(operacion.SecuenciaCartera != 0 && cartera != null) {
					operacion.MonedaLiquidacion = cartera.IdMoneda;
				}
			}
		}
		
		logger.trace("FIN::CargaMasivaArchivoOperacionTermino.repasarMonedas");
		return operaciones;
	}
	
	
	private Archivo obtenerArchivo(Connection con, EntradaCargaMasivaArchivoOperacionTermino entrada, Map<String, List<OperacionTermino>> operacionesAgrupadas) throws Exception{
		logger.trace("INICIO::CargaMasivaArchivoOperacionTermino.obtenerArchivo");
		
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		
		Long idCargaMasiva = Utils.getSecuencia(con, "BSCL.Mesa.BD.Secuencias::CargaMasiva", 1L).get(0);
		
		Archivo archivo = new Archivo();
		archivo.IdCargaMasiva = idCargaMasiva;
		archivo.IdEmpresa = entrada.p_IdEmpresa;
		archivo.IdUsuario = entrada.p_IdUsuario;
		archivo.CantidadRegistros = Long.valueOf(operacionesAgrupadas.size());
		archivo.NombreArchivo = entrada.p_NombreArchivo;
		archivo.IdMesa = entrada.p_IdMesa;
		archivo.FechaCarga = fechaSistema;
		
		logger.trace("FIN::CargaMasivaArchivoOperacionTermino.obtenerArchivo");
		
		return archivo;
	}
	
	private void insertarArchivo(Connection con, Archivo archivo) throws Exception{
		logger.trace("INICIO::CargaMasivaArchivoOperacionTermino.insertarArchivo");
		
		List<Archivo> archivos = Arrays.asList(archivo);
		SQL sql = new SQL();
		sql.batch(con, SQL_INSERT_ARCHIVO, archivos, Archivo.class);
		
		logger.trace("FIN::CargaMasivaArchivoOperacionTermino.insertarArchivo");
	}
	
	private Map<String, List<OperacionTermino>> obtenerOperacionesAgrupadas(List<OperacionTermino> operaciones) throws Exception {
		logger.trace("INICIO::CargaMasivaArchivoOperacionTermino.obtenerOperacionesAgrupadas");
		
		Map<String, List<OperacionTermino>> operacionesAgrupadas = operaciones.stream().collect(Collectors.groupingBy(operacion->operacion.FolioAgrupadorCartera));
		
		logger.trace("FIN::CargaMasivaArchivoOperacionTermino.obtenerOperacionesAgrupadas");
		
		return operacionesAgrupadas;
	}
	
	private List<OperacionTerminoCabecera> obtenerCabeceras(Connection con, Long idCargaMasiva, Map<String, List<OperacionTermino>> operacionesAgrupadas, Map<String, ConsultarPortafolioPorDocumentoIdentidadCodigoPortafolio.Portafolio> portafolios) throws Exception {
		logger.trace("INICIO::CargaMasivaArchivoOperacionTermino.obtenerCabeceras");
		List<OperacionTerminoCabecera> operacionesCabecera = new ArrayList<OperacionTerminoCabecera>();
		List<Long> secuencias = Utils.getSecuencia(con, "BSCL.Mesa.BD.Secuencias::CargaMasivaDetalle", Long.valueOf(operacionesAgrupadas.size()));
		Integer index = 0;
		
		for(Entry<String, List<OperacionTermino>> operacion : operacionesAgrupadas.entrySet()) {
			OperacionTerminoCabecera operacionCabecera = new OperacionTerminoCabecera();
			operacionCabecera.IdCargaMasivaDetalle = secuencias.get(index);
			operacionCabecera.IdCargaMasiva = idCargaMasiva;
			operacionCabecera.Operaciones = operacion.getValue().stream().map(detalle-> {
				ConsultarPortafolioPorDocumentoIdentidadCodigoPortafolio.Portafolio portafolio = portafolios.get(detalle.DocumentoIdentidad + "_" + detalle.CodigoPortafolio);
				OperacionTerminoDetalle operacionDetalle = new OperacionTerminoDetalle();
				operacionDetalle.IdCargaMasiva = operacionCabecera.IdCargaMasiva;
				operacionDetalle.IdCargaMasivaDetalleCabecera = operacionCabecera.IdCargaMasivaDetalle;
				operacionDetalle.IdPortafolio = portafolio == null ? 0L : portafolio.IdPortafolio;
				operacionDetalle.FolioAgrupadorCartera = detalle.FolioAgrupadorCartera;
				operacionDetalle.DocumentoIdentidad = detalle.DocumentoIdentidad;
				operacionDetalle.CodigoPortafolio = detalle.CodigoPortafolio;
				operacionDetalle.TipoOperacion = detalle.TipoOperacion;
				operacionDetalle.CondicionLiquidacion = detalle.CondicionLiquidacion;
				operacionDetalle.TipoReajuste = detalle.TipoReajuste;
				operacionDetalle.ValorInicial = detalle.ValorInicial;
				operacionDetalle.Mercado = detalle.Mercado;
				operacionDetalle.SecuenciaCartera = detalle.SecuenciaCartera;
				operacionDetalle.Nemotecnico = detalle.Nemotecnico;
				operacionDetalle.Emisor = detalle.Emisor;
				operacionDetalle.Boveda = detalle.Boveda;
				operacionDetalle.ReajusteCartera = detalle.ReajusteCartera;
				operacionDetalle.MonedaLiquidacion = detalle.MonedaLiquidacion;
				operacionDetalle.FechaVencimiento = detalle.FechaVencimiento;
				operacionDetalle.Cantidad = detalle.Cantidad;
				operacionDetalle.ValorRescate = detalle.ValorRescate;
				operacionDetalle.Fungible = detalle.Fungible;
				operacionDetalle.Monto = detalle.Monto;
				return operacionDetalle;
			}).collect(Collectors.toList());
			
			OperacionTerminoDetalle operacionDetalle = operacionCabecera.Operaciones.get(0);
			operacionCabecera.IdPortafolio = operacionDetalle.IdPortafolio;
			operacionCabecera.FolioAgrupadorCartera = operacionDetalle.FolioAgrupadorCartera;
			operacionCabecera.DocumentoIdentidad = operacionDetalle.DocumentoIdentidad;
			operacionCabecera.CodigoPortafolio = operacionDetalle.CodigoPortafolio;
			operacionCabecera.TipoOperacion = operacionDetalle.TipoOperacion;
			operacionCabecera.TipoReajuste = operacionDetalle.TipoReajuste;
			operacionCabecera.CondicionLiquidacion = operacionDetalle.CondicionLiquidacion;
			operacionCabecera.ValorInicial = operacionDetalle.ValorInicial;
			operacionCabecera.MonedaLiquidacion = operacionDetalle.MonedaLiquidacion;
			operacionesCabecera.add(operacionCabecera);
			index++;
		}
		
		logger.trace("FIN::CargaMasivaArchivoOperacionTermino.obtenerCabeceras");
		return operacionesCabecera;
	}
	
	private void insertarCabeceras(Connection con, List<OperacionTerminoCabecera> cabeceras) throws Exception{
		logger.trace("INICIO::CargaMasivaArchivoOperacionTermino.insertarCabeceras");
		
		SQL sql = new SQL();
		sql.batch(con, SQL_INSERT_CABECERA, cabeceras, OperacionTerminoCabecera.class);
		
		logger.trace("FIN::CargaMasivaArchivoOperacionTermino.insertarCabeceras");
	}
	
	private void insertarDetalles(Connection con, List<OperacionTerminoCabecera> cabeceras) throws Exception{
		logger.trace("INICIO::CargaMasivaArchivoOperacionTermino.insertarDetalles");
		
		List<OperacionTerminoDetalle> detalles = new ArrayList<OperacionTerminoDetalle>();
		for(OperacionTerminoCabecera cabecera : cabeceras) {
			detalles.addAll(cabecera.Operaciones);
		}
		
		SQL sql = new SQL();
		sql.batch(con, SQL_INSERT_DETALLE, detalles, OperacionTerminoDetalle.class);
		
		logger.trace("FIN::CargaMasivaArchivoOperacionTermino.insertarDetalles");
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");		                  
		try (Connection con = PlataformaUtils.getDESAConnection()) {
		Gson gson = Utils.getGsonBuilder();			
		
		String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":9,\"p_IdModulo\":\"MRF\",\"p_UsaReglasNegocio\":\"S\",\"p_SimularOperacion\":\"N\",\"p_NombreArchivo\":\"CargaVTMerrillDESA.txt\",\"p_IdMesa\":24,\"p_Operaciones\":[{\"FolioAgrupadorCartera\":\"A64\",\"DocumentoIdentidad\":\"15298053-1\",\"CodigoPortafolio\":\"1\",\"TipoOperacion\":\"CT\",\"CondicionLiquidacion\":\"PH\",\"TipoReajuste\":\"CLP\",\"ValorInicial\":0,\"Mercado\":\"RF\",\"SecuenciaCartera\":0,\"Nemotecnico\":\"BTP0450326\",\"Emisor\":\"\",\"Boveda\":\"COR\",\"ReajusteCartera\":\"\",\"MonedaLiquidacion\":\"\",\"FechaVencimiento\":\"\",\"Cantidad\":530000000,\"ValorRescate\":0,\"Fungible\":\"\",\"Monto\":514290000,\"MENSAJEVALIDACION\":\"OK\"}]}";
		EntradaCargaMasivaArchivoOperacionTermino Objin = gson.fromJson(json, EntradaCargaMasivaArchivoOperacionTermino.class);
		CargaMasivaArchivoOperacionTermino demo = new CargaMasivaArchivoOperacionTermino();				
		try {
			SalidaCargaMasivaArchivoOperacionTermino ObjOut = demo.logica(con, Objin);
			con.rollback();
			System.out.println(ObjOut.toString());
		}
		catch(Exception er) {
			er.printStackTrace();	
			con.rollback();
		}
						
    	};
		System.out.println("== END ==");
	}

}
