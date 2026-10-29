package cl.bcs.APV;

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

import cl.bcs.APV.GrabarContratoSAAPV.ConsultaRelacionPersona;
import cl.bcs.custodia.utils.ValorizarCustodia.ObjUpdateTablas;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class AnularTraspasoEgreso implements JavaCall{
	
	private static final Logger logger = LogManager.getLogger(AnularTraspasoEgreso.class);
	
	public final String SQL_ANULAR_TRASPASOS = loadResourceToString("cl/bcs/APV/sql/AnularTraspasosEgreso.sql");
	public final String SQL_ANULAR_DETALLE_TRASPASOS = loadResourceToString("cl/bcs/APV/sql/AnularDetalleTraspasosEgreso.sql");
	public final String SQL_REACTIVAR_CONTRATOS = loadResourceToString("cl/bcs/APV/sql/ReactivarContratoTraspasosEgreso.sql");

	
	public static class AnularTraspasoEgresoIn {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public List<Integer>	p_TraspasosValidos;
		public List<Integer>	p_TraspasosInValidos;
		
	    public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Traspasos {
		public Long IdTraspaso;
		public Long IdTrabajo;
	}
	public static class  AnularTraspasoEgresoSalida {		
		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;
		
		public  AnularTraspasoEgresoSalida(){
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	


	
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		AnularTraspasoEgresoIn jin = gson.fromJson(in.toString(), AnularTraspasoEgresoIn.class);
		AnularTraspasoEgresoSalida out = logica(conn,jin);		
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	};
	
			
	public AnularTraspasoEgresoSalida logica(Connection con, AnularTraspasoEgresoIn objin) throws Exception {
	
		logger.info("Inicio servicio anulacion de traspaso egreso APV");
		
		SQL sql = new SQL();
		AnularTraspasoEgresoSalida out = new AnularTraspasoEgresoSalida();
		out.P_OUT_ERRORESCONTADOR = 0L;
		// out debe llevar los traspasos anulados y los no anulados, contador errores y detalle errores
		if(objin.p_TraspasosValidos.size() > 0 ) {
			
			Long idTrabajo = new Double(Math.random()*10000000000L).longValue();
			List<Traspasos> trasObj = new ArrayList<>();
			for (Integer p : objin.p_TraspasosValidos) {
				Traspasos tras = new Traspasos();
				tras.IdTrabajo = idTrabajo;
				tras.IdTraspaso = Long.parseLong(p+"");
				trasObj.add(tras);
			}
			
			logger.info("Inicio anulacion de traspaso");
			sql.batch(con, SQL_ANULAR_TRASPASOS, trasObj, Traspasos.class);
		
			logger.info("Fin anulacion de traspaso");
			
			logger.info("Inicio anulacion detalle traspaso");
			sql.batch(con, SQL_ANULAR_DETALLE_TRASPASOS, trasObj, Traspasos.class);
			logger.info("Fin anulacion detalle traspaso");

			
			try {
				logger.info("Inicio reactivacion de contrato relacionado a traspaso");
				sql.batch(con, SQL_REACTIVAR_CONTRATOS, trasObj, Traspasos.class);
			}catch(Exception e) {
				System.out.println(e.toString());
			}
			
		
			logger.info("Fin reactivacion de contrato relacionado a traspaso");
			
			out.P_OUT_ERRORESCONTADOR = 0L;
			logger.info("Traspasos anulados: "+ objin.p_TraspasosValidos.toString());
			logger.info("Traspasos no validos para anular: "+ objin.p_TraspasosInValidos.toString());
			
			
		}else {
			
			logger.info("No existen traspasos validos para anular.");
			out.P_OUT_ERRORESCONTADOR = 1L;
			
			MensajeSalida msg = new MensajeSalida();
			msg.id = 0L;
			msg.tipo = "ERROR";
			msg.codigo = "";
			msg.descripcion = "No existen traspasos validos para anular.";
			out.P_OUT_MENSAJES.add(msg);
			agregarLogs(con, objin);

			return out;
		}
		agregarLogs(con, objin);

		logger.info("Fin servicio anulacion de traspaso egreso APV");
		return out;
	}


	private void agregarLogs(Connection con, AnularTraspasoEgresoIn objin) throws Exception {
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, objin.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
		for (Integer ord : objin.p_TraspasosValidos) {
			AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
			log.IdCabeceraLog = ord.longValue();
			log.IdTipoLog = "EGRESO";
			log.IdModulo = "APV";
			log.IdUsuario = objin.p_IdUsuario;
			log.IdEmpresa = objin.p_IdEmpresa;
			log.FechaSistemaLog = fechaSistema;
			log.IdUsuarioAtendido = objin.p_IdUsuario;
			log.IdRegistro =ord.longValue()+"";
			log.IdTipoRegistro = "TRASPASOEGRESOVALIDO";
			
			log.IdProceso = 0L;
			log.EstadoProceso = "";
			log.NombreProceso = "";						
			entradaAgregarLog.p_Log.add(log);
		}
		
		for (Integer ord : objin.p_TraspasosInValidos) {
			AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
			log.IdCabeceraLog = ord.longValue();
			log.IdTipoLog = "EGRESO";
			log.IdModulo = "APV";
			log.IdUsuario = objin.p_IdUsuario;
			log.IdEmpresa = objin.p_IdEmpresa;
			log.FechaSistemaLog = fechaSistema;
			log.IdUsuarioAtendido = objin.p_IdUsuario;
			log.IdRegistro =ord.longValue()+"";
			log.IdTipoRegistro = "TRASPASOEGRESOINVALIDO";
			
			log.IdProceso = 0L;
			log.EstadoProceso = "";
			log.NombreProceso = "";						
			entradaAgregarLog.p_Log.add(log);
		}
		AgregarLog agregarLogService = new AgregarLog();
		agregarLogService.logica(con, entradaAgregarLog);
		System.out.print("asd");
	}
	
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		
		try (Connection con = PlataformaUtils.getMASTER01Connection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String json="{\"p_IdEmpresa\":1,\"p_IdUsuario\":0,\"p_IdModulo\":\"APV\",\"p_TraspasosValidos\":[2991600],\"p_TraspasosInValidos\":[2991601]}";
			
			AnularTraspasoEgresoIn Objin = gson.fromJson(json, AnularTraspasoEgresoIn.class);
			
			AnularTraspasoEgreso demo = new AnularTraspasoEgreso();


			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			System.out.println("SALIDA");
	 			
 			try {
 				AnularTraspasoEgresoSalida ObjOut = demo.logica(con, Objin);
 				con.commit();
 				System.out.println(ObjOut.toString());
 				con.close();
 			}
 			catch(Exception er) {
				//er.printStackTrace();	
			}
		}
		System.out.println("== END ==");
	}
}