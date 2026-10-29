package cl.bcs.custodia;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.custodia.CertificaSuscripcionOSAMasivo.EventoCapitalClienteCert;
import cl.bcs.custodia.IngresarGarantiaJava.IngresarGarantiaEntrada;
import cl.bcs.custodia.IngresarGarantiaJava.IngresarGarantiaSalida;
import cl.bcs.custodia.IngresarGarantiaJava.ObjGarantiaCustodia;
import cl.bcs.custodia.RetirarGarantiaJava.ObjGarantiasARetirar;
import cl.bcs.custodia.RetirarGarantiaJava.RetirarGarantiaEntrada;
import cl.bcs.custodia.RetirarGarantiaJava.RetirarGarantiaSalida;
import cl.bcs.facturacion.CrearMovimientoComprobante;
import cl.bcs.facturacion.CrearMovimientoComprobante.CrearMovimientoComprobante_Salida;
import cl.bcs.facturacion.CrearMovimientoComprobante.MovimientoComprobante_Entrada;
import cl.bcs.facturacion.CrearMovimientoComprobante.MovimientoComprobante_Salida;
import cl.bcs.plataforma.ins.ConsultarDatosInstrumentos;
import cl.bcs.plataforma.ins.ConsultarDatosInstrumentos.ConsultaInstrumento;
import cl.bcs.plataforma.ins.ConsultarDatosInstrumentos.ConsultarDatosInstrumento_Entrada;
import cl.bcs.plataforma.ins.ConsultarDatosInstrumentos.ConsultarDatosInstrumento_Salida;
import cl.bcs.plataforma.ins.ConsultarDatosInstrumentos.DatosInstrumento;
import cl.bcs.plataforma.mer.CalcularParidad;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadSalida;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoEntrada;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class EnviarMovimientoFacturarSuscripcionOSA_Masivo implements JavaCall{
	
	private static final Logger logger = LogManager.getLogger(EnviarMovimientoFacturarSuscripcionOSA_Masivo.class);
	//private final String SQL_SELECT_EVENTO_CAPITAL_CLIENTE = loadResourceToString("cl/bcs/custodia/sql/EnviarMovimientoFacturarSuscripcionOSA_SelectEventoCapitalClientes.sql");
	private final String SQL_UPDATE_EVENTO_CAPITAL_CLIENTE = loadResourceToString("cl/bcs/custodia/sql/EnviarMovimientoFacturarSuscripcionOSA_UpdateEventoCapitalClientes.sql");
	//private final String SQL_SELECT_EVENTO_CAPITAL_FPAGO = loadResourceToString("cl/bcs/custodia/sql/EnviarMovimientoFacturarSuscripcionOSA_SelectEventoCapitalClientesFormaPago.sql");
	
	private final String SQL_SELECT_EVENTO_CAPITAL_CLIENTE_MASIVO = loadResourceToString("cl/bcs/custodia/sql/EnviarMovimientoFacturarSuscripcionOSAMasivo_SelectEventoCapitalClientes.sql");
	private final String SQL_SELECT_EVENTO_CAPITAL_FPAGO_MASIVO = loadResourceToString("cl/bcs/custodia/sql/EnviarMovimientoFacturarSuscripcionOSAMasivo_SelectEventoCapitalClientesFormaPago.sql");
	private final String SQL_SELECT_SUSCRIPCION_OSA_DISPONIBLE = loadResourceToString("cl/bcs/custodia/sql/EnviarMovimientoFacturarSuscripcionOSAMasivo_SelectOSADisponible.sql");
	private final String SQL_SELECT_SUSCRIPCION_OSA_GARANTIA = loadResourceToString("cl/bcs/custodia/sql/EnviarMovimientoFacturarSuscripcionOSAMasivo_SelectOSAGarantia.sql");	
	private final String SQL_SELECT_SUSCRIPCION_OSA_GARANTIA_TOTAL = loadResourceToString("cl/bcs/custodia/sql/EnviarMovimientoFacturarSuscripcionOSAMasivo_SelectOSAGarantiaTotal.sql");
	
	public static class EnviarMovimientoFacturarSuscripcionOSA_Entrada{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public List<Long> p_IdEventoCapitalCliente;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class EnviarMovimientoFacturarSuscripcionOSA_Salida{
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida>	P_OUT_MENSAJES;
		
		public EnviarMovimientoFacturarSuscripcionOSA_Salida(){
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class UpdateEventoCapitalCliente{
		public Long IdEventoCapitalCliente;
		public Long IdComprobanteFac;
		public Long IdMovimientoComprobanteFac = 0L;
	}
	
	public static class EventoCapitalClienteFac{
		public Long IdCliente;
		public Long IdPortafolio;
		public Long IdInstrumento;
		public Long IdInstrumentoRelacionado;
		public String IdMoneda;
		public String IdBoveda;
		public String IdBolsa;
		public String IdEmisor;
		public BigDecimal CantidadNueva;
		public BigDecimal Precio;
		public Long IdEventoCapitalCliente;
		public String OperaCuentaInversion;
		public String IdPreferenciaPago;
		public String EsPreferenciaPagoPorDefecto;
		public String IdEstadoPreferenciaPago;
		public String IdEstadoEventoCliente;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class EventoCapitalClienteFormaPago{
	
		public Long IdEventoCapitalClienteFormaDePago;
		public Long IdEventoCapitalCliente;
		public String IdTipoMovimiento;
		public String IdModalidadPago;
		public String IdMedioDePago;
		public String NombreMedioDePago;
		public Long IdBanco;
		public String NombreBanco;
		public String IdTipoCuentaBancaria;
		public String NombreTipoCuentaBancaria;
		public Long IdCuentaBancaria;
		public String NombreCuentaBancaria;
		public String NumeroCuentaBancaria;
		public String IdTipoTarjetaBancaria;
		public String NumeroTarjetaBancaria;
		public String NumeroMedioDePago;
		public String Sucursal;
		public String FechaVctoDocumento;
		public BigDecimal Monto;
		public String FechaHoraIngresoRegistro;
		public Long IdUsuarioIngreso;
		public String IdEstado;
		public Long IdPreferenciaPagoPortafolio;
		public String IdMoneda;
		public Long IdTarjetaBancaria;
	
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	
	public static class OsaDisponible {
		
		public Long IdInstrumento;
		public Long IdPortafolio;
		public BigDecimal INGCantidad;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class OsaGarantiaTotal {
		public Long IdInstrumento;
		public Long IdPortafolio;
		public BigDecimal INGCantidad;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class OsaGarantia {
		
		public Long IdGarantia;
		public Long FolioGarantia;
		public Long IdInstrumento;
		public Long IdPortafolio;
		public BigDecimal INGCantidad;
		public String Nemotecnico;
		public String IdEstado;
		public String FechaOperacion;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	//Funcion que revisa si en el resultado de la consulta de cliente 
	//faltan registros de la lista de entrada de eventos a facturar
	public Long getFaltantesEventoCapitalCliente(List<Long> entrada, List<EventoCapitalClienteFac> resultadoConsulta) {
		Boolean existe = false;
		Integer faltantes = 0;
		for (Long id : entrada) {
			existe = false;
			for(EventoCapitalClienteFac evento : resultadoConsulta) {
				if(evento.IdEventoCapitalCliente.equals(id)) {
					existe = true;
					break;
				}
			}
			if(!existe) {
				faltantes++;
			}				
		}
		
		/*
		Long faltantes = entrada.p_IdEventoCapitalCliente
						.stream()
						.filter(id -> !eventoCapitalClientes.stream().filter(evt -> evt.IdEventoCapitalCliente.equals(id)).findFirst().isPresent())	
						.count();
		*/
		return (long)faltantes;
	}
	
	public EnviarMovimientoFacturarSuscripcionOSA_Salida logica(Connection con, EnviarMovimientoFacturarSuscripcionOSA_Entrada entrada) throws Exception {
		logger.debug("INICIO::EnviarMovimientoFacturarSuscripcionOSA_Masivo");
		EnviarMovimientoFacturarSuscripcionOSA_Salida out = new EnviarMovimientoFacturarSuscripcionOSA_Salida();
		SQL sql=new SQL();
		HashMap<String,Object> params = new HashMap<String,Object>();
						
		params.put("ListaIdEventoCapitalCliente", entrada.p_IdEventoCapitalCliente);
		String localizacionSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.LocalizacionSistema");
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		List<EventoCapitalClienteFac> eventoCapitalClientes = sql.query(con, SQL_SELECT_EVENTO_CAPITAL_CLIENTE_MASIVO, params, EventoCapitalClienteFac.class);
		List<EventoCapitalClienteFac> eventoCapitalClientesCer = new ArrayList<EventoCapitalClienteFac>();
		List<MovimientoComprobante_Salida> listMovimientoComprobante = new ArrayList<MovimientoComprobante_Salida>();
		HashMap<Long, Object> hashMovimientoComprobante =  new HashMap<Long, Object>();

		if(eventoCapitalClientes.size()> 0){
			for (EventoCapitalClienteFac evcapcli : eventoCapitalClientes) {
				if (evcapcli.IdEstadoEventoCliente.equalsIgnoreCase("CRT")) {
					eventoCapitalClientesCer.add(evcapcli);					
				}else {
					    String codigo = "UI_ERROR_MOVIMIENTO_DISTINTO_CER";
					    String descripcion = Diccionario.getMensaje(con, entrada.p_IdEmpresa, "UI_ERROR_MOVIMIENTO_DISTINTO_CER").add("IdEventoCapitalCliente", evcapcli.IdEventoCapitalCliente).msg();
					    MensajeSalida m = new MensajeSalida(evcapcli.IdEventoCapitalCliente,MensajeSalida.MENSAJE_INFORMACION,codigo,descripcion);
					    out.P_OUT_MENSAJES.add(m);
				}					
			}
		}
		
		//Long faltantes = getFaltantesEventoCapitalCliente(entrada.p_IdEventoCapitalCliente, eventoCapitalClientesCer);
	
		/*if ( !faltantes.equals(0L) ) {
			out.P_OUT_ERRORESCONTADOR = (long)faltantes;
			MensajeSalida msj = new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"NO_EXISTE_EVENTO_CAPITAL_CLIENTE",new HashMap<String,String>(),con,entrada.p_IdEmpresa);
			out.P_OUT_MENSAJES.add(msj);
			return out;
		}*/

		BigDecimal MontoLiquidacionFormaPago = BigDecimal.ZERO; 
		CrearMovimientoComprobante.CrearMovimientoComprobante_Entrada entradaCrearMovimientoComprobante = new CrearMovimientoComprobante.CrearMovimientoComprobante_Entrada();
		entradaCrearMovimientoComprobante.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaCrearMovimientoComprobante.p_IdUsuario = entrada.p_IdUsuario;
		entradaCrearMovimientoComprobante.p_IdModulo = entrada.p_IdModulo;
		entradaCrearMovimientoComprobante.p_simularOperacion = "N";
		entradaCrearMovimientoComprobante.p_UsaReglasNegocio = "S";
		entradaCrearMovimientoComprobante.p_EsIngresoAutomatico = "N";
		int index = 0;
		
		BigDecimal valorPrecioDolar = obtenerValorDolar(con, entrada.p_IdEmpresa);
		if (valorPrecioDolar.compareTo(BigDecimal.ZERO) <= 0) {		
			String codigo = "UI_ERROR_OBTENER_VALOR_DOLAR";
			String descripcion = Diccionario.getMensaje(con, entrada.p_IdEmpresa, "UI_ERROR_OBTENER_VALOR_DOLAR").msg();
			MensajeSalida m = new MensajeSalida(1L,MensajeSalida.MENSAJE_INFORMACION,codigo,descripcion);
			out.P_OUT_MENSAJES.add(m);
			out.P_OUT_ERRORESCONTADOR++;
		}
		
		List<Long> idsPortafolioEventoCapitalCliente = eventoCapitalClientesCer.stream()
				.map(x -> x.IdPortafolio)
				.collect(Collectors.toList());
		
		List<Long> idsInstrumentoOsa = eventoCapitalClientesCer.stream()
				.map(x -> x.IdInstrumentoRelacionado)
				.collect(Collectors.toList());	
		
		params.clear();

		params.put("IdsPortafolio", idsPortafolioEventoCapitalCliente);
		params.put("IdsInstrumentosRelacionados", idsInstrumentoOsa);
		
		List<OsaDisponible> osasDisponibles = sql.query(con, SQL_SELECT_SUSCRIPCION_OSA_DISPONIBLE, params, OsaDisponible.class);
		List<OsaGarantiaTotal> osaGarantiaTotal = sql.query(con, SQL_SELECT_SUSCRIPCION_OSA_GARANTIA_TOTAL, params, OsaGarantiaTotal.class);
		HashMap<Long, Object> hashMapOsasDisponibles = Utils.List2Hash(osasDisponibles, "IdPortafolio");
		HashMap<Long, Object> hashMapOsaGarantia = Utils.List2Hash(osaGarantiaTotal, "IdPortafolio");
		
		HashMap<Long, BigDecimal> hashMapDiferencias = new HashMap<Long, BigDecimal>();
		
		List<Long> idsPortafoliosGarantiasARetirar = new ArrayList<Long>();
		
		for(EventoCapitalClienteFac ecc : eventoCapitalClientesCer){
			
			OsaDisponible osaDisponible = (OsaDisponible) hashMapOsasDisponibles.get(ecc.IdPortafolio);
			
			OsaGarantiaTotal totalGarantiaCliente = (OsaGarantiaTotal) hashMapOsaGarantia.get(ecc.IdPortafolio);			
			
			BigDecimal cantidadGarantia = (totalGarantiaCliente != null) ? totalGarantiaCliente.INGCantidad : BigDecimal.ZERO;
			
			if (osaDisponible == null || cantidadGarantia.compareTo(ecc.CantidadNueva) >= 0) {
				idsPortafoliosGarantiasARetirar.add(ecc.IdPortafolio);				
				hashMapDiferencias.put(ecc.IdPortafolio, ecc.CantidadNueva);
			} else if (ecc.CantidadNueva.compareTo(osaDisponible.INGCantidad) > 0) {
				idsPortafoliosGarantiasARetirar.add(ecc.IdPortafolio);				
				hashMapDiferencias.put(ecc.IdPortafolio, ecc.CantidadNueva.subtract(osaDisponible.INGCantidad));
			}
			
			CrearMovimientoComprobante.MovimientoComprobante_Entrada mov = new CrearMovimientoComprobante.MovimientoComprobante_Entrada();
			mov.NumeroMvtoCmbte = new Long(index);
			mov.IdEmpresa = entrada.p_IdEmpresa;
			mov.IdCliente = ecc.IdCliente;
			mov.IdPortafolio = ecc.IdPortafolio;
			mov.IdInstrumento = ecc.IdInstrumento;
			mov.IdInstrumentoRelacionado = ecc.IdInstrumentoRelacionado;
			mov.IdMoneda = ecc.IdMoneda;
			mov.IdMonedaLiquidacion = ecc.IdMoneda;
			mov.IdBoveda = ecc.IdBoveda;
			mov.IdBolsa = ecc.IdBolsa;
			mov.IdEmisor = ecc.IdEmisor;
			mov.Cantidad = ecc.CantidadNueva;
			mov.Precio = ecc.Precio;
			mov.Monto = mov.Cantidad.multiply(ecc.Precio, MathContext.DECIMAL128);
			mov.MontoLiquidacion = mov.Cantidad.multiply(ecc.Precio, MathContext.DECIMAL128);
			MontoLiquidacionFormaPago= mov.Cantidad.multiply(ecc.Precio, MathContext.DECIMAL128);
			mov.FechaLiquidacion = fechaSistema;
			mov.FechaOrden = fechaSistema;
			mov.FechaTransaccion = fechaSistema;
			mov.FechaMovimientoComprobante = fechaSistema;
			
			if ("USD".equalsIgnoreCase(ecc.IdMoneda) && valorPrecioDolar.compareTo(BigDecimal.ZERO) > 0) {					
			    mov.MontoMovimientoComprobante = mov.Cantidad.multiply(ecc.Precio.multiply(valorPrecioDolar) , MathContext.DECIMAL128);					
				mov.ValorTipoCambio = valorPrecioDolar;
				mov.ValorCaptacionMonedaOrigen = mov.Cantidad.multiply(ecc.Precio, MathContext.DECIMAL128);
			}else {		    
				mov.MontoMovimientoComprobante = mov.Cantidad.multiply(ecc.Precio, MathContext.DECIMAL128);
				mov.ValorTipoCambio = BigDecimal.ZERO;
				mov.ValorCaptacionMonedaOrigen = BigDecimal.ZERO;
			}
			mov.FechaEmision = fechaSistema;
			mov.IdUsuario = entrada.p_IdUsuario;
			mov.IdConcepto = "SUECOS";
			mov.IdTipoProducto = "PE".equalsIgnoreCase(localizacionSistema) ? "INRV" : "INAC"; 
			mov.IdTipoComprobante = "PE".equalsIgnoreCase(localizacionSistema) ? "POL" : obtenerTipoComprobante(con, entrada.p_IdEmpresa, "FEE");
			mov.IdTipoOperacion = "SUSOSA";
			mov.IdCondicionLiquidacion = "T0";
			mov.IdTipoReajuste = "";
			mov.IdTipoConvenio = "SCV";
			mov.IdFormaCustodia = "CUS";
			mov.IdTipoCanalIngreso = "AGE";
			mov.EsComprobIndividual= "N";
			mov.EsCuentaPropia= "N";
			mov.EsVentaCorta= "N";
			mov.EsPrecioPrima= "N";
			mov.EsEnRueda= "N";
			mov.EsRuteo= "N";
			mov.EsAPlazo= "N";
			mov.EsFungible= "N";
			mov.ActualizarCustodia= "S";
			mov.ActualizarCuentaInversion= "S";
			if (	
				 	!"INGSUS_TRA_CLP".equalsIgnoreCase(ecc.IdPreferenciaPago)
				&& 	!"S".equalsIgnoreCase(ecc.EsPreferenciaPagoPorDefecto)
				&& 	!"TRA".equalsIgnoreCase(ecc.IdEstadoPreferenciaPago)
				&& "S".equalsIgnoreCase(ecc.OperaCuentaInversion) ) 
			{
				
				mov.ActualizarTesoreria= "N";
			}
			else {

				mov.ActualizarTesoreria= "S";
			}
			mov.ActualizarCustodiaRelacionado = "N";
			mov.CrearComprobanteAutomatico = "S";
			mov.SecuenciaAgrupacion = "";
			mov.ModPrefFact = "";
			mov.IdEmpleadoDigitador = 0L;
			mov.IdInstrumentoSubyacente = 0L;
			mov.IdMonedaSubyacente = "";
			mov.IdCorredoraContraparte = 0L;
			mov.IdOrden = 0L;
			mov.IdAsignacion = 0L;
			mov.IdAsignacionRef = 0L;
			mov.IdTransaccion = 0L;
			mov.FolioTransaccion = "";
			mov.FechaVencimiento = "0001-01-01";
			mov.PrecioAPlazo = BigDecimal.ZERO;
			mov.MontoAPlazo = BigDecimal.ZERO;
			mov.FechaParidad = "0001-01-01";
			mov.ValorParidad = BigDecimal.ZERO;
			mov.FechaTipoCambio = "0001-01-01";
			mov.Tasa = BigDecimal.ZERO;
			mov.MontoDescuento = BigDecimal.ZERO;
			mov.MontoInteres = BigDecimal.ZERO;
			mov.Glosa = "";
			mov.ValorRescate = BigDecimal.ZERO;
			mov.TasaAgente = BigDecimal.ZERO;
			mov.Spread = BigDecimal.ZERO;
			mov.DiasPlazo = BigDecimal.ZERO;
			mov.FolioCartera = 0L;
			mov.TasaEmision = BigDecimal.ZERO;
			mov.IdOperacion = ecc.IdEventoCapitalCliente;
			index++;
			entradaCrearMovimientoComprobante.p_Movimientos.add(mov);
		}
		
		if (idsPortafoliosGarantiasARetirar.size() > 0) {
			params.clear();
			params.put("IdsPortafolio", idsPortafoliosGarantiasARetirar);
			params.put("IdsInstrumentosRelacionados", idsInstrumentoOsa);
			List<OsaGarantia> garantiasPortafolios = sql.query(con, SQL_SELECT_SUSCRIPCION_OSA_GARANTIA, params, OsaGarantia.class);
			
			RetirarGarantiaJava retirarGarantiaService = new RetirarGarantiaJava();
			RetirarGarantiaEntrada entradaRetiroGarantia = new RetirarGarantiaEntrada();
			entradaRetiroGarantia.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaRetiroGarantia.p_IdUsuario = entrada.p_IdUsuario;
			entradaRetiroGarantia.p_IdModulo = entrada.p_IdModulo;
			entradaRetiroGarantia.p_UsaReglasNegocio = "S";
			entradaRetiroGarantia.p_SimulaOperacion = "N";
			entradaRetiroGarantia.p_FechaMovimiento = fechaSistema;
			entradaRetiroGarantia.p_IdTipoOperacion = "RETGTA";
			
			for (OsaGarantia garantia : garantiasPortafolios) {
				
				BigDecimal cantidadRetiro = hashMapDiferencias.get(garantia.IdPortafolio);
				
				ObjGarantiasARetirar garantiaARetirar = new ObjGarantiasARetirar();
				garantiaARetirar.IdGarantia = garantia.IdGarantia;
				garantiaARetirar.Cantidad = cantidadRetiro;
				garantiaARetirar.ConCertificadoAutomatico = "S";
				entradaRetiroGarantia.p_tablaRetiro.add(garantiaARetirar);
			}
			
			RetirarGarantiaSalida salidaRetiroGarantia = retirarGarantiaService.logica(con, entradaRetiroGarantia);
		}
		
		HashMap<String,Object> param = new HashMap<String,Object>();
		param.put("ListaIdEventoCapitalCliente", entrada.p_IdEventoCapitalCliente);
		
/////////////FORMAS DE PAGOS		
		List<EventoCapitalClienteFormaPago> eventoCapitalClientesFormaPago = sql.query(con, SQL_SELECT_EVENTO_CAPITAL_FPAGO_MASIVO, param, EventoCapitalClienteFormaPago.class);
		
		for(EventoCapitalClienteFormaPago eccfp : eventoCapitalClientesFormaPago){
			CrearMovimientoComprobante.MovimientoComprobanteFormaDePago_Entrada fp = new CrearMovimientoComprobante.MovimientoComprobanteFormaDePago_Entrada();
			fp.NumeroMvtoCmbte = new Long(index);
			fp.AfectarCuentaInversion = "S";
			fp.FechaDocumentoPago= "0001-01-01";
			fp.FechaVctoTarjetaBancaria=  "0001-01-01";
			fp.IdBanco= eccfp.IdBanco;
			fp.IdCuentaBancaria=eccfp.IdCuentaBancaria;
			fp.IdMedioDePago= eccfp.IdMedioDePago;
			fp.IdModalidadDePago = eccfp.IdModalidadPago;
			fp.IdMoneda= eccfp.IdMoneda;
			fp.IdTarjetaBancaria=  eccfp.IdTarjetaBancaria;
			fp.MontoLiquidacionFormaDePago = MontoLiquidacionFormaPago;
			fp.NumeroCuentaBancaria = eccfp.NumeroCuentaBancaria;
			fp.NumeroDocumento = 0L;
			fp.NumeroMvtoCmbte = 0L;
			fp.NumeroMvtoCmbteFormaDePago = 0L;
			fp.NumeroSerie = "";
			fp.NumeroTarjetaBancaria =eccfp.NumeroTarjetaBancaria;
			fp.IdTipoCuentaBancaria = eccfp.IdTipoCuentaBancaria;
			index++;
			entradaCrearMovimientoComprobante.p_MovimientosFormaDePago.add(fp);
		}		
		
		CrearMovimientoComprobante serviceCrearMovimientoComprobante = new CrearMovimientoComprobante();
		CrearMovimientoComprobante_Salida salidaCrearMovimientoComprobante = serviceCrearMovimientoComprobante.logica(con,entradaCrearMovimientoComprobante);
		listMovimientoComprobante = salidaCrearMovimientoComprobante.p_out_Movimientos;
		hashMovimientoComprobante = Utils.List2Hash(listMovimientoComprobante,"IdOperacion");
		for (MensajeSalida msj : salidaCrearMovimientoComprobante.P_OUT_MENSAJES) {
			if (msj.tipo.equalsIgnoreCase(MensajeSalida.MENSAJE_ERROR))
				out.P_OUT_ERRORESCONTADOR++;
			out.P_OUT_MENSAJES.add(msj);
		}
		

		if("PE".equals(localizacionSistema)){
////////////////////////////////////////INGRESO DE GARANTIAS ///////////////////////////////////////////////////////////////////////		
		    // INSTRUMENTOS		
			ConsultarDatosInstrumentos obtenerDatosInstrumentos = new ConsultarDatosInstrumentos();
			ConsultarDatosInstrumento_Entrada inObtenerDatosInstrumentos = new ConsultarDatosInstrumento_Entrada();
			
			List<ConsultaInstrumento> instrumentosEntrada = new ArrayList<ConsultaInstrumento>();
			
			// GARANTIAS		
			List<ObjGarantiaCustodia> detalleGarantiaCustodia = new ArrayList<ObjGarantiaCustodia>();
								
			Integer idx = 1;
			for (MovimientoComprobante_Entrada movimiento: entradaCrearMovimientoComprobante.p_Movimientos) {
				if ( !instrumentosEntrada.stream().filter(inst -> inst.IdInstrumento.equals(movimiento.IdInstrumento)).findFirst().isPresent()) {
					ConsultaInstrumento instruIn = new ConsultaInstrumento();
					instruIn.Correlativo = new Long(idx);
					instruIn.IdInstrumento = movimiento.IdInstrumento;
					instruIn.Nemotecnico = null;
					instrumentosEntrada.add(instruIn);
					idx++;
				}
				
			}
			
			inObtenerDatosInstrumentos.p_IdEmpresa = entrada.p_IdEmpresa;
			inObtenerDatosInstrumentos.p_IdModulo = entrada.p_IdModulo;
			inObtenerDatosInstrumentos.p_IdUsuario = entrada.p_IdUsuario;
			inObtenerDatosInstrumentos.p_CreaOpcionAutomatica = "N";
			inObtenerDatosInstrumentos.p_Instrumentos.addAll(instrumentosEntrada);
			
			
			ConsultarDatosInstrumento_Salida outDatosInstrumentos = obtenerDatosInstrumentos.logica(con, inObtenerDatosInstrumentos);
			
			
			for(MovimientoComprobante_Entrada movimiento : entradaCrearMovimientoComprobante.p_Movimientos){
				ObjGarantiaCustodia registroInGar = new ObjGarantiaCustodia();
				registroInGar.IdInstrumento 				= movimiento.IdInstrumento;
				registroInGar.IdConcepto 					= "BLOSUS";// BLOQUEO SUSCRIPCION		
				registroInGar.IdMoneda						= movimiento.IdMoneda;
				registroInGar.Cantidad						= movimiento.Cantidad;
				registroInGar.ValorRescate					= movimiento.ValorRescate;
				registroInGar.MontoMonedaInstrumento		= movimiento.Monto;
				registroInGar.MontoMonedaSistema 			= movimiento.Monto;
		 		registroInGar.IdTipoReajuste 				= movimiento.IdTipoReajuste;		
				registroInGar.IdEmisor 						= movimiento.IdEmisor;
				registroInGar.EsFungible 					= movimiento.EsFungible;
				registroInGar.IdCliente 					= movimiento.IdCliente;
				registroInGar.IdPortafolio					= movimiento.IdPortafolio;
				registroInGar.IdBolsa						= movimiento.IdBolsa;
				registroInGar.Precio 						= movimiento.Precio;
				registroInGar.Tasa							= movimiento.Tasa;
				registroInGar.IdContraparteGarantia			= 10184L;	
				registroInGar.IdMonedaEmisionInstrumento	= "";
				registroInGar.IdOperacion					= 0L;
				registroInGar.IdTipoOperacion				= "INGGTA";
				registroInGar.PlazoEnDias					= 0L;
				registroInGar.IdOperacion					= 0L;
				registroInGar.FechaEmisionInstrumento		= "0001-01-01";
				registroInGar.IdAgente						= 310L;
				registroInGar.IdTipoComprobante 			= "CUS";
				registroInGar.FolioCarteraGarantizada		= movimiento.FolioCartera;
				
				// INSTRUMENTOS		
				for (DatosInstrumento recorreIns : outDatosInstrumentos.P_OUT_INSTRUMENTOS){
					if(recorreIns.IdInstrumento.equals(movimiento.IdInstrumento)) {
						registroInGar.FechaVencimientoInstrumento		= recorreIns.FechaVencimiento.toString();
						registroInGar.NemoSVS 							= ""; // SOLO PARA IF
						registroInGar.IdClasificacionTipoInstrumento	= recorreIns.IdClasificacionTipoInstrumento;
						registroInGar.IdTipoInstrumento					= recorreIns.IdTipoInstrumento;
						registroInGar.IdInstrumentoGarantizado			= movimiento.IdInstrumento;
						break;
					}
				}
				detalleGarantiaCustodia.add(registroInGar);
			}
			
			
			IngresarGarantiaJava  	Garantia 		= new IngresarGarantiaJava();
			IngresarGarantiaEntrada IngresoGarantia = new IngresarGarantiaEntrada();		
			
			IngresoGarantia.p_IdEmpresa 		= entrada.p_IdEmpresa;
			IngresoGarantia.p_IdUsuario			= entrada.p_IdUsuario;
			IngresoGarantia.p_IdModulo			= entrada.p_IdModulo;
			IngresoGarantia.p_IdEmpleado		= 168L;
			IngresoGarantia.p_UsaReglasNegocio	= "N";
			IngresoGarantia.p_SimulaOperacion	= "N";
			IngresoGarantia.p_Certifica 		= "N";
			IngresoGarantia.p_FechaMovimiento	= fechaSistema;		
			IngresoGarantia.p_GarantiaCustodia.addAll(detalleGarantiaCustodia);


			System.out.println(IngresoGarantia);
			
			IngresarGarantiaSalida GarantiaOut = Garantia.logica(con, IngresoGarantia);
		}
////////////////////////////////////////INGRESO DE GARANTIAS ///////////////////////////////////////////////////////////////////////		
		
		List<UpdateEventoCapitalCliente> upds = new ArrayList<UpdateEventoCapitalCliente>();
		UpdateEventoCapitalCliente upd = new UpdateEventoCapitalCliente();
		for (Long id : entrada.p_IdEventoCapitalCliente) {
			upd = new UpdateEventoCapitalCliente();
			upd.IdEventoCapitalCliente = id;			
			// Se obtiene el IdComprobanteFacturacion para relacionarlo al EventoCapitalCliente.
			for(MensajeSalida msj : out.P_OUT_MENSAJES) {			
				if(msj.codigo.equalsIgnoreCase("UI_COMPROBANTE_CREADO")) {
					upd.IdComprobanteFac = msj.id;
				}
			}
			MovimientoComprobante_Salida movimiento = (MovimientoComprobante_Salida) hashMovimientoComprobante.get(id);
			if(movimiento != null) {
				upd.IdMovimientoComprobanteFac = movimiento.IdMovimientoComprobante;
			}
			upds.add(upd);
		}
		
		sql.batch(con, SQL_UPDATE_EVENTO_CAPITAL_CLIENTE, upds, UpdateEventoCapitalCliente.class);
		
		logger.debug("FIN::EnviarMovimientoFacturarSuscripcionOSA_Masivo");
		
		return out;
	}
	
	public String obtenerTipoComprobante(Connection con, Long idEmpresa, String tipoComprobantePorDefecto) {
		String tipoComprobante = tipoComprobantePorDefecto;
		try {
			tipoComprobante = ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "CUSTODIA", "TipoFacturaDefectoEventoCapital");
		} catch(Exception e) {
			logger.error("ERROR::EnviarMovimientoFacturarSuscripcionOSA_Masivo:: no se ecuentra parametro TipoFacturaDefectoAsignaciones");
		}
		return tipoComprobante;
	}
	
////////////OBTENER VALOR DOLAR/////////
private BigDecimal obtenerValorDolar(Connection conexion, Long idEmpresa) throws Exception {

logger.debug("INICIO:: ModificarMovimientoCarteraEnTransitoMasivo.obtenerValorDolar");
Long inicio = System.currentTimeMillis();

CalculoEntrada tipoCambioFondoLocalRespectoLocal = new CalculoEntrada();
tipoCambioFondoLocalRespectoLocal.IdTrabajo = 1L;
tipoCambioFondoLocalRespectoLocal.IdInterno = 1L;
tipoCambioFondoLocalRespectoLocal.Fecha = ConfiguracionSistema.getParametroConfiguracionString(conexion,
idEmpresa, "PLATAFORMA", "PLA.FechaSistema");
tipoCambioFondoLocalRespectoLocal.IdMonedaOrigen = "USD";
tipoCambioFondoLocalRespectoLocal.IdMonedaDestino = "CLP";
tipoCambioFondoLocalRespectoLocal.ValorCambioLocalModificado = BigDecimal.ZERO;
tipoCambioFondoLocalRespectoLocal.MontoOrigen = BigDecimal.ONE;
tipoCambioFondoLocalRespectoLocal.IdUso = "";

CalcularParidadEntrada calcularParidadEntrada = new CalcularParidadEntrada();
calcularParidadEntrada.p_IdTrabajo = 1L;
calcularParidadEntrada.p_IdEmpresa = idEmpresa;
calcularParidadEntrada.p_CalculosEntrada.add(tipoCambioFondoLocalRespectoLocal);

CalcularParidad calcularParidad = new CalcularParidad();
CalcularParidadSalida calculoSalida = calcularParidad.logica(conexion, calcularParidadEntrada);

Map<Long, CalcularParidad.CalculoSalida> calculosPorIdInterno = calculoSalida.p_out_CalculosSalida.stream().collect(Collectors.toMap(calculo -> calculo.IdInterno, calculo -> calculo));

BigDecimal valorDolar = calculosPorIdInterno.getOrDefault(1L, new CalcularParidad.CalculoSalida()).MontoDestino;

Long fin = System.currentTimeMillis();
logger.debug("FIN:: ModificarMovimientoCarteraEnTransitoMasivo.obtenerValorDolar {}", fin - inicio);

return valorDolar;
}

	
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = new Gson();	
		EnviarMovimientoFacturarSuscripcionOSA_Entrada in=gson.fromJson(jin.toString() ,EnviarMovimientoFacturarSuscripcionOSA_Entrada.class);
		EnviarMovimientoFacturarSuscripcionOSA_Salida out= logica(conn,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		
		return dataout;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			//String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"CUS\",\"p_IdEventoCapitalCliente\": [149819, 92800, 99956]}";
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"CUS\",\"p_IdEventoCapitalCliente\": [149819, 92800, 99956]}";
			EnviarMovimientoFacturarSuscripcionOSA_Entrada Objin = gson.fromJson(json, EnviarMovimientoFacturarSuscripcionOSA_Entrada.class);
			
			EnviarMovimientoFacturarSuscripcionOSA_Masivo demo=new EnviarMovimientoFacturarSuscripcionOSA_Masivo();				
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			System.out.println("SALIDA");
 			try {
 				EnviarMovimientoFacturarSuscripcionOSA_Salida ObjOut= demo.logica(con, Objin);
 				//con.commit();
 				con.rollback();
 				System.out.println(ObjOut.toString());
 			}
 			catch(Exception er) {
				er.printStackTrace();	
				con.rollback();
			}
 			//con.commit();
 			
	    	}
		 	
		System.out.println("== END ==");
	}
	
}
