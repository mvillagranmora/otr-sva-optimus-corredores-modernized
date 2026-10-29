package cl.bcs.canalagente;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.cuentaInversion.ConsultarCartolaDeMovimiento;
import cl.bcs.cuentaInversion.ConsultarCartolaDeMovimiento.CartolaDeMovimiento;
import cl.bcs.cuentaInversion.ConsultarCartolaDeMovimiento.EntradaConsultarCartolaDeMovimiento;
import cl.bcs.cuentaInversion.ConsultarCartolaDeMovimiento.SalidaConsultarCartolaDeMovimiento;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;

public class ConsultarHistorialMovimientosCuentaInversion implements JavaCall  {
	
	private static final Logger logger = LogManager.getLogger(ConsultarHistorialMovimientosCuentaInversion.class);
	private final String SQL_SELECT_IDCLIENTE = loadResourceToString("cl/bcs/canalagente/sql/ConsultarHistorialMovimientosCuentaInversion_SelectIdClientePortafolio.sql");
	private final String SQL_INSERT_CARTOLADEMOVIMIENTOCOMPROBANTE = loadResourceToString("cl/bcs/canalagente/sql/ConsultarHistorialMovimientosCuentaInversion_InsertCartolaDeMovimientoComprobante.sql");
	private final String SQL_SELECT_CARTOLADEMOVIMIENTOCOMPROBANTE = loadResourceToString("cl/bcs/canalagente/sql/ConsultarHistorialMovimientosCuentaInversion_SelectCartolaDeMovimientoComprobante.sql");
	private final String SQL_DELETE_CARTOLADEMOVIMIENTOCOMPROBANTE = loadResourceToString("cl/bcs/canalagente/sql/ConsultarHistorialMovimientosCuentaInversion_DeleteCartolaDeMovimientoComprobante.sql");

	public static class EntradaConsultarHistorialMovimientosCuentaInversion {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public Long p_IdPortafolio;
		public String p_FechaDesde;
		public String p_FechaHasta;
		public String p_IdMoneda;
		public Long p_IdMatrizAgrupador;
		public String p_EsADC;
		
		public EntradaConsultarHistorialMovimientosCuentaInversion() {
			this.p_IdMatrizAgrupador = null;
			this.p_EsADC = null;
		}

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class SalidaConsultarHistorialMovimientosCuentaInversion {
		public BigDecimal P_SALDOEFECTIVOINICIAL;
		public BigDecimal P_SALDOEFECTIVOFINAL;
		public List<MovimientoCuentaInversion> P_OUT_RESULTADO;
		public Long P_OUT_ADVERTENCIASCONTADOR;
		public List<AdvertenciasDetalle> P_OUT_ADVERTENCIASDETALLE;
		public Long P_OUT_ERRORESCONTADOR;
		public List<ErroresDetalle> P_OUT_ERRORESDETALLE;

		public SalidaConsultarHistorialMovimientosCuentaInversion() {
			this.P_OUT_RESULTADO = new ArrayList<MovimientoCuentaInversion>();
			this.P_OUT_ADVERTENCIASDETALLE = new ArrayList<AdvertenciasDetalle>();
			this.P_OUT_ERRORESDETALLE = new ArrayList<ErroresDetalle>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class MovimientoCuentaInversion {
		public Long IdCargoAbonoMovimiento;
		public String NombreCliente;
		public Long IdCliente;
		public Long IdPortafolio;
		public Date FechaMovimiento;
		public Date FechaLiquidacion;
		public String IdMoneda;
		public String IdConcepto;
		public String NombreConcepto;
		public BigDecimal Cargo;
		public BigDecimal Abono;
		public String FolioComprobante;
		public String IdEstado;
		public String NombreEstado;
		public String NombreTipoComprobante;
		public String NombreTipoProducto;
		public String NombreTipoOperacion;
		public String NombreTipoMovimiento;
		public BigDecimal SaldoSimulado;
		public String CodigoPortafolio;
		public Long IdComprobante;
		public Long IdRespuestaDTE;
		public String RespuestaPdfDTE;
		public String DocumentoIdentidad;
		public Long IdMatrizAgrupador;
		public String EsADC;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class MovimientoCuentaInversionComprobante {
		public Long IdCargoAbonoMovimiento;
		public String FolioComprobante;
		public String CodigoPortafolio;
		public Long IdRespuestaDTE;
		public String RespuestaPdfDTE;
		public String DocumentoIdentidad;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class AdvertenciasDetalle{
		public String Codigo;
		public String RequiereAutorizacion;
		public String Descripcion;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class ErroresDetalle{
		public String Codigo;
		public String Descripcion;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class IdClientePortafolio {
		public Long IdCliente;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	@Trace
	public SalidaConsultarHistorialMovimientosCuentaInversion logica(Connection con, EntradaConsultarHistorialMovimientosCuentaInversion entrada) throws Exception {
		logger.info("INICIO::ConsultarHistorialMovimientosCuentaInversion");
		long inicioServicio = System.currentTimeMillis();
		SalidaConsultarHistorialMovimientosCuentaInversion salida = new SalidaConsultarHistorialMovimientosCuentaInversion();
		SQL sql=new SQL();
		try {
			HashMap<String, Object> params = new HashMap<>();
			params.put("IdPortafolio", entrada.p_IdPortafolio);
			long inicioSelectIdCliente = System.currentTimeMillis();
			List<IdClientePortafolio> idClientePortafolio = sql.query(con, SQL_SELECT_IDCLIENTE, params, IdClientePortafolio.class);
			long finSelectIdCliente = System.currentTimeMillis();
			logger.info("Tiempo SQL_SELECT_IDCLIENTE: " + (finSelectIdCliente - inicioSelectIdCliente));
			if (idClientePortafolio.isEmpty()) {
				ErroresDetalle error = new ErroresDetalle();
				error.Codigo = MensajeSalida.MENSAJE_ERROR;
				error.Descripcion = "Cliente no encontrado.";
				salida.P_OUT_ERRORESCONTADOR++;
				salida.P_OUT_ERRORESDETALLE.add(error);
				return salida;
			}
			
			EntradaConsultarCartolaDeMovimiento entradaConsultarCartolaDeMovimiento = new EntradaConsultarCartolaDeMovimiento();
			entradaConsultarCartolaDeMovimiento.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaConsultarCartolaDeMovimiento.p_IdUsuario = entrada.p_IdUsuario;
			entradaConsultarCartolaDeMovimiento.p_IdModulo = entrada.p_IdModulo;
			entradaConsultarCartolaDeMovimiento.p_UsaReglasNegocio = "S";
			entradaConsultarCartolaDeMovimiento.p_IdCuentaInversion = null;
			entradaConsultarCartolaDeMovimiento.p_IdCliente = idClientePortafolio.get(0).IdCliente;
			entradaConsultarCartolaDeMovimiento.p_IdPortafolio = entrada.p_IdPortafolio;
			entradaConsultarCartolaDeMovimiento.p_IdMoneda = entrada.p_IdMoneda;
			entradaConsultarCartolaDeMovimiento.p_IdEstado = null;
			entradaConsultarCartolaDeMovimiento.p_FechaDesde = entrada.p_FechaDesde;
			entradaConsultarCartolaDeMovimiento.p_FechaHasta = entrada.p_FechaHasta;
			entradaConsultarCartolaDeMovimiento.p_SaldoPorFechaEfectiva = "N";
			entradaConsultarCartolaDeMovimiento.p_IdMatrizAgrupador = entrada.p_IdMatrizAgrupador;
			entradaConsultarCartolaDeMovimiento.p_EsADC = entrada.p_EsADC;
			
			ConsultarCartolaDeMovimiento cartolaDeMovimiento = new ConsultarCartolaDeMovimiento();
			
			long inicioConsultarCartolaDeMovimiento = System.currentTimeMillis();
			SalidaConsultarCartolaDeMovimiento salidaCartolaDeMovimiento = cartolaDeMovimiento.logica(con, entradaConsultarCartolaDeMovimiento);
			long finConsultarCartolaDeMovimiento = System.currentTimeMillis();
			logger.info("Tiempo ConsultarCartolaDeMovimiento: " + (finConsultarCartolaDeMovimiento - inicioConsultarCartolaDeMovimiento));
			
			long inicioInsertCartolaDeMovimiento = System.currentTimeMillis();
			sql.batch(con, SQL_INSERT_CARTOLADEMOVIMIENTOCOMPROBANTE, salidaCartolaDeMovimiento.P_OUT_CARTOLADEMOVIMIENTO, CartolaDeMovimiento.class);
			long finInsertCartolaDeMovimiento = System.currentTimeMillis();
			logger.info("Tiempo SQL_INSERT_CARTOLADEMOVIMIENTOCOMPROBANTE: " + (finInsertCartolaDeMovimiento - inicioInsertCartolaDeMovimiento));
			
			long inicioSelectCartolaDeMovimientoComprobante = System.currentTimeMillis();
			List<MovimientoCuentaInversionComprobante> listaMovimientoCuentaInversionComprobante = sql.query(con, SQL_SELECT_CARTOLADEMOVIMIENTOCOMPROBANTE, new HashMap<>(), MovimientoCuentaInversionComprobante.class);
			long finSelectCartolaDeMovimientoComprobante = System.currentTimeMillis();
			logger.info("Tiempo SQL_SELECT_CARTOLADEMOVIMIENTOCOMPROBANTE: " + (finSelectCartolaDeMovimientoComprobante - inicioSelectCartolaDeMovimientoComprobante));
			
			HashMap<Long, MovimientoCuentaInversionComprobante> hashMovimientoCuentaInversionComprobante = new HashMap<>();
			for (MovimientoCuentaInversionComprobante movimiento: listaMovimientoCuentaInversionComprobante) {
				hashMovimientoCuentaInversionComprobante.put(movimiento.IdCargoAbonoMovimiento, movimiento);
			}
			
			for (CartolaDeMovimiento cartola : salidaCartolaDeMovimiento.P_OUT_CARTOLADEMOVIMIENTO) {
				MovimientoCuentaInversionComprobante movimientoComprobante = hashMovimientoCuentaInversionComprobante.get(cartola.IdCargoAbonoMovimiento);
				movimientoComprobante = movimientoComprobante != null ? movimientoComprobante : new MovimientoCuentaInversionComprobante();
				MovimientoCuentaInversion movimiento = new MovimientoCuentaInversion();
				movimiento.IdCargoAbonoMovimiento = cartola.IdCargoAbonoMovimiento;
				movimiento.NombreCliente = cartola.NombreCliente;
				movimiento.IdCliente = cartola.IdCliente;
				movimiento.IdPortafolio = cartola.IdPortafolio;
				movimiento.FechaMovimiento = cartola.FechaSistema;
				movimiento.FechaLiquidacion = cartola.FechaLiquidacionOperacion;
				movimiento.IdMoneda = cartola.IdMoneda;
				movimiento.IdConcepto = cartola.IdConcepto;
				movimiento.NombreConcepto = cartola.NombreConcepto;
				movimiento.Cargo = cartola.Cargo;
				movimiento.Abono = cartola.Abono;
				movimiento.FolioComprobante = movimientoComprobante.FolioComprobante;
				movimiento.IdEstado = cartola.IdEstado;
				movimiento.NombreEstado = cartola.NombreEstado;
				movimiento.NombreTipoComprobante = cartola.NombreTipoComprobante;
				movimiento.NombreTipoProducto = cartola.NombreTipoProducto;
				movimiento.NombreTipoOperacion = cartola.NombreTipoOperacion;
				movimiento.NombreTipoMovimiento = cartola.NombreTipoMovimiento;
				movimiento.SaldoSimulado = cartola.SaldoSimulado;
				movimiento.CodigoPortafolio = movimientoComprobante.CodigoPortafolio;
				movimiento.IdComprobante = cartola.IdComprobante;
				movimiento.IdRespuestaDTE = movimientoComprobante.IdRespuestaDTE;
				movimiento.RespuestaPdfDTE = movimientoComprobante.RespuestaPdfDTE;
				movimiento.DocumentoIdentidad = movimientoComprobante.DocumentoIdentidad;
				movimiento.IdMatrizAgrupador = cartola.IdMatrizAgrupador;
				movimiento.EsADC = cartola.EsADC;
				salida.P_OUT_RESULTADO.add(movimiento);
			}
			SQL.exec(con, SQL_DELETE_CARTOLADEMOVIMIENTOCOMPROBANTE);
			salida.P_OUT_ERRORESCONTADOR = 0L;
		}catch(Exception e){
			logger.error("ERROR",e);
			throw e;
		}
		long finServicio = System.currentTimeMillis();
		logger.info("Tiempo ConsultarHistorialMovimientosCuentaInversion: " + (finServicio - inicioServicio));
		logger.info("FIN::ConsultarHistorialMovimientosCuentaInversion");
		return salida;
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaConsultarHistorialMovimientosCuentaInversion jin = gson.fromJson(in.toString(), EntradaConsultarHistorialMovimientosCuentaInversion.class);
		SalidaConsultarHistorialMovimientosCuentaInversion out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("becbpro1","connections.properties")) {
			Gson gson = Utils.getGsonBuilder();		
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":\"1646\",\"p_IdModulo\":\"CTA\",\"p_IdPortafolio\":66720,\"p_FechaDesde\":\"2026-05-14\",\"p_FechaHasta\":\"2026-08-14\",\"p_IdMoneda\":\"CLP\",\"p_IdMatrizAgrupador\":\"0\"}";
			EntradaConsultarHistorialMovimientosCuentaInversion Objin = gson.fromJson(json, EntradaConsultarHistorialMovimientosCuentaInversion.class);
			ConsultarHistorialMovimientosCuentaInversion  demo=new ConsultarHistorialMovimientosCuentaInversion();				
			try {
				SalidaConsultarHistorialMovimientosCuentaInversion asdf = demo.logica(con, Objin);
				//con.commit();
				System.out.println(asdf.toString());
			}
			catch(Exception er) {
				er.printStackTrace();	
			}
			finally {
				con.rollback();
				con.close();
			}
    	}
		System.out.println("== END ==");
	}

}
