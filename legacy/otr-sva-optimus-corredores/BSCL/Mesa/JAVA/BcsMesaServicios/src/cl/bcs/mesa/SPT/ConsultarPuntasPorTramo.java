package cl.bcs.mesa.SPT;

import java.math.BigDecimal;
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

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConsultarPuntasPorTramo implements JavaCall{
	
	private static final Logger logger = LogManager.getLogger(ConsultarPuntasPorTramo.class);
	
	private final String SQL_SELECT_PUNTAS = loadResourceToString("cl/bcs/mesa/SPT/sql/ConsultarPuntasPorTramo_SelectPuntas.sql");
	private final String SQL_SELECT_PUNTAS_POR_TRAMO = loadResourceToString("cl/bcs/mesa/SPT/sql/ConsultarPuntasPorTramo_SelectPuntasPorTramo.sql");
	
	
	public static class ConsultarPuntasPorTramoEntrada {
		public Long p_IdEmpresa;
		public String p_IdMoneda;
		public Long p_MontoPorTramo = 0L;
		public String p_Canal = "Punta";
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ConsultarPuntasPorTramoSalida {
		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;
		public List<PuntaPorTramoSalida> P_OUT_PUNTAS;
		
		public ConsultarPuntasPorTramoSalida() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_PUNTAS = new ArrayList<PuntaPorTramoSalida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Punta {
		public Long IdPuntas;
		public Long IdEmpresa;
		public java.sql.Timestamp Fecha;
		public String IdUsuario;
		public BigDecimal AperturaPrecio;
		public BigDecimal PuntaCompra;
		public BigDecimal PuntaVenta;
		public String IdMonedaPunta;
		public String CheckPrecioPorTramo;
		public String Estado;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class PuntaPorTramo {
		public Long IdPuntasPorTramo;
		public Long IdEmpresa;
		public Long IdPuntas;
		public BigDecimal MontoPorTramo;
		public BigDecimal PorcentajeTramo;
		public BigDecimal MontoCentavos;
		public BigDecimal ValorCompra;
		public BigDecimal ValorVenta;
		public String Estado;
		public String IdUsuario;
		public java.sql.Timestamp FechaModificacion;
		public String Canal;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class PuntaPorTramoSalida {
		public BigDecimal MontoPorTramo;
		public BigDecimal PuntaCompra;
		public BigDecimal PuntaVenta;
		public BigDecimal PuntaCompraDefault;
		public BigDecimal PuntaVentaDefault;
		public String CheckPrecioPorTramo;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		ConsultarPuntasPorTramoEntrada in = gson.fromJson(jin.toString(), ConsultarPuntasPorTramoEntrada.class);
		ConsultarPuntasPorTramoSalida out = logica(con, in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public ConsultarPuntasPorTramoSalida logica(Connection conn, ConsultarPuntasPorTramoEntrada in) throws Exception {
		Long init = System.currentTimeMillis();
		logger.info("Inicio servicio ConsultarPuntasPorTramo");
		
		SQL sql = new SQL();
		ConsultarPuntasPorTramoSalida out = new ConsultarPuntasPorTramoSalida();
		out.P_OUT_ERRORESCONTADOR = 0L;
		
		try {
			HashMap<String, Object> params = new HashMap<String, Object>();
			params.put("IdMoneda", in.p_IdMoneda);
			params.put("IdEmpresa", in.p_IdEmpresa);
			logger.debug("INICIO::SQL_SELECT_PUNTAS");
			List<Punta> puntaList = sql.query(conn, SQL_SELECT_PUNTAS, params, Punta.class);
			logger.debug("FIN::SQL_SELECT_PUNTAS");
			//VALIDAR SI MONEDA EXISTE
			if (puntaList.size() == 0) {
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ADVERTENCIA,"PUNTA","Moneda no registra Puntas configuradas."));
				return out;
			}
			
			//VALIDAR SI MONEDA POSEE TRAMOS
			if ("NO".equals(puntaList.get(0).CheckPrecioPorTramo)) {
				for (Punta punta : puntaList) {
					PuntaPorTramoSalida puntaPorTramoSalida = new PuntaPorTramoSalida();
					puntaPorTramoSalida.MontoPorTramo = BigDecimal.ZERO;
					puntaPorTramoSalida.PuntaCompra = punta.PuntaCompra;
					puntaPorTramoSalida.PuntaVenta = punta.PuntaVenta;
					puntaPorTramoSalida.PuntaCompraDefault = punta.PuntaCompra;
					puntaPorTramoSalida.PuntaVentaDefault = punta.PuntaVenta;
					puntaPorTramoSalida.CheckPrecioPorTramo = punta.CheckPrecioPorTramo;
					out.P_OUT_PUNTAS.add(puntaPorTramoSalida);
				}
			}
			else if ("SI".equals(puntaList.get(0).CheckPrecioPorTramo)) {
				params.put("Canal", in.p_Canal);
				logger.debug("INICIO::SQL_SELECT_PUNTAS_POR_TRAMO");
				List<PuntaPorTramoSalida> puntaPorTramoSalidaList = sql.query(conn, SQL_SELECT_PUNTAS_POR_TRAMO, params, PuntaPorTramoSalida.class);
				logger.debug("FIN::SQL_SELECT_PUNTAS_POR_TRAMO");
				//VALIDAR SI EXISTE TRAMOS
				if (puntaPorTramoSalidaList.stream().filter(p -> "SI".equals(p.CheckPrecioPorTramo)).collect(Collectors.toList()).size() == 0) {
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ADVERTENCIA,"TRAMO","Moneda no registra Puntas por Tramo configuradas."));
					return out;
				}
				
				out.P_OUT_PUNTAS = new ArrayList<PuntaPorTramoSalida>(puntaPorTramoSalidaList);
			}
		}
		catch(Exception e){
			out.P_OUT_ERRORESCONTADOR = 1L;
			logger.error(e);
			throw e;
		}
		
		logger.info("Termino del servicio ConsultarPuntasPorTramo, tiempo:" + (System.currentTimeMillis() - init));
		return out;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String json = "{\"p_IdEmpresa\":1,\"p_IdMoneda\":\"USD\",\"p_MontoPorTramo\":0}";
			ConsultarPuntasPorTramoEntrada Objin = gson.fromJson(json, ConsultarPuntasPorTramoEntrada.class);
			
			ConsultarPuntasPorTramo demo = new ConsultarPuntasPorTramo();				
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			System.out.println("SALIDA");
 			try {
 				ConsultarPuntasPorTramoSalida ObjOut = demo.logica(con, Objin);
 				//con.commit();
 				System.out.println(ObjOut.toString());
 			}
 			catch(Exception er) {
				er.printStackTrace();	
				con.rollback();
			}
			finally {
				con.close();
			}
	    }	
		System.out.println("== END ==");
	}
}