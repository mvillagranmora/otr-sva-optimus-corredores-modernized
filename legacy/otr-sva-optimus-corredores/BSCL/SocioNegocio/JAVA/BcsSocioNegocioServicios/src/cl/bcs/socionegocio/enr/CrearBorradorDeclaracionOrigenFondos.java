package cl.bcs.socionegocio.enr;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.socionegocio.enr.pojo.DeclaracionOrigenFondos;
import cl.bcs.socionegocio.enr.pojo.DetalleOrigenFondo;
import cl.bcs.socionegocio.enr.pojo.EntradaAdministrarBorrador;
import cl.bcs.socionegocio.enr.pojo.ResultadoAdministrarBorradorEntidad;
import cl.bcs.socionegocio.enr.pojo.SalidaAdministrarBorrador;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearBorradorDeclaracionOrigenFondos implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(CrearBorradorDeclaracionOrigenFondos.class);

	private String SQL_INSERT_DECLARACION_ORIGEN_FONDOS = loadResourceToString("cl/bcs/socionegocio/enr/sql/CrearBorradorDeclaracionOrigenFondos_InsertDeclaracionOrigenFondos.sql");
	private String SQL_INSERT_DETALLE_DECLARACION_ORIGEN_FONDOS = loadResourceToString("cl/bcs/socionegocio/enr/sql/CrearBorradorDeclaracionOrigenFondos_InsertDetalleDeclaracionOrigenFondos.sql");;
	
	public static class EntradaCrearBorradorDeclaracionOrigenFondos extends EntradaAdministrarBorrador{
		public List<DeclaracionOrigenFondos> p_DeclaracionOrigenFondos;
		
		public EntradaCrearBorradorDeclaracionOrigenFondos(){
			this.p_DeclaracionOrigenFondos = new ArrayList<DeclaracionOrigenFondos>();
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaCrearBorradorDeclaracionOrigenFondos jin = gson.fromJson(in.toString(), EntradaCrearBorradorDeclaracionOrigenFondos.class);
		SalidaAdministrarBorrador out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaAdministrarBorrador logica(Connection con, EntradaCrearBorradorDeclaracionOrigenFondos entrada) throws Exception {
		logger.info("INICIO::CrearBorradorDeclaracionOrigenFondos");
		long inicioServicio = System.currentTimeMillis();
		SalidaAdministrarBorrador out = new SalidaAdministrarBorrador();
		
		out = entrada.validarCamposObligatoriosEntrada(con, entrada.p_DeclaracionOrigenFondos, DeclaracionOrigenFondos.class, out);
		out = entrada.validarGrillasObligatoriasEntrada(con, entrada.p_DeclaracionOrigenFondos, "DeclaracionesOrigenFondos", out);
		if(out.P_OUT_ERRORESCONTADOR > 0) {
			return out;
		}
		
		List<DeclaracionOrigenFondos> declaracionesOrigenFondos = obtenerDeclaracionesOrigenFondos(con, entrada.p_DeclaracionOrigenFondos, entrada.p_IdUsuario, entrada.p_IdEmpresa);
		List<DetalleOrigenFondo> detalleDeclaracionesOrigenFondos = obtenerDetalleDeclaracionesOrigenFondos(declaracionesOrigenFondos);
		
		insertarDeclaracionesOrigenFondos(con, declaracionesOrigenFondos);
		insertarDetalleDeclaracionesOrigenFondos(con, detalleDeclaracionesOrigenFondos);

		out.P_OUT_REFID = obtenerIdsInsertados(declaracionesOrigenFondos);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::CrearBorradorDeclaracionOrigenFondos", finServicio - inicioServicio);
		return out;
		
	}


	private List<DetalleOrigenFondo> obtenerDetalleDeclaracionesOrigenFondos(List<DeclaracionOrigenFondos> declaracionesOrigenFondos) {
		logger.debug("INICIO::CrearBorradorDeclaracionOrigenFondos.obtenerDetalleDeclaracionesOrigenFondos");
		long inicio = System.currentTimeMillis();
		
		List<DetalleOrigenFondo> detalleDeclaracionesOrigenFondos = new ArrayList<>();
	
		for(DeclaracionOrigenFondos declaracion : declaracionesOrigenFondos) {
			for(DetalleOrigenFondo detalleDeclaracion : declaracion.DetalleOrigenFondo) {
				detalleDeclaracionesOrigenFondos.add(detalleDeclaracion);
			}
		}
			
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearBorradorDeclaracionOrigenFondos.obtenerDetalleDeclaracionesOrigenFondos", fin - inicio);
		return detalleDeclaracionesOrigenFondos;
	}
	

	private void insertarDetalleDeclaracionesOrigenFondos(Connection con, List<DetalleOrigenFondo> detalleDeclaracionesOrigenFondos) throws Exception {
		logger.debug("INICIO::CrearBorradorDeclaracionOrigenFondos.insertarDetalleDeclaracionesOrigenFondos");
		long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		sql.batch(con, SQL_INSERT_DETALLE_DECLARACION_ORIGEN_FONDOS, detalleDeclaracionesOrigenFondos, DetalleOrigenFondo.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearBorradorDeclaracionOrigenFondos.insertarDetalleDeclaracionesOrigenFondos", fin - inicio);
		
	}

	private List<DeclaracionOrigenFondos> obtenerDeclaracionesOrigenFondos(Connection con, List<DeclaracionOrigenFondos> borradoresDeclaracionesOrigenFondos, Long idUsuario, Long idEmpresa) throws Exception{
		logger.trace("INICIO::CrearBorradorDeclaracionOrigenFondos.obtenerDeclaracionesOrigenFondos");
		long inicio = System.currentTimeMillis();
		
		List<Long> secuencias = Utils.getSecuencia(con, "BSCL.SocioNegocio.BD.Secuencias::BorradorDeclaracionOrigenFondos", Long.valueOf(borradoresDeclaracionesOrigenFondos.size()));
		String fechaHoraSistema = FechaDB.ObtenerFechaHoraSistemaLocal(con, idEmpresa);
		
		AtomicInteger index = new AtomicInteger();
		List<DeclaracionOrigenFondos> declaracionesOrigenFondos = borradoresDeclaracionesOrigenFondos.parallelStream().map(borradorDeclaracionOrigenFondos->{
			Long idDeclaracionOrigenFondos = secuencias.get(index.getAndIncrement());
			DeclaracionOrigenFondos declaracionOrigenFondos = obtenerEntidadDeclaracionOrigenFondos(borradorDeclaracionOrigenFondos, idDeclaracionOrigenFondos, idUsuario, idEmpresa, fechaHoraSistema);
			return declaracionOrigenFondos;
		}).collect(Collectors.toList());
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearBorradorDeclaracionOrigenFondos.obtenerDeclaracionesOrigenFondos", fin - inicio);
		return declaracionesOrigenFondos;
	}
	
	private DeclaracionOrigenFondos obtenerEntidadDeclaracionOrigenFondos(DeclaracionOrigenFondos borradorDeclaracionOrigenFondos, Long idDeclaracionOrigenFondos, Long idUsuario, Long idEmpresa, String fechaHoraSistema) {
		logger.trace("INICIO::CrearBorradorDeclaracionOrigenFondos.obtenerEntidadDeclaracionOrigenFondos");
		long inicio = System.currentTimeMillis();
		
		DeclaracionOrigenFondos declaracionOrigenFondos = new DeclaracionOrigenFondos();
		declaracionOrigenFondos.IdDeclaracionOrigenFondos = idDeclaracionOrigenFondos;
		declaracionOrigenFondos.IdInterno = borradorDeclaracionOrigenFondos.IdInterno;
		declaracionOrigenFondos.IdEnrolamiento = borradorDeclaracionOrigenFondos.IdEnrolamiento;
		declaracionOrigenFondos.IdTipoOperacionOrigenFondos = borradorDeclaracionOrigenFondos.IdTipoOperacionOrigenFondos;
		declaracionOrigenFondos.FechaDeclaracion = borradorDeclaracionOrigenFondos.FechaDeclaracion;
		declaracionOrigenFondos.Comentario = borradorDeclaracionOrigenFondos.Comentario;
		declaracionOrigenFondos.IdUsuarioCreador = idUsuario;
		declaracionOrigenFondos.FechaIngreso = Fecha.toDateTime(fechaHoraSistema);
		declaracionOrigenFondos.IdEmpresa = idEmpresa;
		declaracionOrigenFondos.OperacionInscripcion = borradorDeclaracionOrigenFondos.OperacionInscripcion;
		declaracionOrigenFondos.DetalleOrigenFondo = borradorDeclaracionOrigenFondos.DetalleOrigenFondo.parallelStream().map(detalleOrigen -> {
			DetalleOrigenFondo detalleDeclaracionOrigenFondos = new DetalleOrigenFondo();
			detalleDeclaracionOrigenFondos.IdDeclaracionOrigenFondos = idDeclaracionOrigenFondos;
			detalleDeclaracionOrigenFondos.IdTipoOrigenFondo = detalleOrigen.IdTipoOrigenFondo;
			detalleDeclaracionOrigenFondos.Valor = detalleOrigen.Valor;
			detalleDeclaracionOrigenFondos.Otros = detalleOrigen.Otros;
			detalleDeclaracionOrigenFondos.FechaIngreso = Fecha.toDateTime(fechaHoraSistema);
			detalleDeclaracionOrigenFondos.IdUsuarioCreador = idUsuario;
			return detalleDeclaracionOrigenFondos;
		}).collect(Collectors.toList());
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearBorradorDeclaracionOrigenFondos.obtenerEntidadDeclaracionOrigenFondos", fin - inicio);
		return declaracionOrigenFondos;
	}
	
	private void insertarDeclaracionesOrigenFondos(Connection con, List<DeclaracionOrigenFondos> declaracionesOrigenFondos) throws Exception {
		logger.debug("INICIO::CrearBorradorDeclaracionOrigenFondos.insertarDeclaracionesOrigenFondos");
		long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		sql.batch(con, SQL_INSERT_DECLARACION_ORIGEN_FONDOS, declaracionesOrigenFondos, DeclaracionOrigenFondos.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearBorradorDeclaracionOrigenFondos.insertarDeclaracionesOrigenFondos", fin - inicio);
	}
	
	private List<ResultadoAdministrarBorradorEntidad> obtenerIdsInsertados(List<DeclaracionOrigenFondos> declaracionesOrigenFondos){
		logger.trace("INICIO::CrearBorradorDeclaracionOrigenFondos.obtenerIdsInsertados");
		long inicio = System.currentTimeMillis();
		
		List<ResultadoAdministrarBorradorEntidad> idsInsertados = declaracionesOrigenFondos.parallelStream().map(DeclaracionOrigenFondos->{
			ResultadoAdministrarBorradorEntidad resultadoCreacionBorradorEntidad = new ResultadoAdministrarBorradorEntidad();
			resultadoCreacionBorradorEntidad.IdInterno = DeclaracionOrigenFondos.IdInterno;
			resultadoCreacionBorradorEntidad.IdEntidad = DeclaracionOrigenFondos.IdDeclaracionOrigenFondos;
			return resultadoCreacionBorradorEntidad;
		}).collect(Collectors.toList());
		long fin = System.currentTimeMillis();
		
		logger.trace("FIN::CrearBorradorDeclaracionOrigenFondos.obtenerIdsInsertados", fin - inicio);
		return idsInsertados;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String json = "{\r\n"
					+ "    \"p_IdEmpresa\": 1,\r\n"
					+ "    \"p_IdUsuario\": 4,\r\n"
					+ "    \"p_IdModulo\": \"SOC\",\r\n"
					+ "    \"p_UsaReglasNegocio\": \"S\",\r\n"
					+ "    \"p_IdTipoCanal\": \"WBS\",\r\n"
					+ "    \"p_CodigoFormulario\": \"DECLARACION_ORIGEN_FONDOS_FULL_NATURAL\",\r\n"
					+ "    \"p_DeclaracionOrigenFondos\": [\r\n"
					+ "      {\r\n"
					+ "        \"IdInterno\": 1,\r\n"
					+ "        \"IdEnrolamiento\": 456,\r\n"
					+ "        \"IdTipoOperacionOrigenFondos\": \"asd\",\r\n"
					+ "        \"FechaDeclaracion\": \"2020-01-08\",\r\n"
					+ "        \"Comentario\": \"ASDSAD\",\r\n"
					+ "        \"OperacionInscripcion\": \"FICHA\",\r\n"
					+ "        \"DetalleOrigenFondo\": [\r\n"
					+ "          { \"IdTipoOrigenFondo\": \"HER\", \"Valor\": 0, \"Otros\": \"\" },\r\n"
					+ "          { \"IdTipoOrigenFondo\": \"ISL\", \"Valor\": 0, \"Otros\": \"\" },\r\n"
					+ "          { \"IdTipoOrigenFondo\": \"JDA\", \"Valor\": 0, \"Otros\": \"\" },\r\n"
					+ "          { \"IdTipoOrigenFondo\": \"VBR\", \"Valor\": 0, \"Otros\": \"\" },\r\n"
					+ "          { \"IdTipoOrigenFondo\": \"VDP\", \"Valor\": 0, \"Otros\": \"\" },\r\n"
					+ "          { \"IdTipoOrigenFondo\": \"VDV\", \"Valor\": 0, \"Otros\": \"\" },\r\n"
					+ "          { \"IdTipoOrigenFondo\": \"OTR\", \"Valor\": 0, \"Otros\": \"\" },\r\n"
					+ "          { \"IdTipoOrigenFondo\": \"SUELDO\", \"Valor\": 0, \"Otros\": \"\" },\r\n"
					+ "          { \"IdTipoOrigenFondo\": \"INV\", \"Valor\": 0, \"Otros\": \"\" },\r\n"
					+ "          { \"IdTipoOrigenFondo\": \"XX\", \"Valor\": 0, \"Otros\": \"\" }\r\n"
					+ "        ]\r\n"
					+ "      },\r\n"
					+ "      {\r\n"
					+ "        \"IdInterno\": 2,\r\n"
					+ "        \"IdEnrolamiento\": 432,\r\n"
					+ "        \"IdTipoOperacionOrigenFondos\": \"APORFM\",\r\n"
					+ "        \"FechaDeclaracion\": \"2020-01-08\",\r\n"
					+ "        \"Comentario\": \"ASDSAD\",\r\n"
					+ "        \"OperacionInscripcion\": \"FICHA\",\r\n"
					+ "        \"DetalleOrigenFondo\": [\r\n"
					+ "          { \"IdTipoOrigenFondo\": \"HER\", \"Valor\": 0, \"Otros\": \"\" },\r\n"
					+ "          { \"IdTipoOrigenFondo\": \"ISL\", \"Valor\": 0, \"Otros\": \"\" },\r\n"
					+ "          { \"IdTipoOrigenFondo\": \"JDA\", \"Valor\": 0, \"Otros\": \"\" },\r\n"
					+ "          { \"IdTipoOrigenFondo\": \"JYD\", \"Valor\": 0, \"Otros\": \"\" },\r\n"
					+ "          { \"IdTipoOrigenFondo\": \"VBR\", \"Valor\": 0, \"Otros\": \"\" },\r\n"
					+ "          { \"IdTipoOrigenFondo\": \"VDP\", \"Valor\": 0, \"Otros\": \"\" },\r\n"
					+ "          { \"IdTipoOrigenFondo\": \"VDV\", \"Valor\": 0, \"Otros\": \"\" },\r\n"
					+ "          { \"IdTipoOrigenFondo\": \"OTR\", \"Valor\": 0, \"Otros\": \"\" },\r\n"
					+ "          { \"IdTipoOrigenFondo\": \"SUELDO\", \"Valor\": 0, \"Otros\": \"\" },\r\n"
					+ "          { \"IdTipoOrigenFondo\": \"INV\", \"Valor\": 0, \"Otros\": \"\" },\r\n"
					+ "          { \"IdTipoOrigenFondo\": \"XX\", \"Valor\": 0, \"Otros\": \"\" }\r\n"
					+ "        ]\r\n"
					+ "      },\r\n"
					+ "      {\r\n"
					+ "        \"IdInterno\": 3,\r\n"
					+ "        \"IdEnrolamiento\": 213,\r\n"
					+ "        \"IdTipoOperacionOrigenFondos\": \"APORFM\",\r\n"
					+ "        \"FechaDeclaracion\": \"2020-01-08\",\r\n"
					+ "        \"Comentario\": \"ASDSAD\",\r\n"
					+ "        \"OperacionInscripcion\": \"FICHA\",\r\n"
					+ "        \"DetalleOrigenFondo\": [\r\n"
					+ "          { \"IdTipoOrigenFondo\": \"HER\", \"Valor\": 0, \"Otros\": \"\" },\r\n"
					+ "          { \"IdTipoOrigenFondo\": \"ISL\", \"Valor\": 0, \"Otros\": \"\" },\r\n"
					+ "          { \"IdTipoOrigenFondo\": \"JDA\", \"Valor\": 0, \"Otros\": \"\" },\r\n"
					+ "          { \"IdTipoOrigenFondo\": \"JYD\", \"Valor\": 0, \"Otros\": \"\" },\r\n"
					+ "          { \"IdTipoOrigenFondo\": \"VBR\", \"Valor\": 0, \"Otros\": \"\" },\r\n"
					+ "          { \"IdTipoOrigenFondo\": \"VDP\", \"Valor\": 0, \"Otros\": \"\" },\r\n"
					+ "          { \"IdTipoOrigenFondo\": \"VDV\", \"Valor\": 0, \"Otros\": \"\" },\r\n"
					+ "          { \"IdTipoOrigenFondo\": \"OTR\", \"Valor\": 0, \"Otros\": \"\" },\r\n"
					+ "          { \"IdTipoOrigenFondo\": \"SUELDO\", \"Valor\": 0, \"Otros\": \"\" },\r\n"
					+ "          { \"IdTipoOrigenFondo\": \"INV\", \"Valor\": 0, \"Otros\": \"\" },\r\n"
					+ "          { \"IdTipoOrigenFondo\": \"XX\", \"Valor\": 0, \"Otros\": \"\" }\r\n"
					+ "        ]\r\n"
					+ "      }\r\n"
					+ "    ]\r\n"
					+ "  }";
			
			EntradaCrearBorradorDeclaracionOrigenFondos Objin = gson.fromJson(json, EntradaCrearBorradorDeclaracionOrigenFondos.class);
			CrearBorradorDeclaracionOrigenFondos demo = new CrearBorradorDeclaracionOrigenFondos();
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			System.out.println("SALIDA");
	 			
 			try {
 				SalidaAdministrarBorrador ObjOut = demo.logica(con, Objin);
 				con.rollback();
 				//con.commit();
 				System.out.println(ObjOut.toString());
 			}
 			catch(Exception er) {
				er.printStackTrace();	
			}
			finally {
				con.rollback();
				con.close();
			}
		}
		System.out.println("== END ==");
	}
}
