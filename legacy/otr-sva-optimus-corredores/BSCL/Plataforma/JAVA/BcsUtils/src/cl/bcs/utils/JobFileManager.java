package cl.bcs.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.Time;
import java.util.Properties;
import java.util.Vector;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class JobFileManager implements JavaCall{
	private final Log log = new Log("FTP.JobFileManager");
	
	private static String SFTP_PROTOCOLO = "SFTP";
	
	public static class EntradaJobFileManager {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_Directorio;
		public String p_Archivo;
		public String p_Destino;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Conexion {
		public String IdCodigoTipo;
		public String Extension;
		public String Ip;
		public Long Puerto;
		public String Carpeta;
		public String Usuario;
		public String Clave;
		public String Protocolo;
		public String ExpresionArchivo;
		public String Elimina;
		public String Java;
		public String EntradaSalida;
		public Time HoraDesde;
		public Time HoraHasta;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = new Gson();		
		EntradaJobFileManager in=gson.fromJson(jin.toString() ,EntradaJobFileManager.class);
		logica(con,in);
		//JsonParser parser 	= new JsonParser();
        JsonObject dataout	= new JsonObject();//parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public void logica(Connection con, EntradaJobFileManager entrada) throws Exception {
		log.write("COMIENZA CARGA DE ARCHIVOS SFTP DESDE NODO " + entrada.p_Destino.toUpperCase());
		String Glosa = "";
		
		//String rutaArchivosConfiguracion = System.getProperty("user.dir") + "/" ; //TODO Pruebas en Local
		String rutaArchivosConfiguracion = Utils.getConfiguracion(con, entrada.p_IdEmpresa, "PLA.RutaArchivosConfiguracion");
		
		
		Properties prop = new Properties();
        try { 										 
			InputStream input = new FileInputStream(rutaArchivosConfiguracion + "jobFile.properties");
			prop.load(input);
		} catch (FileNotFoundException e) {
			Glosa = "ARCHIVO_CONFIGURACION_NO_ENCONTRADO";
			log.write(Glosa,e);
			throw e;
		} catch (IOException e) {
			Glosa = "ARCHIVO_CONFIGURACION_PROBLEMA_LECTURA";
			log.write(Glosa,e);
			throw e;
		}
        
        String Nodo = entrada.p_Destino.toUpperCase(); //Especifica si ir a buscar la configuracion del nodo Primario o Secundario
        Conexion conexion = new Conexion();
		conexion.Puerto 	= Long.parseLong((String) prop.get(Nodo + ".PORT"));
		conexion.Usuario 	= (String) prop.get(Nodo + ".USER");
		String encyptedKey = "";
		try {
			encyptedKey = Utils.getConfiguracion(con, entrada.p_IdEmpresa, "PLA.Clave" + entrada.p_Destino);
		} catch (Exception e) {
			Glosa = "PARAMETRO_CONFIGURACION_NO_ENCONTRADO: " + "PLA.Clave" + entrada.p_Destino;
			log.write(Glosa,e);
			throw e;
		}
		conexion.Clave 		= Crypto.decrypt(encyptedKey, CryptoUtils.CRYPTO_KEY);
		conexion.Ip 		= (String) prop.get(Nodo + ".HOST");
		conexion.Protocolo 	= (String) prop.get(Nodo + ".PROTOCOL");
		String privateKey	= (String) prop.get(Nodo + ".KEY");	
		
		conexion.Carpeta = entrada.p_Directorio;
		if(entrada.p_Archivo.contains("/")) {
			conexion.Carpeta += entrada.p_Archivo.substring(0, entrada.p_Archivo.indexOf("/"));
			conexion.ExpresionArchivo = entrada.p_Archivo.substring(entrada.p_Archivo.indexOf("/") + 1, entrada.p_Archivo.length());
		}
		else {
			conexion.ExpresionArchivo = entrada.p_Archivo;
		}
				
		JSch jsch = new JSch();
		Session session = null;
		ChannelSftp sftpChannel = null;
		if(privateKey != null) jsch.addIdentity(privateKey);		
		
		try {
			session = getSession(conexion, jsch);
			log.write("Session Connected: [" + session.getUserName() + "@" + session.getHost() + "]");
			
			sftpChannel = (ChannelSftp) getChannel(session);
			log.write("Channel Connected: [" + sftpChannel.isConnected() + "]");
			
			log.write("Parametros busqueda:", conexion.Protocolo, conexion.Carpeta, conexion.ExpresionArchivo);
			if(SFTP_PROTOCOLO.equals(conexion.Protocolo)) {
				if(null != sftpChannel) {
					//Lista los archivos en el directorio
					@SuppressWarnings("unchecked")
					Vector<String> vectorArray = sftpChannel.ls(conexion.Carpeta);
					if (null != vectorArray) {
						for (int i = 0; i < vectorArray.size(); i++) {
							Object obj = vectorArray.elementAt(i);
							if (obj instanceof LsEntry) {
								LsEntry entry = (LsEntry) obj;
								if (!entry.getAttrs().isDir()) {
									if(null != entry.getFilename() && entry.getFilename().matches(conexion.ExpresionArchivo)) {
										String filePath = "/" + conexion.Carpeta + "/" + entry.getFilename();
										Glosa = "Archivo cargado correctamente desde SFTP: " + filePath;
										log.write(Glosa);
										//sftpChannel.get(filePath, System.getProperty("user.dir")); //TODO Pruebas en Local
										sftpChannel.get(filePath, filePath); //Se descarga en la misma ruta con el mismo nombre
										break;
									}
								}
							}
						}
					}
				}
			}
		} catch (JSchException e) {
			Glosa = "Error al crear la session o el canal SFTP!";
			log.write(Glosa,e);
			throw e;
		} catch (Exception e) {
			Glosa = "Error en FileManager.loadFilesFromSftp()!";			
        	log.write(Glosa);
			e.printStackTrace();
			throw e;
		} finally {
			if(null != sftpChannel) {
	            sftpChannel.exit();
	            sftpChannel.disconnect();
			}
			if(null != session) {
				session.disconnect();
			}	
        }
        log.write("FINALIZA CARGA DE ARCHIVOS SFTP DESDE NODO " + entrada.p_Destino.toUpperCase());
	}
	
	private Session getSession(Conexion conexion, JSch jsch) throws JSchException {
		Session session = null;
		conexion.Puerto = (null != conexion.Puerto && conexion.Puerto > 0 ? conexion.Puerto : 22);
		log.write("Parametros Conexion:",conexion.Usuario, conexion.Ip, conexion.Puerto.intValue());
		//log.write("Clave Conexion:",conexion.Clave);
		session = jsch.getSession(conexion.Usuario, conexion.Ip, conexion.Puerto.intValue());
		if(null != session) {
			session.setConfig("StrictHostKeyChecking", "no");
			session.setPassword(conexion.Clave);
			session.connect();
		}
		return session;
	}
	
	private Channel getChannel(Session session) throws JSchException {
		Channel channel = null;
		if(null != session && session.isConnected()) {
			channel = session.openChannel("sftp");
			channel.connect();
		}
		return channel;
	}
	
	//TODO Falta especificar que se trate de una conexion SOLO LECTURA
	//TODO Falta especificar el directorio /mnt como unico destino posible de la conexion
	//TODO Falta ver implementar KEY para los server que lo requieran
	
}
