package cl.bcs.contabilidad;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CentralizaGeneraCabecera implements JavaCall {

	private final Logger log = LogManager.getLogger(CentralizaGeneraCabecera.class);
	
	private final String SQL_SELECT_TIPO_CENTRALIZACION = loadResourceToString("cl/bcs/contabilidad/sql/CentralizaGenerico_SelectTipoCentralizacion.sql");
	private final String SQL_INSERT_CENTRALIZACION = loadResourceToString("cl/bcs/contabilidad/sql/CentralizaGenerico_InsertCentralizacion.sql");
	
	public static class SalidaCentralizaGeneraCabecera extends BcsPojo {
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaCentralizaGeneraCabecera() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
	}
	public static class EntradaCentralizaGeneraCabecera extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_Ejercicio;
		public String p_FechaContable;
		public Long p_CodigoUsuario;
		public List<TipoCentralizacion>	p_TipoCentralizacion;
		public EntradaCentralizaGeneraCabecera() {
			this.p_TipoCentralizacion = new ArrayList<TipoCentralizacion>();
		}
	}
	public static class TipoCentralizacion extends BcsPojo {
		public Long IdTipoCentralizacion;
		public String Nombre;
		public String TipoEnvio;
	}
	public static class CentralizacionNuevo extends BcsPojo {
		public Long IdCentralizacion;  
		public Long IdTipoCentralizacion;
		public Long IdEmpresa;
		public String FechaProceso;
		public String HoraProceso;
		public String FechaContable;
		public String Glosa;
		public String CodigoUsuario;
		public String TipoGeneracion;
		public String Estado;
	}
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = getGsonBuilder();
		EntradaCentralizaGeneraCabecera in = gson.fromJson(jin.toString(), EntradaCentralizaGeneraCabecera.class);
		SalidaCentralizaGeneraCabecera out = logica(con, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaCentralizaGeneraCabecera logica(Connection con, EntradaCentralizaGeneraCabecera entrada) throws Exception {
		log.info("INICIO::CentralizaGeneraCabecera");
		SalidaCentralizaGeneraCabecera salida 	= new SalidaCentralizaGeneraCabecera();
		HashMap<String,Object> params 			= new HashMap<String,Object>();
		SQL sql = new SQL();
		int i 	= 0;
		
		if (entrada.p_TipoCentralizacion.stream().count() == 0) {
			return salida;
		}
		
		params.put("IdTipoCentralizacion", entrada.p_TipoCentralizacion.stream().map(c -> c.IdTipoCentralizacion).collect(Collectors.toList()));
		List<TipoCentralizacion> tiposCentralizacion = sql.query(con, SQL_SELECT_TIPO_CENTRALIZACION, params, TipoCentralizacion.class);
		
		String fechaProceso = Fecha.toFormat(FechaDB.ObtenerFechaHoraTs(con, entrada.p_IdEmpresa),"yyyy-MM-dd");
		String horaProceso = FechaDB.ObtenerHoraUTC(con);
		HashMap<Long,Object> hTiposCentralizacion = Utils.List2Hash(tiposCentralizacion, "IdTipoCentralizacion");
		List<Long> secuenciasCentralizaciones = Utils.getSecuencia(con, "BSCL.Contabilidad.BD.Secuencias::IdCentralizacion", entrada.p_TipoCentralizacion.stream().count());
		List<CentralizacionNuevo> centralizacionesNuevo = new ArrayList<CentralizacionNuevo>();
		
		for (TipoCentralizacion centralizacionItem : entrada.p_TipoCentralizacion) {
			
			CentralizacionNuevo centralizacionNuevo = new CentralizacionNuevo();
			TipoCentralizacion tipoCentralizacion = (TipoCentralizacion) hTiposCentralizacion.get(centralizacionItem.IdTipoCentralizacion);
			Long idCentralizacion = 0L;
			idCentralizacion = secuenciasCentralizaciones.get(i);
			
			centralizacionNuevo.IdCentralizacion = idCentralizacion;
			centralizacionNuevo.IdTipoCentralizacion = centralizacionItem.IdTipoCentralizacion;
			centralizacionNuevo.IdEmpresa = entrada.p_IdEmpresa;
			centralizacionNuevo.FechaProceso = fechaProceso;
			centralizacionNuevo.HoraProceso = horaProceso;
			centralizacionNuevo.FechaContable = entrada.p_FechaContable;
			centralizacionNuevo.Glosa = "";
			centralizacionNuevo.CodigoUsuario = entrada.p_CodigoUsuario != null ? entrada.p_CodigoUsuario.toString() : "";
			centralizacionNuevo.TipoGeneracion = tipoCentralizacion.TipoEnvio;
			centralizacionNuevo.Estado = "E";
			centralizacionesNuevo.add(centralizacionNuevo);
			HashMap<String,String> valores = new HashMap<String,String>();
			valores.put("Centralizacion", tipoCentralizacion.Nombre);
			valores.put("IdCentralizacion", String.valueOf(idCentralizacion));
			salida.P_OUT_MENSAJES.add(new MensajeSalida(centralizacionItem.IdTipoCentralizacion, MensajeSalida.MENSAJE_ERROR, "CENTRALIZACION_GENERADA_CON_ERROR", valores, con, entrada.p_IdEmpresa));
			i++;
		}
		log.debug("INSERT CENTRALIZACIONES CABECERA");
		sql.batch(con, SQL_INSERT_CENTRALIZACION, centralizacionesNuevo, CentralizacionNuevo.class);
		
		log.info("FIN::CentralizaGeneraCabecera");
		return salida;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST::START ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String jsonUi = "{\"p_IdEmpresa\":1,\"p_Ejercicio\":\"2024\",\"p_FechaContable\":\"2024-02-20\",\"p_CodigoUsuario\":97,\"p_TipoCentralizacion\":[{\"IdTipoCentralizacion\":331}]}";
			EntradaCentralizaGeneraCabecera Objin = gson.fromJson(jsonUi, EntradaCentralizaGeneraCabecera.class);
			CentralizaGeneraCabecera demo = new CentralizaGeneraCabecera();
			try {
				long init = System.currentTimeMillis();
				SalidaCentralizaGeneraCabecera ObjOut = demo.logica(con, Objin);
				demo.log.debug(String.format("Termino CentralizacionMultiSeleccion en %d millis", System.currentTimeMillis() - init));
				con.commit();
				System.out.println(ObjOut.toString());
			}
			catch (Exception e) {
				e.printStackTrace();	
				con.rollback();
			}
		}
		catch (Exception ce) {
			ce.printStackTrace();
		}
		System.out.println("== TEST::END ==");
	}
}
