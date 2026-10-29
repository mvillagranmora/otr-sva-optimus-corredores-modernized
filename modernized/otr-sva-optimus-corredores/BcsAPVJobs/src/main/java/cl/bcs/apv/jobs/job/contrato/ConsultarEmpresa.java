package cl.bcs.apv.jobs.job.contrato;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import cl.bcs.apv.jobs.job.JsonLegacy;

/**
 * JSON contract of the legacy collaborator {@code cl.bcs.plataforma.pla.utils.ConsultarEmpresa} (the Colaboradores port carries its input/output as the legacy JSON). The nested data
 * classes are VERBATIM copies of the legacy source (field names ARE the contract, constructor defaults included); only {@code toString} uses
 * {@link JsonLegacy}. Generated from the oracle shims by the Step 2 scratch script; nothing is retyped.
 */
@SuppressWarnings({"unused", "rawtypes"})
public final class ConsultarEmpresa {

    private ConsultarEmpresa() {
    }

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
			return JsonLegacy.texto(this);
		}
		
		
	}
}
