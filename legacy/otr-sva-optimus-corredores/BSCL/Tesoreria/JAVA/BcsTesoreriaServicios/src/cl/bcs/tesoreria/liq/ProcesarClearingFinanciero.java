package cl.bcs.tesoreria.liq;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.tesoreria.CrearIngresoEgreso;
import cl.bcs.tesoreria.CrearIngresoEgreso.CrearIngresoEgreso_Entrada;
import cl.bcs.tesoreria.CrearIngresoEgreso.CrearIngresoEgreso_Salida;
import cl.bcs.tesoreria.CrearIngresoEgreso.FormaDePago_Entrada;
import cl.bcs.tesoreria.CrearIngresoEgreso.IngresoEgresoGenerado_Salida;
import cl.bcs.tesoreria.CrearIngresoEgreso.IngresoEgreso_Entrada;
import cl.bcs.tesoreria.liq.ConsultarClearingFinanciero.ClearingFinancieroResumen;
import cl.bcs.tesoreria.liq.ConsultarClearingFinanciero.EntradaClearingFinanciero;
import cl.bcs.tesoreria.liq.ConsultarClearingFinanciero.SalidaClearingFinanciero;
import cl.bcs.tesoreria.pojo.Camara;
import cl.bcs.tesoreria.pojo.CamaraCompensacion;
import cl.bcs.tesoreria.pojo.ConstantesTesoreria;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ProcesarClearingFinanciero implements JavaCall {
	private static final Logger LOGGER = LogManager.getLogger(ProcesarClearingFinanciero.class);
	
	public String SELECT_CAMARA_COMPENSACION						= loadResourceToString("cl/bcs/tesoreria/sql/ProcesarClearingFinanciero_SelectCamaraDeCompensacion.sql");
	public String SELECT_CAMARAS 									= loadResourceToString("cl/bcs/tesoreria/sql/ProcesarClearingFinanciero_SelectCamaras.sql");
	public String SELECT_PREFERENCIAS_DE_PAGO 						= loadResourceToString("cl/bcs/tesoreria/sql/ProcesarClearingFinanciero_SelectPreferenciasDePago.sql");
	public String UPDATE_TRANSACCIONES 								= loadResourceToString("cl/bcs/tesoreria/sql/ProcesarClearingFinanciero_UpdateTransacciones.sql");
	public String UPDATE_EVENTO_CAPITAL_CLIENTE 					= loadResourceToString("cl/bcs/tesoreria/sql/ProcesarClearingFinanciero_UpdateEventoCapitalCliente.sql");
	public String UPDATE_LIQUIDACIONES								= loadResourceToString("cl/bcs/tesoreria/sql/ProcesarClearingFinanciero_UpdateLiquidaciones.sql");
	public String SELECT_COMPROBANTE								= loadResourceToString("cl/bcs/tesoreria/sql/ProcesarClearingFinanciero_SelectComprobante.sql");
	public String SELECT_PREFERENCIAS_DE_PAGO_PORTAFOLIO_CLEARING 	= loadResourceToString("cl/bcs/tesoreria/sql/ProcesarClearingFinanciero_SelectPreferenciasDePagoPortafolioClearing.sql");
	
	public static class ProcesarClearingFinancieroEntrada{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio; // S/N se usa en caso de servicios anidados.
		public String p_FechaLiquidacion;
		public Long p_IdPais; // pais de la camara de compensacion
		public List<Camara> p_Camaras;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ProcesarClearingFinancieroSalida {
		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR = 0L;
		
		public ProcesarClearingFinancieroSalida() {
			super();
			P_OUT_MENSAJES = new ArrayList<PlataformaUtils.MensajeSalida>();
		}

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	private String getConcepto(Long CodigoGrupoCamara,String localizacionSistema) {
		String concepto = "";
		if (CodigoGrupoCamara.equals(1L) && localizacionSistema.equalsIgnoreCase("CL")) {
			concepto = "CLFIPM";
		}
		if (CodigoGrupoCamara.equals(2L) && localizacionSistema.equalsIgnoreCase("CL")) {
			concepto = "CFPHSM";
		}
		if (CodigoGrupoCamara.equals(3L) && localizacionSistema.equalsIgnoreCase("CL")) {
			concepto = "CLFIRV";
		}
		if (CodigoGrupoCamara.equals(4L) && localizacionSistema.equalsIgnoreCase("CL")) {
			concepto = "CLFIDV";
		}
		if (CodigoGrupoCamara.equals(5L) && localizacionSistema.equalsIgnoreCase("CL")) {
			concepto = "CLFIHS";
		}		
		if (CodigoGrupoCamara.equals(1L) && localizacionSistema.equalsIgnoreCase("PE")) {
			concepto = "CLFIRV";
		}
		if (CodigoGrupoCamara.equals(2L) && localizacionSistema.equalsIgnoreCase("PE")) {
			concepto = "CLFIRF";
		}
		if (CodigoGrupoCamara.equals(3L) && localizacionSistema.equalsIgnoreCase("PE")) {
			concepto = "CLFIRP";
		}
		if (CodigoGrupoCamara.equals(4L) && localizacionSistema.equalsIgnoreCase("PE")) {
			concepto = "CLFIRM";
		}
		return concepto;				
	};
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		
		Gson gson = Utils.getGsonBuilder();
		ProcesarClearingFinancieroEntrada in = gson.fromJson(jin.toString(), ProcesarClearingFinancieroEntrada.class);
		ProcesarClearingFinancieroSalida out = logica(conn, in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
		
	}
	
	public ProcesarClearingFinancieroSalida logica(Connection conn, ProcesarClearingFinancieroEntrada in) throws Exception {
		ProcesarClearingFinancieroSalida salida = new ProcesarClearingFinancieroSalida();
		SQL _sql = new SQL();
		ConstantesTesoreria _c = new ConstantesTesoreria();
		//String codigoCamaraCompensacion = "";
		CamaraCompensacion camara = new CamaraCompensacion();
		SalidaClearingFinanciero conClearingSalida = null; 
		Long id = 0L;
		try {
			LOGGER.info("INICIO  ProcesarClearingFinanciero");
			String localizacionSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.LocalizacionSistema");
			
			HashMap<String, Object> params = new HashMap<String, Object>();
			params.put("IdPaisCamara", in.p_IdPais);
			List<CamaraCompensacion> camarasEntidadLiquidadora = _sql.query(conn, SELECT_CAMARA_COMPENSACION, params, CamaraCompensacion.class);
			List<String> cList = camarasEntidadLiquidadora.stream().map(s -> s.IdCamaraDeCompensacion).collect(Collectors.toList());
			List<CamaraCompensacion> cList1 = camarasEntidadLiquidadora.stream().filter(s -> s.IdPersona == null || s.IdPersona.equals(0L)).collect(Collectors.toList());
			
			//------------------------------------------------------------------------
			//-- OBTIENE PREFERENCIAS DE PAGO DEL CORREDOR PARA CLEARING FINANCIERO
			//------------------------------------------------------------------------
			
			// Parámetros para prefPagoList (solo IdEmpresa)
			HashMap<String, Object> paramsPrefPago = new HashMap<>();
			paramsPrefPago.put("IdEmpresa", in.p_IdEmpresa);
			List<PreferenciaPago> prefPagoList = _sql.query(conn, SELECT_PREFERENCIAS_DE_PAGO, paramsPrefPago, PreferenciaPago.class);
			        
			// Parámetros para prefPagoPortClearing (IdEmpresa + IdPaisCamara)
			HashMap<String, Object> paramsPrefPortClearing = new HashMap<>();
			paramsPrefPortClearing.put("IdEmpresa", in.p_IdEmpresa);
			paramsPrefPortClearing.put("IdPaisCamara", in.p_IdPais);
			List<PreferenciaPagoPortafolioClearing> prefPagoPortClearing = _sql.query(conn, SELECT_PREFERENCIAS_DE_PAGO_PORTAFOLIO_CLEARING, paramsPrefPortClearing, PreferenciaPagoPortafolioClearing.class);
			        
			
			/*for (PreferenciaPago pag : prefPagoList) {
				prefPagoMap.put(pag.IdTipoMovimiento, pag.IdConcepto, pag.IdMoneda, pag);
			}
			*/
			if (cList.isEmpty()) {
				salida.P_OUT_MENSAJES.add(new MensajeSalida(id,MensajeSalida.MENSAJE_ERROR,"EMPRESA_LIQUIDADORA_NO_EXISTE",null,conn,in.p_IdEmpresa));
				id++;
			}
			if (cList1.size() > 0) {
				salida.P_OUT_MENSAJES.add(new MensajeSalida(id,MensajeSalida.MENSAJE_ERROR,"EMPRESA_LIQUIDADORA_SIN_PERSONA_RELACIONADA",null,conn,in.p_IdEmpresa));
				id++;
			}
			if (prefPagoList.isEmpty()) {
				salida.P_OUT_MENSAJES.add(new MensajeSalida(id,MensajeSalida.MENSAJE_ERROR,"PREFERENCIAS_DE_PAGO_CLEARING_NO_CONFIGURADAS",null,conn,in.p_IdEmpresa));
				id++;
			}
			if (MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES) > 0 ) {
				return salida;
			}
			
			List<String> p_CamarasList = in.p_Camaras.stream().map(s -> s.Camara).collect(Collectors.toList());
			params = new HashMap<String, Object>();
			params.put("IdCamaraDeCompensacion", cList);
			params.put("Camaras", p_CamarasList);
			List<Camara> camList = _sql.query(conn, SELECT_CAMARAS, params, Camara.class);
			//validar que las camaras coincidan con las que ingreso.
			
			
			if (localizacionSistema.equalsIgnoreCase("CL")) {
				ConsultarClearingFinanciero conClearing = new ConsultarClearingFinanciero();
				EntradaClearingFinanciero inClearing = new EntradaClearingFinanciero();
				inClearing.p_IdEmpresa = in.p_IdEmpresa;
				inClearing.p_IdUsuario	= in.p_IdUsuario;
				inClearing.p_IdModulo	= in.p_IdModulo;
				inClearing.p_UsaReglasNegocio	= in.p_UsaReglasNegocio;
				inClearing.p_FechaLiquidacion	= in.p_FechaLiquidacion;
				//for (Camara c: camList) {
				inClearing.p_Camaras.addAll(camList);
				//}
				conClearingSalida = conClearing.logica(conn, inClearing);
				
			}else if (localizacionSistema.equalsIgnoreCase("PE")) {
				//hay que migrar el sql de Peru
				/*CALL "BSCL.Tesoreria.BD.Servicios.LIQ::ConsultarClearingFinancieroPE"(
				    	 p_IdEmpresa         				=> :p_IdEmpresa
				    	,p_IdUsuario         				=> :p_IdUsuario
				    	,p_IdModulo          				=> :p_IdModulo
				    	,p_UsaReglasNegocio	 				=> :p_UsaReglasNegocio
						,p_FechaLiquidacion	   				=> :p_FechaLiquidacion
						,p_Camaras           				=> :p_Camaras
						,p_out_ClearingFinancieroResumen  	=> :v_ClearingFinancieroResumen
						,p_out_ClearingFinancieroDetalle  	=> :v_ClearingFinancieroDetalle
						,p_out_AdvertenciasContador 		=> :p_out_AdvertenciasContador
						,p_out_AdvertenciasDetalle  		=> :p_out_AdvertenciasDetalle
						,p_out_ErroresContador      		=> :p_out_ErroresContador
						,p_out_ErroresDetalle       		=> :p_out_ErroresDetalle
				);
				conClearingSalida = conClearingPe.logica(conn, inClearing);
				*/
			}
			salida.P_OUT_MENSAJES.addAll(conClearingSalida.P_OUT_MENSAJES);
			List<ClearingFinancieroResumen> clearingResumenSalida = conClearingSalida.P_OUT_CLEARINGFINANCIERORESUMEN.stream().filter(s->s.IdComprobanteTesoreria.compareTo(0L) < 1).collect(Collectors.toList());
			if (clearingResumenSalida.isEmpty()) {
				salida.P_OUT_MENSAJES.add(new MensajeSalida(id,MensajeSalida.MENSAJE_ERROR,"NO_EXISTEN_MOVIMIENTOS_A_PROCESAR",null,conn,in.p_IdEmpresa));
				id++;
			}
			// Considera solo los movimientos(transacciones y dividendos) no procesados (Segun Sql originales)
			if (MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES) > 0 ) {
				return salida;
			}
						
			//--------------------------------------------------------------
			//-- OBTIENE TOTALES POR CAMARA, MONEDA
			//--------------------------------------------------------------	   
		    /*v_SaldoGrupoCamara = 
		    SELECT
		    	 ROW_NUMBER() over(ORDER BY "CodigoGrupoCamara", "NombreGrupoCamara","IdMonedaTransaccion") AS "NumeroReferencia"  -- Id Referencial
				,"CodigoGrupoCamara"    -- CHILE:   CambiosCCLV(S) -> 3(RV), 1(PM), 2(PH+SM)   -- CambiosCCLV(N) -> 1(RV+PM), 2(PH+SM)
				,"NombreGrupoCamara"
				,"IdMonedaTransaccion" AS "IdMoneda"
		   		,(COALESCE(SUM("TotalVenta"),0)-COALESCE(SUM("TotalCompra"),0)) AS "SaldoCamara"	    
		   	FROM :v_ClearingFinancieroResumen	   		
		    GROUP BY "CodigoGrupoCamara", "NombreGrupoCamara","IdMonedaTransaccion";
		   	*/
			//MultiKeyMap saldoGrupoCamara = new MultiKeyMap();
			//Long idGrupo = 0L;
			/*for(ClearingFinancieroResumen cl: conClearingSalida.p_out_ClearingFinancieroResumen) {
				GrupoCamara clRe = (GrupoCamara) saldoGrupoCamara.get(cl.CodigoGrupoCamara,cl.NombreGrupoCamara,cl.IdMonedaTransaccion);
				if (clRe == null) {
					GrupoCamara grupo = new GrupoCamara();
					grupo.NumeroReferencia  = idGrupo++;
					grupo.CodigoGrupoCamara = cl.CodigoGrupoCamara;
					grupo.NombreGrupoCamara = cl.NombreGrupoCamara;
					grupo.IdMoneda			= cl.IdMonedaTransaccion;
					grupo.SaldoCamara		= cl.TotalVenta.subtract(cl.TotalCompra);
					saldoGrupoCamara.put(cl.CodigoGrupoCamara,cl.NombreGrupoCamara,cl.IdMonedaTransaccion, grupo);
				}
				else {
					clRe.SaldoCamara =clRe.SaldoCamara.add(cl.TotalVenta.subtract(cl.TotalCompra));
				}
				
			}*/
			
			Map<String, List<ClearingFinancieroResumen>> saldoGrupoCamaraHash = clearingResumenSalida.parallelStream().collect(Collectors.groupingBy(cl ->{
				return cl.CodigoGrupoCamara+ "-" +cl.NombreGrupoCamara+ "-" +cl.IdMonedaTransaccion;
			}));
			
			//List<String> grupoOk = new ArrayList<String>();
			HashMap<String, String> hashGrupoOk = new HashMap<>();
			List<GrupoCamara> saldosGrupoCamara = clearingResumenSalida.stream().map(gr->{
				String ref = gr.CodigoGrupoCamara+ "-" +gr.NombreGrupoCamara+ "-" +gr.IdMonedaTransaccion;
				
				GrupoCamara grupo = new GrupoCamara();
				
				if (hashGrupoOk.get(ref) == null) {
					List<ClearingFinancieroResumen> cl = saldoGrupoCamaraHash.get(ref);
					//grupo = new GrupoCamara();
					grupo.NumeroReferencia  = (new Random()).nextLong();
					grupo.CodigoGrupoCamara = gr.CodigoGrupoCamara;
					grupo.NombreGrupoCamara = gr.NombreGrupoCamara;
					grupo.IdMoneda			= gr.IdMonedaTransaccion;
					cl.stream().forEach(c->{
						grupo.SaldoCamara =grupo.SaldoCamara.add(c.TotalVenta.subtract(c.TotalCompra));
					});
					hashGrupoOk.put(ref, ref);
					//grupoOk.add(ref);
				}
				return grupo;
			}).filter(b->b.NumeroReferencia != null && b.SaldoCamara.compareTo(BigDecimal.ZERO) != 0 ).collect(Collectors.toList());
			//filtro los blancos
			//List<GrupoCamara> saldosGrupoCamara = sGrupoCam.stream().filter(b->b.NumeroReferencia != null).collect(Collectors.toList());
			/*
			Map<GrupoCamara,List<ClearingFinancieroResumen>> saldosGrupoCamaraHash = conClearingSalida.p_out_ClearingFinancieroResumen.parallelStream().collect(Collectors.groupingBy(clearing -> {
				GrupoCamara grupo = new GrupoCamara();
				grupo.NumeroReferencia  = idGrupo++;
				grupo.CodigoGrupoCamara = clearing.CodigoGrupoCamara;
				grupo.NombreGrupoCamara = clearing.NombreGrupoCamara;
				grupo.IdMoneda			= clearing.IdMonedaTransaccion;
				grupo.SaldoCamara		= clearing.TotalVenta.subtract(clearing.TotalCompra);
				saldosGrupoCamaraHash.get
				return grupo;
			}));*/
			/*
			List<GrupoCamara> saldosGrupoCamara = conClearingSalida.p_out_ClearingFinancieroResumen.parallelStream().map(gr->{				
				GrupoCamara grupo = new GrupoCamara();
				grupo.NumeroReferencia  = idGrupo++;
				grupo.CodigoGrupoCamara = gr.CodigoGrupoCamara;
				grupo.NombreGrupoCamara = gr.NombreGrupoCamara;
				grupo.IdMoneda			= gr.IdMonedaTransaccion;
				List<ClearingFinancieroResumen> saldosGrupo = saldosGrupoCamaraHash.get(grupo) != null? saldosGrupoCamaraHash.get(gr) : new ArrayList<ClearingFinancieroResumen>();
				//grupo.SaldoCamara 		= null;//grupo.SaldoCamara.add(gr.sa);
				return grupo;
			});*/
			//--------------------------------------------------------------
			//-- GENERAR COMPROBANTES DE TESORERIA
			//--------------------------------------------------------------
			CrearIngresoEgreso ingEgrTeso = new CrearIngresoEgreso();
			CrearIngresoEgreso_Entrada inTeso = new CrearIngresoEgreso_Entrada();
			inTeso.p_IdEmpresa				= in.p_IdEmpresa;
			inTeso.p_IdUsuario				= in.p_IdUsuario;
			inTeso.p_IdModulo				= in.p_IdModulo;
			inTeso.p_SimularOperacion 		= "N";
			inTeso.p_UsaReglasNegocio		= in.p_UsaReglasNegocio;		
			inTeso.p_EsIngresoAutomatico 	= "S";        //S: Ingreso automático   N: Ingreso Manual
			inTeso.p_FechaMovimiento 		= null;   // Fecha de Sistema(si no viene le asigna fecha sistema)  //////// nuevo jv
			try {
				saldosGrupoCamara.stream().forEach(saldo -> {
					IngresoEgreso_Entrada ingresoEgreso = new IngresoEgreso_Entrada();
					ingresoEgreso.NumeroReferencia = saldo.NumeroReferencia;
					if (saldo.SaldoCamara.compareTo(BigDecimal.ZERO) == -1) {
						ingresoEgreso.IdTipoMovimiento = "MOVEGR";
					}else {
						ingresoEgreso.IdTipoMovimiento = "MOVING";
					}
					ingresoEgreso.IdTipoPersona = "CNT";
					ingresoEgreso.IdPersona = camarasEntidadLiquidadora.get(0).IdPersona;
					ingresoEgreso.IdPortafolio = 0L;
					ingresoEgreso.IdMoneda = saldo.IdMoneda;
					ingresoEgreso.Monto	 = saldo.SaldoCamara.abs();
					ingresoEgreso.FechaLiquidacion = in.p_FechaLiquidacion;
					ingresoEgreso.IdConcepto = getConcepto(saldo.CodigoGrupoCamara, localizacionSistema);
					ingresoEgreso.IdTipoPersonaTitularPago = "CNT";
					ingresoEgreso.IdPersonaTitularPago = camarasEntidadLiquidadora.get(0).IdPersona;
					ingresoEgreso.IdPortafolioTitularPago = 0L;
					ingresoEgreso.IdCaja = 0L;
					ingresoEgreso.SecuenciaAgrupacion= "";
					ingresoEgreso.SecuenciaRelacionada = 0L;
					ingresoEgreso.Glosa =    "CLEARING FINANCIERO - CAMARA: " + saldo.NombreGrupoCamara ;			
					ingresoEgreso.IdEmpleadoDigitador = 0L;
					ingresoEgreso.IdSucursalEmpleadoDigitador = 0L;
					ingresoEgreso.IdTipoProducto = "";
					ingresoEgreso.IdTipoOperacion = "";
					ingresoEgreso.IdTipoComprobante = "";
					ingresoEgreso.IdComprobante = 0L;
					ingresoEgreso.FechaComprobante = "0001-01-01";
					ingresoEgreso.FechaLiquidacionComprobante = "0001-01-01";
					ingresoEgreso.IdOperacion = 0L; 
					ingresoEgreso.FechaOperacion = "0001-01-01";
					ingresoEgreso.IdCondicionLiquidacion = "";
					ingresoEgreso.IdBolsa = "";
					ingresoEgreso.IdMonedaOperacion = "";
					ingresoEgreso.MontoOperacion = BigDecimal.ZERO;
					ingresoEgreso.IdTipoReajuste = "";
					ingresoEgreso.FechaParidad = "0001-01-01";
					ingresoEgreso.ValorParidad = BigDecimal.ZERO;
					ingresoEgreso.FolioCartera = 0L;
					ingresoEgreso.IdInstrumento = 0L;
					ingresoEgreso.IdTipoInstrumento = "";
					ingresoEgreso.IdClasificacionTipoInstrumento ="";
					ingresoEgreso.Bilateral = "N";
					ingresoEgreso.GeneraComprobante = "S";
					ingresoEgreso.AgruparEnComprobante = "S";
					ingresoEgreso.MueveCuentaInversion = "S";
					ingresoEgreso.TitularPago = "S";
	           	
					inTeso.p_IngresoEgresoGenerar.add(ingresoEgreso);
				
					PreferenciaPagoPortafolioClearing port = prefPagoPortClearing.stream()
							.filter(q-> q.TipoMovimiento.equalsIgnoreCase(ingresoEgreso.IdTipoMovimiento) 
								&& q.IdMoneda.equalsIgnoreCase(ingresoEgreso.IdMoneda)).findAny().orElse(null);
						
					PreferenciaPago pag = prefPagoList.stream()
							.filter(p-> p.IdTipoMovimiento.equalsIgnoreCase(ingresoEgreso.IdTipoMovimiento) 
									&& p.IdConcepto.equalsIgnoreCase(ingresoEgreso.IdConcepto) 
									&& p.IdMoneda.equalsIgnoreCase(ingresoEgreso.IdMoneda))
							.findAny().orElseThrow(() -> new RuntimeException(						
							        "SIN_PREFERENCIA_PAGO|"+ ingresoEgreso.IdTipoMovimiento + "-" + ingresoEgreso.IdConcepto + "-" + ingresoEgreso.IdMoneda));

					FormaDePago_Entrada formaDePago = new FormaDePago_Entrada();
					formaDePago.NumeroReferencia = ingresoEgreso.NumeroReferencia;
					formaDePago.NumeroReferenciaFormaDePago = ingresoEgreso.NumeroReferencia;
					formaDePago.IdMedioDePago = pag != null ? pag.IdMedioDePago : "";
					formaDePago.IdModalidadDePago = pag != null ? pag.IdModalidadDePago : "";
					formaDePago.IdMoneda = ingresoEgreso.IdMoneda;
					formaDePago.Monto = ingresoEgreso.Monto;				
					formaDePago.IdCuentaBancariaCorredora = pag != null ? pag.IdCuentaBanco : 0L; 
					formaDePago.IdTarjetaBancariaCorredora = 0L;					
					formaDePago.IdChequera = 0L;
					formaDePago.NumeroDocumentoCorredora = 0L;
					formaDePago.FechaDocumentoPagoCorredora = "0001-01-01";				
					formaDePago.IdCuentaBancariaSocio = port != null ? port.IdCuentaBancaria : 0L;
					formaDePago.IdBancoSocio = port != null ? port.IdBancoCuentaBancaria : 0L;
					formaDePago.NombreCuentaBancariaSocio = port != null ? port.NombreBancoCuentaBancaria : "";
					formaDePago.NumeroCuentaBancariaSocio = port != null ? port.NumeroCuentaBancaria : "";
					formaDePago.IdTipoCuentaBancariaSocio = port != null ? port.IdTipoCuentaBancaria : ""; 
					formaDePago.NombreTipoCuentaBancariaSocio = port != null ? port.NombreTipoCuentaBancaria : "";														
					formaDePago.IdTarjetaBancariaSocio = 0L;
					formaDePago.IdTipoTarjetaBancariaSocio = "";
					formaDePago.NombreTipoTarjetaBancariaSocio = "";
					formaDePago.NumeroTarjetaBancariaSocio = "";
					formaDePago.NumeroSerieSocio = "";
					formaDePago.NumeroDocumentoSocio = 0L;
					formaDePago.FechaDocumentoPagoSocio = "0001-01-01";
				
					inTeso.p_FormaDePago.add(formaDePago);
				});
			} catch (RuntimeException e) {
			    if (e.getMessage() != null && e.getMessage().startsWith("SIN_PREFERENCIA_PAGO")) {

			        String claveCompuesta = e.getMessage().contains("|")? e.getMessage().split("\\|")[1]: "DESCONOCIDA";  
			        String codigoMensaje = "UI_SIN_PREFERENCIA_PAGO_CLEARING";
			        String mensajeDescripcion = Diccionario.getMensaje(conn, in.p_IdEmpresa, codigoMensaje).add("Clave", claveCompuesta).msg();
			        salida.P_OUT_MENSAJES.add(new MensajeSalida(null, MensajeSalida.MENSAJE_ERROR, codigoMensaje, mensajeDescripcion));
			        salida.P_OUT_ERRORESCONTADOR = salida.P_OUT_ERRORESCONTADOR + 1L;
			        return salida;
			    }
			    throw e;
			}
			
			CrearIngresoEgreso_Salida ingEgrTesoSalida = ingEgrTeso.logica(conn, inTeso);
			salida.P_OUT_MENSAJES.addAll(ingEgrTesoSalida.P_OUT_MENSAJES);
			
			if (MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES) > 0) {
				return salida;
			}
			//Buscar el IdComprobante generados
			params.clear();
			params.put("IdIngresoEgreso", ingEgrTesoSalida.P_OUT_INGRESOEGRESOGENERADO.stream().map(s->s.IdIngresoEgreso).collect(Collectors.toList()));
			List<IngresoEgresoComprobante> comprobanteTeso = _sql.query(conn, SELECT_COMPROBANTE, params, IngresoEgresoComprobante.class);
			//--------------------------------------------------------------				
			//-- MARCA TRANSACCIONES CON EL FOLIO DEL COMPROBANTE GENERADO 
			//--------------------------------------------------------------
			List<TransaccionUpd> updateTransaccion = conClearingSalida.P_OUT_CLEARINGFINANCIERODETALLE.parallelStream().map(det ->{
				TransaccionUpd traUpd = new TransaccionUpd();
				traUpd.IdTransaccion = det.IdTransaccion;
				
				saldosGrupoCamara.stream().forEach(saldo->{
					IngresoEgresoGenerado_Salida teso = ingEgrTesoSalida.P_OUT_INGRESOEGRESOGENERADO.stream().filter(tes-> tes.NumeroReferencia.equals(saldo.NumeroReferencia)).findAny().get();
					traUpd.IdComprobanteTesoreria =  comprobanteTeso.stream().filter(cmp->cmp.IdIngresoEgreso.equals(teso.IdIngresoEgreso)).map(cm->cm.IdComprobante).findFirst().get();
				});
				
				return traUpd;
			}).collect(Collectors.toList());
			
			_sql.batch(conn, UPDATE_TRANSACCIONES, updateTransaccion, TransaccionUpd.class);			
			//--------------------------------------------------------------
			//-- MARCA DIVIDENDOS CON EL FOLIO DEL COMPROBANTE GENERADO (CHILE)
			//--------------------------------------------------------------
			if (localizacionSistema.equalsIgnoreCase("CL")) {
				List<EventoCapitalClienteUpd> updateEventoCapitalCliente = conClearingSalida.P_OUT_CLEARINGFINANCIERODIVIDENDOS.parallelStream().map(det ->{
					EventoCapitalClienteUpd evcUpd = new EventoCapitalClienteUpd();
					evcUpd.IdEventoCapitalCliente = det.IdEventoCapitalCliente;
					
					saldosGrupoCamara.stream().forEach(saldo->{
						IngresoEgresoGenerado_Salida teso = ingEgrTesoSalida.P_OUT_INGRESOEGRESOGENERADO.stream().filter(tes-> tes.NumeroReferencia.equals(saldo.NumeroReferencia)).findAny().get();
						evcUpd.IdComprobanteTesoreria =  comprobanteTeso.stream().filter(cmp->cmp.IdIngresoEgreso.equals(teso.IdIngresoEgreso)).map(cm->cm.IdComprobante).findFirst().get();
					});
					
					return evcUpd;
				}).collect(Collectors.toList());
				
				_sql.batch(conn, UPDATE_EVENTO_CAPITAL_CLIENTE, updateEventoCapitalCliente, EventoCapitalClienteUpd.class);
				
			}
			//--------------------------------------------------------------				
			//-- MARCA LAS LIQUIDACIONES DE DERIVADOS(FUTUROS(LIQUIDACION DIARIA/AL VENCIMIENTO), OPCIONES(EJERCICIO-SORTEO)) CON EL FOLIO DEL COMPROBANTE GENERADO 
			//--------------------------------------------------------------
			List<LiquidacionUpd> updateLiquidaciones = conClearingSalida.P_OUT_CLEARINGFINANCIEROLIQUIDACIONES.parallelStream().map(det ->{
				LiquidacionUpd liqUpd = new LiquidacionUpd();
				liqUpd.IdCargoAbonoMovimiento = det.IdMovimiento;
				
				saldosGrupoCamara.stream().forEach(saldo->{
					IngresoEgresoGenerado_Salida teso = ingEgrTesoSalida.P_OUT_INGRESOEGRESOGENERADO.stream().filter(tes-> tes.NumeroReferencia.equals(saldo.NumeroReferencia)).findAny().get();
					liqUpd.IdComprobanteTesoreria =  comprobanteTeso.stream().filter(cmp->cmp.IdIngresoEgreso.equals(teso.IdIngresoEgreso)).map(cm->cm.IdComprobante).findFirst().get();
				});
				
				return liqUpd;
			}).collect(Collectors.toList());
			
			_sql.batch(conn, UPDATE_LIQUIDACIONES, updateLiquidaciones, LiquidacionUpd.class);
			
			LOGGER.info("TERMINO ProcesarClearingFinanciero");
		}catch (Exception e) {
			// TODO: handle exception
			LOGGER.error(e);
			e.printStackTrace();
			conn.rollback();
			throw e;
		}finally {
			salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
		}
		return salida;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("master2", "connections.properties")) {   // PlataformaUtils.getBolsaConnection()) {
			Gson gson = Utils.getGsonBuilder();
			//String jsonPE = "{\"p_ConsultarEnrolamiento\":[{\"IdEnrolamiento\":0,\"IdEmpresa\":1,\"IdUsuario\":103,\"IdTipoEntidad\":\"CLIENTE\",\"IdEntidadEnrolada\":0,\"IdTipoDocumentoIdentidad\":\"IUP\",\"IdTipoDocumentoIdentidadAsociado\":\"FIC\",\"DocumentoIdentidad\":\"22000000002620\",\"IdTipoDocumentoIdentidadAdicional\":\"RUT\",\"DocumentoIdentidadAdicional\":\"00000000262\",\"Tipo\":\"J\",\"Nombres\":\"\",\"ApellidoPaterno\":\"\",\"ApellidoMaterno\":\"\",\"RazonSocial\":\"PERSONA JURIDICA FICTICIO\",\"IdPaisNacimiento\":0,\"FechaNacimiento\":\"0001-01-01T03:00:00.000Z\",\"Sexo\":\"M\",\"EstadoCivil\":\"\",\"IdCiudad\":1660024,\"IdDivisionPais\":1661835,\"IdTipoTelefono\":\"FIJO\",\"Telefono\":\"000000000\",\"Email\":\"HOLAHOLA@CHAO.COM\",\"DireccionCalle\":\"DIRECCION\",\"DireccionNumero\":\"666\",\"DireccionDepartamento\":\"\",\"FechaCreacion\":\"2018-06-01T20:14:48.416Z\",\"FechaModificacion\":\"2018-06-01T20:14:48.416Z\",\"IdPersonaConyuge\":0,\"RegimenConyugal\":\"\",\"CLI_IdCategoriaInversionista\":\"AGRESIVO\",\"CLI_IdTipoCliente\":\"CLIENTE AGENCIA\",\"CLI_IdClaseCliente\":\"INTERMEDIARIO\",\"CLI_IdEmpleadoEjecutivo\":271,\"CLI_CodigoAdicionalHolding\":\"\",\"CLI_ClienteFinal\":\"N\",\"POR_IdPerfilComercial\":\"MODELO AGRESIVO 01\",\"POR_IdPersonaBrokerExtranjero\":0,\"EMP_CodigoEmpleado\":\"\",\"EMP_CodigoTipoCargoEmpleado\":\"\",\"EMP_IdEmpleadoSupervisor\":0,\"EMP_IdSucursal\":0,\"EMP_FechaInicioCargo\":\"0001-01-01\",\"EMP_FechaTerminoCargo\":\"0001-01-01\",\"CNT_CodigoInscripcion\":\"\",\"CNT_Emisor\":\"\",\"CNT_Depositos\":\"\",\"CNT_Bolsa\":\"\",\"CNT_Corredora\":\"\",\"CNT_CamaraDeCompensacion\":\"\",\"CNT_Banco\":\"\",\"CNT_Boveda\":\"\",\"CNT_Otros\":\"\",\"PRO_IdAreaProveedor\":0,\"EMP_CargoIndefinido\":\"\",\"EsRutInterno\":\"S\"}],\"p_IdEmpresa\":1,\"p_IdUsuario\":103,\"p_IdModulo\":\"SOC\"}";
			
			// OJO::a futuro deberia venir la entidad liquidadora CCLV y las camaras que quieren proces
			String jsonCL = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":9196,\"p_IdModulo\":\"TES\",\"p_UsaReglasNegocio\":\"S\",\"p_FechaLiquidacion\":\"2026-04-06\",\"p_Camaras\":[{\"Camara\":\"RV\"},{\"Camara\":\"PM\"},{\"Camara\":\"PH\"},{\"Camara\":\"SM\"},{\"Camara\":\"DV\"},{\"Camara\":\"HS\"}],\"p_IdPais\":44}";
			
			ProcesarClearingFinancieroEntrada Objin = gson.fromJson(jsonCL, ProcesarClearingFinancieroEntrada.class);

			ProcesarClearingFinanciero demo = new ProcesarClearingFinanciero();
			System.out.println("ENTRADA");
			System.out.println(Objin.toString());
			
			try {
				ProcesarClearingFinancieroSalida ObjOut = demo.logica(con, Objin);
				con.rollback();
				System.out.println(ObjOut.toString());
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

	public static class PreferenciaPago {
		public String IdTipoMovimiento;
		public String IdMedioDePago;
		public String IdModalidadDePago;
		public Long IdCuentaBanco;
		public String IdMoneda;
		public String IdConcepto;
		
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class PreferenciaPagoPortafolioClearing {
		public String IdPreferenciaPago;
		public Long IdCuentaBancaria;
		public Long IdBancoCuentaBancaria;
		public String NombreBancoCuentaBancaria;
		public String NumeroCuentaBancaria;
		public String IdEstado;
		public String TipoMovimiento;
		public String TipoOperacion;
		public String NombreTipoCuentaBancaria;
		public String IdTipoCuentaBancaria;
		public String IdMedioDePago;
		public String MedioDePago;
		public String IdMoneda;
		public String RecibePagaPortafolio;
		
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class GrupoCamara {
		public Long NumeroReferencia;
		public Long CodigoGrupoCamara;
		public String NombreGrupoCamara;
		public String IdMoneda;
   		public BigDecimal SaldoCamara;
		
		public GrupoCamara() {
			super();
			
			SaldoCamara = BigDecimal.ZERO;
		}
		
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class TransaccionUpd {
		public Long IdTransaccion;
		public Long IdComprobanteTesoreria;
		
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class EventoCapitalClienteUpd {
		public Long IdEventoCapitalCliente;
		public Long IdComprobanteTesoreria;
		
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class LiquidacionUpd {
		public Long IdCargoAbonoMovimiento;
		public Long IdComprobanteTesoreria;
		
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class IngresoEgresoComprobante {
		public Long IdIngresoEgreso;
		public Long IdComprobante;
		
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
}
