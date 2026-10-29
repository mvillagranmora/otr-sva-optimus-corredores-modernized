package cl.bcs.mesa.MRF;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
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

import cl.bcs.cuentaInversion.CrearCargoAbono;
import cl.bcs.cuentaInversion.CrearCargoAbono.CrearCargoAbonoEntrada;
import cl.bcs.cuentaInversion.CrearCargoAbono.CrearCargoAbonoSalida;
import cl.bcs.cuentaInversion.CrearCargoAbono.ObjCargoAbonoAGenerar;
import cl.bcs.custodia.utils.IngresoCustodia;
import cl.bcs.custodia.utils.LogCustodia;
import cl.bcs.custodia.utils.LogCustodia.ObjMovimiento;
import cl.bcs.custodia.utils.RetiroCustodia;
import cl.bcs.custodia.utils.RetiroCustodia.RetiroCustodiaSalida.ObjDatosSalida;
import cl.bcs.facturacion.ProcesarFacturacionCustodia.ClienteNeteo;
import cl.bcs.facturacion.ProcesarFacturacionCustodia.EntradaNeteo;
import cl.bcs.mesa.MES.CambiarEstadoOperacion;
import cl.bcs.mesa.MES.CambiarEstadoOperacion.CambiarEstadoOperacionEntrada;
import cl.bcs.mesa.MES.CambiarEstadoOperacion.CambiarEstadoOperacionSalida;
import cl.bcs.mesa.MES.CambiarEstadoOperacion.ObjCambiarEstadoOperacionIN;
import cl.bcs.mesa.MES.CambiarEstadoOperacion.ObjEstadoOperacionOUT;
import cl.bcs.mesa.MRF.CrearOperacionMesa.FormaDePagoMesa;
import cl.bcs.mesa.MRF.CrearOperacionMesa.ModalidadDePago;
import cl.bcs.mesa.MRF.CrearOperacionMesa.PreferenciaPagoPortafolio;
import cl.bcs.mesa.MRF.CrearOperacionMesa.Sucursal;
import cl.bcs.mesa.MRF.CrearOperacionMesa.TipoMovimientoMesa;
import cl.bcs.plataforma.bat.AgregarPrograma;
import cl.bcs.plataforma.bat.AgregarPrograma.InAgregarPrograma.Parametro;
import cl.bcs.plataforma.pla.ObtenerDiaAnterior;
import cl.bcs.plataforma.pla.ObtenerDiaAnterior.InObtenerDiaAnterior;
import cl.bcs.plataforma.pla.ObtenerDiaAnterior.OutObtenerDiaAnterior;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.tesoreria.CrearIngresoEgreso;
import cl.bcs.tesoreria.CrearIngresoEgreso.CrearIngresoEgreso_Entrada;
import cl.bcs.tesoreria.CrearIngresoEgreso.CrearIngresoEgreso_Salida;
import cl.bcs.tesoreria.CrearIngresoEgreso.FormaDePago_Entrada;
import cl.bcs.tesoreria.CrearIngresoEgreso.IngresoEgresoGenerado_Salida;
import cl.bcs.tesoreria.CrearIngresoEgreso.IngresoEgreso_Entrada;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class VencerOperacion implements JavaCall{
	private static final Logger logger = LogManager.getLogger(VencerOperacion.class);
	
	private final String SQL_SELECT_CONCEPTOS_MESA = loadResourceToString("cl/bcs/mesa/sql/ConsultarConceptosMesa.sql");
	private final String SQL_SELECT_SUCURSAL_EMPLEADO = loadResourceToString("cl/bcs/mesa/MRF/sql/VencerOperacion_SelectSucursalEmpleado.sql");
	private final String SQL_SELECT_TIPO_MOVIMIENTO = loadResourceToString("cl/bcs/mesa/sql/ConsultarTipoMovimientoMesa.sql");
	private final String SQL_SELECT_FORMAS_DE_PAGO_MESA = loadResourceToString("cl/bcs/mesa/sql/ConsultarFormasDePagoMesa.sql");
	private final String SQL_SELECT_MODALIDAD_DE_PAGO = loadResourceToString("cl/bcs/mesa/MRF/sql/VencerOperacion_SelectModalidadDePago.sql");
	private final String SQL_SELECT_PREFERENCIA_PAGO_PORTAFOLIO = loadResourceToString("cl/bcs/mesa/MRF/sql/VencerOperacion_SelectPreferenciaPagoPortafolio.sql");
	private final String SQL_SELECT_DETALLE_OPERACION = loadResourceToString("cl/bcs/mesa/sql/VencerOperacionMesa_SelectOperacion.sql");
	private final String SQL_SELECT_DETALLE_RETIRO_CARTERA_ORIGEN 	= loadResourceToString("cl/bcs/mesa/sql/VencerOperacionMesa_SelectCarteraRetiroOrigen.sql");
	private final String SQL_SELECT_DETALLE_RETIRO_PACTO 			= loadResourceToString("cl/bcs/mesa/sql/VencerOperacionMesa_SelectPactoRetiro.sql");
	private final String SQL_SELECT_DETALLE_RETIRO_PACTO_SUBYACENTE = loadResourceToString("cl/bcs/mesa/sql/VencerOperacionMesa_SelectPactoSubyacenteRetiro.sql");
	private final String SQL_SELECT_DETALLE_COMPROBANTE_PAGO_OPERACION	= loadResourceToString("cl/bcs/mesa/sql/VencerOperacionMesa_SelectComprobantePagoOperacion.sql");
	private final String SQL_SELECT_CUADRATURA_CIERRE = loadResourceToString("cl/bcs/mesa/MRF/sql/VencerOperacion_SelectCuadraturaCierre.sql");
	private final String SQL_UPDATE_FORMAS_DE_PAGOS	= loadResourceToString("cl/bcs/mesa/sql/AnticiparOperacionMesa_UpdateFormasDePago.sql");
	
	public static class VencerOperacionMesaEntrada {
		public Long 				p_IdEmpresa;
		public Long 				p_IdUsuario;
		public String 				p_IdModulo; 
		public String				p_UsaReglasNegocio;
		public String 				p_FechaVencimiento;// 			DATE
		public List<Operacion> 		p_Operacion;
		
		public VencerOperacionMesaEntrada() {
			this.p_Operacion 		= new ArrayList<Operacion>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	}
	
	public static class Operacion {
		public Long IdInternoOperacion;
		public Long IdOperacion;//					BIGINT
		public String IdEstadoSiguiente;//			NVARCHAR(3)
		public BigDecimal MontoOperacion;	
		public String IdCondicionLiquidacion;
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	}
		
	public static class VencerOperacionMesaSalida {

		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;
		public List<ObjEstadoOperacionOUT>	P_OUT_OPERACION;
		
		public VencerOperacionMesaSalida() {
			this.P_OUT_OPERACION 		= new ArrayList<ObjEstadoOperacionOUT>();
			this.P_OUT_MENSAJES 		= new ArrayList<MensajeSalida>();
			this.P_OUT_ERRORESCONTADOR 	= 0L;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class DetalleOperacion {
		public Long				IdOperacion;
		public Long				FolioOperacion;
		public String			IdTipoProducto;
		public Long 			IdCliente;
		public Long				IdPersona;
		public Long 			IdPortafolio;
		public Long 			IdClienteCorredor;
		public Long 			IdPortafolioCorredor;
		public String 			IdTipoOperacion;
		public Long				IdInstrumentoPacto;
		public Long 			FolioCarteraPacto;
		public Long				IdCarteraPacto;
		public BigDecimal		ValorRescatePacto;
		public String			IdMonedaLiquidacion;
		public String 			IdMonedaInstrumento;
		
		public Long				IdReserva;
		public Long				IdInstrumentoDetalle;
	    public BigDecimal 		CantidadDetalle;
	    public BigDecimal 		ValorRescateDetalle;
	    public BigDecimal 		MontoValorizadoDetalle;
	    public Long 			IdCarteraOrigenDetalle;
	    public Long 			FolioCarteraOrigenDetalle;	    
	    public Long 			IdCarteraDestinoDetalle;
	    public Long 			FolioCarteraDestinoDetalle;
	    public Long 			IdCarteraSubyacenteOrigenDetalle;
	    public BigDecimal 		CantidadCarteraDestinoDetalle;
	    public BigDecimal 		ValorRescateDestinoDetalle;
	    public Date				FechaLiquidacion;
	    public Long				PlazoEnDias;
	    public String			EsFungible;
	    public String			IdEmisor;
	    public String			IdTipoReajuste;
	    
	    public BigDecimal       MontoFinal;
	    public BigDecimal       MontoInicial;
	    
	    public BigDecimal		MontoOperacionInicial;
	    public BigDecimal		MontoOperacionFinal;
	    
	    public String 			IdCondicionLiquidacion;	  
	    public BigDecimal	    MontoOperacion;
	    public Long				IdUsuario;
	    
	    public BigDecimal		MontoOperacionInicialMoneda;
	    public BigDecimal		MontoOperacionFinalMoneda;
	    
	    public BigDecimal	    MontoOperacionMoneda;
	    
	    public Date				FechaVencimientoInstrumento;
	    public BigDecimal		CantidadCarteraOrigenDetalle;
	    public BigDecimal		ValorRescateOrigenDetalle;
	    public BigDecimal		INGCantidadCarteraOrigenDetalle;
	    public BigDecimal		INGValorRescateOrigenDetalle;
	    public String			IdClasificacionTipoInstrumentoOrigenDetalle;
	    public BigDecimal		TasaOperacion;
	    public String			IdTipoReajusteOperacion;
	    public String 			Glosa = "";
	    
	    public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class EntidadTesoreriaProceso {
		
		public Long IdComprobanteOrigen;
		public Long Id;
		public String Entidad;
		public String NombreTipoMovimiento;
		public String IdEstado;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class CargoAbonoProceso {
		public Long IdComprobante;
		public String IdTipoComprobante;
		public Long IdCargoAbonoMovimiento;
		public String NombreTipoMovimiento;
		public String IdEstado;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	public static class Concepto{
		public String IdConcepto;
	    public String NombreConcepto;
	    public String IdTipoProducto;
	    public String IdTipoOperacion;
	    public String Uso;
	    public String IdConceptoRelacionado;
	    public String NombreConceptoRelacionado;
	    
	    public String toString() {
			return Utils.printJson(this, false);
		};
	}
	public static class ComprobantePagoOperacion {
		public Long IdComprobantePagoOperacion;
		public Long IdEmpresa;
		public Long IdOperacion;
		public Long IdIngresoEgreso;
		public String IdTipoProducto;
		public String IdFormaDePago;
		public String RecibePaga;
		public String IdModalidadPago;
		public String IdMoneda;
		public BigDecimal Monto;
		public Date FechaIngresoComprobantePagoOperacion;
		public Date FechaLiquidacionComprobantePagoOperacion;
		public String IdEstado;
		public Long IdAbono;
		public Long IdCargo;
		public String AfectarTesoreria;
		public Long IdIngresoEgresoAnticipo;
		public Long IdAbonoAnticipo;
		public Long IdCargoAnticipo;
		public Long IdCuentaBanco;
		public String IdTipoOperacion;
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	}
	
	public static class ControlProceso{
		public Long IdEmpresa;
		public Long IdControlProceso;
		public String CodigoModulo;
		public String CodigoProceso;
		public Date FechaSistema;
		public Timestamp FechaRegistro;
		public String CodigoUsuario;
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	}
	
	public VencerOperacionMesaSalida logica (Connection conn, VencerOperacionMesaEntrada in) throws Exception {
		SQL _sql = new SQL();
		logger.info("INICIO::MRF.VencerOperacionMesa");
		HashMap<String,String> mapMsg = new HashMap<String,String>();
		VencerOperacionMesaSalida out = new VencerOperacionMesaSalida();
		Long Id = 0L;
		ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
		InformacionEmpresa empresa = consultarEmpresaService.logica(conn, in.p_IdEmpresa);
	    Long idPaisSistema = empresa.IdPais;
	    String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
	    
	    ObtenerDiaAnterior obtenerDiaAnterior = new ObtenerDiaAnterior();
	    InObtenerDiaAnterior inObtenerDiaAnterior = new InObtenerDiaAnterior();
	    inObtenerDiaAnterior.idPais = idPaisSistema;
	    inObtenerDiaAnterior.diaHabil = "S";
	    inObtenerDiaAnterior.soloCaeDiaHabil = "S";
	    InObtenerDiaAnterior.FechasIn fechaInicial = new InObtenerDiaAnterior.FechasIn();
	    fechaInicial.fechaInicial = fechaSistema;
	    fechaInicial.numeroDias = 1L;
	    inObtenerDiaAnterior.fechas.add(fechaInicial);
	    OutObtenerDiaAnterior outObtenerDiaAnterior = obtenerDiaAnterior.logica(conn, inObtenerDiaAnterior);
	    String fechaAnterior = outObtenerDiaAnterior.fechas.get(0).fechaCalculada;
	    
	    HashMap<String, Object> paramsDetOpIn = new HashMap<String, Object>();
	    HashMap<String, Object> paramsFPagIn = new HashMap<String, Object>();
	    HashMap<String, Object> params = new HashMap<String, Object>();
	    List<Object> par = new ArrayList<>();
	    Long TicketAtencion = 0L;
	    List<Object> parPre = new ArrayList<>(); 
	    HashMap<String, Object> parPrefPor = new HashMap<>();
	    List<ClienteNeteo> clientesANetear = new ArrayList<ClienteNeteo>();
	    String  operacionesParaNetear = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "CUSTODIA", "CUS.OperacionesNeteo");
	    
	    List<RetiroCustodia.ObjDatosEntrada> p_RetiroCartera = new ArrayList<RetiroCustodia.ObjDatosEntrada>();
	    List<IngresoCustodia.ObjDatosEntrada> p_IngresoCartera = new ArrayList<IngresoCustodia.ObjDatosEntrada>();
	    
	    params.put("FechaSistema", fechaAnterior);
	    List<ControlProceso> controlProceso = _sql.query(conn, SQL_SELECT_CUADRATURA_CIERRE, params, ControlProceso.class);
		//busqueda de conceptos usado en mesa
		Concepto conceptoVencimiento = null;
		HashMap<String,Object> paramConcep = new HashMap<>();
		paramConcep.put("IdTipoProducto", "MERF");
		List<Concepto> selectConcepto = _sql.query(conn, SQL_SELECT_CONCEPTOS_MESA, paramConcep, Concepto.class);
		for (Concepto con: selectConcepto){
			if (con.Uso.equals("VENCIMIENTO")){
				conceptoVencimiento = con;
				break;
			}
		}
	    
	    for (Operacion op: in.p_Operacion){
	    	par.add(op.IdOperacion);
	    }	  
	    paramsDetOpIn.put("IdOperacion", par);
	    paramsFPagIn.put("IdOperacion", par);
	    paramsDetOpIn.put("FechaVencimiento", in.p_FechaVencimiento);	    
	    List<DetalleOperacion> detOpe = _sql.query(conn, SQL_SELECT_DETALLE_OPERACION, paramsDetOpIn, DetalleOperacion.class);	  
	    List<DetalleOperacion> detOpePactoRetiro = _sql.query(conn, SQL_SELECT_DETALLE_RETIRO_PACTO, paramsDetOpIn, DetalleOperacion.class);
	    List<DetalleOperacion> detOpePactoSubyacenteRetiro = _sql.query(conn, SQL_SELECT_DETALLE_RETIRO_PACTO_SUBYACENTE, paramsDetOpIn, DetalleOperacion.class);
	    List<ComprobantePagoOperacion> comprobantePagoOperacion = _sql.query(conn, SQL_SELECT_DETALLE_COMPROBANTE_PAGO_OPERACION, paramsFPagIn, ComprobantePagoOperacion.class);
	    HashMap<Long, Object> inOperacionesHash = Utils.List2Hash(in.p_Operacion, "IdOperacion");
	    
	    //TipoMovimiento
		List<TipoMovimientoMesa> tipoMov = _sql.query(conn, SQL_SELECT_TIPO_MOVIMIENTO, new HashMap<>(), TipoMovimientoMesa.class);
		
		Map<String, String> tipoMovTes = new HashMap<String, String>();
		Map<String, String> tipoMovCtaCli = new HashMap<String, String>();
		Map<String, String> tipoMovCtaCor = new HashMap<String, String>();
		for (TipoMovimientoMesa tipo : tipoMov){
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
		
		//sucursal empleado
		List<Long> idUsuarios = new ArrayList<Long>();
		for(DetalleOperacion detalle : detOpe) {
			idUsuarios.add(detalle.IdUsuario);
		}
		HashMap<String, Object> parEmp = new HashMap<>();
		parEmp.put("p_IdUsuario", idUsuarios);
		parEmp.put("p_IdEmpresa", in.p_IdEmpresa);
		List<Sucursal> idSucursal = _sql.query(conn, SQL_SELECT_SUCURSAL_EMPLEADO, parEmp, Sucursal.class);
		HashMap<Long,Object> hashIdSucursales = Utils.List2Hash(idSucursal, "IdUsuario");
		
		
		//consulto formas de pago de la mesa
		HashMap<String, Object> selectFormaPagoMesaIN = new HashMap<>();
		selectFormaPagoMesaIN.put("IdTipoProducto" , "MERF");
		//selectFormaPagoMesaIN.put("IdTipoOperacion", IdTipoOperacion);
		List<FormaDePagoMesa> FormaDePagoMesaList = _sql.query(conn, SQL_SELECT_FORMAS_DE_PAGO_MESA, selectFormaPagoMesaIN, FormaDePagoMesa.class);
		HashMap<String, Object> FormaDePagoMesaHash = Utils.List2Hash(FormaDePagoMesaList, "IdFormaDePago");
		
		//Modalidad de pago
		List<ModalidadDePago> modalidadPago = _sql.query(conn, SQL_SELECT_MODALIDAD_DE_PAGO, new HashMap<String, Object>(), ModalidadDePago.class);
		HashMap<String, Object> modalidadPagoHash = Utils.List2Hash(modalidadPago, "IdMedioDePago");
		
		
		//valido que vengan datos
	    if (conceptoVencimiento == null){
	    	//mapMsg.put("IdOperacion", op.IdOperacion.toString());
			out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"CONCEPTOS_MESA_NO_CONFIGURADO",mapMsg,conn,in.p_IdEmpresa));
			Id++;
	    }
	    if (in.p_Operacion.size() == 0){
			//mapMsg.put("IdOperacion", op.IdOperacion.toString());
			out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"NO_SE_INGRESO_OPERACION_MESA_RF",mapMsg,conn,in.p_IdEmpresa));
			Id++;
		}
	    if (detOpe.size() == 0){
			//mapMsg.put("IdOperacion", op.IdOperacion.toString());
			out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"NO_SE_ENCONTRO_OPERACION_MESA_RF",mapMsg,conn,in.p_IdEmpresa));
			Id++;
		}
	    if (in.p_Operacion.size() != detOpePactoRetiro.size()){
	    	out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"NO_SE_ENCONTRO_OPERACION_MESA_RF",mapMsg,conn,in.p_IdEmpresa));
			Id++;
	    }
	    if (detOpePactoRetiro.size() < detOpe.size()) {
	    	out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"NO_SE_ENCONTRO_CABECERA_OPERACION_MESA_RF",mapMsg,conn,in.p_IdEmpresa));
			Id++;
	    }
		
	    out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		
		if (!out.P_OUT_ERRORESCONTADOR.equals(0L)){
			return out;
		}
		
			
		for (Operacion ope: in.p_Operacion){
	    	for (DetalleOperacion det: detOpe){
	    		if (ope.IdOperacion.equals(det.IdOperacion)){	    				
	    			det.MontoOperacion =ope.MontoOperacion;	    		
	    			det.IdCondicionLiquidacion = ope.IdCondicionLiquidacion;    			
	    		}
	    	}
		}
		
		for (DetalleOperacion ope: detOpe){
			parPre.add(ope.IdPortafolio);
		}
		
		HashMap<Long,Object> HashOperaciones  = Utils.List2Hash(detOpe, "IdOperacion");
	    List<ComprobantePagoOperacion> nuevaFpag = recalculaMontosAnticipos(HashOperaciones,comprobantePagoOperacion);
	    comprobantePagoOperacion.clear();
	    comprobantePagoOperacion.addAll(nuevaFpag);
	    
	    //ACTUALIZA MONTOS DE LAS FORMAS DE PAGOS AL ANTICIPAR
	    _sql.batch(conn, SQL_UPDATE_FORMAS_DE_PAGOS, nuevaFpag, ComprobantePagoOperacion.class); 	    
	    
	    //consultar preferencia Pago portafolio
  		parPrefPor.put("IdPortafolio", parPre);
  		List<PreferenciaPagoPortafolio> prefPagoPortafolio = _sql.query(conn, SQL_SELECT_PREFERENCIA_PAGO_PORTAFOLIO, parPrefPor, PreferenciaPagoPortafolio.class);  		
  		
  		List<IngresoEgresoGenerado_Salida> ListaIngresosEgresosGenerados = new ArrayList<IngresoEgresoGenerado_Salida>();
  		HashMap<Long,String> mapeoReferenciaIdOperacion = new HashMap<Long,String>();
  		
  		//CREAR COMPROBANTE TESORERIA
  		this.crearComprobanteTesoreria(conn, in, out, fechaSistema, prefPagoPortafolio, nuevaFpag, tipoMovTes, HashOperaciones, FormaDePagoMesaList, hashIdSucursales, conceptoVencimiento,ListaIngresosEgresosGenerados,mapeoReferenciaIdOperacion);
  			
	    //CREAR CARGO ABONO 
  		this.crearCargoAbono(conn, in, out, fechaSistema, nuevaFpag, HashOperaciones, ListaIngresosEgresosGenerados, mapeoReferenciaIdOperacion, conceptoVencimiento, tipoMovCtaCli, idSucursal, tipoMovTes, tipoMovCtaCor);
  		
	    //SE SEPARAN LISTAS EN VENTAS Y COMPRAS
	    List<DetalleOperacion> detOpeCom = new ArrayList<DetalleOperacion>();
	    List<DetalleOperacion> detOpeVen = new ArrayList<DetalleOperacion>();
	    List<DetalleOperacion> detOpeCarteraOrigenRetiroCom = new ArrayList<DetalleOperacion>();
	    List<DetalleOperacion> detOpeCarteraOrigenRetiroVen = new ArrayList<DetalleOperacion>();
	    List<DetalleOperacion> detOpePactoRetiroCom = new ArrayList<DetalleOperacion>();
	    List<DetalleOperacion> detOpePactoRetiroVen = new ArrayList<DetalleOperacion>();
	    List<DetalleOperacion> detOpePactoSubyacenteRetiroCom = new ArrayList<DetalleOperacion>();
	    List<DetalleOperacion> detOpePactoSubyacenteRetiroVen = new ArrayList<DetalleOperacion>();
	    List<ComprobantePagoOperacion> comprobantePagoOperacionCom = new ArrayList<ComprobantePagoOperacion>();
	    List<ComprobantePagoOperacion> comprobantePagoOperacionVen = new ArrayList<ComprobantePagoOperacion>();
	    		
	    //DETALLE OPERACION
	    for(DetalleOperacion ope : detOpe) {
	    	if("VENPAC".equalsIgnoreCase(ope.IdTipoOperacion))
	    		detOpeVen.add(ope);
	    	else if(("COMPAC".equalsIgnoreCase(ope.IdTipoOperacion)))
	    		detOpeCom.add(ope);
	    }
	    
	    //DETALLE RETIRO PACTO
	    for(DetalleOperacion ope : detOpePactoRetiro) {
	    	if("VENPAC".equalsIgnoreCase(ope.IdTipoOperacion))
	    		detOpePactoRetiroVen.add(ope);
	    	else if(("COMPAC".equalsIgnoreCase(ope.IdTipoOperacion)))
	    		detOpePactoRetiroCom.add(ope);
	    }
	    
	    //DETALLE RETIRO PACTO SUBYACENTE
	    for(DetalleOperacion ope : detOpePactoSubyacenteRetiro) {
	    	if("VENPAC".equalsIgnoreCase(ope.IdTipoOperacion))
	    		detOpePactoSubyacenteRetiroVen.add(ope);
	    	else if(("COMPAC".equalsIgnoreCase(ope.IdTipoOperacion)))
	    		detOpePactoSubyacenteRetiroCom.add(ope);
	    }
	    
	    //DETALLE COMPROBANTE PAGO OPERACION
	    for(ComprobantePagoOperacion com : comprobantePagoOperacion) {
	    	if("VENPAC".equalsIgnoreCase(com.IdTipoOperacion))
	    		comprobantePagoOperacionVen.add(com);
	    	else if(("COMPAC".equalsIgnoreCase(com.IdTipoOperacion)))
	    		comprobantePagoOperacionCom.add(com);
	    }
	    
	    //VENTAS CON PACTO
	    {
	    	//CABECERA	    	
	    	p_RetiroCartera.addAll(this.obtenerCabeceraPacto(detOpePactoRetiroVen, inOperacionesHash, conceptoVencimiento, fechaSistema));
		   
		    //SUBYACENTE
	    	p_RetiroCartera.addAll(this.obtenerSubyacentePacto(detOpePactoSubyacenteRetiroVen, inOperacionesHash, conceptoVencimiento, fechaSistema));
		    
		    //CARTERA PAPEL
	    	detOpeCarteraOrigenRetiroVen = this.obtenerPapelesPacto(conn, _sql,par, in, "VENPAC");
	    	p_RetiroCartera.addAll(this.obtenerDetallePacto(detOpeCarteraOrigenRetiroVen, inOperacionesHash, conceptoVencimiento, fechaSistema,"VENPAC"));
		   
		    if (p_RetiroCartera.size() > 0 || p_IngresoCartera.size() > 0){
		    	List<ObjMovimiento>  entLogCus = new ArrayList<>();
		    	TicketAtencion = LogCustodia.logCustodia(conn,entLogCus);
		    }
		    
		    if (p_RetiroCartera.size() > 0){
		    	logger.debug("INICIO::MRF.RetiroCustodia");
		    	RetiroCustodia retiroCustodia = new RetiroCustodia();
		    	RetiroCustodia.RetiroCustodiaEntrada paramInSPRetiro = new RetiroCustodia.RetiroCustodiaEntrada();
				paramInSPRetiro.p_IdEmpresa			= in.p_IdEmpresa;
				paramInSPRetiro.p_SimularOperacion	= "N";
				paramInSPRetiro.p_TicketAtencion	= TicketAtencion;			
				paramInSPRetiro.p_ValorizacionAutomatica = "S";
		    	
		    	List<RetiroCustodia.ObjDatosEntrada> p_RetiroCarteraSubyacente = new ArrayList<RetiroCustodia.ObjDatosEntrada>();
		    	List<RetiroCustodia.ObjDatosEntrada> p_RetiroCarteras = new ArrayList<RetiroCustodia.ObjDatosEntrada>();
		    	
		    	for ( RetiroCustodia.ObjDatosEntrada sub : p_RetiroCartera) {
		    		if(sub.TablaAfectada.equals("SUBYACENTE")){
		    			p_RetiroCarteraSubyacente.add(sub);
		    		}else {
		    			p_RetiroCarteras.add(sub);
		    		}	    		
		    	}
		    	
		    	paramInSPRetiro.p_DatosEntrada		= p_RetiroCarteraSubyacente;
				 
				RetiroCustodia.RetiroCustodiaSalida resultRetirosSubyacente = retiroCustodia.logica(conn, paramInSPRetiro);
				this.actualizarMensajesErroresYContador(resultRetirosSubyacente.P_OUT_MENSAJES, out);
		    			    	
		    	//RETIROS DE CUSTODIA 1 A 1 
		    	for ( RetiroCustodia.ObjDatosEntrada retiros : p_RetiroCarteras) {
		    		
		    		List<RetiroCustodia.ObjDatosEntrada> p_Retiro1a1 = new ArrayList<RetiroCustodia.ObjDatosEntrada>();
		    		p_Retiro1a1.add(retiros);
		    		paramInSPRetiro.p_DatosEntrada		= p_Retiro1a1;
					RetiroCustodia.RetiroCustodiaSalida resultRetiros = retiroCustodia.logica(conn, paramInSPRetiro);	
					
					this.actualizarMensajesErroresYContador(resultRetiros.P_OUT_MENSAJES, out);
					
					for(ObjDatosSalida ope : resultRetiros.P_OUT_DATOSSALIDA) {
						for(DetalleOperacion ope2 : detOpeVen) {
							if(ope2.IdOperacion.equals(ope.IdGenerarMovimientoCustodia) && "CARTERA_RETIRADA".equalsIgnoreCase(ope.CodigoMensaje)) {
								HashMap<String,String> parametrosMensaje = new HashMap<String,String>();
								parametrosMensaje.put("IdOperacion", String.valueOf(ope2.IdOperacion));
								out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_OK,"OPERACION_VENCIDA_PACTOS",parametrosMensaje,conn,in.p_IdEmpresa));
							}
						}
					}
					
		    	}
				
				logger.debug("FIN::MRF.RetiroCustodia");
		    }
	    }
	    
	    //COMPRAS CON PACTO
	    {
	    	p_RetiroCartera.clear();	    	
	    	
	    	//CABECERA
	    	p_RetiroCartera.addAll(this.obtenerCabeceraPacto(detOpePactoRetiroCom, inOperacionesHash, conceptoVencimiento, fechaSistema));
		   
		    //SUBYACENTE
	    	p_RetiroCartera.addAll(this.obtenerSubyacentePacto(detOpePactoSubyacenteRetiroCom, inOperacionesHash, conceptoVencimiento, fechaSistema));	    	
		    
		    //CARTERA PAPEL
	    	detOpeCarteraOrigenRetiroCom = this.obtenerPapelesPacto(conn, _sql,par, in, "COMPAC");
	    	p_RetiroCartera.addAll(this.obtenerDetallePacto(detOpeCarteraOrigenRetiroCom, inOperacionesHash, conceptoVencimiento, fechaSistema,"COMPAC"));	
	    	
		    if (p_RetiroCartera.size() > 0 || p_IngresoCartera.size() > 0){
		    	List<ObjMovimiento>  entLogCus = new ArrayList<>();
		    	TicketAtencion = LogCustodia.logCustodia(conn,entLogCus);
		    }
		    
		    if (p_RetiroCartera.size() > 0){
		    	logger.debug("INICIO::MRF.RetiroCustodia");
		    	RetiroCustodia retiroCustodia = new RetiroCustodia();
		    	RetiroCustodia.RetiroCustodiaEntrada paramInSPRetiro = new RetiroCustodia.RetiroCustodiaEntrada();
				paramInSPRetiro.p_IdEmpresa			= in.p_IdEmpresa;
				paramInSPRetiro.p_SimularOperacion	= "N";
				paramInSPRetiro.p_TicketAtencion	= TicketAtencion;			
				paramInSPRetiro.p_ValorizacionAutomatica = "S";
		    	
		    	List<RetiroCustodia.ObjDatosEntrada> p_RetiroCarteraSubyacente = new ArrayList<RetiroCustodia.ObjDatosEntrada>();
		    	List<RetiroCustodia.ObjDatosEntrada> p_RetiroCarteras = new ArrayList<RetiroCustodia.ObjDatosEntrada>();
		    	
		    	for ( RetiroCustodia.ObjDatosEntrada sub : p_RetiroCartera) {
		    		if(sub.TablaAfectada.equals("SUBYACENTE")){
		    			p_RetiroCarteraSubyacente.add(sub);
		    		}else {
		    			p_RetiroCarteras.add(sub);
		    		}	    		
		    	}
							
				 paramInSPRetiro.p_DatosEntrada		= p_RetiroCarteraSubyacente;
				 
				 RetiroCustodia.RetiroCustodiaSalida resultRetirosSubyacente = retiroCustodia.logica(conn, paramInSPRetiro);
				 this.actualizarMensajesErroresYContador(resultRetirosSubyacente.P_OUT_MENSAJES, out);
		    					 
		    	for ( RetiroCustodia.ObjDatosEntrada retiros : p_RetiroCarteras) {
		    		
		    		List<RetiroCustodia.ObjDatosEntrada> p_Retiro1a1 = new ArrayList<RetiroCustodia.ObjDatosEntrada>();
		    		p_Retiro1a1.add(retiros);
		    		paramInSPRetiro.p_DatosEntrada		= p_Retiro1a1;
					RetiroCustodia.RetiroCustodiaSalida resultRetiros = retiroCustodia.logica(conn, paramInSPRetiro);	
					
					this.actualizarMensajesErroresYContador(resultRetiros.P_OUT_MENSAJES, out);
					
					// aqui va llaamda a  clientes
					if( retiros.IdTipoOperacion.equalsIgnoreCase("COMPAC")){
						ClienteNeteo clienteNeteo = new ClienteNeteo();
						clienteNeteo.IdInstrumento = retiros.IdInstrumento;
						clienteNeteo.IdPortafolio = retiros.IdPortafolio;
						clientesANetear.add(clienteNeteo);
					}
		    	}     
		    	
				logger.debug("FIN::MRF.RetiroCustodia");
		    }
		    
			 // se llama job de  neteo 
		    if(clientesANetear.size() > 0) {
		    	
		    	EntradaNeteo entradaNeteo = new EntradaNeteo();
		    	entradaNeteo.p_IdEmpresa = in.p_IdEmpresa;
		    	entradaNeteo.p_IdModulo = "CUS";
		    	entradaNeteo.p_IdUsuario = in.p_IdUsuario;
		    	entradaNeteo.p_PortafoliosInstrumento = clientesANetear;
		    	entradaNeteo.p_SimularOperacion = "N";
		    	entradaNeteo.p_UsaReglasNegocio = "S";
		    	
		    	AgregarPrograma.InAgregarPrograma paramsIn = new AgregarPrograma.InAgregarPrograma(); 
		    	paramsIn.p_IdEmpresa		= in.p_IdEmpresa;
		    	paramsIn.p_IdModulo			= "CUS";
		    	paramsIn.p_IdUsuario		= in.p_IdUsuario;
		    	paramsIn.p_IdTipoPrograma	= "NeteoCarterasNegativas";
		    	paramsIn.p_Segundos			= 0L;
		    	List<Parametro> paramsParametros = new ArrayList<>();
		    	Parametro parametro = new Parametro();
		    	parametro.NombreParametro = "Entrada";
		    	Gson gson = Utils.getGsonBuilder();
		    	parametro.Valor = "";
		    	parametro.ValorBlob = gson.toJson(entradaNeteo);
		    	paramsParametros.add(parametro);
		    	paramsIn.p_Parametros = paramsParametros ;
		    	AgregarPrograma AP = new AgregarPrograma();
		    	AP.logica(conn, paramsIn);
		    }
	    }
	    
	    //CAMBIO ESTADO OPERACIONES MESA.
	    this.cambioEstadoOperaciones(conn, in, out, detOpe, idPaisSistema, inOperacionesHash);
	    
		logger.info("FIN::MRF.VencerOperacionMesa");
	    //CacheValorizacion.valorizar(conn);
	    if (out.P_OUT_ERRORESCONTADOR.equals(0L)){
	    	HashMap<String,String> param = new HashMap<String,String>();
	    	param.put("Cantidad", String.valueOf(out.P_OUT_OPERACION.size()));
			out.P_OUT_MENSAJES.clear();
	    	out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_OK,"OPERACIONES_VENCIDAS",param,conn,in.p_IdEmpresa));
		}
	   
		return out;
	}
	
	
	public List<RetiroCustodia.ObjDatosEntrada> obtenerSubyacentePacto (List<DetalleOperacion> listaOperacionesRetiro, HashMap<Long, Object> inOperacionesHash,Concepto conceptoVencimiento ,String fechaSistema){
		logger.trace("INICIO::VencerOperacion.obtenerSubyacentePacto");
		long inicioMetodo = System.currentTimeMillis();
		
		List<RetiroCustodia.ObjDatosEntrada> p_RetiroCartera = new ArrayList<RetiroCustodia.ObjDatosEntrada>();
		
		 for (DetalleOperacion det: listaOperacionesRetiro){
	    	if (inOperacionesHash.get(det.IdOperacion) != null){
	    		RetiroCustodia.ObjDatosEntrada	ObjEntrada = new RetiroCustodia.ObjDatosEntrada();
				ObjEntrada.IdGenerarMovimientoCustodia			= det.IdCarteraSubyacenteOrigenDetalle;//det.IdReserva;
	    		ObjEntrada.TablaAfectada 						= "SUBYACENTE";
				ObjEntrada.AccionOperacion						= "VENCIMIENTO";		
				ObjEntrada.IdCliente							= det.IdCliente;//det.IdTipoOperacion.contains("COM")?det.IdClienteCorredor:det.IdCliente;
				ObjEntrada.IdPortafolio							= det.IdPortafolio;//det.IdTipoOperacion.contains("COM")?det.IdPortafolioCorredor:det.IdPortafolio;
				ObjEntrada.FolioCustodia						= det.FolioCarteraOrigenDetalle;
				ObjEntrada.IdCustodia							= det.IdCarteraSubyacenteOrigenDetalle;//det.IdCarteraOrigenDetalle;
				ObjEntrada.IdConcepto							= conceptoVencimiento.IdConcepto;
				ObjEntrada.Cantidad								= det.CantidadDetalle;
				ObjEntrada.ValorRescate							= det.ValorRescateDetalle;
				ObjEntrada.IdInstrumento						= det.IdInstrumentoDetalle;
				ObjEntrada.FechaLiquidacion						= det.FechaLiquidacion;//det.FechaLiquidacion;
				ObjEntrada.FechaOperacion						= Fecha.toDate(fechaSistema);
				ObjEntrada.EsFungible							= det.EsFungible;
				ObjEntrada.IdTipoProducto						= det.IdTipoProducto;
				ObjEntrada.IdTipoOperacion						= "VCDPAC";
				ObjEntrada.MontoCompra							= calcularMontoPlazo(det.MontoValorizadoDetalle,det.TasaOperacion,new BigDecimal(det.PlazoEnDias),det.IdTipoReajusteOperacion);
				ObjEntrada.TasaCompra							= det.TasaOperacion;
				ObjEntrada.IdEmisor								= det.IdEmisor;
				// MSH
				ObjEntrada.IdMonedaEmision						= det.IdTipoReajuste;
				ObjEntrada.IdMonedaInstrumento					= det.IdMonedaInstrumento;	
				ObjEntrada.IdTipoReajusteInstrumento			= det.IdTipoReajuste;
				ObjEntrada.FechaVencimientoInstrumento			= det.FechaVencimientoInstrumento;
				
				p_RetiroCartera.add(ObjEntrada);
	    	}
		}
		
		long finMetodo = System.currentTimeMillis();
		logger.trace("FIN::VencerOperacion.obtenerSubyacentePacto :" , finMetodo- inicioMetodo);	
	    
		return p_RetiroCartera;
	}
	

	public List<RetiroCustodia.ObjDatosEntrada> obtenerCabeceraPacto (List<DetalleOperacion> listaOperacionesRetiro, HashMap<Long, Object> inOperacionesHash,Concepto conceptoVencimiento ,String fechaSistema){
		logger.trace("INICIO::VencerOperacion.obtenerCabeceraPacto");
		long inicioMetodo = System.currentTimeMillis();
		
		List<RetiroCustodia.ObjDatosEntrada> p_RetiroCartera = new ArrayList<RetiroCustodia.ObjDatosEntrada>();
		
		for (DetalleOperacion det: listaOperacionesRetiro){
	    	if (inOperacionesHash.get(det.IdOperacion) != null){
	    		RetiroCustodia.ObjDatosEntrada	ObjEntrada = new RetiroCustodia.ObjDatosEntrada();    				
				ObjEntrada.IdGenerarMovimientoCustodia			= det.IdOperacion;
	    		ObjEntrada.TablaAfectada 						= "CARTERA";
				ObjEntrada.AccionOperacion						= "VENCIMIENTO";		
				ObjEntrada.IdCliente							= det.IdCliente;
				ObjEntrada.IdPortafolio							= det.IdPortafolio;
				ObjEntrada.FolioCustodia						= det.FolioCarteraPacto;
				ObjEntrada.IdCustodia							= det.IdCarteraPacto;
				ObjEntrada.IdConcepto							= conceptoVencimiento.IdConcepto;
				ObjEntrada.Cantidad								= BigDecimal.ONE;
				ObjEntrada.IdInstrumento						= det.IdInstrumentoPacto;
				ObjEntrada.ValorRescate							= det.ValorRescatePacto;
				ObjEntrada.FechaLiquidacion						= det.FechaLiquidacion;//det.FechaLiquidacion;
				ObjEntrada.FechaOperacion						= Fecha.toDate(fechaSistema);
				ObjEntrada.EsFungible							= det.EsFungible;
				ObjEntrada.IdTipoProducto						= det.IdTipoProducto;
				ObjEntrada.IdTipoOperacion						= "VCDPAC";
				ObjEntrada.IdOperacion							= det.IdOperacion;
				ObjEntrada.MontoCompra							= det.MontoFinal;	
				ObjEntrada.TasaCompra							= det.TasaOperacion;
				ObjEntrada.IdEmisor								= "".equalsIgnoreCase(det.IdEmisor) ? "OPTIMUS" : det.IdEmisor;
				// MSH
				ObjEntrada.IdMonedaEmision						= det.IdTipoReajuste;
				ObjEntrada.IdMonedaInstrumento					= det.IdMonedaInstrumento;	
				ObjEntrada.IdTipoReajusteInstrumento			= det.IdTipoReajuste;
				
				
				p_RetiroCartera.add(ObjEntrada);
	    	}
	    }
		
		long finMetodo = System.currentTimeMillis();
		logger.trace("FIN::VencerOperacion.obtenerCabeceraPacto :" , finMetodo- inicioMetodo);	
	    
		return p_RetiroCartera;
	}
	
	
	public List<RetiroCustodia.ObjDatosEntrada> obtenerDetallePacto (List<DetalleOperacion> detOpeCarteraOrigenRetiro, HashMap<Long, Object> inOperacionesHash,Concepto conceptoVencimiento ,String fechaSistema ,String TipoOperacion ){
		logger.trace("INICIO::VencerOperacion.obtenerDetallePacto");
		long inicioMetodo = System.currentTimeMillis();
		
		List<RetiroCustodia.ObjDatosEntrada> p_RetiroCartera = new ArrayList<RetiroCustodia.ObjDatosEntrada>();
		
		for (DetalleOperacion det: detOpeCarteraOrigenRetiro){
			if (inOperacionesHash.get(det.IdOperacion) != null){
				RetiroCustodia.ObjDatosEntrada	ObjEntrada = new RetiroCustodia.ObjDatosEntrada();	
				
				
				ObjEntrada.IdGenerarMovimientoCustodia			= det.IdReserva;
	    		ObjEntrada.TablaAfectada 						= "CARTERA";
				ObjEntrada.AccionOperacion						= "VENCIMIENTO";		
				ObjEntrada.IdCliente							= det.IdTipoOperacion.equals("COMPAC")? det.IdClienteCorredor: det.IdCliente;      
				ObjEntrada.IdPortafolio							= det.IdTipoOperacion.equals("COMPAC")? det.IdPortafolioCorredor:det.IdPortafolio;
				ObjEntrada.FolioCustodia						= det.FolioCarteraDestinoDetalle;
				ObjEntrada.IdCustodia							= det.IdCarteraDestinoDetalle;
				ObjEntrada.IdConcepto							= conceptoVencimiento.IdConcepto;
				ObjEntrada.Cantidad								= det.CantidadDetalle;
				ObjEntrada.IdInstrumento						= det.IdInstrumentoDetalle;
				ObjEntrada.ValorRescate							= det.ValorRescateDetalle;
				ObjEntrada.FechaLiquidacion						= det.FechaLiquidacion;
				ObjEntrada.FechaOperacion						= Fecha.toDate(fechaSistema);
				ObjEntrada.EsFungible							= det.EsFungible;
				ObjEntrada.IdTipoProducto						= det.IdTipoProducto;
				ObjEntrada.IdTipoOperacion						= "VCDPAC";			
				ObjEntrada.IdEmisor								= det.IdEmisor;
				ObjEntrada.IdOperacion							= det.IdOperacion;
				ObjEntrada.MontoCompra							= calcularMontoPlazo(det.MontoValorizadoDetalle,det.TasaOperacion,new BigDecimal(det.PlazoEnDias),det.IdTipoReajusteOperacion);
				ObjEntrada.TasaCompra							= det.TasaOperacion;
				// MSH
				ObjEntrada.IdMonedaEmision						= det.IdTipoReajuste;
				ObjEntrada.IdMonedaInstrumento					= det.IdMonedaInstrumento;	
				ObjEntrada.IdTipoReajusteInstrumento			= det.IdTipoReajuste;
				
				p_RetiroCartera.add(ObjEntrada);
			}
		}
		
		long finMetodo = System.currentTimeMillis();
		logger.trace("FIN::VencerOperacion.obtenerDetallePacto :" , finMetodo- inicioMetodo);	
	    
		return p_RetiroCartera;
	}
	
	
	public void crearComprobanteTesoreria (Connection conn,VencerOperacionMesaEntrada in,VencerOperacionMesaSalida out,String fechaSistema, List<PreferenciaPagoPortafolio> prefPagoPortafolio
			,List<ComprobantePagoOperacion> nuevaFpag,Map<String, String> tipoMovTes ,HashMap<Long,Object> HashOperaciones,List<FormaDePagoMesa> FormaDePagoMesaList ,HashMap<Long,Object> hashIdSucursales
			,Concepto conceptoVencimiento, List<IngresoEgresoGenerado_Salida> ListaIngresosEgresosGenerados,HashMap<Long,String> mapeoReferenciaIdOperacion) throws Exception {
		logger.trace("INICIO::VencerOperacion.crearComprobanteTesoreria");
		long inicioMetodo = System.currentTimeMillis();
		
		CrearIngresoEgreso_Salida tesOut = new CrearIngresoEgreso_Salida();
  		
  	   
		//genero INGRESO EGRESO a Tesoreria
		CrearIngresoEgreso tes = new CrearIngresoEgreso();
		CrearIngresoEgreso_Entrada entTes = new CrearIngresoEgreso_Entrada();
		entTes.p_IdEmpresa = in.p_IdEmpresa;
		entTes.p_IdUsuario = in.p_IdUsuario;
		entTes.p_IdModulo = in.p_IdModulo;
		entTes.p_SimularOperacion = "N";
		entTes.p_UsaReglasNegocio = in.p_UsaReglasNegocio;		
		entTes.p_EsIngresoAutomatico = "S";       
		entTes.p_FechaMovimiento = fechaSistema;
		
		Long numeroReferenciaFormaPago = 1L;
		Long numeroReferencia = 1L;
		
		for (ComprobantePagoOperacion fPag :nuevaFpag){
			if(fPag.AfectarTesoreria.equals("S")) {
				DetalleOperacion ope = (DetalleOperacion)HashOperaciones.get(fPag.IdOperacion);	
				if (ope!=null&& (ope.IdTipoOperacion.equals("COMPAC") && fPag.RecibePaga.equals("R"))
						|| (ope.IdTipoOperacion.equals("VENPAC") && fPag.RecibePaga.equals("R"))) {
					FormaDePagoMesa formaDePago = seleccionaFormaPagoMesa(FormaDePagoMesaList, fPag.IdFormaDePago);
					IngresoEgreso_Entrada IEGen = new IngresoEgreso_Entrada();
					
					IEGen.NumeroReferencia 				= numeroReferencia;  				
					IEGen.IdTipoMovimiento 				= ope.IdTipoOperacion.contains("COM")?tipoMovTes.get("R"):tipoMovTes.get("P");  	
					IEGen.IdTipoPersona 				= "CLI";  		 	
					IEGen.IdPersona						= ope.IdPersona;  		 		
					IEGen.IdPortafolio 					= ope.IdPortafolio;  		 			
					IEGen.IdMoneda 						= ope.IdMonedaLiquidacion;
					IEGen.Monto 						= fPag.Monto;
					IEGen.FechaLiquidacion 				= fechaSistema;                                                                 
					IEGen.IdConcepto 					= conceptoVencimiento.IdConcepto;  	
					IEGen.IdTipoPersonaTitularPago 		= "CLI"; 
					IEGen.IdPersonaTitularPago 			= ope.IdPersona; 
					IEGen.IdPortafolioTitularPago 		= ope.IdPortafolio;
					IEGen.IdCaja 						= 0L; 
					IEGen.SecuenciaAgrupacion 			= ""; 	
					IEGen.SecuenciaRelacionada 			= 0L;	
					IEGen.Glosa 						= ope.Glosa; 										   	
				   	//Parametros Referenciales(Opcionales)
					IEGen.IdEmpleadoDigitador			= in.p_IdUsuario;  	
					IEGen.IdSucursalEmpleadoDigitador	= hashIdSucursales.get(ope.IdUsuario) != null ? ((Sucursal) hashIdSucursales.get(ope.IdUsuario)).IdSucursal : 0L; 
					IEGen.IdTipoProducto				= ope.IdTipoProducto; 
					IEGen.IdTipoOperacion				= ope.IdTipoOperacion;  								    
					IEGen.IdTipoComprobante				= "MES";  		 
					IEGen.IdComprobante					= ope.FolioOperacion;  		 
					IEGen.FechaComprobante				= fechaSistema+" "+"12:00:00";	 	
					IEGen.FechaLiquidacionComprobante	= fechaSistema;
					
					IEGen.IdOperacion					= fPag.IdOperacion;  		 					
					IEGen.FechaOperacion				= fechaSistema+" "+"12:00:00";  		
					IEGen.IdCondicionLiquidacion		= "T0";  		
					IEGen.IdBolsa						= "";  		 
					IEGen.IdMonedaOperacion				= ope.IdMonedaLiquidacion;  
					IEGen.MontoOperacion				= ope.MontoOperacion;
					IEGen.IdTipoReajuste				= ope.IdTipoReajuste;  
					IEGen.FechaParidad					= "0001-01-01";  	
					IEGen.ValorParidad					= BigDecimal.ZERO;   	
					IEGen.FolioCartera					= ope.FolioCarteraPacto != null ? ope.FolioCarteraPacto:0;  
					IEGen.IdInstrumento					= 0L;  		 
					IEGen.IdTipoInstrumento				= "";  		 
					IEGen.IdClasificacionTipoInstrumento= ""; 
				    //Indicadores
					IEGen.Bilateral 					= "N";	
					IEGen.GeneraComprobante 			= "S";
					IEGen.AgruparEnComprobante 			= "N"; 
					IEGen.MueveCuentaInversion			= "N";  
					IEGen.TitularPago					= "S";  
					
					entTes.p_IngresoEgresoGenerar.add(IEGen);
					
					FormaDePago_Entrada FPagTes = new FormaDePago_Entrada();
					FPagTes.NumeroReferencia			= numeroReferencia;			
					FPagTes.NumeroReferenciaFormaDePago	= numeroReferenciaFormaPago;
					FPagTes.IdMedioDePago				= formaDePago.IdMedioDePago;      
					FPagTes.IdModalidadDePago			= formaDePago.IdModalidadDePago;  
					FPagTes.IdMoneda					= ope.IdMonedaLiquidacion;
					FPagTes.Monto						= fPag.Monto;
					FPagTes.IdCuentaBancariaCorredora	= ope.IdTipoOperacion.equals("COMPAC") ? fPag.IdCuentaBanco : 0L; 		
					FPagTes.IdTarjetaBancariaCorredora	= 0L; 	
					FPagTes.IdChequera					= 0L; 	
					FPagTes.NumeroDocumentoCorredora	= 0L; 	
					FPagTes.FechaDocumentoPagoCorredora	= "0001-01-01 00:00:00";	
					//areglar a la por defecto del cliente
					if (prefPagoPortafolio.size() > 0) {
						
						for (PreferenciaPagoPortafolio pref : prefPagoPortafolio){
							if (fPag.IdFormaDePago.contains(pref.IdPreferenciaPago) && pref.IdPreferenciaPago.contains(ope.IdMonedaLiquidacion) && pref.IdCuentaBancaria.compareTo(fPag.IdCuentaBanco) == 0 ){
								FPagTes.IdCuentaBancariaSocio		= pref.IdCuentaBancaria; 			
								FPagTes.IdBancoSocio				= pref.IdBancoCuentaBancaria; 					
								FPagTes.NombreCuentaBancariaSocio	= pref.NombreBancoCuentaBancaria;		
								FPagTes.NumeroCuentaBancariaSocio	= pref.NumeroCuentaBancaria;		
								FPagTes.IdTipoCuentaBancariaSocio	= pref.IdTipoCuentaBancaria;		
								FPagTes.NombreTipoCuentaBancariaSocio = pref.NombreTipoCuentaBancaria; 	
								FPagTes.IdTarjetaBancariaSocio		= pref.IdTarjetaBancaria;			
								FPagTes.IdTipoTarjetaBancariaSocio	= pref.IdTipoTarjetaBancaria;  	
								FPagTes.NombreTipoTarjetaBancariaSocio = pref.NombreBancoTarjetaBancaria; 
								FPagTes.NumeroTarjetaBancariaSocio	= pref.NumeroTarjetaBancaria;  	
								break;
							} else {
								FPagTes.IdCuentaBancariaSocio		= ope.IdTipoOperacion.equals("VENPAC") ?  fPag.IdCuentaBanco : 0L; 			
								FPagTes.IdBancoSocio				= 0L; 					
								FPagTes.NombreCuentaBancariaSocio	= "";		
								FPagTes.NumeroCuentaBancariaSocio	= "";		
								FPagTes.IdTipoCuentaBancariaSocio	= "";		
								FPagTes.NombreTipoCuentaBancariaSocio = ""; 	
								FPagTes.IdTarjetaBancariaSocio		= 0L;			
								FPagTes.IdTipoTarjetaBancariaSocio	= "";  	
								FPagTes.NombreTipoTarjetaBancariaSocio = ""; 
								FPagTes.NumeroTarjetaBancariaSocio	= "";
							}
						}		
					} else {
							FPagTes.IdCuentaBancariaSocio		= ope.IdTipoOperacion.equals("VENPAC") ?  fPag.IdCuentaBanco : 0L; 			
							FPagTes.IdBancoSocio				= 0L; 					
							FPagTes.NombreCuentaBancariaSocio	= "";		
							FPagTes.NumeroCuentaBancariaSocio	= "";		
							FPagTes.IdTipoCuentaBancariaSocio	= "";		
							FPagTes.NombreTipoCuentaBancariaSocio = ""; 	
							FPagTes.IdTarjetaBancariaSocio		= 0L;			
							FPagTes.IdTipoTarjetaBancariaSocio	= "";  
							FPagTes.NombreTipoTarjetaBancariaSocio = ""; 
							FPagTes.NumeroTarjetaBancariaSocio	= "";
					}
					FPagTes.NumeroSerieSocio			= "";  				
					FPagTes.NumeroDocumentoSocio		= 0L;			
					FPagTes.FechaDocumentoPagoSocio		= Fecha.toFormat(ope.FechaLiquidacion,"yyyy-MM-dd hh:mm:ss");  		//	NVARCHAR(40)
					entTes.p_FormaDePago.add(FPagTes);
					
					mapeoReferenciaIdOperacion.put(numeroReferencia, fPag.IdFormaDePago);
					numeroReferencia++;
					numeroReferenciaFormaPago++;
				}
			}
			
		}
		
		logger.debug("MRF.Llamada Ingreso Tesoreria");
		if (entTes.p_IngresoEgresoGenerar.size() > 0 && entTes.p_FormaDePago.size() > 0){
			tesOut = tes.logica(conn, entTes);
			this.actualizarMensajesErroresYContador(tesOut.P_OUT_MENSAJES, out);			
			ListaIngresosEgresosGenerados.addAll(tesOut.P_OUT_INGRESOEGRESOGENERADO);
		}
		
		long finMetodo = System.currentTimeMillis();
		logger.trace("FIN::VencerOperacion.crearComprobanteTesoreria :" , finMetodo- inicioMetodo);	
		
	}
	
	public void crearCargoAbono (Connection conn,VencerOperacionMesaEntrada in,VencerOperacionMesaSalida out,String fechaSistema,List<ComprobantePagoOperacion> nuevaFpag,HashMap<Long,Object> HashOperaciones
			,List<IngresoEgresoGenerado_Salida> ListaIngresosEgresosGenerados,HashMap<Long,String> mapeoReferenciaIdOperacion,Concepto conceptoVencimiento,Map<String, String> tipoMovCtaCli ,List<Sucursal> idSucursal
			,Map<String, String> tipoMovTes,Map<String, String> tipoMovCtaCor) throws Exception {
		logger.trace("INICIO::VencerOperacion.crearCargoAbono");
		long inicioMetodo = System.currentTimeMillis();

	    CrearCargoAbonoSalida ctaOut = new CrearCargoAbonoSalida();
	    
		//genero CARGO ABONO cta inversion
		CrearCargoAbonoEntrada entCta = new CrearCargoAbonoEntrada();
		entCta.p_IdEmpresa = in.p_IdEmpresa;
		entCta.p_IdUsuario = in.p_IdUsuario;
		entCta.p_IdModulo = in.p_IdModulo;
		entCta.p_simularOperacion = "N";
		entCta.p_UsaReglasNegocio = in.p_UsaReglasNegocio;
		entCta.p_FechaSistema = fechaSistema;
		entCta.p_EsIngresoManual	= "N";
		Long numeroCargoAbono = 1L;
		//CLIENTES
		for (ComprobantePagoOperacion fPag :nuevaFpag){
			DetalleOperacion ope = (DetalleOperacion)HashOperaciones.get(fPag.IdOperacion);
			if (ope !=null&&(ope.IdTipoOperacion.equals("COMPAC") && fPag.RecibePaga.equals("R"))
					|| (ope.IdTipoOperacion.equals("VENPAC") && fPag.RecibePaga.equals("R"))) {
				Long IdMovtoTeso = 0L;
				for (IngresoEgresoGenerado_Salida tesOutGen : ListaIngresosEgresosGenerados){
					String idFormaPago = mapeoReferenciaIdOperacion.get(tesOutGen.NumeroReferencia) != null ? mapeoReferenciaIdOperacion.get(tesOutGen.NumeroReferencia) : "";
					if (fPag.IdFormaDePago.equals(idFormaPago)){
						IdMovtoTeso = tesOutGen.IdIngresoEgreso;
					}
				}
				ObjCargoAbonoAGenerar objCtaInCli = new ObjCargoAbonoAGenerar();
				ObjCargoAbonoAGenerar objCtaInCorr = new ObjCargoAbonoAGenerar();
				objCtaInCli.NumeroCargoAbono 				= numeroCargoAbono; 
				objCtaInCli.IdPortafolio 					= ope.IdPortafolio; 
				objCtaInCli.IdMoneda 						= ope.IdMonedaLiquidacion;//Liquidacion; 
				objCtaInCli.IdTipoMovimiento 				= ope.IdTipoOperacion.contains("COM")?tipoMovCtaCli.get("R"):tipoMovCtaCli.get("P");; 
				objCtaInCli.IdConcepto						= conceptoVencimiento.IdConcepto;
				objCtaInCli.Descripcion						= conceptoVencimiento.NombreConcepto; 
				objCtaInCli.IdCondicionDeLiquidacion		= ope.IdCondicionLiquidacion; 
				objCtaInCli.FechaCondicionDeLiquidacion 	= Fecha.toFormat(Fecha.toDate(fechaSistema), "yyyy-MM-dd");
				objCtaInCli.Monto							= fPag.Monto; 
				objCtaInCli.IdEjecutivo						= in.p_IdUsuario; 
				objCtaInCli.IdSucursal						= idSucursal.size() > 0 ? idSucursal.get(0).IdSucursal : 0L; 
				objCtaInCli.IdTipoProducto					= ope.IdTipoProducto; 
				objCtaInCli.IdTipoOperacion					= ope.IdTipoOperacion; 
				objCtaInCli.IdOperacion						= fPag.IdOperacion; 
				objCtaInCli.FechaOperacion					= Fecha.toFormat(Fecha.toDate(fechaSistema),"yyyy-MM-dd"); 
				objCtaInCli.FechaLiquidacionOperacion		= Fecha.toFormat(Fecha.toDate(fechaSistema),"yyyy-MM-dd"); 
				objCtaInCli.FechaVencimientoOperacion		= Fecha.toFormat(Fecha.toDate(fechaSistema),"yyyy-MM-dd"); 
				objCtaInCli.IdMonedaOperacion				= ope.IdMonedaLiquidacion; 
				objCtaInCli.MontoOperacion					= ope.MontoOperacion; 
				objCtaInCli.TipoCambioOperacion				= BigDecimal.ZERO; 
				objCtaInCli.IdContraparte					= 0L; 
				objCtaInCli.IdTipoComprobante				= "MES"; 
				objCtaInCli.IdComprobante					= fPag.IdOperacion; 
				objCtaInCli.FolioComprobante				= ope.FolioOperacion.toString(); 
				objCtaInCli.IdComprobanteDetalle			= 0L; 
				objCtaInCli.IdComprobanteFormaDePago		= 0L; 
				objCtaInCli.IdTipoMovimientoAsociado		= ope.IdTipoOperacion.contains("COM")?tipoMovTes.get("P"):tipoMovTes.get("R"); 
				objCtaInCli.IdMovimientoAsociado			= IdMovtoTeso; 
				objCtaInCli.FechaComprobante				= Fecha.toFormat(Fecha.toDate(fechaSistema),"yyyy-MM-dd"); 
				objCtaInCli.IdPersonaPagoCobro				= ope.IdPersona; 
				objCtaInCli.IdInstrumento					= 0L; 
				objCtaInCli.IdTipoInstrumento				= ""; 
				objCtaInCli.Nemo							= ""; 
				objCtaInCli.EsIndicadorBilateral			= "N"; 
				objCtaInCli.EsCargoAbonoPorTraspaso			= "N"; 
				objCtaInCli.IdPortafolioTraspaso			= 0L; 
				objCtaInCli.GeneroContraparteEnTesoreria	= "S"; 
				objCtaInCli.CertificadoAutomatico			= "S";
				entCta.p_CargoAbonoAGenerar.add(objCtaInCli);
				numeroCargoAbono++;
				//CORREDORA SOLO SI NO ES MOVIMIENTO EN TESORERIA
				if(!fPag.AfectarTesoreria.equals("S")) {
					Utils.copyPojo(objCtaInCli,objCtaInCorr);
					objCtaInCorr.NumeroCargoAbono				= numeroCargoAbono;
					objCtaInCorr.IdPortafolio					= ope.IdPortafolioCorredor;
					objCtaInCorr.IdTipoMovimiento				= ope.IdTipoOperacion.contains("COM")?tipoMovCtaCor.get("R"):tipoMovCtaCor.get("P");
					entCta.p_CargoAbonoAGenerar.add(objCtaInCorr);
					numeroCargoAbono++;
				}
			}
		}
		
		CrearCargoAbono cta = new CrearCargoAbono();
		logger.debug("MRF.Llamada Ingreso Cuenta Inversión");
		ctaOut = cta.logica(conn, entCta);
		this.actualizarMensajesErroresYContador(ctaOut.P_OUT_MENSAJES, out);
		
		long finMetodo = System.currentTimeMillis();
		logger.trace("FIN::VencerOperacion.crearCargoAbono :" , finMetodo- inicioMetodo);	
				
	}
	
	
	
	private FormaDePagoMesa seleccionaFormaPagoMesa(List<FormaDePagoMesa> FormaDePagoMesaList, String preferenciaPago) {
		List<FormaDePagoMesa> listaFormaPago = FormaDePagoMesaList.stream().filter(c -> c.IdPreferenciaPago.equals(preferenciaPago)).collect(Collectors.toList());
		FormaDePagoMesa fp = new FormaDePagoMesa();
		if (listaFormaPago.size() > 0) {
			fp = listaFormaPago.get(0);
		}

		return fp;
	}
	
	public List<ComprobantePagoOperacion> recalculaMontosAnticipos(HashMap<Long,Object>HashOperaciones, List<ComprobantePagoOperacion> ListaFormasPago ) throws Exception{
		
			List<ComprobantePagoOperacion> nuevaFpag = new ArrayList<ComprobantePagoOperacion>();			
			HashMap<Long,BigDecimal> montosRecalculados = new HashMap<Long,BigDecimal>();
			
			for(ComprobantePagoOperacion fPag : ListaFormasPago ){
				DetalleOperacion ope = (DetalleOperacion)HashOperaciones.get(fPag.IdOperacion);
				if (ope!=null&&(ope.IdTipoOperacion.equals("COMPAC") && fPag.RecibePaga.equals("R"))
						|| (ope.IdTipoOperacion.equals("VENPAC") && fPag.RecibePaga.equals("R"))) {
					ComprobantePagoOperacion ObjFpag =  new ComprobantePagoOperacion();
					BigDecimal MontoOperacion = BigDecimal.ZERO;
					BigDecimal MontoVencimiento = BigDecimal.ZERO;
					
					if(ope.IdTipoReajuste != "CLP" && ope.IdTipoReajuste != "UF") {
						MontoOperacion = ope.MontoOperacionInicialMoneda;
						MontoVencimiento = ope.MontoOperacionFinalMoneda;
					} else {
						MontoOperacion = ope.MontoOperacionInicial;
						MontoVencimiento = ope.MontoOperacionFinal;
					}
					
					BigDecimal porcentaje = (fPag.Monto.multiply(new BigDecimal(100)).divide(MontoOperacion,20,RoundingMode.HALF_UP)).divide(new BigDecimal(100),20,RoundingMode.HALF_UP);
					BigDecimal nuevoMonto = (porcentaje.multiply(MontoVencimiento)).setScale(0, BigDecimal.ROUND_UP);
									
					Utils.copyPojo(fPag, ObjFpag);
					ObjFpag.Monto= nuevoMonto;
					nuevaFpag.add(ObjFpag);
					
					BigDecimal MontoRecalculado=montosRecalculados.get(fPag.IdOperacion) != null ? montosRecalculados.get(fPag.IdOperacion) : BigDecimal.ZERO;
					MontoRecalculado = MontoRecalculado.add(ObjFpag.Monto);
					montosRecalculados.put(fPag.IdOperacion, MontoRecalculado);
				}
			}
			
			for(Map.Entry<Long, Object> obj : HashOperaciones.entrySet()) {
				DetalleOperacion op = (DetalleOperacion) obj.getValue();
				BigDecimal MontoRecalculado=montosRecalculados.get(op.IdOperacion) != null ? montosRecalculados.get(op.IdOperacion) : BigDecimal.ZERO;
				
				//AJUSTAR
				BigDecimal DiferenciaAjuste = BigDecimal.ZERO;
				if(!op.IdTipoReajuste.equals("CLP") && !op.IdTipoReajuste.equals("UF")) {
					if(MontoRecalculado.compareTo(op.MontoOperacionFinalMoneda)!=0){						
						if(MontoRecalculado.compareTo(op.MontoOperacionFinalMoneda)>0){
							DiferenciaAjuste = MontoRecalculado.subtract(op.MontoOperacionFinalMoneda);
						}
						if(MontoRecalculado.compareTo(op.MontoOperacionFinalMoneda)<0){
							DiferenciaAjuste = op.MontoOperacionFinalMoneda.subtract(MontoRecalculado);
						}
					}
				} else {
					if(MontoRecalculado.compareTo(op.MontoOperacionFinal)!=0){						
						if(MontoRecalculado.compareTo(op.MontoOperacionFinal)>0){
							DiferenciaAjuste = MontoRecalculado.subtract(op.MontoOperacionFinal);
						}
						if(MontoRecalculado.compareTo(op.MontoOperacionFinal)<0){
							DiferenciaAjuste = op.MontoOperacionFinal.subtract(MontoRecalculado);
						}
					}
				}
				
				for(ComprobantePagoOperacion fPag : nuevaFpag) {
					if(fPag.IdOperacion.equals(op.IdOperacion)) {
						fPag.Monto = fPag.Monto.subtract(DiferenciaAjuste);
						break;
					}
				}
			}

			return nuevaFpag;
		}
	
	private void actualizarMensajesErroresYContador(List<MensajeSalida> mensajesSalidaServicio, VencerOperacionMesaSalida salidaVencimiento) {
		
		for(MensajeSalida mensaje : mensajesSalidaServicio) {
			if(MensajeSalida.MENSAJE_ERROR.equals(mensaje.tipo)) {
				salidaVencimiento.P_OUT_MENSAJES.add(mensaje);
				salidaVencimiento.P_OUT_ERRORESCONTADOR++;
			} else if(MensajeSalida.MENSAJE_ADVERTENCIA.equals(mensaje.tipo)){
				salidaVencimiento.P_OUT_MENSAJES.add(mensaje);
			}
		}
		
		//return salidaVencimiento;
	}
	
	
	public List<DetalleOperacion>  obtenerPapelesPacto(Connection conn,SQL sql,List<Object> operaciones,VencerOperacionMesaEntrada in ,String TipoOperacion) throws Exception {
		logger.debug("INICIO::VencerOperacion.obtenerPapelesPacto");
		long inicioMetodo = System.currentTimeMillis();
		
		HashMap<String,Object>params = new HashMap<>();
		params.put("IdOperacion", operaciones);		
		params.put("FechaVencimiento", in.p_FechaVencimiento);
		params.put("TipoOperacion", TipoOperacion);
		List<DetalleOperacion> detOpeCarteraOrigenRetiro = sql.query(conn, SQL_SELECT_DETALLE_RETIRO_CARTERA_ORIGEN, params, DetalleOperacion.class);
		
		long finMetodo = System.currentTimeMillis();
		logger.debug("FIN::VencerOperacion.obtenerPapelesPacto :" , finMetodo- inicioMetodo);	
		
		return detOpeCarteraOrigenRetiro;
	}
	
	public void cambioEstadoOperaciones (Connection conn,VencerOperacionMesaEntrada in,VencerOperacionMesaSalida out, List<DetalleOperacion> detOpe , Long idPaisSistema,HashMap<Long, Object> inOperacionesHash) throws Exception {
		
		///Cambio estado para las operaciones
	    CambiarEstadoOperacionEntrada inCambioEstado = new CambiarEstadoOperacionEntrada(in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo, "N", in.p_UsaReglasNegocio, idPaisSistema);
	    for (DetalleOperacion det: detOpe){
		    ObjCambiarEstadoOperacionIN estadoOp = new ObjCambiarEstadoOperacionIN(); 
		    estadoOp.IdOperacion 					= det.IdOperacion;
		    estadoOp.IdTipoProducto 				= det.IdTipoProducto;
		    estadoOp.IdNuevoEstado 					= ((Operacion)inOperacionesHash.get(det.IdOperacion)).IdEstadoSiguiente;
		    estadoOp.IdReserva						= det.IdReserva;
		    estadoOp.FolioCarteraCustodia			= det.FolioCarteraDestinoDetalle;
		    estadoOp.MontoOperacionAnticipo			= BigDecimal.ZERO;
		    estadoOp.MontoOperacionInicialAnticipo	= BigDecimal.ZERO;
		    estadoOp.MontoOperacionFinalAnticipo	= BigDecimal.ZERO;
		    estadoOp.TasaAnticipo					= BigDecimal.ZERO;
		    estadoOp.MontoOperacionMonedaAnticipo	= BigDecimal.ZERO;
		    estadoOp.MontoOperacionInicialMonedaAnticipo	= BigDecimal.ZERO;
		    estadoOp.MontoOperacionFinalMonedaAnticipo		= BigDecimal.ZERO;
		    estadoOp.IdMonedaLiquidacionAnticipo			= "";
		    estadoOp.IdCondicionLiquidacionAnticipo			= "";
		    estadoOp.FechaCondicionLiquidacionAnticipo		= Fecha.toDate("0001-01-01");
		    estadoOp.EstadoConfirmacion ="NCF";
		    estadoOp.Glosa = det.Glosa;
			inCambioEstado.p_DataCambiarEstado.add(estadoOp);
	    }
	    logger.debug("INICIO::MRF.CambiarEstadoOperacion");
		CambiarEstadoOperacion cambioEstado = new CambiarEstadoOperacion();
		CambiarEstadoOperacionSalida cambioEstadoOut = cambioEstado.logica(conn, inCambioEstado);
		this.actualizarMensajesErroresYContador(cambioEstadoOut.P_OUT_MENSAJES, out);
		out.P_OUT_OPERACION.addAll(cambioEstadoOut.P_OUT_OPERACION);
		logger.debug("FIN::MRF.CambiarEstadoOperacion");
		
	}
	
	public BigDecimal calcularMontoPlazo (BigDecimal monto,BigDecimal tasa, BigDecimal plazo,String tipoReajuste)
	{
		BigDecimal MontoAPlazoAnticipado = BigDecimal.ZERO;
		BigDecimal base = (tipoReajuste.equalsIgnoreCase("CLP") || tipoReajuste.equalsIgnoreCase("UF")) ? new BigDecimal(30) : new BigDecimal(360);
		BigDecimal calculoTasa = tasa.divide(new BigDecimal(100),10,RoundingMode.HALF_UP);
		BigDecimal calculoPlazo = plazo.divide(base,10,RoundingMode.HALF_UP);
		MontoAPlazoAnticipado = monto.multiply(BigDecimal.ONE.add(calculoTasa.multiply(calculoPlazo)));
		return MontoAPlazoAnticipado.setScale(0, RoundingMode.HALF_UP);
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		Gson gson = Utils.getGsonBuilder();
		VencerOperacionMesaEntrada jin = gson.fromJson(in.toString(), VencerOperacionMesaEntrada.class);
		VencerOperacionMesaSalida out = logica(conn, jin);		
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	};
	
	public static void main(String[] args)  throws Exception,SQLException {
		// TODO Auto-generated method stub
	
		Connection conn = null;
		Gson gson = new Gson();
		
		try {
			System.out.println("== BEGIN ==");
			conn = PlataformaUtils.getPREBCOESTADOConnection();
			//datos entrada
			String json = "";			
			VencerOperacionMesaEntrada objIn = gson.fromJson(json, VencerOperacionMesaEntrada.class);//,{\"IdInternoOperacion\":2,\"IdOperacion\":1598,\"IdEstadoSiguiente\":\"LIQ\"}
			//mientras pruebo hare el rollback
			VencerOperacion demo = new VencerOperacion();
			
			VencerOperacionMesaSalida ObjOut=demo.logica(conn, objIn);
				
			System.out.println(ObjOut.toString());
			
			
			//conn.commit();
		}catch (Exception e){
			e.printStackTrace();
		}finally {
			conn.close();
		}
		System.out.println("== END ==");

	};
	
}
