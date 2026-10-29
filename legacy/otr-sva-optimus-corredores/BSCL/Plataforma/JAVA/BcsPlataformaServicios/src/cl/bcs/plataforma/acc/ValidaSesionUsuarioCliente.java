package cl.bcs.plataforma.acc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.acc.ObtenerTokenOptimusClientes.EntradaObtenerTokenOptimusClientes;
import cl.bcs.plataforma.acc.ObtenerTokenOptimusClientes.SalidaObtenerTokenOptimusClientes;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.SalidaServicios;
import cl.bcs.plataforma.pla.utils.SalidaServicios.AdvertenciaDetalle;
import cl.bcs.plataforma.pla.utils.SalidaServicios.ErrorDetalle;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ValidaSesionUsuarioCliente implements JavaCall{
	
	private String SQL_CALL_ValidaSesionUsuario = loadResourceToString("cl/bcs/plataforma/acc/sql/ValidaSesionUsuarioCliente_Call_ValidaSesionUsuario.sql");
	
	public class EntradaValidaSesionUsuarioCliente{
		public String p_Token;
		public String p_NuevoToken = "N";
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}	
		
	}
	public static class ValoresDefectoSistema{
		public String IdValorDefectoSistema;
		public String Nombre;
		public String Valor;
		public String IdEmpresa;
		public String IdEstado;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}	
	}
	public static class SalidaValidaSesionUsuarioCliente extends SalidaServicios{
		public Long P_OUT_IDEMPRESA;	
		public Long P_OUT_IDUSUARIO;	
		public String P_OUT_NOMBREEMPRESA;
		public String P_OUT_NOMBREUSUARIO;
		public String P_OUT_CODIGOMENUUSUARIO;
		public String P_OUT_DEBECAMBIARCLAVE;	
		public Long P_OUT_IDSOCIODENEGOCIO;	
		public Long P_OUT_IDPERSONACORREDORA;	
		public String P_OUT_IDMONEDASISTEMA;	
		public Long P_OUT_IDPAISSISTEMA;
		public String P_OUT_FECHASISTEMA;
		public String P_OUT_BOLSADEFECTOSISTEMA;
		public List<ValoresDefectoSistema>P_OUT_VALORESDEFECTOSISTEMA;
		public Long P_OUT_IDEMPLEADO;	
		public Long P_OUT_IDCLIENTE;
		public Long P_OUT_IDPERSONA;
		public String P_OUT_TOKEN = "";
		
		public SalidaValidaSesionUsuarioCliente(){
			super();
		}

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}	
	}
	
	public SalidaValidaSesionUsuarioCliente logica(Connection conn, EntradaValidaSesionUsuarioCliente entrada) {
		SalidaValidaSesionUsuarioCliente salida = new SalidaValidaSesionUsuarioCliente();
		SQL sql = new SQL();
		
		HashMap<String, Object> paramsIn = new HashMap<>();
		paramsIn.put("p_Token", entrada.p_Token);
						
		HashMap<String, Class> paramsOut = new HashMap<>();
		paramsOut.put("p_out_IdEmpresa", Long.class);
		paramsOut.put("p_out_IdUsuario", Long.class);
		
		paramsOut.put("p_out_NombreEmpresa", String.class);
		paramsOut.put("p_out_NombreUsuario", String.class);
		paramsOut.put("p_out_CodigoMenuUsuario", String.class);
		paramsOut.put("p_out_DebeCambiarClave", String.class);
		paramsOut.put("p_out_IdSocioDeNegocio", Long.class);
		paramsOut.put("p_out_IdPersonaCorredora", Long.class);
		paramsOut.put("p_out_IdMonedaSistema", String.class);
		paramsOut.put("p_out_IdPaisSistema", Long.class);
		paramsOut.put("p_out_FechaSistema", String.class);
		paramsOut.put("p_out_BolsaDefectoSistema", String.class);
		paramsOut.put("p_out_ValoresDefectoSistema", ValoresDefectoSistema.class);
		paramsOut.put("p_out_IdEmpleado", Long.class);
		paramsOut.put("p_out_IdCliente", Long.class);
		paramsOut.put("p_out_IdPersona", Long.class);
		
		paramsOut.put("p_out_AdvertenciasContador", Long.class);
		paramsOut.put("p_out_AdvertenciasDetalle", SalidaValidaSesionUsuarioCliente.AdvertenciaDetalle.class);
		paramsOut.put("p_out_ErroresContador", Long.class);
		paramsOut.put("p_out_ErroresDetalle", SalidaValidaSesionUsuarioCliente.ErrorDetalle.class);

		try {
			HashMap<String, Object> resultSP = sql.call(conn, SQL_CALL_ValidaSesionUsuario, paramsIn, paramsOut);
			if(resultSP.containsKey("p_out_ErroresContador")) {
				salida.P_OUT_IDEMPRESA = (Long) resultSP.get("p_out_IdEmpresa");
				salida.P_OUT_IDUSUARIO = (Long) resultSP.get("p_out_IdUsuario");

				salida.P_OUT_NOMBREEMPRESA = (String) resultSP.get("p_out_NombreEmpresa");
				salida.P_OUT_NOMBREUSUARIO = (String) resultSP.get("p_out_NombreUsuario");
				salida.P_OUT_CODIGOMENUUSUARIO = (String) resultSP.get("p_out_CodigoMenuUsuario");
				salida.P_OUT_DEBECAMBIARCLAVE = (String) resultSP.get("p_out_DebeCambiarClave");
				salida.P_OUT_IDSOCIODENEGOCIO = (Long) resultSP.get("p_out_IdSocioDeNegocio");
				salida.P_OUT_IDPERSONACORREDORA = (Long) resultSP.get("p_out_IdPersonaCorredora");
				salida.P_OUT_IDMONEDASISTEMA = (String) resultSP.get("p_out_IdMonedaSistema");
				salida.P_OUT_IDPAISSISTEMA = (Long) resultSP.get("p_out_IdPaisSistema");
				salida.P_OUT_FECHASISTEMA= (String) resultSP.get("p_out_FechaSistema");
				salida.P_OUT_BOLSADEFECTOSISTEMA = (String) resultSP.get("p_out_BolsaDefectoSistema");
				salida.P_OUT_VALORESDEFECTOSISTEMA = (List<ValoresDefectoSistema>) resultSP.get("p_out_ValoresDefectoSistema");
				salida.P_OUT_IDEMPLEADO = (Long) resultSP.get("p_out_IdEmpleado");
				salida.P_OUT_IDCLIENTE = (Long) resultSP.get("p_out_IdCliente");
				salida.P_OUT_IDPERSONA = (Long) resultSP.get("p_out_IdPersona");

				salida.P_OUT_ADVERTENCIASCONTADOR = (Long) resultSP.get("p_out_AdvertenciasContador");
				salida.P_OUT_ADVERTENCIASDETALLE = (List<AdvertenciaDetalle>) resultSP.get("p_out_AdvertenciasDetalle");
				salida.P_OUT_ERRORESCONTADOR = (Long) resultSP.get("p_out_ErroresContador");
				salida.P_OUT_ERRORESDETALLE = (List<ErrorDetalle>) resultSP.get("p_out_ErroresDetalle");
				
				if("S".equals(entrada.p_NuevoToken)) {
					ObtenerTokenOptimusClientes newToken = new ObtenerTokenOptimusClientes();
					EntradaObtenerTokenOptimusClientes entradaNewToken = new EntradaObtenerTokenOptimusClientes();
					entradaNewToken.p_CodigoUsuario = salida.P_OUT_NOMBREUSUARIO;
					entradaNewToken.p_IdEmpresa = salida.P_OUT_IDEMPRESA;
					SalidaObtenerTokenOptimusClientes salidaNewToken = newToken.logica(conn, entradaNewToken);
					salida.P_OUT_TOKEN= (salida.P_OUT_ERRORESCONTADOR==0L) ? salidaNewToken.P_OUT_TOKEN : "";
				}
				
			}	
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return salida;
	}

	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaValidaSesionUsuarioCliente entrada = gson.fromJson(jin.toString(), EntradaValidaSesionUsuarioCliente.class);
		SalidaValidaSesionUsuarioCliente salida = logica(conn, entrada);
		JsonObject dataout = new JsonParser().parse(gson.toJson(salida)).getAsJsonObject();
		return dataout;
	}
	
	public static void main(String[] args) {
		try (Connection con = PlataformaUtils.getPropertiesConnection("presura")) {
			try {
				Gson gson = Utils.getGsonBuilder();	
				String json = "{\"p_Token\": \"MTo3MDMxNTc3LTc6MjAyMC0wNy0wMSAxMjo0NjoxMS40NTcwMDAwOjI5NzZGQzVFOTU5Rjk5MUNFMTAwMDAwMEExRUUwNTU2\", \"p_NuevoToken\": \"N\"}";
				//MTo3MDMxNTc3LTc6MjAyMC0wNi0zMCAxNzo0MzoxNy4wODgwMDAwOkIxRkVGQjVFNzM5MjhBMURFMTAwMDAwMEExRUUwNTU2
				EntradaValidaSesionUsuarioCliente Objin = gson.fromJson(json, EntradaValidaSesionUsuarioCliente.class);								
				ValidaSesionUsuarioCliente  kk = new ValidaSesionUsuarioCliente();
				SalidaValidaSesionUsuarioCliente salida = kk.logica(con, Objin);
				System.out.println(salida);
				System.out.println(salida.P_OUT_TOKEN);
				con.commit();
				
				System.out.println("---- FIN Agregar Agenda ------");
				
		    	}catch(Exception er) {
					er.printStackTrace();	
					con.rollback();
				}
		} catch (Exception e) {
			e.printStackTrace();			
		}
	}

}
