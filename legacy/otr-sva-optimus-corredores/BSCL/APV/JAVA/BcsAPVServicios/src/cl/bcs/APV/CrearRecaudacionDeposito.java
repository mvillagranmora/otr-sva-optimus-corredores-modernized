package cl.bcs.APV;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;

import cl.bcs.tesoreria.CrearIngresoEgreso;
import cl.bcs.tesoreria.CrearIngresoEgreso.CrearIngresoEgreso_Entrada;
import cl.bcs.tesoreria.CrearIngresoEgreso.CrearIngresoEgreso_Salida;
import cl.bcs.tesoreria.LiquidarComprobante;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;


public class CrearRecaudacionDeposito implements JavaCall   {

	static final Logger logger = LogManager.getLogger(CrearRecaudacionDeposito.class);


	public String SQL_CREAR_Recaudacion 		= loadResourceToString("cl/bcs/APV/sql/Deposito_Insert.sql");
	public String SQL_MODIFICAR_Recaudacion 	= loadResourceToString("cl/bcs/APV/sql/Deposito_Update.sql");	
	public String SQL_SELECT_EntidadRecaudadora 	= loadResourceToString("cl/bcs/APV/sql/ConsultaEntidadRecaudadora.sql");	
	public String SQL_SELECT_PersonaBanco 	= loadResourceToString("cl/bcs/APV/sql/ConsultarPersonaBanco.sql");	

	
	private final String SQL_SELECT_Recaudacion = " SELECT \"IdRecaudacion\"" + 
												"	FROM \"BSCL.APV.BD::REC.Recaudacion\"" + 
												"	WHERE  \"IdRecaudacion\" IN (:IdsRecaudacion)";

	
	

	
	private final String SQL_DatosEmpresa = " SELECT SOC.\"IdPersona\", CLI.\"IdPortafolioDefecto\"" + 
											" FROM \"BSCL.Plataforma.BD::PLA.Empresa\" AS EMP" + 
											" INNER JOIN \"BSCL.SocioNegocio.BD::SOC.SocioDeNegocio\" AS SOC ON (SOC.\"IdSocioDeNegocio\" = EMP.\"IdSocioDeNegocio\")" + 
											" INNER JOIN \"BSCL.SocioNegocio.BD::CLI.Cliente\" AS CLI ON ( CLI.\"IdPersona\" = SOC.\"IdPersona\" )" + 
											" WHERE EMP.\"IdEmpresa\" = :IdEmpresa";
	
	

	private final String SQL_SELECT_CuentaBanco = " SELECT \"IdCuentaBanco\", \"IdBanco\", \"NumeroCuentaBancaria\" from \"BSCL.Tesoreria.BD::TES.CuentaBanco\"" +
											 " WHERE \"IdBanco\" IN (:IdsBanco)" +
											 " AND \"NumeroCuentaBancaria\" IN (:NumerosCuentaBancaria)";

			
	//Obtiene caja tesoreria(PAC) destinada para recaudacion APV (si se cambia deba cambiarse en LiquidarComprobante.sql de tesoreria) 
	private final String SQL_SELECT_CAJA_RECAUDACION_TESORERIA = "SELECT \"IdCaja\", \"Codigo\", \"Nombre\" FROM \"BSCL.Tesoreria.BD::TES.Caja\" WHERE \"Codigo\" = 'PAC' AND \"IdEstado\" = 'ABT' AND \"IdEmpresa\" = :IdEmpresa";

	
	/********************
	 * Clases IN/OUT	*
	 ********************/
	
	public static class ParametrosRecaudacion{

		public Long IdInterno;
		public Long IdRecaudacion;
		public Long FolioCartola;
		public Long LineaCartola;
		public String Sucursal;
		public String FechaDeposito;
		public Long IdCuentaBanco;
		public Long IdBanco;
		public String CuentaCorriente;
		public BigDecimal MontoDeposito;
		public String Observacion;
		public Long NumeroDocumento;
		public String ConceptoRecaudacion;
		public String IdTipoDeposito;
		public Long IdCarga;
		public String IdEstado;
	    public ParametrosRecaudacion() {
	    	IdInterno = 0L;
	    	IdCuentaBanco = 0L;
	    }
	}
	
	public static class Recaudacion{

		public Long IdInterno;
		public Long IdRecaudacion;		
		public Long IdEmpresa;	
		public String CodigoArchivo;
		public Long FolioCartola;		
		public Long LineaCartola;		
		public String Sucursal;			
		public String FechaDeposito;	
		public Long IdCuentaBanco;	
		public Long IdBanco;			
		public String CuentaCorriente;	
		public BigDecimal MontoDeposito;		
		public String ConceptoRecaudacion;
		public String Origen;				
		public String Observacion;		
		public Long NumeroDocumento;	
		public String IdTipoDeposito;		
		public Long IdRegimenTributario;
		public Long IdCarga;
		public String IdEstado;
		
	    public Recaudacion() {
	    	IdInterno = 0L;
	    	IdCuentaBanco = 0L;
	    }
	    
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class DatosEmpresa{

		public Long IdPersona;
		public Long IdPortafolioDefecto;
	}

	public static class CuentaBanco{

		public Long IdCuentaBanco;
		public Long IdBanco;
		public String NumeroCuentaBancaria;
		
	}
	
	public static class DatosEntidad{

		public Long IdPersonaEntidad;
		public Long IdContratoSAAPV;
	}
	
	public static class DatosBanco{

		public Long IdPersonaBanco;
		public Long IdBanco;
	}
	
	//  SERVICIO ENTRADA
	public static class ParametrosEntradaServicio {
		
		public 	Long					p_IdEmpresa;
		public	Long					p_IdUsuario;
		public 	String					p_IdModulo;
		public	String					p_Tipo; //Individual Manual o Por Servicio
		public	String					p_Origen;
		
		public List<Recaudacion>		p_Parametros;
		
		public ParametrosEntradaServicio(){
			p_Parametros = new ArrayList<Recaudacion>();	
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}	

	// SERVICIO SALIDA
	public static class ParametrosSalidaServicio {		
		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;
		public Long					P_OUT_IDREC;
		public List<Resultado>		P_OUT_RESULTADO;
		public String toString() {
			return Utils.printJson(this, false);
		}
		
		public ParametrosSalidaServicio(){
			P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			P_OUT_RESULTADO = new ArrayList<Resultado>();
		}		
	};

	public static class CajaTesoreria 
	{    
	    public  Long	IdCaja;
	    public  String	Codigo;
	    public  String	Nombre;
	}
	public static class Resultado {
		public  Long		IdInterno;
	    public 	Long		IdRecaudacion;
		
	}
	
	//-----------------------------------------------------------------
	// CALL
	//-----------------------------------------------------------------	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		ParametrosEntradaServicio jin = gson.fromJson(in.toString() ,ParametrosEntradaServicio.class);
		ParametrosSalidaServicio out = logica(conn,jin);		
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	};
	
	
	
	//-----------------------------------------------------------------
	// LOGICA
	//-----------------------------------------------------------------
	
	public ParametrosSalidaServicio logica(Connection con,ParametrosEntradaServicio in)throws Exception {
		Long init = System.currentTimeMillis();
		SQL sql = new SQL();

		logger.info("INICIO:: CrearRecaudacionDeposito");
		
		String FechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		List<LiquidarComprobante.ComprobanteLiquidar_Entrada> lstComprobanteLiquidar = new ArrayList<LiquidarComprobante.ComprobanteLiquidar_Entrada>();
		LiquidarComprobante.ComprobanteLiquidar_Entrada objComprobanteLiquidar;
		
		HashMap<String,String> valDic = new HashMap<String,String>();
		valDic.put("a","n");
		
	
		ParametrosSalidaServicio 	salida 			= new ParametrosSalidaServicio ();
		ArrayList<CrearIngresoEgreso.IngresoEgreso_Entrada> paramsIngresoEgreso = new ArrayList<CrearIngresoEgreso.IngresoEgreso_Entrada>();
		ArrayList<CrearIngresoEgreso.FormaDePago_Entrada> paramsFormaDePago = new ArrayList<CrearIngresoEgreso.FormaDePago_Entrada>();
		
		List<CuentaBanco> lstCuentaBancoCorredora = new ArrayList<CuentaBanco>();
		
		salida.P_OUT_ERRORESCONTADOR = 0L;
		

		List<Long> idsRecaudacion = Utils.getSecuencia(con, "BSCL.APV.BD.Secuencia::REC.IdRecaudacion", (long)in.p_Parametros.size());

		
		//----------------------------------------------------------------------------
		//Se rescata el IdPersona y IdPortafolioDefecto de la corredora, esto se puede traer desde el JS u otros lados pero por tiempo se dejara aqui mientras
		HashMap<String,Object> ParamsDatosEmpresa = new HashMap<String,Object>();
		ParamsDatosEmpresa.put("IdEmpresa", in.p_IdEmpresa);
		
		logger.info("Inicio obtiene datos empresa");
		List<DatosEmpresa> lstDatosEmpresa = sql.query(con, SQL_DatosEmpresa, ParamsDatosEmpresa, DatosEmpresa.class);
		logger.info("Fin obtiene datos empresa");

		Long IdPortafolioCorredora 	= 0L;
		Long IdPersonaCorredora 	= 0L;
		
		if (lstDatosEmpresa.isEmpty())
		{
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"NO_SE_PUEDE_OBTENER_CONNFIGURACION_EMPRESA",null,con,in.p_IdEmpresa));
			salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
			return salida;
		}

		IdPortafolioCorredora 	= lstDatosEmpresa.get(0).IdPortafolioDefecto;
		IdPersonaCorredora 		= lstDatosEmpresa.get(0).IdPersona;	
		
		////SI es traspaso se busca informacion de la entidad recaudadora
		List<Long> IdContratosSAAPV = new ArrayList<Long>();
		for(CrearRecaudacionDeposito.Recaudacion fila : in.p_Parametros){
			IdContratosSAAPV.add(fila.NumeroDocumento);	
		}
		
		HashMap<String,Object> ParamsDatosEntidad = new HashMap<String,Object>();
		ParamsDatosEntidad.put("IdContratoSAAPV", IdContratosSAAPV);
		List<DatosEntidad> PersonaEntidad = sql.query(con, SQL_SELECT_EntidadRecaudadora, ParamsDatosEntidad, DatosEntidad.class);
		
		//----------------------------------------------------------------------------	
		//Obtiene caja tesoreria(PAC) destinada para recaudacion APV (si se cambia deba cambiarse en LiquidarComprobante.sql de tesoreria)
		logger.info("Inicio obtiene caja recaudacion");
		List<CajaTesoreria> cajaTesoreria = sql.query(con, SQL_SELECT_CAJA_RECAUDACION_TESORERIA, ParamsDatosEmpresa, CajaTesoreria.class);
		logger.info("Fin obtiene caja recaudacion");
		if (cajaTesoreria.size() == 0)
		{
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"CAJA_RECAUDACION_CERRADA_O_INEXISTENTE",null,con,in.p_IdEmpresa));
			salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
			return salida;
		}
		Long IdCajaRecaudacion = cajaTesoreria.get(0).IdCaja;

	

		int contadorIds = 0;
		List<Recaudacion> lstRecaudacion = new ArrayList<Recaudacion>();
		List<Long> lstIdRecaudacion = new ArrayList<Long>();		
		Recaudacion objRecaudacion;
		HashMap<String,Object> params = new HashMap<String,Object>();		
		List<String> lstNumeroCuentaCorredora = new ArrayList<String>();
		List<Long>   lstIdBanco = new ArrayList<Long>(); 
		
		
		for(CrearRecaudacionDeposito.Recaudacion fila : in.p_Parametros)
		{
			if (!(in.p_Tipo.compareTo("M")==0))
			{
				fila.IdRecaudacion = idsRecaudacion.get(contadorIds);
			}
			
						
			contadorIds = contadorIds+1;
			
			objRecaudacion = new Recaudacion();
			objRecaudacion.IdRecaudacion 		= fila.IdRecaudacion;
			objRecaudacion.IdEmpresa 			= in.p_IdEmpresa;
			objRecaudacion.CodigoArchivo 		= fila.CodigoArchivo;
			objRecaudacion.FolioCartola 		= fila.FolioCartola; 			
			objRecaudacion.LineaCartola 		= fila.LineaCartola; 						
			objRecaudacion.Sucursal 			= fila.Sucursal; 					
			objRecaudacion.FechaDeposito 		= fila.FechaDeposito; 				
			objRecaudacion.CuentaCorriente 		= fila.CuentaCorriente; 							
			objRecaudacion.MontoDeposito 		= fila.MontoDeposito;					
			objRecaudacion.ConceptoRecaudacion 	= fila.ConceptoRecaudacion;							
			objRecaudacion.Origen 				= in.p_Origen; 							
			objRecaudacion.Observacion 			= fila.Observacion;							
			objRecaudacion.NumeroDocumento 		= fila.NumeroDocumento; 					
			objRecaudacion.IdTipoDeposito 		= fila.IdTipoDeposito;
			objRecaudacion.IdBanco 				= fila.IdBanco;
			objRecaudacion.IdRegimenTributario 	= 0L;
			objRecaudacion.IdCarga 				= fila.IdCarga;
			objRecaudacion.IdEstado 			= fila.IdEstado;
			objRecaudacion.IdInterno 			= fila.IdInterno;

			objRecaudacion.IdCuentaBanco 		= fila.IdCuentaBanco;
			lstIdBanco.add(fila.IdBanco);
			lstRecaudacion.add(objRecaudacion);
			lstIdRecaudacion.add(fila.IdRecaudacion);
			
			//Para rescatar IdCuentaBanco de la corredora en caso de que no lo declaren
			if(in.p_Tipo.compareTo("I")==0)
			{
				if(fila.IdCuentaBanco.equals(0L))
				{	
					lstNumeroCuentaCorredora.add(fila.CuentaCorriente.trim().replaceFirst("^0*", ""));					
				}				
			}			
		}
		
		List<Recaudacion> lstRecaudacionOK = new ArrayList<Recaudacion>();
		HashMap<Long,Object> hashRecaudacionOK = new HashMap<Long,Object>();
		
		
		if (!lstRecaudacion.isEmpty())
		{
			int[] res = sql.batch(con, in.p_Tipo.compareTo("M")==0 ? SQL_MODIFICAR_Recaudacion : SQL_CREAR_Recaudacion, lstRecaudacion, Recaudacion.class);
		
			if(res==null || res.length==0)
			{
				salida.P_OUT_MENSAJES.add(new MensajeSalida((long) contadorIds,MensajeSalida.MENSAJE_ERROR,in.p_Tipo.compareTo("M")==0 ? "UI_MODIFICACION_DEPOSITO_ERROR" : "UI_CREACION_DEPOSITO_ERROR",valDic,con,in.p_IdEmpresa));
				salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
				return salida;
			}
			
			//Obtiene datos de las recudaciones correctamente insertadas
			params.clear();
			params.put("IdsRecaudacion", lstIdRecaudacion);
			logger.info("Inicio select recaudacion validas");
			lstRecaudacionOK  = sql.query(con, SQL_SELECT_Recaudacion, params, Recaudacion.class);
			logger.info("Fin select recaudacion validas");
			hashRecaudacionOK = Utils.List2Hash(lstRecaudacionOK, "IdRecaudacion");
			
		}
		
		//Se rescata el IdCuentaBanco de la corredora en caso de que no lo declaren
		if (!lstNumeroCuentaCorredora.isEmpty())
		{
			params.clear();
			params.put("IdsBanco", lstIdBanco);
			params.put("NumerosCuentaBancaria", lstNumeroCuentaCorredora);	
			logger.info("Inicio select cuenta bancaria corredora");
			lstCuentaBancoCorredora = sql.query(con, SQL_SELECT_CuentaBanco, params, CuentaBanco.class);
			logger.info("Fin select cuenta bancaria corredora");
			if (lstCuentaBancoCorredora.size()==0)
			{
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_BANCO_CORREDORA_Y_CUENTA_NO_VALIDOS",valDic,con,in.p_IdEmpresa));
				salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
				return salida;				
			}
		}
		//se buscan identificador de persona de la entidad bancaria que deposita
		HashMap<String,Object> ParamsDatosBanco = new HashMap<String,Object>();
		ParamsDatosBanco.put("IdsBanco", lstIdBanco);
		List<DatosBanco> PersonaBanco = sql.query(con, SQL_SELECT_PersonaBanco, ParamsDatosBanco, DatosBanco.class);
		
		

		//------------------------------------------------------------------
		//  PREPARA PARAMETROS PARA ENVIO A TESORERIA
		//------------------------------------------------------------------
		Resultado resultado;
		CrearIngresoEgreso.IngresoEgreso_Entrada objIngresoEgreso;
		CrearIngresoEgreso.FormaDePago_Entrada objFormaDePago;
		
		//Numero correlativo para ligar cada Recaudacion a sus respectivos movimientos de tesoreria y formas de pago.
		long numeroReferencia = 0L;
		for (Recaudacion fila :lstRecaudacion)
		{
			numeroReferencia++;
			Recaudacion infoRecaudacion = (Recaudacion) hashRecaudacionOK.get(fila.IdRecaudacion);
			
			if (infoRecaudacion != null)
			{				
				// Mesaje de salida (exito)();		
				String codigoMensajeExito = in.p_Tipo.compareTo("M")==0 ? "UI_MODIFICACION_DEPOSITO_OK" : "UI_CREACION_DEPOSITO_OK";
				String mensajeExito = Diccionario.getMensaje(con, in.p_IdEmpresa, codigoMensajeExito).add("IdRecaudacion", fila.IdRecaudacion).msg();
				salida.P_OUT_MENSAJES.add(new MensajeSalida(fila.IdRecaudacion, MensajeSalida.MENSAJE_OK,codigoMensajeExito, mensajeExito));
				
				
				// Prepara salida				
				resultado = new Resultado();
				resultado.IdInterno 	= fila.IdInterno;
				resultado.IdRecaudacion = fila.IdRecaudacion;				
				salida.P_OUT_RESULTADO.add(resultado);				
				salida.P_OUT_IDREC = fila.IdRecaudacion;
				
				if(in.p_Tipo.compareTo("I")==0){
	
					objIngresoEgreso 	= new CrearIngresoEgreso.IngresoEgreso_Entrada();
					objFormaDePago 		= new CrearIngresoEgreso.FormaDePago_Entrada();
					
					//Se crea movimiento de tesoreria con los datos de recaudacion
					objIngresoEgreso.NumeroReferencia = numeroReferencia;//fila.IdRecaudacion;
					objIngresoEgreso.AgruparEnComprobante = "N";
					objIngresoEgreso.Bilateral = "N";
					objIngresoEgreso.FechaComprobante = FechaSistema;
					objIngresoEgreso.FechaLiquidacion = FechaSistema;
					objIngresoEgreso.FechaLiquidacionComprobante = FechaSistema;
					objIngresoEgreso.FechaOperacion =  FechaSistema;
					objIngresoEgreso.FechaParidad = "0001-01-01";
					objIngresoEgreso.FolioCartera = 0L;
					objIngresoEgreso.GeneraComprobante = "S"; // Genera comprobante automaticamente
					objIngresoEgreso.Glosa = "";
					objIngresoEgreso.IdBolsa = "";
					objIngresoEgreso.IdCaja = IdCajaRecaudacion;
					objIngresoEgreso.IdClasificacionTipoInstrumento = "";
					
					if (in.p_Origen.equals("BONIF")){
						objIngresoEgreso.IdComprobante = 0L;
						objIngresoEgreso.IdConcepto = "INGBON";
					}
					else {
						objIngresoEgreso.IdComprobante = fila.IdRecaudacion;
						objIngresoEgreso.IdConcepto = "TRASPA".equals(in.p_Origen) ? "ATRAPV" : "ABNAPV";
					}
					
					objIngresoEgreso.IdCondicionLiquidacion = "";
					objIngresoEgreso.IdEmpleadoDigitador = 0L;
					objIngresoEgreso.IdInstrumento = 0L;
					objIngresoEgreso.IdMoneda = "CLP";
					objIngresoEgreso.IdMonedaOperacion = "";
					objIngresoEgreso.IdOperacion = fila.IdRecaudacion;
					objIngresoEgreso.IdPersona = IdPersonaCorredora;   
					
					if("TRASPA".equalsIgnoreCase(in.p_Origen)) {
						for(int j=0;j<PersonaEntidad.size() ; j++) {
							if(PersonaEntidad.get(j).IdContratoSAAPV.compareTo(fila.NumeroDocumento)==0) {
								objIngresoEgreso.IdPersonaTitularPago = PersonaEntidad.get(j).IdPersonaEntidad;
							}
						}
					}else {
						
						for(int i=0;i<PersonaBanco.size() ; i++) {
							if(PersonaBanco.get(i).IdBanco.compareTo(fila.IdBanco)==0) {
								objIngresoEgreso.IdPersonaTitularPago = PersonaBanco.get(i).IdPersonaBanco;
							}
						}
					}
					
					objIngresoEgreso.IdPortafolioTitularPago = IdPortafolioCorredora;
					objIngresoEgreso.IdPortafolio = IdPortafolioCorredora;
					
					objIngresoEgreso.IdSucursalEmpleadoDigitador = 0L;
					objIngresoEgreso.IdTipoComprobante = "APV";
					objIngresoEgreso.IdTipoInstrumento = "";
					objIngresoEgreso.IdTipoMovimiento = "MOVING";
					objIngresoEgreso.IdTipoOperacion = "INGTES";
					objIngresoEgreso.IdTipoPersona = "CLI";
					objIngresoEgreso.IdTipoPersonaTitularPago = "CLI";
					objIngresoEgreso.IdTipoProducto = "";
					objIngresoEgreso.IdTipoReajuste = "";
					objIngresoEgreso.Monto = fila.MontoDeposito;
					objIngresoEgreso.MontoOperacion = fila.MontoDeposito;
					objIngresoEgreso.MueveCuentaInversion = "S";
					objIngresoEgreso.SecuenciaAgrupacion = "";
					objIngresoEgreso.SecuenciaRelacionada = null;
					objIngresoEgreso.TitularPago = "S";
					objIngresoEgreso.ValorParidad = BigDecimal.valueOf(0);	
					paramsIngresoEgreso.add(objIngresoEgreso);
					
					//Se crea forma de pago correspondiente al movimiento de tesoreria con los datos de recaudacion
					objFormaDePago.NumeroReferencia = numeroReferencia;//fila.IdRecaudacion;
					objFormaDePago.NumeroReferenciaFormaDePago = fila.IdRecaudacion;
					objFormaDePago.FechaDocumentoPagoCorredora = "0001-01-01";
					objFormaDePago.FechaDocumentoPagoSocio = FechaSistema;//fila.FechaDeposito+" 00:00:00";
					objFormaDePago.IdBancoSocio = 0L;
					objFormaDePago.IdChequera = 0L;
					
					if(fila.IdCuentaBanco.compareTo(0L) == 0)
					{
						for (CuentaBanco cta: lstCuentaBancoCorredora)
						{
							if (fila.IdBanco.compareTo(cta.IdBanco) == 0 && fila.CuentaCorriente.trim().replaceFirst("^0*", "").equals(cta.NumeroCuentaBancaria.trim().replaceFirst("^0*", "")))
							{
								fila.IdCuentaBanco = cta.IdCuentaBanco;							
							}
						}
					}
					
					objFormaDePago.IdCuentaBancariaCorredora = fila.IdCuentaBanco;					
					objFormaDePago.IdCuentaBancariaSocio = 0L;
					objFormaDePago.IdMedioDePago = fila.IdTipoDeposito;
					objFormaDePago.IdModalidadDePago = "MODDEP";
					objFormaDePago.IdMoneda = "CLP";
					objFormaDePago.IdTarjetaBancariaCorredora = 0L;
					objFormaDePago.IdTarjetaBancariaSocio = 0L;
					objFormaDePago.IdTipoCuentaBancariaSocio = "";
					objFormaDePago.IdTipoTarjetaBancariaSocio = "";
					objFormaDePago.Monto = fila.MontoDeposito;
					objFormaDePago.NombreCuentaBancariaSocio = "";
					objFormaDePago.NombreTipoCuentaBancariaSocio = "";
					objFormaDePago.NombreTipoTarjetaBancariaSocio = "";
					objFormaDePago.NumeroCuentaBancariaSocio = "";
					objFormaDePago.NumeroDocumentoCorredora = 0L;
					objFormaDePago.NumeroDocumentoSocio = 0L;
					objFormaDePago.NumeroSerieSocio = "";
					objFormaDePago.NumeroTarjetaBancariaSocio = "";
					
					paramsFormaDePago.add(objFormaDePago);
				}
				
			}
			
		}
		

		
		
		
		
		//------------------------------------------------------------------
		//  ENVIO A TESORERIA (Movimiento y comprobante)
		//------------------------------------------------------------------
	    if (!paramsIngresoEgreso.isEmpty())
	    {
	    	
	    	CrearIngresoEgreso servicioCrearIngresoEgreso = new CrearIngresoEgreso();
		    CrearIngresoEgreso_Entrada paramsIngresoEgreso_Entrada = new CrearIngresoEgreso_Entrada();

		    
		    paramsIngresoEgreso_Entrada.p_IdEmpresa 			= in.p_IdEmpresa;
		    paramsIngresoEgreso_Entrada.p_IdUsuario 			= in.p_IdUsuario;
		    paramsIngresoEgreso_Entrada.p_IdModulo 				= in.p_IdModulo;
		    paramsIngresoEgreso_Entrada.p_SimularOperacion 		= "N";
		    paramsIngresoEgreso_Entrada.p_UsaReglasNegocio 		= "S";
		    paramsIngresoEgreso_Entrada.p_EsIngresoAutomatico 	= "N";
		    paramsIngresoEgreso_Entrada.p_FormaDePago 			= paramsFormaDePago;
		    paramsIngresoEgreso_Entrada.p_IngresoEgresoGenerar 	= paramsIngresoEgreso;
		    
		    logger.info("Inicio generar movimientos y comprobantes de tesoreria");
		    CrearIngresoEgreso_Salida OutTesoreria = servicioCrearIngresoEgreso.logica(con, paramsIngresoEgreso_Entrada); 
		    logger.info("Fin generar movimientos y comprobantes de tesoreria");
		    
		    salida.P_OUT_MENSAJES.addAll(OutTesoreria.P_OUT_MENSAJES);


			if (MensajeSalida.getNumeroErrores(OutTesoreria.P_OUT_MENSAJES) > 0) 
			{
				salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
				return salida;
			}
			else
			{

				for(CrearIngresoEgreso.ResultadoProceso_Salida outResultado :OutTesoreria.P_OUT_RESULTADOPROCESO){

					
					//Obtiene los Id de los comprobantes de tesoreria generados para posteriormente liquidarlos
					if(outResultado.Campo.equals("IdComprobante") && outResultado.Resultado.equals("ING") && outResultado.Servicio.equals("BSCL.Tesoreria.BD.Servicios::CrearComprobante") )
					{						
						objComprobanteLiquidar = new LiquidarComprobante.ComprobanteLiquidar_Entrada();						
						objComprobanteLiquidar.IdComprobante 		 = outResultado.Id;
						objComprobanteLiquidar.IdTipoMovimiento 	 = "MOVING";
						objComprobanteLiquidar.AfectaCuentaInversion = "CLI";						
						lstComprobanteLiquidar.add(objComprobanteLiquidar);
					}
						
						
				};	
			}

	    }
	    


   
			//-----------------------------------------------------------------
			//  LIQUIDACION TESORERIA
			//-----------------------------------------------------------------			
			
			//Obtencion de parametros APV para la validacion
			String valorParametroLiquidacion = ConfiguracionSistema.getParametroConfiguracionString(con,in.p_IdEmpresa, "APV","REC.LiquidacionAutomatica");
			
			if (valorParametroLiquidacion.equals("S") && lstComprobanteLiquidar.size() > 0)
			{
				
				LiquidarComprobante.LiquidarComprobante_Entrada paramsLiquidar_Entrada = new LiquidarComprobante.LiquidarComprobante_Entrada();			
				
				paramsLiquidar_Entrada.p_IdEmpresa 			 = in.p_IdEmpresa;
				paramsLiquidar_Entrada.p_IdUsuario 			 = in.p_IdUsuario;
				paramsLiquidar_Entrada.p_IdModulo 			 = in.p_IdModulo;
				paramsLiquidar_Entrada.p_UsaReglasNegocio 	 = "S";
				paramsLiquidar_Entrada.p_FechaMovimiento 	 = FechaSistema.toString();
				paramsLiquidar_Entrada.p_ComprobanteLiquidar = lstComprobanteLiquidar;
				

				LiquidarComprobante ProcesoLiquidar = new LiquidarComprobante();
				
				logger.info("Inicio liquidar comprobantes de tesoreria");
				LiquidarComprobante.LiquidarComprobante_Salida OutLiquidar = ProcesoLiquidar.logica(con, paramsLiquidar_Entrada); 
				logger.info("Fin liquidar comprobantes de tesoreria");
				
				salida.P_OUT_MENSAJES.addAll(OutLiquidar.P_OUT_MENSAJES);
				
				if (MensajeSalida.getNumeroErrores(OutLiquidar.P_OUT_MENSAJES) > 0) 
				{
					salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
					return salida;
				}
		
			 }
				 
	    
		logger.info("FIN:: CrearRecaudacionDeposito, tiempo:" + (System.currentTimeMillis() - init));
		
	
		return salida;
	}
	
	
	
	
	//-----------------------------------------------------------------
	//  METODO PRUEBAS LOCALES
	//-----------------------------------------------------------------

	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getQAConnection()) { //getMBConnection()) {
			try {
				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();				//
				
				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":51,\"p_IdModulo\":\"APV\",\"p_Tipo\":\"I\",\"p_Origen\":\"TRASPA\",\"p_Parametros\":[{\"IdRecaudacion\":963,\"IdEmpresa\":1,\"FolioCartola\":1,\"LineaCartola\":1,\"Sucursal\":4,\"FechaDeposito\":\"2018-05-09\",\"CuentaCorriente\":\"13602227\",\"MontoDeposito\":1900,\"ConceptoRecaudacion\":\"\",\"Origen\":\"CAJSURA\",\"Observacion\":\"AS\",\"NumeroDocumento\":1,\"IdTipoDeposito\":\"TRA\",\"IdBanco\":12,\"IdCarga\":0,\"IdRegimenTributario\":0,\"IdEstado\":\"ING\"}]}";
				ParametrosEntradaServicio Objin = gson.fromJson(json, ParametrosEntradaServicio.class);
				
				CrearRecaudacionDeposito demo = new CrearRecaudacionDeposito();	
				
	 			System.out.println("ENTRADA");
	 			System.out.println(Objin.toString());
	 			System.out.println("SALIDA");
	 			
	 			try { 
	 				ParametrosSalidaServicio ObjOut = demo.logica(con, Objin);
	 				con.rollback();
	 				System.out.println(ObjOut.toString());
	 			} 
	 			catch(Exception er) {
					er.printStackTrace();	
					con.rollback();
				}
	 			
		} catch (Exception e) {
			e.printStackTrace();			
		}	
		System.out.println("== END ==");
		};
	}
	
}