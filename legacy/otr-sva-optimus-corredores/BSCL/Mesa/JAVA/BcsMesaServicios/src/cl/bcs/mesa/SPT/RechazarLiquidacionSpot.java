package cl.bcs.mesa.SPT;

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

import cl.bcs.mesa.SPT.AnularOperacionSpot.AnularOperacionSpotEntrada;
import cl.bcs.mesa.SPT.AnularOperacionSpot.AnularOperacionSpotSalida;
import cl.bcs.mesa.SPT.CrearOperacionSpot.OperacionSpot;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class RechazarLiquidacionSpot implements JavaCall{
	
	static final Logger logger = LogManager.getLogger(RechazarLiquidacionSpot.class);
	
	private final String SQL_UPDATE_OPERACION_SPOT = loadResourceToString("cl/bcs/mesa/SPT/sql/RechazarLiquidacionSpot_UpdateOperacionSpot.sql");
	
	public static class EntradaRechazarLiquidacionSpot extends BcsPojo{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public List<OperacionSpotRechazar> p_DatosOperacion;
		
		public EntradaRechazarLiquidacionSpot() {
			this.p_DatosOperacion = new ArrayList<OperacionSpotRechazar>();
		}
	}
	
	public static class OperacionSpotRechazar extends BcsPojo{
		public String NumeroOperacion;
	}
	
	public static class SalidaRechazarLiquidacionSpot extends BcsPojo{
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long	P_OUT_ERRORESCONTADOR;
		public Long	P_OUT_ADVERTENCIASCONTADOR;
		
		public SalidaRechazarLiquidacionSpot() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaRechazarLiquidacionSpot jin = gson.fromJson(in.toString(), EntradaRechazarLiquidacionSpot.class);
		SalidaRechazarLiquidacionSpot out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaRechazarLiquidacionSpot logica(Connection con, EntradaRechazarLiquidacionSpot entrada) throws Exception {
		logger.info("INICIO::RechazarLiquidacionSpot");
		long inicioServicio = System.currentTimeMillis();
		SalidaRechazarLiquidacionSpot out = new SalidaRechazarLiquidacionSpot();
		
		out = anularOperacionSpot(con, entrada);
		actualizarOperacionSpot(con, entrada.p_DatosOperacion);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::RechazarLiquidacionSpot", finServicio - inicioServicio);
		return out;
	}
	
	private SalidaRechazarLiquidacionSpot anularOperacionSpot(Connection con, EntradaRechazarLiquidacionSpot entrada) throws Exception{
		logger.debug("INICIO::RechazarLiquidacionSpot.anularOperacionSpot");
		long inicio = System.currentTimeMillis();
		AnularOperacionSpotEntrada entradaAnularOperacionSpot = new AnularOperacionSpotEntrada();
		entradaAnularOperacionSpot.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaAnularOperacionSpot.p_IdUsuario = entrada.p_IdUsuario;
		entradaAnularOperacionSpot.p_IdModulo = entrada.p_IdModulo;
		entradaAnularOperacionSpot.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
		entradaAnularOperacionSpot.p_DatosOperacion = entrada.p_DatosOperacion.parallelStream().map(operacion->{
			OperacionSpot operacionSpot = new OperacionSpot();
			operacionSpot.NumeroOperacion = operacion.NumeroOperacion;
			return operacionSpot;
		}).collect(Collectors.toList());
		
		AnularOperacionSpot anularOperacionSpot = new AnularOperacionSpot();
		AnularOperacionSpotSalida anularOperacionSpotSalida = anularOperacionSpot.logica(con, entradaAnularOperacionSpot);
		
		SalidaRechazarLiquidacionSpot resultado = new SalidaRechazarLiquidacionSpot();
		resultado.P_OUT_ADVERTENCIASCONTADOR = anularOperacionSpotSalida.P_OUT_ADVERTENCIASCONTADOR;
		resultado.P_OUT_ERRORESCONTADOR = anularOperacionSpotSalida.P_OUT_ERRORESCONTADOR;
		resultado.P_OUT_MENSAJES = anularOperacionSpotSalida.P_OUT_MENSAJES;
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::RechazarLiquidacionSpot.anularOperacionSpot", fin - inicio);
		
		return resultado;
		
	}
	
	private void actualizarOperacionSpot(Connection con, List<OperacionSpotRechazar> operaciones) throws Exception {
		logger.debug("INICIO::RechazarLiquidacionSpot.actualizarOperacionSpot");
		long inicio = System.currentTimeMillis();
		SQL sql = new SQL();
		sql.batch(con, SQL_UPDATE_OPERACION_SPOT, operaciones, OperacionSpotRechazar.class);
		long fin = System.currentTimeMillis();
		logger.debug("FIN::RechazarLiquidacionSpot.actualizarOperacionSpot", fin - inicio);
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getMBIConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String jsonDesa = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":0,\"p_IdModulo\":\"TES\",\"p_UsaReglasNegocio\":\"S\",\"p_DatosOperacion\":[{\"NumeroOperacion\":47455}]}";
			EntradaRechazarLiquidacionSpot Objin = gson.fromJson(jsonDesa, EntradaRechazarLiquidacionSpot.class);
			
			RechazarLiquidacionSpot demo = new RechazarLiquidacionSpot();
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			System.out.println("SALIDA");
 			try {
 				SalidaRechazarLiquidacionSpot ObjOut = demo.logica(con, Objin);
 				con.rollback();
 				System.out.println(ObjOut.toString());
 			}
 			catch(Exception er) {
				con.rollback();
			}
			finally {
				con.close();
			}
	    }
		System.out.println("== END ==");
	}

}
