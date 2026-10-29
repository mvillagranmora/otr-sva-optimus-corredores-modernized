package cl.bcs.custodia;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.custodia.IngresoRetiroTitulosJava;
import cl.bcs.custodia.utils.CacheValorizacion;
import cl.bcs.custodia.utils.IngresoCustodia;
import cl.bcs.custodia.utils.LogCustodia;
import cl.bcs.custodia.utils.RetiroCustodia;
import cl.bcs.plataforma.cre.ConsultarEstadoModulo;
import cl.bcs.plataforma.cre.ConsultarEstadoModulo.InConsultarEstadoModulo;
import cl.bcs.plataforma.cre.ConsultarEstadoModulo.Modulo;
import cl.bcs.plataforma.cre.ConsultarEstadoModulo.OutConsultarEstadoModulo;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.MonedaFormato;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class UnificarInstrumentosCLP implements JavaCall {
	private static final Logger logger = LogManager.getLogger(UnificarInstrumentosCLP.class);

	private final String SQL_SELECT_MESAS = loadResourceToString("cl/bcs/custodia/sql/IngresoCustodia_SEL_Mesas.sql");
	private final String SQL_SELECT_DOLAR_HISTORICO = loadResourceToString("cl/bcs/custodia/sql/UnificarInstrumentosCLP_SelectDolar.sql");
	private final String SQL_SELECT_HOMOLOGO_GLOBAL = loadResourceToString("cl/bcs/custodia/sql/UnificarInstrumentosCLP_SelectHomologo.sql");
	private final String SQL_SELECT_CARTERA = loadResourceToString("cl/bcs/custodia/sql/UnificarInstrumentosCLP_SelectCartera.sql");
	private final String SQL_UPDATE_INSTRUMENTO = loadResourceToString("cl/bcs/custodia/sql/UnificarInstrumentosCLP_UpdateParametros.sql");
	private final String SQL_UPDATE_CARTERA_FECHAS = loadResourceToString("cl/bcs/custodia/sql/UnificarInstrumentosCLP_UpdateCarteraFechas.sql");
	private static final String MODULO_ESTADO_CERRADO = "CER";

	public static class UnificarInstrumentosEntrada {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public List<InstrumentoCLP> P_INSTRUMENTOS;
		public List<Long> p_MovimientosEnTransito;
		public String p_CanalIngreso = "OPT";
		public boolean p_ExisteMovEnTransito;

		public UnificarInstrumentosEntrada() {
			P_INSTRUMENTOS = new ArrayList<InstrumentoCLP>();
			p_MovimientosEnTransito = new ArrayList<Long>();
		}

		public static class InstrumentoCLP {
			public Long IdGenerarMovimientoCustodia = 0L;
			public String FechaOperacion;
			public String FechaOperacionOriginal;
			public String FechaVencimiento;
			public String FechaLiquidacion;
			public String IdCondicionLiquidacion;
			public String IdConcepto;
			public String IdBolsa;
			public String IdTipoOperacion;
			public String IdTipoProducto = "";
			public Long IdCliente;
			public Long IdPortafolio;
			public Long IdClienteDestino;
			public Long IdPortafolioDestino;
			public String IdBoveda;
			public String CuentaBoveda;
			public Long IdCuentaCustodioPortafolio;
			public String IdBovedaRelacionada;
			public String CuentaBovedaRelacionada;
			public Long IdCuentaCustodioPortafolioRelacionado;
			public String esTraspasoCliente = "N";
			public String esTraspasoPortafolio = "N";
			public String esTraspasoBoveda = "N";
			public String DebeCertificar;
			public String EsGarantia = "N";
			public Long PlazoEnDias = 0L;
			public Long IdCartera = 0L;
			public Long FolioCartera = 0L;
			public String FolioExternoReferencia = "";
			public String Glosa = "";
			public String IndicadorNominal = "N";
			public Long IdAgente;
			public Long IdSucursal;
			public String IdTipoCanalIngreso;
			public String IdClasificacionTipoInstrumento;
			public String IdTipoInstrumento;
			public Long IdInstrumento;
			public String NemoOrigen;
			public String NemoDestino;
			public String IdEmisor;
			public String IdMonedaInstrumento = "";
			public String IdMonedaEmision = "";
			public String IdTipoReajuste = "";
			public String IdMonedaAdicional = null;
			public String FechaEmisionInstrumento = "0001-01-01";
			public String FechaVencimientoInstrumento = "0001-01-01";
			public String Fungible = "N";
			public String NemoSVS = "";
			public BigDecimal Precio = BigDecimal.ZERO;
			public BigDecimal Cantidad = BigDecimal.ZERO;
			public BigDecimal ValorRescate = BigDecimal.ZERO;
			public BigDecimal CorteMinimo = BigDecimal.ONE;
			public BigDecimal Monto = BigDecimal.ZERO;
			public BigDecimal MontoEnMonedaInstrumento = BigDecimal.ZERO;
			public BigDecimal Tasa = BigDecimal.ZERO;
			public BigDecimal TasaAgente = BigDecimal.ZERO;
			public String CodigoIFRS = "";
			public String EsCtaPropia = "N";
			public String IdRegimenFondoMutuo = "";
			public String Procesar = "S";
			public Long IdCorredora;
			public String EsInstrumentoCalificado = "N";

			public long errores = 0;
			public long advertencias = 0;
			public long autorizaciones = 0;
			public long ok = 0;
		}

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class UnificarInstrumentosSalida {
		public Long P_OUT_ERRORESCONTADOR;
		public Long P_OUT_TICKETATENCION;
		public List<ObjResultado> P_OUT_RESULTADO;
		public List<MensajeSalida> P_OUT_MENSAJES;

		public static class ObjResultado {
			public Long IdGenerarMovimientoCustodia;
			public String TablaOrigen;
			public Long IdCustodia;
			public Long FolioCustodia;
		}

		public UnificarInstrumentosSalida() {
			P_OUT_ERRORESCONTADOR = 0L;
			P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			P_OUT_RESULTADO = new ArrayList<ObjResultado>();
		}

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class objMesas {
		public Long IdMesa;
		public Long IdPortafolio;
	}

	public static class DolarHistorico extends BcsPojo {
		public BigDecimal ValorDolar;
	}

	public static class InstrumentoGlobal extends BcsPojo {
		public Long IdInstrumento;
		public String Nemotecnico;
		public String IdEmisor;
	}

	public static class CarteraOrigen extends BcsPojo {
		public Long FolioCartera;
		public Long IdCliente;
		public Long IdPortafolio;
		public String IdBoveda;
		public String CuentaBoveda;
		public java.sql.Date FechaOperacion;
		public java.sql.Date FechaLiquidacion;
		public BigDecimal Cantidad;
		public BigDecimal PrecioCompra;
		public BigDecimal MontoCompra;
		public BigDecimal MontoMercado;
		public String IdClasificacionTipoInstrumento;
		public String IdTipoInstrumento;
		public Long IdInstrumento;
		public String IdEmisor;
		public String IdMonedaEmision;
		public String IdMonedaInstrumento;
		public String IdTipoProducto;
		public String IdCondicionLiquidacion;
		public String IdTipoCanalIngreso;
	}

	public UnificarInstrumentosSalida logica(Connection con, UnificarInstrumentosEntrada in) throws Exception {
		logger.info("INICIO::UnificarInstrumentosCLP");
		SQL sql = new SQL();
		UnificarInstrumentosSalida out = new UnificarInstrumentosSalida();
		out.P_OUT_ERRORESCONTADOR = 0L;

		try {
			if (in.P_INSTRUMENTOS == null || in.P_INSTRUMENTOS.isEmpty()) {
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "FALTA_NEMOORIGEN", "La lista de instrumentos viene vacia en el JSON."));
				out.P_OUT_ERRORESCONTADOR++;
				return out;
			}

			logger.debug("ACTUALIZACION DE INSTRUMENTOS");
			String condicionLiquidacionDefault = "T0";

			for (UnificarInstrumentosEntrada.InstrumentoCLP instEntrada : in.P_INSTRUMENTOS) {
				if (instEntrada.NemoOrigen == null || instEntrada.NemoDestino == null) continue;

				InstrumentoGlobal infoGlobal = obtenerHomologoGlobal(con, instEntrada.NemoDestino, in.p_IdEmpresa);

				if (infoGlobal != null) {
					try (java.sql.PreparedStatement actualizacionNemos = con.prepareStatement(SQL_UPDATE_INSTRUMENTO)) {
						actualizacionNemos.setLong(1, infoGlobal.IdInstrumento);
						actualizacionNemos.setString(2, infoGlobal.IdEmisor);
						actualizacionNemos.setString(3, condicionLiquidacionDefault);

						actualizacionNemos.setString(4, instEntrada.NemoOrigen);
						actualizacionNemos.setLong(5, infoGlobal.IdInstrumento);
						actualizacionNemos.setString(6, infoGlobal.IdEmisor);
						actualizacionNemos.setString(7, condicionLiquidacionDefault);

						int filas = actualizacionNemos.executeUpdate();

						if (filas > 0) {
							logger.info("Instrumento actualizado correctamente: " + instEntrada.NemoOrigen);
							out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_INFORMACION, "UPDATE_OK", "Se actualizo correctamente el instrumento: " + instEntrada.NemoOrigen + " con su homologo " + instEntrada.NemoDestino));
						} else {
							logger.info("El instrumento " + instEntrada.NemoOrigen + " ya estaba actualizado, Se omite el Update");
							out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_INFORMACION, "UPDATE_OMITIDO", "El instrumento " + instEntrada.NemoOrigen + " ya se encuentra actualizado con los parámetros de su homólogo (" + instEntrada.NemoDestino + ")."));
						}
					}
				} else {
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_HOMOLOGO", "No se encontro instrumento global para: " + instEntrada.NemoDestino));
					out.P_OUT_ERRORESCONTADOR++;
					return out;
				}
			}

			logger.debug("BUSQUEDA DE CARTERA MASIVA");

			List<UnificarInstrumentosEntrada.InstrumentoCLP> nuevosInstrumentos = new ArrayList<>();

			for (UnificarInstrumentosEntrada.InstrumentoCLP instEntrada : in.P_INSTRUMENTOS) {
				String nemoOrigenMasivo = instEntrada.NemoOrigen;
				String nemoDestinoMasivo = instEntrada.NemoDestino;

				if (nemoOrigenMasivo == null || nemoOrigenMasivo.isEmpty() || nemoDestinoMasivo == null || nemoDestinoMasivo.isEmpty()) {
					continue;
				}

				HashMap<String, Object> paramsCartera = new HashMap<>();
				paramsCartera.put("NemoOrigen", nemoOrigenMasivo);
				List<CarteraOrigen> carterasBD = sql.query(con, SQL_SELECT_CARTERA, paramsCartera, CarteraOrigen.class);

				if (carterasBD == null || carterasBD.isEmpty()) {
					continue;
				}

				for (CarteraOrigen cart : carterasBD) {
					UnificarInstrumentosEntrada.InstrumentoCLP inst = new UnificarInstrumentosEntrada.InstrumentoCLP();

					inst.IdGenerarMovimientoCustodia = cart.FolioCartera;
					inst.IdCliente = cart.IdCliente;
					inst.IdPortafolio = cart.IdPortafolio;
					inst.IdBoveda = cart.IdBoveda;
					inst.CuentaBoveda = cart.CuentaBoveda != null ? cart.CuentaBoveda : "";
					inst.FechaOperacion = cart.FechaOperacion != null ? cart.FechaOperacion.toString() : "0001-01-01";
					inst.FechaOperacionOriginal = inst.FechaOperacion;
					inst.FechaLiquidacion = cart.FechaLiquidacion != null ? cart.FechaLiquidacion.toString() : "0001-01-01";
					inst.FechaVencimiento = "0001-01-01";
					inst.Cantidad = cart.Cantidad;
					inst.Precio = cart.PrecioCompra;
					inst.Monto = cart.MontoCompra;
					inst.MontoEnMonedaInstrumento = cart.MontoCompra;
					inst.NemoOrigen = nemoOrigenMasivo;
					inst.NemoDestino = nemoDestinoMasivo;
					inst.IdInstrumento = cart.IdInstrumento;
					inst.IdEmisor = cart.IdEmisor;
					inst.IdMonedaInstrumento = cart.IdMonedaInstrumento;
					inst.IdMonedaEmision = cart.IdMonedaEmision != null ? cart.IdMonedaEmision : "";
					inst.IdClasificacionTipoInstrumento = cart.IdClasificacionTipoInstrumento;
					inst.IdTipoInstrumento = cart.IdTipoInstrumento;
					inst.IdTipoProducto = cart.IdTipoProducto;
					inst.IdTipoOperacion = "RETTIT";
					inst.IdConcepto = "INGRAD";
					inst.IdCondicionLiquidacion = cart.IdCondicionLiquidacion;
					inst.IdTipoCanalIngreso = cart.IdTipoCanalIngreso;
					inst.esTraspasoCliente = "N";
					inst.esTraspasoPortafolio = "N";
					inst.esTraspasoBoveda = "N";
					inst.EsGarantia = "N";
					inst.Fungible = "N";
					inst.EsCtaPropia = "N";
					inst.EsInstrumentoCalificado = "N";
					inst.ValorRescate = cart.MontoMercado != null ? cart.MontoMercado : BigDecimal.ZERO;

					nuevosInstrumentos.add(inst);
				}
			}

			if (nuevosInstrumentos.isEmpty()) {
				return out;
			}

			in.P_INSTRUMENTOS = nuevosInstrumentos;
			List<UnificarInstrumentosEntrada.InstrumentoCLP> instrumentos = in.P_INSTRUMENTOS;

			logger.debug(" ADAPTADOR DEL CODIGO LEGACY");
			IngresoRetiroTitulosJava.IngresoRetiroTitulosEntrada inLegacy = new IngresoRetiroTitulosJava.IngresoRetiroTitulosEntrada();

			inLegacy.p_IdEmpresa = in.p_IdEmpresa;
			inLegacy.p_IdUsuario = in.p_IdUsuario;
			inLegacy.p_IdModulo = in.p_IdModulo;
			inLegacy.p_CanalIngreso = in.p_CanalIngreso;
			inLegacy.p_simularOperacion = "N";
			inLegacy.p_UsaReglasNegocio = "N";
			inLegacy.p_Certifica = "N";

			out.P_OUT_TICKETATENCION = LogCustodia.logIngresoRetiroTitulos(con, inLegacy);
			if (out.P_OUT_TICKETATENCION.equals(0L)) {
				out.P_OUT_ERRORESCONTADOR++;
				return out;
			}

			logger.debug("VALIDACIONES INICIALES");

			String estadoModulo = ConsultarEstadoModulo(in.p_IdEmpresa, in.p_IdUsuario, con);
			if (MODULO_ESTADO_CERRADO.equalsIgnoreCase(estadoModulo)) {
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "MODULO_CERRADO", new HashMap<String, String>(0), con, in.p_IdEmpresa));
				out.P_OUT_ERRORESCONTADOR++;
				return out;
			}

			if (instrumentos == null || instrumentos.isEmpty()) {
				return out;
			}

			List<objMesas> mesas = new ArrayList<objMesas>();
			List<Long> idsPortafolio = new ArrayList<>();
			boolean requiereMesa = false;

			for (UnificarInstrumentosEntrada.InstrumentoCLP inst : instrumentos) {
				if ("IF".equals(inst.IdClasificacionTipoInstrumento) || "RF".equals(inst.IdClasificacionTipoInstrumento)) {
					requiereMesa = true;
				}
				if (!idsPortafolio.contains(inst.IdPortafolio)) {
					idsPortafolio.add(inst.IdPortafolio);
				}
			}

			if (requiereMesa && !idsPortafolio.isEmpty()) {
				HashMap<String, Object> paramsMesas = new HashMap<>(3);
				paramsMesas.put("IdEmpresa", in.p_IdEmpresa);
				paramsMesas.put("IdPortafolio", idsPortafolio);
				paramsMesas.put("filtroIdPortafolio", 1);
				mesas = sql.query(con, SQL_SELECT_MESAS, paramsMesas, objMesas.class);

				if (mesas.isEmpty()) {
					paramsMesas.put("filtroIdPortafolio", 0);
					mesas = sql.query(con, SQL_SELECT_MESAS, paramsMesas, objMesas.class);
				}
			}

			logger.debug("TRANSFORMACION DEL INSTRUMENTO");
			List<RetiroCustodia.ObjDatosEntrada> p_RetiroCartera = new ArrayList<>();
			List<IngresoCustodia.ObjDatosEntrada> p_IngresoCartera = new ArrayList<>();
			String idMonedaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.MonedaSistema");

			HashMap<String, BigDecimal> cacheDolares = new HashMap<>();
			HashMap<String, InstrumentoGlobal> cacheHomologos = new HashMap<>();
			java.sql.Date fechaSistema = Fecha.toDate("0001-01-01");
			try {
				fechaSistema = Fecha.toDate(FechaDB.ObtenerFechaHoraSistemaLocal(con, in.p_IdEmpresa).toString().split(" ")[0]);
			} catch (Exception e) {
				logger.warn("No se pudo obtener fecha de BD, usando local", e);
				String hoyStr = new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());
				fechaSistema = Fecha.toDate(hoyStr);
			}
			
			for (UnificarInstrumentosEntrada.InstrumentoCLP instDual : instrumentos) {

				String fechaCompraOriginal = (instDual.FechaOperacionOriginal != null && !instDual.FechaOperacionOriginal.equals("0001-01-01") && !instDual.FechaOperacionOriginal.isEmpty()) ? instDual.FechaOperacionOriginal : instDual.FechaOperacion;

				BigDecimal valorDolarObservado;
				if (cacheDolares.containsKey(fechaCompraOriginal)) {
					valorDolarObservado = cacheDolares.get(fechaCompraOriginal);
				} else {
					valorDolarObservado = obtenerDolarHistorico(con, fechaCompraOriginal, in.p_IdEmpresa);
					cacheDolares.put(fechaCompraOriginal, valorDolarObservado);
				}

				if (valorDolarObservado.compareTo(BigDecimal.ZERO) <= 0) {
					out.P_OUT_MENSAJES.add(new MensajeSalida(instDual.IdGenerarMovimientoCustodia, MensajeSalida.MENSAJE_ERROR, "ERROR_DOLAR", "No se encontro valor del Dolar Observado para la fecha " + fechaCompraOriginal));
					out.P_OUT_ERRORESCONTADOR++;
					continue;
				}

				InstrumentoGlobal infoGlobal;
				if (cacheHomologos.containsKey(instDual.NemoDestino)) {
					infoGlobal = cacheHomologos.get(instDual.NemoDestino);
				} else {
					infoGlobal = obtenerHomologoGlobal(con, instDual.NemoDestino, in.p_IdEmpresa);
					cacheHomologos.put(instDual.NemoDestino, infoGlobal);
				}

				if (infoGlobal == null) {
					out.P_OUT_MENSAJES.add(new MensajeSalida(instDual.IdGenerarMovimientoCustodia, MensajeSalida.MENSAJE_ERROR, "ERROR_HOMOLOGO", "No se encontro un instrumento global valido para el Nemo origen: " + instDual.NemoOrigen));
					out.P_OUT_ERRORESCONTADOR++;
					continue;
				}

				BigDecimal precioGlobalUSD = BigDecimal.ZERO;
				if (valorDolarObservado.compareTo(BigDecimal.ZERO) > 0) {
					precioGlobalUSD = instDual.Precio.divide(valorDolarObservado, 8, java.math.RoundingMode.HALF_UP);
				}

				RetiroCustodia.ObjDatosEntrada retiro = new RetiroCustodia.ObjDatosEntrada();
				retiro.IdGenerarMovimientoCustodia = instDual.IdGenerarMovimientoCustodia;
				retiro.TablaAfectada = "CARTERA";
				retiro.AccionOperacion = "RETIRO";
				retiro.IdCliente = instDual.IdCliente;
				retiro.IdPortafolio = instDual.IdPortafolio;
				retiro.FechaOperacion = fechaSistema;
				retiro.FechaComprobante = fechaSistema;
				retiro.IdTipoOperacion = "RETTIT";
				retiro.IdConcepto = "RUDUAL";
				retiro.IdCondicionLiquidacion = instDual.IdCondicionLiquidacion;
				retiro.FechaLiquidacion = fechaSistema;
				retiro.IdTipoCanalIngreso = instDual.IdTipoCanalIngreso;
				retiro.IdTipoReajusteInstrumento = instDual.IdTipoReajuste;
				retiro.IdUsuario = in.p_IdUsuario;
				retiro.IdSucursal = instDual.IdSucursal;
				retiro.IdAgente = instDual.IdAgente;
				retiro.Glosa = "Retiro por Unificacion de Nemos CLP/USD";
				retiro.IdInstrumento = instDual.IdInstrumento;
				retiro.IdBoveda = instDual.IdBoveda;
				retiro.CuentaBoveda = instDual.CuentaBoveda;
				retiro.IdEmisor = instDual.IdEmisor;
				retiro.EsFungible = instDual.Fungible;
				retiro.IdMonedaEmision = instDual.IdMonedaEmision;
				retiro.Cantidad = instDual.Cantidad;
				retiro.CorteMinimo = instDual.CorteMinimo;
				retiro.PrecioCompra = instDual.Precio;
				retiro.TasaCompra = instDual.Tasa != null ? instDual.Tasa : BigDecimal.ZERO;
				retiro.MontoCompraMonedaInstrumento = instDual.MontoEnMonedaInstrumento != null ? instDual.MontoEnMonedaInstrumento : instDual.Monto;
				retiro.IdMonedaAdicional = instDual.IdMonedaAdicional;
				retiro.IdOperacion = instDual.IdGenerarMovimientoCustodia;
				retiro.IdClasificacionTipoInstrumento = instDual.IdClasificacionTipoInstrumento;
				retiro.IdMonedaInstrumento = instDual.IdMonedaInstrumento;
				retiro.Nemotecnico = instDual.NemoOrigen;
				retiro.MontoCompra = instDual.Monto;
				retiro.IdTipoProducto = instDual.IdTipoProducto;
				retiro.ValorRescate = instDual.ValorRescate;
				retiro.IdTipoComprobante = null;
				retiro.FolioComprobante = null;

				p_RetiroCartera.add(retiro);

				IngresoCustodia.ObjDatosEntrada ingreso = new IngresoCustodia.ObjDatosEntrada();
				Utils.copyPojo(retiro, ingreso);

				ingreso.AccionOperacion = "INGRESO";
				ingreso.IdTipoOperacion = "INGTIT";
				ingreso.FechaOperacion = fechaSistema;
				ingreso.FechaComprobante = fechaSistema;
				ingreso.FechaLiquidacion = fechaSistema;
				ingreso.IdConcepto = "IUDUAL";
				ingreso.Glosa = "INGRESO POR UNIFICACION DUAL";
				ingreso.EsRegistroNuevo = "S";
				ingreso.IdMovimientoCarteraEnTransitoOrigen = instDual.IdGenerarMovimientoCustodia;
				ingreso.IdInstrumento = infoGlobal.IdInstrumento;
				ingreso.Nemotecnico = infoGlobal.Nemotecnico;
				ingreso.IdEmisor = infoGlobal.IdEmisor;
				ingreso.IdMonedaInstrumento = "USD";
				ingreso.IdMonedaEmision = "USD";
				ingreso.PrecioCompra = precioGlobalUSD;
				ingreso.MontoCompra = instDual.Monto;
				ingreso.MontoCompraMonedaInstrumento = instDual.MontoEnMonedaInstrumento != null ? instDual.MontoEnMonedaInstrumento : instDual.Monto;
				ingreso.IdTipoComprobante = null;
				ingreso.FolioComprobante = null;
				ingreso.ValorRescate = instDual.ValorRescate;

				ingreso.IdMesa = (("IF").equalsIgnoreCase(instDual.IdClasificacionTipoInstrumento) || ("RF").equalsIgnoreCase(instDual.IdClasificacionTipoInstrumento)) && !mesas.isEmpty() ? mesas.get(0).IdMesa : 0L;

				p_IngresoCartera.add(ingreso);
			}

			logger.debug("REALIZAR RETIROS");
			if (!p_RetiroCartera.isEmpty()) {
				RetiroCustodia retiroCustodia = new RetiroCustodia();
				RetiroCustodia.RetiroCustodiaEntrada paramInSP = new RetiroCustodia.RetiroCustodiaEntrada();
				paramInSP.p_IdEmpresa = in.p_IdEmpresa;
				paramInSP.p_SimularOperacion = "N";
				paramInSP.p_TicketAtencion = out.P_OUT_TICKETATENCION;
				paramInSP.p_DatosEntrada = p_RetiroCartera;
				paramInSP.p_ValorizacionAutomatica = "N";

				RetiroCustodia.RetiroCustodiaSalida resultRetiros = retiroCustodia.logica(con, paramInSP);
				out.P_OUT_MENSAJES.addAll(resultRetiros.P_OUT_MENSAJES);

				if (resultRetiros.P_OUT_ERRORESCONTADOR.compareTo(0L) == 1) {
					out.P_OUT_ERRORESCONTADOR += resultRetiros.P_OUT_ERRORESCONTADOR;
					out.P_OUT_MENSAJES = out.P_OUT_MENSAJES.stream().filter(t -> t.tipo.equalsIgnoreCase(MensajeSalida.MENSAJE_ERROR)).collect(Collectors.toList());
					return out;
				}

				for (RetiroCustodia.RetiroCustodiaSalida.ObjDatosSalida result : resultRetiros.P_OUT_DATOSSALIDA) {
					for (RetiroCustodia.ObjDatosEntrada ret : p_RetiroCartera) {
						if (!ret.IdGenerarMovimientoCustodia.equals(result.IdGenerarMovimientoCustodia)) continue;

						UnificarInstrumentosSalida.ObjResultado resultadoProceso = new UnificarInstrumentosSalida.ObjResultado();
						Utils.copyPojo(result, resultadoProceso);
						out.P_OUT_RESULTADO.add(resultadoProceso);

						HashMap<String, String> valores = new HashMap<String, String>(2);
						if (("IF").equalsIgnoreCase(ret.IdClasificacionTipoInstrumento)) {
							valores.put("Cantidad", ret.ValorRescate.toString());
						} else {
							String Cantidad = MonedaFormato.formatear(con, ret.Cantidad, MonedaFormato.CANTIDAD, idMonedaSistema).valor.toString();
							valores.put("Cantidad", Cantidad);
						}
						valores.put("Folio", result.IdGenerarMovimientoCustodia.toString());
						out.P_OUT_MENSAJES.add(new MensajeSalida(result.IdGenerarMovimientoCustodia, MensajeSalida.MENSAJE_INFORMACION, "UI_RETIRO_TITULO_OK", valores, con, in.p_IdEmpresa));
					}
				}
			}

			logger.debug("REALIZAR INGRESO");
			if (!p_IngresoCartera.isEmpty()) {
				IngresoCustodia ingresoCustodia = new IngresoCustodia();
				IngresoCustodia.IngresoCustodiaEntrada paramInSP = new IngresoCustodia.IngresoCustodiaEntrada();
				paramInSP.p_IdEmpresa = in.p_IdEmpresa;
				paramInSP.p_IdModulo = in.p_IdModulo;
				paramInSP.p_SimularOperacion = "N";
				paramInSP.p_TicketAtencion = out.P_OUT_TICKETATENCION;
				paramInSP.p_DatosEntrada = p_IngresoCartera;
				paramInSP.p_ValorizacionAutomatica = "N";

				IngresoCustodia.IngresoCustodiaSalida resultIngresos = ingresoCustodia.logica(con, paramInSP);
				out.P_OUT_MENSAJES.addAll(resultIngresos.P_OUT_MENSAJES);

				if (resultIngresos.P_OUT_ERRORESCONTADOR.compareTo(0L) == 1) {
					out.P_OUT_ERRORESCONTADOR += resultIngresos.P_OUT_ERRORESCONTADOR;
					out.P_OUT_MENSAJES = out.P_OUT_MENSAJES.stream().filter(t -> t.tipo.equalsIgnoreCase(MensajeSalida.MENSAJE_ERROR)).collect(Collectors.toList());
					return out;
				}

				try (java.sql.PreparedStatement psUpdate = con.prepareStatement(SQL_UPDATE_CARTERA_FECHAS)) {
					for (IngresoCustodia.IngresoCustodiaSalida.ObjDatosSalida result : resultIngresos.P_OUT_DATOSSALIDA) {
						HashMap<String, String> valores = new HashMap<String, String>(1);
						valores.put("Folio", result.FolioCustodia.toString());

						UnificarInstrumentosSalida.ObjResultado resultadoProceso = new UnificarInstrumentosSalida.ObjResultado();
						Utils.copyPojo(result, resultadoProceso);
						out.P_OUT_RESULTADO.add(resultadoProceso);
						out.P_OUT_MENSAJES.add(
								new MensajeSalida(result.IdGenerarMovimientoCustodia, MensajeSalida.MENSAJE_INFORMACION,"UI_INGRESO_TITULO_OK", valores, con, in.p_IdEmpresa));
						
						for (UnificarInstrumentosEntrada.InstrumentoCLP inst : instrumentos) {
							if (inst.IdGenerarMovimientoCustodia.equals(result.IdGenerarMovimientoCustodia)) {
								String fechaCompraOrig = (inst.FechaOperacionOriginal != null
										&& !inst.FechaOperacionOriginal.equals("0001-01-01")
										&& !inst.FechaOperacionOriginal.isEmpty()) ? inst.FechaOperacionOriginal
												: inst.FechaOperacion;

								psUpdate.setDate(1, Fecha.toDate(fechaCompraOrig));
								psUpdate.setDate(2, Fecha.toDate(inst.FechaLiquidacion));
								psUpdate.setLong(3, result.FolioCustodia);
								psUpdate.addBatch();
								break;
							}
						}
					}
					psUpdate.executeBatch();
					logger.info("Fechas sobreescritas exitosamente en la tabla CARTERAS");

				} catch (Exception ex) {
					logger.error("Error al intentar sobreescribir en las carteras: ", ex);
				}
			}

		} catch (Exception e) {
			logger.error("Error critico en UnificarInstrumentosCLP: ", e);
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_INESPERADO",
					"Ocurrio un error inesperado durante el procesamiento: " + e.getMessage()));
			out.P_OUT_ERRORESCONTADOR++;
			throw e;
		}

		for (MensajeSalida resultMensajes : out.P_OUT_MENSAJES) {
			if (("ERROR").equalsIgnoreCase(resultMensajes.tipo)) {
				List<MensajeSalida> filteredMensajes = out.P_OUT_MENSAJES.stream().filter(mje -> mje.tipo.equals(MensajeSalida.MENSAJE_ERROR)).collect(Collectors.toList());
				out.P_OUT_MENSAJES.clear();
				out.P_OUT_MENSAJES.addAll(filteredMensajes);
				return out;
			}
		}

		logger.debug("VALORIZAR");
		CacheValorizacion.valorizar(con);

		logger.info("TERMINO::UnificarInstrumentosCLP");
		return out;
	}

	private String ConsultarEstadoModulo(Long idEmpresa, Long idUsuario, Connection conn) {
		String estado = "";
		InConsultarEstadoModulo entrada = new InConsultarEstadoModulo();
		entrada.p_IdEmpresa = idEmpresa;
		entrada.p_IdUsuario = idUsuario;
		entrada.p_IdModulo = "CUS";
		ConsultarEstadoModulo objConsulta = new ConsultarEstadoModulo();

		try {
			OutConsultarEstadoModulo salida = objConsulta.logica(conn, entrada);
			for (Modulo modulo : salida.p_out_ListaModulos) {
				if (entrada.p_IdModulo.equals(modulo.NombreModulo)) {
					estado = modulo.IdEstadoModulo;
					break;
				}
			}
		} catch (Exception e) {
			estado = "";
			e.printStackTrace();
		}
		return estado;
	}

	private BigDecimal obtenerDolarHistorico(Connection con, String fechaOriginal, Long idEmpresa) throws Exception {
		SQL sql = new SQL();
		HashMap<String, Object> params = new HashMap<>();
		params.put("FechaOperacion", fechaOriginal);

		List<DolarHistorico> result = sql.query(con, SQL_SELECT_DOLAR_HISTORICO, params, DolarHistorico.class);

		if (result != null && !result.isEmpty() && result.get(0).ValorDolar != null && result.get(0).ValorDolar.compareTo(BigDecimal.ZERO) > 0) {
			return result.get(0).ValorDolar;
		}

		return BigDecimal.ZERO;
	}

	private InstrumentoGlobal obtenerHomologoGlobal(Connection con, String nemoDestino, Long idEmpresa) throws Exception {
		SQL sql = new SQL();
		HashMap<String, Object> params = new HashMap<>();

		params.put("NemoDestino", nemoDestino);

		List<InstrumentoGlobal> result = sql.query(con, SQL_SELECT_HOMOLOGO_GLOBAL, params, InstrumentoGlobal.class);

		if (result != null && !result.isEmpty()) {
			return result.get(0);
		}

		return null;
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		UnificarInstrumentosEntrada jin = gson.fromJson(in.toString(), UnificarInstrumentosEntrada.class);
		UnificarInstrumentosSalida out = logica(conn, jin);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);

		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa02", "connections.properties")) {
			try {
				Gson gson = Utils.getGsonBuilder();

				String jsonIngreso = "{\"p_IdEmpresa\":1,\"p_IdModulo\":\"CUS\",\"p_IdUsuario\":9802,\"P_INSTRUMENTOS\":[{\"NemoOrigen\":\"AAPLCL\",\"NemoDestino\":\"AAPL\"},{\"NemoOrigen\":\"AMZNCL\",\"NemoDestino\":\"AMZN\"}]}";
				UnificarInstrumentosEntrada Objin = gson.fromJson(jsonIngreso, UnificarInstrumentosEntrada.class);

				UnificarInstrumentosCLP kk = new UnificarInstrumentosCLP();
				System.out.println(kk.logica(con, Objin));
				con.commit();
			} catch (Exception er) {
				er.printStackTrace();
				con.rollback();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}