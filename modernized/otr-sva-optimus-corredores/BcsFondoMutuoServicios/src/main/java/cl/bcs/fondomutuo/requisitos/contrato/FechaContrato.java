package cl.bcs.fondomutuo.requisitos.contrato;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

/**
 * Contract (legacy field names and defaults) of the collaborator {@code CalcularFechaSolicitudAporteRescate}, which step 3A ports. The 3B classes
 * depend on {@link Puerto} only; step 4 binds the 3A service to it.
 */
public final class FechaContrato {

    private FechaContrato() {
    }

    /** Port of 3B toward {@code CalcularFechaSolicitudAporteRescate.logica}. */
    @FunctionalInterface
    public interface Puerto {
        Salida calcular(Entrada entrada) throws Exception;
    }

    public static class Entrada {
        public Long p_IdEmpresa;
        public Long p_IdPais;
        public List<InFechas> p_SolicitudAporteRescate = new ArrayList<>();
    }

    public static class InFechas {
        public Long IdInterno;
        public String IdSerieFondo;
        public String FechaEmision;
        public String EstadoFondo;
        public String EstadoLiquidacion;
        public Long NumeroDiasRetencion;
        public Long NumeroValutaValorCuota;
        public Long NumeroValutaCertificacion;
        public Long NumeroValutaCurse;
        public String SolicitudBatch;
        public String TipoSolicitud;
        public String FondoExtranjero;
        public Long IdPaisFondo;
        public Long IdPaisEmisor;
        public Long IdPaisMoneda;
        public String Switch = "N";
        public String RescateGeneraSpotCertificacion = "N";
        public String FechaCertificacionRescateOrigen = "0001-01-01";
        public String FechaLiquidacionRescateOrigen = "0001-01-01";
        public String FechaValorCuotaRescateOrigen = "0001-01-01";
        public String FondoNacionalDolar;
        public String IdMercadoFondoMutuo = "";
    }

    public static class Salida {
        public List<ResultadoFecha> P_FECHASAPORTERESCATE = new ArrayList<>();
    }

    public static class ResultadoFecha {
        public Long IdInterno;
        public String IdSerieFondo;
        public Date FechaEmision;
        public Date FechaDigitacion;
        public Date FechaAceptacion;
        public Date FechaValorCuota;
        public Date FechaCertificacion;
        public Date FechaLiquidacion;
    }
}
