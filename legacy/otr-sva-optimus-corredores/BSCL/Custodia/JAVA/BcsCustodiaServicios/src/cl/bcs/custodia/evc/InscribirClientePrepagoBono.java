/***************************************************************************
* Purpose	: Inscrir un cliente con derecho para evento capital prepago bono
* Author	: JVILLAGRAN - BCS
* Date		: 14/04/2018
**************************************************************************/
package cl.bcs.custodia.evc;


import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.text.SimpleDateFormat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.custodia.ConsultarFlujoCustodia;
import cl.bcs.custodia.ConsultaCarterasSuscripcionCliente.ConsultaInstrumentoRelacionado;
import cl.bcs.custodia.ConsultaCarterasSuscripcionCliente.ConsultaSuscripcionCliente;
import cl.bcs.custodia.ConsultarFlujoCustodia.FlujoCustodia;
import cl.bcs.custodia.ConsultarFlujoCustodia.FlujoCustodiaDetallado;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;



public class InscribirClientePrepagoBono implements JavaCall {
	
	private final Log log = new Log("CUS.InscribirClientePrepagoBono");
	
		
	private final String SQL_SELECT_EVENTO_CAPITAL = loadResourceToString("cl/bcs/custodia/sql/InscribirClientePrepagoBono_SelectEventoCapital.sql");	
	private final String SQL_INSERT_EVENTO_CAPITAL_CLIENTE = loadResourceToString("cl/bcs/custodia/sql/InscribirClientePrepagoBono_InsertEventoCapitalCliente.sql");	
	private final String SQL_SELECT_CARTERA_BONO_CLIENTE = loadResourceToString("cl/bcs/custodia/sql/InscribirClientePrepagoBono_SelectEventoCapitalCliente.sql");
	
	
    //-------------------------------------------------------------------------------------------
	// CLASES QUE REPRESENTAN OBJETOS DE ENTRADA
	
	public static class InscribirClientePrepagoBono_Entrada
	{	
		public Long 			p_IdEmpresa;
		public Long 			p_IdUsuario;
		public String 			p_IdModulo;
		public List<EventoCapitalCliente_Entrada> p_EventoCapitalCliente; 
		
		public InscribirClientePrepagoBono_Entrada()
		{
			this.p_EventoCapitalCliente = new ArrayList<EventoCapitalCliente_Entrada>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
		
	
	public static class EventoCapitalCliente_Entrada
	{	
		public Long             IdInterno; 
		public Long 			IdEventoCapital;
		public Long 			IdCliente;
		public Long 			IdPortafolio;
	    public String 			IdClasificacionTipoInstrumento;     // Ej.: RF
	 	public String 			IdTipoInstrumento;                  // Ej.: BON
	  	public Long   			IdInstrumento; 				        // Ej.: 177542
		public BigDecimal 		CantidadNueva;
		public BigDecimal 		PrecioNuevo;
		public BigDecimal 		MontoCapital;
		public BigDecimal 		MontoInteres;
		public BigDecimal 		MontoReajuste;
		public BigDecimal 		Total;
		public Long				IdCartera;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
    //-------------------------------------------------------------------------------------------
	// CLASES QUE REPRESENTAN OBJETOS DE SALIDA	
	
    public static class InscribirClientePrepagoBono_Salida
    {	                                
    	public List<MensajeSalida> 		P_OUT_MENSAJES;
    	public Long 					P_OUT_ERRORESCONTADOR;
		public List<EventoCapitalCliente_Salida> P_EVENTOCAPITALCLIENTEGENERADO; 
		
		
		
    	public InscribirClientePrepagoBono_Salida()
    	{
    		this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
    		this.P_EVENTOCAPITALCLIENTEGENERADO = new ArrayList<EventoCapitalCliente_Salida>();
    	}
    	
    	public String toString() {
    		return Utils.printJson(this, false);
    	}
    }
    
	
	public static class EventoCapitalCliente_Salida
	{	
		public Long             IdInterno; 
		public Long 			IdEventoCapital;
		public Long 			IdEventoCapitalCliente;
		public Long 			IdCliente;
		public Long 			IdPortafolio;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
    //-------------------------------------------------------------------------------------------
	// CLASES UTILITARIAS DE APOYO	
   
	public static class ClientePrepagoBono_Proceso
	{	
		public String 		Llave;		// Llave compuesta IdEventoCapital + IdCliente + IdPortafolio
		public Long         IdCartera;
		public Long         IdInterno;
		public Long 		IdEventoCapital;
		public Long 		IdEventoCapitalCliente;  //Id del evento a crear
		public Long 		IdCliente;
		public Long 		IdPortafolio;		
		public String 		IdTipoEventoCapital; 		
		public Long 		Secuencia;         			
		public Long 		IdSocioDeNegocio;
		public String 		TipoDocumentoIdentidad;
		public String 		DocumentoIdentidad;
		public String 		RazonSocial;
		public String 		IdClasificacionTipoInstrumento;
		public String 		IdTipoInstrumento;	
		public Long 		IdInstrumento;
		public String 		Nemo;
		public Date 		FechaPago; //Fecha registrada al crear el evento capital
		public BigDecimal 	Cantidad;
		public BigDecimal 	Precio;
		public String       INGIdMonedaPagoCupon;    // Se usará para IdMoneda
		public Long 		INGNumeroCupon;          // Se usará para SecuenciaCupon
		public Long 		IdEmpleadoEjecutivoPortafolio;
		public String 		CodigoEmpleado;
		public String 		NombreCompletoEjecutivo;
		public String 		CodigoSucursal;
		public String 		IdBoveda;
		public String 		IdBolsa;
		public String 		IdTipoReajuste;
		public Long 		NumeroDividendo;
		public Long 		IdUsuario;
		public BigDecimal 	CantidadNueva;
		public BigDecimal 	PrecioNuevo;
		public BigDecimal 	MontoCapital;
		public BigDecimal 	MontoInteres;
		public BigDecimal 	MontoReajuste;
		public BigDecimal 	Total;
		public String 		CallConfirmado;
		public String 		CuponeraVariable;
		public Date 		INGFechaPagoCorteCupon;               //Fecha de pago del próximo corte cupón 
		public Long         IdCuentaCustodioPortafolio;
		public BigDecimal 	CantidadDisponibleFlujoCustodia;      //Cantidad disponible según flujo custodia
		public BigDecimal	MontoRetencion;
				
		//Para validaciones
		public long 	errores 		= 0;
		public long 	advertencias 	= 0;
		public long 	autorizaciones 	= 0;
		public long 	ok 				= 0;

		public String 		IdClasificacionTipoInstrumento_Real;
		public String 		IdTipoInstrumento_Real;	
		public Long 		IdInstrumento_Real;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	
	public static class EventoCapital
	{	
		public Long 		IdEventoCapital;
		public String 		IdTipoEventoCapital;
		public Long 		Secuencia;
		public String 		IdClasificacionTipoInstrumento;
		public String 		IdTipoInstrumento;	
		public Long 		IdInstrumento;
		public String 		Nemo;
		public Date 		FechaPago;   // Fecha de pago ingresada al crear el evento capital
		public Long 		NumeroDividendo;
		
		//Datos del instrumento bono
		public String 		CallConfirmado;
		public String 		CuponeraVariable; 
		
				
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	public static class CarteraBonoCliente
	{	
		public String 		Llave;		// Llave compuesta IdEventoCapital + IdCliente + IdPortafolio
		public Long         IdCartera;
		public Long 		IdSocioDeNegocio;
		public String 		TipoDocumentoIdentidad;
		public String 		DocumentoIdentidad;
		public String 		RazonSocial;
		public BigDecimal 	Cantidad;
		public BigDecimal 	Precio;
		public String       INGIdMonedaPagoCupon;    // Se usará para IdMoneda		
		public Long 		INGNumeroCupon;          // Se usará para SecuenciaCupon
		
		public Long 		IdEmpleadoEjecutivoPortafolio;
		public String 		CodigoEmpleado;
		public String 		NombreCompletoEjecutivo;
		public String 		CodigoSucursal;
		public String 		IdBoveda;
		public String 		IdBolsa;
		public String 		IdTipoReajuste;
		public Date 		INGFechaPagoCorteCupon;
		public Long 		IdCuentaCustodioPortafolio;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	
	

    //-------------------------------------------------------------------------------------------
	// METODOS UTILITARIOS	
	
	//Procesa salida de validaciones
	private void ProcesaRequisito (Connection con, Long idEmpresa, boolean esError, String codMsg, String CodRequisito, ClientePrepagoBono_Proceso pro, List<MensajeSalida> P_OUT_MENSAJES)
			throws Exception {
		if (esError)
		{
			String msg=Diccionario.getMensaje(con, idEmpresa, codMsg)					
					.add("IdCliente", pro.IdCliente)
					.add("Nemotecnico", pro.Nemo)
					.add("Nemo", pro.Nemo)
					.add("FechaPago", pro.FechaPago)
					.add("FechaPagoCupon", pro.INGFechaPagoCorteCupon)
				    .msg();
			 
			if (CodRequisito.equals(MensajeSalida.MENSAJE_ERROR)) 
			{
				P_OUT_MENSAJES.add(new MensajeSalida(pro.IdCliente,MensajeSalida.MENSAJE_ERROR,codMsg,msg));
				pro.errores++;
			}
			else if (CodRequisito.equals(MensajeSalida.MENSAJE_AUTORIZACION)) 
			{
				P_OUT_MENSAJES.add(new MensajeSalida(pro.IdCliente,MensajeSalida.MENSAJE_AUTORIZACION,codMsg,msg));
				pro.autorizaciones++;						
			}
			else if (CodRequisito.equals(MensajeSalida.MENSAJE_ADVERTENCIA)) 
			{
				P_OUT_MENSAJES.add(new MensajeSalida(pro.IdCliente,MensajeSalida.MENSAJE_ADVERTENCIA,codMsg,msg));
				pro.advertencias++;
			}
			else 
			{
				pro.ok++;
			}
		}
		else 
		{
			pro.ok++;
		}		
	}
	
	

	//-------------------------------------------------------------------------------------------
	// LOGICA DEL SERVICIO
	
	public InscribirClientePrepagoBono_Salida logica(Connection conn, InscribirClientePrepagoBono_Entrada entrada) throws Exception 
	{
		InscribirClientePrepagoBono_Salida salida = new InscribirClientePrepagoBono_Salida();
		salida.P_OUT_ERRORESCONTADOR = 0L;
		
		SQL sql = new SQL();		
		log.write("== INICIO INSCRIBIR CLIENTE PREPAGO BONO ==");
		
		try 
		{
			log.write("PARAMETROS DE ENTRADA : " + entrada.toString());
			
			if (entrada.p_EventoCapitalCliente.size() == 0) 
			{
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"NO_SE_ESPECIFICAN_EVENTOS_CAPITAL_A_PROCESAR",null,conn,entrada.p_IdEmpresa));				
				return salida;
			}
			
			
			
			//---------------------------------------------------------------------------------------------
			//VALIDA SI CUMPLE REQUISITOS BASICOS
			RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
			RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(conn, entrada.p_IdEmpresa, entrada.p_IdUsuario, entrada.p_IdModulo);
			if (MensajeSalida.getNumeroErrores(salidaBasicos.p_out_Mensajes) > 0) 
			{
				log.write("NO CUMPLE REQUISITOS BASICOS");
				salida.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
				return salida;
			}			

		
			
			
			//---------------------------------------------------------------------------------------------
			// OBTIENE DATOS EXTRAS DEL EVENTO CAPITAL
			
			HashMap<Long, EventoCapital> datosEventoCapital = new HashMap<>();
			{	
				HashMap<String,Object> paramConsultaEventoCapital = new HashMap<>();
				List<Long> idsEventoCapital  = new ArrayList<Long>();
				
				for (EventoCapitalCliente_Entrada ecc : entrada.p_EventoCapitalCliente)
				{	 
					idsEventoCapital.add(ecc.IdEventoCapital);
				}

				paramConsultaEventoCapital.put("IdsEventoCapital", idsEventoCapital);
				List<EventoCapital> resultConsultaEventoCapital = sql.query(conn, SQL_SELECT_EVENTO_CAPITAL, paramConsultaEventoCapital, EventoCapital.class);
				
				 
				HashMap<Object, Object> _datosEventoCapital = Utils.List2Hash(resultConsultaEventoCapital, "IdEventoCapital");  //Transforma la lista en Hash
				for(Object key: _datosEventoCapital.keySet())
				{
					datosEventoCapital.put((Long)key, (EventoCapital) _datosEventoCapital.get(key));
				}
				
				log.write("OBTIENE DATOS DEL EVENTO CAPITAL > " + resultConsultaEventoCapital);
			}
			



			//---------------------------------------------------------------------------------------------
			// OBTIENE DATOS EXTRAS DE LA CARTERA BONO DEL CLIENTE
			
			HashMap<String, CarteraBonoCliente> datosCarteraBonoCliente = new HashMap<>();
			{
				HashMap<String,Object> paramConsultaCarteraBonoCliente = new HashMap<>();

				String _llaveCarCliPor = "";
				List<String> llavesCarCliPor  = new ArrayList<String>();
				
				for (EventoCapitalCliente_Entrada ecc : entrada.p_EventoCapitalCliente)
				{
					//Arma llave compuesta para consulta
					_llaveCarCliPor = ecc.IdCliente.toString() + ecc.IdPortafolio.toString() + ecc.IdClasificacionTipoInstrumento + ecc.IdTipoInstrumento + ecc.IdInstrumento.toString() + ecc.IdCartera.toString();
					llavesCarCliPor.add(_llaveCarCliPor);
				}
				
				paramConsultaCarteraBonoCliente.put("LlavesCarCliPor", llavesCarCliPor);
				List<CarteraBonoCliente> resultConsultaCarteraBonoCliente = sql.query(conn, SQL_SELECT_CARTERA_BONO_CLIENTE, paramConsultaCarteraBonoCliente, CarteraBonoCliente.class);
				 
				HashMap<Object, Object> _datosCarteraBonoCliente = Utils.List2Hash(resultConsultaCarteraBonoCliente, "Llave");  //Transforma la lista resultConsultaEveCliCar en Hash
				for(Object key: _datosCarteraBonoCliente.keySet())
				{
					datosCarteraBonoCliente.put(key.toString(), (CarteraBonoCliente) _datosCarteraBonoCliente.get(key));
				}
				
				log.write("OBTIENE DATOS EXTRAS DE LA CARTERA DEL CLIENTE > " + resultConsultaCarteraBonoCliente);
			}
			
			
			
			
			//---------------------------------------------------------------------------------------------
			// INCORPORAR DATOS DE ENTRADA A DATOS PROCESO
			
			List<ClientePrepagoBono_Proceso> _ClientePrepagoBono_Proceso  = new ArrayList<ClientePrepagoBono_Proceso>();

			for (EventoCapitalCliente_Entrada ecc_entrada : entrada.p_EventoCapitalCliente)
			{	
				ClientePrepagoBono_Proceso cpb_Proceso = new ClientePrepagoBono_Proceso();
				
				Utils.copyPojo(ecc_entrada, cpb_Proceso);      //Copia los datos de entrada a proceso
				
				if (datosEventoCapital.size() > 0)
				{
					EventoCapital infoEventoCapital = (EventoCapital) datosEventoCapital.get(ecc_entrada.IdEventoCapital);
					
					cpb_Proceso.IdEventoCapital = infoEventoCapital.IdEventoCapital; 					
					cpb_Proceso.IdTipoEventoCapital = infoEventoCapital.IdTipoEventoCapital;
					cpb_Proceso.Secuencia = infoEventoCapital.Secuencia;
					cpb_Proceso.Nemo = infoEventoCapital.Nemo;
					cpb_Proceso.FechaPago = infoEventoCapital.FechaPago;
					cpb_Proceso.NumeroDividendo = infoEventoCapital.NumeroDividendo;
					cpb_Proceso.CallConfirmado = infoEventoCapital.CallConfirmado;
					cpb_Proceso.CuponeraVariable = infoEventoCapital.CuponeraVariable;
					cpb_Proceso.IdClasificacionTipoInstrumento_Real = infoEventoCapital.IdClasificacionTipoInstrumento;
					cpb_Proceso.IdTipoInstrumento_Real = infoEventoCapital.IdTipoInstrumento;						
					cpb_Proceso.IdInstrumento_Real = infoEventoCapital.IdInstrumento;
					
				}
				
				if (datosCarteraBonoCliente.size() > 0)
				{
					String llave = "";
					llave = ecc_entrada.IdCliente.toString() + ecc_entrada.IdPortafolio.toString() + ecc_entrada.IdClasificacionTipoInstrumento + ecc_entrada.IdTipoInstrumento + ecc_entrada.IdInstrumento + ecc_entrada.IdCartera;
					CarteraBonoCliente infoCarteraBonoCliente = (CarteraBonoCliente) datosCarteraBonoCliente.get(llave);
					Utils.copyPojo(infoCarteraBonoCliente, cpb_Proceso);
				}
				
				cpb_Proceso.IdUsuario = entrada.p_IdUsuario;
				cpb_Proceso.CantidadDisponibleFlujoCustodia = BigDecimal.ZERO;  //Mas adelante se actualiza con el flujo custodia  
				cpb_Proceso.MontoRetencion = BigDecimal.ZERO;

				_ClientePrepagoBono_Proceso.add(cpb_Proceso);
			}
			log.write("DATOS PROCESO: " + _ClientePrepagoBono_Proceso);
			
			
			
			//---------------------------------------------------------------------------------------------
			// CONSULTA FLUJO CUSTODIA PARA PREPAGO BONO(FLPRBO)
			
			log.write("CONSULTA FLUJO CUSTODIA");
			
			for (ClientePrepagoBono_Proceso  cpb: _ClientePrepagoBono_Proceso)	
			{
				//Llamado a Flujo de Custodia
				List<ConsultarFlujoCustodia.FlujoUso> dato_p_uso = new ArrayList<ConsultarFlujoCustodia.FlujoUso>();
				ConsultarFlujoCustodia.FlujoUso datosFlujoUso = new ConsultarFlujoCustodia.FlujoUso();
				datosFlujoUso.UsoTipoOperacion				= "FLEVCP";     //Flujo Prepago Bono
				datosFlujoUso.IdCliente						= cpb.IdCliente;
				datosFlujoUso.IdPortafolio					= cpb.IdPortafolio;
				datosFlujoUso.BusquedaEspecifica			= "FLUJO_CLIENTES_DERECHO_CAPITAL";
				datosFlujoUso.IdTipoInstrumentoEspecifico	= "";
				dato_p_uso.add(datosFlujoUso);
				
				List<ConsultarFlujoCustodia.FlujoFiltrosBusqueda> dato_p_FiltrosBusqueda = new ArrayList<ConsultarFlujoCustodia.FlujoFiltrosBusqueda>();
				ConsultarFlujoCustodia.FlujoFiltrosBusqueda datosFlujoFiltrosBusqueda = new ConsultarFlujoCustodia.FlujoFiltrosBusqueda();
				datosFlujoFiltrosBusqueda.IdClasificacionTipoInstrumento	= cpb.IdClasificacionTipoInstrumento;
				datosFlujoFiltrosBusqueda.IdTipoInstrumento					= cpb.IdTipoInstrumento;
				datosFlujoFiltrosBusqueda.IdInstrumento						= cpb.IdInstrumento;				
				datosFlujoFiltrosBusqueda.IdEmisor							= null;
				datosFlujoFiltrosBusqueda.FechaVencimiento					= null;
				datosFlujoFiltrosBusqueda.IdTipoReajuste					= null;
				datosFlujoFiltrosBusqueda.EsFungible						= null;
				datosFlujoFiltrosBusqueda.IdBoveda							= null;
				dato_p_FiltrosBusqueda.add(datosFlujoFiltrosBusqueda);
				
				List<ConsultarFlujoCustodia.FlujoEsAgrupadoPor> dato_p_EsAgrupadoPor = new ArrayList<ConsultarFlujoCustodia.FlujoEsAgrupadoPor>();
				ConsultarFlujoCustodia.FlujoEsAgrupadoPor datosFlujoEsAgrupadoPor = new ConsultarFlujoCustodia.FlujoEsAgrupadoPor();
				datosFlujoEsAgrupadoPor.ClasificacionTipoInstrumento	= "S";
				datosFlujoEsAgrupadoPor.TipoInstrumento					= "S";
				datosFlujoEsAgrupadoPor.Instrumento						= "S";
				datosFlujoEsAgrupadoPor.Emisor							= "N";
				datosFlujoEsAgrupadoPor.Nemotecnico						= "N";
				datosFlujoEsAgrupadoPor.NemoSVS							= "N";
				datosFlujoEsAgrupadoPor.FechaEmisionInstrumento			= "N";
				datosFlujoEsAgrupadoPor.FechaVencimientoInstrumento		= "N";
				datosFlujoEsAgrupadoPor.FechaLiquidacionInstrumento		= "N";
				datosFlujoEsAgrupadoPor.TipoReajuste					= "N";
				datosFlujoEsAgrupadoPor.EsFungible						= "N";
				datosFlujoEsAgrupadoPor.CodigoIFRS						= "N";
				datosFlujoEsAgrupadoPor.Boveda							= "N";
				datosFlujoEsAgrupadoPor.Bolsa							= "N";
				datosFlujoEsAgrupadoPor.TipoOperacion					= "N";
				datosFlujoEsAgrupadoPor.Mesa							= "N";
				datosFlujoEsAgrupadoPor.FolioRegistro					= "N";
				datosFlujoEsAgrupadoPor.ItemOrigen						= "N";
				dato_p_EsAgrupadoPor.add(datosFlujoEsAgrupadoPor);
				
				ConsultarFlujoCustodia llamadoFlujoCustodia = new ConsultarFlujoCustodia();
				ConsultarFlujoCustodia.FlujoCustodiaEntrada paramInSP = new ConsultarFlujoCustodia.FlujoCustodiaEntrada(); 
				paramInSP.p_IdEmpresa			= entrada.p_IdEmpresa;
				paramInSP.p_IdUsuario			= entrada.p_IdUsuario;
				paramInSP.p_IdModulo			= entrada.p_IdModulo;
				paramInSP.p_FechaSaldo			= cpb.FechaPago.toString();
				paramInSP.p_IdPais				= 166L;   // TODO : Leer desde configuracion
				paramInSP.p_DiasProyeccion		= 0;
				paramInSP.p_DiaHabil			= "S";
				paramInSP.p_ValorizarFlujo		= "N";    // Solo interesa cantidad no montos valorizados
				paramInSP.p_Uso					= dato_p_uso;
				paramInSP.p_FiltrosBusqueda		= dato_p_FiltrosBusqueda;
				paramInSP.p_EsAgrupadoPor		= dato_p_EsAgrupadoPor;
				
				ConsultarFlujoCustodia.FlujoCustodiaSalida salidaFlujo = llamadoFlujoCustodia.logica(conn, paramInSP);
				
				
				if (salidaFlujo.P_OUT_ERRORESCONTADOR == null || salidaFlujo.P_OUT_ERRORESCONTADOR.equals(0L) )
				{	
					for(FlujoCustodia flujoCustodiaTotalizado : salidaFlujo.p_out_FlujoTotalizado)
					{	
						cpb.CantidadDisponibleFlujoCustodia = flujoCustodiaTotalizado.Cantidad;
					};
					
				};
			}
			log.write("DATOS PROCESO CON FLUJO CUSTODIA: " + _ClientePrepagoBono_Proceso);
			
			
			//---------------------------------------------------------------------------------------------			
			// VALIDACIONES
			log.write("VALIDACION DE DATOS DE INSCRIPCION DE CLIENTES");
			
			for (ClientePrepagoBono_Proceso  cpb: _ClientePrepagoBono_Proceso)	
			{
				Boolean esInstrumentoCorrecto = (cpb.IdClasificacionTipoInstrumento.equalsIgnoreCase(cpb.IdClasificacionTipoInstrumento_Real) && cpb.IdTipoInstrumento.equalsIgnoreCase(cpb.IdTipoInstrumento_Real) && cpb.IdInstrumento.compareTo(cpb.IdInstrumento_Real) == 0); 
				ProcesaRequisito(conn, entrada.p_IdEmpresa, (!esInstrumentoCorrecto) , "INSTRUMENTO_NO_CORRESPONDE_A_EVENTO_CAPITAL", MensajeSalida.MENSAJE_ERROR, cpb, salida.P_OUT_MENSAJES);
				ProcesaRequisito(conn, entrada.p_IdEmpresa, (cpb.IdCartera == null || cpb.IdCartera == 0) , "CLIENTE_SIN_CARTERA_PARA_PREPAGO_BONO", MensajeSalida.MENSAJE_ERROR, cpb, salida.P_OUT_MENSAJES);
				ProcesaRequisito(conn, entrada.p_IdEmpresa, esInstrumentoCorrecto && ((cpb.CallConfirmado == null || cpb.CallConfirmado.equalsIgnoreCase("N") || cpb.CallConfirmado.equalsIgnoreCase("")) && cpb.IdCartera != null) , "BONO_SIN_CALL_CONFIRMADO", MensajeSalida.MENSAJE_ERROR, cpb, salida.P_OUT_MENSAJES);     
				ProcesaRequisito(conn, entrada.p_IdEmpresa, esInstrumentoCorrecto && ((cpb.CuponeraVariable == null || cpb.CuponeraVariable.equalsIgnoreCase("N") || cpb.CuponeraVariable.equalsIgnoreCase("")) && cpb.IdCartera != null), "BONO_SIN_CUPONERA", MensajeSalida.MENSAJE_ERROR, cpb, salida.P_OUT_MENSAJES);
				ProcesaRequisito(conn, entrada.p_IdEmpresa, esInstrumentoCorrecto && (cpb.FechaPago.compareTo(cpb.INGFechaPagoCorteCupon) != 0 && cpb.IdCartera != null), "FECHA_PAGO_NO_COINCIDE_CON_FECHA_CORTE", MensajeSalida.MENSAJE_ERROR, cpb, salida.P_OUT_MENSAJES);
				ProcesaRequisito(conn, entrada.p_IdEmpresa, esInstrumentoCorrecto && (cpb.CantidadNueva.compareTo(cpb.CantidadDisponibleFlujoCustodia) == 1 && cpb.IdCartera != null), "CANTIDAD_CUSTODIA_INSUFICIENTE", MensajeSalida.MENSAJE_ERROR, cpb, salida.P_OUT_MENSAJES);
			}
			
			
			if (MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES) > 0)
			{
				log.write("INSCRIPCION DE CLIENTE PB CON ERRORES DE VALIDACION");
				
				for (MensajeSalida mensaje : salida.P_OUT_MENSAJES)
				{
					log.write("ERROR VALIDACION > " + mensaje.descripcion);
				}
				
				return salida;
			}
			
			
			
			//---------------------------------------------------------------------------------------------
			// INSERCION DE NUEVOS EVENTOS DE CAPITAL CLIENTE
			
			//Genera las secuencias de los nuevos eventos capital clientes
			List<Long> secuenciasEventoCapitalCliente = Utils.getSecuencia(conn, "BSCL.Custodia.BD.Secuencias::EventoCapitalCliente", new Long(_ClientePrepagoBono_Proceso.size()));
			
			for (ClientePrepagoBono_Proceso cpb_Proceso: _ClientePrepagoBono_Proceso)
			{	
				cpb_Proceso.IdEventoCapitalCliente = secuenciasEventoCapitalCliente.get(_ClientePrepagoBono_Proceso.indexOf(cpb_Proceso));
			}
			
			
			if (_ClientePrepagoBono_Proceso.size() > 0)
			{
				log.write("INSERCION DE NUEVOS EVENTOS DE CAPITAL CLIENTE");
				sql.batch(conn, SQL_INSERT_EVENTO_CAPITAL_CLIENTE, _ClientePrepagoBono_Proceso , ClientePrepagoBono_Proceso.class);
				
			}
			
			
			//---------------------------------------------------------------------------------------------
			// PROCESAR DATOS SALIDA
			
			if (_ClientePrepagoBono_Proceso.size() > 0)
			{
				log.write("PROCESAR DATOS PARA LA SALIDA");
				
				for (ClientePrepagoBono_Proceso cpb_Proceso: _ClientePrepagoBono_Proceso)
				{
					EventoCapitalCliente_Salida ecc_salida = new EventoCapitalCliente_Salida();
					Utils.copyPojo(cpb_Proceso, ecc_salida);
					salida.P_EVENTOCAPITALCLIENTEGENERADO.add(ecc_salida);
				}
			}

			
			
		}
		catch(Exception e)
		{	
			log.write("ERROR > ", e);
		}
		
		log.write("SALIDA SERVICIO : " + salida.toString());
		log.write("== TERMINO INSCRIBIR CLIENTE PREPAGO BONO ==");
		
		return salida;
	}


	//-------------------------------------------------------------------------------------------
	// OBJETO DE TRANSPORTE
	
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = new Gson();
		//Entrada in = gson.fromJson(jin.toString(), Entrada.class);
		InscribirClientePrepagoBono_Entrada in = gson.fromJson(jin.toString(), InscribirClientePrepagoBono_Entrada.class);
		
		InscribirClientePrepagoBono_Salida out = logica(conn,in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		
		
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();	
		return dataout;
	}
	
	
	//-------------------------------------------------------------------------------------------
	// METODO PARA PRUEBAS LOCALES
	
	public static void main(String[] args) throws Exception {
		System.out.println("== INICIO PRUEBA LOCAL ==");

		try (Connection con = PlataformaUtils.getDESAPEConnection())
		{
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
	
			////String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":69,\"p_IdModulo\":\"CUS\",\"p_EventoCapitalCliente\": [{\"IdInterno\":1,\"IdEventoCapital\":129,\"IdCliente\":175014,\"IdPortafolio\":40760,\"CantidadNueva\":100.00,\"PrecioNuevo\":200.00,\"MontoCapital\"=111.11,\"MontoInteres\"=222.22,\"MontoReajuste\"=333.33,\"Total\"=444.44}]}";
			//String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":69,\"p_IdModulo\":\"CUS\",\"p_EventoCapitalCliente\": [{\"IdInterno\":1,\"IdEventoCapital\":162,\"IdCliente\":174993,\"IdPortafolio\":40738, \"IdClasificacionTipoInstrumento\": \"RF\", \"IdTipoInstrumento\": \"BON\", \"IdInstrumento\": 177542, \"CantidadNueva\":100.00,\"PrecioNuevo\":200.00,\"MontoCapital\"=111.11,\"MontoInteres\"=222.22,\"MontoReajuste\"=333.33,\"Total\"=444.44}]}"; 
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":103,\"p_IdModulo\":\"CUS\",\"p_EventoCapitalCliente\":[{\"IdInterno\":1,\"IdEventoCapital\":225,\"IdCliente\":116606,\"IdPortafolio\":20262,\"IdClasificacionTipoInstrumento\":\"RF\",\"IdTipoInstrumento\":\"BON\",\"IdInstrumento\":177542,\"CantidadNueva\":1000,\"PrecioNuevo\":105.8483,\"MontoCapital\":53950,\"MontoInteres\":0.04,\"MontoReajuste\":0,\"Total\":731.66}]}";
			
			InscribirClientePrepagoBono_Entrada Objin = gson.fromJson(json, InscribirClientePrepagoBono_Entrada.class);

			InscribirClientePrepagoBono demo = new InscribirClientePrepagoBono();
			
			System.out.println("ENTRADA:");
			System.out.println(Objin.toString());

			try
			{
				InscribirClientePrepagoBono_Salida ObjOut = demo.logica(con, Objin);
				//con.commit();
				con.rollback();
				
				System.out.println("SALIDA:");
				ObjOut.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(ObjOut.P_OUT_MENSAJES);
				System.out.println(ObjOut.toString());
			} catch(Exception er) {
				//er.printStackTrace();
				con.rollback();
			}
		}
		System.out.println("== FIN PRUEBA LOCAL ==");
    }
	

}
