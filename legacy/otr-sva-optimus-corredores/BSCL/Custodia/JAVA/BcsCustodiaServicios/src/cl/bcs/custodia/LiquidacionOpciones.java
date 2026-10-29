package cl.bcs.custodia;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
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
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.tesoreria.CrearIngresoEgreso;
import cl.bcs.tesoreria.CrearIngresoEgreso.CrearIngresoEgreso_Entrada;
import cl.bcs.tesoreria.CrearIngresoEgreso.CrearIngresoEgreso_Salida;
import cl.bcs.tesoreria.CrearIngresoEgreso.FormaDePago_Entrada;
import cl.bcs.tesoreria.CrearIngresoEgreso.IngresoEgreso_Entrada;
import cl.bcs.utils.JavaCall;
//import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class LiquidacionOpciones implements JavaCall {

	private final String SQL_SELECT_CARGO_ABONO_CUENTA_INVERSION = loadResourceToString("cl/bcs/custodia/sql/LiquidacionOpciones_SelectCargoAbono.sql");
	private final String SQL_SELECT_CARGO_ABONO_TESORERIA = loadResourceToString("cl/bcs/custodia/sql/LiquidacionOpciones_SelectIngresoEgreso.sql");
	private final String SQL_SELECT_PORTAFOLIOS_OPCIONES = loadResourceToString("cl/bcs/custodia/sql/LiquidacionOpciones_SelectPortafoliosOpciones.sql");	
	private final String SQL_SELECT_TESORERIA_FORMA_DE_PAGO = loadResourceToString("cl/bcs/custodia/sql/LiquidacionOpciones_SelectFormaDePago.sql");	
	private final String SQL_UPDATE_NUMERO_REFERENCIA = loadResourceToString("cl/bcs/custodia/sql/LiquidacionOpciones_Update_NumeroReferencia.sql");
	private static final SimpleDateFormat FORMATO_YYYYMMDD     = new SimpleDateFormat("yyyy-MM-dd");


	// --------------------------------------------------------
	// CLASES DE ENTRADA
	// --------------------------------------------------------
	private class LiquidacionOpcionesEntrada {
		public Long   p_IdEmpresa;
		public Long   p_IdUsuario;
		public String p_IdModulo;
		public String p_FechaConsulta;
		public String toString() {
			return Utils.printJson(this, false);			
		}
	};

	// --------------------------------------------------------
	// CLASES DE SALIDA
	// --------------------------------------------------------

	private class LiquidacionOpcionesSalida {
		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;
		
		public LiquidacionOpcionesSalida (){
			P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}	
	};


	// --------------------------------------------------------
	// CLASES DE PROCESO
	// --------------------------------------------------------
	
	public static class CargoAbono{
		public Long 		NumeroCargoAbono;
		public Long 		IdPortafolio;
		public String 		IdMoneda;
		public String 		IdTipoMovimiento;
		public String 		IdConcepto;
		public String 		Descripcion;
		public String 		IdCondicionDeLiquidacion;
		public String 		FechaCondicionDeLiquidacion;
		public BigDecimal 	Monto;
		public Long 		IdEjecutivo;
		public Long 		IdSucursal;
		public String 		IdTipoProducto;
		public String 		IdTipoOperacion;
		public Long 		IdOperacion;
		public String 		FechaOperacion;
		public String 		FechaLiquidacionOperacion;
		public String 		FechaVencimientoOperacion;
		public String 		IdMonedaOperacion;
		public BigDecimal 	MontoOperacion;
		public BigDecimal 	TipoCambioOperacion;
		public Long 		IdContraparte;
		public String 		IdTipoComprobante;
		public Long 		IdComprobante;
		public String 		FolioComprobante;
		public Long 		IdComprobanteDetalle;
		public Long 		IdComprobanteFormaDePago;
		public String 		IdTipoMovimientoAsociado;
		public Long 		IdMovimientoAsociado;
		public String 		FechaComprobante;
		public Long 		IdPersonaPagoCobro;
		public Long 		IdInstrumento;
		public String 		IdTipoInstrumento;
		public String 		Nemo;
		public String 		EsIndicadorBilateral;
		public String 		EsCargoAbonoPorTraspaso;
		public Long 		IdPortafolioTraspaso;
		public String 		GeneroContraparteEnTesoreria;
		public String 		CertificadoAutomatico;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}	
	
		public static class TesoreriaFormaDePago {
		public Long 		IdPortafolio;
		public String		IdMedioDePago;
		public String 		IdModalidadPago;		
		public BigDecimal 	Monto;		
		public Long 		IdCuentaBancaria;		
		public String 		NombreBanco;		
		public String 		NumeroCuentaBancaria;
		public String 		IdTipoCuentaBancaria;
		public String 		NombreTipoCuentaBancaria;		
		public String 		IdTipoTarjetaBancaria;		
		public String 		NumeroTarjetaBancaria;		
		public String 		FechaVencimiento;
		public Long         NumeroReferencia;
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	}
		
		
		public static class ClientePortafolio{
			public Long		NumeroReferencia;
			public Long 	IdPortafolio;
			public Date 	Fecha;
		
			public String toString() {
				return Utils.printJson(this, false);
			};			
		}		

	private static final Logger logger = LogManager.getLogger(LiquidacionOpciones.class);
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		LiquidacionOpcionesEntrada in = gson.fromJson(jin.toString(), LiquidacionOpcionesEntrada.class);

		LiquidacionOpcionesSalida out = logica(con, in);				
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);

		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	
	public LiquidacionOpcionesSalida logica(Connection con, LiquidacionOpcionesEntrada in) throws Exception {
		
		LiquidacionOpcionesSalida out = new LiquidacionOpcionesSalida();
		
		out.P_OUT_ERRORESCONTADOR = 0L;
		logger.info("INICIO LIQUIDACION OPCIONES");
		
		SQL sql=new SQL();
		logger.info("ENTRADA > " + in.toString());

		
		String IdMonedaSistema 	= ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa,"PLATAFORMA", "PLA.MonedaSistema");
		
		String fechaSistema = in.p_FechaConsulta;
		
		if (in.p_FechaConsulta.isEmpty()) {
			fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");//"2020-02-20";
		}

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////		
// 1.CARGOS Y ABONOS CTA INV.
//  	PREPARAR DATA PARA EL ENVIO A CUENTA DE INVERSION (debe ser anterior al envio a cuenta inversion)

		HashMap<String,Object> params = new HashMap<String,Object>();					
		// Para todos los clientes se genera un ABONO en cuenta de inversion			
		
		List<CargoAbono> cargoAbono = new ArrayList<CargoAbono>();
		params.clear();
		params.put("FechaSistema", fechaSistema);
		cargoAbono = sql.query(con,SQL_SELECT_CARGO_ABONO_CUENTA_INVERSION,params,CargoAbono.class);
		logger.debug("CARGOS_ABONOS",cargoAbono);

		if(!cargoAbono.isEmpty()){			
			CrearCargoAbonoEntrada entradaCrearCargoAbono = new CrearCargoAbonoEntrada();
			CrearCargoAbono crearCargoAbono = new CrearCargoAbono();
			for(CargoAbono ingCargoAbono : cargoAbono){
				ObjCargoAbonoAGenerar entradaCargoAbono = new ObjCargoAbonoAGenerar();
				entradaCargoAbono.NumeroCargoAbono 				= ingCargoAbono.NumeroCargoAbono;
				entradaCargoAbono.IdPortafolio 					= ingCargoAbono.IdPortafolio;
				entradaCargoAbono.IdMoneda						= IdMonedaSistema;
				entradaCargoAbono.IdTipoMovimiento 				= ingCargoAbono.IdTipoMovimiento;
				entradaCargoAbono.IdConcepto 					= ingCargoAbono.IdConcepto;
				entradaCargoAbono.Descripcion 					= ingCargoAbono.Descripcion;
				entradaCargoAbono.IdCondicionDeLiquidacion 		= ingCargoAbono.IdCondicionDeLiquidacion;
				entradaCargoAbono.FechaCondicionDeLiquidacion	= ingCargoAbono.FechaCondicionDeLiquidacion;
				entradaCargoAbono.Monto 						= ingCargoAbono.Monto;				
				entradaCargoAbono.IdTipoOperacion				= ingCargoAbono.IdTipoOperacion;
				entradaCargoAbono.FechaOperacion				= ingCargoAbono.FechaOperacion;
				entradaCargoAbono.FechaLiquidacionOperacion		= ingCargoAbono.FechaLiquidacionOperacion;
				entradaCargoAbono.IdTipoMovimientoAsociado		= ingCargoAbono.IdTipoMovimientoAsociado;
				entradaCargoAbono.Nemo							= ingCargoAbono.Nemo;
				entradaCargoAbono.EsCargoAbonoPorTraspaso		= ingCargoAbono.EsCargoAbonoPorTraspaso;
				entradaCargoAbono.GeneroContraparteEnTesoreria	= ingCargoAbono.GeneroContraparteEnTesoreria;
				entradaCargoAbono.CertificadoAutomatico			= ingCargoAbono.CertificadoAutomatico;
				entradaCargoAbono.IdOperacion 					= ingCargoAbono.IdOperacion;	
				entradaCrearCargoAbono.p_CargoAbonoAGenerar.add(entradaCargoAbono);
			}
			entradaCrearCargoAbono.p_IdEmpresa 			= in.p_IdEmpresa;
			entradaCrearCargoAbono.p_IdUsuario 			= in.p_IdUsuario;
			entradaCrearCargoAbono.p_IdModulo 			= in.p_IdModulo;
			entradaCrearCargoAbono.p_simularOperacion 	= "N";
			entradaCrearCargoAbono.p_UsaReglasNegocio 	= "S";
			entradaCrearCargoAbono.p_FechaSistema 		= fechaSistema;
			entradaCrearCargoAbono.p_EsIngresoManual 	= "N";
			CrearCargoAbonoSalida salidaCrearCargoAbono = crearCargoAbono.logica(con, entradaCrearCargoAbono);

			System.out.println(salidaCrearCargoAbono.P_OUT_MENSAJES);
			
			out.P_OUT_ERRORESCONTADOR +=  salidaCrearCargoAbono.P_OUT_ERRORESCONTADOR != null ? salidaCrearCargoAbono.P_OUT_ERRORESCONTADOR : 0L;
			out.P_OUT_MENSAJES.addAll(salidaCrearCargoAbono.P_OUT_MENSAJES);
		}
		
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////		
//2. INGRESO Y/O EGRESOS TESORERIA
		
		//Busca portafolios y actualiza NumeroReferencia
		params.clear();
		params.put("FechaSistema", fechaSistema);
		List<ClientePortafolio> listaPortfolios = sql.query(con, SQL_SELECT_PORTAFOLIOS_OPCIONES, params, ClientePortafolio.class);		
		
		List<Long> listaPortafoliosAgrupada = listaPortfolios.stream().map(s -> s.IdPortafolio).distinct().collect(Collectors.toList());
		LinkedList<Long> secuencias = new LinkedList<>(Utils.getSecuencia(con, "BSCL.Custodia.BD.Secuencias::NumeroReferenciaOpciones", (long) listaPortafoliosAgrupada.size()));
		HashMap<Long,Long> hashLista = new HashMap<>();
		for(int i = 0; i< listaPortafoliosAgrupada.size(); i++) {
			hashLista.put(listaPortafoliosAgrupada.get(i), secuencias.get(i));
		}
		
		for (ClientePortafolio elemento : listaPortfolios) {
			elemento.NumeroReferencia = hashLista.get(elemento.IdPortafolio);
		}

		List<ClientePortafolio> actualizar = new ArrayList<ClientePortafolio>();		
		
		for (ClientePortafolio dato:  listaPortfolios) {
			ClientePortafolio portafolio = new ClientePortafolio();			
			portafolio.IdPortafolio 	= dato.IdPortafolio;
			portafolio.NumeroReferencia = dato.NumeroReferencia;
			portafolio.Fecha 			=  new java.sql.Date(FORMATO_YYYYMMDD.parse(fechaSistema).getTime());
			actualizar.add(portafolio);			
		}
		
		sql.batch(con, SQL_UPDATE_NUMERO_REFERENCIA, actualizar, ClientePortafolio.class);
		
		// Para todos los clientes se genera un ABONO en cuenta de inversion	
		CrearIngresoEgreso_Salida tesOut = new CrearIngresoEgreso_Salida();
		
		List<IngresoEgreso_Entrada> ingresoEgreso = new ArrayList<IngresoEgreso_Entrada>();
		params.clear();
		params.put("FechaSistema", fechaSistema);
		ingresoEgreso = sql.query(con,SQL_SELECT_CARGO_ABONO_TESORERIA,params,IngresoEgreso_Entrada.class);
		
		
		if (!ingresoEgreso.isEmpty()) {		
			//Formas de Pago
			params.clear();
			params.put("FechaSistema", fechaSistema);
			List<TesoreriaFormaDePago> formasDePagoCliente = sql.query(con, SQL_SELECT_TESORERIA_FORMA_DE_PAGO, params, TesoreriaFormaDePago.class);		
	
			//Ingreso Egreso a Tesoreria
			CrearIngresoEgreso tes = new CrearIngresoEgreso();
			CrearIngresoEgreso_Entrada entTes = new CrearIngresoEgreso_Entrada();
			entTes.p_IdEmpresa 				= in.p_IdEmpresa;
			entTes.p_IdUsuario 				= in.p_IdUsuario;
			entTes.p_IdModulo 				= in.p_IdModulo;
			entTes.p_SimularOperacion 		= "N";
			entTes.p_UsaReglasNegocio 		= "S";				
			entTes.p_EsIngresoAutomatico 	= "S";      
			entTes.p_FechaMovimiento 		= fechaSistema; 
			
			for (IngresoEgreso_Entrada ingEgr: ingresoEgreso) {
				IngresoEgreso_Entrada IEGen = new IngresoEgreso_Entrada();
				IEGen.NumeroReferencia 				= ingEgr.NumeroReferencia;							 
				IEGen.IdTipoMovimiento 				= ingEgr.IdTipoMovimiento;							
				IEGen.IdTipoPersona 				= ingEgr.IdTipoPersona;											
				IEGen.IdPersona						= ingEgr.IdPersona;  		 						
				IEGen.IdPortafolio 					= ingEgr.IdPortafolio;  		 					 
				IEGen.IdMoneda 						= IdMonedaSistema;											
				IEGen.Monto 						= ingEgr.Monto;										
				IEGen.FechaLiquidacion 				= ingEgr.FechaLiquidacion;  						                                                                      
				IEGen.IdConcepto 					= ingEgr.IdConcepto;  		 						
				IEGen.IdTipoPersonaTitularPago 		= ingEgr.IdTipoPersona;											
				IEGen.IdPersonaTitularPago 			= ingEgr.IdPersona;									
				IEGen.IdPortafolioTitularPago 		= ingEgr.IdPortafolio;								
				IEGen.IdCaja 						= 0L;  		 										
				IEGen.SecuenciaAgrupacion 			= ""; 												
				IEGen.SecuenciaRelacionada 			= 0L;	 											
				IEGen.Glosa 						= "";  		 																								   	
			   	//Parametros Referenciales(Opcionales)
				//IEGen.IdEmpleadoDigitador			= operacionCerrar.p_IdUsuario;  		 			
				IEGen.IdEmpleadoDigitador			= in.p_IdUsuario;  		 																
				IEGen.IdSucursalEmpleadoDigitador	= 0L;  												
				IEGen.IdTipoProducto				= "";  		 										
				IEGen.IdTipoOperacion				= ingEgr.IdTipoOperacion;																    
				IEGen.IdTipoComprobante				= "CUS";  		 									
				IEGen.IdComprobante					= ingEgr.NumeroReferencia;							
				IEGen.FechaComprobante				= ingEgr.FechaComprobante;							
				IEGen.FechaLiquidacionComprobante	= ingEgr.FechaLiquidacion;							
				
				IEGen.IdOperacion					= ingEgr.NumeroReferencia;										
				IEGen.FechaOperacion				= ingEgr.FechaLiquidacion;							
				IEGen.IdCondicionLiquidacion		= "";  												
				IEGen.IdBolsa						= "";  		 										
				IEGen.IdMonedaOperacion				= IdMonedaSistema;											
				IEGen.MontoOperacion				= ingEgr.Monto;										
				IEGen.IdTipoReajuste				= "";  		 										
				IEGen.FechaParidad					= "0001-01-01";  		 							
				IEGen.ValorParidad					= BigDecimal.ZERO;   								
				IEGen.FolioCartera					= 0L;  		 										
				IEGen.IdInstrumento					= 0L;  		 										
				IEGen.IdTipoInstrumento				= "";  		 										
				IEGen.IdClasificacionTipoInstrumento= ""; 												
			    //Indicadores
				IEGen.Bilateral 					= "N";		        								
				IEGen.GeneraComprobante 			= "N"; 												
				IEGen.AgruparEnComprobante 			= "N"; 												
				IEGen.MueveCuentaInversion			= "N";  											
				IEGen.TitularPago					= "S";  		 												
				
				entTes.p_IngresoEgresoGenerar.add(IEGen);
				
			}
			
			if (!formasDePagoCliente.isEmpty()) {
		
				for (TesoreriaFormaDePago  fPag:formasDePagoCliente) {
					FormaDePago_Entrada FPagTes = new FormaDePago_Entrada();
					FPagTes.NumeroReferencia			= fPag.NumeroReferencia;
					FPagTes.NumeroReferenciaFormaDePago	= fPag.NumeroReferencia;
					FPagTes.IdMedioDePago				= fPag.IdMedioDePago;  
					FPagTes.IdModalidadDePago			= fPag.IdModalidadPago;
					
					
					FPagTes.IdMoneda					= IdMonedaSistema;		
					FPagTes.Monto						= fPag.Monto;
					FPagTes.IdCuentaBancariaCorredora	= 0L; 		
					FPagTes.IdTarjetaBancariaCorredora	= 0L; 	
					FPagTes.IdChequera					= 0L; 	
					FPagTes.NumeroDocumentoCorredora	= 0L; 	
					FPagTes.FechaDocumentoPagoCorredora	= "0001-01-01 00:00:00";	
					FPagTes.IdCuentaBancariaSocio		= fPag.IdCuentaBancaria; 			
					FPagTes.IdBancoSocio				= 0L;
					FPagTes.NombreCuentaBancariaSocio	= fPag.NombreBanco;		
					FPagTes.NumeroCuentaBancariaSocio	= fPag.NumeroCuentaBancaria;		
					FPagTes.IdTipoCuentaBancariaSocio	= fPag.IdTipoCuentaBancaria;		
					FPagTes.NombreTipoCuentaBancariaSocio = fPag.NombreTipoCuentaBancaria; 	
					FPagTes.IdTarjetaBancariaSocio		= 0L;			
					FPagTes.IdTipoTarjetaBancariaSocio	= fPag.IdTipoTarjetaBancaria;  	
					FPagTes.NombreTipoTarjetaBancariaSocio = ""; 
					FPagTes.NumeroTarjetaBancariaSocio	= fPag.NumeroTarjetaBancaria;  	
					FPagTes.NumeroSerieSocio			= "";  				
					FPagTes.NumeroDocumentoSocio		= 0L;			
					FPagTes.FechaDocumentoPagoSocio		= fPag.FechaVencimiento;
					entTes.p_FormaDePago.add(FPagTes);			
				}
				
			}
			
			logger.debug("CUS.Llamada Ingreso Tesoreria");
			if (!entTes.p_IngresoEgresoGenerar.isEmpty()) {
				
				tesOut = tes.logica(con, entTes);
				 out.P_OUT_MENSAJES.addAll(tesOut.P_OUT_MENSAJES);		
				if (MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES) > 0){
					return out;
				}
			}
		
		}
			
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////			
		logger.info("TERMINO LIQUIDACION OPCIONES");
		return out;
	}


	public static void main(String[] args) {
		System.out.println("== INICIO PRUEBA LOCAL ==");

		try {
			
			Connection con = PlataformaUtils.getDESAConnection();
			Gson gson = Utils.getGsonBuilder();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":2,\"p_IdModulo\":\"CUS\",\"p_FechaConsulta\":\"2020-03-19\",\"p_ComprobanteLiquidar\":[{\"IdComprobante\":5077,\"IdTipoMovimiento\":\"MOVING\",\"AfectaCuentaInversion\":\"CLI\"}],\"p_FormaDePagoLiquidar\":[]}";

			
			LiquidacionOpcionesEntrada Objin = gson.fromJson(json, LiquidacionOpcionesEntrada.class);
			LiquidacionOpciones demo = new LiquidacionOpciones();

			try {
				LiquidacionOpcionesSalida ObjOut = demo.logica(con, Objin);
				System.out.println(ObjOut.toString());
				 //con.rollback();
				con.commit();

			} catch (Exception er) {
				er.printStackTrace();
				con.rollback();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("== FIN PRUEBA LOCAL ==");
	}

};
