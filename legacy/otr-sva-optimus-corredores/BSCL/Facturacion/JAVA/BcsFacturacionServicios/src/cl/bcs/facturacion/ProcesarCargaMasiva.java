package cl.bcs.facturacion;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.math.RoundingMode;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.facturacion.CrearComprobanteNotaDeCredito.Comprobante_Entrada;
import cl.bcs.facturacion.CrearComprobanteNotaDeCredito.CrearComprobanteNotaDeCredito_Entrada;
import cl.bcs.facturacion.CrearComprobanteNotaDeCredito.CrearComprobanteNotaDeCredito_Salida;
import cl.bcs.facturacion.CrearComprobanteNotaDeCredito.Movimiento_Salida;
import cl.bcs.facturacion.CrearMovimientoComprobante.CrearMovimientoComprobante_Entrada;
import cl.bcs.facturacion.CrearMovimientoComprobante.CrearMovimientoComprobante_Salida;
import cl.bcs.facturacion.CrearMovimientoComprobante.MovimientoComprobanteCobro_Entrada;
import cl.bcs.facturacion.CrearMovimientoComprobante.MovimientoComprobanteFormaDePago_Entrada;
import cl.bcs.facturacion.CrearMovimientoComprobante.MovimientoComprobante_Entrada;
import cl.bcs.facturacion.CrearMovimientoComprobante.MovimientoComprobante_Salida;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente.FechasIn;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.OutObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.OutObtenerDiaSiguiente.FechasOut;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ProcesarCargaMasiva implements JavaCall {

	private final Log log = new Log("FAC.ProcesarCargaMasiva");
	private final String SQL_INSERT_CARGA_PROCESAR_PROCESO = loadResourceToString("cl/bcs/facturacion/sql/ProcesarCargaMasiva_InsertCargaProcesarProceso.sql");
	private final String SQL_SELECT_CARGA_PROCESAR_PROCESO = loadResourceToString("cl/bcs/facturacion/sql/ProcesarCargaMasiva_SelectCargaProcesarProceso.sql");
	private final String SQL_SELECT_DETALLE_CARGA_NOTACREDITO = loadResourceToString("cl/bcs/facturacion/sql/ProcesarCargaMasiva_SelectDetalleCargaNotaCredito.sql");
	private final String SQL_SELECT_DETALLE_CARGA_MOVIMIENTO = loadResourceToString("cl/bcs/facturacion/sql/ProcesarCargaMasiva_SelectDetalleCargaMovimiento.sql");
	private final String SQL_SELECT_DETALLE_CARGA_NOTACREDITOPARCIAL = loadResourceToString("cl/bcs/facturacion/sql/ProcesarCargaMasiva_SelectDetalleCargaNotaCreditoParcial.sql");
	private final String SQL_SELECT_VALOR_COBRO = loadResourceToString("cl/bcs/facturacion/sql/ProcesarCargaMasiva_SelectValorCobro.sql");	
	private final String SQL_UPDATE_CARGA_MASIVA = loadResourceToString("cl/bcs/facturacion/sql/ProcesarCargaMasiva_UpdateCargaMasiva.sql");
	private final String SQL_UPDATE_CARGA_MASIVA_DETALLE = loadResourceToString("cl/bcs/facturacion/sql/ProcesarCargaMasiva_UpdateCargaMasivaDetalle.sql");
	private final String SQL_SELECT_TIPO_COMPROBANTE_FACTURACION = loadResourceToString("cl/bcs/facturacion/sql/ProcesarCargaMasiva_SelectTipoComprobanteFacturacion.sql");
	private final String SQL_SELECT_FERIADOS = loadResourceToString("cl/bcs/facturacion/sql/ProcesarCargaMasiva_SelectFeriados.sql");
	
	private final String SI = "S";
	private final String NO = "N";
	private final String FECHA_DEFAULT = "0001-01-01";
	
	public static class ProcesarCargaMasiva_Entrada {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public String p_FechaProceso;
		public String p_EsCargaMasiva;
		public List<CargaProcesar> p_CargaMasivaProcesar;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class DetalleCargaNotaCredito {
		public Long IdCargaMasiva;
		public Long IdCargaMasivaDetalle;
		public String IdConcepto;
		public String IdTipoOperacion;
		public String DocumentoIdentidad;
		public Long IdPortafolio;
		public String CodigoPortafolio;
		public Long IdCliente;
		public String IdTipoComprobante;
		public String IdCondicionDeLiquidacion;
		public int PlazoEnDias;
		public String IdMoneda;
		public BigDecimal Monto;
		public String Glosa;
		public String Referencia;
		public String TipoReferencia;
		public String IdMedioDePago;
		public String IdModalidadDePago;
		public String CodigoArchivo;
		public Long IdEmpleadoDigitador;
		public String IdBolsa;
		public String IdBoveda;
		public String OperaCuentaInversion;
		public String TipoComprobanteDestino;
		public String FolioComprobante;
		public String AfectoImpuesto;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class DetalleCargaMovimiento {
		public Long IdCargaMasiva;
		public Long IdCargaMasivaDetalle;
		public String IdConcepto;
		public String IdTipoOperacion;
		public String DocumentoIdentidad;
		public Long IdPortafolio;
		public String CodigoPortafolio;
		public Long IdCliente;
		public String IdTipoComprobante;
		public String IdCondicionDeLiquidacion;
		public int PlazoEnDias;
		public String IdMoneda;
		public BigDecimal Monto;
		public String Glosa;
		public String Referencia;
		public String TipoReferencia;
		public String IdMedioDePago;
		public String IdModalidadDePago;
		public String CodigoArchivo;
		public Long IdEmpleadoDigitador;
		public String IdBolsa;
		public String IdBoveda;
		public String OperaCuentaInversion;
		public String AfectoImpuesto;
		public String Contrapartida;
		public String FechaLiquidacion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class CargaProcesar {
		public Long IdCargaMasiva;
		public Long IdTipoCargaMasiva;
		public String CodigoArchivo;
		public Long IdEmpleadoDigitador;
		public String IdBolsa;
		public String IdBoveda;
		public String FechaProceso;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class CargaProcesarDetalle {
		public Long NumeroMvtoCmbte;
		public Long IdMovimientoComprobante;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class CargaProcesar_Proceso extends CargaProcesar {
		public long errores = 0;
		public long advertencias = 0;
		public long autorizaciones = 0;
		public long ok = 0;
		
		public Long IdTipoCargaMasiva_Real;
		public String IdEstado;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class ProcesarCargaMasiva_Salida {
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR;
		public Long P_OUT_ADVERTENCIASCONTADOR;

		public ProcesarCargaMasiva_Salida() {
			P_OUT_MENSAJES = new ArrayList<>();
			P_OUT_ERRORESCONTADOR = 0L;
			P_OUT_ADVERTENCIASCONTADOR = 0L;
		}

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class AdvertenciaDetalle {
		public String Codigo;
		public String RequiereAutorizacion;
		public String Descripcion;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class ErrorDetalle {
		public String Codigo;
		public String Descripcion;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ValorCobro {
		public String IdCobro;
		public String NombreCobro;
		public BigDecimal ValorCobro;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class TipoComprobanteFacturacion {
		public String IdTipoComprobante;
		public String AfectoImpuesto;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		ProcesarCargaMasiva_Entrada entrada = gson.fromJson(in, ProcesarCargaMasiva_Entrada.class);
		ProcesarCargaMasiva_Salida salida = logica(conn, entrada);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(salida)).getAsJsonObject();
		return dataout;
	}

	ProcesarCargaMasiva_Salida logica(Connection conn, ProcesarCargaMasiva_Entrada entrada) throws Exception {
		log.write("INICIO");
		SQL _sql = new SQL();
		ProcesarCargaMasiva_Salida salida = new ProcesarCargaMasiva_Salida();
		Date fechaSistema = Fecha.toDate(ConfiguracionSistema.getParametroConfiguracionString(conn,entrada.p_IdEmpresa ,"PLATAFORMA","PLA.FechaSistema"));
		List<CargaProcesar_Proceso> cargas = new ArrayList<>();
		//si usa regla de negocio realiza las validaciones
		if ("S".equalsIgnoreCase(entrada.p_UsaReglasNegocio)) {
			RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
			RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(conn, entrada.p_IdEmpresa, entrada.p_IdUsuario, entrada.p_IdModulo);
			if (MensajeSalida.getNumeroErrores(salidaBasicos.p_out_Mensajes) > 0) {
				salida.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
				return salida;
			}
			if (entrada.p_CargaMasivaProcesar.isEmpty()) {
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"NO_SE_INGRESARON_CARGAS",null,conn, entrada.p_IdEmpresa));
				return salida;
			}
			if (!entrada.p_CargaMasivaProcesar.isEmpty()) {
				_sql.batch(conn, SQL_INSERT_CARGA_PROCESAR_PROCESO, entrada.p_CargaMasivaProcesar , CargaProcesar.class);
			}
			HashMap<String,Object> paramsIn1 = new HashMap<>();
			for (CargaProcesar carga : entrada.p_CargaMasivaProcesar) {
				paramsIn1.put("IdCargaMasiva", carga.IdCargaMasiva);
			}
			
			cargas = _sql.query(conn, SQL_SELECT_CARGA_PROCESAR_PROCESO, paramsIn1, CargaProcesar_Proceso.class);
			for (CargaProcesar_Proceso carga : cargas) {
				ProcesaRequisito(conn, entrada.p_IdEmpresa,(carga.IdTipoCargaMasiva_Real == null || carga.IdTipoCargaMasiva_Real.equals(0L)), "TIPO_CARGA_MASIVA_NO_EXISTE", MensajeSalida.MENSAJE_ERROR, carga, salida.P_OUT_MENSAJES);
				ProcesaRequisito(conn, entrada.p_IdEmpresa,(carga.IdEstado == null), "CARGA_MASIVA_NO_EXISTE", MensajeSalida.MENSAJE_ERROR, carga, salida.P_OUT_MENSAJES);
				ProcesaRequisito(conn, entrada.p_IdEmpresa,("PRO".equalsIgnoreCase(carga.IdEstado)), "CARGA_MASIVA_PROCESADA", MensajeSalida.MENSAJE_ERROR, carga, salida.P_OUT_MENSAJES);
				ProcesaRequisito(conn, entrada.p_IdEmpresa,("INV".equalsIgnoreCase(carga.IdEstado)), "CARGA_MASIVA_INVALIDA", MensajeSalida.MENSAJE_ERROR, carga, salida.P_OUT_MENSAJES);
			}
			//validar carga con errores
			List<CargaProcesar_Proceso> cargasConErrores = new ArrayList<>();
			limpiarCargas(cargas, cargasConErrores);
			if (cargas.isEmpty()) {
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR, "CARGAS_MASIVAS_CON_ERRORES", null, conn, entrada.p_IdEmpresa));
				return salida;
			}
		}
		
		//obtener detalles de carga
		List<Long> idsCarga = new ArrayList<>();
		for (CargaProcesar_Proceso carga : cargas) {
			carga.FechaProceso = entrada.p_FechaProceso;
			if (!idsCarga.contains(carga.IdCargaMasiva)) {
				idsCarga.add(carga.IdCargaMasiva);
			}
		}
		
		List<DetalleCargaNotaCredito> detallesCargaNotaCredito = new ArrayList<>();
		List<DetalleCargaMovimiento> detallesCargaMovimiento = new ArrayList<>();
		List<DetalleCargaNotaCredito> detallesCargaNotaCreditoParcial = new ArrayList<>();
		List<TipoComprobanteFacturacion> tipoComprobanteFacturacion = new ArrayList<>();
		
		HashMap<String,Object> paramIdCarga = new HashMap<>();		
		paramIdCarga.put("IdCargaMasiva", idsCarga);
		
		detallesCargaNotaCredito = _sql.query(conn, SQL_SELECT_DETALLE_CARGA_NOTACREDITO, paramIdCarga, DetalleCargaNotaCredito.class);
		detallesCargaMovimiento = _sql.query(conn, SQL_SELECT_DETALLE_CARGA_MOVIMIENTO, paramIdCarga, DetalleCargaMovimiento.class);
		detallesCargaNotaCreditoParcial = _sql.query(conn, SQL_SELECT_DETALLE_CARGA_NOTACREDITOPARCIAL, paramIdCarga, DetalleCargaNotaCredito.class);
		
		long cantidadFechasFaltantes = detallesCargaMovimiento.stream()
				.filter(data -> "OT".equals(data.IdCondicionDeLiquidacion)
						&& (data.FechaLiquidacion == null || data.FechaLiquidacion.isEmpty()))
				.collect(Collectors.counting());

		List<Date> fechaLiquidacionOT = detallesCargaMovimiento.stream()
				.filter(data -> "OT".equals(data.IdCondicionDeLiquidacion) && data.FechaLiquidacion != null && !data.FechaLiquidacion.isEmpty())
				.map(data -> Fecha.toDate(data.FechaLiquidacion)).distinct().collect(Collectors.toList());

		Calendar c = Calendar.getInstance();

		long cantidadInhabil = fechaLiquidacionOT.stream().filter(data -> {
			c.setTime(data);
			int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
			return 1 == dayOfWeek || 7 == dayOfWeek || data.before(fechaSistema);
		}).collect(Collectors.counting());

		HashMap<String, Object> paramsConsultaFeriado = new HashMap<>(2);
		paramsConsultaFeriado.put("Fechas", fechaLiquidacionOT);
		paramsConsultaFeriado.put("IdEmpresa", entrada.p_IdEmpresa);
		Long cantidadFeriados = _sql.countQuery(conn, SQL_SELECT_FERIADOS, paramsConsultaFeriado);

		if (cantidadInhabil > 0 || cantidadFeriados > 0 || cantidadFechasFaltantes > 0) {
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,
					"OT_FECHA_LIQUIDACION_INVALIDA", null, conn, entrada.p_IdEmpresa));
			return salida;
		}
		
		HashMap<String,Object> param = new HashMap<>();
		tipoComprobanteFacturacion = _sql.query(conn, SQL_SELECT_TIPO_COMPROBANTE_FACTURACION, param, TipoComprobanteFacturacion.class);
		
		List<ValorCobro> valorCobro = new ArrayList<>();
		HashMap<String,Object> paramValorCobro = new HashMap<>();
		paramValorCobro.put("IdCobro", "IVA");
		valorCobro = _sql.query(conn, SQL_SELECT_VALOR_COBRO, paramValorCobro, ValorCobro.class);
		BigDecimal MontoCobroCal = BigDecimal.ZERO;
		BigDecimal MontoMvto = BigDecimal.ZERO;
		
		OutObtenerDiaSiguiente outObtenerDiaSiguiente = new OutObtenerDiaSiguiente();
		HashMap<String,FechasOut> hashFechasOutDetalleCargaMovimiento = new HashMap<>();
		if (!detallesCargaMovimiento.isEmpty()) {
			ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
			InformacionEmpresa empresa = consultarEmpresaService.logica(conn, entrada.p_IdEmpresa);
			Long idPaisEmpresa = empresa.IdPais;
			ObtenerDiaSiguiente obtenerDiaSiguiente = new ObtenerDiaSiguiente();
			InObtenerDiaSiguiente inObtenerDiaSiguiente = new InObtenerDiaSiguiente();
			inObtenerDiaSiguiente.p_idPais = idPaisEmpresa;
			inObtenerDiaSiguiente.p_diaHabil = SI;
			inObtenerDiaSiguiente.p_soloCaeDiaHabil = SI;
			List<FechasIn> fechasDetallesCargaMovimiento = new ArrayList<>();
			FechasIn fechaIn;
			HashMap<String,FechasIn> hashFechasDetalleCargaMovimiento = new HashMap<>();
			for (DetalleCargaMovimiento detalleMovimiento : detallesCargaMovimiento) {
				String llaveDetalleMovimiento = entrada.p_FechaProceso + detalleMovimiento.PlazoEnDias;
				FechasIn fechaExistente = hashFechasDetalleCargaMovimiento.get(llaveDetalleMovimiento);
				if (fechaExistente == null) {
					fechaIn = new FechasIn();
					fechaIn.fechaInicial = entrada.p_FechaProceso;
					fechaIn.numeroDias = (long) detalleMovimiento.PlazoEnDias;
					fechasDetallesCargaMovimiento.add(fechaIn);
					hashFechasDetalleCargaMovimiento.put(llaveDetalleMovimiento, fechaIn);
				}
			}
			inObtenerDiaSiguiente.p_fechas = fechasDetallesCargaMovimiento;
			outObtenerDiaSiguiente = obtenerDiaSiguiente.logica(conn, inObtenerDiaSiguiente);
			for (FechasOut fecha : outObtenerDiaSiguiente.p_out_fechas) {
				String llave = fecha.fechaInicial + fecha.numeroDiasInicial;
				FechasOut fechaExistente = hashFechasOutDetalleCargaMovimiento.get(llave);
				if (fechaExistente == null) {
					hashFechasOutDetalleCargaMovimiento.put(llave, fecha);
				}
			}
		}
		
		if (!detallesCargaNotaCreditoParcial.isEmpty()) {
			ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
			InformacionEmpresa empresa = consultarEmpresaService.logica(conn, entrada.p_IdEmpresa);
			Long idPaisEmpresa = empresa.IdPais;
			ObtenerDiaSiguiente obtenerDiaSiguiente = new ObtenerDiaSiguiente();
			InObtenerDiaSiguiente inObtenerDiaSiguiente = new InObtenerDiaSiguiente();
			inObtenerDiaSiguiente.p_idPais = idPaisEmpresa;
			inObtenerDiaSiguiente.p_diaHabil = SI;
			inObtenerDiaSiguiente.p_soloCaeDiaHabil = SI;
			
			List<FechasIn> fechasDetalleCargaNotaCredito = new ArrayList<>();
			FechasIn fechaIn;
			HashMap<String,FechasIn> hashFechasDetalleCargaNotaCredito = new HashMap<>();
			
			for (DetalleCargaNotaCredito detalleNotaCreditoParcial : detallesCargaNotaCreditoParcial) {
				String llaveDetalleCargaNotaCredito = entrada.p_FechaProceso + detalleNotaCreditoParcial.PlazoEnDias;
				FechasIn fechaExistente = hashFechasDetalleCargaNotaCredito.get(llaveDetalleCargaNotaCredito);
				if (fechaExistente == null) {
					fechaIn = new FechasIn();
					fechaIn.fechaInicial = entrada.p_FechaProceso;
					fechaIn.numeroDias = (long) detalleNotaCreditoParcial.PlazoEnDias;
					fechasDetalleCargaNotaCredito.add(fechaIn);
					hashFechasDetalleCargaNotaCredito.put(llaveDetalleCargaNotaCredito, fechaIn);
				}
			}
			
			inObtenerDiaSiguiente.p_fechas = fechasDetalleCargaNotaCredito;
			outObtenerDiaSiguiente = obtenerDiaSiguiente.logica(conn, inObtenerDiaSiguiente);
			for (FechasOut fecha : outObtenerDiaSiguiente.p_out_fechas) {
				String llave = fecha.fechaInicial + fecha.numeroDiasInicial;
				FechasOut fechaExistente = hashFechasOutDetalleCargaMovimiento.get(llave);
				if (fechaExistente == null) {
					hashFechasOutDetalleCargaMovimiento.put(llave, fecha);
				}
			}
		}

		//Variables de configuracion
		String permiteNotasCredParcial = ConfiguracionSistema.getParametroConfiguracionString(conn, entrada.p_IdEmpresa, "FACTURACION", "NOTASCREDITOPARCIAL");
		String afectoImpuesto = ConfiguracionSistema.getParametroConfiguracionString(conn, entrada.p_IdEmpresa, "FACTURACION", "CARGAMASIVAAFECTAIMPUESTO");
		String localizacionSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.LocalizacionSistema");
						
		if (permiteNotasCredParcial.equalsIgnoreCase("S") && !detallesCargaNotaCreditoParcial.isEmpty()) {			
			
			// Crea Nota de credito parcial
			CrearMovimientoComprobante crearMovimientoComprobante = new CrearMovimientoComprobante();
			CrearMovimientoComprobante_Entrada crearMovimientoComprobanteEntrada = new CrearMovimientoComprobante_Entrada();
			crearMovimientoComprobanteEntrada.p_IdEmpresa = entrada.p_IdEmpresa;
			crearMovimientoComprobanteEntrada.p_IdUsuario = entrada.p_IdUsuario;                                                                          
			crearMovimientoComprobanteEntrada.p_IdModulo = entrada.p_IdModulo;                                                        
			crearMovimientoComprobanteEntrada.p_simularOperacion = NO;
			crearMovimientoComprobanteEntrada.p_UsaReglasNegocio = NO;
			crearMovimientoComprobanteEntrada.p_EsIngresoAutomatico = SI;
			crearMovimientoComprobanteEntrada.p_EsCargaMasiva = SI;
			MovimientoComprobante_Entrada movimientoComprobanteEntrada;
			Long secuenciaCobros = 0L;
			
			for (DetalleCargaNotaCredito detalle : detallesCargaNotaCreditoParcial) {
				String tipoCmbteAfectoImpuesto = tipoComprobanteAfectoImpuesto(tipoComprobanteFacturacion,detalle.IdTipoComprobante,afectoImpuesto);
				String llaveDetalle = entrada.p_FechaProceso + detalle.PlazoEnDias;
				FechasOut fechaOut = hashFechasOutDetalleCargaMovimiento.get(llaveDetalle);
				
				if ("S".equalsIgnoreCase(afectoImpuesto) && "S".equalsIgnoreCase(tipoCmbteAfectoImpuesto)) {
					MontoCobroCal = (detalle.Monto.multiply(valorCobro.get(0).ValorCobro)).divide(new BigDecimal(100));	
				};
				
				if ("VENOTC".equalsIgnoreCase(detalle.IdTipoOperacion)) {
					MontoMvto = detalle.Monto.subtract(MontoCobroCal);	
				}else {
					MontoMvto = detalle.Monto.add(MontoCobroCal);
				}
				
				movimientoComprobanteEntrada = new MovimientoComprobante_Entrada();
				movimientoComprobanteEntrada.NumeroMvtoCmbte = detalle.IdCargaMasivaDetalle;
				movimientoComprobanteEntrada.IdEmpresa = entrada.p_IdEmpresa;
				movimientoComprobanteEntrada.IdConcepto = detalle.IdConcepto;
				movimientoComprobanteEntrada.SecuenciaAgrupacion = "";
				movimientoComprobanteEntrada.IdCliente = detalle.IdCliente;
				movimientoComprobanteEntrada.IdPortafolio = detalle.IdPortafolio;
				movimientoComprobanteEntrada.ModPrefFact = "MODPREFNORM";
				movimientoComprobanteEntrada.IdEmpleadoDigitador = detalle.IdEmpleadoDigitador;
				movimientoComprobanteEntrada.IdInstrumento = 0L;
				movimientoComprobanteEntrada.NemotecnicoInstrumento = "";
				movimientoComprobanteEntrada.IdInstrumentoRelacionado = 0L;
				movimientoComprobanteEntrada.IdInstrumentoSubyacente = 0L;
				movimientoComprobanteEntrada.IdTipoProducto = "";
				movimientoComprobanteEntrada.IdMoneda = detalle.IdMoneda;
				movimientoComprobanteEntrada.IdMonedaSubyacente = "";
				movimientoComprobanteEntrada.IdMonedaLiquidacion = detalle.IdMoneda;
				movimientoComprobanteEntrada.IdMonedaSpot = "";
				movimientoComprobanteEntrada.IdTipoComprobante = detalle.IdTipoComprobante;
				movimientoComprobanteEntrada.IdTipoOperacion = detalle.IdTipoOperacion;
				movimientoComprobanteEntrada.IdBoveda = detalle.IdBoveda;
				movimientoComprobanteEntrada.IdCorredoraContraparte = 0L;
				movimientoComprobanteEntrada.IdCondicionLiquidacion = detalle.IdCondicionDeLiquidacion;
				movimientoComprobanteEntrada.FechaLiquidacion = fechaOut.fechaCalculada;
				movimientoComprobanteEntrada.IdBolsa = detalle.IdBolsa;
				movimientoComprobanteEntrada.IdTipoReajuste = "";
				movimientoComprobanteEntrada.IdTipoConvenio = "SCV";
				movimientoComprobanteEntrada.IdFormaCustodia = "";
				movimientoComprobanteEntrada.IdEmisor = "";
				movimientoComprobanteEntrada.IdOrden = 0L;
				movimientoComprobanteEntrada.IdAsignacion = 0L;
				movimientoComprobanteEntrada.IdAsignacionRef = 0L;
				movimientoComprobanteEntrada.IdTipoCanalIngreso = "";
				movimientoComprobanteEntrada.FechaOrden = FECHA_DEFAULT;
				movimientoComprobanteEntrada.IdTransaccion = 0L;
				movimientoComprobanteEntrada.FolioTransaccion = "";
				movimientoComprobanteEntrada.FechaTransaccion = FECHA_DEFAULT;
				movimientoComprobanteEntrada.FechaVencimiento = FECHA_DEFAULT;
				movimientoComprobanteEntrada.PrecioAPlazo = BigDecimal.ZERO;
				movimientoComprobanteEntrada.MontoAPlazo = BigDecimal.ZERO;
				movimientoComprobanteEntrada.FechaParidad = FECHA_DEFAULT;
				movimientoComprobanteEntrada.ValorParidad = BigDecimal.ZERO;
				movimientoComprobanteEntrada.FechaTipoCambio = FECHA_DEFAULT;
				movimientoComprobanteEntrada.ValorTipoCambio = BigDecimal.ZERO;
				movimientoComprobanteEntrada.ValorCaptacionMonedaOrigen = BigDecimal.ZERO;
				movimientoComprobanteEntrada.Tasa = BigDecimal.ZERO;
				movimientoComprobanteEntrada.Cantidad = BigDecimal.ZERO;
				movimientoComprobanteEntrada.Precio = BigDecimal.ZERO;
				movimientoComprobanteEntrada.Monto = detalle.Monto;
				movimientoComprobanteEntrada.MontoMovimientoComprobante = detalle.Monto;
				movimientoComprobanteEntrada.MontoLiquidacion = detalle.Monto;
				movimientoComprobanteEntrada.MontoDescuento = BigDecimal.ZERO;
				movimientoComprobanteEntrada.MontoInteres = BigDecimal.ZERO;
				movimientoComprobanteEntrada.Glosa = detalle.Glosa;
				movimientoComprobanteEntrada.Referencia = detalle.Referencia;
				movimientoComprobanteEntrada.TipoReferencia = detalle.TipoReferencia;
				movimientoComprobanteEntrada.ValorRescate = BigDecimal.ZERO;
				movimientoComprobanteEntrada.TasaAgente = BigDecimal.ZERO;
				movimientoComprobanteEntrada.Spread = BigDecimal.ZERO;
				movimientoComprobanteEntrada.DiasPlazo = BigDecimal.ZERO;
				movimientoComprobanteEntrada.FechaMovimientoComprobante = entrada.p_FechaProceso;
				movimientoComprobanteEntrada.IdMesa = 0L;
				movimientoComprobanteEntrada.FolioCartera = 0L;
				movimientoComprobanteEntrada.FechaEmision = FECHA_DEFAULT;
				movimientoComprobanteEntrada.TasaEmision = BigDecimal.ZERO;
				movimientoComprobanteEntrada.EsComprobIndividual = SI;
				movimientoComprobanteEntrada.EsCuentaPropia = NO;
				movimientoComprobanteEntrada.IndicadorOrdenDirecta = NO;
				movimientoComprobanteEntrada.EsVentaCorta = NO;
				movimientoComprobanteEntrada.EsPrecioPrima = NO;
				movimientoComprobanteEntrada.EsEnRueda = SI;
				movimientoComprobanteEntrada.EsRuteo = NO;
				movimientoComprobanteEntrada.EsAPlazo = NO;
				movimientoComprobanteEntrada.EsFungible = NO;
				movimientoComprobanteEntrada.ActualizarCustodia = NO;
				movimientoComprobanteEntrada.ActualizarCuentaInversion = SI;
				movimientoComprobanteEntrada.IdComprobanteReferencia = Long.parseLong(detalle.FolioComprobante);
				
				if (SI.equalsIgnoreCase(detalle.OperaCuentaInversion) && "CIN".equalsIgnoreCase(detalle.IdMedioDePago)) {
					movimientoComprobanteEntrada.ActualizarTesoreria = NO;
				} else if (SI.equalsIgnoreCase(detalle.OperaCuentaInversion) && !"CIN".equalsIgnoreCase(detalle.IdMedioDePago)) {
					movimientoComprobanteEntrada.ActualizarTesoreria = SI;
				}
				
				movimientoComprobanteEntrada.ActualizarCustodiaRelacionado = NO;
				movimientoComprobanteEntrada.CrearComprobanteAutomatico = SI;
				movimientoComprobanteEntrada.IdUsuario = entrada.p_IdUsuario;
				crearMovimientoComprobanteEntrada.p_Movimientos.add(movimientoComprobanteEntrada);				
				
				if ("S".equalsIgnoreCase(afectoImpuesto) && "S".equalsIgnoreCase(tipoCmbteAfectoImpuesto)) {
					BigDecimal cobro;
					if (!valorCobro.isEmpty()) cobro = valorCobro.get(0).ValorCobro;
					else cobro = new BigDecimal(0);
					MovimientoComprobanteCobro_Entrada movimientoComprobanteCobro = new MovimientoComprobanteCobro_Entrada();
					movimientoComprobanteCobro.NumeroMvtoCmbte = detalle.IdCargaMasivaDetalle;
					movimientoComprobanteCobro.NumeroMvtoCmbteCobro = secuenciaCobros++;
					movimientoComprobanteCobro.IdCobro = "IGV_OC";
					movimientoComprobanteCobro.IdMonedaCobro = detalle.IdMoneda;
					movimientoComprobanteCobro.IdMonedaLiquidacionCobro = detalle.IdMoneda;
					movimientoComprobanteCobro.MontoCobro = MontoCobroCal;
					movimientoComprobanteCobro.MontoMovimientoComprobanteCobro = MontoCobroCal;
					movimientoComprobanteCobro.MontoLiquidacionCobro = MontoCobroCal;
					movimientoComprobanteCobro.Porcentaje = cobro;
					crearMovimientoComprobanteEntrada.p_MovimientosCobros.add(movimientoComprobanteCobro);
				}
				
			}
			CrearMovimientoComprobante_Salida crearMovimientoComprobanteSalida = crearMovimientoComprobante.logica(conn, crearMovimientoComprobanteEntrada);
			if (!crearMovimientoComprobanteSalida.p_out_Movimientos.isEmpty()) {
				_sql.batch(conn, SQL_UPDATE_CARGA_MASIVA_DETALLE, crearMovimientoComprobanteSalida.p_out_Movimientos, MovimientoComprobante_Salida.class);
			}
			salida.P_OUT_MENSAJES.addAll(crearMovimientoComprobanteSalida.P_OUT_MENSAJES);
		
		}else {
			// Crea Nota de credito
			if (!detallesCargaNotaCredito.isEmpty()) {
				CrearComprobanteNotaDeCredito crearComprobanteNotaCredito = new CrearComprobanteNotaDeCredito();
				CrearComprobanteNotaDeCredito_Entrada crearComprobanteNotaCreditoEntrada = new CrearComprobanteNotaDeCredito_Entrada();
				crearComprobanteNotaCreditoEntrada.p_IdEmpresa = entrada.p_IdEmpresa;
				crearComprobanteNotaCreditoEntrada.p_IdUsuario = entrada.p_IdUsuario;
				crearComprobanteNotaCreditoEntrada.p_IdModulo = entrada. p_IdModulo;                                                      
				crearComprobanteNotaCreditoEntrada.p_simularOperacion = NO;
				crearComprobanteNotaCreditoEntrada.p_UsaReglasNegocio = SI;
				crearComprobanteNotaCreditoEntrada.p_EsIngresoAutomatico = NO;
				crearComprobanteNotaCreditoEntrada.p_CrearComprobanteAutomatico = SI;
				crearComprobanteNotaCreditoEntrada.p_EsCargaMasiva = SI;
				Comprobante_Entrada comprobanteEntrada;
				for (DetalleCargaNotaCredito detalle : detallesCargaNotaCredito) {
					comprobanteEntrada = new Comprobante_Entrada();
					comprobanteEntrada.IdPortafolio = detalle.IdPortafolio;
					comprobanteEntrada.IdCliente = detalle.IdCliente;
					comprobanteEntrada.IdComprobante = Long.parseLong(detalle.FolioComprobante);
					comprobanteEntrada.IdTipoComprobante = detalle.TipoComprobanteDestino;
					comprobanteEntrada.IdCargaMasivaDetalle = detalle.IdCargaMasivaDetalle;
					crearComprobanteNotaCreditoEntrada.P_COMPROBANTE.add(comprobanteEntrada);
				}
				
				
				List<Long> comprobanteReferenciaRepetido = crearComprobanteNotaCreditoEntrada.P_COMPROBANTE.stream().collect(Collectors.groupingBy(s -> s.IdComprobante)).entrySet().stream().filter(e -> e.getValue().size() > 1).map(e -> e.getKey()).collect(Collectors.toList());
				if (!comprobanteReferenciaRepetido.isEmpty()) {
					for (Long idComprobanteReferenciaRepetido : comprobanteReferenciaRepetido) {
						String codigo = "CMBTE_ID_COMPROBANTE_REFERENCIA_REPETIDO";
						String descripcion = Diccionario.getMensaje(conn, crearComprobanteNotaCreditoEntrada.p_IdEmpresa, codigo).add("IdComprobanteReferencia", idComprobanteReferenciaRepetido).msg();
						salida.P_OUT_MENSAJES.add(new MensajeSalida(idComprobanteReferenciaRepetido, MensajeSalida.MENSAJE_ERROR, codigo, descripcion));
					}
					return salida;
				}
	
				CrearComprobanteNotaDeCredito_Salida crearComprobanteNotaCreditoSalida = crearComprobanteNotaCredito.logica(conn, crearComprobanteNotaCreditoEntrada);
				if (crearComprobanteNotaCreditoSalida != null) {
					List<CargaProcesarDetalle> cargaProcesoDetalles = new ArrayList<>();
					CargaProcesarDetalle cargaProcesoDetalle;
					for (Movimiento_Salida movimientoSalida : crearComprobanteNotaCreditoSalida.P_OUT_MOVIMIENTO) {  
						for (Comprobante_Entrada comprobanteEntradaDetalle : crearComprobanteNotaCreditoEntrada.P_COMPROBANTE) {
							if (comprobanteEntradaDetalle.IdComprobante.equals(movimientoSalida.IdComprobanteReferencia)) {
								cargaProcesoDetalle = new CargaProcesarDetalle();
								cargaProcesoDetalle.NumeroMvtoCmbte = comprobanteEntradaDetalle.IdCargaMasivaDetalle;
								cargaProcesoDetalle.IdMovimientoComprobante = movimientoSalida.IdMovimientoComprobante;
								cargaProcesoDetalles.add(cargaProcesoDetalle);
							}
						}
					}
					if (!cargaProcesoDetalles.isEmpty()) {
						_sql.batch(conn, SQL_UPDATE_CARGA_MASIVA_DETALLE, cargaProcesoDetalles, CargaProcesarDetalle.class);
					}
				}
				salida.P_OUT_MENSAJES.addAll(crearComprobanteNotaCreditoSalida.P_OUT_MENSAJES);
			} else if (!detallesCargaMovimiento.isEmpty()) {
						CrearMovimientoComprobante crearMovimientoComprobante = new CrearMovimientoComprobante();
						CrearMovimientoComprobante_Entrada crearMovimientoComprobanteEntrada = new CrearMovimientoComprobante_Entrada();
						crearMovimientoComprobanteEntrada.p_IdEmpresa = entrada.p_IdEmpresa;
						crearMovimientoComprobanteEntrada.p_IdUsuario = entrada.p_IdUsuario;                                                                          
						crearMovimientoComprobanteEntrada.p_IdModulo = entrada.p_IdModulo;                                                        
						crearMovimientoComprobanteEntrada.p_simularOperacion = NO;
						crearMovimientoComprobanteEntrada.p_UsaReglasNegocio = NO;
						crearMovimientoComprobanteEntrada.p_EsIngresoAutomatico = SI;
						crearMovimientoComprobanteEntrada.p_EsCargaMasiva = SI;
						MovimientoComprobante_Entrada movimientoComprobanteEntrada;
						MovimientoComprobanteFormaDePago_Entrada movimientoComprobanteFormaPagoEntrada;
						Long secuenciaFormasPago = 0L;
						Long secuenciaCobros = 0L;
						for (DetalleCargaMovimiento detalle : detallesCargaMovimiento) {
							String tipoCmbteAfectoImpuesto = tipoComprobanteAfectoImpuesto(tipoComprobanteFacturacion,detalle.IdTipoComprobante,afectoImpuesto);
							String llaveDetalle = entrada.p_FechaProceso + detalle.PlazoEnDias;
							FechasOut fechaOut = hashFechasOutDetalleCargaMovimiento.get(llaveDetalle);
							
							if ("S".equalsIgnoreCase(afectoImpuesto) && "S".equalsIgnoreCase(tipoCmbteAfectoImpuesto)) {
								MontoCobroCal = (detalle.Monto.multiply(valorCobro.get(0).ValorCobro)).divide(new BigDecimal(100));	
							};
							
							if ("VENOTC".equalsIgnoreCase(detalle.IdTipoOperacion)) {
								MontoMvto = detalle.Monto.subtract(MontoCobroCal);	
							}else {
								MontoMvto = detalle.Monto.add(MontoCobroCal);
							}
							
							movimientoComprobanteEntrada = new MovimientoComprobante_Entrada();
							movimientoComprobanteEntrada.NumeroMvtoCmbte = detalle.IdCargaMasivaDetalle;
							movimientoComprobanteEntrada.IdEmpresa = entrada.p_IdEmpresa;
							movimientoComprobanteEntrada.IdConcepto = detalle.IdConcepto;
							movimientoComprobanteEntrada.SecuenciaAgrupacion = "";
							movimientoComprobanteEntrada.IdCliente = detalle.IdCliente;
							movimientoComprobanteEntrada.IdPortafolio = detalle.IdPortafolio;
							movimientoComprobanteEntrada.ModPrefFact = "MODPREFNORM";
							movimientoComprobanteEntrada.IdEmpleadoDigitador = detalle.IdEmpleadoDigitador;
							movimientoComprobanteEntrada.IdInstrumento = 0L;
							movimientoComprobanteEntrada.NemotecnicoInstrumento = "";
							movimientoComprobanteEntrada.IdInstrumentoRelacionado = 0L;
							movimientoComprobanteEntrada.IdInstrumentoSubyacente = 0L;
							movimientoComprobanteEntrada.IdTipoProducto = "";
							movimientoComprobanteEntrada.IdMoneda = detalle.IdMoneda;
							movimientoComprobanteEntrada.IdMonedaSubyacente = "";
							movimientoComprobanteEntrada.IdMonedaLiquidacion = detalle.IdMoneda;
							movimientoComprobanteEntrada.IdMonedaSpot = "";
							movimientoComprobanteEntrada.IdTipoComprobante = detalle.IdTipoComprobante;
							movimientoComprobanteEntrada.IdTipoOperacion = detalle.IdTipoOperacion;
							movimientoComprobanteEntrada.IdBoveda = detalle.IdBoveda;
							movimientoComprobanteEntrada.IdCorredoraContraparte = 0L;
							movimientoComprobanteEntrada.IdCondicionLiquidacion = detalle.IdCondicionDeLiquidacion;
							movimientoComprobanteEntrada.FechaLiquidacion = "OT".equals(detalle.IdCondicionDeLiquidacion) ? detalle.FechaLiquidacion : fechaOut.fechaCalculada;
							movimientoComprobanteEntrada.IdBolsa = detalle.IdBolsa;
							movimientoComprobanteEntrada.IdTipoReajuste = "";
							movimientoComprobanteEntrada.IdTipoConvenio = "SCV";
							movimientoComprobanteEntrada.IdFormaCustodia = "";
							movimientoComprobanteEntrada.IdEmisor = "";
							movimientoComprobanteEntrada.IdOrden = 0L;
							movimientoComprobanteEntrada.IdAsignacion = 0L;
							movimientoComprobanteEntrada.IdAsignacionRef = 0L;
							movimientoComprobanteEntrada.IdTipoCanalIngreso = "";
							movimientoComprobanteEntrada.FechaOrden = FECHA_DEFAULT;
							movimientoComprobanteEntrada.IdTransaccion = 0L;
							movimientoComprobanteEntrada.FolioTransaccion = "";
							movimientoComprobanteEntrada.FechaTransaccion = FECHA_DEFAULT;
							movimientoComprobanteEntrada.FechaVencimiento = FECHA_DEFAULT;
							movimientoComprobanteEntrada.PrecioAPlazo = BigDecimal.ZERO;
							movimientoComprobanteEntrada.MontoAPlazo = BigDecimal.ZERO;
							movimientoComprobanteEntrada.FechaParidad = FECHA_DEFAULT;
							movimientoComprobanteEntrada.ValorParidad = BigDecimal.ZERO;
							movimientoComprobanteEntrada.FechaTipoCambio = FECHA_DEFAULT;
							movimientoComprobanteEntrada.ValorTipoCambio = BigDecimal.ZERO;
							movimientoComprobanteEntrada.ValorCaptacionMonedaOrigen = BigDecimal.ZERO;
							movimientoComprobanteEntrada.Tasa = BigDecimal.ZERO;
							movimientoComprobanteEntrada.Cantidad = BigDecimal.ZERO;
							movimientoComprobanteEntrada.Precio = BigDecimal.ZERO;
							movimientoComprobanteEntrada.Monto = detalle.Monto;
							movimientoComprobanteEntrada.MontoMovimientoComprobante = detalle.Monto;
							movimientoComprobanteEntrada.MontoLiquidacion = detalle.Monto;
							movimientoComprobanteEntrada.MontoDescuento = BigDecimal.ZERO;
							movimientoComprobanteEntrada.MontoInteres = BigDecimal.ZERO;
							movimientoComprobanteEntrada.Glosa = detalle.Glosa;
							movimientoComprobanteEntrada.Referencia = detalle.Referencia;
							movimientoComprobanteEntrada.TipoReferencia = detalle.TipoReferencia;
							movimientoComprobanteEntrada.ValorRescate = BigDecimal.ZERO;
							movimientoComprobanteEntrada.TasaAgente = BigDecimal.ZERO;
							movimientoComprobanteEntrada.Spread = BigDecimal.ZERO;
							movimientoComprobanteEntrada.DiasPlazo = BigDecimal.ZERO;
							movimientoComprobanteEntrada.FechaMovimientoComprobante = entrada.p_FechaProceso;
							movimientoComprobanteEntrada.IdMesa = 0L;
							movimientoComprobanteEntrada.FolioCartera = 0L;
							movimientoComprobanteEntrada.FechaEmision = FECHA_DEFAULT;
							movimientoComprobanteEntrada.TasaEmision = BigDecimal.ZERO;
							movimientoComprobanteEntrada.EsComprobIndividual = SI;
							movimientoComprobanteEntrada.EsCuentaPropia = NO;
							movimientoComprobanteEntrada.IndicadorOrdenDirecta = NO;
							movimientoComprobanteEntrada.EsVentaCorta = NO;
							movimientoComprobanteEntrada.EsPrecioPrima = NO;
							movimientoComprobanteEntrada.EsEnRueda = SI;
							movimientoComprobanteEntrada.EsRuteo = NO;
							movimientoComprobanteEntrada.EsAPlazo = NO;
							movimientoComprobanteEntrada.EsFungible = NO;
							movimientoComprobanteEntrada.ActualizarCustodia = NO;
							movimientoComprobanteEntrada.ActualizarCuentaInversion = SI;
							if (SI.equalsIgnoreCase(detalle.OperaCuentaInversion) && "CIN".equalsIgnoreCase(detalle.IdMedioDePago)) {
								movimientoComprobanteEntrada.ActualizarTesoreria = NO;
							} else if (SI.equalsIgnoreCase(detalle.OperaCuentaInversion) && !"CIN".equalsIgnoreCase(detalle.IdMedioDePago)) {
								movimientoComprobanteEntrada.ActualizarTesoreria = SI;
							}else {
								movimientoComprobanteEntrada.ActualizarTesoreria = SI;
							}
							
							movimientoComprobanteEntrada.ActualizarCustodiaRelacionado = NO;
							movimientoComprobanteEntrada.CrearComprobanteAutomatico = NO;
							movimientoComprobanteEntrada.IdUsuario = entrada.p_IdUsuario;
							movimientoComprobanteEntrada.Contrapartida = detalle.Contrapartida;
							crearMovimientoComprobanteEntrada.p_Movimientos.add(movimientoComprobanteEntrada);						
													
							
							if (detalle.IdMedioDePago != null && !detalle.IdMedioDePago.isEmpty()) {
								movimientoComprobanteFormaPagoEntrada = new MovimientoComprobanteFormaDePago_Entrada();
								movimientoComprobanteFormaPagoEntrada.NumeroMvtoCmbte = detalle.IdCargaMasivaDetalle;
								movimientoComprobanteFormaPagoEntrada.NumeroMvtoCmbteFormaDePago = secuenciaFormasPago++;
								movimientoComprobanteFormaPagoEntrada.IdMedioDePago = detalle.IdMedioDePago;
								movimientoComprobanteFormaPagoEntrada.NumeroSerie = "";
								movimientoComprobanteFormaPagoEntrada.NumeroDocumento = 0L;
								movimientoComprobanteFormaPagoEntrada.IdCuentaBancaria = 0L;
								movimientoComprobanteFormaPagoEntrada.IdTipoCuentaBancaria = "";
								movimientoComprobanteFormaPagoEntrada.NumeroCuentaBancaria = "";
								movimientoComprobanteFormaPagoEntrada.IdTarjetaBancaria = 0L;
								movimientoComprobanteFormaPagoEntrada.IdTipoTarjetaBancaria = "";
								movimientoComprobanteFormaPagoEntrada.NumeroTarjetaBancaria = "";
								movimientoComprobanteFormaPagoEntrada.FechaVctoTarjetaBancaria = FECHA_DEFAULT;
								movimientoComprobanteFormaPagoEntrada.IdModalidadDePago = detalle.IdModalidadDePago == null ? "" : detalle.IdModalidadDePago;
								movimientoComprobanteFormaPagoEntrada.IdBanco = 0L;
								movimientoComprobanteFormaPagoEntrada.IdMoneda = detalle.IdMoneda;
								movimientoComprobanteFormaPagoEntrada.FechaDocumentoPago = FECHA_DEFAULT;
								movimientoComprobanteFormaPagoEntrada.MontoLiquidacionFormaDePago = detalle.IdMoneda.equalsIgnoreCase("CLP") ? MontoMvto.setScale(0, RoundingMode.HALF_UP) : MontoMvto;
								movimientoComprobanteFormaPagoEntrada.IdTipoMovimientoSpot = "";
								crearMovimientoComprobanteEntrada.p_MovimientosFormaDePago.add(movimientoComprobanteFormaPagoEntrada);
							}
														
							if ("S".equalsIgnoreCase(afectoImpuesto) && "S".equalsIgnoreCase(tipoCmbteAfectoImpuesto)) {
								BigDecimal cobro;
								if (!valorCobro.isEmpty()) cobro = valorCobro.get(0).ValorCobro;
								else cobro = new BigDecimal(0);
								MovimientoComprobanteCobro_Entrada movimientoComprobanteCobro = new MovimientoComprobanteCobro_Entrada();
								movimientoComprobanteCobro.NumeroMvtoCmbte = detalle.IdCargaMasivaDetalle;
								movimientoComprobanteCobro.NumeroMvtoCmbteCobro = secuenciaCobros++;
								
								if ("PE".equalsIgnoreCase(localizacionSistema)) {
									movimientoComprobanteCobro.IdCobro = "IGV_OC";
								}else {
									movimientoComprobanteCobro.IdCobro = "IVA";
								}

								movimientoComprobanteCobro.IdMonedaCobro = detalle.IdMoneda;
								movimientoComprobanteCobro.IdMonedaLiquidacionCobro = detalle.IdMoneda;
								movimientoComprobanteCobro.MontoCobro = MontoCobroCal;
								movimientoComprobanteCobro.MontoMovimientoComprobanteCobro = MontoCobroCal;
								movimientoComprobanteCobro.MontoLiquidacionCobro = MontoCobroCal;
								movimientoComprobanteCobro.Porcentaje = cobro;
								crearMovimientoComprobanteEntrada.p_MovimientosCobros.add(movimientoComprobanteCobro);
							}
					}
					CrearMovimientoComprobante_Salida crearMovimientoComprobanteSalida = crearMovimientoComprobante.logica(conn, crearMovimientoComprobanteEntrada);
					if (!crearMovimientoComprobanteSalida.p_out_Movimientos.isEmpty()) {
						_sql.batch(conn, SQL_UPDATE_CARGA_MASIVA_DETALLE, crearMovimientoComprobanteSalida.p_out_Movimientos, MovimientoComprobante_Salida.class);
					}
					salida.P_OUT_MENSAJES.addAll(crearMovimientoComprobanteSalida.P_OUT_MENSAJES);
				}
	  }	///termina
		salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
		if (salida.P_OUT_ERRORESCONTADOR == 0) {
			_sql.batch(conn, SQL_UPDATE_CARGA_MASIVA, cargas, CargaProcesar_Proceso.class);
		}
		return salida;
	}
	
	private void ProcesaRequisito(Connection con, Long idEmpresa, boolean esError, String codMsg, String CodRequisito, CargaProcesar_Proceso cargaProcesar, List<MensajeSalida> P_OUT_MENSAJES) 
			throws Exception {
		if (esError) {
			String msg=Diccionario.getMensaje(con,idEmpresa,codMsg)
				  .add("IdTipoCargaMasiva", cargaProcesar.IdTipoCargaMasiva)
				  .msg();
			if (CodRequisito.equals(MensajeSalida.MENSAJE_ERROR)) {
				P_OUT_MENSAJES.add(new MensajeSalida(cargaProcesar.IdCargaMasiva,MensajeSalida.MENSAJE_ERROR,codMsg,msg));
				cargaProcesar.errores++;
			}else if (CodRequisito.equals(MensajeSalida.MENSAJE_AUTORIZACION)) {
				P_OUT_MENSAJES.add(new MensajeSalida(cargaProcesar.IdCargaMasiva,MensajeSalida.MENSAJE_AUTORIZACION,codMsg,msg));
				cargaProcesar.autorizaciones++;						
			}
			else if (CodRequisito.equals(MensajeSalida.MENSAJE_ADVERTENCIA)) {
				P_OUT_MENSAJES.add(new MensajeSalida(cargaProcesar.IdCargaMasiva,MensajeSalida.MENSAJE_ADVERTENCIA,codMsg,msg));
				cargaProcesar.advertencias++;
			}
			else {
				cargaProcesar.ok++;
			}
		}else {
			cargaProcesar.ok++;
		}		
	}

	public void limpiarCargas(List<CargaProcesar_Proceso> cargas, List<CargaProcesar_Proceso> cargasConErrores) {
		for (CargaProcesar_Proceso carga : cargas) {
			if (carga.errores > 0) {
				cargasConErrores.add(carga);
			}
		}
		
		for (CargaProcesar_Proceso cargaConErrores : cargasConErrores) {
			if (cargas.contains(cargaConErrores))
				cargas.remove(cargaConErrores);
		}
	}
	
	public String tipoComprobanteAfectoImpuesto(List<TipoComprobanteFacturacion> tiposComprobantes,String idTipoComprobante,String parametroAfectoImpuesto) {
		String afectoImpuesto = "";
		for(TipoComprobanteFacturacion tipoComprobante : tiposComprobantes) {
			if(tipoComprobante.IdTipoComprobante.equalsIgnoreCase(idTipoComprobante))
				afectoImpuesto = tipoComprobante.AfectoImpuesto;
		}
		if ("".equalsIgnoreCase(afectoImpuesto))
			afectoImpuesto = parametroAfectoImpuesto;
		
		return afectoImpuesto;
		
	}

	public static void main(String[] args) throws Exception {
		Connection con =  PlataformaUtils.getPropertiesConnection("desa02","c:/optimus-corredores/connections.properties");
		System.out.println("Inicio ProcesarCargaMasiva");
		try {
			//String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":110,\"p_IdModulo\":\"FAC\",\"p_UsaReglasNegocio\":\"S\",\"p_CargaMasivaProcesar\":[{\"IdCargaMasiva\":107,\"IdTipoCargaMasiva\":2,\"CodigoArchivo\":\"CARGA_NOTA_DE_CREDITO\",\"IdEmpleadoDigitador\":7293,\"IdBolsa\":\"BCS\",\"IdBoveda\":\"DCV\"}],\"p_FechaProceso\":\"2018-07-31\"}";
			String jsonNotaCredito = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"FAC\",\"p_UsaReglasNegocio\":\"S\",\"p_CargaMasivaProcesar\":[{\"IdCargaMasiva\":86,\"IdTipoCargaMasiva\":2,\"CodigoArchivo\":\"CARGA_NOTA_DE_CREDITO\",\"IdEmpleadoDigitador\":309,\"IdBolsa\":\"BVL\",\"IdBoveda\":\"CAV\"}],\"p_FechaProceso\":\"2019-03-18\"}";
			String jsonOtrosConceptos = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"FAC\",\"p_UsaReglasNegocio\":\"S\",\"p_CargaMasivaProcesar\":[{\"IdCargaMasiva\":92,\"IdTipoCargaMasiva\":1,\"CodigoArchivo\":\"CARGA_MOVIMIENTOS_OTROS_CONCEPTOS\",\"IdEmpleadoDigitador\":168,\"IdBolsa\":\"BVL\",\"IdBoveda\":\"CAV\"}],\"p_FechaProceso\":\"2019-03-18\"}";
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":97,\"p_IdModulo\":\"FAC\",\"p_UsaReglasNegocio\":\"S\",\"p_CargaMasivaProcesar\":[{\"IdCargaMasiva\":372,\"IdTipoCargaMasiva\":1,\"CodigoArchivo\":\"CARGA_MOVIMIENTOS_OTROS_CONCEPTOS\",\"IdEmpleadoDigitador\":240,\"IdBolsa\":\"BCS\",\"IdBoveda\":\"DCV\"}],\"p_FechaProceso\":\"2024-07-15\"}";
			String jsonDesa01 = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":0,\"p_IdModulo\":\"FAC\",\"p_UsaReglasNegocio\":\"S\",\"p_CargaMasivaProcesar\":[{\"IdCargaMasiva\":1317,\"IdTipoCargaMasiva\":1,\"CodigoArchivo\":\"CARGA_MOVIMIENTOS_OTROS_CONCEPTOS\",\"IdEmpleadoDigitador\":795,\"IdBolsa\":\"BCS\",\"IdBoveda\":\"BCS\"}],\"p_FechaProceso\":\"2024-06-05\"}";
			JsonParser parser = new JsonParser();
			JsonObject jsonObject = (JsonObject) parser.parse(json);
			ProcesarCargaMasiva procesarCargaMasiva = new ProcesarCargaMasiva();
			JsonObject salida = procesarCargaMasiva.call(con, jsonObject);
			System.out.println(salida.toString());
			con.rollback();
			//con.commit();
		} catch (Exception ex) {
			ex.printStackTrace();
			con.rollback();
		}
		System.out.println("Fin ProcesarCargaMasiva");
	}
}
