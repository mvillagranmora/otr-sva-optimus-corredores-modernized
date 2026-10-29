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

import cl.bcs.normativa.declaraciones.GenerarCertificado21Masivo.Rut;
import cl.bcs.normativa.declaraciones.DJ1949.ReporteCertificado71;
import cl.bcs.normativa.declaraciones.DJ1949.ReporteCertificado71.ReporteCertificado71Entrada;
import cl.bcs.normativa.declaraciones.DJ1949.ReporteCertificado71.ReporteCertificado71Salida;

@SuppressWarnings({"rawtypes", "unchecked"})
public class GenerarCertificado71Masivo implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(GenerarCertificado71Masivo.class);	
	
	private final String SQL_SELECT_LOGO_CORREDORA  = loadResourceToString("cl/bcs/normativa/declaraciones/sql/GenerarCertificadoMasivo_SelectLogoCorredora.sql");
	private final String SQL_SELECT_RAIZ_ARCHIVO    = loadResourceToString("cl/bcs/normativa/declaraciones/sql/GenerarCertificadoMasivo_SelectRaizArchivo.sql");	
	private final String SQL_SELECT_FIRMA_CORREDORA = loadResourceToString("cl/bcs/normativa/declaraciones/sql/GenerarCertificadoMasivo_SelectImagenFirma.sql");

	private final String SQL_SELECT_ID_REPORTE      = loadResourceToString("cl/bcs/normativa/declaraciones/sql/GenerarCertificadoMasivo_SelectReporte.sql");
	public  final String SQL_EXIST_TABLA_RUT  		= "select 1 AS \"Tab\" from m_temporary_tables where table_name = '#GENERARCERTIFICADO71MASIVO_RUT'";
	private final String SQL_CREATE_TABLA_RUT 		= "CREATE LOCAL TEMPORARY TABLE #GenerarCertificado71Masivo_RUT (\"Rut\" NVARCHAR(40))";													
	private final String SQL_INSERT_TABLA_RUT		= "INSERT INTO #GenerarCertificado71Masivo_RUT (\"Rut\") VALUES (:Rut)";
	
	
	public  final String SQL_DROP_TABLA_RUT  		= "DROP TABLE #GenerarCertificado71Masivo_RUT";
	private final String SQL_UPDATE_NOMBRE_SALIDA_PROGRAMA_TRIBUTARIO = "UPDATE \"BSCL.Normativa.BD::TRI.ResumenGeneracion\" SET \"NombreArchivo\"=:RutaArchivo, \"Glosa\"=:Glosa, \"TotalRegistrosGenerados\"=:TotalRegistrosGenerados WHERE \"IdProceso\"= :IdProceso";
	/*Servicio de Crystal Report*/
	private final String DJ="1949";
	private final String SQL_SELECT_GRUPO_CERTIFICA = loadResourceToString("cl/bcs/normativa/declaraciones/sql/GenerarCertificadoMasivo_SelectGrupoCertificado71.sql");
	private final String SQL_CALL_CERTIFICADO	    = loadResourceToString("cl/bcs/normativa/declaraciones/sql/GenerarCertificadoMasivo_certificado71_Call.sql");	
	private final String SQL_UPDATE_CERTIFICADO_ESTADO = loadResourceToString("cl/bcs/normativa/jobs/sql/BcsJobGenerarCertificadosTributarios_UpdateCertificadoClienteEstado.sql");
	private final Log log = new Log("NOR.GenerarCertificado71Masivo");
	
	private static final Map<String, String> esquemaCertificado = new HashMap<>();
	public String v_RutCertificado="";
	public long contadorArchivos=0;
	static {
		esquemaCertificado.put("IdCliente", "number");
		esquemaCertificado.put("LogoCorredora", "blob");
		esquemaCertificado.put("FirmaCorredora", "blob");
	}

	public static class EntradaGenerarCertificado71Masivo extends BcsPojo{
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
		
		public EntradaGenerarCertificado71Masivo() {
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
	
	public static class SalidaGenerarCertificado71Masivo extends BcsPojo {
		public Long P_OUT_ERRORESCONTADOR;
		public List<PlataformaUtils.MensajeSalida> P_OUT_MENSAJES;
		public String p_out_RutaZip;
		public String p_out_NombreZip;
		public Long p_out_contadorArchivos;

		public SalidaGenerarCertificado71Masivo() {
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

        public BigDecimal NroCertificadoF5;
        public BigDecimal FactorF7;
        public BigDecimal NroDividendoF2;
        public BigDecimal MontoF6;
        public BigDecimal MontoActualizadoF8;
        public BigDecimal ConCreditoIDPCDesed2017C6F9;
        public BigDecimal ConCreditoIDPCHasta2017C7F10;
        public BigDecimal ConDerechoIDPCVoluntarioC8F11;
        public BigDecimal SinDerechoCreditoC9F12;
        public BigDecimal MtoDivReaREXTribCumpRentasRap_C10F13;
        public BigDecimal MtoDivReaREXTribCumpOtrasRentas_C11F14;
        public BigDecimal MtoDivReaREXTribCumpExcDist_C12F15;
        public BigDecimal MtoDivReaREXTribCumpUtiISFUT_C13F16;
        public BigDecimal MtoDivReaREXTribCumpRentasISFUT_C14F17;
        public BigDecimal MtoDivReaREXRentExIGCAfecIAart11_C15F18;
        public BigDecimal RentasExcentasIGCC16F19;
        public BigDecimal MtoDivReaREXNoConstRenta_C17F20;
        public BigDecimal CIGCDesde01012017RentAfecNoRestSDD_C18F21;
        public BigDecimal CIGCDesde01012017RentAfecNoRestCDD_C19F22;
        public BigDecimal CIGCDesde01012017RentAfecNoRestSDD_C20F23;
        public BigDecimal CIGCDesde01012017RentAfecNoRestCDD_C21F24;
        public BigDecimal CIGCDesde01012017RentAfecRestSDD_C22F25;
        public BigDecimal CIGCDesde01012017RentAfecRestCDD_C23F26;
        public BigDecimal CIGCDesde01012017RentExenRestSDD_C24F27;
        public BigDecimal CIGCDesde01012017RentExenRestCDD_C25F28;
        public BigDecimal CIGCDesde01012017CredIPE_C26F29;
        public BigDecimal CIGCHasta31122016RentAfecSDD_C27F30;
        public BigDecimal CIGCHasta31122016RentAfecCDD_C28F31;
        public BigDecimal CIGCHasta31122016RentExenSDD_C29F32;
        public BigDecimal CIGCHasta31122016RentExenCDD_C30F33;
        public BigDecimal CIGCHasta31122016CredIPE_C31F34;
        public BigDecimal CIGCImptoTasaAdicional_C32F35;
        public BigDecimal DevCapArt17N7LIR_C33F36;
        public BigDecimal TasaEfectCredFUT;
        public BigDecimal TasaEfectCredFUNT;
        public String FechaDividendo;
        public String RutSocPagadora;
        public String RazonSocialSocPagadora;
        public String TipoSociedad;
        public String RutReceptor;
        public String RazonSocialEmpresa;
        public String DocumentoIdentidadEmpresa;
        public String DireccionEmpresa;
        public String NombreEntidadRecaudadora;
        public Long IdCliente;
        public String NombreCompletoCliente;
        public String DocumentoIdentidadCliente;
        public String CiudadFecha;

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
	
	public SalidaGenerarCertificado71Masivo logica(Connection conn, EntradaGenerarCertificado71Masivo entrada) throws Exception {
		logger.info("INICIO::GenerarCertificado71Masivo");
		long inicio = System.currentTimeMillis();
		log.write( "*GenerarCertificado71Masivo* INICIO");
		String Estado="Procesando";
		SQL sql = new SQL();
		SalidaGenerarCertificado71Masivo salida = new SalidaGenerarCertificado71Masivo();
		
		String logoCorredoraB64  = obtenerLogoCorredoraB64(conn, entrada.p_IdEmpresa);
		String firmaCorredoraB64 = obtenerFirmaB64(conn, entrada.p_IdEmpresa);
		log.write( "*GenerarCertificado71Masivo* obtenerRaizArchivo");
		RaizArchivo raizArchivo = obtenerRaizArchivo(conn, entrada.p_IdEmpresa);
		if (StringUtils.isNullOrBlank(raizArchivo.RutaArchivo) || StringUtils.isNullOrBlank(raizArchivo.RutaNOR)) {
			salida.P_OUT_ERRORESCONTADOR++;
			salida.P_OUT_MENSAJES.add(new PlataformaUtils.MensajeSalida(0L, PlataformaUtils.MensajeSalida.MENSAJE_ERROR, "FALTA_CONFIGURACION_RUTA_ARCHIVO", new HashMap<>(), conn, entrada.p_IdEmpresa));
			return salida;
		}
		log.write( "*GenerarCertificado71Masivo* RutaArchivo" + raizArchivo.RutaArchivo);
		// Para pruebas locales.
		//final String rutaAbsolutaBase = System.getProperty("user.home") + File.separator + "Desktop" + File.separator + "Certificado31" + File.separator;
		final String rutaAbsolutaBase = raizArchivo.RutaArchivo;
		final String rutaRelativaBase = raizArchivo.RutaNOR;
		final String nombreBaseArchivo = "CertTRI71_";
		String nombreBaseZip = "CerTRI71_";
		if (entrada.p_NombreZIP !=""){
			nombreBaseZip=entrada.p_NombreZIP;
		}
		final String timestampSistema = Fecha.toFormat(FechaDB.ObtenerFechaHoraSistemaTs(conn, entrada.p_IdEmpresa), "yyyyMMdd_HHmmss");
		
		//buscar datos 		
		log.write( "*GenerarCertificado71Masivo* obtenerListaCertificados");
		List<DatosCertificado> listaCerificados = obtenerListaCertificados (conn , entrada);
		
		if(listaCerificados.isEmpty()) {
			salida.P_OUT_MENSAJES.add(new MensajeSalida( 0L, MensajeSalida.MENSAJE_ERROR, "UI_SIN_DATOS", "No se encontraron datos consultados"));
			salida.P_OUT_ERRORESCONTADOR = Long.valueOf(salida.P_OUT_MENSAJES.size());
			return salida;
		};
		log.write( "*GenerarCertificado71Masivo* LeerNombreArchivos");
		LeerNombreArchivos(conn,entrada.p_IdEmpresa,entrada.p_IdTipoDocumento,entrada.p_VersionArchivo,DJ);
		
		log.write( "*GenerarCertificado71Masivo* obtenerTraspasoReporte");
		List<DatosCertificadoCompleto> traspasosInformacion= obtenerTraspasoReporte(listaCerificados, logoCorredoraB64,firmaCorredoraB64);
		log.write( "*GenerarCertificado71Masivo* obtenerGrupoCertificado");
		List<Rut> GrupoCertificado=obtenerGrupoCertificado(conn , entrada);
		if(GrupoCertificado.size()>0) {
			 List<ResumenGeneracion> programa = new ArrayList<ResumenGeneracion>();
				if (entrada.p_IdProceso>0){
					Estado="Iniciando PDF:< " + GrupoCertificado.size() + " >";
					programa.add(new ResumenGeneracion(entrada.p_IdProceso, "",Estado,0L));
		        	sql.batch(conn, SQL_UPDATE_NOMBRE_SALIDA_PROGRAMA_TRIBUTARIO, programa,ResumenGeneracion.class);
		        	conn.commit();
				};
		}
		
		log.write( "*GenerarCertificado71Masivo* generarArchivosInformes");
		List<String> rutasArchivosSinComprimir = generarArchivosInformes(conn,entrada.p_DataSource, rutaAbsolutaBase, nombreBaseArchivo, traspasosInformacion,entrada,GrupoCertificado);
		log.write( "*GenerarCertificado71Masivo* comprimirArchivos");
		salida.p_out_NombreZip = comprimirArchivos(timestampSistema, rutaAbsolutaBase, rutaRelativaBase, nombreBaseZip, rutasArchivosSinComprimir);
		salida.p_out_RutaZip= rutaRelativaBase + salida.p_out_NombreZip;
		salida.p_out_contadorArchivos=contadorArchivos;
		//salida.p_out_RutaZip="nor/Certificado71-10005725-5.pdf";
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::GenerarCertificado71Masivo {}", fin - inicio);
		log.write( "*GenerarCertificado71Masivo* FIN");
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


	private List<String> generarArchivosInformes(Connection conn,DataSource dataSource, String rutaAbsoluta,String nombreBaseArchivo, List<DatosCertificadoCompleto> traspasosInformacion,EntradaGenerarCertificado71Masivo entrada,List<Rut> rutLista) throws Exception {
		logger.debug("INICIO::GenerarCertificado71Masivo.generarArchivosInformes");
        String updTramo = entrada.p_NombreZIP.replace("TRI_CERT71_PT", "P");
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
					certEstadoRut.add(new CertificadoEstadoRut("CERT71", entrada.p_VersionArchivo,"GEN",rut.Rut));
					sql.batch(conn, SQL_UPDATE_CERTIFICADO_ESTADO, certEstadoRut,CertificadoEstadoRut.class); //ESTADOS
					conn.commit();
					log.write( "*BcsJobGenerarCertificadosTributarios* SQL_UPDATE_CERTIFICADO_ESTADO");
					 //FIN TRAMOS
					
					if (contresumen==50){
						String Estado=updTramo + " < " + rutLista.size() + " > ";
						programa.add(new ResumenGeneracion(entrada.p_IdProceso, "",Estado,contadorArchivos));
			        	sql.batch(conn, SQL_UPDATE_NOMBRE_SALIDA_PROGRAMA_TRIBUTARIO, programa,ResumenGeneracion.class);
			        	conn.commit();
			        	contresumen=0;
					};
		}
		
		if (rutLista.size()>0){
			String Estado=updTramo + " < Comprimiendo >";
			programa.add(new ResumenGeneracion(entrada.p_IdProceso, "",Estado,contadorArchivos));
        	sql.batch(conn, SQL_UPDATE_NOMBRE_SALIDA_PROGRAMA_TRIBUTARIO, programa,ResumenGeneracion.class);
        	conn.commit();
        	contresumen=0;
		};
		
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::GenerarCertificado71Masivo.obtenerTraspasoReporte {}", fin - inicio);
		return rutasArchivos;
	}
	
	
	private String generarReportePDF(Connection conn,ReportDocument report, String rutaBase, List<DatosCertificadoCompleto> listaTraspasoFinal,EntradaGenerarCertificado71Masivo entrada) throws Exception {
		logger.trace("INICIO::GenerarCertificado71Masivo.generarReportePDF");
		long inicio = System.currentTimeMillis();

		String rutaReporte = rutaBase + ".pdf";
		String json = Utils.getGsonBuilder().toJson(listaTraspasoFinal);
		JsonArray informacionCertificado = new JsonParser().parse(json).getAsJsonArray();
		String idTemplate =obtenerIdReporte(conn,entrada);// "173603C07D0185DB1700D20086E4297E";
		log.write( "*GenerarCertificado71Masivo* idTemplate" + idTemplate);
		if (report.readDocument(idTemplate)) {
			try {
				report.setDocumentResultSet(esquemaCertificado, informacionCertificado);
			} catch (Exception e) {
				logger.error(e.getMessage());
			}

			try (InputStream is = report.exportDocument("pdf")) {
				File reportePDF = new File(rutaReporte);
				FileUtils.copyInputStreamToFile(is, reportePDF);
				log.write( "*GenerarCertificado71Masivo* generarReportePDF" + rutaReporte);
			} catch (Exception e) {
				logger.error(e.getMessage());
			}
		} else {
			logger.error("Error al obtener el template {}, generacion de PDF omitida", idTemplate);
		}

		long fin = System.currentTimeMillis();
		logger.trace("FIN::GenerarCertificado71Masivo.generarReportePDF {}", fin - inicio);

		return rutaReporte;
	}


	private List<DatosCertificadoCompleto> obtenerTraspasoReporte(List<DatosCertificado> listaCerificados, String logoCorredoraB64, String firmaCorredoraB64) {
		logger.debug("INICIO::GenerarCertificado71Masivo.obtenerTraspasoReporte");
		long inicio = System.currentTimeMillis();
		Integer contadorCertificado=1;
		
		List<DatosCertificadoCompleto>  listaTraspasosReporte = new ArrayList<DatosCertificadoCompleto>();
		
		for (DatosCertificado tra : listaCerificados) {
			DatosCertificadoCompleto traspasoImagen = new DatosCertificadoCompleto();


            traspasoImagen.Folio=tra.Folio;
            traspasoImagen.IdEmpresa=tra.IdEmpresa;
            traspasoImagen.Esquema=tra.Esquema;
            traspasoImagen.Version=tra.Version;
            traspasoImagen.Anio=tra.Anio;
            traspasoImagen.Periodo=tra.Periodo;
            
            traspasoImagen.NroCertificadoF5=tra.NroCertificadoF5;
            traspasoImagen.FactorF7=tra.FactorF7;
            traspasoImagen.NroDividendoF2=tra.NroDividendoF2;
            traspasoImagen.MontoF6=tra.MontoF6;
            traspasoImagen.MontoActualizadoF8=tra.MontoActualizadoF8;
            traspasoImagen.ConCreditoIDPCDesed2017C6F9=tra.ConCreditoIDPCDesed2017C6F9;
            traspasoImagen.ConCreditoIDPCHasta2017C7F10=tra.ConCreditoIDPCHasta2017C7F10;
            traspasoImagen.ConDerechoIDPCVoluntarioC8F11=tra.ConDerechoIDPCVoluntarioC8F11;
            traspasoImagen.SinDerechoCreditoC9F12=tra.SinDerechoCreditoC9F12;
            traspasoImagen.MtoDivReaREXTribCumpRentasRap_C10F13=tra.MtoDivReaREXTribCumpRentasRap_C10F13;
            traspasoImagen.MtoDivReaREXTribCumpOtrasRentas_C11F14=tra.MtoDivReaREXTribCumpOtrasRentas_C11F14;
            traspasoImagen.MtoDivReaREXTribCumpExcDist_C12F15=tra.MtoDivReaREXTribCumpExcDist_C12F15;
            traspasoImagen.MtoDivReaREXTribCumpUtiISFUT_C13F16=tra.MtoDivReaREXTribCumpUtiISFUT_C13F16;
            traspasoImagen.MtoDivReaREXTribCumpRentasISFUT_C14F17=tra.MtoDivReaREXTribCumpRentasISFUT_C14F17;
            traspasoImagen.MtoDivReaREXRentExIGCAfecIAart11_C15F18=tra.MtoDivReaREXRentExIGCAfecIAart11_C15F18;
            traspasoImagen.RentasExcentasIGCC16F19=tra.RentasExcentasIGCC16F19;
            traspasoImagen.MtoDivReaREXNoConstRenta_C17F20=tra.MtoDivReaREXNoConstRenta_C17F20;
            traspasoImagen.CIGCDesde01012017RentAfecNoRestSDD_C18F21=tra.CIGCDesde01012017RentAfecNoRestSDD_C18F21;
            traspasoImagen.CIGCDesde01012017RentAfecNoRestCDD_C19F22=tra.CIGCDesde01012017RentAfecNoRestCDD_C19F22;
            traspasoImagen.CIGCDesde01012017RentAfecNoRestSDD_C20F23=tra.CIGCDesde01012017RentAfecNoRestSDD_C20F23;
            traspasoImagen.CIGCDesde01012017RentAfecNoRestCDD_C21F24=tra.CIGCDesde01012017RentAfecNoRestCDD_C21F24;
            traspasoImagen.CIGCDesde01012017RentAfecRestSDD_C22F25=tra.CIGCDesde01012017RentAfecRestSDD_C22F25;
            traspasoImagen.CIGCDesde01012017RentAfecRestCDD_C23F26=tra.CIGCDesde01012017RentAfecRestCDD_C23F26;
            traspasoImagen.CIGCDesde01012017RentExenRestSDD_C24F27=tra.CIGCDesde01012017RentExenRestSDD_C24F27;
            traspasoImagen.CIGCDesde01012017RentExenRestCDD_C25F28=tra.CIGCDesde01012017RentExenRestCDD_C25F28;
            traspasoImagen.CIGCDesde01012017CredIPE_C26F29=tra.CIGCDesde01012017CredIPE_C26F29;
            traspasoImagen.CIGCHasta31122016RentAfecSDD_C27F30=tra.CIGCHasta31122016RentAfecSDD_C27F30;
            traspasoImagen.CIGCHasta31122016RentAfecCDD_C28F31=tra.CIGCHasta31122016RentAfecCDD_C28F31;
            traspasoImagen.CIGCHasta31122016RentExenSDD_C29F32=tra.CIGCHasta31122016RentExenSDD_C29F32;
            traspasoImagen.CIGCHasta31122016RentExenCDD_C30F33=tra.CIGCHasta31122016RentExenCDD_C30F33;
            traspasoImagen.CIGCHasta31122016CredIPE_C31F34=tra.CIGCHasta31122016CredIPE_C31F34;
            traspasoImagen.CIGCImptoTasaAdicional_C32F35=tra.CIGCImptoTasaAdicional_C32F35;
            traspasoImagen.DevCapArt17N7LIR_C33F36=tra.DevCapArt17N7LIR_C33F36;
            traspasoImagen.TasaEfectCredFUT=tra.TasaEfectCredFUT;
            traspasoImagen.TasaEfectCredFUNT=tra.TasaEfectCredFUNT;
            traspasoImagen.FechaDividendo=tra.FechaDividendo;
            traspasoImagen.RutSocPagadora=tra.RutSocPagadora;
            traspasoImagen.RazonSocialSocPagadora=tra.RazonSocialSocPagadora;
            traspasoImagen.TipoSociedad=tra.TipoSociedad;
            traspasoImagen.RutReceptor=tra.RutReceptor;
            traspasoImagen.RazonSocialEmpresa=tra.RazonSocialEmpresa;
            traspasoImagen.DocumentoIdentidadEmpresa=tra.DocumentoIdentidadEmpresa;
            traspasoImagen.DireccionEmpresa=tra.DireccionEmpresa;
            traspasoImagen.NombreEntidadRecaudadora=tra.NombreEntidadRecaudadora;
            traspasoImagen.IdCliente=tra.IdCliente;
            traspasoImagen.NombreCompletoCliente=tra.NombreCompletoCliente;
            traspasoImagen.DocumentoIdentidadCliente=tra.DocumentoIdentidadCliente;
            traspasoImagen.CiudadFecha=tra.CiudadFecha;

            traspasoImagen.RazonSocialCorredora=tra.RazonSocialCorredora;
            traspasoImagen.DireccionCorredora=tra.DireccionCorredora;
            traspasoImagen.DocumentoIdentidadCorredora=tra.DocumentoIdentidadCorredora;
            traspasoImagen.rut_representante=tra.rut_representante;
            traspasoImagen.nombre_representante=tra.nombre_representante;
            traspasoImagen.RutCertificado=tra.RutCertificado;
			
			traspasoImagen.LogoCorredora    =logoCorredoraB64;
			traspasoImagen.FirmaCorredora   =firmaCorredoraB64;
			
			listaTraspasosReporte.add(traspasoImagen);
			contadorCertificado++;			
		}		
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::GenerarCertificado71Masivo.obtenerTraspasoReporte {}", fin - inicio);

		return listaTraspasosReporte;
	}


	private List<DatosCertificado> obtenerListaCertificados(Connection conn,EntradaGenerarCertificado71Masivo entrada)  {
		logger.debug("INICIO::GenerarCertificado71Masivo.obtenerListaCertificados");
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
					log.write( "*GenerarCertificado71Masivo* { SE EJECUTA EL DROP INICIAL}");
				} catch (Exception er) { 
					er.printStackTrace();
					log.write("*GenerarCertificado71Masivo* {DROP INICIAL, NO LOGRADO}");
				}
				
			}else{
				log.write("*GenerarCertificado71Masivo* { NO SE EJECUTA EL DROP INICIAL}");
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

			// HashMap<String, Object> resultadoSP = sql.call(conn, SQL_CALL_CERTIFICADO, paramIN, paramOUT);
			// listaCertificados = resultadoSP.get("p_Salida") == null ? new ArrayList<>() : (List<DatosCertificado>) resultadoSP.get("p_Salida");
			
			// Inicia reporte Java
			
			ReporteCertificado71 Certificado71 = new ReporteCertificado71();
			ReporteCertificado71Entrada EntradaCertificado71 = new ReporteCertificado71Entrada();
			
			List<ReporteCertificado71Salida> SalidaCertificado71 = new ArrayList<ReporteCertificado71Salida>();
			
			List<ResumenGeneracion> prog = new ArrayList<ResumenGeneracion>();
			String Est="< Buscando... >";
			prog.add(new ResumenGeneracion(entrada.p_IdProceso, "v 2.0",Est,0L));
        	sql.batch(conn, SQL_UPDATE_NOMBRE_SALIDA_PROGRAMA_TRIBUTARIO, prog,ResumenGeneracion.class);
    
        	conn.commit();
	
        	EntradaCertificado71.p_IdEmpresa	= entrada.p_IdEmpresa;
			EntradaCertificado71.p_IdModulo = entrada.p_IdModulo;
			EntradaCertificado71.p_IdUsuario	= entrada.p_IdUsuario;
			EntradaCertificado71.p_Version = entrada.p_VersionArchivo;
			SalidaCertificado71 = Certificado71.logica(conn, EntradaCertificado71);

			if (SalidaCertificado71.size() > 0) {
				List<ResumenGeneracion> prog2 = new ArrayList<ResumenGeneracion>();
				String Est2 = "< Encontrados... >";
				prog2.add(new ResumenGeneracion(entrada.p_IdProceso, "", Est2, 0L));
	        	sql.batch(conn, SQL_UPDATE_NOMBRE_SALIDA_PROGRAMA_TRIBUTARIO, prog2, ResumenGeneracion.class);
	        	
	        	conn.commit();
				
	        	listaCertificados = AgregarCertificado(SalidaCertificado71);
				log.write("*GenerarCertificado71Masivo* { arma lista desde JAVA}" + listaCertificados.toString());
			}
			
			// Fin reporte JAVA	
						
						
		}catch (Exception e) {
			e.printStackTrace();
	
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::GenerarCertificado71Masivo.obtenerListaCertificados {}", fin - inicio);
		return listaCertificados;
	}

    private List<DatosCertificado> AgregarCertificado(List<ReporteCertificado71Salida> SalidaCertificado71)  {
		List<DatosCertificado> o = new ArrayList<DatosCertificado>();
		
		for (ReporteCertificado71Salida  datos : SalidaCertificado71) {
			DatosCertificado out = new DatosCertificado();
			
			out.Folio=datos.Folio;
            out.IdEmpresa=datos.IdEmpresa;
            out.Esquema=datos.Esquema;
            out.Version=datos.Version;
            out.Anio=datos.Anio;
            out.Periodo=datos.Periodo;

            out.NroCertificadoF5=datos.NroCertificadoF5;
            out.FactorF7=datos.FactorF7;
            out.NroDividendoF2=datos.NroDividendoF2;
            out.MontoF6=datos.MontoF6;
            out.MontoActualizadoF8=datos.MontoActualizadoF8;
            out.ConCreditoIDPCDesed2017C6F9=datos.ConCreditoIDPCDesed2017C6F9;
            out.ConCreditoIDPCHasta2017C7F10=datos.ConCreditoIDPCHasta2017C7F10;
            out.ConDerechoIDPCVoluntarioC8F11=datos.ConDerechoIDPCVoluntarioC8F11;
            out.SinDerechoCreditoC9F12=datos.SinDerechoCreditoC9F12;
            out.MtoDivReaREXTribCumpRentasRap_C10F13=datos.MtoDivReaREXTribCumpRentasRap_C10F13;
            out.MtoDivReaREXTribCumpOtrasRentas_C11F14=datos.MtoDivReaREXTribCumpOtrasRentas_C11F14;
            out.MtoDivReaREXTribCumpExcDist_C12F15=datos.MtoDivReaREXTribCumpExcDist_C12F15;
            out.MtoDivReaREXTribCumpUtiISFUT_C13F16=datos.MtoDivReaREXTribCumpUtiISFUT_C13F16;
            out.MtoDivReaREXTribCumpRentasISFUT_C14F17=datos.MtoDivReaREXTribCumpRentasISFUT_C14F17;
            out.MtoDivReaREXRentExIGCAfecIAart11_C15F18=datos.MtoDivReaREXRentExIGCAfecIAart11_C15F18;
            out.RentasExcentasIGCC16F19=datos.RentasExcentasIGCC16F19;
            out.MtoDivReaREXNoConstRenta_C17F20=datos.MtoDivReaREXNoConstRenta_C17F20;
            out.CIGCDesde01012017RentAfecNoRestSDD_C18F21=datos.CIGCDesde01012017RentAfecNoRestSDD_C18F21;
            out.CIGCDesde01012017RentAfecNoRestCDD_C19F22=datos.CIGCDesde01012017RentAfecNoRestCDD_C19F22;
            out.CIGCDesde01012017RentAfecNoRestSDD_C20F23=datos.CIGCDesde01012017RentAfecNoRestSDD_C20F23;
            out.CIGCDesde01012017RentAfecNoRestCDD_C21F24=datos.CIGCDesde01012017RentAfecNoRestCDD_C21F24;
            out.CIGCDesde01012017RentAfecRestSDD_C22F25=datos.CIGCDesde01012017RentAfecRestSDD_C22F25;
            out.CIGCDesde01012017RentAfecRestCDD_C23F26=datos.CIGCDesde01012017RentAfecRestCDD_C23F26;
            out.CIGCDesde01012017RentExenRestSDD_C24F27=datos.CIGCDesde01012017RentExenRestSDD_C24F27;
            out.CIGCDesde01012017RentExenRestCDD_C25F28=datos.CIGCDesde01012017RentExenRestCDD_C25F28;
            out.CIGCDesde01012017CredIPE_C26F29=datos.CIGCDesde01012017CredIPE_C26F29;
            out.CIGCHasta31122016RentAfecSDD_C27F30=datos.CIGCHasta31122016RentAfecSDD_C27F30;
            out.CIGCHasta31122016RentAfecCDD_C28F31=datos.CIGCHasta31122016RentAfecCDD_C28F31;
            out.CIGCHasta31122016RentExenSDD_C29F32=datos.CIGCHasta31122016RentExenSDD_C29F32;
            out.CIGCHasta31122016RentExenCDD_C30F33=datos.CIGCHasta31122016RentExenCDD_C30F33;
            out.CIGCHasta31122016CredIPE_C31F34=datos.CIGCHasta31122016CredIPE_C31F34;
            out.CIGCImptoTasaAdicional_C32F35=datos.CIGCImptoTasaAdicional_C32F35;
            out.DevCapArt17N7LIR_C33F36=datos.DevCapArt17N7LIR_C33F36;
            out.TasaEfectCredFUT=datos.TasaEfectCredFUT;
            out.TasaEfectCredFUNT=datos.TasaEfectCredFUNT;
            out.FechaDividendo=datos.FechaDividendo;
            out.RutSocPagadora=datos.RutSocPagadora;
            out.RazonSocialSocPagadora=datos.RazonSocialSocPagadora;
            out.TipoSociedad=datos.TipoSociedad;
            out.RutReceptor=datos.RutReceptor;
            out.RazonSocialEmpresa=datos.RazonSocialEmpresa;
            out.DocumentoIdentidadEmpresa=datos.DocumentoIdentidadEmpresa;
            out.DireccionEmpresa=datos.DireccionEmpresa;
            out.NombreEntidadRecaudadora=datos.NombreEntidadRecaudadora;
            out.IdCliente=datos.IdCliente;
            out.NombreCompletoCliente=datos.NombreCompletoCliente;
            out.DocumentoIdentidadCliente=datos.DocumentoIdentidadCliente;
            out.CiudadFecha=datos.CiudadFecha;

            out.RazonSocialCorredora=datos.RazonSocialCorredora;
            out.DireccionCorredora=datos.DireccionCorredora;
            out.DocumentoIdentidadCorredora=datos.DocumentoIdentidadCorredora;
            out.rut_representante=datos.rut_representante;
            out.nombre_representante=datos.nombre_representante;
            out.RutCertificado=datos.DocumentoIdentidadCliente;
			
			o.add(out);
		}
		
		return o;
	}
	
	private List<Rut> obtenerGrupoCertificado(Connection conn,EntradaGenerarCertificado71Masivo entrada)  {
		logger.debug("INICIO::GenerarCertificado71Masivo.obtenerGrupoCertificado");
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
				
				logger.debug("INICIO::GenerarCertificado71Masivo.obtenerListaCertificados { SE EJECUTA EL DROP FINAL}");
			} catch (Exception er) { 
				er.printStackTrace();
				logger.debug("INICIO::GenerarCertificado71Masivo.obtenerListaCertificados { NO SE EJECUTA EL DROP FINAL}");
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::GenerarCertificado71Masivo.obtenerListaCertificados {}", fin - inicio);
		return rutLista;
	}

	public String obtenerLogoCorredoraB64(Connection conn, Long idEmpresa) throws SQLException {
		logger.debug("INICIO::GenerarCertificado71Masivo.obtenerLogoCorredora");
		long inicio = System.currentTimeMillis();

		HashMap<String, Object> params = new HashMap<>(2);
		params.put("IdEmpresa", idEmpresa);
		Imagen logo = new SQL().query(conn, SQL_SELECT_LOGO_CORREDORA, params, Imagen.class).stream().findFirst().orElse(new Imagen());
		String contenido = Base64.encodeBase64String(logo.Contenido);

		long fin = System.currentTimeMillis();
		logger.debug("FIN::GenerarCertificado71Masivo.obtenerLogoCorredora {}", fin - inicio);

		return contenido;
	}
	
	public String obtenerIdReporte(Connection conn, EntradaGenerarCertificado71Masivo entrada) throws SQLException {
		logger.debug("INICIO::GenerarCertificado71Masivo.obtenerLogoCorredora");
		long inicio = System.currentTimeMillis();
		HashMap<String, Object> params = new HashMap<>(2);
		params.put("IdEmpresa", entrada.p_IdEmpresa);
		params.put("IdTipoDocumento", entrada.p_IdTipoDocumento);
		params.put("VersionArchivo", entrada.p_VersionArchivo);
		List<Template> idLista = new SQL().query(conn, SQL_SELECT_ID_REPORTE, params, Template.class);
		//List<Template> idLista = new SQL().query(conn, SQL_SELECT_ID_REPORTE, new HashMap<>(2), Template.class);
		String IdTemplate =idLista.get(0).Id;

		long fin = System.currentTimeMillis();
		logger.debug("FIN::GenerarCertificado71Masivo.obtenerLogoCorredora {}", fin - inicio);

		return IdTemplate;
	}

	public String obtenerFirmaB64(Connection conn, Long idEmpresa) throws SQLException {
		logger.debug("INICIO::GenerarCertificado71Masivo.obtenerLogoCorredora");
		long inicio = System.currentTimeMillis();

		Imagen firma = new SQL().query(conn, SQL_SELECT_FIRMA_CORREDORA,new HashMap<String, Object>(), Imagen.class).stream().findFirst().orElse(new Imagen());
		String contenido = Base64.encodeBase64String(firma.Contenido);

		long fin = System.currentTimeMillis();
		logger.debug("FIN::GenerarCertificado71Masivo.obtenerLogoCorredora {}", fin - inicio);

		return contenido;
	}
	
	public RaizArchivo obtenerRaizArchivo(Connection conn, Long idEmpresa) throws SQLException {
		logger.debug("INICIO::GenerarCertificado71Masivo.obtenerRaizArchivo");
		long inicio = System.currentTimeMillis();

		HashMap<String, Object> params = new HashMap<>(2);
		params.put("IdEmpresa", idEmpresa);

		RaizArchivo raiz = new SQL().query(conn, SQL_SELECT_RAIZ_ARCHIVO, params, RaizArchivo.class).stream().findFirst().orElse(new RaizArchivo());

		long fin = System.currentTimeMillis();
		logger.debug("INICIO::GenerarCertificado71Masivo.obtenerRaizArchivo {}", fin - inicio);

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
			EntradaGenerarCertificado71Masivo entrada = new EntradaGenerarCertificado71Masivo();
			Certificado Certificado = new Certificado();
			entrada.p_IdEmpresa = 1L;
			entrada.p_IdUsuario = 21L;
			entrada.p_IdModulo = "NOR";
			entrada.p_IdProceso=559065L;
	
			Rut Rut = new Rut();
//			Rut.Rut = "1316026-0";

			entrada.p_listaRut.add(Rut);
			entrada.p_VersionArchivo="2023";
			entrada.p_IdTipoDocumento="CERT71";
			
			entrada.p_DataSource = PlataformaUtils.getDESADatasource();

			SalidaGenerarCertificado71Masivo salida = new GenerarCertificado71Masivo().logica(conn, entrada);
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
			EntradaGenerarCertificado71Masivo entrada = new EntradaGenerarCertificado71Masivo();
			Certificado Certificado = new Certificado();
			entrada.p_IdEmpresa = 1L;
			entrada.p_IdUsuario = 21L;
			entrada.p_IdModulo = "NOR";
			entrada.p_IdProceso=559065L;
	
			Rut Rut = new Rut();
			//Rut.Rut = "1-9";

			entrada.p_listaRut.add(Rut);
			entrada.p_VersionArchivo="2021";
			entrada.p_IdTipoDocumento="CERT71";
			entrada.p_DataSource = PlataformaUtils.getMASTER1Datasource();

			SalidaGenerarCertificado71Masivo salida = new GenerarCertificado71Masivo().logica(conn, entrada);
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
