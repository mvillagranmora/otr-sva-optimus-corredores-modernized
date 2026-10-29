package cl.bcs.socionegocio.enr;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.socionegocio.enr.ValidarExistenciaDireccionFisica.EntradaValidarExistenciaBorradorDireccionFisica;
import cl.bcs.socionegocio.enr.pojo.DireccionFisica;
import cl.bcs.socionegocio.enr.pojo.EntradaAdministrarBorrador;
import cl.bcs.socionegocio.enr.pojo.ResultadoAdministrarBorradorEntidad;
import cl.bcs.socionegocio.enr.pojo.SalidaAdministrarBorrador;
import cl.bcs.socionegocio.enr.pojo.Validacion;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class EliminarBorradorDireccionFisica implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(EliminarBorradorDireccionFisica.class);

	private String SQL_DELETE_DIRECCION_FISICA = loadResourceToString("cl/bcs/socionegocio/enr/sql/EliminarBorradorDireccionFisica_DeleteDireccionFisica.sql");
	
	public static class EntradaEliminarBorradorDireccionFisica extends EntradaAdministrarBorrador{
		public List<DireccionFisica> p_DireccionesFisicas;
		
		public EntradaEliminarBorradorDireccionFisica(){
			this.p_DireccionesFisicas = new ArrayList<DireccionFisica>();
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaEliminarBorradorDireccionFisica jin = gson.fromJson(in.toString(), EntradaEliminarBorradorDireccionFisica.class);
		SalidaAdministrarBorrador out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaAdministrarBorrador logica(Connection con, EntradaEliminarBorradorDireccionFisica entrada) throws Exception {
		logger.info("INICIO::EliminarBorradorDireccionFisica");
		long inicioServicio = System.currentTimeMillis();
		SalidaAdministrarBorrador out = new SalidaAdministrarBorrador();
		
		ValidarExistenciaDireccionFisica validarExistenciaDireccionFisica = new ValidarExistenciaDireccionFisica();
		EntradaValidarExistenciaBorradorDireccionFisica entradaValidarExistenciaBorradorDireccionFisica = new EntradaValidarExistenciaBorradorDireccionFisica();
		entradaValidarExistenciaBorradorDireccionFisica.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaValidarExistenciaBorradorDireccionFisica.p_DireccionesFisicas = entrada.p_DireccionesFisicas;
		
		List<Validacion> validarExistenciaDireccionesFisicas = validarExistenciaDireccionFisica.logica(con, entradaValidarExistenciaBorradorDireccionFisica).validaciones;
		out.agregarValidacionesSalida(validarExistenciaDireccionesFisicas);
				
		if(out.P_OUT_ERRORESCONTADOR > 0) {
			return out;
		}
		
		eliminarDireccionesFisicas(con, entrada.p_DireccionesFisicas);
		
		out.P_OUT_REFID = obtenerIdsEliminados(entrada.p_DireccionesFisicas);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::EliminarBorradorDireccionFisica", finServicio - inicioServicio);
		return out;
	}
	
	private void eliminarDireccionesFisicas(Connection con, List<DireccionFisica> DireccionesFisicas) throws Exception {
		logger.debug("INICIO::EliminarBorradorDireccionFisica.eliminarDireccionesFisicas");
		long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		sql.batch(con, SQL_DELETE_DIRECCION_FISICA, DireccionesFisicas, DireccionFisica.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::EliminarBorradorDireccionFisica.eliminarDireccionesFisicas", fin - inicio);
	}
	
	private List<ResultadoAdministrarBorradorEntidad> obtenerIdsEliminados(List<DireccionFisica> DireccionesFisicas){
		logger.trace("INICIO::EliminarBorradorDireccionFisica.obtenerIdsEliminados");
		long inicio = System.currentTimeMillis();
		
		List<ResultadoAdministrarBorradorEntidad> idsInsertados = DireccionesFisicas.parallelStream().map(DireccionFisica->{
			ResultadoAdministrarBorradorEntidad resultadoCreacionBorradorEntidad = new ResultadoAdministrarBorradorEntidad();
			resultadoCreacionBorradorEntidad.IdInterno = DireccionFisica.IdInterno;
			resultadoCreacionBorradorEntidad.IdEntidad = DireccionFisica.IdDireccionFisica;
			return resultadoCreacionBorradorEntidad;
		}).collect(Collectors.toList());
		long fin = System.currentTimeMillis();
		
		logger.trace("FIN::EliminarBorradorDireccionFisica.obtenerIdsEliminados", fin - inicio);
		return idsInsertados;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":9,\"p_IdModulo\":\"SOC\",\"p_UsaReglasNegocio\":\"S\",\"p_CodigoFormulario\":\"DIRECCION_FISICA_FULL_NATURAL\",\"p_IdTipoCanal\":\"OPT\",\"p_DireccionesFisicas\":[{\"IdInterno\":1,\"IdDireccionFisica\":81,\"IdEnrolamiento\":199,\"Calle\":\"\",\"Numero\":\"\",\"Sector\":\"\"}]}";
			EntradaEliminarBorradorDireccionFisica Objin = gson.fromJson(json, EntradaEliminarBorradorDireccionFisica.class);
			
			EliminarBorradorDireccionFisica demo = new EliminarBorradorDireccionFisica();
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			System.out.println("SALIDA");
	 			
 			try {
 				SalidaAdministrarBorrador ObjOut = demo.logica(con, Objin);
 				con.rollback();
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