package cl.bcs.tesoreria;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public  class CrearCargaMasivaPac implements JavaCall{
	
	private final String SQL_INSERT_UNIVERSO_PAC  = loadResourceToString("cl/bcs/tesoreria/sql/CrearCargaMasivaPac_InsertUniversoPac.sql");
	private final String SQL_UPDATE_UNIVERSO_PAC = loadResourceToString("cl/bcs/tesoreria/sql/CrearCargaMasivaPac_UpdateUniversoPac.sql");
	private final String SQL_UPDATE_ESTADO_ARCHIVO_PAC = loadResourceToString("cl/bcs/tesoreria/sql/CrearCargaMasivaPac_UpdateEstadoUniversoPac.sql");
	private final String SQL_UPDATE_DEHABILITAR_M_PAC = loadResourceToString("cl/bcs/tesoreria/sql/CrearCargaMasivaPac_UpdateDeshabilitarMandatoPac.sql");
	private final String SQL_SELECT_UNIVERSO_PAC = loadResourceToString("cl/bcs/tesoreria/sql/CrearCargaMasivaPac_SelectUniversoPac.sql");
	
	//private final String SQL_INSERT_RENDICION_PAC = loadResourceToString("cl/bcs/tesoreria/sql/CrearCargaMasivaPac_InsertRendicionPac.sql");
	
	//private final String SQL_SELECT_UNIVERSO_PAC = loadResourceToString("cl/bcs/tesoreria/sql/CrearCargaMasivaPac_SelectUniversoPac.sql");
		
	//--AES--		
		/******************************************************************************
		 * 							CLASES
		******************************************************************************/
    public static class EntradaCargaMasivaPac{
		public Long   p_IdEmpresa;
		public Long   p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public String p_FechaCarga;
		public List<DetalleCargaMasiva> p_CargaMasiva;
		public List<DetalleTransanccionesUni>p_CargaMasivaDetalle;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
    public static class DetalleCargaMasiva{		
		public Long 	  IdInterno;			  
		public String 	  NombreArchivo;
		public String CodigoTipoCargaMasiva;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
    public static class DetalleTransanccionesUni{
    	 public Long IdCargaMasivaDetalle;
    	 public Long IdCargaMasivaPac;
		 public String CodigoBanco;
		 public String CodigoEmpresa;
		 public String CodigoConvenio;
		 public String TipoRegistro;
		 public String IdentServicio;
		 public String FechaGeneracion;
		 public String FechaInicioServ;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}
    public static class DetalleTransanccionesUni2{
   	
		 public String IdentServicio;
		 public String tipo;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
   
    
	public static class MensajeTraansaccionPac	
	{	
	
		public static String		codBanco;     			

		
		public String toString(){
			return Utils.printJson(this, false);
		}			
	}		
	public static class SalidaCarga{
	
		public SalidaCarga() {
			super();
		}
		public String	 mensaje;
		
	}
	public static class SalidaCargaMasivaPac{
			public Long P_OUT_ERRORESCONTADOR;
			public List <MensajeSalida> P_OUT_MENSAJES;
		
			public SalidaCargaMasivaPac(){
				this.P_OUT_ERRORESCONTADOR = 0L;
				this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			
			}
			
			public String toString() {
				return Utils.printJson(this, false);
			}
	}			

		/******************************************************************************
		 * 						OBJETO DE TRANSPORTE
		 ******************************************************************************/
		@Override
		public JsonObject call(Connection conn, JsonObject jin) throws Exception {
			Gson gson = new Gson();		
			EntradaCargaMasivaPac in = gson.fromJson(jin.toString() ,EntradaCargaMasivaPac.class);
			
			//RecaudacionBCP_Entrada in = gson.fromJson(jin.toString(), RecaudacionBCP_Entrada.class);
			SalidaCargaMasivaPac out = logica(conn, in);
			
			out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
			
			JsonParser parser = new JsonParser();
			JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
			return dataout;
		}
		/******************************************************************************
		 * 							LOGICA
		 ******************************************************************************/
		
		public SalidaCargaMasivaPac logica(Connection conn, EntradaCargaMasivaPac entrada) {
			
			SQL sql = new SQL();
			SalidaCargaMasivaPac Salida = new  SalidaCargaMasivaPac();
			Salida.P_OUT_ERRORESCONTADOR= 0L;
			
			try {		
				
				//---------------
				for (DetalleCargaMasiva carga0 : entrada.p_CargaMasiva){
					DetalleCargaMasiva _mensajeCargaMasiva = new DetalleCargaMasiva();
					_mensajeCargaMasiva.CodigoTipoCargaMasiva = carga0.CodigoTipoCargaMasiva;
					
					//**********************************************************************************
					if(_mensajeCargaMasiva.CodigoTipoCargaMasiva.equals("CARGA_UNIVERSO_PAC")){

							Long contador = (long) 16;

							List<DetalleTransanccionesUni2> paramInsertMensajeTransaccionPacU2 = new ArrayList<DetalleTransanccionesUni2>();
							DetalleTransanccionesUni2 _mensajeTransaccionP2 = new DetalleTransanccionesUni2();
							_mensajeTransaccionP2.tipo = "UNI";
							paramInsertMensajeTransaccionPacU2.add(_mensajeTransaccionP2);
							sql.batch(conn, SQL_UPDATE_ESTADO_ARCHIVO_PAC, paramInsertMensajeTransaccionPacU2, DetalleTransanccionesUni2.class);
							sql.batch(conn, SQL_UPDATE_DEHABILITAR_M_PAC, paramInsertMensajeTransaccionPacU2, DetalleTransanccionesUni2.class);
							
							conn.commit();
							List<DetalleTransanccionesUni> paramInsertMensajeTransaccionPacU = new ArrayList<DetalleTransanccionesUni>();
							for (DetalleTransanccionesUni carga : entrada.p_CargaMasivaDetalle){
								
								DetalleTransanccionesUni _mensajeTransaccionP = new DetalleTransanccionesUni();  
								_mensajeTransaccionP.IdCargaMasivaDetalle = contador;
								_mensajeTransaccionP.IdCargaMasivaPac = contador;
								_mensajeTransaccionP.CodigoBanco = carga.CodigoBanco;
								_mensajeTransaccionP.CodigoEmpresa = carga.CodigoEmpresa;
								_mensajeTransaccionP.CodigoConvenio = carga.CodigoConvenio;
								_mensajeTransaccionP.TipoRegistro = carga.TipoRegistro;
								_mensajeTransaccionP.IdentServicio = carga.IdentServicio.replace(" ","");
								_mensajeTransaccionP.FechaGeneracion = carga.FechaGeneracion;
								_mensajeTransaccionP.FechaInicioServ = carga.FechaInicioServ;
								paramInsertMensajeTransaccionPacU.add(_mensajeTransaccionP);
								
								contador++;
							}
							sql.batch(conn, SQL_INSERT_UNIVERSO_PAC, paramInsertMensajeTransaccionPacU, DetalleTransanccionesUni.class);
							
							HashMap<String,Object> params2 = new HashMap<String,Object>();
							//params.put("IdentServicio", 'a');
							List<DetalleTransanccionesUni2> traspasos = sql.query(conn, SQL_SELECT_UNIVERSO_PAC,params2, DetalleTransanccionesUni2.class);
							List<DetalleTransanccionesUni2> ListaArchivo = new ArrayList<DetalleTransanccionesUni2>();
							for(DetalleTransanccionesUni2 solicitud : traspasos){
								
								DetalleTransanccionesUni2 _datos = new DetalleTransanccionesUni2();
								_datos.IdentServicio = solicitud.IdentServicio;
								ListaArchivo.add(_datos);	
								
								
								
							}
							sql.batch(conn, SQL_UPDATE_UNIVERSO_PAC, ListaArchivo, DetalleTransanccionesUni2.class);
					}
					
					
					
					
				}
					
					Salida.P_OUT_ERRORESCONTADOR= 0L;
					Salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_OK,"UI_PROCESO_EJECUTADO_EXITO","Archivo Cargado con exito"));
					    
				
			}catch (Exception e){
				e.printStackTrace();
				Salida.P_OUT_ERRORESCONTADOR= 1L;
				Salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"Error","Error en el sistema"));
				
			}
			
			return Salida;
		}


		
		/******************************************************************************
		 * 							MAIN
		 ******************************************************************************/
		public static void main(String[] args){
			
			try (Connection con = PlataformaUtils.getDESAConnection())
			{
				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_IdModulo\":\"TES\",\"p_UsaReglasNegocio\":\"S\",\"p_FechaCarga\":\"2018-10-02\",\"p_CargaMasiva\":[{\"IdInterno\":1,\"NombreArchivo\":\"bajapc2_01-03-2018.txt\",\"CodigoTipoCargaMasiva\":\"CARGA_UNIVERSO_PAC\"}],\"p_CargaMasivaDetalle\":[{\"CodigoBanco\":\"001\",\"CodigoEmpresa\":\"006\",\"CodigoConvenio\":\"023\",\"TipoRegistro\":\"D\",\"IdentServicio\":\"0000000000011111115046\",\"Filler\":\" \",\"FechaGeneracion\":\"20180228\",\"FechaInicioServ\":\"20111227\"},{\"CodigoBanco\":\"001\",\"CodigoEmpresa\":\"006\",\"CodigoConvenio\":\"023\",\"TipoRegistro\":\"D\",\"IdentServicio\":\"0000000000000366666699\",\"Filler\":\" \",\"FechaGeneracion\":\"20180228\",\"FechaInicioServ\":\"20120824\"}]}";
				
				EntradaCargaMasivaPac entrada = gson.fromJson(json, EntradaCargaMasivaPac.class);
				CrearCargaMasivaPac demo = new CrearCargaMasivaPac();
				
				SalidaCargaMasivaPac salida = demo.logica(con, entrada);
				
				
				System.out.println("salida --> "+salida.toString());
				con.commit();//--rollback();
				
			}catch(Exception e){
				e.printStackTrace(); 
			}
			
			System.out.println("== FIN PRUEBA LOCAL ==");
		}



		
	
}
