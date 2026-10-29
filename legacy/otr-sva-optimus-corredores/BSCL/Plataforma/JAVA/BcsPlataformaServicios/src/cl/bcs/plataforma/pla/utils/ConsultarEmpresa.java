package cl.bcs.plataforma.pla.utils;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;

import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;

public class ConsultarEmpresa implements JavaCall {

	private final String SQL_CONSULTAR_EMPRESA_SELECT = loadResourceToString("cl/bcs/plataforma/pla/sql/ConsultarEmpresa_Select.sql");
	
	static final Logger logger = LogManager.getLogger(ConsultarEmpresa.class);
		
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Trace
	public InformacionEmpresa logica(Connection conn, Long idEmpresa) throws Exception {
		
		SQL _sql = new SQL();
		HashMap<String,Object> params = new HashMap<>();
		params.put("IdEmpresa", idEmpresa);
		logger.debug("INICIO::SQL_CONSULTAR_EMPRESA_SELECT");
		List<InformacionEmpresa> empresas = _sql.query(conn, SQL_CONSULTAR_EMPRESA_SELECT, params, InformacionEmpresa.class);
		logger.debug("FIN::SQL_CONSULTAR_EMPRESA_SELECT");
		if (empresas.size() == 0)
			throw new Exception("Empresa no existe");
		return empresas.get(0);
	}
	
	public static class InformacionEmpresa {
		public Long IdEmpresa;
		public String Codigo;
		public String Nombre;
		public String IdEstado;
		public Long IdPais;
		public Long IdSocioDeNegocio;
		public Long IdContraparte;
		public Long IdPersona;
		public Long IdCliente;
		public Long IdCorredora;
		public java.sql.Timestamp FechaIngreso;
		public java.sql.Timestamp FechaCreacion;
		public java.sql.Timestamp FechaModificacion;
		public Long IdDocumentoIdentidad;
		public String DocumentoIdentidad;
		public String NombreCompleto;
		public String DocumentoIdentidadRepresentante;
		public String DireccionElectronicaPrincipal;
		public String DireccionFisicaPrincipalCalle;
		public String DireccionFisicaPrincipalNumero;
		public String DireccionFisicaPrincipalDepartamento;
		public String DireccionFisicaPrincipalSector;
		public String DireccionFisicaPrincipalNombreCiudad;
		public String DireccionFisicaPrincipalNombreDivisionPais;
		public String TelefonoPrincipal;
		public String CodigoPais;
		public String NombrePais;
		public String CodigoInscripcion;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
		
		
	}
	
}