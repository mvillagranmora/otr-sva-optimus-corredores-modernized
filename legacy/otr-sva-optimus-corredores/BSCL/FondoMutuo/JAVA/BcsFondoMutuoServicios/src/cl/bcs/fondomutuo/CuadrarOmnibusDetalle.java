package cl.bcs.fondomutuo;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.math.BigDecimal;
import java.math.MathContext;

import cl.bcs.fondomutuo.pojo.OmnibusDetalle;
import cl.bcs.fondomutuo.pojo.ValorCuota;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CuadrarOmnibusDetalle implements JavaCall{
	private final Log log = new Log("FMM.CuadrarOmnibusDetalle");
	
	private final String SQL_SELECT_VALOR_CUOTA = loadResourceToString("cl/bcs/fondomutuo/sql/CuadrarOmnibusDetalle_SelectValorCuota.sql");
	private final String SQL_SELECT_OMNIBUS_DETALLE = loadResourceToString("cl/bcs/fondomutuo/sql/CuadrarOmnibusDetalle_SelectOmnibusDetalle.sql");
	private final String SQL_UPDATE_OMNIBUS_DETALLE = loadResourceToString("cl/bcs/fondomutuo/sql/CuadrarOmnibusDetalle_UpdateOmnibusDetalle.sql");
	
	private static final String ESTADO_AUTORIZADO = "AUT";
	private static final String ESTADO_NO_AUTORIZADO = "RZD";
	
	public static class EntradaCuadrarOmnibusDetalle{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public List<Long> p_OmnibusDetalle;
		
		public EntradaCuadrarOmnibusDetalle(){
			this.p_OmnibusDetalle = new ArrayList<Long>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaCuadrarOmnibusDetalle{
		public Long P_OUT_ERRORESCONTADOR;
		public List <MensajeSalida> P_OUT_ERRORESDETALLE;
		
		public SalidaCuadrarOmnibusDetalle(){
			this.P_OUT_ERRORESCONTADOR=0L;
			this.P_OUT_ERRORESDETALLE = new ArrayList<MensajeSalida>(); 
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = new Gson();		
		EntradaCuadrarOmnibusDetalle in=gson.fromJson(jin.toString() ,EntradaCuadrarOmnibusDetalle.class);
		SalidaCuadrarOmnibusDetalle out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaCuadrarOmnibusDetalle logica(Connection conn, EntradaCuadrarOmnibusDetalle in) throws Exception {
		log.write("INICIO::EntradaCuadrarOmnibusDetalle");
		SalidaCuadrarOmnibusDetalle out = new SalidaCuadrarOmnibusDetalle();
		SQL sql = new SQL();
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("IdOmnibusDetalle", in.p_OmnibusDetalle);
		List<OmnibusDetalle> omnibusDetalles = sql.query(conn, SQL_SELECT_OMNIBUS_DETALLE, params, OmnibusDetalle.class);
		List<String> idseries = new ArrayList<String>();
		for(OmnibusDetalle om : omnibusDetalles){
			if(!idseries.contains(om.IdSerieFondo)){
				idseries.add(om.IdSerieFondo);
			}
		}
		params.clear();
		params.put("IdEmpresa",in.p_IdEmpresa);
		params.put("IdSerieFondo", idseries);
		List<ValorCuota> vc = sql.query(conn, SQL_SELECT_VALOR_CUOTA, params, ValorCuota.class);
		
		BigDecimal porcentajeMaximo = ConfiguracionSistema.getParametroConfiguracionDecimal(conn, in.p_IdEmpresa, "FONDOMUTUO", "PORCENTAJE_VARIACION_FFMM");
		for(OmnibusDetalle om : omnibusDetalles){
			ValorCuota vcA = new ValorCuota();
			vcA.ValorCuota = BigDecimal.ZERO;
			ValorCuota vcH= new ValorCuota();
			vcH.ValorCuota = BigDecimal.ZERO;
			for(ValorCuota vct : vc){
				if(vct.IdSerieFondo.equalsIgnoreCase(om.IdSerieFondo)){
					vcA = vct;
					break;
				}
			}
			if(vcA.FechaIngresoRegistro != null){
				for(ValorCuota vct : vc){
					if(vct.IdSerieFondo.equalsIgnoreCase(om.IdSerieFondo) && (vct.FechaIngresoRegistro.compareTo(vcA.FechaIngresoRegistro) != 0)){
						vcH = vct;
						break;
					}
				}
			}
			om.ValorCuota = vcA.ValorCuota;
			om.UltimoValorCuota = vcH.ValorCuota;
			om.DiferenciaValorCuota = om.ValorCuota.subtract(om.UltimoValorCuota, MathContext.DECIMAL128);
			om.DiferenciaValorCuotaAGF = om.ValorCuota.subtract(om.ValorCuotaAGF, MathContext.DECIMAL128);
			if(om.UltimoValorCuota.compareTo(BigDecimal.ZERO) != 0){
				BigDecimal porcentaje = om.DiferenciaValorCuota.abs().divide(om.UltimoValorCuota, MathContext.DECIMAL128).multiply(new BigDecimal(100), MathContext.DECIMAL128);
				if(porcentaje.compareTo(porcentajeMaximo) == 1){
					om.IdEstado = ESTADO_NO_AUTORIZADO;
				}else{
					om.IdEstado = ESTADO_AUTORIZADO;
				}
			}else{
				om.IdEstado = ESTADO_AUTORIZADO;
			}
		}
		
		sql.batch(conn, SQL_UPDATE_OMNIBUS_DETALLE, omnibusDetalles, OmnibusDetalle.class);
		
		log.write("FIN::EntradaCuadrarOmnibusDetalle");
		return out;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getUATConnection()) {
		
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();			
			String jsonRF = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":25,\"p_OmnibusDetalle\":[43]}";
					
			EntradaCuadrarOmnibusDetalle Objin = gson.fromJson(jsonRF, EntradaCuadrarOmnibusDetalle.class);
			
			CuadrarOmnibusDetalle  demo=new CuadrarOmnibusDetalle();				
 			
 			try {
 				SalidaCuadrarOmnibusDetalle ObjOut=demo.logica(con, Objin);
 				//con.commit();
 				System.out.println(ObjOut.toString());
 			}
 			catch(Exception er) {
				er.printStackTrace();	
				con.rollback();
			}
 			con.rollback(); 			
	    	}
		System.out.println("== END ==");
	}
	
}
