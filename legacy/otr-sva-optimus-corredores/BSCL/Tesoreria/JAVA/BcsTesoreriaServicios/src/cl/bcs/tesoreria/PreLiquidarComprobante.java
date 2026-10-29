package cl.bcs.tesoreria;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.cuentaInversion.CrearCargoAbono.ObjCargoAbonoAGenerar;
import cl.bcs.plataforma.pla.SaldoFlujoCaja;
import cl.bcs.plataforma.pla.SaldoFlujoCaja.ObjFLUJOCAJA;
import cl.bcs.plataforma.pla.SaldoFlujoCaja.ObjPORTAFOLIOACONSULTAR;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.tesoreria.ban.ActualizarSaldosCuentaBanco;
import cl.bcs.tesoreria.ban.ActualizarSaldosCuentaBanco.ActualizarSaldosCuentaBanco_Entrada;
import cl.bcs.tesoreria.ban.ActualizarSaldosCuentaBanco.ActualizarSaldosCuentaBanco_Salida;
import cl.bcs.tesoreria.ban.ActualizarSaldosCuentaBanco.CuentaBanco_Entrada;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class PreLiquidarComprobante implements JavaCall // En SQL se llama LiquidarComprobantes
{

	// Log
	private static final Logger logger = LogManager.getLogger(PreLiquidarComprobante.class);

	// Queries
	private final String SQL_INSERT_COMPROBANTES_A_LIQUIDAR = loadResourceToString(
			"cl/bcs/tesoreria/sql/LiquidarComprobante_ComprobantesALiquidar.sql");
	private final String SQL_INSERT_COMPROBANTE = loadResourceToString(
			"cl/bcs/tesoreria/sql/LiquidarComprobante_InsertComprobante.sql");
	private final String SQL_INSERT_COMPROBANTE_DETALLE = loadResourceToString(
			"cl/bcs/tesoreria/sql/LiquidarComprobante_InsertComprobanteDetalle.sql");
	private final String SQL_INSERT_COMPROBANTE_FORMA_DE_PAGO = loadResourceToString(
			"cl/bcs/tesoreria/sql/LiquidarComprobante_InsertComprobanteFormaDepago.sql");

	private final String SQL_LIQUIDAR_FORMAS_DE_PAGO = loadResourceToString(
			"cl/bcs/tesoreria/sql/LiquidarComprobante_LiquidarFormasDepago.sql");
	private final String SQL_LIQUIDAR_COMPROBANTE = loadResourceToString(
			"cl/bcs/tesoreria/sql/LiquidarComprobante_LiquidarComprobante.sql");

	private final String SQL_INSERT_COMPROBANTES_MUEVEN_CTA_INVERSION = loadResourceToString(
			"cl/bcs/tesoreria/sql/LiquidarComprobante_InsertComprobantesMuevenCuentaInversion.sql");
	private final String SQL_SELECT_CARGO_ABONO_A_GENERAR = loadResourceToString(
			"cl/bcs/tesoreria/sql/LiquidarComprobante_SelectCargoAbonoAGenerar.sql");
	private final String SQL_DELETE_COMPROBANTE = loadResourceToString(
			"cl/bcs/tesoreria/sql/LiquidarComprobante_DeleteComprobante.sql");
	private final String SQL_DELETE_COMPROBANTE_DETALLE = loadResourceToString(
			"cl/bcs/tesoreria/sql/LiquidarComprobante_DeleteComprobanteDetalle.sql");
	private final String SQL_DELETE_COMPROBANTE_FORMA_DE_PAGO = loadResourceToString(
			"cl/bcs/tesoreria/sql/LiquidarComprobante_DeleteComprobanteFormaDePago.sql");
	private final String SQL_DELETE_COMPROBANTES_A_LIQUIDAR = loadResourceToString(
			"cl/bcs/tesoreria/sql/LiquidarComprobante_DeleteComprobantesALiquidar.sql");
	private final String SQL_DELETE_FORMASDEPAGO_A_LIQUIDAR = loadResourceToString(
			"cl/bcs/tesoreria/sql/LiquidarComprobante_DeleteFormasDepagoALiquidar.sql");
	private final String SQL_SELECT_DATOS_PORTAFOLIO = loadResourceToString(
			"cl/bcs/tesoreria/sql/LiquidarComprobante_SelectDatosPortafolio.sql");
	private final String SQL_SELECT_CONCEPTO_TESORERIA = loadResourceToString(
			"cl/bcs/tesoreria/sql/LiquidarComprobante_SelectConceptoTesoreria.sql");
	private final String SQL_SELECT_CUENTA_BANCO_A_ACTUALIZAR   = loadResourceToString(
			"cl/bcs/tesoreria/sql/LiquidarComprobante_SelectCuentaBancoAActualizar.sql");

	// Para revisar contenido de tabla global que mueve cuenta inversion

	// -------------------------------------------------------------------------------------------
	// CLASES DE ENTRADA
	// -------------------------------------------------------------------------------------------

	public static class LiquidarComprobante_Entrada {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_SimularOperacion = "N";
		public String p_UsaReglasNegocio;
		public String p_FechaMovimiento; // Corresponde a Fecha Sistema

		// En SQL se llama "p_TablaComprobante"
		public List<ComprobanteLiquidar_Entrada> p_ComprobanteLiquidar; // Comprobantes a Liquidar

		// En SQL se llama "p_TablaComprobanteFormaDePago"
		public List<FormaDePagoLiquidar_Entrada> p_FormaDePagoLiquidar; // Formas de Pago a Liquidar > Si no especifica
																		// la FP a liquidar, se asume que se deben
																		// liquidar todas las FP de ese comprobante

		public LiquidarComprobante_Entrada() {
			p_SimularOperacion = "N";
			p_ComprobanteLiquidar = new ArrayList<ComprobanteLiquidar_Entrada>();
			p_FormaDePagoLiquidar = new ArrayList<FormaDePagoLiquidar_Entrada>();
		}

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	// Comprobantes a Liquidar
	public static class ComprobanteLiquidar_Entrada {
		public Long IdComprobante;
		public String IdTipoMovimiento; // MOVING / MOVEGR
		public String AfectaCuentaInversion; // CLI: Cliente / COR: Corredora / VACIO: Ambos
	}

	// Formas de Pago a Liquidar - Si no especifica la FP a liquidar para un
	// comprobante, se asume que se deben liquidar todas las FP de ese comprobante
	public static class FormaDePagoLiquidar_Entrada {
		public Long IdComprobante;
		public Long IdComprobanteFormaDePago;
	}

	// -------------------------------------------------------------------------------------------
	// CLASES DE SALIDA
	// -------------------------------------------------------------------------------------------

	public static class LiquidarComprobante_Salida {
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR;
		public List<ResultadoProceso_Salida> P_OUT_RESULTADOPROCESO;

		public LiquidarComprobante_Salida() {
			P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			P_OUT_RESULTADOPROCESO = new ArrayList<ResultadoProceso_Salida>();
		};

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class ResultadoProceso_Salida {
		public String Campo; // VARCHAR(40) -- Ej: "IdComprobante"
		public Long Id; // BIGINT -- IdComprobante liquidado Ej: 9999
		public String Servicio; // VARCHAR(250) -- Ej: "BSCL.Tesoreria.BD.Servicios::LiquidarComprobantes"
		public String Resultado; // VARCHAR(40) -- Ej: "OK"

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	// -------------------------------------------------------------------------------------------
	// CLASES UTILITARIAS DE PROCESAMIENTO
	// -------------------------------------------------------------------------------------------

	public static class Comprobante {
		public Long IdInterno;
		public Long IdComprobanteInformado;
		public String IdTipoMovimientoInformado;
		public Long IdComprobante;
		public String IdTipoMovimiento;
		public BigDecimal MontoComprobante;
		public Long CantidadFormaDePago;
		public BigDecimal MontoTotalFormaDePago;
		public String IdEstadoComprobante;
		public String AfectaCuentaInversion;
		public Long IdPortafolioTitularPago;
		public String IdConcepto;
		public String FechaMovimiento; // Fecha de sistema en que se liquida
		public Long IdPersona;
		public String NombrePersona;
		public String TitularPago;
		public String IdTipoPersonaTitularPago;
		public Long IdPersonaTitularPago;
		public String FechaLiquidacion;
		public Long IdCaja;

		// Para validaciones
		public long errores = 0;
		public long advertencias = 0;
		public long autorizaciones = 0;
		public long ok = 0;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class ComprobanteDetalle {
		public Long IdInterno;
		public Long IdComprobanteInformado;
		public Long IdComprobanteDetalle;
		public String IdTipoMovimiento;
		public String IdTipoComprobanteOrigen;
		public Long SecuenciaRelacionada;
		public int PendienteDeLiquidar;
		public String IdConcepto;
		public BigDecimal Monto;
		public long errores = 0;
		public long advertencias = 0;
		public long autorizaciones = 0;
		public long ok = 0;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class ComprobanteFormaDePago {
		public Long IdInterno;
		public Long IdComprobanteInformado;
		public Long IdComprobanteFormaDePagoInformado;
		public Long IdComprobanteFormaDePago;
		public Long IdCuentaBancariaCorredora;
		public String IdEstadoFormaDePago;
		public String IdEstadoComprobante;
		public String TipoMedioDePago;
		public String LiquidacionParcialEnviaACTA; // es un parametro de tesoreria
		public String EstadosPosibleTipoMovimientoMedioDePago;
		public String AfectaSaldoEnEstado;
		public String IdMoneda;
		public BigDecimal Monto;
		public Long IdDeposito;
		public String IdEstadoDepositoDetalle;
		public java.sql.Date FechaLiquidacion;
		public Long DiasRetencion;
		public String IdMedioDePago;
		public Long IdCaja;
		public String FechaMovimiento; // Fecha de sistema en que se liquida
		public Long IdUsuario;
		public String NumeroCuentaBancariaCorredora;
		public String IdTipoMovimiento;
		public String IdModalidadDePago;
		public Long IdCuentaBancariaSocio;
		public String NumeroCuentaBancariaSocio;
		public Long IdBancoSocio;
		public String TipoComprobante;
		public String ValidarFormaDePagoSinCertificar = "S"; // Para recudacion APV no se valida
		public String ValidarFormaDePagoSinDepositar = "S"; // Para recudacion APV no se valida
		public long errores = 0;
		public long advertencias = 0;
		public long autorizaciones = 0;
		public long ok = 0;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class ParametrosConfiguracion {

		public String LiquidacionParcialEnviaACTA;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class FlujoCajaElem {
		String IdMoneda;
		String Fecha;
		Long IdPortafolio;

		public FlujoCajaElem(String moneda, String fecha, Long idPortafolio) {
			this.IdMoneda = moneda;
			this.Fecha = fecha;
			this.IdPortafolio = idPortafolio;
		}

		@Override
		public boolean equals(Object o) {
			FlujoCajaElem e = (FlujoCajaElem) o;
			return this.IdMoneda.equalsIgnoreCase(e.IdMoneda) && this.Fecha.equalsIgnoreCase(e.Fecha)
					&& this.IdPortafolio.equals(e.IdPortafolio);
		}

		@Override
		public int hashCode() {
			return (this.IdMoneda + this.Fecha + this.IdPortafolio).hashCode();
		}
	}

	public static class ConceptoTesoreria extends BcsPojo {
		public String IdConcepto;
		public String IndicadorNoValidaSaldoCaja;

		public ConceptoTesoreria() {
			IndicadorNoValidaSaldoCaja = "N";
		}
	}

	private void ProcesaRequisitoCargoAbono(Connection con, Long idEmpresa, boolean esError, String codMsg,
			String CodRequisito, ObjCargoAbonoAGenerar cargoAbono, DatoPortafolio datosPortafolio,
			List<MensajeSalida> P_OUT_MENSAJES) throws Exception {
		if (esError) {
			String msg = Diccionario.getMensaje(con, idEmpresa, codMsg)
					.add("IdComprobante", cargoAbono.NumeroCargoAbono).add("Monto", cargoAbono.Monto)
					.add("IdMoneda", cargoAbono.IdMoneda).add("DocumentoIdentidad", datosPortafolio.DocumentoIdentidad)
					.add("NombreCompleto",
							"IdPortafolio " + datosPortafolio.IdPortafolio + " " + datosPortafolio.NombreCompleto)
					.msg();

			if (CodRequisito.equals(MensajeSalida.MENSAJE_ERROR)) {
				P_OUT_MENSAJES
						.add(new MensajeSalida(cargoAbono.NumeroCargoAbono, MensajeSalida.MENSAJE_ERROR, codMsg, msg));
			}
		}
	}

	public static class DatoPortafolio {
		public Long IdPortafolio;
		public String NombreCompleto;
		public String DocumentoIdentidad;
	}

	private Boolean IndicadorNoValidaSaldoCaja(Connection conn, Long idEmpresa,
			List<ObjCargoAbonoAGenerar> cargosAbonos) throws SQLException {
		logger.debug("INICIO::LiquidarComprobante.IgnorarSaldosNegativos");
		long inicio = System.currentTimeMillis();
		Boolean Respuesta = true;
		HashMap<String, Object> params = new HashMap<>(2);
		params.put("ConceptoIds", cargosAbonos.stream().map(u -> u.IdConcepto).collect(Collectors.toList()));
		Map<String, String> indicadorNoValidaSaldoCajaPorIdConcepto = new SQL()
				.query(conn, SQL_SELECT_CONCEPTO_TESORERIA, params, ConceptoTesoreria.class).stream()
				.filter(u -> u.IndicadorNoValidaSaldoCaja.equals("S"))
				.collect(Collectors.toMap(u -> u.IdConcepto, u -> u.IndicadorNoValidaSaldoCaja));
		if (indicadorNoValidaSaldoCajaPorIdConcepto.size() > 0) {
			Respuesta = true;
		} else {
			Respuesta = false;
		}
		long fin = System.currentTimeMillis();
		logger.debug("FIN::LiquidarComprobante.IgnorarSaldosNegativos", fin - inicio);
		return Respuesta;
	}

	// -------------------------------------------------------------------------------------------
	// LOGICA DEL SERVICIO
	// -------------------------------------------------------------------------------------------

	public LiquidarComprobante_Salida logica(Connection con, LiquidarComprobante_Entrada in) throws Exception {

		logger.info("== INICIO LIQUIDAR COMPROBANTE ==");
		SQL sql = new SQL();
		LiquidarComprobante_Salida out = new LiquidarComprobante_Salida();
		HashMap<String, Object> param = new HashMap<>();
		List<Comprobante> comprobantesAProcesar = in.p_ComprobanteLiquidar.parallelStream().map(comprobanteEntrada -> {
			Comprobante comprobante = new Comprobante();
			comprobante.IdComprobante = comprobanteEntrada.IdComprobante;
			return comprobante;
		}).collect(Collectors.toList());

		// Obtiene parametro "LIQUIDACION PARCIAL DE COMPROBANTE ENVIA A CUENTA
		// INVERSION"
		String liquidacionParcialEnviaACTA = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa,
				"TESORERIA", "LIQUIDACIONPARCIALENVIAACTA");
		try {
			// --------------------------------------------------------------------------------------------------
			// OBTIENE DATOS DE COMPROBANTES A LIQUIDAR
			// Guarda en tabla global datos de entrada de comprobantes a liquidar
			sql.batch(con, SQL_INSERT_COMPROBANTES_A_LIQUIDAR, in.p_ComprobanteLiquidar,
					ComprobanteLiquidar_Entrada.class);
			// Obtiene y Guarda en tabla global mas datos de los comprobantes a liquidar
			List<Comprobante> lstComprobante = new ArrayList<Comprobante>();
			Comprobante objComprobante = new Comprobante();
			objComprobante.FechaMovimiento = in.p_FechaMovimiento;
			lstComprobante.add(objComprobante);
			sql.batch(con, SQL_INSERT_COMPROBANTE, lstComprobante, Comprobante.class);

			// --------------------------------------------------------------------------------------------------
			// OBTIENE DATOS DE COMPROBANTES DETALLE
			// Obtiene y Guarda en tabla global datos de los comprobantes detalles a
			// liquidar
			List<ComprobanteDetalle> lstComprobanteDetalle = new ArrayList<ComprobanteDetalle>();
			ComprobanteDetalle objComprobanteDetalle = new ComprobanteDetalle();
			objComprobanteDetalle.IdInterno = 1L; // solo para que entre un parametro sino no hace insert
			lstComprobanteDetalle.add(objComprobanteDetalle);
			sql.batch(con, SQL_INSERT_COMPROBANTE_DETALLE, lstComprobanteDetalle, ComprobanteDetalle.class);

			// Obtiene y Guarda en tabla global mas datos de comprobante formas de pago
			List<ComprobanteFormaDePago> lstComprobanteFormaDePago = new ArrayList<ComprobanteFormaDePago>();
			ComprobanteFormaDePago objComprobanteFormaDePago = new ComprobanteFormaDePago();
			objComprobanteFormaDePago.FechaMovimiento = in.p_FechaMovimiento;
			lstComprobanteFormaDePago.add(objComprobanteFormaDePago);
			sql.batch(con, SQL_INSERT_COMPROBANTE_FORMA_DE_PAGO, lstComprobanteFormaDePago,
					ComprobanteFormaDePago.class);

			// --------------------------------------------------------------------------------------------------
			// PROCESO DE LIQUIDACION

			// Liquidar las formas de pago de los comprobantes
			List<ComprobanteFormaDePago> lstLiquidarFormaDePago = new ArrayList<ComprobanteFormaDePago>();
			ComprobanteFormaDePago objLiquidarFormaDePago = new ComprobanteFormaDePago();
			objLiquidarFormaDePago.FechaMovimiento = in.p_FechaMovimiento;
			objLiquidarFormaDePago.IdUsuario = in.p_IdUsuario;
			lstLiquidarFormaDePago.add(objLiquidarFormaDePago);
			sql.batch(con, SQL_LIQUIDAR_FORMAS_DE_PAGO, lstLiquidarFormaDePago, ComprobanteFormaDePago.class);

			// Liquidar los comprobantes
			List<Comprobante> lstLiquidarComprobante = new ArrayList<Comprobante>();
			Comprobante objLiquidarComprobante = new Comprobante();
			objLiquidarComprobante.FechaMovimiento = in.p_FechaMovimiento;
			lstLiquidarComprobante.add(objLiquidarComprobante);
			sql.batch(con, SQL_LIQUIDAR_COMPROBANTE, lstLiquidarComprobante, Comprobante.class);
			
			// --------------------------------------------------------------------------------------------------
			// ACTUALIZA SALDOS DE CUENTAS BANCARIAS		
			
			ActualizarSaldosCuentaBanco servicioActualizarSaldosCuentaBanco = new ActualizarSaldosCuentaBanco();
			ActualizarSaldosCuentaBanco_Entrada entradaActualizarSaldosCuentaBanco = new ActualizarSaldosCuentaBanco_Entrada();		
			

			//Obtien datos de las cuentas bancarias de la corredora a actualizar el saldo
			param.clear();
			logger.debug("Actualizar bancos");
			List<CuentaBanco_Entrada> lstCuentaBanco = sql.query(con, SQL_SELECT_CUENTA_BANCO_A_ACTUALIZAR, param, CuentaBanco_Entrada.class);
			
			if (lstCuentaBanco.size() > 0)
			{
				entradaActualizarSaldosCuentaBanco.p_FechaMovimiento = in.p_FechaMovimiento;
				entradaActualizarSaldosCuentaBanco.p_CuentaBanco = lstCuentaBanco;
			
				ActualizarSaldosCuentaBanco_Salida salidaActualizarSaldosCuentaBanco = servicioActualizarSaldosCuentaBanco.logica(con, entradaActualizarSaldosCuentaBanco);
				
				out.P_OUT_MENSAJES.addAll(salidaActualizarSaldosCuentaBanco.P_OUT_MENSAJES);			
				out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
				
				if (out.P_OUT_ERRORESCONTADOR.compareTo(0L) == 1) {
					//simular
					return out;
				}
				
			}

			// --------------------------------------------------------------------------------------------------
			// GENERA CARGO/ABONO EN CUENTA INVERSION (para cliente, corredora segun
			// parametro de entrada)

			// Obtiene y guarda en tabla global datos de comprobantes que moveran cuenta
			// inversion
			List<ParametrosConfiguracion> lstParametrosConfiguracion = new ArrayList<ParametrosConfiguracion>();
			ParametrosConfiguracion objParametrosConfiguracion = new ParametrosConfiguracion();
			objParametrosConfiguracion.LiquidacionParcialEnviaACTA = liquidacionParcialEnviaACTA;
			lstParametrosConfiguracion.add(objParametrosConfiguracion);
			sql.batch(con, SQL_INSERT_COMPROBANTES_MUEVEN_CTA_INVERSION, lstParametrosConfiguracion,
					ParametrosConfiguracion.class);

			// Obtiene y prepara los datos para el envio a cuenta inversion
			param.clear();
			param.put("IdEmpresa", in.p_IdEmpresa);
			param.put("FechaMovimiento", in.p_FechaMovimiento);
			List<ObjCargoAbonoAGenerar> lstCargoAbonoAGenerar = sql.query(con, SQL_SELECT_CARGO_ABONO_A_GENERAR, param,
					ObjCargoAbonoAGenerar.class);

			Boolean NoValidaSaldos = IndicadorNoValidaSaldoCaja(con, in.p_IdEmpresa, lstCargoAbonoAGenerar);
			if (NoValidaSaldos) {
				if (lstCargoAbonoAGenerar.size() > 0) {
					SaldoFlujoCaja.ServiceEntrada entradaFlujoCaja = new SaldoFlujoCaja.ServiceEntrada();
					java.sql.Date fechaLiquidacionMinima = Fecha
							.toDate(lstCargoAbonoAGenerar.get(0).FechaCondicionDeLiquidacion);
					java.sql.Date fechaLiquidacionMaxima = Fecha
							.toDate(lstCargoAbonoAGenerar.get(0).FechaCondicionDeLiquidacion);

					HashSet<SaldoFlujoCaja.ObjPORTAFOLIOACONSULTAR> portafoliosFlujoCaja = new HashSet<>();
					HashMap<FlujoCajaElem, BigDecimal> totalEgresosPortafolio = new HashMap<>();
					HashMap<FlujoCajaElem, BigDecimal> totalFlujoCajaPortafolio = new HashMap<>();

					for (ObjCargoAbonoAGenerar cargoAbono : lstCargoAbonoAGenerar) {
						if ("MOVCAR".equals(cargoAbono.IdTipoMovimiento)) {
							java.sql.Date fechaLiquidacion = Fecha.toDate(cargoAbono.FechaCondicionDeLiquidacion);
							if (fechaLiquidacion.before(fechaLiquidacionMinima)) {
								fechaLiquidacionMinima = fechaLiquidacion;
							}
							if (fechaLiquidacion.after(fechaLiquidacionMaxima)) {
								fechaLiquidacionMaxima = fechaLiquidacion;
							}
							FlujoCajaElem llave = new FlujoCajaElem(cargoAbono.IdMoneda,
									cargoAbono.FechaCondicionDeLiquidacion, cargoAbono.IdPortafolio);
							if (!totalEgresosPortafolio.containsKey(llave)) {
								totalEgresosPortafolio.put(llave, BigDecimal.ZERO);
							}
							BigDecimal totalEgresos = totalEgresosPortafolio.get(llave);
							totalEgresos = totalEgresos.add(cargoAbono.Monto);
							totalEgresosPortafolio.put(llave, totalEgresos);
							SaldoFlujoCaja.ObjPORTAFOLIOACONSULTAR port = new SaldoFlujoCaja.ObjPORTAFOLIOACONSULTAR();
							port.IdMoneda = cargoAbono.IdMoneda;
							port.IdPortafolio = cargoAbono.IdPortafolio;
							portafoliosFlujoCaja.add(port);
						}
					}

					if (portafoliosFlujoCaja.size() > 0) {

						entradaFlujoCaja.P_IDEMPRESA = in.p_IdEmpresa;
						entradaFlujoCaja.P_FECHADESDE = Fecha.toStringDate(fechaLiquidacionMinima);
						entradaFlujoCaja.P_FECHAHASTA = Fecha.toStringDate(fechaLiquidacionMaxima);
						entradaFlujoCaja.P_USO = "LIQUIDACION_TESORERIA";
						entradaFlujoCaja.P_PORTAFOLIOACONSULTAR = new ArrayList<ObjPORTAFOLIOACONSULTAR>(
								portafoliosFlujoCaja);
						SaldoFlujoCaja saldoFlujoCajaService = new SaldoFlujoCaja();
						SaldoFlujoCaja.ServiceSalida salidaFlujoCaja = saldoFlujoCajaService.logica(con,
								entradaFlujoCaja);

						List<ObjFLUJOCAJA> resultadoFlujoCaja = salidaFlujoCaja.P_OUT_FLUJOCAJA;
						for (ObjFLUJOCAJA flujoCaja : resultadoFlujoCaja) {
							FlujoCajaElem llave = new FlujoCajaElem(flujoCaja.IdMoneda, flujoCaja.FechaSaldo,
									flujoCaja.IdPortafolio);
							if (!totalFlujoCajaPortafolio.containsKey(llave)) {
								totalFlujoCajaPortafolio.put(llave, BigDecimal.ZERO);
							}
							BigDecimal totalFlujoCaja = totalFlujoCajaPortafolio.get(llave);
							totalFlujoCaja = totalFlujoCaja.add(flujoCaja.Saldo);
							totalFlujoCajaPortafolio.put(llave, totalFlujoCaja);
						}

						List<Long> idsPortafolio = lstCargoAbonoAGenerar.stream()
								.map(cargoAbono -> cargoAbono.IdPortafolio).collect(Collectors.toList());
						HashMap<String, Object> parametros = new HashMap<String, Object>(2);
						parametros.put("IdPortafolio", idsPortafolio);
						List<DatoPortafolio> datosPortafolios = sql.query(con, SQL_SELECT_DATOS_PORTAFOLIO, parametros,
								DatoPortafolio.class);
						Map<Long, DatoPortafolio> datosPortafoliosPorIdPortafolio = datosPortafolios.stream().collect(
								Collectors.toMap(portafolio -> portafolio.IdPortafolio, portafolio -> portafolio));

						for (ObjCargoAbonoAGenerar cargoAbono : lstCargoAbonoAGenerar) {
							if ("MOVCAR".equals(cargoAbono.IdTipoMovimiento)) {
								FlujoCajaElem llave = new FlujoCajaElem(cargoAbono.IdMoneda,
										cargoAbono.FechaCondicionDeLiquidacion, cargoAbono.IdPortafolio);
								BigDecimal totalEgresos = totalEgresosPortafolio.get(llave);
								BigDecimal totalFlujoCaja = totalFlujoCajaPortafolio.get(llave);

								if (totalEgresos.compareTo(totalFlujoCaja) == 1) {
									DatoPortafolio datoPortafolio = datosPortafoliosPorIdPortafolio
											.get(cargoAbono.IdPortafolio);
									ProcesaRequisitoCargoAbono(con, in.p_IdEmpresa, true, "SALDO_CAJA_INSUFICIENTE",
											MensajeSalida.MENSAJE_ERROR, cargoAbono, datoPortafolio,
											out.P_OUT_MENSAJES);
								}
							}
						}
						out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
						if (out.P_OUT_ERRORESCONTADOR > 0) {
							return out;
						}
					}
					out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
					if (out.P_OUT_ERRORESCONTADOR.compareTo(0L) == 1) {
						return out;
					}
				}
			}
		} catch (Exception e) {
			throw e;
		} finally {
			sql.batch(con, SQL_DELETE_COMPROBANTE, comprobantesAProcesar, Comprobante.class);
			sql.batch(con, SQL_DELETE_COMPROBANTE_DETALLE, comprobantesAProcesar, Comprobante.class);
			sql.batch(con, SQL_DELETE_COMPROBANTE_FORMA_DE_PAGO, comprobantesAProcesar, Comprobante.class);
			sql.batch(con, SQL_DELETE_COMPROBANTES_A_LIQUIDAR, comprobantesAProcesar, Comprobante.class);
			sql.batch(con, SQL_DELETE_FORMASDEPAGO_A_LIQUIDAR, comprobantesAProcesar, Comprobante.class);
		}

		// --------------------------------------------------------------------------------------------------
		// GENERAR EVENTO DE NOTIFICACION : TODO
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		logger.info("== TERMINO LIQUIDAR COMPROBANTE ==");
		con.rollback();
		return out;

	}

	// -------------------------------------------------------------------------------------------
	// OBJETO DE TRANSPORTE
	// -------------------------------------------------------------------------------------------
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = getGsonBuilder();

		LiquidarComprobante_Entrada in = gson.fromJson(jin.toString(), LiquidarComprobante_Entrada.class);
		LiquidarComprobante_Salida out = logica(con, in);
		// LiquidarComprobante_Salida out = new LiquidarComprobante_Salida();
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		// out.P_OUT_MENSAJES=(List<MensajeSalida>) in;
		if (in.p_SimularOperacion.equalsIgnoreCase("S")) {
			con.rollback();
		}
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	// -------------------------------------------------------------------------------------------
	// METODO PARA PRUEBAS LOCALES
	// -------------------------------------------------------------------------------------------

	public static void main(String[] args) throws Exception {
		System.out.println("== INICIO PRUEBA LOCAL ==");

		try (Connection con = PlataformaUtils.getPropertiesConnection("desa02","connections.properties")) {
			Gson gson = Utils.getGsonBuilder();
               
            String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":97,\"p_IdModulo\":\"TES\",\"p_UsaReglasNegocio\":\"S\",\"p_FechaMovimiento\":\"2024-09-13\",\"p_ComprobanteLiquidar\":[{\"IdComprobante\":504927,\"IdTipoMovimiento\":\"MOVING\",\"AfectaCuentaInversion\":\"CLI\"}],\"p_FormaDePagoLiquidar\":[]}";
               
			LiquidarComprobante_Entrada Objin = gson.fromJson(json, LiquidarComprobante_Entrada.class);
			PreLiquidarComprobante demo = new PreLiquidarComprobante();

			System.out.println("ENTRADA:");
			System.out.println(Objin.toString());

			try {
				LiquidarComprobante_Salida ObjOut = demo.logica(con, Objin);
				System.out.println(ObjOut.toString());
				con.rollback();
				//con.commit();
			} catch (Exception er) {
				er.printStackTrace();
				// con.commit();
			}
			con.close();
		}
		System.out.println("== FIN PRUEBA LOCAL ==");
	}

}
