package cl.bcs.mesa.SPT;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class WSConsultaOperacionesSpot implements JavaCall
{
	static final Logger logger = LogManager.getLogger(WSConsultaOperacionesSpot.class);
	//*****************************************************//
	//-------------------------------------------------
	// QUERIES
	//-------------------------------------------------
	private final String SQL_SELECT_ORDENESMX = loadResourceToString("cl/bcs/mesa/SPT/sql/WSConsultaOperacionesSpot_SelectOperacionesSpot.sql");
	
	//-------------------------------------------------
	// CLASES DE ENTRADA 
	//-------------------------------------------------
	public static class WSConsultaOperacionesSpot_Entrada
	{
		public Long 				p_IdEmpresa;
		public Long					p_IdUsuario; 
		public String				p_ClaveUsuario;
		public List<OperacionesSpot>	p_ConsultaOperaciones;
		
		public WSConsultaOperacionesSpot_Entrada()
		{
			p_ConsultaOperaciones = new ArrayList<OperacionesSpot>();
		}
		
		@Override
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	}
	
	public static class OperacionesSpot
	{
		public String DocumentoIdentidad = "";
		public String CodigoPortafolio = "";
		public String IdTipoOperacion = "";
		public String IdEstado = "";
		public String CodigoAgente = "";
		public String FechaDesde = "";
		public String FechaHasta = "";
		
		public OperacionesSpot()
		{
		}
        
		@Override
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	}

	
	//-------------------------------------------------
	// CLASES DE SALIDA 
	//-------------------------------------------------
	public static class WSConsultaOperacionesSpot_Salida
	{
		public Long					P_OUT_ERRORESCONTADOR;
		public Long					p_Out_ContadorEntidadCreada;
		public List<EntidadCreada>	p_Out_EntidadCreada;
		
		@Override
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	}
	
	public static class EntidadCreada
	{
		public String Mensaje;
		public List<Operaciones> Operaciones;
		public int ReturnServicio;
		public int StatusServicio;
		
		public EntidadCreada()
		{
			Operaciones = new ArrayList<Operaciones>();
		}
		
		@Override
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	}

	public static class Operaciones {		
		public String FechaOperacion;
		public String IdTipoOperacion;
		public String TipoOperacion;
		public Long FolioOperacion;
		public BigDecimal TipoCambioCierre;
		public String IdMonedaPrincipal;
		public BigDecimal MontoMonedaPrincipal;
		public String IdMonedaSecundaria;		
		public BigDecimal MontoMonedaSecundaria;
		public String IdEstado;
		public String Estado;
		public String DocumentoIdentidad;
		public String CodigoPortafolio;
		public String NombreCliente;
		public String IdTipoComprobante;
		public String NombreComprobante;
		public String CodigoAgente;
		public String IdTipoCanalIngreso;
		public String NombreCanal;
		public String IdPlataformaNegociacion;
		public String NombreEmpleadoDigitador;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		}
	
	public static class MensajesErrores
	{
		public Long		IdInterno;
		public String	TipoMensaje;
		public String	Codigo;
		public String	Mensaje;
		
		@Override
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	}
	
	
	
	//-------------------------------------------------
	// LOGICA DEL SERVICIO 
	//-------------------------------------------------
public WSConsultaOperacionesSpot_Salida logica(Connection conn, WSConsultaOperacionesSpot_Entrada in) throws Exception
{
	WSConsultaOperacionesSpot_Salida salida = new WSConsultaOperacionesSpot_Salida();
	
	Long ErroresContador = 0L;
	Long ContadorEntidadCreada = 0L;
			
	List<EntidadCreada> listEntidadCreada = new ArrayList<EntidadCreada>();
	List<MensajesErrores> listMensajesErrores = new ArrayList<MensajesErrores>();
			
	String fechaDesdeFN = null;
	String fechaHastaFN = null;
	String docidentidadFN = null;
	String operacionFN = null;
	String estadoFN = "";
	String agenteFN = null;
	String codigoPortafolioFN = null;
	int cantidadReg =  in.p_ConsultaOperaciones.size();
	List<Operaciones> ordenesMX = new ArrayList<Operaciones>();
	List<Operaciones> ordenesMXaux = new ArrayList<Operaciones>();
	EntidadCreada entidadCreada = new EntidadCreada();
	MensajesErrores mensajesErrores = new MensajesErrores();
	
	String jsonFN = new Gson().toJson(in);
	JSONObject json = new JSONObject(jsonFN);
	
	for (int j = 0; j < json.getJSONArray("p_ConsultaOperaciones").length(); j++) {
	org.json.JSONArray jsonArray = json.getJSONArray("p_ConsultaOperaciones");
	for (int i = 0; i < jsonArray.length(); i++){
		try {JSONObject aux = (JSONObject) json.getJSONArray("p_ConsultaOperaciones").get(j);
		try{docidentidadFN = (String) aux.get("DocumentoIdentidad");}
		catch (Exception er) {docidentidadFN = "%%";}
		try{codigoPortafolioFN = (String) aux.get("CodigoPortafolio");}
		catch (Exception er) {codigoPortafolioFN = null;}
		try{operacionFN = (String) aux.get("IdTipoOperacion");}
		catch (Exception er) {operacionFN = null;}
		try{estadoFN = (String) aux.get("IdEstado");}
		catch (Exception er) {estadoFN = null;}
		try{agenteFN = (String) aux.get("CodigoAgente");}
		catch (Exception er) {agenteFN = null;}
		try{fechaDesdeFN = (String) aux.get("FechaDesde"); }
		catch (Exception er) {fechaDesdeFN = null; ErroresContador++;}
		try{fechaHastaFN = (String) aux.get("FechaHasta");}
		catch (Exception er) {fechaHastaFN = null; ErroresContador++;}
		}	
	catch(Exception er) {cantidadReg=0;}
	}
	
	if(cantidadReg > 0){		
		try {
			java.util.Date date1=new SimpleDateFormat("yyyy-MM-dd").parse(fechaDesdeFN); 			    
			java.util.Date date2=new SimpleDateFormat("yyyy-MM-dd").parse(fechaHastaFN);

			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
			LocalDate localDate = LocalDate.parse(fechaHastaFN, formatter);
			LocalDate nextDay = localDate.plusDays(1);  
			if(date1.before(date2) || date1.equals(date2)) {
				long diff = date2.getTime()- date1.getTime();
				long diffDays = diff /(24*60*60*1000);
				if(diffDays<=30) {    				    			
					try {
						SQL sqlConsulta=new SQL();
						long inicioSelectOrdenesSpot = System.currentTimeMillis();
						HashMap<String, Object> params = new HashMap<>();
						if(docidentidadFN!=null) {params.put("p_DocIdentidad", "%"+docidentidadFN+"%");}
						
						params.put("Accion", "%"+operacionFN+"%"); 
						params.put("nemotecnico", "%%"); 
						if("C".equals(operacionFN)){
							params.put("Accion", "COMPRA"); 
							params.put("nemotecnico", "%%"); 
							}
						if("V".equals(operacionFN)){
							params.put("Accion", "VENTA"); 
							params.put("nemotecnico", "%%"); 
							}
						if("CA".equals(operacionFN)){
							params.put("Accion", "COMPRA"); 
							params.put("nemotecnico", "%ARBITR%");  
							}
						if("VA".equals(operacionFN)){
							params.put("Accion", "VENTA"); 
							params.put("nemotecnico", "%ARBITR%");   
							}
						if("CN".equals(operacionFN)){
							params.put("Accion", "CANJE"); 
							params.put("nemotecnico", "%%");   
							}
						params.put("estado", "%%");						
						if(estadoFN.length()>=1){
							params.put("estado", estadoFN);
							}						
						
						if(agenteFN.equals(null) || agenteFN.equals(""))
						{params.put("CodigoAgente", null);}
						else{params.put("CodigoAgente", "%"+agenteFN+"%");}
						
						params.put("fechaDesde", fechaDesdeFN);
						params.put("fechaHasta", nextDay);
						if("".equals(codigoPortafolioFN)){
							params.put("p_CodigoPortafolio", null);
							}
						else {params.put("p_CodigoPortafolio", codigoPortafolioFN+"%");	}	
						
						ordenesMX = sqlConsulta.query(conn, SQL_SELECT_ORDENESMX,params, Operaciones.class);
						for(int k=0;k<ordenesMX.size();k++)
						{
							ordenesMXaux.add(ordenesMX.get(k)); 
							ContadorEntidadCreada++;
						}
						ContadorEntidadCreada+=ordenesMX.size();
						long finSelectOrdenesSpot = System.currentTimeMillis();
						logger.info("Tiempo SQL_SELECT_ORDENESMX: "+ (finSelectOrdenesSpot - inicioSelectOrdenesSpot));   
						entidadCreada.Mensaje = "OK";						
						entidadCreada.Operaciones=ordenesMX;
						entidadCreada.ReturnServicio = 1;
						entidadCreada.StatusServicio = 1;								
						}
					catch(Exception er) {
						er.printStackTrace();
						ErroresContador++;
						entidadCreada.Mensaje = "ERROR_EN_SERVIDOR";
						entidadCreada.ReturnServicio = 0;
						entidadCreada.StatusServicio = 2;
						}
					}
					else{
						ErroresContador++;
						entidadCreada.Mensaje = "UI_FECHAS_NO_DEBEN_SOBREPASAR_30_DIAS_DE_DIFERENCIA";
						entidadCreada.ReturnServicio = 0;
						entidadCreada.StatusServicio = 3;							
						}
				}
			else{
				ErroresContador++; 
				entidadCreada.Mensaje = "UI_FECHA_DESDE_NO_DEBE_SER_MAYOR_A_FECHA_HASTA";
				entidadCreada.ReturnServicio = 0;
				entidadCreada.StatusServicio = 3;						
				}
			}
		catch(Exception er) {
			ErroresContador++;
			entidadCreada.Mensaje = "UI_FECHAS_INVALIDAS";
			entidadCreada.ReturnServicio = 0;
			entidadCreada.StatusServicio = 3;								
			}
		mensajesErrores.IdInterno = 0L;
		mensajesErrores.TipoMensaje = "OK";
		}else {
			ErroresContador++;
			entidadCreada.Mensaje = "UI_ERROR_EN_VALIDACION_DE_ENTRADA_AL_SERVICIO";
			entidadCreada.ReturnServicio = 0;
			entidadCreada.StatusServicio = 3;
		}
	listMensajesErrores.add(mensajesErrores);
	listEntidadCreada.add(entidadCreada);
	salida.P_OUT_ERRORESCONTADOR = ErroresContador;
	salida.p_Out_ContadorEntidadCreada = ContadorEntidadCreada;
	salida.p_Out_EntidadCreada = listEntidadCreada;
	}
	return salida;
	}
	

	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception
	{
		Gson gson = Utils.getGsonBuilder();
		WSConsultaOperacionesSpot_Entrada in = gson.fromJson(jin.toString(), WSConsultaOperacionesSpot_Entrada.class);
		WSConsultaOperacionesSpot_Salida out = logica(conn, in);
		
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	
	//-------------------------------------------------
	// METODO PARA PRUEBAS LOCALES 
	//-------------------------------------------------
	public static void main(String[] args) throws Exception
	{
		System.out.println("== INICIO PRUEBA LOCAL ==");
		EntidadCreada entidadCreada = new EntidadCreada();
		WSConsultaOperacionesSpot_Salida salida = new WSConsultaOperacionesSpot_Salida();
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties"))
		{
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			
			//String jsonCL = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":0,\"p_ClaveUsuario\":null,\"p_ConsultaOperaciones\":[{\"IdEntidad\":1,\"TipoEntidad\":\"PERSONA\",\"IdTipoPersona\":\"N\",\"TipoDocumentoIdentidad\":\"R\",\"DocumentoIdentidad\":\"18576992-5\",\"Nombres\":\"SERGIO\",\"IdGenero\":\"M\",\"IdPaisNacimientoConstitucion\":44,\"FechaNacimientoConstitucion\":\"1993-08-05\",\"IdNacionalidadPrincipal\":44,\"IdEstadoCivil\":\"SO\",\"IdPaisNacimiento\":44,\"IdPaisNacionalidad\":44,\"IdPaisResidencia\":44,\"DireccionFisica\":[{\"IdDireccionFisica\":1307707,\"IdProposito\":\"PART\",\"Sector\":\"Centro\",\"CodigoPostal\":\"6162000\",\"Calle\":\"Baquedano\",\"Numero\":\"999\",\"IdCiudad\":2614}],\"Telefono\":[{\"IdTelefono\":1735014,\"IdProposito\":\"PART\",\"Numero\":\"612654321\",\"IdPais\":44,\"Tipo\":\"FIJO\"}],\"DireccionElectronica\":[{\"IdDireccionElectronica\":855876,\"IdProposito\":\"PART\",\"Direccion\":\"smedrano@email.com\"}]}]}";
			//String jsonCL = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":0,\"p_ClaveUsuario\":null,\"p_ConsultaOperaciones\":[{\"rutcliente\":\"\",\"cuenta\":\"\",\"operacion\":\"\",\"estado\":\"\",\"agente\":\"\",\"fechaDesde\":\"2021-11-23\",\"fechaHasta\":\"2021-12-22\"}]}";
			//String jsonCL = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":0,\"p_ClaveUsuario\":\"\",\"p_ConsultaOperaciones\":[{}]}";
			//String jsonCL = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":0,\"p_ClaveUsuario\":null,\"p_ConsultaOperaciones\":[{\"docidentidad\":\"20231097\",\"cuenta\":\"0\",\"operacion\":\"\",\"estado\":\"\",\"agente\":\"\",\"fechaDesde\":\"2021-12-02\",\"fechaHasta\":\"2021-12-22\"},{\"docidentidad\":\"20231097\",\"cuenta\":\"0\",\"operacion\":\"\",\"estado\":\"\",\"agente\":\"\",\"fechaDesde\":\"2021-12-02\",\"fechaHasta\":\"2021-12-22\"}]}";
			String jsonCL = "{\"p_IdEmpresa\":0,\"p_IdUsuario\":0,\"p_ClaveUsuario\":\"string\",\"p_ConsultaOperaciones\":[{\"DocumentoIdentidad\":\"\",\"CodigoPortafolio\":\"\",\"IdTipoOperacion\":\"\",\"IdEstado\":\"\",\"CodigoAgente\":\"\",\"FechaDesde\":\"2021-12-02\",\"FechaHasta\":\"2021-12-22\"}]}";
			try{
				WSConsultaOperacionesSpot_Entrada Objin = gson.fromJson(jsonCL, WSConsultaOperacionesSpot_Entrada.class);
				WSConsultaOperacionesSpot demo = new WSConsultaOperacionesSpot();
				System.out.println(Objin.toString());
				try {
					WSConsultaOperacionesSpot_Salida ObjOut = demo.logica(con, Objin);
					con.rollback();
					//con.commit();
					System.out.println(ObjOut.toString());
				} catch (Exception er) {
					er.printStackTrace();
					con.rollback();
				}
				con.rollback();
			}
			catch (Exception er) {
				jsonCL="{}";
				WSConsultaOperacionesSpot_Entrada Objin = gson.fromJson(jsonCL, WSConsultaOperacionesSpot_Entrada.class);
				WSConsultaOperacionesSpot demo = new WSConsultaOperacionesSpot();
				System.out.println(Objin.toString());
				try {
					WSConsultaOperacionesSpot_Salida ObjOut = demo.logica(con, Objin);
					con.rollback();
					//con.commit();
					System.out.println("SALIDA");
					System.out.println(ObjOut.toString());
				} catch (Exception er1) {
					er1.printStackTrace();
					con.rollback();
				}
				con.rollback();
			}	
			
			
		} catch (SQLException e) {
			entidadCreada.Mensaje = "ERROR_EN_SERVIDOR";
			entidadCreada.ReturnServicio = 0;
			entidadCreada.StatusServicio = 2;
			salida.P_OUT_ERRORESCONTADOR =1L;
		}
		
		System.out.println("== FIN PRUEBA LOCAL ==");
	}
}