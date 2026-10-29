package cl.bcs.ordenes;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.custodia.utils.ValorizarInstrumento;
import cl.bcs.custodia.utils.ValorizarInstrumento.ObjEntradaInstrumentos;
import cl.bcs.custodia.utils.ValorizarInstrumento.ObjResultadoValorizacion;
import cl.bcs.custodia.utils.ValorizarInstrumento.ValorizarInstrumentoEntrada;
import cl.bcs.custodia.utils.ValorizarInstrumento.ValorizarInstrumentoSalida;
import cl.bcs.ordenes.ConsultarCobrosOrdenes.LineaMatrizOperacional;
import cl.bcs.ordenes.ConsultarCobrosOrdenesPe.ConsultarCobrosOrdenesPe_Entrada;
import cl.bcs.ordenes.ConsultarCobrosOrdenesPe.ConsultarCobrosOrdenesPe_Salida;
import cl.bcs.ordenes.ConsultarCobrosOrdenesPe.DefinicionCobro_Salida;
import cl.bcs.ordenes.ConsultarCobrosOrdenesPe.InformacionOrden_Entrada;
import cl.bcs.ordenes.pojo.ConstantesOrdenes;
import cl.bcs.plataforma.com.CalcularCobros;
import cl.bcs.plataforma.com.CalcularCobros.CalcularCobrosEntrada;
import cl.bcs.plataforma.com.CalcularCobros.CalcularCobrosSalida;
import cl.bcs.plataforma.com.pe.CalcularCobrosPe;
import cl.bcs.plataforma.com.pe.CalcularCobrosPe.CalcularCobrosPe_Entrada;
import cl.bcs.plataforma.com.pe.CalcularCobrosPe.CalcularCobrosPe_Salida;
import cl.bcs.plataforma.com.pe.CalcularCobrosPe.DefinicionCobro_Entrada;
import cl.bcs.plataforma.com.pe.CalcularCobrosPe.InformacionOperacion_Entrada;
import cl.bcs.plataforma.com.pe.ConsultarCobrosPeru.DefinicionRangoCobro;
import cl.bcs.plataforma.ins.ConsultarInstrumentoPrecioBolsa;
import cl.bcs.plataforma.ins.ConsultarInstrumentoPrecioBolsa.ConsultarInstrumentoPrecioBolsa_Entrada;
import cl.bcs.plataforma.ins.ConsultarInstrumentoPrecioBolsa.ConsultarInstrumentoPrecioBolsa_Salida;
import cl.bcs.plataforma.mer.CalcularParidad;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadSalida;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoSalida;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente.FechasIn;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.OutObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.OutObtenerDiaSiguiente.FechasOut;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.MonedaFormato;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;


public class CalcularMontoYCobrosOrden implements JavaCall {
	private static final Logger logger = LogManager.getLogger(CalcularMontoYCobrosOrden.class);

	private final String SQL_SELECT_INSTRUMENTOS = loadResourceToString("cl/bcs/ordenes/sql/CalcularMontoYCobrosOrden_SelectInstrumentos.sql");
	private final String SQL_SELECT_TIPO_OPERACION = loadResourceToString("cl/bcs/ordenes/sql/CalcularMontoYCobrosOrden_SelectTipoOperacion.sql");
	private final String SQL_SELECT_COBROS_ULTIMA_ORDEN = loadResourceToString("cl/bcs/ordenes/sql/CalcularMontoYCobrosOrden_SelectCobrosUltimaOrden.sql");
	private final ConstantesOrdenes _c = new ConstantesOrdenes();
		
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = getGsonBuilder();
		CalcularMontoYCobrosOrden_Entrada in = gson.fromJson(jin.toString(), CalcularMontoYCobrosOrden_Entrada.class);
		CalcularMontoYCobrosOrden_Salida out = logica(con, in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public static class CalcularCobroYMontosOrden_Instrumento {
		public Long IdInstrumento;
		public Long Tamano;
		public String IdMoneda;
		public String IdClasificacionTipoInstrumento;
		public String IdTipoInstrumento;
		public String IdTipoReajuste;
		public Date	FechaVencimiento;
		public Date	FechaEmision;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class CalcularCobroYMontosOrden_TipoOperacion {
		public String IdTipoOperacion;
		public String AccionOrden;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Cobro_Fijo {
		public Long IdInterno; 
		public Long IdInternoCobro;
		public Long IdPortafolio; 
		public String IdBolsa;
		public String IdTipoProducto;
		public String IdTipoOperacion;
		public String IdTipoCanalIngreso;
		public String IdCobro;
		public BigDecimal ValorCobro; 
		public String FormaCobro; 
		public BigDecimal ValorTopeMinimoOrden; 
		public String IdMoneda; 
		public String CodigoCobroClasificacion;
		public String PermiteCambiarValor; 
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public BigDecimal calculaMontoParidad(BigDecimal Precio,BigDecimal Cantidad,BigDecimal Factor){
		BigDecimal puntoCinco = new BigDecimal(0.5);
		BigDecimal y = Precio.multiply(Factor);
		//y = y.setScale(10, RoundingMode.HALF_UP);
		if (y.compareTo(puntoCinco) < 0){
			y = y.setScale(3, RoundingMode.DOWN);
		}
		else {
			y = y.setScale(2, RoundingMode.HALF_UP);
		}

		return y.multiply(Cantidad);
		/*
		 * BigDecimal y = ord.PrecioOrden.multiply(calc.FactorOrigenDestino);
				BigDecimal puntoCinco = new BigDecimal(0.5);
				y = y.setScale(10, RoundingMode.HALF_UP);
				if (y.compareTo(puntoCinco) < 0){
					y = y.setScale(3, RoundingMode.DOWN);
				}
				else {
					y = y.setScale(2, RoundingMode.HALF_UP);;
				}
				BigDecimal montoDestino = y.multiply(ord.CantidadOrden);
		 */
	}
	
	@Trace
	public CalcularMontoYCobrosOrden_Salida logica(Connection conn,CalcularMontoYCobrosOrden_Entrada ent) throws Exception {
		Long init = System.currentTimeMillis();
		logger.info("Inicio servicio CalcularMontoYCobrosOrden");
		CalcularMontoYCobrosOrden_Salida salida = new CalcularMontoYCobrosOrden_Salida();
		
		HashMap<Long,Orden_Proceso> ordenesHash = new HashMap<Long,Orden_Proceso>();
		ArrayList<Orden_Proceso> ordenes = new ArrayList<Orden_Proceso>();
		SQL _sql = new SQL();
		Long idTrabajo = 5000L;
		
		if (ent.p_Ordenes.size() == 0) {
			return salida;
		}
		//TODO REALIZAR VALIDACIONES DE QUE VENGA CADA CAMPO EN LA ENTRADA
		ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
		InformacionEmpresa empresa = consultarEmpresaService.logica(conn, ent.p_IdEmpresa);
		Long idPaisEmpresa = empresa.IdPais;
		//creacion de hashes
		HashMap<String,CalcularCobroYMontosOrden_TipoOperacion> tiposOperacion = new HashMap<>();
		HashMap<Long,CalcularCobroYMontosOrden_Instrumento> instrumentos = new HashMap<>();
		HashMap<String, Object> _instrumentosHash = new HashMap<>();
		{
			ArrayList<Long> idsInstrumento = new ArrayList<>();
			ArrayList<String> idsTipoOperacion = new ArrayList<>();
			for(Orden_Entrada ord: ent.p_Ordenes) {
				//Utils.copyPojo(ord, ords);
				Orden_Proceso ords = copyPojoOrdenProceso(ord);
				ordenesHash.put((Long)ord.IdInterno,ords);
				ordenes.add(ords);
				
				idsInstrumento.add(ords.IdInstrumento);
				if (ords.IdInstrumentoSubyacente != null)
					idsInstrumento.add(ords.IdInstrumentoSubyacente);
				idsTipoOperacion.add(ords.IdTipoOperacion);
			}
			
			HashMap<String,Object> paramsIn1 = new HashMap<>();
			paramsIn1.put("idsInstrumento", idsInstrumento);
			List<CalcularCobroYMontosOrden_Instrumento> _instrumentos = _sql.query(conn, SQL_SELECT_INSTRUMENTOS, paramsIn1, CalcularCobroYMontosOrden_Instrumento.class);
			_instrumentosHash = Utils.List2Hash(_instrumentos, "IdInstrumento");
			for (CalcularCobroYMontosOrden_Instrumento obj : _instrumentos) {
				instrumentos.put(obj.IdInstrumento, obj);
			}
			
			HashMap<String,Object> paramsIn2 = new HashMap<>();
			paramsIn2.put("idsTipoOperacion", idsTipoOperacion);
			List<CalcularCobroYMontosOrden_TipoOperacion> _tiposOperacion = _sql.query(conn, SQL_SELECT_TIPO_OPERACION, paramsIn2, CalcularCobroYMontosOrden_TipoOperacion.class);
			for (CalcularCobroYMontosOrden_TipoOperacion obj : _tiposOperacion) {
				tiposOperacion.put(obj.IdTipoOperacion, obj);
			}
		}
		
		String idMonedaSimultanea = ConfiguracionSistema.getParametroConfiguracionString(conn, ent.p_IdEmpresa, "ORDENES", "MonedaSimultanea");
		String idMonedaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, ent.p_IdEmpresa, "PLATAFORMA", "PLA.MonedaSistema");
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, ent.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		String localizacionSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, ent.p_IdEmpresa, "PLATAFORMA", "PLA.LocalizacionSistema");
		
		String parametroCalculoPrecioPunta;
		try {
			parametroCalculoPrecioPunta = ConfiguracionSistema.getParametroConfiguracionString(conn, ent.p_IdEmpresa, "ORDENES", "CalculoPrecioPunta");
		}catch(Exception e) {
			parametroCalculoPrecioPunta = "N";
		}
		
		for (Orden_Proceso ord : ordenes) {
			if (ord.IdTipoOperacion.equalsIgnoreCase(_c.TIPO_OPERACION_COMSIM) || ord.IdTipoOperacion.equalsIgnoreCase(_c.TIPO_OPERACION_VENSIM)) {
				ord.IdMonedaOrden = idMonedaSimultanea;
			}
			//REPORTES, LA MONEDA DE LA ORDEN ES LA DEL SUBYACENTE,EXCEPTO COMPRA POR CANAL AGENTE, LA MONEDA DE LA ORDEN ES LA DE LIQUIDACION
			else if (instrumentos.get(ord.IdInstrumentoSubyacente) != null 
					&& (ord.IdTipoOperacion.equalsIgnoreCase(_c.TIPO_OPERACION_COMRRV) || ord.IdTipoOperacion.equalsIgnoreCase(_c.TIPO_OPERACION_COMRRF) 
							|| ord.IdTipoOperacion.equalsIgnoreCase(_c.TIPO_OPERACION_VENRRV) || ord.IdTipoOperacion.equalsIgnoreCase(_c.TIPO_OPERACION_VENRRF))) {
				ord.IdMonedaOrden = ord.IdMonedaLiquidacionOrden;//instrumentos.get(ord.IdInstrumentoSubyacente).IdMoneda;
			}
			else if (instrumentos.get(ord.IdInstrumento).IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_IF)){
				//se mantiene la moneda
				//logger.debug(ord.IdMonedaOrden);
			}
			else if (ord.IdMonedaOrden != null && !"".equalsIgnoreCase(ord.IdMonedaOrden.trim()))
			{
				//se mantiene la moneda
			}
			else {
				ord.IdMonedaOrden = instrumentos.get(ord.IdInstrumento).IdMoneda;
			}
			ord.AccionOrden = tiposOperacion.get(ord.IdTipoOperacion).AccionOrden;
		}
		
		//cobros chile
		HashMap<Long,Cobro_Proceso> cobrosHash = new HashMap<>();
		if (localizacionSistema.equalsIgnoreCase("CL"))
		{
			ConsultarCobrosOrdenes.ConsultarCobrosOrdenes_Entrada consultarCobrosOrdenesEntrada = new ConsultarCobrosOrdenes.ConsultarCobrosOrdenes_Entrada();
			ConsultarCobrosOrdenes consultarCobrosOrdenesService = new ConsultarCobrosOrdenes();
			
			consultarCobrosOrdenesEntrada.p_IdEmpresa = ent.p_IdEmpresa;
			consultarCobrosOrdenesEntrada.p_IdModulo = ent.p_IdModulo;
			consultarCobrosOrdenesEntrada.p_IdUsuario = ent.p_IdUsuario;
			
			for (Orden_Proceso ord: ordenes) {
				ConsultarCobrosOrdenes.InformacionOperacion aux = new ConsultarCobrosOrdenes.InformacionOperacion();
				aux.IdBolsa = ord.IdBolsa;
				aux.IdCorredoraCorresponsal = ord.IdCorredorCorresponsal;
				aux.IdInterno = ord.IdInterno;
				aux.IdPortafolio = ord.IdPortafolio;
				aux.IdTipoCanalIngreso = ord.IdTipoCanalIngreso;
				aux.IdTipoOperacion = ord.IdTipoOperacion;
				aux.IdTipoProducto = ord.IdTipoProducto;
				consultarCobrosOrdenesEntrada.p_Operaciones.add(aux);
			}
			Long IdInternoCobro = 0L;
			if(ent.p_CobrosFijos.size()==0){
				ConsultarCobrosOrdenes.ConsultarCobrosOrdenes_Salida  cobrosOut = consultarCobrosOrdenesService.logica(conn, consultarCobrosOrdenesEntrada);
				
				for (LineaMatrizOperacional mat : cobrosOut.p_out_MatrizOperacional) {
					Orden_Proceso ord2 = ordenesHash.get(mat.IdInterno);
					//Utils.copyPojo(mat, cob1);
					Cobro_Proceso cob1 = copyPojoCobroProceso(mat);
					cob1.IdInternoCobro = IdInternoCobro++;
					cob1.IdOrigenCobro = mat.IdOrigenCobro;
					cob1.ValorCobroPerfil = mat.ValorCobro;
					cob1.MontoMinimo = mat.ValorTopeMinimoOrden;
					cob1.MontoOperacionMonedaCobro = BigDecimal.ZERO;
					cob1.MontoCobroCalculado = BigDecimal.ZERO;
					cob1.MontoCobroCalculadoLocal = BigDecimal.ZERO;
					cob1.MontoCobroCalculadoLiquidacion = BigDecimal.ZERO;
					cob1.ValorCobroAplicado = cob1.ValorCobroPerfil;
					cob1.IdMonedaLiquidacionOrden = ord2.IdMonedaLiquidacionOrden;
					ord2.cobros.add(cob1);
					cobrosHash.put(cob1.IdInternoCobro, cob1);
				}
			}
				
			//cobros modificados y fijos
			if(ent.p_CobrosFijos.size()>0){
				for(Cobro_Fijo cobFijo : ent.p_CobrosFijos){
					Orden_Proceso ord2 = ordenesHash.get(cobFijo.IdInterno); 
					Cobro_Proceso cob1 = new Cobro_Proceso();
					
					cob1.IdOrigenCobro = "PORTAFOLIO";
					cob1.IdInterno =cobFijo.IdInterno;
					cob1.IdInternoCobro = IdInternoCobro++;
					cob1.IdMonedaLiquidacionOrden =cobFijo.IdMoneda;
					cob1.MontoOperacionMonedaCobro = BigDecimal.ZERO;
					cob1.IdCobro = cobFijo.IdCobro;
					cob1.CodigoCobroClasificacion=cobFijo.CodigoCobroClasificacion;
					cob1.IdMoneda=cobFijo.IdMoneda;
					cob1.NombreCobro="COMISION VARIABLE";
					cob1.ValorCobroPerfil = cobFijo.ValorCobro;
					cob1.PermiteCambiarValor=cobFijo.PermiteCambiarValor;
					cob1.FormaCobro=cobFijo.FormaCobro;
					cob1.MontoMinimo= BigDecimal.ZERO;
					cob1.ValorCobroAplicado = cobFijo.ValorCobro;
					cob1.MontoCobroCalculado = BigDecimal.ZERO;
					cob1.MontoCobroCalculadoLocal = BigDecimal.ZERO;
					cob1.MontoCobroCalculadoLiquidacion = BigDecimal.ZERO;
					
					ord2.cobros.add(cob1);
					cobrosHash.put(cob1.IdInternoCobro, cob1);
				}
				
				for (Cobro_Fijo CobroFijo : ent.p_CobrosFijos) {
					Orden_Proceso ord = (Orden_Proceso) ordenesHash.get(CobroFijo.IdInterno);
					for (Cobro_Proceso cobroOrden : ord.cobros) {
						if (cobroOrden.IdCobro.equalsIgnoreCase(CobroFijo.IdCobro)) {
							cobroOrden.ValorCobroAplicado = CobroFijo.ValorCobro;
						}
					}
				}
			}else{
				for (Cobro_Entrada cobroModificado : ent.p_CobrosModificados) {
					Orden_Proceso ord = (Orden_Proceso) ordenesHash.get(cobroModificado.IdInterno);
					for (Cobro_Proceso cobroOrden : ord.cobros) {
						if (cobroOrden.IdCobro.equalsIgnoreCase(cobroModificado.IdCobro)) {
							cobroOrden.ValorCobroAplicado = cobroModificado.ValorCobroAplicado;
						}
					}
				}
			}
			
			
		}
		
		//COBROS PERU
		HashMap<Long,DefinicionCobro_Proceso> cobrosPeruHash = new HashMap<>();
		if (localizacionSistema.equalsIgnoreCase("PE")) {
		
			ConsultarCobrosOrdenesPe serviceConsultarCobrosOrdenes = new ConsultarCobrosOrdenesPe();
			ConsultarCobrosOrdenesPe_Entrada entradaConsultarCobrosOrdenes = new ConsultarCobrosOrdenesPe_Entrada();
			entradaConsultarCobrosOrdenes.p_IdEmpresa = ent.p_IdEmpresa;
			entradaConsultarCobrosOrdenes.p_IdModulo = ent.p_IdModulo;
			entradaConsultarCobrosOrdenes.p_IdUsuario = ent.p_IdUsuario;
			
			for (Orden_Proceso ord: ordenes) {
				InformacionOrden_Entrada op = new InformacionOrden_Entrada();
				op.EsAPlazo = ord.EsAPlazo;
				if (op.EsAPlazo == null)
					op.EsAPlazo = "N";
				op.IdBolsa = ord.IdBolsa;
				op.IdCorredoraCorresponsal = ord.IdCorredorCorresponsal;
				op.IdEmisor = ord.IdEmisor;
				op.IdInstrumento = ord.IdInstrumento;
				op.IdInterno = ord.IdInterno;
				op.IdPortafolio = ord.IdPortafolio;
				op.IdTipoCanalIngreso = ord.IdTipoCanalIngreso;
				op.IdTipoOperacion = ord.IdTipoOperacion;
				op.IdTipoProducto = ord.IdTipoProducto;
				entradaConsultarCobrosOrdenes.p_Ordenes.add(op );
			}
			
			ConsultarCobrosOrdenesPe_Salida salidaConsultarCobrosOrdenes = serviceConsultarCobrosOrdenes.logica(conn, entradaConsultarCobrosOrdenes);
			if (salidaConsultarCobrosOrdenes.p_out_Cobros.size() == 0) {
				String codigo = "PORTAFOLIO_SIN_COBROS_CONFIGURADOS";
				String msg = Diccionario.getMensaje(conn, ent.p_IdEmpresa, codigo).msg();
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,codigo,msg));
				return salida;
			}
			
			Long IdInternoCobro = 0L;
			for (DefinicionCobro_Salida def : salidaConsultarCobrosOrdenes.p_out_Cobros) {
				Orden_Proceso ord = ordenesHash.get(def.IdInterno);
				//Utils.copyPojo(def, definicionCobroProceso);
				DefinicionCobro_Proceso definicionCobroProceso = copyPojoDefinicionCobroProceso(def);
				definicionCobroProceso.IdInternoCobro = IdInternoCobro++;
				definicionCobroProceso.IdOrigenCobro = def.IdOrigenCobro;
				ord.definicionCobros.add(definicionCobroProceso);
				cobrosPeruHash.put(definicionCobroProceso.IdInternoCobro, definicionCobroProceso);
			}
			
			//cobros modificados
			for (Cobro_Entrada cobroModificado : ent.p_CobrosModificados) {
				Orden_Proceso ord = (Orden_Proceso) ordenesHash.get(cobroModificado.IdInterno);
				if (ord == null) {
					continue;
				}
				for (DefinicionCobro_Proceso cobroOrden : ord.definicionCobros) {
					if (cobroOrden.CodigoDefinicionCobro.equalsIgnoreCase(cobroModificado.IdCobro)) {
						cobroOrden.ValorCobroAplicado = cobroModificado.ValorCobroAplicado;
					}
				}
			}
		}
		
		//ORDEN POR MONTO
		{
			for (Orden_Proceso ord : ordenes) {
				if (ord.EsPorMonto.equalsIgnoreCase("S") && localizacionSistema.equalsIgnoreCase("PE"))  {
					ord.CantidadOrden = ord.MontoTotal.divide(ord.PrecioOrden,0,RoundingMode.FLOOR);
				}
				if (ord.EsPorMonto.equalsIgnoreCase("S") && localizacionSistema.equalsIgnoreCase("CL"))  {
					
					//llevamos precio orden a moneda de liquidacion
					BigDecimal precioOrdenMonedaLiquidacion = BigDecimal.ZERO;
					{
						idTrabajo++;
					
						CalcularParidadEntrada calcularParidadEntradaPrecioOrden = new  CalcularParidadEntrada();
						calcularParidadEntradaPrecioOrden.p_IdEmpresa = ent.p_IdEmpresa;
						calcularParidadEntradaPrecioOrden.p_IdTrabajo = idTrabajo;
						
						CalculoEntrada calculoPrecioOrden = new CalculoEntrada();
						calculoPrecioOrden.Fecha = fechaSistema;
						calculoPrecioOrden.IdInterno = 0L;
						calculoPrecioOrden.IdMonedaDestino = ord.IdMonedaLiquidacionOrden;
						calculoPrecioOrden.IdMonedaOrigen = ord.IdMonedaOrden;
						calculoPrecioOrden.IdTrabajo = idTrabajo;
						calculoPrecioOrden.IdUso = obtenerIdUsoPorIdTipoOperacion(ord.IdTipoOperacion,localizacionSistema,ord.IdBolsa);

						calculoPrecioOrden.MontoOrigen = ord.PrecioOrden;
						calculoPrecioOrden.ValorCambioLocalModificado = BigDecimal.ZERO;
						calcularParidadEntradaPrecioOrden.p_CalculosEntrada.add(calculoPrecioOrden);
						
						CalcularParidad calcularParidadService = new CalcularParidad();
						CalcularParidadSalida calcularParidadSalidaPrecioOrden = calcularParidadService.logica(conn, calcularParidadEntradaPrecioOrden);
						
						precioOrdenMonedaLiquidacion = calcularParidadSalidaPrecioOrden.p_out_CalculosSalida.get(0).MontoDestino;
					}
					
					//suma de cobros
					BigDecimal sumaVariables = BigDecimal.ZERO;
					BigDecimal sumaFijos = BigDecimal.ZERO;
					BigDecimal sumaImpuestos = BigDecimal.ZERO;
					{
						
						for (Cobro_Proceso cob : ord.cobros) {
							
							if (cob.CodigoCobroClasificacion.equalsIgnoreCase("IMPUESTO")) {
								sumaImpuestos = sumaImpuestos.add(cob.ValorCobroPerfil.divide(new BigDecimal(100)));
							}
							else if (cob.FormaCobro.equalsIgnoreCase("V")) {
								if (cob.PermiteCambiarValor.equalsIgnoreCase("S"))
									sumaVariables = sumaVariables.add(cob.ValorCobroAplicado.divide(new BigDecimal(100)));
								else
									sumaVariables = sumaVariables.add(cob.ValorCobroPerfil.divide(new BigDecimal(100)));
							}
							else if (cob.FormaCobro.equalsIgnoreCase("F")) {
								
								BigDecimal valorCobroFijo = BigDecimal.ZERO;
								if (cob.PermiteCambiarValor.equalsIgnoreCase("S"))
									valorCobroFijo = cob.ValorCobroAplicado==null ? BigDecimal.ZERO : cob.ValorCobroAplicado;
								else
									valorCobroFijo = cob.ValorCobroPerfil==null ? BigDecimal.ZERO : cob.ValorCobroPerfil;
								
								//llevamos monto de cobro a moneda de liquidacion
								{
									idTrabajo++;
									CalcularParidadEntrada calcularParidadEntradaMontoOrdenMonedaCobro = new  CalcularParidadEntrada();
									calcularParidadEntradaMontoOrdenMonedaCobro.p_IdEmpresa = ent.p_IdEmpresa;
									calcularParidadEntradaMontoOrdenMonedaCobro.p_IdTrabajo = idTrabajo;
									
									CalculoEntrada calculoCobro = new CalculoEntrada();
									calculoCobro.Fecha = fechaSistema;
									calculoCobro.IdInterno = cob.IdInternoCobro;
									calculoCobro.IdMonedaDestino = ord.IdMonedaLiquidacionOrden;
									calculoCobro.IdMonedaOrigen = cob.IdMoneda;
									calculoCobro.IdTrabajo = idTrabajo;
									calculoCobro.IdUso = obtenerIdUsoPorIdTipoOperacion(ord.IdTipoOperacion,localizacionSistema,ord.IdBolsa);
									calculoCobro.MontoOrigen = valorCobroFijo;
									calculoCobro.ValorCambioLocalModificado = BigDecimal.ZERO;
									calcularParidadEntradaMontoOrdenMonedaCobro.p_CalculosEntrada.add(calculoCobro);
									
									CalcularParidad calcularParidadService = new CalcularParidad();
									CalcularParidadSalida calcularParidadSalidaMontoOrdenMonedaCobro = calcularParidadService.logica(conn, calcularParidadEntradaMontoOrdenMonedaCobro);
									
									sumaFijos = sumaFijos.add(calcularParidadSalidaMontoOrdenMonedaCobro.p_out_CalculosSalida.get(0).MontoDestino);
								}
							}
						}
					}
					BigDecimal arriba =BigDecimal.ZERO;
					BigDecimal abajo =BigDecimal.ZERO;
					
					// Get puntas venta y compra
					ConsultarInstrumentoPrecioBolsa_Entrada objConsultaPrecio_Entrada = new ConsultarInstrumentoPrecioBolsa_Entrada();
					ConsultarInstrumentoPrecioBolsa objConsultaPrecio = new ConsultarInstrumentoPrecioBolsa();
					
					ConsultarInstrumentoPrecioBolsa.Instrumento objInstrumento = new ConsultarInstrumentoPrecioBolsa.Instrumento();
					
					objInstrumento.Fecha = ord.FechaOperacion.substring(0, 10);
					objInstrumento.IdInstrumento = ord.IdInstrumento;
					objInstrumento.IdEmpresa = ent.p_IdEmpresa;
				
					objConsultaPrecio_Entrada.p_Instrumentos.add(objInstrumento);
					
					ConsultarInstrumentoPrecioBolsa_Salida objConsultaPrecio_Salida;
					
					BigDecimal puntaCompra =BigDecimal.ZERO;
					BigDecimal puntaVenta =BigDecimal.ZERO;
					BigDecimal precioCierre = BigDecimal.ZERO;
					
					try {
						
						objConsultaPrecio_Salida = objConsultaPrecio.logica(conn, objConsultaPrecio_Entrada);
						 puntaCompra =objConsultaPrecio_Salida.P_OUT_INSTRUMENTO.get(0).PuntaCompra == null ? BigDecimal.ZERO : objConsultaPrecio_Salida.P_OUT_INSTRUMENTO.get(0).PuntaCompra ;
						 puntaVenta =objConsultaPrecio_Salida.P_OUT_INSTRUMENTO.get(0).PuntaVenta == null ? BigDecimal.ZERO : objConsultaPrecio_Salida.P_OUT_INSTRUMENTO.get(0).PuntaVenta ;
						 precioCierre = objConsultaPrecio_Salida.P_OUT_INSTRUMENTO.get(0).PrecioCierre == null ? BigDecimal.ZERO : objConsultaPrecio_Salida.P_OUT_INSTRUMENTO.get(0).PrecioCierre ;
					 
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					
					if (ord.AccionOrden.equalsIgnoreCase("COMPRA")) {			
						if(parametroCalculoPrecioPunta.equalsIgnoreCase("S")){
							BigDecimal precio = precioCierre.compareTo(puntaVenta) == 1 ? precioCierre : puntaVenta;
							
							if(precio.compareTo(BigDecimal.ZERO) == 0)
								ord.CantidadOrden = new BigDecimal(Math.floor((ord.MontoTotal.divide(precioCierre,20,RoundingMode.HALF_UP).doubleValue())));
							else	
								ord.CantidadOrden = new BigDecimal(Math.floor((ord.MontoTotal.divide(precio,20,RoundingMode.HALF_UP).doubleValue())));
							
						}
						else {
						    final BigDecimal UNO = BigDecimal.ONE;
						    final BigDecimal k = UNO.add(sumaVariables.multiply(UNO.add(sumaImpuestos)));  // 1 + V*(1+IVA)
						    final BigDecimal fijoBruto = sumaFijos.multiply(UNO.add(sumaImpuestos));       // F*(1+IVA)
						    final BigDecimal precio = precioOrdenMonedaLiquidacion;                        // alias final
						    final BigDecimal var = sumaVariables;
						    final BigDecimal imp = sumaImpuestos;
						    final BigDecimal fijos = sumaFijos;
						    
						    // Q_floor = floor((T - F_bruto) / (P*k))
						    final BigDecimal numerador   = ord.MontoTotal.subtract(fijoBruto);
						    final BigDecimal denominador = precio.multiply(k);
						    BigDecimal q = numerador.divideToIntegralValue(denominador);
						    if (q.signum() < 0) q = BigDecimal.ZERO; // seguridad

						    java.util.function.Function<BigDecimal, BigDecimal> totalForward = Q -> {
						        BigDecimal subtotal = Q.multiply(precio).setScale(0, RoundingMode.HALF_UP);
						        BigDecimal comVar = subtotal.multiply(var).setScale(0, RoundingMode.HALF_UP);
						        BigDecimal ivaFijo = fijos.multiply(imp).setScale(0, RoundingMode.HALF_UP);
						        BigDecimal ivaComVar = comVar.multiply(imp).setScale(0, RoundingMode.HALF_UP);
						        BigDecimal total = subtotal.add(fijos).add(ivaFijo).add(comVar).add(ivaComVar);

						        return total.setScale(0, RoundingMode.HALF_UP);
						    };

						    // Intentar subir a Q+1 SOLO si no nos pasamos del monto total
						    BigDecimal totalConQPlus = totalForward.apply(q.add(BigDecimal.ONE));
						    if (totalConQPlus.compareTo(ord.MontoTotal) <= 0) {
						        q = q.add(BigDecimal.ONE);
						    }
						    ord.CantidadOrden = q;
						}
					}
					else if (ord.AccionOrden.equalsIgnoreCase("VENTA")) {
						if(parametroCalculoPrecioPunta.equalsIgnoreCase("S")){
							BigDecimal precio = precioCierre.compareTo(puntaCompra) == -1 ? precioCierre : puntaCompra;
							if(precio.compareTo(BigDecimal.ZERO) == 0)
								ord.CantidadOrden = new BigDecimal(Math.floor((ord.MontoTotal.divide(precioCierre,20,RoundingMode.HALF_UP).doubleValue())));
							else	
								ord.CantidadOrden = new BigDecimal(Math.floor((ord.MontoTotal.divide(precio,20,RoundingMode.HALF_UP).doubleValue())));
						}
						else {
							final BigDecimal UNO = BigDecimal.ONE;
							final BigDecimal precio = precioOrdenMonedaLiquidacion;   // P
							final BigDecimal var    = sumaVariables;                  // V (comision variable)
							final BigDecimal imp    = sumaImpuestos;                  // IVA
							final BigDecimal fijos  = sumaFijos;                      // F
							final BigDecimal a         = UNO.subtract(var.multiply(UNO.add(imp))); // 1 - V*(1+IVA)
							final BigDecimal fijoBruto = fijos.multiply(UNO.add(imp));             // F*(1+IVA)

							// ---------------------------------------------
							// Q_floor = floor( (N + F_bruto) / (P * a) )
							// ---------------------------------------------
							final BigDecimal numerador   = ord.MontoTotal.add(fijoBruto);     // N + F_bruto
							final BigDecimal denominador = precio.multiply(a);                // P * a

							BigDecimal q = numerador.divideToIntegralValue(denominador);
							if (q.signum() < 0) q = BigDecimal.ZERO; // seguridad
							java.util.function.Function<BigDecimal, BigDecimal> netoRecibido = Q -> {
							    BigDecimal subtotal = Q.multiply(precio);
							    BigDecimal neto     = subtotal.multiply(a).subtract(fijoBruto).setScale(0, RoundingMode.DOWN);
							    return neto;
							};
							
							// Intentar subir a Q+1 SOLO si no se pasa del monto neto disponible
							BigDecimal netoConQPlus = netoRecibido.apply(q.add(BigDecimal.ONE));
							if (netoConQPlus.compareTo(ord.MontoTotal) <= 0) {
							    q = q.add(BigDecimal.ONE);
							}
							ord.CantidadOrden = q; 				
						}
					}
					
					if (ord.CantidadOrden.compareTo(BigDecimal.ZERO) == -1) {
						ord.CantidadOrden = BigDecimal.ZERO;
					}
				}
			}
		}
		
		
		//CORRECCION DE CANTIDADES, MONTOS, VALOR RESCATE
		for (Orden_Proceso ord: ordenes) {
			
			if (instrumentos.get(ord.IdInstrumento).IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_RV)) {
				ord.ValorRescateOrden = BigDecimal.ZERO;
				if (ord.IdTipoOperacion.equalsIgnoreCase(_c.TIPO_OPERACION_COMSIM) && ord.IdTipoCanalIngreso.equalsIgnoreCase(_c.TIPO_CANAL_INGRESO_AGENTE)) {
					ord.CantidadOrden = new BigDecimal(1);
				}
				
				if (!(ord.IdTipoOperacion.equalsIgnoreCase(_c.TIPO_OPERACION_COMSIM) && ord.IdTipoCanalIngreso.equalsIgnoreCase(_c.TIPO_CANAL_INGRESO_AGENTE))) {
					if(ord.CantidadOrden.compareTo(BigDecimal.ZERO) != 0) {
						ord.MontoOrden = ord.CantidadOrden.multiply(ord.PrecioOrden);
					}
					
				}
			}
			else if (instrumentos.get(ord.IdInstrumento).IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_DV)) {
				ord.ValorRescateOrden = BigDecimal.ZERO;
				ord.MontoOrden = ord.CantidadOrden.multiply(ord.PrecioOrden).multiply(new BigDecimal(instrumentos.get(ord.IdInstrumento).Tamano));
			}	
			else if (instrumentos.get(ord.IdInstrumento).IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_RF)) {
				ord.ValorRescateOrden = BigDecimal.ZERO;
			}
			else if (instrumentos.get(ord.IdInstrumento).IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_IF)) {
				if (ord.EsFungible.equalsIgnoreCase(_c.SI)) {
					ord.CantidadOrden = new BigDecimal(1);
				}
			}
			else if (instrumentos.get(ord.IdInstrumento).IdClasificacionTipoInstrumento.equalsIgnoreCase(_c.CLASIFICACION_RE)) {
				ord.ValorRescateOrden = BigDecimal.ZERO;
				/*if (ord.IdTipoOperacion.equalsIgnoreCase(_c.TIPO_OPERACION_COMRRV)) {
					ord.CantidadOrden = new BigDecimal(1);
				}
				if (ord.IdTipoOperacion.equalsIgnoreCase(_c.TIPO_OPERACION_COMRRF)) {
					ord.CantidadOrden = new BigDecimal(1);
				}*/
				
				if (ord.IdTipoOperacion.equalsIgnoreCase(_c.TIPO_OPERACION_VENRRV)) {
					ord.MontoOrden = ord.CantidadOrden.multiply(ord.PrecioOrden.setScale(2, RoundingMode.HALF_UP));
				}
				
			}
			
			ord.MontoOrden = MonedaFormato.formatear(conn, ord.MontoOrden, MonedaFormato.MONTO, ord.IdMonedaOrden).valor;
			
		}
		
		{//Valorizacion para Rf PE
			//buscar dia siguiente
			ObtenerDiaSiguiente diaSgte = new ObtenerDiaSiguiente();
			InObtenerDiaSiguiente datDia = new InObtenerDiaSiguiente();
			datDia.p_idPais = idPaisEmpresa;
			datDia.p_diaHabil = "N";
			for (Orden_Proceso ord: ordenes) {
				if (ord.IdTipoOperacion.equalsIgnoreCase(_c.TIPO_OPERACION_VENRRF) || ord.IdTipoOperacion.equalsIgnoreCase(_c.TIPO_OPERACION_COMRRF)){
					FechasIn fechas = new FechasIn();
					fechas.fechaInicial = ord.FechaOperacion;
					fechas.numeroDias = ord.PlazoReporte;
					datDia.p_fechas.add(fechas);	
				}
				
			}
			
			if (datDia.p_fechas.size() > 0) {
				OutObtenerDiaSiguiente salDiaSgte = diaSgte.logica(conn, datDia);
				
				ValorizarInstrumento val = new ValorizarInstrumento();
				ValorizarInstrumentoEntrada valInCon = new ValorizarInstrumentoEntrada();
				ValorizarInstrumentoEntrada valInPla = new ValorizarInstrumentoEntrada();
				valInCon.p_IdEmpresa 	= ent.p_IdEmpresa;		
				valInCon.p_IdUsuario 	= ent.p_IdUsuario;
				valInCon.p_IdModulo	= ent.p_IdModulo;
				valInCon.p_IdPais 		= idPaisEmpresa;
				valInCon.p_OrigenProcesamiento = "MONTO_COBRO_ORDEN";	//legacy(se usara para loguear)
				
				//Utils.copyPojo(valInCon, valInPla);
				valInPla.p_IdEmpresa = valInCon.p_IdEmpresa;
				valInPla.p_IdUsuario = valInCon.p_IdUsuario;
				valInPla.p_IdModulo = valInCon.p_IdModulo;
				valInPla.p_IdPais = valInCon.p_IdPais;
				valInPla.p_IdMonedaAdicional = valInCon.p_IdMonedaAdicional;
				valInPla.p_Instrumentos = new ArrayList<ObjEntradaInstrumentos>(valInCon.p_Instrumentos);
				valInPla.p_OrigenProcesamiento = valInCon.p_OrigenProcesamiento;
				
				for (Orden_Proceso ord: ordenes) {
					if (ord.IdTipoOperacion.equalsIgnoreCase(_c.TIPO_OPERACION_VENRRF) || ord.IdTipoOperacion.equalsIgnoreCase(_c.TIPO_OPERACION_COMRRF)){
						ObjEntradaInstrumentos objCont = new ObjEntradaInstrumentos();
						objCont.IdValorizacion 					= ord.IdInterno;//new Long(i);
						objCont.FechaOperacion					= ord.FechaOperacion;
						for (FechasOut fec : salDiaSgte.p_out_fechas){
							if (fec.fechaInicial.equals(ord.FechaOperacion) && 
								fec.numeroDiasInicial.equals(ord.PlazoReporte)){
								objCont.FechaVencimientoOperacion 		= fec.fechaCalculada;;//ord.,
							}
						}
						String key = ord.IdInstrumentoSubyacente == null ? ord.IdInstrumento.toString() : ord.IdInstrumentoSubyacente.toString();
						objCont.PlazoEnDiasOperacion 			= ord.PlazoReporte; //Solo para operaciones a plazo
						objCont.FechaValorizacion				= ord.FechaOperacion; 		
						objCont.IdClasificacionTipoInstrumento	= ((CalcularCobroYMontosOrden_Instrumento) _instrumentosHash.get(key)).IdClasificacionTipoInstrumento; 
						objCont.IdTipoInstrumento				= ((CalcularCobroYMontosOrden_Instrumento) _instrumentosHash.get(key)).IdTipoInstrumento; 
						objCont.IdInstrumento					= ((CalcularCobroYMontosOrden_Instrumento) _instrumentosHash.get(key)).IdInstrumento; 
						objCont.FechaVencimiento				= null;
						objCont.FechaEmision					= null;
						objCont.IdMonedaInstrumento				= ((CalcularCobroYMontosOrden_Instrumento) _instrumentosHash.get(key)).IdMoneda;
						objCont.IdTipoReajusteInstrumento		= ((CalcularCobroYMontosOrden_Instrumento) _instrumentosHash.get(key)).IdTipoReajuste;
						objCont.Cantidad						= ord.CantidadOrden; 
						objCont.ValorRescate					= null;
						objCont.PrecioCompra					= ord.PrecioOrden;
						objCont.TasaCompra						= null;
						objCont.MontoCompra						= null;//en moneda del instrumento		
						objCont.PrecioSubyacente				= null; 
						objCont.TasaSubyacente					= null; 
						objCont.PrecioGarantia					= null; 
						objCont.TasaGarantia					= null; 
						objCont.TasaTIF							= null;		
						objCont.IdPortafolio					= null;
						objCont.TablaOrigen						= null;	
						objCont.IdUso							= null; 
						objCont.TipoCartera						= null; 
						objCont.IdTipoCriterioTasa				= null;
						
						//Utils.copyPojo(objCont, objPla);
						ObjEntradaInstrumentos objPla = copyPojoObjEntradaInstrumentos(objCont);
						objPla.FechaVencimientoOperacion 		= null;//ord.,
						for (FechasOut fec : salDiaSgte.p_out_fechas){
							if (fec.fechaInicial.equals(ord.FechaOperacion) && 
								fec.numeroDiasInicial.equals(ord.PlazoReporte)){
								objPla.FechaValorizacion 		= fec.fechaCalculada;;//ord.,
							}
						}
						
						valInCon.p_Instrumentos.add(objCont);
						valInPla.p_Instrumentos.add(objPla);						
					}
				}
				
				if (valInCon.p_Instrumentos.size() > 0){
					ValorizarInstrumentoSalida valOut = val.logica(conn, valInCon);
					salida.P_OUT_MENSAJES.addAll(valOut.P_OUT_MENSAJES);
					for (ObjResultadoValorizacion inVal : valOut.P_OUT_INSTRUMENTOSVALORIZADOS){
						if (inVal.DimensionTasa.equalsIgnoreCase("COMP") && inVal.DimensionTiempo.equalsIgnoreCase("AC")){
							//ordenesHash.get(inVal.IdValorizacion).MontoOrden 			= inVal.MontoCapitalCorteCuponMonedaInstrumento;
							ordenesHash.get(inVal.IdValorizacion).MontoInteresContado 	= inVal.MontoInteresCorteCuponMonedaInstrumento;
							//ordenesHash.get(inVal.IdValorizacion).MontoOperacionPlazo 	= inVal.MontoAPlazoMonedaInstrumento;
							//ordenesHash.get(inVal.IdValorizacion).MontoInteresPlazo 	= inVal.MontoInteresesPagoCorteCupon;
						}
					}
				}
				if (valInPla.p_Instrumentos.size() > 0){
					ValorizarInstrumentoSalida valOutPla = val.logica(conn, valInPla);
					salida.P_OUT_MENSAJES.addAll(valOutPla.P_OUT_MENSAJES);
					for (ObjResultadoValorizacion inVal : valOutPla.P_OUT_INSTRUMENTOSVALORIZADOS){
						if (inVal.DimensionTasa.equalsIgnoreCase("COMP") && inVal.DimensionTiempo.equalsIgnoreCase("AC")){
							//ordenesHash.get(inVal.IdValorizacion).MontoOrden 			= inVal.MontoCapitalCorteCuponMonedaInstrumento;
							//ordenesHash.get(inVal.IdValorizacion).MontoInteresContado 	= inVal.MontoInteresCorteCuponMonedaInstrumento;
							//ordenesHash.get(inVal.IdValorizacion).MontoOperacionPlazo 	= inVal.MontoAPlazoMonedaInstrumento;
							ordenesHash.get(inVal.IdValorizacion).MontoInteresPlazo 	= inVal.MontoInteresCorteCuponMonedaInstrumento;
						}
					}
				}
			}
		}
		
		//PARA OPERACIONES A PLAZO SIM, REPORTES
		{
			for (Orden_Proceso ordenProceso: ordenes) {
				if (ordenProceso.TipoInteres!= null && ordenProceso.TipoInteres.equalsIgnoreCase(_c.INTERES_SIMPLE) && ordenProceso.IdTipoOperacion.equalsIgnoreCase(_c.TIPO_OPERACION_VENSIM)) {
					BigDecimal factor = ordenProceso.TasaSimultanea.multiply(new BigDecimal(ordenProceso.PlazoSimultanea)).divide(new BigDecimal(ordenProceso.BaseTasa),20,RoundingMode.HALF_UP).divide(new BigDecimal(100),20,RoundingMode.HALF_UP);
					ordenProceso.PrecioPlazo = ordenProceso.PrecioOrden.add(ordenProceso.PrecioOrden.multiply(factor));
					ordenProceso.MontoOperacionPlazo = ordenProceso.CantidadOrden.multiply(ordenProceso.PrecioPlazo);
				}
				else if (ordenProceso.TipoInteres!= null && ordenProceso.TipoInteres.equalsIgnoreCase(_c.INTERES_SIMPLE) && ordenProceso.IdTipoOperacion.equalsIgnoreCase(_c.TIPO_OPERACION_COMSIM)) {
					ordenProceso.PrecioPlazo = BigDecimal.ZERO;;
					BigDecimal factor = ordenProceso.TasaSimultanea.multiply(new BigDecimal(ordenProceso.PlazoSimultanea)).divide(new BigDecimal(ordenProceso.BaseTasa),20,RoundingMode.HALF_UP).divide(new BigDecimal(100),20,RoundingMode.HALF_UP);
					ordenProceso.MontoOperacionPlazo = ordenProceso.MontoOrden.add(ordenProceso.MontoOrden.multiply(factor));
				}
				else if (ordenProceso.TipoInteres!= null && ordenProceso.TipoInteres.equalsIgnoreCase(_c.INTERES_COMPUESTO) && (ordenProceso.IdTipoOperacion.equalsIgnoreCase(_c.TIPO_OPERACION_VENRRV) || ordenProceso.IdTipoOperacion.equalsIgnoreCase(_c.TIPO_OPERACION_COMRRV))) {
					BigDecimal tasa = ordenProceso.TasaReporte.divide(new BigDecimal(100),20,RoundingMode.HALF_UP);
					BigDecimal periodos = new BigDecimal(ordenProceso.PlazoReporte).divide(new BigDecimal(ordenProceso.BaseTasa),20,RoundingMode.HALF_UP);
					Double factor = Math.pow(BigDecimal.ONE.add(tasa).doubleValue(),periodos.doubleValue());
					/*ordenProceso.PrecioPlazo = ordenProceso.PrecioOrden.multiply(new BigDecimal(factor));
					ordenProceso.MontoOperacionPlazo = ordenProceso.CantidadOrden.multiply(ordenProceso.PrecioPlazo);*/
					ordenProceso.MontoOperacionPlazo = ordenProceso.MontoOrden.multiply(new BigDecimal(factor));
					ordenProceso.PrecioPlazo = ordenProceso.MontoOperacionPlazo.divide(ordenProceso.CantidadOrden,20,RoundingMode.HALF_UP);
				}
				else if (ordenProceso.TipoInteres!= null && ordenProceso.TipoInteres.equalsIgnoreCase(_c.INTERES_COMPUESTO) && (ordenProceso.IdTipoOperacion.equalsIgnoreCase(_c.TIPO_OPERACION_VENRRF) || ordenProceso.IdTipoOperacion.equalsIgnoreCase(_c.TIPO_OPERACION_COMRRF))) {
					BigDecimal tasa = ordenProceso.TasaReporte.divide(new BigDecimal(100),20,RoundingMode.HALF_UP);
					BigDecimal periodos = new BigDecimal(ordenProceso.PlazoReporte).divide(new BigDecimal(ordenProceso.BaseTasa),20,RoundingMode.HALF_UP);
					BigDecimal montoMasInteres = ordenProceso.MontoOrden.add(ordenProceso.MontoInteresContado);
					Double factor = Math.pow(BigDecimal.ONE.add(tasa).doubleValue(),periodos.doubleValue());
					BigDecimal montoMasInteresFactor = montoMasInteres.multiply(new BigDecimal(factor));
					ordenProceso.PrecioPlazo = (montoMasInteresFactor.subtract(ordenProceso.MontoInteresPlazo)).divide(ordenProceso.CantidadOrden,8,RoundingMode.HALF_UP);
					ordenProceso.MontoOperacionPlazo = ordenProceso.PrecioPlazo.multiply(ordenProceso.CantidadOrden);
				}/*
				else if (ordenProceso.TipoInteres!= null && ordenProceso.TipoInteres.equalsIgnoreCase(_c.INTERES_COMPUESTO) && (ordenProceso.IdTipoOperacion.equalsIgnoreCase(_c.TIPO_OPERACION_COMRRV) || ordenProceso.IdTipoOperacion.equalsIgnoreCase(_c.TIPO_OPERACION_COMRRF))) {
					BigDecimal tasa = ordenProceso.TasaReporte.divide(new BigDecimal(100),20,RoundingMode.HALF_UP);
					BigDecimal periodos = new BigDecimal(ordenProceso.PlazoReporte).divide(new BigDecimal(ordenProceso.BaseTasa),20,RoundingMode.HALF_UP);
					Double factor = Math.pow(BigDecimal.ONE.add(tasa).doubleValue(),periodos.doubleValue());
					ordenProceso.PrecioPlazo = BigDecimal.ZERO;
					ordenProceso.MontoOperacionPlazo = ordenProceso.MontoOrden.multiply(new BigDecimal(factor));
				}*/
				else {
					ordenProceso.PrecioPlazo = BigDecimal.ZERO;
					ordenProceso.MontoOperacionPlazo = BigDecimal.ZERO;
				}

				ordenProceso.MontoOperacionPlazo = MonedaFormato.formatear(conn, ordenProceso.MontoOperacionPlazo, MonedaFormato.MONTO, ordenProceso.IdMonedaOrden).valor;
			}
		}
		
		//CALCULAR COBROS CHILE
		if (localizacionSistema.equalsIgnoreCase("CL")) 
		{
			//MONTO ORDEN A MONEDA COBRO (CALCULO DE ORDENES CHILE REQUIERE QUE SE ENVIE EL MONTO DE LA ORDEN EN LA MONEDA DEL COBRO)
			idTrabajo++;
			CalcularParidadEntrada calcularParidadEntrada = new  CalcularParidadEntrada();
			calcularParidadEntrada.p_IdEmpresa = ent.p_IdEmpresa;
			calcularParidadEntrada.p_IdTrabajo = idTrabajo;
			
			for (Orden_Proceso ord : ordenes) {
				for (Cobro_Proceso cob : ord.cobros) {
					CalculoEntrada c1 = new CalculoEntrada();
					c1.Fecha = fechaSistema;
					c1.IdInterno = cob.IdInternoCobro;
					c1.IdMonedaDestino = cob.IdMoneda;
					c1.IdMonedaOrigen = ord.IdMonedaOrden;
					c1.IdTrabajo = idTrabajo;
					c1.IdUso = obtenerIdUsoPorIdTipoOperacion(ord.IdTipoOperacion,localizacionSistema,ord.IdBolsa);
					c1.MontoOrigen = ord.MontoOrden;
					c1.ValorCambioLocalModificado = ord.TipoCambio;
					calcularParidadEntrada.p_CalculosEntrada.add(c1);
				}
			}
			
			CalcularParidad calcularParidadService = new CalcularParidad();
			CalcularParidadSalida calcularParidadSalida = calcularParidadService.logica(conn, calcularParidadEntrada);
			
			for (CalculoSalida calc: calcularParidadSalida.p_out_CalculosSalida) {
				
				if (!calc.Resultado.equals("OK")) {
					String codigo = calc.CodigoMensaje;
					String desc = Diccionario.getMensaje(conn, ent.p_IdEmpresa,codigo)
							.add("IdMonedaOrigen", calc.IdMonedaOrigen)
							.add("IdMonedaDestino", calc.IdMonedaDestino)
							.msg();
					salida.P_OUT_MENSAJES.add(new MensajeSalida(calc.IdInterno,MensajeSalida.MENSAJE_ERROR,codigo,desc));
					return salida;
				}
				
				Cobro_Proceso cob = cobrosHash.get(calc.IdInterno);
				cob.MontoOperacionMonedaCobro = calc.MontoDestino;
			}
			
			
			
			for (Orden_Proceso ord : ordenes) {
				//EN CHILE LOS COBROS CON TASA Y PLAZO SON SOLO PARA SIMULTANEA
				CalcularCobrosEntrada calcularCobrosEntrada = new CalcularCobrosEntrada();
				calcularCobrosEntrada.DiasPlazo = ord.PlazoSimultanea;
				calcularCobrosEntrada.Tasa = ord.TasaSimultanea;
				for (Cobro_Proceso cob: ord.cobros) {
					CalcularCobros.CobroEntrada c1 = new CalcularCobros.CobroEntrada();
					//Utils.copyPojo(cob, c1);
					c1.IdInternoCobro = cob.IdInternoCobro;
					c1.MontoOperacionMonedaCobro = cob.MontoOperacionMonedaCobro;
					c1.IdCobro = cob.IdCobro;
					c1.ValorCobroPerfil = cob.ValorCobroPerfil;
					c1.MontoMinimo = cob.MontoMinimo;
					c1.CodigoCobroClasificacion = cob.CodigoCobroClasificacion;
					c1.PermiteCambiarValor = cob.PermiteCambiarValor;
					c1.ValorCobroAplicado = cob.ValorCobroAplicado;
					c1.FormaCobro = cob.FormaCobro;
					//c1.IdMonedaCobro = cob.IdMonedaCobro;
					c1.IdMonedaCobro = cob.IdMoneda;
					calcularCobrosEntrada.cobros.add(c1);
				}
				CalcularCobros calcularCobrosService = new CalcularCobros();
				CalcularCobrosSalida calcularCobrosSalida = calcularCobrosService.logica(conn,calcularCobrosEntrada);
				for (CalcularCobros.CobroSalida cobroSalida : calcularCobrosSalida.p_out_Cobros) {
					Cobro_Proceso cobroOrden = cobrosHash.get(cobroSalida.IdInternoCobro);
					cobroOrden.ValorCobroFinal = cobroSalida.ValorCobroFinal;
					cobroOrden.ValorCobroAplicado = cobroSalida.ValorCobroFinal;
					cobroOrden.MontoCobroCalculado = cobroSalida.MontoCobroCalculado;
					cobroOrden.MontoCobroCalculado = MonedaFormato.formatear(conn, cobroOrden.MontoCobroCalculado, MonedaFormato.MONTO, cobroOrden.IdMoneda).valor;
					
				}
				
			}
			
		}
		
		//MONTO ORDEN A MONEDA DE LIQUIDACION Y MONEDA LOCAL
		{
			idTrabajo++;
			CalcularParidadEntrada calcularParidadEntrada = new  CalcularParidadEntrada();
			calcularParidadEntrada.p_IdEmpresa = ent.p_IdEmpresa;
			calcularParidadEntrada.p_IdTrabajo = idTrabajo;
			
			for (Orden_Proceso ord : ordenes) {
				CalculoEntrada c1 = new CalculoEntrada();
				c1.Fecha = _c.ORIGEN_REFACTURACION.equalsIgnoreCase(ord.GlosaRefactura) && _c.CLASIFICACION_IF.equalsIgnoreCase(instrumentos.get(ord.IdInstrumento).IdClasificacionTipoInstrumento) ? ord.FechaOperacionOriginal : fechaSistema;
				c1.IdInterno = ord.IdInterno;
				c1.IdMonedaDestino = ord.IdTipoProducto.equalsIgnoreCase(_c.TIPO_PRODUCTO_RERV) || ord.IdTipoProducto.equalsIgnoreCase(_c.TIPO_PRODUCTO_RERF)?ord.IdMonedaOrden:ord.IdMonedaLiquidacionOrden;
				c1.IdMonedaOrigen = ord.IdMonedaOrden;
				c1.IdTrabajo = idTrabajo;
				c1.IdUso = obtenerIdUsoPorIdTipoOperacion(ord.IdTipoOperacion,localizacionSistema,ord.IdBolsa);
				c1.MontoOrigen = ord.MontoOrden;
				c1.ValorCambioLocalModificado = ord.TipoCambio;
				calcularParidadEntrada.p_CalculosEntrada.add(c1);
			
			}
			
			CalcularParidad calcularParidadService = new CalcularParidad();
			CalcularParidadSalida calcularParidadSalida = calcularParidadService.logica(conn, calcularParidadEntrada);
			
			for (CalculoSalida calc: calcularParidadSalida.p_out_CalculosSalida) {
				
				Orden_Proceso ord = ordenesHash.get(calc.IdInterno);
				
				if (!calc.Resultado.equals("OK")) {
					String codigo = calc.CodigoMensaje;
					String desc = Diccionario.getMensaje(conn, ent.p_IdEmpresa,codigo)
							.add("IdMonedaOrigen", calc.IdMonedaOrigen)
							.add("IdMonedaDestino", calc.IdMonedaDestino)
							.msg();
					salida.P_OUT_MENSAJES.add(new MensajeSalida(ord.IdInterno,MensajeSalida.MENSAJE_ERROR,codigo,desc));
					return salida;
				}
				if (ord.IdTipoProducto.equalsIgnoreCase(_c.TIPO_PRODUCTO_RERV)){
					ord.MontoOperacionLiquidacion = MonedaFormato.formatear(conn, calculaMontoParidad(ord.PrecioOrden, ord.CantidadOrden, calc.FactorOrigenDestino), MonedaFormato.MONTO, ord.IdMonedaLiquidacionOrden).valor;
				}
				else {
					ord.MontoOperacionLiquidacion = MonedaFormato.formatear(conn, calc.MontoDestino, MonedaFormato.MONTO, ord.IdMonedaLiquidacionOrden).valor;
				}
				ord.MontoOrden = ord.IdMonedaOrden.equals("COP") || ord.IdMonedaOrden.equals("PEN") ? ord.MontoOperacionLiquidacion : ord.MontoOrden;
				ord.MontoOperacionLocal = MonedaFormato.formatear(conn, calc.MontoLocal, MonedaFormato.MONTO, idMonedaSistema).valor;
			}
		}
		
		//MONTO ORDEN PLAZO A MONEDA DE LIQUIDACION Y MONEDA LOCAL
		{
			idTrabajo++;
			CalcularParidadEntrada calcularParidadEntrada = new  CalcularParidadEntrada();
			calcularParidadEntrada.p_IdEmpresa = ent.p_IdEmpresa;
			calcularParidadEntrada.p_IdTrabajo = idTrabajo;
			
			for (Orden_Proceso ord : ordenes) {
				if (ord.MontoOperacionPlazo.compareTo(BigDecimal.ZERO) == 1) {
					CalculoEntrada c1 = new CalculoEntrada();
					c1.Fecha = fechaSistema;
					c1.IdInterno = ord.IdInterno;
					c1.IdMonedaDestino = ord.IdTipoProducto.equalsIgnoreCase("RERV") || ord.IdTipoProducto.equalsIgnoreCase("RERF")?ord.IdMonedaOrden:ord.IdMonedaLiquidacionOrden;
					c1.IdMonedaOrigen = ord.IdMonedaOrden;
					c1.IdTrabajo = idTrabajo;
					c1.IdUso = obtenerIdUsoPorIdTipoOperacion(ord.IdTipoOperacion,localizacionSistema,ord.IdBolsa);
					c1.MontoOrigen = ord.MontoOperacionPlazo;
					c1.ValorCambioLocalModificado = ord.TipoCambio;
					calcularParidadEntrada.p_CalculosEntrada.add(c1);
				}
			}
			
			if (calcularParidadEntrada.p_CalculosEntrada.size() > 0) {
				CalcularParidad calcularParidadService = new CalcularParidad();
				CalcularParidadSalida calcularParidadSalida = calcularParidadService.logica(conn, calcularParidadEntrada);
				
				for (CalculoSalida calc: calcularParidadSalida.p_out_CalculosSalida) {
					
					Orden_Proceso ord = ordenesHash.get(calc.IdInterno);
				
					if (!calc.Resultado.equals("OK")) {
						String codigo = calc.CodigoMensaje;
						String desc = Diccionario.getMensaje(conn, ent.p_IdEmpresa,codigo)
								.add("IdMonedaOrigen", calc.IdMonedaOrigen)
								.add("IdMonedaDestino", calc.IdMonedaDestino)
								.msg();
						salida.P_OUT_MENSAJES.add(new MensajeSalida(ord.IdInterno,MensajeSalida.MENSAJE_ERROR,codigo,desc));
					}
					else {
						if (ord.IdTipoProducto.equalsIgnoreCase(_c.TIPO_PRODUCTO_RERV)){
							ord.MontoOperacionLiquidacionPlazo = MonedaFormato.formatear(conn, calculaMontoParidad(ord.PrecioOrden, ord.CantidadOrden, calc.FactorOrigenDestino), MonedaFormato.MONTO, ord.IdMonedaLiquidacionOrden).valor;
						}
						else {
							ord.MontoOperacionLiquidacionPlazo = MonedaFormato.formatear(conn, calc.MontoDestino, MonedaFormato.MONTO, ord.IdMonedaLiquidacionOrden).valor;
						}
						ord.MontoOperacionLocalPlazo = MonedaFormato.formatear(conn, calc.MontoLocal, MonedaFormato.MONTO, idMonedaSistema).valor;
					}
				}
			}			
		}
		
		
		//CALCULAR COBROS PERU
		if (localizacionSistema.equalsIgnoreCase("PE")) {
			
			CalcularCobrosPe serviceCalcularCobrosPe = new CalcularCobrosPe();
			CalcularCobrosPe_Entrada entradaCalcularCobrosPe = new CalcularCobrosPe_Entrada();
			entradaCalcularCobrosPe.p_IdEmpresa = ent.p_IdEmpresa;
			for (Orden_Proceso ord : ordenes) {
				//EN PERU LOS COBROS CON TASA Y PLAZO SON SOLO PARA REPORTES
				InformacionOperacion_Entrada op = new InformacionOperacion_Entrada();
				op.CantidadOperacion = ord.CantidadOrden;
				op.MontoOperacionPlazo = ord.MontoOperacionPlazo;
				op.FechaOperacion = Fecha.toDate(ord.FechaOperacion);
				op.IdInterno = ord.IdInterno;
				op.IdMonedaLiquidacion = ord.IdMonedaLiquidacionOrden;
				op.IdMonedaOperacion = ord.IdMonedaLiquidacionOrden;//ord.IdMonedaOrden;
				op.MontoOperacion = ord.MontoOrden;
				op.PlazoEnDias = ord.PlazoReporte;
				op.Tasa = ord.TasaReporte;
				op.TasaReportado = ord.TasaReportado;
				op.TipoCambio = ord.TipoCambio;
				op.MontoLiquidacionOperacion = ord.IdTipoProducto.equalsIgnoreCase("RERV") || ord.IdTipoProducto.equalsIgnoreCase("RERF")?ord.MontoOrden:ord.MontoOperacionLiquidacion;
				op.MontoLiquidacionOperacionPlazo= ord.IdTipoProducto.equalsIgnoreCase("RERV") || ord.IdTipoProducto.equalsIgnoreCase("RERF")?ord.MontoOperacionPlazo:ord.MontoOperacionLiquidacionPlazo;
				op.MontoTotalComExternaContado = BigDecimal.ZERO;
				op.ValorIGVContado = BigDecimal.ZERO;
				op.BaseTasa = ord.BaseTasa;
				op.PorcentajeAllIn = ord.PorcentajeAllIn;
				op.MontoAllIn = ord.MontoAllIn;
				op.MontoInteresContado = ord.MontoInteresContado;
				op.MontoInteresPlazo = ord.MontoInteresPlazo;
				op.AccionOrden = ord.AccionOrden;
				entradaCalcularCobrosPe.p_Operaciones.add(op);
				
				for (DefinicionCobro_Proceso cob : ord.definicionCobros) {
					//Utils.copyPojo(cob,cobEntrada);
					DefinicionCobro_Entrada cobEntrada = copyPojoDefinicionCobroEntrada(cob);
					entradaCalcularCobrosPe.p_Cobros.add(cobEntrada);
				}
			}
			
			CalcularCobrosPe_Salida salidaCalcularCobrosPe = serviceCalcularCobrosPe.logica(conn, entradaCalcularCobrosPe);
			for (cl.bcs.plataforma.com.pe.CalcularCobrosPe.Cobro_Salida cobroSalida : salidaCalcularCobrosPe.p_out_Cobros) {
				DefinicionCobro_Proceso cobroOrden = cobrosPeruHash.get(cobroSalida.IdInternoCobro);
				cobroOrden.ValorCobroFinal = cobroSalida.ValorCobroFinal;
				cobroOrden.ValorCobroPerfil = cobroSalida.ValorCobroPerfil;
				cobroOrden.IdMonedaCobro = cobroSalida.IdMonedaCobro;
				cobroOrden.MontoCobroCalculado = cobroSalida.MontoCobroCalculado;
				cobroOrden.MontoCobroCalculadoLiquidacion = cobroSalida.MontoCobroCalculadoLiquidacion;
				cobroOrden.MontoCobroCalculadoLocal = cobroSalida.MontoCobroCalculadoLocal;
			}
			
			for (Orden_Proceso ord : ordenes) {
				for (cl.bcs.plataforma.com.pe.CalcularCobrosPe.Operacion_Salida op : salidaCalcularCobrosPe.p_out_operaciones) {
					if (ord.IdInterno.equals(op.IdInterno)) {
						ord.MontoAllIn = op.MontoAllIn;
						ord.PorcentajeAllIn = op.PorcentajeAllIn;
						if (ord.IdTipoProducto.equalsIgnoreCase("RERV") || ord.IdTipoProducto.equalsIgnoreCase("RERF")){
							ord.MontoOperacionLocal = op.MontoOperacion;
							ord.MontoOperacionLocalPlazo = op.MontoOperacionPlazo;
						}
						else {
							ord.MontoOrden = op.MontoOperacion;
							ord.MontoOperacionPlazo = op.MontoOperacionPlazo;
						}
						ord.MontoOperacionLiquidacion = op.MontoLiquidacionOperacion;
						ord.MontoOperacionLiquidacionPlazo = op.MontoLiquidacionOperacionPlazo;
					}
				}
			}
			
			
		}
		
		//CHILE : MONTO COBROS A MONEDA DE LIQUIDACION Y MONEDA LOCAL
		if (localizacionSistema.equalsIgnoreCase("CL")) {
			idTrabajo++;
			CalcularParidadEntrada calcularParidadEntrada = new  CalcularParidadEntrada();
			calcularParidadEntrada.p_IdEmpresa = ent.p_IdEmpresa;
			calcularParidadEntrada.p_IdTrabajo = idTrabajo;
			
			for (Orden_Proceso ord : ordenes) {
				for (Cobro_Proceso cob : ord.cobros) {
					CalculoEntrada c1 = new CalculoEntrada();
					c1.Fecha = fechaSistema;
					c1.IdInterno = cob.IdInternoCobro;
					c1.IdMonedaDestino = ord.IdMonedaLiquidacionOrden;
					c1.IdMonedaOrigen = cob.IdMoneda;
					c1.IdTrabajo = idTrabajo;
					c1.IdUso = obtenerIdUsoPorIdTipoOperacion(ord.IdTipoOperacion,localizacionSistema,ord.IdBolsa);
					c1.MontoOrigen = cob.MontoCobroCalculado;
					c1.ValorCambioLocalModificado = ord.TipoCambio;
					calcularParidadEntrada.p_CalculosEntrada.add(c1);
				}
			}
			
			CalcularParidad calcularParidadService = new CalcularParidad();
			CalcularParidadSalida calcularParidadSalida = calcularParidadService.logica(conn, calcularParidadEntrada);
			
			for (CalculoSalida calc: calcularParidadSalida.p_out_CalculosSalida) {
				
				Cobro_Proceso cob = cobrosHash.get(calc.IdInterno);
				Orden_Proceso ord = ordenesHash.get(cob.IdInterno);
				
				if (!calc.Resultado.equals("OK")) {
					String codigo = calc.CodigoMensaje;
					String desc = Diccionario.getMensaje(conn, ent.p_IdEmpresa,codigo)
							.add("IdMonedaOrigen", calc.IdMonedaOrigen)
							.add("IdMonedaDestino", calc.IdMonedaDestino)
							.msg();
					salida.P_OUT_MENSAJES.add(new MensajeSalida(ord.IdInterno,MensajeSalida.MENSAJE_ERROR,codigo,desc));
					return salida;
				}
				else {
					cob.MontoCobroCalculadoLiquidacion = MonedaFormato.formatear(conn, calc.MontoDestino, MonedaFormato.MONTO, cob.IdMonedaLiquidacionOrden).valor;
					cob.MontoCobroCalculadoLocal = MonedaFormato.formatear(conn, calc.MontoLocal, MonedaFormato.MONTO, idMonedaSistema).valor;
				}
			}
		}
		
		//PERU MONTO COBROS A MONEDA DE LIQUIDACION Y MONEDA LOCAL
		if (localizacionSistema.equalsIgnoreCase("PE")) {
			//AHORA ESTA DENTRO DE CALCULAR COBROS PE
		}
		
		
		//CALCULO DE TOTALES POR ORDEN
		{
			for (Orden_Proceso ord : ordenes) {
				for (Cobro_Proceso cob : ord.cobros) {
					ord.TotalCobrosLiquidacion = ord.TotalCobrosLiquidacion.add(cob.MontoCobroCalculadoLiquidacion); 
					ord.TotalCobrosLocal = ord.TotalCobrosLocal.add(cob.MontoCobroCalculadoLocal);
				}
				
				for (DefinicionCobro_Proceso cob : ord.definicionCobros) {
					if (cob.Otros_CheckOperacionPlazo.equalsIgnoreCase("S")) {
						ord.TotalCobrosLiquidacionPlazo = ord.TotalCobrosLiquidacionPlazo.add(cob.MontoCobroCalculadoLiquidacion);
					}
					else {
						ord.TotalCobrosLiquidacion = ord.TotalCobrosLiquidacion.add(cob.MontoCobroCalculadoLiquidacion);
						ord.TotalCobrosLocal = ord.TotalCobrosLocal.add(cob.MontoCobroCalculadoLocal);
					}
				}
				
				if (ord.AccionOrden.equals(_c.ACCION_VENTA)) {
					ord.MontoTotalLocal = ord.MontoOperacionLocal.subtract(ord.TotalCobrosLocal);
					ord.MontoTotalLiquidacion = ord.MontoOperacionLiquidacion.subtract(ord.TotalCobrosLiquidacion);
					ord.MontoTotalLiquidacionPlazo = ord.MontoOperacionLiquidacionPlazo.add(ord.TotalCobrosLiquidacionPlazo);
				}
				else if (ord.AccionOrden.equalsIgnoreCase(_c.ACCION_COMPRA)) {
					ord.MontoTotalLocal = ord.MontoOperacionLocal.add(ord.TotalCobrosLocal);
					ord.MontoTotalLiquidacion = ord.MontoOperacionLiquidacion.add(ord.TotalCobrosLiquidacion);
					ord.MontoTotalLiquidacionPlazo = ord.MontoOperacionLiquidacionPlazo.subtract(ord.TotalCobrosLiquidacionPlazo);
				}				
			}
		}
		
		//COBROS ULTIMA ORDEN
		{
			HashMap<String,Object> params = new HashMap<String,Object>();
			ArrayList<String> idsTipoOperacion = new ArrayList<String>();
			ArrayList<Long> idsPortafolio = new ArrayList<Long>();
			for (Orden_Proceso ord : ordenes) {
				idsTipoOperacion.add(ord.IdTipoOperacion);
				idsPortafolio.add(ord.IdPortafolio);
			}

			params.put("idsTipoOperacion", idsTipoOperacion);
			params.put("idsPortafolio", idsPortafolio);
			List<CobroUltimaOrden> cobrosUltimaOrden = _sql.query(conn, SQL_SELECT_COBROS_ULTIMA_ORDEN, params, CobroUltimaOrden.class);
			for (Orden_Proceso ord : ordenes) {
				for (CobroUltimaOrden cobro : cobrosUltimaOrden) {
					if (ord.IdPortafolio.equals(cobro.IdPortafolio) && ord.IdTipoOperacion.equalsIgnoreCase(cobro.IdTipoOperacion)) {
						ord.PorcentajeCobrosUltimaOrden = cobro.PorcentajeCobros;
					}
				}
			}
		}
		
		//RESULTADO
		for (Orden_Proceso ord : ordenes) {
			//Utils.copyPojo(ord, ord_salida);
			Orden_Salida ord_salida = copyPojoOrdenSalida(ord);
			ord_salida.CantidadOperacion = ord.CantidadOrden;
			ord_salida.MontoOperacion = ord.MontoOrden;
			salida.p_out_Ordenes.add(ord_salida);
			if (localizacionSistema.equalsIgnoreCase("CL")) {
				for (Cobro_Proceso cob : ord.cobros) {
					//Utils.copyPojo(cob, cob_salida);
					Cobro_Salida cob_salida = copyPojoCobroProcesoCobroSalida(cob);
					cob_salida.IdMonedaCobro = cob.IdMoneda;
					cob_salida.CodigoCobroClasificacionBase = cob.CodigoCobroClasificacion;
					if (cob_salida.ValorCobroAplicado == null) {
						cob_salida.ValorCobroAplicado = cob_salida.ValorCobroPerfil;
					}
					salida.p_out_Cobros.add(cob_salida);
				}
			}
			if (localizacionSistema.equalsIgnoreCase("PE")) {
				for (DefinicionCobro_Proceso cob : ord.definicionCobros) {
					//Utils.copyPojo(cob, cob_salida);
					Cobro_Salida cob_salida = copyPojoDefinicionCobroProcesoCobroSalida(cob);
					cob_salida.IdCobro = cob.CodigoDefinicionCobro;
					cob_salida.CodigoCobroClasificacionBase = cob.IdClaseCobroBase;
					cob_salida.NombreCobro = cob.Descripcion;
					if (cob.Otros_CheckPermiteAumentarValor.equalsIgnoreCase("S") || cob.Otros_CheckPermiteDisminuirValor.equalsIgnoreCase("S")) {
						cob_salida.PermiteCambiarValor = "S";
					}
					else {
						cob_salida.PermiteCambiarValor = "N";
					}
					cob_salida.Otros_CheckAllIn = cob.Otros_CheckAllIn;
					cob_salida.FormaCobro = cob.IdTipoCobro;
					cob_salida.NombreFormaCobro = cob.NombreTipoCobro;
					cob_salida.MontoMinimo = cob.PorcentualFijo_ValorMontoMinimo;
					cob_salida.CodigoCobroClasificacion = cob.IdClaseCobro;
					cob_salida.IdMonedaCobro = cob.IdMonedaCobro;
					cob_salida.IdMonedaLiquidacionOrden = ord.IdMonedaLiquidacionOrden;
					if (cob_salida.ValorCobroAplicado == null) {
						cob_salida.ValorCobroAplicado = cob_salida.ValorCobroPerfil;
					}
					//cob_salida.RangoCobros.addAll(cob.RangoCobros);
					cob_salida.PorcentualRango_IdMonedaRangoMonto = cob.PorcentualRango_IdMonedaRangoMonto;
					cob_salida.PorcentualRango_IdTipoRangoCobro = cob.PorcentualRango_IdTipoRangoCobro;
					salida.p_out_Cobros.add(cob_salida);
				}
				
				Collections.sort(salida.p_out_Cobros, new Comparator<Cobro_Salida>() {
				    @Override
				    public int compare(Cobro_Salida s1, Cobro_Salida s2) {
				        int i1 = Character.getNumericValue(s1.CodigoCobroClasificacionBase.charAt(0)) - Character.getNumericValue(s2.CodigoCobroClasificacionBase.charAt(0));
				        if (i1 != 0) {
				        	return i1;
				        }
				        
				        int i2 = Character.getNumericValue(s1.IdOrigenCobro.charAt(0)) - Character.getNumericValue(s2.IdOrigenCobro.charAt(0));
				        return i2;
				    }
				});
			}
		}
		
		logger.info("Termino del servicio CalcularMontoYCobrosOrden, tiempo:" + (System.currentTimeMillis() - init));
		return salida;
	}
	
	
	private String obtenerIdUsoPorIdTipoOperacion(String idTipoOperacion, String LocalizacionSistema, String IdBolsa) {
		String idUso = "";
		if("CL".equalsIgnoreCase(LocalizacionSistema)) {
			if ("COMMIL".equalsIgnoreCase(idTipoOperacion))
				idUso = "COMPRA MILA";
			else if ("VENMIL".equalsIgnoreCase(idTipoOperacion))
				idUso = "VENTA MILA";
			else if ("COMETX".equalsIgnoreCase(idTipoOperacion) || "COMCFX".equalsIgnoreCase(idTipoOperacion) || "COMACX".equalsIgnoreCase(idTipoOperacion)) 
				idUso = "COMPRA EXTRANJEROS"; //COMETX;COMCFX;COMACX
			else if ("VENETX".equalsIgnoreCase(idTipoOperacion) || "VENCFX".equalsIgnoreCase(idTipoOperacion) || "VENACX".equalsIgnoreCase(idTipoOperacion))
				idUso = "VENTA EXTRANJEROS"; //VENETX;VENCFX;VENACX
			else
				idUso = "ORDENES";
		}else if("PE".equalsIgnoreCase(LocalizacionSistema)) {
			if ("COMACN".equalsIgnoreCase(idTipoOperacion) && "MIL".equalsIgnoreCase(IdBolsa))
				idUso = "COMPRA MILA";
			else if ("VENACN".equalsIgnoreCase(idTipoOperacion)&& "MIL".equalsIgnoreCase(IdBolsa))
				idUso = "VENTA MILA";
			else if ("COMACN".equalsIgnoreCase(idTipoOperacion) && "OUT".equalsIgnoreCase(IdBolsa)) 
				idUso = "COMPRA EXTRANJEROS"; 
			else if ("VENACN".equalsIgnoreCase(idTipoOperacion) && "OUT".equalsIgnoreCase(IdBolsa))
				idUso = "VENTA EXTRANJEROS"; 
			else if ("VENRRV".equalsIgnoreCase(idTipoOperacion) ||
					  "COMRRV".equalsIgnoreCase(idTipoOperacion) || "VENRRF".equalsIgnoreCase(idTipoOperacion) || "COMRRF".equalsIgnoreCase(idTipoOperacion) ||
					  "ANUVRV".equalsIgnoreCase(idTipoOperacion) || "ANTVRV".equalsIgnoreCase(idTipoOperacion) || "VENVRV".equalsIgnoreCase(idTipoOperacion) ||
					  "ANUVRF".equalsIgnoreCase(idTipoOperacion) || "ANTVRF".equalsIgnoreCase(idTipoOperacion) || "VENVRF".equalsIgnoreCase(idTipoOperacion) ||
					  "ANUCRV".equalsIgnoreCase(idTipoOperacion) || "ANTCRV".equalsIgnoreCase(idTipoOperacion) || "VENCRV".equalsIgnoreCase(idTipoOperacion) ||
					  "ANUCRF".equalsIgnoreCase(idTipoOperacion) || "ANTCRF".equalsIgnoreCase(idTipoOperacion) || "VENCRF".equalsIgnoreCase(idTipoOperacion)) 
				idUso = "REPORTES";
			else
				idUso = "ORDENES";	
		}
		return idUso;
	}

	public static class CobroUltimaOrden {
		public Long IdPortafolio;
		public String IdTipoOperacion;
		public BigDecimal PorcentajeCobros;
		public Long IdOrden;
	}
	
	public static class CalcularMontoYCobrosOrden_Entrada {
		public long 				p_IdEmpresa;
		public long 				p_IdUsuario;
		public String 				p_IdModulo;
		public List<Orden_Entrada>	p_Ordenes;
		public List<Cobro_Entrada>	p_CobrosModificados;
		public List<Cobro_Fijo>		p_CobrosFijos;
		
		public CalcularMontoYCobrosOrden_Entrada() {
			p_Ordenes = new ArrayList<>();
			p_CobrosModificados = new ArrayList<>();
			p_CobrosFijos = new ArrayList<>();
		}
		public String toString() {
			return Utils.printJson(this, true);
		}
	}
	
	public static class CalcularMontoYCobrosOrden_Salida {
		public List<MensajeSalida> P_OUT_MENSAJES;
		public long P_OUT_ERRORESCONTADOR;
		public List<Orden_Salida> p_out_Ordenes;
		public List<Cobro_Salida> p_out_Cobros;
		
		public String toString() {
			return Utils.printJson(this, true);
		}
		
		public CalcularMontoYCobrosOrden_Salida() {
			p_out_Ordenes = new ArrayList<>();
			p_out_Cobros = new ArrayList<>();
			P_OUT_MENSAJES = new ArrayList<>();
		}
	}
	
	//COBRO PARA PERU
	public static class DefinicionCobro_Proceso {
		public String IdOrigenCobro;
		public String Descripcion;
		public BigDecimal ValorCobroPerfil;
		public String IdMonedaCobro;
		public Long IdInternoCobro;
		public Long IdInterno;
		public Long IdPortafolio;
		public String IdPerfilCobros;
		public String EsPerfilPersonalizado;
		public String IdEstadoPerfilCobros;
		public String IdTipoProducto;
		public String IdTipoOperacion;
		public String IdTipoCanalIngreso;
		public String IdBolsa;
		public String IdClaseCobro;
		public String IdClaseCobroBase;
		public String IdTipoCobro;
		public String NombreTipoCobro;
		public String CodigoDefinicionCobro;
		public String IdEstado;
		public String CheckCobroMonedaOperacion;
		public String IdMonedaCobroOtraMoneda;
		public BigDecimal MontoFijo_MontoCobro;
		public String MontoFijo_CheckMontoOperacionMenor;
		public BigDecimal MontoFijo_ValorMontoOperacionMenor;
		public String MontoFijo_CheckSiNo;
		public BigDecimal MontoFijo_ValorMontoSiNo;
		public String MontoFijoPorUnidad_AllInCheckValor;
		public BigDecimal MontoFijoPorUnidad_MontoCobro;
		public BigDecimal PorcentualFijo_ValorCobro;
		public String PorcentualFijo_CheckSobreMonto;
		public String PorcentualFijo_CheckSobreCantidad;
		public String PorcentualFijo_CheckMayorEntreDos;
		public String PorcentualFijo_CheckMenorEntreDos;
		public String PorcentualFijo_CheckMontoMinimo;
		public BigDecimal PorcentualFijo_ValorMontoMinimo;
		public String PorcentualFijo_CheckMontoMaximo;
		public BigDecimal PorcentualFijo_ValorMontoMaximo;
		public String PorcentualFijo_IdMonedaMinimoMaximo;
		public BigDecimal PorcentualTasaPlazo_ValorCobro;
		public String Otros_CheckOperacionContado;
		public String Otros_CheckOperacionPlazo;
		public String Otros_CheckBaseTasa;
		public Long Otros_ValorBaseTasa;
		public String Otros_CheckInteresSimple;
		public String Otros_CheckInteresCompuesto;
		public String Otros_CheckAllIn;
		public BigDecimal Otros_ValorAllInMontoOperacionBase;
		public String Otros_CheckAfectoImpuesto;
		public String Otros_CheckAplicaPortafolio;
		public String Otros_CheckAplicaCorresponsal;
		public String Otros_CheckPermiteAumentarValor;
		public String Otros_CheckPermiteDisminuirValor;
		public String PorcentualRango_IdMonedaRangoMonto;
		public String PorcentualRango_IdTipoRangoCobro;
		public String Otros_IncluyeExcluyeEmisoresCheck;
		public String Otros_IncluyeExcluyeInstrumentosCheck;
		public String Otros_IncluyeExcluyeCorresponsalesCheck;
		public String Otros_IncluyeExcluyeEmisoresValor;
		public String Otros_IncluyeExcluyeInstrumentosValor;
		public String Otros_IncluyeExcluyeCorresponsalesValor;
		public List<DefinicionRangoCobro> RangoCobros = new ArrayList<DefinicionRangoCobro>();
		public Long ValorBaseTasaCobro;
		
		public BigDecimal	ValorCobroFinal;
		public BigDecimal	ValorCobroAplicado;
		public BigDecimal	MontoCobroCalculado; 
		public BigDecimal	MontoCobroCalculadoLocal; 
		public BigDecimal	MontoCobroCalculadoLiquidacion; 
	}
	
	public static class Cobro_Proceso {
		public String 		IdOrigenCobro;
		public Long			IdInterno;
		public Long			IdInternoCobro;
		public String		IdMonedaLiquidacionOrden;
		public BigDecimal	MontoOperacionMonedaCobro;
		public String		IdCobro;
		public String 		CodigoCobroClasificacion;
		public String		IdMoneda;
		public String		NombreCobro;
		public BigDecimal	ValorCobroPerfil;
		public String		PermiteCambiarValor;
		public String 		FormaCobro;
		public BigDecimal 	MontoMinimo;
		public BigDecimal	ValorCobroAplicado; 
		public BigDecimal	ValorCobroFinal; 
		public BigDecimal	MontoCobroCalculado; 
		public BigDecimal	MontoCobroCalculadoLocal; 
		public BigDecimal	MontoCobroCalculadoLiquidacion; 
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Orden_Proceso {
		public String		TipoInteres;
		public Long 		BaseTasa;
		public BigDecimal	PorcentajeCobrosUltimaOrden;
		public String		IdClasificacionTipoInstrumento;
		public String		FechaOperacion;
		public Long			IdInterno;
		public Long			IdPortafolio; 
		public String		IdTipoOperacion;
		public String		IdTipoProducto;
		public String		IdTipoCanalIngreso; 
		public String		IdBolsa; 
		public Long			IdCorredorCorresponsal; 
		public Long			IdInstrumento;
		public Long			IdInstrumentoSubyacente;
		public BigDecimal	CantidadOrden; 
		public BigDecimal	MontoOrden;			//PARA COMPRA SIMULTANEA
		public BigDecimal	MontoOperacionPlazo = BigDecimal.ZERO;
		public BigDecimal	PrecioPlazo = BigDecimal.ZERO;
		public BigDecimal	ValorRescateOrden;
		public String 		EsFungible;
		public BigDecimal	TasaSimultanea; 
		public BigDecimal	TasaRFIF; 
		public BigDecimal	TasaVentaCorta; 
		public BigDecimal	TasaReporte;
		public BigDecimal	TasaReportado;
		
		public Long			PlazoReporte;
		public Long			PlazoIF;
		public Long			PlazoSimultanea;
		public Long			PlazoVentaCorta;
		
		public String		IdMonedaLiquidacionOrden; 
		public BigDecimal	PrecioOrden; 
		public BigDecimal	TipoCambio;
		public String		AccionOrden;
		public String 		IdEmisor;
		public String 		EsAPlazo;
		
		//ORDEN POR MONTO
		public BigDecimal	MontoTotal;
		public String		EsPorMonto;
		public BigDecimal	MontoTotalMonedaOrden;
		
		
		public String 		IdMonedaOrden;
		public BigDecimal 	MontoTotalLocal;
		public BigDecimal	MontoTotalLiquidacion  = BigDecimal.ZERO;
		public BigDecimal	MontoTotalLiquidacionPlazo = BigDecimal.ZERO;
		public BigDecimal	MontoOperacionLiquidacionPlazo = BigDecimal.ZERO;
		public BigDecimal	MontoOperacionLocalPlazo = BigDecimal.ZERO;
		public BigDecimal	TotalCobrosLiquidacionPlazo = BigDecimal.ZERO;
		public BigDecimal	MontoOperacionLocal  = BigDecimal.ZERO;
		public BigDecimal	MontoOperacionLiquidacion  = BigDecimal.ZERO;
		public BigDecimal	TotalCobrosLocal  = BigDecimal.ZERO;
		public BigDecimal	TotalCobrosLiquidacion  = BigDecimal.ZERO;
		public BigDecimal	MontoInteresContado  = BigDecimal.ZERO;
		public BigDecimal	MontoInteresPlazo  = BigDecimal.ZERO;
		
		public BigDecimal	PorcentajeAllIn;
		public BigDecimal 	MontoAllIn;
		
		public String GlosaRefactura = "";
		public String FechaOperacionOriginal = "";
		
		public List<Cobro_Proceso> cobros;
		public List<DefinicionCobro_Proceso> definicionCobros;
		
		public Orden_Proceso() {
			cobros = new ArrayList<Cobro_Proceso>();
			definicionCobros = new ArrayList<DefinicionCobro_Proceso>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Orden_Salida {
		public Long			IdInterno;
		public BigDecimal	PorcentajeCobrosUltimaOrden;
		public BigDecimal	PrecioPlazo;
		public String		IdMonedaLiquidacionOrden;
		public BigDecimal 	CantidadOperacion;
		public BigDecimal	MontoTotalLiquidacion; 
		public BigDecimal	MontoOperacion; 
		public BigDecimal	MontoOperacionLocal; 
		public BigDecimal	MontoOperacionLiquidacion;
		public BigDecimal	MontoOperacionPlazo;
		public BigDecimal	MontoTotalLocal;
		public BigDecimal	TotalCobrosLocal; 
		public BigDecimal	TotalCobrosLiquidacion;
		public BigDecimal	MontoOperacionLiquidacionPlazo;
		public BigDecimal	MontoOperacionLocalPlazo;
		public BigDecimal	MontoTotalLiquidacionPlazo;
		public BigDecimal	TotalCobrosLiquidacionPlazo;
		public String 		IdMonedaOrden;
		public BigDecimal	TasaReporte;
		public BigDecimal	ValorRescateOrden;
		public BigDecimal	MontoInteresContado;
		public BigDecimal	MontoInteresPlazo;
		
		//DATOS DE COBROS
		public BigDecimal	MontoAllIn;
		public BigDecimal	PorcentajeAllIn;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Cobro_Salida {
		public String 		NombreFormaCobro;
		public Long			IdInterno;
		public Long			IdInternoCobro;
		public String		IdCobro;
		public String		NombreCobro;
		public String 		CodigoCobroClasificacion;
		public String 		CodigoCobroClasificacionBase;
		public String		IdMonedaCobro;
		public BigDecimal	ValorCobroPerfil;
		public String		PermiteCambiarValor;
		public String 		FormaCobro;
		public String		IdMonedaLiquidacionOrden;
		public BigDecimal	ValorCobroAplicado;
		public BigDecimal	ValorCobroFinal; 
		public BigDecimal	MontoCobroCalculado; 
		public BigDecimal	MontoCobroCalculadoLocal; 
		public BigDecimal	MontoCobroCalculadoLiquidacion;
		public String		IdOrigenCobro;
		public BigDecimal	MontoMinimo;
		
		//CAMPOS PERU
		public String			CheckCobroMonedaOperacion;
		public String 			Otros_CheckAllIn;
		public String			IdMonedaCobroOtraMoneda;
		public BigDecimal		MontoFijo_MontoCobro;
		public String			MontoFijo_CheckMontoOperacionMenor;
		public BigDecimal		MontoFijo_ValorMontoOperacionMenor;
		public String			MontoFijo_CheckSiNo;
		public BigDecimal		MontoFijo_ValorMontoSiNo;
		public BigDecimal		MontoFijoPorUnidad_MontoCobro;
		public String			MontoFijoPorUnidad_AllInCheckValor;
		public BigDecimal		PorcentualFijo_ValorCobro;
		public String			PorcentualFijo_CheckSobreMonto;
		public String			PorcentualFijo_CheckSobreCantidad;
		public String			PorcentualFijo_CheckMayorEntreDos;
		public String			PorcentualFijo_CheckMenorEntreDos;
		public String			PorcentualFijo_CheckMontoMinimo;
		public BigDecimal		PorcentualFijo_ValorMontoMinimo;
		public String			PorcentualFijo_CheckMontoMaximo;
		public BigDecimal		PorcentualFijo_ValorMontoMaximo;
		public String			PorcentualFijo_IdMonedaMinimoMaximo;
		public BigDecimal		PorcentualTasaPlazo_ValorCobro;
		public String			Otros_CheckAfectoImpuesto;
		public String			Otros_CheckPermiteAumentarValor;
		public String			Otros_CheckPermiteDisminuirValor;
		public String			Otros_CheckOperacionContado;
		public String			Otros_CheckOperacionPlazo;
		public String 			Otros_CheckInteresCompuesto;
		public String 			Otros_CheckInteresSimple;
		public List<DefinicionRangoCobro> RangoCobros = new ArrayList<>();
		public String 			PorcentualRango_IdMonedaRangoMonto;
		public String 			PorcentualRango_IdTipoRangoCobro;
		
		
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Orden_Entrada {
		public Long			IdInterno; 
		public Long			IdPortafolio; 
		public String		IdTipoOperacion;
		public String		IdTipoProducto;
		public String		IdTipoCanalIngreso; 
		public String		IdBolsa; 
		public Long			IdCorredorCorresponsal; 
		public Long			IdInstrumento;
		public Long			IdInstrumentoSubyacente;
		public BigDecimal	CantidadOrden; 
		public BigDecimal	MontoOrden;	//PARA COMPRA SIMULTANEA
		public BigDecimal	ValorRescateOrden;
		public String 		EsFungible;
		public BigDecimal	TasaSimultanea; 
		public BigDecimal	TasaRFIF; 
		public BigDecimal	TasaVentaCorta;
		public Long 		PlazoSimultanea;
		public Long			PlazoVentaCorta;
		public Long			PlazoIF;
		public String		IdMonedaLiquidacionOrden; 
		public String 		IdMonedaOrden; // solo para IF
		public BigDecimal	PrecioOrden; 
		public BigDecimal	TipoCambio;
		public String 		IdEmisor;
		public String 		EsAPlazo;
		public String		FechaOperacion;
		
		public String GlosaRefactura = "";
		public String FechaOperacionOriginal = "";
		
		//ORDEN POR MONTO
		public BigDecimal	MontoTotal;
		public String		EsPorMonto;
		
		//REPORTES
		public BigDecimal	TasaReporte;
		public BigDecimal	TasaReportado;
		public Long			PlazoReporte;
		public Long 		BaseTasa;
		public String		TipoInteres;
		
		public BigDecimal	PorcentajeAllIn;	//SI VIENEN LOS DOS, LE CREEMOS AL MONTO, ES MAS EXACTO
		public BigDecimal	MontoAllIn;
		public BigDecimal 	ValorAllIn;
		public String IdClasificacionTipoInstrumento;			
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Cobro_Entrada {
		public Long 		IdInterno;
		public String		IdCobro;
		public BigDecimal	ValorCobroAplicado; 
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	private Orden_Proceso copyPojoOrdenProceso(Orden_Entrada ord) {
		Orden_Proceso ords = new Orden_Proceso();
		ords.TipoInteres = ord.TipoInteres;
		ords.BaseTasa = ord.BaseTasa;
		//ords.PorcentajeCobrosUltimaOrden = ord.PorcentajeCobrosUltimaOrden;
		ords.IdClasificacionTipoInstrumento = ord.IdClasificacionTipoInstrumento;
		ords.FechaOperacion = ord.FechaOperacion;
		ords.IdInterno = ord.IdInterno;
		ords.IdPortafolio = ord.IdPortafolio; 
		ords.IdTipoOperacion = ord.IdTipoOperacion;
		ords.IdTipoProducto = ord.IdTipoProducto;
		ords.IdTipoCanalIngreso = ord.IdTipoCanalIngreso; 
		ords.IdBolsa = ord.IdBolsa; 
		ords.IdCorredorCorresponsal = ord.IdCorredorCorresponsal; 
		ords.IdInstrumento = ord.IdInstrumento;
		ords.IdInstrumentoSubyacente = ord.IdInstrumentoSubyacente;
		ords.CantidadOrden = ord.CantidadOrden; 
		ords.MontoOrden = ord.MontoOrden;
		//ords.MontoOperacionPlazo = ord.MontoOperacionPlazo;
		//ords.PrecioPlazo = ord.PrecioPlazo;
		ords.ValorRescateOrden = ord.ValorRescateOrden;
		ords.EsFungible = ord.EsFungible;
		ords.TasaSimultanea = ord.TasaSimultanea; 
		ords.TasaRFIF = ord.TasaRFIF; 
		ords.TasaVentaCorta = ord.TasaVentaCorta; 
		ords.TasaReporte = ord.TasaReporte;
		ords.TasaReportado = ord.TasaReportado;
		ords.PlazoReporte = ord.PlazoReporte;
		ords.PlazoIF = ord.PlazoIF;
		ords.PlazoSimultanea = ord.PlazoSimultanea;
		ords.PlazoVentaCorta = ord.PlazoVentaCorta;
		ords.IdMonedaLiquidacionOrden = ord.IdMonedaLiquidacionOrden; 
		ords.PrecioOrden = ord.PrecioOrden; 
		ords.TipoCambio = ord.TipoCambio;
		//ords.AccionOrden = ord.AccionOrden;
		ords.IdEmisor = ord.IdEmisor;
		ords.EsAPlazo = ord.EsAPlazo;
		ords.MontoTotal = ord.MontoTotal;
		ords.EsPorMonto = ord.EsPorMonto;
		//ords.MontoTotalMonedaOrden = ord.MontoTotalMonedaOrden;
		ords.IdMonedaOrden = ord.IdMonedaOrden;
		//ords.MontoTotalLocal = ord.MontoTotalLocal;
		//ords.MontoTotalLiquidacion = ord.MontoTotalLiquidacion;
		//ords.MontoTotalLiquidacionPlazo = ord.MontoTotalLiquidacionPlazo;
		//ords.MontoOperacionLiquidacionPlazo = ord.MontoOperacionLiquidacionPlazo;
		//ords.MontoOperacionLocalPlazo = ord.MontoOperacionLocalPlazo;
		//ords.TotalCobrosLiquidacionPlazo = ord.TotalCobrosLiquidacionPlazo;
		//ords.MontoOperacionLocal = ord.MontoOperacionLocal;
		//ords.MontoOperacionLiquidacion = ord.MontoOperacionLiquidacion;
		//ords.TotalCobrosLocal = ord.TotalCobrosLocal;
		//ords.TotalCobrosLiquidacion = ord.TotalCobrosLiquidacion;
		//ords.MontoInteresContado = ord.MontoInteresContado;
		//ords.MontoInteresPlazo = ord.MontoInteresPlazo;
		ords.PorcentajeAllIn = ord.PorcentajeAllIn;
		ords.MontoAllIn = ord.MontoAllIn;
		//ords.cobros = new ArrayList<Cobro_Proceso>(ord.cobros);
		//ords.definicionCobros = new ArrayList<DefinicionCobro_Proceso>(ord.definicionCobros);
		
		ords.GlosaRefactura = ord.GlosaRefactura;
		ords.FechaOperacionOriginal = ord.FechaOperacionOriginal;
		
		return ords;
	}
	
	private Cobro_Proceso copyPojoCobroProceso(LineaMatrizOperacional mat) {
		Cobro_Proceso cob1 = new Cobro_Proceso();
		cob1.IdOrigenCobro = mat.IdOrigenCobro;
		cob1.IdInterno = mat.IdInterno;
		//cob1.IdInternoCobro = mat.IdInternoCobro;
		//cob1.IdMonedaLiquidacionOrden = mat.IdMonedaLiquidacionOrden;
		//cob1.MontoOperacionMonedaCobro = mat.MontoOperacionMonedaCobro;
		cob1.IdCobro = mat.IdCobro;
		cob1.CodigoCobroClasificacion = mat.CodigoCobroClasificacion;
		cob1.IdMoneda = mat.IdMoneda;
		cob1.NombreCobro = mat.NombreCobro;
		//cob1.ValorCobroPerfil = mat.ValorCobroPerfil;
		cob1.PermiteCambiarValor = mat.PermiteCambiarValor;
		cob1.FormaCobro = mat.FormaCobro;
		//cob1.MontoMinimo = mat.MontoMinimo;
		//cob1.ValorCobroAplicado = mat.ValorCobroAplicado; 
		//cob1.ValorCobroFinal = mat.ValorCobroFinal; 
		//cob1.MontoCobroCalculado = mat.MontoCobroCalculado; 
		//cob1.MontoCobroCalculadoLocal = mat.MontoCobroCalculadoLocal; 
		//cob1.MontoCobroCalculadoLiquidacion = mat.MontoCobroCalculadoLiquidacion;
		return cob1;
	}
	
	private DefinicionCobro_Proceso copyPojoDefinicionCobroProceso(DefinicionCobro_Salida def) {
		DefinicionCobro_Proceso definicionCobroProceso = new DefinicionCobro_Proceso();
		definicionCobroProceso.IdOrigenCobro = def.IdOrigenCobro;
		definicionCobroProceso.Descripcion = def.Descripcion;
		//definicionCobroProceso.ValorCobroPerfil = def.ValorCobroPerfil;
		//definicionCobroProceso.IdMonedaCobro = def.IdMonedaCobro;
		//definicionCobroProceso.IdInternoCobro = def.IdInternoCobro;
		definicionCobroProceso.IdInterno = def.IdInterno;
		definicionCobroProceso.IdPortafolio = def.IdPortafolio;
		definicionCobroProceso.IdPerfilCobros = def.IdPerfilCobros;
		definicionCobroProceso.EsPerfilPersonalizado = def.EsPerfilPersonalizado;
		definicionCobroProceso.IdEstadoPerfilCobros = def.IdEstadoPerfilCobros;
		definicionCobroProceso.IdTipoProducto = def.IdTipoProducto;
		definicionCobroProceso.IdTipoOperacion = def.IdTipoOperacion;
		definicionCobroProceso.IdTipoCanalIngreso = def.IdTipoCanalIngreso;
		definicionCobroProceso.IdBolsa = def.IdBolsa;
		definicionCobroProceso.IdClaseCobro = def.IdClaseCobro;
		definicionCobroProceso.IdClaseCobroBase = def.IdClaseCobroBase;
		definicionCobroProceso.IdTipoCobro = def.IdTipoCobro;
		definicionCobroProceso.NombreTipoCobro = def.NombreTipoCobro;
		definicionCobroProceso.CodigoDefinicionCobro = def.CodigoDefinicionCobro;
		definicionCobroProceso.IdEstado = def.IdEstado;
		definicionCobroProceso.CheckCobroMonedaOperacion = def.CheckCobroMonedaOperacion;
		definicionCobroProceso.IdMonedaCobroOtraMoneda = def.IdMonedaCobroOtraMoneda;
		definicionCobroProceso.MontoFijo_MontoCobro = def.MontoFijo_MontoCobro;
		definicionCobroProceso.MontoFijo_CheckMontoOperacionMenor = def.MontoFijo_CheckMontoOperacionMenor;
		definicionCobroProceso.MontoFijo_ValorMontoOperacionMenor = def.MontoFijo_ValorMontoOperacionMenor;
		definicionCobroProceso.MontoFijo_CheckSiNo = def.MontoFijo_CheckSiNo;
		definicionCobroProceso.MontoFijo_ValorMontoSiNo = def.MontoFijo_ValorMontoSiNo;
		definicionCobroProceso.MontoFijoPorUnidad_AllInCheckValor = def.MontoFijoPorUnidad_AllInCheckValor;
		definicionCobroProceso.MontoFijoPorUnidad_MontoCobro = def.MontoFijoPorUnidad_MontoCobro;
		definicionCobroProceso.PorcentualFijo_ValorCobro = def.PorcentualFijo_ValorCobro;
		definicionCobroProceso.PorcentualFijo_CheckSobreMonto = def.PorcentualFijo_CheckSobreMonto;
		definicionCobroProceso.PorcentualFijo_CheckSobreCantidad = def.PorcentualFijo_CheckSobreCantidad;
		definicionCobroProceso.PorcentualFijo_CheckMayorEntreDos = def.PorcentualFijo_CheckMayorEntreDos;
		definicionCobroProceso.PorcentualFijo_CheckMenorEntreDos = def.PorcentualFijo_CheckMenorEntreDos;
		definicionCobroProceso.PorcentualFijo_CheckMontoMinimo = def.PorcentualFijo_CheckMontoMinimo;
		definicionCobroProceso.PorcentualFijo_ValorMontoMinimo = def.PorcentualFijo_ValorMontoMinimo;
		definicionCobroProceso.PorcentualFijo_CheckMontoMaximo = def.PorcentualFijo_CheckMontoMaximo;
		definicionCobroProceso.PorcentualFijo_ValorMontoMaximo = def.PorcentualFijo_ValorMontoMaximo;
		definicionCobroProceso.PorcentualFijo_IdMonedaMinimoMaximo = def.PorcentualFijo_IdMonedaMinimoMaximo;
		definicionCobroProceso.PorcentualTasaPlazo_ValorCobro = def.PorcentualTasaPlazo_ValorCobro;
		definicionCobroProceso.Otros_CheckOperacionContado = def.Otros_CheckOperacionContado;
		definicionCobroProceso.Otros_CheckOperacionPlazo = def.Otros_CheckOperacionPlazo;
		definicionCobroProceso.Otros_CheckBaseTasa = def.Otros_CheckBaseTasa;
		definicionCobroProceso.Otros_ValorBaseTasa = def.Otros_ValorBaseTasa;
		definicionCobroProceso.Otros_CheckInteresSimple = def.Otros_CheckInteresSimple;
		definicionCobroProceso.Otros_CheckInteresCompuesto = def.Otros_CheckInteresCompuesto;
		definicionCobroProceso.Otros_CheckAllIn = def.Otros_CheckAllIn;
		definicionCobroProceso.Otros_ValorAllInMontoOperacionBase = def.Otros_ValorAllInMontoOperacionBase;
		definicionCobroProceso.Otros_CheckAfectoImpuesto = def.Otros_CheckAfectoImpuesto;
		definicionCobroProceso.Otros_CheckAplicaPortafolio = def.Otros_CheckAplicaPortafolio;
		definicionCobroProceso.Otros_CheckAplicaCorresponsal = def.Otros_CheckAplicaCorresponsal;
		definicionCobroProceso.Otros_CheckPermiteAumentarValor = def.Otros_CheckPermiteAumentarValor;
		definicionCobroProceso.Otros_CheckPermiteDisminuirValor = def.Otros_CheckPermiteDisminuirValor;
		definicionCobroProceso.PorcentualRango_IdMonedaRangoMonto = def.PorcentualRango_IdMonedaRangoMonto;
		definicionCobroProceso.PorcentualRango_IdTipoRangoCobro = def.PorcentualRango_IdTipoRangoCobro;
		definicionCobroProceso.Otros_IncluyeExcluyeEmisoresCheck = def.Otros_IncluyeExcluyeEmisoresCheck;
		definicionCobroProceso.Otros_IncluyeExcluyeInstrumentosCheck = def.Otros_IncluyeExcluyeInstrumentosCheck;
		definicionCobroProceso.Otros_IncluyeExcluyeCorresponsalesCheck = def.Otros_IncluyeExcluyeCorresponsalesCheck;
		definicionCobroProceso.Otros_IncluyeExcluyeEmisoresValor = def.Otros_IncluyeExcluyeEmisoresValor;
		definicionCobroProceso.Otros_IncluyeExcluyeInstrumentosValor = def.Otros_IncluyeExcluyeInstrumentosValor;
		definicionCobroProceso.Otros_IncluyeExcluyeCorresponsalesValor = def.Otros_IncluyeExcluyeCorresponsalesValor;
		definicionCobroProceso.RangoCobros = new ArrayList<DefinicionRangoCobro>(def.RangoCobros);
		definicionCobroProceso.ValorBaseTasaCobro = def.ValorBaseTasaCobro;
		//definicionCobroProceso.ValorCobroFinal = def.ValorCobroFinal;
		//definicionCobroProceso.ValorCobroAplicado = def.ValorCobroAplicado;
		//definicionCobroProceso.MontoCobroCalculado = def.MontoCobroCalculado;
		//definicionCobroProceso.MontoCobroCalculadoLocal = def.MontoCobroCalculadoLocal;
		//definicionCobroProceso.MontoCobroCalculadoLiquidacion = def.MontoCobroCalculadoLiquidacion;
		return definicionCobroProceso;
	}
	
	private ObjEntradaInstrumentos copyPojoObjEntradaInstrumentos(ObjEntradaInstrumentos objCont) {
		ObjEntradaInstrumentos objPla = new ObjEntradaInstrumentos();
		objPla.IdValorizacion = objCont.IdValorizacion;
		objPla.OrigenProcesamiento = objCont.OrigenProcesamiento;
		objPla.FechaValorizacion = objCont.FechaValorizacion;
		objPla.FechaOperacion = objCont.FechaOperacion;
		objPla.FechaVencimientoOperacion = objCont.FechaVencimientoOperacion;
		objPla.PlazoEnDiasOperacion = objCont.PlazoEnDiasOperacion;
		objPla.IdClasificacionTipoInstrumento = objCont.IdClasificacionTipoInstrumento;
		objPla.IdTipoInstrumento = objCont.IdTipoInstrumento;
		objPla.IdInstrumento = objCont.IdInstrumento;
		objPla.Nemotecnico = objCont.Nemotecnico;
		objPla.FechaVencimiento = objCont.FechaVencimiento;
		objPla.FechaEmision = objCont.FechaEmision;
		objPla.IdMonedaInstrumento = objCont.IdMonedaInstrumento;
		objPla.IdMonedaEmisionInstrumento = objCont.IdMonedaEmisionInstrumento;
		objPla.IdTipoReajusteInstrumento = objCont.IdTipoReajusteInstrumento;
		objPla.Emisor = objCont.Emisor;
		objPla.IdCondicionDeLiquidacion = objCont.IdCondicionDeLiquidacion;
		objPla.FechaDeLiquidacion = objCont.FechaDeLiquidacion;
		objPla.Cantidad = objCont.Cantidad;
		objPla.ValorRescate = objCont.ValorRescate;
		objPla.PrecioCompra = objCont.PrecioCompra;
		objPla.TasaCompra = objCont.TasaCompra;
		objPla.MontoCompra = objCont.MontoCompra;
		objPla.MontoCompraMonedaSistema = objCont.MontoCompraMonedaSistema;
		objPla.PrecioSubyacente = objCont.PrecioSubyacente;
		objPla.TasaSubyacente = objCont.TasaSubyacente;
		objPla.PrecioGarantia = objCont.PrecioGarantia;
		objPla.TasaGarantia = objCont.TasaGarantia;
		objPla.TasaTIF = objCont.TasaTIF;
		objPla.IdPortafolio = objCont.IdPortafolio;
		objPla.TablaOrigen = objCont.TablaOrigen;
		objPla.IdUso = objCont.IdUso;
		objPla.TipoCartera = objCont.TipoCartera;
		objPla.IdTipoCriterioTasa = objCont.IdTipoCriterioTasa;
		return objPla;
	}
	
	private DefinicionCobro_Entrada copyPojoDefinicionCobroEntrada(DefinicionCobro_Proceso cob) {
		DefinicionCobro_Entrada cobEntrada = new DefinicionCobro_Entrada();
		cobEntrada.IdInterno = cob.IdInterno;						
		cobEntrada.IdInternoCobro = cob.IdInternoCobro;
		cobEntrada.ValorCobroAplicado = cob.ValorCobroAplicado;
		cobEntrada.IdClaseCobro = cob.IdClaseCobro;
		cobEntrada.IdClaseCobroBase = cob.IdClaseCobroBase;
		cobEntrada.IdTipoCobro = cob.IdTipoCobro;
		cobEntrada.CodigoDefinicionCobro = cob.CodigoDefinicionCobro;
		cobEntrada.CheckCobroMonedaOperacion = cob.CheckCobroMonedaOperacion;
		cobEntrada.IdMonedaCobroOtraMoneda = cob.IdMonedaCobroOtraMoneda;
		cobEntrada.MontoFijo_MontoCobro = cob.MontoFijo_MontoCobro;
		cobEntrada.MontoFijo_CheckMontoOperacionMenor = cob.MontoFijo_CheckMontoOperacionMenor;
		cobEntrada.MontoFijo_ValorMontoOperacionMenor = cob.MontoFijo_ValorMontoOperacionMenor;
		cobEntrada.MontoFijo_CheckSiNo = cob.MontoFijo_CheckSiNo;
		cobEntrada.MontoFijo_ValorMontoSiNo = cob.MontoFijo_ValorMontoSiNo;
		cobEntrada.MontoFijoPorUnidad_AllInCheckValor = cob.MontoFijoPorUnidad_AllInCheckValor;
		cobEntrada.MontoFijoPorUnidad_MontoCobro = cob.MontoFijoPorUnidad_MontoCobro;
		cobEntrada.PorcentualFijo_ValorCobro = cob.PorcentualFijo_ValorCobro;
		cobEntrada.PorcentualFijo_CheckSobreMonto = cob.PorcentualFijo_CheckSobreMonto;
		cobEntrada.PorcentualFijo_CheckSobreCantidad = cob.PorcentualFijo_CheckSobreCantidad;
		cobEntrada.PorcentualFijo_CheckMayorEntreDos = cob.PorcentualFijo_CheckMayorEntreDos;
		cobEntrada.PorcentualFijo_CheckMenorEntreDos = cob.PorcentualFijo_CheckMenorEntreDos;
		cobEntrada.PorcentualFijo_CheckMontoMinimo = cob.PorcentualFijo_CheckMontoMinimo;
		cobEntrada.PorcentualFijo_ValorMontoMinimo = cob.PorcentualFijo_ValorMontoMinimo;
		cobEntrada.PorcentualFijo_CheckMontoMaximo = cob.PorcentualFijo_CheckMontoMaximo;
		cobEntrada.PorcentualFijo_ValorMontoMaximo = cob.PorcentualFijo_ValorMontoMaximo;
		cobEntrada.PorcentualFijo_IdMonedaMinimoMaximo = cob.PorcentualFijo_IdMonedaMinimoMaximo;
		cobEntrada.PorcentualTasaPlazo_ValorCobro = cob.PorcentualTasaPlazo_ValorCobro;
		cobEntrada.Otros_CheckAfectoImpuesto = cob.Otros_CheckAfectoImpuesto;
		cobEntrada.Otros_CheckAllIn = cob.Otros_CheckAllIn;
		cobEntrada.Otros_CheckPermiteAumentarValor = cob.Otros_CheckPermiteAumentarValor;
		cobEntrada.Otros_CheckPermiteDisminuirValor = cob.Otros_CheckPermiteDisminuirValor;
		cobEntrada.Otros_CheckOperacionContado = cob.Otros_CheckOperacionContado;
		cobEntrada.Otros_CheckOperacionPlazo = cob.Otros_CheckOperacionPlazo;
		cobEntrada.PorcentualRango_IdMonedaRangoMonto = cob.PorcentualRango_IdMonedaRangoMonto;
		cobEntrada.PorcentualRango_IdTipoRangoCobro = cob.PorcentualRango_IdTipoRangoCobro;
		cobEntrada.RangoCobros = new ArrayList<DefinicionRangoCobro>(cob.RangoCobros);
		cobEntrada.Otros_CheckInteresSimple = cob.Otros_CheckInteresSimple;
		cobEntrada.Otros_CheckInteresCompuesto = cob.Otros_CheckInteresCompuesto;
		cobEntrada.ValorBaseTasaCobro = cob.ValorBaseTasaCobro;
		return cobEntrada;
	}
	
	private Orden_Salida copyPojoOrdenSalida(Orden_Proceso ord) {
		Orden_Salida ord_salida = new Orden_Salida();
		ord_salida.IdInterno = ord.IdInterno;
		ord_salida.PorcentajeCobrosUltimaOrden = ord.PorcentajeCobrosUltimaOrden;
		ord_salida.PrecioPlazo = ord.PrecioPlazo;
		ord_salida.IdMonedaLiquidacionOrden = ord.IdMonedaLiquidacionOrden;
		//ord_salida.CantidadOperacion = ord.CantidadOperacion;
		ord_salida.MontoTotalLiquidacion = ord.MontoTotalLiquidacion;
		//ord_salida.MontoOperacion = ord.MontoOperacion;
		ord_salida.MontoOperacionLocal = ord.MontoOperacionLocal;
		ord_salida.MontoOperacionLiquidacion = ord.MontoOperacionLiquidacion;
		ord_salida.MontoOperacionPlazo = ord.MontoOperacionPlazo;
		ord_salida.MontoTotalLocal = ord.MontoTotalLocal;
		ord_salida.TotalCobrosLocal = ord.TotalCobrosLocal;
		ord_salida.TotalCobrosLiquidacion = ord.TotalCobrosLiquidacion;
		ord_salida.MontoOperacionLiquidacionPlazo = ord.MontoOperacionLiquidacionPlazo;
		ord_salida.MontoOperacionLocalPlazo = ord.MontoOperacionLocalPlazo;
		ord_salida.MontoTotalLiquidacionPlazo = ord.MontoTotalLiquidacionPlazo;
		ord_salida.TotalCobrosLiquidacionPlazo = ord.TotalCobrosLiquidacionPlazo;
		ord_salida.IdMonedaOrden = ord.IdMonedaOrden;
		ord_salida.TasaReporte = ord.TasaReporte;
		ord_salida.ValorRescateOrden = ord.ValorRescateOrden;
		ord_salida.MontoInteresContado = ord.MontoInteresContado;
		ord_salida.MontoInteresPlazo = ord.MontoInteresPlazo;
		ord_salida.MontoAllIn = ord.MontoAllIn;
		ord_salida.PorcentajeAllIn = ord.PorcentajeAllIn;
		return ord_salida;
	}
	
	private Cobro_Salida copyPojoCobroProcesoCobroSalida(Cobro_Proceso cob) {
		Cobro_Salida cob_salida = new Cobro_Salida();
		//cob_salida.NombreFormaCobro = cob.NombreFormaCobro;
		cob_salida.IdInterno = cob.IdInterno;
		cob_salida.IdInternoCobro = cob.IdInternoCobro;
		cob_salida.IdCobro = cob.IdCobro;
		cob_salida.NombreCobro = cob.NombreCobro;
		cob_salida.CodigoCobroClasificacion = cob.CodigoCobroClasificacion;
		//cob_salida.CodigoCobroClasificacionBase = cob.CodigoCobroClasificacionBase;
		//cob_salida.IdMonedaCobro = cob.IdMonedaCobro;
		cob_salida.ValorCobroPerfil = cob.ValorCobroPerfil;
		cob_salida.PermiteCambiarValor = cob.PermiteCambiarValor;
		cob_salida.FormaCobro = cob.FormaCobro;
		cob_salida.IdMonedaLiquidacionOrden = cob.IdMonedaLiquidacionOrden;
		cob_salida.ValorCobroAplicado = cob.ValorCobroAplicado;
		cob_salida.ValorCobroFinal = cob.ValorCobroFinal; 
		cob_salida.MontoCobroCalculado = cob.MontoCobroCalculado; 
		cob_salida.MontoCobroCalculadoLocal = cob.MontoCobroCalculadoLocal; 
		cob_salida.MontoCobroCalculadoLiquidacion = cob.MontoCobroCalculadoLiquidacion;
		cob_salida.IdOrigenCobro = cob.IdOrigenCobro;
		cob_salida.MontoMinimo = cob.MontoMinimo;
		//cob_salida.CheckCobroMonedaOperacion = cob.CheckCobroMonedaOperacion;
		//cob_salida.Otros_CheckAllIn = cob.Otros_CheckAllIn;
		//cob_salida.IdMonedaCobroOtraMoneda = cob.IdMonedaCobroOtraMoneda;
		//cob_salida.MontoFijo_MontoCobro = cob.MontoFijo_MontoCobro;
		//cob_salida.MontoFijo_CheckMontoOperacionMenor = cob.MontoFijo_CheckMontoOperacionMenor;
		//cob_salida.MontoFijo_ValorMontoOperacionMenor = cob.MontoFijo_ValorMontoOperacionMenor;
		//cob_salida.MontoFijo_CheckSiNo = cob.MontoFijo_CheckSiNo;
		//cob_salida.MontoFijo_ValorMontoSiNo = cob.MontoFijo_ValorMontoSiNo;
		//cob_salida.MontoFijoPorUnidad_MontoCobro = cob.MontoFijoPorUnidad_MontoCobro;
		//cob_salida.MontoFijoPorUnidad_AllInCheckValor = cob.MontoFijoPorUnidad_AllInCheckValor;
		//cob_salida.PorcentualFijo_ValorCobro = cob.PorcentualFijo_ValorCobro;
		//cob_salida.PorcentualFijo_CheckSobreMonto = cob.PorcentualFijo_CheckSobreMonto;
		//cob_salida.PorcentualFijo_CheckSobreCantidad = cob.PorcentualFijo_CheckSobreCantidad;
		//cob_salida.PorcentualFijo_CheckMayorEntreDos = cob.PorcentualFijo_CheckMayorEntreDos;
		//cob_salida.PorcentualFijo_CheckMenorEntreDos = cob.PorcentualFijo_CheckMenorEntreDos;
		//cob_salida.PorcentualFijo_CheckMontoMinimo = cob.PorcentualFijo_CheckMontoMinimo;
		//cob_salida.PorcentualFijo_ValorMontoMinimo = cob.PorcentualFijo_ValorMontoMinimo;
		//cob_salida.PorcentualFijo_CheckMontoMaximo = cob.PorcentualFijo_CheckMontoMaximo;
		//cob_salida.PorcentualFijo_ValorMontoMaximo = cob.PorcentualFijo_ValorMontoMaximo;
		//cob_salida.PorcentualFijo_IdMonedaMinimoMaximo = cob.PorcentualFijo_IdMonedaMinimoMaximo;
		//cob_salida.PorcentualTasaPlazo_ValorCobro = cob.PorcentualTasaPlazo_ValorCobro;
		//cob_salida.Otros_CheckAfectoImpuesto = cob.Otros_CheckAfectoImpuesto;
		//cob_salida.Otros_CheckPermiteAumentarValor = cob.Otros_CheckPermiteAumentarValor;
		//cob_salida.Otros_CheckPermiteDisminuirValor = cob.Otros_CheckPermiteDisminuirValor;
		//cob_salida.Otros_CheckOperacionContado = cob.Otros_CheckOperacionContado;
		//cob_salida.Otros_CheckOperacionPlazo = cob.Otros_CheckOperacionPlazo;
		//cob_salida.Otros_CheckInteresCompuesto = cob.Otros_CheckInteresCompuesto;
		//cob_salida.Otros_CheckInteresSimple = cob.Otros_CheckInteresSimple;
		//cob_salida.RangoCobros = new ArrayList<DefinicionRangoCobro>(cob.RangoCobros);
		//cob_salida.PorcentualRango_IdMonedaRangoMonto = cob.PorcentualRango_IdMonedaRangoMonto;
		//cob_salida.PorcentualRango_IdTipoRangoCobro = cob.PorcentualRango_IdTipoRangoCobro;
		return cob_salida;
	}
	
	private Cobro_Salida copyPojoDefinicionCobroProcesoCobroSalida(DefinicionCobro_Proceso cob) {
		Cobro_Salida cob_salida = new Cobro_Salida();
		//cob_salida.NombreFormaCobro = cob.NombreFormaCobro;
		cob_salida.IdInterno = cob.IdInterno;
		cob_salida.IdInternoCobro = cob.IdInternoCobro;
		//cob_salida.IdCobro = cob.IdCobro;
		//cob_salida.NombreCobro = cob.NombreCobro;
		//cob_salida.CodigoCobroClasificacion = cob.CodigoCobroClasificacion;
		//cob_salida.CodigoCobroClasificacionBase = cob.CodigoCobroClasificacionBase;
		cob_salida.IdMonedaCobro = cob.IdMonedaCobro;
		cob_salida.ValorCobroPerfil = cob.ValorCobroPerfil;
		//cob_salida.PermiteCambiarValor = cob.PermiteCambiarValor;
		//cob_salida.FormaCobro = cob.FormaCobro;
		//cob_salida.IdMonedaLiquidacionOrden = cob.IdMonedaLiquidacionOrden;
		cob_salida.ValorCobroAplicado = cob.ValorCobroAplicado;
		cob_salida.ValorCobroFinal = cob.ValorCobroFinal; 
		cob_salida.MontoCobroCalculado = cob.MontoCobroCalculado; 
		cob_salida.MontoCobroCalculadoLocal = cob.MontoCobroCalculadoLocal; 
		cob_salida.MontoCobroCalculadoLiquidacion = cob.MontoCobroCalculadoLiquidacion;
		cob_salida.IdOrigenCobro = cob.IdOrigenCobro;
		//cob_salida.MontoMinimo = cob.MontoMinimo;
		cob_salida.CheckCobroMonedaOperacion = cob.CheckCobroMonedaOperacion;
		cob_salida.Otros_CheckAllIn = cob.Otros_CheckAllIn;
		cob_salida.IdMonedaCobroOtraMoneda = cob.IdMonedaCobroOtraMoneda;
		cob_salida.MontoFijo_MontoCobro = cob.MontoFijo_MontoCobro;
		cob_salida.MontoFijo_CheckMontoOperacionMenor = cob.MontoFijo_CheckMontoOperacionMenor;
		cob_salida.MontoFijo_ValorMontoOperacionMenor = cob.MontoFijo_ValorMontoOperacionMenor;
		cob_salida.MontoFijo_CheckSiNo = cob.MontoFijo_CheckSiNo;
		cob_salida.MontoFijo_ValorMontoSiNo = cob.MontoFijo_ValorMontoSiNo;
		cob_salida.MontoFijoPorUnidad_MontoCobro = cob.MontoFijoPorUnidad_MontoCobro;
		cob_salida.MontoFijoPorUnidad_AllInCheckValor = cob.MontoFijoPorUnidad_AllInCheckValor;
		cob_salida.PorcentualFijo_ValorCobro = cob.PorcentualFijo_ValorCobro;
		cob_salida.PorcentualFijo_CheckSobreMonto = cob.PorcentualFijo_CheckSobreMonto;
		cob_salida.PorcentualFijo_CheckSobreCantidad = cob.PorcentualFijo_CheckSobreCantidad;
		cob_salida.PorcentualFijo_CheckMayorEntreDos = cob.PorcentualFijo_CheckMayorEntreDos;
		cob_salida.PorcentualFijo_CheckMenorEntreDos = cob.PorcentualFijo_CheckMenorEntreDos;
		cob_salida.PorcentualFijo_CheckMontoMinimo = cob.PorcentualFijo_CheckMontoMinimo;
		cob_salida.PorcentualFijo_ValorMontoMinimo = cob.PorcentualFijo_ValorMontoMinimo;
		cob_salida.PorcentualFijo_CheckMontoMaximo = cob.PorcentualFijo_CheckMontoMaximo;
		cob_salida.PorcentualFijo_ValorMontoMaximo = cob.PorcentualFijo_ValorMontoMaximo;
		cob_salida.PorcentualFijo_IdMonedaMinimoMaximo = cob.PorcentualFijo_IdMonedaMinimoMaximo;
		cob_salida.PorcentualTasaPlazo_ValorCobro = cob.PorcentualTasaPlazo_ValorCobro;
		cob_salida.Otros_CheckAfectoImpuesto = cob.Otros_CheckAfectoImpuesto;
		cob_salida.Otros_CheckPermiteAumentarValor = cob.Otros_CheckPermiteAumentarValor;
		cob_salida.Otros_CheckPermiteDisminuirValor = cob.Otros_CheckPermiteDisminuirValor;
		cob_salida.Otros_CheckOperacionContado = cob.Otros_CheckOperacionContado;
		cob_salida.Otros_CheckOperacionPlazo = cob.Otros_CheckOperacionPlazo;
		cob_salida.Otros_CheckInteresCompuesto = cob.Otros_CheckInteresCompuesto;
		cob_salida.Otros_CheckInteresSimple = cob.Otros_CheckInteresSimple;
		cob_salida.RangoCobros = new ArrayList<DefinicionRangoCobro>(cob.RangoCobros);
		cob_salida.PorcentualRango_IdMonedaRangoMonto = cob.PorcentualRango_IdMonedaRangoMonto;
		cob_salida.PorcentualRango_IdTipoRangoCobro = cob.PorcentualRango_IdTipoRangoCobro;
		return cob_salida;
	}
	
	
	public static void main(String[]args) throws Exception{
		
		Gson gson = Utils.getGsonBuilder();
		//String pruebaSinAllIn = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":69,\"p_IdModulo\":\"ORD\",\"p_Ordenes\":[{\"CantidadOrden\":123,\"DiasPlazo\":0,\"EsFungible\":\"N\",\"IdBolsa\":\"OUT\",\"IdCorredorCorresponsal\":4,\"IdInterno\":0,\"IdInstrumento\":471042,\"IdMonedaLiquidacionOrden\":\"USD\",\"IdPortafolio\":40694,\"IdTipoProducto\":\"IOPD\",\"IdTipoOperacion\":\"COMOPC\",\"IdTipoCanalIngreso\":\"AGE\",\"FechaOperacion\":\"2017-07-26\",\"MontoOrden\":0,\"PrecioOrden\":12,\"TasaSimultanea\":0,\"TasaRFIF\":0,\"TasaVentaCorta\":0,\"TipoCambio\":null,\"ValorRescateOrden\":0,\"MontoTotal\":147600,\"EsPorMonto\":\"N\",\"TasaReporte\":0,\"TasaReportado\":0,\"PlazoReporte\":0,\"BaseTasa\":0}],\"p_CobrosModificados\":[{\"IdInterno\":0,\"IdCobro\":\"IGV OPCIONES\",\"ValorCobroAplicado\":18},{\"IdInterno\":0,\"IdCobro\":\"COMISION SAB OPCIONES\",\"ValorCobroAplicado\":5}]}";
		//String pruebaAllInPorcentaje = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":69,\"p_IdModulo\":\"ORD\",\"p_Ordenes\":[{\"CantidadOrden\":20000,\"DiasPlazo\":65,\"EsFungible\":\"N\",\"IdBolsa\":\"BVL\",\"IdCorredorCorresponsal\":null,\"IdInterno\":0,\"IdInstrumento\":2250,\"IdInstrumentoSubyacente\":10000,\"IdMonedaLiquidacionOrden\":\"PEN\",\"IdPortafolio\":40780,\"IdTipoProducto\":\"RERV\",\"IdTipoOperacion\":\"COMRRV\",\"IdTipoCanalIngreso\":\"AGE\",\"FechaOperacion\":\"2018-04-10\",\"MontoOrden\":197400,\"PrecioOrden\":9.87,\"TasaSimultanea\":0,\"TasaRFIF\":0,\"TasaVentaCorta\":0,\"TipoCambio\":null,\"ValorRescateOrden\":0,\"PlazoSimultanea\":0,\"PlazoVentaCorta\":0,\"PlazoIF\":0,\"IdEmisor\":\"\",\"EsAPlazo\":\"N\",\"MontoTotal\":197400,\"EsPorMonto\":\"N\",\"TasaReporte\":7,\"TasaReportado\":4,\"PlazoReporte\":65,\"BaseTasa\":360,\"TipoInteres\":\"C\"}],\"p_CobrosModificados\":[]}";
		//String pruebaAllInMonto = "{\"p_IdEmpresa\":1,\fp_IdUsuario\":69,\"p_IdModulo\":\"ORD\",\"p_Ordenes\":[{\"CantidadOrden\":123,\"DiasPlazo\":0,\"EsFungible\":\"N\",\"IdBolsa\":\"OUT\",\"IdCorredorCorresponsal\":4,\"IdInterno\":0,\"IdInstrumento\":471042,\"IdMonedaLiquidacionOrden\":\"USD\",\"IdPortafolio\":40694,\"IdTipoProducto\":\"IOPD\",\"IdTipoOperacion\":\"COMOPC\",\"IdTipoCanalIngreso\":\"AGE\",\"FechaOperacion\":\"2017-07-26\",\"MontoOrden\":0,\"PrecioOrden\":12,\"TasaSimultanea\":0,\"TasaRFIF\":0,\"TasaVentaCorta\":0,\"TipoCambio\":null,\"ValorRescateOrden\":0,\"MontoTotal\":147600,\"EsPorMonto\":\"N\",\"TasaReporte\":0,\"TasaReportado\":0,\"PlazoReporte\":0,\"BaseTasa\":0}],\"p_CobrosModificados\":[{\"IdInterno\":0,\"IdCobro\":\"IGV OPCIONES\",\"ValorCobroAplicado\":18},{\"IdInterno\":0,\"IdCobro\":\"COMISION SAB OPCIONES\",\"ValorCobroAplicado\":5}]}";
		//String pp = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":69,\"p_IdModulo\":\"ORD\",\"p_Ordenes\":[{\"CantidadOrden\":100000,\"DiasPlazo\":30,\"EsFungible\":\"N\",\"IdBolsa\":\"BVL\",\"IdCorredorCorresponsal\":null,\"IdInterno\":0,\"IdInstrumento\":2254,\"IdInstrumentoSubyacente\":177563,\"IdMonedaLiquidacionOrden\":\"PEN\",\"IdPortafolio\":40917,\"IdTipoProducto\":\"RERF\",\"IdTipoOperacion\":\"VENRRF\",\"IdTipoCanalIngreso\":\"AGE\",\"FechaOperacion\":\"2018-04-10\",\"MontoOrden\":90000,\"PrecioOrden\":90,\"TasaSimultanea\":0,\"TasaRFIF\":0,\"TasaVentaCorta\":0,\"TipoCambio\":null,\"ValorRescateOrden\":0,\"PlazoSimultanea\":0,\"PlazoVentaCorta\":0,\"PlazoIF\":0,\"IdEmisor\":\"\",\"EsAPlazo\":\"N\",\"MontoTotal\":0,\"EsPorMonto\":\"N\",\"TasaReporte\":7,\"TasaReportado\":10,\"PlazoReporte\":30,\"BaseTasa\":360,\"TipoInteres\":\"C\"}],\"p_CobrosModificados\":[]}";
		//String pp = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":6096,\"p_IdModulo\":\"ORD\",\"p_Ordenes\":[{\"CantidadOrden\":22222,\"DiasPlazo\":0,\"EsFungible\":\"N\",\"IdBolsa\":\"BCS\",\"IdCorredorCorresponsal\":null,\"IdInterno\":0,\"IdInstrumento\":2223,\"IdInstrumentoSubyacente\":0,\"IdMonedaLiquidacionOrden\":\"USD\",\"IdPortafolio\":286907,\"IdTipoProducto\":\"INIF\",\"IdTipoOperacion\":\"COMIIF\",\"IdTipoCanalIngreso\":\"TRA\",\"FechaOperacion\":\"2018-07-20\",\"MontoOrden\":0,\"PrecioOrden\":0,\"TasaSimultanea\":0,\"TasaRFIF\":0,\"TasaVentaCorta\":0,\"TipoCambio\":null,\"ValorRescateOrden\":0,\"PlazoSimultanea\":0,\"PlazoVentaCorta\":0,\"PlazoIF\":0,\"IdEmisor\":\"\",\"EsAPlazo\":\"N\",\"MontoTotal\":0,\"EsPorMonto\":\"N\",\"TasaReporte\":0,\"TasaReportado\":0,\"PlazoReporte\":0,\"BaseTasa\":0}],\"p_CobrosModificados\":[]}";
		//String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":110,\"p_IdModulo\":\"ORD\",\"p_Ordenes\":[{\"CantidadOrden\":1000,\"DiasPlazo\":0,\"EsFungible\":\"N\",\"IdBolsa\":\"BVL\",\"IdCorredorCorresponsal\":null,\"IdInterno\":0,\"IdInstrumento\":48,\"IdInstrumentoSubyacente\":0,\"IdMonedaLiquidacionOrden\":\"USD\",\"IdPortafolio\":20021,\"IdTipoProducto\":\"INMI\",\"IdTipoOperacion\":\"COMMIL\",\"IdTipoCanalIngreso\":\"AGE\",\"FechaOperacion\":\"2018-07-31\",\"MontoOrden\":0,\"PrecioOrden\":600,\"TasaSimultanea\":0,\"TasaRFIF\":0,\"TasaVentaCorta\":0,\"TipoCambio\":null,\"ValorRescateOrden\":0,\"PlazoSimultanea\":0,\"PlazoVentaCorta\":0,\"PlazoIF\":0,\"IdEmisor\":\"\",\"EsAPlazo\":\"N\",\"MontoTotal\":0,\"EsPorMonto\":\"N\",\"TasaReporte\":0,\"TasaReportado\":0,\"PlazoReporte\":0,\"BaseTasa\":0}],\"p_CobrosModificados\":[]}";
		//String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"ORD\",\"p_Ordenes\":[{\"CantidadOrden\":100,\"DiasPlazo\":0,\"EsFungible\":\"N\",\"IdBolsa\":\"MIL\",\"IdCorredorCorresponsal\":10,\"IdInterno\":0,\"IdInstrumento\":19382,\"IdInstrumentoSubyacente\":0,\"IdMonedaLiquidacionOrden\":\"USD\",\"IdPortafolio\":162283,\"IdTipoProducto\":\"INRV\",\"IdTipoOperacion\":\"VENACN\",\"IdTipoCanalIngreso\":\"AGE\",\"FechaOperacion\":\"2018-04-10\",\"MontoOrden\":0,\"PrecioOrden\":24.5,\"TasaSimultanea\":0,\"TasaRFIF\":0,\"TasaVentaCorta\":0,\"TipoCambio\":null,\"ValorRescateOrden\":0,\"PlazoSimultanea\":0,\"PlazoVentaCorta\":0,\"PlazoIF\":0,\"IdEmisor\":\"\",\"EsAPlazo\":\"N\",\"MontoTotal\":0,\"EsPorMonto\":\"N\",\"TasaReporte\":0,\"TasaReportado\":0,\"PlazoReporte\":0,\"BaseTasa\":0}],\"p_CobrosModificados\":[]}";
		String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":0,\"p_IdModulo\":\"ORD\",\"p_Ordenes\":[{\"CantidadOrden\":0,\"DiasPlazo\":0,\"EsFungible\":\"N\",\"IdBolsa\":0,\"IdCorredorCorresponsal\":null,\"IdInterno\":0,\"IdInstrumento\":502,\"IdInstrumentoSubyacente\":0,\"IdMonedaLiquidacionOrden\":\"CLP\",\"IdPortafolio\":206341,\"IdTipoProducto\":\"INAC\",\"IdTipoOperacion\":\"COMACN\",\"IdTipoCanalIngreso\":\"AGE\",\"FechaOperacion\":\"2024-07-24\",\"MontoOrden\":0,\"PrecioOrden\":6956,\"TasaSimultanea\":0,\"TasaRFIF\":0,\"TasaVentaCorta\":0,\"TipoCambio\":null,\"ValorRescateOrden\":0,\"PlazoSimultanea\":0,\"PlazoVentaCorta\":0,\"PlazoIF\":0,\"IdEmisor\":\"\",\"EsAPlazo\":\"N\",\"MontoTotal\":10,\"EsPorMonto\":\"S\",\"TasaReporte\":0,\"TasaReportado\":0,\"PlazoReporte\":0,\"BaseTasa\":0}],\"p_CobrosModificados\":[]}";
		String peru = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":6408,\"p_IdModulo\":\"ORD\",\"p_Ordenes\":[{\"CantidadOrden\":20,\"DiasPlazo\":0,\"EsFungible\":\"N\",\"IdBolsa\":\"BVL\",\"IdCorredorCorresponsal\":null,\"IdInterno\":0,\"IdInstrumento\":401,\"IdInstrumentoSubyacente\":0,\"IdMonedaLiquidacionOrden\":\"USD\",\"IdPortafolio\":47940,\"IdTipoProducto\":\"INAX\",\"IdTipoOperacion\":\"COMACX\",\"IdTipoCanalIngreso\":\"AGE\",\"FechaOperacion\":\"2026-01-21\",\"MontoOrden\":0,\"PrecioOrden\":647,\"TasaSimultanea\":0,\"TasaRFIF\":0,\"TasaVentaCorta\":0,\"TipoCambio\":null,\"ValorRescateOrden\":0,\"PlazoSimultanea\":0,\"PlazoVentaCorta\":0,\"PlazoIF\":0,\"IdEmisor\":\"\",\"EsAPlazo\":\"N\",\"MontoTotal\":0,\"EsPorMonto\":\"N\",\"TasaReporte\":0,\"TasaReportado\":0,\"PlazoReporte\":0,\"BaseTasa\":0}],\"p_CobrosModificados\":[]}";	
		String jsonP = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":6408,\"p_IdModulo\":\"ORD\",\"p_Ordenes\":[{\"CantidadOrden\":1200,\"DiasPlazo\":0,\"EsFungible\":\"N\",\"IdBolsa\":\"BCS\",\"IdCorredorCorresponsal\":10,\"IdInterno\":0,\"IdInstrumento\":239,\"IdInstrumentoSubyacente\":0,\"IdMonedaLiquidacionOrden\":\"USD\",\"IdPortafolio\":47940,\"IdTipoProducto\":\"INMI\",\"IdTipoOperacion\":\"COMMIL\",\"IdTipoCanalIngreso\":\"AGE\",\"FechaOperacion\":\"2026-01-21\",\"MontoOrden\":0,\"PrecioOrden\":6500,\"TasaSimultanea\":0,\"TasaRFIF\":0,\"TasaVentaCorta\":0,\"TipoCambio\":null,\"ValorRescateOrden\":0,\"PlazoSimultanea\":0,\"PlazoVentaCorta\":0,\"PlazoIF\":0,\"IdEmisor\":\"\",\"EsAPlazo\":\"N\",\"MontoTotal\":0,\"EsPorMonto\":\"N\",\"TasaReporte\":0,\"TasaReportado\":0,\"PlazoReporte\":0,\"BaseTasa\":0}],\"p_CobrosModificados\":[{\"IdInterno\":0,\"IdCobro\":\"COMISION_SAB_MILA\",\"ValorCobroAplicado\":100},{\"IdInterno\":0,\"IdCobro\":\"COMISION_CUSTODIA_DCV\",\"ValorCobroAplicado\":8},{\"IdInterno\":0,\"IdCobro\":\"COMISION_CCR_CHILE\",\"ValorCobroAplicado\":0.6},{\"IdInterno\":0,\"IdCobro\":\"GASTOS _FIJOS_CCR_CHILE\",\"ValorCobroAplicado\":4500},{\"IdInterno\":0,\"IdCobro\":\"IGV\",\"ValorCobroAplicado\":18},{\"IdInterno\":0,\"IdCobro\":\"IMPUESTO_IVA\",\"ValorCobroAplicado\":19}]}";
		 
		CalcularMontoYCobrosOrden_Entrada c = gson.fromJson(jsonP, CalcularMontoYCobrosOrden_Entrada.class);
		
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa2", "connections.properties")){
			
			CalcularMontoYCobrosOrden cc = new CalcularMontoYCobrosOrden();
			CalcularMontoYCobrosOrden_Salida a = cc.logica(con,c);
			con.rollback();
			System.out.println(a.toString());
		}
			
	}
}

	