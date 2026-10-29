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

import cl.bcs.normativa.declaraciones.DJ1922.ReporteCertificado43;
import cl.bcs.normativa.declaraciones.DJ1922.ReporteCertificado43.ReporteCertificado43Entrada;
import cl.bcs.normativa.declaraciones.DJ1922.ReporteCertificado43.ReporteCertificado43Salida;

@SuppressWarnings({"rawtypes", "unchecked"})
public class GenerarCertificado43Masivo implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(GenerarCertificado43Masivo.class);	
	
	private final String SQL_SELECT_LOGO_CORREDORA  = loadResourceToString("cl/bcs/normativa/declaraciones/sql/GenerarCertificadoMasivo_SelectLogoCorredora.sql");
	private final String SQL_SELECT_RAIZ_ARCHIVO    = loadResourceToString("cl/bcs/normativa/declaraciones/sql/GenerarCertificadoMasivo_SelectRaizArchivo.sql");	
	private final String SQL_SELECT_FIRMA_CORREDORA = loadResourceToString("cl/bcs/normativa/declaraciones/sql/GenerarCertificadoMasivo_SelectImagenFirma.sql");

	private final String SQL_SELECT_ID_REPORTE      = loadResourceToString("cl/bcs/normativa/declaraciones/sql/GenerarCertificadoMasivo_SelectReporte.sql");
	public  final String SQL_EXIST_TABLA_RUT  		= "select 1 AS \"Tab\" from m_temporary_tables where table_name = '#GENERARCERTIFICADO43MASIVO_RUT'";
	private final String SQL_CREATE_TABLA_RUT 		= "CREATE LOCAL TEMPORARY TABLE #GenerarCertificado43Masivo_RUT (\"Rut\" NVARCHAR(40))";													
	private final String SQL_INSERT_TABLA_RUT		= "INSERT INTO #GenerarCertificado43Masivo_RUT (\"Rut\") VALUES (:Rut)";
	
	
	public  final String SQL_DROP_TABLA_RUT  		= "DROP TABLE #GenerarCertificado43Masivo_RUT";
	private final String SQL_UPDATE_NOMBRE_SALIDA_PROGRAMA_TRIBUTARIO = "UPDATE \"BSCL.Normativa.BD::TRI.ResumenGeneracion\" SET \"NombreArchivo\"=:RutaArchivo, \"Glosa\"=:Glosa, \"TotalRegistrosGenerados\"=:TotalRegistrosGenerados WHERE \"IdProceso\"= :IdProceso";
	/*Servicio de Crystal Report*/
	private final String DJ="1922";
	private final String SQL_SELECT_GRUPO_CERTIFICA = loadResourceToString("cl/bcs/normativa/declaraciones/sql/GenerarCertificadoMasivo_SelectGrupoCertificado43.sql");
	private final String SQL_CALL_CERTIFICADO	    = loadResourceToString("cl/bcs/normativa/declaraciones/sql/GenerarCertificadoMasivo_certificado43_Call.sql");	

	private final String SQL_UPDATE_CERTIFICADO_ESTADO = loadResourceToString("cl/bcs/normativa/jobs/sql/BcsJobGenerarCertificadosTributarios_UpdateCertificadoClienteEstado.sql");

	private final Log log = new Log("NOR.GenerarCertificado43Masivo");
	
	private static final Map<String, String> esquemaCertificado = new HashMap<>();
	public String v_RutCertificado="";
	public long contadorArchivos=0;
	static {
		esquemaCertificado.put("IdCliente", "number");
		esquemaCertificado.put("LogoCorredora", "blob");
		esquemaCertificado.put("FirmaCorredora", "blob");
	}

	public static class EntradaGenerarCertificado43Masivo extends BcsPojo{
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
		
		public EntradaGenerarCertificado43Masivo() {
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

	
	public static class SalidaGenerarCertificado43Masivo extends BcsPojo {
		public Long P_OUT_ERRORESCONTADOR;
		public List<PlataformaUtils.MensajeSalida> P_OUT_MENSAJES;
		public String p_out_RutaZip;
		public String p_out_NombreZip;
		public Long p_out_contadorArchivos;

		public SalidaGenerarCertificado43Masivo() {
			P_OUT_ERRORESCONTADOR = 0L;
			P_OUT_MENSAJES = new ArrayList<>();
		}
	}
	
	public static class Rut extends BcsPojo{
		public String Rut;		
		
		public Rut () {
			this.Rut = "";			
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
  
		public Long IdCliente;
        public int IdPortafolio;
        public String NombreCompletoEmisor;
        public String RutEmisor;
        public String TipoOperacionCertificado;
		public String NombreCompletoCliente;
        public String DocumentoIdentidadCliente;
        public String RazonSocialEmpresa;
        public String DocumentoIdentidadEmpresa;
        public String DireccionEmpresa;
		public String CiudadFecha;
        public String GiroActividad;
        public String FechaNumeroCuotas;
        public String FechaMontoActualizado;
        public Long IdCertificadoCliente;
		public String FechaCertificado;
        public Long IdOperacion;
        public String RutAdministradora;
        public String IdEmisor;
        public String RutFondo;
		public String RunFondo;
        public String Fondo;
        public String TipoFondo;
        public String RutCliente;
        public String NombreParticipe;
		public String TaxId;
        public String CodigoPais;
        public String TipoOperacion;
        public Date FechaInversion;
		public String FechaInversionText;
        public String MonedaReajuste;
		public BigDecimal MontoNominalAportes;
        public BigDecimal MontoNominalAportesActualizado;
        public Long NumeroSerieCuotas;
        public BigDecimal CantidadCuotasParticipe;
        public Long ParticipeCumpleRequisitos;
		public Long CuotasCumplenRequisitos;
        public BigDecimal ValorCuotaPesos;
        public BigDecimal NumCuotasSerieParticipe;
        public BigDecimal PorcentajeParticipacion31Dic;
        public BigDecimal ValNominalCuotasRescatadas;
        public BigDecimal CantidadCuotasRescatadas;
		public BigDecimal DiferenciaPorRescateOEnajenacion;
        public BigDecimal DiferenciaPorRescateOEnajenacionActualizado;
        public BigDecimal DifPorRescateOEnajenacionCFIAcogido107;
        public BigDecimal DifPorRescateOEnajenacionCFIAcogido107Actualizado;
        public BigDecimal MENORVALOR_I;
        public BigDecimal MAYORVALOR_I;
        public BigDecimal MENORVALOR_FM;     
        public BigDecimal MAYORVALOR_FM;      
		public BigDecimal ValNominalInversionaFechaLiquidacion;
        public BigDecimal CantCuotasCedidasPorLiquidacion;
        public BigDecimal DiferenciaValorCuotasFechaLiquidacion;
        public BigDecimal DiferenciaValorCuotasFechaLiquidacionActualizado;
        public String TributaSa;
		public BigDecimal DivRemDistAfectGCConIDPC;
        public BigDecimal DivRemDistAfectGCConIDPCal31122016;
        public BigDecimal DivRemDistAfectGCConIDPCVOL;
        public BigDecimal DivRemDistAfectGCSINDC;
        public BigDecimal DisRemRentasTribCProvRap_C96;
		public BigDecimal DisRemRentasTribCORP_C97;
        public BigDecimal DisRemRentasTribCExDD_C98;
        public BigDecimal DisRemRentasTribCUtAFFut_C99;
        public BigDecimal DivRemDistNoRentaFUT;
        public BigDecimal DisRemRentasExIGCAfecImptoAdi_C100;
		public BigDecimal CredImpGloCompNSRSD;
        public BigDecimal CredImpGloCompNSRCD;
        public BigDecimal CredIGCAcumD01012017RANSRD01012020SDD_C101;
        public BigDecimal CredIGCAcumD01012017RANSRD01012020CDD_C102;
        public BigDecimal CredImpGloCompSRSD;   
		public BigDecimal CredImpGloCompSRCD;
        public BigDecimal CredIGCAcumD01012017RXSRSDD_C103;
        public BigDecimal CredIGCAcumD01012017RXSRCDD_C104;
        public BigDecimal CredImpGloCompCTDCIF;
        public BigDecimal CredImpGloCompCDD31122016;
		public BigDecimal CredImpGloCompSDD31122016;
        public BigDecimal CredIGCAcumH31122016RXSDD_C105;
        public BigDecimal CredIGCAcumH31122016RXCDD_C106;
        public BigDecimal CredImpGloCompCTDCIF31122016;
        public BigDecimal CredImpGloCompCITAExArt21;  
		public String DetBenNetoPerRutFdo_C107;
        public BigDecimal DetBenNetoPerPOS_C113;
        public BigDecimal DetBenNetoPerNEG_C114;
        public BigDecimal DividendosExentosIGCoAdicional;
        public BigDecimal DividendosExentosIGCoAdicionalActualizado;
		public BigDecimal DividendosNoConstitutivosDeRenta;
        public BigDecimal DividendosNoConstitutivosDeRentaActualizado;
        public BigDecimal DivRemDistNoRentaFE;
        public BigDecimal DividendosAfectosImptoUnicoTasa10;
        public BigDecimal DividendosAfectosImptoUnicoTasa10Actualizado;
		public BigDecimal DividendosAfectosImptoUnicoTasa4;
        public BigDecimal DividendosAfectosImptoUnicoTasa4Actualizado;
        public BigDecimal DistribucionXDiminucionNoImputadaCapital;
        public BigDecimal DevolucionesDeCapital;
        public BigDecimal DevolucionesDeCapitalActualizado;                       
		public BigDecimal MontoInversionenCuotasCFIAnterior93;
        public Long NumeroCertificado;
        public BigDecimal FactorActualizacion;
        public String FechaOrigenInversion;
        public BigDecimal SumaNumeroCuotas; 
		public BigDecimal SumaMontoActualizado;
        public String Certificado;
        public String EstadoCertificado;
        public String NombreFondo;

		public String RazonSocialCorredora;
		public String DireccionCorredora;
		public String DocumentoIdentidadCorredora;

		public String rut_representante;
		public String nombre_representante;
		
		public String RutCertificado;
		public String RunNemotecnico;
		
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
	
	public SalidaGenerarCertificado43Masivo logica(Connection conn, EntradaGenerarCertificado43Masivo entrada) throws Exception {
		logger.info("INICIO::GenerarCertificado43Masivo");
		long inicio = System.currentTimeMillis();
		log.write( "*GenerarCertificado43Masivo* INICIO");
		String Estado="Procesando";
		SQL sql = new SQL();
		SalidaGenerarCertificado43Masivo salida = new SalidaGenerarCertificado43Masivo();
		
		String logoCorredoraB64  = obtenerLogoCorredoraB64(conn, entrada.p_IdEmpresa);
		String firmaCorredoraB64 = obtenerFirmaB64(conn, entrada.p_IdEmpresa);
		log.write( "*GenerarCertificado43Masivo* obtenerRaizArchivo");
		RaizArchivo raizArchivo = obtenerRaizArchivo(conn, entrada.p_IdEmpresa);
		if (StringUtils.isNullOrBlank(raizArchivo.RutaArchivo) || StringUtils.isNullOrBlank(raizArchivo.RutaNOR)) {
			salida.P_OUT_ERRORESCONTADOR++;
			salida.P_OUT_MENSAJES.add(new PlataformaUtils.MensajeSalida(0L, PlataformaUtils.MensajeSalida.MENSAJE_ERROR, "FALTA_CONFIGURACION_RUTA_ARCHIVO", new HashMap<>(), conn, entrada.p_IdEmpresa));
			return salida;
		}
		log.write( "*GenerarCertificado43Masivo* RutaArchivo" + raizArchivo.RutaArchivo);
		// Para pruebas locales.
		//final String rutaAbsolutaBase = System.getProperty("user.home") + File.separator + "Desktop" + File.separator + "Certificado31" + File.separator;
		final String rutaAbsolutaBase = raizArchivo.RutaArchivo;
		final String rutaRelativaBase = raizArchivo.RutaNOR;
		final String nombreBaseArchivo = "CertTRI43_";
		String nombreBaseZip = "CerTRI43_";
		if (entrada.p_NombreZIP !=""){
			nombreBaseZip=entrada.p_NombreZIP;
		}
		final String timestampSistema = Fecha.toFormat(FechaDB.ObtenerFechaHoraSistemaTs(conn, entrada.p_IdEmpresa), "yyyyMMdd_HHmmss");
		
		//buscar datos 		
		log.write( "*GenerarCertificado43Masivo* obtenerListaCertificados");
		List<DatosCertificado> listaCerificados = obtenerListaCertificados (conn , entrada);
		
		//Se filtran los rut de los certificados
        List<Rut> listaRutCertificados = entrada.p_listaRut.stream()
        		.filter(rut -> listaCerificados.stream()
        				.anyMatch(certificado -> certificado.RutCliente.equals(rut.Rut)))
        		.collect(Collectors.toList());
        //Se filtran Rut de entrada para que tome solo los que tienen certificados
        entrada.p_listaRut = entrada.p_listaRut.stream()
        		.filter(rut -> listaRutCertificados.contains(rut))
        		.collect(Collectors.toList());
		
		if(listaCerificados.isEmpty()) {
			salida.P_OUT_MENSAJES.add(new MensajeSalida( 0L, MensajeSalida.MENSAJE_ERROR, "UI_SIN_DATOS", "No se encontraron datos consultados"));
			salida.P_OUT_ERRORESCONTADOR = Long.valueOf(salida.P_OUT_MENSAJES.size());
			return salida;
		};
		log.write( "*GenerarCertificado43Masivo* LeerNombreArchivos");
		LeerNombreArchivos(conn,entrada.p_IdEmpresa,entrada.p_IdTipoDocumento,entrada.p_VersionArchivo,DJ);
		
		log.write( "*GenerarCertificado43Masivo* obtenerTraspasoReporte");
		List<DatosCertificadoCompleto> traspasosInformacion= obtenerTraspasoReporte(listaCerificados, logoCorredoraB64,firmaCorredoraB64);
		log.write( "*GenerarCertificado43Masivo* obtenerGrupoCertificado");
		List<Rut> GrupoCertificado=obtenerGrupoCertificado(conn , entrada);
		
		//Se filtran Rut de GrupoCertificado para que tome solo los que tienen certificados
        GrupoCertificado = GrupoCertificado.stream()
        		.filter(rut -> listaRutCertificados.stream()
        				.anyMatch(certificado -> certificado.Rut.equals(rut.Rut)))
        		.collect(Collectors.toList());
		
		if(GrupoCertificado.size()>0) {
			 List<ResumenGeneracion> programa = new ArrayList<ResumenGeneracion>();
				if (entrada.p_IdProceso>0){
					Estado="Iniciando PDF:< " + GrupoCertificado.size() + " > ";
					programa.add(new ResumenGeneracion(entrada.p_IdProceso, "",Estado,0L));
		        	sql.batch(conn, SQL_UPDATE_NOMBRE_SALIDA_PROGRAMA_TRIBUTARIO, programa,ResumenGeneracion.class);
		        	conn.commit();
				};
		}
		
		log.write( "*GenerarCertificado43Masivo* generarArchivosInformes");
		List<String> rutasArchivosSinComprimir = generarArchivosInformes(conn,entrada.p_DataSource, rutaAbsolutaBase, nombreBaseArchivo, traspasosInformacion,entrada,GrupoCertificado);
		log.write( "*GenerarCertificado43Masivo* comprimirArchivos");
		salida.p_out_NombreZip = comprimirArchivos(timestampSistema, rutaAbsolutaBase, rutaRelativaBase, nombreBaseZip, rutasArchivosSinComprimir);
		salida.p_out_RutaZip= rutaRelativaBase + salida.p_out_NombreZip;
		salida.p_out_contadorArchivos=contadorArchivos;
		//salida.p_out_RutaZip="nor/Certificado43-10005725-5.pdf";
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::GenerarCertificado43Masivo {}", fin - inicio);
		log.write( "*GenerarCertificado43Masivo* FIN");
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


	private List<String> generarArchivosInformes(Connection conn,DataSource dataSource, String rutaAbsoluta,String nombreBaseArchivo, List<DatosCertificadoCompleto> traspasosInformacion,EntradaGenerarCertificado43Masivo entrada,List<Rut> rutLista) throws Exception {
		logger.debug("INICIO::GenerarCertificado43Masivo.generarArchivosInformes");
		String updTramo = entrada.p_NombreZIP.replace("TRI_CERT43_PT", "P");
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
					certEstadoRut.add(new CertificadoEstadoRut("CERT43", entrada.p_VersionArchivo,"GEN",rut.Rut));
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
		logger.debug("FIN::GenerarCertificado43Masivo.obtenerTraspasoReporte {}", fin - inicio);
		return rutasArchivos;
	}
	
	
	private String generarReportePDF(Connection conn,ReportDocument report, String rutaBase, List<DatosCertificadoCompleto> listaTraspasoFinal,EntradaGenerarCertificado43Masivo entrada) throws Exception {
		logger.trace("INICIO::GenerarCertificado43Masivo.generarReportePDF");
		long inicio = System.currentTimeMillis();

		String rutaReporte = rutaBase + ".pdf";
		String json = Utils.getGsonBuilder().toJson(listaTraspasoFinal);
		JsonArray informacionCertificado = new JsonParser().parse(json).getAsJsonArray();
		String idTemplate =obtenerIdReporte(conn,entrada);// "173603C07D0185DB1700D20086E4297E";
		log.write( "*GenerarCertificado43Masivo* idTemplate" + idTemplate);
		if (report.readDocument(idTemplate)) {
			try {
				report.setDocumentResultSet(esquemaCertificado, informacionCertificado);
			} catch (Exception e) {
				logger.error(e.getMessage());
			}

			try (InputStream is = report.exportDocument("pdf")) {
				File reportePDF = new File(rutaReporte);
				FileUtils.copyInputStreamToFile(is, reportePDF);
				log.write( "*GenerarCertificado43Masivo* generarReportePDF" + rutaReporte);
			} catch (Exception e) {
				logger.error(e.getMessage());
			}
		} else {
			logger.error("Error al obtener el template {}, generacion de PDF omitida", idTemplate);
		}

		long fin = System.currentTimeMillis();
		logger.trace("FIN::GenerarCertificado43Masivo.generarReportePDF {}", fin - inicio);

		return rutaReporte;
	}


	private List<DatosCertificadoCompleto> obtenerTraspasoReporte(List<DatosCertificado> listaCerificados, String logoCorredoraB64, String firmaCorredoraB64) {
		logger.debug("INICIO::GenerarCertificado43Masivo.obtenerTraspasoReporte");
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
            traspasoImagen.IdCliente=tra.IdCliente;
            
            traspasoImagen.IdPortafolio=tra.IdPortafolio;
            traspasoImagen.NombreCompletoEmisor=tra.NombreCompletoEmisor;
            traspasoImagen.RutEmisor=tra.RutEmisor;
            traspasoImagen.TipoOperacionCertificado=tra.TipoOperacionCertificado;
            traspasoImagen.NombreCompletoCliente=tra.NombreCompletoCliente;
            traspasoImagen.DocumentoIdentidadCliente=tra.DocumentoIdentidadCliente;
            traspasoImagen.RazonSocialEmpresa=tra.RazonSocialEmpresa;
            traspasoImagen.DocumentoIdentidadEmpresa=tra.DocumentoIdentidadEmpresa;
            traspasoImagen.DireccionEmpresa=tra.DireccionEmpresa;
            traspasoImagen.CiudadFecha=tra.CiudadFecha;
            traspasoImagen.GiroActividad=tra.GiroActividad;
            traspasoImagen.FechaNumeroCuotas=tra.FechaNumeroCuotas;
            traspasoImagen.FechaMontoActualizado=tra.FechaMontoActualizado;
            traspasoImagen.IdCertificadoCliente=tra.IdCertificadoCliente;
            traspasoImagen.FechaCertificado=tra.FechaCertificado;
            traspasoImagen.IdOperacion=tra.IdOperacion;
            traspasoImagen.RutAdministradora=tra.RutAdministradora;
            traspasoImagen.IdEmisor=tra.IdEmisor;
            traspasoImagen.RutFondo=tra.RutFondo;
            traspasoImagen.RunFondo=tra.RunFondo;
            traspasoImagen.Fondo=tra.Fondo;
            traspasoImagen.TipoFondo=tra.TipoFondo;
            traspasoImagen.RutCliente=tra.RutCliente;
            traspasoImagen.NombreParticipe=tra.NombreParticipe;
            traspasoImagen.TaxId=tra.TaxId;
            traspasoImagen.CodigoPais=tra.CodigoPais;
            traspasoImagen.TipoOperacion=tra.TipoOperacion;
            traspasoImagen.FechaInversion=tra.FechaInversion;
			traspasoImagen.FechaInversionText=tra.FechaInversionText;
            traspasoImagen.MonedaReajuste=tra.MonedaReajuste;
            traspasoImagen.MontoNominalAportes=tra.MontoNominalAportes;
            traspasoImagen.MontoNominalAportesActualizado=tra.MontoNominalAportesActualizado;
            traspasoImagen.NumeroSerieCuotas=tra.NumeroSerieCuotas;
            traspasoImagen.CantidadCuotasParticipe=tra.CantidadCuotasParticipe;
            traspasoImagen.ParticipeCumpleRequisitos=tra.ParticipeCumpleRequisitos;
            traspasoImagen.CuotasCumplenRequisitos=tra.CuotasCumplenRequisitos;
            traspasoImagen.ValorCuotaPesos=tra.ValorCuotaPesos;
            traspasoImagen.NumCuotasSerieParticipe=tra.NumCuotasSerieParticipe;
            traspasoImagen.PorcentajeParticipacion31Dic=tra.PorcentajeParticipacion31Dic;
            traspasoImagen.ValNominalCuotasRescatadas=tra.ValNominalCuotasRescatadas;
            traspasoImagen.CantidadCuotasRescatadas=tra.CantidadCuotasRescatadas;
            traspasoImagen.DiferenciaPorRescateOEnajenacion=tra.DiferenciaPorRescateOEnajenacion;
            traspasoImagen.DiferenciaPorRescateOEnajenacionActualizado=tra.DiferenciaPorRescateOEnajenacionActualizado;
            traspasoImagen.DifPorRescateOEnajenacionCFIAcogido107=tra.DifPorRescateOEnajenacionCFIAcogido107;
            traspasoImagen.DifPorRescateOEnajenacionCFIAcogido107Actualizado=tra.DifPorRescateOEnajenacionCFIAcogido107Actualizado;
            traspasoImagen.MENORVALOR_I =tra.MENORVALOR_I; 
            traspasoImagen.MAYORVALOR_I =tra.MAYORVALOR_I;
            traspasoImagen.MENORVALOR_FM =tra.MENORVALOR_FM;
            traspasoImagen.MAYORVALOR_FM =tra.MAYORVALOR_FM;
            traspasoImagen.ValNominalInversionaFechaLiquidacion=tra.ValNominalInversionaFechaLiquidacion;
            traspasoImagen.CantCuotasCedidasPorLiquidacion=tra.CantCuotasCedidasPorLiquidacion;
            traspasoImagen.DiferenciaValorCuotasFechaLiquidacion=tra.DiferenciaValorCuotasFechaLiquidacion;
            traspasoImagen.DiferenciaValorCuotasFechaLiquidacionActualizado=tra.DiferenciaValorCuotasFechaLiquidacionActualizado;
            traspasoImagen.TributaSa=tra.TributaSa;
            traspasoImagen.DivRemDistAfectGCConIDPC=tra.DivRemDistAfectGCConIDPC;
            traspasoImagen.DivRemDistAfectGCConIDPCal31122016=tra.DivRemDistAfectGCConIDPCal31122016;
            traspasoImagen.DivRemDistAfectGCConIDPCVOL=tra.DivRemDistAfectGCConIDPCVOL;
            traspasoImagen.DivRemDistAfectGCSINDC=tra.DivRemDistAfectGCSINDC;
            traspasoImagen.DisRemRentasTribCProvRap_C96=tra.DisRemRentasTribCProvRap_C96;
            traspasoImagen.DisRemRentasTribCORP_C97=tra.DisRemRentasTribCORP_C97;
            traspasoImagen.DisRemRentasTribCExDD_C98=tra.DisRemRentasTribCExDD_C98;
            traspasoImagen.DisRemRentasTribCUtAFFut_C99=tra.DisRemRentasTribCUtAFFut_C99;
            traspasoImagen.DivRemDistNoRentaFUT=tra.DivRemDistNoRentaFUT;
            traspasoImagen.DisRemRentasExIGCAfecImptoAdi_C100=tra.DisRemRentasExIGCAfecImptoAdi_C100;
            traspasoImagen.CredImpGloCompNSRSD=tra.CredImpGloCompNSRSD;
            traspasoImagen.CredImpGloCompNSRCD=tra.CredImpGloCompNSRCD;
            traspasoImagen.CredIGCAcumD01012017RANSRD01012020SDD_C101=tra.CredIGCAcumD01012017RANSRD01012020SDD_C101;
            traspasoImagen.CredIGCAcumD01012017RANSRD01012020CDD_C102=tra.CredIGCAcumD01012017RANSRD01012020CDD_C102;
            traspasoImagen.CredImpGloCompSRSD =tra.CredImpGloCompSRSD; 
            traspasoImagen.CredImpGloCompSRCD=tra.CredImpGloCompSRCD;
            traspasoImagen.CredIGCAcumD01012017RXSRSDD_C103=tra.CredIGCAcumD01012017RXSRSDD_C103;
            traspasoImagen.CredIGCAcumD01012017RXSRCDD_C104=tra.CredIGCAcumD01012017RXSRCDD_C104;
            traspasoImagen.CredImpGloCompCTDCIF=tra.CredImpGloCompCTDCIF;
            traspasoImagen.CredImpGloCompCDD31122016=tra.CredImpGloCompCDD31122016;
            traspasoImagen.CredImpGloCompSDD31122016=tra.CredImpGloCompSDD31122016;
            traspasoImagen.CredIGCAcumH31122016RXSDD_C105=tra.CredIGCAcumH31122016RXSDD_C105;
            traspasoImagen.CredIGCAcumH31122016RXCDD_C106=tra.CredIGCAcumH31122016RXCDD_C106;
            traspasoImagen.CredImpGloCompCTDCIF31122016=tra.CredImpGloCompCTDCIF31122016;
            traspasoImagen.CredImpGloCompCITAExArt21 =tra.CredImpGloCompCITAExArt21; 
            traspasoImagen.DetBenNetoPerRutFdo_C107=tra.DetBenNetoPerRutFdo_C107;
            traspasoImagen.DetBenNetoPerPOS_C113=tra.DetBenNetoPerPOS_C113;
            traspasoImagen.DetBenNetoPerNEG_C114=tra.DetBenNetoPerNEG_C114;
            traspasoImagen.DividendosExentosIGCoAdicional=tra.DividendosExentosIGCoAdicional;
            traspasoImagen.DividendosExentosIGCoAdicionalActualizado=tra.DividendosExentosIGCoAdicionalActualizado;
            traspasoImagen.DividendosNoConstitutivosDeRenta=tra.DividendosNoConstitutivosDeRenta;
            traspasoImagen.DividendosNoConstitutivosDeRentaActualizado=tra.DividendosNoConstitutivosDeRentaActualizado;
            traspasoImagen.DivRemDistNoRentaFE=tra.DivRemDistNoRentaFE;
            traspasoImagen.DividendosAfectosImptoUnicoTasa10=tra.DividendosAfectosImptoUnicoTasa10;
            traspasoImagen.DividendosAfectosImptoUnicoTasa10Actualizado=tra.DividendosAfectosImptoUnicoTasa10Actualizado;
            traspasoImagen.DividendosAfectosImptoUnicoTasa4=tra.DividendosAfectosImptoUnicoTasa4;
            traspasoImagen.DividendosAfectosImptoUnicoTasa4Actualizado=tra.DividendosAfectosImptoUnicoTasa4Actualizado;
            traspasoImagen.DistribucionXDiminucionNoImputadaCapital=tra.DistribucionXDiminucionNoImputadaCapital;
            traspasoImagen.DevolucionesDeCapital=tra.DevolucionesDeCapital;
            traspasoImagen.DevolucionesDeCapitalActualizado =tra.DevolucionesDeCapitalActualizado; 
            traspasoImagen.MontoInversionenCuotasCFIAnterior93=tra.MontoInversionenCuotasCFIAnterior93;
            traspasoImagen.NumeroCertificado=tra.NumeroCertificado;
            traspasoImagen.FactorActualizacion=tra.FactorActualizacion;
            traspasoImagen.FechaOrigenInversion=tra.FechaOrigenInversion;
            traspasoImagen.SumaNumeroCuotas =tra.SumaNumeroCuotas; 
            traspasoImagen.SumaMontoActualizado=tra.SumaMontoActualizado;
            traspasoImagen.Certificado=tra.Certificado;
            traspasoImagen.EstadoCertificado=tra.EstadoCertificado;
            traspasoImagen.NombreFondo=tra.NombreFondo;

            traspasoImagen.RazonSocialCorredora=tra.RazonSocialCorredora;
            traspasoImagen.DireccionCorredora=tra.DireccionCorredora;
            traspasoImagen.DocumentoIdentidadCorredora=tra.DocumentoIdentidadCorredora;
            traspasoImagen.rut_representante=tra.rut_representante;
            traspasoImagen.nombre_representante=tra.nombre_representante;
            traspasoImagen.RutCertificado=tra.RutCertificado;
			
			traspasoImagen.LogoCorredora    =logoCorredoraB64;
			traspasoImagen.FirmaCorredora   =firmaCorredoraB64;
			traspasoImagen.RunNemotecnico = tra.RunNemotecnico;
			
			listaTraspasosReporte.add(traspasoImagen);
			contadorCertificado++;			
		}		
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::GenerarCertificado43Masivo.obtenerTraspasoReporte {}", fin - inicio);

		return listaTraspasosReporte;
	}


	private List<DatosCertificado> obtenerListaCertificados(Connection conn,EntradaGenerarCertificado43Masivo entrada)  {
		logger.debug("INICIO::GenerarCertificado43Masivo.obtenerListaCertificados");
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
					log.write( "*GenerarCertificado43Masivo* { SE EJECUTA EL DROP INICIAL}");
				} catch (Exception er) { 
					er.printStackTrace();
					log.write("*GenerarCertificado43Masivo* {DROP INICIAL, NO LOGRADO}");
				}
				
			}else{
				log.write("*GenerarCertificado43Masivo* { NO SE EJECUTA EL DROP INICIAL}");
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
			ReporteCertificado43 Certificado43 = new ReporteCertificado43();
			ReporteCertificado43Entrada EntradaCertificado43 = new ReporteCertificado43Entrada();
			
			List<ReporteCertificado43Salida> SalidaCertificado43 = new ArrayList<ReporteCertificado43Salida>();
			
			List<ResumenGeneracion> prog = new ArrayList<ResumenGeneracion>();
			String Est="< Buscando... >";
			prog.add(new ResumenGeneracion(entrada.p_IdProceso, "v 2.0",Est,0L));
        	sql.batch(conn, SQL_UPDATE_NOMBRE_SALIDA_PROGRAMA_TRIBUTARIO, prog,ResumenGeneracion.class);
        	conn.commit();
	
			
			EntradaCertificado43.p_IdEmpresa	=entrada.p_IdEmpresa;
			EntradaCertificado43.p_IdModulo 	=entrada.p_IdModulo;
			EntradaCertificado43.p_IdUsuario	=entrada.p_IdUsuario;
			EntradaCertificado43.p_Version		=entrada.p_VersionArchivo;
			SalidaCertificado43 = Certificado43.logica(conn,EntradaCertificado43);
			
			SalidaCertificado43.removeIf(certificado -> certificado.IdCertificadoCliente == 0);
			
			if (SalidaCertificado43.size()>0) {
				
				List<ResumenGeneracion> prog2 = new ArrayList<ResumenGeneracion>();
				String Est2="< Encontrados... >";
				prog2.add(new ResumenGeneracion(entrada.p_IdProceso, "",Est2,0L));
	        	sql.batch(conn, SQL_UPDATE_NOMBRE_SALIDA_PROGRAMA_TRIBUTARIO, prog2,ResumenGeneracion.class);
	        	conn.commit();
				listaCertificados=AgregarCertificado( SalidaCertificado43);
				log.write("*GenerarCertificado9Masivo* { arma lista desde JAVA}" + listaCertificados.toString());
				
				
			}
			//Fin reporte JAVA

						
						
		}catch (Exception e) {
			e.printStackTrace();
	
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::GenerarCertificado43Masivo.obtenerListaCertificados {}", fin - inicio);
		return listaCertificados;
	}
	
	private List<DatosCertificado> AgregarCertificado(List<ReporteCertificado43Salida> SalidaCertificado43)  {
		List<DatosCertificado> o = new ArrayList<DatosCertificado>();
		
		for (ReporteCertificado43Salida  datos : SalidaCertificado43) {
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
			out.IdCliente=datos.IdCliente;
			out.IdPortafolio= datos.IdPortafolio;
			out.NombreCompletoCliente=datos.NombreCompletoCliente;
			out.DocumentoIdentidadEmpresa=datos.DocumentoIdentidadEmpresa;
			out.CiudadFecha=datos.CiudadFecha;
			
			out.IdPortafolio=datos.IdPortafolio;
            out.NombreCompletoEmisor=datos.NombreCompletoEmisor;
            out.RutEmisor=datos.RutEmisor;
            out.TipoOperacionCertificado=datos.TipoOperacionCertificado;
            out.NombreCompletoCliente=datos.NombreCompletoCliente;
            out.DocumentoIdentidadCliente=datos.DocumentoIdentidadCliente;
            out.RazonSocialEmpresa=datos.RazonSocialEmpresa;
            out.DocumentoIdentidadEmpresa=datos.DocumentoIdentidadEmpresa;
            out.DireccionEmpresa=datos.DireccionEmpresa;
            out.CiudadFecha=datos.CiudadFecha;
            out.GiroActividad=datos.GiroActividad;
            out.FechaNumeroCuotas=datos.FechaNumeroCuotas;
            out.FechaMontoActualizado=datos.FechaMontoActualizado;
            out.IdCertificadoCliente=datos.IdCertificadoCliente;
            out.FechaCertificado=datos.FechaCertificado;
            out.IdOperacion=datos.IdOperacion;
            out.RutAdministradora=datos.RutAdministradora;
            out.IdEmisor=datos.IdEmisor;
            out.RutFondo=datos.RutFondo;
            out.RunFondo=datos.RunFondo;
            out.Fondo=datos.Fondo;
            out.TipoFondo=datos.TipoFondo;
            out.RutCliente=datos.RutCliente;
            out.NombreParticipe=datos.NombreParticipe;
            out.TaxId=datos.TaxId;
            out.CodigoPais=datos.CodigoPais;
            out.TipoOperacion=datos.TipoOperacion;
            out.FechaInversion=datos.FechaInversion;
			out.FechaInversionText=datos.FechaInversionText;
            out.MonedaReajuste=datos.MonedaReajuste;
            out.MontoNominalAportes=datos.MontoNominalAportes;
            out.MontoNominalAportesActualizado=datos.MontoNominalAportesActualizado;
            out.NumeroSerieCuotas=datos.NumeroSerieCuotas;
            out.CantidadCuotasParticipe=datos.CantidadCuotasParticipe;
            out.ParticipeCumpleRequisitos=datos.ParticipeCumpleRequisitos;
            out.CuotasCumplenRequisitos=datos.CuotasCumplenRequisitos;
            out.ValorCuotaPesos=datos.ValorCuotaPesos;
            out.NumCuotasSerieParticipe=datos.NumCuotasSerieParticipe;
            out.PorcentajeParticipacion31Dic=datos.PorcentajeParticipacion31Dic;
            out.ValNominalCuotasRescatadas=datos.ValNominalCuotasRescatadas;
            out.CantidadCuotasRescatadas=datos.CantidadCuotasRescatadas;
            out.DiferenciaPorRescateOEnajenacion=datos.DiferenciaPorRescateOEnajenacion;
            out.DiferenciaPorRescateOEnajenacionActualizado=datos.DiferenciaPorRescateOEnajenacionActualizado;
            out.DifPorRescateOEnajenacionCFIAcogido107=datos.DifPorRescateOEnajenacionCFIAcogido107;
            out.DifPorRescateOEnajenacionCFIAcogido107Actualizado=datos.DifPorRescateOEnajenacionCFIAcogido107Actualizado;
            out.MENORVALOR_I =datos.MENORVALOR_I; 
            out.MAYORVALOR_I =datos.MAYORVALOR_I;
            out.MENORVALOR_FM =datos.MENORVALOR_FM;
            out.MAYORVALOR_FM =datos.MAYORVALOR_FM;
            out.ValNominalInversionaFechaLiquidacion=datos.ValNominalInversionaFechaLiquidacion;
            out.CantCuotasCedidasPorLiquidacion=datos.CantCuotasCedidasPorLiquidacion;
            out.DiferenciaValorCuotasFechaLiquidacion=datos.DiferenciaValorCuotasFechaLiquidacion;
            out.DiferenciaValorCuotasFechaLiquidacionActualizado=datos.DiferenciaValorCuotasFechaLiquidacionActualizado;
            out.TributaSa=datos.TributaSa;
            out.DivRemDistAfectGCConIDPC=datos.DivRemDistAfectGCConIDPC;
            out.DivRemDistAfectGCConIDPCal31122016=datos.DivRemDistAfectGCConIDPCal31122016;
            out.DivRemDistAfectGCConIDPCVOL=datos.DivRemDistAfectGCConIDPCVOL;
            out.DivRemDistAfectGCSINDC=datos.DivRemDistAfectGCSINDC;
            out.DisRemRentasTribCProvRap_C96=datos.DisRemRentasTribCProvRap_C96;
            out.DisRemRentasTribCORP_C97=datos.DisRemRentasTribCORP_C97;
            out.DisRemRentasTribCExDD_C98=datos.DisRemRentasTribCExDD_C98;
            out.DisRemRentasTribCUtAFFut_C99=datos.DisRemRentasTribCUtAFFut_C99;
            out.DivRemDistNoRentaFUT=datos.DivRemDistNoRentaFUT;
            out.DisRemRentasExIGCAfecImptoAdi_C100=datos.DisRemRentasExIGCAfecImptoAdi_C100;
            out.CredImpGloCompNSRSD=datos.CredImpGloCompNSRSD;
            out.CredImpGloCompNSRCD=datos.CredImpGloCompNSRCD;
            out.CredIGCAcumD01012017RANSRD01012020SDD_C101=datos.CredIGCAcumD01012017RANSRD01012020SDD_C101;
            out.CredIGCAcumD01012017RANSRD01012020CDD_C102=datos.CredIGCAcumD01012017RANSRD01012020CDD_C102;
            out.CredImpGloCompSRSD =datos.CredImpGloCompSRSD; 
            out.CredImpGloCompSRCD=datos.CredImpGloCompSRCD;
            out.CredIGCAcumD01012017RXSRSDD_C103=datos.CredIGCAcumD01012017RXSRSDD_C103;
            out.CredIGCAcumD01012017RXSRCDD_C104=datos.CredIGCAcumD01012017RXSRCDD_C104;
            out.CredImpGloCompCTDCIF=datos.CredImpGloCompCTDCIF;
            out.CredImpGloCompCDD31122016=datos.CredImpGloCompCDD31122016;
            out.CredImpGloCompSDD31122016=datos.CredImpGloCompSDD31122016;
            out.CredIGCAcumH31122016RXSDD_C105=datos.CredIGCAcumH31122016RXSDD_C105;
            out.CredIGCAcumH31122016RXCDD_C106=datos.CredIGCAcumH31122016RXCDD_C106;
            out.CredImpGloCompCTDCIF31122016=datos.CredImpGloCompCTDCIF31122016;
            out.CredImpGloCompCITAExArt21 =datos.CredImpGloCompCITAExArt21; 
            out.DetBenNetoPerRutFdo_C107=datos.DetBenNetoPerRutFdo_C107;
            out.DetBenNetoPerPOS_C113=datos.DetBenNetoPerPOS_C113;
            out.DetBenNetoPerNEG_C114=datos.DetBenNetoPerNEG_C114;
            out.DividendosExentosIGCoAdicional=datos.DividendosExentosIGCoAdicional;
            out.DividendosExentosIGCoAdicionalActualizado=datos.DividendosExentosIGCoAdicionalActualizado;
            out.DividendosNoConstitutivosDeRenta=datos.DividendosNoConstitutivosDeRenta;
            out.DividendosNoConstitutivosDeRentaActualizado=datos.DividendosNoConstitutivosDeRentaActualizado;
            out.DivRemDistNoRentaFE=datos.DivRemDistNoRentaFE;
            out.DividendosAfectosImptoUnicoTasa10=datos.DividendosAfectosImptoUnicoTasa10;
            out.DividendosAfectosImptoUnicoTasa10Actualizado=datos.DividendosAfectosImptoUnicoTasa10Actualizado;
            out.DividendosAfectosImptoUnicoTasa4=datos.DividendosAfectosImptoUnicoTasa4;
            out.DividendosAfectosImptoUnicoTasa4Actualizado=datos.DividendosAfectosImptoUnicoTasa4Actualizado;
            out.DistribucionXDiminucionNoImputadaCapital=datos.DistribucionXDiminucionNoImputadaCapital;
            out.DevolucionesDeCapital=datos.DevolucionesDeCapital;
            out.DevolucionesDeCapitalActualizado =datos.DevolucionesDeCapitalActualizado; 
            out.MontoInversionenCuotasCFIAnterior93=datos.MontoInversionenCuotasCFIAnterior93;
            out.NumeroCertificado=datos.NumeroCertificado;
            out.FactorActualizacion=datos.FactorActualizacion;
            out.FechaOrigenInversion=datos.FechaOrigenInversion;
            out.SumaNumeroCuotas =datos.SumaNumeroCuotas; 
            out.SumaMontoActualizado=datos.SumaMontoActualizado;
            out.NombreFondo=datos.NombreFondo;
			
            out.RazonSocialCorredora=datos.RazonSocialCorredora;
			out.DireccionCorredora=datos.DireccionCorredora;
			out.DocumentoIdentidadCorredora=datos.DocumentoIdentidadCorredora;
			out.rut_representante=datos.rut_representante;
			out.nombre_representante=datos.nombre_representante;
			
			//out.Certificacion="N";
			out.EstadoCertificado="NGE";
			out.RutCertificado=datos.DocumentoIdentidadCliente;
			out.RunNemotecnico = datos.RunNemotecnico;
			
			o.add(out);
		}
		
		return o;
	}
	
	private List<Rut> obtenerGrupoCertificado(Connection conn,EntradaGenerarCertificado43Masivo entrada)  {
		logger.debug("INICIO::GenerarCertificado43Masivo.obtenerGrupoCertificado");
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
				
				logger.debug("INICIO::GenerarCertificado43Masivo.obtenerListaCertificados { SE EJECUTA EL DROP FINAL}");
			} catch (Exception er) { 
				er.printStackTrace();
				logger.debug("INICIO::GenerarCertificado43Masivo.obtenerListaCertificados { NO SE EJECUTA EL DROP FINAL}");
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::GenerarCertificado43Masivo.obtenerListaCertificados {}", fin - inicio);
		return rutLista;
	}

	public String obtenerLogoCorredoraB64(Connection conn, Long idEmpresa) throws SQLException {
		logger.debug("INICIO::GenerarCertificado43Masivo.obtenerLogoCorredora");
		long inicio = System.currentTimeMillis();

		HashMap<String, Object> params = new HashMap<>(2);
		params.put("IdEmpresa", idEmpresa);
		Imagen logo = new SQL().query(conn, SQL_SELECT_LOGO_CORREDORA, params, Imagen.class).stream().findFirst().orElse(new Imagen());
		String contenido = Base64.encodeBase64String(logo.Contenido);

		long fin = System.currentTimeMillis();
		logger.debug("FIN::GenerarCertificado43Masivo.obtenerLogoCorredora {}", fin - inicio);

		return contenido;
	}
	
	public String obtenerIdReporte(Connection conn, EntradaGenerarCertificado43Masivo entrada) throws SQLException {
		logger.debug("INICIO::GenerarCertificado43Masivo.obtenerLogoCorredora");
		long inicio = System.currentTimeMillis();
		HashMap<String, Object> params = new HashMap<>(2);
		params.put("IdEmpresa", entrada.p_IdEmpresa);
		params.put("IdTipoDocumento", entrada.p_IdTipoDocumento);
		params.put("VersionArchivo", entrada.p_VersionArchivo);
		List<Template> idLista = new SQL().query(conn, SQL_SELECT_ID_REPORTE, params, Template.class);
		//List<Template> idLista = new SQL().query(conn, SQL_SELECT_ID_REPORTE, new HashMap<>(2), Template.class);
		String IdTemplate =idLista.get(0).Id;

		long fin = System.currentTimeMillis();
		logger.debug("FIN::GenerarCertificado43Masivo.obtenerLogoCorredora {}", fin - inicio);

		return IdTemplate;
	}

	public String obtenerFirmaB64(Connection conn, Long idEmpresa) throws SQLException {
		logger.debug("INICIO::GenerarCertificado43Masivo.obtenerLogoCorredora");
		long inicio = System.currentTimeMillis();

		Imagen firma = new SQL().query(conn, SQL_SELECT_FIRMA_CORREDORA,new HashMap<String, Object>(), Imagen.class).stream().findFirst().orElse(new Imagen());
		String contenido = Base64.encodeBase64String(firma.Contenido);

		long fin = System.currentTimeMillis();
		logger.debug("FIN::GenerarCertificado43Masivo.obtenerLogoCorredora {}", fin - inicio);

		return contenido;
	}
	
	public RaizArchivo obtenerRaizArchivo(Connection conn, Long idEmpresa) throws SQLException {
		logger.debug("INICIO::GenerarCertificado43Masivo.obtenerRaizArchivo");
		long inicio = System.currentTimeMillis();

		HashMap<String, Object> params = new HashMap<>(2);
		params.put("IdEmpresa", idEmpresa);

		RaizArchivo raiz = new SQL().query(conn, SQL_SELECT_RAIZ_ARCHIVO, params, RaizArchivo.class).stream().findFirst().orElse(new RaizArchivo());

		long fin = System.currentTimeMillis();
		logger.debug("INICIO::GenerarCertificado43Masivo.obtenerRaizArchivo {}", fin - inicio);

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
			EntradaGenerarCertificado43Masivo entrada = new EntradaGenerarCertificado43Masivo();
			Certificado Certificado = new Certificado();
			entrada.p_IdEmpresa = 1L;
			entrada.p_IdUsuario = 21L;
			entrada.p_IdModulo = "NOR";
			entrada.p_IdProceso=559065L;
	
			Rut Rut = new Rut();
			Rut.Rut = "10215782-6";
			
			entrada.p_listaRut.add(Rut);
			entrada.p_VersionArchivo="2023";
			entrada.p_IdTipoDocumento="CERT43";
			
			entrada.p_DataSource = PlataformaUtils.getDESADatasource();

			SalidaGenerarCertificado43Masivo salida = new GenerarCertificado43Masivo().logica(conn, entrada);
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
			EntradaGenerarCertificado43Masivo entrada = new EntradaGenerarCertificado43Masivo();
			Certificado Certificado = new Certificado();
			entrada.p_IdEmpresa = 1L;
			entrada.p_IdUsuario = 21L;
			entrada.p_IdModulo = "NOR";
			entrada.p_IdProceso=559065L;
	
			Rut Rut = new Rut();
			//Rut.Rut = "1-9";

			entrada.p_listaRut.add(Rut);
			entrada.p_VersionArchivo="2021";
			entrada.p_IdTipoDocumento="CERT43";
			entrada.p_DataSource = PlataformaUtils.getMASTER1Datasource();

			SalidaGenerarCertificado43Masivo salida = new GenerarCertificado43Masivo().logica(conn, entrada);
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
