package cl.bcs.contabilidad.domain.port.out;

import cl.bcs.contabilidad.domain.model.clienterelacionado.ConceptoNormativoConfig;
import cl.bcs.contabilidad.domain.model.clienterelacionado.FuentesOperaciones;

import java.util.List;

/**
 * Reads for the related-client report (legacy ConsultarOperacionesClienteRelacionado,
 * ConsultarCondicionesConceptosNormativos and GenerarInformeClienteRelacionado). Failures propagate as runtime
 * exceptions; the services turn their message into the legacy error output.
 */
public interface ClienteRelacionadoRepository {

    /**
     * Every normative concept with its condition groups (ConsultarCondicionesConceptosNormativos_SelectConceptosConfiguraciones.sql).
     */
    List<ConceptoNormativoConfig> consultarConceptosNormativos();

    /**
     * The seven ConsultarOperacionesClienteRelacionado_Select*.sql result sets. The dates are passed to SQL unchanged as
     * yyyy-MM-dd strings (the SQL compares {@code TO_VARCHAR(.., 'YYYY-MM-DD') BETWEEN :FechaInicio AND :FechaFin}).
     */
    FuentesOperaciones consultarOperaciones(String fechaInicio, String fechaFin);

    /** Latest LOGO_CORREDOR image of the company, or an empty array when there is none. */
    byte[] consultarLogoCorredora(Long idEmpresa);
}
