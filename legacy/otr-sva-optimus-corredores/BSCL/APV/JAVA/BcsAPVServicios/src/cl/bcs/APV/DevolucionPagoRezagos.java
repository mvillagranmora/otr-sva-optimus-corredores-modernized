package cl.bcs.APV;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

//Recaudacion
import cl.bcs.APV.CambiarEstadoRecaudacion;
import cl.bcs.APV.ProcesarDistribucion.ParametrosRecaudacion;
import cl.bcs.tesoreria.CrearComprobante;
//Tesoreria
import cl.bcs.tesoreria.CrearIngresoEgreso;
import cl.bcs.tesoreria.LiquidarComprobante;
import cl.bcs.tesoreria.CrearIngresoEgreso.CrearIngresoEgreso_Entrada;
import cl.bcs.tesoreria.CrearIngresoEgreso.CrearIngresoEgreso_Salida;
import cl.bcs.cuentaInversion.CrearCargoAbono;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.AgregarLog.InAgregarLog;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;


public class DevolucionPagoRezagos implements JavaCall   {

	/*
	 * El log
	 */	
	private final Log log = new Log("DevolucionPagoRezagos");
	
	static final Logger logger = LogManager.getLogger("DevolucionPagoRezagos");
	/*
	 * Queries que van a ser utilizadas por el servicio. 
	 */	
	
	public String SQL_SELECT_Recaudacion = loadResourceToString("cl/bcs/APV/sql/ConsultarDetalleRecaudacionADevolver.sql");
	
	public String SQL_SELECT_Datos_Bancarios = loadResourceToString("cl/bcs/APV/sql/ConsultarDatosCuentaBancaria.sql");

	private final String SQL_DatosEmpresa = "SELECT Empresa.\"IdPersona\",\"IdPortafolioDefecto\" FROM \"BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa\" Empresa"
													+ " INNER JOIN \"BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente\" Cliente"
													+ " ON Cliente.\"IdSocioDeNegocio\" = Empresa.\"IdSocioDeNegocio\""
													+ " WHERE Empresa.\"IdEmpresa\" = :IdEmpresa";

	private final String SQL_PortafolioEmpresa = "SELECT \"IdPortafolioDefecto\" FROM \"BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa\" Empresa"
													+ " INNER JOIN \"BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente\" Cliente"
													+ " ON Cliente.\"IdSocioDeNegocio\" = Empresa.\"IdSocioDeNegocio\""
													+ " WHERE Empresa.\"IdEmpresa\" = :IdEmpresa";
	
	private final String SQL_CtaBco = "SELECT \"DestinoCtaBco\" FROM \"BSCL.APV.BD::APV.OrigenDestinoTransicion\" WHERE \"Operacion\" = 'DEV' AND \"IdEmpresa\" = :IdEmpresa";
	/********************
	 * Clases IN/OUT	*
	 ********************/

	//  SERVICIO ENTRADA
	public static class ParametrosEntradaServicio {
		
		public 	Long					p_IdEmpresa;
		public	Long					p_IdUsuario;
		public 	String					p_IdModulo;
		public 	String					p_DevolucionTipoPersona;
		public	Long					p_DevolucionIdPersona;
		public	Long					p_DevolucionIdPortafolio;
		public	String					p_DevolucionIdMedioPago;
		public	Long					p_DevolucionIdBanco;
		public	Long					p_DevolucionIdCuentaCorriente;
		
		
		public List<ParametrosRecaudacion>		p_Parametros;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		public ParametrosEntradaServicio(){
			p_DevolucionIdPortafolio = 0L;
		}
	}			

	// SERVICIO SALIDA
	public static class ParametrosSalidaServicio {		
		public List<MensajeSalida>									P_OUT_MENSAJES;
		public Long													P_OUT_ERRORESCONTADOR;

		public List<CrearIngresoEgreso.IngresoEgreso_Entrada>		p_IngresoEgreso;
		public List<CrearIngresoEgreso.FormaDePago_Entrada> 		p_FormaDePago;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
		public ParametrosSalidaServicio(){
			this.P_OUT_MENSAJES 	   = new ArrayList<MensajeSalida>();
			this.p_IngresoEgreso 	   = new ArrayList<CrearIngresoEgreso.IngresoEgreso_Entrada>();
			this.p_FormaDePago 		   = new ArrayList<CrearIngresoEgreso.FormaDePago_Entrada>();
			this.P_OUT_ERRORESCONTADOR =	0L;
		}		
	};

	public static class ParametrosRecaudacion{

		public Long IdRecaudacionCliente;
	}

	public static class DatosEmpresa{

		public Long IdPersona;
		public Long IdPortafolioDefecto;
	}

	public static class IdPortafolioDefecto{

		public Long IdPortafolioDefecto;
	}

	public static class OrigenCtaBco{

		public Long OrigenCtaBco;
	}
	public static class DestinoCtaBco{

		public Long DestinoCtaBco;
	}
	
	public static class Recaudacion{

		public Long FolioRecaudacion;
		public Long IdRecaudacionCliente;
		public Long IdRecaudacionEmpleador;			
		public Long IdPersonaCliente;				
		public Long IdCliente;			
		public String RutEmpleador;		
		public Date FechaDeposito;				
		public BigDecimal RecTotal;				
		public Long IdBanco;			
		public Long IdContrato;	
		public Long IdPortafolio;	
		public String IdRegimen;	
		public String IdTipoDeposito;				
		public String NombreTipoDeposito;		
		public Long CuentaCorriente;			
		public String CodigoEstado;		
		public String Observacion;	
	}
	
	public static class DatosBancarios{
		public Long IdCuentaBancaria;
		public Long IdBanco;
		public String IdTipoCuentaBancaria;
		public String IdMoneda;
		public String NumeroCuentaBancaria;
		public String NombreTipoCuentaBancaria;
		
		public DatosBancarios(){
			this.IdCuentaBancaria = 0L;
			this.IdBanco = 0L;
			this.IdTipoCuentaBancaria = "";
			this.IdMoneda = "";
			this.NumeroCuentaBancaria = "";
			this.NombreTipoCuentaBancaria = "";
		}
	}
	
	
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
	
	/************************
	 * Métodos Básicos	*	
	 ************************/
	
	public ParametrosSalidaServicio logica(Connection con,ParametrosEntradaServicio in)throws Exception {
		ParametrosSalidaServicio out = new ParametrosSalidaServicio();

		SQL sql = new SQL();
		ParametrosSalidaServicio 	salida 			= new ParametrosSalidaServicio ();
		//java.sql.Date FechaSistema = new java.sql.Date(time);
		String FechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		//MensajeSalida               msg             = new MensajeSalida();
		//String ClientesBuscados = "";
		Long contador = 1L;
		//Long IdPortafolioCorredora = 0L;
		Long IdCtaBco = 0L;
		
		BigDecimal sumaMontos = BigDecimal.valueOf(0);

		HashMap<String,Object> ParamsSelectRecaudacion = new HashMap<String,Object>();
		ArrayList<Long> ClientesBuscados = new ArrayList<Long>();

			for(ParametrosRecaudacion fila : in.p_Parametros){
				
				ClientesBuscados.add(fila.IdRecaudacionCliente);	
			}
			ParamsSelectRecaudacion.put("IdRecaudacionClienteGrupo", ClientesBuscados);

		HashMap<String,Object> ParamsSelectPortafolio = new HashMap<String,Object>();
		HashMap<String,Object> ParamsSelectCtaBco = new HashMap<String,Object>();
		ParamsSelectPortafolio.put("IdEmpresa", in.p_IdEmpresa);
		ParamsSelectCtaBco.put("IdEmpresa", in.p_IdEmpresa);

		List<Recaudacion> _Recaudacion = sql.query(con, SQL_SELECT_Recaudacion, ParamsSelectRecaudacion, Recaudacion.class);
		List<IdPortafolioDefecto> _IdPortafolioDefecto = sql.query(con, SQL_PortafolioEmpresa, ParamsSelectPortafolio, IdPortafolioDefecto.class);
		List<DestinoCtaBco> _CtaBco = sql.query(con, SQL_CtaBco, ParamsSelectPortafolio, DestinoCtaBco.class);
		
		//select info cuenta bancaria
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("IdCuentaBancaria", in.p_DevolucionIdBanco);
		List<DatosBancarios> DatosBancarios = sql.query(con, SQL_SELECT_Datos_Bancarios, params, DatosBancarios.class);

		//IdPortafolioCorredora = _IdPortafolioDefecto.get(0).IdPortafolioDefecto;
		IdCtaBco = _CtaBco.get(0).DestinoCtaBco;

		//Se rescata el IdPersona y IdPortafolioDefecto de la corredora, esto se puede traer desde el JS u otros lados pero por tiempo se dejara aqui mientras
			HashMap<String,Object> ParamsDatosEmpresa = new HashMap<String,Object>();
			ParamsDatosEmpresa.put("IdEmpresa", in.p_IdEmpresa);
				
			List<DatosEmpresa> _DatosEmpresa = sql.query(con, SQL_DatosEmpresa, ParamsDatosEmpresa, DatosEmpresa.class);

			Long IdPortafolioCorredora = _DatosEmpresa.get(0).IdPortafolioDefecto;
			Long IdPersonaCorredora = _DatosEmpresa.get(0).IdPersona;
		//FIN 
		//Arreglos
		ArrayList<CrearIngresoEgreso.IngresoEgreso_Entrada> paramsIngresoEgreso = new ArrayList<CrearIngresoEgreso.IngresoEgreso_Entrada>();
		ArrayList<CrearIngresoEgreso.FormaDePago_Entrada> paramsFormaDePago = new ArrayList<CrearIngresoEgreso.FormaDePago_Entrada>();
		List<CrearComprobante.FormaDePago_Entrada> paramsComprobanteFormaDePago_Entrada = new ArrayList<CrearComprobante.FormaDePago_Entrada>();
		List<CambiarEstadoRecaudacion.ParametrosRecaudacionEstado> paramsCambioEstado = new ArrayList<CambiarEstadoRecaudacion.ParametrosRecaudacionEstado>();
		
		for(Recaudacion fila : _Recaudacion){

			//obtejos de tesoreria y ordenes
			CrearIngresoEgreso.IngresoEgreso_Entrada IngresoEgreso = new CrearIngresoEgreso.IngresoEgreso_Entrada(); //IngresoEgreso
		    CambiarEstadoRecaudacion.ParametrosRecaudacionEstado RecaudacionEstado = new CambiarEstadoRecaudacion.ParametrosRecaudacionEstado();
			CrearIngresoEgreso.FormaDePago_Entrada FormaDePago = new CrearIngresoEgreso.FormaDePago_Entrada();       //FormaPagoTeso
			CrearComprobante.FormaDePago_Entrada ComprobanteFormaDePago = new CrearComprobante.FormaDePago_Entrada();//ComprobanteFormaPago
			
			//Sumatoria de montos para posteriores usos
			sumaMontos = sumaMontos.add(fila.RecTotal);
			
			//Se crea movimiento de tesoreria con los datos de recaudacion
			IngresoEgreso.NumeroReferencia = fila.IdRecaudacionCliente; 
			IngresoEgreso.AgruparEnComprobante = "N";
			IngresoEgreso.Bilateral = "N";
			IngresoEgreso.FechaComprobante = "0001-01-01";
			IngresoEgreso.FechaLiquidacion = FechaSistema;//fila.FechaDeposito.toString();
			IngresoEgreso.FechaLiquidacionComprobante = "0001-01-01";
			IngresoEgreso.FechaOperacion = fila.FechaDeposito + " 00:00:00";
			IngresoEgreso.FechaParidad = "0001-01-01";
			IngresoEgreso.FolioCartera = 0L;
			IngresoEgreso.GeneraComprobante = "N";
			IngresoEgreso.Glosa = "";
			IngresoEgreso.IdBolsa = "";
			IngresoEgreso.IdCaja = 0L;
			IngresoEgreso.IdClasificacionTipoInstrumento = "";
			IngresoEgreso.IdComprobante = fila.FolioRecaudacion;
			IngresoEgreso.IdConcepto = "DEVRZG";
			IngresoEgreso.IdCondicionLiquidacion = "";
			IngresoEgreso.IdEmpleadoDigitador = 0L;
			IngresoEgreso.IdInstrumento = 0L;
			IngresoEgreso.IdMoneda = "CLP";
			IngresoEgreso.IdMonedaOperacion = "";
			IngresoEgreso.IdOperacion = fila.IdRecaudacionCliente;
			IngresoEgreso.IdPersona = fila.IdPersonaCliente; //fila.IdPersona;
			IngresoEgreso.IdPersonaTitularPago = in.p_DevolucionIdPersona; //fila.IdPersonaCliente;//fila.IdPersona;
			IngresoEgreso.IdPortafolio = 0L;//IdPortafolioCorredora;//in.p_DevolucionIdPortafolio; //fila.IdPortafolio;
			IngresoEgreso.IdPortafolioTitularPago = 0L;//IdPortafolioCorredora;
			IngresoEgreso.IdSucursalEmpleadoDigitador = 0L;
			IngresoEgreso.IdTipoComprobante = "APV";
			IngresoEgreso.IdTipoInstrumento = "";
			IngresoEgreso.IdTipoMovimiento = "MOVEGR";
			IngresoEgreso.IdTipoOperacion = "EGRTES";
			IngresoEgreso.IdTipoPersona = "PER";
			IngresoEgreso.IdTipoPersonaTitularPago = "PER";
			IngresoEgreso.IdTipoProducto = "";
			IngresoEgreso.IdTipoReajuste = "";
			IngresoEgreso.Monto = fila.RecTotal;
			IngresoEgreso.MontoOperacion = fila.RecTotal;
			IngresoEgreso.MueveCuentaInversion = "S";
			IngresoEgreso.SecuenciaAgrupacion = "";
			IngresoEgreso.SecuenciaRelacionada = null;
			IngresoEgreso.TitularPago = "S";
			IngresoEgreso.ValorParidad = BigDecimal.valueOf(0);	
			paramsIngresoEgreso.add(IngresoEgreso);
			
			//Se crea forma de pago correspondiente al movimiento de tesoreria con los datos de recaudacion
			
			FormaDePago.NumeroReferencia = fila.IdRecaudacionCliente;
			FormaDePago.NumeroReferenciaFormaDePago = fila.IdRecaudacionCliente;
			FormaDePago.FechaDocumentoPagoCorredora = "0001-01-01";
			FormaDePago.FechaDocumentoPagoSocio = FechaSistema.toString()+" 00:00:00";
			FormaDePago.IdBancoSocio = DatosBancarios.get(0).IdBanco;
			FormaDePago.IdChequera = 0L;
			FormaDePago.IdCuentaBancariaCorredora = IdCtaBco; //122L
			FormaDePago.IdCuentaBancariaSocio = 0L;
			FormaDePago.IdMedioDePago = in.p_DevolucionIdMedioPago;
			FormaDePago.IdModalidadDePago = "MODDEP";
			FormaDePago.IdMoneda = "CLP";
			FormaDePago.IdTarjetaBancariaCorredora = 0L;
			FormaDePago.IdTarjetaBancariaSocio = 0L;
			FormaDePago.IdTipoCuentaBancariaSocio =  DatosBancarios.get(0).IdTipoCuentaBancaria;
			FormaDePago.IdTipoTarjetaBancariaSocio = "";
			FormaDePago.Monto = fila.RecTotal;
			FormaDePago.NombreCuentaBancariaSocio = "";
			FormaDePago.NombreTipoCuentaBancariaSocio =  DatosBancarios.get(0).NombreTipoCuentaBancaria;
			FormaDePago.NombreTipoTarjetaBancariaSocio = "";
			FormaDePago.NumeroCuentaBancariaSocio = DatosBancarios.get(0).NumeroCuentaBancaria;
			FormaDePago.NumeroDocumentoCorredora = 0L;
			FormaDePago.NumeroDocumentoSocio = 0L;
			FormaDePago.NumeroSerieSocio = "";
			FormaDePago.NumeroTarjetaBancariaSocio = "";
			
			paramsFormaDePago.add(FormaDePago);
						
			//Se crea forma de pago correspondiente al comprobante de tesoreria con los datos de recaudacion
			
			ComprobanteFormaDePago.NumeroReferencia = 1L;
			ComprobanteFormaDePago.IdMedioDePago = fila.IdTipoDeposito;
			ComprobanteFormaDePago.IdModalidadDePago ="MODDEP";
			ComprobanteFormaDePago.IdMoneda ="CLP";
			ComprobanteFormaDePago.Monto =fila.RecTotal;
			ComprobanteFormaDePago.IdCuentaBancariaCorredora = IdCtaBco; //=129L;
			ComprobanteFormaDePago.IdTarjetaBancariaCorredora =0L;
			ComprobanteFormaDePago.IdChequera =0L;
			ComprobanteFormaDePago.NumeroDocumentoCorredora =0L;
			ComprobanteFormaDePago.FechaDocumentoPagoCorredora ="0001-01-01";
			ComprobanteFormaDePago.IdCuentaBancariaSocio =0L;
			ComprobanteFormaDePago.IdBancoSocio = DatosBancarios.get(0).IdBanco;
			ComprobanteFormaDePago.NombreCuentaBancariaSocio ="";
			ComprobanteFormaDePago.NumeroCuentaBancariaSocio = DatosBancarios.get(0).NumeroCuentaBancaria;
			ComprobanteFormaDePago.IdTipoCuentaBancariaSocio = DatosBancarios.get(0).IdTipoCuentaBancaria;
			ComprobanteFormaDePago.NombreTipoCuentaBancariaSocio = DatosBancarios.get(0).NombreTipoCuentaBancaria;
			ComprobanteFormaDePago.IdTarjetaBancariaSocio =0L;
			ComprobanteFormaDePago.IdTipoTarjetaBancariaSocio ="";
			ComprobanteFormaDePago.NombreTipoTarjetaBancariaSocio ="";
			ComprobanteFormaDePago.NumeroTarjetaBancariaSocio ="";
			ComprobanteFormaDePago.NumeroSerieSocio ="";
			ComprobanteFormaDePago.NumeroDocumentoSocio =0L;
			ComprobanteFormaDePago.FechaDocumentoPagoSocio =FechaSistema.toString();
			
			paramsComprobanteFormaDePago_Entrada.add(ComprobanteFormaDePago);

			RecaudacionEstado.FolioRecaudacion = 0L; //No debe actualizar estado del deposito
			RecaudacionEstado.IdRecaudacionEmpleador = fila.IdRecaudacionEmpleador;
			RecaudacionEstado.IdRecaudacionCliente = fila.IdRecaudacionCliente;
			RecaudacionEstado.IdEstado = "DEV";
			paramsCambioEstado.add(RecaudacionEstado);
			
		}

		TraspasoCuentasBancarias.ParametrosEntradaServicio paramsTrasCtaBca_Entrada = new TraspasoCuentasBancarias.ParametrosEntradaServicio();
		    
			paramsTrasCtaBca_Entrada.p_IdEmpresa = in.p_IdEmpresa;
			paramsTrasCtaBca_Entrada.p_IdUsuario = in.p_IdUsuario;
		    paramsTrasCtaBca_Entrada.p_IdModulo = in.p_IdModulo;
		    paramsTrasCtaBca_Entrada.p_Opcion = "DEV";
		    paramsTrasCtaBca_Entrada.p_Fecha = FechaSistema.toString();
		    paramsTrasCtaBca_Entrada.p_Monto = sumaMontos;		    
		    
		    if (sumaMontos.compareTo(BigDecimal.ZERO)== 0) {
		    	out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"MONTO_TOTAL_RECAUDADO_IGUAL_CERO",null,con,in.p_IdEmpresa));
		    	out.P_OUT_ERRORESCONTADOR++;
		    	return out;
		    }
		    
		    
		   
		    TraspasoCuentasBancarias procesoTrasCtaBca = new TraspasoCuentasBancarias();
			TraspasoCuentasBancarias.ParametrosSalidaServicio OutTrasCtaBca_Entrada = procesoTrasCtaBca.logica(con, paramsTrasCtaBca_Entrada);
			logger.info("SALIDA::TraspasoCuentasBancarias.OutTrasCtaBca_Entrada");
			 System.out.println("SALIDA::TraspasoCuentasBancarias.OutTrasCtaBca_Entrada");
			 
			out.P_OUT_ERRORESCONTADOR += MensajeSalida.getNumeroErrores(OutTrasCtaBca_Entrada.P_OUT_MENSAJES);
			
			if(out.P_OUT_ERRORESCONTADOR > 0) {
				List<MensajeSalida> mensajesErrores = OutTrasCtaBca_Entrada.P_OUT_MENSAJES.parallelStream().filter(mensaje->MensajeSalida.MENSAJE_ERROR.equals(mensaje.tipo)).collect(Collectors.toList());
				out.P_OUT_MENSAJES.addAll(mensajesErrores);
				return out;
			}
		 
			 // con.commit();
		    CrearIngresoEgreso_Entrada paramsIngresoEgreso_Entrada = new CrearIngresoEgreso_Entrada();
		    
		    paramsIngresoEgreso_Entrada.p_IdEmpresa = in.p_IdEmpresa;
		    paramsIngresoEgreso_Entrada.p_IdUsuario = in.p_IdUsuario;
		    paramsIngresoEgreso_Entrada.p_IdModulo = in.p_IdModulo;
		    paramsIngresoEgreso_Entrada.p_SimularOperacion = "N";
		    paramsIngresoEgreso_Entrada.p_UsaReglasNegocio = "S";
		    paramsIngresoEgreso_Entrada.p_EsIngresoAutomatico = "N";
		    paramsIngresoEgreso_Entrada.p_FormaDePago = paramsFormaDePago;
		    paramsIngresoEgreso_Entrada.p_IngresoEgresoGenerar = paramsIngresoEgreso;
		    
		    CrearIngresoEgreso procesoIngresoEgreso = new CrearIngresoEgreso();
			List<CrearComprobante.IngresoEgresoGenerar_Entrada> paramsComprobanteIngresoEgreso_Entrada = new ArrayList<CrearComprobante.IngresoEgresoGenerar_Entrada>();
		    CrearIngresoEgreso_Salida OutTesoreria = procesoIngresoEgreso.logica(con, paramsIngresoEgreso_Entrada); 
		    
		    logger.info("SALIDA::CrearIngresoEgreso.procesoIngresoEgreso");
		    System.out.println("SALIDA::CrearIngresoEgreso.procesoIngresoEgreso");
			 
			out.P_OUT_ERRORESCONTADOR += MensajeSalida.getNumeroErrores(OutTesoreria.P_OUT_MENSAJES);
			
			if(out.P_OUT_ERRORESCONTADOR > 0) {
				List<MensajeSalida> mensajesErrores = OutTesoreria.P_OUT_MENSAJES.parallelStream().filter(mensaje->MensajeSalida.MENSAJE_ERROR.equals(mensaje.tipo)).collect(Collectors.toList());
				out.P_OUT_MENSAJES.addAll(mensajesErrores);
				return out;
			}
		    
		    
	
			for(CrearIngresoEgreso.IngresoEgresoGenerado_Salida outIngresoEgreso :OutTesoreria.P_OUT_INGRESOEGRESOGENERADO){
	
				CrearComprobante.IngresoEgresoGenerar_Entrada ComprobanteIngresoEgreso_Entrada = new CrearComprobante.IngresoEgresoGenerar_Entrada();
				ComprobanteIngresoEgreso_Entrada.NumeroReferencia = 1L;//outIngresoEgreso.NumeroReferencia;//1L;
				ComprobanteIngresoEgreso_Entrada.IdIngresoEgreso = outIngresoEgreso.IdIngresoEgreso;
				ComprobanteIngresoEgreso_Entrada.TipoComprobante = "";
				paramsComprobanteIngresoEgreso_Entrada.add(ComprobanteIngresoEgreso_Entrada);
			};
			
		    //COMPROBANTE
			    
				CrearComprobante.CrearComprobante_Entrada paramsComprobante_Entrada = new CrearComprobante.CrearComprobante_Entrada();
				List<CrearComprobante.ComprobanteGenerar_Entrada> paramsComprobanteGenerar_Entrada = new ArrayList<CrearComprobante.ComprobanteGenerar_Entrada>();
				
				paramsComprobante_Entrada.p_IdEmpresa = in.p_IdEmpresa;
				paramsComprobante_Entrada.p_IdUsuario = in.p_IdUsuario;
				paramsComprobante_Entrada.p_IdModulo = in.p_IdModulo;
				paramsComprobante_Entrada.p_UsaReglasNegocio = "S";
				paramsComprobante_Entrada.p_FechaMovimiento = FechaSistema.toString();
				paramsComprobante_Entrada.p_IdCaja = 1L;
				paramsComprobante_Entrada.p_GenerarComprobanteDesdeMovimientos = "S";
				paramsComprobante_Entrada.p_ComprobanteGenerar = paramsComprobanteGenerar_Entrada;
				paramsComprobante_Entrada.p_IngresoEgresoGenerar = paramsComprobanteIngresoEgreso_Entrada;
				paramsComprobante_Entrada.p_FormaDePago = paramsComprobanteFormaDePago_Entrada;
				
				CrearComprobante ProcesoCrearComprobante = new CrearComprobante();
	
			    CrearComprobante.CrearComprobante_Salida OutComprobante = ProcesoCrearComprobante.logica(con, paramsComprobante_Entrada); 
			    
			    logger.info("SALIDA::CrearComprobante.OutComprobante");
			    System.out.println("SALIDA::CrearComprobante.OutComprobante");
			    
				out.P_OUT_ERRORESCONTADOR += MensajeSalida.getNumeroErrores(OutComprobante.P_OUT_MENSAJES);
				
				if(out.P_OUT_ERRORESCONTADOR > 0) {
					List<MensajeSalida> mensajesErrores = OutComprobante.P_OUT_MENSAJES.parallelStream().filter(mensaje->MensajeSalida.MENSAJE_ERROR.equals(mensaje.tipo)).collect(Collectors.toList());
					out.P_OUT_MENSAJES.addAll(mensajesErrores);
					return out;
				}
			    
		
				
			  //CAMBIO ESTADOS
			    CambiarEstadoRecaudacion.ParametrosEntradaServicio paramsCambioEstado_Entrada = new CambiarEstadoRecaudacion.ParametrosEntradaServicio();
	
			    paramsCambioEstado_Entrada.p_IdEmpresa = in.p_IdEmpresa;
			    paramsCambioEstado_Entrada.p_IdUsuario = in.p_IdUsuario;
			    paramsCambioEstado_Entrada.p_IdModulo = in.p_IdModulo;
			    paramsCambioEstado_Entrada.p_Parametros = paramsCambioEstado;
			    
			    CambiarEstadoRecaudacion ProcesoCambioEstado = new CambiarEstadoRecaudacion();
	
			    CambiarEstadoRecaudacion.ParametrosSalidaServicio OutCambioEstado = ProcesoCambioEstado.logica(con, paramsCambioEstado_Entrada); 
			    System.out.println("SALIDA::CambiarEstadoRecaudacion.OutCambioEstado");
			    
			    //Log
			    agregarLog(con, in, FechaSistema, paramsCambioEstado);
			    
			
		return out;
	}
	
	private void agregarLog(Connection con, ParametrosEntradaServicio in, String fechaSistema, List<CambiarEstadoRecaudacion.ParametrosRecaudacionEstado> paramsCambioEstado) throws Exception {
		logger.trace("INICIO::DevolucionPagoRezagos.agregarLog");
		long inicio = System.currentTimeMillis();
		
		InAgregarLog entradaAgregarLog = new InAgregarLog();
		
		if(!paramsCambioEstado.isEmpty()) {
			
			AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
			Long idTrabajo = new Double(Math.random()*10000000000L).longValue();
			log.IdCabeceraLog = 1L;
			log.IdTrabajo = idTrabajo;
			log.IdTipoLog = "INFORMATIVO";
			log.IdModulo = in.p_IdModulo;
			log.IdUsuario = in.p_IdUsuario;
			log.IdEmpresa = in.p_IdEmpresa;
			log.FechaSistemaLog = fechaSistema;
			log.IdUsuarioAtendido = in.p_IdUsuario;
			log.IdTipoRegistro = "DEVOLUCION REZAGOS";
			log.NombreProceso = "Devolución pago rezagos";
			entradaAgregarLog.p_Log.add(log);
			List<Long> listaFolioRecaudacionCliente = new ArrayList<>();
			
			paramsCambioEstado.stream()
				.filter(recaudacion -> recaudacion.IdRecaudacionCliente != null && recaudacion.IdRecaudacionCliente.compareTo(0L) > 0)
				.sorted(Comparator.comparingLong(recaudacion -> recaudacion.IdRecaudacionCliente))
				.forEach(recaudacion ->{
					if(!listaFolioRecaudacionCliente.contains(recaudacion.IdRecaudacionCliente)) {
						String subRegistro = String.format("Folio Recaudación Cliente %s", recaudacion.IdRecaudacionCliente.toString());
						entradaAgregarLog.agregarDetalleLogBasico(1L, idTrabajo, "IdEstado", "", "DEV", subRegistro);
						listaFolioRecaudacionCliente.add(recaudacion.IdRecaudacionCliente);
					}
				});
			
			AgregarLog agregarLogService = new AgregarLog();
			if(!entradaAgregarLog.p_LogDetalle.isEmpty()) {
				agregarLogService.logica(con, entradaAgregarLog);
			}
			
		}
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::DevolucionPagoRezagos.agregarLog", fin - inicio);
	}
	
	/********************
	 * Método Principal *
	 ********************/

	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			try {
				DevolucionPagoRezagos demo = new DevolucionPagoRezagos();	

				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();				
				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":51,\"p_IdModulo\":\"APV\",\"p_DevolucionTipoPersona\":\"OTROS\",\"p_DevolucionIdPersona\":20162,\"p_DevolucionIdPortafolio\":100027,\"p_DevolucionIdMedioPago\":\"TRA\",\"p_DevolucionIdBanco\":106,\"p_DevolucionIdCuentaCorriente\":\"77889966\",\"p_Parametros\":[{\"IdRecaudacionCliente\":13}]}";
				ParametrosEntradaServicio Objin = gson.fromJson(json, ParametrosEntradaServicio.class);
				
	 			try { 
	 				ParametrosSalidaServicio ObjOut = demo.logica(con, Objin);
	 				//con.commit();
	 				con.rollback();
	 				//System.out.println(ObjOut.toString());
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