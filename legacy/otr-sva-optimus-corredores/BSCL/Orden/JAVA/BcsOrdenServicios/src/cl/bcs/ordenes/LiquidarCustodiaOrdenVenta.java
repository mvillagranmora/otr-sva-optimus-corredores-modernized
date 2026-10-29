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

public class LiquidarCustodiaOrdenVenta implements JavaCall{
	private final Log log = new Log("ORD.LiquidarCustodiaOrdenVenta");
	
	private final String SQL_SELECT_SUBYACENTE 	= loadResourceToString("cl/bcs/ordenes/sql/LiquidarCustodiaOrden_ConsultarSubyacente.sql");
	private final String SQL_SELECT_CARTERA 	= loadResourceToString("cl/bcs/ordenes/sql/LiquidarCustodiaOrden_ConsultarCartera.sql");

	private final String SQL_CONSULTAR_CONTRAPARTE_SIMULTANEA = loadResourceToString("cl/bcs/ordenes/sql/LiquidarCustodiaOrden_ConsultarContraparteSimultanea.sql");

	// ----------------------------------------------
	// -- CLASES PARA ENTRADA
	// ----------------------------------------------
	
	public static class LiquidarCustodiaOrdenVenta_IN {
		public Long				p_IdPais;
		public Long				p_IdEmpresa;
	    public Long				p_IdUsuario;
	    public String			p_IdModulo;
		public String			p_SimularOperacion = "N";
	    public String			p_UsaReglasNegocio;
	    public Long				p_TicketAtencion;
	    public String			p_OrigenLiquidacion;
	    public List<CarteraVentaObj_IN>	p_DatosCartera;
	    
	    
	    public LiquidarCustodiaOrdenVenta_IN() {
	    	this.p_DatosCartera = new ArrayList<CarteraVentaObj_IN>();
	    }


		public String toString() {
			return Utils.printJson(this, false);
		};
		
	}
	
	
	public static class CarteraVentaObj_IN {
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
		////public BigDecimal	MontoAPlazo;
		
		public String toString() {
			return Utils.printJson(this, false);
		};		
	}	

	
	
	// ----------------------------------------------
	// -- CLASES PARA SALIDA
	// ----------------------------------------------

	public static class LiquidarCustodiaOrdenVenta_OUT {
		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;

		public List<CarteraObj_OUT>		P_OUT_OPERACION;
		
		public LiquidarCustodiaOrdenVenta_OUT() {
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
	
	public static class Subyacente {
		public Long 		IdSubyacente;
	    public Long 		FolioSubyacente;
	    public Long 		FolioCarteraRelacionada; 
	    public String 		IdEstado;
	    public BigDecimal 	Cantidad;
	    public BigDecimal 	ValorRescate;	    
	    public Long 		IdInstrumento;
	    public BigDecimal 	PrecioCompraOperacionRelacionada;    
	    public BigDecimal 	TasaCompraOperacionRelacionada;	    
	    public Long 		IdCarteraOrigen; 
	    public String 		IdTipoOperacionOrigen;	    
	    public Long 		IdCliente;
	    public Long 		IdPortafolio; 
	    public String 		IdBoveda;	    
	    public String 		IdMonedaInstrumento;
	    public String 		IdEmisor;
	    public String 		IdTipoCanalIngreso;	  
	    
	    public Long 		IdCarteraRetirada;
	    public String 		IdBolsa;
	    public Long 		IdCuentaCustodioPortafolio;
	    public BigDecimal 	MontoCompra;
	    
	    
	    public String toString() {
			return Utils.printJson(this, false);
		}
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
	public LiquidarCustodiaOrdenVenta_OUT logica (Connection conn, LiquidarCustodiaOrdenVenta_IN in) throws Exception {
		SQL _sql = new SQL();
		LiquidarCustodiaOrdenVenta_OUT out = new LiquidarCustodiaOrdenVenta_OUT();
		//Long Id = 0L,errTot = 0L;
		Long errTot = 0L;
		HashMap<Long,Object> InformacionCartera = new HashMap<>();
		HashMap<String,Object> paramsCarteraVenta = new HashMap<>();
		
		log.write("INICIO::ORD.LiquidarCustodiaOrdenVenta");
		
	    String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
	    List<Long> idCarteraVenta = new ArrayList<>();
	    List<CarteraVentaObj_IN> carteraVenta = new ArrayList<>();
	    List<Subyacente> subyacente  = new ArrayList<>();
	    List<Cartera> cartera  = new ArrayList<>();
	    List<ClienteNeteo> clientesANetear = new ArrayList<>();
	    
	    String conceptoVenta = "";
	    
	    for (CarteraVentaObj_IN car: in.p_DatosCartera)
	    {
	    	if (car.AccionOperacion.equalsIgnoreCase("VENTA")){
	    		carteraVenta.add(car);
	    		idCarteraVenta.add(car.IdCartera);
	    		conceptoVenta = car.IdConcepto;
	    	}
	    }
	    
	    //TODO:: validaciones

	    
	    errTot = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		if (!errTot.equals(0L) ){
			return out;
		}
		
		
		
		/****1: BUSCA DATOS EXTRA DE CARTERA Y SUBYACENTE (Asociado a la venta para retirarlas) */
		if (carteraVenta.size() > 0)
		{				
			paramsCarteraVenta.put("IdCarteras", idCarteraVenta);				
			subyacente = _sql.query(conn, SQL_SELECT_SUBYACENTE, paramsCarteraVenta, Subyacente.class); //Consulta datos del subyacente				
			cartera = _sql.query(conn, SQL_SELECT_CARTERA, paramsCarteraVenta, Cartera.class);  //Consulta datos adiconales de la cartera 
			
			InformacionCartera = Utils.List2Hash(cartera, "IdCartera");   // Transforma la lista cartera en un hash
			
			if (in.p_DatosCartera.get(0).IdTipoOperacion.equals("VENSIM")) {
				List<ContraparteSimultanea> contr = _sql.query(conn, SQL_CONSULTAR_CONTRAPARTE_SIMULTANEA, new HashMap<>(),ContraparteSimultanea.class);
				
				for (Subyacente sub : subyacente) {
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
		}
		
		
		
		RetiroCustodia retCusSub = new RetiroCustodia();
		RetiroCustodiaEntrada retCusIn = new RetiroCustodiaEntrada();
	    RetiroCustodiaSalida retCusOut = new RetiroCustodiaSalida();
		
	    retCusIn.p_IdEmpresa = in.p_IdEmpresa;
		retCusIn.p_SimularOperacion = in.p_SimularOperacion;
		retCusIn.p_TicketAtencion = in.p_TicketAtencion;
		retCusIn.p_IdModulo = in.p_IdModulo;
		
		
		/***** 2: RETIRO CARTERA SUBYACENTE (tambien se devuelven las carteras prestadas si es que el subyacente tiene el IdCarteraRetirada, si no lo tiene se devuelve mas abajo ) ******/
		{	
			for (Subyacente carSub: subyacente){
				ObjDatosEntrada datos = new ObjDatosEntrada();
				datos.IdGenerarMovimientoCustodia = carSub.IdSubyacente;	//ID Interno del proceso
				datos.TablaAfectada			= "SUBYACENTE"; 					//CARTERA - SUBYACENTE - TRANSITO - GARANTIA
				datos.EsRetiroNegocio 		= "S";
				datos.IdCustodia 			= carSub.IdSubyacente;						
				datos.FolioCustodia 		= carSub.FolioSubyacente;
				datos.AccionOperacion		= in.p_OrigenLiquidacion;		//RETIRO - VENCIMIENTO - ANTICIPO - ANULACION - CERTIFICACION
				datos.FechaComprobante 		= Fecha.toDate(fechaSistema);
				datos.IdCliente 			= carSub.IdCliente;
				datos.IdPortafolio 			= carSub.IdPortafolio;
				datos.FechaOperacion 		= Fecha.toDate(fechaSistema);
				datos.IdConcepto 			= conceptoVenta;
				datos.IdCondicionLiquidacion = "T0"; 
				datos.FechaLiquidacion 		=  Fecha.toDate(fechaSistema);
				datos.IdUsuario 			= in.p_IdUsuario;
				datos.IdModulo 				= in.p_IdModulo;
				datos.Glosa 				= "VENCIMIENTO SIMULTANEA VENTA";
				datos.IdCarteraOrigen		= carSub.IdCarteraOrigen;
				datos.IdTipoOperacion		= carSub.IdTipoOperacionOrigen;
				datos.PrecioCompra			= carSub.PrecioCompraOperacionRelacionada;
				datos.TasaCompra			= carSub.TasaCompraOperacionRelacionada;
				datos.IdInstrumento 		= carSub.IdInstrumento;
				datos.Cantidad 				= carSub.Cantidad; 
				datos.ValorRescate 			= carSub.ValorRescate; 
			
				retCusIn.p_DatosEntrada.add(datos);
			}
			
		}
		
		
		/***** 3: RETIRO CARTERA (CABECERA) ******/
		{
			for (CarteraVentaObj_IN car: carteraVenta)
			{
				ObjDatosEntrada datos = new ObjDatosEntrada();
				Cartera infoCartera = (Cartera) InformacionCartera.get(car.IdCartera);

				datos.IdGenerarMovimientoCustodia = car.IdCartera;		//ID Interno del proceso
				datos.TablaAfectada 		 = "CARTERA"; 				//CARTERA - SUBYACENTE - TRANSITO - GARANTIA
				datos.EsRetiroNegocio 		 = "S";
				datos.IdCustodia 			 = car.IdCartera;						
				datos.FolioCustodia 		 = car.FolioCartera;
				datos.AccionOperacion		 = in.p_OrigenLiquidacion;  //RETIRO - VENCIMIENTO - ANTICIPO - ANULACION - CERTIFICACION
				datos.FechaComprobante 		 = Fecha.toDate(fechaSistema);
				datos.FechaOperacion 		 = Fecha.toDate(fechaSistema);
				datos.IdTipoOperacion 		 = car.IdTipoOperacion;
				datos.IdConcepto			 = car.IdConcepto;
				datos.IdCondicionLiquidacion = car.IdCondicionLiquidacion;
				datos.FechaLiquidacion 		 = Fecha.toDate(fechaSistema);
				datos.IdUsuario 			 = in.p_IdUsuario;
				datos.IdModulo 				 = in.p_IdModulo;
				datos.Glosa 				 = "VENCIMIENTO SIMULTANEA VENTA";
				datos.IdMonedaInstrumento 	 = infoCartera.IdMonedaInstrumento;					
				datos.IdBoveda 				 = infoCartera.IdBoveda;
				datos.IdCliente				 = car.IdCliente;
				datos.IdPortafolio			 = car.IdPortafolio;
				datos.IdInstrumento			 = car.IdInstrumento;					
				datos.Cantidad				 = car.Cantidad; 
				datos.IdEmisor               = car.IdEmisor;					 
				datos.ValorRescate			 = BigDecimal.ZERO;
				datos.MontoAPlazo            = calcularMontoAPlazo(infoCartera.INGTasaCompra, infoCartera.PlazoEnDiasOperacion, infoCartera.INGPrecioCompra.multiply(infoCartera.INGCantidad));
				
				retCusIn.p_DatosEntrada.add(datos);
			}
		}
		
	    
	
		retCusOut = retCusSub.logica(conn, retCusIn);
		out.P_OUT_MENSAJES.addAll(retCusOut.P_OUT_MENSAJES);
		
		
		
		
		/***** 4: SI POR ALGUN MOTIVO EL SUBYACENTE NO TIENE IdCarteraRetirada RETORNAMOS MANUALMENTE LAS CARTERAS PRESTADAS (cuando el subyacente tiene IdCarteraRetirada  se devuelven al vencer el subyacente en el punto 1)  ******/
				
		List<IngresoCustodia.ObjDatosEntrada> retornoCartera = new ArrayList<>();
		
		for (Subyacente sub: subyacente)		
		{						
			if (sub.IdCarteraRetirada.compareTo(0L) != 0)   continue;
			// Solo si no esta relacionado con el IdCarteraRetirada
			IngresoCustodia.ObjDatosEntrada	ObjEntrada = new IngresoCustodia.ObjDatosEntrada();
			ObjEntrada.IdGenerarMovimientoCustodia		= sub.IdSubyacente;
			ObjEntrada.TablaAfectada					= "CARTERA";
			ObjEntrada.AccionOperacion					= "INGRESO";
			ObjEntrada.EsRegistroNuevo					= "S";
			ObjEntrada.IdCliente						= sub.IdCliente;
			ObjEntrada.IdPortafolio						= sub.IdPortafolio;			
			ObjEntrada.FechaOperacion					= Fecha.toDate(fechaSistema);
			ObjEntrada.FechaComprobante 				= Fecha.toDate(fechaSistema);
			ObjEntrada.IdTipoOperacion					= "INGTIT"; 
			ObjEntrada.IdTipoProducto					= "INSI";        	// INTERMEDIACION SIMULTANEAS
			ObjEntrada.IdConcepto						= "VCTSIV";  	    // VENCIMIENTO SIMULTANEA VENTA						
			ObjEntrada.FechaVencimientoOperacion        = Fecha.toDate(fechaSistema);
			ObjEntrada.IdCondicionLiquidacion			= "T0"; 
			ObjEntrada.FechaLiquidacion					= Fecha.toDate(fechaSistema);
			ObjEntrada.IdUsuario						= in.p_IdUsuario;
			ObjEntrada.Glosa							= "VENCIMIENTO SIMULTANEA VENTA";
			ObjEntrada.IdInstrumento					= sub.IdInstrumento;
			ObjEntrada.IdEmisor							= sub.IdEmisor;
			ObjEntrada.Cantidad							= sub.Cantidad;
			ObjEntrada.EsVentaCorta						= "N";
			ObjEntrada.IdBolsa							= sub.IdBolsa;
			ObjEntrada.IdBoveda							= sub.IdBoveda;
			ObjEntrada.IdCuentaCustodioPortafolio		= sub.IdCuentaCustodioPortafolio;
			ObjEntrada.MontoCompra                      = sub.MontoCompra;
			
			retornoCartera.add(ObjEntrada);	
		}
		
		
		if (retornoCartera.size() > 0)
		{
			IngresoCustodia ingresoCustodia = new IngresoCustodia();
			IngresoCustodia.IngresoCustodiaEntrada ingCusIn = new IngresoCustodia.IngresoCustodiaEntrada();
			ingCusIn.p_IdEmpresa				= in.p_IdEmpresa;		
			ingCusIn.p_IdModulo					= in.p_IdModulo;
			ingCusIn.p_IdUsuario				= in.p_IdUsuario;
			ingCusIn.p_SimularOperacion			= in.p_SimularOperacion;
			ingCusIn.p_TicketAtencion 			= 0L;
			ingCusIn.p_ValorizacionAutomatica	= "N";
			ingCusIn.p_DatosEntrada				= retornoCartera;
			IngresoCustodia.IngresoCustodiaSalida ingCusOut = ingresoCustodia.logica(conn, ingCusIn);
		}
		
		
		
		
		
		
		/** 5: GARANTIAS QUE PUEDA TENER INGRESADAS NO VAN POR QUE SE RETIRAN MANUAL PARA REPORTE - PARA SIMUTANEAS NO APLICAN *****/
		
		
		
		
		/***** 6: GENERAR MOVIMIENTO EN TRANSITO DE RETITO DE TITULOS (SOLO SIMULTANEA NOMINAL) ******/
		{
			List<IngresoCustodia.ObjDatosEntrada> transitos = new ArrayList<>();
			
			for (CarteraVentaObj_IN car: carteraVenta)
			{
				Cartera infoCartera = (Cartera) InformacionCartera.get(car.IdCartera);
				
				if ("VENSIM".equalsIgnoreCase(car.IdTipoOperacion) && "S".equalsIgnoreCase(infoCartera.EsNominal))   //Solo simultanea nominal
				{
					IngresoCustodia.ObjDatosEntrada	ObjEntrada = new IngresoCustodia.ObjDatosEntrada();
					ObjEntrada.IdGenerarMovimientoCustodia		= -1*car.IdCartera;
					ObjEntrada.TablaAfectada					= "TRANSITO";
					ObjEntrada.AccionOperacion					= "RETIRO";
					ObjEntrada.EsRegistroNuevo					= "S";
					ObjEntrada.IdCustodia 						= car.IdCartera;
					ObjEntrada.FolioCarteraOrigen				= car.FolioCartera;	
					ObjEntrada.FolioCustodia                    = car.FolioCartera;
					ObjEntrada.IdCliente						= car.IdCliente;
					ObjEntrada.IdPortafolio						= car.IdPortafolio;
					ObjEntrada.FechaOperacion					= Fecha.toDate(fechaSistema);
					ObjEntrada.FechaComprobante 				= Fecha.toDate(fechaSistema);
					ObjEntrada.IdTipoOperacion					= "VESIMV";      	// VENCIMIENTO SIMULTANEA DE VENTA
					ObjEntrada.IdTipoProducto					= "INSI";        	// INTERMEDIACION SIMULTANEAS
					ObjEntrada.IdConcepto						= "VCTSIV";  	    // VENCIMIENTO SIMULTANEA VENTA						
					ObjEntrada.FechaVencimientoOperacion        = Fecha.toDate(fechaSistema);
					ObjEntrada.IdCondicionLiquidacion			= car.IdCondicionLiquidacion; 
					ObjEntrada.FechaLiquidacion					= Fecha.toDate(fechaSistema);
					ObjEntrada.IdUsuario						= in.p_IdUsuario;
					ObjEntrada.Glosa							= "VENCIMIENTO SIMULTANEA VENTA";
					ObjEntrada.IdInstrumento					= car.IdInstrumento;
					ObjEntrada.IdEmisor							= car.IdEmisor;
					ObjEntrada.Cantidad							= car.Cantidad;
					ObjEntrada.EsVentaCorta						= "N";
					ObjEntrada.EsNominal						= infoCartera.EsNominal;
					ObjEntrada.IdBolsa							= infoCartera.IdBolsa;
					ObjEntrada.IdBoveda							= infoCartera.IdBoveda;
					ObjEntrada.IdCuentaCustodioPortafolio		= infoCartera.IdCuentaCustodioPortafolio;
									
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

				
			} // if (transitos.size() > 0)
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
		if (!errTot.equals(0L) ){
			return out;
		}
			
			
		
		log.write("FIN::ORD.LiquidarCustodiaOrdenVenta");
		return out;
	};
	
	
	
	// ----------------------------------------------
	// -- CALL
	// ----------------------------------------------	
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		Gson gson = Utils.getGsonBuilder();
		LiquidarCustodiaOrdenVenta_IN jin = gson.fromJson(in.toString(), LiquidarCustodiaOrdenVenta_IN.class);
		LiquidarCustodiaOrdenVenta_OUT out = logica(conn, jin);
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
			String json = "{\"p_IdPais\":166,\"p_IdEmpresa\":1,\"p_IdUsuario\":166,\"p_IdModulo\":\"ORD\",\"p_UsaReglasNegocio\":\"S\",\"p_TicketAtencion\":" +TicketAtencion+",\"p_Operacion\":\"ANTICIPO\",\"p_DatosCartera\":[{\"AccionOperacion\":\"VENTA\",\"IdCartera\":2386,\"FolioCartera\":1121,\"IdTipoOperacion\": \"VENRRV\",\"IdOperacion\":1111,\"Cantidad\":1000,\"Monto\":7750,\"Tasa\":7.75,\"IdCondicionLiquidacion\":\"T0\",\"Fecha\":\"2017-07-26\"}]}";
			LiquidarCustodiaOrdenVenta_IN objIn = gson.fromJson(json,LiquidarCustodiaOrdenVenta_IN.class);
			//mientras pruebo hare el rollback
			LiquidarCustodiaOrdenVenta demo = new LiquidarCustodiaOrdenVenta();
			
			LiquidarCustodiaOrdenVenta_OUT ObjOut=demo.logica(conn, objIn);
				
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
