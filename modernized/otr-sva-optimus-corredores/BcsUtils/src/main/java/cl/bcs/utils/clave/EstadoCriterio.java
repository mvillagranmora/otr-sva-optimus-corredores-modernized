package cl.bcs.utils.clave;

/** RULE-031: one criterion of the new-password check and whether it holds (legacy EstadoCriteriosNuevaClave). */
public record EstadoCriterio(String criterio, boolean esValido) {
}
