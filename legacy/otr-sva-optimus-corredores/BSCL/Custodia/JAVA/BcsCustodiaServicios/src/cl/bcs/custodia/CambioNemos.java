package cl.bcs.custodia;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CambioNemos implements JavaCall{
	
	private final Log log =new Log("CUS.CambioNemos");
	
	private final String SQL_SELECT_INSTRUMENTOS_CAMBIO_NEMO = loadResourceToString("cl/bcs/custodia/sql/CambioNemos_SelectInstrumentos.sql");
	private final String SQL_UPDATE_CARTERA = loadResourceToString("cl/bcs/custodia/sql/CambioNemos_UpdateCartera.sql");
	private final String SQL_UPDATE_COMPROBANTE_DETALLE = loadResourceToString("cl/bcs/custodia/sql/CambioNemos_UpdateComprobanteDetalle.sql");
	private final String SQL_UPDATE_GARANTIA = loadResourceToString("cl/bcs/custodia/sql/CambioNemos_UpdateGarantia.sql");
	private final String SQL_UPDATE_MOVIMIENTO_COMPROBANTE = loadResourceToString("cl/bcs/custodia/sql/CambioNemos_UpdateMovimientoComprobante.sql");
	private final String SQL_UPDATE_MOVIMIENTO_COMPROBANTE_DETALLE = loadResourceToString("cl/bcs/custodia/sql/CambioNemos_UpdateMovimientoComprobanteDetalle.sql");
	private final String SQL_UPDATE_SUBYACENTE = loadResourceToString("cl/bcs/custodia/sql/CambioNemos_UpdateSubyacente.sql");
	
	public static class Instrumentos{
		public Long IdInstrumentoAnterior = 0L;
		public Long IdInstrumentoNuevo = 0L;
		public String NemoNuevo = "";
		public String Emisor = "";
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class UpdateInstrumentos{
		public Long p_IdInstrumento;
		public Long p_IdInstrumentoNuevo;
		public String p_NemoNuevo;
		public String p_Emisor;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
		
	public static class UpdateInstrumentosSinEmisor{
		public Long p_IdInstrumento;
		public Long p_IdInstrumentoNuevo;
		public String p_NemoNuevo;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	/*******************
	 * clases in/out
	 *******************/
	
	public static class InCambioNemos{
		public Long 		p_IdEmpresa;
		public Long 		p_IdUsuario;
		public String 		p_IdModulo;
	}
	
	public static class OutCambioNemos{
		public Long 	P_OUT_ERRORESCONTADOR;
	}
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = new Gson();	
		InCambioNemos in=gson.fromJson(jin.toString() ,InCambioNemos.class);
		OutCambioNemos out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		
		return dataout;
	}
	
	/**********************
	 * lógica del programa
	 **********************/
	public OutCambioNemos logica(Connection con, InCambioNemos entrada) throws Exception {
		OutCambioNemos out = new OutCambioNemos();
		
		out.P_OUT_ERRORESCONTADOR = 0L;
		
		SQL sql=new SQL();
        String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
        log.write("Fecha de sistema: " + fechaSistema);
		HashMap<String,Object> params = new HashMap<>();
		params.put("FechaSistema", fechaSistema);
		List<Instrumentos> instrumentos = sql.query(con, SQL_SELECT_INSTRUMENTOS_CAMBIO_NEMO, params, Instrumentos.class);
		UpdateInstrumentos updateInstrumento = new UpdateInstrumentos();
		List<UpdateInstrumentos> updateInstrumentos = new ArrayList<UpdateInstrumentos>();
		UpdateInstrumentosSinEmisor updateInstrumentoSinEmisor = new UpdateInstrumentosSinEmisor();
		List<UpdateInstrumentosSinEmisor> updateInstrumentosSinEmisor = new ArrayList<UpdateInstrumentosSinEmisor>();
		//Si hay instrumentos que tengan que cambiar nemo
		if (instrumentos.size() > 0){
			for (Instrumentos ins : instrumentos){
				updateInstrumento.p_IdInstrumento = ins.IdInstrumentoAnterior;
				updateInstrumento.p_IdInstrumentoNuevo = ins.IdInstrumentoNuevo;
				updateInstrumento.p_NemoNuevo = ins.NemoNuevo;
				updateInstrumento.p_Emisor = ins.Emisor;
				updateInstrumentoSinEmisor.p_IdInstrumento = ins.IdInstrumentoAnterior;
				updateInstrumentoSinEmisor.p_IdInstrumentoNuevo = ins.IdInstrumentoNuevo;
				updateInstrumentoSinEmisor.p_NemoNuevo = ins.NemoNuevo;
				updateInstrumentos.add(updateInstrumento);
				updateInstrumentosSinEmisor.add(updateInstrumentoSinEmisor);
			}
			sql.batch(con, SQL_UPDATE_CARTERA, updateInstrumentos, UpdateInstrumentos.class);
			sql.batch(con, SQL_UPDATE_SUBYACENTE, updateInstrumentos, UpdateInstrumentos.class);
			sql.batch(con, SQL_UPDATE_MOVIMIENTO_COMPROBANTE, updateInstrumentos, UpdateInstrumentos.class);
			sql.batch(con, SQL_UPDATE_MOVIMIENTO_COMPROBANTE_DETALLE, updateInstrumentosSinEmisor, UpdateInstrumentosSinEmisor.class);
			sql.batch(con, SQL_UPDATE_COMPROBANTE_DETALLE, updateInstrumentosSinEmisor, UpdateInstrumentosSinEmisor.class);
			sql.batch(con, SQL_UPDATE_GARANTIA, updateInstrumentos, UpdateInstrumentos.class);
		} else {
			log.write("No existen instrumentos con nemo a cambiar para fecha de sistema: " + fechaSistema);
		}
		
		log.writeAsJson("SALIDA",out);
		return out;
		
	}
	public static void main(String[] args) {
		
		try {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"CUS\"}";
			InCambioNemos Objin = gson.fromJson(json, InCambioNemos.class);
			Connection con = PlataformaUtils.getDESAPEConnection();		
			
			CambioNemos cn = new CambioNemos();
			OutCambioNemos resp = cn.logica(con, Objin);
			con.rollback();
		} catch (Exception e) {
			e.printStackTrace();
		}

		}
	
}
