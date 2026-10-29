package cl.bcs.APV;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;


public class DevolucionBF_EntidadRecaudadora implements JavaCall   {

	/*
	 * El log
	 */	
	private final Log log = new Log("DevolucionBF_EntidadRecaudadora");

	/*
	 * Queries que van a ser utilizadas por el servicio. 
	 */	
	
	public String SQL_SELECT_Bonificacion = loadResourceToString("cl/bcs/APV/sql/ConsultarBonificacionADevolver.sql");

	private final String SQL_DatosEmpresa = "SELECT Empresa.\"IdPersona\",\"IdPortafolioDefecto\" FROM \"BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa\" Empresa"
													+ " INNER JOIN \"BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente\" Cliente"
													+ " ON Cliente.\"IdSocioDeNegocio\" = Empresa.\"IdSocioDeNegocio\""
													+ " WHERE Empresa.\"IdEmpresa\" = :IdEmpresa";

	private final String SQL_PortafolioEmpresa = "SELECT \"IdPortafolioDefecto\" FROM \"BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa\" Empresa"
													+ " INNER JOIN \"BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente\" Cliente"
													+ " ON Cliente.\"IdSocioDeNegocio\" = Empresa.\"IdSocioDeNegocio\""
													+ " WHERE Empresa.\"IdEmpresa\" = :IdEmpresa";
	
	private final String SQL_CtaBco = "SELECT \"DestinoCtaBco\" FROM \"BSCL.APV.BD::APV.OrigenDestinoTransicion\" WHERE \"Operacion\" = 'DBF' AND \"IdEmpresa\" = :IdEmpresa";
	/********************
	 * Clases IN/OUT	*
	 ********************/

	//  SERVICIO ENTRADA
	public static class ParametrosEntradaServicio {
		
		public 	Long					p_IdEmpresa;
		public	Long					p_IdUsuario;
		public 	String					p_IdModulo;
		
		
		public List<ParametrosBonificacion>		p_Parametros;
		
		public String toString() {
			return Utils.printJson(this, false);
		}/*
		public ParametrosEntradaServicio(){
			p_DevolucionIdPortafolio = 0L;
		}*/
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
			P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			p_IngresoEgreso = new ArrayList<CrearIngresoEgreso.IngresoEgreso_Entrada>();
			p_FormaDePago = new ArrayList<CrearIngresoEgreso.FormaDePago_Entrada>();
		}		
	};

	public static class ParametrosBonificacion{

		public Long IdBonificacion;
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
	
	public static class Bonificacion{

		public Long IdBonificacion;
		public Long IdPersonaEntidad;			
		public BigDecimal Monto;	
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
		String FechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");

		Long contador = 0L;
		Long IdCtaBco = 0L;
		
		BigDecimal sumaMontos = BigDecimal.valueOf(0);

		HashMap<String,Object> ParamsSelectBonificacion = new HashMap<String,Object>();
		ArrayList<Long> BonificacionBuscada = new ArrayList<Long>();

			for(ParametrosBonificacion fila : in.p_Parametros){
				BonificacionBuscada.add(fila.IdBonificacion);	
			}
			ParamsSelectBonificacion.put("IdBonificacionGrupo", BonificacionBuscada);
			ParamsSelectBonificacion.put("IdEmpresa", in.p_IdEmpresa);

		HashMap<String,Object> ParamsSelectPortafolio = new HashMap<String,Object>();
		HashMap<String,Object> ParamsSelectCtaBco = new HashMap<String,Object>();
		ParamsSelectPortafolio.put("IdEmpresa", in.p_IdEmpresa);
		ParamsSelectCtaBco.put("IdEmpresa", in.p_IdEmpresa);

		List<Bonificacion> _Bonificacion = sql.query(con, SQL_SELECT_Bonificacion, ParamsSelectBonificacion, Bonificacion.class);
		List<IdPortafolioDefecto> _IdPortafolioDefecto = sql.query(con, SQL_PortafolioEmpresa, ParamsSelectPortafolio, IdPortafolioDefecto.class);
		List<DestinoCtaBco> _CtaBco = sql.query(con, SQL_CtaBco, ParamsSelectPortafolio, DestinoCtaBco.class);
		
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
		List<CrearCargoAbono.ObjCargoAbonoAGenerar> paramsCargoAbono = new ArrayList<CrearCargoAbono.ObjCargoAbonoAGenerar>();
		
		for(Bonificacion fila : _Bonificacion){
			contador++;
			//obtejos de tesoreria y ordenes
			CrearIngresoEgreso.IngresoEgreso_Entrada IngresoEgreso = new CrearIngresoEgreso.IngresoEgreso_Entrada(); //IngresoEgreso
		    CambiarEstadoRecaudacion.ParametrosRecaudacionEstado RecaudacionEstado = new CambiarEstadoRecaudacion.ParametrosRecaudacionEstado();
			CrearIngresoEgreso.FormaDePago_Entrada FormaDePago = new CrearIngresoEgreso.FormaDePago_Entrada();       //FormaPagoTeso
			CrearComprobante.FormaDePago_Entrada ComprobanteFormaDePago = new CrearComprobante.FormaDePago_Entrada();//ComprobanteFormaPago
			CrearCargoAbono.ObjCargoAbonoAGenerar CargoAbono = new CrearCargoAbono.ObjCargoAbonoAGenerar();			 //CargoAbono

			//Sumatoria de montos para posteriores usos
			sumaMontos = sumaMontos.add(fila.Monto);
			
			//Se crea movimiento de tesoreria con los datos de recaudacion
			IngresoEgreso.NumeroReferencia = contador;
			IngresoEgreso.AgruparEnComprobante = "N";
			IngresoEgreso.Bilateral = "N";
			IngresoEgreso.FechaComprobante = "0001-01-01";
			IngresoEgreso.FechaLiquidacion = null;//fila.FechaDeposito.toString();
			IngresoEgreso.FechaLiquidacionComprobante = "0001-01-01";
			IngresoEgreso.FechaOperacion = null;
			IngresoEgreso.FechaParidad = "0001-01-01";
			IngresoEgreso.FolioCartera = 0L;
			IngresoEgreso.GeneraComprobante = "N";
			IngresoEgreso.Glosa = "";
			IngresoEgreso.IdBolsa = "";
			IngresoEgreso.IdCaja = 0L;
			IngresoEgreso.IdClasificacionTipoInstrumento = "";
			IngresoEgreso.IdComprobante = 0L;
			IngresoEgreso.IdConcepto = "EGRBON";
			IngresoEgreso.IdCondicionLiquidacion = "";
			IngresoEgreso.IdEmpleadoDigitador = 0L;
			IngresoEgreso.IdInstrumento = 0L;
			IngresoEgreso.IdMoneda = "CLP";
			IngresoEgreso.IdMonedaOperacion = "";
			IngresoEgreso.IdOperacion = 0L;
			IngresoEgreso.IdPersona = fila.IdPersonaEntidad; //fila.IdPersonaCliente; //fila.IdPersona;
			IngresoEgreso.IdPersonaTitularPago = fila.IdPersonaEntidad; //fila.IdPersonaCliente;//fila.IdPersona;
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
			IngresoEgreso.Monto = fila.Monto;
			IngresoEgreso.MontoOperacion = fila.Monto;
			IngresoEgreso.MueveCuentaInversion = "S";
			IngresoEgreso.SecuenciaAgrupacion = "";
			IngresoEgreso.SecuenciaRelacionada = null;
			IngresoEgreso.TitularPago = "S";
			IngresoEgreso.ValorParidad = BigDecimal.valueOf(0);	
			paramsIngresoEgreso.add(IngresoEgreso);
			
			//Se crea forma de pago correspondiente al movimiento de tesoreria con los datos de recaudacion
			
			FormaDePago.NumeroReferencia = contador;
			FormaDePago.NumeroReferenciaFormaDePago = contador;
			FormaDePago.FechaDocumentoPagoCorredora = "0001-01-01";
			FormaDePago.FechaDocumentoPagoSocio = FechaSistema.toString()+" 00:00:00";
			FormaDePago.IdBancoSocio = 0L;//in.p_DevolucionIdBanco;
			FormaDePago.IdChequera = 0L;
			FormaDePago.IdCuentaBancariaCorredora = IdCtaBco; //122L
			FormaDePago.IdCuentaBancariaSocio = 0L;
			FormaDePago.IdMedioDePago = "TRA";//in.p_DevolucionIdMedioPago;
			FormaDePago.IdModalidadDePago = "MODDEP";
			FormaDePago.IdMoneda = "CLP";
			FormaDePago.IdTarjetaBancariaCorredora = 0L;
			FormaDePago.IdTarjetaBancariaSocio = 0L;
			FormaDePago.IdTipoCuentaBancariaSocio = "";
			FormaDePago.IdTipoTarjetaBancariaSocio = "";
			FormaDePago.Monto = fila.Monto;
			FormaDePago.NombreCuentaBancariaSocio = "";
			FormaDePago.NombreTipoCuentaBancariaSocio = "";
			FormaDePago.NombreTipoTarjetaBancariaSocio = "";
			FormaDePago.NumeroCuentaBancariaSocio = "";
			FormaDePago.NumeroDocumentoCorredora = 0L;
			FormaDePago.NumeroDocumentoSocio = 0L;
			FormaDePago.NumeroSerieSocio = "";
			FormaDePago.NumeroTarjetaBancariaSocio = "";
			
			paramsFormaDePago.add(FormaDePago);
						
			//Se crea forma de pago correspondiente al comprobante de tesoreria con los datos de recaudacion
			
			ComprobanteFormaDePago.NumeroReferencia = 1L;
			ComprobanteFormaDePago.IdMedioDePago = "TRA";
			ComprobanteFormaDePago.IdModalidadDePago ="MODDEP";
			ComprobanteFormaDePago.IdMoneda ="CLP";
			ComprobanteFormaDePago.Monto =fila.Monto;
			ComprobanteFormaDePago.IdCuentaBancariaCorredora = IdCtaBco; //=129L;
			ComprobanteFormaDePago.IdTarjetaBancariaCorredora =0L;
			ComprobanteFormaDePago.IdChequera =0L;
			ComprobanteFormaDePago.NumeroDocumentoCorredora =0L;
			ComprobanteFormaDePago.FechaDocumentoPagoCorredora ="0001-01-01";
			ComprobanteFormaDePago.IdCuentaBancariaSocio =0L;
			ComprobanteFormaDePago.IdBancoSocio =0L;
			ComprobanteFormaDePago.NombreCuentaBancariaSocio ="";
			ComprobanteFormaDePago.NumeroCuentaBancariaSocio ="";
			ComprobanteFormaDePago.IdTipoCuentaBancariaSocio ="";
			ComprobanteFormaDePago.NombreTipoCuentaBancariaSocio ="";
			ComprobanteFormaDePago.IdTarjetaBancariaSocio =0L;
			ComprobanteFormaDePago.IdTipoTarjetaBancariaSocio ="";
			ComprobanteFormaDePago.NombreTipoTarjetaBancariaSocio ="";
			ComprobanteFormaDePago.NumeroTarjetaBancariaSocio ="";
			ComprobanteFormaDePago.NumeroSerieSocio ="";
			ComprobanteFormaDePago.NumeroDocumentoSocio =0L;
			ComprobanteFormaDePago.FechaDocumentoPagoSocio =FechaSistema.toString();
			
			paramsComprobanteFormaDePago_Entrada.add(ComprobanteFormaDePago);
/*
			RecaudacionEstado.FolioRecaudacion = fila.FolioRecaudacion;
			RecaudacionEstado.IdRecaudacionEmpleador = fila.IdRecaudacionEmpleador;
			RecaudacionEstado.IdRecaudacionCliente = fila.IdRecaudacionCliente;
			RecaudacionEstado.IdEstado = "DEV";
			paramsCambioEstado.add(RecaudacionEstado);*/
			
		}

		TraspasoCuentasBancarias.ParametrosEntradaServicio paramsTrasCtaBca_Entrada = new TraspasoCuentasBancarias.ParametrosEntradaServicio();
		    
			paramsTrasCtaBca_Entrada.p_IdEmpresa = in.p_IdEmpresa;
			paramsTrasCtaBca_Entrada.p_IdUsuario = in.p_IdUsuario;
		    paramsTrasCtaBca_Entrada.p_IdModulo = in.p_IdModulo;
		    paramsTrasCtaBca_Entrada.p_Opcion = "DBF";
		    paramsTrasCtaBca_Entrada.p_Fecha = FechaSistema.toString();
		    paramsTrasCtaBca_Entrada.p_Monto = sumaMontos;		    
		    
		    TraspasoCuentasBancarias procesoTrasCtaBca = new TraspasoCuentasBancarias();
			TraspasoCuentasBancarias.ParametrosSalidaServicio OutTrasCtaBca_Entrada = procesoTrasCtaBca.logica(con, paramsTrasCtaBca_Entrada);

		    con.commit();
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

		for(CrearIngresoEgreso.IngresoEgresoGenerado_Salida outIngresoEgreso :OutTesoreria.P_OUT_INGRESOEGRESOGENERADO){

			CrearComprobante.IngresoEgresoGenerar_Entrada ComprobanteIngresoEgreso_Entrada = new CrearComprobante.IngresoEgresoGenerar_Entrada();
			ComprobanteIngresoEgreso_Entrada.NumeroReferencia = 1L;//outIngresoEgreso.NumeroReferencia;//1L;
			ComprobanteIngresoEgreso_Entrada.IdIngresoEgreso = outIngresoEgreso.IdIngresoEgreso;
			ComprobanteIngresoEgreso_Entrada.TipoComprobante = "PAREGR";
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

			LiquidarComprobante.ComprobanteLiquidar_Entrada ComprobanteLiquidar_Entrada = new LiquidarComprobante.ComprobanteLiquidar_Entrada();
			List<LiquidarComprobante.ComprobanteLiquidar_Entrada> paramsComprobanteLiquidar_Entrada = new ArrayList<LiquidarComprobante.ComprobanteLiquidar_Entrada>();

			if (MensajeSalida.getNumeroErrores(OutComprobante.P_OUT_MENSAJES) > 0) 
			{
				
			}else{
				for(int i = 0; i < OutComprobante.P_OUT_RESULTADOPROCESO.size(); i ++){
					if(OutComprobante.P_OUT_RESULTADOPROCESO.get(i).Campo.equals("IdComprobante")){
						ComprobanteLiquidar_Entrada.IdComprobante = OutComprobante.P_OUT_RESULTADOPROCESO.get(i).Id;
						ComprobanteLiquidar_Entrada.IdTipoMovimiento = "PAREGR";
						ComprobanteLiquidar_Entrada.AfectaCuentaInversion = "COR";
						paramsComprobanteLiquidar_Entrada.add(ComprobanteLiquidar_Entrada);
					}
				}
			}

			//LIQUIDACION
			/*
				LiquidarComprobante.LiquidarComprobante_Entrada paramsLiquidar_Entrada = new LiquidarComprobante.LiquidarComprobante_Entrada();
				
				paramsLiquidar_Entrada.p_IdEmpresa = in.p_IdEmpresa;
				paramsLiquidar_Entrada.p_IdUsuario = in.p_IdUsuario;
				paramsLiquidar_Entrada.p_IdModulo = in.p_IdModulo;
				paramsLiquidar_Entrada.p_UsaReglasNegocio = "S";
				paramsLiquidar_Entrada.p_FechaMovimiento = FechaSistema.toString();
				paramsLiquidar_Entrada.p_ComprobanteLiquidar = paramsComprobanteLiquidar_Entrada;
				
				LiquidarComprobante ProcesoLiquidar = new LiquidarComprobante();
				
				LiquidarComprobante.LiquidarComprobante_Salida OutLiquidar = ProcesoLiquidar.logica(con, paramsLiquidar_Entrada); 
				
				if (MensajeSalida.getNumeroErrores(OutLiquidar.P_OUT_MENSAJES) > 0) 
				{
					//salida.P_OUT_MENSAJES.addAll(OutLiquidar.P_OUT_MENSAJES);

					for (MensajeSalida filaMsg : OutLiquidar.P_OUT_MENSAJES) {
						MensajeSalida msg = new MensajeSalida();
						msg.id = 1L;
						msg.tipo = filaMsg.tipo;
						msg.codigo = filaMsg.codigo;
						msg.descripcion = filaMsg.descripcion;
						salida.P_OUT_MENSAJES.add(msg);	
					}
					return salida;
				}else{
					//salida.P_OUT_MENSAJES.addAll(OutLiquidar.P_OUT_MENSAJES);

					for (MensajeSalida filaMsg : OutLiquidar.P_OUT_MENSAJES) {
						MensajeSalida msg = new MensajeSalida();
						msg.id = 1L;
						msg.tipo = filaMsg.tipo;
						msg.codigo = filaMsg.codigo;
						msg.descripcion = filaMsg.descripcion;
						salida.P_OUT_MENSAJES.add(msg);	
					}
						
				}
				*/
		  //CAMBIO ESTADOS
		    CambiarEstadoRecaudacion.ParametrosEntradaServicio paramsCambioEstado_Entrada = new CambiarEstadoRecaudacion.ParametrosEntradaServicio();

		    paramsCambioEstado_Entrada.p_IdEmpresa = in.p_IdEmpresa;
		    paramsCambioEstado_Entrada.p_IdUsuario = in.p_IdUsuario;
		    paramsCambioEstado_Entrada.p_IdModulo = in.p_IdModulo;
		    paramsCambioEstado_Entrada.p_Parametros = paramsCambioEstado;
		    
		    CambiarEstadoRecaudacion ProcesoCambioEstado = new CambiarEstadoRecaudacion();

		    CambiarEstadoRecaudacion.ParametrosSalidaServicio OutCambioEstado = ProcesoCambioEstado.logica(con, paramsCambioEstado_Entrada); 
		    

		/*
		out.p_IngresoEgreso = paramsIngresoEgreso;
		out.p_FormaDePago = paramsFormaDePago;*/
		return out;
	}
	
	/********************
	 * Método Principal *
	 ********************/

	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getQAConnection()) {
			try {
				DevolucionBF_EntidadRecaudadora demo = new DevolucionBF_EntidadRecaudadora();	

				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();				
				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":39,\"p_IdModulo\":\"APV\",\"p_Parametros\":[{\"IdBonificacion\":61}]}";
				ParametrosEntradaServicio Objin = gson.fromJson(json, ParametrosEntradaServicio.class);
				
	 			try { 
	 				ParametrosSalidaServicio ObjOut = demo.logica(con, Objin);
	 				con.commit();
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