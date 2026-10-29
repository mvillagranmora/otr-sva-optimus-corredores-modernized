package cl.bcs.replica.domain;

/**
 * Input for initiating a replica (Full or Delta).
 */
public record EntradaIniciarReplica(Long idEmpresa, Long idUsuario) {
}
