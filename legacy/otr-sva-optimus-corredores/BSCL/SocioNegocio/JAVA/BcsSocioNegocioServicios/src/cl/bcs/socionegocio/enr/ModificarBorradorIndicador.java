package cl.bcs.socionegocio.enr;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.enr.pojo.EntradaAdministrarBorrador;
import cl.bcs.socionegocio.enr.pojo.Indicador;
import cl.bcs.socionegocio.enr.pojo.ResultadoAdministrarBorradorEntidad;
import cl.bcs.socionegocio.enr.pojo.SalidaAdministrarBorrador;
import cl.bcs.socionegocio.enr.pojo.Validacion;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ModificarBorradorIndicador implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(ModificarBorradorIndicador.class);
	
	private String SQL_SELECT_INDICADOR = loadResourceToString("cl/bcs/socionegocio/enr/sql/ModificarBorradorIndicador_SelectIndicador.sql");
	private String SQL_UPDATE_INDICADOR = loadResourceToString("cl/bcs/socionegocio/enr/sql/ModificarBorradorIndicador_UpdateIndicador.sql");
	
	public static class EntradaModificarBorradorIndicador extends EntradaAdministrarBorrador{
		public List<Indicador> p_Indicadores;
		
		public EntradaModificarBorradorIndicador(){
			this.p_Indicadores = new ArrayList<Indicador>();
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaModificarBorradorIndicador jin = gson.fromJson(in.toString(), EntradaModificarBorradorIndicador.class);
		SalidaAdministrarBorrador out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaAdministrarBorrador logica(Connection con, EntradaModificarBorradorIndicador entrada) throws Exception {
		logger.info("INICIO::ModificarBorradorIndicador");
		long inicioServicio = System.currentTimeMillis();
		SalidaAdministrarBorrador out = new SalidaAdministrarBorrador();
		
		out = entrada.validarCamposObligatoriosEntradaIndicadores(con, entrada.p_Indicadores, Indicador.class , out);
		out = entrada.validarGrillasObligatoriasEntrada(con, entrada.p_Indicadores, "Indicadores", out);
		
		if(out.P_OUT_ERRORESCONTADOR > 0) {
			return out;
		}
		
		List<Validacion> validaciones = validarExistenciaIndicadores(con, entrada.p_Indicadores, entrada.p_IdEmpresa);
		out.agregarValidacionesSalida(validaciones);
		
		if(out.P_OUT_ERRORESCONTADOR > 0) {
			return out;
		}
		
		actualizarIndicadores(con, entrada.p_Indicadores);
		
		out.P_OUT_REFID = obtenerIdsActualizados(entrada.p_Indicadores);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarBorradorIndicador", finServicio - inicioServicio);
		return out;
	}
	
	private List<Validacion> validarExistenciaIndicadores(Connection con, List<Indicador> indicadores, Long idEmpresa) throws Exception {
		logger.debug("INICIO::ModificarBorradorIndicador.validarExistenciaTarjetasBancarias");
		long inicio = System.currentTimeMillis();
		
		List<Long> idIndicadores = indicadores.parallelStream().map(indicador->{
			return indicador.IdIndicador;
		}).collect(Collectors.toList());
		
		HashMap<String, Object> parametros = new HashMap<String, Object>(1);
		parametros.put("IdIndicador", idIndicadores);
		
		SQL sql = new SQL();
		List<Indicador> indicadoresExistentes = sql.query(con, SQL_SELECT_INDICADOR, parametros, Indicador.class);
		Map<Long, Indicador> indicadoresPorIdIndicador = indicadoresExistentes.parallelStream().collect(Collectors.toMap(indicador->indicador.IdIndicador, indicador->indicador));
		
		List<Validacion> validaciones = new ArrayList<Validacion>();
		
		for(Indicador indicador : indicadores) {
			if(indicadoresPorIdIndicador.get(indicador.IdIndicador) == null) {
				Validacion validacion = new Validacion();
				validacion.EsError = true;
				HashMap<String,String> datosMensajes = new HashMap<String,String>(1);
				validacion.MensajeError = new MensajeSalida(indicador.IdInterno,MensajeSalida.MENSAJE_ERROR, "UI_ERROR_BORRADOR_INDICADOR_NO_EXISTE", datosMensajes, con, idEmpresa);
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarBorradorIndicador.validarExistenciaTarjetasBancarias", fin - inicio);
		return validaciones;
	}
	
	private void actualizarIndicadores(Connection con, List<Indicador> indicadores) throws Exception {
		logger.debug("INICIO::ModificarBorradorIndicador.actualizarIndicadores");
		long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		sql.batch(con, SQL_UPDATE_INDICADOR, indicadores, Indicador.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarBorradorIndicador.actualizarIndicadores", fin - inicio);
	}
	
	private List<ResultadoAdministrarBorradorEntidad> obtenerIdsActualizados(List<Indicador> indicadores){
		logger.trace("INICIO::ModificarBorradorIndicador.obtenerIdsActualizados");
		long inicio = System.currentTimeMillis();
		
		List<ResultadoAdministrarBorradorEntidad> idsActualizados = indicadores.parallelStream().map(indicador->{
			ResultadoAdministrarBorradorEntidad resultadoCreacionBorradorEntidad = new ResultadoAdministrarBorradorEntidad();
			resultadoCreacionBorradorEntidad.IdInterno = indicador.IdInterno;
			resultadoCreacionBorradorEntidad.IdEntidad = indicador.IdIndicador;
			return resultadoCreacionBorradorEntidad;
		}).collect(Collectors.toList());
		long fin = System.currentTimeMillis();
		
		logger.trace("FIN::ModificarBorradorIndicador.obtenerIdsActualizados", fin - inicio);
		return idsActualizados;
	}

}
