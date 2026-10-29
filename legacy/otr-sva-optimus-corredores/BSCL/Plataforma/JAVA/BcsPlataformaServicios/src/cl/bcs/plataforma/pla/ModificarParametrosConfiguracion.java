package cl.bcs.plataforma.pla;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ModificarParametrosConfiguracion implements JavaCall{	
	private final Log log = new Log("PLA.ModificarParametrosConfiguracion");
	//QUERIES UPDATE
	private final String SQL_CuentaInversion_Update = loadResourceToString("cl/bcs/plataforma/pla/sql/parametrosconfiguracion/CuentaInversion_Update.sql");
	private final String SQL_Custodia_Update = loadResourceToString("cl/bcs/plataforma/pla/sql/parametrosconfiguracion/Custodia_Update.sql");
	private final String SQL_Facturacion_Update = loadResourceToString("cl/bcs/plataforma/pla/sql/parametrosconfiguracion/Facturacion_Update.sql");
	private final String SQL_FondosMutuos_Update = loadResourceToString("cl/bcs/plataforma/pla/sql/parametrosconfiguracion/FondosMutuos_Update.sql");
	private final String SQL_Mesa_Update = loadResourceToString("cl/bcs/plataforma/pla/sql/parametrosconfiguracion/Mesa_Update.sql");
	private final String SQL_Normativa_Update = loadResourceToString("cl/bcs/plataforma/pla/sql/parametrosconfiguracion/Normativa_Update.sql");
	private final String SQL_NormativaParametro_Update = loadResourceToString("cl/bcs/plataforma/pla/sql/parametrosconfiguracion/NormativaParametro_Update.sql");
	private final String SQL_Ordenes_Update = loadResourceToString("cl/bcs/plataforma/pla/sql/parametrosconfiguracion/Ordenes_Update.sql");
	private final String SQL_Plataforma_Update = loadResourceToString("cl/bcs/plataforma/pla/sql/parametrosconfiguracion/Plataforma_Update.sql");
	private final String SQL_SocioNegocio_Update = loadResourceToString("cl/bcs/plataforma/pla/sql/parametrosconfiguracion/SocioNegocio_Update.sql");
	private final String SQL_Tesoreria_Update = loadResourceToString("cl/bcs/plataforma/pla/sql/parametrosconfiguracion/Tesoreria_Update.sql");
	private final String SQL_Producto_Update = loadResourceToString("cl/bcs/plataforma/pla/sql/parametrosconfiguracion/Producto_Update.sql");
	
	private final String SQL_Contabilidad_Update = loadResourceToString("cl/bcs/plataforma/pla/sql/parametrosconfiguracion/Contabilidad_Update.sql");
	private final String SQL_Limite_Update = loadResourceToString("cl/bcs/plataforma/pla/sql/parametrosconfiguracion/Limite_Update.sql");
	
	//QUERYS SELECT
	private final String SQL_CuentaInversion_Select = loadResourceToString("cl/bcs/plataforma/pla/sql/parametrosconfiguracion/CuentaInversion_Select.sql");
	private final String SQL_Custodia_Select = loadResourceToString("cl/bcs/plataforma/pla/sql/parametrosconfiguracion/Custodia_Select.sql");
	private final String SQL_Facturacion_Select = loadResourceToString("cl/bcs/plataforma/pla/sql/parametrosconfiguracion/Facturacion_Select.sql");
	private final String SQL_FondosMutuos_Select = loadResourceToString("cl/bcs/plataforma/pla/sql/parametrosconfiguracion/FondosMutuos_Select.sql");
	private final String SQL_Mesa_Select = loadResourceToString("cl/bcs/plataforma/pla/sql/parametrosconfiguracion/Mesa_Select.sql");
	private final String SQL_Normativa_Select = loadResourceToString("cl/bcs/plataforma/pla/sql/parametrosconfiguracion/Normativa_Select.sql");
	private final String SQL_NormativaParametro_Select = loadResourceToString("cl/bcs/plataforma/pla/sql/parametrosconfiguracion/NormativaParametro_Select.sql");
	private final String SQL_Ordenes_Select = loadResourceToString("cl/bcs/plataforma/pla/sql/parametrosconfiguracion/Ordenes_Select.sql");
	private final String SQL_Plataforma_Select = loadResourceToString("cl/bcs/plataforma/pla/sql/parametrosconfiguracion/Plataforma_Select.sql");
	private final String SQL_SocioNegocio_Select = loadResourceToString("cl/bcs/plataforma/pla/sql/parametrosconfiguracion/SocioNegocio_Select.sql");
	private final String SQL_Tesoreria_Select = loadResourceToString("cl/bcs/plataforma/pla/sql/parametrosconfiguracion/Tesoreria_Select.sql");
	private final String SQL_Producto_Select = loadResourceToString("cl/bcs/plataforma/pla/sql/parametrosconfiguracion/Producto_Select.sql");
	
	private final String SQL_Contabilidad_Select = loadResourceToString("cl/bcs/plataforma/pla/sql/parametrosconfiguracion/Contabilidad_Select.sql");	
	private final String SQL_Limite_Select = loadResourceToString("cl/bcs/plataforma/pla/sql/parametrosconfiguracion/Limite_Select.sql");	
	
	
	/*
	 * Clases IN/OUT
	 */
	public static class EntradaServicio {
		public Long						p_IdEmpresa;
		public Long						p_IdUsuario;
		public String					p_IdModulo;
		public String					p_SimularOperacion;
		public String					p_Modulo;
		public String					p_Codigo;
		public String					p_Valor;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class UpdateConfiguracion {
		public Long						p_IdEmpresa;
		public String					p_Codigo;
		public String					p_Valor;
		public String					p_Usuario;
	}
	
	public static class SelectConfiguracion {
		public String					Valor;
	}
	
	public static class SalidaParametrosConfiguracion{
		public String					p_Salida;
		public SalidaParametrosConfiguracion(String resultado){
			p_Salida = resultado;
		}
	}

	public static class ServicioSalida {
		public Long								P_OUT_ERRORESCONTADOR;
		public Long								P_OUT_ADVERTENCIASCONTADOR;
		public List<ServicioSalidaAdvertencia>	P_OUT_ADVERTENCIADETALLE;
		public List<ServicioSalidaError>		P_OUT_ERRORESDETALLE;
		public List<SalidaParametrosConfiguracion>p_Respuesta;

		public ServicioSalida() {
			P_OUT_ADVERTENCIADETALLE = new ArrayList<>();
			P_OUT_ERRORESDETALLE = new ArrayList<>();
			p_Respuesta = new ArrayList<>();		
		}
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class ServicioSalidaAdvertencia {
		public String	Codigo;
		public String	RequiereAutorizacion;
		public String	Descripcion;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class ServicioSalidaError {
		public String	Codigo;
		public String	RequiereAutorizacion;
		public String	Descripcion;

		public ServicioSalidaError() {
		}

		public ServicioSalidaError(String codigo, String mensaje) {
			this.Codigo = codigo;
			this.Descripcion = mensaje;
		}

		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	/**********************
	 * Transporte
	 **********************/
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {				
		Gson gson = new Gson();
		EntradaServicio in = gson.fromJson(jin.toString(), EntradaServicio.class);
		ServicioSalida out = logica(con, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public ServicioSalida logica(Connection con, EntradaServicio entrada) throws Exception {
		ServicioSalida salida = new ServicioSalida();
		salida.P_OUT_ERRORESCONTADOR = new Long(0);
		salida.P_OUT_ADVERTENCIASCONTADOR = 0L;
		
		SQL sql = new SQL();
		String codigoUsuario = Utils.getCodigoUsuario(con, entrada.p_IdEmpresa, entrada.p_IdUsuario);
		
		UpdateConfiguracion data = new UpdateConfiguracion();
		data.p_IdEmpresa = entrada.p_IdEmpresa;
		data.p_Codigo = entrada.p_Codigo;
		data.p_Valor = entrada.p_Valor;
		data.p_Usuario = codigoUsuario;
		
		List<UpdateConfiguracion> lista = new ArrayList<UpdateConfiguracion>();
		lista.add(data);

		String update_sql = "";
		String select_sql = "";
		switch(entrada.p_Modulo){
			case "TESORERIA":
				update_sql = SQL_Tesoreria_Update;
				select_sql = SQL_Tesoreria_Select;
				break;
			case "FFMM":
				update_sql = SQL_FondosMutuos_Update;
				select_sql = SQL_FondosMutuos_Select;
				break;
			case "MESA":
				update_sql = SQL_Mesa_Update;
				select_sql = SQL_Mesa_Select;
				break;
			case "ORDENES":
				update_sql = SQL_Ordenes_Update;
				select_sql = SQL_Ordenes_Select;
				break;
			case "CTAINV":
				update_sql = SQL_CuentaInversion_Update;
				select_sql = SQL_CuentaInversion_Select;
				break;
			case "NORMATIVA":
				update_sql = SQL_Normativa_Update;
				select_sql = SQL_Normativa_Select;
				break;
			case "NORMATIVAPARAMETRO":
				update_sql = SQL_NormativaParametro_Update;
				select_sql = SQL_NormativaParametro_Select;
				break;
			case "PLATAFORMA":
				update_sql = SQL_Plataforma_Update;
				select_sql = SQL_Plataforma_Select;
				break;
			case "SOCIONEGOCIO":
				update_sql = SQL_SocioNegocio_Update;
				select_sql = SQL_SocioNegocio_Select;
				break;
			case "FACTURACION":
				update_sql = SQL_Facturacion_Update;
				select_sql = SQL_Facturacion_Select;
				break;
			case "CUSTODIA":
				update_sql = SQL_Custodia_Update;
				select_sql = SQL_Custodia_Select;
				break;
			case "PRODUCTO":
				update_sql = SQL_Producto_Update;
				select_sql = SQL_Producto_Select;
				break;	

			case "CONTABILIDAD":
				update_sql = SQL_Contabilidad_Update;
				select_sql = SQL_Contabilidad_Select;
				break;	
				
			case "LIMITES":
				update_sql = SQL_Limite_Update;
				select_sql = SQL_Limite_Select;
				break;	
				
				
				
		}
		if(!update_sql.equals("")){
			HashMap<String, Object> params = new HashMap<>();
			params.put("p_IdEmpresa", data.p_IdEmpresa);
			params.put("p_Codigo", data.p_Codigo);
			List<SelectConfiguracion> rs = sql.query(con, select_sql, params, SelectConfiguracion.class);
			for (SelectConfiguracion selectConfiguracion : rs) {
				sql.batch(con, update_sql, lista, UpdateConfiguracion.class);
				
				salida.p_Respuesta.add(new SalidaParametrosConfiguracion("UI_ACTUALIZADO_OK"));
				
				Utils.addLog(con, "BSCL", entrada.p_IdEmpresa, entrada.p_IdUsuario, "PLA", "MODIFICACION", 0, 0, "Configuracion", data.p_Codigo, selectConfiguracion.Valor, data.p_Valor);
			}
			
		}else{
			salida.P_OUT_ERRORESCONTADOR++;
			salida.P_OUT_ERRORESDETALLE.add(new ServicioSalidaError("UI_ERROR", "UI_MODULO_NO_EXISTE"));
		}
		
		return salida;
	}	
	
	/**********************
	 * main pruebas
	 **********************/
	public static void main(String[] args){		
        System.out.println("== START ==");
        try {
            Connection con = PlataformaUtils.getDESAConnection();
            // ---------------------------------
            try {
                JsonParser parser = new JsonParser();
                JsonObject dataJsonIn = parser.parse("{\"p_IdEmpresa\":1,\"p_IdUsuario\":25,\"p_IdModulo\":\"PLA\",\"p_SimularOperacion\":\"N\",\"p_Modulo\":\"PLATAFORMA\",\"p_Codigo\":\"PLA.ClaveUsuarioTiempoBloqueo\",\"p_Valor\":\"15\"}").getAsJsonObject();
                ModificarParametrosConfiguracion demo = new ModificarParametrosConfiguracion();
                System.out.println(demo.call(con, dataJsonIn).toString());
                con.commit();
            } catch (Exception er) {
                er.printStackTrace();
                con.rollback();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("== END ==");       
	}
}
