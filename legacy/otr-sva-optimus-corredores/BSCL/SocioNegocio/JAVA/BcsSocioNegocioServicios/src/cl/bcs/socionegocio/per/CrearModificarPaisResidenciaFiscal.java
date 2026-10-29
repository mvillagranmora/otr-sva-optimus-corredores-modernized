package cl.bcs.socionegocio.per;

import java.sql.Connection;
import java.sql.SQLException;
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
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearModificarPaisResidenciaFiscal implements JavaCall {
	private final Log log = new Log("SOCIONEGOCIO_CrearModificarPaisResidenciaFiscal");
	private final String SQL_InsertPaisResidenciaFiscal_IN = loadResourceToString("cl/bcs/socionegocio/per/sql/CrearModificarPaisResidenciaFiscal_InsertPaisResidenciaFiscal.sql");
	private final String SQL_UpdatePaisResidenciaFiscal_IN = loadResourceToString("cl/bcs/socionegocio/per/sql/CrearModificarPaisResidenciaFiscal_UpdatePaisResidenciaFiscal.sql");
	private final String SQL_PaisResidenciaFiscal = loadResourceToString("cl/bcs/socionegocio/per/sql/CrearModificarPaisResidenciaFiscal_SelPaisResidenciaFiscal.sql");

	public static class ObjPaisResidenciaFiscal{
		public Long IdPaisResidenciaFiscal;
		public Long IdPersona;
		public Long IdPais;
		public java.util.Date FechaInicioTributacionPais;
		public String IdEstado;
		public Long IdEmpresa;
		public Long IdUsuarioCreador;
		public Long IdUsuarioModificador;
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	}
	public static class ObjPaisResidenciaFiscalIN{
		public Long idEmpresa;
		public String accion;
		public ArrayList<ObjPaisResidenciaFiscal> paisResidenciaFiscal;
		public ObjPaisResidenciaFiscalIN (){
			paisResidenciaFiscal = new ArrayList<ObjPaisResidenciaFiscal>();
		};
		public String toString() {
			return Utils.printJson(this, false);
		};
	}
	public static class ObjPaisResidenciaFiscalOUT{
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long	P_OUT_ERRORESCONTADOR;
		
		public ObjPaisResidenciaFiscalOUT (){
			P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		};
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	};
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		Gson gson = new Gson();
		ObjPaisResidenciaFiscalIN jin=gson.fromJson(in.toString() ,ObjPaisResidenciaFiscalIN.class);
		ObjPaisResidenciaFiscalOUT out=logica(conn,jin);
		
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public ObjPaisResidenciaFiscalOUT validaData(Connection conn, ObjPaisResidenciaFiscalIN in) throws Exception{
		SQL sql = new SQL();
		ObjPaisResidenciaFiscalOUT out = new ObjPaisResidenciaFiscalOUT();
		HashMap<String, String> valores = new HashMap<String,String>();
		Long index = new Long(0);
		try
		{
			if (in.paisResidenciaFiscal.size() > 0){
				//busco pais de residencia existentes
				HashMap<String, Object> params = new HashMap<String, Object>();
				ArrayList<String> dato = new ArrayList<>();
				params.put("IdEmpresa",in.idEmpresa);
				
				for(ObjPaisResidenciaFiscal item : in.paisResidenciaFiscal){
					dato.add(item.IdPaisResidenciaFiscal.toString());					
				}
				params.put("PaisResidenciaFiscal",dato);
				List<ObjPaisResidenciaFiscal> paisResidenciaFiscal = sql.query(conn, SQL_PaisResidenciaFiscal, params, ObjPaisResidenciaFiscal.class);
				for (ObjPaisResidenciaFiscal item : paisResidenciaFiscal){
					out.P_OUT_MENSAJES.add(new MensajeSalida(index, MensajeSalida.MENSAJE_ERROR, "UI_PAIS_RESIDENCIA_FISCAL_EXISTENTE", valores, conn, new Long(in.idEmpresa)));
					index++;
				}
				out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
			}
		}
		catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return out;
	}
	
	public ObjPaisResidenciaFiscalOUT logica(Connection conn, ObjPaisResidenciaFiscalIN in) throws Exception {
		SQL sql = new SQL();
		ObjPaisResidenciaFiscalOUT out = new ObjPaisResidenciaFiscalOUT();
		HashMap<String, String> valores = new HashMap<String,String>();
		Long index = new Long(0);
		try {
			log.write("Inicia Ejecucion Crear/Modificar Pais Residencia Fiscal: "+in);
			HashMap<String,Object> paramsVacio = new HashMap<String,Object>();
			//parametro de entrada
			if (in.paisResidenciaFiscal.size() > 0){
				log.write("Carga Tabla Global: "+in.paisResidenciaFiscal);
				
				if (in.accion.equals("MODIFICAR")){
					sql.batch(conn, SQL_UpdatePaisResidenciaFiscal_IN, in.paisResidenciaFiscal, ObjPaisResidenciaFiscal.class);

					out.P_OUT_MENSAJES.add(new MensajeSalida(index, MensajeSalida.MENSAJE_INFORMACION, "UI_PAIS_RESIDENCIA_FISCAL_ACTUALIZADAS", valores, conn, in.idEmpresa));
					index++;
				}
				else {
					sql.batch(conn, SQL_InsertPaisResidenciaFiscal_IN, in.paisResidenciaFiscal ,ObjPaisResidenciaFiscal.class);
					
					out.P_OUT_MENSAJES.add(new MensajeSalida(index, MensajeSalida.MENSAJE_INFORMACION, "UI_PAIS_RESIDENCIA_FISCAL_CREADAS", valores, conn, in.idEmpresa));
					index++;
				}
				
			}
			else {
				out.P_OUT_MENSAJES.add(new MensajeSalida(index, MensajeSalida.MENSAJE_ERROR, "UI_INGRESAR_PAIS_RESIDENCIA_FISCAL_ERROR", valores, conn, in.idEmpresa));
				index++;
			}
			out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
			
		}catch (Exception e){
			e.printStackTrace();
		}

		return out;
	}
	private static ObjPaisResidenciaFiscalIN getDataPrueba(){
		/*ObjPaisResidenciaFiscalIN entrada = new ObjPaisResidenciaFiscalIN();
		entrada.idEmpresa = new Long(1);
		entrada.accion = "CREAR";
		ObjPaisResidenciaFiscal entradaData = new ObjPaisResidenciaFiscal(); 
		//prueba pais de residencia fiscal
		entradaData.FechaCreacion = "2017-09-11";
		entradaData.FechaModificacion = "2017-09-11";
		entradaData.IdPersona = "ACT";
		entradaData.IdEstado = "ACT";
		entradaData.IdPaisDeResidenciaFiscal = "1";
		entradaData.IdPais = new Long(44);
		entradaData.IdUsuarioCreador = new Long(9);
		entradaData.IdUsuarioModificador = new Long(9);
		entradaData.FechaInicioTributacionPais = "2015-09-11";;
		entrada.paisResidenciaFiscal.add(entradaData);	
		
		entradaData = new ObjPaisResidenciaFiscal(); 
		entradaData.IdPaisDeResidenciaFiscal = "1";
		entradaData.IdPais = new Long(44);
		entradaData.FechaInicioTributacionPais = "2015-09-11";
		entradaData.IdPersona = "ACT";
		entradaData.IdEstado = "ACT";		
		entradaData.FechaCreacion = "2017-09-11";
		entradaData.FechaModificacion = "2017-09-11";
		entradaData.IdUsuarioCreador = new Long(9);
		entradaData.IdUsuarioModificador = new Long(9);
		
		entrada.paisResidenciaFiscal.add(entradaData);*/
		Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
		String json = "{\r\n"
				+ "    \"idEmpresa\": 1,\r\n"
				+ "    \"paisResidenciaFiscal\": [\r\n"
				+ "        {\r\n"
				+ "            \"IdPaisResidenciaFiscal\": 1,\r\n"
				+ "            \"IdPais\": 48,\r\n"
				+ "            \"FechaInicioTributacionPais\": \"2010-01-01\",\r\n"
				+ "            \"FechaModificacion\": \"2024-07-23\",\r\n"
				+ "            \"IdUsuarioModificador\": 97,\r\n"
				+ "            \"IdEstado\": \"ACT\",\r\n"
				+ "            \"IdEmpresa\": 1\r\n"
				+ "        }\r\n"
				+ "    ],\r\n"
				+ "    \"accion\": \"CREAR\"\r\n"
				+ "}";
		ObjPaisResidenciaFiscalIN entrada= gson.fromJson(json ,ObjPaisResidenciaFiscalIN.class);
		return entrada;
	};
	
	public static void main(String[] args) throws SQLException {
		// TODO Auto-generated method stub
		Connection conn = null;
		Gson gson = new Gson();
		JsonParser parser = new JsonParser();
		
		try {
			System.out.println("== BEGIN ==");
			conn = PlataformaUtils.getDESAConnection();
			//datos entrada
			JsonObject in = parser.parse(gson.toJson(getDataPrueba())).getAsJsonObject();
			CrearModificarPaisResidenciaFiscal prueba = new CrearModificarPaisResidenciaFiscal(); 
			ObjPaisResidenciaFiscalOUT resp=gson.fromJson(prueba.call(conn, in),ObjPaisResidenciaFiscalOUT.class);
			//mientras pruebo hare el rollback
			System.out.println(resp);
			//conn.rollback();
			conn.commit();
		}catch (Exception e){
			e.printStackTrace();
		}finally {
			conn.close();
		}
			System.out.println("== END ==");
	}
}
