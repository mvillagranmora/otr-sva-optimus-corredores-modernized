package cl.bcs.mesa.MRF;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.cuentaInversion.CrearCargoAbono;
import cl.bcs.cuentaInversion.CrearCargoAbono.CrearCargoAbonoEntrada;
import cl.bcs.cuentaInversion.CrearCargoAbono.CrearCargoAbonoSalida;
import cl.bcs.cuentaInversion.CrearCargoAbono.ObjCargoAbonoAGenerar;
import cl.bcs.cuentaInversion.CrearCargoAbono.ObjCargoAbonoGenerado;
import cl.bcs.custodia.utils.CacheValorizacion;
import cl.bcs.custodia.utils.IngresoCustodia;
import cl.bcs.custodia.utils.IngresoCustodia.IngresoCustodiaEntrada;
import cl.bcs.custodia.utils.IngresoCustodia.IngresoCustodiaSalida;
import cl.bcs.custodia.utils.IngresoCustodia.IngresoCustodiaSalida.ObjDatosSalida;
import cl.bcs.custodia.utils.IngresoCustodia.ObjDatosEntrada;
import cl.bcs.custodia.utils.LogCustodia;
import cl.bcs.custodia.utils.LogCustodia.ObjMovimiento;
import cl.bcs.mesa.MES.ConsultarInstrumento;
import cl.bcs.mesa.MES.ConsultarInstrumento.InformacionInstrumento;
import cl.bcs.mesa.MES.ConsultarTipoInstrumento;
import cl.bcs.mesa.MES.ConsultarTipoInstrumento.ConsultarTipoInstrumentoIN;
import cl.bcs.mesa.MES.ConsultarTipoInstrumento.InformacionTipoInstrumento;
import cl.bcs.mesa.MES.CrearComprobantePagoOperacion;
import cl.bcs.mesa.MES.CrearComprobantePagoOperacion.ComprobantePagoOperacionObj_IN;
import cl.bcs.mesa.MES.CrearComprobantePagoOperacion.ComprobantePagoOperacionObj_OUT;
import cl.bcs.mesa.MES.CrearComprobantePagoOperacion.CrearComprobantePagoOperacion_IN;
import cl.bcs.mesa.MES.CrearComprobantePagoOperacion.CrearComprobantePagoOperacion_OUT;
import cl.bcs.mesa.MES.IngresoOperacion;
import cl.bcs.mesa.MES.IngresoOperacion.IngresoOperacion_IN;
import cl.bcs.mesa.MES.IngresoOperacion.IngresoOperacion_OUT;
import cl.bcs.mesa.MES.IngresoOperacion.OperacionObjIn;
import cl.bcs.mesa.MES.IngresoOperacion.OperacionObjOut;
import cl.bcs.mesa.MRF.CrearOperacionMesa.Concepto;
import cl.bcs.mesa.MRF.CrearOperacionMesa.CrearOperacionMesa_FormaPagoOperacion_OUT;
import cl.bcs.mesa.MRF.CrearOperacionMesa.CrearOperacionMesa_Operacion_OUT;
import cl.bcs.mesa.MRF.CrearOperacionMesa.FormaDePagoMesa;
import cl.bcs.mesa.MRF.CrearOperacionMesa.Persona;
import cl.bcs.mesa.MRF.CrearOperacionMesa.PreferenciaPagoPortafolio;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.tesoreria.CrearIngresoEgreso;
import cl.bcs.tesoreria.CrearIngresoEgreso.CrearIngresoEgreso_Entrada;
import cl.bcs.tesoreria.CrearIngresoEgreso.CrearIngresoEgreso_Salida;
import cl.bcs.tesoreria.CrearIngresoEgreso.FormaDePago_Entrada;
import cl.bcs.tesoreria.CrearIngresoEgreso.IngresoEgresoGenerado_Salida;
import cl.bcs.tesoreria.CrearIngresoEgreso.IngresoEgreso_Entrada;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;



public class TomarDepositoDesembolso implements JavaCall{
	private final Log log = new Log("MRF.TomarDepositoDesembolso");
	private final String SQL_UPDATE_OPERACIONES = loadResourceToString("cl/bcs/mesa/sql/IngresoOperacion_UpdateOperacion.sql");
	private final String SQL_CONSULTAR_INSTRUMENTO = loadResourceToString("cl/bcs/mesa/sql/ConsultarInstrumento.sql");
	private final String SQL_CONSULTAR_TIPO_INSTRUMENTO = loadResourceToString("cl/bcs/mesa/sql/ConsultarTipoInstrumento.sql");
	SQL _sql = new SQL();
	
	public static class CrearOperacionDepositoDesembolso_Operacion_IN {
		public String 		Accion;
		public Long 		IdInternoOperacion;
		public Long 		IdOperacion;
		public Long			FolioOperacion;
		public Long			FolioCartera;//para pactos y depositos y desembolso
		public Long			IdCliente;
		public Long			IdPortafolio;
		public Long			IdClienteCorredor;
		public Long			IdPortafolioCorredor;
		public String		Intermesa;
		public Long			IdMesaOrigen;
		public Long			IdMesaDestino;
		public String		AfectaCustodia;
		public String		IdTipoProducto;//'MERF'
		public String		IdTipoOperacion;//COMTER,VENTER,COMPAC,VENPAC
		public String		IdTipoReajuste;//"USD","UF"
		public Long			Plazo;
		public BigDecimal	Monto;
		public BigDecimal	MontoMoneda;
		public String		IdMoneda;//CLP "USD","UF" moneda local
		public String		IdMonedaLiquidacion; //CLP "USD","UF"
		public BigDecimal	Tasa;
		public BigDecimal	ValorFinal;
		public BigDecimal	ValorFinalMoneda;
		public BigDecimal	TasaAgente;
		public String 		FechaOperacion;
		public String		FechaVencimiento;
		public String		IdCondicionLiquidacion;
		public String		FechaLiquidacion;
		public Long			IdAgente;
		public Long			IdOperador;
		public String		IdEstado;
		public String		Glosa;
		public String		IdTipoCanalIngreso;
		public Concepto 	Concepto;
		public Persona		Persona;
		//Nuevo deposito desembolso
		public String		IdEmisor;
				
	 	public String toString() {
			return Utils.printJson(this, false);
		};
	}
	public static class CrearOperacionDepositoDesembolso_FormaPagoOperacion_IN {
		public Long 		IdInternoPagoOperacion;
		public Long			IdInternoOperacion;
		public Long 		IdComprobantePagoOperacion;
		public String 		IdFormaDePago;
		public String 		IdMoneda;
		//public String 		IdMonedaLiquidacion;
		public Long 		IdOperacion;
		public String 		RecibePagaPortafolio;
		public BigDecimal 	MontoFormaDePago;
		//public BigDecimal 	MontoLiquidacionFormaDePago;
		public String		FechaLiquidacionFormaDePago;
		public String 		AfectarTesoreria;
		public String		IdTipoMovimientoTes;
		public String		IdTipoMovimientoCtaCli;
		public String		IdTipoMovimientoCtaCorr;
		public String		IdModalidadDePago;
		public FormaDePagoMesa FormaDePago;
		public PreferenciaPagoPortafolio PreferenciaDePago;
		public String		IdEstado;
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	};
	public static class TomarDepositoDesembolso_IN {
		public Long												p_IdEmpresa;
	    public Long												p_IdUsuario;
	    public Long												p_IdPais;
	    public String											p_MonedaSistema;
	    public String											p_FechaSistema;
	    public Long												p_IdSucursal;
	    public String											p_IdModulo; 
	    public String											p_UsaReglasNegocio;
		public List<CrearOperacionDepositoDesembolso_Operacion_IN>	p_Operacion;
		public List<CrearOperacionDepositoDesembolso_FormaPagoOperacion_IN> p_FormasDePago;
		
		public TomarDepositoDesembolso_IN(){
			this.p_Operacion = new ArrayList<CrearOperacionDepositoDesembolso_Operacion_IN>();
			this.p_FormasDePago = new ArrayList<CrearOperacionDepositoDesembolso_FormaPagoOperacion_IN>();
		}
		public String toString() {
			return Utils.printJson(this, false);
		};
	}
	public static class TomarDepositoDesembolso_OUT {
		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;

		public List<CrearOperacionMesa_Operacion_OUT>		P_OUT_OPERACION;
		public List<CrearOperacionMesa_FormaPagoOperacion_OUT>	P_OUT_FORMASDEPAGO;
		
		public TomarDepositoDesembolso_OUT() {
			this.P_OUT_OPERACION 		= new ArrayList<CrearOperacionMesa_Operacion_OUT>();
			this.P_OUT_FORMASDEPAGO		= new ArrayList<CrearOperacionMesa_FormaPagoOperacion_OUT>();
			this.P_OUT_MENSAJES 		= new ArrayList<MensajeSalida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class TomarDepositoDesembolsoUpdate {
		public Long IdOperacion;
		public Long FolioCartera;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	private List<ObjMovimiento>  cargaLogDatosCustodia(Connection conn, TomarDepositoDesembolso_IN in, HashMap<String, Object> tipoInst, HashMap<String, Object> inst) throws Exception{
		List<ObjMovimiento> salida = new ArrayList<>();
		try {
			String IdBovedaDefecto	= ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa,"MESA", "IDBOVEDA_DEPOSITO_AUTODESEMBOLSO");
			
			for (CrearOperacionDepositoDesembolso_Operacion_IN ent : in.p_Operacion){
				ObjMovimiento objCus = new ObjMovimiento();
				objCus.IdGenerarMovimientoCustodia 		= ent.IdOperacion;
				objCus.EstaCertificando = "N";
				//--FolioComprobanteBusqueda--FolioCustodia--FolioCartera
				objCus.FolioCustodiaBusqueda 			= ent.IdOperacion;
				objCus.FolioComprobante 				= ent.IdOperacion;
				objCus.IdTipoComprobante				= "MES";
			//--OPERACION
				objCus.IdEmpresa						= in.p_IdEmpresa;
				objCus.IdPais							= in.p_IdPais;
				objCus.FechaOperacion					= Fecha.toDate(ent.FechaOperacion);
				objCus.FechaVencimientoOperacion		= Fecha.toDate(ent.FechaVencimiento);
				objCus.PlazoEnDiasOperacion				= ent.Plazo;
				objCus.EsNominalIngreso					= "S";
				objCus.IdClienteIngreso					= ent.IdCliente;
				objCus.IdPortafolioIngreso				= ent.IdPortafolio;
				objCus.EsNominalRetiro					= "S";
				//objCus.IdClienteRetiro					= ;
				//objCus.IdPortafolioRetiro;
				objCus.IdTipoProducto					= ent.IdTipoProducto;
				objCus.IdTipoCanalIngreso				= ent.IdTipoCanalIngreso;
				objCus.IdMonedaSistema					= in.p_MonedaSistema;
				//--CodigoIFRS
				objCus.IdConcepto						= ent.Concepto.IdConcepto;
				objCus.IdTipoOperacion					= ent.IdTipoOperacion;
				objCus.IdGrupoOperacion					= ent.IdOperacion;
				objCus.IdOperacion						= ent.IdOperacion;
				objCus.IdMesa							= ent.IdMesaOrigen;
				objCus.IdBolsa							= ((InformacionTipoInstrumento) tipoInst.get(ent.IdTipoOperacion)).IdBolsa;
				objCus.IdSucursal						= in.p_IdSucursal;
				objCus.IdAgente							= ent.IdAgente;
				objCus.IdCondicionLiquidacion			= ent.IdCondicionLiquidacion;
				objCus.FechaLiquidacion					= Fecha.toDate(ent.FechaLiquidacion);
				objCus.Glosa							= ent.Glosa;
				objCus.IdUsuario						= in.p_IdUsuario;
				objCus.IdModulo							= in.p_IdModulo;	
				objCus.TipoCarteraCorredor				= "";
			//--INSTRUMENTO
				objCus.IdClasificacionTipoInstrumento	= ((InformacionInstrumento) inst.get(((InformacionTipoInstrumento) tipoInst.get(ent.IdTipoOperacion)).IdInstrumento)).IdClasificacionTipoInstrumento;
				objCus.IdTipoInstrumento				= ((InformacionInstrumento) inst.get(((InformacionTipoInstrumento) tipoInst.get(ent.IdTipoOperacion)).IdInstrumento)).IdTipoInstrumento;
				objCus.IdInstrumento					= ((InformacionInstrumento) inst.get(((InformacionTipoInstrumento) tipoInst.get(ent.IdTipoOperacion)).IdInstrumento)).IdInstrumento;
				objCus.Nemotecnico						= ((InformacionInstrumento) inst.get(((InformacionTipoInstrumento) tipoInst.get(ent.IdTipoOperacion)).IdInstrumento)).Nemotecnico;
				objCus.IdEmisor							= "";
				objCus.IdTipoReajuste					= ent.IdTipoReajuste;
				objCus.FechaEmisionInstrumento			= Fecha.toDate(ent.FechaOperacion);
				objCus.FechaVencimientoInstrumento		= Fecha.toDate(ent.FechaVencimiento);
				objCus.EsFungible						= ((InformacionInstrumento) inst.get(((InformacionTipoInstrumento) tipoInst.get(ent.IdTipoOperacion)).IdInstrumento)).EsDivisible;
				objCus.IdMonedaInstrumento				= ent.IdTipoReajuste;
				objCus.IdBoveda							= IdBovedaDefecto;
			//--CANTIDADES Y MONTOS
				objCus.Cantidad							= BigDecimal.ONE;
				objCus.CorteMinimo						= BigDecimal.ZERO;
				objCus.PrecioCompra						= BigDecimal.ZERO;
				objCus.TasaCompra						= ent.Tasa;
				objCus.MontoCompra						= ent.Monto;
				objCus.MontoCompraMonedaInstrumento		= ent.MontoMoneda;
				objCus.MontoAPlazo						= ent.ValorFinal;
				objCus.MontoAPlazoMonedaInstrumento		= ent.ValorFinalMoneda;
				objCus.ValorRescate						= ent.ValorFinal;
				/* deposito desemboso no lleva detalle
			//--INSTRUMENTO RELACIONADO
				objCus.IdClasificacionTipoInstrumentoRelacionado;
				objCus.IdTipoInstrumentoRelacionado;
				objCus.IdInstrumentoRelacionado;
				objCus.NemotecnicoRelacionado;
				objCus.IdEmisorRelacionado;
				objCus.IdTipoReajusteRelacionado;
				objCus.FechaEmisionInstrumentoRelacionado;
				objCus.FechaVencimientoInstrumentoRelacionado;
				objCus.EsFungibleRelacionado;
				objCus.IdBovedaRelacionado;
				objCus.IdMonedaInstrumentoRelacionado;
			//--CANTIDADES Y MONTOS INSTRUMENTO RELACIONADO
				objCus.CantidadInstrumentoRelacionado;
				objCus.ValorRescateInstrumentoRelacionado;
				objCus.PrecioCompraInstrumentoRelacionado;
				objCus.TasaCompraInstrumentoRelacionado;
				objCus.MontoCompraInstrumentoRelacionado;
				objCus.MontoCompraMonedaInstrumentoRelacionado;
				*/
				salida.add(objCus);
			}
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return salida;
	}
	
	public TomarDepositoDesembolso_OUT logica (Connection conn, TomarDepositoDesembolso_IN in) throws Exception {
		SQL _sql = new SQL();
		TomarDepositoDesembolso_OUT out = new TomarDepositoDesembolso_OUT();
		Long Id = 0L,errTot = 0L;
		Long TicketAtencion = 0L;
		HashMap<String,String> mapMsg = new HashMap<>();
		log.write("INICIO::MRF.TomarDepositoDesembolso");
		try {
			//CUSTODIA
			//INGRESO OPERACION
			//FORMA DE PAGO
			//TESORERIA
			//CUENTA INVERSION
			List<OperacionObjIn> v_Operacion = new ArrayList<>();
			Map<Object,Object> v_OutOperacionHash = new HashMap<>();
			Map<Object,Object> v_OutComprobantePagoHash = new HashMap<>();
			IngresoOperacion_OUT resultOperacion = new IngresoOperacion_OUT();
			TomarDepositoDesembolso_IN operacionCerrar = new TomarDepositoDesembolso_IN();
			TomarDepositoDesembolso_IN operacionPendiente = new TomarDepositoDesembolso_IN();
			Long cantCerrar = 0L;
			IngresoCustodiaSalida cusOut = new IngresoCustodiaSalida();
			CrearCargoAbonoSalida ctaOut = new CrearCargoAbonoSalida();
			CrearIngresoEgreso_Salida tesOut = new CrearIngresoEgreso_Salida();
			CrearComprobantePagoOperacion_OUT resultCompPag = new CrearComprobantePagoOperacion_OUT();
			ConsultarInstrumento inst = new ConsultarInstrumento();
			List<Long> instIn = new ArrayList<>();
			ConsultarTipoInstrumento tipInst = new ConsultarTipoInstrumento();
			ConsultarTipoInstrumentoIN tipInstIN = new ConsultarTipoInstrumentoIN();
			for (CrearOperacionDepositoDesembolso_Operacion_IN ent : in.p_Operacion){
				tipInstIN.IdTipoOperacion.add(ent.IdTipoOperacion);
			}		
			List<InformacionTipoInstrumento> tipInstOut = tipInst.logica(conn, tipInstIN);
			HashMap<String, Object> tipInstHash = Utils.List2Hash(tipInstOut, "IdTipoOperacion");
			for(InformacionTipoInstrumento tipIns: tipInstOut){
				instIn.add(tipIns.IdInstrumento);
			}
			List<InformacionInstrumento> instOut = inst.logica(conn, instIn);
			HashMap<String, Object> instOutHash = Utils.List2Hash(instOut, "IdInstrumento");
			List<ObjMovimiento>  entLogCus = new ArrayList<>();
			
			{//Ingreso Operacion
				for (CrearOperacionDepositoDesembolso_Operacion_IN operacion : in.p_Operacion){
					OperacionObjIn data = new OperacionObjIn();
					Utils.copyPojo(operacion, data);
					data.Accion = operacion.Accion;//operacion.IdOperacion.equals(0L)? "CREAR":"MODIFICAR";
					data.IdInternoOperacion = operacion.IdInternoOperacion;
					data.MontoOperacion = operacion.Monto;
					data.MontoOperacionInicial = operacion.Monto;					
					data.MontoOperacionFinal = operacion.ValorFinal;			
					data.MontoOperacionMoneda = operacion.MontoMoneda;			
					data.MontoOperacionInicialMoneda = operacion.MontoMoneda;
					data.MontoOperacionFinalMoneda = operacion.ValorFinalMoneda;	
					data.PlazoEnDias = operacion.Plazo;
					data.FechaCondicionLiquidacion = Fecha.toDate(operacion.FechaLiquidacion);	
					data.IdUsuarioCreacion = operacion.IdOperador;
					data.IdEmpresa = in.p_IdEmpresa;							
					data.UsuarioEdicion = in.p_IdUsuario;
					data.FolioOperacion = 0L;									
					data.IdOperacionRelacionada = 0L;						
					data.IdInstrumento = ((InformacionInstrumento) instOutHash.get(((InformacionTipoInstrumento) tipInstHash.get(operacion.IdTipoOperacion)).IdInstrumento)).IdInstrumento;									
					data.IdMonedaPrincipal = operacion.IdMoneda;							
					data.IdMonedaSecundaria = operacion.IdMonedaLiquidacion;
					data.IdModalidadLiquidacion = 0L;							
					data.IdReferenciaMercado = 0L;
					data.IdPlaza = 0L;
					data.TCCierre = BigDecimal.ZERO;
					data.ParidadCierre = BigDecimal.ZERO;									
					data.TCCosto = BigDecimal.ZERO;
					data.PuntosTasasForward  = "";
					data.PuntosForward = BigDecimal.ZERO;
					data.TCForward = BigDecimal.ZERO;
					data.TasasMonedaPrincipal = BigDecimal.ZERO;
					data.TasasMonedaSecundaria = BigDecimal.ZERO;
					data.MontoDiferido = BigDecimal.ZERO;
					data.MontoMargen = BigDecimal.ZERO;
					data.IdFixing = 0L;
					data.FechaFixing = Fecha.toDate("0001-01-01");						
					data.HoraFixing = "00:00:00";
					data.IdSpotAsociado = 0L;									
					data.IdClasificacionCartera = 0L;						
					data.FechaLiquidacion = Fecha.toDate("0001-01-01");	
					data.AccionEdicion = "B";
					data.FechaOperacion = operacion.FechaOperacion;
					data.FechaVencimiento = Fecha.toDate(operacion.FechaVencimiento);
					data.IdEmisor = operacion.IdEmisor;
					
					v_Operacion.add(data);

					//IdTipoOperacion.add(operacion.IdTipoOperacion);
					//IdTipoProducto.add(operacion.IdTipoProducto);
										
				}
				//if (v_operacion.size() > 0){
				IngresoOperacion callOpe = new IngresoOperacion();
				IngresoOperacion_IN inOpe = new IngresoOperacion_IN();
				inOpe.p_IdEmpresa = in.p_IdEmpresa;
				inOpe.p_IdUsuario = in.p_IdUsuario;
				inOpe.p_IdModulo = in.p_IdModulo;
				inOpe.p_simularOperacion = "N"; 
				inOpe.p_UsaReglasNegocio = in.p_UsaReglasNegocio;
				inOpe.p_IdPais = in.p_IdPais;
				inOpe.p_Operaciones = v_Operacion;
				log.write("MRF.Llamada Ingreso Operacion");
				resultOperacion = callOpe.logica(conn, inOpe);
				out.P_OUT_MENSAJES.addAll(resultOperacion.P_OUT_MENSAJES);
				if (MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES) > 0){
					return out;
				}
				//}
			}
			v_OutOperacionHash = Utils.List2Hash(resultOperacion.P_OUT_OPERACION,"IdAplicacion");
			//v_OperacionHash = Utils.List2Hash(v_Operacion,"IdAplicacion");
			
			for (CrearOperacionDepositoDesembolso_Operacion_IN ope: in.p_Operacion){
				ope.IdOperacion = ((OperacionObjOut)v_OutOperacionHash.get(ope.IdInternoOperacion)).IdOperacion;
				ope.FolioOperacion = ((OperacionObjOut)v_OutOperacionHash.get(ope.IdInternoOperacion)).FolioOperacion;
				//ope.IdOperacionRelacionada = ((OperacionObjOut)v_OutOperacionHash.get(ope.IdInternoOperacion)).IdOperacionRelacionada;
				ope.IdEstado = ((OperacionObjOut)v_OutOperacionHash.get(ope.IdInternoOperacion)).IdEstado;
				for (CrearOperacionDepositoDesembolso_FormaPagoOperacion_IN fPag :in.p_FormasDePago){
					fPag.IdOperacion = ((OperacionObjOut)v_OutOperacionHash.get(ope.IdInternoOperacion)).IdOperacion;
					fPag.IdEstado = ope.IdEstado;
				}
				if ("CERRAR".equalsIgnoreCase(ope.Accion)){
					operacionCerrar.p_IdEmpresa = in.p_IdEmpresa;
					operacionCerrar.p_IdUsuario = in.p_IdUsuario;
					operacionCerrar.p_IdPais = in.p_IdPais;
					operacionCerrar.p_MonedaSistema = in.p_MonedaSistema;
					operacionCerrar.p_IdSucursal = in.p_IdSucursal;
					operacionCerrar.p_IdModulo = in.p_IdModulo; 
					operacionCerrar.p_UsaReglasNegocio = in.p_UsaReglasNegocio;
					operacionCerrar.p_Operacion.add(ope);
					for (CrearOperacionDepositoDesembolso_FormaPagoOperacion_IN pag: in.p_FormasDePago){
						if (pag.IdInternoOperacion.equals(ope.IdInternoOperacion))
							operacionCerrar.p_FormasDePago.add(pag);
					}
					cantCerrar++;
				}
				/*
				 else {
					operacionPendiente.p_IdEmpresa = in.p_IdEmpresa;
					operacionPendiente.p_IdUsuario = in.p_IdUsuario;
					operacionPendiente.p_IdPais = in.p_IdPais;
					operacionPendiente.p_MonedaSistema = in.p_MonedaSistema;
					operacionPendiente.p_IdSucursal = in.p_IdSucursal;
					operacionPendiente.p_IdModulo = in.p_IdModulo; 
					operacionPendiente.p_UsaReglasNegocio = in.p_UsaReglasNegocio;
					operacionPendiente.p_Operacion.add(ope);
					for (CrearOperacionMesa_FormaPagoOperacion_IN pag: in.p_FormasDePago){
						if (pag.IdInternoOperacion.equals(ope.IdInternoOperacion))
							operacionPendiente.p_FormasDePago.add(pag);
					}
				}
				*/
				
			}
			if (cantCerrar.compareTo(0L) > 0){
				//01 - LOG (Requerido)
				entLogCus = cargaLogDatosCustodia(conn, operacionCerrar,tipInstHash, instOutHash);
				TicketAtencion = LogCustodia.logCustodia(conn,entLogCus);
				if (TicketAtencion.compareTo(0L) <= 0){
					out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"SIN_TICKET_ATENCION",mapMsg,conn,in.p_IdEmpresa));
					Id++;
				}
			}
			errTot = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
			if (!errTot.equals(0L) ){
				return out;
			}
			
			//Ingreso a Custodia
			if (cantCerrar.compareTo(0L) > 0){
				{
					IngresoCustodiaEntrada cusIn = new IngresoCustodiaEntrada();
					cusIn.p_IdEmpresa = in.p_IdEmpresa;
				    cusIn.p_IdModulo = in.p_IdModulo;
				    cusIn.p_SimularOperacion = "N";
				    cusIn.p_ValorizacionAutomatica = "N";
				    cusIn.p_TicketAtencion = TicketAtencion;
					String CodigoIFRSDefecto	= ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa,"MESA", "CODIGOIFRS_DEPOSITO_AUTODESEMBOLSO");
				    
				    for (CrearOperacionDepositoDesembolso_Operacion_IN ope: operacionCerrar.p_Operacion){
				    	for(ObjMovimiento e: entLogCus){
				    		if (e.IdGenerarMovimientoCustodia.equals(ope.IdOperacion)){
								//CUSTODIA, puede ir a quedar operaciones pendientes
								//02 - CREAR U OBTENER MOVIMIENTO EN TRANSITO (determinar si va o no a moviemitno en tranisto)
								//GestionMovimientoCarteraEnTransito tt= new GestionMovimientoCarteraEnTransito();buscar info movto en transito
								//RetiroCustodia rr = new RetiroCustodia();
								ObjDatosEntrada obj = new ObjDatosEntrada();
								//Utils.copyPojo(ope, obj);
								Utils.copyPojo(e, obj);
								obj.IdGenerarMovimientoCustodia = ope.IdOperacion;	//ID Interno del proceso
								obj.TablaAfectada = "CARTERA"; 					//CARTERA - SUBYACENTE - TRANSITO - GARANTIA
								obj.EsRegistroNuevo = "S";				//S o N
								/////////////////////////// OPERACION
								obj.AccionOperacion = "INGRESO";				//INGRESO - CERTIFICACION		
								obj.IdCliente = ope.IdClienteCorredor; 
								obj.IdPortafolio = ope.IdPortafolioCorredor; 
								obj.FechaOperacion = Fecha.toDate(ope.FechaOperacion);
								obj.FechaVencimientoOperacion = Fecha.toDate(ope.FechaVencimiento);
								obj.PlazoEnDiasOperacion = ope.Plazo;
								obj.IdTipoOperacion = ope.IdTipoOperacion;		
								obj.IdGrupoOperacion = ope.IdOperacion; 
								obj.IdOperacion = ope.IdOperacion;
								obj.IdConcepto = ope.Concepto.IdConcepto;
								obj.IdCondicionLiquidacion = ope.IdCondicionLiquidacion; 
								obj.FechaLiquidacion = Fecha.toDate(ope.FechaLiquidacion);		
								obj.IdTipoProducto = ope.IdTipoProducto; 
								obj.IdTipoCanalIngreso = ope.IdTipoCanalIngreso; 		
								obj.IdTipoComprobante = "MES"; 
								obj.FolioComprobante = ope.IdOperacion; 
								obj.FechaComprobante = Fecha.toDate(ope.FechaOperacion); 
								obj.IdMesa = ope.IdMesaOrigen;
								//public String				IdBolsa;
								obj.IdUsuario = in.p_IdUsuario;
								obj.IdSucursal = in.p_IdSucursal; 
								obj.IdAgente = ope.IdAgente; 
								obj.Glosa = ope.Glosa;
								obj.PermiteCertificacionPorUsuario = "S";		//Mov en transito
								obj.CodigoIFRS = CodigoIFRSDefecto;
								/////////////////////////// INSTRUMENTO					
								//obj.IdInstrumento = ;
								//obj.IdInstrumentoAnterior;
								//obj.IdMonedaInstrumento;
								obj.IdTipoReajusteInstrumento = ope.IdTipoReajuste;
								obj.IdEmisor = ope.IdEmisor;
								//obj.EsFungible;
								//obj.IdBoveda;		
								//obj.CuentaBoveda;			//????
								obj.EsNominal = "S";
								//TESORERIA
								//CUENTA INVERSION
								cusIn.p_DatosEntrada.add(obj);
				    		}
				    	}
					}
					IngresoCustodia cus = new IngresoCustodia();
					log.write("MRF.Llamada Ingreso Custodia");
					cusOut = cus.logica(conn, cusIn);
					CacheValorizacion.valorizar(conn);
					out.P_OUT_MENSAJES.addAll(cusOut.P_OUT_MENSAJES);
					if (MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES) > 0){
						return out;
					}
					for (ObjDatosSalida c: cusOut.P_OUT_DATOSSALIDA){
						mapMsg.put("IdOperacion",c.IdGenerarMovimientoCustodia.toString());
						out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_OK,"INGRESO_CUSTODIA_OK",mapMsg,conn,in.p_IdEmpresa));
						Id++;
					}
				}
				//IngresoCustodiaSalida
				//ingreso Operacion, actualizando el folio si es que hay ioeracuib cerrar falta hacer el UPDATE
				HashMap<String, Object> cusOutHash =  Utils.List2Hash(cusOut.P_OUT_DATOSSALIDA, "IdGenerarMovimientoCustodia");
				
				List<TomarDepositoDesembolsoUpdate> upd = new ArrayList<>();
				for (CrearOperacionDepositoDesembolso_Operacion_IN ope: operacionCerrar.p_Operacion){
					TomarDepositoDesembolsoUpdate updDat= new TomarDepositoDesembolsoUpdate();
					updDat.IdOperacion = ope.IdOperacion;
					updDat.FolioCartera = ((ObjDatosSalida)cusOutHash.get(ope.IdOperacion)).FolioCustodia;
					ope.FolioCartera = ((ObjDatosSalida)cusOutHash.get(ope.IdOperacion)).FolioCustodia;
					upd.add(updDat);
				}
				log.write("MRF.Actualizar Folio Cartera");
				_sql.batch(conn, SQL_UPDATE_OPERACIONES, upd, TomarDepositoDesembolsoUpdate.class);
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
					entTes.p_FechaMovimiento = in.p_FechaSistema;   // Fecha de Sistema(si no viene le asigna fecha sistema)  //////// nuevo jv
					
					for (CrearOperacionDepositoDesembolso_Operacion_IN ope: operacionCerrar.p_Operacion){
						IngresoEgreso_Entrada IEGen = new IngresoEgreso_Entrada();
						FormaDePago_Entrada FPagTes = new FormaDePago_Entrada();
						for (CrearOperacionDepositoDesembolso_FormaPagoOperacion_IN fPag :operacionCerrar.p_FormasDePago){
							if (fPag.AfectarTesoreria.equals("S")){
								IEGen.NumeroReferencia 				= fPag.IdOperacion;  				//	BIGINT 
								IEGen.IdTipoMovimiento 				= fPag.IdTipoMovimientoTes;  		 		//	NVARCHAR(6)    -- MOVING, MOVEGR
								IEGen.IdTipoPersona 				= "CLI";  		 		//	NVARCHAR(3)    -- Es el IdTipoSocioDeNegocio (CLI / EMP / PRO / OPE)
								IEGen.IdPersona						= ope.Persona.IdPersona;  		 			//	BIGINT
								IEGen.IdPortafolio 					= ope.IdPortafolio;  		 			//	BIGINT 
								IEGen.IdMoneda 						= fPag.IdMoneda;//Liquidacion;  		 				//	NVARCHAR(3)
								IEGen.Monto 						= fPag.MontoFormaDePago;  		 				//	DECIMAL(26,8)
								IEGen.FechaLiquidacion 				= fPag.FechaLiquidacionFormaDePago;  		 		//	NVARCHAR(10)                                                                      
								IEGen.IdConcepto 					= ope.Concepto.IdConcepto;  		 			//	NVARCHAR(6)
								IEGen.IdTipoPersonaTitularPago 		= "CLI";  		//	NVARCHAR(3)    -- Es el IdTipoSocioDeNegocio (CLI / EMP / PRO / OPE)
								IEGen.IdPersonaTitularPago 			= ope.Persona.IdPersona;  		 	//	BIGINT
								IEGen.IdPortafolioTitularPago 		= ope.IdPortafolio;  		//	BIGINT
								IEGen.IdCaja 						= 0L;  		 				//	BIGINT
								IEGen.SecuenciaAgrupacion 			= ""; 		//	NVARCHAR(40)  -- Movimientos con una misma secuencia se agrupan en un mismo comprobante(también influyen parámetros de agrupación)
								IEGen.SecuenciaRelacionada 			= 0L;	 	//	BIGINT		  -- Permite relacionar distintos mov y generar una orden de liquidación entre ellos
								IEGen.Glosa 						= ope.Concepto.NombreConcepto;  		 				//	NVARCHAR(260)														   	
							   	//Parámetros Referenciales(Opcionales)
								IEGen.IdEmpleadoDigitador			= operacionCerrar.p_IdUsuario;  		 	//	BIGINT
								IEGen.IdSucursalEmpleadoDigitador	= operacionCerrar.p_IdSucursal;  	//	BIGINT
								IEGen.IdTipoProducto				= ope.IdTipoProducto;  		 		//	NVARCHAR(4)
								IEGen.IdTipoOperacion				= ope.IdTipoOperacion;  		 		//	NVARCHAR(6)									    
								IEGen.IdTipoComprobante				= "MES";  		 	//	NVARCHAR(3)
								IEGen.IdComprobante					= ope.FolioOperacion;  		 		//	BIGINT
								IEGen.FechaComprobante				= Fecha.toFormat(Fecha.toDate(ope.FechaOperacion),"YYYY-MM-dd hh:mm:ss");  		 		//	NVARCHAR(40)
								IEGen.FechaLiquidacionComprobante	= ope.FechaLiquidacion;  	//	NVARCHAR(40)
								
								IEGen.IdOperacion					= fPag.IdOperacion;  		 			//	BIGINT			
								IEGen.FechaOperacion				= Fecha.toFormat(Fecha.toDate(ope.FechaOperacion),"YYYY-MM-dd hh:mm:ss");  		 		//	NVARCHAR(40)
								IEGen.IdCondicionLiquidacion		= ope.IdCondicionLiquidacion;  		//	NVARCHAR(2)
								IEGen.IdBolsa						= "";  		 				//	NVARCHAR(3)
								IEGen.IdMonedaOperacion				= ope.IdMonedaLiquidacion;  		 	//	NVARCHAR(3)
								IEGen.MontoOperacion				= fPag.MontoFormaDePago;//LiquidacionFormaDePago;   				//	DECIMAL(26,8)
								IEGen.IdTipoReajuste				= ope.IdTipoReajuste;  		 		//	NVARCHAR(3)
								IEGen.FechaParidad					= "0001-01-01";  		 			//	NVARCHAR(40)
								IEGen.ValorParidad					= BigDecimal.ZERO;   				// 	DECIMAL(26,8)
								IEGen.FolioCartera					= ope.FolioCartera;  		 			//	BIGINT
								IEGen.IdInstrumento					= 0L;  		 		//	BIGINT
								IEGen.IdTipoInstrumento				= "";  		 	//	NVARCHAR(3)
								IEGen.IdClasificacionTipoInstrumento= ""; //	NVARCHAR(3)
							    //Indicadores
								IEGen.Bilateral 					= "N";		        //	NVARCHAR(1)  -- Liquidación bilateral entre corredores, no pasa por el CCLV
								IEGen.GeneraComprobante 			= "S"; 		//	NVARCHAR(1)  -- S/N: genera comprobante automático
								IEGen.AgruparEnComprobante 			= "N"; 	//	NVARCHAR(1)  -- S/N: permite agrupar varios mov. en un comprobante
								IEGen.MueveCuentaInversion			= "N";  			//	NVARCHAR(1)  -- S/N: mueve cuenta inversion // *** No se usa el campo, se debe borrar ? ***
								IEGen.TitularPago					= "S";  		 			//	NVARCHAR(1)  -- S/N: Socio de negocio que genera el movimiento es quién cobra o paga
								entTes.p_IngresoEgresoGenerar.add(IEGen);
								
								FPagTes.NumeroReferencia			= fPag.IdOperacion;				//  BIGINT        Id Referencial (para unir el movimiento con su forma de pago)
								FPagTes.NumeroReferenciaFormaDePago	= fPag.IdComprobantePagoOperacion;	//  BIGINT        Id Referencia para FP (1,2,3,..) debe ser único por FP aunque sean de distintos movimientos
								FPagTes.IdMedioDePago				= fPag.FormaDePago.IdMedioDePago;          		//	NVARCHAR(3)   Ej.: CHE, EFE, TRA, VVI, CIN
								FPagTes.IdModalidadDePago			= fPag.IdModalidadDePago;      		//	NVARCHAR(6)   Ej.: MODCAJ: Por Caja   MODDEP: Depósito
								FPagTes.IdMoneda					= fPag.IdMoneda;//Liquidacion;               		//	NVARCHAR(3)
								FPagTes.Monto						= fPag.MontoFormaDePago;//LiquidacionFormaDePago; 							//	DECIMAL(26,8)
								FPagTes.IdCuentaBancariaCorredora	= 0L; 		//	BIGINT
								FPagTes.IdTarjetaBancariaCorredora	= 0L; 	//	BIGINT
								FPagTes.IdChequera					= 0L; 					//	BIGINT
								FPagTes.NumeroDocumentoCorredora	= 0L; 		//	BIGINT
								FPagTes.FechaDocumentoPagoCorredora	= "0001-01-01 00:00:00";	//	NVARCHAR(40)
								if (fPag.PreferenciaDePago != null){
									FPagTes.IdCuentaBancariaSocio		= fPag.PreferenciaDePago.IdCuentaBancaria; 			//	BIGINT
									FPagTes.IdBancoSocio				= fPag.PreferenciaDePago.IdBancoCuentaBancaria; 					//	BIGINT
									FPagTes.NombreCuentaBancariaSocio	= fPag.PreferenciaDePago.NombreBancoCuentaBancaria;		//	NVARCHAR(100)
									FPagTes.NumeroCuentaBancariaSocio	= fPag.PreferenciaDePago.NumeroCuentaBancaria;		//	NVARCHAR(100)
									FPagTes.IdTipoCuentaBancariaSocio	= fPag.PreferenciaDePago.IdTipoCuentaBancaria;		//	NVARCHAR(4)
									FPagTes.NombreTipoCuentaBancariaSocio = fPag.PreferenciaDePago.NombreTipoCuentaBancaria; 	//  NVARCHAR(100)
									FPagTes.IdTarjetaBancariaSocio		= fPag.PreferenciaDePago.IdTarjetaBancaria;			//	BIGINT
									FPagTes.IdTipoTarjetaBancariaSocio	= fPag.PreferenciaDePago.IdTipoTarjetaBancaria;  	//	NVARCHAR(40)
									FPagTes.NombreTipoTarjetaBancariaSocio = fPag.PreferenciaDePago.NombreBancoTarjetaBancaria; //	NVARCHAR(100)
									FPagTes.NumeroTarjetaBancariaSocio	= fPag.PreferenciaDePago.NumeroTarjetaBancaria;  	//	NVARCHAR(100)
								}
								else {
									FPagTes.IdCuentaBancariaSocio		= 0L; 			//	BIGINT
									FPagTes.IdBancoSocio				= 0L; 					//	BIGINT
									FPagTes.NombreCuentaBancariaSocio	= "";		//	NVARCHAR(100)
									FPagTes.NumeroCuentaBancariaSocio	= "";		//	NVARCHAR(100)
									FPagTes.IdTipoCuentaBancariaSocio	= "";		//	NVARCHAR(4)
									FPagTes.NombreTipoCuentaBancariaSocio = ""; 	//  NVARCHAR(100)
									FPagTes.IdTarjetaBancariaSocio		= 0L;			//	BIGINT
									FPagTes.IdTipoTarjetaBancariaSocio	= "";  	//	NVARCHAR(40)
									FPagTes.NombreTipoTarjetaBancariaSocio = ""; //	NVARCHAR(100)
									FPagTes.NumeroTarjetaBancariaSocio	= "";
								}
								FPagTes.NumeroSerieSocio			= "";  				//	NVARCHAR(40)
								FPagTes.NumeroDocumentoSocio		= 0L;			//	BIGINT
								FPagTes.FechaDocumentoPagoSocio		= Fecha.toFormat(Fecha.toDate(fPag.FechaLiquidacionFormaDePago),"YYYY-MM-dd hh:mm:ss");  		//	NVARCHAR(40)
								entTes.p_FormaDePago.add(FPagTes);
							}
						}
						
					}
					log.write("MRF.Llamada Ingreso Tesoreria");
					if (entTes.p_IngresoEgresoGenerar.size() > 0){
						tesOut = tes.logica(conn, entTes);
						out.P_OUT_MENSAJES.addAll(tesOut.P_OUT_MENSAJES);
					
						if (MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES) > 0){
							return out;
						}
					}
				}
				{
					//genero CARGO ABONO cta inversion
					CrearCargoAbonoEntrada entCta = new CrearCargoAbonoEntrada();
					entCta.p_IdEmpresa = in.p_IdEmpresa;
					entCta.p_IdUsuario = in.p_IdUsuario;
					entCta.p_IdModulo = in.p_IdModulo;
					entCta.p_simularOperacion = "N";
					entCta.p_UsaReglasNegocio = in.p_UsaReglasNegocio;
					entCta.p_FechaSistema = in.p_FechaSistema;
					entCta.p_EsIngresoManual	= "N";
					for (CrearOperacionDepositoDesembolso_Operacion_IN ope: operacionCerrar.p_Operacion){
						for (CrearOperacionDepositoDesembolso_FormaPagoOperacion_IN fPag :operacionCerrar.p_FormasDePago){
							if (ope.IdOperacion.equals(fPag.IdOperacion)){
								Long IdMovtoTeso = 0L;
								for (IngresoEgresoGenerado_Salida tesOutGen : tesOut.P_OUT_INGRESOEGRESOGENERADO){
									if (fPag.IdOperacion.equals(tesOutGen.NumeroReferencia)){
										IdMovtoTeso = tesOutGen.IdIngresoEgreso;
									}
								}
								ObjCargoAbonoAGenerar objCtaInCli = new ObjCargoAbonoAGenerar();
								ObjCargoAbonoAGenerar objCtaInCorr = new ObjCargoAbonoAGenerar();
								objCtaInCli.NumeroCargoAbono 				= fPag.IdOperacion + fPag.IdInternoPagoOperacion;  //NumeroCargoAbono Para Cliente
								objCtaInCli.IdPortafolio 					= ope.IdPortafolio; 
								objCtaInCli.IdMoneda 						= fPag.IdMoneda;//Liquidacion; 
								objCtaInCli.IdTipoMovimiento 				= fPag.IdTipoMovimientoCtaCli; 
								objCtaInCli.IdConcepto						= ope.Concepto.IdConcepto; //validar si hay intermesa para el concepto relacionado
								objCtaInCli.Descripcion						= ope.Concepto.NombreConcepto; 
								objCtaInCli.IdCondicionDeLiquidacion		= ope.IdCondicionLiquidacion; 
								objCtaInCli.FechaCondicionDeLiquidacion		= fPag.FechaLiquidacionFormaDePago; 
								objCtaInCli.Monto							= fPag.MontoFormaDePago; 
								objCtaInCli.IdEjecutivo						= operacionCerrar.p_IdUsuario; 
								objCtaInCli.IdSucursal						= operacionCerrar.p_IdSucursal; 
								objCtaInCli.IdTipoProducto					= ope.IdTipoProducto; 
								objCtaInCli.IdTipoOperacion					= ope.IdTipoOperacion; 
								objCtaInCli.IdOperacion						= fPag.IdOperacion; 
								objCtaInCli.FechaOperacion					= ope.FechaOperacion; 
								objCtaInCli.FechaLiquidacionOperacion		= ope.FechaLiquidacion; 
								objCtaInCli.FechaVencimientoOperacion		= ope.FechaVencimiento; 
								objCtaInCli.IdMonedaOperacion				= fPag.IdMoneda;//Liquidacion; 
								objCtaInCli.MontoOperacion					= fPag.MontoFormaDePago; 
								objCtaInCli.TipoCambioOperacion				= BigDecimal.ZERO; 
								objCtaInCli.IdContraparte					= 0L; 
								objCtaInCli.IdTipoComprobante				= "MES"; 
								objCtaInCli.IdComprobante					= fPag.IdOperacion; 
								objCtaInCli.FolioComprobante				= ope.FolioOperacion.toString(); 
								objCtaInCli.IdComprobanteDetalle			= 0L; 
								objCtaInCli.IdComprobanteFormaDePago		= 0L; 
								objCtaInCli.IdTipoMovimientoAsociado		= fPag.IdTipoMovimientoTes; 
								objCtaInCli.IdMovimientoAsociado			= IdMovtoTeso; 
								objCtaInCli.FechaComprobante				= ope.FechaOperacion; 
								objCtaInCli.IdPersonaPagoCobro				= ope.Persona.IdPersona; 
								objCtaInCli.IdInstrumento					= 0L; 
								objCtaInCli.IdTipoInstrumento				= ""; 
								objCtaInCli.Nemo							= ""; 
								objCtaInCli.EsIndicadorBilateral			= "N"; 
								objCtaInCli.EsCargoAbonoPorTraspaso			= "N"; 
								objCtaInCli.IdPortafolioTraspaso			= 0L; 
								objCtaInCli.GeneroContraparteEnTesoreria	= "S"; 
								objCtaInCli.CertificadoAutomatico			= "S";
								Utils.copyPojo(objCtaInCli,objCtaInCorr);
								objCtaInCorr.NumeroCargoAbono = -1 * (fPag.IdOperacion + fPag.IdInternoPagoOperacion);   //NumeroCargoAbono Para Corredor
								objCtaInCorr.IdPortafolio					= ope.IdPortafolioCorredor;
								objCtaInCorr.IdTipoMovimiento				= fPag.IdTipoMovimientoCtaCorr; 
								entCta.p_CargoAbonoAGenerar.add(objCtaInCli);
								entCta.p_CargoAbonoAGenerar.add(objCtaInCorr);
							}
						}
					}
					CrearCargoAbono cta = new CrearCargoAbono();
					log.write("MRF.Llamada Ingreso Cuenta Inversión");
					ctaOut = cta.logica(conn, entCta);
					out.P_OUT_MENSAJES.addAll(ctaOut.P_OUT_MENSAJES);
					if (MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES) > 0){
						return out;
					}
				}
				//Ingreso las formas de pago con la actualizacion de los id ade cta inv y teso
				//Ingreso Forma de pago Operacion Mesa
				{				
					CrearComprobantePagoOperacion callCompPag = new CrearComprobantePagoOperacion();
					CrearComprobantePagoOperacion_IN compPag = new CrearComprobantePagoOperacion_IN();
					compPag.p_IdEmpresa = in.p_IdEmpresa;
					compPag.p_IdUsuario = in.p_IdUsuario;
					compPag.p_IdModulo = in.p_IdModulo;
					compPag.p_UsaReglasNegocio = in.p_UsaReglasNegocio;
					
					HashMap<String, Object> tesOutMap = Utils.List2Hash(tesOut.P_OUT_INGRESOEGRESOGENERADO, "NumeroReferencia");				
					for (CrearOperacionDepositoDesembolso_Operacion_IN ope: in.p_Operacion){
						for (CrearOperacionDepositoDesembolso_FormaPagoOperacion_IN inComp : in.p_FormasDePago){
							if (ope.IdInternoOperacion.equals(inComp.IdInternoOperacion)){
								ComprobantePagoOperacionObj_IN data = new ComprobantePagoOperacionObj_IN();
								data.IdEmpresa					= in.p_IdEmpresa;
								data.IdInternoPagoOperacion 	= inComp.IdInternoPagoOperacion;
								data.IdComprobantePagoOperacion = inComp.IdComprobantePagoOperacion;
								data.IdOperacion 				= inComp.IdOperacion;
								data.IdIngresoEgreso			= tesOutMap.size() > 0?((IngresoEgresoGenerado_Salida) tesOutMap.get(inComp.IdOperacion)).IdIngresoEgreso:0L;
									data.IdAbono = 0L;
									data.IdCargo = 0L;
								for (ObjCargoAbonoGenerado ctaOutCAAB: ctaOut.P_OUT_CARGOABONOGENERADO){
									Long numeroCargoAbono = inComp.IdOperacion + inComp.IdInternoPagoOperacion;
									
									if ((numeroCargoAbono.compareTo(ctaOutCAAB.NumeroCargoAbono) == 0 || numeroCargoAbono.compareTo(-1*ctaOutCAAB.NumeroCargoAbono) == 0) && ctaOutCAAB.IdTipoMovimiento.equals("MOVABN") ){
										data.IdAbono			= ctaOutCAAB.IdCargoAbonoMovimiento;
										
									}							
									if ((numeroCargoAbono.compareTo(ctaOutCAAB.NumeroCargoAbono) == 0 || numeroCargoAbono.compareTo(-1*ctaOutCAAB.NumeroCargoAbono) == 0) && ctaOutCAAB.IdTipoMovimiento.equals("MOVCAR")){	
										data.IdCargo			= ctaOutCAAB.IdCargoAbonoMovimiento;
										
									}
								}
								data.IdIngresoEgresoAnticipo	= 0L;
								data.IdAbonoAnticipo			= 0L;
								data.IdCargoAnticipo			= 0L;
								data.IdTipoProducto				= ope.IdTipoProducto;
								data.RecibePagaPortafolio 		= inComp.RecibePagaPortafolio;
								data.IdFormaDePago 				= inComp.IdFormaDePago;
								data.IdModalidadPago			= inComp.IdModalidadDePago;
								data.IdMoneda 					= inComp.IdMoneda;
								//data.IdMonedaLiquidacion 		= inComp.IdMonedaLiquidacion;
								data.Monto 						= inComp.MontoFormaDePago;
								//data.MontoLiquidacion			= inComp.MontoLiquidacionFormaDePago;
								data.FechaIngresoComprobantePagoOperacion		= ope.FechaOperacion;//" 		DATE
								data.FechaLiquidacionComprobantePagoOperacion	= inComp.FechaLiquidacionFormaDePago;//"	DATE
								data.IdEstado									= inComp.IdEstado;//" 									NVARCHAR(3)
								data.AfectarTesoreria 							= inComp.AfectarTesoreria;
								
								compPag.p_ComprobantePagoOperacion.add(data);
							}
						}
					}
					log.write("MRF.Llamada Ingreso Comprobante Pago Operación");
					resultCompPag = callCompPag.logica(conn, compPag);
					out.P_OUT_MENSAJES.addAll(resultCompPag.P_OUT_MENSAJES);
					if (MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES) > 0){
						return out;
					}
					v_OutComprobantePagoHash = Utils.List2Hash(resultCompPag.P_OUT_COMPRABANTEPAGO,"IdInternoPagoOperacion");
					for (CrearOperacionDepositoDesembolso_FormaPagoOperacion_IN inComp : in.p_FormasDePago){
						inComp.IdComprobantePagoOperacion = ((ComprobantePagoOperacionObj_OUT)v_OutComprobantePagoHash.get(inComp.IdInternoOperacion)).IdComprobantePagoOperacion;
					}
				}
			}
			

			//tabla salida
			for (OperacionObjIn ope: v_Operacion){
				CrearOperacionMesa_Operacion_OUT opSal= new CrearOperacionMesa_Operacion_OUT();
				Utils.copyPojo(ope, opSal);
				opSal.Accion = ope.Accion;
				opSal.IdInternoOperacion = ope.IdInternoOperacion;
				opSal.IdOperacion = ope.IdOperacion;
				opSal.FolioOperacion = ope.FolioOperacion;
				opSal.IdCliente = ope.IdCliente;
				opSal.IdPortafolio = ope.IdPortafolio;
				opSal.IdClienteCorredor = ope.IdClienteCorredor;
				opSal.IdPortafolioCorredor = ope.IdPortafolioCorredor;
				opSal.IdOperacionRelacionada = ope.IdOperacionRelacionada;
				opSal.Intermesa = ope.Intermesa;
				opSal.IdMesaOrigen = ope.IdMesaOrigen;
				opSal.IdMesaDestino = ope.IdMesaDestino;
				opSal.AfectaCustodia = ope.AfectaCustodia;
				opSal.IdTipoProducto = ope.IdTipoProducto; //'MERF'
				opSal.IdTipoOperacion = ope.IdTipoOperacion; //COMTER,VENTER,COMPAC,VENPAC
				opSal.IdTipoReajuste = ope.IdTipoReajuste; //"USD","UF"
				opSal.Plazo = ope.PlazoEnDias;
				opSal.Monto = ope.MontoOperacion;
				opSal.MontoMoneda = ope.MontoOperacionMoneda;
				opSal.IdMoneda = ope.IdMoneda;//CLP "USD","UF" moneda local
				opSal.IdMonedaLiquidacion = ope.IdMonedaLiquidacion; //CLP "USD","UF"
				opSal.Tasa = ope.Tasa;
				opSal.ValorFinal = ope.MontoOperacionFinal;
				opSal.ValorFinalMoneda = ope.MontoOperacionFinalMoneda;
				opSal.TasaAgente = ope.TasaAgente;
				opSal.FechaOperacion =  Fecha.toDate(ope.FechaOperacion);
				opSal.FechaVencimiento = ope.FechaVencimiento;
				opSal.IdCondicionLiquidacion = ope.IdCondicionLiquidacion;
				opSal.FechaLiquidacion = ope.FechaCondicionLiquidacion;
				opSal.IdAgente = ope.IdAgente;
				opSal.IdOperador = ope.UsuarioEdicion;
				opSal.IdEstado = ope.IdEstado;
				opSal.NombreEstado = ((OperacionObjOut)v_OutOperacionHash.get(ope.IdInternoOperacion)).NombreEstado;
				opSal.Glosa = ope.Glosa;
				opSal.IdTipoCanalIngreso = ope.IdTipoCanalIngreso;
				out.P_OUT_OPERACION.add(opSal);
			}
			for (CrearOperacionDepositoDesembolso_FormaPagoOperacion_IN ope: in.p_FormasDePago){
				CrearOperacionMesa_FormaPagoOperacion_OUT opSal= new CrearOperacionMesa_FormaPagoOperacion_OUT();
				Utils.copyPojo(ope, opSal);
				out.P_OUT_FORMASDEPAGO.add(opSal);
			}
			
		}catch (Exception e){
			e.printStackTrace();
		}
		log.write("FIN::MRF.TomarDepositoDesembolso");
		return out;
	}
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		Gson gson = Utils.getGsonBuilder();
		TomarDepositoDesembolso_IN jin = gson.fromJson(in.toString(), TomarDepositoDesembolso_IN.class);
		TomarDepositoDesembolso_OUT out = logica(conn, jin);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	};

}
