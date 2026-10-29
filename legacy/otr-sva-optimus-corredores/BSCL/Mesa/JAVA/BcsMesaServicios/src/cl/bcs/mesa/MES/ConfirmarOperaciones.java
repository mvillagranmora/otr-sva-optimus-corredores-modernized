package cl.bcs.mesa.MES;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.mesa.MRF.CrearOperacionMesa;
import cl.bcs.mesa.MRF.CrearOperacionMesa.CrearOperacionMesa_DetalleOperacion_IN;
import cl.bcs.mesa.MRF.CrearOperacionMesa.CrearOperacionMesa_FormaPagoOperacion_IN;
import cl.bcs.mesa.MRF.CrearOperacionMesa.CrearOperacionMesa_IN;
import cl.bcs.mesa.MRF.CrearOperacionMesa.CrearOperacionMesa_OUT;
import cl.bcs.mesa.MRF.CrearOperacionMesa.CrearOperacionMesa_Operacion_IN;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConfirmarOperaciones implements JavaCall{
	
	private static final Logger logger = LogManager.getLogger(ConfirmarOperaciones.class);
	
	private final String SQL_UPDATE_CONFIRMAR = loadResourceToString("cl/bcs/mesa/MES/sql/ConfirmarOperaciones.sql");
	private final String SQL_SELECT_OPERACION_INGRESADA_RF = loadResourceToString("cl/bcs/mesa/MES/sql/ConfirmarOperaciones_SelectOperacionIngresadaRF.sql");
	private final String SQL_SELECT_DETALLE_INGRESADA_RF = loadResourceToString("cl/bcs/mesa/MES/sql/ConfirmarOperaciones_SelectDetalleOperacionIngresadaRF.sql");
	private final String SQL_SELECT_FORMAPAGO_INGRESADA_RF = loadResourceToString("cl/bcs/mesa/MES/sql/ConfirmarOperaciones_SelectFormasPagoOperacionIngresadaRF.sql");
	
	public class ConfirmarOperacionesEntrada {
		public Long			p_IdOperacion;
		public Long			p_IdEmpresa;
		public Long			p_IdUsuario;
		public Long 		p_IdCliente;
		public String		p_IdModulo;
		public String 		p_IdMesa;
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	};
	
	public class ConfirmarOperacionesSalida{
		public List<MensajeSalida>			P_OUT_MENSAJES;
		public Long							P_OUT_ERRORESCONTADOR;
		
		public ConfirmarOperacionesSalida() {
			this.P_OUT_ERRORESCONTADOR  = 0L;
			this.P_OUT_MENSAJES 		= new ArrayList<MensajeSalida>();
		}
	};
	
	public static class Operacion{
		public Long IdOperacion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class DatosEntradaOperacion{
		public Long FolioOperacion;
		public Long FolioCartera;
		public Long IdPortafolio;
		public Long IdClienteCorredor;
		public Long IdPortafolioCorredor;
		public Long IdMesaOrigen;
		public Long IdMesaDestino;
		public String IdTipoProducto;// 'MERF'
		public String IdTipoOperacion;// COMTER,VENTER,COMPAC,VENPAC
		public String IdTipoReajuste;// "USD","UF"
		public Long Plazo;
		public BigDecimal Monto;
		public BigDecimal MontoMoneda;
		public String IdMoneda;// CLP "USD","UF" moneda local
		public String IdMonedaLiquidacion; // CLP "USD","UF"
		public BigDecimal MontoLiquidacion;
		public BigDecimal Tasa;
		public BigDecimal ValorFinal;
		public BigDecimal ValorFinalMoneda;
		public BigDecimal TasaAgente;
		public Date FechaOperacion;
		public Date FechaVencimiento;
		public String IdCondicionLiquidacion;
		public Date FechaLiquidacion;
		public Long IdAgente;
		public String IdEstado;
		public String Glosa;
		public String IdTipoCanalIngreso;
		public String TipoDocumento;
		public Long IdContacto;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class DatosDetalleOperacion{
		public Long IdReserva;
		public String IdTipoProducto; // 'MERF'
		public Long IdInstrumento;
		public String Nemotecnico;
		public String EsFungible;
		public String IdBolsa;
		public String IdBoveda;
		public Long IdCartera;
		public Long FolioCartera;
		public String CodigoIFRS;
		public String IdEmisor;
		public String IdMoneda;
		public String IdTipoReajuste;
		public String IdMonedaEmisionInstrumento;
		public String IdMonedaLiquidacionInstrumento;
		public String IdMonedaLiquidacionOperacion;
		public BigDecimal CorteMinimo;
		public BigDecimal Cantidad;
		public BigDecimal Precio;
		public BigDecimal PrecioMercado;
		public BigDecimal Tasa;
		public BigDecimal TasaMercado;
		public BigDecimal Monto;
		public BigDecimal MontoLiquidacion;
		public BigDecimal MontoLiquidacionOperacion;
		public BigDecimal ValorRescate;
		public Date FechaVencimiento;
		public BigDecimal MontoValorizadoReserva;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class DatosFormaPagoOperacion{
		public Long IdComprobantePagoOperacion;
		public String IdFormaDePago;
		public String IdMoneda;
		public String RecibePagaPortafolio;
		public BigDecimal MontoFormaDePago;
		public String AfectarTesoreria;
		public Long IdCuentaBanco;
		public Date FechaLiquidacionComprobantePagoOperacion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		Gson gson = Utils.getGsonBuilder();
		ConfirmarOperacionesEntrada jin = gson.fromJson(in.toString(), ConfirmarOperacionesEntrada.class);
		ConfirmarOperacionesSalida out = logica(conn, jin);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	};
	
	public ConfirmarOperacionesSalida logica(Connection conn, ConfirmarOperacionesEntrada in) throws Exception {
		logger.info("Inicio servicio MES.ConfirmarOperaciones");
		long startTime = System.nanoTime();
		SQL sql = new SQL();
		HashMap<String, String> mapMsg = new HashMap<>();
		
		ConfirmarOperacionesSalida out = new ConfirmarOperacionesSalida();
		CrearOperacionMesa creaOperacionRF = new CrearOperacionMesa();
		CrearOperacionMesa_IN creaOperacionRFEntrada = new CrearOperacionMesa_IN();
		CrearOperacionMesa_OUT creaOperacionRFSalida = new CrearOperacionMesa_OUT();
 		CrearOperacionMesa_Operacion_IN operacionEntrada = new CrearOperacionMesa_Operacion_IN();
		CrearOperacionMesa_DetalleOperacion_IN detalleEntrada = new CrearOperacionMesa_DetalleOperacion_IN();

		List<CrearOperacionMesa_Operacion_IN> listOperacionesEntrada = new ArrayList<>();
		List<CrearOperacionMesa_DetalleOperacion_IN> listDetalleEntrada = new ArrayList<>(); 
		List<CrearOperacionMesa_FormaPagoOperacion_IN> listFormaPagoEntrada = new ArrayList<>();
		HashMap<Long, CrearOperacionMesa_Operacion_IN> operacionEntradaHash = new HashMap<>();
		
		SimpleDateFormat formatoFecha = new SimpleDateFormat("yyyy-MM-dd");	
		try {
			if ((in.p_IdOperacion != null || in.p_IdOperacion != 0L)) {// falta diferenciar que sea forward
				boolean existenErroresFP= true;
				if(in.p_IdMesa.equals("MERF")) {
				HashMap<String, Object> params = new HashMap<>();
				params.put("IdOperacion", in.p_IdOperacion);

				logger.debug("[BuscaOperacion] Inicio SQL_SELECT_OPERACION_INGRESADA_RF");
				startTime = System.nanoTime();
				List<DatosEntradaOperacion> datosOperacion = sql.query(conn, SQL_SELECT_OPERACION_INGRESADA_RF, params, DatosEntradaOperacion.class);
				logger.debug("[BuscaOperacion] (" + (System.nanoTime() - startTime) / 1000000 + ") Fin SQL_SELECT_OPERACION_INGRESADA_RF");

				if (!datosOperacion.isEmpty()) {
					// Datos Operacion
					for (DatosEntradaOperacion operacion : datosOperacion) {

						operacionEntrada.Accion = "CERRAR";
						operacionEntrada.IdInternoOperacion = 1L;
						operacionEntrada.IdOperacion = in.p_IdOperacion;
						operacionEntrada.FolioOperacion = 0L;
						operacionEntrada.IdCliente = in.p_IdCliente;
						operacionEntrada.IdPortafolio = operacion.IdPortafolio;
						operacionEntrada.IdClienteCorredor = operacion.IdClienteCorredor;
						operacionEntrada.IdPortafolioCorredor = operacion.IdPortafolioCorredor;
						operacionEntrada.Intermesa = "N";
						operacionEntrada.IdMesaOrigen = operacion.IdMesaOrigen;
						operacionEntrada.IdMesaDestino = operacion.IdMesaDestino;
						operacionEntrada.AfectaCustodia = "S";
						operacionEntrada.IdTipoProducto = operacion.IdTipoProducto;
						operacionEntrada.IdTipoOperacion = operacion.IdTipoOperacion;
						operacionEntrada.IdTipoReajuste = operacion.IdTipoReajuste;
						operacionEntrada.Plazo = operacion.Plazo;
						operacionEntrada.Monto = operacion.Monto;
						operacionEntrada.MontoMoneda = operacion.MontoMoneda;
						operacionEntrada.MontoLiquidacion = operacion.MontoLiquidacion;
						operacionEntrada.IdMoneda = operacion.IdMoneda;
						operacionEntrada.IdMonedaLiquidacion = operacion.IdMonedaLiquidacion;
						operacionEntrada.Tasa = operacion.Tasa;
						operacionEntrada.ValorFinal = operacion.ValorFinal;
						operacionEntrada.ValorFinalMoneda = operacion.ValorFinalMoneda;
						operacionEntrada.TasaAgente = operacion.TasaAgente;
						operacionEntrada.FechaOperacion = formatoFecha.format(operacion.FechaOperacion);
						operacionEntrada.FechaVencimiento = formatoFecha.format(operacion.FechaVencimiento);
						operacionEntrada.IdCondicionLiquidacion = operacion.IdCondicionLiquidacion;
						operacionEntrada.FechaLiquidacion = formatoFecha.format(operacion.FechaLiquidacion);
						operacionEntrada.IdAgente = operacion.IdAgente;
						operacionEntrada.IdOperador = in.p_IdUsuario;
						operacionEntrada.IdEstado = operacion.IdEstado;
						operacionEntrada.Glosa = operacion.Glosa;
						operacionEntrada.IdTipoCanalIngreso = operacion.IdTipoCanalIngreso;
						operacionEntrada.TipoDocumento = operacion.TipoDocumento;
						operacionEntrada.IdContacto = operacion.IdContacto;

						listOperacionesEntrada.add(operacionEntrada);
						operacionEntradaHash.put(in.p_IdOperacion, operacionEntrada);
					}

					// Datos detalle

					logger.debug("[BuscaDetalle] Inicio SQL_SELECT_DETALLE_INGRESADA_RF");
					startTime = System.nanoTime();
					List<DatosDetalleOperacion> datosDetalle = sql.query(conn, SQL_SELECT_DETALLE_INGRESADA_RF, params, DatosDetalleOperacion.class);
					logger.debug("[BuscaDetalle] (" + (System.nanoTime() - startTime) / 1000000 + ") Fin SQL_SELECT_DETALLE_INGRESADA_RF");

					if (!datosDetalle.isEmpty()) {
						for (DatosDetalleOperacion detalle : datosDetalle) {
							detalleEntrada = new CrearOperacionMesa_DetalleOperacion_IN();
							detalleEntrada.IdInternoDetalle = 0L;
							detalleEntrada.IdInternoOperacion = operacionEntradaHash.get(in.p_IdOperacion).IdInternoOperacion;
							detalleEntrada.IdReserva = detalle.IdReserva;
							detalleEntrada.IdCliente = in.p_IdCliente;
							detalleEntrada.IdPortafolio = operacionEntradaHash.get(in.p_IdOperacion).IdPortafolio;
							detalleEntrada.IdClienteCorredor = operacionEntradaHash.get(in.p_IdOperacion).IdClienteCorredor;
							detalleEntrada.IdPortafolioCorredor = operacionEntradaHash.get(in.p_IdOperacion).IdPortafolioCorredor;
							detalleEntrada.IdMesa = operacionEntradaHash.get(in.p_IdOperacion).IdMesaOrigen;
							detalleEntrada.IdTipoProducto = detalle.IdTipoProducto;
							detalleEntrada.IdTipoOperacion = operacionEntradaHash.get(in.p_IdOperacion).IdTipoOperacion;
							detalleEntrada.IdInstrumento = detalle.IdInstrumento;
							detalleEntrada.Nemotecnico = detalle.Nemotecnico;
							detalleEntrada.EsFungible = detalle.EsFungible;
							detalleEntrada.IdBolsa = detalle.IdBolsa;
							detalleEntrada.IdBoveda = detalle.IdBoveda;
							detalleEntrada.IdCartera = detalle.IdCartera;
							detalleEntrada.FolioCartera = detalle.FolioCartera;
							detalleEntrada.CodigoIFRS = detalle.CodigoIFRS;
							detalleEntrada.IdEmisor = detalle.IdEmisor;
							detalleEntrada.IdMoneda = detalle.IdMoneda;
							detalleEntrada.IdTipoReajuste = detalle.IdTipoReajuste;
							detalleEntrada.IdMonedaEmisionInstrumento = detalle.IdMonedaEmisionInstrumento;
							detalleEntrada.IdMonedaLiquidacionInstrumento = detalle.IdMonedaLiquidacionInstrumento;
							detalleEntrada.IdMonedaLiquidacionOperacion = detalle.IdMonedaLiquidacionOperacion;
							detalleEntrada.CorteMinimo = detalle.CorteMinimo;
							detalleEntrada.Cantidad = detalle.Cantidad;
							detalleEntrada.Precio = detalle.Precio;
							detalleEntrada.PrecioMercado = detalle.PrecioMercado;
							detalleEntrada.Tasa = detalle.Tasa;
							detalleEntrada.TasaMercado = detalle.TasaMercado;
							detalleEntrada.Monto = detalle.Monto;
							detalleEntrada.MontoLiquidacion = detalle.MontoLiquidacion;
							detalleEntrada.MontoLiquidacionOperacion = detalle.MontoLiquidacionOperacion;
							detalleEntrada.ValorRescate = detalle.ValorRescate;
							detalleEntrada.FechaVencimiento = formatoFecha.format(detalle.FechaVencimiento);
							detalleEntrada.MontoValorizadoReserva = detalle.MontoValorizadoReserva;

							listDetalleEntrada.add(detalleEntrada);

						}

						// formas de pago

						logger.debug("[BuscaDetalle] Inicio SQL_SELECT_FORMAPAGO_INGRESADA_RF");
						startTime = System.nanoTime();
						List<DatosFormaPagoOperacion> datosFormasPago = sql.query(conn, SQL_SELECT_FORMAPAGO_INGRESADA_RF, params, DatosFormaPagoOperacion.class);
						logger.debug("[BuscaDetalle] (" + (System.nanoTime() - startTime) / 1000000 + ") Fin SQL_SELECT_FORMAPAGO_INGRESADA_RF");
					
						
						
						if (!datosFormasPago.isEmpty()) {
							Long IdInterno=1L;
						

							for (DatosFormaPagoOperacion formaspago : datosFormasPago) {
								if(formaspago.MontoFormaDePago.compareTo(BigDecimal.ZERO)== 0) {
									Long Id = 0L;
									 existenErroresFP= false;
									 out.P_OUT_ERRORESCONTADOR = 1L;
									 out.P_OUT_MENSAJES.add(new MensajeSalida(Id, MensajeSalida.MENSAJE_ERROR, "UI_FORMA_DE_PAGO_EN_CERO", mapMsg, conn, in.p_IdEmpresa));
									break;
								}else {
									CrearOperacionMesa_FormaPagoOperacion_IN formaPagoentrada = new CrearOperacionMesa_FormaPagoOperacion_IN();
									formaPagoentrada.IdInternoPagoOperacion = IdInterno;//(long) datosFormasPago.size();
									formaPagoentrada.IdInternoOperacion = 1L;
									formaPagoentrada.IdComprobantePagoOperacion = 0L;
									formaPagoentrada.IdFormaDePago = formaspago.IdFormaDePago;
									formaPagoentrada.IdMoneda = formaspago.IdMoneda;
									formaPagoentrada.IdOperacion = in.p_IdOperacion;
									formaPagoentrada.RecibePagaPortafolio = formaspago.RecibePagaPortafolio;
									formaPagoentrada.MontoFormaDePago = formaspago.MontoFormaDePago;
									formaPagoentrada.AfectarTesoreria = formaspago.AfectarTesoreria;
									formaPagoentrada.IdCuentaBancaria = formaspago.IdCuentaBanco;
									formaPagoentrada.FechaLiquidacion = formatoFecha.format(formaspago.FechaLiquidacionComprobantePagoOperacion);
									listFormaPagoEntrada.add(formaPagoentrada);
									IdInterno++;
								}
								
							}
							
							if(existenErroresFP != false) {
								creaOperacionRFEntrada.p_IdEmpresa = in.p_IdEmpresa;
								creaOperacionRFEntrada.p_IdUsuario = in.p_IdUsuario;
								creaOperacionRFEntrada.p_IdModulo = in.p_IdModulo;
								creaOperacionRFEntrada.p_UsaReglasNegocio = "S";
								creaOperacionRFEntrada.p_Operacion = listOperacionesEntrada;
								creaOperacionRFEntrada.p_DetalleOperacion = listDetalleEntrada;
								creaOperacionRFEntrada.p_FormasDePago = listFormaPagoEntrada;

								// Agrega Ingreso custodia, tesoreria, cta inv
								creaOperacionRFSalida = creaOperacionRF.logica(conn, creaOperacionRFEntrada);
							}
						}
					}
				}
				
			}
				
				Long erroresCrearOperacionRFSalida =  creaOperacionRFSalida.P_OUT_ERRORESCONTADOR; 
				boolean erroresCrearOperacionRFSalidaBoolean = true;
				if(erroresCrearOperacionRFSalida != null) {
					if(erroresCrearOperacionRFSalida >0) {
						erroresCrearOperacionRFSalidaBoolean = false;
					}
				}
				if ((in.p_IdMesa.equals("MEFW") ||  !erroresCrearOperacionRFSalidaBoolean) &&  existenErroresFP != false) {
					// cambia estado confirmacion pago operacion
					List<Operacion> param = new ArrayList<>();
					Operacion ope = new Operacion();
					ope.IdOperacion = in.p_IdOperacion;

					param.add(ope);
					sql.batch(conn, SQL_UPDATE_CONFIRMAR, param, Operacion.class);
					out.P_OUT_ERRORESCONTADOR = 0L;
					out.P_OUT_MENSAJES.addAll(creaOperacionRFSalida.P_OUT_MENSAJES);
				} else {
					out.P_OUT_ERRORESCONTADOR = 1L;
					out.P_OUT_MENSAJES.addAll(creaOperacionRFSalida.P_OUT_MENSAJES);
				}
			} else {
				out.P_OUT_ERRORESCONTADOR = 1L;
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "No hay operaciones", mapMsg, conn, in.p_IdEmpresa));
			}
		} catch (Exception e) {
			out.P_OUT_ERRORESCONTADOR = 1L;
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, e.getMessage(), mapMsg, conn, in.p_IdEmpresa));
		}
		
		return out;
	}
	public static void main(String[] args) {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = Utils.getGsonBuilder();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":1646,\"p_IdModulo\":\"MRF\",\"p_IdOperacion\":585827,\"p_IdCliente\":21648,\"p_IdMesa\":\"MERF\"}";
			ConfirmarOperacionesEntrada Objin = gson.fromJson(json, ConfirmarOperacionesEntrada.class);
			ConfirmarOperaciones demo = new ConfirmarOperaciones();
			System.out.println("ENTRADA");
			System.out.println(Objin.toString());
			try {
				ConfirmarOperacionesSalida ObjOut = demo.logica(con, Objin);
				con.rollback();
				//con.commit();
				System.out.println(ObjOut.P_OUT_MENSAJES);
				System.out.println("SALIDA");
			} catch (Exception er) {
				er.printStackTrace();
				con.rollback();
			}
			con.rollback();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println("== END ==");
	}
}
