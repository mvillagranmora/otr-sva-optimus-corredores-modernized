package cl.bcs.APV;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.tesoreria.CrearComprobante;
//Tesoreria
import cl.bcs.tesoreria.CrearIngresoEgreso;
import cl.bcs.tesoreria.CrearIngresoEgreso.CrearIngresoEgreso_Entrada;
import cl.bcs.tesoreria.CrearIngresoEgreso.CrearIngresoEgreso_Salida;
import cl.bcs.tesoreria.LiquidarComprobante;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;


public class TraspasoCuentasBancarias implements JavaCall   {

	static final Logger logger = LogManager.getLogger(TraspasoCuentasBancarias.class);

	/*
	 * Queries que van a ser utilizadas por el servicio. 
	 */	
	public String SQL_SELECT_OrigenDestinoTransicion = loadResourceToString("cl/bcs/APV/sql/ConsultaOrigenDestinoTransicion.sql");
	public String SQL_SELECT_OrigenRecaudacion = loadResourceToString("cl/bcs/APV/sql/ConsultaOrigenRecaudacion.sql");
	public String SQL_SELECT_PersonaClienteRecaudacion = loadResourceToString("cl/bcs/APV/sql/ConsultarPersonaClienteRecaudacion.sql");
	
	/********************
	 * Clases IN/OUT	*
	 ********************/

	public static class OrigenDestinoTransicion{

		public String OrigenDestino;
		public String MueveCuentaCorriente;
		public String MueveCuentaInversion;
		public Long IdCuentaBanco;
		public Long IdBanco;
		public String IdTipoCuentaBancaria;
		public String NombreTipoCuentaBancaria;
		public Long IdSocioDeNegocio;
		public Long SocioNegocioCtaBco;
		public Long IdBancoSocio;
		public String IdTipoCuentaBancariaSocio;
		public String NumeroCuentaBancariaSocio;
		public Long SocioNegocioNumCta;
		public Long IdPortafolioDefecto;
		public Long IdPersona;
		public BigDecimal Monto;
		public Long IdPersonaCliente;
		public String Origen;

		public OrigenDestinoTransicion(){
			Monto = BigDecimal.ZERO;	
		}
		
	}

	public static class OrigenRecaudacion{

		public Long IdRecaudacionCliente;
	}
	
	public static class RecaudacionPersona{
		public Long IdRecaudacionCliente;
		public Long IdPersonaCliente;
	}
	
	//  SERVICIO ENTRADA
	public static class ParametrosEntradaServicio {
		
		public  String					p_Simula  = "N"; 
		public 	Long					p_IdEmpresa;
		public	Long					p_IdUsuario;
		public 	String					p_IdModulo;
		public 	String					p_Opcion;
		public 	String					p_Fecha;
		public 	BigDecimal				p_Monto;
		public 	Boolean					p_EsTraspaso;
		
		public List<OrigenRecaudacion> p_OrigenRecaudacion;

		public ParametrosEntradaServicio(){
			this.p_OrigenRecaudacion = new ArrayList<OrigenRecaudacion>();
			this.p_EsTraspaso = false;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}			

	// SERVICIO SALIDA
	public static class ParametrosSalidaServicio {		
		public List<MensajeSalida>									P_OUT_MENSAJES;
		public Long													P_OUT_ERRORESCONTADOR ;

		public List<CrearIngresoEgreso.IngresoEgreso_Entrada>		p_IngresoEgreso;
		public List<CrearIngresoEgreso.FormaDePago_Entrada> 		p_FormaDePago;
		public List<CrearComprobante.IngresoEgresoGenerar_Entrada>	p_ComprobanteIngresoEgreso;
		public List<CrearComprobante.FormaDePago_Entrada> 			p_ComprobanteFormaDePago;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
		public ParametrosSalidaServicio(){
			P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			p_IngresoEgreso = new ArrayList<CrearIngresoEgreso.IngresoEgreso_Entrada>();
			p_FormaDePago = new ArrayList<CrearIngresoEgreso.FormaDePago_Entrada>();
		}		
	};

	public static class ContratoConsultado {
		public  Long		IdContratoSAAPV;
	    public 	String		IdEstado;
		
		public String toString() {
			return Utils.printJson(this, false);
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
		logger.info("INICIO::TraspasoCuentasBancarias");
		long inicioServicio = System.currentTimeMillis();
		ParametrosSalidaServicio out = new ParametrosSalidaServicio();

		SQL sql = new SQL();
		String FechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		String AbreRegistrosTesoreria = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "APV.AbreRegistrosTesoreria");
		
		Long contador = 1L;
		String ConceptoIngreso = "";
		String ConceptoEgreso = "";
		if (in.p_Opcion.equals("REZ")) ConceptoIngreso = "INGREZ";
		else if (in.p_Opcion.equals("DEV")) ConceptoIngreso = "INGDEV";
		else if (in.p_Opcion.equals("DBF")) ConceptoIngreso = "INGBON";
		else if (in.p_Opcion.equals("DIS") && in.p_EsTraspaso) ConceptoIngreso = "ITRAPV";
		else ConceptoIngreso = "INGAPV";
		
		if (in.p_Opcion.equals("REZ")) ConceptoEgreso = "EGRREZ";
		else if (in.p_Opcion.equals("DEV") && "APV".equalsIgnoreCase(in.p_IdModulo)) ConceptoEgreso = "DEVRZG";
		else if (in.p_Opcion.equals("DEV")) ConceptoEgreso = "EGRDEV";
		else if (in.p_Opcion.equals("DBF")) ConceptoEgreso = "EGRBON";
		else if (in.p_Opcion.equals("DIS") && in.p_EsTraspaso) ConceptoEgreso = "ETRAPV";
		else ConceptoEgreso = "EGRAPV";
		
		String OrigenOperacion = "";
		Long IdPersonaCliente= 0L;
		Long IdCuentaBancoTraspaso  = 0L;
		
		HashMap<String,Object> ParamsSELECTOrigenDestinoTransicion = new HashMap<String,Object>();
		ParamsSELECTOrigenDestinoTransicion.put("IdEmpresa", in.p_IdEmpresa);
		ParamsSELECTOrigenDestinoTransicion.put("Operacion", in.p_Opcion);

		List<OrigenDestinoTransicion> _Transicion = sql.query(con, SQL_SELECT_OrigenDestinoTransicion, ParamsSELECTOrigenDestinoTransicion, OrigenDestinoTransicion.class);

		if  ( in.p_Opcion.equals("REZ")  ) {
			if("N".equalsIgnoreCase(AbreRegistrosTesoreria)) {
				for(OrigenDestinoTransicion filaTrans : _Transicion){
					filaTrans.Monto = in.p_Monto;
				}
			}
		
		}else {
			for(OrigenDestinoTransicion filaTrans : _Transicion){
				filaTrans.Monto = in.p_Monto;
			}
		}
		ArrayList<CrearIngresoEgreso.IngresoEgreso_Entrada> paramsIngresoEgreso = new ArrayList<CrearIngresoEgreso.IngresoEgreso_Entrada>();
		ArrayList<CrearIngresoEgreso.FormaDePago_Entrada> paramsFormaDePago = new ArrayList<CrearIngresoEgreso.FormaDePago_Entrada>();
		List<CrearComprobante.FormaDePago_Entrada> paramsComprobanteFormaDePago_Entrada = new ArrayList<CrearComprobante.FormaDePago_Entrada>();

		
		if (in.p_OrigenRecaudacion.size()>0){

			HashMap<String,Object> ParamsSELECTOrigenRecaudacion = new HashMap<String,Object>();
			ParamsSELECTOrigenRecaudacion.put("IdEmpresa", in.p_IdEmpresa);
			ParamsSELECTOrigenRecaudacion.put("Operacion", in.p_Opcion);
			ArrayList<Long> idsRecaudacion = new ArrayList<Long>();
			for(OrigenRecaudacion filaOrigenRecaudacion : in.p_OrigenRecaudacion){
				idsRecaudacion.add(filaOrigenRecaudacion.IdRecaudacionCliente);
			}
			
			ParamsSELECTOrigenRecaudacion.put("IdsRecaudacionCliente", idsRecaudacion);
		
			HashMap<String,Object> paramsSelectRecaudacion = new HashMap<String,Object>();
			paramsSelectRecaudacion.put("IdsRecaudacionCliente", idsRecaudacion);
			List<RecaudacionPersona> PersonaRecaudacion = sql.query(con, SQL_SELECT_PersonaClienteRecaudacion, paramsSelectRecaudacion, RecaudacionPersona.class);
			IdPersonaCliente = PersonaRecaudacion.get(0).IdPersonaCliente;
		
			List<OrigenDestinoTransicion> _TransicionRec = new ArrayList<>();
			_TransicionRec = sql.query(con, SQL_SELECT_OrigenRecaudacion, ParamsSELECTOrigenRecaudacion, OrigenDestinoTransicion.class);
			
			

		if( in.p_Opcion.equals("REZ") && "S".equalsIgnoreCase(AbreRegistrosTesoreria)) {
			List<OrigenDestinoTransicion> _TransicionAux = new ArrayList<>();
			

			
			for(OrigenDestinoTransicion recas : _TransicionRec){
				OrigenDestinoTransicion OD = new OrigenDestinoTransicion();
					OD.IdPersonaCliente = recas.IdPersonaCliente;
					OD.Monto = recas.Monto;
					_TransicionAux.add(OD);
				
			
			}
			
			llenadoTransicionArmada(_Transicion, _TransicionAux);
			
			_Transicion = _TransicionAux.isEmpty()?_Transicion:_TransicionAux;
		
		}

			for(OrigenDestinoTransicion filaTrans : _Transicion){
				if (filaTrans.OrigenDestino.equals("DESTINO")){
					
					for(OrigenDestinoTransicion filaRec : _TransicionRec){
					
						OrigenOperacion =  filaRec.Origen;
						IdPersonaCliente = filaRec.IdPersonaCliente;
						IdCuentaBancoTraspaso = filaRec.IdCuentaBanco;
						
					}
					
					_TransicionRec.add(filaTrans);
				
				}
			}
			_Transicion = _TransicionRec;
		}
		
		for (OrigenDestinoTransicion fila : _Transicion){

			CrearIngresoEgreso.IngresoEgreso_Entrada IngresoEgreso = new CrearIngresoEgreso.IngresoEgreso_Entrada(); //IngresoEgreso
			CrearIngresoEgreso.FormaDePago_Entrada FormaDePago = new CrearIngresoEgreso.FormaDePago_Entrada();       //FormaPagoTeso
			CrearComprobante.FormaDePago_Entrada ComprobanteFormaDePago = new CrearComprobante.FormaDePago_Entrada();//ComprobanteFormaPago

			//Se crea movimiento de tesoreria con los datos de recaudacion
			IngresoEgreso.NumeroReferencia = contador;
			IngresoEgreso.AgruparEnComprobante = "N";
			IngresoEgreso.Bilateral = "N";
			IngresoEgreso.FechaComprobante = "0001-01-01";
			IngresoEgreso.FechaLiquidacion = null; //in.p_Fecha;
			IngresoEgreso.FechaLiquidacionComprobante = "0001-01-01";
			IngresoEgreso.FechaOperacion = null;//in.p_Fecha + " 00:00:00";
			IngresoEgreso.FechaParidad = "0001-01-01";
			IngresoEgreso.FolioCartera = 0L;
			IngresoEgreso.GeneraComprobante = "N";
			IngresoEgreso.Glosa = "";
			IngresoEgreso.IdBolsa = "";
			IngresoEgreso.IdCaja = 0L;
			IngresoEgreso.IdClasificacionTipoInstrumento = "";
			IngresoEgreso.IdComprobante = 0L;
			if (fila.OrigenDestino.equals("ORIGEN")) IngresoEgreso.IdConcepto = ConceptoEgreso; else  IngresoEgreso.IdConcepto = ConceptoIngreso;
			IngresoEgreso.IdCondicionLiquidacion = "";
			IngresoEgreso.IdEmpleadoDigitador = 0L;
			IngresoEgreso.IdInstrumento = 0L;
			IngresoEgreso.IdMoneda = "CLP";
			IngresoEgreso.IdMonedaOperacion = "";
			IngresoEgreso.IdOperacion = 0L;		
			IngresoEgreso.IdPersonaTitularPago = fila.IdPersona;
			IngresoEgreso.IdPortafolio = fila.IdPortafolioDefecto; //
			IngresoEgreso.IdPortafolioTitularPago = fila.IdPortafolioDefecto;
			IngresoEgreso.IdSucursalEmpleadoDigitador = 0L;
			IngresoEgreso.IdTipoComprobante = "";
			IngresoEgreso.IdTipoInstrumento = "";
			if (fila.OrigenDestino.equals("ORIGEN")) IngresoEgreso.IdTipoMovimiento = "MOVEGR"; else  IngresoEgreso.IdTipoMovimiento = "MOVING";
			if (fila.OrigenDestino.equals("ORIGEN")) IngresoEgreso.IdTipoOperacion = "EGRTES"; else  IngresoEgreso.IdTipoOperacion = "INGTES";
			
			if ("INGTES".equalsIgnoreCase(IngresoEgreso.IdTipoOperacion)) {
				IngresoEgreso.IdPersona =  (in.p_Opcion.equals("REZ") && ("S".equalsIgnoreCase(AbreRegistrosTesoreria))) ? fila.IdPersonaCliente:IdPersonaCliente;

			}else {
				IngresoEgreso.IdPersona = fila.IdPersona; 
			}
			
			IngresoEgreso.IdTipoPersona = "CLI";
			IngresoEgreso.IdTipoPersonaTitularPago = "CLI";
			IngresoEgreso.IdTipoProducto = "";
			IngresoEgreso.IdTipoReajuste = "";
			IngresoEgreso.Monto = fila.Monto;
			IngresoEgreso.MontoOperacion = fila.Monto;
			IngresoEgreso.MueveCuentaInversion = fila.MueveCuentaInversion;
			IngresoEgreso.SecuenciaAgrupacion = "";
			IngresoEgreso.SecuenciaRelacionada = null;
			IngresoEgreso.TitularPago = "S";
			IngresoEgreso.ValorParidad = BigDecimal.valueOf(0);	
			
			paramsIngresoEgreso.add(IngresoEgreso);

			//Se crea forma de pago correspondiente al movimiento de tesoreria con los datos de recaudacion
			FormaDePago.NumeroReferencia = contador;
			FormaDePago.NumeroReferenciaFormaDePago = contador;
			FormaDePago.FechaDocumentoPagoCorredora = "0001-01-01";
			FormaDePago.FechaDocumentoPagoSocio = null; // in.p_Fecha+" 00:00:00";
			FormaDePago.IdBancoSocio = fila.IdBancoSocio; //12
			FormaDePago.IdChequera = 0L;
			
			if ("TRASPA".equalsIgnoreCase(OrigenOperacion)  && "INGTES".equalsIgnoreCase(IngresoEgreso.IdTipoOperacion)) {
				FormaDePago.IdCuentaBancariaCorredora = IdCuentaBancoTraspaso;
			}else {
				FormaDePago.IdCuentaBancariaCorredora = fila.IdCuentaBanco; //122L
			}
			
			FormaDePago.IdCuentaBancariaSocio = fila.SocioNegocioCtaBco; //100
			FormaDePago.IdMedioDePago = "TRA";
			FormaDePago.IdModalidadDePago = "MODDEP";
			FormaDePago.IdMoneda = "CLP";
			FormaDePago.IdTarjetaBancariaCorredora = 0L;
			FormaDePago.IdTarjetaBancariaSocio = 0L;
			FormaDePago.IdTipoCuentaBancariaSocio = fila.IdTipoCuentaBancariaSocio; //CUCO
			FormaDePago.IdTipoTarjetaBancariaSocio = "";
			FormaDePago.Monto = fila.Monto;
			FormaDePago.NombreCuentaBancariaSocio = fila.NombreTipoCuentaBancaria; //NombreTipoCuentaBancaria
			FormaDePago.NombreTipoCuentaBancariaSocio = "";
			FormaDePago.NombreTipoTarjetaBancariaSocio = "";
			FormaDePago.NumeroCuentaBancariaSocio = fila.NumeroCuentaBancariaSocio;
			FormaDePago.NumeroDocumentoCorredora = 0L;
			FormaDePago.NumeroDocumentoSocio = 0L; //12312313
			FormaDePago.NumeroSerieSocio = "";
			FormaDePago.NumeroTarjetaBancariaSocio = "";
			
			paramsFormaDePago.add(FormaDePago);

			ComprobanteFormaDePago.NumeroReferencia = contador;
			ComprobanteFormaDePago.IdMedioDePago = "TRA";
			ComprobanteFormaDePago.IdModalidadDePago ="MODDEP";
			ComprobanteFormaDePago.IdMoneda ="CLP";
			ComprobanteFormaDePago.Monto =fila.Monto;
			ComprobanteFormaDePago.IdCuentaBancariaCorredora = 0L; //=129L;
			ComprobanteFormaDePago.IdTarjetaBancariaCorredora =0L;
			ComprobanteFormaDePago.IdChequera =0L;
			ComprobanteFormaDePago.NumeroDocumentoCorredora =0L;
			ComprobanteFormaDePago.FechaDocumentoPagoCorredora ="0001-01-01";
			ComprobanteFormaDePago.IdCuentaBancariaSocio =0L;
			ComprobanteFormaDePago.IdBancoSocio =0L;
			ComprobanteFormaDePago.NombreCuentaBancariaSocio ="";
			ComprobanteFormaDePago.NumeroCuentaBancariaSocio ="";
			ComprobanteFormaDePago.IdTipoCuentaBancariaSocio =fila.IdTipoCuentaBancariaSocio;
			ComprobanteFormaDePago.NombreTipoCuentaBancariaSocio ="";
			ComprobanteFormaDePago.IdTarjetaBancariaSocio =0L;
			ComprobanteFormaDePago.IdTipoTarjetaBancariaSocio ="";
			ComprobanteFormaDePago.NombreTipoTarjetaBancariaSocio ="";
			ComprobanteFormaDePago.NumeroTarjetaBancariaSocio ="";
			ComprobanteFormaDePago.NumeroSerieSocio ="";
			ComprobanteFormaDePago.NumeroDocumentoSocio =0L;
			ComprobanteFormaDePago.FechaDocumentoPagoSocio =in.p_Fecha.toString();
			paramsComprobanteFormaDePago_Entrada.add(ComprobanteFormaDePago);

			contador = contador+1;

		}
		
		if (_Transicion.isEmpty()){
			return out;
		}
		
		//CREACION INGRESO EGRESO TESORERIA
		/*
		 * Se envia previamente a tesoreria y no se junta con el ProcesarDistribucion ya que lanza un error el ingresoEgreso de tesoreria, si es posible solucionar este error se debe
		 * enviar de vuelta los parametros para no hacer doble llamada al servicio
		 */
		   
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
		    out.P_OUT_MENSAJES.addAll(OutTesoreria.P_OUT_MENSAJES);
		    //Se toma la salida del ingresoEgreso de tesoreria para crear comprobantes 
			for(CrearIngresoEgreso.IngresoEgresoGenerado_Salida outIngresoEgreso :OutTesoreria.P_OUT_INGRESOEGRESOGENERADO){
	
				CrearComprobante.IngresoEgresoGenerar_Entrada ComprobanteIngresoEgreso_Entrada = new CrearComprobante.IngresoEgresoGenerar_Entrada();
				ComprobanteIngresoEgreso_Entrada.NumeroReferencia = outIngresoEgreso.NumeroReferencia;
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
			paramsComprobante_Entrada.p_FechaMovimiento = in.p_Fecha.toString();
			paramsComprobante_Entrada.p_IdCaja = 1L;
			paramsComprobante_Entrada.p_GenerarComprobanteDesdeMovimientos = "S";
			paramsComprobante_Entrada.p_ComprobanteGenerar = paramsComprobanteGenerar_Entrada;
			paramsComprobante_Entrada.p_IngresoEgresoGenerar = paramsComprobanteIngresoEgreso_Entrada;
			paramsComprobante_Entrada.p_FormaDePago = paramsComprobanteFormaDePago_Entrada;
			
			CrearComprobante ProcesoCrearComprobante = new CrearComprobante();

			CrearComprobante.CrearComprobante_Salida OutComprobante = ProcesoCrearComprobante.logica(con, paramsComprobante_Entrada); 
		    out.P_OUT_MENSAJES.addAll(OutComprobante.P_OUT_MENSAJES);
			List<LiquidarComprobante.ComprobanteLiquidar_Entrada> paramsComprobanteLiquidar_Entrada = new ArrayList<LiquidarComprobante.ComprobanteLiquidar_Entrada>();

			if (MensajeSalida.getNumeroErrores(OutComprobante.P_OUT_MENSAJES) > 0) 
			{
				
			}else{
				for(int i = 0; i < OutComprobante.P_OUT_RESULTADOPROCESO.size(); i ++){
					if(OutComprobante.P_OUT_RESULTADOPROCESO.get(i).Campo.equals("IdComprobante")){
						LiquidarComprobante.ComprobanteLiquidar_Entrada ComprobanteLiquidar_Entrada = new LiquidarComprobante.ComprobanteLiquidar_Entrada();
						ComprobanteLiquidar_Entrada.IdComprobante = OutComprobante.P_OUT_RESULTADOPROCESO.get(i).Id;
						ComprobanteLiquidar_Entrada.IdTipoMovimiento = paramsIngresoEgreso.get(i).IdTipoMovimiento;
						ComprobanteLiquidar_Entrada.AfectaCuentaInversion = "CLI";
						paramsComprobanteLiquidar_Entrada.add(ComprobanteLiquidar_Entrada);
					}
				}
			}

		String valorParametroLiquidacion = ConfiguracionSistema.getParametroConfiguracionString(con,in.p_IdEmpresa, "APV","REC.LiquidacionAutomatica");
		 if (valorParametroLiquidacion.equals("S") && !("DEVRZG".equals(ConceptoEgreso))){

			LiquidarComprobante.LiquidarComprobante_Entrada paramsLiquidar_Entrada = new LiquidarComprobante.LiquidarComprobante_Entrada();
			
			paramsLiquidar_Entrada.p_IdEmpresa = in.p_IdEmpresa;
			paramsLiquidar_Entrada.p_IdUsuario = in.p_IdUsuario;
			paramsLiquidar_Entrada.p_IdModulo = in.p_IdModulo;
			paramsLiquidar_Entrada.p_UsaReglasNegocio = "S";
			paramsLiquidar_Entrada.p_FechaMovimiento = FechaSistema.toString();
			paramsLiquidar_Entrada.p_ComprobanteLiquidar = paramsComprobanteLiquidar_Entrada;
			
			LiquidarComprobante ProcesoLiquidar = new LiquidarComprobante();
			
			LiquidarComprobante.LiquidarComprobante_Salida OutLiquidar = ProcesoLiquidar.logica(con, paramsLiquidar_Entrada);
	        out.P_OUT_MENSAJES.addAll(OutLiquidar.P_OUT_MENSAJES);
		 }
		
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		out.p_IngresoEgreso = paramsIngresoEgreso;
		out.p_FormaDePago = paramsFormaDePago;
		out.p_ComprobanteIngresoEgreso = paramsComprobanteIngresoEgreso_Entrada;
		out.p_ComprobanteFormaDePago = paramsComprobanteFormaDePago_Entrada;
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::TraspasoCuentasBancarias", finServicio - inicioServicio);
		return out;
	}

	private void llenadoTransicionArmada(List<OrigenDestinoTransicion> _Transicion,
			List<OrigenDestinoTransicion> _TransicionAux) {
		for(OrigenDestinoTransicion creados : _TransicionAux){
			for(OrigenDestinoTransicion filaTrans : _Transicion){
				creados.IdBanco=filaTrans.IdBanco;
				creados.IdBancoSocio=filaTrans.IdBancoSocio;
				creados.IdCuentaBanco=filaTrans.IdCuentaBanco;
				creados.IdPersona =filaTrans.IdPersona;
				creados.IdPortafolioDefecto=filaTrans.IdPortafolioDefecto;
				creados.IdSocioDeNegocio=filaTrans.IdSocioDeNegocio;
				creados.IdTipoCuentaBancaria=filaTrans.IdTipoCuentaBancaria;
				creados.IdTipoCuentaBancariaSocio=filaTrans.IdTipoCuentaBancaria;
				creados.MueveCuentaCorriente=filaTrans.MueveCuentaCorriente;
				creados.MueveCuentaInversion=filaTrans.MueveCuentaInversion;
				creados.NombreTipoCuentaBancaria=filaTrans.NombreTipoCuentaBancaria;
				creados.NumeroCuentaBancariaSocio=filaTrans.NumeroCuentaBancariaSocio;
				creados.Origen=filaTrans.Origen;
				creados.OrigenDestino=filaTrans.OrigenDestino;
				creados.SocioNegocioCtaBco=filaTrans.SocioNegocioCtaBco;
				creados.SocioNegocioNumCta=filaTrans.SocioNegocioNumCta;
					
						
						
			}
		}
	}
	/********************
	 * Método Principal *
	 ********************/

	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			try {
				ParametrosEntradaServicio Objin = new ParametrosEntradaServicio();
				TraspasoCuentasBancarias demo = new TraspasoCuentasBancarias();	

				Objin.p_IdEmpresa = 1L;
				Objin.p_IdUsuario = 17L;
				Objin.p_IdModulo = "APV";
				Objin.p_Opcion = "DIS";
				Objin.p_Fecha = "2017-01-01";
				Objin.p_Monto = BigDecimal.valueOf(1000L);
				
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
