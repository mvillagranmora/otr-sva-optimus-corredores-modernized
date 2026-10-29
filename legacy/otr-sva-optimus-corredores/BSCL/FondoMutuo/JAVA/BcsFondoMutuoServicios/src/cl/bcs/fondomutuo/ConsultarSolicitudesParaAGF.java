package cl.bcs.fondomutuo;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.fondomutuo.CambiarEstadoSolicitudAgf.EntradaCambiarEstadoSolicitudAgf;
import cl.bcs.fondomutuo.pojo.ConstantesFondoMutuo;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConsultarSolicitudesParaAGF implements JavaCall{

	private final Log log = new Log("FM.ConsultarSolicitudesParaAGF");
	
	private final ConstantesFondoMutuo _c = new ConstantesFondoMutuo();
	
	private final String SQL_SELECT_SOLICITUDES = loadResourceToString("cl/bcs/fondomutuo/sql/ConsultarSolicitudesParaAGF_SelectSolicitudes.sql");
	
	public static class EntradaConsultarSolicitudesParaAGF{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	
	public static class SalidaConsultarSolicitudesParaAGF{
		public Long P_OUT_ERRORESCONTADOR;
		public List <MensajeSalida> P_OUT_MENSAJES;
		public List <OutSolicitudes> P_OUT_RESULTADO;
		
		public SalidaConsultarSolicitudesParaAGF(){
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_RESULTADO = new ArrayList<OutSolicitudes>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	
	public static class OutSolicitudes{
		public Long IdInterfazAgf;
		public String TipoRegistro;
		public String Registro;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		EntradaConsultarSolicitudesParaAGF jin = gson.fromJson(in.toString(), EntradaConsultarSolicitudesParaAGF.class);
		SalidaConsultarSolicitudesParaAGF out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaConsultarSolicitudesParaAGF logica(Connection con, EntradaConsultarSolicitudesParaAGF entrada) throws Exception{
		log.write("INICIO:: ConsultarSolicitudesParaAGF");
		SalidaConsultarSolicitudesParaAGF out = new SalidaConsultarSolicitudesParaAGF();
		
		SQL sql=new SQL();
		
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("IdEmpresa", entrada.p_IdEmpresa);
		out.P_OUT_RESULTADO = sql.query(con, SQL_SELECT_SOLICITUDES, params, OutSolicitudes.class);
		
		if(out.P_OUT_RESULTADO.size() > 0){
			CambiarEstadoSolicitudAgf cambiarEstado = new CambiarEstadoSolicitudAgf();
			EntradaCambiarEstadoSolicitudAgf entradaCambiarEstado = new EntradaCambiarEstadoSolicitudAgf();
			entradaCambiarEstado.p_IdEmpresa = entrada.p_IdEmpresa;
			for(OutSolicitudes o : out.P_OUT_RESULTADO){
				CambiarEstadoSolicitudAgf.InSolicitudes sol = new CambiarEstadoSolicitudAgf.InSolicitudes();
				sol.IdInterfazAgf = o.IdInterfazAgf;
				sol.IdEstado = _c.EstadoRES;
				sol.Observacion = "";
				entradaCambiarEstado.p_SolicitudesAGF.add(sol);
			}
			cambiarEstado.logica(con, entradaCambiarEstado);
		}
		
		log.write("FIN:: ConsultarSolicitudesParaAGF");
		return out;
	}
}
