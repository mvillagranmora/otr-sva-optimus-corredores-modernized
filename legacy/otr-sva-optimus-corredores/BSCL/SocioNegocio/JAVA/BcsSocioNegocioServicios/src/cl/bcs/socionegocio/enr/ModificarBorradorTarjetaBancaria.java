package cl.bcs.socionegocio.enr;

import cl.bcs.socionegocio.enr.ValidarExistenciaBorradorTarjetaBancaria.EntradaValidarExistenciaBorradorTarjetaBancaria;
import cl.bcs.socionegocio.enr.pojo.EntradaAdministrarBorrador;
import cl.bcs.socionegocio.enr.pojo.ResultadoAdministrarBorradorEntidad;
import cl.bcs.socionegocio.enr.pojo.SalidaAdministrarBorrador;
import cl.bcs.socionegocio.enr.pojo.TarjetaBancaria;
import cl.bcs.socionegocio.enr.pojo.Validacion;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ModificarBorradorTarjetaBancaria implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(ModificarBorradorTarjetaBancaria.class);

	private String SQL_UPDATE_TARJETA_BANCARIA = loadResourceToString("cl/bcs/socionegocio/enr/sql/ModificarBorradorTarjetaBancaria_UpdateTarjetaBancaria.sql");
	
	public static class EntradaModificarBorradorTarjetaBancaria extends EntradaAdministrarBorrador{
		public List<TarjetaBancaria> p_TarjetasBancarias;
		
		public EntradaModificarBorradorTarjetaBancaria(){
			this.p_TarjetasBancarias = new ArrayList<TarjetaBancaria>();
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaModificarBorradorTarjetaBancaria jin = gson.fromJson(in.toString(), EntradaModificarBorradorTarjetaBancaria.class);
		SalidaAdministrarBorrador out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaAdministrarBorrador logica(Connection con, EntradaModificarBorradorTarjetaBancaria entrada) throws Exception {
		logger.info("INICIO::ModificarBorradorTarjetaBancaria");
		long inicioServicio = System.currentTimeMillis();
		SalidaAdministrarBorrador out = new SalidaAdministrarBorrador();

		out = entrada.validarCamposObligatoriosEntrada(con, entrada.p_TarjetasBancarias, TarjetaBancaria.class, out);
		out = entrada.validarGrillasObligatoriasEntrada(con, entrada.p_TarjetasBancarias, "TarjetasBancarias", out);

		if (out.P_OUT_ERRORESCONTADOR > 0) {
			return out;
		}

		EntradaValidarExistenciaBorradorTarjetaBancaria entradaValidarExistenciaBorradorTarjetaBancaria = new EntradaValidarExistenciaBorradorTarjetaBancaria();
		entradaValidarExistenciaBorradorTarjetaBancaria.p_TarjetasBancarias = entrada.p_TarjetasBancarias;
		entradaValidarExistenciaBorradorTarjetaBancaria.p_IdEmpresa = entrada.p_IdEmpresa;

		ValidarExistenciaBorradorTarjetaBancaria validarExistenciaBorradorTarjetaBancaria = new ValidarExistenciaBorradorTarjetaBancaria();
		List<Validacion> validaciones = validarExistenciaBorradorTarjetaBancaria.logica(con, entradaValidarExistenciaBorradorTarjetaBancaria).validaciones;
		out.agregarValidacionesSalida(validaciones);

		if (out.P_OUT_ERRORESCONTADOR > 0) {
			return out;
		}

		actualizarTarjetasBancarias(con, entrada.p_TarjetasBancarias);

		out.P_OUT_REFID = obtenerIdsActualizados(entrada.p_TarjetasBancarias);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarBorradorTarjetaBancaria", finServicio - inicioServicio);
		return out;
	}
	
	private void actualizarTarjetasBancarias(Connection con, List<TarjetaBancaria> tarjetasBancarias) throws Exception {
		logger.debug("INICIO::ModificarBorradorTarjetaBancaria.actualizarTarjetasBancarias");
		long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		sql.batch(con, SQL_UPDATE_TARJETA_BANCARIA, tarjetasBancarias, TarjetaBancaria.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarBorradorTarjetaBancaria.actualizarTarjetasBancarias", fin - inicio);
	}
	
	private List<ResultadoAdministrarBorradorEntidad> obtenerIdsActualizados(List<TarjetaBancaria> tarjetasBancarias){
		logger.trace("INICIO::ModificarBorradorTarjetaBancaria.obtenerIdsActualizados");
		long inicio = System.currentTimeMillis();
		
		List<ResultadoAdministrarBorradorEntidad> idsInsertados = tarjetasBancarias.parallelStream().map(tarjetaBancaria->{
			ResultadoAdministrarBorradorEntidad resultadoCreacionBorradorEntidad = new ResultadoAdministrarBorradorEntidad();
			resultadoCreacionBorradorEntidad.IdInterno = tarjetaBancaria.IdInterno;
			resultadoCreacionBorradorEntidad.IdEntidad = tarjetaBancaria.IdTarjetaBancaria;
			return resultadoCreacionBorradorEntidad;
		}).collect(Collectors.toList());
		long fin = System.currentTimeMillis();
		
		logger.trace("FIN::ModificarBorradorTarjetaBancaria.obtenerIdsActualizados", fin - inicio);
		return idsInsertados;
	}

}
