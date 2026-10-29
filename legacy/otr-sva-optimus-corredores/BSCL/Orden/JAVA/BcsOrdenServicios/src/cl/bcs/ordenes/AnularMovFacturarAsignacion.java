package cl.bcs.ordenes;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.ordenes.utils.OrdenUtils;
import cl.bcs.ordenes.utils.OrdenUtils.AdvertenciaDetalle;
import cl.bcs.ordenes.utils.OrdenUtils.ErrorDetalle;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;

public class AnularMovFacturarAsignacion implements JavaCall{
	private final Log log = new Log("ORD.AnularMovFacturarAsignacion");
	
	public static class Asignacion_Entrada {
		public Long	IdAsignacion;
	
		public String toString() {
			return Utils.printJson(this, false);
		};
	};
	
	public static class ObjSalidaResumen {
		public Long			IdInterno; 
		public Long			IdOperacion; 
		public String		Resultado; 
		public Long  		OK;
		public Long         ADVERTENCIA;
		public Long         AUTORIZACION;
		public Long         RECHAZO; 
	}
	
	public static class AnularMovFacturarAsignacion_Entrada{
		public Long							p_IdEmpresa;
		public Long							p_IdUsuario;
		public String						p_IdModulo;
		//FALTA IMPLEMENTAR LA SIMULACION
		public List<Asignacion_Entrada>	    p_Asignaciones;
		
		public AnularMovFacturarAsignacion_Entrada (){			
			p_Asignaciones	= new ArrayList<Asignacion_Entrada>();
		};
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	};
	
	public static class AnularMovFacturarAsignacion_Salida{
		public Long									P_OUT_ERRORESCONTADOR;
		public List<ObjSalidaResumen>	            P_OUT_SALIDARESUMEN;
		public List<MensajeSalida>					P_OUT_MENSAJES;
		
		public AnularMovFacturarAsignacion_Salida (){
			P_OUT_SALIDARESUMEN     = new ArrayList<ObjSalidaResumen>();
			P_OUT_MENSAJES			= new ArrayList<MensajeSalida>();
			
		};
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	};
	
	@Trace
	public AnularMovFacturarAsignacion_Salida logica(Connection con, AnularMovFacturarAsignacion_Entrada in) throws Exception {
		log.write("Inicio AnularMovFacturarAsignacion");
		log.write(in.toString());
		AnularMovFacturarAsignacion_Salida out = new AnularMovFacturarAsignacion_Salida();
		SQL sql = new SQL();
		
		try {
			
			SQL.exec(con, "CREATE LOCAL TEMPORARY TABLE #AnularMovFacturarAsignacionMasiva_p_asignaciones (\"IdAsignacion\" BIGINT)");
			
			sql.batch(con, "INSERT INTO #AnularMovFacturarAsignacionMasiva_p_asignaciones (\"IdAsignacion\") VALUES (:IdAsignacion)",in.p_Asignaciones, Asignacion_Entrada.class);
			
			HashMap<String,Object> parmsIn =new HashMap<String,Object>();
			parmsIn.put("p_IdEmpresa", in.p_IdEmpresa);
			parmsIn.put("p_IdUsuario", in.p_IdUsuario);
			parmsIn.put("p_IdModulo", in.p_IdModulo);
			
			HashMap<String,Class> parmsOut =new HashMap<String,Class>();
			parmsOut.put("p_out_SalidaResumen", ObjSalidaResumen.class);
			parmsOut.put("p_out_AdvertenciasContador", Long.class);
			parmsOut.put("p_out_AdvertenciasDetalle", OrdenUtils.AdvertenciaDetalle.class);
			parmsOut.put("p_out_ErroresContador", Long.class);
			parmsOut.put("p_out_ErroresDetalle", OrdenUtils.ErrorDetalle.class);
			
			String queryCall = "call \"BSCL.Orden.BD.Servicios::AnularMovFacturarAsignacionMasiva\"(p_IdEmpresa=>:p_IdEmpresa,p_IdUsuario=>:p_IdUsuario,p_IdModulo=>:p_IdModulo,p_asignaciones=>#AnularMovFacturarAsignacionMasiva_p_asignaciones,p_out_SalidaResumen=>$p_out_SalidaResumen,p_out_AdvertenciasContador=>@p_out_AdvertenciasContador, p_out_AdvertenciasDetalle=>$p_out_AdvertenciasDetalle, p_out_ErroresContador=>@p_out_ErroresContador,p_out_ErroresDetalle=>$p_out_ErroresDetalle)";
			HashMap<String, Object> rpt = sql.call(con, queryCall, parmsIn, parmsOut);
			
			out.P_OUT_SALIDARESUMEN = (List<ObjSalidaResumen>) rpt.get("p_out_SalidaResumen");
			List<ErrorDetalle> erroresDetalle = (List<OrdenUtils.ErrorDetalle>) rpt.get("p_out_ErroresDetalle");
			List<AdvertenciaDetalle> advertenciasDetalle = (List<OrdenUtils.AdvertenciaDetalle>) rpt.get("p_out_AdvertenciasDetalle");
			
			if (erroresDetalle != null)
			{
			for (ErrorDetalle err : erroresDetalle) {
				MensajeSalida m = new MensajeSalida(err.IdInterno, MensajeSalida.MENSAJE_ERROR, err.Codigo, err.Descripcion);
				out.P_OUT_MENSAJES.add(m);
				}
			}

			if (advertenciasDetalle != null)
			{
			for (AdvertenciaDetalle adv : advertenciasDetalle) {
				MensajeSalida m;
				if (adv.RequiereAutorizacion.equalsIgnoreCase("S")) {
					m = new MensajeSalida(adv.IdInterno, MensajeSalida.MENSAJE_AUTORIZACION, adv.Codigo, adv.Descripcion);
				}
				else {
					m = new MensajeSalida(adv.IdInterno, MensajeSalida.MENSAJE_ADVERTENCIA, adv.Codigo, adv.Descripcion);
				}
				
				out.P_OUT_MENSAJES.add(m);
			}
			}
		}
		finally {
			SQL.exec(con, "DROP TABLE #AnularMovFacturarAsignacionMasiva_p_asignaciones");
		}
		
		log.write(out.toString());
		log.write("Fin");
		return out;
	};
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		AnularMovFacturarAsignacion_Entrada jin=gson.fromJson(in.toString() ,AnularMovFacturarAsignacion_Entrada.class);
		AnularMovFacturarAsignacion_Salida out=logica(conn,jin);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	};
};

