package cl.bcs.loaderapp.servicios;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConsultarDocumentoIdentidadCliente {
	public String SQL_OBTENER_CLIENTE = "SELECT \"SOC\".\"IdCliente\", \"SOC\".\"IdPersona\", \"SOC\".\"IdContraparte\" FROM \"BSCL.SocioNegocio.BD::PER.DocumentoIdentidad\" as \"DOC\" "
			+ "LEFT JOIN \"BSCL.SocioNegocio.BD::SOC.SocioDeNegocio\" as \"SOC\" on \"SOC\".\"IdPersona\" = \"DOC\".\"IdPersona\" "
			+ "WHERE \"DOC\".\"DocumentoIdentidad\" = :DocumentoIdentidad AND \"DOC\".\"IdTipoDocumentoIdentidad\" = :TipoDocumentoIdentidad AND \"IdEmpresa\" = :IdEmpresa;";
	
	public static class SalidaSocioNegocio{
		public Long IdCliente;
		public Long IdPersona;
		public Long IdContraparte;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	/**
	 * 
	 * @param conn
	 * @param documentoIdentidad
	 * @param tipoDocumentoIdentidad
	 * @param tipoPersona
	 * @param idEmpresa 
	 * @return objeto con los ids del socio de negocio
	 */
	public SalidaSocioNegocio logica(Connection conn, String documentoIdentidad, String tipoDocumentoIdentidad, String tipoPersona, Long idEmpresa){
		SQL sql = new SQL();
		SalidaSocioNegocio salidaSocioNegocio = new SalidaSocioNegocio();
		
		try {
			
			HashMap<String, Object> params = new HashMap<>();
			params.put("DocumentoIdentidad", documentoIdentidad);
			params.put("TipoDocumentoIdentidad", tipoDocumentoIdentidad);
			params.put("IdEmpresa", idEmpresa);
			//params.put("Tipo", tipoPersona);
			List<SalidaSocioNegocio> contadorPersona = sql.query(conn, SQL_OBTENER_CLIENTE, params, SalidaSocioNegocio.class);
			for (SalidaSocioNegocio salida : contadorPersona) {
				return salida;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return salidaSocioNegocio;
	}
	
	public Optional<SalidaSocioNegocio> consultarIUP(Connection conn, String documentoIdentidad, String tipoDocumentoIdentidad, Long idEmpresa){
		SQL sql = new SQL();
		SalidaSocioNegocio salidaSocioNegocio = null;
		
		try {
			
			HashMap<String, Object> params = new HashMap<>();
			params.put("DocumentoIdentidad", documentoIdentidad);
			params.put("TipoDocumentoIdentidad", tipoDocumentoIdentidad);
			params.put("IdEmpresa", idEmpresa);
			List<SalidaSocioNegocio> contadorPersona = sql.query(conn, SQL_OBTENER_CLIENTE, params, SalidaSocioNegocio.class);
			for (SalidaSocioNegocio socio : contadorPersona) {
				salidaSocioNegocio = socio; 
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return Optional.ofNullable(salidaSocioNegocio);
	}
}
