package cl.bcs.plataforma.ftp;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import cl.bcs.plataforma.pla.InsertarLogServicio;
import cl.bcs.plataforma.pla.InsertarLogServicio.RegistroLog;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;

public class FileManager implements JavaCall{
	
	private final Logger logger = LogManager.getLogger(FileManager.class);
	private final String SQL_SELECT_MAESTRO = loadResourceToString("cl/bcs/plataforma/ftp/sql/FTP_SelectMaestro.sql");
	private final String SQL_INSERT_CABECERA = loadResourceToString("cl/bcs/plataforma/ftp/sql/FTP_InsertTableCabecera.sql");
	private final String SQL_INSERT_DETALLE = loadResourceToString("cl/bcs/plataforma/ftp/sql/FTP_InsertTableDetalle.sql");
	private final String SQL_SELECT_ID_CABECERA = loadResourceToString("cl/bcs/plataforma/ftp/sql/FTP_SelectIdCabecera.sql");
	private final String SQL_SELECT_CABECERA = loadResourceToString("cl/bcs/plataforma/ftp/sql/FTP_SelectCabecera.sql");
	private final String SQL_SELECT_DETALLE = loadResourceToString("cl/bcs/plataforma/ftp/sql/FTP_SelectDetalle.sql");
	private final String SQL_UPDATE_CABECERA = loadResourceToString("cl/bcs/plataforma/ftp/sql/FTP_UpdateCabecera.sql");
	
	private static String FTP_PROTOCOLO = "FTP";
	private static String SFTP_PROTOCOLO = "SFTP";
	private static String ESTADO_PENDIENTE = "PEN";
	private static String ESTADO_PROCESADO = "PRO";
	private static String ENTRADA = "E";
	private static String SALIDA = "S";
	private static String ELIMINAR_ARCHIVO = "S";
	private static String MOVER_ARCHIVO = "M";
	private static String CARPETA_PROCESADOS = "Procesados";
	
	public static class EntradaFileManager extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
	}
	
	public static class SalidaFileManager extends BcsPojo {
		
		public Long P_OUT_ERRORESCONTADOR;
		public List <MensajeSalida> P_OUT_ERRORESDETALLE;
		
		public SalidaFileManager(){
			this.P_OUT_ERRORESCONTADOR=0L;
			this.P_OUT_ERRORESDETALLE = new ArrayList<MensajeSalida>(); 
		}
	}
	
	public static class Maestro extends BcsPojo {
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
		
	}
	
	public static class Cabecera extends BcsPojo {
		public Long IdCabecera;
		public String IdCodigoTipo;
		public String IdEstado;
		public java.sql.Timestamp FechaCreacion;
		public Long IdUsuario;
		public String NombreArchivo;
		
		public Cabecera() {
			this.IdCabecera = 0L;
			this.IdCodigoTipo = "";
			this.IdEstado = "";
			this.FechaCreacion = Fecha.toDateTime("0001-01-01");
			this.IdUsuario = 0L;
			this.NombreArchivo = "";
		}
	}
	
	public static class Detalle extends BcsPojo {
		public Long IdDetalle;
		public Long IdCabecera;
		public String Linea;
		public String IdTipoEntidad;
		public Long IdEntidad;
		
		public Detalle() {
			this.IdDetalle = 0L;
			this.IdCabecera = 0L;
			this.Linea = "";
			this.IdTipoEntidad = "";
			this.IdEntidad = 0L;
		}
	}
	
	public static class Id  extends BcsPojo {
		public Long IdCabecera;
		
		public Id() {
			this.IdCabecera = 0L;
		}
	}
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = new Gson();		
		EntradaFileManager in=gson.fromJson(jin.toString() ,EntradaFileManager.class);
		SalidaFileManager out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaFileManager logica(Connection con, EntradaFileManager entrada) throws Exception {
		logger.info("INICIO::FileManager");
		SalidaFileManager out = new SalidaFileManager();
		List<Maestro> maestroList = new ArrayList<Maestro>();
		HashMap<String,Object> params = new HashMap<String,Object>();
		SQL sql = new SQL();
		maestroList = sql.query(con, SQL_SELECT_MAESTRO, params, Maestro.class);
		String horaSistemaString = FechaDB.ObtenerHoraSistemaLocal(con, entrada.p_IdEmpresa);		
		Time horaSistema = Time.valueOf(horaSistemaString);
		Time cero = Time.valueOf("00:00:00");
		
		for(Maestro maestroTemp : maestroList){
			if(!(cero.equals(maestroTemp.HoraDesde) && cero.equals(maestroTemp.HoraHasta)) && (horaSistema.before(maestroTemp.HoraDesde) || horaSistema.after(maestroTemp.HoraHasta) )) {
				logger.info("INFO::FileManager :: Hora de Sistema fuera del Rango Horario para procesar archivos del Tipo " + maestroTemp.IdCodigoTipo);
				continue;
			}
			logger.info("INFO::FileManager :: Hora de Sistema dentro del Rango Horario para procesar archivos del Tipo " + maestroTemp.IdCodigoTipo);
			
			if(SALIDA.equals(maestroTemp.EntradaSalida) && FTP_PROTOCOLO.equals(maestroTemp.Protocolo)){
				logger.info("INFO::FileManager :: Se intentara crear Archivo " + maestroTemp.IdCodigoTipo);
				boolean create = createFtpFile(con,maestroTemp);
				if(!create){
					out.P_OUT_ERRORESCONTADOR = 0L;
					out.P_OUT_ERRORESDETALLE.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_NO_SE_PUDO_CREAR_ARCHIVO",new HashMap<String, String>(),con,entrada.p_IdEmpresa));
					logger.info("INFO::FileManager :: No se pudo crear Archivo " + maestroTemp.IdCodigoTipo);
				}
			}
			if(SALIDA.equals(maestroTemp.EntradaSalida) && SFTP_PROTOCOLO.equals(maestroTemp.Protocolo)){
				logger.info("INFO::FileManager :: Se intentara crear Archivo " + maestroTemp.IdCodigoTipo);
				boolean create = createSftpFile(con,maestroTemp,entrada.p_IdUsuario);
				if(!create){
					out.P_OUT_ERRORESCONTADOR = 0L;
					out.P_OUT_ERRORESDETALLE.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_NO_SE_PUDO_CREAR_ARCHIVO",new HashMap<String, String>(),con,entrada.p_IdEmpresa));
					logger.info("INFO::FileManager :: No se pudo crear Archivo " + maestroTemp.IdCodigoTipo);
				}
			}
			if(ENTRADA.equalsIgnoreCase(maestroTemp.EntradaSalida) && FTP_PROTOCOLO.equals(maestroTemp.Protocolo)){
				logger.info("INFO::FileManager :: Se intentara leer Archivo " + maestroTemp.IdCodigoTipo);
				boolean load = loadFiles(con,maestroTemp,entrada.p_IdUsuario);
				if(!load){
					out.P_OUT_ERRORESCONTADOR = 0L;
					out.P_OUT_ERRORESDETALLE.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_NO_SE_PUDO_LEER_ARCHIVO",new HashMap<String, String>(),con,entrada.p_IdEmpresa));
					logger.info("INFO::FileManager :: No se pudo leer Archivo " + maestroTemp.IdCodigoTipo);
				}
			}
			if(ENTRADA.equalsIgnoreCase(maestroTemp.EntradaSalida) && SFTP_PROTOCOLO.equals(maestroTemp.Protocolo)){
				logger.info("INFO::FileManager :: Se intentara leer Archivo " + maestroTemp.IdCodigoTipo);
				boolean load = loadFilesFromSftp(con,maestroTemp,entrada.p_IdUsuario);
				if(!load){
					out.P_OUT_ERRORESCONTADOR = 0L;
					out.P_OUT_ERRORESDETALLE.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_NO_SE_PUDO_LEER_ARCHIVO",new HashMap<String, String>(),con,entrada.p_IdEmpresa));
					logger.info("INFO::FileManager :: No se pudo leer Archivo " + maestroTemp.IdCodigoTipo);
				}
			}
		}
		
		logger.info("FIN::FileManager");
		return out;
	}

	private boolean createSftpFile(Connection conn, Maestro maestroTemp, Long p_IdUsuario) throws Exception {
		logger.info("INICIO::FileManager.createSftpFile - Comienza creacion de Archivos SFTP");
		InsertarLogServicio insertarLogService = new InsertarLogServicio();
		List<RegistroLog> lstDatosLog = new ArrayList<RegistroLog>();
		boolean resultCreation = false;
		JSch jsch = new JSch();
		SQL sql = new SQL();
		Session session = null;
		ChannelSftp sftpChannel = null;
		try {
			session = getSession(maestroTemp, jsch);
			sftpChannel = (ChannelSftp) getChannel(session);
			if(null != sftpChannel) {
				logger.info("INFO::FileManager.createSftpFile - Se establece conexion con SFTP");
				HashMap<String,Object> params = new HashMap<String,Object>();
				params.put("IdCodigoTipo", maestroTemp.IdCodigoTipo);
				params.put("IdEstado", ESTADO_PENDIENTE);
				List<Cabecera> cabeceraList = sql.query(conn, SQL_SELECT_CABECERA, params, Cabecera.class);
				logger.info("INFO::FileManager.createSftpFile - Se encontraron " + cabeceraList.size() + " Cabeceras");
				
				for (Cabecera cabecera : cabeceraList) {
					logger.info("INFO::FileManager.createSftpFile - Se buscaran detalles para la Cabecera de Id: "+ cabecera.IdCabecera);
					params.clear();
					params.put("IdCabecera", cabecera.IdCabecera);
					List<Detalle> detalleList = sql.query(conn, SQL_SELECT_DETALLE, params, Detalle.class);
					List<String> lines = new ArrayList<String>();
					String line = "";
					logger.info("INFO::FileManager.createSftpFile - La Cabecera tiene " + detalleList.size() + " Detalles");
					
					logger.info("INFO::FileManager.createSftpFile - Se comienzan a agregar los Detalles");
					for (Detalle detalle : detalleList) {
						line = (null != line) ? detalle.Linea : "";
						lines.add(line);
					}
					logger.info("INFO::FileManager.createSftpFile - Detalles agregados");
					
					//Crea el archivo local
					Path file = Paths.get(cabecera.NombreArchivo);
					//Se escribe en el archivo local
					Files.write(file, lines, Charset.forName("UTF-8"));
					
					//Se sube archivo local al servidor SFTP
		            try (FileInputStream inputStream = new FileInputStream(cabecera.NombreArchivo)) {
			            String fileName = "/" + maestroTemp.Carpeta + "/" + cabecera.NombreArchivo;
			            sftpChannel.put(inputStream, fileName);
		            }
		            cabecera.IdEstado = ESTADO_PROCESADO;
		            String Glosa = "Archivo creado correctamente desde SFTP.";
		            lstDatosLog.add(insertarLogService.armaRegistroLog("PLA",cabecera.IdUsuario,"INFORMACION",Glosa,cabecera.IdCabecera.toString(),cabecera.NombreArchivo));
				}
				logger.info("INFO::FileManager.createSftpFile - Se actualizan las Cebeceras a estado Procesado");
				sql.batch(conn, SQL_UPDATE_CABECERA, cabeceraList, Cabecera.class);
	            resultCreation = true;
			}
		}catch (IOException ex) {
			String Glosa = "Error al agregar el archivo al servidor SFTP!";
			lstDatosLog.add(insertarLogService.armaRegistroLog("PLA",p_IdUsuario, "ERROR", Glosa, ex.getMessage(), ""));
			logger.info("INFO::FileManager.createSftpFile - " + Glosa, ex);
            throw ex;
        } catch (SQLException e) {
        	String Glosa = "Error al obtener la data desde BD!";
        	lstDatosLog.add(insertarLogService.armaRegistroLog("PLA",p_IdUsuario, "ERROR", Glosa, e.getMessage(), ""));
        	logger.info("INFO::FileManager.createSftpFile - " + Glosa, e);
        	throw e;
		} catch (JSchException e) {
			String Glosa = "Error al crear la session o el canal SFTP!";
			lstDatosLog.add(insertarLogService.armaRegistroLog("PLA",p_IdUsuario, "ERROR", Glosa, e.getMessage(), ""));
			logger.info("INFO::FileManager.createSftpFile - " + Glosa, e);
			throw e;
		} catch (Exception e) {
			String Glosa = "Error en FileManager.createSftpFile() !";
			lstDatosLog.add(insertarLogService.armaRegistroLog("PLA",p_IdUsuario, "ERROR", Glosa, e.getMessage(), ""));
			logger.info("INFO::FileManager.createSftpFile - " + Glosa, e);
			throw e;
		} finally {
			if(null != sftpChannel) {
				logger.info("INFO::FileManager.createSftpFile - Se desconecta del SFTP");
				sftpChannel.disconnect();
				sftpChannel.exit();
			}
			if(null != session) {
				logger.info("INFO::FileManager.createSftpFile - Se desconecta la Sesion");
				session.disconnect();
			}
			if(lstDatosLog.size() >= 1) {
				logger.info("INFO::FileManager.createSftpFile - Se insertan logs");
				insertarLogService.logica(conn,lstDatosLog);
			}
        }
		logger.info("FIN::FileManager.createSftpFile - Finaliza creacion de Archivos SFTP");
		return resultCreation;
	}

	public boolean createFtpFile(Connection conn, Maestro maestroTemp) throws Exception {
		logger.info("INICIO::FileManager.createFtpFile - Comienza creacion de Archivos FTP");
		boolean resultCreation = false;
		FTPClient ftpClient = new FTPClient();
		SQL sql = new SQL();
		try {
			if(connectToFtp(maestroTemp, ftpClient)) {
				logger.info("INFO::FileManager.createFtpFile - Se establece conexion FTP");
				HashMap<String,Object> params = new HashMap<String,Object>();
				params.put("IdCodigoTipo", maestroTemp.IdCodigoTipo);
				params.put("IdEstado", ESTADO_PENDIENTE);
				List<Cabecera> cabeceraList = sql.query(conn, SQL_SELECT_CABECERA, params, Cabecera.class);
				logger.info("INFO::FileManager.createFtpFile - Se encontraron " + cabeceraList.size() + " Cabeceras");
				
				for (Cabecera cabecera : cabeceraList) {
					logger.info("INFO::FileManager.createFtpFile - Se buscaran detalles para la Cabecera de Id: "+ cabecera.IdCabecera);
					params.clear();
					params.put("IdCabecera", cabecera.IdCabecera);
					List<Detalle> detalleList = sql.query(conn, SQL_SELECT_DETALLE, params, Detalle.class);
					List<String> lines = new ArrayList<String>();
					String line = "";
					logger.info("INFO::FileManager.createFtpFile - La Cabecera tiene " + detalleList.size() + " Detalles");
					
					logger.info("INFO::FileManager.createFtpFile - Se comienzan a agregar los Detalles");
					for (Detalle detalle : detalleList) {
						line = (null != line) ? detalle.Linea : "";
						lines.add(line);
					}
					logger.info("INFO::FileManager.createFtpFile - Detalles agregados");
					
					//Crea el archivo local
					Path file = Paths.get(cabecera.NombreArchivo);
					//Se escribe en el archivo local
					Files.write(file, lines, Charset.forName("UTF-8"));
					//Se sube archivo local al servidor FTP
		            try (FileInputStream inputStream = new FileInputStream(cabecera.NombreArchivo)) {
		                ftpClient.storeFile("/" + maestroTemp.Carpeta + "/" + cabecera.NombreArchivo, inputStream);
		            }
		            cabecera.IdEstado = ESTADO_PROCESADO;
				}
				logger.info("INFO::FileManager.createFtpFile - Se actualizan las Cebeceras a estado Procesado");
				sql.batch(conn, SQL_UPDATE_CABECERA, cabeceraList, Cabecera.class);
			}
            resultCreation = true;
        } catch (IOException ex) {
        	logger.info("INFO::FileManager.createFtpFile - Error al agregar el archivo al servidor FTP!", ex);
            throw ex;
        } catch (SQLException e) {
        	logger.info("INFO::FileManager.createFtpFile - Error al obtener la data desde BD!", e);
        	throw e;
		} catch (Exception e) {
			logger.info("INFO::FileManager.createFtpFile - Error en FileManager.createFile() !", e);
        	throw e;
		} finally {
			logger.info("INFO::FileManager.createFtpFile - Se desconecta del FTP");
            disconnectFtp(ftpClient);
        }
		logger.info("FIN::FileManager.createFtpFile - Finaliza creacion de Archivos FTP");
		return resultCreation;
	}
	
	public boolean loadFiles(Connection conn, Maestro maestroTemp, Long idUsuario) throws Exception {
		logger.info("INICIO::FileManager.loadFiles - Comienza carga de Archivos FTP");
		boolean resultLoad = false;
		FTPClient ftpClient = new FTPClient();
		SQL sql = new SQL();
		
		try {
            List<Cabecera> cabeceraList = new ArrayList<Cabecera>();
        	List<Detalle> detalleList = new ArrayList<Detalle>();
            Cabecera cabecera;
        	Detalle detalle;
        	
			if(FTP_PROTOCOLO.equals(maestroTemp.Protocolo)) {
				if(connectToFtp(maestroTemp, ftpClient)) {
					logger.info("INFO::FileManager.loadFiles - Se conecto al FTP");
					// Lists files and directories
					logger.info("INFO::FileManager.loadFiles - Se buscan archivos desde la ruta: "+ maestroTemp.Carpeta);
		            FTPFile[] files = ftpClient.listFiles(maestroTemp.Carpeta);
		            ByteArrayOutputStream output;
		            byte[] byteArray;
		            ByteArrayInputStream bais;
		            BufferedReader reader;
					String line = "";
					HashMap<String,Object> params = new HashMap<String,Object>();
		            
		            for (FTPFile ftpFile : files) {
		            	logger.info("INFO::FileManager.loadFiles - Existe archivo");
		            	if(null != ftpFile.getName() && ftpFile.getName().matches(maestroTemp.ExpresionArchivo) && 
		            			ENTRADA.equals(maestroTemp.EntradaSalida)) {
		            		logger.info("INFO::FileManager.loadFiles - Se cargara Cabecera para el Archivo con nombre " + ftpFile.getName());
							cabecera = new Cabecera();
							List<Id> selectId = sql.query(conn, SQL_SELECT_ID_CABECERA, params, Id.class);
							Long idCabecera = selectId.get(0).IdCabecera;
							cabecera.IdCabecera = idCabecera;
							cabecera.IdCodigoTipo = maestroTemp.IdCodigoTipo;
							cabecera.IdEstado = ESTADO_PENDIENTE;
							cabecera.FechaCreacion = new Timestamp(System.currentTimeMillis());
							cabecera.IdUsuario = idUsuario;
							cabecera.NombreArchivo = ftpFile.getName();
							cabeceraList.add(cabecera);	
							
							String filePath = "/" + maestroTemp.Carpeta + "/" + ftpFile.getName();
							
							logger.info("INFO::FileManager.loadFiles - Se leen las lineas del Archivo "+ cabecera.NombreArchivo);
							output = new ByteArrayOutputStream();
							ftpClient.retrieveFile(filePath, output);
							byteArray = output.toByteArray();
							bais = new ByteArrayInputStream(byteArray);
							reader = new BufferedReader(new InputStreamReader(bais));
							
							logger.info("INFO::FileManager.loadFiles - Se armara lista de Detalles");
							while(null != (line = reader.readLine())) {
								if(line.trim().isEmpty()) {
									continue;
								}
								detalle = new Detalle();
								detalle.IdCabecera = idCabecera;
								detalle.Linea = line;
								detalleList.add(detalle);
							}
							logger.info("INFO::FileManager.loadFiles - Se lista de Detalles completada con "+ detalleList.size() + "registros");
							
							if(null != cabeceraList && !cabeceraList.isEmpty() && 
									null != detalleList && !detalleList.isEmpty()) {
								logger.info("INFO::FileManager.loadFiles - Se insertan Cabecera y Detalles");
					            sql.batch(conn, SQL_INSERT_CABECERA, cabeceraList, Cabecera.class);
					            sql.batch(conn, SQL_INSERT_DETALLE, detalleList, Detalle.class);
					            
					            if(ELIMINAR_ARCHIVO.equalsIgnoreCase(maestroTemp.Elimina)) {
					            	logger.info("INFO::FileManager.loadFiles - Se debe Eliminar el Archivo de nombre " + cabecera.NombreArchivo + " desde el FTP");
									ftpClient.deleteFile(filePath);
								}
							}
						}
					}
				}
			}
            resultLoad = true;
        } catch (IOException ex) {
        	logger.info("INFO::FileManager.loadFiles - Error al extraer los archivos desde servidor FTP!", ex);
            ex.printStackTrace();
            throw ex;
        } catch (SQLException e) {
        	logger.info("INFO::FileManager.loadFiles - Error al insertar en BD!", e);
			e.printStackTrace();
			throw e;
		} catch (Exception e) {
			logger.info("INFO::FileManager.loadFiles - Error en FileManager.loadFiles() !", e);
			e.printStackTrace();
			throw e;
		} finally {
			logger.info("INFO::FileManager.loadFiles - Se desconecta del FTP");
            disconnectFtp(ftpClient);
        }
		logger.info("FIN::FileManager.loadFiles - Finaliza carga de Archivos FTP");
        return resultLoad;
	}
	
	private boolean loadFilesFromSftp(Connection conn, Maestro maestroTemp, Long p_IdUsuario) throws Exception {
		logger.info("INICIO::FileManager.loadFilesFromSftp - Comienza carga de Archivos desde SFTP");
		InsertarLogServicio insertarLogService = new InsertarLogServicio();
		List<RegistroLog> lstDatosLog = new ArrayList<RegistroLog>();
		boolean resultLoad = false;
		JSch jsch = new JSch();
		SQL sql = new SQL();
		Session session = null;
		ChannelSftp sftpChannel = null;
		
		try {
			session = getSession(maestroTemp, jsch);
			sftpChannel = (ChannelSftp) getChannel(session);
            List<Cabecera> cabeceraList = new ArrayList<Cabecera>();
        	List<Detalle> detalleList = new ArrayList<Detalle>();
            Cabecera cabecera;
        	Detalle detalle;
        	String line = "";
			HashMap<String,Object> params = new HashMap<String,Object>();
        	
			if(SFTP_PROTOCOLO.equals(maestroTemp.Protocolo)) {
				if(null != sftpChannel) {
					logger.info("INFO::FileManager.loadFilesFromSftp - Se conecto al SFTP");
					//Lista los archivos en el directorio
					logger.info("INFO::FileManager.loadFilesFromSftp - Se buscan archivos desde la ruta: "+ maestroTemp.Carpeta);
					@SuppressWarnings("unchecked")
					Vector<String> vectorArray = sftpChannel.ls(maestroTemp.Carpeta);
					if (null != vectorArray) {
						logger.info("INFO::FileManager.loadFilesFromSftp - Se encontraron archivos para cargar");
						for (int i = 0; i < vectorArray.size(); i++) {
							Object obj = vectorArray.elementAt(i);
							if (obj instanceof LsEntry) {
								LsEntry entry = (LsEntry) obj;
								if (!entry.getAttrs().isDir()) {
									if(null != entry.getFilename() && entry.getFilename().matches(maestroTemp.ExpresionArchivo) && 
					            			ENTRADA.equals(maestroTemp.EntradaSalida)) {
										logger.info("INFO::FileManager.loadFilesFromSftp - Se cargara Cabecera para el Archivo con nombre " + entry.getFilename());
										cabecera = new Cabecera();
										List<Id> selectId = sql.query(conn, SQL_SELECT_ID_CABECERA, params, Id.class);
										Long idCabecera = selectId.get(0).IdCabecera;
										cabecera.IdCabecera = idCabecera;
										cabecera.IdCodigoTipo = maestroTemp.IdCodigoTipo;
										cabecera.IdEstado = ESTADO_PENDIENTE;
										cabecera.FechaCreacion = new Timestamp(System.currentTimeMillis());
										cabecera.IdUsuario = p_IdUsuario;
										cabecera.NombreArchivo = entry.getFilename();
										cabeceraList.add(cabecera);
										logger.info("INFO::FileManager.loadFilesFromSftp - Se agrego la Cabecera correctamente");
										
										String Glosa = "Archivo cargado correctamente desde SFTP.";
										lstDatosLog.add(insertarLogService.armaRegistroLog("PLA",cabecera.IdUsuario,"INFORMACION",Glosa,cabecera.IdCabecera.toString(),cabecera.NombreArchivo));
										String filePath = "/" + maestroTemp.Carpeta + "/" + entry.getFilename();
										
										logger.info("INFO::FileManager.loadFilesFromSftp - Se leen las lineas del Archivo "+ cabecera.NombreArchivo);
										InputStream in = sftpChannel.get(filePath);
										BufferedReader br = new BufferedReader(new InputStreamReader(in));
										
										logger.info("INFO::FileManager.loadFilesFromSftp - Se armara lista de Detalles");
						                while ((line = br.readLine()) != null) {
						                	if(line.trim().isEmpty()) {
						                		continue;
						                	}
						                	detalle = new Detalle();
											detalle.IdCabecera = idCabecera;
											detalle.Linea = line;
											detalleList.add(detalle);
						                }
						                logger.info("INFO::FileManager.loadFilesFromSftp - Se lista de Detalles completada con "+ detalleList.size() + "registros");
						                
						                if(null != cabeceraList && !cabeceraList.isEmpty() &&
												null != detalleList && !detalleList.isEmpty()) {
								            if(ELIMINAR_ARCHIVO.equalsIgnoreCase(maestroTemp.Elimina)) {
								            	logger.info("INFO::FileManager.loadFilesFromSftp - Se debe Eliminar el Archivo de nombre " + cabecera.NombreArchivo + " desde el SFTP");
								            	sftpChannel.rm(filePath);
											} else if(MOVER_ARCHIVO.equalsIgnoreCase(maestroTemp.Elimina)) {
												logger.info("INFO::FileManager.loadFilesFromSftp - Se debe mover el Archivo de nombre " + cabecera.NombreArchivo + " a la carpeta " + CARPETA_PROCESADOS + " en el SFTP");
												moveSftpFile(sftpChannel, maestroTemp.Carpeta, filePath, cabecera.NombreArchivo);
											}
										}
									}
								}
							}
						}
						if(null != cabeceraList && !cabeceraList.isEmpty() && 
								null != detalleList && !detalleList.isEmpty()) {
							logger.info("INFO::FileManager.loadFilesFromSftp - Se insertan Cabeceras y Detalles");
							sql.batch(conn, SQL_INSERT_CABECERA, cabeceraList, Cabecera.class);
							sql.batch(conn, SQL_INSERT_DETALLE, detalleList, Detalle.class);
			            }
					}
				}
			}
            resultLoad = true;
        } catch (IOException ex) {
        	String Glosa = "Error al extraer los archivos desde servidor SFTP!";
        	lstDatosLog.add(insertarLogService.armaRegistroLog("PLA",p_IdUsuario, "ERROR", Glosa, ex.getMessage(), ""));
        	logger.info("INFO::FileManager.loadFilesFromSftp - Error al extraer los archivos desde servidor SFTP!");
            ex.printStackTrace();
            throw ex;
        } catch (SQLException e) {
        	String Glosa = "Error al insertar en BD!";
        	lstDatosLog.add(insertarLogService.armaRegistroLog("PLA",p_IdUsuario, "ERROR", Glosa, e.getMessage(), ""));
        	logger.info("INFO::FileManager.loadFilesFromSftp - Error al insertar en BD!");
			e.printStackTrace();
			throw e;
		} catch (JSchException e) {
			String Glosa = "Error al crear la session o el canal SFTP!";
			lstDatosLog.add(insertarLogService.armaRegistroLog("PLA",p_IdUsuario, "ERROR", Glosa, e.getMessage(), ""));
			logger.info("INFO::FileManager.loadFilesFromSftp - Error al crear la session o el canal SFTP!");
			throw e;
		} catch (Exception e) {
			String Glosa = "Error en FileManager.loadFilesFromSftp()!";
			lstDatosLog.add(insertarLogService.armaRegistroLog("PLA",p_IdUsuario, "ERROR", Glosa, e.getMessage(), ""));
			logger.info("INFO::FileManager.loadFilesFromSftp - Error en FileManager.loadFilesFromSftp()!");
			e.printStackTrace();
			throw e;
		} finally {
			if(null != sftpChannel) {
				logger.info("INFO::FileManager.loadFilesFromSftp - Se desconecta del SFTP");
	            sftpChannel.exit();
	            sftpChannel.disconnect();
			}
			if(null != session) {
				logger.info("INFO::FileManager.loadFilesFromSftp - Se desconecta la Sesion");
				session.disconnect();
			}
			if(lstDatosLog.size() >= 1) {
				logger.info("INFO::FileManager.loadFilesFromSftp - Se insertan logs");
				insertarLogService.logica(conn,lstDatosLog);
			}	
        }
		logger.info("FIN::FileManager.loadFilesFromSftp - Finaliza carga de Archivos desde SFTP");
        return resultLoad;
	}
	
	private void moveSftpFile(ChannelSftp sftpChannel, String carpetaOrigen, String filePath, String nombreArchivo) throws SftpException {
		logger.info("INICIO::FileManager.moveSftpFile");
		String carpetaDestino = "/" + carpetaOrigen + "/" + CARPETA_PROCESADOS;
		try {
			sftpChannel.stat(carpetaDestino);
		} catch (SftpException e) {
			logger.info("INFO::FileManager.moveSftpFile - Carpeta " + carpetaDestino + " no existe, se crea");
			sftpChannel.mkdir(carpetaDestino);
		}

		String nombreDestino = nombreArchivo;
		if(existeArchivoSftp(sftpChannel, carpetaDestino + "/" + nombreDestino)) {
			nombreDestino = generarNombreArchivoDisponible(sftpChannel, carpetaDestino, nombreArchivo);
			logger.info("INFO::FileManager.moveSftpFile - Ya existe un archivo con el nombre " + nombreArchivo + " en " + carpetaDestino + ", se movera con el nombre " + nombreDestino);
		}

		String destinoPath = carpetaDestino + "/" + nombreDestino;
		sftpChannel.rename(filePath, destinoPath);
		logger.info("FIN::FileManager.moveSftpFile - Archivo movido a " + destinoPath);
	}

	private boolean existeArchivoSftp(ChannelSftp sftpChannel, String path) {
		try {
			sftpChannel.stat(path);
			return true;
		} catch (SftpException e) {
			return false;
		}
	}

	private String generarNombreArchivoDisponible(ChannelSftp sftpChannel, String carpetaDestino, String nombreArchivo) {
		String nombreBase = nombreArchivo;
		String extension = "";
		int idx = nombreArchivo.lastIndexOf('.');
		if(idx > 0) {
			nombreBase = nombreArchivo.substring(0, idx);
			extension = nombreArchivo.substring(idx);
		}
		String timestamp = new java.text.SimpleDateFormat("yyyyMMddHHmmss").format(new java.util.Date());
		String nuevoNombre = nombreBase + "_" + timestamp + extension;
		int contador = 1;
		while(existeArchivoSftp(sftpChannel, carpetaDestino + "/" + nuevoNombre)) {
			nuevoNombre = nombreBase + "_" + timestamp + "_" + contador + extension;
			contador++;
		}
		return nuevoNombre;
	}

	private boolean connectToFtp(Maestro maestroTemp, FTPClient ftpClient) throws Exception {
		logger.info("INICIO::FileManager.connectToFtp");
		boolean result = true;
        try {
        	maestroTemp.Puerto = (null != maestroTemp.Puerto && maestroTemp.Puerto.intValue() > 0 ? maestroTemp.Puerto : ftpClient.getDefaultPort());
			ftpClient.connect(maestroTemp.Ip, maestroTemp.Puerto.intValue());
			logger.info("INFO::FileManager.connectToFtp - Conectando a " + maestroTemp.Ip + " en puerto " + maestroTemp.Puerto);

	        int replyCode = ftpClient.getReplyCode();
	        if (!FTPReply.isPositiveCompletion(replyCode)) {
	        	logger.info("INFO::FileManager.connectToFtp - Server FTP " + maestroTemp.Ip + " en puerto " + maestroTemp.Puerto + " rechazo la conexion");
	            return false;
	        }

	        boolean success = ftpClient.login(maestroTemp.Usuario, maestroTemp.Clave);
	        if (!success) {
	        	logger.info("INFO::FileManager.connectToFtp - No se pudo loguear al server " + maestroTemp.Ip);
	            return false;
	        }
	        logger.info("INFO::FileManager.connectToFtp - Conectado al server " + maestroTemp.Ip);
		} catch (IOException e) {
			result = false;
			e.printStackTrace();
			throw e;
		}
        logger.info("FIN::FileManager.connectToFtp");
		return result;
	}

	private void disconnectFtp(FTPClient ftpClient) throws Exception {
		logger.info("INICIO::FileManager.disconnectFtp");
		try {
			logger.info("INFO::FileManager.disconnectFtp - Desconectando del server FTP");
		    if (ftpClient.isConnected()) {
		        ftpClient.logout();
		        ftpClient.disconnect();
		    }
		    logger.info("INFO::FileManager.disconnectFtp - Desconectado del server FTP");
		} catch (IOException ex) {
			logger.info("INFO::FileManager.disconnectFtp - Error desconectando del server FTP", ex);
		    ex.printStackTrace();
		    throw ex;
		}
		logger.info("FIN::FileManager.disconnectFtp");
	}
	
	private Session getSession(Maestro maestroTemp, JSch jsch) throws JSchException {
		logger.info("INICIO::FileManager.getSession");
		Session session = null;
		maestroTemp.Puerto = (null != maestroTemp.Puerto && maestroTemp.Puerto > 0 ? maestroTemp.Puerto : 22);
		logger.info("INFO::FileManager.getSession - Parametros Conexion - Usuario: " + maestroTemp.Usuario + " IP: " + maestroTemp.Ip + " Puerto: " + maestroTemp.Puerto.intValue());
		logger.info("INFO::FileManager.getSession - Clave Conexion: " + maestroTemp.Clave);
		session = jsch.getSession(maestroTemp.Usuario, maestroTemp.Ip, maestroTemp.Puerto.intValue());
		if(null != session) {
			logger.info("INFO::FileManager.getSession - Se obtiene Sesion");
			session.setConfig("StrictHostKeyChecking", "no");
			session.setPassword(maestroTemp.Clave);
			session.connect(30000);
		}
		logger.info("FIN::FileManager.getSession");
		return session;
	}
	
	private Channel getChannel(Session session) throws JSchException {
		logger.info("INICIO::FileManager.getChannel");
		Channel channel = null;
		if(null != session && session.isConnected()) {
			logger.info("INFO::FileManager.getChannel - Se conecta al channel");
			channel = session.openChannel("sftp");
			channel.connect();
		}
		logger.info("FIN::FileManager.getChannel");
		return channel;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");

		try (Connection con = PlataformaUtils.getPropertiesConnection("qa02")) {
		
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();			
			String jsonRF = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"FMM\",\"p_IdSolicitudAporteRescate\":[14330,14329]}";
					
			EntradaFileManager Objin = gson.fromJson(jsonRF, EntradaFileManager.class);
			
			FileManager  demo=new FileManager();				
 			
 			try {
 				SalidaFileManager ObjOut=demo.logica(con, Objin);
// 				con.commit();
 				System.out.println(ObjOut.toString());
 			}
 			catch(Exception er) {
				er.printStackTrace();	
				con.rollback();
			}
 			con.rollback(); 			
	    	}

		System.out.println("== END ==");
	}
}
