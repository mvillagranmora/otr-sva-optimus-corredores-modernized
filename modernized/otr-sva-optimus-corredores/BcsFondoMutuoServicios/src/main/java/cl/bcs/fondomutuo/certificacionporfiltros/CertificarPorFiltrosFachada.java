package cl.bcs.fondomutuo.certificacionporfiltros;

import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescatePort;
import cl.bcs.fondomutuo.certificacionporfiltros.CertificarPorFiltrosContrato.Entrada;
import cl.bcs.fondomutuo.certificacionporfiltros.CertificarPorFiltrosContrato.InFiltros;
import cl.bcs.fondomutuo.certificacionporfiltros.CertificarPorFiltrosContrato.Salida;

/**
 * Legacy {@code CertificarPorFiltrosSolicitudAporteRescate.logica} (FM-CXF-01..05): every filter is read in order, the rows pile up WITHOUT de-duplication
 * (Certificar rejects the repeated ones, FM-CXF-03) and Certificar is called exactly once, also with an empty list (FM-CXF-04). The lists of Certificar's answer are shared, not copied, as in the legacy. Quirks kept: a null
 * {@code p_Filtros} or a null answer of Certificar is a {@link NullPointerException}; whatever the read or Certificar throws goes up unchanged.
 */
public class CertificarPorFiltrosFachada implements CertificarPorFiltrosPort {

    private final CertificarAporteRescatePort certificar;
    private final CertificarPorFiltrosDatos datos;

    public CertificarPorFiltrosFachada(CertificarAporteRescatePort certificar, CertificarPorFiltrosDatos datos) {
        this.certificar = certificar;
        this.datos = datos;
    }

    @Override
    public Salida logica(Entrada entrada) throws Exception {
        CertificarAporteRescateContrato.Entrada paraCertificar = new CertificarAporteRescateContrato.Entrada();
        paraCertificar.p_IdEmpresa = entrada.p_IdEmpresa;
        paraCertificar.p_IdUsuario = entrada.p_IdUsuario;
        paraCertificar.p_IdModulo = entrada.p_IdModulo;
        paraCertificar.p_SimularOperacion = entrada.p_SimularOperacion;
        paraCertificar.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
        paraCertificar.p_FechaCertificacion = entrada.p_FechaCertificacion;
        paraCertificar.p_IdTipoCanalIngreso = entrada.p_IdTipoCanalIngreso;
        for (InFiltros filtro : entrada.p_Filtros) {
            paraCertificar.p_Solicitudes.addAll(datos.solicitudes(filtro, entrada.p_FechaCertificacion, entrada.p_IdEmpresa));
        }
        CertificarAporteRescateContrato.Salida salida = certificar.logica(paraCertificar);
        Salida out = new Salida();
        out.P_OUT_ERRORESCONTADOR = salida.P_OUT_ERRORESCONTADOR;
        out.P_OUT_MENSAJES = salida.P_OUT_MENSAJES;
        out.P_OUT_RESULTADO = salida.P_OUT_RESULTADO;
        return out;
    }
}
