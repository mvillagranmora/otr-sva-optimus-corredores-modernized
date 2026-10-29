package cl.bcs.normativa.jobs;
import java.io.BufferedInputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import org.apache.commons.io.IOUtils;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.sql.DataSource;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import cl.bcs.normativa.dal.LocalizadorArchivo;
import cl.bcs.plataforma.report.api.ReportDocument;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.ListMapResultSet;
import cl.bcs.utils.Utils;
import cl.bcs.utils.Log;

public final class BcsJobInformeEjecucionOrdenesMasivo extends BcsJob {
	private final Log log = new Log("NORMATIVA_EJECUCION_ORDENES_MASIVO_PDF"); 	
	private static final Logger LOGGER = Logger.getLogger( BcsJobInformeEjecucionOrdenesMasivo.class.getName());
	AtomicBoolean running=new AtomicBoolean(false); 
	
	public boolean transaction(Connection dbcon) throws InterruptedException, Throwable {
		System.out.println( ":GENERAR INFORME EJECUCION DE ORDENES MASIVO:  ****** INICIO  *******");
		log.write("  ****** INICIO  *******");
		DataSource datasource = super.getDataSource();
		CallableStatement spexe = null;
		
		final long idEmpresa = Long.parseLong(this.getParam("IdEmpresa"));
		final long secuenciaSolicitud = Long.parseLong(this.getParam("IdSolicitudCircular"));
		final long idDocumento = Long.parseLong(this.getParam("idDocumento"));
		final String notificar = this.getParam("Notificacion");
		
		log.write("Notificar = " + notificar);
		
		final String FINI = this.getParam("FINI");
		final String FTER = this.getParam("FTER");		
		List<String> archivos = new ArrayList<String>();
		//obtener la ruta de archivos para normativo
		String ruta = LocalizadorArchivo.buscarRutaRaiz(dbcon, Utils.getSchema(), idEmpresa)+ File.separator;
		
		String selectCertificados = "";
		//obtener los datos para la generacion de cada certificado
		if(notificar.equalsIgnoreCase("0") || notificar.equalsIgnoreCase("1")) {
			selectCertificados = "SELECT * FROM (SELECT  NOM.\"IdEmpresa\",NOM.\"IdCliente\" FROM  \"BSCL.Normativa.BD.Vistas::EJORD.ConsultarNomina\" AS  NOM"
					+ " LEFT JOIN \"BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente\" as ICLI ON NOM.\"IdCliente\" = ICLI.\"IdCliente\""
					+ " WHERE ICLI.\"IdTipoIndicador\" = '74' AND ICLI.\"ValorIndicador\" <> '1'"
					+ "   AND  NOM.\"IdEmpresa\"=?  AND TO_DATE (NOM.\"FechaEjecucion\") BETWEEN  TO_DATE (?) AND TO_DATE (?)"
					+ "   GROUP BY NOM.\"IdEmpresa\", NOM.\"IdCliente\" ORDER BY NOM.\"IdCliente\")"	  
					+ "   CROSS JOIN (SELECT TOP 1 ARC.* FROM  \"BSCL.Normativa.BD.Vistas::DOC.ConsultarDocumento\" AS DOC"
					+ "		INNER JOIN \"BSCL.Normativa.BD.Vistas::NOR.ConsultarArchivoNormativa\" AS ARC"
					+ "		ON (DOC.\"IdTipoDocumento\"=ARC.\"IdTipoDocumento\" AND DOC.\"Esquema\" =ARC.\"Esquema\"  AND DOC.\"Version\"=ARC.\"Version\")"
					+ "		WHERE DOC.\"IdDocumento\"=?) AS B";
		}
		else {
			selectCertificados = "SELECT * FROM (SELECT  NOM.\"IdEmpresa\",NOM.\"IdCliente\" FROM  \"BSCL.Normativa.BD.Vistas::EJORD.ConsultarNomina\" AS  NOM"
					+ " LEFT JOIN \"BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente\" as ICLI ON NOM.\"IdCliente\" = ICLI.\"IdCliente\""
					+ " WHERE ICLI.\"IdTipoIndicador\" = '74' AND ICLI.\"ValorIndicador\"=?"
					+ "   AND  NOM.\"IdEmpresa\"=?  AND TO_DATE (NOM.\"FechaEjecucion\") BETWEEN  TO_DATE (?) AND TO_DATE (?)"
					+ "   GROUP BY NOM.\"IdEmpresa\", NOM.\"IdCliente\" ORDER BY NOM.\"IdCliente\")"	  
					+ "   CROSS JOIN (SELECT TOP 1 ARC.* FROM  \"BSCL.Normativa.BD.Vistas::DOC.ConsultarDocumento\" AS DOC"
					+ "		INNER JOIN \"BSCL.Normativa.BD.Vistas::NOR.ConsultarArchivoNormativa\" AS ARC"
					+ "		ON (DOC.\"IdTipoDocumento\"=ARC.\"IdTipoDocumento\" AND DOC.\"Esquema\" =ARC.\"Esquema\"  AND DOC.\"Version\"=ARC.\"Version\")"
					+ "		WHERE DOC.\"IdDocumento\"=?) AS B";
		}
		  
		//Servicio del certificado
		final String callSpexe = "CALL \"BSCL\".\"BSCL.Normativa.CrystalReports.Servicios::ConsultarEjecucionOrdenes\"(?,?,?,?)";
		
		//dbConnection = datasource.getConnection();
		try (PreparedStatement pstmt = dbcon.prepareStatement(selectCertificados)) {
			if(notificar.equalsIgnoreCase("0") || notificar.equalsIgnoreCase("1")) {
				pstmt.setLong(1, idEmpresa);
				pstmt.setString(2, FINI);
				pstmt.setString(3, FTER);
				pstmt.setLong(4, idDocumento);
			}
			else {
				pstmt.setString(1, notificar);
				pstmt.setLong(2, idEmpresa);
				pstmt.setString(3, FINI);
				pstmt.setString(4, FTER);
				pstmt.setLong(5, idDocumento);
			}
			String N="";
			System.out.println(System.getProperty("java.version"));
			log.write("**  VERSION JAVA: " + System.getProperty("java.version"));
			
			try (ResultSet rs = pstmt.executeQuery()) {
				long contadorArchivos = 0;
				while (rs.next()) {
					contadorArchivos++;
					String reportId = rs.getString("IdArchivo");

					ReportDocument report = new ReportDocument(Utils.getSchema(),datasource);
					if(!report.readDocument(reportId)){
						return false;
					}

					try {
						

						//Aqui se carga el resultado del servicio.
						System.out.println("Cliente ID = " + rs.getLong("IdCliente"));
						spexe = dbcon.prepareCall(callSpexe);
						spexe.setLong(1, idEmpresa);
						spexe.setLong(2, rs.getLong("IdCliente"));
						spexe.setString(3, FINI);
						spexe.setString(4, FTER);
						boolean isResultSet =spexe.execute();
						
						if(isResultSet){
							System.out.println("INFORME EJECUCION: Servicio Crystal Resulset  ->  " + isResultSet);
							ResultSet rsSP = spexe.getResultSet();
							report.setDocumentResultSet(rsSP);
							System.out.println("INFORME EJECUCION: modifica Resulset en crystal");
							log.write("**  MODIFICACION RESULSET PARA CRYSTAL REPORT / OK");
							
							//exportar el reporte en formato pdf, se guarda fisicamente en <<ruta>>.
							try (InputStream inStreamRpt = report.exportDocument("pdf")) {
								
								String fileName =rs.getString("NombreArchivo") + "_"+String.format("%03d",contadorArchivos)+".pdf";
								
								FileUtils.copyInputStreamToFile(inStreamRpt, new File(ruta, fileName));
								IOUtils.closeQuietly(inStreamRpt);
								//se guardar las rutas de cada archivo para luego zipearlos
								archivos.add(ruta+fileName);
								System.out.println("INFORME EJECUCION: ARCHIVO CREADO ->  " + ruta+fileName);
								log.write("**  ARCHIVO CREADO :  " + ruta+fileName);
							}
						}
						
						
						
					} catch (Exception e) {
						log.write(e);
						e.printStackTrace();
					}
				}
				
				System.out.println("NUMERO DE ARCHIVOS = "+contadorArchivos);
				
				//Nombre y ruta ZIP
				String zipName = "EJECUCIONORD_"+new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date())+ ".zip";
				String zip = ruta  + zipName;
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
				
				//actualiza la ruta del archivo y el estado de la solicitud
				ActualizarSolicitud(secuenciaSolicitud, zipName, ruta, contadorArchivos, dbcon, getJobId());
			}
		}
		System.out.println( ":GENERAR INFORME EJECUCION DE ORDENES MASIVO:  ****** FIN  *******");
		log.write("****** FIN TAREA *******");

		return true;
	}


	private String getQuotedString(final String name) {
		return "\"" + name + "\"";
	}

	private String ActualizarSolicitud(final long p_IdDocumento, String p_NombreArchivo, String p_RutaArchivo,
			final long p_NumRegistros, Connection conn, long jobId) {
		
		String sqlCall = "";

		String PROCEDURE_NAME_ACTUALIZAR = "BSCL.Normativa.BD.Servicios.NOR::ModificarEstadoSolicitudCircular";
		p_NombreArchivo = "'" + p_NombreArchivo + "'";
		p_RutaArchivo = "'" + p_RutaArchivo + "'";
		sqlCall = String.format("{CALL %s.%s(%s,%s,%s,%s,%s)}", getQuotedString("BSCL"),
				getQuotedString(PROCEDURE_NAME_ACTUALIZAR), p_IdDocumento, p_NombreArchivo, p_RutaArchivo,
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

				LOGGER.log(Level.SEVERE, "Error no se pudo ejecutar servicio actualizaciÃ¯Â¿Â½n");
				return "NOK";
			}

		} catch (Exception ex) {

			LOGGER.log(Level.SEVERE, "Error durante proceso de Actualizacion de Solicitud", ex);
			return "NOK";
		}

	}

	@Override
	public void onError(Throwable t) {
		System.out.println( ":GENERAR INFORME EJECUCION DE ORDENES MASIVO:  ****** ERROR  *******");
		log.write(t);
		t.printStackTrace();
	}

	@Override
	public void pause() {

	}

}
