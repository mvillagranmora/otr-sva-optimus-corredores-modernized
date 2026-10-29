package cl.bcs.mesa.MRF;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.mesa.MES.CrearComprobantePagoOperacion;
import cl.bcs.mesa.MES.CrearComprobantePagoOperacion.ComprobantePagoOperacionObj_IN;
import cl.bcs.mesa.MES.CrearComprobantePagoOperacion.CrearComprobantePagoOperacion_IN;
import cl.bcs.mesa.MES.CrearComprobantePagoOperacion.CrearComprobantePagoOperacion_OUT;
import cl.bcs.mesa.MRF.CrearOperacionMesa.CrearOperacionMesa_DetalleOperacion_OUT;
import cl.bcs.mesa.MRF.CrearOperacionMesa.CrearOperacionMesa_FormaPagoOperacion_OUT;
import cl.bcs.mesa.MRF.CrearOperacionMesa.CrearOperacionMesa_Operacion_OUT;
import cl.bcs.mesa.MRF.IngresarDetalleCompraVentaPacto.IngresarDetalleCompraVentaPacto_DetalleOperacion_IN;
import cl.bcs.mesa.MRF.IngresarDetalleCompraVentaPacto.IngresarDetalleCompraVentaPacto_IN;
import cl.bcs.mesa.MRF.IngresarDetalleCompraVentaPacto.IngresarDetalleCompraVentaPacto_OUT;
import cl.bcs.mesa.MRF.IngresarDetalleCompraVentaPacto.IngresarDetalleCompraVentaPacto_Operacion_IN;
import cl.bcs.mesa.MRF.IngresarDetalleCompraVentaTermino.IngresarDetalleCompraVentaTermino_DetalleOperacion_IN;
import cl.bcs.mesa.MRF.IngresarDetalleCompraVentaTermino.IngresarDetalleCompraVentaTermino_IN;
import cl.bcs.mesa.MRF.IngresarDetalleCompraVentaTermino.IngresarDetalleCompraVentaTermino_OUT;
import cl.bcs.mesa.MRF.IngresarDetalleCompraVentaTermino.IngresarDetalleCompraVentaTermino_Operacion_IN;
import cl.bcs.mesa.MRF.ConsultarCarteraIngresoOperacionMesa.DetalleConsulta;
import cl.bcs.mesa.MRF.ConsultarCarteraIngresoOperacionMesa.EntradaConsultarCarteraIngresoOperacionMesa;
import cl.bcs.mesa.MRF.ConsultarCarteraIngresoOperacionMesa.SalidaConsultarCarteraIngresoOperacionMesa;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.InsertarLogServicio;
import cl.bcs.plataforma.pla.InsertarLogServicio.RegistroLog;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente.FechasIn;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.OutObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class IngresarDetalleOperacionMesa implements JavaCall {

	private static final Logger logger = LogManager.getLogger(IngresarDetalleOperacionMesa.class);

	private final String SQL_SELECT_CONCEPTOS_MESA		= loadResourceToString("cl/bcs/mesa/sql/ConsultarConceptosMesa.sql");
	private final String SQL_SELECT_FORMAS_DE_PAGO_MESA = loadResourceToString("cl/bcs/mesa/sql/ConsultarFormasDePagoMesa.sql");
	private final String SQL_SELECT_TIPO_MOVIMIENTO 	= loadResourceToString("cl/bcs/mesa/sql/ConsultarTipoMovimientoMesa.sql");
	
	private final String SQL_SELECT_PREFERENCIA_PAGO_PORTAFOLIO = loadResourceToString("cl/bcs/mesa/MRF/sql/IngresarDetalleOperacionMesa_SelectFormaPagoPortafolio.sql");
	private final String SQL_SELECT_CLIENTE 			  = loadResourceToString("cl/bcs/mesa/MRF/sql/IngresarDetalleOperacionMesa_SelectCliente.sql");
	private final String SQL_SELECT_SUCURSAL_EMPLEADO 	  = loadResourceToString("cl/bcs/mesa/MRF/sql/IngresarDetalleOperacionMesa_SelectSucursalEmpleado.sql");
	private final String SQL_SELECT_ID_CARTERA_RESERVA    = loadResourceToString("cl/bcs/mesa/MRF/sql/IngresarDetalleOperacionMesa_SelectIdCartera.sql");
	private final String SQL_SELECT_PARAMETROS_CONFIGURACION = loadResourceToString("cl/bcs/mesa/MRF/sql/IngresarDetalleOperacionMesa_SelectParametroConfiguracion.sql");
	
	private final String SQL_SELECT_BLOQUEO_CARTERAS = loadResourceToString("cl/bcs/mesa/MRF/sql/IngresarDetalleOperacionMesa_SelectBloqueoCarteras.sql");
	
	//Consultar Formas de pago por defecto
	private final String SQL_DELETE_FORMAS_DE_PAGO_DEFECTO = loadResourceToString("cl/bcs/mesa/MRF/sql/IngresarDetalleOperacionMesa_DeleteFormasDePagoDefecto.sql");
	private final String SQL_SELECT_FORMAS_DE_PAGO_DEFECTO_CORREDOR = loadResourceToString("cl/bcs/mesa/MRF/sql/IngresarDetalleOperacionMesa_SelectFormasDePagoDefecto.sql");
	private final String SQL_SELECT_FORMAS_DE_PAGO_DEFECTO_CLIENTE = loadResourceToString("cl/bcs/mesa/MRF/sql/IngresarDetalleOperacionMesa_SelectFormasDePagoDefectoCliente.sql");
	
	// --------------------------------------------------------------
	// CLASES DE ENTRADA
	// --------------------------------------------------------------
	public static class IngresarDetalleOperacionMesa_Operacion_IN extends BcsPojo {
		public BigDecimal 	TasaAnualizada;
		public String	 	CostoFondo;
		public BigDecimal 	UtilidadPerdida;
		public BigDecimal 	UtilidadFinal;
		public String Accion;
		public Long IdInternoOperacion;
		public Long IdOperacion;
		public Long FolioOperacion;
		public Long FolioCartera;// para pactos y depositos y desembolso
		public Long IdCliente;
		public Long IdPortafolio;
		public Long IdClienteCorredor;
		public Long IdPortafolioCorredor;
		public String Intermesa;
		public Long IdMesaOrigen;
		public Long IdMesaDestino;
		public String AfectaCustodia;
		public String IdTipoProducto;// 'MERF'
		public String IdTipoOperacion;// COMTER,VENTER,COMPAC,VENPAC
		public String IdTipoReajuste;// "USD","UF"
		public Long Plazo;
		public BigDecimal Monto;
		public BigDecimal MontoMoneda;
		public String IdMoneda;// CLP "USD","UF" moneda local
		public String IdMonedaLiquidacion; // CLP "USD","UF"
		public BigDecimal MontoLiquidacion;
		public BigDecimal Tasa;
		public BigDecimal ValorFinal;
		public BigDecimal ValorFinalMoneda;
		public BigDecimal TasaAgente;
		public String FechaOperacion;
		public String FechaVencimiento;
		public String IdCondicionLiquidacion;
		public String FechaLiquidacion;
		public Long IdAgente;
		public Long IdOperador;
		public String IdEstado;
		public String Glosa;
		public String IdTipoCanalIngreso;
		public String TipoDocumento;
		public Long IdContacto;
		// Nuevo deposito desembolso
		public String IdEmisor = "";
	}

	public static class IngresarDetalleOperacionMesa_DetalleOperacion_IN extends BcsPojo{
		public Long IdInternoDetalle;
		public Long IdInternoOperacion;
		public Long IdReserva;
		public Long IdCliente;
		public Long IdPortafolio;
		public Long IdClienteCorredor;
		public Long IdPortafolioCorredor;
		public Long IdMesa;
		public String IdTipoProducto; // 'MERF'
		public String IdTipoOperacion; // COMTER,VENTER,COMPAC,VENPAC
		public Long IdInstrumento;
		public String Nemotecnico;
		public String EsFungible;
		public String IdBolsa;
		public String IdBoveda;
		public Long IdCartera;
		public Long FolioCartera;
		public String CodigoIFRS;
		public String IdEmisor;
		public String IdMoneda;
		public String IdTipoReajuste;
		public String IdMonedaEmisionInstrumento;
		public String IdMonedaLiquidacionInstrumento;
		public String IdMonedaLiquidacionOperacion;
		public BigDecimal CorteMinimo;
		public BigDecimal Cantidad;
		public BigDecimal Precio;
		public BigDecimal PrecioMercado;
		public BigDecimal Tasa;
		public BigDecimal TasaMercado;
		public BigDecimal Monto;
		public BigDecimal MontoLiquidacion;
		public BigDecimal MontoLiquidacionOperacion;
		public BigDecimal ValorRescate;
		public String FechaVencimiento;
		public BigDecimal MontoValorizadoReserva;
		public String IdClasificacionTipoInstrumento;
		public String TipoCarteraCorredor;
		public BigDecimal MontoFinalCarteraMonedaSistema;
		public BigDecimal MontoFinalCarteraMonedaInstrumento;
		public BigDecimal INGMontoAPlazoDetalle;
		public BigDecimal INGMontoAPlazoMonedaInstrumentoDetalle;
	};

	public static class IngresarDetalleOperacionMesa_FormaPagoOperacion_IN extends BcsPojo{
		public Long IdInternoPagoOperacion;
		public Long IdInternoOperacion;
		public Long IdComprobantePagoOperacion;
		public String IdFormaDePago;
		public String IdMoneda;
		public Long IdOperacion;
		public String RecibePagaPortafolio;
		public BigDecimal MontoFormaDePago;
		public String AfectarTesoreria;
		public Long IdCuentaBancaria;
		public boolean EsCliente;
		public String FechaLiquidacion;
		public String NombreBanco;
		public String NombreBancoTarjeta;
		public String NumeroTarjeta;
		public String CuentaBancaria;
	};

	public static class IngresarDetalleOperacionMesa_IN extends BcsPojo{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public String p_OrigenProceso;
		public List<IngresarDetalleOperacionMesa_Operacion_IN> p_Operacion;
		public List<IngresarDetalleOperacionMesa_DetalleOperacion_IN> p_DetalleOperacion;
		public List<IngresarDetalleOperacionMesa_FormaPagoOperacion_IN> p_FormasDePago;

		public IngresarDetalleOperacionMesa_IN() {
			this.p_OrigenProceso = "";
			this.p_Operacion = new ArrayList<IngresarDetalleOperacionMesa_Operacion_IN>();
			this.p_DetalleOperacion = new ArrayList<IngresarDetalleOperacionMesa_DetalleOperacion_IN>();
			this.p_FormasDePago = new ArrayList<IngresarDetalleOperacionMesa_FormaPagoOperacion_IN>();
		}

	};
	
	public static class VerificarCartera  extends BcsPojo  {
		public Long FolioCartera;
		public Long IdCartera;
		public String IdEstado;
		
	}

	// --------------------------------------------------------------
	// CLASES DE SALIDA
	// --------------------------------------------------------------
	public static class IngresarDetalleOperacionMesa_Operacion_OUT extends BcsPojo{
		public String Accion;
		public Long IdInternoOperacion;
		public Long IdOperacion;
		public Long FolioOperacion;
		public Long IdCliente;
		public Long IdPortafolio;
		public Long IdClienteCorredor;
		public Long IdPortafolioCorredor;
		public Long IdOperacionRelacionada;
		public String Intermesa;
		public Long IdMesaOrigen;
		public Long IdMesaDestino;
		public String AfectaCustodia;
		public String IdTipoProducto; // 'MERF'
		public String IdTipoOperacion; // COMTER,VENTER,COMPAC,VENPAC
		public String IdTipoReajuste; // "USD","UF"
		public Long Plazo;
		public BigDecimal Monto;
		public BigDecimal MontoMoneda;
		public String IdMoneda;// CLP "USD","UF" moneda local
		public String IdMonedaLiquidacion; // CLP "USD","UF"
		public BigDecimal Tasa;
		public BigDecimal ValorFinal;
		public BigDecimal ValorFinalMoneda;
		public BigDecimal TasaAgente;
		public Date FechaOperacion;
		public Date FechaVencimiento;
		public String IdCondicionLiquidacion;
		public Date FechaLiquidacion;
		public Long IdAgente;
		public Long IdOperador;
		public String IdEstado;
		public String NombreEstado;
		public String Glosa;
		public String IdTipoCanalIngreso;
		public String confPacto;
		// Nuevo deposito desembolso
		public String IdEmisor;
	};

	public static class IngresarDetalleOperacionMesa_DetalleOperacion_OUT extends BcsPojo{
		public Long IdInternoDetalle;
		public Long IdInternoOperacion;
		public Long IdReserva;
		public Long IdCliente;
		public Long IdPortafolio;
		public Long IdClienteCorredor;
		public Long IdPortafolioCorredor;
		public Long IdMesa;
		public String IdTipoProducto;// 'MERF'
		public String IdTipoOperacion;// COMTER,VENTER,COMPAC,VENPAC
		public Long IdInstrumento;
		public String EsFungible;
		public String IdBolsa;
		public String IdBoveda;
		public Long FolioCartera;
		public String CodigoIFRS;
		public String IdEmisor;
		public String IdTipoReajuste;
		public String IdMonedaEmisionInstrumento;
		public String IdMonedaLiquidacionInstrumento;
		public String IdMonedaLiquidacionOperacion;
		public BigDecimal Cantidad;
		public BigDecimal Precio;
		public BigDecimal Tasa;
		public BigDecimal Monto;
		public BigDecimal MontoLiquidacion;
		public BigDecimal MontoLiquidacionOperacion;
		public BigDecimal ValorRescate;
		public Date FechaVencimiento;
		public String confPacto;
		public String TipoCarteraCorredor;
	};

	public static class IngresarDetalleOperacionMesa_FormaPagoOperacion_OUT extends BcsPojo{
		public Long IdInternoPagoOperacion;
		public Long IdComprobantePagoOperacion;
		public String IdFormaDePago;
		public String IdMoneda;
		public Long IdOperacion;
		public String RecibePagaPortafolio;
		public BigDecimal MontoFormaDePago;
	};

	public static class IngresarDetalleOperacionMesa_OUT extends BcsPojo {
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR;

		public List<CrearOperacionMesa_Operacion_OUT> P_OUT_OPERACION;
		public List<CrearOperacionMesa_DetalleOperacion_OUT> P_OUT_DETALLEOPERACION;
		public List<CrearOperacionMesa_FormaPagoOperacion_OUT> P_OUT_FORMASDEPAGO;

		public IngresarDetalleOperacionMesa_OUT() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_OPERACION = new ArrayList<CrearOperacionMesa_Operacion_OUT>();
			this.P_OUT_DETALLEOPERACION = new ArrayList<CrearOperacionMesa_DetalleOperacion_OUT>();
			this.P_OUT_FORMASDEPAGO = new ArrayList<CrearOperacionMesa_FormaPagoOperacion_OUT>();
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
	}

	// --------------------------------------------------------------
	// CLASES COMPLEMENTARIAS
	// --------------------------------------------------------------

	public static class FormaDePagoMesa extends BcsPojo {
		public String IdFormaDePago;
		public String NombreFormaDePago;
		public Long Dias;
		public String PorDefecto;
		public String RecibePagaPortafolioDesc;
		public String IdEstado;
		public String NombreEstado;
		public String IdPreferenciaPago;
		public String IdTipoOperacion;
		public String IdMedioDePago;
		public String IdMoneda;
		public String UsaPersonaRelacionada;
		public Date FechaCreacion;
		public Date FechaModificacion;
		public Long IdUsuarioCreador;
		public Long IdEmpresa;
		public String NombreTipoOperacion;
		public String IdTipoProducto;
		public String NombreTipoProducto;
		public String RecibePagaPortafolio;
		public String NombreMedioDePago;
		public String NombreMoneda;
		public String UsaCuentaBancaria;
		public String UsaTarjetaBancaria;
		public String IdEstadoPreferenciaPago;
		public String NombreEstadoPreferenciaPago;
		public String IdModalidadDePago;
	}

	public static class TipoMovimientoMesa extends BcsPojo{
		public String TipoMov;
		public String RecibePaga;
		public String IdTipoMovimiento;
	}

	public static class Concepto extends BcsPojo{
		public String IdConcepto;
		public String NombreConcepto;
		public String IdTipoProducto;
		public String IdTipoOperacion;
		public String Uso;
		public String IdConceptoRelacionado;
		public String NombreConceptoRelacionado;
	}

	public static class ConfiguracionMesa extends BcsPojo{
		public Long IdEmpresa;
		public String IdTipoProducto;
		public String CodigoParametro;
		public String Descripcion;
		public String Valor;
	}

	public static class Sucursal extends BcsPojo{
		public Long IdSucursal;
		public Long IdEmpleado;
		public Long IdUsuario;
	}

	public static class Persona extends BcsPojo{
		public Long IdCliente;
		public Long IdPersona;
		public Long IdEmpresa;
		public String IdEstado;
		public String DocumentoIdentidad = "";
	}

	public static class ModalidadDePago extends BcsPojo{
		public Long IdMedioDePagoYModalidadDePago;
		public String IdMedioDePago;
		public String IdModalidadDePago;
		public String IdEstado;
	}

	public static class PreferenciaPagoPortafolio extends BcsPojo{
		public Long IdPortafolio;
		public String IdPreferenciaPago;
		public Long IdCuentaBancaria;
		public Long IdBancoCuentaBancaria;
		public String NombreBancoCuentaBancaria;
		public String NumeroCuentaBancaria;
		public String IdTipoCuentaBancaria;
		public String NombreTipoCuentaBancaria;
		public Long IdTarjetaBancaria;
		public String IdTipoTarjetaBancaria;
		public String NombreBancoTarjetaBancaria;
		public String NumeroTarjetaBancaria;
		public String IdModalidadDePago;
	}

	public static class DatosOperacion extends BcsPojo{
		public Long IdOperacion;
		public String IdEstado;
		public String FechaOperacion;
		public Long IdCliente;
		public Long IdPortafolio;
		public Long IdPortafolioMesa;
		public String IdTipoOperacion;
		public Long PlazoEnDias;
		public Long IdUsuarioCreacion;
		public BigDecimal MontoOperacionInicial;
		public BigDecimal TotalDetalle;
		public BigDecimal TotalPagos;
		public Long FolioOperacion;
		public String NombreTipoOperacion;
		public String Agente;
		public String DocumentoIdentidad;
		public String CodigoPortafolio;
		public String NombreProducto;
		public String IdTipoReajuste;
		public Long IdDireccionElectronica;
		public Long IdPersona;
		public String NombreEstado;
		public BigDecimal MontoOperacionFinal;
		public BigDecimal MontoOperacionMoneda;
		public String FechaVencimiento;
		public BigDecimal Tasa;
	}

	public static class ParametrosConfiguracion extends BcsPojo{
		public String CodigoParametro;
		public String Valor;
	}

	public static class ObjCondicionLiquidacion extends BcsPojo{
		public String IdCondicionDeLiquidacion;
		public int PlazoEnDias;
	}
	
	public static class DatosValidacionFormaPago extends BcsPojo{
		public BigDecimal montoFormaPagoCorregido;
		public String RecibePagoPortafolio;
		public String IdTipoOperacion;
		public String ModificaMonto;
	}

// ------------------------------------------------------------
// LOGICA
// ------------------------------------------------------------

	public IngresarDetalleOperacionMesa_OUT logica(Connection conn, IngresarDetalleOperacionMesa_IN in) throws Exception {
		Long init = System.currentTimeMillis();
		long startTime = System.nanoTime();		
		logger.info("Inicio servicio MRF.IngresarDetalleOperacionMesa");
		Boolean sonFormaPagoDefecto = false;
		InsertarLogServicio insertarLogService = new InsertarLogServicio();

		SQL _sql = new SQL();
		IngresarDetalleOperacionMesa_OUT out = new IngresarDetalleOperacionMesa_OUT();

		Long Id = 0L;

		HashMap<String, String> mapMsg = new HashMap<>();
		if(in.p_FormasDePago.size()==0) {
			in.p_FormasDePago = crearFormaDePagoPorDefecto(conn,in.p_Operacion,in.p_DetalleOperacion);
			sonFormaPagoDefecto= true;
			
		}
		
		ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
		InformacionEmpresa empresa = consultarEmpresaService.logica(conn, in.p_IdEmpresa);
		Long idPaisSistema = empresa.IdPais;

		logger.debug("[IngresarDetalleOperacionMesa] Inicio Obtiene parámetros Configuracion");
		startTime = System.nanoTime();
		String monedaSistema = "", fechaSistema = "";

		HashMap<String, Object> paramCongif = new HashMap<>();
		paramCongif.put("IdEmpresa", in.p_IdEmpresa);
		// No se usa ConfiguracionSistema.getParametroConfiguracionString por
		// rendimiento
		List<ParametrosConfiguracion> lstParametrosConfiguracion = _sql.query(conn, SQL_SELECT_PARAMETROS_CONFIGURACION, paramCongif, ParametrosConfiguracion.class);

		if (lstParametrosConfiguracion.size() > 0){
			for (ParametrosConfiguracion config : lstParametrosConfiguracion){
				if (config.CodigoParametro.equalsIgnoreCase("PLA.MonedaSistema"))       { monedaSistema = config.Valor; }
				if (config.CodigoParametro.equalsIgnoreCase("PLA.FechaSistema"))        { fechaSistema = config.Valor; }
			}
		}
		
		List<String> IdTipoProducto = new ArrayList<String>();
		HashMap<String, Object> paramConcep = new HashMap<>();
		HashMap<Long, IngresarDetalleOperacionMesa_Operacion_IN> v_IdOperacionHash = new HashMap<>();

		// Recorre entrada y asigna variables para consultas
		// --------------------------------------------------------------------------------------

		//HashMap<String, Object> parPrefPor = new HashMap<>();
		//HashMap<String, Object> parPrefPorCor = new HashMap<>();
		HashMap<String, Object> params = new HashMap<>();

		List<Object> pc = new ArrayList<>();
		List<Object> parPre = new ArrayList<>();
		List<Object> parPreCor = new ArrayList<>();

		for (IngresarDetalleOperacionMesa_Operacion_IN ope : in.p_Operacion) {
			pc.add(ope.IdCliente);
			pc.add(ope.IdClienteCorredor);
			IdTipoProducto.add(ope.IdTipoProducto);
			params.put("IdPortafolio", ope.IdPortafolio);

			v_IdOperacionHash.put(ope.IdInternoOperacion, ope);
			parPre.add(ope.IdPortafolio);
			parPreCor.add(ope.IdPortafolioCorredor);
		}

		// Consulta datos Cliente
		HashMap<String, Object> parCli = new HashMap<>();
		parCli.put("IdCliente", pc);
		List<CrearOperacionMesa.Persona> PersonaList = _sql.query(conn, SQL_SELECT_CLIENTE, parCli, CrearOperacionMesa.Persona.class);
		HashMap<String, Object> PersonaHash = Utils.List2Hash(PersonaList, "IdCliente");

		List<RegistroLog> lstDatosLog = new ArrayList<RegistroLog>();
		
		if(lstDatosLog.size() >= 1) {
			insertarLogService.logica(conn,lstDatosLog);
		}	
		// Busca conceptos usados en mesa
		// --------------------------------------------------------------------------------------
		paramConcep.put("IdTipoProducto", "MERF");
		List<CrearOperacionMesa.Concepto> selectConcepto = _sql.query(conn, SQL_SELECT_CONCEPTOS_MESA, paramConcep, CrearOperacionMesa.Concepto.class);
		List<CrearOperacionMesa.Concepto> selectConceptoIngresoOperacion = new ArrayList<>();
		List<CrearOperacionMesa.Concepto> selectConceptoIngresoReservaOperacion = new ArrayList<>();
		List<CrearOperacionMesa.Concepto> selectConceptoRetiroReservaOperacion = new ArrayList<>();
		HashMap<String, Object> selectConceptoIngresoOperacionHash = new HashMap<>();
		HashMap<String, Object> selectConceptoIngresoReservaOperacionHash = new HashMap<>();
		HashMap<String, Object> selectConceptoRetiroReservaOperacionHash = new HashMap<>();
		for (CrearOperacionMesa.Concepto conce : selectConcepto) {
			switch (conce.Uso) {
			case "INGRESO":
				selectConceptoIngresoOperacion.add(conce);
				break;
			case "INGRESO.RESERVA":
				selectConceptoIngresoReservaOperacion.add(conce);
				break;
			case "RETIRO.RESERVA":
				selectConceptoRetiroReservaOperacion.add(conce);
				break;
			default:
				break;
			}
		}
		selectConceptoIngresoOperacionHash = Utils.List2Hash(selectConceptoIngresoOperacion, "IdTipoOperacion");
		selectConceptoIngresoReservaOperacionHash = Utils.List2Hash(selectConceptoIngresoReservaOperacion, "IdTipoOperacion");
		selectConceptoRetiroReservaOperacionHash = Utils.List2Hash(selectConceptoRetiroReservaOperacion, "IdTipoOperacion");

		// Busca TipoMovimiento usados en mesa
		List<TipoMovimientoMesa> tipoMov = _sql.query(conn, SQL_SELECT_TIPO_MOVIMIENTO, new HashMap<>(), TipoMovimientoMesa.class);

		Map<String, String> tipoMovTes = new HashMap<>();
		Map<String, String> tipoMovCtaCli = new HashMap<>();
		Map<String, String> tipoMovCtaCor = new HashMap<>();
		for (TipoMovimientoMesa tipo : tipoMov) {
			switch (tipo.TipoMov) {
			case "TES":
				tipoMovTes.put(tipo.RecibePaga, tipo.IdTipoMovimiento);
				break;
			case "CTAINVCLI":
				tipoMovCtaCli.put(tipo.RecibePaga, tipo.IdTipoMovimiento);
				break;
			case "CTAINVCOR":
				tipoMovCtaCor.put(tipo.RecibePaga, tipo.IdTipoMovimiento);
				break;
			}
		}

		// Busca sucursal empleado
		HashMap<String, Object> parEmp = new HashMap<>();
		parEmp.put("p_IdUsuario", in.p_IdUsuario);
		parEmp.put("p_IdEmpresa", in.p_IdEmpresa);
		List<Sucursal> idSucursal = _sql.query(conn, SQL_SELECT_SUCURSAL_EMPLEADO, parEmp, Sucursal.class);
		
		// Consulta paridad y tipo cambio Forma de pago
		// --------------------------------------------------------------------------------------
		ObtenerDiaSiguiente diaSgte = new ObtenerDiaSiguiente();
		InObtenerDiaSiguiente datDia = new InObtenerDiaSiguiente();
		datDia.p_idPais = idPaisSistema;
		datDia.p_diaHabil = "S";

		OutObtenerDiaSiguiente salDiaSgte = new OutObtenerDiaSiguiente();
		{
			for (IngresarDetalleOperacionMesa_FormaPagoOperacion_IN inComp : in.p_FormasDePago) {
				FechasIn fechas = new FechasIn();
				fechas.fechaInicial = ((IngresarDetalleOperacionMesa_Operacion_IN) v_IdOperacionHash.get(inComp.IdInternoOperacion)).FechaLiquidacion;
				fechas.numeroDias = 0L;
				datDia.p_fechas.add(fechas);
			}
			salDiaSgte = diaSgte.logica(conn, datDia);
		}

		// TODO:: validaciones	
		for (IngresarDetalleOperacionMesa_Operacion_IN ope : in.p_Operacion) {
			if (selectConceptoIngresoOperacionHash.get(ope.IdTipoOperacion) == null) {
				// mapMsg.put("IdOperacion", op.IdOperacion.toString());
				out.P_OUT_MENSAJES.add(new MensajeSalida(Id, MensajeSalida.MENSAJE_ERROR, "SRV_MRF_CONCEPTOS_MESA_NO_CONFIGURADO", mapMsg, conn, in.p_IdEmpresa));
				Id++;
				break;
			}
		}
		for (IngresarDetalleOperacionMesa_DetalleOperacion_IN det : in.p_DetalleOperacion) {
			if (selectConceptoIngresoReservaOperacionHash.get(det.IdTipoOperacion) == null || selectConceptoRetiroReservaOperacionHash.get(det.IdTipoOperacion) == null) {
				// mapMsg.put("IdOperacion", op.IdOperacion.toString());
				out.P_OUT_MENSAJES.add(new MensajeSalida(Id, MensajeSalida.MENSAJE_ERROR, "SRV_MRF_CONCEPTOS_MESA_NO_CONFIGURADO", mapMsg, conn, in.p_IdEmpresa));
				Id++;
				break;
			}
		}
		
		if ("MAN".equalsIgnoreCase(in.p_OrigenProceso))
			validarCustodiaDetalle(conn, in.p_IdEmpresa, in.p_DetalleOperacion, out, _sql);
		
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		if (!out.P_OUT_ERRORESCONTADOR.equals(0L)) {
			return out;
		}

		for (IngresarDetalleOperacionMesa_Operacion_IN ope : in.p_Operacion) {
			String concepto = "";
			switch (ope.IdTipoOperacion) {
			case "COMTER":
			case "VENTER":
				// actualizo valor concepto operacion
				IngresarDetalleCompraVentaTermino_Operacion_IN opeTer = copyPojoOperacionATermino(ope);
				opeTer.Concepto = (CrearOperacionMesa.Concepto) selectConceptoIngresoOperacionHash.get(ope.IdTipoOperacion);
				opeTer.Persona = (CrearOperacionMesa.Persona) PersonaHash.get(ope.IdCliente);
				opeTer.IdEmpleado = idSucursal.get(0).IdEmpleado;
				opeTer.DocumentoIdentidadCorredor = ((CrearOperacionMesa.Persona) PersonaHash.get(ope.IdClienteCorredor)).DocumentoIdentidad;
				
				IngresarDetalleCompraVentaTermino_IN inTer = new IngresarDetalleCompraVentaTermino_IN();
				inTer.p_IdEmpresa = in.p_IdEmpresa;
				inTer.p_IdModulo = in.p_IdModulo;
				inTer.p_IdUsuario = in.p_IdUsuario;
				inTer.p_IdPais = idPaisSistema;
				inTer.p_FechaSistema = fechaSistema;
				inTer.p_IdSucursal = idSucursal.get(0).IdSucursal;
				inTer.p_MonedaSistema = monedaSistema;
				inTer.p_UsaReglasNegocio = in.p_UsaReglasNegocio;
				inTer.p_EsDetalle="S";
				inTer.p_Operacion.add(opeTer);
				
				// actualizo concepto Detalle operacion
				List<IngresarDetalleCompraVentaTermino_DetalleOperacion_IN> opeDet = new ArrayList<>();
				for (IngresarDetalleOperacionMesa_DetalleOperacion_IN det : in.p_DetalleOperacion) {
					IngresarDetalleCompraVentaTermino_DetalleOperacion_IN opeDetTer = copyPojoOperacionATerminoDetalle(det);
					if (opeDetTer.Cantidad.equals(BigDecimal.ZERO)) { // es un retiro de reserva
						concepto = ((CrearOperacionMesa.Concepto) selectConceptoRetiroReservaOperacionHash.get(opeDetTer.IdTipoOperacion)).IdConcepto;
					} else {
						concepto = ((CrearOperacionMesa.Concepto) selectConceptoIngresoReservaOperacionHash.get(opeDetTer.IdTipoOperacion)).IdConcepto;
					}
					opeDetTer.Concepto = concepto;
					opeDet.add(opeDetTer);
				}
				
				inTer.p_DetalleOperacion = opeDet;
						
				List<CrearOperacionMesa_FormaPagoOperacion_OUT>  salidaComprobantesTermino = this.crearComprobanteFormaDePago(conn,in,sonFormaPagoDefecto);
				
				IngresarDetalleCompraVentaTermino CompraVentaTermino = new IngresarDetalleCompraVentaTermino();
				IngresarDetalleCompraVentaTermino_OUT opeTerOut = CompraVentaTermino.logica(conn, inTer);
				logger.debug("[IngresarDetalleOperacionMesa] (" + (System.nanoTime() - startTime) / 1000000 + ") Fin llamada CompraVentaTermino");

				out.P_OUT_MENSAJES.addAll(opeTerOut.P_OUT_MENSAJES);
				out.P_OUT_OPERACION.addAll(opeTerOut.P_OUT_OPERACION);
				out.P_OUT_DETALLEOPERACION.addAll(opeTerOut.P_OUT_DETALLEOPERACION);
				out.P_OUT_FORMASDEPAGO.addAll(salidaComprobantesTermino);

				break;
			case "COMPAC":
			case "VENPAC":
				IngresarDetalleCompraVentaPacto_IN inPac = new IngresarDetalleCompraVentaPacto_IN();
				IngresarDetalleCompraVentaPacto CompraVentaPacto = new IngresarDetalleCompraVentaPacto();
				// actualizo valor concepto operacion
				CrearOperacionMesa.Concepto concepo = (CrearOperacionMesa.Concepto) selectConceptoIngresoOperacionHash.get(ope.IdTipoOperacion);				
				CrearOperacionMesa.Persona persona = (CrearOperacionMesa.Persona) PersonaHash.get(ope.IdCliente);
				String documentoIdentidadCorredor = ((CrearOperacionMesa.Persona) PersonaHash.get(ope.IdClienteCorredor)).DocumentoIdentidad;
				Long idEmpleado = idSucursal.get(0).IdEmpleado;
				
				//crea cabecera
				IngresarDetalleCompraVentaPacto_Operacion_IN opePac = this.crearCabeceraPacto(ope,concepo,persona,documentoIdentidadCorredor,idEmpleado);
				//crea detalles
				List<IngresarDetalleCompraVentaPacto_DetalleOperacion_IN> opePacDetalle=this.crearDetallePacto(in, selectConceptoRetiroReservaOperacionHash, selectConceptoIngresoReservaOperacionHash); 
				//crea formas de pago
				List<CrearOperacionMesa_FormaPagoOperacion_OUT>  salidaComprobantes = this.crearComprobanteFormaDePago(conn,in,sonFormaPagoDefecto);
				
				
				
				inPac.p_IdEmpresa = in.p_IdEmpresa;
				inPac.p_IdModulo = in.p_IdModulo;
				inPac.p_IdUsuario = in.p_IdUsuario;
				inPac.p_IdPais = idPaisSistema;
				inPac.p_FechaSistema = fechaSistema;
				inPac.p_IdSucursal = idSucursal.get(0).IdSucursal;
				inPac.p_MonedaSistema = monedaSistema;
				inPac.p_UsaReglasNegocio = in.p_UsaReglasNegocio;
				inPac.p_Operacion.add(opePac);
				inPac.p_EsDetalle="S";
				
				List<IngresarDetalleCompraVentaPacto_DetalleOperacion_IN> listaDetallesVerificados = this.verificarIdCarteraVigente(conn, opePacDetalle, out, in.p_IdEmpresa, ope.AfectaCustodia );		 	
			
				if (out.P_OUT_ERRORESCONTADOR.compareTo(0L) != 0) { 
					return out;  
				} 
				
				inPac.p_DetalleOperacion.addAll(listaDetallesVerificados);    
				IngresarDetalleCompraVentaPacto_OUT opePacOut = CompraVentaPacto.logica(conn, inPac);
				out.P_OUT_MENSAJES.addAll(opePacOut.P_OUT_MENSAJES);
				out.P_OUT_OPERACION.addAll(opePacOut.P_OUT_OPERACION);
				out.P_OUT_DETALLEOPERACION.addAll(opePacOut.P_OUT_DETALLEOPERACION);
				out.P_OUT_FORMASDEPAGO.addAll(salidaComprobantes);				
				
				break;
			default:
				break;
			}
		}
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		if (!out.P_OUT_ERRORESCONTADOR.equals(0L)) {
			return out;
		}

		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		logger.info("[IngresarDetalleOperacionMesa] Termino del servicio IngresarDetalleOperacionMesa, tiempo: " + (System.currentTimeMillis() - init) + " ms - " + (System.currentTimeMillis() - init) / 1000 + " seg");
		return out;
	};

	

	private List<IngresarDetalleCompraVentaPacto_DetalleOperacion_IN> verificarIdCarteraVigente(Connection conn,List<IngresarDetalleCompraVentaPacto_DetalleOperacion_IN> opePacDetalle, IngresarDetalleOperacionMesa_OUT out, Long idEmpresa, String afectaCustodia) throws Exception {
		logger.debug("INICIO::IngresarDetalleOperacionMesa.verificarIdCarteraVigente");
		long inicioMetodo = System.currentTimeMillis();

		SQL sql = new SQL();

		HashMap<String,Object> params = new HashMap<String,Object>();
		List<Long> listaFolios = opePacDetalle.stream().map(detalle -> detalle.FolioCartera).collect(Collectors.toList());
		params.put("ListaFolioCartera", listaFolios);

		Map<Long,VerificarCartera> mapCarterasVigentes = sql.query(conn, SQL_SELECT_ID_CARTERA_RESERVA, params, VerificarCartera.class)
				.stream().collect(Collectors.toMap(cartera -> cartera.FolioCartera, cartera-> cartera));

				List<IngresarDetalleCompraVentaPacto_DetalleOperacion_IN> detallesActualizados = new ArrayList<>();
				for (IngresarDetalleCompraVentaPacto_DetalleOperacion_IN detalle : opePacDetalle) {
					VerificarCartera carteraVerificada = mapCarterasVigentes.get(detalle.FolioCartera);
					if (detalle.FolioCartera != 0 && carteraVerificada != null && carteraVerificada.IdCartera.compareTo(detalle.IdCartera) != 0) {
						detalle.IdCartera = carteraVerificada.IdCartera;
					}

						if (carteraVerificada == null && afectaCustodia.equals("S") && detalle.IdCartera.compareTo(0L) != 0 ) {
							HashMap<String, String> mapMsg = new HashMap<>();
							mapMsg.put("FolioCartera", detalle.FolioCartera.toString());
							out.P_OUT_MENSAJES.add(new MensajeSalida(1L, MensajeSalida.MENSAJE_ERROR, "ERROR_CARTERA_NO_TIENE_CUSTODIA", mapMsg, conn, idEmpresa));
							out.P_OUT_ERRORESCONTADOR++;
							break;
						} else {
							detallesActualizados.add(detalle);	
						}
					
				}

		long finMetodo = System.currentTimeMillis();
		logger.debug("FIN::IngresarDetalleOperacionMesa.verificarIdCarteraVigente :" , finMetodo-inicioMetodo);

		return detallesActualizados;
	}

	// ------------------------------------------------------------
	// METODOS UTILITARIOS
	// ------------------------------------------------------------
	
	public IngresarDetalleCompraVentaPacto_Operacion_IN crearCabeceraPacto (IngresarDetalleOperacionMesa_Operacion_IN ope,cl.bcs.mesa.MRF.CrearOperacionMesa.Concepto concepto,cl.bcs.mesa.MRF.CrearOperacionMesa.Persona persona ,String documentoIdentidadCorredor ,Long idEmpleado) {
		IngresarDetalleCompraVentaPacto_Operacion_IN opePac = copyPojoOperacionPacto(ope);
		opePac.Concepto = concepto;
		opePac.Persona = persona;
		opePac.DocumentoIdentidadCorredor = documentoIdentidadCorredor;
		opePac.IdEmpleado = idEmpleado;
		opePac.TasaAnualizada 	= ope.TasaAnualizada;
		opePac.CostoFondo	  	= ope.CostoFondo != null ? new BigDecimal(ope.CostoFondo.replace(",",".")): BigDecimal.ZERO;
		opePac.UtilidadPerdida	= ope.UtilidadPerdida;
		opePac.UtilidadFinal	= ope.UtilidadFinal;
		return opePac;
	}
	
	private List<CrearOperacionMesa_FormaPagoOperacion_OUT>  crearComprobanteFormaDePago(Connection conn,IngresarDetalleOperacionMesa_IN in,Boolean sonFormaPagoDefecto) throws Exception {
		logger.debug("INICIO::IngresarDetalleOperacionMesa.crearComprobanteFormaDePago");
		long inicioMetodo = System.currentTimeMillis();
		
		List<CrearOperacionMesa_FormaPagoOperacion_OUT> ListaFormaDePagoSalida = new ArrayList();
		CrearComprobantePagoOperacion_OUT comprobanteSalida = new CrearComprobantePagoOperacion_OUT();		
		CrearComprobantePagoOperacion_IN  comprobanteEntrada = new CrearComprobantePagoOperacion_IN();
		CrearComprobantePagoOperacion comprobantes = new CrearComprobantePagoOperacion();
		List<ComprobantePagoOperacionObj_IN> ListaformaDePago = new ArrayList();
		 
		comprobanteEntrada.p_IdEmpresa= in.p_IdEmpresa;
		comprobanteEntrada.p_IdModulo= in.p_IdModulo;
		comprobanteEntrada.p_IdUsuario= in.p_IdUsuario;
		comprobanteEntrada.p_UsaReglasNegocio="N";
		comprobanteEntrada.p_FormaPagoNueva=sonFormaPagoDefecto?"S":"N";
		comprobanteEntrada.p_ActualizaFormaPago=sonFormaPagoDefecto?"N":"S";		
		
		in.p_FormasDePago.forEach( formaPago -> {
			ComprobantePagoOperacionObj_IN fpIngreso = new ComprobantePagoOperacionObj_IN();
			fpIngreso.IdInternoPagoOperacion = formaPago.IdInternoOperacion;
			fpIngreso.IdComprobantePagoOperacion=formaPago.IdComprobantePagoOperacion;
			fpIngreso.IdEmpresa= in.p_IdEmpresa;
			fpIngreso.IdOperacion= formaPago.IdOperacion;
			fpIngreso.IdIngresoEgreso= 0L;
			fpIngreso.IdAbono= 0L;
			fpIngreso.IdCargo= 0L;
			fpIngreso.IdIngresoEgresoAnticipo= 0L;
			fpIngreso.IdAbonoAnticipo= 0L;
			fpIngreso.IdCargoAnticipo= 0L;
			fpIngreso.IdTipoProducto= "MERF";
			fpIngreso.RecibePagaPortafolio= formaPago.RecibePagaPortafolio;
			fpIngreso.IdFormaDePago= formaPago.IdFormaDePago;
			fpIngreso.IdModalidadPago= this.modalidadDePago(formaPago.IdFormaDePago); // buscar validacion
			fpIngreso.IdMoneda= formaPago.IdMoneda;
			fpIngreso.Monto= formaPago.MontoFormaDePago;
			fpIngreso.FechaIngresoComprobantePagoOperacion= formaPago.FechaLiquidacion;
			fpIngreso.FechaLiquidacionComprobantePagoOperacion= formaPago.FechaLiquidacion;
			fpIngreso.IdEstado = "ACT";
			fpIngreso.AfectarTesoreria= formaPago.AfectarTesoreria;
			fpIngreso.IdCuentaBanco= formaPago.IdCuentaBancaria;
			
			ListaformaDePago.add(fpIngreso);
			
		});
		
		
		comprobanteEntrada.p_ComprobantePagoOperacion.addAll(ListaformaDePago);
		comprobanteSalida = comprobantes.logica(conn, comprobanteEntrada);
		
		comprobanteSalida.P_OUT_FORMADEPAGO.forEach(compSalida->{
			CrearOperacionMesa_FormaPagoOperacion_OUT fpSalida = new CrearOperacionMesa_FormaPagoOperacion_OUT();
			fpSalida.IdInternoPagoOperacion= compSalida.IdInternoPagoOperacion;
			fpSalida.IdComprobantePagoOperacion =compSalida.IdComprobantePagoOperacion;
			fpSalida.IdFormaDePago= compSalida.IdFormaDePago;
			fpSalida.IdMoneda= compSalida.IdMoneda;
			fpSalida.IdOperacion= compSalida.IdOperacion;
			fpSalida.RecibePagaPortafolio= compSalida.RecibePaga;
			fpSalida.MontoFormaDePago= compSalida.Monto;
			fpSalida.NombreFormaDePago= compSalida.NombreFormaDePago;
			fpSalida.NombreMoneda= compSalida.IdMoneda;
			fpSalida.IdMedioDePago= compSalida.NombreFormaDePago;
			fpSalida.AfectarTesoreria= compSalida.AfectarTesoreria;
			fpSalida.IdCuentaBancaria= compSalida.IdCuentaBancaria;
			fpSalida.NombreBanco= compSalida.NombreBancoCuentaBancaria;
			fpSalida.CuentaBancaria=compSalida.NumeroCuentaBancaria;
			
		
			ListaFormaDePagoSalida.add(fpSalida);
			
		});
	
		long finMetodo = System.currentTimeMillis();
		logger.debug("FIN::IngresarDetalleOperacionMesa.crearComprobanteFormaDePago :" , finMetodo-inicioMetodo);
		return ListaFormaDePagoSalida;
		
	}
	
	private String modalidadDePago(String IdFormaDePago) {
		String modalidad = "MODDEP";
		
		if(IdFormaDePago.contains("CHE") || IdFormaDePago.contains("VVI")||IdFormaDePago.contains("EFE")||IdFormaDePago.contains("EBA")) {
			 modalidad = "MODCAJ";
		}
		return modalidad;
	}
	
	public List<IngresarDetalleCompraVentaPacto_DetalleOperacion_IN> crearDetallePacto (IngresarDetalleOperacionMesa_IN in,HashMap<String, Object> selectConceptoRetiroReservaOperacionHash,HashMap<String, Object> selectConceptoIngresoReservaOperacionHash) {			
		List<IngresarDetalleCompraVentaPacto_DetalleOperacion_IN> opeDetPac = new ArrayList<>();
		String concepto= "";
		
		for (IngresarDetalleOperacionMesa_DetalleOperacion_IN det : in.p_DetalleOperacion) {
			IngresarDetalleCompraVentaPacto_DetalleOperacion_IN opeDetPacto = copyPojoOperacionPactoDetalle(det);
			if (("RF").equals(det.IdClasificacionTipoInstrumento) && opeDetPacto.Cantidad.compareTo(BigDecimal.ZERO)==0  ||("IF").equals(det.IdClasificacionTipoInstrumento) && opeDetPacto.ValorRescate.compareTo(BigDecimal.ZERO)==0 ) { // es un retiro de reserva
				concepto = ((CrearOperacionMesa.Concepto) selectConceptoRetiroReservaOperacionHash.get(opeDetPacto.IdTipoOperacion)).IdConcepto;
			} else {
				concepto = ((CrearOperacionMesa.Concepto) selectConceptoIngresoReservaOperacionHash.get(opeDetPacto.IdTipoOperacion)).IdConcepto;
			}
			opeDetPacto.Concepto = concepto;

			// Se calcula el ValorFinal para COMPAC y VENPAC
			if (opeDetPacto.IdTipoOperacion.contentEquals("COMPAC") || opeDetPacto.IdTipoOperacion.contentEquals("VENPAC")) {	
				BigDecimal uno = new BigDecimal(1);
				BigDecimal cien = new BigDecimal(100);
				BigDecimal plazo = new BigDecimal(in.p_Operacion.get(0).Plazo);
				BigDecimal base = new BigDecimal(30);
				BigDecimal baseNoClp = new BigDecimal(360);
				BigDecimal tasaCien = in.p_Operacion.get(0).Tasa.divide(cien, MathContext.DECIMAL128);
				BigDecimal plazoBase = plazo.divide("CLP".equals(det.IdMonedaLiquidacionOperacion) ? base : baseNoClp, MathContext.DECIMAL128);
				BigDecimal mult = tasaCien.multiply(plazoBase);
				BigDecimal sum = uno.add(mult);
				opeDetPacto.MontoFinalCarteraMonedaSistema = opeDetPacto.Monto.multiply(sum).setScale(0, RoundingMode.HALF_UP);
				opeDetPacto.MontoFinalCarteraMonedaInstrumento = opeDetPacto.MontoLiquidacionOperacion.multiply(sum).setScale(1, RoundingMode.HALF_UP);
				opeDetPacto.ValorFinal = opeDetPacto.Monto.multiply(sum).setScale(0, RoundingMode.HALF_UP);
			}
			opeDetPac.add(opeDetPacto);
		}
		return opeDetPac;
	}
	
	private cl.bcs.mesa.MRF.CrearOperacionMesa.FormaDePagoMesa seleccionaFormaPagoMesa(List<cl.bcs.mesa.MRF.CrearOperacionMesa.FormaDePagoMesa> FormaDePagoMesaList, String preferenciaPago) {
		List<cl.bcs.mesa.MRF.CrearOperacionMesa.FormaDePagoMesa> listaFormaPago = FormaDePagoMesaList.stream().filter(c -> c.IdPreferenciaPago.equals(preferenciaPago)).collect(Collectors.toList());
		cl.bcs.mesa.MRF.CrearOperacionMesa.FormaDePagoMesa fp = new cl.bcs.mesa.MRF.CrearOperacionMesa.FormaDePagoMesa();
		if (listaFormaPago.size() > 0) {
			fp = listaFormaPago.get(0);
		}
		return fp;
	}

	private IngresarDetalleCompraVentaPacto_DetalleOperacion_IN copyPojoOperacionPactoDetalle(IngresarDetalleOperacionMesa_DetalleOperacion_IN operacionDetalle) {
		IngresarDetalleCompraVentaPacto_DetalleOperacion_IN operacionDetallePacto = new IngresarDetalleCompraVentaPacto_DetalleOperacion_IN();

		operacionDetallePacto.IdInternoDetalle = operacionDetalle.IdInternoDetalle;
		operacionDetallePacto.IdInternoOperacion = operacionDetalle.IdInternoOperacion;
		operacionDetallePacto.IdReserva = operacionDetalle.IdReserva;
		operacionDetallePacto.IdCliente = operacionDetalle.IdCliente;
		operacionDetallePacto.IdPortafolio = operacionDetalle.IdPortafolio;
		operacionDetallePacto.IdClienteCorredor = operacionDetalle.IdClienteCorredor;
		operacionDetallePacto.IdPortafolioCorredor = operacionDetalle.IdPortafolioCorredor;
		operacionDetallePacto.IdMesa = operacionDetalle.IdMesa;
		operacionDetallePacto.IdTipoProducto = operacionDetalle.IdTipoProducto;
		operacionDetallePacto.IdTipoOperacion = operacionDetalle.IdTipoOperacion;
		operacionDetallePacto.IdInstrumento = operacionDetalle.IdInstrumento;
		operacionDetallePacto.Nemotecnico = operacionDetalle.Nemotecnico;
		operacionDetallePacto.EsFungible = operacionDetalle.EsFungible;
		operacionDetallePacto.IdBolsa = operacionDetalle.IdBolsa;
		operacionDetallePacto.IdBoveda = operacionDetalle.IdBoveda;
		operacionDetallePacto.IdCartera = operacionDetalle.IdCartera;
		operacionDetallePacto.FolioCartera = operacionDetalle.FolioCartera;
		operacionDetallePacto.CodigoIFRS = operacionDetalle.CodigoIFRS;
		operacionDetallePacto.IdEmisor = operacionDetalle.IdEmisor;
		operacionDetallePacto.IdMoneda = operacionDetalle.IdMoneda;
		operacionDetallePacto.IdTipoReajuste = operacionDetalle.IdTipoReajuste;
		operacionDetallePacto.IdMonedaEmisionInstrumento = operacionDetalle.IdMonedaEmisionInstrumento;
		operacionDetallePacto.IdMonedaLiquidacionInstrumento = operacionDetalle.IdMonedaLiquidacionInstrumento;
		operacionDetallePacto.IdMonedaLiquidacionOperacion = operacionDetalle.IdMonedaLiquidacionOperacion;
		operacionDetallePacto.CorteMinimo = operacionDetalle.CorteMinimo;
		operacionDetallePacto.Cantidad = operacionDetalle.Cantidad;
		operacionDetallePacto.Precio = operacionDetalle.Precio;
		operacionDetallePacto.PrecioMercado = operacionDetalle.PrecioMercado;
		operacionDetallePacto.Tasa = operacionDetalle.Tasa;
		operacionDetallePacto.TasaMercado = operacionDetalle.TasaMercado;
		operacionDetallePacto.Monto = operacionDetalle.Monto;
		operacionDetallePacto.MontoLiquidacion = operacionDetalle.MontoLiquidacion;
		operacionDetallePacto.MontoLiquidacionOperacion = operacionDetalle.MontoLiquidacionOperacion;
		operacionDetallePacto.ValorRescate = operacionDetalle.ValorRescate;
		operacionDetallePacto.FechaVencimiento = operacionDetalle.FechaVencimiento;
		operacionDetallePacto.MontoValorizadoReserva = operacionDetalle.MontoValorizadoReserva;
		operacionDetallePacto.TipoCarteraCorredor = operacionDetalle.TipoCarteraCorredor;
		operacionDetallePacto.MontoFinalCarteraMonedaSistema=operacionDetalle.MontoFinalCarteraMonedaSistema;
		operacionDetallePacto.MontoFinalCarteraMonedaInstrumento=operacionDetalle.MontoFinalCarteraMonedaInstrumento;

		return operacionDetallePacto;
	}

	private IngresarDetalleCompraVentaPacto_Operacion_IN copyPojoOperacionPacto(IngresarDetalleOperacionMesa_Operacion_IN operacion) {
		IngresarDetalleCompraVentaPacto_Operacion_IN operacionPacto = new IngresarDetalleCompraVentaPacto_Operacion_IN();

		operacionPacto.Accion = operacion.Accion;
		operacionPacto.IdInternoOperacion = operacion.IdInternoOperacion;
		operacionPacto.IdOperacion = operacion.IdOperacion;
		operacionPacto.FolioOperacion = operacion.FolioOperacion;
		operacionPacto.FolioCartera = operacion.FolioCartera;
		operacionPacto.IdCliente = operacion.IdCliente;
		operacionPacto.IdPortafolio = operacion.IdPortafolio;
		operacionPacto.IdClienteCorredor = operacion.IdClienteCorredor;
		operacionPacto.IdPortafolioCorredor = operacion.IdPortafolioCorredor;
		operacionPacto.Intermesa = operacion.Intermesa;
		operacionPacto.IdMesaOrigen = operacion.IdMesaOrigen;
		operacionPacto.IdMesaDestino = operacion.IdMesaDestino;
		operacionPacto.AfectaCustodia = operacion.AfectaCustodia;
		operacionPacto.IdTipoProducto = operacion.IdTipoProducto;
		operacionPacto.IdTipoOperacion = operacion.IdTipoOperacion;
		operacionPacto.IdTipoReajuste = operacion.IdTipoReajuste;
		operacionPacto.Plazo = operacion.Plazo;
		operacionPacto.Monto = operacion.Monto;
		operacionPacto.MontoMoneda = operacion.MontoMoneda;
		operacionPacto.IdMoneda = operacion.IdMoneda;
		operacionPacto.IdMonedaLiquidacion = operacion.IdMonedaLiquidacion;
		operacionPacto.Tasa = operacion.Tasa;
		operacionPacto.ValorFinal = operacion.ValorFinal;
		operacionPacto.ValorFinalMoneda = operacion.ValorFinalMoneda;
		operacionPacto.TasaAgente = operacion.TasaAgente;
		operacionPacto.FechaOperacion = operacion.FechaOperacion;
		operacionPacto.FechaVencimiento = operacion.FechaVencimiento;
		operacionPacto.IdCondicionLiquidacion = operacion.IdCondicionLiquidacion;
		operacionPacto.FechaLiquidacion = operacion.FechaLiquidacion;
		operacionPacto.IdAgente = operacion.IdAgente;
		operacionPacto.IdOperador = operacion.IdOperador;
		operacionPacto.IdEstado = operacion.IdEstado;
		operacionPacto.Glosa = operacion.Glosa;
		operacionPacto.IdTipoCanalIngreso = operacion.IdTipoCanalIngreso;
		operacionPacto.TipoDocumento = operacion.TipoDocumento;
		operacionPacto.IdContacto = operacion.IdContacto;

		return operacionPacto;
	}

	private IngresarDetalleCompraVentaTermino_DetalleOperacion_IN copyPojoOperacionATerminoDetalle(IngresarDetalleOperacionMesa_DetalleOperacion_IN operacionDetalle) {
		IngresarDetalleCompraVentaTermino_DetalleOperacion_IN operacionDetalleTermino = new IngresarDetalleCompraVentaTermino_DetalleOperacion_IN();

		operacionDetalleTermino.IdInternoDetalle = operacionDetalle.IdInternoDetalle;
		operacionDetalleTermino.IdInternoOperacion = operacionDetalle.IdInternoOperacion;
		operacionDetalleTermino.IdReserva = operacionDetalle.IdReserva;
		operacionDetalleTermino.IdCliente = operacionDetalle.IdCliente;
		operacionDetalleTermino.IdPortafolio = operacionDetalle.IdPortafolio;
		operacionDetalleTermino.IdClienteCorredor = operacionDetalle.IdClienteCorredor;
		operacionDetalleTermino.IdPortafolioCorredor = operacionDetalle.IdPortafolioCorredor;
		operacionDetalleTermino.IdMesa = operacionDetalle.IdMesa;
		operacionDetalleTermino.IdTipoProducto = operacionDetalle.IdTipoProducto;
		operacionDetalleTermino.IdTipoOperacion = operacionDetalle.IdTipoOperacion;
		operacionDetalleTermino.IdInstrumento = operacionDetalle.IdInstrumento;
		operacionDetalleTermino.Nemotecnico = operacionDetalle.Nemotecnico;
		operacionDetalleTermino.EsFungible = operacionDetalle.EsFungible;
		operacionDetalleTermino.IdBolsa = operacionDetalle.IdBolsa;
		operacionDetalleTermino.IdBoveda = operacionDetalle.IdBoveda;
		operacionDetalleTermino.IdCartera = operacionDetalle.IdCartera;
		operacionDetalleTermino.FolioCartera = operacionDetalle.FolioCartera;
		operacionDetalleTermino.CodigoIFRS = operacionDetalle.CodigoIFRS;
		operacionDetalleTermino.IdEmisor = operacionDetalle.IdEmisor;
		operacionDetalleTermino.IdMoneda = operacionDetalle.IdMoneda;
		operacionDetalleTermino.IdTipoReajuste = operacionDetalle.IdTipoReajuste;
		operacionDetalleTermino.IdMonedaEmisionInstrumento = operacionDetalle.IdMonedaEmisionInstrumento;
		operacionDetalleTermino.IdMonedaLiquidacionInstrumento = operacionDetalle.IdMonedaLiquidacionInstrumento;
		operacionDetalleTermino.IdMonedaLiquidacionOperacion = operacionDetalle.IdMonedaLiquidacionOperacion;
		operacionDetalleTermino.CorteMinimo = operacionDetalle.CorteMinimo;
		operacionDetalleTermino.Cantidad = operacionDetalle.Cantidad;
		operacionDetalleTermino.Precio = operacionDetalle.Precio;
		operacionDetalleTermino.PrecioMercado = operacionDetalle.PrecioMercado;
		operacionDetalleTermino.Tasa = operacionDetalle.Tasa;
		operacionDetalleTermino.TasaMercado = operacionDetalle.TasaMercado;
		operacionDetalleTermino.Monto = operacionDetalle.Monto;
		operacionDetalleTermino.MontoLiquidacion = operacionDetalle.MontoLiquidacion;
		operacionDetalleTermino.MontoLiquidacionOperacion = operacionDetalle.MontoLiquidacionOperacion;
		operacionDetalleTermino.ValorRescate = operacionDetalle.ValorRescate;
		operacionDetalleTermino.FechaVencimiento = operacionDetalle.FechaVencimiento;
		operacionDetalleTermino.MontoValorizadoReserva = operacionDetalle.MontoValorizadoReserva;

		return operacionDetalleTermino;
	}

	private IngresarDetalleCompraVentaTermino_Operacion_IN copyPojoOperacionATermino(IngresarDetalleOperacionMesa_Operacion_IN operacion) {
		IngresarDetalleCompraVentaTermino_Operacion_IN operacionTermino = new IngresarDetalleCompraVentaTermino_Operacion_IN();

		operacionTermino.Accion = operacion.Accion;
		operacionTermino.IdInternoOperacion = operacion.IdInternoOperacion;
		operacionTermino.IdOperacion = operacion.IdOperacion;
		operacionTermino.FolioOperacion = operacion.FolioOperacion;
		operacionTermino.FolioCartera = operacion.FolioCartera;
		operacionTermino.IdCliente = operacion.IdCliente;
		operacionTermino.IdPortafolio = operacion.IdPortafolio;
		operacionTermino.IdClienteCorredor = operacion.IdClienteCorredor;
		operacionTermino.IdPortafolioCorredor = operacion.IdPortafolioCorredor;
		operacionTermino.Intermesa = operacion.Intermesa;
		operacionTermino.IdMesaOrigen = operacion.IdMesaOrigen;
		operacionTermino.IdMesaDestino = operacion.IdMesaDestino;
		operacionTermino.AfectaCustodia = operacion.AfectaCustodia;
		operacionTermino.IdTipoProducto = operacion.IdTipoProducto;
		operacionTermino.IdTipoOperacion = operacion.IdTipoOperacion;
		operacionTermino.IdTipoReajuste = operacion.IdTipoReajuste;
		operacionTermino.Plazo = operacion.Plazo;
		operacionTermino.Monto = operacion.Monto;
		operacionTermino.MontoMoneda = operacion.MontoMoneda;
		operacionTermino.IdMoneda = operacion.IdMoneda;
		operacionTermino.IdMonedaLiquidacion = operacion.IdMonedaLiquidacion;
		operacionTermino.Tasa = operacion.Tasa;
		operacionTermino.ValorFinal = operacion.ValorFinal;
		operacionTermino.ValorFinalMoneda = operacion.ValorFinalMoneda;
		operacionTermino.TasaAgente = operacion.TasaAgente;
		operacionTermino.FechaOperacion = operacion.FechaOperacion;
		operacionTermino.FechaVencimiento = operacion.FechaVencimiento;
		operacionTermino.IdCondicionLiquidacion = operacion.IdCondicionLiquidacion;
		operacionTermino.FechaLiquidacion = operacion.FechaLiquidacion;
		operacionTermino.IdAgente = operacion.IdAgente;
		operacionTermino.IdOperador = operacion.IdOperador;
		operacionTermino.IdEstado = operacion.IdEstado;
		operacionTermino.Glosa = operacion.Glosa;
		operacionTermino.IdTipoCanalIngreso = operacion.IdTipoCanalIngreso;
		operacionTermino.TipoDocumento = operacion.TipoDocumento;
		operacionTermino.IdContacto = operacion.IdContacto;

		return operacionTermino;
	}

	private List<IngresarDetalleOperacionMesa_FormaPagoOperacion_IN> crearFormaDePagoPorDefecto(Connection conn,List<IngresarDetalleOperacionMesa_Operacion_IN> operacion,List<IngresarDetalleOperacionMesa_DetalleOperacion_IN> operacionDetalle) throws SQLException {
		SQL sql = new SQL();
		
		Long idInternoOperacion = 0L;
		Long idPortafolioCli =0L;
		Long idPortafolioCor = 0L;
		String idTipoOperacion="";
		String idTipoOperacionCli ="";
		String idMoneda="";
		String fechaOperacion="";
		String fechaLiquidacion="";
		String fechaVencimiento="";
		Long idOperacion =0L;
		Long idInternoPagoOperacion =0L;
		BigDecimal monto = BigDecimal.ZERO;
		BigDecimal montoLiquidacion = BigDecimal.ZERO;

		String recibePagaPortafolioCli ="";
		String recibePagaPortafolioCor ="";
		
		BigDecimal montoAsignadofmpCli = BigDecimal.ZERO;
		BigDecimal montoAsignadofmpCor = BigDecimal.ZERO;
		HashMap<String, Object>paramsFmpCli = new HashMap<>(3);
		HashMap<String, Object>paramsFmpCor = new HashMap<>(3);
		
		sql.batch(conn, SQL_DELETE_FORMAS_DE_PAGO_DEFECTO, operacion, IngresarDetalleOperacionMesa_Operacion_IN.class);
		
		for(IngresarDetalleOperacionMesa_Operacion_IN op: operacion) {
			idInternoOperacion = op.IdInternoOperacion;
			idPortafolioCli = op.IdPortafolio;
			idPortafolioCor = op.IdPortafolioCorredor;
			idTipoOperacion = op.IdTipoOperacion;
			
			idMoneda=op.IdMoneda;
			fechaOperacion = op.FechaOperacion;
			fechaLiquidacion = op.FechaLiquidacion;
			fechaVencimiento = op.FechaVencimiento;
			idOperacion = op.IdOperacion;
		}
		for(IngresarDetalleOperacionMesa_DetalleOperacion_IN opDet: operacionDetalle) {
			monto = idTipoOperacion.equals("VENTER") || idTipoOperacion.equals("COMTER") ?opDet.MontoLiquidacion:opDet.INGMontoAPlazoMonedaInstrumentoDetalle;
			montoLiquidacion =opDet.MontoLiquidacion;
		}
		
		if(idTipoOperacion.equals("VENPAC")) {
			paramsFmpCor.put("IdPortafolio",idPortafolioCor);
			paramsFmpCor.put("IdMoneda",idMoneda);
			paramsFmpCor.put("IdTipoOperacion","VENPAC");
			recibePagaPortafolioCor= "P";
			montoAsignadofmpCor =montoLiquidacion;
			
			
			paramsFmpCli.put("IdPortafolio",idPortafolioCli);
			paramsFmpCli.put("IdMoneda",idMoneda);
			paramsFmpCli.put("IdTipoOperacion","VENPAC");
			recibePagaPortafolioCli ="R";
			montoAsignadofmpCli = monto;
			
		}else if(idTipoOperacion.equals("COMPAC") || idTipoOperacion.equals("COMTER")){
			paramsFmpCor.put("IdPortafolio",idPortafolioCor);
			paramsFmpCor.put("IdMoneda",idMoneda);
			paramsFmpCor.put("IdTipoOperacion",idTipoOperacion.equals("COMPAC")?"VENPAC":"VENTER");
			recibePagaPortafolioCor ="R";
			montoAsignadofmpCor = monto;
			
			paramsFmpCli.put("IdPortafolio",idPortafolioCli);
			paramsFmpCli.put("IdMoneda",idMoneda);
			paramsFmpCli.put("IdTipoOperacion",idTipoOperacion.equals("COMPAC")?"VENPAC":"VENTER");
			recibePagaPortafolioCli= "P";
			montoAsignadofmpCli =montoLiquidacion;
		}else if(idTipoOperacion.equals("VENTER")) {
			paramsFmpCor.put("IdPortafolio",idPortafolioCor);
			paramsFmpCor.put("IdMoneda",idMoneda);
			paramsFmpCor.put("IdTipoOperacion","VENTER");
			recibePagaPortafolioCor= "P";
			montoAsignadofmpCor =montoLiquidacion;
		}
		List<IngresarDetalleOperacionMesa_FormaPagoOperacion_IN> formasDePago = new ArrayList<IngresarDetalleOperacionMesa.IngresarDetalleOperacionMesa_FormaPagoOperacion_IN>();
		
		if(paramsFmpCli.size()>0) {
			List<cl.bcs.mesa.MRF.CrearOperacionMesa.PreferenciaPagoPortafolio> fmpDefectoCli = sql.query(conn, SQL_SELECT_FORMAS_DE_PAGO_DEFECTO_CLIENTE,paramsFmpCli , cl.bcs.mesa.MRF.CrearOperacionMesa.PreferenciaPagoPortafolio.class);
			if(!(("VENTER").equals(idTipoOperacion) && "P".equals(recibePagaPortafolioCli)) || !(("COMTER").equals(idTipoOperacion) && "R".equals(recibePagaPortafolioCli))) {
			 	for(cl.bcs.mesa.MRF.CrearOperacionMesa.PreferenciaPagoPortafolio fmpCli:fmpDefectoCli) {
			 		IngresarDetalleOperacionMesa_FormaPagoOperacion_IN fmp = new IngresarDetalleOperacionMesa_FormaPagoOperacion_IN();
			 		idInternoPagoOperacion +=1;
			 		fmp.AfectarTesoreria = fmpCli.IdMedioDePago.equals("CIN")?"N":"S";
			 		fmp.FechaLiquidacion = "P".equals(recibePagaPortafolioCli)?fechaLiquidacion:fechaVencimiento;
			 		fmp.IdOperacion = idOperacion;
			 		fmp.IdInternoPagoOperacion = idInternoPagoOperacion;
			 		fmp.IdFormaDePago = fmpCli.IdPreferenciaPago;
			 		fmp.IdMoneda = idMoneda;
			 		fmp.RecibePagaPortafolio =recibePagaPortafolioCli;
			 		fmp.MontoFormaDePago = montoAsignadofmpCli;
			 		fmp.IdCuentaBancaria = fmpCli.IdCuentaBancaria;
			 		fmp.IdInternoOperacion = idInternoOperacion;
			 		fmp.IdComprobantePagoOperacion =0L;
			 		fmp.NombreBanco = fmpCli.NombreBancoCuentaBancaria;
			 		fmp.NombreBancoTarjeta = fmpCli.NombreBancoTarjetaBancaria;
			 		fmp.CuentaBancaria = fmpCli.NumeroCuentaBancaria;
			 		fmp.NumeroTarjeta = fmpCli.NumeroTarjetaBancaria;
			 		formasDePago.add(fmp);
			 	}
			 }
		}
		if(paramsFmpCor.size()>0) {
			List<cl.bcs.mesa.MRF.CrearOperacionMesa.PreferenciaPagoPortafolio> fmpDefectoCor = sql.query(conn, SQL_SELECT_FORMAS_DE_PAGO_DEFECTO_CORREDOR,paramsFmpCor , cl.bcs.mesa.MRF.CrearOperacionMesa.PreferenciaPagoPortafolio.class);
			if(!("COMTER").equals(idTipoOperacion)) {
		 		for(cl.bcs.mesa.MRF.CrearOperacionMesa.PreferenciaPagoPortafolio fmpCor:fmpDefectoCor) {
			 		IngresarDetalleOperacionMesa_FormaPagoOperacion_IN fmp = new IngresarDetalleOperacionMesa_FormaPagoOperacion_IN();
			 		idInternoPagoOperacion +=1;
			 		fmp.AfectarTesoreria = fmpCor.IdMedioDePago.equals("CIN")?"N":"S";
			 		fmp.FechaLiquidacion ="P".equals(recibePagaPortafolioCor)?fechaLiquidacion:fechaVencimiento;
			 		fmp.IdOperacion = idOperacion;
			 		fmp.IdInternoPagoOperacion = idInternoPagoOperacion;
			 		fmp.IdFormaDePago = fmpCor.IdPreferenciaPago;
			 		fmp.IdMoneda = idMoneda;
			 		fmp.RecibePagaPortafolio =recibePagaPortafolioCor;
			 		fmp.MontoFormaDePago = montoAsignadofmpCor;
			 		fmp.IdCuentaBancaria = fmpCor.IdCuentaBancaria;
			 		fmp.IdInternoOperacion = idInternoOperacion;
			 		fmp.IdComprobantePagoOperacion =0L;
			 		fmp.NombreBanco = fmpCor.NombreBancoCuentaBancaria;
			 		fmp.NombreBancoTarjeta = fmpCor.NombreBancoTarjetaBancaria;
			 		fmp.CuentaBancaria = fmpCor.NumeroCuentaBancaria;
			 		fmp.NumeroTarjeta = fmpCor.NumeroTarjetaBancaria;
			 		formasDePago.add(fmp);
			 	}
			}
		}
		
		
		
	 	
	 	
	 	
		
		
		return formasDePago;
	}
	
	private void validarCustodiaDetalle(Connection conn, Long idEmpresa, List<IngresarDetalleOperacionMesa_DetalleOperacion_IN> detalle, IngresarDetalleOperacionMesa_OUT out, SQL _sql) throws Exception {
		List<Long> folioCartera = detalle.stream().filter(det -> det.FolioCartera != null && det.FolioCartera > 0).map(det -> det.FolioCartera).collect(Collectors.toList());
		if(folioCartera.isEmpty())
			return;
		
		HashMap<String, Object> params = new HashMap<>(1);
		params.put("ListaFolioCartera", folioCartera);
		
		//Bloqueo carteras
		_sql.query(conn, SQL_SELECT_BLOQUEO_CARTERAS, params, VerificarCartera.class);
		
		ConsultarCarteraIngresoOperacionMesa consultarCarterasMesa = new ConsultarCarteraIngresoOperacionMesa();
		EntradaConsultarCarteraIngresoOperacionMesa inConsultarCarterasMesa = new EntradaConsultarCarteraIngresoOperacionMesa();
		SalidaConsultarCarteraIngresoOperacionMesa outConsultarCarterasMesa = new SalidaConsultarCarteraIngresoOperacionMesa();
		
		inConsultarCarterasMesa.p_IdEmpresa = idEmpresa;
		inConsultarCarterasMesa.p_FolioCartera.addAll(folioCartera);
		outConsultarCarterasMesa = consultarCarterasMesa.logica(conn, inConsultarCarterasMesa);
		HashMap<String, Object> mapCarteras = Utils.List2Hash(outConsultarCarterasMesa.P_OUT_SALIDA, "FolioCartera");
		
		for(IngresarDetalleOperacionMesa_DetalleOperacion_IN det : detalle) {
			if(det.FolioCartera == null || det.FolioCartera <= 0)
				continue;
			DetalleConsulta cartera = (DetalleConsulta) mapCarteras.get(det.FolioCartera);
			if(cartera != null) {
				BigDecimal cantidadRescate = BigDecimal.ZERO;
				BigDecimal cantidadCartera = BigDecimal.ZERO;
				if("IF".equals(det.IdClasificacionTipoInstrumento) && "S".equalsIgnoreCase(det.EsFungible)) {
					cantidadRescate = det.ValorRescate;
					cantidadCartera = cartera.INGValorRescate;
				} else {
					cantidadRescate = det.Cantidad;
					cantidadCartera = cartera.INGCantidad;
				}
				
				if(cantidadCartera.compareTo(cantidadRescate) < 0) {
					HashMap<String, String> mapMsg = new HashMap<>();
					mapMsg.put("FolioCartera", det.FolioCartera.toString());
					out.P_OUT_MENSAJES.add(new MensajeSalida(1L, MensajeSalida.MENSAJE_ERROR, "ERROR_CARTERA_NO_TIENE_CUSTODIA", mapMsg, conn, idEmpresa));
					out.P_OUT_ERRORESCONTADOR++;
				} else {
					if("IF".equals(det.IdClasificacionTipoInstrumento)  && "S".equalsIgnoreCase(det.EsFungible)) {
						cartera.INGValorRescate = cartera.INGValorRescate.subtract(det.ValorRescate);
					} else {
						cartera.INGCantidad = cartera.INGCantidad.subtract(det.Cantidad);
					}
				}
			} else {
				HashMap<String, String> mapMsg = new HashMap<>();
				mapMsg.put("FolioCartera", det.FolioCartera.toString());
				out.P_OUT_MENSAJES.add(new MensajeSalida(1L, MensajeSalida.MENSAJE_ERROR, "ERROR_CARTERA_NO_TIENE_CUSTODIA", mapMsg, conn, idEmpresa));
				out.P_OUT_ERRORESCONTADOR++;
			}
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		Gson gson = Utils.getGsonBuilder();
		IngresarDetalleOperacionMesa_IN jin = gson.fromJson(in.toString(), IngresarDetalleOperacionMesa_IN.class);
		IngresarDetalleOperacionMesa_OUT out = logica(conn, jin);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	};

	// ------------------------------------------------------------
	// METODO PRINCIPAL PARA PRUEBAS LOCALES
	// ------------------------------------------------------------

	public static void main(String[] args) throws Exception, SQLException {
		Connection conn = null;
		Gson gson = new Gson();
		try {
			System.out.println("== BEGIN ==");
			conn = PlataformaUtils.getPropertiesConnection("desa02", "connections.properties");
			
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":97,\"p_IdModulo\":\"MRF\",\"p_UsaReglasNegocio\":\"N\",\"p_IdEstadoConfirmacionPago\":\"NCF\",\"p_Operacion\":[{\"Accion\":\"MODIFICAR\",\"IdInternoOperacion\":1,\"IdOperacion\":194141,\"FolioOperacion\":0,\"FechaOperacion\":\"2026-02-02\",\"FechaLiquidacion\":\"2026-02-02\",\"IdCliente\":4843,\"IdPortafolio\":1285,\"IdClienteCorredor\":4846,\"IdPortafolioCorredor\":2225,\"Intermesa\":\"N\",\"IdMesaOrigen\":39,\"IdMesaDestino\":39,\"AfectaCustodia\":\"S\",\"IdTipoProducto\":\"MERF\",\"IdTipoOperacion\":\"VENPAC\",\"IdTipoReajuste\":\"CLP\",\"Plazo\":8,\"Monto\":2000000000,\"MontoMoneda\":2000000000,\"MontoLiquidacion\":5000000000,\"IdMoneda\":\"CLP\",\"IdMonedaLiquidacion\":\"CLP\",\"Tasa\":0.23,\"ValorFinal\":2001226667,\"ValorFinalMoneda\":2001226667,\"TasaAgente\":0,\"FechaVencimiento\":\"2026-02-10\",\"IdCondicionLiquidacion\":\"T0\",\"IdAgente\":4111,\"IdOperador\":97,\"IdEstado\":\"PEN\",\"Glosa\":\"\",\"IdTipoCanalIngreso\":\"AGE\",\"TipoDocumento\":\"\",\"IdContacto\":0,\"TasaAnualizada\":0,\"CostoFondo\":0.02,\"UtilidadPerdida\":-1120000.0000000002,\"UtilidadFinal\":0,\"ValorizacionManual\":\"N\"}],\"p_DetalleOperacion\":[{\"IdInternoDetalle\":0,\"IdInternoOperacion\":1,\"IdReserva\":0,\"IdCliente\":4843,\"IdPortafolio\":1285,\"IdClienteCorredor\":4846,\"IdPortafolioCorredor\":2225,\"IdMesa\":39,\"IdTipoProducto\":\"MERF\",\"IdTipoOperacion\":\"VENPAC\",\"IdClasificacionTipoInstrumento\":\"IF\",\"IdInstrumento\":2224,\"Nemotecnico\":\"PAGARE NR\",\"EsFungible\":\"S\",\"IdBolsa\":\"BCS\",\"IdBoveda\":\"DCV\",\"FolioCartera\":1362457,\"IdCartera\":83811460,\"CodigoIFRS\":\"T\",\"IdEmisor\":\"SANTANDER\",\"IdMoneda\":\"CLP\",\"IdTipoReajuste\":\"CLP\",\"IdMonedaEmisionInstrumento\":\"CLP\",\"IdMonedaLiquidacionInstrumento\":\"CLP\",\"IdMonedaLiquidacionOperacion\":\"CLP\",\"CorteMinimo\":1,\"Cantidad\":1,\"Precio\":0,\"PrecioMercado\":0,\"Tasa\":0.38,\"TasaMercado\":0,\"Monto\":5000000000,\"MontoLiquidacion\":5000000000,\"MontoLiquidacionOperacion\":5000000000,\"ValorRescate\":5006966667,\"FechaVencimiento\":\"2026-02-13\",\"MontoValorizadoReserva\":0,\"TipoCarteraCorredor\":\"\",\"INGMontoAPlazoDetalle\":5003066666.666667,\"INGMontoAPlazoMonedaInstrumentoDetalle\":5003066666.666667}],\"p_FormasDePago\":[{\"AfectarTesoreria\":\"S\",\"FechaLiquidacion\":\"2026-02-02T03:00:00.000Z\",\"FechaVencimiento\":\"2026-02-02T03:00:00.000Z\",\"IdComprobantePagoOperacion\":2134054,\"IdCuentaBancaria\":1084,\"IdFormaDePago\":\"VENPAC_TRA_CLP\",\"IdInternoOperacion\":1,\"IdMoneda\":\"CLP\",\"IdOperacion\":194141,\"IdTipoCuentaBancaria\":\"\",\"MontoFormaDePago\":2000000000,\"NombreCuentaBancaria\":\"\",\"RecibePagaPortafolio\":\"P\",\"NombreFormaDePago\":\"TRANSFERENCIA\"},{\"AfectarTesoreria\":\"S\",\"FechaLiquidacion\":\"2026-02-10T03:00:00.000Z\",\"FechaVencimiento\":\"2026-02-10T03:00:00.000Z\",\"IdComprobantePagoOperacion\":2134053,\"IdCuentaBancaria\":0,\"IdFormaDePago\":\"VENPAC_TRA_CLP\",\"IdInternoOperacion\":1,\"IdMoneda\":\"CLP\",\"IdOperacion\":194141,\"IdTipoCuentaBancaria\":\"\",\"MontoFormaDePago\":2001226667,\"NombreCuentaBancaria\":\"\",\"RecibePagaPortafolio\":\"R\",\"NombreFormaDePago\":\"TRANSFERENCIA\"}]}";

			IngresarDetalleOperacionMesa_IN objIn = gson.fromJson(json, IngresarDetalleOperacionMesa_IN.class);
			IngresarDetalleOperacionMesa demo = new IngresarDetalleOperacionMesa();
			IngresarDetalleOperacionMesa_OUT ObjOut = demo.logica(conn, objIn);
			System.out.println(ObjOut.toString());
			//conn.commit();
			conn.rollback();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			conn.close();
		}
		System.out.println("== END ==");
	};
}
