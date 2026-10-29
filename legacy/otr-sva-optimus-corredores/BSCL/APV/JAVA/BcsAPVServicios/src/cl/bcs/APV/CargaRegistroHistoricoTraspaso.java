package cl.bcs.APV;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.APV.CargaMasivaArchivo.CargaArchivo;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;

import java.math.BigDecimal;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CargaRegistroHistoricoTraspaso implements JavaCall {

	private final String SQL_INSERT_CARGA_XML = loadResourceToString("cl/bcs/APV/sql/CargaXMLRegistroHistorico_Insert.sql");
	private final String SQL_INSERT_CARGA_ARCHIVO = loadResourceToString("cl/bcs/APV/sql/CargaMasivaArchivo_Insert.sql");

	/********************
	 * Clases IN/OUT *
	 ********************/

	// SERVICIO ENTRADA
	public static class Parametros_Entrada {

		public String p_IdEmpresa;
		public String p_IdModulo;
		public String p_IdUsuario;
		public String p_NombreCarga;
		public String p_Origen;
		public String p_FechaCarga;
		public String p_Archivo;	
	}
	
	// SERVICIO SALIDA
	public static class ParametrosSalidaServicio {
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR;

		public String toString() {
			return Utils.printJson(this, false);
		}

		public ParametrosSalidaServicio() {
			P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
	}
	
	public static class EntidadPersona {
		public String p_IdEmpresa;
		public String p_IdModulo;
		public String p_IdUsuario;
		public String p_NombreCarga;
		public String p_Origen;
		public String p_FechaCarga;
		public String p_Archivo;	
	}

	public static class CargaArchivo {
		public String IdEmpresa;
		public Long IdCargaMasiva;
		public String NombreCargaMasiva;
		public Long CantidadRegistros;
		public String IdUsuario;
		public Long IdTipoCarga;
		public String Descripcion;
		public String FechaCarga;
		public String IdEstado;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	};

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();
		Parametros_Entrada Objin = gson.fromJson(in.toString(), Parametros_Entrada.class);
		ParametrosSalidaServicio Objout = logica(conn, Objin);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(Objout)).getAsJsonObject();

		return dataout;
	};

	/************************
	 * Métodos Básicos *
	 ************************/
	public ParametrosSalidaServicio logica(Connection con, Parametros_Entrada in) throws Exception {
		ParametrosSalidaServicio salida = new ParametrosSalidaServicio();
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, Long.valueOf(in.p_IdEmpresa), "PLATAFORMA", "PLA.FechaSistema");
		SQL sql = new SQL();

		MensajeSalida msg;
		
		try {
			List<Long> idsCargaArchivo = Utils.getSecuencia(con, "BSCL.APV.BD.Secuencia::APV.IdCargaMasiva", 1L);
	
			ArrayList<CargaArchivo> RegCargaArchivo = new ArrayList<>();
	
			CargaArchivo ArchivoCargar = new CargaArchivo();
			ArchivoCargar.IdEmpresa = in.p_IdEmpresa;
			ArchivoCargar.IdCargaMasiva = idsCargaArchivo.get(0);
			ArchivoCargar.NombreCargaMasiva = in.p_NombreCarga;
			ArchivoCargar.CantidadRegistros = 0L;
			ArchivoCargar.IdUsuario = in.p_IdUsuario;
			ArchivoCargar.IdTipoCarga = 5L;
			ArchivoCargar.Descripcion = in.p_Origen;
			ArchivoCargar.FechaCarga = fechaSistema;
			ArchivoCargar.IdEstado = "VIG"; 
			
			RegCargaArchivo.add(ArchivoCargar);
	
			int[] res = sql.batch(con, SQL_INSERT_CARGA_ARCHIVO, RegCargaArchivo, CargaArchivo.class);
			if(res!=null && res.length>0 && res[0]==1){
				
				/***************************************************/
				
				ArrayList<EntidadPersona> asdf = new ArrayList<>();
		
				EntidadPersona entidadPersona = new EntidadPersona();
				entidadPersona.p_IdEmpresa = in.p_IdEmpresa;
				entidadPersona.p_IdModulo = in.p_IdModulo;
				entidadPersona.p_IdUsuario = in.p_IdUsuario;
				entidadPersona.p_NombreCarga = in.p_NombreCarga;
				entidadPersona.p_Origen = in.p_Origen;
				entidadPersona.p_FechaCarga = in.p_FechaCarga;
				entidadPersona.p_Archivo = in.p_Archivo; 
				
				asdf.add(entidadPersona);
				
				sql.batch(con, SQL_INSERT_CARGA_XML, asdf, EntidadPersona.class);
				
				/***************************************************/
			}
		}
		catch(Exception error){
			msg = new MensajeSalida();
			msg.id = 1L;
			msg.tipo = "ERROR";
			msg.codigo = "CARGA_HISTORICO_APV_ERROR";
			msg.descripcion = error.getMessage();
			salida.P_OUT_MENSAJES.add(msg);
			salida.P_OUT_ERRORESCONTADOR = (long)salida.P_OUT_MENSAJES.size(); 
			//error.printStackTrace();
		}

		con.commit(); 
		return salida;
	}

	/********************
	 * Método Principal *
	 ********************/

	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getQAConnection()) {
			CargaRegistroHistoricoTraspaso CRHT = new CargaRegistroHistoricoTraspaso();
			try {
				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
				String json = "{\"p_IdEmpresa\":1,\"p_IdModulo\":\"APV\",\"p_IdUsuario\":51,\"p_NombreCarga\":\"TH_05193_01033_20140108163904.xml\",\"p_Origen\":\"Carga Registro Historico APV\",\"p_FechaCarga\":\"2019-03-05T03:00:00.000Z\",\"p_Archivo\":\"Depositos.txt\"}";
				//String json = "";
				
				Parametros_Entrada Objin = gson.fromJson(json, Parametros_Entrada.class);
				
				try {
					ParametrosSalidaServicio ObjOut = CRHT.logica(con, Objin);
					con.commit();
					//System.out.println(ObjOut.toString());
				} catch (Exception er) {
					er.printStackTrace();
					con.rollback();
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
			System.out.println("== END ==");
		};
	}
}