package cl.bcs.fondomutuo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.sql.DataSource;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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

@SuppressWarnings({"rawtypes", "unchecked"})
public class GenerarCertificado31Masivo implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(GenerarCertificado31Masivo.class);	
	
	private final String SQL_SELECT_LOGO_CORREDORA = loadResourceToString("cl/bcs/fondomutuo/sql/GenerarInformeRentabilidadFFMMExtranjeroMasivo_SelectLogoCorredora.sql");
	private final String SQL_SELECT_RAIZ_ARCHIVO = loadResourceToString("cl/bcs/fondomutuo/sql/GenerarInformeRentabilidadFFMMExtranjeroMasivo_SelectRaizArchivo.sql");	
	private final String SQL_SELECT_FIRMA_CORREDORA = loadResourceToString("cl/bcs/fondomutuo/sql/GenerarCertificado31Masivo_SelectImagenFirma.sql");
	private final String SQL_CALL_CERTIFICADO_31 = loadResourceToString("cl/bcs/fondomutuo/sql/GenerarCertificado31Masivo_certificado31_Call.sql");
	private final String SQL_SELECT_ID_REPORTE = loadResourceToString("cl/bcs/fondomutuo/sql/GenerarCertificado31Masivo_SelectReporte.sql");
	private final String SQL_CREATE_TABLA_TRASPASO= "CREATE LOCAL TEMPORARY TABLE #GenerarCertificado31Masivo_Traspasos (\"IdTraspaso\" BIGINT)";													
	private final String SQL_INSERT_TABLA_TRASPASO= "INSERT INTO #GenerarCertificado31Masivo_Traspasos (\"IdTraspaso\") VALUES (:IdTraspaso)";
	public  final String SQL_DROP_TABLA_TRASPASO  = "DROP TABLE #GenerarCertificado31Masivo_Traspasos";
	
	
	
	
	private static final Map<String, String> esquemaCertificado31 = new HashMap<>();

	static {
		esquemaCertificado31.put("IdCliente", "number");
		esquemaCertificado31.put("LogoCorredora", "blob");
		esquemaCertificado31.put("FirmaCorredora", "blob");
	}

	public static class EntradaGenerarCertificado31Masivo extends BcsPojo{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;	
		public String p_EsMasivo;
		public String p_Ciudad;
		public String p_Fecha;
		public List<Traspaso> p_listaTraspasos;
		
		public transient DataSource p_DataSource;
		
		public EntradaGenerarCertificado31Masivo() {
			this.p_EsMasivo="S";
			this.p_listaTraspasos = new ArrayList<Traspaso>();
		}
		
	} 
	
	public static class SalidaGenerarCertificado31Masivo extends BcsPojo {
		public Long P_OUT_ERRORESCONTADOR;
		public List<PlataformaUtils.MensajeSalida> P_OUT_MENSAJES;
		public String p_out_RutaZip;

		public SalidaGenerarCertificado31Masivo() {
			P_OUT_ERRORESCONTADOR = 0L;
			P_OUT_MENSAJES = new ArrayList<>();
		}
	}
	
	public static class Traspaso extends BcsPojo{
		public Long IdTraspaso;		
		
		public Traspaso () {
			this.IdTraspaso = 0L;			
		}
	}
	
	public static class Imagen extends BcsPojo {
		public byte[] Contenido;

		public Imagen() {
			Contenido = new byte[0];
		}
	}
	
	public static class RaizArchivo extends BcsPojo {
		public String RutaArchivo;
		public String RutaFFMM;
	}
	
	public static class Template extends BcsPojo {
		public String Id;
	}
	
	public static class DatosCertificado extends BcsPojo{
		public Long FolioTraspaso;
		public Long IdTraspaso;
		public String FechaEncabezado;
		public String Lugar;
		public String NombreCompleto;
		public String RutCliente;
		public BigDecimal NumeroCuotas;
		public BigDecimal ValorCuota;
		public String IdSerieFondo;
		public String RutFondo;
		public String NombreSerieFondo;
		public String Fechaingreso;
		public BigDecimal MontoTraspasoUF;
		public BigDecimal MontoOriginalUF;
		public String FechaIngresoCartera;
		public BigDecimal ProcentajeInvAccionesFondo;	  
		public BigDecimal ProcentajeInvAccionesFondoRescate;  
		public String RunFondoMutuo;
		public String NombreFondoMutuo;
		public String RutSociedadAdministradora;
		public String NombreSociedadAdministradora;
		public String DocumentoIdentidadEntidadRecaudadora;	  
		public Long IdCliente;
		public String RazonSocial;
		public String YearComercial;
		public String Direccion;
		public String DocumentoIdentidad;
		public Long NumeroCertificado;
		
		public DatosCertificado() {
			this.NumeroCertificado = 0L;
		} 
	
	}
	
	public static class DatosCertificadoCompleto extends DatosCertificado{
		public String LogoCorredora;
		public String FirmaCorredora;	
	}
	
	@Override	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		return null;
	}
	
	public SalidaGenerarCertificado31Masivo logica(Connection conn, EntradaGenerarCertificado31Masivo entrada) throws Exception {
		logger.info("INICIO::GenerarCertificado31Masivo");
		long inicio = System.currentTimeMillis();
		
		SalidaGenerarCertificado31Masivo salida = new SalidaGenerarCertificado31Masivo();
		
		String logoCorredoraB64  = obtenerLogoCorredoraB64(conn, entrada.p_IdEmpresa);
		String firmaCorredoraB64 = obtenerFirmaB64(conn, entrada.p_IdEmpresa);
		
		RaizArchivo raizArchivo = obtenerRaizArchivo(conn, entrada.p_IdEmpresa);
		if (StringUtils.isNullOrBlank(raizArchivo.RutaArchivo) || StringUtils.isNullOrBlank(raizArchivo.RutaFFMM)) {
			salida.P_OUT_ERRORESCONTADOR++;
			salida.P_OUT_MENSAJES.add(new PlataformaUtils.MensajeSalida(0L, PlataformaUtils.MensajeSalida.MENSAJE_ERROR, "FALTA_CONFIGURACION_RUTA_ARCHIVO", new HashMap<>(), conn, entrada.p_IdEmpresa));
			return salida;
		}
		
		// Para pruebas locales.
		//final String rutaAbsolutaBase = System.getProperty("user.home") + File.separator + "Desktop" + File.separator + "Certificado31" + File.separator;
		//final String rutaRelativaBase = System.getProperty("user.home") + File.separator + "Desktop" + File.separator + "Certificado31" + File.separator;
		final String rutaAbsolutaBase = raizArchivo.RutaArchivo;
		final String rutaRelativaBase = raizArchivo.RutaFFMM;
		final String nombreBaseArchivo = "Certificado31-";
		final String nombreBaseZip = "CertificadosFFMM-";
		final String timestampSistema = Fecha.toFormat(FechaDB.ObtenerFechaHoraSistemaTs(conn, entrada.p_IdEmpresa), "yyyyMMdd_HHmmss");
		
		//buscar datos traspasos		
		List<DatosCertificado> listaCerificados = obtenerListaCertificados (conn , entrada);
		
		if(listaCerificados.isEmpty()) {
			salida.P_OUT_MENSAJES.add(new MensajeSalida( 0L, MensajeSalida.MENSAJE_ERROR, "UI_SIN_DATOS_TRASPASOS", "No se encontraron traspasos consultados"));
			salida.P_OUT_ERRORESCONTADOR = Long.valueOf(salida.P_OUT_MENSAJES.size());
			return salida;
		}
		
		List<DatosCertificadoCompleto> traspasosInformacion= obtenerTraspasoReporte(listaCerificados, logoCorredoraB64,firmaCorredoraB64, entrada);
		List<String> rutasArchivosSinComprimir = generarArchivosInformes(conn, entrada.p_IdEmpresa, entrada.p_DataSource, rutaAbsolutaBase, nombreBaseArchivo, traspasosInformacion);
		salida.p_out_RutaZip = comprimirArchivos(timestampSistema, rutaAbsolutaBase, rutaRelativaBase, nombreBaseZip, rutasArchivosSinComprimir);
		
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::GenerarCertificado31Masivo {}", fin - inicio);

		return salida;
	}
	
	private String comprimirArchivos(String timestampSistema, String rutaAbsolutaBase, String rutaRelativaBase,	String nombreBaseZip, List<String> rutasArchivos) {
		logger.debug("FIN::GenerarCertificado31Masivo.comprimirArchivos");
		long inicio = System.currentTimeMillis();

		final String nombreZip = nombreBaseZip + timestampSistema + ".zip";
		final String rutaAbsolutaZip = rutaAbsolutaBase + nombreZip;
		final String rutaRelativaZip = rutaRelativaBase + nombreZip;
		
		try (OutputStream os = new FileOutputStream(rutaAbsolutaZip); ZipOutputStream zos = new ZipOutputStream(os)) {
			for (String rutaArchivo : rutasArchivos) {
				logger.debug("PRUEBA::GenerarCertificado31Masivo.comprimirArchivos:::::::::: rutaArchivo" ,rutaArchivo);
				String nombreArchivoSinComprimir = FilenameUtils.getName(rutaArchivo);
				try (InputStream is = new FileInputStream(rutaArchivo)) {
					zos.putNextEntry(new ZipEntry(nombreArchivoSinComprimir));
					int bytesRead = 0;
					byte[] buffer = new byte[1024];
					while ((bytesRead = is.read(buffer)) > 0) {
						zos.write(buffer, 0, bytesRead);
					}
					zos.closeEntry();
				}catch (Exception e) {
					logger.info("PRUEBA::try for comprimirArchivos:::::::::", e.getMessage());					
				}

				FileUtils.forceDelete(new File(rutaArchivo));
			}
		}catch (Exception e) {
			logger.info("PRUEBA::primer try comprimirArchivos:::::::::", e.getMessage());
			
		}

		long fin = System.currentTimeMillis();
		logger.debug("FIN::GenerarCertificado31Masivo.comprimirArchivos", rutaRelativaZip);
		logger.debug("FIN::GenerarCertificado31Masivo.comprimirArchivos {}", fin - inicio);
		return rutaRelativaZip;
	}


	private List<String> generarArchivosInformes(Connection conn, Long idEmpresa, DataSource dataSource, String rutaAbsoluta,String nombreBaseArchivo, List<DatosCertificadoCompleto> traspasosInformacion) throws Exception {
		logger.debug("INICIO::GenerarCertificado31Masivo.generarArchivosInformes");
		long inicio = System.currentTimeMillis();
		
		final List<String> rutasArchivos = new ArrayList<>();
		final ReportDocument report = new ReportDocument(Utils.getSchema(), dataSource);		

		for(DatosCertificadoCompleto traspaso : traspasosInformacion) {
			String rutaBaseReporte = rutaAbsoluta + nombreBaseArchivo +traspaso.IdTraspaso;			
			try {
				rutasArchivos.add(generarReportePDF(conn, idEmpresa, report, rutaBaseReporte, traspaso));
			} catch (Exception e) {
				e.printStackTrace();
				logger.info("PRUEBA::generarArchivosInformess:::::::::", e.getMessage());
			}
		}	
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::GenerarCertificado31Masivo.obtenerTraspasoReporte {}", fin - inicio);
		return rutasArchivos;
	}
	
	
	private String generarReportePDF(Connection conn, Long idEmpresa, ReportDocument report, String rutaBase, DatosCertificadoCompleto traspaso) throws Exception {
		logger.trace("INICIO::GenerarCertificado31Masivo.generarReportePDF");
		long inicio = System.currentTimeMillis();

		String rutaReporte = "";
		List<DatosCertificadoCompleto> listaTraspasoFinal = new ArrayList<>();
		listaTraspasoFinal.add(traspaso);
		String json = Utils.getGsonBuilder().toJson(listaTraspasoFinal);
		JsonArray informacionCertificado = new JsonParser().parse(json).getAsJsonArray();
		String idTemplate =obtenerIdReporte(conn);// "173603C07D0185DB1700D20086E4297E";
		if (report.readDocument(idTemplate)) {
			try {
				report.setDocumentResultSet(esquemaCertificado31, informacionCertificado);
			} catch (Exception e) {
				logger.error(e.getMessage());
			}

			try (InputStream is = report.exportDocument("pdf")) {
				String timestampSistema = Fecha.toFormat(FechaDB.ObtenerFechaHoraSistemaTs(conn, idEmpresa), "yyyyMMdd_HHmmssSSSS");
				rutaReporte = rutaBase + "_" + timestampSistema + ".pdf";
				File reportePDF = new File(rutaReporte);
				FileUtils.copyInputStreamToFile(is, reportePDF);
			} catch (Exception e) {
				logger.error(e.getMessage());
			}
		} else {
			logger.error("Error al obtener el template {}, generacion de PDF omitida", idTemplate);
		}

		long fin = System.currentTimeMillis();
		logger.trace("FIN::GenerarCertificado31Masivo.generarReportePDF {}", fin - inicio);

		return rutaReporte;
	}


	private List<DatosCertificadoCompleto> obtenerTraspasoReporte(List<DatosCertificado> listaCerificados, String logoCorredoraB64, String firmaCorredoraB64, EntradaGenerarCertificado31Masivo entrada) {
		logger.debug("INICIO::GenerarCertificado31Masivo.obtenerTraspasoReporte");
		long inicio = System.currentTimeMillis();
		Integer contadorCertificado=1;
		
		List<DatosCertificadoCompleto>  listaTraspasosReporte = new ArrayList<DatosCertificadoCompleto>();
		
		for (DatosCertificado tra : listaCerificados) {
			DatosCertificadoCompleto traspasoImagen = new DatosCertificadoCompleto();
			traspasoImagen.FolioTraspaso=tra.FolioTraspaso ;
			traspasoImagen.IdTraspaso= tra.IdTraspaso;
			traspasoImagen.FechaEncabezado= entrada.p_Fecha ;
			traspasoImagen.Lugar= entrada.p_Ciudad ;
			traspasoImagen.NombreCompleto=tra.NombreCompleto ;
			traspasoImagen.RutCliente=tra.RutCliente ;
			traspasoImagen.NumeroCuotas=tra.NumeroCuotas ;
			traspasoImagen.ValorCuota=tra.ValorCuota ;
			traspasoImagen.IdSerieFondo=tra.IdSerieFondo ;
			traspasoImagen.RutFondo=tra.RutFondo ;
			traspasoImagen.NombreSerieFondo=tra.NombreSerieFondo ;
			traspasoImagen.Fechaingreso=tra.Fechaingreso ;
			traspasoImagen.MontoTraspasoUF=tra.MontoTraspasoUF ;
			traspasoImagen.MontoOriginalUF=tra.MontoOriginalUF ;
			traspasoImagen.FechaIngresoCartera=tra.FechaIngresoCartera ;
			traspasoImagen.ProcentajeInvAccionesFondo=tra.ProcentajeInvAccionesFondo;	  
			traspasoImagen.ProcentajeInvAccionesFondoRescate=tra.ProcentajeInvAccionesFondoRescate ;  
			traspasoImagen.RunFondoMutuo=tra.RunFondoMutuo ;
			traspasoImagen.NombreFondoMutuo=tra.NombreFondoMutuo ;
			traspasoImagen.RutSociedadAdministradora=tra.RutSociedadAdministradora ;
			traspasoImagen.NombreSociedadAdministradora=tra.NombreSociedadAdministradora ;
			traspasoImagen.DocumentoIdentidadEntidadRecaudadora =tra.DocumentoIdentidadEntidadRecaudadora ;	  
			traspasoImagen.IdCliente=tra.IdCliente ;
			traspasoImagen.RazonSocial=tra.RazonSocial ;
			traspasoImagen.YearComercial= tra.YearComercial;
			traspasoImagen.Direccion=tra.Direccion ;
			traspasoImagen.DocumentoIdentidad=tra.DocumentoIdentidad ;
			traspasoImagen.NumeroCertificado= new Long (contadorCertificado);
			traspasoImagen.LogoCorredora=logoCorredoraB64;
			traspasoImagen.FirmaCorredora= firmaCorredoraB64;
			
			listaTraspasosReporte.add(traspasoImagen);
			contadorCertificado++;			
		}		
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::GenerarCertificado31Masivo.obtenerTraspasoReporte {}", fin - inicio);

		return listaTraspasosReporte;
	}


	private List<DatosCertificado> obtenerListaCertificados(Connection conn,EntradaGenerarCertificado31Masivo entrada)  {
		logger.debug("INICIO::GenerarCertificado31Masivo.obtenerListaCertificados");
		long inicio = System.currentTimeMillis();
		List<DatosCertificado> listaCertificados =new ArrayList<DatosCertificado>();			
		
		SQL sql = new SQL();		
		
		try {
			SQL.exec(conn, SQL_CREATE_TABLA_TRASPASO);			
			sql.batch(conn,SQL_INSERT_TABLA_TRASPASO ,entrada.p_listaTraspasos, Traspaso.class);
			
			
			HashMap<String,Object> paramIN = new HashMap<>();
			paramIN.put("IdEmpresa", entrada.p_IdEmpresa);
			paramIN.put("IdUsuario", entrada.p_IdUsuario);
			paramIN.put("IdModulo" , entrada.p_IdModulo);		
			
			HashMap<String, Class> paramOUT = new HashMap<>();
			paramOUT.put("p_Salida", DatosCertificado .class);

			HashMap<String, Object> resultadoSP = sql.call(conn, SQL_CALL_CERTIFICADO_31, paramIN, paramOUT);
			listaCertificados = resultadoSP.get("p_Salida") == null ? new ArrayList<>() : (List<DatosCertificado>) resultadoSP.get("p_Salida");
						
		}catch (Exception e) {
			e.printStackTrace();
			
		}finally {
			try {				
				SQL.exec(conn, SQL_DROP_TABLA_TRASPASO);				
				
				logger.debug("INICIO::GenerarCertificado31Masivo.obtenerListaCertificados { SE EJECUTA EL DROP FINAL}");
			} catch (Exception er) { 
				er.printStackTrace();
				logger.debug("INICIO::GenerarCertificado31Masivo.obtenerListaCertificados { NO SE EJECUTA EL DROP FINAL}");
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::GenerarCertificado31Masivo.obtenerListaCertificados {}", fin - inicio);
		return listaCertificados;
	}


	public String obtenerLogoCorredoraB64(Connection conn, Long idEmpresa) throws SQLException {
		logger.debug("INICIO::GenerarCertificado31Masivo.obtenerLogoCorredora");
		long inicio = System.currentTimeMillis();

		HashMap<String, Object> params = new HashMap<>(2);
		params.put("IdEmpresa", idEmpresa);
		Imagen logo = new SQL().query(conn, SQL_SELECT_LOGO_CORREDORA, params, Imagen.class).stream().findFirst().orElse(new Imagen());
		String contenido = Base64.encodeBase64String(logo.Contenido);

		long fin = System.currentTimeMillis();
		logger.debug("FIN::GenerarCertificado31Masivo.obtenerLogoCorredora {}", fin - inicio);

		return contenido;
	}
	
	public String obtenerIdReporte(Connection conn) throws SQLException {
		logger.debug("INICIO::GenerarCertificado31Masivo.obtenerLogoCorredora");
		long inicio = System.currentTimeMillis();
		
		List<Template> idLista = new SQL().query(conn, SQL_SELECT_ID_REPORTE, new HashMap<>(2), Template.class);
		String IdTemplate =idLista.get(0).Id;

		long fin = System.currentTimeMillis();
		logger.debug("FIN::GenerarCertificado31Masivo.obtenerLogoCorredora {}", fin - inicio);

		return IdTemplate;
	}

	public String obtenerFirmaB64(Connection conn, Long idEmpresa) throws SQLException {
		logger.debug("INICIO::GenerarCertificado31Masivo.obtenerLogoCorredora");
		long inicio = System.currentTimeMillis();

		Imagen firma = new SQL().query(conn, SQL_SELECT_FIRMA_CORREDORA,new HashMap<String, Object>(), Imagen.class).stream().findFirst().orElse(new Imagen());
		String contenido = Base64.encodeBase64String(firma.Contenido);

		long fin = System.currentTimeMillis();
		logger.debug("FIN::GenerarCertificado31Masivo.obtenerLogoCorredora {}", fin - inicio);

		return contenido;
	}
	
	public RaizArchivo obtenerRaizArchivo(Connection conn, Long idEmpresa) throws SQLException {
		logger.debug("INICIO::GenerarCertificado31Masivo.obtenerRaizArchivo");
		long inicio = System.currentTimeMillis();

		HashMap<String, Object> params = new HashMap<>(2);
		params.put("IdEmpresa", idEmpresa);

		RaizArchivo raiz = new SQL().query(conn, SQL_SELECT_RAIZ_ARCHIVO, params, RaizArchivo.class).stream().findFirst().orElse(new RaizArchivo());

		long fin = System.currentTimeMillis();
		logger.debug("INICIO::GenerarCertificado31Masivo.obtenerRaizArchivo {}", fin - inicio);

		return raiz;
	}
	
	public static void main(String[] args) {
		System.out.println("== INICIO TEST ==");

		try (Connection conn = PlataformaUtils.getDESAConnection()) {
			EntradaGenerarCertificado31Masivo entrada = new EntradaGenerarCertificado31Masivo();
			entrada.p_IdEmpresa = 1L;
			entrada.p_IdUsuario = 21L;
			entrada.p_IdModulo = "FMM";
			
			Traspaso traspaso = new Traspaso();
			traspaso.IdTraspaso = 389L;
			//traspaso.IdTrabajo = 1L;
			entrada.p_listaTraspasos.add(traspaso);
			
			entrada.p_DataSource = PlataformaUtils.getDESADatasource();

			SalidaGenerarCertificado31Masivo salida = new GenerarCertificado31Masivo().logica(conn, entrada);
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
