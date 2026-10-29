package cl.bcs.mesa.utils;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;

import com.google.gson.JsonObject;

import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;

public class ConsultarParametroMesa implements JavaCall {

	private final String SQL_SELECT_PARAMETRO_MESA = loadResourceToString("cl/bcs/mesa/sql/ConsultarParametroMesa.sql");
		
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
	public ParametroMesa logica(Connection conn, Long idEmpresa, String nombreTipoParametro, String nombreParametro, String idEstado) throws Exception {
		SQL _sql = new SQL();
		HashMap<String,Object> params = new HashMap<>();
		params.put("IdEmpresa", idEmpresa);
		params.put("NombreTipoParametro", nombreTipoParametro);
		params.put("NombreParametro", nombreParametro);
		params.put("IdEstado", idEstado);
		
		List<ParametroMesa> parametroMesa = _sql.query(conn, SQL_SELECT_PARAMETRO_MESA, params, ParametroMesa.class);
		if (parametroMesa.size() == 0)
			throw new Exception("No existe parametro de mesa para los valores ingresados");
		return parametroMesa.get(0);
	}
	
	public static class ParametroMesa{
		public Long IdEmpresa;
        public Long IdParametro;
        public String NombreTipoParametro;
        public String ValorParametro;
        public String IdEstado;
        public String NombreParametro;
        public Long NumeroDiaSemana;
	}
}