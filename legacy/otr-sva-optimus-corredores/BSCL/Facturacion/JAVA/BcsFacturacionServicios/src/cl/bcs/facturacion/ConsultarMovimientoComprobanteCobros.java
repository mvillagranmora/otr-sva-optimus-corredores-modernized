package cl.bcs.facturacion;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Utils;

public class ConsultarMovimientoComprobanteCobros implements JavaCall{
	static final Logger logger = LogManager.getLogger(ConsultarMovimientoComprobanteCobros.class);
	private final String SQL_CONSULTAR_MOVIMIENTOS = loadResourceToString("cl/bcs/facturacion/sql/ConsultarMovimientoComprobanteCobros_SelectMovimientoComprobanteCobrosPorIdMovimientoComprobante.sql");
	
	public static class EntradaConsultarMovimientoComprobanteCobros extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public List<Long> p_IdsMovimientosComprobante;
		
		public EntradaConsultarMovimientoComprobanteCobros() {
			this.p_IdsMovimientosComprobante = new ArrayList<>();
		}
	}
	
	public static class SalidaConsultarMovimientoComprobanteCobros extends BcsPojo {
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		public List<MovimientosComprobantesCobros> P_OUT_COBROS;
	}
	
	public static class MovimientosComprobantesCobros extends BcsPojo {
		public Long IdCobro;
		public BigDecimal Porcentaje;
		public String IdEstado;
		public String EsRecalculable;
		public String CodigoCobroClasificacion;
		public String DescripcionCobroClasificacion;
		public String NombreCobro;
		public String FormaCobro;
		public String NombreFormaCobro;
		public String IdMonedaCobro;
		public String IdMonedaLiquidacionCobro;
		public String IdMonedaMovimientoComprobanteCobro;
		public String CodigoMonedaSistemaCobro;
		public String NombreMonedaSistemaCobro;
		public Long MontoMovimientoComprobanteCobro;
		public String CodigoMonedaLiquidacionCobro; 
		public String NombreMonedaLiquidacionCobro;
		public Long MontoLiquidacionCobro;
		
		public MovimientosComprobantesCobros () {
			
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		
		EntradaConsultarMovimientoComprobanteCobros in = gson.fromJson(jin.toString(), EntradaConsultarMovimientoComprobanteCobros.class);
		SalidaConsultarMovimientoComprobanteCobros out = logica(conn, in);
		
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public SalidaConsultarMovimientoComprobanteCobros logica(Connection conn, EntradaConsultarMovimientoComprobanteCobros entrada) throws Exception {
		logger.info("INICIO::ConsultarMovimientoComprobanteCobros");
		long inicio = System.currentTimeMillis();
		
		SalidaConsultarMovimientoComprobanteCobros salida = new SalidaConsultarMovimientoComprobanteCobros();
		
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultarMovimientoComprobanteCobros {}", fin - inicio);
		
		return salida;
	}
	
}
