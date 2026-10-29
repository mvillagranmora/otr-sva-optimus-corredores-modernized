package cl.bcs.fondomutuo;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.fondomutuo.pojo.ConstantesFondoMutuo;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CambiarEstadoSolicitudAgf implements JavaCall{
	
	private final Log log = new Log("FM.ProcesarEventoCapitalCorteCupon");
	
	private final ConstantesFondoMutuo _c = new ConstantesFondoMutuo();
	
	private final String SQL_SELECT_SOLICITUDES = loadResourceToString("cl/bcs/fondomutuo/sql/CambiarEstadoSolicitudAgf_SelectSolicitudes.sql");
	private final String SQL_UPDATE_SOLICITUDES = loadResourceToString("cl/bcs/fondomutuo/sql/CambiarEstadoSolicitudAgf_UpdateSolicitudes.sql");
	private final String SQL_UPDATE_SOLICITUDES_RELACIONADOS = loadResourceToString("cl/bcs/fondomutuo/sql/CambiarEstadoSolicitudAgf_UpdateSolicitudesRelacionados.sql");
	
	public static class EntradaCambiarEstadoSolicitudAgf{
		public Long p_IdEmpresa;
		public List<InSolicitudes> p_SolicitudesAGF;
		
		public EntradaCambiarEstadoSolicitudAgf(){
			this.p_SolicitudesAGF = new ArrayList<InSolicitudes>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class InSolicitudes{
		public Long IdInterfazAgf;
		public String IdEstado;
		public String Observacion;
		
		public InSolicitudes(){
			this.IdEstado = "PEN";
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SolicitudesProceso{
		public Long IdInterfazAgf;
		public Long IdSolicitudAporteRescateRelacionado;
		public String IdEstado;
		public String Observacion;
		public String IdEstadoNuevo;
		public Long IdEmpresa;
		public String EstadoProceso;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class OutSolicitudes{
		public Long IdInterfazAgf;
		public String Resultado;
		public String Codigo;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaCambiarEstadoSolicitudAgf{
		public Long P_OUT_ERRORESCONTADOR;
		public List <MensajeSalida> P_OUT_MENSAJES;
		public List <OutSolicitudes> P_OUT_RESULTADO;
		
		public SalidaCambiarEstadoSolicitudAgf(){
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_RESULTADO = new ArrayList<OutSolicitudes>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		EntradaCambiarEstadoSolicitudAgf jin = gson.fromJson(in.toString(), EntradaCambiarEstadoSolicitudAgf.class);
		SalidaCambiarEstadoSolicitudAgf out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaCambiarEstadoSolicitudAgf logica(Connection con, EntradaCambiarEstadoSolicitudAgf entrada) throws Exception{
		log.write("INICIO:: CambiarEstadoSolicitudAgf");
		SalidaCambiarEstadoSolicitudAgf out = new SalidaCambiarEstadoSolicitudAgf();
		
		SQL sql=new SQL();
		
		if(entrada.p_SolicitudesAGF.size() == 0){
			out.P_OUT_ERRORESCONTADOR = 1L;
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"NO_SE_INGRESARON_SOLICITUDES",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
			return out;
		}
		
		ArrayList<Long> correlativos = new ArrayList<Long>();
		for(InSolicitudes sol : entrada.p_SolicitudesAGF){
			if(correlativos.contains(sol.IdInterfazAgf)){
				out.P_OUT_ERRORESCONTADOR = 1L;
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"SOLICITUDES_REPETIDAS",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
				return out;
			}
			correlativos.add(sol.IdInterfazAgf);
		}
		
		HashMap<String,Object> params = new HashMap<String,Object>();
		
		params.put("IdInterfazAgf", correlativos);
		params.put("IdEmpresa", entrada.p_IdEmpresa);
		List<SolicitudesProceso> solicitudesProceso = sql.query(con, SQL_SELECT_SOLICITUDES, params, SolicitudesProceso.class);
		
		HashMap<Long,Object> hSolicitudes = Utils.List2Hash(solicitudesProceso, "IdInterfazAgf");
		
		List<SolicitudesProceso> solicitudesActualizar = new ArrayList<SolicitudesProceso>();
		List<SolicitudesProceso> solicitudesErrores = new ArrayList<SolicitudesProceso>();
		
		for(InSolicitudes inSol : entrada.p_SolicitudesAGF){
			SolicitudesProceso sol = (SolicitudesProceso) hSolicitudes.get(inSol.IdInterfazAgf);
			
			OutSolicitudes outSol = new OutSolicitudes();
			outSol.IdInterfazAgf = inSol.IdInterfazAgf;
			if(sol == null){
				out.P_OUT_ERRORESCONTADOR += 1L;
				out.P_OUT_MENSAJES.add(new MensajeSalida(inSol.IdInterfazAgf,MensajeSalida.MENSAJE_ERROR,"SOLICITUD_NO_EXISTE",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
				outSol.Codigo = "SOLICITUD_NO_EXISTE";
				outSol.Resultado = _c.NO;
				out.P_OUT_RESULTADO.add(outSol);
				continue;
			}
			
			if(!(_c.EstadoPendiente.equals(sol.IdEstado) && _c.EstadoRES.equals(inSol.IdEstado)) && !(_c.EstadoError.equals(sol.IdEstado) && _c.EstadoRES.equals(inSol.IdEstado)) 
			   && !(_c.EstadoRES.equals(sol.IdEstado) && (_c.EstadoProcesado.equals(inSol.IdEstado) ||  _c.EstadoError.equals(inSol.IdEstado)))){
				out.P_OUT_ERRORESCONTADOR += 1L;
				out.P_OUT_MENSAJES.add(new MensajeSalida(inSol.IdInterfazAgf,MensajeSalida.MENSAJE_ERROR,"CAMBIO_ESTADO_INVALIDO",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
				sol.EstadoProceso = _c.EstadoRechazado;
				outSol.Codigo = "CAMBIO_ESTADO_INVALIDO";
				outSol.Resultado = _c.NO;
				out.P_OUT_RESULTADO.add(outSol);
				continue;
			}
			outSol.Codigo = "CAMBIO_ESTADO_OK";
			outSol.Resultado = _c.SI;
			out.P_OUT_RESULTADO.add(outSol);
			sol.Observacion = inSol.Observacion;
			sol.IdEstadoNuevo = inSol.IdEstado;
			if(_c.EstadoError.equals(inSol.IdEstado)){
				solicitudesErrores.add(sol);
			}
			
			solicitudesActualizar.add(sol);
		}
		
		sql.batch(con, SQL_UPDATE_SOLICITUDES, solicitudesActualizar, SolicitudesProceso.class);
		sql.batch(con, SQL_UPDATE_SOLICITUDES_RELACIONADOS, solicitudesErrores, SolicitudesProceso.class);

		log.write("FIN:: CambiarEstadoSolicitudAgf");
		return out;
	}
}
