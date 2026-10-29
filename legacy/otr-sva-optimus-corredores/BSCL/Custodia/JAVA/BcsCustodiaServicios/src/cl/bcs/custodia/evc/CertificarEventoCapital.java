package cl.bcs.custodia.evc;

/** 
 * Proposito: Pertite certificar(CRT) uno o varios eventos de capital cliente.
 *            Operativo para PREPAGO BONO (PERU)
 * Autor: JVILLAGRAN - BCS
 * Fecha Creacion: 02/09/2018
 **/ 

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CertificarEventoCapital implements JavaCall {
	
	private final Log log = new Log("CUS.CertificarEventoCapital");
	
	private final String SQL_SELECT_EVENTO_CAPITAL_CLIENTE = loadResourceToString("cl/bcs/custodia/sql/CertificarEventoCapital_SelectEventoCapitalCliente.sql");
	private final String SQL_UPDATE_EVENTO_CAPITAL_CLIENTE = loadResourceToString("cl/bcs/custodia/sql/CertificarEventoCapital_UpdateEventoCapitalCliente.sql");
	
	
	
    //-------------------------------------------------------------------------------------------
	// CLASES QUE REPRESENTAN OBJETOS DE ENTRADA
	
	public static class CertificarEventoCapital_Entrada
	{
		public Long 			p_IdEmpresa;
		public Long 			p_IdUsuario;
		public String 			p_IdModulo;
		public Long 			p_IdEventoCapital;
		public String 			p_TipoEventoCapital;  //Ej.: PB / SU
		public List<EventoCapitalCliente_Entrada> p_EventoCapitalCliente;  // EV de clientes a certificar  
		
		public CertificarEventoCapital_Entrada()
		{
			this.p_EventoCapitalCliente = new ArrayList<EventoCapitalCliente_Entrada>();
		}
	    
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class EventoCapitalCliente_Entrada
	{
		public Long p_IdEventoCapitalCliente;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
		
	
    //-------------------------------------------------------------------------------------------
	// CLASES QUE REPRESENTAN OBJETOS DE SALIDA	
	
	
    public static class CertificarEventoCapital_Salida
    {	
    	public List<MensajeSalida> 		P_OUT_MENSAJES;
    	public Long 					P_OUT_ERRORESCONTADOR;
    	   
    	public CertificarEventoCapital_Salida()
    	{
    		this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
    	}
    	
    	public String toString() {
    		return Utils.printJson(this, false);
    	}
    }
    
	
    //-------------------------------------------------------------------------------------------
	// CLASES UTILITARIAS DE APOYO	
	
	public static class EventoCapitalCliente_Proceso
	{
		public Long 	IdEventoCapital;
		public Long 	IdEventoCapitalCliente;
		public String 	IdTipoEventoCapital;  //Ej.: PB / SU
		public String 	IdEstado;
		public Long     IdEmpresa;
		
		//Para validaciones
		public long 	errores 		= 0;
		public long 	advertencias 	= 0;
		public long 	autorizaciones 	= 0;
		public long 	ok 				= 0;
	}

   

    //-------------------------------------------------------------------------------------------
	// METODOS UTILITARIOS	
	
	//Procesa salida de validaciones
	private void ProcesaRequisito (Connection con, Long idEmpresa, boolean esError, String codMsg, String CodRequisito, EventoCapitalCliente_Proceso ecc, List<MensajeSalida> P_OUT_MENSAJES)
			throws Exception {
		if (esError) 
		{
			String msg=Diccionario.getMensaje(con, idEmpresa, codMsg)					
					.add("IdEventoCapitalCliente", ecc.IdEventoCapitalCliente)
				    .msg();			
			 
			if (CodRequisito.equals(MensajeSalida.MENSAJE_ERROR)) 
			{
				P_OUT_MENSAJES.add(new MensajeSalida(ecc.IdEventoCapitalCliente,MensajeSalida.MENSAJE_ERROR,codMsg,msg));
				ecc.errores++;
			}
			else if (CodRequisito.equals(MensajeSalida.MENSAJE_AUTORIZACION)) 
			{
				P_OUT_MENSAJES.add(new MensajeSalida(ecc.IdEventoCapitalCliente,MensajeSalida.MENSAJE_AUTORIZACION,codMsg,msg));
				ecc.autorizaciones++;						
			}
			else if (CodRequisito.equals(MensajeSalida.MENSAJE_ADVERTENCIA)) 
			{
				P_OUT_MENSAJES.add(new MensajeSalida(ecc.IdEventoCapitalCliente,MensajeSalida.MENSAJE_ADVERTENCIA,codMsg,msg));
				ecc.advertencias++;
			}
			else 
			{
				ecc.ok++;
			}
		}
		else 
		{
			ecc.ok++;
		}		
	}
	
	

	//-------------------------------------------------------------------------------------------
	// LOGICA DEL SERVICIO
	
	public CertificarEventoCapital_Salida logica(Connection conn, CertificarEventoCapital_Entrada entrada) throws Exception 
	{
		CertificarEventoCapital_Salida salida = new CertificarEventoCapital_Salida();
		salida.P_OUT_ERRORESCONTADOR = 0L;
		
		SQL sql = new SQL();		
		log.write("== INICIO CERTIFICACION EVENTO CAPITAL ==");
		
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
			// OBTIENE DATOS EXTRAS DE EVENTO CAPITAL CLIENTE
			
			HashMap<String,Object> paramSelectEventoCapitalCliente = new HashMap<>();			
			List<Long> _idEventoCapitalCliente = new ArrayList<>();
			
			for (EventoCapitalCliente_Entrada _ecce : entrada.p_EventoCapitalCliente)
			{
				_idEventoCapitalCliente.add(_ecce.p_IdEventoCapitalCliente);
			}
			
			paramSelectEventoCapitalCliente.put("IdEventoCapitalCliente", _idEventoCapitalCliente);
			List<EventoCapitalCliente_Proceso> _eventoCapitalCliente_Proceso = sql.query(conn, SQL_SELECT_EVENTO_CAPITAL_CLIENTE, paramSelectEventoCapitalCliente, EventoCapitalCliente_Proceso.class);    //Obtiene datos extras de 
			
			

			//---------------------------------------------------------------------------------------------			
			// VALIDACIONES
			log.write("VALIDACION DE EVENTOS DE CAPITAL A CERTIFICAR");
			for (EventoCapitalCliente_Proceso ecc: _eventoCapitalCliente_Proceso)	
			{
				ProcesaRequisito(conn, entrada.p_IdEmpresa, ecc.IdEstado.equalsIgnoreCase("CRT"),"EVENTO_CAPITAL_SE_ENCUENTRA_CERTIFICADO", MensajeSalida.MENSAJE_ERROR, ecc, salida.P_OUT_MENSAJES);
				ProcesaRequisito(conn, entrada.p_IdEmpresa, (!ecc.IdTipoEventoCapital.equalsIgnoreCase("PB") && !ecc.IdTipoEventoCapital.equalsIgnoreCase("SU")),"TIPO_EVENTO_CAPITAL_NO_SE_CERTIFICA", MensajeSalida.MENSAJE_ERROR, ecc, salida.P_OUT_MENSAJES);
				ecc.IdEstado = "CRT";     // Asigna estado CRT (certificado) para posterior actualizacion
			}
				
			
			if (MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES) > 0) 
			{
				log.write("CERTIFICACION DE EVENTO CAPITAL CON ERRORES DE VALIDACION");
				
				for (MensajeSalida mensaje : salida.P_OUT_MENSAJES)
				{
					log.write("ERROR VALIDACION > " + mensaje.descripcion);
				}
				
				return salida;
			}
			
			
			if (_eventoCapitalCliente_Proceso.size() > 0)
			{
				log.write("ACTUALIZACION DE EVENTO DE CAPITAL A CERTIFICADO");
				sql.batch(conn, SQL_UPDATE_EVENTO_CAPITAL_CLIENTE, _eventoCapitalCliente_Proceso , EventoCapitalCliente_Proceso.class);
			}
		
		}
		catch(Exception e)
		{	
			log.write("ERROR > ", e);
		}
		
		log.write("SALIDA SERVICIO : " + salida.toString());
		log.write("== TERMINO CERTIFICACION EVENTO CAPITAL ==");
		
		return salida;
	}


	//-------------------------------------------------------------------------------------------
	// OBJETO DE TRANSPORTE
	
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = new Gson();
		//Entrada in = gson.fromJson(jin.toString(), Entrada.class);
		CertificarEventoCapital_Entrada in = gson.fromJson(jin.toString(), CertificarEventoCapital_Entrada.class);
		
		CertificarEventoCapital_Salida out = logica(conn,in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		
		
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();	
		return dataout;
	}
	
	
	//-------------------------------------------------------------------------------------------
	// METODO PARA PRUEBAS LOCALES
	
	public static void main(String[] args) throws Exception {
		System.out.println("== INICIO PRUEBA LOCAL ==");

		try (Connection con = PlataformaUtils.getQAPEConnection())
		{
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":69,\"p_IdModulo\":\"CUS\",\"p_IdEventoCapital\":97,\"p_TipoEventoCapital\":\"PB\",\"p_EventoCapitalCliente\": [{\"p_IdEventoCapitalCliente\":5486},{\"p_IdEventoCapitalCliente\":5590}]}";
					
			CertificarEventoCapital_Entrada Objin = gson.fromJson(json, CertificarEventoCapital_Entrada.class);
			
			CertificarEventoCapital demo = new CertificarEventoCapital();		
			
			System.out.println("ENTRADA:");
			System.out.println(Objin.toString());
			
			
			try 
			{
				CertificarEventoCapital_Salida ObjOut= demo.logica(con, Objin);
				con.commit();
				
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
