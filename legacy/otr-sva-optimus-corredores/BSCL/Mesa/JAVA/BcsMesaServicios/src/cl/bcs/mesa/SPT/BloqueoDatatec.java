package cl.bcs.mesa.SPT;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.mesa.utils.ConsultarConfiguracionMesa;
import cl.bcs.mesa.utils.ConsultarConfiguracionMesa.ConfiguracionMesa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class BloqueoDatatec implements JavaCall{
	
	private static final Logger logger = LogManager.getLogger(BloqueoDatatec.class);
	
	private final String SQL_UPDATE_USUARIO_BLOQUEO = loadResourceToString("cl/bcs/mesa/SPT/sql/BloqueoDatatec_UpdateUsuarioBloqueo.sql");
	
	
	public static class BloqueoDatatecEntrada {
		public Long p_IdEmpresa;
		public String p_CodigoUsuario;
		public String p_BloqueoDesbloqueo = "B";
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class BloqueoDatatecSalida {
		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;
		public String				P_OUT_VALOR;
		public String				P_OUT_MODIFICADORPARAMETRO;

		public BloqueoDatatecSalida() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}

		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class DatosBloqueoDatatec {
		public Long IdEmpresa;
		public String Valor;
		public String ModificadorParametro;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		BloqueoDatatecEntrada in = gson.fromJson(jin.toString(), BloqueoDatatecEntrada.class);
		BloqueoDatatecSalida out = logica(con, in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public BloqueoDatatecSalida logica(Connection conn, BloqueoDatatecEntrada in) throws Exception {
		Long init = System.currentTimeMillis();
		logger.info("Inicio servicio BloqueoDatatec");
		
		SQL _sql = new SQL();
		BloqueoDatatecSalida out = new BloqueoDatatecSalida();
		out.P_OUT_ERRORESCONTADOR = 0L;
		HashMap<String,String> mapMsg = new HashMap<>();
		
		try {
			ConfiguracionMesa configuracionMesa = new ConsultarConfiguracionMesa().logica(conn, in.p_IdEmpresa, "MEME", "USUARIOBLOQUEODATATEC");

			//Consulta de solo lectura: no muta el parametro, solo informa el estado actual.
			//La usa la ventana Datatec Monitor Automatico para saber si esta bloqueado (y por
			//quien) sin arriesgar el toggle de 'B' descrito mas abajo.
			if ("C".equals(in.p_BloqueoDesbloqueo)) {
				out.P_OUT_VALOR = configuracionMesa.Valor;
				out.P_OUT_MODIFICADORPARAMETRO = configuracionMesa.ModificadorParametro;
				return out;
			}

			if ("S".equals(configuracionMesa.Valor) && "B".equals(in.p_BloqueoDesbloqueo) &&
					!in.p_CodigoUsuario.equals(configuracionMesa.ModificadorParametro)) {
				mapMsg.put("Usuario", configuracionMesa.ModificadorParametro);
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_MODULO_DATATEC_BLOQUEADO_POR",mapMsg,conn,in.p_IdEmpresa));
				out.P_OUT_ERRORESCONTADOR ++;
				out.P_OUT_VALOR = configuracionMesa.Valor;
				out.P_OUT_MODIFICADORPARAMETRO = configuracionMesa.ModificadorParametro;
				return out;
			} else if("S".equals(configuracionMesa.Valor) && "B".equals(in.p_BloqueoDesbloqueo)
					&& in.p_CodigoUsuario.equals(configuracionMesa.ModificadorParametro)){
				in.p_BloqueoDesbloqueo = "D";
			}

			List<DatosBloqueoDatatec> datosBloqueoDatatecList = new ArrayList<BloqueoDatatec.DatosBloqueoDatatec>();
			DatosBloqueoDatatec datosBloqueoDatatec = new DatosBloqueoDatatec();
			datosBloqueoDatatec.IdEmpresa = in.p_IdEmpresa;
			datosBloqueoDatatec.Valor = "B".equals(in.p_BloqueoDesbloqueo) ? "S" : "N";
			datosBloqueoDatatec.ModificadorParametro = "B".equals(in.p_BloqueoDesbloqueo) ? in.p_CodigoUsuario : "";
			datosBloqueoDatatecList.add(datosBloqueoDatatec);

			//Actualiza Valor y ModificadorParametro del parametro USUARIOBLOQUEODATATEC
			_sql.batch(conn, SQL_UPDATE_USUARIO_BLOQUEO, datosBloqueoDatatecList, DatosBloqueoDatatec.class);

			out.P_OUT_VALOR = datosBloqueoDatatec.Valor;
			out.P_OUT_MODIFICADORPARAMETRO = datosBloqueoDatatec.ModificadorParametro;

		}
		catch(Exception e){
			out.P_OUT_ERRORESCONTADOR = 1L;
			logger.error(e);
			throw e;
		}
		
		logger.info("Termino del servicio BloqueoDatatec, tiempo:" + (System.currentTimeMillis() - init));
		return out;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String jsonB = "{\"p_IdEmpresa\":1,\"p_CodigoUsuario\":\"MURIBE\",\"p_BloqueoDesbloqueo\":\"B\"}";
			//String jsonD = "{\"p_IdEmpresa\":1,\"p_CodigoUsuario\":\"\",\"p_BloqueoDesbloqueo\":\"D\"}";
			BloqueoDatatecEntrada Objin = gson.fromJson(jsonB, BloqueoDatatecEntrada.class);
			
			BloqueoDatatec demo = new BloqueoDatatec();				
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			System.out.println("SALIDA");
 			try {
 				BloqueoDatatecSalida ObjOut = demo.logica(con, Objin);
 				//con.commit();
 				System.out.println(ObjOut.toString());
 			}
 			catch(Exception er) {
				er.printStackTrace();	
				con.rollback();
			}
			finally {
				con.close();
			}
	    }	
		System.out.println("== END ==");
	}
}