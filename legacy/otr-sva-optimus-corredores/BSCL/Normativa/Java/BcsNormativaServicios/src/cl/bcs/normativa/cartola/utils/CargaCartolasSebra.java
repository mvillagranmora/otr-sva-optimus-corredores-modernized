package cl.bcs.normativa.cartola.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
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

/**
 * Clase utilitaria para cargar las cartolas historicas de sebra en optimus.
 * NO UTILIZAR PARA NADA MAS!!
 * @author chernandez
 *
 */
public class CargaCartolasSebra implements JavaCall {
	private static final Logger logger = LogManager.getLogger(CargaCartolasSebra.class);
	
	private static final String SQL_CREATE_LOCAL_TABLE = "CREATE LOCAL TEMPORARY ROW TABLE #CartolasSebra(\"DocumentoIdentidad\" NVARCHAR(100), \"CodigoPortafolio\" NVARCHAR(100), \"CartolaEncoded\" BLOB,\"Periodo\" NVARCHAR(10), \"NombreCartola\" NVARCHAR(5000))";
	private static final String SQL_DROP_LOCAL_TABLE = "DROP TABLE #CartolasSebra";
	private static final String SQL_INSERT_LOCAL_TABLE = "INSERT INTO #CartolasSebra(\"DocumentoIdentidad\", \"CodigoPortafolio\", \"CartolaEncoded\",\"Periodo\",\"NombreCartola\") VALUES(:DocumentoIdentidad, :CodigoPortafolio, :CartolaEncoded,:Periodo, :NombreCartola)";
	private static final String SQL_QUERY_CLIENTES = "SELECT CLI.\"IdCliente\", CLI.\"DocumentoIdentidad\", POR.\"IdPortafolio\", POR.\"CodigoPortafolio\", CS.\"CartolaEncoded\",CS.\"Periodo\", CS.\"NombreCartola\" FROM \"BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente\" CLI LEFT JOIN \"BSCL.Portafolio.BD::POR.Portafolio\" POR ON POR.\"IdCliente\" = CLI.\"IdCliente\" LEFT JOIN #CartolasSebra CS ON CLI.\"DocumentoIdentidad\" = CS.\"DocumentoIdentidad\" AND POR.\"CodigoPortafolio\" = CS.\"CodigoPortafolio\" WHERE POR.\"IdPortafolio\" IS NOT NULL";

	private final String SQL_INSERT_NOMINA = loadResourceToString("cl/bcs/normativa/cartola/sql/GestionarNomina_InsertNomina.sql");
	private final String SQL_INSERT_CARTOLA = "INSERT INTO \"BSCL.Normativa.BD::CAR.Cartola\" (\"IdCartola\", \"IdNomina\", \"IdCliente\", \"IdPortafolio\", \"Reprocesar\", \"Notificar\", \"EstadoProceso\", \"MensajeProceso\", \"NombreArchivo\", \"CartolaEncoded\",\"Origen\") VALUES(\"BSCL.Normativa.BD.Secuencias::CAR.Global\".NEXTVAL, :IdNomina, :IdCliente, :IdPortafolio, '', '', 'TER', '', :NombreArchivo,:CartolaEncoded, :Origen);";
	
	private static final DateTimeFormatter YEAR_MONTH_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM");
	private static final DateTimeFormatter ISO8601 = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	
	public static class ClienteCartola{
		public String Periodo;
		public String DocumentoIdentidad;
		public String CodigoPortafolio;
		public String NombreCartola;
		public byte[] CartolaEncoded;
		
		public ClienteCartola(String periodo, String documentoIdentidad, String codigoPortafolio, String nombreCartola,byte[] CartolaEncoded) {
			this.Periodo = periodo;
			this.DocumentoIdentidad = documentoIdentidad;
			this.CodigoPortafolio = codigoPortafolio;
			this.NombreCartola = nombreCartola;
			this.CartolaEncoded = CartolaEncoded;
		}
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ClientesOptimus{
		public String Periodo;
		public String DocumentoIdentidad;
		public String CodigoPortafolio;
		public String NombreCartola;
		public byte[] CartolaEncoded;
		public Long IdCliente;
		public Long IdPortafolio;
		public Date FechaDesde;
		public Date FechaHasta;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class CartolaOptimus{
		public Long		IdNomina;
		public Long		IdCliente;
		public Long		IdPortafolio;
		public String	Origen;
		public String	NombreArchivo;
		public byte[]	CartolaEncoded;
		
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public boolean logica(Connection conn, String rutaCartolas, String periodo2) throws Exception {
		logger.info("INIT");
		SQL sql = new SQL();
		
		SQL.exec(conn, SQL_CREATE_LOCAL_TABLE);
		logger.debug("Ruta base: " + rutaCartolas);
		String[] extensions = {"pdf"};
		logger.debug("Obtener archivos de la ruta base");
		Collection<File> files = FileUtils.listFiles(new File(rutaCartolas), extensions ,true);
		
		logger.debug("Parsear nombres de los archivos y asignar en la clase ClienteCartola");
		List<ClienteCartola> clientesCartolaSebra = files.parallelStream().map(file -> {
			String filename = FilenameUtils.getBaseName(file.getName());
			String[] parsedName = filename.split(" ");
			try {
				return new ClienteCartola(periodo2, parsedName[0], parsedName[1], FilenameUtils.getName(file.getName()),comprimirData(Files.readAllBytes(file.toPath())));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}).collect(Collectors.toList());
		logger.debug("Insert tabla temporal");
		sql.batch(conn, SQL_INSERT_LOCAL_TABLE, clientesCartolaSebra, ClienteCartola.class);
		
		HashMap<String, Object> params = new HashMap<>();
		List<ClientesOptimus> cartolasOptimus = sql.query(conn, SQL_QUERY_CLIENTES, params, ClientesOptimus.class);
		MultiKeyMap mapaClientesOptimus = new MultiKeyMap();
		cartolasOptimus.forEach(cartola -> {
			mapaClientesOptimus.put(cartola.DocumentoIdentidad, cartola.CodigoPortafolio, cartola);
		});
		logger.debug("Cantidad de cartolas de cliente sebra: " + clientesCartolaSebra.size());
		logger.debug("Cantidad clientes totales optimus: " + cartolasOptimus.size());
		logger.debug("Cantidad clientes optimus con cartola sebra : "+ cartolasOptimus.stream().filter(cartola -> cartola.Periodo!=null).count());
		logger.debug("Cantidad clientes sebra que no existen en optimus: "+ clientesCartolaSebra.stream().filter(cliente -> mapaClientesOptimus.get(cliente.DocumentoIdentidad,cliente.CodigoPortafolio)==null).count());
		clientesCartolaSebra.stream().filter(cliente -> mapaClientesOptimus.get(cliente.DocumentoIdentidad,cliente.CodigoPortafolio)==null).forEach(cliente -> {
			logger.trace(String.format("CLIENTE: {%s}, Portafolio:{%s} no existe en optimus", cliente.DocumentoIdentidad,cliente.CodigoPortafolio));
		});
		logger.debug("Cantidad clientes optimus sin cartola sebra:" + cartolasOptimus.stream().filter(cartola -> cartola.Periodo==null).count());
		cartolasOptimus.stream().filter(cartola -> cartola.Periodo==null).forEach(cartola -> {	
			logger.trace(String.format("CLIENTE: {%s}, Portafolio:{%s} no tiene cartola en sebra", cartola.DocumentoIdentidad,cartola.CodigoPortafolio));
		});

		//eliminar todos los resultados que no tengan periodo (no existe cartola para ese cliente)
		cartolasOptimus = cartolasOptimus.stream().filter(cartola -> cartola.Periodo!=null).collect(Collectors.toList());
		
		logger.debug("Agrupar nominas por periodo");
		Map<String, List<ClientesOptimus>> cartolasAgrupadas = cartolasOptimus.stream().collect(Collectors.groupingBy(cliente -> cliente.Periodo));
		
		logger.debug("Obtener Nominas a ingresar");
		LinkedList<Long> secuencias = new LinkedList<>();
		//secuencias.addAll(Arrays.asList(1000000L,2000000L));
		secuencias.addAll(Utils.getSecuencia(conn, "BSCL.Normativa.BD.Secuencias::CAR.Global", (long) cartolasAgrupadas.size()));
		List<Nomina> nominas = new ArrayList<>();
		HashMap<String, Long> idsNominaPeriodo = new HashMap<>();
		cartolasAgrupadas.keySet().forEach(periodo->{
			LocalDate ld = YearMonth.parse(periodo, YEAR_MONTH_FORMAT).atEndOfMonth();
			Date fechaDesde = Date.valueOf(LocalDate.parse(periodo+"-01", ISO8601));
			Date fechaHasta = Date.valueOf(ld);
			idsNominaPeriodo.put(periodo, secuencias.pop());
			nominas.add(templateNomina(idsNominaPeriodo.get(periodo), periodo, fechaDesde, fechaHasta));
		});
		logger.debug("Insert nominas");
		sql.batch(conn, SQL_INSERT_NOMINA, nominas, Nomina.class);
		logger.debug("Map Cartolas con nominas");
		List<CartolaOptimus> cartolas = cartolasOptimus.stream().map(car -> {
			CartolaOptimus cartola = new CartolaOptimus();
			cartola.IdNomina = idsNominaPeriodo.get(car.Periodo);
			cartola.IdCliente = car.IdCliente;
			cartola.IdPortafolio = car.IdPortafolio;
			cartola.NombreArchivo = car.NombreCartola;
			cartola.Origen = "OPTIMUS";	//DEJAR SEBRA PARA LEER DE FILESYSTEM
			cartola.CartolaEncoded = car.CartolaEncoded;	//COMENTAR ESTA LINEA PARA LEER DE FILESYSTEM
			return cartola;
		}).collect(Collectors.toList());
		logger.debug("Insert cartolas");
		sql.batch(conn, SQL_INSERT_CARTOLA, cartolas, CartolaOptimus.class);
		SQL.exec(conn, SQL_DROP_LOCAL_TABLE);
		 
		logger.info("END");
		return true;
	}

	private byte[] comprimirData (byte[] data) throws IOException {
		Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION);
		
		deflater.setInput(data);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
		deflater.finish();
		
		byte[] buffer = new byte[1024];
		while (!deflater.finished()) {
			int count = deflater.deflate(buffer);
			outputStream.write(buffer, 0, count);
		}
		outputStream.close();
		
		return outputStream.toByteArray();
	}
	
	private static Nomina templateNomina(Long idNomina, String periodo, Date fechaDesde, Date fechaHasta) {
		Nomina nomina = new Nomina();
		nomina.IdNomina = idNomina;
		nomina.IdEmpresa = 1L;
		nomina.IdUsuario = 0L;
		nomina.IdPlantilla = null;
		nomina.Periodo = periodo;
		nomina.FechaDesde = fechaDesde;
		nomina.FechaHasta = fechaHasta;
		nomina.Comentario = "Carga Sebra";
		nomina.Certificada = "N";
		nomina.IdUsuarioCertificador = 0L;
		nomina.NombreUsuarioCertificador = "";
		nomina.NotaCertificacion = "";
		nomina.TipoProceso = "NORMAL"; //DEJAR OFFLINE PARA QUE SE LEA DESDE FILESYSTEM
		nomina.EstadoProceso = "TER";
		nomina.FechaProgramadaProceso = null;
		nomina.FechaInicioProceso = Fecha.toTimeStamp("0001-01-01 00:00:00");
		nomina.FechaTerminoProceso = Fecha.toTimeStamp("0001-01-01 00:00:00");
		nomina.MensajeProceso = "";
		nomina.Ano = periodo != null ? periodo.substring(0, 4) : "";
		nomina.Mes = periodo != null ? periodo.substring(5, 7) : "";
		nomina.IncluirPortafoliosConSaldo = "";
		nomina.IncluirPortafoliosConCustodia = "";
		nomina.IncluirPortafoliosConMovimientoCuentaInversion = "";
		nomina.IncluirCategoriaInversionista = "";
		nomina.CategoriaInversionista = "";
		nomina.IncluirTipoPersona = "";
		nomina.TipoPersona = "";
		nomina.IncluirTipoCliente = "";
		nomina.TipoCliente = "";
		nomina.IncluirPortafoliosExcluidos = "";
		nomina.ExcluirPortafoliosIncluidosOtraNomina = "";
		nomina.ExcluirOperadoresDirectos = "";
		nomina.OpcionProceso = "";
		nomina.IdNominaPadre = 0L;
		nomina.CartolaConsolidada = "n";
		return nomina;
	}

	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
	//periodo ej 2021-01
	public static void main(String[] args) {
		
		if(args==null || args.length<2) {
			System.out.println("Se requieren 2 parametros: <<sistema>> <<ruta_cartolas>> <<periodo>>");
			System.out.println("Parametros opcionales: <<properties>> <<debug>>");
			System.exit(1);
		}
		String sistema = args[0];
		String cartolas = args[1];
		String periodo = args[2];
		String properties = (args.length >= 4) ? args[3] : "application.properties";
		boolean debug = (args.length >= 5) ? Boolean.valueOf(args[4]) : false;
		
		CargaCartolasSebra servicio = new CargaCartolasSebra();
		
		try(Connection conn = PlataformaUtils.getPropertiesConnection(sistema,properties )){
			servicio.logica(conn, cartolas,periodo);
			
			if(!debug) {
				conn.commit();
			}else {
				conn.rollback();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
