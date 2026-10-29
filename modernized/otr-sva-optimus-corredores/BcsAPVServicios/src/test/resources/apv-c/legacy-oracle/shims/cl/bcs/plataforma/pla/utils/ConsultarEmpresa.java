package cl.bcs.plataforma.pla.utils;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import cl.bcs.utils.Utils;
import cl.bcs.utils.JavaCall;
import cl.bcs.oracle.Hooks;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import com.google.gson.JsonObject;

/**
 * ORACLE FAKE of cl.bcs.plataforma.pla.utils.ConsultarEmpresa.
 */
public class ConsultarEmpresa implements JavaCall {

	// legacy cl/bcs/plataforma/pla/utils/ConsultarEmpresa.java:43-78 (verbatim)
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


	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception { return null; }

	/** FAKE of the legacy logica: records its input (Hooks.registrar) and answers the script colaboradores.<Servicio> of the case (see oracle/Hooks.java); nothing of the legacy behaviour is reproduced. */
	public InformacionEmpresa logica(Connection conn, Long idEmpresa) throws Exception {
		java.util.Map<String, Object> entrada = new java.util.TreeMap<>();
		entrada.put("idEmpresa", idEmpresa);
		Hooks.registrar("ConsultarEmpresa", entrada);
		return Hooks.salida("ConsultarEmpresa", InformacionEmpresa.class);
	}

}
