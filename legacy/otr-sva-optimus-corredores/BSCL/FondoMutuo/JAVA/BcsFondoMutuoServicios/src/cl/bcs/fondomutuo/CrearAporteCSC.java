package cl.bcs.fondomutuo;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.mer.CalcularParidad;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadSalida;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoSalida;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente.FechasIn;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.Feriados;
import cl.bcs.plataforma.pla.utils.MonedaFormato;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.tesoreria.CrearComprobante;
import cl.bcs.tesoreria.CrearIngresoEgreso;
import cl.bcs.tesoreria.LiquidarComprobante;
import cl.bcs.tesoreria.CrearIngresoEgreso.CrearIngresoEgreso_Entrada;
import cl.bcs.tesoreria.CrearIngresoEgreso.CrearIngresoEgreso_Salida;
import cl.bcs.tesoreria.CrearIngresoEgreso.IngresoEgresoGenerado_Salida;
import cl.bcs.tesoreria.LiquidarComprobante.LiquidarComprobante_Entrada;
import cl.bcs.tesoreria.LiquidarComprobante.LiquidarComprobante_Salida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Utils;
import cl.bcs.utils.SQL;
import cl.bcs.APV.CrearRecaudacionDeposito.CuentaBanco;
import cl.bcs.APV.CrearRecaudacionDeposito.Resultado;
import cl.bcs.cuentaInversion.webservices.CrearCargoAbonoWS;
import cl.bcs.cuentaInversion.webservices.CrearCargoAbonoWS.CargoAbonoAGenerar;
import cl.bcs.cuentaInversion.webservices.CrearCargoAbonoWS.EntradaCrearCargoAbonoWS;
import cl.bcs.cuentaInversion.webservices.CrearCargoAbonoWS.SalidaCrearCargoAbonoWS;
import cl.bcs.fondomutuo.CalcularMontoNumeroCuota.EntradaCalcularMontoNumeroCuota;
import cl.bcs.fondomutuo.CalcularMontoNumeroCuota.SalidaCalcularMontoNumeroCuota;
import cl.bcs.fondomutuo.CrearIngresoCSC.EntradaCrearIngresoCSC;
import cl.bcs.fondomutuo.CrearIngresoCSC.InIngresoCSC;
import cl.bcs.fondomutuo.CrearIngresoCSC.InIngresoCSCDetalle;
import cl.bcs.fondomutuo.CrearIngresoCSC.SalidaCrearIngresoCSC;
import cl.bcs.fondomutuo.CrearSolicitudAporteRescateNormal.EntradaCrearSolicitudAporteRescateNormal;
import cl.bcs.fondomutuo.CrearSolicitudAporteRescateNormal.InFormaPagoSpot;
import cl.bcs.fondomutuo.CrearSolicitudAporteRescateNormal.SalidaCrearSolicitudAporteRescateNormal;
import cl.bcs.fondomutuo.ProcesarReinversionRescate.ClaseUtilitaria;
import cl.bcs.fondomutuo.pojo.ClientePortafolioSolicitudes;
import cl.bcs.fondomutuo.pojo.ConstantesFondoMutuo;
import cl.bcs.fondomutuo.pojo.ResultadoSolicitud;
import cl.bcs.fondomutuo.CrearSolicitudAporteRescateNormal.InSolicitudesAporteRescate;

public class CrearAporteCSC implements JavaCall {
	private static final Logger logger = LogManager.getLogger(CrearAporteCSC.class);
	
	private final ConstantesFondoMutuo _c = new ConstantesFondoMutuo();
	
	private final String SQL_SELECT_IDPERSONA = loadResourceToString(
			"cl/bcs/socionegocio/soc/sql/CrearSocioNegocio_SelectIdPersona.sql");
	private final String SQL_IdCuentaBanco = "SELECT \"IdCuentaBanco\" from \"BSCL.Tesoreria.BD.Vistas::ConsultarCuentasBancarias\""
			+ " WHERE \"IdBanco\" = :IdBanco" + " AND \"NumeroCuentaBancaria\" = :NumeroCuentaBancaria AND \"IdMoneda\" = :IdMoneda";
	private final String SQL_SELECT_MONEDA_FONDO = loadResourceToString("cl/bcs/fondomutuo/sql/CrearAporteCSC_SelectMonedaFondo.sql");
	private final String SQL_SELECT_PORTAFOLIO = loadResourceToString("cl/bcs/fondomutuo/sql/ComunesSolicitudes_SelectPortafolio.sql");
	private final String SQL_SELECT_DOCUMENTO_IDENTIDAD_SEGUN_ID_CLIENTE = loadResourceToString("cl/bcs/fondomutuo/sql/CrearAporteCSC_SelectDocumentoIdentidadSegunIdCliente.sql");
	
	private String FolioComprobante = "";
	// -------------------------------------------------
	// CLASES DE ENTRADA
	// -------------------------------------------------
	public static class CrearAporteCSC_Entrada extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_ClaveUsuario;
		public List<FondosMutuos> p_FondosMutuos;

		public CrearAporteCSC_Entrada() {
			p_FondosMutuos = new ArrayList<FondosMutuos>();
		}
	}

	public static class FondosMutuos extends BcsPojo {
		public Long IdInterno = 0L;
		public String Simula = "N";
		public Long IdCuentaBanco;
		public Long IdBanco = 0L;
		public String CuentaCorriente = "";
		public String FolioCSC = "";
		public String Hora = "";
		public String Fecha = "";
		public BigDecimal MontoPesos = BigDecimal.ZERO;
		public BigDecimal MontoUF = BigDecimal.ZERO;
		public BigDecimal MontoUSD = BigDecimal.ZERO;
		public String TipoPago = "";
		public Long IdCliente = 0L;
		public Long IdPortafolio;
		public String CodigoPortafolio;
		public String CodigoPortafolioCargo = "";
		public Long MontoAGF = 0L;
		public String Canal = "";
		public List<DistribucionDeFondo> p_DistribucionDeFondo;

		public FondosMutuos() {
			p_DistribucionDeFondo = new ArrayList<DistribucionDeFondo>();
		}

	}

	public static class DistribucionDeFondo extends BcsPojo {
		public String IdSerieFondo = "";
		public BigDecimal Monto;
		public BigDecimal Porcentaje;
		public String Regimen;
	}

	// -------------------------------------------------
	// CLASES DE SALIDA
	// -------------------------------------------------
	public static class CrearAporteCSC_Salida extends BcsPojo {
		public Long P_OUT_ERRORESCONTADOR;
		public List<ResultadoOperacion> P_OUT_RESULTADO;

		public CrearAporteCSC_Salida() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_RESULTADO = new ArrayList<ResultadoOperacion>();
		}
		
	}

	public static class ResultadoOperacion extends BcsPojo {
		public String Origen;
		public String Identificador;
		public List<MensajesErrores> Mensajes;
	}

	public static class MensajesErrores extends BcsPojo {
		public Long IdInterno;
		public String TipoMensaje;
		public String Codigo;
		public String Mensaje;
		
		public MensajesErrores () {
			this.IdInterno = 0L;
			this.TipoMensaje = "";
			this.Codigo = "";
			this.Mensaje = "";
		}
		
		public MensajesErrores(Long idInterno,String tipoMensaje, String codigo, HashMap<String, String> valores, Connection con, Long idEmpresa) throws Exception {
			this.IdInterno = idInterno;
			this.TipoMensaje = tipoMensaje;
			this.Codigo = codigo;
			this.Mensaje = Diccionario.getMensaje(con, idEmpresa, codigo, valores);
		}
	}

	// Servicio Salida
	public static class ParametrosSalidaServicio extends BcsPojo {
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR;
		public Long P_OUT_IDREC;
		public List<Resultado> P_OUT_RESULTADO;

		public ParametrosSalidaServicio() {
			P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			P_OUT_RESULTADO = new ArrayList<Resultado>();
		}

	}

	// -------------------------------------------------
	// CLASES UTILITARIAS
	// -------------------------------------------------
	public static class SelectIdPersona extends BcsPojo {
		public Long IdPersona = 0L;
	}

	public static class SerieFondo extends BcsPojo {
		public String IdSerieFondo;
		public String IdMoneda;
	}
	
	public static class ParamsPortafolio_Update extends BcsPojo {
		public Long p_IdPortafolio;
		public Long p_IdEmpresa;
	}

	// -------------------------------------------------
	// LOGICA DEL SERVICIO
	// -------------------------------------------------
	public CrearAporteCSC_Salida logica(Connection con, CrearAporteCSC_Entrada entrada) throws Exception {
		logger.info("INICIO::CrearAporteCSC");
		
		CrearAporteCSC_Salida out = new CrearAporteCSC_Salida();

		SQL sql = new SQL();

		String FechaSistema = Utils.getFechaSistema(con, Utils.getSchema(), entrada.p_IdEmpresa);
		
		String FechaHoraSistema = Utils.getFechaHoraSistema(con, Utils.getSchema(), entrada.p_IdEmpresa);
		
		Timestamp fechaHoraUTC = FechaDB.ObtenerFechaHoraUTCTs(con);
		Integer zonaHoraria = FechaDB.ObtenerSegundosZonaHoraria(con, entrada.p_IdEmpresa);
		Timestamp fechaHoraLocal = Fecha.addSeconds(fechaHoraUTC, zonaHoraria);
		String fechaLocal = Fecha.toStringDate(fechaHoraLocal);
		ObtenerDiaSiguiente diaSgte = new ObtenerDiaSiguiente();
		InObtenerDiaSiguiente diaSgteIn = new InObtenerDiaSiguiente();
		HashMap<String, HashMap<String, String>> configuraciones = Utils.getConfiguraciones(con, entrada.p_IdEmpresa);
		long idPais = Long.valueOf(configuraciones.get("PLATAFORMA").get("PLA.PaisSistema"));
		boolean fechaLocalEsDiaHabil = Feriados.esDiaHabil(con, fechaLocal.toString(), idPais);
		
		if(!fechaLocalEsDiaHabil) {
			diaSgteIn.p_idPais = idPais;
			diaSgteIn.p_diaHabil = "S";
			diaSgteIn.p_fechas.add(new FechasIn(fechaLocal, 1L, idPais));
			fechaLocal = diaSgte.logica(con, diaSgteIn).p_out_fechas.stream().findFirst().get().fechaCalculada;
		}
		
		String HoraProceso = FechaDB.ObtenerHoraSistemaLocal(con, entrada.p_IdEmpresa);

		Long idInterno = 0L;
		Long ErroresContador = 0L;

		String SIM = "";
		Long idPortafolio = 0L;
	
		Long idFolioCSC = 0L;
		// Se rescata el IdPersona y IdPortafolioDefecto
		HashMap<String, Object> ParamsDatosEmpresa = new HashMap<String, Object>();
		ParamsDatosEmpresa.put("IdEmpresa", entrada.p_IdEmpresa);

		Long IdCuentaBanco = 0L;


		List<ResultadoOperacion> listaResultadoOperacion = new ArrayList<ResultadoOperacion>();
		List<ClaseUtilitaria> listaClaseUtilitaria = new ArrayList<ClaseUtilitaria>();
		List<String> series = new ArrayList<String>();
		// Si la entrada viene con info
		if (entrada.p_FondosMutuos.size() > 0) {


			HashMap<String,Object> params = new HashMap<String,Object>();
			
			for (FondosMutuos inEntradaFondosMutuos : entrada.p_FondosMutuos) {
				for (DistribucionDeFondo distribuciones : inEntradaFondosMutuos.p_DistribucionDeFondo) {
					if(!series.contains(distribuciones.IdSerieFondo)) series.add(distribuciones.IdSerieFondo);
				}
			}
			
			params.put("IdSerieFondo", series);
			List<SerieFondo> monedasFondo = sql.query(con, SQL_SELECT_MONEDA_FONDO, params, SerieFondo.class);
			params.clear();
			
			HashMap<String,Object> hSeries = Utils.List2Hash(monedasFondo, "IdSerieFondo");
			
			// Llamar al proceso de invocar la secuencia que es quien relfeja
			// que una operacion es de un mismo grupo
			List<Long> secuencias = new ArrayList<>();
			if(series.size() > 0) {
				secuencias = Utils.getSecuencia(con, "BSCL.FondoMutuo.BD.Secuencias::FolioContrato", 1L);
			}
			
			for (FondosMutuos inEntradaFondosMutuos : entrada.p_FondosMutuos) {
				SIM = inEntradaFondosMutuos.Simula.trim();
				// + Si es Simulacion
				idInterno = inEntradaFondosMutuos.IdInterno;
				idPortafolio = inEntradaFondosMutuos.IdPortafolio;
				String simulaCambioPortafolioEjecutadoOk = _c.NO;
				
				if(_c.Dolar.equals(obtenerMoneda(inEntradaFondosMutuos))) {
					out = validaMonedaDistribucionDolar(con, inEntradaFondosMutuos.p_DistribucionDeFondo, hSeries, entrada.p_IdEmpresa, idInterno);
					if(out.P_OUT_ERRORESCONTADOR > 0) {
						return out;
					}
				}
				
				out = validaClienteBloqueado(con, idInterno, idPortafolio, entrada.p_IdEmpresa);
				
				if(out.P_OUT_ERRORESCONTADOR > 0) {
					return out;
				}
				
				//Conversion de UF a Peso	
				if(inEntradaFondosMutuos.MontoUF.compareTo(BigDecimal.ZERO) != 0){
					CalcularParidad calcularParidad = new CalcularParidad();
					CalcularParidadEntrada calcularParidadEntrada = new CalcularParidadEntrada();
					calcularParidadEntrada.p_IdEmpresa = entrada.p_IdEmpresa;
					calcularParidadEntrada.p_IdTrabajo = 1L;
					CalculoEntrada calculoEntradaUF = new CalculoEntrada();
					calculoEntradaUF.IdTrabajo = 1L;
					calculoEntradaUF.IdInterno = idInterno;
					calculoEntradaUF.Fecha = FechaSistema;
					calculoEntradaUF.IdMonedaOrigen = _c.UF;
					calculoEntradaUF.IdMonedaDestino = _c.Pesos;
					calculoEntradaUF.ValorCambioLocalModificado = BigDecimal.ZERO;
					calculoEntradaUF.MontoOrigen =  inEntradaFondosMutuos.MontoUF;
					calculoEntradaUF.IdUso = "";
					calcularParidadEntrada.p_CalculosEntrada.add(calculoEntradaUF);
					
					CalcularParidadSalida calculoSalida = calcularParidad.logica(con, calcularParidadEntrada);
					HashMap<Long,Object> hashMontoMonedaPago = Utils.List2Hash(calculoSalida.p_out_CalculosSalida, "IdInterno");
					CalculoSalida montoPesos = (CalculoSalida) hashMontoMonedaPago.get(idInterno);
					inEntradaFondosMutuos.MontoPesos = MonedaFormato.formatear(con, montoPesos.MontoDestino, MonedaFormato.MONTO, _c.Pesos).valor;
				}
				
				//Si viene AGF se agrega al monto en pesos
				if (inEntradaFondosMutuos.MontoAGF != 0L) {
					inEntradaFondosMutuos.MontoPesos = inEntradaFondosMutuos.MontoPesos.add(new BigDecimal(inEntradaFondosMutuos.MontoAGF));
				}
				
				if (inEntradaFondosMutuos.IdCuentaBanco == null) {
					// Se rescata el IdCuentaBanco de la corredora en caso de
					// que no lo declaren
					HashMap<String, Object> ParamsCuentaBanco = new HashMap<String, Object>();
					ParamsCuentaBanco.put("IdBanco", inEntradaFondosMutuos.IdBanco);
					ParamsCuentaBanco.put("NumeroCuentaBancaria", inEntradaFondosMutuos.CuentaCorriente.trim());
					ParamsCuentaBanco.put("IdMoneda", obtenerMoneda(inEntradaFondosMutuos));
					List<CuentaBanco> _IdCuentaBanco = sql.query(con, SQL_IdCuentaBanco, ParamsCuentaBanco,
							CuentaBanco.class);
					if (_IdCuentaBanco.size() == 0) {
						// Se crea la lista para cargarla y luego pasarla al
						// proceso
						List<MensajesErrores> listaMensajesErrores = new ArrayList<MensajesErrores>();
						ResultadoOperacion objResultadoOperacion = new ResultadoOperacion();

						MensajesErrores mensajesErrores = new MensajesErrores();
						mensajesErrores.IdInterno = idInterno;
						mensajesErrores.TipoMensaje = MensajeSalida.MENSAJE_ERROR;
						mensajesErrores.Codigo = "UI_BANCO_CORREDORA_Y_CUENTA_NO_VALIDOS";
						mensajesErrores.Mensaje = "UI_BANCO_CORREDORA_Y_CUENTA_NO_VALIDOS";

						objResultadoOperacion.Origen = "FFMM";
						objResultadoOperacion.Identificador = idInterno.toString();

						listaMensajesErrores.add(mensajesErrores);
						objResultadoOperacion.Mensajes = listaMensajesErrores;
						listaResultadoOperacion.add(objResultadoOperacion);
						out.P_OUT_ERRORESCONTADOR = ErroresContador++;
						out.P_OUT_RESULTADO = listaResultadoOperacion;
						continue;
					} else
						IdCuentaBanco = _IdCuentaBanco.get(0).IdCuentaBanco;
				} else {
					IdCuentaBanco = inEntradaFondosMutuos.IdCuentaBanco;
				}
				
				if(!("").equals(inEntradaFondosMutuos.CodigoPortafolioCargo) && !("").equals(inEntradaFondosMutuos.CodigoPortafolio) ) {
					
					List<MensajesErrores> listaMensajes = new ArrayList<MensajesErrores>();
					if(inEntradaFondosMutuos.CodigoPortafolio.equals(inEntradaFondosMutuos.CodigoPortafolioCargo)) {
						if( inEntradaFondosMutuos.p_DistribucionDeFondo.isEmpty()) {
							listaMensajes.add(new MensajesErrores( idInterno, MensajeSalida.MENSAJE_ERROR, "UI_ERROR_CODIGOS_PORTAFOLIOS_IGUALES", new HashMap<String,String>(0), con, entrada.p_IdEmpresa));
						} else {
							listaMensajes.add(new MensajesErrores( idInterno, MensajeSalida.MENSAJE_ADVERTENCIA, "UI_ADVERTENCIA_CODIGOS_PORTAFOLIOS_IGUALES", new HashMap<String,String>(0), con, entrada.p_IdEmpresa));
						}
					} else {
						List<MensajesErrores> mensajesCrearCargoAbono = crearCargoAbonoCambioPortafolio(con, inEntradaFondosMutuos, entrada.p_IdUsuario, entrada.p_IdEmpresa, FechaSistema);
						listaMensajes.addAll(mensajesCrearCargoAbono);
					}
					
					ResultadoOperacion objResultadoOperacion = new ResultadoOperacion();
					
					objResultadoOperacion.Origen = "CSC";
					objResultadoOperacion.Identificador = idInterno.toString();

					objResultadoOperacion.Mensajes = listaMensajes;
					listaResultadoOperacion.add(objResultadoOperacion);
					out.P_OUT_RESULTADO = listaResultadoOperacion;
					
					if(listaMensajes.stream().filter(mens ->MensajeSalida.MENSAJE_ERROR.equals( mens.TipoMensaje)).count() > 0 ) {
						out.P_OUT_ERRORESCONTADOR =+  listaMensajes.stream().filter(mens ->MensajeSalida.MENSAJE_ERROR.equals( mens.TipoMensaje)).count();
						return out;
					}
					simulaCambioPortafolioEjecutadoOk = _c.SI.equals(inEntradaFondosMutuos.Simula) ? _c.SI : _c.NO;
				}
				
				if(inEntradaFondosMutuos.p_DistribucionDeFondo.size() > 0) {
					// ------------------------------------------------------------------
					// Llamar al ingreso del CSC
					// ------------------------------------------------------------------
					CrearIngresoCSC objInCrearIngresoCSC = new CrearIngresoCSC();
					EntradaCrearIngresoCSC objInEntradaCrearIngresoCSC = new EntradaCrearIngresoCSC();

					objInEntradaCrearIngresoCSC.p_IdEmpresa = entrada.p_IdEmpresa;
					objInEntradaCrearIngresoCSC.p_IdUsuario = entrada.p_IdUsuario;
					objInEntradaCrearIngresoCSC.p_IdModulo = "FMM";
					objInEntradaCrearIngresoCSC.p_SimularOperacion = inEntradaFondosMutuos.Simula.trim();
					objInEntradaCrearIngresoCSC.p_UsaReglasNegocio = "N";

					List<InIngresoCSC> listaInIngresoCSC = new ArrayList<InIngresoCSC>();
					InIngresoCSC objInIngresoCSC = new InIngresoCSC();

					objInIngresoCSC.FolioCSC = inEntradaFondosMutuos.FolioCSC;
					objInIngresoCSC.IdCliente = inEntradaFondosMutuos.IdCliente;
					objInIngresoCSC.IdPortafolio = inEntradaFondosMutuos.IdPortafolio;
					objInIngresoCSC.Hora = HoraProceso;
					objInIngresoCSC.Fecha = Fecha.toDate(FechaSistema);
					objInIngresoCSC.MontoPesos = inEntradaFondosMutuos.MontoUF.compareTo(BigDecimal.ZERO) == 0 ? inEntradaFondosMutuos.MontoPesos : BigDecimal.ZERO;
					objInIngresoCSC.MontoUF = inEntradaFondosMutuos.MontoUF;
					objInIngresoCSC.MontoUSD = inEntradaFondosMutuos.MontoUSD;
					objInIngresoCSC.TipoPago = inEntradaFondosMutuos.TipoPago; 
					objInIngresoCSC.IdBanco = inEntradaFondosMutuos.IdBanco;
					objInIngresoCSC.DomicilioSucursal = "";
					objInIngresoCSC.CodigoSucursal = "";
					objInIngresoCSC.NombreFuncionario = "";
					objInIngresoCSC.DocumentoIdentificacionFuncionario = "";
					objInIngresoCSC.FirmaFuncionario = "";
					objInIngresoCSC.DiaDescuento = 0L;
					objInIngresoCSC.AceptoCondiciones = "S";
					objInIngresoCSC.Estado = "ING";
					objInIngresoCSC.Canal = "M";
					objInIngresoCSC.IdFolioEdit = inEntradaFondosMutuos.FolioCSC;
					objInIngresoCSC.CodigoPortafolio = inEntradaFondosMutuos.CodigoPortafolio;
					objInIngresoCSC.AporteCSC = "S";

					// + Se Obtiene secuencia que agrupa las operaciones
					Long FolioContrato = secuencias.get(0);
					Long IdInterno = 0L;
					
					BigDecimal porcentaje = BigDecimal.ZERO;
					BigDecimal sumaPorcentaje = BigDecimal.ZERO;
					BigDecimal MontoDistribuido = BigDecimal.ZERO;
					
					// Se crea la lista para cargarla y luego pasarla al proceso
					List<InIngresoCSCDetalle> listaInIngresoCSCDetalle = new ArrayList<InIngresoCSCDetalle>();
					
					// Por cada Rescate llamo al servicio de crear solicitudes
					// FFMM
					for (DistribucionDeFondo inDistribucionContrato : inEntradaFondosMutuos.p_DistribucionDeFondo) {
						if (inDistribucionContrato.Monto == null && inDistribucionContrato.Porcentaje == null) {
							List<MensajesErrores> listaMensajesErrores = new ArrayList<MensajesErrores>();
							ResultadoOperacion objResultadoOperacion = new ResultadoOperacion();
							MensajesErrores mensajesErrores = new MensajesErrores();

							mensajesErrores.IdInterno = idInterno;
							mensajesErrores.TipoMensaje = "ERROR";
							mensajesErrores.Codigo = "ERROR_MONTO_O_PORCENTAJE_VACIO";
							mensajesErrores.Mensaje = "Debe ingresar monto o porcentaje a la distribución";

							objResultadoOperacion.Origen = "FFMM";
							objResultadoOperacion.Identificador = idInterno.toString();

							listaMensajesErrores.add(mensajesErrores);
							objResultadoOperacion.Mensajes = listaMensajesErrores;
							listaResultadoOperacion.add(objResultadoOperacion);

							out.P_OUT_ERRORESCONTADOR++;
							out.P_OUT_RESULTADO = listaResultadoOperacion;
							return out;
						}else{
							if(inDistribucionContrato.Monto != null){
								porcentaje = new BigDecimal(100).multiply(inDistribucionContrato.Monto.divide(obtenerMonto(inEntradaFondosMutuos), MathContext.DECIMAL128));
								sumaPorcentaje = sumaPorcentaje.add(porcentaje);
								inDistribucionContrato.Porcentaje = porcentaje;
								MontoDistribuido = MontoDistribuido.add(inDistribucionContrato.Monto);
								inDistribucionContrato.Monto = null; //Se calcula despues
							}else{
								sumaPorcentaje = sumaPorcentaje.add(inDistribucionContrato.Porcentaje);
							}
						}
						sumaPorcentaje = sumaPorcentaje.setScale(4, RoundingMode.HALF_DOWN);					
						// Setea el Id Interno que es el que permite agrupar las
						// operaciones
						IdInterno++;
						ClaseUtilitaria claseUtilitaria = new ClaseUtilitaria();

						InIngresoCSCDetalle objInIngresoCSCDetalle = new InIngresoCSCDetalle();

						objInIngresoCSCDetalle.Serie = inDistribucionContrato.IdSerieFondo;
						objInIngresoCSCDetalle.Monto = inDistribucionContrato.Monto;
						objInIngresoCSCDetalle.Porcentaje = inDistribucionContrato.Porcentaje;
						objInIngresoCSCDetalle.Regimen = inDistribucionContrato.Regimen;

						claseUtilitaria.FolioContrato = FolioContrato;
						claseUtilitaria.IdInterno = IdInterno;

						listaInIngresoCSCDetalle.add(objInIngresoCSCDetalle);
						listaClaseUtilitaria.add(claseUtilitaria);
					}
					// Fin for detalle aporte
					objInIngresoCSC.p_IngresoCSCDetalle = listaInIngresoCSCDetalle;
					
					
					
					if(sumaPorcentaje.compareTo(new BigDecimal(100)) !=0 && MontoDistribuido.compareTo(obtenerMonto(inEntradaFondosMutuos))!=0){
						
						List<MensajesErrores> listaMensajesErrores = new ArrayList<MensajesErrores>();
						ResultadoOperacion objResultadoOperacion = new ResultadoOperacion();

						MensajesErrores mensajesErrores = new MensajesErrores();
						mensajesErrores.IdInterno = idInterno;
						mensajesErrores.TipoMensaje = MensajeSalida.MENSAJE_ERROR;
						mensajesErrores.Codigo = "UI_TOTAL_DISTRIBUCION_DIFERENTE_A_MONTO";
						mensajesErrores.Mensaje = "UI_TOTAL_DISTRIBUCION_DIFERENTE_A_MONTO";
					
						objResultadoOperacion.Origen = "FFMM";
						objResultadoOperacion.Identificador = idInterno.toString();

						listaMensajesErrores.add(mensajesErrores);
						objResultadoOperacion.Mensajes = listaMensajesErrores;
						listaResultadoOperacion.add(objResultadoOperacion);
						out.P_OUT_ERRORESCONTADOR++;
						out.P_OUT_RESULTADO = listaResultadoOperacion;
						return out;
					}
					
					listaInIngresoCSC.add(objInIngresoCSC);
					objInEntradaCrearIngresoCSC.p_IngresoCSC = listaInIngresoCSC;

					// Invoca a la creacion de solicitudes CSC
					SalidaCrearIngresoCSC objOutSalidaCrearIngresoCSC = objInCrearIngresoCSC.logica(con,objInEntradaCrearIngresoCSC);
					
					idPortafolio = objOutSalidaCrearIngresoCSC.P_OUT_RESULTADO.get(0).IdPortafolio;
					idFolioCSC  = objOutSalidaCrearIngresoCSC.P_OUT_RESULTADO.get(0).IdFolioCSC;
					if (objOutSalidaCrearIngresoCSC.P_OUT_ERRORESCONTADOR > 0) {
						for (MensajeSalida errorDetalle : objOutSalidaCrearIngresoCSC.P_OUT_MENSAJES) {
							List<MensajesErrores> listaMensajesErrores = new ArrayList<MensajesErrores>();
							MensajesErrores mensajesErrores = new MensajesErrores();
							ResultadoOperacion objResultadoOperacion = new ResultadoOperacion();

							objResultadoOperacion.Origen = "CSC";
							objResultadoOperacion.Identificador =  FolioContrato.toString();

							mensajesErrores.IdInterno = idInterno;
							mensajesErrores.TipoMensaje = errorDetalle.tipo;
							mensajesErrores.Codigo = errorDetalle.codigo;
							mensajesErrores.Mensaje = errorDetalle.descripcion;

							listaMensajesErrores.add(mensajesErrores);
							objResultadoOperacion.Mensajes = listaMensajesErrores;
							listaResultadoOperacion.add(objResultadoOperacion);
							
							if(MensajeSalida.MENSAJE_ERROR.equals(errorDetalle.tipo) ){
								ErroresContador++;
							}
						}
						out.P_OUT_ERRORESCONTADOR = ErroresContador;
						out.P_OUT_RESULTADO = listaResultadoOperacion;
						return out;
					} else {
						for (MensajeSalida errorDetalle : objOutSalidaCrearIngresoCSC.P_OUT_MENSAJES) {
							List<MensajesErrores> listaMensajes = new ArrayList<MensajesErrores>();
							MensajesErrores mensajes = new MensajesErrores();
							ResultadoOperacion objResultadoOperacion = new ResultadoOperacion();

							objResultadoOperacion.Origen = "CSC";
							objResultadoOperacion.Identificador =objOutSalidaCrearIngresoCSC.P_OUT_RESULTADO.get(0).FolioCSC.toString();
							FolioComprobante = objInIngresoCSC.FolioCSC;
							mensajes.IdInterno = idInterno;
							mensajes.TipoMensaje = errorDetalle.tipo;
							mensajes.Codigo = errorDetalle.codigo;
							mensajes.Mensaje = errorDetalle.descripcion;

							listaMensajes.add(mensajes);
							objResultadoOperacion.Mensajes = listaMensajes;
							listaResultadoOperacion.add(objResultadoOperacion);
						}
						{
							List<MensajesErrores> listaMensajes = new ArrayList<MensajesErrores>();
							MensajesErrores mensajes = new MensajesErrores();
							ResultadoOperacion objResultadoOperacion = new ResultadoOperacion();
		
							objResultadoOperacion.Origen = "CSC";
							objResultadoOperacion.Identificador = idFolioCSC.toString();
		
							mensajes.IdInterno = idInterno;
							mensajes.TipoMensaje = "OK";
							mensajes.Codigo = "CREACION_CONTRATO_OK_ID_FOLIO_CSC";
							mensajes.Mensaje = "Creacion de contrato exitosa, id folio csc: " + idFolioCSC.toString();
		
							listaMensajes.add(mensajes);
							objResultadoOperacion.Mensajes = listaMensajes;
							listaResultadoOperacion.add(objResultadoOperacion);
						}
						
						if (inEntradaFondosMutuos.TipoPago.equalsIgnoreCase("CIN"))
						{ // Si paga con Cuenta Inversion, solo crea Solicitud de Aporte
							
							SalidaCrearSolicitudAporteRescateNormal objOutSalidaSolicitudAporte = crearSolicitudAporteFFMM(con, entrada, inEntradaFondosMutuos, idPortafolio, FolioContrato, FechaHoraSistema , objOutSalidaCrearIngresoCSC.P_OUT_RESULTADO.get(0).FolioCSC.toString(), FechaSistema, hSeries, listaResultadoOperacion, out, simulaCambioPortafolioEjecutadoOk);
							
							if (objOutSalidaSolicitudAporte.P_OUT_ERRORESCONTADOR > 0) {
								listaResultadoOperacion.clear();
								for (MensajeSalida errorDetalle : objOutSalidaSolicitudAporte.P_OUT_MENSAJES) {
									List<MensajesErrores> listaMensajesErrores = new ArrayList<MensajesErrores>();
									MensajesErrores mensajesErrores = new MensajesErrores();
									ResultadoOperacion objResultadoOperacion = new ResultadoOperacion();
												
									objResultadoOperacion.Origen = "CSC";
									objResultadoOperacion.Identificador = FolioContrato.toString();

									mensajesErrores.IdInterno = idInterno;
									mensajesErrores.TipoMensaje = errorDetalle.tipo;
									mensajesErrores.Codigo = errorDetalle.codigo;
									mensajesErrores.Mensaje = errorDetalle.descripcion;

									listaMensajesErrores.add(mensajesErrores);
									objResultadoOperacion.Mensajes = listaMensajesErrores;
									listaResultadoOperacion.add(objResultadoOperacion);
									if(MensajeSalida.MENSAJE_ERROR.equals(errorDetalle.tipo)) {
										ErroresContador++;
									}
								}
								out.P_OUT_ERRORESCONTADOR = ErroresContador;
								out.P_OUT_RESULTADO = listaResultadoOperacion;
								return out;
							} else {
								for (MensajeSalida mensaje : objOutSalidaSolicitudAporte.P_OUT_MENSAJES) {
									List<MensajesErrores> listaMensajes = new ArrayList<MensajesErrores>();
									ResultadoOperacion objResultadoOperacion = new ResultadoOperacion();
									MensajesErrores mensajes = new MensajesErrores();

									mensajes.IdInterno = idInterno;
									mensajes.TipoMensaje = mensaje.tipo;
									mensajes.Codigo = mensaje.codigo;
									mensajes.Mensaje = mensaje.descripcion;

									objResultadoOperacion.Origen = "FFMM";
									objResultadoOperacion.Identificador = idInterno.toString();

									listaMensajes.add(mensajes);
									objResultadoOperacion.Mensajes = listaMensajes;
									listaResultadoOperacion.add(objResultadoOperacion);
								}
								
								for (ResultadoSolicitud resultadoSolicitud : objOutSalidaSolicitudAporte.P_OUT_RESULTADO) {
									List<MensajesErrores> listaMensajesErrores = new ArrayList<MensajesErrores>();
									MensajesErrores mensajes = new MensajesErrores();
									ResultadoOperacion objResultadoOperacion = new ResultadoOperacion();

									objResultadoOperacion.Origen = "CSC";
									objResultadoOperacion.Identificador = resultadoSolicitud.FolioContrato.toString();

									mensajes.IdInterno = idInterno;
									mensajes.TipoMensaje = "INFORMACION";
									mensajes.Codigo = "CREACION_APORTE_CSC_EXITOSA_FOLIO_CONTRATO";
									mensajes.Mensaje = "Creacion Aporte CSC Exitoso";

									listaMensajesErrores.add(mensajes);
									objResultadoOperacion.Mensajes = listaMensajesErrores;
									listaResultadoOperacion.add(objResultadoOperacion);
								}
							}
						}
						else { // Si NO se paga con Cuenta Inversion, hace todo > Abono en Cuenta inversion a través de tesoreria y crea la Solicitud de Aporte
							
							CrearIngresoEgreso_Salida OutTesoreria = crearIngresoTesoreria(con, inEntradaFondosMutuos, idFolioCSC, idPortafolio, fechaLocal, IdCuentaBanco, entrada.p_IdEmpresa, entrada.p_IdUsuario, "ABNCSC");
		
							if (OutTesoreria.P_OUT_ERRORESCONTADOR != null && OutTesoreria.P_OUT_ERRORESCONTADOR > 0) {
								listaResultadoOperacion.clear();
								for (MensajeSalida errorDetalle : OutTesoreria.P_OUT_MENSAJES) {
									List<MensajesErrores> listaMensajesErrores = new ArrayList<MensajesErrores>();
									MensajesErrores mensajesErrores = new MensajesErrores();
									ResultadoOperacion objResultadoOperacion = new ResultadoOperacion();
		
									objResultadoOperacion.Origen = "CSC";
									objResultadoOperacion.Identificador = FolioContrato.toString();
		
									mensajesErrores.IdInterno = idInterno;
									mensajesErrores.TipoMensaje = errorDetalle.tipo;
									mensajesErrores.Codigo = errorDetalle.codigo;
									mensajesErrores.Mensaje = errorDetalle.descripcion;
		
									listaMensajesErrores.add(mensajesErrores);
		
									objResultadoOperacion.Mensajes = listaMensajesErrores;
		
									listaResultadoOperacion.add(objResultadoOperacion);
									ErroresContador++;
									break;
								}
							} else {
								for (MensajeSalida errorDetalle : OutTesoreria.P_OUT_MENSAJES) {
									List<MensajesErrores> listaMensajes = new ArrayList<MensajesErrores>();
									MensajesErrores mensajes = new MensajesErrores();
									ResultadoOperacion objResultadoOperacion = new ResultadoOperacion();
		
									objResultadoOperacion.Origen = "CSC";
									objResultadoOperacion.Identificador = objOutSalidaCrearIngresoCSC.P_OUT_RESULTADO.get(0).FolioCSC.toString();
		
									mensajes.IdInterno = idInterno;
									mensajes.TipoMensaje = errorDetalle.tipo;
									mensajes.Codigo = errorDetalle.codigo;
									mensajes.Mensaje = errorDetalle.descripcion;
		
									listaMensajes.add(mensajes);
									objResultadoOperacion.Mensajes = listaMensajes;
									listaResultadoOperacion.add(objResultadoOperacion);
									break;
								}
		
								try {
									//-----------------------------------------------------------
									// Crea los comprobantes
									//-----------------------------------------------------------
									CrearComprobante.CrearComprobante_Salida OutComprobante = crearComprobante(con, OutTesoreria, entrada.p_IdEmpresa, entrada.p_IdUsuario, fechaLocal, inEntradaFondosMutuos.CodigoPortafolioCargo);
									
									// si hay errores al crear el comprobante
									if (MensajeSalida.getNumeroErrores(OutComprobante.P_OUT_MENSAJES) > 0) {
										for (MensajeSalida errorDetalle : OutComprobante.P_OUT_MENSAJES) {
											
											// limpiamos mensajes , para mostrar solo error
											listaResultadoOperacion.clear();
											List<MensajesErrores> listaMensajesErrores = new ArrayList<MensajesErrores>();
											MensajesErrores mensajesErrores = new MensajesErrores();
											ResultadoOperacion objResultadoOperacion = new ResultadoOperacion();
		
											objResultadoOperacion.Origen = "CSC";
											objResultadoOperacion.Identificador = objOutSalidaCrearIngresoCSC.P_OUT_RESULTADO.get(0).FolioCSC.toString();
		
											mensajesErrores.IdInterno = idInterno;
											mensajesErrores.TipoMensaje = errorDetalle.tipo;
											mensajesErrores.Codigo = errorDetalle.codigo;
											mensajesErrores.Mensaje = errorDetalle.descripcion;
		
											listaMensajesErrores.add(mensajesErrores);
											objResultadoOperacion.Mensajes = listaMensajesErrores;
											listaResultadoOperacion.add(objResultadoOperacion);
											if(MensajeSalida.MENSAJE_ERROR.equals(errorDetalle.tipo) ){
												ErroresContador++;
											}
										}
										out.P_OUT_ERRORESCONTADOR = ErroresContador;
										out.P_OUT_RESULTADO = listaResultadoOperacion;
										return out;
									} else {
										for (MensajeSalida errorDetalle : OutComprobante.P_OUT_MENSAJES) {
											List<MensajesErrores> listaMensajes = new ArrayList<MensajesErrores>();
											MensajesErrores mensajesErrores = new MensajesErrores();
											ResultadoOperacion objResultadoOperacion = new ResultadoOperacion();
		
											objResultadoOperacion.Origen = "CSC";
											objResultadoOperacion.Identificador = objOutSalidaCrearIngresoCSC.P_OUT_RESULTADO.get(0).FolioCSC.toString();
		
											mensajesErrores.IdInterno = idInterno;
											mensajesErrores.TipoMensaje = errorDetalle.tipo;
											mensajesErrores.Codigo = errorDetalle.codigo;
											mensajesErrores.Mensaje = errorDetalle.descripcion;
		
											listaMensajes.add(mensajesErrores);
											objResultadoOperacion.Mensajes = listaMensajes;
											listaResultadoOperacion.add(objResultadoOperacion);
											break;
										}
										
										LiquidarComprobante_Salida liquidarComprobante_Salida = liquidarComprobante(con, OutComprobante, entrada.p_IdEmpresa, entrada.p_IdUsuario, fechaLocal, inEntradaFondosMutuos.CodigoPortafolioCargo);
		
										if (MensajeSalida.getNumeroErrores(liquidarComprobante_Salida.P_OUT_MENSAJES) > 0) {
											for (MensajeSalida mensaje : liquidarComprobante_Salida.P_OUT_MENSAJES) {
												List<MensajesErrores> listaMensajesErrores = new ArrayList<MensajesErrores>();
												ResultadoOperacion objResultadoOperacion = new ResultadoOperacion();
												MensajesErrores mensajesErrores = new MensajesErrores();
		
												mensajesErrores.IdInterno = idInterno;
												mensajesErrores.TipoMensaje = mensaje.tipo;
												mensajesErrores.Codigo = mensaje.codigo;
												mensajesErrores.Mensaje = mensaje.descripcion;
		
												objResultadoOperacion.Origen = "CSC";
												objResultadoOperacion.Identificador = idInterno.toString();
		
												listaMensajesErrores.add(mensajesErrores);
												objResultadoOperacion.Mensajes = listaMensajesErrores;
												listaResultadoOperacion.add(objResultadoOperacion);
		
												if(MensajeSalida.MENSAJE_ERROR.equals(mensaje.tipo) ){
													ErroresContador++;
												}
											}
											out.P_OUT_ERRORESCONTADOR = ErroresContador;
											out.P_OUT_RESULTADO = listaResultadoOperacion;
											return out;
										} else {
											for (MensajeSalida mensaje : liquidarComprobante_Salida.P_OUT_MENSAJES) {
												List<MensajesErrores> listaMensajes = new ArrayList<MensajesErrores>();
												ResultadoOperacion objResultadoOperacion = new ResultadoOperacion();
												MensajesErrores mensajes = new MensajesErrores();
		
												mensajes.IdInterno = idInterno;
												mensajes.TipoMensaje = mensaje.tipo;
												mensajes.Codigo = mensaje.codigo;
												mensajes.Mensaje = mensaje.descripcion;
		
												objResultadoOperacion.Origen = "FFMM";
												objResultadoOperacion.Identificador = idInterno.toString();
		
												listaMensajes.add(mensajes);
												objResultadoOperacion.Mensajes = listaMensajes;
												listaResultadoOperacion.add(objResultadoOperacion);
											}
		
											// Llamar al ingreso del solicitudes Aporte
											try {
												
												if (inEntradaFondosMutuos.p_DistribucionDeFondo.size() > 0) {
		
													SalidaCrearSolicitudAporteRescateNormal objOutSalidaSolicitudAporte = crearSolicitudAporteFFMM(con, entrada, inEntradaFondosMutuos, idPortafolio, FolioContrato, FechaHoraSistema , objOutSalidaCrearIngresoCSC.P_OUT_RESULTADO.get(0).FolioCSC.toString(), FechaSistema, hSeries, listaResultadoOperacion, out, simulaCambioPortafolioEjecutadoOk);
													
													if (objOutSalidaSolicitudAporte.P_OUT_ERRORESCONTADOR > 0) {
														listaResultadoOperacion.clear();
														for (MensajeSalida errorDetalle : objOutSalidaSolicitudAporte.P_OUT_MENSAJES) {
															List<MensajesErrores> listaMensajesErrores = new ArrayList<MensajesErrores>();
															MensajesErrores mensajesErrores = new MensajesErrores();
															ResultadoOperacion objResultadoOperacion = new ResultadoOperacion();
																		
															objResultadoOperacion.Origen = "CSC";
															objResultadoOperacion.Identificador = FolioContrato.toString();
		
															mensajesErrores.IdInterno = idInterno;
															mensajesErrores.TipoMensaje = errorDetalle.tipo;
															mensajesErrores.Codigo = errorDetalle.codigo;
															mensajesErrores.Mensaje = errorDetalle.descripcion;
		
															listaMensajesErrores.add(mensajesErrores);
															objResultadoOperacion.Mensajes = listaMensajesErrores;
															listaResultadoOperacion.add(objResultadoOperacion);
															if(MensajeSalida.MENSAJE_ERROR.equals(errorDetalle.tipo)) {
																ErroresContador++;
															}
														}
														out.P_OUT_ERRORESCONTADOR = ErroresContador;
														out.P_OUT_RESULTADO = listaResultadoOperacion;
														return out;
													} else {
														for (MensajeSalida mensaje : objOutSalidaSolicitudAporte.P_OUT_MENSAJES) {
															List<MensajesErrores> listaMensajes = new ArrayList<MensajesErrores>();
															ResultadoOperacion objResultadoOperacion = new ResultadoOperacion();
															MensajesErrores mensajes = new MensajesErrores();
		
															mensajes.IdInterno = idInterno;
															mensajes.TipoMensaje = mensaje.tipo;
															mensajes.Codigo = mensaje.codigo;
															mensajes.Mensaje = mensaje.descripcion;
		
															objResultadoOperacion.Origen = "FFMM";
															objResultadoOperacion.Identificador = idInterno.toString();
		
															listaMensajes.add(mensajes);
															objResultadoOperacion.Mensajes = listaMensajes;
															listaResultadoOperacion.add(objResultadoOperacion);
														}
														
														for (ResultadoSolicitud resultadoSolicitud : objOutSalidaSolicitudAporte.P_OUT_RESULTADO) {
															List<MensajesErrores> listaMensajesErrores = new ArrayList<MensajesErrores>();
															MensajesErrores mensajes = new MensajesErrores();
															ResultadoOperacion objResultadoOperacion = new ResultadoOperacion();
		
															objResultadoOperacion.Origen = "CSC";
															objResultadoOperacion.Identificador = resultadoSolicitud.FolioContrato.toString();
		
															mensajes.IdInterno = idInterno;
															mensajes.TipoMensaje = "INFORMACION";
															mensajes.Codigo = "CREACION_APORTE_CSC_EXITOSA";
															mensajes.Mensaje = "Creacion Aporte CSC Exitoso";
		
															listaMensajesErrores.add(mensajes);
															objResultadoOperacion.Mensajes = listaMensajesErrores;
															listaResultadoOperacion.add(objResultadoOperacion);
														}
													}
												}
											} catch (Exception er2) {
												logger.error(er2);
												List<MensajesErrores> listaMensajesErrores = new ArrayList<MensajesErrores>();
												MensajesErrores mensajesErrores = new MensajesErrores();
		
												ResultadoOperacion objResultadoOperacion = new ResultadoOperacion();
		
												objResultadoOperacion.Origen = "CSC";
												objResultadoOperacion.Identificador = FolioContrato.toString();
		
												mensajesErrores.IdInterno = idInterno;
												mensajesErrores.TipoMensaje = "ERROR";
												mensajesErrores.Codigo = "ERROR_CREAR_APORTE";
												mensajesErrores.Mensaje = "Error interno al crear el aporte";
		
												listaMensajesErrores.add(mensajesErrores);
												objResultadoOperacion.Mensajes = listaMensajesErrores;
												listaResultadoOperacion.add(objResultadoOperacion);
		
												ErroresContador++;
												break;
											}
										}
									}
								} catch (Exception er1) {
									logger.error(er1);
									List<MensajesErrores> listaMensajesErrores = new ArrayList<MensajesErrores>();
									MensajesErrores mensajesErrores = new MensajesErrores();
		
									ResultadoOperacion objResultadoOperacion = new ResultadoOperacion();
		
									objResultadoOperacion.Origen = "CSC";
									objResultadoOperacion.Identificador = FolioContrato.toString();
		
									mensajesErrores.IdInterno = idInterno;
									mensajesErrores.TipoMensaje = "ERROR";
									mensajesErrores.Codigo = "ERROR_CREAR_COMPROBANTE";
									mensajesErrores.Mensaje = "Error interno al crear el comprobante";
		
									listaMensajesErrores.add(mensajesErrores);
									objResultadoOperacion.Mensajes = listaMensajesErrores;
									listaResultadoOperacion.add(objResultadoOperacion);
		
									ErroresContador++;
									break;
								}
							}

						}     // if (inEntradaFondosMutuos.TipoPago.equalsIgnoreCase("CIN"))
					}
				} else {
					
					if(inEntradaFondosMutuos.TipoPago.equalsIgnoreCase("CIN")) {
						List<MensajesErrores> listaMensajesErrores = new ArrayList<MensajesErrores>();
						MensajesErrores mensajesErrores = new MensajesErrores();

						ResultadoOperacion objResultadoOperacion = new ResultadoOperacion();

						objResultadoOperacion.Origen = "CSC";
						objResultadoOperacion.Identificador = idInterno.toString();

						mensajesErrores.IdInterno = idInterno;
						mensajesErrores.TipoMensaje = "ERROR";
						mensajesErrores.Codigo = "ERROR_MEDIO_PAGO_NO_VALIDO";
						mensajesErrores.Mensaje = "Cuenta Inversión no es un medio de pago valido, para modalidad solo abono.";

						listaMensajesErrores.add(mensajesErrores);
						objResultadoOperacion.Mensajes = listaMensajesErrores;
						listaResultadoOperacion.add(objResultadoOperacion);

						ErroresContador++;
						return out;
					}

					CrearIngresoEgreso_Salida OutTesoreria = crearIngresoTesoreria(con, inEntradaFondosMutuos, idFolioCSC, idPortafolio, fechaLocal, IdCuentaBanco, entrada.p_IdEmpresa, entrada.p_IdUsuario, "APORCI");
					
					if (OutTesoreria.P_OUT_ERRORESCONTADOR != null && OutTesoreria.P_OUT_ERRORESCONTADOR > 0) {
						listaResultadoOperacion.clear();
						for (MensajeSalida errorDetalle : OutTesoreria.P_OUT_MENSAJES) {
							List<MensajesErrores> listaMensajesErrores = new ArrayList<MensajesErrores>();
							MensajesErrores mensajesErrores = new MensajesErrores();
							ResultadoOperacion objResultadoOperacion = new ResultadoOperacion();
							
							objResultadoOperacion.Origen = "CSC";
							objResultadoOperacion.Identificador = idInterno.toString();
							
							mensajesErrores.IdInterno = idInterno;
							mensajesErrores.TipoMensaje = errorDetalle.tipo;
							mensajesErrores.Codigo = errorDetalle.codigo;
							mensajesErrores.Mensaje = errorDetalle.descripcion;
							
							listaMensajesErrores.add(mensajesErrores);
							
							objResultadoOperacion.Mensajes = listaMensajesErrores;
							
							listaResultadoOperacion.add(objResultadoOperacion);
							
							if(MensajeSalida.MENSAJE_ERROR.equals(errorDetalle.tipo) ){
								ErroresContador++;
							}							
						} 
						
						out.P_OUT_ERRORESCONTADOR = ErroresContador;
						out.P_OUT_RESULTADO = listaResultadoOperacion;
						return out;
					}
					
					for (MensajeSalida errorDetalle : OutTesoreria.P_OUT_MENSAJES) {
						List<MensajesErrores> listaMensajes = new ArrayList<MensajesErrores>();
						MensajesErrores mensajes = new MensajesErrores();
						ResultadoOperacion objResultadoOperacion = new ResultadoOperacion();
						
						objResultadoOperacion.Origen = "CSC";
						IngresoEgresoGenerado_Salida operacion = OutTesoreria.P_OUT_INGRESOEGRESOGENERADO
								.stream()
								.filter(operacionGenerada->operacionGenerada.NumeroReferencia.equals(errorDetalle.id))
								.findFirst()
								.orElse(null);
						objResultadoOperacion.Identificador = operacion == null ? "0" : String.valueOf(operacion.IdIngresoEgreso);
						
						mensajes.IdInterno = idInterno;
						mensajes.TipoMensaje = errorDetalle.tipo;
						mensajes.Codigo = errorDetalle.codigo;
						mensajes.Mensaje = errorDetalle.descripcion;
						
						listaMensajes.add(mensajes);
						objResultadoOperacion.Mensajes = listaMensajes;
						listaResultadoOperacion.add(objResultadoOperacion);
						break;
					}
					
					try {
						
						//-----------------------------------------------------------
						// Crea los comprobantes
						//-----------------------------------------------------------
						CrearComprobante.CrearComprobante_Salida OutComprobante = crearComprobante(con, OutTesoreria, entrada.p_IdEmpresa, entrada.p_IdUsuario, fechaLocal, inEntradaFondosMutuos.CodigoPortafolioCargo);
						
						// si hay errores al crear el comprobante
						if (MensajeSalida.getNumeroErrores(OutComprobante.P_OUT_MENSAJES) > 0) {
							for (MensajeSalida errorDetalle : OutComprobante.P_OUT_MENSAJES) {
								
								// limpiamos mensajes , para mostrar solo error
								listaResultadoOperacion.clear();
								List<MensajesErrores> listaMensajesErrores = new ArrayList<MensajesErrores>();
								MensajesErrores mensajesErrores = new MensajesErrores();
								ResultadoOperacion objResultadoOperacion = new ResultadoOperacion();
								
								objResultadoOperacion.Origen = "CSC";
								objResultadoOperacion.Identificador = idInterno.toString();
								
								mensajesErrores.IdInterno = idInterno;
								mensajesErrores.TipoMensaje = errorDetalle.tipo;
								mensajesErrores.Codigo = errorDetalle.codigo;
								mensajesErrores.Mensaje = errorDetalle.descripcion;
								
								listaMensajesErrores.add(mensajesErrores);
								objResultadoOperacion.Mensajes = listaMensajesErrores;
								listaResultadoOperacion.add(objResultadoOperacion);
								
								if(MensajeSalida.MENSAJE_ERROR.equals(errorDetalle.tipo) ){
									ErroresContador++;
								}
							}
							out.P_OUT_ERRORESCONTADOR = ErroresContador;
							out.P_OUT_RESULTADO = listaResultadoOperacion;
							return out;
						}
						
						LiquidarComprobante_Salida liquidarComprobante_Salida = liquidarComprobante(con, OutComprobante, entrada.p_IdEmpresa, entrada.p_IdUsuario, fechaLocal, inEntradaFondosMutuos.CodigoPortafolioCargo);
						
						if (MensajeSalida.getNumeroErrores(liquidarComprobante_Salida.P_OUT_MENSAJES) > 0) {
							for (MensajeSalida mensaje : liquidarComprobante_Salida.P_OUT_MENSAJES) {
								List<MensajesErrores> listaMensajesErrores = new ArrayList<MensajesErrores>();
								ResultadoOperacion objResultadoOperacion = new ResultadoOperacion();
								MensajesErrores mensajesErrores = new MensajesErrores();
								
								mensajesErrores.IdInterno = idInterno;
								mensajesErrores.TipoMensaje = mensaje.tipo;
								mensajesErrores.Codigo = mensaje.codigo;
								mensajesErrores.Mensaje = mensaje.descripcion;
								
								objResultadoOperacion.Origen = "CSC";
								objResultadoOperacion.Identificador = idInterno.toString();
								
								listaMensajesErrores.add(mensajesErrores);
								objResultadoOperacion.Mensajes = listaMensajesErrores;
								listaResultadoOperacion.add(objResultadoOperacion);
								
								if(MensajeSalida.MENSAJE_ERROR.equals(mensaje.tipo) ){
									ErroresContador++;
								}
							}
							out.P_OUT_ERRORESCONTADOR = ErroresContador;
							out.P_OUT_RESULTADO = listaResultadoOperacion;
							return out;
						}
						
						for (MensajeSalida mensaje : liquidarComprobante_Salida.P_OUT_MENSAJES) {
							List<MensajesErrores> listaMensajes = new ArrayList<MensajesErrores>();
							ResultadoOperacion objResultadoOperacion = new ResultadoOperacion();
							MensajesErrores mensajes = new MensajesErrores();
							
							mensajes.IdInterno = idInterno;
							mensajes.TipoMensaje = mensaje.tipo;
							mensajes.Codigo = mensaje.codigo;
							mensajes.Mensaje = mensaje.descripcion;
							
							objResultadoOperacion.Origen = "FFMM";
							cl.bcs.tesoreria.LiquidarComprobante.ResultadoProceso_Salida operacion = liquidarComprobante_Salida.P_OUT_RESULTADOPROCESO
									.stream()
									.filter(operacionGenerada->"IdComprobante".equals(operacionGenerada.Campo))
									.findFirst()
									.orElse(null);
							objResultadoOperacion.Identificador = operacion == null ? "0" : String.valueOf(operacion.Id);
							
							listaMensajes.add(mensajes);
							objResultadoOperacion.Mensajes = listaMensajes;
							listaResultadoOperacion.add(objResultadoOperacion);
						}
						
					} catch(Exception error) {
						logger.error(error);
						List<MensajesErrores> listaMensajesErrores = new ArrayList<MensajesErrores>();
						MensajesErrores mensajesErrores = new MensajesErrores();

						ResultadoOperacion objResultadoOperacion = new ResultadoOperacion();

						objResultadoOperacion.Origen = "CSC";
						objResultadoOperacion.Identificador = "0";

						mensajesErrores.IdInterno = idInterno;
						mensajesErrores.TipoMensaje = "ERROR";
						mensajesErrores.Codigo = "ERROR_CREAR_COMPROBANTE";
						mensajesErrores.Mensaje = "Error interno al crear el comprobante";

						listaMensajesErrores.add(mensajesErrores);
						objResultadoOperacion.Mensajes = listaMensajesErrores;
						listaResultadoOperacion.add(objResultadoOperacion);

						ErroresContador++;
						return out;
					}
				}
				
			}

			
			
			// fin si encabezado viene con datos
			if (SIM.equals("S")) {
				List<MensajesErrores> listaMensajesErrores = new ArrayList<MensajesErrores>();
				MensajesErrores mensajesErrores = new MensajesErrores();
				ResultadoOperacion objResultadoOperacion = new ResultadoOperacion();

				objResultadoOperacion.Origen = "CSC";
				objResultadoOperacion.Identificador = "0";

				mensajesErrores.IdInterno = idInterno;
				mensajesErrores.TipoMensaje = "OK";
				mensajesErrores.Codigo = "SIMULACION_EXITOSA";
				if (ErroresContador != 0) {
					mensajesErrores.Mensaje = "SIMULACION CON OBSERVACIONES";
				} else {
					mensajesErrores.Mensaje = "SIMULACION SE HA EJECUTADO EXITOSAMENTE";
				}
				listaMensajesErrores.add(mensajesErrores);
				objResultadoOperacion.Mensajes = listaMensajesErrores;
				listaResultadoOperacion.add(objResultadoOperacion);
				con.rollback();
			}

			// Hace todo o no hace nada
			if (ErroresContador > 0) {
				con.rollback();
			}

			out.P_OUT_ERRORESCONTADOR = ErroresContador;
			out.P_OUT_RESULTADO = listaResultadoOperacion;

			logger.info("FIN::CrearAporteCSC");
		}
		return out;
	}

	private List<MensajesErrores> crearCargoAbonoCambioPortafolio(Connection con, FondosMutuos inEntradaFondosMutuos, Long p_IdUsuario, Long p_IdEmpresa, String fechaSistema ) throws Exception {
		logger.debug("INICIO::CrearAporteCSC.crearCargoAbonoCambioPortafolio");
		Long inicio = System.currentTimeMillis();
		
		CrearCargoAbonoWS crearCargoAbonoWS = new CrearCargoAbonoWS();
		EntradaCrearCargoAbonoWS entradaCrearCargoAbonoWS = new EntradaCrearCargoAbonoWS();
		entradaCrearCargoAbonoWS.p_IdEmpresa = p_IdEmpresa;
		entradaCrearCargoAbonoWS.p_IdUsuario = p_IdUsuario;
		entradaCrearCargoAbonoWS.p_CargoAbonoAGenerar = obtenerCargosAbonosAGenerar(con, inEntradaFondosMutuos, p_IdEmpresa, fechaSistema);
		entradaCrearCargoAbonoWS.p_SimularOperacion = inEntradaFondosMutuos.Simula;
		SalidaCrearCargoAbonoWS SalidaCrearCargoAbonoWS = crearCargoAbonoWS.logica(con, entradaCrearCargoAbonoWS);
		
		List<MensajesErrores> listaMensajesErrores = SalidaCrearCargoAbonoWS.P_OUT_MENSAJES.stream().map(mens -> {
			MensajesErrores mensaje =new MensajesErrores();
			mensaje.IdInterno = mens.id;
			mensaje.TipoMensaje = mens.tipo;
			mensaje.Codigo = mens.codigo;
			mensaje.Mensaje = mens.descripcion;
			return mensaje;
		}).collect(Collectors.toList());
		
		Long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearAporteCSC.crearCargoAbonoCambioPortafolio", fin - inicio);
		return listaMensajesErrores;
	}

	private List<CargoAbonoAGenerar> obtenerCargosAbonosAGenerar(Connection con, FondosMutuos inEntradaFondosMutuos, Long p_IdEmpresa, String fechaSistema) throws Exception {
		logger.debug("INICIO::CrearAporteCSC.obtenerCargosAbonosAGenerar");
		Long inicio = System.currentTimeMillis();
		
		CargoAbonoAGenerar documentoIdentidad = consultarDocumentoIdentidadSegunIdCliente(con, inEntradaFondosMutuos.IdCliente);
		List<CargoAbonoAGenerar> cargos = new ArrayList<CargoAbonoAGenerar>();
		CargoAbonoAGenerar cargo = new CargoAbonoAGenerar();
		cargo.NumeroCargoAbono = 1L;
		cargo.DocumentoIdentidad = documentoIdentidad.DocumentoIdentidad;
		cargo.DocumentoIdentidadTraspaso = documentoIdentidad.DocumentoIdentidadTraspaso;
		cargo.CodigoPortafolio = inEntradaFondosMutuos.CodigoPortafolioCargo;
		cargo.CodigoPortafolioTraspaso = inEntradaFondosMutuos.CodigoPortafolio;
		cargo.IdMoneda = obtenerIdMonedaParaGenerarCargosAbonos(inEntradaFondosMutuos);
		cargo.IdTipoMovimiento = _c.TIPO_MOV_TRANSFERENCIA;
		cargo.IdConcepto = _c.CONCEPTO_CAMBIO_PORTAFOLIOS;
		cargo.Descripcion = "";
		cargo.FechaCondicionDeLiquidacion = fechaSistema;
		cargo.Monto = obtenerMontoCargosAbonos(cargo.IdMoneda, inEntradaFondosMutuos);
		cargo.CertificadoAutomatico = _c.SI;
		cargos.add(cargo);
		
		Long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearAporteCSC.obtenerCargosAbonosAGenerar", fin - inicio);
		return cargos;
	}
	
	private BigDecimal obtenerMontoCargosAbonos(String idMoneda, FondosMutuos inEntradaFondosMutuos) {
		logger.trace("INICIO::CrearAporteCSC.obtenerMontoCargosAbonos");
				
		BigDecimal monto = BigDecimal.ZERO;
		switch(idMoneda) {
			case "CLP" : {
				monto = inEntradaFondosMutuos.MontoPesos;
				break;
			}
			case "USD": {
				monto = inEntradaFondosMutuos.MontoUSD;
				break;
			}
			case "UF": {
				monto = inEntradaFondosMutuos.MontoUF;
				break;
			}
			default: {
				monto = BigDecimal.ZERO;
				break;
			}
		}
		
		logger.debug("FIN::CrearAporteCSC.obtenerMontoCargosAbonos");
		return monto;
	}

	private String obtenerIdMonedaParaGenerarCargosAbonos(FondosMutuos inEntradaFondosMutuos) {
		logger.debug("INICIO::CrearAporteCSC.obtenerIdMonedaParaGenerarCargosAbonos");
		
		String idMoneda = "";
		
		if(inEntradaFondosMutuos.MontoPesos.compareTo(BigDecimal.ZERO) > 0L) {
			idMoneda = _c.Pesos;
			return idMoneda;
		}
		
		if(inEntradaFondosMutuos.MontoUSD.compareTo(BigDecimal.ZERO) > 0L) {
			idMoneda =  _c.Dolar;
			return idMoneda;
		}
		
		if(inEntradaFondosMutuos.MontoUF.compareTo(BigDecimal.ZERO) > 0L) {
			idMoneda = _c.UF;
			return idMoneda;
		}
		
		logger.debug("FIN::CrearAporteCSC.obtenerIdMonedaParaGenerarCargosAbonos");
		return idMoneda;
	}

	private CargoAbonoAGenerar consultarDocumentoIdentidadSegunIdCliente(Connection con, Long idCliente) throws Exception{
		logger.debug("INICIO::CrearAporteCSC.consultarDocumentoIdentidadSegunIdCliente");
		Long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		
		HashMap<String, Object> params = new HashMap<>(1);
		params.put("IdCliente", idCliente);
		
		CargoAbonoAGenerar documentoIdentidad = sql.query(con, SQL_SELECT_DOCUMENTO_IDENTIDAD_SEGUN_ID_CLIENTE, params, CargoAbonoAGenerar.class).stream().findFirst().orElse(new CargoAbonoAGenerar());
		
		Long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearAporteCSC.consultarDocumentoIdentidadSegunIdCliente", fin - inicio);
		return documentoIdentidad;
	}



	public  SalidaCrearSolicitudAporteRescateNormal crearSolicitudAporteFFMM(Connection con, CrearAporteCSC_Entrada entrada, FondosMutuos inEntradaFondosMutuos, Long idPortafolio, Long FolioContrato, String FechaHoraSistema , String FolioCSC, String FechaSistema, HashMap<String,Object> hSeries, List<ResultadoOperacion> listaResultadoOperacion, CrearAporteCSC_Salida out , String simulaCambioPortafolioEjecutadoOk) throws Exception
	{
		BigDecimal montoTotalDistribucion = BigDecimal.ZERO;
		Long idInternoSpot = 1L;		
		Long IdInterno = 0L;
		boolean hasVC = true;
		
		EntradaCrearSolicitudAporteRescateNormal objInEntradaAporteNormal = new EntradaCrearSolicitudAporteRescateNormal();
		CrearSolicitudAporteRescateNormal objInAporteNormal = new CrearSolicitudAporteRescateNormal();		
		List<InSolicitudesAporteRescate> listaSolicitudesAporteRescate = new ArrayList<InSolicitudesAporteRescate>();
		SalidaCrearSolicitudAporteRescateNormal objOutSalidaSolicitudAporte = new SalidaCrearSolicitudAporteRescateNormal();
		
		
		objInEntradaAporteNormal.p_IdEmpresa 			= entrada.p_IdEmpresa;
		objInEntradaAporteNormal.p_IdModulo		 		= "FMM";
		objInEntradaAporteNormal.p_IdUsuario	 		= entrada.p_IdUsuario;
		objInEntradaAporteNormal.p_SimularOperacion 	= inEntradaFondosMutuos.Simula;
		objInEntradaAporteNormal.p_UsaReglasNegocio 	= "S";
		objInEntradaAporteNormal.p_IdTipoCanalIngreso 	= inEntradaFondosMutuos.Canal;
		objInEntradaAporteNormal.p_IdOrigenProceso		= _c.CSC;
		objInEntradaAporteNormal.p_PermiteOperarProspectos = _c.SI;
		
		CalcularMontoNumeroCuota calculo = new CalcularMontoNumeroCuota();
		EntradaCalcularMontoNumeroCuota entradaCalculo = new EntradaCalcularMontoNumeroCuota();
		
		if (inEntradaFondosMutuos.p_DistribucionDeFondo.size() > 0) {
						
			for (DistribucionDeFondo inDistribucionContrato : inEntradaFondosMutuos.p_DistribucionDeFondo) {
				IdInterno++;
				
				BigDecimal montoAporteCalculado = obtenerMonto(inEntradaFondosMutuos);
				String moneda = obtenerMoneda(inEntradaFondosMutuos);
				
				// Se calcula el monto si es que viene en porcentaje en la distribucion
				if (inDistribucionContrato.Monto == null && inDistribucionContrato.Porcentaje != null) {
					montoAporteCalculado = montoAporteCalculado.multiply(inDistribucionContrato.Porcentaje.divide(new BigDecimal("100"), MathContext.DECIMAL128));
					montoAporteCalculado = MonedaFormato.formatear(con, montoAporteCalculado, MonedaFormato.MONTO, moneda).valor;
					montoTotalDistribucion = montoTotalDistribucion.add(montoAporteCalculado);
				}
				

				InSolicitudesAporteRescate objInSolicitudAporte = new InSolicitudesAporteRescate();
				SerieFondo sf = (SerieFondo) hSeries.get(inDistribucionContrato.IdSerieFondo);
				objInSolicitudAporte.CalculoPorMontoCuota = "M";
				objInSolicitudAporte.EsPorMontoLiquido = "N";
				objInSolicitudAporte.FechaOperacion = FechaHoraSistema;
				objInSolicitudAporte.FolioContrato = FolioContrato;
				objInSolicitudAporte.Glosa = "CREAR APORTE CSC";
				objInSolicitudAporte.IdAutorizadoOrigen = 0L;
				objInSolicitudAporte.IdCliente = inEntradaFondosMutuos.IdCliente; // Ojo
				objInSolicitudAporte.IdEmisorDestino = "";
				objInSolicitudAporte.IdInterno = IdInterno;
				objInSolicitudAporte.IdMedioDePago = "CIN";
				objInSolicitudAporte.IdMoneda = sf.IdMoneda;
				objInSolicitudAporte.IdMonedaPago = moneda;
				objInSolicitudAporte.IdPortafolio = idPortafolio;
				objInSolicitudAporte.IdPreferenciaPagoPortafolio = 0L;
				objInSolicitudAporte.IdRegimenTributario = inDistribucionContrato.Regimen;
				objInSolicitudAporte.IdSerieFondo = inDistribucionContrato.IdSerieFondo;
				objInSolicitudAporte.IdSerieFondoDestino = "";
				objInSolicitudAporte.IndicadorMLT = "N"; // Ojo
				objInSolicitudAporte.Monto = montoAporteCalculado;
				objInSolicitudAporte.NumeroCuotas = BigDecimal.ZERO; // Ojo
				objInSolicitudAporte.OrigenOperacion = "M";
				objInSolicitudAporte.Porcentaje = inDistribucionContrato.Porcentaje;
				objInSolicitudAporte.SolicitudBatch = "N";
				objInSolicitudAporte.TipoSolicitud = "APO";
				objInSolicitudAporte.Visado = "N";
				objInSolicitudAporte.IngresoAutomatico = _c.NO;
				objInSolicitudAporte.TipoContratoOperacion = _c.CSC;
				objInSolicitudAporte.FolioContratoOperacion = FolioCSC;
				objInSolicitudAporte.SimulaCambioPortafolioEjecutadoOk = simulaCambioPortafolioEjecutadoOk;
				listaSolicitudesAporteRescate.add(objInSolicitudAporte);

			}
			
			//Se resta diferencia en caso de haber decimales 
			BigDecimal diferencia = obtenerMonto(inEntradaFondosMutuos).subtract(montoTotalDistribucion);
			InSolicitudesAporteRescate ultimoAporte = listaSolicitudesAporteRescate.get(listaSolicitudesAporteRescate.size() -1);
			if(ultimoAporte != null && diferencia.compareTo(BigDecimal.ZERO) != 0) {
				ultimoAporte.Monto = ultimoAporte.Monto.add(diferencia,MathContext.DECIMAL128);
				ultimoAporte.Monto = MonedaFormato.formatear(con,ultimoAporte.Monto,MonedaFormato.MONTO,_c.Pesos).valor;
			}
			
			
			for (InSolicitudesAporteRescate SolicitudAporte : listaSolicitudesAporteRescate) {
				entradaCalculo.p_IdEmpresa = entrada.p_IdEmpresa;
				CalcularMontoNumeroCuota.Serie serie = new CalcularMontoNumeroCuota.Serie();
				SerieFondo sf = (SerieFondo) hSeries.get(SolicitudAporte.IdSerieFondo);
				if(_c.Dolar.equals(sf.IdMoneda) && !_c.Dolar.equals(SolicitudAporte.IdMonedaPago)){
					
					serie.IdInterno = SolicitudAporte.IdInterno;
					serie.Fecha = FechaSistema.toString();
					serie.IdSerieFondo = sf.IdSerieFondo;
					serie.CalculoPorMontoCuota = "M";
					serie.Monto = SolicitudAporte.Monto;
					serie.NumeroCuotas = BigDecimal.ZERO;
					serie.IdMonedaFondo = sf.IdMoneda;
					serie.IdMonedaInversion = _c.Pesos;
					serie.TipoSolicitud = "APO";
					serie.TipoFactorAjuste = _c.UF;
					serie.IdEmpresa = entrada.p_IdEmpresa;
					entradaCalculo.p_Series.add(serie);
				}
			}
			
			SalidaCalcularMontoNumeroCuota salidaCalculo = calculo.logica(con, entradaCalculo);
			
			List<MensajesErrores> listaMensajesErroresNoVC = new ArrayList<MensajesErrores>();
			
			for (CalcularMontoNumeroCuota.Resultado sc : salidaCalculo.P_OUT_RESULTADO) {
				if (!sc.CodigoMensaje.equals("OK")) {
					if (salidaCalculo.P_OUT_RESULTADO.indexOf(sc) == 0) {
						listaResultadoOperacion.clear();
					}
					MensajesErrores mensajesErrores = new MensajesErrores();
					hasVC = false;
					mensajesErrores.TipoMensaje = "ERROR";
					mensajesErrores.IdInterno = sc.IdInterno;
					mensajesErrores.Codigo = sc.CodigoMensaje;
					mensajesErrores.Mensaje = sc.Resultado;

					listaMensajesErroresNoVC.add(mensajesErrores);
					out.P_OUT_ERRORESCONTADOR++;
				}
			}
				
				
				
			if (!hasVC) {
				ResultadoOperacion objResultadoOperacion = new ResultadoOperacion();
				objResultadoOperacion.Origen = "CSC";
				objResultadoOperacion.Identificador = FolioContrato.toString();
				objResultadoOperacion.Mensajes = listaMensajesErroresNoVC;
				listaResultadoOperacion.add(objResultadoOperacion);
				out.P_OUT_RESULTADO = listaResultadoOperacion;
				
			}
			
			HashMap<Long,Object> valores = Utils.List2Hash(salidaCalculo.P_OUT_RESULTADO, "IdInterno");
			
			for (InSolicitudesAporteRescate solicitud : listaSolicitudesAporteRescate) {
				
				CalcularMontoNumeroCuota.Resultado vc = (CalcularMontoNumeroCuota.Resultado) valores.get(solicitud.IdInterno);
				
				if(vc != null){
					
					List<InFormaPagoSpot> p_FormaPagoSpot = new ArrayList<>();
					p_FormaPagoSpot = new ArrayList<InFormaPagoSpot>();
					idInternoSpot++;
					
					InFormaPagoSpot FormaPagoSpot = new InFormaPagoSpot();
					FormaPagoSpot.IdInternoPagoOperacion = idInternoSpot;
					FormaPagoSpot.IdComprobantePagoOperacion= 0L;
					FormaPagoSpot.IdTipoMovimiento = "MOVEGR";
					FormaPagoSpot.IdFormaDePago    = "VENSPT_CIN_USD_0";
					FormaPagoSpot.IdOperacion      = 	solicitud.IdInterno;
					FormaPagoSpot.RecibePagaPortafolio = "P";
					FormaPagoSpot.IdMoneda         =  solicitud.IdMoneda;
					FormaPagoSpot.IdMonedaLiquidacion  =  solicitud.IdMoneda;
					FormaPagoSpot.MontoFormaDePago = vc.Monto;
					FormaPagoSpot.AfectarTesoreria =  "N";
					p_FormaPagoSpot.add(FormaPagoSpot);
					
					idInternoSpot++;
					
					InFormaPagoSpot FormaPagoSpot2 = new InFormaPagoSpot();
					FormaPagoSpot2.IdInternoPagoOperacion = idInternoSpot;
					FormaPagoSpot2.IdComprobantePagoOperacion= 0L;
					FormaPagoSpot2.IdTipoMovimiento = "MOVING";
					FormaPagoSpot2.IdFormaDePago    = "COMSPT_CIN_CLP_0";
					FormaPagoSpot2.IdOperacion      = 	solicitud.IdInterno;
					FormaPagoSpot2.RecibePagaPortafolio = "R";
					FormaPagoSpot2.IdMoneda         = _c.Pesos;
					FormaPagoSpot2.IdMonedaLiquidacion =  _c.Pesos;
					FormaPagoSpot2.MontoFormaDePago = solicitud.Monto;
					FormaPagoSpot2.AfectarTesoreria =  "N";
					p_FormaPagoSpot.add(FormaPagoSpot2);
					solicitud.ForzarSpot = _c.SI;
					solicitud.Monto = vc.Monto;
					solicitud.NumeroCuotas=	BigDecimal.ZERO;	
					objInEntradaAporteNormal.p_FormaPagoSpot.addAll(p_FormaPagoSpot);
				}
				
			}
			objInEntradaAporteNormal.p_SolicitudesAporteRescate = listaSolicitudesAporteRescate;
			
		}
		
		if (objInEntradaAporteNormal.p_SolicitudesAporteRescate.size() > 0) {
			objOutSalidaSolicitudAporte = objInAporteNormal.logica(con, objInEntradaAporteNormal);
		}
				
		return objOutSalidaSolicitudAporte;
	}
		
	private CrearIngresoEgreso_Salida crearIngresoTesoreria(Connection con, FondosMutuos inEntradaFondosMutuos, Long idFolioCSC, Long idPortafolio, String fechaSistema, Long idCuentaBanco, Long idEmpresa, Long idUsuario, String idConcepto) throws Exception {
		if(!("").equals(inEntradaFondosMutuos.CodigoPortafolioCargo))  {
			return new CrearIngresoEgreso_Salida();
		} 
		HashMap<String, Object> parametroEntrada = new HashMap<String, Object>();
		parametroEntrada.put("p_IdCliente", inEntradaFondosMutuos.IdCliente);
		SQL sql = new SQL();
		List<SelectIdPersona> SelectIdPersona = sql.query(con, SQL_SELECT_IDPERSONA, parametroEntrada,SelectIdPersona.class);
		
		// -----------------------------------------------------------------------
		// Llamar al ingreso del Tesoreria y mover cuenta inversion
		// -----------------------------------------------------------------------
		
		// Listas de Forma de Pago
		ArrayList<CrearIngresoEgreso.IngresoEgreso_Entrada> paramsIngresoEgreso = new ArrayList<CrearIngresoEgreso.IngresoEgreso_Entrada>();
		ArrayList<CrearIngresoEgreso.FormaDePago_Entrada> paramsFormaDePago = new ArrayList<CrearIngresoEgreso.FormaDePago_Entrada>();

		// Objetos de ingreso e egreso
		CrearIngresoEgreso.IngresoEgreso_Entrada IngresoEgreso = new CrearIngresoEgreso.IngresoEgreso_Entrada(); // IngresoEgreso
		CrearIngresoEgreso.FormaDePago_Entrada FormaDePago = new CrearIngresoEgreso.FormaDePago_Entrada(); // FormaPagoTeso

		// Se crea movimiento de tesoreria con los datos de ingreso
		IngresoEgreso.IdTipoMovimiento = "MOVING";
		IngresoEgreso.IdTipoPersona = "CLI";
		IngresoEgreso.IdPersona = SelectIdPersona.get(0).IdPersona;
		IngresoEgreso.IdPortafolio = idPortafolio;
		IngresoEgreso.IdMoneda = obtenerMoneda(inEntradaFondosMutuos);
		IngresoEgreso.Monto = obtenerMonto(inEntradaFondosMutuos);
		IngresoEgreso.FechaLiquidacion = fechaSistema;
		IngresoEgreso.IdConcepto = idConcepto;
		IngresoEgreso.IdTipoPersonaTitularPago = "CLI";
		IngresoEgreso.IdPersonaTitularPago = SelectIdPersona.get(0).IdPersona;
		IngresoEgreso.IdPortafolioTitularPago = idPortafolio;
		IngresoEgreso.IdCaja = 0L;
		IngresoEgreso.SecuenciaAgrupacion = "";
		IngresoEgreso.SecuenciaRelacionada = null;
		IngresoEgreso.Glosa = "ABNCSC".equals(idConcepto) ? "Abono cliente por contrato CSC" : "Abono cliente";
		IngresoEgreso.IdEmpleadoDigitador = 0L;
		IngresoEgreso.IdSucursalEmpleadoDigitador = 0L;
		IngresoEgreso.IdTipoComprobante = "CSC";
		IngresoEgreso.FolioCSC = FolioComprobante;
		IngresoEgreso.IdComprobante = idFolioCSC; 
		IngresoEgreso.FechaComprobante = fechaSistema;
		IngresoEgreso.FechaLiquidacionComprobante = fechaSistema;
		IngresoEgreso.FolioCartera = 0L;
		IngresoEgreso.IdInstrumento = 0L;
		IngresoEgreso.IdTipoInstrumento = "";
		IngresoEgreso.IdClasificacionTipoInstrumento = "";
		IngresoEgreso.IdTipoProducto = "";
		IngresoEgreso.IdTipoOperacion = "";//Si se manda IdTipoOperacion exige IdOperacion
		IngresoEgreso.IdOperacion = 0L;//Si se manda IdTipoOperacion exige IdOperacion
		IngresoEgreso.FechaOperacion = fechaSistema;
		IngresoEgreso.IdCondicionLiquidacion = "";
		IngresoEgreso.IdBolsa = "";
		IngresoEgreso.IdMonedaOperacion = "";
		IngresoEgreso.MontoOperacion = obtenerMonto(inEntradaFondosMutuos);
		IngresoEgreso.IdTipoReajuste = "";
		IngresoEgreso.FechaParidad = "0001-01-01";
		IngresoEgreso.ValorParidad = BigDecimal.valueOf(0);
		IngresoEgreso.Bilateral = "N";
		IngresoEgreso.GeneraComprobante = "N";
		IngresoEgreso.AgruparEnComprobante = "N";
		IngresoEgreso.MueveCuentaInversion = "S";
		IngresoEgreso.TitularPago = "N";
		IngresoEgreso.NumeroReferencia = 1L;

		paramsIngresoEgreso.add(IngresoEgreso);

		// Se crea forma de pago correspondiente al movimiento de
		// tesoreria con los datos de recaudacion
		FormaDePago.IdMedioDePago = "TRA";
		FormaDePago.IdModalidadDePago = "MODDEP";
		FormaDePago.IdMoneda = obtenerMoneda(inEntradaFondosMutuos);
		FormaDePago.Monto = obtenerMonto(inEntradaFondosMutuos);
		FormaDePago.IdCuentaBancariaCorredora = idCuentaBanco;
		FormaDePago.IdTarjetaBancariaCorredora = 0L;
		FormaDePago.IdChequera = 0L;
		FormaDePago.NumeroDocumentoCorredora = 0L;
		FormaDePago.FechaDocumentoPagoCorredora = "0001-01-01";
		FormaDePago.IdCuentaBancariaSocio = 0L;
		FormaDePago.IdBancoSocio = 0L;
		FormaDePago.NombreCuentaBancariaSocio = "";
		FormaDePago.NumeroCuentaBancariaSocio = "";
		FormaDePago.IdTipoCuentaBancariaSocio = "";
		FormaDePago.NombreTipoCuentaBancariaSocio = "";
		FormaDePago.IdTarjetaBancariaSocio = 0L;
		FormaDePago.IdTipoTarjetaBancariaSocio = "";
		FormaDePago.NombreTipoTarjetaBancariaSocio = "";
		FormaDePago.NumeroTarjetaBancariaSocio = "";
		FormaDePago.NumeroSerieSocio = "";
		FormaDePago.NumeroDocumentoSocio = 0L;
		FormaDePago.FechaDocumentoPagoSocio = fechaSistema;
		FormaDePago.NumeroReferencia = 1L;
		FormaDePago.NumeroReferenciaFormaDePago = 1L;

		paramsFormaDePago.add(FormaDePago);

		CrearIngresoEgreso_Entrada paramsIngresoEgreso_Entrada = new CrearIngresoEgreso_Entrada();

		paramsIngresoEgreso_Entrada.p_IdEmpresa = idEmpresa;
		paramsIngresoEgreso_Entrada.p_IdUsuario = idUsuario;
		paramsIngresoEgreso_Entrada.p_IdModulo = "TES";
		paramsIngresoEgreso_Entrada.p_SimularOperacion = "N";
		paramsIngresoEgreso_Entrada.p_UsaReglasNegocio = "S";
		paramsIngresoEgreso_Entrada.p_EsIngresoAutomatico = "N";
		paramsIngresoEgreso_Entrada.p_FormaDePago = paramsFormaDePago;
		paramsIngresoEgreso_Entrada.p_IngresoEgresoGenerar = paramsIngresoEgreso;				
		CrearIngresoEgreso procesoIngresoEgreso = new CrearIngresoEgreso();
		CrearIngresoEgreso_Salida outTesoreria = procesoIngresoEgreso.logica(con, paramsIngresoEgreso_Entrada);
		return outTesoreria;
	}
	
	private CrearComprobante.CrearComprobante_Salida crearComprobante(Connection con, CrearIngresoEgreso_Salida outTesoreria, Long idEmpresa, Long idUsuario, String fechaSistema, String codigoPortafolioCargo) throws Exception {
		if(!("").equals(codigoPortafolioCargo)){
			return new CrearComprobante.CrearComprobante_Salida();
		}
		List<CrearComprobante.IngresoEgresoGenerar_Entrada> paramsComprobanteIngresoEgreso_Entrada = new ArrayList<CrearComprobante.IngresoEgresoGenerar_Entrada>();
		
		for (CrearIngresoEgreso.IngresoEgresoGenerado_Salida outIngresoEgreso : outTesoreria.P_OUT_INGRESOEGRESOGENERADO) {
			CrearComprobante.IngresoEgresoGenerar_Entrada ComprobanteIngresoEgreso_Entrada = new CrearComprobante.IngresoEgresoGenerar_Entrada();

			ComprobanteIngresoEgreso_Entrada.NumeroReferencia = 0L;
			ComprobanteIngresoEgreso_Entrada.IdIngresoEgreso = outIngresoEgreso.IdIngresoEgreso;
			ComprobanteIngresoEgreso_Entrada.TipoComprobante = "";

			paramsComprobanteIngresoEgreso_Entrada.add(ComprobanteIngresoEgreso_Entrada);
		}
	
		// Llena info de comprobante a liquidar
		CrearComprobante.CrearComprobante_Entrada paramsComprobante_Entrada = new CrearComprobante.CrearComprobante_Entrada();
		List<CrearComprobante.ComprobanteGenerar_Entrada> paramsComprobanteGenerar_Entrada = new ArrayList<CrearComprobante.ComprobanteGenerar_Entrada>();
		List<CrearComprobante.FormaDePago_Entrada> paramsComprobanteFormaDePago_Entrada = new ArrayList<CrearComprobante.FormaDePago_Entrada>();
		
		paramsComprobante_Entrada.p_IdEmpresa = idEmpresa;
		paramsComprobante_Entrada.p_IdUsuario = idUsuario;
		paramsComprobante_Entrada.p_IdModulo = "TES";
		paramsComprobante_Entrada.p_UsaReglasNegocio = "N";
		paramsComprobante_Entrada.p_FechaMovimiento = fechaSistema;
		paramsComprobante_Entrada.p_IdCaja = 0L;
		paramsComprobante_Entrada.p_GenerarComprobanteDesdeMovimientos = "S";
		paramsComprobante_Entrada.p_ComprobanteGenerar = paramsComprobanteGenerar_Entrada;
		paramsComprobante_Entrada.p_IngresoEgresoGenerar = paramsComprobanteIngresoEgreso_Entrada;
		paramsComprobante_Entrada.p_FormaDePago = paramsComprobanteFormaDePago_Entrada;

		CrearComprobante ProcesoCrearComprobante = new CrearComprobante();

		CrearComprobante.CrearComprobante_Salida outComprobante = ProcesoCrearComprobante.logica(con, paramsComprobante_Entrada);
		
		return outComprobante;
	}
	
	private LiquidarComprobante_Salida liquidarComprobante(Connection con, CrearComprobante.CrearComprobante_Salida outComprobante, Long idEmpresa, Long idUsuario, String fechaSistema, String codigoPortafolioCargo) throws Exception {
		if(!("").equals(codigoPortafolioCargo)){
			return new LiquidarComprobante_Salida();
		}
		// Llena lista de comprobantes a liquidar
		LiquidarComprobante.ComprobanteLiquidar_Entrada ComprobanteLiquidar_Entrada = new LiquidarComprobante.ComprobanteLiquidar_Entrada();

		LiquidarComprobante_Entrada liquidarComprobanteEntrada = new LiquidarComprobante_Entrada();
		
		liquidarComprobanteEntrada.p_IdEmpresa = idEmpresa;
		liquidarComprobanteEntrada.p_IdUsuario = idUsuario;
		liquidarComprobanteEntrada.p_IdModulo = "TES";
		liquidarComprobanteEntrada.p_SimularOperacion = "N";
		liquidarComprobanteEntrada.p_UsaReglasNegocio = "S";
		liquidarComprobanteEntrada.p_FechaMovimiento = fechaSistema;
		
		
		List<LiquidarComprobante.ComprobanteLiquidar_Entrada> paramsComprobanteLiquidar_Entrada = new ArrayList<LiquidarComprobante.ComprobanteLiquidar_Entrada>();
		// Por cada comprobante ingresado recorre para
		// liquidarlo
		for (int i = 0; i < outComprobante.P_OUT_RESULTADOPROCESO.size(); i++) {
			// Mientras recorre valida que el
			// comprobante sea el mismo en la lista
			if (outComprobante.P_OUT_RESULTADOPROCESO.get(i).Campo.equals("IdComprobante")) {
				ComprobanteLiquidar_Entrada.IdComprobante = outComprobante.P_OUT_RESULTADOPROCESO.get(i).Id;
				ComprobanteLiquidar_Entrada.IdTipoMovimiento = "MOVING";
				ComprobanteLiquidar_Entrada.AfectaCuentaInversion = "CLI";
				paramsComprobanteLiquidar_Entrada.add(ComprobanteLiquidar_Entrada);
			}
		}

		liquidarComprobanteEntrada.p_ComprobanteLiquidar = paramsComprobanteLiquidar_Entrada;

		liquidarComprobanteEntrada.FolioCSC = FolioComprobante;
		LiquidarComprobante liquidarComprobante = new LiquidarComprobante();
		LiquidarComprobante_Salida liquidarComprobante_Salida = liquidarComprobante.logica(con,liquidarComprobanteEntrada);
		
		return liquidarComprobante_Salida;
	}
	
	private CrearAporteCSC_Salida validaClienteBloqueado(Connection con, Long idInterno, Long idPortafolio, Long idEmpresa) throws Exception {
		CrearAporteCSC_Salida out = new CrearAporteCSC_Salida();
		
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("IdPortafolio", idPortafolio);
		SQL sql = new SQL();
		
		ClientePortafolioSolicitudes cliente= sql.query(con, SQL_SELECT_PORTAFOLIO, params, ClientePortafolioSolicitudes.class).stream().findFirst().orElse(null);
		
		if(cliente != null && !_c.EstadoActivo.equals(cliente.IdEstadoCliente) && _c.NO.equals(cliente.EsClienteNuevoProspecto)){
			List<MensajesErrores> listaMensajesErrores = new ArrayList<MensajesErrores>();
			MensajesErrores mensajesErrores = new MensajesErrores();
			
			HashMap<String,String> parametros = new HashMap<String,String>();
			parametros.put("DocumentoIdentidad", cliente.DocumentoIdentidad);

			ResultadoOperacion objResultadoOperacion = new ResultadoOperacion();

			objResultadoOperacion.Origen = "CSC";
			objResultadoOperacion.Identificador = idInterno.toString();

			mensajesErrores.IdInterno = idInterno;
			mensajesErrores.TipoMensaje = MensajeSalida.MENSAJE_ERROR;
			mensajesErrores.Codigo = "CLIENTE_BLOQUEADO";
			mensajesErrores.Mensaje = Diccionario.getMensaje(con, idEmpresa, mensajesErrores.Codigo, parametros);

			listaMensajesErrores.add(mensajesErrores);
			objResultadoOperacion.Mensajes = listaMensajesErrores;
			out.P_OUT_RESULTADO.add(objResultadoOperacion);
			out.P_OUT_ERRORESCONTADOR++;
		}
		
		if(cliente != null && cliente.BloqueadoPorCumplimiento.equalsIgnoreCase("S") && _c.NO.equals(cliente.EsClienteNuevoProspecto)){
			List<MensajesErrores> listaMensajesErrores = new ArrayList<MensajesErrores>();
			MensajesErrores mensajesErrores = new MensajesErrores();
			
			HashMap<String,String> parametros = new HashMap<String,String>();
			parametros.put("DocumentoIdentidad", cliente.DocumentoIdentidad);
			parametros.put("NombreCliente",cliente.NombreCompleto);
			
			ResultadoOperacion objResultadoOperacion = new ResultadoOperacion();

			objResultadoOperacion.Origen = "CSC";
			objResultadoOperacion.Identificador = idInterno.toString();

			mensajesErrores.IdInterno = idInterno;
			mensajesErrores.TipoMensaje = MensajeSalida.MENSAJE_ERROR;
			mensajesErrores.Codigo = "CLIENTE_BLOQUEADO_POR_CUMPLIMIENTO";
			mensajesErrores.Mensaje = Diccionario.getMensaje(con, idEmpresa, mensajesErrores.Codigo, parametros);

			listaMensajesErrores.add(mensajesErrores);
			objResultadoOperacion.Mensajes = listaMensajesErrores;
			out.P_OUT_RESULTADO.add(objResultadoOperacion);
			out.P_OUT_ERRORESCONTADOR++;
		}

		return out;
	}
	
	private CrearAporteCSC_Salida validaMonedaDistribucionDolar(Connection con, List<DistribucionDeFondo> distribuciones, HashMap<String, Object> series, Long idEmpresa, Long idInterno) throws Exception {
		CrearAporteCSC_Salida out = new CrearAporteCSC_Salida();
		
		for(DistribucionDeFondo distribucion : distribuciones) {
			SerieFondo serieFondo = (SerieFondo) series.get(distribucion.IdSerieFondo);
			if(serieFondo != null && !_c.Dolar.equals(serieFondo.IdMoneda)) {
				List<MensajesErrores> listaMensajesErrores = new ArrayList<MensajesErrores>();
				MensajesErrores mensajesErrores = new MensajesErrores();
				ResultadoOperacion objResultadoOperacion = new ResultadoOperacion();

				objResultadoOperacion.Origen = "CSC";
				objResultadoOperacion.Identificador = idInterno.toString();
				mensajesErrores.IdInterno = idInterno;
				mensajesErrores.TipoMensaje = MensajeSalida.MENSAJE_ERROR;
				mensajesErrores.Codigo = "SERIE_FONDO_NO_ES_DOLAR";
				HashMap<String,String> parametros = new HashMap<String,String>();
				parametros.put("IdSerieFondo", distribucion.IdSerieFondo);
				mensajesErrores.Mensaje = Diccionario.getMensaje(con, idEmpresa, mensajesErrores.Codigo, parametros);
				listaMensajesErrores.add(mensajesErrores);
				objResultadoOperacion.Mensajes = listaMensajesErrores;
				out.P_OUT_RESULTADO.add(objResultadoOperacion);
				out.P_OUT_ERRORESCONTADOR++;
			}
		}
		return out;
	}
	
	private String obtenerMoneda(FondosMutuos entrada) {
		String moneda = _c.Pesos;
		if(entrada.MontoUSD.compareTo(BigDecimal.ZERO) == 1) {
			moneda = _c.Dolar;
		}
		return moneda;
	}
	
	private BigDecimal obtenerMonto(FondosMutuos entrada) {
		BigDecimal monto = entrada.MontoPesos;
		if(entrada.MontoUSD.compareTo(BigDecimal.ZERO) == 1) {
			monto = entrada.MontoUSD;
		}
		return monto;
	}
	
	// -------------------------------------------------------------------------------------------
	// OBJETO DE TRANSPORTE - CALL
	// -------------------------------------------------------------------------------------------
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		CrearAporteCSC_Entrada jin = gson.fromJson(in.toString(), CrearAporteCSC_Entrada.class);
		CrearAporteCSC_Salida out = logica(conn, jin);

		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	// -------------------------------------------------
	// METODO PARA PRUEBAS LOCALES
	// -------------------------------------------------
	public static void main(String[] args) throws Exception {
		System.out.println("== INICIO PRUEBA LOCAL ==");

		try (Connection con = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();

			String JsonMaster02 = "{\r\n"
					+ "	\"p_IdEmpresa\":1,\r\n"
					+ "	\"p_IdUsuario\":\"1022\",\r\n"
					+ "	\"p_ClaveUsuario\":\"51923006DAFACDCC56CAE976382E1AA3D84408465C3FC455D633DA0B198A71A5\",\r\n"
					+ "	\"p_FondosMutuos\":[\r\n"
					+ "		{\r\n"
					+ "			\"IdInterno\":0,\r\n"
					+ "			\"Simula\":\"S\",\r\n"
					+ "			\"IdCuentaBanco\":2978,\r\n"
					+ "			\"IdBanco\":21,\r\n"
					+ "			\"CuentaCorriente\":\"1316400\",\r\n"
					+ "			\"FolioCSC\":\"W\",\r\n"
					+ "			\"TipoPago\":\"TRA\",\r\n"
					+ "			\"DocumentoIdentidad\":\"17323241-1\",\r\n"
					+ "			\"CodigoPortafolio\":\"110\",\r\n"
					+ "			\"CodigoPortafolioCargo\":\"0\",\r\n"
					+ "			\"MontoAGF\":0,\r\n"
					+ "			\"Canal\":\"WEB\",\r\n"
					+ "			\"IdCliente\":78295,\r\n"
					+ "			\"p_DistribucionDe Fondo\":[\r\n"
					+ "				{\r\n"
					+ "					\"IdSerieFondo\":\"00382ACLP\",\r\n"
					+ "					\"Monto\":5000,\r\n"
					+ "					\"Regimen\":\"18Q\"\r\n"
					+ "				},\r\n"
					+ "				{\r\n"
					+ "					\"IdSerieFondo\":\"00167ACLP\",\r\n"
					+ "					\"Monto\":5000,\r\n"
					+ "					\"Regimen\":\"18Q\"\r\n"
					+ "				},\r\n"
					+ "				{\r\n"
					+ "					\"IdSerieFondo\":\"00038ACLP\",\r\n"
					+ "					\"Monto\":5000,\r\n"
					+ "					\"Regimen\":\"18Q\"\r\n"
					+ "				}\r\n"
					+ "			],\r\n"
					+ "			\"IdPortafolio\":464260,\r\n"
					+ "			\"MontoUSD\":0,\r\n"
					+ "			\"MontoPesos\":15000,\r\n"
					+ "			\"MontoUF\":0\r\n"
					+ "		}\r\n"
					+ "	]\r\n"
					+ "}";
			String jsonConPortafolio = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":1,\"p_ClaveUsuario\":\"8467f1576b1c1fe18fe3f6e0b06b3ab155f251d177d16b10a66dcf733273663f\",\"p_FondosMutuos\":[{\"Simula\":\"N\",\"IdBanco\":21,\"CuentaCorriente\":\"1316400\",\"FolioCSC\":\"000044556132223\",\"Monto\":30000000,\"Moneda\":\"CLP\",\"TipoPago\":\"CHE\",\"DocumentoIdentidad\":\"96757870-3\",\"CodigoPortafolio\":\"110\",\"Canal\":\"EJE\",\"IdCliente\":63450,\"p_DistribucionDeFondo\":[{\"IdSerieFondo\":\"00039DCLP\",\"Monto\":30000000,\"Regimen\":\"18Q\"}],\"IdPortafolio\":248367,\"MontoPesos\":30000000,\"MontoUF\":0,\"MontoUSD\":0}]}";
			CrearAporteCSC_Entrada Objin = gson.fromJson(jsonConPortafolio, CrearAporteCSC_Entrada.class);

			CrearAporteCSC demo = new CrearAporteCSC();
			System.out.println("ENTRADA");
			System.out.println(Objin.toString());

			try {
				CrearAporteCSC_Salida ObjOut = demo.logica(con, Objin);
				con.rollback();
				System.out.println("SALIDA");
				System.out.println(ObjOut.toString());
			} catch (Exception er) {
				er.printStackTrace();
				con.rollback();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		System.out.println("== FIN PRUEBA LOCAL ==");
	}
}