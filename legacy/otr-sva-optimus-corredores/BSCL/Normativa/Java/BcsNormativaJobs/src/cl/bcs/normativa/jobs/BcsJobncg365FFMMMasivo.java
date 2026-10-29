package cl.bcs.normativa.jobs;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;

import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;
import com.google.gson.JsonObject;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.sql.DataSource;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import cl.bcs.normativa.dal.LocalizadorArchivo;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.report.api.ReportDocument;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.ObtenerDiaAnterior;
import cl.bcs.plataforma.pla.ObtenerDiaAnterior.InObtenerDiaAnterior;
import cl.bcs.plataforma.pla.ObtenerDiaAnterior.OutObtenerDiaAnterior;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Utils;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;

public final class BcsJobncg365FFMMMasivo extends BcsJob implements JavaCall {
	private final Log log = new Log("NORMATIVA_EJECUCION_ORDENES_MASIVO_PDF"); 	
	private static final Logger LOGGER = Logger.getLogger( BcsJobncg365FFMMMasivo.class.getName());
	AtomicBoolean running=new AtomicBoolean(false); 
	
	String SQL_INSERT_ARCHIVO_NCG365 = loadResourceToString("cl/bcs/normativa/jobs/sql/BcsJobncg365FFMMMasivo_InsertArchivoNCG365.sql");
	String SQL_CONSULTA_ES_PAUTA = loadResourceToString("cl/bcs/normativa/jobs/sql/BcsJobncg365FFMMMasivo_SelectSolicitudEsPauta.sql");
		
	
	@Override
	public boolean transaction(Connection con) throws InterruptedException, Throwable {
		return logica(con, false);
	}
	
    public boolean logica(Connection dbcon,boolean debug) throws Exception {
		System.out.println( ":GENERAR NCG 365 MASIVO:  ****** INICIO  *******");
		log.write("  ****** INICIO  *******");
		//running.set(true);		
		DataSource datasource = null;
		CallableStatement spexe = null;
		CallableStatement spRem = null;
		CallableStatement spEmisores = null;
		Long idEmpresa = 0L;
		Long secuenciaSolicitud =0L;
		String FTER = "0001-01-01";
		Long idDocumento =0L;
		String ruta = "";
		Long diasAtrasoNotificacionNCG365 = 0L;
		
		//Parametros
		if(debug) {
			idEmpresa =1L;
			secuenciaSolicitud = 3747L;
			FTER = "2024-10-02";
			idDocumento = 119L;
			datasource = PlataformaUtils.getQADatasource();
			ruta = System.getProperty("user.home") + File.separator + "Desktop" + File.separator + "NORMATIVA" + File.separator;
			diasAtrasoNotificacionNCG365 = obtenerParametrOdiasAtrasoNotificacionNCG365(dbcon, idEmpresa);
		} else {
			idEmpresa = Long.parseLong(this.getParam("IdEmpresa"));
			secuenciaSolicitud = Long.parseLong(this.getParam("IdSolicitudCircular"));
			FTER = this.getParam("FTER");
			idDocumento = Long.parseLong(this.getParam("idDocumento"));
			datasource = super.getDataSource();
			//obtener la ruta de archivos para normativo
			ruta = LocalizadorArchivo.buscarRutaRaiz(dbcon, Utils.getSchema(), idEmpresa)+ File.separator;
			diasAtrasoNotificacionNCG365 = obtenerParametrOdiasAtrasoNotificacionNCG365(dbcon, idEmpresa);
		}
		
		List<String> archivos = new ArrayList<String>();//Lista de archivos para ZIP
		String EsPauta = "N";
		
		SQL sql = new SQL();
		List<ArchivoNCG365> archivosInsert = new ArrayList<ArchivoNCG365>(); //Lista de elementos para insertar en BSCL.Normativa.BD::CIR.ArchivoNCG365
		
		HashMap<String, Object> consultaPautaMap = new HashMap<String, Object>();
		consultaPautaMap.put("IdSolicitudCircular", secuenciaSolicitud);
		List<ConsultaEsPauta> consultaEsPauta = sql.query(dbcon, SQL_CONSULTA_ES_PAUTA, consultaPautaMap, ConsultaEsPauta.class);
		if(!consultaEsPauta.isEmpty()) {
			EsPauta = consultaEsPauta.get(0).EsPauta;
		}
	
		
		
		//obtener los datos para la generacion de cada certificado
		
		final String selectCertificados = "SELECT * FROM (SELECT SOLICITUD.\"IdCliente\", SOLICITUD.\"DocumentoIdentidad\""
				+ "	FROM  \"BSCL\".\"BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudAporteRescate\" AS SOLICITUD"
				+ "	WHERE  SOLICITUD.\"IdEmpresa\"= ? AND TO_DATE(SOLICITUD.\"FechaCertificacion\") =TO_DATE(?) AND SOLICITUD.\"EstadoSolicitud\"='CRT' AND SOLICITUD.\"IdTipoMovimientoFondoMutuo\" IN ('RESTRASP', 'APONORM', 'APOREINV', 'RESNORM', 'RESREINV', 'APOTRASP', 'APOCES', 'RESCCES')" 
				+ "	GROUP BY SOLICITUD.\"IdCliente\", SOLICITUD.\"DocumentoIdentidad\")"
				+ "	CROSS JOIN (SELECT TOP 1 ARC.* FROM  \"BSCL\".\"BSCL.Normativa.BD.Vistas::DOC.ConsultarDocumento\" AS DOC INNER JOIN \"BSCL\".\"BSCL.Normativa.BD.Vistas::NOR.ConsultarArchivoNormativa\" AS ARC"
				+ "	ON (DOC.\"IdTipoDocumento\"=ARC.\"IdTipoDocumento\" AND DOC.\"Esquema\" =ARC.\"Esquema\"  AND DOC.\"Version\"=ARC.\"Version\" )  WHERE DOC.\"IdDocumento\"=?)AS B";
	
		  
		//Servicio del certificado
		final String callSpexe = "CALL \"BSCL\".\"BSCL.Normativa.CrystalReports.Servicios::NCG365FFMM\"(?,?,?,?)";
		final String callSpRem = "CALL \"BSCL\".\"BSCL.Normativa.CrystalReports.Servicios::NCG365FFMM\"(?,?,?,?)";
		final String callSpEmisores = "CALL \"BSCL\".\"BSCL.Normativa.CrystalReports.Servicios::NCG365FFMM_Emisores\"(?)";
		
		try (PreparedStatement pstmt = dbcon.prepareStatement(selectCertificados)) {
			pstmt.setLong(1, idEmpresa);
			String fechaFinal = "";
			if("S".equals(EsPauta) ) {
				Long idPaisSistema = ConfiguracionSistema.getParametroConfiguracionLong(dbcon, idEmpresa,
						"PLATAFORMA", "PLA.PaisSistema");
				
				ObtenerDiaAnterior obtenerDiaAnt = new ObtenerDiaAnterior();
				InObtenerDiaAnterior inObtenerDiaAnt = new InObtenerDiaAnterior();
				InObtenerDiaAnterior.FechasIn fechasIn = new InObtenerDiaAnterior.FechasIn();
				fechasIn.fechaInicial = FTER;
				fechasIn.numeroDias = diasAtrasoNotificacionNCG365;
				inObtenerDiaAnt.fechas.add(fechasIn);
				inObtenerDiaAnt.idPais = idPaisSistema;
				inObtenerDiaAnt.diaHabil = "S";
				inObtenerDiaAnt.soloCaeDiaHabil = "S";
				
				OutObtenerDiaAnterior outObtenerDiaAnteriorHabil = obtenerDiaAnt.logica(dbcon, inObtenerDiaAnt);
				String fechaAnteriorHabil = outObtenerDiaAnteriorHabil.fechas.get(0).fechaCalculada;

				pstmt.setString(2, fechaAnteriorHabil);
				fechaFinal = fechaAnteriorHabil;
			}else {
				pstmt.setString(2, FTER);
				fechaFinal = FTER;
			}
			pstmt.setLong(3, idDocumento);
			
			System.out.println(System.getProperty("java.version"));
			log.write("**  VERSION JAVA: " + System.getProperty("java.version"));
			
			try (ResultSet rs = pstmt.executeQuery()) {
				//Nombre y ruta ZIP
				String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(dbcon, idEmpresa,"PLATAFORMA", "PLA.FechaSistema");
				String fechaFormat = new SimpleDateFormat("yyyyMMdd").format(Fecha.toDate(fechaSistema));
				Timestamp fechaHoraSistemaTs = Fecha.toTimeStamp(FechaDB.ObtenerFechaHoraSistemaLocal(dbcon, 1));
				Timestamp horaSistema = Fecha.toTimeStamp(FechaDB.ObtenerFechaHoraSistemaLocal(dbcon, 1));
				
				String horaFormat = Fecha.toFormat(fechaHoraSistemaTs, "_HHmmss");
				String fechaHoraFormat = fechaFormat + horaFormat;
				
				String zipName = "NCG365FM_"+fechaFormat;
				if (EsPauta.equals("S")) {
					zipName = zipName + ".zip";
				}else {
					zipName = zipName + horaFormat + ".zip";
				}
				String zip = ruta  + zipName;
				log.write("Fecha Certificacion: "+ FTER + "  -  Ruta Zip: " +zip);
				long contadorArchivos = 0;
				ReportDocument report = new ReportDocument(Utils.getSchema(),datasource);
				while (rs.next()) {
					contadorArchivos++;
					String reportId = rs.getString("IdArchivo");											
					if(!report.readDocument(reportId)){
						return false;
					}

					try {
						//Aqui se carga el resultado del servicio.
						
						//Movimientos de FMMM
						spexe = dbcon.prepareCall(callSpexe);
						spexe.setLong(1, idEmpresa);
						spexe.setLong(2, rs.getLong("IdCliente"));
						spexe.setString(3, "SERIES");
						spexe.setString(4, fechaFinal);
						boolean isResultSet =spexe.execute();
						
						//Remuneraciones de Fondos
						spRem = dbcon.prepareCall(callSpRem);
						spRem.setLong(1, idEmpresa);
						spRem.setLong(2, rs.getLong("IdCliente"));
						spRem.setString(3, "REMUNERACION");
						spRem.setString(4, fechaFinal);
						
						boolean isResultSetRem =spRem.execute();
						
						spEmisores = dbcon.prepareCall(callSpEmisores);
						spEmisores.setLong(1, idEmpresa);
						boolean isResultSetEmisores = spEmisores.execute();
						
						if(isResultSet){
							System.out.println("INFORME NCG 365 : Servicio Crystal Resulset  ->  " + isResultSet);
							ResultSet rsSP = spexe.getResultSet();
							report.setDocumentResultSet(rsSP);
							
							//sub reporte
							ResultSet rsSPRemuneraciones = spRem.getResultSet();
							ResultSet rsSPEmisores = spEmisores.getResultSet();
														
							HashMap<String,ResultSet> subResult = new HashMap<>();
							subResult.put("REMUNERACIONES", rsSPRemuneraciones);
							subResult.put("EMISORES", rsSPEmisores);
							
							report.setSubdocumentsResultSet(subResult);
							
							System.out.println("INFORME NCG 365: modifica Resulset en crystal");
							log.write("**  MODIFICACION RESULSET PARA CRYSTAL REPORT / OK");
							
							//exportar el reporte en formato pdf, se guarda fisicamente en <<ruta>>.
							try (InputStream inStreamRpt = report.exportDocument("pdf")) {
								// Genera el nombre de arhivo				
					
								String hora = Fecha.toFormat(horaSistema, "_HHmmss");
								
                                // CAMBIOS PARA ENCRIPTACION
								String tempFileName =rs.getString("NombreArchivo") + "_"+ rs.getString("DocumentoIdentidad") + "_" + fechaFormat + hora  + "_temp" + ".pdf";

								FileUtils.copyInputStreamToFile(inStreamRpt, new File(ruta, tempFileName));
								IOUtils.closeQuietly(inStreamRpt);

                                log.write("**  ARCHIVO TEMPORAL CREADO :  " + tempFileName);
								
								String docuIdent = rs.getString("DocumentoIdentidad");
								String pdfPassword = docuIdent.substring(docuIdent.length()-6,docuIdent.length()-2);
								String fileName = tempFileName.substring(0,tempFileName.length()-9)+tempFileName.substring(tempFileName.length()-4);

                                try {
                        			PDDocument document = Loader.loadPDF(new File(ruta + tempFileName));
						            StandardProtectionPolicy spp = new StandardProtectionPolicy(pdfPassword, pdfPassword, new AccessPermission());
						            spp.setEncryptionKeyLength(128);
						            document.protect(spp);
						            document.save(ruta + fileName);
						            document.close();
								} catch (Exception e) {
									log.write("ERROR :  " + e.getMessage());
								}
								
								log.write("**  ARCHIVO ENCRIPTADO CREADO :  " + ruta + fileName);

								File file = new File(ruta + fileName);
					            byte[] archivoPdf = Files.readAllBytes(file.toPath());
								File tempFile = new File(ruta + tempFileName);
								boolean tempFileBorrado = tempFile.delete();
								if(tempFileBorrado) {
									log.write("**  ARCHIVO TEMPORAL BORRADO :  " + ruta + tempFileName);
								}else {
									log.write("**  ERROR AL BORRAR ARCHIVO TEMPORAL :  " + ruta + tempFileName);
								}
								// ENCRIPTACION FIN

								//se guardar las rutas de cada archivo para luego zipearlos
								archivos.add(ruta+fileName);
								System.out.println("INFORME NCG 365: ARCHIVO CREADO ->  " + ruta+fileName);
								log.write("**  ARCHIVO CREADO :  " + ruta+fileName);
								
								ArchivoNCG365 archivoNCG365 = new ArchivoNCG365();
								archivoNCG365.IdCliente = rs.getLong("IdCliente");
								archivoNCG365.NombreArchivo = fileName;
								archivoNCG365.FechaCertificacion = fechaFinal;
								archivoNCG365.UbicacionZip = zip;
								archivoNCG365.Archivo = archivoPdf;
								archivosInsert.add(archivoNCG365);
							}
						}
					} catch (Exception e) {
						log.write(e.getMessage());
						e.printStackTrace();
					}
				}
				
				System.out.println("INFORME EJECUCION: ZIPFILE -> " + zip);
				log.write("ARCHIVO ZIP : " + zip);
				//Crear ZIP
				
				try (OutputStream os = new FileOutputStream(zip)) {
					try (ZipOutputStream zos = new ZipOutputStream(os)) {
						for (String archivo : archivos) {
							//obtener cada archivo a zipear
							String fileName = FilenameUtils.getName(archivo);
							System.out.println("COMPRESSFILE: " + fileName);
							
							try (InputStream fis = new FileInputStream(archivo)) {
								zos.putNextEntry(new ZipEntry(fileName));
								//escribie un archivo en el zip
								int length;
								byte[] buffer = new byte[1024];
								while ((length = fis.read(buffer)) > 0) {
									zos.write(buffer, 0, length);
								}
								zos.closeEntry();
								System.out.println("COMPRESSFILE: " + archivo+ " DONE");
								log.write("COMPRIMIDO ZIP : " + archivo+ " AGREGADO");
							}
							FileUtils.forceDelete(new File(archivo));//Borrar cada arhivo incluido en el zip al terminar.
						}
					}
				}
				// Se registran los archivos creados
				List<ArchivoNCG365> lista = new ArrayList<>();
				int parametroTanda = 0;
				try {
					parametroTanda = Integer.parseInt(ConfiguracionSistema.getParametroConfiguracionString(dbcon, idEmpresa, "PLATAFORMA", "PLA.ValorTandaArchivoAdjunto"));
				} catch (Exception e) {
					parametroTanda = 30;
				}
				try { 
					if (archivosInsert != null && archivosInsert.size() > 0) {
						for (ArchivoNCG365 archivoAdjunto : archivosInsert) {
							lista.add(archivoAdjunto);
							if(lista.size() == parametroTanda) {
								sql.batch(dbcon, SQL_INSERT_ARCHIVO_NCG365, lista, ArchivoNCG365.class);
								log.write("Archivos registrados parcialmente en tabla BSCL.Normativa.BD::CIR.ArchivoNCG365");
								lista.clear();
							}
						}                    
						if(!lista.isEmpty()) {
							sql.batch(dbcon, SQL_INSERT_ARCHIVO_NCG365, lista, ArchivoNCG365.class);
							log.write("Archivos registrados parcialmente en tabla BSCL.Normativa.BD::CIR.ArchivoNCG365");
						}                    
					}
					log.write("Finalizacion Archivos registrados en tabla BSCL.Normativa.BD::CIR.ArchivoNCG365");					
				} catch (Exception e) {
					log.write("Error en el registro de archivos en tabla BSCL.Normativa.BD::CIR.ArchivoNCG365");
					log.write(e.getMessage());
				}
				
				if(!debug) {
					//actualiza la ruta del archivo y el estado de la solicitud
					ActualizarSolicitud(secuenciaSolicitud, zipName, ruta, contadorArchivos, dbcon, getJobId());					
				}
			}
		}
		System.out.println( ":GENERAR INFORME NCG 365 FFMM  MASIVO:  ****** FIN  *******");
		log.write("****** FIN TAREA *******");

		return true;
	}


	private String getQuotedString(final String name) {
		return "\"" + name + "\"";
	}

	private String ActualizarSolicitud(final long p_IdSolicitud, String p_NombreArchivo, String p_RutaArchivo,
			final long p_NumRegistros, Connection conn, long jobId) {
		
		String sqlCall = "";

		String PROCEDURE_NAME_ACTUALIZAR = "BSCL.Normativa.BD.Servicios.NOR::ModificarEstadoSolicitudCircular";
		p_NombreArchivo = "'" + p_NombreArchivo + "'";
		p_RutaArchivo = "'" + p_RutaArchivo + "'";
		sqlCall = String.format("{CALL %s.%s(%s,%s,%s,%s,%s)}", getQuotedString("BSCL"),
				getQuotedString(PROCEDURE_NAME_ACTUALIZAR), p_IdSolicitud, p_NombreArchivo, p_RutaArchivo,
				p_NumRegistros, jobId);
		System.out.println( sqlCall);

		try (CallableStatement cstmt = conn.prepareCall(sqlCall)) {
			conn.setAutoCommit(false);

			if (cstmt.execute()) {
				conn.commit();

				try (final ResultSet rs = cstmt.getResultSet()) {
					if (rs.next()) {
						LOGGER.log(Level.SEVERE, "Proceso de Actualizacion de Solicitud exitoso");
						return "OK";
					} else {
						LOGGER.log(Level.SEVERE, "Error durante proceso de Actualizacion de Solicitud, no RS");
						return "NOK";

					} 

				} catch (Exception ex) {

					LOGGER.log(Level.SEVERE, "Error durante proceso de Actualizacion de Solicitud", ex);
					return "NOK";
				}

			} else {

				LOGGER.log(Level.SEVERE, "Error no se pudo ejecutar servicio actualizacion");
				return "NOK";
			}

		} catch (Exception ex) {

			LOGGER.log(Level.SEVERE, "Error durante proceso de Actualizacion de Solicitud", ex);
			return "NOK";
		}

	}
	
	private Long obtenerParametrOdiasAtrasoNotificacionNCG365(Connection dbcon, Long idEmpresa) {
		Long diasAtrasoNotificacionNCG365 = 0L;
		try {
			diasAtrasoNotificacionNCG365 = ConfiguracionSistema.getParametroConfiguracionLong(dbcon, idEmpresa, "FONDOMUTUO", "DiasAtrasoNotificacionNCG365");
		}catch(Exception e) {
			LOGGER.log(Level.SEVERE, "ADVERTENCIA::BcsJobncg365FFMMMasivo::No se encuentra parametro DiasAtrasoNotificacionNCG365");
		}
		return diasAtrasoNotificacionNCG365;
	}

	@Override
	public void onError(Throwable t) {
		System.out.println( ":GENERAR INFORME NCG 365 FFMM  MASIVO:  ****** ERROR  *******");
		log.write(t);
		t.printStackTrace();
	}

	@Override
	public void pause() {

	}
	
	public static class ConsultaEsPauta {
		public String EsPauta;
	}
	
	public static class ArchivoNCG365 {
		public Long			IdCliente;
		public String		NombreArchivo;
		public String 		UbicacionZip;
		public String 		FechaCertificacion;
		public byte[]		Archivo;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class BcsJobncg365FFMMMasivo_Entrada {
		public Long			p_IdEmpresa;
		public Long			p_IdUsuario;
		public String 		p_IdModulo;
		//public List<Orden>	p_Ordenes;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static void main(String[] args){
		//Porcentaje.setScale(100);
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("qa01","connections.properties")) {
			BcsJobncg365FFMMMasivo sp = new BcsJobncg365FFMMMasivo();
			sp.logica(con,true);
			con.rollback();
		} catch (Throwable e) {
			e.printStackTrace();			
		}	
		System.out.println("== END ==");
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
