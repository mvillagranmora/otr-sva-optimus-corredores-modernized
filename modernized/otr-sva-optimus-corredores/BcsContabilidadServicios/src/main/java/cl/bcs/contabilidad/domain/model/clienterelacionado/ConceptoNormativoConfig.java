package cl.bcs.contabilidad.domain.model.clienterelacionado;

import java.util.List;

/**
 * A configured normative concept with its condition groups (ITZ.ConceptoNormativo + ITZ.CondicionConceptoNormativo).
 * Legacy {@code ConsultarCondicionesConceptosNormativos.ConceptoNormativo}. {@code condiciones} may be null or empty;
 * such a concept is reported with every total at zero (CONT-D-10).
 */
public record ConceptoNormativoConfig(Long idConceptoNormativo, String conceptoNormativo,
                                      List<CondicionConcepto> condiciones) {
}
