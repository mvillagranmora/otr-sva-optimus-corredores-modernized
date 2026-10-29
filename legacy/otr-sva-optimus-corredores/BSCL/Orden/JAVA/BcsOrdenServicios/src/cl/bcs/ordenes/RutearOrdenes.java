package cl.bcs.ordenes;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;

public class RutearOrdenes implements JavaCall {

	private static final Logger logger = LogManager.getLogger(RutearOrdenes.class);

	private final String SQL_INSERT_ENTRADA = loadResourceToString("cl/bcs/ordenes/sql/RutearOrdenes_InsertEntrada.sql");
	
	public static class RutearOrdenes_Entrada {
		public Long			p_IdEmpresa;
		public List<Orden>	p_Ordenes;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Orden {
		public Long IdOrden;
		public String IdBolsaDistribucion;
		
	}
	
	public static class RutearOrdenes_Salida {

		public List<SalidaDetalle>	P_OUT_SALIDADETALLE;
		public List<SalidaResumen>	P_OUT_SALIDARESUMEN;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

		
	public static class SalidaDetalle {
		public Long Id;
		public Long IdOperacion;
		public String Resultado;
		public String CodigoMensaje;
		public String CodigoDato1;
		public String ValorDato1;
		public String CodigoDato2;
		public String ValorDato2;
	}

	
	public static class SalidaResumen {
		public Long IdInterno;
		public Long IdOperacion;
		public String Resultado;
		public Long OK;
		public Long ADVERTENCIA;
		public Long AUTORIZACION;
		public Long RECHAZO;
	}
		
	/*
	 * Logica Servicio
	 * 
	 */
	@Trace
	public RutearOrdenes_Salida logica(Connection con, RutearOrdenes_Entrada in) throws Exception {
		SQL sql = new SQL();
		RutearOrdenes_Salida out = new RutearOrdenes_Salida();
		
		sql.batch(con,SQL_INSERT_ENTRADA,in.p_Ordenes, Orden.class);

		HashMap<String, Object> parmsIN = new HashMap<String, Object>();

		parmsIN.put("p_IdEmpresa", in.p_IdEmpresa);
	
		HashMap<String, Class> parmsOUT = new HashMap<String, Class>();
		parmsOUT.put("p_out_SalidaDetalle", SalidaDetalle.class);
		parmsOUT.put("p_out_SalidaResumen", SalidaResumen.class);
			
		String queryCall = "call \"BSCL.Orden.BD.Servicios::MIG.RutearOrdenes\" (p_IdEmpresa => :p_IdEmpresa,p_out_SalidaDetalle => $p_out_SalidaDetalle, p_out_SalidaResumen => $p_out_SalidaResumen)";
		HashMap<String, Object> rpt = sql.call(con, queryCall, parmsIN, parmsOUT);

		out.P_OUT_SALIDARESUMEN = (List<SalidaResumen>) rpt.get("P_OUT_SALIDARESUMEN");
		out.P_OUT_SALIDADETALLE = (List<SalidaDetalle>) rpt.get("P_OUT_SALIDADETALLE");

		return out;

	}

	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		Long idEmpresa = 1L;
		RutearOrdenes r = new RutearOrdenes();
		
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			
			
			RutearOrdenes_Entrada intr = new RutearOrdenes_Entrada();
			intr.p_IdEmpresa = 1L;
			List<Orden> list = new ArrayList<>();
			Orden ord = new Orden();
			ord.IdBolsaDistribucion = "BCS";
			ord.IdOrden = 329106L;
			list.add(ord );
			intr.p_Ordenes = list ;
					
			
			RutearOrdenes_Salida salida = r.logica(con, intr );
			System.out.println(salida);
			con.commit();
		} catch (Exception e) {
			e.printStackTrace();			
		}	
		System.out.println("== END ==");
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}