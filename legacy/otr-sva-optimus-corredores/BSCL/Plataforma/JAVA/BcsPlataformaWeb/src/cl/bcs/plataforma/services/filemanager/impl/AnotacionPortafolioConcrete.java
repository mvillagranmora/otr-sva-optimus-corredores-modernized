package cl.bcs.plataforma.services.filemanager.impl;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Key;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;
import java.util.Properties;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.plataforma.services.filemanager.FileBlobManager;
import cl.bcs.utils.Utils;

public class AnotacionPortafolioConcrete implements FileBlobManager{
	private Connection connection = null;
	private String filename = null;
	static String algorithm = "DESede";
	
	static final Logger logger = LogManager.getLogger(AnotacionPortafolioConcrete.class);
	
	@Override
	public String update(HttpServletRequest request, HttpServletResponse response) {
		try {
			String name = request.getParameter("name");
			Long idAnotacion = Long.parseLong(request.getParameter("id"));
			Part filePart = request.getPart("file");
							
			final String updateArchivoSql = "UPDATE \"BSCL.Portafolio.BD::POR.Anotacion\" SET \"Archivo\" = ?, \"NombreArchivo\" = ? WHERE \"IdAnotacion\" = ?;";
			try(PreparedStatement pstmt = connection.prepareStatement(updateArchivoSql)){
				pstmt.setBinaryStream(1, filePart.getInputStream());
				pstmt.setString(2, name);					
				pstmt.setLong(3, idAnotacion);
				pstmt.execute();
				connection.commit();
				return "OK";
			} 
			
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ServletException e) {
			e.printStackTrace();
		}
		return "NOK";
	}

	@Override
	public void download(HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {
		logger.info("INICIO::AnotacionPortafolioConcrete.download");
		Long idAnotacion = Long.parseLong(request.getParameter("id"));
		String origen = request.getParameter("origen");
		
		if("SEBRA".equals(origen)) {
			boolean debug = false;
			
			String DBdriverSebra= "";
			String DBuserSebra 	= "";
			String DBpassSebra 	= "";
			String DBurlSebra 	= "";
			String DBpropSebra	= "";
			String DBkeySebra	= "";
			
			Properties prop = new Properties();
			InputStream input;
			try {
	        	if( debug ) {
	                input = new FileInputStream("C:\\GIT\\OPTIMUS\\workspace\\optimus-corredores-dev-workspace\\OptimusDevJava\\optimus.properties");
	            } else {
	            	String rutaArchivoProperties = Utils.getConfiguracion(connection, 1L, "PLA.RutaArchivosConfiguracion");        	
	            	input = new FileInputStream(rutaArchivoProperties+"optimus.properties");
	            	logger.info("Configuracion optimus.properties cargada");
	            }
	        	
	        	prop.load(input);
	        	DBdriverSebra=prop.getProperty("DBdriverSebra");
	            DBuserSebra=prop.getProperty("DBuserSebra");
	            DBpassSebra=prop.getProperty("DBpassSebra");
	            DBurlSebra=prop.getProperty("DBurlSebra");
	            DBpropSebra=prop.getProperty("DBpropSebra");
	            DBkeySebra = prop.getProperty("DBkeySebra");
	            input.close();
	        } catch(Exception ex) {
	        	logger.info(ex);
	        }
	        
	        @SuppressWarnings("resource")
			BasicDataSource dsTarget = new BasicDataSource();            
	        try {                                                
		        dsTarget.setDriverClassName(DBdriverSebra);
		        dsTarget.setUsername(decrypt(DBuserSebra, DBkeySebra));
		        dsTarget.setPassword(decrypt(DBpassSebra, DBkeySebra));
		        dsTarget.setUrl(DBurlSebra);          
		        dsTarget.setConnectionProperties(DBpropSebra);
		        dsTarget.setDefaultAutoCommit(true);
		        logger.info("Conectado a Sebra");
	        } catch( Exception e ){
	        	logger.info("ERROR NO SE PUEDE OBTENER CONEXION SEBRA DB: " + e);
	        	logger.info(e.toString());                                            
	        }
	        
	        try( Connection conRU = dsTarget.getConnection() ) {
	            logger.info("Consulta de data generada en sebra");
	            String selectFileSebra = "select AA.adj_nombre AS \"NombreArchivo\", AA.adj_adjunto AS \"Archivo\" FROM DETALLE_ANOTACION AS DA LEFT JOIN ANOTACION_ADJUNTO AS AA ON AA.dan_id = DA.dan_id WHERE DA.dan_id = ?";
	            try (PreparedStatement st=conRU.prepareStatement(selectFileSebra)) {
	            	st.setLong(1, idAnotacion);
	            	try  (ResultSet rs=st.executeQuery();) {
	            		while(rs.next()){
	            			try(InputStream is = rs.getBlob(2).getBinaryStream()){
	        					IOUtils.copy(is,response.getOutputStream());
	        				} catch (IOException e) {
	        					e.printStackTrace();
	        				}
	            		}
	            		response.setHeader("Content-disposition","inline; filename="+getFileName());
	        			response.setHeader("Cache-Control", "private, no-store, no-cache, must-revalidate");//HTTP 1.1
	        			response.setHeader("Pragma", "no-cache");//HTTP 1.0
	        			response.getOutputStream().flush();
	            	}
	            }
	            conRU.prepareStatement(selectFileSebra);
	            
	            logger.info("Se agregaron los archivos de las anotaciones de Sebra.");
			} catch( Exception er ) {
				logger.info("ERROR NO SE PUEDE CONECTAR BD EN SEBRA: " + er);
				logger.info(er.toString());                                                    
			}
		} else {
			final String selectFile = "select \"NombreArchivo\", \"Archivo\" from \"BSCL.Portafolio.BD::POR.Anotacion\" where \"IdAnotacion\" = ?";
			try(PreparedStatement pstmt = connection.prepareStatement(selectFile)){
				pstmt.setLong(1, idAnotacion);
				ResultSet rs = pstmt.executeQuery();
				while(rs.next()){
					try(InputStream is = rs.getBlob(2).getBinaryStream()){
						IOUtils.copy(is,response.getOutputStream());
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				response.setHeader("Content-disposition","inline; filename="+getFileName());
				response.setHeader("Cache-Control", "private, no-store, no-cache, must-revalidate");//HTTP 1.1
				response.setHeader("Pragma", "no-cache");//HTTP 1.0
				response.getOutputStream().flush();
			}
		}
		logger.info("FIN::AnotacionPortafolioConcrete.download");
	}

	@Override
	public void setConnection(Connection connection) {
		this.connection = connection;
	}

	@Override
	public String getFileName() {
		return this.filename;
	}

	@Override
	public String upload(HttpServletRequest request, HttpServletResponse response) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public static String decrypt(String encryptedText, String key) {
        try {
            Cipher cipher = Cipher.getInstance(algorithm);
            cipher.init(Cipher.DECRYPT_MODE, getKeyFromText(key));
            byte[] decodedBytes = Base64.getDecoder().decode(encryptedText); // Decodificamos desde Base64
            byte[] decryptedBytes = cipher.doFinal(decodedBytes);
            return new String(decryptedBytes);
        } catch (Exception e) {
            logger.error("Error al descifrar: ", e);
            return null;
        }
    }
	
	public static Key getKeyFromText(String keyText) {
        byte[] keyBytes = new byte[24];
        byte[] textBytes = keyText.getBytes();
        System.arraycopy(textBytes, 0, keyBytes, 0, Math.min(textBytes.length, keyBytes.length));
        return new SecretKeySpec(keyBytes, algorithm);
    }
}
