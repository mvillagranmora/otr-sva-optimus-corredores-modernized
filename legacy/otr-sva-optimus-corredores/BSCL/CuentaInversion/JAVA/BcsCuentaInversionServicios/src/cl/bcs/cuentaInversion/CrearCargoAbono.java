package cl.bcs.cuentaInversion;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import cl.bcs.utils.BcsPojo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.AgregarLog.InAgregarLog;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente.FechasIn;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.cuentaInversion.ActualizarSaldoCargoAbono.ActualizarSaldoCargoAbonoEntrada;
import cl.bcs.cuentaInversion.ActualizarSaldoCargoAbono.CuentaInversion;
import cl.bcs.cuentaInversion.CrearCargoAbono.ObjComprobanteDetalle;
import cl.bcs.ordenes.CrearOrdenes.RequisitoOperativoCrearOrden;
import cl.bcs.plataforma.bat.AgregarPrograma;
import cl.bcs.plataforma.bat.AgregarPrograma.InAgregarPrograma.Parametro;
import cl.bcs.plataforma.pla.SaldoFlujoCaja;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.Feriados;
import cl.bcs.plataforma.pla.utils.MonedaFormato;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.StringUtils;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;


public class CrearCargoAbono implements JavaCall{
	private static final Logger logger = LogManager.getLogger(CrearCargoAbono.class);
	
	private final String SQL_INSERT_CARGO_ABONO_A_GENERAR 		= loadResourceToString("cl/bcs/cuentaInversion/sql/CrearCargoAbono_INS_CargoAbonoAGenerar.sql");
	private final String SQL_SELECT_CARGO_ABONO_DETALLADO 		= "select \"NumeroCargoAbono\",\"IdCargoAbonoMovimiento\",\"IdTipoMovimiento\"from \"BSCL.CuentaInversion.BD::CTA.CrearCargoAbono_OUT_CargoAbonoGenerado\"";
	private final String SQL_SELECT_INFORMACION_DETALLE 		= "select \"NumeroCargoAbono\",\"IdCargoAbonoMovimiento\",\"Codigo\",\"Descripcion\"from \"BSCL.CuentaInversion.BD::CTA.CrearCargoAbono_OUT_InformacionDetalle\"";
	private final String SQL_SELECT_ADVERTENCIAS_DETALLE 		= "select \"NumeroCargoAbono\",\"Codigo\",\"RequiereAutorizacion\",\"Descripcion\"from \"BSCL.CuentaInversion.BD::CTA.CrearCargoAbono_OUT_AdvertenciasDetalle\"";
	private final String SQL_SELECT_ERRORES_DETALLE 			= "select \"NumeroCargoAbono\",\"Codigo\",\"Descripcion\"from \"BSCL.CuentaInversion.BD::CTA.CrearCargoAbono_OUT_ErroresDetalle\"";
	private final String SQL_CALL_CREA_CARGO_ABONO 				= "call \"BSCL.CuentaInversion.BD.Servicios::CrearCargoAbono\"(p_IdEmpresa=>:p_IdEmpresa,p_IdUsuario=>:p_IdUsuario,p_IdModulo=>:p_IdModulo,p_simularOperacion=>:p_simularOperacion,p_UsaReglasNegocio=>'S',p_FechaSistema=>:p_FechaSistema,p_EsIngresoManual=>:p_EsIngresoManual,p_out_InformacionContador=>@p_out_InformacionContador,p_out_AdvertenciasContador=>@p_out_AdvertenciasContador,p_out_ErroresContador=>@p_out_ErroresContador)";
	private final String SQL_SELECT_DATOS_PROCESAMIENTO 		= loadResourceToString("cl/bcs/cuentaInversion/sql/CrearCargoAbono_SelectDatosProcesamiento.sql");
	private final String SQL_INSERT_SALDOS 						= loadResourceToString("cl/bcs/cuentaInversion/sql/CrearCargoAbono_InsertSaldos.sql");
	private final String SQL_SELECT_SUMA_SALDOS 				= loadResourceToString("cl/bcs/cuentaInversion/sql/CrearCargoAbono_SelectSumaSaldos.sql");
	private final String SQL_SELECT_REQUISITOS_OPERATIVOS 		= loadResourceToString("cl/bcs/ordenes/sql/CrearOrdenes_SelectRequisitosOperativos.sql");
	private final String SQL_INSERT_CARGO_ABONO 				= loadResourceToString("cl/bcs/cuentaInversion/sql/CrearCargoAbono_InsertCargoAbono.sql");
	private final String SQL_DELETE_CARGO_ABONO_SALDOS_PASO 	= loadResourceToString("cl/bcs/cuentaInversion/sql/CrearCargoAbono_DeleteCargoAbonoSaldosPaso.sql");
	private final String SQL_DELETE_CARGO_ABONO_GENERAR_PASO 	= loadResourceToString("cl/bcs/cuentaInversion/sql/CrearCargoAbono_DeleteCargoAbonoGenerarPaso.sql");
	private final String SQL_SELECT_CONCEPTO_ORDEN 				= loadResourceToString("cl/bcs/cuentaInversion/sql/CrearCargoAbono_SelectConceptoOrden.sql");
	private final String SQL_SELECT_CUENTA_PROPIA 				= loadResourceToString("cl/bcs/cuentaInversion/sql/CrearCargoAbono_SelectCuentaPropia.sql");
	private final String SQL_SELECT_Corredora 					= loadResourceToString("cl/bcs/cuentaInversion/sql/CrearCargoAbono_SelectCorredora.sql");
	private final String SQL_SELECT_CLIENTE                     = loadResourceToString("cl/bcs/cuentaInversion/sql/CrearCargoAbono_SelectCliente.sql");
	private final String SQL_SELECT_IDTIPOCOMPROBANTEORIGEN     = loadResourceToString("cl/bcs/cuentaInversion/sql/CrearCargoAbono_SelectIdTipoComprobanteOrigen.sql");
	
	public static class ObjCargoAbonoAGenerar implements Cloneable{
		public Long					NumeroCargoAbono;
		public Long					IdPortafolio;
		public String				IdMoneda;
		public String				IdTipoMovimiento;
		public String				IdConcepto;
		public String				Descripcion;
		public String				IdCondicionDeLiquidacion;
		public String				FechaCondicionDeLiquidacion;
		public BigDecimal			Monto; 
		public Long					IdEjecutivo; 
		public Long					IdSucursal; 
		public String				IdTipoProducto; 
		public String				IdTipoOperacion; 
		public Long					IdOperacion; 
		public String				FechaOperacion; 
		public String				FechaLiquidacionOperacion; 
		public String				FechaVencimientoOperacion; 
		public String				IdMonedaOperacion; 
		public BigDecimal			MontoOperacion; 
		public BigDecimal			TipoCambioOperacion; 
		public Long					IdContraparte; 
		public String				IdTipoComprobante; 
		public Long					IdComprobante; 
		public String				FolioComprobante; 
		public Long					IdComprobanteDetalle; 
		public Long					IdComprobanteFormaDePago; 
		public String				IdTipoMovimientoAsociado; 
		public Long					IdMovimientoAsociado; 
		public String				FechaComprobante; 
		public Long					IdPersonaPagoCobro; 
		public Long					IdInstrumento; 
		public String				IdTipoInstrumento;
		public BigDecimal			Cantidad; 
		public String				Nemo; 
		public String				EsIndicadorBilateral; 
		public String				EsCargoAbonoPorTraspaso; 
		public Long					IdPortafolioTraspaso; 
		public String				GeneroContraparteEnTesoreria; 
		public String				CertificadoAutomatico;
		
		public String toString() {
			return Utils.printJson(this, false);
		};

		public Object clone(){ Object obj=null; try{ obj=super.clone(); }catch(CloneNotSupportedException ex){ System.out.println(" no se puede duplicar"); } return obj; }
		
	};
	
	public static class ObjProcesamientoCargoAbonoAGenerar {
		public Long                 IdInterno;
		public Long                 IdCargoAbonoMovimiento;
		public Long					NumeroCargoAbono; 
		public Long					IdPortafolioInformado; 
		public String				IdMonedaInformado; 
		public String				IdTipoMovimientoInformado; 
		public String				IdConceptoInformado; 
		public String				Descripcion; 
		public String				IdCondicionDeLiquidacion; 
		public String				FechaCondicionDeLiquidacion; 
		public BigDecimal			Monto; 
		public Long					IdEjecutivo; 
		public Long					IdSucursal; 
		public String				IdTipoProductoInformado; 
		public String				IdTipoOperacionInformado; 
		public Long					IdOperacion; 
		public String				FechaOperacion; 
		public String				FechaLiquidacionOperacion; 
		public String				FechaVencimientoOperacion; 
		public String				IdMonedaOperacion; 
		public BigDecimal			MontoOperacion; 
		public BigDecimal			TipoCambioOperacion; 
		public Long					IdContraparte; 
		public String				IdTipoComprobante; 
		public Long					IdComprobante; 
		public String				FolioComprobante; 
		public Long					IdComprobanteDetalle; 
		public Long					IdComprobanteFormaDePago; 
		public String				IdTipoMovimientoAsociado; 
		public Long					IdMovimientoAsociado; 
		public String				FechaComprobante; 
		public Long					IdPersonaPagoCobro; 
		public Long					IdInstrumento; 
		public String				IdTipoInstrumentoInformado;
		public BigDecimal			Cantidad; 
		public String				Nemo; 
		public String				EsIndicadorBilateral; 
		public String				EsCargoAbonoPorTraspaso; 
		public Long					IdPortafolioTraspaso; 
		public String				GeneroContraparteEnTesoreria; 
		public String				CertificadoAutomatico;
		public Long                 IdCuentaInversion;
		public Long                 IdPortafolio;
		public String               IdMoneda;
		public String               IdEstado;
		public String               IdTipoMovimiento;
		public String               IdConcepto;
		public String               IdMonedaTesoreria;
		public String               IdEstadoMonedaTesoreria;
		public String 				IdTipoOperacion;
		public String               IdTipoProducto;
		public String               IdTipoInstrumento;
		public String               OperaLineaSobregiro;
		public String               NombreCompleto;
		public long                 KeyTraspaso;
		public long 				errores = 0;
		public long 				advertencias = 0;
		public long 				autorizaciones = 0;
		public long 				ok = 0;
		public String               OperaCuentaInversion;
		

		public String               DocumentoIdentidad;
		

		public String               BloqueadoPorCumplimiento = "N";
		public String 				CodigoPortafoliCargo ="";
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	}
	
	public static class ObjInsCargoAbono {
        public Long IdInterno;
        public Long IdCargoAbonoMovimiento;
        public Long NumeroCargoAbono;
        public Date FechaSistema;
        public String IdModulo;
        public String IdTipoMovimiento;
        public String EsIngresoManual;
        public Long IdCuentaInversion;
        public String NombreCompleto;
        public String IdConcepto;
        public String Descripcion;
        public String IdCondicionDeLiquidacion;
        public Date FechaCondicionDeLiquidacion;
        public BigDecimal Monto;
        public BigDecimal Saldo;
        public BigDecimal SaldoPorFechaMovimiento;
        public BigDecimal SaldoLibroInventario;
        public String EsCertificadoAutomatico;
        public Timestamp FechaCertificacion;
        public Long IdEjecutivo;
        public Long IdSucursal;
        public String IdTipoComprobante;
        public Long IdComprobante;
        public String FolioComprobante;
        public Long IdComprobanteDetalle;
        public Long IdComprobanteFormaDePago;
        public String IdTipoMovimientoAsociado;
        public Long IdMovimientoAsociado;
        public Date FechaComprobante;
        public String IdTipoProducto;
        public String IdTipoOperacion;
        public Long IdOperacion;
        public Date FechaOperacion;
        public Date FechaLiquidacionOperacion;
        public Date FechaVencimientoOperacion;
        public String IdMonedaOperacion;
        public BigDecimal MontoOperacion;
        public BigDecimal TipoCambioOperacion;
        public Long IdInstrumento;
        public String IdTipoInstrumento;
        public String Nemo;
        public BigDecimal Cantidad;
        public String EsCargoAbonoPorTraspaso;
        public String EsIndicadorBilateral;
        public String GeneroContraparteEnTesoreria;
        public Long IdContraparte;
        public Long IdPersonaPagoCobro;
        public Long IdUsuario;
        public Long IdEmpresa;
        public String IdEstado;
        public Long IdCargoAbonoRelacionadoTraspaso;
        
        public String toString() {
			return Utils.printJson(this, false);
		};
	}
	
	public static class ObjInSumaSaldos {
		public Long IdPortafolio;
		public String IdMoneda;
		public String FechaSaldo;
		public BigDecimal Saldo;		
		 
        public String toString() {
			return Utils.printJson(this, false);
		};
	}
	
	public static class ObjSumaSaldos {
		public Long IdInterno;
		public BigDecimal SaldoFinal;		
		 
        public String toString() {
			return Utils.printJson(this, false);
		};
	}
	
	public static class ObjCargoAbonoGenerado {
		public Long			NumeroCargoAbono; 
		public Long			IdCargoAbonoMovimiento; 
		public String		IdTipoMovimiento; 		
		 
        public String toString() {
			return Utils.printJson(this, false);
		};
	};
	
	public static class ObjInformacionDetalle {
		public Long			Obj; 
		public Long			IdCargoAbonoMovimiento; 
		public String		Codigo; 
		public String		Descripcion;
		
        public String toString() {
			return Utils.printJson(this, false);
		};
	}
	public static class ObjAdvertenciasDetalle {
		public Long			NumeroCargoAbono; 
		public String		Codigo; 
		public String		RequiereAutorizacion; 
		public String		Descripcion; 
		
		 
        public String toString() {
			return Utils.printJson(this, false);
		};
	}
	public static class ObjErroresDetalle {
		public Long			NumeroCargoAbono; 
		public String		Codigo; 
		public String		Descripcion; 
		 
        public String toString() {
			return Utils.printJson(this, false);
		};
	}
	
	public static class CrearCargoAbonoEntrada{
		public Long							p_IdEmpresa;
		public Long							p_IdUsuario;
		public String						p_IdModulo;
		public String						p_simularOperacion;
		public String						p_UsaReglasNegocio;
		public String						p_FechaSistema;
		public String						p_EsIngresoManual	= "N";
		public List<ObjCargoAbonoAGenerar>	p_CargoAbonoAGenerar;
		
		public CrearCargoAbonoEntrada (){			
			p_CargoAbonoAGenerar	= new ArrayList<ObjCargoAbonoAGenerar>();
		};
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	};
	
	public static class CrearCargoAbonoSalida{
		public List<MensajeSalida>			P_OUT_MENSAJES;
		public Long							P_OUT_ERRORESCONTADOR;
		public List<ObjCargoAbonoGenerado>	P_OUT_CARGOABONOGENERADO;

		public CrearCargoAbonoSalida (){
			P_OUT_MENSAJES 				= new ArrayList<MensajeSalida>();
			P_OUT_CARGOABONOGENERADO	= new ArrayList<ObjCargoAbonoGenerado>();
		};
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	};
	
	public static class ConsultaCuentaPropia_Result{
		public Long IdCuentaInversion;
	}
	
	public static class InformacionCorredora 
	{
	    public String 				Nombre;
	    public Long 				IdPersona;
	    public String				DocumentoIdentidad;
	    
        public String toString() {
			return Utils.printJson(this, false);
		};
	}

	public static class ConceptoOrden extends BcsPojo {
		public String IdConcepto;
	}
	
	public static class ObjCliente extends BcsPojo{
		public Long                 IdCuentaInversion;
		public Long					IdCliente;
	}
	
	public static class ObjComprobanteDetalle extends BcsPojo{
		public String              	IdTipoComprobanteOrigen;
		public Long					IdComprobanteDetalle;
	}

	@SuppressWarnings("rawtypes")
	@Trace
	public CrearCargoAbonoSalida logica(Connection con, CrearCargoAbonoEntrada in) throws Exception {
		logger.info("Inicio servicio CrearCargoAbono");
		CrearCargoAbonoSalida out = new CrearCargoAbonoSalida();
		out.P_OUT_ERRORESCONTADOR = 0L;
		SQL sql = new SQL();
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		String usaCargoAbonoJava = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.UsaCargoAbonoJava");
		List<ObjProcesamientoCargoAbonoAGenerar> procesamientoCargoAbonoError = new ArrayList<ObjProcesamientoCargoAbonoAGenerar>();
		List<ObjInsCargoAbono> listInsAbono = new ArrayList<ObjInsCargoAbono>();
		List<ObjInsCargoAbono> listInsCargo = new ArrayList<ObjInsCargoAbono>();
		HashMap<String, RequisitoOperativoCrearOrden> requisitos = new HashMap<>();
		List<CuentaInversion> listaCuentaInversion = new ArrayList<CuentaInversion>();

		HashMap<String, Object> paramsQueryConceptoOrden = new HashMap<>(2);
		paramsQueryConceptoOrden.put("Uso", "INGRESOINSTRUCCION");
		Set<String> conceptosInstrucciones = sql.query(con, SQL_SELECT_CONCEPTO_ORDEN, paramsQueryConceptoOrden, ConceptoOrden.class).stream().map(co -> co.IdConcepto).collect(Collectors.toSet());

		{
			HashMap<String,Object> paramsInRequisitosOperativos = new HashMap<>();
			paramsInRequisitosOperativos.put("IdEmpresa", in.p_IdEmpresa);
			List<RequisitoOperativoCrearOrden> _requisitos = sql.query(con, SQL_SELECT_REQUISITOS_OPERATIVOS,paramsInRequisitosOperativos,RequisitoOperativoCrearOrden.class);
			HashMap<Object,Object> __requisitos = Utils.List2Hash(_requisitos, "IdTipoCanalIngreso");
			for(Object key: __requisitos.keySet()) {
				requisitos.put((String)key,(RequisitoOperativoCrearOrden) __requisitos.get(key));
			}
		}
		
		if (usaCargoAbonoJava.equalsIgnoreCase("N")){
			HashMap<String,Object> parmsVacio = new HashMap<String,Object>();
			
			//Escalares de entrada:
			HashMap<String,Object> parmsIn =new HashMap<String,Object>();
			parmsIn.put("p_IdEmpresa", in.p_IdEmpresa);
			parmsIn.put("p_IdUsuario", in.p_IdUsuario);
			parmsIn.put("p_IdModulo", in.p_IdModulo);
			parmsIn.put("p_simularOperacion", in.p_simularOperacion);
			parmsIn.put("p_UsaReglasNegocio", in.p_UsaReglasNegocio);
			parmsIn.put("p_FechaSistema", in.p_FechaSistema);
			parmsIn.put("p_EsIngresoManual", in.p_EsIngresoManual);
			
			//Escalares de salida:
			HashMap<String,Class> parmsOut =new HashMap<String,Class>();
			parmsOut.put("p_out_InformacionContador", Long.class);
			parmsOut.put("p_out_AdvertenciasContador", Long.class);
			parmsOut.put("p_out_ErroresContador", Long.class);
			
			//tablas temporales globales de entrada
			if(in.p_CargoAbonoAGenerar.size()>0){				
				ArrayList<ObjCargoAbonoAGenerar> p_CargoAbonoAGenerar = (ArrayList<ObjCargoAbonoAGenerar>)in.p_CargoAbonoAGenerar;
				sql.batch(con,SQL_INSERT_CARGO_ABONO_A_GENERAR, p_CargoAbonoAGenerar, ObjCargoAbonoAGenerar.class);
			}
			//Realiza el call
			sql.call(con, SQL_CALL_CREA_CARGO_ABONO, parmsIn, parmsOut);

			//Recuperar datos temporales globales de salida
			List<ObjCargoAbonoGenerado> p_out_CargoAbonoGenerado = sql.query(con, SQL_SELECT_CARGO_ABONO_DETALLADO,parmsVacio,ObjCargoAbonoGenerado.class);
			List<ObjInformacionDetalle> p_out_InformacionDetalle = sql.query(con, SQL_SELECT_INFORMACION_DETALLE,parmsVacio,ObjInformacionDetalle.class);
			List<ObjAdvertenciasDetalle> p_out_AdvertenciasDetalle = sql.query(con, SQL_SELECT_ADVERTENCIAS_DETALLE,parmsVacio,ObjAdvertenciasDetalle.class);
			List<ObjErroresDetalle> p_out_ErroresDetalle = sql.query(con, SQL_SELECT_ERRORES_DETALLE,parmsVacio,ObjErroresDetalle.class);
			
			//Asigna Salida
			out.P_OUT_CARGOABONOGENERADO = p_out_CargoAbonoGenerado;
			
			for (ObjInformacionDetalle  Informacion: p_out_InformacionDetalle){
				out.P_OUT_MENSAJES.add(new MensajeSalida(Informacion.IdCargoAbonoMovimiento,MensajeSalida.MENSAJE_OK, Informacion.Codigo, Informacion.Descripcion));
			};
			for (ObjAdvertenciasDetalle  Advertencia: p_out_AdvertenciasDetalle){
				out.P_OUT_MENSAJES.add(new MensajeSalida(Advertencia.NumeroCargoAbono,MensajeSalida.MENSAJE_ADVERTENCIA, Advertencia.Codigo, Advertencia.Descripcion));
			};
			for (ObjErroresDetalle  Errores: p_out_ErroresDetalle){
				out.P_OUT_MENSAJES.add(new MensajeSalida(Errores.NumeroCargoAbono,MensajeSalida.MENSAJE_ERROR, Errores.Codigo, Errores.Descripcion));
			};
			
			out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		} else {
			String fechaDesde = "";
			String fechaHasta = "";
			HashMap<String,Object> validaPortafolioMoneda = new HashMap<String,Object>();
			List<ObjInSumaSaldos> listSumaSaldos = new ArrayList<ObjInSumaSaldos>();
			SaldoFlujoCaja sfj = new SaldoFlujoCaja();
			SaldoFlujoCaja.ServiceEntrada entradaFlujoCaja = new SaldoFlujoCaja.ServiceEntrada();
			List<SaldoFlujoCaja.ObjPORTAFOLIOACONSULTAR> portafolioAConsultar = new ArrayList<SaldoFlujoCaja.ObjPORTAFOLIOACONSULTAR>();
			List<ObjCargoAbonoGenerado> lstCargoAbonoGenerado = new ArrayList<ObjCargoAbonoGenerado>(); 
			//Escalares de entrada:
			HashMap<String,Object> parmsIn =new HashMap<String,Object>();
			parmsIn.put("p_IdEmpresa", in.p_IdEmpresa);
			parmsIn.put("p_IdUsuario", in.p_IdUsuario);
			parmsIn.put("p_IdModulo", in.p_IdModulo);
			parmsIn.put("p_simularOperacion", in.p_simularOperacion);
			parmsIn.put("p_UsaReglasNegocio", in.p_UsaReglasNegocio);
			parmsIn.put("p_FechaSistema", in.p_FechaSistema);
			parmsIn.put("p_EsIngresoManual", in.p_EsIngresoManual);
			
			HashMap<String,String> params = new HashMap<String,String>();
			if(in.p_CargoAbonoAGenerar.size()>0){		
				ArrayList<ObjCargoAbonoAGenerar> p_CargoAbonoAGenerar = (ArrayList<ObjCargoAbonoAGenerar>)in.p_CargoAbonoAGenerar;
				HashMap<Long, ObjCargoAbonoAGenerar> hashCargoAbonoAgenerar = new HashMap<>();

				for(ObjCargoAbonoAGenerar movimiento : p_CargoAbonoAGenerar) {
					hashCargoAbonoAgenerar.put(movimiento.IdOperacion, movimiento);
				}
				
				sql.batch(con,SQL_INSERT_CARGO_ABONO_A_GENERAR, p_CargoAbonoAGenerar, ObjCargoAbonoAGenerar.class);
				parmsIn.clear();
				List<ObjProcesamientoCargoAbonoAGenerar> procesamientoCargoAbono = sql.query(con, SQL_SELECT_DATOS_PROCESAMIENTO, parmsIn, ObjProcesamientoCargoAbonoAGenerar.class);
				HashMap<String, String> mensajeDiccionario = new HashMap<>();
				
				//Agregar Log Crear Cargo Abono 	
				AgregarLog agregarLog = new AgregarLog();
				InAgregarLog entradaLog = new InAgregarLog();
				
				if (procesamientoCargoAbono != null && !procesamientoCargoAbono.isEmpty()) {
					
					List<Long> lstCuentaInversion = procesamientoCargoAbono.stream().map(procesamiento ->{
						return procesamiento.IdCuentaInversion;
					}).distinct().collect(Collectors.toList());
					
					List<ObjCliente> listaCliente = new ArrayList();
					HashMap<Long,Object> hashCliente = new HashMap();
					
					if (!lstCuentaInversion.isEmpty()) {
						HashMap<String,Object> parmsIn2 = new HashMap<String,Object>();
						parmsIn2.put("IdCuentaInversion",lstCuentaInversion);

						listaCliente = sql.query(con, SQL_SELECT_CLIENTE, parmsIn2, ObjCliente.class);
						
						hashCliente = Utils.List2Hash(listaCliente, "IdCuentaInversion");		
					}
					
					for(ObjProcesamientoCargoAbonoAGenerar datosLog : procesamientoCargoAbono) {
						
						ObjCliente cliente = new ObjCliente();
						if(hashCliente.size()>0) {
							cliente = (ObjCliente) hashCliente.get(datosLog.IdCuentaInversion);
						}
					
						//Construir entradas log
						InAgregarLog.Log headLog = new InAgregarLog.Log();
						headLog.NombreProceso = "Crear Cargo Abono";
						headLog.IdCabeceraLog = datosLog.IdCargoAbonoMovimiento != null ? datosLog.IdCargoAbonoMovimiento : 0L;
						headLog.IdTipoLog = "INGRESO";
						headLog.IdModulo = in.p_IdModulo;
						headLog.IdTipoRegistro = "CARGO_ABONO_MOVIMIENTO";
						headLog.IdUsuario = in.p_IdUsuario;
						headLog.IdEmpresa = in.p_IdEmpresa;
						headLog.FechaSistemaLog = fechaSistema != null ? fechaSistema : "";
						headLog.IdUsuarioAtendido = in.p_IdUsuario;
						headLog.IdCliente = (cliente != null && cliente.IdCliente != null) ? cliente.IdCliente : 0L;
						headLog.IdPortafolio = datosLog.IdPortafolio != null ? datosLog.IdPortafolio : 0L;
						headLog.IdTipoOperacion = datosLog.IdTipoMovimiento != null ? datosLog.IdTipoMovimiento : "";
						headLog.Folio = datosLog.IdCargoAbonoMovimiento != null ? datosLog.IdCargoAbonoMovimiento : 0L;
						headLog.Monto = datosLog.Monto != null ? datosLog.Monto : BigDecimal.ZERO;
						headLog.IdMonedaOrigen = datosLog.IdMoneda != null ? datosLog.IdMoneda : "";
					
						if((datosLog.IdTipoMovimiento != null) && (datosLog.IdTipoMovimiento).equals("MOVABN")){
							headLog.IdTipoRegistro = "ABONO_MOVIMIENTO";
						}
						if((datosLog.IdTipoMovimiento != null) && (datosLog.IdTipoMovimiento).equals("MOVCAR")){
							headLog.IdTipoRegistro = "CARGO_MOVIMIENTO";
						}
						
						entradaLog.p_Log.add(headLog);
						
					}	
					//llamar al log
					agregarLog.logica(con, entradaLog);
				}	
				
				for (ObjProcesamientoCargoAbonoAGenerar proc : procesamientoCargoAbono){
					if (fechaDesde.equalsIgnoreCase("")){fechaDesde = proc.FechaCondicionDeLiquidacion; fechaHasta = proc.FechaCondicionDeLiquidacion;}
					mensajeDiccionario.clear();
					ProcesaRequisito(con, in.p_IdEmpresa,(proc.IdPortafolioInformado == null || proc.IdMonedaInformado == null),"CUENTA_INVERSION_INVALIDA",MensajeSalida.MENSAJE_ERROR,proc,requisitos,mensajeDiccionario,out.P_OUT_MENSAJES);
					ProcesaRequisito(con, in.p_IdEmpresa,("S".equalsIgnoreCase(proc.EsCargoAbonoPorTraspaso) && proc.IdPortafolioTraspaso == null),"CUENTA_INVERSION_TRASPASO_INVALIDA",MensajeSalida.MENSAJE_ERROR,proc,requisitos,mensajeDiccionario,out.P_OUT_MENSAJES);
					mensajeDiccionario.clear();
				    mensajeDiccionario.put("NombreCliente", proc.NombreCompleto);
				    mensajeDiccionario.put("NombreMoneda", hashCargoAbonoAgenerar.get(proc.IdOperacion).IdMoneda);
					ProcesaRequisito(con, in.p_IdEmpresa,(proc.IdCuentaInversion == null),"CUENTA_INVERSION_NO_EXISTE",MensajeSalida.MENSAJE_ERROR,proc,requisitos,mensajeDiccionario,out.P_OUT_MENSAJES);
					ProcesaRequisito(con, in.p_IdEmpresa,("INA".equalsIgnoreCase(proc.IdEstado)),"CUENTA_INVERSION_NO_ACTIVA",MensajeSalida.MENSAJE_ERROR,proc,requisitos,mensajeDiccionario,out.P_OUT_MENSAJES);
					mensajeDiccionario.clear();
				    mensajeDiccionario.put("IdTipoMovimiento", hashCargoAbonoAgenerar.get(proc.IdOperacion).IdTipoMovimiento);
					ProcesaRequisito(con, in.p_IdEmpresa,(proc.IdTipoMovimientoInformado == null || proc.IdTipoMovimientoInformado.length() == 0),"TIPO_MOVIMIENTO_INVALIDO",MensajeSalida.MENSAJE_ERROR,proc,requisitos,mensajeDiccionario,out.P_OUT_MENSAJES);
					mensajeDiccionario.clear();
					ProcesaRequisito(con, in.p_IdEmpresa,(proc.IdConceptoInformado == null || proc.IdConceptoInformado.length() == 0),"CONCEPTO_CUENTA_INVERSION_INVALIDO",MensajeSalida.MENSAJE_ERROR,proc,requisitos,mensajeDiccionario,out.P_OUT_MENSAJES);
					mensajeDiccionario.put("NombreCliente", proc.NombreCompleto);
					mensajeDiccionario.put("NombreMoneda", hashCargoAbonoAgenerar.get(proc.IdOperacion).IdMoneda);
					ProcesaRequisito(con, in.p_IdEmpresa,(!conceptosInstrucciones.contains(proc.IdConceptoInformado) && !in.p_IdModulo.equalsIgnoreCase("FMM") && (Fecha.toDate(proc.FechaCondicionDeLiquidacion).compareTo(Fecha.toDate(fechaSistema)) < 0 && !proc.IdConcepto.equalsIgnoreCase("ABNCSC"))),"FECHA_LIQUIDACION_MENOR_FECHA_MOVIMIENTO",MensajeSalida.MENSAJE_ERROR,proc,requisitos,mensajeDiccionario,out.P_OUT_MENSAJES);
					mensajeDiccionario.clear();
					ProcesaRequisito(con, in.p_IdEmpresa,(proc.Monto.compareTo(BigDecimal.ZERO) == -1),"MONTO_MENOR_IGUAL_A_CERO",MensajeSalida.MENSAJE_ERROR,proc,requisitos,mensajeDiccionario,out.P_OUT_MENSAJES);
					ProcesaRequisito(con, in.p_IdEmpresa,(proc.IdTipoComprobante != null && proc.IdTipoComprobante.length() > 0 && (proc.IdComprobante == null || proc.IdComprobante.compareTo(0L) == 0) && (proc.FolioComprobante == null || proc.FolioComprobante.equalsIgnoreCase(""))),"SECUENCIA_O_FOLIO_COMPROBANTE_VACIO",MensajeSalida.MENSAJE_ERROR,proc,requisitos,mensajeDiccionario,out.P_OUT_MENSAJES);
					ProcesaRequisito(con, in.p_IdEmpresa,(proc.IdTipoOperacion != null && proc.IdTipoOperacion.length() > 0 && (proc.IdOperacion == null || proc.IdOperacion.compareTo(0L) == 0)),"FOLIO_OPERACION_VACIO",MensajeSalida.MENSAJE_ERROR,proc,requisitos,mensajeDiccionario,out.P_OUT_MENSAJES);





					mensajeDiccionario.clear();
				    mensajeDiccionario.put("NombreCliente", proc.NombreCompleto);
				    mensajeDiccionario.put("DocumentoIdentidad", proc.DocumentoIdentidad);
	
					HashMap<String,Object> parmsVacio = new HashMap<String,Object>();
					List<InformacionCorredora> resultCosultaCorredora = sql.query(con, SQL_SELECT_Corredora, parmsVacio, InformacionCorredora.class);
					for (InformacionCorredora  Informacion: resultCosultaCorredora){
						if(proc.DocumentoIdentidad.equalsIgnoreCase(Informacion.DocumentoIdentidad)) {
					    	proc.BloqueadoPorCumplimiento = "N";
					    }	    
					}
					
					ProcesaRequisito(con, in.p_IdEmpresa,(("MOVABN").equalsIgnoreCase(proc.IdTipoMovimiento) && ("S").equalsIgnoreCase(proc.BloqueadoPorCumplimiento) && !("FFM").equalsIgnoreCase(proc.IdTipoInstrumento)),"CLIENTE_BLOQUEADO_POR_CUMPLIMIENTO",MensajeSalida.MENSAJE_ERROR,proc,requisitos,mensajeDiccionario,out.P_OUT_MENSAJES);
					
					if (proc.IdTipoMovimiento == null || proc.IdTipoMovimiento.equalsIgnoreCase("")) {
				    	mensajeDiccionario.clear();
				    	ProcesaRequisito(con, in.p_IdEmpresa,(proc.IdTipoMovimiento == null || proc.IdTipoMovimiento.equalsIgnoreCase("")),"CUENTA_INVERSION_TRASPASO_INVALIDA",MensajeSalida.MENSAJE_ERROR,proc,requisitos,mensajeDiccionario,out.P_OUT_MENSAJES);
				    	out.P_OUT_MENSAJES.add(new MensajeSalida(proc.IdInterno,MensajeSalida.MENSAJE_ERROR,"TIPO_MOVIMIENTO_NO_EXISTE",params,con,in.p_IdEmpresa));
				    } else {
				    	if ("MOVCAR".equalsIgnoreCase(proc.IdTipoMovimiento) && Fecha.toDate(proc.FechaCondicionDeLiquidacion).compareTo(Fecha.toDate(fechaDesde)) < 0) fechaDesde = proc.FechaCondicionDeLiquidacion;
						if ("MOVCAR".equalsIgnoreCase(proc.IdTipoMovimiento) && Fecha.toDate(proc.FechaCondicionDeLiquidacion).compareTo(Fecha.toDate(fechaHasta)) > 0) fechaHasta = proc.FechaCondicionDeLiquidacion;
						if ("MOVCAR".equalsIgnoreCase(proc.IdTipoMovimiento) && ("S".equalsIgnoreCase(in.p_EsIngresoManual) || "CMBPTF".equalsIgnoreCase(proc.IdConcepto))) {	
				    		if (!validaPortafolioMoneda.containsKey(proc.IdPortafolio + proc.IdMoneda)){
				    			SaldoFlujoCaja.ObjPORTAFOLIOACONSULTAR portafolio = new SaldoFlujoCaja.ObjPORTAFOLIOACONSULTAR();
				    			portafolio.IdMoneda = proc.IdMoneda;
				    			portafolio.IdPortafolio = proc.IdPortafolio;
				    			portafolioAConsultar.add(portafolio);
				    			validaPortafolioMoneda.put(proc.IdPortafolio + proc.IdMoneda, proc);
				    		}
				    	}
				    }
				    mensajeDiccionario.clear();
				    mensajeDiccionario.put("IdConcepto", hashCargoAbonoAgenerar.get(proc.IdOperacion).IdConcepto);
				    ProcesaRequisito(con, in.p_IdEmpresa,(proc.IdConcepto == null || "".equalsIgnoreCase(proc.IdConcepto)),"CONCEPTO_CUENTA_INVERSION_NO_EXISTE",MensajeSalida.MENSAJE_ERROR,proc,requisitos, mensajeDiccionario,out.P_OUT_MENSAJES);
				    mensajeDiccionario.clear();
				    mensajeDiccionario.put("IdMoneda", hashCargoAbonoAgenerar.get(proc.IdOperacion).IdMoneda);
				    ProcesaRequisito(con, in.p_IdEmpresa,(proc.IdMonedaTesoreria.equalsIgnoreCase("")),"MONEDA_NO_EXISTE",MensajeSalida.MENSAJE_ERROR,proc,requisitos,mensajeDiccionario,out.P_OUT_MENSAJES);
				    ProcesaRequisito(con, in.p_IdEmpresa,(proc.IdEstadoMonedaTesoreria.equalsIgnoreCase("INA")),"MONEDA_NO_ACTIVA",MensajeSalida.MENSAJE_ERROR,proc,requisitos,mensajeDiccionario,out.P_OUT_MENSAJES);
				    mensajeDiccionario.clear();
				    mensajeDiccionario.put("IdTipoOperacion", hashCargoAbonoAgenerar.get(proc.IdOperacion).IdTipoOperacion);
				    ProcesaRequisito(con, in.p_IdEmpresa,(proc.IdTipoOperacionInformado != null && proc.IdTipoOperacionInformado.length() > 0 && proc.IdTipoOperacion != null && proc.IdTipoOperacion.equalsIgnoreCase("")),"TIPO_OPERACION_NO_EXISTE",MensajeSalida.MENSAJE_ERROR,proc,requisitos,mensajeDiccionario,out.P_OUT_MENSAJES);
				    mensajeDiccionario.clear();
				    mensajeDiccionario.put("IdProducto", hashCargoAbonoAgenerar.get(proc.IdOperacion).IdTipoProducto);
				    ProcesaRequisito(con, in.p_IdEmpresa,(proc.IdTipoProductoInformado != null && proc.IdTipoProductoInformado.length() > 0 && proc.IdTipoProducto != null && proc.IdTipoProducto.equalsIgnoreCase("")),"PRODUCTO_NO_EXISTE",MensajeSalida.MENSAJE_ERROR,proc,requisitos,mensajeDiccionario,out.P_OUT_MENSAJES);
				    mensajeDiccionario.clear();
				    mensajeDiccionario.put("IdTipoInstrumento", hashCargoAbonoAgenerar.get(proc.IdOperacion).IdTipoInstrumento);
				    ProcesaRequisito(con, in.p_IdEmpresa,(proc.IdTipoInstrumentoInformado != null && proc.IdTipoInstrumentoInformado.length() > 0 && proc.IdTipoInstrumento != null && proc.IdTipoInstrumento.equalsIgnoreCase("")),"TIPO_INSTRUMENTO_NO_EXISTE",MensajeSalida.MENSAJE_ERROR,proc,requisitos,mensajeDiccionario,out.P_OUT_MENSAJES);
				}
				limpiarCargosAbonos(procesamientoCargoAbono,procesamientoCargoAbonoError);					
				List<ObjProcesamientoCargoAbonoAGenerar> procesamientoAbono = procesamientoCargoAbono.stream().filter(c -> c.IdTipoMovimiento.equals("MOVABN")).collect(Collectors.toList());
				List<ObjProcesamientoCargoAbonoAGenerar> procesamientoCargo = procesamientoCargoAbono.stream().filter(c -> c.IdTipoMovimiento.equals("MOVCAR")).collect(Collectors.toList());
				
				entradaFlujoCaja.P_IDEMPRESA = in.p_IdEmpresa;
				entradaFlujoCaja.P_FECHADESDE = fechaDesde;
				entradaFlujoCaja.P_FECHAHASTA = fechaHasta;
				entradaFlujoCaja.P_USO = "CUENTA_INVERSION";
				
				for(ObjProcesamientoCargoAbonoAGenerar  cargo : procesamientoCargo ) {
					if("MOVCAR".equalsIgnoreCase(cargo.IdTipoMovimiento) && ("S".equalsIgnoreCase(in.p_EsIngresoManual) || "CMBPTF".equalsIgnoreCase(cargo.IdConcepto))) {
						entradaFlujoCaja.P_PORTAFOLIOACONSULTAR.addAll( portafolioAConsultar);
					}
				}
				
				if(entradaFlujoCaja.P_PORTAFOLIOACONSULTAR.size() > 0) {
					SaldoFlujoCaja.ServiceSalida salidaSaldoFlujoCaja = sfj.logica(con, entradaFlujoCaja);
					List<SaldoFlujoCaja.ObjFLUJOCAJA> flujosCaja = salidaSaldoFlujoCaja.P_OUT_FLUJOCAJA;
					
					if(flujosCaja.size() > 0 ) {
						List<MensajeSalida> errores = new ArrayList<MensajeSalida>();
						for(SaldoFlujoCaja.ObjFLUJOCAJA flujo : flujosCaja) {
							for(ObjProcesamientoCargoAbonoAGenerar  cargo : procesamientoCargo ) {
								if( cargo.Monto.compareTo(flujo.Saldo) == 1  && cargo.IdConcepto.equals("CMBPTF") && ("MOVCAR").equals(cargo.IdTipoMovimiento) && cargo.FechaCondicionDeLiquidacion.equals(flujo.FechaSaldo) && cargo.IdPortafolio.equals(flujo.IdPortafolio)) {
									HashMap<String,String> mapMsg = new HashMap<>(2);
									mapMsg.put("CodigoPortafolio",cargo.CodigoPortafoliCargo);
									mapMsg.put("DocumentoIdentidad",cargo.DocumentoIdentidad);
									
									errores.add( new MensajeSalida(cargo.IdInterno,MensajeSalida.MENSAJE_ERROR,"UI_SALDO_INSUFICIENTE_FLUJO_CAJA",mapMsg,con,in.p_IdEmpresa));
								}
							}
						}
						
						if(errores.size() > 0) {
							out.P_OUT_MENSAJES.addAll(errores);
							out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
							return out;
						}
					}
				}
				
				HashMap<Long,Object> procHash = Utils.List2Hash(procesamientoCargoAbono, "KeyTraspaso");
				
				listInsAbono = creaListaCargoAbono(con,in ,procesamientoAbono,procHash,fechaSistema);			
				//hacer if simula	
				if(("N").equals(in.p_simularOperacion)) {
					sql.batch(con,SQL_INSERT_CARGO_ABONO,listInsAbono,ObjInsCargoAbono.class);
				}
				
				for (ObjInsCargoAbono cargoAbonoIns : listInsAbono){
					ObjCargoAbonoGenerado objCargoAbonoGenerado = new ObjCargoAbonoGenerado();
					objCargoAbonoGenerado.IdCargoAbonoMovimiento = cargoAbonoIns.IdCargoAbonoMovimiento;
					objCargoAbonoGenerado.IdTipoMovimiento = cargoAbonoIns.IdTipoMovimiento;
					objCargoAbonoGenerado.NumeroCargoAbono = cargoAbonoIns.NumeroCargoAbono;
					lstCargoAbonoGenerado.add(objCargoAbonoGenerado);
					String mensaje = (cargoAbonoIns.IdTipoMovimiento.equals("MOVABN") ? "Abono N° " : "Cargo N° ") + cargoAbonoIns.IdCargoAbonoMovimiento + (cargoAbonoIns.IdEstado.equalsIgnoreCase("PEN") ? " creado pendiente de certificacion" : " creado"); 
					out.P_OUT_MENSAJES.add(new MensajeSalida(cargoAbonoIns.IdInterno,(cargoAbonoIns.IdEstado.equalsIgnoreCase("CRT") ? MensajeSalida.MENSAJE_INFORMACION : MensajeSalida.MENSAJE_ADVERTENCIA),"ABONO_CARGO_CREADO",mensaje));
				}
				
				if (procesamientoCargo.size() >0 && portafolioAConsultar.size() > 0 && "S".equalsIgnoreCase(in.p_EsIngresoManual)){
					entradaFlujoCaja.P_IDEMPRESA = in.p_IdEmpresa;
					entradaFlujoCaja.P_FECHADESDE = fechaDesde;
					entradaFlujoCaja.P_FECHAHASTA = fechaHasta;
					entradaFlujoCaja.P_USO = "CUENTA_INVERSION";
					entradaFlujoCaja.P_PORTAFOLIOACONSULTAR = portafolioAConsultar;
					SaldoFlujoCaja.ServiceSalida outSaldoFlujoCaja = sfj.logica(con, entradaFlujoCaja);
					List<SaldoFlujoCaja.ObjFLUJOCAJA> listFlujoCaja = outSaldoFlujoCaja.P_OUT_FLUJOCAJA;
					
					for (SaldoFlujoCaja.ObjFLUJOCAJA obj : listFlujoCaja){
						ObjInSumaSaldos objSumaSaldos = new ObjInSumaSaldos();
						objSumaSaldos.IdPortafolio = obj.IdPortafolio;
						objSumaSaldos.IdMoneda = obj.IdMoneda;
						objSumaSaldos.Saldo = MonedaFormato.formatear(con, obj.Saldo, MonedaFormato.MONTO, obj.IdMoneda).valor;
						objSumaSaldos.FechaSaldo = obj.FechaSaldo;
						listSumaSaldos.add(objSumaSaldos);
					}
					sql.batch(con,SQL_INSERT_SALDOS, listSumaSaldos, ObjInSumaSaldos.class);
					List<ObjSumaSaldos> listSaldosFinales = sql.query(con,SQL_SELECT_SUMA_SALDOS,new HashMap<String,Object>(),ObjSumaSaldos.class);
					HashMap<Long,Object> hashSaldosFinales = Utils.List2Hash(listSaldosFinales, "IdInterno");
					
					for (ObjProcesamientoCargoAbonoAGenerar proc : procesamientoCargo){							
						if ("MOVCAR".equalsIgnoreCase(proc.IdTipoMovimiento) && "S".equalsIgnoreCase(proc.OperaCuentaInversion)){
							
							ObjSumaSaldos obj = (ObjSumaSaldos) hashSaldosFinales.get(proc.IdInterno);
							mensajeDiccionario.clear();
							ProcesaRequisito(con, in.p_IdEmpresa,(outSaldoFlujoCaja.P_OUT_ERRORESCONTADOR > 0L),"ERROR_CONSULTA_FLUJO_CAJA",MensajeSalida.MENSAJE_ERROR,proc,requisitos,mensajeDiccionario,out.P_OUT_MENSAJES);  
							mensajeDiccionario.clear();
						    mensajeDiccionario.put("NombreCompleto", proc.NombreCompleto);
						    mensajeDiccionario.put("DocumentoIdentidad", proc.DocumentoIdentidad);

						    ProcesaRequisito(con, in.p_IdEmpresa,(outSaldoFlujoCaja.P_OUT_ERRORESCONTADOR == 0L && obj.SaldoFinal.compareTo(BigDecimal.ZERO) == -1),"SALDO_CAJA_INSUFICIENTE",MensajeSalida.MENSAJE_ERROR,proc,requisitos,mensajeDiccionario,out.P_OUT_MENSAJES);
						   
						}
					}
				}

				limpiarCargosAbonos(procesamientoCargo, procesamientoCargoAbonoError);
				
				listInsCargo = creaListaCargoAbono(con,in ,procesamientoCargo,procHash,fechaSistema);
				//hacer if simula
				if(("N").equals(in.p_simularOperacion)) {
					sql.batch(con,SQL_INSERT_CARGO_ABONO,listInsCargo,ObjInsCargoAbono.class);
				}

				for (ObjInsCargoAbono cargoAbonoIns : listInsCargo){
					ObjCargoAbonoGenerado objCargoAbonoGenerado = new ObjCargoAbonoGenerado();
					objCargoAbonoGenerado.IdCargoAbonoMovimiento = cargoAbonoIns.IdCargoAbonoMovimiento;
					objCargoAbonoGenerado.IdTipoMovimiento = cargoAbonoIns.IdTipoMovimiento;
					objCargoAbonoGenerado.NumeroCargoAbono = cargoAbonoIns.NumeroCargoAbono;
					lstCargoAbonoGenerado.add(objCargoAbonoGenerado);
					String mensaje = (cargoAbonoIns.IdTipoMovimiento.equals("MOVABN") ? "Abono N° " : "Cargo N° ") + cargoAbonoIns.IdCargoAbonoMovimiento + (cargoAbonoIns.IdEstado.equalsIgnoreCase("PEN") ? " creado pendiente de certificacion" : " creado"); 
					out.P_OUT_MENSAJES.add(new MensajeSalida(cargoAbonoIns.IdInterno,(cargoAbonoIns.IdEstado.equalsIgnoreCase("CRT") ? MensajeSalida.MENSAJE_INFORMACION : MensajeSalida.MENSAJE_ADVERTENCIA),"ABONO_CARGO_CREADO",mensaje));
				}
				out.P_OUT_CARGOABONOGENERADO = lstCargoAbonoGenerado;
								
				// Actualizar Saldo Cargo Abono
				// ----------------------------------------------------------				
				 
				//si es cuenta propia llamar job 
				HashMap<String,Object> parmsConsultaCuentaPropia =new HashMap<String,Object>();
				parmsConsultaCuentaPropia.put("p_IdEmpresa", in.p_IdEmpresa);
				List<ConsultaCuentaPropia_Result> p_out_ConsultaCuentaPropia = sql.query(con, SQL_SELECT_CUENTA_PROPIA,parmsConsultaCuentaPropia,ConsultaCuentaPropia_Result.class);
				
				//separar cuentas propias de terceros
				List<ObjInsCargoAbono> listInsAbonoPropia = new ArrayList<ObjInsCargoAbono>();
				List<ObjInsCargoAbono> listInsCargoPropia = new ArrayList<ObjInsCargoAbono>();
				
				List<ObjInsCargoAbono> listInsAbonoTercero = new ArrayList<ObjInsCargoAbono>();
				List<ObjInsCargoAbono> listInsCargoTercero = new ArrayList<ObjInsCargoAbono>();
				
				listInsAbonoPropia = listInsAbono.stream()
						.filter(abono -> esCuentaPropia(p_out_ConsultaCuentaPropia, abono.IdCuentaInversion))
						.collect(Collectors.toList());
				
				listInsCargoPropia = listInsCargo.stream()
						.filter(cargo -> esCuentaPropia(p_out_ConsultaCuentaPropia, cargo.IdCuentaInversion))
						.collect(Collectors.toList());
				
				listInsAbonoTercero = listInsAbono.stream()
						.filter(abono -> !esCuentaPropia(p_out_ConsultaCuentaPropia, abono.IdCuentaInversion))
						.collect(Collectors.toList());
	
				listInsCargoTercero = listInsCargo.stream()
						.filter(cargo -> !esCuentaPropia(p_out_ConsultaCuentaPropia, cargo.IdCuentaInversion))
						.collect(Collectors.toList());

				if (listInsAbonoPropia.size() > 0 || listInsCargoPropia.size() > 0  )
				{
					//llamar job
					AgregarPrograma agregarPrograma = new AgregarPrograma();
	    		   	AgregarPrograma.InAgregarPrograma agregarProgramaEntrada = new AgregarPrograma.InAgregarPrograma();
	    		   	agregarProgramaEntrada.p_IdEmpresa = in.p_IdEmpresa;
	    		   	agregarProgramaEntrada.p_IdUsuario = in.p_IdUsuario;
	    		   	agregarProgramaEntrada.p_IdModulo = in.p_IdModulo;
	    		   	agregarProgramaEntrada.p_FechaSistema = in.p_FechaSistema;
	    		   	agregarProgramaEntrada.p_IdTipoPrograma = "ACTUALIZAR_SALDO_CARGO_ABONO";
	    		   	agregarProgramaEntrada.p_Segundos = 0L; //delay de 30 segundos.	
	    		   	
	    		   	List<String> cuentas = new ArrayList<String>();
	    		   	for (ObjInsCargoAbono cargoAbonoIns : listInsAbonoPropia){
						if (!cuentas.contains(cargoAbonoIns.IdCuentaInversion.toString())) cuentas.add(cargoAbonoIns.IdCuentaInversion.toString());
					}
					for (ObjInsCargoAbono cargoAbonoIns : listInsCargoPropia){
						if (!cuentas.contains(cargoAbonoIns.IdCuentaInversion.toString())) cuentas.add(cargoAbonoIns.IdCuentaInversion.toString());
					}
	    		   	
	    			String cuentasJob = StringUtils.join(";",cuentas);
	    			//String cuentasJobB64 =  Base64.getEncoder().encodeToString(cuentasJob.getBytes());
	    			
	    			Parametro jobParametro = new Parametro();
	    			jobParametro.NombreParametro = "CuentasInversion";
	    			jobParametro.Valor = cuentasJob;
	    			
	    			agregarProgramaEntrada.p_Parametros.add(jobParametro);
	    			
	    			jobParametro = new Parametro();
	    			jobParametro.NombreParametro = "IdEmpresa";
	    			jobParametro.Valor = in.p_IdEmpresa.toString();
	    			
	    			agregarProgramaEntrada.p_Parametros.add(jobParametro);
	    			
	    			jobParametro = new Parametro();
	    			jobParametro.NombreParametro = "IdUsuario";
	    			jobParametro.Valor = in.p_IdUsuario.toString();
	    			
	    			agregarProgramaEntrada.p_Parametros.add(jobParametro);
	    			
	    			jobParametro = new Parametro();
	    			jobParametro.NombreParametro = "FechaSistema";
	    			jobParametro.Valor = in.p_FechaSistema;
	    			
	    			agregarProgramaEntrada.p_Parametros.add(jobParametro);
	    			    			
	    			

	    		   	AgregarPrograma.OutAgregarPrograma agregarProgramaSalida = agregarPrograma.logica(con, agregarProgramaEntrada);
	    		   	
	    		   	if(agregarProgramaSalida.P_OUT_ERRORESCONTADOR>0){
	    		   		logger.warn("Error agregando programa job");
	    		   	}else{
	    		   		logger.info("Programa job agregado exitosamente");
	    		   	}
				}

				if (listInsAbonoTercero.size() > 0 || listInsCargoTercero.size() > 0  )
				{
					for (ObjInsCargoAbono cargoAbonoIns : listInsAbonoTercero){
						CuentaInversion cuenta_inversion = new CuentaInversion();
						cuenta_inversion.IdCuentaInversion = cargoAbonoIns.IdCuentaInversion;
					    if (!listaCuentaInversion.contains(cuenta_inversion)) listaCuentaInversion.add(cuenta_inversion);
					}
					for (ObjInsCargoAbono cargoAbonoIns : listInsCargoTercero){
						CuentaInversion cuenta_inversion = new CuentaInversion();
						cuenta_inversion.IdCuentaInversion = cargoAbonoIns.IdCuentaInversion;
					    if (!listaCuentaInversion.contains(cuenta_inversion)) listaCuentaInversion.add(cuenta_inversion);
					}
					   
				    if(("N").equals(in.p_simularOperacion)) {
					    ActualizarSaldoCargoAbono actualizarSaldoCargoAbono = new ActualizarSaldoCargoAbono();
					    ActualizarSaldoCargoAbonoEntrada entradaActualizarSaldoCargoAbonoEntrada = new ActualizarSaldoCargoAbonoEntrada(); 
					    entradaActualizarSaldoCargoAbonoEntrada.p_IdEmpresa = in.p_IdEmpresa;
					    entradaActualizarSaldoCargoAbonoEntrada.p_IdUsuario = in.p_IdUsuario;
					    entradaActualizarSaldoCargoAbonoEntrada.p_FechaMovimiento = in.p_FechaSistema;  
					    entradaActualizarSaldoCargoAbonoEntrada.p_CuentaInversion.addAll(listaCuentaInversion);
					    actualizarSaldoCargoAbono.logica(con, entradaActualizarSaldoCargoAbonoEntrada);	
				    }
				} // if (listInsCargo.size() > 0)
				
				
			    SQL.exec(con,SQL_DELETE_CARGO_ABONO_SALDOS_PASO);
			    SQL.exec(con,SQL_DELETE_CARGO_ABONO_GENERAR_PASO);	    
			}	
		}
		
		out.P_OUT_ERRORESCONTADOR=MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		logger.info("Fin servicio CrearCargoAbono");
		return out;
	};
	
	public boolean esCuentaPropia (List<ConsultaCuentaPropia_Result> cuentasPropias, Long idCuenta) {
		return cuentasPropias.stream().filter(cuenta -> cuenta.IdCuentaInversion.equals(idCuenta)).findFirst().isPresent();
	}
	
	public List<ObjInsCargoAbono> creaListaCargoAbono (Connection con, CrearCargoAbonoEntrada in ,List<ObjProcesamientoCargoAbonoAGenerar> procesamientoCargoAbono, HashMap<Long,Object> procHash, String fechaSistema ) throws Exception{
		
		List<ObjInsCargoAbono> listInsCargoAbono = new ArrayList<ObjInsCargoAbono>();
		Timestamp fechaHoraSistema = FechaDB.ObtenerFechaHoraSistemaTs(con, in.p_IdEmpresa);
		Integer zonaHoraria = FechaDB.ObtenerSegundosZonaHoraria(con, in.p_IdEmpresa);
		Timestamp fechaHora = FechaDB.ObtenerFechaHoraUTCTs(con);
		Timestamp fechaLocalTS = Fecha.addSeconds(fechaHora, zonaHoraria);
		String fechaLocal = Fecha.toStringDate(fechaLocalTS);
		String horaUTC = Fecha.toStringTime(Fecha.toTime(Fecha.toFormat(fechaHora,"HH:mm:ss").toString()));
		Timestamp fechaLocalHoraUTC = Fecha.toTimeStamp(fechaLocal+" "+horaUTC);
		ObtenerDiaSiguiente diaSgte = new ObtenerDiaSiguiente();
		InObtenerDiaSiguiente diaSgteIn = new InObtenerDiaSiguiente();
		HashMap<String, HashMap<String, String>> configuraciones = Utils.getConfiguraciones(con, in.p_IdEmpresa);
		long idPais = Long.valueOf(configuraciones.get("PLATAFORMA").get("PLA.PaisSistema"));
		boolean fechaLocalEsDiaHabil = Feriados.esDiaHabil(con, fechaLocal, idPais);
		
		if(!fechaLocalEsDiaHabil) {
			diaSgteIn.p_idPais = idPais;
			diaSgteIn.p_diaHabil = "S";
			diaSgteIn.p_fechas.add(new FechasIn(fechaLocal, 1L, idPais));
			fechaLocal = diaSgte.logica(con, diaSgteIn).p_out_fechas.stream().findFirst().get().fechaCalculada;
			
			fechaLocalHoraUTC = Fecha.addSeconds(Fecha.toTimeStamp(fechaLocal + " 00:01:00"), zonaHoraria * -1);
		}
		
		SQL sql2 = new SQL();
		List<ObjComprobanteDetalle> listaIdTipoComprobanteOrigen = new ArrayList<ObjComprobanteDetalle>();
		HashMap<Long,Object> hashIdTipoComprobanteOrigen = new HashMap<Long, Object>();
				
		if (procesamientoCargoAbono != null && !procesamientoCargoAbono.isEmpty()) {
			List<Long> lstComprobanteDetalle = procesamientoCargoAbono.stream().map(procesamiento ->{
				return procesamiento.IdComprobanteDetalle;
			}).distinct().collect(Collectors.toList());
							
			if (!lstComprobanteDetalle.isEmpty()) {
				HashMap<String,Object> parmsIn2 = new HashMap<String,Object>();
				parmsIn2.put("IdComprobanteDetalle",lstComprobanteDetalle);
				listaIdTipoComprobanteOrigen = sql2.query(con, SQL_SELECT_IDTIPOCOMPROBANTEORIGEN, parmsIn2, ObjComprobanteDetalle.class);
				hashIdTipoComprobanteOrigen = Utils.List2Hash(listaIdTipoComprobanteOrigen, "IdComprobanteDetalle");		
			}
		}
		
		for (ObjProcesamientoCargoAbonoAGenerar cargoAbonoIns : procesamientoCargoAbono){
			
			ObjInsCargoAbono 					objIns 		= new ObjInsCargoAbono();

			ObjProcesamientoCargoAbonoAGenerar 	objProcHash = (ObjProcesamientoCargoAbonoAGenerar) procHash.get(cargoAbonoIns.KeyTraspaso * -1);
			
			objIns.IdInterno 				= cargoAbonoIns.IdInterno;
			objIns.IdCargoAbonoMovimiento 	= cargoAbonoIns.IdCargoAbonoMovimiento;
			objIns.NumeroCargoAbono			= cargoAbonoIns.IdInterno;
			objIns.IdModulo 				= in.p_IdModulo;
			objIns.IdTipoMovimiento 		= cargoAbonoIns.IdTipoMovimiento;
			objIns.EsIngresoManual 			= in.p_EsIngresoManual;
			objIns.IdCuentaInversion 		= cargoAbonoIns.IdCuentaInversion;
			objIns.NombreCompleto 			= cargoAbonoIns.NombreCompleto;
			objIns.IdConcepto 				= cargoAbonoIns.IdConcepto;
			objIns.Descripcion 				= cargoAbonoIns.Descripcion;
			objIns.IdCondicionDeLiquidacion = cargoAbonoIns.IdCondicionDeLiquidacion;
			objIns.FechaCondicionDeLiquidacion = Fecha.toDate(cargoAbonoIns.FechaCondicionDeLiquidacion); 
			objIns.Monto 					= cargoAbonoIns.Monto;
			objIns.Saldo 					= BigDecimal.ZERO;
			objIns.SaldoPorFechaMovimiento 	= BigDecimal.ZERO;
			objIns.SaldoLibroInventario 	= in.p_EsIngresoManual.equalsIgnoreCase("S") ? BigDecimal.ZERO : cargoAbonoIns.Monto;
			objIns.EsCertificadoAutomatico 	= cargoAbonoIns.CertificadoAutomatico;			
			objIns.IdEjecutivo 				= cargoAbonoIns.IdEjecutivo;
			objIns.IdSucursal 				= cargoAbonoIns.IdSucursal != null ? cargoAbonoIns.IdSucursal : 0L;
			objIns.IdTipoComprobante 		= cargoAbonoIns.IdTipoComprobante;
			objIns.IdComprobante 			= cargoAbonoIns.IdComprobante;
			objIns.FolioComprobante 		= cargoAbonoIns.FolioComprobante;
			objIns.IdComprobanteDetalle 	= cargoAbonoIns.IdComprobanteDetalle;		
			objIns.IdComprobanteFormaDePago = cargoAbonoIns.IdComprobanteFormaDePago;
			objIns.IdTipoMovimientoAsociado = cargoAbonoIns.IdTipoMovimientoAsociado;
			objIns.IdMovimientoAsociado 	= cargoAbonoIns.IdMovimientoAsociado;
			objIns.FechaComprobante 		= Fecha.toDate(cargoAbonoIns.FechaComprobante != null ? cargoAbonoIns.FechaComprobante : "0001-01-01");
			objIns.IdTipoProducto 			= cargoAbonoIns.IdTipoProducto != null ? cargoAbonoIns.IdTipoProducto : "";
			objIns.IdTipoOperacion 			= cargoAbonoIns.IdTipoOperacion != null ? cargoAbonoIns.IdTipoOperacion : "";
			objIns.IdOperacion 				= cargoAbonoIns.IdOperacion != null ? cargoAbonoIns.IdOperacion : 0L;
			objIns.FechaOperacion 			= Fecha.toDate(cargoAbonoIns.FechaOperacion != null ? cargoAbonoIns.FechaOperacion : "0001-01-01");
			objIns.FechaLiquidacionOperacion = Fecha.toDate(cargoAbonoIns.FechaLiquidacionOperacion != null ? cargoAbonoIns.FechaLiquidacionOperacion : "0001-01-01");
			objIns.FechaVencimientoOperacion = Fecha.toDate(cargoAbonoIns.FechaVencimientoOperacion != null ? cargoAbonoIns.FechaVencimientoOperacion : "0001-01-01");
			objIns.IdMonedaOperacion 		= cargoAbonoIns.IdMonedaOperacion;
			objIns.MontoOperacion 			= cargoAbonoIns.MontoOperacion;
			objIns.TipoCambioOperacion 		= cargoAbonoIns.TipoCambioOperacion;
			objIns.IdInstrumento 			= cargoAbonoIns.IdInstrumento;
			objIns.IdTipoInstrumento 		= cargoAbonoIns.IdTipoInstrumento != null ? cargoAbonoIns.IdTipoInstrumento : "";
			objIns.Nemo = cargoAbonoIns.Nemo != null ? cargoAbonoIns.Nemo : "";
			objIns.Cantidad 				= cargoAbonoIns.Cantidad != null ? cargoAbonoIns.Cantidad : BigDecimal.ZERO;
			objIns.EsCargoAbonoPorTraspaso 	= cargoAbonoIns.EsCargoAbonoPorTraspaso != null ? cargoAbonoIns.EsCargoAbonoPorTraspaso : "";
			objIns.EsIndicadorBilateral 	= cargoAbonoIns.EsIndicadorBilateral != null ? cargoAbonoIns.EsIndicadorBilateral : "";
			objIns.GeneroContraparteEnTesoreria = cargoAbonoIns.GeneroContraparteEnTesoreria != null ? cargoAbonoIns.GeneroContraparteEnTesoreria : "";
			objIns.IdContraparte 			= cargoAbonoIns.IdContraparte != null ? cargoAbonoIns.IdContraparte : 0;
			objIns.IdPersonaPagoCobro 		= cargoAbonoIns.IdPersonaPagoCobro != null ? cargoAbonoIns.IdPersonaPagoCobro : 0;
			objIns.IdUsuario 				= in.p_IdUsuario;
			objIns.IdEmpresa 				= in.p_IdEmpresa;
			objIns.IdEstado 				= ((cargoAbonoIns.CertificadoAutomatico.equalsIgnoreCase("N") && cargoAbonoIns.autorizaciones == 0) || cargoAbonoIns.autorizaciones > 0) ? "PEN" : "CRT";
			objIns.IdCargoAbonoRelacionadoTraspaso = objProcHash != null ? objProcHash.IdCargoAbonoMovimiento : 0L;
			
			ObjComprobanteDetalle comprobanteDetalle = new ObjComprobanteDetalle();
			if(hashIdTipoComprobanteOrigen.size()>0) {
				comprobanteDetalle = (ObjComprobanteDetalle) hashIdTipoComprobanteOrigen.get(cargoAbonoIns.IdComprobanteDetalle);
			}
			
			boolean esCSC = comprobanteDetalle != null && comprobanteDetalle.IdComprobanteDetalle != null && "CSC".equalsIgnoreCase(comprobanteDetalle.IdTipoComprobanteOrigen);
			
			objIns.FechaSistema = esCSC ? Fecha.toDate(fechaLocal) : Fecha.toDate(fechaSistema);
			objIns.FechaCertificacion = esCSC ? fechaLocalHoraUTC : in.p_EsIngresoManual.equalsIgnoreCase("S") ? Fecha.toTimeStamp("0001-01-01 00:00:00") : fechaHoraSistema;
			
			listInsCargoAbono.add(objIns);
		}		
		
		return listInsCargoAbono;		
	}
	
	public void limpiarCargosAbonos(List<ObjProcesamientoCargoAbonoAGenerar> cargosabonos, List<ObjProcesamientoCargoAbonoAGenerar> cargosabonosConErrores) {
		for (ObjProcesamientoCargoAbonoAGenerar ca : cargosabonos) {
			if (ca.errores > 0) {
				cargosabonosConErrores.add(ca);
			}
		}
		
		for (ObjProcesamientoCargoAbonoAGenerar ca : cargosabonosConErrores) {
			if (cargosabonos.contains(ca))
				cargosabonos.remove(ca);
		}
	}
	
	private void ProcesaRequisito(Connection con,Long idEmpresa,boolean esError,String codMsg,String CodRequisito,ObjProcesamientoCargoAbonoAGenerar cargoAbono,HashMap<String, RequisitoOperativoCrearOrden> requisitos, HashMap<String, String> mensajeDiccionario, List<MensajeSalida> P_OUT_MENSAJES) 
			throws Exception {
		if (esError) {
			if (CodRequisito.equals(MensajeSalida.MENSAJE_ERROR)) {
				P_OUT_MENSAJES.add(new MensajeSalida(cargoAbono.IdInterno, MensajeSalida.MENSAJE_ERROR, codMsg, mensajeDiccionario, con, idEmpresa));
				cargoAbono.errores++;					
			}else if (CodRequisito.equals(MensajeSalida.MENSAJE_AUTORIZACION)) {
				P_OUT_MENSAJES.add(new MensajeSalida(cargoAbono.IdInterno, MensajeSalida.MENSAJE_AUTORIZACION, codMsg, mensajeDiccionario, con, idEmpresa));
				cargoAbono.autorizaciones++;						
			}
			else if (CodRequisito.equals(MensajeSalida.MENSAJE_ADVERTENCIA)) {
				P_OUT_MENSAJES.add(new MensajeSalida(cargoAbono.IdInterno, MensajeSalida.MENSAJE_ADVERTENCIA, codMsg, mensajeDiccionario, con, idEmpresa));
				cargoAbono.advertencias++;
			}
			else {
				//si el comportamiento viene de requisitos operativos
				String requisito=obtenerValorRequisitoOperativo(requisitos, "AGE", CodRequisito);
				if (requisito.equals(MensajeSalida.MENSAJE_ERROR) || requisito.equals(MensajeSalida.MENSAJE_RECHAZO)) { //TODO ELIMINAR MENSAJE RECHAZO
					P_OUT_MENSAJES.add(new MensajeSalida(cargoAbono.IdInterno, MensajeSalida.MENSAJE_ERROR, codMsg, mensajeDiccionario, con, idEmpresa));
					cargoAbono.errores++;
				}
				else if (requisito.equals(MensajeSalida.MENSAJE_AUTORIZACION)) {
					P_OUT_MENSAJES.add(new MensajeSalida(cargoAbono.IdInterno, MensajeSalida.MENSAJE_AUTORIZACION, codMsg, mensajeDiccionario, con, idEmpresa));
					cargoAbono.autorizaciones++;
				}
				else if (requisito.equals(MensajeSalida.MENSAJE_ADVERTENCIA)) {
					P_OUT_MENSAJES.add(new MensajeSalida(cargoAbono.IdInterno, MensajeSalida.MENSAJE_ADVERTENCIA, codMsg, mensajeDiccionario, con, idEmpresa));
					cargoAbono.advertencias++;
				}
				else {
					cargoAbono.ok++;
				}
			}
		}else {
			cargoAbono.ok++;
		}		
	}
	
	public String obtenerValorRequisitoOperativo(HashMap<String, RequisitoOperativoCrearOrden> requisitos,String IdTipoCanalIngreso,String requisitoOperativo) throws IllegalArgumentException, IllegalAccessException {
		if (requisitos.get(IdTipoCanalIngreso) == null &&  requisitos.get("0") == null) return "OK";
		RequisitoOperativoCrearOrden reqCanal = requisitos.get(IdTipoCanalIngreso);
		RequisitoOperativoCrearOrden reqTodos = requisitos.get("0");
		Field[] fields = reqCanal.getClass().getDeclaredFields();
		for (Field f: fields) {
			if (f.getName().equalsIgnoreCase(requisitoOperativo)) {
				String requisitoCanal = (String) f.get(reqCanal);
				String requisitoTodos = null;
				if (f.get(reqTodos)!= null) {
					requisitoTodos = (String) f.get(reqTodos);
				} else {
					requisitoTodos = null;
				}
				if (requisitoCanal != null){
					return requisitoCanal;
				}
				if (requisitoTodos != null){
					return requisitoTodos;
				}
				else {
					return "OK";
				}
			}
		}
		return "";
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();		
		CrearCargoAbonoEntrada jin=gson.fromJson(in.toString() ,CrearCargoAbonoEntrada.class);
		CrearCargoAbonoSalida out=logica(conn,jin);
		
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	};
	
	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();			
			//Gson gson = new Gson();		
			//3205988	1	249212	1451214	CLP
			//String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"CTA\",\"p_simularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"S\",\"p_FechaSistema\":\"2020-01-10\",\"p_CargoAbonoAGenerar\":[{\"NumeroCargoAbono\":1,\"IdPortafolio\":1451750,\"IdMoneda\":\"CLP\",\"IdTipoMovimiento\":\"MOVABN\",\"IdConcepto\":\"AAAAAA\",\"Descripcion\":\"\",\"IdCondicionDeLiquidacion\":\"T0\",\"FechaCondicionDeLiquidacion\":\"2021-01-10\",\"Monto\":150000,\"IdEjecutivo\":0,\"IdSucursal\":0,\"IdTipoProducto\":\"\",\"IdTipoOperacion\":\"\",\"IdOperacion\":0,\"FechaOperacion\":\"0001-01-01\",\"FechaLiquidacionOperacion\":\"0001-01-01\",\"FechaVencimientoOperacion\":null,\"IdMonedaOperacion\":\"\",\"MontoOperacion\":null,\"TipoCambioOperacion\":0,\"IdContraparte\":null,\"IdTipoComprobante\":null,\"IdComprobante\":null,\"FolioComprobante\":null,\"IdComprobanteDetalle\":null,\"IdComprobanteFormaDePago\":null,\"IdTipoMovimientoAsociado\":null,\"IdMovimientoAsociado\":null,\"FechaComprobante\":null,\"IdPersonaPagoCobro\":null,\"IdInstrumento\":null,\"IdTipoInstrumento\":null,\"Nemo\":null,\"EsIndicadorBilateral\":\"N\",\"EsCargoAbonoPorTraspaso\":\"N\",\"IdPortafolioTraspaso\":null,\"GeneroContraparteEnTesoreria\":\"N\",\"CertificadoAutomatico\":\"N\"}],\"p_EsIngresoManual\":\"S\"}";
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":1,\"p_IdModulo\":\"CTA\",\"p_simularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"S\",\"p_FechaSistema\":\"2025-08-11\",\"p_CargoAbonoAGenerar\":[{\"NumeroCargoAbono\":1,\"IdPortafolio\":18581,\"IdMoneda\":\"CLP\",\"IdTipoMovimiento\":\"MOVABN\",\"IdConcepto\":\"AACFIR\",\"Descripcion\":\"\",\"IdCondicionDeLiquidacion\":\"T0\",\"FechaCondicionDeLiquidacion\":\"2025-08-11\",\"Monto\":5000,\"IdEjecutivo\":0,\"IdSucursal\":0,\"IdTipoProducto\":\"\",\"IdTipoOperacion\":\"\",\"IdOperacion\":0,\"FechaOperacion\":\"0001-01-01\",\"FechaLiquidacionOperacion\":\"0001-01-01\",\"FechaVencimientoOperacion\":null,\"IdMonedaOperacion\":\"\",\"MontoOperacion\":null,\"TipoCambioOperacion\":0,\"IdContraparte\":null,\"IdTipoComprobante\":null,\"IdComprobante\":null,\"FolioComprobante\":null,\"IdComprobanteDetalle\":null,\"IdComprobanteFormaDePago\":null,\"IdTipoMovimientoAsociado\":null,\"IdMovimientoAsociado\":null,\"FechaComprobante\":null,\"IdPersonaPagoCobro\":null,\"IdInstrumento\":null,\"IdTipoInstrumento\":null,\"Nemo\":null,\"EsIndicadorBilateral\":\"N\",\"EsCargoAbonoPorTraspaso\":\"N\",\"IdPortafolioTraspaso\":null,\"GeneroContraparteEnTesoreria\":\"N\",\"CertificadoAutomatico\":\"N\"}],\"p_EsIngresoManual\":\"S\"}";
			CrearCargoAbonoEntrada Objin = gson.fromJson(json, CrearCargoAbonoEntrada.class);
			CrearCargoAbono demo = new CrearCargoAbono();
			
			try {
				CrearCargoAbonoSalida Objout = demo.logica(con, Objin);	
				con.commit();
				//con.rollback();
				System.out.println(Objout.toString());
			}   	
			catch(Exception er) {
				er.printStackTrace();	
				con.rollback();
			}
				
		    	
		} catch (Exception e) {
			e.printStackTrace();			
		}
		System.out.println("== END ==");
	};
};