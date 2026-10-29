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

import cl.bcs.socionegocio.enr.pojo.Telefono;
import cl.bcs.socionegocio.enr.ValidarExistenciaBorradorTelefono.EntradaValidarExistenciaBorradorTelefono;
import cl.bcs.socionegocio.enr.pojo.EntradaAdministrarBorrador;
import cl.bcs.socionegocio.enr.pojo.ResultadoAdministrarBorradorEntidad;
import cl.bcs.socionegocio.enr.pojo.SalidaAdministrarBorrador;
import cl.bcs.socionegocio.enr.pojo.Validacion;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class EliminarBorradorTelefono implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(EliminarBorradorTelefono.class);

	private String SQL_DELETE_TELEFONO = loadResourceToString("cl/bcs/socionegocio/enr/sql/EliminarBorradorTelefono_DeleteTelefono.sql");
	
	public static class EntradaEliminarBorradorTelefono extends EntradaAdministrarBorrador{
		public List<Telefono> p_Telefonos;
		
		public EntradaEliminarBorradorTelefono(){
			this.p_Telefonos = new ArrayList<Telefono>();
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaEliminarBorradorTelefono jin = gson.fromJson(in.toString(), EntradaEliminarBorradorTelefono.class);
		SalidaAdministrarBorrador out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaAdministrarBorrador logica(Connection con, EntradaEliminarBorradorTelefono entrada) throws Exception {
		logger.info("INICIO::EliminarBorradorTelefono");
		long inicioServicio = System.currentTimeMillis();
		SalidaAdministrarBorrador out = new SalidaAdministrarBorrador();

		ValidarExistenciaBorradorTelefono validarExistenciaBorradorTelefono = new ValidarExistenciaBorradorTelefono();
		EntradaValidarExistenciaBorradorTelefono entradaValidarExistenciaBorradorTelefono = new EntradaValidarExistenciaBorradorTelefono();
		entradaValidarExistenciaBorradorTelefono.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaValidarExistenciaBorradorTelefono.p_Telefonos = entrada.p_Telefonos;
		
		List<Validacion> validacionesExistenciaTelefonos = validarExistenciaBorradorTelefono.logica(con, entradaValidarExistenciaBorradorTelefono).validaciones;
		out.agregarValidacionesSalida(validacionesExistenciaTelefonos);
		
		if(out.P_OUT_ERRORESCONTADOR > 0) {
			return out;
		}
		
		eliminarTelefonos(con, entrada.p_Telefonos);
		
		out.P_OUT_REFID = obtenerIdsEliminados(entrada.p_Telefonos);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::EliminarBorradorTelefono", finServicio - inicioServicio);
		return out;
	}
	
	
	private void eliminarTelefonos(Connection con, List<Telefono> telefonos) throws Exception {
		logger.debug("INICIO::EliminarBorradorTelefono.eliminarTelefonos");
		long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		sql.batch(con, SQL_DELETE_TELEFONO, telefonos, Telefono.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::EliminarBorradorTelefono.eliminarTelefonos", fin - inicio);
	}
	
	private List<ResultadoAdministrarBorradorEntidad> obtenerIdsEliminados(List<Telefono> telefonos){
		logger.trace("INICIO::EliminarBorradorTelefono.obtenerIdsEliminados");
		long inicio = System.currentTimeMillis();
		
		List<ResultadoAdministrarBorradorEntidad> idsInsertados = telefonos.parallelStream().map(telefono->{
			ResultadoAdministrarBorradorEntidad resultadoCreacionBorradorEntidad = new ResultadoAdministrarBorradorEntidad();
			resultadoCreacionBorradorEntidad.IdInterno = telefono.IdInterno;
			resultadoCreacionBorradorEntidad.IdEntidad = telefono.IdTelefono;
			return resultadoCreacionBorradorEntidad;
		}).collect(Collectors.toList());
		long fin = System.currentTimeMillis();
		
		logger.trace("FIN::EliminarBorradorTelefono.obtenerIdsEliminados", fin - inicio);
		return idsInsertados;
	}
}
