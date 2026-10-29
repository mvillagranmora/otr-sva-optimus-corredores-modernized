package cl.bcs.mesa.MES;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.util.HashMap;
import java.util.List;

import com.google.gson.JsonObject;

import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;

public class ConsultarInstrumentoTipoReajuste implements JavaCall {

	private final String SQL_CONSULTAR_INSTRUMENTO_TIPO_REAJUSTE = loadResourceToString("cl/bcs/mesa/sql/ConsultarInstrumentoTipoReajuste.sql");
		
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
	public List<InformacionInstrumentoTipoReajuste> logica(Connection conn, List<Long> IdInstrumento) throws Exception {
		
		SQL _sql = new SQL();
		HashMap<String,Object> params = new HashMap<>();
		params.put("IdInstrumento", IdInstrumento);
		
		List<InformacionInstrumentoTipoReajuste> instrumentos = _sql.query(conn, SQL_CONSULTAR_INSTRUMENTO_TIPO_REAJUSTE, params, InformacionInstrumentoTipoReajuste.class);
		if (instrumentos.size() == 0)
			throw new Exception("instrumento no existe");
		return instrumentos;
	}
	
	public static class InformacionInstrumentoTipoReajuste {
		public Long IdInstrumento;
		public String Nemotecnico;
		public String IdTipoInstrumento;
		public Date FechaEmision;
		public String IdClasificacionTipoInstrumento;
		public String IdTipoReajuste;
		public String NombreTipoReajuste;
		public String IdMoneda;
		public BigDecimal CorteMinimo;
		public String Codigo;
		public String Reajustable;
		public String IdEstado;
	}
	
}