package cl.bcs.fondomutuo;

import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.plataforma.report.api.ReportDocument;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.StringUtils;
import cl.bcs.utils.Utils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.opencsv.CSVWriter;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@SuppressWarnings({"rawtypes", "unchecked"})
public class GenerarInformeRentabilidadFFMMExtranjeroMasivo implements JavaCall {
	static final Logger logger = LogManager.getLogger(GenerarInformeRentabilidadFFMMExtranjeroMasivo.class);

	private final String SQL_CALL_OBTENER_RENTABILIDAD_CUSTODIA_CLIENTE = loadResourceToString("cl/bcs/fondomutuo/sql/GenerarInformeRentabilidadFFMMExtranjeroMasivo_CallObtenerRentabilidadCustodiaCliente.sql");
	private final String SQL_SELECT_LOGO_CORREDORA = loadResourceToString("cl/bcs/fondomutuo/sql/GenerarInformeRentabilidadFFMMExtranjeroMasivo_SelectLogoCorredora.sql");
	private final String SQL_SELECT_RAIZ_ARCHIVO = loadResourceToString("cl/bcs/fondomutuo/sql/GenerarInformeRentabilidadFFMMExtranjeroMasivo_SelectRaizArchivo.sql");
	private final String SQL_INSERT_DATA_TABLA_INTERMEDIA= loadResourceToString("cl/bcs/fondomutuo/sql/GenerarInformeRentabilidadFFMMExtranjeroMasivo_InsertarTablaIntermediaCliente.sql");
	private final String SQL_DELETE_DATA_TABLA_INTERMEDIA = loadResourceToString("cl/bcs/fondomutuo/sql/GenerarInformeRentabilidadFFMMExtranjeroMasivo_BorrarDataTablaIntermediaCliente.sql");

	private static final Map<String, String> esquemaResumenRentabilidad = new HashMap<>();

	static {
		esquemaResumenRentabilidad.put("IdCliente", "number");
		esquemaResumenRentabilidad.put("LogoCorredora", "blob");
		esquemaResumenRentabilidad.put("Cantidad", "number");
		esquemaResumenRentabilidad.put("Tasa", "number");
		esquemaResumenRentabilidad.put("ValorPresente", "number");
		esquemaResumenRentabilidad.put("PrecioCierre", "number");
	}

	public static class EntradaGenerarInformeRentabilidadFFMMExtranjeroMasivo extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public String p_IdTipoCanalIngreso;
		public String p_AgnoComercial;
		public String p_EsMasivo;
		public List<Cliente> p_IdClientes;
		
		public transient DataSource p_DataSource;

		public EntradaGenerarInformeRentabilidadFFMMExtranjeroMasivo() {
			this.p_IdClientes = new ArrayList<>();
		}
	}
	
	public static class Cliente extends BcsPojo {
		public Long IdCliente;
		public Long IdTrabajo;
		
		public Cliente () {
			this.IdCliente = 0L;
			this.IdTrabajo = 0L;
		}
	}
	
	public static class SalidaGenerarInformeRentabilidadFFMMExtranjeroMasivo extends BcsPojo {
		public Long P_OUT_ERRORESCONTADOR;
		public List<PlataformaUtils.MensajeSalida> P_OUT_MENSAJES;
		public String p_out_RutaZip;

		public SalidaGenerarInformeRentabilidadFFMMExtranjeroMasivo() {
			P_OUT_ERRORESCONTADOR = 0L;
			P_OUT_MENSAJES = new ArrayList<>();
		}
	}

	public static class LogoCorredora extends BcsPojo {
		public byte[] Contenido;

		public LogoCorredora() {
			Contenido = new byte[0];
		}
	}

	public static class RaizArchivo extends BcsPojo {
		public String RutaArchivo;
		public String RutaFFMM;
	}

	public static class DatosRentabilidadAnualCustodiaCliente extends BcsPojo {
		public Long IdCliente;
		public Long IdInstrumento;
		public String IdRegimenTributario;
		public String NombreCompleto;
		public String DocumentoIdentidadCliente;
		public String NombreFondo;
		public String CodigoSerie;
		public String CodigoPais;
		public String AgnoComercial;
		public Long TotalRescatadoEnero;
		public Long TotalRescatadoFebrero;
		public Long TotalRescatadoMarzo;
		public Long TotalRescatadoAbril;
		public Long TotalRescatadoMayo;
		public Long TotalRescatadoJunio;
		public Long TotalRescatadoJulio;
		public Long TotalRescatadoAgosto;
		public Long TotalRescatadoSeptiembre;
		public Long TotalRescatadoOctubre;
		public Long TotalRescatadoNoviembre;
		public Long TotalRescatadoDiciembre;
		public Long TotalAportadoEnero;
		public Long TotalAportadoFebrero;
		public Long TotalAportadoMarzo;
		public Long TotalAportadoAbril;
		public Long TotalAportadoMayo;
		public Long TotalAportadoJunio;
		public Long TotalAportadoJulio;
		public Long TotalAportadoAgosto;
		public Long TotalAportadoSeptiembre;
		public Long TotalAportadoOctubre;
		public Long TotalAportadoNoviembre;
		public Long TotalAportadoDiciembre;
		public Long DiferenciaEnero;
		public Long DiferenciaFebrero;
		public Long DiferenciaMarzo;
		public Long DiferenciaAbril;
		public Long DiferenciaMayo;
		public Long DiferenciaJunio;
		public Long DiferenciaJulio;
		public Long DiferenciaAgosto;
		public Long DiferenciaSeptiembre;
		public Long DiferenciaOctubre;
		public Long DiferenciaNoviembre;
		public Long DiferenciaDiciembre;
		public Long MontoActualizadoEnero;
		public Long MontoActualizadoFebrero;
		public Long MontoActualizadoMarzo;
		public Long MontoActualizadoAbril;
		public Long MontoActualizadoMayo;
		public Long MontoActualizadoJunio;
		public Long MontoActualizadoJulio;
		public Long MontoActualizadoAgosto;
		public Long MontoActualizadoSeptiembre;
		public Long MontoActualizadoOctubre;
		public Long MontoActualizadoNoviembre;
		public Long MontoActualizadoDiciembre;
		public BigDecimal FactorActualizacionEnero;
		public BigDecimal FactorActualizacionFebrero;
		public BigDecimal FactorActualizacionMarzo;
		public BigDecimal FactorActualizacionAbril;
		public BigDecimal FactorActualizacionMayo;
		public BigDecimal FactorActualizacionJunio;
		public BigDecimal FactorActualizacionJulio;
		public BigDecimal FactorActualizacionAgosto;
		public BigDecimal FactorActualizacionSeptiembre;
		public BigDecimal FactorActualizacionOctubre;
		public BigDecimal FactorActualizacionNoviembre;
		public BigDecimal FactorActualizacionDiciembre;
		public String FechaSistema;
		public String DocumentoIdentidadCorredora;
		public String RazonSocialCorredora;
		public String CiudadCorredora;
		public String Nombres;
		public String RazonSocial;
		public String ApellidoPaterno;
		public String ApellidoMaterno;
		public String IdTemplate;
		public String DJProcesada;
	}

	public static class InformeRentabilidadAnualCustodiaCliente extends DatosRentabilidadAnualCustodiaCliente {
		public String LogoCorredora;
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		return null;
	}

	public SalidaGenerarInformeRentabilidadFFMMExtranjeroMasivo logica(Connection conn, EntradaGenerarInformeRentabilidadFFMMExtranjeroMasivo entrada) throws Exception {
		logger.info("INICIO::GenerarInformeRentabilidadFFMMExtranjeroMasivo");
		long inicio = System.currentTimeMillis();

		SalidaGenerarInformeRentabilidadFFMMExtranjeroMasivo salida = new SalidaGenerarInformeRentabilidadFFMMExtranjeroMasivo();

		String logoCorredoraB64 = obtenerLogoCorredoraB64(conn, entrada.p_IdEmpresa);
		RaizArchivo raizArchivo = obtenerRaizArchivo(conn, entrada.p_IdEmpresa);
		if (StringUtils.isNullOrBlank(raizArchivo.RutaArchivo) || StringUtils.isNullOrBlank(raizArchivo.RutaFFMM)) {
			salida.P_OUT_ERRORESCONTADOR++;
			salida.P_OUT_MENSAJES.add(new PlataformaUtils.MensajeSalida(0L, PlataformaUtils.MensajeSalida.MENSAJE_ERROR, "FALTA_CONFIGURACION_RUTA_ARCHIVO", new HashMap<>(), conn, entrada.p_IdEmpresa));
			return salida;
		}

		// Para pruebas locales.
//		final String rutaAbsolutaBase = System.getProperty("user.home") + File.separator + "Desktop" + File.separator + "Rentabilidad" + File.separator;
//		final String rutaRelativaBase = System.getProperty("user.home") + File.separator + "Desktop" + File.separator + "Rentabilidad" + File.separator;
		final String rutaAbsolutaBase = raizArchivo.RutaArchivo;
		final String rutaRelativaBase = raizArchivo.RutaFFMM;
		final String nombreBaseArchivo = "InformeRentabilidad-";
		final String nombreBaseZip = "InformesRentabilidades-";
		final String timestampSistema = Fecha.toFormat(FechaDB.ObtenerFechaHoraSistemaTs(conn, entrada.p_IdEmpresa), "yyyyMMdd_HHmmss");

		List<DatosRentabilidadAnualCustodiaCliente> rentabilidadesAnualesCustodiasClientes = obtenerDatosRentabilidadAnualCustodiaCliente(conn, entrada);
		
		if(rentabilidadesAnualesCustodiasClientes.isEmpty()) {
			salida.P_OUT_MENSAJES.add(new MensajeSalida( 0L, MensajeSalida.MENSAJE_ERROR, "UI_SIN_DATOS_RENTABILIDAD_ANUAL", new HashMap<>() , conn, entrada.p_IdEmpresa));
			salida.P_OUT_ERRORESCONTADOR = Long.valueOf(salida.P_OUT_MENSAJES.size());
			return salida;
		}
		
		Map<String, List<InformeRentabilidadAnualCustodiaCliente>> informesPorCliente = obtenerRentabilidadesAnualesCustodiasPorCliente(rentabilidadesAnualesCustodiasClientes, logoCorredoraB64);
		List<String> rutasArchivosSinComprimir = generarArchivosInformes(entrada.p_DataSource, rutaAbsolutaBase, nombreBaseArchivo, informesPorCliente);
		salida.p_out_RutaZip = comprimirArchivos(timestampSistema, rutaAbsolutaBase, rutaRelativaBase, nombreBaseZip, rutasArchivosSinComprimir);
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::GenerarInformeRentabilidadFFMMExtranjeroMasivo {}", fin - inicio);

		return salida;
	}

	public String obtenerLogoCorredoraB64(Connection conn, Long idEmpresa) throws SQLException {
		logger.debug("INICIO::GenerarInformeRentabilidadFFMMExtranjeroMasivo.obtenerLogoCorredora");
		long inicio = System.currentTimeMillis();

		HashMap<String, Object> params = new HashMap<>(2);
		params.put("IdEmpresa", idEmpresa);
		LogoCorredora logo = new SQL().query(conn, SQL_SELECT_LOGO_CORREDORA, params, LogoCorredora.class).stream().findFirst().orElse(new LogoCorredora());
		String contenido = Base64.encodeBase64String(logo.Contenido);

		long fin = System.currentTimeMillis();
		logger.debug("FIN::GenerarInformeRentabilidadFFMMExtranjeroMasivo.obtenerLogoCorredora {}", fin - inicio);

		return contenido;
	}

	public RaizArchivo obtenerRaizArchivo(Connection conn, Long idEmpresa) throws SQLException {
		logger.debug("INICIO::GenerarInformeRentabilidadFFMMExtranjeroMasivo.obtenerRaizArchivo");
		long inicio = System.currentTimeMillis();

		HashMap<String, Object> params = new HashMap<>(2);
		params.put("IdEmpresa", idEmpresa);

		RaizArchivo raiz = new SQL().query(conn, SQL_SELECT_RAIZ_ARCHIVO, params, RaizArchivo.class).stream().findFirst().orElse(new RaizArchivo());

		long fin = System.currentTimeMillis();
		logger.debug("INICIO::GenerarInformeRentabilidadFFMMExtranjeroMasivo.obtenerRaizArchivo {}", fin - inicio);

		return raiz;
	}

	private List<DatosRentabilidadAnualCustodiaCliente> obtenerDatosRentabilidadAnualCustodiaCliente(Connection conn, EntradaGenerarInformeRentabilidadFFMMExtranjeroMasivo entrada) throws Exception {
		logger.debug("INICIO::GenerarInformeRentabilidadFFMMExtranjeroMasivo.obtenerDatosRentabilidadAnualCustodiaCliente");
		long inicio = System.currentTimeMillis();

		Long idTrabajo = new Double(Math.random()*10000000000L).longValue();
		HashMap<String, Object> entradaSP = new HashMap<>();
		entradaSP.put("IdEmpresa", entrada.p_IdEmpresa);
		entradaSP.put("EsMasivo", entrada.p_EsMasivo);
		entradaSP.put("AgnoComercial", entrada.p_AgnoComercial);
		entradaSP.put("IdTrabajo", idTrabajo);
		
		SQL sql = new SQL();
		List<DatosRentabilidadAnualCustodiaCliente> rentabilidadesAnuales = new ArrayList<DatosRentabilidadAnualCustodiaCliente>();
		
		try {
			
			if(!entrada.p_IdClientes.isEmpty()) {
				entrada.p_IdClientes.parallelStream().forEach(cliente -> cliente.IdTrabajo = idTrabajo);
				sql.batch(conn, SQL_INSERT_DATA_TABLA_INTERMEDIA, entrada.p_IdClientes, Cliente.class);
			}
				
			HashMap<String, Class> salidaSP = new HashMap<>();
			salidaSP.put("p_salida", DatosRentabilidadAnualCustodiaCliente.class);

			HashMap<String, Object> resultadoSP = sql.call(conn, SQL_CALL_OBTENER_RENTABILIDAD_CUSTODIA_CLIENTE, entradaSP, salidaSP);
			rentabilidadesAnuales = resultadoSP.get("p_salida") == null ? new ArrayList<>() : (List<DatosRentabilidadAnualCustodiaCliente>) resultadoSP.get("p_salida");

		} catch (SQLException er) {
			er.printStackTrace();
		
		} finally {
			try { 
				if(!entrada.p_IdClientes.isEmpty()) {
					SQL.exec(conn, SQL_DELETE_DATA_TABLA_INTERMEDIA.replace(":IdTrabajo", idTrabajo+""));
				}
				
				logger.debug("INICIO::GenerarInformeRentabilidadFFMMExtranjeroMasivo.obtenerDatosRentabilidadAnualCustodiaCliente { SE EJECUTA EL DELETE FINAL}");
			} catch (Exception er) { 
				er.printStackTrace();
				logger.debug("INICIO::GenerarInformeRentabilidadFFMMExtranjeroMasivo.obtenerDatosRentabilidadAnualCustodiaCliente { NO SE EJECUTA EL DELETE FINAL}");
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::GenerarInformeRentabilidadFFMMExtranjeroMasivo.obtenerDatosRentabilidadAnualCustodiaCliente {}", fin - inicio);

		return rentabilidadesAnuales;
	}

	private Map<String, List<InformeRentabilidadAnualCustodiaCliente>> obtenerRentabilidadesAnualesCustodiasPorCliente(List<DatosRentabilidadAnualCustodiaCliente> rentabilidadesAnualesCustodiasClientes, String logoCorredoraB64) {
		logger.debug("INICIO::GenerarInformeRentabilidadFFMMExtranjeroMasivo.obtenerRentabilidadesAnualesCustodiasPorCliente");
		long inicio = System.currentTimeMillis();

		Map<String, List<InformeRentabilidadAnualCustodiaCliente>> rentabilidadesAnualesPorCliente = rentabilidadesAnualesCustodiasClientes.stream()
				.map(u -> {
					InformeRentabilidadAnualCustodiaCliente rac = new InformeRentabilidadAnualCustodiaCliente();
					rac.IdCliente = u.IdCliente;
					rac.IdInstrumento = u.IdInstrumento;
					rac.IdRegimenTributario = u.IdRegimenTributario;
					rac.NombreCompleto = u.NombreCompleto;
					rac.DocumentoIdentidadCliente = u.DocumentoIdentidadCliente;
					rac.NombreFondo = u.NombreFondo;
					rac.CodigoSerie = u.CodigoSerie;
					rac.CodigoPais = u.CodigoPais;
					rac.AgnoComercial = u.AgnoComercial;
					rac.TotalRescatadoEnero = u.TotalRescatadoEnero;
					rac.TotalRescatadoFebrero = u.TotalRescatadoFebrero;
					rac.TotalRescatadoMarzo = u.TotalRescatadoMarzo;
					rac.TotalRescatadoAbril = u.TotalRescatadoAbril;
					rac.TotalRescatadoMayo = u.TotalRescatadoMayo;
					rac.TotalRescatadoJunio = u.TotalRescatadoJunio;
					rac.TotalRescatadoJulio = u.TotalRescatadoJulio;
					rac.TotalRescatadoAgosto = u.TotalRescatadoAgosto;
					rac.TotalRescatadoSeptiembre = u.TotalRescatadoSeptiembre;
					rac.TotalRescatadoOctubre = u.TotalRescatadoOctubre;
					rac.TotalRescatadoNoviembre = u.TotalRescatadoNoviembre;
					rac.TotalRescatadoDiciembre = u.TotalRescatadoDiciembre;
					rac.TotalAportadoEnero = u.TotalAportadoEnero;
					rac.TotalAportadoFebrero = u.TotalAportadoFebrero;
					rac.TotalAportadoMarzo = u.TotalAportadoMarzo;
					rac.TotalAportadoAbril = u.TotalAportadoAbril;
					rac.TotalAportadoMayo = u.TotalAportadoMayo;
					rac.TotalAportadoJunio = u.TotalAportadoJunio;
					rac.TotalAportadoJulio = u.TotalAportadoJulio;
					rac.TotalAportadoAgosto = u.TotalAportadoAgosto;
					rac.TotalAportadoSeptiembre = u.TotalAportadoSeptiembre;
					rac.TotalAportadoOctubre = u.TotalAportadoOctubre;
					rac.TotalAportadoNoviembre = u.TotalAportadoNoviembre;
					rac.TotalAportadoDiciembre = u.TotalAportadoDiciembre;
					rac.DiferenciaEnero = u.DiferenciaEnero;
					rac.DiferenciaFebrero = u.DiferenciaFebrero;
					rac.DiferenciaMarzo = u.DiferenciaMarzo;
					rac.DiferenciaAbril = u.DiferenciaAbril;
					rac.DiferenciaMayo = u.DiferenciaMayo;
					rac.DiferenciaJunio = u.DiferenciaJunio;
					rac.DiferenciaJulio = u.DiferenciaJulio;
					rac.DiferenciaAgosto = u.DiferenciaAgosto;
					rac.DiferenciaSeptiembre = u.DiferenciaSeptiembre;
					rac.DiferenciaOctubre = u.DiferenciaOctubre;
					rac.DiferenciaNoviembre = u.DiferenciaNoviembre;
					rac.DiferenciaDiciembre = u.DiferenciaDiciembre;
					rac.FechaSistema = u.FechaSistema;
					rac.DocumentoIdentidadCorredora = u.DocumentoIdentidadCorredora;
					rac.RazonSocialCorredora = u.RazonSocialCorredora;
					rac.CiudadCorredora = u.CiudadCorredora;
					rac.Nombres = u.Nombres;
					rac.RazonSocial = u.RazonSocial;
					rac.ApellidoPaterno = u.ApellidoPaterno;
					rac.ApellidoMaterno = u.ApellidoMaterno;
					rac.IdTemplate = u.IdTemplate;
					rac.LogoCorredora = logoCorredoraB64;
					rac.DJProcesada = u.DJProcesada;
					return rac;
				})
				.collect(Collectors.groupingBy(u -> u.DocumentoIdentidadCliente));

		long fin = System.currentTimeMillis();
		logger.debug("FIN::GenerarInformeRentabilidadFFMMExtranjeroMasivo.obtenerRentabilidadesAnualesCustodiasPorCliente {}", fin - inicio);

		return rentabilidadesAnualesPorCliente;
	}

	private List<String> generarArchivosInformes(final DataSource dataSource, final String rutaAbsoluta, final String nombreBaseArchivo, Map<String, List<InformeRentabilidadAnualCustodiaCliente>> informesPorCliente) throws Exception {
		logger.debug("INICIO::GenerarInformeRentabilidadFFMMExtranjeroMasivo.generarArchivosInformes");
		long inicio = System.currentTimeMillis();

		final List<String> rutasArchivos = new ArrayList<>();
		final ReportDocument report = new ReportDocument(Utils.getSchema(), dataSource);
		String rutaBaseReporteCSV = rutaAbsoluta + nombreBaseArchivo + "Clientes.csv";
		List<String[]> lineasCSVRentabilidad = new ArrayList<String[]>();
		
		informesPorCliente.forEach((rutCliente, rentabilidadesCliente) -> {
			String rutaBaseReporte = rutaAbsoluta + nombreBaseArchivo + rutCliente;
			rutasArchivos.add(generarReportePDF(report, rutaBaseReporte, rentabilidadesCliente));
			lineasCSVRentabilidad.addAll(lineasReporteCSV(rentabilidadesCliente));
		});
		
		rutasArchivos.add(generarReporteCSV(rutaBaseReporteCSV, lineasCSVRentabilidad));

		long fin = System.currentTimeMillis();
		logger.debug("FIN::GenerarInformeRentabilidadFFMMExtranjeroMasivo.generarArchivosInformes {}", fin - inicio);

		return rutasArchivos;
	}

	private String generarReporteCSV(String rutaBase, List<String[]> lineasCSVRentabilidad) {
		logger.trace("INICIO::GenerarInformeRentabilidadFFMMExtranjeroMasivo.generarReporteCSV");
		long inicio = System.currentTimeMillis();

		String rutaReporte = rutaBase;
		try (FileOutputStream fos = new FileOutputStream(rutaReporte); OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8)) {
			
			CSVWriter csvWriter = new CSVWriter(osw, ';', CSVWriter.NO_QUOTE_CHARACTER);
			String[] cabeceras = {"NombreFondoInstrumento", "Razon Social", "Tax-id", "Pais", "Monto Actualizado", "RentaExterior", "PerdidaExterior", "MesVenta", "RutParticipe", "ApellidoPaterno", "ApellidoMaterno", "Nombre"};
			
			csvWriter.writeNext(cabeceras);
			csvWriter.writeAll(lineasCSVRentabilidad);
			
		} catch (IOException e) {
			logger.error(e.getMessage());
		}

		long fin = System.currentTimeMillis();
		logger.trace("FIN::GenerarInformeRentabilidadFFMMExtranjeroMasivo.generarReporteCSV {}", fin - inicio);

		return rutaReporte;
	}

	private List<String[]> lineasReporteCSV(List<InformeRentabilidadAnualCustodiaCliente> rentabilidadesCliente){
		logger.trace("INICIO::GenerarInformeRentabilidadFFMMExtranjeroMasivo.lineasReporteCSV");
		long inicio = System.currentTimeMillis();
		List<String[]> lineasCSVRentabilidad = new ArrayList<String[]>();
		
		for (InformeRentabilidadAnualCustodiaCliente rentabilidad : rentabilidadesCliente) {
			lineasCSVRentabilidad.addAll(obtenerLineasCSVRentabilidadesCliente(rentabilidad));
		}
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::GenerarInformeRentabilidadFFMMExtranjeroMasivo.lineasReporteCSV {}", fin - inicio);
		return lineasCSVRentabilidad;
	}
	
	private List<String[]> obtenerLineasCSVRentabilidadesCliente(InformeRentabilidadAnualCustodiaCliente rentabilidad) {
		logger.trace("INICIO::GenerarInformeRentabilidadFFMMExtranjeroMasivo.obtenerLineasCSVRentabilidadesCliente");
		long inicio = System.currentTimeMillis();

		List<String[]> lineasCSVRentabilidad = new ArrayList<String[]>();
		
		if(rentabilidad.TotalRescatadoEnero > 0) {
			String[] linea =  {rentabilidad.NombreFondo+" "+rentabilidad.CodigoSerie , rentabilidad.RazonSocialCorredora, rentabilidad.DocumentoIdentidadCliente.split("-")[0], rentabilidad.CodigoPais, Long.toString(rentabilidad.TotalAportadoEnero), rentabilidad.DiferenciaEnero > 0 ? Long.toString(rentabilidad.DiferenciaEnero) : "0", rentabilidad.DiferenciaEnero < 0 ? Long.toString(rentabilidad.DiferenciaEnero * -1) : "0", "1", rentabilidad.DocumentoIdentidadCliente, rentabilidad.ApellidoPaterno, rentabilidad.ApellidoMaterno, rentabilidad.Nombres};
			lineasCSVRentabilidad.add(linea);
		}
		if(rentabilidad.TotalRescatadoFebrero > 0) {
			String[] linea =  {rentabilidad.NombreFondo+" "+rentabilidad.CodigoSerie , rentabilidad.RazonSocialCorredora, rentabilidad.DocumentoIdentidadCliente.split("-")[0], rentabilidad.CodigoPais, Long.toString(rentabilidad.TotalAportadoFebrero), rentabilidad.DiferenciaFebrero > 0 ? Long.toString(rentabilidad.DiferenciaFebrero) : "0", rentabilidad.DiferenciaFebrero < 0 ? Long.toString(rentabilidad.DiferenciaFebrero * -1) : "0", "2", rentabilidad.DocumentoIdentidadCliente, rentabilidad.ApellidoPaterno, rentabilidad.ApellidoMaterno, rentabilidad.Nombres};
			lineasCSVRentabilidad.add(linea);
		}
		if(rentabilidad.TotalRescatadoMarzo > 0) {
			String[] linea =  {rentabilidad.NombreFondo+" "+rentabilidad.CodigoSerie , rentabilidad.RazonSocialCorredora, rentabilidad.DocumentoIdentidadCliente.split("-")[0], rentabilidad.CodigoPais, Long.toString(rentabilidad.TotalAportadoMarzo), rentabilidad.DiferenciaMarzo > 0 ? Long.toString(rentabilidad.DiferenciaMarzo) : "0", rentabilidad.DiferenciaMarzo < 0 ? Long.toString(rentabilidad.DiferenciaMarzo * -1) : "0", "3", rentabilidad.DocumentoIdentidadCliente, rentabilidad.ApellidoPaterno, rentabilidad.ApellidoMaterno, rentabilidad.Nombres};
			lineasCSVRentabilidad.add(linea);
		}
		if(rentabilidad.TotalRescatadoAbril > 0) {
			String[] linea =  {rentabilidad.NombreFondo+" "+rentabilidad.CodigoSerie , rentabilidad.RazonSocialCorredora, rentabilidad.DocumentoIdentidadCliente.split("-")[0], rentabilidad.CodigoPais, Long.toString(rentabilidad.TotalAportadoAbril), rentabilidad.DiferenciaAbril > 0 ? Long.toString(rentabilidad.DiferenciaAbril) : "0", rentabilidad.DiferenciaAbril < 0 ? Long.toString(rentabilidad.DiferenciaAbril * -1) : "0", "4", rentabilidad.DocumentoIdentidadCliente, rentabilidad.ApellidoPaterno, rentabilidad.ApellidoMaterno, rentabilidad.Nombres};
			lineasCSVRentabilidad.add(linea);
		}
		if(rentabilidad.TotalRescatadoMayo > 0) {
			String[] linea =  {rentabilidad.NombreFondo+" "+rentabilidad.CodigoSerie , rentabilidad.RazonSocialCorredora, rentabilidad.DocumentoIdentidadCliente.split("-")[0], rentabilidad.CodigoPais, Long.toString(rentabilidad.TotalAportadoMayo), rentabilidad.DiferenciaMayo > 0 ? Long.toString(rentabilidad.DiferenciaMayo) : "0", rentabilidad.DiferenciaMayo < 0 ? Long.toString(rentabilidad.DiferenciaMayo * -1) : "0", "5", rentabilidad.DocumentoIdentidadCliente, rentabilidad.ApellidoPaterno, rentabilidad.ApellidoMaterno, rentabilidad.Nombres};
			lineasCSVRentabilidad.add(linea);
		}
		if(rentabilidad.TotalRescatadoJunio > 0) {
			String[] linea =  {rentabilidad.NombreFondo+" "+rentabilidad.CodigoSerie , rentabilidad.RazonSocialCorredora, rentabilidad.DocumentoIdentidadCliente.split("-")[0], rentabilidad.CodigoPais, Long.toString(rentabilidad.TotalAportadoJunio), rentabilidad.DiferenciaJunio > 0 ? Long.toString(rentabilidad.DiferenciaJunio) : "0", rentabilidad.DiferenciaJunio < 0 ? Long.toString(rentabilidad.DiferenciaJunio * -1) : "0", "6", rentabilidad.DocumentoIdentidadCliente, rentabilidad.ApellidoPaterno, rentabilidad.ApellidoMaterno, rentabilidad.Nombres};
			lineasCSVRentabilidad.add(linea);
		}
		if(rentabilidad.TotalRescatadoJulio > 0) {
			String[] linea =  {rentabilidad.NombreFondo+" "+rentabilidad.CodigoSerie , rentabilidad.RazonSocialCorredora, rentabilidad.DocumentoIdentidadCliente.split("-")[0], rentabilidad.CodigoPais, Long.toString(rentabilidad.TotalAportadoJulio), rentabilidad.DiferenciaJulio > 0 ? Long.toString(rentabilidad.DiferenciaJulio) : "0", rentabilidad.DiferenciaJulio < 0 ? Long.toString(rentabilidad.DiferenciaJulio * -1) : "0", "7", rentabilidad.DocumentoIdentidadCliente, rentabilidad.ApellidoPaterno, rentabilidad.ApellidoMaterno, rentabilidad.Nombres};
			lineasCSVRentabilidad.add(linea);
		}
		if(rentabilidad.TotalRescatadoAgosto > 0) {
			String[] linea =  {rentabilidad.NombreFondo+" "+rentabilidad.CodigoSerie , rentabilidad.RazonSocialCorredora, rentabilidad.DocumentoIdentidadCliente.split("-")[0], rentabilidad.CodigoPais, Long.toString(rentabilidad.TotalAportadoAgosto), rentabilidad.DiferenciaAgosto > 0 ? Long.toString(rentabilidad.DiferenciaAgosto) : "0", rentabilidad.DiferenciaAgosto < 0 ? Long.toString(rentabilidad.DiferenciaAgosto * -1) : "0", "8", rentabilidad.DocumentoIdentidadCliente, rentabilidad.ApellidoPaterno, rentabilidad.ApellidoMaterno, rentabilidad.Nombres};
			lineasCSVRentabilidad.add(linea);
		}
		if(rentabilidad.TotalRescatadoSeptiembre > 0) {
			String[] linea =  {rentabilidad.NombreFondo+" "+rentabilidad.CodigoSerie , rentabilidad.RazonSocialCorredora, rentabilidad.DocumentoIdentidadCliente.split("-")[0], rentabilidad.CodigoPais, Long.toString(rentabilidad.TotalAportadoSeptiembre), rentabilidad.DiferenciaSeptiembre > 0 ? Long.toString(rentabilidad.DiferenciaSeptiembre) : "0", rentabilidad.DiferenciaSeptiembre < 0 ? Long.toString(rentabilidad.DiferenciaSeptiembre * -1) : "0", "9", rentabilidad.DocumentoIdentidadCliente, rentabilidad.ApellidoPaterno, rentabilidad.ApellidoMaterno, rentabilidad.Nombres};
			lineasCSVRentabilidad.add(linea);
		}
		if(rentabilidad.TotalRescatadoOctubre > 0) {
			String[] linea =  {rentabilidad.NombreFondo+" "+rentabilidad.CodigoSerie , rentabilidad.RazonSocialCorredora, rentabilidad.DocumentoIdentidadCliente.split("-")[0], rentabilidad.CodigoPais, Long.toString(rentabilidad.TotalAportadoOctubre), rentabilidad.DiferenciaOctubre > 0 ? Long.toString(rentabilidad.DiferenciaOctubre) : "0", rentabilidad.DiferenciaOctubre < 0 ? Long.toString(rentabilidad.DiferenciaOctubre * -1) : "0", "10", rentabilidad.DocumentoIdentidadCliente, rentabilidad.ApellidoPaterno, rentabilidad.ApellidoMaterno, rentabilidad.Nombres};
			lineasCSVRentabilidad.add(linea);
		}
		if(rentabilidad.TotalRescatadoNoviembre > 0) {
			String[] linea =  {rentabilidad.NombreFondo+" "+rentabilidad.CodigoSerie , rentabilidad.RazonSocialCorredora, rentabilidad.DocumentoIdentidadCliente.split("-")[0], rentabilidad.CodigoPais, Long.toString(rentabilidad.TotalAportadoNoviembre), rentabilidad.DiferenciaNoviembre > 0 ? Long.toString(rentabilidad.DiferenciaNoviembre) : "0", rentabilidad.DiferenciaNoviembre < 0 ? Long.toString(rentabilidad.DiferenciaNoviembre * -1) : "0", "11", rentabilidad.DocumentoIdentidadCliente, rentabilidad.ApellidoPaterno, rentabilidad.ApellidoMaterno, rentabilidad.Nombres};
			lineasCSVRentabilidad.add(linea);
		}
		if(rentabilidad.TotalRescatadoDiciembre > 0) {
			String[] linea =  {rentabilidad.NombreFondo+" "+rentabilidad.CodigoSerie , rentabilidad.RazonSocialCorredora, rentabilidad.DocumentoIdentidadCliente.split("-")[0], rentabilidad.CodigoPais, Long.toString(rentabilidad.TotalAportadoDiciembre), rentabilidad.DiferenciaDiciembre > 0 ? Long.toString(rentabilidad.DiferenciaDiciembre) : "0", rentabilidad.DiferenciaDiciembre < 0 ? Long.toString(rentabilidad.DiferenciaDiciembre * -1) : "0", "12", rentabilidad.DocumentoIdentidadCliente, rentabilidad.ApellidoPaterno, rentabilidad.ApellidoMaterno, rentabilidad.Nombres};
			lineasCSVRentabilidad.add(linea);
		}
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::GenerarInformeRentabilidadFFMMExtranjeroMasivo.obtenerLineasCSVRentabilidadesCliente {}", fin - inicio);
		return lineasCSVRentabilidad;
	}

	private String generarReportePDF(ReportDocument report, String rutaBase, List<InformeRentabilidadAnualCustodiaCliente> rentabilidadesCliente) {
		logger.trace("INICIO::GenerarInformeRentabilidadFFMMExtranjeroMasivo.generarReportePDF");
		long inicio = System.currentTimeMillis();

		String rutaReporte = rutaBase + ".pdf";
		String json = Utils.getGsonBuilder().toJson(rentabilidadesCliente);
		JsonArray resumenRentabilidad = new JsonParser().parse(json).getAsJsonArray();
		String idTemplate = rentabilidadesCliente.stream().findFirst().orElse(new InformeRentabilidadAnualCustodiaCliente()).IdTemplate;
		if (report.readDocument(idTemplate)) {
			try {
				report.setDocumentResultSet(esquemaResumenRentabilidad, resumenRentabilidad);
			} catch (Exception e) {
				logger.error(e.getMessage());
			}

			try (InputStream is = report.exportDocument("pdf")) {
				File reportePDF = new File(rutaReporte);
				FileUtils.copyInputStreamToFile(is, reportePDF);
			} catch (Exception e) {
				logger.error(e.getMessage());
			}
		} else {
			logger.error("Error al obtener el template {}, generacion de PDF omitida", idTemplate);
		}

		long fin = System.currentTimeMillis();
		logger.trace("FIN::GenerarInformeRentabilidadFFMMExtranjeroMasivo.generarReportePDF {}", fin - inicio);

		return rutaReporte;
	}

	private String comprimirArchivos(final String timestampSistema, final String rutaAbsolutaBase, final String rutaRelativaBase, final String nombreBaseZip, List<String> rutasArchivos) throws Exception {
		logger.debug("FIN::GenerarInformeRentabilidadFFMMExtranjeroMasivo.comprimirArchivos");
		long inicio = System.currentTimeMillis();

		final String nombreZip = nombreBaseZip + timestampSistema + ".zip";
		final String rutaAbsolutaZip = rutaAbsolutaBase + nombreZip;
		final String rutaRelativaZip = rutaRelativaBase + nombreZip;
		
		try (OutputStream os = new FileOutputStream(rutaAbsolutaZip); ZipOutputStream zos = new ZipOutputStream(os)) {
			for (String rutaArchivo : rutasArchivos) {
				String nombreArchivoSinComprimir = FilenameUtils.getName(rutaArchivo);
				try (InputStream is = new FileInputStream(rutaArchivo)) {
					zos.putNextEntry(new ZipEntry(nombreArchivoSinComprimir));
					int bytesRead = 0;
					byte[] buffer = new byte[1024];
					while ((bytesRead = is.read(buffer)) > 0) {
						zos.write(buffer, 0, bytesRead);
					}
					zos.closeEntry();
				}

				FileUtils.forceDelete(new File(rutaArchivo));
			}
		}

		long fin = System.currentTimeMillis();
		logger.debug("FIN::GenerarInformeRentabilidadFFMMExtranjeroMasivo.comprimirArchivos", rutaRelativaZip);
		logger.debug("FIN::GenerarInformeRentabilidadFFMMExtranjeroMasivo.comprimirArchivos {}", fin - inicio);
		return rutaRelativaZip;
	}

	public static void main(String[] args) {
		System.out.println("== INICIO TEST ==");

		try (Connection conn = PlataformaUtils.getBOLSAConnection()) {
			EntradaGenerarInformeRentabilidadFFMMExtranjeroMasivo entrada = new EntradaGenerarInformeRentabilidadFFMMExtranjeroMasivo();
			entrada.p_IdEmpresa = 1L;
			entrada.p_IdUsuario = 4L;
			entrada.p_IdModulo = "FMM";
			entrada.p_UsaReglasNegocio = "S";
			entrada.p_IdTipoCanalIngreso = "OPT";
			entrada.p_AgnoComercial = "2020";
			entrada.p_EsMasivo = "S";
			Cliente cliente = new Cliente();
			cliente.IdCliente = 77773L;
			entrada.p_IdClientes.add(cliente);
			
			entrada.p_DataSource = PlataformaUtils.getDESADatasource();

			SalidaGenerarInformeRentabilidadFFMMExtranjeroMasivo salida = new GenerarInformeRentabilidadFFMMExtranjeroMasivo().logica(conn, entrada);
			if (salida.P_OUT_ERRORESCONTADOR > 0) {
				conn.rollback();
			}
			
			System.out.println("ENTRADA");
			System.out.println(entrada);
			System.out.println("SALIDA");
			System.out.println(salida);

		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("== FIN TEST ==");
	}
}
