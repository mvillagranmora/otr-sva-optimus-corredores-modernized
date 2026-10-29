package cl.bcs.mesa.MRF;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
import cl.bcs.mesa.MRF.pojo.CacheMRF;
import cl.bcs.plataforma.bat.AgregarPrograma;
import cl.bcs.plataforma.bat.AgregarPrograma.InAgregarPrograma.Parametro;
import cl.bcs.plataforma.pla.AgregarMensajeWebsocket;
import cl.bcs.plataforma.pla.utils.CacheServicio;
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
import cl.bcs.facturacion.ProcesarFacturacionCustodia.ClienteNeteo;
import cl.bcs.facturacion.ProcesarFacturacionCustodia.EntradaNeteo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class AnticiparOperacion implements JavaCall{
	private final Log log = new Log("MRF.AnticiparOperacionMesa");
	
	private final String SQL_SELECT_CONCEPTOS_MESA = loadResourceToString("cl/bcs/mesa/sql/ConsultarConceptosMesa.sql");
	private final String SQL_SELECT_SUCURSAL_EMPLEADO = loadResourceToString("cl/bcs/mesa/MRF/sql/AnticiparOperacion_SelectSucursalEmpleado.sql");
	private final String SQL_SELECT_TIPO_MOVIMIENTO = loadResourceToString("cl/bcs/mesa/sql/ConsultarTipoMovimientoMesa.sql");
	private final String SQL_SELECT_FORMAS_DE_PAGO_MESA = loadResourceToString("cl/bcs/mesa/sql/ConsultarFormasDePagoMesa.sql");
	private final String SQL_SELECT_MODALIDAD_DE_PAGO = loadResourceToString("cl/bcs/mesa/MRF/sql/AnticiparOperacion_SelectModalidadDePago.sql");
	private final String SQL_SELECT_PREFERENCIA_PAGO_PORTAFOLIO = loadResourceToString("cl/bcs/mesa/MRF/sql/AnticiparOperacion_SelectPreferenciaPagoPortafolio.sql");

	
	private final String SQL_SELECT_DETALLE_OPERACION = loadResourceToString("cl/bcs/mesa/sql/AnticiparOperacionMesa_SelectOperacion.sql");
	
	private final String SQL_SELECT_DETALLE_RETIRO_CARTERA 			= loadResourceToString("cl/bcs/mesa/sql/AnticiparOperacionMesa_SelectCarteraRetiro.sql");
	private final String SQL_SELECT_DETALLE_RETIRO_CARTERA_ORIGEN 	= loadResourceToString("cl/bcs/mesa/sql/AnticiparOperacionMesa_SelectCarteraRetiroOrigen.sql");
	private final String SQL_SELECT_DETALLE_RETIRO_PACTO 			= loadResourceToString("cl/bcs/mesa/sql/AnticiparOperacionMesa_SelectPactoRetiro.sql");
	private final String SQL_SELECT_DETALLE_RETIRO_PACTO_SUBYACENTE = loadResourceToString("cl/bcs/mesa/sql/AnticiparOperacionMesa_SelectPactoSubyacenteRetiro.sql");
	private final String SQL_SELECT_DETALLE_COMPROBANTE_PAGO_OPERACION	= loadResourceToString("cl/bcs/mesa/sql/AnticiparOperacionMesa_SelectComprobantePagoOperacion.sql");
	private final String SQL_SELECT_DETALLE_RETIRO_CARTERA_ORIGEN_EN_VENTA 	= loadResourceToString("cl/bcs/mesa/sql/AnticiparOperacionMesa_SelectCarteraRetiroOrigenEnVenta.sql");
	private final String SQL_SELECT_DETALLE_CARTERA_RESERVADA	= loadResourceToString("cl/bcs/mesa/sql/AnularOperacionMesa_SelectCarteraReservada.sql");
	private final String SQL_SELECT_DETALLE_CARTERA_COMPROMETIDA	= loadResourceToString("cl/bcs/mesa/sql/AnularOperacionMesa_SelectCarteraComprometida.sql");
	
	private final String SQL_UPDATE_FORMAS_DE_PAGOS	= loadResourceToString("cl/bcs/mesa/sql/AnticiparOperacionMesa_UpdateFormasDePago.sql");
	
	private final String GlosaAnticipo = "ANTICIPO PACTO MESA RF/IF";

	public static class AnticiparOperacionMesa_IN {
		public Long 				p_IdEmpresa;
		public Long 				p_IdUsuario;
		public String 				p_IdModulo; 
		public String				p_UsaReglasNegocio;
		public List<Operacion> 		p_Operacion;
		
		public AnticiparOperacionMesa_IN() {
			this.p_Operacion 		= new ArrayList<Operacion>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	}
	
	public static class Operacion {
		public Long IdInternoOperacion;
		public Long IdOperacion;//					BIGINT
		public Long FolioOperacion;//				BIGINT
		public Long IdCliente;// 					BIGINT
		public Long IdPortafolio;//					BIGINT
		public Long IdClienteCorredor;//			BIGINT
		public Long IdPortafolioCorredor;//			BIGINT
		public String Intermesa;//					NVARCHAR(1)
		public Long IdMesaOrigen;//					BIGINT
		public Long IdMesaDestino;//				BIGINT
		public String AfectaCustodia;//				NVARCHAR(6)
		public String IdTipoProducto;//				NVARCHAR(4)-- 'MERF'
		public String IdTipoOperacion;//				NVARCHAR(6)-- COMTER,VENTER,COMPAC,VENPAC
		public String IdTipoReajuste;//				NVARCHAR(3)--"USD","UF"
		public BigDecimal Monto;//						DECIMAL(26,8)
		public BigDecimal MontoMoneda;//			 		DECIMAL(26,8)
		public String IdMoneda;//						NVARCHAR(3)--CLP "USD","UF" moneda local
		public String IdMonedaLiquidacion;//			NVARCHAR(3)	--CLP "USD","UF"
		public BigDecimal Tasa;//							DECIMAL(26,8)
		public BigDecimal ValorFinal;//					DECIMAL(26,8)
		public BigDecimal ValorFinalMoneda;//				DECIMAL(26,8)
		public String IdCondicionLiquidacion;//		NVARCHAR(2)
		public String FechaLiquidacion;// 			DATE
		public String IdEstadoSiguiente;//			NVARCHAR(3)
		public Long PlazoAnticipo;
		public String Glosa = ""; //      NVARCHAR(500)
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
		
	public static class AnticiparOperacionMesa_OUT {

		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;
		public List<ObjEstadoOperacionOUT>	P_OUT_OPERACION;
		
		public AnticiparOperacionMesa_OUT() {
			this.P_OUT_OPERACION 		= new ArrayList<ObjEstadoOperacionOUT>();
			this.P_OUT_MENSAJES 		= new ArrayList<MensajeSalida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	public static class DetalleValidacion {
		public Long				IdOperacion;
		public Long 			FolioCarteraCP;
		public String 			Nemotecnico;
		public String 			Emisor;		

	    public BigDecimal 		CantidadCP;
	    public BigDecimal 		CantidadCartera;
	    public BigDecimal 		CantidadReservada;
	    
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
		public Long 			IdCarteraRetirada;
		
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
	    
	    // MSH
	    public BigDecimal		TasaCompra;
		public BigDecimal		PrecioCompra;	
		public String			IdMonedaEmision;
		public String			IdMonedaInstrumento;
	    
	    //datos de Anticipo
	    public BigDecimal		TasaAnticipo;
	    public BigDecimal		MontoOperacionAnticipo;
	    public BigDecimal		MontoOperacionInicialAnticipo;
	    public BigDecimal		MontoOperacionFinalAnticipo;
	    public BigDecimal		MontoOperacionMonedaAnticipo;
	    public BigDecimal		MontoOperacionInicialMonedaAnticipo;
	    public BigDecimal		MontoOperacionFinalMonedaAnticipo;
	    public String			IdMonedaLiquidacionAnticipo;
		public String			IdCondicionLiquidacionAnticipo;
		public String			FechaLiquidacionAnticipo;
		public Date				FechaVencimientoInstrumento;
	    
		public String			IdEstadoCarteraDestinoDetalle;
		public String			IdTipoReajusteOperacion;
		
	    public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class CarteraValida_Proceso {
		
		public Long IdComprobanteOrigen;
		public Long Id;
		public String Entidad;
		public String NombreTipoMovimiento;
		public String IdEstado;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	
	public static class EntidadTesoreria_Proceso {
		
		public Long IdComprobanteOrigen;
		public Long Id;
		public String Entidad;
		public String NombreTipoMovimiento;
		public String IdEstado;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class CargoAbono_Proceso {
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
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	}
	public AnticiparOperacionMesa_OUT logica (Connection conn, AnticiparOperacionMesa_IN in) throws Exception {
		SQL _sql = new SQL();
		log.write("INICIO::MRF.AnticiparOperacionMesa");
		AnticiparOperacionMesa_OUT out = new AnticiparOperacionMesa_OUT();
		Long Id = 0L,errTot = 0L;
		ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
		InformacionEmpresa empresa = consultarEmpresaService.logica(conn, in.p_IdEmpresa);
	    Long idPaisSistema = empresa.IdPais;
	    String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
	    HashMap<String, Object> paramsDetOpIn = new HashMap<String, Object>();
	    List<Object> par = new ArrayList<>();
	    Long TicketAtencion = 0L;
	    List<Object> parPre = new ArrayList<>(); 
	    HashMap<String, Object> parPrefPor = new HashMap<>();
	    List<ClienteNeteo> clientesANetear = new ArrayList<>();
	    
	    List<RetiroCustodia.ObjDatosEntrada> p_RetiroCartera = new ArrayList<RetiroCustodia.ObjDatosEntrada>();
	    List<IngresoCustodia.ObjDatosEntrada> p_IngresoCartera = new ArrayList<IngresoCustodia.ObjDatosEntrada>();
	    //List<RetiroCustodia.ObjDatosEntrada> p_RetiroCarteraSubyacentePacto = new ArrayList<RetiroCustodia.ObjDatosEntrada>();
	    
	    //sucursal empleado
		HashMap<String, Object> parEmp = new HashMap<>();
		parEmp.put("p_IdUsuario", in.p_IdUsuario);
		parEmp.put("p_IdEmpresa", in.p_IdEmpresa);
		List<Sucursal> idSucursal = _sql.query(conn, SQL_SELECT_SUCURSAL_EMPLEADO, parEmp, Sucursal.class);
		//busqueda de conceptos usado en mesa
		Concepto conceptoAnticipo = new Concepto();
		HashMap<String,Object> paramConcep = new HashMap<>();
		paramConcep.put("IdTipoProducto", "MERF");
		List<Concepto> selectConcepto = _sql.query(conn, SQL_SELECT_CONCEPTOS_MESA, paramConcep, Concepto.class);
		for (Concepto con: selectConcepto){
			if (con.Uso.equals("ANTICIPO")){
				conceptoAnticipo = con;
			}
		}		
	    for (Operacion op: in.p_Operacion){
	    	par.add(op.IdOperacion);
	    }	  
	    paramsDetOpIn.put("IdOperacion", par);
	    List<DetalleOperacion> detOpe 							= _sql.query(conn, SQL_SELECT_DETALLE_OPERACION, paramsDetOpIn, DetalleOperacion.class);
	    List<DetalleOperacion> detOpeCarteraRetiro 				= _sql.query(conn, SQL_SELECT_DETALLE_RETIRO_CARTERA, paramsDetOpIn, DetalleOperacion.class);
	    List<DetalleOperacion> detOpeCarteraOrigenRetiro		= _sql.query(conn, SQL_SELECT_DETALLE_RETIRO_CARTERA_ORIGEN, paramsDetOpIn, DetalleOperacion.class);
	    List<DetalleOperacion> detOpeCarteraOrigenRetiroEnVenta	= _sql.query(conn, SQL_SELECT_DETALLE_RETIRO_CARTERA_ORIGEN_EN_VENTA, paramsDetOpIn, DetalleOperacion.class);
	    List<DetalleOperacion> detOpePactoRetiro 				= _sql.query(conn, SQL_SELECT_DETALLE_RETIRO_PACTO, paramsDetOpIn, DetalleOperacion.class);
	    List<DetalleOperacion> detOpePactoSubyacenteRetiro 		= _sql.query(conn, SQL_SELECT_DETALLE_RETIRO_PACTO_SUBYACENTE, paramsDetOpIn, DetalleOperacion.class);
	    List<ComprobantePagoOperacion> comprobantePagoOperacion = _sql.query(conn, SQL_SELECT_DETALLE_COMPROBANTE_PAGO_OPERACION, paramsDetOpIn, ComprobantePagoOperacion.class);
	    HashMap<Long, Object> inOperacionesHash = Utils.List2Hash(in.p_Operacion, "IdOperacion");
	
	    Id = 0L;
	    List<DetalleValidacion> detOpeReservada = _sql.query(conn, SQL_SELECT_DETALLE_CARTERA_RESERVADA, paramsDetOpIn, DetalleValidacion.class);
	    List<DetalleValidacion> detOpeComprometida = _sql.query(conn, SQL_SELECT_DETALLE_CARTERA_COMPROMETIDA, paramsDetOpIn, DetalleValidacion.class);
	    
	    //Valida Cartera Anulacion es Valida
	    HashMap<String,String> mapMsg = new HashMap<>();
	    
	    //Validaciones, antes de anular se verifica si la cartera esta reservada o comprometida
	    for (DetalleValidacion det :detOpeReservada){
	    	mapMsg.put("FolioCartera", det.FolioCarteraCP.toString());
	    	if ( det.CantidadReservada.compareTo(BigDecimal.ZERO) !=0 ) {
	    		out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"SRV_MRF_CARTERA_ESTA_RESERVADA",mapMsg,conn,in.p_IdEmpresa));
				Id++;
	    	} 
	    	mapMsg.clear();
	    }
	    
	    for (DetalleValidacion det :detOpeComprometida){
	    	mapMsg.put("FolioCartera", det.FolioCarteraCP.toString());
	    	if ( det.CantidadReservada.compareTo(BigDecimal.ZERO) !=0 ) {
	    		out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"SRV_MRF_CARTERA_ESTA_COMPROMETIDA",mapMsg,conn,in.p_IdEmpresa));
				Id++;
	    	} 
	    	mapMsg.clear();
	    }
	    
	    
	    //TipoMovimiento
		List<TipoMovimientoMesa> tipoMov = _sql.query(conn, SQL_SELECT_TIPO_MOVIMIENTO, new HashMap<>(), TipoMovimientoMesa.class);
		
		Map<String, String> tipoMovTes = new HashMap<>();
		Map<String, String> tipoMovCtaCli = new HashMap<>();
		Map<String, String> tipoMovCtaCor = new HashMap<>();
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
		
		//consulto formas de pago de la mesa
		HashMap<String, Object> selectFormaPagoMesaIN = new HashMap<>();
		selectFormaPagoMesaIN.put("IdTipoProducto" , "MERF");
		//selectFormaPagoMesaIN.put("IdTipoOperacion", IdTipoOperacion);
		List<FormaDePagoMesa> FormaDePagoMesaList = _sql.query(conn, SQL_SELECT_FORMAS_DE_PAGO_MESA, selectFormaPagoMesaIN, FormaDePagoMesa.class);
		HashMap<String, Object> FormaDePagoMesaHash = Utils.List2Hash(FormaDePagoMesaList, "IdFormaDePago");
		
		//Modalidad de pago
		List<ModalidadDePago> modalidadPago = _sql.query(conn, SQL_SELECT_MODALIDAD_DE_PAGO, new HashMap<>(), ModalidadDePago.class);
		HashMap<String, Object> modalidadPagoHash = Utils.List2Hash(modalidadPago, "IdMedioDePago");
		
		
		
	    //valido que vengan datos
	    if (in.p_Operacion.size() == 0){
			//mapMsg.put("IdOperacion", op.IdOperacion.toString());
			out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"SRV_MRF_NO_SE_INGRESO_OPERACION_PARA_PROCESAR",mapMsg,conn,in.p_IdEmpresa));
			Id++;
		}
	    if (in.p_Operacion.size() != detOpePactoRetiro.size()){
	    	out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"NO_SE_ENCONTRO_OPERACION_MESA_RF",mapMsg,conn,in.p_IdEmpresa));
			Id++;
	    }else if(detOpe.size() == 0) {
	    	//mapMsg.put("IdOperacion", op.IdOperacion.toString());
			out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"SRV_MRF_NO_SE_ENCONTRO_CARTERA_DE_OPERACION",mapMsg,conn,in.p_IdEmpresa));
			Id++;
	    }
		errTot = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		if (!errTot.equals(0L) ){
			return out;
		}
		
		BigDecimal MontoOperacion = BigDecimal.ZERO;
		BigDecimal MontoAnticipo = BigDecimal.ZERO;
		
		
	    for (Operacion ope: in.p_Operacion){
	    	for (DetalleOperacion det: detOpe){
	    		if (ope.IdOperacion.equals(det.IdOperacion)){
	    			det.MontoOperacionAnticipo = ope.Monto;	
	    			det.MontoOperacionInicialAnticipo = ope.Monto;
	    			det.MontoOperacionFinalAnticipo = ope.ValorFinal;	
	    			det.TasaAnticipo = ope.Tasa;	
	    			det.MontoOperacionMonedaAnticipo = ope.MontoMoneda;	
	    			det.MontoOperacionInicialMonedaAnticipo	= ope.MontoMoneda;
	    			det.MontoOperacionFinalMonedaAnticipo = ope.ValorFinalMoneda;
	    			det.IdMonedaLiquidacionAnticipo	= ope.IdMonedaLiquidacion;
	    			det.IdCondicionLiquidacionAnticipo = ope.IdCondicionLiquidacion;
	    			det.FechaLiquidacionAnticipo = ope.FechaLiquidacion;
	    			parPre.add(ope.IdPortafolio);
	    		}
	    	}
		    for (DetalleOperacion det: detOpePactoRetiro){
	    		if (ope.IdOperacion.equals(det.IdOperacion)){
	    			det.MontoOperacionAnticipo = ope.Monto;	
	    			det.MontoOperacionInicialAnticipo = ope.Monto;
	    			det.MontoOperacionFinalAnticipo = ope.ValorFinal;	
	    			det.TasaAnticipo = ope.Tasa;	
	    			det.MontoOperacionMonedaAnticipo = ope.MontoMoneda;	
	    			det.MontoOperacionInicialMonedaAnticipo	= ope.MontoMoneda;
	    			det.MontoOperacionFinalMonedaAnticipo = ope.ValorFinalMoneda;
	    			det.IdMonedaLiquidacionAnticipo	= ope.IdMonedaLiquidacion;
	    			det.IdCondicionLiquidacionAnticipo = ope.IdCondicionLiquidacion;
	    			det.FechaLiquidacionAnticipo = ope.FechaLiquidacion;
	    			det.PlazoEnDias = ope.PlazoAnticipo;
	    			
	    			//IMPMBIOPT-445 : para recalcular los montos anticipo esta considerando montos de distinta moneda respecto a las formas de pago
	    		    if ("UF".equals(ope.IdTipoReajuste)) {
	    		    	MontoOperacion = det.MontoOperacionAnticipo;
		    		    MontoAnticipo = det.MontoOperacionFinalAnticipo;
	    		    }
	    		    else {
	    		    	MontoOperacion = det.MontoOperacionMonedaAnticipo;
		    		    MontoAnticipo = det.MontoOperacionFinalMonedaAnticipo;
	    		    }
	    		}
	    	}
	    	for (DetalleOperacion det: detOpePactoSubyacenteRetiro){
	    		if (ope.IdOperacion.equals(det.IdOperacion)){
	    			det.FechaLiquidacionAnticipo = ope.FechaLiquidacion;
	    			det.PlazoEnDias = ope.PlazoAnticipo;
	    			det.TasaAnticipo = ope.Tasa;
	    			det.IdTipoReajusteOperacion = ope.IdTipoReajuste;
	    		}
	    	}
	    	for (DetalleOperacion det: detOpeCarteraRetiro){
	    		if (ope.IdOperacion.equals(det.IdOperacion)){
	    			det.FechaLiquidacionAnticipo = ope.FechaLiquidacion;
	    			det.PlazoEnDias = ope.PlazoAnticipo;
	    		}
	    	}
	    	for (DetalleOperacion det: detOpeCarteraOrigenRetiro){
	    		if (ope.IdOperacion.equals(det.IdOperacion)){
	    			det.FechaLiquidacionAnticipo = ope.FechaLiquidacion;
	    			det.PlazoEnDias = ope.PlazoAnticipo;
	    			det.TasaAnticipo = ope.Tasa;
	    			det.IdTipoReajusteOperacion = ope.IdTipoReajuste;
	    		}
	    	}
	    	ope.Glosa = this.GlosaAnticipo;
	    }
	    //cabecera de pacto 
	    for (DetalleOperacion det: detOpePactoRetiro){
	    	if (inOperacionesHash.get(det.IdOperacion) != null){
	    		RetiroCustodia.ObjDatosEntrada	ObjEntrada = new RetiroCustodia.ObjDatosEntrada();
	    		ObjEntrada.IdGenerarMovimientoCustodia			= det.IdOperacion;
	    		ObjEntrada.TablaAfectada 						= "CARTERA";
				ObjEntrada.AccionOperacion						= "ANTICIPO";		
				ObjEntrada.IdCliente							= det.IdCliente;
				ObjEntrada.IdPortafolio							= det.IdPortafolio;
				ObjEntrada.FolioCustodia						= det.FolioCarteraPacto;
				ObjEntrada.IdCustodia							= det.IdCarteraPacto;
				ObjEntrada.IdConcepto							= conceptoAnticipo.IdConcepto;
				ObjEntrada.Cantidad								= BigDecimal.ONE;
				ObjEntrada.IdInstrumento						= det.IdInstrumentoPacto;
				ObjEntrada.ValorRescate							= det.ValorRescatePacto;
				ObjEntrada.FechaLiquidacion						= Fecha.toDate(det.FechaLiquidacionAnticipo);//det.FechaLiquidacion;
				ObjEntrada.FechaOperacion						= Fecha.toDate(fechaSistema);
				ObjEntrada.EsFungible							= det.EsFungible;
				ObjEntrada.IdTipoProducto						= det.IdTipoProducto;
				ObjEntrada.IdTipoOperacion						= "ANTPAC";
				ObjEntrada.TasaDescuentoAnticipo				= det.TasaAnticipo;
				ObjEntrada.MontoARecibirAnticipo				= det.MontoOperacionFinalAnticipo;
				ObjEntrada.MontoCompra							= det.MontoOperacionFinalAnticipo;
				ObjEntrada.IdEmisor								= det.IdEmisor;
				// MSH
				ObjEntrada.IdMonedaEmision						= det.IdTipoReajuste;
				ObjEntrada.IdMonedaInstrumento					= det.IdMonedaInstrumento;	
				ObjEntrada.IdTipoReajusteInstrumento			= det.IdTipoReajuste;
				ObjEntrada.FechaVencimientoInstrumento			= det.FechaVencimientoInstrumento;
			
				
				p_RetiroCartera.add(ObjEntrada);
	    	}
	    }
	    
	    //Validacion si existe una venta que incluye alguna de las carteras dentro de la compra a anticipar
	    if (detOpeCarteraOrigenRetiro.size() < detOpeCarteraOrigenRetiroEnVenta.size()) {
    		mapMsg.clear();
    		mapMsg.put("FolioCartera", detOpeCarteraOrigenRetiroEnVenta.get(0).FolioCarteraDestinoDetalle.toString());
    		out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"CARTERA_COMPAC_ASOCIADA_VENPAC_VIGENTE",mapMsg,conn,in.p_IdEmpresa));
    		return out;
	    }
	    
	    //cartera papel 
	    for (DetalleOperacion det: detOpeCarteraOrigenRetiro){
	    	if (inOperacionesHash.get(det.IdOperacion) != null){
	    		RetiroCustodia.ObjDatosEntrada	ObjEntrada = new RetiroCustodia.ObjDatosEntrada();
	    		
	    		ObjEntrada.IdGenerarMovimientoCustodia			= det.IdReserva;
	    		ObjEntrada.TablaAfectada 						= "CARTERA";
				ObjEntrada.AccionOperacion						= "RETIRO";		
				ObjEntrada.IdCliente							= det.IdTipoOperacion.equals("COMPAC")? det.IdClienteCorredor: det.IdCliente;      
				ObjEntrada.IdPortafolio							= det.IdTipoOperacion.equals("COMPAC")? det.IdPortafolioCorredor:det.IdPortafolio;
				ObjEntrada.FolioCustodia						= det.FolioCarteraDestinoDetalle;
				ObjEntrada.IdCustodia							= det.IdCarteraDestinoDetalle;
				ObjEntrada.IdConcepto							= conceptoAnticipo.IdConcepto;
				ObjEntrada.Cantidad								= det.CantidadDetalle;
				ObjEntrada.IdInstrumento						= det.IdInstrumentoDetalle;
				ObjEntrada.ValorRescate							= det.ValorRescateDetalle;
				ObjEntrada.FechaLiquidacion						= Fecha.toDate(det.FechaLiquidacionAnticipo);//det.FechaLiquidacion;
				ObjEntrada.FechaOperacion						= Fecha.toDate(fechaSistema);
				ObjEntrada.EsFungible							= det.EsFungible;
				ObjEntrada.IdTipoProducto						= det.IdTipoProducto;
				ObjEntrada.IdTipoOperacion						= "ANTPAC";
				ObjEntrada.TasaDescuentoAnticipo				= det.TasaAnticipo;
				ObjEntrada.MontoARecibirAnticipo				= det.MontoOperacionFinalAnticipo;
				ObjEntrada.MontoCompra							= calcularMontoPlazo(det.MontoValorizadoDetalle,det.TasaAnticipo,new BigDecimal(det.PlazoEnDias),det.IdTipoReajusteOperacion);
				ObjEntrada.IdEmisor								= det.IdEmisor;
				// MSH
				ObjEntrada.IdMonedaEmision						= det.IdTipoReajuste;
				ObjEntrada.IdMonedaInstrumento					= det.IdMonedaInstrumento;	
				ObjEntrada.IdTipoReajusteInstrumento			= det.IdTipoReajuste;
				ObjEntrada.FechaVencimientoInstrumento			= det.FechaVencimientoInstrumento;
				p_RetiroCartera.add(ObjEntrada);
				
				if( det.IdTipoOperacion.equalsIgnoreCase("COMPAC")){
					ClienteNeteo clienteNeteo = new ClienteNeteo();
					clienteNeteo.IdInstrumento = det.IdInstrumentoDetalle;
					clienteNeteo.IdPortafolio = det.IdPortafolioCorredor;
					clientesANetear.add(clienteNeteo);
				}
	    	}
	    }
	    
	    //Subyacente 
	    for (DetalleOperacion det: detOpePactoSubyacenteRetiro){
	    	if (inOperacionesHash.get(det.IdOperacion) != null){
	    		RetiroCustodia.ObjDatosEntrada	ObjEntrada = new RetiroCustodia.ObjDatosEntrada();
	    		
	    		ObjEntrada.IdGenerarMovimientoCustodia			= det.IdCarteraSubyacenteOrigenDetalle;//det.IdReserva;
	    		ObjEntrada.TablaAfectada 						= "SUBYACENTE";
				ObjEntrada.AccionOperacion						= "ANTICIPO";		
				ObjEntrada.IdCliente							= det.IdCliente;//det.IdTipoOperacion.contains("COM")?det.IdClienteCorredor:det.IdCliente;
				ObjEntrada.IdPortafolio							= det.IdPortafolio;//det.IdTipoOperacion.contains("COM")?det.IdPortafolioCorredor:det.IdPortafolio;
				ObjEntrada.FolioCustodia						= det.FolioCarteraOrigenDetalle;
				ObjEntrada.IdCustodia							= det.IdCarteraSubyacenteOrigenDetalle;//det.IdCarteraOrigenDetalle;
				ObjEntrada.IdConcepto							= conceptoAnticipo.IdConcepto;
				ObjEntrada.Cantidad								= det.CantidadDetalle;
				ObjEntrada.ValorRescate							= det.ValorRescateDetalle;
				ObjEntrada.IdInstrumento						= det.IdInstrumentoDetalle;
				ObjEntrada.FechaLiquidacion						= Fecha.toDate(det.FechaLiquidacionAnticipo);//det.FechaLiquidacion;
				ObjEntrada.FechaOperacion						= Fecha.toDate(fechaSistema);
				ObjEntrada.EsFungible							= det.EsFungible;
				ObjEntrada.IdTipoProducto						= det.IdTipoProducto;
				ObjEntrada.IdTipoOperacion						= "ANTPAC";
				ObjEntrada.MontoCompra							= calcularMontoPlazo(det.MontoValorizadoDetalle,det.TasaAnticipo,new BigDecimal(det.PlazoEnDias),det.IdTipoReajusteOperacion);
				ObjEntrada.IdEmisor								= det.IdEmisor;
				// MSH
				ObjEntrada.IdMonedaEmision						= det.IdTipoReajuste;
				ObjEntrada.IdMonedaInstrumento					= det.IdMonedaInstrumento;	
				ObjEntrada.IdTipoReajusteInstrumento			= det.IdTipoReajuste;
				ObjEntrada.FechaVencimientoInstrumento			= det.FechaVencimientoInstrumento;
				p_RetiroCartera.add(ObjEntrada);			
	    	}
	    }
	    
	    
		 //* se llama a job para neteo de operaciones con cartera negativa 

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
		    	paramsIn.p_FechaSistema		= fechaSistema;
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
	    
	    

	    if (p_RetiroCartera.size() > 0 || p_IngresoCartera.size() > 0){
	    	List<ObjMovimiento>  entLogCus = new ArrayList<>();
	    	TicketAtencion = LogCustodia.logCustodia(conn,entLogCus);
	    }
	    
	    if (p_RetiroCartera.size() > 0){
	    	log.write("INICIO::MRF.RetiroCustodia");
	    	RetiroCustodia retiroCustodia = new RetiroCustodia();
			RetiroCustodia.RetiroCustodiaEntrada paramInSPRetiro = new RetiroCustodia.RetiroCustodiaEntrada();
			paramInSPRetiro.p_IdEmpresa			= in.p_IdEmpresa;
			paramInSPRetiro.p_SimularOperacion	= "N";
			paramInSPRetiro.p_TicketAtencion	= TicketAtencion;
			paramInSPRetiro.p_DatosEntrada		= p_RetiroCartera;
			paramInSPRetiro.p_ValorizacionAutomatica = "S";
			RetiroCustodia.RetiroCustodiaSalida resultRetiros = retiroCustodia.logica(conn, paramInSPRetiro);
			
						
			if (MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES) > 0){
				return out;
			}
			for (RetiroCustodia.RetiroCustodiaSalida.ObjDatosSalida c: resultRetiros.P_OUT_DATOSSALIDA){
				mapMsg.clear();
				String cantidad = "0";
				String folioCustodia = "0";
				if (c.Cantidad != null) {
					cantidad =  c.Cantidad.toString();
				}
				if (c.FolioCustodia!= null) {
					folioCustodia = c.FolioCustodia.toString();
				}
				mapMsg.put("v_Cantidad", cantidad);
				mapMsg.put("v_FolioCustodia", folioCustodia );
				out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_OK,"UI_RETIRO_TITULO_OK",mapMsg,conn,in.p_IdEmpresa));
				Id++;
			}
			log.write("FIN::MRF.RetiroCustodia");

			
	    }

		
		//consultar preferencia Pago portafolio
		parPrefPor.put("IdPortafolio", parPre);
		List<PreferenciaPagoPortafolio> prefPagoPortafolio = _sql.query(conn, SQL_SELECT_PREFERENCIA_PAGO_PORTAFOLIO, parPrefPor, PreferenciaPagoPortafolio.class);
		CrearIngresoEgreso_Salida tesOut = new CrearIngresoEgreso_Salida();
		List<DetalleOperacion> detallesOperacion = new ArrayList<DetalleOperacion>();
		HashMap<Long,String> mapeoReferenciaIdOperacion = new HashMap<Long,String>();
		HashMap<Long,DetalleOperacion> hashDetalleOperaciones =  new HashMap<Long,DetalleOperacion>();
		for(DetalleOperacion operacion: detOpe){
			DetalleOperacion detalleOperacion = hashDetalleOperaciones.get(operacion.IdOperacion);
			if(detalleOperacion == null) {
				detallesOperacion.add(operacion);
				hashDetalleOperaciones.put(operacion.IdOperacion, operacion);
			}
		}
		
		   
	    //RECALCULAR MONTOS CON RESPECTO AL MONTO DE ANTICIPO
	    
	    List<ComprobantePagoOperacion> nuevaFpag = recalculaMontosAnticipos(MontoOperacion,MontoAnticipo,comprobantePagoOperacion,hashDetalleOperaciones);
	    comprobantePagoOperacion.clear();
	    comprobantePagoOperacion.addAll(nuevaFpag);
	    
	    //ACTUALIZA MONTOS DE LAS FORMAS DE PAGOS AL ANTICIPAR
	    _sql.batch(conn, SQL_UPDATE_FORMAS_DE_PAGOS, nuevaFpag, ComprobantePagoOperacion.class); 
		
		
	    {
			//genero INGRESO EGRESO a Tesoreria
			CrearIngresoEgreso tes = new CrearIngresoEgreso();
			CrearIngresoEgreso_Entrada entTes = new CrearIngresoEgreso_Entrada();
			entTes.p_IdEmpresa = in.p_IdEmpresa;
			entTes.p_IdUsuario = in.p_IdUsuario;
			entTes.p_IdModulo = in.p_IdModulo;
			entTes.p_SimularOperacion = "N";
			entTes.p_UsaReglasNegocio = in.p_UsaReglasNegocio;		
			entTes.p_EsIngresoAutomatico = "S";        //S: Ingreso automático   N: Ingreso Manual
			entTes.p_FechaMovimiento = fechaSistema;   // Fecha de Sistema(si no viene le asigna fecha sistema)  //////// nuevo jv
			Long numeroReferenciaFormaPago = 1L;
			Long numeroReferencia = 1L;
			
			for (ComprobantePagoOperacion fPag :comprobantePagoOperacion){
				DetalleOperacion ope = hashDetalleOperaciones.get(fPag.IdOperacion);
				if ((ope.IdTipoOperacion.equals("COMPAC") && fPag.RecibePaga.equals("R"))
						|| (ope.IdTipoOperacion.equals("VENPAC") && fPag.RecibePaga.equals("R"))) {
					if(fPag.AfectarTesoreria.equals("S")) {
						FormaDePagoMesa formaDePago = seleccionaFormaPagoMesa(FormaDePagoMesaList, fPag.IdFormaDePago);
						IngresoEgreso_Entrada IEGen = new IngresoEgreso_Entrada();
						
						IEGen.NumeroReferencia 				= numeroReferencia;  				
						IEGen.IdTipoMovimiento 				= ope.IdTipoOperacion.contains("COM")?tipoMovTes.get("R"):tipoMovTes.get("P");  	
						IEGen.IdTipoPersona 				= "CLI";  		 	
						IEGen.IdPersona						= ope.IdPersona;  		 		
						IEGen.IdPortafolio 					= ope.IdPortafolio;  		 			
						IEGen.IdMoneda 						= ope.IdMonedaLiquidacionAnticipo;
						IEGen.Monto 						= fPag.Monto;
						IEGen.FechaLiquidacion 				= fechaSistema;                                                                 
						IEGen.IdConcepto 					= conceptoAnticipo.IdConcepto;  	
						IEGen.IdTipoPersonaTitularPago 		= "CLI"; 
						IEGen.IdPersonaTitularPago 			= ope.IdPersona; 
						IEGen.IdPortafolioTitularPago 		= ope.IdPortafolio;
						IEGen.IdCaja 						= 0L; 
						IEGen.SecuenciaAgrupacion 			= ""; 	
						IEGen.SecuenciaRelacionada 			= 0L;	
						IEGen.Glosa 						= this.GlosaAnticipo; 										   	
					   	//Parametros Referenciales(Opcionales)
						IEGen.IdEmpleadoDigitador			= in.p_IdUsuario;  	
						IEGen.IdSucursalEmpleadoDigitador	= idSucursal.size() > 0 ? idSucursal.get(0).IdSucursal : 0L; 
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
						IEGen.IdMonedaOperacion				= ope.IdMonedaLiquidacionAnticipo;  
						IEGen.MontoOperacion				= ope.MontoOperacionAnticipo;
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
						FPagTes.IdMoneda					= ope.IdMonedaLiquidacionAnticipo;
						FPagTes.Monto						= fPag.Monto;
						FPagTes.IdCuentaBancariaCorredora	= ope.IdTipoOperacion.equals("COMPAC") ? fPag.IdCuentaBanco : 0L; 		
						FPagTes.IdTarjetaBancariaCorredora	= 0L; 	
						FPagTes.IdChequera					= 0L; 	
						FPagTes.NumeroDocumentoCorredora	= 0L; 	
						FPagTes.FechaDocumentoPagoCorredora	= "0001-01-01 00:00:00";
						
						//areglar a la por defecto del cliente
						if (prefPagoPortafolio.size() > 0) {
							
							for (PreferenciaPagoPortafolio pref : prefPagoPortafolio){
								if (fPag.IdFormaDePago.contains(pref.IdPreferenciaPago) && pref.IdPreferenciaPago.contains(ope.IdMonedaLiquidacionAnticipo) && pref.IdCuentaBancaria.compareTo(fPag.IdCuentaBanco) == 0 ){
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
									FPagTes.IdCuentaBancariaSocio		= 0L; 			
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
								FPagTes.IdCuentaBancariaSocio		= ope.IdTipoOperacion.equals("VENPAC") ? fPag.IdCuentaBanco : 0L;			
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
						FPagTes.FechaDocumentoPagoSocio		= Fecha.toFormat(ope.FechaLiquidacion,"yyyy-MM-dd HH:mm:ss");  		//	NVARCHAR(40)
						entTes.p_FormaDePago.add(FPagTes);
						
						mapeoReferenciaIdOperacion.put(numeroReferencia, fPag.IdFormaDePago);
						numeroReferencia++;
						numeroReferenciaFormaPago++;
					}
				}
				
			}
			
			log.write("MRF.Llamada Ingreso Tesoreria");
			if (entTes.p_IngresoEgresoGenerar.size() > 0 && entTes.p_FormaDePago.size() > 0){
				tesOut = tes.logica(conn, entTes);
				
				if(tesOut.P_OUT_MENSAJES.size()>0) {
					out.P_OUT_MENSAJES.addAll(tesOut.P_OUT_MENSAJES);
				}				
				//if (MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES) > 0){
				//	return out;
				//}
			}
		}
	    CrearCargoAbonoSalida ctaOut = new CrearCargoAbonoSalida();
	    {
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
			for (ComprobantePagoOperacion fPag :comprobantePagoOperacion){
				DetalleOperacion ope = hashDetalleOperaciones.get(fPag.IdOperacion);
				if ((ope.IdTipoOperacion.equals("COMPAC") && fPag.RecibePaga.equals("R"))
						|| (ope.IdTipoOperacion.equals("VENPAC") && fPag.RecibePaga.equals("R"))) {
					Long IdMovtoTeso = 0L;
					for (IngresoEgresoGenerado_Salida tesOutGen : tesOut.P_OUT_INGRESOEGRESOGENERADO){
						String idFormaPago = mapeoReferenciaIdOperacion.get(tesOutGen.NumeroReferencia) != null ? mapeoReferenciaIdOperacion.get(tesOutGen.NumeroReferencia) : "";
						if (fPag.IdFormaDePago.equals(idFormaPago)){
							IdMovtoTeso = tesOutGen.IdIngresoEgreso;
						}
					}
					ObjCargoAbonoAGenerar objCtaInCli = new ObjCargoAbonoAGenerar();
					ObjCargoAbonoAGenerar objCtaInCorr = new ObjCargoAbonoAGenerar();
					objCtaInCli.NumeroCargoAbono 				= numeroCargoAbono; 
					objCtaInCli.IdPortafolio 					= ope.IdPortafolio; 
					objCtaInCli.IdMoneda 						= ope.IdMonedaLiquidacionAnticipo;//Liquidacion; 
					objCtaInCli.IdTipoMovimiento 				= ope.IdTipoOperacion.contains("COM")?tipoMovCtaCli.get("R"):tipoMovCtaCli.get("P");; 
					objCtaInCli.IdConcepto						= conceptoAnticipo.IdConcepto;
					objCtaInCli.Descripcion						= conceptoAnticipo.NombreConcepto; 
					objCtaInCli.IdCondicionDeLiquidacion		= ope.IdCondicionLiquidacionAnticipo; 
					objCtaInCli.FechaCondicionDeLiquidacion		= Fecha.toFormat(Fecha.toDate(fechaSistema),"YYYY-MM-dd"); 
					objCtaInCli.Monto							= fPag.Monto; 
					objCtaInCli.IdEjecutivo						= in.p_IdUsuario; 
					objCtaInCli.IdSucursal						= idSucursal.get(0).IdSucursal; 
					objCtaInCli.IdTipoProducto					= ope.IdTipoProducto; 
					objCtaInCli.IdTipoOperacion					= ope.IdTipoOperacion; 
					objCtaInCli.IdOperacion						= fPag.IdOperacion; 
					objCtaInCli.FechaOperacion					= Fecha.toFormat(Fecha.toDate(fechaSistema),"YYYY-MM-dd"); 
					objCtaInCli.FechaLiquidacionOperacion		= Fecha.toFormat(Fecha.toDate(fechaSistema),"YYYY-MM-dd"); 
					objCtaInCli.FechaVencimientoOperacion		= Fecha.toFormat(Fecha.toDate(fechaSistema),"YYYY-MM-dd"); 
					objCtaInCli.IdMonedaOperacion				= ope.IdMonedaLiquidacionAnticipo; 
					objCtaInCli.MontoOperacion					= ope.MontoOperacionFinalMonedaAnticipo; 
					objCtaInCli.TipoCambioOperacion				= BigDecimal.ZERO; 
					objCtaInCli.IdContraparte					= 0L; 
					objCtaInCli.IdTipoComprobante				= "MES"; 
					objCtaInCli.IdComprobante					= fPag.IdOperacion; 
					objCtaInCli.FolioComprobante				= ope.FolioOperacion.toString(); 
					objCtaInCli.IdComprobanteDetalle			= 0L; 
					objCtaInCli.IdComprobanteFormaDePago		= 0L; 
					objCtaInCli.IdTipoMovimientoAsociado		= ope.IdTipoOperacion.contains("COM")?tipoMovTes.get("P"):tipoMovTes.get("R"); 
					objCtaInCli.IdMovimientoAsociado			= IdMovtoTeso; 
					objCtaInCli.FechaComprobante				= Fecha.toFormat(Fecha.toDate(fechaSistema),"YYYY-MM-dd"); 
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
			log.write("MRF.Llamada Ingreso Cuenta Inversión");
			ctaOut = cta.logica(conn, entCta);
			
			if(ctaOut.P_OUT_MENSAJES.size()>0) {
				out.P_OUT_MENSAJES.addAll(ctaOut.P_OUT_MENSAJES);
			}
		}
	     
	    ///Cambio estado para las operaciones
	    CambiarEstadoOperacionEntrada inCambioEstado = new CambiarEstadoOperacionEntrada(in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo, "N", in.p_UsaReglasNegocio, idPaisSistema);
	    for (DetalleOperacion det: detOpe){
		    ObjCambiarEstadoOperacionIN estadoOp = new ObjCambiarEstadoOperacionIN(); 
		    estadoOp.IdOperacion 					= det.IdOperacion;
		    estadoOp.IdTipoProducto 				= det.IdTipoProducto;
		    estadoOp.IdNuevoEstado 					= ((Operacion)inOperacionesHash.get(det.IdOperacion)).IdEstadoSiguiente;
		    estadoOp.IdReserva						= det.IdReserva;
		    estadoOp.FolioCarteraCustodia			= det.FolioCarteraDestinoDetalle;
		    estadoOp.MontoOperacionAnticipo			= det.MontoOperacionAnticipo;
		    estadoOp.MontoOperacionInicialAnticipo	= det.MontoOperacionInicialAnticipo;
		    estadoOp.MontoOperacionFinalAnticipo	= det.MontoOperacionFinalAnticipo;
		    estadoOp.TasaAnticipo					= det.TasaAnticipo;
		    estadoOp.MontoOperacionMonedaAnticipo	= det.MontoOperacionMonedaAnticipo;
		    estadoOp.MontoOperacionInicialMonedaAnticipo	= det.MontoOperacionInicialMonedaAnticipo;
		    estadoOp.MontoOperacionFinalMonedaAnticipo		= det.MontoOperacionFinalMonedaAnticipo;
		    estadoOp.IdMonedaLiquidacionAnticipo			= det.IdMonedaLiquidacionAnticipo;
		    estadoOp.IdCondicionLiquidacionAnticipo			= det.IdCondicionLiquidacionAnticipo;
		    estadoOp.FechaCondicionLiquidacionAnticipo		= Fecha.toDate(det.FechaLiquidacionAnticipo);
		    estadoOp.EstadoConfirmacion = "NCF";
		    estadoOp.Glosa = this.GlosaAnticipo;
		    
			inCambioEstado.p_DataCambiarEstado.add(estadoOp);
	    }
	    log.write("INICIO::MRF.CambiarEstadoOperacion");
		CambiarEstadoOperacion cambioEstado = new CambiarEstadoOperacion();
		CambiarEstadoOperacionSalida cambioEstadoOut = cambioEstado.logica(conn, inCambioEstado);
		out.P_OUT_MENSAJES.addAll(cambioEstadoOut.P_OUT_MENSAJES);
		out.P_OUT_OPERACION.addAll(cambioEstadoOut.P_OUT_OPERACION);
		log.write("FIN::MRF.CambiarEstadoOperacion");
	    log.write("FIN::MRF.AnticiparOperacionMesa");
	    //CacheValorizacion.valorizar(conn);
	    
	    List<MensajeSalida> outFilter = new ArrayList<MensajeSalida>();
	    for(MensajeSalida   msj : out.P_OUT_MENSAJES) {
	    	if(msj.tipo.equals("ERROR")) {
	    		outFilter.add(msj);
	    	}
	    	
	    }
	    out.P_OUT_MENSAJES.clear();
	    out.P_OUT_MENSAJES.addAll(outFilter);
	    out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
	    
	    if(out.P_OUT_ERRORESCONTADOR == 0L) {
	    	CacheServicio.invalidarPorTag(CacheMRF.TAG_POSICION);
	    	AgregarMensajeWebsocket.agregar(conn, in.p_IdEmpresa,"ANTMRF","Refresco Anticipar Operacion MRF" );
	    }
	    
		return out;
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		Gson gson = Utils.getGsonBuilder();
		AnticiparOperacionMesa_IN jin = gson.fromJson(in.toString(), AnticiparOperacionMesa_IN.class);
		AnticiparOperacionMesa_OUT out = logica(conn, jin);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	};
	
	// ------------------------------------------------------------
	// METODOS UTILITARIOS
	// ------------------------------------------------------------

	private FormaDePagoMesa seleccionaFormaPagoMesa(List<FormaDePagoMesa> FormaDePagoMesaList, String preferenciaPago) {
		List<FormaDePagoMesa> listaFormaPago = FormaDePagoMesaList.stream().filter(c -> c.IdPreferenciaPago.equals(preferenciaPago)).collect(Collectors.toList());
		FormaDePagoMesa fp = new FormaDePagoMesa();
		if (listaFormaPago.size() > 0) {
			fp = listaFormaPago.get(0);
		}

		return fp;
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
		
	public List<ComprobantePagoOperacion> recalculaMontosAnticipos(BigDecimal MontoOperacion,BigDecimal MontoAnticipo, List<ComprobantePagoOperacion> ListaFormasPago, HashMap<Long, DetalleOperacion> hashDetalleOperaciones) throws Exception{
		
		List<ComprobantePagoOperacion> nuevaFpag = new ArrayList<ComprobantePagoOperacion>();
		ComprobantePagoOperacion ObjFpag;  
		
		if(MontoOperacion.compareTo(MontoAnticipo)!=0){
			BigDecimal MontoRecalculado = BigDecimal.ZERO;
			
			for(ComprobantePagoOperacion fPag : ListaFormasPago ){
				DetalleOperacion ope = hashDetalleOperaciones.get(fPag.IdOperacion);
				if ((ope.IdTipoOperacion.equals("COMPAC") && fPag.RecibePaga.equals("R"))
						|| (ope.IdTipoOperacion.equals("VENPAC") && fPag.RecibePaga.equals("R"))) {
					ObjFpag =  new ComprobantePagoOperacion();
					BigDecimal porcentaje = (fPag.Monto.multiply(new BigDecimal(100)).divide(MontoOperacion,20,RoundingMode.HALF_UP)).divide(new BigDecimal(100),20,RoundingMode.HALF_UP);
					BigDecimal nuevoMonto = (porcentaje.multiply(MontoAnticipo)).setScale(0, BigDecimal.ROUND_UP);
									
					Utils.copyPojo(fPag, ObjFpag);
					ObjFpag.Monto= nuevoMonto;
					nuevaFpag.add(ObjFpag);
					MontoRecalculado=MontoRecalculado.add(ObjFpag.Monto);
				}
				
			}
			
			//AJUSTAR
			if(MontoRecalculado.compareTo(MontoAnticipo)!=0){
				BigDecimal DiferenciaAjuste = BigDecimal.ZERO;
				
				if(MontoRecalculado.compareTo(MontoAnticipo)>0){
					DiferenciaAjuste = MontoRecalculado.subtract(MontoAnticipo);
				}
				if(MontoRecalculado.compareTo(MontoAnticipo)<0){
					DiferenciaAjuste = MontoAnticipo.subtract(MontoRecalculado);
				}
				
				nuevaFpag.get(0).Monto= nuevaFpag.get(0).Monto.subtract(DiferenciaAjuste);	
				
			}
			
			
		}else{
			nuevaFpag.addAll(ListaFormasPago);
		}
		
		return nuevaFpag;
	}
	
	public static void main(String[] args)  throws Exception,SQLException {
		
		/*
		BigDecimal N1 = (new BigDecimal (85121833340.27099235165814651944)).setScale(0, BigDecimal.ROUND_UP);
		BigDecimal N2 = (new BigDecimal (219821926847.48607996888889883470)).setScale(0, BigDecimal.ROUND_UP);
		BigDecimal Monto = (new BigDecimal (304943760187.75707232054704535414)).setScale(0, BigDecimal.ROUND_UP);
		BigDecimal Suma = N1.add(N2);
		
		System.out.println("N1 : "+N1);
		System.out.println("N2 : "+N2);
		System.out.println("Suma : "+Suma);
		System.out.println("Monto : "+Monto);*/
	
		
		// TODO Auto-generated method stub
		Connection conn = null;
		Gson gson = new Gson();
		
		try {
			System.out.println("== BEGIN ==");
			conn = PlataformaUtils.getDESAConnection();
			
			String jsonRF = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":97,\"p_IdModulo\":\"MRF\",\"p_UsaReglasNegocio\":\"S\",\"p_Operacion\":[{\"IdInternoOperacion\":1,\"IdOperacion\":169913,\"FolioOperacion\":161,\"IdCliente\":2715,\"IdPortafolio\":null,\"IdClienteCorredor\":2976,\"IdPortafolioCorredor\":4913,\"Intermesa\":\"N\",\"IdMesaOrigen\":14,\"IdMesaDestino\":14,\"AfectaCustodia\":\"ENV\",\"IdTipoProducto\":\"MERF\",\"IdTipoOperacion\":\"VENPAC\",\"IdTipoReajuste\":\"USD\",\"Monto\":2813323,\"MontoMoneda\":4130.61856729,\"IdMoneda\":\"CLP\",\"IdMonedaLiquidacion\":\"USD\",\"Tasa\":1.75,\"ValorFinal\":2814553.8288,\"ValorFinalMoneda\":4132.4257,\"IdCondicionLiquidacion\":\"T0\",\"FechaLiquidacion\":\"2019-05-16\",\"IdEstadoSiguiente\":\"ANT\",\"PlazoAnticipo\":9}]}";
			
			AnticiparOperacionMesa_IN objIn = gson.fromJson(jsonRF, AnticiparOperacionMesa_IN.class);
			AnticiparOperacion demo = new AnticiparOperacion();
			AnticiparOperacionMesa_OUT ObjOut=demo.logica(conn, objIn);
			System.out.println(ObjOut.toString());
			
			
			conn.rollback();
			//conn.commit();
		}catch (Exception e){
			e.printStackTrace();
		}finally {
			conn.close();
		}
		System.out.println("== END ==");
	
	};
	
}
