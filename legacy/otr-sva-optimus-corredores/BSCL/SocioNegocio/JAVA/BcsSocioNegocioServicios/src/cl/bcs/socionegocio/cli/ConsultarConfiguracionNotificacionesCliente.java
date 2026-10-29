package cl.bcs.socionegocio.cli;

import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.cli.ObtenerClientesPortafolio.ConsultarClientesPortafolio_entrada;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConsultarConfiguracionNotificacionesCliente implements JavaCall {
	
	// SELECT propio de la ventana
	private final String SQL_SELECT_CONFIG_NOTIFICACIONES = loadResourceToString("cl/bcs/socionegocio/cli/sql/ConsultarNotificacionesCliente_SelConfiguracion.sql");
	

	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = new Gson();
		ConsultarConfiguracionNotificacionesCliente_Entrada in = gson.fromJson(jin.toString(), ConsultarConfiguracionNotificacionesCliente_Entrada.class);
		ConfiguracionNotificacionCliente_Salida out = logica(conn, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;		
	}


	public static class ConsultarConfiguracionNotificacionesCliente_Entrada {
		//Parametros Fijos
		public Long		p_IdEmpresa;
		public Long		p_IdUsuario;
		public String	p_IdModulo;
		public Long 	p_IdEmpleadoConsulta;
		
		//Parametros de ventana
		public Long 	p_IdCliente;
		public String 	p_estadoNotificacion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	//  Estructura salida datos para Grilla
	public static class ConsultarConfiguracionNotificacionesCliente_Salida{
		
		public Long IdEmpresa;
		public String DocumentoIdentidad;
		public String Nombre;
		public String Entidad;
		public String DescripcionEntidad;
		public String Notificacion;
		public String EstadoNotificacion;
		public Timestamp FechaActivacion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	
	
	//Logica para obtencion de data
	public ConfiguracionNotificacionCliente_Salida logica(Connection conn, ConsultarConfiguracionNotificacionesCliente_Entrada entrada)throws Exception{
		SQL _sql = new SQL();
		int valorAux = 0;
		
		//Obtenemos parametros de entrada
		Long idCliente = entrada.p_IdCliente;
		String estadoNotificacion = entrada.p_estadoNotificacion;
		
		
		if (estadoNotificacion.equalsIgnoreCase("0")) {
			estadoNotificacion = "";
			valorAux = 1;
		}
	
		List<Long> listIdCliente = new ArrayList<Long>();
		HashMap<String,Object> params = null;
		
		//Si no se selecciona Cliente se obtienen todos los clientes segun sea el perfil del Empleado que consulta
		if (idCliente == 0) {
			
			//Obtenemos los clientes del portafolio
			ObtenerClientesPortafolio clientesPortafolio = new ObtenerClientesPortafolio();
			
			ConsultarClientesPortafolio_entrada entradaConsulta = new ConsultarClientesPortafolio_entrada();
			
			entradaConsulta.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaConsulta.p_IdUsuario = entrada.p_IdUsuario;
			entradaConsulta.p_IdModulo = entrada.p_IdModulo;
			entradaConsulta.p_IdEmpleadoConsulta = entrada.p_IdEmpleadoConsulta;
			
			listIdCliente = clientesPortafolio.getClientesPortafolio(conn, entradaConsulta);
			
		}else {
			listIdCliente.add(idCliente); 			
		}
		
		
		//Logica propia de la ventana
		//Obtenemos consulta Notificaciones Cliente
		ConfiguracionNotificacionCliente_Salida salida = new ConfiguracionNotificacionCliente_Salida();
		params = new HashMap<>();
		params.put("p_listaClientes", listIdCliente); 
		params.put("p_IdEmpresa", entrada.p_IdEmpresa);
		params.put("p_IdEstadoNotificacion", estadoNotificacion);
		params.put("p_ValorPaso", valorAux);
		
		
		List<ConsultarConfiguracionNotificacionesCliente_Salida> notificaciones = null;
		notificaciones = _sql.query(conn, SQL_SELECT_CONFIG_NOTIFICACIONES, params, ConsultarConfiguracionNotificacionesCliente_Salida.class);
		
		for(ConsultarConfiguracionNotificacionesCliente_Salida notif : notificaciones) {
			salida.P_OUT_LISTACONFNOTIFICACIONES.add(notif);
		}
		
		return salida;		
	}
	
	//Arreglo de salida para la grilla
	public static class ConfiguracionNotificacionCliente_Salida {
		public List<MensajeSalida>		P_OUT_MENSAJES;
		public Long						P_OUT_ERRORESCONTADOR;
		
		public List<ConsultarConfiguracionNotificacionesCliente_Salida> P_OUT_LISTACONFNOTIFICACIONES;
		
		public ConfiguracionNotificacionCliente_Salida() {
			P_OUT_MENSAJES = new ArrayList<>();
			P_OUT_LISTACONFNOTIFICACIONES = new ArrayList<>();
		}
		
		public String toString() {
			return Utils.printJson(this, true);
		}	
		
	}
	
	
	// //Fijo
	//Main para prueba
	public static void main(String[] args) throws Exception {
		System.out.println("== INICIO PRUEBA LOCAL ==");

		try (Connection con = PlataformaUtils.getDESAConnection())
		{
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			
			//Datos simulados, obtenidos desde ventana
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_IdModulo\":\"PLA\",\"p_IdEmpleadoConsulta\":7293,\"p_IdCliente\":0,\"p_IdPortafolio\":0,\"p_estadoNotif\":\"0\"}";
			
			ConsultarConfiguracionNotificacionesCliente_Entrada Objin = gson.fromJson(json, ConsultarConfiguracionNotificacionesCliente_Entrada.class);
			ConsultarConfiguracionNotificacionesCliente demo = new ConsultarConfiguracionNotificacionesCliente();
			System.out.println("ENTRADA:");
			System.out.println(Objin.toString());
			try 
			{
				ConfiguracionNotificacionCliente_Salida ObjOut= demo.logica(con, Objin);
				System.out.println("SALIDA:");
				ObjOut.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(ObjOut.P_OUT_MENSAJES);
				System.out.println(ObjOut.toString());
			} catch(Exception er) {
				er.printStackTrace();
				con.rollback();
			}
		}	
		System.out.println("== FIN PRUEBA LOCAL ==");
    }
	
	 
}
