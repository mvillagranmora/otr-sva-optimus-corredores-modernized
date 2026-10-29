package cl.bcs.mesa.MRF;

import java.math.BigDecimal;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.mesa.MRF.pojo.CacheMRF;
import cl.bcs.plataforma.pla.AgregarMensajeWebsocket;
import cl.bcs.plataforma.pla.utils.CacheServicio;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class GrabarItemsManualesPosicionMRF implements JavaCall {	
	
	private static final Logger logger = LogManager.getLogger(ConsultarPosicionMRF.class);
	
	private final String SQL_GRABARITEMMANUAL_POSICIONMRF  = loadResourceToString("cl/bcs/mesa/MRF/sql/GrabarItemsManualesPosicionMRFCall.sql");
	private final String SQL_CREATE_TABLE_ITEMMANUAL_POSICIONMRF = loadResourceToString("cl/bcs/mesa/MRF/sql/GrabarItemsManualesPosicionMRF_CreateTable.sql");
	private final String SQL_INSERT_TABLE_ITEMMANUAL_POSICIONMRF = loadResourceToString("cl/bcs/mesa/MRF/sql/GrabarItemsManualesPosicionMRF_InsertTable.sql");
	private final String SQL_DROP_TABLE_ITEMMANUAL_POSICIONMRF = loadResourceToString("cl/bcs/mesa/MRF/sql/GrabarItemsManualesPosicionMRF_DropTable.sql");
	
	private SQL _sql = new SQL();
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = getGsonBuilder();
		GrabarItemsManualesPosicionMRF_Entrada jin = gson.fromJson(in.toString(), GrabarItemsManualesPosicionMRF_Entrada.class);
		GrabarItemsManualesPosicionMRF_Salida out = logica(conn, jin);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public static class ErrorDetalleCall {
		public String Codigo;
		public String Descripcion;
	}

	public static class AdvertenciaDetalleCall {
		public String Codigo;
		public String RequiereAutorizacion;
		public String Descripcion;
	}

	public static class GrabarItemsManualesPosicionMRF_Entrada {
		public Long p_IdEmpresa;
		public Long	p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public String p_Fecha;
		public ArrayList<ItemManuales> p_ItemsManuales;
		
		public GrabarItemsManualesPosicionMRF_Entrada(){
			p_IdEmpresa = 0L;
			p_IdUsuario = 0L;
			p_IdModulo = "";
			p_UsaReglasNegocio = "";
			p_Fecha = "";
			p_ItemsManuales = new ArrayList<ItemManuales>();
					
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ItemManuales{
		public Long IdPosicion;
		public Long IdMetodo;
		public Long IdEvento;
		public BigDecimal MontoEvento;
	}
	
	public static class GrabarItemsManualesPosicionMRF_Salida {
		public List<ErrorDetalleCall> P_OUT_ERRORESDETALLE;
		public List<AdvertenciaDetalleCall> P_OUT_ADVERTENCIASDETALLE;
		public Long P_OUT_ERRORESCONTADOR;
		public Long P_OUT_ADVERTENCIASCONTADOR;
		public Long P_OUT_REFID;

		public GrabarItemsManualesPosicionMRF_Salida() {
			this.P_OUT_ERRORESDETALLE = new ArrayList<ErrorDetalleCall>();
			this.P_OUT_ADVERTENCIASDETALLE = new ArrayList<AdvertenciaDetalleCall>();
		}

		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	public GrabarItemsManualesPosicionMRF_Salida logica(Connection conn, GrabarItemsManualesPosicionMRF_Entrada entrada) throws Exception {
		logger.info("INICIO::MRF.GrabarItemsManualesPosicionMRF");
		
				
		
		GrabarItemsManualesPosicionMRF_Salida salida = new GrabarItemsManualesPosicionMRF_Salida();
		logger.debug("Inicio SELECT_POSICION_MRF");
		try {
			SQL.exec(conn, SQL_CREATE_TABLE_ITEMMANUAL_POSICIONMRF);
			
			_sql.batch(conn,SQL_INSERT_TABLE_ITEMMANUAL_POSICIONMRF,entrada.p_ItemsManuales,ItemManuales.class);
			
			HashMap<String,Object> paramsIn = new HashMap<>();
			paramsIn.put("IdEmpresa", entrada.p_IdEmpresa);
			paramsIn.put("IdUsuario", entrada.p_IdUsuario);
			paramsIn.put("IdModulo", entrada.p_IdModulo);
			paramsIn.put("UsaReglasNegocio", entrada.p_UsaReglasNegocio);
			Date fechaDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").parse(entrada.p_Fecha);
			SimpleDateFormat output = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			paramsIn.put("Fecha", Fecha.toDate(output.format(fechaDate)));
			//paramsIn.put("p_ItemsManuales", entrada.p_ItemsManuales);
			
			HashMap<String, Class> paramsOut = new HashMap<String, Class>();
			paramsOut.put("p_out_RefId", Long.class);
			paramsOut.put("p_out_AdvertenciasContador", Long.class);
			paramsOut.put("p_out_AdvertenciasDetalle", AdvertenciaDetalleCall.class);
			paramsOut.put("p_out_ErroresContador", Long.class);
			paramsOut.put("p_out_ErroresDetalle", ErrorDetalleCall.class);
			
			HashMap<String, Object> resultSP = _sql.call(conn, SQL_GRABARITEMMANUAL_POSICIONMRF, paramsIn, paramsOut);
			logger.debug("Fin SELECT_POSICION_MRF");
			
			salida.P_OUT_REFID = (Long)resultSP.get("p_out_RefId");
			salida.P_OUT_ERRORESCONTADOR = (Long)resultSP.get("p_out_ErroresContador");
			salida.P_OUT_ERRORESDETALLE = (List<ErrorDetalleCall>) resultSP.get("p_out_ErroresDetalle");
			salida.P_OUT_ADVERTENCIASCONTADOR = (Long)resultSP.get("p_out_AdvertenciasContador");
			salida.P_OUT_ADVERTENCIASDETALLE = (List<AdvertenciaDetalleCall>) resultSP.get("p_out_AdvertenciasDetalle");
			
			CacheServicio.invalidarPorTag(CacheMRF.TAG_POSICION);
			AgregarMensajeWebsocket.agregar(conn, entrada.p_IdEmpresa, "ACTPOSMRF", "Refresca Posicion Mesa RF/IF");
			
			logger.info("FIN::MRF.GrabarItemsManualesPosicionMRF");
			return salida;
			
		}catch(Exception er) {
			logger.error("ERROR", er);
			throw er;
		}
		finally 
		{
			try 
			{
				SQL.exec(conn, SQL_DROP_TABLE_ITEMMANUAL_POSICIONMRF);		
			} 
			catch (Exception er) 
			{
				logger.error("ERROR", er);	
			}
	
		}

	}
	
	
	
	public static void main(String[] args) throws Exception  {
		System.out.println("== TEST ==");
		//try (Connection con = PlataformaUtils.getMASTER2Connection()) {
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":102,\"p_IdModulo\":\"MES\",\"p_UsaReglasNegocio\":\"N\",\"p_Fecha\":\"2022-01-26T03:00:00.000Z\",\"p_ItemsManuales\":[{\"IdPosicion\":0,\"IdMetodo\":5,\"IdEvento\":11,\"MontoEvento\":12000}]}";
			//
			GrabarItemsManualesPosicionMRF_Entrada Objin = gson.fromJson(json, GrabarItemsManualesPosicionMRF_Entrada.class);
			GrabarItemsManualesPosicionMRF demo = new GrabarItemsManualesPosicionMRF();
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			System.out.println("SALIDA");
 			try {
 				GrabarItemsManualesPosicionMRF_Salida ObjOut = demo.logica(con, Objin);
 				//con.rollback();
 				con.commit();
 				System.out.println(ObjOut.toString());
 			}
 			catch(Exception er) {
				er.printStackTrace();	
 				System.out.println(er.toString());
				con.rollback();
			}
 			//con.commit();
 			
	    	}
		 	
		System.out.println("== END ==");
	}
	
}
