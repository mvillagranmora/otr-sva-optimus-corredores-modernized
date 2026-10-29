package cl.bcs.custodia.evc;


import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.cuentaInversion.CrearCargoAbono;
import cl.bcs.cuentaInversion.CrearCargoAbono.CrearCargoAbonoEntrada;
import cl.bcs.cuentaInversion.CrearCargoAbono.CrearCargoAbonoSalida;
import cl.bcs.cuentaInversion.CrearCargoAbono.ObjCargoAbonoAGenerar;
import cl.bcs.ordenes.pojo.IndicadoresPortafolio;
import cl.bcs.plataforma.pla.ConsultarFlujoCaja;
import cl.bcs.plataforma.pla.ConsultarFlujoCaja.EntradaConsultarFlujoCaja;
import cl.bcs.plataforma.pla.ConsultarFlujoCaja.PortafolioAConsultar;
import cl.bcs.plataforma.pla.ConsultarFlujoCaja.SalidaConsultarFlujoCaja;
import cl.bcs.plataforma.pla.SaldoFlujoCaja.ObjFLUJOCAJA;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.tesoreria.CrearIngresoEgreso;
import cl.bcs.tesoreria.CrearIngresoEgreso.CrearIngresoEgreso_Entrada;
import cl.bcs.tesoreria.CrearIngresoEgreso.CrearIngresoEgreso_Salida;
import cl.bcs.tesoreria.CrearIngresoEgreso.FormaDePago_Entrada;
import cl.bcs.tesoreria.CrearIngresoEgreso.IngresoEgreso_Entrada;
import cl.bcs.custodia.evc.NotificarEventoCapital.NotificarEventoCapital_Entrada;
import cl.bcs.custodia.evc.NotificarEventoCapital.NotificarEventoCapital_Salida;
import cl.bcs.custodia.evc.NotificarEventoCapital.ObjEventoCapital_Entrada;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CerrarEventoCapitalRC implements JavaCall{
	
	private static final Logger logger = LogManager.getLogger(CerrarEventoCapitalRC.class);
	
	private final String SQL_SELECT_VALIDACION_CERRAR_CAPITAL   		= loadResourceToString("cl/bcs/custodia/sql/CerrarEventoCapitalDV_SelectValidacionEstado.sql");
	

	private final String SQL_SELECT_EGRESO_INGRESO_TOTAL 				= loadResourceToString("cl/bcs/custodia/sql/CerrarEventoCapitalRC_SelectIngresoEgresoTotal.sql");
	private final String SQL_SELECT_EGRESO_INGRESO_CLIENTE 				= loadResourceToString("cl/bcs/custodia/sql/CerrarEventoCapitalRC_SelectIngresoEgresoCliente.sql");
	private final String SQL_SELECT_FORMAS_PAGO 						= loadResourceToString("cl/bcs/custodia/sql/CerrarEventoCapitalDV_SelectFormasPago.sql");

	

	private final String SQL_SELECT_CARGO_ABONO 						= loadResourceToString("cl/bcs/custodia/sql/CerrarEventoCapitalRC_SelectCargoAbono.sql");	
	private final String SQL_UPDATE_EVENTO_CAPITAL 						= loadResourceToString("cl/bcs/custodia/sql/CerrarEventoCapitalDV_UpdateEventoCapital.sql");
	private final String SQL_UPDATE_EVENTO_CAPITAL_CLIENTE 				= loadResourceToString("cl/bcs/custodia/sql/CerrarEventoCapitalDV_UpdateEventoCapitalCliente.sql");

	private final String SQL_SELECT_INDICADORES_PORTAFOLIO 				= loadResourceToString("cl/bcs/ordenes/sql/Globales_SelectIndicadoresPortafolio.sql");
	
	public static class CerrarEventoCapitalRCEntrada {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public Long p_IdEventoCapital;
		public String p_IdTipoEventoCapital;
		
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	public static class CerrarEventoCapitalRCSalida {
		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;
		
		public CerrarEventoCapitalRCSalida (){
			P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			P_OUT_ERRORESCONTADOR= 0L;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}	
	};
	
	public static class Validacion{
		public Long Contador;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	
	public static class IngresoEgreso{
		public Long 		NumeroReferencia;
		public String 		IdTipoMovimiento;
		public String 		IdTipoPersona;
		public Long			IdPersona;
		public Long 		IdPortafolio;
		public String 		IdMoneda;
		public BigDecimal 	Monto;
		public String 		FechaLiquidacion;
		public String 		IdConcepto;
		public String 		IdTipoPersonaTitularPago;
		public Long 		IdPersonaTitularPago;
		public Long 		IdPortafolioTitularPago;
		public Long 		IdCaja;
		public String 		SecuenciaAgrupacion;
		public Long 		SecuenciaRelacionada;
		public String 		Glosa;
		public Long 		IdEmpleadoDigitador;
		public Long 		IdSucursalEmpleadoDigitador;
		public String 		IdTipoProducto;
		public String 		IdTipoOperacion;
		public String 		IdTipoComprobante;
		public Long 		IdComprobante;
		public String 		FechaComprobante;
		public String 		FechaLiquidacionComprobante;
		public Long 		IdOperacion;
		public String 		FechaOperacion;
		public String 		IdCondicionLiquidacion;
		public String 		IdBolsa;
		public String 		IdBoveda;
		public String 		IdMonedaOperacion = "";
		public BigDecimal 	MontoOperacion;
		public String 		IdTipoReajuste;
		public String 		FechaParidad;
		public BigDecimal 	ValorParidad;
		public Long 		FolioCartera;
		public Long 		IdInstrumento;
		public String 		IdTipoInstrumento;
		public String 		IdClasificacionTipoInstrumento;
		public String 		Bilateral;
		public String 		GeneraComprobante;
		public String 		AgruparEnComprobante;
		public String 		MueveCuentaInversion;
		public String 		TitularPago;
		public BigDecimal 	MontoMenosMontoRetenido = BigDecimal.ZERO;
		public BigDecimal 	MontoBeneficioArchivo = BigDecimal.ZERO;
		public String 		TienePreferenciaDePago = "";
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	public static class FormaPago{
		public Long NumeroReferencia;
		public Long NumeroReferenciaFormaDePago;
		public String IdMedioDePago;
		public String IdModalidadDePago;
		public String IdMoneda;
		public BigDecimal Monto;
		public Long IdCuentaBancariaCorredora;
		public Long IdTarjetaBancariaCorredora;
		public Long IdChequera;
		public Long NumeroDocumentoCorredora;
		public String FechaDocumentoPagoCorredora;
		public Long IdCuentaBancariaSocio;
		public Long IdBancoSocio;
		public String NombreCuentaBancariaSocio;
		public String NumeroCuentaBancariaSocio;
		public String IdTipoCuentaBancariaSocio;
		public String NombreTipoCuentaBancariaSocio;
		public Long IdTarjetaBancariaSocio;
		public String IdTipoTarjetaBancariaSocio;
		public String NombreTipoTarjetaBancariaSocio;
		public String NumeroTarjetaBancariaSocio;
		public String NumeroSerieSocio;
		public Long NumeroDocumentoSocio;
		public String FechaDocumentoPagoSocio;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class CargoAbono{
		public Long 		NumeroCargoAbono;
		public Long 		IdPortafolio;
		public String 		IdMoneda;
		public String 		IdTipoMovimiento;
		public String 		IdConcepto;
		public String 		Descripcion;
		public String 		IdBoveda = "";
		public String 		IdCondicionDeLiquidacion;
		public String 		FechaCondicionDeLiquidacion;
		public BigDecimal 	Monto;
		public Long 		IdEjecutivo;
		public Long 		IdSucursal;
		public String 		IdTipoProducto;
		public String 		IdTipoOperacion;
		public Long 		IdOperacion;
		public String 		FechaOperacion;
		public String 		FechaLiquidacionOperacion;
		public String 		FechaVencimientoOperacion;
		public String 		IdMonedaOperacion;
		public BigDecimal 	MontoOperacion;
		public BigDecimal 	TipoCambioOperacion;
		public Long 		IdContraparte;
		public String 		IdTipoComprobante;
		public Long 		IdComprobante;
		public String 		FolioComprobante;
		public Long 		IdComprobanteDetalle;
		public Long 		IdComprobanteFormaDePago;
		public String 		IdTipoMovimientoAsociado;
		public Long 		IdMovimientoAsociado;
		public String 		FechaComprobante;
		public Long 		IdPersonaPagoCobro;
		public Long 		IdInstrumento;
		public String 		IdTipoInstrumento;
		public String 		Nemo;
		public String 		EsIndicadorBilateral;
		public String 		EsCargoAbonoPorTraspaso;
		public Long 		IdPortafolioTraspaso;
		public String 		GeneroContraparteEnTesoreria;
		public String 		CertificadoAutomatico;
		public BigDecimal 	MontoMenosRetenido 		= BigDecimal.ZERO;
		public BigDecimal 	MontoBeneficioArchivo 	= BigDecimal.ZERO;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class ActualizarEvento{
		public Long IdEventoCapital;
	}
	

	
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		CerrarEventoCapitalRCEntrada jin=gson.fromJson(in.toString() ,CerrarEventoCapitalRCEntrada.class);
		CerrarEventoCapitalRCSalida out=logica(conn,jin);
		
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	};
	
	
	
	
	// -------------------------------------------------------------------
	// LOGICA
	// -------------------------------------------------------------------
	public CerrarEventoCapitalRCSalida logica(Connection con, CerrarEventoCapitalRCEntrada in) throws Exception {
		
		CerrarEventoCapitalRCSalida out = new CerrarEventoCapitalRCSalida();
		
		logger.info(":: INICIO CerrarEventoCapitalRC ::");
		logger.info("ENTRADA > ",in);
		
		out.P_OUT_ERRORESCONTADOR = 0L;
		SQL sql=new SQL();		
		HashMap<String,Object> params = new HashMap<String,Object>();
		
		
		params.clear();
		params.put("IdEventoCapital", in.p_IdEventoCapital);		
		List<Validacion> validaciones = sql.query(con,SQL_SELECT_VALIDACION_CERRAR_CAPITAL,params,Validacion.class);
		Validacion validacion = validaciones.get(0);
		
		if(validacion.Contador > 0){
			
			String fechaSistema 		= ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
			String localizacionSistema 	= ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.LocalizacionSistema");
			String pNombreEmisor		= ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "CUSTODIA", "CUS.NombreEmisor"); //Solo debe estar 'S' en TANNER
			

			
			
			
			// -----------------------------------------------------------------------------------------------------
			// PREPARAR DATA PARA EL ENVIO A CUENTA DE INVERSION (debe ser anterior al envio a tesoreria)			
			// Para todos los clientes se genera un ABONO en cuenta de inversion			
			
			logger.info("Prepara data para envio de abonos a Cuenta Inversion");
			
			List<CargoAbono> cargoAbono = new ArrayList<CargoAbono>();
			List<CargoAbono> cargoAbonoNew = new ArrayList<CargoAbono>();
			params.clear();
			params.put("IdEventoCapital", in.p_IdEventoCapital);
			params.put("FechaSistema", fechaSistema);
			cargoAbono = sql.query(con,SQL_SELECT_CARGO_ABONO,params,CargoAbono.class);
			
			// Consultar flujo caja para recalcular el monto del dividendo en el egreso 
			List<PortafolioAConsultar> portafoliosAConsultar = new ArrayList();
			for(CargoAbono recorre_cargoAbono : cargoAbono){
				PortafolioAConsultar portafolio = new PortafolioAConsultar();
				portafolio.IdMoneda =recorre_cargoAbono.IdMoneda ;
				portafolio.IdPortafolio = recorre_cargoAbono.IdPortafolio;
				portafoliosAConsultar.add(portafolio);
				
			}
			
			
			
			ConsultarFlujoCaja llamadoFlujoCaja = new ConsultarFlujoCaja();
			EntradaConsultarFlujoCaja entradaFlujoCaja = new EntradaConsultarFlujoCaja();
			entradaFlujoCaja.p_IdEmpresa = in.p_IdEmpresa;
			entradaFlujoCaja.p_IdUsuario = in.p_IdUsuario;
			entradaFlujoCaja.p_IdModulo  = in.p_IdModulo;
			entradaFlujoCaja.p_UsaReglasNegocio="S";
			entradaFlujoCaja.p_FechaDesde = fechaSistema;
			entradaFlujoCaja.p_FechaHasta = fechaSistema;
			entradaFlujoCaja.p_Uso="EGRESOS_TESORERIA";
			entradaFlujoCaja.p_PortafolioAConsultar =portafoliosAConsultar;

			logger.info("Inicio Consultar flujo Caja: " + entradaFlujoCaja.toString());
			SalidaConsultarFlujoCaja salidaFlujoCaja = llamadoFlujoCaja.logica(con, entradaFlujoCaja);
			
			List<ObjFLUJOCAJA> flujoCaja = salidaFlujoCaja.P_OUT_FLUJOCAJA;
			//
			if (localizacionSistema.equalsIgnoreCase("PE")) {
				for(CargoAbono recorre_cargoAbono : cargoAbono){
					CargoAbono regcargoAbono = new CargoAbono();
					Utils.copyPojo(recorre_cargoAbono, regcargoAbono);
					if (recorre_cargoAbono.MontoMenosRetenido.compareTo(BigDecimal.ZERO) > 0 && recorre_cargoAbono.IdBoveda.equals("CAV")){ 
						regcargoAbono.Monto = recorre_cargoAbono.MontoMenosRetenido;
						cargoAbonoNew.add(regcargoAbono);
					}else{
						if (recorre_cargoAbono.IdBoveda.equals("CAV")){
							regcargoAbono.Monto = recorre_cargoAbono.MontoBeneficioArchivo;
							cargoAbonoNew.add(regcargoAbono);
						}else{
							cargoAbonoNew.add(regcargoAbono);
						}
					}
				}
			}else{
				for(CargoAbono recorre_cargoAbono : cargoAbono){
					CargoAbono regcargoAbono = new CargoAbono();
					Utils.copyPojo(recorre_cargoAbono, regcargoAbono);
					cargoAbonoNew.add(regcargoAbono);
				}
			}

			logger.info("Data para envio a Cuenta Inversion > " + cargoAbonoNew);
			

			
			if(cargoAbonoNew.size() > 0 ){
				logger.info("Inicio CrearCargoAbono");
				CrearCargoAbonoEntrada entradaCrearCargoAbono = new CrearCargoAbonoEntrada();
				CrearCargoAbono crearCargoAbono = new CrearCargoAbono();
				for(CargoAbono ca : cargoAbonoNew){
					ObjCargoAbonoAGenerar obj = new ObjCargoAbonoAGenerar();
					obj = Utils.copyPojo(ca, obj);
					entradaCrearCargoAbono.p_CargoAbonoAGenerar.add(obj);
				}
				entradaCrearCargoAbono.p_IdEmpresa = in.p_IdEmpresa;
				entradaCrearCargoAbono.p_IdUsuario = in.p_IdUsuario;
				entradaCrearCargoAbono.p_IdModulo = in.p_IdModulo;
				entradaCrearCargoAbono.p_simularOperacion = "N";
				entradaCrearCargoAbono.p_UsaReglasNegocio = "S";
				entradaCrearCargoAbono.p_FechaSistema = fechaSistema;
				entradaCrearCargoAbono.p_EsIngresoManual = "N";
				CrearCargoAbonoSalida salidaCrearCargoAbono = crearCargoAbono.logica(con, entradaCrearCargoAbono);
				
				out.P_OUT_ERRORESCONTADOR +=  salidaCrearCargoAbono.P_OUT_ERRORESCONTADOR != null ? salidaCrearCargoAbono.P_OUT_ERRORESCONTADOR : 0L;
				out.P_OUT_MENSAJES.addAll(salidaCrearCargoAbono.P_OUT_MENSAJES);
				
				
				if (MensajeSalida.getNumeroErrores(salidaCrearCargoAbono.P_OUT_MENSAJES) > 0) {
					logger.debug("ERROR AL CREAR ABONOS EN CUENTA INVERSION");
					return out;
				}
				
				
				logger.info("Fin CrearCargoAbono");
			}

			
			
			
			
			// -----------------------------------------------------------------------------------------------------
			// PREPARAR DATA PARA EL ENVIO A TESORERIA (debe ser porterior al envio a cuenta inversion)			
			
			logger.info("Inicio CrearIngresoEgreso");
			
			List<IngresoEgreso>  ingresoEgresos    = new ArrayList<IngresoEgreso>();
			List<IngresoEgreso>  ingresoEgresosNew = new ArrayList<IngresoEgreso>();
			
			logger.info("Prepara data para envio a tesoreria");
			
			// Genera movimiento de INGRESO a tesoreria por el total
			HashMap<String,Object> paramsExtras = new HashMap<String,Object>();
			paramsExtras.put("IdEventoCapital", in.p_IdEventoCapital);
			paramsExtras.put("pNombreEmisor", 	pNombreEmisor);
			paramsExtras.put("FechaSistema", fechaSistema);
			ingresoEgresos.addAll(sql.query(con,SQL_SELECT_EGRESO_INGRESO_TOTAL,paramsExtras,IngresoEgreso.class));
			
			AtomicLong idInterno = new AtomicLong(-1L);
			ingresoEgresos.parallelStream().forEach(ingresoEgresoTotal->{
				ingresoEgresoTotal.NumeroReferencia = idInterno.getAndDecrement();
			});
			
			
			// Genera movimiento de EGRESO a tesoreria  por cada cliente que NO opere con cuenta de inversion			
			params.clear();
			params.put("IdEventoCapital", in.p_IdEventoCapital);
			params.put("FechaSistema", fechaSistema);
			ingresoEgresos.addAll(sql.query(con,SQL_SELECT_EGRESO_INGRESO_CLIENTE,params,IngresoEgreso.class));
			
			String pPagoNeto		= ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "TESORERIA", "RECALCULARMONTOEGRESO");
			List< Long>  IdEventosCapitalNoGenerados 	= new ArrayList<Long>();
			
			if (localizacionSistema.equalsIgnoreCase("PE")) {
				for(IngresoEgreso recorre_IngresoEgreso : ingresoEgresos){
					IngresoEgreso regIngresoEgreso = new IngresoEgreso();
					Utils.copyPojo(recorre_IngresoEgreso, regIngresoEgreso);
					if ((recorre_IngresoEgreso.MontoMenosMontoRetenido.compareTo(BigDecimal.ZERO) > 0) && (recorre_IngresoEgreso.IdBoveda.equals("CAV"))){
						regIngresoEgreso.Monto = recorre_IngresoEgreso.MontoMenosMontoRetenido;
						if (recorre_IngresoEgreso.TienePreferenciaDePago.equals("S")){		// SOLO DEBE ENVIAR A TESORERIA LOS QUE TENGAN FORMA DE PAGO
							ingresoEgresosNew.add(regIngresoEgreso);
						}
					}else{
						if (recorre_IngresoEgreso.IdBoveda.equals("CAV")){
							regIngresoEgreso.Monto = recorre_IngresoEgreso.MontoBeneficioArchivo;
							if (recorre_IngresoEgreso.TienePreferenciaDePago.equals("S")){	// SOLO DEBE ENVIAR A TESORERIA LOS QUE TENGAN FORMA DE PAGO
								ingresoEgresosNew.add(regIngresoEgreso);
							}
						}else{
							ingresoEgresosNew.add(regIngresoEgreso);
						}
					}
				}
			}else{
				for(IngresoEgreso recorre_IngresoEgreso : ingresoEgresos){
					IngresoEgreso regIngresoEgreso = new IngresoEgreso();
					Utils.copyPojo(recorre_IngresoEgreso, regIngresoEgreso);
					if(pPagoNeto.equalsIgnoreCase("S")&& recorre_IngresoEgreso.IdTipoMovimiento.equals("MOVEGR")){
						
						HashMap<String,Object> paramsPortafolios = new HashMap<>();
						paramsPortafolios.put("idsPortafolio", regIngresoEgreso.IdPortafolio);
						List<IndicadoresPortafolio> _indicadoresPortafolios = sql.query(con, SQL_SELECT_INDICADORES_PORTAFOLIO, paramsPortafolios, IndicadoresPortafolio.class);

						for (IndicadoresPortafolio indicador : _indicadoresPortafolios) {
							
							if(indicador.OperaCuentaInversion.equals("S") && indicador.IdPortafolio.equals(regIngresoEgreso.IdPortafolio)) {
								List<ObjFLUJOCAJA> flujoCajaDetalle = flujoCaja;
								flujoCajaDetalle = flujoCaja.stream()
										.filter(s -> s.NombreUso.equals("EGRESOS_TESORERIA") && s.IdPortafolio.equals(regIngresoEgreso.IdPortafolio) && s.IdMoneda.equals(regIngresoEgreso.IdMoneda))
										.collect(Collectors.toList());
								
								for (ObjFLUJOCAJA flujo : flujoCajaDetalle) {
									
									//CASO 1 SI MPD>FCET Y FCET<=0 ENTONCES MONTO EGRESO = MPD-FCET
									if((regIngresoEgreso.Monto.compareTo(flujo.Saldo.abs())>0) && flujo.Saldo.compareTo(BigDecimal.ZERO)<=0) {
										regIngresoEgreso.Monto=regIngresoEgreso.Monto.subtract(flujo.Saldo.abs());
										ingresoEgresosNew.add(regIngresoEgreso);
									}else {
										if((regIngresoEgreso.Monto.compareTo(flujo.Saldo.abs())<0) && flujo.Saldo.compareTo(BigDecimal.ZERO)<=0) {
											//BORRAR EGRESO
											// CASO 3 SI MPD<FCET Y FCET<=0 ENTONCES NO SE GENERA EGRESO
											IdEventosCapitalNoGenerados.add(regIngresoEgreso.NumeroReferencia);
											ingresoEgresosNew.remove(regIngresoEgreso);
										}else {
											// CASO 2 SI FCET>0 ENTONCES MONTO EGRESO = MPD
											ingresoEgresosNew.add(regIngresoEgreso);
										}
									}
									
									
								}
							}else {
								ingresoEgresosNew.add(regIngresoEgreso);
							}
						}
					}else {	
						ingresoEgresosNew.add(regIngresoEgreso);
					}
				}
			}
				
			// FORMAS DE PAGO.
			params.clear();
			params.put("IdEventoCapital", in.p_IdEventoCapital);
			List<FormaPago> formapagos = sql.query(con,SQL_SELECT_FORMAS_PAGO,params,FormaPago.class);
			

			logger.info("Data para envio a Tesoreria > " + ingresoEgresosNew);
			
			Map<Long, BigDecimal> preciosActualizados = new HashMap();
			
			if(ingresoEgresosNew.size() > 0){
				CrearIngresoEgreso crearIngresoEgreso = new CrearIngresoEgreso();
				CrearIngresoEgreso_Entrada entradacrearIngresoEgreso = new CrearIngresoEgreso_Entrada();
				for(IngresoEgreso ie : ingresoEgresosNew){
					IngresoEgreso_Entrada ieEntrada = new IngresoEgreso_Entrada();
					ieEntrada = Utils.copyPojo(ie, ieEntrada);
					entradacrearIngresoEgreso.p_IngresoEgresoGenerar.add(ieEntrada);
					
					preciosActualizados.put(ieEntrada.NumeroReferencia, ieEntrada.Monto); 
				}
				for(FormaPago formaPago : formapagos){
					FormaDePago_Entrada fp = new FormaDePago_Entrada();
					fp = Utils.copyPojo(formaPago, fp);
					if(pPagoNeto.equalsIgnoreCase("S")){
						fp.Monto = preciosActualizados.get(fp.NumeroReferencia);
						if(IdEventosCapitalNoGenerados.contains(fp.NumeroReferencia)) {
							entradacrearIngresoEgreso.p_FormaDePago.remove(fp); 
						}else {
							entradacrearIngresoEgreso.p_FormaDePago.add(fp);
						}
						
					}else {
						entradacrearIngresoEgreso.p_FormaDePago.add(fp);
					}
				}
				entradacrearIngresoEgreso.p_IdEmpresa = in.p_IdEmpresa;
				entradacrearIngresoEgreso.p_IdUsuario = in.p_IdUsuario;
				entradacrearIngresoEgreso.p_IdModulo = in.p_IdModulo;
				entradacrearIngresoEgreso.p_SimularOperacion = "N";
				entradacrearIngresoEgreso.p_UsaReglasNegocio = "S";
				entradacrearIngresoEgreso.p_EsIngresoAutomatico = "S";
				CrearIngresoEgreso_Salida salidaIngresoEgreso = crearIngresoEgreso.logica(con, entradacrearIngresoEgreso);
				out.P_OUT_ERRORESCONTADOR +=  salidaIngresoEgreso.P_OUT_ERRORESCONTADOR != null ? salidaIngresoEgreso.P_OUT_ERRORESCONTADOR : 0L;
				out.P_OUT_MENSAJES.addAll(salidaIngresoEgreso.P_OUT_MENSAJES);
				
				
				if (MensajeSalida.getNumeroErrores(salidaIngresoEgreso.P_OUT_MENSAJES) > 0) {
					logger.debug("ERROR AL CREAR MOVIMIENTOS DE PAGO EN TESORERIA");
					return out;
				}
				
			}

			logger.info("Fin CrearIngresoEgreso");

			// -----------------------------------------------------------------------------------------------------
			// ACTUALIZAR ESTADO DE EVENTO CAPITAL
			
			ActualizarEvento evento = new ActualizarEvento();
			evento.IdEventoCapital = in.p_IdEventoCapital;
			List<ActualizarEvento> actualizar = new ArrayList<ActualizarEvento>();
			actualizar.add(evento);
			
			logger.info("Actualiza estado de evento capital");
			
			sql.batch(con, SQL_UPDATE_EVENTO_CAPITAL, actualizar, ActualizarEvento.class);			
			sql.batch(con, SQL_UPDATE_EVENTO_CAPITAL_CLIENTE, actualizar, ActualizarEvento.class);
			
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_OK, "UI_EXITO", new HashMap<String,String>() ,con,in.p_IdEmpresa));

		}else{
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR, "UI_EVENTO_VIGENTE_CERRAR", new HashMap<String,String>() ,con,in.p_IdEmpresa));
		}
		
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		
		if(out.P_OUT_ERRORESCONTADOR==0) {
			NotificarEventoCapital notificarEventoCierre = new NotificarEventoCapital();	
			NotificarEventoCapital_Entrada NotifIN = new NotificarEventoCapital_Entrada();
			NotifIN.p_IdUsuario = in.p_IdUsuario;
			NotifIN.p_IdEmpresa = in.p_IdEmpresa;
			NotifIN.p_IdModulo = in.p_IdModulo;
			NotifIN.p_EstadoEventoCapital = "CER";
			ObjEventoCapital_Entrada objIdsCapital = new ObjEventoCapital_Entrada();
			objIdsCapital.p_IdEventoCapital=in.p_IdEventoCapital;
			NotifIN.p_IdsEventoCapital.add(objIdsCapital);
			
			try {
				NotificarEventoCapital_Salida ObjOut= notificarEventoCierre.logica(con, NotifIN);
				List<MensajeSalida> mensajeAdvertencia = ObjOut.P_OUT_MENSAJES.stream().filter(s -> s.tipo.equalsIgnoreCase("ADVERTENCIA")).collect(Collectors.toList());	
				if(ObjOut.P_OUT_ERRORESCONTADOR>0) {
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ADVERTENCIA,"UI_NOTIFICACION_EVENTO_CAPITAL_NO_SE_REALIZO",new HashMap<String,String>(),con,in.p_IdEmpresa) );								
				}else if(mensajeAdvertencia.size()>0) {
					out.P_OUT_MENSAJES=ObjOut.P_OUT_MENSAJES.stream().filter(s -> s.tipo.equalsIgnoreCase("ADVERTENCIA")).collect(Collectors.toList());	
				}
			}
			catch(Exception e) {				
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ADVERTENCIA,"UI_NOTIFICACION_EVENTO_CAPITAL_NO_SE_REALIZO",new HashMap<String,String>(),con,in.p_IdEmpresa) );				
			}
		}
				
		logger.info("SALIDA",out.P_OUT_MENSAJES);
		return out;
	}
	
	
	// -------------------------------------------------------------------
	// METODO PARA PRUEBAS LOCALES
	// --------------------------------------------------------------------
	public static void main(String[] args) throws Exception {		
		try (Connection con = PlataformaUtils.getBOLSAConnection()) {
			try {
				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();				//
				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":1020,\"p_IdModulo\":\"CUS\",\"p_IdEventoCapital\":6804995,\"p_IdTipoEventoCapital\":\"RC\"}";
				CerrarEventoCapitalRCEntrada Objin = gson.fromJson(json, CerrarEventoCapitalRCEntrada.class);
								
				CerrarEventoCapitalRC  kk = new CerrarEventoCapitalRC();
				System.out.println(kk.logica(con, Objin));
				con.commit();
				//con.rollback();
				
		    	}catch(Exception er) {
					er.printStackTrace();	
					con.rollback();
				}
		} catch (Exception e) {
			e.printStackTrace();			
		}
	};
	
}
