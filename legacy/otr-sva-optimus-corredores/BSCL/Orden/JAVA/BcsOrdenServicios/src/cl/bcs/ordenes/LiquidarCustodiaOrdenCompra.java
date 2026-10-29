/**
 * Objetivo: Vencimiento de Operaciones de Compra Simultanea(CL) y Reportes (PE)
 */
package cl.bcs.ordenes;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.custodia.utils.LogCustodia.ObjMovimiento;
import cl.bcs.custodia.utils.GestionMovimientoCarteraEnTransito;
import cl.bcs.custodia.utils.IngresoCustodia;
import cl.bcs.custodia.utils.LogCustodia;
import cl.bcs.custodia.utils.RetiroCustodia;
import cl.bcs.custodia.utils.RetiroCustodia.ObjDatosEntrada;
import cl.bcs.custodia.utils.RetiroCustodia.RetiroCustodiaEntrada;
import cl.bcs.custodia.utils.RetiroCustodia.RetiroCustodiaSalida;
import cl.bcs.custodia.utils.RetiroCustodia.RetiroCustodiaSalida.ObjDatosSalida;
import cl.bcs.plataforma.bat.AgregarPrograma;
import cl.bcs.plataforma.bat.AgregarPrograma.InAgregarPrograma.Parametro;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class LiquidarCustodiaOrdenCompra implements JavaCall{
	private final Log log = new Log("ORD.LiquidarCustodiaOrdenCompra");
	
	private final String SQL_SELECT_SUBYACENTE 	= loadResourceToString("cl/bcs/ordenes/sql/LiquidarCustodiaOrden_ConsultarSubyacente.sql");
	private final String SQL_SELECT_GARANTIA 	= loadResourceToString("cl/bcs/ordenes/sql/LiquidarCustodiaOrden_ConsultarGarantia.sql");
	private final String SQL_SELECT_CARTERA 	= loadResourceToString("cl/bcs/ordenes/sql/LiquidarCustodiaOrden_ConsultarCartera.sql");
	
	
	private final String SQL_SELECT_CARTERA_RELACIONADA = loadResourceToString("cl/bcs/ordenes/sql/LiquidarCustodiaOrden_ConsultarCarteraRelacionada.sql");
	private final String SQL_CONSULTAR_CONTRAPARTE_SIMULTANEA = loadResourceToString("cl/bcs/ordenes/sql/LiquidarCustodiaOrden_ConsultarContraparteSimultanea.sql");
	
	
	// ----------------------------------------------
	// -- CLASES PARA ENTRADA
	// ----------------------------------------------	
	public static class CarteraCompraObj_IN {
		public String 		AccionOperacion;
		public Long 		IdCartera;
		public Long 		FolioCartera;
		public String		IdTipoOperacion;
		public Long			IdOperacion;
		public String		IdConcepto;
		public BigDecimal	Cantidad;
		public BigDecimal	Monto;
		public BigDecimal	Tasa;
		public String		IdCondicionLiquidacion;
		public String		Fecha;
		public Long 		IdInstrumento;		
		public Long 		IdCliente;
		public Long 		IdPortafolio;
		public String		IdEmisor;
		public Long 		FolioComprobante;
		///public BigDecimal	MontoAPlazo; /// falta borrar
		
		
		public String toString() {
			return Utils.printJson(this, false);
		};		
	}
	
	public static class LiquidarCustodiaOrdenCompra_IN {
		public Long				p_IdPais;
		public Long				p_IdEmpresa;
	    public Long				p_IdUsuario;
	    public String			p_IdModulo; 
	    public String			p_SimularOperacion = "N";
	    public String			p_UsaReglasNegocio;
	    public Long				p_TicketAtencion;
	    public String			p_OrigenLiquidacion;
	    public List<CarteraCompraObj_IN>	p_DatosCartera;
	    
	    public LiquidarCustodiaOrdenCompra_IN() {
	    	this.p_DatosCartera = new ArrayList<CarteraCompraObj_IN>();
	    	this.p_OrigenLiquidacion = "";
	    }
	    public String toString() {
			return Utils.printJson(this, false);
		};
		
	}

	
	
	// ----------------------------------------------
	// -- CLASES PARA SALIDA
	// ----------------------------------------------	

	public static class LiquidarCustodiaOrdenCompra_OUT {
		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;

		public List<CarteraObj_OUT>		P_OUT_OPERACION;
		
		public LiquidarCustodiaOrdenCompra_OUT() {
			this.P_OUT_OPERACION 		= new ArrayList<CarteraObj_OUT>();
			this.P_OUT_MENSAJES 		= new ArrayList<MensajeSalida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class CarteraObj_OUT {
		
	}	
	
	// ----------------------------------------------
	// -- CLASES UTILITARIAS
	// ----------------------------------------------	
	public static class Garantia {
		public Long IdGarantia;
		public Long FolioGarantia;
		public Long FolioCarteraOrigen;
		public Long IdCarteraOrigen;
		public BigDecimal INGCantidad;
		public BigDecimal INGValorRescate;
		public BigDecimal INGMontoATasaGarantiaActual;
		public BigDecimal PrecioCompraOperacionOrigen;
		public BigDecimal TasaCompraOperacionOrigen;	
		public String IdTipoOperacionOrigen;    
	    public Long IdCliente; 
	    public Long IdPortafolio; 
	    public Long IdInstrumento;
	    public String IdEmisor;
	    public String IdOrigenGarantia;
	    public String IdConcepto;
	    public String IdMonedaInstrumento;
		public String IdBoveda;
		public String IdTipoCanalIngreso;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	public static class Subyacente {
		public Long IdSubyacente;
	    public Long FolioSubyacente;
	    public Long FolioCarteraRelacionada; 
	    public String IdEstado;
	    public BigDecimal Cantidad;
	    public BigDecimal ValorRescate;	    
	    public Long IdInstrumento;
	    public BigDecimal PrecioCompraOperacionRelacionada;    
	    public BigDecimal TasaCompraOperacionRelacionada;	    
	    public Long IdCarteraOrigen; 
	    public String IdTipoOperacionOrigen;	    
	    
	    public Long IdCliente;
	    public Long IdPortafolio; 
	    public String IdBoveda;	    
	    public String IdMonedaInstrumento;
	    public String IdEmisor;
	    public String IdTipoCanalIngreso;
	    
		
	    public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class DatoGta {
		public String IdTipoOperacion;
		public Long IdOperacion;
	}
	
	
	public static class Cartera {
		public Long  	IdCartera;
		public String 	IdTipoOperacion;		
		public String 	IdBolsa;
		public String 	IdBoveda;
		public String 	IdMonedaInstrumento;		
	    public String 	EsNominal;
	    public Long  	IdCuentaCustodioPortafolio;
	    public BigDecimal INGCantidad;
	    public BigDecimal INGPrecioCompra;
	    public BigDecimal INGTasaCompra;
		public Long PlazoEnDiasOperacion;
		public BigDecimal TasaCompra; 
	    public BigDecimal PrecioCompra;
	    public BigDecimal PrecioAPlazo;
	    
		
		
		public Long 	IdCliente;
		public Long 	IdPortafolio;
		public Long 	FolioCartera;
		public Long 	IdInstrumento;  
		public String 	IdEmisor;
		public String 	IdConcepto;
		public String 	IdCondicionLiquidacion;
		
		
		public Long  	IdSubyacente;
		  
	    public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ContraparteSimultanea {
		public Long IdEmpresa;
		public String Nombre;
		public Long IdPortafolioDefecto;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
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
	
	// ----------------------------------------------
	// -- LOGICA
	// ----------------------------------------------	
	public LiquidarCustodiaOrdenCompra_OUT logica (Connection conn, LiquidarCustodiaOrdenCompra_IN in) throws Exception {
		SQL _sql = new SQL();
		LiquidarCustodiaOrdenCompra_OUT out = new LiquidarCustodiaOrdenCompra_OUT();
		Long errTot = 0L;
		HashMap<Long,Object> InformacionCartera = new HashMap<>();
		HashMap<String,Object> paramsCarteraCompra = new HashMap<>();
		
		log.write("INICIO::ORD.LiquidarCustodiaOrdenCompra");
		
		
	    String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
	    
	 
	    String pasaAGarantiaReporteSimultanea = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "CUSTODIA", "IngresaAGarantiaSimRepDeCompra");
	    
	    
	    List<Long> idCarteraCompra = new ArrayList<>();
	    
	    List<Long> IdOperacionCompra= new ArrayList<>();
	    List<String> IdTipoOperacionCompra= new ArrayList<>();
	    List<CarteraCompraObj_IN> carteraCompra = new ArrayList<>();
	    List<Subyacente> carteraSubyacente  = new ArrayList<>();
	    List<Garantia> carteraGarantia = new ArrayList<>();
	    String conceptoCompra = "";
	    List<ClienteNeteo> clientesANetear = new ArrayList<>();
	    
	    for (CarteraCompraObj_IN car: in.p_DatosCartera)
	    {   	
	    	if (car.AccionOperacion.equalsIgnoreCase("COMPRA")){
	    		carteraCompra.add(car);
	    		IdOperacionCompra.add(car.IdOperacion);
	    		IdTipoOperacionCompra.add(car.IdTipoOperacion);
	    		idCarteraCompra.add(car.IdCartera);
	    		conceptoCompra = car.IdConcepto;					
	    	}
	    }
	    //TODO:: validaciones
	    
	    
	    errTot = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		if (!errTot.equals(0L) ){
			return out;
		}
		
		
		if (carteraCompra.size() > 0)
		{
			
			RetiroCustodia retCusSub = new RetiroCustodia();
			RetiroCustodiaEntrada retCusIn = new RetiroCustodiaEntrada();
		    RetiroCustodiaSalida retCusOut = new RetiroCustodiaSalida();
		    retCusIn.p_IdEmpresa 		= in.p_IdEmpresa;			    
		    retCusIn.p_IdModulo 		= in.p_IdModulo;
			retCusIn.p_SimularOperacion = in.p_SimularOperacion;
			retCusIn.p_TicketAtencion 	= in.p_TicketAtencion;
			
			
		/**** 0: BUSCA CARTERA EN GARANTIA (asociado a la simultanea compra para retirarlas, si es que se pasaron a garantia)(con parametro apagado no se fueron a garantia por lo para ese caso no se retira garantia */
			paramsCarteraCompra.put("IdCarteras", idCarteraCompra);	
			
			if (pasaAGarantiaReporteSimultanea.equalsIgnoreCase("S")) 
			{									
				carteraGarantia = _sql.query(conn, SQL_SELECT_GARANTIA, paramsCarteraCompra, Garantia.class);
			}
			
	
			
		/**** 1: BUSCA OTROS DASTOS DE LAS CARTERAS DE SIMULTANEA A VENCER */
			List<Cartera> cartera  = new ArrayList<>();
			cartera = _sql.query(conn, SQL_SELECT_CARTERA, paramsCarteraCompra, Cartera.class);  //Consulta datos adiconales de la cartera
			InformacionCartera = Utils.List2Hash(cartera, "IdCartera");   // Transforma la lista cartera en un hash
			
		/**** 1.1: BUSCA DASTOS DE CARTERAS RELACIONADAS */	
			
			List<Cartera> carteraRelacionada  = new ArrayList<>();
			if (pasaAGarantiaReporteSimultanea.equalsIgnoreCase("N")) 
			{
				carteraRelacionada = _sql.query(conn, SQL_SELECT_CARTERA_RELACIONADA, paramsCarteraCompra, Cartera.class);  //Consulta datos de cartera relacionada
			}
			
		/***** 2: RETIRO DE GARANTIAS(esto hace retorno de cartera original) ******/
			if (pasaAGarantiaReporteSimultanea.equalsIgnoreCase("S")) 
			{	
				for (Garantia gar: carteraGarantia)
				{
					ObjDatosEntrada datos = new ObjDatosEntrada();
					datos.IdGenerarMovimientoCustodia = gar.IdGarantia;		//ID Interno del proceso
					datos.TablaAfectada 		 = "GARANTIA"; 				//CARTERA - SUBYACENTE - TRANSITO - GARANTIA
					datos.EsRetiroNegocio 		 = "S";
					//////////////////////////RETIRO REGISTRO ESPECIFICO
					datos.IdCustodia			 = gar.IdGarantia;						
					datos.FolioCustodia 		 = gar.FolioGarantia;
					/////////////////////////// OPERACION
					datos.AccionOperacion		 = in.p_OrigenLiquidacion;     //RETIRO - VENCIMIENTO - ANTICIPO - ANULACION - CERTIFICACION
					datos.FechaComprobante		 = Fecha.toDate(fechaSistema);
					datos.IdCliente				 = gar.IdCliente; 
					datos.IdPortafolio			 = gar.IdPortafolio;
					datos.FechaOperacion 		 = Fecha.toDate(fechaSistema);
					datos.IdTipoOperacion 		 = "";		
					datos.IdOperacion 			 = 0L;
					datos.IdConcepto			 = gar.IdConcepto;
					datos.IdCarteraOrigen		 = gar.IdCarteraOrigen;
					
					datos.IdTipoOperacion		 = gar.IdTipoOperacionOrigen;
					datos.PrecioCompra			 = gar.PrecioCompraOperacionOrigen;
					datos.TasaCompra			 = gar.TasaCompraOperacionOrigen;
					datos.IdCondicionLiquidacion = "T0"; 
					datos.FechaLiquidacion 		 = Fecha.toDate(fechaSistema);
					datos.IdTipoCanalIngreso 	 = gar.IdTipoCanalIngreso;
					datos.IdUsuario				 = in.p_IdUsuario;
					datos.IdModulo				 = in.p_IdModulo;
					datos.Glosa					 = "VENCIMIENTO SIMUTANEA";						
					
					/////////////////////////// INSTRUMENTO
					datos.IdOrigenGarantia      = gar.IdOrigenGarantia;
					datos.IdInstrumento 		= gar.IdInstrumento;
					datos.IdEmisor				= gar.IdEmisor;
					datos.IdMonedaInstrumento	= gar.IdMonedaInstrumento;
					
					/////////////////////////// CANTIDADES Y MONTOS
					datos.Cantidad				= gar.INGCantidad;
					datos.ValorRescate 			= gar.INGValorRescate;
					datos.IdBoveda				= gar.IdBoveda;
					datos.PrecioCompra			= gar.PrecioCompraOperacionOrigen; 
					datos.TasaCompra			= gar.TasaCompraOperacionOrigen; 
					datos.MontoCompraMonedaInstrumento	= gar.INGMontoATasaGarantiaActual;
					retCusIn.p_DatosEntrada.add(datos);
				}
			}
			
		
			
		/**** 3: BUSCA CARTERA SUBYACENTE asociado a la venta para retirarlas*/
			paramsCarteraCompra = new HashMap<>();
			paramsCarteraCompra.put("IdCarteras", idCarteraCompra);
			carteraSubyacente = _sql.query(conn, SQL_SELECT_SUBYACENTE, paramsCarteraCompra, Subyacente.class);
		
			if (in.p_DatosCartera.get(0).IdTipoOperacion.equals("COMSIM")) {
				List<ContraparteSimultanea> contr = _sql.query(conn, SQL_CONSULTAR_CONTRAPARTE_SIMULTANEA, new HashMap<>(),ContraparteSimultanea.class);
				
				for (Subyacente sub : carteraSubyacente) {
					ClienteNeteo cliente = new ClienteNeteo();
					cliente.IdInstrumento = sub.IdInstrumento;
					cliente.IdPortafolio = sub.IdPortafolio;
					clientesANetear.add(cliente);
					
					ClienteNeteo contraparte = new ClienteNeteo();
					contraparte.IdInstrumento = sub.IdInstrumento;
					contraparte.IdPortafolio = contr.get(0).IdPortafolioDefecto;
					clientesANetear.add(contraparte);		
				}		
			}
		/***** 4: RETIRO CARTERA SUBYACENTE ******/
			{	
				for (Subyacente carSub: carteraSubyacente){
					ObjDatosEntrada datos = new ObjDatosEntrada();
					datos.IdGenerarMovimientoCustodia = carSub.IdSubyacente;	//ID Interno del proceso
					datos.TablaAfectada 		 = "SUBYACENTE"; 				//CARTERA - SUBYACENTE - TRANSITO - GARANTIA
					datos.EsRetiroNegocio 		 = "S";					
					datos.IdCustodia 			 = carSub.IdSubyacente;						
					datos.FolioCustodia 		 = carSub.FolioSubyacente;
					datos.IdCarteraOrigen 		 = carSub.IdCarteraOrigen;
					datos.AccionOperacion		 = in.p_OrigenLiquidacion;	   //RETIRO - VENCIMIENTO - ANTICIPO - ANULACION - CERTIFICACION
					datos.FechaComprobante		 = Fecha.toDate(fechaSistema);						
					datos.IdCliente				 = carSub.IdCliente;
					datos.IdPortafolio			 = carSub.IdPortafolio;
					datos.FechaOperacion 		 = Fecha.toDate(fechaSistema);
					datos.IdTipoOperacion 		 = "";		
					datos.IdOperacion 			 = 0L;
					datos.IdConcepto			 = conceptoCompra;
					datos.IdCarteraOrigen		 = carSub.IdCarteraOrigen;
					datos.IdTipoOperacion		 = carSub.IdTipoOperacionOrigen;
					datos.PrecioCompra			 = carSub.PrecioCompraOperacionRelacionada;
					datos.TasaCompra			 = carSub.TasaCompraOperacionRelacionada;
					datos.IdInstrumento 		 = carSub.IdInstrumento;
					datos.IdCondicionLiquidacion = "T0"; 
					datos.FechaLiquidacion 		 = Fecha.toDate(fechaSistema);						
					datos.IdTipoCanalIngreso 	 = carSub.IdTipoCanalIngreso;
					datos.IdUsuario				 = in.p_IdUsuario;
					datos.IdModulo				 = in.p_IdModulo;
					datos.Glosa					 = "VENCIMIENTO SIMULTANEA";
					datos.IdInstrumento			 = carSub.IdInstrumento;
					datos.IdMonedaInstrumento	 = carSub.IdMonedaInstrumento;
					datos.IdEmisor				 = carSub.IdEmisor;
					datos.IdBoveda				 = carSub.IdBoveda;
					datos.Cantidad 				 = carSub.Cantidad;
					datos.ValorRescate 			 = carSub.ValorRescate; 
					
					retCusIn.p_DatosEntrada.add(datos);
				}
			}
			
		
			
			/***** 5: RETIRO CARTERA (CABECERA SIMULTANEA) ******/
			{
				for (CarteraCompraObj_IN car: carteraCompra){
					ObjDatosEntrada datos = new ObjDatosEntrada();
					Cartera infoCartera = (Cartera) InformacionCartera.get(car.IdCartera);
					
					datos.IdGenerarMovimientoCustodia = car.IdCartera;		//ID Interno del proceso
					datos.TablaAfectada 		 = "CARTERA"; 				//CARTERA - SUBYACENTE - TRANSITO - GARANTIA
					datos.EsRetiroNegocio 		 = "S";
					datos.IdCustodia 			 = car.IdCartera;						
					datos.FolioCustodia 		 = car.FolioCartera;
					datos.AccionOperacion		 = in.p_OrigenLiquidacion;   //	RETIRO - VENCIMIENTO - ANTICIPO - ANULACION - CERTIFICACION
					datos.FechaComprobante 		 = Fecha.toDate(fechaSistema);
					datos.IdCliente				 = car.IdCliente; 
					datos.IdPortafolio			 = car.IdPortafolio;
					datos.FechaOperacion 		 = Fecha.toDate(fechaSistema);
					datos.IdTipoOperacion 		 = car.IdTipoOperacion;						
					datos.IdConcepto		     = car.IdConcepto;
					datos.IdCliente				 = car.IdCliente;
					datos.IdPortafolio			 = car.IdPortafolio;
					datos.IdInstrumento			 = car.IdInstrumento;
					datos.IdEmisor               = car.IdEmisor;
					datos.IdUsuario 			 = in.p_IdUsuario;
					datos.IdModulo 				 = in.p_IdModulo;
					datos.Glosa 				 = "VENCIMIENTO SIMULTANEA COMPRA";
					datos.IdCondicionLiquidacion = car.IdCondicionLiquidacion;
					datos.FechaLiquidacion 		 = Fecha.toDate(fechaSistema);	
					datos.Cantidad 				 = car.Cantidad; 
					datos.ValorRescate 			 = BigDecimal.ZERO;
					datos.IdMonedaInstrumento 	 = infoCartera.IdMonedaInstrumento;					
					datos.IdBoveda 				 = infoCartera.IdBoveda;
					datos.MontoAPlazo            = calcularMontoAPlazo(infoCartera.INGTasaCompra, infoCartera.PlazoEnDiasOperacion, infoCartera.INGPrecioCompra.multiply(infoCartera.INGCantidad));
					datos.TasaCompra			 = infoCartera.TasaCompra;
					datos.PrecioCompra			 = infoCartera.PrecioCompra;
					datos.PrecioAPlazo			 = infoCartera.PrecioAPlazo;
					
					retCusIn.p_DatosEntrada.add(datos);
				}
			}
			
			retCusOut = retCusSub.logica(conn, retCusIn);
			out.P_OUT_MENSAJES.addAll(retCusOut.P_OUT_MENSAJES);
			
			
		
			/**** 6: BUSCA CARTERA PRESTADA AL FINANCISTA PARA RETIRARLAS  */

			retCusIn.p_DatosEntrada.clear();		
			
			
			if (pasaAGarantiaReporteSimultanea.equalsIgnoreCase("N")) {				
			
				for (Subyacente sub: carteraSubyacente){
					ObjDatosEntrada datos = new ObjDatosEntrada();
					
					Cartera infoCartera = (Cartera) InformacionCartera.get(sub.IdCarteraOrigen);
					
					datos.IdGenerarMovimientoCustodia = sub.IdSubyacente;	//ID Interno del proceso
					datos.TablaAfectada 		 = "CARTERA"; 					//CARTERA - SUBYACENTE - TRANSITO - GARANTIA
					datos.EsRetiroNegocio 		 = "S";						
					datos.AccionOperacion		 = "RETIRO";    				//	RETIRO - VENCIMIENTO - ANTICIPO - ANULACION - CERTIFICACION
					datos.FechaComprobante 		 = Fecha.toDate(fechaSistema);
					datos.IdCliente				 = sub.IdCliente; 
					datos.IdPortafolio			 = sub.IdPortafolio;
					datos.FechaOperacion 		 = Fecha.toDate(fechaSistema);
					datos.IdTipoOperacion 		 = "COMSIM";						
					datos.IdConcepto		     = "VENCIMIENTO".equalsIgnoreCase(in.p_OrigenLiquidacion) ? "VCTSIC":conceptoCompra;					
					datos.IdInstrumento			 = sub.IdInstrumento;
					datos.IdEmisor               = sub.IdEmisor;
					datos.IdUsuario 			 = in.p_IdUsuario;
					datos.IdModulo 				 = in.p_IdModulo;
					datos.Glosa 				 = "VENCIMIENTO SIMULTANEA COMPRA";
					datos.IdCondicionLiquidacion = "T0"; 
					datos.FechaLiquidacion 		 = Fecha.toDate(fechaSistema);	
					datos.Cantidad 				 = sub.Cantidad; 
					datos.ValorRescate 			 = BigDecimal.ZERO;
					datos.IdMonedaInstrumento 	 = sub.IdMonedaInstrumento;					
					datos.IdBoveda 				 = sub.IdBoveda;
					if(infoCartera != null && "VENCIMIENTO".equalsIgnoreCase(in.p_OrigenLiquidacion)) {
						datos.TasaCompra			 					= infoCartera.TasaCompra;
						datos.PrecioCompra			 					= infoCartera.PrecioCompra;
						datos.PrecioAPlazo			 					= infoCartera.PrecioAPlazo;
						datos.SIMCarteraPrestadaAlFinancista 			= "S";
					}
					
					for (Cartera carRel: carteraRelacionada){
						if (carRel.IdSubyacente.compareTo(sub.IdSubyacente) == 0 && carRel.IdInstrumento.compareTo(sub.IdInstrumento) == 0 && carRel.IdPortafolio.compareTo(sub.IdPortafolio) == 0 && carRel.INGCantidad.compareTo(sub.Cantidad) == 0)
						{	//Si existe cartera relacionada, hacemos referencia a ella (cuando la custodia prestada no queda en garantia sino directamente en cartera)
							datos.IdCustodia 			 = carRel.IdCartera;						
							datos.FolioCustodia 		 = carRel.FolioCartera;
						}							
					}
					
					
					retCusIn.p_DatosEntrada.add(datos);
				}
				
				

				
				retCusOut = retCusSub.logica(conn, retCusIn);
				// Si no se pudo retirar la cartera prestada al financista, se genera un movimiento en transito de Retiro, pendiende de certificar

					List<IngresoCustodia.ObjDatosEntrada> transitos = new ArrayList<>();
					for  (MensajeSalida mensaje : retCusOut.P_OUT_MENSAJES){
						if (mensaje.tipo.equalsIgnoreCase("ERROR")){	//Si se produjo un error en el reiro, se genera un movimiento en transito de retiro por el total del subyacente
							for (Subyacente sub: carteraSubyacente){
								if (mensaje.id.compareTo(sub.IdSubyacente) == 0){
									IngresoCustodia.ObjDatosEntrada	ObjEntrada = new IngresoCustodia.ObjDatosEntrada();
									ObjEntrada.IdGenerarMovimientoCustodia		= sub.IdSubyacente;
									ObjEntrada.TablaAfectada					= "TRANSITO";
									ObjEntrada.AccionOperacion					= "RETIRO";
									ObjEntrada.EsRegistroNuevo					= "S";
									ObjEntrada.IdCliente						= sub.IdCliente;
									ObjEntrada.IdPortafolio						= sub.IdPortafolio;
									ObjEntrada.FechaOperacion					= Fecha.toDate(fechaSistema);
									ObjEntrada.FechaComprobante 				= Fecha.toDate(fechaSistema);
									ObjEntrada.IdTipoOperacion					= "RETTIT";      	// RETIRO DE TITULOS
									ObjEntrada.IdTipoProducto					= "INSI";        	// INTERMEDIACION SIMULTANEAS					
									ObjEntrada.IdConcepto						= "VCTSIC";         // VENCIMIENTO SIMULTANEA COMPRA
									ObjEntrada.FechaVencimientoOperacion        = Fecha.toDate(fechaSistema);
									ObjEntrada.IdCondicionLiquidacion			= "T0"; 
									ObjEntrada.FechaLiquidacion					= Fecha.toDate(fechaSistema);
									ObjEntrada.IdUsuario						= in.p_IdUsuario;
									ObjEntrada.Glosa							= "VENCIMIENTO SIMULTANEA COMPRA (Subyacente: " + sub.IdSubyacente + " )";
									ObjEntrada.IdInstrumento					= sub.IdInstrumento;
									ObjEntrada.IdEmisor							= sub.IdEmisor;
									ObjEntrada.Cantidad							= sub.Cantidad;
									ObjEntrada.EsVentaCorta						= "N";
									ObjEntrada.IdBoveda							= sub.IdBoveda;
									ObjEntrada.IdMonedaInstrumento				= sub.IdMonedaInstrumento;
									ObjEntrada.FechaLiquidacion					= Fecha.toDate(fechaSistema);
									transitos.add(ObjEntrada);	
								}
							}
						}
						else
						{
							//Si NO se produjo un error en el retiro, se chequea que se haya retirado el total del subyacente. Si no es asi se genera un mov en transito de retiro por la cantidad faltante por retirar
							for (Subyacente sub: carteraSubyacente) {
								if (mensaje.id.compareTo(sub.IdSubyacente) == 0) {
									
									BigDecimal custodiaRetirada = retCusOut.P_OUT_DATOSSALIDA.stream()
											.filter(result -> sub.IdSubyacente.compareTo(result.IdGenerarMovimientoCustodia) == 0)
											.map(result ->{
												BigDecimal cantidad = BigDecimal.ZERO;
												if(result.Cantidad != null) {
													cantidad = result.Cantidad;
												}
												return cantidad;
											}).reduce(BigDecimal.ZERO, BigDecimal::add);
									
									if(custodiaRetirada.compareTo(sub.Cantidad) == -1) {
										
										Cartera infoCartera = (Cartera) InformacionCartera.get(sub.IdCarteraOrigen);
										IngresoCustodia.ObjDatosEntrada	ObjEntrada = new IngresoCustodia.ObjDatosEntrada();
										ObjEntrada.IdGenerarMovimientoCustodia		= sub.IdSubyacente;
										ObjEntrada.TablaAfectada					= "TRANSITO";
										ObjEntrada.AccionOperacion					= "RETIRO";
										ObjEntrada.EsRegistroNuevo					= "S";
										ObjEntrada.IdCliente						= sub.IdCliente;
										ObjEntrada.IdPortafolio						= sub.IdPortafolio;
										ObjEntrada.FechaOperacion					= Fecha.toDate(fechaSistema);
										ObjEntrada.FechaComprobante 				= Fecha.toDate(fechaSistema);
										ObjEntrada.IdTipoOperacion					= "RETTIT";      	// RETIRO DE TITULOS
										ObjEntrada.IdTipoProducto					= "INSI";        	// INTERMEDIACION SIMULTANEAS					
										ObjEntrada.IdConcepto						= "VCTSIC";         // VENCIMIENTO SIMULTANEA COMPRA
										ObjEntrada.FechaVencimientoOperacion        = Fecha.toDate(fechaSistema);
										ObjEntrada.IdCondicionLiquidacion			= "T0"; 
										ObjEntrada.FechaLiquidacion					= Fecha.toDate(fechaSistema);
										ObjEntrada.IdUsuario						= in.p_IdUsuario;
										ObjEntrada.Cantidad							= sub.Cantidad.subtract(custodiaRetirada);
										ObjEntrada.Glosa							= "VENCIMIENTO SIMULTANEA COMPRA (Subyacente: " + sub.IdSubyacente + " ) MOV PARCIAL " + ObjEntrada.Cantidad + " DE " + sub.Cantidad;
										ObjEntrada.IdInstrumento					= sub.IdInstrumento;
										ObjEntrada.IdEmisor							= sub.IdEmisor;										
										ObjEntrada.EsVentaCorta						= "N";
										ObjEntrada.IdBoveda							= sub.IdBoveda;
										ObjEntrada.IdMonedaInstrumento				= sub.IdMonedaInstrumento;
										ObjEntrada.FechaLiquidacion					= Fecha.toDate(fechaSistema);
										if(infoCartera != null && "VENCIMIENTO".equalsIgnoreCase(in.p_OrigenLiquidacion)) {
											ObjEntrada.TasaCompra			 		= infoCartera.TasaCompra;
											ObjEntrada.PrecioCompra			 		= infoCartera.PrecioCompra;
											ObjEntrada.PrecioAPlazo			 		= infoCartera.PrecioAPlazo;
										}
										
										transitos.add(ObjEntrada);
										
									}
									
								}
							}
						}
						
						
					}
					
					
					if (transitos.size() > 0)
					{
						IngresoCustodia ingresoCustodia = new IngresoCustodia();
						IngresoCustodia.IngresoCustodiaEntrada ingCusIn = new IngresoCustodia.IngresoCustodiaEntrada();
						ingCusIn.p_IdEmpresa				= in.p_IdEmpresa;		
						ingCusIn.p_IdModulo					= in.p_IdModulo;
						ingCusIn.p_IdUsuario				= in.p_IdUsuario;
						ingCusIn.p_SimularOperacion			= in.p_SimularOperacion;
						ingCusIn.p_TicketAtencion 			= 0L;
						ingCusIn.p_ValorizacionAutomatica	= "N";
						ingCusIn.p_DatosEntrada				= transitos;
						IngresoCustodia.IngresoCustodiaSalida ingCusOut = ingresoCustodia.logica(conn, ingCusIn);				

					}
					


			}	


		}
		


		/***** 7: GENERAR MOVIMIENTO EN TRANSITO DE RETITO DE GARANTIAS (SOLO PARA SIMULTANEAS) ******/
		if (pasaAGarantiaReporteSimultanea.equalsIgnoreCase("S"))
		{
			List<IngresoCustodia.ObjDatosEntrada> transitos = new ArrayList<>();
			
			for (Garantia gar: carteraGarantia)
			{	
				if ("COMSIM".equalsIgnoreCase(gar.IdTipoOperacionOrigen))   //Solo simultanea
				{
					IngresoCustodia.ObjDatosEntrada	ObjEntrada = new IngresoCustodia.ObjDatosEntrada();
					ObjEntrada.IdGenerarMovimientoCustodia		= -1*gar.IdGarantia;
					ObjEntrada.TablaAfectada					= "TRANSITO";
					ObjEntrada.AccionOperacion					= "INGRESO";
					ObjEntrada.EsRegistroNuevo					= "S";
					ObjEntrada.IdCustodia 						= gar.IdCarteraOrigen;
					ObjEntrada.FolioCarteraOrigen				= gar.FolioCarteraOrigen;	
					ObjEntrada.FolioCustodia                    = gar.FolioCarteraOrigen;
					ObjEntrada.IdCliente						= gar.IdCliente;
					ObjEntrada.IdPortafolio						= gar.IdPortafolio;
					ObjEntrada.FechaOperacion					= Fecha.toDate(fechaSistema);
					ObjEntrada.FechaComprobante 				= Fecha.toDate(fechaSistema);
					ObjEntrada.IdTipoOperacion					= "VESIMC";      	// VENCIMIENTO SIMULTANEA DE COMPRA
					ObjEntrada.IdTipoProducto					= "INSI";        	// INTERMEDIACION SIMULTANEAS					
					ObjEntrada.IdConcepto						= "VCTSIC";         // VENCIMIENTO SIMULTANEA COMPRA
					ObjEntrada.FechaVencimientoOperacion        = Fecha.toDate(fechaSistema);
					ObjEntrada.IdCondicionLiquidacion			= "T0"; 
					ObjEntrada.FechaLiquidacion					= Fecha.toDate(fechaSistema);
					ObjEntrada.IdUsuario						= in.p_IdUsuario;
					ObjEntrada.Glosa							= "VENCIMIENTO SIMULTANEA COMPRA";
					ObjEntrada.IdInstrumento					= gar.IdInstrumento;
					ObjEntrada.IdEmisor							= gar.IdEmisor;
					ObjEntrada.Cantidad							= gar.INGCantidad;
					ObjEntrada.EsVentaCorta						= "N";
					ObjEntrada.IdBoveda							= gar.IdBoveda;
					ObjEntrada.IdMonedaInstrumento				= gar.IdMonedaInstrumento;
					ObjEntrada.PrecioCompra						= gar.PrecioCompraOperacionOrigen;
					ObjEntrada.MontoCompra						= gar.INGMontoATasaGarantiaActual;
					ObjEntrada.FechaLiquidacion					= Fecha.toDate(fechaSistema);
					transitos.add(ObjEntrada);	
				}
			}
			

			
			if (transitos.size() > 0)
			{
				IngresoCustodia ingresoCustodia = new IngresoCustodia();
				IngresoCustodia.IngresoCustodiaEntrada ingCusIn = new IngresoCustodia.IngresoCustodiaEntrada();
				ingCusIn.p_IdEmpresa				= in.p_IdEmpresa;		
				ingCusIn.p_IdModulo					= in.p_IdModulo;
				ingCusIn.p_IdUsuario				= in.p_IdUsuario;
				ingCusIn.p_SimularOperacion			= in.p_SimularOperacion;
				ingCusIn.p_TicketAtencion 			= 0L;
				ingCusIn.p_ValorizacionAutomatica	= "N";
				ingCusIn.p_DatosEntrada				= transitos;
				IngresoCustodia.IngresoCustodiaSalida ingCusOut = ingresoCustodia.logica(conn, ingCusIn);
				out.P_OUT_MENSAJES.addAll(ingCusOut.P_OUT_MENSAJES);
				
				//CERTIFICA LOS MOVIMIENTOS EN TRANSITO CREADOS (Solo los cambia de estado CRT)
				List<GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar> movimientosAActualizar = new ArrayList<GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar>();
				
				for (IngresoCustodia.IngresoCustodiaSalida.ObjDatosSalida datosSalida :ingCusOut.P_OUT_DATOSSALIDA)
				{
					GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar movimiento = new GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar();					
					movimiento.IdMovimientoCarteraEnTransito = datosSalida.IdCustodia;
					movimiento.EsCertificacion = "S";
					movimiento.IdUsuario = in.p_IdUsuario;
					movimientosAActualizar.add(movimiento);						
				}
										
					
				if (movimientosAActualizar.size() > 0)
				{
					GestionMovimientoCarteraEnTransito.ActualizarMovimientoEnTransito(conn, movimientosAActualizar);
				}
			}
		}			

		
		/*---------------
		  INICIO NETEO	
		---------------*/

		if (clientesANetear.size() > 0) {
			EntradaNeteo eee = new EntradaNeteo();
			eee.p_IdEmpresa = in.p_IdEmpresa;
			eee.p_IdModulo = "FAC";
			eee.p_IdUsuario = in.p_IdUsuario;
			eee.p_PortafoliosInstrumento = clientesANetear;
			eee.p_SimularOperacion = "N";
			eee.p_UsaReglasNegocio = "S";

			AgregarPrograma.InAgregarPrograma paramsIn = new AgregarPrograma.InAgregarPrograma();
			paramsIn.p_IdEmpresa = in.p_IdEmpresa;
			paramsIn.p_IdModulo = "FAC";
			paramsIn.p_IdUsuario = in.p_IdUsuario;
			paramsIn.p_IdTipoPrograma = "NeteoCarterasNegativas";
			paramsIn.p_Segundos = 0L;
			paramsIn.p_FechaSistema = fechaSistema;
			List<Parametro> paramsParametros = new ArrayList<>();
			Parametro ppp = new Parametro();
			ppp.NombreParametro = "Entrada";
			Gson gson = Utils.getGsonBuilder();
			ppp.Valor = "";
			ppp.ValorBlob = gson.toJson(eee);
			paramsParametros.add(ppp);
			paramsIn.p_Parametros = paramsParametros;
			AgregarPrograma AP = new AgregarPrograma();
			AP.logica(conn, paramsIn);
		}
     
		/*---------------
		  FIN NETEO	
		---------------*/
		
		errTot = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		if (!errTot.equals(0L) )
		{
			return out;
		}
			
		
		log.write("FIN::ORD.LiquidarCustodiaOrdenCompra");
		return out;
	};
	
	
	// ----------------------------------------------
	// -- CALL
	// ----------------------------------------------	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		Gson gson = Utils.getGsonBuilder();
		LiquidarCustodiaOrdenCompra_IN jin = gson.fromJson(in.toString(), LiquidarCustodiaOrdenCompra_IN.class);
		LiquidarCustodiaOrdenCompra_OUT out = logica(conn, jin);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	};
	
	
	
	// Calcula Monto a Plazo  [Monto * (1 + (Tasa/100)*(Plazo/30))]
	private BigDecimal calcularMontoAPlazo(BigDecimal tasaPorcentaje, Long diasPlazo, BigDecimal monto) throws Exception {	
		try
		{
			BigDecimal tasa 							= tasaPorcentaje.divide(new BigDecimal(100),20,RoundingMode.HALF_UP);
			BigDecimal periodos 						= new BigDecimal((double)diasPlazo / 30);
			BigDecimal factor 							= BigDecimal.ONE.add(tasa.multiply(periodos));
			return  monto.multiply(factor);
		}
		catch(Exception ex)
		{
			return  BigDecimal.ZERO;
		}
	} 
 	
	// ----------------------------------------------
	// -- METODO PARA PRUEBAS LOCALES
	// ----------------------------------------------	
	public static void main(String[] args)  throws Exception,SQLException {
		// TODO Auto-generated method stub
		Connection conn = null;
		Gson gson = new Gson();
		
		try {
			System.out.println("== BEGIN ==");
			conn = PlataformaUtils.getDESAPEConnection();
			List<ObjMovimiento> entLogCus = new ArrayList<>();
			Long TicketAtencion = LogCustodia.logCustodia(conn,entLogCus);
			//datos entrada
			String json = "{\"p_IdPais\":166,\"p_IdEmpresa\":1,\"p_IdUsuario\":166,\"p_IdModulo\":\"ORD\",\"p_UsaReglasNegocio\":\"S\",\"p_TicketAtencion\":" +TicketAtencion+",\"p_Operacion\":\"ANTICIPO\",\"p_DatosCartera\":[{\"AccionOperacion\":\"COMPRA\",\"IdCartera\":2386,\"FolioCartera\":1121,\"IdTipoOperacion\": \"COMRRV\",\"IdOperacion\":1111,\"Cantidad\":1000,\"Monto\":7750,\"Tasa\":7.75,\"IdCondicionLiquidacion\":\"T0\",\"Fecha\":\"2017-07-26\"}]}";
			LiquidarCustodiaOrdenCompra_IN objIn = gson.fromJson(json,LiquidarCustodiaOrdenCompra_IN.class);
			 
			//mientras pruebo hare el rollback
			LiquidarCustodiaOrdenCompra demo = new LiquidarCustodiaOrdenCompra();
			
			LiquidarCustodiaOrdenCompra_OUT ObjOut=demo.logica(conn, objIn);
				
			System.out.println(ObjOut.toString());
			//if (MensajeSalida.getNumeroErrores(ObjOut.P_OUT_MENSAJES) > 0)
				conn.rollback();
			//else conn.commit();
			
		}catch (Exception e){
			e.printStackTrace();
		}finally {
			conn.close();
		}
			System.out.println("== END ==");

	};
}
