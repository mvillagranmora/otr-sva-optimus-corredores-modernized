package cl.bcs.mesa.MES;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.JsonObject;

import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConsultarTipoInstrumento implements JavaCall {

	private final String SQL_CONSULTAR_TIPO_INSTRUMENTO = loadResourceToString("cl/bcs/mesa/sql/ConsultarTipoInstrumento.sql");
	public static class ConsultarTipoInstrumentoIN{
		//public List<Long> IdTipoInstrumento;
		public List<String> IdTipoOperacion;

		public ConsultarTipoInstrumentoIN() {
			this.IdTipoOperacion = new ArrayList<>();
		}
		
	}
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
	public List<InformacionTipoInstrumento> logica(Connection conn, ConsultarTipoInstrumentoIN entrada) throws Exception {
		
		SQL _sql = new SQL();
		HashMap<String,Object> params = new HashMap<>();
		//params.put("IdTipoInstrumento", entrada.IdTipoInstrumento);
		params.put("IdTipoOperacion", entrada.IdTipoOperacion);
		
		List<InformacionTipoInstrumento> instrumentos = _sql.query(conn, SQL_CONSULTAR_TIPO_INSTRUMENTO, params, InformacionTipoInstrumento.class);
		if (instrumentos.size() == 0)
			throw new Exception("instrumento no existe");
		return instrumentos;
	}
	
	public static class InformacionTipoInstrumento {
		public String IdTipoInstrumento; 
		public String CodigoTipoInstrumento; 
		public String NombreTipoInstrumento; 
		public String NombreLargoTipoInstrumento; 
		public String DescripcionTipoInstrumento; 
		public String IdEstado;
		public String ClasificacionTipoInstrumento; 
		public String IdClasificacionTipoInstrumento; 
		public String IdBolsa;        
		public String IdTipoOperacion;
		public Long IdInstrumento;
		public String Nemotecnico; 
		public String Accion; 
		public String EsOrden; 
		public String EsVentaCorta; 
		public String EsTransaccion; 
		public String Comprobante;
		public String ExigeGarantia;
		public Long IdContraparteDestinoGarantia;     
		public Long IdEntidadDestinoGarantia;
		public Long IdBolsaTipoInstrumento;
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	}
	
}