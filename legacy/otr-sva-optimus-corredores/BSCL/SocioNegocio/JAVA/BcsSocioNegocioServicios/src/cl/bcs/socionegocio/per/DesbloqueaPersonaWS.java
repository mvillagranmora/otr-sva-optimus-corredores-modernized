package cl.bcs.socionegocio.per;

import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;

import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class DesbloqueaPersonaWS implements JavaCall{
	public String SQL_CALL_DESBLOQUEA_PERSONA = loadResourceToString("cl/bcs/socionegocio/per/sql/DesbloqueaPersona_CallModificarEstadoPersona.sql");
	public String SQL_CONSULTAR_ID_PERSONA = loadResourceToString("cl/bcs/socionegocio/per/sql/DesbloqueaPersonaWS_SelectIdPersonaPorRut.sql");


	
	
	public static class EntradaDesbloqueaPersonaWS{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglaNegocio;
		public String p_DocumentoIdentidad;
		public String p_IdEstado;
		public String p_IdTipo;
		public String p_Comentario;
		
	}
	
	
	public static class SalidaDesbloqueaPersonaWS{
		public long P_OUT_REFID;
		public long P_OUT_ADVERTENCIASCONTADOR;
		public List<AdvertenciasDetalle> P_OUT_ADVERTENCIASDETALLE;
		public long P_OUT_ERRORESCONTADOR;
		public List<ErroresDetalle> P_OUT_ERRORESDETALLE;
		
		public List<MensajeSalida> P_OUT_ADVERTENCIASDETALLEWS;
		public List<MensajeSalida> P_OUT_ERRORESDETALLEWS;
		public List<MensajeSalida> P_OUT_SUCCESSDETALLEWS;
		
		public SalidaDesbloqueaPersonaWS() {
			P_OUT_ERRORESDETALLEWS = new ArrayList<>();
			P_OUT_ADVERTENCIASDETALLEWS = new ArrayList<>();
			P_OUT_SUCCESSDETALLEWS = new ArrayList<>();
			P_OUT_ERRORESDETALLE = new ArrayList<>();
			P_OUT_ADVERTENCIASDETALLE = new ArrayList<>();
		}
		
	}
	public static class DatosPersona{
		public Long IdPersona;
	}
	
	public static class AdvertenciasDetalle {
		public String Codigo;
		public String RequiereAutorizacion;
		public String Descripcion;
	}
	
	public static class ErroresDetalle {
		public String Codigo;
		public String Descripcion;
	}
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = new Gson();
		EntradaDesbloqueaPersonaWS in = gson.fromJson(jin.toString(), EntradaDesbloqueaPersonaWS.class);
		SalidaDesbloqueaPersonaWS out = logica(con, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaDesbloqueaPersonaWS logica(Connection con, EntradaDesbloqueaPersonaWS entrada) throws Exception{
		SQL sql = new SQL();
		SalidaDesbloqueaPersonaWS out = new SalidaDesbloqueaPersonaWS();
		
		Long idPersona = ObtieneIdPersona(con, entrada.p_DocumentoIdentidad);
		if(idPersona > 0) {
			
			HashMap<String, Object> parametrosEntrada = new HashMap<String, Object>();
			
			if(entrada.p_IdEmpresa != 0) {
				parametrosEntrada.put("p_IdEmpresa", entrada.p_IdEmpresa);
			}
			else {
				parametrosEntrada.put("p_IdEmpresa", 1L);
			}
			parametrosEntrada.put("p_IdUsuario", entrada.p_IdUsuario);
			parametrosEntrada.put("p_IdModulo", entrada.p_IdModulo);
			parametrosEntrada.put("p_UsaReglasNegocio", entrada.p_UsaReglaNegocio);
			parametrosEntrada.put("p_IdPersona", idPersona);
			parametrosEntrada.put("p_IdEstado", entrada.p_IdEstado);
			parametrosEntrada.put("p_IdTipo", entrada.p_IdTipo);
			parametrosEntrada.put("p_Fecha", obtenerFechaActual());
			parametrosEntrada.put("p_Comentario", entrada.p_Comentario);
			
			HashMap<String, Class> parametrosSalida = new HashMap<String, Class>();
			parametrosSalida.put("P_OUT_REFID", Long.class);
		    parametrosSalida.put("P_OUT_ADVERTENCIASCONTADOR", Long.class);
		    parametrosSalida.put("P_OUT_ADVERTENCIASDETALLE", AdvertenciasDetalle.class);
		    parametrosSalida.put("P_OUT_ERRORESCONTADOR", Long.class);
		    parametrosSalida.put("P_OUT_ERRORESDETALLE", ErroresDetalle.class);

		    
		    HashMap<String, Object> respuestaDesbloqueaPersona = sql.call(con, SQL_CALL_DESBLOQUEA_PERSONA, parametrosEntrada, parametrosSalida);
		    
		    out.P_OUT_ERRORESCONTADOR += (Long) respuestaDesbloqueaPersona.get("P_OUT_ERRORESCONTADOR");
		    out.P_OUT_ADVERTENCIASCONTADOR = (Long) respuestaDesbloqueaPersona.get("P_OUT_ADVERTENCIASCONTADOR");
		    if(out.P_OUT_ERRORESCONTADOR > 0 || out.P_OUT_ADVERTENCIASCONTADOR > 0) {
		    	out.P_OUT_ADVERTENCIASDETALLE = (List<AdvertenciasDetalle>)  respuestaDesbloqueaPersona.get("P_OUT_ADVERTENCIASDETALLE");
			    for (AdvertenciasDetalle advertencia: out.P_OUT_ADVERTENCIASDETALLE) {
					  out.P_OUT_ADVERTENCIASDETALLEWS.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ADVERTENCIA, advertencia.Codigo, advertencia.Descripcion));
				}
			    out.P_OUT_ERRORESDETALLE = (List<ErroresDetalle>)  respuestaDesbloqueaPersona.get("P_OUT_ERRORESDETALLE");
			    for (ErroresDetalle nombre: out.P_OUT_ERRORESDETALLE) {
					  out.P_OUT_ERRORESDETALLEWS.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, nombre.Codigo, nombre.Descripcion));
				}
		    }
		    else {
		    	out.P_OUT_SUCCESSDETALLEWS.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_OK, "DESBLOQUEO_OK", "PERSONA DESBLOQUEDA"));
		    }
		    	
			
			return out;
		}
		else
		{
			out.P_OUT_ERRORESCONTADOR = 1L;
			out.P_OUT_ERRORESDETALLEWS.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "PERSONA_NO_EXISTE", "PERSONA INGRESADA NO EXISTE"));
			
			return out;
		}
		
		
	}
	public Long ObtieneIdPersona(Connection con,String rut) {
		SQL sql = new SQL();
		try {
			
			HashMap<String, Object> consultaIdPersona = new HashMap<String, Object>();
			consultaIdPersona.put("p_DocumentoIdentidad", rut);
			List<DatosPersona> datosPersona = sql.query(con, SQL_CONSULTAR_ID_PERSONA, consultaIdPersona, DatosPersona.class);
			if(datosPersona.size() > 0){
				return datosPersona.get(0).IdPersona;
			}
			else
			{
				return 0L;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return 0L;
		}
		
		
	}
	private String obtenerFechaActual() throws Exception {
		SimpleDateFormat dtf = new SimpleDateFormat("yyyy/MM/dd");
        Calendar calendar = Calendar.getInstance();

        Date dateObj = calendar.getTime();
        String fechaSistema = dtf.format(dateObj);
        
		return fechaSistema;

	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			
			Gson gson = Utils.getGsonBuilder();
			String json = "{p_IdEmpresa : 1,\r\n"
					+ "	p_IdUsuario : 0,\r\n"
					+ "	p_IdModulo : \"SOC\",\r\n"
					+ "	p_UsaReglaNegocio : \"N\",\r\n"
					+ "	p_DocumentoIdentidad : \"19248592-4\",\r\n"
					+ "	p_IdEstado : \"ACT\",\r\n"
					+ "	p_IdTipo : \"DESBACTUALIZADOC\",\r\n"
					+ "	p_Comentario : \"23434\"}";

			EntradaDesbloqueaPersonaWS Objin = gson.fromJson(json,
					EntradaDesbloqueaPersonaWS.class);
			DesbloqueaPersonaWS demo = new DesbloqueaPersonaWS();
			System.out.println("ENTRADA:" + Objin.toString());
			
			

			try {
				SalidaDesbloqueaPersonaWS ObjOut = demo.logica(con, Objin);
				System.out.println(ObjOut);
				System.out.println(ObjOut.P_OUT_ERRORESCONTADOR);
				System.out.println(ObjOut.P_OUT_ERRORESDETALLEWS);
				
				System.out.println(ObjOut.P_OUT_SUCCESSDETALLEWS);
				System.out.println(ObjOut.P_OUT_ADVERTENCIASDETALLEWS);
				System.out.println(ObjOut.P_OUT_ADVERTENCIASCONTADOR);

				con.rollback();
				//con.commit();
			} catch (Exception er) {
				er.printStackTrace();
				// con.commit();
			}

			con.close();
			
			
		} catch (Exception e) {
			System.out.println(e);
		}
		System.out.println("== END ==");
	}

}
