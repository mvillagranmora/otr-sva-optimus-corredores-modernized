package cl.bcs.socionegocio.soc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;

import cl.bcs.socionegocio.pojo.Enrolamiento;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;

public class ConsultarEnrolamiento implements JavaCall {
	public String SQL_CONSULTA_ENROLAMIENTO = loadResourceToString("cl/bcs/socionegocio/soc/sql/ConsultarEnrolamiento_Select.sql");
	
	private Logger logger = LogManager.getLogger(ConsultarEnrolamiento.class);
	
	public static class EntradaConsultaEnrolamiento{
		public String codigoPais;
		public Enrolamiento enrolamiento;
	}
	public static class SalidaConsultaEnrolamiento{
		public List<Enrolamiento> enrolamientos;
		@Override
		public String toString() {
			return String.format("SalidaConsultaEnrolamiento [enrolamientos=%s]", enrolamientos);
		}
	}
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
	public SalidaConsultaEnrolamiento logica(Connection conn, EntradaConsultaEnrolamiento entrada) throws SQLException{
		SQL sql = new SQL();
		SalidaConsultaEnrolamiento salida = new SalidaConsultaEnrolamiento();
		
		HashMap<String, Object> params = new HashMap<String,Object>();
		params.put("IdTipoEntidad", entrada.enrolamiento.IdTipoEntidad);
		params.put("IdTipoDocumentoIdentidad", entrada.enrolamiento.IdTipoDocumentoIdentidad);
		params.put("DocumentoIdentidad", entrada.enrolamiento.DocumentoIdentidad);
		params.put("Pais", entrada.codigoPais);
		params.put("Tipo", entrada.enrolamiento.Tipo);
		logger.debug("INICIO::SQL_CONSULTA_ENROLAMIENTO");
		salida.enrolamientos = sql.query(conn, SQL_CONSULTA_ENROLAMIENTO, params, Enrolamiento.class);
		logger.debug("FIN::SQL_CONSULTA_ENROLAMIENTO");
		return salida;
	}

}
