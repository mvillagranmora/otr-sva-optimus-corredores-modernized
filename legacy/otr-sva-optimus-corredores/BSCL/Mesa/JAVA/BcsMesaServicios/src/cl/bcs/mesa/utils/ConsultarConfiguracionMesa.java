package cl.bcs.mesa.utils;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;

import com.google.gson.JsonObject;

import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;

public class ConsultarConfiguracionMesa implements JavaCall {

	private final String SQL_SELECT_CONFIGURACION_MESA = loadResourceToString("cl/bcs/mesa/sql/ConsultarConfiguracionMesa.sql");
		
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
	public ConfiguracionMesa logica(Connection conn, Long idEmpresa, String idTipoProducto, String codigoParametro) throws Exception {
		SQL _sql = new SQL();
		HashMap<String,Object> params = new HashMap<>();
		params.put("IdEmpresa", idEmpresa);
		params.put("IdTipoProducto", idTipoProducto);
		params.put("CodigoParametro", codigoParametro);
		
		List<ConfiguracionMesa> configuracionMesa = _sql.query(conn, SQL_SELECT_CONFIGURACION_MESA, params, ConfiguracionMesa.class);
		if (configuracionMesa.size() == 0)
			throw new Exception("No existe configuracion de mesa para los parametros ingresados");
		return configuracionMesa.get(0);
	}
	
	public static class ConfiguracionMesa{
		public Long IdEmpresa;
        public String IdTipoProducto;
        public String CodigoParametro;
        public String Descripcion;
        public String Valor;
        public String ModificadorParametro = "";
	}
}