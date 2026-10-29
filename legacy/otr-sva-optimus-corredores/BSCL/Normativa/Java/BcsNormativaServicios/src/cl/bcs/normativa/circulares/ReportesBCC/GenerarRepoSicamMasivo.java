package cl.bcs.normativa.circulares.ReportesBCC;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.StringUtils;
import cl.bcs.utils.Utils;

@SuppressWarnings({})
public class GenerarRepoSicamMasivo implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(GenerarRepoSicamMasivo.class);	
	
	private final String SQL_SELECT_REPO3P1  = loadResourceToString("cl/bcs/normativa/circulares/ReportesBCC/Reportes3p1/sql/ObtenerRegistrosREPO3P1_Masivo.sql");
	private final String SQL_SELECT_REPO2P1    = loadResourceToString("cl/bcs/normativa/circulares/ReportesBCC/Reportes2p1/sql/ObtenerRegistrosREPO2P1_Masivo.sql");
	private final String SQL_SELECT_REPO2P2    = loadResourceToString("cl/bcs/normativa/circulares/ReportesBCC/Reportes2p2/sql/ObtenerRegistrosREPO2P2_Masivo.sql");	
	private final String SQL_SELECT_RAIZ_ARCHIVO    = loadResourceToString("cl/bcs/normativa/declaraciones/sql/GenerarCertificadoMasivo_SelectRaizArchivo.sql");
	private final String SQL_UPDATE_NOMBRE_SALIDA_PROGRAMA_TRIBUTARIO = "UPDATE \"BSCL.Normativa.BD::TRI.ResumenGeneracion\" SET \"NombreArchivo\"=:RutaArchivo, \"Glosa\"=:Glosa, \"TotalRegistrosGenerados\"=:TotalRegistrosGenerados WHERE \"IdProceso\"= :IdProceso";
	private final Log log = new Log("NOR.GenerarRepoSicamMasivo");
	
	public long contadorArchivos=0;

	public static class EntradaGenerarRepoSicamMasivo extends BcsPojo{
		public Long 	p_IdEmpresa;
		public Long 	p_IdUsuario;
		public String 	p_IdModulo;	
		public String 	p_IdEsquema;
		public String 	p_Version;
		public String 	p_FechaInicio;
		public String 	p_FechaFin;
		public Long		p_IdProceso;
	} 
	
	public static class SalidaGenerarRepoSicamMasivo extends BcsPojo {
		public Long P_OUT_ERRORESCONTADOR;
		public List<PlataformaUtils.MensajeSalida> P_OUT_MENSAJES;
		public String p_out_RutaArchivo;
		public String p_out_NombreArchivo;
		public Long p_out_contadorArchivos;

		public SalidaGenerarRepoSicamMasivo() {
			P_OUT_ERRORESCONTADOR = 0L;
			P_OUT_MENSAJES = new ArrayList<>();
		}
	}
		
	public static class ResumenGeneracion extends BcsPojo{
		public Long 	IdProceso;
		public String 	RutaArchivo;
		public String 	Glosa;
		public Long 	TotalRegistrosGenerados;
		
		public ResumenGeneracion(Long IdProceso, String RutaArchivo, String Glosa,Long TotalRegistrosGenerados)
		{
			this.IdProceso = IdProceso;
			this.RutaArchivo = RutaArchivo;		
			this.Glosa = Glosa;
			this.TotalRegistrosGenerados = TotalRegistrosGenerados;
		}
	}

	
	// public static class Certificado extends BcsPojo{
	// 	public String IdTipoDocumento;		
	// 	public String VersionArchivo;
	// }
	
	public static class RaizArchivo extends BcsPojo {
		public String RutaArchivo;
		public String RutaNOR;
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

	public enum TipoFormato {
		Normal(15, 5),
		Especial(5, 2),
		Normal2P2(20, 2),
		Especial2P2(10, 5);

		private final int entero;
		private final int decimal;

		TipoFormato(int entero, int decimal) {
			this.entero = entero;
			this.decimal = decimal;
		}

		public int getEntero() { return entero; }
		public int getDecimal() { return decimal; }
	}

	
   public static class Repo3P1 extends BcsPojo {
    	public Long				ID;
    	public String 			TipoRegistro;
    	public String 			RUTReportante;
    	public String 			FechaOperacion;
    	public String			HoraOperacion;
    	public String			TipoOperacion;
    	public String			EventoReporte;
    	public String			Identificador;
    	public String			RUTContraparte;
    	public String			LEIContraparte;
    	public String			NombreContraparte;
    	public String			PlataformaNegociacion;
    	public BigDecimal		MontoMonedaComprada;
    	public BigDecimal		MontoMonedaVendida;
    	public BigDecimal		TipoCambio;
    	
    	public String toString() {
	        return Utils.printJson(this, false);
	    }
    }
   
   public static class Repo2P1 extends BcsPojo {
   	public Long				ID;
   	public String 			TipoRegistro;
   	public String 			EventoReporte;
   	public String 			TipoOperacion;
   	public String			FechaNegociacion;
   	public String			HoraNegociacion;
   	public String			FechaValutaMonedaRecibida;
   	public String			FechaValutaMonedaEntregada;
   	public String			Identificador;
   	public String			RUTContraparte1;
   	public String			LEIContraparte1;
   	public String			NombreContraparte1;
   	public String			RUTContraparte2;
   	public String			LEIContraparte2;
   	public String			NombreContraparte2;
   	public String			PaisContraparte2;
   	public String			RUTIntermediario;
   	public String			PlataformaNegociacion;
   	public String 			CodigoOperacionCambio;
   	public String 			PaisMonedaExtranjera;
   	public String			MonedaCompradaRecibida;
   	public BigDecimal		MontoMonedaCompradaRecibida;
   	public String			MonedaVendidaEntregada;
   	public BigDecimal		MontoMonedaVendidaEntregada;
   	public String			ConvencionTipoCambio;
   	public BigDecimal		TipoCambio;
   	public String			AfectaDerivados;
   	public String			IdentificadorContratoDerivados;
   	public String			FechaSuscripcionContrato;
   	public String			HoraSuscripcionContrato;
   	public String			OperacionEmpresaRelacionada;
   	public String			NumeroInscripcionObligacionExterior;
   	public String			NombreFinancistaAporante;
   	public String			LEIFinancistaAporante;
   	public String			NombreReceptorInversion;
   	public String			LEIReceptorInversion;
   	public String			ModalidadInversionCustodio;
   	
   	public String toString() {
	        return Utils.printJson(this, false);
	    }
   }

   public static class Repo2P2 extends BcsPojo {
    	public Long				ID;
    	public String 			TipoRegistro;
    	public String 			RUTEntidadMCF;
    	public String 			Fecha;
    	public BigDecimal		SaldoCuentaCambioConversion;
    	public BigDecimal		SaldoMonedaOriginal;
		public String 			MonedaSaldo;
    	public BigDecimal		Paridad;
    	
    	public String toString() {
	        return Utils.printJson(this, false);
	    }
    }
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {				
		Gson gson = Utils.getGsonBuilder();
		EntradaGenerarRepoSicamMasivo in=gson.fromJson(jin.toString() ,EntradaGenerarRepoSicamMasivo.class);
		SalidaGenerarRepoSicamMasivo out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaGenerarRepoSicamMasivo logica(Connection conn, EntradaGenerarRepoSicamMasivo entrada) throws Exception {
		logger.info("INICIO::GenerarRepoSicamMasivo");
		long inicio = System.currentTimeMillis();
		log.write( "*GenerarRepoSicamMasivo* INICIO");
		new SQL();
		SalidaGenerarRepoSicamMasivo salida = new SalidaGenerarRepoSicamMasivo();
		
		log.write( "*GenerarRepoSicamMasivo* obtenerRaizArchivo");
		RaizArchivo raizArchivo = obtenerRaizArchivo(conn, entrada.p_IdEmpresa);
		if (StringUtils.isNullOrBlank(raizArchivo.RutaArchivo) || StringUtils.isNullOrBlank(raizArchivo.RutaNOR)) {
			salida.P_OUT_ERRORESCONTADOR++;
			salida.P_OUT_MENSAJES.add(new PlataformaUtils.MensajeSalida(0L, PlataformaUtils.MensajeSalida.MENSAJE_ERROR, "FALTA_CONFIGURACION_RUTA_ARCHIVO", new HashMap<>(), conn, entrada.p_IdEmpresa));
			return salida;
		}
		log.write( "*GenerarRepoSicamMasivo* RutaArchivo" + raizArchivo.RutaArchivo);
		// Para pruebas locales.
		//final String rutaAbsolutaBase = System.getProperty("user.home") + File.separator + "Desktop" + File.separator + "Certificado31" + File.separator;
		final String rutaAbsolutaBase = raizArchivo.RutaArchivo;
		final String rutaRelativaBase = raizArchivo.RutaNOR;
		final String nombreBaseArchivo = entrada.p_FechaInicio +"_" + entrada.p_FechaFin  +"_" + entrada.p_IdEsquema +"_" + entrada.p_Version.replace("/", "-");
		String nombreBaseZip = entrada.p_FechaInicio +"_" + entrada.p_FechaFin  +"_" + entrada.p_IdEsquema +"_" + entrada.p_Version.replace("/", "-");
		final String timestampSistema = Fecha.toFormat(FechaDB.ObtenerFechaHoraSistemaTs(conn, entrada.p_IdEmpresa), "yyyyMMdd_HHmmss");
		
		//buscar datos 		
		log.write( "*GenerarRepoSicamMasivo* obtenerListaREPO");
		log.write( "*GenerarRepoSicamMasivo* p_IdEsquema" , entrada.p_IdEsquema);
		log.write( "*GenerarRepoSicamMasivo* p_IdEsquema" , entrada.p_Version);
		log.write( "*GenerarRepoSicamMasivo* p_IdEsquema" , entrada.p_FechaInicio);
		log.write( "*GenerarRepoSicamMasivo* p_IdEsquema" , entrada.p_FechaFin);
		if(entrada.p_IdEsquema.equals("REPO2P1")) {
			List<String> listaREPO = obtenerListaREPO (conn , entrada);
			if(listaREPO.isEmpty()) {
				salida.P_OUT_MENSAJES.add(new MensajeSalida( 0L, MensajeSalida.MENSAJE_ERROR, "UI_SIN_DATOS", "No se encontraron datos consultados"));
				salida.P_OUT_ERRORESCONTADOR = Long.valueOf(salida.P_OUT_MENSAJES.size());
				return salida;
			};
			log.write( "*GenerarRepoSicamMasivo* generarREPO");
			log.write( "*GenerarRepoSicamMasivo* comprimirArchivos");
			String rutaArchivosSinComprimir = generarArchivoREPO(conn, rutaAbsolutaBase, nombreBaseArchivo, timestampSistema, listaREPO, entrada);
			salida.p_out_NombreArchivo = rutaArchivosSinComprimir;
			salida.p_out_RutaArchivo= rutaRelativaBase + salida.p_out_NombreArchivo;
			salida.p_out_contadorArchivos=contadorArchivos;
			long fin = System.currentTimeMillis();
			logger.info("FIN::GenerarRepoSicamMasivo {}", fin - inicio);
			log.write( "*GenerarRepoSicamMasivo* FIN");
			return salida;
		}else {
			List<String> listaREPO = obtenerListaREPO (conn , entrada);
			if(listaREPO.isEmpty()) {
				salida.P_OUT_MENSAJES.add(new MensajeSalida( 0L, MensajeSalida.MENSAJE_ERROR, "UI_SIN_DATOS", "No se encontraron datos consultados"));
				salida.P_OUT_ERRORESCONTADOR = Long.valueOf(salida.P_OUT_MENSAJES.size());
				return salida;
			};
			log.write( "*GenerarRepoSicamMasivo* generarREPO");
			log.write( "*GenerarRepoSicamMasivo* comprimirArchivos");
			String rutaArchivosSinComprimir = generarArchivoREPO(conn, rutaAbsolutaBase, nombreBaseArchivo, timestampSistema, listaREPO, entrada);
			salida.p_out_NombreArchivo = rutaArchivosSinComprimir;
			salida.p_out_RutaArchivo= rutaRelativaBase + salida.p_out_NombreArchivo;
			salida.p_out_contadorArchivos=contadorArchivos;
			long fin = System.currentTimeMillis();
			logger.info("FIN::GenerarRepoSicamMasivo {}", fin - inicio);
			log.write( "*GenerarRepoSicamMasivo* FIN");
			return salida;
		}
	}
	
	private String comprimirArchivos(String timestampSistema, String rutaAbsolutaBase, String nombreBaseZip, List<String> rutasArchivos) throws Exception {
		logger.debug("FIN::GenerarRepoSicamMasivo.comprimirArchivos");
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
		logger.debug("FIN::GenerarRepoSicamMasivo.comprimirArchivos", nombreZip);
		logger.debug("FIN::GenerarRepoSicamMasivo.comprimirArchivos {}", fin - inicio);
		return nombreZip;
	}


	private String generarArchivoREPO(Connection conn, String rutaAbsoluta,String nombreBaseArchivo, String timestampSistema, List<String> dataReporte, EntradaGenerarRepoSicamMasivo entrada) throws Exception {
		logger.debug("INICIO::GenerarCertificado21Masivo.generarArchivosInformes");
		long inicio = System.currentTimeMillis();		
		SQL sql = new SQL();
		String fileName="";
		fileName =nombreBaseArchivo +"_"+timestampSistema+".csv";
		String rutaBaseReporte = rutaAbsoluta + fileName;
		String reporteGenerado = generarReporte(rutaBaseReporte, dataReporte);
		List<ResumenGeneracion> programa = new ArrayList<ResumenGeneracion>();
		programa.add(new ResumenGeneracion(entrada.p_IdProceso, fileName,"Finalizado",contadorArchivos));
		sql.batch(conn, SQL_UPDATE_NOMBRE_SALIDA_PROGRAMA_TRIBUTARIO, programa,ResumenGeneracion.class);
		String Est2="< Archivo generado con ... >";
		long fin = System.currentTimeMillis();
		logger.debug("FIN::GenerarCertificado21Masivo.obtenerTraspasoReporte {}", fin - inicio);
		return fileName;
	}
	

	private List<String> obtenerListaREPO(Connection conn,EntradaGenerarRepoSicamMasivo entrada)  {
		logger.debug("INICIO::GenerarRepoSicamMasivo.obtenerListaReporte");
		long inicio = System.currentTimeMillis();
		List<String> listaPlanaRepo =new ArrayList<String>();			
		
		SQL sql = new SQL();		
		
		try {
			HashMap<String, Object> params = new HashMap<>(2);
			params.put("p_Esquema", entrada.p_IdEsquema);
			params.put("p_Version", entrada.p_Version);
			params.put("p_FechaInicio", entrada.p_FechaInicio);
			params.put("p_FechaFin", entrada.p_FechaFin);
			List<ResumenGeneracion> programa = new ArrayList<ResumenGeneracion>();
			if(entrada.p_IdEsquema.equals("REPO2P1")) {
				List<Repo2P1> reporte2P1 = new ArrayList<Repo2P1>();
				reporte2P1 = new SQL().query(conn, SQL_SELECT_REPO2P1, params, Repo2P1.class);
				if(!reporte2P1.isEmpty()) {
					for(Repo2P1 r : reporte2P1) {
						contadorArchivos ++;
						String fechaNegociacion = (!"0001-01-01".equals(r.FechaNegociacion)) ? r.FechaNegociacion + "T" + r.HoraNegociacion : "";
						String fechaSuscripcionContrato = (!"0001-01-01".equals(r.FechaSuscripcionContrato)) ? r.FechaSuscripcionContrato + "T" + r.HoraSuscripcionContrato : "";
						StringBuilder sb = new StringBuilder();
						sb.append(r.TipoRegistro != null ? r.TipoRegistro : "").append(";");
						sb.append(r.EventoReporte != null ? r.EventoReporte : "").append(";");
						sb.append(r.TipoOperacion != null ? r.TipoOperacion : "").append(";");
						sb.append(fechaNegociacion != null ? fechaNegociacion : "").append(";");
						sb.append(!"0001-01-01".equals(r.FechaValutaMonedaRecibida) ? r.FechaValutaMonedaRecibida : "").append(";");
						sb.append(!"0001-01-01".equals(r.FechaValutaMonedaEntregada) ? r.FechaValutaMonedaEntregada : "").append(";");
						sb.append(r.Identificador != null ? r.Identificador : "").append(";");
						sb.append(r.RUTContraparte1 != null ? r.RUTContraparte1.replace("-", "") : "").append(";");
						sb.append(r.LEIContraparte1 != null ? r.LEIContraparte1 : "").append(";");
						sb.append(r.NombreContraparte1 != null ? r.NombreContraparte1 : "").append(";");
						sb.append(r.RUTContraparte2 != null ? r.RUTContraparte2.replace("-", "") : "").append(";");
						sb.append(r.LEIContraparte2 != null ? r.LEIContraparte2 : "").append(";");
						sb.append(r.NombreContraparte2 != null ? r.NombreContraparte2 : "").append(";");
						sb.append(r.PaisContraparte2 != null ? r.PaisContraparte2 : "").append(";");
						sb.append(r.RUTIntermediario != null ? r.RUTIntermediario.replace("-", "") : "").append(";");
						sb.append(r.PlataformaNegociacion != null ? r.PlataformaNegociacion : "").append(";");
						sb.append(r.CodigoOperacionCambio != null ? r.CodigoOperacionCambio : "").append(";");
						sb.append(r.PaisMonedaExtranjera != null ? r.PaisMonedaExtranjera : "").append(";");
						sb.append(r.MonedaCompradaRecibida != null ? r.MonedaCompradaRecibida : "").append(";");
						sb.append(r.MontoMonedaCompradaRecibida != null ?  formatearMontos(r.MontoMonedaCompradaRecibida, TipoFormato.Normal) : "000000000000000.00000").append(";");
						sb.append(r.MonedaVendidaEntregada != null ? r.MonedaVendidaEntregada : "").append(";");
						sb.append(r.MontoMonedaVendidaEntregada != null ?  formatearMontos(r.MontoMonedaVendidaEntregada, TipoFormato.Normal) : "000000000000000.00000").append(";");
						sb.append(r.ConvencionTipoCambio != null ? r.ConvencionTipoCambio : "").append(";");
						sb.append(r.TipoCambio != null ?  formatearMontos(r.TipoCambio, TipoFormato.Normal) : "000000000000000.00000").append(";");
						sb.append(r.AfectaDerivados != null ? r.AfectaDerivados : "").append(";");
						sb.append(r.IdentificadorContratoDerivados != null ? r.IdentificadorContratoDerivados : "").append(";");
						sb.append(fechaSuscripcionContrato != null ? fechaSuscripcionContrato : "").append(";");
						sb.append(r.OperacionEmpresaRelacionada != null ? r.OperacionEmpresaRelacionada : "").append(";");
						sb.append(r.NumeroInscripcionObligacionExterior != null ? r.NumeroInscripcionObligacionExterior : "").append(";");
						sb.append(r.NombreFinancistaAporante != null ? r.NombreFinancistaAporante : "").append(";");
						sb.append(r.LEIFinancistaAporante != null ? r.LEIFinancistaAporante : "").append(";");
						sb.append(r.NombreReceptorInversion != null ? r.NombreReceptorInversion : "").append(";");
						sb.append(r.LEIReceptorInversion != null ? r.LEIReceptorInversion : "").append(";");
						sb.append(r.ModalidadInversionCustodio != null ? r.ModalidadInversionCustodio : "");
						listaPlanaRepo.add(sb.toString());
					}
				}else {
					
				}
				
			}
			else if(entrada.p_IdEsquema.equals("REPO2P2")){
				List<Repo2P2> reporte2P2 = new ArrayList<Repo2P2>();
				reporte2P2 = new SQL().query(conn, SQL_SELECT_REPO2P2, params, Repo2P2.class);
				if(!reporte2P2.isEmpty()){
					for(Repo2P2 r : reporte2P2){
						contadorArchivos ++;
						StringBuilder sb = new StringBuilder();
	    	            sb.append(r.TipoRegistro != null ? r.TipoRegistro : "").append(";");
	    	            sb.append(r.RUTEntidadMCF != null ? r.RUTEntidadMCF.replace("-", "") : "").append(";");
	    	            sb.append(r.Fecha != null ? r.Fecha : "").append(";");
						sb.append(r.SaldoCuentaCambioConversion != null ? formatearMontos(r.SaldoCuentaCambioConversion, TipoFormato.Normal2P2) : "00000000000000000000.00").append(";");
	    	            sb.append(r.SaldoMonedaOriginal != null ? formatearMontos(r.SaldoMonedaOriginal, TipoFormato.Normal2P2) : "00000000000000000000.00").append(";");
	    	            sb.append(r.MonedaSaldo != null ? r.MonedaSaldo : "").append(";");
	    	            sb.append(r.Paridad != null ? formatearMontos(r.Paridad, TipoFormato.Especial2P2) : "0000000000.00000");
	    	            listaPlanaRepo.add(sb.toString());
					}
				}
			}
			else if(entrada.p_IdEsquema.equals("REPO3P1")){
				List<Repo3P1> reporte3P1 = new ArrayList<Repo3P1>();
				reporte3P1 = new SQL().query(conn, SQL_SELECT_REPO3P1, params, Repo3P1.class);
				if(!reporte3P1.isEmpty()) {
					for(Repo3P1 r : reporte3P1) {
						contadorArchivos ++;
						String fechaHoraOperacion = (r.FechaOperacion != null && r.HoraOperacion != null) ? r.FechaOperacion + "T" + r.HoraOperacion : "";
	    	            StringBuilder sb = new StringBuilder();
	    	            sb.append(r.TipoRegistro != null ? r.TipoRegistro : "").append(";");
	    	            sb.append(r.RUTReportante != null ? r.RUTReportante.replace("-", "") : "").append(";");
	    	            sb.append(fechaHoraOperacion != null ? fechaHoraOperacion : "").append(";");
	    	            sb.append(r.TipoOperacion != null ? r.TipoOperacion : "").append(";");
	    	            sb.append(r.EventoReporte != null ? r.EventoReporte : "").append(";");
	    	            sb.append(r.Identificador != null ? r.Identificador : "").append(";");
	    	            sb.append(r.RUTContraparte != null ? r.RUTContraparte.replace("-", "") : "").append(";");
	    	            sb.append(r.LEIContraparte != null ? r.LEIContraparte : "").append(";");
	    	            sb.append(r.NombreContraparte != null ? r.NombreContraparte : "").append(";");
	    	            sb.append(r.PlataformaNegociacion != null ? r.PlataformaNegociacion : "").append(";");
	    	            sb.append(r.MontoMonedaComprada != null ? formatearMontos(r.MontoMonedaComprada, TipoFormato.Normal) : "000000000000000.00000").append(";");
	    	            sb.append(r.MontoMonedaVendida != null ? formatearMontos(r.MontoMonedaVendida, TipoFormato.Normal) : "000000000000000.00000").append(";");
	    	            sb.append(r.TipoCambio != null ? formatearMontos(r.TipoCambio, TipoFormato.Especial) : "00000.00");
	    	            listaPlanaRepo.add(sb.toString());
	    	        }
				}
			}				
			//Fin reporte JAVA	
			String Estado= "Archivo Generado con: "+contadorArchivos+" registros";
			programa.add(new ResumenGeneracion(entrada.p_IdProceso, "",Estado,contadorArchivos));
			sql.batch(conn, SQL_UPDATE_NOMBRE_SALIDA_PROGRAMA_TRIBUTARIO, programa,ResumenGeneracion.class);
		}catch (Exception e) {
			e.printStackTrace();
	
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::GenerarRepoSicamMasivo.obtenerListaREPO {}", fin - inicio);
		logger.debug("FIN::GenerarRepoSicamMasivo.obtenerListaREPO cantidad archivos{}", contadorArchivos);
		return listaPlanaRepo;
	}
	
	private String generarReporte(String rutaBase, List<String> dataReporte) throws Exception {
		logger.trace("INICIO::GenerarRepoSicamMasivo.generarReporte");
		long inicio = System.currentTimeMillis();
		String rutaReporte = rutaBase;
		long fin = System.currentTimeMillis();
		File archivoReporte = new File(rutaReporte);
	    try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(archivoReporte), StandardCharsets.UTF_8))) {
	        for (String lineaReporte : dataReporte) {
	            writer.write(lineaReporte);
	            writer.newLine();
	        }
	    }
		logger.trace("FIN::GenerarRepoSicamMasivo.generarReporte {}", fin - inicio);
		return rutaReporte;
	}
		
	public RaizArchivo obtenerRaizArchivo(Connection conn, Long idEmpresa) throws SQLException {
		logger.debug("INICIO::GenerarRepoSicamMasivo.obtenerRaizArchivo");
		long inicio = System.currentTimeMillis();

		HashMap<String, Object> params = new HashMap<>(2);
		params.put("IdEmpresa", idEmpresa);

		RaizArchivo raiz = new SQL().query(conn, SQL_SELECT_RAIZ_ARCHIVO, params, RaizArchivo.class).stream().findFirst().orElse(new RaizArchivo());

		long fin = System.currentTimeMillis();
		logger.debug("INICIO::GenerarRepoSicamMasivo.obtenerRaizArchivo {}", fin - inicio);

		return raiz;
	}
	
	public static String formatearMontos(BigDecimal monto, TipoFormato formato) {
		if (monto == null) return "0";

		monto = monto.setScale(formato.getDecimal(), RoundingMode.HALF_UP);
		String[] partes = monto.toPlainString().split("\\.");
		String parteEntera = partes[0];
		String parteDecimal = partes.length > 1 ? partes[1] : "";

		if (parteEntera.length() > formato.getEntero()) {
			parteEntera = parteEntera.substring(0, formato.getEntero());
		}
		if (parteDecimal.length() > formato.getDecimal()) {
			parteDecimal = parteDecimal.substring(0, formato.getDecimal());
		}
		return parteDecimal.isEmpty() ? parteEntera : parteEntera + "." + parteDecimal;
	}
	
	public static void main(String[] args) throws Exception, SQLException {
		Connection con = null;
		Gson gson = new Gson();

		try {
			System.out.println("== BEGIN ==");
			//(con = PlataformaUtils.getPRUEBAConnection();
			con = PlataformaUtils.getPropertiesConnection("desa01", "C:\\GIT\\OPTIMUS\\optimus-corredores\\connections.properties");
		
			String json1 = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":1,\"p_IdEsquema\":\"REPO2P2\",\"p_Version\":\"2025/08\",\"p_Estado\":\"NEN\",\"p_FechaReporte\":\"2024-06-14\"}";
			
			EntradaGenerarRepoSicamMasivo objIn = gson.fromJson(json1, EntradaGenerarRepoSicamMasivo.class);

			GenerarRepoSicamMasivo demo = new GenerarRepoSicamMasivo();

			SalidaGenerarRepoSicamMasivo ObjOut = demo.logica(con, objIn);

			System.out.println(ObjOut.toString());
			//con.commit();
			con.rollback();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			con.close();
		}
		System.out.println("== END ==");

	};

}