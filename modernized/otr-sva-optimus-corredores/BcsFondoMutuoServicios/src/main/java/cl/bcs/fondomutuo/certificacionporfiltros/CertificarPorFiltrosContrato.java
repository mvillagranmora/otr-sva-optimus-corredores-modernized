package cl.bcs.fondomutuo.certificacionporfiltros;

import java.util.ArrayList;
import java.util.List;

import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato;
import cl.bcs.fondomutuo.kit.MensajeSalida;

/**
 * Legacy in and out of {@code CertificarPorFiltrosSolicitudAporteRescate} (FM-CXF-01..05), field names and declaration order as the legacy has them. The in-process
 * caller passes the typed {@link Entrada}; there is no JSON entry for this class.
 */
public final class CertificarPorFiltrosContrato {

    private CertificarPorFiltrosContrato() {
    }

    public static class Entrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String p_SimularOperacion;
        public String p_UsaReglasNegocio;
        public String p_FechaCertificacion;
        public String p_IdTipoCanalIngreso;
        public List<InFiltros> p_Filtros = new ArrayList<>();
    }

    public static class InFiltros {
        public String IdEmisor;
        public String IdMoneda;
        public String IdSerieFondo;
        public String Nacional;
        public String TipoSolicitud;
        public String IdTipoMovimientoFondoMutuo;
        public String MoneyMarket;
        public Long IdCarga;
    }

    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<MensajeSalida> P_OUT_MENSAJES = new ArrayList<>();
        public List<CertificarAporteRescateContrato.ResultadoCertificacion> P_OUT_RESULTADO = new ArrayList<>();
    }
}
