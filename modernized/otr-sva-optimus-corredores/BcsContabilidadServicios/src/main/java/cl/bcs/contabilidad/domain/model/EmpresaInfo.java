package cl.bcs.contabilidad.domain.model;

/**
 * Company information returned by PlataformaPort.consultarEmpresa().
 *
 * Derived from legacy cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa.
 * Only the fields actually used by CentralizaAnticipoPactos are surfaced here;
 * the full structure can be expanded as more centralisations are ported.
 */
public record EmpresaInfo(
        Long   idEmpresa,
        Long   idPais,
        String documentoIdentidad,
        String nombre
) {}
