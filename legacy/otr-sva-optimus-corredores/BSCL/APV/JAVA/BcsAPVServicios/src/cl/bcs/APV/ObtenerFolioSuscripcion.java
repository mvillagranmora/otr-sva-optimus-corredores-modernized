package cl.bcs.APV;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.nuam.StoredProcedure;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ObtenerFolioSuscripcion implements JavaCall 
{
	static final Logger logger = LogManager.getLogger(ObtenerFolioSuscripcion.class);
	//-------------------------------------------------
	// QUERIES
	//-------------------------------------------------
	private final String SQL_SELECT_FOR_UPDATE						= loadResourceToString("cl/bcs/APV/sql/ObtenerFolioSuscripcion_SelectForUpdate.sql");
	private final String SQL_INSERT_FOLIO_SUSCRIPCION				= loadResourceToString("cl/bcs/APV/sql/ObtenerFolioSuscripcion_InsertFolioSuscripcion.sql");
	
	//-------------------------------------------------
	// CLASES DE ENTRADA 
	//-------------------------------------------------
	public static class ObtenerFolioSuscripcion_Entrada
	{
		public Long 				p_IdEmpresa;
		public Long					p_IdUsuario;
		public String				p_IdModulo;
		public String				p_Canal;
		
		@Override
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	}
	
	
	//-------------------------------------------------
	// CLASES DE SALIDA 
	//-------------------------------------------------
	public static class ObtenerFolioSuscripcion_Salida
	{
		public String				p_Out_Canal;
		public Long					p_Out_Folio;
		public String				p_Out_FolioSuscripcion;
		public Long					p_Out_ContadorError;
		public MensajesErrores		p_Out_Mensajes;
		
		@Override
		public String toString()
		{
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
	// CLASES UTILITARIAS 
	//-------------------------------------------------
	public static class SelectFolioSuscripcion
	{
        public String	Canal;
        public Long		Folio;
		
		@Override
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	}
	
	public static class InsertUpdateFolioSuscripcion
	{
        public String	p_Canal;
        public Long		p_Folio;
		
		@Override
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	}
	public static class FolioSuscripcionActualizados extends BcsPojo{
		public String 	p_Out_Canal;
		public Long 	p_Out_Folio;
		public String 	p_Out_FolioSuscripcion;
		
		public FolioSuscripcionActualizados() {
			p_Out_Canal = "";
			p_Out_Folio = 0L;
			p_Out_FolioSuscripcion = "";
		}
	}
	
	//-------------------------------------------------
	// LOGICA DEL SERVICIO 
	//-------------------------------------------------
	public ObtenerFolioSuscripcion_Salida logica(Connection conn, ObtenerFolioSuscripcion_Entrada in) throws Exception
	{
		logger.info("Inicia ObtenerFolioSuscripcion");
		SQL sql = new SQL();
		
		ObtenerFolioSuscripcion_Salida salida = new ObtenerFolioSuscripcion_Salida();
		
		if(!in.p_Canal.isEmpty())
		{
			HashMap<String,Object> parametroEntrada = new HashMap<String,Object>();
			parametroEntrada.put("p_Canal", in.p_Canal);
			List<SelectFolioSuscripcion> SelectFolioSuscripcion = sql.query(conn, SQL_SELECT_FOR_UPDATE, parametroEntrada, SelectFolioSuscripcion.class);
			
			if(!SelectFolioSuscripcion.isEmpty())
			{
				JsonObject dataSRVIn = new JsonObject();
				dataSRVIn.addProperty("p_Canal", SelectFolioSuscripcion.get(0).Canal );
				dataSRVIn.addProperty("p_Folio", SelectFolioSuscripcion.get(0).Folio + 1L);
				StoredProcedure kHashSrv=new StoredProcedure("BSCL.APV.BD.Servicios::ActualizarFolioSuscripcion");
				
				logger.info("INICIO::SQL_CALL_UPDATE_FOLIO_SUSCRIPCION");
				kHashSrv.call(conn, dataSRVIn);
				logger.info("FIN::SQL_CALL_UPDATE_FOLIO_SUSCRIPCION");
				
				salida.p_Out_Canal = SelectFolioSuscripcion.get(0).Canal;
				salida.p_Out_Folio = SelectFolioSuscripcion.get(0).Folio + 1L;
				salida.p_Out_FolioSuscripcion = SelectFolioSuscripcion.get(0).Canal + (SelectFolioSuscripcion.get(0).Folio + 1L);
				salida.p_Out_ContadorError = 0L;
			}
			else
			{
				List<InsertUpdateFolioSuscripcion> listInsertFolioSuscripcion = new ArrayList<InsertUpdateFolioSuscripcion>();
				InsertUpdateFolioSuscripcion insertFolioSuscripcion = new InsertUpdateFolioSuscripcion();
				
				insertFolioSuscripcion.p_Canal = in.p_Canal;
				insertFolioSuscripcion.p_Folio = 1L;
				listInsertFolioSuscripcion.add(insertFolioSuscripcion);
				
				sql.batch(conn, SQL_INSERT_FOLIO_SUSCRIPCION, listInsertFolioSuscripcion, InsertUpdateFolioSuscripcion.class);
				
				salida.p_Out_Canal = in.p_Canal;
				salida.p_Out_Folio = 1L;
				salida.p_Out_FolioSuscripcion = in.p_Canal + 1;
				salida.p_Out_ContadorError = 0L;
			}
		}
		else
		{
			MensajesErrores mensajesErrores = new MensajesErrores();
			
			mensajesErrores.IdInterno = 0L;
			mensajesErrores.TipoMensaje = "ERROR";
			mensajesErrores.Codigo = "NO_SE_INGRESO_UN_CANAL";
			mensajesErrores.Mensaje = "NO_SE_INGRESO_UN_CANAL";
			
			salida.p_Out_Canal = "";
			salida.p_Out_Folio = 0L;
			salida.p_Out_FolioSuscripcion = "";
			salida.p_Out_ContadorError = 1L;
			salida.p_Out_Mensajes = mensajesErrores;
		}
		logger.info("Fin ObtenerFolioSuscripcion");
		return salida;
	}
	
	
	//-------------------------------------------------------------------------------------------
	// OBJETO DE TRANSPORTE - CALL
	//-------------------------------------------------------------------------------------------
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception
	{
		Gson gson = Utils.getGsonBuilder();
		ObtenerFolioSuscripcion_Entrada in = gson.fromJson(jin.toString(), ObtenerFolioSuscripcion_Entrada.class);
		ObtenerFolioSuscripcion_Salida out = logica(conn, in);
		
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
		
		try (Connection con = PlataformaUtils.getBOLSAConnection())
		{
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();

			String jsonCL = "{\"p_IdEmpresa\": 1,\"p_IdUsuario\": \"6\",\"p_IdModulo\": \"APV\",\"p_Canal\": \"W\"}";

			ObtenerFolioSuscripcion_Entrada Objin = gson.fromJson(jsonCL, ObtenerFolioSuscripcion_Entrada.class);

			ObtenerFolioSuscripcion demo = new ObtenerFolioSuscripcion();
			System.out.println("ENTRADA");
			System.out.println(Objin.toString());
			
			try {
				ObtenerFolioSuscripcion_Salida ObjOut = demo.logica(con, Objin);
//				con.commit();
				con.rollback();
				System.out.println("SALIDA");
				System.out.println(ObjOut.toString());
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