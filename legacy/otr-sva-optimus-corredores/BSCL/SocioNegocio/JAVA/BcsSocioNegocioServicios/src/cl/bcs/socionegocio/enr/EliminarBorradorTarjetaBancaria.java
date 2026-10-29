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

import cl.bcs.socionegocio.enr.ValidarExistenciaBorradorTarjetaBancaria.EntradaValidarExistenciaBorradorTarjetaBancaria;
import cl.bcs.socionegocio.enr.pojo.EntradaAdministrarBorrador;
import cl.bcs.socionegocio.enr.pojo.ResultadoAdministrarBorradorEntidad;
import cl.bcs.socionegocio.enr.pojo.SalidaAdministrarBorrador;
import cl.bcs.socionegocio.enr.pojo.TarjetaBancaria;
import cl.bcs.socionegocio.enr.pojo.Validacion;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class EliminarBorradorTarjetaBancaria implements JavaCall {
	
static final Logger logger = LogManager.getLogger(EliminarBorradorTarjetaBancaria.class);

	private String SQL_DELETE_TARJETA_BANCARIA = loadResourceToString("cl/bcs/socionegocio/enr/sql/EliminarBorradorTarjetaBancaria_DeleteTarjetaBancaria.sql");
	
	public static class EntradaEliminarBorradorTarjetaBancaria extends EntradaAdministrarBorrador{
		public List<TarjetaBancaria> p_TarjetasBancarias;
		
		public EntradaEliminarBorradorTarjetaBancaria(){
			this.p_TarjetasBancarias = new ArrayList<TarjetaBancaria>();
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaEliminarBorradorTarjetaBancaria jin = gson.fromJson(in.toString(), EntradaEliminarBorradorTarjetaBancaria.class);
		SalidaAdministrarBorrador out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaAdministrarBorrador logica(Connection con, EntradaEliminarBorradorTarjetaBancaria entrada) throws Exception {
		logger.info("INICIO::EliminarBorradorTarjetaBancaria");
		long inicioServicio = System.currentTimeMillis();
		SalidaAdministrarBorrador out = new SalidaAdministrarBorrador();

		EntradaValidarExistenciaBorradorTarjetaBancaria entradaValidarExistenciaBorradorTarjetaBancaria = new EntradaValidarExistenciaBorradorTarjetaBancaria();
		entradaValidarExistenciaBorradorTarjetaBancaria.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaValidarExistenciaBorradorTarjetaBancaria.p_TarjetasBancarias = entrada.p_TarjetasBancarias;

		ValidarExistenciaBorradorTarjetaBancaria validarExistenciaBorradorTarjetaBancaria = new ValidarExistenciaBorradorTarjetaBancaria();
		List<Validacion> validaciones = validarExistenciaBorradorTarjetaBancaria.logica(con, entradaValidarExistenciaBorradorTarjetaBancaria).validaciones;
		out.agregarValidacionesSalida(validaciones);
		
		if(out.P_OUT_ERRORESCONTADOR > 0) {
			return out;
		}
		
		eliminarTarjetasBancarias(con, entrada.p_TarjetasBancarias);
		
		out.P_OUT_REFID = obtenerIdsEliminados(entrada.p_TarjetasBancarias);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::EliminarBorradorTarjetaBancaria", finServicio - inicioServicio);
		return out;
	}
	
	private void eliminarTarjetasBancarias(Connection con, List<TarjetaBancaria> tarjetasBancarias) throws Exception {
		logger.debug("INICIO::EliminarBorradorTarjetaBancaria.eliminarTarjetasBancarias");
		long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		sql.batch(con, SQL_DELETE_TARJETA_BANCARIA, tarjetasBancarias, TarjetaBancaria.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::EliminarBorradorTarjetaBancaria.eliminarTarjetasBancarias", fin - inicio);
	}
	
	private List<ResultadoAdministrarBorradorEntidad> obtenerIdsEliminados(List<TarjetaBancaria> tarjetasBancarias){
		logger.trace("INICIO::EliminarBorradorTarjetaBancaria.obtenerIdsEliminados");
		long inicio = System.currentTimeMillis();
		
		List<ResultadoAdministrarBorradorEntidad> idsInsertados = tarjetasBancarias.parallelStream().map(tarjetaBancaria->{
			ResultadoAdministrarBorradorEntidad resultadoCreacionBorradorEntidad = new ResultadoAdministrarBorradorEntidad();
			resultadoCreacionBorradorEntidad.IdInterno = tarjetaBancaria.IdInterno;
			resultadoCreacionBorradorEntidad.IdEntidad = tarjetaBancaria.IdTarjetaBancaria;
			return resultadoCreacionBorradorEntidad;
		}).collect(Collectors.toList());
		long fin = System.currentTimeMillis();
		
		logger.trace("FIN::EliminarBorradorTarjetaBancaria.obtenerIdsEliminados", fin - inicio);
		return idsInsertados;
	}

}
