package cl.bcs.facturacion;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.sql.Connection;
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

@SuppressWarnings({"rawtypes", "unchecked"})
public class GenerarInformeIGK implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(GenerarInformeIGK.class);	
	
	private final String SQL_SELECT_LOGO_CORREDORA  = loadResourceToString("cl/bcs/facturacion/sql/GenerarInformeIGK_SelectLogoCorredora.sql");
	private final String SQL_SELECT_RAIZ_ARCHIVO    = loadResourceToString("cl/bcs/facturacion/sql/GenerarInformeIGK_SelectRaizArchivo.sql");	
	private final String SQL_SELECT_FIRMA_CORREDORA = loadResourceToString("cl/bcs/facturacion/sql/GenerarInformeIGK_SelectImagenFirma.sql");

	private final String SQL_SELECT_ID_REPORTE      = loadResourceToString("cl/bcs/facturacion/sql/GenerarInformeIGK_SelectReporte.sql");
	public  final String SQL_EXIST_TABLA_RUT  		= "select 1 AS \"Tab\" from m_temporary_tables where table_name = '#GENERARINFORMEIGK_DATOSDEFILTRO'";
	private final String SQL_CREATE_TABLA_RUT 		= "CREATE LOCAL TEMPORARY TABLE #GENERARINFORMEIGK_DATOSDEFILTRO (\"IdCliente\" BIGINT,\"Folio\" BIGINT)";													
	private final String SQL_INSERT_TABLA_RUT		= "INSERT INTO #GENERARINFORMEIGK_DATOSDEFILTRO (\"IdCliente\",\"Folio\") VALUES (:IdCliente,:Folio)";
	
	
	public  final String SQL_DROP_TABLA_RUT  		= "DROP TABLE #GENERARINFORMEIGK_DATOSDEFILTRO";
	//private final String SQL_UPDATE_NOMBRE_SALIDA_PROGRAMA_TRIBUTARIO = "UPDATE \"BSCL.Normativa.BD::TRI.ResumenGeneracion\" SET \"NombreArchivo\"=:RutaArchivo, \"Glosa\"=:Glosa, \"TotalRegistrosGenerados\"=:TotalRegistrosGenerados WHERE \"IdProceso\"= :IdProceso";
	/*Servicio de Crystal Report*/
	
	private final String SQL_SELECT_GRUPO_CERTIFICA = loadResourceToString("cl/bcs/facturacion/sql/GenerarInformeIGK_SelectGrupoClientes.sql");
	private final String SQL_CALL_CERTIFICADO	    = loadResourceToString("cl/bcs/facturacion/sql/GenerarInformeIGK_InformeIGK_Call.sql");	

	private final Log log = new Log("NOR.GenerarInformeIGK");
	
	private static final Map<String, String> esquemaCertificado = new HashMap<>();
	public String v_RutCertificado="";
	public long contadorArchivos=0;
	static {
		esquemaCertificado.put("IdCliente", "number");
		esquemaCertificado.put("LogoCorredora", "blob");
		esquemaCertificado.put("FirmaCorredora", "blob");
	}

	public static class EntradaGenerarInformeIGK extends BcsPojo{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;	

		public Long   p_IdProceso;
		
		public String p_FechaConsulta;
		
		public List<Datos> p_Datos;
		
		public transient DataSource p_DataSource;
		
		public EntradaGenerarInformeIGK() {
			
			this.p_Datos = new ArrayList<Datos>();
			this.p_IdProceso=0L;
		}
		
	} 
	
	public static class SalidaGenerarInformeIGK extends BcsPojo {
		public Long P_OUT_ERRORESCONTADOR;
		public List<PlataformaUtils.MensajeSalida> P_OUT_MENSAJES;
		public String p_out_RutaZip;
		public String p_out_NombreZip;
		public Long p_out_contadorArchivos;

		public SalidaGenerarInformeIGK() {
			P_OUT_ERRORESCONTADOR = 0L;
			P_OUT_MENSAJES = new ArrayList<>();
		}
	}
	
	public static class Datos extends BcsPojo{
		
		public Long IdCliente;
		public Long Folio;		
		public String Rut;
		public Datos () {
			this.Rut = "";			
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
		public String RutaFAC;
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
		
		
		
		public String  FechaComprobante;
		public Long Folio;
		public String Instrumento;
		public BigDecimal Cantidad;
		public BigDecimal Precio;
		public BigDecimal Monto;
		public String IdMoneda;
		public BigDecimal MontoRetencionGananciaCap;
		public BigDecimal MontoRetencionGananciaCapCLP;
		public java.sql.Timestamp  FechaIngresoRegistro;
		public Long NroLinea;
		public Long IdCliente;
		public String NombreCompletoCliente;
		public String DocumentoIdentidadCliente;	
		public String DireccionCliente;	
		public String ComunaCliente;
		public String CiudadCliente;
		public String FechainformeDesde;
		public String FechainformeHasta;
		public String PorcentajeRetencion;
	
		public String RazonSocialCorredora;
		public String DireccionCorredora;
		public String RutCorredora;
		public String GiroCorredor;
		public String rut_representante;
		public String nombre_representante;
		
	
		
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
	
	public SalidaGenerarInformeIGK logica(Connection conn, EntradaGenerarInformeIGK entrada) throws Exception {
		logger.info("INICIO::GenerarInformeIGK");
		long inicio = System.currentTimeMillis();
		log.write( "*GenerarInformeIGK* INICIO");
		
		
		SalidaGenerarInformeIGK salida = new SalidaGenerarInformeIGK();
		
		String logoCorredoraB64  = obtenerLogoCorredoraB64(conn, entrada.p_IdEmpresa);
		String firmaCorredoraB64 = obtenerFirmaB64(conn, entrada.p_IdEmpresa);
		log.write( "*GenerarInformeIGK* obtenerRaizArchivo");
		RaizArchivo raizArchivo = obtenerRaizArchivo(conn, entrada.p_IdEmpresa);
		if (StringUtils.isNullOrBlank(raizArchivo.RutaArchivo) || StringUtils.isNullOrBlank(raizArchivo.RutaFAC)) {
			salida.P_OUT_ERRORESCONTADOR++;
			salida.P_OUT_MENSAJES.add(new PlataformaUtils.MensajeSalida(0L, PlataformaUtils.MensajeSalida.MENSAJE_ERROR, "FALTA_CONFIGURACION_RUTA_ARCHIVO", new HashMap<>(), conn, entrada.p_IdEmpresa));
			return salida;
		}
		log.write( "*GenerarInformeIGK* RutaArchivo" + raizArchivo.RutaArchivo);
		// Para pruebas locales.
		//final String rutaAbsolutaBase = System.getProperty("user.home") + File.separator + "Desktop" + File.separator + "Certificado31" + File.separator;
		final String rutaAbsolutaBase = raizArchivo.RutaArchivo;
		final String rutaRelativaBase = raizArchivo.RutaFAC;
		final String nombreBaseArchivo = "InformeIGK_";
		final String nombreBaseZip = "InformeIGK_";
		final String timestampSistema = Fecha.toFormat(FechaDB.ObtenerFechaHoraSistemaTs(conn, entrada.p_IdEmpresa), "yyyyMMdd_HHmmss");
		
		//buscar datos 		
		log.write( "*GenerarInformeIGK* obtenerListaCertificados");
		List<DatosCertificado> listaCerificados = obtenerListaCertificados (conn , entrada);
		
		if(listaCerificados.isEmpty()) {
			
			try {
				HashMap<String, Object> params = new HashMap<>(2);
				List<ExisteTabRut> TabRut =new ArrayList<ExisteTabRut>();
				TabRut = new SQL().query(conn, SQL_EXIST_TABLA_RUT, params, ExisteTabRut.class);
				if(TabRut.size()>0) {
					try {				
						SQL.exec(conn, SQL_DROP_TABLA_RUT);				
						log.write( "*GenerarInformeIGK* { SE EJECUTA EL DROP FINAL SIN DATOS}");
					} catch (Exception er) { 
						er.printStackTrace();
						log.write("*GenerarInformeIGK* {DROP FINAL, NO LOGRADO}");
					}
					
				}else{
					log.write("*GenerarInformeIGK* { NO SE EJECUTA EL DROP FINAL SIN DATOS}");
				}
							
			}catch (Exception e) {
				e.printStackTrace();
		
			}
			
			
			
			
			
			salida.P_OUT_MENSAJES.add(new MensajeSalida( 0L, MensajeSalida.MENSAJE_ERROR, "UI_SIN_DATOS", "No se encontraron datos consultados"));
			salida.P_OUT_ERRORESCONTADOR = Long.valueOf(salida.P_OUT_MENSAJES.size());
			return salida;
		};
		log.write( "*GenerarInformeIGK* LeerNombreArchivos");
		LeerNombreArchivos(conn,entrada.p_IdEmpresa,"NOMBRESALIDA",entrada.p_FechaConsulta);
		
		log.write( "*GenerarInformeIGK* obtenerInformacionReporte");
		List<DatosCertificadoCompleto> traspasosInformacion= obtenerInformacionReporte(listaCerificados, logoCorredoraB64,firmaCorredoraB64);
		//log.write( "*GenerarInformeIGK* obtenerGrupoCertificado");
		List<Datos> GrupoCertificado=obtenerGrupoCertificado(conn , entrada);
		
		/*
		if(GrupoCertificado.size()>0) {
			 List<ResumenGeneracion> programa = new ArrayList<ResumenGeneracion>();
				if (entrada.p_IdProceso>0){
					Estado="< " + GrupoCertificado.size() + " >";
					programa.add(new ResumenGeneracion(entrada.p_IdProceso, "",Estado,0L));
		        	sql.batch(conn, SQL_UPDATE_NOMBRE_SALIDA_PROGRAMA_TRIBUTARIO, programa,ResumenGeneracion.class);
		        	conn.commit();
				};
		}
		*/
		log.write( "*GenerarInformeIGK* generarArchivosInformes");
		List<String> rutasArchivosSinComprimir = generarArchivosInformes(conn,entrada.p_DataSource, rutaAbsolutaBase, nombreBaseArchivo, traspasosInformacion,entrada,GrupoCertificado);
		log.write( "*GenerarInformeIGK* comprimirArchivos");
		salida.p_out_NombreZip = comprimirArchivos(timestampSistema, rutaAbsolutaBase, rutaRelativaBase, nombreBaseZip, rutasArchivosSinComprimir);
		salida.p_out_RutaZip= rutaRelativaBase + salida.p_out_NombreZip;
		salida.p_out_contadorArchivos=contadorArchivos;
		//salida.p_out_RutaZip="nor/Certificado21-10005725-5.pdf";
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::GenerarInformeIGK {}", fin - inicio);
		log.write( "*GenerarInformeIGK* FIN");
		return salida;
	}
	
	


	private String comprimirArchivos(String timestampSistema, String rutaAbsolutaBase, String rutaRelativaBase,	String nombreBaseZip, List<String> rutasArchivos) throws Exception {
		logger.debug("FIN::GenerarInformeRentabilidadFFMMExtranjeroMasivo.comprimirArchivos");
		long inicio = System.currentTimeMillis();

		final String nombreZip = nombreBaseZip + timestampSistema + ".zip";
		final String rutaAbsolutaZip = rutaAbsolutaBase + nombreZip;
		
		
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


	private List<String> generarArchivosInformes(Connection conn,DataSource dataSource, String rutaAbsoluta,String nombreBaseArchivo, List<DatosCertificadoCompleto> traspasosInformacion,EntradaGenerarInformeIGK entrada,List<Datos> rutLista) throws Exception {
		logger.debug("INICIO::GenerarInformeIGK.generarArchivosInformes");
		long inicio = System.currentTimeMillis();
	
		final List<String> rutasArchivos = new ArrayList<>();
		final ReportDocument report = new ReportDocument(Utils.getSchema(), dataSource);		
		
		String param1aux,param2aux,param3aux,param4aux,param5aux="";
		
		//List<ResumenGeneracion> programa = new ArrayList<ResumenGeneracion>();
		for (Datos rut: rutLista) {
			contadorArchivos++;
			
			List<DatosCertificadoCompleto> listaTraspasoFinal = new ArrayList<>();
					for (DatosCertificadoCompleto mov: traspasosInformacion) {
				
						if (mov.DocumentoIdentidadCliente.equals(rut.Rut))
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
					/*
					if (contresumen==100){
						String Estado="< " + rutLista.size() + " >";
						programa.add(new ResumenGeneracion(entrada.p_IdProceso, "",Estado,contadorArchivos));
			        	sql.batch(conn, SQL_UPDATE_NOMBRE_SALIDA_PROGRAMA_TRIBUTARIO, programa,ResumenGeneracion.class);
			        	conn.commit();
			        	contresumen=0;
					};
					*/
		}
		/*
		if (rutLista.size()>0){
			String Estado="< Comprimiendo >";
			programa.add(new ResumenGeneracion(entrada.p_IdProceso, "",Estado,contadorArchivos));
        	sql.batch(conn, SQL_UPDATE_NOMBRE_SALIDA_PROGRAMA_TRIBUTARIO, programa,ResumenGeneracion.class);
        	conn.commit();
        	contresumen=0;
		};
		*/
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::GenerarInformeIGK.obtenerInformacionReporte {}", fin - inicio);
		return rutasArchivos;
	}
	
	
	private String generarReportePDF(Connection conn,ReportDocument report, String rutaBase, List<DatosCertificadoCompleto> listaTraspasoFinal,EntradaGenerarInformeIGK entrada) throws Exception {
		logger.trace("INICIO::GenerarInformeIGK.generarReportePDF");
		long inicio = System.currentTimeMillis();

		String rutaReporte = rutaBase + ".pdf";
		String json = Utils.getGsonBuilder().toJson(listaTraspasoFinal);
		JsonArray informacionCertificado = new JsonParser().parse(json).getAsJsonArray();
		String idTemplate =obtenerIdReporte(conn,entrada);// "173603C07D0185DB1700D20086E4297E";
		log.write( "*GenerarInformeIGK* idTemplate" + idTemplate);
		if (report.readDocument(idTemplate)) {
			try {
				report.setDocumentResultSet(esquemaCertificado, informacionCertificado);
			} catch (Exception e) {
				logger.error(e.getMessage());
			}

			try (InputStream is = report.exportDocument("pdf")) {
				File reportePDF = new File(rutaReporte);
				FileUtils.copyInputStreamToFile(is, reportePDF);
				log.write( "*GenerarInformeIGK* generarReportePDF" + rutaReporte);
			} catch (Exception e) {
				logger.error(e.getMessage());
			}
		} else {
			logger.error("Error al obtener el template {}, generacion de PDF omitida", idTemplate);
		}

		long fin = System.currentTimeMillis();
		logger.trace("FIN::GenerarInformeIGK.generarReportePDF {}", fin - inicio);

		return rutaReporte;
	}


	private List<DatosCertificadoCompleto> obtenerInformacionReporte(List<DatosCertificado> listaCerificados, String logoCorredoraB64, String firmaCorredoraB64) {
		logger.debug("INICIO::GenerarInformeIGK.obtenerInformacionReporte");
		long inicio = System.currentTimeMillis();
		Integer contadorCertificado=1;
		
		List<DatosCertificadoCompleto>  listaTraspasosReporte = new ArrayList<DatosCertificadoCompleto>();
		
		for (DatosCertificado tra : listaCerificados) {
			DatosCertificadoCompleto traspasoImagen = new DatosCertificadoCompleto();

		
			traspasoImagen.FechaComprobante=tra.FechaComprobante;
			traspasoImagen.Folio=tra.Folio;
			traspasoImagen.Instrumento=tra.Instrumento;
			traspasoImagen.Cantidad=tra.Cantidad;
			traspasoImagen.Precio=tra.Precio;
			traspasoImagen.Monto=tra.Monto;
			traspasoImagen.IdMoneda=tra.IdMoneda;
			traspasoImagen.MontoRetencionGananciaCap=tra.MontoRetencionGananciaCap;
			traspasoImagen.MontoRetencionGananciaCapCLP=tra.MontoRetencionGananciaCapCLP;
			traspasoImagen.FechaIngresoRegistro=tra.FechaIngresoRegistro;
			traspasoImagen.NroLinea=tra.NroLinea;
			traspasoImagen.IdCliente=tra.IdCliente;
			traspasoImagen.NombreCompletoCliente=tra.NombreCompletoCliente;
			traspasoImagen.DocumentoIdentidadCliente=tra.DocumentoIdentidadCliente;	
			traspasoImagen.DireccionCliente=tra.DireccionCliente;	
			traspasoImagen.ComunaCliente=tra.ComunaCliente;
			traspasoImagen.CiudadCliente=tra.CiudadCliente;
			traspasoImagen.FechainformeDesde=tra.FechainformeDesde;
			traspasoImagen.FechainformeHasta=tra.FechainformeHasta;
			traspasoImagen.PorcentajeRetencion=tra.PorcentajeRetencion;
		
			traspasoImagen.RazonSocialCorredora=tra.RazonSocialCorredora;
			traspasoImagen.DireccionCorredora=tra.DireccionCorredora;
			traspasoImagen.RutCorredora=tra.RutCorredora;
			traspasoImagen.GiroCorredor=tra.GiroCorredor;
			traspasoImagen.rut_representante=tra.rut_representante;
			traspasoImagen.nombre_representante=tra.nombre_representante;
		
			traspasoImagen.LogoCorredora=logoCorredoraB64;
			traspasoImagen.FirmaCorredora= firmaCorredoraB64;
			
			listaTraspasosReporte.add(traspasoImagen);
			contadorCertificado++;			
		}		
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::GenerarInformeIGK.obtenerInformacionReporte {}", fin - inicio);

		return listaTraspasosReporte;
	}


	private List<DatosCertificado> obtenerListaCertificados(Connection conn,EntradaGenerarInformeIGK entrada)  {
		logger.debug("INICIO::GenerarInformeIGK.obtenerListaCertificados");
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
					log.write( "*GenerarInformeIGK* { SE EJECUTA EL DROP INICIAL}");
				} catch (Exception er) { 
					er.printStackTrace();
					log.write("*GenerarInformeIGK* {DROP INICIAL, NO LOGRADO}");
				}
				
			}else{
				log.write("*GenerarInformeIGK* { NO SE EJECUTA EL DROP INICIAL}");
			}
			
			
			SQL.exec(conn, SQL_CREATE_TABLA_RUT);			
			sql.batch(conn,SQL_INSERT_TABLA_RUT ,entrada.p_Datos, Datos.class);
			
			
			HashMap<String,Object> paramIN = new HashMap<>();
			paramIN.put("IdEmpresa", entrada.p_IdEmpresa);
			paramIN.put("IdUsuario", entrada.p_IdUsuario);
			paramIN.put("IdModulo" , entrada.p_IdModulo);		
			paramIN.put("FechaConsulta" , entrada.p_FechaConsulta);//entrada.p_Certificado.VersionArchivo);
			HashMap<String, Class> paramOUT = new HashMap<>();
			paramOUT.put("p_Salida", DatosCertificado.class);

			HashMap<String, Object> resultadoSP = sql.call(conn, SQL_CALL_CERTIFICADO, paramIN, paramOUT);
			listaCertificados = resultadoSP.get("p_Salida") == null ? new ArrayList<>() : (List<DatosCertificado>) resultadoSP.get("p_Salida");
			
			
						
						
		}catch (Exception e) {
			e.printStackTrace();
	
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::GenerarInformeIGK.obtenerListaCertificados {}", fin - inicio);
		return listaCertificados;
	}
	
	private List<Datos> obtenerGrupoCertificado(Connection conn,EntradaGenerarInformeIGK entrada)  {
		logger.debug("INICIO::GenerarInformeIGK.obtenerGrupoCertificado");
		long inicio = System.currentTimeMillis();
			
		
		
		List<Datos> rutLista =new ArrayList<Datos>();
		
		try {
						
			
			HashMap<String, Object> params = new HashMap<>(2);
			params.put("IdEmpresa", entrada.p_IdEmpresa);
			//params.put("IdTipoDocumento", entrada.p_IdTipoDocumento);
			params.put("FechaConsulta", entrada.p_FechaConsulta);
			rutLista = new SQL().query(conn, SQL_SELECT_GRUPO_CERTIFICA, params, Datos.class);
	
		}catch (Exception e) {
			e.printStackTrace();
			
		}finally {
			try {				
				SQL.exec(conn, SQL_DROP_TABLA_RUT);				
				
				logger.debug("INICIO::GenerarInformeIGK.obtenerListaCertificados { SE EJECUTA EL DROP FINAL}");
			} catch (Exception er) { 
				er.printStackTrace();
				logger.debug("INICIO::GenerarInformeIGK.obtenerListaCertificados { NO SE EJECUTA EL DROP FINAL}");
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::GenerarInformeIGK.obtenerListaCertificados {}", fin - inicio);
		return rutLista;
	}

	public String obtenerLogoCorredoraB64(Connection conn, Long idEmpresa) throws SQLException {
		logger.debug("INICIO::GenerarInformeIGK.obtenerLogoCorredora");
		long inicio = System.currentTimeMillis();

		HashMap<String, Object> params = new HashMap<>(2);
		params.put("IdEmpresa", idEmpresa);
		Imagen logo = new SQL().query(conn, SQL_SELECT_LOGO_CORREDORA, params, Imagen.class).stream().findFirst().orElse(new Imagen());
		String contenido = Base64.encodeBase64String(logo.Contenido);

		long fin = System.currentTimeMillis();
		logger.debug("FIN::GenerarInformeIGK.obtenerLogoCorredora {}", fin - inicio);

		return contenido;
	}
	
	public String obtenerIdReporte(Connection conn, EntradaGenerarInformeIGK entrada) throws SQLException {
		logger.debug("INICIO::GenerarInformeIGK.obtenerIdReporte");
		long inicio = System.currentTimeMillis();
		HashMap<String, Object> params = new HashMap<>(2);
		params.put("IdEmpresa", entrada.p_IdEmpresa);
		params.put("NombreReporte", "REPORTE_IGK");
		
		List<Template> idLista = new SQL().query(conn, SQL_SELECT_ID_REPORTE, params, Template.class);
		//List<Template> idLista = new SQL().query(conn, SQL_SELECT_ID_REPORTE, new HashMap<>(2), Template.class);
		String IdTemplate =idLista.get(0).Id;

		long fin = System.currentTimeMillis();
		logger.debug("FIN::GenerarInformeIGK.obtenerIdReporte {}", fin - inicio);

		return IdTemplate;
	}

	public String obtenerFirmaB64(Connection conn, Long idEmpresa) throws SQLException {
		logger.debug("INICIO::GenerarInformeIGK.obtenerLogoCorredora");
		long inicio = System.currentTimeMillis();

		Imagen firma = new SQL().query(conn, SQL_SELECT_FIRMA_CORREDORA,new HashMap<String, Object>(), Imagen.class).stream().findFirst().orElse(new Imagen());
		String contenido = Base64.encodeBase64String(firma.Contenido);

		long fin = System.currentTimeMillis();
		logger.debug("FIN::GenerarInformeIGK.obtenerLogoCorredora {}", fin - inicio);

		return contenido;
	}
	
	public RaizArchivo obtenerRaizArchivo(Connection conn, Long idEmpresa) throws SQLException {
		logger.debug("INICIO::GenerarInformeIGK.obtenerRaizArchivo");
		long inicio = System.currentTimeMillis();

		HashMap<String, Object> params = new HashMap<>(2);
		params.put("IdEmpresa", idEmpresa);

		RaizArchivo raiz = new SQL().query(conn, SQL_SELECT_RAIZ_ARCHIVO, params, RaizArchivo.class).stream().findFirst().orElse(new RaizArchivo());

		long fin = System.currentTimeMillis();
		logger.debug("INICIO::GenerarInformeIGK.obtenerRaizArchivo {}", fin - inicio);

		return raiz;
	}
	
	private String param1,param2,param3,param4,param5="";
    public void LeerNombreArchivos(Connection dbcon, Long idEmpresa, String nombreReporte, String version) throws Exception{
    	System.out.println("Obtener parametros para nombre de PDFs");
    	//nombreReporte=nombreReporte.replace("CERT", "CERTIFICADO");
    	try {
    		param1=ConfiguracionSistema.getParametroConfiguracionString(dbcon, idEmpresa, "FACTURACION", "INFO_IGK_NOMBREARCHIVO_01");
				System.out.println("param1:" + param1);
				if(param1.length()==0)
				{
					param1="";
				};
			} catch (Exception e) {
				param1="";
			}
    	try {
	    	param2=ConfiguracionSistema.getParametroConfiguracionString(dbcon, idEmpresa, "FACTURACION", "INFO_IGK_NOMBREARCHIVO_02");
			System.out.println("param2:" + param2);
				if(param2.length()==0)
				{
					param2="";
				};
			} catch (Exception e) {
				param2="";
			}		    
    	try {
    		param3=ConfiguracionSistema.getParametroConfiguracionString(dbcon, idEmpresa, "FACTURACION", "INFO_IGK_NOMBREARCHIVO_03");
				System.out.println("param3:" + param3);
				if(param3.length()==0)
				{
					param3="";
				};
			} catch (Exception e) {
				param3="";
			}
    	try {
	    	param4=ConfiguracionSistema.getParametroConfiguracionString(dbcon, idEmpresa, "FACTURACION", "INFO_IGK_NOMBREARCHIVO_04");
			System.out.println("param4:" + param4);
				if(param4.length()==0)
				{
					param4="";
				};
			} catch (Exception e) {
				param4="";
			}	
    	
    	try {
	    	param5=ConfiguracionSistema.getParametroConfiguracionString(dbcon, idEmpresa, "FACTURACION", "INFO_IGK_NOMBREARCHIVO_05");
			System.out.println("param5:" + param5);
				if(param5.length()==0)
				{
					param5="";
				};
			} catch (Exception e) {
				param5="";
			}	    	
    	    if (param1.equalsIgnoreCase("[PERIODO]")){param1=version.substring(0, 7);};if (param2.equalsIgnoreCase("[PERIODO]")){param2=version.substring(0, 7);};if (param3.equalsIgnoreCase("[PERIODO]")){param3=version.substring(0, 7);};if (param4.equalsIgnoreCase("[PERIODO]")){param4=version.substring(0, 7);}if (param5.equalsIgnoreCase("[PERIODO]")){param5=version.substring(0, 7);};
    	    /*
    	    if (param1.equalsIgnoreCase("[DJ]")){param1=esquema.replace("DJ", "");};if (param2.equalsIgnoreCase("[DJ]")){param2=esquema.replace("DJ", "");};if (param3.equalsIgnoreCase("[DJ]")){param3=esquema.replace("DJ", "");};if (param4.equalsIgnoreCase("[DJ]")){param4=esquema.replace("DJ", "");};if (param5.equalsIgnoreCase("[DJ]")){param5=esquema.replace("DJ", "");};
    	    
    	    if (param1.equalsIgnoreCase("[CERT]")){param1=nombreReporte.replace("CERTIFICADO", "");param1 = String.format("%2s",  (param1).length() > 2 ?  (param1).substring(0,2) :  (param1)).replace(' ', '0');};
    	    if (param2.equalsIgnoreCase("[CERT]")){param2=nombreReporte.replace("CERTIFICADO", "");param2 = String.format("%2s",  (param2).length() > 2 ?  (param2).substring(0,2) :  (param2)).replace(' ', '0');};
    	    if (param3.equalsIgnoreCase("[CERT]")){param3=nombreReporte.replace("CERTIFICADO", "");param3 = String.format("%2s",  (param3).length() > 2 ?  (param3).substring(0,2) :  (param3)).replace(' ', '0');};
    	    if (param4.equalsIgnoreCase("[CERT]")){param4=nombreReporte.replace("CERTIFICADO", "");param4 = String.format("%2s",  (param4).length() > 2 ?  (param4).substring(0,2) :  (param4)).replace(' ', '0');};
    	    if (param5.equalsIgnoreCase("[CERT]")){param5=nombreReporte.replace("CERTIFICADO", "");param5 = String.format("%2s",  (param5).length() > 2 ?  (param5).substring(0,2) :  (param5)).replace(' ', '0');};
    	     */
    	    
		  
	}

	
	public static void main(String[] args) {
		System.out.println("== INICIO TEST ==");

		//try (Connection conn = PlataformaUtils.getMASTER1Connection()) {
		try (Connection conn = PlataformaUtils.getDESAConnection()) {
			EntradaGenerarInformeIGK entrada = new EntradaGenerarInformeIGK();
			
			entrada.p_IdEmpresa = 1L;
			entrada.p_IdUsuario = 21L;
			entrada.p_IdModulo = "NOR";
			entrada.p_IdProceso=559065L;
			Datos Rut = new Datos();
			//Rut.Rut = "1-9";
			entrada.p_Datos.add(Rut);
			entrada.p_FechaConsulta="2022-08-01";
			
			
			//entrada.p_DataSource = PlataformaUtils.getMASTER1Datasource();
			entrada.p_DataSource = PlataformaUtils.getDESADatasource();

			SalidaGenerarInformeIGK salida = new GenerarInformeIGK().logica(conn, entrada);
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
