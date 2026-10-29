package cl.bcs.tesoreria;

/**
 * Proposito: Consulta que valida que el "Id del Cliente"(depositante) enviado por banco BCP exista y esté habilitado en optimus.
 *            Si cliente existe retorna sus datos. si no existe, retorna código de error BCP.  
 * Autor: JVILLAGRAN - BCS
 * Fecha Creacion: 18/09/2017
 */ 

import java.sql.Connection;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;


import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConsultaBCP  implements JavaCall {

	// Log
	private final Log log = new Log("TES.ConsultaBCP");
	
	
	// Queries
	private final String SQL_INSERT_LogMensajeTransaccion = loadResourceToString("cl/bcs/tesoreria/sql/RecaudacionBCP_InsertLogMensajeTransaccion.sql"); 	
	private final String SQL_SELECT_LogMensajeTransaccion = loadResourceToString("cl/bcs/tesoreria/sql/RecaudacionBCP_SelectLogMensajeTransaccion.sql");
	private final String SQL_SELECT_Cliente               = loadResourceToString("cl/bcs/tesoreria/sql/RecaudacionBCP_SelectCliente.sql");
	private final String SQL_UPDATE_LogMensajeTransaccion = loadResourceToString("cl/bcs/tesoreria/sql/RecaudacionBCP_UpdateLogMensajeTransaccion.sql");
		
	
	// Constantes	
	private final Long idEmpresa = 1L;
	
	
	
    //-------------------------------------------------------------------------------------------
	// CLASES QUE REPRESENTAN OBJETOS IN
	//-------------------------------------------------------------------------------------------
	
	public static class ConsultaBCP_Entrada 
	{	
		public String               	IdSolicitudBcp;	// MFX_SvcRecaudaInq/SvcRecaudaInqRq/RqUID
		public String 	   				MensajeEntrada;	// Mensaje completo enviado por BCP(parseado a json)			
		public List<Consulta_Entrada>  	ConsultasBcp;   // MFX_SvcRecaudaInq/SvcRecaudaInqRq/SvcRecaudaInqRef/SvcEmpLst/SvcEmp 
		
		public ConsultaBCP_Entrada()
		{
			ConsultasBcp =  new ArrayList<Consulta_Entrada>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class Consulta_Entrada
	{	
		public 		String	IdServicioBcp;	 // MFX_SvcRecaudaInq/SvcRecaudaInqRq/SvcRecaudaInqRef/SvcEmpLst/SvcEmp/SvcId
		public 		String	IdClienteBcp;  	 // MFX_SvcRecaudaInq/SvcRecaudaInqRq/SvcRecaudaInqRef/SvcEmpLst/SvcEmp/PagId   // Documento identidad del depositante BCP

		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
    //-------------------------------------------------------------------------------------------
	// CLASES QUE REPRESENTAN OBJETOS OUT	
	//-------------------------------------------------------------------------------------------
	
	public static class ConsultaBCP_Salida
	{	
		public List<MensajeSalida>		  P_OUT_MENSAJES;
		public Long						  P_OUT_ERRORESCONTADOR;
		
		public String 					  P_OUT_IDENTIFICADORSOLICITUD;  	// MFX_SvcRecaudaInq/SvcRecaudaInqRs/RqUID
		public java.sql.Timestamp  		  P_OUT_FECHAPROCESO;				// MFX_SvcRecaudaInq/SvcRecaudaInqRs/MsjRsHdr/CtxRsHdr/ServFec
		public String					  P_OUT_NUMEROOPERACIONOPTIMUS;		// MFX_SvcRecaudaInq/SvcRecaudaInqRs/MsjRsHdr/CtxRsHdr/OpnNroAdic
		public List<ResultadoConsulta_Salida> P_OUT_RESULTADOCONSULTA; 		// MFX_SvcRecaudaInq/SvcRecaudaInqRs/SvcRecaudaInqReg/SvcEmpLst/SvcEmp
		
		public ConsultaBCP_Salida()
		{	
			this.P_OUT_RESULTADOCONSULTA 	= new ArrayList<ResultadoConsulta_Salida>();
			this.P_OUT_MENSAJES 	 		= new ArrayList<MensajeSalida>();
		}

        
		public String toString() {
			return Utils.printJson(this, false);
		}
	}	


	public static class ResultadoConsulta_Salida
	{	
		public 		String	IdServicioBcp;					// MFX_SvcRecaudaInq/SvcRecaudaInqRs/SvcRecaudaInqReg/SvcEmpLst/SvcEmp/SvcId
		public 		String	IdClienteBcp;  					// MFX_SvcRecaudaInq/SvcRecaudaInqRs/SvcRecaudaInqReg/SvcEmpLst/SvcEmp/PagId
		public 		String	NombreCliente;					// MFX_SvcRecaudaInq/SvcRecaudaInqRs/SvcRecaudaInqReg/SvcEmpLst/SvcEmp/NomCompleto
		public 		String	TipoDocumentoIdentidad;			// MFX_SvcRecaudaInq/SvcRecaudaInqRs/SvcRecaudaInqReg/SvcEmpLst/SvcEmp/DocTp
		public 		String	DocumentoIdentidad;				// MFX_SvcRecaudaInq/SvcRecaudaInqRs/SvcRecaudaInqReg/SvcEmpLst/SvcEmp/DocNro
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}	
	
	//-------------------------------------------------------------------------------------------
	// CLASES UTILITARIAS QUE REPRESENTAN OBJETOS DE PROCESAMIENTO
	//-------------------------------------------------------------------------------------------

	public static class Consulta_Proceso 
	{	 
		public      String  LlaveCliente;  //Llave de Cliente(IdEmpresa + IdTipoDocumentoIdentidad + DocumentoIdentidad)
		public      Long    IdMensajeTransaccion; //Id del LOG registrado con del mensaje de entrada de la consulta
		public 		Long 	IdPersona;
		public 		Long 	IdCliente;
		public 		String	IdServicioBcp;
		public 		String	IdClienteBcp;
		public 		String	NombreCliente;
		public 		String	IdTipoDocumentoIdentidad;
		public 		String	DocumentoIdentidad;
		public 		String	IdEstadoPersona;		
		public 		Long	IdPortafolioDefecto;
		public 		String	IdEstadoCliente;
		
		//Para validaciones
		public 		long 	errores 			= 0;
		public 		long 	ok 					= 0;
		public 		long 	autorizaciones		= 0;
		public 		long  	advertencias		= 0;
		
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	
	// Se utiliza para el registro del LOG de mensaje enviado por BCP para la consulta y la respuesta de Optimus	 
	public static class MensajeTransaccionBCP	
	{	
		public Long         		IdMensajeTransaccion;
		public String				RqUID;
		public String				TipoTrasaccion;
		public String				MensajeEntrada;   // XML enviado desde BCP a Optimus   (en formato json) 
		public String				MensajeSalida;    // XML de respuesta de Optimus a BCP (en formato json)
		public java.sql.Timestamp	FechaHoraRecepcion; 
		public java.sql.Date		FechaSistema;
		public Long         		IdEmpresa;
		
		public String toString(){
			return Utils.printJson(this, false);
		}			
	}	


	public static class Cliente	
	{	 
		public String			LlaveCliente;    //Llave del registro: IdEmpresa + TipoDocumentoIdentidad + DocumentoIdentidad (Ej.: '1DNI12969673', '1RUC20101031340')
		public Long         	IdPersona;
		public Long				IdCliente;
		public String			NombreCompleto;
		public String			IdEstadoPersona;
		public String			IdEstadoCliente;
		public String           IdTipoDocumentoIdentidad;
		public String			DocumentoIdentidad;
		public Long   			IdPortafolioDefecto;
		public String			IdTipoSocioDeNegocio; 
		
		public String toString(){
			return Utils.printJson(this, false);
		}			
	}	

	
	 
	// Enum para definir "Código Documento Identidad BCP" según tipo de documento(se usa para retorno)
	public enum TipoDocumentoIdentidadBCP
	{
		DNI("1","DNI"),
		CI("2","Carnet de Identidad"),
		CE("3","Carnet de Extranjería"),
		PAS("4", "Pasaporte"),
		LT("5", "Libreta Tributaria"),
		RUC("6","RUC");
		
		
		private String  Codigo;
		private String  Descripcion;
		
		public String getCodigo()
		{
			return Codigo;
		}
	
		public String getDescripcion()
		{
			return Descripcion;
		}
		
		private TipoDocumentoIdentidadBCP(String codigo, String descripcion)
		{
			this.Codigo = codigo;
			this.Descripcion = descripcion;
		}
	}	
	

	//-------------------------------------------------------------------------------------------
	// METODOS UTILITARIOS PARA PROCESAMIENTO EN LOGICA DEL SERVICIO
	//-------------------------------------------------------------------------------------------	
	
	private void ProcesaRequisitoConsultaBCP(Connection con, Long idEmpresa, boolean esError, String codMsg, String CodRequisito, Consulta_Proceso proceso, List<MensajeSalida> P_OUT_MENSAJES,String CodErrorBcp)
			throws Exception {
		if (esError) 
		{
			String msg=Diccionario.getMensaje(con, idEmpresa, codMsg)
					.add("IdCliente", proceso.IdClienteBcp)
					.add("IdPersona", proceso.IdClienteBcp)
					.add("TipoDocumentoIdentidad", proceso.IdClienteBcp)					
					.add("NombrePersona", proceso.NombreCliente)
				    .msg();
			 
			if (CodRequisito.equals(MensajeSalida.MENSAJE_ERROR)) 
			{	
				P_OUT_MENSAJES.add(new MensajeSalida(proceso.IdMensajeTransaccion,MensajeSalida.MENSAJE_ERROR,CodErrorBcp,msg)); // retorna IdMensajeTransaccion ya que no hay otro registro por ser sólo consulta
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
	
	
	
	//-------------------------------------------------------------------------------------------
	// OBJETO DE TRANSPORTE
	//-------------------------------------------------------------------------------------------
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		
		ConsultaBCP_Entrada in = gson.fromJson(jin.toString(), ConsultaBCP_Entrada.class);
		ConsultaBCP_Salida out = logica(con, in);
		
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	
	//-------------------------------------------------------------------------------------------
	// LOGICA DEL SERVICIO
	//-------------------------------------------------------------------------------------------
	
	public ConsultaBCP_Salida logica(Connection con, ConsultaBCP_Entrada in) throws Exception {
		SQL _sql = new SQL();
		ConsultaBCP_Salida out = new ConsultaBCP_Salida();
		
		log.write("== INICIO CONSULTA BCP ==");
		try
		{
			log.write("PARAMETROS DE ENTRADA : " + in.toString());
			
			String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "PLATAFORMA", "PLA.FechaSistema");
			
				
			
			//---------------------------------------------------------------------------------------------
			// GUARDA LOG CON MENSAJE DE ENTRADA (Enviado desde BCP a Optimus para realizar la consulta)		
			
			List<MensajeTransaccionBCP> MensajeTransaccionBCP = new ArrayList<MensajeTransaccionBCP>();
			SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd");
			
			MensajeTransaccionBCP _mensajeTransaccionBCP = new MensajeTransaccionBCP();			
			_mensajeTransaccionBCP.RqUID = in.IdSolicitudBcp;
			_mensajeTransaccionBCP.TipoTrasaccion = "CONSULTA";   // CONSULTA | PAGO | EXTORNO
			_mensajeTransaccionBCP.MensajeEntrada = in.MensajeEntrada; // Mensaje enviado por BCP para consulta (en formato json)
			_mensajeTransaccionBCP.FechaSistema = new Date(formato.parse(fechaSistema).getTime());
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
				parametrosConsultaLMT.put("TipoTrasaccion", "CONSULTA");
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
			
				out.P_OUT_NUMEROOPERACIONOPTIMUS = (mensajeTransaccionBCP.get(in.IdSolicitudBcp).IdMensajeTransaccion).toString();  //Se retorna el Id del LOG registrado (BSCL.Tesoreria.BD::LOG.MensajeTransaccionBCP)
				log.write("CONSULTA REGISTRO DE MENSAJE DE ENTRADA OK > " + mensajeTransaccionBCP.get(in.IdSolicitudBcp).RqUID);				
			}
			

			//--------------------------------------------------------------------------------------------- 
			// ELIMINA LOS CEROS DE LA IZQUIERDE DEL IdClienteBcp (Id depositante Bcp) -->MFX_SvcRecaudaInq/SvcRecaudaInqRq/SvcRecaudaInqRef/SvcEmpLst/SvcEmp/PagId
			// Ej.: 0000012969 >> 12969
			for (Consulta_Entrada consultaEntrada : in.ConsultasBcp)
			{
				if (isNumeric(consultaEntrada.IdClienteBcp))
				{
					Long lngIdClienteBcp = Long.parseLong(consultaEntrada.IdClienteBcp);
					String strIdClienteBcp = lngIdClienteBcp.toString();
					consultaEntrada.IdClienteBcp = strIdClienteBcp;					
				}
			}
			log.write("ELIMINA CEROS DE LA IZQUIERDA DEL IDENTIFICADOR CLIENTE BCP (IdClienteBcp)");
			
			
			
			//---------------------------------------------------------------------------------------------
			//OBTIENE DATOS DEL CLIENTE
			
			HashMap<String, Cliente> datosCliente = new HashMap<>();
			{	
				HashMap<String,Object> paramConsultaCliente = new HashMap<>();
				
				String llaveCliente = "";
				List<String> llavesClientes  = new ArrayList<String>();
				
				for (Consulta_Entrada consultaEntrada : in.ConsultasBcp)
				{	
					//Arma llave para cliente. El criterio de consulta es una llave compuesta: IdEmpresa + TipoDocumentoIdentidad + DocumentoIdentidad (Ej.: '1DNI12969673', '1RUC20101031340')
					if (consultaEntrada.IdClienteBcp.length() == 8)    //Largo 8 --> Persona Natural (usa DNI)						
					    llaveCliente = idEmpresa.toString() + "DNI" + consultaEntrada.IdClienteBcp;
					else if (consultaEntrada.IdClienteBcp.length() == 11)    //Largo 11 --> Persona Jurídica (usa RUC)
						llaveCliente = idEmpresa.toString() + "RUC" + consultaEntrada.IdClienteBcp;
				
					llavesClientes.add(llaveCliente);
				}

				paramConsultaCliente.put("llavesClientes", llavesClientes);
				List<Cliente> resultConsultaCliente = _sql.query(con, SQL_SELECT_Cliente, paramConsultaCliente, Cliente.class);
				
				 
				HashMap<Object, Object>__cliente = Utils.List2Hash(resultConsultaCliente, "LlaveCliente");  //Transforma la lista resultConsultaCliente en Hash
				for(Object key: __cliente.keySet())
				{
					datosCliente.put(key.toString(), (Cliente) __cliente.get(key));
				}
				
				log.write("OBTIENE DATOS DEL CLIENTE > " + resultConsultaCliente);
			}
			
			
			
			//---------------------------------------------------------------------------------------------
			// INCORPORAR DATOS DE ENTRADA A DATOS PROCESO
			
			List<Consulta_Proceso> ConsultaProceso  = new ArrayList<Consulta_Proceso>();
			
			
			for (Consulta_Entrada consultaEntrada : in.ConsultasBcp)
			{	
				//Arma llave de Cliente(IdEmpresa + IdTipoDocumentoIdentidad + DocumentoIdentidad) para buscar tupla
				String llaveCliente = ""; 
				if (consultaEntrada.IdClienteBcp.length() == 8)   //Largo 8 --> Persona Natural (usa DNI)
					llaveCliente = idEmpresa.toString() + "DNI" + consultaEntrada.IdClienteBcp;
				else if (consultaEntrada.IdClienteBcp.length() == 11)  // Persona Jurídica (usa RUC)
					llaveCliente = idEmpresa.toString() + "RUC" + consultaEntrada.IdClienteBcp;
				else
					llaveCliente = ""; //Si la llave es vacía saltará en las validaciones 
					
				Cliente infoCliente = (Cliente) datosCliente.get(llaveCliente); //Busca y asigna cliente
				
				Consulta_Proceso _consultaProceso = new Consulta_Proceso();
				Utils.copyPojo(consultaEntrada, _consultaProceso);

				_consultaProceso.LlaveCliente = llaveCliente;				
				_consultaProceso.IdPersona = (infoCliente == null) ? 0 : infoCliente.IdPersona;				
				_consultaProceso.IdCliente = (infoCliente == null) ? 0 : infoCliente.IdCliente;
				_consultaProceso.IdEstadoPersona = (infoCliente == null) ? "" : infoCliente.IdEstadoPersona;
				_consultaProceso.IdEstadoCliente = (infoCliente == null) ? "" : infoCliente.IdEstadoCliente;				
				_consultaProceso.NombreCliente = (infoCliente == null) ? "" : infoCliente.NombreCompleto;
				_consultaProceso.IdTipoDocumentoIdentidad = (infoCliente == null) ? "" : infoCliente.IdTipoDocumentoIdentidad;
				_consultaProceso.DocumentoIdentidad = (infoCliente == null) ? "" : infoCliente.DocumentoIdentidad;
				_consultaProceso.IdPortafolioDefecto = (infoCliente == null) ? 0 : infoCliente.IdPortafolioDefecto;
				_consultaProceso.IdMensajeTransaccion = mensajeTransaccionBCP.get(in.IdSolicitudBcp).IdMensajeTransaccion; 
								
				ConsultaProceso.add(_consultaProceso);
			}
			log.write("PROCESO DE DATOS DE ENTRADA: " + ConsultaProceso);

			
			
			//---------------------------------------------------------------------------------------------
			//VALIDACIONES DE DATOS 
			{	
				for (Consulta_Proceso _consultaProceso : ConsultaProceso)
				{	
					ProcesaRequisitoConsultaBCP(con, idEmpresa, (_consultaProceso.LlaveCliente == null || _consultaProceso.LlaveCliente == ""),"TIPO_DOCUMENTO_IDENTIDAD_DESCONOCIDO",MensajeSalida.MENSAJE_ERROR, _consultaProceso , out.P_OUT_MENSAJES, "CP0010"); // CP0010 >> Id depositante No existe					
					ProcesaRequisitoConsultaBCP(con, idEmpresa, (_consultaProceso.IdPersona == null || _consultaProceso.IdPersona == 0),"PERSONA_NO_EXISTE",MensajeSalida.MENSAJE_ERROR, _consultaProceso , out.P_OUT_MENSAJES,"CP0010");                          // CP0010 >> Id depositante No existe					
					ProcesaRequisitoConsultaBCP(con, idEmpresa, ((_consultaProceso.IdCliente == null || _consultaProceso.IdCliente == 0) && _consultaProceso.IdPersona != 0),"CLIENTE_NO_EXISTE",MensajeSalida.MENSAJE_ERROR, _consultaProceso , out.P_OUT_MENSAJES,"CP0141");  // CP0141 >> Id depositante Restringido para consulta
					ProcesaRequisitoConsultaBCP(con, idEmpresa, (!_consultaProceso.IdEstadoPersona.equalsIgnoreCase("ACT") && _consultaProceso.IdPersona != 0),"PERSONA_NO_ACTIVA",MensajeSalida.MENSAJE_ERROR, _consultaProceso , out.P_OUT_MENSAJES,"CP0141");   // CP0141 >> Id depositante Restringido para consulta					
					ProcesaRequisitoConsultaBCP(con, idEmpresa, (!_consultaProceso.IdEstadoCliente.equalsIgnoreCase("ACT") && _consultaProceso.IdPersona != 0  && _consultaProceso.IdCliente != 0),"CLIENTE_NO_ACTIVO",MensajeSalida.MENSAJE_ERROR, _consultaProceso , out.P_OUT_MENSAJES, "CP0141");  // CP0141 >> Id depositante Restringido para consulta					
					ProcesaRequisitoConsultaBCP(con, idEmpresa, (_consultaProceso.IdPortafolioDefecto == 0 && _consultaProceso.IdPersona != 0 && _consultaProceso.IdCliente != 0),"PORTAFOLIO_DEFECTO_NO_EXISTE",MensajeSalida.MENSAJE_ERROR, _consultaProceso , out.P_OUT_MENSAJES, "CP0141"); // CP0141 >> Id depositante Restringido para consulta
				}
				log.write("VALIDACION DE DATOS REALIZADA");
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
			
			
			
			//---------------------------------------------------------------------------------------------
			// PREPARA SALIDA DEL SERVICIO
			
			out.P_OUT_IDENTIFICADORSOLICITUD = in.IdSolicitudBcp; 		// MFX_SvcRecaudaInq/SvcRecaudaInqRq/RqUID   //Valor que se retornan tal cual se recibió de BCP
			out.P_OUT_FECHAPROCESO = mensajeTransaccionBCP.get(in.IdSolicitudBcp).FechaHoraRecepcion;
			
			
			List<ResultadoConsulta_Salida> ResultadoConsulta = new ArrayList<ResultadoConsulta_Salida>(); 
			for (Consulta_Proceso consultaoProceso : ConsultaProceso)
			{
				ResultadoConsulta_Salida _resultadoConsulta = new ResultadoConsulta_Salida();
				
				_resultadoConsulta.IdServicioBcp = consultaoProceso.IdServicioBcp;  //Este dato se retorna tal cual se recibió de BCP
				_resultadoConsulta.IdClienteBcp = consultaoProceso.IdClienteBcp;    //Este dato se retorna tal cual se recibió de BCP
				_resultadoConsulta.NombreCliente = consultaoProceso.NombreCliente;
				_resultadoConsulta.DocumentoIdentidad = consultaoProceso.DocumentoIdentidad;
				
				switch (consultaoProceso.IdTipoDocumentoIdentidad)
				{
					case "DNI" :
						_resultadoConsulta.TipoDocumentoIdentidad = TipoDocumentoIdentidadBCP.DNI.getCodigo();
						break;
					case "RUC" :
						_resultadoConsulta.TipoDocumentoIdentidad = TipoDocumentoIdentidadBCP.RUC.getCodigo();
				};
				
				ResultadoConsulta.add(_resultadoConsulta);
			}
			
			out.P_OUT_RESULTADOCONSULTA = ResultadoConsulta;
			out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);  //No siempre pasará por "public JsonObject call" por lo que se calcula acá para que no retorne null
			log.write("SALIDA DEL SERVICIO: " + out);			
		}
		catch(Exception e)
		{
				log.write(e);
				throw e;
		}
		finally 
		{

		}
	
		log.write("== TERMINÓ CONSULTA BCP ==");
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
		   
		try (Connection con = PlataformaUtils.getQAPEConnection())   // QA PE
		{
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			
			String json  = "{" +
					"\"IdSolicitudBcp\":\"54d461s7-74rd-426s-wa54-14a4c894gd01\"," +
					"\"MensajeEntrada\":\"Mensaje de Prueba..1.0\"," +
					"\"ConsultasBcp\":[{" +
							"\"IdServicioBcp\":\"1002\"," + 
							"\"IdClienteBcp\":\"0000012969673\" }]" +
					"}";
			
			ConsultaBCP_Entrada Objin = gson.fromJson(json, ConsultaBCP_Entrada.class);
			ConsultaBCP demo = new ConsultaBCP();
			
 			System.out.println("ENTRADA:");
 			System.out.println(Objin.toString());
 			
 			try 
 			{	
 				ConsultaBCP_Salida ObjOut = demo.logica(con, Objin);
 				System.out.println(ObjOut.toString());
 				con.commit();
 			}
 			catch(Exception er) 
 			{
				//er.printStackTrace();	
				con.rollback();
			}
 			
 			con.close();
 			
	    	}
		 	
		System.out.println("== FIN PRUEBA LOCAL ==");
	}	
}

