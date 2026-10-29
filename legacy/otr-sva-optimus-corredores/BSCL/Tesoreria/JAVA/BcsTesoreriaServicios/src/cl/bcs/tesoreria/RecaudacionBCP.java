package cl.bcs.tesoreria;

/**
 * Proposito: Recibe una transacción desde BCP(Pago o extorno) y lo procesa en optimus: 
 *            - Crea Movimientos y Comprobantes de IE (usa preferencias de pago corredora) 
 *            - Valida horario y liquida comprobantes(Si está en horario se liquidan)
 *            - Cuando se liquida, mueve cuenta inversión            
 * Autor: JVILLAGRAN - BCS
 * Fecha Creacion: 18/09/2017
 */ 

import java.math.BigDecimal;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.ObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.OutObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente.FechasIn;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.OutObtenerDiaSiguiente.FechasOut;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.plataforma.pla.utils.FechaDB;


import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

import cl.bcs.tesoreria.CrearIngresoEgreso;
import cl.bcs.tesoreria.CrearIngresoEgreso.CrearIngresoEgreso_Entrada;
import cl.bcs.tesoreria.CrearIngresoEgreso.CrearIngresoEgreso_Salida;
import cl.bcs.tesoreria.CrearIngresoEgreso.IngresoEgreso_Entrada;
import cl.bcs.tesoreria.LiquidarComprobante;
import cl.bcs.tesoreria.ConsultaBCP.Cliente;
import cl.bcs.tesoreria.ConsultaBCP.Consulta_Entrada;
import cl.bcs.tesoreria.ConsultaBCP.Consulta_Proceso;
import cl.bcs.tesoreria.ConsultaBCP.MensajeTransaccionBCP;
import cl.bcs.tesoreria.LiquidarComprobante.LiquidarComprobante_Entrada;
import cl.bcs.tesoreria.LiquidarComprobante.LiquidarComprobante_Salida;
import cl.bcs.tesoreria.LiquidarComprobante.ComprobanteLiquidar_Entrada;



public class RecaudacionBCP implements JavaCall {
	
	// Log
	private final Log log = new Log("TES.RecaudacionBCP");
	
	// Querys
	private final String SQL_INSERT_LogMensajeTransaccion = loadResourceToString("cl/bcs/tesoreria/sql/RecaudacionBCP_InsertLogMensajeTransaccion.sql");
	private final String SQL_SELECT_LogMensajeTransaccion = loadResourceToString("cl/bcs/tesoreria/sql/RecaudacionBCP_SelectLogMensajeTransaccion.sql");	
	private final String SQL_UPDATE_LogMensajeTransaccion = loadResourceToString("cl/bcs/tesoreria/sql/RecaudacionBCP_UpdateLogMensajeTransaccion.sql");
	
	private final String SQL_SELECT_Comprobante = "SELECT \"IdComprobante\", \"IdTipoMovimiento\", 'CLI' AS \"AfectaCuentaInversion\" FROM \"BSCL.Tesoreria.BD::TES.Comprobante\" WHERE \"IdComprobante\" IN (:idsComprobantes)";
	private final String SQL_SELECT_ConfiguracionTesoreria = loadResourceToString("cl/bcs/tesoreria/sql/Globales_SelectConfiguracionTesoreria.sql");	
	private final String SQL_SELECT_PreferenciaPagoCorredor = loadResourceToString("cl/bcs/tesoreria/sql/Globales_SelectPreferenciaPagoCorredor.sql");
	private final String SQL_SELECT_Cliente = loadResourceToString("cl/bcs/tesoreria/sql/RecaudacionBCP_SelectCliente.sql");

	
	// Constantes
	private final Long idEmpresa = 1L;
	
	//// FALTA .... sacar de configuracion
	private final Long idUsuario = 69L;	
	private final Long idPaisEmpresa = 44L;
	
	
	
    //-------------------------------------------------------------------------------------------
	// CLASES UTILITARIAS QUE REPRESENTAN OBJETOS IN
	//-------------------------------------------------------------------------------------------
	
	public static class RecaudacionBCP_Entrada 
	{	
		public String               IdSolicitudBcp;		   		// MFX_SvcRecaudaTrn/SvcRecaudaTrnRq/RqUID
		public String               NumeroOperacionBcp;    		// MFX_SvcRecaudaTrn/SvcRecaudaTrnRq/MsjRqHdr/CtxRqHdr/OpnNro
		public String               NumeroOperacionExtornoBcp; 	// MFX_SvcRecaudaTrn/SvcRecaudaTrnRq/MsjRqHdr/CtxRqHdr/TrnExtorno/OpnExtornoNro  (numero operacion de origen bcp que se está extornando)
		public String               FechaOperacionBcp;     		// MFX_SvcRecaudaTrn/SvcRecaudaTrnRq/MsjRqHdr/CtxRqHdr/ClientFec		
		public String       		TipoOperacionBCP;     		// PAGO | EXTORNO | CONSULTA
		public String 				MensajeEntrada;		
		public List<Operacion_Entrada>	OperacionesBcp;

		public RecaudacionBCP_Entrada()
		{	
			this.OperacionesBcp = new ArrayList<Operacion_Entrada>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	

	public static class Operacion_Entrada
	{	
		public String		IdClienteBcp;  			// MFX_SvcRecaudaTrn/SvcRecaudaTrnRef/SvcEmpLst/SvcEmp/PagId; >> identificador depositante BCP
	   	public String    	IdMoneda;  		 		
	   	public BigDecimal 	Monto;  		 		
	}
	
	
    //-------------------------------------------------------------------------------------------
	// CLASES UTILITARIAS QUE REPRESENTAN OBJETOS OUT	
	//-------------------------------------------------------------------------------------------
		
	public static class RecaudacionBCP_Salida
	{	
		public String                               P_OUT_FECHAPROCESO; //fecha de sistema en que se generó el movimiento y comprobante de tesorería 
		public List<MensajeSalida>					P_OUT_MENSAJES;
		public Long									P_OUT_ERRORESCONTADOR;
		public List<ResultadoProceso_Salida>        P_OUT_RESULTADOPROCESO;
		
		public RecaudacionBCP_Salida()
		{
			this.P_OUT_MENSAJES 			 = new ArrayList<MensajeSalida>();
			this.P_OUT_RESULTADOPROCESO		 = new ArrayList<ResultadoProceso_Salida>();
		}
	
        
		public String toString() {
			return Utils.printJson(this, false);
		}
	}


	public static class ResultadoProceso_Salida
	{
		public String 	Campo;        // VARCHAR(40)   -- Ej: "IdIngresoEgreso"
		public Long   	Id;           // BIGINT        -- IdIngresoEgreso generado  Ej: 9999
		public String 	Servicio;     // VARCHAR(250)  -- Ej: "BSCL.Tesoreria.BD.Servicios::CrearIngresoEgreso"
		public String	Resultado;    // VARCHAR(40)   -- Ej: "ING"
		
		public String toString(){
			return Utils.printJson(this, false);
		}
	}


	//-------------------------------------------------------------------------------------------
	// CLASES UTILITARIAS QUE REPRESENTAN OBJETOS DE PROCESAMIENTO
	//-------------------------------------------------------------------------------------------

	public static class Operacion_Proceso 
	{	 
		public String  		LlaveCliente;  //Llave de Cliente(IdEmpresa + IdTipoDocumentoIdentidad + DocumentoIdentidad)
		public Long    		IdMensajeTransaccion; //Id del LOG registrado con del mensaje de entrada de la transaccion
		public Long 		IdPersona;
		public Long 		IdCliente;
		public String		IdClienteBcp;   //Identificador del depositante bcp
		public Long   		IdPortafolioDefecto;
		public String 		IdTipoSocioDeNegocio;
		public String		IdEstadoPersona;
		public String		IdEstadoCliente;		  			
	   	public String    	IdMoneda;  		 				//	NVARCHAR(3)
	   	public BigDecimal 	Monto;  		 				//	DECIMAL(26,8)
	   	
	   	
		//Para validaciones
		public 		long 	errores 			= 0;
		public 		long 	ok 					= 0;
		public 		long 	autorizaciones		= 0;
		public 		long  	advertencias		= 0;
		
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	//Parámetros de configuración de tesorería
	public static class ConfiguracionTesoreria
	{
		public String 	CodigoParametro;
		public String 	Valor;
		public String 	Descripcion;
		 
		public String toString(){
			return Utils.printJson(this, false);
		}
	}
 

	public static class PreferenciaPagoCorredor	
	{	
		public Long   		IdPreferenciasPagoCorredor;
		public String		IdTipoMovimiento;
		public String		IdConcepto;
		public String		IdMedioDePago;
		public String		IdModalidadDePago;
		public Long   		IdCuentaBanco;
		public String		IdEstado;
		public Long   		IdBanco;
		public String		NombreCuentaBancaria;
		public String		IdTipoCuentaBancaria;
		public String		IdMonedaCuentaBanco;
		public String		NumeroCuentaBancaria;
		public String		IdEstadoCuentaBanco;
		
		public String toString(){
			return Utils.printJson(this, false);
		}			
	}


	public static class Cliente	
	{
		public String		LlaveCliente;  //llave del registro: IdEmpresa + TipoDocumentoIdentidad + DocumentoIdentidad (Ej.: '1DNI12969673', '1RUC20101031340')
		public Long   		IdPersona;
		public Long			IdCliente;
		public String		NombreCompleto;
		public String		IdEstadoPersona;
		public String		IdEstadoCliente;
		public String       IdTipoDocumentoIdentidad;
		public String		DocumentoIdentidad;
		public Long   		IdPortafolioDefecto; 
		public String		IdTipoSocioDeNegocio;
		
		public String toString(){
			return Utils.printJson(this, false);
		}			
	}	
	
	
	public static class ComprobanteOrigen	
	{	
		public Log		    		IdComprobante;
		public BigDecimal 			Monto;
		public Long					IdComprobanteOrigen;
		public String				IdTipoComprobanteOrigen;
		public java.sql.Timestamp	FechaComprobanteOrigen;
		
		public String toString(){
			return Utils.printJson(this, false);
		}			
	}
	
	
	public static class MensajeTransaccionBCP	
	{	
		public Long         		IdMensajeTransaccion;
		public String				RqUID;     			
		public String				TipoTrasaccion;   // CONSULTA | PAGO | EXTORNO
		public String				MensajeEntrada;   // XML enviado desde BCP a Optimus   (en formato json)
		public String				MensajeSalida;    // XML de respuesta de Optimus a BCP (en formato json)
		public java.sql.Timestamp	FechaHoraRecepcion; 
		public java.sql.Date		FechaSistema;
		public Long         		IdEmpresa;
		
		public String toString(){
			return Utils.printJson(this, false);
		}			
	}		
	
	
	//Enum TipoOperacionBCP(clase especial que limita la creación de objetos a PAGO o EXTORNO)
	// Concepto --> Concepto con el que entrarán los movimientos por recaudación BCP (RECBCP)
	// Tipo Movimiento --> MOVING para pagos / MOVEGR para extornos
	// Glosa  --> Glosa descriptiva
	public enum TipoOperacionBCP
	{
		PAGO("RECBCP","MOVING","Ingreso por Operación de Pago BCP"),
		EXTORNO("RECBCP","MOVEGR","Egreso por Operación de Extorno BCP");
		
		private String IdConcepto;
		private String IdTipoMovimiento;
		private String Glosa;
		
		public String getConcepto()
		{
			return IdConcepto;
		}
	
		public String getTipoMovimiento()
		{
			return IdTipoMovimiento;
		}
		
		public String getGlosa()
		{
			return Glosa;
		}		
		
		private TipoOperacionBCP(String concepto,String tipoMovimiento,String glosa)
		{
			this.IdConcepto = concepto;
			this.IdTipoMovimiento = tipoMovimiento;
			this.Glosa = glosa;
		}
	}




	//-------------------------------------------------------------------------------------------
	// OBJETO DE TRANSPORTE
	//-------------------------------------------------------------------------------------------
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		
		RecaudacionBCP_Entrada in = gson.fromJson(jin.toString(), RecaudacionBCP_Entrada.class);
		RecaudacionBCP_Salida out = logica(con, in);
		
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	
	
	//-------------------------------------------------------------------------------------------
	// METODOS UTILITARIOS PARA PROCESAMIENTO EN LOGICA DEL SERVICIO
	//-------------------------------------------------------------------------------------------	
	
	
	private void ProcesaRequisitoTransaccionBCP(Connection con, Long idEmpresa, boolean esError, String codMsg, String CodRequisito, Operacion_Proceso proceso, List<MensajeSalida> P_OUT_MENSAJES,String CodErrorBcp)
			throws Exception {
		if (esError) 
		{
			String msg=Diccionario.getMensaje(con, idEmpresa, codMsg)
					.add("IdCliente", proceso.IdClienteBcp)
					.add("IdPersona", proceso.IdClienteBcp)
					.add("TipoDocumentoIdentidad", proceso.IdClienteBcp)					
					
				    .msg();			
			 
			if (CodRequisito.equals(MensajeSalida.MENSAJE_ERROR)) 
			{
				P_OUT_MENSAJES.add(new MensajeSalida(proceso.IdMensajeTransaccion,MensajeSalida.MENSAJE_ERROR,CodErrorBcp,msg));
				proceso.errores++;
			}
			else if (CodRequisito.equals(MensajeSalida.MENSAJE_AUTORIZACION))
			{
				P_OUT_MENSAJES.add(new MensajeSalida(proceso.IdMensajeTransaccion,MensajeSalida.MENSAJE_AUTORIZACION,CodErrorBcp,msg));
				proceso.autorizaciones++;						
			}
			else if (CodRequisito.equals(MensajeSalida.MENSAJE_ADVERTENCIA)) 
			{
				P_OUT_MENSAJES.add(new MensajeSalida(proceso.IdMensajeTransaccion,MensajeSalida.MENSAJE_ADVERTENCIA,CodErrorBcp,msg));
				proceso.advertencias++;
			}
			else 
			{
				proceso.ok++;
			}
		}
		else 
		{
			proceso.ok++;
		}		
	}
	

	
	//Quita del proceso los movimientos que no son validos 
	public void limpiarIngresosEgresos(List<Operacion_Proceso> operacionProceso, List<Operacion_Proceso> operacionConErrores) 
	{
		for (Operacion_Proceso iep : operacionProceso) 
		{
			if (iep.errores > 0) {
				operacionConErrores.add(iep);
			}
		}
		
		for (Operacion_Proceso iep : operacionConErrores) 
		{
			//Remueve el movimiento
			if (operacionProceso.contains(iep))
				operacionProceso.remove(iep);
		}
	}

	
	
	//-------------------------------------------------------------------------------------------
	// LOGICA DEL SERVICIO
	//-------------------------------------------------------------------------------------------
	
	public RecaudacionBCP_Salida logica(Connection con, RecaudacionBCP_Entrada in) throws Exception {
		SQL _sql = new SQL();
		
		RecaudacionBCP_Salida out = new RecaudacionBCP_Salida();
		List<Long> idsComprobantesGenerados = new ArrayList<Long>();
		List<Long> idsComprobantesLiquidados = new ArrayList<Long>();
		ArrayList<Operacion_Proceso> operacionConErrores = new ArrayList<>();
		String horarioAperturaRecaudacionBcp = "";
		String horarioCierreRecaudacionBcp = "";
		String fechaLiquidacion = "";
		Boolean estaEnHorario = false;
		Long numeroReferencia = 0L;
		
		log.write("== INICIO RECAUDACION BCP ==");
		
		try
		{
			log.write("RECIBE PARAMETROS DE ENTRADA : " + in.toString());
			
			String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "PLATAFORMA", "PLA.FechaSistema");

			
			FechaDB fechaDB = new FechaDB();
			//String fechaHoraSistema = fechaDB.ObtenerFechaHoraSistemaUTC(con, idEmpresa);
			String fechaHoraSistema = FechaDB.ObtenerFechaHoraSistemaLocal(con, idEmpresa);
			out.P_OUT_FECHAPROCESO = fechaHoraSistema; //fechaSistema;
			
			if (in.OperacionesBcp.size() == 0) 
			{
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"NO_SE_INGRESARON_SOLICITUDES_DE_INGRESO_EGRESO",null,con,idEmpresa));
				return out;
			}
			
			
			//---------------------------------------------------------------------------------------------
			// GUARDA MENSAJE DE ENTRADA ENVIADO POR BCP(como log y para respuesta)
			List<MensajeTransaccionBCP> MensajeTransaccionBCP = new ArrayList<MensajeTransaccionBCP>();
			SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd");
			
			MensajeTransaccionBCP _mensajeTransaccionBCP = new MensajeTransaccionBCP();
			_mensajeTransaccionBCP.RqUID = in.IdSolicitudBcp;
			_mensajeTransaccionBCP.TipoTrasaccion = in.TipoOperacionBCP;  // PAGO | EXTORNO  | CONSULTA
			_mensajeTransaccionBCP.MensajeEntrada = in.MensajeEntrada;
			_mensajeTransaccionBCP.FechaSistema = new java.sql.Date(formato.parse(fechaSistema).getTime());
			_mensajeTransaccionBCP.IdEmpresa = idEmpresa;
			
			MensajeTransaccionBCP.add(_mensajeTransaccionBCP);
			
			_sql.batch(con, SQL_INSERT_LogMensajeTransaccion, MensajeTransaccionBCP, MensajeTransaccionBCP.class);
			

			
			//---------------------------------------------------------------------------------------------
			//OBTIENE REGISTRO DEL LOG (PARA ACTUALIZARLO AL FINAL DEL PROCESO)
			HashMap<String, MensajeTransaccionBCP> mensajeTransaccionBCP = new HashMap<>();
			{
				HashMap<String,Object> parametrosConsultaLMT = new HashMap<>();
				
				List<String> _RqUIDs = new ArrayList<String>();
				_RqUIDs.add(in.IdSolicitudBcp);
				
				parametrosConsultaLMT.put("IdEmpresa", idEmpresa);
				parametrosConsultaLMT.put("TipoTrasaccion", in.TipoOperacionBCP);  // in.TipoOperacionBCP: PAGO | EXTORNO
				parametrosConsultaLMT.put("RqUIDs", _RqUIDs);
				
				List<MensajeTransaccionBCP> resultConsultaLMT = _sql.query(con, SQL_SELECT_LogMensajeTransaccion, parametrosConsultaLMT, MensajeTransaccionBCP.class);
				
				for (MensajeTransaccionBCP mtBCP: resultConsultaLMT)
				{
					if (mtBCP.RqUID.equalsIgnoreCase(in.IdSolicitudBcp))
					{
						mensajeTransaccionBCP.put(mtBCP.RqUID, mtBCP);
						break; //toma el primero que es ul ultimo registrado ya que está ordenado en forma desc (en caso de haber más de uno)
					}
				}
				
				if (mensajeTransaccionBCP.size() < 1)
				{
					log.write("ERROR EN REGISTRO DEL MENSAJE DE ENTRADA BCP");
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"ERROR_EN_REGISTRO_MENSAJE_ENTRADA_BCP",null,con,idEmpresa));
					return out;							
				}

				log.write("CONSULTA REGISTRO DE MENSAJE DE ENTRADA OK > " + mensajeTransaccionBCP.get(in.IdSolicitudBcp).RqUID);				
			}
			
			
			
			//---------------------------------------------------------------------------------------------
			//OBTIENE PARAMETROS DE HORARIO APERTURA/CIERRE DE RECAUDACION BCP
			HashMap<String, ConfiguracionTesoreria> configuracionTesoreria = new HashMap<>();
			{	
				HashMap<String,Object> parametrosConsultaCT = new HashMap<>();
				
				List<String> codigoParametros = new ArrayList<String>();
				codigoParametros.add("HORARIOAPERTURARECAUDACIONBCP"); 
				codigoParametros.add("HORARIOCIERRERECAUDACIONBCP");
				
				parametrosConsultaCT.put("IdEmpresa", idEmpresa);
				parametrosConsultaCT.put("CodigoParametros", codigoParametros);
				
				List<ConfiguracionTesoreria> resultCosultaCT = _sql.query(con, SQL_SELECT_ConfiguracionTesoreria, parametrosConsultaCT, ConfiguracionTesoreria.class);
				
				HashMap<Object, Object>__configuracionTesoreria = Utils.List2Hash(resultCosultaCT, "CodigoParametro");  //Transforma la lista resultCosultaCT en Hash 
				for(Object key: __configuracionTesoreria.keySet())
				{
					configuracionTesoreria.put((String)key,(ConfiguracionTesoreria) __configuracionTesoreria.get(key));
				}
				
				if (configuracionTesoreria.size() > 0)
				{
					horarioAperturaRecaudacionBcp = configuracionTesoreria.get("HORARIOAPERTURARECAUDACIONBCP").Valor;
					horarioCierreRecaudacionBcp = configuracionTesoreria.get("HORARIOCIERRERECAUDACIONBCP").Valor;
				}
				
				if (horarioAperturaRecaudacionBcp.equalsIgnoreCase("") || horarioCierreRecaudacionBcp.equalsIgnoreCase(""))
				{
					log.write("ERROR > OBTIENE PARAMETROS DE HORARIO APERTURA/CIERRE DE RECAUDACION BCP");
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"NO_EXISTE_PARAMETRO_HORARIO_RECAUDACION_BCP",null,con,idEmpresa));
					return out;					
				}
				
				log.write("CONSULTA HORARIOS DE APERTURA/CIERRE RECAUDACION BCP > " + horarioAperturaRecaudacionBcp +" / "+ horarioCierreRecaudacionBcp);
			}
			
			
			//--------------------------------------------------------------------------------------------- 
			// ELIMINA LOS CEROS DE LA IZQUIERDE DEL IdClienteBcp (Id depositante Bcp)
			// Ej.: 0000012969 >> 12969
			for (Operacion_Entrada operacionEntrada : in.OperacionesBcp)
			{
				if (isNumeric(operacionEntrada.IdClienteBcp))
				{
					Long lngIdClienteBcp = Long.parseLong(operacionEntrada.IdClienteBcp);
					String strIdClienteBcp = lngIdClienteBcp.toString();
					operacionEntrada.IdClienteBcp = strIdClienteBcp;					
				}
			}
			log.write("ELIMINA CEROS DE LA IZQUIERDA DEL IDENTIFICADOR CLIENTE BCP (IdClienteBcp)");		
 
			
			//---------------------------------------------------------------------------------------------
			// VALIDACION DE HORARIO DE RECEPCION DE OPERACIONES                                                                                                                                                                                                                                                                                                                                                           
			{				
				Date fechaHoraActual, fechaHoraApertura, fechaHoraCierre;
		        Date fechaActual = new Date();
		        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
	
		        String horaActual = df.format(fechaActual); // hora actual
		        
		        fechaHoraActual = df.parse(horaActual); // hora actual con fecha de comparación
		        fechaHoraApertura = df.parse(horarioAperturaRecaudacionBcp); // hora apertura con fecha de comparación
		        fechaHoraCierre = df.parse(horarioCierreRecaudacionBcp);  // hora cierre con fecha de comparación
		        
		        if (fechaHoraActual.compareTo(fechaHoraApertura) >= 0 && fechaHoraActual.compareTo(fechaHoraCierre) <= 0)  // 0: F1=F2   -1: F1<(F2)  1 F1>(F2)
		        {	
		        	log.write("VALIDACION DE HORARIO > " + horaActual + " DENTRO DE HORARIO PERMITIDO");
		        	estaEnHorario = true;
		        }
		        else
		        {	// Hora está fuera del intervalo
		        	log.write("VALIDACION DE HORARIO > " + horaActual + " FUERA DE HORARIO PERMITIDO");		        	
		        	estaEnHorario = false;
		        }
			}
			
			

			//---------------------------------------------------------------------------------------------			
			//CALCULAR FECHA LIQUIDACION (Si está en horario es fecha actual(de sistema) en caso contrario T+1)
			{	
				if (estaEnHorario)
				{
					fechaLiquidacion = fechaSistema;
				}
				else
				{	//Se le suma 1 día hábil a la fecha de sistema
					ObtenerDiaSiguiente obtenerDiaSiguiente = new ObtenerDiaSiguiente();
					InObtenerDiaSiguiente inObtenerDiaSiguiente = new InObtenerDiaSiguiente();
					
					inObtenerDiaSiguiente.p_idPais = idPaisEmpresa;
					inObtenerDiaSiguiente.p_diaHabil = "S";
					inObtenerDiaSiguiente.p_fechas = new ArrayList<FechasIn>();
					
					FechasIn fechasIn = new FechasIn();
					fechasIn.fechaInicial = fechaSistema;
					fechasIn.numeroDias = 1L;
					inObtenerDiaSiguiente.p_fechas.add(fechasIn);
					
					OutObtenerDiaSiguiente outObtenerDiaSiguiente = obtenerDiaSiguiente.logica(con, inObtenerDiaSiguiente);
					
					for (FechasOut fecha : outObtenerDiaSiguiente.p_out_fechas) 
					{
						fechaLiquidacion = fecha.fechaCalculada;
					}				
				}
				
				if (fechaLiquidacion.equals("") || fechaLiquidacion == null)
				{
					log.write("ERROR > CALCULAR FECHA LIQUIDACION");
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"FECHA_LIQUIDACION_VACIA",null,con,idEmpresa));
					return out;							
				}
				
				log.write("SE CALCULA FECHA LIQUIDACION > " + fechaLiquidacion);
			}
			

			
			//---------------------------------------------------------------------------------------------
			// OBTIENE DATOS DEL CLIENTE Y PORTAFOLIO POR DEFECTO			
			HashMap<String, Cliente> cliente = new HashMap<>();
			{	
				HashMap<String,Object> paramConsultaCliente = new HashMap<>();
				
				String llaveCliente = "";
				List<String> llavesClientes  = new ArrayList<String>();
				
				for (Operacion_Entrada operacionEntrada : in.OperacionesBcp)
				{	
					//Arma llave para cliente. El criterio de consulta es una llave compuesta: IdEmpresa + TipoDocumentoIdentidad + DocumentoIdentidad (Ej.: '1DNI12969673', '1RUC20101031340')
					if (operacionEntrada.IdClienteBcp.length() == 8)    //Largo 8 --> Persona Natural (usa DNI)						
					    llaveCliente = idEmpresa.toString() + "DNI" + operacionEntrada.IdClienteBcp; 
					else if (operacionEntrada.IdClienteBcp.length() == 11)    //Largo 11 --> Persona Jurídica (usa RUC)
						llaveCliente = idEmpresa.toString() + "RUC" + operacionEntrada.IdClienteBcp;
										
					llavesClientes.add(llaveCliente);
				}

				paramConsultaCliente.put("llavesClientes", llavesClientes);
				List<Cliente> resultConsultaCLI = _sql.query(con, SQL_SELECT_Cliente, paramConsultaCliente, Cliente.class);
				
				 
				HashMap<Object, Object>__cliente = Utils.List2Hash(resultConsultaCLI, "LlaveCliente");  //Transforma la lista resultConsultaCLI en Hash
				for(Object key: __cliente.keySet())
				{
					cliente.put(key.toString(), (Cliente) __cliente.get(key));
				}
				
				log.write("OBTIENE DATOS CLIENTE > " + resultConsultaCLI);
			}

	
			

			//******************************************************************************
			//---------------------------------------------------------------------------------------------
			// OBTIENE DATOS DE ORIGEN PARA EXTORNOS (FALTA)
			//******************************************************************************

			
			//---------------------------------------------------------------------------------------------
			// OBTIENE PREFERENCIAS DE PAGO DEL CORREDOR PARA RECAUDACION BCP
			HashMap<String, PreferenciaPagoCorredor> preferenciaPagoCorredor = new HashMap<>();
			{	
				HashMap<String,Object> parametrosConsultaPPC = new HashMap<>();
				
				List<String> idConceptos = new ArrayList<String>();
				List<String> idTipoMovimientos = new ArrayList<String>();
				
				if (in.TipoOperacionBCP.equalsIgnoreCase("PAGO"))
				{	
					idConceptos.add(TipoOperacionBCP.PAGO.getConcepto());						
					idTipoMovimientos.add(TipoOperacionBCP.PAGO.getTipoMovimiento());
				}
				else if (in.TipoOperacionBCP.equalsIgnoreCase("EXTORNO"))
				{
					idConceptos.add(TipoOperacionBCP.EXTORNO.getConcepto());
					idTipoMovimientos.add(TipoOperacionBCP.EXTORNO.getTipoMovimiento());
				}
				
				parametrosConsultaPPC.put("IdEmpresa", idEmpresa);
				parametrosConsultaPPC.put("IdConceptos", idConceptos);
				parametrosConsultaPPC.put("IdTipoMovimientos", idTipoMovimientos);
				
				List<PreferenciaPagoCorredor> resultConsultaPPC = _sql.query(con, SQL_SELECT_PreferenciaPagoCorredor, parametrosConsultaPPC, PreferenciaPagoCorredor.class);
				 
				HashMap<Object, Object>__preferenciaPagoCorredor = Utils.List2Hash(resultConsultaPPC, "IdTipoMovimiento");  //Transforma la lista resultCosultaPPC en Hash
				for(Object key: __preferenciaPagoCorredor.keySet())
				{
					preferenciaPagoCorredor.put((String)key,(PreferenciaPagoCorredor) __preferenciaPagoCorredor.get(key));
				}
				
				if (preferenciaPagoCorredor.size() == 0)
				{
					log.write("ERROR > NO EXISTEN PREFERENCIAS DE PAGO DEL CORREDOR PARA RECAUDACION BCP");
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"NO_EXISTE_PREFERENCIAS_PAGO_CORREDOR_RECAUDACION_BCP",null,con,idEmpresa));
					return out;
				}
				
				log.write("CONSULTA PREFERENCIAS DE PAGO DEL CORREDOR > " + resultConsultaPPC);
			}
			
			
			//---------------------------------------------------------------------------------------------
			// INCORPORAR DATOS DE ENTRADA A DATOS PROCESO
			List<Operacion_Proceso> operacion_Proceso  = new ArrayList<Operacion_Proceso>();
			
			for (Operacion_Entrada operacionEntrada : in.OperacionesBcp)
			{	
				//Arma llave de Cliente(IdEmpresa + IdTipoDocumentoIdentidad + DocumentoIdentidad) para buscar tupla
				String llaveCliente = ""; 
				if (operacionEntrada.IdClienteBcp.length() == 8)   //Largo 8 --> Persona Natural (usa DNI)
					llaveCliente = idEmpresa.toString() + "DNI" + operacionEntrada.IdClienteBcp;
				else if (operacionEntrada.IdClienteBcp.length() == 11)  // Persona Jurídica (usa RUC)
					llaveCliente = idEmpresa.toString() + "RUC" + operacionEntrada.IdClienteBcp;
				else
					llaveCliente = ""; //Si la llave es vacía saltará en las validaciones 
				
				Cliente infoCliente = (Cliente) cliente.get(llaveCliente); //Busca y asigna cliente
				
				Operacion_Proceso _operacion_Proceso = new Operacion_Proceso();
				Utils.copyPojo(operacionEntrada, _operacion_Proceso);

				_operacion_Proceso.LlaveCliente = llaveCliente; /* acá */
				_operacion_Proceso.IdPersona = (infoCliente == null) ? 0 : infoCliente.IdPersona;				
				_operacion_Proceso.IdCliente = (infoCliente == null) ? 0 : infoCliente.IdCliente;				
				_operacion_Proceso.IdPortafolioDefecto =  (infoCliente == null) ? 0 : infoCliente.IdPortafolioDefecto;
				_operacion_Proceso.IdTipoSocioDeNegocio = (infoCliente == null) ? "" : infoCliente.IdTipoSocioDeNegocio;
				_operacion_Proceso.IdEstadoPersona = (infoCliente == null) ? "" : infoCliente.IdEstadoPersona;
				_operacion_Proceso.IdEstadoCliente = (infoCliente == null) ? "" : infoCliente.IdEstadoCliente;				
				_operacion_Proceso.IdMensajeTransaccion = mensajeTransaccionBCP.get(in.IdSolicitudBcp).IdMensajeTransaccion; 
				
				operacion_Proceso.add(_operacion_Proceso);
			}
			log.write("PROCESO DE DATOS DE ENTRADA: " + operacion_Proceso);
	
			
			//---------------------------------------------------------------------------------------------
			//VALIDACIONES DE DATOS
			{
				for (Operacion_Proceso _operacionProceso : operacion_Proceso)
				{
					ProcesaRequisitoTransaccionBCP(con, idEmpresa, (_operacionProceso.LlaveCliente == null || _operacionProceso.LlaveCliente == ""),"TIPO_DOCUMENTO_IDENTIDAD_DESCONOCIDO",MensajeSalida.MENSAJE_ERROR, _operacionProceso , out.P_OUT_MENSAJES, "CP0140"); // CP0140 >> Id Depositante restringido
					ProcesaRequisitoTransaccionBCP(con, idEmpresa, (_operacionProceso.IdPersona == null || _operacionProceso.IdPersona == 0),"PERSONA_NO_EXISTE",MensajeSalida.MENSAJE_ERROR, _operacionProceso , out.P_OUT_MENSAJES, "CP0140");					
					ProcesaRequisitoTransaccionBCP(con, idEmpresa, ((_operacionProceso.IdCliente == null || _operacionProceso.IdCliente == 0) && _operacionProceso.IdPersona != 0),"CLIENTE_NO_EXISTE",MensajeSalida.MENSAJE_ERROR, _operacionProceso , out.P_OUT_MENSAJES,"CP0140");
					ProcesaRequisitoTransaccionBCP(con, idEmpresa, (!_operacionProceso.IdEstadoPersona.equalsIgnoreCase("ACT") && _operacionProceso.IdPersona != 0),"PERSONA_NO_ACTIVA",MensajeSalida.MENSAJE_ERROR, _operacionProceso , out.P_OUT_MENSAJES, "CP0140");					
					ProcesaRequisitoTransaccionBCP(con, idEmpresa, (!_operacionProceso.IdEstadoCliente.equalsIgnoreCase("ACT") && _operacionProceso.IdPersona != 0 && _operacionProceso.IdCliente != 0),"CLIENTE_NO_ACTIVO",MensajeSalida.MENSAJE_ERROR, _operacionProceso , out.P_OUT_MENSAJES,"CP0140");					
					ProcesaRequisitoTransaccionBCP(con, idEmpresa, (_operacionProceso.IdPortafolioDefecto == 0  && _operacionProceso.IdPersona != 0  && _operacionProceso.IdCliente != 0),"PORTAFOLIO_DEFECTO_NO_EXISTE",MensajeSalida.MENSAJE_ERROR, _operacionProceso , out.P_OUT_MENSAJES, "CP0140");
					
					
					// ************************* ********************************* *********************************
					//INI - SOLO PARA CERTIFICACION BCP - DESPUES DE DEBE BORRAR (para estos DNI se forza un error para la cerificacion bcp)
					ProcesaRequisitoTransaccionBCP(con, idEmpresa, (in.TipoOperacionBCP.equalsIgnoreCase("PAGO") && _operacionProceso.IdClienteBcp.equalsIgnoreCase("67412388") && _operacionProceso.IdPersona != 0  && _operacionProceso.IdCliente != 0),"CLIENTE_NO_ACTIVO",MensajeSalida.MENSAJE_ERROR, _operacionProceso , out.P_OUT_MENSAJES, "CP0140");
					if (in.TipoOperacionBCP.equalsIgnoreCase("PAGO") && _operacionProceso.IdClienteBcp.equalsIgnoreCase("44607843") && _operacionProceso.IdPersona != 0  && _operacionProceso.IdCliente != 0)
					{
						throw new Exception("Error");
					}
					
					ProcesaRequisitoTransaccionBCP(con, idEmpresa, (in.TipoOperacionBCP.equalsIgnoreCase("EXTORNO") && _operacionProceso.IdClienteBcp.equalsIgnoreCase("44225588") && _operacionProceso.IdPersona != 0  && _operacionProceso.IdCliente != 0),"CLIENTE_NO_ACTIVO",MensajeSalida.MENSAJE_ERROR, _operacionProceso , out.P_OUT_MENSAJES, "CP0139");
					if (in.TipoOperacionBCP.equalsIgnoreCase("EXTORNO") && _operacionProceso.IdClienteBcp.equalsIgnoreCase("11225588") && _operacionProceso.IdPersona != 0  && _operacionProceso.IdCliente != 0)
					{
						throw new Exception("Error");
					}
					
					//FIN - SOLO PARA CERTIFICACION BCP - DESPUES DE DEBE BORRAR (para estos DNI se forza un error para la cerificacion bcp)
					// ************************* ********************************* *********************************
					
					
				}
				
				limpiarIngresosEgresos(operacion_Proceso, operacionConErrores); //limpia los registros que no pasaron las validaciones
				
				if (operacion_Proceso.size() == 0) 
				{
					log.write("DATOS DE MOVIMIENTOS INVALIDALIDOS > " + operacionConErrores.toString());
				}
				else
				{	
					log.write("VALIDACION DE DATOS REALIZADA");
				}
				
			}
			

			
			// --------------------------------------------------------------
			// CREAR MOVIMIENTOS INGRESO/EGRESO Y COMPROBANTES DE TESORERIA
			
			if (operacion_Proceso.size() > 0 )
			{
				log.write("SE INVOCA SERVICIO CREAR MOVIMIENTOS I/E (CrearIngreso.java)");
				
				CrearIngresoEgreso_Entrada crearIngresoEgresoEntrada = new CrearIngresoEgreso_Entrada();
				
				crearIngresoEgresoEntrada.p_IdEmpresa = idEmpresa;
				crearIngresoEgresoEntrada.p_IdUsuario = idUsuario;
				crearIngresoEgresoEntrada.p_IdModulo = "TES";
				crearIngresoEgresoEntrada.p_SimularOperacion = "N";
				crearIngresoEgresoEntrada.p_UsaReglasNegocio = "S";		
				crearIngresoEgresoEntrada.p_EsIngresoAutomatico = "S";
				
				List<CrearIngresoEgreso.IngresoEgreso_Entrada> IngresoEgresoEntrada = new ArrayList<CrearIngresoEgreso.IngresoEgreso_Entrada>();
				List<CrearIngresoEgreso.FormaDePago_Entrada> FormaDePagoEntrada = new ArrayList<CrearIngresoEgreso.FormaDePago_Entrada>();
				
				 
				//Lee y asigna los datos para generar movimientos de tesoreria
				for (Operacion_Proceso _operacion_Proceso : operacion_Proceso)
				{
					numeroReferencia++;
					CrearIngresoEgreso.IngresoEgreso_Entrada _ingresoEgresoEntrada =  new CrearIngresoEgreso.IngresoEgreso_Entrada();
					
					Utils.copyPojo(_operacion_Proceso, _ingresoEgresoEntrada);
					

					if (in.TipoOperacionBCP.equalsIgnoreCase("PAGO"))
					{
						_ingresoEgresoEntrada.IdConcepto = TipoOperacionBCP.PAGO.getConcepto();
						_ingresoEgresoEntrada.IdTipoMovimiento = TipoOperacionBCP.PAGO.getTipoMovimiento();
						_ingresoEgresoEntrada.Glosa = TipoOperacionBCP.PAGO.getGlosa() + " - IdSolicitudBcp: " + in.IdSolicitudBcp;
					}
					else if (in.TipoOperacionBCP.equalsIgnoreCase("EXTORNO"))
					{
						_ingresoEgresoEntrada.IdConcepto = TipoOperacionBCP.EXTORNO.getConcepto();
						_ingresoEgresoEntrada.IdTipoMovimiento = TipoOperacionBCP.EXTORNO.getTipoMovimiento();
						_ingresoEgresoEntrada.Glosa = TipoOperacionBCP.EXTORNO.getGlosa() + " - IdSolicitudBcp: " + in.IdSolicitudBcp + " - NumeroOperacionExtorno: " + in.NumeroOperacionExtornoBcp;
					}
				
					
					_ingresoEgresoEntrada.IdPortafolio = _operacion_Proceso.IdPortafolioDefecto;	
					_ingresoEgresoEntrada.IdTipoPersona = _operacion_Proceso.IdTipoSocioDeNegocio;		//	Es el IdTipoSocioDeNegocio (CLI / EMP / PRO / OPE)
					_ingresoEgresoEntrada.IdPersona = _operacion_Proceso.IdPersona;					
					
					_ingresoEgresoEntrada.FechaLiquidacion = fechaLiquidacion;
					_ingresoEgresoEntrada.NumeroReferencia = numeroReferencia;
					_ingresoEgresoEntrada.IdCaja = 0L;	   	
					_ingresoEgresoEntrada.SecuenciaAgrupacion = ""; 		
					_ingresoEgresoEntrada.SecuenciaRelacionada = 0L;	 	
	   	
					
					//El títular pago es el mismo cliente
					_ingresoEgresoEntrada.TitularPago = "N";
					_ingresoEgresoEntrada.IdTipoPersonaTitularPago = _operacion_Proceso.IdTipoSocioDeNegocio;  //	NVARCHAR(3)    -- Es el IdTipoSocioDeNegocio (CLI / EMP / PRO / OPE)
					_ingresoEgresoEntrada.IdPersonaTitularPago = _operacion_Proceso.IdPersona;  		 		//	BIGINT
					_ingresoEgresoEntrada.IdPortafolioTitularPago = _operacion_Proceso.IdPortafolioDefecto;  	//	BIGINT
					
					
					//Parámetros Referenciales(Opcionales)
					_ingresoEgresoEntrada.IdEmpleadoDigitador = 0L;  			//	BIGINT
					_ingresoEgresoEntrada.IdSucursalEmpleadoDigitador = 0L; 	//	BIGINT
					_ingresoEgresoEntrada.IdTipoProducto = "";  		 		//	NVARCHAR(4)
					_ingresoEgresoEntrada.IdTipoOperacion = "";		 			//	NVARCHAR(6)									    

					//Datos de origen BCP
					_ingresoEgresoEntrada.IdTipoComprobante = "WS";  		 		//	NVARCHAR(3)  -- WS(Carga online x WS) | BAT(Carga batch archivo conciliación)					
					_ingresoEgresoEntrada.IdComprobante = Long.parseLong((in.NumeroOperacionBcp == "" || in.NumeroOperacionBcp == null) ? "0" : in.NumeroOperacionBcp);
					_ingresoEgresoEntrada.FechaComprobante = in.FechaOperacionBcp;
					_ingresoEgresoEntrada.FechaLiquidacionComprobante = "0001-01-01";
					
					
					if (in.TipoOperacionBCP.equalsIgnoreCase("PAGO"))					
						_ingresoEgresoEntrada.IdOperacion = 0L;
					else if (in.TipoOperacionBCP.equalsIgnoreCase("EXTORNO"))					
						_ingresoEgresoEntrada.IdOperacion = Long.parseLong(in.NumeroOperacionExtornoBcp);

					
					_ingresoEgresoEntrada.FechaOperacion = "0001-01-01";  		//	NVARCHAR(40)
					_ingresoEgresoEntrada.IdCondicionLiquidacion = ""; 			//	NVARCHAR(2)
					_ingresoEgresoEntrada.IdBolsa = "";  		 				//	NVARCHAR(3)
					_ingresoEgresoEntrada.IdMonedaOperacion = "";  		 		//	NVARCHAR(3)
					_ingresoEgresoEntrada.MontoOperacion = BigDecimal.ZERO;		//	DECIMAL(26,8)
					_ingresoEgresoEntrada.IdTipoReajuste = "";	 				//	NVARCHAR(3)
					_ingresoEgresoEntrada.FechaParidad = "0001-01-01";  		//	NVARCHAR(40)
					_ingresoEgresoEntrada.ValorParidad = BigDecimal.ZERO;		// 	DECIMAL(26,8)
					_ingresoEgresoEntrada.FolioCartera = 0L;  		 			//	BIGINT
					_ingresoEgresoEntrada.IdInstrumento = 0L;  		 			//	BIGINT
					_ingresoEgresoEntrada.IdTipoInstrumento = "";  		 		//	NVARCHAR(3)
					_ingresoEgresoEntrada.IdClasificacionTipoInstrumento = ""; 	//	NVARCHAR(3)

					//Indicadores
					_ingresoEgresoEntrada.Bilateral = "N";
					_ingresoEgresoEntrada.GeneraComprobante = "S";
					_ingresoEgresoEntrada.AgruparEnComprobante = "N";
					_ingresoEgresoEntrada.MueveCuentaInversion = "S";
					
					IngresoEgresoEntrada.add(_ingresoEgresoEntrada);
					
					
					
					// --------------------------------------------------------------
					// Asigna Forma de Pago(según preferencias de pago de la corredora para recaudacion BCP)
					{
						CrearIngresoEgreso.FormaDePago_Entrada _formaDePago =  new CrearIngresoEgreso.FormaDePago_Entrada();
						
						_formaDePago.NumeroReferencia = _ingresoEgresoEntrada.NumeroReferencia;
						_formaDePago.NumeroReferenciaFormaDePago = _ingresoEgresoEntrada.NumeroReferencia; //como siempre será una sola FP, asignamos el mismo numeroReferencia(si son + de 1 FP debe ir 1,2,..)

						
						if (in.TipoOperacionBCP.equalsIgnoreCase("PAGO"))
						{
							_formaDePago.IdMedioDePago = preferenciaPagoCorredor.get("MOVING").IdMedioDePago;
							_formaDePago.IdModalidadDePago = preferenciaPagoCorredor.get("MOVING").IdModalidadDePago;
							_formaDePago.IdCuentaBancariaCorredora = preferenciaPagoCorredor.get("MOVING").IdCuentaBanco;
						}
						else
						{
							_formaDePago.IdMedioDePago = preferenciaPagoCorredor.get("MOVEGR").IdMedioDePago;
							_formaDePago.IdModalidadDePago = preferenciaPagoCorredor.get("MOVEGR").IdModalidadDePago;
							_formaDePago.IdCuentaBancariaCorredora = preferenciaPagoCorredor.get("MOVEGR").IdCuentaBanco;
						}
						
						_formaDePago.IdMoneda = _ingresoEgresoEntrada.IdMoneda;
						_formaDePago.Monto = _ingresoEgresoEntrada.Monto;
						//Forma de Pago de parte de la Corredora
						
						_formaDePago.IdTarjetaBancariaCorredora = 0L; 				
						_formaDePago.IdChequera = 0L; 								
						_formaDePago.NumeroDocumentoCorredora = 0L; 				
						_formaDePago.FechaDocumentoPagoCorredora = "0001-01-01";	
						//Forma de Pago de parte del socio de Negocio
						_formaDePago.IdCuentaBancariaSocio = 0L; 					
						_formaDePago.IdBancoSocio = 0L; 							
						_formaDePago.NombreCuentaBancariaSocio = "";				
						_formaDePago.NumeroCuentaBancariaSocio = "";				
						_formaDePago.IdTipoCuentaBancariaSocio = "";				
						_formaDePago.NombreTipoCuentaBancariaSocio = "";			
						_formaDePago.IdTarjetaBancariaSocio = 0L;					
						_formaDePago.IdTipoTarjetaBancariaSocio = "";  				
						_formaDePago.NombreTipoTarjetaBancariaSocio = ""; 			
						_formaDePago.NumeroTarjetaBancariaSocio = "";  				
						_formaDePago.NumeroSerieSocio = "";  						
						_formaDePago.NumeroDocumentoSocio = 0L;						
						_formaDePago.FechaDocumentoPagoSocio = "0001-01-01";		
						
						FormaDePagoEntrada.add(_formaDePago);
					}
					
				}
				crearIngresoEgresoEntrada.p_IngresoEgresoGenerar =  IngresoEgresoEntrada;
				crearIngresoEgresoEntrada.p_FormaDePago =  FormaDePagoEntrada;
				
				
				
				log.write("PARAMETROS ENVIADOS A CREAR MOVIMIENTOS I/E : " + crearIngresoEgresoEntrada);
				
				CrearIngresoEgreso crearIngresoEgreso = new CrearIngresoEgreso();
				CrearIngresoEgreso_Salida crearIngresoEgresoSalida = crearIngresoEgreso.logica(con, crearIngresoEgresoEntrada);
				
				if (MensajeSalida.getNumeroErrores(crearIngresoEgresoSalida.P_OUT_MENSAJES) > 0) 
				{
					log.write("ERROR > CREACION DE MOVIMIENTOS Y COMPROBANTES : " + crearIngresoEgresoSalida.P_OUT_MENSAJES);
					out.P_OUT_MENSAJES.addAll(crearIngresoEgresoSalida.P_OUT_MENSAJES);
					return out;
				}
				else
				{
					log.write("CREACION DE MOVIMIENTOS Y COMPROBANTES OK - MENSAJES > " + crearIngresoEgresoSalida.P_OUT_MENSAJES);
					out.P_OUT_MENSAJES.addAll(crearIngresoEgresoSalida.P_OUT_MENSAJES);
				}
				 
				
				for (CrearIngresoEgreso.ResultadoProceso_Salida resultadoProcesoCIE: crearIngresoEgresoSalida.P_OUT_RESULTADOPROCESO)
				{
					//Obtiene y agrega a la salida el resultado del proceso de crear movimientos y comprobantes de Ingreso/Egreso	
					ResultadoProceso_Salida resultadoProcesoBCP = new ResultadoProceso_Salida();
					Utils.copyPojo(resultadoProcesoCIE, resultadoProcesoBCP);
					out.P_OUT_RESULTADOPROCESO.add(resultadoProcesoBCP);
				
					//Obtiene Id de comprobantes generados
					if (resultadoProcesoCIE.Campo.equalsIgnoreCase("IdComprobante") && resultadoProcesoCIE.Resultado.equalsIgnoreCase("ING"))
					{
						idsComprobantesGenerados.add(resultadoProcesoCIE.Id);
					}
				}
				
				
				if (idsComprobantesGenerados.size() > 0)
					log.write("ID COMPROBANTES GENERADOS: " + idsComprobantesGenerados);
			}

			
			
			// --------------------------------------------------------------
			// LIQUIDAR COMPROBANTES (Solo si está en horario permitido)

			if (idsComprobantesGenerados.size() > 0 && estaEnHorario)
			{
				log.write("SE INVOCA SERVICIO LIQUIDAR COMPROBANTE (LiquidarComprobante.java)");
				LiquidarComprobante_Entrada liquidarComprobanteEntrada = new LiquidarComprobante_Entrada();
				
				liquidarComprobanteEntrada.p_IdEmpresa = idEmpresa;
				liquidarComprobanteEntrada.p_IdUsuario = idUsuario;
				liquidarComprobanteEntrada.p_IdModulo = "TES";
				liquidarComprobanteEntrada.p_SimularOperacion = "N"; 
				liquidarComprobanteEntrada.p_UsaReglasNegocio = "S";
				liquidarComprobanteEntrada.p_FechaMovimiento = fechaSistema;
				
				//Comprobantes a Liquidar	
				HashMap<String,Object> parametrosConsultarComprobantes = new HashMap<>();
				parametrosConsultarComprobantes.put("idsComprobantes", idsComprobantesGenerados);
				List<ComprobanteLiquidar_Entrada> _comprobanteLiquidar = _sql.query(con, SQL_SELECT_Comprobante, parametrosConsultarComprobantes, ComprobanteLiquidar_Entrada.class);
	
				liquidarComprobanteEntrada.p_ComprobanteLiquidar = _comprobanteLiquidar;
				//liquidarComprobantesEntrada.p_FormaDePagoLiquidar = null;  // se liquida por comprobante completo, no parciales
				
				log.write("PARAMETROS ENVIADOS A LIQUIDAR COMPROBANTE : " + liquidarComprobanteEntrada);
				
				LiquidarComprobante liquidarComprobante = new LiquidarComprobante();
				LiquidarComprobante_Salida liquidarComprobante_Salida = liquidarComprobante.logica(con, liquidarComprobanteEntrada);
				
				if (MensajeSalida.getNumeroErrores(liquidarComprobante_Salida.P_OUT_MENSAJES) > 0) 
				{
					log.write("ERROR > LIQUIDAR COMPROBANTES : " + liquidarComprobante_Salida.P_OUT_MENSAJES);
					out.P_OUT_MENSAJES.addAll(liquidarComprobante_Salida.P_OUT_MENSAJES);
					return out;
				}
				else
				{
					log.write("LIQUIDAR COMPROBANTES OK: " + liquidarComprobante_Salida.P_OUT_MENSAJES);
					out.P_OUT_MENSAJES.addAll(liquidarComprobante_Salida.P_OUT_MENSAJES);
				}
				
				//Obtiene Id de comprobantes liquidados
				for (LiquidarComprobante.ResultadoProceso_Salida resultadoProcesoLC: liquidarComprobante_Salida.P_OUT_RESULTADOPROCESO)
				{	
					//Obtiene y agrega a la salida el resultado del proceso de liquidar comprobante	
					ResultadoProceso_Salida resultadoProcesoBCP = new ResultadoProceso_Salida();
					Utils.copyPojo(resultadoProcesoLC, resultadoProcesoBCP);
					out.P_OUT_RESULTADOPROCESO.add(resultadoProcesoBCP);
					
					if (resultadoProcesoLC.Resultado.equalsIgnoreCase("LIQ"))
					{
						idsComprobantesLiquidados.add(resultadoProcesoLC.Id);
					}
				}
				log.write("ID COMPROBANTES LIQUIDADOS: " + idsComprobantesLiquidados);
			}
			

			
			//--------------------------------------------------------------------------------------------- 		
			// ACTUALIZA LOG(guardado al comienzo) CON LOS DATOS DE SALIDA (Respuesta enviada desde Optimus a BCP)
			
			List<MensajeTransaccionBCP> paramUpdateMensajeTransaccionBCP = new ArrayList<MensajeTransaccionBCP>();
			
			for(Object key: mensajeTransaccionBCP.keySet())
			{
				mensajeTransaccionBCP.get(key).MensajeSalida = out.toString();				
				paramUpdateMensajeTransaccionBCP.add(mensajeTransaccionBCP.get(key));
			}
			
			if (paramUpdateMensajeTransaccionBCP.size() > 0)
			{
				_sql.batch(con, SQL_UPDATE_LogMensajeTransaccion, paramUpdateMensajeTransaccionBCP, MensajeTransaccionBCP.class);	
			}
			
			
			
		}
		catch(Exception e)
		{
				log.write(e);
				throw e;
		}
		finally 
		{

		}
	
		log.write("== TERMINO RECAUDACION BCP ==");
		return out;
		
	}
	
	
	//-------------------------------------------------------------------------------------------
	// METODOS DE APOYO LOGICA
	//-------------------------------------------------------------------------------------------	
	
	//Retorna "Verdadero" si un string es numérido y "Falso" si no es numérico 
	public static boolean isNumeric(String str)
	{
	    try
	    {
	      double d = Double.parseDouble(str);
	    }
	    catch(NumberFormatException nfe)
	    {
	      return false;
	    }
	    return true;
	}
	
	
	
	//-------------------------------------------------------------------------------------------
	// METODO PARA PRUEBAS LOCALES
	//-------------------------------------------------------------------------------------------	
	
	public static void main(String[] args) throws Exception 
	{
		System.out.println("== INICIO PRUEBA LOCAL ==");
		
		//try (Connection con = PlataformaUtils.getDESAConnection())		

		try (Connection con = PlataformaUtils.getQAPEConnection())    // QA PE
		{
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();

			String json  = "{ "+
								"\"IdSolicitudBcp\":\"xa-12-z-2015\", " +
								"\"NumeroOperacionBcp\":11111, " +
								"\"NumeroOperacionExtornoBcp\":22222, " +
								"\"FechaOperacionBcp\":\"2017-07-26 05:06:00\", " +
								"\"TipoOperacionBCP\":\"PAGO\", " +
								"\"MensajeEntrada\":\"Prueba Mensaje...1111\", " +
								"\"OperacionesBcp\":[{" +
															"\"IdClienteBcp\":\"00000000000000000000006741238\", " +
															"\"IdMoneda\": \"PEN\"," +
															"\"Monto\":100001 " +
															 "}]}";
	
			RecaudacionBCP_Entrada Objin = gson.fromJson(json, RecaudacionBCP_Entrada.class);
			RecaudacionBCP demo = new RecaudacionBCP();
			
 			System.out.println("ENTRADA >" +  Objin.toString());
 			
 			try 
 			{	
 				RecaudacionBCP_Salida ObjOut = demo.logica(con, Objin);
 				System.out.println(ObjOut.toString());
 				////con.commit();
 			}
 			catch(Exception er) 
 			{
				//er.printStackTrace();	
 				////con.rollback();
			}
 			
 			
 			con.close();
 			
	    	}
		 	
		System.out.println("== FIN PRUEBA LOCAL ==");
	}

}