package cl.bcs.contabilidad.domain.model.clienterelacionado;

/**
 * One configured value of a condition group: ITZ.CondicionConceptoNormativo ValorVarchar plus OperadorFuncion
 * ("-" subtracts, anything else adds). Legacy {@code ConsultarCondicionesConceptosNormativos.Valores}.
 */
public record ValorCondicion(String valorVarchar, String operadorFuncion) {
}
