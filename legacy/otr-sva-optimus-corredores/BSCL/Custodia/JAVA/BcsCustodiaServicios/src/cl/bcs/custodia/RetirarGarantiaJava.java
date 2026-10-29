package cl.bcs.custodia;

import java.math.BigDecimal;
import java.sql.Connection;
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
import cl.bcs.custodia.CertificarMovimientosEnTransito.CertificarMovimientosEnTransitoSalida;
import cl.bcs.custodia.CertificarMovimientosEnTransito.ObjMovimientos;
import cl.bcs.custodia.utils.GestionMovimientoCarteraEnTransito;
import cl.bcs.custodia.utils.IngresoCustodia;
import cl.bcs.custodia.utils.LogCustodia;
import cl.bcs.custodia.utils.RetiroCustodia;
import cl.bcs.custodia.utils.RetiroCustodia.ObjDatosEntrada;
import cl.bcs.facturacion.ProcesarFacturacionCustodia.ClienteNeteo;
import cl.bcs.facturacion.ProcesarFacturacionCustodia.EntradaNeteo;
import cl.bcs.plataforma.bat.AgregarPrograma;
import cl.bcs.plataforma.bat.AgregarPrograma.InAgregarPrograma.Parametro;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class RetirarGarantiaJava implements JavaCall{		
	private final Log log = new Log("RetirarGarantia");
	private static final Logger logger = LogManager.getLogger(RetirarGarantiaJava.class);
	private final String SQL_Garantias = loadResourceToString("cl/bcs/custodia/sql/RetirarGarantia_SEL_Garantia.sql");
	private final String SQL_GarantiasTransito = loadResourceToString("cl/bcs/custodia/sql/RetirarGarantia_SEL_GarantiasDesdeTransito.sql");	
	private final String SQL_GarantiasTransitoIFRF = loadResourceToString("cl/bcs/custodia/sql/RetirarGarantia_Select_CertificacionTransitos.sql");
	
	
	
	// ---------------------------------------------
	// CLASES DE ENTRADA
	// ---------------------------------------------
	
	public static class ObjGarantiasARetirar {
		public Long			IdGarantia;
		public BigDecimal	Cantidad;
		public BigDecimal	ValorRescate;
		public BigDecimal	MontoMonedaInstrumento;
		public BigDecimal	PrecioRetiro;
		public BigDecimal	TasaRetiro;
		public String 		IdConcepto;
		public String 		ConCertificadoAutomatico;
		public String       IdClasificacionTipoInstrumento;
		public String		EsProcesoContraparteGarantia;
		public String toString() {
			return Utils.printJson(this, false);
		}
		
		public ObjGarantiasARetirar() {
			this.EsProcesoContraparteGarantia = "N";
		}
	};
	public static class IdGarantiaTransito extends BcsPojo {
		public Long IdGarantia;
	}

	
	public static class RetirarGarantiaEntrada {
	    public Long							p_IdEmpresa;
	    public Long							p_IdUsuario;
	    public String						p_IdModulo;
	    
	    public Long 						p_IdEmpleado;
	    public String						p_UsaReglasNegocio;
	    public String						p_SimulaOperacion;
	    public String						p_FechaMovimiento;
	    public String						p_IdTipoOperacion;
	    public Long							p_IdContraparteGarantia;
	    public List<ObjGarantiasARetirar>	p_tablaRetiro;
	    public List<Long>					p_MovimientosEnTransito;
	    
	    public RetirarGarantiaEntrada (){
	    	p_tablaRetiro 			= new ArrayList<ObjGarantiasARetirar>();
	    	p_MovimientosEnTransito	= new ArrayList<Long>();
		}
	   
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	
	// ---------------------------------------------
	// CLASES DE SALIDA
	// ---------------------------------------------

	public static class RetirarGarantiaSalida {				
		public Long							P_OUT_ERRORESCONTADOR;
		public Long							P_OUT_TICKETATENCION;
		public List<ObjResultado>			P_OUT_RESULTADOPROCESO;
		public List<MensajeSalida>			P_OUT_MENSAJES;
				
		public static class ObjResultado {
			public Long		IdGenerarMovimientoCustodia;
			public String	TablaOrigen;
			public Long		IdCustodia;
			public Long		FolioCustodia;
		};		
		public RetirarGarantiaSalida (){
			P_OUT_MENSAJES 			= new ArrayList<MensajeSalida>();
			P_OUT_RESULTADOPROCESO	= new ArrayList<ObjResultado>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	};

	// ---------------------------------------------
	// CLASES DE PROCESO
	// ---------------------------------------------
	
	public static class ObjGarantias {
		public Long				IdGarantia; 
		public Long				IdGenerarMovimientoCustodia;
		public Long				FolioGarantia;		
		public Long				FolioCarteraOrigen;
		public Long				IdCarteraOrigen;
		public String			IdTipoOperacion;
		public Long				IdContraparteGarantia;
		public String 			RazonSocialContraparteGarantia;
		public String			IdOrigenGarantia;
		public java.sql.Date	FechaOperacion;
		public java.sql.Date	FechaVencimientoOperacion;
		public java.sql.Date 	FechaVencimientoInstrumento;
		public Long				PlazoEnDiasOperacion;				
		public Long				IdCliente;						
		public Long				IdPortafolio;
		public String			IdTipoProducto;				
		public String			IdBolsa; 						
		public String 			IdCondicionLiquidacion; 		
		public String			IdClasificacionTipoInstrumento; 	
		public String			IdTipoInstrumento;				
		public Long				IdInstrumento; 					
		public String			Nemotecnico;						
		public String			IdEmisor;							
		public String			NemoSVS;	 						
		public String			IdTipoReajuste; 					
		public java.sql.Date	FechaEmisionInstrumento;			
		public String 			EsFungible;
		public String			IdMonedaInstrumento;
		public String			IdMonedaEmision;
		public String			IdBoveda;				
		public Long				IdInstrumentoCarteraGarantizada;
		public Long				IdEmpleadoEjecutivo;
		public Long				IdSucursalEjecutivo;
		public BigDecimal		Cantidad;
		public BigDecimal		ValorRescate;
		public BigDecimal		CantidadRetirada;
		public BigDecimal		ValorRescateRetirado;
		public BigDecimal		PrecioCompra;
		public BigDecimal		TasaCompra;
		public BigDecimal		PrecioCompraOrigen;
		public BigDecimal		TasaCompraOrigen;
		public BigDecimal		PrecioRetiro;
		public BigDecimal		TasaRetiro;	
		public BigDecimal 		MontoRetiroAMonedaInstrumento;
		public String 			IdConcepto;
		public String 			ConCertificadoAutomatico;   //Revisar al final
		public String 			IdEstado;
		public BigDecimal 		MontoGarantia = BigDecimal.ZERO;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	public static class GarantiasARetirar_Proceso {
		public Long				IdGarantia;
		public BigDecimal		Cantidad;
		public BigDecimal		ValorRescate;
		public BigDecimal		MontoMonedaInstrumento;
		public BigDecimal		PrecioRetiro;
		public BigDecimal		TasaRetiro;
		public String 			IdConcepto;
		public String 			ConCertificadoAutomatico;
		public String 			EsProcesoContraparteGarantia = "N";
		public String			IdBolsa;
		//Para validaciones
		public long 			errores 		= 0;
		public long 			advertencias 	= 0;
		public long 			autorizaciones 	= 0;
		public long 			ok 				= 0;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	};


	// ------------------------------------------------
	// METODOS COMPLEMENTARIOS
	// ------------------------------------------------

	//Procesa validaciones de negocio
	private void ProcesaRequisito(Connection con, Long idEmpresa, boolean esError, String codMsg, String CodRequisito, GarantiasARetirar_Proceso gar, List<MensajeSalida> P_OUT_MENSAJES)
			throws Exception {
		if (esError) 
		{
			String msg=Diccionario.getMensaje(con, idEmpresa, codMsg)
					.add("IdGarantia", gar.IdGarantia)
				    .msg();
			 
			if (CodRequisito.equals(MensajeSalida.MENSAJE_ERROR)) 
			{
				P_OUT_MENSAJES.add(new MensajeSalida(gar.IdGarantia, MensajeSalida.MENSAJE_ERROR,codMsg,msg));
				gar.errores++;
			}
			else if (CodRequisito.equals(MensajeSalida.MENSAJE_AUTORIZACION)) 
			{
				P_OUT_MENSAJES.add(new MensajeSalida(gar.IdGarantia, MensajeSalida.MENSAJE_AUTORIZACION,codMsg,msg));
				gar.autorizaciones++;						
			}
			else if (CodRequisito.equals(MensajeSalida.MENSAJE_ADVERTENCIA)) 
			{
				P_OUT_MENSAJES.add(new MensajeSalida(gar.IdGarantia, MensajeSalida.MENSAJE_ADVERTENCIA,codMsg,msg));
				gar.advertencias++;
			}
			else 
			{
				gar.ok++;
			}
		}
		else 
		{
			gar.ok++;
		}		
	}

	//Quita del proceso los registros invalidos
	public void limpiarGarantiasARetirar(List<GarantiasARetirar_Proceso> garantiasARetirar_Proceso, List<GarantiasARetirar_Proceso> garantiasARetirar_ConErrores) {
		for (GarantiasARetirar_Proceso garRetPro : garantiasARetirar_Proceso) {
			if (garRetPro.errores > 0) {
				garantiasARetirar_ConErrores.add(garRetPro);
			}
		}
		
		for (GarantiasARetirar_Proceso garRetErr : garantiasARetirar_ConErrores) {
			//Remueve el movimiento
			if (garantiasARetirar_Proceso.contains(garRetErr)) {
				garantiasARetirar_Proceso.remove(garRetErr);
			}
		}
	}	


	
	// ------------------------------------------------
	// LOGICA
	// ------------------------------------------------
	
	
	public RetirarGarantiaSalida logica(Connection con, RetirarGarantiaEntrada in) throws Exception {
		RetirarGarantiaSalida out = new RetirarGarantiaSalida();
		out.P_OUT_ERRORESCONTADOR = 0L;
		SQL sql = new SQL();
		
		log.write("INICIO RETIRO GARANTIAS");
		log.write("ENTRADA > " + in.toString());
		String retiraGarantiaIFRF = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "CUSTODIA", "CUS.RetiraGarantiaIFRFAlVencer");
		List<GarantiasARetirar_Proceso> garantiasARetirar_ConErrores = new ArrayList<GarantiasARetirar_Proceso>();
		List<GarantiasARetirar_Proceso> garantiasARetirar_Proceso = new ArrayList<GarantiasARetirar_Proceso>();
		HashMap<Long,GarantiasARetirar_Proceso> garantiasARetirar_ProcesoHash = new HashMap<Long,GarantiasARetirar_Proceso>();
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		
		//******************************************************************************************************************
		//												VALIDACIONES INICIALES
		//******************************************************************************************************************
		RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
		RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(con, in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo);
		if (MensajeSalida.getNumeroErrores(salidaBasicos.p_out_Mensajes) > 0) {
			out.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
		};
	
		if (out.P_OUT_MENSAJES.size() > 0){
			return out;
		};
		
		
		Long TicketAtencion = LogCustodia.logRetirarGarantia(con, in);
		if (TicketAtencion.equals(0L)){
			//TODO : incorporar mensajes de error
			out.P_OUT_ERRORESCONTADOR = 1L;				
			return out; 
		};
		
		//01 - RECUPERAR INFORMACION DE GARANTIA
		HashMap<String,Object> paramsGarantias = new HashMap<>();
		List<Long> IdGarantia = new ArrayList<Long>();
		
		
		for (ObjGarantiasARetirar garantiasARetirar: in.p_tablaRetiro) {
			IdGarantia.add(garantiasARetirar.IdGarantia);			
					
			GarantiasARetirar_Proceso objGarantiasARetirarProceso = new GarantiasARetirar_Proceso();
			Utils.copyPojo(garantiasARetirar, objGarantiasARetirarProceso);
			garantiasARetirar_Proceso.add(objGarantiasARetirarProceso);
			garantiasARetirar_ProcesoHash.put(objGarantiasARetirarProceso.IdGarantia,objGarantiasARetirarProceso);
		};
		
		List<Long> garantiaRetirarCerAuto = new ArrayList<Long>();
		HashMap<Long,Object> InformacionGarantia = new HashMap<Long,Object>();
		
		//02 - CREAR U OBTENER MOVIMIENTO EN TRANSITO
		List<Long> IdMovimientosEnTransito = new ArrayList<Long>();	
		HashMap<Long,Long> foliosGarantia = new HashMap<Long,Long>();
		if (in.p_MovimientosEnTransito.size() == 0) {
			
			List<Long> listaTransitosCertificarIFRF = this.filtraMovimientoTransitoGarantiaIFRF(in.p_tablaRetiro);				
			if(!listaTransitosCertificarIFRF.isEmpty()) {					
				//certificar movimiento en transito para garantia					
				CertificarMovimientosEnTransito.CertificarMovimientosEnTransitoSalida salidaCerMov = this.existeCertificarMovimientoTransitoGarantiaIFRF(con,in,listaTransitosCertificarIFRF,fechaSistema);
				//preparar salida.		
				out.P_OUT_MENSAJES.addAll(salidaCerMov.P_OUT_MENSAJES);				
				this.validarGarantiasInstrumentosVencidos(salidaCerMov,garantiasARetirar_ProcesoHash,IdGarantia,garantiasARetirar_Proceso);
			}
					
			paramsGarantias.put("IdGarantia", IdGarantia);
			List<ObjGarantias> CMC = sql.query(con, SQL_Garantias, paramsGarantias, ObjGarantias.class);	
			InformacionGarantia = Utils.List2Hash(CMC, "IdGarantia");
			
			for (GarantiasARetirar_Proceso garRet: garantiasARetirar_Proceso)
			{
				ObjGarantias InfoGarantia = (ObjGarantias) InformacionGarantia.get(garRet.IdGarantia);				
				ProcesaRequisito(con, in.p_IdEmpresa, ((garRet.Cantidad.compareTo(InfoGarantia.Cantidad) == 1 && !InfoGarantia.IdClasificacionTipoInstrumento.equalsIgnoreCase("IF")) || (garRet.Cantidad.compareTo(InfoGarantia.ValorRescate) == 1 && InfoGarantia.IdClasificacionTipoInstrumento.equalsIgnoreCase("'IF"))),"CANTIDAD_RETIRO_EXCEDE_DIPONIBLE_GARANTIA", MensajeSalida.MENSAJE_ERROR, garRet, out.P_OUT_MENSAJES);				
			}
			
			limpiarGarantiasARetirar(garantiasARetirar_Proceso, garantiasARetirar_ConErrores);
			
			if (garantiasARetirar_Proceso.size() == 0)
			{
				log.write("GARANTIAS INVALIDAS > " + garantiasARetirar_ConErrores.toString());
				out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
				return out;
			}
			
			
			List<IngresoCustodia.ObjDatosEntrada> p_Datos = new ArrayList<IngresoCustodia.ObjDatosEntrada>();
			for (GarantiasARetirar_Proceso garantiasARetirar: garantiasARetirar_Proceso)
			{
				IngresoCustodia.ObjDatosEntrada	aux = new IngresoCustodia.ObjDatosEntrada();
				ObjGarantias InfoGarantia = (ObjGarantias) InformacionGarantia.get(garantiasARetirar.IdGarantia);
				if (InfoGarantia == null) {					
					out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
					continue;
				};
				
				if (!InfoGarantia.IdEstado.equalsIgnoreCase("VIG")){
					continue;
				};
				
				if (InfoGarantia.ConCertificadoAutomatico.equalsIgnoreCase("S") || InfoGarantia.IdOrigenGarantia.equalsIgnoreCase("CTA")){
					garantiaRetirarCerAuto.add(InfoGarantia.IdGenerarMovimientoCustodia);
				}
				
				
				aux.IdGenerarMovimientoCustodia		= InfoGarantia.IdGenerarMovimientoCustodia;
				aux.TablaAfectada					= "TRANSITO";								
				aux.AccionOperacion					= "RETIRO";
				aux.EsRegistroNuevo					= "N";
				aux.IdConcepto						= "N".equals(garantiasARetirar.EsProcesoContraparteGarantia) ? InfoGarantia.IdConcepto : garantiasARetirar.IdConcepto;
				aux.IdCliente						= InfoGarantia.IdCliente;
				aux.IdPortafolio					= InfoGarantia.IdPortafolio;
				aux.FechaOperacion					= Fecha.toDate(in.p_FechaMovimiento);
				aux.IdInstrumento					= InfoGarantia.IdInstrumento;
				aux.IdCustodia						= garantiasARetirar.IdGarantia;
				aux.FechaVencimientoInstrumento 	= InfoGarantia.FechaVencimientoInstrumento;
				aux.FolioCarteraOrigen				= InfoGarantia.FolioGarantia;
				aux.TasaCompra						= garantiasARetirar.TasaRetiro;
				aux.PrecioCompra					= garantiasARetirar.PrecioRetiro;
				aux.IdMonedaInstrumento				= InfoGarantia.IdMonedaInstrumento;
				aux.IdMonedaEmision					= InfoGarantia.IdMonedaEmision;
				aux.IdTipoReajusteInstrumento		= InfoGarantia.IdTipoReajuste;	
				aux.EsFungible						= InfoGarantia.EsFungible;
				aux.MontoCompraMonedaInstrumento	= garantiasARetirar.MontoMonedaInstrumento;		
				aux.IdOrigenGarantia				= InfoGarantia.IdOrigenGarantia;
				aux.IdTipoOperacion					= in.p_IdTipoOperacion;
				aux.IdCondicionLiquidacion			= "T0";
				aux.FechaLiquidacion				= Fecha.toDate(in.p_FechaMovimiento);
				aux.IdEmisor						= InfoGarantia.IdEmisor;
				aux.IdBolsa							= garantiasARetirar.IdBolsa;
				if (InfoGarantia.IdOrigenGarantia.equalsIgnoreCase("ESP") ){
					aux.Cantidad					 = BigDecimal.ONE;
					aux.MontoCompra					 = InfoGarantia.MontoGarantia;
					aux.MontoCompraMonedaInstrumento = InfoGarantia.MontoGarantia;
				} 
				else {
					if (InfoGarantia.IdClasificacionTipoInstrumento.equalsIgnoreCase("IF") ){
						aux.Cantidad				= ("N").equalsIgnoreCase(aux.EsFungible) ? garantiasARetirar.Cantidad : BigDecimal.ONE;
						aux.ValorRescate			= garantiasARetirar.ValorRescate;
					} else {
						aux.Cantidad				= garantiasARetirar.Cantidad;
						aux.ValorRescate			= BigDecimal.ZERO;
					}
				};
				p_Datos.add(aux);
			};
			
			IngresoCustodia ingresoCustodia = new IngresoCustodia();
			IngresoCustodia.IngresoCustodiaEntrada paramInSP = new IngresoCustodia.IngresoCustodiaEntrada();
			paramInSP.p_IdEmpresa			= in.p_IdEmpresa;
			paramInSP.p_IdModulo			= in.p_IdModulo;
			paramInSP.p_SimularOperacion	= in.p_SimulaOperacion;
			paramInSP.p_TicketAtencion		= TicketAtencion;
			paramInSP.p_DatosEntrada		= p_Datos;
			IngresoCustodia.IngresoCustodiaSalida resultIngresoTransito = ingresoCustodia.logica(con, paramInSP);
			
			for (IngresoCustodia.IngresoCustodiaSalida.ObjDatosSalida result : resultIngresoTransito.P_OUT_DATOSSALIDA){
				if (result.RegistroConProblemas.equalsIgnoreCase("S")){
					out.P_OUT_MENSAJES.add(new MensajeSalida(result.IdGenerarMovimientoCustodia ,MensajeSalida.MENSAJE_ERROR, result.Mensaje, new HashMap<>() ,con,in.p_IdEmpresa));
					out.P_OUT_ERRORESCONTADOR = 1L;
					continue;
				};
				if (garantiaRetirarCerAuto.contains(result.IdGenerarMovimientoCustodia)){
					IdMovimientosEnTransito.add( result.IdCustodia);
				}
				else {
					HashMap<String,String> valores = new HashMap<String,String>();
					valores.put("Folio", result.IdCustodia.toString());
					out.P_OUT_MENSAJES.add(new MensajeSalida(result.IdCustodia, MensajeSalida.MENSAJE_ADVERTENCIA, "UI_INGRESO_TITULO_TRANSITO_OK", valores ,con,in.p_IdEmpresa));
				}
			};
		} else {
			for (Long M : in.p_MovimientosEnTransito){
				IdMovimientosEnTransito.add(M);
			};
		};
		
		
		HashMap<String,Object> paramsTransitos = new HashMap<>();
		paramsTransitos.put("IdMovimientosEnTransito", IdMovimientosEnTransito);
		
		List<IdGarantiaTransito> GarantiasTransitos = sql.query(con, SQL_GarantiasTransito, paramsTransitos, IdGarantiaTransito.class);
		
		for (IdGarantiaTransito GT: GarantiasTransitos) {
			IdGarantia.add(GT.IdGarantia);
		};
		paramsGarantias.put("IdGarantia", IdGarantia);
		
		HashMap<Long,Object> InformacionGarantiaTest = new HashMap<Long,Object>();
		//03 - PROCESAR MOVIMIENTO EN TRANSITO 
		List<GestionMovimientoCarteraEnTransito.ObjMovimientosEnTransito> MovimientosEnTransito =  GestionMovimientoCarteraEnTransito.ObtenerMovimientoEnTransito(con, IdMovimientosEnTransito);
		if ( MovimientosEnTransito.size() > 0 ){
			List<RetiroCustodia.ObjDatosEntrada> p_RetiroGarantia = new ArrayList<RetiroCustodia.ObjDatosEntrada>();
			List<GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar> p_Transitos = new ArrayList<GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar>();
			for (GestionMovimientoCarteraEnTransito.ObjMovimientosEnTransito Mov : MovimientosEnTransito) {
				RetiroCustodia.ObjDatosEntrada Ret = new RetiroCustodia.ObjDatosEntrada();
				Utils.copyPojo(Mov, Ret);
				Ret.IdGenerarMovimientoCustodia = Mov.IdMovimientoCarteraEnTransito;
				Ret.TablaAfectada 	= "GARANTIA";
				Ret.AccionOperacion = "RETIRO";
				Ret.IdCustodia		= Mov.IdCustodiaOrigen;					
				Ret.IdMovimientoCarteraEnTransitoOrigen	= Mov.IdMovimientoCarteraEnTransito;
				Ret.IdTipoReajusteInstrumento = Mov.IdTipoReajuste;
				ObjGarantias garantia = (ObjGarantias) InformacionGarantiaTest.get(Mov.IdCustodiaOrigen);
				Ret.FolioGarantiaOrigen = garantia != null ? garantia.FolioGarantia : 0L;
				p_RetiroGarantia.add(Ret);					
				
				GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar MOV = new GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar();
				MOV.IdMovimientoCarteraEnTransito = Mov.IdMovimientoCarteraEnTransito;
				MOV.EsCertificacion = "S";
				MOV.IdUsuario = in.p_IdUsuario;
				p_Transitos.add(MOV);
			};			

			//RETIRO DE GARANTIA
			if ( p_RetiroGarantia.size() > 0 ){
				RetiroCustodia.RetiroCustodiaSalida resultRetiros = new RetiroCustodia.RetiroCustodiaSalida();
				List<RetiroCustodia.ObjDatosEntrada> otrosRetiros = p_RetiroGarantia.stream().filter(ret -> !("CUS").equals(ret.IdOrigenGarantia)).collect(Collectors.toList());
				List<RetiroCustodia.ObjDatosEntrada> retirosCustodia = p_RetiroGarantia.stream().filter(ret -> ("CUS").equals(ret.IdOrigenGarantia)).collect(Collectors.toList());
				
				if(!otrosRetiros.isEmpty()) {
					RetiroCustodia retiroCustodia = new RetiroCustodia();
					RetiroCustodia.RetiroCustodiaEntrada paramRetCartera = new RetiroCustodia.RetiroCustodiaEntrada();
					paramRetCartera.p_IdEmpresa			= in.p_IdEmpresa;
					paramRetCartera.p_SimularOperacion	= in.p_SimulaOperacion;
					paramRetCartera.p_TicketAtencion	= TicketAtencion;
					paramRetCartera.p_DatosEntrada		= otrosRetiros;
					resultRetiros = retiroCustodia.logica(con, paramRetCartera);
				}
				
				if(!retirosCustodia.isEmpty()) {
					RetiroCustodia retiroCustodia = new RetiroCustodia();
					RetiroCustodia.RetiroCustodiaSalida resultRetirosCustodia = new RetiroCustodia.RetiroCustodiaSalida();
					RetiroCustodia.RetiroCustodiaEntrada paramRetCartera = new RetiroCustodia.RetiroCustodiaEntrada();
						
					paramRetCartera.p_IdEmpresa			= in.p_IdEmpresa;
					paramRetCartera.p_SimularOperacion	= in.p_SimulaOperacion;
					paramRetCartera.p_TicketAtencion	= TicketAtencion;
					paramRetCartera.p_DatosEntrada		= retirosCustodia;
					resultRetirosCustodia = retiroCustodia.logica(con, paramRetCartera);
					resultRetiros.P_OUT_CARTERAS_RETIRADAS.addAll(resultRetirosCustodia.P_OUT_CARTERAS_RETIRADAS);
					resultRetiros.P_OUT_DATOSSALIDA.addAll(resultRetirosCustodia.P_OUT_DATOSSALIDA);
					resultRetiros.P_OUT_MENSAJES.addAll(resultRetirosCustodia.P_OUT_MENSAJES);
				}
				
				resultRetiros.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(resultRetiros.P_OUT_MENSAJES);
				
				if (!resultRetiros.P_OUT_ERRORESCONTADOR.equals(0L)){						
					for (RetiroCustodia.RetiroCustodiaSalida.ObjDatosSalida msg : resultRetiros.P_OUT_DATOSSALIDA){
						if (msg.RegistroConProblemas.equals("N")) continue;
						out.P_OUT_MENSAJES.add(new MensajeSalida(msg.IdGenerarMovimientoCustodia ,MensajeSalida.MENSAJE_ERROR, msg.Mensaje, new HashMap<>() ,con,in.p_IdEmpresa));
					};						
					out.P_OUT_ERRORESCONTADOR = 1L;
					return out;												
				};
				for (RetiroCustodia.RetiroCustodiaSalida.ObjDatosSalida result : resultRetiros.P_OUT_DATOSSALIDA){
					foliosGarantia.put(result.IdGenerarMovimientoCustodia,result.IdGarantiaGenerada);
					
				}
				List<Long> listaTransitosCertificarIFRF = this.filtraMovimientoTransitoGarantiaIFRFCerticar(p_RetiroGarantia);				
				
				
				if(!listaTransitosCertificarIFRF.isEmpty() && "N".equals(retiraGarantiaIFRF)) {					
					//certificar movimiento en transito para garantia					
					CertificarMovimientosEnTransito.CertificarMovimientosEnTransitoSalida salidaCerMov = this.certificarMovimientoTransitoGarantiaIFRF(con,in,listaTransitosCertificarIFRF,fechaSistema);
					//preparar salida.		
					out.P_OUT_MENSAJES.addAll(salidaCerMov.P_OUT_MENSAJES);
					if (!salidaCerMov.P_OUT_ERRORESCONTADOR.equals(0L)){						
						out.P_OUT_ERRORESCONTADOR = 1L;
						return out;	
					}
				}
				if(!listaTransitosCertificarIFRF.isEmpty() && "S".equals(retiraGarantiaIFRF)) {
					List<CertificarMovimientosEnTransito.ObjMovimientos> movimientosACertificarIFRF = this.movimientosEntransitoIFRF(con,listaTransitosCertificarIFRF);
					List<GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar> p_TransitosIFRF = new ArrayList<GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar>();
					
					for (CertificarMovimientosEnTransito.ObjMovimientos MovACertificar : movimientosACertificarIFRF) {
						GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar Movimiento = new GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar();
						Movimiento.IdMovimientoCarteraEnTransito = MovACertificar.IdMovimientoCarteraEnTransito;
						Movimiento.EsCertificacion = "S";
						Movimiento.IdUsuario = in.p_IdUsuario;
						p_TransitosIFRF.add(Movimiento);
					}
					GestionMovimientoCarteraEnTransito.ActualizarMovimientoEnTransito(con, p_TransitosIFRF);
				}

				List<ObjGarantias> CMCCargoAbono = sql.query(con, SQL_Garantias, paramsGarantias, ObjGarantias.class);			
				HashMap<Long,Object> InformacionGarantiaCargoAbono = Utils.List2Hash(CMCCargoAbono, "IdGarantia");
				
				List<CrearCargoAbono.ObjCargoAbonoAGenerar> CargosAbonosAGenerar = new ArrayList<CrearCargoAbono.ObjCargoAbonoAGenerar>();
				for (RetiroCustodia.RetiroCustodiaSalida.ObjDatosSalida result : resultRetiros.P_OUT_DATOSSALIDA ){
					RetirarGarantiaJava.RetirarGarantiaSalida.ObjResultado resultadoProceso = new RetirarGarantiaJava.RetirarGarantiaSalida.ObjResultado(); 
					Utils.copyPojo(result, resultadoProceso);
					out.P_OUT_RESULTADOPROCESO.add(resultadoProceso);
					
					ObjGarantias InfoGarantia = (ObjGarantias) InformacionGarantiaCargoAbono.get(result.IdCustodia);
					GarantiasARetirar_Proceso infoGta = (GarantiasARetirar_Proceso) garantiasARetirar_ProcesoHash.get(result.IdCustodia);
					if (InfoGarantia == null) continue;
					if (InfoGarantia.IdOrigenGarantia.equals("ESP")) continue;
					
					//CUENTA INVERSION
					if (InfoGarantia.IdOrigenGarantia.equals("CTA")){
						CrearCargoAbono.ObjCargoAbonoAGenerar CargoAbono = new CrearCargoAbono.ObjCargoAbonoAGenerar();
						CargoAbono.NumeroCargoAbono				= result.IdGenerarMovimientoCustodia;
						CargoAbono.IdPortafolio					= InfoGarantia.IdPortafolio; 
						CargoAbono.IdMoneda						= InfoGarantia.IdMonedaInstrumento; 
						CargoAbono.IdTipoMovimiento				= "MOVABN"; 
						CargoAbono.IdConcepto					= infoGta != null && "S".equals(infoGta.EsProcesoContraparteGarantia) ? infoGta.IdConcepto : "RETGCC";
						CargoAbono.Descripcion					= "RETIRO GARANTIA"; 
						CargoAbono.IdCondicionDeLiquidacion		= "T0"; 
						CargoAbono.FechaCondicionDeLiquidacion	= in.p_FechaMovimiento; 
						CargoAbono.Monto						= InfoGarantia.MontoRetiroAMonedaInstrumento; 
						//CargoAbono.IdEjecutivo					= InfoGarantia.IdEmpleadoEjecutivo;
						CargoAbono.IdEjecutivo					= (in.p_IdEmpleado != null && in.p_IdEmpleado>0 ? in.p_IdEmpleado : InfoGarantia.IdEmpleadoEjecutivo);
						CargoAbono.IdSucursal					= InfoGarantia.IdSucursalEjecutivo;
						CargoAbono.IdTipoProducto				= "";
						CargoAbono.IdTipoOperacion				= in.p_IdTipoOperacion; 
						CargoAbono.IdOperacion					= InfoGarantia.FolioGarantia; 
						CargoAbono.FechaOperacion				= in.p_FechaMovimiento;
						CargoAbono.FechaLiquidacionOperacion	= in.p_FechaMovimiento; 
						CargoAbono.FechaVencimientoOperacion	= "0001-01-01"; 
						CargoAbono.IdMonedaOperacion			= ""; 
						CargoAbono.MontoOperacion				= new BigDecimal(0); 
						CargoAbono.TipoCambioOperacion			= new BigDecimal(0); 
						CargoAbono.IdContraparte				= InfoGarantia.IdContraparteGarantia; 
						CargoAbono.IdTipoComprobante			= ""; 
						CargoAbono.IdComprobante				= result.IdGarantiaGenerada; 
						CargoAbono.FolioComprobante				= result.FolioCustodia.toString(); 
						CargoAbono.IdComprobanteDetalle			= 0L; 
						CargoAbono.IdComprobanteFormaDePago		= 0L; 
						CargoAbono.IdTipoMovimientoAsociado		= in.p_IdTipoOperacion; 
						CargoAbono.IdMovimientoAsociado			= result.IdGenerarMovimientoCustodia; 
						CargoAbono.FechaComprobante				= in.p_FechaMovimiento; 
						CargoAbono.IdPersonaPagoCobro			= 0L;
						CargoAbono.IdInstrumento				= InfoGarantia.IdInstrumentoCarteraGarantizada; 
						CargoAbono.IdTipoInstrumento			= InfoGarantia.IdTipoInstrumento; 
						CargoAbono.Nemo							= ""; 
						CargoAbono.EsIndicadorBilateral			= ""; 
						CargoAbono.EsCargoAbonoPorTraspaso		= "N"; 
						CargoAbono.IdPortafolioTraspaso			= 0L; 
						CargoAbono.GeneroContraparteEnTesoreria	= "N";
						
						CargoAbono.CertificadoAutomatico		= InfoGarantia.ConCertificadoAutomatico;
						CargosAbonosAGenerar.add(CargoAbono);
					};
				};
		
				//REALIZAR ABONO A CUENTA INVERSION
				if (out.P_OUT_ERRORESCONTADOR.equals(0L) && CargosAbonosAGenerar.size() > 0 ){
					CrearCargoAbono.CrearCargoAbonoEntrada paramsCrearCargoAbono = new CrearCargoAbono.CrearCargoAbonoEntrada();
					paramsCrearCargoAbono.p_IdEmpresa			= in.p_IdEmpresa;
					paramsCrearCargoAbono.p_IdUsuario			= in.p_IdUsuario;
					paramsCrearCargoAbono.p_IdModulo			= in.p_IdModulo;
					paramsCrearCargoAbono.p_FechaSistema		= in.p_FechaMovimiento;
					paramsCrearCargoAbono.p_CargoAbonoAGenerar 	= CargosAbonosAGenerar;
					paramsCrearCargoAbono.p_simularOperacion	= "N";
					paramsCrearCargoAbono.p_UsaReglasNegocio	= "S";						
					paramsCrearCargoAbono.p_EsIngresoManual		= "N";						
					
					
					CrearCargoAbono CrearCargoAbonoSP = new CrearCargoAbono();
					CrearCargoAbono.CrearCargoAbonoSalida resultCrearCargoAbono = CrearCargoAbonoSP.logica(con, paramsCrearCargoAbono);
					if (!resultCrearCargoAbono.P_OUT_ERRORESCONTADOR.equals(0L)){						
						out.P_OUT_MENSAJES = resultCrearCargoAbono.P_OUT_MENSAJES;					
						out.P_OUT_ERRORESCONTADOR = 1L;
						return out;												
					}else{
						for (MensajeSalida newMens : resultCrearCargoAbono.P_OUT_MENSAJES ){
							out.P_OUT_MENSAJES.add(newMens);
						}
					};
				};
				
			};
			
			if (out.P_OUT_ERRORESCONTADOR.equals(0L)){
				GestionMovimientoCarteraEnTransito.ActualizarMovimientoEnTransito(con, p_Transitos);
				
				for (RetiroCustodia.ObjDatosEntrada info : p_RetiroGarantia){
					HashMap<String,String> valores = new HashMap<String,String>();
					valores.put("Folio", info.IdCustodia.toString());
					out.P_OUT_MENSAJES.add(new MensajeSalida(info.FolioCustodia ,MensajeSalida.MENSAJE_INFORMACION, "UI_RETIRO_GARANTIA_OK", valores ,con, in.p_IdEmpresa));
				};
			};
		};
		
		List<ClienteNeteo> clientesANetear = new ArrayList<>();
		for(GestionMovimientoCarteraEnTransito.ObjMovimientosEnTransito movimientoEnTransito : MovimientosEnTransito) {
			ClienteNeteo clienteNeteo = new ClienteNeteo();
			clienteNeteo.IdInstrumento = movimientoEnTransito.IdInstrumento;
			clienteNeteo.IdPortafolio = movimientoEnTransito.IdPortafolio;
			clientesANetear.add(clienteNeteo);
		}
		
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
			AP.logica(con, paramsIn);
		}
		
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		
		log.write("FIN RETIRO GARANTIAS");
		log.write("SALIDA > " + out.toString());
		
		return out;
	}


	private CertificarMovimientosEnTransitoSalida existeCertificarMovimientoTransitoGarantiaIFRF(Connection con,RetirarGarantiaEntrada in, List<Long> listaTransitosCertificarIFRF, String fechaSistema) throws Exception {
		logger.trace("INICIO::RetirarGarantia.existeCertificarMovimientoTransitoGarantiaIFRF");
		long inicioMetodo = System.currentTimeMillis();
			
		CertificarMovimientosEnTransito.CertificarMovimientosEnTransitoSalida  salidaCerMov = new CertificarMovimientosEnTransito.CertificarMovimientosEnTransitoSalida();
		List<CertificarMovimientosEnTransito.ObjMovimientos> movimientoACertificarIFRF = this.movimientosEntransitoIFRF(con,listaTransitosCertificarIFRF);
		HashMap<Long,CertificarMovimientosEnTransito.ObjMovimientos> hashMovimientoEnTransito = new HashMap<Long,CertificarMovimientosEnTransito.ObjMovimientos>(); 		
		this.validarCantidadGaratiaTransito(movimientoACertificarIFRF,in.p_tablaRetiro,hashMovimientoEnTransito,salidaCerMov);	
		
		long finMetodo = System.currentTimeMillis();
		logger.trace("FIN::RetirarGarantia.certificarMovimientoTransitoGarantiaIFRF :" , finMetodo-inicioMetodo);
		return salidaCerMov;
	}

	private void validarGarantiasInstrumentosVencidos(CertificarMovimientosEnTransitoSalida salidaCerMov,HashMap<Long, GarantiasARetirar_Proceso> garantiasARetirar_ProcesoHash, List<Long> IdGarantia,	List<GarantiasARetirar_Proceso> garantiasARetirar_Proceso) {
		logger.trace("INICIO::RetirarGarantia.existeCertificarMovimientoTransitoGarantiaIFRF");
		long inicioMetodo = System.currentTimeMillis();	
		

		HashMap<Long,CertificarMovimientosEnTransitoSalida.ObjMovimientosProcesados> garantiasDescatadasHash = new HashMap<Long,CertificarMovimientosEnTransitoSalida.ObjMovimientosProcesados>();
		
		for ( CertificarMovimientosEnTransitoSalida.ObjMovimientosProcesados gar  : salidaCerMov.P_OUT_MOVIMIENTOSPROCESADOS) {
			if("NOK".equals(gar.IdEstado)) {
				garantiasDescatadasHash.put(gar.IdMovimientoCarteraEnTransito,gar);		
			}
				
		}	
		
		List <Long>IdGarantiafiltrada = new ArrayList<Long>();
		List<GarantiasARetirar_Proceso> garantiasARetirar_Proceso_filtrado = new ArrayList<GarantiasARetirar_Proceso>();				
		
		for (Long garantia :IdGarantia ) {					
			if(garantiasDescatadasHash.get(garantia)==null ) {
				IdGarantiafiltrada.add(garantia);
				garantiasARetirar_Proceso_filtrado.add(garantiasARetirar_ProcesoHash.get(garantia));
			}
			
		}
		
		IdGarantia.clear();
		garantiasARetirar_Proceso.clear();
		IdGarantia.addAll(IdGarantiafiltrada);
		garantiasARetirar_Proceso.addAll(garantiasARetirar_Proceso_filtrado);
		
		long finMetodo = System.currentTimeMillis();
		logger.trace("FIN::RetirarGarantia.validarGarantiasInstrumentosVencidos :" , finMetodo-inicioMetodo);
		
	}

	private List<Long> filtraMovimientoTransitoGarantiaIFRF(List<ObjGarantiasARetirar> p_RetiroGarantia) {
		logger.trace("INICIO::RetirarGarantia.filtraMovimientoTransitoGarantiaIFRF");
		long inicioMetodo = System.currentTimeMillis();		
				
		List<ObjGarantiasARetirar> filtroRetiroGarantia = p_RetiroGarantia.stream().filter(ret -> 
			"IF".equals(ret.IdClasificacionTipoInstrumento )|| 
			"RF".equals(ret.IdClasificacionTipoInstrumento )
		).collect(Collectors.toList());		
		
		//se los Id IdCustodia = IdGarantia   				
		List<Long> listaTransitosCertificarIFRF = filtroRetiroGarantia.stream().map(gar -> {					
			return gar.IdGarantia; 					
		}).collect(Collectors.toList());
		
		long finMetodo = System.currentTimeMillis();
		logger.trace("FIN::RetirarGarantia.filtraMovimientoTransitoGarantiaIFRF :" , finMetodo-inicioMetodo);
		return listaTransitosCertificarIFRF;
	}
	
	private List<Long> filtraMovimientoTransitoGarantiaIFRFCerticar(List<ObjDatosEntrada> p_RetiroGarantia) {
		logger.trace("INICIO::RetirarGarantia.filtraMovimientoTransitoGarantiaIFRF");
		long inicioMetodo = System.currentTimeMillis();		
				
		List<RetiroCustodia.ObjDatosEntrada> filtroRetiroGarantia = p_RetiroGarantia.stream().filter(ret -> 
			"IF".equals(ret.IdClasificacionTipoInstrumento )|| 
			"RF".equals(ret.IdClasificacionTipoInstrumento )
		).collect(Collectors.toList());		
		
		//se los Id IdCustodia = IdGarantia   				
		List<Long> listaTransitosCertificarIFRF = filtroRetiroGarantia.stream().map(gar -> {					
			return gar.IdCustodia; 					
		}).collect(Collectors.toList());
		
		long finMetodo = System.currentTimeMillis();
		logger.trace("FIN::RetirarGarantia.filtraMovimientoTransitoGarantiaIFRF :" , finMetodo-inicioMetodo);
		return listaTransitosCertificarIFRF;
	}				
				

	public CertificarMovimientosEnTransitoSalida certificarMovimientoTransitoGarantiaIFRF(Connection con,RetirarGarantiaEntrada in, List<Long> listaTransitosCertificarIFRF, String fechaSistema) throws Exception {
		logger.trace("INICIO::RetirarGarantia.certificarMovimientoTransitoGarantiaIFRF");
		long inicioMetodo = System.currentTimeMillis();
		
		CertificarMovimientosEnTransito certificaMovTransito = new CertificarMovimientosEnTransito();
		CertificarMovimientosEnTransito.CertificarMovimientosEnTransitoEntrada entradaCerMov = new CertificarMovimientosEnTransito.CertificarMovimientosEnTransitoEntrada();	
		CertificarMovimientosEnTransito.CertificarMovimientosEnTransitoSalida  salidaCerMov = new CertificarMovimientosEnTransito.CertificarMovimientosEnTransitoSalida();
		List<CertificarMovimientosEnTransito.ObjMovimientos> movimientoACertificarIFRF = this.movimientosEntransitoIFRF(con,listaTransitosCertificarIFRF);		
		
		entradaCerMov.p_IdEmpresa = in.p_IdEmpresa;
		entradaCerMov.p_IdUsuario = in.p_IdUsuario;
		entradaCerMov.p_IdModulo  = in.p_IdModulo;
		entradaCerMov.p_UsaReglasNegocio = in.p_UsaReglasNegocio;
		entradaCerMov.p_FechaOperacion = fechaSistema;
		entradaCerMov.P_MOVIMIENTOSCERTIFICAR = movimientoACertificarIFRF;				
		
		if(!movimientoACertificarIFRF.isEmpty()) {
			salidaCerMov= certificaMovTransito.logica(con, entradaCerMov);
		}		
		
		long finMetodo = System.currentTimeMillis();
		logger.trace("FIN::RetirarGarantia.certificarMovimientoTransitoGarantiaIFRF :" , finMetodo-inicioMetodo);
		return salidaCerMov;
	}

	private void validarCantidadGaratiaTransito(List<ObjMovimientos> movimientoACertificarIFRF,	List<ObjGarantiasARetirar> p_tablaRetiro, HashMap<Long, ObjMovimientos> hashMovimientoEnTransito,CertificarMovimientosEnTransitoSalida salidaCerMov) {
		logger.trace("INICIO::RetirarGarantia.validarCantidadGaratiaTransito");
		long inicioMetodo = System.currentTimeMillis();
		
		movimientoACertificarIFRF.forEach(tra -> {
			hashMovimientoEnTransito.put(tra.IdOperacion, tra);			
		});
		
				
		for(ObjGarantiasARetirar gar : p_tablaRetiro ) {			
			CertificarMovimientosEnTransito.ObjMovimientos transito = hashMovimientoEnTransito.get(gar.IdGarantia);
			CertificarMovimientosEnTransito.CertificarMovimientosEnTransitoSalida.ObjMovimientosProcesados movimientoDescartado = new CertificarMovimientosEnTransito.CertificarMovimientosEnTransitoSalida.ObjMovimientosProcesados ();
			
			if(transito != null) {
				if("IF".equals(transito.IdClasificacionTipoInstrumento)) {
					if(transito.ValorRescate.compareTo(gar.ValorRescate)!= 0) {
						hashMovimientoEnTransito.remove(gar.IdGarantia);
						salidaCerMov.P_OUT_MENSAJES.add(new MensajeSalida(gar.IdGarantia, MensajeSalida.MENSAJE_ERROR,"UI_CANTIDAD_DE_RETIRO_MENOR_AL_TOTAL","Cantidad del retiro es menor al total") );
						salidaCerMov.P_OUT_ERRORESCONTADOR=1L;
						movimientoDescartado.IdClasificacionTipoInstrumento = transito.IdClasificacionTipoInstrumento;
						movimientoDescartado.IdConcepto = gar.IdConcepto;
						movimientoDescartado.IdEstado = "NOK";
						movimientoDescartado.IdMovimientoCarteraEnTransito = gar.IdGarantia;
						salidaCerMov.P_OUT_MOVIMIENTOSPROCESADOS.add(movimientoDescartado);
					}
				}
				if("RF".equals(transito.IdClasificacionTipoInstrumento)) {
					if(transito.Cantidad.compareTo(gar.Cantidad)!= 0) {
						hashMovimientoEnTransito.remove(gar.IdGarantia);
						salidaCerMov.P_OUT_MENSAJES.add(new MensajeSalida(gar.IdGarantia, MensajeSalida.MENSAJE_ERROR,"UI_CANTIDAD_DE_RETIRO_MENOR_AL_TOTAL","Cantidad del retiro es menor al total") );
						salidaCerMov.P_OUT_ERRORESCONTADOR=1L;
						movimientoDescartado.IdClasificacionTipoInstrumento = transito.IdClasificacionTipoInstrumento;
						movimientoDescartado.IdConcepto = gar.IdConcepto;
						movimientoDescartado.IdEstado = "NOK";
						movimientoDescartado.IdMovimientoCarteraEnTransito = gar.IdGarantia;
						salidaCerMov.P_OUT_MOVIMIENTOSPROCESADOS.add(movimientoDescartado);
					}
				}
			}	
		}
		
		movimientoACertificarIFRF.clear();
		for(Map.Entry<Long,CertificarMovimientosEnTransito.ObjMovimientos> movTransito : hashMovimientoEnTransito.entrySet()) {
			movimientoACertificarIFRF.add(movTransito.getValue());
		}		
		
		long finMetodo = System.currentTimeMillis();
		logger.debug("FIN::RetirarGarantia.validarCantidadGaratiaTransito :" , finMetodo-inicioMetodo);		
		
	}

	private List<ObjMovimientos> movimientosEntransitoIFRF(Connection con, List<Long> listaTransitosCertificarIFRF) throws Exception {
		logger.debug("INICIO::RetirarGarantia.movimientosEntransitoIFRF");
		long inicioMetodo = System.currentTimeMillis();
					
		SQL sql = new SQL();
		HashMap<String,Object> params = new HashMap<>(1);
		params.put("IdOperacion", listaTransitosCertificarIFRF);
		
		List<ObjMovimientos> listaMovimientoEnTrasito= sql.query(con, SQL_GarantiasTransitoIFRF, params, ObjMovimientos.class);
		
		long finMetodo = System.currentTimeMillis();
		logger.debug("FIN::RetirarGarantia.movimientosEntransitoIFRF :" , finMetodo-inicioMetodo);
		return listaMovimientoEnTrasito;
	}

	// --------------------------------------------------
	// CALL
	// --------------------------------------------------
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();		
		RetirarGarantiaEntrada jin=gson.fromJson(in.toString() ,RetirarGarantiaEntrada.class);
		RetirarGarantiaSalida out=logica(conn,jin);		
		
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	};
	

	// --------------------------------------------------
	// METODO PARA PRUEBAS LOCALES
	// --------------------------------------------------
	
	public static void main(String[] args) throws Exception {
		System.out.println("== INICIO PRUEBA ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa2","connections.properties")) {
			try {
				Gson gson = Utils.getGsonBuilder();	
				String json = "{\r\n"
						+ "    \"p_IdEmpresa\": 1,\r\n"
						+ "    \"p_IdUsuario\": 97,\r\n"
						+ "    \"p_IdModulo\": \"CUS\",\r\n"
						+ "    \"p_IdEmpleado\": 108,\r\n"
						+ "    \"p_UsaReglasNegocio\": \"S\",\r\n"
						+ "    \"p_SimulaOperacion\": \"N\",\r\n"
						+ "    \"p_FechaMovimiento\": \"2025-06-05\",\r\n"
						+ "    \"p_IdTipoOperacion\": \"RETGTA\",\r\n"
						+ "    \"p_tablaRetiro\": [\r\n"
						+ "        {\r\n"
						+ "            \"IdGarantia\": 52986,\r\n"
						+ "            \"Cantidad\": 400,\r\n"
						+ "            \"ValorRescate\": 400,\r\n"
						+ "            \"MontoMonedaInstrumento\": 1624891.28,\r\n"
						+ "            \"PrecioRetiro\": 4612.6,\r\n"
						+ "            \"TasaRetiro\": 0,\r\n"
						+ "            \"IdConcepto\": \"IGRV85\",\r\n"
						+ "            \"ConCertificadoAutomatico\": \"S\",\r\n"
						+ "            \"IdClasificacionTipoInstrumento\": \"RV\"\r\n"
						+ "        },\r\n"
						+ "        {\r\n"
						+ "            \"IdGarantia\": 52985,\r\n"
						+ "            \"Cantidad\": 500,\r\n"
						+ "            \"ValorRescate\": 500,\r\n"
						+ "            \"MontoMonedaInstrumento\": 2031114.1,\r\n"
						+ "            \"PrecioRetiro\": 4612.6,\r\n"
						+ "            \"TasaRetiro\": 0,\r\n"
						+ "            \"IdConcepto\": \"IGRV85\",\r\n"
						+ "            \"ConCertificadoAutomatico\": \"S\",\r\n"
						+ "            \"IdClasificacionTipoInstrumento\": \"RV\"\r\n"
						+ "        }\r\n"
						+ "    ]\r\n"
						+ "}";
				
				RetirarGarantiaEntrada Objin = gson.fromJson(json, RetirarGarantiaEntrada.class);
								
				RetirarGarantiaJava  kk = new RetirarGarantiaJava();				
				RetirarGarantiaSalida salida = kk.logica(con, Objin);
				
//				con.commit();
				con.rollback();
				
		    	}catch(Exception er) {
					er.printStackTrace();	
					con.rollback();
				}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("== FIN PRUEBA  ==");
	};
}
