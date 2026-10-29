package cl.bcs.socionegocio.enr;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import cl.bcs.socionegocio.enr.ValidarExistenciaBorradorRelacionPersona.EntradaValidarExistenciaBorradorRelacionPersona;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.socionegocio.enr.pojo.EntradaAdministrarBorrador;
import cl.bcs.socionegocio.enr.pojo.RelacionPersona;
import cl.bcs.socionegocio.enr.pojo.ResultadoAdministrarBorradorEntidad;
import cl.bcs.socionegocio.enr.pojo.SalidaAdministrarBorrador;
import cl.bcs.socionegocio.enr.pojo.Validacion;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class EliminarBorradorRelacionPersona implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(ModificarBorradorTarjetaBancaria.class);

	private String SQL_DELETE_RELACION_PERSONA = loadResourceToString("cl/bcs/socionegocio/enr/sql/EliminarBorradorRelacionPersona_DeleteRelacionPersona.sql");
	
	public static class EntradaEliminarBorradorRelacionPersona extends EntradaAdministrarBorrador{
		public List<RelacionPersona> p_RelacionesPersona;
		
		public EntradaEliminarBorradorRelacionPersona(){
			this.p_RelacionesPersona = new ArrayList<RelacionPersona>();
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaEliminarBorradorRelacionPersona jin = gson.fromJson(in.toString(), EntradaEliminarBorradorRelacionPersona.class);
		SalidaAdministrarBorrador out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaAdministrarBorrador logica(Connection con, EntradaEliminarBorradorRelacionPersona entrada) throws Exception {
		logger.info("INICIO::EliminarBorradorRelacionPersona");
		long inicioServicio = System.currentTimeMillis();
		SalidaAdministrarBorrador out = new SalidaAdministrarBorrador();

		EntradaValidarExistenciaBorradorRelacionPersona entradaValidarExistenciaBorradorRelacionPersona = new EntradaValidarExistenciaBorradorRelacionPersona();
		entradaValidarExistenciaBorradorRelacionPersona.p_RelacionesPersonas = entrada.p_RelacionesPersona;
		entradaValidarExistenciaBorradorRelacionPersona.p_IdEmpresa = entrada.p_IdEmpresa;

		ValidarExistenciaBorradorRelacionPersona validarExistenciaBorradorRelacionPersona = new ValidarExistenciaBorradorRelacionPersona();
		List<Validacion> validaciones = validarExistenciaBorradorRelacionPersona.logica(con, entradaValidarExistenciaBorradorRelacionPersona).validaciones;
		out.agregarValidacionesSalida(validaciones);
		
		if(out.P_OUT_ERRORESCONTADOR > 0) {
			return out;
		}
		
		eliminarRelacionesPersona(con, entrada.p_RelacionesPersona);
		
		out.P_OUT_REFID = obtenerIdsEliminados(entrada.p_RelacionesPersona);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::EliminarBorradorRelacionPersona", finServicio - inicioServicio);
		return out;
	}
	
	private void eliminarRelacionesPersona(Connection con, List<RelacionPersona> relacionesPersona) throws Exception {
		logger.debug("INICIO::EliminarBorradorRelacionPersona.eliminarRelacionesPersona");
		long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		sql.batch(con, SQL_DELETE_RELACION_PERSONA, relacionesPersona, RelacionPersona.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::EliminarBorradorRelacionPersona.eliminarRelacionesPersona", fin - inicio);
	}
	
	private List<ResultadoAdministrarBorradorEntidad> obtenerIdsEliminados(List<RelacionPersona> relacionesPersona){
		logger.trace("INICIO::EliminarBorradorRelacionPersona.obtenerIdsEliminados");
		long inicio = System.currentTimeMillis();
		
		List<ResultadoAdministrarBorradorEntidad> idsEliminados = relacionesPersona.parallelStream().map(relacionPersona->{
			ResultadoAdministrarBorradorEntidad resultadoCreacionBorradorEntidad = new ResultadoAdministrarBorradorEntidad();
			resultadoCreacionBorradorEntidad.IdInterno = relacionPersona.IdInterno;
			resultadoCreacionBorradorEntidad.IdEntidad = relacionPersona.IdRelacionPersona;
			return resultadoCreacionBorradorEntidad;
		}).collect(Collectors.toList());
		long fin = System.currentTimeMillis();
		
		logger.trace("FIN::EliminarBorradorRelacionPersona.obtenerIdsEliminados", fin - inicio);
		return idsEliminados;
	}

}
