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
import cl.bcs.custodia.IngresarGarantiaJava;
import cl.bcs.custodia.IngresarGarantiaJava.IngresarGarantiaEntrada;
import cl.bcs.custodia.IngresarGarantiaJava.IngresarGarantiaSalida;
import cl.bcs.custodia.IngresarGarantiaJava.ObjGarantiaCuentaInversion;
import cl.bcs.custodia.evc.NotificarEventoCapital.NotificarEventoCapital_Entrada;
import cl.bcs.custodia.evc.NotificarEventoCapital.NotificarEventoCapital_Salida;
import cl.bcs.custodia.evc.NotificarEventoCapital.ObjEventoCapital_Entrada;
import cl.bcs.custodia.evc.ProcesarRetencionIsfut.ProcesarRetencionIsfut_Entrada;
import cl.bcs.custodia.evc.ProcesarRetencionIsfut.ProcesarRetencionIsfut_Salida;
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
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CerrarEventoCapitalDV implements JavaCall{
	
	private static final Logger logger = LogManager.getLogger(CerrarEventoCapitalDV.class);
	
	private final String SQL_SELECT_VALIDACION_CERRAR_CAPITAL_DV 		= loadResourceToString("cl/bcs/custodia/sql/CerrarEventoCapitalDV_SelectValidacionEstado.sql");
	private final String SQL_SELECT_EGRESO_INGRESO_TOTAL 				= loadResourceToString("cl/bcs/custodia/sql/CerrarEventoCapitalDV_SelectIngresoEgresoTotal.sql");
	private final String SQL_SELECT_EGRESO_INGRESO_CLIENTE 				= loadResourceToString("cl/bcs/custodia/sql/CerrarEventoCapitalDV_SelectIngresoEgresoCliente.sql");
	private final String SQL_SELECT_FORMAS_PAGO 						= loadResourceToString("cl/bcs/custodia/sql/CerrarEventoCapitalDV_SelectFormasPago.sql");
	private final String SQL_SELECT_CARGO_ABONO 						= loadResourceToString("cl/bcs/custodia/sql/CerrarEventoCapitalDV_SelectCargoAbono.sql");
	private final String SQL_UPDATE_EVENTO_CAPITAL 						= loadResourceToString("cl/bcs/custodia/sql/CerrarEventoCapitalDV_UpdateEventoCapital.sql");
	private final String SQL_UPDATE_EVENTO_CAPITAL_CLIENTE 				= loadResourceToString("cl/bcs/custodia/sql/CerrarEventoCapitalDV_UpdateEventoCapitalCliente.sql");
	private final String SQL_SELECT_GARANTIA 							= loadResourceToString("cl/bcs/custodia/sql/CerrarEventoCapitalDV_SelectGarantia.sql");
	private final String SQL_SELECT_CONTRAPARTE_GARANTIA				= loadResourceToString("cl/bcs/custodia/sql/CerrarEventoCapitalDV_SelectContraparteGarantia.sql");
	
	private final String SQL_SELECT_INDICADORES_PORTAFOLIO 				= loadResourceToString("cl/bcs/ordenes/sql/Globales_SelectIndicadoresPortafolio.sql");
	

	public static class CerrarEventoCapitalDVEntrada {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public Long p_IdEventoCapital;
		public String p_IdTipoEventoCapital;
		
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	public static class CerrarEventoCapitalDVSalida {
		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;
		
		public CerrarEventoCapitalDVSalida (){
			P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			P_OUT_ERRORESCONTADOR = 0L;
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
		public Long NumeroReferencia;
		public String IdTipoMovimiento;
		public String IdTipoPersona;
		public Long	IdPersona;
		public Long IdPortafolio;
		public String IdMoneda;
		public BigDecimal Monto;
		public String FechaLiquidacion;
		public String IdConcepto;
		public String IdTipoPersonaTitularPago;
		public Long IdPersonaTitularPago;
		public Long IdPortafolioTitularPago;
		public Long IdCaja;
		public String SecuenciaAgrupacion;
		public Long SecuenciaRelacionada;
		public String Glosa;
		public Long IdEmpleadoDigitador;
		public Long IdSucursalEmpleadoDigitador;
		public String IdTipoProducto;
		public String IdTipoOperacion;
		public String IdTipoComprobante;
		public Long IdComprobante;
		public String FechaComprobante;
		public String FechaLiquidacionComprobante;
		public Long IdOperacion;
		public String FechaOperacion;
		public String IdCondicionLiquidacion;
		public String IdBolsa;
		public String IdMonedaOperacion;
		public BigDecimal MontoOperacion;
		public String IdTipoReajuste;
		public String FechaParidad;
		public BigDecimal ValorParidad;
		public Long FolioCartera;
		public Long IdInstrumento;
		public String IdTipoInstrumento;
		public String IdClasificacionTipoInstrumento;
		public String Bilateral;
		public String GeneraComprobante;
		public String AgruparEnComprobante;
		public String MueveCuentaInversion;
		public String TitularPago;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class _IngresoEgreso{
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
		public String 		IdMonedaOperacion;
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
		public BigDecimal 	MontoMenosMontoRetenido;
		public BigDecimal 	MontoBeneficioArchivo;
		public String 		TienePreferenciaDePago;
		
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
		public Long NumeroCargoAbono;
		public Long IdPortafolio;
		public String IdMoneda;
		public String IdTipoMovimiento;
		public String IdConcepto;
		public String Descripcion;
		public String IdCondicionDeLiquidacion;
		public String FechaCondicionDeLiquidacion;
		public BigDecimal Monto;
		public Long IdEjecutivo;
		public Long IdSucursal;
		public String IdTipoProducto;
		public String IdTipoOperacion;
		public Long IdOperacion;
		public String FechaOperacion;
		public String FechaLiquidacionOperacion;
		public String FechaVencimientoOperacion;
		public String IdMonedaOperacion;
		public BigDecimal MontoOperacion;
		public BigDecimal TipoCambioOperacion;
		public Long IdContraparte;
		public String IdTipoComprobante;
		public Long IdComprobante;
		public String FolioComprobante;
		public Long IdComprobanteDetalle;
		public Long IdComprobanteFormaDePago;
		public String IdTipoMovimientoAsociado;
		public Long IdMovimientoAsociado;
		public String FechaComprobante;
		public Long IdPersonaPagoCobro;
		public Long IdInstrumento;
		public String IdTipoInstrumento;
		public String Nemo;
		public String EsIndicadorBilateral;
		public String EsCargoAbonoPorTraspaso;
		public Long IdPortafolioTraspaso;
		public String GeneroContraparteEnTesoreria;
		public String CertificadoAutomatico;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class _CargoAbono{
		public Long NumeroCargoAbono;
		public Long IdPortafolio;
		public String IdMoneda;
		public String IdTipoMovimiento;
		public String IdConcepto;
		public String Descripcion;
		public String IdBoveda;
		public String IdCondicionDeLiquidacion;
		public String FechaCondicionDeLiquidacion;
		public BigDecimal Monto;
		public Long IdEjecutivo;
		public Long IdSucursal;
		public String IdTipoProducto;
		public String IdTipoOperacion;
		public Long IdOperacion;
		public String FechaOperacion;
		public String FechaLiquidacionOperacion;
		public String FechaVencimientoOperacion;
		public String IdMonedaOperacion;
		public BigDecimal MontoOperacion;
		public BigDecimal TipoCambioOperacion;
		public Long IdContraparte;
		public String IdTipoComprobante;
		public Long IdComprobante;
		public String FolioComprobante;
		public Long IdComprobanteDetalle;
		public Long IdComprobanteFormaDePago;
		public String IdTipoMovimientoAsociado;
		public Long IdMovimientoAsociado;
		public String FechaComprobante;
		public Long IdPersonaPagoCobro;
		public Long IdInstrumento;
		public String IdTipoInstrumento;
		public String Nemo;
		public String EsIndicadorBilateral;
		public String EsCargoAbonoPorTraspaso;
		public Long IdPortafolioTraspaso;
		public String GeneroContraparteEnTesoreria;
		public String CertificadoAutomatico;
		public BigDecimal MontoMenosRetenido;
		public BigDecimal MontoBeneficioArchivo;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class Garantia{
		public String 		IdMoneda; 
		public BigDecimal 	MontoMonedaInstrumento;
		public BigDecimal 	MontoMonedaSistema;
		public Long 		IdCliente;
		public Long 		IdPortafolio;
		public String 		IdTipoInstrumento;
		public String 		IdBolsa;								
		public Long 		IdInstrumentoGarantizado;
		public String 		IdConcepto;
		public Long 		IdContraparteGarantia;
		public String 		IdTipoOperacion;
		public Long 		IdOperacion;
		public BigDecimal 	MontoMenosMontoRetenido;
		public BigDecimal 	MontoBeneficioArchivo;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class Contraparte{
		public Long IdContraparte;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ActualizarEvento{
		public Long IdEventoCapital;
	}
	
	public static class Errores{
		public String Codigo;
		public String Descripcion;
	}
	
	public static class Advertencias{
		public String Codigo;
		public String RequiereAutorizacion;
		public String Descripcion;
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		CerrarEventoCapitalDVEntrada jin=gson.fromJson(in.toString() ,CerrarEventoCapitalDVEntrada.class);
		CerrarEventoCapitalDVSalida out=logica(conn,jin);
		
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	};
	
	
	
	public CerrarEventoCapitalDVSalida logica(Connection con, CerrarEventoCapitalDVEntrada in) throws Exception {
		
		CerrarEventoCapitalDVSalida out = new CerrarEventoCapitalDVSalida();
		
		out.P_OUT_ERRORESCONTADOR = 0L;
		logger.info("Se inicia con los siguientes parametros",in);

		SQL sql=new SQL();
		
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("IdEventoCapital", in.p_IdEventoCapital);
		
		List<Validacion> validaciones = sql.query(con,SQL_SELECT_VALIDACION_CERRAR_CAPITAL_DV,params,Validacion.class);
		Validacion validacion = validaciones.get(0);
		//validacion modificada
		if(validacion.Contador > 0){
			
			String fechaSistema 		= ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
			String localizacionSistema 	= ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.LocalizacionSistema");
			String pNombreEmisor		= ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "CUSTODIA", "CUS.NombreEmisor"); //Solo debe estar 'S' en TANNER
			
			List<IngresoEgreso>  ingresoEgresos 	= new ArrayList<IngresoEgreso>();
			List<_IngresoEgreso> _ingresoEgresos 	= new ArrayList<_IngresoEgreso>();
			
			
			
			// -----------------------------------------------------------------------------------------------------
			// 1. PREPARAR DATA GENERAR ABONOS EN CUENTA DE INVERSION (debe ser anterior al envio a tesoreria)
			
			
			// Para todos los clientes se genera un ABONO en cuenta de inversion			
			List<CargoAbono> cargoAbono = new ArrayList<CargoAbono>();
			List<_CargoAbono> _cargoAbono = new ArrayList<_CargoAbono>();
			params.clear();
			params.put("IdEventoCapital", in.p_IdEventoCapital);
			params.put("FechaSistema", fechaSistema);
			_cargoAbono = sql.query(con,SQL_SELECT_CARGO_ABONO,params,_CargoAbono.class);
			
			
			// Consultar flujo caja para recalcular el monto del dividendo en el egreso 
			List<PortafolioAConsultar> portafoliosAConsultar = new ArrayList();
			for(_CargoAbono recorre_SaldocargoAbono : _cargoAbono){
				PortafolioAConsultar portafolio = new PortafolioAConsultar();
				portafolio.IdMoneda =recorre_SaldocargoAbono.IdMoneda ;
				portafolio.IdPortafolio = recorre_SaldocargoAbono.IdPortafolio;
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
				for(_CargoAbono recorre_cargoAbono : _cargoAbono){
					CargoAbono regcargoAbono = new CargoAbono();
					Utils.copyPojo(recorre_cargoAbono, regcargoAbono);
					if (recorre_cargoAbono.MontoMenosRetenido.compareTo(BigDecimal.ZERO) > 0 && recorre_cargoAbono.IdBoveda.equals("CAV")){ 
						regcargoAbono.Monto = recorre_cargoAbono.MontoMenosRetenido;
						cargoAbono.add(regcargoAbono);
					}else{
						if (recorre_cargoAbono.IdBoveda.equals("CAV")){
							regcargoAbono.Monto = recorre_cargoAbono.MontoBeneficioArchivo;
							cargoAbono.add(regcargoAbono);
						}else{
							cargoAbono.add(regcargoAbono);
						}
					}
				}
			}else{
				for(_CargoAbono recorre_cargoAbono : _cargoAbono){
					CargoAbono regcargoAbono = new CargoAbono();
					Utils.copyPojo(recorre_cargoAbono, regcargoAbono);
					cargoAbono.add(regcargoAbono);
				}
			}

			
			logger.debug("ABONOS A CREAR > ", cargoAbono);
			
			if(cargoAbono.size() > 0 ){
				CrearCargoAbonoEntrada entradaCrearCargoAbono = new CrearCargoAbonoEntrada();
				CrearCargoAbono crearCargoAbono = new CrearCargoAbono();
				for(CargoAbono ca : cargoAbono){
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
				
				logger.debug("CARGO RETENCION ISFUT A TESORERIA");
				
				//Aplica cargo retencion isfut
				ProcesarRetencionIsfut_Entrada entradaRetencionIsfut = new ProcesarRetencionIsfut_Entrada();
				entradaRetencionIsfut.p_IdEmpresa = in.p_IdEmpresa;
				entradaRetencionIsfut.p_IdUsuario = in.p_IdUsuario;
				entradaRetencionIsfut.p_IdModulo = in.p_IdModulo;
				entradaRetencionIsfut.p_IdEventoCapital = in.p_IdEventoCapital;
				entradaRetencionIsfut.p_UsaReglasNegocio = "S";
				ProcesarRetencionIsfut retencionIsfut = new ProcesarRetencionIsfut();
				ProcesarRetencionIsfut_Salida salidaRetencionIsfut = retencionIsfut.logica(con, entradaRetencionIsfut);
				for(MensajeSalida info : salidaRetencionIsfut.P_OUT_MENSAJES) {					
					out.P_OUT_MENSAJES.add(new MensajeSalida(info.id ,info.tipo,info.codigo,info.descripcion));
				}
				//
				
			}

			
			
			
			// -----------------------------------------------------------------------------------------------------
			// 2. PREPARAR DATA PARA EL ENVIO A TESORERIA (debe ser porterior al envio a cuenta inversion)			
			
			// Genera movimiento de INGRESO a tesoreria por el total
			HashMap<String,Object> paramsExtras = new HashMap<String,Object>();
			paramsExtras.put("IdEventoCapital", in.p_IdEventoCapital);
			paramsExtras.put("pNombreEmisor", 	pNombreEmisor);
			paramsExtras.put("FechaSistema", fechaSistema);
			_ingresoEgresos.addAll(sql.query(con,SQL_SELECT_EGRESO_INGRESO_TOTAL,paramsExtras,_IngresoEgreso.class));
			AtomicLong idInterno = new AtomicLong(-1L);
			_ingresoEgresos.parallelStream().forEach(ingresoEgresoTotal->{
				ingresoEgresoTotal.NumeroReferencia = idInterno.getAndDecrement();
			});
			
			
			// Genera movimiento de EGRESO a tesoreria  por cada cliente que NO opere con cuenta de inversion			
			params.clear();
			params.put("IdEventoCapital", in.p_IdEventoCapital);
			params.put("FechaSistema", fechaSistema);
			_ingresoEgresos.addAll(sql.query(con,SQL_SELECT_EGRESO_INGRESO_CLIENTE,params,_IngresoEgreso.class));

			String pPagoNeto		= ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "TESORERIA", "RECALCULARMONTOEGRESO");
			List< Long>  IdEventosCapitalNoGenerados 	= new ArrayList<Long>();
			

			
			if (localizacionSistema.equalsIgnoreCase("PE")) {
				for(_IngresoEgreso recorre_IngresoEgreso : _ingresoEgresos){
					IngresoEgreso regIngresoEgreso = new IngresoEgreso();
					Utils.copyPojo(recorre_IngresoEgreso, regIngresoEgreso);
					if ((recorre_IngresoEgreso.MontoMenosMontoRetenido.compareTo(BigDecimal.ZERO) > 0) && (recorre_IngresoEgreso.IdBoveda.equals("CAV"))){
						regIngresoEgreso.Monto = recorre_IngresoEgreso.MontoMenosMontoRetenido;
						if (recorre_IngresoEgreso.TienePreferenciaDePago.equals("S")){		// SOLO DEBE ENVIAR A TESORERIA LOS QUE TENGAN FORMA DE PAGO
							ingresoEgresos.add(regIngresoEgreso);
						}
					}else{
						if (recorre_IngresoEgreso.IdBoveda.equals("CAV")){
							regIngresoEgreso.Monto = recorre_IngresoEgreso.MontoBeneficioArchivo;
							if (recorre_IngresoEgreso.TienePreferenciaDePago.equals("S")){	// SOLO DEBE ENVIAR A TESORERIA LOS QUE TENGAN FORMA DE PAGO
								ingresoEgresos.add(regIngresoEgreso);
							}
						}else{
							ingresoEgresos.add(regIngresoEgreso);
						}
					}
				}
			}else{
				for(_IngresoEgreso recorre_IngresoEgreso : _ingresoEgresos){
					IngresoEgreso regIngresoEgreso = new IngresoEgreso();
					Utils.copyPojo(recorre_IngresoEgreso, regIngresoEgreso);
					
					if(pPagoNeto.equalsIgnoreCase("S")&& recorre_IngresoEgreso.IdTipoMovimiento.equals("MOVEGR") ){
					
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
										ingresoEgresos.add(regIngresoEgreso);
									}else {
										if((regIngresoEgreso.Monto.compareTo(flujo.Saldo.abs())<0) && flujo.Saldo.compareTo(BigDecimal.ZERO)<=0) {
											//BORRAR EGRESO
											// CASO 3 SI MPD<FCET Y FCET<=0 ENTONCES NO SE GENERA EGRESO
											IdEventosCapitalNoGenerados.add(regIngresoEgreso.NumeroReferencia);
											ingresoEgresos.remove(regIngresoEgreso);
										}else {
											// CASO 2 SI FCET>0 ENTONCES MONTO EGRESO = MPD
											ingresoEgresos.add(regIngresoEgreso);
										}
									}
									
									
								}
							}else {
								ingresoEgresos.add(regIngresoEgreso);
							}
						}
					if (_indicadoresPortafolios.size()==0 && recorre_IngresoEgreso.IdTipoPersona.equals("CNT") ) {
						ingresoEgresos.add(regIngresoEgreso);
					};
					}else {	
						ingresoEgresos.add(regIngresoEgreso);
					}
				}
			}
				
			// FORMAS DE PAGO.
			params.clear();
			params.put("IdEventoCapital", in.p_IdEventoCapital);
			List<FormaPago> formapagos = sql.query(con,SQL_SELECT_FORMAS_PAGO,params,FormaPago.class);
			
			logger.info("INGRESOS_EGRESOS",ingresoEgresos);
			
			
			Map<Long, BigDecimal> preciosActualizados = new HashMap();
			if(ingresoEgresos.size() > 0){
				CrearIngresoEgreso crearIngresoEgreso = new CrearIngresoEgreso();
				CrearIngresoEgreso_Entrada entradacrearIngresoEgreso = new CrearIngresoEgreso_Entrada();
				for(IngresoEgreso ie : ingresoEgresos){
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



			params.clear();
			params.put("IdEventoCapital", in.p_IdEventoCapital);
			List<Garantia> garantias = sql.query(con, SQL_SELECT_GARANTIA, params, Garantia.class);
			
			logger.debug("GARANTIAS",garantias);
			
			if(garantias.size() > 0 ){
				
				params.clear();
				params.put("IdEventoCapital", in.p_IdEventoCapital);
				List<Contraparte> contraparteGarantias = sql.query(con, SQL_SELECT_CONTRAPARTE_GARANTIA, params, Contraparte.class);
				
				Long vContraparte = new Long(0L);
				
				for (Contraparte valorContraparte : contraparteGarantias){
					vContraparte = valorContraparte.IdContraparte; 
				}
				
				IngresarGarantiaEntrada ingresarGarantiaEntrada = new IngresarGarantiaEntrada();
				ingresarGarantiaEntrada.p_IdEmpresa = in.p_IdEmpresa;
				ingresarGarantiaEntrada.p_IdUsuario = in.p_IdUsuario;
				ingresarGarantiaEntrada.p_IdModulo = in.p_IdModulo;
				ingresarGarantiaEntrada.p_UsaReglasNegocio = "S";
				ingresarGarantiaEntrada.p_SimulaOperacion = "N";
				ingresarGarantiaEntrada.p_Certifica = "S";
				ingresarGarantiaEntrada.p_FechaMovimiento = fechaSistema;
				
				for(Garantia garantia : garantias){
					ObjGarantiaCuentaInversion garantiaSP = new ObjGarantiaCuentaInversion();
					garantiaSP.IdMoneda 					= garantia.IdMoneda;
					garantiaSP.IdMoneda 					= garantia.IdMoneda; 
					garantiaSP.MontoMonedaInstrumento 		= garantia.MontoMonedaInstrumento;
					garantiaSP.MontoMonedaSistema 			= garantia.MontoMonedaSistema; 
					garantiaSP.IdCliente 					= garantia.IdCliente; 
					garantiaSP.IdPortafolio 				= garantia.IdPortafolio; 
					garantiaSP.IdTipoInstrumento 			= garantia.IdTipoInstrumento; 
					garantiaSP.IdBolsa 						= garantia.IdBolsa; 
					garantiaSP.IdInstrumentoGarantizado		= garantia.IdInstrumentoGarantizado; 
					garantiaSP.IdConcepto 					= garantia.IdConcepto; 
					//garantiaSP.IdContraparteGarantia 		= garantia.IdContraparteGarantia; 
					garantiaSP.IdContraparteGarantia 		= vContraparte;
					garantiaSP.IdTipoOperacion 				= garantia.IdTipoOperacion; 
					garantiaSP.IdOperacion 					= garantia.IdOperacion;
					if (garantia.MontoMenosMontoRetenido.compareTo(BigDecimal.ZERO) > 0){
						garantiaSP.MontoMonedaInstrumento 	= garantia.MontoMenosMontoRetenido;
						garantiaSP.MontoMonedaSistema 		= garantia.MontoMenosMontoRetenido;
						ingresarGarantiaEntrada.p_GarantiaCuentaInversion.add(garantiaSP);
					}else{
						if (garantia.MontoBeneficioArchivo.compareTo(BigDecimal.ZERO) > 0){
							garantiaSP.MontoMonedaInstrumento 	= garantia.MontoMenosMontoRetenido;
							garantiaSP.MontoMonedaSistema 		= garantia.MontoMenosMontoRetenido;
							ingresarGarantiaEntrada.p_GarantiaCuentaInversion.add(garantiaSP);
						}else{
							ingresarGarantiaEntrada.p_GarantiaCuentaInversion.add(garantiaSP);
						}
					}

				}
				
				IngresarGarantiaJava ingresarGarantia = new IngresarGarantiaJava();
				IngresarGarantiaSalida salidaingresarGarantia = ingresarGarantia.logica(con, ingresarGarantiaEntrada);
				out.P_OUT_ERRORESCONTADOR += salidaingresarGarantia.P_OUT_ERRORESCONTADOR;
				out.P_OUT_MENSAJES.addAll(salidaingresarGarantia.P_OUT_MENSAJES);
				
				
				if (MensajeSalida.getNumeroErrores(salidaingresarGarantia.P_OUT_MENSAJES) > 0) {
					logger.debug("ERROR AL INGRESAR GARANTIAS");
					return out;
				}
				
			}			
			
			ActualizarEvento evento = new ActualizarEvento();
			evento.IdEventoCapital = in.p_IdEventoCapital;
			List<ActualizarEvento> actualizar = new ArrayList<ActualizarEvento>();
			actualizar.add(evento);
			
			sql.batch(con, SQL_UPDATE_EVENTO_CAPITAL, actualizar, ActualizarEvento.class);
			
			sql.batch(con, SQL_UPDATE_EVENTO_CAPITAL_CLIENTE, actualizar, ActualizarEvento.class);
			
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_OK, "UI_EXITO", new HashMap<String,String>() ,con,in.p_IdEmpresa));

		}else{
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR, "UI_EVENTO_VIGENTE_CERRAR", new HashMap<String,String>() ,con,in.p_IdEmpresa));
		}
		
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		
		// crear objeto de tipo noticacion
		
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
	
	public static void main(String[] args) throws Exception {		
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			try {
				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
				String json ="{\"p_IdEmpresa\":1,\"p_IdUsuario\":0,\"p_IdModulo\":\"CUS\",\"p_IdEventoCapital\":367906,\"p_IdTipoEventoCapital\":\"DV\"}";
				CerrarEventoCapitalDVEntrada Objin = gson.fromJson(json, CerrarEventoCapitalDVEntrada.class);
								
				CerrarEventoCapitalDV  kk = new CerrarEventoCapitalDV();
				System.out.println(kk.logica(con, Objin));
			con.rollback();
			//	con.commit();
				
		    	}catch(Exception er) {
					er.printStackTrace();	
					con.rollback();
				}
			
			con.close();
		} catch (Exception e) {
			e.printStackTrace();			
		}
	};
	
}
