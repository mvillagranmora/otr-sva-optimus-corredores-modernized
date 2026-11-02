// GENERATED DOUBLE of the legacy cl.bcs.plataforma.pla.utils.ConsultarEmpresa by gen_doubles.py (method bodies replaced; declarations are the legacy ones)
package cl.bcs.plataforma.pla.utils;

import java.sql.Connection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;

import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;

public class ConsultarEmpresa implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(ConsultarEmpresa.class);
		
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception { return cl.bcs.oracle.Doubles.answer("ConsultarEmpresa", "call", new Object[]{conn, in}, JsonObject.class); }
	
	@Trace
	public InformacionEmpresa logica(Connection conn, Long idEmpresa) throws Exception { return cl.bcs.oracle.Doubles.answer("ConsultarEmpresa", "logica", new Object[]{conn, idEmpresa}, InformacionEmpresa.class); }
	
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