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

public class ModificarMatrizAgrupadorNOAPV implements JavaCall{
	
	static final Logger logger = LogManager.getLogger(ModificarMatrizAgrupadorNOAPV.class);
	
	private final String SQL_SELECT_MATRIZ_AGRUPADOR_NO_APV = loadResourceToString("cl/bcs/portafolio/sql/ComunMatrizAgrupadorNOAPV_SelectMatrizAgrupadorNOAPV.sql");
	private final String SQL_ACTUALIZAR_MATRIZ_AGRUPADOR_NO_APV = loadResourceToString("cl/bcs/portafolio/sql/ModificarMatrizAgrupadorNOAPV_ActualizarMatrizAgrupadorNOAPV.sql");
	
	public static class EntradaModificarMatrizAgrupadorNOAPV{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_SimularOperacion;
		public String p_UsaReglasNegocio;
		public List<MatrizAgrupadorNOAPV> p_MatrizAgrupadorNOAPV;
		
		public EntradaModificarMatrizAgrupadorNOAPV() {
			this.p_MatrizAgrupadorNOAPV = new ArrayList<MatrizAgrupadorNOAPV>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaModificarMatrizAgrupadorNOAPV{
		public Long P_OUT_ERRORESCONTADOR;
		public List <MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaModificarMatrizAgrupadorNOAPV() {
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
		EntradaModificarMatrizAgrupadorNOAPV jin = gson.fromJson(in.toString(), EntradaModificarMatrizAgrupadorNOAPV.class);
		SalidaModificarMatrizAgrupadorNOAPV out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaModificarMatrizAgrupadorNOAPV logica(Connection con, EntradaModificarMatrizAgrupadorNOAPV entrada) throws Exception {
		logger.info("INICIO::ModificarMatrizAgrupadorNOAPV");
		SalidaModificarMatrizAgrupadorNOAPV out = new SalidaModificarMatrizAgrupadorNOAPV();
		
		SQL sql=new SQL();
		
List<Long> idMatrices = entrada.p_MatrizAgrupadorNOAPV.stream().map(matriz -> matriz.IdMatrizAgrupadorNOAPV).collect(Collectors.toList());
		
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("IdMatrizAgrupadorNOAPV", idMatrices);
		params.put("CodigoPortafolio", new ArrayList<String>());
		
		List<MatrizAgrupadorNOAPV> matrizAgrupadorExistentes = sql.query(con, SQL_SELECT_MATRIZ_AGRUPADOR_NO_APV, params, MatrizAgrupadorNOAPV.class);
		HashMap<Long,Object> existentes = Utils.List2Hash(matrizAgrupadorExistentes, "IdMatrizAgrupadorNOAPV");
		
		for(MatrizAgrupadorNOAPV matriz : entrada.p_MatrizAgrupadorNOAPV) {
			if(existentes.get(matriz.IdMatrizAgrupadorNOAPV) == null) {
				HashMap<String,String> paramsMensaje = new HashMap<String,String>();
				paramsMensaje.put("IdMatrizAgrupadorNOAPV", String.valueOf(matriz.IdMatrizAgrupadorNOAPV));
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"ID_MATRIZ_NO_EXISTE_MATRIZ",paramsMensaje,con,entrada.p_IdEmpresa));
				out.P_OUT_ERRORESCONTADOR += 1L;
			}
		}
		
		if(out.P_OUT_ERRORESCONTADOR > 0) {
			return out;
		}
		
		sql.batch(con, SQL_ACTUALIZAR_MATRIZ_AGRUPADOR_NO_APV, entrada.p_MatrizAgrupadorNOAPV, MatrizAgrupadorNOAPV.class);
		
		out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_OK,"MATRIZ_MODIFICACION_EXISTOSA",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
		
		logger.info("FIN::ModificarMatrizAgrupadorNOAPV");
		return out;
	}

}
