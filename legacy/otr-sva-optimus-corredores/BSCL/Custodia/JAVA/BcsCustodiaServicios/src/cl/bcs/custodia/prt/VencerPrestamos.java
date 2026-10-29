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
import cl.bcs.custodia.prt.VencerEmprestitos.Entrada;
import cl.bcs.custodia.prt.VencerEmprestitos.ObjADVERTENCIASDETALLES;
import cl.bcs.custodia.prt.VencerEmprestitos.ObjERRORESDETALLES;
import cl.bcs.facturacion.CrearMovimientoComprobante;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.bck.AnticiparPrestamoBlockChain.FolioPrestamoBlockChain;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class VencerPrestamos implements JavaCall {

	private final Log log = new Log("CUS.VencerPrestamos");
	public final String SI = "S";
	
	private final String SQL_CONSULTAR_PLAZO_COBRADOS_Call = loadResourceToString("cl/bcs/custodia/sql/prt/CALL_ConsultarPlazoCobrado.sql");

	public static class Entrada {
		public Long 						p_IdEmpresa;
		public Long 						p_IdUsuario;
		public String 						p_IdModulo;
		public String 						p_Fecha;
		public String 						p_OrigenLiquidacion; //ANTICIPO - VENCIMIENTO   
		public Long 						p_IdPais;
		public List<ObjRegistroPrestamo>	p_lstPrestamos;
		public String						p_VieneDesdeBlockChain = "N"; 	// si esto esta en S
																			// significa que debe
																			// saltarse validar en
																			// BlockChain e ir a Anticipar/Vencer en BlockChain


		public String						p_VieneDesdeVencerEmprestito = "N"; 	// Si ya anticipo emprestito no debe volver a anticipar
		public boolean 						p_EsPauta= false;
		
		public Entrada(){
			this.p_OrigenLiquidacion = "VENCIMIENTO"; //por defecto
			this.p_lstPrestamos = new ArrayList<ObjRegistroPrestamo>();
		}
		
		
		public String toString() {
			return Utils.printJson(this, false);
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
		}

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	
	public static class FolioMontoPremio {
		public Long Folio;
		public BigDecimal MontoPremio;
		public BigDecimal Cantidad;
		
		public FolioMontoPremio() {
			this.Cantidad = BigDecimal.ZERO;
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

	
	public static class ObjRegistroEmprestito{
		public Long FolioEmprestito;
	}


	public static class ObjRegistroPrestamo {
		public Long IdCartera;
		public Long IdOperacion;
		public Long FolioCartera;
		public String Nemotecnico;
		public String EsPagaderoEn;
		public BigDecimal INGMontoATasaCompraActual;
		public BigDecimal INGMontoCompraActualMonedaInstrumento;
		public BigDecimal INGMontoAPlazo;
		public String IdClasificacionTipoInstrumento;
		public String IdTipoInstrumento;
		public Long IdInstrumento;
		public Long IdPortafolio;
		public String IdMonedaInstrumento;
		public String IdTipoProducto;
		public String IdTipoOperacion; // "VCDTIT"
		public String OperaContraCuentaInversion;
		public Long IdPersona;
		public Long IdCliente;
		public java.sql.Date FechaOperacion;
		public String IdConcepto;
		public String IdTipoCanalIngreso;
		public Long IdUsuario;
		public Long IdSucursal;
		public Long IdAgente;
		public String Glosa;
		public String IdBolsa;
		public BigDecimal Cantidad;
		public BigDecimal CorteMinimo;
		public BigDecimal MontoCompraMonedaInstrumento;
		//
		public BigDecimal PrecioAPlazo;
		public BigDecimal MontoAPlazo;
		public java.sql.Date FechaComprobante;
		public String IdMonedaSistema; // se usa como IdMonedaOperacion
		public String IdCondicionLiquidacion; // se esta usando T0
		public BigDecimal INGCantidad; // usado como Cantidad?
		public BigDecimal INGValorRescate;
		public BigDecimal ValorRescate;
		public BigDecimal INGTasaCompra; // es la tasa
		public BigDecimal PrecioCompra;
		public BigDecimal INGPrecioCompra;
		public String AccionOperacion; // lo usa la simultanea
		public Long IdInstrumentoSubyacente;
		public Long FolioSubyacente;
		public Long FolioPrestamo; // para que el emprestito lo referencie
		public String FechaLiquidacion;
		public Long FolioComprobante;
		public String IdTipoComprobante;
		public String CobraPrimaAlInicio;
		public String TablaAfecta;
		public String IdTipoComprobanteBusqueda;
		public Long FolioComprobanteBusqueda;
		public java.sql.Date FechaLiquidacionDate;
		public BigDecimal INGMontoPrima;     // Monto prima de Prestamos (se usa para enviar a facturar prestamos) 
		public BigDecimal INGMontoPremio;    // Monto premio de Emprestitos (se usa para enviar a facturar emprestitos)                    
		public String IdEmisor;
		//RF
		public String TieneCorteDeCupon;
		// MSH
		public String IdTipoReajuste;
		public String IdMonedaEmision;
		
		public BigDecimal 	CantidadAsignada;
		public BigDecimal	CantidadPorAsignar;
		public String		IdTipoPrestamo;
		public String		PrestamoAsignadoTotal = "S";
		
		public String FolioSistemaExterno = ""; //Valor que tiene asociado un prestamo (FolioBlockChain) cuando esta registrado en el sistema externo llamado BlockChain
		public String EsAnticipoParcial = "N";
		public java.sql.Date FechaVencimientoPrestamo;
		public BigDecimal INGPrecioInstrumento;
		public BigDecimal INGTasaInstrumento;
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
		log.write("INICIO::EjecutarVencimiento/Anticipo");
		Salida out = new Salida();
		out.P_OUT_LOGEJECUCION = new ArrayList<LogEjecucion>();

		log.write("Inicia ejecucion de " + entrada.p_OrigenLiquidacion + " Prestamos con datos de entrada: " + entrada);
		
		String usaFechaLiquidacionEnVencimiento = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "CUS.UsaFechaLiquidacionEnVencimiento");
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		out.P_OUT_ERRORESCONTADOR = 0L;
		out.P_OUT_ADVERTENCIASCONTADOR = 0L;
		List<ObjRegistroPrestamo> lstPrestamos = new ArrayList<ObjRegistroPrestamo>();
		lstPrestamos = entrada.p_lstPrestamos;
		
		List<ObjRegistroPrestamo> lsPrestamosOK = new ArrayList<ObjRegistroPrestamo>();
		List<ObjRegistroPrestamo> lsPrestamosMovimientosComprobante = new ArrayList<ObjRegistroPrestamo>();
		List<FolioPrestamoBlockChain> lstPrestamosBC = new ArrayList<>();
		
		//servicio NO esta siendo llamado desde lectura de LOG BlockChain
		if(!entrada.p_VieneDesdeBlockChain.equalsIgnoreCase("N"))
		{
			for (ObjRegistroPrestamo item : lstPrestamos) {
				//existe Folio BlockChain
				if(!item.FolioSistemaExterno.equals("")) {
					FolioPrestamoBlockChain objFolioBC = new FolioPrestamoBlockChain();
					objFolioBC.FolioPrestamoBlockChain = item.FolioSistemaExterno;
					lstPrestamosBC.add(objFolioBC); 
				}
			}
		}

		//puede seguir con el proceso de anticipo
		if(out.P_OUT_ERRORESCONTADOR==0L) {
			//----------------------------------------------------------------\\
			//---------------  llamar a vencer los emprestitos ---------------\\
			//----------------------------------------------------------------\\
			//Si el flujo no proviene del vencimiento de emprestitos, estos se vencen
			if (entrada.p_VieneDesdeVencerEmprestito.equalsIgnoreCase("N") && !entrada.p_OrigenLiquidacion.equalsIgnoreCase("ANULACION"))
			{
					List<FolioMontoPremio> lstPrestamosEmprestitos = new ArrayList<FolioMontoPremio>();
					
					for (ObjRegistroPrestamo item : lstPrestamos) {
						FolioMontoPremio fmp = new FolioMontoPremio();
						fmp.Folio = item.FolioCartera;
						fmp.MontoPremio = item.INGMontoPremio;
						fmp.Cantidad = "S".equals(item.EsAnticipoParcial) ? item.Cantidad : BigDecimal.ZERO;
						lstPrestamosEmprestitos.add(fmp);
					}
					
			
					VencerEmprestitos.Entrada prmVencerEmprestitos = new VencerEmprestitos.Entrada();
					prmVencerEmprestitos.p_IdEmpresa = entrada.p_IdEmpresa;
					prmVencerEmprestitos.p_IdUsuario = entrada.p_IdUsuario;
					prmVencerEmprestitos.p_IdModulo = entrada.p_IdModulo;
					prmVencerEmprestitos.p_IdPais = entrada.p_IdPais;
					prmVencerEmprestitos.p_Fecha = entrada.p_Fecha;
					prmVencerEmprestitos.p_OrigenLiquidacion = entrada.p_OrigenLiquidacion;
					prmVencerEmprestitos.p_lstPrestamos = lstPrestamosEmprestitos;
					prmVencerEmprestitos.p_EsPauta= entrada.p_EsPauta;
					
					VencerEmprestitos objVencerEmprestitos = new VencerEmprestitos();
					VencerEmprestitos.Salida resulVencimientosEmprestitos = objVencerEmprestitos.logica(con, prmVencerEmprestitos);
					if (resulVencimientosEmprestitos.P_OUT_ERRORESCONTADOR > 0){
						out.P_OUT_MENSAJES.addAll(resulVencimientosEmprestitos.P_OUT_MENSAJES);
						return out;
					}
			}
			
			// -------------------------------------\\
			// ----- Liquida Custodia Prestamos ----\\
			// -------------------------------------\\
			log.write("Accion=" + entrada.p_OrigenLiquidacion + "; Tipo=PRT [CUSTODIA] Inicio Ejecucion LiquidarCustodiaPrestamos");
			logEjecucion(out.P_OUT_LOGEJECUCION, "Se invoca Custodia Liquidar Prestamos", "N", "N");
			
			LiquidarCustodiaPrestamo servicioLiquidarCustodiaPrestamo = new LiquidarCustodiaPrestamo();
			LiquidarCustodiaPrestamo.LiquidarCustodiaPrestamo_Entrada entradaLiquidarCustodiaPrestamo = new LiquidarCustodiaPrestamo.LiquidarCustodiaPrestamo_Entrada();
			LiquidarCustodiaPrestamo.LiquidarCustodiaPrestamo_Salida salidaLiquidarCustodiaPrestamo = new LiquidarCustodiaPrestamo.LiquidarCustodiaPrestamo_Salida();
			
			entradaLiquidarCustodiaPrestamo.p_IdEmpresa 			= entrada.p_IdEmpresa;
			entradaLiquidarCustodiaPrestamo.p_IdUsuario 			= entrada.p_IdUsuario;
			entradaLiquidarCustodiaPrestamo.p_IdModulo 				= entrada.p_IdModulo;
			entradaLiquidarCustodiaPrestamo.p_SimularOperacion 		= "N";				
			entradaLiquidarCustodiaPrestamo.p_OrigenLiquidacion 	= entrada.p_OrigenLiquidacion;
			
			
			for (ObjRegistroPrestamo car : lstPrestamos) {
				LiquidarCustodiaPrestamo.PrestamoVencer_Entrada entradaPrestamoVencer = new LiquidarCustodiaPrestamo.PrestamoVencer_Entrada();
				Utils.copyPojo(car, entradaPrestamoVencer);
				entradaPrestamoVencer.FechaVencimientoAnticipo = entrada.p_Fecha;
				entradaLiquidarCustodiaPrestamo.p_PrestamosVencer.add(entradaPrestamoVencer);
			}
			
			salidaLiquidarCustodiaPrestamo = servicioLiquidarCustodiaPrestamo.logica(con, entradaLiquidarCustodiaPrestamo);
			
			out.P_OUT_ERRORESCONTADOR += salidaLiquidarCustodiaPrestamo.P_OUT_ERRORESCONTADOR;
			
			for (MensajeSalida mens : salidaLiquidarCustodiaPrestamo.P_OUT_MENSAJES){
				if (mens.tipo.equalsIgnoreCase(MensajeSalida.MENSAJE_ERROR)) out.P_OUT_MENSAJES.add(mens);
			}
			
			if (salidaLiquidarCustodiaPrestamo.P_OUT_ERRORESCONTADOR > 0) return out;
			
			for (ObjRegistroPrestamo reg : lstPrestamos){
				for (Long prtOK : salidaLiquidarCustodiaPrestamo.P_OUT_PRESTAMOS_OK){
					if (reg.FolioCartera.equals(prtOK)){
						lsPrestamosOK.add(reg);
						break;
					}
				}
			}

			
			//----------------------------------------------------------------\\
			//---- Genera los movimientos a factuar de prestamos vencidos ----\\
			//----------------------------------------------------------------\\
			if (lsPrestamosOK.size()>0 && !entrada.p_OrigenLiquidacion.equalsIgnoreCase("ANULACION")) {
				
				for (ObjRegistroPrestamo itemPrestamoOK : lsPrestamosOK) {
					if (!usaFechaLiquidacionEnVencimiento.equalsIgnoreCase(SI) || (!entrada.p_OrigenLiquidacion.equalsIgnoreCase("ANTICIPO")) ||
							(entrada.p_OrigenLiquidacion.equalsIgnoreCase("ANTICIPO") && usaFechaLiquidacionEnVencimiento.equalsIgnoreCase(SI) && itemPrestamoOK.FechaLiquidacionDate != null && itemPrestamoOK.FechaLiquidacionDate.before(dateFormat.parse(entrada.p_Fecha)))) {
						lsPrestamosMovimientosComprobante.add(itemPrestamoOK);
					} else {
						HashMap<String,String> valores = new HashMap<String,String>();
						valores.put("FolioPrestamo", itemPrestamoOK.FolioCartera.toString());
						out.P_OUT_MENSAJES.add(new MensajeSalida(itemPrestamoOK.FolioCartera, MensajeSalida.MENSAJE_ADVERTENCIA, 
								"PRESTAMO_NO_GENERA_MOVIMIENTO_FACTURACION", valores, con, entrada.p_IdEmpresa));
					}
				}
				
				if (lsPrestamosMovimientosComprobante.size() > 0) {
					logEjecucion(out.P_OUT_LOGEJECUCION, "Se invoca ejecucion Facturacion (Prestamos)", "N", "N");
					
					CrearMovimientoComprobante cmpPres = new CrearMovimientoComprobante();
					CrearMovimientoComprobante.CrearMovimientoComprobante_Entrada movtosFacturarPrestamos = prepararDatosFacturacion(con, "N", lsPrestamosOK, entrada, entrada.p_Fecha, entrada.p_OrigenLiquidacion);
					log.write("Accion=" + entrada.p_OrigenLiquidacion + "; Tipo=Prestamos [FACTURACION] CrearMovimientoComprobante => Cantidad Registros(prestamos)=" + movtosFacturarPrestamos.p_Movimientos.size());
		
					CrearMovimientoComprobante.CrearMovimientoComprobante_Salida resulCrearMovCompro = cmpPres.logica(con, movtosFacturarPrestamos);
		
					if (resulCrearMovCompro.p_out_Movimientos.size() == 0) {
						out.P_OUT_ADVERTENCIASCONTADOR += out.P_OUT_ERRORESCONTADOR;
						out.P_OUT_MENSAJES.addAll(resulCrearMovCompro.P_OUT_MENSAJES);
					}
				}
				
				
				for (ObjRegistroPrestamo reg : lsPrestamosOK) {
					HashMap<String,String> valores = new HashMap<String,String>();
					valores.put("FolioPrestamo", reg.FolioCartera.toString());
					out.P_OUT_MENSAJES.add(new MensajeSalida(reg.FolioCartera, MensajeSalida.MENSAJE_INFORMACION,
								(entrada.p_OrigenLiquidacion.equalsIgnoreCase("VENCIMIENTO") ? "PRESTAMO_VENCIDO_EXITOSAMENTE" : "PRESTAMO_ANTICIPADO_EXITOSAMENTE"),
								valores, con, entrada.p_IdEmpresa));
				}			
			}

			// notificar vencimientos
			if (salidaLiquidarCustodiaPrestamo.P_OUT_PRESTAMOS_OK.size()>0 && entrada.p_OrigenLiquidacion.equalsIgnoreCase("VENCIMIENTO")) {
				NotificarVencimientoInstrumentos notificar = new NotificarVencimientoInstrumentos();
				EntradaNotificarVencimientoInstrumentos entradaNotificar = new NotificarVencimientoInstrumentos.EntradaNotificarVencimientoInstrumentos();
				entradaNotificar.p_IdEmpresa= entrada.p_IdEmpresa;
				entradaNotificar.p_IdModulo= entrada.p_IdModulo;
				entradaNotificar.p_IdUsuario = entrada.p_IdUsuario;
				entradaNotificar.vencimientos = new ArrayList<Long>();
				entradaNotificar.vencimientos.addAll(salidaLiquidarCustodiaPrestamo.P_OUT_PRESTAMOS_OK);
				if (entradaNotificar.vencimientos != null && !entradaNotificar.vencimientos.isEmpty()) {
					notificar.logica(con, entradaNotificar);
				}
			}
			
		}//Fin out.P_OUT_ERRORESCONTADOR > 0
		log.write("FIN::EjecutarVencimiento");
		return out;
	}

	/**********************
	 * metodos del programa
	 **********************/

	public void logEjecucion(List<LogEjecucion> logej, String mensaje, String esError, String esInterno) {
		log.write("TipoVencimiento=PRT => logEjecucion[esError=" + esError + "]: " + mensaje);
		LogEjecucion aux = new LogEjecucion();
		aux.TimeMillis = System.currentTimeMillis();
		aux.Mensaje = mensaje;
		aux.EsError = esError;
		aux.EsInterno = esInterno;
		logej.add(aux);
	}

	private Long consultarPlazoCobrado (Connection con, Entrada entrada, ObjRegistroPrestamo item) throws SQLException {
		SQL sql = new SQL();
		
		HashMap<String,Object>  paramsIn = new HashMap<String,Object>();
		paramsIn.put("p_IdEmpresa", entrada.p_IdEmpresa);
		paramsIn.put("p_IdUsuario", entrada.p_IdUsuario);
		paramsIn.put("p_IdModulo", entrada.p_IdModulo);
		
		paramsIn.put("p_FechaDesde", item.FechaLiquidacionDate);
		paramsIn.put("p_FechaHasta", entrada.p_OrigenLiquidacion.equalsIgnoreCase("ANTICIPO") ? entrada.p_Fecha : item.FechaVencimientoPrestamo);
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
			List<ObjRegistroPrestamo> lCV, Entrada entrada, String fechaSistemaAnterior, String OrigenLiquidacion) throws SQLException{
		
		CrearMovimientoComprobante.CrearMovimientoComprobante_Entrada incmc = new CrearMovimientoComprobante.CrearMovimientoComprobante_Entrada();
		incmc.p_IdEmpresa = entrada.p_IdEmpresa;
		incmc.p_IdUsuario = entrada.p_IdUsuario;
		incmc.p_IdModulo = entrada.p_IdModulo;
		incmc.p_simularOperacion = simular;
		incmc.p_UsaReglasNegocio = "S";
		incmc.p_EsIngresoAutomatico = "S";
		incmc.p_EsPauta = entrada.p_EsPauta;
		List<CrearMovimientoComprobante.MovimientoComprobante_Entrada> p_InMovimientoComprobante = new ArrayList<CrearMovimientoComprobante.MovimientoComprobante_Entrada>();
		
		for(ObjRegistroPrestamo item : lCV) {
			Long diasCobrados = consultarPlazoCobrado (con, entrada, item);
			
			CrearMovimientoComprobante.MovimientoComprobante_Entrada comprobante = new CrearMovimientoComprobante.MovimientoComprobante_Entrada();
			comprobante.ActualizarCustodia = "N";
			comprobante.ActualizarCuentaInversion ="S";//item.OperaContraCuentaInversion;
			comprobante.ActualizarTesoreria = (item.OperaContraCuentaInversion.equalsIgnoreCase("S") ? "N" : "S");
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
			comprobante.FolioCartera = item.FolioCartera;
			comprobante.FolioTransaccion = "0";
			comprobante.IdAsignacion = new Long(0);
			comprobante.IdAsignacionRef = new Long(0);
			comprobante.IdBolsa = item.IdBolsa;
			comprobante.IdBoveda = "";
			comprobante.IdCliente = item.IdCliente;
			comprobante.IdConcepto =  (OrigenLiquidacion.equalsIgnoreCase("VENCIMIENTO") ? "VENPRT" : "ANTPRT");//item.IdConcepto;
			comprobante.IdCondicionLiquidacion = "T0";
			comprobante.IdCorredoraContraparte = new Long(0);
			comprobante.IdEmisor = "";
			comprobante.IdEmpleadoDigitador = new Long(0);                          
			comprobante.IdEmpresa = entrada.p_IdEmpresa;
			comprobante.IdFormaCustodia = "";
			comprobante.IdInstrumento = item.IdInstrumento;
			comprobante.IdInstrumentoRelacionado = new Long(0);
			comprobante.IdInstrumentoSubyacente = item.IdInstrumentoSubyacente;
			comprobante.IdMoneda = item.IdMonedaInstrumento;		//IdMonedaInstrumento
			comprobante.IdMonedaEmision = item.IdMonedaInstrumento;
			comprobante.IdMonedaLiquidacion = item.IdMonedaSistema;
			comprobante.IdMonedaSubyacente = item.IdMonedaEmision;
			comprobante.IndicadorOrdenDirecta = "";
			comprobante.IdOrden = new Long(0);
			comprobante.IdPortafolio = item.IdPortafolio;
			comprobante.IdTipoComprobante = "FEE";//item.IdTipoComprobante;
			comprobante.IdTipoOperacion = (OrigenLiquidacion.equalsIgnoreCase("VENCIMIENTO") ? "VENPRT" : "ANTPRT");//item.IdTipoOperacion;
			comprobante.IdTipoProducto = item.IdTipoProducto;
			comprobante.IdTipoReajuste = "CLP";
			comprobante.IdTipoConvenio = "SCV";
			comprobante.IdTipoCanalIngreso = "SIS";
			comprobante.IdTransaccion = new Long(0);
			comprobante.IdUsuario = new Long(0);
			comprobante.ModPrefFact = "";
			comprobante.MontoDescuento = new BigDecimal(0);
			comprobante.MontoInteres = new BigDecimal(0);
			comprobante.MontoAPlazo = item.MontoAPlazo;
			comprobante.NumeroMvtoCmbte = item.FolioCartera;	//CORRELATIVO
			comprobante.NemotecnicoInstrumento = item.Nemotecnico;
			comprobante.Precio = item.INGPrecioInstrumento.setScale(2, RoundingMode.DOWN);
			comprobante.PrecioAPlazo = item.INGPrecioInstrumento.setScale(2, RoundingMode.DOWN);
			comprobante.ReajustesVAC = BigDecimal.ZERO;
			comprobante.SecuenciaAgrupacion = "";
			comprobante.Spread = new BigDecimal(0);
			comprobante.Tasa = item.INGTasaInstrumento; //TIR
			comprobante.TasaAgente = new BigDecimal(0);
			comprobante.TasaEmision = new BigDecimal(0);
			comprobante.TasaOperacion = item.INGTasaCompra;
			comprobante.ValorRescate = item.INGValorRescate;
			comprobante.ValorParidad = new BigDecimal(0);
			comprobante.ValorTipoCambio = new BigDecimal(0);
			comprobante.ValorCaptacionMonedaOrigen = new BigDecimal(0);
			comprobante.Monto = item.INGMontoPrima;
			comprobante.MontoMovimientoComprobante = item.INGMontoPrima;
			comprobante.MontoLiquidacion = item.INGMontoPrima;
			comprobante.EsPrecioPrima = "S";
			
			BigDecimal Cantidad = OrigenLiquidacion.equalsIgnoreCase("VENCIMIENTO") ? item.INGCantidad : item.Cantidad;
			
			comprobante.Glosa = OrigenLiquidacion + " PRESTAMO " + item.FolioCartera + 
					", Instrumento: "+ item.Nemotecnico + 
					", Cantidad: " + Cantidad.setScale(0, RoundingMode.HALF_UP)  + 
					", Tasa Prima: " + item.INGTasaCompra.setScale(4, RoundingMode.HALF_UP) + 
					", Precio: " + item.INGPrecioInstrumento.setScale(4, RoundingMode.HALF_UP) + 
					", Plazo (dias): " + diasCobrados +
					", Cantidad Prima: " + item.INGMontoPrima.setScale(2, RoundingMode.HALF_UP);
			
			p_InMovimientoComprobante.add(comprobante);
		}
		incmc.p_Movimientos = p_InMovimientoComprobante;
		
		return incmc;
	}

	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = Utils.getGsonBuilder();
			//String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":17,\"p_IdModulo\":\"CUS\",\"p_SimularOperacion\":\"N\",\"p_IdTipoVencimiento\":\"PRT\",\"p_Fecha\":\"2018-09-28\",\"p_IdPais\":44}";
			String json = "{p_FechaAnticipo: \"2018-10-05\",p_IdEmpresa: 1,p_IdModulo: \"CUS\",p_IdPais: 44,p_IdUsuario: 21,p_UsaPrecioMedio: \"N\",p_UsaReglasNegocio: \"S\",p_DatosPrestamos: [{Cantidad: 2500,DevolverCustodia: \"S\",FechaAnticipo: \"2018-10-05\",FechaLiquidacion: \"2018-10-05\",FolioPrestamo: 20687061,IdCondicionLiquidacion: \"T0\",IdEmpleadoDigitador: 7889,IdMonedaLiquidacion: \"CLP\",IdPais: 44,Monto: 14638250,MontoAPlazo: 14650399.747500002,MontoPrima: 12149.747500002384,PrecioInstrumento: 5855.3,TasaAnticipo: 0.83,p_FechaAnticipo: \"2018-10-05\",p_IdEmpresa: 1,p_IdModulo: \"CUS\"	p_IdPais: 44}]}";
			
			Entrada Objin = gson.fromJson(json, Entrada.class);

			System.out.println("ENTRADA");
			System.out.println(Objin.toString());
			System.out.println("SALIDA");

			VencerPrestamos ven = new VencerPrestamos();
			try {
				Salida resp = ven.logica(con, Objin);
				//con.rollback();
				con.rollback();
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
