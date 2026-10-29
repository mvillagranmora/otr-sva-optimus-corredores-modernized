package cl.bcs.tesoreria;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class LiquidacionMasivaComprobante implements JavaCall // En SQL se llama LiquidarComprobantes
{

	// Log
	private final Log log = new Log("TES.LiquidarComprobanteMasiva");

	// Queries
	private final String SQL_SELECT_COMPROBANTE_MASIVO	 = loadResourceToString("cl/bcs/tesoreria/sql/LiquidarComprobante_ConsultarFacturacionCargaMasivaTes.sql");
	private final String SQL_SELECT_COMPROBANTE_FOLIO	 = loadResourceToString("cl/bcs/tesoreria/sql/LiquidarComprobante_SelectComprobanteFolio.sql");
	private final String SQL_SELECT_CANTIDAD_FACTURA  	 = loadResourceToString("cl/bcs/tesoreria/sql/LiquidarComprobante_SelectCantidadMovimientoTransito.sql");
	private final String SQL_SELECT_TIPO_OPERACION		 = loadResourceToString("cl/bcs/tesoreria/sql/LiquidarComprobante_SelectTipoOperacion.sql");
	private final String SQL_SELECT_FORMA_DE_PAGO		 = loadResourceToString("cl/bcs/tesoreria/sql/LiquidarComprobante_SelectConsultarFormaDePago.sql");
	private final String SQL_CALL_MODIFICAR_FORMA_DE_PAGO = "call \"BSCL.Tesoreria.BD.Servicios::EdicionMultiple\"("
			+ "p_IdEmpresa=>:p_IdEmpresa,"
			+ "p_IdUsuario=>:p_IdUsuario,"
			+ "p_IdModulo=>:p_IdModulo,"
			+ "p_UsaReglasNegocio=>:p_UsaReglasNegocio,"
			+ "p_FechaMovimiento=>:p_FechaMovimiento,"
			+ "p_Entidad=>:p_Entidad,"
			+ "p_FormasDePago=>#p_FormasDePago,"
			+ "p_RegistrosAProcesar=>#p_RegistrosAProcesar,"
			+ "p_out_InformacionContador=>@p_out_InformacionContador,"
			+ "p_out_AdvertenciasContador=>@p_out_AdvertenciasContador,"
			+ "p_out_ErroresContador=>@p_out_ErroresContador)";
	
	private final String SQL_CONSULTAR_FORMA_PAGO 		 = loadResourceToString("cl/bcs/ordenes/sql/FormaDePago_ConsultarFormaDePago.sql");
	private final String SQL_CONSULTAR_BANCO_CORREDORA 	 = loadResourceToString("cl/bcs/ordenes/sql/FormaDePago_ConsultarCuentaCorredora.sql");

	// -------------------------------------------------------------------------------------------
	// CLASES DE ENTRADA
	// -------------------------------------------------------------------------------------------

	public static class LiquidarComprobante_Entrada {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_SimularOperacion = "N";
		public String p_UsaReglasNegocio;
		public String p_FechaMovimiento; // Corresponde a Fecha Sistema
		
		public List<NombreCuentaBancariaEntrada> p_Banco; 
		
		// En SQL se llama "p_TablaComprobante"
		public List<ComprobanteLiquidarEntrada> p_ComprobanteLiquidar; // Comprobantes a Liquidar

		// En SQL se llama "p_TablaComprobanteFormaDePago"
		public List<FormaDePagoLiquidar_Entrada> p_FormaDePagoLiquidar; // Formas de Pago a Liquidar > Si no especifica
																		// la FP a liquidar, se asume que se deben
																		// liquidar todas las FP de ese comprobante

		public LiquidarComprobante_Entrada() {
			p_SimularOperacion = "N";
			p_ComprobanteLiquidar = new ArrayList<ComprobanteLiquidarEntrada>();
			p_FormaDePagoLiquidar = new ArrayList<FormaDePagoLiquidar_Entrada>();
		}

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	// Comprobantes a Liquidar
	public static class ComprobanteLiquidarEntrada {
		public Long IdComprobante;

	}
	
	public static class NombreCuentaBancariaEntrada {
		public String NombreCuentaBancaria;
	}
	
	public static class NombreCuentaBancaria {
		public String NombreCuentaBancaria;
		public Long IdCuentaBancariaCorredora;
	}

	// Formas de Pago a Liquidar - Si no especifica la FP a liquidar para un
	// comprobante, se asume que se deben liquidar todas las FP de ese comprobante
	public static class FormaDePagoLiquidar_Entrada {
		public Long IdComprobante;
		public Long IdComprobanteFormaDePago;
	}

	// -------------------------------------------------------------------------------------------
	// CLASES DE SALIDA
	// -------------------------------------------------------------------------------------------
	
	public static class FormaDePagoLiquidar_Salida {
		public Long IdComprobante;
		public Long IdComprobanteFormaDePago;
	}
	
	
	
	public static class ComprobanteLiquidar_Salida {
		public Long IdComprobante;
		public String IdTipoMovimiento; // MOVING / MOVEGR
		public String AfectaCuentaInversion; // CLI: Cliente / COR: Corredora / VACIO: Ambos

	}

	public static class LiquidarComprobante_Salida {
		public List<MensajeSalida>                  P_OUT_MENSAJES;
		public Long 								P_OUT_ERRORESCONTADOR;
		public List<ResultadoProceso_Salida> 		P_OUT_RESULTADOPROCESO;
		public List<MovimientosCertifificar_Salida> P_OUT_MOVIMIENTOSCERTIFICAR;
		public List<ComprobanteLiquidar_Salida> 	P_OUT_COMPROBANTELIQUIDAR;
		public List<FormaDePagoLiquidar_Salida> 	P_OUT_FORMADEPAGO;

		public LiquidarComprobante_Salida() {
			P_OUT_MENSAJES			 = new ArrayList<MensajeSalida>();
			P_OUT_RESULTADOPROCESO	 = new ArrayList<ResultadoProceso_Salida>();
			P_OUT_MOVIMIENTOSCERTIFICAR	 = new ArrayList<MovimientosCertifificar_Salida>();
			P_OUT_COMPROBANTELIQUIDAR	 = new ArrayList<ComprobanteLiquidar_Salida>();
			P_OUT_FORMADEPAGO			 = new ArrayList<FormaDePagoLiquidar_Salida>();
		};

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class ResultadoProceso_Salida {
		public String Campo; // VARCHAR(40) -- Ej: "IdComprobante"
		public Long   Id; // BIGINT -- IdComprobante liquidado Ej: 9999
		public String Servicio; // VARCHAR(250) -- Ej: "BSCL.Tesoreria.BD.Servicios::LiquidarComprobantes"
		public String Resultado; // VARCHAR(40) -- Ej: "OK"

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class MovimientosCertifificar_Salida {
		public Long   IdMovimientoCarteraEnTransito;
		public String IdTipoOperacion;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class ComprobanteFolioSalida {
		public Long       IdComprobante; // BIGINT -- IdComprobante en base al folio
		public Long       FolioComprobante;
		public String     IdEstado;
		public Long       IdComprobanteFacturacion;
		public BigDecimal MontoFactura;
		public BigDecimal MontoTesoreria;
		public String     IdTipoMovimiento;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class CantidadFactura {
		public Long       IdCfact; // IdComprobanteFacturacion
		public BigDecimal CantidadFactura;
		public BigDecimal CantidadMovimiento;


//		public Long 		IdMovimientoCarteraEnTransito;
//		public String		IdTipoOperacion;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class Datos_Usuario_Entrada {
		public Long    IdComprobante; // BIGINT -- IdEmpresa en base al IdComprobante
		public String  IdTipoMovimiento;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class DataEntradaFormadePago {
		public Long       IdComprobante; // BIGINT -- IdComprobante en base al folio
		public Long       FolioComprobante;
		public String     IdEstado;
		public Long       IdComprobanteFacturacion;
		public BigDecimal MontoFactura;
		public BigDecimal MontoTesoreria;
		public String     IdTipoMovimiento;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class FormaDePagoEntrada
	{
		public Long 		NumeroReferencia;				//  BIGINT        
		public String		IdMedioDePago;          		//	NVARCHAR(3)  
		public String		IdModalidadDePago;      		//	NVARCHAR(6)   
		public String		IdMoneda;               		//	NVARCHAR(3)
		public BigDecimal   Monto; 							//	DECIMAL(26,8)
		public Long 		IdCuentaBancariaCorredora; 		//	BIGINT
		public Long 		IdTarjetaBancariaCorredora; 	//	BIGINT
		public Long 		IdChequera; 					//	BIGINT
		public Long 		NumeroDocumentoCorredora; 		//	BIGINT
		public String		FechaDocumentoPagoCorredora;	//	NVARCHAR(40)
		public Long 		IdCuentaBancariaSocio; 			//	BIGINT
		public Long 		IdBancoSocio; 					//	BIGINT
		public String		NombreCuentaBancariaSocio;		//	NVARCHAR(100)
		public String		NumeroCuentaBancariaSocio;		//	NVARCHAR(100)
		public String		IdTipoCuentaBancariaSocio;		//	NVARCHAR(4)
		public String		NombreTipoCuentaBancariaSocio; 	//  NVARCHAR(100)
		public Long 		IdTarjetaBancariaSocio;			//	BIGINT
		public String		IdTipoTarjetaBancariaSocio;  	//	NVARCHAR(40)
		public String		NombreTipoTarjetaBancariaSocio; //	NVARCHAR(100)
		public String		NumeroTarjetaBancariaSocio;  	//	NVARCHAR(100)
		public String		NumeroSerieSocio;  				//	NVARCHAR(40)
		public Long 		NumeroDocumentoSocio;			//	BIGINT
		public String		FechaDocumentoPagoSocio;  		//	NVARCHAR(40)	
		
		public String toString(){
			return Utils.printJson(this, false);
		}
	}
	public static class FormaDePagoSalida
	{
		public Long 		NumeroReferencia;		
		public String		IdMedioDePago; 
		public String		IdModalidadDePago; 
		public String		IdMoneda; 
		public BigDecimal   Monto; 	
		public String		FechaDocumentoPagoSocio; 

		public String toString(){
			return Utils.printJson(this, false);
		}
	}
	public static class RegistrosProcesar {
		public Long Id;
		public String Entidad;
		public String Accion;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	// -------------------------------------------------------------------------------------------
	// LOGICA DEL SERVICIO
	// -------------------------------------------------------------------------------------------

	public LiquidarComprobante_Salida logica(Connection con, LiquidarComprobante_Entrada in) throws Exception {

		log.write("== INICIO LIQUIDAR COMPROBANTE MASIVO==");
		log.write("ENTRADA > " + in.toString());

		SQL sql = new SQL();
		LiquidarComprobante_Salida out = new LiquidarComprobante_Salida();

		HashMap<String, Object> parametroFolio = new HashMap<>();
		HashMap<String, Object> parametroComprobante = new HashMap<>();
//        MensajeSalida ms = new MensajeSalida();

		List<Long> IdComprobante = new ArrayList<>();
		List<ComprobanteLiquidarEntrada> datosFolios = new ArrayList<ComprobanteLiquidarEntrada>();
		for (int i = 0; i < in.p_ComprobanteLiquidar.size(); i++) {
			ComprobanteLiquidarEntrada dataFolio = new ComprobanteLiquidarEntrada();
			dataFolio.IdComprobante = in.p_ComprobanteLiquidar.get(i).IdComprobante;
			IdComprobante.add(in.p_ComprobanteLiquidar.get(i).IdComprobante);
			datosFolios.add(dataFolio);

		}

		parametroFolio.put("folio", IdComprobante);
		List<ComprobanteFolioSalida> salidaComprobante = sql.query(con, SQL_SELECT_COMPROBANTE_FOLIO, parametroFolio,
				ComprobanteFolioSalida.class);

		
		if (salidaComprobante.isEmpty()) {
			out.P_OUT_MENSAJES.add(
					new MensajeSalida(null, MensajeSalida.MENSAJE_ERROR, "Factura", "Factura sin Folio Asociado "));
		}
		
		

	
// aqui se debe ejecutar el for para modificar las formas de pago
		
		List<String> NombreCuentaBancaria = in.p_Banco.stream().map(linea -> linea.NombreCuentaBancaria).collect(Collectors.toList());
		List<Long> ComprobanteTes = salidaComprobante.stream().map(linea -> linea.IdComprobante).collect(Collectors.toList());
		String FechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa,"PLATAFORMA", "PLA.FechaSistema");
		List<Long> IdComprobanteEntrada = new ArrayList<>();
		List<Long> IdNumeroReferencia = new ArrayList<>();
		List<String> NombreBancoEntrada = new ArrayList<>();
		List<String> NombreBancoSalida = new ArrayList<>();
		HashMap<String, Object> params = new HashMap<>();
		params.put( "IdComprobante", ComprobanteTes);
		List<FormaDePagoSalida> salidaFormadePago = sql.query(con, SQL_CONSULTAR_FORMA_PAGO, params,
				FormaDePagoSalida.class);
		
		HashMap<String, Object> var = new HashMap<>();
		var.put("banco", NombreCuentaBancaria);
		List<NombreCuentaBancaria> salidaBancoCorredora = sql.query(con, SQL_CONSULTAR_BANCO_CORREDORA, var,
				NombreCuentaBancaria.class);
		List<FormaDePagoEntrada> ObjFormaPago = new ArrayList<FormaDePagoEntrada>();
		List<RegistrosProcesar> ObjRegistro = new ArrayList<RegistrosProcesar>();
		for (ComprobanteFolioSalida com : salidaComprobante) {
			IdComprobanteEntrada.add(com.IdComprobante);
			for (String bancoCorredora : NombreCuentaBancaria) {
				NombreBancoEntrada.add(bancoCorredora);
				for (NombreCuentaBancaria dataCorredora : salidaBancoCorredora) {
					NombreBancoSalida.add(dataCorredora.NombreCuentaBancaria);
					if (NombreBancoEntrada.equals(NombreBancoSalida)) {
						for (FormaDePagoSalida data : salidaFormadePago) {
							IdNumeroReferencia.add(data.NumeroReferencia);
							if (IdComprobanteEntrada.equals(IdNumeroReferencia) && IdComprobanteEntrada!=null ) {
								
								SQL.exec(con, "CREATE LOCAL TEMPORARY TABLE #p_FormasDePago( "
										+ "\"NumeroReferencia\" BIGINT CS_FIXED,"
										+ "\"IdMedioDePago\" NVARCHAR(3) CS_STRING,"
										+ "\"IdModalidadDePago\" NVARCHAR(6) CS_STRING,"
										+ "\"IdMoneda\" NVARCHAR(3) CS_STRING,"
										+ "\"Monto\" DECIMAL(26,8) CS_FIXED,"
										+ "\"IdCuentaBancariaCorredora\" BIGINT CS_FIXED,"
										+ "\"IdTarjetaBancariaCorredora\" BIGINT CS_FIXED,"
										+ "\"IdChequera\" BIGINT CS_FIXED,"
										+ "\"NumeroDocumentoCorredora\" BIGINT CS_FIXED,"
										+ "\"FechaDocumentoPagoCorredora\" NVARCHAR(40) CS_STRING,"
										+ "\"IdCuentaBancariaSocio\" BIGINT CS_FIXED,"
										+ "\"IdBancoSocio\" BIGINT CS_FIXED,"
										+ "\"NombreCuentaBancariaSocio\" NVARCHAR(100) CS_STRING,"
										+ "\"NumeroCuentaBancariaSocio\" NVARCHAR(100) CS_STRING,"
										+ "\"IdTipoCuentaBancariaSocio\" NVARCHAR(4) CS_STRING,"
										+ "\"NombreTipoCuentaBancariaSocio\" NVARCHAR(100) CS_STRING,"
										+ "\"IdTarjetaBancariaSocio\" BIGINT CS_FIXED,"
										+ "\"IdTipoTarjetaBancariaSocio\" NVARCHAR(40) CS_STRING,"
										+ "\"NombreTipoTarjetaBancariaSocio\" NVARCHAR(100) CS_STRING,"
										+ "\"NumeroTarjetaBancariaSocio\" NVARCHAR(100) CS_STRING,"
										+ "\"NumeroSerieSocio\" NVARCHAR(40) CS_STRING,"
										+ "\"NumeroDocumentoSocio\" BIGINT CS_FIXED,"
										+ "\"FechaDocumentoPagoSocio\" NVARCHAR(40) CS_STRING"
										+ ")");
								
								
								FormaDePagoEntrada ins = new FormaDePagoEntrada();
								
								ins.NumeroReferencia = data.NumeroReferencia;
								ins.IdMedioDePago = data.IdMedioDePago;
								ins.IdModalidadDePago = data.IdModalidadDePago;
								ins.IdMoneda = data.IdMoneda;
								ins.Monto = data.Monto;
								ins.IdCuentaBancariaCorredora = dataCorredora.IdCuentaBancariaCorredora;
								ins.IdTarjetaBancariaCorredora = 0L ;
								ins.IdChequera = 0L ;
								ins.NumeroDocumentoCorredora = 0L ;
								ins.FechaDocumentoPagoCorredora = FechaSistema ;
								ins.IdCuentaBancariaSocio = null;
								ins.IdBancoSocio = null;
								ins.NombreCuentaBancariaSocio = null;
								ins.NumeroCuentaBancariaSocio = null;
								ins.IdTipoCuentaBancariaSocio = null;
								ins.NombreTipoCuentaBancariaSocio = null;
								ins.IdTarjetaBancariaSocio = null;
								ins.IdTipoTarjetaBancariaSocio = null;
								ins.NombreTipoTarjetaBancariaSocio = null;
								ins.NumeroTarjetaBancariaSocio = null;
								ins.NumeroSerieSocio = null;
								ins.NumeroDocumentoSocio = null;
								ins.FechaDocumentoPagoSocio = FechaSistema ;
								
								ObjFormaPago.add(ins);
								
								sql.batch(con,
										"INSERT INTO #p_FormasDePago ("
												+ "\"NumeroReferencia\","
												+ "\"IdMedioDePago\","
												+ "\"IdModalidadDePago\","
												+ "\"IdMoneda\","
												+ "\"Monto\","
												+ "\"IdCuentaBancariaCorredora\","
												+ "\"IdTarjetaBancariaCorredora\","
												+ "\"IdChequera\","
												+ "\"NumeroDocumentoCorredora\","
												+ "\"FechaDocumentoPagoCorredora\","
												+ "\"IdCuentaBancariaSocio\","
												+ "\"IdBancoSocio\","
												+ "\"NombreCuentaBancariaSocio\","
												+ "\"NumeroCuentaBancariaSocio\","
												+ "\"IdTipoCuentaBancariaSocio\","
												+ "\"NombreTipoCuentaBancariaSocio\","
												+ "\"IdTarjetaBancariaSocio\","
												+ "\"IdTipoTarjetaBancariaSocio\","
												+ "\"NombreTipoTarjetaBancariaSocio\","
												+ "\"NumeroTarjetaBancariaSocio\","
												+ "\"NumeroSerieSocio\","
												+ "\"NumeroDocumentoSocio\","
												+ "\"FechaDocumentoPagoSocio\")"
									 	+ " VALUES("
									 	+ ":NumeroReferencia,"
									 	+ ":IdMedioDePago,"
									 	+ ":IdModalidadDePago,"
									 	+ ":IdMoneda,"
									 	+ ":Monto,"
									 	+ ":IdCuentaBancariaCorredora,"
									 	+ ":IdTarjetaBancariaCorredora,"
									 	+ ":IdChequera,"
										+ ":NumeroDocumentoCorredora,"
										+ ":FechaDocumentoPagoCorredora,"
										+ ":IdCuentaBancariaSocio,"
										+ ":IdBancoSocio,"
										+ ":NombreCuentaBancariaSocio,"
										+ ":NumeroCuentaBancariaSocio,"
										+ ":IdTipoCuentaBancariaSocio,"
										+ ":NombreTipoCuentaBancariaSocio,"
										+ ":IdTarjetaBancariaSocio,"
										+ ":IdTipoTarjetaBancariaSocio,"
										+ ":NombreTipoTarjetaBancariaSocio,"
										+ ":NumeroTarjetaBancariaSocio,"
										+ ":NumeroSerieSocio,"
										+ ":NumeroDocumentoSocio,"
										+ ":FechaDocumentoPagoSocio)",
										ObjFormaPago, 
									 	FormaDePagoEntrada.class);


				
				SQL.exec(con, "CREATE LOCAL TEMPORARY TABLE #p_RegistrosAProcesar ("
						+ "\"Id\"			   			BIGINT,"
						+ "\"Entidad\"				   	NVARCHAR(300),"
						+ "\"Accion\"		   					NVARCHAR(3)"
					 	+ ")");


					RegistrosProcesar instru = new RegistrosProcesar();
					
						instru.Id = ObjFormaPago.get(0).NumeroReferencia;
						instru.Entidad = "TES.Comprobante";
						instru.Accion = "AGN";
						ObjRegistro.add(instru);
						RegistrosProcesar reg = new RegistrosProcesar();
						reg.Id = ObjFormaPago.get(0).NumeroReferencia;
						reg.Entidad = "TES.Comprobante";
						reg.Accion = "NTN";
						ObjRegistro.add(reg);
						
						sql.batch(con,
							"INSERT INTO #p_RegistrosAProcesar ("
							+ "\"Id\","
							+ "\"Entidad\","				
							+ "\"Accion\")"
						 	+ " VALUES("
						 			+ ":Id,"
									+ ":Entidad,"				
									+ ":Accion)",
						 	ObjRegistro, 
						 	RegistrosProcesar.class);
						
						System.out.println(ObjRegistro);

					// Escalares de entrada:
					HashMap<String, Object> parmsIn = new HashMap<String, Object>();
					parmsIn.put("p_IdEmpresa", in.p_IdEmpresa);
					parmsIn.put("p_IdUsuario", in.p_IdUsuario);
					parmsIn.put("p_IdModulo", in.p_IdModulo);
					parmsIn.put("p_UsaReglasNegocio", in.p_UsaReglasNegocio);
					parmsIn.put("p_FechaMovimiento", FechaSistema);
					parmsIn.put("p_LiquidaComprobante","N");
					parmsIn.put("p_Entidad", "TES.Comprobante");
					parmsIn.put("p_FormasDePago", ObjFormaPago);
					parmsIn.put("p_RegistrosAProcesar", ObjRegistro);

					HashMap<String, Class> paramsOut = new HashMap<String, Class>();
					paramsOut.put("p_out_InformacionContador", Long.class);
					paramsOut.put("p_out_AdvertenciasContador", Long.class);
					paramsOut.put("p_out_ErroresContador", Long.class);

					// Realiza el call

					HashMap<String, Object> msj =  sql.call(con, SQL_CALL_MODIFICAR_FORMA_DE_PAGO, parmsIn, paramsOut);
					System.out.println(msj);
					SQL.exec(con, "DROP TABLE #p_RegistrosAProcesar");
					SQL.exec(con, "DROP TABLE #p_FormasDePago");
					
					if (msj.get("p_out_InformacionContador") != null) {
						out.P_OUT_MENSAJES.add(
								new MensajeSalida(null, MensajeSalida.MENSAJE_OK, "Comprobate" + IdComprobanteEntrada , "Forma de pago Modificada "));
					}
					IdComprobanteEntrada.clear();
							}
							IdNumeroReferencia.clear();
							NombreBancoEntrada.clear();

							ObjFormaPago.clear();
							ObjRegistro.clear();
						}
					}
					NombreBancoSalida.clear();
				}
			}
			IdComprobanteEntrada.clear();
		}

	
		// Rescato el IdcomprobanteFacturacion para luego consultar la cantidad y
		// realizar la segunda validacion
		HashMap<String, Object> idComprobanteFacturacion = new HashMap<>();
		HashMap<String, Object> idMovimientoTransito = new HashMap<>();
		HashMap<String, Object> idFormaPago = new HashMap<>();
		List<Long> idComprobanteF = new ArrayList<Long>();
		List<Long> IdComprobanteFormaPago = new ArrayList<Long>();
		for (ComprobanteFolioSalida folio : salidaComprobante) {
			idComprobanteF.add(folio.IdComprobanteFacturacion);

		}

		idComprobanteFacturacion.put("idComprobanteFacturacion", idComprobanteF);

		// Se rescata las cantidades para la segunda validacion
		List<CantidadFactura> cantidadFactura = sql.query(con, SQL_SELECT_CANTIDAD_FACTURA, idComprobanteFacturacion,
				CantidadFactura.class);
		for (ComprobanteFolioSalida comprobante_Folio_Salida : salidaComprobante) {

			// Primera Validacion
			log.write(comprobante_Folio_Salida.FolioComprobante + " - " + comprobante_Folio_Salida.MontoFactura + " - "
					+ comprobante_Folio_Salida.MontoTesoreria);
			BigDecimal suma = new BigDecimal(0);
			for (int i = 0; i < salidaComprobante.size(); i++) {
				if (comprobante_Folio_Salida.FolioComprobante.equals(salidaComprobante.get(i).FolioComprobante)) {
					suma = suma.add(salidaComprobante.get(i).MontoTesoreria);
				}
			}

			if (suma.compareTo(comprobante_Folio_Salida.MontoFactura) != 0) {
				log.write("no son iguales :" + comprobante_Folio_Salida.MontoFactura + " - " + suma);
				out.P_OUT_MENSAJES.add(new MensajeSalida(comprobante_Folio_Salida.FolioComprobante,
						MensajeSalida.MENSAJE_ERROR, " Montos",
						"Monto Factura " + comprobante_Folio_Salida.FolioComprobante + " Montos no son iguales "
								+ comprobante_Folio_Salida.MontoFactura + " - " + suma));

			}

			// Segunda Validacion
			for (CantidadFactura cF : cantidadFactura) {
				if (comprobante_Folio_Salida.IdComprobanteFacturacion.equals(cF.IdCfact)) {

					log.write(comprobante_Folio_Salida.FolioComprobante + ": " + cF.CantidadFactura + " - "
							+ cF.CantidadMovimiento);

					if (cF.CantidadFactura.compareTo(cF.CantidadMovimiento) != 0) {
						log.write(comprobante_Folio_Salida.FolioComprobante + " - "
								+ comprobante_Folio_Salida.IdComprobanteFacturacion + " : La cantidad no es igual : "
								+ cF.CantidadFactura + " - " + cF.CantidadMovimiento);
						out.P_OUT_MENSAJES.add(new MensajeSalida(comprobante_Folio_Salida.FolioComprobante,
								MensajeSalida.MENSAJE_ERROR, "Cantidad",
								"Cantidad Factura : " + comprobante_Folio_Salida.FolioComprobante + " - "
										+ cF.CantidadFactura + " - " + cF.CantidadMovimiento
										+ " La cantidad de facturacion no son iguales"));
					}
				}
			}

			if (comprobante_Folio_Salida.IdEstado.equalsIgnoreCase("LIQ")) {
				log.write(comprobante_Folio_Salida.FolioComprobante + " Liquidado");
				out.P_OUT_MENSAJES.add(new MensajeSalida(comprobante_Folio_Salida.FolioComprobante,
						MensajeSalida.MENSAJE_ERROR, "Factura", "Factura: " + comprobante_Folio_Salida.FolioComprobante
								+ " Con Problemas de Liquidacion " + salidaComprobante.size()));
			}

		}
		in.p_ComprobanteLiquidar.clear();

		idMovimientoTransito.put("FolioCoprobante", idComprobanteF);
		// Se rescata los datos para certificar los movimientos en trancito
		List<MovimientosCertifificar_Salida> movimientoTransito = sql.query(con, SQL_SELECT_TIPO_OPERACION,
				idMovimientoTransito, MovimientosCertifificar_Salida.class);
		List<MovimientosCertifificar_Salida> dataComprobanteCertificar = new ArrayList<>();
		for (MovimientosCertifificar_Salida data : movimientoTransito) {
			System.out.println(data.IdMovimientoCarteraEnTransito);
			System.out.println(data.IdTipoOperacion);
			MovimientosCertifificar_Salida comprobanteCertificar = new MovimientosCertifificar_Salida();
			comprobanteCertificar.IdMovimientoCarteraEnTransito = data.IdMovimientoCarteraEnTransito;
			comprobanteCertificar.IdTipoOperacion = data.IdTipoOperacion;
			dataComprobanteCertificar.add(comprobanteCertificar);

		}
		out.P_OUT_MOVIMIENTOSCERTIFICAR = dataComprobanteCertificar;
		
		List<ComprobanteLiquidar_Salida> dataComprobanteLiquidar = new ArrayList<>();

		for (int y = 0; y < salidaComprobante.size(); y++) {
			parametroComprobante.put("IdComprobante", salidaComprobante.get(y).IdComprobante);
			List<Datos_Usuario_Entrada> salidaUsuario = sql.query(con, SQL_SELECT_COMPROBANTE_MASIVO,
					parametroComprobante, Datos_Usuario_Entrada.class);
			for (Datos_Usuario_Entrada data : salidaUsuario) {
				System.out.println(data.IdComprobante);
				System.out.println(data.IdTipoMovimiento);
				ComprobanteLiquidar_Salida comprobanteLiquidar = new ComprobanteLiquidar_Salida();
				ComprobanteLiquidarEntrada comprobante = new ComprobanteLiquidarEntrada();
				comprobanteLiquidar.IdComprobante = data.IdComprobante;
				comprobanteLiquidar.IdTipoMovimiento = data.IdTipoMovimiento;
				comprobanteLiquidar.AfectaCuentaInversion = "";
				dataComprobanteLiquidar.add(comprobanteLiquidar);
				in.p_ComprobanteLiquidar.add(comprobante);
				IdComprobanteFormaPago.add(data.IdComprobante);

			}
			out.P_OUT_COMPROBANTELIQUIDAR = dataComprobanteLiquidar;
		}

		
		idFormaPago.put("IdComprobante", IdComprobanteFormaPago);
		// Se rescata los datos para certificar los movimientos en trancito
		List<FormaDePagoLiquidar_Salida> formaPago = sql.query(con, SQL_SELECT_FORMA_DE_PAGO,idFormaPago, FormaDePagoLiquidar_Salida.class);
		List<FormaDePagoLiquidar_Salida> dataFormaDePago = new ArrayList<>();
		for (FormaDePagoLiquidar_Salida data : formaPago) {
			System.out.println(data.IdComprobante);
			System.out.println(data.IdComprobanteFormaDePago);
			FormaDePagoLiquidar_Salida formaDePago = new FormaDePagoLiquidar_Salida();
			formaDePago.IdComprobante = data.IdComprobante;
			formaDePago.IdComprobanteFormaDePago = data.IdComprobanteFormaDePago;
			dataFormaDePago.add(formaDePago);

		}
		out.P_OUT_FORMADEPAGO = dataFormaDePago;
		
		
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		if (out.P_OUT_ERRORESCONTADOR.compareTo(0L) == 1) {
			return out;
		}
		return out;

	} // FIN if (lstCargoAbonoAGenerar.size() > 0)

	// -------------------------------------------------------------------------------------------
	// OBJETO DE TRANSPORTE
	// -------------------------------------------------------------------------------------------
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = getGsonBuilder();
//		LiquidarComprobante_Salida salida = new LiquidarComprobante_Salida();

		LiquidarComprobante_Entrada in = gson.fromJson(jin.toString(), LiquidarComprobante_Entrada.class);
		LiquidarComprobante_Salida out = logica(con, in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);

		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public LiquidarComprobante_Salida errores(Connection con, Long IdEmpresa, Long errores) throws Exception {
		LiquidarComprobante_Salida salida = new LiquidarComprobante_Salida();
		if (errores > 0) {
			salida.P_OUT_ERRORESCONTADOR = 1L;
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,
					"NO_SE_INGRESARON_DEPOSITOS_DE_TRASPASO", new HashMap<String, String>(), con, IdEmpresa));

		}

		return salida;

	}

//	public static class SalidaMensajesErrores {
//		public Long P_OUT_ERRORESCONTADOR;
//		public List<MensajeSalida> P_OUT_MENSAJES;
//		public List<ResultadoSolicitud> P_OUT_RESULTADO;
//		public List<MovimientosCertifificar_Salida> P_OUT_MOVIMIENTOSCERTIFICAR;
//		public List<ComprobanteLiquidar_Salida> 	P_OUT_COMPROBANTELIQUIDAR;
//		public List<FormaDePagoLiquidar_Salida> 	P_OUT_FORMADEPAGO;
//
//		public SalidaMensajesErrores() {
//			this.P_OUT_ERRORESCONTADOR = 0L;
//			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
//			this.P_OUT_RESULTADO = new ArrayList<ResultadoSolicitud>();
//			this.P_OUT_MOVIMIENTOSCERTIFICAR	 = new ArrayList<MovimientosCertifificar_Salida>();
//			this.P_OUT_COMPROBANTELIQUIDAR	 = new ArrayList<ComprobanteLiquidar_Salida>();
//			this.P_OUT_FORMADEPAGO			 = new ArrayList<FormaDePagoLiquidar_Salida>();
//		}
//
//		public String toString() {
//			return Utils.printJson(this, false);
//		}
//	}

	// -------------------------------------------------------------------------------------------
	// METODO PARA PRUEBAS LOCALES
	// -------------------------------------------------------------------------------------------

	public static void main(String[] args) throws Exception {
		System.out.println("== INICIO PRUEBA LOCAL ==");

		// try (Connection con = PlataformaUtils.getQAConnection()) {
		 try (Connection con = PlataformaUtils.getBOLSAConnection()) {
//		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = Utils.getGsonBuilder();

//			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":9,\"p_IdModulo\":\"ORD\",\"p_UsaReglasNegocio\":\"S\",\"p_IdTipoAsignacion\":\"ASIGNACION_POSTRERA\",\"p_IdTipo\":\"NORMAL\",\"p_Origen\":\"ASIG\","
//					+ "\"entradaArchivo\":[{\"folioTransaccion\":\"10141705199\",\"rutCliente\":\"1-9\",\"codigoPortafolio\":\"101010\",\"cantidadAsignar\":\"50\"},"
//					+ "{\"folioTransaccion\":\"210351\",\"rutCliente\":\"2-9\",\"codigoPortafolio\":\"80808\",\"cantidadAsignar\":\"70\"},"
//					+ "{\"folioTransaccion\":\"210351\",\"rutCliente\":\"2-9\",\"codigoPortafolio\":\"80808\",\"cantidadAsignar\":\"119\"}]}";

			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":9,\"p_IdModulo\":\"TES\",\"p_SimularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"S\",\"p_FechaMovimiento\":\"2020-02-25\",\"p_ComprobanteLiquidar\":"
					+ "[{\"IdComprobante\":432},{\"IdComprobante\":433}],\"p_Banco\":[{\"NombreCuentaBancaria\":\"SCOTPRINC \"},{\"NombreCuentaBancaria\":\"BICE31645 \"}],\"p_FormaDePagoLiquidar\":[]}";

			LiquidarComprobante_Entrada Objin = gson.fromJson(json, LiquidarComprobante_Entrada.class);
			LiquidacionMasivaComprobante demo = new LiquidacionMasivaComprobante();

			try {
				LiquidarComprobante_Salida ObjOut = demo.logica(con, Objin);
				ObjOut.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(ObjOut.P_OUT_MENSAJES);
				System.out.println(ObjOut.toString());
 				con.rollback();
				// con.commit();
			} catch (Exception er) {
				er.printStackTrace();
//				con.commit();
			}

// 			}
		}
		System.out.println("== FIN PRUEBA LOCAL ==");
	}
}