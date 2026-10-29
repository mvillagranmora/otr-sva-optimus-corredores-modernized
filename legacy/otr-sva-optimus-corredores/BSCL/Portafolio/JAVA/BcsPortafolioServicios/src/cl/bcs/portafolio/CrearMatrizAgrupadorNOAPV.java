package cl.bcs.portafolio;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.portafolio.pojo.MatrizAgrupadorNOAPV;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearMatrizAgrupadorNOAPV implements JavaCall{
	
	static final Logger logger = LogManager.getLogger(CrearMatrizAgrupadorNOAPV.class);
	private final String SQL_SELECT_MATRIZ_AGRUPADOR_NO_APV = loadResourceToString("cl/bcs/portafolio/sql/ComunMatrizAgrupadorNOAPV_SelectMatrizAgrupadorNOAPV.sql");
	private final String SQL_INSERT_MATRIZ_AGRUPADOR_NO_APV = loadResourceToString("cl/bcs/portafolio/sql/CrearMatrizAgrupadorNOAPV_InsertMatrizAgrupadorNOAPV.sql");
	
	public static class EntradaCrearMatrizAgrupadorNOAPV{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_SimularOperacion;
		public String p_UsaReglasNegocio;
		public List<MatrizAgrupadorNOAPV> p_MatrizAgrupadorNOAPV;
		
		public EntradaCrearMatrizAgrupadorNOAPV() {
			this.p_MatrizAgrupadorNOAPV = new ArrayList<MatrizAgrupadorNOAPV>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaCrearMatrizAgrupadorNOAPV{
		public Long P_OUT_ERRORESCONTADOR;
		public List <MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaCrearMatrizAgrupadorNOAPV() {
			this.P_OUT_ERRORESCONTADOR=0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		EntradaCrearMatrizAgrupadorNOAPV jin = gson.fromJson(in.toString(), EntradaCrearMatrizAgrupadorNOAPV.class);
		SalidaCrearMatrizAgrupadorNOAPV out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaCrearMatrizAgrupadorNOAPV logica(Connection con, EntradaCrearMatrizAgrupadorNOAPV entrada) throws Exception {
		logger.info("INICIO::CrearMatrizAgrupadorNOAPV");
		SalidaCrearMatrizAgrupadorNOAPV out = new SalidaCrearMatrizAgrupadorNOAPV();
		
		SQL sql=new SQL();
		
		List<String> codigoPortafolios = entrada.p_MatrizAgrupadorNOAPV.stream().map(matriz -> matriz.CodigoPortafolio).collect(Collectors.toList());
		
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("CodigoPortafolio", codigoPortafolios);
		params.put("IdMatrizAgrupadorNOAPV", new ArrayList<Long>());
		
		List<MatrizAgrupadorNOAPV> matrizAgrupadorExistentes = sql.query(con, SQL_SELECT_MATRIZ_AGRUPADOR_NO_APV, params, MatrizAgrupadorNOAPV.class);
		
		for(MatrizAgrupadorNOAPV matriz : matrizAgrupadorExistentes) {
			HashMap<String,String> paramsMensaje = new HashMap<String,String>();
			paramsMensaje.put("CodigoPortafolio", matriz.CodigoPortafolio);
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"CODIGO_PORTAFOLIO_YA_EXISTE_MATRIZ",paramsMensaje,con,entrada.p_IdEmpresa));
			out.P_OUT_ERRORESCONTADOR += 1L;
		}
		
		if(out.P_OUT_ERRORESCONTADOR > 0) {
			return out;
		}
		
		sql.batch(con, SQL_INSERT_MATRIZ_AGRUPADOR_NO_APV, entrada.p_MatrizAgrupadorNOAPV, MatrizAgrupadorNOAPV.class);
		
		out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_OK,"MATRIZ_CREADA_EXISTOSA",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
		
		logger.info("FIN::CrearMatrizAgrupadorNOAPV");
		return out;
	}

}
