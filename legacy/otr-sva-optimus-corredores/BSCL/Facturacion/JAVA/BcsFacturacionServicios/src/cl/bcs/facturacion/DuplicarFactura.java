package cl.bcs.facturacion;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import cl.bcs.facturacion.CrearMovimientoComprobante.Comprobante_Salida;
import cl.bcs.facturacion.CrearMovimientoComprobante.CrearMovimientoComprobante_Entrada;
import cl.bcs.facturacion.CrearMovimientoComprobante.CrearMovimientoComprobante_Salida;
import cl.bcs.facturacion.CrearMovimientoComprobante.MovimientoComprobanteCobro_Entrada;
import cl.bcs.facturacion.CrearMovimientoComprobante.MovimientoComprobanteFormaDePago_Entrada;
import cl.bcs.facturacion.CrearMovimientoComprobante.MovimientoComprobante_Entrada;
import cl.bcs.facturacion.CrearMovimientoComprobante.MovimientoComprobante_Salida;
import cl.bcs.plataforma.pla.ObtenerPlazo;
import cl.bcs.plataforma.pla.ObtenerPlazo.OutObtenerPlazo;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class DuplicarFactura implements JavaCall{
	
	private static final Logger logger = LogManager.getLogger(DuplicarFactura.class);
	
	private final String SQL_SELECT_MOVIMIENTO_FACTURAR = loadResourceToString("cl/bcs/facturacion/sql/DuplicarFactura_SelectMovimientoFacturar.sql");
	private final String SQL_SELECT_FORMAS_PAGO_MOVIMIENTO = loadResourceToString("cl/bcs/facturacion/sql/DuplicarFactura_SelectFormaPagoMovimientoFacturar.sql");
	private final String SQL_SELECT_CONDICION_LIQUIDACION = loadResourceToString("cl/bcs/facturacion/sql/DuplicarFactura_SelectCondicionLiquidacion.sql");
	
	public static class EntradaDuplicarFactura extends BcsPojo{
		
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public Long IdComprobanteBase;		
		public List<MovimientoComprobanteCobro_Entrada> p_MovimientosCobros;
		
	}
	
	public static class SalidaDuplicarFactura extends BcsPojo{
		public Long P_OUT_ERRORESCONTADOR;
		public List<PlataformaUtils.MensajeSalida> P_OUT_MENSAJES;	
		public List<MovimientoComprobante_Salida> p_out_Movimientos;
		public List<Comprobante_Salida> p_out_Comprobantes;

		public SalidaDuplicarFactura() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<>();
			this.p_out_Comprobantes = new ArrayList<>();
			this.p_out_Movimientos = new ArrayList<>();
		}
	}
	
	public static class MovimientoBase extends MovimientoComprobante_Entrada {
		public Long IdMovimientoComprobante;		
	}
	
	public static class CondicionDeLiquidacion extends BcsPojo {
		public Long PlazoEnDias;
		public String IdCondicionDeLiquidacion;
	}

	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaDuplicarFactura in = gson.fromJson(jin.toString(), EntradaDuplicarFactura.class);
		SalidaDuplicarFactura out = logica(conn, in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}


	private SalidaDuplicarFactura logica(Connection conn, EntradaDuplicarFactura in) {
		logger.info("INICIO::DuplicarFactura");
		long inicio = System.currentTimeMillis();
		
		SalidaDuplicarFactura salida = new SalidaDuplicarFactura();
		
		try {
			Date fechaSistema = Fecha.toDate(ConfiguracionSistema.getParametroConfiguracionString(conn,in.p_IdEmpresa,"PLATAFORMA","PLA.FechaSistema"));
			ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
			InformacionEmpresa empresa = consultarEmpresaService.logica(conn, in.p_IdEmpresa);
			
			//Busca movimiento base y crea movimiento a duplicar
			MovimientoBase movientoDuplicado = obtenerMovimientoBase(conn,in);
			
			modificarFechasMovimientoBase(conn, fechaSistema, movientoDuplicado, empresa);
			
			//Busca cobros movimiento base
			List<MovimientoComprobanteFormaDePago_Entrada>formaDePagoDuplicado = obtenerFormaPagoMovimientoBase(conn,movientoDuplicado.IdMovimientoComprobante);
		    
			modificarFechaFormasDePago(movientoDuplicado, formaDePagoDuplicado);
			
			// CrearMovimiento Duplicado
			CrearMovimientoComprobante_Salida movimientoDuplicado = crearMovimiendoAfacturar(conn,in,salida,formaDePagoDuplicado,movientoDuplicado);			
			
			salida.P_OUT_ERRORESCONTADOR=movimientoDuplicado.P_OUT_ERRORESCONTADOR;
			salida.P_OUT_MENSAJES     = movimientoDuplicado.P_OUT_MENSAJES;
			salida.p_out_Comprobantes = movimientoDuplicado.p_out_Comprobantes;
			salida.p_out_Movimientos  = movimientoDuplicado.p_out_Movimientos;				
		
			
		} catch (Exception e) {						
			salida.P_OUT_ERRORESCONTADOR=1L;
			salida.P_OUT_MENSAJES.add(new MensajeSalida( 0L, MensajeSalida.MENSAJE_ERROR, "UI_ERROR_AL_DUPLICAR", "Error al duplicar factura"));
			logger.error("FIN::DuplicarFactura {}", e);
		}
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::DuplicarFactura {}", fin - inicio);
		return salida;
	}


	private List<MovimientoComprobanteFormaDePago_Entrada> obtenerFormaPagoMovimientoBase(Connection conn,Long idMovimientoComprobante)  throws Exception {
		logger.debug("INICIO::DuplicarFactura.obtenerFormaPagoMovimientoBase");
		long inicio = System.currentTimeMillis();	
		SQL sql = new SQL();
		Long correlativoFormaPago = 0L;
		HashMap <String,Object> params = new HashMap<String,Object>(2);	
		params.put("IdMovimientoComprobante",idMovimientoComprobante);
		List<MovimientoComprobanteFormaDePago_Entrada> listaFormaPagoMovimiento =  sql.query(conn, SQL_SELECT_FORMAS_PAGO_MOVIMIENTO, params, MovimientoComprobanteFormaDePago_Entrada.class);	
		
		
		for (MovimientoComprobanteFormaDePago_Entrada formaPago : listaFormaPagoMovimiento) {
			formaPago.NumeroMvtoCmbte = 0L;
			formaPago.NumeroMvtoCmbteFormaDePago =correlativoFormaPago;
			correlativoFormaPago= correlativoFormaPago+1L;
		}


		long fin = System.currentTimeMillis();
		logger.debug("FIN::DuplicarFactura.obtenerFormaPagoMovimientoBase {}", fin - inicio);		
		return listaFormaPagoMovimiento;
	}


	private MovimientoBase obtenerMovimientoBase(Connection conn, EntradaDuplicarFactura entrada) throws Exception {
		logger.debug("INICIO::DuplicarFactura.obtenerMovimientoBase");
		long inicio = System.currentTimeMillis();		
		SQL sql = new SQL();
		HashMap <String,Object> params = new HashMap<String,Object>(2);	
		params.put("IdComprobanteBase", entrada.IdComprobanteBase);
		List<MovimientoBase> listaMovimientoFacturacion =  sql.query(conn, SQL_SELECT_MOVIMIENTO_FACTURAR, params, MovimientoBase.class);
		listaMovimientoFacturacion.get(0).NumeroMvtoCmbte=0L;
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::DuplicarFactura.obtenerMovimientoBase {}", fin - inicio);
		return listaMovimientoFacturacion.get(0);
	}


	private CrearMovimientoComprobante_Salida crearMovimiendoAfacturar(Connection conn, EntradaDuplicarFactura in,SalidaDuplicarFactura salida, List<MovimientoComprobanteFormaDePago_Entrada> formaDePagoDuplicado ,MovimientoBase movientoDuplicado ) throws Exception {
		logger.trace("INICIO::DuplicarFactura.crearMovimiendoAfacturar");
		long inicio = System.currentTimeMillis();
		
		CrearMovimientoComprobante movimientoDuplicado = new  CrearMovimientoComprobante();
		CrearMovimientoComprobante_Entrada entradaMovimientoDuplicado = new CrearMovimientoComprobante_Entrada();
		entradaMovimientoDuplicado.p_IdEmpresa= in.p_IdEmpresa;
		entradaMovimientoDuplicado.p_IdUsuario= in.p_IdUsuario;
		entradaMovimientoDuplicado.p_IdModulo = in.p_IdModulo;
		entradaMovimientoDuplicado.p_EsIngresoAutomatico= "N";
		entradaMovimientoDuplicado.p_Movimientos.add(movientoDuplicado);
		entradaMovimientoDuplicado.p_MovimientosCobros.addAll(in.p_MovimientosCobros);
		entradaMovimientoDuplicado.p_MovimientosFormaDePago.addAll(formaDePagoDuplicado);
		
		CrearMovimientoComprobante_Salida salidaMovimientoDuplicado = movimientoDuplicado.logica(conn, entradaMovimientoDuplicado);		
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::DuplicarFactura.crearMovimiendoAfacturar {}", fin - inicio);
		return salidaMovimientoDuplicado;
	}
	
	private void modificarFechasMovimientoBase(Connection conn, Date fechaSistema, MovimientoBase movientoBase, InformacionEmpresa empresa) throws Exception {
		logger.trace("INICIO::DuplicarFactura.modificarFechasMovimientoBase");
		long inicio = System.currentTimeMillis();
		
		Date fechaDefault = Fecha.toDate("0001-01-01");
		String fechaHoraSistemaUTC = FechaDB.ObtenerFechaHoraSistemaUTC(conn, empresa.IdEmpresa);
		
		if(movientoBase != null) {
			if(fechaSistema != null && fechaSistema.compareTo(fechaDefault) != 0) {
				Date fechaLiquidacion = movientoBase.FechaLiquidacion != null ? Fecha.toDate(movientoBase.FechaLiquidacion) : null;
				Date fechaMovimientoComprobante = movientoBase.FechaMovimientoComprobante != null ? Fecha.toDate(movientoBase.FechaMovimientoComprobante.split(" ")[0]): null;
				if (fechaMovimientoComprobante != null && fechaMovimientoComprobante.compareTo(fechaDefault) != 0) {
					if(fechaSistema.after(fechaMovimientoComprobante)) {
						if(fechaLiquidacion != null && fechaLiquidacion.compareTo(fechaDefault) != 0) {	
							if(fechaSistema.after(fechaLiquidacion)) {
								movientoBase.FechaLiquidacion = Fecha.toStringDate(fechaSistema);
								movientoBase.FechaMovimientoComprobante = Fecha.toFormat(fechaSistema, "yyyy-MM-dd HH:mm:ss");
								movientoBase.FechaHoraMovimientoComprobante = fechaHoraSistemaUTC;
								movientoBase.IdCondicionLiquidacion = "T0";
							}else {
								Long dias = 0L;
								OutObtenerPlazo salidaObtenerPlazo = obtenerPlazo(conn, fechaSistema, fechaLiquidacion, empresa.IdPais, movientoBase.IdAsignacion);
								if(salidaObtenerPlazo != null && !salidaObtenerPlazo.p_out_Fechas.isEmpty()) {
									dias = salidaObtenerPlazo.p_out_Fechas.get(0).Plazo;
									CondicionDeLiquidacion condicionDeLiquidacion = obtenerCondicionDeLiquidacion (conn, dias);
									if(condicionDeLiquidacion != null && condicionDeLiquidacion.IdCondicionDeLiquidacion != null) {
										movientoBase.IdCondicionLiquidacion = condicionDeLiquidacion.IdCondicionDeLiquidacion;
										movientoBase.FechaMovimientoComprobante = Fecha.toFormat(fechaSistema, "yyyy-MM-dd HH:mm:ss");
										movientoBase.FechaHoraMovimientoComprobante = fechaHoraSistemaUTC;
									}
								}
							}
						}
					}
				}
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::DuplicarFactura.modificarFechasMovimientoBase: tiempo", fin - inicio);
	}
	
	private CondicionDeLiquidacion obtenerCondicionDeLiquidacion (Connection conn,Long plazo) throws SQLException{
		logger.debug("INICIO::DuplicarFactura.obtenerCondicionDeLiquidacion");
		long inicio = System.currentTimeMillis();
		
		CondicionDeLiquidacion condicionDeLiquidacion = null;
		SQL sql = new SQL();
		
		HashMap <String,Object> params = new HashMap<String,Object>(1);
		params.put("Plazo", plazo);
		List<CondicionDeLiquidacion> listaCondicionDeLiquidacion =  sql.query(conn, SQL_SELECT_CONDICION_LIQUIDACION, params, CondicionDeLiquidacion.class);
		if(listaCondicionDeLiquidacion != null && listaCondicionDeLiquidacion.size() > 0) {
			condicionDeLiquidacion = listaCondicionDeLiquidacion.get(0);
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::DuplicarFactura.obtenerCondicionDeLiquidacion: tiempo", fin - inicio);
		return condicionDeLiquidacion;
	}
	
	private OutObtenerPlazo obtenerPlazo (Connection conn, Date fechaSistema, Date fechaLiquidacion, Long idPais, Long idInterno) throws Exception {
		logger.trace("INICIO::DuplicarFactura.obtenerPlazo");
		long inicio = System.currentTimeMillis();
		
		ObtenerPlazo.InObtenerPlazo entradaObtenerPlazo = new ObtenerPlazo.InObtenerPlazo();
		entradaObtenerPlazo.p_IdPais = idPais;
		ObtenerPlazo.FechasIn f = new ObtenerPlazo.FechasIn();
		f.FechaDesde = Fecha.toStringDate(fechaSistema);
		f.FechaHasta = Fecha.toStringDate(fechaLiquidacion);
		f.IdInterno = idInterno;
		f.SoloDiasHabiles = "S";
		entradaObtenerPlazo.p_Fechas.add(f);
		ObtenerPlazo serviceObtenerPlazo = new ObtenerPlazo();
		
		OutObtenerPlazo salidaObtenerPlazo = serviceObtenerPlazo.logica(conn, entradaObtenerPlazo);
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::DuplicarFactura.obtenerPlazo: tiempo", fin - inicio);
		return salidaObtenerPlazo;
	}
	
	private void modificarFechaFormasDePago (MovimientoBase movientoBase, List<MovimientoComprobanteFormaDePago_Entrada> listaFormasDePago) {
		logger.trace("INICIO::DuplicarFactura.modificarFechaFormasDePago");
		long inicio = System.currentTimeMillis();
		
		if(movientoBase != null) {
			if(listaFormasDePago != null && !listaFormasDePago.isEmpty()) {
				listaFormasDePago.forEach(formaDePago ->{
					if(formaDePago.FechaDocumentoPago != null && !"0001-01-01".equalsIgnoreCase(formaDePago.FechaDocumentoPago)) {
						formaDePago.FechaDocumentoPago = movientoBase.FechaLiquidacion;
					}
				});
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::DuplicarFactura.modificarFechaFormasDePago: tiempo", fin - inicio);
	}
	 
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getBOLSAConnection()) {
			Gson gson = Utils.getGsonBuilder();
			String json = "{\r\n" + 
					"	\"p_IdEmpresa\" : 1,\r\n" + 
					"	\"p_IdUsuario\": 21,\r\n" + 
					"	\"p_IdModulo\":'FAC',\r\n" + 
					"	\"IdComprobanteBase\":452939,\r\n" + 
					"	\"p_MovimientosCobros\" :[\r\n" + 
					"		{\r\n" + 
					"			\"NumeroMvtoCmbte\":0,\r\n" + 
					"			\"NumeroMvtoCmbteCobro\":1,\r\n" + 
					"			\"IdCobro\":'COVA',\r\n" + 
					"			\"IdMonedaCobro\":'CLP',\r\n" + 
					"			\"IdMonedaLiquidacionCobro\":'CLP',\r\n" + 
					"			\"MontoCobro\":292,\r\n" + 
					"			\"MontoMovimientoComprobanteCobro\":292,\r\n" + 
					"			\"MontoLiquidacionCobro\":292,\r\n" + 
					"			\"Porcentaje\":0.01\r\n" + 
					"		},{\r\n" + 
					"			\"NumeroMvtoCmbte\":0,\r\n" + 
					"			\"NumeroMvtoCmbteCobro\":2,\r\n" + 
					"			\"IdCobro\":'DEEX',\r\n" + 
					"			\"IdMonedaCobro\":'CLP',\r\n" + 
					"			\"IdMonedaLiquidacionCobro\":'CLP',\r\n" + 
					"			\"MontoCobro\":0,\r\n" + 
					"			\"MontoMovimientoComprobanteCobro\":0,\r\n" + 
					"			\"MontoLiquidacionCobro\":0,\r\n" + 
					"			\"Porcentaje\":0     \r\n" + 
					"		},{\r\n" + 
					"			\"NumeroMvtoCmbte\":0,\r\n" + 
					"			\"NumeroMvtoCmbteCobro\":3,\r\n" + 
					"			\"IdCobro\":'GFFA',\r\n" + 
					"			\"IdMonedaCobro\":'CLP',\r\n" + 
					"			\"IdMonedaLiquidacionCobro\":'CLP',\r\n" + 
					"			\"MontoCobro\":2000,\r\n" + 
					"			\"MontoMovimientoComprobanteCobro\":2000,\r\n" + 
					"			\"MontoLiquidacionCobro\":2000,\r\n" + 
					"			\"Porcentaje\":0\r\n" + 
					"		},{                                        \r\n" + 
					"			\"NumeroMvtoCmbte\":0,\r\n" + 
					"			\"NumeroMvtoCmbteCobro\":4,\r\n" + 
					"			\"IdCobro\":'IVA',\r\n" + 
					"			\"IdMonedaCobro\":'CLP',\r\n" + 
					"			\"IdMonedaLiquidacionCobro\":'CLP',\r\n" + 
					"			\"MontoCobro\":435,\r\n" + 
					"			\"MontoMovimientoComprobanteCobro\":435,\r\n" + 
					"			\"MontoLiquidacionCobro\":435,\r\n" + 
					"			\"Porcentaje\":19\r\n" + 
					"		}\r\n" + 
					"	]\r\n" + 
					"}";
			EntradaDuplicarFactura Objin = gson.fromJson(json, EntradaDuplicarFactura.class);
			
			DuplicarFactura demo=new DuplicarFactura();				
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			
 			try {
 				SalidaDuplicarFactura ObjOut = demo.logica(con, Objin);
 				System.out.println(ObjOut.toString());
 				con.commit();
 				//con.rollback();
 			}
 			catch(Exception er) {
				er.printStackTrace();	
			}
 			
	    	}
		 	
		System.out.println("== END ==");
	} 
	 

}
