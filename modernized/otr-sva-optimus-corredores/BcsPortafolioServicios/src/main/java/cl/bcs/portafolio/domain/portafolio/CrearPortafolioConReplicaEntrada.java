package cl.bcs.portafolio.domain.portafolio;

public record CrearPortafolioConReplicaEntrada(
    CrearPortafolioEntrada base,
    String replicaPortafolio,
    String propositoPortafolioReplica,
    String categoriaInversionista
) {}
