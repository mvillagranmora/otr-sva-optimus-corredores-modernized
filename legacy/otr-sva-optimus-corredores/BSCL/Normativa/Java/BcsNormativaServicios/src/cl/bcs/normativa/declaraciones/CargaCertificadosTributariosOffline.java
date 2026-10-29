package cl.bcs.normativa.declaraciones;

import java.io.File;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import org.apache.commons.collections.map.MultiKeyMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;

import cl.bcs.normativa.cartola.GestionarNomina.Nomina;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CargaCertificadosTributariosOffline implements JavaCall {
	private static final Logger logger = LogManager.getLogger(CargaCertificadosTributariosOffline.class);
	
	private static final String SQL_CREATE_LOCAL_TABLE = "CREATE LOCAL TEMPORARY ROW TABLE #CertificadosSebra(\"IdEmpresa\" BIGINT,\"DocumentoIdentidad\" NVARCHAR(100), \"CodigoPortafolio\" NVARCHAR(100), \"NombreArchivo\" NVARCHAR(5000),\"Esquema\" NVARCHAR(10), \"Version\" NVARCHAR(10),\"IdTipoCertificado\" NVARCHAR(6))";
	private static final String SQL_DROP_LOCAL_TABLE = "DROP TABLE #CertificadosSebra";
	private static final String SQL_INSERT_LOCAL_TABLE = "INSERT INTO #CertificadosSebra(\"IdEmpresa\",\"DocumentoIdentidad\", \"CodigoPortafolio\", \"NombreArchivo\",\"Esquema\",\"Version\",\"IdTipoCertificado\") VALUES(:IdEmpresa,:DocumentoIdentidad, :CodigoPortafolio, :NombreArchivo,:Esquema,:Version,:IdTipoCertificado)";
	private static final String SQL_QUERY_CLIENTES = 
			"SELECT CLI.\"IdCliente\", CLI.\"DocumentoIdentidad\", POR.\"IdPortafolio\", POR.\"CodigoPortafolio\", CS.\"NombreArchivo\", CS.\"Esquema\", CS.\"Version\",CS.\"IdEmpresa\",CS.\"IdTipoCertificado\" "
			+ "FROM \"BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente\" CLI "
			+ "LEFT JOIN \"BSCL.Portafolio.BD::POR.Portafolio\" POR ON POR.\"IdCliente\" = CLI.\"IdCliente\" "
			+ "LEFT JOIN #CertificadosSebra CS ON CLI.\"DocumentoIdentidad\" = CS.\"DocumentoIdentidad\" AND POR.\"CodigoPortafolio\" = CS.\"CodigoPortafolio\" "
			+ "WHERE POR.\"IdPortafolio\" IS NOT NULL";

	private final String SQL_INSERT_CERTIFICADO = "INSERT INTO \"BSCL.Normativa.BD::TRI.CertificadoClienteOffline\" "
			+ "(\"IdEmpresa\", \"Folio\",\"Esquema\", \"Version\", \"IdCliente\", \"IdPortafolio\", \"IdTipoCertificado\", \"NombreArchivo\",\"CertificadoEncoded\") "
			+ "VALUES(:IdEmpresa, 0,:Esquema, :Version, :IdCliente, :IdPortafolio, :IdTipoCertificado, :NombreArchivo, :CertificadoEncoded);";
	
	private static final DateTimeFormatter YEAR_MONTH_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM");
	private static final DateTimeFormatter ISO8601 = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	
	public static class ClienteCertificado{
		public Long IdEmpresa;
		public String DocumentoIdentidad;
		public String CodigoPortafolio;
		public Long IdCliente;
		public Long IdPortafolio;
		public String NombreArchivo;
		public String Esquema;
		public String Version;
		public String IdTipoCertificado;
		public byte[] CertificadoEncoded;
		
		public ClienteCertificado() {}
		
		public ClienteCertificado(String documentoIdentidad, String codigoPortafolio, String nombreArchivo,String esquema,String version, String idTipoCertificado,long idEmpresa) {
			this.DocumentoIdentidad = documentoIdentidad;
			this.CodigoPortafolio = codigoPortafolio;
			this.NombreArchivo = nombreArchivo;
			this.IdTipoCertificado = idTipoCertificado;
			this.Esquema = esquema;
			this.Version = version;
			this.IdEmpresa = idEmpresa;
		}
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public boolean logica(Connection conn, String rutaCertificado, String esquema, String version, String tipocertificado) throws Exception {
		logger.info("INIT");
		SQL sql = new SQL();
		
		SQL.exec(conn, SQL_CREATE_LOCAL_TABLE);
		logger.debug("Ruta base: " + rutaCertificado);
		String[] extensions = {"pdf"};
		logger.debug("Obtener archivos de la ruta base");
		Collection<File> files = FileUtils.listFiles(new File(rutaCertificado), extensions ,true);
		
		logger.debug("Parsear nombres de los archivos y asignar en la clase ClienteCertificado");
		List<ClienteCertificado> clientesCertificadoSebra = new ArrayList<>();
		HashMap<String,File> hashFiles = new HashMap<>();
		if (tipocertificado.equalsIgnoreCase("CERT21")) {	//DJ1894
			for (File file: files) {
				String filename = FilenameUtils.getBaseName(file.getName());
				String[] parsedName = filename.split("_");
				//1 rut, 2 cuenta
				//String documentoIdentidad, String codigoPortafolio, String NombreArchivo
				ClienteCertificado c = new ClienteCertificado(parsedName[1], parsedName[2], FilenameUtils.getName(file.getName()),esquema,version,tipocertificado,1L);
				clientesCertificadoSebra.add(c);
				hashFiles.put(c.NombreArchivo,file);
			}
		}
		
		if (tipocertificado.equalsIgnoreCase("CERT43")) { //DJ1922
			for (File file: files) {
				String filename = FilenameUtils.getBaseName(file.getName());
				String[] parsedName = filename.split("_");
				//1 rut, 2 cuenta
				//String documentoIdentidad, String codigoPortafolio, String NombreArchivo
				ClienteCertificado c = new ClienteCertificado(parsedName[1], parsedName[2], FilenameUtils.getName(file.getName()),esquema,version,tipocertificado,1L);
				clientesCertificadoSebra.add(c);
				hashFiles.put(c.NombreArchivo,file);
			}
		}
		
		if (tipocertificado.equalsIgnoreCase("CERT71")) { //1949
			for (File file: files) {
				String filename = FilenameUtils.getBaseName(file.getName());
				String[] parsedName = filename.split("AV");
				//1 rut, 2 cuenta
				//String documentoIdentidad, String codigoPortafolio, String NombreArchivo
				
				String regex = "^0+(?!$)";
				String str = parsedName[0].replaceAll(regex, "");
				
				ClienteCertificado c = new ClienteCertificado(str, "0", FilenameUtils.getName(file.getName()),esquema,version,tipocertificado,1L);
				clientesCertificadoSebra.add(c);
				hashFiles.put(c.NombreArchivo,file);
			}
		}
		
		logger.debug("Insert tabla temporal");
		sql.batch(conn, SQL_INSERT_LOCAL_TABLE, clientesCertificadoSebra, ClienteCertificado.class);
		
		Deflater deflater = new Deflater();
		Inflater inflater = new Inflater();

		HashMap<String, Object> params = new HashMap<>();
		List<ClienteCertificado> clientesOptimus = sql.query(conn, SQL_QUERY_CLIENTES, params, ClienteCertificado.class).stream().filter(x -> x.NombreArchivo != null).collect(Collectors.toList());
		for (ClienteCertificado c: clientesOptimus) {
			
			File f = hashFiles.get(c.NombreArchivo);
			byte[] bytes = Files.readAllBytes(f.toPath());
			c.CertificadoEncoded = bytes;
			/*deflater.setInput(bytes);
			byte[] bytesCompressed = new byte[1024*200];
			int totalBytesCompressed = deflater.deflate(bytesCompressed);
			
			inflater.setInput(bytesCompressed);
			byte[] bytesDecompressed = new byte[1024*200];
			int totalBytesDecompressed = inflater.inflate(bytesDecompressed);*/
			
			//logger.debug("");
			
		}
		sql.batch(conn,SQL_INSERT_CERTIFICADO , clientesOptimus, ClienteCertificado.class);
		SQL.exec(conn, SQL_DROP_LOCAL_TABLE);
		 
		logger.info("END");
		return true;
	}

	

	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
	//mbipre Certificados_71_MBI DJ1949 2022 CERT71
	//mbipre Certificados_43_MBI DJ1922 2022 CERT43
	//mbipre Certificados_21_MBI DJ1894 2022 CERT21
	
	public static void main(String[] args) {
		
		if(args==null || args.length<5) {
			System.out.println("Se requieren 5 parametros: <<sistema>> <<ruta_certificados>> <<esquema>> <<version>> <<idtipocertificado>>");
			//idtipocertificado CERT21,CERT71,CERT43
			//esquema DJXXXX
			//version 2022
			System.exit(1);
		}
		String sistema = args[0];
		String ruta_certificados = args[1];
		String esquema = args[2];
		String version = args[3];
		String tipocertificado = args[4];
		
		CargaCertificadosTributariosOffline servicio = new CargaCertificadosTributariosOffline();
		
		try(Connection conn = PlataformaUtils.getPropertiesConnection(sistema)){
			servicio.logica(conn, ruta_certificados,esquema,version,tipocertificado);
			conn.commit();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
