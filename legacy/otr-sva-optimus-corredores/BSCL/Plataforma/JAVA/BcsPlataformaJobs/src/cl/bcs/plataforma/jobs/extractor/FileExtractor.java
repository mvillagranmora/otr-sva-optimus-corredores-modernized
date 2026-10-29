package cl.bcs.plataforma.jobs.extractor;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import java.util.Properties;

import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPConnectionClosedException;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileFilter;
import org.apache.commons.net.ftp.FTPReply;
import org.sql2o.Sql2o;

import cl.bcs.utils.Log;


public class FileExtractor {
	
	class OptiFile {
		String tableName;
		Date fileDate;
		FTPFile leFile;
	}
	
	private String ftpHostname = "";
	private int ftpPort = 0;
	private String ftpUser = "";
	private String ftpPassword = "";
	private String ftpFolder = "";
	private Long companyId = 0L;

	private FTPClient ftp;
	
	private boolean toArchive = false;
	private Map<String,File> files;
	
	private final String MASTER_TABLE = "\"BSCL.Plataforma.BD::PLA.ExtractorDeArchivo\"";
	private final String DETAIL_TABLE = "\"BSCL.Plataforma.BD::PLA.DetalleExtractorDeArchivo\"";
	
	private String schema = "BSCL";
	private DataSource dataSource = null;
	private Sql2o sql2o = null;
	private Log log;
	
	public FileExtractor(String ftpHostname, String ftpUser, String ftpPassword, Properties prop,String schema, Sql2o sql2ox, Long idEmpresa, String folder, Log log) {
		this.log = log;
		configureConnection(ftpHostname,ftpUser,ftpPassword);
		configureFileExtractor(prop);
		setDataSource(sql2ox);
		this.ftpFolder = folder;
		this.companyId = idEmpresa;
	}
	
	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(Sql2o sql2o) {
		this.sql2o = sql2o;
		this.dataSource = this.sql2o.getDataSource();
	}
	
	public void setDataSource(DataSource dataSource) {
		this.sql2o = new Sql2o(dataSource);
		this.dataSource = this.sql2o.getDataSource();
	}
	
	public void setDataSource(String schema, DataSource dataSource) {
		this.schema = schema;
		setDataSource(dataSource);
	}
	
	public void setDataSource(String database, String schema, String user, String password) {
		this.schema = schema;
		this.sql2o = new Sql2o(database + schema,user, password);
		this.dataSource = this.sql2o.getDataSource();
	}
	
	public boolean configureFileExtractor(Properties config) {
		String archive = config.getProperty("archive");
		toArchive = (archive != null && archive.equals("true")) ? true : false;
				
		files = new HashMap<String,File>();
		for (Entry<Object, Object> entry : config.entrySet() ) {
			String[] prop = entry.getKey().toString().split("\\.");
			if (prop.length == 3 && prop[0].equals("file")) {
				if (!files.containsKey(prop[1])) {
					files.put(prop[1], new File());
				}
				switch (prop[2]) {
				case "extension" :
					files.get(prop[1]).setExtension(entry.getValue().toString());
					break;
				case "format" :
					files.get(prop[1]).setFormat(entry.getValue().toString());
					break;
				}
			}
		}
		
		return true;
	}
	
	public boolean configureConnection(String ftpHostname, String ftpUser, String ftpPassword) {
		this.ftpHostname = ftpHostname;
		this.ftpPassword =  ftpPassword;
		this.ftpUser = ftpUser;
		this.ftpPort = -1;
		return true;
	}
	
	public boolean loadFiles() {
		boolean error = false;

		ftp = new FTPClient();
		log.write("COMIENZA CARGA DE ARCHIVOS");

		ftp.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out), true));

		try {
			int reply;
			if (ftpPort > 0) {
				ftp.connect(ftpHostname, ftpPort);
			} else {
				ftp.connect(ftpHostname);
			}
			log.write("Connected to " + ftpHostname + " on " + (ftpPort > 0 ? ftpPort : ftp.getDefaultPort()));

			reply = ftp.getReplyCode();

			if (!FTPReply.isPositiveCompletion(reply)) {
				ftp.disconnect();
				log.write("FTP server refused connection.");
				return false;
			}

		} catch (IOException e) {
			log.write("ERROR",e);
			if (ftp.isConnected()) {
				try {
					ftp.disconnect();
				} catch (IOException f) {
					log.write("ERROR",f);
				}
			}
			log.write("Could not connect to server.");
			return false;
		}

		try {

			if (ftp.login(ftpUser, ftpPassword)) {

				log.write("Remote system is " + ftp.getSystemType());

				FTPFileFilter filter = new FTPFileFilter() {
					@Override
					public boolean accept(FTPFile ftpFile) {
						boolean fileInConfiguration = false;
						if (ftpFile == null) {
							log.write("FILE NULL");
							return false;
						}
						
						for (Entry<String, File> entry : files.entrySet() ) {
							if(ftpFile.getName() != null && ftpFile.getName().startsWith(entry.getKey())) {
								fileInConfiguration = true;
							}
						}
						return (ftpFile.isFile() && ftpFile.getName() != null &&  !ftpFile.getName().endsWith("_old") && fileInConfiguration);
					}
				}; 
				
				ftp.enterLocalPassiveMode();
				log.write("OBTENIENDO LISTA DE ARCHIVOS");
				
				boolean changedOk = ftp.changeWorkingDirectory(this.ftpFolder);
				if (!changedOk) {
					log.write("ERROR AL CAMBIAR CARPETA");
				}
				
				FTPFile[] csvFiles =  ftp.listFiles(null, filter);
				
				//para buscar el ultimo archivo
				HashMap<String,OptiFile> optiFiles = new HashMap<String,OptiFile>();
				ArrayList<FTPFile> oldFiles = new ArrayList<>();	//lista de archivos para renombrar
				
				for (FTPFile f : csvFiles) {
					String[] splits = f.getName().split("[_.]");
					SimpleDateFormat parser=new SimpleDateFormat("yyyyMMdd");
					Date date = null;
					try {
						date = parser.parse(splits[1]);
						
						OptiFile opf = new OptiFile();
						opf.leFile = f;
						opf.tableName = splits[0];
						opf.fileDate = date;
						
						//existe archivo?
						if (!optiFiles.containsKey(splits[0])) {
							optiFiles.put(opf.tableName, opf);
						}
						else {
							//es el ultimo?
							OptiFile actual = optiFiles.get(splits[0]);
							if (actual.fileDate.before(opf.fileDate)) {
								oldFiles.add(actual.leFile);
								optiFiles.put(opf.tableName, opf);
							}
						}
					} catch (ParseException e) {
						e.printStackTrace();
						//archivo mal formateado, le cambio el nombre
						oldFiles.add(f);
						
					}
					
					
				}
				
				Iterator it = optiFiles.entrySet().iterator();
				while (it.hasNext()) {
					 Map.Entry pair = (Map.Entry)it.next();
					 OptiFile opf = (OptiFile) pair.getValue();
					 FTPFile f = opf.leFile;

					log.write("CARGANDO ARCHIVO> " + f.getName() + " ; " + f.getSize() + " ; " + f.getType() + " ; "
							+ f.getTimestamp().getTime().toString());
					
					Pattern p = Pattern.compile("(.*)\\_(.*)\\.(.*)");
					Matcher m = p.matcher(f.getName());
					String pattern = "";
					
					if(m.find() && files.containsKey(pattern = m.replaceFirst("$1"))) {
					//	String fileTimestamp = m.replaceFirst("$2");
						String fileExtension = m.replaceFirst("$3");
						File file = files.get(pattern);
						
						// BCSCSV Files
						if (file.getFormat().equals("bcscsv") && fileExtension.equals(file.getExtension())) {
							ByteArrayOutputStream output = new ByteArrayOutputStream();

							ftp.retrieveFile(f.getName(), output);

							byte[] byteArray = output.toByteArray();
							ByteArrayInputStream bais = new ByteArrayInputStream(byteArray);

							BufferedReader reader = new BufferedReader(new InputStreamReader(bais));
							String line = null;
							int n = -1;
							int recs = 0;
							Timestamp fechaCarga = new Timestamp(new Date().getTime());
							
							long t1 = System.currentTimeMillis();
							try (org.sql2o.Connection conn = this.sql2o.beginTransaction()) {
								
								org.sql2o.Query detailInsert = conn.createQuery(getDetailInsert(), false);
							
								while ((line = reader.readLine()) != null) {
									if (line.indexOf("##") > -1) {
										reader.skip(byteArray.length);
										break;
									}
									n++;
									
									if (n == 0) {
										try {
											recs = Integer.parseInt(line.substring(0, line.indexOf(';')));
										} catch (java.lang.NumberFormatException e) {
											recs = -1;
										}
										conn.createQuery(getMasterInsert(), false)
												.addParameter("idEmpresa", this.companyId)
												.addParameter("archivo", f.getName())
												.addParameter("fechaCarga", fechaCarga)
												.addParameter("idEstado", "CAR")
												.addParameter("totalDeLineas", recs)
												.addParameter("formato", file.getFormat())
												.addParameter("destino", pattern)
												.executeUpdate();
									}
									else {
										detailInsert
											.addParameter("idEmpresa", this.companyId)
											.addParameter("archivo", f.getName())
											.addParameter("fechaCarga", fechaCarga)
											.addParameter("numeroDeLinea", n)
											.addParameter("linea", line)
											.addParameter("procesado", "N")
											.addParameter("destino", pattern)
											.addToBatch();
									}
	
								}
								conn.createQuery(getMasterUpdate(), false)
									.addParameter("idEmpresa", this.companyId)
									.addParameter("archivo", f.getName())
									.addParameter("totalDeLineas", n).executeUpdate();
									
								detailInsert.executeBatch();
								conn.commit();
								//add file to old file list
								oldFiles.add(f);
							}
							long t2 = System.currentTimeMillis();
							log.write("> ------------------------------------------");
							log.write("> Database time: " + (t2 - t1) + " ms.");
							log.write("> ------------------------------------------");
							
							output.close();
							reader.close();
							bais.close();
							
						} // End BCSCSV Files
					}
				}
				
				//rename old files
				for (Iterator iterator = oldFiles.iterator(); iterator.hasNext();) {
					FTPFile ftpFile = (FTPFile) iterator.next();
					if (this.toArchive) {
						ftp.rename(ftpFile.getName(), ftpFile.getName() + "_old");
					}
				}
				
				ftp.noop();
				ftp.logout();

			} else {
				ftp.logout();
				error = true;
			}

		} catch (FTPConnectionClosedException e) {
			error = true;
			log.write("Server closed connection.");
			log.write(e.getMessage());
		} catch (IOException e) {
			error = true;
			log.write("IOEXCEPTION",e.getMessage());
		}catch (Exception e) {
			error = true;
			log.write("EXCEPTION",e.getMessage());
		} finally {
			if (ftp.isConnected()) {
				try {
					ftp.disconnect();
				} catch (IOException f) {
					log.write("FINALLY IOEXCEPTION",f.getMessage());
				}
			}
		}
		
		return error;
	}
	
	/**
	 * Parameters: idEmpresa, archivo, fechaCarga, idEstado, totalDeLineas, formato, :destino
	 */
	private String getMasterInsert() {
		return "INSERT INTO " + this.schema + "." + this.MASTER_TABLE
			+ " ( \"IdEmpresa\", \"Archivo\", \"FechaCarga\", \"IdEstado\", \"TotalDeLineas\", \"Formato\", \"Destino\" )"
			+ " VALUES "
			+ " ( :idEmpresa, :archivo, :fechaCarga, :idEstado, :totalDeLineas, :formato, :destino ) ";
	}
	
	private String getMasterUpdate() {
		return "UPDATE " + this.schema + "." + this.MASTER_TABLE
			+ " SET \"TotalDeLineas\" = :totalDeLineas  "
			+ " WHERE \"IdEmpresa\" = :idEmpresa AND \"Archivo\" = :archivo;";
	}
	
	/**
	 * Parameters: idEmpresa, archivo, numeroDeLinea, linea, procesado, destino
	 */
	private String getDetailInsert() {
		return "INSERT INTO " + this.schema + "." + this.DETAIL_TABLE
			+ " ( \"IdEmpresa\", \"Archivo\",\"FechaCarga\", \"NumeroDeLinea\", \"Linea\", \"Procesado\", \"Destino\" )"
			+ " VALUES "
			+ " ( :idEmpresa, :archivo, :fechaCarga, :numeroDeLinea, :linea, :procesado, :destino ) ";
	}
	
}
