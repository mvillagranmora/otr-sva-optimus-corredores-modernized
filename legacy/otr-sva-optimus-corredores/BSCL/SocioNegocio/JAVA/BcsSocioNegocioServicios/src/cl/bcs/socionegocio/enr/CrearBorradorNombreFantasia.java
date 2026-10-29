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
import cl.bcs.socionegocio.enr.pojo.NombreFantasia;
import cl.bcs.socionegocio.enr.pojo.EntradaAdministrarBorrador;
import cl.bcs.socionegocio.enr.pojo.ResultadoAdministrarBorradorEntidad;
import cl.bcs.socionegocio.enr.pojo.SalidaAdministrarBorrador;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearBorradorNombreFantasia implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(CrearBorradorNombreFantasia.class);

	private String SQL_INSERT_NOMBRE_FANTASIA = loadResourceToString("cl/bcs/socionegocio/enr/sql/CrearBorradorNombreFantasia_InsertNombreFantasia.sql");
	
	public static class EntradaCrearBorradorNombreFantasia extends EntradaAdministrarBorrador{
		public List<NombreFantasia> p_NombresFantasia;
		
		public EntradaCrearBorradorNombreFantasia(){
			this.p_NombresFantasia = new ArrayList<NombreFantasia>();
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaCrearBorradorNombreFantasia jin = gson.fromJson(in.toString(), EntradaCrearBorradorNombreFantasia.class);
		SalidaAdministrarBorrador out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaAdministrarBorrador logica(Connection con, EntradaCrearBorradorNombreFantasia entrada) throws Exception {
		logger.info("INICIO::CrearBorradorNombreFantasia");
		long inicioServicio = System.currentTimeMillis();
		SalidaAdministrarBorrador out = new SalidaAdministrarBorrador();
		
		out = entrada.validarCamposObligatoriosEntrada(con, entrada.p_NombresFantasia, NombreFantasia.class, out);
		out = entrada.validarGrillasObligatoriasEntrada(con, entrada.p_NombresFantasia, "NombresFantasia", out);
		if(out.P_OUT_ERRORESCONTADOR > 0) {
			return out;
		}
		
		List<NombreFantasia> nombresFantasia = obtenerNombresFantasia(con, entrada.p_NombresFantasia, entrada.p_IdEmpresa, entrada.p_IdUsuario);
		
		insertarNombresFantasia(con, nombresFantasia);
		
		out.P_OUT_REFID = obtenerIdsInsertados(nombresFantasia);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::CrearBorradorNombreFantasia", finServicio - inicioServicio);
		return out;
		
	}

	private List<NombreFantasia> obtenerNombresFantasia(Connection con, List<NombreFantasia> borradoresNombresFantasia, Long idEmpresa, Long idUsuario) throws Exception{
		logger.trace("INICIO::CrearBorradorNombreFantasia.obtenerNombresFantasia");
		long inicio = System.currentTimeMillis();
		
		List<Long> secuencias = Utils.getSecuencia(con, "BSCL.SocioNegocio.BD.Secuencias::BorradorNombreFantasia", Long.valueOf(borradoresNombresFantasia.size()));
		String fechaHoraSistema = FechaDB.ObtenerFechaHoraSistemaLocal(con, idEmpresa);
		
		AtomicInteger index = new AtomicInteger();
		List<NombreFantasia> nombresFantasia = borradoresNombresFantasia.parallelStream().map(borradorNombreFantasia->{
			Long idNombreFantasia = secuencias.get(index.getAndIncrement());
			Long idUsuarioCreador = idUsuario;
			String fechaIngreso = fechaHoraSistema;
			NombreFantasia nombreFantasia = obtenerEntidadNombreFantasia(borradorNombreFantasia, idNombreFantasia, fechaIngreso, idUsuarioCreador);
			return nombreFantasia;
		}).collect(Collectors.toList());
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearBorradorNombreFantasia.obtenerNombresFantasia", fin - inicio);
		return nombresFantasia;
	}
	
	private NombreFantasia obtenerEntidadNombreFantasia(NombreFantasia borradorNombreFantasia, Long idNombreFantasia, String fechaIngreso, Long idUsuarioCreador) {
		logger.trace("INICIO::CrearBorradorNombreFantasia.obtenerEntidadNombreFantasia");
		long inicio = System.currentTimeMillis();
		
		NombreFantasia nombreFantasia = new NombreFantasia();
		nombreFantasia.IdNombreFantasia = idNombreFantasia;
		nombreFantasia.IdInterno = borradorNombreFantasia.IdInterno;
		nombreFantasia.IdEnrolamiento = borradorNombreFantasia.IdEnrolamiento;
		nombreFantasia.NombreFantasia = borradorNombreFantasia.NombreFantasia;
		nombreFantasia.FechaIngreso = Fecha.toDateTime(fechaIngreso);
		nombreFantasia.IdUsuarioCreador = idUsuarioCreador;
				
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearBorradorNombreFantasia.obtenerEntidadNombreFantasia", fin - inicio);
		return nombreFantasia;
	}
	
	private void insertarNombresFantasia(Connection con, List<NombreFantasia> nombresFantasia) throws Exception {
		logger.debug("INICIO::CrearBorradorNombreFantasia.insertarNombresFantasia");
		long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		sql.batch(con, SQL_INSERT_NOMBRE_FANTASIA, nombresFantasia, NombreFantasia.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearBorradorNombreFantasia.insertarNombresFantasia", fin - inicio);
	}
	
	private List<ResultadoAdministrarBorradorEntidad> obtenerIdsInsertados(List<NombreFantasia> nombresFantasia){
		logger.trace("INICIO::CrearBorradorNombreFantasia.obtenerIdsInsertados");
		long inicio = System.currentTimeMillis();
		
		List<ResultadoAdministrarBorradorEntidad> idsInsertados = nombresFantasia.parallelStream().map(nombreFantasia->{
			ResultadoAdministrarBorradorEntidad resultadoCreacionBorradorEntidad = new ResultadoAdministrarBorradorEntidad();
			resultadoCreacionBorradorEntidad.IdInterno = nombreFantasia.IdInterno;
			resultadoCreacionBorradorEntidad.IdEntidad = nombreFantasia.IdNombreFantasia;
			return resultadoCreacionBorradorEntidad;
		}).collect(Collectors.toList());
		long fin = System.currentTimeMillis();
		
		logger.trace("FIN::CrearBorradorNombreFantasia.obtenerIdsInsertados", fin - inicio);
		return idsInsertados;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			//String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_IdModulo\":\"SOC\",\"p_UsaReglasNegocio\":\"S\",\"p_IdTipoCanal\":\"WBS\",\"p_CodigoFormulario\":\"NOMBRES_FANTASIA_PERSONA_FULL_JURIDICA\",\"p_NombresFantasia\":[{\"IdInterno\":1,\"IdEnrolamiento\":456,\"NombreFantasia\":\"PRUEBA\"}]}";
			//String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_IdModulo\":\"SOC\",\"p_UsaReglasNegocio\":\"S\",\"p_IdTipoCanal\":\"WBS\",\"p_CodigoFormulario\":\"NOMBRES_FANTASIA_PERSONA_FULL_JURIDICA\",\"p_NombresFantasia\":[{\"IdInterno\":1,\"IdEnrolamiento\":456,\"NombreFantasia\":\"\"}]}";
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_IdModulo\":\"SOC\",\"p_UsaReglasNegocio\":\"S\",\"p_CodigoFormulario\":\"NOMBRES_FANTASIA_PERSONA_FULL_JURIDICA\",\"p_IdTipoCanal\":\"WBS\"}";
			EntradaCrearBorradorNombreFantasia Objin = gson.fromJson(json, EntradaCrearBorradorNombreFantasia.class);
			
			CrearBorradorNombreFantasia demo = new CrearBorradorNombreFantasia();
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			System.out.println("SALIDA");
	 			
 			try {
 				SalidaAdministrarBorrador ObjOut = demo.logica(con, Objin);
 				con.commit();
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
