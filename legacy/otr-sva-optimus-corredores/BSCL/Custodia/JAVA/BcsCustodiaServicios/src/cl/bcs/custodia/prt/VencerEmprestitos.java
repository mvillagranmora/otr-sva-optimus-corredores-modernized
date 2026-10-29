package cl.bcs.custodia.prt;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.custodia.NotificarVencimientoInstrumentos;
import cl.bcs.custodia.NotificarVencimientoInstrumentos.EntradaNotificarVencimientoInstrumentos;
import cl.bcs.custodia.prt.VencerPrestamos.FolioMontoPremio;
import cl.bcs.facturacion.CrearMovimientoComprobante;
import cl.bcs.plataforma.bat.AgregarPrograma;
import cl.bcs.plataforma.bat.AgregarPrograma.InAgregarPrograma.Parametro;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;



public class VencerEmprestitos implements JavaCall {

	private final Log log = new Log("CUS.VencerEmprestitos");
	private String fechaVencimiento;
	public final String SI = "S";
	
	private final String SQL_VencerEmprestitos_BuscaEmprestitos_x_Fecha = loadResourceToString("cl/bcs/custodia/sql/prt/VencerEmprestitos_SelectEmprestitosPorFecha.sql");
	private final String SQL_VencerEmprestitos_BuscaEmprestitos_x_Folio = loadResourceToString("cl/bcs/custodia/sql/prt/VencerEmprestitos_SelectEmprestitosPorFolio.sql");
	private final String SQL_VencerEmprestitos_BuscaEmprestitos_x_Prestamo = loadResourceToString("cl/bcs/custodia/sql/prt/VencerEmprestitos_SelectEmprestitosPorPrestamo.sql");
	private final String SQL_CONSULTAR_PLAZO_COBRADOS_Call = loadResourceToString("cl/bcs/custodia/sql/prt/CALL_ConsultarPlazoCobrado.sql");
	
	public static class ObjRegistroEmprestito{
		public Long FolioEmprestito;
	}
	

	public static class Entrada {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public Long p_IdPais;
		public String p_Fecha;
		public String p_OrigenLiquidacion; //ANTICIPO - VENCIMIENTO
		public String p_RebajaPrestamo;   //Si viene en S, significa q es un anticipo manual del UN prestamista, por ende tengo q rebajar la cantidad que estoy anticipando tambien del prestamo.
		public List<Long> p_lstEmprestitos;
		public List<FolioMontoPremio> p_lstPrestamos;
		public List<EmprestitoAnticipando>	p_lstAnticipos;
		public boolean p_EsPauta = false;
		
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
		public Entrada(){
			this.p_RebajaPrestamo = "N";
			this.p_lstEmprestitos = new ArrayList<Long>();
			this.p_lstPrestamos = new ArrayList<FolioMontoPremio>();
			this.p_lstAnticipos = new ArrayList<EmprestitoAnticipando>();
			this.p_OrigenLiquidacion = "VENCIMIENTO";
		}
		
	}

	public static class Salida {
		public Long P_OUT_ERRORESCONTADOR;
		public Long P_OUT_ADVERTENCIASCONTADOR;
		public String P_OUT_DESCRIPCION;
		public List<MensajeSalida> P_OUT_MENSAJES;
		public List<LogEjecucion> P_OUT_LOGEJECUCION;

		public Salida() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_ADVERTENCIASCONTADOR = 0L;
			this.P_OUT_DESCRIPCION = "";
			
		}

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}

	public static class LogEjecucion {
		public Long TimeMillis;
		public String Mensaje;
		public String EsError;
		public String EsInterno;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	public static class EmprestitoAnticipando {
		public Long       		FolioEmprestito;
		public BigDecimal      	PrecioInstrumento;
		public BigDecimal      	Cantidad;
		public BigDecimal      	MontoInstrumento;
		public BigDecimal      	TasaPremio;
		public BigDecimal      	MontoPremio;
		public BigDecimal      	MontoAPlazo;
		public Integer      	PlazoAlVencimiento;
		public Long       		IdEmpleadoDigitador;
		public java.sql.Date 	FechaEmprestito;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	

	public static class ObjRegistroEmprestitoDetalle {
		public Long IdCartera;
		public Long FolioEmprestito;
		public java.sql.Date FechaVencimientoEmprestito;
		public java.sql.Date FechaEmprestito;
		public Long IdCliente;
		public Long IdPortafolio;
		public String IdTipoProducto;
		public Long FolioPrestamo;
		public Long IdClientePrestamo;
		public Long IdPortafolioPrestamo;
		public java.sql.Date FechaPrestamo;
		public Long IdInstrumentoEmprestito;
		public String IdTipoInstrumentoEmprestito;
		public String IdEmisorEmprestito;
		public String NemoSubyacente;
		public Long IdInstrumentoSubyacente;
		public String IdTipoInstrumentoSubyacente;
		public String IdClasificacionTipoInstrumentoSubyacente;
		public String IdTipoReajusteSubyacente;
		public String IdEmisorSubyacente;
		public String IdMonedaSubyacente;
		public BigDecimal INGCantidad;
		public BigDecimal INGTasaEmprestito;
		public BigDecimal INGValorRescate;
		public BigDecimal INGMontoInstrumento;
		public BigDecimal INGMontoTotalEmprestito;
		public BigDecimal INGMontoPremio;
		public String ConCustodiaPrestamista;
		public String ConCustodiaPrestatario;
		public String IdBolsa;
		public String OperaContraCuentaInversionPrestamista;
		public String OperaContraCuentaInversionPrestatario;
		
		public BigDecimal INGTasaInstrumento;
		public BigDecimal PrecioInstrumento;
		public Long PlazoAlVencimiento;
		public Long EmpleadoDigitador;
		
		public Long PlazoEnDiasOperacion;
		public String IdTipoPrestamo;
		public java.sql.Date FechaLiquidacion;
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	}
	
	public static class ObjERRORESDETALLES {
		public String	Codigo;
		public String	Descripcion;
		public String toString() {
			return Utils.printJson(this, false);
		};
	}
	
	public static class ObjADVERTENCIASDETALLES {
		public String	Codigo;
		public String	RequiereAutorizacion;
		public String	Descripcion;
		public String toString() {
			return Utils.printJson(this, false);
		};
	}
	public static class EntradaNeteo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public String p_SimularOperacion;
		public List<ClienteNeteo> p_PortafoliosInstrumento;
	}
	public static class ClienteNeteo {
		public Long IdPortafolio;
		public Long IdInstrumento;
	}

	/*******************
	 * metodo principal
	 *******************/
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		log.write("*** Inicio de la ejecucion ***");
		Gson gson = new Gson();
		Entrada in = gson.fromJson(jin.toString(), Entrada.class);
		Salida out = logica(con, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		log.write("*** Fin de la ejecucion ***");
		return dataout;
	}

	/**********************
	 * logica del programa
	 **********************/
	public Salida logica(Connection con, Entrada entrada) throws Exception {
		log.write("INICIO::EjecutarVencimiento/Anticipo/Anulacion");
		SQL sql = new SQL();
		Salida out = new Salida();
		fechaVencimiento = entrada.p_Fecha;
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String IdMonedaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.MonedaSistema");
		

		log.write("Inicia ejecucion de " + entrada.p_OrigenLiquidacion + " Emprestitos con datos de entrada: " + entrada);
		
		out.P_OUT_ERRORESCONTADOR = 0L;
		out.P_OUT_ADVERTENCIASCONTADOR = 0L;
		out.P_OUT_LOGEJECUCION = new ArrayList<LogEjecucion>();
		
		HashMap<String, Object> prmBuscaEmprestitos = new HashMap<String, Object>();
		prmBuscaEmprestitos.put("p_IdEmpresa", entrada.p_IdEmpresa);
		ArrayList<Long> folioList = new ArrayList<>();
		String sqlAUsar = "";
		List<Long> listPrestamo = new ArrayList<Long>();
		if (!entrada.p_lstEmprestitos.isEmpty()){   // si es q ya sabe que emprestitos va a vencer
			for (Long folio: entrada.p_lstEmprestitos){
				folioList.add(folio);
			}
			prmBuscaEmprestitos.put("lstFolioEmprestito", folioList);
			sqlAUsar = SQL_VencerEmprestitos_BuscaEmprestitos_x_Folio;
			
		} else if (!entrada.p_lstPrestamos.isEmpty()){  // si es que va a vencer los emprestitos de uno o mas prestamos
			for (FolioMontoPremio emp: entrada.p_lstPrestamos){
				listPrestamo.add(emp.Folio);
			}
			prmBuscaEmprestitos.putIfAbsent("lstFolioPrestamo", listPrestamo);//test
			sqlAUsar = SQL_VencerEmprestitos_BuscaEmprestitos_x_Prestamo;
			
		} else {                                         // todos los emprestitos que venzan en la fecha 
			prmBuscaEmprestitos.put("FechaVencimiento", entrada.p_Fecha);
			sqlAUsar = SQL_VencerEmprestitos_BuscaEmprestitos_x_Fecha;
			
		}
		
		List<ObjRegistroEmprestitoDetalle> lstEmprestitos = new ArrayList<ObjRegistroEmprestitoDetalle>();
		List<ObjRegistroEmprestitoDetalle> lstEmprestitosOK = new ArrayList<ObjRegistroEmprestitoDetalle>();
		List<ObjRegistroEmprestitoDetalle> lstEmprestitosMovimientosComprobante = new ArrayList<ObjRegistroEmprestitoDetalle>();
		
		lstEmprestitos = sql.query(con, sqlAUsar, prmBuscaEmprestitos,ObjRegistroEmprestitoDetalle.class);
		
		
		String usaFechaLiquidacionEnVencimiento = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "CUS.UsaFechaLiquidacionEnVencimiento");
		
		//--- cuando se trata de anticipo de emprestito, rescata los valores enviados para anticipar --
		if (entrada.p_OrigenLiquidacion.equalsIgnoreCase("ANTICIPO")){
			for (ObjRegistroEmprestitoDetalle reg : lstEmprestitos){
				
				for(FolioMontoPremio emp: entrada.p_lstPrestamos) {
					if(reg.FolioPrestamo.equals(emp.Folio)) {
						reg.INGMontoPremio = emp.MontoPremio;
						if(emp.Cantidad.compareTo(BigDecimal.ZERO) != 0 && emp.Cantidad.compareTo(reg.INGCantidad) == -1) {
							reg.INGCantidad = emp.Cantidad;
						}
					}
				}
				
				for (EmprestitoAnticipando datoEmpr : entrada.p_lstAnticipos){
					if (reg.FolioEmprestito.equals(datoEmpr.FolioEmprestito)){
						reg.INGCantidad = datoEmpr.Cantidad;
						reg.PrecioInstrumento = datoEmpr.PrecioInstrumento;
						reg.INGMontoInstrumento = datoEmpr.MontoInstrumento;
						reg.INGMontoPremio = datoEmpr.MontoPremio;
						reg.INGMontoTotalEmprestito = datoEmpr.MontoAPlazo;
						reg.INGTasaEmprestito = datoEmpr.TasaPremio;
						reg.FechaEmprestito = datoEmpr.FechaEmprestito;
						break;  
					}
				}
			}
		}
		

		
		// -------------------------------------\\
		// ----- Liquida Custodia Emprestitos 
		// -------------------------------------\\
		
		log.write("Inicio Ejecucion LiquidarCustodiaEmprestitos");
		logEjecucion(out.P_OUT_LOGEJECUCION, "Se invoca Custodia Liquidar Emprestitos", "N", "N");
		
		LiquidarCustodiaEmprestito servicioLiquidarCustodiaEmprestito = new LiquidarCustodiaEmprestito();
		LiquidarCustodiaEmprestito.LiquidarCustodiaEmprestito_Entrada entradaLiquidarCustodiaEmprestito = new LiquidarCustodiaEmprestito.LiquidarCustodiaEmprestito_Entrada();
		LiquidarCustodiaEmprestito.LiquidarCustodiaEmprestito_Salida salidaLiquidarCustodiaEmprestito = new LiquidarCustodiaEmprestito.LiquidarCustodiaEmprestito_Salida();
		
		entradaLiquidarCustodiaEmprestito.p_IdEmpresa 			= entrada.p_IdEmpresa;
		entradaLiquidarCustodiaEmprestito.p_IdUsuario 			= entrada.p_IdUsuario;
		entradaLiquidarCustodiaEmprestito.p_IdModulo 			= entrada.p_IdModulo;
		entradaLiquidarCustodiaEmprestito.p_IdPais 				= entrada.p_IdPais;
		entradaLiquidarCustodiaEmprestito.p_RebajaPrestamo 		= entrada.p_RebajaPrestamo;
		entradaLiquidarCustodiaEmprestito.p_SimularOperacion 	= "N";
		entradaLiquidarCustodiaEmprestito.p_OrigenLiquidacion 	= entrada.p_OrigenLiquidacion;
		entradaLiquidarCustodiaEmprestito.p_FechaVencimientoAnticipo 	= entrada.p_Fecha;
		
		for (ObjRegistroEmprestitoDetalle car : lstEmprestitos) {
			LiquidarCustodiaEmprestito.EmprestitoALiquidar entradaEmprestitoLiquidar = new LiquidarCustodiaEmprestito.EmprestitoALiquidar();
			Utils.copyPojo(car, entradaEmprestitoLiquidar);
			entradaEmprestitoLiquidar.FechaEmprestito = car.FechaEmprestito.toString();
			entradaLiquidarCustodiaEmprestito.p_EmprestitosVencer.add(entradaEmprestitoLiquidar);
		}
		
		salidaLiquidarCustodiaEmprestito = servicioLiquidarCustodiaEmprestito.logica(con, entradaLiquidarCustodiaEmprestito);
		out.P_OUT_ERRORESCONTADOR = salidaLiquidarCustodiaEmprestito.P_OUT_ERRORESCONTADOR;
		
		
		for (MensajeSalida mens : salidaLiquidarCustodiaEmprestito.P_OUT_MENSAJES){
			if (mens.tipo.equalsIgnoreCase(MensajeSalida.MENSAJE_ERROR)) out.P_OUT_MENSAJES.add(mens);
		}
		
		for (ObjRegistroEmprestitoDetalle reg : lstEmprestitos){
			for (Long emprOk : salidaLiquidarCustodiaEmprestito.P_OUT_EMPRESTITOS_OK){
				if (reg.FolioEmprestito.equals(emprOk)){
					lstEmprestitosOK.add(reg);
					break;
				}
			}
		}
		
		
		//----------------------------------------------------------------\\
		//---- Genera los movimientos a factuar de emprestitos vencidos ----\\
		//----------------------------------------------------------------\\		
		if (lstEmprestitosOK.size() > 0 && !entrada.p_OrigenLiquidacion.equalsIgnoreCase("ANULACION")) {
			
			for (ObjRegistroEmprestitoDetalle itemEmprestitoOK : lstEmprestitosOK) {
				if (!usaFechaLiquidacionEnVencimiento.equalsIgnoreCase(SI) || (usaFechaLiquidacionEnVencimiento.equalsIgnoreCase(SI) && itemEmprestitoOK.FechaLiquidacion.before(dateFormat.parse(entrada.p_Fecha)))) {
					lstEmprestitosMovimientosComprobante.add(itemEmprestitoOK);
				} else {
					HashMap<String,String> valores = new HashMap<String,String>();
					valores.put("FolioEmprestito", itemEmprestitoOK.FolioEmprestito.toString());
					out.P_OUT_MENSAJES.add(new MensajeSalida(itemEmprestitoOK.FolioEmprestito, MensajeSalida.MENSAJE_ADVERTENCIA, 
							"PRESTAMISTA_NO_GENERA_MOVIMIENTO_FACTURACION", valores, con, entrada.p_IdEmpresa));
				}
			}
			
			if (lstEmprestitosMovimientosComprobante.size() > 0) {
				out.P_OUT_LOGEJECUCION = new ArrayList<LogEjecucion>();
				logEjecucion(out.P_OUT_LOGEJECUCION, "Se invoca ejecucion Facturacion (Emprestitos)", "N", "N");

				CrearMovimientoComprobante cmpPres = new CrearMovimientoComprobante();
				CrearMovimientoComprobante.CrearMovimientoComprobante_Entrada movtosFacturarEmprestitos = prepararDatosFacturacion(con, "N", lstEmprestitosMovimientosComprobante, entrada, fechaVencimiento, IdMonedaSistema, entrada.p_OrigenLiquidacion);
				log.write("Accion=" + entrada.p_OrigenLiquidacion + "; Tipo=Prestamos (prestamistas) [FACTURACION] CrearMovimientoComprobante => Cantidad Registros(emprestitos)=" + movtosFacturarEmprestitos.p_Movimientos.size());
				
				CrearMovimientoComprobante.CrearMovimientoComprobante_Salida resulCrearMovFact = cmpPres.logica(con, movtosFacturarEmprestitos);

				if (resulCrearMovFact.p_out_Movimientos.size() == 0){
					out.P_OUT_ADVERTENCIASCONTADOR = out.P_OUT_ERRORESCONTADOR;
					out.P_OUT_MENSAJES.addAll(resulCrearMovFact.P_OUT_MENSAJES);
				}
			}
			
			for (ObjRegistroEmprestitoDetalle reg : lstEmprestitosOK){
				HashMap<String,String> valores = new HashMap<String,String>();
				valores.put("FolioEmprestito", reg.FolioEmprestito.toString());
				out.P_OUT_MENSAJES.add(new MensajeSalida(reg.FolioEmprestito, MensajeSalida.MENSAJE_INFORMACION,
						(entrada.p_OrigenLiquidacion.equalsIgnoreCase("VENCIMIENTO") ? "PRESTAMISTA_VENCIDO_EXITOSAMENTE" : "PRESTAMISTA_ANTICIPADO_EXITOSAMENTE"),
						valores, con, entrada.p_IdEmpresa));
			}
			
			// notificar vencimientos
			if (salidaLiquidarCustodiaEmprestito.P_OUT_EMPRESTITOS_OK.size() > 0) {
				NotificarVencimientoInstrumentos notificar = new NotificarVencimientoInstrumentos();
				EntradaNotificarVencimientoInstrumentos entradaNotificar = new NotificarVencimientoInstrumentos.EntradaNotificarVencimientoInstrumentos();
				entradaNotificar.vencimientos = new ArrayList<Long>();
				entradaNotificar.p_IdEmpresa= entrada.p_IdEmpresa;
				entradaNotificar.p_IdModulo= entrada.p_IdModulo;
				entradaNotificar.p_IdUsuario = entrada.p_IdUsuario;
				entradaNotificar.vencimientos.addAll(salidaLiquidarCustodiaEmprestito.P_OUT_EMPRESTITOS_OK);
				if (entradaNotificar.vencimientos != null && !entradaNotificar.vencimientos.isEmpty()) {
					notificar.logica(con, entradaNotificar);
				}
			}
			
		}
		
		/*---------------
		  INICIO NETEO	
		---------------*/
		List<ClienteNeteo> clientesANetear = new ArrayList<>();
		for (ObjRegistroEmprestitoDetalle reg : lstEmprestitosOK) {			
			ClienteNeteo c = new ClienteNeteo();
			c.IdInstrumento = reg.IdInstrumentoSubyacente;
			c.IdPortafolio = reg.IdPortafolio;
			clientesANetear.add(c);
		}
		
		if(clientesANetear.size() > 0) {	
			String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
			EntradaNeteo eee = new EntradaNeteo();
			eee.p_IdEmpresa = entrada.p_IdEmpresa;
			eee.p_IdModulo = "CUS";
			eee.p_IdUsuario = entrada.p_IdUsuario;
			eee.p_PortafoliosInstrumento = clientesANetear;
			eee.p_SimularOperacion = "N";
			eee.p_UsaReglasNegocio = "S";
			
			AgregarPrograma.InAgregarPrograma paramsIn = new AgregarPrograma.InAgregarPrograma(); 
			paramsIn.p_IdEmpresa		= entrada.p_IdEmpresa;
			paramsIn.p_IdModulo			= "CUS";
			paramsIn.p_IdUsuario		= entrada.p_IdUsuario;
			paramsIn.p_IdTipoPrograma	= "NeteoCarterasNegativas";
			paramsIn.p_Segundos			= 0L;
			paramsIn.p_FechaSistema		= fechaSistema;
			List<Parametro> paramsParametros = new ArrayList<>();
			Parametro ppp = new Parametro();
			ppp.NombreParametro = "Entrada";
			Gson gson = Utils.getGsonBuilder();
			ppp.Valor = "";
			ppp.ValorBlob = gson.toJson(eee);
			paramsParametros.add(ppp );
			paramsIn.p_Parametros		= paramsParametros ;
			AgregarPrograma AP = new AgregarPrograma();
			AP.logica(con, paramsIn);
		}
		/*---------------
		  FIN NETEO	
		---------------*/
				
		log.write("FIN::EjecutarVencimiento/Anticipo/Anulacion");
		return out;
	}

	//------------------------
	//-- metodos del programa
	//------------------------

	public void logEjecucion(List<LogEjecucion> logej, String mensaje, String esError, String esInterno) {
		log.write("Tipo=EMP => logEjecucion[esError=" + esError + "]: " + mensaje);
		LogEjecucion aux = new LogEjecucion();
		aux.TimeMillis = System.currentTimeMillis();
		aux.Mensaje = mensaje;
		aux.EsError = esError;
		aux.EsInterno = esInterno;
		logej.add(aux);
	}
	
	private Long consultarPlazoCobrado (Connection con, Entrada entrada, ObjRegistroEmprestitoDetalle item) throws SQLException {
		SQL sql = new SQL();
		
		HashMap<String,Object>  paramsIn = new HashMap<String,Object>();
		paramsIn.put("p_IdEmpresa", entrada.p_IdEmpresa);
		paramsIn.put("p_IdUsuario", entrada.p_IdUsuario);
		paramsIn.put("p_IdModulo", entrada.p_IdModulo);
		
		paramsIn.put("p_FechaDesde", item.FechaLiquidacion);
		paramsIn.put("p_FechaHasta", entrada.p_OrigenLiquidacion.equalsIgnoreCase("ANTICIPO") ? entrada.p_Fecha : item.FechaVencimientoEmprestito);
		paramsIn.put("p_DiasHabiles", "N");
		paramsIn.put("p_IdCondicionDeLiquidacion", "T0");
		paramsIn.put("p_IdPais", entrada.p_IdPais);
		
		HashMap<String,Class>  paramsOut = new HashMap<String,Class>();
		paramsOut.put("p_out_PlazoDias", Long.class);
		paramsOut.put("p_out_AdvertenciasContador", Long.class);
		paramsOut.put("p_out_AdvertenciasDetalle", ObjADVERTENCIASDETALLES.class);
		paramsOut.put("p_out_ErroresContador", Long.class);
		paramsOut.put("p_out_ErroresDetalle", ObjERRORESDETALLES.class);
		
		HashMap<String,Object> resultSP = sql.call(con, SQL_CONSULTAR_PLAZO_COBRADOS_Call, paramsIn, paramsOut);
		Long diasCobrados = (Long) resultSP.get("p_out_PlazoDias");
		return diasCobrados;
	}

	public CrearMovimientoComprobante.CrearMovimientoComprobante_Entrada prepararDatosFacturacion(Connection con, String simular,
			List<ObjRegistroEmprestitoDetalle> lCV, Entrada entrada, String fechaSistemaAnterior, String IdMonedaSistema, String OrigenLiquidacion) throws SQLException{
		
		CrearMovimientoComprobante.CrearMovimientoComprobante_Entrada incmc = new CrearMovimientoComprobante.CrearMovimientoComprobante_Entrada();
		incmc.p_IdEmpresa = entrada.p_IdEmpresa;
		incmc.p_IdUsuario = entrada.p_IdUsuario;
		incmc.p_IdModulo = entrada.p_IdModulo;
		incmc.p_simularOperacion = simular;
		incmc.p_UsaReglasNegocio = "S";
		incmc.p_EsIngresoAutomatico = "S";
		incmc.p_EsPauta= entrada.p_EsPauta;
		List<CrearMovimientoComprobante.MovimientoComprobante_Entrada> p_InMovimientoComprobante = new ArrayList<CrearMovimientoComprobante.MovimientoComprobante_Entrada>();
		
		for(ObjRegistroEmprestitoDetalle item : lCV) {
			Long diasCobrados = consultarPlazoCobrado (con, entrada, item);
			
			CrearMovimientoComprobante.MovimientoComprobante_Entrada comprobante = new CrearMovimientoComprobante.MovimientoComprobante_Entrada();
			comprobante.ActualizarCustodia = "N";
			comprobante.ActualizarCuentaInversion = "S";//item.OperaContraCuentaInversionPrestamista;
			comprobante.ActualizarTesoreria = (item.OperaContraCuentaInversionPrestamista.equalsIgnoreCase("S") ? "N" : "S");
			comprobante.ActualizarCustodiaRelacionado = "N";
			comprobante.Cantidad = item.INGCantidad;
			comprobante.CrearComprobanteAutomatico = "N";//"S";
			comprobante.DiasPlazo = new BigDecimal(0);
			comprobante.EsAPlazo = "N";
			comprobante.EsComprobIndividual = "N";
			comprobante.EsCuentaPropia = "N";
			comprobante.EsEnRueda = "S";
			comprobante.EsFungible = "N";
			comprobante.EsRuteo = "N";
			comprobante.EsVentaCorta = "N";
			comprobante.FechaEmision = fechaSistemaAnterior;
			comprobante.FechaLiquidacion = fechaSistemaAnterior;
			comprobante.FechaMovimientoComprobante = fechaSistemaAnterior;
			comprobante.FechaOrden = "0001-01-01";
			comprobante.FechaParidad = "0001-01-01";
			comprobante.FechaTipoCambio = "0001-01-01";
			comprobante.FechaTransaccion = "0001-01-01";
			comprobante.FechaVencimiento = fechaSistemaAnterior;
			comprobante.FolioCartera = item.FolioEmprestito;
			comprobante.FolioTransaccion = "0";
			comprobante.IdAsignacion = new Long(0);
			comprobante.IdAsignacionRef = new Long(0);
			comprobante.IdBolsa = item.IdBolsa;
			comprobante.IdBoveda = "";
			comprobante.IdCliente = item.IdCliente;
			comprobante.IdConcepto = (OrigenLiquidacion.equalsIgnoreCase("VENCIMIENTO") ? "VENEMP" : "ANTEMP");
			comprobante.IdCondicionLiquidacion = "T0";
			comprobante.IdCorredoraContraparte = new Long(0);
			comprobante.IdEmisor = "";
			comprobante.IdEmpleadoDigitador = new Long(0);                          
			comprobante.IdEmpresa = entrada.p_IdEmpresa;
			comprobante.IdFormaCustodia = "";
			comprobante.IdInstrumento = item.IdInstrumentoEmprestito;
			comprobante.IdInstrumentoRelacionado = new Long(0);
			comprobante.IdInstrumentoSubyacente = item.IdInstrumentoSubyacente;
			comprobante.IdMoneda = item.IdMonedaSubyacente;
			comprobante.IdMonedaEmision = item.IdMonedaSubyacente;
			comprobante.IdMonedaLiquidacion = IdMonedaSistema;
			comprobante.IdMonedaSubyacente = item.IdMonedaSubyacente;
			comprobante.IndicadorOrdenDirecta = "";
			comprobante.IdOrden = new Long(0);
			comprobante.IdPortafolio = item.IdPortafolio;
			comprobante.IdTipoComprobante = "FEE";
			comprobante.IdTipoOperacion = (OrigenLiquidacion.equalsIgnoreCase("VENCIMIENTO") ? "VENEMP" : "ANTEMP");
			comprobante.IdTipoProducto = item.IdTipoProducto;
			comprobante.IdTipoReajuste = "CLP";
			comprobante.IdTipoConvenio = "SCV";
			comprobante.IdTipoCanalIngreso = "SIS";
			comprobante.IdTransaccion = new Long(0);
			comprobante.IdUsuario = new Long(0);
			comprobante.ModPrefFact = "";
			comprobante.MontoDescuento = new BigDecimal(0);
			comprobante.MontoInteres = new BigDecimal(0);
			comprobante.MontoAPlazo = item.INGMontoTotalEmprestito;
			comprobante.NumeroMvtoCmbte = item.FolioEmprestito;	//CORRELATIVO
			comprobante.NemotecnicoInstrumento = item.NemoSubyacente;
			comprobante.Precio = item.PrecioInstrumento.setScale(2, RoundingMode.DOWN);
			comprobante.PrecioAPlazo = item.PrecioInstrumento.setScale(2, RoundingMode.DOWN);
			comprobante.ReajustesVAC = BigDecimal.ZERO;
			comprobante.SecuenciaAgrupacion = "";
			comprobante.Spread = new BigDecimal(0);
			comprobante.Tasa = item.INGTasaInstrumento; //TIR
			comprobante.TasaAgente = new BigDecimal(0);
			comprobante.TasaEmision = new BigDecimal(0);
			comprobante.TasaOperacion = item.INGTasaEmprestito;
			comprobante.ValorRescate = item.INGValorRescate;
			comprobante.ValorParidad = new BigDecimal(0);
			comprobante.ValorTipoCambio = new BigDecimal(0);
			comprobante.ValorCaptacionMonedaOrigen = new BigDecimal(0);
			comprobante.Monto = item.INGMontoPremio;
			comprobante.MontoMovimientoComprobante = item.INGMontoPremio;
			comprobante.MontoLiquidacion = item.INGMontoPremio;
			comprobante.EsPrecioPrima = "S";
			
			comprobante.Glosa = OrigenLiquidacion + " PRESTAMISTA " + item.FolioEmprestito + 
					", Instrumento: "+ item.NemoSubyacente + 
					", Cantidad: " + item.INGCantidad.setScale(0, RoundingMode.HALF_UP)  + 
					", Tasa Premio: " + item.INGTasaEmprestito.setScale(4, RoundingMode.HALF_UP) + 
					", Precio: " + item.PrecioInstrumento.setScale(4, RoundingMode.HALF_UP) + 
					", Plazo (dias): " + diasCobrados +
					", Cantidad Premio: " + item.INGMontoPremio.setScale(2, RoundingMode.HALF_UP);
			
			p_InMovimientoComprobante.add(comprobante);
		}
		incmc.p_Movimientos = p_InMovimientoComprobante;
		
		return incmc;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = Utils.getGsonBuilder();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":17,\"p_IdModulo\":\"CUS\",\"p_SimularOperacion\":\"N\",\"p_IdTipoVencimiento\":\"PRT\",\"p_Fecha\":\"2018-09-28\",\"p_IdPais\":44}";
			
			Entrada Objin = gson.fromJson(json, Entrada.class);

			System.out.println("ENTRADA");
			System.out.println(Objin.toString());
			System.out.println("SALIDA");

			VencerEmprestitos ven = new VencerEmprestitos();
			try {
				Salida resp = ven.logica(con, Objin);
				//con.rollback();
				con.commit();
				con.close();
				System.out.println(resp);
			} catch (Exception e) {
				e.printStackTrace();
				con.rollback();
			}
			
			System.out.println("== END ==");
		}
	}
}
