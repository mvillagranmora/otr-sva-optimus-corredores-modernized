package cl.bcs.fondomutuo.certificacionporfiltros;

import java.util.List;

import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.InSolicitudes;
import cl.bcs.fondomutuo.certificacionporfiltros.CertificarPorFiltrosContrato.InFiltros;

/** The only read of the class: one call is one {@code SELECT DISTINCT} of the legacy (FM-CXF-01). */
public interface CertificarPorFiltrosDatos {

    List<InSolicitudes> solicitudes(InFiltros filtro, String fechaCertificacion, Long idEmpresa);
}
