package cl.bcs.socionegocio.enr;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.socionegocio.enr.pojo.EntradaAdministrarBorrador;
import cl.bcs.socionegocio.enr.pojo.Indicador;
import cl.bcs.socionegocio.enr.pojo.ResultadoAdministrarBorradorEntidad;
import cl.bcs.socionegocio.enr.pojo.SalidaAdministrarBorrador;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearBorradorIndicador implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(CrearBorradorIndicador.class);
	
	private String SQL_INSERT_INDICADOR = loadResourceToString("cl/bcs/socionegocio/enr/sql/CrearBorradorIndicador_InsertIndicador.sql");
	
	public static class EntradaCrearBorradorIndicador extends EntradaAdministrarBorrador{
		public List<Indicador> p_Indicadores;
		
		public EntradaCrearBorradorIndicador(){
			this.p_Indicadores = new ArrayList<Indicador>();
		}
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaCrearBorradorIndicador jin = gson.fromJson(in.toString(), EntradaCrearBorradorIndicador.class);
		SalidaAdministrarBorrador out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaAdministrarBorrador logica(Connection con, EntradaCrearBorradorIndicador entrada) throws Exception {
		logger.info("INICIO::CrearBorradorIndicador");
		long inicioServicio = System.currentTimeMillis();
		SalidaAdministrarBorrador out = new SalidaAdministrarBorrador();
		
		out = entrada.validarCamposObligatoriosEntrada(con, entrada.p_Indicadores, Indicador.class, out);
		out = entrada.validarGrillasObligatoriasEntrada(con, entrada.p_Indicadores, "Indicadores", out);
		if(out.P_OUT_ERRORESCONTADOR > 0) {
			return out;
		}
		
		List<Indicador> indicadores = obtenerIndicadores(con, entrada.p_Indicadores);
		
		insertarIndicadores(con, indicadores);
		
		out.P_OUT_REFID = obtenerIdsInsertados(indicadores);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::CrearBorradorIndicador", finServicio - inicioServicio);
		return out;
		
	}
	
	private List<Indicador> obtenerIndicadores(Connection con, List<Indicador> borradoresIndicadores) throws Exception{
		logger.trace("INICIO::CrearBorradorIndicador.obtenerIndicadores");
		long inicio = System.currentTimeMillis();
		
		List<Long> secuencias = Utils.getSecuencia(con, "BSCL.SocioNegocio.BD.Secuencias::BorradorIndicador", Long.valueOf(borradoresIndicadores.size()));
		
		AtomicInteger index = new AtomicInteger();
		List<Indicador> indicadores = borradoresIndicadores.parallelStream().map(borradorIndicador->{
			Long idIndicador = secuencias.get(index.getAndIncrement());
			Indicador indicador = obtenerEntidadIndicador(borradorIndicador, idIndicador);
			return indicador;
		}).collect(Collectors.toList());
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearBorradorIndicador.obtenerIndicadores", fin - inicio);
		return indicadores;
	}
	
	private Indicador obtenerEntidadIndicador(Indicador borradorIndicador, Long idIndicador) {
		logger.trace("INICIO::CrearBorradorIndicador.obtenerEntidadIndicador");
		long inicio = System.currentTimeMillis();
		
		Indicador indicador = new Indicador();
		indicador.IdIndicador = idIndicador;
		indicador.IdInterno = borradorIndicador.IdInterno;
		indicador.IdEnrolamiento = borradorIndicador.IdEnrolamiento;
		indicador.IdTipoEntidad = borradorIndicador.IdTipoEntidad;
		indicador.IdTipoIndicador = borradorIndicador.IdTipoIndicador;
		indicador.ModeloCliente = borradorIndicador.ModeloCliente;
		indicador.IdEmpresa = borradorIndicador.IdEmpresa;
		indicador.ValorIndicador = borradorIndicador.ValorIndicador;
		indicador.IdIndicadorPersona = borradorIndicador.IdIndicadorPersona;
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearBorradorIndicador.obtenerEntidadIndicador", fin - inicio);
		return indicador;
	}
	
	private void insertarIndicadores(Connection con, List<Indicador> indicadores) throws Exception {
		logger.debug("INICIO::CrearBorradorIndicador.insertarIndicadores");
		long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		sql.batch(con, SQL_INSERT_INDICADOR, indicadores, Indicador.class);
		long fin = System.currentTimeMillis();

		logger.debug("FIN::CrearBorradorIndicador.insertarIndicadores", fin - inicio);
	}
	
	private List<ResultadoAdministrarBorradorEntidad> obtenerIdsInsertados(List<Indicador> indicadores){
		logger.trace("INICIO::CrearBorradorIndicador.obtenerIdsInsertados");
		long inicio = System.currentTimeMillis();
		
		List<ResultadoAdministrarBorradorEntidad> idsInsertados = indicadores.parallelStream().map(indicador->{
			ResultadoAdministrarBorradorEntidad resultadoCreacionBorradorEntidad = new ResultadoAdministrarBorradorEntidad();
			resultadoCreacionBorradorEntidad.IdInterno = indicador.IdInterno;
			resultadoCreacionBorradorEntidad.IdEntidad = indicador.IdIndicador;
			return resultadoCreacionBorradorEntidad;
		}).collect(Collectors.toList());
		long fin = System.currentTimeMillis();
		
		logger.trace("FIN::CrearBorradorIndicador.obtenerIdsInsertados", fin - inicio);
		return idsInsertados;
	}

}
