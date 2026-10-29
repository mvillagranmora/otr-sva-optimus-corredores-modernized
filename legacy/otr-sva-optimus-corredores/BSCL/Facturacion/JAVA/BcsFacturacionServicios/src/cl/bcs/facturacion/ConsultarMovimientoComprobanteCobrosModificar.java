package cl.bcs.facturacion;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConsultarMovimientoComprobanteCobrosModificar implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(ConsultarMovimientoComprobanteCobrosModificar.class);
	private final String SQL_SELECT_MOVIMIENTO_COMPROBANTE_COBROS = loadResourceToString("cl/bcs/facturacion/sql/ConsultarMovimientoComprobanteCobrosModificar_SelectMovimientoComprobanteCobros.sql");
	
	public static class EntradaConsultarMovimientoComprobanteCobrosModificar extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public String p_IdCobro;
		public List<MovimientoComprobante> p_MovimientosComprobante;
		
		public EntradaConsultarMovimientoComprobanteCobrosModificar() {
			this.p_MovimientosComprobante = new ArrayList<>();
		}
	}
	
	public static class MovimientoComprobante extends BcsPojo {
		public Long IdMovimientoComprobante;
	}
	
	public static class SalidaConsultarMovimientoComprobanteCobrosModificar extends BcsPojo {
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long	P_OUT_ERRORESCONTADOR;
		public List<MovimientoComprobanteCobrosModificar> P_OUT_COBROS;
		public SalidaConsultarMovimientoComprobanteCobrosModificar(){
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<>();
			this.P_OUT_COBROS = new ArrayList<>();
		}
	}
	
	public static class MovimientoComprobanteCobrosModificar extends BcsPojo {
		public String IdTipoCobro;
		public String NombreTipoCobro;
		public String NombreClasificacionCobro;
		public String NombreFormaCobro;
		public String IdMonedaCobro;
		public BigDecimal MontoCobro;
		public BigDecimal MontoCobroOriginal;
		public String IdMonedaCobroSistema;
		public BigDecimal MontoLocal;
		public String IdMonedaCobroLiquidacion;
		public BigDecimal MontoLiquidacion;
		public BigDecimal Porcentaje;
		public String Estado;
		public String Recalculable;
		public BigDecimal MontoOperacion;
	}
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaConsultarMovimientoComprobanteCobrosModificar in = gson.fromJson(jin.toString(), EntradaConsultarMovimientoComprobanteCobrosModificar.class);
		SalidaConsultarMovimientoComprobanteCobrosModificar out = logica(con, in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaConsultarMovimientoComprobanteCobrosModificar logica(Connection con, EntradaConsultarMovimientoComprobanteCobrosModificar entrada) throws Exception {
		logger.info("INICIO::ConsultarMovimientoComprobanteCobrosModificar");
		long inicioServicio = System.currentTimeMillis();
		
		SalidaConsultarMovimientoComprobanteCobrosModificar out = new SalidaConsultarMovimientoComprobanteCobrosModificar();
		out.P_OUT_COBROS = consultarMovimientoComprobanteCobros(con, entrada.p_IdCobro, entrada.p_MovimientosComprobante);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ConsultarMovimientoComprobanteCobrosModificar", finServicio - inicioServicio);
		return out;
	}
	
	private List<MovimientoComprobanteCobrosModificar> consultarMovimientoComprobanteCobros(Connection con, String idCobro, List<MovimientoComprobante> movimientosComprobante) throws Exception {
		logger.debug("INICIO::ConsultarMovimientoComprobanteCobrosModificar.consultarMovimientoComprobanteCobros");
		long inicio = System.currentTimeMillis();
		
		List<Long> idsMovimientoComprobante = movimientosComprobante.stream()
				.map(movimientoComprobante->movimientoComprobante.IdMovimientoComprobante)
				.distinct()
				.collect(Collectors.toList());
		
		HashMap<String, Object> parametros = new HashMap<String, Object>();
		parametros.put("IdCobro", idCobro);
		parametros.put("IdMovimientoComprobante", idsMovimientoComprobante);
		
		SQL sql = new SQL();
		List<MovimientoComprobanteCobrosModificar> movimientosComprobanteCobros = sql.query(con, SQL_SELECT_MOVIMIENTO_COMPROBANTE_COBROS, parametros, MovimientoComprobanteCobrosModificar.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultarMovimientoComprobanteCobrosModificar.consultarMovimientoComprobanteCobros", fin - inicio);
		return movimientosComprobanteCobros;
	}

}
