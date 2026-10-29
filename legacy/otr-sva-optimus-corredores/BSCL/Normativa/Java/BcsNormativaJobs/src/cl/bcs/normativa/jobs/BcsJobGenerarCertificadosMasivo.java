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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.FileLoader;
import cl.bcs.plataforma.report.api.ReportDocument;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.ListMapResultSet;
import cl.bcs.utils.Utils;
import cl.bcs.utils.Log;
import cl.bcs.utils.JavaCall;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;


public final class BcsJobGenerarCertificadosMasivo extends BcsJob {
	private static final Logger logger = LogManager.getLogger(BcsJobGenerarCertificadosMasivo.class);
	AtomicBoolean running=new AtomicBoolean(false); 
	
	private String param1,param2,param3,param4,param5="";
	
	
	public boolean transaction(Connection dbcon) throws InterruptedException, Throwable {
		logger.info(":GENERAR CERTIFICADOS MASIVO:  ****** INICIO  *******");
	
		DataSource datasource = super.getDataSource();
		CallableStatement spexe = null;
		CallableStatement spresumen = null;
		
		final long idEmpresa = Long.parseLong(this.getParam("IdEmpresa"));
		final long idUsuario = Long.parseLong(this.getParam("IdUsuario"));
		final String idModulo = this.getParam("IdModulo");
		final String version = this.getParam("Version");		
		final String todos = this.getParam("Todos");	
		final String nombreReporte = this.getParam("NombreReporte");	
		final String codigoPrograma = this.getParam("CodigoPrograma");
		final String esquema = this.getParam("Esquema");
		final long JobId=getJobId();
		logger.debug("NombreReporte:" + nombreReporte);
		String nombrePDF="";
		
		//leer parametros nombre de archivos
		LeerNombreArchivos(dbcon,idEmpresa,nombreReporte,version,esquema);
		
		if((param1 +param2+param3+param4+param5 ).length()==0)
		{
			nombrePDF=nombreReporte;
			
		};
		
		
		List<String> archivos = new ArrayList<String>();//Lista de archivos para ZIP
		//obtener la ruta de archivos para normativo
		String ruta = LocalizadorArchivo.buscarRutaRaiz(dbcon, Utils.getSchema(), idEmpresa)+ File.separator;
	
		final String spConsultarClientesFolioCertificado = "CALL \"BSCL\".\"BSCL.Normativa.BD.Servicios::TRI.ConsultarClientesFolioCertificado\"(?,?,?,?,?)";
		String callSpResumen = "CALL \"BSCL\".\"BSCL.Normativa.BD.Servicios::TRI.CrearResumenGeneracion\"(?,?,?,?,?,?,?,?,?)";
		logger.debug(spConsultarClientesFolioCertificado);
		//Servicio del certificado
		String callSpexe = "";
		if (codigoPrograma.equalsIgnoreCase("GenerarCertificado7")){
			callSpexe = "CALL \"BSCL\".\"BSCL.Normativa.BD.Servicios::TRI.DJ1890.GenerarDocumentoCertificado7\"(?,?,?,?,?,?,?,?)";
		} else if (codigoPrograma.equalsIgnoreCase("GenerarCertificado21")){
			callSpexe = "CALL \"BSCL\".\"BSCL.Normativa.BD.Servicios::TRI.DJ1894.GenerarDocumentoCertificado21\"(?,?,?,?,?,?,?,?)";
		} else if (codigoPrograma.equalsIgnoreCase("GenerarCertificado31")){
			callSpexe = "CALL \"BSCL\".\"BSCL.Normativa.BD.Servicios::TRI.DJ1894.GenerarDocumentoCertificado31\"(?,?,?,?,?,?,?,?)";
		} else if (codigoPrograma.equalsIgnoreCase("GenerarCertificado4")){
			callSpexe = "CALL \"BSCL\".\"BSCL.Normativa.BD.Servicios::TRI.DJ1885.GenerarDocumentoCertificado4\"(?,?,?,?,?,?,?,?)";
		} else if (codigoPrograma.equalsIgnoreCase("GenerarCertificado9")){
			callSpexe = "CALL \"BSCL\".\"BSCL.Normativa.BD.Servicios::TRI.DJ1889.GenerarDocumentoCertificado9\"(?,?,?,?,?,?,?,?)";	
		} else if (codigoPrograma.equalsIgnoreCase("GenerarCertificado17")){
			callSpexe = "CALL \"BSCL\".\"BSCL.Normativa.BD.Servicios::TRI.DJ1944.GenerarDocumentoCertificado17\"(?,?,?,?,?,?,?,?)";
		} else if (codigoPrograma.equalsIgnoreCase("GenerarCertificado24")){
			callSpexe = "CALL \"BSCL\".\"BSCL.Normativa.BD.Servicios::TRI.DJ1899.GenerarDocumentoCertificado24\"(?,?,?,?,?,?,?,?)";
		} else if (codigoPrograma.equalsIgnoreCase("GenerarCertificado8")){
			callSpexe = "CALL \"BSCL\".\"BSCL.Normativa.BD.Servicios::TRI.DJ1944.GenerarDocumentoCertificado8\"(?,?,?,?,?,?,?,?)";
		} else if (codigoPrograma.equalsIgnoreCase("GenerarCertificado30")){
			callSpexe = "CALL \"BSCL\".\"BSCL.Normativa.BD.Servicios::TRI.DJ1899.GenerarDocumentoCertificado30\"(?,?,?,?,?,?,?,?)";
		} else if (codigoPrograma.equalsIgnoreCase("GenerarCertificado36")){
			callSpexe = "CALL \"BSCL\".\"BSCL.Normativa.BD.Servicios::TRI.DJ1899.GenerarDocumentoCertificado36\"(?,?,?,?,?,?,?,?)";
			
		} else if (codigoPrograma.equalsIgnoreCase("GenerarCertificado71")){
			callSpexe = "CALL \"BSCL\".\"BSCL.Normativa.BD.Servicios::TRI.DJ1949.GenerarDocumentoCertificado71\"(?,?,?,?,?,?,?,?)";
		} else {
			callSpexe = "CALL \"BSCL\".\"BSCL.Normativa.BD.Servicios::TRI.DJ1922.GenerarDocumentoCertificado43\"(?,?,?,?,?,?,?,?)";
		}
		logger.debug("idEmpresa:"+ idEmpresa);
		logger.debug("idUsuario:"+ idUsuario);
		logger.debug("idModulo:"+ idModulo);
		logger.debug("codigoPrograma:"+ codigoPrograma);
		
		//String reportId = "3D8F019093B618C31700D2006448EF3A";
		String reportId = this.getParam("IdReporte");
		logger.debug(callSpexe);
		
		logger.debug("Antes de instanciar reporte :" + reportId);
		final ReportDocument report = new ReportDocument(Utils.getSchema(),datasource);
		if(!report.readDocument(reportId)){
			return false;
		}
		logger.debug("Fin de instanciar reporte :" + reportId);
		
		try (PreparedStatement pstmt = dbcon.prepareStatement(spConsultarClientesFolioCertificado)) {
			pstmt.setLong(1, idEmpresa);
			pstmt.setLong(2, idUsuario);
			pstmt.setString(3, idModulo);
			pstmt.setString(4, version);
			pstmt.setString(5, codigoPrograma);

			String RutCliente,CodigoPortafolio,param1aux,param2aux,param3aux,param4aux,param5aux="";
			try (ResultSet rs = pstmt.executeQuery()) {
				long contadorArchivos = 0;
				while (rs.next()) {
					contadorArchivos++;
					try {
						spexe = dbcon.prepareCall(callSpexe);
						spexe.setLong(1, idEmpresa);
						spexe.setLong(2, idUsuario);
						spexe.setString(3, idModulo);
						spexe.setString(4, version);
						spexe.setString(5, "N");
						
						if (codigoPrograma.equalsIgnoreCase("GenerarCertificado43")){
							spexe.setString(6, rs.getString("RutCliente"));
							spexe.setString(7, rs.getString("CodigoPortafolio"));
						} else if (codigoPrograma.equalsIgnoreCase("GenerarCertificado9")){
							spexe.setString(6, rs.getString("RutCliente"));
							spexe.setString(7, rs.getString("CodigoPortafolio"));
						} else if (codigoPrograma.equalsIgnoreCase("GenerarCertificado24")){
							spexe.setString(6, rs.getString("RutCliente"));
							spexe.setString(7, rs.getString("CodigoPortafolio"));
						} else if (codigoPrograma.equalsIgnoreCase("GenerarCertificado36")){
							spexe.setString(6, rs.getString("RutCliente"));
							spexe.setString(7, rs.getString("CodigoPortafolio"));
						} else if (codigoPrograma.equalsIgnoreCase("GenerarCertificado71")){
							spexe.setString(6, rs.getString("RutCliente"));
							spexe.setString(7, rs.getString("CodigoPortafolio"));
						} else if (codigoPrograma.equalsIgnoreCase("GenerarCertificado17")){
							spexe.setString(6, rs.getString("RutCliente"));
							spexe.setString(7, rs.getString("CodigoPortafolio"));
						} else if (codigoPrograma.equalsIgnoreCase("GenerarCertificado8")){
							spexe.setString(6, rs.getString("RutCliente"));
							spexe.setString(7, rs.getString("CodigoPortafolio"));
						} else if (codigoPrograma.equalsIgnoreCase("GenerarCertificado21")){
							spexe.setString(6, rs.getString("RutCliente"));
							spexe.setString(7, rs.getString("CodigoPortafolio"));
						} else {
							spexe.setLong(6, rs.getLong("IdCliente"));
							spexe.setLong(7, rs.getLong("IdPortafolio"));
						};
						
						spexe.setLong(8, rs.getLong("Folio"));
						RutCliente=rs.getString("RutCliente");
						if ((RutCliente).length()!=0){RutCliente=String.format("%10s", (RutCliente).length() > 10 ?  (RutCliente).substring(0,10) :  (RutCliente)).replace(' ', '0');};
						
						CodigoPortafolio=rs.getString("CodigoPortafolio");
						
						logger.debug("Antes de ejecutar SP del reporte :" + callSpexe);
						logger.debug("parametros :");
						logger.debug("idEmpresa :" +idEmpresa);
						logger.debug("idUsuario :" +idUsuario);
						logger.debug("idModulo :" +idModulo);
						logger.debug("version :" +version);
						logger.debug("todos :" +"N");
						logger.debug("IdCliente :" +rs.getLong("IdCliente"));
						logger.debug("IdPortafolio :" +rs.getLong("IdPortafolio"));
						logger.debug("Folio :" +rs.getLong("Folio"));
						
						logger.debug("RutCliente :" +RutCliente);
						logger.debug("CodigoPortafolio :" +CodigoPortafolio);
					
						
						
						boolean isResultSet =spexe.execute();
						
						if(isResultSet){
							logger.debug("CERTIFICADO TRIBUTARIO: Servicio Crystal Resulset  ->  " + isResultSet);
							ResultSet rsSP = spexe.getResultSet();
							report.setDocumentResultSet(rsSP);
							logger.debug("CERTIFICADO TRIBUTARIO: modifica Resulset en crystal");
							logger.debug("**  MODIFICACION RESULSET PARA CRYSTAL REPORT / OK");
							
							//exportar el reporte en formato pdf, se guarda fisicamente en <<ruta>>.
							try (InputStream inStreamRpt = report.exportDocument("pdf")) {
								
                                //NOMBRE DEL ARCHIVO PDF
								String fileName="";
								if((param1 +param2+param3+param4+param5 ).length()!=0)
								{
									if ((RutCliente).length()==0){RutCliente=String.format("%03d",contadorArchivos);};
									 if (param1.equalsIgnoreCase("[RUT]")){param1aux=RutCliente;}else{param1aux=param1;};
									 if (param2.equalsIgnoreCase("[RUT]")){param2aux=RutCliente;}else{param2aux=param2;};
									 if (param3.equalsIgnoreCase("[RUT]")){param3aux=RutCliente;}else{param3aux=param3;};
									 if (param4.equalsIgnoreCase("[RUT]")){param4aux=RutCliente;}else{param4aux=param4;};
									 if (param5.equalsIgnoreCase("[RUT]")){param4aux=RutCliente;}else{param5aux=param5;};
									
									nombrePDF=param1aux +param2aux+param3aux+param4aux+param5aux;
									fileName =nombrePDF + ".pdf";
								}else{	
									
									fileName =nombrePDF + "_"+  RutCliente + "_"+   String.format("%03d",contadorArchivos)+".pdf";	
								};
								
								
								
								
								FileUtils.copyInputStreamToFile(inStreamRpt, new File(ruta, fileName));
								IOUtils.closeQuietly(inStreamRpt);
								//se guardar las rutas de cada archivo para luego zipearlos
								archivos.add(ruta+fileName);
								logger.debug("CERTIFICADO TRIBUTARIO: ARCHIVO CREADO ->  " + ruta+fileName);
								logger.debug("**  ARCHIVO CREADO :  " + ruta+fileName);
							}
						}
					} catch (Exception e) {
						logger.error(e);
						e.printStackTrace();
					}
				}
				//Nombre y ruta ZIP
				String zipName = nombreReporte + "_"+new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date())+ ".zip";
				//String zipName = "CERTMASIVOS_1.zip";
				String zip = ruta  + zipName;
				logger.debug("CERTIFICADO TRIBUTARIO: ZIPFILE -> " + zip);
				logger.debug("ARCHIVO ZIP : " + zip);
				
				logger.debug("Procesando archivos");
				for (String archivo : archivos) {
					logger.debug(archivo);
				}
				
				
				//Crear ZIP
				int contArchivos=0;
				try (OutputStream os = new FileOutputStream(zip)) {
					try (ZipOutputStream zos = new ZipOutputStream(os)) {
						
						for (String archivo : archivos) {
							//obtener cada archivo a zipear
							String fileName = FilenameUtils.getName(archivo);
							logger.debug("COMPRESSFILE: " + fileName);
							
							try (InputStream fis = new FileInputStream(archivo)) {
								zos.putNextEntry(new ZipEntry(fileName));
								//escribie un archivo en el zip
								int length;
								byte[] buffer = new byte[1024];
								while ((length = fis.read(buffer)) > 0) {
									zos.write(buffer, 0, length);
								}
								zos.closeEntry();
								contArchivos=contArchivos+1;
								logger.debug("COMPRESSFILE: " + archivo+ " DONE");
								logger.debug("COMPRIMIDO ZIP : " + archivo+ " AGREGADO");
							}
							catch(Exception e) {
								logger.error("ME CAI AL ZIPEAR: " + archivo);
								logger.error(e);
							}
							try {
								FileUtils.forceDelete(new File(archivo));//Borrar cada arhivo incluido en el zip al terminar.
							}
							catch(Exception e) {
								logger.error("ME CAI AL BORRAR: " + archivo);
								logger.error(e);
							}
						}
					}
				}
				logger.debug("Registrando resumen de generacion para :" + zipName);
				logger.debug(callSpResumen);
				String glosa ="Finalizada";
				try {
					spresumen = dbcon.prepareCall(callSpResumen);
					spresumen.setLong(1, idEmpresa);
					spresumen.setLong(2, idUsuario);
					spresumen.setString(3, idModulo);
					spresumen.setString(4, esquema);
					spresumen.setString(5, version);
					spresumen.setLong(6, JobId);
					spresumen.setString(7,zipName);
					spresumen.setString(8,glosa);
					spresumen.setLong(9, contArchivos);
					
					
					logger.debug("Antes de ejecutar SP del resumen :" + callSpResumen);
					logger.debug("parametros :");
					logger.debug("idEmpresa :" +idEmpresa);
					logger.debug("idUsuario :" +idUsuario);
					logger.debug("idModulo :" +idModulo);
					logger.debug("esquema :" +esquema);
					logger.debug("version :" +version);
					logger.debug("JobId :" +JobId);
					logger.debug("zipName :" +zipName);
					logger.debug("glosa :" +glosa);
					logger.debug("contArchivos :" +contArchivos);
					
					
					
					boolean Result =spresumen.execute();
					
					
				} catch (Exception e) {
					logger.error(e);
					e.printStackTrace();
				}
				
			}
		}
		
		logger.info( ":GENERAR CERTIFICADO TRIBUTARIO DE ORDENES MASIVO:  ****** FIN  *******");

		return true;
	}

	@Override
	public void onError(Throwable t) {
		logger.error( ":GENERAR CERTIFICADO TRIBUTARIO DE ORDENES MASIVO:  ****** ERROR  *******");
		logger.error(t);
		t.printStackTrace();
	}

	@Override
	public void pause() {

	}
	
    public void LeerNombreArchivos(Connection dbcon, Long idEmpresa, String nombreReporte, String version,String esquema ) throws Exception{
    	System.out.println("Obtener parametros para nombre de PDFs");
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
		
	
	
	

}
