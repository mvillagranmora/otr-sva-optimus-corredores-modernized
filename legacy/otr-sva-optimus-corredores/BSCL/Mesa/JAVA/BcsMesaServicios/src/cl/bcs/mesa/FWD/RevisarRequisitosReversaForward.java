package cl.bcs.mesa.FWD;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import com.google.gson.JsonObject;

import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;
import cl.bcs.mesa.FWD.ReversaAnticipoForward.ReversaAnticipoForwardEntrada;

public class RevisarRequisitosReversaForward implements JavaCall {
	
	private final String SQL_CONSULTAR_MOVIMIENTOS_FWD_VENCIMIENTO = loadResourceToString("cl/bcs/mesa/FWD/sql/ReversaAnticipoForward_ConsultarMovimientosFWDVencimiento.sql");
	private final String SQL_CONSULTAR_MOVIMIENTOS_SPOT_FWD_VENCIMIENTO = loadResourceToString("cl/bcs/mesa/FWD/sql/ReversaAnticipoForward_ConsultarMovimientosSpotFWDVencimiento.sql");
	private final String SQL_CONSULTAR_MODALIDAD_LIQUIDACION_FWD = loadResourceToString("cl/bcs/mesa/FWD/sql/ReversaAnticipoForward_ConsultarModalidadLiquidacion.sql");
	
	@Trace
	public RevisarRequisitosReversaForwardSalida logica(Connection conn, ReversaAnticipoForwardEntrada in) throws Exception {
		RevisarRequisitosReversaForwardSalida salida = new RevisarRequisitosReversaForwardSalida();
		
		HashMap<String,String> mapMsg = new HashMap<>();
		Long Id = 0L;
		SQL sql = new SQL();
		HashMap<String,Object> params = new HashMap<>();
		
		params.put("IdCartera", in.p_tablaOperReversa.get(0).IdCartera);
		List<ModalidadLiquidacion> modalidadLiquidacion = sql.query(conn, SQL_CONSULTAR_MODALIDAD_LIQUIDACION_FWD, params, ModalidadLiquidacion.class);
		
		if (modalidadLiquidacion.size() == 0) {
			salida.p_out_Mensajes.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"SRV_MRF_NO_SE_ENCONTRO_CARTERA_DE_OPERACION",mapMsg,conn,in.p_IdEmpresa));
		} else if(modalidadLiquidacion.get(0).IdEstado.contentEquals("ANT") || modalidadLiquidacion.get(0).OperaCuentaInversion.contentEquals("S")) {
			return salida;
		} else {
			if(modalidadLiquidacion.get(0).ModalidadLiquidacion.contentEquals("COMPENSADA")) { /*LIQUIDACION COMPENSADA*/
				List<Movimiento> datosMovimiento = sql.query(conn, SQL_CONSULTAR_MOVIMIENTOS_FWD_VENCIMIENTO, params, Movimiento.class);
				
				if(datosMovimiento.size()>0 && datosMovimiento.get(0).IdEstadoComprobante.contentEquals("LIQ")) {
					mapMsg.put("Id",Long.toString(datosMovimiento.get(0).IdComprobante));
					salida.p_out_Mensajes.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"FWD_COMPROBANTE_TESORERIA_LIQUIDADO",mapMsg,conn,in.p_IdEmpresa));
				}	
			} else { /*LIQUIDACION ENTREGA FISICA*/
				String fwdIntegradoConSpot = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "MESA", "FwdIntegradoConSpot");
				if (fwdIntegradoConSpot.equals("S")) {
					List<MovimientoSpot> datosMovimientoSpot = sql.query(conn, SQL_CONSULTAR_MOVIMIENTOS_SPOT_FWD_VENCIMIENTO, params, MovimientoSpot.class);
					
					if(datosMovimientoSpot.get(0).IdComprobanteFacturacion > 0) {
						mapMsg.put("Id",Long.toString(datosMovimientoSpot.get(0).IdOperacionSPOT));
						salida.p_out_Mensajes.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"UI_MOVIMIENTO_FACTURADO",mapMsg,conn,in.p_IdEmpresa));
					}
				}
			}
		}
		return salida;
	}
	
	public static class Movimiento {
		public Long IdCartera;
		public Long IdPortafolio;
		public Long FolioOperacion;
		public String ModalidadLiquidacion;
		public String IdEstadoFWD;
		public Long IdComprobante;
		public Long IdComprobanteFormaDePago;
		public String IdEstadoComprobante;
	}
	
	public static class MovimientoSpot {
		public Long IdCartera;
		public Long IdPortafolio;
		public Long FolioOperacion;
		public String ModalidadLiquidacion;
		public String IdEstadoFWD;
		public Long IdOperacionSPOT;
		public String IdTipoOperacionSPOT;
		public String EstadoSPOT;
		public Long IdComprobanteFacturacion;
	}
	
	public static class ModalidadLiquidacion {
		public Long IdCartera;
		public String ModalidadLiquidacion;
		public String IdEstado;
		public String OperaCuentaInversion;
	}
	
	public static class RevisarRequisitosReversaForwardSalida {
		public List<MensajeSalida> p_out_Mensajes;
		
		public RevisarRequisitosReversaForwardSalida() {
			this.p_out_Mensajes = new ArrayList<MensajeSalida>();
		}
		
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		return null;
	}
}