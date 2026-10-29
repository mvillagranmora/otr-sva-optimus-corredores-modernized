package cl.bcs.plataforma.acc;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.common.base.Charsets;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.acc.CrearAutorizadoOrden.CrearAutorizadoOrden_Entrada;
import cl.bcs.plataforma.acc.CrearAutorizadoOrden.CrearAutorizadoOrden_Salida;
import cl.bcs.plataforma.acc.utils.ClaveProvisional;
import cl.bcs.plataforma.evt.AgregarEvento;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;


public class CrearClaveICliente implements JavaCall {
	
    //-------------------------------------------------
	// Llamados SQL
	//-------------------------------------------------

	private final String SQL_CONSULTA_NUEVO_CLIENTE                 = loadResourceToString("cl/bcs/plataforma/acc/sql/ConsultaNuevoCliente.sql");
	//private final String SQL_CREAR_USUARIO		                 	=loadResourceToString("cl/bcs/plataforma/acc/sql/CrearUsuario.sql");
	private final String SQL_INSERT_CLAVE                           = loadResourceToString("cl/bcs/plataforma/acc/sql/InsertarClaveProvisoria.sql");
	private final String SQL_SELECT_CONSULTAR_CLIENTE_PORTAFOLIO 	= loadResourceToString("cl/bcs/plataforma/acc/sql/CrearClaveICliente_SelectConsultaClientePortafolio.sql");

    //-------------------------------------------------
	// CLASES DE ENTRADA 
	//-------------------------------------------------
	public static class CrearClaveICliente_Entrada
	{
		public 	Long	p_IdEmpresa;
		public	Long	p_IdUsuario;
		public 	String	p_IdModulo;
		public  String	p_CodigoUsuario; 
		public  Long 	p_IdCliente;
		public 	String 	p_IdTipoAutorizado;
		public 	String 	p_FirmaOptimusCliente;

		@Override
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	}
	
    //-------------------------------------------------------------------------------------------
	// CLASES UTILITARIAS QUE REPRESENTAN OBJETOS OUT	
	//-------------------------------------------------------------------------------------------
	
	public static class CrearClaveICliente_Salida{

		public List<MensajeSalida>					P_OUT_MENSAJES;
		public Long									P_OUT_ERRORESCONTADOR;
		public String 				     			P_OUT_RESULTADOPROCESO;
		
		public CrearClaveICliente_Salida()
		{	
			this.P_OUT_MENSAJES 			 = new ArrayList<MensajeSalida>();
			this.P_OUT_ERRORESCONTADOR 		 = 0L;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class ConsultaNuevoCliente_Clase {   //Doc
		
		public String IdSecuencia="0";
		
		public Long   IDPERSONA;
		public String CodigoUsuario;
		public Long   IdDireccionElectronicaPrincipal;
		public String Nombres;
		public String ApellidoPaterno;
		public String ApellidoMaterno;
		public String CORREDORA;
		public String TipoDocumentoIdentidad;
		public String DocumentoIdentidad;
		public String Numero;
		public Long   IdEmpresa;
		public Long   IdUsuario;
		public String TipoEntidad;
		public String Direccion;
		
		public String IdPortafolio="1";
		public String IdPersonaEmpleado="1";

		@Override
		public String toString()
		{
			return Utils.printJson(this, false);
		}
		
	}
	
	public static class UpdateClave
	{
		public String p_ClaveProvisoria;
		public String   p_IdUsuario;
		
		public String toString()
		{
			return Utils.printJson(this, false);
		}	
	}
	
	public static class ConsultaClientePortafolio extends BcsPojo {
		
		public Long IdPortafolio;
		public Long IdPersona;
		
	}
	
	//-------------------------------------------------
	// LOGICA DEL SERVICIO 
	//-------------------------------------------------
	public CrearClaveICliente_Salida logica(Connection conn, CrearClaveICliente_Entrada in) throws Exception
	{
		CrearClaveICliente_Salida salida = new CrearClaveICliente_Salida();
	    String encontrados="FAIL";
		SQL sql = new SQL();
		List<ConsultaNuevoCliente_Clase> resultado= new ArrayList<ConsultaNuevoCliente_Clase>();
		
		HashMap<String,Object> parametroEntrada = new HashMap<String,Object>();
		parametroEntrada.put("p_IdCliente", in.p_IdCliente);
		List<ConsultaNuevoCliente_Clase> SelectIdPersona = sql.query(conn, SQL_CONSULTA_NUEVO_CLIENTE, parametroEntrada, ConsultaNuevoCliente_Clase.class);
		
		if(SelectIdPersona.get(0).IdDireccionElectronicaPrincipal == null) {
			MensajeSalida msj = new MensajeSalida();
			msj.codigo = "ERROR";
			msj.descripcion = "No tiene EMAIL asociado";
			msj.id = 0L;
			msj.tipo = "ERROR";
			salida.P_OUT_ERRORESCONTADOR = 1L;
			salida.P_OUT_MENSAJES.add(msj);
			
			return salida;		
		}
		
		for(ConsultaNuevoCliente_Clase item2 : SelectIdPersona)
		{
				try {
	
					AgregarEvento.ServiceEntrada notifIn = new AgregarEvento.ServiceEntrada();
			        notifIn.P_IDEMPRESA = in.p_IdEmpresa;
			        notifIn.P_IDUSUARIO = in.p_IdUsuario;			        
			        notifIn.P_IDMODULO = in.p_IdModulo;
			        notifIn.P_EVENTOS = new ArrayList<>();
			        notifIn.P_VALORES = new ArrayList<>();
			        notifIn.P_ARCHIVOSADJUNTOS = new ArrayList<>();
			        notifIn.P_IDDIRECCIONES = new ArrayList<>();
			        
			        ConsultaNuevoCliente_Clase email = new ConsultaNuevoCliente_Clase();
			        String fechaSistema = Utils.getFechaSistema(conn, Utils.getSchema(), 1);
			        //String horaSistema = FechaDB.ObtenerHoraSistemaLocal(conn, item2.IdEmpresa);

			        //Agregar Evento
					AgregarEvento.ObjEVENTOS evt = new AgregarEvento.ObjEVENTOS();
		            evt.CodigoTipoEvento = "SOLICITUD_CAMBIO_CLAVE";
		            evt.IdSecuencia = Long.parseLong(email.IdSecuencia);         /// no tiene  es IdCartera
		            evt.FechaCreacionSistema =  fechaSistema;            
		            evt.IdPersonaRemitente =  Long.parseLong("0");
		            evt.IdPersonaDestinatario = item2.IDPERSONA;            
		            evt.IdPortafolio = Long.parseLong(email.IdPortafolio);
		            notifIn.P_EVENTOS.add(evt);
		            
		            //Agregar direccion cliente
		            AgregarEvento.ObjIDDIRECCIONES ndir = new AgregarEvento.ObjIDDIRECCIONES();
		            ndir.IdSecuencia = Long.parseLong(email.IdSecuencia);
		            ndir.TipoDestinatario = "TO";
		            //ndir.IdDireccion = new Long(0);
		            ndir.IdDireccion = new Long(1943693); // IdDireccionElectronicaEmpleado
		            notifIn.P_IDDIRECCIONES.add(ndir);
		            
		            //Agregar notificacion al empleado
		            /*ndir = new AgregarEvento.ObjIDDIRECCIONES();
		            ndir.IdSecuencia =Long.parseLong(email.IdSecuencia);
		            ndir.TipoDestinatario = "TO";
		            //ndir.IdDireccion = new Long(item2.IdDireccionElectronicaPrincipal); // IdDireccionElectronicaEmpleado
		            ndir.IdDireccion = new Long(1943959); // IdDireccionElectronicaEmpleado
		            notifIn.P_IDDIRECCIONES.add(ndir);*/
		            //Agregar Detalle evento
		            	            
		            ClaveProvisional ClavePP = new ClaveProvisional();
					String ClaveProvisoria = ClavePP.logica().Clave.toString();
					//String ClaveProvisoria = "1";
					       ClaveProvisoria = ClaveProvisoria.toUpperCase();
					
					HashFunction hf = Hashing.sha256();
                    com.google.common.hash.HashCode hc = hf.newHasher().putString(ClaveProvisoria, Charsets.UTF_8).hash();
                    //com.google.common.hash.HashCode hc2 = hf.newHasher().putString(hc.toString(), Charsets.UTF_8).hash();
                    String hcClaveNueva = hc.toString()/*.toUpperCase()*/;
                    
                    System.out.println("--------------------" +ClaveProvisoria +"---------------");
                    System.out.println("--------------------" +hcClaveNueva +"---------------");
					
		            notifIn.addValor(Long.parseLong(email.IdSecuencia), "Nombre", item2.Nombres)
		            				.addValor(Long.parseLong(email.IdSecuencia), "Corredora", item2.CORREDORA)
		            				.addValor(Long.parseLong(email.IdSecuencia), "ClaveProvisoria",ClaveProvisoria);

		            System.out.println(notifIn);
					if (notifIn.P_EVENTOS.size() > 0) {
			            AgregarEvento notif = new AgregarEvento();
			    		System.out.println(notif);
			            //notif.logica(conn, notifIn);
			            //System.out.println(notif.logica(conn, notifIn));
			            
			           // String ressss = Base64.getEncoder().encodeToString((ClaveProvisoria).getBytes()); //encripto la contrasena provisoria

			            System.out.println("----------" +hcClaveNueva +"----------");
			            
			            HashMap<String,Object> parametroEntradaclave = new HashMap<String,Object>();
						parametroEntradaclave.put("p_ClaveProvisoria", hcClaveNueva);
						parametroEntradaclave.put("p_FechaClaveProvisoria", fechaSistema);
						parametroEntradaclave.put("p_IdUsuario", item2.IdUsuario);					
						/*
						List<UpdateClave> listUpdateRelacionPersona = new ArrayList<UpdateClave>();
						UpdateClave update = new UpdateClave();
						
						update.p_ClaveProvisoria           = hcClaveNueva;
						update.p_IdUsuario                 =item2.IdUsuario;
						listUpdateRelacionPersona.add(update);
						//sql.batch(conn, SQL_INSERT_CLAVE, listUpdateRelacionPersona, UpdateClave.class);
						 */
						CrearUsuario.EntradaCrearUsuario CrearUsuario_IN = new CrearUsuario.EntradaCrearUsuario();

						CrearUsuario_IN.p_IdEmpresa = in.p_IdEmpresa;
						CrearUsuario_IN.p_IdUsuario = in.p_IdUsuario;
						CrearUsuario_IN.p_IdModulo = in.p_IdModulo;
						CrearUsuario_IN.p_UsaReglasNegocio = "S";
						CrearUsuario_IN.p_CodigoUsuario = in.p_CodigoUsuario;
						CrearUsuario_IN.p_Clave = hcClaveNueva;
						CrearUsuario_IN.p_FechaIngreso = fechaSistema;
						CrearUsuario_IN.p_FechaVigencia = "0001-01-01";
						CrearUsuario_IN.p_FechaUltimaConexion = fechaSistema;
						CrearUsuario_IN.p_Conectado = "N";
						CrearUsuario_IN.p_Hora = "00:00:00";
						CrearUsuario_IN.p_Terminal = "Terminal Nro";
						CrearUsuario_IN.p_IdEstado = "ACT";
						CrearUsuario_IN.p_IntentosConexion = 0;
						CrearUsuario_IN.p_FechaBloqueoTemporal = fechaSistema+"23:59:59";
						CrearUsuario_IN.p_TipoEntidad = "CLI";
						CrearUsuario_IN.p_IdEntidad = in.p_IdCliente;
						CrearUsuario_IN.p_HeredarRolesUsuario = -1L;
						CrearUsuario_IN.p_Canal = "AGE";
					    
						CrearUsuario CrearUsuario = new CrearUsuario();
						CrearUsuario.SalidaCrearUsuario OutCambioEstado = CrearUsuario.logica(conn, CrearUsuario_IN); 
					    System.out.println(OutCambioEstado.toString());
					    salida.P_OUT_ERRORESCONTADOR = OutCambioEstado.P_OUT_ERRORESCONTADOR;
					    salida.P_OUT_MENSAJES = OutCambioEstado.P_OUT_MENSAJES;
					    encontrados = "OK";

						List<UpdateClave> paramsUpdateClave = new ArrayList<>();
						UpdateClave upClave = new UpdateClave();
						upClave.p_ClaveProvisoria = ClaveProvisoria;
						upClave.p_IdUsuario = in.p_CodigoUsuario;
						paramsUpdateClave.add(upClave);

						sql.batch(conn, SQL_INSERT_CLAVE, paramsUpdateClave, UpdateClave.class);
						/*
						HashMap<String, Object> upClave = new HashMap<>();
						upClave.put("p_ClaveProvisoria", hcClaveNueva);
						upClave.put("p_IdUsuario", in.p_CodigoUsuario);
						sql.batch(conn, SQL_INSERT_CLAVE, upClave, UpdateClave.class);*/

			            System.out.println("OK");	
			            if (salida.P_OUT_ERRORESCONTADOR == 0) {
				            MensajeSalida msj = new MensajeSalida();
							msj.codigo = "OK";
							msj.descripcion = "Usuario creado correctamente.";
							msj.id = 0L;
							msj.tipo = "OK";
							
							salida.P_OUT_MENSAJES.add(msj);
							
				            MensajeSalida msj1 = new MensajeSalida();
				            msj1.codigo = "OK";
				            msj1.descripcion = "Clave provisoria:  "+ClaveProvisoria ;
				            msj1.id = 0L;
				            msj1.tipo = "OK";
				            salida.P_OUT_MENSAJES.add(msj1);
				            
				            CrearAutorizadoOrden_Salida salidaCrearAutorizadoOrden = this.crearAutorizadoOrden(conn, in, item2.DocumentoIdentidad);
				            for (MensajeSalida mensajes : salidaCrearAutorizadoOrden.P_OUT_MENSAJES) {
						    	
					            MensajeSalida mensajeCrearAutorizado 	= new MensajeSalida();
					            mensajeCrearAutorizado.id 				= mensajes.id;
					            mensajeCrearAutorizado.tipo 			= mensajes.tipo;
					            mensajeCrearAutorizado.codigo 			= mensajes.codigo;
					            mensajeCrearAutorizado.descripcion 		= mensajes.descripcion;
					            salida.P_OUT_MENSAJES.add(mensajeCrearAutorizado);
								
						    }
				            
							

				            salida.P_OUT_RESULTADOPROCESO = "OK";
			            	
			            }
					    
			        }
				} catch (Exception e) {
					e.printStackTrace();
					salida.P_OUT_ERRORESCONTADOR++;
					
				}
		}
		
		 
		return salida;		
	}
	
	private CrearAutorizadoOrden_Salida crearAutorizadoOrden(Connection conn, CrearClaveICliente_Entrada in, String documentoIdentidad) throws Exception {
		
		SQL sql = new SQL();

		CrearAutorizadoOrden crearAutorizadoOrden = new CrearAutorizadoOrden();
	    CrearAutorizadoOrden_Entrada entradaCrearAutorizadoOrden = new CrearAutorizadoOrden_Entrada();
		
	    HashMap<String,Object> parametros 	= new HashMap<>();
		parametros.put("DocumentoIdentidad", documentoIdentidad);
		List<ConsultaClientePortafolio> consultarPortafolio = sql.query(conn, SQL_SELECT_CONSULTAR_CLIENTE_PORTAFOLIO, parametros, ConsultaClientePortafolio.class);
		
		entradaCrearAutorizadoOrden.p_IdEmpresa 				= in.p_IdEmpresa;
		entradaCrearAutorizadoOrden.p_IdUsuario 				= in.p_IdUsuario;
		entradaCrearAutorizadoOrden.p_IdModulo 					= in.p_IdModulo;
		entradaCrearAutorizadoOrden.p_IdEstado 					= "ACT";
		entradaCrearAutorizadoOrden.p_UsaReglasNegocio 			= "S";
		entradaCrearAutorizadoOrden.p_IdPersona 				= consultarPortafolio.get(0).IdPersona;
		entradaCrearAutorizadoOrden.p_IdPortafolio 				= consultarPortafolio.get(0).IdPortafolio;
		entradaCrearAutorizadoOrden.p_IdTipoAutorizado 			= in.p_IdTipoAutorizado;
		entradaCrearAutorizadoOrden.p_FirmaOptimusCliente 		= in.p_FirmaOptimusCliente;
		CrearAutorizadoOrden_Salida salidaCrearAutorizadoOrden 	= crearAutorizadoOrden.logica(conn, entradaCrearAutorizadoOrden);
		
		return salidaCrearAutorizadoOrden;
	}
	
	
	//-------------------------------------------------------------------------------------------
	// OBJETO DE TRANSPORTE - CALL
	//-------------------------------------------------------------------------------------------
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception
	{
		Gson gson = Utils.getGsonBuilder();
		CrearClaveICliente_Entrada in = gson.fromJson(jin.toString(), CrearClaveICliente_Entrada.class);
		CrearClaveICliente_Salida out = logica(conn, in);
		
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	//-------------------------------------------------
	// METODO PARA PRUEBAS LOCALES 
	//-------------------------------------------------
	public static void main(String[] args)
	{
		System.out.println("== INICIO PRUEBA LOCAL ==");
		
		try (Connection con = PlataformaUtils.getDESAConnection())
		{
			SQL.exec(con, "set schema BSCL");
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();	
			
			//String jsonCL = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":6034,\"p_IdModulo\":\"ACC\",\"p_UsaReglasNegocio\":\"N\",\"p_CodigoUsuario\":\"14055748-K\",\"p_IdCliente\":10922,\"p_IdTipoAutorizado\":\"ICLIENTE-OPERAR\",\"p_FirmaOptimusCliente\":\"S\"}";
			//String jsonCL = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":6034,\"p_IdModulo\":\"ACC\",\"p_UsaReglasNegocio\":\"N\",\"p_CodigoUsuario\":\"CAMPOS1\",\"p_IdCliente\":3362,\"p_IdTipoAutorizado\":\"ICLIENTE-OPERAR\",\"p_FirmaOptimusCliente\":\"S\"}";
			//String jsonCL = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":6034,\"p_IdModulo\":\"ACC\",\"p_UsaReglasNegocio\":\"N\",\"p_CodigoUsuario\":\"CARAMPANG\",\"p_IdCliente\":3406,\"p_IdTipoAutorizado\":\"ICLIENTE-OPERAR\",\"p_FirmaOptimusCliente\":\"S\"}";
			String jsonCL = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":6034,\"p_IdModulo\":\"ACC\",\"p_UsaReglasNegocio\":\"N\",\"p_CodigoUsuario\":\"CARVILE\",\"p_IdCliente\":3455,\"p_IdTipoAutorizado\":\"ICLIENTE-OPERAR\",\"p_FirmaOptimusCliente\":\"S\"}";
			
			//String jsonCL = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":51,\"p_IdModulo\":\"PLA\",\"p_CodigoUsuario\": FABALO,\"p_IdCliente\": 100140}";
			//{\"p_IdEmpresa\":1,\"p_IdUsuario\":51,\"p_IdModulo\":\"APV\",
			//"{\"p_CodigoUsuario\": EVILLAGRAN}";
			//PDDIRECCION
			CrearClaveICliente_Entrada Objin = gson.fromJson(jsonCL, CrearClaveICliente_Entrada.class);

			CrearClaveICliente demo = new CrearClaveICliente();
			System.out.println("ENTRADA");
			System.out.println(Objin.toString());
			
			try {
				CrearClaveICliente_Salida ObjOut = demo.logica(con, Objin);
				//con.rollback();
				System.out.println("SALIDA");
				System.out.println(ObjOut.toString());
				con.commit();
			} catch (Exception er) {
				er.printStackTrace();
				con.rollback();
			}
			con.rollback();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println("== FIN PRUEBA LOCAL ==");
	}
}