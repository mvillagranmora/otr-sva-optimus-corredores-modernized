package cl.bcs.tesoreria;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.opencsv.CSVWriter;

import cl.bcs.plataforma.ftp.FileManager.Cabecera;
import cl.bcs.plataforma.ftp.FileManager.Detalle;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.tesoreria.CrearIngresoEgreso.CrearIngresoEgreso_Entrada;
import cl.bcs.tesoreria.CrearIngresoEgreso.CrearIngresoEgreso_Salida;
import cl.bcs.tesoreria.CrearIngresoEgreso.FormaDePago_Entrada;
import cl.bcs.tesoreria.CrearIngresoEgreso.IngresoEgreso_Entrada;
import cl.bcs.tesoreria.CrearIngresoEgreso.ResultadoProceso_Salida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CargaMasivaIngresoEgresoDAPFFMM implements JavaCall {
	static final Logger logger = LogManager.getLogger(CargaMasivaIngresoEgresoDAPFFMM.class);
	
	private final String SQL_INSERT_CABECERA_ARCHIVO_SALIDA = loadResourceToString("cl/bcs/tesoreria/sql/CargaMasivaIngresoEgresoDAPFFMM_InsertCabecera.sql");
	private final String SQL_INSERT_DETALLES_ARCHIVO_SALIDA = loadResourceToString("cl/bcs/tesoreria/sql/CargaMasivaIngresoEgresoDAPFFMM_InsertDetalles.sql");
	private final String SQL_SELECT_CABECERA = loadResourceToString("cl/bcs/tesoreria/sql/CargaMasivaIngresoEgresoDAPFFMM_SelectCabeceras.sql");
	private final String SQL_SELECT_DETALLE = loadResourceToString("cl/bcs/tesoreria/sql/CargaMasivaIngresoEgresoDAPFFMM_SelectDetalles.sql");
	private final String SQL_INSERT_TABLA_PASO = loadResourceToString("cl/bcs/tesoreria/sql/CargaMasivaIngresoEgresoDAPFFMM_InsertTablaPaso.sql");
	private final String SQL_UPDATE_CABECERA = loadResourceToString("cl/bcs/tesoreria/sql/CargaMasivaIngresoEgresoDAPFFMM_UpdateCabecera.sql");
	private final String SQL_UPDATE_DETALLES = loadResourceToString("cl/bcs/tesoreria/sql/CargaMasivaIngresoEgresoDAPFFMM_UpdateDetalles.sql");
	private final String SQL_SELECT_IDPERSONA_IDPORTAFOLIO = loadResourceToString("cl/bcs/tesoreria/sql/CargaMasivaIngresoEgresoDAPFFMM_SelectIdPersonaIdPortafolioSegunTablaPaso.sql");
	private final String SQL_SELECT_CONCEPTOS_SISTEMA = loadResourceToString("cl/bcs/tesoreria/sql/CargaMasivaIngresoEgresoDAPFFMM_SelectConceptosSistema.sql");
	private final String SQL_SELECT_TIPO_MOVIMIENTO_TESORERIA = loadResourceToString("cl/bcs/tesoreria/sql/CargaMasivaIngresoEgresoDAPFFMM_SelectTipoMovimientoTesoreria.sql");
	
	private static final String BASE_CODIGO_TIPO = "CARGA_MASIVA_INGRESO_EGRESO_";
	private static final String BASE_NOMBRE_ARCHIVO_SALIDA = "CDB_Validacion_IngresoEgreso_";
	
	public static class EntradaCargaMasivaIngresoEgresoDAPFFMM extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_TipoArchivoCarga;
		public String p_EnviarArchivoACasilla;
		public String p_FechaCarga;
		public String p_IdModulo = "TES";
		public String p_SimularOperacion = "N";
		public String p_UsaReglasNegocio = "S";
		
		public EntradaCargaMasivaIngresoEgresoDAPFFMM () {
			this.p_IdEmpresa = 1L;
			this.p_IdUsuario = 0L;
			this.p_IdModulo = "TES";
			this.p_SimularOperacion = "N";
			this.p_UsaReglasNegocio = "S";
		}
	}

	public static class SalidaCargaMasivaIngresoEgresoDAPFFMM extends BcsPojo {
		public Long P_OUT_ERRORESCONTADOR;
		public String P_OUT_RUTA_ARCHIVO;
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_REGISTROSINGRESADOSCONTADOR;
		
		public SalidaCargaMasivaIngresoEgresoDAPFFMM () {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_REGISTROSINGRESADOSCONTADOR = 0L;
			this.P_OUT_RUTA_ARCHIVO = "";
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
		
	}
	
	public static class EjecucionCarga extends BcsPojo{
		public String FechaEjecucion;
		public String IdTipoArchivoCargaMasiva;
		public Long IdEmpresa;
		
		public EjecucionCarga () {
			this.FechaEjecucion = "0001-01-01";
			this.IdTipoArchivoCargaMasiva = "";
			this.IdEmpresa = 0L;
		}
		
		public EjecucionCarga (String fechaEjecucion, String idTipoArchivoCargaMasiva, Long idEmpresa) {
			this.FechaEjecucion = fechaEjecucion;
			this.IdTipoArchivoCargaMasiva = idTipoArchivoCargaMasiva;
			this.IdEmpresa = idEmpresa;
		}
	}
	
	public static class PersonaPortafolio extends DocumentoIdentidadCodigoPortafolio{
		public Long IdPersona;
		public Long IdPortafolio;
		
		public PersonaPortafolio (){
			this.IdPersona = 0L;
			this.IdPortafolio = 0L;
		}
	}

	public static class DocumentoIdentidadCodigoPortafolio extends BcsPojo{
		public String IdTipoDocumentoIdentidad;
		public String DocumentoIdentidad;
		public String CodigoPortafolio;
		public Long IdTrabajo;
		
		public DocumentoIdentidadCodigoPortafolio (){
			this.IdTipoDocumentoIdentidad = "";
			this.DocumentoIdentidad = "";
			this.CodigoPortafolio = "";
			this.IdTrabajo = 0L;
		}
	}
	
	public static class Concepto extends BcsPojo {
		public String IdConcepto;
		public String ConfiguradoParaTES;
		public String ConfiguradoParaCTA;
		public String UsoTES;
		
		public Concepto () {
			this.IdConcepto = "";
			this.ConfiguradoParaTES = "NO";
			this.ConfiguradoParaCTA = "NO";
			this.UsoTES = "";
		}
	}
	
	public static class TipoMovimiento extends BcsPojo {
		public String IdTipoMovimiento;
		
		public TipoMovimiento () {
			this.IdTipoMovimiento = "";
		}
	}
	
	public SalidaCargaMasivaIngresoEgresoDAPFFMM logica(Connection con, EntradaCargaMasivaIngresoEgresoDAPFFMM entrada) throws Exception {
		logger.info("INICIO::CargaMasivaIngresoEgresoDAPFFMM");
		long inicio = System.currentTimeMillis();
		SalidaCargaMasivaIngresoEgresoDAPFFMM salida = new SalidaCargaMasivaIngresoEgresoDAPFFMM();
		
		SQL sql = new SQL();
		List<Cabecera> cabeceras = getCabeceras(con, entrada.p_TipoArchivoCarga,entrada.p_FechaCarga,"PEN");
		
		if(cabeceras.isEmpty()) {
			logger.info("INFO::CargaMasivaIngresoEgresoDAPFFMM - No se encontraron archivos para cargar");
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR, "UI_CARGA_MASIVA_DAPFFMM_SIN_ARCHIVO_PARA_CARGAR",new HashMap<String, String>(0),con,entrada.p_IdEmpresa));
			salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
			return salida;
		}
		
		try {
			cabeceras.stream().forEach(cab -> cab.IdEstado = "ASI");
			sql.batch(con,SQL_UPDATE_CABECERA , cabeceras ,Cabecera.class);
			
			List<String[]> lineasArchivoSalida = new ArrayList<>();
			
			List<Detalle> detalles = getDetalles(con, cabeceras);
			if(detalles.isEmpty()) {
				logger.info("INFO::CargaMasivaIngresoEgresoDAPFFMM - No se encontraron detalles, el archivo se encuentra vacio");
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR, "UI_CARGA_MASIVA_DAPFFMM_ARCHIVO_CARGA_VACIO",new HashMap<String, String>(0),con,entrada.p_IdEmpresa));
				salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
			}else {
				HashMap<Long, String> hashValidacionDetalle = validarDetalles(con, detalles);
				List<Detalle> detallesFiltrados = filtrarDetalles(detalles, hashValidacionDetalle);
				
				logger.info("INFO::CargaMasivaIngresoEgresoDAPFFMM - Se obtuvieron "+ detalles.size() + " detalles: " + detalles);

				CrearIngresoEgreso_Salida salidaCrearIngresos = new CrearIngresoEgreso_Salida();
				CrearIngresoEgreso_Salida salidaCrearEgreso = new CrearIngresoEgreso_Salida();
				
				if(!detallesFiltrados.isEmpty()) {
					List<IngresoEgreso_Entrada> ingresosAGenerar = obtenerIngresosEgresosAGenerar(con, detallesFiltrados,"MOVING");
					List<FormaDePago_Entrada> formasDePagoAGenerarIngresos = obtenerFormasDePagoAGenerar(detallesFiltrados,"MOVING");

					List<IngresoEgreso_Entrada> egresosAGenerar = obtenerIngresosEgresosAGenerar(con, detallesFiltrados, "MOVEGR");
					List<FormaDePago_Entrada> formasDePagoAGenerarEgresos = obtenerFormasDePagoAGenerar(detallesFiltrados, "MOVEGR");
					
					if(!ingresosAGenerar.isEmpty()) {
						salidaCrearIngresos = crearIngresosEgresos(con, entrada, ingresosAGenerar, formasDePagoAGenerarIngresos);
						logger.info("INFO::CargaMasivaIngresoEgresoDAPFFMM - Salida CrearIngresoEgreso: "+ salidaCrearIngresos);
					}
					
					if(!egresosAGenerar.isEmpty()) {
						salidaCrearEgreso = crearIngresosEgresos(con, entrada, egresosAGenerar, formasDePagoAGenerarEgresos);
						logger.info("INFO::CargaMasivaIngresoEgresoDAPFFMM - Salida CrearIngresoEgreso: "+ salidaCrearEgreso);
					}
				}

				lineasArchivoSalida.addAll(armarLineasArchivoSalida(detalles,salidaCrearIngresos,salidaCrearEgreso,hashValidacionDetalle));
				
				if(!lineasArchivoSalida.isEmpty()) {
					String nombreArchivoSalida = BASE_NOMBRE_ARCHIVO_SALIDA + entrada.p_TipoArchivoCarga +"_"+ entrada.p_FechaCarga.replace("-", "");
					
					String rutaArchivo = agregarArchivoSalida(con, entrada, lineasArchivoSalida, nombreArchivoSalida);
					salida.P_OUT_RUTA_ARCHIVO = rutaArchivo;
					if("".equals(salida.P_OUT_RUTA_ARCHIVO)) {
						cabeceras.stream().forEach(cab -> cab.IdEstado = "PEN");
						long fin = System.currentTimeMillis();
						logger.info("FIN::CargaMasivaIngresoEgresoDAPFFMM", fin - inicio);
						return salida;
					}
					
					Long contadorRegistrosProcesados = lineasArchivoSalida.stream().filter(cols -> !"0".equals(cols[1])).count();
					salida.P_OUT_REGISTROSINGRESADOSCONTADOR = contadorRegistrosProcesados;
					
					actualizarDetallesArchivoEntradaSFTP(con, detalles, lineasArchivoSalida);
					agregarArchivoSalidaSFTP(con, entrada, lineasArchivoSalida, nombreArchivoSalida);
				}

				salida.P_OUT_MENSAJES.addAll(salidaCrearIngresos.P_OUT_MENSAJES);
				salida.P_OUT_MENSAJES.addAll(salidaCrearEgreso.P_OUT_MENSAJES);
				salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);

				logger.info("INFO::CargaMasivaIngresoEgresoDAPFFMM - Se agrega el Control de Proceso de la Carga del Archivo "+ entrada.p_TipoArchivoCarga+" para el dia "+ entrada.p_FechaCarga);
				
			}
			
			cabeceras.stream().forEach(cab -> cab.IdEstado = "PRO");
		} catch (Exception ex) {
			cabeceras.stream().forEach(cab -> cab.IdEstado = "PEN");
			logger.error("ERROR", ex.getMessage());
			ex.printStackTrace();
			throw ex;
		} finally {
			sql.batch(con,SQL_UPDATE_CABECERA , cabeceras ,Cabecera.class);		
		}
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::CargaMasivaIngresoEgresoDAPFFMM", fin - inicio);
		return salida;
	}
	
	private void actualizarDetallesArchivoEntradaSFTP(Connection con, List<Detalle> detalles, List<String[]> lineasArchivoSalida) throws Exception {
		logger.info("INICIO::CargaMasivaIngresoEgresoDAPFFMM.actualizarDetallesArchivoEntradaSFTP");
		long inicio = System.currentTimeMillis();
		
		Map <String,String[]> hash = lineasArchivoSalida.stream().filter( linea -> !"Rechazado".equals(linea[2]) ).collect(Collectors.toMap(lineas -> lineas[0], lineas -> lineas));
		for(Detalle detalle : detalles) {
			List<String> lineas = new ArrayList<String>(Arrays.asList(detalle.Linea.split(";")));
			if(!lineas.isEmpty()) {
				String NumeroReferencia = lineas.get(0);
				String[] lineaArchivoSalida = hash.get(NumeroReferencia);
				if(lineaArchivoSalida != null) {
					detalle.IdTipoEntidad =  "0".equals(lineaArchivoSalida[1]) ? "" : "IdComprobanteTesoreria";
					detalle.IdEntidad = Long.parseLong(lineaArchivoSalida[1]);					
				}
			}
		}
		
		SQL sql = new SQL();
		
		sql.batch(con,SQL_UPDATE_DETALLES , detalles ,Detalle.class);
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::CargaMasivaIngresoEgresoDAPFFMM.actualizarDetallesArchivoEntradaSFTP", fin - inicio);
	}

	private List<Detalle> filtrarDetalles(List<Detalle> detalles, HashMap<Long, String> hashValidacionDetalles) {
		logger.info("INICIO::CargaMasivaIngresoEgresoDAPFFMM.filtrarDetalles");
		long inicio = System.currentTimeMillis();
		
		List<Detalle> detallesFiltrados = new ArrayList<Detalle>();
		
		for(Detalle detalle :detalles) {
			List<String> lineas = Arrays.stream(detalle.Linea.split(";", -1)).filter(tok -> !tok.isEmpty()).collect(Collectors.toList());
			
			Long numeroReferencia = Long.valueOf(detalles.indexOf(detalle));
			
			String validacion = hashValidacionDetalles.get(numeroReferencia) != null ? hashValidacionDetalles.get(numeroReferencia) : "";
			if("".equals(validacion) && !lineas.isEmpty()) {
				detallesFiltrados.add(detalle);
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::CargaMasivaIngresoEgresoDAPFFMM.filtrarDetalles", fin - inicio);
		return detallesFiltrados;
	}
	
	private HashMap<Long, String> validarDetalles(Connection con, List<Detalle> detalles) throws Exception {
		logger.info("INICIO::CargaMasivaIngresoEgresoDAPFFMM.validarDetalles");
		long inicio = System.currentTimeMillis();
		
		HashMap<Long, String> validacionesDetalles = new HashMap<Long,String>();
		
		List<String> conceptosDetalle = detalles.stream().map(det -> { String[] tokens = det.Linea.split(";", -1); return (tokens.length > 9) ? tokens[9] : ""; }).filter(valor -> !valor.isEmpty()).distinct().collect(Collectors.toList());

		SQL sql = new SQL();
		HashMap<String,Object> params = new HashMap<String,Object>(1);
		params.put("IdsConcepto", conceptosDetalle);
		
		List<Concepto> conceptos = sql.query(con, SQL_SELECT_CONCEPTOS_SISTEMA, params, Concepto.class);
		Map<String, Concepto> hashConceptos = conceptos.stream().collect(Collectors.toMap(conc -> conc.IdConcepto , conc -> conc));
		params.clear();
		
		List<TipoMovimiento> tiposMovimiento = sql.query(con, SQL_SELECT_TIPO_MOVIMIENTO_TESORERIA, params, TipoMovimiento.class);
		Map<String, TipoMovimiento> hashTipoMovimiento = tiposMovimiento.stream().collect(Collectors.toMap(tipoMov -> tipoMov.IdTipoMovimiento , tipoMov -> tipoMov));
		params.clear();
			
		for(Detalle detalle :detalles) {
			String validacion = "";
			List<String> lineas = Arrays.stream(detalle.Linea.split(";", -1)).collect(Collectors.toList());
			
			if (lineas.stream().filter(tok -> !tok.isEmpty()).collect(Collectors.toList()).isEmpty() ) {
				continue;
		    }
			
			if(lineas.size() < 39) {
				validacion += "El registro no tiene la cantidad minima de valores. ";
				validacionesDetalles.put( 0L, validacion);
				continue;
			}
			
			if(lineas.get(7).contains(",") || lineas.get(19).contains(",")) {
				validacion += "Deben utilizarse . para los separadores de decimales en los campos de Montos. ";
			}
			
			if (!lineas.get(8).matches("^\\d{4}-\\d{2}-\\d{2}$") || !lineas.get(21).matches("^\\d{4}-\\d{2}-\\d{2}$") || !lineas.get(25).matches("^\\d{4}-\\d{2}-\\d{2}$") || !lineas.get(37).matches("^\\d{4}-\\d{2}-\\d{2}$")) {
				validacion += "Formato invalido de Fechas. Debe ser YYYY-MM-DD. ";
			}

			if("".equals(lineas.get(0))) {
				validacion += "El campo NumeroReferencia (en la posicion 0) es obligatorio. ";
			}
			
			Long indice = Long.valueOf(detalles.indexOf(detalle));
			Concepto concepto = hashConceptos.get(lineas.get(9));
			TipoMovimiento tipoMov = hashTipoMovimiento.get(lineas.get(1));

			if("".equals(lineas.get(1))) {
				validacion += "El campo IdTipoMovimiento (en la posicion 1) es obligatorio. ";
			}
			
			if(!"".equals(lineas.get(1)) && tipoMov == null) {
				validacion += "El Tipo de Movimiento no es valido. ";
			}

			if("".equals(lineas.get(2))) {
				validacion += "El campo IdTipoPersona (en la posicion 2) es obligatorio. ";
			}

			if("".equals(lineas.get(3))) {
				validacion += "El campo TipoDocumentoIdentidad (en la posicion 3) es obligatorio. ";
			}

			if("".equals(lineas.get(4))) {
				validacion += "El campo DocumentoIdentidad (en la posicion 4) es obligatorio. ";
			}

			if("".equals(lineas.get(5))) {
				validacion += "El campo CodigoPortafolio (en la posicion 5) es obligatorio. ";
			}

			if("".equals(lineas.get(6))) {
				validacion += "El campo IdMoneda (en la posicion 6) es obligatorio. ";
			}

			if("".equals(lineas.get(7)) || "0".equals(lineas.get(7))) {
				validacion += "El campo Monto (en la posicion 7) es obligatorio. ";
			}

			if("".equals(lineas.get(8)) || "0".equals(lineas.get(8))) {
				validacion += "El campo FechaLiquidacion (en la posicion 8) es obligatorio. ";
			}

			if("".equals(lineas.get(9)) || "0".equals(lineas.get(9))) {
				validacion += "El campo IdConcepto (en la posicion 9) es obligatorio. ";
			}

			if(concepto == null) {
				validacion += "El Concepto " + lineas.get(9) + " no se encuentra configurado o Vigente en el Sistema para Ingresos de Tesoreria. ";
			}

			if("".equals(lineas.get(10))) {
				validacion += "El campo SecuenciaAgrupacionComprobante (en la posicion 10) es obligatorio. ";
			}

			if("".equals(lineas.get(11))) {
				validacion += "El campo Glosa (en la posicion 11) es obligatorio. ";
			}
			
			if(!"N".equals(lineas.get(12)) && !"S".equals(lineas.get(12))) {
				validacion += "El Campo GeneraComprobanteAutomatico (en la posicion 12) permite valores S o N. ";
			}
			
			if(!"N".equals(lineas.get(13)) && !"S".equals(lineas.get(13))) {
				validacion += "El Campo LiquidarComprobanteAutomatico (en la posicion 13) permite valores S o N. ";
			}
			
			if(!"N".equals(lineas.get(14)) && !"S".equals(lineas.get(14))) {
				validacion += "El Campo AgruparEnComprobante (en la posicion 14) permite valores S o N. ";
			}

			if("".equals(lineas.get(15))) {
				validacion += "El campo NumeroReferenciaFormaDePago (en la posicion 15) es obligatorio. ";
			}

			if(!"".equals(lineas.get(15)) && !"".equals(lineas.get(0)) && !(lineas.get(15)).equals(lineas.get(0)) ) {
				validacion += "El campo NumeroReferenciaFormaDePago debe ser igual al IdInterno del registro. ";
			}

			if("".equals(lineas.get(16)) || "0".equals(lineas.get(16))) {
				validacion += "El campo IdMedioDePago (en la posicion 16) es obligatorio. ";
			}

			if("".equals(lineas.get(17)) || "0".equals(lineas.get(17))) {
				validacion += "El campo IdModalidadDePago (en la posicion 17) es obligatorio. ";
			}

			if("".equals(lineas.get(18)) || "0".equals(lineas.get(18))) {
				validacion += "El campo IdMonedaFormaDePago (en la posicion 18) es obligatorio. ";
			}

			if("".equals(lineas.get(19)) || "0".equals(lineas.get(19))) {
				validacion += "El campo MontoFormaDePago (en la posicion 19) es obligatorio. ";
			}

			if("".equals(lineas.get(20))) {
				validacion += "El campo NumeroDocumentoCorredora (en la posicion 20) es obligatorio. ";
			}

			if("".equals(lineas.get(21))) {
				validacion += "El campo FechaDocumentoPagoCorredora (en la posicion 21) es obligatorio. ";
			}

			if("".equals(lineas.get(24))) {
				validacion += "El campo NumeroCuentaBancariaSocio (en la posicion 24) es obligatorio. ";
			}

			if("".equals(lineas.get(25))) {
				validacion += "El campo FechaDocumentoPagoSocio (en la posicion 25) es obligatorio. ";
			}
			
			if("".equals(lineas.get(26)) || "0".equals(lineas.get(26))) {
				validacion += "El campo IdCuentaBancariaCorredora (en la posicion 26) es obligatorio. ";
			}
			
			if("".equals(lineas.get(34)) || "0".equals(lineas.get(34))) {
				validacion += "El campo IdTipoProducto (en la posicion 34) es obligatorio. ";
			}
			
			if("".equals(lineas.get(35)) || "0".equals(lineas.get(35))) {
				validacion += "El campo IdTipoOperacion (en la posicion 35) es obligatorio. ";
			}
			
			if("".equals(lineas.get(36)) || "0".equals(lineas.get(36))) {
				validacion += "El campo FolioOperacion (en la posicion 36) es obligatorio. ";
			}
			
			if("".equals(lineas.get(37)) || "0".equals(lineas.get(37))) {
				validacion += "El campo FechaOperacion (en la posicion 37) es obligatorio. ";
			}
			
			if("".equals(lineas.get(38)) || "0".equals(lineas.get(38))) {
				validacion += "El campo IdCondicionDeLiquidacion (en la posicion 38) es obligatorio. ";
			}
			
			if( !"".equals(validacion)) {
				validacion += "Por lo que no se realizara el procesamiento del registro.";
				validacionesDetalles.put(indice, validacion);
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::CargaMasivaIngresoEgresoDAPFFMM.validarDetalles", fin - inicio);
		return validacionesDetalles;
	}

	private void agregarArchivoSalidaSFTP(Connection con, EntradaCargaMasivaIngresoEgresoDAPFFMM entrada, List<String[]> lineasArchivoSalida, String nombreArchivoSalida) throws Exception {
		logger.info("INICIO::CargaMasivaIngresoEgresoDAPFFMM.agregarArchivoSalidaSFTP");
		long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		
		List<Long> secuenciaCartera = Utils.getSecuencia(con, "BSCL.Plataforma.BD.Secuencias::Cabecera", new Long(1L));

		List<Cabecera> cabeceras = new ArrayList<Cabecera>();
		Cabecera cabecera = new Cabecera();
		cabecera.IdCabecera = secuenciaCartera.get(0);
		cabecera.IdCodigoTipo = "SALIDA_" + BASE_CODIGO_TIPO + entrada.p_TipoArchivoCarga;
		cabecera.IdEstado = "PEN";
		cabecera.IdUsuario = entrada.p_IdUsuario;
		cabecera.NombreArchivo = nombreArchivoSalida + ".csv";
		cabecera.FechaCreacion = Fecha.toDateTime(entrada.p_FechaCarga);
		cabeceras.add(cabecera);
		
		sql.batch(con, SQL_INSERT_CABECERA_ARCHIVO_SALIDA, cabeceras, Cabecera.class);
		
		List<Detalle> detalles = lineasArchivoSalida.stream().map( linea -> {
			Detalle detalle = new Detalle();
			detalle.IdCabecera = secuenciaCartera.get(0);
			detalle.Linea = linea[0] + ";" + linea[1] + ";" + linea[2] + ";" + linea[3];
			detalle.IdTipoEntidad = "0".equals(linea[1]) ? "" : "IdComprobanteTesoreria";
			detalle.IdEntidad = Long.parseLong(linea[1]);
			return detalle;
		}).collect(Collectors.toList());
		
		sql.batch(con, SQL_INSERT_DETALLES_ARCHIVO_SALIDA, detalles, Detalle.class);
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::CargaMasivaIngresoEgresoDAPFFMM.agregarArchivoSalidaSFTP", fin - inicio);
	}

	private String agregarArchivoSalida(Connection con, EntradaCargaMasivaIngresoEgresoDAPFFMM entrada, List<String[]> lineasArchivoSalida, String nombreArchivoSalida) throws Exception {
		logger.info("INICIO::CargaMasivaIngresoEgresoDAPFFMM.agregarArchivoSalida");
		long inicio = System.currentTimeMillis();
		
		String rutaArchivo = "";
		String rutaAbsolutaBase = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.RutaBaseArchivos");
//		String rutaAbsolutaBase = System.getProperty("user.home") + File.separator + "Desktop" + File.separator ;
		String rutaRelativaCargaMasivaTeasoreria = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "TESORERIA", "TES.RutaSalidaArchivosCargaMasiva");
		String rutaBaseArchivoSalida = rutaAbsolutaBase + rutaRelativaCargaMasivaTeasoreria;
		
		String rutaRelativaArchivo = rutaRelativaCargaMasivaTeasoreria + nombreArchivoSalida + ".csv";
		
		File folder = new File(rutaBaseArchivoSalida);
		if(!folder.exists()){
			logger.info("INFO::CargaMasivaIngresoEgresoDAPFFMM.agregarArchivoSalida - Carpeta destino no existe "+ rutaBaseArchivoSalida);
			long fin = System.currentTimeMillis();
			logger.info("FIN::CargaMasivaIngresoEgresoDAPFFMM.agregarArchivoSalida", fin - inicio);
			return rutaArchivo;
		}
		Boolean resultadoGenerarCSV = generarReporteCSV(rutaAbsolutaBase, rutaRelativaArchivo, lineasArchivoSalida);
		
		rutaArchivo = resultadoGenerarCSV ? rutaRelativaArchivo : "";
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::CargaMasivaIngresoEgresoDAPFFMM.agregarArchivoSalida", fin - inicio);
		return rutaArchivo;
	}

	private boolean generarReporteCSV(String rutaAbsolutaBase, String rutaRelativaArchivo, List<String[]> lineasArchivoSalida) throws Exception {
		logger.info("INICIO::CargaMasivaIngresoEgresoDAPFFMM.generarReporteCSV");
		long inicio = System.currentTimeMillis();
		
		String rutaReporte = rutaAbsolutaBase + rutaRelativaArchivo;
		
		FileOutputStream fos = new FileOutputStream(rutaReporte);
		OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
		CSVWriter csvWriter = new CSVWriter(osw, ';', CSVWriter.NO_QUOTE_CHARACTER);
		try {
			osw.write('\uFEFF');
			csvWriter.writeAll(lineasArchivoSalida);
		} catch (Exception e) {
			logger.error(e.getMessage());
			long fin = System.currentTimeMillis();
			logger.info("FIN::CargaMasivaIngresoEgresoDAPFFMM.generarReporteCSV", fin - inicio);
			return false;
		} finally {
        	try {
        		csvWriter.close();
        	} catch (Exception ex) {
        		logger.error(ex.getMessage());
        		long fin = System.currentTimeMillis();
        		logger.info("FIN::CargaMasivaIngresoEgresoDAPFFMM.generarReporteCSV", fin - inicio);
        		return false;
        	}
        }
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::CargaMasivaIngresoEgresoDAPFFMM.generarReporteCSV", fin - inicio);
		return true;
	}

	private List<String[]> armarLineasArchivoSalida(List<Detalle> detalles, CrearIngresoEgreso_Salida salidaCrearIngresos, CrearIngresoEgreso_Salida salidaCrearEgresos, HashMap<Long, String> hashValidacionDetalle) {
		logger.info("INICIO::CargaMasivaIngresoEgresoDAPFFMM.armarLineasArchivoSalida");
		long inicio = System.currentTimeMillis();
		List<String[]> lineasArchivoSalida = new ArrayList<>();
		
		for(Detalle detalle : detalles) {
			if(!esLineaValida(detalle, "")) {
				String[] linea = { "0", "0", "Rechazado", "El registro no cuenta con el formato requerido."};
				lineasArchivoSalida.add(linea);
				continue;
			}
			
			List<String> lineas = new ArrayList<String>(Arrays.asList(detalle.Linea.split(";")));
			Long indice = Long.valueOf(detalles.indexOf(detalle));
			Long numeroReferencia = !"".equals(lineas.get(0)) ? Long.parseLong(lineas.get(0)) : 0L;
			String mensajeValidacion = hashValidacionDetalle.get(indice) != null ? hashValidacionDetalle.get(indice) : "";
			
			List<ResultadoProceso_Salida> resultadosFiltrados = new ArrayList<ResultadoProceso_Salida>();
			List<Long> idsComprobantes = new ArrayList<Long>();
			
			resultadosFiltrados.addAll(salidaCrearIngresos.P_OUT_RESULTADOPROCESO.stream().filter(compr -> !"".equals(lineas.get(0)) && numeroReferencia.equals(compr.NumeroReferencia) && "IdComprobante".equals(compr.Campo)).collect(Collectors.toList()));
			resultadosFiltrados.addAll(salidaCrearEgresos.P_OUT_RESULTADOPROCESO.stream().filter(compr -> !"".equals(lineas.get(0)) && numeroReferencia.equals(compr.NumeroReferencia) && "IdComprobante".equals(compr.Campo)).collect(Collectors.toList()));
			
			idsComprobantes.addAll(resultadosFiltrados.stream().map(res -> {return res.Id;}).collect(Collectors.toList()));
			
			List<MensajeSalida> errores = new ArrayList<>();
			errores.addAll(salidaCrearIngresos.P_OUT_MENSAJES.stream().filter(msj -> MensajeSalida.MENSAJE_ERROR.equals(msj.tipo) && ((!"".equals(lineas.get(0)) && numeroReferencia.equals(msj.id) )|| idsComprobantes.contains(msj.id))).collect(Collectors.toList()));
			errores.addAll(salidaCrearEgresos.P_OUT_MENSAJES.stream().filter(msj -> MensajeSalida.MENSAJE_ERROR.equals(msj.tipo) && ((!"".equals(lineas.get(0)) && numeroReferencia.equals(msj.id)) || idsComprobantes.contains(msj.id))).collect(Collectors.toList()));
			
			String comentario = "";
			for(MensajeSalida error : errores) {
				comentario += error.descripcion + ". ";
			}
			String estadoResultado = !resultadosFiltrados.isEmpty() ?  "Aplicado" : "Rechazado";
			String[] linea = { numeroReferencia.toString(), !resultadosFiltrados.isEmpty() ? resultadosFiltrados.get(0).Id.toString() : "0", estadoResultado, "".equals(mensajeValidacion) ? comentario : mensajeValidacion};
			lineasArchivoSalida.add(linea);
		}
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::CargaMasivaIngresoEgresoDAPFFMM.armarLineasArchivoSalida", fin - inicio);
		return lineasArchivoSalida;
	}

	private CrearIngresoEgreso_Salida crearIngresosEgresos(Connection con, EntradaCargaMasivaIngresoEgresoDAPFFMM entrada, List<IngresoEgreso_Entrada> ingresoEgresosAGenerar, List<FormaDePago_Entrada> formasDePagoAGenerar) throws Exception {
		logger.info("INICIO::CargaMasivaIngresoEgresoDAPFFMM.crearIngresosEgresos");
		long inicio = System.currentTimeMillis();
		
		CrearIngresoEgreso servicioCrearIngresoEgreso = new CrearIngresoEgreso();
		
		CrearIngresoEgreso_Entrada entradaCrearIngresoEgreso = new CrearIngresoEgreso_Entrada();
		entradaCrearIngresoEgreso.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaCrearIngresoEgreso.p_IdUsuario = entrada.p_IdUsuario;
		entradaCrearIngresoEgreso.p_IdModulo = entrada.p_IdModulo;
		entradaCrearIngresoEgreso.p_SimularOperacion = "N";
		entradaCrearIngresoEgreso.p_UsaReglasNegocio = "S";
		entradaCrearIngresoEgreso.p_EsIngresoAutomatico = "S";
		entradaCrearIngresoEgreso.p_IngresoEgresoGenerar = ingresoEgresosAGenerar;
		entradaCrearIngresoEgreso.p_FormaDePago = formasDePagoAGenerar;
		
		CrearIngresoEgreso_Salida salidaCrearIngresoEgreso = servicioCrearIngresoEgreso.logica(con, entradaCrearIngresoEgreso);
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::CargaMasivaIngresoEgresoDAPFFMM.crearIngresosEgresos", fin - inicio);
		return salidaCrearIngresoEgreso;
	}

	private List<FormaDePago_Entrada> obtenerFormasDePagoAGenerar(List<Detalle> detalles, String tipoMovimiento) {
		logger.info("INICIO::CargaMasivaIngresoEgresoDAPFFMM.obtenerFormasDePagoAGenerar");
		long inicio = System.currentTimeMillis();
		
		List<FormaDePago_Entrada> formasDePago = detalles.stream().filter(detalle -> esLineaValida(detalle, tipoMovimiento)).map(detalle -> {
			List<String> lineas = new ArrayList<String>(Arrays.asList(detalle.Linea.split(";")));
			FormaDePago_Entrada formaDePago = new FormaDePago_Entrada();
			
			formaDePago.NumeroReferencia = Long.parseLong(lineas.get(0));
			formaDePago.NumeroReferenciaFormaDePago = Long.parseLong(lineas.get(0));
			formaDePago.IdMedioDePago = lineas.get(16);
			formaDePago.IdModalidadDePago = lineas.get(17);
			formaDePago.IdMoneda = lineas.get(18);
			formaDePago.Monto = new BigDecimal(lineas.get(19));
			formaDePago.IdCuentaBancariaCorredora = Long.parseLong(lineas.get(26));
			formaDePago.IdTarjetaBancariaCorredora = !"".equals(lineas.get(27)) ? Long.parseLong(lineas.get(27)) : 0L;
			formaDePago.IdChequera = !"".equals(lineas.get(28)) ? Long.parseLong(lineas.get(28)) : 0L;
			formaDePago.NumeroDocumentoCorredora = Long.parseLong(lineas.get(20));
			formaDePago.FechaDocumentoPagoCorredora = lineas.get(21);
			formaDePago.IdCuentaBancariaSocio = Long.parseLong(lineas.get(22));
			formaDePago.IdBancoSocio = Long.parseLong(lineas.get(23));
			formaDePago.NombreCuentaBancariaSocio = "";
			formaDePago.NumeroCuentaBancariaSocio = lineas.get(24);
			formaDePago.IdTipoCuentaBancariaSocio = "";
			formaDePago.NombreTipoCuentaBancariaSocio = "";
			formaDePago.IdTarjetaBancariaSocio = !"".equals(lineas.get(29)) ? Long.parseLong(lineas.get(29)) : 0L;
			formaDePago.IdTipoTarjetaBancariaSocio = lineas.get(30);
			formaDePago.NombreTipoTarjetaBancariaSocio = "";
			formaDePago.NumeroTarjetaBancariaSocio = lineas.get(31);
			formaDePago.NumeroSerieSocio = lineas.get(32);
			formaDePago.NumeroDocumentoSocio = !"".equals(lineas.get(33)) ? Long.parseLong(lineas.get(33)) : 0L;
			formaDePago.FechaDocumentoPagoSocio = lineas.get(25);
			
			return formaDePago;
		}).collect(Collectors.toList());
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::CargaMasivaIngresoEgresoDAPFFMM.obtenerFormasDePagoAGenerar", fin - inicio);
		return formasDePago;
	}

	private List<IngresoEgreso_Entrada> obtenerIngresosEgresosAGenerar(Connection con, List<Detalle> detalles, String tipoMovimiento) throws Exception {
		logger.info("INICIO::CargaMasivaIngresoEgresoDAPFFMM.obtenerIngresosEgresosAGenerar");
		long inicio = System.currentTimeMillis();
		
		Map<String,PersonaPortafolio> personaPortafolio =  obtenerHashPersonaPortafolio(con, detalles);
		
		List<IngresoEgreso_Entrada> ingresosEgresos = detalles.stream().filter(detalle -> esLineaValida(detalle,tipoMovimiento)).map(detalle -> {
			List<String> lineas = new ArrayList<String>(Arrays.asList(detalle.Linea.split(";")));
			String llave = lineas.get(3) + "_" + lineas.get(4) + "_" + lineas.get(5);
			PersonaPortafolio datosPersonaPortafolio = personaPortafolio.get(llave);
			IngresoEgreso_Entrada ingresoEgreso = new IngresoEgreso_Entrada();
			
			ingresoEgreso.NumeroReferencia = Long.parseLong(lineas.get(0));
			ingresoEgreso.IdTipoMovimiento = lineas.get(1);
			ingresoEgreso.IdTipoPersona = lineas.get(2);
			ingresoEgreso.IdPersona = datosPersonaPortafolio.IdPersona;
			ingresoEgreso.IdPortafolio = datosPersonaPortafolio.IdPortafolio;
			ingresoEgreso.IdMoneda = lineas.get(6);
			ingresoEgreso.Monto = !"".equals(tipoMovimiento) ? new BigDecimal(lineas.get(7)) : BigDecimal.ZERO;
			ingresoEgreso.FechaLiquidacion = lineas.get(8);
			ingresoEgreso.IdConcepto = lineas.get(9);
			ingresoEgreso.IdTipoPersonaTitularPago = lineas.get(2);
			ingresoEgreso.IdPersonaTitularPago = datosPersonaPortafolio.IdPersona;
			ingresoEgreso.IdPortafolioTitularPago = datosPersonaPortafolio.IdPortafolio;
			ingresoEgreso.IdCaja = 0L;
			ingresoEgreso.SecuenciaAgrupacion = "0".equals( lineas.get(10)) ? "" : lineas.get(10);
			ingresoEgreso.SecuenciaRelacionada = null;
			ingresoEgreso.Glosa = "OPERACION CARGA MASIVA - " + lineas.get(11);
			ingresoEgreso.IdEmpleadoDigitador = 0L;
			ingresoEgreso.IdSucursalEmpleadoDigitador = 0L;
			ingresoEgreso.IdTipoComprobante = "";
			ingresoEgreso.IdComprobante = 0L;
			ingresoEgreso.FechaComprobante = "0001-01-01";
			ingresoEgreso.FechaLiquidacionComprobante = "0001-01-01";
			ingresoEgreso.FolioCartera = 0L;
			ingresoEgreso.IdInstrumento = 0L;
			ingresoEgreso.IdTipoInstrumento = "";
			ingresoEgreso.IdClasificacionTipoInstrumento = "";
			ingresoEgreso.IdTipoProducto = lineas.get(34);
			ingresoEgreso.IdTipoOperacion = lineas.get(35);
			ingresoEgreso.IdOperacion = Long.parseLong(lineas.get(36));
			ingresoEgreso.FechaOperacion = lineas.get(37);
			ingresoEgreso.IdCondicionLiquidacion = lineas.get(38);
			ingresoEgreso.IdBolsa = "";
			ingresoEgreso.IdMonedaOperacion = "";
			ingresoEgreso.MontoOperacion = BigDecimal.ZERO;
			ingresoEgreso.IdTipoReajuste = "";
			ingresoEgreso.FechaParidad = "0001-01-01";
			ingresoEgreso.ValorParidad = BigDecimal.ZERO;
			ingresoEgreso.Bilateral = "N";
			ingresoEgreso.MueveCuentaInversion = "S";
			ingresoEgreso.TitularPago = "N";
			ingresoEgreso.GeneraComprobante = !"".equals(lineas.get(12)) ? lineas.get(12) : "S";
			ingresoEgreso.LiquidarComprobante = !"".equals(lineas.get(13)) ? lineas.get(13) : "S";
			ingresoEgreso.AgruparEnComprobante = !"".equals(lineas.get(14)) ? lineas.get(14) : "N";
			ingresoEgreso.IdTipoCanalIngreso = "CME";
			
			return ingresoEgreso;
		}).sorted(Comparator.comparing(ing ->ing.NumeroReferencia)).collect(Collectors.toList());
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::CargaMasivaIngresoEgresoDAPFFMM.obtenerIngresosEgresosAGenerar", fin - inicio);
		return ingresosEgresos;
	}

	private Map<String, PersonaPortafolio> obtenerHashPersonaPortafolio(Connection con, List<Detalle> detalles) throws Exception {
		logger.info("INICIO::CargaMasivaIngresoEgresoDAPFFMM.obtenerHashPersonaPortafolio");
		long inicio = System.currentTimeMillis();
		
		Long idTrabajo = new Double(Math.random()*10000000000L).longValue();
		SQL sql = new SQL();
		List<DocumentoIdentidadCodigoPortafolio> clientesABuscar = detalles.stream().filter(detalle -> esLineaValida(detalle, "")).map(detalle -> {
			List<String> lineas = new ArrayList<String>(Arrays.asList(detalle.Linea.split(";")));
			
			DocumentoIdentidadCodigoPortafolio documentoIdentidadCodigoPortafolio = new DocumentoIdentidadCodigoPortafolio();
			documentoIdentidadCodigoPortafolio.IdTipoDocumentoIdentidad = lineas.get(3);
			documentoIdentidadCodigoPortafolio.DocumentoIdentidad = lineas.get(4);
			documentoIdentidadCodigoPortafolio.CodigoPortafolio = lineas.get(5);
			documentoIdentidadCodigoPortafolio.IdTrabajo = idTrabajo;
			return documentoIdentidadCodigoPortafolio;
		}).collect(Collectors.toList());
		
		sql.batch(con, SQL_INSERT_TABLA_PASO,clientesABuscar,DocumentoIdentidadCodigoPortafolio.class);
		
		HashMap<String,Object> params = new HashMap<String,Object>(1);
		params.put("IdTrabajo", idTrabajo);
		Map<String,PersonaPortafolio> personasPortafolios = sql.query(con, SQL_SELECT_IDPERSONA_IDPORTAFOLIO, params, PersonaPortafolio.class).stream().collect(Collectors.toMap(pp-> pp.IdTipoDocumentoIdentidad.toString() + "_" + pp.DocumentoIdentidad+ "_" + pp.CodigoPortafolio, pp -> pp));
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::CargaMasivaIngresoEgresoDAPFFMM.obtenerHashPersonaPortafolio", fin - inicio);
		return personasPortafolios;
	}

	private Boolean esLineaValida(Detalle detalle, String tipoMovimiento) {
		logger.info("INICIO::CargaMasivaIngresoEgresoDAPFFMM.esLineaValida");
		long inicio = System.currentTimeMillis();
		
		List<String> lineas = new ArrayList<String>(Arrays.asList(detalle.Linea.split(";")));
		Boolean esLineaValida = null != lineas && lineas.size() == 39 && (tipoMovimiento.equals(lineas.get(1)) || "".equals(tipoMovimiento));
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::CargaMasivaIngresoEgresoDAPFFMM.esLineaValida", fin - inicio);
		return esLineaValida;
	}

	private List<Detalle> getDetalles(Connection con, List<Cabecera> cabeceras) throws Exception {
		logger.info("INICIO::CargaMasivaIngresoEgresoDAPFFMM.getDetalles");
		long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		HashMap<String,Object> params = new HashMap<String,Object>();
		List<Long> idCabeceras = cabeceras.stream().map(cabecera -> cabecera.IdCabecera).collect(Collectors.toList());
		params.put("IdCabecera", idCabeceras);
		List<Detalle> detalles = sql.query(con, SQL_SELECT_DETALLE, params, Detalle.class);

		long fin = System.currentTimeMillis();
		logger.info("FIN::CargaMasivaIngresoEgresoDAPFFMM.getDetalles", fin - inicio);
		return detalles;
	}

	public List<Cabecera> getCabeceras(Connection con, String tipoArchivoCarga, String fechaEjecucion, String idEstado) throws Exception {
		logger.info("INICIO::CargaMasivaIngresoEgresoDAPFFMM.getCabeceras");
		long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		HashMap<String,Object> params = new HashMap<String,Object>();
		String codigosTipo = BASE_CODIGO_TIPO + tipoArchivoCarga;
		params.put("IdCodigoTipo", codigosTipo);
		params.put("TipoArchivo", tipoArchivoCarga);
		params.put("FechaEjecucion", fechaEjecucion);
		params.put("IdEstado", idEstado);
		List<Cabecera> cabeceras = sql.query(con, SQL_SELECT_CABECERA, params, Cabecera.class);
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::CargaMasivaIngresoEgresoDAPFFMM.getCabeceras", fin - inicio);
		return cabeceras;
	}

	public static void main(String[] args) throws SQLException {
		System.out.println("== START ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa02")){
			Gson gson = Utils.getGsonBuilder();
			String test = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":97,\"p_TipoArchivoCarga\":\"FFMM\",\"p_EnviarArchivoACasilla\":\"N\",\"p_FechaCarga\":\"2025-05-05\",\"p_IdModulo\":\"TES\",\"p_SimularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"S\"}";
			
			CargaMasivaIngresoEgresoDAPFFMM p1 = new CargaMasivaIngresoEgresoDAPFFMM();
			EntradaCargaMasivaIngresoEgresoDAPFFMM ObjIn = gson.fromJson(test, EntradaCargaMasivaIngresoEgresoDAPFFMM.class);
			
			System.out.println("ENTRADA:");
			System.out.println(ObjIn.toString());
			try {
				SalidaCargaMasivaIngresoEgresoDAPFFMM ObjOut = p1.logica(con, ObjIn);
				System.out.println(ObjOut.toString());
				
				con.rollback();				
			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				con.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("== END ==");

	}

	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		
		EntradaCargaMasivaIngresoEgresoDAPFFMM in = gson.fromJson(jin.toString(), EntradaCargaMasivaIngresoEgresoDAPFFMM.class);
		SalidaCargaMasivaIngresoEgresoDAPFFMM out = logica(con, in);
		
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

}