package cl.bcs.socionegocio.enr;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.socionegocio.enr.ValidarExistenciaDireccionElectronica.EntradaValidarExistenciaBorradorDireccionElectronica;
import cl.bcs.socionegocio.enr.pojo.DireccionElectronica;
import cl.bcs.socionegocio.enr.pojo.EntradaAdministrarBorrador;
import cl.bcs.socionegocio.enr.pojo.ResultadoAdministrarBorradorEntidad;
import cl.bcs.socionegocio.enr.pojo.SalidaAdministrarBorrador;
import cl.bcs.socionegocio.enr.pojo.Validacion;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class EliminarBorradorDireccionElectronica implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(EliminarBorradorDireccionElectronica.class);

	private String SQL_DELETE_DIRECCION_ELECTRONICA = loadResourceToString("cl/bcs/socionegocio/enr/sql/EliminarBorradorDireccionElectronica_DeleteDireccionElectronica.sql");
	
	public static class EntradaEliminarBorradorDireccionElectronica extends EntradaAdministrarBorrador{
		public List<DireccionElectronica> p_DireccionesElectronicas;
		
		public EntradaEliminarBorradorDireccionElectronica(){
			this.p_DireccionesElectronicas = new ArrayList<DireccionElectronica>();
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaEliminarBorradorDireccionElectronica jin = gson.fromJson(in.toString(), EntradaEliminarBorradorDireccionElectronica.class);
		SalidaAdministrarBorrador out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaAdministrarBorrador logica(Connection con, EntradaEliminarBorradorDireccionElectronica entrada) throws Exception {
		logger.info("INICIO::EliminarBorradorDireccionElectronica");
		long inicioServicio = System.currentTimeMillis();
		SalidaAdministrarBorrador out = new SalidaAdministrarBorrador();
		
		ValidarExistenciaDireccionElectronica validarExistenciaDireccionElectronica = new ValidarExistenciaDireccionElectronica();
		EntradaValidarExistenciaBorradorDireccionElectronica entradaValidarExistenciaBorradorDireccionElectronica = new EntradaValidarExistenciaBorradorDireccionElectronica();
		entradaValidarExistenciaBorradorDireccionElectronica.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaValidarExistenciaBorradorDireccionElectronica.p_DireccionesElectronicas = entrada.p_DireccionesElectronicas;
		
		List<Validacion> validacionesExistenciaDireccionesElectronicas = validarExistenciaDireccionElectronica.logica(con, entradaValidarExistenciaBorradorDireccionElectronica).validaciones;
		out.agregarValidacionesSalida(validacionesExistenciaDireccionesElectronicas);
				
		if(out.P_OUT_ERRORESCONTADOR > 0) {
			return out;
		}
		
		eliminarDireccionesElectronicas(con, entrada.p_DireccionesElectronicas);
		
		out.P_OUT_REFID = obtenerIdsEliminados(entrada.p_DireccionesElectronicas);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::EliminarBorradorDireccionElectronica", finServicio - inicioServicio);
		return out;
	}
	
	private void eliminarDireccionesElectronicas(Connection con, List<DireccionElectronica> DireccionesElectronicas) throws Exception {
		logger.debug("INICIO::EliminarBorradorDireccionElectronica.eliminarDireccionesElectronicas");
		long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		sql.batch(con, SQL_DELETE_DIRECCION_ELECTRONICA, DireccionesElectronicas, DireccionElectronica.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::EliminarBorradorDireccionElectronica.eliminarDireccionesElectronicas", fin - inicio);
	}
	
	private List<ResultadoAdministrarBorradorEntidad> obtenerIdsEliminados(List<DireccionElectronica> DireccionesElectronicas){
		logger.trace("INICIO::EliminarBorradorDireccionElectronica.obtenerIdsEliminados");
		long inicio = System.currentTimeMillis();
		
		List<ResultadoAdministrarBorradorEntidad> idsInsertados = DireccionesElectronicas.parallelStream().map(DireccionElectronica->{
			ResultadoAdministrarBorradorEntidad resultadoCreacionBorradorEntidad = new ResultadoAdministrarBorradorEntidad();
			resultadoCreacionBorradorEntidad.IdInterno = DireccionElectronica.IdInterno;
			resultadoCreacionBorradorEntidad.IdEntidad = DireccionElectronica.IdDireccionElectronica;
			return resultadoCreacionBorradorEntidad;
		}).collect(Collectors.toList());
		long fin = System.currentTimeMillis();
		
		logger.trace("FIN::EliminarBorradorDireccionElectronica.obtenerIdsEliminados", fin - inicio);
		return idsInsertados;
	}
}