package cl.bcs.contabilidad.domain.model.basilea;

/**
 * The broker's own account: IdClienteCtaPropia and its RUT. Legacy {@code ConsultarCustodiaPortafolio.DatosCorredora},
 * as used by ProcesarInterfazBasilea.java:290-292.
 */
public record CuentaPropia(Long idClienteCtaPropia, String documentoIdentidadCtaPropia) {
}
