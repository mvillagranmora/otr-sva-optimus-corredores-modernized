package cl.bcs.normativa.declaraciones;

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

import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.plataforma.report.api.ReportDocument;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.StringUtils;
import cl.bcs.utils.Utils;

import cl.bcs.normativa.declaraciones.DJ1899.ReporteCertificado24;
import cl.bcs.normativa.declaraciones.DJ1899.ReporteCertificado24.ReporteCertificado24Entrada;
import cl.bcs.normativa.declaraciones.DJ1899.ReporteCertificado24.ReporteCertificado24Salida;

@SuppressWarnings({"rawtypes", "unchecked"})
public class GenerarCertificado24Masivo implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(GenerarCertificado24Masivo.class);	
	
	private final String SQL_SELECT_LOGO_CORREDORA  = loadResourceToString("cl/bcs/normativa/declaraciones/sql/GenerarCertificadoMasivo_SelectLogoCorredora.sql");
	private final String SQL_SELECT_RAIZ_ARCHIVO    = loadResourceToString("cl/bcs/normativa/declaraciones/sql/GenerarCertificadoMasivo_SelectRaizArchivo.sql");	
	private final String SQL_SELECT_FIRMA_CORREDORA = loadResourceToString("cl/bcs/normativa/declaraciones/sql/GenerarCertificadoMasivo_SelectImagenFirma.sql");

	private final String SQL_SELECT_ID_REPORTE      = loadResourceToString("cl/bcs/normativa/declaraciones/sql/GenerarCertificadoMasivo_SelectReporte.sql");
	public  final String SQL_EXIST_TABLA_RUT  		= "select 1 AS \"Tab\" from m_temporary_tables where table_name = '#GENERARCERTIFICADO24MASIVO_RUT'";
	private final String SQL_CREATE_TABLA_RUT 		= "CREATE LOCAL TEMPORARY TABLE #GenerarCertificado24Masivo_RUT (\"Rut\" NVARCHAR(40))";													
	private final String SQL_INSERT_TABLA_RUT		= "INSERT INTO #GenerarCertificado24Masivo_RUT (\"Rut\") VALUES (:Rut)";
	
	
	public  final String SQL_DROP_TABLA_RUT  		= "DROP TABLE #GenerarCertificado24Masivo_RUT";
	private final String SQL_UPDATE_NOMBRE_SALIDA_PROGRAMA_TRIBUTARIO = "UPDATE \"BSCL.Normativa.BD::TRI.ResumenGeneracion\" SET \"NombreArchivo\"=:RutaArchivo, \"Glosa\"=:Glosa, \"TotalRegistrosGenerados\"=:TotalRegistrosGenerados WHERE \"IdProceso\"= :IdProceso";
	/*Servicio de Crystal Report*/
	private final String DJ="1899";
	private final String SQL_SELECT_GRUPO_CERTIFICA = loadResourceToString("cl/bcs/normativa/declaraciones/sql/GenerarCertificadoMasivo_SelectGrupoCertificado24.sql");
	private final String SQL_CALL_CERTIFICADO	    = loadResourceToString("cl/bcs/normativa/declaraciones/sql/GenerarCertificadoMasivo_certificado24_Call.sql");	

	private final String SQL_UPDATE_CERTIFICADO_ESTADO = loadResourceToString("cl/bcs/normativa/jobs/sql/BcsJobGenerarCertificadosTributarios_UpdateCertificadoClienteEstado.sql");

	private final Log log = new Log("NOR.GenerarCertificado24Masivo");
	
	private static final Map<String, String> esquemaCertificado = new HashMap<>();
	public String v_RutCertificado="";
	public long contadorArchivos=0;
	static {
		esquemaCertificado.put("IdCliente", "number");
		esquemaCertificado.put("LogoCorredora", "blob");
		esquemaCertificado.put("FirmaCorredora", "blob");
	}

	public static class EntradaGenerarCertificado24Masivo extends BcsPojo{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;	
		public String p_EsMasivo;
		public String p_VersionArchivo;
		public String p_IdTipoDocumento;
		public Long   p_IdProceso;
		public String p_NombreZIP;
		
		public List<Rut> p_listaRut;
		
		public transient DataSource p_DataSource;
		
		public EntradaGenerarCertificado24Masivo() {
			this.p_EsMasivo="S";
			this.p_listaRut = new ArrayList<Rut>();
			this.p_IdProceso=0L;
			this.p_NombreZIP="";
		}
		
	} 
	
	public static class CertificadoEstadoRut extends Rut{
		public String IdTipoDocumento;		
		public String VersionArchivo;
		public String IdEstado;
		
		
		public CertificadoEstadoRut(String IdTipoDocumento, String VersionArchivo,String IdEstado,String Rut)
		{
			this.IdTipoDocumento = IdTipoDocumento;
			this.VersionArchivo = VersionArchivo;		
			this.IdEstado = IdEstado;
			this.Rut = Rut;
			
		}
	}
	
	public static class Rut extends BcsPojo{
		public String Rut;		
		
		public Rut () {
			this.Rut = "";			
		}
	}
	
	public static class SalidaGenerarCertificado24Masivo extends BcsPojo {
		public Long P_OUT_ERRORESCONTADOR;
		public List<PlataformaUtils.MensajeSalida> P_OUT_MENSAJES;
		public String p_out_RutaZip;
		public String p_out_NombreZip;
		public Long p_out_contadorArchivos;

		public SalidaGenerarCertificado24Masivo() {
			P_OUT_ERRORESCONTADOR = 0L;
			P_OUT_MENSAJES = new ArrayList<>();
		}
	}
	
	public static class ResumenGeneracion extends BcsPojo{
		public Long 	IdProceso;
		public String 	RutaArchivo;
		public String 	Glosa;
		public Long TotalRegistrosGenerados;
		
		public ResumenGeneracion(Long IdProceso, String RutaArchivo, String Glosa,Long TotalRegistrosGenerados)
		{
			this.IdProceso = IdProceso;
			this.RutaArchivo = RutaArchivo;		
			this.Glosa = Glosa;
			this.TotalRegistrosGenerados = TotalRegistrosGenerados;
		}
	}
	
	public static class Certificado extends BcsPojo{
		public String IdTipoDocumento;		
		public String VersionArchivo;
	}
	
	public static class Imagen extends BcsPojo {
		public byte[] Contenido;

		public Imagen() {
			Contenido = new byte[0];
		}
	}
	
	public static class RaizArchivo extends BcsPojo {
		public String RutaArchivo;
		public String RutaNOR;
	}
	
	public static class Template extends BcsPojo {
		public String Id;
	}
	
	public static class ProgramaBatch extends BcsPojo{
		public Long 	IdPrograma;
		public String 	RutaArchivo;
				
		public ProgramaBatch(Long IdPrograma, String RutaArchivo)
		{
			this.IdPrograma = IdPrograma;
			this.RutaArchivo = RutaArchivo;		
		}
	}

	public static class ExisteTabRut extends BcsPojo{
		public Long count;
	}
	
	public static class DatosCertificado extends BcsPojo{

		public Long Folio;
		public Long IdEmpresa;
		public String Esquema;
		public String Version;
		public String Anio;
		public String Periodo;

		public String NombreEntidadRecaudadora;
		public String DocumentoIdentidadEntidad;	  
		public String DireccionEntidadRecaudadora;
		public Long IdCliente;
		public String NombreCompletoCliente;
		public String DocumentoIdentidadCliente;
		public String DireccionCliente;
		public String RazonSocialEmpresa;
		public String DocumentoIdentidadEmpresa;
		public String DireccionEmpresa;
		public String CiudadFecha;	
		public Long Mes;
		public String DescripcionMes;
		public BigDecimal MontoModalidadEmpleador;
		public BigDecimal MontoModalidadTrabajador;
		public BigDecimal MontoCotizacionesObligatorias;
		public BigDecimal MontoCotizacionesVoluntarias;
		public BigDecimal MontoTrabajadoresActivos;
		public BigDecimal MontoPensionados;
		public BigDecimal MontoRetencion;
		public String Certificacion;
		public String EstadoCertificado;
		public Long ActivoPensionado;
		public BigDecimal AhorrosModalidadIndirectaEmpleador;
		public BigDecimal AhorrosModalidadDirectaTrabajador;
		public BigDecimal CotizacionesModalidadIndirectaEmpleador;
		public BigDecimal CotizacionesModalidadDirectaTrabajador;
		public BigDecimal CotizacionesTrabajadorIndependiente;
		public BigDecimal MontoRetiroPorAhorrosIncisoPrimero;
		public BigDecimal RetencionImpuestoSobreRetiros;
		public BigDecimal MontoRetiroPorAhorrosIncisoSegundo;
		public BigDecimal RetiroEmpleadorActualizado;
		
		public BigDecimal ResumenTrabajadorActivo;
		public BigDecimal ResumenTrabajadorPensionado;

		public BigDecimal TotalActivo;
		public BigDecimal TotalPensionado;

		public String RazonSocialCorredora;
		public String DireccionCorredora;
		public String DocumentoIdentidadCorredora;

		public String rut_representante;
		public String nombre_representante;
		
		public String RutCertificado;
		
		public DatosCertificado() {
			this.Folio = 0L;
		} 
	
	}
	
	public static class DatosCertificadoCompleto extends DatosCertificado{
		public String LogoCorredora;
		public String FirmaCorredora;	
	}
	
	@Override	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		return null;
	}
	
	public SalidaGenerarCertificado24Masivo logica(Connection conn, EntradaGenerarCertificado24Masivo entrada) throws Exception {
		logger.info("INICIO::GenerarCertificado24Masivo");
		long inicio = System.currentTimeMillis();
		log.write( "*GenerarCertificado24Masivo* INICIO");
		String Estado="Procesando";
		SQL sql = new SQL();
		SalidaGenerarCertificado24Masivo salida = new SalidaGenerarCertificado24Masivo();
		
		String logoCorredoraB64  = obtenerLogoCorredoraB64(conn, entrada.p_IdEmpresa);
		String firmaCorredoraB64 = obtenerFirmaB64(conn, entrada.p_IdEmpresa);
		log.write( "*GenerarCertificado24Masivo* obtenerRaizArchivo");
		RaizArchivo raizArchivo = obtenerRaizArchivo(conn, entrada.p_IdEmpresa);
		if (StringUtils.isNullOrBlank(raizArchivo.RutaArchivo) || StringUtils.isNullOrBlank(raizArchivo.RutaNOR)) {
			salida.P_OUT_ERRORESCONTADOR++;
			salida.P_OUT_MENSAJES.add(new PlataformaUtils.MensajeSalida(0L, PlataformaUtils.MensajeSalida.MENSAJE_ERROR, "FALTA_CONFIGURACION_RUTA_ARCHIVO", new HashMap<>(), conn, entrada.p_IdEmpresa));
			return salida;
		}
		log.write( "*GenerarCertificado24Masivo* RutaArchivo" + raizArchivo.RutaArchivo);
		// Para pruebas locales.
		//final String rutaAbsolutaBase = System.getProperty("user.home") + File.separator + "Desktop" + File.separator + "Certificado31" + File.separator;
		final String rutaAbsolutaBase = raizArchivo.RutaArchivo;
		final String rutaRelativaBase = raizArchivo.RutaNOR;
		final String nombreBaseArchivo = "CertTRI24_";
		String nombreBaseZip = "CerTRI24_";
		if (entrada.p_NombreZIP !=""){
			nombreBaseZip=entrada.p_NombreZIP;
		}
		final String timestampSistema = Fecha.toFormat(FechaDB.ObtenerFechaHoraSistemaTs(conn, entrada.p_IdEmpresa), "yyyyMMdd_HHmmss");
		
		//buscar datos 		
		log.write( "*GenerarCertificado24Masivo* obtenerListaCertificados");
		List<DatosCertificado> listaCerificados = obtenerListaCertificados (conn , entrada);
		
		if(listaCerificados.isEmpty()) {
			salida.P_OUT_MENSAJES.add(new MensajeSalida( 0L, MensajeSalida.MENSAJE_ERROR, "UI_SIN_DATOS", "No se encontraron datos consultados"));
			salida.P_OUT_ERRORESCONTADOR = Long.valueOf(salida.P_OUT_MENSAJES.size());
			return salida;
		};
		log.write( "*GenerarCertificado24Masivo* LeerNombreArchivos");
		LeerNombreArchivos(conn,entrada.p_IdEmpresa,entrada.p_IdTipoDocumento,entrada.p_VersionArchivo,DJ);
		
		log.write( "*GenerarCertificado24Masivo* obtenerTraspasoReporte");
		List<DatosCertificadoCompleto> traspasosInformacion= obtenerTraspasoReporte(listaCerificados, logoCorredoraB64,firmaCorredoraB64);
		log.write( "*GenerarCertificado24Masivo* obtenerGrupoCertificado");
		List<Rut> GrupoCertificado=obtenerGrupoCertificado(conn , entrada);
		if(GrupoCertificado.size()>0) {
			 List<ResumenGeneracion> programa = new ArrayList<ResumenGeneracion>();
				if (entrada.p_IdProceso>0){
					Estado="Iniciando PDF:< " + GrupoCertificado.size() + " > ";
					programa.add(new ResumenGeneracion(entrada.p_IdProceso, "",Estado,0L));
		        	sql.batch(conn, SQL_UPDATE_NOMBRE_SALIDA_PROGRAMA_TRIBUTARIO, programa,ResumenGeneracion.class);
		        	conn.commit();
				};
		}
		
		log.write( "*GenerarCertificado24Masivo* generarArchivosInformes");
		List<String> rutasArchivosSinComprimir = generarArchivosInformes(conn,entrada.p_DataSource, rutaAbsolutaBase, nombreBaseArchivo, traspasosInformacion,entrada,GrupoCertificado);
		log.write( "*GenerarCertificado24Masivo* comprimirArchivos");
		salida.p_out_NombreZip = comprimirArchivos(timestampSistema, rutaAbsolutaBase, rutaRelativaBase, nombreBaseZip, rutasArchivosSinComprimir);
		salida.p_out_RutaZip= rutaRelativaBase + salida.p_out_NombreZip;
		salida.p_out_contadorArchivos=contadorArchivos;
		//salida.p_out_RutaZip="nor/Certificado24-10005725-5.pdf";
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::GenerarCertificado24Masivo {}", fin - inicio);
		log.write( "*GenerarCertificado24Masivo* FIN");
		return salida;
	}
	
	


	private String comprimirArchivos(String timestampSistema, String rutaAbsolutaBase, String rutaRelativaBase,	String nombreBaseZip, List<String> rutasArchivos) throws Exception {
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
		logger.debug("FIN::GenerarInformeRentabilidadFFMMExtranjeroMasivo.comprimirArchivos", nombreZip);
		logger.debug("FIN::GenerarInformeRentabilidadFFMMExtranjeroMasivo.comprimirArchivos {}", fin - inicio);
		return nombreZip;
	}


	private List<String> generarArchivosInformes(Connection conn,DataSource dataSource, String rutaAbsoluta,String nombreBaseArchivo, List<DatosCertificadoCompleto> traspasosInformacion,EntradaGenerarCertificado24Masivo entrada,List<Rut> rutLista) throws Exception {
		logger.debug("INICIO::GenerarCertificado24Masivo.generarArchivosInformes");
		String updTramo = entrada.p_NombreZIP.replace("TRI_CERT24_PT", "P");
		updTramo=updTramo.replace("de", "/");
		updTramo=updTramo.replace("_", "");
		long inicio = System.currentTimeMillis();
	
		final List<String> rutasArchivos = new ArrayList<>();
		final ReportDocument report = new ReportDocument(Utils.getSchema(), dataSource);		
		SQL sql = new SQL();
		String RutCliente,CodigoPortafolio,param1aux,param2aux,param3aux,param4aux,param5aux="";
		long contresumen = 0;
		List<ResumenGeneracion> programa = new ArrayList<ResumenGeneracion>();
		for (Rut rut: rutLista) {
			contadorArchivos++;
			contresumen++;
			List<DatosCertificadoCompleto> listaTraspasoFinal = new ArrayList<>();
					for (DatosCertificadoCompleto mov: traspasosInformacion) {
				
						if (mov.RutCertificado.contains(rut.Rut))
						{
							try {
								
								listaTraspasoFinal.add(mov);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
					
					
					  //NOMBRE DEL ARCHIVO PDF
					String fileName="";
					if((param1 +param2+param3+param4+param5 ).length()!=0)
					{
						 if (param1.equalsIgnoreCase("[RUT]")){param1aux=rut.Rut;}else{param1aux=param1;};
						 if (param2.equalsIgnoreCase("[RUT]")){param2aux=rut.Rut;}else{param2aux=param2;};
						 if (param3.equalsIgnoreCase("[RUT]")){param3aux=rut.Rut;}else{param3aux=param3;};
						 if (param4.equalsIgnoreCase("[RUT]")){param4aux=rut.Rut;}else{param4aux=param4;};
						 if (param5.equalsIgnoreCase("[RUT]")){param4aux=rut.Rut;}else{param5aux=param5;};
						fileName =param1aux +param2aux+param3aux+param4aux+param5aux;
					}else{	
						fileName =nombreBaseArchivo + "_"+  rut.Rut + "_"+   String.format("%03d",contadorArchivos);	
					};
					String rutaBaseReporte = rutaAbsoluta + fileName;
					rutasArchivos.add(generarReportePDF(conn,report, rutaBaseReporte, listaTraspasoFinal,entrada));
					
					//INICIO TRAMOS
					List<CertificadoEstadoRut> certEstadoRut = new ArrayList<CertificadoEstadoRut>();
					certEstadoRut.add(new CertificadoEstadoRut("CERT24", entrada.p_VersionArchivo,"GEN",rut.Rut));
					sql.batch(conn, SQL_UPDATE_CERTIFICADO_ESTADO, certEstadoRut,CertificadoEstadoRut.class); //ESTADOS
					conn.commit();
					log.write( "*BcsJobGenerarCertificadosTributarios* SQL_UPDATE_CERTIFICADO_ESTADO");
						//FIN TRAMOS

					if (contresumen==50){
						String Estado=updTramo+ " < " + rutLista.size() + " > ";
						programa.add(new ResumenGeneracion(entrada.p_IdProceso, "",Estado,contadorArchivos));
			        	sql.batch(conn, SQL_UPDATE_NOMBRE_SALIDA_PROGRAMA_TRIBUTARIO, programa,ResumenGeneracion.class);
			        	conn.commit();
			        	contresumen=0;
					};
		}
		
		if (rutLista.size()>0){
			String Estado=updTramo+ " < Comprimiendo >";
			programa.add(new ResumenGeneracion(entrada.p_IdProceso, "",Estado,contadorArchivos));
        	sql.batch(conn, SQL_UPDATE_NOMBRE_SALIDA_PROGRAMA_TRIBUTARIO, programa,ResumenGeneracion.class);
        	conn.commit();
        	contresumen=0;
		};
		
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::GenerarCertificado24Masivo.obtenerTraspasoReporte {}", fin - inicio);
		return rutasArchivos;
	}
	
	
	private String generarReportePDF(Connection conn,ReportDocument report, String rutaBase, List<DatosCertificadoCompleto> listaTraspasoFinal,EntradaGenerarCertificado24Masivo entrada) throws Exception {
		logger.trace("INICIO::GenerarCertificado24Masivo.generarReportePDF");
		long inicio = System.currentTimeMillis();

		String rutaReporte = rutaBase + ".pdf";
		String json = Utils.getGsonBuilder().toJson(listaTraspasoFinal);
		JsonArray informacionCertificado = new JsonParser().parse(json).getAsJsonArray();
		String idTemplate =obtenerIdReporte(conn,entrada);// "173603C07D0185DB1700D20086E4297E";
		log.write( "*GenerarCertificado24Masivo* idTemplate" + idTemplate);
		if (report.readDocument(idTemplate)) {
			try {
				report.setDocumentResultSet(esquemaCertificado, informacionCertificado);
			} catch (Exception e) {
				logger.error(e.getMessage());
			}

			try (InputStream is = report.exportDocument("pdf")) {
				File reportePDF = new File(rutaReporte);
				FileUtils.copyInputStreamToFile(is, reportePDF);
				log.write( "*GenerarCertificado24Masivo* generarReportePDF" + rutaReporte);
			} catch (Exception e) {
				logger.error(e.getMessage());
			}
		} else {
			logger.error("Error al obtener el template {}, generacion de PDF omitida", idTemplate);
		}

		long fin = System.currentTimeMillis();
		logger.trace("FIN::GenerarCertificado24Masivo.generarReportePDF {}", fin - inicio);

		return rutaReporte;
	}


	private List<DatosCertificadoCompleto> obtenerTraspasoReporte(List<DatosCertificado> listaCerificados, String logoCorredoraB64, String firmaCorredoraB64) {
		logger.debug("INICIO::GenerarCertificado24Masivo.obtenerTraspasoReporte");
		long inicio = System.currentTimeMillis();
		Integer contadorCertificado=1;
		
		List<DatosCertificadoCompleto>  listaTraspasosReporte = new ArrayList<DatosCertificadoCompleto>();
		
		for (DatosCertificado tra : listaCerificados) {
			DatosCertificadoCompleto traspasoImagen = new DatosCertificadoCompleto();

			traspasoImagen.Folio		=tra.Folio;
			traspasoImagen.IdEmpresa	=tra.IdEmpresa;
			traspasoImagen.Esquema		=tra.Esquema;
			traspasoImagen.Version		=tra.Version;
			traspasoImagen.Anio			=tra.Anio;
			traspasoImagen.Periodo		=tra.Periodo;


			traspasoImagen.NombreEntidadRecaudadora		=tra.NombreEntidadRecaudadora;
			traspasoImagen.DocumentoIdentidadEntidad	=tra.DocumentoIdentidadEntidad;	  
			traspasoImagen.DireccionEntidadRecaudadora	=tra.DireccionEntidadRecaudadora;
			traspasoImagen.IdCliente					=tra.IdCliente;
			traspasoImagen.NombreCompletoCliente		=tra.NombreCompletoCliente;
			traspasoImagen.DocumentoIdentidadCliente	=tra.DocumentoIdentidadCliente;
			traspasoImagen.DireccionCliente				=tra.DireccionCliente;
			traspasoImagen.RazonSocialEmpresa			=tra.RazonSocialEmpresa;
			traspasoImagen.DocumentoIdentidadEmpresa	=tra.DocumentoIdentidadEmpresa;
			traspasoImagen.DireccionEmpresa				=tra.DireccionEmpresa;
			traspasoImagen.CiudadFecha					=tra.CiudadFecha;	
			traspasoImagen.Mes							=tra.Mes;
			traspasoImagen.DescripcionMes				=tra.DescripcionMes;
			traspasoImagen.MontoModalidadEmpleador		=tra.MontoModalidadEmpleador;
			traspasoImagen.MontoModalidadTrabajador		=tra.MontoModalidadTrabajador;
			traspasoImagen.MontoCotizacionesObligatorias=tra.MontoCotizacionesObligatorias;
			traspasoImagen.MontoCotizacionesVoluntarias	=tra.MontoCotizacionesVoluntarias;
			traspasoImagen.MontoTrabajadoresActivos		=tra.MontoTrabajadoresActivos;
			traspasoImagen.MontoPensionados				=tra.MontoPensionados;
			traspasoImagen.MontoRetencion				=tra.MontoRetencion;
			traspasoImagen.Certificacion				=tra.Certificacion;
			traspasoImagen.EstadoCertificado			=tra.EstadoCertificado;
			traspasoImagen.ActivoPensionado				=tra.ActivoPensionado;

			traspasoImagen.AhorrosModalidadIndirectaEmpleador		=tra.AhorrosModalidadIndirectaEmpleador;
			traspasoImagen.AhorrosModalidadDirectaTrabajador		=tra.AhorrosModalidadDirectaTrabajador;
			traspasoImagen.CotizacionesModalidadIndirectaEmpleador	=tra.CotizacionesModalidadIndirectaEmpleador;
			traspasoImagen.CotizacionesModalidadDirectaTrabajador	=tra.CotizacionesModalidadDirectaTrabajador;
			traspasoImagen.CotizacionesTrabajadorIndependiente		=tra.CotizacionesTrabajadorIndependiente;
			traspasoImagen.MontoRetiroPorAhorrosIncisoPrimero		=tra.MontoRetiroPorAhorrosIncisoPrimero;
			traspasoImagen.RetencionImpuestoSobreRetiros			=tra.RetencionImpuestoSobreRetiros;
			traspasoImagen.MontoRetiroPorAhorrosIncisoSegundo		=tra.MontoRetiroPorAhorrosIncisoSegundo;
			traspasoImagen.RetiroEmpleadorActualizado				=tra.RetiroEmpleadorActualizado;

			traspasoImagen.ResumenTrabajadorActivo				    =tra.ResumenTrabajadorActivo;
			traspasoImagen.ResumenTrabajadorPensionado				=tra.ResumenTrabajadorPensionado;

			traspasoImagen.TotalActivo				    =tra.TotalActivo;
			traspasoImagen.TotalPensionado				=tra.TotalPensionado;

			traspasoImagen.RazonSocialCorredora         =tra.RazonSocialCorredora;
			traspasoImagen.DireccionCorredora           =tra.DireccionCorredora;
			traspasoImagen.DocumentoIdentidadCorredora  =tra.DocumentoIdentidadCorredora;			
			traspasoImagen.rut_representante            =tra.rut_representante;
			traspasoImagen.nombre_representante         =tra.nombre_representante;
			traspasoImagen.RutCertificado               =tra.RutCertificado;
			
			traspasoImagen.LogoCorredora    =logoCorredoraB64;
			traspasoImagen.FirmaCorredora   =firmaCorredoraB64;
			
			listaTraspasosReporte.add(traspasoImagen);
			contadorCertificado++;			
		}		
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::GenerarCertificado24Masivo.obtenerTraspasoReporte {}", fin - inicio);

		return listaTraspasosReporte;
	}


	private List<DatosCertificado> obtenerListaCertificados(Connection conn,EntradaGenerarCertificado24Masivo entrada)  {
		logger.debug("INICIO::GenerarCertificado24Masivo.obtenerListaCertificados");
		long inicio = System.currentTimeMillis();
		List<DatosCertificado> listaCertificados =new ArrayList<DatosCertificado>();			
		
		SQL sql = new SQL();		
		
		try {
			HashMap<String, Object> params = new HashMap<>(2);
			List<ExisteTabRut> TabRut =new ArrayList<ExisteTabRut>();
			TabRut = new SQL().query(conn, SQL_EXIST_TABLA_RUT, params, ExisteTabRut.class);
			if(TabRut.size()>0) {
				try {				
					SQL.exec(conn, SQL_DROP_TABLA_RUT);				
					log.write( "*GenerarCertificado24Masivo* { SE EJECUTA EL DROP INICIAL}");
				} catch (Exception er) { 
					er.printStackTrace();
					log.write("*GenerarCertificado24Masivo* {DROP INICIAL, NO LOGRADO}");
				}
				
			}else{
				log.write("*GenerarCertificado24Masivo* { NO SE EJECUTA EL DROP INICIAL}");
			}
			
			
			SQL.exec(conn, SQL_CREATE_TABLA_RUT);			
			sql.batch(conn,SQL_INSERT_TABLA_RUT ,entrada.p_listaRut, Rut.class);
			
			
			HashMap<String,Object> paramIN = new HashMap<>();
			paramIN.put("IdEmpresa", entrada.p_IdEmpresa);
			paramIN.put("IdUsuario", entrada.p_IdUsuario);
			paramIN.put("IdModulo" , entrada.p_IdModulo);		
			paramIN.put("Version" , entrada.p_VersionArchivo);//entrada.p_Certificado.VersionArchivo);
			HashMap<String, Class> paramOUT = new HashMap<>();
			paramOUT.put("p_Salida", DatosCertificado.class);

			//HashMap<String, Object> resultadoSP = sql.call(conn, SQL_CALL_CERTIFICADO, paramIN, paramOUT);
			//listaCertificados = resultadoSP.get("p_Salida") == null ? new ArrayList<>() : (List<DatosCertificado>) resultadoSP.get("p_Salida");
			
			//inicio reporte JAVA
			ReporteCertificado24 Certificado24 = new ReporteCertificado24();
			ReporteCertificado24Entrada EntradaCertificado24 = new ReporteCertificado24Entrada();
			
			List<ReporteCertificado24Salida> SalidaCertificado24 = new ArrayList<ReporteCertificado24Salida>();
			
			List<ResumenGeneracion> prog = new ArrayList<ResumenGeneracion>();
			String Est="< Buscando... >";
			prog.add(new ResumenGeneracion(entrada.p_IdProceso, "v 2.0",Est,0L));
        	sql.batch(conn, SQL_UPDATE_NOMBRE_SALIDA_PROGRAMA_TRIBUTARIO, prog,ResumenGeneracion.class);
        	conn.commit();
	
			
			EntradaCertificado24.p_IdEmpresa	=entrada.p_IdEmpresa;
			EntradaCertificado24.p_IdModulo 	=entrada.p_IdModulo;
			EntradaCertificado24.p_IdUsuario	=entrada.p_IdUsuario;
			EntradaCertificado24.p_Version		=entrada.p_VersionArchivo;
			SalidaCertificado24 = Certificado24.logica(conn,EntradaCertificado24);

			if (SalidaCertificado24.size()>0) {
				
				List<ResumenGeneracion> prog2 = new ArrayList<ResumenGeneracion>();
				String Est2="< Encontrados... >";
				prog2.add(new ResumenGeneracion(entrada.p_IdProceso, "",Est2,0L));
	        	sql.batch(conn, SQL_UPDATE_NOMBRE_SALIDA_PROGRAMA_TRIBUTARIO, prog2,ResumenGeneracion.class);
	        	conn.commit();
				listaCertificados=AgregarCertificado( SalidaCertificado24);
				log.write("*GenerarCertificado9Masivo* { arma lista desde JAVA}" + listaCertificados.toString());
				
				
			}
			//Fin reporte JAVA

		}catch (Exception e) {
			e.printStackTrace();
	
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::GenerarCertificado24Masivo.obtenerListaCertificados {}", fin - inicio);
		return listaCertificados;
	}
	
	private List<DatosCertificado> AgregarCertificado(List<ReporteCertificado24Salida> SalidaCertificado24)  {
		List<DatosCertificado> o = new ArrayList<DatosCertificado>();
		
		for (ReporteCertificado24Salida  datos : SalidaCertificado24) {
			DatosCertificado out = new DatosCertificado();
			
			out.Folio=datos.Folio;
			out.IdEmpresa=datos.IdEmpresa;
			out.Esquema=datos.Esquema;
			out.Version=datos.Version;
			out.Anio=datos.Anio;
			out.Periodo=datos.Periodo;
			
			out.RazonSocialEmpresa=datos.RazonSocialEmpresa;
			out.DocumentoIdentidadCliente=datos.DocumentoIdentidadCliente;
			out.DireccionEmpresa=datos.DireccionEmpresa;
			out.NombreEntidadRecaudadora=datos.NombreEntidadRecaudadora;
			out.IdCliente=datos.IdCliente;
			out.NombreCompletoCliente=datos.NombreCompletoCliente;
			out.DocumentoIdentidadEmpresa=datos.DocumentoIdentidadEmpresa;
			out.CiudadFecha=datos.CiudadFecha;
			
			out.Mes= (long) datos.Mes;
			out.DescripcionMes=datos.DescripcionMes;
			out.DocumentoIdentidadEntidad=datos.DocumentoIdentidadEntidad;	  
			out.DireccionEntidadRecaudadora=datos.DireccionEntidadRecaudadora;
			out.DireccionCliente=datos.DireccionCliente;

			out.MontoModalidadEmpleador=datos.MontoModalidadEmpleador;
			out.MontoModalidadTrabajador=datos.MontoModalidadTrabajador;
			out.MontoCotizacionesObligatorias=datos.MontoCotizacionesObligatorias;
			out.MontoCotizacionesVoluntarias=datos.MontoCotizacionesVoluntarias;
			out.MontoTrabajadoresActivos=datos.MontoTrabajadoresActivos;
			out.MontoPensionados=datos.MontoPensionados;
			out.MontoRetencion=datos.MontoRetencion;
			out.Certificacion=datos.Certificacion;
			out.EstadoCertificado=datos.EstadoCertificado;
			out.ActivoPensionado=datos.ActivoPensionado;

			out.AhorrosModalidadIndirectaEmpleador=datos.AhorrosModalidadIndirectaEmpleador;
			out.AhorrosModalidadDirectaTrabajador=datos.AhorrosModalidadDirectaTrabajador;
			out.CotizacionesModalidadIndirectaEmpleador=datos.CotizacionesModalidadIndirectaEmpleador;
			out.CotizacionesModalidadDirectaTrabajador=datos.CotizacionesModalidadDirectaTrabajador;
			out.CotizacionesTrabajadorIndependiente=datos.CotizacionesTrabajadorIndependiente;
			out.MontoRetiroPorAhorrosIncisoPrimero=datos.MontoRetiroPorAhorrosIncisoPrimero;
			out.RetencionImpuestoSobreRetiros=datos.RetencionImpuestoSobreRetiros;
			out.MontoRetiroPorAhorrosIncisoSegundo=datos.MontoRetiroPorAhorrosIncisoSegundo;
			out.RetiroEmpleadorActualizado=datos.RetiroEmpleadorActualizado;

			out.ResumenTrabajadorActivo=datos.ResumenTrabajadorActivo;
			out.ResumenTrabajadorPensionado=datos.ResumenTrabajadorPensionado;

			out.TotalActivo=datos.TotalActivo;
			out.TotalPensionado=datos.TotalPensionado;
			
			out.RazonSocialCorredora=datos.RazonSocialCorredora;
			out.DireccionCorredora=datos.DireccionCorredora;
			out.DocumentoIdentidadCorredora=datos.DocumentoIdentidadCorredora;
			out.rut_representante=datos.rut_representante;
			out.nombre_representante=datos.nombre_representante;
			
			out.Certificacion="N";
			out.EstadoCertificado="NGE";
			out.RutCertificado=datos.DocumentoIdentidadCliente;
			
			o.add(out);
		}
		
		return o;
	}
	
	private List<Rut> obtenerGrupoCertificado(Connection conn,EntradaGenerarCertificado24Masivo entrada)  {
		logger.debug("INICIO::GenerarCertificado24Masivo.obtenerGrupoCertificado");
		long inicio = System.currentTimeMillis();
		List<DatosCertificado> listaCertificados =new ArrayList<DatosCertificado>();			
		
		SQL sql = new SQL();		
		List<Rut> rutLista =new ArrayList<Rut>();
		
		try {
						
			
			HashMap<String, Object> params = new HashMap<>(2);
			params.put("IdEmpresa", entrada.p_IdEmpresa);
			params.put("IdTipoDocumento", entrada.p_IdTipoDocumento);
			params.put("Version", entrada.p_VersionArchivo);
			rutLista = new SQL().query(conn, SQL_SELECT_GRUPO_CERTIFICA, params, Rut.class);
	
		}catch (Exception e) {
			e.printStackTrace();
			
		}finally {
			try {				
				SQL.exec(conn, SQL_DROP_TABLA_RUT);				
				
				logger.debug("INICIO::GenerarCertificado24Masivo.obtenerListaCertificados { SE EJECUTA EL DROP FINAL}");
			} catch (Exception er) { 
				er.printStackTrace();
				logger.debug("INICIO::GenerarCertificado24Masivo.obtenerListaCertificados { NO SE EJECUTA EL DROP FINAL}");
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::GenerarCertificado24Masivo.obtenerListaCertificados {}", fin - inicio);
		return rutLista;
	}

	public String obtenerLogoCorredoraB64(Connection conn, Long idEmpresa) throws SQLException {
		logger.debug("INICIO::GenerarCertificado24Masivo.obtenerLogoCorredora");
		long inicio = System.currentTimeMillis();

		HashMap<String, Object> params = new HashMap<>(2);
		params.put("IdEmpresa", idEmpresa);
		Imagen logo = new SQL().query(conn, SQL_SELECT_LOGO_CORREDORA, params, Imagen.class).stream().findFirst().orElse(new Imagen());
		String contenido = Base64.encodeBase64String(logo.Contenido);

		long fin = System.currentTimeMillis();
		logger.debug("FIN::GenerarCertificado24Masivo.obtenerLogoCorredora {}", fin - inicio);

		return contenido;
	}
	
	public String obtenerIdReporte(Connection conn, EntradaGenerarCertificado24Masivo entrada) throws SQLException {
		logger.debug("INICIO::GenerarCertificado24Masivo.obtenerLogoCorredora");
		long inicio = System.currentTimeMillis();
		HashMap<String, Object> params = new HashMap<>(2);
		params.put("IdEmpresa", entrada.p_IdEmpresa);
		params.put("IdTipoDocumento", entrada.p_IdTipoDocumento);
		params.put("VersionArchivo", entrada.p_VersionArchivo);
		List<Template> idLista = new SQL().query(conn, SQL_SELECT_ID_REPORTE, params, Template.class);
		//List<Template> idLista = new SQL().query(conn, SQL_SELECT_ID_REPORTE, new HashMap<>(2), Template.class);
		String IdTemplate =idLista.get(0).Id;

		long fin = System.currentTimeMillis();
		logger.debug("FIN::GenerarCertificado24Masivo.obtenerLogoCorredora {}", fin - inicio);

		return IdTemplate;
	}

	public String obtenerFirmaB64(Connection conn, Long idEmpresa) throws SQLException {
		logger.debug("INICIO::GenerarCertificado24Masivo.obtenerLogoCorredora");
		long inicio = System.currentTimeMillis();

		Imagen firma = new SQL().query(conn, SQL_SELECT_FIRMA_CORREDORA,new HashMap<String, Object>(), Imagen.class).stream().findFirst().orElse(new Imagen());
		String contenido = Base64.encodeBase64String(firma.Contenido);

		long fin = System.currentTimeMillis();
		logger.debug("FIN::GenerarCertificado24Masivo.obtenerLogoCorredora {}", fin - inicio);

		return contenido;
	}
	
	public RaizArchivo obtenerRaizArchivo(Connection conn, Long idEmpresa) throws SQLException {
		logger.debug("INICIO::GenerarCertificado24Masivo.obtenerRaizArchivo");
		long inicio = System.currentTimeMillis();

		HashMap<String, Object> params = new HashMap<>(2);
		params.put("IdEmpresa", idEmpresa);

		RaizArchivo raiz = new SQL().query(conn, SQL_SELECT_RAIZ_ARCHIVO, params, RaizArchivo.class).stream().findFirst().orElse(new RaizArchivo());

		long fin = System.currentTimeMillis();
		logger.debug("INICIO::GenerarCertificado24Masivo.obtenerRaizArchivo {}", fin - inicio);

		return raiz;
	}
	
	private String param1,param2,param3,param4,param5="";
    public void LeerNombreArchivos(Connection dbcon, Long idEmpresa, String nombreReporte, String version,String esquema ) throws Exception{
    	System.out.println("Obtener parametros para nombre de PDFs");
    	nombreReporte=nombreReporte.replace("CERT", "CERTIFICADO");
    	try {
    		param1=ConfiguracionSistema.getParametroConfiguracionString(dbcon, idEmpresa, "NORMATIVA", "TRI_NOM_" + nombreReporte + "_01");
				System.out.println("param1:" + param1);
				if(param1.length()==0)
				{
					param1="";
				};
			} catch (Exception e) {
				param1="";
			}
    	try {
	    	param2=ConfiguracionSistema.getParametroConfiguracionString(dbcon, idEmpresa, "NORMATIVA", "TRI_NOM_" + nombreReporte + "_02");
			System.out.println("param2:" + param2);
				if(param2.length()==0)
				{
					param2="";
				};
			} catch (Exception e) {
				param2="";
			}		    
    	try {
    		param3=ConfiguracionSistema.getParametroConfiguracionString(dbcon, idEmpresa, "NORMATIVA", "TRI_NOM_" + nombreReporte + "_03");
				System.out.println("param3:" + param3);
				if(param3.length()==0)
				{
					param3="";
				};
			} catch (Exception e) {
				param3="";
			}
    	try {
	    	param4=ConfiguracionSistema.getParametroConfiguracionString(dbcon, idEmpresa, "NORMATIVA", "TRI_NOM_" + nombreReporte + "_04");
			System.out.println("param4:" + param4);
				if(param4.length()==0)
				{
					param4="";
				};
			} catch (Exception e) {
				param4="";
			}	
    	
    	try {
	    	param5=ConfiguracionSistema.getParametroConfiguracionString(dbcon, idEmpresa, "NORMATIVA", "TRI_NOM_" + nombreReporte + "_05");
			System.out.println("param5:" + param5);
				if(param5.length()==0)
				{
					param5="";
				};
			} catch (Exception e) {
				param5="";
			}	    	
    	    if (param1.equalsIgnoreCase("[PERIODO]")){param1=version;};if (param2.equalsIgnoreCase("[PERIODO]")){param2=version;};if (param3.equalsIgnoreCase("[PERIODO]")){param3=version;};if (param4.equalsIgnoreCase("[PERIODO]")){param4=version;}if (param5.equalsIgnoreCase("[PERIODO]")){param5=version;};
    	    if (param1.equalsIgnoreCase("[DJ]")){param1=esquema.replace("DJ", "");};if (param2.equalsIgnoreCase("[DJ]")){param2=esquema.replace("DJ", "");};if (param3.equalsIgnoreCase("[DJ]")){param3=esquema.replace("DJ", "");};if (param4.equalsIgnoreCase("[DJ]")){param4=esquema.replace("DJ", "");};if (param5.equalsIgnoreCase("[DJ]")){param5=esquema.replace("DJ", "");};
    	    
    	    if (param1.equalsIgnoreCase("[CERT]")){param1=nombreReporte.replace("CERTIFICADO", "");param1 = String.format("%2s",  (param1).length() > 2 ?  (param1).substring(0,2) :  (param1)).replace(' ', '0');};
    	    if (param2.equalsIgnoreCase("[CERT]")){param2=nombreReporte.replace("CERTIFICADO", "");param2 = String.format("%2s",  (param2).length() > 2 ?  (param2).substring(0,2) :  (param2)).replace(' ', '0');};
    	    if (param3.equalsIgnoreCase("[CERT]")){param3=nombreReporte.replace("CERTIFICADO", "");param3 = String.format("%2s",  (param3).length() > 2 ?  (param3).substring(0,2) :  (param3)).replace(' ', '0');};
    	    if (param4.equalsIgnoreCase("[CERT]")){param4=nombreReporte.replace("CERTIFICADO", "");param4 = String.format("%2s",  (param4).length() > 2 ?  (param4).substring(0,2) :  (param4)).replace(' ', '0');};
    	    if (param5.equalsIgnoreCase("[CERT]")){param5=nombreReporte.replace("CERTIFICADO", "");param5 = String.format("%2s",  (param5).length() > 2 ?  (param5).substring(0,2) :  (param5)).replace(' ', '0');};
  	      
	}

	
	public static void main(String[] args) {
		System.out.println("== INICIO TEST ==");

		//::::::: DESA
		try (Connection conn = PlataformaUtils.getDESAConnection()) {
			EntradaGenerarCertificado24Masivo entrada = new EntradaGenerarCertificado24Masivo();
			Certificado Certificado = new Certificado();
			entrada.p_IdEmpresa = 1L;
			entrada.p_IdUsuario = 21L;
			entrada.p_IdModulo = "NOR";
			entrada.p_IdProceso=559065L;
	
			Rut Rut = new Rut();
			// Rut.Rut = "10000040-7";
			
			entrada.p_listaRut.add(Rut);
			entrada.p_VersionArchivo="2023";
			entrada.p_IdTipoDocumento="CERT24";
			
			entrada.p_DataSource = PlataformaUtils.getDESADatasource();

			SalidaGenerarCertificado24Masivo salida = new GenerarCertificado24Masivo().logica(conn, entrada);
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

		//::::::: MASTER
		/*
		try (Connection conn = PlataformaUtils.getMASTER1Connection()) {
			EntradaGenerarCertificado24Masivo entrada = new EntradaGenerarCertificado24Masivo();
			Certificado Certificado = new Certificado();
			entrada.p_IdEmpresa = 1L;
			entrada.p_IdUsuario = 21L;
			entrada.p_IdModulo = "NOR";
			entrada.p_IdProceso=559065L;
	
			Rut Rut = new Rut();
			//Rut.Rut = "1-9";

			entrada.p_listaRut.add(Rut);
			entrada.p_VersionArchivo="2021";
			entrada.p_IdTipoDocumento="CERT24";
			entrada.p_DataSource = PlataformaUtils.getMASTER1Datasource();

			SalidaGenerarCertificado24Masivo salida = new GenerarCertificado24Masivo().logica(conn, entrada);
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
		*/

		System.out.println("== FIN TEST ==");
	}


}
