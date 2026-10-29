package cl.bcs.mesa.SPT;

import cl.bcs.mesa.SPT.CrearOperacionSpot.OperacionObjOut;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Utils;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class WSModificarEstadoCajaOperacionSpot implements JavaCall{

	static final Logger logger = LogManager.getLogger(WSModificarEstadoCajaOperacionSpot.class);
	
	public static class WSModificarEstadoCajaOperacionSpotEntrada extends BcsPojo{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_CanalIngreso;
		public List<ActualizarEstadoCajaOperacionSpot> p_ActualizarEstadoCaja;
		
		public WSModificarEstadoCajaOperacionSpotEntrada() {
			this.p_IdModulo = "MSP";
			this.p_ActualizarEstadoCaja = new ArrayList<>();
		}
	}
	
	public static class ActualizarEstadoCajaOperacionSpot extends BcsPojo{
		public Long FolioOperacion;
		public String EstadoCaja;
	}
	
	public static class WSModificarEstadoCajaOperacionSpotSalida extends BcsPojo{
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		public List<OperacionObjOut> P_OUT_IDOPERACIONSPOT;
		
		public WSModificarEstadoCajaOperacionSpotSalida() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_IDOPERACIONSPOT = new ArrayList<OperacionObjOut>();
		}
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		WSModificarEstadoCajaOperacionSpotEntrada entrada = gson.fromJson(in.toString(), WSModificarEstadoCajaOperacionSpotEntrada.class);
		WSModificarEstadoCajaOperacionSpotSalida out = logica(conn, entrada);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public WSModificarEstadoCajaOperacionSpotSalida logica(Connection conn, WSModificarEstadoCajaOperacionSpotEntrada entrada) throws Exception {
		
		logger.info("Inicio servicio WSModificarEstadoCajaOperacionSpot");

		WSModificarEstadoCajaOperacionSpotSalida out = new WSModificarEstadoCajaOperacionSpotSalida();

		if (entrada.p_ActualizarEstadoCaja == null || entrada.p_ActualizarEstadoCaja.isEmpty()) {
			out.P_OUT_ERRORESCONTADOR++;
			out.P_OUT_MENSAJES.add(new MensajeSalida(out.P_OUT_ERRORESCONTADOR, MensajeSalida.MENSAJE_ERROR, "NO_HAY_OPERACIONES_PARA_MODIFICAR", null, conn, entrada.p_IdEmpresa));
			return out;
		}

		ModificarEstadoCajaOperacionSpot.ModificarEstadoCajaOperacionSpotEntrada entradaModificar = new ModificarEstadoCajaOperacionSpot.ModificarEstadoCajaOperacionSpotEntrada();

		entradaModificar.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaModificar.p_IdUsuario = entrada.p_IdUsuario;
		entradaModificar.p_IdModulo = entrada.p_IdModulo;
		entradaModificar.IdCanal = "WBS";

		if (entrada.p_ActualizarEstadoCaja != null) {
			for (ActualizarEstadoCajaOperacionSpot item : entrada.p_ActualizarEstadoCaja) {
				entradaModificar.EstadoCajaModificar.add(new ModificarEstadoCajaOperacionSpot.EstadoCajaEntrada(item.FolioOperacion,item.EstadoCaja));
			}
		}

		ModificarEstadoCajaOperacionSpot ModificarEstadoCajaOperacionSpot = new ModificarEstadoCajaOperacionSpot();

		ModificarEstadoCajaOperacionSpot.ModificarEstadoCajaOperacionSpotSalida salidaModificar = ModificarEstadoCajaOperacionSpot.logica(conn, entradaModificar);

		out.P_OUT_ERRORESCONTADOR = salidaModificar.P_OUT_ERRORESCONTADOR;
		out.P_OUT_MENSAJES = salidaModificar.P_OUT_MENSAJES;

		logger.info("Fin servicio WSModificarEstadoCajaOperacionSpot " + out);

		return out;
	}
	
	
	public static void main(String args[]) throws Exception {
		
		System.out.println("== INICIO PRUEBA LOCAL ==");
		
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa02", "connections.properties")) {
			
			Gson gson = Utils.getGsonBuilder();
			
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":9593,\"p_IdModulo\":\"MSP\",\"p_CanalIngreso\":\"WBS\",\"p_ActualizarEstadoCaja\":[{\"FolioOperacion\":83492236,\"EstadoCaja\":\"CNF\"}]}";
			
			try {
				WSModificarEstadoCajaOperacionSpotEntrada Objin = gson.fromJson(json, WSModificarEstadoCajaOperacionSpotEntrada.class);
				WSModificarEstadoCajaOperacionSpot demo = new WSModificarEstadoCajaOperacionSpot();
				
				System.out.println(Objin.toString());
				
				WSModificarEstadoCajaOperacionSpotSalida ObjOut = demo.logica(con, Objin);
				con.rollback();
				//con.commit();
				
			} catch (Exception er) {
				er.printStackTrace();
				con.rollback();
			}
		}
	}
	
}
