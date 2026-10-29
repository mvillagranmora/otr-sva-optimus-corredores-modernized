package cl.bcs.replica;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class IniciarReplicaDelta implements JavaCall, IniciarReplica {
	
	static final Logger logger = LogManager.getLogger(IniciarReplicaDelta.class);
	
	private final String SQL_INSERT_WORKSTACK = loadResourceToString("cl/bcs/replica/sql/IniciarReplica_InsertWorkstack.sql");
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaIniciarReplica jin = gson.fromJson(in.toString(), EntradaIniciarReplica.class);
		SalidaIniciarReplica out = logica(conn, jin);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	@Override
	public SalidaIniciarReplica logica (Connection conn, EntradaIniciarReplica in) throws Exception {
		logger.info("INICIO::IniciarReplica");
		SQL sql = new SQL();
		SalidaIniciarReplica salida = new SalidaIniciarReplica();
		Workstack workstack = new Workstack();
		workstack.IdStack = Utils.getSecuencia(conn, "BSCL.Plataforma.BD.Secuencias::WorkStack", 1L).get(0);
		workstack.Key = "-1";
		workstack.Table = "INICIAR_REPLICA_PAUTA";
		workstack.Action = "F";
		workstack.Status = 0;
		workstack.Stamp = new Timestamp(System.currentTimeMillis());
		workstack.Parameters = "BCS_query:QueryReplicaDelta;Fecha:" + Utils.getFechaSistema(conn, "BSCL", in.p_IdEmpresa);
		List<Workstack> listWorkstack = new ArrayList<>();
		listWorkstack.add(workstack);
		sql.batch(conn, SQL_INSERT_WORKSTACK, listWorkstack, Workstack.class);
		MensajeSalida mensaje = new MensajeSalida(0L,MensajeSalida.MENSAJE_OK,"INICIAR_REPLICA_PAUTA","Se inicia Replica desde Pauta");
		salida.P_OUT_MENSAJES.add(mensaje);
		salida.P_OUT_ERRORESCONTADOR = 0L;
		logger.info("FIN::IniciarReplica");
		return salida;
	}
	
	public static void main(String[] args) throws Exception,SQLException {
		Connection conn = null;
		Gson gson = Utils.getGsonBuilder();
		try {
			System.out.println("== BEGIN ==");
			conn = PlataformaUtils.getDESAConnection();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":4}";
			EntradaIniciarReplica entrada = gson.fromJson(json, EntradaIniciarReplica.class);
			IniciarReplicaDelta iniciarReplica = new IniciarReplicaDelta();
			SalidaIniciarReplica salida = iniciarReplica.logica(conn, entrada);
			System.out.println(salida.toString());
			//conn.rollback();
			conn.commit();
		}catch (Exception e){
			e.printStackTrace();
			conn.rollback();
		}finally {
			conn.close();
		}
		System.out.println("== END ==");
	};
}
