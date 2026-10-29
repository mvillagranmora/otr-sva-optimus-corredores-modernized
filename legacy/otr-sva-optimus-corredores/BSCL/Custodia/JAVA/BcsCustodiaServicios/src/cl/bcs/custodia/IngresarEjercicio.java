package cl.bcs.custodia;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class IngresarEjercicio implements JavaCall{
	
	private final String SELECT_EJERCICIO = loadResourceToString("cl/bcs/custodia/sql/IngresarEjercicio_SelectEjercicio.sql");
	private final String INSERT_EJERCICIO = loadResourceToString("cl/bcs/custodia/sql/IngresarEjercicio_InsertEjercicio.sql");
	private final String UPDATE_EJERCICIO = loadResourceToString("cl/bcs/custodia/sql/IngresarEjercicio_UpdateEjercicio.sql");
	
	public static class Entrada_IngresarEjercicio{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public Long p_IdPais;
		public List<Ejercicio> p_Ejercicios;		
		
		public Entrada_IngresarEjercicio() {
			p_Ejercicios = new ArrayList<Ejercicio>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}		
	}
	
	public static class Ejercicio{
		public Long IdEjercicio;
		public Long IdCartera;
		public String Bloqueado;
		public Long ContratosEjercidos;
		public Long ContratosSorteados;		
		public Timestamp FechaHora;
		public String IdEstado;
		public String IdEstadoCCLV;
		public String DescripcionEstadoCCLV;
		public Long IdCliente;
		public Long IdPortafolio;
		public Long IdEmpresa;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Salida_IngresarEjercicio{
		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;
		public List<Ejercicio>		P_OUT_EJERCICIOS;
		
		public Salida_IngresarEjercicio() {
			P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			P_OUT_ERRORESCONTADOR = 0L;
			P_OUT_EJERCICIOS = new ArrayList<Ejercicio>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}		
	}
	
	/* TODO: CAMBIAR A LA VISTA */
	private Ejercicio obtenerEjercicio(Connection con, Long idCartera) throws SQLException {
		List<Ejercicio> ejercicio = new ArrayList<Ejercicio>();
		SQL sql = new SQL();
		HashMap<String, Object> params = new HashMap<String, Object>();		
		params.put("p_IdCartera", idCartera);
		params.put("p_IdEstado", "GUA");		
		ejercicio = sql.query(con, SELECT_EJERCICIO, params, Ejercicio.class);
		if(ejercicio.isEmpty())
			return null;
		return ejercicio.get(0);
	}
	
	private Ejercicio ingresarEjercicio(Connection con, Ejercicio ejercicio) throws SQLException {
		System.out.println("INSERTANDO NUEVO EJERCICIO");
		SQL sql = new SQL();
		List<Ejercicio> ejercicioInsert = new ArrayList<Ejercicio>();
		List<Long> secuenciaIdEjercicio = Utils.getSecuencia(con, "BSCL.Custodia.BD.Secuencias::OpcionesEjercicioBloqueo",	new Long(1L));
		ejercicio.IdEjercicio = secuenciaIdEjercicio.get(0);
		ejercicio.FechaHora = new Timestamp(System.currentTimeMillis());
		ejercicioInsert.add(ejercicio);
		sql.batch(con, INSERT_EJERCICIO, ejercicioInsert, Ejercicio.class);
		return ejercicioInsert.get(0);
	}
	
	private Ejercicio actualizarEjercicio(Connection con, Ejercicio ejercicio) throws SQLException {
		System.out.println("ACTUALIZANDO EJERCICIO");
		SQL sql = new SQL();
		List<Ejercicio> ejercicioUpdate = new ArrayList<Ejercicio>();
		ejercicio.IdEstado = "INA";
		ejercicioUpdate.add(ejercicio);
		sql.batch(con, UPDATE_EJERCICIO, ejercicioUpdate, Ejercicio.class);
		return ejercicioUpdate.get(0);
	}
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		Entrada_IngresarEjercicio in=gson.fromJson(jin.toString(),Entrada_IngresarEjercicio.class);
		Salida_IngresarEjercicio out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	/* ObtenerPrecioBase para ACN, MILA, para Dolar, indicador TipoCambioIndice */
	public Salida_IngresarEjercicio logica(Connection con, Entrada_IngresarEjercicio entrada) throws Exception{		
		Salida_IngresarEjercicio salidaIngresarEjercicio = new Salida_IngresarEjercicio();
		HashMap<String, String> mapMsg = new HashMap<String, String>();		
		for(Ejercicio ejercicio: entrada.p_Ejercicios) {
			Ejercicio ejer = obtenerEjercicio(con, ejercicio.IdCartera);			
			if(ejer == null) {				
				Ejercicio ejercicioNuevo = ingresarEjercicio(con, ejercicio);
				mapMsg.put("IdEjercicio", ejercicioNuevo.IdEjercicio.toString());
				salidaIngresarEjercicio.P_OUT_EJERCICIOS.add(ejercicioNuevo);
				salidaIngresarEjercicio.P_OUT_MENSAJES.add(new MensajeSalida(ejercicioNuevo.IdEjercicio,MensajeSalida.MENSAJE_OK,"EJERCICIO INGRESADO",mapMsg,con,entrada.p_IdEmpresa));
			}
			else {
				Ejercicio ejercicioActualizado = actualizarEjercicio(con, ejer);				
				mapMsg.put("IdEjercicio", ejercicioActualizado.IdEjercicio.toString());
				salidaIngresarEjercicio.P_OUT_MENSAJES.add(new MensajeSalida(ejercicioActualizado.IdEjercicio,MensajeSalida.MENSAJE_OK,"EJERCICIO ACTUALIZADO",mapMsg,con,entrada.p_IdEmpresa));
				salidaIngresarEjercicio.P_OUT_EJERCICIOS.add(ejercicioActualizado);	
				
				ejer.IdEstado = ejercicio.IdEstado;
				Ejercicio ejercicioNuevo = ingresarEjercicio(con, ejercicio);
				mapMsg.put("IdEjercicio", ejercicioNuevo.IdEjercicio.toString());
				salidaIngresarEjercicio.P_OUT_EJERCICIOS.add(ejercicioNuevo);
				salidaIngresarEjercicio.P_OUT_MENSAJES.add(new MensajeSalida(ejercicioNuevo.IdEjercicio,MensajeSalida.MENSAJE_OK,"EJERCICIO INGRESADO",mapMsg,con,entrada.p_IdEmpresa));
			}
		}
	
		return salidaIngresarEjercicio;		
	}
	
	
	
	public static void main(String[] args) { 
		System.out.println("=== BEGIN TEST ===");
		
		try (Connection con =  PlataformaUtils.getDESAConnection()){
			Gson gson = Utils.getGsonBuilder();			
			
			String json = "{\r\n" + 
					"  \"p_IdEmpresa\": 1,\r\n" + 
					"  \"p_IdUsuario\": 21,\r\n" + 
					"  \"p_IdPais\": 44,\r\n" + 
					"  \"p_Ejercicios\": [\r\n" + 
					"    {\r\n" + 
					"      \"IdCartera\": 45465,\r\n" + 
					"      \"Bloqueado\": \"N\",\r\n" + 
					"      \"ContratosEjercidos\": 0,\r\n" + 
					"      \"ContratosSorteados\": 0,\r\n" + 					
					"      \"IdEstado\": \"GUA\",\r\n" + 
					"      \"IdEstadoCCLV\": \"\",\r\n" + 
					"      \"DescripcionEstadoCCLV\": \"\",\r\n" + 
					"      \"IdCliente\": 123569,\r\n" + 
					"      \"IdPortafolio\": 27499,\r\n" + 
					"      \"IdEmpresa\": 1\r\n" + 
					"    }\r\n" + 
					"  ]\r\n" + 
					"}";
			
			Entrada_IngresarEjercicio entrada = gson.fromJson(json, Entrada_IngresarEjercicio.class);
			IngresarEjercicio demo = new IngresarEjercicio();
			long init = System.currentTimeMillis();
			Salida_IngresarEjercicio salida = demo.logica(con, entrada);			
			System.out.println(String.format("TERMINO EN: %d", (System.currentTimeMillis()-init)/1000));			
			System.out.println(salida);			
			con.rollback();
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		System.out.println("=== END TEST ===");
	}

}
